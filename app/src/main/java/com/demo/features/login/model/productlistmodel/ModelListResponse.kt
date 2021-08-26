package com.demo.features.login.model.productlistmodel

import com.demo.app.domain.ModelEntity
import com.demo.app.domain.ProductListEntity
import com.demo.base.BaseResponse

class ModelListResponse: BaseResponse() {
    var model_list: ArrayList<ModelEntity>? = null
}