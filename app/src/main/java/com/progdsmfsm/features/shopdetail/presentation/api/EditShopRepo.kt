package com.progdsmfsm.features.shopdetail.presentation.api

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import com.progdsmfsm.app.FileUtils
import com.progdsmfsm.app.Pref
import com.progdsmfsm.features.addshop.model.AddLogReqData
import com.progdsmfsm.features.addshop.model.AddShopRequestData
import com.progdsmfsm.features.addshop.model.AddShopResponse
import com.progdsmfsm.features.addshop.model.LogFileResponse
import com.progdsmfsm.features.dashboard.presentation.DashboardActivity
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/**
 * Created by Saikat on 10-10-2018.
 */
class EditShopRepo(val apiService: EditShopApi) {

    fun editShop(shop: AddShopRequestData): Observable<AddShopResponse> {
        return apiService.editShop(shop)
    }

    fun addShopWithImage(shop: AddShopRequestData, shop_image: String?, context: Context): Observable<AddShopResponse> {
        var profile_img_data: MultipartBody.Part? = null
        var profile_img_file: File? = null

        if (!TextUtils.isEmpty(shop_image))
            profile_img_file = FileUtils.getFile(context, Uri.parse(shop_image))

        if (profile_img_file != null && profile_img_file.exists()) {
            val profileImgBody = RequestBody.create(MediaType.parse("multipart/form-data"), profile_img_file)
            profile_img_data = MultipartBody.Part.createFormData("shop_image", profile_img_file.name, profileImgBody)
        } else {
            var mFile: File
            mFile = (context as DashboardActivity).getShopDummyImageFile()
            val profileImgBody = RequestBody.create(MediaType.parse("multipart/form-data"), mFile)
            profile_img_data = MultipartBody.Part.createFormData("shop_image", mFile.name, profileImgBody)
        }


        //var shopObject: RequestBody? = null
        var jsonInString = ""
        try {
            jsonInString = ObjectMapper().writeValueAsString(shop)
            //  shopObject = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonInString)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return apiService.editShopWithImage(jsonInString, profile_img_data)
        // return apiService.getAddShopWithoutImage(jsonInString)
    }

    fun addShopWithImage(shop: AddShopRequestData, shop_image: String?, degree_image: String?, context: Context): Observable<AddShopResponse> {
        var profile_img_data: MultipartBody.Part? = null
        var degree_img_data: MultipartBody.Part? = null

        /*val profile_img_file = FileUtils.getFile(context, Uri.parse(shop_image))
        if (profile_img_file != null && profile_img_file.exists()) {
            val profileImgBody = RequestBody.create(MediaType.parse("multipart/form-data"), profile_img_file)
            profile_img_data = MultipartBody.Part.createFormData("shop_image", profile_img_file.name, profileImgBody)
        } else {
            var mFile: File
            mFile = (context as DashboardActivity).getShopDummyImageFile()
            val profileImgBody = RequestBody.create(MediaType.parse("multipart/form-data"), mFile)
            profile_img_data = MultipartBody.Part.createFormData("shop_image", mFile.name, profileImgBody)
        }

        if (!TextUtils.isEmpty(degree_image)) {
            val degree_image_file = FileUtils.getFile(context, Uri.parse(degree_image))
            if (degree_image_file != null && degree_image_file.exists()) {
                val degreeImgBody = RequestBody.create(MediaType.parse("multipart/form-data"), degree_image_file)
                degree_img_data = MultipartBody.Part.createFormData("degree_image", degree_image_file.name, degreeImgBody)
            } else {
                var mFile: File
                mFile = (context as DashboardActivity).getShopDummyImageFile()
                val degreeImgBody = RequestBody.create(MediaType.parse("multipart/form-data"), mFile)
                degree_img_data = MultipartBody.Part.createFormData("degree_image", mFile.name, degreeImgBody)
            }
        }*/

        if (!TextUtils.isEmpty(shop_image)) {
            val profile_img_file = FileUtils.getFile(context, Uri.parse(shop_image))

            if (profile_img_file != null && profile_img_file.exists()) {
                val profileImgBody = RequestBody.create(MediaType.parse("multipart/form-data"), profile_img_file)
                profile_img_data = MultipartBody.Part.createFormData("shop_image", profile_img_file.name, profileImgBody)
            } else {
                var mFile: File
                mFile = (context as DashboardActivity).getShopDummyImageFile()
                val profileImgBody = RequestBody.create(MediaType.parse("multipart/form-data"), mFile)
                profile_img_data = MultipartBody.Part.createFormData("shop_image", mFile.name, profileImgBody)
            }
        } else if (!TextUtils.isEmpty(degree_image)) {
            val degree_image_file = FileUtils.getFile(context, Uri.parse(degree_image))
            if (degree_image_file != null && degree_image_file.exists()) {
                val degreeImgBody = RequestBody.create(MediaType.parse("multipart/form-data"), degree_image_file)
                degree_img_data = MultipartBody.Part.createFormData("degree", degree_image_file.name, degreeImgBody)
            } else {
                var mFile: File
                mFile = (context as DashboardActivity).getShopDummyImageFile()
                val degreeImgBody = RequestBody.create(MediaType.parse("multipart/form-data"), mFile)
                degree_img_data = MultipartBody.Part.createFormData("degree", mFile.name, degreeImgBody)
            }
        }

        //var shopObject: RequestBody? = null
        var jsonInString = ""
        try {
            jsonInString = Gson().toJson(shop)
            //  shopObject = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonInString)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        var gc=profile_img_data
        var gcc=profile_img_data

        return if (degree_img_data != null)
            apiService.editShopWithDegImage(jsonInString, degree_img_data)
        else
            apiService.editShopWithImage(jsonInString, profile_img_data)
        // return apiService.getAddShopWithoutImage(jsonInString)
    }

    fun addLogfile(user_id: AddLogReqData, shop_image: String, context: Context): Observable<LogFileResponse> {
        var log_attachments_new:MultipartBody.Part? = null
        var log_attachments_file: File? = null
        log_attachments_file = File(shop_image)
        val profileImgBody = RequestBody.create(MediaType.parse("multipart/form-data"), log_attachments_file)
        var jsonInString = ""
        try {
            jsonInString = ObjectMapper().writeValueAsString(user_id)
            //  shopObject = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonInString)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        //log_attachments_new = MultipartBody.Part.createFormData("attachments", Pref.user_id, profileImgBody)
        log_attachments_new = MultipartBody.Part.createFormData("attachments", "${Pref.user_id}.zip", profileImgBody)
        return  apiService.logshareFile(jsonInString, log_attachments_new)
    }
}