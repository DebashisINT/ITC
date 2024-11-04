package com.progdsmfsm.app.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.progdsmfsm.app.AppConstant

@Entity(tableName = AppConstant.NEW_ORDER_DATA)
data class NewOrderDataEntity (
    @PrimaryKey(autoGenerate = true) var sl_no: Int = 0,
    @ColumnInfo var order_id:String = "",
    @ColumnInfo var order_date:String = "",
    @ColumnInfo var order_time:String = "",
    @ColumnInfo var order_date_time:String = "",
    @ColumnInfo var shop_id:String = "",
    @ColumnInfo var shop_name:String = "",
    @ColumnInfo var shop_type:String = "",
    @ColumnInfo  var isInrange:Int = 0,
    @ColumnInfo var order_lat:String = "",
    @ColumnInfo var order_long:String = "",
    @ColumnInfo var shop_addr:String = "",
    @ColumnInfo var shop_pincode:String = "",
    @ColumnInfo var order_total_amt:String = "",
    @ColumnInfo var order_remarks:String = "",
    @ColumnInfo var isUploaded:Boolean = false,

    @ColumnInfo(name = "order_edit_date_time", defaultValue = "") var order_edit_date_time:String = "",
    @ColumnInfo(name = "order_edit_remarks", defaultValue = "") var order_edit_remarks:String = "",
    @ColumnInfo(name = "isEdited", defaultValue = "0") var isEdited:Boolean = false,
    @ColumnInfo(name = "isDeleted", defaultValue = "0") var isDeleted:Boolean = false

)