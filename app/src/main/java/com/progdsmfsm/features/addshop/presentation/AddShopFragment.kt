package com.progdsmfsm.features.addshop.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import com.google.android.material.textfield.TextInputLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder
import com.progdsmfsm.R
import com.progdsmfsm.app.AppDatabase
import com.progdsmfsm.app.NetworkConstant
import com.progdsmfsm.app.Pref
import com.progdsmfsm.app.domain.*
import com.progdsmfsm.app.types.FragType
import com.progdsmfsm.app.utils.*
import com.progdsmfsm.base.BaseResponse
import com.progdsmfsm.base.presentation.BaseActivity
import com.progdsmfsm.base.presentation.BaseFragment
import com.progdsmfsm.features.SearchLocation.locationInfoModel
import com.progdsmfsm.features.addAttendence.FingerprintDialog
import com.progdsmfsm.features.addshop.api.AddShopRepositoryProvider
import com.progdsmfsm.features.addshop.api.areaList.AreaListRepoProvider
import com.progdsmfsm.features.addshop.api.assignToPPList.AssignToPPListRepoProvider
import com.progdsmfsm.features.addshop.api.assignedToDDList.AssignToDDListRepoProvider
import com.progdsmfsm.features.addshop.api.typeList.TypeListRepoProvider
import com.progdsmfsm.features.addshop.model.*
import com.progdsmfsm.features.addshop.model.assigntoddlist.AssignToDDListResponseModel
import com.progdsmfsm.features.addshop.model.assigntopplist.AssignToPPListResponseModel
import com.progdsmfsm.features.commondialog.presentation.CommonDialog
import com.progdsmfsm.features.commondialog.presentation.CommonDialogClickListener
import com.progdsmfsm.features.commondialogsinglebtn.CommonDialogSingleBtn
import com.progdsmfsm.features.commondialogsinglebtn.OnDialogClickListener
import com.progdsmfsm.features.dashboard.presentation.DashboardActivity
import com.progdsmfsm.features.dashboard.presentation.ReasonDialog
import com.progdsmfsm.features.dashboard.presentation.ShopVerificationDialog
import com.progdsmfsm.features.dashboard.presentation.api.otpsentapi.OtpSentRepoProvider
import com.progdsmfsm.features.dashboard.presentation.api.otpverifyapi.OtpVerificationRepoProvider
import com.progdsmfsm.features.location.LocationWizard
import com.progdsmfsm.features.location.SingleShotLocationProvider
import com.progdsmfsm.features.location.UserLocationDataEntity
import com.progdsmfsm.features.location.model.ShopDurationRequest
import com.progdsmfsm.features.location.model.ShopDurationRequestData
import com.progdsmfsm.features.location.shopdurationapi.ShopDurationRepositoryProvider
import com.progdsmfsm.features.login.model.productlistmodel.ModelListResponse
import com.progdsmfsm.features.login.presentation.LoginActivity
import com.progdsmfsm.features.nearbyshops.api.ShopListRepositoryProvider
import com.progdsmfsm.features.nearbyshops.model.*
import com.progdsmfsm.features.shopdetail.presentation.api.EditShopRepoProvider
import com.progdsmfsm.widgets.AppCustomEditText
import com.progdsmfsm.widgets.AppCustomTextView
import timber.log.Timber
import com.squareup.picasso.Picasso
import com.themechangeapp.pickimage.PermissionHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_add_shop.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Pratishruti on 27-10-2017.
 */

// Rev 1.0 Suman 10-07-2023 IsnewShop in api+room mantis id 26537

class AddShopFragment : BaseFragment(), View.OnClickListener {
    private lateinit var captureShopImage: ImageView
    private lateinit var shopImage: RelativeLayout
    private lateinit var mContext: Context
    private lateinit var saveTV: AppCustomTextView
    private var imagePath: String = ""
    private var imagePathCompetitor: String = ""
    private var shopDataModel = AddShopDBModelEntity()

    private lateinit var shopName: AppCustomEditText
    private lateinit var shopAddress: AppCustomEditText
    private lateinit var shopPin: AppCustomEditText
    private lateinit var ownerName: AppCustomEditText
    private lateinit var ownerNumber: AppCustomEditText
    private lateinit var ownerEmail: AppCustomEditText
    private lateinit var shopLargeImg: ImageView
    lateinit var layer_image_vw_IMG: ImageView
    private lateinit var imageRL: RelativeLayout
    private lateinit var take_photo_tv: AppCustomTextView
    private lateinit var capture_shop_image_IV: ImageView
    private lateinit var shop_name_TL: TextInputLayout
    private lateinit var rl_assign_to_dd: RelativeLayout
    private lateinit var tv_assign_to_dd: AppCustomTextView

    private var shopLongitude: Double = 0.0
    private var shopLatitude: Double = 0.0
    private lateinit var shop_type_RL: RelativeLayout
    private lateinit var assign_to_rl: RelativeLayout
    private lateinit var mReceiverAddshop: BroadcastReceiver
    private lateinit var type_TV: AppCustomTextView
    private lateinit var shop_name_EDT: AppCustomEditText
    private lateinit var dob_EDT: AppCustomEditText
    private lateinit var date_aniverdary_EDT: AppCustomEditText
    private var isApiCall = false
    var myCalendar = Calendar.getInstance(Locale.ENGLISH)
    private lateinit var progress_wheel: com.pnikosis.materialishprogress.ProgressWheel
    private var dialog: AccuracyIssueDialog? = null
    //    val compositeDisposable: CompositeDisposable = CompositeDisposable()
    var addShop: AddShopRequest = AddShopRequest()
    var addShopData = AddShopRequestData()
    var isDOB = -1
    private lateinit var assign_to_tv: AppCustomTextView
    private lateinit var themeListPopupWindowAdapter: InflateThemeListPopupWindowAdapter
    private var state_list: List<StateListEntity>? = null
    private var assignedToDDId = ""
    private var assignedToPPId = ""
    private lateinit var rv_suggestion_shop_list: RecyclerView
    private var isGetLocation = -1
    private var mLocation: Location? = null

    private var fullAdd: String = "";
    private var pinCode: String = "";
    private var mLatitude: String = ""
    private var mLongitude: String = ""
    private var amount = ""

    private lateinit var rl_amount: RelativeLayout
    private lateinit var amount_EDT: AppCustomEditText
    private lateinit var feedback_EDT: AppCustomEditText
    private lateinit var rl_area: RelativeLayout
    private lateinit var tv_area: AppCustomTextView
    private lateinit var tv_area_asterisk_mark: AppCustomTextView
    private lateinit var tv_model: AppCustomTextView
    private lateinit var tv_primary_app: AppCustomTextView
    private lateinit var tv_secondary_app: AppCustomTextView
    private lateinit var tv_lead_type: AppCustomTextView
    private lateinit var tv_stage: AppCustomTextView
    private lateinit var tv_funnel_stage: AppCustomTextView
    private lateinit var booking_amount_EDT: AppCustomEditText
    private lateinit var iv_model_dropdown: ImageView
    private lateinit var iv_primary_app_dropdown: ImageView
    private lateinit var iv_secondary_app_dropdown: ImageView
    private lateinit var iv_lead_type_dropdown: ImageView
    private lateinit var iv_stage_dropdown: ImageView
    private lateinit var iv_funnel_stage_dropdown: ImageView
    private lateinit var ll_customer_view: LinearLayout
    private lateinit var rl_owner_name_main: RelativeLayout
    private lateinit var til_no: TextInputLayout
    private lateinit var til_mail: TextInputLayout
    private lateinit var rl_area_main: RelativeLayout
    private lateinit var tv_type: AppCustomTextView
    private lateinit var rl_type: RelativeLayout
    private lateinit var tv_dd_asterisk_mark: AppCustomTextView
    private lateinit var next_visit_date_EDT: AppCustomEditText
    private lateinit var tv_visit_date_asterisk_mark: AppCustomTextView
    private lateinit var rl_audio_record_date: RelativeLayout
    private lateinit var ll_competitor_image: LinearLayout
    private lateinit var audio_record_date_EDT: AppCustomEditText

    private lateinit var scroll_bar: NestedScrollView
    private lateinit var ll_doc_extra_info: LinearLayout
    private lateinit var attachment_EDT: AppCustomEditText
    private lateinit var et_specalization: AppCustomEditText
    private lateinit var et_patient_count: AppCustomEditText
    private lateinit var et_category: AppCustomEditText
    private lateinit var doc_family_mem_dob_EDT: AppCustomEditText
    private lateinit var doc_address_EDT: AppCustomEditText
    private lateinit var doc_pin_code_EDT: AppCustomEditText
    private lateinit var et_remarks: AppCustomEditText
    private lateinit var iv_yes: ImageView
    private lateinit var iv_no: ImageView
    private lateinit var chemist_name_EDT: AppCustomEditText
    private lateinit var chemist_address_EDT: AppCustomEditText
    private lateinit var chemist_pin_code_EDT: AppCustomEditText
    private lateinit var assistant_name_EDT: AppCustomEditText
    private lateinit var assistant_no_EDT: AppCustomEditText
    private lateinit var assistant_dob_EDT: AppCustomEditText
    private lateinit var assistant_date_aniverdary_EDT: AppCustomEditText
    private lateinit var assistant_family_mem_dob_EDT: AppCustomEditText
    private lateinit var ll_yes: LinearLayout
    private lateinit var ll_no: LinearLayout
    private lateinit var til_remarks: TextInputLayout
    private lateinit var add_shop_ll: LinearLayout
    private lateinit var tv_name_asterisk_mark: AppCustomTextView
    private lateinit var ll_extra_info: LinearLayout
    private lateinit var director_name_EDT: AppCustomEditText
    private lateinit var family_mem_dob_EDT: AppCustomEditText
    private lateinit var key_person_name_EDT: AppCustomEditText
    private lateinit var add_dob_EDT: AppCustomEditText
    private lateinit var add_date_aniverdary_EDT: AppCustomEditText
    private lateinit var key_person_no_EDT: AppCustomEditText
    private lateinit var til_name: TextInputLayout
    private lateinit var rl_entity_main: RelativeLayout
    private lateinit var tv_entity: AppCustomTextView
    private lateinit var rl_entity: RelativeLayout
    private lateinit var rl_party_main: RelativeLayout
    private lateinit var tv_party: AppCustomTextView
    private lateinit var rl_party: RelativeLayout
    private lateinit var rl_select_retailer: RelativeLayout
    private lateinit var tv_select_retailer: AppCustomTextView
    private lateinit var rl_select_dealer: RelativeLayout
    private lateinit var tv_select_dealer: AppCustomTextView
    private lateinit var rl_select_beat: RelativeLayout
    private lateinit var tv_select_beat: AppCustomTextView
    private lateinit var assign_to_shop_rl: RelativeLayout
    private lateinit var assign_to_shop_tv: AppCustomTextView

    private var fingerprintDialog: FingerprintDialog? = null
    private var areaId = ""
    private var modelId = ""
    private var primaryAppId = ""
    private var secondaryAppId = ""
    private var leadTypeId = ""
    private var stageId = ""
    private var funnelStageId = ""
    private var typeId = ""
    private var nextVisitDate = ""
    private var filePath = ""
    private var isDocDegree = -1
    private var degreeImgLink = ""
    private var reasonDialog: ReasonDialog? = null
    private var entityId = ""
    private var partyStatusId = ""
    private var retailerId = ""
    private var dealerId = ""
    private var beatId = ""
    private var assignedToShopId = ""
    private var actualAddress = ""

    var finalUniqKey:String ? = null

    //01-09-2021
    private lateinit var customer_name_EDT: AppCustomTextView
    private lateinit var rl_shop_name_root: RelativeLayout
    private lateinit var rl_address_root: RelativeLayout
    private lateinit var rl_pincode_root: RelativeLayout
    private lateinit var rl_owner_contact_root: RelativeLayout
    private var randTen:String=""


    private val mTess: TessOCR by lazy {
        TessOCR(mContext)
    }

    private var datapath = ""

    //    public lateinit var img_Uri: Uri

