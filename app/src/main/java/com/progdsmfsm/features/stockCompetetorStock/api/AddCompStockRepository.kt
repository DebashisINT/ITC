package com.progdsmfsm.features.stockCompetetorStock.api

import com.progdsmfsm.base.BaseResponse
import com.progdsmfsm.features.orderList.model.NewOrderListResponseModel
import com.progdsmfsm.features.stockCompetetorStock.ShopAddCompetetorStockRequest
import com.progdsmfsm.features.stockCompetetorStock.model.CompetetorStockGetData
import io.reactivex.Observable

class AddCompStockRepository(val apiService:AddCompStockApi){

    fun addCompStock(shopAddCompetetorStockRequest: ShopAddCompetetorStockRequest): Observable<BaseResponse> {
        return apiService.submShopCompStock(shopAddCompetetorStockRequest)
    }

    fun getCompStockList(sessiontoken: String, user_id: String, date: String): Observable<CompetetorStockGetData> {
        return apiService.getCompStockList(sessiontoken, user_id, date)
    }
}