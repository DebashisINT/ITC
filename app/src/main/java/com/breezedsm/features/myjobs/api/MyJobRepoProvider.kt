package com.breezedsm.features.myjobs.api

import android.content.Context
import android.net.Uri
import android.util.Log
import com.breezedsm.app.FileUtils
import com.breezedsm.base.BaseResponse
import com.breezedsm.features.activities.model.ActivityImage
import com.breezedsm.features.activities.model.AddActivityInputModel
import com.breezedsm.features.myjobs.model.WIPSubmit
import com.fasterxml.jackson.databind.ObjectMapper
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

object MyJobRepoProvider {
    fun jobRepoProvider(): MyJobRepo {
        return MyJobRepo(MyJobApi.create())
    }

    fun jobMultipartRepoProvider(): MyJobRepo {
        return MyJobRepo(MyJobApi.createMultiPart())
    }

}