    val date = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        // TODO Auto-generated method stub
        myCalendar.set(Calendar.YEAR, year)
        myCalendar.set(Calendar.MONTH, monthOfYear)
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        updateLabel()
    }

    companion object {
        private var mlocationInfoModel: locationInfoModel? = null
        var isOrderEntryPressed:Boolean = false
        var isNewShop:Boolean = false
        var newShopID:String = ""
        fun getInstance(mObj: Any): AddShopFragment {
            val mAddShopFragment = AddShopFragment()
            if (mObj is locationInfoModel) {
                /*fullAdd = mObj.address
                pinCode = mObj.pinCode
                mLatitude = mObj.latitude
                mLongitude = mObj.longitude*/

                val bundle = Bundle()
                bundle.putString("address", mObj.address)
                bundle.putString("pinCode", mObj.pinCode)
                bundle.putString("latitude", mObj.latitude)
                bundle.putString("longitude", mObj.longitude)
                mAddShopFragment.arguments = bundle
            }
            return mAddShopFragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context

        try {
            if (!TextUtils.isEmpty(arguments?.getString("address")))
                fullAdd = arguments?.getString("address").toString()

            if (!TextUtils.isEmpty(arguments?.getString("pinCode")))
                pinCode = arguments?.getString("pinCode").toString()

            if (!TextUtils.isEmpty(arguments?.getString("latitude")))
                mLatitude = arguments?.getString("latitude").toString()

            if (!TextUtils.isEmpty(arguments?.getString("longitude")))
                mLongitude = arguments?.getString("longitude").toString()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_add_shop, container, false)
        isApiCall = true
        initView(view)
        initTextChangeListener()

        /*if (AppUtils.mLocation != null) {
            if (AppUtils.mLocation!!.accuracy <= 100) {
                getAddressFromLatLng(AppUtils.mLocation!!)
            } else {
                Timber.d("======Saved current location is inaccurate (Add Shop)========")
                normalGetLocFlow()
            }
        } else {
            Timber.d("=====Saved current location is null (Add Shop)======")
            normalGetLocFlow()
        }*/

        if (!TextUtils.isEmpty(mLatitude) && !TextUtils.isEmpty(mLongitude)) {
            val locationList = AppDatabase.getDBInstance()!!.userLocationDataDao().getLocationUpdateForADay(AppUtils.getCurrentDateForShopActi())

            val loc_distance = LocationWizard.getDistance(locationList[locationList.size - 1].latitude.toDouble(), locationList[locationList.size - 1].longitude.toDouble(),
                    mLatitude.toDouble(), mLongitude.toDouble())

            if (loc_distance > 50) {
                mLatitude = ""
                mLongitude = ""
                (mContext as DashboardActivity).showSnackMessage("Location is not valid")
            }
        }

        normalGetLocFlow()

        /*if (mLongitude == "" && mLatitude == "") {
            if (dialog == null) {
                dialog = AccuracyIssueDialog()
                dialog?.show((mContext as DashboardActivity).supportFragmentManager, "AccuracyIssueDialog")
            }
        }
        else {
            shopAddress.setText(fullAdd)
            shopPin.setText(pinCode)
        }*/


        return view
    }

    private fun normalGetLocFlow() {
        if (mLongitude == "" && mLatitude == "") {
            //getShopLatLong()
            /*if (dialog == null) {
                dialog = AccuracyIssueDialog()
                dialog?.show((mContext as DashboardActivity).supportFragmentManager, "AccuracyIssueDialog")
            } else {
                dialog?.dismissAllowingStateLoss()
                dialog?.show((mContext as DashboardActivity).supportFragmentManager, "AccuracyIssueDialog")

            }*/


            if (AppUtils.mLocation != null) {
                if (AppUtils.mLocation!!.accuracy <= 100) {
                    getAddressFromLatLng(AppUtils.mLocation!!)
                } else {
                    Timber.d("======Saved current location is inaccurate (Add Shop)========")
                    getShopLatLong()
                }
            } else {
                Timber.d("=====Saved current location is null (Add Shop)======")
                getShopLatLong()
            }


        } else {
            Timber.d("=====Get location from map (Add Shop)======")
            actualAddress = fullAdd
            shopAddress.setText(fullAdd)
            shopPin.setText(pinCode)
        }
    }

    private fun getShopLatLong() {
        progress_wheel.spin()
        SingleShotLocationProvider.requestSingleUpdate(mContext,
                object : SingleShotLocationProvider.LocationCallback {
                    override fun onStatusChanged(status: String) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onProviderEnabled(status: String) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onProviderDisabled(status: String) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onNewLocationAvailable(location: Location) {
                        if (isGetLocation == -1) {
                            isGetLocation = 0
                            progress_wheel.stopSpinning()
                            try {
                                if (location != null && location.accuracy > 100) {
                                    if (dialog == null) {
                                        dialog = AccuracyIssueDialog()
                                        dialog?.show((mContext as DashboardActivity).supportFragmentManager, "AccuracyIssueDialog")
                                    } else {
                                        dialog?.dismissAllowingStateLoss()
                                        dialog?.show((mContext as DashboardActivity).supportFragmentManager, "AccuracyIssueDialog")

                                    }
                                    return
                                }
                                /*shopAddress.setText(LocationWizard.getLocationName(mContext, location.latitude, location.longitude))
                                shopPin.setText(LocationWizard.getPostalCode(mContext, location.latitude, location.longitude))*/

                                getAddressFromLatLng(location)

                            } catch (e: Exception) {
                                shopAddress.setText("")
                            }
                        }
                        /*else
                            isGetLocation = -1*/
                    }
                })

        val t = Timer()
        t.schedule(object : TimerTask() {
            override fun run() {
                try {
                    if (isGetLocation == -1) {
                        isGetLocation = 1
                        progress_wheel.stopSpinning()
                        dialog = AccuracyIssueDialog()
                        dialog?.show((mContext as DashboardActivity).supportFragmentManager, "AccuracyIssueDialog")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, 15000)
    }

    private fun getAddressFromLatLng(location: Location) {
        //22.6068776, 88.4898951
        mLocation = location
        var address = LocationWizard.getAdressFromLatlng(mContext, location.latitude, location.longitude)
        Timber.e("Shop address (Add Shop)======> $address")

        if (address.contains("http"))
            address = "Unknown"

        actualAddress = address
        shopAddress.setText(address)
        shopPin.setText(LocationWizard.getPostalCode(mContext, location.latitude, location.longitude))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView(view: View) {
        assign_to_tv = view.findViewById(R.id.assign_to_tv)
        captureShopImage = view.findViewById(R.id.capture_shop_image_IV)
        shopImage = view.findViewById(R.id.shop_image_RL)
        saveTV = view.findViewById(R.id.save_TV)
        progress_wheel = view.findViewById(R.id.progress_wheel)
        progress_wheel.stopSpinning()
        shopName = view.findViewById(R.id.shop_name_EDT)
        shopAddress = view.findViewById(R.id.address_EDT)
        shopPin = view.findViewById(R.id.pin_code_EDT)
        ownerName = view.findViewById(R.id.ownername_EDT)
        ownerNumber = view.findViewById(R.id.ownernumber_EDT)
        ownerEmail = view.findViewById(R.id.owneremail_EDT)
        shopLargeImg = view.findViewById(R.id.shop_large_IMG)
        layer_image_vw_IMG = view.findViewById(R.id.layer_image_vw_IMG)
        imageRL = view.findViewById(R.id.shop_image_RL)
        take_photo_tv = view.findViewById(R.id.take_photo_tv)
        capture_shop_image_IV = view.findViewById(R.id.capture_shop_image_IV)
        shop_type_RL = view.findViewById(R.id.shop_type_RL)
        assign_to_rl = view.findViewById(R.id.assign_to_rl)
        type_TV = view.findViewById(R.id.type_TV)
        shop_name_EDT = view.findViewById(R.id.shop_name_EDT)
        dob_EDT = view.findViewById(R.id.dob_EDT)
        date_aniverdary_EDT = view.findViewById(R.id.date_aniverdary_EDT)
        dob_EDT.setOnClickListener(this)
        date_aniverdary_EDT.setOnClickListener(this)
        shop_name_TL = view.findViewById(R.id.shop_name_TL)
        rl_assign_to_dd = view.findViewById(R.id.rl_assign_to_dd)
        tv_assign_to_dd = view.findViewById(R.id.tv_assign_to_dd)
        rl_amount = view.findViewById(R.id.rl_amount)
        amount_EDT = view.findViewById(R.id.amount_EDT)
        rv_suggestion_shop_list = view.findViewById(R.id.rv_suggestion_shop_list)
        rv_suggestion_shop_list.layoutManager = LinearLayoutManager(mContext)
        feedback_EDT = view.findViewById(R.id.feedback_EDT)
        rl_area = view.findViewById(R.id.rl_area)
        tv_area = view.findViewById(R.id.tv_area)
        tv_area_asterisk_mark = view.findViewById(R.id.tv_area_asterisk_mark)
        tv_model = view.findViewById(R.id.tv_model)
        tv_primary_app = view.findViewById(R.id.tv_primary_app)
        tv_secondary_app = view.findViewById(R.id.tv_secondary_app)
        tv_lead_type = view.findViewById(R.id.tv_lead_type)
        tv_stage = view.findViewById(R.id.tv_stage)
        tv_funnel_stage = view.findViewById(R.id.tv_funnel_stage)
        booking_amount_EDT = view.findViewById(R.id.booking_amount_EDT)
        iv_model_dropdown = view.findViewById(R.id.iv_model_dropdown)
        iv_primary_app_dropdown = view.findViewById(R.id.iv_primary_app_dropdown)
        iv_secondary_app_dropdown = view.findViewById(R.id.iv_secondary_app_dropdown)
        iv_lead_type_dropdown = view.findViewById(R.id.iv_lead_type_dropdown)
        iv_stage_dropdown = view.findViewById(R.id.iv_stage_dropdown)
        iv_funnel_stage_dropdown = view.findViewById(R.id.iv_funnel_stage_dropdown)
        ll_customer_view = view.findViewById(R.id.ll_customer_view)
        rl_owner_name_main = view.findViewById(R.id.rl_owner_name_main)
        til_mail = view.findViewById(R.id.til_mail)
        til_no = view.findViewById(R.id.til_no)
        rl_area_main = view.findViewById(R.id.rl_area_main)
        tv_type = view.findViewById(R.id.tv_type)
        rl_type = view.findViewById(R.id.rl_type)
        tv_dd_asterisk_mark = view.findViewById(R.id.tv_dd_asterisk_mark)
        next_visit_date_EDT = view.findViewById(R.id.next_visit_date_EDT)
        tv_visit_date_asterisk_mark = view.findViewById(R.id.tv_visit_date_asterisk_mark)
        rl_audio_record_date = view.findViewById(R.id.rl_audio_record_date)
        ll_competitor_image = view.findViewById(R.id.ll_competitor_image)
        audio_record_date_EDT = view.findViewById(R.id.audio_record_date_EDT)
        ll_extra_info = view.findViewById(R.id.ll_extra_info)
        director_name_EDT = view.findViewById(R.id.director_name_EDT)
        family_mem_dob_EDT = view.findViewById(R.id.family_mem_dob_EDT)
        key_person_name_EDT = view.findViewById(R.id.key_person_name_EDT)
        add_dob_EDT = view.findViewById(R.id.add_dob_EDT)
        add_date_aniverdary_EDT = view.findViewById(R.id.add_date_aniverdary_EDT)
        key_person_no_EDT = view.findViewById(R.id.key_person_no_EDT)
        scroll_bar = view.findViewById(R.id.scroll_bar)
        tv_name_asterisk_mark = view.findViewById(R.id.tv_name_asterisk_mark)
        ll_doc_extra_info = view.findViewById(R.id.ll_doc_extra_info)
        et_specalization = view.findViewById(R.id.et_specalization)
        et_patient_count = view.findViewById(R.id.et_patient_count)
        et_category = view.findViewById(R.id.et_category)
        doc_family_mem_dob_EDT = view.findViewById(R.id.doc_family_mem_dob_EDT)
        doc_address_EDT = view.findViewById(R.id.doc_address_EDT)
        doc_pin_code_EDT = view.findViewById(R.id.doc_pin_code_EDT)
        et_remarks = view.findViewById(R.id.et_remarks)
        iv_yes = view.findViewById(R.id.iv_yes)
        iv_no = view.findViewById(R.id.iv_no)
        chemist_name_EDT = view.findViewById(R.id.chemist_name_EDT)
        chemist_address_EDT = view.findViewById(R.id.chemist_address_EDT)
        chemist_pin_code_EDT = view.findViewById(R.id.chemist_pin_code_EDT)
        assistant_name_EDT = view.findViewById(R.id.assistant_name_EDT)
        assistant_no_EDT = view.findViewById(R.id.assistant_no_EDT)
        assistant_dob_EDT = view.findViewById(R.id.assistant_dob_EDT)
        assistant_date_aniverdary_EDT = view.findViewById(R.id.assistant_date_aniverdary_EDT)
        assistant_family_mem_dob_EDT = view.findViewById(R.id.assistant_family_mem_dob_EDT)
        ll_yes = view.findViewById(R.id.ll_yes)
        ll_no = view.findViewById(R.id.ll_no)
        attachment_EDT = view.findViewById(R.id.attachment_EDT)
        til_remarks = view.findViewById(R.id.til_remarks)
        add_shop_ll = view.findViewById(R.id.add_shop_ll)
        til_name = view.findViewById(R.id.til_name)
        rl_entity_main = view.findViewById(R.id.rl_entity_main)
        tv_entity = view.findViewById(R.id.tv_entity)
        rl_entity = view.findViewById(R.id.rl_entity)
        rl_party_main = view.findViewById(R.id.rl_party_main)
        tv_party = view.findViewById(R.id.tv_party)
        rl_party = view.findViewById(R.id.rl_party)
        rl_select_retailer = view.findViewById(R.id.rl_select_retailer)
        tv_select_retailer = view.findViewById(R.id.tv_select_retailer)
        rl_select_dealer = view.findViewById(R.id.rl_select_dealer)
        tv_select_dealer = view.findViewById(R.id.tv_select_dealer)
        rl_select_beat = view.findViewById(R.id.rl_select_beat)
        tv_select_beat = view.findViewById(R.id.tv_select_beat)
        assign_to_shop_rl = view.findViewById(R.id.assign_to_shop_rl)
        assign_to_shop_tv = view.findViewById(R.id.assign_to_shop_tv)

        //01-09-2021
        customer_name_EDT = view.findViewById(R.id.customer_name_EDT)
        rl_shop_name_root = view.findViewById(R.id.rl_shop_name_root)
        rl_address_root = view.findViewById(R.id.rl_address_root)
        rl_pincode_root = view.findViewById(R.id.rl_pincode_root)
        rl_owner_contact_root = view.findViewById(R.id.rl_owner_contact_root)
        //rl_shop_name_root.visibility=View.GONE  //22-09-2021 comment
        if(Pref.loginID.equals("manager",ignoreCase = true)){
            rl_address_root.visibility=View.VISIBLE
            rl_pincode_root.visibility=View.VISIBLE
        }else{
            rl_address_root.visibility=View.GONE
            rl_pincode_root.visibility=View.GONE
        }

        //rl_owner_contact_root.visibility=View.GONE   //22-09-2021 comment


        randTen=Pref.user_id+AppUtils.getRandomNumber(5).toString()
        customer_name_EDT.text=randTen
        try{
            var assigDDList = AppDatabase.getDBInstance()?.ddListDao()?.getAll()
            if (assigDDList != null) {
                if (assigDDList != null || assigDDList.size > 0){
                    var dis_id=assigDDList!!.get(0).dd_id
                    dis_id=dis_id!!.drop(6)
                    //shop_name_EDT.setText(dis_id.toString()+"_"+randTen) //22-09-2021  comment
                    //ownerNumber.setText(randTen)  //22-09-2021  comment
                }
            }
        }catch (ex:java.lang.Exception){ex.printStackTrace()}






        assign_to_shop_tv.hint = getString(R.string.assign_to_hint_text) + " ${Pref.shopText}"

        if (Pref.isShowBeatGroup)
            rl_select_beat.visibility = View.VISIBLE
        else
            rl_select_beat.visibility = View.GONE


        if (Pref.isNextVisitDateMandatory)
            tv_visit_date_asterisk_mark.visibility = View.VISIBLE
        else
            tv_visit_date_asterisk_mark.visibility = View.GONE

        if (Pref.isRecordAudioEnable)
            rl_audio_record_date.visibility = View.VISIBLE
        else
            rl_audio_record_date.visibility = View.GONE


        // force off for ITC 25-08-2021
       /* if(AppUtils.getSharedPreferenceslogCompetitorImgEnable(mContext))
            ll_competitor_image.visibility=View.VISIBLE
        else
            ll_competitor_image.visibility=View.GONE*/


        val typeList = AppDatabase.getDBInstance()?.shopTypeDao()?.getAll()
        if (typeList != null && typeList.isNotEmpty()) {
            type_TV.text = typeList[0].shoptype_name
            addShopData.type = typeList[0].shoptype_id
            shop_name_TL.hint = Pref.shopText + " name"
        }
        else {
            type_TV.text = ""
            addShopData.type = ""
        }

        val assignPPList = AppDatabase.getDBInstance()?.ppListDao()?.getAll()
        val assignDDList = AppDatabase.getDBInstance()?.ddListDao()?.getAll()

        if (Pref.isCustomerFeatureEnable) {
            ll_customer_view.visibility = View.VISIBLE
            rl_owner_name_main.visibility = View.GONE
            til_no.hint = getString(R.string.contact_number)
            til_mail.hint = getString(R.string.only_email)
            til_name.hint = getString(R.string.owner_name)
            rl_assign_to_dd.visibility = View.GONE
            assign_to_rl.visibility = View.GONE
            rl_amount.visibility = View.GONE
            assign_to_tv.hint = "Assigned to " + Pref.ppText
            rl_entity_main.visibility = View.GONE
            rl_select_retailer.visibility = View.GONE
            rl_select_dealer.visibility = View.GONE
            assign_to_shop_rl.visibility = View.GONE
        }
        else {
            ll_customer_view.visibility = View.GONE
            //rl_owner_name_main.visibility = View.VISIBLE  // force off for ITC 25-08-2021

            when (addShopData.type) {
                "1" -> {
                    //rl_assign_to_dd.visibility = View.VISIBLE    // force off for ITC 25-08-2021
                    //assign_to_rl.visibility = View.VISIBLE    // force off for ITC 25-08-2021
                    rl_amount.visibility = View.GONE
                    shopImage.visibility = View.VISIBLE
                    ll_doc_extra_info.visibility = View.GONE
                    ll_extra_info.visibility = View.GONE
                    assign_to_shop_rl.visibility = View.GONE
                    setMargin(false)
                    til_no.hint = getString(R.string.owner_contact_number)
                    til_mail.hint = getString(R.string.owner_email)
                    til_name.hint = getString(R.string.owner_name)
                    assign_to_tv.hint = "Assigned to " + Pref.ppText

                    if (Pref.willShowEntityTypeforShop)
                        rl_entity_main.visibility = View.VISIBLE
                    else
                        rl_entity_main.visibility = View.GONE

                    if (Pref.isShowRetailerEntity)
                        rl_select_retailer.visibility = View.VISIBLE
                    else
                        rl_select_retailer.visibility = View.GONE

                    if (Pref.isShowDealerForDD)
                        rl_select_dealer.visibility = View.VISIBLE
                    else {
                        rl_select_dealer.visibility = View.GONE

                        if(assignDDList != null && assignDDList.isNotEmpty()) {
                            assignedToDDId = assignDDList[0].dd_id!!
                            tv_assign_to_dd.text = assignDDList[0].dd_name
                        }
                    }

                    if(assignPPList != null && assignPPList.isNotEmpty()) {
                        assignedToPPId = assignPPList[0].pp_id!!
                        assign_to_tv.text = assignPPList[0].pp_name
                    }
                }
                "2" -> {
                    rl_assign_to_dd.visibility = View.GONE
                    assign_to_rl.visibility = View.GONE
                    rl_amount.visibility = View.GONE
                    shopImage.visibility = View.VISIBLE
                    ll_doc_extra_info.visibility = View.GONE
                    ll_extra_info.visibility = View.GONE
                    rl_entity_main.visibility = View.GONE
                    rl_select_retailer.visibility = View.GONE
                    rl_select_dealer.visibility = View.GONE
                    assign_to_shop_rl.visibility = View.GONE
                    setMargin(false)
                    til_no.hint = getString(R.string.owner_contact_number)
                    til_mail.hint = getString(R.string.owner_email)
                    til_name.hint = getString(R.string.owner_name)
                    assign_to_tv.hint = "Assigned to " + Pref.ppText
                }
                "3" -> {
                    rl_assign_to_dd.visibility = View.GONE
                    assign_to_rl.visibility = View.GONE
                    rl_amount.visibility = View.GONE
                    shopImage.visibility = View.VISIBLE
                    ll_doc_extra_info.visibility = View.GONE
                    ll_extra_info.visibility = View.GONE
                    rl_entity_main.visibility = View.GONE
                    rl_select_retailer.visibility = View.GONE
                    rl_select_dealer.visibility = View.GONE
                    assign_to_shop_rl.visibility = View.GONE
                    setMargin(false)
                    til_no.hint = getString(R.string.owner_contact_number)
                    til_mail.hint = getString(R.string.owner_email)
                    til_name.hint = getString(R.string.owner_name)
                    assign_to_tv.hint = "Assigned to " + Pref.ppText
                }
                "4", "12", "13", "14", "15" -> {
                    assign_to_rl.visibility = View.VISIBLE
                    rl_assign_to_dd.visibility = View.GONE
                    rl_amount.visibility = View.GONE
                    shopImage.visibility = View.VISIBLE
                    ll_doc_extra_info.visibility = View.GONE
                    ll_extra_info.visibility = View.GONE
                    rl_entity_main.visibility = View.GONE
                    rl_select_retailer.visibility = View.GONE
                    assign_to_shop_rl.visibility = View.GONE
                    setMargin(false)
                    til_no.hint = getString(R.string.owner_contact_number)
                    til_mail.hint = getString(R.string.owner_email)
                    til_name.hint = getString(R.string.owner_name)
                    assign_to_tv.hint = "Assigned to " + Pref.ppText

                    if (Pref.isShowDealerForDD)
                        rl_select_dealer.visibility = View.VISIBLE
                    else
                        rl_select_dealer.visibility = View.GONE

                    if(assignPPList != null && assignPPList.isNotEmpty()) {
                        assignedToPPId = assignPPList[0].pp_id!!
                        assign_to_tv.text = assignPPList[0].pp_name
                    }
                }
                "5" -> {
                    rl_assign_to_dd.visibility = View.VISIBLE
                    assign_to_rl.visibility = View.VISIBLE
                    rl_amount.visibility = View.VISIBLE
                    shopImage.visibility = View.VISIBLE
                    ll_doc_extra_info.visibility = View.GONE
                    ll_extra_info.visibility = View.GONE
                    rl_entity_main.visibility = View.GONE
                    rl_select_retailer.visibility = View.GONE
                    rl_select_dealer.visibility = View.GONE
                    assign_to_shop_rl.visibility = View.GONE
                    setMargin(false)
                    til_no.hint = getString(R.string.owner_contact_number)
                    til_mail.hint = getString(R.string.owner_email)
                    til_name.hint = getString(R.string.owner_name)
                    assign_to_tv.hint = "Assigned to " + Pref.ppText

                    if(assignPPList != null && assignPPList.isNotEmpty()) {
                        assignedToPPId = assignPPList[0].pp_id!!
                        assign_to_tv.text = assignPPList[0].pp_name
                    }

                    if(assignDDList != null && assignDDList.isNotEmpty()) {
                        assignedToDDId = assignDDList[0].dd_id!!
                        tv_assign_to_dd.text = assignDDList[0].dd_name
                    }
                }
                "6" -> {
                    rl_assign_to_dd.visibility = View.GONE
                    assign_to_rl.visibility = View.GONE
                    rl_amount.visibility = View.GONE
                    tv_name_asterisk_mark.visibility = View.VISIBLE
                    ll_doc_extra_info.visibility = View.GONE
                    rl_entity_main.visibility = View.GONE
                    rl_select_retailer.visibility = View.GONE
                    rl_select_dealer.visibility = View.GONE
                    assign_to_shop_rl.visibility = View.GONE
                    checkExtraInfoWillVisibleOrNot()
                    shopImage.visibility = View.VISIBLE
                    setMargin(false)
                    til_no.hint = getString(R.string.contact_number)
                    til_mail.hint = getString(R.string.contact_email)
                    til_name.hint = getString(R.string.contact_name)
                    assign_to_tv.hint = "Assigned to " + Pref.ppText
                }
                "7" -> {
                    rl_assign_to_dd.visibility = View.GONE
                    assign_to_rl.visibility = View.VISIBLE
                    rl_amount.visibility = View.GONE
                    tv_name_asterisk_mark.visibility = View.VISIBLE
                    ll_doc_extra_info.visibility = View.GONE
                    rl_entity_main.visibility = View.GONE
                    rl_select_retailer.visibility = View.GONE
                    rl_select_dealer.visibility = View.GONE
                    assign_to_shop_rl.visibility = View.GONE
                    checkExtraInfoWillVisibleOrNot()
                    shopImage.visibility = View.VISIBLE
                    setMargin(false)
                    til_no.hint = getString(R.string.contact_number)
                    til_mail.hint = getString(R.string.contact_email)
                    til_name.hint = getString(R.string.contact_name)
                    assign_to_tv.hint = "Assigned to"

                    if(assignPPList != null && assignPPList.isNotEmpty()) {
                        assignedToPPId = assignPPList[0].pp_id!!
                        assign_to_tv.text = assignPPList[0].pp_name
                    }
                }
                "8" -> {
                    rl_assign_to_dd.visibility = View.GONE
                    assign_to_rl.visibility = View.GONE
                    rl_amount.visibility = View.GONE
                    tv_name_asterisk_mark.visibility = View.GONE
                    ll_doc_extra_info.visibility = View.VISIBLE
                    ll_extra_info.visibility = View.GONE
                    rl_entity_main.visibility = View.GONE
                    rl_select_retailer.visibility = View.GONE
                    rl_select_dealer.visibility = View.GONE
                    //ownerEmail.imeOptions = EditorInfo.IME_ACTION_NEXT
                    shopImage.visibility = View.GONE
                    assign_to_shop_rl.visibility = View.GONE
                    setMargin(true)
                    til_no.hint = getString(R.string.contact_number)
                    til_mail.hint = getString(R.string.contact_email)
                    til_name.hint = getString(R.string.contact_name)
                    assign_to_tv.hint = "Assigned to " + Pref.ppText
                }
                "10" -> {
                    if (Pref.isDDShowForMeeting) {
                        rl_assign_to_dd.visibility = View.VISIBLE

                        if(assignDDList != null && assignDDList.isNotEmpty()) {
                            assignedToDDId = assignDDList[0].dd_id!!
                            tv_assign_to_dd.text = assignDDList[0].dd_name
                        }
                    }
                    else
                        rl_assign_to_dd.visibility = View.GONE

                    if (Pref.isDDMandatoryForMeeting)
                        tv_dd_asterisk_mark.visibility = View.VISIBLE
                    else
                        tv_dd_asterisk_mark.visibility = View.GONE

                    assign_to_rl.visibility = View.GONE
                    rl_amount.visibility = View.GONE
                    rl_type.visibility = View.VISIBLE
                    shopImage.visibility = View.VISIBLE
                    ll_doc_extra_info.visibility = View.GONE
                    ll_extra_info.visibility = View.GONE
                    rl_entity_main.visibility = View.GONE
                    rl_select_retailer.visibility = View.GONE
                    rl_select_dealer.visibility = View.GONE
                    assign_to_shop_rl.visibility = View.GONE
                    setMargin(false)
                    til_no.hint = getString(R.string.owner_contact_number)
                    til_mail.hint = getString(R.string.owner_email)
                    til_name.hint = getString(R.string.owner_name)
                    assign_to_tv.hint = "Assigned to " + Pref.ppText
                }
                "11" -> {
                    rl_assign_to_dd.visibility = View.GONE
                    assign_to_rl.visibility = View.GONE
                    rl_amount.visibility = View.GONE
                    shopImage.visibility = View.VISIBLE
                    ll_doc_extra_info.visibility = View.GONE
                    ll_extra_info.visibility = View.GONE
                    assign_to_shop_rl.visibility = View.GONE
                    rl_select_dealer.visibility = View.GONE
                    rl_entity_main.visibility = View.GONE
                    assign_to_shop_rl.visibility = View.VISIBLE
                    setMargin(false)
                    til_no.hint = getString(R.string.owner_contact_number)
                    til_mail.hint = getString(R.string.owner_email)
                    til_name.hint = getString(R.string.owner_name)
                    assign_to_tv.hint = "Assigned to " + Pref.ppText

                    if (Pref.isShowRetailerEntity)
                        rl_select_retailer.visibility = View.VISIBLE
                    else
                        rl_select_retailer.visibility = View.GONE
                }
                else -> {
                    rl_assign_to_dd.visibility = View.GONE
                    assign_to_rl.visibility = View.GONE
                    rl_amount.visibility = View.GONE
                    rl_type.visibility = View.GONE
                    shopImage.visibility = View.VISIBLE
                    ll_doc_extra_info.visibility = View.GONE
                    ll_extra_info.visibility = View.GONE
                    rl_entity_main.visibility = View.GONE
                    rl_select_retailer.visibility = View.GONE
                    rl_select_dealer.visibility = View.GONE
                    assign_to_shop_rl.visibility = View.GONE
                    setMargin(false)
                    til_no.hint = getString(R.string.owner_contact_number)
                    til_mail.hint = getString(R.string.owner_email)
                    til_name.hint = getString(R.string.owner_name)
                    assign_to_tv.hint = "Assigned to " + Pref.ppText
                }
            }
        }

        /*if (Pref.isReplaceShopText)
            shop_name_TL.hint = getString(R.string.customer_name)
        else
            shop_name_TL.hint = getString(R.string.shop_name)*/

        shop_name_TL.hint = Pref.shopText + " name"

        if (Pref.isAreaMandatoryInPartyCreation)
            tv_area_asterisk_mark.visibility = View.VISIBLE
        else
            tv_area_asterisk_mark.visibility = View.GONE

        // force off for ITC 25-08-2021
        /*if (Pref.isAreaVisible)
            rl_area_main.visibility = View.VISIBLE
        else
            rl_area_main.visibility = View.GONE*/

        if (Pref.willShowPartyStatus)
            rl_party_main.visibility = View.VISIBLE
        else
            rl_party_main.visibility = View.GONE

        //booking_amount_EDT.addTextChangedListener(CustomTextWatcher(booking_amount_EDT, 10, 2))
        booking_amount_EDT.filters = arrayOf<InputFilter>(InputFilterDecimal(10, 2))

        shop_type_RL.setOnClickListener(this)
        assign_to_rl.setOnClickListener(this)
        rl_assign_to_dd.setOnClickListener(this)
        rl_area.setOnClickListener(this)
        tv_model.setOnClickListener(this)
        tv_primary_app.setOnClickListener(this)
        tv_secondary_app.setOnClickListener(this)
        tv_lead_type.setOnClickListener(this)
        tv_stage.setOnClickListener(this)
        tv_funnel_stage.setOnClickListener(this)
        iv_model_dropdown.setOnClickListener(this)
        iv_primary_app_dropdown.setOnClickListener(this)
        iv_secondary_app_dropdown.setOnClickListener(this)
        iv_lead_type_dropdown.setOnClickListener(this)
        iv_stage_dropdown.setOnClickListener(this)
        iv_funnel_stage_dropdown.setOnClickListener(this)
        rl_type.setOnClickListener(this)
        next_visit_date_EDT.setOnClickListener(this)
        audio_record_date_EDT.setOnClickListener(this)

        family_mem_dob_EDT.setOnClickListener(this)
        add_dob_EDT.setOnClickListener(this)
        add_date_aniverdary_EDT.setOnClickListener(this)
        ll_yes.setOnClickListener(this)
        ll_no.setOnClickListener(this)
        assistant_date_aniverdary_EDT.setOnClickListener(this)
        assistant_family_mem_dob_EDT.setOnClickListener(this)
        assistant_dob_EDT.setOnClickListener(this)
        doc_family_mem_dob_EDT.setOnClickListener(this)
        attachment_EDT.setOnClickListener(this)
        rl_entity.setOnClickListener(this)
        rl_party.setOnClickListener(this)
        rl_select_retailer.setOnClickListener(this)
        rl_select_dealer.setOnClickListener(this)
        rl_select_beat.setOnClickListener(this)
        assign_to_shop_rl.setOnClickListener(this)

//        shopLargeImg = view.findViewById(R.id.shop_large_IMG);
//        imageRL = view.findViewById(R.id.shop_image_RL)

//        shopPin.setText(getZipCodeFromLocation(Location())


        layer_image_vw_IMG.visibility = View.VISIBLE
        take_photo_tv.visibility = View.VISIBLE
        capture_shop_image_IV.visibility = View.VISIBLE


//        captureShopImage.setOnClickListener(this)
        saveTV.setOnClickListener(this)
//        imageRL.setOnClickListener(this)
        shopLargeImg.setOnClickListener(this)
        ll_competitor_image.setOnClickListener(this)


        //assign_to_tv.hint = "Assigned to " + Pref.ppText
        tv_assign_to_dd.hint = "Assigned to " + Pref.ddText

        shopName.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (!hasFocus)
                rv_suggestion_shop_list.visibility = View.GONE
        }

        shopName.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    shopName.clearFocus()
                    shopAddress.requestFocus()
                    return true
                }
                return false
            }
        })

        feedback_EDT.setOnTouchListener(View.OnTouchListener { v, event ->
            if (feedback_EDT.hasFocus()) {
                v?.parent?.requestDisallowInterceptTouchEvent(true)
                when (event?.action /*& MotionEvent.ACTION_MASK*/) {
                    MotionEvent.ACTION_SCROLL -> {
                        v?.parent?.requestDisallowInterceptTouchEvent(false)
                        return@OnTouchListener true
                    }
                }
            }
            false
        })

        /*datapath = (mContext as DashboardActivity).filesDir.absolutePath + "/tesseract/"
        FTStorageUtils.checkFile(File(datapath + "tessdata/"), datapath, mContext)
        mTess.init(datapath, "eng")*/
    }

    override fun onResume() {
        super.onResume()

        try {
            if (mLongitude != "" && mLatitude != "") {
                //Toaster.msgShort(mContext, "Lat: $mLatitude, Lng: $mLongitude")
                Timber.e("AddShop : Lat=> $mLatitude, Long==> $mLongitude")
            } else {
                //Toaster.msgShort(mContext, "Lat: ${mLocation?.latitude}, Lng: ${mLocation?.longitude}")
                Timber.e("AddShop : Lat=> " + mLocation?.latitude + ", Long==> " + mLocation?.longitude)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setMargin(isDoctor: Boolean) {

        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        if (isDoctor) {
            params.setMargins(mContext.resources.getDimensionPixelOffset(R.dimen._10sdp), mContext.resources.getDimensionPixelOffset(R.dimen._10sdp),
                    mContext.resources.getDimensionPixelOffset(R.dimen._10sdp), mContext.resources.getDimensionPixelOffset(R.dimen._10sdp))
        } else {
            params.setMargins(mContext.resources.getDimensionPixelOffset(R.dimen._10sdp), mContext.resources.getDimensionPixelOffset(R.dimen._135sdp),
                    mContext.resources.getDimensionPixelOffset(R.dimen._10sdp), mContext.resources.getDimensionPixelOffset(R.dimen._10sdp))
        }
        add_shop_ll.layoutParams = params
    }


    private fun checkExtraInfoWillVisibleOrNot() {
        if (Pref.willMoreVisitUpdateCompulsory) {
            ll_extra_info.visibility = View.VISIBLE
            //ownerEmail.imeOptions = EditorInfo.IME_ACTION_NEXT
        } else {
            ll_extra_info.visibility = View.GONE
            //ownerEmail.imeOptions = EditorInfo.IME_ACTION_DONE
        }
    }

    private fun initShopNameListAdapter(shopList: ArrayList<String>) {
        rv_suggestion_shop_list.visibility = View.VISIBLE
        rv_suggestion_shop_list.adapter = SuggestionShopListAdapter(mContext, shopList)
    }

    private fun initTextChangeListener() {
        shopName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (shopName.text!!.length > 1) {
                    //val list = AppDatabase.getDBInstance()!!.addShopEntryDao().all

                    val allShopList = AppDatabase.getDBInstance()!!.addShopEntryDao().all

                    val newList = java.util.ArrayList<AddShopDBModelEntity>()

                    for (i in allShopList.indices) {
                        val userId = allShopList[i].shop_id.substring(0, allShopList[i].shop_id.indexOf("_"))
                        if (userId == Pref.user_id)
                            newList.add(allShopList[i])
                    }

                    val shopList = ArrayList<String>()
                    if (/*newList != null &&*/newList.size > 0) {
                        for (i in newList.indices) {
                            if (newList[i].shopName.contains(shopName.text.toString().trim())) {
                                shopList.add(newList[i].shopName)
                            }
                        }
                    }

                    if (shopList.size > 0)
                        initShopNameListAdapter(shopList)
                    else
                        rv_suggestion_shop_list.visibility = View.GONE
                } else
                    rv_suggestion_shop_list.visibility = View.GONE
            }
        })

        /*if (!shopName.isFocused) {
            rv_suggestion_shop_list.visibility = View.GONE
        }*/
    }


    private fun showProfileAlert() {
        CommonDialog.getInstance(getString(R.string.app_name), "Please update your profile", getString(R.string.cancel), getString(R.string.ok), object : CommonDialogClickListener {
            override fun onLeftClick() {
            }

            override fun onRightClick(editableData: String) {
                (mContext as DashboardActivity).loadFragment(FragType.MyProfileFragment, false, "")
            }

        }).show((mContext as DashboardActivity).supportFragmentManager, "")
    }

    private fun getAssignedPPListApi(isShopAdded: Boolean, shop_id: String?) {
        //bypass code begin
        if(isShopAdded){
            getAssignedToShopApi(isShopAdded, shop_id)
        }else{
        //bypass code end
        val repository = AssignToPPListRepoProvider.provideAssignPPListRepository()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
                repository.assignToPPList(Pref.profile_state)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as AssignToPPListResponseModel
                            if (response.status == NetworkConstant.SUCCESS) {
                                val list = response.assigned_to_pp_list

                                if (list != null && list.isNotEmpty()) {

                                    doAsync {

                                        val assignPPList = AppDatabase.getDBInstance()?.ppListDao()?.getAll()
                                        if (assignPPList != null)
                                            AppDatabase.getDBInstance()?.ppListDao()?.delete()

                                        for (i in list.indices) {
                                            val assignToPP = AssignToPPEntity()
                                            assignToPP.pp_id = list[i].assigned_to_pp_id
                                            assignToPP.pp_name = list[i].assigned_to_pp_authorizer_name
                                            assignToPP.pp_phn_no = list[i].phn_no
                                            AppDatabase.getDBInstance()?.ppListDao()?.insert(assignToPP)
                                        }

                                        uiThread {
                                            progress_wheel.stopSpinning()
                                            if (!isShopAdded)
                                                showAssignedToPPDialog(AppDatabase.getDBInstance()?.ppListDao()?.getAll(), addShopData.type)
                                            else {
                                                getAssignedDDListApi(isShopAdded, shop_id)
                                            }
                                        }
                                    }
                                } else {
                                    progress_wheel.stopSpinning()
                                    if (!isShopAdded)
                                        (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                    else {
                                        /*if (!TextUtils.isEmpty(shop_id))
                                            callOtpSentApi(shop_id!!)*/
                                        showShopVerificationDialog(shop_id!!)
                                    }
                                }
                            } else {
                                progress_wheel.stopSpinning()
                                if (!isShopAdded)
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                else {
                                    /*if (!TextUtils.isEmpty(shop_id))
                                        callOtpSentApi(shop_id!!)*/
                                    showShopVerificationDialog(shop_id!!)
                                }
                            }

                        }, { error ->
                            progress_wheel.stopSpinning()
                            if (!isShopAdded)
                                (mContext as DashboardActivity).showSnackMessage("ERROR")
                            else {
                                /*if (!TextUtils.isEmpty(shop_id))
                                    callOtpSentApi(shop_id!!)*/
                                showShopVerificationDialog(shop_id!!)
                            }
                        })
        )
        }
    }
    private fun voiceAttendanceMsg(msg: String) {
        if (Pref.isVoiceEnabledForAttendanceSubmit) {
            val speechStatus = (mContext as DashboardActivity).textToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, null)
            if (speechStatus == TextToSpeech.ERROR)
                Log.e("Add Day Start", "TTS error in converting Text to Speech!");
        }
    }

    private fun addShopApi(addShop: AddShopRequestData, shop_imgPath: String?, doc_degree: String?) {
//        if (!AppUtils.isOnline(mContext)){
//            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
//            return
//        }
        AppUtils.isShopAdded = true

        Timber.d("================AddShop Input Params==================")
        Timber.d("shop id=======> " + addShop.shop_id)
        val index = addShop.shop_id!!.indexOf("_")
        Timber.d("decoded shop id=======> " + addShop.user_id + "_" + AppUtils.getDate(addShop.shop_id!!.substring(index + 1, addShop.shop_id!!.length).toLong()))
        Timber.d("shop added date=======> " + addShop.added_date)
        Timber.d("shop address=======> " + addShop.address)
        Timber.d("assigned to dd id=======> " + addShop.assigned_to_dd_id)
        Timber.d("assigned to pp id=======> " + addShop.assigned_to_pp_id)
        Timber.d("date aniversery=======> " + addShop.date_aniversary)
        Timber.d("dob=======> " + addShop.dob)
        Timber.d("shop owner phn no=======> " + addShop.owner_contact_no)
        Timber.d("shop owner email=======> " + addShop.owner_email)
        Timber.d("shop owner name=======> " + addShop.owner_name)
        Timber.d("shop pincode=======> " + addShop.pin_code)
        Timber.d("session token=======> " + addShop.session_token)
        Timber.d("shop lat=======> " + addShop.shop_lat)
        Timber.d("shop long=======> " + addShop.shop_long)
        Timber.d("shop name=======> " + addShop.shop_name)
        Timber.d("shop type=======> " + addShop.type)
        Timber.d("user id=======> " + addShop.user_id)
        Timber.d("amount=======> " + addShop.amount)
        Timber.d("area id=======> " + addShop.area_id)
        Timber.d("model id=======> " + addShop.model_id)
        Timber.d("primary app id=======> " + addShop.primary_app_id)
        Timber.d("secondary app id=======> " + addShop.secondary_app_id)
        Timber.d("lead id=======> " + addShop.lead_id)
        Timber.d("stage id=======> " + addShop.stage_id)
        Timber.d("funnel stage id=======> " + addShop.funnel_stage_id)
        Timber.d("booking amount=======> " + addShop.booking_amount)
        Timber.d("type id=======> " + addShop.type_id)
        Timber.d("director name=======> " + addShop.director_name)
        Timber.d("family member dob=======> " + addShop.family_member_dob)
        Timber.d("key person's name=======> " + addShop.key_person_name)
        Timber.d("phone no=======> " + addShop.phone_no)
        Timber.d("additional dob=======> " + addShop.addtional_dob)
        Timber.d("additional doa=======> " + addShop.addtional_doa)
        Timber.d("doctor family member dob=======> " + addShop.doc_family_member_dob)
        Timber.d("specialization=======> " + addShop.specialization)
        Timber.d("average patient count per day=======> " + addShop.average_patient_per_day)
        Timber.d("category=======> " + addShop.category)
        Timber.d("doctor address=======> " + addShop.doc_address)
        Timber.d("doctor pincode=======> " + addShop.doc_pincode)
        Timber.d("chambers or hospital under same headquarter=======> " + addShop.is_chamber_same_headquarter)
        Timber.d("chamber related remarks=======> " + addShop.is_chamber_same_headquarter_remarks)
        Timber.d("chemist name=======> " + addShop.chemist_name)
        Timber.d("chemist name=======> " + addShop.chemist_address)
        Timber.d("chemist pincode=======> " + addShop.chemist_pincode)
        Timber.d("assistant name=======> " + addShop.assistant_name)
        Timber.d("assistant contact no=======> " + addShop.assistant_contact_no)
        Timber.d("assistant dob=======> " + addShop.assistant_dob)
        Timber.d("assistant date of anniversary=======> " + addShop.assistant_doa)
        Timber.d("assistant family dob=======> " + addShop.assistant_family_dob)
        Timber.d("entity id=======> " + addShop.entity_id)
        Timber.d("party status id=======> " + addShop.party_status_id)
        Timber.d("retailer id=======> " + addShop.retailer_id)
        Timber.d("dealer id=======> " + addShop.dealer_id)
        Timber.d("beat id=======> " + addShop.beat_id)
        Timber.d("assigned to shop id=======> " + addShop.assigned_to_shop_id)
        Timber.d("actual address=======> " + addShop.actual_address)
        Timber.d("shopDuplicate=======> " + addShop.isShopDuplicate)

        if (shop_imgPath != null)
            Timber.d("shop image path=======> $shop_imgPath")

        if (doc_degree != null)
            Timber.d("doctor degree image path=======> $doc_degree")
        Timber.d("====================================================")

        progress_wheel.spin()



        if (TextUtils.isEmpty(shop_imgPath) && TextUtils.isEmpty(doc_degree)) {
            val repository = AddShopRepositoryProvider.provideAddShopWithoutImageRepository()
            BaseActivity.compositeDisposable.add(
                    repository.addShop(addShop)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ result ->
                                val addShopResult = result as AddShopResponse
                                Timber.d("AddShop : " + ", SHOP: " + addShop.shop_name + ", RESPONSE:" + result.message)
                                if (addShopResult.status == NetworkConstant.SUCCESS) {
                                    AppDatabase.getDBInstance()!!.addShopEntryDao().updateIsUploaded(true, addShop.shop_id)
                                    //callShopActivitySubmit(addShop.shop_id!!)
                                    progress_wheel.stopSpinning()
//                                (mContext as DashboardActivity).showSnackMessage("SUCCESS")
                                    (mContext as DashboardActivity).updateFence()
                                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.shop_added_successfully))

                                    voiceAttendanceMsg("Data Added successfully.")
                                    //(mContext as DashboardActivity).onBackPressed()

                                    // force off for ITC 25-08-2021
                                 /*   if(imagePathCompetitor!=null){
                                        addShopCompetetorImg(addShop.session_token,addShop.shop_id!!,Pref.user_id!!,imagePathCompetitor)
                                    }*/

                                    getAssignedPPListApi(true, addShop.shop_id)
                                    //showShopVerificationDialog(addShop.shop_id!!)

                                } else if (addShopResult.status == NetworkConstant.SESSION_MISMATCH) {
                                    Timber.d("AddShop : " + ", SHOP: " + addShop.shop_name + ", RESPONSE:" + result.message)
                                    progress_wheel.stopSpinning()
                                    (mContext as DashboardActivity).clearData()
                                    startActivity(Intent(mContext as DashboardActivity, LoginActivity::class.java))
                                    (mContext as DashboardActivity).overridePendingTransition(0, 0)
                                    (mContext as DashboardActivity).finish()
                                } else if (addShopResult.status == NetworkConstant.DUPLICATE_SHOP_ID) {
                                    Timber.d("DuplicateShop : " + ", SHOP: " + addShop.shop_name)
                                    progress_wheel.stopSpinning()
                                    (mContext as DashboardActivity).showSnackMessage(addShopResult.message!!)
                                    if (AppDatabase.getDBInstance()!!.addShopEntryDao().getDuplicateShopData(addShop.owner_contact_no).size > 0) {
                                        AppDatabase.getDBInstance()!!.addShopEntryDao().deleteShopById(addShop.shop_id)
                                        AppDatabase.getDBInstance()!!.shopActivityDao().deleteShopByIdAndDate(addShop.shop_id!!, AppUtils.getCurrentDateForShopActi())
                                    }
                                    (mContext as DashboardActivity).onBackPressed()
                                    //01-09-2021
                                    (mContext as DashboardActivity).loadFragment(FragType.DashboardFragment, false, "")
                                    //(mContext as DashboardActivity).loadFragment(FragType.ShopDetailFragment, true, addShop.shop_id!!)
                                } else {
                                    progress_wheel.stopSpinning()
                                    Timber.d("AddShop : " + ", SHOP: " + addShop.shop_name + ", RESPONSE:" + result.message)
                                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.shop_added_successfully))
