package com.progdsmfsm.features.login.api.productlistapi

import com.progdsmfsm.app.Pref
import com.progdsmfsm.app.domain.ProductListEntity
import com.progdsmfsm.base.BaseResponse
import com.progdsmfsm.features.createOrder.DeleteOrd
import com.progdsmfsm.features.createOrder.EditOrd
import com.progdsmfsm.features.createOrder.GetOrderHistory
import com.progdsmfsm.features.createOrder.GetProductRateReq
import com.progdsmfsm.features.createOrder.GetProductReq
import com.progdsmfsm.features.createOrder.SyncOrd
import com.progdsmfsm.features.login.model.productlistmodel.ProductListOfflineResponseModel
import com.progdsmfsm.features.login.model.productlistmodel.ProductListOfflineResponseModelNew
import com.progdsmfsm.features.login.model.productlistmodel.ProductListResponseModel
import com.progdsmfsm.features.login.model.productlistmodel.ProductRateListResponseModel
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

    fun editProductListITC(obj: EditOrd): Observable<BaseResponse> {
        return apiService.editProductListITC(obj)
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

    fun deleteOrderITC(obj: DeleteOrd): Observable<BaseResponse> {
        return apiService.deleteOrderITCApi(obj)
    }
}