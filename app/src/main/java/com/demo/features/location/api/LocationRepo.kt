package com.demo.features.location.api

import com.demo.app.Pref
import com.demo.base.BaseResponse
import com.demo.features.location.model.AppInfoInputModel
import com.demo.features.location.model.AppInfoResponseModel
import com.demo.features.location.model.ShopDurationRequest
import com.demo.features.location.shopdurationapi.ShopDurationApi
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