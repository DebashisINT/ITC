package com.demo.features.stockAddCurrentStock.api

import com.demo.features.location.shopRevisitStatus.ShopRevisitStatusApi
import com.demo.features.location.shopRevisitStatus.ShopRevisitStatusRepository

object ShopAddStockProvider {
    fun provideShopAddStockRepository(): ShopAddStockRepository {
        return ShopAddStockRepository(ShopAddStockApi.create())
    }
}