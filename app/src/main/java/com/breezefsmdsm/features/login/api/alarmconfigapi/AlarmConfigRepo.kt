package com.breezefsmdsm.features.login.api.alarmconfigapi

import com.breezefsmdsm.app.Pref
import com.breezefsmdsm.features.login.model.alarmconfigmodel.AlarmConfigResponseModel
import io.reactivex.Observable
import timber.log.Timber

/**
 * Created by Saikat on 19-02-2019.
 */
class AlarmConfigRepo(val apiService: AlarmConfigApi) {
    fun alarmConfig(): Observable<AlarmConfigResponseModel> {
        Timber.d("AlarmApi alarmConfigResponse")
        return apiService.alarmConfigResponse(Pref.session_token!!, Pref.user_id!!)
    }
}