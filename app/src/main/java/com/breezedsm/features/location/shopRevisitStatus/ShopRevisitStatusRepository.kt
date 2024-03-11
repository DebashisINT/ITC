package com.breezedsm.features.location.shopRevisitStatus

import com.breezedsm.base.BaseResponse
import com.breezedsm.features.location.model.ShopDurationRequest
import com.breezedsm.features.location.model.ShopRevisitStatusRequest
import io.reactivex.Observable

class ShopRevisitStatusRepository(val apiService : ShopRevisitStatusApi) {
    fun shopRevisitStatus(shopRevisitStatus: ShopRevisitStatusRequest?): Observable<BaseResponse> {
        return apiService.submShopRevisitStatus(shopRevisitStatus)
    }
}