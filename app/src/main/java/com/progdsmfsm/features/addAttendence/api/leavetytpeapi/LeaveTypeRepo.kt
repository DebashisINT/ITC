package com.progdsmfsm.features.addAttendence.api.leavetytpeapi

import com.progdsmfsm.app.Pref
import com.progdsmfsm.base.BaseResponse
import com.progdsmfsm.features.addAttendence.model.LeaveTypeResponseModel
import com.progdsmfsm.features.photoReg.model.clearAttendanceonRejectReqModelRejectReqModel
import io.reactivex.Observable

/**
 * Created by Saikat on 22-11-2018.
 */
class LeaveTypeRepo(val apiService: LeaveTypeApi) {
    fun getLeaveTypeList(): Observable<LeaveTypeResponseModel> {
        return apiService.getLeaveTypeList(Pref.session_token!!, Pref.user_id!!)
    }

    fun clearAttendanceonRejectclick(clearAttendanceonRejectReModel: clearAttendanceonRejectReqModelRejectReqModel): Observable<BaseResponse> {
        return apiService.clearAttendanceonRejectclick(clearAttendanceonRejectReModel)
    }
}