package com.breezefsmdsm.features.login.model.productlistmodel

import com.breezefsmdsm.app.domain.ModelEntity
import com.breezefsmdsm.app.domain.ProductListEntity
import com.breezefsmdsm.base.BaseResponse

class ModelListResponse: BaseResponse() {
    var model_list: ArrayList<ModelEntity>? = null
}