package com.fsmitc.features.photoReg.api

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import com.fsmitc.app.FileUtils
import com.fsmitc.base.BaseResponse
import com.fsmitc.features.addshop.model.AddShopRequestCompetetorImg
import com.fsmitc.features.dashboard.presentation.DashboardActivity
import com.fsmitc.features.photoReg.model.*
import com.fsmitc.features.stockAddCurrentStock.api.ShopAddStockApi
import com.fsmitc.features.stockAddCurrentStock.model.CurrentStockGetData
import com.google.gson.Gson
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class GetUserListPhotoRegRepository(val apiService : GetUserListPhotoRegApi) {


    fun getUserListApi(user_id: String, session_token: String): Observable<GetUserListResponse> {
        return apiService.getUserListApi(user_id,session_token)
    }

    fun deleteUserPicApi(user_id: String, session_token: String): Observable<DeleteUserPicResponse> {
        return apiService.deleteUserPicApi(user_id,session_token)
    }

    fun getUserFacePicUrlApi(user_id: String, session_token: String): Observable<UserFacePicUrlResponse> {
        return apiService.getUserFacePic(user_id,session_token)
    }

    fun addUserFaceRegImg(obj: UserPhotoRegModel, user_image: String?, context: Context,user_contactid:String?): Observable<FaceRegResponse> {
        var profile_img_data: MultipartBody.Part? = null
        if (!TextUtils.isEmpty(user_image)){
            val profile_img_file = FileUtils.getFile(context, Uri.parse(user_image))
            if (profile_img_file != null && profile_img_file.exists()) {
                val profileImgBody = RequestBody.create(MediaType.parse("multipart/form-data"), profile_img_file)
                profile_img_data = MultipartBody.Part.createFormData("attachments", profile_img_file.name.replaceAfter("cropped",user_contactid.toString()).replace("cropped","")+".jpg", profileImgBody)
            }
        }


        var jsonInString = ""
        try {
            jsonInString = Gson().toJson(obj)
            //  shopObject = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonInString)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return  apiService.getAddUserFaceImage(jsonInString, profile_img_data)
    }

}