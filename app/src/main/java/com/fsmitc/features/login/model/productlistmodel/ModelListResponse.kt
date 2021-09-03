package com.fsmitc.features.login.model.productlistmodel

import com.fsmitc.app.domain.ModelEntity
import com.fsmitc.app.domain.ProductListEntity
import com.fsmitc.base.BaseResponse

class ModelListResponse: BaseResponse() {
    var model_list: ArrayList<ModelEntity>? = null
}