package com.progdsmfsm.features.orderList.api.neworderlistapi

import com.progdsmfsm.app.Pref
import com.progdsmfsm.base.BaseResponse
import com.progdsmfsm.features.orderList.model.NewOrderListResponseModel
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