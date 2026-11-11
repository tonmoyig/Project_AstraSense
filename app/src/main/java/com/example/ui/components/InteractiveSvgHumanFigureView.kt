package com.example.ui.components

import android.graphics.Path as AndroidPath
import android.graphics.RectF
import android.graphics.Region
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.PathParser
import com.example.model.CrewMember
import com.example.model.RiskLevel
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.StarlightBlue
import com.example.ui.theme.WarningAmber

/**
 * Anatomical Body Part classification for SVG path interactive mapping.
 */
enum class SvgBodyRegion(
  val id: String,
  val displayName: String,
  val subtitle: String,
  val svgPathData: String
) {
  HEAD(
    id = "head",
    displayName = "Head & Cranium",
    subtitle = "Neuro-Ocular (SANS), Intracranial Pressure & PVT Reaction",
    // Anatomical cranium, temporal arch, jawline, chin, and cervical spine
    svgPathData = "M 150 20 C 136 20 128 32 128 46 C 128 60 134 72 143 78 L 143 94 L 157 94 L 157 78 C 166 72 172 60 172 46 C 172 32 164 20 150 20 Z"
  ),
  CHEST(
    id = "chest",
    displayName = "Chest & Thorax",
    subtitle = "Cardiovascular (ECG/HR, BP), Pulmonary Lungs & Stroke Volume",
    // Natural sloping trapezius, deltoid apex, pectoral contours, and ribcage taper
    svgPathData = "M 143 94 C 134 96 124 100 116 106 C 114 116 118 128 124 136 C 122 154 124 176 126 196 C 138 194 162 194 174 196 C 176 176 178 154 176 136 C 182 128 186 116 184 106 C 176 100 166 96 157 94 Z"
  ),
  ABDOMEN_CORE(
    id = "abdomen_core",
    displayName = "Abdomen & Pelvis",
    subtitle = "Metabolic Glucose, Core Body Temp, Liver & Renal Calcium",
    // Athletic waist taper, lumbar spine, iliac crest flare, and pelvic arch
    svgPathData = "M 126 196 C 138 194 162 194 174 196 C 172 216 176 228 176 240 C 176 254 168 266 150 258 C 132 266 124 254 124 240 C 124 228 128 216 126 196 Z"
  ),
  LEFT_ARM(
    id = "left_arm",
    displayName = "Left Arm & Hand",
    subtitle = "Bio-Harness BLE Sensor Link, Pulse Wave & Perfusion",
    // Deltoid curve, biceps, elbow flexion, tapered forearm, and hand
    svgPathData = "M 116 106 C 104 112 96 128 90 148 C 84 168 78 198 74 225 C 70 248 66 268 64 282 C 67 284 72 284 75 278 C 79 262 84 240 88 220 C 94 196 100 172 108 150 C 116 142 120 138 124 136 C 120 124 117 114 116 106 Z"
  ),
  RIGHT_ARM(
    id = "right_arm",
    displayName = "Right Arm & Hand",
    subtitle = "Shoulder Dosimeter (Radiation Flux), Grip Force & Skin Barrier",
    // Symmetric right deltoid, bicep, elbow, forearm, and hand
    svgPathData = "M 184 106 C 196 112 204 128 210 148 C 216 168 222 198 226 225 C 230 248 234 268 236 282 C 233 284 228 284 225 278 C 221 262 216 240 212 220 C 206 196 200 172 192 150 C 184 142 180 138 176 136 C 180 124 183 114 184 106 Z"
  ),
  LEFT_LEG(
    id = "left_leg",
    displayName = "Left Leg & Foot",
    subtitle = "Soleus Anti-Gravity Muscle Mass, Calcaneal BMD & Venous Pooling",
    // Natural hip contour, quadriceps, knee joint, calf taper, ankle, and foot
    svgPathData = "M 124 240 C 118 265 116 295 116 330 C 114 365 112 400 118 430 L 112 452 C 118 456 128 454 130 450 L 126 430 C 128 400 128 365 130 330 C 132 295 136 275 150 258 C 140 264 132 266 124 240 Z"
  ),
  RIGHT_LEG(
    id = "right_leg",
    displayName = "Right Leg & Foot",
    subtitle = "Femoral Bone Density, ARED Resistive Load & Kinetic Strike",
    // Symmetric right hip, quadriceps, knee joint, gastrocnemius calf, ankle, and foot
    svgPathData = "M 176 240 C 182 265 184 295 184 330 C 186 365 188 400 182 430 L 188 452 C 182 456 172 454 170 450 L 174 430 C 172 400 172 365 170 330 C 168 295 164 275 150 258 C 160 264 168 266 176 240 Z"
  )
}

