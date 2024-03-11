package com.breezedsm.features.settings.presentation

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.breezedsm.R
import com.breezedsm.base.presentation.BaseFragment
import com.breezedsm.features.changepassword.presentation.ChangePasswordDialog
import com.breezedsm.widgets.AppCustomTextView
import android.content.Context
import java.util.*
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.ImageView
import androidx.cardview.widget.CardView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.FileProvider
import com.breezedsm.app.Pref
import com.breezedsm.app.utils.AutoStartHelper
import com.breezedsm.app.utils.PermissionUtils
import com.breezedsm.features.dashboard.presentation.DashboardActivity
import com.fasterxml.jackson.databind.util.ClassUtil.getPackageName
import java.io.File
import java.io.FileInputStream

/**
 * Created by Pratishruti on 31-10-2017.
 */
class SettingsFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mContext: Context

    private lateinit var cv_auto_start: CardView
    private lateinit var cv_over_other_apps: CardView
    private lateinit var cv_camera: CardView
    private lateinit var cv_loc: CardView
    private lateinit var cv_phone: CardView
    private lateinit var cv_gallery: CardView
    private lateinit var cv_audio: CardView
    private lateinit var cv_calender: CardView
    private lateinit var ll_settings_main: LinearLayout
    private lateinit var cv_openCam: CardView


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        initView(view)
        initClickListener()

        return view

    }

    private fun initView(view: View) {
        view.apply {
            cv_auto_start = findViewById(R.id.cv_auto_start)
            cv_over_other_apps = findViewById(R.id.cv_over_other_apps)
            cv_camera = findViewById(R.id.cv_camera)
            cv_loc = findViewById(R.id.cv_loc)
            cv_phone = findViewById(R.id.cv_phone)
            cv_gallery = findViewById(R.id.cv_gallery)
            cv_audio = findViewById(R.id.cv_audio)
            cv_calender = findViewById(R.id.cv_calender)
            ll_settings_main = findViewById(R.id.ll_settings_main)
            cv_openCam = findViewById(R.id.cv_open_cam)
        }
    }

    private fun initClickListener() {
        cv_auto_start.setOnClickListener(this)
        cv_openCam.setOnClickListener(this)
        cv_over_other_apps.setOnClickListener(this)
        cv_camera.setOnClickListener(this)
        cv_loc.setOnClickListener(this)
        cv_phone.setOnClickListener(this)
        cv_gallery.setOnClickListener(this)
        cv_audio.setOnClickListener(this)
        cv_calender.setOnClickListener(this)
        ll_settings_main.setOnClickListener(null)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.cv_auto_start -> {
                AutoStartHelper.instance.getAutoStartPermission(mContext)
            }

            R.id.cv_over_other_apps -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M /*&& !Settings.canDrawOverlays(this)*/) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${mContext.packageName}"))
                    startActivity(intent)
                }
            }

            R.id.cv_camera -> {
                openAppInfo()
            }

            R.id.cv_loc -> {
                openAppInfo()
            }

            R.id.cv_phone -> {
                openAppInfo()
            }

            R.id.cv_gallery -> {
                openAppInfo()
            }

            R.id.cv_audio -> {
                openAppInfo()
            }

            R.id.cv_calender -> {
                openAppInfo()
            }
            R.id.cv_open_cam->{
                initPermissionCheck()
            }
        }
    }

    private fun openAppInfo() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", mContext.packageName, null)
        intent.data = uri
        startActivity(intent)
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
                proceedProcess()
            }

            override fun onPermissionNotGranted() {
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.accept_permission))
            }

        },permissionList)// arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    fun onRequestPermission(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionUtils?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun proceedProcess(){
        captureImage()
    }


    private var mCurrentPhotoPath: String = ""
    private var filePath: String = ""
    fun captureImage() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(mContext.packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
                if (photoFile != null) {
                    val photoURI: Uri = if (Build.VERSION.SDK_INT >= 24) {
                        FileProvider.getUriForFile(mContext, mContext.packageName + ".provider", photoFile)
                    } else
                        Uri.fromFile(photoFile)
                    mCurrentPhotoPath = "file:" + photoFile.absolutePath
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 0)
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivityForResult(takePictureIntent, 110)
                }
            } catch (ex: Exception) {
                // Error occurred while creating the File
                ex.printStackTrace()
                return
            }
        }
    }

    fun createImageFile(): File {
        val imageFileName = "fieldtrackingsystem" +  /*Calendar.getInstance(Locale.ENGLISH).time*/ java.util.UUID.randomUUID()
        //val storageDir = File(Environment.getExternalStorageDirectory().toString() + File.separator + "fieldtrackingsystem" + File.separator)
        val storageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "fieldtrackingsystem" + File.separator)
        storageDir.mkdirs()
        return File.createTempFile(imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            getCameraImage(data)

            setImage(filePath)
        }
    }

    fun getCameraImage(data: Intent?) {

        val isCamera: Boolean
        isCamera = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            if (data == null) {
                true
            } else {
                val action = data.action
                if (action == null) {
                    false
                } else {
                    action == android.provider.MediaStore.ACTION_IMAGE_CAPTURE
                }
            }
        } else
            true

        var selectedImageUri: Uri?
        if (isCamera) {
            selectedImageUri = Uri.parse(mCurrentPhotoPath) // outputFileUri;
            // outputFileUri = null;
        } else {
            selectedImageUri = data?.data
        }
        if (selectedImageUri == null)
            selectedImageUri = Uri.parse(mCurrentPhotoPath)
        val filemanagerstring = selectedImageUri!!.path

        val selectedImagePath = getPath(mContext as Activity, selectedImageUri)

        when {
            selectedImagePath != null -> filePath = selectedImagePath
            filemanagerstring != null -> filePath = filemanagerstring
            else -> {
            }
        }
    }

    fun getPath(mActivity: Activity, uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = mActivity.managedQuery(uri, projection, null, null, null)
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } else
            return null
    }

    fun setImage(filePath: String) {
        val file = File(filePath)
        var newFile: File? = null

        getBitmap(filePath)
    }

    fun getBitmap(path: String?) {
        var bitmap: Bitmap? = null
        try {
            val f = File(path)
            val options: BitmapFactory.Options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            bitmap = BitmapFactory.decodeStream(FileInputStream(f), null, options)

            val simpleDialog = Dialog(mContext)
            simpleDialog.setCancelable(false)
            simpleDialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            simpleDialog.setContentView(R.layout.dialog_message_iv)
            val iv_dialog_msg = simpleDialog.findViewById(R.id.iv_dialog_msg) as ImageView
            val okBtn = simpleDialog.findViewById(R.id.tv_message_ok) as AppCustomTextView
            iv_dialog_msg.setImageBitmap(bitmap)

            okBtn.setOnClickListener({ view ->
                simpleDialog.cancel()
            })
            simpleDialog.show()


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}