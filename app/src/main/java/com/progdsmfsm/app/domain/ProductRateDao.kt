package com.progdsmfsm.app.domain

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.progdsmfsm.app.AppConstant

/**
 * Created by Saikat on 11-May-20.
 */
@Dao
interface ProductRateDao {

    @Insert
    fun insert(vararg productRate: ProductRateEntity)

    @Query("DELETE FROM " + AppConstant.PRODUCT_RATE_TABLE)
    fun deleteAll()

    @Query("SELECT * FROM " + AppConstant.PRODUCT_RATE_TABLE)
    fun getAll(): List<ProductRateEntity>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    abstract fun insertAll(kist: List<ProductRateEntity>)


}