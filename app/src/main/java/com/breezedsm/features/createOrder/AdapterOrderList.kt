package com.breezedsm.features.createOrder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.breezedsm.R
import com.breezedsm.app.Pref
import com.breezedsm.app.domain.NewOrderDataEntity
import com.breezedsm.app.utils.AppUtils
import kotlinx.android.synthetic.main.row_order_list.view.iv_row_ord_sync
import kotlinx.android.synthetic.main.row_order_list.view.tv_row_ord_amt
import kotlinx.android.synthetic.main.row_order_list.view.tv_row_ord_date
import kotlinx.android.synthetic.main.row_order_list.view.tv_row_ord_delete
import kotlinx.android.synthetic.main.row_order_list.view.tv_row_ord_edit
import kotlinx.android.synthetic.main.row_order_list.view.tv_row_ord_id
import kotlinx.android.synthetic.main.row_order_list.view.tv_row_ord_view

class AdapterOrderList(var mContext: Context, var ordL: ArrayList<NewOrderDataEntity>,var listner:OnActionClick) :
    RecyclerView.Adapter<AdapterOrderList.OrderListViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderListViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.row_order_list, parent, false)
        return OrderListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return ordL.size
    }

    override fun onBindViewHolder(holder: OrderListViewHolder, position: Int) {
        holder.bindItems(mContext,ordL,listner)
    }

    inner class OrderListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(context: Context,ordL:ArrayList<NewOrderDataEntity>,listner : OnActionClick){
            itemView.tv_row_ord_date.text = AppUtils.convertToDateLikeOrderFormat(ordL.get(adapterPosition).order_date)//AppUtils.getFormatedDateNew(ordL.get(adapterPosition).order_date,"yyyy-mm-dd","dd-mm-yyyy")
            itemView.tv_row_ord_amt.text = "Amt: ₹ "+String.format("%.02f",ordL.get(adapterPosition).order_total_amt.toDouble())
            itemView.tv_row_ord_id.text = "Order ID: "+ordL.get(adapterPosition).order_id

            if(ordL.get(adapterPosition).isUploaded){
                itemView.iv_row_ord_sync.setImageResource(R.drawable.ic_registered_shop_sync)
            }else{
                itemView.iv_row_ord_sync.setImageResource(R.drawable.ic_registered_shop_not_sync)
            }

            itemView.iv_row_ord_sync.setOnClickListener {
                if(!ordL.get(adapterPosition).isUploaded){
                    listner.onSyncClick(ordL.get(adapterPosition))
                }
            }

            itemView.tv_row_ord_view.setOnClickListener {
                listner.onViewClick(ordL.get(adapterPosition))
            }
            if(Pref.OrderEditEnable){
                itemView.tv_row_ord_edit.visibility = View.VISIBLE
            }else{
                itemView.tv_row_ord_edit.visibility = View.GONE
            }
            if(Pref.OrderDeleteEnable){
                itemView.tv_row_ord_delete.visibility = View.VISIBLE
            }else{
                itemView.tv_row_ord_delete.visibility = View.GONE
            }
            itemView.tv_row_ord_edit.setOnClickListener {
                listner.onEditClick(ordL.get(adapterPosition))
            }
        }
    }

    interface OnActionClick {
        fun onViewClick(obj:NewOrderDataEntity)
        fun onSyncClick(obj:NewOrderDataEntity)
        fun onEditClick(obj:NewOrderDataEntity)
        fun onDelClick(obj:NewOrderDataEntity)
    }
}