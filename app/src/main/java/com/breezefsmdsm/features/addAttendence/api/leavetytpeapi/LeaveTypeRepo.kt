package com.breezefsmdsm.features.addAttendence.api.leavetytpeapi

import com.breezefsmdsm.app.Pref
import com.breezefsmdsm.features.addAttendence.model.LeaveTypeResponseModel
import io.reactivex.Observable

/**
 * Created by Saikat on 22-11-2018.
 */
class LeaveTypeRepo(val apiService: LeaveTypeApi) {
    fun getLeaveTypeList(): Observable<LeaveTypeResponseModel> {
        return apiService.getLeaveTypeList(Pref.session_token!!, Pref.user_id!!)
    }
}