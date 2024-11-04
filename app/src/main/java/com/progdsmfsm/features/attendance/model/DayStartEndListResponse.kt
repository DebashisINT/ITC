package com.progdsmfsm.features.attendance.model

import com.progdsmfsm.base.BaseResponse

class DayStartEndListResponse: BaseResponse() {
    var user_id:String?=null
    var user_name:String?=null
    var day_start_end_list:List<DayStartEndResponseData>?=null
}
data class DayStartEndResponseData(var dayStart_date_time:String,var dayEnd_date_time:String,var location_name:String,var isQualifiedAttendance:Int)

data class AttendSummResponse(var status:String,var message:String,var total_work_day:Int,var total_present_day:Int,var total_absent_day:Int,
var total_qualified_day:Int)

