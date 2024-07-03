package com.breezedsm.app.domain

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.breezedsm.features.createOrder.FinalProductRateSubmit

@Dao
interface NewOrderProductDao {
    @Insert
    fun insert(vararg model: NewOrderProductEntity)

    @Query("Select * from new_order_product where order_id=:order_id ")
    fun getProductsOrder(order_id:String): List<NewOrderProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    abstract fun insertAll(kist: List<NewOrderProductEntity>)

/*    @Query("select product_id,product_name,submitedQty,submitedSpecialRate as submitedRate, \n" +
            "(select mrp from new_rate_list where new_rate_list.product_id = new_order_product.product_id) as mrp,\n" +
            "(select item_price from new_rate_list where new_rate_list.product_id = new_order_product.product_id) as item_price , total_amt \n" +
            "from new_order_product\n" +
            "where order_id = :order_id")
    fun getCustomOrdProductL(order_id:String): List<FinalProductRateSubmit>*/

    @Query("select product_id,product_name,submitedQty,submitedSpecialRate as submitedRate,mrp,itemPrice as item_price,total_amt\n" +
            "from new_order_product where order_id = :order_id ")
    fun getCustomOrdProductL(order_id:String): List<FinalProductRateSubmit>


    @Query("delete from new_order_product where order_id = :order_id ")
    fun deleteProductByOrdID(order_id:String)

    @Query("select * from new_product_list where product_id in (:id_list) ")
    fun getProductExistance(id_list:List<String>):List<NewProductListEntity>

}