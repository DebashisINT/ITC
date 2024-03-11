package com.breezedsm.features.newcollection.model

import com.breezedsm.app.domain.CollectionDetailsEntity
import com.breezedsm.base.BaseResponse
import com.breezedsm.features.shopdetail.presentation.model.collectionlist.CollectionListDataModel

/**
 * Created by Saikat on 15-02-2019.
 */
class NewCollectionListResponseModel : BaseResponse() {
    //var collection_list: ArrayList<CollectionListDataModel>? = null
    var collection_list: ArrayList<CollectionDetailsEntity>? = null
}