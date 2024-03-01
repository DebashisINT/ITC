package com.breezefsmdsm.app

import com.breezefsmdsm.features.dashboard.presentation.DashboardFragment
import com.marcinmoskala.kotlinpreferences.PreferenceHolder

/**
 * Created by Pratishruti on 08-11-2017.
 */

object Pref : PreferenceHolder() {
    var text: String? by bindToPreferenceFieldNullable()
    var num: Int by bindToPreferenceField(0, "SomeIntKey")
    var login_time: String? by bindToPreferenceFieldNullable()
    var add_attendence_time: String? by bindToPreferenceFieldNullable()
    var logout_time: String? by bindToPreferenceFieldNullable()
    var appLaunchDate: String? by bindToPreferenceFieldNullable()
    var latitude: String? by bindToPreferenceFieldNullable()
    var longitude: String? by bindToPreferenceFieldNullable()
    var merediam: String? by bindToPreferenceFieldNullable()
    var prevTimeStamp: Long by bindToPreferenceField(0, "prevTimeStamp")
    var user_id: String? by bindToPreferenceFieldNullable()
    var isLocFuzedBroadPlaying: Boolean by bindToPreferenceField(false,"isLocFuzedBroadPlaying")
    var temp_user_id: String? by bindToPreferenceFieldNullable()
    var session_token: String? by bindToPreferenceFieldNullable()
    var login_date_time: String? by bindToPreferenceFieldNullable()
    var user_name: String? by bindToPreferenceFieldNullable()
    var isLogoutInitiated: Boolean by bindToPreferenceField(false, "SomeIntKey")
    var imei: String by bindToPreferenceField("", "imei")
    var totalTimeSpenAtShop: String by bindToPreferenceField("0", "totalTimeSpenAtShop")
    var user_profile_img: String by bindToPreferenceField("", "userProfileImg")
    var totalShopVisited: String by bindToPreferenceField("0", "totalShopVisited")
    var totalAttendance: String by bindToPreferenceField("0", "totalAttendance")
    var isAutoLogout: Boolean by bindToPreferenceField(false, "isAutoLogout")
    var login_date: String? by bindToPreferenceFieldNullable()
    var temp_login_date: String? by bindToPreferenceFieldNullable()
    var isLocationActivitySynced: Boolean by bindToPreferenceField(true, "isLocationActivitySynced")
    var prevShopActivityTimeStamp: Long by bindToPreferenceField(0, "prevTimeStamp")
    var prevEnsShopDurationTimeStamp: Long by bindToPreferenceField(0, "prevTimeStamp")
    var prevLocationActivityTimeStamp: Long by bindToPreferenceField(0, "prevLocTimeStamp")
    var prevRevisitGarbageTimeStamp: Long by bindToPreferenceField(0, "prevLocTimeStamp")
    var prevIdealLocationActivityTimeStamp: Long by bindToPreferenceField(0, "prevIdealLocTimeStamp")
    var prevShopActivityTimeStampMonitorService: Long by bindToPreferenceField(0, "prevTimeStampMonitor")
    var prevShopDurationTimeStamp: Long by bindToPreferenceField(0, "prevShopDurationTimeStamp")
    var prevMeetingDurationTimeStamp: Long by bindToPreferenceField(0, "prevMeetingDurationTimeStamp")
    var prevLocNotiDurationTimeStamp: Long by bindToPreferenceField(0, "prevLocNotiDurationTimeStamp")
    var prevHomeLocReasonTimeStamp: Long by bindToPreferenceField(0, "prevHomeLocReasonTimeStamp")
    var prevBatNetSaveTimeStamp: Long by bindToPreferenceField(0, "prevBatNetSaveTimeStamp")
    var prevBatNetSyncTimeStamp: Long by bindToPreferenceField(0, "prevBatNetSyncTimeStamp")
    var isLoginInitiated: Boolean by bindToPreferenceField(false, "isLoginInitiated")
    var profile_img: String by bindToPreferenceField("", "profile_img")
    var profile_state: String by bindToPreferenceField("", "profile_state")
    var profile_city: String by bindToPreferenceField("", "profile_city")
    var profile_pincode: String by bindToPreferenceField("", "profile_pincode")
    var profile_address: String by bindToPreferenceField("", "profile_address")
    var profile_country: String by bindToPreferenceField("", "profile_country")
    var isProfileUpdated: Boolean by bindToPreferenceField(false, "isProfileUpdated")
    var isGeoFenceAdded: Boolean by bindToPreferenceField(false, "isGeoFenceAdded")
    var isMarketingImgSynched: Boolean by bindToPreferenceField(false, "isMarketingImgSynched")
    var current_latitude: String by bindToPreferenceField("", "current_latitude")
    var current_longitude: String by bindToPreferenceField("", "current_longitude")
    var isAddAttendence: Boolean by bindToPreferenceField(false, "isAddAttendence")
    var gpsAccuracy: String by bindToPreferenceField("100", "gpsAccuracy")
    var source_latitude: String by bindToPreferenceField("", "source_latitude")
    var source_longitude: String by bindToPreferenceField("", "source_longitude")
    var isOnLeave: String by bindToPreferenceField("", "isOnLeave")
    var logout_latitude: String by bindToPreferenceField("0.0", "logout_latitude")
    var logout_longitude: String by bindToPreferenceField("0.0", "logout_longitude")
    var totalS2SDistance: String by bindToPreferenceField("0.0", "totalS2SDistance")
    var tempDistance: String by bindToPreferenceField("0.0", "tempDistance")
    var willAlarmTrigger: Boolean by bindToPreferenceField(false, "willAlarmTrigger")
    var isShopVisited: Boolean by bindToPreferenceField(false, "isShopVisited")
    var deviceToken: String by bindToPreferenceField("", "deviceToken")
    var isSeenTermsConditions: Boolean by bindToPreferenceField(false, "isSeenTermsConditions")
    var termsConditionsText: String by bindToPreferenceField("", "termsConditionsText")
    var prevOrderCollectionCheckTimeStamp: Long by bindToPreferenceField(0, "prevOrderCollectionCheckTimeStamp")
    var isHomeLocAvailable: Boolean by bindToPreferenceField(false, "isHomeLocAvailable")
    var approvedInTime: String by bindToPreferenceField("", "approvedInTime")
    var approvedOutTime: String by bindToPreferenceField("", "approvedOutTime")
    var home_latitude: String by bindToPreferenceField("", "home_latitude")
    var home_longitude: String by bindToPreferenceField("", "home_longitude")
    var isFieldWorkVisible: String by bindToPreferenceField("", "isFieldWorkVisible")
    var willStockShow: Boolean by bindToPreferenceField(false, "willStockShow")
    var maxFileSize: String by bindToPreferenceField("400", "maxFileSize")
    var willKnowYourStateShow: Boolean by bindToPreferenceField(false, "willKnowYourStateShow")
    var willAttachmentCompulsory: Boolean by bindToPreferenceField(true, "willAttachmentCompulsory")
    var canAddBillingFromBillingList: Boolean by bindToPreferenceField(true, "canAddBillingFromBillingList")
    var willShowUpdateDayPlan: Boolean by bindToPreferenceField(false, "willShowUpdateDayPlan")
    var updateDayPlanText: String by bindToPreferenceField("", "updateDayPlanText")
    var dailyPlanListHeaderText: String by bindToPreferenceField("", "dailyPlanListHeaderText")
    var allPlanListHeaderText: String by bindToPreferenceField("", "allPlanListHeaderText")
    var willSetYourTodaysTargetVisible: Boolean by bindToPreferenceField(false, "willSetYourTodaysTargetVisible")
    var attendenceAlertHeading: String by bindToPreferenceField("Attendance Confirmation", "attendenceAlertHeading")
    var attendenceAlertText: String by bindToPreferenceField("Do you want submit attendance?", "attendenceAlertText")
    var isRateNotEditable: Boolean by bindToPreferenceField(false, "isRateNotEditable")
    var isMeetingAvailable: Boolean by bindToPreferenceField(false, "isMeetingAvailable")
    var meetingText: String by bindToPreferenceField("Meeting", "meetingText")
    var meetingDistance: String by bindToPreferenceField("30", "meetingDistance")
    var willLeaveApprovalEnable: Boolean by bindToPreferenceField(false, "willLeaveApprovalEnable")
    var willReportShow: Boolean by bindToPreferenceField(false, "willReportShow")
    var isFingerPrintMandatoryForAttendance: Boolean by bindToPreferenceField(false, "isFingerPrintMandatoryForAttendance")
    var isFingerPrintMandatoryForVisit: Boolean by bindToPreferenceField(false, "isFingerPrintMandatoryForVisit")
    var isSelfieMandatoryForAttendance: Boolean by bindToPreferenceField(false, "isSelfieMandatoryForAttendance")
    var willAttendanceReportShow: Boolean by bindToPreferenceField(false, "willAttendanceReportShow")
    var willPerformanceReportShow: Boolean by bindToPreferenceField(false, "willPerformanceReportShow")
    var willVisitReportShow: Boolean by bindToPreferenceField(false, "willVisitReportShow")
    var attendance_text: String by bindToPreferenceField("10.30 AM", "attendance_text")
    var updateBillingText: String by bindToPreferenceField("Update Billing Details Within 3 Days", "updateBillingText")
    var willTimesheetShow: Boolean by bindToPreferenceField(false, "willTimesheetShow")
    var isAttendanceFeatureOnly: Boolean by bindToPreferenceField(false, "isAttendanceFeatureOnly")
    var isCollectioninMenuShow: Boolean by bindToPreferenceField(false, "iscollectioninMenuShow")
    var isVisitShow: Boolean by bindToPreferenceField(false, "isVisitShow")
    var isOrderShow: Boolean by bindToPreferenceField(false, "isOrderShow")
    var isShopAddEditAvailable: Boolean by bindToPreferenceField(false, "isShopAddEditAvailable")
    var isEntityCodeVisible: Boolean by bindToPreferenceField(false, "isEntityCodeVisible")
    var isRateOnline: Boolean by bindToPreferenceField(false, "isRateOnline")
    var isAreaMandatoryInPartyCreation: Boolean by bindToPreferenceField(false, "isAreaMandatoryInPartyCreation")
    var isShowPartyInAreaWiseTeam: Boolean by bindToPreferenceField(false, "isShowPartyInAreaWiseTeam")
    var isChangePasswordAllowed: Boolean by bindToPreferenceField(false, "isChangePasswordAllowed")
    var isHomeRestrictAttendance: String by bindToPreferenceField("0", "isHomeRestrictAttendance")
    var ppText: String by bindToPreferenceField("PP", "ppText")
    var ddText: String by bindToPreferenceField("DD", "ddText")
    var isReplaceShopText: Boolean by bindToPreferenceField(false, "isReplaceShopText")
    var isQuotationShow: Boolean by bindToPreferenceField(false, "isQuotationShow")
    var shopText: String by bindToPreferenceField("Shop", "shopText")
    var isCustomerFeatureEnable: Boolean by bindToPreferenceField(false, "isCustomerFeatureEnable")
    var isAreaVisible: Boolean by bindToPreferenceField(false, "isAreaVisible")
    var cgstPercentage: String by bindToPreferenceField("14", "cgstPercentage")
    var sgstPercentage: String by bindToPreferenceField("14", "sgstPercentage")
    var tcsPercentage: String by bindToPreferenceField("0.75", "tcsPercentage")
    var isQuotationPopupShow: Boolean by bindToPreferenceField(false, "isQuotationPopupShow")
    var homeLocDistance: String by bindToPreferenceField("50", "homeLocDistance")
    var shopLocAccuracy: String by bindToPreferenceField("500", "shopLocAccuracy")
    var isMultipleAttendanceSelection: Boolean by bindToPreferenceField(false, "isMultipleAttendanceSelection")
    var isOrderReplacedWithTeam: Boolean by bindToPreferenceField(false, "isOrderReplacedWithTeam")
    var isSefieAlarmed: Boolean by bindToPreferenceField(false, "isSefieAlarmed")
    var isDDShowForMeeting: Boolean by bindToPreferenceField(false, "isDDShowForMeeting")
    var isDDMandatoryForMeeting: Boolean by bindToPreferenceField(false, "isDDMandatoryForMeeting")
    var isOfflineTeam: Boolean by bindToPreferenceField(false, "isOfflineTeam")
    var supervisor_name: String by bindToPreferenceField("", "supervisor_name")
    var client_text: String by bindToPreferenceField("", "client_text")
    var product_text: String by bindToPreferenceField("", "product_text")
    var activity_text: String by bindToPreferenceField("", "activity_text")
    var project_text: String by bindToPreferenceField("", "project_text")
    var time_text: String by bindToPreferenceField("", "time_text")
    var comment_text: String by bindToPreferenceField("", "comment_text")
    var submit_text: String by bindToPreferenceField("", "submit_text")
    var timesheet_past_days: String by bindToPreferenceField("30", "timesheet_past_days")
    var isAllTeamAvailable: Boolean by bindToPreferenceField(false, "isAllTeamAvailable")
    var reportId: String by bindToPreferenceField("", "reportId")
    var isNextVisitDateMandatory: Boolean by bindToPreferenceField(false, "isNextVisitDateMandatory")
    var isRecordAudioEnable: Boolean by bindToPreferenceField(false, "isRecordAudioEnable")
    var isShowCurrentLocNotifiaction: Boolean by bindToPreferenceField(false, "isShowCurrentLocNotifiaction")
    var isUpdateWorkTypeEnable: Boolean by bindToPreferenceField(false, "isUpdateWorkTypeEnable")
    var isAchievementEnable: Boolean by bindToPreferenceField(false, "isAchievementEnable")
    var isTarVsAchvEnable: Boolean by bindToPreferenceField(false, "isTarVsAchvEnable")
    var isLeaveEnable: Boolean by bindToPreferenceField(false, "isLeaveEnable")
    var isOrderMailVisible: Boolean by bindToPreferenceField(false, "isOrderMailVisible")
    var isShopEditEnable: Boolean by bindToPreferenceField(false, "isShopEditEnable")
    var isClearData: Boolean by bindToPreferenceField(false, "isClearData")
    var isTaskEnable: Boolean by bindToPreferenceField(false, "isTaskEnable")
    var isAppInfoEnable: Boolean by bindToPreferenceField(false, "isAppInfoEnable")
    var appInfoMins: String by bindToPreferenceField("1", "appInfoMins")
    var autoRevisitDistance: String by bindToPreferenceField("120", "autoRevisitDistance")
    var autoRevisitTime: String by bindToPreferenceField("5", "autoRevisitTime")
    var willAutoRevisitEnable: Boolean by bindToPreferenceField(false, "willAutoRevisitEnable")
    var dynamicFormName: String by bindToPreferenceField("", "dynamicFormName")
    var willDynamicShow: Boolean by bindToPreferenceField(false, "willDynamicShow")
    var isOfflineShopSaved: Boolean by bindToPreferenceField(false, "isOfflineShopSaved")
    var willActivityShow: Boolean by bindToPreferenceField(false, "willActivityShow")
    var willMoreVisitUpdateCompulsory: Boolean by bindToPreferenceField(false, "willMoreVisitUpdateCompulsory")
    var willMoreVisitUpdateOptional: Boolean by bindToPreferenceField(true, "willMoreVisitUpdateOptional")
    var isRememberMe: Boolean by bindToPreferenceField(false, "isRememberMe")
    var PhnNo: String by bindToPreferenceField("", "PhnNo")
    var pwd: String by bindToPreferenceField("", "pwd")
    var docAttachmentNo: String by bindToPreferenceField("0", "docAttachmentNo")
    var isDocumentRepoShow: Boolean by bindToPreferenceField(false, "isDocumentRepoShow")
    var isChatBotShow: Boolean by bindToPreferenceField(false, "isChatBotShow")
    var isAttendanceBotShow: Boolean by bindToPreferenceField(false, "isAttendanceBotShow")
    var isVisitBotShow: Boolean by bindToPreferenceField(false, "isVisitBotShow")
    var chatBotMsg: String by bindToPreferenceField("", "chatBotMsg")
    var contactMail: String by bindToPreferenceField("", "contactMail")
    var isShowOrderRemarks: Boolean by bindToPreferenceField(false, "isShowOrderRemarks")
    var isShowOrderSignature: Boolean by bindToPreferenceField(false, "isShowOrderSignature")
    var isVoiceEnabledForAttendanceSubmit: Boolean by bindToPreferenceField(false, "isVoiceEnabledForAttendanceSubmit")
    var isVoiceEnabledForOrderSaved: Boolean by bindToPreferenceField(false, "isVoiceEnabledForOrderSaved")
    var isVoiceEnabledForInvoiceSaved: Boolean by bindToPreferenceField(false, "isVoiceEnabledForInvoiceSaved")
    var isVoiceEnabledForCollectionSaved: Boolean by bindToPreferenceField(false, "isVoiceEnabledForCollectionSaved")
    var isVoiceEnabledForHelpAndTipsInBot: Boolean by bindToPreferenceField(false, "isVoiceEnabledForHelpAndTipsInBot")
    var isShowSmsForParty: Boolean by bindToPreferenceField(false, "isShowSmsForParty")
    var isShowTimeline: Boolean by bindToPreferenceField(false, "isShowTimeline")
    var willScanVisitingCard: Boolean by bindToPreferenceField(false, "willScanVisitingCard")
    var isCreateQrCode: Boolean by bindToPreferenceField(false, "isCreateQrCode")
    var isScanQrForRevisit: Boolean by bindToPreferenceField(false, "isScanQrForRevisit")
    var willShowHomeLocReason: Boolean by bindToPreferenceField(false, "willShowHomeLocReason")
    var isShowLogoutReason: Boolean by bindToPreferenceField(false, "isShowLogoutReason")
    var isShowHomeLocReason: Boolean by bindToPreferenceField(false, "isShowHomeLocReason")
    var willShowShopVisitReason: Boolean by bindToPreferenceField(false, "willShowShopVisitReason")
    var minVisitDurationSpentTime: String by bindToPreferenceField("", "minVisitDurationSpentTime")
    var isShowShopVisitReason: Boolean by bindToPreferenceField(false, "isShowShopVisitReason")
    var durationCompletedShopId: String by bindToPreferenceField("", "durationCompletedShopId")
    var durationCompletedStartTimeStamp: String by bindToPreferenceField("", "durationCompletedStartTimeStamp")
    var willShowPartyStatus: Boolean by bindToPreferenceField(false, "willShowPartyStatus")
    var willShowEntityTypeforShop: Boolean by bindToPreferenceField(false, "willShowEntityTypeforShop")
    var isShowRetailerEntity: Boolean by bindToPreferenceField(false, "isShowRetailerEntity")
    var isShowDealerForDD: Boolean by bindToPreferenceField(false, "isShowDealerForDD")
    var isShowBeatGroup: Boolean by bindToPreferenceField(false, "isShowBeatGroup")
    var isShowShopBeatWise: Boolean by bindToPreferenceField(false, "isShowShopBeatWise")
    var isShowBankDetailsForShop: Boolean by bindToPreferenceField(false, "isShowBankDetailsForShop")
    var isShowOTPVerificationPopup: Boolean by bindToPreferenceField(false, "isShowOTPVerificationPopup")
    var locationTrackInterval: String by bindToPreferenceField("60", "locationTrackInterval")
    var isShowMicroLearning: Boolean by bindToPreferenceField(false, "isShowMicroLearning")
    var isLocationPermissionGranted: Boolean by bindToPreferenceField(false, "isLocationPermissionGranted")
    var homeLocReasonCheckMins: String by bindToPreferenceField("1", "homeLocReasonCheckMins")
    var homeLocStartTimeStamp: String by bindToPreferenceField("", "homeLocStartTimeStamp")
    var homeLocEndTimeStamp: String by bindToPreferenceField("", "homeLocEndTimeStamp")
    var currentLocationNotificationMins: String by bindToPreferenceField("1", "currentLocationNotificationMins")
    var isMultipleVisitEnable: Boolean by bindToPreferenceField(false, "isMultipleVisitEnable")
    var isShowVisitRemarks: Boolean by bindToPreferenceField(false, "isShowVisitRemarks")
    var isShowNearbyCustomer: Boolean by bindToPreferenceField(false, "isShowNearbyCustomer")
    var isServiceFeatureEnable: Boolean by bindToPreferenceField(false, "isServiceFeatureEnable")
    var isPatientDetailsShowInOrder: Boolean by bindToPreferenceField(false, "isPatientDetailsShowInOrder")
    var isPatientDetailsShowInCollection: Boolean by bindToPreferenceField(false, "isPatientDetailsShowInCollection")
    var isShopImageMandatory: Boolean by bindToPreferenceField(false, "isShopImageMandatory")

