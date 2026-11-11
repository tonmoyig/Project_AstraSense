package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MedicalOfficerActionReceiver : BroadcastReceiver() {

  companion object {
    private const val TAG = "MedicalOfficerReceiver"
  }

  override fun onReceive(context: Context?, intent: Intent?) {
    if (context == null || intent == null) return

    val action = intent.action
    if (action == AstraMissionRiskNotificationService.ACTION_ACKNOWLEDGE_CRITICAL) {
      val alertId = intent.getStringExtra(AstraMissionRiskNotificationService.EXTRA_ALERT_ID)
      val astronautId = intent.getStringExtra(AstraMissionRiskNotificationService.EXTRA_ASTRONAUT_ID) ?: "UNKNOWN"
      val score = intent.getIntExtra(AstraMissionRiskNotificationService.EXTRA_RISK_SCORE, 0)

      Log.i(TAG, "Medical Officer Quick Acknowledgement received for alert $alertId, astronaut $astronautId, score $score")

      if (!alertId.isNullOrEmpty()) {
        AstraMissionRiskNotificationService.acknowledgeAlert(
          context = context,
          alertId = alertId,
          officerId = "FLIGHT-SURGEON-DIRECT",
          notes = "Instant acknowledgement signed via system notification action shade."
        )
      }
    }
  }
}
