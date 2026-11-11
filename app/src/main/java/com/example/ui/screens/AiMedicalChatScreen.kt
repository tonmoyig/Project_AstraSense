package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.MedicalChatMessage
import com.example.ai.MedicalLiteratureReference
import com.example.ai.SpaceAiAssistant
import com.example.data.AstraRepository
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.StarlightBlue
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun AiMedicalChatScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onNavigateBack() }

  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val listState = rememberLazyListState()

  val currentUser by AstraRepository.currentUser.collectAsState()
  val destination by AstraRepository.currentDestination.collectAsState()
  val currentAstronautId = currentUser?.id ?: "UNKNOWN-ASTRONAUT"

  var inputQuery by remember { mutableStateOf("") }
  var isThinkingMode by remember { mutableStateOf(false) }
  var isLowLatencyMode by remember { mutableStateOf(false) }
  var isGenerating by remember { mutableStateOf(false) }
  var showStandardsDialog by remember { mutableStateOf(false) }
  var showClearConfirmDialog by remember { mutableStateOf(false) }
  var isHandsFreeVoiceActive by remember { mutableStateOf(false) }
  var playingAudioMessageId by remember { mutableStateOf<String?>(null) }

  // Load persistent in-memory session history for current astronaut
  val messages = remember(currentAstronautId) {
    mutableStateListOf<MedicalChatMessage>().apply {
      addAll(SpaceAiAssistant.getSessionHistory(currentAstronautId))
    }
  }

  // Pre-configured tactical spaceflight emergency drills
  val emergencyTacticalChips = listOf(
    Pair("PRESSURE LOSS", "EMERGENCY: Cabin Depressurization & DCS Pre-Breathe Directive"),
    Pair("SOLAR SPE STORM", "EMERGENCY: Solar Particle Event / Radiation Shelter Protocol"),
    Pair("CO₂ TOXIC SPIKE", "EMERGENCY: High CO2 Hypercapnia & Scrubbing Countermeasures"),
    Pair("CARDIAC CODE", "EMERGENCY: Microgravity Cardiac Arrest / Resuscitation Code Blue"),
    Pair("SUIT PUNCTURE", "EMERGENCY: EVA Spacesuit Puncture & Airlock Abort"),
    Pair("HAB FIRE / SMOKE", "EMERGENCY: Hab Fire Suppression & Toxic Smoke Inhalation"),
    Pair("AMMONIA BREACH", "EMERGENCY: Ammonia Coolant Loop Rupture & EEBA Donning"),
    Pair("SMS SICKNESS", "Space Motion Sickness Antiemetic Titration Protocol"),
    Pair("SANS VISION", "Microgravity SANS (Visual Impairment Assessment)"),
    Pair("PVT FATIGUE", "Circadian Desynchronosis & PVT Reaction Time Lag")
  )

  fun submitQuery(queryText: String) {
    if (queryText.isBlank() || isGenerating) return
    val cleanQuery = queryText.trim()
    val now = SimpleDateFormat("HH:mm", Locale.US).format(Date())

    val userMsg = MedicalChatMessage(
      id = UUID.randomUUID().toString(),
      sender = "ASTRONAUT",
      text = cleanQuery,
      timestamp = now
    )
    messages.add(userMsg)
    inputQuery = ""
    isGenerating = true

    val aiMsgId = UUID.randomUUID().toString()
    val modelName = when {
      isThinkingMode -> "Gemini 4 Argon (Thinking)"
      isLowLatencyMode -> "Gemini 3.5 Flash-Lite"
      else -> "Gemini 3.8 Flash"
    }

    val placeholderAiMsg = MedicalChatMessage(
      id = aiMsgId,
      sender = "ASTRA-AI",
      text = "Analyzing astronaut telemetry and querying NASA-STD-3001 clinical archives...",
      timestamp = now,
      sourceModel = modelName,
      emergencyLevel = "ANALYZING"
    )
    messages.add(placeholderAiMsg)

    scope.launch {
      listState.animateScrollToItem(messages.size - 1)
      var finalAiText = ""
      var finalRefs = emptyList<MedicalLiteratureReference>()

      try {
        SpaceAiAssistant.streamMedicalGuidance(
          prompt = cleanQuery,
          astronautId = currentAstronautId,
          isThinkingMode = isThinkingMode,
          isLowLatency = isLowLatencyMode
        ).collect { (accumulated, parsedRefs) ->
          finalAiText = accumulated
          finalRefs = parsedRefs
          val index = messages.indexOfFirst { it.id == aiMsgId }
          if (index != -1) {
            val liveChecklist = SpaceAiAssistant.parseEmergencyChecklist(accumulated)
            val isEmerg = cleanQuery.contains("emergency", ignoreCase = true) ||
              cleanQuery.contains("protocol", ignoreCase = true) ||
              cleanQuery.contains("code", ignoreCase = true) ||
              cleanQuery.contains("fire", ignoreCase = true) ||
              cleanQuery.contains("depress", ignoreCase = true) ||
              cleanQuery.contains("ammonia", ignoreCase = true) ||
              cleanQuery.contains("suit", ignoreCase = true) ||
              liveChecklist.isNotEmpty()

            messages[index] = placeholderAiMsg.copy(
              text = accumulated,
              references = parsedRefs,
              emergencyLevel = if (isEmerg) "EMERGENCY PROTOCOL" else "CLINICAL ADVISORY",
              checklist = liveChecklist,
              isEmergency = isEmerg
            )
          }
        }
      } finally {
        isGenerating = false
        // Persist to session memory in SpaceAiAssistant
        if (finalAiText.isNotBlank()) {
          SpaceAiAssistant.appendMessages(
            astronautId = currentAstronautId,
            userText = cleanQuery,
            aiText = finalAiText,
            references = finalRefs,
            sourceModel = modelName
          )
        }
      }
    }
  }

  fun toggleChecklistStep(messageId: String, stepIndex: Int) {
    val updated = SpaceAiAssistant.toggleChecklistItem(currentAstronautId, messageId, stepIndex)
    val index = messages.indexOfFirst { it.id == messageId }
    if (index != -1) {
      if (updated != null) {
        messages[index] = updated
      } else {
        val current = messages[index]
        val set = current.completedChecklistIndices.toMutableSet()
        if (set.contains(stepIndex)) set.remove(stepIndex) else set.add(stepIndex)
        messages[index] = current.copy(completedChecklistIndices = set)
      }
    }
  }

  fun copyConsultToClipboard() {
    val consultSummary = buildString {
      appendLine("=== ASTRASENSE EMERGENCY MEDICAL & FLIGHT LOG ===")
      appendLine("Patient: ${currentUser?.fullName ?: "Astronaut"} (${currentAstronautId})")
      appendLine("Mission: ${destination.displayName} (Comms Delay: ${destination.commsDelaySeconds}s)")
      appendLine("Timestamp: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).format(Date())}")
      appendLine("---")
      messages.forEach { msg ->
        appendLine("[${msg.timestamp}] ${msg.sender} (${msg.emergencyLevel ?: "NOMINAL"}):")
        appendLine(msg.text)
        if (msg.checklist.isNotEmpty()) {
          appendLine("Emergency Checklist Status:")
          msg.checklist.forEachIndexed { i, step ->
            val done = if (msg.completedChecklistIndices.contains(i)) "[X]" else "[ ]"
            appendLine(" $done $step")
          }
        }
        if (msg.references.isNotEmpty()) {
          appendLine("Cited Flight Rules & Standards:")
          msg.references.forEach { ref ->
            appendLine(" • [${ref.documentId}] ${ref.titleOrSource} (${ref.protocolLevel})")
          }
        }
        appendLine()
      }
    }

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("AstraSense Emergency Medical Log", consultSummary)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Emergency log copied for Houston downlink", Toast.LENGTH_SHORT).show()
  }

  fun syncLiveBiometrics() {
    val snapshot = SpaceAiAssistant.captureCurrentSnapshot()
    val syncMsg = SpaceAiAssistant.recordBiometricsSync(currentAstronautId, snapshot)
    messages.add(syncMsg)
    scope.launch {
      listState.animateScrollToItem(messages.size - 1)
    }
    Toast.makeText(context, "Latest telemetry synced to AI session", Toast.LENGTH_SHORT).show()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // 1. Top Navigation Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.size(36.dp).testTag("ai_chat_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = NeonCyan
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                "EMERGENCY AI CO-PILOT",
                style = MaterialTheme.typography.titleMedium.copy(
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
              )
              Spacer(modifier = Modifier.width(6.dp))
              Icon(Icons.Default.Lock, contentDescription = null, tint = NominalGreen, modifier = Modifier.size(12.dp))
            }
            Text(
              "NASA-STD-3001 • DEEP SPACE AUTONOMOUS TRIAGE",
              style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 8.sp)
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
          // Reference Library Button
          IconButton(
            onClick = { showStandardsDialog = true },
            modifier = Modifier.size(36.dp).testTag("nasa_reference_library_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "NASA Standards", tint = NeonCyan, modifier = Modifier.size(18.dp))
          }

          // Export Consult Button
          IconButton(
            onClick = { copyConsultToClipboard() },
            modifier = Modifier.size(36.dp).testTag("export_consult_button")
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Export Consult", tint = BrightTeal, modifier = Modifier.size(18.dp))
          }

          // Clear History Button
          IconButton(
            onClick = { showClearConfirmDialog = true },
            modifier = Modifier.size(36.dp).testTag("clear_history_button")
          ) {
            Icon(Icons.Default.Delete, contentDescription = "Clear History", tint = Color.Gray, modifier = Modifier.size(18.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // 2. Active Astronaut Session Telemetry Banner
      val snapshot = remember(currentUser) { SpaceAiAssistant.captureCurrentSnapshot() }
      val activeHazards = remember(snapshot) { SpaceAiAssistant.detectActiveHazards(snapshot) }

      // 2.1 Critical Real-Time Hazard Alert Banner (if any acute hazard detected)
      if (activeHazards.isNotEmpty()) {
        GlassCard(
          modifier = Modifier.fillMaxWidth().testTag("active_hazards_banner"),
          cornerRadius = 12.dp,
          borderColor = CriticalRed
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(Icons.Default.Warning, contentDescription = null, tint = CriticalRed, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text(
                  "ACTIVE SPACECRAFT HAZARD DETECTED",
                  style = MaterialTheme.typography.labelSmall.copy(color = CriticalRed, fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
                )
                Text(
                  activeHazards.first(),
                  style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
                  maxLines = 1
                )
              }
            }
            Button(
              onClick = { submitQuery("EMERGENCY PROTOCOL: ${activeHazards.first()}") },
              colors = ButtonDefaults.buttonColors(containerColor = CriticalRed, contentColor = Color.White),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(30.dp).testTag("resolve_hazard_button")
            ) {
              Text("EXECUTE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold))
            }
          }
        }
        Spacer(modifier = Modifier.height(6.dp))
      }

      // 2.2 Telemetry Summary Card
      GlassCard(
        modifier = Modifier.fillMaxWidth().testTag("active_session_banner"),
        cornerRadius = 12.dp,
        borderColor = NeonCyan.copy(alpha = 0.35f)
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(if (activeHazards.isNotEmpty()) CriticalRed else NominalGreen)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "CREW: ${snapshot.fullName.uppercase()} (${snapshot.astronautId})",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 9.sp
                )
              )
            }

            // Sync Biometrics Action
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(BrightTeal.copy(alpha = 0.2f))
                .clickable { syncLiveBiometrics() }
                .padding(horizontal = 6.dp, vertical = 2.dp)
                .testTag("sync_biometrics_button")
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = BrightTeal, modifier = Modifier.size(10.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("SYNC TELEMETRY", style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 8.sp))
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Telemetry Pills Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            TelemetryPill("HR", "${snapshot.heartRate} bpm", if (snapshot.heartRate > 120 || snapshot.heartRate < 50) CriticalRed else BrightTeal)
            TelemetryPill("BP", snapshot.bloodPressure, Color(0xFFBAE6FD))
            TelemetryPill("SpO₂", "${snapshot.spO2}%", if (snapshot.spO2 < 92) CriticalRed else NominalGreen)
            TelemetryPill("Rad", "${snapshot.radiationDailyMsv} mSv/d", if (snapshot.radiationDailyMsv > 0.8) WarningAmber else NeonCyan)
            TelemetryPill("Cabin Press", "${snapshot.cabinPressureKpa} kPa", if (snapshot.cabinPressureKpa < 95.0) CriticalRed else Color.White)
            TelemetryPill("Cabin CO₂", "${snapshot.cabinCo2Ppm} ppm", if (snapshot.cabinCo2Ppm > 3000) WarningAmber else Color.White)
            TelemetryPill("Comms Delay", "${snapshot.commsDelaySeconds}s", StarlightBlue)
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // 3. Reasoning Mode Selector Chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        ReasoningChip(
          title = "GEMINI 3.8 FLASH",
          icon = Icons.Default.AutoAwesome,
          isSelected = !isThinkingMode && !isLowLatencyMode,
          accentColor = NeonCyan,
          onClick = {
            isThinkingMode = false
            isLowLatencyMode = false
          },
          modifier = Modifier.weight(1f).testTag("mode_flash_chip")
        )

        ReasoningChip(
          title = "ARGON THINKING",
          icon = Icons.Default.Psychology,
          isSelected = isThinkingMode,
          accentColor = BrightTeal,
          onClick = {
            isThinkingMode = true
            isLowLatencyMode = false
          },
          modifier = Modifier.weight(1f).testTag("mode_thinking_chip")
        )

        ReasoningChip(
          title = "FLASH-LITE",
          icon = Icons.Default.Bolt,
          isSelected = isLowLatencyMode,
          accentColor = WarningAmber,
          onClick = {
            isLowLatencyMode = true
            isThinkingMode = false
          },
          modifier = Modifier.weight(1f).testTag("mode_lite_chip")
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // 4. Tactical Emergency Chips Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        emergencyTacticalChips.forEach { (label, fullQuery) ->
          val isCritical = label.contains("PRESSURE") || label.contains("SOLAR") || label.contains("CARDIAC") || label.contains("FIRE") || label.contains("SUIT") || label.contains("AMMONIA")
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (isCritical) CriticalRed.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.4f))
              .border(1.dp, if (isCritical) CriticalRed.copy(alpha = 0.8f) else GlassBorderStroke, RoundedCornerShape(8.dp))
              .clickable { submitQuery(fullQuery) }
              .padding(horizontal = 8.dp, vertical = 5.dp)
              .testTag("tactical_chip_${label.replace(" ", "_")}")
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              if (isCritical) {
                Icon(Icons.Default.Emergency, contentDescription = null, tint = CriticalRed, modifier = Modifier.size(11.dp))
                Spacer(modifier = Modifier.width(4.dp))
              }
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (isCritical) Color.White else Color(0xFFBAE6FD),
                  fontWeight = if (isCritical) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 8.5.sp
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 5. Multi-turn Messages List with Interactive Emergency Checklists
      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(messages, key = { it.id }) { msg ->
          MessageBubble(
            msg = msg,
            isPlayingAudio = playingAudioMessageId == msg.id,
            onToggleAudioPlayback = {
              playingAudioMessageId = if (playingAudioMessageId == msg.id) null else msg.id
            },
            onToggleChecklistStep = { stepIdx ->
              toggleChecklistStep(msg.id, stepIdx)
            }
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // 6. Zero-G Hands-Free Audio Voice Panel (if active)
      AnimatedVisibility(visible = isHandsFreeVoiceActive) {
        GlassCard(
          modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp).testTag("hands_free_voice_panel"),
          cornerRadius = 12.dp,
          borderColor = NeonCyan
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  "ZERO-G HANDS-FREE VOICE ASSISTANT ACTIVE",
                  style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                )
              }
              VoiceWaveformAnimation()
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
              "Voice command simulation: Tap spoken command to execute hands-free:",
              style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray, fontSize = 8.5.sp)
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              listOf(
                "Check cabin integrity and pressure status",
                "Execute Code Blue cardiac resuscitation steps",
                "Hab fire detected in Node 2: initiate suppression",
                "Report SPE solar radiation shelter status"
              ).forEach { cmd ->
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(BrightTeal.copy(alpha = 0.2f))
                    .border(1.dp, BrightTeal.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .clickable { submitQuery(cmd) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(cmd, style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 8.5.sp))
                }
              }
            }
          }
        }
      }

      // 7. Query Input Bar with Mic & Send
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // Hands-free voice mic toggle
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isHandsFreeVoiceActive) NeonCyan else Color.White.copy(alpha = 0.08f))
            .clickable { isHandsFreeVoiceActive = !isHandsFreeVoiceActive }
            .testTag("hands_free_mic_toggle"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isHandsFreeVoiceActive) Icons.Default.Mic else Icons.Default.MicOff,
            contentDescription = "Hands-free voice mode",
            tint = if (isHandsFreeVoiceActive) Color.Black else NeonCyan,
            modifier = Modifier.size(20.dp)
          )
        }

        OutlinedTextField(
          value = inputQuery,
          onValueChange = { inputQuery = it },
          placeholder = {
            Text(
              "Emergency query: depressurization, cardiac code, fire, SPE shelter...",
              color = Color.Gray,
              fontSize = 10.sp
            )
          },
          modifier = Modifier
            .weight(1f)
            .testTag("ai_medical_query_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = GlassBorderStroke,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          shape = RoundedCornerShape(12.dp)
        )

        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isGenerating) Color.Gray else NeonCyan)
            .clickable(enabled = !isGenerating && inputQuery.isNotBlank()) {
              submitQuery(inputQuery)
            }
            .testTag("ai_send_query_button"),
          contentAlignment = Alignment.Center
        ) {
          if (isGenerating) {
            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
          } else {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(18.dp))
          }
        }
      }
    }
  }

  // NASA Medical Standards Reference Dialog
  if (showStandardsDialog) {
    AlertDialog(
      onDismissRequest = { showStandardsDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("NASA Aerospace Medical Standards", color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
      },
      text = {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          items(SpaceAiAssistant.getOfficialMedicalStandards()) { standard ->
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .border(1.dp, GlassBorderStroke, RoundedCornerShape(8.dp))
                .padding(10.dp)
            ) {
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(standard.documentId, style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontWeight = FontWeight.Bold))
                  StatusBadge(text = standard.protocolLevel, color = if (standard.protocolLevel.contains("RULE")) CriticalRed else NeonCyan)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(standard.titleOrSource, style = MaterialTheme.typography.titleSmall.copy(color = Color.White))
                Spacer(modifier = Modifier.height(2.dp))
                Text(standard.guidelineSummary, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1), fontSize = 11.sp))
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { showStandardsDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
        ) {
          Text("CLOSE")
        }
      },
      containerColor = Color(0xFF0A1222),
      shape = RoundedCornerShape(16.dp)
    )
  }

  // Clear Session History Confirmation Dialog
  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      title = { Text("Reset Emergency AI Session?", color = Color.White) },
      text = {
        Text(
          "This will reset the conversational history for astronaut ${currentUser?.fullName ?: "Crew Member"}. Telemetry baseline and flight rules remain active.",
          color = Color(0xFFCBD5E1)
        )
      },
      confirmButton = {
        Button(
          onClick = {
            SpaceAiAssistant.clearSessionHistory(currentAstronautId)
            messages.clear()
            messages.addAll(SpaceAiAssistant.getSessionHistory(currentAstronautId))
            showClearConfirmDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = CriticalRed)
        ) {
          Text("RESET")
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirmDialog = false }) {
          Text("CANCEL", color = Color.LightGray)
        }
      },
      containerColor = Color(0xFF0A1222)
    )
  }
}

