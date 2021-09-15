package com.fsmitc.features.photoReg

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder.with
import com.bumptech.glide.GenericTransitionOptions.with
import com.bumptech.glide.Glide.with
import com.elvishew.xlog.XLog
import com.fsmitc.R
import com.fsmitc.app.NetworkConstant
import com.fsmitc.app.Pref
import com.fsmitc.app.SearchListener
import com.fsmitc.app.types.FragType
import com.fsmitc.app.uiaction.IntentActionable
import com.fsmitc.app.utils.AppUtils
import com.fsmitc.app.utils.PermissionUtils
import com.fsmitc.app.utils.ProcessImageUtils_v1
import com.fsmitc.app.utils.Toaster
import com.fsmitc.base.BaseResponse
import com.fsmitc.base.presentation.BaseActivity
import com.fsmitc.base.presentation.BaseFragment
import com.fsmitc.features.dashboard.presentation.DashboardActivity
import com.fsmitc.features.myjobs.api.MyJobRepoProvider
import com.fsmitc.features.myjobs.model.WIPImageSubmit
import com.fsmitc.features.photoReg.adapter.AdapterUserList
import com.fsmitc.features.photoReg.adapter.PhotoRegUserListner
import com.fsmitc.features.photoReg.api.GetUserListPhotoRegProvider
import com.fsmitc.features.photoReg.model.AadhaarSubmitData
import com.fsmitc.features.photoReg.model.DeleteUserPicResponse
import com.fsmitc.features.photoReg.model.GetUserListResponse
import com.fsmitc.features.photoReg.model.UserListResponseModel
import com.fsmitc.mappackage.SendBrod
import com.fsmitc.widgets.AppCustomEditText
import com.fsmitc.widgets.AppCustomTextView
import com.squareup.picasso.Picasso
import com.themechangeapp.pickimage.PermissionHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_photo_registration.*
import kotlinx.android.synthetic.main.row_user_list_face_regis.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileInputStream
import java.net.URLEncoder

class ProtoRegistrationFragment:BaseFragment(),View.OnClickListener {

    private lateinit var mContext: Context
    private lateinit var mRv_userList: RecyclerView
    private lateinit var progress_wheel: com.pnikosis.materialishprogress.ProgressWheel
    var userList:ArrayList<UserListResponseModel> = ArrayList()
    var userList_temp:ArrayList<UserListResponseModel> = ArrayList()
    private var adapter: AdapterUserList?= null
    private var str_aadhaarNo:String=""

    private lateinit var et_attachment: AppCustomEditText
    private lateinit var et_photo: AppCustomEditText

    private var isAttachment = false
    private var dataPath = ""
    private var imagePath = ""


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    companion object{
        var user_uid: String = ""
        fun getInstance(objects: Any): ProtoRegistrationFragment {
            val protoRegistrationFragment = ProtoRegistrationFragment()
            if (!TextUtils.isEmpty(objects.toString())) {
                user_uid=objects.toString()
            }
            return protoRegistrationFragment
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_photo_registration, container, false)
        initView(view)


        (mContext as DashboardActivity).setSearchListener(object : SearchListener {
            override fun onSearchQueryListener(query: String) {
                if (query.isBlank()) {
                    userList?.let {
                        adapter?.refreshList(it)
                        tv_cust_no.text = "Total customer(s): " + it.size
                    }
                } else {
                    adapter?.filter?.filter(query)
                }
            }
        })

        return view
    }

    private fun initView(view:View){
        et_attachment = view!!.findViewById(R.id.et_attachment)
        et_photo = view!!.findViewById(R.id.et_photo)
        mRv_userList=view!!.findViewById(R.id.rv_frag_photo_reg)
        progress_wheel = view.findViewById(R.id.progress_wheel)

        mRv_userList.layoutManager = LinearLayoutManager(mContext)

        initPermissionCheck()
        progress_wheel.spin()
        Handler(Looper.getMainLooper()).postDelayed({
            callUSerListApi()
        }, 300)

    }