    //From Hahnemann
    var isRevisitCaptureImage: Boolean by bindToPreferenceField(false, "isRevisitCaptureImage")
    var isShowAllProduct: Boolean by bindToPreferenceField(false, "isShowAllProduct")
    var isPrimaryTargetMandatory: Boolean by bindToPreferenceField(false, "isPrimaryTargetMandatory")
    var isStockAvailableForAll: Boolean by bindToPreferenceField(false, "isStockAvailableForAll")
    var isStockAvailableForPopup: Boolean by bindToPreferenceField(true, "isStockAvailableForPopup")
    var isOrderAvailableForPopup: Boolean by bindToPreferenceField(true, "isOrderAvailableForPopup")
    var isCollectionAvailableForPopup: Boolean by bindToPreferenceField(true, "isCollectionAvailableForPopup")
    var isDDFieldEnabled: Boolean by bindToPreferenceField(false, "isDDFieldEnabled")
    var isActivatePJPFeature: Boolean by bindToPreferenceField(false, "isActivatePJPFeature")
    var willShowTeamDetails: Boolean by bindToPreferenceField(false, "willShowTeamDetails")
    var isAllowPJPUpdateForTeam: Boolean by bindToPreferenceField(false, "isAllowPJPUpdateForTeam")
    var willReimbursementShow: Boolean by bindToPreferenceField(false, "willReimbursementShow")
    var isVisitPlanMandatory: Boolean by bindToPreferenceField(false, "isVisitPlanMandatory")
    var isVisitPlanShow: Boolean by bindToPreferenceField(false, "isVisitPlanShow")
    var isAttendanceDistanceShow: Boolean by bindToPreferenceField(false, "isAttendanceDistanceShow")
    var visitDistance: String by bindToPreferenceField("", "visitDistance")
    var willTimelineWithFixedLocationShow: Boolean by bindToPreferenceField(false, "willTimelineWithFixedLocationShow")
    var distributorName: String by bindToPreferenceField("", "distributorName")
    var marketWorked: String by bindToPreferenceField("", "marketWorked")


