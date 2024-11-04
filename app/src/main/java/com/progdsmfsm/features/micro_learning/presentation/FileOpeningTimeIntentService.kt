package com.progdsmfsm.features.micro_learning.presentation

import android.app.IntentService
import android.content.Intent
import com.progdsmfsm.R
import com.progdsmfsm.app.NetworkConstant
import com.progdsmfsm.app.Pref
import com.progdsmfsm.app.utils.AppUtils
import com.progdsmfsm.app.utils.Toaster
import com.progdsmfsm.base.BaseResponse
import com.progdsmfsm.base.presentation.BaseActivity
import com.progdsmfsm.features.dashboard.presentation.DashboardActivity
import com.progdsmfsm.features.micro_learning.api.MicroLearningRepoProvider
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