    private var permissionUtils: PermissionUtils? = null
    private fun initPermissionCheck() {
        permissionUtils = PermissionUtils(mContext as Activity, object : PermissionUtils.OnPermissionListener {
            override fun onPermissionGranted() {
                callUSerListApi()
            }

            override fun onPermissionNotGranted() {
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.accept_permission))
            }

        }, arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    private fun callUSerListApi(){
        userList.clear()
        val repository = GetUserListPhotoRegProvider.provideUserListPhotoReg()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
                repository.getUserListApi(Pref.user_id!!,Pref.session_token!!)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            progress_wheel.stopSpinning()
                            var response = result as GetUserListResponse
                            if (response.status == NetworkConstant.SUCCESS) {
                                if(response.user_list!!.size>0 && response.user_list!!!=null){

                                    doAsync {
                                        userList=response.user_list!!

                                        uiThread {
                                            setAdapter()
                                        }
                                    }

                                }else{
                                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_date_found))
                                }
//
                            } else {
                             (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_date_found))
                            }
                        }, { error ->
                            progress_wheel.stopSpinning()
                            error.printStackTrace()
//                            (mContext as DashboardActivity).showSnackMessage("ERROR")
                        })
        )

    }

    private fun setAdapter(){

        //Toast.makeText(mContext,userList.size.toString(),Toast.LENGTH_SHORT).show()


        adapter = AdapterUserList(mContext,userList!!,object : PhotoRegUserListner{

            override fun getUserInfoOnLick(obj: UserListResponseModel) {
                (mContext as DashboardActivity).loadFragment(FragType.RegisTerFaceFragment, true, obj)
            }

            override fun getPhoneOnLick(phone: String) {
                IntentActionable.initiatePhoneCall(mContext, phone)
            }

            override fun getWhatsappOnLick(phone: String) {
                var phone="+91"+phone
                sendWhats(phone)
            }

            override fun deletePicOnLick(obj: UserListResponseModel) {


                val simpleDialogg = Dialog(mContext)
                simpleDialogg.setCancelable(false)
                simpleDialogg.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                simpleDialogg.setContentView(R.layout.dialog_yes_no)
                val dialogHeader = simpleDialogg.findViewById(R.id.dialog_cancel_order_header_TV) as AppCustomTextView
                dialogHeader.text="Are you sure?"
                val dialogYes = simpleDialogg.findViewById(R.id.tv_dialog_yes_no_yes) as AppCustomTextView
                val dialogNo = simpleDialogg.findViewById(R.id.tv_dialog_yes_no_no) as AppCustomTextView

                dialogYes.setOnClickListener( { view ->
                    simpleDialogg.cancel()

                    if (AppUtils.isOnline(mContext)){
                        deletePicApi(obj.user_id.toString())
                    }else{
                        (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
                    }


                })
                dialogNo.setOnClickListener( { view ->
                    simpleDialogg.cancel()
                })
                simpleDialogg.show()


            }

            override fun viewPicOnLick(img_link: String, name: String) {
                progress_wheel.spin()
                val simpleDialogg = Dialog(mContext)
                simpleDialogg.setCancelable(true)
                simpleDialogg.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                simpleDialogg.setContentView(R.layout.view_face_img)


                val faceImg = simpleDialogg.findViewById(R.id.iv_face_img) as ImageView
                val faceName = simpleDialogg.findViewById(R.id.face_name) as AppCustomTextView
                faceName.text = name

                        Picasso.get()
                                .load(img_link)
                                .resize(500, 500)
                                .into(faceImg)
                progress_wheel.stopSpinning()

                simpleDialogg.show()
            }

            override fun getAadhaarOnLick(obj: UserListResponseModel) {
                OpenDialogForAdhaarReg(obj)
            }
        },{
            it
        })

        mRv_userList.adapter = adapter
    }


    override fun onClick(p0: View?) {

    }


    fun deletePicApi(usr_id:String){

        val repository = GetUserListPhotoRegProvider.providePhotoReg()
        BaseActivity.compositeDisposable.add(
                repository.deleteUserPicApi(usr_id,Pref.session_token!!)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            progress_wheel.stopSpinning()
                            var response = result as DeleteUserPicResponse
                            if (response.status == NetworkConstant.SUCCESS) {
                                callUSerListApi()

                            } else {
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                            }
                        }, { error ->
                            progress_wheel.stopSpinning()
                            error.printStackTrace()
//                            (mContext as DashboardActivity).showSnackMessage("ERROR")
                        })
        )

    }

    /////////////////////////////////////////////

    private fun sendWhats(phone: String) {
        val packageManager: PackageManager = mContext.getPackageManager()
        val i = Intent(Intent.ACTION_VIEW)
        try {
            val url = "https://api.whatsapp.com/send?phone=" + phone + "&text=" + URLEncoder.encode("", "UTF-8")
            i.setPackage("com.whatsapp")
            i.data = Uri.parse(url)
            if (i.resolveActivity(packageManager) != null) {
                this.startActivity(i)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    lateinit var simpleDialog:Dialog
    lateinit var iv_takenImg:ImageView
    private fun OpenDialogForAdhaarReg(obj: UserListResponseModel) {
        simpleDialog = Dialog(mContext)
        simpleDialog.setCancelable(true)
        simpleDialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        simpleDialog.setContentView(R.layout.dialog_adhaar_reg)
        val dialogEtCardNumber1 = simpleDialog.findViewById(R.id.dialog_adhaar_reg_et_no_et_1) as AppCustomEditText
        val dialogEtCardNumber2 = simpleDialog.findViewById(R.id.dialog_adhaar_reg_et_no_et_2) as AppCustomEditText
        val dialogEtCardNumber3 = simpleDialog.findViewById(R.id.dialog_adhaar_reg_et_no_et_3) as AppCustomEditText

        val dialogEtFeedback = simpleDialog.findViewById(R.id.tv_dialog_adhaar_reg_feedback) as AppCustomEditText
        val dialogCameraclick = simpleDialog.findViewById(R.id.tv_dialog_adhaar_reg_iv_camera) as ImageView
        val dialogCameraclickCancel = simpleDialog.findViewById(R.id.iv_dialog_aadhaar_reg_cancel_pic) as ImageView
        iv_takenImg = simpleDialog.findViewById(R.id.iv_dialog_aadhaar_reg_pic) as ImageView

        val dialogConfirm = simpleDialog.findViewById(R.id.tv_dialog_adhaar_reg_confirm) as AppCustomTextView
        val dialogCancel = simpleDialog.findViewById(R.id.tv_dialog_adhaar_reg_cancel) as AppCustomTextView

        if(obj.RegisteredAadhaarNo!=null && obj.RegisteredAadhaarNo!!.length>0){
            dialogEtCardNumber1.setText(obj.RegisteredAadhaarNo!!.get(0).toString()+obj.RegisteredAadhaarNo!!.get(1).toString()+
                    obj.RegisteredAadhaarNo!!.get(2).toString()+obj.RegisteredAadhaarNo!!.get(3).toString())
            dialogEtCardNumber2.setText(obj.RegisteredAadhaarNo!!.get(4).toString()+obj.RegisteredAadhaarNo!!.get(5).toString()+
                    obj.RegisteredAadhaarNo!!.get(6).toString()+obj.RegisteredAadhaarNo!!.get(7).toString())
            dialogEtCardNumber3.setText(obj.RegisteredAadhaarNo!!.get(8).toString()+obj.RegisteredAadhaarNo!!.get(9).toString()+
                    obj.RegisteredAadhaarNo!!.get(10).toString()+obj.RegisteredAadhaarNo!!.get(11).toString())
        }

        dialogCameraclick.setOnClickListener{v: View? ->
            showPictureDialog()
        }
        dialogCameraclickCancel.setOnClickListener{v: View? ->
            iv_takenImg.setImageBitmap(null)
            dataPath=""
            imagePath=""
        }
        dialogCancel.setOnClickListener{v: View? ->
            progress_wheel.stopSpinning()
            simpleDialog.cancel()
        }

        dialogConfirm.setOnClickListener({ view ->
            //simpleDialog.cancel()

            if(dialogEtCardNumber1.text.toString().length==4){
                if(dialogEtCardNumber2.text.toString().length==4){
                    if(dialogEtCardNumber3.text.toString().length==4){
                        ////////
                        val simpleDialogInner = Dialog(mContext)
                        simpleDialogInner.setCancelable(false)
                        simpleDialogInner.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        simpleDialogInner.setContentView(R.layout.dialog_yes_no)
                        val dialogHeader = simpleDialogInner.findViewById(R.id.dialog_cancel_order_header_TV) as AppCustomTextView
                        val dialogHeaderTTV = simpleDialogInner.findViewById(R.id.dialog_yes_no_headerTV) as AppCustomTextView
                        dialogHeader.text="Are you sure?"
                        dialogHeaderTTV.text="Hi "+Pref.user_name+"!"
                        val dialogYes = simpleDialogInner.findViewById(R.id.tv_dialog_yes_no_yes) as AppCustomTextView
                        val dialogNo = simpleDialogInner.findViewById(R.id.tv_dialog_yes_no_no) as AppCustomTextView

                        dialogYes.setOnClickListener( { view ->
                            simpleDialogInner.cancel()
                            str_aadhaarNo=dialogEtCardNumber1.text.toString()+dialogEtCardNumber2.text.toString()+dialogEtCardNumber3.text.toString()
                            simpleDialog.cancel()
                            submitAadhaarDetails(obj,dialogEtFeedback.text.toString())
                        })
                        dialogNo.setOnClickListener( { view ->
                            simpleDialogInner.cancel()
                        })
                        simpleDialogInner.show()

                        ///////

                    }else{
                        dialogEtCardNumber3.setError("Please Enter Aadhaad No")
                        dialogEtCardNumber3.requestFocus()
                    }
                }else{
                    dialogEtCardNumber2.setError("Please Enter Aadhaad No")
                    dialogEtCardNumber2.requestFocus()
                }
            }else{
                dialogEtCardNumber1.setError("Please Enter Aadhaad No")
                dialogEtCardNumber1.requestFocus()
            }

        })
        simpleDialog.show()

    }


    private fun submitAadhaarDetails(obj: UserListResponseModel,feedBac:String){
        var aadhaarSubmitData: AadhaarSubmitData=AadhaarSubmitData()
        aadhaarSubmitData.session_token=Pref.session_token.toString()
        aadhaarSubmitData.aadhaar_holder_user_id=obj.user_id.toString()
        aadhaarSubmitData.aadhaar_holder_user_contactid=obj.user_contactid.toString()
        aadhaarSubmitData.aadhaar_no=str_aadhaarNo
        aadhaarSubmitData.date=AppUtils.getCurrentDateForShopActi()
        aadhaarSubmitData.feedback=feedBac
        aadhaarSubmitData.address=""

        val repository = GetUserListPhotoRegProvider.provideUserListPhotoReg()
        BaseActivity.compositeDisposable.add(
                repository.sendUserAadhaarApi(aadhaarSubmitData)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as BaseResponse
                            progress_wheel.stopSpinning()
                            //(mContext as DashboardActivity).showSnackMessage(response.message!!)
                            //(mContext as DashboardActivity).showSnackMessage("Aadhaar registered successfully")

                            if (response.status == NetworkConstant.SUCCESS) {
                                if (!TextUtils.isEmpty(et_attachment.text.toString().trim()) || !TextUtils.isEmpty(et_photo.text.toString().trim())) {
                                    val imgList = java.util.ArrayList<WIPImageSubmit>()

                                    if (!TextUtils.isEmpty(et_attachment.text.toString()))
                                        imgList.add(WIPImageSubmit(dataPath, "attachment"))

                                    if (!TextUtils.isEmpty(et_photo.text.toString()))
                                        imgList.add(WIPImageSubmit(imagePath, "image"))

                                    val repository = GetUserListPhotoRegProvider.jobMultipartRepoProvider()
                                    BaseActivity.compositeDisposable.add(
                                            repository.submitAadhaarDetails(aadhaarSubmitData, imgList, mContext)
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribeOn(Schedulers.io())
                                                    .subscribe({ result ->
                                                        val response = result as BaseResponse
                                                        progress_wheel.stopSpinning()
                                                        //(mContext as DashboardActivity).showSnackMessage(response.message!!)
                                                        //(mContext as DashboardActivity).showSnackMessage("Aadhar registered successfully")
                                                        if (response.status == NetworkConstant.SUCCESS) {
                                                            aadharSuccessDialogShow()
                                                            //voiceAttendanceMsg("Aadhaar registered successfully")
                                                           /* Handler(Looper.getMainLooper()).postDelayed({
                                                                callUSerListApi()
                                                            }, 300)*/
                                                            //(mContext as DashboardActivity).loadFragment(FragType.ProtoRegistrationFragment, false, "")
                                                        }

                                                    }, { error ->
                                                        progress_wheel.stopSpinning()
                                                        error.printStackTrace()
                                                        (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                                                    })
                                    )
                                }else{
                                    aadharSuccessDialogShow()
                                    //voiceAttendanceMsg("Aadhaar registered successfully")
                                    //(mContext as DashboardActivity).loadFragment(FragType.ProtoRegistrationFragment, false, "")
                                }
                            }

                        }, { error ->
                            progress_wheel.stopSpinning()
                            error.printStackTrace()
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                        })
        )


    }

    private fun aadharSuccessDialogShow() {
        val simpleDialogAdhhar = Dialog(mContext)
        simpleDialogAdhhar.setCancelable(false)
        simpleDialogAdhhar.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        simpleDialogAdhhar.setContentView(R.layout.dialog_message)
        val dialogHeader = simpleDialogAdhhar.findViewById(R.id.dialog_message_header_TV) as AppCustomTextView
        val dialogHeaderTTV = simpleDialogAdhhar.findViewById(R.id.dialog_message_headerTV) as AppCustomTextView
        dialogHeader.text="Aadhaar registered successfully."
        dialogHeaderTTV.text="Hi "+Pref.user_name+"!"
        val tv_message_ok = simpleDialogAdhhar.findViewById(R.id.tv_message_ok) as AppCustomTextView

        tv_message_ok.setOnClickListener( { view ->
            simpleDialogAdhhar.cancel()
            voiceAttendanceMsg("Aadhaar registered successfully.")
        })
        simpleDialogAdhhar.show()
    }


    private fun voiceAttendanceMsg(msg: String) {
        if (Pref.isVoiceEnabledForAttendanceSubmit) {
            val speechStatus = (mContext as DashboardActivity).textToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, null)
            if (speechStatus == TextToSpeech.ERROR)
                Log.e("Add Day Start", "TTS error in converting Text to Speech!");
        }
        Handler(Looper.getMainLooper()).postDelayed({
            callUSerListApi()
        }, 300)
    }


    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(mContext)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture Image", "Select file from file manager")
        pictureDialog.setItems(pictureDialogItems,
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        0 ->{
                            isAttachment=false
                            selectImageInAlbum()
                        }
                        1 -> {
                            isAttachment=false
                            launchCamera()
                        }
                        2 -> {
                            isAttachment=true
                            (mContext as DashboardActivity).openFileManager()
                        }
                    }
                })
        pictureDialog.show()
    }

    private fun selectImageInAlbum() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        (mContext as DashboardActivity).startActivityForResult(galleryIntent, PermissionHelper.REQUEST_CODE_STORAGE)
    }


    private fun launchCamera() {
        (mContext as DashboardActivity).captureImage()
    }



    /*fun setImage(file: File) {
        if (isAttachment) {
            et_attachment.setText(file.name)
            dataPath = file.absolutePath
        }
        else {
            imagePath = file.absolutePath
            et_photo.setText(file.name)
        }
    }*/

    fun setImage(filePath: String) {
        val file = File(filePath)
        var newFile: File? = null
        progress_wheel.spin()
        doAsync {
            val processImage = ProcessImageUtils_v1(mContext, file, 50)
            newFile = processImage.ProcessImageSelfie()
            uiThread {
                if (newFile != null) {
                    XLog.e("=========Image from new technique==========")
                    val fileSize = AppUtils.getCompressImage(filePath)
                    var tyy=filePath

                    if (isAttachment) {
                        et_attachment.setText(newFile!!.name)
                        dataPath = newFile!!.absolutePath
                    }
                    else {
                        imagePath = newFile!!.absolutePath
                        et_photo.setText(newFile!!.name)

                        val f = File(newFile!!.absolutePath)
                        val options: BitmapFactory.Options = BitmapFactory.Options()
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888
                        var bitmap = BitmapFactory.decodeStream(FileInputStream(f), null, options)
                        iv_takenImg.setImageBitmap(bitmap)
                        var tt="asd"

                    }
                    progress_wheel.stopSpinning()
                } else {
                    // Image compression
                    val fileSize = AppUtils.getCompressImage(filePath)
                    var tyy=filePath
                    progress_wheel.stopSpinning()
                }
            }
        }
    }

    fun setDoc(file: File) {
        if (isAttachment) {
            et_attachment.setText(file.name)
            dataPath = file.absolutePath
        }
    }



    /* fun setImage(filePath: String) {

         val file = File(filePath)
         var newFile: File? = null

         progress_wheel.spin()
         doAsync {

             val processImage = ProcessImageUtils_v1(mContext, file, 50)
             newFile = processImage.ProcessImage()

             uiThread {
                 if (newFile != null) {
                     XLog.e("=========Image from new technique==========")
                     //reimbursementEditPic(newFile!!.length(), newFile?.absolutePath!!)
                 } else {
                     // Image compression
                     val fileSize = AppUtils.getCompressImage(filePath)
                     //reimbursementEditPic(fileSize, filePath)
                 }
             }
         }
     }*/

}