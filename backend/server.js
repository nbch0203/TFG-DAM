// CONFIGURACIÓN INICIAL

import dotenv from 'dotenv';
import express from 'express';
import cors from 'cors';
import bodyParser from 'body-parser';
import mysql from 'mysql2/promise';
import bcrypt from 'bcrypt';
import os from 'os';
import { exec } from 'child_process';
import util from 'util';

dotenv.config();

const app = express();
app.use(cors());
app.use(bodyParser.json({ charset: 'utf-8' }));

// Corrige texto mojibake típico (ej. "MaÃ±ana" -> "Mañana") en toda respuesta JSON.
const looksLikeMojibake = (value = '') => /Ã.|Â.|â.|ðŸ|�/.test(value);

const normalizeMojibakeString = (value) => {
  if (typeof value !== 'string') return value;
  if (!looksLikeMojibake(value)) return value;

  try {
    const decoded = Buffer.from(value, 'latin1').toString('utf8');
    // Evita reemplazar por una cadena peor si la conversión falla.
    if (!decoded || decoded.includes('�')) {
      return value;
    }
    return decoded;
  } catch {
    return value;
  }
};

const normalizeMojibakePayload = (payload) => {
  if (payload instanceof Date) {
    return payload;
  }

  if (Array.isArray(payload)) {
    return payload.map((item) => normalizeMojibakePayload(item));
  }

  if (payload && typeof payload === 'object') {
    const normalized = {};
    Object.entries(payload).forEach(([key, value]) => {
      normalized[key] = normalizeMojibakePayload(value);
    });
    return normalized;
  }

  return normalizeMojibakeString(payload);
};

app.use((req, res, next) => {
  const originalJson = res.json.bind(res);
  res.json = (payload) => originalJson(normalizeMojibakePayload(payload));
  next();
});

app.use((req, res, next) => {
  res.set('Content-Type', 'application/json; charset=utf-8');
  next();
});

const execAsync = util.promisify(exec);

// Conexión a base de datos 
const pool = mysql.createPool({
  host: process.env.DB_HOST || 'localhost',
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '',
  database: process.env.DB_NAME || 'schooltrack',
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0,
  charset: 'utf8mb4'
});

const columnExists = async (tableName, columnName) => {
  const [rows] = await pool.query(
    `SELECT COUNT(*) AS total
     FROM information_schema.columns
     WHERE table_schema = DATABASE()
       AND table_name = ?
       AND column_name = ?`,
    [tableName, columnName]
  );
  return Number(rows[0]?.total || 0) > 0;
};

const ensureOperationalTables = async () => {
  // Actualizar rol de usuarios para incluir PROFESOR si es necesario
  try {
    await pool.query(`
      ALTER TABLE users MODIFY COLUMN role ENUM('PARENT','DRIVER','ADMIN','PROFESOR') NOT NULL DEFAULT 'PARENT'
    `);
  } catch (err) {
    // Ignorar si ya existe o hay otro error
    console.warn('No se pudo modificar columna role:', err.message);
  }

  // Asegurar que las columnas nombre y apellidos existen en la tabla users
  const usersHasNombre = await columnExists('users', 'nombre');
  if (!usersHasNombre) {
    try {
      await pool.query(`ALTER TABLE users ADD COLUMN nombre VARCHAR(100) NOT NULL DEFAULT '' AFTER password`);
      // Inicializar nombre con el email del usuario como fallback
      await pool.query(`UPDATE users SET nombre = email WHERE nombre = ''`);
    } catch (err) {
      console.warn('No se pudo añadir columna nombre a users:', err.message);
    }
  }

  const usersHasApellidos = await columnExists('users', 'apellidos');
  if (!usersHasApellidos) {
    try {
      await pool.query(`ALTER TABLE users ADD COLUMN apellidos VARCHAR(100) NULL AFTER nombre`);
    } catch (err) {
      console.warn('No se pudo añadir columna apellidos a users:', err.message);
    }
  }

  // Corregir usuarios de prueba que tienen el hash de contraseña placeholder '$2a$10$YourHashedPasswordHere'.
  // Estos usuarios no pueden iniciar sesión porque el hash no es válido.
  // Se les asigna la contraseña 'password123' como contraseña de prueba.
  try {
    const [usersWithPlaceholder] = await pool.query(
      `SELECT id FROM users WHERE password = '$2a$10$YourHashedPasswordHere'`
    );
    if (usersWithPlaceholder.length > 0) {
      const fixedHash = await bcrypt.hash('password123', 10);
      await pool.query(
        `UPDATE users SET password = ? WHERE password = '$2a$10$YourHashedPasswordHere'`,
        [fixedHash]
      );
      console.log(`Migración: contraseña de ${usersWithPlaceholder.length} usuario(s) de prueba corregida a 'password123'.`);
    }
  } catch (err) {
    console.warn('No se pudo corregir contraseñas de prueba:', err.message);
  }

  await pool.query(`
    CREATE TABLE IF NOT EXISTS schools (
      id BIGINT NOT NULL AUTO_INCREMENT,
      nombre VARCHAR(150) NOT NULL,
      direccion VARCHAR(255) NULL,
      telefono VARCHAR(30) NULL,
      email VARCHAR(255) NULL,
      activo TINYINT(1) DEFAULT 1,
      created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      UNIQUE KEY uq_school_nombre (nombre)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  `);

  const [schoolRows] = await pool.query('SELECT id FROM schools ORDER BY id ASC LIMIT 1');
  if (schoolRows.length === 0) {
    await pool.query(
      `INSERT INTO schools (nombre, direccion, telefono, email, activo, created_at, updated_at)
       VALUES ('Colegio Principal', 'Dirección no especificada', NULL, NULL, 1, NOW(), NOW())`
    );
  }

  const [defaultSchoolRows] = await pool.query('SELECT id FROM schools ORDER BY id ASC LIMIT 1');
  const defaultSchoolId = defaultSchoolRows[0]?.id;

  const routesHasSchool = await columnExists('routes', 'school_id');
  if (!routesHasSchool) {
    await pool.query('ALTER TABLE routes ADD COLUMN school_id BIGINT NULL');
    await pool.query('ALTER TABLE routes ADD INDEX idx_routes_school (school_id)');
  }

  const stopsHasSchool = await columnExists('stops', 'school_id');
  if (!stopsHasSchool) {
    await pool.query('ALTER TABLE stops ADD COLUMN school_id BIGINT NULL');
    await pool.query('ALTER TABLE stops ADD INDEX idx_stops_school (school_id)');
  }

  const studentsHasSchool = await columnExists('students', 'school_id');
  if (!studentsHasSchool) {
    await pool.query('ALTER TABLE students ADD COLUMN school_id BIGINT NULL');
    await pool.query('ALTER TABLE students ADD INDEX idx_students_school (school_id)');
  }

  if (defaultSchoolId) {
    await pool.query('UPDATE routes SET school_id = ? WHERE school_id IS NULL', [defaultSchoolId]);
    await pool.query('UPDATE stops SET school_id = ? WHERE school_id IS NULL', [defaultSchoolId]);
    await pool.query('UPDATE students SET school_id = ? WHERE school_id IS NULL', [defaultSchoolId]);
  }

  await pool.query(`
    CREATE TABLE IF NOT EXISTS driver_stop_checkins (
      id BIGINT NOT NULL AUTO_INCREMENT,
      route_assignment_id BIGINT NOT NULL,
      stop_id BIGINT NOT NULL,
      driver_id BIGINT NOT NULL,
      action ENUM('ARRIVAL','DEPARTURE') NOT NULL,
      notes TEXT NULL,
      created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      KEY idx_assignment_stop (route_assignment_id, stop_id),
      KEY idx_driver_created (driver_id, created_at),
      CONSTRAINT driver_stop_checkins_fk_assignment FOREIGN KEY (route_assignment_id) REFERENCES route_assignments(id) ON DELETE CASCADE,
      CONSTRAINT driver_stop_checkins_fk_stop FOREIGN KEY (stop_id) REFERENCES stops(id) ON DELETE CASCADE,
      CONSTRAINT driver_stop_checkins_fk_driver FOREIGN KEY (driver_id) REFERENCES users(id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS admin_messages (
      id BIGINT NOT NULL AUTO_INCREMENT,
      subject VARCHAR(255) NOT NULL,
      content LONGTEXT NOT NULL,
      sender_name VARCHAR(255) NULL,
      type ENUM('error','advertencia','info','soporte') DEFAULT 'info',
      status ENUM('nuevo','abierto','en_progreso','resuelto','cerrado') DEFAULT 'nuevo',
      priority ENUM('baja','media','alta','crítica') DEFAULT 'media',
      \`read\` TINYINT(1) DEFAULT 0,
      error_details JSON NULL,
      created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      KEY idx_status (status),
      KEY idx_priority (priority),
      KEY idx_read (\`read\`),
      KEY idx_created_at (created_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS admin_message_notes (
      id BIGINT NOT NULL AUTO_INCREMENT,
      message_id BIGINT NOT NULL,
      text LONGTEXT NOT NULL,
      author_name VARCHAR(255) NULL,
      created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      KEY idx_message_id (message_id),
      CONSTRAINT admin_message_notes_fk_message FOREIGN KEY (message_id) REFERENCES admin_messages(id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  `);
};

