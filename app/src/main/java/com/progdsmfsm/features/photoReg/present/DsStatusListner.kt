package com.progdsmfsm.features.photoReg.present

import com.progdsmfsm.app.domain.ProspectEntity
import com.progdsmfsm.app.domain.StageEntity
import com.progdsmfsm.features.photoReg.model.UserListResponseModel

interface DsStatusListner {
    fun getDSInfoOnLick(obj: ProspectEntity)
    fun getDSInfoOnLick(obj: StageEntity)
}