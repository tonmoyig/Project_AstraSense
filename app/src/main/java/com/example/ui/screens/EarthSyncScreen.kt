package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AstraRepository
import com.example.model.DestinationType
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.StarlightBlue
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EarthSyncScreen(
  currentDestination: DestinationType,
  onOpenNasaOpenData: () -> Unit = {},
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onNavigateBack() }

  val scope = rememberCoroutineScope()
  val unsyncedCount by AstraRepository.unsyncedPacketsCount.collectAsState()
  val lastSyncTime by AstraRepository.lastSyncTimestamp.collectAsState()
  var isSyncingNow by remember { mutableStateOf(false) }
  var syncCompletedNotification by remember { mutableStateOf<String?>(null) }

  // Cloud SQL for PostgreSQL Relational Sync State
  val cloudSqlSyncState by com.example.data.cloudsql.CloudSqlSyncManager.syncState.collectAsState()
  val cloudSqlPendingCount by com.example.data.cloudsql.CloudSqlSyncManager.pendingQueueCount.collectAsState()
  val cloudSqlLastSync by com.example.data.cloudsql.CloudSqlSyncManager.lastSyncTimestamp.collectAsState()
  val cloudSqlTotalSynced by com.example.data.cloudsql.CloudSqlSyncManager.totalSyncedRecords.collectAsState()
  var isCloudSqlSyncingNow by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(14.dp)
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = NeonCyan
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                "EARTH SYNCHRONIZATION QUEUE",
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
              )
              Text(
                "Offline Store-and-Forward Telemetry Protocols",
                style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 9.sp)
              )
            }
          }
          StatusBadge(text = "DSN S-BAND", color = StarlightBlue)
        }
      }

      // Offline Operation Overview Card
      item {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                "DEEP SPACE COMMS PARAMETERS",
                style = MaterialTheme.typography.labelMedium.copy(color = NeonCyan)
              )
              Text(currentDestination.displayName, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              "AstraSense operates 100% autonomously without Earth connectivity. Vital biometrics, dosimeter logs, and safety alerts are encrypted and safely stored in on-device storage. When the high-gain transmission antenna is aligned, telemetry packets are batched to Mission Control.",
              style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFCBD5E1), lineHeight = 20.sp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Metric parameter strip
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color.Black.copy(alpha = 0.35f))
                  .padding(8.dp)
              ) {
                Column {
                  Text("ONE-WAY DELAY", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp))
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    if (currentDestination.commsDelaySeconds > 0) "${currentDestination.commsDelaySeconds / 60}m ${currentDestination.commsDelaySeconds % 60}s" else "0.02s",
                    style = MaterialTheme.typography.titleSmall.copy(color = WarningAmber)
                  )
                }
              }

              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color.Black.copy(alpha = 0.35f))
                  .padding(8.dp)
              ) {
                Column {
                  Text("QUEUED PACKETS", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp))
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    "$unsyncedCount PACKETS",
                    style = MaterialTheme.typography.titleSmall.copy(color = if (unsyncedCount > 0) NeonCyan else NominalGreen)
                  )
                }
              }

              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color.Black.copy(alpha = 0.35f))
                  .padding(8.dp)
              ) {
                Column {
                  Text("LAST EARTH ACK", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp))
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(lastSyncTime, style = MaterialTheme.typography.titleSmall.copy(color = NominalGreen, fontSize = 11.sp))
                }
              }
            }
          }
        }
      }

      // Sync Trigger Card
      item {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = if (unsyncedCount > 0) Icons.Default.CloudUpload else Icons.Default.CheckCircle,
              contentDescription = null,
              tint = if (unsyncedCount > 0) NeonCyan else NominalGreen,
              modifier = Modifier.size(44.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = if (unsyncedCount > 0) "PENDING TRANSMISSION BUNDLE" else "ALL TELEMETRY SYNCHRONIZED",
              style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = "Target Ground Ground Stations: Goldstone 70m Antenna & Madrid Complex",
              style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
            )

            syncCompletedNotification?.let { notif ->
              Spacer(modifier = Modifier.height(8.dp))
              Text(notif, style = MaterialTheme.typography.bodySmall.copy(color = NominalGreen))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
              onClick = {
                isSyncingNow = true
                scope.launch {
                  delay(2200) // Simulate transmission packaging and carrier wave lock
                  AstraRepository.syncWithEarth()
                  isSyncingNow = false
                  syncCompletedNotification = "Telemetry packet verified & received by Houston Flight Medicine."
                }
              },
              enabled = !isSyncingNow,
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("earth_sync_submit_button"),
              colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
              shape = RoundedCornerShape(12.dp)
            ) {
              if (isSyncingNow) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("TRANSMITTING ENCRYPTED TELEMETRY...", style = MaterialTheme.typography.labelSmall)
              } else {
                Icon(Icons.Default.SatelliteAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("INITIATE HIGH-GAIN S-BAND SYNC", style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
              }
            }
          }
        }
      }

      // NASA Open Data Integration Card
      item {
        GlassCard(modifier = Modifier.fillMaxWidth().testTag("earth_sync_nasa_card")) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Public, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  "NASA OPEN DATA & DONKI REPOSITORIES",
                  style = MaterialTheme.typography.labelMedium.copy(color = NeonCyan, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                )
              }
              StatusBadge(text = "LIVE ACCESS", color = NominalGreen)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              "Synchronize with NASA's official Open Data portal (data.nasa.gov) and Space Weather Database of Notifications (api.nasa.gov/DONKI). View EVA mission logs, live solar radiation storms, and HRP hazard mitigation protocols.",
              style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1), fontSize = 11.5.sp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
              onClick = onOpenNasaOpenData,
              colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth().height(38.dp).testTag("open_nasa_open_data_from_sync")
            ) {
              Icon(Icons.Default.Dataset, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("EXPLORE NASA DATASETS & SPACE WEATHER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
            }
          }
        }
      }

      // Cloud SQL for PostgreSQL Relational Database Synchronization Card
      item {
        GlassCard(modifier = Modifier.fillMaxWidth().testTag("earth_sync_cloudsql_card")) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
              ) {
                Icon(Icons.Default.Storage, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f, fill = false)) {
                  Text(
                    "CLOUD SQL FOR POSTGRESQL",
                    style = MaterialTheme.typography.titleSmall.copy(
                      color = Color.White,
                      fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                      fontSize = 11.5.sp
                    ),
                    maxLines = 1
                  )
                  Text(
                    "Relational Telemetry: astronauts • biometric_telemetry • mission_logs",
                    style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 8.5.sp),
                    maxLines = 1
                  )
                }
              }

              StatusBadge(
                text = when (cloudSqlSyncState) {
                  com.example.data.cloudsql.CloudSqlSyncStatus.ONLINE_SYNCED -> "PG SYNCED"
                  com.example.data.cloudsql.CloudSqlSyncStatus.OFFLINE_RF_BLACKOUT -> "RF BLACKOUT"
                  com.example.data.cloudsql.CloudSqlSyncStatus.SYNCING_BATCH -> "SYNCING..."
                  com.example.data.cloudsql.CloudSqlSyncStatus.SYNC_ERROR -> "RETRY"
                },
                color = when (cloudSqlSyncState) {
                  com.example.data.cloudsql.CloudSqlSyncStatus.ONLINE_SYNCED -> NominalGreen
                  com.example.data.cloudsql.CloudSqlSyncStatus.OFFLINE_RF_BLACKOUT -> WarningAmber
                  com.example.data.cloudsql.CloudSqlSyncStatus.SYNCING_BATCH -> NeonCyan
                  com.example.data.cloudsql.CloudSqlSyncStatus.SYNC_ERROR -> com.example.ui.theme.CriticalRed
                }
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              "Local SQLite offline queue automatically buffers Bio-Harness vitals and cabin overrides during solar radio blackouts, then bulk-syncs parameterized relational records to Cloud SQL with IAM database authentication.",
              style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1), fontSize = 11.sp, lineHeight = 16.sp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3-Metric Cloud SQL telemetry strip
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color.Black.copy(alpha = 0.4f))
                  .padding(8.dp)
              ) {
                Column {
                  Text("LOCAL SQLITE QUEUE", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 7.5.sp))
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    "$cloudSqlPendingCount QUEUED",
                    style = MaterialTheme.typography.titleSmall.copy(
                      color = if (cloudSqlPendingCount > 0) WarningAmber else NominalGreen,
                      fontSize = 11.5.sp,
                      fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                  )
                }
              }

              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color.Black.copy(alpha = 0.4f))
                  .padding(8.dp)
              ) {
                Column {
                  Text("POSTGRES COMMITS", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 7.5.sp))
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    "$cloudSqlTotalSynced COMMITTED",
                    style = MaterialTheme.typography.titleSmall.copy(color = NeonCyan, fontSize = 11.5.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                  )
                }
              }

              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color.Black.copy(alpha = 0.4f))
                  .padding(8.dp)
              ) {
                Column {
                  Text("LAST COMMIT TIME", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 7.5.sp))
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    cloudSqlLastSync,
                    style = MaterialTheme.typography.titleSmall.copy(color = StarlightBlue, fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
              onClick = {
                isCloudSqlSyncingNow = true
                scope.launch {
                  com.example.data.cloudsql.CloudSqlSyncManager.syncPendingRecordsAsync()
                  isCloudSqlSyncingNow = false
                }
              },
              enabled = !isCloudSqlSyncingNow,
              colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan.copy(alpha = 0.25f),
                contentColor = NeonCyan
              ),
              border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth().height(38.dp).testTag("cloudsql_sync_now_button")
            ) {
              if (isCloudSqlSyncingNow) {
                CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("TRANSMITTING BATCH TO CLOUD SQL...", style = MaterialTheme.typography.labelSmall)
              } else {
                Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  "SYNC QUEUED SQL RELATIONAL RECORDS",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                )
              }
            }
          }
        }
      }

      // Transmission Audit Log
      item {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
          Column {
            Text(
              "GROUND CONTROL AUDIT LOG (HIPAA COMPLIANT)",
              style = MaterialTheme.typography.labelMedium.copy(color = BrightTeal)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val logs = listOf(
              "T-04:12:08 UTC • 12 Packets • 256-bit AES • ACK RECEIVED (Houston)",
              "T-10:45:19 UTC • 18 Packets • 256-bit AES • ACK RECEIVED (Canberra DSN)",
              "T-18:22:04 UTC • 14 Packets • 256-bit AES • ACK RECEIVED (Madrid DSN)"
            )

            logs.forEach { log ->
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color.Black.copy(alpha = 0.3f))
                  .padding(8.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Lock, contentDescription = null, tint = NominalGreen, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(log, style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontSize = 9.sp))
                }
              }
            }
          }
        }
      }

      item { Spacer(modifier = Modifier.height(16.dp)) }
    }
  }
}