//                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.shop_added_successfully))
                                    (mContext as DashboardActivity).onBackPressed()
                                    //(mContext as DashboardActivity).loadFragment(FragType.NearByShopsListFragment, false, "")

                                    //01-09-2021
                                    (mContext as DashboardActivity).loadFragment(FragType.DashboardFragment, false, "")
                                    //(mContext as DashboardActivity).loadFragment(FragType.ShopDetailFragment, true, addShop.shop_id!!)
                                }
                                BaseActivity.isApiInitiated = false
//                            isApiCall=true
//                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.shop_added_successfully))
//                            (mContext as DashboardActivity).loadFragment(FragType.NearByShopsListFragment, false, "")
//                            (mContext as DashboardActivity).showSnackMessage("RESPONSE")
                            }, { error ->
                                //                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.shop_added_successfully))
//                            (mContext as DashboardActivity).loadFragment(FragType.NearByShopsListFragment, false, "")
//                            error.printStackTrace()
                                progress_wheel.stopSpinning()
                                BaseActivity.isApiInitiated = false
                                //(mContext as DashboardActivity).showSnackMessage(getString(R.string.unable_to_sync))
//                            (mContext as DashboardActivity).showSnackMessage("ERROR")
                                //(mContext as DashboardActivity).showSnackMessage(getString(R.string.shop_added_successfully))
                                (mContext as DashboardActivity).onBackPressed()
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.shop_added_successfully))
                                voiceAttendanceMsg("Data Added successfully.")
                                //(mContext as DashboardActivity).loadFragment(FragType.NearByShopsListFragment, false, "")
                                //01-09-2021
                                (mContext as DashboardActivity).loadFragment(FragType.DashboardFragment, false, "")
                                //(mContext as DashboardActivity).loadFragment(FragType.ShopDetailFragment, true, addShop.shop_id!!)
                                if (error != null) {
                                    Timber.d("AddShop : " + ", SHOP: " + addShop.shop_name + ", ERROR: " + error.localizedMessage)
                                }
                            })
            )
        }
        else {
            val repository = AddShopRepositoryProvider.provideAddShopRepository()
            BaseActivity.compositeDisposable.add(
                    repository.addShopWithImage(addShop, shop_imgPath, doc_degree, mContext)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ result ->
                                val addShopResult = result as AddShopResponse
                                Timber.d("AddShop : " + ", SHOP: " + addShop.shop_name + ", RESPONSE:" + result.message)
                                if (addShopResult.status == NetworkConstant.SUCCESS) {
                                    AppDatabase.getDBInstance()!!.addShopEntryDao().updateIsUploaded(true, addShop.shop_id)
                                    //callShopActivitySubmit(addShop.shop_id!!)
                                    progress_wheel.stopSpinning()
//                                (mContext as DashboardActivity).showSnackMessage("SUCCESS")
                                    (mContext as DashboardActivity).updateFence()
                                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.shop_added_successfully))
                                    //(mContext as DashboardActivity).onBackPressed()
                                    //01-09-2021
                                    voiceAttendanceMsg("Data Added successfully.")
                                    // force off for ITC 25-08-2021
                                     /*   if(imagePathCompetitor!=null){
                                            addShopCompetetorImg(addShop.session_token,addShop.shop_id!!,Pref.user_id!!,imagePathCompetitor)
                                        }*/


                                    getAssignedPPListApi(true, addShop.shop_id)
                                    //showShopVerificationDialog(addShop.shop_id!!)

                                } else if (addShopResult.status == NetworkConstant.SESSION_MISMATCH) {
                                    Timber.d("AddShop : " + ", SHOP: " + addShop.shop_name + ", RESPONSE:" + result.message)
                                    progress_wheel.stopSpinning()
                                    (mContext as DashboardActivity).clearData()
                                    startActivity(Intent(mContext as DashboardActivity, LoginActivity::class.java))
                                    (mContext as DashboardActivity).overridePendingTransition(0, 0)
                                    (mContext as DashboardActivity).finish()
                                } else if (addShopResult.status == NetworkConstant.DUPLICATE_SHOP_ID) {
                                    Timber.d("DuplicateShop : " + ", SHOP: " + addShop.shop_name)
                                    progress_wheel.stopSpinning()
                                    (mContext as DashboardActivity).showSnackMessage(addShopResult.message!!)
                                    if (AppDatabase.getDBInstance()!!.addShopEntryDao().getDuplicateShopData(addShop.owner_contact_no).size > 0) {
                                        AppDatabase.getDBInstance()!!.addShopEntryDao().deleteShopById(addShop.shop_id)
                                        AppDatabase.getDBInstance()!!.shopActivityDao().deleteShopByIdAndDate(addShop.shop_id!!, AppUtils.getCurrentDateForShopActi())
                                    }
                                    (mContext as DashboardActivity).onBackPressed()

                                    //01-09-2021
                                    (mContext as DashboardActivity).loadFragment(FragType.DashboardFragment, false, "")

                                    //(mContext as DashboardActivity).loadFragment(FragType.ShopDetailFragment, true, addShop.shop_id!!)
                                } else {
                                    progress_wheel.stopSpinning()
                                    Timber.d("AddShop : " + ", SHOP: " + addShop.shop_name + ", RESPONSE:" + result.message)
                                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.shop_added_successfully))
//                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.shop_added_successfully))
                                    (mContext as DashboardActivity).onBackPressed()
                                    //(mContext as DashboardActivity).loadFragment(FragType.NearByShopsListFragment, false, "")
                                    //01-09-2021
                                    (mContext as DashboardActivity).loadFragment(FragType.DashboardFragment, false, "")
                                    //(mContext as DashboardActivity).loadFragment(FragType.ShopDetailFragment, true, addShop.shop_id!!)
                                }
                                BaseActivity.isApiInitiated = false
