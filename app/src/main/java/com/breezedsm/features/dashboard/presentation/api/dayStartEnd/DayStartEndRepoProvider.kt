package com.breezedsm.features.dashboard.presentation.api.dayStartEnd

import com.breezedsm.features.stockCompetetorStock.api.AddCompStockApi
import com.breezedsm.features.stockCompetetorStock.api.AddCompStockRepository

object DayStartEndRepoProvider {
    fun dayStartRepositiry(): DayStartEndRepository {
        return DayStartEndRepository(DayStartEndApi.create())
    }

}