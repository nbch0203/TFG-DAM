import crypto from 'crypto';
import fs from 'fs/promises';
const FCM_SCOPE = 'https://www.googleapis.com/auth/firebase.messaging';
const DEFAULT_TOKEN_URI = 'https://oauth2.googleapis.com/token';
const DEFAULT_FCM_ENDPOINT = 'https://fcm.googleapis.com/v1/projects/{projectId}/messages:send';
let cachedAccessToken = null;
let cachedAccessTokenExpiresAt = 0;
let cachedServiceAccount = null;
const base64UrlEncode = (value) => Buffer.from(value).toString('base64')
  .replace(/=/g, '')
  .replace(/\+/g, '-')
  .replace(/\//g, '_');
const readServiceAccountSource = async () => {
  if (cachedServiceAccount) return cachedServiceAccount;
  const jsonEnv = process.env.FIREBASE_SERVICE_ACCOUNT_JSON?.trim();
  const pathEnv = process.env.FIREBASE_SERVICE_ACCOUNT_PATH?.trim() || process.env.GOOGLE_APPLICATION_CREDENTIALS?.trim();
  let raw = null;
  if (jsonEnv) {
    raw = jsonEnv;
  } else if (pathEnv) {
    raw = await fs.readFile(pathEnv, 'utf8');
  }
  if (!raw) {
    throw new Error('No se encontró configuración de Firebase. Define FIREBASE_SERVICE_ACCOUNT_JSON o FIREBASE_SERVICE_ACCOUNT_PATH.');
  }
  const parsed = JSON.parse(raw);
  const serviceAccount = {
    projectId: parsed.project_id || process.env.FIREBASE_PROJECT_ID,
    clientEmail: parsed.client_email,
    privateKey: parsed.private_key ? String(parsed.private_key).replace(/\\n/g, '\n') : null,
    tokenUri: parsed.token_uri || DEFAULT_TOKEN_URI,
  };
  if (!serviceAccount.projectId) {
    throw new Error('Falta FIREBASE_PROJECT_ID o project_id en el service account de Firebase.');
  }
  if (!serviceAccount.clientEmail || !serviceAccount.privateKey) {
    throw new Error('El service account de Firebase debe incluir client_email y private_key.');
  }
  cachedServiceAccount = serviceAccount;
  return serviceAccount;
};
const buildServiceAccountJwt = (serviceAccount) => {
  const now = Math.floor(Date.now() / 1000);
  const header = { alg: 'RS256', typ: 'JWT' };
  const payload = {
    iss: serviceAccount.clientEmail,
    scope: FCM_SCOPE,
    aud: serviceAccount.tokenUri,
    iat: now,
    exp: now + 3600,
  };
  const signingInput = `${base64UrlEncode(JSON.stringify(header))}.${base64UrlEncode(JSON.stringify(payload))}`;
  const signer = crypto.createSign('RSA-SHA256');
  signer.update(signingInput);
  signer.end();
  const signature = signer.sign(serviceAccount.privateKey, 'base64')
    .replace(/=/g, '')
    .replace(/\+/g, '-')
    .replace(/\//g, '_');
  return `${signingInput}.${signature}`;
};
const getAccessToken = async () => {
  const serviceAccount = await readServiceAccountSource();
  const now = Date.now();
  if (cachedAccessToken && cachedAccessTokenExpiresAt - now > 60_000) {
    return cachedAccessToken;
  }
  const assertion = buildServiceAccountJwt(serviceAccount);
  const body = new URLSearchParams({
    grant_type: 'urn:ietf:params:oauth:grant-type:jwt-bearer',
    assertion,
  });
  const response = await fetch(serviceAccount.tokenUri, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body,
  });
  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`No se pudo obtener el access token de Google: ${response.status} ${errorText}`);
  }
  const data = await response.json();
  if (!data.access_token) {
    throw new Error('Google no devolvió un access token válido para Firebase.');
  }
  cachedAccessToken = data.access_token;
  cachedAccessTokenExpiresAt = now + ((Number(data.expires_in) || 3600) * 1000);
  return cachedAccessToken;
};
const normalizeDataPayload = (data = {}) => Object.fromEntries(
  Object.entries(data)
    .filter(([, value]) => value !== undefined && value !== null)
    .map(([key, value]) => [key, String(value)])
);
export const sendFcmMessage = async ({ token, title, body, data = {} }) => {
  if (!token || String(token).trim() === '') {
    return { skipped: true, reason: 'token_vacio' };
  }
  const serviceAccount = await readServiceAccountSource();
  const accessToken = await getAccessToken();
  const endpoint = DEFAULT_FCM_ENDPOINT.replace('{projectId}', serviceAccount.projectId);
  const response = await fetch(endpoint, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${accessToken}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      message: {
        token,
        notification: {
          title: title || 'SchoolSafeTrack',
          body: body || '',
        },
        data: normalizeDataPayload(data),
      },
    }),
  });
  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`FCM respondió ${response.status}: ${errorText}`);
  }
  return response.json();
};
export const sendFcmToTokens = async ({ tokens = [], title, body, data = {} }) => {
  const uniqueTokens = [...new Set(tokens.map((token) => String(token || '').trim()).filter(Boolean))];
  if (uniqueTokens.length === 0) {
    return { sent: 0, failed: 0, skipped: true };
  }
  const results = await Promise.allSettled(
    uniqueTokens.map((token) => sendFcmMessage({ token, title, body, data }))
  );
  return {
    sent: results.filter((result) => result.status === 'fulfilled').length,
    failed: results.filter((result) => result.status === 'rejected').length,
    results,
  };
};
