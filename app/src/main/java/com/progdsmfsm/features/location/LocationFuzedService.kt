package com.progdsmfsm.features.location

import android.annotation.SuppressLint
import android.app.*
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.location.GnssStatus
import android.location.GpsStatus
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.progdsmfsm.MonitorBroadcast
import com.progdsmfsm.R
import com.progdsmfsm.app.*
import com.progdsmfsm.app.Pref.tempDistance
import com.progdsmfsm.app.domain.*
import com.progdsmfsm.app.utils.AppUtils
import com.progdsmfsm.app.utils.AppUtils.Companion.getDateTimeFromTimeStamp
import com.progdsmfsm.app.utils.AppUtils.Companion.isLocationActivityUpdating
import com.progdsmfsm.app.utils.FTStorageUtils
import com.progdsmfsm.app.utils.NotificationUtils
import com.progdsmfsm.base.BaseResponse
import com.progdsmfsm.base.presentation.BaseActivity
import com.progdsmfsm.base.presentation.BaseActivity.Companion.isMeetingUpdating
import com.progdsmfsm.base.presentation.BaseActivity.Companion.isShopActivityUpdating
import com.progdsmfsm.features.addshop.api.AddShopRepositoryProvider
import com.progdsmfsm.features.addshop.model.AddShopRequestCompetetorImg
import com.progdsmfsm.features.addshop.model.AddShopRequestData
import com.progdsmfsm.features.addshop.model.AddShopResponse
import com.progdsmfsm.features.averageshop.api.ShopActivityRepositoryProvider
import com.progdsmfsm.features.averageshop.model.ShopActivityRequest
import com.progdsmfsm.features.averageshop.model.ShopActivityResponse
import com.progdsmfsm.features.averageshop.model.ShopActivityResponseDataList
import com.progdsmfsm.features.commondialogsinglebtn.CommonDialogSingleBtn
import com.progdsmfsm.features.dashboard.presentation.DashboardActivity
import com.progdsmfsm.features.dashboard.presentation.DashboardFragment
import com.progdsmfsm.features.dashboard.presentation.SystemEventReceiver
import com.progdsmfsm.features.dashboard.presentation.api.ShopVisitImageUploadRepoProvider
import com.progdsmfsm.features.dashboard.presentation.model.ShopVisitImageUploadInputModel
import com.progdsmfsm.features.gpsstatus.GpsReceiver
import com.progdsmfsm.features.gpsstatus.LocationCallBack
import com.progdsmfsm.features.location.LocationWizard.Companion.NEARBY_RADIUS
import com.progdsmfsm.features.location.api.LocationRepoProvider
import com.progdsmfsm.features.location.ideallocapi.IdealLocationRepoProvider
import com.progdsmfsm.features.location.model.*
import com.progdsmfsm.features.location.shopRevisitStatus.ShopRevisitStatusRepositoryProvider
import com.progdsmfsm.features.location.shopdurationapi.ShopDurationRepositoryProvider
import com.progdsmfsm.features.orderhistory.api.LocationUpdateRepositoryProviders
import com.progdsmfsm.features.orderhistory.model.LocationData
import com.progdsmfsm.features.orderhistory.model.LocationUpdateRequest
import com.progdsmfsm.widgets.AppCustomTextView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login_new.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.*
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by riddhi on 10/11/17.
 */
