package com.progdsmfsm.features.nearbyshops.presentation

import com.progdsmfsm.app.domain.AddShopDBModelEntity

/**
 * Created by Pratishruti on 30-10-2017.
 */
interface NearByShopsListClickListener {
    fun OnNearByShopsListClick(position: Int)
    fun mapClick(position: Int)
    fun orderClick(position: Int)
    fun callClick(position: Int)
    fun syncClick(position: Int)
    fun updateLocClick(position: Int)
    fun onStockClick(position: Int)
    fun onUpdateStageClick(position: Int)
    fun onQuotationClick(position: Int)
    fun onActivityClick(position: Int)
    fun onShareClick(position: Int)
    fun onCollectionClick(position: Int)
    fun onWhatsAppClick(no: String)
    fun onSmsClick(no: String)
    fun onCreateQrClick(position: Int)
    fun onUpdatePartyStatusClick(position: Int)
    fun onUpdateBankDetailsClick(position: Int)
    fun onUpdateStatusClick(obj: AddShopDBModelEntity)
    fun createOrderClick(obj: AddShopDBModelEntity)

}