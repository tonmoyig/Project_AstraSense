package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AstraRepository
import com.example.model.CrewMember
import com.example.model.DestinationType
import com.example.model.UserRole
import com.example.service.AstraAudioAlarmService
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.WarningAmber

/**
 * Adaptive Top Mission Bar responsive across Portrait and Landscape orientations.
 * Integrates real-time audio warning mute controls, high-priority Emergency SOS button,
 * mission destination picker, offline sync queue, and portal status.
 */
@Composable
fun TopMissionBar(
  currentDestination: DestinationType,
  onDestinationSelected: (DestinationType) -> Unit,
  currentUser: CrewMember?,
  isDarkMode: Boolean,
  onToggleTheme: () -> Unit,
  unsyncedPackets: Int,
  onOpenSync: () -> Unit,
  onOpenProfile: () -> Unit,
  onSwitchPortal: () -> Unit,
  onOpenSettings: (() -> Unit)? = null,
  activePortalName: String = "PORTAL",
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var destinationMenuExpanded by remember { mutableStateOf(false) }
  val isSosActive by AstraRepository.isEmergencySosActive.collectAsState()
  val isMuted by AstraAudioAlarmService.isMuted.collectAsState()

  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f))
      .border(1.dp, GlassBorderStroke, RoundedCornerShape(16.dp))
      .padding(horizontal = 12.dp, vertical = 8.dp)
  ) {
    val isPortrait = maxWidth < 540.dp

    Column(modifier = Modifier.fillMaxWidth()) {
      if (isPortrait) {
        // ================= PORTRAIT ADAPTIVE LAYOUT =================
        // Row 1: Brand Logo + Title + Audio Mute + Emergency SOS (high-priority controls front and center)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = 0.2f))
                .border(1.dp, NeonCyan, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Rocket,
                contentDescription = "AstraSense Logo",
                tint = NeonCyan,
                modifier = Modifier.size(16.dp)
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text(
                text = "ASTRA SENSE",
                style = MaterialTheme.typography.titleSmall.copy(
                  letterSpacing = 1.2.sp,
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
              )
              Text(
                text = "NASA OS • CLEARANCE ACTIVE",
                style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 7.5.sp)
              )
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            // Audio Alarm Mute / Unmute Toggle Button
            IconButton(
              onClick = { AstraAudioAlarmService.toggleMute(context) },
              modifier = Modifier.size(32.dp).testTag("audio_mute_toggle_button")
            ) {
              Icon(
                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = if (isMuted) "Unmute Alarm" else "Mute Alarm",
                tint = if (isMuted) WarningAmber else NeonCyan,
                modifier = Modifier.size(17.dp)
              )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // High-Priority Emergency SOS Button
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSosActive) CriticalRed else CriticalRed.copy(alpha = 0.22f))
                .border(1.dp, CriticalRed, RoundedCornerShape(8.dp))
                .clickable {
                  if (isSosActive) {
                    AstraRepository.deactivateEmergencySos()
                  } else {
                    AstraRepository.triggerEmergencySos(currentUser)
                  }
                }
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .testTag("top_mission_bar_sos_button")
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Emergency,
                  contentDescription = "Emergency SOS",
                  tint = if (isSosActive) Color.White else CriticalRed,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isSosActive) "SOS 10Hz" else "SOS DISTRESS",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isSosActive) Color.White else CriticalRed,
                    fontWeight = FontWeight.Black,
                    fontSize = 8.5.sp
                  )
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Row 2: Destination Picker + Comms Delay + Sync Status + Theme + Avatar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Destination Picker Dropdown
          Box {
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable { destinationMenuExpanded = true }
                .padding(horizontal = 6.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Public,
                contentDescription = null,
                tint = BrightTeal,
                modifier = Modifier.size(11.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = currentDestination.displayName.split(" ").take(2).joinToString(" "),
                style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 9.sp)
              )
              Spacer(modifier = Modifier.width(2.dp))
              Text("▼", color = BrightTeal, fontSize = 7.sp)
            }

            DropdownMenu(
              expanded = destinationMenuExpanded,
              onDismissRequest = { destinationMenuExpanded = false },
              modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, GlassBorderStroke, RoundedCornerShape(8.dp))
            ) {
              DestinationType.entries.forEach { dest ->
                DropdownMenuItem(
                  text = {
                    Column {
                      Text(dest.displayName, style = MaterialTheme.typography.titleSmall.copy(color = Color.White))
                      Text("${dest.distanceDescription} • ${dest.habitatVehicle}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 10.sp))
                    }
                  },
                  onClick = {
                    onDestinationSelected(dest)
                    destinationMenuExpanded = false
                  }
                )
              }
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            // Comms Delay Badge
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(horizontal = 5.dp, vertical = 2.5.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.SatelliteAlt,
                  contentDescription = null,
                  tint = if (currentDestination.commsDelaySeconds > 0) WarningAmber else NominalGreen,
                  modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = if (currentDestination.commsDelaySeconds > 0) "${currentDestination.commsDelaySeconds / 60}m" else "LIVE",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (currentDestination.commsDelaySeconds > 0) WarningAmber else NominalGreen,
                    fontSize = 8.sp
                  )
                )
              }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Sync Queue Icon
            Box(
              modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (unsyncedPackets > 0) WarningAmber.copy(alpha = 0.2f) else NominalGreen.copy(alpha = 0.2f))
                .clickable { onOpenSync() },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (unsyncedPackets > 0) Icons.Default.CloudOff else Icons.Default.CloudDone,
                contentDescription = "Sync Queue",
                tint = if (unsyncedPackets > 0) WarningAmber else NominalGreen,
                modifier = Modifier.size(13.dp)
              )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Theme Toggle
            IconButton(
              onClick = onToggleTheme,
              modifier = Modifier.size(26.dp).testTag("theme_toggle_button")
            ) {
              Icon(
                imageVector = if (isDarkMode) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                contentDescription = "Toggle Theme",
                tint = NeonCyan,
                modifier = Modifier.size(13.dp)
              )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Switch Portal Gateway Button
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(NeonCyan.copy(alpha = 0.18f))
                .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                .clickable { onSwitchPortal() }
                .padding(horizontal = 6.dp, vertical = 3.dp)
                .testTag("top_mission_bar_switch_portal_button"),
              contentAlignment = Alignment.Center
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.SwapHoriz,
                  contentDescription = "Switch Portal",
                  tint = NeonCyan,
                  modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                  text = "PORTAL",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = NeonCyan,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold
                  )
                )
              }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Avatar Button
            currentUser?.let { user ->
              Box(
                modifier = Modifier
                  .size(26.dp)
                  .clip(CircleShape)
                  .background(if (user.role == UserRole.ADMIN) NeonCyan else BrightTeal)
                  .clickable { onOpenProfile() }
                  .testTag("profile_avatar_button"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = user.fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 8.5.sp
                  )
                )
              }
            }
          }
        }
      } else {
        // ================= LANDSCAPE / WIDE LAYOUT =================
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // App Insignia & Destination
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = 0.18f))
                .border(1.dp, NeonCyan, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Rocket,
                contentDescription = "AstraSense Logo",
                tint = NeonCyan,
                modifier = Modifier.size(18.dp)
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "ASTRA SENSE",
                  style = MaterialTheme.typography.titleMedium.copy(
                    letterSpacing = 1.6.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                  )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(NominalGreen.copy(alpha = 0.2f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                  Text(
                    "NASA OS",
                    style = MaterialTheme.typography.labelSmall.copy(color = NominalGreen, fontSize = 7.sp)
                  )
                }
              }

              // Destination Picker
              Box {
                Row(
                  modifier = Modifier
                    .clickable { destinationMenuExpanded = true }
                    .padding(vertical = 1.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = BrightTeal,
                    modifier = Modifier.size(11.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = currentDestination.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 10.sp)
                  )
                  Spacer(modifier = Modifier.width(2.dp))
                  Text("▼", color = BrightTeal, fontSize = 7.sp)
                }

                DropdownMenu(
                  expanded = destinationMenuExpanded,
                  onDismissRequest = { destinationMenuExpanded = false },
                  modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, GlassBorderStroke, RoundedCornerShape(8.dp))
                ) {
                  DestinationType.entries.forEach { dest ->
                    DropdownMenuItem(
                      text = {
                        Column {
                          Text(dest.displayName, style = MaterialTheme.typography.titleSmall.copy(color = Color.White))
                          Text("${dest.distanceDescription} • ${dest.habitatVehicle}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 10.sp))
                        }
                      },
                      onClick = {
                        onDestinationSelected(dest)
                        destinationMenuExpanded = false
                      }
                    )
                  }
                }
              }
            }
          }

          // Right Action Controls
          Row(verticalAlignment = Alignment.CenterVertically) {
            // Audio Alarm Mute / Unmute Toggle Button
            IconButton(
              onClick = { AstraAudioAlarmService.toggleMute(context) },
              modifier = Modifier.size(30.dp).testTag("audio_mute_toggle_button")
            ) {
              Icon(
                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = if (isMuted) "Unmute Alarm" else "Mute Alarm",
                tint = if (isMuted) WarningAmber else NeonCyan,
                modifier = Modifier.size(16.dp)
              )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Comms Delay Badge
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.SatelliteAlt,
                  contentDescription = null,
                  tint = if (currentDestination.commsDelaySeconds > 0) WarningAmber else NominalGreen,
                  modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (currentDestination.commsDelaySeconds > 0)
                    "${currentDestination.commsDelaySeconds / 60}m DELAY"
                  else
                    "LIVE LINK",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (currentDestination.commsDelaySeconds > 0) WarningAmber else NominalGreen,
                    fontSize = 8.sp
                  )
                )
              }
            }

            Spacer(modifier = Modifier.width(5.dp))

            // Earth Offline / Sync Packet Queue
            Box(
              modifier = Modifier
                .clip(CircleShape)
                .background(if (unsyncedPackets > 0) WarningAmber.copy(alpha = 0.2f) else NominalGreen.copy(alpha = 0.2f))
                .clickable { onOpenSync() }
                .padding(5.dp)
            ) {
              Icon(
                imageVector = if (unsyncedPackets > 0) Icons.Default.CloudOff else Icons.Default.CloudDone,
                contentDescription = "Sync Queue",
                tint = if (unsyncedPackets > 0) WarningAmber else NominalGreen,
                modifier = Modifier.size(15.dp)
              )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Dark / Standard Mode Toggle
            IconButton(
              onClick = onToggleTheme,
              modifier = Modifier.size(28.dp).testTag("theme_toggle_button")
            ) {
              Icon(
                imageVector = if (isDarkMode) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                contentDescription = "Toggle Theme",
                tint = NeonCyan,
                modifier = Modifier.size(15.dp)
              )
            }

            // Manual Feature Controls & Settings Icon Button
            IconButton(
              onClick = { onOpenSettings?.invoke() ?: onOpenProfile() },
              modifier = Modifier.size(28.dp).testTag("top_bar_settings_button")
            ) {
              Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Manual Feature Controls & Settings",
                tint = NeonCyan,
                modifier = Modifier.size(15.dp)
              )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // High-Priority Emergency SOS Button
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSosActive) CriticalRed else CriticalRed.copy(alpha = 0.2f))
                .border(1.dp, CriticalRed, RoundedCornerShape(8.dp))
                .clickable {
                  if (isSosActive) {
                    AstraRepository.deactivateEmergencySos()
                  } else {
                    AstraRepository.triggerEmergencySos(currentUser)
                  }
                }
                .padding(horizontal = 7.dp, vertical = 4.dp)
                .testTag("top_mission_bar_sos_button")
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Emergency,
                  contentDescription = "Emergency SOS",
                  tint = if (isSosActive) Color.White else CriticalRed,
                  modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = if (isSosActive) "SOS 10Hz" else "SOS",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isSosActive) Color.White else CriticalRed,
                    fontWeight = FontWeight.Black,
                    fontSize = 8.5.sp
                  )
                )
              }
            }

            Spacer(modifier = Modifier.width(5.dp))

            // User Avatar Button
            currentUser?.let { user ->
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .background(if (user.role == UserRole.ADMIN) NeonCyan else BrightTeal)
                  .clickable { onOpenProfile() }
                  .testTag("profile_avatar_button"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = user.fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp
                  )
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Bottom Row: Dedicated Quick Portal Switcher & Settings Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Color.Black.copy(alpha = 0.55f))
          .border(1.dp, GlassBorderStroke, RoundedCornerShape(8.dp))
          .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f, fill = false),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(if (activePortalName.contains("ADMIN", ignoreCase = true)) NeonCyan else BrightTeal)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "ACTIVE: ",
            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
          )
          Text(
            text = activePortalName.replace("INDIVIDUAL ACCESS", "").replace("(", "").replace(")", "").trim().uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
              color = if (activePortalName.contains("ADMIN", ignoreCase = true)) NeonCyan else BrightTeal,
              fontWeight = FontWeight.Black,
              fontSize = 8.5.sp,
              letterSpacing = 0.5.sp
            ),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
          modifier = Modifier.wrapContentWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Quick Settings & Manual Controls Chip
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(BrightTeal.copy(alpha = 0.16f))
              .border(1.dp, BrightTeal.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
              .clickable { onOpenSettings?.invoke() ?: onOpenProfile() }
              .padding(horizontal = 7.dp, vertical = 4.dp)
              .testTag("top_bar_quick_settings_chip"),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = "Controls",
              tint = BrightTeal,
              modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "CONTROLS",
              style = MaterialTheme.typography.labelSmall.copy(
                color = BrightTeal,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold
              ),
              maxLines = 1,
              softWrap = false
            )
          }

          // Portals Switcher Button
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(NeonCyan.copy(alpha = 0.18f))
              .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
              .clickable { onSwitchPortal() }
              .padding(horizontal = 7.dp, vertical = 4.dp)
              .testTag("top_bar_portals_chip"),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.SwapHoriz,
              contentDescription = "Switch Portals",
              tint = NeonCyan,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "PORTALS",
              style = MaterialTheme.typography.labelSmall.copy(
                color = NeonCyan,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold
              ),
              maxLines = 1,
              softWrap = false
            )
          }
        }
      }
    }
  }
}
