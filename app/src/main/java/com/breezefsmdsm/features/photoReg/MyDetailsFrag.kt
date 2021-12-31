package com.breezefsmdsm.features.photoReg

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.breezefsmdsm.R
import com.breezefsmdsm.base.presentation.BaseFragment

class MyDetailsFrag : BaseFragment(), View.OnClickListener{

    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.frag_my_details, container, false)
        initView(view)
        return view
    }

    private fun initView(view:View){

    }

    override fun onClick(v: View?) {

    }
}