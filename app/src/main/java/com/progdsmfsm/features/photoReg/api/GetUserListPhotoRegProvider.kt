package com.progdsmfsm.features.photoReg.api

import com.progdsmfsm.features.stockAddCurrentStock.api.ShopAddStockApi
import com.progdsmfsm.features.stockAddCurrentStock.api.ShopAddStockRepository

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