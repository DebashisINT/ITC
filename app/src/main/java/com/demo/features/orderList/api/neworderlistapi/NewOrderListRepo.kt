package com.demo.features.orderList.api.neworderlistapi

import com.demo.app.Pref
import com.demo.base.BaseResponse
import com.demo.features.orderList.model.NewOrderListResponseModel
import io.reactivex.Observable

/**
 * Created by Saikat on 03-12-2018.
 */
class NewOrderListRepo(val apiService: NewOrderListApi) {
    fun getOrderList(sessiontoken: String, user_id: String, date: String): Observable<NewOrderListResponseModel> {
        return apiService.getOrderList(sessiontoken, user_id, date)
    }

    fun sendOrderEmail(shopId: String, orderId: String, type: String): Observable<BaseResponse> {
        return apiService.sendOrderEmail(Pref.session_token!!, Pref.user_id!!, orderId, shopId, type)
    }
}