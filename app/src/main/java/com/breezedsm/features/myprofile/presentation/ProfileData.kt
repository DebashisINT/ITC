package com.breezedsm.features.myprofile.presentation

data class ProfileData(var user_id:String="",var session_token:String="")

data class ProfileDataQRResponse(var status:String="",var message:String="",var qr_img_link:String="")