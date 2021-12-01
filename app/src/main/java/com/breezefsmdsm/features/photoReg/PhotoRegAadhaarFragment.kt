package com.breezefsmdsm.features.photoReg

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.breezefsmdsm.MySingleton
import com.breezefsmdsm.R
import com.breezefsmdsm.app.utils.AppUtils
import com.breezefsmdsm.app.utils.Toaster
import com.breezefsmdsm.base.presentation.BaseFragment
import com.breezefsmdsm.features.dashboard.presentation.DashboardActivity
import com.breezefsmdsm.features.photoReg.model.UserListResponseModel
import com.breezefsmdsm.widgets.AppCustomTextView
import com.pnikosis.materialishprogress.ProgressWheel
import com.themechangeapp.pickimage.PermissionHelper
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.util.HashMap

class PhotoRegAadhaarFragment: BaseFragment(), View.OnClickListener {
    private lateinit var mContext: Context
    private lateinit var nameTV: AppCustomTextView
    private lateinit var aadhaarImg: ImageView
    private lateinit var progress_wheel: ProgressWheel
    private lateinit var submitBtn: Button

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
        submitBtn = view.findViewById(R.id.btn_frag_photo_reg_aadhaar_upload)
        progress_wheel = view.findViewById(R.id.progress_wheel)
        progress_wheel.stopSpinning()

        submitBtn.setOnClickListener(this)

        nameTV.text = RegisTerFaceFragment.user_name!!

        launchCamera()
    }

    fun launchCamera() {
        if (PermissionHelper.checkCameraPermission(mContext as DashboardActivity) && PermissionHelper.checkStoragePermission(mContext as DashboardActivity)) {
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

    override fun onClick(p0: View?) {
        if(p0!=null){
            when(p0.id){
                R.id.btn_frag_photo_reg_aadhaar_upload -> {
                    if(imagePath.length>0 && imagePath!="") {
                        val simpleDialogg = Dialog(mContext)
                        simpleDialogg.setCancelable(false)
                        simpleDialogg.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        simpleDialogg.setContentView(R.layout.dialog_yes_no)
                        val dialogHeader = simpleDialogg.findViewById(R.id.dialog_cancel_order_header_TV) as AppCustomTextView
                        dialogHeader.text="Do you want to submit ?"
                        val dialogYes = simpleDialogg.findViewById(R.id.tv_dialog_yes_no_yes) as AppCustomTextView
                        val dialogNo = simpleDialogg.findViewById(R.id.tv_dialog_yes_no_no) as AppCustomTextView

                        dialogYes.setOnClickListener( { view ->
                            simpleDialogg.cancel()
                            if (AppUtils.isOnline(mContext)){
                                uploadAadhaarPic()
                            }else{
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
                            }

                        })
                        dialogNo.setOnClickListener( { view ->
                            simpleDialogg.cancel()
                        })
                    }
                }
            }
        }
    }


    private fun uploadAadhaarPic(){
        progress_wheel.spin()

        //after success
        //faceAadhaarCompareParam()

    }

    private fun faceAadhaarCompareParam( doc1:String, doc2:String) {
        try {
            val jsonObject = JSONObject()
            val notificationBody = JSONObject()
            notificationBody.put("document1", doc1)
            notificationBody.put("document2", doc2)
            jsonObject.put("data", notificationBody)
            val jsonArray = JSONArray()
            jsonObject.put("task_id", "11986")
            jsonObject.put("group_id", "11986")
            faceAadhaarCompare(jsonObject)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun faceAadhaarCompare(notification: JSONObject) {
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest("https://eve.idfy.com/v3/tasks/sync/compare/face", notification,
                object : Response.Listener<JSONObject?> {
                    override fun onResponse(response: JSONObject?) {
                        var jObj:JSONObject= JSONObject()
                        jObj=response!!.getJSONObject("result")
                        var isMatch=jObj.getBoolean("is_a_match")
                        var matchScore=jObj.getDouble("match_score")
                        extractAadhaarDtls()
                    }
                },
                object : Response.ErrorListener {
                    override fun onErrorResponse(error: VolleyError?) {
                        var yy="wre"

                    }
                }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["api-key"] = "dfe0a602-7e79-4a5b-af00-509fc0e8349a"
                params["Content-Type"] = "application/json"
                params["account-id"] = "aaa73f1c1bdb/fa4cf738-2dda-41db-b0e5-0b406ebe6d2f"
                return params
            }
        }
        jsonObjectRequest.setRetryPolicy(DefaultRetryPolicy(
                120000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
        MySingleton.getInstance(mContext.applicationContext)!!.addToRequestQueue(jsonObjectRequest)

    }

    fun extractAadhaarDtls(){
        try {
            val jsonObject = JSONObject()
            val notificationBody = JSONObject()
            notificationBody.put("document1", "http://3.7.30.86:82/CommonFolder/AadharImage/119842021-11-22image_1637569676078.jpg")
            notificationBody.put("consent", "yes")
            jsonObject.put("data", notificationBody)
            val jsonArray = JSONArray()
            jsonObject.put("task_id", "11986")
            jsonObject.put("group_id", "11986")


            val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest("https://eve.idfy.com/v3/tasks/sync/extract/ind_aadhaar", jsonObject,
                    object : Response.Listener<JSONObject?> {
                        override fun onResponse(response: JSONObject?) {
                            var jObj:JSONObject= JSONObject()
                            jObj=response!!.getJSONObject("result")
                            var tt=jObj.getJSONObject("extraction_output")
                            var ttt=tt.getString("date_of_birth")
                            var tttt=tt.getString("name_on_card")
                            var aad_no=tt.getString("id_number")
                            var gg="asd"
                            Toaster.msgShort(mContext,"DOB "+ttt.toString()+" Name "+tttt.toString()+" aadhaar No "+aad_no.toString())
                            //checkCustom("http://3.7.30.86:82/CommonFolder/FaceImageDetection/EMS0000070.jpg","http://3.7.30.86:82/CommonFolder/FaceImageDetection/EMS0000070.jpg")
                        }
                    },
                    object : Response.ErrorListener {
                        override fun onErrorResponse(error: VolleyError?) {
                            var yy="wre"

                        }
                    }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["api-key"] = "dfe0a602-7e79-4a5b-af00-509fc0e8349a"
                    params["Content-Type"] = "application/json"
                    params["account-id"] = "aaa73f1c1bdb/fa4cf738-2dda-41db-b0e5-0b406ebe6d2f"
                    return params
                }
            }
            jsonObjectRequest.setRetryPolicy(DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
            MySingleton.getInstance(mContext.applicationContext)!!.addToRequestQueue(jsonObjectRequest)



        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


}