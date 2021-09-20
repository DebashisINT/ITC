package com.breezefsmdsm.features.location.shopRevisitStatus

import com.breezefsmdsm.features.location.shopdurationapi.ShopDurationApi
import com.breezefsmdsm.features.location.shopdurationapi.ShopDurationRepository

object ShopRevisitStatusRepositoryProvider {
    fun provideShopRevisitStatusRepository(): ShopRevisitStatusRepository {
        return ShopRevisitStatusRepository(ShopRevisitStatusApi.create())
    }
}