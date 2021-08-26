package com.demo.features.stockAddCurrentStock.api

import com.demo.base.BaseResponse
import com.demo.features.location.model.ShopRevisitStatusRequest
import com.demo.features.location.shopRevisitStatus.ShopRevisitStatusApi
import com.demo.features.stockAddCurrentStock.ShopAddCurrentStockRequest
import com.demo.features.stockAddCurrentStock.model.CurrentStockGetData
import com.demo.features.stockCompetetorStock.model.CompetetorStockGetData
import io.reactivex.Observable

class ShopAddStockRepository (val apiService : ShopAddStockApi){
    fun shopAddStock(shopAddCurrentStockRequest: ShopAddCurrentStockRequest?): Observable<BaseResponse> {
        return apiService.submShopAddStock(shopAddCurrentStockRequest)
    }

    fun getCurrStockList(sessiontoken: String, user_id: String, date: String): Observable<CurrentStockGetData> {
        return apiService.getCurrStockListApi(sessiontoken, user_id, date)
    }

}