    var isCompetitorImgEnable: Boolean by bindToPreferenceField(false, "IsCompetitorenable")
    var isOrderStatusRequired: Boolean by bindToPreferenceField(false, "IsOrderStatusRequired")
    var isCurrentStockEnable: Boolean by bindToPreferenceField(false, "IsCurrentStockEnable")
    var IsCurrentStockApplicableforAll: Boolean by bindToPreferenceField(false, "IsCurrentStockApplicableforAll")
    var IscompetitorStockRequired: Boolean by bindToPreferenceField(false, "IscompetitorStockRequired")
    var IsCompetitorStockforParty: Boolean by bindToPreferenceField(false, "IsCompetitorStockforParty")
    var IsFaceDetectionOn: Boolean by bindToPreferenceField(false, "IsFaceDetectionOn")
    var IsFaceDetection: Boolean by bindToPreferenceField(false, "IsFaceDetection")
    var IsFaceDetectionWithCaptcha: Boolean by bindToPreferenceField(false, "IsFaceDetectionWithCaptcha")
    var IsScreenRecorderEnable: Boolean by bindToPreferenceField(false, "IsScreenRecorderEnable")
    //var IsFromPortal: Boolean by bindToPreferenceField(false, "IsFromPortal")
    var IsFromPortal: Boolean by bindToPreferenceField(false, "IsDocRepoFromPortal")
    var IsDocRepShareDownloadAllowed: Boolean by bindToPreferenceField(false, "IsDocRepShareDownloadAllowed")



