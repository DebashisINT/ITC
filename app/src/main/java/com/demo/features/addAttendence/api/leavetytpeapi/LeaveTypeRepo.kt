package com.demo.features.addAttendence.api.leavetytpeapi

import com.demo.app.Pref
import com.demo.features.addAttendence.model.LeaveTypeResponseModel
import io.reactivex.Observable

/**
 * Created by Saikat on 22-11-2018.
 */
class LeaveTypeRepo(val apiService: LeaveTypeApi) {
    fun getLeaveTypeList(): Observable<LeaveTypeResponseModel> {
        return apiService.getLeaveTypeList(Pref.session_token!!, Pref.user_id!!)
    }
}