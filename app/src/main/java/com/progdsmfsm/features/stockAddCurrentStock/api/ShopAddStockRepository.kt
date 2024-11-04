package com.progdsmfsm.features.stockAddCurrentStock.api

import com.progdsmfsm.base.BaseResponse
import com.progdsmfsm.features.location.model.ShopRevisitStatusRequest
import com.progdsmfsm.features.location.shopRevisitStatus.ShopRevisitStatusApi
import com.progdsmfsm.features.stockAddCurrentStock.ShopAddCurrentStockRequest
import com.progdsmfsm.features.stockAddCurrentStock.model.CurrentStockGetData
import com.progdsmfsm.features.stockCompetetorStock.model.CompetetorStockGetData
import io.reactivex.Observable

class ShopAddStockRepository (val apiService : ShopAddStockApi){
    fun shopAddStock(shopAddCurrentStockRequest: ShopAddCurrentStockRequest?): Observable<BaseResponse> {
        return apiService.submShopAddStock(shopAddCurrentStockRequest)
    }

    fun getCurrStockList(sessiontoken: String, user_id: String, date: String): Observable<CurrentStockGetData> {
        return apiService.getCurrStockListApi(sessiontoken, user_id, date)
    }

}