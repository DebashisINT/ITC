package com.progdsmfsm

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.*
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.progdsmfsm.MonitorBroadcast
import com.progdsmfsm.app.AppDatabase
import com.progdsmfsm.app.Pref
import com.progdsmfsm.app.domain.BatteryNetStatusEntity
import com.progdsmfsm.app.domain.NewGpsStatusEntity
import com.progdsmfsm.app.types.FragType
import com.progdsmfsm.app.utils.AppUtils
import com.progdsmfsm.app.utils.FTStorageUtils
import com.progdsmfsm.app.utils.Toaster
import com.progdsmfsm.features.commondialogsinglebtn.CommonDialogSingleBtn
import com.progdsmfsm.features.commondialogsinglebtn.OnDialogClickListener
import com.progdsmfsm.features.dashboard.presentation.DashboardActivity
import com.progdsmfsm.features.location.LocationFuzedService
import com.progdsmfsm.features.location.LocationJobService
import com.progdsmfsm.features.logoutsync.presentation.LogoutSyncFragment
import com.progdsmfsm.mappackage.SendBrod
import timber.log.Timber
import java.util.*

class MonitorService:Service() {
    private val monitorNotiID = 201
    private var monitorBroadcast : MonitorBroadcast? = null
    var powerSaver:Boolean = false
    var isFirst:Boolean = true

    var timer : Timer? = null
    private val POWER_SAVE_MODE_SETTING_NAMES = arrayOf(
            "SmartModeStatus", // huawei setting name
            "POWER_SAVE_MODE_OPEN" // xiaomi setting name
    )

    @SuppressLint("NewApi")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

//        if (intent != null) {
//            val action = intent.action
//            if (action != null) {
//                if (action == CustomConstants.START_MONITOR_SERVICE) {
//                    serviceStatusActionable()
//                } else if (action == CustomConstants.STOP_MONITOR_SERVICE) {
//                    //stopMonitorService()
//                }
//            }
//        }
//        return super.onStartCommand(intent, flags, startId)

