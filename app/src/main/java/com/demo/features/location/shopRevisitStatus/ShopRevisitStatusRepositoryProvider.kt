package com.demo.features.location.shopRevisitStatus

import com.demo.features.location.shopdurationapi.ShopDurationApi
import com.demo.features.location.shopdurationapi.ShopDurationRepository

object ShopRevisitStatusRepositoryProvider {
    fun provideShopRevisitStatusRepository(): ShopRevisitStatusRepository {
        return ShopRevisitStatusRepository(ShopRevisitStatusApi.create())
    }
}