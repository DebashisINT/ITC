package com.breezedsm.features.quotation.api

import com.breezedsm.app.Pref
import com.breezedsm.base.BaseResponse
import com.breezedsm.features.quotation.model.AddQuotInputModel
import com.breezedsm.features.quotation.model.BSListResponseModel
import com.breezedsm.features.quotation.model.QuotationListResponseModel
import io.reactivex.Observable
import timber.log.Timber

/**
 * Created by Saikat on 12-Jun-20.
 */
class QuotationRepo(val apiService: QuotationApi) {
    fun getBSList(): Observable<BSListResponseModel> {
        Timber.d("Qut api_call getBSList")
        return apiService.getBSList(Pref.session_token!!, Pref.user_id!!)
    }

    fun addQuot(addQuot: AddQuotInputModel): Observable<BaseResponse> {
        Timber.d("Qut api_call addQuot")
        return apiService.AddQuotation(addQuot)
    }

    fun getQuotList(): Observable<QuotationListResponseModel> {
        Timber.d("Qut api_call getQuotList")
        return apiService.getQuotationList(Pref.session_token!!, Pref.user_id!!)
    }

    fun sendQuoSmsMail(quo_id: String, shop_id: String, isSms: Boolean): Observable<BaseResponse> {
        Timber.d("Qut api_call sendQuoSmsMail")
        return apiService.sendQuotationMailSms(Pref.session_token!!, Pref.user_id!!, quo_id, shop_id, isSms)
    }
}