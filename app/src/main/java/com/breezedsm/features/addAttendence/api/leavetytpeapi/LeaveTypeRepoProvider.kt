package com.breezedsm.features.addAttendence.api.leavetytpeapi

import com.breezedsm.base.BaseResponse
import com.breezedsm.features.photoReg.model.clearAttendanceonRejectReqModelRejectReqModel
import io.reactivex.Observable

/**
 * Created by Saikat on 22-11-2018.
 */
object LeaveTypeRepoProvider {
    fun leaveTypeListRepoProvider(): LeaveTypeRepo {
        return LeaveTypeRepo(LeaveTypeApi.create())
    }

}