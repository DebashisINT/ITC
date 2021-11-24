package com.breezefsmdsm.features.photoReg

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.breezefsmdsm.R
import com.breezefsmdsm.base.presentation.BaseFragment
import com.breezefsmdsm.features.dashboard.presentation.DashboardActivity
import com.breezefsmdsm.features.photoReg.model.UserListResponseModel
import com.breezefsmdsm.widgets.AppCustomTextView
import com.themechangeapp.pickimage.PermissionHelper
import java.io.File
import java.io.FileInputStream

class PhotoRegAadhaarFragment: BaseFragment(), View.OnClickListener {
    private lateinit var mContext: Context
    private lateinit var nameTV: AppCustomTextView
    private lateinit var aadhaarImg: ImageView

    lateinit var imgUri:Uri
    private var imagePath: String = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    companion object{
        var user_id: String? = null
        var user_name: String? = null
        var user_login_id: String? = null
        var user_contactid: String? = null
        fun getInstance(objects: Any): PhotoRegAadhaarFragment {
            val photoRegAadhaarFragment = PhotoRegAadhaarFragment()
            if (!TextUtils.isEmpty(objects.toString())) {

                var obj = objects as UserListResponseModel

                user_id=obj!!.user_id.toString()
                user_name=obj!!.user_name
                user_login_id=obj!!.user_login_id
                user_contactid=obj!!.user_contactid
            }
            return photoRegAadhaarFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_photo_reg_aadhaar, container, false)
        initView(view)
        return view
    }

    private fun initView(view:View){
        nameTV = view.findViewById(R.id.tv_frag_photo_reg_aadhaar_name)
        aadhaarImg = view.findViewById(R.id.iv_frag_photo_reg_aadhaar_pic)

        nameTV.text = RegisTerFaceFragment.user_name!!

        launchCamera()
    }

    fun launchCamera() {
        if (PermissionHelper.checkCameraPermission(mContext as DashboardActivity) && PermissionHelper.checkStoragePermission(mContext as DashboardActivity)) {
            /*val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, (mContext as DashboardActivity).getPhotoFileUri(System.currentTimeMillis().toString() + ".png"))
            (mContext as DashboardActivity).startActivityForResult(intent, PermissionHelper.REQUEST_CODE_CAMERA)*/

            (mContext as DashboardActivity).captureImage()
        }
    }

    fun setImage(imgRealPath: Uri, fileSizeInKB: Long) {
        imgUri=imgRealPath
        imagePath = imgRealPath.toString()

        getBitmap(imgRealPath.path)

    }

    fun getBitmap(path: String?) {
        var bitmap: Bitmap? = null
        try {
            val f = File(path)
            val options: BitmapFactory.Options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            bitmap = BitmapFactory.decodeStream(FileInputStream(f), null, options)
            aadhaarImg.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //return bitmap
    }

    override fun onClick(v: View?) {

    }
}