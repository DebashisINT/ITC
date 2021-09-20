package com.breezefsmdsm.features.newcollection.model

import com.breezefsmdsm.app.domain.CollectionDetailsEntity
import com.breezefsmdsm.base.BaseResponse
import com.breezefsmdsm.features.shopdetail.presentation.model.collectionlist.CollectionListDataModel

/**
 * Created by Saikat on 15-02-2019.
 */
class NewCollectionListResponseModel : BaseResponse() {
    //var collection_list: ArrayList<CollectionListDataModel>? = null
    var collection_list: ArrayList<CollectionDetailsEntity>? = null
}