// FUNCIONES DE UTILIDAD 

// Maneja errores de forma centralizada
const handleError = (res, err, mensaje = 'Error en el servidor') => {
  console.error(mensaje, err);
  if (err.code === 'ER_DUP_ENTRY') {
    return res.status(409).json({ error: 'El registro ya existe' });
  }
  res.status(500).json({ error: mensaje });
};

// Ejecuta un SELECT y devuelve los resultados (como un findAll en Java)
const findAll = async (res, query, params = []) => {
  try {
    const [rows] = await pool.query(query, params);
    res.json(rows);
  } catch (err) {
    handleError(res, err, 'Error al obtener datos');
  }
};

// Ejecuta un SELECT devolviendo un único registro
const findOne = async (res, query, params = []) => {
  try {
    const [rows] = await pool.query(query, params);
    if (rows.length === 0) {
      return res.status(404).json({ error: 'Registro no encontrado' });
    }
    res.json(rows[0]);
  } catch (err) {
    handleError(res, err, 'Error al obtener dato');
  }
};

// Ejecuta un INSERT 
const create = async (res, query, params) => {
  try {
    await pool.query(query, params);
    res.status(201).json({ success: true });
  } catch (err) {
    handleError(res, err, 'Error al crear registro');
  }
};

// Ejecuta un UPDATE 
const update = async (res, query, params) => {
  try {
    const [result] = await pool.query(query, params);
    if (result.affectedRows === 0) {
      return res.status(404).json({ error: 'Registro no encontrado' });
    }
    res.json({ success: true });
  } catch (err) {
    handleError(res, err, 'Error al actualizar registro');
  }
};

// Ejecuta un DELETE 
const remove = async (res, query, params) => {
  try {
    const [result] = await pool.query(query, params);
    if (result.affectedRows === 0) {
      return res.status(404).json({ error: 'Registro no encontrado' });
    }
    res.json({ success: true });
  } catch (err) {
    handleError(res, err, 'Error al eliminar registro');
  }
};

// Normaliza valores porcentuales provenientes de docker
const toPercent = (value = '') => {
  const normalized = parseFloat(String(value).replace('%', '').replace(',', '.'));
  return Number.isFinite(normalized) ? Number(normalized.toFixed(1)) : 0;
};

// Convierte "123MiB / 2GiB" a MiB numérico
const toMiB = (value = '') => {
  const [used] = String(value).split('/');
  const upper = used.trim().toUpperCase();
  const numeric = parseFloat(upper.replace(/[^0-9.]/g, ''));
  const factor = upper.includes('GI') ? 1024 : upper.includes('KI') ? 1 / 1024 : 1;
  return Number.isFinite(numeric) ? Number((numeric * factor).toFixed(1)) : 0;
};


// ----------------------------------------------------------ENDPOINTS GENERALES

// Healthcheck del servicio y conectividad de base de datos
app.get('/health', async (req, res) => {
  try {
    await pool.query('SELECT 1');
    res.json({ status: 'ok', db: 'connected', timestamp: new Date().toISOString() });
  } catch (err) {
    console.error('Healthcheck fallido:', err);
    res.status(503).json({ status: 'error', db: 'disconnected', timestamp: new Date().toISOString() });
  }
});


// ============================================
// CRUD DE COLEGIOS
// ============================================

const schoolsListQuery = `
  SELECT
    s.*,
    (SELECT COUNT(*) FROM routes r WHERE r.school_id = s.id) AS routes_count,
    (SELECT COUNT(*) FROM stops st WHERE st.school_id = s.id) AS stops_count,
    (SELECT COUNT(*) FROM students su WHERE su.school_id = s.id) AS students_count
  FROM schools s
  ORDER BY s.nombre ASC
`;

app.get('/api/colegios', (req, res) => {
  findAll(res, schoolsListQuery);
});

app.get('/api/schools', (req, res) => {
  findAll(res, schoolsListQuery);
});

app.post('/api/colegios', async (req, res) => {
  const { nombre, direccion, telefono, email, activo } = req.body;

  if (!nombre || String(nombre).trim() === '') {
    return res.status(400).json({ error: 'El nombre del colegio es requerido' });
  }

  try {
    const [result] = await pool.query(
      `INSERT INTO schools (nombre, direccion, telefono, email, activo, created_at, updated_at)
       VALUES (?, ?, ?, ?, ?, NOW(), NOW())`,
      [
        String(nombre).trim(),
        direccion || null,
        telefono || null,
        email || null,
        activo === 0 || activo === false ? 0 : 1
      ]
    );

    res.status(201).json({ success: true, id: result.insertId });
  } catch (err) {
    handleError(res, err, 'Error al crear colegio');
  }
});

app.post('/api/schools', async (req, res) => {
  const { nombre, direccion, telefono, email, activo } = req.body;

  if (!nombre || String(nombre).trim() === '') {
    return res.status(400).json({ error: 'El nombre del colegio es requerido' });
  }

  try {
    const [result] = await pool.query(
      `INSERT INTO schools (nombre, direccion, telefono, email, activo, created_at, updated_at)
       VALUES (?, ?, ?, ?, ?, NOW(), NOW())`,
      [
        String(nombre).trim(),
        direccion || null,
        telefono || null,
        email || null,
        activo === 0 || activo === false ? 0 : 1
      ]
    );

    res.status(201).json({ success: true, id: result.insertId });
  } catch (err) {
    handleError(res, err, 'Error al crear colegio');
  }
});

app.put('/api/colegios/:id', async (req, res) => {
  const schoolId = Number(req.params.id);
  const { nombre, direccion, telefono, email, activo } = req.body;

  if (!Number.isInteger(schoolId) || schoolId <= 0) {
    return res.status(400).json({ error: 'id de colegio inválido' });
  }

  if (!nombre || String(nombre).trim() === '') {
    return res.status(400).json({ error: 'El nombre del colegio es requerido' });
  }

  try {
    const [result] = await pool.query(
      `UPDATE schools
       SET nombre = ?, direccion = ?, telefono = ?, email = ?, activo = ?, updated_at = NOW()
       WHERE id = ?`,
      [
        String(nombre).trim(),
        direccion || null,
        telefono || null,
        email || null,
        activo === 0 || activo === false ? 0 : 1,
        schoolId
      ]
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({ error: 'Colegio no encontrado' });
    }

    res.json({ success: true });
  } catch (err) {
    handleError(res, err, 'Error al actualizar colegio');
  }
});

