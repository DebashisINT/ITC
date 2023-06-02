package com.breezefsmdsm.features.nearbyshops.diaog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.breezefsmdsm.R
import com.breezefsmdsm.app.domain.AddShopDBModelEntity
import com.breezefsmdsm.widgets.AppCustomTextView

class ShopInactiveDialog: DialogFragment() {

    private lateinit var mContext: Context
    private lateinit var header: AppCustomTextView
    private lateinit var close: ImageView
    private lateinit var rv_shop: RecyclerView

    companion object{
        private lateinit var onSelect: (AddShopDBModelEntity) -> Unit
        private var mList: ArrayList<AddShopDBModelEntity>? = null

        fun newInstance(pList: ArrayList<AddShopDBModelEntity>, function: (AddShopDBModelEntity) -> Unit): ShopInactiveDialog {
            val dialogFragment = ShopInactiveDialog()
            mList = pList
            onSelect = function
            return dialogFragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        dialog?.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val v = inflater.inflate(R.layout.dialog_shop_inactive, container, false)
        isCancelable = false
        initView(v)
        return v
    }

    private fun initView(v: View){
        header=v!!.findViewById(R.id.tv_shop_list_header)
        close=v!!.findViewById(R.id.iv_dialog_shop_list_close_icon)
        rv_shop=v!!.findViewById(R.id.rv_shop_inactive_list)

        header.text = "Inactive Shop List"

        close.setOnClickListener {
            dismiss()
        }

        rv_shop.adapter = AdapterInactiveShop(mContext,mList!!, object : AdapterInactiveShop.OnClick{
            override fun onItemClick(shop_id:String) {
                dismiss()
                mList!!.filter { it.shop_id.equals(shop_id) }.firstOrNull()?.let { onSelect(it) }
            }
        })
    }

}