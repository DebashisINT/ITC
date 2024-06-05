package com.breezedsm.app.domain

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.breezedsm.app.AppConstant
import com.breezedsm.features.createOrder.DateWiseOrdReportFrag
import java.math.BigDecimal

@Dao
interface NewOrderDataDao {
    @Insert
    fun insert(vararg model: NewOrderDataEntity)

    @Query("Select * from new_order_data where order_date=:order_date ")
    fun getTodayOrder(order_date:String): List<NewOrderDataEntity>

    @Query("Select * from new_order_data where order_date=:order_date order by order_id desc")
    fun getTodayOrderOrderBy(order_date:String): List<NewOrderDataEntity>

    @Query("Select * from new_order_data where order_date=:order_date AND\n" +
            "shop_id in(select shop_id from shop_detail)\n" +
            " order by order_id desc")
    fun getTodayOrderOrderByShopMasterValidation(order_date:String): List<NewOrderDataEntity>

    @Query("Select * from new_order_data")
    fun getAllOrder(): List<NewOrderDataEntity>

    @Query("Select * from new_order_data where isUploaded=:isUploaded ")
    fun getUnsyncList(isUploaded:Boolean): List<NewOrderDataEntity>

    @Query("Select * from new_order_data where shop_id=:shop_id order by order_date desc,order_id desc")
    fun getOrderByShop(shop_id:String): List<NewOrderDataEntity>

    @Query("Select * from new_order_data where order_id=:order_id")
    fun getOrderByID(order_id:String): NewOrderDataEntity

    @Query("select \n" +
            "case when sum(order_total_amt) IS NULL then '0.00' else sum(order_total_amt) END as total\n" +
            " from new_order_data where shop_id = :shop_id")
    fun getOrderAmtByShop(shop_id:String): Double

    @Query("select \n" +
            "case when sum(order_total_amt) IS NULL then '0.00' else sum(order_total_amt) END as total\n" +
            " from new_order_data where order_date = :order_date")
    fun getOrderSumByDate(order_date:String): Double

    @Query("update new_order_data set isUploaded=:isUploaded where order_id=:order_id ")
    fun updateIsUploaded(order_id:String,isUploaded:Boolean)

    @Query("Select * from new_order_data order by order_date desc ,order_id desc")
    fun getAllOrderOrderBy(): List<NewOrderDataEntity>

    @Query(" Select * from new_order_data\n" +
            "where shop_id in(select shop_id from shop_detail) order by order_date desc ,order_id desc")
    fun getAllOrderOrderByShopMasterValidation(): List<NewOrderDataEntity>

    @Query("select * from new_order_data \n" +
            "where order_date between :fromD and :toD \n" +
            "order by order_date asc,order_time asc")
    fun getOrderDtlsDateWise(fromD:String,toD:String): List<NewOrderDataEntity>

    @Query("select distinct(order_date) from new_order_data \n" +
            "where order_date between :fromD and :toD \n" +
            "order by order_date asc,order_time asc")
    fun getDistinctOrdDates(fromD:String,toD:String): List<String>

    @Query("select case when sum(submitedQty) IS NULL then '0' ELSE \n" +
            " sum(submitedQty) end as qty from new_order_product where order_id = :order_id ")
    fun getQtySumByOrdID(order_id:String): String

    @Query("select order_date,shop_id,shop_name,sum(orderQtyTotal) as orderQtyTotal,cast(sum(orderValueTotal) as TEXT) as orderValueTotal\n" +
            "from(\n" +
            "select a.order_date,a.order_id,a.shop_id,a.shop_name,\n" +
            "(b.orderValueTotal) as orderValueTotal,(b.orderQtyTotal) as orderQtyTotal\n" +
            "from new_order_data a\n" +
            "inner join \n" +
            "(select order_id,shop_id,(submitedqty) as orderQtyTotal,(submitedqty*submitedSpecialRate) as orderValueTotal \n" +
            "from new_order_product) b \n" +
            "on a.order_id=b.order_id and a.shop_id = b.shop_id\n" +
            "where a.order_date between :fromD and :toD\n" +
            ") ab\n" +
            "group by ab.order_date,ab.shop_id order by order_date asc")
    fun getOrdReportByDt(fromD:String,toD:String): List<DateWiseOrdReportFrag.OrdReportDtlsQuery>

    @Query("\n" +
            "select shop_id,shop_name,orderQtyTotal,cast (orderValueTotal as TEXT) as orderValueTotal from (\n" +
            "select order_date,shop_id,shop_name,sum(orderQtyTotal) as orderQtyTotal,sum(orderValueTotal) as orderValueTotal\n" +
            "from(\n" +
            "select a.order_date,a.order_id,a.shop_id,a.shop_name,\n" +
            "(b.orderValueTotal) as orderValueTotal,(b.orderQtyTotal) as orderQtyTotal\n" +
            "from new_order_data a\n" +
            "inner join \n" +
            "(select order_id,shop_id,(submitedqty) as orderQtyTotal,(submitedqty*submitedSpecialRate) as orderValueTotal \n" +
            "from new_order_product) b \n" +
            "on a.order_id=b.order_id and a.shop_id = b.shop_id\n" +
            "where a.order_date between :speceficDt and :speceficDt\n" +
            ") ab\n" +
            "group by ab.order_date,ab.shop_id order by order_date asc\n" +
            ") where order_date = :speceficDt")
    fun getOrdReportBySingleDt(speceficDt:String): List<DateWiseOrdReportFrag.OrdReportDtls>

    @Query("update new_order_data set order_edit_date_time = :order_edit_date_time,order_total_amt=:order_total_amt," +
            " order_edit_remarks=:order_edit_remarks,isEdited=:isEdited \n" +
            "where order_id =:order_id ")
    fun updateOrdEdit(order_id:String,order_edit_date_time:String,order_total_amt:String,order_edit_remarks:String,isEdited:Boolean)

}