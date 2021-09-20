package com.breezefsmdsm.features.stockAddCurrentStock.api

import com.breezefsmdsm.features.location.shopRevisitStatus.ShopRevisitStatusApi
import com.breezefsmdsm.features.location.shopRevisitStatus.ShopRevisitStatusRepository

object ShopAddStockProvider {
    fun provideShopAddStockRepository(): ShopAddStockRepository {
        return ShopAddStockRepository(ShopAddStockApi.create())
    }
}