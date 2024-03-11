package com.breezedsm.features.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import timber.log.Timber

class RestartBroadcast : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.e("RestartBroadcast: " + "=======================Received====================")

        //if (!FTStorageUtils.isMyServiceRunning(LocationFuzedService::class.java, context)) {
            Timber.e("RestartBroadcast: " + "=======================Start Service====================")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(Intent(context, LocationFuzedService::class.java))
            else
                context.startService(Intent(context, LocationFuzedService::class.java))
        //}
    }
}