package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NasaOpenDataRepository
import com.example.model.DestinationType
import com.example.model.NasaDatasetPackage
import com.example.model.NasaDonkiNotification
import com.example.model.NasaEvaRecord
import com.example.model.NasaHrpHazard
import com.example.model.RiskLevel
import com.example.ui.components.GlassCard
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.CelestialAmber
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.StarlightBlue
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.launch

@Composable
fun NasaOpenDataScreen(
  currentDestination: DestinationType,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onNavigateBack() }

  val coroutineScope = rememberCoroutineScope()
  var selectedTabIndex by remember { mutableIntStateOf(0) }
  val tabs = listOf("🛰️ EVA DATASET", "☀️ DONKI RADIATION", "🧬 HRP 5 HAZARDS", "🔍 DATASET SEARCH")

  val evaRecords by NasaOpenDataRepository.evaRecords.collectAsState()
  val spaceWeatherAlerts by NasaOpenDataRepository.spaceWeatherAlerts.collectAsState()
  val hrpHazards = remember { NasaOpenDataRepository.hrpHazards }
  val searchResults by NasaOpenDataRepository.searchResults.collectAsState()
  val isSearching by NasaOpenDataRepository.isSearching.collectAsState()

  var appliedAlertMessage by remember { mutableStateOf<String?>(null) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Top Mission Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFF070E1A))
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.size(38.dp).testTag("nasa_open_data_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = NeonCyan
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "NASA OPEN DATA & TELEMETRY",
              style = MaterialTheme.typography.titleMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
              )
            )
            Text(
              text = "DATA.NASA.GOV & API.NASA.GOV/DONKI • ${currentDestination.displayName.uppercase()}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = BrightTeal,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
              )
            )
          }
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0D253A))
            .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "NASA OPEN API",
            style = MaterialTheme.typography.labelSmall.copy(
              color = NeonCyan,
              fontWeight = FontWeight.Bold,
              fontSize = 9.sp
            )
          )
        }
      }

      // Tab Navigation Bar
      TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color(0xFF0B1426),
        contentColor = NeonCyan,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
            color = NeonCyan,
            height = 3.dp
          )
        }
      ) {
        tabs.forEachIndexed { index, title ->
          Tab(
            selected = selectedTabIndex == index,
            onClick = { selectedTabIndex = index },
            text = {
              Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 11.sp,
                  color = if (selectedTabIndex == index) NeonCyan else Color.Gray
                ),
                maxLines = 1
              )
            },
            modifier = Modifier.testTag("nasa_tab_$index")
          )
        }
      }

      // Applied notification banner if space weather was pushed to cabin sensors
      AnimatedVisibility(
        visible = appliedAlertMessage != null,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        appliedAlertMessage?.let { msg ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(NominalGreen.copy(alpha = 0.2f))
              .border(1.dp, NominalGreen, RoundedCornerShape(0.dp))
              .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NominalGreen, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = msg, style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontSize = 11.sp))
            }
            Text(
              text = "DISMISS",
              style = MaterialTheme.typography.labelSmall.copy(color = NominalGreen, fontWeight = FontWeight.Bold),
              modifier = Modifier.clickable { appliedAlertMessage = null }
            )
          }
        }
      }

      // Tab Body Content
      Box(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        when (selectedTabIndex) {
          0 -> EvaDatasetContent(evaRecords = evaRecords)
          1 -> DonkiSpaceWeatherContent(
            alerts = spaceWeatherAlerts,
            onApplyToCabin = { alert ->
              NasaOpenDataRepository.applySpaceWeatherToShipCabin(alert)
              appliedAlertMessage = "Space Weather '${alert.solarHazardClass}' synced to ship sensors! Radiation rate: ${alert.peakFluxRate} mSv/h"
            }
          )
          2 -> HrpHazardsContent(hazards = hrpHazards)
          3 -> DatasetSearchContent(
            searchResults = searchResults,
            isSearching = isSearching,
            onSearch = { query ->
              coroutineScope.launch {
                NasaOpenDataRepository.searchNasaDatasets(query)
              }
            }
          )
        }
      }
    }
  }
}

