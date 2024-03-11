package com.breezedsm.features.alarm.api.report_confirm_api

import com.breezedsm.base.BaseResponse
import com.breezedsm.features.alarm.model.ReviewConfirmInputModel
import io.reactivex.Observable
import timber.log.Timber

/**
 * Created by Saikat on 21-02-2019.
 */
class ReviewConfirmRepo(val apiService: ReviewConfirmApi) {
    fun reviewConfirm(reviewConfirm: ReviewConfirmInputModel): Observable<BaseResponse> {
        Timber.d("AlarmApi alarmConfigResponse")
        return apiService.reviewConfirm(reviewConfirm)
    }
}