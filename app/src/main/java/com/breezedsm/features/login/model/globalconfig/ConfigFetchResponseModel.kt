package com.breezedsm.features.login.model.globalconfig

import com.breezedsm.base.BaseResponse

/**
 * Created by Saikat on 14-01-2019.
 */
class ConfigFetchResponseModel : BaseResponse() {
    var min_accuracy: String? = "200"
    var max_accuracy: String? = "1500"
    var min_distance: String? = null
    var max_distance: String? = "1000"
    var idle_time: String? = null
    var willStockShow: Boolean? = null
    var maxFileSize: String? = "400"
    var willKnowYourStateShow: Boolean? = null
    var willAttachmentCompulsory: Boolean? = null
    var canAddBillingFromBillingList: Boolean? = null
    var willShowUpdateDayPlan: Boolean? = null
    var updateDayPlanText: String? = "Update Day Plan"
    var dailyPlanListHeaderText: String? = "List of Party"
    var allPlanListHeaderText: String? = "Plan/Achievement Details"
    var willSetYourTodaysTargetVisible: Boolean? = null
    var attendenceAlertHeading: String? = ""
    var attendenceAlertText: String? = ""
    var meetingText: String? = ""
    var meetingDistance: String? = ""
    var updateBillingText: String? = ""
    var isRateOnline: Boolean? = null
    var ppText: String = ""
    var ddText: String = ""
    /*var isReplaceShopText: Boolean? = null
    var isQuotationShow: Boolean? = null*/
    var shopText: String = ""
    var isCustomerFeatureEnable: Boolean? = null
    var isAreaVisible: Boolean? = null
    var cgstPercentage: String = ""
    var sgstPercentage: String = ""
    var tcsPercentage: String = ""
    var docAttachmentNo: String = ""
    var chatBotMsg: String = ""
    var contactMail: String = ""
    var isVoiceEnabledForAttendanceSubmit: Boolean? = null
    var isVoiceEnabledForOrderSaved: Boolean? = null
    var isVoiceEnabledForInvoiceSaved: Boolean? = null
    var isVoiceEnabledForCollectionSaved: Boolean? = null
    var isVoiceEnabledForHelpAndTipsInBot: Boolean? = null


    //From Hahnemann
    var isRevisitCaptureImage: Boolean? = null
    var isShowAllProduct: Boolean? = null
    var isPrimaryTargetMandatory: Boolean? = null
    var isStockAvailableForAll: Boolean? = null
    var isStockAvailableForPopup: Boolean? = null
    var isOrderAvailableForPopup: Boolean? = null
    var isCollectionAvailableForPopup: Boolean? = null
    var isDDFieldEnabled: Boolean? = null
    var isActivatePJPFeature: Boolean? = null
    var willReimbursementShow: Boolean? = null



    var GPSAlert: Boolean? = null

    var BatterySetting: Boolean? = null
    var PowerSaverSetting: Boolean? = null
    var Show_App_Logout_Notification: Boolean? = null

    var FaceRegistrationFrontCamera: Boolean? = null

    var IsShowMyDetails:Boolean? = null
    var IsAttendVisitShowInDashboard:Boolean? = null
    var IsShowInPortalManualPhotoRegn:Boolean? = null
    var IsShowHomeLocationMap:Boolean? = null

    var ShowApproxDistanceInNearbyShopList: Boolean? = null

    var IsnewleadtypeforRuby: Boolean? = null

    var IsUpdateVisitDataInTodayTable:Boolean?=null
    var ConsiderInactiveShopWhileLogin:Boolean?=null

    //begin mantis id 0027279 ShowPartyWithGeoFence functionality Puja 01-03-2024
    var ShowPartyWithGeoFence:Boolean? = false
    //end mantis id 0027279 ShowPartyWithGeoFence functionality Puja 01-03-2024

    //begin mantis id 0027285 ShowPartyWithCreateOrder functionality Puja 01-03-2024
    var ShowPartyWithCreateOrder:Boolean? = false
    //end mantis id 0027285 ShowPartyWithCreateOrder functionality Puja 01-03-2024

    var IsShowPrivacyPolicyInMenu:Boolean?=null// mantis 25912

}