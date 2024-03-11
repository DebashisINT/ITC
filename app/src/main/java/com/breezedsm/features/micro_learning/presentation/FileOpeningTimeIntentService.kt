package com.breezedsm.features.micro_learning.presentation

import android.app.IntentService
import android.content.Intent
import com.breezedsm.R
import com.breezedsm.app.NetworkConstant
import com.breezedsm.app.Pref
import com.breezedsm.app.utils.AppUtils
import com.breezedsm.app.utils.Toaster
import com.breezedsm.base.BaseResponse
import com.breezedsm.base.presentation.BaseActivity
import com.breezedsm.features.dashboard.presentation.DashboardActivity
import com.breezedsm.features.micro_learning.api.MicroLearningRepoProvider
import timber.log.Timber
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class FileOpeningTimeIntentService : IntentService("") {

    override fun onHandleIntent(p0: Intent?) {

        val id = p0?.getStringExtra("id")
        val startTime = p0?.getStringExtra("start_time")

        val repository = MicroLearningRepoProvider.microLearningRepoProvider()
        BaseActivity.compositeDisposable.add(
                repository.updateFileOpeningTime(id!!, startTime!!)
                        .subscribe({ result ->
                            val response = result as BaseResponse
                            Timber.d("UPDATE FILE OPENING TIME: " + "RESPONSE : " + response.status + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + response.message)
                        }, { error ->
                            Timber.d("UPDATE FILE OPENING TIME: " + "ERROR : " + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + error.localizedMessage)
                            error.printStackTrace()
                        })
        )
    }
}