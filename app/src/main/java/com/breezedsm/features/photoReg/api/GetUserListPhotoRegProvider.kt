package com.breezedsm.features.photoReg.api

import com.breezedsm.features.stockAddCurrentStock.api.ShopAddStockApi
import com.breezedsm.features.stockAddCurrentStock.api.ShopAddStockRepository

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