@Composable
private fun MessageBubble(
  msg: MedicalChatMessage,
  isPlayingAudio: Boolean,
  onToggleAudioPlayback: () -> Unit,
  onToggleChecklistStep: (Int) -> Unit
) {
  val isAi = msg.sender == "ASTRA-AI"
  val isTelemetry = msg.sender == "SYSTEM-TELEMETRY"
  val isEmergency = msg.isEmergency || (msg.emergencyLevel?.contains("CRITICAL") == true) || (msg.emergencyLevel?.contains("LIFE") == true)

  val bubbleBg = when {
    isEmergency -> Color(0xFF1A0A0E)
    isTelemetry -> Color(0xFF0F1E36)
    isAi -> Color(0xFF081226)
    else -> NeonCyan.copy(alpha = 0.18f)
  }

  val borderStroke = when {
    isEmergency -> CriticalRed.copy(alpha = 0.7f)
    isTelemetry -> BrightTeal.copy(alpha = 0.4f)
    isAi -> GlassBorderStroke
    else -> NeonCyan.copy(alpha = 0.4f)
  }

  var isRefsExpanded by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(bubbleBg)
      .border(1.dp, borderStroke, RoundedCornerShape(14.dp))
      .padding(12.dp)
      .testTag("chat_message_${msg.id}")
  ) {
    Column {
      // Message Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = when {
              isEmergency -> Icons.Default.Emergency
              isTelemetry -> Icons.Default.Speed
              isAi -> Icons.Default.AutoAwesome
              else -> Icons.Default.Shield
            },
            contentDescription = null,
            tint = when {
              isEmergency -> CriticalRed
              isTelemetry -> BrightTeal
              isAi -> NeonCyan
              else -> BrightTeal
            },
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = when {
              isTelemetry -> "BIOMETRIC SENSOR STREAM"
              isAi -> if (isEmergency) "ASTRA EMERGENCY CO-PILOT" else "ASTRA FLIGHT SURGEON AI"
              else -> "ASTRONAUT TELE-QUERY"
            },
            style = MaterialTheme.typography.labelSmall.copy(
              color = if (isEmergency) CriticalRed else if (isAi) NeonCyan else BrightTeal,
              fontWeight = FontWeight.Bold
            )
          )

          msg.emergencyLevel?.let { level ->
            Spacer(modifier = Modifier.width(6.dp))
            StatusBadge(
              text = level,
              color = when {
                level.contains("CRITICAL") || level.contains("LIFE") || level.contains("CODE") -> CriticalRed
                level.contains("ALERT") || level.contains("HAZARD") -> WarningAmber
                else -> NominalGreen
              }
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (isAi && msg.text.isNotBlank()) {
            IconButton(
              onClick = onToggleAudioPlayback,
              modifier = Modifier.size(24.dp).testTag("audio_playback_button_${msg.id}")
            ) {
              Icon(
                imageVector = if (isPlayingAudio) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Simulate Audio Playback",
                tint = if (isPlayingAudio) BrightTeal else Color.Gray,
                modifier = Modifier.size(16.dp)
              )
            }
            Spacer(modifier = Modifier.width(4.dp))
          }

          Text(
            text = msg.timestamp,
            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 9.sp)
          )
        }
      }

      // Audio Readout Indicator (if playing)
      if (isPlayingAudio) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(BrightTeal.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = BrightTeal, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              "PLAYING ONBOARD SYNTHESIZED SPEECH",
              style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            )
          }
          VoiceWaveformAnimation()
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Main Text
      Text(
        text = msg.text,
        style = MaterialTheme.typography.bodyMedium.copy(
          color = Color(0xFFF1F5F9),
          lineHeight = 20.sp
        )
      )

      // Interactive Action Checklist (if any emergency steps were extracted)
      if (msg.checklist.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))

        val completedCount = msg.completedChecklistIndices.size
        val totalCount = msg.checklist.size
        val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .border(1.dp, if (completedCount == totalCount) NominalGreen.copy(alpha = 0.6f) else WarningAmber.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(10.dp)
            .testTag("checklist_card_${msg.id}")
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (completedCount == totalCount) Icons.Default.CheckCircle else Icons.Default.Emergency,
                  contentDescription = null,
                  tint = if (completedCount == totalCount) NominalGreen else WarningAmber,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  "EMERGENCY ACTION CHECKLIST",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (completedCount == totalCount) NominalGreen else WarningAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                  )
                )
              }

              Text(
                "$completedCount OF $totalCount COMPLETED",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (completedCount == totalCount) NominalGreen else Color.LightGray,
                  fontWeight = FontWeight.Bold,
                  fontSize = 8.5.sp
                )
              )
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
              progress = { progress },
              modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
              color = if (completedCount == totalCount) NominalGreen else NeonCyan,
              trackColor = Color.White.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              msg.checklist.forEachIndexed { idx, step ->
                val isChecked = msg.completedChecklistIndices.contains(idx)
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isChecked) NominalGreen.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f))
                    .clickable { onToggleChecklistStep(idx) }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("checklist_step_${msg.id}_$idx"),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isChecked) NominalGreen else Color.Gray,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "${idx + 1}. $step",
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = if (isChecked) Color(0xFF94A3B8) else Color.White,
                      fontWeight = if (isChecked) FontWeight.Normal else FontWeight.SemiBold,
                      fontSize = 11.sp
                    )
                  )
                }
              }
            }
          }
        }
      }

      // Aerospace Medical Literature References Card (Expandable)
      if (msg.references.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .clickable { isRefsExpanded = !isRefsExpanded }
            .padding(8.dp)
            .testTag("literature_references_card_${msg.id}")
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = BrightTeal, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "AEROSPACE MEDICAL LITERATURE (${msg.references.size} CITED)",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = BrightTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                  )
                )
              }
              Icon(
                imageVector = if (isRefsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = BrightTeal,
                modifier = Modifier.size(16.dp)
              )
            }

            AnimatedVisibility(visible = isRefsExpanded) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                msg.references.forEach { ref ->
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(6.dp))
                      .background(Color(0xFF050B14))
                      .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                      .padding(8.dp)
                  ) {
                    Column {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = ref.documentId,
                          style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                          )
                        )
                        StatusBadge(
                          text = ref.protocolLevel,
                          color = if (ref.protocolLevel.contains("RULE")) CriticalRed else BrightTeal
                        )
                      }
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(
                        text = ref.titleOrSource,
                        style = MaterialTheme.typography.bodySmall.copy(
                          color = Color.White,
                          fontWeight = FontWeight.SemiBold,
                          fontSize = 11.sp
                        )
                      )
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(
                        text = ref.guidelineSummary,
                        style = MaterialTheme.typography.bodySmall.copy(
                          color = Color(0xFF94A3B8),
                          fontSize = 10.sp
                        )
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun VoiceWaveformAnimation() {
  val infiniteTransition = rememberInfiniteTransition(label = "waveform")
  val height1 by infiniteTransition.animateFloat(
    initialValue = 4f,
    targetValue = 14f,
    animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
    label = "h1"
  )
  val height2 by infiniteTransition.animateFloat(
    initialValue = 14f,
    targetValue = 6f,
    animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
    label = "h2"
  )
  val height3 by infiniteTransition.animateFloat(
    initialValue = 6f,
    targetValue = 16f,
    animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse),
    label = "h3"
  )

  Row(
    horizontalArrangement = Arrangement.spacedBy(2.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(modifier = Modifier.width(3.dp).height(height1.dp).clip(CircleShape).background(NeonCyan))
    Box(modifier = Modifier.width(3.dp).height(height2.dp).clip(CircleShape).background(BrightTeal))
    Box(modifier = Modifier.width(3.dp).height(height3.dp).clip(CircleShape).background(NeonCyan))
    Box(modifier = Modifier.width(3.dp).height(height1.dp).clip(CircleShape).background(BrightTeal))
  }
}

@Composable
private fun TelemetryPill(label: String, value: String, color: Color) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(Color.Black.copy(alpha = 0.4f))
      .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
      .padding(horizontal = 6.dp, vertical = 3.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = "$label: ",
        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp)
      )
      Text(
        text = value,
        style = MaterialTheme.typography.labelSmall.copy(
          color = color,
          fontWeight = FontWeight.Bold,
          fontSize = 8.sp
        )
      )
    }
  }
}

@Composable
private fun ReasoningChip(
  title: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isSelected: Boolean,
  accentColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (isSelected) accentColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
      .border(1.dp, if (isSelected) accentColor else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
      .clickable { onClick() }
      .padding(vertical = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(icon, contentDescription = null, tint = if (isSelected) accentColor else Color.Gray, modifier = Modifier.size(12.dp))
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
          color = if (isSelected) accentColor else Color.LightGray,
          fontSize = 8.sp,
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
      )
    }
  }
}
