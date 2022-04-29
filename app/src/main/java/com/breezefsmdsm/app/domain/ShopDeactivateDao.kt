package com.breezefsmdsm.app.domain

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ShopDeactivateDao {

    @Insert
    fun insert(obj: ShopDeactivateEntity)

    @Query("select * from shop_deactivate_record")
    fun getAll():List<ShopDeactivateEntity>

    @Query("delete from shop_deactivate_record where shop_id=:shop_id ")
    fun deleteByShopID(shop_id:String)

}