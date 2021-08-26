package com.demo.features.nearbyuserlist.api

import com.demo.app.Pref
import com.demo.features.nearbyuserlist.model.NearbyUserResponseModel
import com.demo.features.newcollection.model.NewCollectionListResponseModel
import com.demo.features.newcollection.newcollectionlistapi.NewCollectionListApi
import io.reactivex.Observable

class NearbyUserRepo(val apiService: NearbyUserApi) {
    fun nearbyUserList(): Observable<NearbyUserResponseModel> {
        return apiService.getNearbyUserList(Pref.session_token!!, Pref.user_id!!)
    }
}