//                            isApiCall=true
//                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.shop_added_successfully))
//                            (mContext as DashboardActivity).loadFragment(FragType.NearByShopsListFragment, false, "")
//                            (mContext as DashboardActivity).showSnackMessage("RESPONSE")
                            }, { error ->
                                //                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.shop_added_successfully))
//                            (mContext as DashboardActivity).loadFragment(FragType.NearByShopsListFragment, false, "")
//                            error.printStackTrace()
                                progress_wheel.stopSpinning()
                                BaseActivity.isApiInitiated = false
                                //(mContext as DashboardActivity).showSnackMessage(getString(R.string.unable_to_sync))
//                            (mContext as DashboardActivity).showSnackMessage("ERROR")
                                //(mContext as DashboardActivity).showSnackMessage(getString(R.string.shop_added_successfully))
                                (mContext as DashboardActivity).onBackPressed()
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.shop_added_successfully))
                                //(mContext as DashboardActivity).loadFragment(FragType.NearByShopsListFragment, false, "")
                                //01-09-2021
                                 voiceAttendanceMsg("Data Added successfully.")
                                //(mContext as DashboardActivity).loadFragment(FragType.ShopDetailFragment, true, addShop.shop_id!!)
                                //01-09-2021
                                (mContext as DashboardActivity).loadFragment(FragType.DashboardFragment, false, "")
                                if (error != null) {
                                    Timber.d("AddShop : " + ", SHOP: " + addShop.shop_name + ", ERROR: " + error.localizedMessage)
                                }
                            })
            )
        }

    }



    private fun addShopCompetetorImg(sessionToken:String?,shopId:String,userId:String,shop_imgPathCompetitor: String?){
        var objCompetetor :AddShopRequestCompetetorImg = AddShopRequestCompetetorImg()
        objCompetetor.session_token=sessionToken
        objCompetetor.shop_id=shopId
        objCompetetor.user_id=userId
        //objCompetetor.visited_date=AppUtils.getCurrentDateTime()
        objCompetetor.visited_date=""
        val repository = AddShopRepositoryProvider.provideAddShopRepository()
        BaseActivity.compositeDisposable.add(
                repository.addShopWithImageCompetetorImg(objCompetetor,shop_imgPathCompetitor,mContext)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as BaseResponse
                            if(response.status==NetworkConstant.SUCCESS){
                                AppDatabase.getDBInstance()!!.shopVisitCompetetorImageDao().updateisUploaded(true,shopId)
                                Timber.d("AddShop : CompetetorImg" + ", SHOP: " + shopId + ", Success: ")
                            }else{
                                Timber.d("AddShop : CompetetorImg" + ", SHOP: " + shopId + ", Failed: ")
                            }
                        },{
                            error ->
                            if (error != null) {
                                Timber.d("AddShop : CompetetorImg" + ", SHOP: " + shopId + ", ERROR: " + error.localizedMessage)
                            }
                        })
        )
    }



    private fun showShopVerificationDialog(shop_id: String) {

        // change ITC 27-08-2021
        if(Pref.isOrderShow==false){
            (mContext as DashboardActivity).loadFragment(FragType.DashboardFragment, false, "")
        }else{
                if (!Pref.isShowOTPVerificationPopup)  {
            (mContext as DashboardActivity).onBackPressed()
                    // change ITC 11-10-2021
            //(mContext as DashboardActivity).loadFragment(FragType.ShopDetailFragment, true, shop_id)
        }
        else {
            ShopVerificationDialog.getInstance(shop_id, object : ShopVerificationDialog.OnOTPButtonClickListener {
                override fun onEditClick(number: String) {
                    val addShopData = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopDetail(shop_id)
                    if (!addShopData.isUploaded || addShopData.isEditUploaded == 0) {
                        (mContext as DashboardActivity).showSnackMessage("Please sync this shop first.")
                        (mContext as DashboardActivity).onBackPressed()
                        (mContext as DashboardActivity).loadFragment(FragType.NearByShopsListFragment, true, "")
                    } else {
                        saveData(addShopData, number)
                    }

                }

                override fun onCancelClick() {
                    (mContext as DashboardActivity).onBackPressed()
                    (mContext as DashboardActivity).loadFragment(FragType.ShopDetailFragment, true, shop_id)
                }

                override fun onOkButtonClick(otp: String) {
                    callOtpSentApi(shop_id)
                }
            }).show((mContext as DashboardActivity).supportFragmentManager, "ShopVerificationDialog")
        }
        }


    }


    private fun saveData(addShopData: AddShopDBModelEntity, number: String) {
        AppDatabase.getDBInstance()?.addShopEntryDao()?.updateContactNo(addShopData.shop_id, number)

        val shop = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopDetail(addShopData.shop_id)
        convertToReqAndApiCall_(shop)
    }


    private fun convertToReqAndApiCall_(addShopData: AddShopDBModelEntity) {
        if (Pref.user_id == null || Pref.user_id == "" || Pref.user_id == " ") {
            (mContext as DashboardActivity).showSnackMessage("Please login again")
            BaseActivity.isApiInitiated = false
            return
        }

        val addShopReqData = AddShopRequestData()
        addShopReqData.session_token = Pref.session_token
        addShopReqData.address = addShopData.address
        addShopReqData.owner_contact_no = addShopData.ownerContactNumber
        addShopReqData.owner_email = addShopData.ownerEmailId
        addShopReqData.owner_name = addShopData.ownerName
        addShopReqData.pin_code = addShopData.pinCode
        addShopReqData.shop_lat = addShopData.shopLat.toString()
        addShopReqData.shop_long = addShopData.shopLong.toString()
        addShopReqData.shop_name = addShopData.shopName.toString()
        addShopReqData.shop_id = addShopData.shop_id
        addShopReqData.added_date = ""
        addShopReqData.user_id = Pref.user_id
        addShopReqData.type = addShopData.type
        addShopReqData.assigned_to_pp_id = addShopData.assigned_to_pp_id
        addShopReqData.assigned_to_dd_id = addShopData.assigned_to_dd_id

        if (!TextUtils.isEmpty(addShopData.dateOfBirth))
            addShopReqData.dob = AppUtils.changeAttendanceDateFormatToCurrent(addShopData.dateOfBirth)

        if (!TextUtils.isEmpty(addShopData.dateOfAniversary))
            addShopReqData.date_aniversary = AppUtils.changeAttendanceDateFormatToCurrent(addShopData.dateOfAniversary)

        addShopReqData.amount = addShopData.amount
        addShopReqData.area_id = addShopData.area_id
        addShopReqData.model_id = addShopData.model_id
        addShopReqData.primary_app_id = addShopData.primary_app_id
        addShopReqData.secondary_app_id = addShopData.secondary_app_id
        addShopReqData.lead_id = addShopData.lead_id
        addShopReqData.stage_id = addShopData.stage_id
        addShopReqData.funnel_stage_id = addShopData.funnel_stage_id
        addShopReqData.booking_amount = addShopData.booking_amount
        addShopReqData.type_id = addShopData.type_id
        /*val addShop = AddShopRequest()
        addShop.data = addShopReqData*/

        addShopReqData.director_name = addShopData.director_name
        addShopReqData.key_person_name = addShopData.person_name
        addShopReqData.phone_no = addShopData.person_no

        if (!TextUtils.isEmpty(addShopData.family_member_dob))
            addShopReqData.family_member_dob = AppUtils.changeAttendanceDateFormatToCurrent(addShopData.family_member_dob)

        if (!TextUtils.isEmpty(addShopData.add_dob))
            addShopReqData.addtional_dob = AppUtils.changeAttendanceDateFormatToCurrent(addShopData.add_dob)

        if (!TextUtils.isEmpty(addShopData.add_doa))
            addShopReqData.addtional_doa = AppUtils.changeAttendanceDateFormatToCurrent(addShopData.add_doa)

        addShopReqData.specialization = addShopData.specialization
        addShopReqData.category = addShopData.category
        addShopReqData.doc_address = addShopData.doc_address
        addShopReqData.doc_pincode = addShopData.doc_pincode
        addShopReqData.is_chamber_same_headquarter = addShopData.chamber_status.toString()
        addShopReqData.is_chamber_same_headquarter_remarks = addShopData.remarks
        addShopReqData.chemist_name = addShopData.chemist_name
        addShopReqData.chemist_address = addShopData.chemist_address
        addShopReqData.chemist_pincode = addShopData.chemist_pincode
        addShopReqData.assistant_contact_no = addShopData.assistant_no
        addShopReqData.average_patient_per_day = addShopData.patient_count
        addShopReqData.assistant_name = addShopData.assistant_name

        if (!TextUtils.isEmpty(addShopData.doc_family_dob))
            addShopReqData.doc_family_member_dob = AppUtils.changeAttendanceDateFormatToCurrent(addShopData.doc_family_dob)

        if (!TextUtils.isEmpty(addShopData.assistant_dob))
            addShopReqData.assistant_dob = AppUtils.changeAttendanceDateFormatToCurrent(addShopData.assistant_dob)

        if (!TextUtils.isEmpty(addShopData.assistant_doa))
            addShopReqData.assistant_doa = AppUtils.changeAttendanceDateFormatToCurrent(addShopData.assistant_doa)

        if (!TextUtils.isEmpty(addShopData.assistant_family_dob))
            addShopReqData.assistant_family_dob = AppUtils.changeAttendanceDateFormatToCurrent(addShopData.assistant_family_dob)

        addShopReqData.entity_id = addShopData.entity_id
        addShopReqData.party_status_id = addShopData.party_status_id
        addShopReqData.retailer_id = addShopData.retailer_id
        addShopReqData.dealer_id = addShopData.dealer_id
        addShopReqData.beat_id = addShopData.beat_id
        addShopReqData.assigned_to_shop_id = addShopData.assigned_to_shop_id
        addShopReqData.actual_address = addShopData.actual_address

        if (AppUtils.isOnline(mContext)) {

            if (BaseActivity.isApiInitiated)
                return

            BaseActivity.isApiInitiated = true

            callEditShopApi(addShopReqData, addShopData.shopImageLocalPath, addShopData.doc_degree)
        } else {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
        }
    }

    private fun callEditShopApi(addShopReqData: AddShopRequestData, shopImageLocalPath: String?, doc_degree: String?) {
        val repository = EditShopRepoProvider.provideEditShopRepository()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
                repository.addShopWithImage(addShopReqData, shopImageLocalPath, doc_degree, mContext)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val addShopResult = result as AddShopResponse
                            Timber.d("Edit Shop : " + ", SHOP: " + addShopReqData.shop_name + ", RESPONSE:" + result.message)
                            if (addShopResult.status == NetworkConstant.SUCCESS) {
                                AppDatabase.getDBInstance()!!.addShopEntryDao().updateIsEditUploaded(1, addShopReqData.shop_id)
                                progress_wheel.stopSpinning()

                                showShopVerificationDialog(addShopReqData.shop_id!!)

                            } else if (addShopResult.status == NetworkConstant.SESSION_MISMATCH) {
                                progress_wheel.stopSpinning()
                                (mContext as DashboardActivity).clearData()
                                startActivity(Intent(mContext as DashboardActivity, LoginActivity::class.java))
                                (mContext as DashboardActivity).overridePendingTransition(0, 0)
                                (mContext as DashboardActivity).finish()
                            } else {
                                progress_wheel.stopSpinning()
                                (mContext as DashboardActivity).onBackPressed()
                                (mContext as DashboardActivity).loadFragment(FragType.ShopDetailFragment, true, addShopReqData.shop_id!!)
                            }
                            BaseActivity.isApiInitiated = false
                        }, { error ->
                            BaseActivity.isApiInitiated = false
                            (mContext as DashboardActivity).onBackPressed()
                            (mContext as DashboardActivity).loadFragment(FragType.ShopDetailFragment, true, addShopReqData.shop_id!!)
                            //(mContext as DashboardActivity).showSnackMessage(getString(R.string.unable_to_sync))
                        })
        )
    }


    private fun callOtpSentApi(shop_id: String) {
        val repository = OtpSentRepoProvider.otpSentRepoProvider()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
                repository.otpSent(shop_id)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val addShopResult = result as BaseResponse
                            progress_wheel.stopSpinning()
                            /*if (addShopResult.status == NetworkConstant.SUCCESS) {

                                (mContext as DashboardActivity).showSnackMessage(addShopResult.message!!)
                                showOtpVerificationDialog(shop_id)

                            } else {
                                (mContext as DashboardActivity).showSnackMessage("OTP sent failed")
                                (mContext as DashboardActivity).onBackPressed()
                                (mContext as DashboardActivity).loadFragment(FragType.ShopDetailFragment, true, shop_id)
                            }*/

                            showOtpVerificationDialog(shop_id, true)

                        }, { error ->
                            error.printStackTrace()
                            progress_wheel.stopSpinning()
                            /*(mContext as DashboardActivity).showSnackMessage("OTP sent failed")
                            (mContext as DashboardActivity).onBackPressed()
                            (mContext as DashboardActivity).loadFragment(FragType.ShopDetailFragment, true, shop_id)*/

                            showOtpVerificationDialog(shop_id, true)
                        })
        )
    }


    private fun showOtpVerificationDialog(shop_id: String, isShowTimer: Boolean) {
        val shop = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopDetail(shop_id)
        OTPVerificationDialog.getInstance(shop.ownerContactNumber, isShowTimer, shop.shopName, object : OTPVerificationDialog.OnOTPButtonClickListener {
            override fun onResentClick() {
                callOtpSentApi(shop_id)
            }

            override fun onCancelClick() {
                (mContext as DashboardActivity).onBackPressed()
                (mContext as DashboardActivity).loadFragment(FragType.ShopDetailFragment, true, shop_id)
            }

            override fun onOkButtonClick(otp: String) {

                val distance = LocationWizard.getDistance(shop.shopLat, shop.shopLong, Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble())

                if (distance * 1000 <= 20)
                    callOtpVerifyApi(otp, shop_id)
                else
                    (mContext as DashboardActivity).showSnackMessage("OTP can be verified only from the shop.")
            }
        }).show((mContext as DashboardActivity).supportFragmentManager, "OTPVerificationDialog")
    }

    private fun callOtpVerifyApi(otp: String, shop_id: String) {
        val repository = OtpVerificationRepoProvider.otpVerifyRepoProvider()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
                repository.otpVerify(shop_id, otp)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val addShopResult = result as BaseResponse
                            progress_wheel.stopSpinning()
                            if (addShopResult.status == NetworkConstant.SUCCESS) {
                                AppDatabase.getDBInstance()!!.addShopEntryDao().updateIsOtpVerified("true", shop_id)
                                (mContext as DashboardActivity).showSnackMessage(addShopResult.message!!)
                                (mContext as DashboardActivity).onBackPressed()
                                (mContext as DashboardActivity).loadFragment(FragType.ShopDetailFragment, true, shop_id)
                            } else {
                                (mContext as DashboardActivity).showSnackMessage("OTP verification failed.")
                                showOtpVerificationDialog(shop_id, false)
                            }
                        }, { error ->
                            error.printStackTrace()
                            progress_wheel.stopSpinning()
                            (mContext as DashboardActivity).showSnackMessage("OTP verification failed.")
                            showOtpVerificationDialog(shop_id, false)
                        })
        )
    }

    private fun callShopActivitySubmit(shopId: String) {
        if (shopId == null)
            return
        var mList = AppDatabase.getDBInstance()!!.shopActivityDao().getShopForDay(shopId, AppUtils.getCurrentDateForShopActi())
        if (mList.isEmpty())
            return
        var shopActivity = mList[0]
//        var shopActivity = AppDatabase.getDBInstance()!!.shopActivityDao().getShopActivityForId(shopId)
        var shopDurationApiReq = ShopDurationRequest()
        shopDurationApiReq.user_id = Pref.user_id
        shopDurationApiReq.session_token = Pref.session_token
        var shopDataList: MutableList<ShopDurationRequestData> = ArrayList()
        var shopDurationData = ShopDurationRequestData()
        shopDurationData.shop_id = shopActivity.shopid
        if (shopActivity.startTimeStamp != "0" && !shopActivity.isDurationCalculated) {
            var totalMinute = AppUtils.getMinuteFromTimeStamp(shopActivity.startTimeStamp, System.currentTimeMillis().toString())
            AppDatabase.getDBInstance()!!.shopActivityDao().updateTotalMinuteForDayOfShop(shopActivity.shopid!!, totalMinute, AppUtils.getCurrentDateForShopActi())
            var duration = AppUtils.getTimeFromTimeSpan(shopActivity.startTimeStamp, System.currentTimeMillis().toString())
            AppDatabase.getDBInstance()!!.shopActivityDao().updateTimeDurationForDayOfShop(shopActivity.shopid!!, duration, AppUtils.getCurrentDateForShopActi())
            shopDurationData.spent_duration = duration
        } else {
            shopDurationData.spent_duration = shopActivity.duration_spent
        }
        shopDurationData.visited_date = shopActivity.visited_date
        shopDurationData.visited_time = shopActivity.visited_date
        if (TextUtils.isEmpty(shopActivity.distance_travelled))
            shopActivity.distance_travelled = "0.0"
        shopDurationData.distance_travelled = shopActivity.distance_travelled
        val list = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdList(shopDurationData.shop_id)
        if (list != null && list.isNotEmpty())
            shopDurationData.total_visit_count = list[0].totalVisitCount

        if (!TextUtils.isEmpty(shopActivity.feedback))
            shopDurationData.feedback = shopActivity.feedback
        else
            shopDurationData.feedback = ""

        shopDurationData.isFirstShopVisited = shopActivity.isFirstShopVisited
        shopDurationData.distanceFromHomeLoc = shopActivity.distance_from_home_loc
        shopDurationData.next_visit_date = shopActivity.next_visit_date

        if (!TextUtils.isEmpty(shopActivity.early_revisit_reason))
            shopDurationData.early_revisit_reason = shopActivity.early_revisit_reason
        else
            shopDurationData.early_revisit_reason = ""

        shopDurationData.in_time = shopActivity.in_time
        shopDurationData.out_time = shopActivity.out_time
        shopDurationData.start_timestamp = shopActivity.startTimeStamp
        shopDurationData.in_location = shopActivity.in_loc
        shopDurationData.out_location = shopActivity.out_loc

        shopDataList.add(shopDurationData)

        if (shopDataList.isEmpty()) {
            return
        }

        shopDurationApiReq.shop_list = shopDataList
        val repository = ShopDurationRepositoryProvider.provideShopDurationRepository()

        BaseActivity.compositeDisposable.add(
                repository.shopDuration(shopDurationApiReq)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            Timber.d("ShopActivityFromAddShop : " + ", SHOP: " + mList[0].shop_name + ", RESPONSE:" + result.message)
                            if (result.status == NetworkConstant.SUCCESS) {

                            }

                        }, { error ->
                            error.printStackTrace()
                            if (error != null)
                                Timber.d("ShopActivityFromAddShop : " + ", SHOP: " + mList[0].shop_name + ", ERROR:" + error.localizedMessage)
                        })
        )

    }


    private fun insertIntoShopActivityTable(addShop: AddShopRequestData) {
        val list = AppDatabase.getDBInstance()!!.addMeetingDao().durationAvailable(false)
        if (list != null) {
            for (i in 0 until list.size) {
                val endTimeStamp = System.currentTimeMillis().toString()
                val duration = AppUtils.getTimeFromTimeSpan(list[i].startTimeStamp!!, endTimeStamp)
                val totalMinute = AppUtils.getMinuteFromTimeStamp(list[i].startTimeStamp!!, endTimeStamp)
                //If duration is greater than 20 hour then stop incrementing
                /*if (totalMinute.toInt() > 20 * 60) {
                    AppDatabase.getDBInstance()!!.addMeetingDao().updateDurationAvailable(true, list[i].id, AppUtils.getCurrentDateForShopActi())
                    return
                }*/
                AppDatabase.getDBInstance()!!.addMeetingDao().updateEndTimeOfMeeting(endTimeStamp, list[i].id, AppUtils.getCurrentDateForShopActi())
                AppDatabase.getDBInstance()!!.addMeetingDao().updateTimeDurationForDayOfMeeting(list[i].id, duration, AppUtils.getCurrentDateForShopActi())
                AppDatabase.getDBInstance()!!.addMeetingDao().updateDurationAvailable(true, list[i].id, AppUtils.getCurrentDateForShopActi())
            }
        }

        val shopList = AppDatabase.getDBInstance()!!.shopActivityDao().getTotalShopVisitedForADay(AppUtils.getCurrentDateForShopActi())
        for (i in shopList.indices) {
            if (shopList[i].shopid != addShop.shop_id && !shopList[i].isDurationCalculated) {
                Pref.durationCompletedShopId = shopList[i].shopid!!
                val endTimeStamp = System.currentTimeMillis().toString()
                val duration = AppUtils.getTimeFromTimeSpan(shopList[i].startTimeStamp, endTimeStamp)
                val totalMinute = AppUtils.getMinuteFromTimeStamp(shopList[i].startTimeStamp, endTimeStamp)
                //If duration is greater than 20 hour then stop incrementing
                if (totalMinute.toInt() > 20 * 60) {
                    if (!Pref.isMultipleVisitEnable)
                        AppDatabase.getDBInstance()!!.shopActivityDao().updateDurationAvailable(true, shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi())
                    else
                        AppDatabase.getDBInstance()!!.shopActivityDao().updateDurationAvailable(true, shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi(), shopList[i].startTimeStamp)
                    return
                }

                if (!Pref.isMultipleVisitEnable) {
                    AppDatabase.getDBInstance()!!.shopActivityDao().updateEndTimeOfShop(endTimeStamp, shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi())
                    AppDatabase.getDBInstance()!!.shopActivityDao().updateTotalMinuteForDayOfShop(shopList[i].shopid!!, totalMinute, AppUtils.getCurrentDateForShopActi())
                    AppDatabase.getDBInstance()!!.shopActivityDao().updateTimeDurationForDayOfShop(shopList[i].shopid!!, duration, AppUtils.getCurrentDateForShopActi())
                    AppDatabase.getDBInstance()!!.shopActivityDao().updateDurationAvailable(true, shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi())
                }
                else {
                    AppDatabase.getDBInstance()!!.shopActivityDao().updateEndTimeOfShop(endTimeStamp, shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi(), shopList[i].startTimeStamp)
                    AppDatabase.getDBInstance()!!.shopActivityDao().updateTotalMinuteForDayOfShop(shopList[i].shopid!!, totalMinute, AppUtils.getCurrentDateForShopActi(), shopList[i].startTimeStamp)
                    AppDatabase.getDBInstance()!!.shopActivityDao().updateTimeDurationForDayOfShop(shopList[i].shopid!!, duration, AppUtils.getCurrentDateForShopActi(), shopList[i].startTimeStamp)
                    AppDatabase.getDBInstance()!!.shopActivityDao().updateDurationAvailable(true, shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi(), shopList[i].startTimeStamp)
                }
                AppDatabase.getDBInstance()!!.shopActivityDao().updateOutTime(AppUtils.getCurrentTimeWithMeredian(), shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi(), shopList[i].startTimeStamp)
                AppDatabase.getDBInstance()!!.shopActivityDao().updateOutLocation(LocationWizard.getNewLocationName(mContext, Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble()), shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi(), shopList[i].startTimeStamp)

                val netStatus = if (AppUtils.isOnline(mContext))
                    "Online"
                else
                    "Offline"

                val netType = if (AppUtils.getNetworkType(mContext).equals("wifi", ignoreCase = true))
                    AppUtils.getNetworkType(mContext)
                else
                    "Mobile ${AppUtils.mobNetType(mContext)}"

                if (!Pref.isMultipleVisitEnable) {
                    AppDatabase.getDBInstance()!!.shopActivityDao().updateDeviceStatusReason(AppUtils.getDeviceName(), AppUtils.getAndroidVersion(),
                            AppUtils.getBatteryPercentage(mContext).toString(), netStatus, netType.toString(), shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi())
                }
                else {
                    AppDatabase.getDBInstance()!!.shopActivityDao().updateDeviceStatusReason(AppUtils.getDeviceName(), AppUtils.getAndroidVersion(),
                            AppUtils.getBatteryPercentage(mContext).toString(), netStatus, netType.toString(), shopList[i].shopid!!, AppUtils.getCurrentDateForShopActi(), shopList[i].startTimeStamp)
                }

                if (Pref.willShowShopVisitReason && totalMinute.toInt() < Pref.minVisitDurationSpentTime.toInt()) {
                    Pref.isShowShopVisitReason = true
                    showRevisitReasonDialog(addShop, shopList[i].startTimeStamp)
                }
            }

        }

        if (!Pref.isShowShopVisitReason)
            startNewVisit(addShop)
    }

    private fun startNewVisit(addShop: AddShopRequestData) {
        val currentDateTime = AppUtils.getCurrentISODateTime()

//        AppDatabase.getDBInstance()!!.shopActivityDao().updateDurationCalculatedStatus(true, AppUtils.getCurrentDateForShopActi())
        val shopActivityEntity = ShopActivityEntity()
        shopActivityEntity.shopid = addShop.shop_id
        shopActivityEntity.shop_name = addShop.shop_name
        shopActivityEntity.shop_address = addShop.address
        shopActivityEntity.date = AppUtils.getCurrentDateForShopActi()
        shopActivityEntity.duration_spent = "00:00:00"
        shopActivityEntity.visited_date = currentDateTime
        shopActivityEntity.isUploaded = false
        shopActivityEntity.isVisited = true
        shopActivityEntity.isDurationCalculated = false
        shopActivityEntity.startTimeStamp = System.currentTimeMillis().toString()

        if (!TextUtils.isEmpty(feedback_EDT.text.toString().trim()))
            shopActivityEntity.feedback = feedback_EDT.text.toString().trim()

        shopActivityEntity.next_visit_date = nextVisitDate

        var distance = 0.0
        Timber.e("======New Distance (At add shop time)=========")

        val shop = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopDetail(addShop.shop_id)

        if (Pref.isOnLeave.equals("false", ignoreCase = true)) {

            Timber.e("=====User is at work (At add shop time)=======")

            /*if (!TextUtils.isEmpty(addShop.shop_lat) && !TextUtils.isEmpty(addShop.shop_long)) {
                if (!TextUtils.isEmpty(Pref.source_latitude) && !TextUtils.isEmpty(Pref.source_longitude)) {
                    distance = LocationWizard.getDistance(Pref.source_latitude.toDouble(), Pref.source_longitude.toDouble(),
                            addShop.shop_lat?.toDouble()!!, addShop.shop_long?.toDouble()!!)

                    Timber.e("=====Both location available=======")
                } else {
                    distance = 0.0 //LocationWizard.getDistance(0.0, 0.0, addShop.shop_lat?.toDouble()!!, addShop.shop_long?.toDouble()!!)
                    Timber.e("=====Only new location available=======")
                }
                Pref.source_latitude = addShop.shop_lat!!
                Pref.source_longitude = addShop.shop_long!!
            } else {
                if (!TextUtils.isEmpty(Pref.source_latitude) && !TextUtils.isEmpty(Pref.source_longitude)) {
                    distance = 0.0 //LocationWizard.getDistance(0.0, 0.0, Pref.source_latitude.toDouble(), Pref.source_longitude.toDouble())
                    Timber.e("=====Only old location available=======")
                } else {
                    distance = 0.0

                    Timber.e("=====No location available=======")
                }
            }*/

            var locationList = AppDatabase.getDBInstance()!!.userLocationDataDao().getLocationUpdateForADay(AppUtils.getCurrentDateForShopActi())

            //val distance = LocationWizard.getDistance(shop.shopLat, shop.shopLong, location.latitude, location.longitude)

            val userlocation = UserLocationDataEntity()
            userlocation.latitude = shop.shopLat.toString()
            userlocation.longitude = shop.shopLong.toString()

            var loc_distance = 0.0
            var finalDistance = ""

            try{
                 loc_distance = LocationWizard.getDistance(locationList[locationList.size - 1].latitude.toDouble(), locationList[locationList.size - 1].longitude.toDouble(),
                        userlocation.latitude.toDouble(), userlocation.longitude.toDouble())
                 finalDistance = (Pref.tempDistance.toDouble() + loc_distance).toString()
            }catch (ex:Exception){
                ex.printStackTrace()
                loc_distance=0.0
                finalDistance="0.0"
            }

        /*    val loc_distance = LocationWizard.getDistance(locationList[locationList.size - 1].latitude.toDouble(), locationList[locationList.size - 1].longitude.toDouble(),
                    userlocation.latitude.toDouble(), userlocation.longitude.toDouble())
            val finalDistance = (Pref.tempDistance.toDouble() + loc_distance).toString()*/


            Timber.e("===Distance (At new shop visit time)===")
            Timber.e("Temp Distance====> " + Pref.tempDistance)
            Timber.e("Normal Distance====> $loc_distance")
            Timber.e("Total Distance====> $finalDistance")
            Timber.e("=======================================")

            userlocation.distance = finalDistance
            userlocation.locationName = LocationWizard.getNewLocationName(mContext, userlocation.latitude.toDouble(), userlocation.longitude.toDouble())
            userlocation.timestamp = LocationWizard.getTimeStamp()
            userlocation.time = LocationWizard.getFormattedTime24Hours(true)
            userlocation.meridiem = LocationWizard.getMeridiem()
            userlocation.hour = LocationWizard.getHour()
            userlocation.minutes = LocationWizard.getMinute()
            userlocation.isUploaded = false
            userlocation.shops = AppDatabase.getDBInstance()!!.shopActivityDao().getTotalShopVisitedForADay(AppUtils.getCurrentDateForShopActi()).size.toString()
            userlocation.updateDate = AppUtils.getCurrentDateForShopActi()
            userlocation.updateDateTime = AppUtils.getCurrentDateTime()
            userlocation.meeting = AppDatabase.getDBInstance()!!.addMeetingDao().getMeetingDateWise(AppUtils.getCurrentDateForShopActi()).size.toString()
            userlocation.network_status = if (AppUtils.isOnline(mContext)) "Online" else "Offline"
            userlocation.battery_percentage = AppUtils.getBatteryPercentage(mContext).toString()

            //harcoded location isUploaded true begin
            userlocation.isUploaded = true
            //harcoded location isUploaded true end

            AppDatabase.getDBInstance()!!.userLocationDataDao().insertAll(userlocation)

            Timber.e("=====New shop visit data added=======")

            Pref.totalS2SDistance = (Pref.totalS2SDistance.toDouble() + userlocation.distance.toDouble()).toString()

            distance = Pref.totalS2SDistance.toDouble()
            Pref.totalS2SDistance = "0.0"
            Pref.tempDistance = "0.0"
        } else {
            Timber.e("=====User is on leave =======")
            distance = 0.0
        }

        Timber.e("shop to shop distance (At new shop visit time)====> $distance")

        shopActivityEntity.distance_travelled = distance.toString()

        val todaysVisitedShop = AppDatabase.getDBInstance()!!.shopActivityDao().getTotalShopVisitedForADay(AppUtils.getCurrentDateForShopActi())

        if (todaysVisitedShop == null || todaysVisitedShop.isEmpty()) {
            shopActivityEntity.isFirstShopVisited = true

            if (!TextUtils.isEmpty(Pref.home_latitude) && !TextUtils.isEmpty(Pref.home_longitude)) {
                val distance = LocationWizard.getDistance(Pref.home_latitude.toDouble(), Pref.home_longitude.toDouble(),
                        addShop.shop_lat?.toDouble()!!, addShop.shop_long?.toDouble()!!)
                shopActivityEntity.distance_from_home_loc = distance.toString()
            } else
                shopActivityEntity.distance_from_home_loc = "0.0"
        } else {
            shopActivityEntity.isFirstShopVisited = false
            shopActivityEntity.distance_from_home_loc = ""
        }
        shopActivityEntity.in_time = AppUtils.getCurrentTimeWithMeredian()
        shopActivityEntity.in_loc = shop.actual_address
        shopActivityEntity.shop_revisit_uniqKey=finalUniqKey?.toString()

        //Begin Rev 1.0 Suman 10-07-2023 IsnewShop in api+room mantis id 26537
        shopActivityEntity.isNewShop = true
        //End Rev 1.0 Suman 10-07-2023 IsnewShop in api+room mantis id 26537

        try {
            shopActivityEntity.device_model = AppUtils.getDeviceName()
            shopActivityEntity.android_version = AppUtils.getAndroidVersion()
            shopActivityEntity.battery = AppUtils.getBatteryPercentage(mContext).toString()
            shopActivityEntity.net_status = if(AppUtils.isOnline(mContext)) "Online" else "Offline"
            shopActivityEntity.net_type = AppUtils.getNetworkType(mContext)
        }catch (ex:Exception){
            ex.printStackTrace()
        }

        AppDatabase.getDBInstance()!!.shopActivityDao().insertAll(shopActivityEntity)

//        AppUtils.isShopVisited = true

        Pref.isShopVisited=true
        val performance = AppDatabase.getDBInstance()!!.performanceDao().getTodaysData(AppUtils.getCurrentDateForShopActi())
        if (performance != null) {
            val list = AppDatabase.getDBInstance()!!.shopActivityDao().getDurationCalculatedVisitedShopForADay(AppUtils.getCurrentDateForShopActi(), true)
            AppDatabase.getDBInstance()!!.performanceDao().updateTotalShopVisited(list.size.toString(), AppUtils.getCurrentDateForShopActi())
            var totalTimeSpentForADay = 0
            for (i in list.indices) {
                totalTimeSpentForADay += list[i].totalMinute.toInt()
            }
            AppDatabase.getDBInstance()!!.performanceDao().updateTotalDuration(totalTimeSpentForADay.toString(), AppUtils.getCurrentDateForShopActi())
        } else {
            val list = AppDatabase.getDBInstance()!!.shopActivityDao().getDurationCalculatedVisitedShopForADay(AppUtils.getCurrentDateForShopActi(), true)
            val performanceEntity = PerformanceEntity()
            performanceEntity.date = AppUtils.getCurrentDateForShopActi()
            performanceEntity.total_shop_visited = list.size.toString()
            var totalTimeSpentForADay = 0
            for (i in list.indices) {
                totalTimeSpentForADay += list[i].totalMinute.toInt()
            }
            performanceEntity.total_duration_spent = totalTimeSpentForADay.toString()
            AppDatabase.getDBInstance()!!.performanceDao().insert(performanceEntity)
        }

        if (Pref.isRecordAudioEnable) {
            val shopVisitAudio = ShopVisitAudioEntity()
            AppDatabase.getDBInstance()?.shopVisitAudioDao()?.insert(shopVisitAudio.apply {
                shop_id = shopDataModel.shop_id
                isUploaded = false
                audio = filePath
                visit_datetime = currentDateTime
            })
        }
    }

    private fun showRevisitReasonDialog(addShop: AddShopRequestData, startTimeStamp: String) {
        val shop = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(Pref.durationCompletedShopId)
        reasonDialog = ReasonDialog.getInstance(shop?.shopName!!, "You are revisiting ${Pref.shopText} but the " +
                "duration spent is less than ${Pref.minVisitDurationSpentTime} minutes. Please write the reason below.", "") {
            reasonDialog?.dismiss()
            Pref.isShowShopVisitReason = false

            if (!Pref.isMultipleVisitEnable)
                AppDatabase.getDBInstance()!!.shopActivityDao().updateEarlyRevisitReason(it, Pref.durationCompletedShopId, AppUtils.getCurrentDateForShopActi())
            else
                AppDatabase.getDBInstance()!!.shopActivityDao().updateEarlyRevisitReason(it, Pref.durationCompletedShopId, AppUtils.getCurrentDateForShopActi(), startTimeStamp)

            startNewVisit(addShop)
        }
        reasonDialog?.show((mContext as DashboardActivity).supportFragmentManager, "")
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.shop_large_IMG -> {
                /* if (PermissionHelper.checkCameraPermission(mContext as DashboardActivity) && PermissionHelper.checkStoragePermission(mContext as DashboardActivity)) {
                     val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                     intent.putExtra(MediaStore.EXTRA_OUTPUT, (mContext as DashboardActivity).getPhotoFileUri(System.currentTimeMillis().toString() + ".png"))
                     (mContext as DashboardActivity).startActivityForResult(intent, PermissionHelper.REQUEST_CODE_CAMERA)
                 }*/
                isDocDegree = 0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    initPermissionCheck()
                else {
                    //showPictureDialog()
                    launchCamera()
                }
            }

            R.id.attachment_EDT -> {
                isDocDegree = 1
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    initPermissionCheck()
                else {
                    showPictureDialog()
                }
            }

            R.id.save_TV -> {
                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
//                validateAndSaveData()
                if (Pref.user_id.isNullOrBlank()) {
                    (mContext as DashboardActivity).clearData()
                    startActivity(Intent(mContext as DashboardActivity, LoginActivity::class.java))
                    (mContext as DashboardActivity).overridePendingTransition(0, 0)
                    (mContext as DashboardActivity).finish()
                    return
                }


                if (AppUtils.isAutoRevisit) {
                    (mContext as DashboardActivity).showSnackMessage("Auto Revisit just started")
                    return
                }

                if (BaseActivity.isApiInitiated)
                    return
                BaseActivity.isApiInitiated = true
//                saveTV.isEnabled=false

                if (TextUtils.isEmpty(mLatitude) && TextUtils.isEmpty(mLongitude)) {
                    //updateshoplocation(Pref.latitude!!.toDouble(), Pref.longitude!!.toDouble())
                    updateshoplocation(mLocation?.latitude!!, mLocation?.longitude!!)
                } else
                    updateshoplocation(mLatitude.toDouble(), mLongitude.toDouble())

                //updateshoplocation(0.0, 0.0)


                //(mContext as DashboardActivity).loadFragment(FragType.SearchLocationFragment, true, "")


            }

            R.id.shop_type_RL -> {
                val shopTypeList = AppDatabase.getDBInstance()?.shopTypeDao()?.getAll()
                if (shopTypeList == null || shopTypeList.isEmpty())
                    getShopTypeListApi(shop_type_RL, false)
                else
                    initShopTypePopUp(shop_type_RL)
            }
            R.id.assign_to_rl -> {
                /* val mAssignedList: ArrayList<String> = ArrayList()
                 doAsync {
                     AppDatabase.getDBInstance()?.ppListDao()?.delete()
                     for (i in 0..20) {
                         mAssignedList.add("test" + i)
                         val assignToPP = AssignToPPEntity()
                         assignToPP.pp_id = "i"
                         assignToPP.pp_name = "test" + i
                         AppDatabase.getDBInstance()?.ppListDao()?.insert(assignToPP)
                     }

                     uiThread {
                         showAssignedToPPDialog(mAssignedList)
                     }
                 }*/


                val assignPPList = AppDatabase.getDBInstance()?.ppListDao()?.getAll()
                if (assignPPList == null || assignPPList.isEmpty()) {
                    if (!TextUtils.isEmpty(Pref.profile_state)) {
                        if (AppUtils.isOnline(mContext))
                            getAssignedPPListApi(false, "")
                        else
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
                    } else {
                        showProfileAlert()
                    }
                } else {
                    showAssignedToPPDialog(assignPPList, addShopData.type)
                }

                //callThemePopUp(assign_to_rl, mAssignedList)
                //showAssignedToPPDialog(mAssignedList)
            }
            R.id.dob_EDT -> {
                isDOB = 0
                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                var datepickerDialog = DatePickerDialog(mContext, R.style.DatePickerTheme, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH))
                datepickerDialog.datePicker.maxDate = Calendar.getInstance(Locale.ENGLISH).timeInMillis
                datepickerDialog.show()
            }
            R.id.date_aniverdary_EDT -> {
                isDOB = 1
                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                var aniDatePicker = DatePickerDialog(mContext, R.style.DatePickerTheme, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH))
                aniDatePicker.datePicker.maxDate = Calendar.getInstance(Locale.ENGLISH).timeInMillis
                aniDatePicker.show()
            }

            R.id.next_visit_date_EDT -> {
                isDOB = 2
                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                val aniDatePicker = DatePickerDialog(mContext, R.style.DatePickerTheme, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH))
                aniDatePicker.datePicker.minDate = Calendar.getInstance(Locale.ENGLISH).timeInMillis + (1000 * 60 * 60 * 24)
                aniDatePicker.datePicker
                aniDatePicker.show()
            }

            R.id.family_mem_dob_EDT -> {
                isDOB = 3
                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                val datepickerDialog = DatePickerDialog(mContext, R.style.DatePickerTheme, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH))
                datepickerDialog.datePicker.maxDate = Calendar.getInstance(Locale.ENGLISH).timeInMillis
                datepickerDialog.show()
            }

            R.id.add_dob_EDT -> {
                isDOB = 4
                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                val datepickerDialog = DatePickerDialog(mContext, R.style.DatePickerTheme, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH))
                datepickerDialog.datePicker.maxDate = Calendar.getInstance(Locale.ENGLISH).timeInMillis
                datepickerDialog.show()
            }

            R.id.add_date_aniverdary_EDT -> {
                isDOB = 5
                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                val datepickerDialog = DatePickerDialog(mContext, R.style.DatePickerTheme, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH))
                datepickerDialog.datePicker.maxDate = Calendar.getInstance(Locale.ENGLISH).timeInMillis
                datepickerDialog.show()
            }

            R.id.doc_family_mem_dob_EDT -> {
                isDOB = 6
                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                val datepickerDialog = DatePickerDialog(mContext, R.style.DatePickerTheme, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH))
                datepickerDialog.datePicker.maxDate = Calendar.getInstance(Locale.ENGLISH).timeInMillis
                datepickerDialog.show()
            }

            R.id.assistant_dob_EDT -> {
                isDOB = 7
                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                val datepickerDialog = DatePickerDialog(mContext, R.style.DatePickerTheme, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH))
                datepickerDialog.datePicker.maxDate = Calendar.getInstance(Locale.ENGLISH).timeInMillis
                datepickerDialog.show()
            }

            R.id.assistant_date_aniverdary_EDT -> {
                isDOB = 8
                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                val datepickerDialog = DatePickerDialog(mContext, R.style.DatePickerTheme, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH))
                datepickerDialog.datePicker.maxDate = Calendar.getInstance(Locale.ENGLISH).timeInMillis
                datepickerDialog.show()
            }

            R.id.assistant_family_mem_dob_EDT -> {
                isDOB = 9
                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                val datepickerDialog = DatePickerDialog(mContext, R.style.DatePickerTheme, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH))
                datepickerDialog.datePicker.maxDate = Calendar.getInstance(Locale.ENGLISH).timeInMillis
                datepickerDialog.show()
            }

            R.id.rl_assign_to_dd -> {
                /*val mAssignedList: ArrayList<String> = ArrayList()
                doAsync {
                    AppDatabase.getDBInstance()?.ddListDao()?.delete()
                    for (i in 0..20) {
                        mAssignedList.add("test" + i)
                        val assignToDD = AssignToDDEntity()
                        assignToDD.dd_id = "i"
                        assignToDD.dd_name = "test" + i
                        AppDatabase.getDBInstance()?.ddListDao()?.insert(assignToDD)
                    }

                    uiThread {
                        showAssignedToDDDialog(mAssignedList)
                    }
                }*/

                val assignDDList = AppDatabase.getDBInstance()?.ddListDao()?.getAll()
                if (assignDDList == null || assignDDList.isEmpty()) {
                    if (!TextUtils.isEmpty(Pref.profile_state)) {
                        if (AppUtils.isOnline(mContext))
                            getAssignedDDListApi(false, "")
                        else
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
                    } else {
                        showProfileAlert()
                    }
                } else {
                    /*if (!TextUtils.isEmpty(assignedToPPId)) {
                        val list = AppDatabase.getDBInstance()?.ddListDao()?.getValuePPWise(assignedToPPId)
                        showAssignedToDDDialog(list)
                    }
                    else {
                        (mContext as DashboardActivity).showSnackMessage(getString(R.string.select_pp))
                    }*/
                    if (dealerId.isNotEmpty()) {
                        val list = AppDatabase.getDBInstance()?.ddListDao()?.getValueTypeWise(dealerId)
                        if (list != null && list.isNotEmpty())
                            showAssignedToDDDialog(list)
                        else
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_data_found))
                    }
                    else
                        showAssignedToDDDialog(assignDDList)
                }
                //callThemePopUp(assign_to_rl, mAssignedList)

            }

            R.id.rl_area -> {

                val areaList = AppDatabase.getDBInstance()?.areaListDao()?.getAll() as ArrayList<AreaListEntity>

                if (areaList == null || areaList.isEmpty()) {
                    if (!TextUtils.isEmpty(Pref.profile_city)) {
                        if (AppUtils.isOnline(mContext))
                            getAreaListApi()
                        else
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
                    } else {
                        showProfileAlert()
                    }
                } else
                    showAreaDialog(areaList)
            }

            R.id.tv_model -> {

                val list = AppDatabase.getDBInstance()?.modelListDao()?.getAll() as ArrayList<ModelEntity>

                if (list == null || list.isEmpty())
                    getModelListApi()
                else
                    showModelDialog(list)
            }

            R.id.iv_model_dropdown -> {

                val list = AppDatabase.getDBInstance()?.modelListDao()?.getAll() as ArrayList<ModelEntity>

                if (list == null || list.isEmpty())
                    getModelListApi()
                else
                    showModelDialog(list)
            }

            R.id.tv_primary_app -> {

                val list = AppDatabase.getDBInstance()?.primaryAppListDao()?.getAll() as ArrayList<PrimaryAppEntity>

                if (list == null || list.isEmpty())
                    getPrimaryAppListApi()
                else
                    showPrimaryAppDialog(list)
            }

            R.id.iv_primary_app_dropdown -> {

                val list = AppDatabase.getDBInstance()?.primaryAppListDao()?.getAll() as ArrayList<PrimaryAppEntity>

                if (list == null || list.isEmpty())
                    getPrimaryAppListApi()
                else
                    showPrimaryAppDialog(list)
            }

            R.id.tv_secondary_app -> {

                val list = AppDatabase.getDBInstance()?.secondaryAppListDao()?.getAll() as ArrayList<SecondaryAppEntity>

                if (list == null || list.isEmpty())
                    geSecondaryAppListApi()
                else
                    showSecondaryyAppDialog(list)
            }

            R.id.iv_secondary_app_dropdown -> {

                val list = AppDatabase.getDBInstance()?.secondaryAppListDao()?.getAll() as ArrayList<SecondaryAppEntity>

                if (list == null || list.isEmpty())
                    geSecondaryAppListApi()
                else
                    showSecondaryyAppDialog(list)
            }

            R.id.tv_lead_type -> {

                val list = AppDatabase.getDBInstance()?.leadTypeDao()?.getAll() as ArrayList<LeadTypeEntity>

                if (list == null || list.isEmpty())
                    geLeadApi()
                else
                    showLeadDialog(list)
            }

            R.id.iv_lead_type_dropdown -> {

                val list = AppDatabase.getDBInstance()?.leadTypeDao()?.getAll() as ArrayList<LeadTypeEntity>

                if (list == null || list.isEmpty())
                    geLeadApi()
                else
                    showLeadDialog(list)
            }

            R.id.tv_stage -> {

                val list = AppDatabase.getDBInstance()?.stageDao()?.getAll() as ArrayList<StageEntity>

                if (list == null || list.isEmpty())
                    geStageApi()
                else
                    showStageDialog(list)
            }

            R.id.iv_stage_dropdown -> {

                val list = AppDatabase.getDBInstance()?.stageDao()?.getAll() as ArrayList<StageEntity>

                if (list == null || list.isEmpty())
                    geStageApi()
                else
                    showStageDialog(list)
            }

            R.id.tv_funnel_stage -> {

                val list = AppDatabase.getDBInstance()?.funnelStageDao()?.getAll() as ArrayList<FunnelStageEntity>

                if (list == null || list.isEmpty())
                    geFunnelStageApi()
                else
                    showFunnelStageDialog(list)
            }

            R.id.iv_funnel_stage_dropdown -> {

                val list = AppDatabase.getDBInstance()?.funnelStageDao()?.getAll() as ArrayList<FunnelStageEntity>

                if (list == null || list.isEmpty())
                    geFunnelStageApi()
                else
                    showFunnelStageDialog(list)
            }

            R.id.rl_type -> {
                val typeList = AppDatabase.getDBInstance()?.typeListDao()?.getAll() as ArrayList<TypeListEntity>
                if (typeList != null && typeList.isNotEmpty())
                    showTypeDialog(typeList)
                else
                    getTypeListApi(false)
            }

            R.id.audio_record_date_EDT -> {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    initRecorderPermissionCheck()
                else
                    openRecorder()
            }

            R.id.ll_yes -> {
                if (!iv_yes.isSelected) {
                    iv_yes.isSelected = true
                    iv_no.isSelected = false
                    til_remarks.visibility = View.VISIBLE
                }
            }

            R.id.ll_no -> {
                if (!iv_no.isSelected) {
                    iv_yes.isSelected = false
                    iv_no.isSelected = true
                    til_remarks.visibility = View.VISIBLE
                }
            }

            R.id.rl_party -> {
                val list = AppDatabase.getDBInstance()?.partyStatusDao()?.getAll() as ArrayList<PartyStatusEntity>
                if (list != null && list.isNotEmpty())
                    showPartyStatusDialog(list)
                else
                    getPartyStatusListApi(false)
            }

            R.id.rl_entity -> {
                val list = AppDatabase.getDBInstance()?.entityDao()?.getAll() as ArrayList<EntityTypeEntity>
                if (list != null && list.isNotEmpty())
                    showEntityDialog(list)
                else
                    getEntityTypeListApi(false)
            }

            R.id.rl_select_retailer -> {
                val list = AppDatabase.getDBInstance()?.retailerDao()?.getAll() as ArrayList<RetailerEntity>
                if (list != null && list.isNotEmpty()) {
                    if (addShopData.type != "11") {
                        if (dealerId.isNotEmpty()) {
                            val list_ = AppDatabase.getDBInstance()?.retailerDao()?.getItemTypeWise(dealerId) as java.util.ArrayList<RetailerEntity>
                            if (list_ != null && list_.isNotEmpty())
                                showRetailerListDialog(list_)
                            else
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_data_found))
                        } else
                            showRetailerListDialog(AppDatabase.getDBInstance()?.retailerDao()?.getAll() as ArrayList<RetailerEntity>)
                    }
                    else if (addShopData.type == "11") {
                        val list_ = AppDatabase.getDBInstance()?.retailerDao()?.getAll()?.filter {
                            it.retailer_id == "2"
                        }

                        if (list_ != null && list_.isNotEmpty())
                            showRetailerListDialog(list_ as ArrayList<RetailerEntity>)
                        else
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_data_found))
                    }
                    else
                        showRetailerListDialog(list)
                }
                else
                    getRetailerListApi(false)
            }

            R.id.rl_select_dealer -> {
                val list = AppDatabase.getDBInstance()?.dealerDao()?.getAll() as ArrayList<DealerEntity>
                if (list != null && list.isNotEmpty())
                    showDealerListDialog(list)
                else
                    getDealerListApi(false)
            }

            R.id.rl_select_beat -> {
                val list = AppDatabase.getDBInstance()?.beatDao()?.getAll() as ArrayList<BeatEntity>
                if (list != null && list.isNotEmpty())
                    showBeatListDialog(list)
                else
                    getBeatListApi(false)
            }

            R.id.assign_to_shop_rl -> {
                val list = AppDatabase.getDBInstance()?.assignToShopDao()?.getAll() as ArrayList<AssignToShopEntity>
                if (list != null && list.isNotEmpty()) {
                    if (retailerId.isNotEmpty()) {
                        val list_ = AppDatabase.getDBInstance()?.assignToShopDao()?.getValueTypeWise(retailerId) as ArrayList<AssignToShopEntity>
                        if (list_ != null && list_.isNotEmpty())
                            showAssignedToShopListDialog(list_)
                        else
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_data_found))
                    }
                    else
                        showAssignedToShopListDialog(list)
                }
                else {
                    if (!TextUtils.isEmpty(Pref.profile_state)) {
                        if (AppUtils.isOnline(mContext))
                            getAssignedToShopApi(false, "")
                        else
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
                    }
                    else
                        showProfileAlert()
                }
            }

            R.id.ll_competitor_image ->{
                isDocDegree = 2
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    initPermissionCheckOne()
                else
                    showPictureDialog()
            }
        }
    }

    private fun initRecorderPermissionCheck() {

        //begin mantis id 26741 Storage permission updation Suman 22-08-2023
        var permissionList = arrayOf<String>(Manifest.permission.RECORD_AUDIO)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            permissionList += Manifest.permission.READ_MEDIA_IMAGES
            permissionList += Manifest.permission.READ_MEDIA_AUDIO
            permissionList += Manifest.permission.READ_MEDIA_VIDEO
        }else{
            permissionList += Manifest.permission.WRITE_EXTERNAL_STORAGE
            permissionList += Manifest.permission.READ_EXTERNAL_STORAGE
        }
