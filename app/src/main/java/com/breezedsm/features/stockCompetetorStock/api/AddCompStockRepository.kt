package com.breezedsm.features.stockCompetetorStock.api

import com.breezedsm.base.BaseResponse
import com.breezedsm.features.orderList.model.NewOrderListResponseModel
import com.breezedsm.features.stockCompetetorStock.ShopAddCompetetorStockRequest
import com.breezedsm.features.stockCompetetorStock.model.CompetetorStockGetData
import io.reactivex.Observable

class AddCompStockRepository(val apiService:AddCompStockApi){

    fun addCompStock(shopAddCompetetorStockRequest: ShopAddCompetetorStockRequest): Observable<BaseResponse> {
        return apiService.submShopCompStock(shopAddCompetetorStockRequest)
    }

    fun getCompStockList(sessiontoken: String, user_id: String, date: String): Observable<CompetetorStockGetData> {
        return apiService.getCompStockList(sessiontoken, user_id, date)
    }
}