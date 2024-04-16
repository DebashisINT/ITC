package com.breezedsm.features.addshop.api.assignedToDDList

import com.breezedsm.app.Pref
import com.breezedsm.features.addshop.model.assigntoddlist.AssignToDDListResponseModel
import io.reactivex.Observable
import timber.log.Timber

/**
 * Created by Saikat on 03-10-2018.
 */
class AssignToDDListRepo(val apiService: AssignToDDListApi) {
    fun assignToDDList(state_id: String): Observable<AssignToDDListResponseModel> {
        Timber.d("tag_itc_check assignToDDList call")
        return apiService.getAssignedToDDList(Pref.session_token!!, Pref.user_id!!, state_id)
    }
}