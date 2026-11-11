package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.StarlightBlue
import com.example.ui.theme.WarningAmber
import kotlin.math.cos
import kotlin.math.sin

data class SpacecraftModuleInfo(
  val id: Int,
  val name: String,
  val status: String,
  val details: String,
  val healthColor: Color
)

@Composable
fun Spacecraft3DCanvas(
  modifier: Modifier = Modifier,
  isStormShieldEngaged: Boolean = false,
  cabinPressureKpa: Double = 101.3
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: 3D Wireframe, 1: Radar Scan
  var rotationYaw by remember { mutableFloatStateOf(25f) }
  var rotationPitch by remember { mutableFloatStateOf(-15f) }
  var selectedModuleIndex by remember { mutableIntStateOf(0) }

  val modules = remember {
    listOf(
      SpacecraftModuleInfo(
        0,
        "Command Bridge & Avionics",
        "NOMINAL",
        "Guidance computers online. Fly-by-wire latency: 0.02ms. Atmospheric loop: 101.3 kPa.",
        NominalGreen
      ),
      SpacecraftModuleInfo(
        1,
        "Hab Node 1 (Living Quarters)",
        "OCCUPIED",
        "4 Private sleep pods. Circadian dynamic lighting active (480nm). Noise level: 44 dBA.",
        NeonCyan
      ),
      SpacecraftModuleInfo(
        2,
        "Columbus Science & Bio-Lab",
        "ACTIVE",
        "Cell biology centrifuge running at 0.38g. Bio-telemetry downlink sync ready.",
        BrightTeal
      ),
      SpacecraftModuleInfo(
        3,
        "Central Storm Shelter (Core)",
        if (isStormShieldEngaged) "SHIELD ENGAGED" else "STANDBY",
        "50cm Polyethylene + 2,000L grey water containment wall. SPE protection factor: 18x.",
        if (isStormShieldEngaged) WarningAmber else StarlightBlue
      ),
      SpacecraftModuleInfo(
        4,
        "Quest Airlock & EVA Prep",
        "SEALED 100%",
        "External hatch sealed. Pressure differential: 0.0 kPa. Suits Alpha & Beta charged.",
        NominalGreen
      ),
      SpacecraftModuleInfo(
        5,
        "Propulsion & Ion Thruster Bay",
        "ONLINE",
        "Xenon propellant remaining: 86%. Magnetoplasmadynamic thruster coils calibrated.",
        NeonCyan
      )
    )
  }

  // Radar continuous rotation animation
  val infiniteTransition = rememberInfiniteTransition(label = "radar")
  val radarSweepAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(4000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "radarSweep"
  )

  // Gentle idle rotation
  val idleYaw by infiniteTransition.animateFloat(
    initialValue = -5f,
    targetValue = 5f,
    animationSpec = infiniteRepeatable(
      animation = tween(6000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "idleYaw"
  )

  GlassCard(
    modifier = modifier.testTag("spacecraft_3d_card"),
    cornerRadius = 20.dp
  ) {
    Column {
      // Header & Mode Toggles
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (selectedTab == 0) Icons.Default.RocketLaunch else Icons.Default.Radar,
              contentDescription = null,
              tint = NeonCyan,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (selectedTab == 0) "SPACECRAFT TELEMETRY 3D" else "SYSTEM RADAR SCAN",
              style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontSize = 8.5.sp),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
          Text(
            text = "NSS ASTRA-V • HABITAT MODEL",
            style = MaterialTheme.typography.titleSmall.copy(
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 11.5.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(2.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(if (selectedTab == 0) NeonCyan.copy(alpha = 0.25f) else Color.Transparent)
              .clickable { selectedTab = 0 }
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              "3D MESH",
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (selectedTab == 0) NeonCyan else Color.Gray,
                fontSize = 8.5.sp,
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
              ),
              maxLines = 1,
              softWrap = false
            )
          }
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(if (selectedTab == 1) NeonCyan.copy(alpha = 0.25f) else Color.Transparent)
              .clickable { selectedTab = 1 }
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              "RADAR",
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (selectedTab == 1) NeonCyan else Color.Gray,
                fontSize = 8.5.sp,
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
              ),
              maxLines = 1,
              softWrap = false
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Canvas Rendering
      if (selectedTab == 0) {
        // Interactive 3D Wireframe Canvas
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF03060E))
            .border(1.dp, Color(0x2238BDF8), RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
              detectDragGestures { change, dragAmount ->
                change.consume()
                rotationYaw = (rotationYaw + dragAmount.x * 0.4f).coerceIn(-60f, 60f)
                rotationPitch = (rotationPitch - dragAmount.y * 0.4f).coerceIn(-40f, 40f)
              }
            }
        ) {
          Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            val totalYawRad = Math.toRadians((rotationYaw + idleYaw).toDouble())
            val pitchRad = Math.toRadians(rotationPitch.toDouble())

            val cosY = cos(totalYawRad).toFloat()
            val sinY = sin(totalYawRad).toFloat()
            val cosP = cos(pitchRad).toFloat()
            val sinP = sin(pitchRad).toFloat()

            // 3D Point Projection Helper
            fun project(x: Float, y: Float, z: Float): Offset {
              // Yaw around Y axis
              val x1 = x * cosY + z * sinY
              val z1 = -x * sinY + z * cosY
              // Pitch around X axis
              val y2 = y * cosP - z1 * sinP
              val z2 = y * sinP + z1 * cosP

              val scale = 180f / (180f + z2 * 0.5f)
              return Offset(cx + x1 * scale, cy + y2 * scale)
            }

            // Draw Celestial Background Grid
            val gridColor = Color(0x1538BDF8)
            for (i in -4..4) {
              val pStart = project(i * 35f, 60f, -100f)
              val pEnd = project(i * 35f, 60f, 100f)
              drawLine(gridColor, pStart, pEnd, strokeWidth = 1f)
            }

            // Spacecraft Central Truss & Cylinders
            val moduleSegments = listOf(
              Pair(-120f, -80f), // Propulsion Bay
              Pair(-80f, -40f),  // Engine Mount & Reaction Wheels
              Pair(-40f, 0f),    // Central Radiation Shelter Core
              Pair(0f, 40f),     // Habitation Node 1
              Pair(40f, 80f),    // Science Columbus Lab
              Pair(80f, 120f)    // Command Bridge & Nose Cone
            )

            // Draw Modules
            moduleSegments.forEachIndexed { idx, (zStart, zEnd) ->
              val isSelected = idx == selectedModuleIndex
              val modColor = if (isSelected) NeonCyan else Color(0x8838BDF8)
              val radius = if (idx == 2) 34f else 26f // Shelter is thicker

              val rings = 4
              for (r in 0..rings) {
                val z = zStart + (zEnd - zStart) * (r.toFloat() / rings)
                val path = Path()
                for (a in 0..12) {
                  val angle = Math.toRadians((a * 30.0))
                  val px = (radius * cos(angle)).toFloat()
                  val py = (radius * sin(angle)).toFloat()
                  val pt = project(px, py, z)
                  if (a == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                }
                drawPath(
                  path = path,
                  color = if (isSelected) NeonCyan.copy(alpha = 0.9f) else modColor.copy(alpha = 0.4f),
                  style = Stroke(width = if (isSelected) 2f else 1f)
                )
              }
            }

            // Solar Array Wings (Port & Starboard)
            val wingColor = Color(0x992DD4BF)
            // Left Solar Array
            val sol1 = project(-110f, 0f, 10f)
            val sol2 = project(-40f, 0f, 10f)
            val sol3 = project(-40f, 0f, -10f)
            val sol4 = project(-110f, 0f, -10f)
            val wingPathL = Path().apply {
              moveTo(sol1.x, sol1.y)
              lineTo(sol2.x, sol2.y)
              lineTo(sol3.x, sol3.y)
              lineTo(sol4.x, sol4.y)
              close()
            }
            drawPath(wingPathL, wingColor, style = Stroke(width = 1.5f))

            // Right Solar Array
            val rSol1 = project(40f, 0f, 10f)
            val rSol2 = project(110f, 0f, 10f)
            val rSol3 = project(110f, 0f, -10f)
            val rSol4 = project(40f, 0f, -10f)
            val wingPathR = Path().apply {
              moveTo(rSol1.x, rSol1.y)
              lineTo(rSol2.x, rSol2.y)
              lineTo(rSol3.x, rSol3.y)
              lineTo(rSol4.x, rSol4.y)
              close()
            }
            drawPath(wingPathR, wingColor, style = Stroke(width = 1.5f))

            // Storm Shield Glowing Boundary if engaged
            if (isStormShieldEngaged) {
              drawCircle(
                color = WarningAmber.copy(alpha = 0.25f),
                radius = 75f,
                center = project(0f, 0f, 0f),
                style = Stroke(width = 2f)
              )
            }
          }

          // Drag Helper Hint
          Text(
            text = "DRAG TO ROTATE 3D",
            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.5f)),
            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
          )
        }
      } else {
        // Live Radar Sweep Canvas (matching the reference image's SYSTEM SCAN radar)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF020710))
            .border(1.dp, Color(0x332DD4BF), RoundedCornerShape(12.dp)),
          contentAlignment = Alignment.Center
        ) {
          Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = 85.dp.toPx()

            // Concentric Rings
            drawCircle(Color(0x1F2DD4BF), radius = maxRadius, center = center, style = Stroke(1f))
            drawCircle(Color(0x2E2DD4BF), radius = maxRadius * 0.66f, center = center, style = Stroke(1f))
            drawCircle(Color(0x3D2DD4BF), radius = maxRadius * 0.33f, center = center, style = Stroke(1f))

            // Crosshairs
            drawLine(Color(0x222DD4BF), Offset(center.x - maxRadius, center.y), Offset(center.x + maxRadius, center.y))
            drawLine(Color(0x222DD4BF), Offset(center.x, center.y - maxRadius), Offset(center.x, center.y + maxRadius))

            // Rotating Sweep Line
            rotate(degrees = radarSweepAngle, pivot = center) {
              drawLine(
                color = NeonCyan,
                start = center,
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 2f
              )
            }

            // Radar Blips (Astronauts & Sensor Beacons)
            val blips = listOf(
              Triple(0.45f, 45f, "EVA SUIT 1"),
              Triple(0.70f, 130f, "SOLAR ARRAY SENSOR"),
              Triple(0.30f, 210f, "CARGO DOCK"),
              Triple(0.85f, 320f, "EXTERNAL DOSIMETER")
            )

            blips.forEach { (distFrac, angleDeg, _) ->
              val r = maxRadius * distFrac
              val rad = Math.toRadians(angleDeg.toDouble())
              val bx = center.x + (r * cos(rad)).toFloat()
              val by = center.y + (r * sin(rad)).toFloat()

              drawCircle(Color(0x6600F0FF), radius = 6f, center = Offset(bx, by))
              drawCircle(NeonCyan, radius = 3f, center = Offset(bx, by))
            }
          }

          Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
          ) {
            Text(
              "RADAR TRACKING: 4 TELEMETRY BEACONS",
              style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal)
            )
            Text(
              "FREQUENCY: 2.4 GHz S-BAND • ZERO INTERFERENCE",
              style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Module Selector Tabs
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        modules.forEachIndexed { idx, mod ->
          val isSelected = idx == selectedModuleIndex
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(
                if (isSelected) NeonCyan.copy(alpha = 0.2f)
                else Color.White.copy(alpha = 0.04f)
              )
              .clickable { selectedModuleIndex = idx }
              .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "M${idx + 1}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (isSelected) NeonCyan else Color.LightGray
              )
            )
          }
          if (idx < modules.size - 1) Spacer(modifier = Modifier.width(4.dp))
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Selected Module Info Box
      val currMod = modules[selectedModuleIndex]
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(Color.Black.copy(alpha = 0.3f))
          .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
          .padding(10.dp)
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              currMod.name,
              style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
            )
            StatusBadge(text = currMod.status, color = currMod.healthColor)
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            currMod.details,
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFCBD5E1))
          )
        }
      }
    }
  }
}
