package com.breezefsmdsm.features.photoReg.api

import com.breezefsmdsm.features.stockAddCurrentStock.api.ShopAddStockApi
import com.breezefsmdsm.features.stockAddCurrentStock.api.ShopAddStockRepository

object GetUserListPhotoRegProvider {

    fun provideUserListPhotoReg(): GetUserListPhotoRegRepository {
        return GetUserListPhotoRegRepository(GetUserListPhotoRegApi.create())
    }

    fun providePhotoReg(): GetUserListPhotoRegRepository {
        return GetUserListPhotoRegRepository(GetUserListPhotoRegApi.createFacePic())
    }


    fun jobMultipartRepoProvider(): GetUserListPhotoRegRepository {
        return GetUserListPhotoRegRepository(GetUserListPhotoRegApi.createMultiPart())
    }

}