package com.breezedsm.features.createOrder

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.breezedsm.R
import com.breezedsm.app.Pref
import com.breezedsm.app.domain.NewOrderDataEntity
import com.breezedsm.app.utils.AppUtils
import kotlinx.android.synthetic.main.row_order_list.view.cv_row_ord_delete
import kotlinx.android.synthetic.main.row_order_list.view.cv_row_ord_edit
import kotlinx.android.synthetic.main.row_order_list.view.iv_row_ord_sync
import kotlinx.android.synthetic.main.row_order_list.view.ll_ord_list_modify_dt_root
import kotlinx.android.synthetic.main.row_order_list.view.ll_row_ord_sync
import kotlinx.android.synthetic.main.row_order_list.view.row_ord_list_modify_dt
import kotlinx.android.synthetic.main.row_order_list.view.tv_row_ord_amt
import kotlinx.android.synthetic.main.row_order_list.view.tv_row_ord_date
import kotlinx.android.synthetic.main.row_order_list.view.tv_row_ord_id
import kotlinx.android.synthetic.main.row_order_list.view.tv_row_ord_view
import java.text.DecimalFormat

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
            itemView.tv_row_ord_amt.text = "Amt: "+String.format("%.02f",ordL.get(adapterPosition).order_total_amt.toDouble())
            itemView.tv_row_ord_id.text = "Order ID: "+ordL.get(adapterPosition).order_id

            if(ordL.get(adapterPosition).isUploaded){
                itemView.iv_row_ord_sync.setImageResource(R.drawable.ic_registered_shop_sync)
            }else{
                itemView.iv_row_ord_sync.setImageResource(R.drawable.ic_registered_shop_not_sync)
            }

            if(ordL.get(adapterPosition).order_edit_date_time.equals("")){
                itemView.ll_ord_list_modify_dt_root.visibility = View.GONE
            }else{
                itemView.ll_ord_list_modify_dt_root.visibility = View.VISIBLE
                itemView.row_ord_list_modify_dt.text = "Modify Date-Time : "+AppUtils.convertToDateLikeOrderFormat(ordL.get(adapterPosition).order_edit_date_time.split(" ").get(0)) +
                        "  "+ordL.get(adapterPosition).order_edit_date_time.split(" ").get(1)
            }

            itemView.ll_row_ord_sync.setOnClickListener {
                if(!ordL.get(adapterPosition).isUploaded){
                    listner.onSyncClick(ordL.get(adapterPosition))
                }
            }

            itemView.tv_row_ord_view.setOnClickListener {
                println("click_check tv_row_ord_view")
                itemView.tv_row_ord_view.isEnabled = false
                listner.onViewClick(ordL.get(adapterPosition))
                Handler().postDelayed(Runnable {
                    itemView.tv_row_ord_view.isEnabled = true
                }, 300)
            }
            if(Pref.IsOrderEditEnable){
                itemView.cv_row_ord_edit.visibility = View.VISIBLE
            }else{
                itemView.cv_row_ord_edit.visibility = View.GONE
            }
            if(Pref.IsOrderDeleteEnable){
                itemView.cv_row_ord_delete.visibility = View.VISIBLE
            }else{
                itemView.cv_row_ord_delete.visibility = View.GONE
            }
            itemView.cv_row_ord_edit.setOnClickListener {
                listner.onEditClick(ordL.get(adapterPosition))
            }
            itemView.cv_row_ord_delete.setOnClickListener {
                itemView.cv_row_ord_delete.isEnabled = false
                listner.onDelClick(ordL.get(adapterPosition))
                Handler().postDelayed(Runnable {
                    itemView.cv_row_ord_delete.isEnabled = true
                }, 300)
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