package com.progdsmfsm.features.login.model.productlistmodel

import com.progdsmfsm.app.domain.ModelEntity
import com.progdsmfsm.app.domain.ProductListEntity
import com.progdsmfsm.base.BaseResponse

class ModelListResponse: BaseResponse() {
    var model_list: ArrayList<ModelEntity>? = null
}