package com.progdsmfsm.features.attendance.api

import com.progdsmfsm.features.attendance.model.AttendSummResponse
import com.progdsmfsm.features.attendance.model.AttendanceRequest
import com.progdsmfsm.features.attendance.model.AttendanceResponse
import com.progdsmfsm.features.attendance.model.DayStartEndListResponse
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