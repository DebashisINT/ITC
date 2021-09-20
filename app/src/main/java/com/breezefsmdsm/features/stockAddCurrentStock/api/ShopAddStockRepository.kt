package com.breezefsmdsm.features.stockAddCurrentStock.api

import com.breezefsmdsm.base.BaseResponse
import com.breezefsmdsm.features.location.model.ShopRevisitStatusRequest
import com.breezefsmdsm.features.location.shopRevisitStatus.ShopRevisitStatusApi
import com.breezefsmdsm.features.stockAddCurrentStock.ShopAddCurrentStockRequest
import com.breezefsmdsm.features.stockAddCurrentStock.model.CurrentStockGetData
import com.breezefsmdsm.features.stockCompetetorStock.model.CompetetorStockGetData
import io.reactivex.Observable

class ShopAddStockRepository (val apiService : ShopAddStockApi){
    fun shopAddStock(shopAddCurrentStockRequest: ShopAddCurrentStockRequest?): Observable<BaseResponse> {
        return apiService.submShopAddStock(shopAddCurrentStockRequest)
    }

    fun getCurrStockList(sessiontoken: String, user_id: String, date: String): Observable<CurrentStockGetData> {
        return apiService.getCurrStockListApi(sessiontoken, user_id, date)
    }

}