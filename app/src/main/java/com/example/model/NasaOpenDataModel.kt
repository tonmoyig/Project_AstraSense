package com.example.model

/**
 * Data models for NASA Open Data Portal (data.nasa.gov) and NASA DONKI API (api.nasa.gov)
 */

data class NasaDatasetPackage(
  val id: String,
  val name: String,
  val title: String,
  val notes: String,
  val maintainer: String?,
  val licenseTitle: String?,
  val metadataCreated: String?,
  val metadataModified: String?,
  val resources: List<NasaDatasetResource> = emptyList(),
  val tags: List<String> = emptyList()
)

data class NasaDatasetResource(
  val id: String,
  val name: String,
  val format: String,
  val url: String,
  val description: String? = null
)

/**
 * Extra-vehicular Activity (EVA) data model based on NASA Open Data dataset:
 * https://data.nasa.gov/dataset/extra-vehicular-activity-eva-us-and-russia
 */
data class NasaEvaRecord(
  val id: String,
  val evaNumber: Int,
  val country: String,      // "USA", "Russia", "International"
  val crewMembers: List<String>,
  val vehicle: String,      // e.g. "International Space Station", "Space Shuttle", "Apollo 11"
  val date: String,         // YYYY-MM-DD
  val durationMinutes: Int, // e.g. 412 min (6h 52m)
  val purpose: String,
  val radiationDoseEstimateMsv: Double = 0.35, // Estimated cosmic dose during spacewalk
  val suitType: String = "EMU (Extravehicular Mobility Unit)"
) {
  val formattedDuration: String
    get() {
      val hours = durationMinutes / 60
      val mins = durationMinutes % 60
      return "${hours}h ${mins}m"
    }
}

/**
 * Space Weather & Solar Energetic Particle event from NASA Space Weather Database (DONKI / api.nasa.gov)
 */
data class NasaDonkiNotification(
  val messageId: String,
  val messageType: String, // "FLR" (Solar Flare), "SEP" (Solar Particle), "CME", "GST" (Geomagnetic Storm)
  val issueTime: String,
  val solarHazardClass: String, // e.g. "X1.8", "M5.2", "S2 Moderate Particle Storm"
  val summary: String,
  val peakFluxRate: Double, // in pfu or mSv/h
  val radiationRiskLevel: RiskLevel,
  val recommendedAction: String,
  val sourceUrl: String = "https://api.nasa.gov/DONKI/notifications"
)

/**
 * NASA Human Research Program (HRP) Spaceflight Hazard Definition
 * based on NASA Human Research Roadmap & open data publications
 */
data class NasaHrpHazard(
  val id: String,
  val hazardName: String,
  val acronym: String,
  val description: String,
  val primaryPhysiologicalRisks: List<String>,
  val primaryBiomarkers: List<String>,
  val nasaCountermeasureProtocol: String,
  val deepSpaceMultiplier: String // e.g. "3.2x higher on Mars Transit vs LEO"
)
