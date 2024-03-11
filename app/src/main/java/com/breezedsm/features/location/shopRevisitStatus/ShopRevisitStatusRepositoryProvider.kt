package com.breezedsm.features.location.shopRevisitStatus

import com.breezedsm.features.location.shopdurationapi.ShopDurationApi
import com.breezedsm.features.location.shopdurationapi.ShopDurationRepository

object ShopRevisitStatusRepositoryProvider {
    fun provideShopRevisitStatusRepository(): ShopRevisitStatusRepository {
        return ShopRevisitStatusRepository(ShopRevisitStatusApi.create())
    }
}