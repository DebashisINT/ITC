package com.fsmitc.features.photoReg

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import com.fsmitc.base.presentation.BaseActivity
import com.fsmitc.base.presentation.BaseFragment
import com.fsmitc.features.dashboard.presentation.DashboardActivity
import com.fsmitc.features.photoReg.adapter.AdapterUserList
import com.fsmitc.features.photoReg.adapter.PhotoRegUserListner
import com.fsmitc.features.photoReg.api.GetUserListPhotoRegProvider
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
import java.net.URLEncoder

class ProtoRegistrationFragment:BaseFragment(),View.OnClickListener {

    private lateinit var mContext: Context
    private lateinit var mRv_userList: RecyclerView
    private lateinit var progress_wheel: com.pnikosis.materialishprogress.ProgressWheel
    var userList:ArrayList<UserListResponseModel> = ArrayList()
    var userList_temp:ArrayList<UserListResponseModel> = ArrayList()
    private var adapter: AdapterUserList?= null


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


    private fun OpenDialogForAdhaarReg(obj: UserListResponseModel) {
        val simpleDialog = Dialog(mContext)
        simpleDialog.setCancelable(true)
        simpleDialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        simpleDialog.setContentView(R.layout.dialog_adhaar_reg)
        val dialogEtCardNumber1 = simpleDialog.findViewById(R.id.dialog_adhaar_reg_et_no_et_1) as AppCustomEditText
        val dialogEtCardNumber2 = simpleDialog.findViewById(R.id.dialog_adhaar_reg_et_no_et_2) as AppCustomEditText
        val dialogEtCardNumber3 = simpleDialog.findViewById(R.id.dialog_adhaar_reg_et_no_et_3) as AppCustomEditText

        val dialogEtFeedback = simpleDialog.findViewById(R.id.tv_dialog_adhaar_reg_feedback) as AppCustomEditText
        val dialogCameraclick = simpleDialog.findViewById(R.id.tv_dialog_adhaar_reg_iv_camera) as ImageView

        val dialogConfirm = simpleDialog.findViewById(R.id.tv_dialog_adhaar_reg_confirm) as AppCustomTextView

        dialogCameraclick.setOnClickListener{v: View? ->
            showPictureDialog()
        }

        dialogConfirm.setOnClickListener({ view ->
            simpleDialog.cancel()

        })
        simpleDialog.show()

    }


    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(mContext)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture Image", "Select file from file manager")
        pictureDialog.setItems(pictureDialogItems,
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        0 -> selectImageInAlbum()
                        1 -> {
                            launchCamera()
                        }
                        2 -> {
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



    fun setImage(filePath: String) {

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
    }

}