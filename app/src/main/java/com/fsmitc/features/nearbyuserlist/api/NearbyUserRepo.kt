package com.fsmitc.features.nearbyuserlist.api

import com.fsmitc.app.Pref
import com.fsmitc.features.nearbyuserlist.model.NearbyUserResponseModel
import com.fsmitc.features.newcollection.model.NewCollectionListResponseModel
import com.fsmitc.features.newcollection.newcollectionlistapi.NewCollectionListApi
import io.reactivex.Observable

class NearbyUserRepo(val apiService: NearbyUserApi) {
    fun nearbyUserList(): Observable<NearbyUserResponseModel> {
        return apiService.getNearbyUserList(Pref.session_token!!, Pref.user_id!!)
    }
}