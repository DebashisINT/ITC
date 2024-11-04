package com.progdsmfsm.features.location.shopdurationapi

import com.progdsmfsm.app.Pref
import com.progdsmfsm.app.utils.AppUtils
import com.progdsmfsm.base.BaseResponse
import com.progdsmfsm.features.location.model.MeetingDurationInputParams
import com.progdsmfsm.features.location.model.ShopDurationRequest
import com.progdsmfsm.features.location.model.VisitRemarksResponseModel
import io.reactivex.Observable
import timber.log.Timber

/**
 * Created by Pratishruti on 29-11-2017.
 */
// Rev 1.0 ShopDurationRepository 07-06-2023  Suman  mantis id 26237
class ShopDurationRepository(val apiService: ShopDurationApi) {
    //Begin of Rev 1.0 ShopDurationRepository 07-06-2023  Suman  mantis id 26237
    /*fun shopDuration(shopDuration: ShopDurationRequest?): Observable<ShopDurationRequest> {
        return apiService.submitShopDuration(shopDuration)
    }*/

    fun shopDuration(shopDuration: ShopDurationRequest?): Observable<ShopDurationRequest> {
        Timber.d("ShopDurationRepository shop_visit_api_call"+ AppUtils.getCurrentDateTime().toString()+"\ndata - "+shopDuration.toString())
        //return apiService.submitShopDurationITC(shopDuration)

        if(Pref.IsUpdateVisitDataInTodayTable){
            return apiService.submitShopDurationITC(shopDuration)
        }else{
            return apiService.submitShopDurationITC(shopDuration)
            //return apiService.submitShopDuration(shopDuration)
        }
    }
    //End of Rev 1.0 ShopDurationRepository 07-06-2023  Suman  mantis id 26237

    fun meetingDuration(meetingDuration: MeetingDurationInputParams?): Observable<BaseResponse> {
        return apiService.submitMeetingDuration(meetingDuration)
    }

    fun getRemarksList(): Observable<VisitRemarksResponseModel> {
        Timber.d("tag VisitRemarks/List api call")
        return apiService.getRemarksList(Pref.session_token!!, Pref.user_id!!)
    }
}