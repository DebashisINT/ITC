package com.breezefsmdsm.features.dashboard.presentation.api.dayStartEnd

import com.breezefsmdsm.features.stockCompetetorStock.api.AddCompStockApi
import com.breezefsmdsm.features.stockCompetetorStock.api.AddCompStockRepository

object DayStartEndRepoProvider {
    fun dayStartRepositiry(): DayStartEndRepository {
        return DayStartEndRepository(DayStartEndApi.create())
    }

}