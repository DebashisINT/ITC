package com.breezedsm.features.login.model.productlistmodel

import com.breezedsm.app.domain.ModelEntity
import com.breezedsm.app.domain.ProductListEntity
import com.breezedsm.base.BaseResponse

class ModelListResponse: BaseResponse() {
    var model_list: ArrayList<ModelEntity>? = null
}