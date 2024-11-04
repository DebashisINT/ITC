package com.progdsmfsm.features.stockAddCurrentStock.api

import com.progdsmfsm.features.location.shopRevisitStatus.ShopRevisitStatusApi
import com.progdsmfsm.features.location.shopRevisitStatus.ShopRevisitStatusRepository

object ShopAddStockProvider {
    fun provideShopAddStockRepository(): ShopAddStockRepository {
        return ShopAddStockRepository(ShopAddStockApi.create())
    }
}