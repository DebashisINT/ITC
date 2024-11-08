package com.progdsmfsm.features.nearbyshops.api

import com.progdsmfsm.app.Pref
import com.progdsmfsm.base.BaseResponse
import com.progdsmfsm.features.login.model.productlistmodel.ModelListResponse
import com.progdsmfsm.features.nearbyshops.model.*
import io.reactivex.Observable

/**
 * Created by Pratishruti on 28-11-2017.
 */
class ShopListRepository(val apiService: ShopListApi) {
    fun getShopList(sessiontoken: String, user_id: String): Observable<ShopListResponse> {
        return apiService.getShopList(sessiontoken, user_id)
    }

    fun getShopInativeList(sessiontoken: String, user_id: String): Observable<ShopListResponse> {
        return apiService.getShopInativeList(sessiontoken, user_id)
    }

    fun getShopTypeList(): Observable<ShopTypeResponseModel> {
        return apiService.getShopTypeList(Pref.session_token!!, Pref.user_id!!)
    }

    fun getShopTypeStockVisibilityList(): Observable<ShopTypeStockViewResponseModel> {
        return apiService.getShopTypeListStockView(Pref.session_token!!, Pref.user_id!!)
    }

    fun getModelList(): Observable<ModelListResponseModel> {
        return apiService.getModelList(Pref.session_token!!, Pref.user_id!!)
    }

    fun getModelListNew(): Observable<ModelListResponse> {
        return apiService.getModelListNew(Pref.session_token!!, Pref.user_id!!)
    }

    fun getPrimaryAppList(): Observable<PrimaryAppListResponseModel> {
        return apiService.getPrimaryAppList(Pref.session_token!!, Pref.user_id!!)
    }

    fun getSecondaryAppList(): Observable<SecondaryAppListResponseModel> {
        return apiService.getSecondaryAppList(Pref.session_token!!, Pref.user_id!!)
    }

    fun getLeadTypeList(): Observable<LeadListResponseModel> {
        return apiService.getLeadList(Pref.session_token!!, Pref.user_id!!)
    }

    fun getStagList(): Observable<StageListResponseModel> {
        return apiService.getStageList(Pref.session_token!!, Pref.user_id!!)
    }

    fun getProsList(): Observable<ProsListResponseModel> {
        return apiService.getProsList(Pref.session_token!!, Pref.user_id!!)
    }

    fun deleteImei(): Observable<BaseResponse> {
        return apiService.deleteImeiAPI(Pref.session_token!!, Pref.user_id!!)
    }

    fun getFunnelStageList(): Observable<FunnelStageListResponseModel> {
        return apiService.getFunnelStageList(Pref.session_token!!, Pref.user_id!!)
    }
}