package com.example.model

enum class DestinationType(
  val displayName: String,
  val subtitle: String,
  val distanceDescription: String,
  val commsDelaySeconds: Int,
  val gravityDescription: String,
  val radiationBaselinePerDay: Double, // in mSv
  val habitatVehicle: String,
  val nasaReference: String
) {
  MARS_TRANSIT(
    displayName = "Mars Transit & Surface Mission",
    subtitle = "Deep Space Long-Duration Interplanetary",
    distanceDescription = "225,000,000 km (Avg)",
    commsDelaySeconds = 840, // 14 min each way
    gravityDescription = "0.38g (Surface) / 0g (Transit)",
    radiationBaselinePerDay = 1.35, // Galactic Cosmic Rays + SPE
    habitatVehicle = "Astra Deep Space Transport (DST)",
    nasaReference = "NASA Design Reference Architecture 5.0 / Artemis to Mars"
  ),
  LUNAR_GATEWAY(
    displayName = "Lunar Gateway & Artemis Base Camp",
    subtitle = "Deep Space Proving Ground",
    distanceDescription = "384,400 km",
    commsDelaySeconds = 2, // 1.3 to 2 seconds
    gravityDescription = "0.166g (1/6 Earth g)",
    radiationBaselinePerDay = 0.72,
    habitatVehicle = "Gateway HALO & Starship HLS",
    nasaReference = "NASA Artemis Accord / Moon to Mars Architecture"
  ),
  LEO_ISS(
    displayName = "Low Earth Orbit / ISS",
    subtitle = "Baseline Control (Microgravity Reference)",
    distanceDescription = "408 km Altitude",
    commsDelaySeconds = 0, // Real-time TDRSS satellite link
    gravityDescription = "Microgravity (10⁻⁶ g)",
    radiationBaselinePerDay = 0.38, // South Atlantic Anomaly passages
    habitatVehicle = "International Space Station (Destiny/Harmony)",
    nasaReference = "NASA Human Research Program (HRP) Baseline"
  )
}