//end mantis id 26741 Storage permission updation Suman 22-08-2023

        permissionUtils = PermissionUtils(mContext as Activity, object : PermissionUtils.OnPermissionListener {
            override fun onPermissionGranted() {
                openRecorder()
            }

            override fun onPermissionNotGranted() {
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.accept_permission))
            }

        },permissionList)// arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO))
    }

    private var audioFile: File? = null
    private fun openRecorder() {
        val folderPath = FTStorageUtils.getFolderPath(mContext)
        audioFile = File("$folderPath/" + System.currentTimeMillis() + ".mp3")

        AndroidAudioRecorder.with(mContext as DashboardActivity)
                // Required
                .setFilePath(audioFile?.absolutePath)
                .setColor(ContextCompat.getColor(mContext, R.color.colorPrimary))
                .setRequestCode(PermissionHelper.REQUEST_CODE_AUDIO)
                .setAutoStart(false)
                .setKeepDisplayOn(true)

                // Start recording
                .record()
    }

    private fun getModelListApi() {

        if (!AppUtils.isOnline(mContext)) {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
            return
        }

        progress_wheel.spin()
        val repository = ShopListRepositoryProvider.provideShopListRepository()
        BaseActivity.compositeDisposable.add(
                //repository.getModelList()
                repository.getModelListNew()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            //val response = result as ModelListResponseModel
                            val response = result as ModelListResponse
                            Timber.d("GET MODEL DATA : " + "RESPONSE : " + response.status + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + response.message)
                            if (response.status == NetworkConstant.SUCCESS) {

                                if (response.model_list != null && response.model_list!!.isNotEmpty()) {

                                    doAsync {

                                        AppDatabase.getDBInstance()?.modelListDao()?.insertAllLarge(response.model_list!!)

                                 /*       response.model_list?.forEach {
                                            val modelEntity = ModelEntity()
                                            AppDatabase.getDBInstance()?.modelListDao()?.insertAll(modelEntity.apply {
                                                model_id = it.id
                                                model_name = it.name
                                            })
                                        }*/

                                        uiThread {
                                            progress_wheel.stopSpinning()
                                            showModelDialog(AppDatabase.getDBInstance()?.modelListDao()?.getAll() as ArrayList<ModelEntity>)
                                        }
                                    }
                                } else {
                                    progress_wheel.stopSpinning()
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                }


                            } else {
                                progress_wheel.stopSpinning()
                                (mContext as DashboardActivity).showSnackMessage(response.message!!)
                            }

                        }, { error ->
                            progress_wheel.stopSpinning()
                            Timber.d("GET MODEL DATA : " + "ERROR : " + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + error.localizedMessage)
                            error.printStackTrace()
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                        })
        )
    }

    private fun showModelDialog(modelList: ArrayList<ModelEntity>) {
        ModelListDialog.newInstance(modelList) { model: ModelEntity ->
            tv_model.text = model.model_name
            modelId = model.model_id!!
            clearFocus()
        }.show((mContext as DashboardActivity).supportFragmentManager, "")
    }


    private fun getPrimaryAppListApi() {

        if (!AppUtils.isOnline(mContext)) {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
            return
        }

        progress_wheel.spin()
        val repository = ShopListRepositoryProvider.provideShopListRepository()
        BaseActivity.compositeDisposable.add(
                repository.getPrimaryAppList()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as PrimaryAppListResponseModel
                            Timber.d("GET PRIMARY APP DATA : " + "RESPONSE : " + response.status + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + response.message)
                            if (response.status == NetworkConstant.SUCCESS) {

                                if (response.primary_application_list != null && response.primary_application_list!!.isNotEmpty()) {

                                    doAsync {

                                        response.primary_application_list?.forEach {
                                            val primaryEntity = PrimaryAppEntity()
                                            AppDatabase.getDBInstance()?.primaryAppListDao()?.insertAll(primaryEntity.apply {
                                                primary_app_id = it.id
                                                primary_app_name = it.name
                                            })
                                        }

                                        uiThread {
                                            progress_wheel.stopSpinning()
                                            showPrimaryAppDialog(AppDatabase.getDBInstance()?.primaryAppListDao()?.getAll() as ArrayList<PrimaryAppEntity>)
                                        }
                                    }
                                } else {
                                    progress_wheel.stopSpinning()
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                }


                            } else {
                                progress_wheel.stopSpinning()
                                (mContext as DashboardActivity).showSnackMessage(response.message!!)
                            }

                        }, { error ->
                            progress_wheel.stopSpinning()
                            Timber.d("GET PRIMARY APP DATA : " + "ERROR : " + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + error.localizedMessage)
                            error.printStackTrace()
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                        })
        )
    }

    private fun showPrimaryAppDialog(primaryAppList: ArrayList<PrimaryAppEntity>) {
        PrimaryAppListDialog.newInstance(primaryAppList) { model: PrimaryAppEntity ->
            tv_primary_app.text = model.primary_app_name
            primaryAppId = model.primary_app_id!!
            clearFocus()
        }.show((mContext as DashboardActivity).supportFragmentManager, "")
    }


    private fun geSecondaryAppListApi() {

        if (!AppUtils.isOnline(mContext)) {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
            return
        }

        progress_wheel.spin()
        val repository = ShopListRepositoryProvider.provideShopListRepository()
        BaseActivity.compositeDisposable.add(
                repository.getSecondaryAppList()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as SecondaryAppListResponseModel
                            Timber.d("GET SECONDARY APP DATA : " + "RESPONSE : " + response.status + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + response.message)
                            if (response.status == NetworkConstant.SUCCESS) {

                                if (response.secondary_application_list != null && response.secondary_application_list!!.isNotEmpty()) {

                                    doAsync {

                                        response.secondary_application_list?.forEach {
                                            val secondaryEntity = SecondaryAppEntity()
                                            AppDatabase.getDBInstance()?.secondaryAppListDao()?.insertAll(secondaryEntity.apply {
                                                secondary_app_id = it.id
                                                secondary_app_name = it.name
                                            })
                                        }

                                        uiThread {
                                            progress_wheel.stopSpinning()
                                            showSecondaryyAppDialog(AppDatabase.getDBInstance()?.secondaryAppListDao()?.getAll() as ArrayList<SecondaryAppEntity>)
                                        }
                                    }
                                } else {
                                    progress_wheel.stopSpinning()
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                }


                            } else {
                                progress_wheel.stopSpinning()
                                (mContext as DashboardActivity).showSnackMessage(response.message!!)
                            }

                        }, { error ->
                            progress_wheel.stopSpinning()
                            Timber.d("GET SECONDARY APP DATA : " + "ERROR : " + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + error.localizedMessage)
                            error.printStackTrace()
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                        })
        )
    }

    private fun showSecondaryyAppDialog(secondaryAppList: ArrayList<SecondaryAppEntity>) {
        SecondaryAppListDialog.newInstance(secondaryAppList) { secondary: SecondaryAppEntity ->
            tv_secondary_app.text = secondary.secondary_app_name
            secondaryAppId = secondary.secondary_app_id!!
            clearFocus()
        }.show((mContext as DashboardActivity).supportFragmentManager, "")
    }

    private fun clearFocus() {
        shop_name_EDT.clearFocus()
        shopAddress.clearFocus()
        shopPin.clearFocus()
        ownerName.clearFocus()
        ownerNumber.clearFocus()
        ownerEmail.clearFocus()
        amount_EDT.clearFocus()
        booking_amount_EDT.clearFocus()
        feedback_EDT.clearFocus()
    }

    private fun geLeadApi() {

        if (!AppUtils.isOnline(mContext)) {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
            return
        }

        progress_wheel.spin()
        val repository = ShopListRepositoryProvider.provideShopListRepository()
        BaseActivity.compositeDisposable.add(
                repository.getLeadTypeList()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as LeadListResponseModel
                            Timber.d("GET LEAD TYPE DATA : " + "RESPONSE : " + response.status + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + response.message)
                            if (response.status == NetworkConstant.SUCCESS) {

                                if (response.lead_type_list != null && response.lead_type_list!!.isNotEmpty()) {

                                    doAsync {

                                        response.lead_type_list?.forEach {
                                            val leadEntity = LeadTypeEntity()
                                            AppDatabase.getDBInstance()?.leadTypeDao()?.insertAll(leadEntity.apply {
                                                lead_id = it.id
                                                lead_name = it.name
                                            })
                                        }

                                        uiThread {
                                            progress_wheel.stopSpinning()
                                            showLeadDialog(AppDatabase.getDBInstance()?.leadTypeDao()?.getAll() as ArrayList<LeadTypeEntity>)
                                        }
                                    }
                                } else {
                                    progress_wheel.stopSpinning()
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                }


                            } else {
                                progress_wheel.stopSpinning()
                                (mContext as DashboardActivity).showSnackMessage(response.message!!)
                            }

                        }, { error ->
                            progress_wheel.stopSpinning()
                            Timber.d("GET LEAD TYPE DATA : " + "ERROR : " + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + error.localizedMessage)
                            error.printStackTrace()
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                        })
        )
    }

    private fun showLeadDialog(leadList: ArrayList<LeadTypeEntity>) {
        LeadListDialog.newInstance(leadList) { lead: LeadTypeEntity ->
            tv_lead_type.text = lead.lead_name
            leadTypeId = lead.lead_id!!
            clearFocus()
        }.show((mContext as DashboardActivity).supportFragmentManager, "")
    }

    private fun geStageApi() {

        if (!AppUtils.isOnline(mContext)) {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
            return
        }

        progress_wheel.spin()
        val repository = ShopListRepositoryProvider.provideShopListRepository()
        BaseActivity.compositeDisposable.add(
                repository.getStagList()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as StageListResponseModel
                            Timber.d("GET STAGE DATA : " + "RESPONSE : " + response.status + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + response.message)
                            if (response.status == NetworkConstant.SUCCESS) {

                                if (response.stage_list != null && response.stage_list!!.isNotEmpty()) {

                                    doAsync {

                                        response.stage_list?.forEach {
                                            val stageEntity = StageEntity()
                                            AppDatabase.getDBInstance()?.stageDao()?.insertAll(stageEntity.apply {
                                                stage_id = it.id
                                                stage_name = it.name
                                            })
                                        }

                                        uiThread {
                                            progress_wheel.stopSpinning()
                                            showStageDialog(AppDatabase.getDBInstance()?.stageDao()?.getAll() as ArrayList<StageEntity>)
                                        }
                                    }
                                } else {
                                    progress_wheel.stopSpinning()
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                }


                            } else {
                                progress_wheel.stopSpinning()
                                (mContext as DashboardActivity).showSnackMessage(response.message!!)
                            }

                        }, { error ->
                            progress_wheel.stopSpinning()
                            Timber.d("GET STAGE DATA : " + "ERROR : " + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + error.localizedMessage)
                            error.printStackTrace()
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                        })
        )
    }

    private fun showStageDialog(stageList: ArrayList<StageEntity>) {
        StageListDialog.newInstance(stageList) { stage: StageEntity ->
            tv_stage.text = stage.stage_name
            stageId = stage.stage_id!!
            clearFocus()
        }.show((mContext as DashboardActivity).supportFragmentManager, "")
    }


    private fun geFunnelStageApi() {

        if (!AppUtils.isOnline(mContext)) {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
            return
        }

        progress_wheel.spin()
        val repository = ShopListRepositoryProvider.provideShopListRepository()
        BaseActivity.compositeDisposable.add(
                repository.getFunnelStageList()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as FunnelStageListResponseModel
                            Timber.d("GET FUNNEL STAGE DATA : " + "RESPONSE : " + response.status + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + response.message)
                            if (response.status == NetworkConstant.SUCCESS) {

                                if (response.funnel_stage_list != null && response.funnel_stage_list!!.isNotEmpty()) {

                                    doAsync {

                                        response.funnel_stage_list?.forEach {
                                            val funnelStageEntity = FunnelStageEntity()
                                            AppDatabase.getDBInstance()?.funnelStageDao()?.insertAll(funnelStageEntity.apply {
                                                funnel_stage_id = it.id
                                                funnel_stage_name = it.name
                                            })
                                        }

                                        uiThread {
                                            progress_wheel.stopSpinning()
                                            showFunnelStageDialog(AppDatabase.getDBInstance()?.funnelStageDao()?.getAll() as ArrayList<FunnelStageEntity>)
                                        }
                                    }
                                } else {
                                    progress_wheel.stopSpinning()
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                }


                            } else {
                                progress_wheel.stopSpinning()
                                (mContext as DashboardActivity).showSnackMessage(response.message!!)
                            }

                        }, { error ->
                            progress_wheel.stopSpinning()
                            Timber.d("GET FUNNEL STAGE DATA : " + "ERROR : " + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + error.localizedMessage)
                            error.printStackTrace()
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                        })
        )
    }

    private fun showFunnelStageDialog(funnelStageList: ArrayList<FunnelStageEntity>) {
        FunnelStageDialog.newInstance(funnelStageList) { funnelStage: FunnelStageEntity ->
            tv_funnel_stage.text = funnelStage.funnel_stage_name
            funnelStageId = funnelStage.funnel_stage_id!!
            clearFocus()
        }.show((mContext as DashboardActivity).supportFragmentManager, "")
    }

    private fun getShopTypeListApi(shop_type_RL: RelativeLayout, isFromRefresh: Boolean) {

        if (!AppUtils.isOnline(mContext)) {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
            return
        }

        if (isFromRefresh)
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.wait_msg), 1000)

        val repository = ShopListRepositoryProvider.provideShopListRepository()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
                repository.getShopTypeList()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as ShopTypeResponseModel
                            if (response.status == NetworkConstant.SUCCESS) {
                                val list = response.Shoptype_list

                                if (list != null && list.isNotEmpty()) {

                                    if (isFromRefresh)
                                        AppDatabase.getDBInstance()?.shopTypeDao()?.deleteAll()



                                    doAsync {

                                        list.forEach {
                                            val shop = ShopTypeEntity()
                                            AppDatabase.getDBInstance()?.shopTypeDao()?.insertAll(shop.apply {
                                                shoptype_id = it.shoptype_id
                                                shoptype_name = it.shoptype_name
                                            })
                                        }

                                        uiThread {
                                            progress_wheel.stopSpinning()
                                            if (!isFromRefresh)
                                                initShopTypePopUp(shop_type_RL)
                                            else
                                                getTypeListApi(isFromRefresh)
                                        }
                                    }
                                } else {
                                    progress_wheel.stopSpinning()
                                    if (!isFromRefresh)
                                        (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                    else
                                        getTypeListApi(isFromRefresh)
                                }
                            } else if (response.status == NetworkConstant.NO_DATA) {
                                progress_wheel.stopSpinning()
                                if (!isFromRefresh)
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                else
                                    getTypeListApi(isFromRefresh)
                            } else {
                                progress_wheel.stopSpinning()
                                if (!isFromRefresh)
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                else
                                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_msg), 1000)
                            }

                        }, { error ->
                            progress_wheel.stopSpinning()
                            error.printStackTrace()
                            if (!isFromRefresh)
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                            else
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_msg), 1000)
                        })
        )
    }

    private fun getAreaListApi() {
        val repository = AreaListRepoProvider.provideAreaListRepository()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
                repository.areaList(Pref.profile_city, "")
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as AreaListResponseModel
                            if (response.status == NetworkConstant.SUCCESS) {
                                val list = response.area_list

                                if (list != null && list.isNotEmpty()) {

                                    doAsync {

                                        list.forEach {
                                            val area = AreaListEntity()
                                            AppDatabase.getDBInstance()?.areaListDao()?.insert(area.apply {
                                                area_id = it.area_id
                                                area_name = it.area_name
                                            })
                                        }

                                        uiThread {
                                            progress_wheel.stopSpinning()
                                            showAreaDialog(AppDatabase.getDBInstance()?.areaListDao()?.getAll() as ArrayList<AreaListEntity>)
                                        }
                                    }
                                } else {
                                    progress_wheel.stopSpinning()
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                }
                            } else {
                                progress_wheel.stopSpinning()
                                (mContext as DashboardActivity).showSnackMessage(response.message!!)
                            }

                        }, { error ->
                            progress_wheel.stopSpinning()
                            error.printStackTrace()
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                        })
        )
    }

    private fun showAreaDialog(areaList: ArrayList<AreaListEntity>) {
        AreaListDialog.newInstance(areaList) { area: AreaListEntity ->
            tv_area.text = area.area_name
            areaId = area.area_id!!
            clearFocus()
        }.show(fragmentManager!!, "")
    }

    private fun getTypeListApi(isFromRefresh: Boolean) {

        if (!isFromRefresh && !AppUtils.isOnline(mContext)) {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
            return
        }

        val repository = TypeListRepoProvider.provideTypeListRepository()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
                repository.typeList()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as TypeListResponseModel
                            if (response.status == NetworkConstant.SUCCESS) {
                                val list = response.type_list

                                if (list != null && list.isNotEmpty()) {

                                    if (isFromRefresh)
                                        AppDatabase.getDBInstance()?.typeListDao()?.delete()

                                    doAsync {

                                        list.forEach {
                                            val type = TypeListEntity()
                                            AppDatabase.getDBInstance()?.typeListDao()?.insert(type.apply {
                                                type_id = it.id
                                                name = it.name
                                            })
                                        }

                                        uiThread {
                                            progress_wheel.stopSpinning()
                                            if (!isFromRefresh)
                                                showTypeDialog(AppDatabase.getDBInstance()?.typeListDao()?.getAll() as ArrayList<TypeListEntity>)
                                            else
                                                getEntityTypeListApi(isFromRefresh)
                                        }
                                    }
                                } else {
                                    progress_wheel.stopSpinning()
                                    if (!isFromRefresh)
                                        (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                    else
                                        getEntityTypeListApi(isFromRefresh)
                                }
                            } else if (response.status == NetworkConstant.NO_DATA) {
                                progress_wheel.stopSpinning()
                                if (!isFromRefresh)
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                else
                                    getEntityTypeListApi(isFromRefresh)
                            } else {
                                progress_wheel.stopSpinning()
                                if (!isFromRefresh)
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                else
                                    getEntityTypeListApi(isFromRefresh)
                            }

                        }, { error ->
                            progress_wheel.stopSpinning()
                            error.printStackTrace()
                            if (!isFromRefresh)
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                            else
                                getEntityTypeListApi(isFromRefresh)
                        })
        )
    }


    private fun showTypeDialog(typeList: ArrayList<TypeListEntity>) {
        TypeDialog.newInstance(typeList) { type: TypeListEntity ->
            tv_type.text = type.name
            typeId = type.type_id!!
        }.show((mContext as DashboardActivity).supportFragmentManager, "")
    }

    private fun getEntityTypeListApi(isFromRefresh: Boolean) {
        if (!isFromRefresh && !AppUtils.isOnline(mContext)) {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
            return
        }
        println("tag_entityc_call getEntityTypeListApi calling")
        Timber.d("tag_entityc_call getEntityTypeListApi calling")
        val repository = TypeListRepoProvider.provideTypeListRepository()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
                repository.entityList()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as EntityResponseModel
                            if (response.status == NetworkConstant.SUCCESS) {
                                val list = response.entity_type

                                if (list != null && list.isNotEmpty()) {

                                    if (isFromRefresh)
                                        AppDatabase.getDBInstance()?.entityDao()?.delete()

                                    doAsync {

                                        list.forEach {
                                            val entity = EntityTypeEntity()
                                            AppDatabase.getDBInstance()?.entityDao()?.insert(entity.apply {
                                                entity_id = it.id
                                                name = it.name
                                            })
                                        }

                                        uiThread {
                                            progress_wheel.stopSpinning()
                                            if (!isFromRefresh)
                                                showEntityDialog(AppDatabase.getDBInstance()?.entityDao()?.getAll() as ArrayList<EntityTypeEntity>)
                                            else
                                                getPartyStatusListApi(isFromRefresh)
                                        }
                                    }
                                } else {
                                    progress_wheel.stopSpinning()
                                    if (!isFromRefresh)
                                        (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                    else
                                        getPartyStatusListApi(isFromRefresh)
                                }
                            } else if (response.status == NetworkConstant.NO_DATA) {
                                progress_wheel.stopSpinning()
                                if (!isFromRefresh)
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                else
                                    getPartyStatusListApi(isFromRefresh)
                            } else {
                                progress_wheel.stopSpinning()
                                if (!isFromRefresh)
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                else
                                    getPartyStatusListApi(isFromRefresh)
                            }

                        }, { error ->
                            progress_wheel.stopSpinning()
                            error.printStackTrace()
                            if (!isFromRefresh)
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                            else
                                getPartyStatusListApi(isFromRefresh)
                        })
        )
    }

    private fun showEntityDialog(list: ArrayList<EntityTypeEntity>) {
        EntityTypeDialog.newInstance(list) {
            tv_entity.text = it.name
            entityId = it.entity_id!!
        }.show((mContext as DashboardActivity).supportFragmentManager, "")
    }

    private fun getPartyStatusListApi(isFromRefresh: Boolean) {
        if (!isFromRefresh && !AppUtils.isOnline(mContext)) {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
            return
        }

        val repository = TypeListRepoProvider.provideTypeListRepository()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
                repository.partyStatusList()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as PartyStatusResponseModel
                            if (response.status == NetworkConstant.SUCCESS) {
                                val list = response.party_status

                                if (list != null && list.isNotEmpty()) {

                                    if (isFromRefresh)
                                        AppDatabase.getDBInstance()?.partyStatusDao()?.delete()

                                    doAsync {

                                        list.forEach {
                                            val party = PartyStatusEntity()
                                            AppDatabase.getDBInstance()?.partyStatusDao()?.insert(party.apply {
                                                party_status_id = it.id
                                                name = it.name
                                            })
                                        }

                                        uiThread {
                                            progress_wheel.stopSpinning()
                                            if (!isFromRefresh)
                                                showPartyStatusDialog(AppDatabase.getDBInstance()?.partyStatusDao()?.getAll() as ArrayList<PartyStatusEntity>)
                                            else
                                                getRetailerListApi(isFromRefresh)
                                        }
                                    }
                                } else {
                                    progress_wheel.stopSpinning()
                                    if (!isFromRefresh)
                                        (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                    else
                                        getRetailerListApi(isFromRefresh)
                                }
                            } else if (response.status == NetworkConstant.NO_DATA) {
                                progress_wheel.stopSpinning()
                                if (!isFromRefresh)
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                else
                                    getRetailerListApi(isFromRefresh)
                            } else {
                                progress_wheel.stopSpinning()
                                if (!isFromRefresh)
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                else
                                    getRetailerListApi(isFromRefresh)
                            }

                        }, { error ->
                            progress_wheel.stopSpinning()
                            error.printStackTrace()
                            if (!isFromRefresh)
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                            else
                                getRetailerListApi(isFromRefresh)
                        })
        )
    }

    private fun showPartyStatusDialog(list: ArrayList<PartyStatusEntity>) {
        PartyStatusDialog.newInstance(list) {
            tv_party.text = it.name
            partyStatusId = it.party_status_id!!
        }.show((mContext as DashboardActivity).supportFragmentManager, "")
    }

    private fun getRetailerListApi(isFromRefresh: Boolean) {
        if (!isFromRefresh && !AppUtils.isOnline(mContext)) {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
            return
        }

        val repository = TypeListRepoProvider.provideTypeListRepository()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
                repository.retailerList()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as RetailerListResponseModel
                            if (response.status == NetworkConstant.SUCCESS) {
                                val list = response.retailer_list

                                if (list != null && list.isNotEmpty()) {

                                    if (isFromRefresh)
                                        AppDatabase.getDBInstance()?.retailerDao()?.delete()

                                    doAsync {

                                        list.forEach {
                                            val retailer = RetailerEntity()
                                            AppDatabase.getDBInstance()?.retailerDao()?.insert(retailer.apply {
                                                retailer_id = it.id
                                                name = it.name
                                                type_id = it.type_id
                                            })
                                        }

                                        uiThread {
                                            progress_wheel.stopSpinning()
                                            if (!isFromRefresh) {
                                                if (addShopData.type != "11") {
                                                    if (dealerId.isNotEmpty()) {
                                                        val list_ = AppDatabase.getDBInstance()?.retailerDao()?.getItemTypeWise(dealerId) as java.util.ArrayList<RetailerEntity>
                                                        if (list_ != null && list_.isNotEmpty())
                                                            showRetailerListDialog(list_)
                                                        else
                                                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_data_found))
                                                    } else
                                                        showRetailerListDialog(AppDatabase.getDBInstance()?.retailerDao()?.getAll() as ArrayList<RetailerEntity>)
                                                }
                                                else if (addShopData.type == "11") {
                                                    val list_ = AppDatabase.getDBInstance()?.retailerDao()?.getAll()?.filter {
                                                        it.retailer_id == "2"
                                                    }

                                                    if (list_ != null && list_.isNotEmpty())
                                                        showRetailerListDialog(list_ as ArrayList<RetailerEntity>)
                                                    else
                                                        (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_data_found))
                                                }
                                                else
                                                    showRetailerListDialog(AppDatabase.getDBInstance()?.retailerDao()?.getAll() as ArrayList<RetailerEntity>)
                                            }
                                            else
                                                getDealerListApi(isFromRefresh)
                                        }
                                    }
                                } else {
                                    progress_wheel.stopSpinning()
                                    if (!isFromRefresh)
                                        (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                    else
                                        getDealerListApi(isFromRefresh)
                                }
                            } else if (response.status == NetworkConstant.NO_DATA) {
                                progress_wheel.stopSpinning()
                                if (!isFromRefresh)
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                else
                                    getDealerListApi(isFromRefresh)
                            } else {
                                progress_wheel.stopSpinning()
                                if (!isFromRefresh)
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                else
                                    getDealerListApi(isFromRefresh)
                            }

                        }, { error ->
                            progress_wheel.stopSpinning()
                            error.printStackTrace()
                            if (!isFromRefresh)
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                            else
                                getDealerListApi(isFromRefresh)
                        })
        )
    }

    private fun showRetailerListDialog(list: ArrayList<RetailerEntity>) {
        RetailerListDialog.newInstance(list) {
            tv_select_retailer.text = it.name
            retailerId = it.retailer_id!!

            if (retailerId == "1")
                rl_entity_main.visibility = View.VISIBLE
            else {
                entityId = ""
                tv_entity.text = ""
                rl_entity_main.visibility = View.GONE
            }

        }.show((mContext as DashboardActivity).supportFragmentManager, "")
    }

    private fun getDealerListApi(isFromRefresh: Boolean) {
        if (!isFromRefresh && !AppUtils.isOnline(mContext)) {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
            return
        }

        val repository = TypeListRepoProvider.provideTypeListRepository()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
                repository.dealerList()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as DealerListResponseModel
                            if (response.status == NetworkConstant.SUCCESS) {
                                val list = response.dealer_list

                                if (list != null && list.isNotEmpty()) {

                                    if (isFromRefresh)
                                        AppDatabase.getDBInstance()?.dealerDao()?.delete()

                                    doAsync {

                                        list.forEach {
                                            val dealer = DealerEntity()
                                            AppDatabase.getDBInstance()?.dealerDao()?.insert(dealer.apply {
                                                dealer_id = it.id
                                                name = it.name
                                            })
                                        }

                                        uiThread {
                                            progress_wheel.stopSpinning()
                                            if (!isFromRefresh)
                                                showDealerListDialog(AppDatabase.getDBInstance()?.dealerDao()?.getAll() as ArrayList<DealerEntity>)
                                            else
                                                getBeatListApi(isFromRefresh)
                                        }
                                    }
                                } else {
                                    progress_wheel.stopSpinning()
                                    if (!isFromRefresh)
                                        (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                    else
                                        getBeatListApi(isFromRefresh)
                                }
                            } else if (response.status == NetworkConstant.NO_DATA) {
                                progress_wheel.stopSpinning()
                                if (!isFromRefresh)
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                else
                                    getBeatListApi(isFromRefresh)
                            } else {
                                progress_wheel.stopSpinning()
                                if (!isFromRefresh)
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                else
                                    getBeatListApi(isFromRefresh)
                            }

                        }, { error ->
                            progress_wheel.stopSpinning()
                            error.printStackTrace()
                            if (!isFromRefresh)
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                            else
                                getBeatListApi(isFromRefresh)
                        })
        )
    }

    private fun showDealerListDialog(list: ArrayList<DealerEntity>) {
        DealerListDialog.newInstance(list) {
            tv_select_dealer.text = it.name
            dealerId = it.dealer_id!!
            retailerId = ""
            tv_select_retailer.text = ""
            assignedToDDId = ""
            tv_assign_to_dd.text = ""
        }.show((mContext as DashboardActivity).supportFragmentManager, "")
    }

    private fun getBeatListApi(isFromRefresh: Boolean) {
        if (!isFromRefresh && !AppUtils.isOnline(mContext)) {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
            return
        }

        val repository = TypeListRepoProvider.provideTypeListRepository()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
                repository.beatList()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as BeatListResponseModel
                            if (response.status == NetworkConstant.SUCCESS) {
                                val list = response.beat_list

                                if (list != null && list.isNotEmpty()) {

                                    if (isFromRefresh)
                                        AppDatabase.getDBInstance()?.beatDao()?.delete()

                                    doAsync {

                                        list.forEach {
                                            val beat = BeatEntity()
                                            AppDatabase.getDBInstance()?.beatDao()?.insert(beat.apply {
                                                beat_id = it.id
                                                name = it.name
                                            })
                                        }

                                        uiThread {
                                            progress_wheel.stopSpinning()
                                            if (!isFromRefresh)
                                                showBeatListDialog(AppDatabase.getDBInstance()?.beatDao()?.getAll() as ArrayList<BeatEntity>)
                                        }
                                    }
                                } else {
                                    progress_wheel.stopSpinning()
                                    if (!isFromRefresh)
                                        (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                }
                            } else if (response.status == NetworkConstant.NO_DATA) {
                                progress_wheel.stopSpinning()
                                if (!isFromRefresh)
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                            } else {
                                progress_wheel.stopSpinning()
                                if (!isFromRefresh)
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                else
                                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_msg), 1000)
                            }

                        }, { error ->
                            progress_wheel.stopSpinning()
                            error.printStackTrace()
                            if (!isFromRefresh)
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                            else
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_msg), 1000)
                        })
        )
    }

    private fun showBeatListDialog(list: ArrayList<BeatEntity>) {
        BeatListDialog.newInstance(list) {
            tv_select_beat.text = it.name
            beatId = it.beat_id!!
        }.show((mContext as DashboardActivity).supportFragmentManager, "")
    }

    private var permissionUtils: PermissionUtils? = null
    private fun initPermissionCheck() {

        //begin mantis id 26741 Storage permission updation Suman 22-08-2023
        var permissionList = arrayOf<String>( Manifest.permission.CAMERA)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            permissionList += Manifest.permission.READ_MEDIA_IMAGES
            permissionList += Manifest.permission.READ_MEDIA_AUDIO
            permissionList += Manifest.permission.READ_MEDIA_VIDEO
        }else{
            permissionList += Manifest.permission.WRITE_EXTERNAL_STORAGE
            permissionList += Manifest.permission.READ_EXTERNAL_STORAGE
        }
