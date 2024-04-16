package com.breezedsm.app.domain

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NewOrderProductDao {
    @Insert
    fun insert(vararg model: NewOrderProductEntity)

    @Query("Select * from new_order_product where order_id=:order_id ")
    fun getProductsOrder(order_id:String): List<NewOrderProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    abstract fun insertAll(kist: List<NewOrderProductEntity>)
}