/**
 * AstraSense Cloud SQL for PostgreSQL Database Connection Pool
 *
 * Implements:
 * 1. Google Cloud SQL Connector with IAM Database Authentication (AuthType.IAM)
 * 2. Enterprise Connection Pooling with keepAlive and graceful shutdown
 * 3. Fallback to Cloud SQL Auth Proxy / Unix Sockets / SSL TCP configuration
 */

import { Connector, AuthType } from '@google-cloud/cloud-sql-connector';
import { Pool, PoolConfig, QueryResult, QueryResultRow } from 'pg';
import * as dotenv from 'dotenv';

dotenv.config();

// Environment Configuration
const CLOUD_SQL_INSTANCE = process.env.INSTANCE_CONNECTION_NAME || 'astrasense-fleet:us-central1:astrasense-pg';
const DB_USER = process.env.DB_USER || 'astrasense-iam-user@astrasense-fleet.iam';
const DB_NAME = process.env.DB_NAME || 'astrasensedb';
const DB_PORT = parseInt(process.env.DB_PORT || '5432', 10);
const USE_IAM_AUTH = process.env.USE_IAM_AUTH !== 'false'; // Default to true for secure IAM auth
const DB_PASSWORD = process.env.DB_PASSWORD || ''; // Empty when using IAM database authentication

let pool: Pool | null = null;
let connector: Connector | null = null;

/**
 * Initializes and returns a singleton PostgreSQL connection pool
 * configured for Google Cloud SQL.
 */
export async function getDbPool(): Promise<Pool> {
  if (pool) {
    return pool;
  }

  try {
    if (process.env.NODE_ENV === 'production' || process.env.USE_CLOUD_SQL_CONNECTOR === 'true') {
      console.log(`[CloudSQL] Initializing Google Cloud SQL Connector for instance: ${CLOUD_SQL_INSTANCE}`);

      connector = new Connector();
      const clientOpts = await connector.getOptions({
        instanceConnectionName: CLOUD_SQL_INSTANCE,
        authType: USE_IAM_AUTH ? AuthType.IAM : AuthType.PASSWORD,
      });

      const poolConfig: PoolConfig = {
        ...clientOpts,
        user: DB_USER,
        database: DB_NAME,
        password: USE_IAM_AUTH ? undefined : DB_PASSWORD,
        max: 20, // Max concurrent connections in pool
        min: 2,  // Keep warm connections
        idleTimeoutMillis: 30000,
        connectionTimeoutMillis: 10000,
        keepAlive: true,
      };

      pool = new Pool(poolConfig);
    } else {
      // Local development or via Cloud SQL Auth Proxy on 127.0.0.1:5432
      console.log(`[CloudSQL] Initializing standard PostgreSQL pool on host ${process.env.DB_HOST || '127.0.0.1'}`);

      pool = new Pool({
        host: process.env.DB_HOST || '127.0.0.1',
        port: DB_PORT,
        user: DB_USER,
        password: DB_PASSWORD,
        database: DB_NAME,
        max: 15,
        idleTimeoutMillis: 30000,
        connectionTimeoutMillis: 5000,
        ssl: process.env.DB_SSL === 'true' ? { rejectUnauthorized: false } : undefined,
      });
    }

    // Pool lifecycle logging
    pool.on('connect', () => {
      console.log('[CloudSQL] New client connection checked out from pool');
    });

    pool.on('error', (err) => {
      console.error('[CloudSQL] Unexpected error on idle client in pool:', err.message);
    });

    return pool;
  } catch (error) {
    console.error('[CloudSQL] Fatal connection pool initialization error:', error);
    throw error;
  }
}

/**
 * Helper to execute parameterized SQL queries with performance telemetry
 */
export async function query<T extends QueryResultRow = any>(
  text: string,
  params?: any[]
): Promise<QueryResult<T>> {
  const start = Date.now();
  const dbPool = await getDbPool();
  try {
    const res = await dbPool.query<T>(text, params);
    const duration = Date.now() - start;
    if (duration > 150) {
      console.warn(`[CloudSQL Slow Query] (${duration}ms) ${text.slice(0, 100)}...`);
    }
    return res;
  } catch (err: any) {
    console.error(`[CloudSQL Query Error] ${err.message} | Query: ${text}`);
    throw err;
  }
}

/**
 * Graceful shutdown for Cloud Run / Node container termination
 */
export async function closePool(): Promise<void> {
  if (pool) {
    console.log('[CloudSQL] Closing connection pool...');
    await pool.end();
    pool = null;
  }
  if (connector) {
    console.log('[CloudSQL] Disposing Cloud SQL connector...');
    connector.close();
    connector = null;
  }
}
