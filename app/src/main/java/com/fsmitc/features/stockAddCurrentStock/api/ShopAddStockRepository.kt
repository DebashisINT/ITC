package com.fsmitc.features.stockAddCurrentStock.api

import com.fsmitc.base.BaseResponse
import com.fsmitc.features.location.model.ShopRevisitStatusRequest
import com.fsmitc.features.location.shopRevisitStatus.ShopRevisitStatusApi
import com.fsmitc.features.stockAddCurrentStock.ShopAddCurrentStockRequest
import com.fsmitc.features.stockAddCurrentStock.model.CurrentStockGetData
import com.fsmitc.features.stockCompetetorStock.model.CompetetorStockGetData
import io.reactivex.Observable

class ShopAddStockRepository (val apiService : ShopAddStockApi){
    fun shopAddStock(shopAddCurrentStockRequest: ShopAddCurrentStockRequest?): Observable<BaseResponse> {
        return apiService.submShopAddStock(shopAddCurrentStockRequest)
    }

    fun getCurrStockList(sessiontoken: String, user_id: String, date: String): Observable<CurrentStockGetData> {
        return apiService.getCurrStockListApi(sessiontoken, user_id, date)
    }

}