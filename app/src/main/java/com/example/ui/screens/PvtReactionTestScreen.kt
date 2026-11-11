package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AstraRepository
import com.example.model.CrewMember
import com.example.model.PvtTestResult
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.StarlightBlue
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.random.Random

enum class PvtState {
  IDLE,
  WAITING_FOR_STIMULUS,
  STIMULUS_ACTIVE,
  FALSE_START,
  COMPLETED
}

@Composable
fun PvtReactionTestScreen(
  crewMember: CrewMember,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onNavigateBack() }

  var pvtState by remember { mutableStateOf(PvtState.IDLE) }
  var stimulusAppearanceTime by remember { mutableLongStateOf(0L) }
  val trialReactionTimes = remember { mutableStateListOf<Int>() }
  var currentTrialNumber by remember { mutableIntStateOf(0) }
  val targetTrials = 5 // 5 quick precision trials for ~30s test duration

  var meanReactionTime by remember { mutableIntStateOf(0) }
  var lapsesCount by remember { mutableIntStateOf(0) }
  var alertnessScore by remember { mutableIntStateOf(0) }
  var readinessStatus by remember { mutableStateOf("CALCULATING...") }

  // Scheduled stimulus trigger
  LaunchedEffect(pvtState, currentTrialNumber) {
    if (pvtState == PvtState.WAITING_FOR_STIMULUS) {
      val randomDelayMs = Random.nextLong(1500, 3800)
      delay(randomDelayMs)
      if (pvtState == PvtState.WAITING_FOR_STIMULUS) {
        stimulusAppearanceTime = System.currentTimeMillis()
        pvtState = PvtState.STIMULUS_ACTIVE
      }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Top Bar with Back Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f, fill = false).padding(end = 6.dp)
        ) {
          IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = NeonCyan
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
              "PVT REACTION TEST",
              style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              "NASA 30-Second Cognitive Readiness",
              style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 8.5.sp),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        StatusBadge(text = "30s TEST", color = NeonCyan)
      }

      // Test Guidelines Card
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
          Text(
            text = "ASTRONAUT: ${crewMember.fullName} (${crewMember.id})",
            style = MaterialTheme.typography.labelMedium.copy(color = BrightTeal)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "When the stimulus turns RED, tap the response pad as fast as possible. Lapses (>500ms) or false starts indicate acute neuro-cognitive fatigue.",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFCBD5E1))
          )
        }
      }

      // Main Test Interactive Area
      GlassCard(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .testTag("pvt_interactive_area"),
        cornerRadius = 24.dp
      ) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          when (pvtState) {
            PvtState.IDLE -> {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                  modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.15f))
                    .border(2.dp, NeonCyan, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(46.dp)
                  )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                  "READY TO BEGIN ASSESSMENT",
                  style = MaterialTheme.typography.titleLarge.copy(color = Color.White)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  "5 Sequential Trials • Reaction in Milliseconds",
                  style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                  onClick = {
                    trialReactionTimes.clear()
                    currentTrialNumber = 1
                    pvtState = PvtState.WAITING_FOR_STIMULUS
                  },
                  modifier = Modifier.height(48.dp).testTag("start_pvt_button"),
                  colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    "START 30s PVT TEST",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                  )
                }
              }
            }

            PvtState.WAITING_FOR_STIMULUS -> {
              // Astronaut waiting, must not tap yet
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .clip(RoundedCornerShape(20.dp))
                  .background(Color(0xFF030712))
                  .clickable {
                    // False start tapped too early!
                    pvtState = PvtState.FALSE_START
                  },
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    "TRIAL $currentTrialNumber OF $targetTrials",
                    style = MaterialTheme.typography.labelMedium.copy(color = NeonCyan)
                  )
                  Spacer(modifier = Modifier.height(14.dp))
                  Box(
                    modifier = Modifier
                      .size(40.dp)
                      .clip(CircleShape)
                      .background(StarlightBlue.copy(alpha = 0.2f))
                      .border(1.dp, StarlightBlue, CircleShape)
                  )
                  Spacer(modifier = Modifier.height(14.dp))
                  Text(
                    "WAIT FOR RED STIMULUS...",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.LightGray)
                  )
                  Text(
                    "(Do not tap yet)",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                  )
                }
              }
            }

            PvtState.STIMULUS_ACTIVE -> {
              // Active red flash: TAP NOW!
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .clip(RoundedCornerShape(20.dp))
                  .background(CriticalRed)
                  .clickable {
                    val reaction = (System.currentTimeMillis() - stimulusAppearanceTime).toInt()
                    trialReactionTimes.add(reaction)

                    if (currentTrialNumber < targetTrials) {
                      currentTrialNumber++
                      pvtState = PvtState.WAITING_FOR_STIMULUS
                    } else {
                      // Final calculation
                      val mean = trialReactionTimes.average().toInt()
                      meanReactionTime = mean
                      lapsesCount = trialReactionTimes.count { it > 500 }
                      alertnessScore = ((600 - mean).toDouble() / 4.0).toInt().coerceIn(10, 100)
                      readinessStatus = when {
                        lapsesCount >= 2 || mean > 380 -> "REST RECOMMENDED • FATIGUE HIGH"
                        mean > 300 -> "MILD FATIGUE • MONITORED DUTY"
                        else -> "FULL FLIGHT DUTY • OPTIMAL"
                      }

                      // Save to repository
                      val res = PvtTestResult(
                        testId = UUID.randomUUID().toString(),
                        astronautId = crewMember.id,
                        timestamp = System.currentTimeMillis(),
                        meanReactionTimeMs = mean,
                        fastestReactionMs = trialReactionTimes.minOrNull() ?: mean,
                        slowestReactionMs = trialReactionTimes.maxOrNull() ?: mean,
                        lapsesCount = lapsesCount,
                        totalTrials = targetTrials,
                        alertnessScore = alertnessScore,
                        readinessStatus = readinessStatus
                      )
                      AstraRepository.recordPvtResult(res)
                      pvtState = PvtState.COMPLETED
                    }
                  }
                  .testTag("pvt_stimulus_active_pad"),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    "TAP NOW!",
                    style = MaterialTheme.typography.displayLarge.copy(
                      color = Color.White,
                      fontWeight = androidx.compose.ui.text.font.FontWeight.Black
                    )
                  )
                  Spacer(modifier = Modifier.height(10.dp))
                  Text(
                    "MILLISECOND TIMING ACTIVE",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
                  )
                }
              }
            }

            PvtState.FALSE_START -> {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(54.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("FALSE START DETECTED", style = MaterialTheme.typography.titleLarge.copy(color = WarningAmber))
                Text("You tapped before the stimulus appeared.", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                  onClick = { pvtState = PvtState.WAITING_FOR_STIMULUS },
                  colors = ButtonDefaults.buttonColors(containerColor = BrightTeal, contentColor = Color.Black)
                ) {
                  Text("RETRY TRIAL $currentTrialNumber")
                }
              }
            }

            PvtState.COMPLETED -> {
              Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NominalGreen, modifier = Modifier.size(50.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  "PVT COGNITIVE ASSESSMENT COMPLETE",
                  style = MaterialTheme.typography.titleLarge.copy(color = Color.White)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Readiness Status Badge
                StatusBadge(
                  text = readinessStatus,
                  color = if (lapsesCount >= 2) WarningAmber else NominalGreen
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Result Summary Grid
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha = 0.4f)).padding(10.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Text("MEAN REACTION", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                      Text("$meanReactionTime ms", style = MaterialTheme.typography.titleLarge.copy(color = NeonCyan))
                    }
                  }

                  Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha = 0.4f)).padding(10.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Text("ALERTNESS INDEX", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                      Text("$alertnessScore/100", style = MaterialTheme.typography.titleLarge.copy(color = NominalGreen))
                    }
                  }

                  Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha = 0.4f)).padding(10.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Text("LAPSES (>500ms)", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                      Text("$lapsesCount", style = MaterialTheme.typography.titleLarge.copy(color = if (lapsesCount > 0) WarningAmber else Color.White))
                    }
                  }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Button(
                    onClick = {
                      trialReactionTimes.clear()
                      currentTrialNumber = 1
                      pvtState = PvtState.WAITING_FOR_STIMULUS
                    },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderStroke)
                  ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RE-TEST")
                  }

                  Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
                  ) {
                    Text("RETURN TO PORTAL")
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