// =========================================================================
// 1. EVA DATASET TAB CONTENT (data.nasa.gov/dataset/extra-vehicular-activity)
// =========================================================================
@Composable
private fun EvaDatasetContent(evaRecords: List<NasaEvaRecord>) {
  var selectedFilter by remember { mutableStateOf("ALL") }

  val filteredRecords = remember(evaRecords, selectedFilter) {
    when (selectedFilter) {
      "USA" -> evaRecords.filter { it.country.equals("USA", ignoreCase = true) }
      "RUSSIA" -> evaRecords.filter { it.country.equals("Russia", ignoreCase = true) }
      "LUNAR" -> evaRecords.filter { it.vehicle.contains("Apollo", ignoreCase = true) }
      "ISS" -> evaRecords.filter { it.vehicle.contains("Station", ignoreCase = true) }
      else -> evaRecords
    }
  }

  val totalMinutes = remember(filteredRecords) { filteredRecords.sumOf { it.durationMinutes } }
  val totalHours = totalMinutes / 60
  val meanDose = remember(filteredRecords) {
    if (filteredRecords.isNotEmpty()) filteredRecords.map { it.radiationDoseEstimateMsv }.average() else 0.0
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // NASA Open Data Citation Card
    item {
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
              Icon(Icons.Default.Dataset, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "DATA.NASA.GOV DATASET REPOSITORY",
                style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(BrightTeal.copy(alpha = 0.15f))
                .border(1.dp, BrightTeal.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
              Text(
                text = "OFFICIAL RECORD",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = BrightTeal,
                  fontWeight = FontWeight.Bold,
                  fontSize = 8.5.sp,
                  letterSpacing = 0.5.sp
                ),
                maxLines = 1,
                softWrap = false
              )
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Extra-vehicular Activity (EVA) - US and Russia: Chronological spacewalk telemetry logging astronaut and cosmonaut surface and microgravity excursions beyond Earth atmosphere.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 11.5.sp)
          )
          Spacer(modifier = Modifier.height(10.dp))

          // Telemetry Summary Strip
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF081220), RoundedCornerShape(8.dp))
              .border(1.dp, GlassBorderStroke, RoundedCornerShape(8.dp))
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "TOTAL EVAs", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 9.sp))
              Text(text = "${filteredRecords.size}", style = MaterialTheme.typography.titleMedium.copy(color = NeonCyan, fontWeight = FontWeight.Black))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "CUMULATIVE DURATION", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 9.sp))
              Text(text = "${totalHours}h ${totalMinutes % 60}m", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Black))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "AVG COSMIC DOSE", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 9.sp))
              Text(text = String.format("%.2f mSv", meanDose), style = MaterialTheme.typography.titleMedium.copy(color = CelestialAmber, fontWeight = FontWeight.Black))
            }
          }
        }
      }
    }

    // Filter Chips
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        listOf("ALL", "USA", "RUSSIA", "ISS", "LUNAR").forEach { filter ->
          item {
            FilterChip(
              selected = selectedFilter == filter,
              onClick = { selectedFilter = filter },
              label = {
                Text(
                  text = filter,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp
                  )
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                selectedLabelColor = NeonCyan,
                containerColor = Color(0xFF0F1B2E),
                labelColor = Color.LightGray
              ),
              border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selectedFilter == filter,
                borderColor = if (selectedFilter == filter) NeonCyan else GlassBorderStroke
              )
            )
          }
        }
      }
    }

    // EVA Record Cards
    items(filteredRecords) { record ->
      EvaRecordCard(record = record)
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun EvaRecordCard(record: NasaEvaRecord) {
  GlassCard(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f).padding(end = 8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(NeonCyan.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
              text = "EVA #${record.evaNumber} • ${record.country.uppercase()}",
              style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "${record.vehicle} • ${record.date}",
              style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 9.5.sp),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0E2238))
            .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = record.formattedDuration,
            style = MaterialTheme.typography.labelSmall.copy(
              color = NeonCyan,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              fontSize = 10.5.sp
            ),
            maxLines = 1,
            softWrap = false
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = record.purpose,
        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFD6E3F8), fontSize = 11.5.sp)
      )

      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Crew: ${record.crewMembers.joinToString(", ")}",
          style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray, fontSize = 10.sp),
          modifier = Modifier.weight(1f)
        )
        Text(
          text = "Dose: ~${record.radiationDoseEstimateMsv} mSv",
          style = MaterialTheme.typography.labelSmall.copy(
            color = CelestialAmber,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
          )
        )
      }
    }
  }
}

