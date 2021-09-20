package com.breezefsmdsm.features.addshop.api

/**
 * Created by Pratishruti on 22-11-2017.
 */
object AddShopRepositoryProvider {
    fun provideAddShopRepository(): AddShopRepository {
        return AddShopRepository(AddShopApi.create())
    }

    fun provideAddShopWithoutImageRepository(): AddShopRepository {
        return AddShopRepository(AddShopApi.createWithoutMultipart())
    }
}