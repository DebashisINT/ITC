package com.fsmitc.features.stockCompetetorStock.api

import com.fsmitc.base.BaseResponse
import com.fsmitc.features.orderList.model.NewOrderListResponseModel
import com.fsmitc.features.stockCompetetorStock.ShopAddCompetetorStockRequest
import com.fsmitc.features.stockCompetetorStock.model.CompetetorStockGetData
import io.reactivex.Observable

class AddCompStockRepository(val apiService:AddCompStockApi){

    fun addCompStock(shopAddCompetetorStockRequest: ShopAddCompetetorStockRequest): Observable<BaseResponse> {
        return apiService.submShopCompStock(shopAddCompetetorStockRequest)
    }

    fun getCompStockList(sessiontoken: String, user_id: String, date: String): Observable<CompetetorStockGetData> {
        return apiService.getCompStockList(sessiontoken, user_id, date)
    }
}