app.put('/api/schools/:id', async (req, res) => {
  const schoolId = Number(req.params.id);
  const { nombre, direccion, telefono, email, activo } = req.body;

  if (!Number.isInteger(schoolId) || schoolId <= 0) {
    return res.status(400).json({ error: 'id de colegio inválido' });
  }

  if (!nombre || String(nombre).trim() === '') {
    return res.status(400).json({ error: 'El nombre del colegio es requerido' });
  }

  try {
    const [result] = await pool.query(
      `UPDATE schools
       SET nombre = ?, direccion = ?, telefono = ?, email = ?, activo = ?, updated_at = NOW()
       WHERE id = ?`,
      [
        String(nombre).trim(),
        direccion || null,
        telefono || null,
        email || null,
        activo === 0 || activo === false ? 0 : 1,
        schoolId
      ]
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({ error: 'Colegio no encontrado' });
    }

    res.json({ success: true });
  } catch (err) {
    handleError(res, err, 'Error al actualizar colegio');
  }
});

app.delete('/api/colegios/:id', async (req, res) => {
  const schoolId = Number(req.params.id);

  if (!Number.isInteger(schoolId) || schoolId <= 0) {
    return res.status(400).json({ error: 'id de colegio inválido' });
  }

  try {
    const [[routesCount]] = await pool.query('SELECT COUNT(*) AS total FROM routes WHERE school_id = ?', [schoolId]);
    const [[stopsCount]] = await pool.query('SELECT COUNT(*) AS total FROM stops WHERE school_id = ?', [schoolId]);
    const [[studentsCount]] = await pool.query('SELECT COUNT(*) AS total FROM students WHERE school_id = ?', [schoolId]);

    if ((routesCount?.total || 0) > 0 || (stopsCount?.total || 0) > 0 || (studentsCount?.total || 0) > 0) {
      return res.status(409).json({
        error: 'No se puede eliminar el colegio porque tiene rutas, paradas o alumnos asociados'
      });
    }

    const [result] = await pool.query('DELETE FROM schools WHERE id = ?', [schoolId]);
    if (result.affectedRows === 0) {
      return res.status(404).json({ error: 'Colegio no encontrado' });
    }

    res.json({ success: true });
  } catch (err) {
    handleError(res, err, 'Error al eliminar colegio');
  }
});

app.delete('/api/schools/:id', async (req, res) => {
  const schoolId = Number(req.params.id);

  if (!Number.isInteger(schoolId) || schoolId <= 0) {
    return res.status(400).json({ error: 'id de colegio inválido' });
  }

  try {
    const [[routesCount]] = await pool.query('SELECT COUNT(*) AS total FROM routes WHERE school_id = ?', [schoolId]);
    const [[stopsCount]] = await pool.query('SELECT COUNT(*) AS total FROM stops WHERE school_id = ?', [schoolId]);
    const [[studentsCount]] = await pool.query('SELECT COUNT(*) AS total FROM students WHERE school_id = ?', [schoolId]);

    if ((routesCount?.total || 0) > 0 || (stopsCount?.total || 0) > 0 || (studentsCount?.total || 0) > 0) {
      return res.status(409).json({
        error: 'No se puede eliminar el colegio porque tiene rutas, paradas o alumnos asociados'
      });
    }

    const [result] = await pool.query('DELETE FROM schools WHERE id = ?', [schoolId]);
    if (result.affectedRows === 0) {
      return res.status(404).json({ error: 'Colegio no encontrado' });
    }

    res.json({ success: true });
  } catch (err) {
    handleError(res, err, 'Error al eliminar colegio');
  }
});

// Login de usuario
app.post('/api/login', async (req, res) => {
  const { email, password } = req.body;
  
  if (!email || !password) {
    return res.status(400).json({ error: 'Correo y contraseña requeridos' });
  }
  
  try {
    const [rows] = await pool.query('SELECT * FROM users WHERE email = ?', [email]);
    
    if (rows.length === 0) {
      return res.status(401).json({ error: 'Usuario no encontrado' });
    }
    
    const user = rows[0];
    const passwordMatch = await bcrypt.compare(password, user.password);
    
    if (!passwordMatch) {
      return res.status(401).json({ error: 'Contraseña incorrecta' });
    }
    
    res.json({ 
      success: true, 
      user: { id: user.id, email: user.email, role: user.role } 
    });
  } catch (err) {
    handleError(res, err, 'Error en login');
  }
});


// Estado del servidor y contenedores Docker

app.get('/api/system/status', async (req, res) => {
  const result = {
    timestamp: new Date().toISOString(),
    server: {
      uptimeSeconds: Math.floor(process.uptime()),
      loadAvg: os.loadavg(),
      totalMemMB: Math.round(os.totalmem() / 1024 / 1024),
      freeMemMB: Math.round(os.freemem() / 1024 / 1024)
    },
    docker: { available: false, containers: [] }
  };

  try {
    await execAsync('docker info --format "{{json .}}"');
    result.docker.available = true;
  } catch (err) {
    console.warn('Docker no accesible:', err.message);
    result.docker.error = 'Docker no disponible o sin permisos';
    return res.json(result);
  }

  try {
    const { stdout: psOut } = await execAsync('docker ps --format "{{.ID}},{{.Names}},{{.Status}},{{.State}}"');
    const lines = psOut.trim().split('\n').filter(Boolean);
    result.docker.containers = lines.map(line => {
      const [id, name, status, state] = line.split(',');
      return { id, name, status, state };
    });
  } catch (err) {
    console.warn('No se pudieron leer contenedores:', err.message);
    result.docker.error = 'No se pudo leer contenedores activos';
    return res.json(result);
  }

  try {
    const { stdout: statsOut } = await execAsync('docker stats --no-stream --format "{{.Name}},{{.CPUPerc}},{{.MemUsage}},{{.MemPerc}}"');
    const statsLines = statsOut.trim().split('\n').filter(Boolean);
    const statsMap = new Map(statsLines.map(line => {
      const [name, cpuPerc, memUsage, memPerc] = line.split(',');
      return [name, { cpuPercent: toPercent(cpuPerc), memUsageMiB: toMiB(memUsage), memPercent: toPercent(memPerc) }];
    }));
    result.docker.containers = result.docker.containers.map(container => ({
      ...container,
      ...(statsMap.get(container.name) || {})
    }));
  } catch (err) {
    console.warn('No se pudieron leer métricas de docker stats:', err.message);
    result.docker.note = 'Contenedores listados sin métricas en vivo';
  }

  res.json(result);
});

// ============================================
// CRUD DE USUARIOS (como un UserController en Java)
// ============================================

// Listar todos
app.get('/api/users', (req, res) => {
  findAll(res, 'SELECT id, email, role FROM users');
});

// Crear nuevo
app.post('/api/users', async (req, res) => {
  const { email, password, role } = req.body;
  
  if (!email || !password || !role) {
    return res.status(400).json({ error: 'Email, contraseña y rol requeridos' });
  }
  
  const hashed = await bcrypt.hash(password, 10);
  create(res, 
    'INSERT INTO users (email, password, role, nombre) VALUES (?, ?, ?, ?)', 
    [email, hashed, role, email]
  );
});

