package com.progdsmfsm.features.login.api.productlistapi

import com.progdsmfsm.app.NetworkConstant
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
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by Saikat on 20-11-2018.
 */
interface ProductListApi {
    @FormUrlEncoded
    @POST("ProductList/List")
    fun getProductList(@Field("session_token") session_token: String, @Field("user_id") user_id: String, @Field("last_update_date") last_update_date: String): Observable<ProductListResponseModel>

    @FormUrlEncoded
    @POST("ProductList/ITCProdMastList")
    fun getProductListITC(@Field("session_token") session_token: String, @Field("user_id") user_id: String): Observable<GetProductReq>

    @POST("ITCOrderWithProductDetail/ITCOrderWithProductDetailSave")
    fun syncProductListITC(@Body addOrder: SyncOrd): Observable<BaseResponse>

    @POST("ITCOrderWithProductDetail/ITCOrderWithProductDetailEdit")
    fun editProductListITC(@Body editOrder: EditOrd): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("ITCOrderWithProductDetail/ITCListForOrderedProduct")
    fun getOrderHistoryApi( @Field("user_id") user_id: String): Observable<GetOrderHistory>


    @FormUrlEncoded
    @POST("ProductList/ITCProdRateList")
    fun getProductRateListITC(@Field("session_token") session_token: String, @Field("user_id") user_id: String): Observable<GetProductRateReq>


    @FormUrlEncoded
    @POST("ProductList/ProductRate")
    fun getProductRateList(@Field("session_token") session_token: String, @Field("user_id") user_id: String, @Field("shop_id") shop_id: String): Observable<ProductRateListResponseModel>

    @FormUrlEncoded
    @POST("ProductList/OfflineProductRate")
    fun getOfflineProductRateList(@Field("session_token") session_token: String, @Field("user_id") user_id: String): Observable<ProductListOfflineResponseModel>

    @FormUrlEncoded
    @POST("ProductList/OfflineProductRate")
    fun getOfflineProductRateListNew(@Field("session_token") session_token: String, @Field("user_id") user_id: String): Observable<ProductListOfflineResponseModelNew>


    @POST("ITCOrderWithProductDetail/ITCOrderWithProductDetailDelete")
    fun deleteOrderITCApi(@Body addOrder: DeleteOrd): Observable<BaseResponse>

    /**
     * Companion object to create the GithubApiService
     */
    companion object Factory {
        fun create(): ProductListApi {
            val retrofit = Retrofit.Builder()
                    .client(NetworkConstant.setTimeOut())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(NetworkConstant.BASE_URL)
                    .build()

            return retrofit.create(ProductListApi::class.java)
        }
    }
}