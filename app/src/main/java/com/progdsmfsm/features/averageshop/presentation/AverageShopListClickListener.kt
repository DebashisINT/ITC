package com.progdsmfsm.features.averageshop.presentation

import android.view.View

/**
 * Created by Pratishruti on 30-10-2017.
 */
interface AverageShopListClickListener {
    fun OnMenuClick(position: Int,view:View)
    fun OnItemClick(position: Int)
    fun onSyncClick(position: Int)
}