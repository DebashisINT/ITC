package com.fsmitc.features.photoReg.adapter

import com.fsmitc.features.photoReg.model.UserListResponseModel

interface PhotoRegUserListner {
    fun getUserInfoOnLick(obj: UserListResponseModel)
    fun getPhoneOnLick(phone: String)
    fun getWhatsappOnLick(phone: String)
    fun deletePicOnLick(obj: UserListResponseModel)
    fun viewPicOnLick(img_link: String,name : String)
}