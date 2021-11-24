package com.breezefsmdsm.features.photoReg

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.breezefsmdsm.R
import com.breezefsmdsm.base.presentation.BaseFragment
import com.breezefsmdsm.features.photoReg.model.UserListResponseModel
import com.breezefsmdsm.widgets.AppCustomTextView

class PhotoRegAadhaarFragment: BaseFragment(), View.OnClickListener {
    private lateinit var mContext: Context
    private lateinit var nameTV: AppCustomTextView

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

        nameTV.text = RegisTerFaceFragment.user_name!!
    }

    override fun onClick(v: View?) {

    }
}