/**
 * Biometric detail profile corresponding to a body region.
 */
data class RegionBiometricDetails(
  val region: SvgBodyRegion,
  val statusLevel: RiskLevel,
  val primaryMetricTitle: String,
  val primaryMetricValue: String,
  val secondaryMetricTitle: String,
  val secondaryMetricValue: String,
  val clinicalSummary: String,
  val spaceflightHazardNotice: String,
  val activeTreatmentProtocol: String,
  val keyVitals: List<Pair<String, String>>
)

/**
 * A new interactive Composable displaying an anatomical human figure rendered using SVG paths.
 * Clicking on body regions (head, chest, limbs) triggers an expansive detail view of related
 * health biometrics with NASA-STD-3001 clinical guidance and interactive telemetry sliders.
 */
@Composable
fun InteractiveSvgHumanFigureView(
  crewMember: CrewMember,
  modifier: Modifier = Modifier,
  initialSelectedRegion: SvgBodyRegion = SvgBodyRegion.CHEST,
  onRegionSelected: ((SvgBodyRegion) -> Unit)? = null
) {
  var selectedRegion by remember { mutableStateOf(initialSelectedRegion) }

  // Interactive biometric calibration state
  var customGlucose by remember(crewMember) { mutableIntStateOf(92) }
  var customOnsd by remember(crewMember) { mutableFloatStateOf(4.7f) }
  var customHeartRate by remember(crewMember) { mutableIntStateOf(crewMember.vitals.heartRateBpm) }
  var customAredTonnage by remember(crewMember) { mutableFloatStateOf(14.5f) }

  // Pulsing animation for active SVG path highlight & scanner beam
  val infiniteTransition = rememberInfiniteTransition(label = "svgPulse")
  val pulseGlowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.25f,
    targetValue = 0.65f,
    animationSpec = infiniteRepeatable(
      animation = tween(1100),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseGlowAlpha"
  )
  val scanAnim by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(2800, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "scanAnim"
  )

  // Compute biometric detail profile for the currently selected SVG region
  val details = remember(selectedRegion, crewMember, customGlucose, customOnsd, customHeartRate, customAredTonnage) {
    computeBiometricDetails(
      region = selectedRegion,
      crewMember = crewMember,
      glucose = customGlucose,
      onsd = customOnsd,
      heartRate = customHeartRate,
      aredTonnage = customAredTonnage
    )
  }

  GlassCard(
    modifier = modifier.fillMaxWidth().testTag("interactive_svg_human_figure_card"),
    cornerRadius = 20.dp
  ) {
    Column {
      // 1. Header: Component Title & Current Astronaut Identifier
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
              .size(32.dp)
              .clip(CircleShape)
              .background(NeonCyan.copy(alpha = 0.2f))
              .border(1.dp, NeonCyan, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Accessibility, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
              "SVG ANATOMICAL BIOMETRIC FIGURE",
              style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              "Click body region or limb to inspect telemetry",
              style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 8.sp),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        StatusBadge(
          text = details.statusLevel.label,
          color = when (details.statusLevel) {
            RiskLevel.LOW -> NominalGreen
            RiskLevel.MODERATE -> WarningAmber
            RiskLevel.HIGH -> CriticalRed
          }
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // 2. Interactive SVG Canvas & Body Region Quick Selection Strip
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        SvgBodyRegion.entries.forEach { region ->
          val isSelected = region == selectedRegion
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) NeonCyan.copy(alpha = 0.28f) else Color.Black.copy(alpha = 0.45f))
              .border(1.dp, if (isSelected) NeonCyan else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
              .clickable {
                selectedRegion = region
                onRegionSelected?.invoke(region)
              }
              .padding(horizontal = 9.dp, vertical = 5.dp)
              .testTag("svg_region_chip_${region.id}"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = region.displayName.split(" ").first().uppercase(),
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (isSelected) Color.White else Color.LightGray,
                fontSize = 8.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // 3. Main Centerpiece: Holographic SVG Vector Human Figure Canvas
      BoxWithConstraints(
        modifier = Modifier
          .fillMaxWidth()
          .height(260.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(Color(0xFF020712).copy(alpha = 0.95f))
          .border(1.dp, GlassBorderStroke, RoundedCornerShape(14.dp))
          .padding(8.dp)
          .testTag("svg_human_canvas_box")
      ) {
        val canvasWidth = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()

        // SVG Coordinate Space: 300 x 500
        val svgViewBoxWidth = 300f
        val svgViewBoxHeight = 500f

        // Uniform aspect ratio scaling prevents distortion/squashing of human physique
        val uniformScale = minOf(canvasWidth / svgViewBoxWidth, canvasHeight / svgViewBoxHeight)
        val offsetX = (canvasWidth - svgViewBoxWidth * uniformScale) / 2f
        val offsetY = (canvasHeight - svgViewBoxHeight * uniformScale) / 2f

        // Prepare Android Path & Region objects for exact vector hit testing
        val parsedRegions = remember {
          SvgBodyRegion.entries.map { regionEnum ->
            val androidPath = PathParser.createPathFromPathData(regionEnum.svgPathData)
            val rectF = RectF()
            androidPath.computeBounds(rectF, true)
            val clipRegion = Region(rectF.left.toInt(), rectF.top.toInt(), rectF.right.toInt(), rectF.bottom.toInt())
            val hitRegion = Region()
            hitRegion.setPath(androidPath, clipRegion)
            Triple(regionEnum, androidPath, hitRegion)
          }
        }

        Canvas(
          modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
              detectTapGestures { tapOffset ->
                // Map screen tap to SVG viewBox 300x500 coordinates accounting for offset
                val svgTapX = (tapOffset.x - offsetX) / uniformScale
                val svgTapY = (tapOffset.y - offsetY) / uniformScale

                // Check exact vector path hit
                val hit = parsedRegions.find { (_, _, hitRegion) ->
                  hitRegion.contains(svgTapX.toInt(), svgTapY.toInt())
                }

                if (hit != null) {
                  selectedRegion = hit.first
                  onRegionSelected?.invoke(hit.first)
                } else {
                  // Fallback: Closest centroid for generous touch targets
                  val nearest = parsedRegions.minByOrNull { (r, androidPath, _) ->
                    val b = RectF()
                    androidPath.computeBounds(b, true)
                    val dx = svgTapX - b.centerX()
                    val dy = svgTapY - b.centerY()
                    dx * dx + dy * dy
                  }
                  if (nearest != null) {
                    selectedRegion = nearest.first
                    onRegionSelected?.invoke(nearest.first)
                  }
                }
              }
            }
        ) {
          // Draw subtle background cybernetic grid
          for (i in 1..4) {
            val y = canvasHeight * (i / 5f)
            drawLine(Color.White.copy(alpha = 0.04f), Offset(0f, y), Offset(canvasWidth, y), strokeWidth = 1f)
          }

          // Central spinal physiological reference axis
          drawLine(
            color = Color.White.copy(alpha = 0.08f),
            start = Offset(offsetX + 150f * uniformScale, offsetY + 78f * uniformScale),
            end = Offset(offsetX + 150f * uniformScale, offsetY + 258f * uniformScale),
            strokeWidth = 1.2f
          )

          // Draw each SVG Body Region with anatomical glow
          parsedRegions.forEach { (regionEnum, androidPath, _) ->
            val isSelected = regionEnum == selectedRegion

            // Create scaled & centered Compose path for rendering
            val scaledAndroidPath = AndroidPath(androidPath)
            val matrix = android.graphics.Matrix()
            matrix.setScale(uniformScale, uniformScale)
            matrix.postTranslate(offsetX, offsetY)
            scaledAndroidPath.transform(matrix)
            val composePath = scaledAndroidPath.asComposePath()

            val regionColor = when {
              isSelected -> NeonCyan
              regionEnum == SvgBodyRegion.HEAD && customOnsd > 5.5f -> CriticalRed
              regionEnum == SvgBodyRegion.CHEST && customHeartRate > 120 -> CriticalRed
              regionEnum == SvgBodyRegion.ABDOMEN_CORE && customGlucose > 140 -> WarningAmber
              else -> BrightTeal
            }

            // Fill body segment with radial aerospace underglow
            drawPath(
              path = composePath,
              brush = Brush.radialGradient(
                colors = listOf(
                  regionColor.copy(alpha = if (isSelected) pulseGlowAlpha else 0.14f),
                  regionColor.copy(alpha = if (isSelected) 0.24f else 0.04f)
                ),
                center = Offset(offsetX + 150f * uniformScale, offsetY + 250f * uniformScale),
                radius = 190f * uniformScale
              )
            )

            // Outline stroke
            drawPath(
              path = composePath,
              color = regionColor.copy(alpha = if (isSelected) 0.95f else 0.45f),
              style = Stroke(width = if (isSelected) 2.2f else 1.2f)
            )

            // Draw holographic biometric telemetry node for each anatomical region
            val nodeCoords = when (regionEnum) {
              SvgBodyRegion.HEAD -> Offset(150f, 48f)
              SvgBodyRegion.CHEST -> Offset(150f, 138f)
              SvgBodyRegion.ABDOMEN_CORE -> Offset(150f, 225f)
              SvgBodyRegion.LEFT_ARM -> Offset(86f, 195f)
              SvgBodyRegion.RIGHT_ARM -> Offset(214f, 195f)
              SvgBodyRegion.LEFT_LEG -> Offset(122f, 350f)
              SvgBodyRegion.RIGHT_LEG -> Offset(178f, 350f)
            }
            val nx = offsetX + nodeCoords.x * uniformScale
            val ny = offsetY + nodeCoords.y * uniformScale

            if (isSelected) {
              // Active targeting reticle & brackets
              drawCircle(
                color = NeonCyan.copy(alpha = pulseGlowAlpha * 0.5f),
                radius = 16f,
                center = Offset(nx, ny)
              )
              drawCircle(
                color = NeonCyan,
                radius = 12f,
                center = Offset(nx, ny),
                style = Stroke(width = 1.5f)
              )
              // Crosshair ticks
              drawLine(NeonCyan, Offset(nx, ny - 16f), Offset(nx, ny - 20f), strokeWidth = 1.5f)
              drawLine(NeonCyan, Offset(nx, ny + 16f), Offset(nx, ny + 20f), strokeWidth = 1.5f)
              drawLine(NeonCyan, Offset(nx - 16f, ny), Offset(nx - 20f, ny), strokeWidth = 1.5f)
              drawLine(NeonCyan, Offset(nx + 16f, ny), Offset(nx + 20f, ny), strokeWidth = 1.5f)
              // Center core pip
              drawCircle(
                color = Color.White,
                radius = 4f,
                center = Offset(nx, ny)
              )
            } else {
              // Passive sensor bead
              drawCircle(
                color = regionColor.copy(alpha = 0.35f),
                radius = 6f,
                center = Offset(nx, ny)
              )
              drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                radius = 2.5f,
                center = Offset(nx, ny)
              )
            }
          }

          // Animated vertical aerospace medical HUD laser scan beam
          val scanY = offsetY + 20f * uniformScale + (455f * uniformScale) * scanAnim
          drawLine(
            brush = Brush.horizontalGradient(
              listOf(
                Color.Transparent,
                NeonCyan.copy(alpha = 0.15f),
                NeonCyan.copy(alpha = 0.65f),
                Color.White.copy(alpha = 0.85f),
                NeonCyan.copy(alpha = 0.65f),
                NeonCyan.copy(alpha = 0.15f),
                Color.Transparent
              )
            ),
            start = Offset(offsetX + 20f * uniformScale, scanY),
            end = Offset(offsetX + 280f * uniformScale, scanY),
            strokeWidth = 2f
          )
        }

        // Floating Region Label overlay on bottom left
        Column(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.65f))
            .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "SELECTED: ${selectedRegion.displayName.uppercase()}",
            style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
          )
          Text(
            text = selectedRegion.subtitle,
            style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray, fontSize = 7.5.sp),
            maxLines = 1
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 4. Detail View of Related Health Biometrics for Selected Region
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF041122).copy(alpha = 0.85f))
          .border(1.dp, GlassBorderStroke, RoundedCornerShape(12.dp))
          .padding(12.dp)
          .testTag("svg_biometric_detail_view")
      ) {
        // Detail Header: Title & Action
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = when (selectedRegion) {
                SvgBodyRegion.HEAD -> Icons.Default.Psychology
                SvgBodyRegion.CHEST -> Icons.Default.Favorite
                SvgBodyRegion.ABDOMEN_CORE -> Icons.Default.WaterDrop
                SvgBodyRegion.LEFT_ARM, SvgBodyRegion.RIGHT_ARM -> Icons.Default.Sensors
                SvgBodyRegion.LEFT_LEG, SvgBodyRegion.RIGHT_LEG -> Icons.Default.FitnessCenter
              },
              contentDescription = null,
              tint = NeonCyan,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "${selectedRegion.displayName.uppercase()} BIOMETRIC TELEMETRY",
              style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            )
          }

          StatusBadge(
            text = details.statusLevel.label,
            color = when (details.statusLevel) {
              RiskLevel.LOW -> NominalGreen
              RiskLevel.MODERATE -> WarningAmber
              RiskLevel.HIGH -> CriticalRed
            }
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Two Main Telemetry Highlight Cards
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          MetricHighlightBox(
            label = details.primaryMetricTitle,
            value = details.primaryMetricValue,
            accentColor = NeonCyan,
            modifier = Modifier.weight(1f)
          )
          MetricHighlightBox(
            label = details.secondaryMetricTitle,
            value = details.secondaryMetricValue,
            accentColor = BrightTeal,
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Key Vitals Grid
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          details.keyVitals.forEach { (vitalLabel, vitalValue) ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.35f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(vitalLabel, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 9.sp))
              Text(vitalValue, style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.5.sp))
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Spaceflight Hazard Mechanism & Clinical Action Directive
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, BrightTeal.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(8.dp)
        ) {
          Text(
            text = "SPACEFLIGHT HAZARD MECHANISM:",
            style = MaterialTheme.typography.labelSmall.copy(color = WarningAmber, fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = details.spaceflightHazardNotice,
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE2E8F0), fontSize = 8.5.sp, lineHeight = 13.sp)
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "CLINICAL ACTION & COUNTERMEASURE:",
            style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = details.activeTreatmentProtocol,
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFBAE6FD), fontSize = 8.5.sp, lineHeight = 13.sp)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interactive Region Biometric Slider
        when (selectedRegion) {
          SvgBodyRegion.HEAD -> {
            InteractiveBiometricSlider(
              label = "Simulate Optic Nerve Sheath Diameter (ONSD mm)",
              display = "${"%.1f".format(customOnsd)} mm",
              value = customOnsd,
              range = 3.8f..6.5f,
              onValueChange = { customOnsd = it }
            )
          }
          SvgBodyRegion.CHEST -> {
            InteractiveBiometricSlider(
              label = "Simulate Heart Rate (ECG bpm)",
              display = "$customHeartRate bpm",
              value = customHeartRate.toFloat(),
              range = 45f..160f,
              onValueChange = { customHeartRate = it.toInt() }
            )
          }
          SvgBodyRegion.ABDOMEN_CORE -> {
            InteractiveBiometricSlider(
              label = "Simulate Blood Glucose (Fingerstick mg/dL)",
              display = "$customGlucose mg/dL",
              value = customGlucose.toFloat(),
              range = 60f..200f,
              onValueChange = { customGlucose = it.toInt() }
            )
          }
          SvgBodyRegion.LEFT_LEG, SvgBodyRegion.RIGHT_LEG -> {
            InteractiveBiometricSlider(
              label = "Simulate Daily ARED Resistive Load",
              display = "${"%.1f".format(customAredTonnage)} Metric Tons",
              value = customAredTonnage,
              range = 8.0f..18.0f,
              onValueChange = { customAredTonnage = it }
            )
          }
          else -> {
            // Nominal feedback for arm limbs
            Text(
              "Sensor telemetry stream: Nominal Bluetooth Low Energy bio-harness telemetry active.",
              style = MaterialTheme.typography.labelSmall.copy(color = NominalGreen, fontSize = 8.5.sp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MetricHighlightBox(
  label: String,
  value: String,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(Color.Black.copy(alpha = 0.45f))
      .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
      .padding(8.dp)
  ) {
    Column {
      Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp))
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        value,
        style = MaterialTheme.typography.titleSmall.copy(color = accentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
      )
    }
  }
}

@Composable
private fun InteractiveBiometricSlider(
  label: String,
  display: String,
  value: Float,
  range: ClosedFloatingPointRange<Float>,
  onValueChange: (Float) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(Color.Black.copy(alpha = 0.3f))
      .padding(8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray, fontSize = 8.5.sp))
      Text(display, style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 9.sp))
    }
    Slider(
      value = value.coerceIn(range.start, range.endInclusive),
      onValueChange = onValueChange,
      valueRange = range,
      colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan),
      modifier = Modifier.fillMaxWidth()
    )
  }
}

