package com.progdsmfsm.app.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.progdsmfsm.app.AppConstant

@Entity(tableName = AppConstant.SHOP_DEACTIVATE_RECORD)
class ShopDeactivateEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0

    @ColumnInfo(name = "shop_id")
    var shop_id: String? = null

    @ColumnInfo(name = "noti_id")
    var noti_id: String? = null

}