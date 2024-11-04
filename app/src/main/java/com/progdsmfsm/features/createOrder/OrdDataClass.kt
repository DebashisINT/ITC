package com.progdsmfsm.features.createOrder

import com.progdsmfsm.app.domain.NewProductListEntity
import com.progdsmfsm.app.domain.NewRateListEntity
import com.progdsmfsm.base.BaseResponse

open class ProductRateList(var product_id:String="",var product_name:String="",var brand_id:String="",var brand_name:String="",var UOM:String="",
                       var category_id:String="",var category_name:String = "",var watt_id:String="",var watt_name:String="",
    var mrp:String="",var item_price:String="",var specialRate:String="")

data class CommonProductCatagory(var id_sel:String , var name_sel :String)

//data class ProductRateListSelection(var submitedQty:String="-1",var submitedRate:String="0"):ProductRateList()

data class FinalProductRateSubmit(var product_id:String="",var product_name:String="", var submitedQty:String="",var submitedRate:String="",var mrp:String="",var item_price:String="",var total_amt:String="")

data class FinalData(var shop_id:String="",var product_list:ArrayList<FinalProductRateSubmit> = ArrayList())

data class GetProductReq(var product_list:ArrayList<NewProductListEntity> = ArrayList()):BaseResponse()
data class GetProductRateReq(var product_rate_list:ArrayList<NewRateListEntity> = ArrayList()):BaseResponse()

data class SyncOrdProductL(var order_id:String="", var product_id:String="", var product_name:String="", var submitedQty:Double=0.0,
                           var submitedSpecialRate:Double=0.0,var total_amt:Double=0.0,var mrp:Double = 0.0,var itemPrice:Double=0.0)

open class SyncOrd(var user_id:String="", var order_id:String="", var order_date:String="", var order_time:String="", var order_date_time:String="",
var shop_id:String="", var shop_name:String="", var shop_type:String="", var isInrange:Int=0, var order_lat:String="", var order_long:String="",
var shop_addr:String="", var shop_pincode:String="", var order_total_amt:Double=0.0, var order_remarks:String="",
                   var product_list:ArrayList<SyncOrdProductL> = ArrayList())

data class GetOrderHistory(var isUploaded :Boolean = false,var order_list:ArrayList<EditOrd>):BaseResponse()

data class DeleteOrd(var user_id:String="",var session_token:String="",var order_delete_list:ArrayList<OrdID> = ArrayList())
data class OrdID(var order_id:String="")

data class EditOrd(var order_edit_date_time:String="",var order_edit_remarks:String=""):SyncOrd()