        timer = Timer()
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                println("MonitorService_abc - 3 sec method");
                serviceStatusActionable()

            }
        }
        timer!!.schedule(task, 0, 10000)

        // 15 mins is 60000 * 15


        // 15 mins is 60000 * 15R
        return START_STICKY
    }

    fun serviceStatusActionable(){

        try{
            Timber.d("MonitorService running : Time :" + AppUtils.getCurrentDateTime())
            if (FTStorageUtils.isMyServiceRunning(LocationFuzedService::class.java, this)) {
                Timber.d("MonitorService loc service check service running : Time :" + AppUtils.getCurrentDateTime())
            }else{
                Timber.d("MonitorService loc service check service not running : Time :" + AppUtils.getCurrentDateTime())
            }
            return
        }catch (ex:Exception){
            ex.printStackTrace()
            return
        }


        //Timber.d("MonitorService  serviceStatusActionable " + " Time :" + AppUtils.getCurrentDateTime() + " user_id ${Pref.user_id}")
        Log.e("MonitorService_abc", "startabc" )
        monitorBroadcast=MonitorBroadcast()

        var powerMode:String = ""
        val powerManager = this.getSystemService(POWER_SERVICE) as PowerManager
       if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            if(powerManager.isPowerSaveMode){
                Pref.PowerSaverStatus = "On"
                powerMode = "Power Save Mode ON"

                println("MonitorService_abc - Power Save Mode ON");

                Handler(Looper.getMainLooper()).postDelayed({
                    println("MonitorService_abc - Power Save Mode ON - Post delayed");
                    if(Pref.GPSAlertGlobal){
                        if(Pref.GPSAlert){
                            SendBrod.sendBrod(this)
                        }else{
                            if(!Pref.isLocFuzedBroadPlaying){
                                SendBrod.stopBrod(this)
                            }
                        }
                    }else{
                        if(!Pref.isLocFuzedBroadPlaying){
                            SendBrod.stopBrod(this)
                        }
                    }

                }, 500)

                powerSaver=true
                //sendGPSOffBroadcast()
            }else{
                Pref.PowerSaverStatus = "Off"
                powerMode = "Power Save Mode OFF"
                powerSaver=false
                Handler(Looper.getMainLooper()).postDelayed({
                    println("MonitorService_abc - Power Save Mode ON - Post delayed");
                    if(!Pref.isLocFuzedBroadPlaying){
                        SendBrod.stopBrod(this)
                    }

                }, 500)
                //cancelGpsBroadcast()
            }
        }

  /*      var manu= Build.MANUFACTURER.toUpperCase(Locale.getDefault())
        if(manu.equals("XIAOMI")){
            if(isPowerSaveModeCompat(this) ){
                powerMode = "Power Save Mode ON"
                if(Pref.GPSAlertGlobal){
                    if(Pref.GPSAlert){
                        SendBrod.sendBrod(this)
                    }
                }
            }else{
                powerMode = "Power Save Mode OFF"
                SendBrod.stopBrod(this)
            }
        }*/

        val newNetStatusObj = NewGpsStatusEntity()
        if(shouldShopActivityUpdate()){
            newNetStatusObj.date_time = AppUtils.getCurrentDateTime()
            newNetStatusObj.network_status = if(AppUtils.isOnline(this)) "Online" else "Offline"
            if (FTStorageUtils.isMyServiceRunning(LocationFuzedService::class.java, this)) {
                //Timber.d("MonitorService LocationFuzedService : " + "true" + "," + " Time :" + AppUtils.getCurrentDateTime())
                //Timber.d("MonitorService Power Save Mode Status : " + powerMode + "," + " Time :" + AppUtils.getCurrentDateTime())
                /*if(powerSaver){
                    sendGPSOffBroadcast()
                }else{
                    cancelGpsBroadcast()
                }*/
                newNetStatusObj.gps_service_status = "Started"
            }else{
                newNetStatusObj.gps_service_status = "Stopped"
                if (!FTStorageUtils.isMyServiceRunning(LocationFuzedService::class.java, this)) {
                    restartLocationService()
                }
                Timber.d("MonitorService LocationFuzedService : " + "false" + "," + " Time :" + AppUtils.getCurrentDateTime())
                Timber.d("MonitorService  Power Save Mode Status : " + powerMode + "," + " Time :" + AppUtils.getCurrentDateTime())
                Timber.d("Monitor Service Stopped" + "" + "," + " Time :" + AppUtils.getCurrentDateTime())
                if(!isFirst){
                    Log.e("abc", "abc stoptimer" )
                    timer!!.cancel()
                }
                isFirst=false
            }

            Log.e("inside outside shouldGpsNetSyncDuration", AppUtils.getCurrentDateTime() )
            if (shouldGpsNetSyncDuration() && !Pref.GPSNetworkIntervalMins.equals("0")) {
                Log.e("inside shouldGpsNetSyncDuration", AppUtils.getCurrentDateTime() )
                AppDatabase.getDBInstance()?.newGpsStatusDao()?.insert(newNetStatusObj)
            }

        }

    }
    private fun shouldGpsNetSyncDuration(): Boolean {
        AppUtils.changeLanguage(this,"en")

        var t = Math.abs(System.currentTimeMillis() - Pref.prevGpsNetSyncTimeStamp)

        return if (Math.abs(System.currentTimeMillis() - Pref.prevGpsNetSyncTimeStamp) > 1000 * 60 * Pref.GPSNetworkIntervalMins.toInt()) {
            Pref.prevGpsNetSyncTimeStamp = System.currentTimeMillis()
            //changeLocale()
            true
            //server timestamp is within 10 minutes of current system time
        } else {
            //changeLocale()
            false
        }
    }

    private fun changeLocale() {
        val intent = Intent()
        intent.action = "CHANGE_LOCALE_BROADCAST"
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    fun sendGPSOffBroadcast(){
        if(Pref.user_id.toString().length > 0){
            Timber.d("MonitorService Called for Battery Broadcast :  Time :" + AppUtils.getCurrentDateTime())
            //var notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            //notificationManager.cancel(monitorNotiID)
            MonitorBroadcast.isSound=Pref.GPSAlertwithSound
            var intent: Intent = Intent(this, MonitorBroadcast::class.java)
            intent.putExtra("notiId", monitorNotiID)
            intent.putExtra("fuzedLoc", "Fuzed Stop.")
            this.sendBroadcast(intent)
        }
    }


    fun cancelGpsBroadcast(){
        if (monitorNotiID != 0){
            if(MonitorBroadcast.player!=null){
                MonitorBroadcast.player.stop()
                MonitorBroadcast.player=null
                MonitorBroadcast.vibrator.cancel()
                MonitorBroadcast.vibrator=null
            }
            var notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(monitorNotiID)
        }
    }

    private fun isPowerSaveModeCompat(context: Context): Boolean {
        for (name in POWER_SAVE_MODE_SETTING_NAMES) {
            val mode = Settings.System.getInt(context.contentResolver, name, -1)
            if (mode != -1) {
                return POWER_SAVE_MODE_VALUES[Build.MANUFACTURER.toUpperCase(Locale.getDefault())] == mode
            }
        }
        return false
    }

    private val POWER_SAVE_MODE_VALUES = mapOf(
            "HUAWEI" to 4,
            "XIAOMI" to 1
    )

    override fun stopService(name: Intent?): Boolean {
        stopForeground(true)
        stopSelf()
        return super.stopService(name)
    }

    override fun onDestroy() {
        try{
            super.onDestroy()
            stopForeground(true)
            stopSelf()
            timer!!.cancel()
        }catch (ex:Exception){
            ex.printStackTrace()
        }
    }

    @SuppressLint("NewApi")
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        //serviceStatusActionable()
    }

    override fun onBind(p0: Intent?): IBinder? {
        throw UnsupportedOperationException("Not Yet Implemented")
    }

    private fun checkGpsStatus() {
        val locationManager: LocationManager =
                getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

        } else {

        }
    }


    fun shouldShopActivityUpdate(): Boolean {
        return if (Math.abs(System.currentTimeMillis() - Pref.prevShopActivityTimeStampMonitorService) > 20000) {
            Pref.prevShopActivityTimeStampMonitorService = System.currentTimeMillis()
            true
            //server timestamp is within 5 minutes of current system time
        } else {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun restartLocationService() {
        try {
            if(Pref.IsLeavePressed== true && Pref.IsLeaveGPSTrack == false){
                return
            }
            val serviceLauncher = Intent(this, LocationFuzedService::class.java)
            if (Pref.user_id != null && Pref.user_id!!.isNotEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                    val componentName = ComponentName(this, LocationJobService::class.java)
                    val jobInfo = JobInfo.Builder(12, componentName)
                        //.setRequiresCharging(true)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        //.setRequiresDeviceIdle(true)
                        .setOverrideDeadline(1000)
                        .build()

                    val resultCode = jobScheduler.schedule(jobInfo)

                    if (resultCode == JobScheduler.RESULT_SUCCESS) {
                        Timber.d("===============================From MonitorS LocationFuzedService   Job scheduled (Base Activity) " + AppUtils.getCurrentDateTime() + "============================")
                    } else {
                        Timber.d("=====================From MonitorS LocationFuzedService Job not scheduled (Base Activity) " + AppUtils.getCurrentDateTime() + "====================================")
                    }
                } else
                    startService(serviceLauncher)
            } else {
                /*stopService(serviceLauncher)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                    jobScheduler.cancelAll()
                    Timber.d("===============================From MonitorS LocationFuzedService Job scheduler cancel (Base Activity)" + AppUtils.getCurrentDateTime() + "============================")
                }

                AlarmReceiver.stopServiceAlarm(this, 123)
                Timber.d("===========From MonitorS LocationFuzedService Service alarm is stopped (Base Activity)================")*/
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}