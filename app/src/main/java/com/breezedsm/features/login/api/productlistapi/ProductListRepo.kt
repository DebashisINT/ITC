package com.breezedsm.features.login.api.productlistapi

import com.breezedsm.app.Pref
import com.breezedsm.app.domain.ProductListEntity
import com.breezedsm.base.BaseResponse
import com.breezedsm.features.createOrder.GetOrderHistory
import com.breezedsm.features.createOrder.GetProductRateReq
import com.breezedsm.features.createOrder.GetProductReq
import com.breezedsm.features.createOrder.SyncOrd
import com.breezedsm.features.login.model.productlistmodel.ProductListOfflineResponseModel
import com.breezedsm.features.login.model.productlistmodel.ProductListOfflineResponseModelNew
import com.breezedsm.features.login.model.productlistmodel.ProductListResponseModel
import com.breezedsm.features.login.model.productlistmodel.ProductRateListResponseModel
import io.reactivex.Observable

/**
 * Created by Saikat on 20-11-2018.
 */
class ProductListRepo(val apiService: ProductListApi) {
    fun getProductList(session_token: String, user_id: String, last_update_date: String): Observable<ProductListResponseModel> {
        return apiService.getProductList(session_token, user_id, last_update_date)
    }

    fun getProductListITC(session_token: String, user_id: String): Observable<GetProductReq> {
        return apiService.getProductListITC(session_token, user_id)
    }

    fun syncProductListITC(obj: SyncOrd): Observable<BaseResponse> {
        return apiService.syncProductListITC(obj)
    }

    fun getOrderHistory(user_id:String): Observable<GetOrderHistory> {
        return apiService.getOrderHistoryApi(user_id)
    }

    fun getProductRateListITC(session_token: String, user_id: String): Observable<GetProductRateReq> {
        return apiService.getProductRateListITC(session_token, user_id)
    }


    fun getProductRateList(shop_id: String): Observable<ProductRateListResponseModel> {
        return apiService.getProductRateList(Pref.session_token!!, Pref.user_id!!, shop_id)
    }

    fun getProductRateOfflineList(): Observable<ProductListOfflineResponseModel> {
        return apiService.getOfflineProductRateList(Pref.session_token!!, Pref.user_id!!)
    }


    fun getProductRateOfflineListNew(): Observable<ProductListOfflineResponseModelNew> {
        return apiService.getOfflineProductRateListNew(Pref.session_token!!, Pref.user_id!!)
    }
}