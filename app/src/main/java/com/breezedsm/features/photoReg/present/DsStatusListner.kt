package com.breezedsm.features.photoReg.present

import com.breezedsm.app.domain.ProspectEntity
import com.breezedsm.app.domain.StageEntity
import com.breezedsm.features.photoReg.model.UserListResponseModel

interface DsStatusListner {
    fun getDSInfoOnLick(obj: ProspectEntity)
    fun getDSInfoOnLick(obj: StageEntity)
}