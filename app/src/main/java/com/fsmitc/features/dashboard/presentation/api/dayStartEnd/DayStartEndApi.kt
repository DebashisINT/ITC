package com.fsmitc.features.dashboard.presentation.api.dayStartEnd

import com.fsmitc.app.NetworkConstant
import com.fsmitc.base.BaseResponse
import com.fsmitc.features.dashboard.presentation.model.DaystartDayendRequest
import com.fsmitc.features.dashboard.presentation.model.StatusDayStartEnd
import com.fsmitc.features.stockCompetetorStock.ShopAddCompetetorStockRequest
import com.fsmitc.features.stockCompetetorStock.api.AddCompStockApi
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface DayStartEndApi {

    @POST("UserWiseDayStartEnd/UserDayStartEnd")
    fun submitDayStartEnd(@Body daystartDayendRequest: DaystartDayendRequest?): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("UserWiseDayStartEnd/UserStatusDayStartEnd")
    fun statusDayStartEnd(@Field("session_token") session_token: String, @Field("user_id") user_id: String,@Field("date") date: String):
            Observable<StatusDayStartEnd>

    companion object Factory {
        fun create(): DayStartEndApi {
            val retrofit = Retrofit.Builder()
                    .client(NetworkConstant.setTimeOutNoRetry())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(NetworkConstant.BASE_URL)
                    .build()

            return retrofit.create(DayStartEndApi::class.java)
        }
    }

}