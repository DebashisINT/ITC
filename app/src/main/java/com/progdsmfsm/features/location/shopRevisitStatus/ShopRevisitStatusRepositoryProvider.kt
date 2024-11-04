package com.progdsmfsm.features.location.shopRevisitStatus

import com.progdsmfsm.features.location.shopdurationapi.ShopDurationApi
import com.progdsmfsm.features.location.shopdurationapi.ShopDurationRepository

object ShopRevisitStatusRepositoryProvider {
    fun provideShopRevisitStatusRepository(): ShopRevisitStatusRepository {
        return ShopRevisitStatusRepository(ShopRevisitStatusApi.create())
    }
}