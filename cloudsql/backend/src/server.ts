/**
 * AstraSense Cloud SQL Telemetry Ingestion & REST Query Server
 * Microservice deploying to Google Cloud Run with IAM Cloud SQL connectivity.
 */

import http from 'http';
import {
  authenticateCrewMember,
  insertBatchBiometricTelemetry,
  getFleetHealthRiskAggregation,
  getCrewTelemetryTrends,
  queryMissionAndDonkiLogs,
  insertSystemOverride,
} from './queries';
import { closePool } from './db';

const PORT = parseInt(process.env.PORT || '8080', 10);

const server = http.createServer(async (req, res) => {
  // CORS & Security Headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  const url = new URL(req.url || '/', `http://${req.headers.host}`);
  const pathname = url.pathname;

  try {
    // Health Check Endpoint
    if (pathname === '/health' && req.method === 'GET') {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ status: 'HEALTHY', database: 'CLOUD_SQL_POSTGRESQL', timestamp: new Date() }));
      return;
    }

    // 1. Crew Authentication
    if (pathname === '/api/v1/auth/login' && req.method === 'POST') {
      const body = await parseJsonBody(req);
      const { identifier, accessKey } = body;
      if (!identifier || !accessKey) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Missing identifier or accessKey' }));
        return;
      }
      const crew = await authenticateCrewMember(identifier, accessKey);
      if (!crew) {
        res.writeHead(401, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Invalid astronaut credentials or clearance' }));
        return;
      }
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ success: true, crew }));
      return;
    }

    // 2. High-Throughput Batch Telemetry Sync
    if (pathname === '/api/v1/telemetry/batch' && req.method === 'POST') {
      const body = await parseJsonBody(req);
      const records = Array.isArray(body) ? body : body.records;
      if (!records || !Array.isArray(records)) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Payload must contain a records array' }));
        return;
      }
      const count = await insertBatchBiometricTelemetry(records);
      res.writeHead(201, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ success: true, insertedCount: count }));
      return;
    }

    // 3. Fleet Health Risk Summary
    if (pathname === '/api/v1/telemetry/fleet-summary' && req.method === 'GET') {
      const summary = await getFleetHealthRiskAggregation();
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify(summary));
      return;
    }

    // 4. Individual Crew Telemetry Trends
    if (pathname.startsWith('/api/v1/telemetry/trends/') && req.method === 'GET') {
      const astronautId = pathname.split('/').pop() || '';
      const hours = parseInt(url.searchParams.get('hours') || '24', 10);
      const trends = await getCrewTelemetryTrends(astronautId, hours);
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify(trends));
      return;
    }

    // 5. NASA DONKI & Space Weather Mission Logs
    if (pathname === '/api/v1/logs/mission' && req.method === 'GET') {
      const severity = url.searchParams.get('severity') || undefined;
      const eventType = url.searchParams.get('eventType') || undefined;
      const status = url.searchParams.get('status') || undefined;
      const limit = parseInt(url.searchParams.get('limit') || '50', 10);

      const logs = await queryMissionAndDonkiLogs({ severity, eventType, status, limit });
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ count: logs.length, logs }));
      return;
    }

    // 6. Environmental Life-Support Overrides
    if (pathname === '/api/v1/system/override' && req.method === 'POST') {
      const body = await parseJsonBody(req);
      const id = await insertSystemOverride(body);
      res.writeHead(201, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ success: true, overrideId: id }));
      return;
    }

    // Route Not Found
    res.writeHead(404, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ error: 'Endpoint not found', availableRoutes: ['/health', '/api/v1/auth/login', '/api/v1/telemetry/batch', '/api/v1/telemetry/fleet-summary', '/api/v1/telemetry/trends/:id', '/api/v1/logs/mission', '/api/v1/system/override'] }));
  } catch (err: any) {
    console.error('[Server Error]', err);
    res.writeHead(500, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ error: 'Internal Server Error', message: err.message }));
  }
});

function parseJsonBody(req: http.IncomingMessage): Promise<any> {
  return new Promise((resolve, reject) => {
    let data = '';
    req.on('data', chunk => { data += chunk; });
    req.on('end', () => {
      try {
        resolve(data ? JSON.parse(data) : {});
      } catch (e) {
        reject(new Error('Invalid JSON'));
      }
    });
    req.on('error', reject);
  });
}

// Graceful Termination
process.on('SIGINT', async () => {
  console.log('[Server] Shutting down...');
  server.close(async () => {
    await closePool();
    process.exit(0);
  });
});

process.on('SIGTERM', async () => {
  console.log('[Server] Terminating...');
  server.close(async () => {
    await closePool();
    process.exit(0);
  });
});

if (require.main === module) {
  server.listen(PORT, () => {
    console.log(`[AstraSense Cloud SQL Server] Running on http://localhost:${PORT}`);
  });
}

export default server;