    var IsShowMenuAddAttendance: Boolean by bindToPreferenceField(false, "IsShowMenuAddAttendance")
    var IsShowMenuAttendance: Boolean by bindToPreferenceField(false, "IsShowMenuAttendance")
    var IsShowMenuMIS_Report: Boolean by bindToPreferenceField(false, "IsShowMenuMIS_Report")
    var IsShowMenuAnyDesk: Boolean by bindToPreferenceField(false, "IsShowMenuAnyDesk")
    var IsShowMenuPermission_Info: Boolean by bindToPreferenceField(false, "IsShowMenuPermission_Info")
    var IsShowMenuScan_QR_Code: Boolean by bindToPreferenceField(false, "IsShowMenuScan_QR_Code")
    var IsShowMenuChat: Boolean by bindToPreferenceField(false, "IsShowMenuChat")
    var IsShowMenuWeather_Details: Boolean by bindToPreferenceField(false, "IsShowMenuWeather_Details")
    var IsShowMenuHome_Location: Boolean by bindToPreferenceField(false, "IsShowMenuHome_Location")
    var IsShowMenuShare_Location: Boolean by bindToPreferenceField(false, "IsShowMenuShare_Location")
    var IsShowMenuMap_View: Boolean by bindToPreferenceField(false, "IsShowMenuMap_View")
    var IsShowMenuReimbursement: Boolean by bindToPreferenceField(false, "IsShowMenuReimbursement")
    var IsShowMenuOutstanding_Details_PP_DD: Boolean by bindToPreferenceField(false, "IsShowMenuOutstanding_Details_PP_DD")
    var IsShowMenuStock_Details_PP_DD: Boolean by bindToPreferenceField(false, "IsShowMenuStock_Details_PP_DD")



