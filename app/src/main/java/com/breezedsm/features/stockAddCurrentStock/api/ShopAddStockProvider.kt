package com.breezedsm.features.stockAddCurrentStock.api

import com.breezedsm.features.location.shopRevisitStatus.ShopRevisitStatusApi
import com.breezedsm.features.location.shopRevisitStatus.ShopRevisitStatusRepository

object ShopAddStockProvider {
    fun provideShopAddStockRepository(): ShopAddStockRepository {
        return ShopAddStockRepository(ShopAddStockApi.create())
    }
}