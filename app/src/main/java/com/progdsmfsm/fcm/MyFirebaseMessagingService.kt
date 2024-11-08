package com.progdsmfsm.fcm

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.text.TextUtils
import timber.log.Timber
import com.progdsmfsm.R
import com.progdsmfsm.app.Pref
import com.progdsmfsm.app.utils.AppUtils

import com.progdsmfsm.app.utils.NotificationUtils
import com.progdsmfsm.base.BaseResponse
import com.progdsmfsm.base.presentation.BaseActivity
import com.progdsmfsm.fcm.api.UpdateDeviceTokenRepoProvider
import com.progdsmfsm.features.chat.model.ChatListDataModel
import com.progdsmfsm.features.chat.model.ChatUserDataModel

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Created by Saikat on 20-09-2018.
 */

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private var messageDetails = ""

    override fun onNewToken(token: String) {
        Timber.e("Refreshed token: $token")


        doAsync {

            var refreshedToken = token

            while (refreshedToken == null) {
                refreshedToken = token
            }

            Timber.e("MyFirebaseInstanceIDService : \nDevice Token=====> $token")

            uiThread {

                if (!TextUtils.isEmpty(Pref.user_id)) {


                    doAsync {

                        callUpdateDeviceTokenApi(refreshedToken)

                        uiThread {

                        }
                    }
                }

                Pref.deviceToken = token
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)


        Timber.e("FirebaseMessageService : ============Push has come============")

        if (TextUtils.isEmpty(Pref.user_id)) {
            Timber.e("FirebaseMessageService : ============Logged out scenario============")

            if (!TextUtils.isEmpty(remoteMessage?.data?.get("type")) && remoteMessage?.data?.get("type") == "clearData") {
                val packageName = applicationContext.packageName
                val runtime = Runtime.getRuntime()
                runtime.exec("pm clear $packageName")
            }

            return
        }

        //getting the title and the body
        //val title = remoteMessage?.notification?.title
        val body = remoteMessage?.data?.get("body")
        val tag = remoteMessage?.data?.get("flag")

        val notification = NotificationUtils(getString(R.string.app_name), "", "", "")

        if (!TextUtils.isEmpty(body)) {
            Timber.e("FirebaseMessageService : \nNotification Message=====> $body")
            //Timber.e("FirebaseMessageService : \nNotification Title=====> $title")
            if (remoteMessage?.data?.get("type") == "clearData") {
                Pref.isClearData = true


                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancelAll()

                notification.sendClearDataNotification(applicationContext, body!!)


            }
            else if (remoteMessage?.data?.get("type") == "chat") {
                val intent = Intent()
                intent.action = "FCM_CHAT_ACTION_RECEIVER"
                intent.putExtra("body", body)
                val chatData = ChatListDataModel(remoteMessage.data?.get("msg_id")!!, remoteMessage.data?.get("msg")!!,
                        remoteMessage.data?.get("time")!!, remoteMessage.data?.get("from_id")!!, remoteMessage.data?.get("from_name")!!)
                intent.putExtra("chatData", chatData)

                val chatUser = ChatUserDataModel(remoteMessage.data?.get("from_user_id")!!, remoteMessage.data?.get("from_user_name")!!,
                        remoteMessage.data?.get("isGroup")?.toBoolean()!!, "", "", "", "", "")
                intent.putExtra("chatUser", chatUser)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

                Handler(Looper.getMainLooper()).postDelayed({
                    if (!AppUtils.isBroadCastRecv)
                        notification.msgNotification(applicationContext, body!!, chatData, chatUser)
                    else
                        AppUtils.isBroadCastRecv = false
                }, 1000)

            }
            else if (remoteMessage?.data?.get("type") == "update_status") {
                val intent = Intent()
                intent.action = "FCM_STATUS_ACTION_RECEIVER"
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            } else if(tag.equals("shop_status_update")){
                notification.sendFCMNotificaitonShopStatusUpdate(applicationContext, remoteMessage)

                val intent = Intent()
                intent.action = "FCM_ACTION_RECEIVER_SHOP_STATUS"
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
            else {
                notification.sendFCMNotificaiton(applicationContext, remoteMessage)

                val intent = Intent()
                intent.action = "FCM_ACTION_RECEIVER"
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
        }

        ringtone()
    }

    private fun callUpdateDeviceTokenApi(refreshedToken: String?) {

        if (!AppUtils.isOnline(applicationContext))
            return

        val repository = UpdateDeviceTokenRepoProvider.updateDeviceTokenRepoProvider()

        BaseActivity.compositeDisposable.add(
                repository.updateDeviceToken(refreshedToken!!)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as BaseResponse
                            Timber.d("UpdateDeviceTokenResponse : " + "\n" + "Status====> " + response.status + ", Message===> " + response.message)

                        }, { error ->
                            error.printStackTrace()
                            Timber.d("UpdateDeviceTokenResponse ERROR: " + error.localizedMessage + "\n" + "Username :" + Pref.user_name + ", Time :" + AppUtils.getCurrentDateTime())
                        })
        )
    }


    private fun ringtone() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            /*val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()*/

            val ringtone = RingtoneManager.getRingtone(applicationContext, notification)
            val audioManager = applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)
            ringtone.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}

