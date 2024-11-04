package com.progdsmfsm.features.newcollection.model

import com.progdsmfsm.app.domain.CollectionDetailsEntity
import com.progdsmfsm.base.BaseResponse
import com.progdsmfsm.features.shopdetail.presentation.model.collectionlist.CollectionListDataModel

/**
 * Created by Saikat on 15-02-2019.
 */
class NewCollectionListResponseModel : BaseResponse() {
    //var collection_list: ArrayList<CollectionListDataModel>? = null
    var collection_list: ArrayList<CollectionDetailsEntity>? = null
}