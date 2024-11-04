package com.progdsmfsm.features.dashboard.presentation.api.gteroutelistapi

import com.progdsmfsm.app.Pref
import com.progdsmfsm.features.dashboard.presentation.model.SelectedRouteListResponseModel
import io.reactivex.Observable
import timber.log.Timber

/**
 * Created by Saikat on 03-12-2018.
 */
class GetRouteListRepo(val apiService: GetRouteListApi) {
    fun routeList(): Observable<SelectedRouteListResponseModel> {
        Timber.d("tag getRouteList RouteList/ListofRoutes api call")
        return apiService.getRouteList(Pref.session_token!!, Pref.user_id!!)
    }
}