package com.breezefsmdsm.features.alarm.api.visit_report_api

import com.breezefsmdsm.app.Pref
import com.breezefsmdsm.features.alarm.model.VisitReportResponseModel
import io.reactivex.Observable
import timber.log.Timber

/**
 * Created by Saikat on 21-02-2019.
 */
class VisitReportRepo(val apiService: VisitReportApi) {
    fun getVisitReportList(from_date: String, to_date: String): Observable<VisitReportResponseModel> {
        Timber.d("AlarmApi visitReportResponse")
        return apiService.visitReportResponse(Pref.session_token!!, Pref.user_id!!, from_date, to_date)
    }
}