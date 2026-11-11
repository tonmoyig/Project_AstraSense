package com.example.data.cloudsql

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * SQLite Local Storage for AstraSense Offline-First Telemetry Queuing.
 * Caches high-frequency vitals, solar storm event logs, and life-support overrides
 * during deep-space RF blackouts or planetary occultations.
 */
class AstraOfflineQueueDbHelper(context: Context) : SQLiteOpenHelper(
  context,
  DATABASE_NAME,
  null,
  DATABASE_VERSION
) {

  companion object {
    private const val TAG = "AstraOfflineQueueDb"
    const val DATABASE_NAME = "astrasense_offline_queue.db"
    const val DATABASE_VERSION = 1

    const val TABLE_NAME = "offline_sync_queue"
    const val COLUMN_ID = "_id"
    const val COLUMN_RECORD_TYPE = "record_type"
    const val COLUMN_PAYLOAD_JSON = "payload_json"
    const val COLUMN_TIMESTAMP_MS = "timestamp_ms"
    const val COLUMN_RETRY_COUNT = "retry_count"
    const val COLUMN_STATUS = "status"

    const val TYPE_BIOMETRIC = "BIOMETRIC_TELEMETRY"
    const val TYPE_MISSION_LOG = "MISSION_LOG"
    const val TYPE_SYSTEM_OVERRIDE = "SYSTEM_OVERRIDE"

    private const val SQL_CREATE_TABLE = """
      CREATE TABLE IF NOT EXISTS $TABLE_NAME (
        $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        $COLUMN_RECORD_TYPE TEXT NOT NULL,
        $COLUMN_PAYLOAD_JSON TEXT NOT NULL,
        $COLUMN_TIMESTAMP_MS INTEGER NOT NULL,
        $COLUMN_RETRY_COUNT INTEGER DEFAULT 0,
        $COLUMN_STATUS TEXT DEFAULT 'PENDING'
      );
    """

    private const val SQL_CREATE_INDEX = """
      CREATE INDEX IF NOT EXISTS idx_offline_queue_status_time 
      ON $TABLE_NAME ($COLUMN_STATUS, $COLUMN_TIMESTAMP_MS ASC);
    """
  }

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(SQL_CREATE_TABLE)
    db.execSQL(SQL_CREATE_INDEX)
    Log.i(TAG, "Offline SQLite queue initialized successfully.")
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
    onCreate(db)
  }

  /**
   * Enqueues an offline telemetry record into the local SQLite database.
   */
  @Synchronized
  fun enqueueRecord(recordType: String, payloadJson: String, timestampMs: Long): Long {
    val db = writableDatabase
    val values = ContentValues().apply {
      put(COLUMN_RECORD_TYPE, recordType)
      put(COLUMN_PAYLOAD_JSON, payloadJson)
      put(COLUMN_TIMESTAMP_MS, timestampMs)
      put(COLUMN_RETRY_COUNT, 0)
      put(COLUMN_STATUS, "PENDING")
    }
    return db.insert(TABLE_NAME, null, values)
  }

  /**
   * Retrieves pending queued records up to the batch limit for synchronization.
   */
  @Synchronized
  fun peekPendingRecords(limit: Int = 50): List<QueuedSyncRecord> {
    val records = mutableListOf<QueuedSyncRecord>()
    val db = readableDatabase
    val cursor = db.query(
      TABLE_NAME,
      arrayOf(COLUMN_ID, COLUMN_RECORD_TYPE, COLUMN_PAYLOAD_JSON, COLUMN_TIMESTAMP_MS, COLUMN_RETRY_COUNT, COLUMN_STATUS),
      "$COLUMN_STATUS = ?",
      arrayOf("PENDING"),
      null,
      null,
      "$COLUMN_TIMESTAMP_MS ASC",
      limit.toString()
    )

    cursor.use {
      while (it.moveToNext()) {
        val id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID))
        val type = it.getString(it.getColumnIndexOrThrow(COLUMN_RECORD_TYPE))
        val json = it.getString(it.getColumnIndexOrThrow(COLUMN_PAYLOAD_JSON))
        val time = it.getLong(it.getColumnIndexOrThrow(COLUMN_TIMESTAMP_MS))
        val retries = it.getInt(it.getColumnIndexOrThrow(COLUMN_RETRY_COUNT))
        val status = it.getString(it.getColumnIndexOrThrow(COLUMN_STATUS))

        records.add(QueuedSyncRecord(id, type, json, time, retries, status))
      }
    }
    return records
  }

  /**
   * Removes successfully synchronized records from the local queue.
   */
  @Synchronized
  fun deleteRecords(ids: List<Long>): Int {
    if (ids.isEmpty()) return 0
    val db = writableDatabase
    val inClause = ids.joinToString(",")
    return db.delete(TABLE_NAME, "$COLUMN_ID IN ($inClause)", null)
  }

  /**
   * Increments the retry count of failed synchronization records.
   */
  @Synchronized
  fun markFailed(ids: List<Long>): Int {
    if (ids.isEmpty()) return 0
    val db = writableDatabase
    val inClause = ids.joinToString(",")
    val sql = "UPDATE $TABLE_NAME SET $COLUMN_RETRY_COUNT = $COLUMN_RETRY_COUNT + 1 WHERE $COLUMN_ID IN ($inClause)"
    db.execSQL(sql)
    return ids.size
  }

  /**
   * Returns the count of pending records queued locally.
   */
  @Synchronized
  fun getPendingCount(): Int {
    val db = readableDatabase
    val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME WHERE $COLUMN_STATUS = 'PENDING'", null)
    return cursor.use {
      if (it.moveToFirst()) it.getInt(0) else 0
    }
  }

  /**
   * Clears the entire offline queue (e.g. for testing or factory reset).
   */
  @Synchronized
  fun clearQueue(): Int {
    val db = writableDatabase
    return db.delete(TABLE_NAME, null, null)
  }
}
