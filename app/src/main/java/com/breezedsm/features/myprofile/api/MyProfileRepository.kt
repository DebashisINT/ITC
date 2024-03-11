package com.breezedsm.features.myprofile.api

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import com.breezedsm.app.FileUtils
import com.breezedsm.app.Pref
import com.fasterxml.jackson.databind.ObjectMapper
import com.breezedsm.base.BaseResponse
import com.breezedsm.features.myprofile.model.ProfileUpdateRequestData
import com.breezedsm.features.myprofile.presentation.ProfileData
import com.breezedsm.features.myprofile.presentation.ProfileDataQRResponse
import com.breezedsm.features.nearbyshops.model.StateCityResponseModel
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

/**
 * Created by Pratishruti on 16-02-2018.
 */
class MyProfileRepository(val apiService: MyProfileApi) {
    fun updateProfileWithImage(profileData: ProfileUpdateRequestData, profile_img:String, context: Context): Observable<BaseResponse> {

        var profile_img_data: MultipartBody.Part?=null
        var jsonInString=""
        try {
            jsonInString = ObjectMapper().writeValueAsString(profileData)
            if (!TextUtils.isEmpty(profile_img))
            {
                val profile_img_file = /*FileUtils.getFile(context, Uri.parse(profile_img))*/ File(profile_img)
                //val profile_img_file = FileUtils.getFile(context, Uri.parse(profile_img))
                val profileImgBody = RequestBody.create(MediaType.parse("multipart/form-data"), profile_img_file)
                profile_img_data = MultipartBody.Part.createFormData("profile_image", profile_img_file.name, profileImgBody)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        if (profile_img_data==null){
            return apiService.uploadProfileWithOutImage(jsonInString)
        }else{
            return apiService.uploadProfileWithImage(jsonInString,profile_img_data)
        }

    }

    fun updateProfileQRImage(profileData: ProfileData, profile_img:String, context: Context): Observable<ProfileDataQRResponse> {

        var profile_img_data: MultipartBody.Part?=null
        var jsonInString=""
        try {
            jsonInString = ObjectMapper().writeValueAsString(profileData)
            if (!TextUtils.isEmpty(profile_img))
            {
                val profile_img_file = File(profile_img)
                val profileImgBody = RequestBody.create(MediaType.parse("multipart/form-data"), profile_img_file)
                profile_img_data = MultipartBody.Part.createFormData("attachments", profile_img_file.name, profileImgBody)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return apiService.uploadProfileQRImage(jsonInString,profile_img_data)
    }


    fun getStateCityList(pinCode: String): Observable<StateCityResponseModel> {
        return apiService.getStateCityList(Pref.session_token!!, Pref.user_id!!, pinCode)
    }

    fun getQRImage(): Observable<ProfileDataQRResponse> {
        return apiService.getQrImageApi(Pref.session_token!!,Pref.user_id!!)
    }

}