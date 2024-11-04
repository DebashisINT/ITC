package com.progdsmfsm.features.location.shopRevisitStatus

import com.progdsmfsm.base.BaseResponse
import com.progdsmfsm.features.location.model.ShopDurationRequest
import com.progdsmfsm.features.location.model.ShopRevisitStatusRequest
import io.reactivex.Observable

class ShopRevisitStatusRepository(val apiService : ShopRevisitStatusApi) {
    fun shopRevisitStatus(shopRevisitStatus: ShopRevisitStatusRequest?): Observable<BaseResponse> {
        return apiService.submShopRevisitStatus(shopRevisitStatus)
    }
}