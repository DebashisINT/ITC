package com.progdsmfsm.features.addshop.api.typeList


import com.progdsmfsm.app.Pref
import com.progdsmfsm.base.BaseResponse
import com.progdsmfsm.features.addshop.model.*
import com.progdsmfsm.features.addshop.model.assigntopplist.AssignToPPListResponseModel
import io.reactivex.Observable
import timber.log.Timber

/**
 * Created by Saikat on 22-Jun-20.
 */
class TypeListRepo(val apiService: TypeListApi) {
    fun typeList(): Observable<TypeListResponseModel> {
        return apiService.getTypeList(Pref.session_token!!, Pref.user_id!!)
    }

    fun entityList(): Observable<EntityResponseModel> {
        println("tag_entityc_call TypeListRepo getEntityTypeListApi calling")
        Timber.d("tag_entityc_call TypeListRepo getEntityTypeListApi calling")
        return apiService.getEntityList(Pref.session_token!!, Pref.user_id!!)
    }

    fun partyStatusList(): Observable<PartyStatusResponseModel> {
        return apiService.getPartyStatusList(Pref.session_token!!, Pref.user_id!!)
    }

    fun updatePartyStatus(shopId: String, party_status_id: String, reason: String): Observable<BaseResponse> {
        return apiService.updatePartyStatus(Pref.session_token!!, Pref.user_id!!, shopId, party_status_id, reason)
    }

    fun retailerList(): Observable<RetailerListResponseModel> {
        return apiService.getRetailerList(Pref.session_token!!, Pref.user_id!!)
    }

    fun dealerList(): Observable<DealerListResponseModel> {
        return apiService.getDealerList(Pref.session_token!!, Pref.user_id!!)
    }

    fun beatList(): Observable<BeatListResponseModel> {
        return apiService.getBeatList(Pref.session_token!!, Pref.user_id!!)
    }

    fun updateBankDetails(shopId: String, accountHolder: String, accountNo: String, bankName: String, ifsc: String, upi: String): Observable<BaseResponse> {
        return apiService.updateBankDetails(Pref.session_token!!, Pref.user_id!!, shopId, accountHolder, accountNo, bankName, ifsc, upi)
    }

    fun assignToShopList(state_id: String): Observable<AssignedToShopListResponseModel> {
        Timber.d("tag_itc_check getAssignedToShopList call")
        return apiService.getAssignedToShopList(Pref.session_token!!, Pref.user_id!!, state_id)
    }
}