    var IsLeavePressed: Boolean by bindToPreferenceField(false, "IsLeavePressed")// local
    var IsLeaveGPSTrack: Boolean by bindToPreferenceField(false, "IsLeaveGPSTrack")
    var IsShowActivitiesInTeam: Boolean by bindToPreferenceField(false, "IsShowActivitiesInTeam")
    var IsMyJobFromTeam: Boolean by bindToPreferenceField(false, "IsMyJobFromTeam")// local

    var IsShowPartyOnAppDashboard: Boolean by bindToPreferenceField(false, "IsShowPartyOnAppDashboard")
    var IsShowAttendanceOnAppDashboard: Boolean by bindToPreferenceField(false, "IsShowAttendanceOnAppDashboard")
    var IsShowTotalVisitsOnAppDashboard: Boolean by bindToPreferenceField(false, "IsShowTotalVisitsOnAppDashboard")
    var IsShowVisitDurationOnAppDashboard: Boolean by bindToPreferenceField(false, "IsShowVisitDurationOnAppDashboard")


    var IsShowDayStart: Boolean by bindToPreferenceField(false, "IsShowDayStart")
    var IsshowDayStartSelfie: Boolean by bindToPreferenceField(false, "IsshowDayStartSelfie")
    var IsShowDayEnd: Boolean by bindToPreferenceField(false, "IsShowDayEnd")
    var IsshowDayEndSelfie: Boolean by bindToPreferenceField(false, "IsshowDayEndSelfie")

