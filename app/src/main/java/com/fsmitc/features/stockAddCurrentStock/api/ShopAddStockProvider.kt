package com.fsmitc.features.stockAddCurrentStock.api

import com.fsmitc.features.location.shopRevisitStatus.ShopRevisitStatusApi
import com.fsmitc.features.location.shopRevisitStatus.ShopRevisitStatusRepository

object ShopAddStockProvider {
    fun provideShopAddStockRepository(): ShopAddStockRepository {
        return ShopAddStockRepository(ShopAddStockApi.create())
    }
}