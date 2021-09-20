package com.breezefsmdsm.features.nearbyuserlist.api

import com.breezefsmdsm.app.Pref
import com.breezefsmdsm.features.nearbyuserlist.model.NearbyUserResponseModel
import com.breezefsmdsm.features.newcollection.model.NewCollectionListResponseModel
import com.breezefsmdsm.features.newcollection.newcollectionlistapi.NewCollectionListApi
import io.reactivex.Observable

class NearbyUserRepo(val apiService: NearbyUserApi) {
    fun nearbyUserList(): Observable<NearbyUserResponseModel> {
        return apiService.getNearbyUserList(Pref.session_token!!, Pref.user_id!!)
    }
}