    var IsShowLeaveInAttendance: Boolean by bindToPreferenceField(false, "IsShowLeaveInAttendance")


    var DayStartMarked: Boolean by bindToPreferenceField(false, "DayStartMarked")
    var DayEndMarked: Boolean by bindToPreferenceField(false, "DayEndMarked")
    var DayStartShopType: String by bindToPreferenceField("", "DayStartShopType")
    var DayStartShopID: String by bindToPreferenceField("", "DayStartShopID")


    //19-08-21
    var IsShowMarkDistVisitOnDshbrd: Boolean by bindToPreferenceField(false, "IsShowMarkDistVisitOnDshbrd")
    var IsDDvistedOnceByDay: Boolean by bindToPreferenceField(false, "IsMyDDVisit")
    var visit_distributor_id: String by bindToPreferenceField("", "visit_distributor_id")
    var visit_distributor_name: String by bindToPreferenceField("", "visit_distributor_name")
    var visit_distributor_date_time: String by bindToPreferenceField("", "visit_distributor_date_time")

    var IsPhotoDeleteShow: Boolean by bindToPreferenceField(false, "IsPhotoDeleteShow")


    var GPSAlertGlobal: Boolean by bindToPreferenceField(false, "GPSAlertGlobal")
    var GPSAlert: Boolean by bindToPreferenceField(false, "GPSAlert")
    var GPSAlertwithSound: Boolean by bindToPreferenceField(false, "GPSAlertwithSound")

