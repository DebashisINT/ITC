package com.fsmitc.features.location.shopRevisitStatus

import com.fsmitc.base.BaseResponse
import com.fsmitc.features.location.model.ShopDurationRequest
import com.fsmitc.features.location.model.ShopRevisitStatusRequest
import io.reactivex.Observable

class ShopRevisitStatusRepository(val apiService : ShopRevisitStatusApi) {
    fun shopRevisitStatus(shopRevisitStatus: ShopRevisitStatusRequest?): Observable<BaseResponse> {
        return apiService.submShopRevisitStatus(shopRevisitStatus)
    }
}