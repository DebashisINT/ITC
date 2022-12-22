package com.breezefsmdsm.features.splash.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.breezefsmdsm.BuildConfig
import com.breezefsmdsm.R
import com.breezefsmdsm.app.NetworkConstant
import com.breezefsmdsm.app.Pref
import com.breezefsmdsm.app.uiaction.DisplayAlert
import com.breezefsmdsm.app.utils.AppUtils
import com.breezefsmdsm.app.utils.PermissionUtils
import com.breezefsmdsm.app.utils.Toaster
import com.breezefsmdsm.base.presentation.BaseActivity
import com.breezefsmdsm.features.alarm.presetation.AlarmBootReceiver
import com.breezefsmdsm.features.commondialog.presentation.CommonDialog
import com.breezefsmdsm.features.commondialog.presentation.CommonDialogClickListener
import com.breezefsmdsm.features.commondialogsinglebtn.CommonDialogSingleBtn
import com.breezefsmdsm.features.commondialogsinglebtn.OnDialogClickListener
import com.breezefsmdsm.features.dashboard.presentation.DashboardActivity
import com.breezefsmdsm.features.login.presentation.LoginActivity
import com.breezefsmdsm.features.splash.presentation.api.VersionCheckingRepoProvider
import com.breezefsmdsm.features.splash.presentation.model.VersionCheckingReponseModel
import com.breezefsmdsm.widgets.AppCustomTextView
import com.elvishew.xlog.XLog
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.messaging.FirebaseMessaging
import com.themechangeapp.pickimage.PermissionHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_splash.*
import net.alexandroid.gps.GpsStatusDetector
import kotlin.system.exitProcess


/**
 * Created by Pratishruti on 26-10-2017.
 */

class SplashActivity : BaseActivity(), GpsStatusDetector.GpsStatusDetectorCallBack {
//asd
    private var isLoginLoaded: Boolean = false
    private var permissionUtils: PermissionUtils? = null
    private var mGpsStatusDetector: GpsStatusDetector? = null
    private lateinit var progress_wheel: com.pnikosis.materialishprogress.ProgressWheel

    var permList = mutableListOf<PermissionDetails>()
    var permListDenied = mutableListOf<PermissionDetails>()
    data class PermissionDetails(var permissionName: String, var permissionTag: Int)

//test
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_splash)

        setContentView(R.layout.activity_splash_new)
    AppUtils.changeLanguage(this, "en")
