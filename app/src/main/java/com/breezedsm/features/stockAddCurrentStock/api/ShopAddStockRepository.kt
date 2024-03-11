package com.breezedsm.features.stockAddCurrentStock.api

import com.breezedsm.base.BaseResponse
import com.breezedsm.features.location.model.ShopRevisitStatusRequest
import com.breezedsm.features.location.shopRevisitStatus.ShopRevisitStatusApi
import com.breezedsm.features.stockAddCurrentStock.ShopAddCurrentStockRequest
import com.breezedsm.features.stockAddCurrentStock.model.CurrentStockGetData
import com.breezedsm.features.stockCompetetorStock.model.CompetetorStockGetData
import io.reactivex.Observable

class ShopAddStockRepository (val apiService : ShopAddStockApi){
    fun shopAddStock(shopAddCurrentStockRequest: ShopAddCurrentStockRequest?): Observable<BaseResponse> {
        return apiService.submShopAddStock(shopAddCurrentStockRequest)
    }

    fun getCurrStockList(sessiontoken: String, user_id: String, date: String): Observable<CurrentStockGetData> {
        return apiService.getCurrStockListApi(sessiontoken, user_id, date)
    }

}