// Obtener perfil de un usuario por id (sin contraseña)
app.get('/api/users/:id', async (req, res) => {
  try {
    const [rows] = await pool.query(
      'SELECT id, email, role, nombre, apellidos FROM users WHERE id = ?',
      [req.params.id]
    );
    if (rows.length === 0) return res.status(404).json({ error: 'Usuario no encontrado' });
    res.json(rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Actualizar perfil propio (no se pueden cambiar id ni role)
app.patch('/api/users/:id/profile', async (req, res) => {
  try {
    const id = Number(req.params.id);
    if (!Number.isInteger(id) || id <= 0) {
      return res.status(400).json({ error: 'Id de usuario inválido' });
    }

    const { nombre, apellidos, email, password } = req.body;

    if (!nombre && !apellidos && !email && !password) {
      return res.status(400).json({ error: 'Debes proporcionar al menos un campo a actualizar' });
    }

    // Obtener valores actuales para no sobreescribir con null
    const [current] = await pool.query(
      'SELECT nombre, apellidos, email FROM users WHERE id = ?', [id]
    );
    if (current.length === 0) return res.status(404).json({ error: 'Usuario no encontrado' });

    // Si el nuevo valor es vacío, mantener el valor actual en lugar de sobreescribir con null/vacío
    const newNombre   = (nombre   !== undefined && String(nombre).trim()   !== '') ? String(nombre).trim()   : current[0].nombre;
    const newApellidos = (apellidos !== undefined && String(apellidos).trim() !== '') ? String(apellidos).trim() : current[0].apellidos;
    const newEmail    = (email    !== undefined && String(email).trim()    !== '') ? String(email).trim()    : current[0].email;

    // nombre es NOT NULL en la BD, no permitir que quede vacío
    if (!newNombre) {
      return res.status(400).json({ error: 'El nombre no puede estar vacío' });
    }

    // email es NOT NULL en la BD, no permitir que quede vacío
    if (!newEmail) {
      return res.status(400).json({ error: 'El correo no puede estar vacío' });
    }

    // Validación mínima de email
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(newEmail)) {
      return res.status(400).json({ error: 'Formato de email no válido' });
    }

    let query = 'UPDATE users SET nombre = ?, apellidos = ?, email = ?';
    const params = [newNombre, newApellidos || null, newEmail];

    if (password) {
      if (String(password).length < 6) {
        return res.status(400).json({ error: 'La contraseña debe tener al menos 6 caracteres' });
      }
      const hashed = await bcrypt.hash(password, 10);
      query += ', password = ?';
      params.push(hashed);
    }

    query += ' WHERE id = ?';
    params.push(id);

    const [result] = await pool.query(query, params);
    if (result.affectedRows === 0) return res.status(404).json({ error: 'Usuario no encontrado' });

    // Devolver perfil actualizado
    const [updated] = await pool.query(
      'SELECT id, email, role, nombre, apellidos FROM users WHERE id = ?', [id]
    );
    res.json({ success: true, user: updated[0] });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Actualizar
app.put('/api/users/:id', async (req, res) => {
  const { id } = req.params;
  const { email, password, role } = req.body;
  
  if (!email || !role) {
    return res.status(400).json({ error: 'Email y rol requeridos' });
  }
  
  let query = 'UPDATE users SET email = ?, role = ?';
  const params = [email, role];
  
  if (password) {
    const hashed = await bcrypt.hash(password, 10);
    query += ', password = ?';
    params.push(hashed);
  }
  
  query += ' WHERE id = ?';
  params.push(id);
  
  update(res, query, params);
});

// Eliminar
app.delete('/api/users/:id', (req, res) => {
  remove(res, 'DELETE FROM users WHERE id = ?', [req.params.id]);
});

// ============================================
// CRUD DE BUSES (como un BusController en Java)
// ============================================

// Listar todos
app.get('/api/buses', (req, res) => {
  findAll(res, `
    SELECT b.*, 
           CONCAT(u.nombre, ' ', u.apellidos) AS conductor_nombre, 
           u.email AS conductor_email 
    FROM buses b 
    LEFT JOIN users u ON b.driver_id = u.id
  `);
});

// Crear nuevo
app.post('/api/buses', (req, res) => {
  const { matricula, marca, modelo, anio, capacidad, color, estado } = req.body;
  
  if (!matricula || !marca || !modelo || !anio || !capacidad || !color || !estado) {
    return res.status(400).json({ error: 'Todos los campos son requeridos' });
  }
  
  create(res, 
    'INSERT INTO buses (matricula, marca, modelo, anio, capacidad, color, estado) VALUES (?, ?, ?, ?, ?, ?, ?)',
    [matricula, marca, modelo, anio, capacidad, color, estado]
  );
});

// Actualizar
app.put('/api/buses/:id', (req, res) => {
  const { matricula, marca, modelo, anio, capacidad, color, estado } = req.body;
  
  if (!matricula || !marca || !modelo || !anio || !capacidad || !color || !estado) {
    return res.status(400).json({ error: 'Todos los campos son requeridos' });
  }
  
  update(res,
    'UPDATE buses SET matricula=?, marca=?, modelo=?, anio=?, capacidad=?, color=?, estado=? WHERE id=?',
    [matricula, marca, modelo, anio, capacidad, color, estado, req.params.id]
  );
});

// Eliminar
app.delete('/api/buses/:id', (req, res) => {
  remove(res, 'DELETE FROM buses WHERE id = ?', [req.params.id]);
});

// Actualizar ubicación GPS del bus
app.patch('/api/buses/:id/location', (req, res) => {
  const { lat, lon } = req.body;
  
  if (lat === undefined || lon === undefined) {
    return res.status(400).json({ error: 'Latitud y longitud requeridas' });
  }
  
  update(res, 
    'UPDATE buses SET lat = ?, lon = ? WHERE id = ?',
    [lat, lon, req.params.id]
  );
});

// ============================================
// CRUD DE ESTUDIANTES (como un StudentController en Java)
// ============================================

// Listar todos
app.get('/api/students', (req, res) => {
  findAll(res, `
    SELECT s.*, u.email AS parent_email, sc.nombre AS school_nombre
    FROM students s
    LEFT JOIN users u ON s.parent_id = u.id
    LEFT JOIN schools sc ON s.school_id = sc.id
  `);
});

// Crear nuevo
app.post('/api/students', (req, res) => {
  const { nombre, apellidos, fecha_nacimiento, curso, parent_id, stop_id, school_id } = req.body;
  
  if (!nombre || !apellidos) {
    return res.status(400).json({ error: 'Nombre y apellidos requeridos' });
  }
  
  create(res,
    'INSERT INTO students (nombre, apellidos, fecha_nacimiento, curso, parent_id, stop_id, school_id, activo) VALUES (?, ?, ?, ?, ?, ?, ?, 1)',
    [nombre, apellidos, fecha_nacimiento || null, curso || null, parent_id || null, stop_id || null, school_id || null]
  );
});

// Actualizar
app.put('/api/students/:id', (req, res) => {
  const { nombre, apellidos, fecha_nacimiento, curso, parent_id, stop_id, school_id } = req.body;
  
  if (!nombre || !apellidos) {
    return res.status(400).json({ error: 'Nombre y apellidos requeridos' });
  }
  
  update(res,
    'UPDATE students SET nombre=?, apellidos=?, fecha_nacimiento=?, curso=?, parent_id=?, stop_id=?, school_id=? WHERE id=?',
    [nombre, apellidos, fecha_nacimiento || null, curso || null, parent_id || null, stop_id || null, school_id || null, req.params.id]
  );
});

// Eliminar
app.delete('/api/students/:id', (req, res) => {
  remove(res, 'DELETE FROM students WHERE id = ?', [req.params.id]);
});

// Obtener detalle de un estudiante por ID
app.get('/api/students/:id', (req, res) => {
  findOne(res, `
    SELECT s.*, st.nombre AS stop_nombre, st.direccion AS stop_direccion,
           st.latitud, st.longitud, sc.nombre AS school_nombre,
           u.email AS parent_email
    FROM students s
    LEFT JOIN stops st ON s.stop_id = st.id
    LEFT JOIN schools sc ON s.school_id = sc.id
    LEFT JOIN users u ON s.parent_id = u.id
    WHERE s.id = ?
  `, [req.params.id]);
});

// Obtener incidencias para la ruta de un alumno (del padre)
app.get('/api/parent/:parentId/children/:childId/incidents', async (req, res) => {
  try {
    const { parentId, childId } = req.params;

    // Verificar que el alumno pertenece a este padre
    const [students] = await pool.query(
      'SELECT stop_id FROM students WHERE id = ? AND parent_id = ? AND activo = 1',
      [childId, parentId]
    );

    if (students.length === 0) {
      return res.status(404).json({ error: 'Alumno no encontrado' });
    }

    const stopId = students[0].stop_id;
    if (!stopId) {
      return res.json([]);
    }

    // Obtener incidencias de los trayectos que sirven la parada del alumno
    const [rows] = await pool.query(
      `SELECT i.id, i.tipo, i.descripcion, i.latitud, i.longitud,
              i.resuelto, i.created_at, i.updated_at, i.fecha_resolucion,
              r.nombre AS route_nombre, b.matricula,
              ra.fecha AS fecha_trayecto
       FROM incidents i
       INNER JOIN route_assignments ra ON i.route_assignment_id = ra.id
       INNER JOIN routes r ON ra.route_id = r.id
       INNER JOIN buses b ON ra.bus_id = b.id
       WHERE ra.route_id IN (
         SELECT DISTINCT route_id FROM stops WHERE id = ?
       )
       ORDER BY i.created_at DESC
       LIMIT 50`,
      [stopId]
    );

    res.json(rows);
  } catch (err) {
    handleError(res, err, 'Error al obtener incidencias del alumno');
  }
});

// ============================================
// CRUD DE RUTAS (como un RouteController en Java)
// ============================================

// Listar todas
app.get('/api/routes', (req, res) => {
  findAll(res, `
    SELECT r.*, sc.nombre AS school_nombre
    FROM routes r
    LEFT JOIN schools sc ON r.school_id = sc.id
  `);
});

// Crear nueva
app.post('/api/routes', (req, res) => {
  const { nombre, descripcion, horario_inicio, horario_fin, tipo, dias_semana, school_id } = req.body;
  
  if (!nombre || !horario_inicio) {
    return res.status(400).json({ error: 'Nombre y horario_inicio requeridos' });
  }
  
  create(res,
    'INSERT INTO routes (nombre, descripcion, horario_inicio, horario_fin, tipo, dias_semana, school_id, activa) VALUES (?, ?, ?, ?, ?, ?, ?, 1)',
    [nombre, descripcion || '', horario_inicio, horario_fin || horario_inicio, tipo || 'IDA', dias_semana || 'LMXJV', school_id || null]
  );
});

// Actualizar
app.put('/api/routes/:id', (req, res) => {
  const { nombre, descripcion, horario_inicio, horario_fin, tipo, dias_semana, school_id } = req.body;
  
  if (!nombre || !horario_inicio) {
    return res.status(400).json({ error: 'Nombre y horario_inicio requeridos' });
  }
  
  update(res,
    'UPDATE routes SET nombre=?, descripcion=?, horario_inicio=?, horario_fin=?, tipo=?, dias_semana=?, school_id=? WHERE id=?',
    [nombre, descripcion || '', horario_inicio, horario_fin || horario_inicio, tipo || 'IDA', dias_semana || 'LMXJV', school_id || null, req.params.id]
  );
});

// Eliminar
app.delete('/api/routes/:id', (req, res) => {
  remove(res, 'DELETE FROM routes WHERE id = ?', [req.params.id]);
});

// ============================================
// ASIGNACIONES OPERATIVAS (conductor-bus-ruta)
// ============================================

// Lista de conductores disponibles para asignaciones
app.get('/api/drivers', (req, res) => {
  findAll(res, `
    SELECT id, nombre, apellidos, email, activo
    FROM users
    WHERE role = 'DRIVER'
    ORDER BY nombre ASC, apellidos ASC
  `);
});

// Asignar o desasignar conductor a un bus
app.patch('/api/buses/:id/driver', async (req, res) => {
  try {
    const busId = Number(req.params.id);
    const { driverId } = req.body;

    if (!Number.isInteger(busId) || busId <= 0) {
      return res.status(400).json({ error: 'busId inválido' });
    }

    // Permite desasignar conductor enviando null
    if (driverId === null || driverId === undefined || driverId === '') {
      const [result] = await pool.query('UPDATE buses SET driver_id = NULL WHERE id = ?', [busId]);
      if (result.affectedRows === 0) {
        return res.status(404).json({ error: 'Bus no encontrado' });
      }
      return res.json({ success: true, message: 'Conductor desasignado del bus' });
    }

    const numericDriverId = Number(driverId);
    if (!Number.isInteger(numericDriverId) || numericDriverId <= 0) {
      return res.status(400).json({ error: 'driverId inválido' });
    }

    const [driverRows] = await pool.query(
      `SELECT id, role, activo FROM users WHERE id = ? LIMIT 1`,
      [numericDriverId]
    );

    if (driverRows.length === 0) {
      return res.status(404).json({ error: 'Conductor no encontrado' });
    }

    const driver = driverRows[0];
    if (driver.role !== 'DRIVER') {
      return res.status(400).json({ error: 'El usuario indicado no es conductor' });
    }

    if (driver.activo !== 1 && driver.activo !== true) {
      return res.status(400).json({ error: 'El conductor no está activo' });
    }

    const [assignedRows] = await pool.query(
      `SELECT id, matricula FROM buses WHERE driver_id = ? AND id <> ? LIMIT 1`,
      [numericDriverId, busId]
    );

    if (assignedRows.length > 0) {
      return res.status(409).json({
        error: `El conductor ya está asignado al bus ${assignedRows[0].matricula}`
      });
    }

    const [result] = await pool.query('UPDATE buses SET driver_id = ? WHERE id = ?', [numericDriverId, busId]);
    if (result.affectedRows === 0) {
      return res.status(404).json({ error: 'Bus no encontrado' });
    }

    res.json({ success: true, message: 'Conductor asignado al bus correctamente' });
  } catch (err) {
    handleError(res, err, 'Error al asignar conductor al bus');
  }
});

// Listar asignaciones de ruta con bus y conductor
app.get('/api/route-assignments', async (req, res) => {
  try {
    const { fecha, estado } = req.query;
    const conditions = [];
    const params = [];

    if (fecha) {
      conditions.push('ra.fecha = ?');
      params.push(fecha);
    }

    if (estado) {
      conditions.push('ra.estado = ?');
      params.push(estado);
    }

    const whereClause = conditions.length ? `WHERE ${conditions.join(' AND ')}` : '';

    const [rows] = await pool.query(
      `SELECT
         ra.id,
         ra.route_id,
         ra.bus_id,
         ra.fecha,
         ra.estado,
         ra.hora_inicio_real,
         ra.hora_fin_real,
         ra.retraso_minutos,
         ra.observaciones,
         r.nombre AS route_nombre,
         b.matricula,
         b.driver_id,
         CONCAT(u.nombre, ' ', COALESCE(u.apellidos, '')) AS conductor_nombre,
         u.email AS conductor_email
       FROM route_assignments ra
       INNER JOIN routes r ON ra.route_id = r.id
       INNER JOIN buses b ON ra.bus_id = b.id
       LEFT JOIN users u ON b.driver_id = u.id
       ${whereClause}
       ORDER BY ra.fecha DESC, ra.id DESC`,
      params
    );

    res.json(rows);
  } catch (err) {
    handleError(res, err, 'Error al obtener asignaciones de ruta');
  }
});

// Crear asignación de ruta para un bus en una fecha
app.post('/api/route-assignments', async (req, res) => {
  try {
    const { routeId, busId, fecha, estado } = req.body;

    if (!routeId || !busId || !fecha) {
      return res.status(400).json({ error: 'routeId, busId y fecha son requeridos' });
    }

    const validStates = ['PROGRAMADA', 'EN_CURSO', 'FINALIZADA', 'CANCELADA'];
    const assignmentState = validStates.includes(estado) ? estado : 'PROGRAMADA';

    const [routeRows] = await pool.query('SELECT id FROM routes WHERE id = ? LIMIT 1', [routeId]);
    if (routeRows.length === 0) {
      return res.status(404).json({ error: 'Ruta no encontrada' });
    }

    const [busRows] = await pool.query('SELECT id FROM buses WHERE id = ? LIMIT 1', [busId]);
    if (busRows.length === 0) {
      return res.status(404).json({ error: 'Bus no encontrado' });
    }

    const [result] = await pool.query(
      `INSERT INTO route_assignments (route_id, bus_id, fecha, estado, created_at, updated_at)
       VALUES (?, ?, ?, ?, NOW(), NOW())`,
      [routeId, busId, fecha, assignmentState]
    );

    res.status(201).json({ success: true, assignmentId: result.insertId });
  } catch (err) {
    if (err.code === 'ER_DUP_ENTRY') {
      return res.status(409).json({ error: 'Ya existe una asignación para esa ruta, bus y fecha' });
    }
    handleError(res, err, 'Error al crear asignación de ruta');
  }
});

// Asignar conductor a una asignación de ruta (vía bus de la asignación)
app.patch('/api/route-assignments/:id/driver', async (req, res) => {
  try {
    const assignmentId = Number(req.params.id);
    const { driverId } = req.body;

    if (!Number.isInteger(assignmentId) || assignmentId <= 0) {
      return res.status(400).json({ error: 'assignmentId inválido' });
    }

    if (!driverId) {
      return res.status(400).json({ error: 'driverId es requerido' });
    }

    const numericDriverId = Number(driverId);
    if (!Number.isInteger(numericDriverId) || numericDriverId <= 0) {
      return res.status(400).json({ error: 'driverId inválido' });
    }

    const [assignmentRows] = await pool.query(
      `SELECT id, bus_id, fecha FROM route_assignments WHERE id = ? LIMIT 1`,
      [assignmentId]
    );
    if (assignmentRows.length === 0) {
      return res.status(404).json({ error: 'Asignación de ruta no encontrada' });
    }

    const assignment = assignmentRows[0];

    const [driverRows] = await pool.query(
      `SELECT id, role, activo FROM users WHERE id = ? LIMIT 1`,
      [numericDriverId]
    );
    if (driverRows.length === 0) {
      return res.status(404).json({ error: 'Conductor no encontrado' });
    }

    const driver = driverRows[0];
    if (driver.role !== 'DRIVER') {
      return res.status(400).json({ error: 'El usuario indicado no es conductor' });
    }

    if (driver.activo !== 1 && driver.activo !== true) {
      return res.status(400).json({ error: 'El conductor no está activo' });
    }

    // Evita solape del mismo conductor en otra asignación activa del mismo día
    const [conflictRows] = await pool.query(
      `SELECT ra.id
       FROM route_assignments ra
       INNER JOIN buses b ON ra.bus_id = b.id
       WHERE b.driver_id = ?
         AND ra.fecha = ?
         AND ra.id <> ?
         AND ra.estado IN ('PROGRAMADA', 'EN_CURSO')
       LIMIT 1`,
      [numericDriverId, assignment.fecha, assignmentId]
    );

    if (conflictRows.length > 0) {
      return res.status(409).json({
        error: 'El conductor ya tiene otra asignación activa para esa fecha'
      });
    }

    await pool.query('UPDATE buses SET driver_id = ? WHERE id = ?', [numericDriverId, assignment.bus_id]);

    res.json({ success: true, message: 'Conductor asignado a la ruta correctamente' });
  } catch (err) {
    handleError(res, err, 'Error al asignar conductor a la ruta');
  }
});

// Actualizar estado de una asignación de ruta
app.patch('/api/route-assignments/:id/status', async (req, res) => {
  try {
    const assignmentId = Number(req.params.id);
    const { estado } = req.body;
    const validStates = ['PROGRAMADA', 'EN_CURSO', 'FINALIZADA', 'CANCELADA'];

    if (!Number.isInteger(assignmentId) || assignmentId <= 0) {
      return res.status(400).json({ error: 'assignmentId inválido' });
    }

    if (!validStates.includes(estado)) {
      return res.status(400).json({ error: 'Estado inválido' });
    }

    const [result] = await pool.query(
      `UPDATE route_assignments SET estado = ?, updated_at = NOW() WHERE id = ?`,
      [estado, assignmentId]
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({ error: 'Asignación de ruta no encontrada' });
    }

    res.json({ success: true });
  } catch (err) {
    handleError(res, err, 'Error al actualizar estado de asignación');
  }
});

// Eliminar asignación de ruta
app.delete('/api/route-assignments/:id', (req, res) => {
  remove(res, 'DELETE FROM route_assignments WHERE id = ?', [req.params.id]);
});

// ============================================
// CRUD DE PARADAS (como un StopController en Java)
// ============================================

// Listar todas
app.get('/api/stops', (req, res) => {
  findAll(res, `
    SELECT s.*, r.nombre AS route_nombre, sc.nombre AS school_nombre
    FROM stops s
    LEFT JOIN routes r ON s.route_id = r.id
    LEFT JOIN schools sc ON s.school_id = sc.id
  `);
});

// Crear nueva
app.post('/api/stops', (req, res) => {
  const { route_id, nombre, direccion, latitud, longitud, orden, hora_estimada, radio_proximidad, school_id } = req.body;
  
  if (!nombre || !route_id) {
    return res.status(400).json({ error: 'Nombre y route_id requeridos' });
  }
  
  create(res,
    'INSERT INTO stops (route_id, nombre, direccion, latitud, longitud, orden, hora_estimada, radio_proximidad, school_id, activa) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1)',
    [route_id, nombre, direccion || '', latitud || null, longitud || null, orden || 1, hora_estimada || null, radio_proximidad || 500, school_id || null]
  );
});

// Actualizar
app.put('/api/stops/:id', (req, res) => {
  const { route_id, nombre, direccion, latitud, longitud, orden, hora_estimada, radio_proximidad, school_id } = req.body;
  
  if (!nombre || !route_id) {
    return res.status(400).json({ error: 'Nombre y route_id requeridos' });
  }
  
  update(res,
    'UPDATE stops SET route_id=?, nombre=?, direccion=?, latitud=?, longitud=?, orden=?, hora_estimada=?, radio_proximidad=?, school_id=? WHERE id=?',
    [route_id, nombre, direccion || '', latitud || null, longitud || null, orden || 1, hora_estimada || null, radio_proximidad || 500, school_id || null, req.params.id]
  );
});

// Eliminar
app.delete('/api/stops/:id', (req, res) => {
  remove(res, 'DELETE FROM stops WHERE id = ?', [req.params.id]);
});

// ============================================
// ENDPOINTS ESPECIALES PARA PADRES
// ============================================

// Obtener buses disponibles para un padre
app.get('/api/parent/:parentId/buses', async (req, res) => {
  try {
    const { parentId } = req.params;
    
    // Obtener paradas de los hijos
    const [students] = await pool.query(
      'SELECT DISTINCT stop_id FROM students WHERE parent_id = ? AND stop_id IS NOT NULL',
      [parentId]
    );
    
    if (students.length === 0) {
      return res.json([]);
    }
    
    // Obtener rutas de esas paradas
    const stopIds = students.map(s => s.stop_id);
    const placeholders = stopIds.map(() => '?').join(',');
    
    const [routes] = await pool.query(
      `SELECT DISTINCT r.id FROM routes r 
       INNER JOIN stops s ON r.id = s.route_id 
       WHERE s.id IN (${placeholders})`,
      stopIds
    );
    
    if (routes.length === 0) {
      return res.json([]);
    }
    
    // Obtener buses activos
    const [buses] = await pool.query(
      `SELECT b.*, CONCAT(u.nombre, ' ', u.apellidos) AS conductor_nombre, u.email AS conductor_email 
       FROM buses b 
       LEFT JOIN users u ON b.driver_id = u.id 
       WHERE b.estado = 'ACTIVO'`
    );
    
    res.json(buses);
  } catch (err) {
    handleError(res, err, 'Error al obtener buses del padre');
  }
});

// Obtener hijos de un padre
app.get('/api/parent/:parentId/children', (req, res) => {
  findAll(res, `
    SELECT s.*, st.nombre AS stop_nombre, st.direccion AS stop_direccion, st.latitud, st.longitud
    FROM students s
    LEFT JOIN stops st ON s.stop_id = st.id
    WHERE s.parent_id = ? AND s.activo = 1
  `, [req.params.parentId]);
});

// ============================================
// ENDPOINTS ESPECIALES PARA CONDUCTOR
// ============================================

// Obtener ruta del dia para un conductor
app.get('/api/driver/:driverId/today-route', async (req, res) => {
  try {
    const { driverId } = req.params;

    const [assignments] = await pool.query(
      `SELECT
         ra.id AS assignment_id,
         ra.fecha,
         ra.estado,
         ra.hora_inicio_real,
         ra.hora_fin_real,
         ra.retraso_minutos,
         r.id AS route_id,
         r.nombre AS route_nombre,
         r.horario_inicio,
         r.horario_fin,
         b.id AS bus_id,
         b.matricula,
         b.marca,
         b.modelo,
         b.estado AS bus_estado
       FROM route_assignments ra
       INNER JOIN buses b ON ra.bus_id = b.id
       INNER JOIN routes r ON ra.route_id = r.id
       WHERE b.driver_id = ? AND ra.fecha = CURDATE()
       ORDER BY
         CASE ra.estado
           WHEN 'EN_CURSO' THEN 1
           WHEN 'PROGRAMADA' THEN 2
           WHEN 'FINALIZADA' THEN 3
           ELSE 4
         END,
         ra.id DESC
       LIMIT 1`,
      [driverId]
    );

    if (assignments.length === 0) {
      return res.json({ route: null, stops: [] });
    }

    const route = assignments[0];
    const [stops] = await pool.query(
      `SELECT id, nombre, direccion, latitud, longitud, orden, hora_estimada
       FROM stops
       WHERE route_id = ? AND activa = 1
       ORDER BY orden ASC`,
      [route.route_id]
    );

    const [checkins] = await pool.query(
      `SELECT id, stop_id, action, notes, created_at
       FROM driver_stop_checkins
       WHERE route_assignment_id = ? AND driver_id = ?
       ORDER BY created_at DESC`,
      [route.assignment_id, driverId]
    );

    res.json({ route, stops, checkins });
  } catch (err) {
    handleError(res, err, 'Error al obtener ruta del conductor');
  }
});

// Registrar check-in de parada para conductor
app.post('/api/driver/checkins', async (req, res) => {
  try {
    const { routeAssignmentId, stopId, driverId, action, notes } = req.body;
    const validActions = ['ARRIVAL', 'DEPARTURE'];

    if (!routeAssignmentId || !stopId || !driverId || !action) {
      return res.status(400).json({ error: 'routeAssignmentId, stopId, driverId y action son requeridos' });
    }

    if (!validActions.includes(action)) {
      return res.status(400).json({ error: 'Acción de check-in inválida' });
    }

    const [result] = await pool.query(
      `INSERT INTO driver_stop_checkins (route_assignment_id, stop_id, driver_id, action, notes, created_at)
       VALUES (?, ?, ?, ?, ?, NOW())`,
      [routeAssignmentId, stopId, driverId, action, notes || null]
    );

    if (action === 'ARRIVAL') {
      await pool.query(
        `UPDATE route_assignments
         SET estado = CASE
             WHEN estado = 'PROGRAMADA' THEN 'EN_CURSO'
             ELSE estado
           END,
           hora_inicio_real = CASE
             WHEN hora_inicio_real IS NULL THEN NOW()
             ELSE hora_inicio_real
           END,
           updated_at = NOW()
         WHERE id = ?`,
        [routeAssignmentId]
      );
    }

    const [stopInfo] = await pool.query(
      `SELECT nombre FROM stops WHERE id = ? LIMIT 1`,
      [stopId]
    );

    const stopName = stopInfo[0]?.nombre || `Parada ${stopId}`;
    const actionLabel = action === 'ARRIVAL' ? 'llegada' : 'salida';

    await pool.query(
      `INSERT INTO admin_messages (subject, content, sender_name, type, status, priority, \`read\`, created_at, updated_at)
       VALUES (?, ?, ?, 'info', 'nuevo', 'media', 0, NOW(), NOW())`,
      [
        `[Driver] Check-in ${actionLabel} · ${stopName}`,
        `El conductor ${driverId} registró ${actionLabel} en la parada ${stopName} para la asignación ${routeAssignmentId}.`,
        `Conductor ${driverId}`
      ]
    );

    res.status(201).json({ success: true, checkinId: result.insertId, message: 'Check-in registrado correctamente' });
  } catch (err) {
    handleError(res, err, 'Error al registrar check-in');
  }
});

// Reportar incidencia de conductor
app.post('/api/driver/incidents', async (req, res) => {
  try {
    const { routeAssignmentId, driverId, tipo, descripcion, latitud, longitud } = req.body;
    const validTypes = ['RETRASO', 'MECANICO', 'ACCIDENTE', 'CLIMA', 'OTRO'];

    if (!routeAssignmentId || !driverId || !tipo || !descripcion) {
      return res.status(400).json({ error: 'routeAssignmentId, driverId, tipo y descripcion son requeridos' });
    }

    if (!validTypes.includes(tipo)) {
      return res.status(400).json({ error: 'Tipo de incidencia inválido' });
    }

    const conn = await pool.getConnection();
    await conn.beginTransaction();

    let result;
    try {
      const [incidentInsert] = await conn.query(
        `INSERT INTO incidents (route_assignment_id, driver_id, tipo, descripcion, latitud, longitud, resuelto, created_at, updated_at)
         VALUES (?, ?, ?, ?, ?, ?, 0, NOW(), NOW())`,
        [routeAssignmentId, driverId, tipo, descripcion, latitud || null, longitud || null]
      );
      result = incidentInsert;

      // Publicar también como ticket operativo para el panel de administración
      const [routeInfo] = await conn.query(
        `SELECT r.nombre AS route_nombre, b.matricula
         FROM route_assignments ra
         INNER JOIN routes r ON ra.route_id = r.id
         INNER JOIN buses b ON ra.bus_id = b.id
         WHERE ra.id = ?
         LIMIT 1`,
        [routeAssignmentId]
      );

      const routeNombre = routeInfo[0]?.route_nombre || 'Ruta sin nombre';
      const matricula = routeInfo[0]?.matricula || 'Sin matrícula';
      const incidentSubject = `[Driver] ${tipo} - ${routeNombre}`;
      const incidentContent = `Incidencia reportada por conductor #${driverId} en bus ${matricula}.\n\nDescripción:\n${descripcion}`;

      await conn.query(
        `INSERT INTO admin_messages (subject, content, sender_name, type, status, priority, \`read\`, created_at, updated_at)
         VALUES (?, ?, ?, 'advertencia', 'nuevo', 'alta', 0, NOW(), NOW())`,
        [incidentSubject, incidentContent, `Conductor ${driverId}`]
      );

      await conn.commit();
    } catch (txErr) {
      await conn.rollback();
      throw txErr;
    } finally {
      conn.release();
    }

    res.status(201).json({
      success: true,
      incidentId: result.insertId,
      message: 'Incidencia reportada correctamente'
    });
  } catch (err) {
    handleError(res, err, 'Error al reportar incidencia');
  }
});

// Historial de incidencias del conductor
app.get('/api/driver/:driverId/incidents', async (req, res) => {
  try {
    const { driverId } = req.params;
    const [rows] = await pool.query(
      `SELECT
         i.id,
         i.tipo,
         i.descripcion,
         i.latitud,
         i.longitud,
         i.resuelto,
         i.created_at,
         i.updated_at,
         i.fecha_resolucion,
         ra.estado AS assignment_estado,
         r.nombre AS route_nombre,
         b.matricula
       FROM incidents i
       LEFT JOIN route_assignments ra ON i.route_assignment_id = ra.id
       LEFT JOIN routes r ON ra.route_id = r.id
       LEFT JOIN buses b ON ra.bus_id = b.id
       WHERE i.driver_id = ?
       ORDER BY i.created_at DESC`,
      [driverId]
    );

    res.json(rows);
  } catch (err) {
    handleError(res, err, 'Error al obtener incidencias del conductor');
  }
});

// Finalizar ruta del conductor
app.post('/api/driver/finish-route', async (req, res) => {
  try {
    const { routeAssignmentId, driverId, summary } = req.body;

    if (!routeAssignmentId || !driverId) {
      return res.status(400).json({ error: 'routeAssignmentId y driverId son requeridos' });
    }

    const [assignmentRows] = await pool.query(
      `SELECT ra.id, ra.estado, r.nombre AS route_nombre, b.matricula
       FROM route_assignments ra
       INNER JOIN routes r ON ra.route_id = r.id
       INNER JOIN buses b ON ra.bus_id = b.id
       WHERE ra.id = ?
       LIMIT 1`,
      [routeAssignmentId]
    );

    if (assignmentRows.length === 0) {
      return res.status(404).json({ error: 'Asignación de ruta no encontrada' });
    }

    const assignment = assignmentRows[0];
    if (assignment.estado === 'FINALIZADA') {
      return res.status(400).json({ error: 'La ruta ya está finalizada' });
    }

    await pool.query(
      `UPDATE route_assignments
       SET estado = 'FINALIZADA',
           hora_fin_real = NOW(),
           observaciones = CASE
             WHEN ? IS NULL OR ? = '' THEN observaciones
             ELSE ?
           END,
           updated_at = NOW()
       WHERE id = ?`,
      [summary || null, summary || null, summary || null, routeAssignmentId]
    );

    const routeName = assignment.route_nombre || 'Ruta sin nombre';
    const busPlate = assignment.matricula || 'Sin matrícula';
    const content = [
      `El conductor ${driverId} finalizó la ruta ${routeName} (bus ${busPlate}).`,
      summary ? `Resumen: ${summary}` : 'Sin resumen adicional.'
    ].join('\n');

    await pool.query(
      `INSERT INTO admin_messages (subject, content, sender_name, type, status, priority, \`read\`, created_at, updated_at)
       VALUES (?, ?, ?, 'info', 'nuevo', 'media', 0, NOW(), NOW())`,
      [`[Driver] Ruta finalizada · ${routeName}`, content, `Conductor ${driverId}`]
    );

    res.json({ success: true, message: 'Ruta finalizada correctamente' });
  } catch (err) {
    handleError(res, err, 'Error al finalizar ruta');
  }
});

// ============================================
// ENDPOINTS DE MENSAJERÍA DEL ADMINISTRADOR
// ============================================

// Obtener todos los mensajes del administrador
app.get('/api/admin/messages', async (req, res) => {
  try {
    const [messages] = await pool.query(
      `SELECT * FROM admin_messages ORDER BY created_at DESC`
    );

    // Obtener notas para cada mensaje
    const messagesWithNotes = await Promise.all(
      messages.map(async (message) => {
        const [notes] = await pool.query(
          `SELECT * FROM admin_message_notes WHERE message_id = ? ORDER BY created_at ASC`,
          [message.id]
        );
        return {
          ...message,
          notes: notes || []
        };
      })
    );

    res.json(messagesWithNotes);
  } catch (err) {
    handleError(res, err, 'Error al obtener mensajes');
  }
});

// Obtener un mensaje específico
app.get('/api/admin/messages/:id', async (req, res) => {
  try {
    const [messages] = await pool.query(
      `SELECT * FROM admin_messages WHERE id = ?`,
      [req.params.id]
    );
    if (messages.length === 0) {
      return res.status(404).json({ error: 'Mensaje no encontrado' });
    }
    res.json(messages[0]);
  } catch (err) {
    handleError(res, err, 'Error al obtener el mensaje');
  }
});

// Marcar un mensaje como leído
app.patch('/api/admin/messages/:id/read', async (req, res) => {
  try {
    await pool.query(
      `UPDATE admin_messages SET \`read\` = 1, updated_at = NOW() WHERE id = ?`,
      [req.params.id]
    );
    res.json({ success: true });
  } catch (err) {
    handleError(res, err, 'Error al marcar como leído');
  }
});

// Cambiar estado de un mensaje
app.patch('/api/admin/messages/:id/status', async (req, res) => {
  try {
    const { status } = req.body;
    const validStatuses = ['nuevo', 'abierto', 'en_progreso', 'resuelto', 'cerrado'];
    if (!validStatuses.includes(status)) {
      return res.status(400).json({ error: 'Estado inválido' });
    }
    await pool.query(
      `UPDATE admin_messages SET status = ?, updated_at = NOW() WHERE id = ?`,
      [status, req.params.id]
    );
    res.json({ success: true });
  } catch (err) {
    handleError(res, err, 'Error al cambiar estado');
  }
});

// Añadir una nota a un mensaje
app.post('/api/admin/messages/:id/notes', async (req, res) => {
  try {
    const { text, authorName } = req.body;
    if (!text || text.trim() === '') {
      return res.status(400).json({ error: 'La nota no puede estar vacía' });
    }

    const [result] = await pool.query(
      `INSERT INTO admin_message_notes (message_id, text, author_name, created_at)
       VALUES (?, ?, ?, NOW())`,
      [req.params.id, text, authorName || 'Administrador']
    );

    res.json({
      success: true,
      note: {
        text: text,
        authorName: authorName || 'Administrador',
        createdAt: new Date().toISOString()
      }
    });
  } catch (err) {
    handleError(res, err, 'Error al guardar la nota');
  }
});

// Eliminar un mensaje
app.delete('/api/admin/messages/:id', async (req, res) => {
  try {
    const conn = await pool.getConnection();
    await conn.beginTransaction();
    
    try {
      // Eliminar notas asociadas
      await conn.query(
        `DELETE FROM admin_message_notes WHERE message_id = ?`,
        [req.params.id]
      );
      // Eliminar mensaje
      await conn.query(
        `DELETE FROM admin_messages WHERE id = ?`,
        [req.params.id]
      );
      await conn.commit();
      res.json({ success: true });
    } catch (err) {
      await conn.rollback();
      throw err;
    } finally {
      conn.release();
    }
  } catch (err) {
    handleError(res, err, 'Error al eliminar mensaje');
  }
});

// Endpoint para que el profesor envíe un mensaje
app.post('/api/messages/send', async (req, res) => {
  try {
    const { subject, content, senderName, type, priority } = req.body;

    // Validar campos requeridos
    if (!subject || !content) {
      return res.status(400).json({ error: 'El asunto y contenido son requeridos' });
    }

    const senderNameFinal = senderName || 'Profesor';
    const typeFinal = type || 'soporte';
    const priorityFinal = priority || 'media';

    // Guardar en admin_messages
    const [result] = await pool.query(
      `INSERT INTO admin_messages (subject, content, sender_name, type, status, priority, \`read\`, created_at, updated_at)
       VALUES (?, ?, ?, ?, 'nuevo', ?, 0, NOW(), NOW())`,
      [subject, content, senderNameFinal, typeFinal, priorityFinal]
    );

    res.json({ 
      success: true, 
      messageId: result.insertId,
      message: 'Mensaje enviado correctamente. El administrador lo revisará pronto.'
    });
  } catch (err) {
    handleError(res, err, 'Error al enviar mensaje');
  }
});

// Endpoint para obtener mensajes enviados por un profesor/remitente
app.get('/api/messages/mine', async (req, res) => {
  try {
    const { senderName } = req.query;

    if (!senderName || String(senderName).trim() === '') {
      return res.status(400).json({ error: 'senderName es requerido' });
    }

    const [messages] = await pool.query(
      `SELECT * FROM admin_messages
       WHERE sender_name = ?
       ORDER BY created_at DESC`,
      [String(senderName).trim()]
    );

    res.json(messages);
  } catch (err) {
    handleError(res, err, 'Error al obtener historial de mensajes');
  }
});

// ============================================
// INICIAR SERVIDOR
// ============================================

const PORT = process.env.PORT || 3001;

const startServer = async () => {
  try {
    await ensureOperationalTables();
    console.log('✅ Tablas operativas verificadas');
  } catch (err) {
    console.error('❌ No se pudieron verificar tablas operativas:', err);
  }

  app.listen(PORT, () => {
    console.log(`🚀 Servidor backend escuchando en puerto ${PORT}`);
  });
};

startServer();
