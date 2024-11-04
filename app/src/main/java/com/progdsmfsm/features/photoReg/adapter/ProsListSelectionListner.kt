package com.progdsmfsm.features.photoReg.adapter

import com.progdsmfsm.features.photoReg.model.ProsCustom
import com.progdsmfsm.features.photoReg.model.UserListResponseModel

interface ProsListSelectionListner {
    fun getInfo(obj: ProsCustom)
}