/**
 * Computes clinically accurate spaceflight biometric profiles for each SVG body region.
 */
fun computeBiometricDetails(
  region: SvgBodyRegion,
  crewMember: CrewMember,
  glucose: Int,
  onsd: Float,
  heartRate: Int,
  aredTonnage: Float
): RegionBiometricDetails {
  return when (region) {
    SvgBodyRegion.HEAD -> {
      val status = if (onsd > 5.8f) RiskLevel.HIGH else if (onsd > 5.2f) RiskLevel.MODERATE else RiskLevel.LOW
      RegionBiometricDetails(
        region = region,
        statusLevel = status,
        primaryMetricTitle = "Optic Nerve Ultrasound (ONSD)",
        primaryMetricValue = "${"%.1f".format(onsd)} mm",
        secondaryMetricTitle = "Intracranial Pressure (ICP)",
        secondaryMetricValue = if (onsd > 5.5f) "Elevated (21 mmHg)" else "Nominal (15 mmHg)",
        clinicalSummary = "Cephalad fluid redistribution causes venous congestion and elevated intracranial pressure behind optic nerve head.",
        spaceflightHazardNotice = "Spaceflight-Associated Neuro-ocular Syndrome (SANS): Posterior globe flattening, optic disc edema (papilledema), and hyperopic shifts.",
        activeTreatmentProtocol = "Execute 45 min Chibis Lower Body Negative Pressure (LBNP at -30 mmHg) to drain cranial venous pooling; prescribe focal flight glasses.",
        keyVitals = listOf(
          "Visual Acuity" to if (onsd > 5.5f) "20/30 (Hyperopic Shift)" else "20/20 (Nominal)",
          "Cognitive Reaction Latency" to "${crewMember.lastPvtReactionTimeMs} ms (PVT Test)",
          "Otolith Stability Index" to "89/100 (Neurovestibular Adaptation)",
          "Middle Cerebral Artery Velocity" to "68 cm/s (TCD Ultrasound)"
        )
      )
    }

    SvgBodyRegion.CHEST -> {
      val status = if (heartRate > 120 || crewMember.vitals.spo2Percentage < 92) RiskLevel.HIGH else if (heartRate > 100 || crewMember.vitals.spo2Percentage < 95) RiskLevel.MODERATE else RiskLevel.LOW
      RegionBiometricDetails(
        region = region,
        statusLevel = status,
        primaryMetricTitle = "Heart Rate & Rhythm",
        primaryMetricValue = "$heartRate BPM (Sinus)",
        secondaryMetricTitle = "Blood Pressure",
        secondaryMetricValue = "${crewMember.vitals.systolicBp} / ${crewMember.vitals.diastolicBp} mmHg",
        clinicalSummary = "Thoracic blood volume expansion in early mission followed by cardiac left ventricular mass shrinkage (~8-10% over 6 months).",
        spaceflightHazardNotice = "Orthostatic deconditioning, decreased stroke volume, potential ventricular ectopic premature contractions (PVCs).",
        activeTreatmentProtocol = "Preserve cardiac stroke volume through daily 30-minute high-intensity cycle ergometer intervals at 75-85% VO2 peak.",
        keyVitals = listOf(
          "Cardiac Ejection Fraction (LVEF)" to "63% (Normal 55-70%)",
          "Oxygen Saturation (SpO₂)" to "${crewMember.vitals.spo2Percentage}%",
          "Respiration Rate" to "${crewMember.vitals.respirationRateBpm} breaths/min",
          "Internal Jugular Diameter" to "7.2 mm (No retrograde flow)"
        )
      )
    }

    SvgBodyRegion.ABDOMEN_CORE -> {
      val status = if (glucose > 160 || glucose < 60) RiskLevel.HIGH else if (glucose > 120 || glucose < 70) RiskLevel.MODERATE else RiskLevel.LOW
      RegionBiometricDetails(
        region = region,
        statusLevel = status,
        primaryMetricTitle = "Blood Glucose (Fingerstick)",
        primaryMetricValue = "$glucose mg/dL",
        secondaryMetricTitle = "Core Body Temperature",
        secondaryMetricValue = "${"%.1f".format(crewMember.vitals.coreTempCelsius)} °C",
        clinicalSummary = "Microgravity alters slow-twitch muscle insulin signaling, precipitating peripheral insulin resistance and altered hepatic glucose production.",
        spaceflightHazardNotice = "Bone demineralization dumps excessive ionic calcium into the kidneys, creating acute nephrolithiasis (kidney stone) precipitation risk.",
        activeTreatmentProtocol = "Mandatory 2.5 L/day fluid intake; low-glycemic spaceflight meal packs; oral potassium citrate alkalinization.",
        keyVitals = listOf(
          "Blood Lactate Concentration" to "1.2 mmol/L (Nominal)",
          "Hydration Status" to "1.8 L fluid logged today",
          "Renal Calcium Excretion" to "Controlled (Hydration adequate)",
          "Gut Microbiome Index" to "Stable (Probiotics supplemented)"
        )
      )
    }

    SvgBodyRegion.LEFT_ARM -> {
      RegionBiometricDetails(
        region = region,
        statusLevel = RiskLevel.LOW,
        primaryMetricTitle = "BLE Bio-Harness Link",
        primaryMetricValue = "Connected (100% Signal)",
        secondaryMetricTitle = "Capillary Refill Time",
        secondaryMetricValue = "1.4 sec (Optimal)",
        clinicalSummary = "Continuous multi-sensor telemetry stream delivering real-time skin temperature, photoplethysmography (PPG), and vector accelerometry.",
        spaceflightHazardNotice = "Peripheral microvascular endothelial changes and reduced upper limb perfusion pressure in microgravity.",
        activeTreatmentProtocol = "Inspect Bio-Harness sensor contact points for contact dermatitis or skin abrasion.",
        keyVitals = listOf(
          "Skin Temperature" to "34.2 °C (Periphery)",
          "Pulse Wave Velocity" to "6.8 m/s (Arterial compliance nominal)",
          "Electrodermal Conductance" to "0.42 µS (Low stress)",
          "Left Hand Grip Force" to "48 kgf"
        )
      )
    }

    SvgBodyRegion.RIGHT_ARM -> {
      val status = if (crewMember.radiation.dailyDoseMsv > 1.0) RiskLevel.HIGH else if (crewMember.radiation.dailyDoseMsv > 0.5) RiskLevel.MODERATE else RiskLevel.LOW
      RegionBiometricDetails(
        region = region,
        statusLevel = status,
        primaryMetricTitle = "Active Shoulder Dosimeter",
        primaryMetricValue = "${"%.3f".format(crewMember.radiation.dailyDoseMsv)} mSv/day",
        secondaryMetricTitle = "Cumulative Mission Dose",
        secondaryMetricValue = "${"%.1f".format(crewMember.radiation.missionCumulativeMsv)} mSv",
        clinicalSummary = "NFC active dosimeter tracking ionizing galactic cosmic ray (GCR) and solar particle event (SPE) exposure on suit shoulder patch.",
        spaceflightHazardNotice = "Deep space ionizing radiation damages cellular DNA, elevates long-term oncogenic risk, and induces endothelial inflammation.",
        activeTreatmentProtocol = "Enforce polyethylene water-wall storm shelter retreat during elevated solar particle flux periods.",
        keyVitals = listOf(
          "Career Dose Threshold" to "${crewMember.radiation.careerThresholdPercentage}% used",
          "Right Hand Grip Force" to "51 kgf",
          "Cutaneous Barrier Integrity" to "Nominal (No radiation erythema)",
          "Suit Chafing Index" to "0 (No skin breakdown)"
        )
      )
    }

    SvgBodyRegion.LEFT_LEG -> {
      val status = if (aredTonnage < 10.0f) RiskLevel.MODERATE else RiskLevel.LOW
      RegionBiometricDetails(
        region = region,
        statusLevel = status,
        primaryMetricTitle = "Soleus Muscle Retention",
        primaryMetricValue = "97.4% baseline",
        secondaryMetricTitle = "Calcaneus Bone Density",
        secondaryMetricValue = "-0.4% delta (Mitigated)",
        clinicalSummary = "Weight-bearing lower limb bones and postural anti-gravity muscles undergo rapid unweighting atrophy without mechanical resistance.",
        spaceflightHazardNotice = "Loss of 1.0 to 1.5% bone mineral density per month; severe calf muscle soleus cross-sectional area shrinkage.",
        activeTreatmentProtocol = "Execute progressive resistive overloading on ARED squats and heel raises (minimum 12 metric tons daily).",
        keyVitals = listOf(
          "Daily ARED Resistance Load" to "${"%.1f".format(aredTonnage)} Metric Tons",
          "Calf Circumference Shift" to "-1.4 cm (Cephalad fluid fluid shift)",
          "Deep Vein Thrombosis (DVT) Risk" to "Low (Compression sleeves active)",
          "Plantar Sensory Threshold" to "Nominal vibration detection"
        )
      )
    }

    SvgBodyRegion.RIGHT_LEG -> {
      val status = if (aredTonnage < 10.0f) RiskLevel.MODERATE else RiskLevel.LOW
      RegionBiometricDetails(
        region = region,
        statusLevel = status,
        primaryMetricTitle = "Femoral Neck Density",
        primaryMetricValue = "-0.3% delta (Mitigated)",
        secondaryMetricTitle = "T2 Treadmill Strike Force",
        secondaryMetricValue = "1.8x Bodyweight",
        clinicalSummary = "Mechanical impact loading on the T2 motorized treadmill stimulates osteoblast bone remodeling and preserves neuromuscular motor unit firing.",
        spaceflightHazardNotice = "Post-flight bone fracture vulnerability and tendon stiffness decrement without daily axial loading.",
        activeTreatmentProtocol = "Maintain 45 minutes of daily high-impact T2 running with bungee shoulder harness tension (>70% bodyweight equivalent).",
        keyVitals = listOf(
          "Daily Treadmill Distance" to "5.2 km logged",
          "Quadriceps Muscle Volume" to "98.1% of baseline",
          "Knee Joint Synovial Fluid" to "Normal (No effusion)",
          "Orthostatic G-Suit Status" to "Checked & stowed for re-entry"
        )
      )
    }
  }
}