//end mantis id 26741 Storage permission updation Suman 22-08-2023

        permissionUtils = PermissionUtils(mContext as Activity, object : PermissionUtils.OnPermissionListener {
            override fun onPermissionGranted() {
                if (isDocDegree == 1)
                    showPictureDialog()
                else
                    launchCamera()
            }

            override fun onPermissionNotGranted() {
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.accept_permission))
            }

        },permissionList)// arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    private fun initPermissionCheckOne() {

        //begin mantis id 26741 Storage permission updation Suman 22-08-2023
        var permissionList = arrayOf<String>( Manifest.permission.CAMERA)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            permissionList += Manifest.permission.READ_MEDIA_IMAGES
            permissionList += Manifest.permission.READ_MEDIA_AUDIO
            permissionList += Manifest.permission.READ_MEDIA_VIDEO
        }else{
            permissionList += Manifest.permission.WRITE_EXTERNAL_STORAGE
            permissionList += Manifest.permission.READ_EXTERNAL_STORAGE
        }
//end mantis id 26741 Storage permission updation Suman 22-08-2023

        permissionUtils = PermissionUtils(mContext as Activity, object : PermissionUtils.OnPermissionListener {
            override fun onPermissionGranted() {
                showPictureDialog()
            }

            override fun onPermissionNotGranted() {
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.accept_permission))
            }

        },permissionList)// arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    fun onRequestPermission(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionUtils?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun getAssignedDDListApi(shopAdded: Boolean, shop_id: String?) {
        Timber.d("tag_itc_check assignToDDList call AddShopFragment")
        val repository = AssignToDDListRepoProvider.provideAssignDDListRepository()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
                repository.assignToDDList(Pref.profile_state)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as AssignToDDListResponseModel
                            if (response.status == NetworkConstant.SUCCESS) {
                                val list = response.assigned_to_dd_list

                                if (list != null && list.isNotEmpty()) {

                                    doAsync {

                                        val assignDDList = AppDatabase.getDBInstance()?.ddListDao()?.getAll()
                                        if (assignDDList != null)
                                            AppDatabase.getDBInstance()?.ddListDao()?.delete()

                                        for (i in list.indices) {
                                            val assignToDD = AssignToDDEntity()
                                            assignToDD.dd_id = list[i].assigned_to_dd_id
                                            assignToDD.dd_name = list[i].assigned_to_dd_authorizer_name
                                            assignToDD.dd_phn_no = list[i].phn_no
                                            assignToDD.pp_id = list[i].assigned_to_pp_id
                                            assignToDD.type_id = list[i].type_id
                                            assignToDD.dd_latitude = list[i].dd_latitude
                                            assignToDD.dd_longitude = list[i].dd_longitude
                                            AppDatabase.getDBInstance()?.ddListDao()?.insert(assignToDD)
                                        }

                                        uiThread {
                                            progress_wheel.stopSpinning()
                                            if (!shopAdded) {
                                                /*if (!TextUtils.isEmpty(assignedToPPId)) {
                                                    val list_ = AppDatabase.getDBInstance()?.ddListDao()?.getValuePPWise(assignedToPPId)
                                                    showAssignedToDDDialog(list_)
                                                }
                                                else {
                                                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.select_pp))
                                                }*/

                                                if (dealerId.isNotEmpty()) {
                                                    val list_ = AppDatabase.getDBInstance()?.ddListDao()?.getValueTypeWise(dealerId)
                                                    if (list_ != null && list_.isNotEmpty())
                                                        showAssignedToDDDialog(list_)
                                                    else
                                                        (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_data_found))
                                                }
                                                else
                                                    showAssignedToDDDialog(AppDatabase.getDBInstance()?.ddListDao()?.getAll())
                                            } else {
                                                /*if (!TextUtils.isEmpty(shop_id))
                                                    callOtpSentApi(shop_id!!)*/
                                                //showShopVerificationDialog(shop_id!!)
                                                getAssignedToShopApi(shopAdded, shop_id)
                                            }
                                        }
                                    }
                                } else {
                                    progress_wheel.stopSpinning()
                                    if (!shopAdded)
                                        (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                    else {
                                        /*if (!TextUtils.isEmpty(shop_id))
                                            callOtpSentApi(shop_id!!)*/
                                        //showShopVerificationDialog(shop_id!!)
                                        getAssignedToShopApi(shopAdded, shop_id)
                                    }
                                }
                            } else {
                                progress_wheel.stopSpinning()
                                if (!shopAdded)
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                else {
                                    /*if (!TextUtils.isEmpty(shop_id))
                                        callOtpSentApi(shop_id!!)*/
                                    //showShopVerificationDialog(shop_id!!)
                                    getAssignedToShopApi(shopAdded, shop_id)
                                }
                            }

                        }, { error ->
                            progress_wheel.stopSpinning()
                            if (!shopAdded)
                                (mContext as DashboardActivity).showSnackMessage("ERROR")
                            else {
                                /*if (!TextUtils.isEmpty(shop_id))
                                    callOtpSentApi(shop_id!!)*/
                                //showShopVerificationDialog(shop_id!!)
                                getAssignedToShopApi(shopAdded, shop_id)
                            }
                        })
        )
    }

    private fun getAssignedToShopApi(shopAdded: Boolean, shop_id: String?) {
        Timber.d("tag_itc_check getAssignedToShopList call AddShopFragment")
        val repository = TypeListRepoProvider.provideTypeListRepository()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
                repository.assignToShopList(Pref.profile_state)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as AssignedToShopListResponseModel
                            if (response.status == NetworkConstant.SUCCESS) {
                                val list = response.shop_list

                                AppDatabase.getDBInstance()?.assignToShopDao()?.delete()

                                doAsync {
                                    list?.forEach {
                                        val shop = AssignToShopEntity()
                                        AppDatabase.getDBInstance()?.assignToShopDao()?.insert(shop.apply {
                                            assigned_to_shop_id = it.assigned_to_shop_id
                                            name = it.name
                                            phn_no = it.phn_no
                                            type_id = it.type_id
                                        })
                                    }

                                    uiThread {
                                        progress_wheel.stopSpinning()
                                        if (!shopAdded) {
                                            if (retailerId.isNotEmpty()) {
                                                val list_ = AppDatabase.getDBInstance()?.assignToShopDao()?.getValueTypeWise(retailerId) as ArrayList<AssignToShopEntity>
                                                if (list_ != null && list_.isNotEmpty())
                                                    showAssignedToShopListDialog(list_)
                                                else
                                                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_data_found))
                                            }
                                            else
                                                showAssignedToShopListDialog(AppDatabase.getDBInstance()?.assignToShopDao()?.getAll() as ArrayList<AssignToShopEntity>)
                                        }
                                        else{
                                            // change ITC 27-08-2021
                                            if(Pref.isOrderShow == false){
                                                (mContext as DashboardActivity).loadFragment(FragType.DashboardFragment, false, "")
                                            }else{
                                                showShopVerificationDialog(shop_id!!)
                                            }
                                        }
                                    }
                                }
                            }
                            else {
                                progress_wheel.stopSpinning()
                                if (!shopAdded)
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                else
                                    showShopVerificationDialog(shop_id!!)
                            }

                        }, { error ->
                            progress_wheel.stopSpinning()
                            error.printStackTrace()
                            if (!shopAdded)
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                            else
                                showShopVerificationDialog(shop_id!!)
                        })
        )
    }

    private fun showAssignedToDDDialog(mAssignedList: List<AssignToDDEntity>?) {
        AssignedToDDDialog.newInstance(mAssignedList, object : AssignedToDDDialog.OnItemSelectedListener {
            override fun onItemSelect(dd: AssignToDDEntity?) {
                tv_assign_to_dd.text = dd?.dd_name + " (" + dd?.dd_phn_no + ")"
                assignedToDDId = dd?.dd_id.toString()
            }
        }).show(fragmentManager!!, "")
    }

    private fun showAssignedToPPDialog(mAssignedList: List<AssignToPPEntity>?, type: String?) {
        AssignedToPPDialog.newInstance(mAssignedList, type!!, object : AssignedToPPDialog.OnItemSelectedListener {
            override fun onItemSelect(pp: AssignToPPEntity?) {
                assign_to_tv.text = pp?.pp_name + " (" + pp?.pp_phn_no + ")"
                assignedToPPId = pp?.pp_id.toString()
            }
        }).show(fragmentManager!!, "")
    }

    private fun showAssignedToShopListDialog(list: ArrayList<AssignToShopEntity>) {
        AssignedToShopDialog.newInstance(list, object : AssignedToShopDialog.OnItemSelectedListener {
            override fun onItemSelect(shop: AssignToShopEntity?) {
                assign_to_shop_tv.text = shop?.name + " (" + shop?.phn_no + ")"
                assignedToShopId = shop?.assigned_to_shop_id!!
            }
        }).show(fragmentManager!!, "")
    }

    fun setImage(imgRealPath: Uri, fileSizeInKB: Long) {
        if (isDocDegree == 0) {
            imagePath = imgRealPath.toString()
            Picasso.get()
                    .load(imgRealPath)
                    .resize(500, 100)
                    .into(shopLargeImg)
            layer_image_vw_IMG.visibility = View.INVISIBLE
            take_photo_tv.visibility = View.INVISIBLE
            capture_shop_image_IV.visibility = View.INVISIBLE
        } else if(isDocDegree==2){
            imagePathCompetitor = imgRealPath.toString()
            Picasso.get()
                    .load(imgRealPath)
                    .resize(500, 100)
                    .into(iv_competitor_image_view)
        }else {
            if (fileSizeInKB <= 400) {
                degreeImgLink = imgRealPath.toString()
                attachment_EDT.setText(imgRealPath.toString())
            } else
                (mContext as DashboardActivity).showSnackMessage("Image size can not be greater than 400 KB")
        }

//        callApiToUploadImg(imgRealPath.toString())
    }

    fun validateAndSaveData() {

        ////////////  Send broadcast to LocationService ////////////
        val i = Intent("android.intent.action.LOCATIONSERVICE")
        i.putExtra("call_msg", "UPDATE")
        mContext.sendBroadcast(i)
        ////////////////////////////////////////////////////////////


    }

    fun launchCamera() {
        if (PermissionHelper.checkCameraPermission(mContext as DashboardActivity) && PermissionHelper.checkStoragePermission(mContext as DashboardActivity)) {
            /*val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, (mContext as DashboardActivity).getPhotoFileUri(System.currentTimeMillis().toString() + ".png"))
            (mContext as DashboardActivity).startActivityForResult(intent, PermissionHelper.REQUEST_CODE_CAMERA)*/

            (mContext as DashboardActivity).captureImage()
        }
    }

    fun selectImageInAlbum() {
        if (PermissionHelper.checkStoragePermission(mContext as DashboardActivity)) {
            val intent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            (mContext as DashboardActivity).startActivityForResult(intent, PermissionHelper.REQUEST_CODE_STORAGE)

        }

    }

    fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(mContext)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(pictureDialogItems) { dialog, which ->
            when (which) {
                0 -> selectImageInAlbum()
                1 -> launchCamera()
            }
        }
        pictureDialog.show()
    }


    fun updateshoplocation(shopLat: Double, shopLong: Double) {

//        (mContext as DashboardActivity).showSnackMessage("Lat: "+shopLat+" Long: "+shopLong)

        ///////////////////////////////

        if ((shopLat == null && shopLong == null) || (shopLat == 0.0 && shopLong == 0.0)) {
            (mContext as DashboardActivity).showSnackMessage("Location is invalid. Please select valid location from map.")

            Handler().postDelayed(Runnable {
                (mContext as DashboardActivity).loadFragment(FragType.SearchLocationFragment, true, "")
            }, 100)
            return
        }


        shopLatitude = shopLat
        shopLongitude = shopLong

        if (!addShopData.type.isNullOrBlank())
            shopDataModel.type = addShopData.type

        if (addShopData.type.isNullOrBlank())
            addShopData.type = "1"

        if (addShopData.type == "1") {
            amount = ""
            assignedToShopId = ""
            val assignDDList = AppDatabase.getDBInstance()?.ddListDao()?.getAll()
            val assignPPList = AppDatabase.getDBInstance()?.ppListDao()?.getAll()
            assignedToPPId = assignPPList?.get(0)?.pp_id!!
            assignedToDDId = assignDDList?.get(0)?.dd_id!!
            if (TextUtils.isEmpty(assignedToPPId)) {
                (mContext as DashboardActivity).showSnackMessage("Please select assigned to " + Pref.ppText)
                BaseActivity.isApiInitiated = false
                return
            }
            else if (Pref.isShowDealerForDD && dealerId.isEmpty()) {
                (mContext as DashboardActivity).showSnackMessage("Please select any GPTPL/Distributor")
                BaseActivity.isApiInitiated = false
                return
            }
            else if (TextUtils.isEmpty(assignedToDDId)) {
                (mContext as DashboardActivity).showSnackMessage("Please select assigned to " + Pref.ddText)
                BaseActivity.isApiInitiated = false
                return
            }
            else if (Pref.isShowRetailerEntity && retailerId.isEmpty()) {
                (mContext as DashboardActivity).showSnackMessage("Please select any retailer/entity")
                BaseActivity.isApiInitiated = false
                return
            }
            else if (Pref.willShowEntityTypeforShop && retailerId == "1" && entityId.isEmpty()) {
                (mContext as DashboardActivity).showSnackMessage("Please select any entity type")
                BaseActivity.isApiInitiated = false
                return
            }
        } else if (addShopData.type == "4" || addShopData.type == "12" || addShopData.type == "13" || addShopData.type == "14" || addShopData.type == "15") {
            amount = ""
            entityId = ""
            assignedToShopId = ""
            if (TextUtils.isEmpty(assignedToPPId)) {
                (mContext as DashboardActivity).showSnackMessage("Please select assigned to " + Pref.ppText)
                BaseActivity.isApiInitiated = false
                return
            }
            else if (Pref.isShowDealerForDD && dealerId.isEmpty()) {
                (mContext as DashboardActivity).showSnackMessage("Please select any GPTPL/Distributor")
                BaseActivity.isApiInitiated = false
                return
            }
        } else if (addShopData.type == "5") {
            entityId = ""
            assignedToShopId = ""
            if (TextUtils.isEmpty(assignedToPPId)) {
                (mContext as DashboardActivity).showSnackMessage("Please select assigned to " + Pref.ppText)
                BaseActivity.isApiInitiated = false
                return
            } else if (TextUtils.isEmpty(assignedToDDId)) {
                (mContext as DashboardActivity).showSnackMessage("Please select assigned to " + Pref.ddText)
                BaseActivity.isApiInitiated = false
                return
            }
        }
        else if (addShopData.type == "7") {
            amount = ""
            entityId = ""
            assignedToShopId = ""

            if (TextUtils.isEmpty(assignedToPPId)) {
                (mContext as DashboardActivity).showSnackMessage("Please select assigned to")
                BaseActivity.isApiInitiated = false
                return
            }
        }
        else if (addShopData.type == "10") {
            amount = ""
            entityId = ""
            assignedToShopId = ""

            if (Pref.isDDMandatoryForMeeting) {
                if (TextUtils.isEmpty(assignedToDDId)) {
                    (mContext as DashboardActivity).showSnackMessage("Please select assigned to " + Pref.ddText)
                    BaseActivity.isApiInitiated = false
                    return
                }
            }
        }
        else if (addShopData.type == "11") {
            amount = ""
            entityId = ""

            if (Pref.isShowRetailerEntity && retailerId.isEmpty()) {
                (mContext as DashboardActivity).showSnackMessage("Please select retailer")
                BaseActivity.isApiInitiated = false
                return
            }
            else if (TextUtils.isEmpty(assignedToShopId)) {
                (mContext as DashboardActivity).showSnackMessage("Please select assigned to " + Pref.shopText)
                BaseActivity.isApiInitiated = false
                return
            }
        }
        else {
            amount = ""
            entityId = ""
            assignedToShopId = ""
        }

        shopDataModel.amount = amount
        shopDataModel.entity_id = entityId
        shopDataModel.assigned_to_shop_id = assignedToShopId

        if (Pref.willShowPartyStatus && partyStatusId.isEmpty()) {
            (mContext as DashboardActivity).showSnackMessage("Please select any party status")
            BaseActivity.isApiInitiated = false
            return
        }
        else
            shopDataModel.party_status_id = partyStatusId

//        shopDataModel = AddShopDBModelEntity()
        if (!(shopName.text!!.isBlank()))
            shopDataModel.shopName = shopName.text.toString()
        else {
            shopName.error = getString(R.string.field_cannot_be_blank)
            (mContext as DashboardActivity).showSnackMessage("Please enter " + Pref.shopText + " name")
            BaseActivity.isApiInitiated = false
            return
        }

        if (!(shopAddress.text!!.isBlank()))
            shopDataModel.address = shopAddress.text.toString()
        else {
            shopDataModel.address="Unknown"
            //11-10-2021
            //shopAddress.error = getString(R.string.field_cannot_be_blank)
            //(mContext as DashboardActivity).showSnackMessage("Please enter " + Pref.shopText + " address")
            //BaseActivity.isApiInitiated = false
            //return
        }
//        if (PermissionHelper.checkLocationPermission((mContext as DashboardActivity), 100)) {
//            var latlong = getLocationFromAddress(mContext, shopAddress.text.toString().trim())
//            if (latlong == null) {
//                shopAddress.error = getString(R.string.valid_sddress)
//                (mContext as DashboardActivity).showSnackMessage(getString(R.string.valid_sddress))
//                return
//            }
//        }

        if (!(shopPin.text!!.isBlank()))
            shopDataModel.pinCode = shopPin.text.toString()
        else {
            shopPin.error = getString(R.string.field_cannot_be_blank)
            (mContext as DashboardActivity).showSnackMessage("Please enter " + Pref.shopText + " pin")
            BaseActivity.isApiInitiated = false
            rl_pincode_root.visibility=View.VISIBLE
            return
        }

        // force off for ITC 25-08-2021
//        if (!Pref.isCustomerFeatureEnable) {
//            if (!(ownerName.text!!.trim().isBlank()))
//                shopDataModel.ownerName = ownerName.text.toString()
//            else {
//                if (addShopData.type != "8") {
//                    ownerName.error = getString(R.string.field_cannot_be_blank)
//
//                    if (addShopData.type != "7")
//                        (mContext as DashboardActivity).showSnackMessage(getString(R.string.ownername_error))
//                    else
//                        (mContext as DashboardActivity).showSnackMessage(getString(R.string.contactname_error))
//
//                    BaseActivity.isApiInitiated = false
//                    return
//                }
//                shopDataModel.ownerName = ""
//            }
//        }

        // force off for ITC 25-08-2021
        shopDataModel.ownerName= shopName.text.toString()

/*        if (!(ownerNumber.text!!.isBlank()))
            shopDataModel.ownerContactNumber = ownerNumber.text.toString()
        else {
            ownerNumber.error = getString(R.string.field_cannot_be_blank)
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.numberblank_error))
            BaseActivity.isApiInitiated = false
            return
        }*/

   /*     if (AppUtils.isValidateMobile(ownerNumber.text.toString())) {
            shopDataModel.ownerContactNumber = ownerNumber.text.toString()
        } else {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.numbervalid_error))
            BaseActivity.isApiInitiated = false
            return
        }*/

    /*    if (ownerNumber.text.toString().trim().startsWith("6") || ownerNumber.text.toString().trim().startsWith("7") ||
                ownerNumber.text.toString().trim().startsWith("8") || ownerNumber.text.toString().trim().startsWith("9") || true) {
            shopDataModel.ownerContactNumber = ownerNumber.text.toString()
        } else {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_enter_valid_phn_no), 3000)
            BaseActivity.isApiInitiated = false
            return
        }*/

        if(ownerNumber.text.toString().length>=0 && ownerNumber.text.toString().length<=15){
            shopDataModel.ownerContactNumber = ownerNumber.text.toString()
        }else{
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.numbervalid_error))
            BaseActivity.isApiInitiated = false
            return
        }

        //22-09-2021 added
        if(ownerNumber.text.toString().length==0){
            shopDataModel.ownerContactNumber = randTen
        }


        //shopDataModel.ownerContactNumber = getRandomNumberString().toString()

        if (addShopData.type == "5") {
            if (!(amount_EDT.text!!.isBlank())) {

                if (amount_EDT.text.toString().trim().toInt() == 0) {
                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.valid_amount_error))
                    BaseActivity.isApiInitiated = false
                    return
                }

                amount = amount_EDT.text.toString()
            } else {
                amount_EDT.error = getString(R.string.field_cannot_be_blank)
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.amount_error))
                BaseActivity.isApiInitiated = false
                return
            }
        }

        if (!(ownerEmail.text!!.trim().isBlank())) {

            if (AppUtils.isValidEmail(ownerEmail.text.toString())) {
                shopDataModel.ownerEmailId = ownerEmail.text!!.trim().toString()

            } else {
                BaseActivity.isApiInitiated = false
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.email_error))
                return
            }
        }

        if (Pref.isAreaVisible && (Pref.isAreaMandatoryInPartyCreation && TextUtils.isEmpty(areaId))) {
            BaseActivity.isApiInitiated = false
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_select_area))
            return
        }

        if (Pref.isCustomerFeatureEnable && TextUtils.isEmpty(modelId)) {
            BaseActivity.isApiInitiated = false
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_select_model))
            return
        }

        if (Pref.isCustomerFeatureEnable && TextUtils.isEmpty(stageId)) {
            BaseActivity.isApiInitiated = false
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_select_stage))
            return
        }

        shopDataModel.doc_degree = ""
        if (ll_doc_extra_info.visibility == View.VISIBLE) {
            if (TextUtils.isEmpty(attachment_EDT.text.toString().trim())) {
                BaseActivity.isApiInitiated = false
                attachment_EDT.error = getString(R.string.field_cannot_be_blank)
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_capture_doc_pic))
                return
            } else
                shopDataModel.doc_degree = degreeImgLink

            if (TextUtils.isEmpty(et_specalization.text.toString().trim())) {
                BaseActivity.isApiInitiated = false
                et_specalization.error = getString(R.string.field_cannot_be_blank)
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_enter_speciallization))
                return
            } else
                shopDataModel.specialization = et_specalization.text.toString().trim()

            if (TextUtils.isEmpty(et_patient_count.text.toString().trim())) {
                BaseActivity.isApiInitiated = false
                et_patient_count.error = getString(R.string.field_cannot_be_blank)
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_enter_patient_count))
                return
            } else
                shopDataModel.patient_count = et_patient_count.text.toString().trim()

            if (TextUtils.isEmpty(et_category.text.toString().trim())) {
                BaseActivity.isApiInitiated = false
                et_category.error = getString(R.string.field_cannot_be_blank)
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_enter_cateogory))
                return
            } else
                shopDataModel.category = et_category.text.toString().trim()


            if (TextUtils.isEmpty(doc_family_mem_dob_EDT.text.toString())) {
                doc_family_mem_dob_EDT.error = getString(R.string.field_cannot_be_blank)
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_enter_family_member_dob))
                BaseActivity.isApiInitiated = false
                return
            }

            if (TextUtils.isEmpty(doc_address_EDT.text.toString().trim())) {
                BaseActivity.isApiInitiated = false
                doc_address_EDT.error = getString(R.string.field_cannot_be_blank)
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_enter_location))
                return
            } else
                shopDataModel.doc_address = doc_address_EDT.text.toString().trim()

            if (TextUtils.isEmpty(doc_pin_code_EDT.text.toString().trim())) {
                BaseActivity.isApiInitiated = false
                doc_pin_code_EDT.error = getString(R.string.field_cannot_be_blank)
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_enter_pincode))
                return
            } else
                shopDataModel.doc_pincode = doc_pin_code_EDT.text.toString().trim()

            if (!iv_yes.isSelected && !iv_no.isSelected) {
                BaseActivity.isApiInitiated = false
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_select_chamber))
                return
            } else if (iv_yes.isSelected)
                shopDataModel.chamber_status = 1
            else if (iv_no.isSelected)
                shopDataModel.chamber_status = 0

            if (et_remarks.visibility == View.VISIBLE && TextUtils.isEmpty(et_remarks.text.toString().trim())) {
                BaseActivity.isApiInitiated = false
                et_remarks.error = getString(R.string.field_cannot_be_blank)
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_enter_remarks))
                return
            } else if (!TextUtils.isEmpty(et_remarks.text.toString().trim()))
                shopDataModel.remarks = et_remarks.text.toString().trim()

            if (TextUtils.isEmpty(chemist_name_EDT.text.toString().trim())) {
                BaseActivity.isApiInitiated = false
                chemist_name_EDT.error = getString(R.string.field_cannot_be_blank)
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_enter_chemist_name))
                return
            } else
                shopDataModel.chemist_name = chemist_name_EDT.text.toString().trim()

            if (TextUtils.isEmpty(chemist_address_EDT.text.toString().trim())) {
                BaseActivity.isApiInitiated = false
                chemist_address_EDT.error = getString(R.string.field_cannot_be_blank)
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_enter_chemist_address))
                return
            } else
                shopDataModel.chemist_address = chemist_address_EDT.text.toString().trim()

            if (TextUtils.isEmpty(chemist_pin_code_EDT.text.toString().trim())) {
                BaseActivity.isApiInitiated = false
                chemist_pin_code_EDT.error = getString(R.string.field_cannot_be_blank)
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_enter_chemist_pincode))
                return
            } else
                shopDataModel.chemist_pincode = chemist_pin_code_EDT.text.toString().trim()

            saveDataToDb()
            return
        }


        if (ll_extra_info.visibility == View.VISIBLE) {
            if (TextUtils.isEmpty(director_name_EDT.text.toString().trim())) {
                BaseActivity.isApiInitiated = false
                director_name_EDT.error = getString(R.string.field_cannot_be_blank)
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_enter_director_name))
                return
            } else
                shopDataModel.director_name = director_name_EDT.text.toString().trim()

            if (TextUtils.isEmpty(family_mem_dob_EDT.text.toString().trim())) {
                BaseActivity.isApiInitiated = false
                family_mem_dob_EDT.error = getString(R.string.field_cannot_be_blank)
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_enter_family_member_dob))
                return
            } else
                shopDataModel.family_member_dob = family_mem_dob_EDT.text.toString().trim()

            if (TextUtils.isEmpty(key_person_name_EDT.text.toString().trim())) {
                BaseActivity.isApiInitiated = false
                key_person_name_EDT.error = getString(R.string.field_cannot_be_blank)
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_enter_person_name))
                return
            } else
                shopDataModel.person_name = key_person_name_EDT.text.toString().trim()

            if (TextUtils.isEmpty(key_person_no_EDT.text.toString().trim())) {
                BaseActivity.isApiInitiated = false
                key_person_no_EDT.error = getString(R.string.field_cannot_be_blank)
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_enter_phn_no))
                return
            } else
                shopDataModel.person_no = key_person_no_EDT.text.toString().trim()


            if (AppUtils.isValidateMobile(key_person_no_EDT.text.toString())) {
                shopDataModel.person_no = key_person_no_EDT.text.toString()
            } else {
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.numbervalid_error))
                BaseActivity.isApiInitiated = false
                return
            }

            saveDataToDb()
            return
        }

        //if (Pref.willMoreVisitUpdateOptional)
            //showAddMoreInfoAlertDialog()
        //else {
            saveDataToDb()
        //}
    }

    private fun saveDataToDb() {
        if (shopLatitude != null && shopLongitude != null) {
            shopDataModel.shopLat = shopLatitude
            shopDataModel.shopLong = shopLongitude

            if (actualAddress.isEmpty()) {
                var address = LocationWizard.getAdressFromLatlng(mContext, shopLatitude, shopLongitude)
                Timber.e("Actual Shop address (Add Shop)======> $address")

                if (address.contains("http"))
                    address = "Unknown"
                actualAddress = address
            }
            shopDataModel.actual_address = actualAddress
        }
        shopDataModel.visitDate = AppUtils.getCurrentDate()

//        imagePath=FTStorageUtils.IMG_URI.toString()

        if (shopDataModel.type != "8") {
            if (imagePath.isNotBlank())
                shopDataModel.shopImageLocalPath = imagePath
            else {
                if (Pref.isShopImageMandatory) {
                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.capture_shop_image))
                    BaseActivity.isApiInitiated = false
                    return
                }
            }
        }

        if (shopDataModel.type != "8") {
            if (imagePathCompetitor.isNotBlank())
                shopDataModel.shopImageLocalPathCompetitor = imagePathCompetitor
            /*else {
                if (Pref.isShopImageMandatory) {
                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.capture_shop_image))
                    BaseActivity.isApiInitiated = false
                    return
                }
            }*/
        }

        if (Pref.isNextVisitDateMandatory && TextUtils.isEmpty(nextVisitDate)) {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_message_next_visit_date))
            BaseActivity.isApiInitiated = false
            return
        }

        if (Pref.isRecordAudioEnable && TextUtils.isEmpty(audio_record_date_EDT.text.toString().trim())) {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_message_audio))
            BaseActivity.isApiInitiated = false
            return
        }


        shopDataModel.visited = false
        shopDataModel.timeStamp = System.currentTimeMillis().toString()
        shopDataModel.totalVisitCount = "1"
        shopDataModel.shop_id = Pref.user_id + "_" + System.currentTimeMillis().toString()

        shopDataModel.user_id = Pref.user_id
        shopDataModel.lastVisitedDate = AppUtils.getCurrentDateChanged()
