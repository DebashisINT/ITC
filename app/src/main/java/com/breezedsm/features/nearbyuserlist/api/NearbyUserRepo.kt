package com.breezedsm.features.nearbyuserlist.api

import com.breezedsm.app.Pref
import com.breezedsm.features.nearbyuserlist.model.NearbyUserResponseModel
import com.breezedsm.features.newcollection.model.NewCollectionListResponseModel
import com.breezedsm.features.newcollection.newcollectionlistapi.NewCollectionListApi
import io.reactivex.Observable

class NearbyUserRepo(val apiService: NearbyUserApi) {
    fun nearbyUserList(): Observable<NearbyUserResponseModel> {
        return apiService.getNearbyUserList(Pref.session_token!!, Pref.user_id!!)
    }
}