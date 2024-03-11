package com.breezedsm.features.attendance.api

import com.breezedsm.features.attendance.model.AttendSummResponse
import com.breezedsm.features.attendance.model.AttendanceRequest
import com.breezedsm.features.attendance.model.AttendanceResponse
import com.breezedsm.features.attendance.model.DayStartEndListResponse
import io.reactivex.Observable

/**
 * Created by Pratishruti on 30-11-2017.
 */
class AttendanceListRepository(val apiService: AttendanceListApi) {
    fun getAttendanceList(attendanceRequest: AttendanceRequest?): Observable<AttendanceResponse> {
        return apiService.getAttendanceList(attendanceRequest)
    }
    fun getDayStartEndList(attendanceRequest: AttendanceRequest?): Observable<DayStartEndListResponse> {
        return apiService.getDayStartEndListAPI(attendanceRequest)
    }

    fun getAttendanceSumm(user_id:String):Observable<AttendSummResponse>{
        return apiService.getAttendanceSummApi(user_id)
    }
}