// =========================================================================
// 2. DONKI SPACE WEATHER TAB CONTENT (api.nasa.gov/DONKI)
// =========================================================================
@Composable
private fun DonkiSpaceWeatherContent(
  alerts: List<NasaDonkiNotification>,
  onApplyToCabin: (NasaDonkiNotification) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.WbSunny, contentDescription = null, tint = CelestialAmber, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "NASA SPACE WEATHER DATABASE (DONKI)",
                style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
              )
              Text(
                text = "SWRC / API.NASA.GOV • REAL-TIME IONIZING RADIATION HAZARD FEED",
                style = MaterialTheme.typography.labelSmall.copy(color = CelestialAmber, fontSize = 9.5.sp)
              )
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "DONKI provides programmatic notifications of Solar Energetic Particle (SEP) events, Coronal Mass Ejections (CME), and Solar Flares (FLR) that affect astronaut biology and deep-space mission safety.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 11.5.sp)
          )
        }
      }
    }

    items(alerts) { alert ->
      DonkiAlertCard(alert = alert, onApply = { onApplyToCabin(alert) })
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun DonkiAlertCard(
  alert: NasaDonkiNotification,
  onApply: () -> Unit
) {
  val accentColor = when (alert.radiationRiskLevel) {
    RiskLevel.HIGH -> CriticalRed
    RiskLevel.MODERATE -> WarningAmber
    RiskLevel.LOW -> NominalGreen
  }

  GlassCard(
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f).padding(end = 8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(accentColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (alert.radiationRiskLevel == RiskLevel.HIGH) Icons.Default.Warning else Icons.Default.Bolt,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(16.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
              text = alert.solarHazardClass.uppercase(),
              style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "TYPE: ${alert.messageType} • ${alert.issueTime.replace("T", " ").replace("Z", " UTC")}",
              style = MaterialTheme.typography.labelSmall.copy(color = accentColor, fontSize = 9.sp),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(accentColor.copy(alpha = 0.15f))
            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = "${alert.peakFluxRate} mSv/h",
            style = MaterialTheme.typography.labelSmall.copy(
              color = accentColor,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp
            ),
            maxLines = 1,
            softWrap = false
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = alert.summary,
        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFD6E3F8), fontSize = 11.5.sp)
      )

      Spacer(modifier = Modifier.height(8.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFF06101E), RoundedCornerShape(6.dp))
          .border(1.dp, GlassBorderStroke, RoundedCornerShape(6.dp))
          .padding(8.dp)
      ) {
        Text(
          text = "ASTRONAUT DIRECTIVE: ${alert.recommendedAction}",
          style = MaterialTheme.typography.labelSmall.copy(
            color = if (alert.radiationRiskLevel == RiskLevel.HIGH) CriticalRed else CelestialAmber,
            fontWeight = FontWeight.Bold,
            fontSize = 10.5.sp
          )
        )
      }

      Spacer(modifier = Modifier.height(10.dp))
      Button(
        onClick = onApply,
        colors = ButtonDefaults.buttonColors(
          containerColor = if (alert.radiationRiskLevel == RiskLevel.HIGH) CriticalRed else NeonCyan
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
        modifier = Modifier.fillMaxWidth().height(36.dp).testTag("apply_space_weather_${alert.messageId}")
      ) {
        Icon(Icons.Default.Shield, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "SYNC CABIN SENSORS & MITIGATE",
          style = MaterialTheme.typography.labelSmall.copy(
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 9.5.sp,
            letterSpacing = 0.3.sp
          ),
          maxLines = 1,
          softWrap = false
        )
      }
    }
  }
}

