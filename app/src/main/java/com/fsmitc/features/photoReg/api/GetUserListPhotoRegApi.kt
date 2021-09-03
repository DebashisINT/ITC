package com.fsmitc.features.photoReg.api

import com.fsmitc.app.NetworkConstant
import com.fsmitc.base.BaseResponse
import com.fsmitc.features.addshop.api.AddShopApi
import com.fsmitc.features.location.model.AppInfoResponseModel
import com.fsmitc.features.photoReg.model.*
import com.fsmitc.features.stockAddCurrentStock.api.ShopAddStockApi
import com.fsmitc.features.stockAddCurrentStock.model.CurrentStockGetData
import io.reactivex.Observable
import okhttp3.MultipartBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface GetUserListPhotoRegApi {

    @FormUrlEncoded
    @POST("FaceRegistration/UserList")
    fun getUserListApi(@Field("user_id") user_id: String,@Field("session_token") session_token: String):
            Observable<GetUserListResponse>

    @Multipart
    @POST("FaceImageDetection/FaceImage")
    fun getAddUserFaceImage(@Query("data") face: String, @Part attachments: MultipartBody.Part?): Observable<FaceRegResponse>

    @FormUrlEncoded
    @POST("FaceRegistration/FaceMatch")
    fun getUserFacePic(@Field("user_id") user_id: String,@Field("session_token") session_token: String ): Observable<UserFacePicUrlResponse>


    @FormUrlEncoded
    @POST("FaceImageDetection/FaceImgDelete")
    fun deleteUserPicApi(@Field("user_id") user_id: String,@Field("session_token") session_token: String):
            Observable<DeleteUserPicResponse>

    companion object Factory {
        fun create(): GetUserListPhotoRegApi {
            val retrofit = Retrofit.Builder()
                    .client(NetworkConstant.setTimeOutNoRetry())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(NetworkConstant.BASE_URL)
                    .build()

            return retrofit.create(GetUserListPhotoRegApi::class.java)
        }




        fun createFacePic(): GetUserListPhotoRegApi {
            val retrofit = Retrofit.Builder()
                    .client(NetworkConstant.setTimeOut())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(NetworkConstant.ADD_SHOP_BASE_URL)
                    .build()

            return retrofit.create(GetUserListPhotoRegApi::class.java)
        }
    }


}