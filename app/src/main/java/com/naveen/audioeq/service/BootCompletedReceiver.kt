package com.naveen.audioeq.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Service is only started if the user had master switch ON before reboot.
            // Actual preference check happens via SettingsRepository in a full build.
        }
    }
}
