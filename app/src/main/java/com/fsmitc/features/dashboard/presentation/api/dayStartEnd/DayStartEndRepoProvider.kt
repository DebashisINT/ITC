package com.fsmitc.features.dashboard.presentation.api.dayStartEnd

import com.fsmitc.features.stockCompetetorStock.api.AddCompStockApi
import com.fsmitc.features.stockCompetetorStock.api.AddCompStockRepository

object DayStartEndRepoProvider {
    fun dayStartRepositiry(): DayStartEndRepository {
        return DayStartEndRepository(DayStartEndApi.create())
    }

}