// Rev 1.0 Suman 06-05-2024 Suman LocationFuzedService mantis 27335
// Rev 2.0 LocationFuzedService v 4.4.7 Suman 08/05/2024 mantis 0027427 location sync update
class LocationFuzedService : Service(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,
        OnCompleteListener<Void>, GpsStatus.Listener {
    override fun onComplete(p0: Task<Void>) {

    }

    var mGoogleAPIClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mLocationProvider: FusedLocationProviderApi? = null
    var mLastLocation: Location? = null
    private val TAG: String = "MyLocationService"
    //var isLocationActivityUpdating: Boolean = false

    private var isLocalShopActivityUpdating: Boolean = false
    var isCheckingNearBy: Boolean = false
    lateinit var mWakeLock: PowerManager.WakeLock
    private var LOCATION_ACTIVITY_INTERVAL = 2 //20 // unit as minutes
    private var gpsStatus = true
    private var i = 0
    private var sameLoc: Long = 0
    private var diffLoc: Long = 0
    private var gpsReceiver: GpsReceiver? = null

    val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private var assumedDistanceCover: Double = AppUtils.maxDistance.toDouble()
    private var acuurateLat = 0.0
    private var acuurateLong = 0.0
    private var previousTimeStamp = 0L
    private var mLastLocationForAssumtion: Location? = null
    private var shopId = ""
    private var shopVisitDate = ""
    private var previousShopVisitDateNumber = 0L
    private var lastLat = 0.0
    private var lastLng = 0.0
    private var startIdleLat = 0.0
    private var startIdleLong = 0.0
    private var endIdleLat = 0.0
    private var endIdleLong = 0.0
    private var previousIdleTimeStamp = 0L
    private var newIdleTimeStamp = 0L
    private var startIdleDateTime = ""
    private var endIdleDateTime = ""
    private var accuracyStatus = ""
    private var prevRevisitTimeStamp = 0L
    private var shop_id = ""

    private var monitorBroadcast : MonitorBroadcast? = null
    private val monitorNotiID = 201

    private val eventReceiver: SystemEventReceiver by lazy {
        SystemEventReceiver()
    }

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate" + " , " + " Time :" + AppUtils.getCurrentDateTime())

        registerReceiver(eventReceiver, IntentFilter().apply {
            addAction("android.intent.action.AIRPLANE_MODE")
            addAction("android.intent.action.BOOT_COMPLETED")
            addAction("android.intent.action.ACTION_SHUTDOWN")
        })


        mGoogleAPIClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        mLocationProvider = LocationServices.FusedLocationApi
        mLocationRequest = LocationRequest()
        /*mLocationRequest!!.interval = 1000 * 60 * 1  // 1 min
        mLocationRequest!!.fastestInterval = 1000 * 60 * 1 // 1 min*/
        /*mLocationRequest!!.interval = 1000 * 30  // 30 secs
        mLocationRequest!!.fastestInterval = 1000 * 30 // 30 secs*/
        mLocationRequest!!.interval = (1000 * Pref.locationTrackInterval.toInt()).toLong()
        mLocationRequest!!.fastestInterval = (1000 * Pref.locationTrackInterval.toInt()).toLong()
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        //mLocationRequest!!.smallestDisplacement = 1.0f

//        populateandAddGeofences()

        monitorBroadcast= MonitorBroadcast()

    }

    fun updateNearbyShopLocationData(shopName: String, shopId: String, localShopId: String) {

        val i = Intent("android.intent.action.NEARBYSHOP")
        i.putExtra("shopName", shopName)
        i.putExtra("shopId", shopId)
        i.putExtra("localShopId", localShopId)

        this.sendBroadcast(i)
    }

    @SuppressLint("InvalidWakeLockTag", "NewApi")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Timber.d("onStartCommand" + " , " + " Time :" + AppUtils.getCurrentDateTime())

//        val value = intent?.getStringExtra("MyService.data")
//        if (value!=null && value == "UPDATE_FENCE") {
//            Timber.d("UPDATE_FENCE")
//            removeGeofence()
//            populateandAddGeofences()
//        }


        var mgr = getSystemService(Context.POWER_SERVICE) as PowerManager;
        mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock") as PowerManager.WakeLock;
        mWakeLock.acquire()

        System.gc()


        if (intent == null) {
            // do nothing and return
            return START_STICKY
        }
        try {
            var notificationIntent = Intent(this, DashboardActivity::class.java)
            notificationIntent.action = AppConstant.MAIN_ACTION
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)


            var pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, PendingIntent.FLAG_IMMUTABLE)


            var icon = BitmapFactory.decodeResource(resources, R.drawable.ic_add)

            /*var notification = NotificationCompat.Builder(this)
                    .setContentTitle("Tracking System Activated")
                    .setTicker("")
                    .setContentText("")
                    .setSmallIcon(R.drawable.ic_notifications_icon)
                    .setLargeIcon(
                            Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build()
            startForeground(AppConstant.FOREGROUND_SERVICE,
                    notification)*/


            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notificationTitle = "${AppUtils.hiFirstNameText()}, thanks for using FSM App."


            //20-10-2021
            /*var timer : Timer? = null
            timer = Timer()
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                println("\npoooooop"+timer.toString()+"\n");
            }
        }
        timer!!.schedule(task, 0, 30000*1)*/


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                /*val channelId = AppUtils.notificationChannelId

                val channelName = AppUtils.notificationChannelName
                val importance = NotificationManager.IMPORTANCE_HIGH
                val notificationChannel = NotificationChannel(channelId, channelName, importance)
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = applicationContext.getColor(R.color.colorPrimary)
                notificationChannel.enableVibration(true)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                notificationManager.createNotificationChannel(notificationChannel)

                val notification = NotificationCompat.Builder(this)
                        .setContentTitle(notificationTitle)
                        .setTicker("")
                        .setContentText("")
                        .setSmallIcon(R.drawable.ic_notifications_icon)
                        .setLargeIcon(
                                Bitmap.createScaledBitmap(icon, 128, 128, false))
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .setChannelId(channelId)
                        .build()

                //notificationManager.notify(randInt, notificationBuilder.build());

                //20-10-2021
                startForeground(AppConstant.FOREGROUND_SERVICE, notification)*/

                //new test code
                var channelIDd = AppUtils.notificationChannelId
                var channelNamee = AppUtils.notificationChannelName
                val importancee = NotificationManager.IMPORTANCE_HIGH
                val notiManagerr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if(notiManagerr.getNotificationChannel(channelIDd) == null){
                    val notiChannell = NotificationChannel(channelIDd, channelNamee, importancee).apply {
                        enableLights(true)
                        lightColor = applicationContext.getColor(R.color.colorPrimary)
                        enableVibration(true)
                        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    }
                    notiManagerr.createNotificationChannel(notiChannell)
                }

                val notificationn = NotificationCompat.Builder(this, channelIDd)
                    .setContentTitle(notificationTitle)
                    .setTicker("")
                    .setContentText("")
                    .setSmallIcon(R.drawable.ic_notifications_icon)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build()
                //startForeground(AppConstant.FOREGROUND_SERVICE, notificationn, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
                startForeground(AppConstant.FOREGROUND_SERVICE, notificationn)

            } else {
                val notification = NotificationCompat.Builder(this)
                        .setContentTitle(notificationTitle)
                        .setTicker("")
                        .setContentText("")
                        .setSmallIcon(R.drawable.ic_notifications_icon)
                        .setLargeIcon(
                                Bitmap.createScaledBitmap(icon, 128, 128, false))
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .build()

                //notificationManager.notify(randInt, notificationBuilder.build())

                //20-10-2021
                startForeground(AppConstant.FOREGROUND_SERVICE, notification)
            }

            gpsReceiver = GpsReceiver(object : LocationCallBack {
                override fun onLocationTriggered(status: Boolean) {
                    //Location state changed
                    if (gpsStatus != status) {
                        gpsStatus = status
//                        Log.e(TAG, "GPS STATUS: " + status)
                        Timber.d("GPS STATUS : " + status + "," + " Time :" + AppUtils.getCurrentDateTime())


                        if (!gpsStatus) {
                            Timber.d("LocationFuzedService GPS turn off : " + status + "," + " Time :" + AppUtils.getCurrentDateTime())
                            sendGPSOffBroadcast()
                            if (!FTStorageUtils.isMyServiceRunning(LocationFuzedService::class.java, this@LocationFuzedService)) {
                                serviceStatusActionable()
                            }
                        } else {
                            if (monitorNotiID != 0){
                                Pref.isLocFuzedBroadPlaying=false
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




                        Handler().postDelayed(Runnable {
                            calculategpsStatus(gpsStatus)
                        }, 1000)
                    }
                }
            })

            try {
                //Register GPS Status Receiver
                if (!TextUtils.isEmpty(Pref.user_id))
                    registerReceiver(gpsReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            registerGpsStatusListener()

            if (mGoogleAPIClient == null) {

                mGoogleAPIClient = GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build()

            } else {
                Log.e(TAG, "mGoogleAPIClient connected: $mGoogleAPIClient")
                mGoogleAPIClient?.connect()
            }

            //showOrderCollectionAlert()

            return START_STICKY
        } catch (e: Exception) {
            e.printStackTrace()
            return START_STICKY
        }
    }

    private fun calculategpsStatus(gpsStatus: Boolean) {

        if (!AppUtils.isOnReceived) {
            AppUtils.isOnReceived = true

            if (!gpsStatus) {
                //Toast.makeText(context, "GPS is disabled!", Toast.LENGTH_LONG).show()
                if (!AppUtils.isGpsOffCalled) {
                    AppUtils.isGpsOffCalled = true
                    Log.e("GpsLocationReceiver", "===========GPS is disabled=============")
                    AppUtils.gpsOffTime = dateFormat.parse(/*"18:14:55"*/AppUtils.getCurrentTime()).time
                    AppUtils.gpsDisabledTime = AppUtils.getCurrentTimeWithMeredian()
                    Log.e("GpsLocationReceiver", "gpsOffTime------------------> " + AppUtils.getTimeInHourMinuteFormat(AppUtils.gpsOffTime))

                    val local_intent = Intent()
                    local_intent.action = AppUtils.gpsDisabledAction
                    sendBroadcast(local_intent)
                }
            } else {
                //Toast.makeText(context, "GPS is enabled!", Toast.LENGTH_LONG).show()
                if (AppUtils.isGpsOffCalled) {
                    AppUtils.isGpsOffCalled = false
                    Log.e("GpsLocationReceiver", "===========GPS is enabled================")
                    AppUtils.gpsOnTime = dateFormat.parse(AppUtils.getCurrentTime()).time
                    AppUtils.gpsEnabledTime = AppUtils.getCurrentTimeWithMeredian()
                    Log.e("GpsLocationReceiver", "gpsOnTime---------------------> " + AppUtils.getTimeInHourMinuteFormat(AppUtils.gpsOnTime))

                    val local_intent = Intent()
                    local_intent.action = AppUtils.gpsEnabledAction
                    sendBroadcast(local_intent)
                }
            }

            val performance = AppDatabase.getDBInstance()!!.performanceDao().getTodaysData(AppUtils.getCurrentDateForShopActi())
            if (performance == null) {
                if ((AppUtils.gpsOnTime - AppUtils.gpsOffTime) > 0) {
                    val performanceEntity = PerformanceEntity()
                    performanceEntity.date = AppUtils.getCurrentDateForShopActi()
                    performanceEntity.gps_off_duration = (AppUtils.gpsOnTime - AppUtils.gpsOffTime).toString()
                    Log.e("GpsLocationReceiver", "duration----------------> " + AppUtils.getTimeInHourMinuteFormat(AppUtils.gpsOnTime - AppUtils.gpsOffTime))
                    AppDatabase.getDBInstance()!!.performanceDao().insert(performanceEntity)
                    saveGPSStatus((AppUtils.gpsOnTime - AppUtils.gpsOffTime).toString())
                    AppUtils.gpsOnTime = 0
                    AppUtils.gpsOffTime = 0
                }
            } else {
                if (TextUtils.isEmpty(performance.gps_off_duration)) {
                    if ((AppUtils.gpsOnTime - AppUtils.gpsOffTime) > 0) {
                        AppDatabase.getDBInstance()!!.performanceDao().updateGPSoffDuration((AppUtils.gpsOnTime - AppUtils.gpsOffTime).toString(), AppUtils.getCurrentDateForShopActi())
                        Log.e("GpsLocationReceiver", "duration----------> " + AppUtils.getTimeInHourMinuteFormat(AppUtils.gpsOnTime - AppUtils.gpsOffTime))
                        saveGPSStatus((AppUtils.gpsOnTime - AppUtils.gpsOffTime).toString())
                        AppUtils.gpsOnTime = 0
                        AppUtils.gpsOffTime = 0
                    }
                } else {
                    if ((AppUtils.gpsOnTime - AppUtils.gpsOffTime) > 0) {
                        val duration = AppUtils.gpsOnTime - AppUtils.gpsOffTime
                        val totalDuration = performance.gps_off_duration?.toLong()!! + duration
                        Log.e("GpsLocationReceiver", "duration-------> " + AppUtils.getTimeInHourMinuteFormat(totalDuration))
                        AppDatabase.getDBInstance()!!.performanceDao().updateGPSoffDuration(totalDuration.toString(), AppUtils.getCurrentDateForShopActi())
                        saveGPSStatus(duration.toString())
                        AppUtils.gpsOnTime = 0
                        AppUtils.gpsOffTime = 0
                    }
                }
            }
            AppUtils.isOnReceived = false
        }
    }

    private fun saveGPSStatus(duration: String) {
        val gpsStatus = GpsStatusEntity()
        val random = Random()
        val m = random.nextInt(9999 - 1000) + 1000

        gpsStatus.gps_id = Pref.user_id + "_" + m + m
        gpsStatus.date = AppUtils.getCurrentDateForShopActi()
        gpsStatus.gps_off_time = AppUtils.gpsDisabledTime
        gpsStatus.gps_on_time = AppUtils.gpsEnabledTime
        gpsStatus.duration = duration
        AppDatabase.getDBInstance()!!.gpsStatusDao().insert(gpsStatus)
        AppUtils.gpsDisabledTime = ""
        AppUtils.gpsEnabledTime = ""
        AppUtils.isGpsReceiverCalled = false
    }

    private fun showOrderCollectionAlert() {

        //Handler().postDelayed(Runnable {

        /*Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {

                if (!Pref.isAutoLogout) {

                    try {
                        val currentTime = System.currentTimeMillis()

                        var isOrderAdded = false
                        var isCollectionAdded = false

                        val todaysOrderList = AppDatabase.getDBInstance()!!.orderDetailsListDao().getListAccordingDate(AppUtils.getCurrentDate())

                        if (todaysOrderList != null && todaysOrderList.isNotEmpty()) {
                            val lastOrderTime = AppUtils.getTimeStampFromValidDetTime(todaysOrderList[0].date!!)

                            val diffInSec = (currentTime - lastOrderTime) / 1000

                            if (diffInSec <= 120) {
                                isOrderAdded = true
                            }
                        }

                        val todaysCollectionList = AppDatabase.getDBInstance()!!.collectionDetailsDao().getDateWiseCollection(AppUtils.getCurrentDate())

                        if (todaysCollectionList != null && todaysCollectionList.isNotEmpty()) {
                            if (!TextUtils.isEmpty(todaysCollectionList[0].only_time)) {
                                val lastCollectionTime = AppUtils.getTimeStampFromValidDetTime(AppUtils.getCurrentDateFormatInTa(todaysCollectionList[0].date!!) +
                                        "T" + todaysCollectionList[0].only_time)

                                val diffInSec = (currentTime - lastCollectionTime) / 1000

                                if (diffInSec <= 120) {
                                    isCollectionAdded = true
                                }
                            }
                        }

                        if (!TextUtils.isEmpty(Pref.user_id) && Pref.isAddAttendence && (!isOrderAdded || !isCollectionAdded)) {
                            //showOrderCollectionAlert(isOrderAdded, isCollectionAdded)

                            val inten = Intent()
                            inten.action = "ALERT_RECIEVER_BROADCAST"
                            inten.putExtra("isOrderAdded", isOrderAdded)
                            inten.putExtra("isCollectionAdded", isCollectionAdded)

                            LocalBroadcastManager.getInstance(this@LocationFuzedService).sendBroadcast(inten)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }, 0, 120 * 1000)*/
        //}, 50000)
    }

    @SuppressLint("MissingPermission")
    override fun onConnected(@Nullable bundle: Bundle?) {
        Log.e(TAG, "onConnected: ")
        val lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleAPIClient!!)
        if (lastLocation != null && lastLocation.latitude != null && lastLocation.latitude != 0.0) {
            AppUtils.mLocation = lastLocation
            Pref.current_latitude = lastLocation.latitude.toString()
            Pref.current_longitude = lastLocation.longitude.toString()
        }

        requestLocationUpdates()
    }

    private fun requestLocationUpdates() {

        Log.e(TAG, "RequestLocationUpdates: ")

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleAPIClient!!, mLocationRequest!!, this) //getting error here..for casting..!

    }

    override fun onConnectionSuspended(i: Int) {

    }

    override fun onConnectionFailed(@NonNull connectionResult: ConnectionResult) {

    }

    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private var previousOnLocationChangedTimeStamp = 0L
    private var currentOnLocationChangedTimeStamp = 0L

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onLocationChanged(location: Location) {
        //Pref.IsShowDayStart = true
        //Pref.DayStartMarked = true

        Timber.d("onLocationChanged ")
        try{
            if(location!=null){
                AppUtils.mLocation = location
                Pref.current_latitude = location.latitude.toString()
                Pref.current_longitude = location.longitude.toString()
                //lastLat = location.latitude
                //lastLng = location.longitude
                Timber.d("onLocationChanged : loc_update : lat - ${Pref.current_latitude.toString()} long - ${Pref.current_longitude.toString()} " + AppUtils.getCurrentDateTime())
            }
        }catch (ex:Exception){
            ex.printStackTrace()
            Timber.d("onLocationChanged : loc_update error" + AppUtils.getCurrentDateTime())
        }

        if(Pref.CommonAINotification){
            showAttendNotification()
            Handler().postDelayed(Runnable {
                showDayEndNotification()
            }, 100)
        }

        //Timber.d("onLocationChanged : LocationFuzedService " + AppUtils.getCurrentDateTime())

        if (Pref.login_date != AppUtils.getCurrentDateChanged()) {
            Timber.e("=======Auto logout scenario (Location Fuzed Service)==========")
            //Pref.prevOrderCollectionCheckTimeStamp = 0L
            resetData()
            return
        }

        if(Pref.IsLeavePressed==true && Pref.IsLeaveGPSTrack == false){
            return
        }

        if(Pref.Show_App_Logout_Notification_Global){
            if(Pref.Show_App_Logout_Notification){
                println("checkForceLogoutNotification() checked LocationFuzerService");
                checkForceLogoutNotification()
            }
        }

        calculateOrderCollectionAlertTime()
        Timber.d("LocationFuzedService : dayStart :  "+Pref.DayStartMarked.toString() + " dayEnd : "+Pref.DayEndMarked.toString()+" sstatus " + AppUtils.getCurrentDateTime())
        if(Pref.IsShowDayStart){
            if(Pref.DayStartMarked){
                //Timber.d("LocationFuzedService : dayStart :  "+Pref.DayStartMarked.toString() + " dayEnd : "+Pref.DayEndMarked.toString()+" " + AppUtils.getCurrentDateTime())

                if(Pref.IsShowMarketSpendTimer){
                    try{
                        if(!Pref.DayStartTime.equals("")){
                            var currentTime = System.currentTimeMillis().toString()
                            var timeDiff = AppUtils.getTimeFromTimeSpan(Pref.DayStartTime,System.currentTimeMillis().toString())
                            timeDiff = timeDiff.split(":").get(0)+":"+timeDiff.split(":").get(1)
                            //DashboardFragment.timerTV.text = "${AppUtils.getMinuteFromTimeStamp(Pref.DayStartTime,System.currentTimeMillis().toString())} + $timeDiff"
                            DashboardFragment.timerTV.text = "$timeDiff"
                        }
                    }catch (ex:Exception){
                        Timber.d("ex timer text")
                    }
                }



                /*try {
                    if (Pref.current_latitude == location.latitude.toString() && Pref.current_longitude == location.longitude.toString()) {
                        if (sameLoc == 0L) {
                            sameLoc = dateFormat.parse(AppUtils.getCurrentTime()).time
                            Log.e(TAG, "same location-------> " + AppUtils.getTimeInHourMinuteFormat(sameLoc))
                        }
                    } else {
                        diffLoc = dateFormat.parse(AppUtils.getCurrentTime()).time
                        Log.e(TAG, "different location----------> " + AppUtils.getTimeInHourMinuteFormat(diffLoc))
                    }

                    val performance = AppDatabase.getDBInstance()!!.performanceDao().getTodaysData(AppUtils.getCurrentDateForShopActi())
                    if (performance == null) {

                        if ((diffLoc - sameLoc) > 0 && sameLoc > 0) {
                            val performanceEntity = PerformanceEntity()
                            performanceEntity.date = AppUtils.getCurrentDateForShopActi()
                            performanceEntity.ideal_duration = (diffLoc - sameLoc).toString()
                            Log.e(TAG, "duration------------------> " + AppUtils.getTimeInHourMinuteFormat(diffLoc - sameLoc))
                            AppDatabase.getDBInstance()!!.performanceDao().insert(performanceEntity)
                            diffLoc = 0
                            sameLoc = 0
                        }
                    } else {
                        if (TextUtils.isEmpty(performance.ideal_duration)) {
                            if ((diffLoc - sameLoc) > 0 && sameLoc > 0) {
                                AppDatabase.getDBInstance()!!.performanceDao().updateIdealDuration((diffLoc - sameLoc).toString(), AppUtils.getCurrentDateForShopActi())
                                Log.e(TAG, "duration------------------> " + AppUtils.getTimeInHourMinuteFormat(diffLoc - sameLoc))
                                diffLoc = 0
                                sameLoc = 0
                            }
                        } else {
                            if ((diffLoc - sameLoc) > 0 && sameLoc > 0) {
                                val duration = diffLoc - sameLoc
                                val totalDuration = performance.ideal_duration?.toLong()!! + duration
                                AppDatabase.getDBInstance()!!.performanceDao().updateIdealDuration(totalDuration.toString(), AppUtils.getCurrentDateForShopActi())
                                Log.e(TAG, "duration------------------> " + AppUtils.getTimeInHourMinuteFormat(diffLoc - sameLoc))
                                diffLoc = 0
                                sameLoc = 0
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }*/

                Timber.e(TAG, "ON LOCATION CHANGED: ")
                //location.accuracy = 200f
                Timber.d("onLocationChanged : " + "Location : " + location.latitude + "," + location.longitude + " Time : " + AppUtils.getCurrentDateTime() + ", Accuracy :" + location.accuracy)
//        System.gc()
//        trackDeviceMemory()

                if (location.isFromMockProvider)
                    Timber.e("==================Mock Location is on (Location Fuzed Serive)====================")
                else
                    Timber.e("==================Mock Location is off (Location Fuzed Serive)====================")

                /*try {

                    val currentTimeStamp = LocationWizard.getTimeStamp()

                    if (accuracyStatus.equals("inaccurate", ignoreCase = true)) {
                        val list = AppDatabase.getDBInstance()!!.inaccurateLocDao().all
                        var inaccurateTimeStamp = ""

                        if (list != null && list.size > 0) {
                            inaccurateTimeStamp = list[list.size - 1].timestamp!!

                            val timeStampLong = inaccurateTimeStamp.toLong()

                            if (inaccurateTimeStamp >= currentTimeStamp) {
                                Timber.e("=====Invalid inaccurate onlocationchange=======")
                                return
                            }
                        }
                    } else {
                        val accurateList = AppDatabase.getDBInstance()!!.userLocationDataDao().all
                        var accurateTimeStamp = ""
                        if (accurateList != null && accurateList.size > 0) {
                            accurateTimeStamp = accurateList[accurateList.size - 1].timestamp

                            val timeStampLong = accurateTimeStamp.toLong()

                            if (accurateTimeStamp >= currentTimeStamp) {
                                Timber.e("=====Invalid accurate onlocationchange=======")
                                return
                            }
                        }
                    }


                    if (currentOnLocationChangedTimeStamp != 0L && currentOnLocationChangedTimeStamp != previousOnLocationChangedTimeStamp) {
                        Timber.e("onLocationChanged: ===previousOnLocationChangedTimeStamp is not equal to old currentOnLocationChangedTimeStamp===")
                        return
                    }

                    currentOnLocationChangedTimeStamp = AppUtils.getTimeStamp(AppUtils.getCurrentDateTime()) //System.currentTimeMillis()

                    if (previousOnLocationChangedTimeStamp != 0L) {
                        //synchronized(this) {

                        val interval = currentOnLocationChangedTimeStamp - previousOnLocationChangedTimeStamp
                        val intervalInSec = (interval / 1000)

                        Timber.e("onLocationChanged previousOnLocationChangedTimeStamp====> $previousOnLocationChangedTimeStamp")
                        Timber.e("onLocationChanged currentOnLocationChangedTimeStamp====> $currentOnLocationChangedTimeStamp")
                        Timber.e("onLocationChanged interval====> $intervalInSec sec(s)")

                        previousOnLocationChangedTimeStamp = currentOnLocationChangedTimeStamp

                        Timber.e("onLocationChanged new previousOnLocationChangedTimeStamp====> $previousOnLocationChangedTimeStamp")

                        if (interval <= 40 * 1000) {
                            return
                        }
                        //}
                    } else
                        previousOnLocationChangedTimeStamp = currentOnLocationChangedTimeStamp


                } catch (e: Exception) {
                    e.printStackTrace()
                }*/

                if (Pref.willAutoRevisitEnable){
                    if(Pref.IsShowDayStart){
                        if(Pref.DayStartMarked){
                            //checkAutoRevisit()
                            checkAutoRevisitAll()
                        }
                    }
                }
                else
                    Timber.e("====================Auto Revisit Disable (Location Fuzed Service)====================")

                //saveAllLocation(location)
                checkMeetingDistance()


                /*Sync all data*/
                if(Pref.IsShowDayStart){
                    if(Pref.DayStartMarked){
                        if(AppUtils.isOnline(this)){
                            syncLocationActivity()
                        }
                    }
                }

                //if (!BaseActivity.isApiInitiated)
                if(AppUtils.isOnline(this)){
                    syncShopListOnebyOne()
                }

                if(shouldEndShopDurationUpdate()){
                    Timber.d("endShopDuration : if ${AppUtils.getCurrentDateTime()}")
                    endShopDuration()
                }else{
                    Timber.d("endShopDuration : else ${AppUtils.getCurrentDateTime()}")

                }


                Handler().postDelayed(Runnable {
                    callShopDurationApi()
                 }, 1000)


                /*Handler().postDelayed(Runnable {
                   if(AppUtils.isOnline(this))
                        callShopActivityApiForActivityCheck()
                }, 1000)*/


                //syncShopVisitImage()

                //callCompetetorImgUploadApi()

                syncIdealLocData()

                syncMeetingData()

                if (Pref.isAppInfoEnable) {
                    saveBatteryNetData()

                    syncBatteryNetData()
                }

                if(!Pref.GPSNetworkIntervalMins.equals("0"))
                    syncGpsNetData()



                /*if (location.isFromMockProvider *//*|| AppUtils.areThereMockPermissionApps(this)*//*) {
            Timber.e("==================Mock Location is on (Location Fuzed Serive)====================")
            return
        }

        Timber.e("==================Mock Location is off (Location Fuzed Serive)====================")*/

                Pref.current_latitude = location.latitude.toString()
                Pref.current_longitude = location.longitude.toString()
                AppUtils.mLocation = location
                AppUtils.saveSharedPreferencesLocation(this, location)

                Pref.logout_latitude = location.latitude.toString()
                Pref.logout_longitude = location.longitude.toString()

                showNotification()

                var accuracy = 0f
                accuracy = if (AppUtils.isOnline(this))
                    AppUtils.minAccuracy.toFloat()
                else
                    800f

                /*Discard Data if Inaccurate*/
                if (location.accuracy > accuracy /*&& shouldLocationUpdate()*/) {

                    /*LOCATION_ACTIVITY_INTERVAL = 0
                    updateInaccurateLocation(location)*/

                    accuracyStatus = "inaccurate"

                    Timber.e("=============Inaccurate location (Location Fuzed Service)============")

                    if (!TextUtils.isEmpty(Pref.home_latitude) && !TextUtils.isEmpty(Pref.home_longitude)) {
                        val distance = LocationWizard.getDistance(Pref.home_latitude.toDouble(), Pref.home_longitude.toDouble(), location.latitude, location.longitude)

                        if (distance * 1000 > Pref.homeLocDistance.toDouble()) {
                            calculateInaccurateDistance(location)
                        } else {
                            Timber.e("==========User is at home location (Location Fuzed Service)==========")
                            if (Pref.isAddAttendence)
                                calculateIdleTime(location, "inaccurate")
                            else
                                Timber.e("=====Attendance is not added for today (Inaccurate idle time)======")
                        }
                    } else
                        calculateInaccurateDistance(location)

                    Timber.e("Temp Distance for inaccurate====> $tempDistance")

                    updateInaccurateLocation(location)

                    lastLat = location.latitude
                    lastLng = location.longitude

                    return
                }


//        if (AppUtils.isShopVisited) {

                try{
                    if( Pref.isShopVisited){
                        if (shouldShopDurationComplete()) {

                            val list = AppDatabase.getDBInstance()!!.shopActivityDao().getDurationCalculatedShopForADay(AppUtils.getCurrentDateForShopActi(), false, true)
                            if (list != null && list.isNotEmpty()) {
                                val shopId = list[0].shopid

                                val shop = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopDetail(shopId)

                                val distance = LocationWizard.getDistance(shop.shopLat, shop.shopLong, location.latitude, location.longitude)

                                Timber.e("Location Fuzed Service: Distance between current location & visited shop location====> $distance km(s)")
                                Timber.e("Location Fuzed Service: Gps accuracy====> " + Pref.gpsAccuracy + " m(s)")

                                if (distance * 1000 > Pref.gpsAccuracy.toInt()) {
                                    endShopDuration(shopId!!)
                                }
                            }
                        }
                    }
                }catch (ex:Exception){

                }



                Timber.e("Temp Distance for accurate====> $tempDistance")

                lastLat = location.latitude
                lastLng = location.longitude

                Pref.latitude = location.latitude.toString()
                Pref.longitude = location.longitude.toString()

                /*if (Pref.login_date != AppUtils.getCurrentDateChanged())
                    return


                Pref.latitude = location.latitude.toString()
                Pref.longitude = location.longitude.toString()

                if (Pref.user_id.isNullOrEmpty())
                    return*/

                //cancelShopDuration()
                accuracyStatus = "accurate"
                continueToAccurateFlow(location)

                /*if (mLastLocation == null) {
                    mLastLocation = location
                    Pref.prevTimeStamp = System.currentTimeMillis()
                    updateLocation(mLastLocation!!, location)
                    LOCATION_ACTIVITY_INTERVAL = 2
                } else if (shouldLocationUpdate()) {
                    updateLocation(mLastLocation!!, location)
                    mLastLocation = location
                    LOCATION_ACTIVITY_INTERVAL = 2
                    val i = Intent("android.intent.action.MAIN").putExtra("some_msg", "LOCATION_DETECTED")
                    this.sendBroadcast(i)
                }*/

                val intent = Intent()
                intent.action = "UPDATE_PJP_LIST"
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }else{
                Timber.d("LocationFuzedService : dayStart :  "+Pref.DayStartMarked.toString() + " dayEnd : "+Pref.DayEndMarked.toString()+" returning " + AppUtils.getCurrentDateTime())
                return
            }
        }else{
            accuracyStatus = "accurate"
            continueToAccurateFlow(location)
        }

    }

    private fun checkAutoRevisit() {
        if (!Pref.isAddAttendence) {
            Timber.e("====================Attendance is not given (Location Fuzed Service)====================")
            return
        }

        if (lastLat == 0.0 || lastLng == 0.0) {
            Timber.e("====================1st time check auto revisit====================")
            return
        }

        var distance = LocationWizard.getDistance(lastLat, lastLng, Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble())
        distance=0.9
        Timber.e("==checkAutoRevisit==")

        var autoRevDistance : Double = 0.0
        if (AppUtils.isOnline(this)) {
            autoRevDistance = Pref.autoRevisitDistance.toDouble()
        }else{
            autoRevDistance = Pref.OfflineShopAccuracy.toDouble()
        }

        //if (distance * 1000 > Pref.autoRevisitDistance.toDouble()) {
        if (distance * 1000 > autoRevDistance) {
            val allShopList = AppDatabase.getDBInstance()!!.addShopEntryDao().all

            if (allShopList != null && allShopList.size > 0) {
                for (i in 0 until allShopList.size) {
                    val shopLat: Double = allShopList[i].shopLat
                    val shopLong: Double = allShopList[i].shopLong

                    if (shopLat != null && shopLong != null) {
                        val shopLocation = Location("")
                        shopLocation.latitude = shopLat
                        shopLocation.longitude = shopLong

                        //val isShopNearby = FTStorageUtils.checkShopPositionWithinRadious(AppUtils.mLocation, shopLocation, Pref.autoRevisitDistance.toInt())
                        val isShopNearby = FTStorageUtils.checkShopPositionWithinRadious(AppUtils.mLocation, shopLocation, autoRevDistance.toInt())
                        //val isShopNearby = true

                        Timber.e("Dist 1 from shop " + allShopList[i].shopName + " loc to current loc===> " + AppUtils.mLocation?.distanceTo(shopLocation) + " Meter")

                        val distance = LocationWizard.getDistance(shopLat, shopLong, Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble())
                        Timber.e("Dist 2 from shop " + allShopList[i].shopName + " loc to current loc===> $distance KM")

                        if (isShopNearby) {
                            Timber.e("==Nearby shop " + allShopList[i].shopName + "(Location Fuzed Service)==")
                            /*val shopActivity = AppDatabase.getDBInstance()!!.shopActivityDao().durationAvailableForTodayShop(allShopList[i].shop_id,
                                    false, false, AppUtils.getCurrentDateForShopActi())*/
                            val shopActivityList = AppDatabase.getDBInstance()!!.shopActivityDao().getShopForDay(allShopList[i].shop_id, AppUtils.getCurrentDateForShopActi())
                            if (shopActivityList == null || shopActivityList.isEmpty()) {
                                AppUtils.changeLanguage(this,"en")
                                val currentTimeStamp = System.currentTimeMillis()
                                changeLocale()
                                if (prevRevisitTimeStamp != 0L) {
                                    if (shop_id == allShopList[i].shop_id) {
                                        val interval = currentTimeStamp - prevRevisitTimeStamp
                                        val intervalInMins = (interval / 1000) / 60
                                        /*4-05-2022*/
                                        val intervalInSec = (interval / 1000)
                                        //Timber.e("Fuzed Location: start auto revisit interval min =====> $intervalInMins min(s)")
                                        Timber.e("Fuzed Location: start auto revisit interval sec =====> $intervalInSec sec(s) autoRevTimeInSec : ${Pref.autoRevisitTimeInSeconds.toString()}")

                                     //   if (intervalInMins >= Pref.autoRevisitTime.toLong()) {
                                            if (intervalInSec >= Pref.autoRevisitTimeInSeconds.toLong()){
                                            AppUtils.isAutoRevisit = true
                                            Timber.e("Fuzed Location: auto revisit started ${AppUtils.getCurrentDateTime()}")
                                            revisitShop()
                                            prevRevisitTimeStamp = 0L
                                            shop_id = ""
                                        }
                                    } else {
                                        prevRevisitTimeStamp = currentTimeStamp
                                        shop_id = allShopList[i].shop_id
                                    }
                                } else {
                                    prevRevisitTimeStamp = currentTimeStamp
                                     shop_id = allShopList[i].shop_id
                                }
                                break
                            } else
                                Timber.e("==" + allShopList[i].shopName + " is visiting now normally (Loc Fuzed Service)==")
                        }
                    }
                }
            }
        }
    }

    private fun checkAutoRevisitAll() {


        if (!Pref.isAddAttendence) {
            Timber.e("====================Attendance is not given (Location Fuzed Service)====================")
            return
        }
        if (lastLat == 0.0 || lastLng == 0.0) {
            Timber.e("====================1st time check auto revisit====================")
            return
        }

        var distance = LocationWizard.getDistance(lastLat, lastLng, Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble())
        distance=0.9
        Timber.e("==checkAutoRevisit==")

        var autoRevDistance : Double = 0.0
        if (AppUtils.isOnline(this)) {
            autoRevDistance = Pref.autoRevisitDistance.toDouble()
        }else{
            if(Pref.OfflineShopAccuracy.toDouble() > Pref.autoRevisitDistance.toDouble())
                autoRevDistance = Pref.OfflineShopAccuracy.toDouble()
            else
                autoRevDistance = Pref.autoRevisitDistance.toDouble()
        }

        shopCodeListNearby= ArrayList()

        if (distance * 1000 > autoRevDistance) {
            // Rectify inactive shop 26-03-2024 Suman mantis id 27329 begin
            val allShopList = AppDatabase.getDBInstance()!!.addShopEntryDao().getAllActiveShops()
            // Rectify inactive shop 26-03-2024 Suman mantis id 27329 end
            //val allShopList = AppDatabase.getDBInstance()!!.addShopEntryDao().all
            println("tag_auto_rev size : ${allShopList.size}")
            if (allShopList != null && allShopList.size > 0) {
                var nearbyAddCount:Int = 0
                for (i in 0 until allShopList.size) {
                    val shopLat: Double = allShopList[i].shopLat
                    val shopLong: Double = allShopList[i].shopLong
                    if (shopLat != null && shopLong != null) {
                        val shopLocation = Location("")
                        shopLocation.latitude = shopLat
                        shopLocation.longitude = shopLong
                        shop_id = allShopList[i].shop_id
                        val isShopNearby = FTStorageUtils.checkShopPositionWithinRadious(AppUtils.mLocation, shopLocation, autoRevDistance.toInt())

                        if (isShopNearby) {
                            val shopActivityList = AppDatabase.getDBInstance()!!.shopActivityDao().getShopForDay(allShopList[i].shop_id, AppUtils.getCurrentDateForShopActi())
                            if (shopActivityList == null || shopActivityList.isEmpty()) {

                                shopCodeListNearby.add(shop_id)
                                println("autorev ${allShopList[i].shopName}  $isShopNearby  added")

                                nearbyAddCount++
                                if(nearbyAddCount>200)
                                {
                                    //break
                                }

                            } else
                                Timber.e("==" + allShopList[i].shopName + " is visiting now normally (Loc Fuzed Service)==")
                        }
                    }
                }
                println("autorev total nearby size ${shopCodeListNearby.size}")
                revisitShopAll()
            }
        }
    }

    var shopCodeListNearby:ArrayList<String> = ArrayList()

    private fun revisitShopAll() {

        if(shopCodeListNearby.size == 0)
            return

        try {
            shop_id = shopCodeListNearby.get(0)
            if(shopCodeListNearby.size>0)
                shopCodeListNearby.removeAt(0)

            println("autorev postDelayed ${shopCodeListNearby.size}")
        }catch (ex:Exception){
            println("autorev error")
            return
        }

        Timber.e("LocFuzed revisitShopAll started ${AppUtils.getCurrentDateTime()} with shop:  $shop_id")

        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(shop_id.hashCode())

            val shopActivityEntity = AppDatabase.getDBInstance()!!.shopActivityDao().getShopForDay(shop_id, AppUtils.getCurrentDateForShopActi())
            val imageUpDateTime = AppUtils.getCurrentISODateTime()

            val mAddShopDBModelEntity = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(shop_id)

            if (shopActivityEntity.isEmpty() || shopActivityEntity[0].date != AppUtils.getCurrentDateForShopActi()) {
                val mShopActivityEntity = ShopActivityEntity()
                AppUtils.changeLanguage(this,"en")
                mShopActivityEntity.startTimeStamp = System.currentTimeMillis().toString()
                changeLocale()
                mShopActivityEntity.isUploaded = false
                mShopActivityEntity.isVisited = true
                mShopActivityEntity.shop_name = mAddShopDBModelEntity?.shopName
                mShopActivityEntity.duration_spent = "00:00:00"
                mShopActivityEntity.date = AppUtils.getCurrentDateForShopActi()
                mShopActivityEntity.shop_address = mAddShopDBModelEntity?.address
                mShopActivityEntity.shopid = mAddShopDBModelEntity?.shop_id
                mShopActivityEntity.visited_date = imageUpDateTime //AppUtils.getCurrentISODateTime()
                mShopActivityEntity.isDurationCalculated = false
                if (mAddShopDBModelEntity?.totalVisitCount != null && mAddShopDBModelEntity?.totalVisitCount != "") {
                    val visitCount = mAddShopDBModelEntity?.totalVisitCount?.toInt()!! + 1
                    AppDatabase.getDBInstance()!!.addShopEntryDao().updateTotalCount(visitCount.toString(), shop_id)
                    AppDatabase.getDBInstance()!!.addShopEntryDao().updateLastVisitDate(AppUtils.getCurrentDateChanged(), shop_id)
                }

                var distance = 0.0
                var address = ""
                Timber.e("======New Distance (At auto revisit time)=========")

                val shop = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopDetail(shop_id)
                //address = if (!TextUtils.isEmpty(shop.actual_address))
                address = if (!TextUtils.isEmpty(shop.address))
                    shop.address
                    //shop.actual_address
                else
                    LocationWizard.getNewLocationName(this, shop.shopLat.toDouble(), shop.shopLong.toDouble())

                if (Pref.isOnLeave.equals("false", ignoreCase = true)) {
                    Timber.e("=====User is at work (At auto revisit time)=======")

                    val locationList = AppDatabase.getDBInstance()!!.userLocationDataDao().getLocationUpdateForADay(AppUtils.getCurrentDateForShopActi())

                    val userlocation = UserLocationDataEntity()
                    userlocation.latitude = shop.shopLat.toString()
                    userlocation.longitude = shop.shopLong.toString()

                    var loc_distance = 0.0

                    if (locationList != null && locationList.isNotEmpty()) {
                        loc_distance = LocationWizard.getDistance(locationList[locationList.size - 1].latitude.toDouble(), locationList[locationList.size - 1].longitude.toDouble(),
                            userlocation.latitude.toDouble(), userlocation.longitude.toDouble())
                    }
                    val finalDistance = (Pref.tempDistance.toDouble() + loc_distance).toString()

                    Timber.e("===Distance (At auto shop revisit time)===")
                    Timber.e("Temp Distance====> " + Pref.tempDistance)
                    Timber.e("Normal Distance====> $loc_distance")
                    Timber.e("Total Distance====> $finalDistance")
                    Timber.e("===========================================")

                    userlocation.distance = finalDistance
                    userlocation.locationName = LocationWizard.getNewLocationName(this, userlocation.latitude.toDouble(), userlocation.longitude.toDouble())
                    userlocation.timestamp = LocationWizard.getTimeStamp()
                    userlocation.time = LocationWizard.getFormattedTime24Hours(true)
                    userlocation.meridiem = LocationWizard.getMeridiem()
                    userlocation.hour = LocationWizard.getHour()
                    userlocation.minutes = LocationWizard.getMinute()
                    userlocation.isUploaded = false
                    userlocation.shops = AppDatabase.getDBInstance()!!.shopActivityDao().getTotalShopVisitedForADay(AppUtils.getCurrentDateForShopActi()).size.toString()
                    userlocation.updateDate = AppUtils.getCurrentDateForShopActi()
                    userlocation.updateDateTime = AppUtils.getCurrentDateTime()
                    userlocation.network_status = if (AppUtils.isOnline(this)) "Online" else "Offline"
                    userlocation.battery_percentage = AppUtils.getBatteryPercentage(this).toString()

                    //harcoded location isUploaded true begin
                    userlocation.isUploaded = true
                    //harcoded location isUploaded true end

                    AppDatabase.getDBInstance()!!.userLocationDataDao().insertAll(userlocation)

                    Timber.e("=====Shop auto revisit data added=======")

                    Pref.totalS2SDistance = (Pref.totalS2SDistance.toDouble() + userlocation.distance.toDouble()).toString()

                    distance = Pref.totalS2SDistance.toDouble()
                    Pref.totalS2SDistance = "0.0"
                    Pref.tempDistance = "0.0"
                } else {
                    Timber.e("=====User is on leave (At auto revisit time)=======")
                    distance = 0.0
                }

                Timber.e("shop to shop distance (At auto revisit time)=====> $distance")

                mShopActivityEntity.distance_travelled = distance.toString()
                mShopActivityEntity.in_time = AppUtils.getCurrentTimeWithMeredian()
                mShopActivityEntity.in_loc = address

                Pref.isShopVisited=true

                var shopAll = AppDatabase.getDBInstance()!!.shopActivityDao().getShopActivityAll()
                mShopActivityEntity.shop_revisit_uniqKey = Pref.user_id + System.currentTimeMillis().toString()

                AppDatabase.getDBInstance()!!.shopActivityDao().insertAll(mShopActivityEntity)

                /*Terminate All other Shop Visit*/
                val shopList = AppDatabase.getDBInstance()!!.shopActivityDao().getTotalShopVisitedForADay(AppUtils.getCurrentDateForShopActi())
                for (i in 0 until shopList.size) {
                    if (shopList[i].shopid != mShopActivityEntity.shopid && !shopList[i].isDurationCalculated) {
                        AppUtils.changeLanguage(this,"en")
                        val endTimeStamp = System.currentTimeMillis().toString()
                        changeLocale()
                        var duration = AppUtils.getTimeFromTimeSpan(shopList[i].startTimeStamp, endTimeStamp)
                        val totalMinute = AppUtils.getMinuteFromTimeStamp(shopList[i].startTimeStamp, endTimeStamp)

                        Timber.d("revisitShop LocFuzedS=> startT: ${shopList[i].startTimeStamp} endTime: $endTimeStamp   duration: $duration totalMinute:$totalMinute")
                        if(duration.contains("-")){
                            duration="00:00:00"
                        }

                        //If duration is greater than 20 hour then stop incrementing
                        /*if (totalMinute.toInt() > 20 * 60) {
                            AppDatabase.getDBInstance()!!.shopActivityDao().updateDurationAvailable(true, shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi())
                            return
                        }*/
                        AppDatabase.getDBInstance()!!.shopActivityDao().updateEndTimeOfShop(endTimeStamp, shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi())
                        AppDatabase.getDBInstance()!!.shopActivityDao().updateTotalMinuteForDayOfShop(shopList[i].shopid!!, totalMinute, AppUtils.getCurrentDateForShopActi())
                        AppDatabase.getDBInstance()!!.shopActivityDao().updateTimeDurationForDayOfShop(shopList[i].shopid!!, duration, AppUtils.getCurrentDateForShopActi())
                        AppDatabase.getDBInstance()!!.shopActivityDao().updateDurationAvailable(true, shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi())
                        AppDatabase.getDBInstance()!!.shopActivityDao().updateOutTime(AppUtils.getCurrentTimeWithMeredian(), shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi(), shopList[i].startTimeStamp)
                        AppDatabase.getDBInstance()!!.shopActivityDao().updateOutLocation(LocationWizard.getNewLocationName(this, Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble()), shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi(), shopList[i].startTimeStamp)

                        val netStatus = if (AppUtils.isOnline(this))
                            "Online"
                        else
                            "Offline"

                        val netType = if (AppUtils.getNetworkType(this).equals("wifi", ignoreCase = true))
                            AppUtils.getNetworkType(this)
                        else
                            "Mobile ${AppUtils.mobNetType(this)}"

                        AppDatabase.getDBInstance()!!.shopActivityDao().updateDeviceStatusReason(AppUtils.getDeviceName(), AppUtils.getAndroidVersion(),
                            AppUtils.getBatteryPercentage(this).toString(), netStatus, netType.toString(), shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi())
                    }
                }
            }

            AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdList(shop_id)!![0].visited = true

            val performance = AppDatabase.getDBInstance()!!.performanceDao().getTodaysData(AppUtils.getCurrentDateForShopActi())
            if (performance != null) {
                val list = AppDatabase.getDBInstance()!!.shopActivityDao().getDurationCalculatedVisitedShopForADay(AppUtils.getCurrentDateForShopActi(), true)
                AppDatabase.getDBInstance()!!.performanceDao().updateTotalShopVisited(list.size.toString(), AppUtils.getCurrentDateForShopActi())
                var totalTimeSpentForADay = 0
                for (i in list.indices) {
                    totalTimeSpentForADay += list[i].totalMinute.toInt()
                }
                AppDatabase.getDBInstance()!!.performanceDao().updateTotalDuration(totalTimeSpentForADay.toString(), AppUtils.getCurrentDateForShopActi())
            } else {
                val list = AppDatabase.getDBInstance()!!.shopActivityDao().getDurationCalculatedVisitedShopForADay(AppUtils.getCurrentDateForShopActi(), true)
                val performanceEntity = PerformanceEntity()
                performanceEntity.date = AppUtils.getCurrentDateForShopActi()
                performanceEntity.total_shop_visited = list.size.toString()
                var totalTimeSpentForADay = 0
                for (i in list.indices) {
                    totalTimeSpentForADay += list[i].totalMinute.toInt()
                }
                performanceEntity.total_duration_spent = totalTimeSpentForADay.toString()
                AppDatabase.getDBInstance()!!.performanceDao().insert(performanceEntity)
            }

            AppUtils.isAutoRevisit = false
            Timber.e("Fuzed Location: auto revisit endes ${AppUtils.getCurrentDateTime()}")
            val intent = Intent()
            intent.action = "AUTO_REVISIT_BROADCAST"
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

            Handler().postDelayed(Runnable {
                revisitShopAll()
            }, 50)


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @SuppressLint("SuspiciousIndentation")
    private fun revisitShop() {

            try {

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(shop_id.hashCode())

                val shopActivityEntity = AppDatabase.getDBInstance()!!.shopActivityDao().getShopForDay(shop_id, AppUtils.getCurrentDateForShopActi())
                val imageUpDateTime = AppUtils.getCurrentISODateTime()

                val mAddShopDBModelEntity = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(shop_id)

                if (shopActivityEntity.isEmpty() || shopActivityEntity[0].date != AppUtils.getCurrentDateForShopActi()) {
                    val mShopActivityEntity = ShopActivityEntity()
                    AppUtils.changeLanguage(this,"en")
                    mShopActivityEntity.startTimeStamp = System.currentTimeMillis().toString()
                    changeLocale()
                    mShopActivityEntity.isUploaded = false
                    mShopActivityEntity.isVisited = true
                    mShopActivityEntity.shop_name = mAddShopDBModelEntity?.shopName
                    mShopActivityEntity.duration_spent = "00:00:00"
                    mShopActivityEntity.date = AppUtils.getCurrentDateForShopActi()
                    mShopActivityEntity.shop_address = mAddShopDBModelEntity?.address
                    mShopActivityEntity.shopid = mAddShopDBModelEntity?.shop_id
                    mShopActivityEntity.visited_date = imageUpDateTime //AppUtils.getCurrentISODateTime()
                    mShopActivityEntity.isDurationCalculated = false
                    if (mAddShopDBModelEntity?.totalVisitCount != null && mAddShopDBModelEntity?.totalVisitCount != "") {
                        val visitCount = mAddShopDBModelEntity?.totalVisitCount?.toInt()!! + 1
                        AppDatabase.getDBInstance()!!.addShopEntryDao().updateTotalCount(visitCount.toString(), shop_id)
                        AppDatabase.getDBInstance()!!.addShopEntryDao().updateLastVisitDate(AppUtils.getCurrentDateChanged(), shop_id)
                    }

                    var distance = 0.0
                    var address = ""
                    Timber.e("======New Distance (At auto revisit time)=========")

                    val shop = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopDetail(shop_id)
                    address = if (!TextUtils.isEmpty(shop.actual_address))
                        shop.actual_address
                    else
                        LocationWizard.getNewLocationName(this, shop.shopLat.toDouble(), shop.shopLong.toDouble())

                    if (Pref.isOnLeave.equals("false", ignoreCase = true)) {
                        Timber.e("=====User is at work (At auto revisit time)=======")

                        val locationList = AppDatabase.getDBInstance()!!.userLocationDataDao().getLocationUpdateForADay(AppUtils.getCurrentDateForShopActi())

                        val userlocation = UserLocationDataEntity()
                        userlocation.latitude = shop.shopLat.toString()
                        userlocation.longitude = shop.shopLong.toString()

                        var loc_distance = 0.0

                        if (locationList != null && locationList.isNotEmpty()) {
                            loc_distance = LocationWizard.getDistance(locationList[locationList.size - 1].latitude.toDouble(), locationList[locationList.size - 1].longitude.toDouble(),
                                userlocation.latitude.toDouble(), userlocation.longitude.toDouble())
                        }
                        val finalDistance = (Pref.tempDistance.toDouble() + loc_distance).toString()

                        Timber.e("===Distance (At auto shop revisit time)===")
                        Timber.e("Temp Distance====> " + Pref.tempDistance)
                        Timber.e("Normal Distance====> $loc_distance")
                        Timber.e("Total Distance====> $finalDistance")
                        Timber.e("===========================================")

                        userlocation.distance = finalDistance
                        userlocation.locationName = LocationWizard.getNewLocationName(this, userlocation.latitude.toDouble(), userlocation.longitude.toDouble())
                        userlocation.timestamp = LocationWizard.getTimeStamp()
                        userlocation.time = LocationWizard.getFormattedTime24Hours(true)
                        userlocation.meridiem = LocationWizard.getMeridiem()
                        userlocation.hour = LocationWizard.getHour()
                        userlocation.minutes = LocationWizard.getMinute()
                        userlocation.isUploaded = false
                        userlocation.shops = AppDatabase.getDBInstance()!!.shopActivityDao().getTotalShopVisitedForADay(AppUtils.getCurrentDateForShopActi()).size.toString()
                        userlocation.updateDate = AppUtils.getCurrentDateForShopActi()
                        userlocation.updateDateTime = AppUtils.getCurrentDateTime()
                        userlocation.network_status = if (AppUtils.isOnline(this)) "Online" else "Offline"
                        userlocation.battery_percentage = AppUtils.getBatteryPercentage(this).toString()

                        //harcoded location isUploaded true begin
                        userlocation.isUploaded = true
                        //harcoded location isUploaded true end

                        AppDatabase.getDBInstance()!!.userLocationDataDao().insertAll(userlocation)

                        Timber.e("=====Shop auto revisit data added=======")

                        Pref.totalS2SDistance = (Pref.totalS2SDistance.toDouble() + userlocation.distance.toDouble()).toString()

                        distance = Pref.totalS2SDistance.toDouble()
                        Pref.totalS2SDistance = "0.0"
                        Pref.tempDistance = "0.0"
                    } else {
                        Timber.e("=====User is on leave (At auto revisit time)=======")
                        distance = 0.0
                    }

                    Timber.e("shop to shop distance (At auto revisit time)=====> $distance")

                    mShopActivityEntity.distance_travelled = distance.toString()
                    mShopActivityEntity.in_time = AppUtils.getCurrentTimeWithMeredian()
                    mShopActivityEntity.in_loc = address

                    Pref.isShopVisited=true

                    var shopAll = AppDatabase.getDBInstance()!!.shopActivityDao().getShopActivityAll()
                    mShopActivityEntity.shop_revisit_uniqKey = Pref.user_id + System.currentTimeMillis().toString()

                    AppDatabase.getDBInstance()!!.shopActivityDao().insertAll(mShopActivityEntity)

                    /*Terminate All other Shop Visit*/
                    val shopList = AppDatabase.getDBInstance()!!.shopActivityDao().getTotalShopVisitedForADay(AppUtils.getCurrentDateForShopActi())
                    for (i in 0 until shopList.size) {
                        if (shopList[i].shopid != mShopActivityEntity.shopid && !shopList[i].isDurationCalculated) {
                            AppUtils.changeLanguage(this,"en")
                            val endTimeStamp = System.currentTimeMillis().toString()
                            changeLocale()
                            var duration = AppUtils.getTimeFromTimeSpan(shopList[i].startTimeStamp, endTimeStamp)
                            val totalMinute = AppUtils.getMinuteFromTimeStamp(shopList[i].startTimeStamp, endTimeStamp)

                            Timber.d("revisitShop LocFuzedS=> startT: ${shopList[i].startTimeStamp} endTime: $endTimeStamp   duration: $duration totalMinute:$totalMinute")
                            if(duration.contains("-")){
                                duration="00:00:00"
                            }

                            //If duration is greater than 20 hour then stop incrementing
                            /*if (totalMinute.toInt() > 20 * 60) {
                                AppDatabase.getDBInstance()!!.shopActivityDao().updateDurationAvailable(true, shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi())
                                return
                            }*/
                            AppDatabase.getDBInstance()!!.shopActivityDao().updateEndTimeOfShop(endTimeStamp, shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi())
                            AppDatabase.getDBInstance()!!.shopActivityDao().updateTotalMinuteForDayOfShop(shopList[i].shopid!!, totalMinute, AppUtils.getCurrentDateForShopActi())
                            AppDatabase.getDBInstance()!!.shopActivityDao().updateTimeDurationForDayOfShop(shopList[i].shopid!!, duration, AppUtils.getCurrentDateForShopActi())
                            AppDatabase.getDBInstance()!!.shopActivityDao().updateDurationAvailable(true, shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi())
                            AppDatabase.getDBInstance()!!.shopActivityDao().updateOutTime(AppUtils.getCurrentTimeWithMeredian(), shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi(), shopList[i].startTimeStamp)
                            AppDatabase.getDBInstance()!!.shopActivityDao().updateOutLocation(LocationWizard.getNewLocationName(this, Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble()), shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi(), shopList[i].startTimeStamp)

                            val netStatus = if (AppUtils.isOnline(this))
                                "Online"
                            else
                                "Offline"

                            val netType = if (AppUtils.getNetworkType(this).equals("wifi", ignoreCase = true))
                                AppUtils.getNetworkType(this)
                            else
                                "Mobile ${AppUtils.mobNetType(this)}"

                            AppDatabase.getDBInstance()!!.shopActivityDao().updateDeviceStatusReason(AppUtils.getDeviceName(), AppUtils.getAndroidVersion(),
                                AppUtils.getBatteryPercentage(this).toString(), netStatus, netType.toString(), shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi())
                        }
                    }
                }

                AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdList(shop_id)!![0].visited = true

                val performance = AppDatabase.getDBInstance()!!.performanceDao().getTodaysData(AppUtils.getCurrentDateForShopActi())
                if (performance != null) {
                    val list = AppDatabase.getDBInstance()!!.shopActivityDao().getDurationCalculatedVisitedShopForADay(AppUtils.getCurrentDateForShopActi(), true)
                    AppDatabase.getDBInstance()!!.performanceDao().updateTotalShopVisited(list.size.toString(), AppUtils.getCurrentDateForShopActi())
                    var totalTimeSpentForADay = 0
                    for (i in list.indices) {
                        totalTimeSpentForADay += list[i].totalMinute.toInt()
                    }
                    AppDatabase.getDBInstance()!!.performanceDao().updateTotalDuration(totalTimeSpentForADay.toString(), AppUtils.getCurrentDateForShopActi())
                } else {
                    val list = AppDatabase.getDBInstance()!!.shopActivityDao().getDurationCalculatedVisitedShopForADay(AppUtils.getCurrentDateForShopActi(), true)
                    val performanceEntity = PerformanceEntity()
                    performanceEntity.date = AppUtils.getCurrentDateForShopActi()
                    performanceEntity.total_shop_visited = list.size.toString()
                    var totalTimeSpentForADay = 0
                    for (i in list.indices) {
                        totalTimeSpentForADay += list[i].totalMinute.toInt()
                    }
                    performanceEntity.total_duration_spent = totalTimeSpentForADay.toString()
                    AppDatabase.getDBInstance()!!.performanceDao().insert(performanceEntity)
                }

                AppUtils.isAutoRevisit = false
                Timber.e("Fuzed Location: auto revisit endes ${AppUtils.getCurrentDateTime()}")
                val intent = Intent()
                intent.action = "AUTO_REVISIT_BROADCAST"
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun saveBatteryNetData() {

        if (!shouldBatNetSaveDuration()) {
            Timber.e("===============Should not save Battery Internet status data(Location Fuzed Service)==============")
            return
        }

        Timber.d("battery status==========> " + AppUtils.getBatteryStatus(this@LocationFuzedService))
        Timber.d("battery percentage==========> " + AppUtils.getBatteryPercentage(this@LocationFuzedService))
        Timber.d("network type==========> " + AppUtils.getNetworkType(this@LocationFuzedService))
        Timber.d("mobile network type==========> " + AppUtils.mobNetType(this@LocationFuzedService))
        Timber.d("device model==========> " + AppUtils.getDeviceName())
        Timber.d("android version==========> " + Build.VERSION.SDK_INT)


        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        val totalSt = StatFs(Environment.getExternalStorageDirectory().path)
        val bytesAvailable: Long
        bytesAvailable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            stat.blockSizeLong * stat.availableBlocksLong
        } else {
            stat.blockSize.toLong() * stat.availableBlocks.toLong()
        }
        val bytesTotal: Long
        bytesTotal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            totalSt.blockCountLong * totalSt.blockSizeLong
        } else {
            totalSt.blockCountLong.toLong() * totalSt.blockSizeLong.toLong()
        }
        val megAvailable = bytesAvailable / (1024 * 1024)
        val megTotal = bytesTotal / (1024 * 1024)
        println("phone_storage : FREE SPACE : " + megAvailable.toString() + " TOTAL SPACE : " + megTotal.toString() + " Time :" + AppUtils.getCurrentDateTime());


        val batNetEntity = BatteryNetStatusEntity()
        AppDatabase.getDBInstance()?.batteryNetDao()?.insert(batNetEntity.apply {
            AppUtils.changeLanguage(this@LocationFuzedService,"en")
            bat_net_id = Pref.user_id + "_batNet_" + System.currentTimeMillis()
            changeLocale()
            date_time = AppUtils.getCurrentISODateTime()
            date = AppUtils.getCurrentDateForShopActi()
            bat_status = AppUtils.getBatteryStatus(this@LocationFuzedService)
            bat_level = AppUtils.getBatteryPercentage(this@LocationFuzedService).toString()
            net_type = AppUtils.getNetworkType(this@LocationFuzedService)
            mob_net_type = AppUtils.mobNetType(this@LocationFuzedService)
            device_model = AppUtils.getDeviceName()
            android_version = Build.VERSION.SDK_INT.toString()
            Available_Storage= megAvailable.toString()+"mb"
            Total_Storage=megTotal.toString()+"mb"
            isUploaded = false
            Power_Saver_Status = Pref.PowerSaverStatus
        })
    }


    private fun syncBatteryNetData() {
       if (!shouldBatNetSyncDuration()) {
            Timber.e("===============Should not sync Battery Internet status data(Location Fuzed Service)==============")
            return
        }

        if (!AppUtils.isOnline(this)) {
            Timber.d("App Info Input(Location Fuzed Service)======> No internet connected")
            return
        }

        val unSyncData = AppDatabase.getDBInstance()?.batteryNetDao()?.getDataSyncStateWise(false)


        if (unSyncData == null || unSyncData.isEmpty())
            return

        if (AppUtils.isAppInfoUpdating)
            return

        AppUtils.isAppInfoUpdating = true

        val appInfoList = ArrayList<AppInfoDataModel>()

        unSyncData.forEach {
            appInfoList.add(AppInfoDataModel(it.bat_net_id!!, it.date_time!!, it.bat_status!!, it.bat_level!!, it.net_type!!, it.mob_net_type!!,
                    it.device_model!!, it.android_version!!,it.Available_Storage!!,it.Total_Storage!!,it.Power_Saver_Status))
        }


        var totalVisitRevisitCount = AppDatabase.getDBInstance()!!.shopActivityDao().getVisitRevisitCountByDate(AppUtils.getCurrentDateForShopActi())
        var totalVisitRevisitCountSynced = AppDatabase.getDBInstance()!!.shopActivityDao().getVisitRevisitCountByDateSyncedUnSynced(AppUtils.getCurrentDateForShopActi(),true)
        var totalVisitRevisitCountUnSynced = AppDatabase.getDBInstance()!!.shopActivityDao().getVisitRevisitCountByDateSyncedUnSynced(AppUtils.getCurrentDateForShopActi(),false)

        val appInfoInput = AppInfoInputModel(Pref.session_token!!, Pref.user_id!!, appInfoList,totalVisitRevisitCount.toString(),totalVisitRevisitCountSynced.toString(),totalVisitRevisitCountUnSynced.toString())

        Timber.d("============App Info Input(Location Fuzed Service)===========")
        Timber.d("session_token==========> " + appInfoInput.session_token)
        Timber.d("user_id==========> " + appInfoInput.user_id)
        Timber.d("app_info_list.size==========> " + appInfoInput.app_info_list?.size)
        Timber.d("powerSaverStatus==========> " + Pref.PowerSaverStatus)
        Timber.d("==============================================================")

        val repository = LocationRepoProvider.provideLocationRepository()
        compositeDisposable.add(
                repository.appInfo(appInfoInput)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
//                        .timeout(60 * 1, TimeUnit.SECONDS)
                        .subscribe({ result ->
                            val response = result as BaseResponse
                            Timber.d("App Info : RESPONSE : " + response.status + ":" + response.message)
                            AppUtils.isAppInfoUpdating = false

                            if (response.status == NetworkConstant.SUCCESS) {
                                unSyncData.forEach {
                                    AppDatabase.getDBInstance()?.batteryNetDao()?.updateIsUploadedAccordingToId(true, it.id)
                                }
                            }

                        }, { error ->
                            AppUtils.isAppInfoUpdating = false
                            if (error == null) {
                                Timber.d("App Info : ERROR : " + "UNEXPECTED ERROR IN LOCATION ACTIVITY API")
                            } else {
                                Timber.d("App Info : ERROR : " + error.localizedMessage)
                                error.printStackTrace()
                            }
                        })
        )
    }

    private fun syncGpsNetData() {
        if (!shouldGpsNetSyncDuration()) {
            Timber.e("===============Should not sync syncGpsNetData(Location Fuzed Service)==============")
            return
        }

        if (!AppUtils.isOnline(this)) {
            Timber.d("App Info Input(Location Fuzed Service)======> No internet connected")
            return
        }

        val unSyncData = AppDatabase.getDBInstance()?.newGpsStatusDao()?.getNotUploaded(false)


        if (unSyncData == null || unSyncData.isEmpty())
            return

        val gps_net_status_list = ArrayList<NewGpsStatusEntity>()

        unSyncData.forEach {
            var obj :NewGpsStatusEntity = NewGpsStatusEntity()
            obj.apply {
                id=it.id
                date_time = it.date_time
                gps_service_status = it.gps_service_status
                network_status = it.network_status
            }
            gps_net_status_list.add(obj)
        }

        var sendObj : GpsNetInputModel = GpsNetInputModel()
        sendObj.user_id = Pref.user_id!!
        sendObj.session_token = Pref.session_token!!
        sendObj.gps_net_status_list = gps_net_status_list


        val repository = LocationRepoProvider.provideLocationRepository()
        compositeDisposable.add(
            repository.gpsNetInfo(sendObj)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
//                        .timeout(60 * 1, TimeUnit.SECONDS)
                .subscribe({ result ->
                    val response = result as BaseResponse
                    if (response.status == NetworkConstant.SUCCESS) {
                        unSyncData.forEach {
                            AppDatabase.getDBInstance()?.newGpsStatusDao()?.updateIsUploadedAccordingToId(true, it.id)
                        }
                    }
                }, { error ->
                    if (error == null) {
                        Timber.d("App Info : ERROR : " + "UNEXPECTED ERROR IN LOCATION ACTIVITY API")
                    } else {
                        Timber.d("App Info : ERROR : " + error.localizedMessage)
                        error.printStackTrace()
                    }
                })
        )
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotification() {

        if (!Pref.isShowCurrentLocNotifiaction)
            return

        if (!shouldUpdateLocationNotificationDuration()) {
            Timber.e("===============Should not show notification data(Location Fuzed Service)==============")
            return
        }

        val notification = NotificationUtils(getString(R.string.app_name), "", "", "")
        val body = "Thanks for being active. Your current location detected as: " + LocationWizard.getLocationName(this, Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble())
        notification.sendLocNotification(this, body)
    }

    private var attendanceAlertDialog: CommonDialogSingleBtn? = null
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAttendNotification() {


        var shopListRoom = AppDatabase.getDBInstance()!!.shopActivityDao().getAllShopActivityByDate(AppUtils.getCurrentDateForShopActi()) as ArrayList<String>
        var timeGapMin = 30
        if(!Pref.isAddAttendence)
            timeGapMin = 5
        else if(Pref.IsShowDayStart && !Pref.DayStartMarked)
            timeGapMin = 5
        else if(shopListRoom.size==0){
            timeGapMin = 30
        }

        if ( !shouldUpdateAttendanceNotificationDuration(timeGapMin)) {
            Timber.e("===============Should not show notification data(Location Fuzed Service)==============")
            return
        }


        val intent = Intent()
        intent.action = "IDEAL_ATTEND_BROADCAST"
        if(!Pref.isAddAttendence)
            intent.putExtra("data_msg","Please mark your attendance for today. Thanks.")
        else if(Pref.IsShowDayStart && !Pref.DayStartMarked)
            intent.putExtra("data_msg","Please start your day for today. Thanks.")
        else if(shopListRoom.size==0){
            intent.putExtra("data_msg","Please visit/revisit shops. Thanks.")
        }else
            intent.putExtra("data_msg","")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

    }

    private fun showDayEndNotification(){
        val currentTimeInLong = AppUtils.convertTimeWithMeredianToLong(AppUtils.getCurrentTimeWithMeredian())
        val approvedOutTimeInLong1 = AppUtils.convertTimeWithMeredianToLong("08:15 PM")
        val approvedOutTimeInLong2 = AppUtils.convertTimeWithMeredianToLong("08:30 PM")
        val approvedOutTimeInLong3 = AppUtils.convertTimeWithMeredianToLong("08:45 PM")

        if(currentTimeInLong > approvedOutTimeInLong1 || currentTimeInLong > approvedOutTimeInLong2 || currentTimeInLong > approvedOutTimeInLong3){
            val intent = Intent()
            intent.action = "IDEAL_ATTEND_BROADCAST"
            intent.putExtra("data_msg","You Dayend time set at 9 PM. Please mark your Dayend before 9 PM.")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateInaccurateDistance(location: Location) {
        Timber.e("==========User is not at home location (Location Fuzed Service)==========")
        //val distance = LocationWizard.getDistance(acuurateLat, acuurateLong, location.latitude, location.longitude)
        val distance = LocationWizard.getDistance(lastLat, lastLng, location.latitude, location.longitude)

        /*if (location.accuracy <= AppUtils.maxAccuracy.toFloat()) {
        Timber.e("=============Accuracy not greater than maxAccuracy (Location Fuzed Service)============")

        if (distance * 1000 > 500)
            updateInaccurateLocation(location)
        else {
            Timber.e("=============Distance less than 500 meter for inaccurate & accurate location (Location Fuzed Service)============")
            continueToAccurateFlow(location)
        }
       } else {
        Timber.e("=============Accuracy greater than maxAccuracy (Location Fuzed Service)============")
        updateInaccurateLocation(location)
       }*/

        Timber.e("DISTANCE=====> $distance")

        if (distance * 1000 <= AppUtils.maxDistance.toDouble() && distance * 1000 >= AppUtils.minDistance.toDouble()) {
            tempDistance = (tempDistance.toDouble() + distance).toString()
            resetData()
            Timber.e("=======Temp Distance is less than maximum distance====")
        } else if (distance * 1000 > AppUtils.maxDistance.toDouble()) {
            tempDistance = (tempDistance.toDouble() + (AppUtils.maxDistance.toDouble() / 1000)).toString()
            resetData()
            Timber.e("=======Temp Distance is greater than maximum distance====")
        } else if (distance * 1000 < AppUtils.minDistance.toDouble()) {

            if (Pref.isAddAttendence)
                calculateIdleTime(location, "inaccurate")
            else
                Timber.e("=====Attendance is not added for today (Inaccurate idle time)======")
        }
    }


    private fun checkMeetingDistance() {
        val list = AppDatabase.getDBInstance()!!.addMeetingDao().durationAvailableSyncWise(false, false)

        if (list != null && list.isNotEmpty()) {
            for (i in list.indices) {
                val distance = LocationWizard.getDistance(list[i].lattitude?.toDouble()!!, list[i].longitude?.toDouble()!!, Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble())

                Timber.e("MEETING DISTANCE==========> $distance KM")
                Timber.e("MEETING DISTANCE LIMIT==========> ${Pref.meetingDistance} Meter")

                if (distance * 1000 > Pref.meetingDistance.toDouble()) {
                    AppUtils.changeLanguage(this,"en")
                    val endTimeStamp = System.currentTimeMillis().toString()
                    changeLocale()
                    val duration = AppUtils.getTimeFromTimeSpan(list[i].startTimeStamp!!, endTimeStamp)
                    AppDatabase.getDBInstance()!!.addMeetingDao().updateEndTimeOfMeeting(endTimeStamp, list[i].id, AppUtils.getCurrentDateForShopActi())
                    AppDatabase.getDBInstance()!!.addMeetingDao().updateTimeDurationForDayOfMeeting(list[i].id, duration, AppUtils.getCurrentDateForShopActi())
                    AppDatabase.getDBInstance()!!.addMeetingDao().updateDurationAvailable(true, list[i].id, AppUtils.getCurrentDateForShopActi())
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkForceLogoutNotification() {

        if (!TextUtils.isEmpty(Pref.approvedOutTime) && !Pref.isAutoLogout) {

            val currentTimeInLong = AppUtils.convertTimeWithMeredianToLong(AppUtils.getCurrentTimeWithMeredian())
            val approvedOutTimeInLong = AppUtils.convertTimeWithMeredianToLong(Pref.approvedOutTime)

            if (currentTimeInLong >= approvedOutTimeInLong) {
                val notification = NotificationUtils(getString(R.string.app_name), "", "", "")
                notification.sendForceLogoutNotification(this, "Hi, final logout time of the day is " + Pref.approvedOutTime +
                ", please logout now. Thanks.")

                val intent = Intent()
                intent.action = "FORCE_LOGOUT_BROADCAST"
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
        }
    }

    private fun calculateOrderCollectionAlertTime() {

        if (!shouldCallOrderCollectionAlertCheck()) {
            Timber.e("============Order Collection Alert Check before desired time(Location Fuzed Service)===========")
            return
        }

        if (!Pref.isAutoLogout) {

            try {
                AppUtils.changeLanguage(this,"en")
                val currentTime = System.currentTimeMillis()
                changeLocale()

                var isOrderAdded = false
                var isCollectionAdded = false

                val todaysOrderList = AppDatabase.getDBInstance()!!.orderDetailsListDao().getListAccordingDate(AppUtils.getCurrentDate())

                if (todaysOrderList != null && todaysOrderList.isNotEmpty()) {
                    val lastOrderTime = AppUtils.getTimeStampFromValidDetTime(todaysOrderList[0].date!!)

                    val diffInSec = (currentTime - lastOrderTime) / 1000

                    if (diffInSec <= 3600) {
                        isOrderAdded = true
                    }
                }

                val todaysCollectionList = AppDatabase.getDBInstance()!!.collectionDetailsDao().getDateWiseCollection(AppUtils.getCurrentDate())

                if (todaysCollectionList != null && todaysCollectionList.isNotEmpty()) {
                    if (!TextUtils.isEmpty(todaysCollectionList[0].only_time)) {
                        val lastCollectionTime = AppUtils.getTimeStampFromValidDetTime(AppUtils.getCurrentDateFormatInTa(todaysCollectionList[0].date!!) +
                                "T" + todaysCollectionList[0].only_time)

                        val diffInSec = (currentTime - lastCollectionTime) / 1000

                        if (diffInSec <= 3600) {
                            isCollectionAdded = true
                        }
                    }
                }

                if (!TextUtils.isEmpty(Pref.user_id) && Pref.isAddAttendence && (!isOrderAdded || !isCollectionAdded)) {
                    //showOrderCollectionAlert(isOrderAdded, isCollectionAdded)

                    val inten = Intent()
                    inten.action = "ALERT_RECIEVER_BROADCAST"
                    inten.putExtra("isOrderAdded", isOrderAdded)
                    inten.putExtra("isCollectionAdded", isCollectionAdded)

                    LocalBroadcastManager.getInstance(this@LocationFuzedService).sendBroadcast(inten)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun endShopDuration(shopId: String) {
        val shopActiList = AppDatabase.getDBInstance()!!.shopActivityDao().getShopForDay(shopId, AppUtils.getCurrentDateForShopActi())
        if (shopActiList.isEmpty())
            return
        Timber.e("Location Fuzed Service: FarFromShop : " + "ShopName : " + shopActiList[0].shop_name!!)

        if (!Pref.isMultipleVisitEnable) {
            if (!shopActiList[0].isDurationCalculated && !shopActiList[0].isUploaded) {
                AppUtils.changeLanguage(this, "en")
                val endTimeStamp = System.currentTimeMillis().toString()
                changeLocale()
                val startTimestamp = shopActiList[0].startTimeStamp

                val duration = AppUtils.getTimeFromTimeSpan(startTimestamp, endTimeStamp)
                val totalMinute = AppUtils.getMinuteFromTimeStamp(startTimestamp, endTimeStamp)

                AppDatabase.getDBInstance()!!.shopActivityDao().updateEndTimeOfShop(endTimeStamp, shopActiList[0].shopid!!, AppUtils.getCurrentDateForShopActi())
                AppDatabase.getDBInstance()!!.shopActivityDao().updateIsUploaded(false, shopActiList[0].shopid!!, AppUtils.getCurrentDateForShopActi())
                AppDatabase.getDBInstance()!!.shopActivityDao().updateTotalMinuteForDayOfShop(shopActiList[0].shopid!!, totalMinute, AppUtils.getCurrentDateForShopActi())
                AppDatabase.getDBInstance()!!.shopActivityDao().updateTimeDurationForDayOfShop(shopActiList[0].shopid!!, duration, AppUtils.getCurrentDateForShopActi())
                AppDatabase.getDBInstance()!!.shopActivityDao().updateDurationAvailable(true, shopActiList[0].shopid!!, AppUtils.getCurrentDateForShopActi())
                AppDatabase.getDBInstance()!!.shopActivityDao().updateOutTime(AppUtils.getCurrentTimeWithMeredian(), shopActiList[0].shopid!!, AppUtils.getCurrentDateForShopActi(), shopActiList[0].startTimeStamp)
                AppDatabase.getDBInstance()!!.shopActivityDao().updateOutLocation(LocationWizard.getNewLocationName(this, Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble()), shopActiList[0].shopid!!, AppUtils.getCurrentDateForShopActi(), shopActiList[0].startTimeStamp)

                val netStatus = if (AppUtils.isOnline(this))
                    "Online"
                else
                    "Offline"

                val netType = if (AppUtils.getNetworkType(this).equals("wifi", ignoreCase = true))
                    AppUtils.getNetworkType(this)
                else
                    "Mobile ${AppUtils.mobNetType(this)}"

                AppDatabase.getDBInstance()!!.shopActivityDao().updateDeviceStatusReason(AppUtils.getDeviceName(), AppUtils.getAndroidVersion(),
                        AppUtils.getBatteryPercentage(this).toString(), netStatus, netType.toString(), shopActiList[0].shopid!!, AppUtils.getCurrentDateForShopActi())

//                AppUtils.isShopVisited = false
                Pref.isShopVisited=false
            }
        }
        else {
            shopActiList.forEach {
                if (!it.isDurationCalculated && !it.isUploaded) {
                    AppUtils.changeLanguage(this, "en")
                    val endTimeStamp = System.currentTimeMillis().toString()
                    changeLocale()
                    val startTimestamp = it.startTimeStamp

                    val duration = AppUtils.getTimeFromTimeSpan(startTimestamp, endTimeStamp)
                    val totalMinute = AppUtils.getMinuteFromTimeStamp(startTimestamp, endTimeStamp)

                    AppDatabase.getDBInstance()!!.shopActivityDao().updateEndTimeOfShop(endTimeStamp, it.shopid!!, AppUtils.getCurrentDateForShopActi(), it.startTimeStamp)
                    AppDatabase.getDBInstance()!!.shopActivityDao().updateIsUploaded(false, it.shopid!!, AppUtils.getCurrentDateForShopActi(), it.startTimeStamp)
                    AppDatabase.getDBInstance()!!.shopActivityDao().updateTotalMinuteForDayOfShop(it.shopid!!, totalMinute, AppUtils.getCurrentDateForShopActi(), it.startTimeStamp)
                    AppDatabase.getDBInstance()!!.shopActivityDao().updateTimeDurationForDayOfShop(it.shopid!!, duration, AppUtils.getCurrentDateForShopActi(), it.startTimeStamp)
                    AppDatabase.getDBInstance()!!.shopActivityDao().updateDurationAvailable(true, it.shopid!!, AppUtils.getCurrentDateForShopActi(), it.startTimeStamp)
                    AppDatabase.getDBInstance()!!.shopActivityDao().updateOutTime(AppUtils.getCurrentTimeWithMeredian(), it.shopid!!, AppUtils.getCurrentDateForShopActi(), it.startTimeStamp)
                    AppDatabase.getDBInstance()!!.shopActivityDao().updateOutLocation(LocationWizard.getNewLocationName(this, Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble()), it.shopid!!, AppUtils.getCurrentDateForShopActi(), it.startTimeStamp)

                    val netStatus = if (AppUtils.isOnline(this))
                        "Online"
                    else
                        "Offline"

                    val netType = if (AppUtils.getNetworkType(this).equals("wifi", ignoreCase = true))
                        AppUtils.getNetworkType(this)
                    else
                        "Mobile ${AppUtils.mobNetType(this)}"

                    AppDatabase.getDBInstance()!!.shopActivityDao().updateDeviceStatusReason(AppUtils.getDeviceName(), AppUtils.getAndroidVersion(),
                            AppUtils.getBatteryPercentage(this).toString(), netStatus, netType.toString(), it.shopid!!, AppUtils.getCurrentDateForShopActi(), it.startTimeStamp)

//                    AppUtils.isShopVisited = false
                    Pref.isShopVisited=false
                }
            }
        }
    }

    private fun syncIdealLocData() {
        if (!shouldIdealLocationUpdate())
            return

        if (Pref.user_id.isNullOrEmpty())
            return

        val syncList = AppDatabase.getDBInstance()!!.idleLocDao().getDataSyncStateWise(false)

        if (syncList == null || syncList.isEmpty())
            return

        val idealLocList = ArrayList<IdealLocationInputDataParams>()

        for (i in syncList.indices) {
            val idealLocation = IdealLocationInputDataParams()
            idealLocation.ideal_id = syncList[i].ideal_id!!
            idealLocation.end_ideal_date_time = syncList[i].end_date_time!!
            idealLocation.end_ideal_lat = syncList[i].end_lat!!
            idealLocation.end_ideal_lng = syncList[i].end_long!!
            idealLocation.start_ideal_date_time = syncList[i].start_date_time!!
            idealLocation.start_ideal_lat = syncList[i].start_lat!!
            idealLocation.start_ideal_lng = syncList[i].start_long!!
            idealLocList.add(idealLocation)
        }

        val idealLoc = IdealLocationInputParams()
        idealLoc.session_token = Pref.session_token!!
        idealLoc.user_id = Pref.user_id!!
        idealLoc.location_list = idealLocList

        val repository = IdealLocationRepoProvider.provideIdealLocationRepository()

        Timber.d("syncIdealLocation : REQUEST")

        compositeDisposable.add(
                repository.idealLocation(idealLoc)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
//                        .timeout(60 * 1, TimeUnit.SECONDS)
                        .subscribe({ result ->
                            val updateShopActivityResponse = result as BaseResponse

                            Timber.d("syncIdealLocation : RESPONSE : " + updateShopActivityResponse.status + ":" + updateShopActivityResponse.message)

                            if (updateShopActivityResponse.status == NetworkConstant.SUCCESS) {
                                for (i in 0 until syncList.size) {
                                    AppDatabase.getDBInstance()!!.idleLocDao().updateIsUploadedAccordingToId(true, syncList[i].id)
                                }
                            }

                        }, { error ->
                            if (error == null) {
                                Timber.d("syncIdealLocation : ERROR : " + "UNEXPECTED ERROR IN LOCATION ACTIVITY API")
                            } else {
                                Timber.d("syncIdealLocation : ERROR : " + error.localizedMessage)
                                error.printStackTrace()
                            }

//                            (mContext as DashboardActivity).showSnackMessage("ERROR")
                        })
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateIdleTime(location: Location, text: String) {
//        if (!AppUtils.isShopVisited) {
            if(!Pref.isShopVisited){
            AppUtils.changeLanguage(this,"en")
            val currentTimeStamp = System.currentTimeMillis()
            changeLocale()

            if (previousIdleTimeStamp != 0L) {
                val interval = currentTimeStamp - previousIdleTimeStamp

                if (TextUtils.isEmpty(startIdleDateTime))
                    startIdleDateTime = AppUtils.getDate(previousIdleTimeStamp) //AppUtils.getCurrentDateTime()

                Timber.e("Fuzed Location: $text start Idle Date Time====> $startIdleDateTime")
                /*Timber.e("Fuzed Location: $text currentIdleTimestamp=====> $currentTimeStamp")
                Timber.e("Fuzed Location: $text previousIdleTimestamp====> $previousIdleTimeStamp")
                Timber.e("Fuzed Location: $text idle interval====> $interval")*/

                val intervalInMins = (interval / 1000) / 60
                Timber.e("Fuzed Location: $text idle interval=====> $intervalInMins min(s)")

                if (startIdleLat == 0.0 && startIdleLong == 0.0) {
                    startIdleLat = location.latitude
                    startIdleLong = location.longitude
                }

                if (intervalInMins >= AppUtils.idle_time.toInt()) {
                    endIdleLat = location.latitude
                    endIdleLong = location.longitude

                    endIdleDateTime = AppUtils.getDate(currentTimeStamp) //AppUtils.getCurrentDateTime()

                    Timber.e("======Idle Location $text========")
                    Timber.e("start lat====> $startIdleLat")
                    Timber.e("start long====> $startIdleLong")
                    Timber.e("end lat====> $endIdleLat")
                    Timber.e("end lat====> $endIdleLong")
                    Timber.e("start date time====> $startIdleDateTime")
                    Timber.e("end date time====> $endIdleDateTime")
                    Timber.e("==================================")

                    saveIdleData()
                    resetData()
                }
            } else
                previousIdleTimeStamp = currentTimeStamp
        } else {
            //Timber.e("======Reset idle data $text========")
            resetData()
        }
    }

    private fun resetData() {
        Timber.e("======Reset idle data========")
        startIdleLat = 0.0
        startIdleLong = 0.0
        startIdleDateTime = ""
        endIdleDateTime = ""
        endIdleLat = 0.0
        endIdleLong = 0.0
        previousIdleTimeStamp = 0L
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveIdleData() {
        AppUtils.changeLanguage(this,"en")
        val random = Random()
        val m = random.nextInt(9999 - 1000) + 1000
        changeLocale()
        val list = AppDatabase.getDBInstance()!!.idleLocDao().getAll()

        if (list != null && list.isNotEmpty()) {
            val dbTimeStamp = AppUtils.getTimeStamp(list[list.size - 1].end_date_time!!)
            val newTimeStamp = AppUtils.getTimeStamp(startIdleDateTime)

            if (newTimeStamp < dbTimeStamp) {
                Timber.e("================Fuzed Location: Invalid ideal value===================")
                return
            }
        }

        val idleLoc = IdleLocEntity()
        idleLoc.ideal_id = Pref.user_id + "_ideal_" + m
        idleLoc.end_date_time = endIdleDateTime
        idleLoc.start_date_time = startIdleDateTime
        idleLoc.start_lat = startIdleLat.toString()
        idleLoc.start_long = startIdleLong.toString()
        idleLoc.end_lat = endIdleLat.toString()
        idleLoc.end_long = endIdleLong.toString()
        AppDatabase.getDBInstance()!!.idleLocDao().insert(idleLoc)

        val notification = NotificationUtils(getString(R.string.app_name), "", "", "")
        notification.sendIdealNotificaiton(this, "${AppUtils.hiFirstNameText()}! Your device detected you in the same nearby " +
                "location from last " + AppUtils.idle_time + " minutes. Thanks.")

        val intent = Intent()
        intent.action = "IDEAL_LOC_BROADCAST"
        intent.putExtra("startTime", startIdleDateTime)
        intent.putExtra("endTime", endIdleDateTime)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        Timber.e("================Fuzed Location: Ideal data added to db===================")
    }

    private fun changeLocale() {
        val intent = Intent()
        intent.action = "CHANGE_LOCALE_BROADCAST"
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun continueToAccurateFlow(location: Location) {
        if (Pref.login_date != AppUtils.getCurrentDateChanged())
            return

        /*Pref.latitude = location.latitude.toString()
        Pref.longitude = location.longitude.toString()*/

        if (Pref.user_id.isNullOrEmpty())
            return

        //cancelShopDuration()

        if (mLastLocation == null) {
            val locList = AppDatabase.getDBInstance()!!.userLocationDataDao().getLocationUpdateForADay(AppUtils.getCurrentDateForShopActi())

            if (locList != null && locList.isNotEmpty()) {
                val loc = Location("")
                loc.longitude = locList[locList.size - 1].longitude.toDouble()
                loc.latitude = locList[locList.size - 1].latitude.toDouble()
                mLastLocation = loc
                Timber.e("=========Fuzed Location: First time get accurate location after service stopped==========")
            } else {
                mLastLocation = location
                Timber.e("=========Fuzed Location: First time get accurate location==========")
            }

            AppUtils.changeLanguage(this,"en")
            Pref.prevTimeStamp = System.currentTimeMillis()
            changeLocale()
            mLastLocationForAssumtion = mLastLocation
            updateLocation(mLastLocation!!, location)
            //saveAllLocation(location)
            LOCATION_ACTIVITY_INTERVAL = 5

        } else /*if (shouldLocationUpdate())*/ {
            /*saveAllLocation(location)
            if (shouldLocationUpdate())*/
            updateLocation(mLastLocation!!, location)
            mLastLocation = location
            LOCATION_ACTIVITY_INTERVAL = 5
            val i = Intent("android.intent.action.MAIN").putExtra("some_msg", "LOCATION_DETECTED")
            this.sendBroadcast(i)
        }
    }


    private fun saveAllLocation(location: Location) {
        /*if (location.latitude == 0.0 || location.longitude == 0.0)
            return

        val userlocation = LocationEntity()
        userlocation.latitude = location.latitude.toString()
        userlocation.longitude = location.longitude.toString()
        userlocation.accuracy = location.accuracy.toString()
        userlocation.locationName = LocationWizard.getLocationName(this, userlocation.latitude.toDouble(), userlocation.longitude.toDouble())
        userlocation.date_time = AppUtils.getCurrentDateTime()
        userlocation.date = AppUtils.getCurrentDateForShopActi()

        Timber.d("====================all location (Location Fuzed Service)=====================")
        Timber.d("accuracy=====> " + userlocation.accuracy)
        Timber.d("lat====> " + userlocation.latitude)
        Timber.d("long=====> " + userlocation.longitude)
        Timber.d("location=====> " + userlocation.locationName)
        Timber.d("date time=====> " + userlocation.date_time)

        AppDatabase.getDBInstance()!!.locationDao().insert(userlocation)
        Timber.d("==============all location added to db (Location Fuzed Service)=======================")*/
    }

    /*private fun cancelShopDuration() {
        val list=AppDatabase.getDBInstance()!!.shopActivityDao().getDurationCalculatedVisitedShopForADay()
    }*/


    private fun updateInaccurateLocation(location: Location) {
        if (location.latitude == 0.0 || location.longitude == 0.0)
            return

        val timestamp = LocationWizard.getTimeStamp()

        val list = AppDatabase.getDBInstance()!!.inaccurateLocDao().all

        if (list != null && list.size > 0) {
            if (timestamp <= list[list.size - 1].timestamp!!)
                return
        }

        val userlocation = InaccurateLocationDataEntity()
        userlocation.latitude = location.latitude.toString()
        userlocation.longitude = location.longitude.toString()
        userlocation.accuracy = location.accuracy.toString()
        userlocation.locationName = LocationWizard.getLocationName(this, userlocation.latitude.toDouble(), userlocation.longitude.toDouble())
        userlocation.timestamp = timestamp //LocationWizard.getTimeStamp()
        userlocation.time = LocationWizard.getFormattedTime24Hours(true)
        userlocation.meridiem = LocationWizard.getMeridiem()
        userlocation.hour = LocationWizard.getHour()
        userlocation.minutes = LocationWizard.getMinute()
        userlocation.isUploaded = false
        userlocation.shops = AppDatabase.getDBInstance()!!.shopActivityDao().getTotalShopVisitedForADay(AppUtils.getCurrentDateForShopActi()).size.toString()
        userlocation.updateDate = AppUtils.getCurrentDateForShopActi()
        userlocation.updateDateTime = AppUtils.getCurrentDateTime()

        Timber.d("====================Current inaccurate location (Location Fuzed Service)=====================")
        Timber.d("accuracy=====> " + userlocation.accuracy)
        Timber.d("lat====> " + userlocation.latitude)
        Timber.d("long=====> " + userlocation.longitude)
        Timber.d("location=====> " + userlocation.locationName)
        Timber.d("date time=====> " + userlocation.updateDateTime)

        AppDatabase.getDBInstance()!!.inaccurateLocDao().insertAll(userlocation)
        Timber.d("==============inaccurate location added to db (Location Fuzed Service)=======================")
    }


    private fun syncMeetingData() {

        Timber.e("===============Sync Meeting Data(Location Fuzed Service)==============")

        if (!shouldUpdateMeetingDuration()) {
            Timber.e("===============Should not call sync Meeting Data(Location Fuzed Service)==============")
            return
        }

        if (TextUtils.isEmpty(Pref.user_id))
            return

        val list = AppDatabase.getDBInstance()!!.addMeetingDao().durationAvailableSyncWise(true, false)

        if (list != null && list.isNotEmpty()) {

            Timber.e("IS MEETING UPDATING (LOCATION FUZED SERVICE)===========> $isMeetingUpdating")

            if (isMeetingUpdating)
                return

            isMeetingUpdating = true

            val meeting = MeetingDurationInputParams()
            meeting.session_token = Pref.session_token!!
            meeting.user_id = Pref.user_id!!

            val meetingDataList = ArrayList<MeetingDurationDataModel>()

            for (i in list.indices) {
                val meetingData = MeetingDurationDataModel()
                meetingData.duration = list[i].duration_spent!!
                meetingData.latitude = list[i].lattitude!!
                meetingData.longitude = list[i].longitude!!
                meetingData.remarks = list[i].remakrs!!
                meetingData.meeting_type_id = list[i].meetingTypeId!!
                meetingData.distance_travelled = list[i].distance_travelled!!
                meetingData.date = list[i].date!!
                meetingData.address = list[i].address!!
                meetingData.pincode = list[i].pincode!!
                meetingData.date_time = list[i].date_time!!

                meetingDataList.add(meetingData)
            }

            meeting.meeting_list = meetingDataList

            Timber.d("========UPLOAD MEETING DATA INPUT PARAMS (LOCATION FUZED SERVICE)======")
            Timber.d("USER ID======> " + meeting.user_id)
            Timber.d("SESSION ID======> " + meeting.session_token)
            Timber.d("MEETING LIST SIZE=========> " + meeting.meeting_list.size)
            Timber.d("========================================================================")

            val repository = ShopDurationRepositoryProvider.provideShopDurationRepository()
            BaseActivity.compositeDisposable.add(
                    repository.meetingDuration(meeting)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ result ->
                                val response = result as BaseResponse
                                Timber.d("UPLOAD MEETING DATA : " + "RESPONSE : " + response.status + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + response.message)
                                if (response.status == NetworkConstant.SUCCESS) {

                                    for (i in list.indices) {
                                        AppDatabase.getDBInstance()!!.addMeetingDao().updateIsUploaded(true, list[i].id)
                                    }
                                }
                                isMeetingUpdating = false

                            }, { error ->
                                Timber.d("UPLOAD MEETING DATA : " + "ERROR : " + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + error.localizedMessage)
                                error.printStackTrace()
                                isMeetingUpdating = false
                            })
            )
        } else
            Timber.e("============NO UNSYNC DURATION COMPLETED MEETING AVAILABLE (LOCATION FUZED SERVICE)================")
    }

    private fun syncLocationActivity() {

        Timber.d("syncLocationActivity : ENTER")
        println("tag_loc_sync 0")

        if (!shouldLocationActivityUpdate())
            return

        println("tag_loc_sync 1")

        Timber.d("syncLocationActivity : Call Api")

        if (Pref.user_id.isNullOrEmpty())
            return

        val syncList = AppDatabase.getDBInstance()!!.userLocationDataDao().getLocationUpdateForADayNotSyn(AppUtils.getCurrentDateForShopActi(), true)

        val list = AppDatabase.getDBInstance()!!.userLocationDataDao().getLocationNotUploaded(false)
        if (list.isEmpty() || isLocationActivityUpdating)
            return

        isLocationActivityUpdating = true

        //writeDataToFile(list)

        /*val random = Random()
        val m = random.nextInt(9999 - 1000) + 1000*/

        val locationUpdateReq = LocationUpdateRequest()
        locationUpdateReq.user_id = Pref.user_id
        locationUpdateReq.session_token = Pref.session_token

        val locationList: MutableList<LocationData> = ArrayList()
        val locationListAllId: MutableList<LocationData> = ArrayList()
        val finalLocationListAllId: MutableList<LocationData> = ArrayList()
        var distanceCovered: Double = 0.0
        var timeStamp = 0L

        val allLocationList = AppDatabase.getDBInstance()!!.userLocationDataDao().getLocationUpdateForADay(AppUtils.getCurrentDateForShopActi()).toMutableList()
        //val unSyncList: MutableList<UserLocationDataEntity> = ArrayList()
        val apiLocationList: MutableList<UserLocationDataEntity> = ArrayList()

//        for (i in 0 until list.size) {
//            if (list[i].latitude == null || list[i].longitude == null)
//                continue
//            val locationData = LocationData()
//
//
//            /*locationData.locationId = list[i].locationId.toString()
//            locationData.date = list[i].updateDateTime
//            locationData.distance_covered = list[i].distance
//            locationData.latitude = list[i].latitude
//            locationData.longitude = list[i].longitude
//            locationData.location_name = list[i].locationName
//            locationData.shops_covered = list[i].shops
//            locationData.last_update_time = list[i].time + " " + list[i].meridiem*/
//
//            if (syncList == null || syncList.isEmpty()) {
//                if (i == 0) {
//                    locationData.locationId = list[i].locationId.toString()
//                    locationData.date = list[i].updateDateTime
//                    locationData.distance_covered = list[i].distance
//                    locationData.latitude = list[i].latitude
//                    locationData.longitude = list[i].longitude
//                    locationData.location_name = list[i].locationName
//                    locationData.shops_covered = list[i].shops
//                    locationData.last_update_time = list[i].time + " " + list[i].meridiem
//                    locationList.add(locationData)
//                }
//            }
//
//            distanceCovered += list[i].distance.toDouble()
//
//            if (i != 0 && i % 5 == 0) {
//                locationData.locationId = list[i].locationId.toString()
//                locationData.date = list[i].updateDateTime
//
//                locationData.distance_covered = distanceCovered.toString()
//
//                locationData.latitude = list[i].latitude
//                locationData.longitude = list[i].longitude
//                locationData.location_name = list[i].locationName
//                locationData.shops_covered = list[i].shops
//                locationData.last_update_time = list[i].time + " " + list[i].meridiem
//                locationList.add(locationData)
//
//                distanceCovered = 0.0
//            }
//
//            /*if (TextUtils.isEmpty(list[i].unique_id)) {
//                //list[i].unique_id = m.toString()
//                AppDatabase.getDBInstance()!!.userLocationDataDao().updateUniqueId(m.toString(), list[i].locationId)
//            }*/
//
//            val locationDataAll = LocationData()
//            locationDataAll.locationId = list[i].locationId.toString()
//            locationListAllId.add(locationDataAll)
//        }

        var selectedTimeStamp = 0L
        var allTimeStamp = 0L
        var fiveMinsRowGap = 5
        println("tag_loc_sync 2")
        if (Pref.locationTrackInterval == "30")
            fiveMinsRowGap = 10
        println("tag_loc_sync 3")
        for (i in 0 until allLocationList.size) {
            println("tag_loc_sync 4")
            if (allLocationList[i].latitude == null || allLocationList[i].longitude == null)
                continue

            //apiLocationList.add(allLocationList[i])
            if (i == 0) {
                apiLocationList.add(allLocationList[i])
                //selectedTimeStamp = allLocationList[i].timestamp.toLong()
            }

            distanceCovered += allLocationList[i].distance.toDouble()

            if (!TextUtils.isEmpty(allLocationList[i].home_duration)) {
                Timber.e("Home Duration (Location Fuzed Service)=================> ${allLocationList[i].home_duration}")
                Timber.e("Time (Location Fuzed Service)=================> ${allLocationList[i].time}")
                val arr = allLocationList[i].home_duration?.split(":".toRegex())?.toTypedArray()
                timeStamp += arr?.get(2)?.toInt()?.toLong()!!
                timeStamp += 60 * arr[1].toInt().toLong()
                timeStamp += 3600 * arr[0].toInt().toLong()
            }

            if (i != 0 /*&& i % 5 == 0*/) {
                try {

                    val timeStamp_ = allLocationList[i].timestamp.toLong()

                    if (i % fiveMinsRowGap == 0) {
                        allLocationList[i].distance = distanceCovered.toString()

                        if (timeStamp != 0L) {
                            val hh = timeStamp / 3600
                            timeStamp %= 3600
                            val mm = timeStamp / 60
                            timeStamp %= 60
                            val ss = timeStamp
                            allLocationList[i].home_duration = AppUtils.format(hh) + ":" + AppUtils.format(mm) + ":" + AppUtils.format(ss)

                            /*Timber.e("Final Home Duration (Location Fuzed Service)=================> ${allLocationList[i].home_duration}")
                            Timber.e("Time (Location Fuzed Service)=================> ${allLocationList[i].time}")*/
                        }

                        apiLocationList.add(allLocationList[i])
                        distanceCovered = 0.0
                        timeStamp = 0L
                        //selectedTimeStamp = allLocationList[i].timestamp.toLong()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()

                    allLocationList[i].distance = distanceCovered.toString()
                    if (timeStamp != 0L) {
                        val hh = timeStamp / 3600
                        timeStamp %= 3600
                        val mm = timeStamp / 60
                        timeStamp %= 60
                        val ss = timeStamp
                        allLocationList[i].home_duration = AppUtils.format(hh) + ":" + AppUtils.format(mm) + ":" + AppUtils.format(ss)

                        /*Timber.e("Final Home Duration (Location Fuzed Service)=================> ${allLocationList[i].home_duration}")
                        Timber.e("Time (Location Fuzed Service)=================> ${allLocationList[i].time}")*/
                    }
                    apiLocationList.add(allLocationList[i])
                    distanceCovered = 0.0
                    timeStamp = 0L
                }
            }

            /*if (!allLocationList[i].isUploaded) {
                val locationDataAll = LocationData()
                locationDataAll.locationId = allLocationList[i].locationId.toString()
                locationListAllId.add(locationDataAll)

                //storeId(allLocationList[i], locationListAllId)
            }*/
        }

        for (i in apiLocationList.indices) {
            println("tag_loc_sync 5")
            if (!apiLocationList[i].isUploaded) {

                Timber.e("Final Home Duration (Location Fuzed Service)=================> ${apiLocationList[i].home_duration}")
                Timber.e("Time (Location Fuzed Service)=================> ${apiLocationList[i].time} ${apiLocationList[i].meridiem}")

                val locationData = LocationData()

                locationData.locationId = apiLocationList[i].locationId.toString()
                locationData.date = apiLocationList[i].updateDateTime
                locationData.distance_covered = apiLocationList[i].distance
                locationData.latitude = apiLocationList[i].latitude
                locationData.longitude = apiLocationList[i].longitude
                locationData.location_name = apiLocationList[i].locationName
                locationData.shops_covered = apiLocationList[i].shops
                locationData.last_update_time = apiLocationList[i].time + " " + apiLocationList[i].meridiem
                locationData.meeting_attended = apiLocationList[i].meeting
                locationData.home_distance = apiLocationList[i].home_distance
                locationData.network_status = apiLocationList[i].network_status
                locationData.battery_percentage = apiLocationList[i].battery_percentage
                locationData.home_duration = apiLocationList[i].home_duration
                locationList.add(locationData)


                val locationDataAll = LocationData()
                locationDataAll.locationId = apiLocationList[i].locationId.toString()
                locationListAllId.add(locationDataAll)
            }
        }
        println("tag_loc_sync 6")
        Timber.d("syncLocationActivity fuzerService : locationList size"+locationList.size.toString() )
        if (locationList.size > 0) {
            println("tag_loc_sync 7")
            locationUpdateReq.location_details = locationList
            val repository = LocationUpdateRepositoryProviders.provideLocationUpdareRepository()

            Timber.d("syncLocationActivity fuzerService : REQUEST")

            compositeDisposable.add(
                    repository.sendLocationUpdate(locationUpdateReq)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
//                        .timeout(60 * 1, TimeUnit.SECONDS)
                            .subscribe({ result ->
                                isLocationActivityUpdating = false
                                var updateShopActivityResponse = result as BaseResponse

                                Timber.d("syncLocationActivity fuzerService : RESPONSE : " + updateShopActivityResponse.status + ":" + updateShopActivityResponse.message)

                                if (updateShopActivityResponse.status == NetworkConstant.SUCCESS) {
                                    for (i in 0 until locationListAllId/*locationList*/.size) {

                                        //AppDatabase.getDBInstance()!!.userLocationDataDao().updateIsUploaded(true, locationList[i].locationId.toInt())

                                        if (syncList != null && syncList.isNotEmpty()) {

                                            if (i == 0)
                                                AppDatabase.getDBInstance()!!.userLocationDataDao().updateIsUploadedFor5Items(true, syncList[syncList.size - 1].locationId.toInt(), locationListAllId[i].locationId.toInt())
                                            else
                                                AppDatabase.getDBInstance()!!.userLocationDataDao().updateIsUploadedFor5Items(true, locationListAllId[i - 1].locationId.toInt(), locationListAllId[i].locationId.toInt())

                                        } else {
                                            if (i == 0)
                                                AppDatabase.getDBInstance()!!.userLocationDataDao().updateIsUploaded(true, locationListAllId[i].locationId.toInt())
                                            else
                                                AppDatabase.getDBInstance()!!.userLocationDataDao().updateIsUploadedFor5Items(true, locationListAllId[i - 1].locationId.toInt(), locationListAllId[i].locationId.toInt())
                                        }
                                    }
                                }

                            }, { error ->
                                isLocationActivityUpdating = false
                                if (error == null) {
                                    Timber.d("syncLocationActivity fuzerService : ERROR : " + "UNEXPECTED ERROR IN LOCATION ACTIVITY API")
                                } else {
                                    Timber.d("syncLocationActivity fuzerService : ERROR : " + error.localizedMessage)
                                    error.printStackTrace()
                                }


//                            (mContext as DashboardActivity).showSnackMessage("ERROR")
                            })
            )
        } else
            isLocationActivityUpdating = false
    }

    private fun storeId(location: UserLocationDataEntity, locationListAllId: MutableList<LocationData>) {
        val locationDataAll = LocationData()
        locationDataAll.locationId = location.locationId.toString()
        locationListAllId.add(locationDataAll)
    }

    private fun writeDataToFile(list: List<UserLocationDataEntity>) {
        val company = JSONArray()

        for (i in 0 until list.size) {
            if (list[i].latitude == null || list[i].longitude == null)
                continue
            val jsonObject = JSONObject()
            jsonObject.put("date", list[i].updateDateTime)
            jsonObject.put("distance_covered", list[i].distance)
            jsonObject.put("last_update_time", list[i].time + " " + list[i].meridiem)
            jsonObject.put("latitude", list[i].latitude)
            jsonObject.put("longitude", list[i].longitude)
            jsonObject.put("locationId", list[i].locationId)
            jsonObject.put("location_name", list[i].locationName)
            jsonObject.put("shops_covered", list[i].shops)
            company.put(jsonObject)
        }

        val parentObject = JSONObject()
        parentObject.put("location_details", company)

        try {
            var output: Writer? = null
            val folderPath = FTStorageUtils.getFolderPath(this)
            val file = File(folderPath + "/FTS_Location_" + System.currentTimeMillis() + ".txt")
            output = BufferedWriter(FileWriter(file))
            output.write(parentObject.toString())
            output.close()
            Log.e("location", "Value saved")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shouldLocationUpdate(): Boolean {
//        long serverTimeStamp=//whatever your server timestamp is, however you are getting it.
////You may have to use Long.parseLong(serverTimestampString) to convert it from a string

        if (AppDatabase.getDBInstance()!!.userLocationDataDao().all.size == 0)
            return true

//3000(millliseconds in a second)*60(seconds in a minute)*5(number of minutes)=300000
        if (abs(System.currentTimeMillis() - Pref.prevTimeStamp) > 1000 * 60 * LOCATION_ACTIVITY_INTERVAL) {
            Pref.prevTimeStamp = System.currentTimeMillis()
            return true
            //server timestamp is within 5 minutes of current system time
        } else {
            return AppDatabase.getDBInstance()!!.userLocationDataDao().all.size == 0
        }


    }


    private fun shouldShopActivityUpdate(): Boolean {
        AppUtils.changeLanguage(this,"en")
        return if (abs(System.currentTimeMillis() - Pref.prevShopActivityTimeStamp) > 1000 * 60 * 14) {
            Pref.prevShopActivityTimeStamp = System.currentTimeMillis()
            changeLocale()
            true
            //server timestamp is within 5 minutes of current system time
        } else {
            changeLocale()
            false
        }
    }

    private fun shouldEndShopDurationUpdate(): Boolean {
        AppUtils.changeLanguage(this,"en")
        return if (abs(System.currentTimeMillis() - Pref.prevEnsShopDurationTimeStamp) > 1000 * 60 * 4) {
            Pref.prevEnsShopDurationTimeStamp = System.currentTimeMillis()
            changeLocale()
            true
            //server timestamp is within 5 minutes of current system time
        } else {
            changeLocale()
            false
        }
    }

    private fun shouldLocationActivityUpdate(): Boolean {
        AppUtils.changeLanguage(this,"en")
        return if (abs(System.currentTimeMillis() - Pref.prevLocationActivityTimeStamp) > 1000 * 60 * 4) {
            Pref.prevLocationActivityTimeStamp = System.currentTimeMillis()
            changeLocale()
            true
            //server timestamp is within 5 minutes of current system time
        } else {
            changeLocale()
            false
        }
    }

    private fun shouldUpdateRevisitGarbage(): Boolean {
        AppUtils.changeLanguage(this,"en")
        return if (abs(System.currentTimeMillis() - Pref.prevRevisitGarbageTimeStamp) > 1000 * 60 * 60 * 5) {
            Pref.prevRevisitGarbageTimeStamp = System.currentTimeMillis()
            changeLocale()
            true
            //server timestamp is within 5 minutes of current system time
        } else {
            changeLocale()
            false
        }
    }

    private fun shouldIdealLocationUpdate(): Boolean {
        AppUtils.changeLanguage(this,"en")
        return if (abs(System.currentTimeMillis() - Pref.prevIdealLocationActivityTimeStamp) > 1000 * 60 * 3) {
            Pref.prevIdealLocationActivityTimeStamp = System.currentTimeMillis()
            changeLocale()
            true
            //server timestamp is within 5 minutes of current system time
        } else {
            changeLocale()
            false
        }
    }

    private fun shouldShopDurationComplete(): Boolean {
        AppUtils.changeLanguage(this,"en")
        return if (abs(System.currentTimeMillis() - Pref.prevShopDurationTimeStamp) > 1000 * 60 * 5) {
            Pref.prevShopDurationTimeStamp = System.currentTimeMillis()
            changeLocale()
            true
            //server timestamp is within 5 minutes of current system time
        } else {
            changeLocale()
            false
        }
    }

    private fun shouldRevisitGarbageFixComplete(): Boolean {
        AppUtils.changeLanguage(this,"en")
        return if (abs(System.currentTimeMillis() - Pref.prevRevisitGarbageDurationTimeStamp) > 1000 * 60 * 10) {
            Pref.prevRevisitGarbageDurationTimeStamp = System.currentTimeMillis()
            changeLocale()
            true
            //server timestamp is within 5 minutes of current system time
        } else {
            changeLocale()
            false
        }
    }

    private fun shouldUpdateMeetingDuration(): Boolean {
        AppUtils.changeLanguage(this,"en")
        Timber.e("PREVIOUS MEETING SYNC API CALL TIME==================> " + getDateTimeFromTimeStamp(Pref.prevMeetingDurationTimeStamp))
        Timber.e("CURRENT TIME==================> " + getDateTimeFromTimeStamp(System.currentTimeMillis()))

        return if (abs(System.currentTimeMillis() - Pref.prevMeetingDurationTimeStamp) > 1000 * 60 * 10) {
            Pref.prevMeetingDurationTimeStamp = System.currentTimeMillis()
            changeLocale()
            true
            //server timestamp is within 10 minutes of current system time
        } else {
            changeLocale()
            false
        }
    }

    private fun shouldBatNetSaveDuration(): Boolean {
        AppUtils.changeLanguage(this,"en")
        Timber.e("PREVIOUS BAT NET SAVE API CALL TIME==================> " + getDateTimeFromTimeStamp(Pref.prevBatNetSaveTimeStamp))
        Timber.e("CURRENT TIME==================> " + getDateTimeFromTimeStamp(System.currentTimeMillis()))

        return if (abs(System.currentTimeMillis() - Pref.prevBatNetSaveTimeStamp) > 1000 * 60 * Pref.appInfoMins.toInt()) {
            Pref.prevBatNetSaveTimeStamp = System.currentTimeMillis()
            changeLocale()
            true
            //server timestamp is within 10 minutes of current system time
        } else {
            changeLocale()
            false
        }
    }

    private fun shouldBatNetSyncDuration(): Boolean {
        AppUtils.changeLanguage(this,"en")
        Timber.e("PREVIOUS BAT NET SYNC API CALL TIME==================> " + getDateTimeFromTimeStamp(Pref.prevBatNetSyncTimeStamp))
        Timber.e("CURRENT TIME==================> " + getDateTimeFromTimeStamp(System.currentTimeMillis()))

        return if (abs(System.currentTimeMillis() - Pref.prevBatNetSyncTimeStamp) > 1000 * 60 * 10) {
            Pref.prevBatNetSyncTimeStamp = System.currentTimeMillis()
            changeLocale()
            true
            //server timestamp is within 10 minutes of current system time
        } else {
            changeLocale()
            false
        }
    }

    private fun shouldGpsNetSyncDuration(): Boolean {
        AppUtils.changeLanguage(this,"en")

        return if (abs(System.currentTimeMillis() - Pref.prevGpsNetSyncTimeStampService) > 1000 * 60 * Pref.GPSNetworkIntervalMins.toInt()) {
            Pref.prevGpsNetSyncTimeStampService = System.currentTimeMillis()
            changeLocale()
            true
            //server timestamp is within 10 minutes of current system time
        } else {
            changeLocale()
            false
        }
    }

    private fun shouldUpdateAttendanceNotificationDuration(timeGapMin:Int): Boolean {
        AppUtils.changeLanguage(this,"en")
        return if (abs(System.currentTimeMillis() - Pref.prevAttendanceNotiDurationTimeStamp) > 1000 * 60 * timeGapMin) {
            Pref.prevAttendanceNotiDurationTimeStamp = System.currentTimeMillis()
            changeLocale()
            true
            //server timestamp is within 10 minutes of current system time
        } else {
            changeLocale()
            false
        }
    }

    private fun shouldUpdateLocationNotificationDuration(): Boolean {
        AppUtils.changeLanguage(this,"en")
        return if (abs(System.currentTimeMillis() - Pref.prevLocNotiDurationTimeStamp) > 1000 * 60 * Pref.currentLocationNotificationMins.toInt()) {
            Pref.prevLocNotiDurationTimeStamp = System.currentTimeMillis()
            changeLocale()
            true
            //server timestamp is within 10 minutes of current system time
        } else {
            changeLocale()
            false
        }
    }

    private fun shouldCheckHomeLocationReason(): Boolean {
        AppUtils.changeLanguage(this,"en")
        return if (abs(System.currentTimeMillis() - Pref.prevHomeLocReasonTimeStamp) > 1000 * 60 * Pref.homeLocReasonCheckMins.toInt()) {
            Pref.prevHomeLocReasonTimeStamp = System.currentTimeMillis()
            changeLocale()
            true
            //server timestamp is within 10 minutes of current system time
        } else {
            changeLocale()
            false
        }
    }

    private fun shouldCallOrderCollectionAlertCheck(): Boolean {
        AppUtils.changeLanguage(this,"en")
        return if (abs(System.currentTimeMillis() - Pref.prevOrderCollectionCheckTimeStamp) > 1000 * 60 * 360) {

            if (Pref.prevOrderCollectionCheckTimeStamp == 0L) {
                Pref.prevOrderCollectionCheckTimeStamp = System.currentTimeMillis()
                changeLocale()
                false
            } else {
                Pref.prevOrderCollectionCheckTimeStamp = System.currentTimeMillis()
                changeLocale()
                true
            }
            //server timestamp is within 2 minutes of current system time
        } else {
            changeLocale()
            false
        }

    }

    fun addLocationData(location: UserLocationDataEntity) {

        Timber.d("======Current valid location (Location Fuzed Service)======")
        Timber.d("distance=====> " + location.distance)
        Timber.d("lat====> " + location.latitude)
        Timber.d("long=====> " + location.longitude)
        Timber.d("location=====> " + location.locationName)
        Timber.d("date time=====> " + location.updateDateTime)
        Timber.d("network_status=====> " + location.network_status)
        Timber.d("battery_percentage=====> " + location.battery_percentage)

        //AppDatabase.getDBInstance()!!.userLocationDataDao().insertAll(location)
//        syncLocationActivity()

        //Timber.d("=====================location added to db (Location Fuzed Service)======================")

        var intervalInSec = 0L

        try {

            if (mLastLocationForAssumtion == null) {
                mLastLocationForAssumtion = mLastLocation
            }

            val distanceCoverd = LocationWizard.getDistance(mLastLocationForAssumtion?.latitude!!, mLastLocationForAssumtion?.longitude!!,
                    location.latitude.toDouble(), location.longitude.toDouble())

            AppUtils.changeLanguage(this,"en")
            val currentTimeStamp = System.currentTimeMillis()
            changeLocale()

            val interval = currentTimeStamp - previousTimeStamp

            /* Timber.e("Fuzed Location: currentTimestamp=====> $currentTimeStamp")
             Timber.e("Fuzed Location: previousTimestamp====> $previousTimeStamp")
             Timber.e("Fuzed Location: interval====> $interval")*/

            val intervalInMins = (interval / 1000) / 60
            Timber.e("Fuzed Location: interval=====> $intervalInMins min(s)")

            intervalInSec = (interval / 1000)
            Timber.e("Fuzed Location: interval=====> $intervalInSec sec(s)")

//        if (/*userlocation.*/speed.toDouble() in 0.0..50.0)
//            assumedDistanceCover = 200.00
//        else if (/*userlocation.*/speed.toDouble() in 51.0..100.0)
//            assumedDistanceCover = 500.00
//        else if (/*userlocation.*/speed.toDouble() in 101.0..500.0)
//            assumedDistanceCover = 1000.00
//        else if (/*userlocation.*/speed.toDouble() >= 501.0 && /*userlocation.*/speed.toDouble() <= 1000.0)
//            assumedDistanceCover = 1500.00

            /*Timber.d("Location Fuzed Service: Assume Distance===> $assumedDistanceCover")
            Timber.d("Location Fuzed Service: Distance Coverd===> $distanceCoverd")*/

            /*if (previousTimeStamp == 0L) {
            if (assumedDistanceCover >= distanceCoverd * 1000) {

                acuurateLat = location.latitude.toDouble()
                acuurateLong = location.longitude.toDouble()

                AppDatabase.getDBInstance()!!.userLocationDataDao().insertAll(location)
                Timber.d("========Accurate location added to db for first time (Location Fuzed Service)=======")

                assumedDistanceCover = AppUtils.maxDistance.toDouble()
                mLastLocationForAssumtion = mLastLocation
            }
        } else {
            if (intervalInMins == 0L)
                intervalInMins = 1

            if (assumedDistanceCover * intervalInMins >= distanceCoverd * 1000) {

                acuurateLat = location.latitude.toDouble()
                acuurateLong = location.longitude.toDouble()

                AppDatabase.getDBInstance()!!.userLocationDataDao().insertAll(location)
                Timber.d("=======Accurate location added to db (Location Fuzed Service)===========")

                assumedDistanceCover = AppUtils.maxDistance.toDouble()
                mLastLocationForAssumtion = mLastLocation
            } else {
                //assumedDistanceCover += assumedDistanceCover
                Timber.d("=========Distance mismatch for accurate location (Location Fuzed Service)==========")


            }
        }*/

            if (previousTimeStamp == 0L) {

                try {
                    val list = AppDatabase.getDBInstance()!!.userLocationDataDao().all
                    val dbTimeStamp = list[list.size - 1].timestamp
                    val newTimeStamp = location.timestamp

                    val longTime = dbTimeStamp.toLong()

                    if (newTimeStamp > dbTimeStamp) {
                        /*acuurateLat = location.latitude.toDouble()
                        acuurateLong = location.longitude.toDouble()

                        AppUtils.totalDistance = AppUtils.totalDistance + location.distance.toDouble()

                        AppDatabase.getDBInstance()!!.userLocationDataDao().insertAll(location)
                        Timber.d("=======First accurate location added to db (Location Fuzed Service)=======")

                        assumedDistanceCover = AppUtils.maxDistance.toDouble()
                        mLastLocationForAssumtion = mLastLocation*/

                        saveAccurateData(location, "=======First accurate location added to db (Location Fuzed Service)=======")

                    } else
                        Timber.d("=========Invalid timestamp (Location Fuzed Service)==========")
                } catch (e: Exception) {
                    e.printStackTrace()

                    Timber.d("=======dbTimeStamp is api date time (Location Fuzed Service)=======")

                    saveAccurateData(location, "=======Accurate location added to db (Location Fuzed Service)=======")
                }


            }
            else {
                //if (intervalInSec > 30) {

                    val list = AppDatabase.getDBInstance()!!.userLocationDataDao().all
                    val dbTimeStamp = list[list.size - 1].timestamp
                    val newTimeStamp = location.timestamp

                    try {
                        val longTime = dbTimeStamp.toLong()

                        if (newTimeStamp > dbTimeStamp) {
                            saveAccurateData(location, "=======Accurate location added to db (Location Fuzed Service)=======")

                        } else
                            Timber.d("=========Invalid timestamp (Location Fuzed Service)==========")
                    } catch (e: Exception) {
                        e.printStackTrace()

                        Timber.d("=======dbTimeStamp is api date time (Location Fuzed Service)=======")
                        saveAccurateData(location, "=======Accurate location added to db (Location Fuzed Service)=======")
                    }

                /*} else {
                    Timber.d("=========Interval is less than 30 seconds (Location Fuzed Service)==========")
                }*/
            }

            previousTimeStamp = currentTimeStamp

        } catch (e: Exception) {
            e.printStackTrace()
        }

        val i = Intent("android.intent.action.MAIN").putExtra("some_msg", "UPDATE")
        this.sendBroadcast(i)
    }

    private fun saveAccurateData(location: UserLocationDataEntity, text: String) {
        acuurateLat = location.latitude.toDouble()
        acuurateLong = location.longitude.toDouble()

        Timber.d("Pref.totalS2SDistance=====> " + Pref.totalS2SDistance)

        Pref.totalS2SDistance = (Pref.totalS2SDistance.toDouble() + location.distance.toDouble()).toString()

        /*val distance = (Pref.totalS2SDistance.toDouble() + location.distance.toDouble()).toString()
        Pref.totalS2SDistance = String.format("%.2f", distance)*/

        location.visit_distance = Pref.visitDistance



        // 13.0 LocationFuzedService v 4.2.6 Suman 08/05/2024 mantis 0027427 location sync update begin

        /*//harcoded location isUploaded true begin
        location.isUploaded = true
        //harcoded location isUploaded true end*/

        if(Pref.IsRouteUpdateForShopUser == false){
            location.isUploaded = true
        }
        // 13.0 LocationFuzedService v 4.2.6 Suman 08/05/2024 mantis 0027427 location sync update end

        AppDatabase.getDBInstance()!!.userLocationDataDao().insertAll(location)
        Timber.d("Shop to shop distance (At accurate loc save time)====> " + Pref.totalS2SDistance)
        Timber.d(text)

        assumedDistanceCover = AppUtils.maxDistance.toDouble()
        mLastLocationForAssumtion = mLastLocation
    }


    private val mGeofenceList: ArrayList<Geofence> = arrayListOf()
    /**
     * Provides access to the Geofencing API.
     */
    private lateinit var mGeofencingClient: GeofencingClient

    private enum class PendingGeofenceTask {
        ADD, REMOVE, NONE
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private fun getGeofencingRequest(): GeofencingRequest? {
        val builder = GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);
        Timber.d("addGeofences : " + " ,Geofence Size : " + mGeofenceList.size)
        // Return a GeofencingRequest.
        return if (mGeofenceList.size > 0) builder.build() else null
    }

    private lateinit var mGeofencePendingIntent: PendingIntent

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private fun getGeofencePendingIntent(): PendingIntent {
        // Reuse the PendingIntent if we already have it.
        if (::mGeofencePendingIntent.isInitialized) {
            return mGeofencePendingIntent
        }
        Timber.d("geofencePendingIntent : " + " , " + " Time :" + AppUtils.getCurrentDateTime() + " , New Pending Intent for Geofence ")
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return mGeofencePendingIntent
    }


    /**
     * Adds geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressLint("MissingPermission")
    private fun addGeofences() {

        Timber.d("addGeofences : " + " , " + " Time :" + AppUtils.getCurrentDateTime())

        mGeofencingClient = LocationServices.getGeofencingClient(this)
        val request = getGeofencingRequest()

        request?.let {
            mGeofencingClient.addGeofences(request, getGeofencePendingIntent())
                    .addOnCompleteListener(this)
            Timber.d("addGeofences Success: " + " , " + " Time :" + AppUtils.getCurrentDateTime())
        }
    }

    fun populateandAddGeofences() {

        Timber.d("populateandAddGeofences : " + " , " + " Time :" + AppUtils.getCurrentDateTime())

        val list = AppDatabase.getDBInstance()!!.addShopEntryDao().all
        for (i in 0 until list.size) {
            mGeofenceList.add(Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(list[i].shop_id)

                    //Sets the delay between GEOFENCE_TRANSITION_ENTER and GEOFENCE_TRANSITION_DWELLING in milliseconds
//                    .setLoiteringDelay(1000*60)

                    // Set the circular region of this geofence.
                    .setCircularRegion(
                            list[i].shopLat,
                            list[i].shopLong,
                            150f
                    )

                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.
                    .setExpirationDuration(24 * 60 * 60 * 1000)//will expire after a day

                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                    // Create the geofence.
                    .build())
        }
//        removeGeofence()
        addGeofences()
    }

    fun checkNearbyShop() {

        if (isCheckingNearBy)
            return
        isCheckingNearBy = true

        var tempLocation = mLastLocation

        val list = AppDatabase.getDBInstance()!!.addShopEntryDao().all

        if (list.size > 0)
            for (i in 0 until list.size) {
                var shopLat: Double = list[i].shopLat
                var shopLong: Double = list[i].shopLong

                if (shopLat != null && shopLong != null) {
                    var shopLocation = Location("")
                    shopLocation.latitude = shopLat
                    shopLocation.longitude = shopLong
                    var isShopNearby = FTStorageUtils.checkShopPositionWithinRadious(tempLocation, shopLocation, NEARBY_RADIUS)
                    if (isShopNearby && !shoulIBotherToUpdate(list[i].shop_id, AppUtils.getCurrentDateForShopActi())) {
                        updateNearbyShopLocationData(list[i].shopName, list[i].shop_id, list[i].shopId.toString())
//                        Timber.d("Shop Nearby : " + "Shop Name : " + list[i].shopName + "," + list[i].shop_id + " Accuracy :" + list[i].shopId.toString())


                    } else {

                        var shopActiList = AppDatabase.getDBInstance()!!.shopActivityDao().getShopForDay(list[i].shop_id, AppUtils.getCurrentDateForShopActi())
                        if (shopActiList.isEmpty())
                            continue

                        if (!shopActiList[0].isDurationCalculated && !shopActiList[0].isUploaded) {
                            var endTimeStamp = System.currentTimeMillis().toString()
                            var startTimestamp = shopActiList[0].startTimeStamp

                            var duration = AppUtils.getTimeFromTimeSpan(startTimestamp, endTimeStamp)
                            var totalMinute = AppUtils.getMinuteFromTimeStamp(startTimestamp, endTimeStamp)
                            AppDatabase.getDBInstance()!!.shopActivityDao().updateEndTimeOfShop(endTimeStamp, shopActiList[0].shopid!!, AppUtils.getCurrentDateForShopActi())
                            AppDatabase.getDBInstance()!!.shopActivityDao().updateIsUploaded(false, shopActiList[0].shopid!!, AppUtils.getCurrentDateForShopActi())
                            AppDatabase.getDBInstance()!!.shopActivityDao().updateTotalMinuteForDayOfShop(shopActiList[0].shopid!!, totalMinute, AppUtils.getCurrentDateForShopActi())
                            AppDatabase.getDBInstance()!!.shopActivityDao().updateTimeDurationForDayOfShop(shopActiList[0].shopid!!, duration, AppUtils.getCurrentDateForShopActi())
                            AppDatabase.getDBInstance()!!.shopActivityDao().updateDurationAvailable(true, shopActiList[0].shopid!!, AppUtils.getCurrentDateForShopActi())
                        }

                    }


                }
            }

        callShopDurationApi()
        updateShopVisitDuration()
        isCheckingNearBy = false
    }


    fun shoulIBotherToUpdate(shopId: String, date: String): Boolean {
        val list = AppDatabase.getDBInstance()!!.shopActivityDao().getShopForDay(shopId, date)
        return if (list.isEmpty())
            false
        else
            list[0].isDurationCalculated
    }

    fun endShopDuration(){
        var isDurationPendingList = AppDatabase.getDBInstance()!!.shopActivityDao().getShopForDurationWise(false,false)
        if(isDurationPendingList.size > 0){
            for(j in 0..isDurationPendingList.size-1){
                val endTimeStamp = System.currentTimeMillis().toString()
                val totalMinute = AppUtils.getMinuteFromTimeStamp(isDurationPendingList[j].startTimeStamp, endTimeStamp)
                val duration = AppUtils.getTimeFromTimeSpan(isDurationPendingList[j].startTimeStamp, endTimeStamp)

                AppDatabase.getDBInstance()!!.shopActivityDao().updateTotalMinuteForDayOfShop(isDurationPendingList[j].shopid!!, totalMinute,  isDurationPendingList[j].date!!)
                AppDatabase.getDBInstance()!!.shopActivityDao().updateEndTimeOfShop(endTimeStamp, isDurationPendingList[j].shopid!!,  isDurationPendingList[j].date!!)
                AppDatabase.getDBInstance()!!.shopActivityDao().updateTimeDurationForDayOfShop(isDurationPendingList[j].shopid!!, duration,  isDurationPendingList[j].date!!)
                AppDatabase.getDBInstance()!!.shopActivityDao().updateDurationAvailable(true, isDurationPendingList[j].shopid!!,  isDurationPendingList[j].date!!)
                AppDatabase.getDBInstance()!!.shopActivityDao().updateIsUploaded(false, isDurationPendingList[j].shopid!!,  isDurationPendingList[j].date!!)

                AppDatabase.getDBInstance()!!.shopActivityDao().updateOutTime(AppUtils.getCurrentTimeWithMeredian(), isDurationPendingList[j].shopid!!,  isDurationPendingList[j].date!!, isDurationPendingList[j].startTimeStamp)
                AppDatabase.getDBInstance()!!.shopActivityDao().updateOutLocation(LocationWizard.getNewLocationName(this, Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble()), isDurationPendingList[j].shopid!!, isDurationPendingList[j].date!!, isDurationPendingList[j].startTimeStamp)

                val netStatus = if (AppUtils.isOnline(this))
                    "Online"
                else
                    "Offline"

                val netType = if (AppUtils.getNetworkType(this).equals("wifi", ignoreCase = true))
                    AppUtils.getNetworkType(this)
                else
                    "Mobile ${AppUtils.mobNetType(this)}"

                AppDatabase.getDBInstance()!!.shopActivityDao().updateDeviceStatusReason(AppUtils.getDeviceName(), AppUtils.getAndroidVersion(),
                    AppUtils.getBatteryPercentage(this).toString(), netStatus, netType.toString(), isDurationPendingList[j].shopid!!,isDurationPendingList[j].date!!)
            }
        }
    }

    private fun callShopDurationApi() {

        Timber.d("callShopDurationApi : ENTER")

        if (!shouldShopActivityUpdate())
            return

        if(!AppUtils.isOnline(this)){
            return
        }

        Log.e("Location Fuzed Srvice", "isShopActivityUpdating=============> $isShopActivityUpdating")

        if (Pref.user_id.isNullOrEmpty() || isShopActivityUpdating)
            return

        /* Get all the shop list that has been synched successfully*/
        val syncedShopList = AppDatabase.getDBInstance()!!.addShopEntryDao().getUnSyncedShops(true)
        if (syncedShopList.isEmpty())
            return

        isShopActivityUpdating = true

        val shopDataList: MutableList<ShopDurationRequestData> = ArrayList()
        val syncedShop = ArrayList<ShopActivityEntity>()

        val revisitStatusList : MutableList<ShopRevisitStatusRequestData> = ArrayList()


        doAsync {

            var counterShopList:Int = 0

            for (k in 0 until syncedShopList.size) {

                if (!Pref.isMultipleVisitEnable) {
                    /* Get shop activity that has completed time duration calculation*/

                    val shopActivity = AppDatabase.getDBInstance()!!.shopActivityDao().durationAvailableForShop(syncedShopList[k].shop_id, true, false)

                    if (shopActivity == null) {
                        val shop_activity = AppDatabase.getDBInstance()!!.shopActivityDao().durationAvailableForTodayShop(syncedShopList[k].shop_id,true, true,
                            AppUtils.getCurrentDateForShopActi())
                        if (shop_activity != null)
                            syncedShop.add(shop_activity)

                    }
                    else {
                        val shopDurationData = ShopDurationRequestData()
                        shopDurationData.shop_id = shopActivity.shopid
                        shopDurationData.spent_duration = shopActivity.duration_spent
                        shopDurationData.visited_date = shopActivity.visited_date
                        shopDurationData.visited_time = shopActivity.visited_date
                        if (AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(shopActivity.shopid) != null)
                            shopDurationData.total_visit_count = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(shopActivity.shopid).totalVisitCount
                        else
                            shopDurationData.total_visit_count = "1"

                        if (TextUtils.isEmpty(shopActivity.distance_travelled))
                            shopActivity.distance_travelled = "0.0"
                        shopDurationData.distance_travelled = shopActivity.distance_travelled

                        val currentShopVisitDateNumber = AppUtils.getTimeStampFromDateOnly(shopActivity.date!!)

                        if (shopId == shopActivity.shopid && previousShopVisitDateNumber == currentShopVisitDateNumber)
                            continue

                        shopId = shopActivity.shopid!!
                        shopVisitDate = shopActivity.date!!
                        previousShopVisitDateNumber = currentShopVisitDateNumber

                        if (!TextUtils.isEmpty(shopActivity.feedback))
                            shopDurationData.feedback = shopActivity.feedback
                        else
                            shopDurationData.feedback = ""

                        shopDurationData.isFirstShopVisited = shopActivity.isFirstShopVisited
                        shopDurationData.distanceFromHomeLoc = shopActivity.distance_from_home_loc

                        shopDurationData.next_visit_date = shopActivity.next_visit_date

                        if (!TextUtils.isEmpty(shopActivity.early_revisit_reason))
                            shopDurationData.early_revisit_reason = shopActivity.early_revisit_reason
                        else
                            shopDurationData.early_revisit_reason = ""

                        shopDurationData.device_model = shopActivity.device_model
                        shopDurationData.android_version = shopActivity.android_version
                        shopDurationData.battery = shopActivity.battery
                        shopDurationData.net_status = shopActivity.net_status
                        shopDurationData.net_type = shopActivity.net_type
                        shopDurationData.in_time = shopActivity.in_time
                        shopDurationData.out_time = shopActivity.out_time
                        shopDurationData.start_timestamp = shopActivity.startTimeStamp
                        shopDurationData.in_location = shopActivity.in_loc
                        shopDurationData.out_location = shopActivity.out_loc
                        try{
                            shopDurationData.shop_revisit_uniqKey = shopActivity.shop_revisit_uniqKey!!
                        }catch (ex:Exception){
                            ex.printStackTrace()
                            shopDurationData.shop_revisit_uniqKey = Pref.user_id + System.currentTimeMillis().toString()
                        }

                        //duration garbage fix
                        try{
                            if(shopDurationData.spent_duration!!.contains("-") || shopDurationData.spent_duration!!.length != 8)
                            {
                                shopDurationData.spent_duration="00:00:10"
                            }
                        }catch (ex:Exception){
                            shopDurationData.spent_duration="00:00:10"
                        }

                        //Begin Rev 1.0 Suman 10-07-2023 IsnewShop in api+room mantis id 26537
                        if(shopActivity.isNewShop){
                            shopDurationData.isNewShop = 1
                        }else{
                            shopDurationData.isNewShop = 0
                        }
                        //End Rev 1.0 Suman 10-07-2023 IsnewShop in api+room mantis id 26537

                        // Rev 1.0 Suman 06-05-2024 Suman LocationFuzedService mantis 27335 begin
                        try {
                            var shopOb = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(shopDurationData.shop_id)
                            shopDurationData.shop_lat=shopOb.shopLat.toString()
                            shopDurationData.shop_long=shopOb.shopLong.toString()
                            shopDurationData.shop_addr=shopOb.address.toString()
                        }catch (ex:Exception){
                            ex.printStackTrace()
                        }
                        // Rev 1.0 Suman 06-05-2024 Suman LocationFuzedService mantis 27335 end


                        shopDataList.add(shopDurationData)

                        //////////////////////////
                        var revisitStatusObj=ShopRevisitStatusRequestData()
                        var data=AppDatabase.getDBInstance()?.shopVisitOrderStatusRemarksDao()!!.getSingleItem(shopDurationData.shop_revisit_uniqKey.toString())
                        if(data != null ){
                            revisitStatusObj.shop_id=data.shop_id
                            revisitStatusObj.order_status=data.order_status
                            revisitStatusObj.order_remarks=data.order_remarks
                            revisitStatusObj.shop_revisit_uniqKey=data.shop_revisit_uniqKey
                            revisitStatusList.add(revisitStatusObj)
                        }

                        Timber.d("====SYNC VISITED SHOP DATA (LOCATION FUZED SERVICE)====")
                        Timber.d("SHOP ID======> " + shopDurationData.shop_id)
                        Timber.d("SPENT DURATION======> " + shopDurationData.spent_duration)
                        Timber.d("VISIT DATE=========> " + shopDurationData.visited_date)
                        Timber.d("VISIT DATE TIME==========> " + shopDurationData.visited_date)
                        Timber.d("TOTAL VISIT COUNT========> " + shopDurationData.total_visit_count)
                        Timber.d("DISTANCE TRAVELLED========> " + shopDurationData.distance_travelled)
                        Timber.d("FEEDBACK========> " + shopDurationData.feedback)
                        Timber.d("isFirstShopVisited========> " + shopDurationData.isFirstShopVisited)
                        Timber.d("distanceFromHomeLoc========> " + shopDurationData.distanceFromHomeLoc)
                        Timber.d("next_visit_date========> " + shopDurationData.next_visit_date)
                        Timber.d("device_model========> " + shopDurationData.device_model)
                        Timber.d("android_version========> " + shopDurationData.android_version)
                        Timber.d("battery========> " + shopDurationData.battery)
                        Timber.d("net_status========> " + shopDurationData.net_status)
                        Timber.d("net_type========> " + shopDurationData.net_type)
                        Timber.d("in_time========> " + shopDurationData.in_time)
                        Timber.d("out_time========> " + shopDurationData.out_time)
                        Timber.d("start_timestamp========> " + shopDurationData.start_timestamp)
                        Timber.d("in_location========> " + shopDurationData.in_location)
                        Timber.d("out_location========> " + shopDurationData.out_location)
                        Timber.d("========================================================")


                        counterShopList++
                        if(counterShopList > 300){
                            //break
                        }

                    }
                }
                else {
                    val shopActivity = AppDatabase.getDBInstance()!!.shopActivityDao().durationAvailableForShopList(syncedShopList[k].shop_id, true,
                        false)

                    shopActivity?.forEach {
                        val shopDurationData = ShopDurationRequestData()
                        shopDurationData.shop_id = it.shopid
                        shopDurationData.spent_duration = it.duration_spent
                        shopDurationData.visited_date = it.visited_date
                        shopDurationData.visited_time = it.visited_date
                        if (AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(it.shopid) != null)
                            shopDurationData.total_visit_count = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(it.shopid).totalVisitCount
                        else
                            shopDurationData.total_visit_count = "1"

                        if (TextUtils.isEmpty(it.distance_travelled))
                            it.distance_travelled = "0.0"
                        shopDurationData.distance_travelled = it.distance_travelled

                        if (!TextUtils.isEmpty(it.feedback))
                            shopDurationData.feedback = it.feedback
                        else
                            shopDurationData.feedback = ""

                        shopDurationData.isFirstShopVisited = it.isFirstShopVisited
                        shopDurationData.distanceFromHomeLoc = it.distance_from_home_loc

                        shopDurationData.next_visit_date = it.next_visit_date

                        if (!TextUtils.isEmpty(it.early_revisit_reason))
                            shopDurationData.early_revisit_reason = it.early_revisit_reason
                        else
                            shopDurationData.early_revisit_reason = ""

                        shopDurationData.device_model = it.device_model
                        shopDurationData.android_version = it.android_version
                        shopDurationData.battery = it.battery
                        shopDurationData.net_status = it.net_status
                        shopDurationData.net_type = it.net_type
                        shopDurationData.in_time = it.in_time
                        shopDurationData.out_time = it.out_time
                        shopDurationData.start_timestamp = it.startTimeStamp
                        shopDurationData.in_location = it.in_loc
                        shopDurationData.out_location = it.out_loc
                        shopDurationData.shop_revisit_uniqKey=it.shop_revisit_uniqKey!!

                        //duration garbage fix
                        try{
                            if(shopDurationData.spent_duration!!.contains("-") || shopDurationData.spent_duration!!.length != 8)
                            {
                                shopDurationData.spent_duration="00:00:10"
                            }
                        }catch (ex:Exception){
                            shopDurationData.spent_duration="00:00:10"
                        }

                        //Begin Rev 1.0 Suman 10-07-2023 IsnewShop in api+room mantis id 26537
                        if(it.isNewShop){
                            shopDurationData.isNewShop = 1
                        }else{
                            shopDurationData.isNewShop = 0
                        }
                        //End Rev 1.0 Suman 10-07-2023 IsnewShop in api+room mantis id 26537

                        // Rev 1.0 Suman 06-05-2024 Suman LocationFuzedService mantis 27335 begin
                        try {
                            var shopOb = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(shopDurationData.shop_id)
                            shopDurationData.shop_lat=shopOb.shopLat.toString()
                            shopDurationData.shop_long=shopOb.shopLong.toString()
                            shopDurationData.shop_addr=shopOb.address.toString()
                        }catch (ex:Exception){
                            ex.printStackTrace()
                        }
                        // Rev 1.0 Suman 06-05-2024 Suman LocationFuzedService mantis 27335 end

                        shopDataList.add(shopDurationData)

                        //////////////////////////
                        var revisitStatusObj=ShopRevisitStatusRequestData()
                        var data=AppDatabase.getDBInstance()?.shopVisitOrderStatusRemarksDao()!!.getSingleItem(shopDurationData.shop_revisit_uniqKey.toString())
                        if(data != null ){
                            revisitStatusObj.shop_id=data.shop_id
                            revisitStatusObj.order_status=data.order_status
                            revisitStatusObj.order_remarks=data.order_remarks
                            revisitStatusObj.shop_revisit_uniqKey=data.shop_revisit_uniqKey
                            revisitStatusList.add(revisitStatusObj)
                        }



                        Timber.d("====SYNC VISITED SHOP DATA (LOCATION FUZED SERVICE)====")
                        Timber.d("SHOP ID======> " + shopDurationData.shop_id)
                        Timber.d("SPENT DURATION======> " + shopDurationData.spent_duration)
                        Timber.d("VISIT DATE=========> " + shopDurationData.visited_date)
                        Timber.d("VISIT DATE TIME==========> " + shopDurationData.visited_date)
                        Timber.d("TOTAL VISIT COUNT========> " + shopDurationData.total_visit_count)
                        Timber.d("DISTANCE TRAVELLED========> " + shopDurationData.distance_travelled)
                        Timber.d("FEEDBACK========> " + shopDurationData.feedback)
                        Timber.d("isFirstShopVisited========> " + shopDurationData.isFirstShopVisited)
                        Timber.d("distanceFromHomeLoc========> " + shopDurationData.distanceFromHomeLoc)
                        Timber.d("next_visit_date========> " + shopDurationData.next_visit_date)
                        Timber.d("device_model========> " + shopDurationData.device_model)
                        Timber.d("android_version========> " + shopDurationData.android_version)
                        Timber.d("battery========> " + shopDurationData.battery)
                        Timber.d("net_status========> " + shopDurationData.net_status)
                        Timber.d("net_type========> " + shopDurationData.net_type)
                        Timber.d("in_time========> " + shopDurationData.in_time)
                        Timber.d("out_time========> " + shopDurationData.out_time)
                        Timber.d("start_timestamp========> " + shopDurationData.start_timestamp)
                        Timber.d("in_location========> " + shopDurationData.in_location)
                        Timber.d("out_location========> " + shopDurationData.out_location)
                        Timber.d("========================================================")
                    }
                }
            }

            uiThread {

                if (shopDataList.isEmpty()) {
                    //isShopActivityUpdating = false

                    val unSyncedList = ArrayList<ShopVisitImageModelEntity>()
                    if (syncedShop != null && syncedShop.isNotEmpty()) {
                        for (j in syncedShop.indices) {
                            val unSyncImage = AppDatabase.getDBInstance()!!.shopVisitImageDao().getUnSyncedData(false, syncedShop[j].shopid!!)
                            if (unSyncImage != null)
                                unSyncedList.add(unSyncImage)
                        }
                        if (unSyncedList != null && unSyncedList.isNotEmpty()) {
                            i = 0
                            callShopVisitImageUploadApi(unSyncedList)
                        } else {

                            val unSyncedAudioList = ArrayList<ShopVisitAudioEntity>()
                            syncedShop.forEach {
                                val unSyncAudio = AppDatabase.getDBInstance()!!.shopVisitAudioDao().getUnSyncedData(false, it.shopid!!)
                                if (unSyncAudio != null)
                                    unSyncedAudioList.add(unSyncAudio)
                            }

                            if (unSyncedAudioList.isNotEmpty()) {
                                i = 0
                                callShopVisitAudioUploadApi(unSyncedAudioList)
                            } else
                                isShopActivityUpdating = false
                        }
                    } else
                        isShopActivityUpdating = false
                }
                else {
                    Timber.e("====SYNC VISITED SHOP (LOCATION FUZED SERVICE)====")
                    Timber.e("ShopData List size===> " + shopDataList.size)
                    //val newShopList = FTStorageUtils.removeDuplicateData(shopDataList)
                    val hashSet = HashSet<ShopDurationRequestData>()
                    var newShopList = ArrayList<ShopDurationRequestData>()

                    if (!Pref.isMultipleVisitEnable) {
                        for (i in shopDataList.indices) {
                            if (hashSet.add(shopDataList[i]))
                                newShopList.add(shopDataList[i])
                        }
                    }

                    val shopDurationApiReq = ShopDurationRequest()
                    shopDurationApiReq.user_id = Pref.user_id
                    shopDurationApiReq.session_token = Pref.session_token
                    if (newShopList.size > 0) {
                        Timber.e("Unique ShopData List size===> " + newShopList.size)
                        shopDurationApiReq.shop_list = newShopList
                    } else
                        shopDurationApiReq.shop_list = shopDataList

                    val repository = ShopDurationRepositoryProvider.provideShopDurationRepository()

                    Timber.d("callShopDurationApi : REQUEST")

                    compositeDisposable.add(
                        repository.shopDuration(shopDurationApiReq)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
//                        .timeout(60 * 1, TimeUnit.SECONDS)
                            .subscribe({ result ->
                                Timber.d("callShopDurationApi : RESPONSE " + result.status)

                                if (result.status == NetworkConstant.SUCCESS) {

                                    var responseShopList = result.shop_list
                                    newShopList = ArrayList()
                                    for(l in 0..responseShopList!!.size-1){
                                        var ob = ShopDurationRequestData()
                                        ob.shop_id = responseShopList.get(l).shopid!!
                                        ob.visited_date = responseShopList.get(l).visited_date!!
                                        ob.visited_time = responseShopList.get(l).visited_time!!
                                        ob.IsShopUpdate = responseShopList.get(l).IsShopUpdate
                                        //ob.total_visit_count = responseShopList.get(l).total_visit_count!!.toString()
                                        newShopList.add(ob)
                                    }

                                    //newShopList = responseShopList as ArrayList<ShopDurationRequestData>


                                    //callCompetetorImgUploadApi()

                                    if(!revisitStatusList.isEmpty()){
                                        callRevisitStatusUploadApi(revisitStatusList!!)
                                    }

                                    if (newShopList.size > 0) {
                                        for (i in 0 until newShopList.size) {
                                            try{
                                                if(newShopList.get(i).IsShopUpdate!!) {
                                                    //callCompetetorImgUploadApi(newShopList[i].shop_id!!)
                                                    AppDatabase.getDBInstance()!!.shopActivityDao().updateisUploaded(true, newShopList[i].shop_id!!, AppUtils.changeAttendanceDateFormatToCurrent(newShopList[i].visited_date!!) /*AppUtils.getCurrentDateForShopActi()*/)
                                                }
                                            }catch (ex:Exception){
                                                ex.printStackTrace()
                                            }
                                        }
                                        syncShopVisitImage(newShopList)
                                    } else {
                                        if (!Pref.isMultipleVisitEnable) {
                                            for (i in 0 until shopDataList.size) {
                                                callCompetetorImgUploadApi(shopDataList[i].shop_id!!)
                                                AppDatabase.getDBInstance()!!.shopActivityDao().updateisUploaded(true, shopDataList[i].shop_id!!, AppUtils.changeAttendanceDateFormatToCurrent(shopDataList[i].visited_date!!) /*AppUtils.getCurrentDateForShopActi()*/)
                                            }

                                            syncShopVisitImage(shopDataList)
                                        }
                                        else {
                                            for (i in 0 until shopDataList.size) {
                                                callCompetetorImgUploadApi(shopDataList[i].shop_id!!)
                                                AppDatabase.getDBInstance()!!.shopActivityDao().updateisUploaded(true, shopDataList[i].shop_id!!, AppUtils.changeAttendanceDateFormatToCurrent(shopDataList[i].visited_date!!), shopDataList[i].start_timestamp!!)
                                            }
                                        }
                                    }
                                }
                                isShopActivityUpdating = false
                            }, { error ->
                                isShopActivityUpdating = false
                                if (error == null) {
                                    Timber.d("callShopDurationApi : ERROR " + "UNEXPECTED ERROR IN SHOP ACTIVITY API")
                                } else {
                                    Timber.d("callShopDurationApi : ERROR " + error.localizedMessage)
                                    error.printStackTrace()
                                }
                                //(mContext as DashboardActivity).showSnackMessage("ERROR")
                            })
                    )
                }

            }
        }
    }


    private fun callRevisitStatusUploadApi(revisitStatusList : MutableList<ShopRevisitStatusRequestData>){
        val revisitStatus = ShopRevisitStatusRequest()
        revisitStatus.user_id=Pref.user_id
        revisitStatus.session_token=Pref.session_token
        revisitStatus.ordernottaken_list=revisitStatusList

        val repository = ShopRevisitStatusRepositoryProvider.provideShopRevisitStatusRepository()
        compositeDisposable.add(
                repository.shopRevisitStatus(revisitStatus)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            Timber.d("callRevisitStatusUploadApi : RESPONSE " + result.status)
                            if (result.status == NetworkConstant.SUCCESS){
                                for(i in revisitStatusList.indices){
                                    AppDatabase.getDBInstance()?.shopVisitOrderStatusRemarksDao()!!.updateOrderStatus(revisitStatusList[i]!!.shop_revisit_uniqKey!!)
                                }
                            }
                        },{error ->
                            if (error == null) {
                                Timber.d("callRevisitStatusUploadApi : ERROR " + "UNEXPECTED ERROR IN SHOP ACTIVITY API")
                            } else {
                                Timber.d("callRevisitStatusUploadApi : ERROR " + error.localizedMessage)
                                error.printStackTrace()
                            }
                        })
        )
    }



    private fun callCompetetorImgUploadApi(shop_id:String){
        //val unsynList = AppDatabase.getDBInstance()!!.shopVisitCompetetorImageDao().getUnSyncedCopetetorImg(Pref.user_id!!)
        val unsynList = AppDatabase.getDBInstance()!!.shopVisitCompetetorImageDao().getUnSyncedCopetetorImgByShopID(shop_id)
        var objCompetetor : AddShopRequestCompetetorImg = AddShopRequestCompetetorImg()

        if(unsynList == null || unsynList.size==0)
            return

        var shop_id:String

        //for(i in unsynList.indices){
            objCompetetor.session_token=Pref.session_token
            objCompetetor.shop_id=unsynList.get(0).shop_id
            objCompetetor.user_id=Pref.user_id
            objCompetetor.visited_date=unsynList.get(0).visited_date!!
            shop_id= unsynList.get(0).shop_id.toString()
            val repository = AddShopRepositoryProvider.provideAddShopRepository()
            BaseActivity.compositeDisposable.add(
                    repository.addShopWithImageCompetetorImg(objCompetetor,unsynList.get(0).shop_image,this)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ result ->
                                val response = result as BaseResponse
                                if(response.status==NetworkConstant.SUCCESS){
                                    AppDatabase.getDBInstance()!!.shopVisitCompetetorImageDao().updateisUploaded(true,shop_id)
                                    Timber.d("FUSED LOCATION : CompetetorImg" + ", SHOP: " + shopId + ", Success: ")
                                }else{
                                    Timber.d("FUSED LOCATION : CompetetorImg" + ", SHOP: " + shopId + ", Failed: ")
                                }
                            },{
                                error ->
                                if (error != null) {
                                    Timber.d("FUSED LOCATION : CompetetorImg" + ", SHOP: " + shopId + ", ERROR: " + error.localizedMessage)
                                }
                            })
            )
        //}


    }


    private var mShopDataList: MutableList<ShopDurationRequestData>? = null
    private fun syncShopVisitImage(shopDataList: MutableList<ShopDurationRequestData>) {
        /*var unSyncedList: List<ShopVisitImageModelEntity>? = null
        for (i in shopDataList.indices) {
            unSyncedList = AppDatabase.getDBInstance()!!.shopVisitImageDao().getUnSyncedListAccordingToShopId(false, shopDataList[i].shop_id!!)
        }*/
        mShopDataList = shopDataList
        val unSyncedList = ArrayList<ShopVisitImageModelEntity>()
        for (i in shopDataList.indices) {
            val unSyncedData = AppDatabase.getDBInstance()!!.shopVisitImageDao().getTodaysUnSyncedListAccordingToShopId(false, shopDataList[i].shop_id!!, shopDataList[i].visited_date!!)

            if (unSyncedData != null && unSyncedData.isNotEmpty()) {
                unSyncedList.add(unSyncedData[0])
            }
        }

        if (unSyncedList.size > 0) {
            i = 0
            callShopVisitImageUploadApi(unSyncedList)
        } else {
            val unSyncedAudioList = ArrayList<ShopVisitAudioEntity>()
            for (i in shopDataList.indices) {
                val unSyncedData = AppDatabase.getDBInstance()!!.shopVisitAudioDao().getTodaysUnSyncedListAccordingToShopId(false, shopDataList[i].shop_id!!, shopDataList[i].visited_date!!)

                if (unSyncedData != null && unSyncedData.isNotEmpty()) {
                    unSyncedAudioList.add(unSyncedData[0])
                }
            }

            if (unSyncedAudioList.size > 0) {
                i = 0
                callShopVisitAudioUploadApi(unSyncedAudioList)
            }
        }
    }

    private fun callShopVisitImageUploadApi(unSyncedList: List<ShopVisitImageModelEntity>) {

        try {
            val visitImageShop = ShopVisitImageUploadInputModel()
            visitImageShop.session_token = Pref.session_token
            visitImageShop.user_id = Pref.user_id
            visitImageShop.shop_id = unSyncedList[i].shop_id
            visitImageShop.visit_datetime = unSyncedList[i].visit_datetime


            Timber.d("========UPLOAD REVISIT ALL IMAGE INPUT PARAMS (LOCATION FUZED SERVICE)======")
            Timber.d("USER ID======> " + visitImageShop.user_id)
            Timber.d("SESSION ID======> " + visitImageShop.session_token)
            Timber.d("SHOP ID=========> " + visitImageShop.shop_id)
            Timber.d("VISIT DATE TIME==========> " + visitImageShop.visit_datetime)
            Timber.d("IMAGE========> " + unSyncedList[i].shop_image)
            Timber.d("============================================================================")

            val repository = ShopVisitImageUploadRepoProvider.provideAddShopRepository()

            BaseActivity.compositeDisposable.add(
                    repository.visitShopWithImage(visitImageShop, unSyncedList[i].shop_image!!, this)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ result ->
                                val logoutResponse = result as BaseResponse
                                Timber.d("UPLOAD REVISIT ALL IMAGE : " + "RESPONSE : " + logoutResponse.status + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + logoutResponse.message)
                                if (logoutResponse.status == NetworkConstant.SUCCESS) {
                                    AppDatabase.getDBInstance()!!.shopVisitImageDao().updateisUploaded(true, unSyncedList.get(i).shop_id!!)

                                    i++
                                    if (i < unSyncedList.size)
                                        callShopVisitImageUploadApi(unSyncedList)
                                    else {
                                        i = 0
                                        checkToCallAudioApi()
                                    }
                                } else
                                    isShopActivityUpdating = false

                            }, { error ->
                                Timber.d("UPLOAD REVISIT ALL IMAGE : " + "ERROR : " + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + error.localizedMessage)
                                error.printStackTrace()
                                isShopActivityUpdating = false
                            })
            )
        } catch (e: Exception) {
            e.printStackTrace()
            isShopActivityUpdating = false
        }
    }

    private fun checkToCallAudioApi() {
        val unSyncAudioList = ArrayList<ShopVisitAudioEntity>()
        mShopDataList?.forEach {
            val unSyncedData = AppDatabase.getDBInstance()!!.shopVisitAudioDao().getTodaysUnSyncedListAccordingToShopId(false, it.shop_id!!, it.visited_date!!)

            if (unSyncedData != null && unSyncedData.isNotEmpty()) {
                unSyncAudioList.add(unSyncedData[0])
            }
        }

        if (unSyncAudioList.isNotEmpty()) {
            i = 0
            callShopVisitAudioUploadApi(unSyncAudioList)
        } else
            isShopActivityUpdating = false
    }


    private fun callShopVisitAudioUploadApi(unSyncedList: List<ShopVisitAudioEntity>) {

        try {
            val visitImageShop = ShopVisitImageUploadInputModel()
            visitImageShop.session_token = Pref.session_token
            visitImageShop.user_id = Pref.user_id
            visitImageShop.shop_id = unSyncedList[i].shop_id
            visitImageShop.visit_datetime = unSyncedList[i].visit_datetime

            Timber.d("====UPLOAD REVISIT ALL AUDIO INPUT PARAMS (LOCATION FUZED SERVICE)======")
            Timber.d("USER ID====> " + visitImageShop.user_id)
            Timber.d("SESSION ID====> " + visitImageShop.session_token)
            Timber.d("SHOP ID====> " + visitImageShop.shop_id)
            Timber.d("VISIT DATE TIME=====> " + visitImageShop.visit_datetime)
            Timber.d("AUDIO=====> " + unSyncedList[i].audio)
            Timber.d("===============================================================")

            val repository = ShopVisitImageUploadRepoProvider.provideAddShopRepository()

            BaseActivity.compositeDisposable.add(
                    repository.visitShopWithAudio(visitImageShop, unSyncedList[i].audio!!, this)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ result ->
                                val logoutResponse = result as BaseResponse
                                Timber.d("UPLOAD REVISIT ALL AUDIO : " + "RESPONSE : " + logoutResponse.status + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + logoutResponse.message)
                                if (logoutResponse.status == NetworkConstant.SUCCESS) {
                                    AppDatabase.getDBInstance()!!.shopVisitAudioDao().updateisUploaded(true, unSyncedList.get(i).shop_id!!)
                                    i++
                                    if (i < unSyncedList.size)
                                        callShopVisitAudioUploadApi(unSyncedList)
                                    else {
                                        i = 0
                                        isShopActivityUpdating = false
                                    }
                                } else
                                    isShopActivityUpdating = false

                            }, { error ->
                                Timber.d("UPLOAD REVISIT ALL AUDIO : " + "ERROR : " + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + error.localizedMessage)
                                error.printStackTrace()
                                isShopActivityUpdating = false

                            })
            )
        } catch (e: Exception) {
            e.printStackTrace()
            isShopActivityUpdating = false
        }
    }


    private fun updateShopVisitDuration() {
        var list = AppDatabase.getDBInstance()!!.shopActivityDao().durationAvailable(false, false)
        if (list.isEmpty())
            return
        isLocalShopActivityUpdating = true

        for (i in 0 until list.size) {

            if (list[i].startTimeStamp != "0") {
                AppUtils.changeLanguage(this,"en")
                var totalMinute = AppUtils.getMinuteFromTimeStamp(list[i].startTimeStamp, System.currentTimeMillis().toString())
                changeLocale()
                //If duration is greater than 20 hour then stop incrementing
                if (totalMinute.toInt() > 20 * 60) {
                    AppDatabase.getDBInstance()!!.shopActivityDao().updateDurationAvailable(true, list[i].shopid!!, AppUtils.getCurrentDateForShopActi())
                    isLocalShopActivityUpdating = false
                    return
                }

                AppDatabase.getDBInstance()!!.shopActivityDao().updateTotalMinuteForDayOfShop(list[i].shopid!!, totalMinute, AppUtils.getCurrentDateForShopActi())
                AppUtils.changeLanguage(this,"en")
                var duration = AppUtils.getTimeFromTimeSpan(list[i].startTimeStamp, System.currentTimeMillis().toString())
                changeLocale()
                AppDatabase.getDBInstance()!!.shopActivityDao().updateTimeDurationForDayOfShop(list[i].shopid!!, duration, AppUtils.getCurrentDateForShopActi())
//                Timber.d("ShopDurationIncrement : " + "ShopName : " + list[i].shop_name + "," + duration)
            }
        }

        isLocalShopActivityUpdating = false
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun updateLocation(mLastLocation: Location, location: Location) {
        if (location.latitude == 0.0 || location.longitude == 0.0)
            return

        val userlocation = UserLocationDataEntity()
        userlocation.latitude = location.latitude.toString()
        userlocation.longitude = location.longitude.toString()
        userlocation.time = LocationWizard.getFormattedTime24Hours(true)
        userlocation.meridiem = LocationWizard.getMeridiem()

        var maxDis = 0.0
        val accurateLocationList = AppDatabase.getDBInstance()!!.userLocationDataDao().getLocationUpdateForADay(AppUtils.getCurrentDateForShopActi())

        if (accurateLocationList != null && accurateLocationList.isNotEmpty()) {
            val currentTime = userlocation.time + " " + userlocation.meridiem

            Timber.e("LocationFuzedService: currentTime=====> $currentTime")
            val currentTimeStamp = AppUtils.getMilisFromMeredian(currentTime)

            val lastAccurateTime = accurateLocationList[accurateLocationList.size - 1].time + " " + accurateLocationList[accurateLocationList.size - 1].meridiem
            Timber.e("LocationFuzedService: lastAccurateTime=====> $lastAccurateTime")

            val lastAccurateTimeStamp = AppUtils.getMilisFromMeredian(lastAccurateTime)

            val timeStampDiff = currentTimeStamp - lastAccurateTimeStamp
            val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(timeStampDiff)

            Timber.e("LocationFuzedService: Difference=====> $diffInMinutes mins")

            maxDis = if (diffInMinutes > 0)
                AppUtils.maxDistance.toDouble() * diffInMinutes
            else
                AppUtils.maxDistance.toDouble()
        } else
            maxDis = AppUtils.maxDistance.toDouble()

        Timber.e("LocationFuzedService: Max Distance=====> $maxDis meter")

        //userlocation.accuracy = location.accuracy.toString()
//        userlocation.prev_latitude=mLastLocation.latitude.toString()
//        userlocation.prev_longitude=mLastLocation.longitude.toString()


        if (!TextUtils.isEmpty(Pref.home_latitude) && !TextUtils.isEmpty(Pref.home_longitude)) {
            val distance = LocationWizard.getDistance(Pref.home_latitude.toDouble(), Pref.home_longitude.toDouble(), location.latitude, location.longitude)

            userlocation.home_distance = (distance * 1000).toString()
            Timber.e("LocationFuzedService: home_distance=====> ${userlocation.home_distance} Meter")

            if (distance * 1000 > Pref.homeLocDistance.toDouble())
                calculateAccurateDistance(userlocation, maxDis, location)
            else {
                Timber.e("==========User is at home location (Location Fuzed Service)==========")
                userlocation.distance = "0.0"

                if (Pref.isAddAttendence) {
                    calculateIdleTime(location, "accurate")

                    val currentTimeInLong = AppUtils.convertTimeWithMeredianToLong(AppUtils.getCurrentTimeWithMeredian())
                    val inTimeInLong = AppUtils.convertTimeWithMeredianToLong("10:30 am")
                    val outTimeInLong = AppUtils.convertTimeWithMeredianToLong("7:30 pm")

                    if (currentTimeInLong in inTimeInLong..outTimeInLong) {
                        if (Pref.willShowHomeLocReason && Pref.isOnLeave.equals("false", ignoreCase = true)) {
                            if (shouldCheckHomeLocationReason()) {
                                Timber.e("==========Should Check Home Location Reason (Location Fuzed Service)==========")
                                /*if (!TextUtils.isEmpty(Pref.approvedOutTime) && !TextUtils.isEmpty(Pref.approvedInTime)) {

                                    val currentTimeInLong = AppUtils.convertTimeWithMeredianToLong(AppUtils.getCurrentTimeWithMeredian())
                                    val approvedOutTimeInLong = AppUtils.convertTimeWithMeredianToLong(Pref.approvedOutTime)
                                    val approvedInTimeInLong = AppUtils.convertTimeWithMeredianToLong(Pref.approvedInTime)

                                    if (currentTimeInLong in approvedInTimeInLong until approvedOutTimeInLong) {*/
                                        Pref.isShowHomeLocReason = true
                                        AppUtils.changeLanguage(this, "en")
                                        Pref.homeLocStartTimeStamp = System.currentTimeMillis().toString()
                                        changeLocale()

                                        val intent = Intent()
                                        intent.action = "HOME_LOC_ACTION_RECEIVER"
                                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                                    /*}
                                }*/
                            } else
                                Timber.e("==========Should Not Check Home Location Reason (Location Fuzed Service)==========")
                        }
                    }
                }
                else
                    Timber.e("=====Attendance is not added for today (Accurate idle time)======")
            }
        } else {
            calculateAccurateDistance(userlocation, maxDis, location)
            userlocation.home_distance = "0.0"
        }


        userlocation.locationName = LocationWizard.getNewLocationName(this, userlocation.latitude.toDouble(), userlocation.longitude.toDouble())
        userlocation.timestamp = LocationWizard.getTimeStamp()
        /*userlocation.time = LocationWizard.getFormattedTime24Hours(true)
        userlocation.meridiem = LocationWizard.getMeridiem()*/
        userlocation.hour = LocationWizard.getHour()
        userlocation.minutes = LocationWizard.getMinute()
        userlocation.isUploaded = false
        userlocation.shops = AppDatabase.getDBInstance()!!.shopActivityDao().getTotalShopVisitedForADay(AppUtils.getCurrentDateForShopActi()).size.toString()
        userlocation.updateDate = AppUtils.getCurrentDateForShopActi()
        userlocation.updateDateTime = AppUtils.getCurrentDateTime()
        userlocation.meeting = AppDatabase.getDBInstance()!!.addMeetingDao().getMeetingDateWise(AppUtils.getCurrentDateForShopActi()).size.toString()
        userlocation.network_status = if (AppUtils.isOnline(this)) "Online" else "Offline"
        userlocation.battery_percentage = AppUtils.getBatteryPercentage(this).toString()

        if (!Pref.isShowHomeLocReason) {
            if (!Pref.homeLocStartTimeStamp.isEmpty() && !Pref.homeLocEndTimeStamp.isEmpty()) {
                userlocation.home_duration = AppUtils.getTimeFromTimeSpan(Pref.homeLocStartTimeStamp, Pref.homeLocEndTimeStamp)
                Pref.homeLocStartTimeStamp = ""
                Pref.homeLocEndTimeStamp = ""
            }
            else
                userlocation.home_duration = ""
        }
        else
            userlocation.home_duration = ""

        addLocationData(userlocation)

        Observable.create<AddShopDBModelEntity> { observable ->
            val list = AppDatabase.getDBInstance()!!.addShopEntryDao().all
            list.forEach {
                try {
                    val results = FloatArray(3)
                    Location.distanceBetween(
                            it.shopLat,
                            it.shopLong,
                            location.latitude,
                            location.longitude,
                            results
                    )

                    if (results.getOrNull(0) ?: -1 in 0..200) {
                        observable.onNext(it)
                    } /*else {
                        val shopActivityList = AppDatabase.getDBInstance()!!.shopActivityDao().getShopForDay(it.shop_id, AppUtils.getCurrentDateForShopActi())
                        for (i in 0 until shopActivityList.size) {
                            if (!shopActivityList[i].isDurationCalculated && shopActivityList[i].startTimeStamp != "0") {
                                val endTimeStamp = System.currentTimeMillis().toString()
                                val totalMinute = AppUtils.getMinuteFromTimeStamp(shopActivityList[i].startTimeStamp, endTimeStamp)
                                val duration = AppUtils.getTimeFromTimeSpan(shopActivityList[i].startTimeStamp, endTimeStamp)
                                AppDatabase.getDBInstance()!!.shopActivityDao().updateTotalMinuteForDayOfShop(shopActivityList[i].shopid!!, totalMinute, AppUtils.getCurrentDateForShopActi())
                                AppDatabase.getDBInstance()!!.shopActivityDao().updateEndTimeOfShop(endTimeStamp, shopActivityList[i].shopid!!, AppUtils.getCurrentDateForShopActi())
                                AppDatabase.getDBInstance()!!.shopActivityDao().updateTimeDurationForDayOfShop(shopActivityList[i].shopid!!, duration, AppUtils.getCurrentDateForShopActi())
                                AppDatabase.getDBInstance()!!.shopActivityDao().updateDurationAvailable(true, shopActivityList[i].shopid!!, AppUtils.getCurrentDateForShopActi())
                                AppDatabase.getDBInstance()!!.shopActivityDao().updateIsUploaded(false, shopActivityList[i].shopid!!, AppUtils.getCurrentDateForShopActi())

                                Timber.d("=====================Geofence Exit=======================")
                                Timber.d("Distance------> " + results[0])
                                Timber.d("Shop " + it.shopName + " exited from geofence")
                                Timber.d("==========================================================")

                            }
                        }
                    }*/
                } catch (e: Exception) {
                    e.printStackTrace()
                    Timber.e("LocationFuzedService", e)
                }
            }
        }.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    //sendNotification(it.shop_id)
                }
        //}
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateAccurateDistance(userlocation: UserLocationDataEntity, maxDis: Double, location: Location) {
        Timber.e("==========User is not at home location (Location Fuzed Service)==========")

        val distance = LocationWizard.getDistance(mLastLocation?.latitude!!, mLastLocation?.longitude!!, userlocation.latitude.toDouble(), userlocation.longitude.toDouble())

        //distance = 1.21

        Timber.e("Original Distance (LocationFuzedService)===> $distance")

        if (distance * 1000 >= AppUtils.minDistance.toDouble() && distance * 1000 <= maxDis /*AppUtils.maxDistance.toDouble()*/) {
            /*resetData()
            val finalDistance = (tempDistance.toDouble() + distance).toString()

            Timber.e("===Distance (LocationFuzedService)===")
            Timber.e("Temp Distance====> $tempDistance")
            Timber.e("Normal Distance====> $distance")
            Timber.e("Total Distance====> $finalDistance")
            Timber.e("=====================================")

            userlocation.distance = finalDistance  //LocationWizard.getDistance(mLastLocation.latitude, mLastLocation.longitude, userlocation.latitude.toDouble(), userlocation.longitude.toDouble()).toString()
            tempDistance = "0.0"*/

            saveData(userlocation, distance)

        } else if (distance * 1000 > maxDis /*AppUtils.maxDistance.toDouble()*/) {
            saveData(userlocation, maxDis /*AppUtils.maxDistance.toDouble()*/ / 1000)
        } else {
            /*if (!AppUtils.isShopVisited) {
                val currentTimeStamp = System.currentTimeMillis()
                if (previousIdleTimeStamp > 0L) {
                    val interval = currentTimeStamp - previousIdleTimeStamp

                    if (TextUtils.isEmpty(startIdleDateTime))
                        startIdleDateTime = AppUtils.getCurrentISODateTime()

                    Timber.e("Fuzed Location: accurate currentIdleTimestamp=====> $currentTimeStamp")
                    Timber.e("Fuzed Location: accurate previousIdleTimestamp====> $previousIdleTimeStamp")
                    Timber.e("Fuzed Location: accurate idle interval====> $interval")

                    val intervalInMins = (interval / 1000) / 60
                    Timber.e("Fuzed Location: accurate idle interval=====> $intervalInMins min(s)")

                    if (startIdleLat == 0.0 && startIdleLong == 0.0) {
                        startIdleLat = location.latitude
                        startIdleLong = location.longitude
                    }

                    if (intervalInMins >= AppUtils.idle_time.toInt()) {
                        endIdleLat = location.latitude
                        endIdleLong = location.longitude
                        endIdleDateTime = AppUtils.getCurrentISODateTime()

                        Timber.e("======Idle Location accurate========")
                        Timber.e("start lat====> $startIdleLat")
                        Timber.e("start long====> $startIdleLong")
                        Timber.e("end lat====> $endIdleLat")
                        Timber.e("end lat====> $endIdleLong")
                        Timber.e("start date time====> $startIdleDateTime")
                        Timber.e("end date time====> $endIdleDateTime")

                        saveIdleData()
                        resetData()
                    }
                } else
                    previousIdleTimeStamp = currentTimeStamp
            } else {
                Timber.e("======Reset idle data accurate========")
                resetData()
            }*/
            userlocation.distance = "0.0"

            if (Pref.isAddAttendence)
                calculateIdleTime(location, "accurate")
            else
                Timber.e("=====Attendance is not added for today (Accurate idle time)======")
        }
    }

    private fun saveData(userlocation: UserLocationDataEntity, distance: Double) {
        resetData()
        val finalDistance = (tempDistance.toDouble() + distance).toString()

        Timber.e("===Distance (LocationFuzedService)===")
        Timber.e("Temp Distance====> $tempDistance")
        Timber.e("Normal Distance====> $distance")
        Timber.e("Total Distance====> $finalDistance")
        Timber.e("=====================================")

        userlocation.distance = finalDistance  //LocationWizard.getDistance(mLastLocation.latitude, mLastLocation.longitude, userlocation.latitude.toDouble(), userlocation.longitude.toDouble()).toString()
        tempDistance = "0.0"
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(shopId: String) {

        try {
            val list = AppDatabase.getDBInstance()!!.shopActivityDao().getShopForDay(shopId, AppUtils.getCurrentDateForShopActi())
            var isDurationCalculated = false
            var isVisited = false
            var shopName = ""
            if (list.isEmpty()) {
                if (AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(shopId) == null)
                    return
                shopName = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(shopId).shopName
            } else {
                isDurationCalculated = list[0].isDurationCalculated
                isVisited = list[0].isVisited
                shopName = list[0].shop_name!!
            }
            Timber.d("Geofence: ENTER : ShopName : $shopName,IS_DURATION_CALCULATED : $isDurationCalculated")
            if (isDurationCalculated || isVisited)
                return

            Timber.d("Geofence: NearToShop : " + "ShopName : " + shopName)
            // Get an instance of the Notification manager
            val notification = NotificationUtils(getString(R.string.app_name), shopName, shopId, "")
            notification.CreateNotification(this, shopId)
//        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Timber.e("LocationFuzedService: " + "=======================TASK REMOVED====================")

        val intent = Intent(this, RestartBroadcast::class.java)
        sendBroadcast(intent)

        /*val restartServiceIntent = Intent(applicationContext, LocationFuzedService::class.java)
        restartServiceIntent.setPackage(packageName);

        val service = PendingIntent.getService(applicationContext, 10001, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 500, service)*/

        super.onTaskRemoved(rootIntent)
    }


    override fun onDestroy() {
        //if (gpsReceiver != null)
            //unregisterReceiver(gpsReceiver)

        try{
            Timber.d("service_tag onDestroy")
            if (gpsReceiver != null)
                unregisterReceiver(gpsReceiver)
            println("loc_ex  gpsReceiver success" );
            Timber.d("loc_ex  gpsReceiver success" )
        }catch (ex:Exception){
            ex.printStackTrace()
            println("loc_ex  gpsReceiver ${ex.printStackTrace()}" );
            Timber.d("loc_ex  gpsReceiver ${ex.printStackTrace()}" )
        }

        //unregisterReceiver(eventReceiver)

        try{
            unregisterReceiver(eventReceiver)
        }catch (ex:Exception){
            ex.printStackTrace()
            println("loc_ex  eventReceiver ${ex.printStackTrace()}" );
            Timber.d("loc_ex  eventReceiver ${ex.printStackTrace()}" );
        }

        Timber.e("onDestroy : " + "LocationFuzedService")
//        removeGeofence()

        try {

            if (TextUtils.isEmpty(Pref.user_id)) {
                resetData()

                if (mGoogleAPIClient != null) {
                    mGoogleAPIClient?.disconnect()
                    mGoogleAPIClient = null
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        try{
            compositeDisposable.clear()
            mWakeLock.release()
        }catch (ex:Exception){
            ex.printStackTrace()
        }

        super.onDestroy()
    }


    private fun removeGeofence() {
        Timber.d("removeGeofence : ")
        mGeofencingClient.removeGeofences(getGeofencePendingIntent())
    }

    private fun trackDeviceMemory() {
        val runtime = Runtime.getRuntime()
        val usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
        Timber.d("\n USED MEMORY (MB): " + usedMemInMB)
        val maxHeapSizeInMB = runtime.maxMemory() / 1048576L
        Timber.d("MAX HEAP MEMORY (MB): " + maxHeapSizeInMB)
        val availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB
        Timber.d("AVAILABLE HEAP SIZE (MB): " + availHeapSizeInMB + "\n")
    }

    public fun getShopDummyImageFile(): File {
        var bm: Bitmap? = null
        if (bm == null) {
            val bitmap = (applicationContext.resources.getDrawable(R.drawable.ic_image_upload_icon) as BitmapDrawable).bitmap
            bm = bitmap
        }
        val bytes = ByteArrayOutputStream()
        bm!!.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        AppUtils.changeLanguage(this,"en")
        var destination = File(Environment.getExternalStorageDirectory(), System.currentTimeMillis().toString() + ".jpg")
        changeLocale()
        val camera_image_path = destination?.absolutePath
        val fo: FileOutputStream
        try {
            destination?.createNewFile()
            fo = FileOutputStream(destination)
            fo.write(bytes.toByteArray())
            fo.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return destination
    }


    fun getPointByDistanceAndBearing(lat: Double, lon: Double, bearing: Double, distanceKm: Double): Pair<Double, Double> {
        val earthRadius = 6378.1

        val bearingR = Math.toRadians(bearing)

        val latR = Math.toRadians(lat)
        val lonR = Math.toRadians(lon)

        val distanceToRadius = distanceKm / earthRadius

        val newLatR = Math.asin(Math.sin(latR) * Math.cos(distanceToRadius) +
                Math.cos(latR) * Math.sin(distanceToRadius) * Math.cos(bearingR))
        val newLonR = lonR + Math.atan2(Math.sin(bearingR) * Math.sin(distanceToRadius) * Math.cos(latR),
                Math.cos(distanceToRadius) - Math.sin(latR) * Math.sin(newLatR))

        val latNew = Math.toDegrees(newLatR)
        val lonNew = Math.toDegrees(newLonR)

        return Pair(latNew, lonNew)
    }

    lateinit var mGnssStatusCallback: GnssStatus.Callback

    @SuppressLint("MissingPermission")
    private fun registerGpsStatusListener() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            mGnssStatusCallback = object : GnssStatus.Callback() {
                override fun onSatelliteStatusChanged(status: GnssStatus) {
                    super.onSatelliteStatusChanged(status)
                }
            }
            Timber.d("LocationFuzedService registerGnssStatusCallback1 : Time :" + AppUtils.getCurrentDateTime())
            locationManager.registerGnssStatusCallback(mGnssStatusCallback!!)
        } else {
            Timber.d("LocationFuzedService registerGnssStatusCallback2 : Time :" + AppUtils.getCurrentDateTime())
            locationManager.addGpsStatusListener(this)
        }

    }

    override fun onGpsStatusChanged(p0: Int) {
        when (p0) {
            GpsStatus.GPS_EVENT_STARTED -> {
                Timber.e("======Check GPS status (Location Fuzed Service)==========")
                Timber.e("GPS_EVENT_STARTED : " + "Time : " + AppUtils.getCurrentDateTime())
            }
            GpsStatus.GPS_EVENT_STOPPED -> {
                Timber.e("======Check GPS status (Location Fuzed Service)==========")
                Timber.e("GPS_EVENT_STOPPED: " + "Time : " + AppUtils.getCurrentDateTime())


                sendGPSOffBroadcast()
                if (!FTStorageUtils.isMyServiceRunning(LocationFuzedService::class.java, this)) {
                    serviceStatusActionable()
                }

            }
            GpsStatus.GPS_EVENT_FIRST_FIX -> {
                Timber.e("======Check GPS status (Location Fuzed Service)==========")
                Timber.e("GPS_EVENT_FIRST_FIX : " + "Time : " + AppUtils.getCurrentDateTime())
            }
            GpsStatus.GPS_EVENT_SATELLITE_STATUS -> {
            }

        }
    }



    fun sendGPSOffBroadcast(){
        if(Pref.GPSAlertGlobal){
            if(Pref.GPSAlert){
                if(!gpsStatus) {
                    if (Pref.user_id.toString().length > 0) {
                        //var notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        //notificationManager.cancel(monitorNotiID)
                        Pref.isLocFuzedBroadPlaying=true
                        MonitorBroadcast.isSound=Pref.GPSAlertwithSound
                        val intent: Intent = Intent(this, MonitorBroadcast::class.java)
                        intent.putExtra("notiId", monitorNotiID)
                        intent.putExtra("fuzedLoc", "Fuzed Stop")
                        sendBroadcast(intent)
                    }
                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun serviceStatusActionable() {
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
                        Timber.d("===============================LocationFuzedService   Job scheduled (Base Activity) " + AppUtils.getCurrentDateTime() + "============================")
                    } else {
                        Timber.d("=====================LocationFuzedService Job not scheduled (Base Activity) " + AppUtils.getCurrentDateTime() + "====================================")
                    }
                } else
                    startService(serviceLauncher)
            } else {
                stopService(serviceLauncher)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                    jobScheduler.cancelAll()
                    Timber.d("===============================LocationFuzedService Job scheduler cancel (Base Activity)" + AppUtils.getCurrentDateTime() + "============================")

                    /*if (AppUtils.mGoogleAPIClient != null) {
                        AppUtils.mGoogleAPIClient?.disconnect()
                        AppUtils.mGoogleAPIClient = null
                    }*/
                }

                /*val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()*/

                AlarmReceiver.stopServiceAlarm(this, 123)
                Timber.d("===========LocationFuzedService Service alarm is stopped (Base Activity)================")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun callShopActivityApiForActivityCheck() {
        if(!shouldUpdateRevisitGarbage()){
            return
        }
        var shopActivity = ShopActivityRequest()
        shopActivity.user_id = Pref.user_id
        shopActivity.session_token = Pref.session_token
        shopActivity.date_span = "30"
        shopActivity.from_date = ""
        shopActivity.to_date = ""
        val repository = ShopActivityRepositoryProvider.provideShopActivityRepository()

        BaseActivity.compositeDisposable.add(
            repository.fetchShopActivitynew(Pref.session_token!!, Pref.user_id!!, "30", "", "")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    var shopActityResponse = result as ShopActivityResponse
                    if (shopActityResponse.status == "200") {
                        if(shopActityResponse.date_list!!.size>0){
                            var actiList = shopActityResponse.date_list as ArrayList<ShopActivityResponseDataList>
                            if(actiList!!.size>1){
                                //actiList.removeAt(actiList!!.size-1)
                                Handler().postDelayed(Runnable {
                                    var mListSynced : ArrayList<ShopActivityResponseDataList> = ArrayList()

                                    var isCurrdatefound  = false
                                    for(i in 0..actiList.size-1){
                                        if(actiList.get(i).date.equals(AppUtils.getCurrentDateForShopActi())){
                                            mListSynced.add(actiList.get(i))
                                            isCurrdatefound = true
                                        }
                                    }
                                    if(isCurrdatefound){
                                        updateActivityGarbage(mListSynced)
                                    }else{
                                        mListSynced.add(actiList.get(actiList.size-1))
                                        updateActivityGarbage(mListSynced)
                                    }
                                }, 150)

                            }
                        }
                    }
                }, { error ->
                    error.printStackTrace()
                })
        )
    }

    fun updateActivityGarbage(listUnsync:ArrayList<ShopActivityResponseDataList>){
        doAsync {
            try{
                Timber.d("updateActivityGarbage FuzedService started " + AppUtils.getCurrentDateTime())
                var  lastobj = listUnsync.get(listUnsync.size-1)
                if(!lastobj.date.equals(AppUtils.getCurrentDateForShopActi())){
                    AppDatabase.getDBInstance()!!.shopActivityDao().updateShopForIsuploadZeroByDate(false,AppUtils.getCurrentDateForShopActi())
                }
            }catch (ex:Exception){
                ex.printStackTrace()
            }


            for(i in 0..listUnsync.size-1){
                var shopListRoom = AppDatabase.getDBInstance()!!.shopActivityDao().getAllShopActivityByDate(listUnsync.get(i)!!.date!!.toString()) as ArrayList<String>
                var objList = listUnsync.get(i)?.shop_list!!
                var shopListApi : ArrayList<String> = ArrayList()
                for(m in 0..objList.size-1){
                    shopListApi.add(objList.get(m).shopid!!)
                }
                //var shopListApi : ArrayList<String> = objList.map { it.shopid } as ArrayList<String>
                if(shopListRoom.size > shopListApi.size){
                    var unsyncedList: List<String> = shopListRoom - shopListApi
                    for(j in 0..unsyncedList.size-1){
                        try{
                            Timber.d("updateActivityGarbage FuzedService marked unsync for  ${unsyncedList.get(j)}" + AppUtils.getCurrentDateTime())
                        }catch (ex:Exception){
                            ex.printStackTrace()
                        }
                        AppDatabase.getDBInstance()!!.shopActivityDao().updateShopForIsuploadZero(false,unsyncedList.get(j),listUnsync.get(i)!!.date!!.toString())
                    }
                }
            }
            uiThread {

            }
        }
    }



    private fun syncShopListOnebyOne() {
        val shopList = AppDatabase.getDBInstance()!!.addShopEntryDao().getUnSyncedShops(false)
        if (shopList.isEmpty() || shopList.size==0){

        }
        else{
            val addShopData = AddShopRequestData()
            val mAddShopDBModelEntity = shopList[0]
            addShopData.session_token = Pref.session_token
            addShopData.address = mAddShopDBModelEntity.address
            addShopData.owner_contact_no = mAddShopDBModelEntity.ownerContactNumber
            addShopData.owner_email = mAddShopDBModelEntity.ownerEmailId
            addShopData.owner_name = mAddShopDBModelEntity.ownerName
            addShopData.pin_code = mAddShopDBModelEntity.pinCode
            addShopData.shop_lat = mAddShopDBModelEntity.shopLat.toString()
            addShopData.shop_long = mAddShopDBModelEntity.shopLong.toString()
            addShopData.shop_name = mAddShopDBModelEntity.shopName.toString()
            addShopData.type = mAddShopDBModelEntity.type.toString()
            addShopData.shop_id = mAddShopDBModelEntity.shop_id
            addShopData.user_id = Pref.user_id
            addShopData.assigned_to_dd_id = mAddShopDBModelEntity.assigned_to_dd_id
            addShopData.assigned_to_pp_id = mAddShopDBModelEntity.assigned_to_pp_id
            addShopData.added_date = mAddShopDBModelEntity.added_date
            addShopData.amount = mAddShopDBModelEntity.amount
            addShopData.area_id = mAddShopDBModelEntity.area_id
            addShopData.model_id = mAddShopDBModelEntity.model_id
            addShopData.primary_app_id = mAddShopDBModelEntity.primary_app_id
            addShopData.secondary_app_id = mAddShopDBModelEntity.secondary_app_id
            addShopData.lead_id = mAddShopDBModelEntity.lead_id
            addShopData.stage_id = mAddShopDBModelEntity.stage_id
            addShopData.funnel_stage_id = mAddShopDBModelEntity.funnel_stage_id
            addShopData.booking_amount = mAddShopDBModelEntity.booking_amount
            addShopData.type_id = mAddShopDBModelEntity.type_id

            addShopData.director_name = mAddShopDBModelEntity.director_name
            addShopData.key_person_name = mAddShopDBModelEntity.person_name
            addShopData.phone_no = mAddShopDBModelEntity.person_no

            if (!TextUtils.isEmpty(mAddShopDBModelEntity.family_member_dob))
                addShopData.family_member_dob = AppUtils.changeAttendanceDateFormatToCurrent(mAddShopDBModelEntity.family_member_dob)

            if (!TextUtils.isEmpty(mAddShopDBModelEntity.add_dob))
                addShopData.addtional_dob = AppUtils.changeAttendanceDateFormatToCurrent(mAddShopDBModelEntity.add_dob)

            if (!TextUtils.isEmpty(mAddShopDBModelEntity.add_doa))
                addShopData.addtional_doa = AppUtils.changeAttendanceDateFormatToCurrent(mAddShopDBModelEntity.add_doa)

            addShopData.specialization = mAddShopDBModelEntity.specialization
            addShopData.category = mAddShopDBModelEntity.category
            addShopData.doc_address = mAddShopDBModelEntity.doc_address
            addShopData.doc_pincode = mAddShopDBModelEntity.doc_pincode
            addShopData.is_chamber_same_headquarter = mAddShopDBModelEntity.chamber_status.toString()
            addShopData.is_chamber_same_headquarter_remarks = mAddShopDBModelEntity.remarks
            addShopData.chemist_name = mAddShopDBModelEntity.chemist_name
            addShopData.chemist_address = mAddShopDBModelEntity.chemist_address
            addShopData.chemist_pincode = mAddShopDBModelEntity.chemist_pincode
            addShopData.assistant_contact_no = mAddShopDBModelEntity.assistant_no
            addShopData.average_patient_per_day = mAddShopDBModelEntity.patient_count
            addShopData.assistant_name = mAddShopDBModelEntity.assistant_name

            if (!TextUtils.isEmpty(mAddShopDBModelEntity.doc_family_dob))
                addShopData.doc_family_member_dob = AppUtils.changeAttendanceDateFormatToCurrent(mAddShopDBModelEntity.doc_family_dob)

            if (!TextUtils.isEmpty(mAddShopDBModelEntity.assistant_dob))
                addShopData.assistant_dob = AppUtils.changeAttendanceDateFormatToCurrent(mAddShopDBModelEntity.assistant_dob)

            if (!TextUtils.isEmpty(mAddShopDBModelEntity.assistant_doa))
                addShopData.assistant_doa = AppUtils.changeAttendanceDateFormatToCurrent(mAddShopDBModelEntity.assistant_doa)

            if (!TextUtils.isEmpty(mAddShopDBModelEntity.assistant_family_dob))
                addShopData.assistant_family_dob = AppUtils.changeAttendanceDateFormatToCurrent(mAddShopDBModelEntity.assistant_family_dob)

            addShopData.entity_id = mAddShopDBModelEntity.entity_id
            addShopData.party_status_id = mAddShopDBModelEntity.party_status_id
            addShopData.retailer_id = mAddShopDBModelEntity.retailer_id
            addShopData.dealer_id = mAddShopDBModelEntity.dealer_id
            addShopData.beat_id = mAddShopDBModelEntity.beat_id
            addShopData.assigned_to_shop_id = mAddShopDBModelEntity.assigned_to_shop_id
            addShopData.actual_address = mAddShopDBModelEntity.actual_address

            var uniqKeyObj=AppDatabase.getDBInstance()!!.shopActivityDao().getNewShopActivityKey(mAddShopDBModelEntity.shop_id,false)
            addShopData.shop_revisit_uniqKey=uniqKeyObj?.shop_revisit_uniqKey!!

            // duplicate shop api call
            addShopData.isShopDuplicate=mAddShopDBModelEntity.isShopDuplicate


            Handler().postDelayed(Runnable {
                callAddShopApi(addShopData, mAddShopDBModelEntity.shopImageLocalPath, shopList, true,
                    mAddShopDBModelEntity.doc_degree)
            }, 100)


        }
    }

    fun callAddShopApi(addShop: AddShopRequestData, shop_imgPath: String?, shopList: MutableList<AddShopDBModelEntity>?,
                       isFromInitView: Boolean, degree_imgPath: String?) {
        val index = addShop.shop_id!!.indexOf("_")
        if (shop_imgPath != null)
            Timber.d("shop image path=======> $shop_imgPath")

        if (degree_imgPath != null)
            Timber.d("doctor degree image path=======> $degree_imgPath")

        if (TextUtils.isEmpty(shop_imgPath) && TextUtils.isEmpty(degree_imgPath)) {
            val repository = AddShopRepositoryProvider.provideAddShopWithoutImageRepository()
            BaseActivity.compositeDisposable.add(
                repository.addShop(addShop)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val addShopResult = result as AddShopResponse
                        Timber.d("syncShopFromShopList : BaseActivity " + ", SHOP: " + addShop.shop_name + ", RESPONSE:" + result.message)

                        when (addShopResult.status) {
                            NetworkConstant.SUCCESS -> {
                                AppDatabase.getDBInstance()!!.addShopEntryDao().updateIsUploaded(true, addShop.shop_id)

                                syncShopListOnebyOne()
                            }
                            NetworkConstant.DUPLICATE_SHOP_ID -> {
                                Timber.d("DuplicateShop : BaseActivity " + ", SHOP: " + addShop.shop_name)
                                AppDatabase.getDBInstance()!!.addShopEntryDao().updateIsUploaded(true, addShop.shop_id)


                                if (AppDatabase.getDBInstance()!!.addShopEntryDao().getDuplicateShopData(addShop.owner_contact_no).size > 0) {
                                    AppDatabase.getDBInstance()!!.addShopEntryDao().deleteShopById(addShop.shop_id)
                                    AppDatabase.getDBInstance()!!.shopActivityDao().deleteShopByIdAndDate(addShop.shop_id!!, AppUtils.getCurrentDateForShopActi())
                                }
                                doAsync {
                                    uiThread {
                                        syncShopListOnebyOne()
                                    }
                                }

                            }
                            else -> {

                            }
                        }
                    }, { error ->
                        error.printStackTrace()

                        syncShopListOnebyOne()
                        if (error != null)
                            Timber.d("syncShopFromShopList : BaseActivity " + ", SHOP: " + addShop.shop_name + error.localizedMessage)
                    })
            )
        }
        else {
            val repository = AddShopRepositoryProvider.provideAddShopRepository()
            BaseActivity.compositeDisposable.add(
                repository.addShopWithImage(addShop, shop_imgPath, degree_imgPath, this)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val addShopResult = result as AddShopResponse
                        Timber.d("syncShopFromShopList : " + ", SHOP: " + addShop.shop_name + ", RESPONSE:" + result.message)

                        when (addShopResult.status) {
                            NetworkConstant.SUCCESS -> {
                                AppDatabase.getDBInstance()!!.addShopEntryDao().updateIsUploaded(true, addShop.shop_id)
                                doAsync {
                                    uiThread {
                                        syncShopListOnebyOne()
                                    }
                                }
                            }
                            NetworkConstant.DUPLICATE_SHOP_ID -> {
                                Timber.d("DuplicateShop : " + ", SHOP: " + addShop.shop_name)
                                AppDatabase.getDBInstance()!!.addShopEntryDao().updateIsUploaded(true, addShop.shop_id)

                                if (AppDatabase.getDBInstance()!!.addShopEntryDao().getDuplicateShopData(addShop.owner_contact_no).size > 0) {
                                    AppDatabase.getDBInstance()!!.addShopEntryDao().deleteShopById(addShop.shop_id)
                                    AppDatabase.getDBInstance()!!.shopActivityDao().deleteShopByIdAndDate(addShop.shop_id!!, AppUtils.getCurrentDateForShopActi())
                                }
                                doAsync {
                                    uiThread {
                                        syncShopListOnebyOne()
                                    }
                                }
                            }
                            else -> {

                            }
                        }
                    }, { error ->
                        error.printStackTrace()

                        syncShopListOnebyOne()
                        if (error != null)
                            Timber.d("syncShopFromShopList : " + ", SHOP: " + addShop.shop_name + error.localizedMessage)
                    })
            )
        }
    }


}




