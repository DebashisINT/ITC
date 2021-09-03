package com.fsmitc.features.location.api

import com.fsmitc.app.Pref
import com.fsmitc.base.BaseResponse
import com.fsmitc.features.location.model.AppInfoInputModel
import com.fsmitc.features.location.model.AppInfoResponseModel
import com.fsmitc.features.location.model.ShopDurationRequest
import com.fsmitc.features.location.shopdurationapi.ShopDurationApi
import io.reactivex.Observable

/**
 * Created by Saikat on 17-Aug-20.
 */
class LocationRepo(val apiService: LocationApi) {
    fun appInfo(appInfo: AppInfoInputModel?): Observable<BaseResponse> {
        return apiService.submitAppInfo(appInfo)
    }

    fun getAppInfo(): Observable<AppInfoResponseModel> {
        return apiService.getAppInfo(Pref.session_token!!, Pref.user_id!!)
    }
}