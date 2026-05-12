import dotenv from 'dotenv';
import fs from 'fs';
import mysql from 'mysql2/promise';
import { fileURLToPath } from 'url';
import path from 'path';
import { sendFcmMessage } from '../fcm.js';

dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const backendRoot = path.resolve(__dirname, '..');

const PLACEHOLDER_VALUES = new Set([
  'tu-endpoint-mysql-aws',
  'usuario',
  'contraseña',
  'tu-project-id',
  '/ruta/al/service-account.json',
]);

const parseArgs = (argv) => {
  const options = {
    title: 'Prueba de notificación',
    body: 'Esta es una notificación de prueba para verificar que FCM llega correctamente al usuario padre.',
  };

  argv.forEach((arg) => {
    if (arg === '--help' || arg === '-h') {
      options.help = true;
      return;
    }

    if (arg.startsWith('--title=')) {
      options.title = arg.slice('--title='.length).trim() || options.title;
      return;
    }

    if (arg.startsWith('--body=')) {
      options.body = arg.slice('--body='.length).trim() || options.body;
    }
  });

  return options;
};

const createPool = () => {
  const sslCaPath = path.join(backendRoot, 'global-bundle.pem');
  const sslConfig = fs.existsSync(sslCaPath)
    ? {
        rejectUnauthorized: true,
        ca: fs.readFileSync(sslCaPath),
      }
    : undefined;

  return mysql.createPool({
    host: process.env.DB_HOST || 'localhost',
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '',
    database: process.env.DB_NAME || 'schooltrack',
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0,
    charset: 'utf8mb4',
    ...(sslConfig ? { ssl: sslConfig } : {}),
  });
};

const validateEnvironment = () => {
  const dbHost = String(process.env.DB_HOST || '').trim();
  const dbUser = String(process.env.DB_USER || '').trim();
  const dbPassword = String(process.env.DB_PASSWORD || '').trim();
  const dbName = String(process.env.DB_NAME || '').trim();

  const invalidFields = [];

  if (!dbHost || PLACEHOLDER_VALUES.has(dbHost)) invalidFields.push('DB_HOST');
  if (!dbUser || PLACEHOLDER_VALUES.has(dbUser)) invalidFields.push('DB_USER');
  if (!dbPassword || PLACEHOLDER_VALUES.has(dbPassword)) invalidFields.push('DB_PASSWORD');
  if (!dbName || PLACEHOLDER_VALUES.has(dbName)) invalidFields.push('DB_NAME');

  if (invalidFields.length > 0) {
    throw new Error(
      `Variables de MySQL no configuradas correctamente: ${invalidFields.join(', ')}. ` +
      'Sustituye los valores de ejemplo en backend/.env por los datos reales de tu base de datos.'
    );
  }
};

const getParentTokens = async (pool) => {
  const [rows] = await pool.query(
    `SELECT DISTINCT
       p.id AS parent_id,
       p.email AS parent_email,
       dt.token AS device_token
     FROM users p
     INNER JOIN device_tokens dt ON dt.user_id = p.id
       AND dt.activo = 1
     WHERE p.role = 'PARENT'
       AND p.activo = 1
       AND dt.token IS NOT NULL
       AND TRIM(dt.token) <> ''
     ORDER BY p.id ASC, dt.updated_at DESC`
  );

  return rows;
};

const main = async () => {
  const options = parseArgs(process.argv.slice(2));

  if (options.help) {
    console.log('Uso: npm run test:push:parents -- [--title="..."] [--body="..."]');
    process.exit(0);
  }

  validateEnvironment();

  const pool = createPool();

  try {
    const recipients = await getParentTokens(pool);

    if (recipients.length === 0) {
      console.log('No hay padres con token FCM activo para enviar la prueba.');
      process.exitCode = 0;
      return;
    }

    console.log(`Destinatarios encontrados: ${recipients.length}`);

    const results = await Promise.allSettled(
      recipients.map((recipient) =>
        sendFcmMessage({
          token: recipient.device_token,
          title: options.title,
          body: options.body,
          data: {
            type: 'TEST_NOTIFICATION',
            targetRole: 'PARENT',
            parentId: recipient.parent_id,
            parentEmail: recipient.parent_email,
            source: 'backend-test-script',
          },
        })
      )
    );

    const summary = results.reduce(
      (accumulator, result, index) => {
        if (result.status === 'fulfilled') {
          accumulator.sent += 1;
          return accumulator;
        }

        accumulator.failed += 1;
        const recipient = recipients[index];
        accumulator.errors.push({
          parentId: recipient.parent_id,
          parentEmail: recipient.parent_email,
          message: result.reason instanceof Error ? result.reason.message : String(result.reason),
        });
        return accumulator;
      },
      { sent: 0, failed: 0, errors: [] }
    );

    console.log(`Enviadas: ${summary.sent}`);
    console.log(`Fallidas: ${summary.failed}`);

    if (summary.errors.length > 0) {
      console.log('Errores:');
      summary.errors.forEach((error) => {
        console.log(`- parentId=${error.parentId} parentEmail=${error.parentEmail} error=${error.message}`);
      });
      process.exitCode = 1;
      return;
    }

    process.exitCode = 0;
  } finally {
    await pool.end().catch(() => {});
  }
};

main().catch((error) => {
  console.error('Error ejecutando la prueba de notificaciones FCM:', error);
  process.exit(1);
});