package com.demo.features.dashboard.presentation.api.dayStartEnd

import com.demo.app.Pref
import com.demo.base.BaseResponse
import com.demo.features.dashboard.presentation.model.DaystartDayendRequest
import com.demo.features.dashboard.presentation.model.StatusDayStartEnd
import com.demo.features.stockCompetetorStock.ShopAddCompetetorStockRequest
import com.demo.features.stockCompetetorStock.api.AddCompStockApi
import io.reactivex.Observable

class DayStartEndRepository (val apiService: DayStartEndApi){
    fun dayStart(daystartDayendRequest: DaystartDayendRequest): Observable<BaseResponse> {
        return apiService.submitDayStartEnd(daystartDayendRequest)
    }

    fun dayStartEndStatus(date:String): Observable<StatusDayStartEnd> {
        return apiService.statusDayStartEnd(Pref.session_token!!, Pref.user_id!!,date)
    }


}