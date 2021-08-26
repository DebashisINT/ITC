package com.demo.features.dashboard.presentation.api.dayStartEnd

import com.demo.features.stockCompetetorStock.api.AddCompStockApi
import com.demo.features.stockCompetetorStock.api.AddCompStockRepository

object DayStartEndRepoProvider {
    fun dayStartRepositiry(): DayStartEndRepository {
        return DayStartEndRepository(DayStartEndApi.create())
    }

}