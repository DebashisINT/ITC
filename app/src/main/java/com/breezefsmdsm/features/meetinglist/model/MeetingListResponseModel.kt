package com.breezefsmdsm.features.meetinglist.model

import com.breezefsmdsm.base.BaseResponse
import com.breezefsmdsm.features.location.model.MeetingDurationDataModel
import java.io.Serializable

/**
 * Created by Saikat on 21-01-2020.
 */
class MeetingListResponseModel : BaseResponse(), Serializable {
    var meeting_list: ArrayList<MeetingDurationDataModel>? = null
}