    var IsTeamAttendance: Boolean by bindToPreferenceField(false, "IsTeamAttendance")/*29-10-2021 Team Attendance*/


    /*24-11-2021 ITC face And Distributoraccu*/
    var FaceDetectionAccuracyUpper: String by bindToPreferenceField("0.93", "FaceDetectionAccuracyUpper")
    var FaceDetectionAccuracyLower: String by bindToPreferenceField("0.13", "FaceDetectionAccuracyLower")
    var DistributorGPSAccuracy: String by bindToPreferenceField("500", "DistributorGPSAccuracy")



    var BatterySettingGlobal: Boolean by bindToPreferenceField(false, "BatterySettingGlobal")
    var PowerSaverSettingGlobal: Boolean by bindToPreferenceField(false, "PowerSaverSettingGlobal")
    var Show_App_Logout_Notification_Global: Boolean by bindToPreferenceField(false, "Show_App_Logout_Notification_Global")
    var Show_App_Logout_Notification: Boolean by bindToPreferenceField(false, "Show_App_Logout_Notification")

    var BatterySetting: Boolean by bindToPreferenceField(false, "BatterySetting")
    var PowerSaverSetting: Boolean by bindToPreferenceField(false, "PowerSaverSetting")
    var IsShowTypeInRegistration: Boolean by bindToPreferenceField(false, "IsShowTypeInRegistration")

    var FaceRegistrationOpenFrontCamera: Boolean by bindToPreferenceField(false, "FaceRegistrationOpenFrontCamera")
    var FaceRegistrationFrontCamera: Boolean by bindToPreferenceField(false, "FaceRegistrationFrontCamera")

    var IsShowMenuShops: Boolean by bindToPreferenceField(false, "IsShowMenuShops")

    var IsShowMyDetailsGlobal: Boolean by bindToPreferenceField(false, "IsShowMyDetailsGlobal")
    var IsShowMyDetails: Boolean by bindToPreferenceField(false, "IsShowMyDetails")

    var IsAttendVisitShowInDashboardGlobal: Boolean by bindToPreferenceField(false, "IsAttendVisitShowInDashboardGlobal")
    var IsAttendVisitShowInDashboard: Boolean by bindToPreferenceField(false, "IsAttendVisitShowInDashboard")
    var IsShowInPortalManualPhotoRegn: Boolean by bindToPreferenceField(false, "IsShowInPortalManualPhotoRegn")
    var IsShowManualPhotoRegnInApp: Boolean by bindToPreferenceField(false, "IsShowManualPhotoRegnInApp")

    var IsDayEndBackPressedRestrict: Boolean by bindToPreferenceField(false, "IsDayEndBackPressedRestrict")
    var IsIMEICheck: Boolean by bindToPreferenceField(false, "IsIMEICheck")
    var UpdateUserName: Boolean by bindToPreferenceField(false, "UpdateUserName")
    var MarkAttendNotification: Boolean by bindToPreferenceField(false, "MarkAttendNotification")

    var IsAllowClickForPhotoRegister: Boolean by bindToPreferenceField(false, "IsAllowClickForPhotoRegister")
    var IsAllowClickForVisit: Boolean by bindToPreferenceField(false, "IsAllowClickForVisit")

    var user_login_ID: String by bindToPreferenceField("", "user_login_ID")

    var ShowAutoRevisitInDashboard: Boolean by bindToPreferenceField(false, "ShowAutoRevisitInDashboard")
    var ShowAutoRevisitInAppMenu: Boolean by bindToPreferenceField(false, "ShowAutoRevisitInAppMenu")

    var IsRestrictNearbyGeofence: Boolean by bindToPreferenceField(false, "IsRestrictNearbyGeofence")
    var GeofencingRelaxationinMeter: Int by bindToPreferenceField(100, "GeofencingRelaxationinMeter")

    var IsShowNearByTeam: Boolean by bindToPreferenceField(false, "IsShowNearByTeam")
    var AllowProfileUpdate: Boolean by bindToPreferenceField(false, "AllowProfileUpdate")
    var IsShowRevisitRemarksPopup: Boolean by bindToPreferenceField(false, "IsShowRevisitRemarksPopup")

    var IsAllowShopStatusUpdate: Boolean by bindToPreferenceField(false, "IsAllowShopStatusUpdate")

