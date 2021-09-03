package com.fsmitc.features.location.shopRevisitStatus

import com.fsmitc.features.location.shopdurationapi.ShopDurationApi
import com.fsmitc.features.location.shopdurationapi.ShopDurationRepository

object ShopRevisitStatusRepositoryProvider {
    fun provideShopRevisitStatusRepository(): ShopRevisitStatusRepository {
        return ShopRevisitStatusRepository(ShopRevisitStatusApi.create())
    }
}