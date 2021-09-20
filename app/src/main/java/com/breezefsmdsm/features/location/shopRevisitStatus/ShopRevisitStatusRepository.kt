package com.breezefsmdsm.features.location.shopRevisitStatus

import com.breezefsmdsm.base.BaseResponse
import com.breezefsmdsm.features.location.model.ShopDurationRequest
import com.breezefsmdsm.features.location.model.ShopRevisitStatusRequest
import io.reactivex.Observable

class ShopRevisitStatusRepository(val apiService : ShopRevisitStatusApi) {
    fun shopRevisitStatus(shopRevisitStatus: ShopRevisitStatusRequest?): Observable<BaseResponse> {
        return apiService.submShopRevisitStatus(shopRevisitStatus)
    }
}