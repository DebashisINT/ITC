package com.breezefsmdsm.features.photoReg.present

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.breezefsmdsm.R
import com.breezefsmdsm.app.AppDatabase
import com.breezefsmdsm.app.NetworkConstant
import com.breezefsmdsm.app.Pref
import com.breezefsmdsm.app.domain.ProspectEntity
import com.breezefsmdsm.app.domain.StageEntity
import com.breezefsmdsm.app.utils.AppUtils
import com.breezefsmdsm.app.utils.Toaster
import com.breezefsmdsm.base.presentation.BaseActivity
import com.breezefsmdsm.features.nearbyshops.api.ShopListRepositoryProvider
import com.breezefsmdsm.features.nearbyshops.model.StageListResponseModel
import com.breezefsmdsm.widgets.AppCustomTextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_achv.progress_wheel
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import timber.log.Timber

//Rev 1.0 Suman 28-08-2023 UpdateDSTypeStatusDialog manti id 26764

class UpdateDSTypeStatusDialog: DialogFragment(), View.OnClickListener {
    private lateinit var mContext: Context

    private lateinit var dialogHeader: AppCustomTextView
    private lateinit var dialogCancel: AppCustomTextView
    private lateinit var selType: AppCustomTextView
    private lateinit var dialogOk: AppCustomTextView
    private lateinit var tv_ds_type_dropdown: AppCustomTextView
    private lateinit var cv_ds_type_main: CardView

    private  var selectedTypeID: String = ""
    private  var selectedTypeName: String = ""

    private var dsTypePopupWindow: PopupWindow? = null
    //Rev 1.0 Suman 28-08-2023 UpdateDSTypeStatusDialog manti id 26764
    //private var dsList:ArrayList<ProspectEntity> = ArrayList()
    private var dsList:ArrayList<StageEntity> = ArrayList()

