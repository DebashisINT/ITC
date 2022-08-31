package com.breezefsmdsm.features.addAttendence.api.leavetytpeapi

import com.breezefsmdsm.base.BaseResponse
import com.breezefsmdsm.features.photoReg.model.clearAttendanceonRejectReqModelRejectReqModel
import io.reactivex.Observable

/**
 * Created by Saikat on 22-11-2018.
 */
object LeaveTypeRepoProvider {
    fun leaveTypeListRepoProvider(): LeaveTypeRepo {
        return LeaveTypeRepo(LeaveTypeApi.create())
    }

}