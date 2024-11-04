package com.progdsmfsm.features.nearbyuserlist.api

import com.progdsmfsm.app.Pref
import com.progdsmfsm.features.nearbyuserlist.model.NearbyUserResponseModel
import com.progdsmfsm.features.newcollection.model.NewCollectionListResponseModel
import com.progdsmfsm.features.newcollection.newcollectionlistapi.NewCollectionListApi
import io.reactivex.Observable

class NearbyUserRepo(val apiService: NearbyUserApi) {
    fun nearbyUserList(): Observable<NearbyUserResponseModel> {
        return apiService.getNearbyUserList(Pref.session_token!!, Pref.user_id!!)
    }
}