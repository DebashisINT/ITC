package com.breezefsmdsm.features.photoReg.present

import com.breezefsmdsm.app.domain.ProspectEntity
import com.breezefsmdsm.app.domain.StageEntity
import com.breezefsmdsm.features.photoReg.model.UserListResponseModel

interface DsStatusListner {
    fun getDSInfoOnLick(obj: ProspectEntity)
    fun getDSInfoOnLick(obj: StageEntity)
}