//        shopDataModel.lastVisitedDate


        if (addShopData.type.isNullOrBlank())
            addShopData.type = "1"

        if (addShopData.type == "4" || addShopData.type == "7" || addShopData.type == "12" || addShopData.type == "13" || addShopData.type == "14" || addShopData.type == "15") {
            assignedToDDId = ""
        } else if (addShopData.type == "3" || addShopData.type == "2" || addShopData.type == "9" || addShopData.type == "10" ||
                addShopData.type == "6" || addShopData.type == "8") {
            assignedToDDId = ""
            assignedToPPId = ""
        }

        addShopData.assigned_to_dd_id = assignedToDDId
        addShopData.assigned_to_pp_id = assignedToPPId

        shopDataModel.isAddressUpdated = true

        shopDataModel.assigned_to_dd_id = addShopData.assigned_to_dd_id!!
        shopDataModel.assigned_to_pp_id = addShopData.assigned_to_pp_id!!
        shopDataModel.added_date = AppUtils.getCurrentISODateTime()
        shopDataModel.area_id = areaId
        shopDataModel.model_id = modelId
        shopDataModel.primary_app_id = primaryAppId
        shopDataModel.secondary_app_id = secondaryAppId
        shopDataModel.lead_id = leadTypeId
        shopDataModel.funnel_stage_id = funnelStageId
        shopDataModel.stage_id = stageId
        shopDataModel.type_id = typeId
        shopDataModel.retailer_id = retailerId
        shopDataModel.dealer_id = dealerId
        shopDataModel.beat_id = beatId

        if (TextUtils.isEmpty(booking_amount_EDT.text.toString().trim()))
            shopDataModel.booking_amount = ""
        else
            shopDataModel.booking_amount = booking_amount_EDT.text.toString().trim()

        if (AppDatabase.getDBInstance()!!.addShopEntryDao().getDuplicateShopData(ownerNumber.text.toString().trim()).size > 0) {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.contact_number_exist))
            BaseActivity.isApiInitiated = false
            return
        }

        val allShopList= AppDatabase.getDBInstance()?.addShopEntryDao()?.all
        shopDataModel.isShopDuplicate=false
        if(allShopList != null){
            for(i in 0..allShopList?.size!!-1){
                var shopLat = allShopList.get(i).shopLat
                var shopLon = allShopList.get(i).shopLong
                if(shopLat == shopDataModel.shopLat && shopLon == shopDataModel.shopLong){
                    shopDataModel.isShopDuplicate=true
                    break
                }else{
                    val dist = LocationWizard.getDistance(shopLat,shopLon,shopDataModel.shopLat,shopDataModel.shopLong)
                    if(dist<0.01) {
                        shopDataModel.isShopDuplicate=true
                        break
                    }
                }
            }
        }

        if (Pref.isFingerPrintMandatoryForVisit) {
            if ((mContext as DashboardActivity).isFingerPrintSupported) {
                (mContext as DashboardActivity).checkForFingerPrint()

                fingerprintDialog = FingerprintDialog()
                fingerprintDialog?.show((mContext as DashboardActivity).supportFragmentManager, "")
            } else {

                AppDatabase.getDBInstance()!!.addShopEntryDao().insertAll(shopDataModel)

                /*************************************Convert to request object and call api*********************************/
                convertToReqAndApiCall(shopDataModel)
            }
        } else {

            AppDatabase.getDBInstance()!!.addShopEntryDao().insertAll(shopDataModel)

            /*************************************Convert to request object and call api*********************************/
            convertToReqAndApiCall(shopDataModel)
        }
    }

    private fun showAddMoreInfoAlertDialog() {
        AppUtils.isShopAdded = true
        CommonDialogSingleBtn.getInstance("Action", "Wish to update more details for the visit?", "Confirm", object : OnDialogClickListener {
            override fun onOkClick() {
                BaseActivity.isApiInitiated = false
                ll_extra_info.visibility = View.VISIBLE
                scroll_bar.smoothScrollTo(0, ll_extra_info.scrollY)
                //ownerEmail.imeOptions = EditorInfo.IME_ACTION_NEXT
            }
        }, object : CommonDialogSingleBtn.OnCrossClickListener {
            override fun onCrossClick() {
                saveDataToDb()
            }

        }).show((mContext as DashboardActivity).supportFragmentManager, "CommonDialogSingleBtn")
    }

    fun addShop() {
        if (fingerprintDialog != null && fingerprintDialog?.isVisible!!) {
            fingerprintDialog?.dismiss()

            AppDatabase.getDBInstance()!!.addShopEntryDao().insertAll(shopDataModel)

            /*************************************Convert to request object and call api*********************************/
            convertToReqAndApiCall(shopDataModel)
        }
    }


    private fun convertToReqAndApiCall(shopDataModel: AddShopDBModelEntity) {
        if (Pref.user_id == null || Pref.user_id == "" || Pref.user_id == " ") {
            (mContext as DashboardActivity).showSnackMessage("Please login again")
            BaseActivity.isApiInitiated = false
            return
        }

        addShopData.session_token = Pref.session_token
        addShopData.address = shopDataModel.address
        addShopData.owner_contact_no = shopDataModel.ownerContactNumber
        addShopData.owner_email = shopDataModel.ownerEmailId
        addShopData.owner_name = shopDataModel.ownerName
        addShopData.pin_code = shopDataModel.pinCode
        addShopData.shop_lat = shopDataModel.shopLat.toString()
        addShopData.shop_long = shopDataModel.shopLong.toString()
        addShopData.shop_name = shopDataModel.shopName.toString()
        addShopData.shop_id = shopDataModel.shop_id
        addShopData.added_date = shopDataModel.added_date
        addShopData.user_id = Pref.user_id

        if (addShopData.type.isNullOrBlank())
            addShopData.type = "1"

        if (addShopData.type == "4" || addShopData.type == "12" || addShopData.type == "13" || addShopData.type == "14" || addShopData.type == "15") {
            assignedToDDId = ""
            amount = ""
        } else if (addShopData.type == "3" || addShopData.type == "2") {
            assignedToDDId = ""
            assignedToPPId = ""
            amount = ""
        } else if (addShopData.type == "1") {
            amount = ""
        }

        addShopData.amount = amount
        addShopData.assigned_to_dd_id = assignedToDDId
        addShopData.assigned_to_pp_id = assignedToPPId
        addShopData.area_id = areaId
        addShopData.model_id = modelId
        addShopData.primary_app_id = primaryAppId
        addShopData.secondary_app_id = secondaryAppId
        addShopData.lead_id = leadTypeId
        addShopData.stage_id = stageId
        addShopData.funnel_stage_id = funnelStageId
        addShopData.booking_amount = shopDataModel.booking_amount
        addShopData.type_id = typeId

        addShopData.director_name = shopDataModel.director_name
        addShopData.key_person_name = shopDataModel.person_name
        addShopData.phone_no = shopDataModel.person_no

        addShopData.specialization = shopDataModel.specialization
        addShopData.category = shopDataModel.category
        addShopData.doc_address = shopDataModel.doc_address
        addShopData.doc_pincode = shopDataModel.doc_pincode
        addShopData.is_chamber_same_headquarter = shopDataModel.chamber_status.toString()
        addShopData.is_chamber_same_headquarter_remarks = shopDataModel.remarks
        addShopData.chemist_name = shopDataModel.chemist_name
        addShopData.chemist_address = shopDataModel.chemist_address
        addShopData.chemist_pincode = shopDataModel.chemist_pincode
        addShopData.assistant_contact_no = shopDataModel.assistant_no
        addShopData.average_patient_per_day = shopDataModel.patient_count
        addShopData.assistant_name = shopDataModel.assistant_name
        addShopData.entity_id = shopDataModel.entity_id
        addShopData.party_status_id = shopDataModel.party_status_id
        addShopData.retailer_id = shopDataModel.retailer_id
        addShopData.dealer_id = shopDataModel.dealer_id
        addShopData.beat_id = shopDataModel.beat_id
        addShopData.assigned_to_shop_id = shopDataModel.assigned_to_shop_id
        addShopData.actual_address = shopDataModel.actual_address

        addShop.data = addShopData

        if (!TextUtils.isEmpty(addShopData.type) && (addShopData.type == "2" || addShopData.type == "6")) {
            val assignToPP = AssignToPPEntity()
            assignToPP.pp_id = addShopData.shop_id
            assignToPP.pp_name = addShopData.shop_name
            assignToPP.pp_phn_no = addShopData.owner_contact_no
            AppDatabase.getDBInstance()?.ppListDao()?.insert(assignToPP)
        } else if (!TextUtils.isEmpty(addShopData.type)  && (addShopData.type == "4" || addShopData.type == "7")) {
            val assignToPP = AssignToDDEntity()
            assignToPP.dd_id = addShopData.shop_id
            assignToPP.dd_name = addShopData.shop_name
            assignToPP.dd_phn_no = addShopData.owner_contact_no
            //assignToPP.pp_id = addShopData.assigned_to_pp_id
            assignToPP.type_id = addShopData.dealer_id
            AppDatabase.getDBInstance()?.ddListDao()?.insert(assignToPP)
        }
        else if (!TextUtils.isEmpty(addShopData.type) && addShopData.type == "1") {
            val assignToShop = AssignToShopEntity()
            AppDatabase.getDBInstance()?.assignToShopDao()?.insert(assignToShop.apply {
                assigned_to_shop_id = addShopData.shop_id
                name = addShopData.shop_name
                phn_no = addShopData.owner_contact_no
                type_id = addShopData.retailer_id
            })
        }

        finalUniqKey=Pref.user_id+System.currentTimeMillis().toString()
        insertIntoShopActivityTable(addShopData)

        var obj:ShopVisitCompetetorModelEntity = ShopVisitCompetetorModelEntity()
        obj.session_token=addShopData.session_token!!
        obj.shop_id=addShopData.shop_id!!
        obj.user_id=Pref.user_id!!
        obj.shop_image=imagePathCompetitor
        obj.isUploaded=false
        obj.visited_date=""
        if(imagePathCompetitor.length>0 && imagePathCompetitor!=null && imagePathCompetitor!=""){
            AppDatabase.getDBInstance()!!.shopVisitCompetetorImageDao().insert(obj)
        }

        addShopData.shop_revisit_uniqKey=finalUniqKey!!

        // duplicate shop api call
        addShopData.isShopDuplicate=shopDataModel.isShopDuplicate



        addShopApi(addShopData, shopDataModel.shopImageLocalPath, shopDataModel.doc_degree)
    }




    private fun initShopTypePopUp(view: View) {
        val popup = PopupWindow(mContext)
        val layout = layoutInflater.inflate(R.layout.shop_type_dropdown, null)

        popup.contentView = layout
        popup.isOutsideTouchable = true
        popup.isFocusable = true

        var width = 900
        var height = 400
        try {
            val size = Point()
            (mContext as DashboardActivity).windowManager.defaultDisplay.getSize(size)
            width = size.x
            height = size.y

        } catch (e: Exception) {
            e.printStackTrace()
        }
        popup.width = width - 10
        popup.height = WindowManager.LayoutParams.WRAP_CONTENT

        val shop_type_TV: AppCustomTextView = layout.findViewById(R.id.shop_type_TV)
        val distributor_tv: AppCustomTextView = layout.findViewById(R.id.distributor_tv)
        val pp_tv: AppCustomTextView = layout.findViewById(R.id.pp_tv)
        val new_party_tv: AppCustomTextView = layout.findViewById(R.id.new_party_tv)
        val diamond_tv: AppCustomTextView = layout.findViewById(R.id.diamond_tv)
        val rv_type_list: RecyclerView = layout.findViewById(R.id.rv_type_list)

        val list = AppDatabase.getDBInstance()?.shopTypeDao()?.getAll()

        if (list?.size == 1)
            return

        rv_type_list.layoutManager = LinearLayoutManager(mContext)
        rv_type_list.adapter = ShopTypeAdapter(mContext, list) { shopType: ShopTypeEntity ->
            type_TV.text = shopType.shoptype_name

            /*if (shopType.shoptype_id == "1" || shopType.shoptype_id == "5") {
                shop_name_TL.hint = Pref.shopText + " name"

            } else
                shop_name_TL.hint = getString(R.string.company_name)*/

            shop_name_TL.hint = Pref.shopText + " name"

            addShopData.type = shopType.shoptype_id

            val assignPPList = AppDatabase.getDBInstance()?.ppListDao()?.getAll()
            val assignDDList = AppDatabase.getDBInstance()?.ddListDao()?.getAll()
            if (!Pref.isCustomerFeatureEnable) {
                when (addShopData.type) {
                    "1" -> {
                        rl_assign_to_dd.visibility = View.VISIBLE
                        assign_to_rl.visibility = View.VISIBLE
                        rl_amount.visibility = View.GONE
                        shopImage.visibility = View.VISIBLE
                        setMargin(false)
                        ll_doc_extra_info.visibility = View.GONE
                        ll_extra_info.visibility = View.GONE
                        assign_to_shop_rl.visibility = View.GONE
                        til_no.hint = getString(R.string.owner_contact_number)
                        til_mail.hint = getString(R.string.owner_email)
                        til_name.hint = getString(R.string.owner_name)
                        assign_to_tv.hint = "Assigned to " + Pref.ppText

                        if (Pref.willShowEntityTypeforShop)
                            rl_entity_main.visibility = View.VISIBLE
                        else
                            rl_entity_main.visibility = View.GONE

                        if (Pref.isShowRetailerEntity)
                            rl_select_retailer.visibility = View.VISIBLE
                        else
                            rl_select_retailer.visibility = View.GONE

                        if (Pref.isShowDealerForDD) {
                            rl_select_dealer.visibility = View.VISIBLE
                            assignedToDDId = ""
                            tv_assign_to_dd.text = ""
                        }
                        else {
                            rl_select_dealer.visibility = View.GONE

                            if(assignDDList != null && assignDDList.isNotEmpty()) {
                                assignedToDDId = assignDDList[0].dd_id!!
                                tv_assign_to_dd.text = assignDDList[0].dd_name
                            }
                        }

                        if(assignPPList != null && assignPPList.isNotEmpty()) {
                            assignedToPPId = assignPPList[0].pp_id!!
                            assign_to_tv.text = assignPPList[0].pp_name
                        }

                        tv_select_dealer.text = ""
                        dealerId = ""
                        retailerId = ""
                        tv_select_retailer.text = ""
                        assignedToShopId = ""
                        assign_to_shop_tv.text = ""
                    }
                    "2" -> {
                        rl_assign_to_dd.visibility = View.GONE
                        assign_to_rl.visibility = View.GONE
                        rl_amount.visibility = View.GONE
                        shopImage.visibility = View.VISIBLE
                        setMargin(false)
                        ll_doc_extra_info.visibility = View.GONE
                        ll_extra_info.visibility = View.GONE
                        rl_select_retailer.visibility = View.GONE
                        rl_select_dealer.visibility = View.GONE
                        assign_to_shop_rl.visibility = View.GONE
                        til_no.hint = getString(R.string.owner_contact_number)
                        til_mail.hint = getString(R.string.owner_email)
                        til_name.hint = getString(R.string.owner_name)
                        assign_to_tv.hint = "Assigned to " + Pref.ppText
                        rl_entity_main.visibility = View.GONE
                        assignedToPPId = ""
                        assignedToDDId = ""

                        tv_select_dealer.text = ""
                        dealerId = ""
                        retailerId = ""
                        tv_select_retailer.text = ""
                        assignedToShopId = ""
                        assign_to_shop_tv.text = ""
                    }
                    "3" -> {
                        rl_assign_to_dd.visibility = View.GONE
                        assign_to_rl.visibility = View.GONE
                        rl_amount.visibility = View.GONE
                        shopImage.visibility = View.VISIBLE
                        setMargin(false)
                        ll_doc_extra_info.visibility = View.GONE
                        ll_extra_info.visibility = View.GONE
                        rl_select_retailer.visibility = View.GONE
                        rl_select_dealer.visibility = View.GONE
                        assign_to_shop_rl.visibility = View.GONE
                        til_no.hint = getString(R.string.owner_contact_number)
                        til_mail.hint = getString(R.string.owner_email)
                        til_name.hint = getString(R.string.owner_name)
                        assign_to_tv.hint = "Assigned to " + Pref.ppText
                        rl_entity_main.visibility = View.GONE
                        assignedToPPId = ""
                        assignedToDDId = ""
                        tv_select_dealer.text = ""
                        dealerId = ""
                        retailerId = ""
                        tv_select_retailer.text = ""
                        assignedToShopId = ""
                        assign_to_shop_tv.text = ""
                    }
                    "4", "12", "13", "14", "15" -> {
                        assign_to_rl.visibility = View.VISIBLE
                        rl_assign_to_dd.visibility = View.GONE
                        rl_amount.visibility = View.GONE
                        shopImage.visibility = View.VISIBLE
                        setMargin(false)
                        ll_doc_extra_info.visibility = View.GONE
                        ll_extra_info.visibility = View.GONE
                        rl_select_retailer.visibility = View.GONE
                        assign_to_shop_rl.visibility = View.GONE
                        til_no.hint = getString(R.string.owner_contact_number)
                        til_mail.hint = getString(R.string.owner_email)
                        til_name.hint = getString(R.string.owner_name)
                        assign_to_tv.hint = "Assigned to " + Pref.ppText
                        rl_entity_main.visibility = View.GONE

                        if (Pref.isShowDealerForDD)
                            rl_select_dealer.visibility = View.VISIBLE
                        else
                            rl_select_dealer.visibility = View.GONE

                        if(assignPPList != null && assignPPList.isNotEmpty()) {
                            assignedToPPId = assignPPList[0].pp_id!!
                            assign_to_tv.text = assignPPList[0].pp_name
                        }
                        assignedToDDId = ""

                        tv_select_dealer.text = ""
                        dealerId = ""
                        retailerId = ""
                        tv_select_retailer.text = ""
                        assignedToShopId = ""
                        assign_to_shop_tv.text = ""
                    }
                    "5" -> {
                        rl_assign_to_dd.visibility = View.VISIBLE
                        assign_to_rl.visibility = View.VISIBLE
                        rl_amount.visibility = View.VISIBLE
                        shopImage.visibility = View.VISIBLE
                        setMargin(false)
                        ll_doc_extra_info.visibility = View.GONE
                        ll_extra_info.visibility = View.GONE
                        rl_select_retailer.visibility = View.GONE
                        rl_select_dealer.visibility = View.GONE
                        assign_to_shop_rl.visibility = View.GONE
                        til_no.hint = getString(R.string.owner_contact_number)
                        til_mail.hint = getString(R.string.owner_email)
                        til_name.hint = getString(R.string.owner_name)
                        assign_to_tv.hint = "Assigned to " + Pref.ppText
                        rl_entity_main.visibility = View.GONE

                        if(assignPPList != null && assignPPList.isNotEmpty()) {
                            assignedToPPId = assignPPList[0].pp_id!!
                            assign_to_tv.text = assignPPList[0].pp_name
                        }

                        if(assignDDList != null && assignDDList.isNotEmpty()) {
                            assignedToDDId = assignDDList[0].dd_id!!
                            tv_assign_to_dd.text = assignDDList[0].dd_name
                        }

                        tv_select_dealer.text = ""
                        dealerId = ""
                        retailerId = ""
                        tv_select_retailer.text = ""
                        assignedToShopId = ""
                        assign_to_shop_tv.text = ""
                    }
                    "6" -> {
                        rl_assign_to_dd.visibility = View.GONE
                        assign_to_rl.visibility = View.GONE
                        rl_amount.visibility = View.GONE
                        tv_name_asterisk_mark.visibility = View.VISIBLE
                        ll_doc_extra_info.visibility = View.GONE
                        rl_select_retailer.visibility = View.GONE
                        rl_select_dealer.visibility = View.GONE
                        assign_to_shop_rl.visibility = View.GONE
                        checkExtraInfoWillVisibleOrNot()
                        shopImage.visibility = View.VISIBLE
                        setMargin(false)
                        til_no.hint = getString(R.string.contact_number)
                        til_mail.hint = getString(R.string.contact_email)
                        til_name.hint = getString(R.string.contact_name)
                        assign_to_tv.hint = "Assigned to " + Pref.ppText
                        rl_entity_main.visibility = View.GONE
                        assignedToPPId = ""
                        assignedToDDId = ""

                        tv_select_dealer.text = ""
                        dealerId = ""
                        retailerId = ""
                        tv_select_retailer.text = ""
                        assignedToShopId = ""
                        assign_to_shop_tv.text = ""
                    }
                    "7" -> {
                        rl_assign_to_dd.visibility = View.GONE
                        assign_to_rl.visibility = View.VISIBLE
                        rl_amount.visibility = View.GONE
                        tv_name_asterisk_mark.visibility = View.VISIBLE
                        ll_doc_extra_info.visibility = View.GONE
                        rl_select_retailer.visibility = View.GONE
                        rl_select_dealer.visibility = View.GONE
                        assign_to_shop_rl.visibility = View.GONE
                        checkExtraInfoWillVisibleOrNot()
                        shopImage.visibility = View.VISIBLE
                        setMargin(false)
                        til_no.hint = getString(R.string.contact_number)
                        til_mail.hint = getString(R.string.contact_email)
                        til_name.hint = getString(R.string.contact_name)
                        assign_to_tv.hint = "Assigned to"
                        rl_entity_main.visibility = View.GONE

                        if(assignPPList != null && assignPPList.isNotEmpty()) {
                            assignedToPPId = assignPPList[0].pp_id!!
                            assign_to_tv.text = assignPPList[0].pp_name
                        }
                        assignedToDDId = ""

                        tv_select_dealer.text = ""
                        dealerId = ""
                        retailerId = ""
                        tv_select_retailer.text = ""
                        assignedToShopId = ""
                        assign_to_shop_tv.text = ""
                    }
                    "8" -> {
                        rl_assign_to_dd.visibility = View.GONE
                        assign_to_rl.visibility = View.GONE
                        rl_amount.visibility = View.GONE
                        tv_name_asterisk_mark.visibility = View.GONE
                        ll_doc_extra_info.visibility = View.VISIBLE
                        ll_extra_info.visibility = View.GONE
                        rl_select_retailer.visibility = View.GONE
                        rl_select_dealer.visibility = View.GONE
                        //ownerEmail.imeOptions = EditorInfo.IME_ACTION_NEXT
                        shopImage.visibility = View.GONE
                        assign_to_shop_rl.visibility = View.GONE
                        setMargin(true)
                        til_no.hint = getString(R.string.contact_number)
                        til_mail.hint = getString(R.string.contact_email)
                        til_name.hint = getString(R.string.contact_name)
                        assign_to_tv.hint = "Assigned to " + Pref.ppText
                        rl_entity_main.visibility = View.GONE
                        assignedToPPId = ""
                        assignedToDDId = ""
                        tv_select_dealer.text = ""
                        dealerId = ""
                        retailerId = ""
                        tv_select_retailer.text = ""
                        assignedToShopId = ""
                        assign_to_shop_tv.text = ""
                    }
                    "10" -> {
                        if (Pref.isDDShowForMeeting) {
                            rl_assign_to_dd.visibility = View.VISIBLE

                            if(assignDDList != null && assignDDList.isNotEmpty()) {
                                assignedToDDId = assignDDList[0].dd_id!!
                                tv_assign_to_dd.text = assignDDList[0].dd_name
                            }
                        }
                        else
                            rl_assign_to_dd.visibility = View.GONE

                        if (Pref.isDDMandatoryForMeeting)
                            tv_dd_asterisk_mark.visibility = View.VISIBLE
                        else
                            tv_dd_asterisk_mark.visibility = View.GONE

                        assign_to_rl.visibility = View.GONE
                        rl_amount.visibility = View.GONE
                        rl_type.visibility = View.VISIBLE
                        shopImage.visibility = View.VISIBLE
                        rl_select_retailer.visibility = View.GONE
                        setMargin(false)
                        ll_doc_extra_info.visibility = View.GONE
                        ll_extra_info.visibility = View.GONE
                        rl_select_dealer.visibility = View.GONE
                        assign_to_shop_rl.visibility = View.GONE
                        til_no.hint = getString(R.string.owner_contact_number)
                        til_mail.hint = getString(R.string.owner_email)
                        til_name.hint = getString(R.string.owner_name)
                        assign_to_tv.hint = "Assigned to " + Pref.ppText
                        rl_entity_main.visibility = View.GONE
                        assignedToPPId = ""

                        tv_select_dealer.text = ""
                        dealerId = ""
                        retailerId = ""
                        tv_select_retailer.text = ""
                        assignedToShopId = ""
                        assign_to_shop_tv.text = ""
                    }
                    "11" -> {
                        rl_assign_to_dd.visibility = View.GONE
                        assign_to_rl.visibility = View.GONE
                        rl_amount.visibility = View.GONE
                        shopImage.visibility = View.VISIBLE
                        setMargin(false)
                        ll_doc_extra_info.visibility = View.GONE
                        ll_extra_info.visibility = View.GONE
                        rl_select_dealer.visibility = View.GONE
                        assign_to_shop_rl.visibility = View.VISIBLE
                        rl_entity_main.visibility = View.GONE
                        til_no.hint = getString(R.string.owner_contact_number)
                        til_mail.hint = getString(R.string.owner_email)
                        til_name.hint = getString(R.string.owner_name)
                        assign_to_tv.hint = "Assigned to " + Pref.ppText

                        if (Pref.isShowRetailerEntity)
                            rl_select_retailer.visibility = View.VISIBLE
                        else
                            rl_select_retailer.visibility = View.GONE

                        tv_select_dealer.text = ""
                        dealerId = ""
                        retailerId = ""
                        tv_select_retailer.text = ""
                        assignedToShopId = ""
                        assign_to_shop_tv.text = ""
                    }
                    else -> {
                        rl_assign_to_dd.visibility = View.GONE
                        assign_to_rl.visibility = View.GONE
                        rl_amount.visibility = View.GONE
                        rl_type.visibility = View.GONE
                        shopImage.visibility = View.VISIBLE
                        setMargin(false)
                        ll_doc_extra_info.visibility = View.GONE
                        ll_extra_info.visibility = View.GONE
                        rl_select_retailer.visibility = View.GONE
                        rl_select_dealer.visibility = View.GONE
                        assign_to_shop_rl.visibility = View.GONE
                        til_no.hint = getString(R.string.owner_contact_number)
                        til_mail.hint = getString(R.string.owner_email)
                        til_name.hint = getString(R.string.owner_name)
                        assign_to_tv.hint = "Assigned to " + Pref.ppText
                        rl_entity_main.visibility = View.GONE
                        assignedToPPId = ""
                        assignedToDDId = ""

                        tv_select_dealer.text = ""
                        dealerId = ""
                        retailerId = ""
                        tv_select_retailer.text = ""
                        assignedToShopId = ""
                        assign_to_shop_tv.text = ""
                    }
                }
            } else {
                rl_assign_to_dd.visibility = View.GONE
                assign_to_rl.visibility = View.GONE
                rl_amount.visibility = View.GONE
                rl_type.visibility = View.GONE
                shopImage.visibility = View.VISIBLE
                setMargin(false)
                ll_doc_extra_info.visibility = View.GONE
                ll_extra_info.visibility = View.GONE
                rl_select_retailer.visibility = View.GONE
                rl_select_dealer.visibility = View.GONE
                assign_to_shop_rl.visibility = View.GONE
                til_no.hint = getString(R.string.contact_number)
                til_mail.hint = getString(R.string.only_email)
                til_name.hint = getString(R.string.owner_name)
                assign_to_tv.hint = "Assigned to " + Pref.ppText
                rl_entity_main.visibility = View.GONE
                assignedToPPId = ""
                assignedToDDId = ""

                tv_select_dealer.text = ""
                dealerId = ""
                retailerId = ""
                tv_select_retailer.text = ""
                assignedToShopId = ""
                assign_to_shop_tv.text = ""
            }

            popup.dismiss()
        }

        shop_type_TV.setOnClickListener(View.OnClickListener {
            type_TV.text = getString(R.string.shop_type)
            shop_name_TL.hint = getString(R.string.shop_name)
            addShopData.type = "1"
            rl_assign_to_dd.visibility = View.VISIBLE
            assign_to_rl.visibility = View.VISIBLE
            rl_amount.visibility = View.GONE
            popup.dismiss()
        })
        distributor_tv.setOnClickListener(View.OnClickListener {
            type_TV.text = getString(R.string.distributor_type)
            shop_name_TL.hint = getString(R.string.company_name)
            addShopData.type = "4"
            assign_to_rl.visibility = View.VISIBLE
            rl_assign_to_dd.visibility = View.GONE
            rl_amount.visibility = View.GONE
            popup.dismiss()
        })
        pp_tv.setOnClickListener(View.OnClickListener {
            type_TV.text = getString(R.string.pp_type)
            shop_name_TL.hint = getString(R.string.company_name)
            addShopData.type = "2"
            rl_assign_to_dd.visibility = View.GONE
            assign_to_rl.visibility = View.GONE
            rl_amount.visibility = View.GONE
            popup.dismiss()
        })
        new_party_tv.setOnClickListener(View.OnClickListener {
            type_TV.text = getString(R.string.new_party_type)
            shop_name_TL.hint = getString(R.string.company_name)
            addShopData.type = "3"
            rl_assign_to_dd.visibility = View.GONE
            assign_to_rl.visibility = View.GONE
            rl_amount.visibility = View.GONE
            popup.dismiss()
        })
        diamond_tv.setOnClickListener(View.OnClickListener {
            type_TV.text = getString(R.string.diamond_type)
            shop_name_TL.hint = getString(R.string.shop_name)
            addShopData.type = "5"
            rl_assign_to_dd.visibility = View.VISIBLE
            assign_to_rl.visibility = View.VISIBLE
            rl_amount.visibility = View.VISIBLE
            popup.dismiss()
        })

        popup.setBackgroundDrawable(ColorDrawable(Color.WHITE))
//        popup.showAsDropDown(view)
        // Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
        val OFFSET_X = resources.getDimensionPixelOffset(R.dimen._50sdp)
        val OFFSET_Y = resources.getDimensionPixelOffset(R.dimen._80sdp)
        popup.showAtLocation(view, Gravity.CENTER_VERTICAL, OFFSET_X, OFFSET_Y)
//        popup.update()
//        popup.showAtLocation(layout , Gravity.CENTER, 0, 0);
    }

    fun updateLabel() {
        /*if (isDOB == 0) {
            addShopData.dob = AppUtils.getFormattedDateForApi(myCalendar.time)
            dob_EDT.setText(AppUtils.changeAttendanceDateFormat(AppUtils.getDobFormattedDate(myCalendar.time)))
            shopDataModel.dateOfBirth = AppUtils.getDobFormattedDate(myCalendar.time)
        } else if (isDOB == 1) {
            addShopData.date_aniversary = AppUtils.getFormattedDateForApi(myCalendar.time)
            date_aniverdary_EDT.setText(AppUtils.changeAttendanceDateFormat(AppUtils.getDobFormattedDate(myCalendar.time)))
            shopDataModel.dateOfAniversary = AppUtils.getDobFormattedDate(myCalendar.time)
        } else if (isDOB == 2) {
            nextVisitDate = AppUtils.getFormattedDateForApi(myCalendar.time)
            next_visit_date_EDT.setText(AppUtils.changeAttendanceDateFormat(AppUtils.getDobFormattedDate(myCalendar.time)))
            //shopDataModel.dateOfAniversary = AppUtils.getDobFormattedDate(myCalendar.time)
        }*/


        when (isDOB) {
            0 -> {
                addShopData.date_aniversary = AppUtils.getFormattedDateForApi(myCalendar.time)
                date_aniverdary_EDT.setText(AppUtils.changeAttendanceDateFormat(AppUtils.getDobFormattedDate(myCalendar.time)))
                shopDataModel.dateOfAniversary = AppUtils.getDobFormattedDate(myCalendar.time)
            }
            1 -> {
                addShopData.dob = AppUtils.getFormattedDateForApi(myCalendar.time)
                dob_EDT.setText(AppUtils.changeAttendanceDateFormat(AppUtils.getDobFormattedDate(myCalendar.time)))
                shopDataModel.dateOfBirth = AppUtils.getDobFormattedDate(myCalendar.time)
            }
            2 -> {
                nextVisitDate = AppUtils.getFormattedDateForApi(myCalendar.time)
                next_visit_date_EDT.setText(AppUtils.changeAttendanceDateFormat(AppUtils.getDobFormattedDate(myCalendar.time)))
            }
            3 -> {
                addShopData.family_member_dob = AppUtils.getDobFormattedDate(myCalendar.time)
                family_mem_dob_EDT.setText(AppUtils.changeAttendanceDateFormat(AppUtils.getDobFormattedDate(myCalendar.time)))
                shopDataModel.family_member_dob = AppUtils.getFormattedDateForApi(myCalendar.time)
            }
            4 -> {
                addShopData.addtional_dob = AppUtils.getDobFormattedDate(myCalendar.time)
                add_dob_EDT.setText(AppUtils.changeAttendanceDateFormat(AppUtils.getDobFormattedDate(myCalendar.time)))
                shopDataModel.add_dob = AppUtils.getFormattedDateForApi(myCalendar.time)
            }
            5 -> {
                addShopData.addtional_doa = AppUtils.getDobFormattedDate(myCalendar.time)
                add_date_aniverdary_EDT.setText(AppUtils.changeAttendanceDateFormat(AppUtils.getDobFormattedDate(myCalendar.time)))
                shopDataModel.add_doa = AppUtils.getFormattedDateForApi(myCalendar.time)
            }
            6 -> {
                addShopData.doc_family_member_dob = AppUtils.getDobFormattedDate(myCalendar.time)
                doc_family_mem_dob_EDT.setText(AppUtils.changeAttendanceDateFormat(AppUtils.getDobFormattedDate(myCalendar.time)))
                shopDataModel.doc_family_dob = AppUtils.getFormattedDateForApi(myCalendar.time)
            }
            7 -> {
                addShopData.assistant_dob = AppUtils.getDobFormattedDate(myCalendar.time)
                assistant_dob_EDT.setText(AppUtils.changeAttendanceDateFormat(AppUtils.getDobFormattedDate(myCalendar.time)))
                shopDataModel.assistant_dob = AppUtils.getFormattedDateForApi(myCalendar.time)
            }
            8 -> {
                addShopData.assistant_doa = AppUtils.getDobFormattedDate(myCalendar.time)
                assistant_date_aniverdary_EDT.setText(AppUtils.changeAttendanceDateFormat(AppUtils.getDobFormattedDate(myCalendar.time)))
                shopDataModel.assistant_doa = AppUtils.getFormattedDateForApi(myCalendar.time)
            }
            9 -> {
                addShopData.assistant_family_dob = AppUtils.getDobFormattedDate(myCalendar.time)
                assistant_family_mem_dob_EDT.setText(AppUtils.changeAttendanceDateFormat(AppUtils.getDobFormattedDate(myCalendar.time)))
                shopDataModel.assistant_family_dob = AppUtils.getFormattedDateForApi(myCalendar.time)
            }
        }
    }

    override fun onDestroy() {
        AppUtils.hideSoftKeyboard((mContext as DashboardActivity))
        mTess.onDestroy()
        super.onDestroy()

    }


    private var popupWindow: PopupWindow? = null

    private fun callThemePopUp(anchorView: View, arr_themes: ArrayList<String>) {

        popupWindow = PopupWindow(ThemedropDownMenu(R.layout.inflate_items_popup_window, arr_themes, anchorView), anchorView.width, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow!!.setBackgroundDrawable(BitmapDrawable())
        popupWindow!!.isOutsideTouchable = true
        popupWindow!!.showAsDropDown(anchorView)
        popupWindow!!.update()

    }

    private fun ThemedropDownMenu(layout: Int, arr_roomType: ArrayList<String>, textview: View): View {
        var view: View? = null
        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(layout, null, false)
        themeListPopupWindowAdapter = InflateThemeListPopupWindowAdapter(mContext, arr_roomType, object : onPopupMenuClickListener {
            override fun onPopupMenuClick(name: String, position: Int) {
                assign_to_tv.text = name
                popupWindow?.dismiss()
            }
        })

        val listView = view.findViewById<ListView>(R.id.lv_roomType)!!
        listView.adapter = themeListPopupWindowAdapter


        return view
    }

    fun refreshList() {
        getShopTypeListApi(shop_type_RL, true)
    }

    fun saveAudio() {
        try {
            filePath = audioFile?.absolutePath!!
            audio_record_date_EDT.setText(filePath)
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun processImage(/*file: File?*/ picTexts: ArrayList<String>, isCopy: Boolean) {
        /*var image = BitmapFactory.decodeResource(mContext.resources, R.drawable.test_image_old)

        progress_wheel.spin()
        doAsync {
            val utfText = mTess.getOCRResult(image)

            uiThread {
                progress_wheel.stopSpinning()
                Log.e("Add Shop", "Visiting Card Utf Text===============> $utfText")
                ownerName.setText(mTess.extractName(utfText))
                ownerEmail.setText(mTess.extractEmail(utfText))
                ownerNumber.setText(mTess.extractPhone(utfText))
            }
        }*/

        /*var image: Bitmap?
        Glide.with(mContext)
                .asBitmap()
                .load(file?.absolutePath)
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        image = resource

                        progress_wheel.spin()
                        doAsync {
                            val utfText = mTess.getOCRResult(image)

                            uiThread {
                                progress_wheel.stopSpinning()
                                Log.e("Add Shop", "Visiting Card Utf Text===============> $utfText")
                                ownerName.setText(mTess.extractName(utfText))
                                ownerEmail.setText(mTess.extractEmail(utfText))
                                ownerNumber.setText(mTess.extractPhone(utfText))
                            }
                        }
                    }
                })*/

        if (!isCopy) {
            ownerNumber.setText("")
            ownerEmail.setText("")

            val numeric = ArrayList<String>()

            picTexts.forEach {
                val removeHyphen = it.replace("-", "")
                val removePlus = removeHyphen.replace("+", "")
                val finalString = removePlus.replace(" ", "")
                if (mTess.isNumeric(finalString))
                    numeric.add(it)
            }

            try {
                if (numeric.size > 0) {
                    ownerNumber.setText(mTess.parseResults(numeric)[0].replace(" ", ""))
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            for (i in picTexts.indices) {
                val email = mTess.extractEmail(picTexts[i])
                ownerEmail.setText(email)

                if (!TextUtils.isEmpty(email))
                    break
            }
        }
        else
            ShowCardDetailsDialog.newInstance(picTexts).show((mContext as DashboardActivity).supportFragmentManager, "")
        //ownerNumber.setText(extractPhone(picTexts).get(0))
    }

    fun getRandomNumberString(): String? {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        val rnd = Random()
        val number = rnd.nextInt(9999)

        // this will convert any number sequence into 6 character.

        val rnd1 = Random()
        val number1 = rnd.nextInt(99999)

        val nam  = "1"+String.format("%4d", number)+String.format("%5d", number1)
        //return String.format("%5d", nam)
        return  nam
    }

}