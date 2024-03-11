package com.breezedsm.features.stockAddCurrentStock.api

import com.breezedsm.app.NetworkConstant
import com.breezedsm.base.BaseResponse
import com.breezedsm.features.location.model.ShopRevisitStatusRequest
import com.breezedsm.features.stockAddCurrentStock.ShopAddCurrentStockRequest
import com.breezedsm.features.stockAddCurrentStock.model.CurrentStockGetData
import com.breezedsm.features.stockCompetetorStock.model.CompetetorStockGetData
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ShopAddStockApi {

    @POST("Stock/AddCurrentStock")
    fun submShopAddStock(@Body shopAddCurrentStockRequest: ShopAddCurrentStockRequest?): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("Stock/CurrentStockList")
    fun getCurrStockListApi(@Field("session_token") session_token: String, @Field("user_id") user_id: String, @Field("date") date: String):
            Observable<CurrentStockGetData>

    companion object Factory {
        fun create(): ShopAddStockApi {
            val retrofit = Retrofit.Builder()
                    .client(NetworkConstant.setTimeOutNoRetry())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(NetworkConstant.BASE_URL)
                    .build()

            return retrofit.create(ShopAddStockApi::class.java)
        }
    }

}