    companion object {

        private lateinit var mHeader: String
        private lateinit var mLeftBtn: String
        private lateinit var mRightBtn: String
        private lateinit var mSelType: String
        private lateinit var muserIdForTypeUpdate: String
        private var mIsCancelable: Boolean = true
        private lateinit var mListener: OnDSButtonClickListener

        fun getInstance(header: String, leftCancel: String, rightOk: String, isCancelable: Boolean,selectedType:String, usrId:String,listener: OnDSButtonClickListener): UpdateDSTypeStatusDialog {
            val cardFragment = UpdateDSTypeStatusDialog()
            mHeader = header
            mLeftBtn = leftCancel
            mRightBtn = rightOk
            mIsCancelable = isCancelable
            mSelType=selectedType
            muserIdForTypeUpdate=usrId
            mListener = listener
            return cardFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        dialog?.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window!!.setBackgroundDrawableResource(R.drawable.rounded_corner_white_bg)
        val v = inflater.inflate(R.layout.dialog_fragment_add_update_ds, container, false)
        isCancelable = mIsCancelable
        initView(v)

        //dsList = AppDatabase.getDBInstance()!!.prosDao().getAll() as ArrayList<ProspectEntity>
        dsList = AppDatabase.getDBInstance()!!.stageDao().getAll() as ArrayList<StageEntity>

        return v
    }

    private fun initView(v: View) {
        dialogHeader = v.findViewById(R.id.dialog_header_TV)
        dialogCancel = v.findViewById(R.id.cancel_TV)
        dialogOk = v.findViewById(R.id.ok_TV)
        dialogOk.isSelected = true
        tv_ds_type_dropdown= v.findViewById(R.id.tv_ds_type_dropdown)
        cv_ds_type_main= v.findViewById(R.id.cv_ds_type_main)
        selType= v.findViewById(R.id.tv_dialog_frag_add_update_ds_selected_type)

        dialogOk.text=mRightBtn
        dialogCancel.text= mLeftBtn

        dialogHeader.text="Select Type for\n"+mHeader
        tv_ds_type_dropdown.hint = "Select Employee Type"
        selType.text=mSelType
        tv_ds_type_dropdown.setOnClickListener(this)
        dialogCancel.setOnClickListener(this)
        dialogOk.setOnClickListener(this)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }


    interface OnDSButtonClickListener {
        fun onLeftClick()
        fun onRightClick(typeId: String,typeName:String,usrId:String)
    }

    override fun onClick(v: View?) {
        when (v!!.id){
            R.id.tv_ds_type_dropdown ->{
                if (dsTypePopupWindow != null && dsTypePopupWindow?.isShowing!!)
                    dsTypePopupWindow?.dismiss()
                else {
                    if (dsList == null || dsList!!.size == 0) {
                        //Toaster.msgShort(mContext, getString(R.string.no_data_available))
                        //return
                        geStageApi()
                    }else{
                        callMeetingTypeDropDownPopUp()
                    }

                }
            }
            R.id.cancel_TV ->{
                mListener.onLeftClick()
                dismiss()
            }
            R.id.ok_TV ->{
                mListener.onRightClick(selectedTypeID,selectedTypeName,muserIdForTypeUpdate)
                dismiss()
            }
        }
    }

    //Begin Rev 1.0 Suman 28-08-2023 UpdateDSTypeStatusDialog manti id 26764
    private fun geStageApi() {
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
                            AppDatabase.getDBInstance()?.stageDao()?.deleteAll()
                            doAsync {
                                response.stage_list?.forEach {
                                    val stageEntity = StageEntity()
                                    AppDatabase.getDBInstance()?.stageDao()?.insertAll(stageEntity.apply {
                                        stage_id = it.id
                                        stage_name = it.name
                                    })
                                }
                                uiThread {
                                    dsList = AppDatabase.getDBInstance()!!.stageDao().getAll() as ArrayList<StageEntity>
                                    callMeetingTypeDropDownPopUp()
                                }
                            }
                        } else {
                            Toaster.msgShort(mContext, getString(R.string.no_data_available))
                        }
                    } else {
                        Toaster.msgShort(mContext, getString(R.string.no_data_available))
                    }
                }, { error ->
                    Timber.d("GET STAGE DATA : " + "ERROR : " + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + error.localizedMessage)
                    error.printStackTrace()
                })
        )
    }
    //End 1.0 Suman 28-08-2023 UpdateDSTypeStatusDialog manti id 26764

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun callMeetingTypeDropDownPopUp() {

        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val customView = inflater!!.inflate(R.layout.popup_meeting_type, null)

        //dsTypePopupWindow = PopupWindow(customView, resources.getDimensionPixelOffset(R.dimen._220sdp), RelativeLayout.LayoutParams.WRAP_CONTENT)
        dsTypePopupWindow = PopupWindow(customView, resources.getDimensionPixelOffset(R.dimen._220sdp), resources.getDimensionPixelOffset(R.dimen._220sdp))
        val rv_ds_type_list = customView.findViewById(R.id.rv_meeting_type_list) as RecyclerView
        rv_ds_type_list.layoutManager = LinearLayoutManager(mContext)

        dsTypePopupWindow?.elevation = 200f
        dsTypePopupWindow?.isFocusable = true
        dsTypePopupWindow?.update()

        //rv_ds_type_list.adapter = DsStatusAdapter(mContext, dsList,object:DsStatusListner{
        rv_ds_type_list.adapter = DsStatusAdapter1(mContext, dsList,object:DsStatusListner{
            override fun getDSInfoOnLick(obj: ProspectEntity) {
               /* tv_ds_type_dropdown.text = obj.pros_name!!
                selectedTypeID=obj.pros_id!!
                selectedTypeName=obj.pros_name!!
                dsTypePopupWindow?.dismiss()*/
            }

            override fun getDSInfoOnLick(obj: StageEntity) {
                tv_ds_type_dropdown.text = obj.stage_name!!
                selectedTypeID=obj.stage_id!!
                selectedTypeName=obj.stage_name!!
                dsTypePopupWindow?.dismiss()
            }
        })


        if (dsTypePopupWindow != null && !dsTypePopupWindow?.isShowing!!) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                cv_ds_type_main.post(Runnable {
                    dsTypePopupWindow?.showAsDropDown(tv_ds_type_dropdown, resources.getDimensionPixelOffset(R.dimen._10sdp), resources.getDimensionPixelOffset(R.dimen._20sdp), Gravity.BOTTOM)
                })
            } else {
                dsTypePopupWindow?.showAsDropDown(tv_ds_type_dropdown, tv_ds_type_dropdown.width - tv_ds_type_dropdown?.width!!, 0)
            }
        }
    }




}