// =========================================================================
// 3. NASA HRP 5 HAZARDS TAB CONTENT (NASA Human Research Program)
// =========================================================================
@Composable
private fun HrpHazardsContent(hazards: List<NasaHrpHazard>) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = BrightTeal, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "NASA HUMAN RESEARCH PROGRAM (HRP)",
                style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
              )
              Text(
                text = "THE 5 HAZARDS OF HUMAN SPACEFLIGHT MATRIX & EVIDENCE BASE",
                style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 9.5.sp)
              )
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "NASA's Human Research Roadmap characterizes how prolonged exploration beyond Low Earth Orbit impacts human physiology. These 5 hazards guide onboard autonomous assessments in AstraSense.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 11.5.sp)
          )
        }
      }
    }

    items(hazards) { hazard ->
      HrpHazardCard(hazard = hazard)
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun HrpHazardCard(hazard: NasaHrpHazard) {
  var isExpanded by remember { mutableStateOf(false) }

  GlassCard(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { isExpanded = !isExpanded }
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(NeonCyan.copy(alpha = 0.2f))
              .border(1.dp, NeonCyan, RoundedCornerShape(6.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = hazard.acronym,
              style = MaterialTheme.typography.labelSmall.copy(
                color = NeonCyan,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
              )
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = hazard.hazardName.uppercase(),
              style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
            Text(
              text = hazard.deepSpaceMultiplier,
              style = MaterialTheme.typography.labelSmall.copy(color = CelestialAmber, fontSize = 9.5.sp)
            )
          }
        }

        Text(
          text = if (isExpanded) "▲ COLLAPSE" else "▼ DETAILS",
          style = MaterialTheme.typography.labelSmall.copy(
            color = BrightTeal,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
          )
        )
      }

      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = hazard.description,
        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFD6E3F8), fontSize = 11.5.sp)
      )

      AnimatedVisibility(visible = isExpanded) {
        Column(modifier = Modifier.padding(top = 10.dp)) {
          Text(
            text = "PRIMARY CLINICAL RISKS:",
            style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 10.sp)
          )
          hazard.primaryPhysiologicalRisks.forEach { risk ->
            Text(
              text = "• $risk",
              style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 11.sp),
              modifier = Modifier.padding(start = 6.dp, top = 2.dp)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "CORE BIOMARKERS MONITORED:",
            style = MaterialTheme.typography.labelSmall.copy(color = CelestialAmber, fontWeight = FontWeight.Bold, fontSize = 10.sp)
          )
          hazard.primaryBiomarkers.forEach { biomarker ->
            Text(
              text = "• $biomarker",
              style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 11.sp),
              modifier = Modifier.padding(start = 6.dp, top = 2.dp)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF091424), RoundedCornerShape(6.dp))
              .border(1.dp, GlassBorderStroke, RoundedCornerShape(6.dp))
              .padding(8.dp)
          ) {
            Column {
              Text(
                text = "NASA COUNTERMEASURE PROTOCOL:",
                style = MaterialTheme.typography.labelSmall.copy(color = NominalGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp)
              )
              Text(
                text = hazard.nasaCountermeasureProtocol,
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontSize = 11.sp)
              )
            }
          }
        }
      }
    }
  }
}

// =========================================================================
// 4. DATASET SEARCH TAB CONTENT (data.nasa.gov/api/3/action/package_search)
// =========================================================================
@Composable
private fun DatasetSearchContent(
  searchResults: List<NasaDatasetPackage>,
  isSearching: Boolean,
  onSearch: (String) -> Unit
) {
  var searchQuery by remember { mutableStateOf("radiation") }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "LIVE QUERY AGAINST DATA.NASA.GOV",
            style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
          )
          Text(
            text = "CKAN API (package_search) • ACCESS OVER 30,000 OPEN NASA SCIENTIFIC DATASETS",
            style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 9.5.sp)
          )
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              placeholder = { Text("Search data.nasa.gov (e.g. radiation, astronaut, iss)", fontSize = 11.sp) },
              modifier = Modifier.weight(1f).testTag("nasa_search_query_input"),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = GlassBorderStroke,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray
              )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = { onSearch(searchQuery) },
              enabled = !isSearching,
              colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(50.dp).testTag("nasa_search_submit_button")
            ) {
              if (isSearching) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
              } else {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Black)
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "QUICK SEARCH QUERIES:",
            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 9.sp)
          )
          Spacer(modifier = Modifier.height(4.dp))
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("radiation", "astronaut", "eva", "iss-biomedical", "nutrition", "cardiovascular").forEach { tag ->
              item {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0F1B2E))
                    .border(1.dp, GlassBorderStroke, RoundedCornerShape(4.dp))
                    .clickable {
                      searchQuery = tag
                      onSearch(tag)
                    }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                  Text(text = tag, style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 10.sp))
                }
              }
            }
          }
        }
      }
    }

    if (searchResults.isEmpty() && !isSearching) {
      item {
        Box(
          modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Type a search query above to query data.nasa.gov datasets.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 12.sp)
          )
        }
      }
    } else {
      items(searchResults) { pkg ->
        NasaDatasetCard(pkg = pkg)
      }
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun NasaDatasetCard(pkg: NasaDatasetPackage) {
  GlassCard(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = pkg.title,
          style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold),
          modifier = Modifier.weight(1f)
        )
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(NeonCyan.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "NASA OPEN DATA",
            style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = pkg.notes,
        style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 11.5.sp),
        maxLines = 4
      )

      if (pkg.tags.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          pkg.tags.take(4).forEach { tag ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF0C1929))
                .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
              Text(text = "#$tag", style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 9.sp))
            }
          }
        }
      }

      if (pkg.resources.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Resources: ${pkg.resources.joinToString(", ") { it.format }}",
            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 10.sp)
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${pkg.resources.size} files available",
              style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            )
          }
        }
      }
    }
  }
}