    var OfflineShopAccuracy: String by bindToPreferenceField("700", "OfflineShopAccuracy")
    var ShowTotalVisitAppMenu: Boolean by bindToPreferenceField(false, "ShowTotalVisitAppMenu")


    var autoRevisitTimeInSeconds: String by bindToPreferenceField("600", "autoRevisitTimeInSeconds")
    var PartyUpdateAddrMandatory: Boolean by bindToPreferenceField(false, "PartyUpdateAddrMandatory")
    var LogoutWithLogFile: Boolean by bindToPreferenceField(false, "LogoutWithLogFile")

    var approvedOutTimeServerFormat: String by bindToPreferenceField("0", "approvedOutTimeServerFormat")
    var IsShowHomeLocationMap: Boolean by bindToPreferenceField(true, "IsShowHomeLocationMap")
    var IsShowHomeLocationMapGlobal: Boolean by bindToPreferenceField(true, "IsShowHomeLocationMap")

    var isLocationHintPermissionGranted: Boolean by bindToPreferenceField(false, "isLocationHintPermissionGranted")
    var AutostartPermissionStatus: Boolean by bindToPreferenceField(false, "AutostartPermissionStatus")

    var WillRoomDBShareinLogin: Boolean by bindToPreferenceField(false, "WillRoomDBShareinLogin")
    var CommonAINotification: Boolean by bindToPreferenceField(false, "CommonAINotification")
    var IsFaceRecognitionOnEyeblink: Boolean by bindToPreferenceField(false, "IsFaceRecognitionOnEyeblink")

    var prevAttendanceNotiDurationTimeStamp: Long by bindToPreferenceField(0, "prevAttendanceNotiDurationTimeStamp")
    var prevRevisitGarbageDurationTimeStamp: Long by bindToPreferenceField(0, "prevRevisitGarbageDurationTimeStamp")

    var PowerSaverStatus: String by bindToPreferenceField("Off", "PowerSaverStatus")

    var GPSNetworkIntervalMins: String by bindToPreferenceField("0", "GPSNetworkIntervalMins")
    var prevGpsNetSyncTimeStamp: Long by bindToPreferenceField(0, "prevGpsNetSyncTimeStamp")
    var prevGpsNetSyncTimeStampService: Long by bindToPreferenceField(0, "prevGpsNetSyncTimeStampService")




    var isCalledFromStart: Boolean by bindToPreferenceField(false, "isCalledFromStart")
    var isStartCall: Boolean by bindToPreferenceField(false, "isStartCall")
    var isEndCall: Boolean by bindToPreferenceField(false, "isEndCall")

    var ShowApproxDistanceInNearbyShopList: Boolean by bindToPreferenceField(false, "ShowApproxDistanceInNearbyShopList")

    var IsnewleadtypeforRuby: Boolean by bindToPreferenceField(false, "IsnewleadtypeforRuby")

    var IsShowCalendar: Boolean by bindToPreferenceField(false, "IsShowCalendar")
    var IsShowCalculator: Boolean by bindToPreferenceField(false, "IsShowCalculator")
    var IsShowInactiveCustomer: Boolean by bindToPreferenceField(false, "IsShowInactiveCustomer")
    var IsShowUploadImageInAppProfile: Boolean by bindToPreferenceField(false, "IsShowUploadImageInAppProfile")
    var IsAttachQRFromProfile: Boolean by bindToPreferenceField(false, "IsAttachQRFromProfile")
    var IsShowMarketSpendTimer: Boolean by bindToPreferenceField(false, "IsShowMarketSpendTimer")
    var IsShowWorkType: Boolean by bindToPreferenceField(true, "IsShowWorkType")

    var DayStartTime: String by bindToPreferenceField("", "DayStartTime")


    var TimerSec: Int by bindToPreferenceField(0, "TimerSec")
    var TimerMin: Int by bindToPreferenceField(0, "TimerMin")
    var TimerHr: Int by bindToPreferenceField(0, "TimerHr")

    var IsUpdateVisitDataInTodayTable : Boolean by bindToPreferenceField(false, "IsUpdateVisitDataInTodayTable")

    var ConsiderInactiveShopWhileLogin : Boolean by bindToPreferenceField(false, "ConsiderInactiveShopWhileLogin")
    var IsShowAttendanceSummary : Boolean by bindToPreferenceField(false, "IsShowAttendanceSummary")

    var prevAttendanceSummaryOpenTimeStamp: Long by bindToPreferenceField(0, "prevAttendanceSummaryOpenTimeStamp")
    var prevAttendanceCalenderOpenTimeStamp: Long by bindToPreferenceField(0, "prevAttendanceCalenderOpenTimeStamp")
    var ShowPartyWithGeoFence: Boolean by bindToPreferenceField(false, "ShowPartyWithGeoFence")
    var ShowPartyWithCreateOrder: Boolean by bindToPreferenceField(false, "ShowPartyWithCreateOrder")

}