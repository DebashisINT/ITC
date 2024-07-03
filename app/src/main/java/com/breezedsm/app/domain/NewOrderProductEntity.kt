package com.breezedsm.app.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.breezedsm.app.AppConstant

@Entity(tableName = AppConstant.NEW_ORDER_PRODUCT)
data class NewOrderProductEntity (
    @PrimaryKey(autoGenerate = true) var sl_no: Int = 0,
    @ColumnInfo var order_id:String = "",
    @ColumnInfo var product_id:String = "",
    @ColumnInfo var product_name:String = "",
    @ColumnInfo var submitedQty:String = "",
    @ColumnInfo var submitedSpecialRate:String = "",
    @ColumnInfo var shop_id:String = "",

    @ColumnInfo var total_amt:String = "",
    @ColumnInfo var mrp:String = "",
    @ColumnInfo var itemPrice:String = ""
)