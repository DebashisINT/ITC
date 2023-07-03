package com.breezefsmdsm.features.attendance.model

import com.breezefsmdsm.base.BaseResponse

class DayStartEndListResponse: BaseResponse() {
    var user_id:String?=null
    var user_name:String?=null
    var day_start_end_list:List<DayStartEndResponseData>?=null
}
data class DayStartEndResponseData(var dayStart_date_time:String,var dayEnd_date_time:String,var location_name:String,var isQualifiedAttendance:Int)
