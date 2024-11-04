package com.progdsmfsm.features.dashboard.presentation.api.dayStartEnd

import com.progdsmfsm.features.stockCompetetorStock.api.AddCompStockApi
import com.progdsmfsm.features.stockCompetetorStock.api.AddCompStockRepository

object DayStartEndRepoProvider {
    fun dayStartRepositiry(): DayStartEndRepository {
        return DayStartEndRepository(DayStartEndApi.create())
    }

}