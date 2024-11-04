package com.progdsmfsm.features.addshop.api.assignToPPList

import com.progdsmfsm.app.Pref
import com.progdsmfsm.features.addshop.model.assigntopplist.AssignToPPListResponseModel
import io.reactivex.Observable
import timber.log.Timber

/**
 * Created by Saikat on 03-10-2018.
 */
class AssignToPPListRepo(val apiService: AssignToPPListApi) {
    fun assignToPPList(state_id: String): Observable<AssignToPPListResponseModel> {
        Timber.d("tag_itc_check assignToPPList call")
        return apiService.getAssignedToPPList(Pref.session_token!!, Pref.user_id!!, state_id)
    }
}