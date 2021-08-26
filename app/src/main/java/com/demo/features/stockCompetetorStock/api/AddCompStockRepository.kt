package com.demo.features.stockCompetetorStock.api

import com.demo.base.BaseResponse
import com.demo.features.orderList.model.NewOrderListResponseModel
import com.demo.features.stockCompetetorStock.ShopAddCompetetorStockRequest
import com.demo.features.stockCompetetorStock.model.CompetetorStockGetData
import io.reactivex.Observable

class AddCompStockRepository(val apiService:AddCompStockApi){

    fun addCompStock(shopAddCompetetorStockRequest: ShopAddCompetetorStockRequest): Observable<BaseResponse> {
        return apiService.submShopCompStock(shopAddCompetetorStockRequest)
    }

    fun getCompStockList(sessiontoken: String, user_id: String, date: String): Observable<CompetetorStockGetData> {
        return apiService.getCompStockList(sessiontoken, user_id, date)
    }
}