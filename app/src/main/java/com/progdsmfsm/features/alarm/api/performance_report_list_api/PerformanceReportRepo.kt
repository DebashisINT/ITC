package com.progdsmfsm.features.alarm.api.performance_report_list_api

import com.progdsmfsm.app.Pref
import com.progdsmfsm.features.alarm.model.PerformanceReportResponseModel
import io.reactivex.Observable
import timber.log.Timber

/**
 * Created by Saikat on 21-02-2019.
 */
class PerformanceReportRepo(val apiService: PerformanceReportApi) {
    fun getPerformanceReportList(from_date: String, to_date: String): Observable<PerformanceReportResponseModel> {
        Timber.d("AlarmApi performanceReportResponse")
        return apiService.performanceReportResponse(Pref.session_token!!, Pref.user_id!!, from_date, to_date)
    }
}