//        Handler().postDelayed({ goToNextScreen() }, 2000)

        //Code by wasim
       // this is for test purpose timing seeting
      // AlarmReceiver.setAlarm(this, 17, 45, 2017)


        /*FirebaseMessaging.getInstance().subscribeToTopic("newss").addOnSuccessListener(object : OnSuccessListener<Void?> {
            override fun onSuccess(aVoid: Void?) {
                //Toast.makeText(applicationContext, "Success", Toast.LENGTH_LONG).show()
            }
        })*/

        val receiver = ComponentName(this, AlarmBootReceiver::class.java)
        packageManager.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)

        progress_wheel = findViewById(R.id.progress_wheel)
        progress_wheel.stopSpinning()

    storageSpace()

      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (Pref.isLocationPermissionGranted){
                initPermissionCheck()
            }
            else {
                LocationPermissionDialog.newInstance(object : LocationPermissionDialog.OnItemSelectedListener {
                    override fun onOkClick() {
                        initPermissionCheck()
                    }

                    override fun onCrossClick() {
                        finish()
                    }
                }).show(supportFragmentManager, "")
            }
        else {
            checkGPSProvider()
        }
        permissionCheck()*/
    }

    fun test(){
        /*val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest("https://theultimate.io/api/SendMessage?token=907f0234-3ffa-11ed-a7c7-9606c7e32d76&phone=919830916971&tempid=optin_hsm", notification,
            object : Response.Listener<JSONObject?> {
                override fun onResponse(response: JSONObject?) {
                    var jObj:JSONObject= JSONObject()
                    jObj=response!!.getJSONObject("result")
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
                    progress_wheel.stopSpinning()
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["tempid"] = "optin_hsm"
                params["phone"] = "9830916971"
                params["Content-Type"] = "application/json"
                params["token"] = "907f0234-3ffa-11ed-a7c7-9606c7e32d76"
                return params
            }
        }*/


        val queue = Volley.newRequestQueue(this)
        val url = "https://theultimate.io/api/SendMessage?token=907f0234-3ffa-11ed-a7c7-9606c7e32d76&phone=919830916971&tempid=optin_hsm"

        val stringRequest = StringRequest(
            Request.Method.GET, url, Response.Listener<String> { response ->
                var tt= "Response is: ${response.substring(0, 500)}"
            },
            Response.ErrorListener {
                var ttt= "That didn't work!"
            })

        queue.add(stringRequest)

    }

    private fun storageSpace(){
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        val bytesAvailable: Long
        bytesAvailable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            stat.blockSizeLong * stat.availableBlocksLong
        } else {
            stat.blockSize.toLong() * stat.availableBlocks.toLong()
        }
        val megAvailable = bytesAvailable / (1024 * 1024)
        println("storage "+megAvailable.toString());
        XLog.d("phone storage : FREE SPACE AVAILABLE : " +megAvailable.toString()+ " Time :" + AppUtils.getCurrentDateTime())

        if(megAvailable<1000){
            val simpleDialog = Dialog(this)
            simpleDialog.setCancelable(false)
            simpleDialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            simpleDialog.setContentView(R.layout.dialog_message)
            val dialogHeader = simpleDialog.findViewById(R.id.dialog_message_header_TV) as AppCustomTextView
            val dialog_yes_no_headerTV = simpleDialog.findViewById(R.id.dialog_message_headerTV) as AppCustomTextView
            if(Pref.user_name!=null){
                dialog_yes_no_headerTV.text = "Hi "+Pref.user_name!!+"!"
            }else{
                dialog_yes_no_headerTV.text = "Hi User"+"!"
            }
            //dialogHeader.text = "Please note that memory available is less than 5 GB. App may not function properly. Please make available memory greater than 5 GB."
            //dialogHeader.text = "Please make sure that you have Min: 1GB. Upto 5GB(Best performance) memory available to get best login experience."
            dialogHeader.text = "Please note that memory available is less than 1 GB. App may not function properly. Please make available memory greater than 2 GB for better performance."

            val dialogYes = simpleDialog.findViewById(R.id.tv_message_ok) as AppCustomTextView
            dialogYes.setOnClickListener({ view ->
                simpleDialog.cancel()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    if (Pref.isLocationPermissionGranted){
                        initPermissionCheck()
                    }
                    else {
                        LocationPermissionDialog.newInstance(object : LocationPermissionDialog.OnItemSelectedListener {
                            override fun onOkClick() {
                                initPermissionCheck()
                            }

                            override fun onCrossClick() {
                                finish()
                            }
                        }).show(supportFragmentManager, "")
                    }
                else {
                    checkGPSProvider()
                }
                permissionCheck()

            })
            simpleDialog.show()
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                if (Pref.isLocationPermissionGranted){
                    /*if (hasLocPermission()) {
                        //locationProcess()
                        var ttt="asd"
                    } else {
                        requestLocPermission()
                    }*/

                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R && Pref.isLocationHintPermissionGranted == false){
                        locDesc()
                    }else{
                        initPermissionCheck()
                    }
                }
                else {
                    LocationPermissionDialog.newInstance(object : LocationPermissionDialog.OnItemSelectedListener {
                        override fun onOkClick() {
                            /*  if (hasLocPermission()) {
                                  //locationProcess()
                                  var ttt="asd"
                              } else {
                                  requestLocPermission()
                              }*/

                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R && Pref.isLocationHintPermissionGranted == false){
                                locDesc()
                            }else{
                                initPermissionCheck()
                            }
                        }

                        override fun onCrossClick() {
                            finish()
                        }
                    }).show(supportFragmentManager, "")
                }
            else {
                checkGPSProvider()
            }
            permissionCheck()
        }

    }

    private fun locDesc(){
        LocationHintDialog.newInstance(object : LocationHintDialog.OnItemSelectedListener {
            override fun onOkClick() {
                Pref.isLocationHintPermissionGranted = true
                initPermissionCheck()
            }
        }).show(supportFragmentManager, "")
    }

    @SuppressLint("NewApi")
    private fun permissionCheck() {
        var strSub:String=""
        permList.clear()
        var info: PackageInfo = this.packageManager.getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
        var list = info.requestedPermissionsFlags
        var list1 = info.requestedPermissions
        for (i in 0..list.size - 1) {
            if (list1.get(i) != "android.permission.ACCESS_GPS") {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && list1.get(i) == "android.permission.ACCESS_BACKGROUND_LOCATION"){
                    strSub=" (For Android 10 & Later)"
                }

                if ( list1.get(i) == "android.permission.USE_FULL_SCREEN_INTENT" || list1.get(i) == "android.permission.SYSTEM_ALERT_WINDOW"
                        || list1.get(i) == "android.permission.FOREGROUND_SERVICE"){
                    strSub=" (System Defined)"
                }

                var obj: PermissionDetails = PermissionDetails(list1.get(i).replace("android.permission.", "").replace("_", " ")
                        .replace("com.google.android.c2dm.permission.RECEIVE", "Receive Data from Internet").replace("com.rubyfood.permission.C2D", "") + strSub, list.get(i))

                strSub=""
                if (list.get(i) == 3) {
                    permList.add(obj)
                } else {
                    permListDenied.add(obj)
                }
            }
        }
        val notifi: Boolean = NotificationManagerCompat.from(this).areNotificationsEnabled()

        if (notifi) {
            permList.add(PermissionDetails("Notification", 3))
        } else {
            permListDenied.add(PermissionDetails("Notification", 1))
        }
        permList = (permList + permListDenied).toMutableList()

        for(i in 0..permList.size-1){
            XLog.d("Permission Name"+permList.get(i).permissionName + " Status : Granted")
        }
        for(i in 0..permListDenied.size-1){
            XLog.d("Permission Name"+permListDenied.get(i).permissionName + " Status : Denied")
        }
    }

    private fun initPermissionCheck() {

        var permissionLists : Array<String> ?= null

        permissionLists = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            //arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        else
            arrayOf<String>(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

        permissionUtils = PermissionUtils(this, object : PermissionUtils.OnPermissionListener {
           // @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPermissionGranted() {

               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                   accessBackLoc()
               }else{
                   Pref.isLocationPermissionGranted = true
                   checkGPSProvider()
               }

                //checkGPSProvider()
            }

            override fun onPermissionNotGranted() {
                //ActivityCompat.requestPermissions(this@SplashActivity,permissionLists, PermissionHelper.TAG_LOCATION_RESULTCODE)
                //AppUtils.showButtonSnackBar(this@SplashActivity, rl_splash_main, getString(R.string.error_loc_permission_request_msg))
                DisplayAlert.showSnackMessage(this@SplashActivity, alert_splash_snack_bar, getString(R.string.accept_permission))
                Handler().postDelayed(Runnable {
                    finish()
                    exitProcess(0)
                }, 3000)
            }

        }, permissionLists)
    }

    private fun accessBackLoc(){
        var permissionLists : Array<String> ?= null

        permissionLists = arrayOf<String>( Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        permissionUtils = PermissionUtils(this, object : PermissionUtils.OnPermissionListener {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPermissionGranted() {
                Pref.isLocationPermissionGranted = true
                checkGPSProvider()
            }

            override fun onPermissionNotGranted() {
                //AppUtils.showButtonSnackBar(this@SplashActivity, rl_splash_main, getString(R.string.error_loc_permission_request_msg))
                DisplayAlert.showSnackMessage(this@SplashActivity, alert_splash_snack_bar, getString(R.string.accept_permission))
                Handler().postDelayed(Runnable {
                    finish()
                    exitProcess(0)
                }, 3000)
            }

        }, permissionLists)
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun checkGPSProvider() {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) /*&& PermissionHelper.checkLocationPermission(this, 0)*/) {
            checkGPSAvailability()

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isIgnoringBatteryOptimizations())
                checkBatteryOptimization()
            else
                doAfterPermissionFunctionality()

        } else {
            showGPSDisabledAlertToUser()
        }
    }

    private fun doAfterPermissionFunctionality() {
        Handler().postDelayed(Runnable {
            //goToNextScreen()
            if (!Pref.isAutoLogout)
                callVersionCheckingApi()
            else
                goToNextScreen()

        }, 1000)
    }

    private fun checkBatteryOptimization() {
        val intent = Intent()
        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data = Uri.parse("package:$packageName")
        startActivityForResult(intent, 100)
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val pwrm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = applicationContext.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return pwrm.isIgnoringBatteryOptimizations(name)
        }
        return true
    }

    private fun callVersionCheckingApi() {

        if (!AppUtils.isOnline(this)) {
            goToNextScreen()
            return
        }

        progress_wheel.spin()
        val repository = VersionCheckingRepoProvider.versionCheckingRepository()
        BaseActivity.compositeDisposable.add(
                repository.versionChecking()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            progress_wheel.stopSpinning()
                            val response = result as VersionCheckingReponseModel

                            XLog.d("VERSION CHECKING RESPONSE: " + "STATUS: " + response.status + ", MESSAGE:" + result.message)

                            if (response.status == NetworkConstant.SUCCESS) {

                                XLog.d("===========VERSION CHECKING SUCCESS RESPONSE===========")
                                XLog.d("min version=====> " + response.min_req_version)
                                XLog.d("store version=====> " + response.play_store_version)
                                XLog.d("mandatory msg======> " + response.mandatory_msg)
                                XLog.d("optional msg=====> " + response.optional_msg)
                                XLog.d("apk url======> " + response.apk_url)
                                XLog.d("=======================================================")

                                versionChecking(response)
                                //goToNextScreen()
                            } else {
                                goToNextScreen()
                            }
                            isApiInitiated = false

                        }, { error ->
                            isApiInitiated = false
                            XLog.d("VERSION CHECKING ERROR: " + "MESSAGE:" + error.message)
                            error.printStackTrace()
                            progress_wheel.stopSpinning()
                            goToNextScreen()
                        })
        )
    }


    private fun showGPSDisabledAlertToUser() {
        mGpsStatusDetector = GpsStatusDetector(this)
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mGpsStatusDetector?.checkGpsStatus()
        }
    }

    private fun versionChecking(response: VersionCheckingReponseModel) {
        try {

            val minVersion = Integer.parseInt(response.min_req_version?.replace(".", "").toString())
            val storeVersion = Integer.parseInt(response.play_store_version?.replace(".", "").toString())
            val currentVersion = Integer.parseInt(BuildConfig.VERSION_NAME.replace(".", ""))

            when {
                currentVersion >= storeVersion -> goToNextScreen()
                currentVersion in minVersion until storeVersion -> {
                    CommonDialog.getInstance("New Update", response.optional_msg!!,
                            "Cancel", "Ok", false, object : CommonDialogClickListener {
                        override fun onLeftClick() {
                            goToNextScreen()
                        }

                        override fun onRightClick(editableData: String) {
                            if (!TextUtils.isEmpty(response.apk_url)) {
                                val webLaunch = Intent(Intent.ACTION_VIEW, Uri.parse(response.apk_url))
                                startActivity(webLaunch)
                                finish()
                                exitProcess(0)
                            }
                            else
                                goToNextScreen()
                        }

                    }).show(supportFragmentManager, "")
                }
                else -> {
                    CommonDialogSingleBtn.getInstance("New Update", response.mandatory_msg!!,
                            "OK", object : OnDialogClickListener {
                        override fun onOkClick() {

                            /*market://details?id=com.fieldtrackingsystem*/


                            if (!TextUtils.isEmpty(response.apk_url)) {
                                val webLaunch = Intent(Intent.ACTION_VIEW, Uri.parse(response.apk_url))
                                startActivity(webLaunch)
                                finish()
                                exitProcess(0)
                            }
                            else
                                goToNextScreen()
                        }
                    }).show(supportFragmentManager, "")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            goToNextScreen()
        }
    }

    /*private fun goToNextScreen() {
        var manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //if (/*manager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&*/ PermissionHelper.checkLocationPermission(this, 0)) {
        if (TextUtils.isEmpty(Pref.user_id) || Pref.user_id.isNullOrBlank()) {
            if (!isLoginLoaded) {
                isLoginLoaded = true
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }

        } else {
            startActivity(Intent(this@SplashActivity, DashboardActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }
        //}
        /*else if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            startActivity(Intent(this@SplashActivity, DashboardActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }*/
    }*/

    private fun goToNextScreen() {
        addAutoStartup()
    }


    private fun addAutoStartup() {
        try {
            val intent = Intent()
            val manufacturer = Build.MANUFACTURER
            if ("xiaomi".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            } else if ("oppo".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
            } else if ("vivo".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
            } else if ("Letv".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")
            } else if ("Honor".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")
            }
            val list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            if (list.size > 0 && Pref.AutostartPermissionStatus==false) {
                //startActivity(intent)
                Pref.AutostartPermissionStatus = true
                startActivityForResult(intent,401)
            }else{
                goTONextActi()
            }
        } catch (e: java.lang.Exception) {
            Log.e("exc", e.toString())
            goTONextActi()
        }
    }

    fun goTONextActi(){

        var dev1= AppUtils.getDeviceName()
        val dev2 = Build.MANUFACTURER
        var dev3 = dev1 + " " + dev2
        if(dev3.contains("OPPO",ignoreCase = true) || dev3.contains("Vivo",ignoreCase = true)){
            val intent = Intent()
            val packageName = packageName
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            var t=pm.isIgnoringBatteryOptimizations(packageName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !pm.isIgnoringBatteryOptimizations(packageName)) {
                Handler().postDelayed(Runnable {
                    println("battery hit 175")
                    startActivityForResult( Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),175) }, 1000)
                return
            }
        }

        if (TextUtils.isEmpty(Pref.user_id) || Pref.user_id.isNullOrBlank()) {
            if (!isLoginLoaded) {
                isLoginLoaded = true
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }

        } else {
            startActivity(Intent(this@SplashActivity, DashboardActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        /*Handler().postDelayed({
            goToNextScreen()
        }, 2000)*/
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        /*if (requestCode == PermissionHelper.TAG_LOCATION_RESULTCODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // location-related task you need to do.
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    goToNextScreen()
                }

            } else {

                PermissionHelper.checkLocationPermission(this, 0)
//                Toast.makeText(this, "Location permission has not been granted", Toast.LENGTH_LONG).show()
            }

        }*/

        permissionUtils?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 175){
            println("battery get 175")
            checkBatteryOptiSettings()
            return
        }

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 100) {
                if (!Pref.isAutoLogout)
                    callVersionCheckingApi()
                else
                    goToNextScreen()
            }
            else {
                mGpsStatusDetector?.checkOnActivityResult(requestCode, resultCode)
                checkGPSAvailability()
                if (!Pref.isAutoLogout)
                    callVersionCheckingApi()
                else
                    goToNextScreen()
            }
        } else {

            /*DisplayAlert.showSnackMessage(this@SplashActivity, alert_splash_snack_bar, getString(R.string.alert_nolocation))

            Handler().postDelayed(Runnable {
                finish()
                System.exit(0)
            },1000)*/

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isIgnoringBatteryOptimizations())
                Toaster.msgShort(this, "Please allow battery optimization")

            val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                mGpsStatusDetector?.checkGpsStatus()
            else {
                checkGPSAvailability()

                Handler().postDelayed(Runnable {
                    if (!Pref.isAutoLogout)
                        callVersionCheckingApi()
                    else
                        goToNextScreen()
                }, 300)
            }
        }
    }

    // GpsStatusDetectorCallBack
    override fun onGpsSettingStatus(enabled: Boolean) {

        if (enabled)
            Log.e("splash", "GPS enabled")
        else
            Log.e("splash", "GPS disabled")
    }

    override fun onGpsAlertCanceledByUser() {
    }

    fun checkBatteryOptiSettings(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = packageName
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {

                //Toaster.msgLong(this,"Please Don't optimize your battery for this app.")
                CommonDialog.getInstance(getString(R.string.app_name), "You must select the option 'Don't Optimise' to use this app. " ,
                    "Cancel", "Ok", false, object : CommonDialogClickListener {
                        override fun onLeftClick() {
                            finish()
                        }

                        override fun onRightClick(editableData: String) {
                            goTONextActi()
                        }

                    }).show(supportFragmentManager, "")
                println("battery dialog scr")
            } else{
                println("battery next scr")
                goTONextActi()
            }
        }
    }


///////////////
/*private fun hasLocPermission() = EasyPermissions.hasPermissions(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION
)
    private fun requestLocPermission() {
        EasyPermissions.requestPermissions(
                this, "Permission Needed", 154,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Toast.makeText(this, "Operation Need", Toast.LENGTH_SHORT).show()
    }
    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

    }*/
///////////


}
