package com.fsmitc.features.dashboard.presentation.api.dayStartEnd

import com.fsmitc.app.Pref
import com.fsmitc.base.BaseResponse
import com.fsmitc.features.dashboard.presentation.model.DaystartDayendRequest
import com.fsmitc.features.dashboard.presentation.model.StatusDayStartEnd
import com.fsmitc.features.stockCompetetorStock.ShopAddCompetetorStockRequest
import com.fsmitc.features.stockCompetetorStock.api.AddCompStockApi
import io.reactivex.Observable

class DayStartEndRepository (val apiService: DayStartEndApi){
    fun dayStart(daystartDayendRequest: DaystartDayendRequest): Observable<BaseResponse> {
        return apiService.submitDayStartEnd(daystartDayendRequest)
    }

    fun dayStartEndStatus(date:String): Observable<StatusDayStartEnd> {
        return apiService.statusDayStartEnd(Pref.session_token!!, Pref.user_id!!,date)
    }


}