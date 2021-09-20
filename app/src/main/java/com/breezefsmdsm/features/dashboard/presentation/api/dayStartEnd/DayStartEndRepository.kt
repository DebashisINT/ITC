package com.breezefsmdsm.features.dashboard.presentation.api.dayStartEnd

import com.breezefsmdsm.app.Pref
import com.breezefsmdsm.base.BaseResponse
import com.breezefsmdsm.features.dashboard.presentation.model.DaystartDayendRequest
import com.breezefsmdsm.features.dashboard.presentation.model.StatusDayStartEnd
import com.breezefsmdsm.features.stockCompetetorStock.ShopAddCompetetorStockRequest
import com.breezefsmdsm.features.stockCompetetorStock.api.AddCompStockApi
import io.reactivex.Observable

class DayStartEndRepository (val apiService: DayStartEndApi){
    fun dayStart(daystartDayendRequest: DaystartDayendRequest): Observable<BaseResponse> {
        return apiService.submitDayStartEnd(daystartDayendRequest)
    }

    fun dayStartEndStatus(date:String): Observable<StatusDayStartEnd> {
        return apiService.statusDayStartEnd(Pref.session_token!!, Pref.user_id!!,date)
    }


}