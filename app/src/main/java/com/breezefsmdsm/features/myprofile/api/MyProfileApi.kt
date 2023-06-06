package com.breezefsmdsm.features.myprofile.api

import com.breezefsmdsm.app.NetworkConstant
import com.breezefsmdsm.base.BaseResponse
import com.breezefsmdsm.features.myprofile.presentation.ProfileDataQRResponse
import com.breezefsmdsm.features.nearbyshops.model.ShopListResponse
import com.breezefsmdsm.features.nearbyshops.model.StateCityResponseModel
import io.reactivex.Observable
import okhttp3.MultipartBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

/**
 * Created by Pratishruti on 16-02-2018.
 */
interface MyProfileApi {
    @Multipart
    @POST("UpdateUserProfile/Profile")
    fun uploadProfileWithImage(@Query("data") addShop: String, @Part logo_img_data: MultipartBody.Part?): Observable<BaseResponse>

    @Multipart
    @POST("QRCodeImageInfo/SaveQRCodeImage")
    fun uploadProfileQRImage(@Query("data") addShop: String, @Part logo_img_data: MultipartBody.Part?): Observable<ProfileDataQRResponse>

//    @Multipart
    @POST("UpdateUserProfile/Profile")
    fun uploadProfileWithOutImage(@Query("data") addShop: String): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("Address/FetchAddress")
    fun getStateCityList(@Field("session_token") session_token:String, @Field("user_id") user_id:String,
                        @Field("pin_code") pin_code:String): Observable<StateCityResponseModel>

    @FormUrlEncoded
    @POST("QRCodeImageFetchDelete/FetchQRCodeImageLink")
    fun getQrImageApi(@Field("session_token") session_token:String, @Field("user_id") user_id:String): Observable<ProfileDataQRResponse>


    /**
     * Companion object to create the GithubApiService
     */
    companion object Factory {
        fun create(): MyProfileApi {
            val retrofit = Retrofit.Builder()
                    .client(NetworkConstant.setTimeOut())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(NetworkConstant.ADD_SHOP_BASE_URL)
                    .build()

            return retrofit.create(MyProfileApi::class.java)
        }

        fun onlyCreate(): MyProfileApi {
            val retrofit = Retrofit.Builder()
                    .client(NetworkConstant.setTimeOut())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(NetworkConstant.BASE_URL)
                    .build()

            return retrofit.create(MyProfileApi::class.java)
        }
    }


}