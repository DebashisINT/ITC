package com.breezefsmdsm.features.alarm.api.attendance_report_list_api

import com.breezefsmdsm.app.Pref
import com.breezefsmdsm.features.alarm.model.AttendanceReportDataModel
import io.reactivex.Observable
import timber.log.Timber

/**
 * Created by Saikat on 20-02-2019.
 */
class AttendanceReportRepo(val apiService: AttendanceReportApi) {
    fun getAttendanceReportList(date: String): Observable<AttendanceReportDataModel> {
        Timber.d("AlarmApi attendanceReportResponse")
        return apiService.attendanceReportResponse(Pref.session_token!!, Pref.user_id!!, date)
    }
}