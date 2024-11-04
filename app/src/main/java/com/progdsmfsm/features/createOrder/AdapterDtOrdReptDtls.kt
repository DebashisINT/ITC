package com.progdsmfsm.features.createOrder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.progdsmfsm.R
import kotlinx.android.synthetic.main.row_ord_dtls_report_dtls.view.tv_row_dor_dtls_shop_name
import kotlinx.android.synthetic.main.row_ord_dtls_report_dtls.view.tv_row_dor_dtls_total_amt
import kotlinx.android.synthetic.main.row_ord_dtls_report_dtls.view.tv_row_dor_dtls_total_qty

class AdapterDtOrdReptDtls(var mContext:Context,var mList:ArrayList<DateWiseOrdReportFrag.OrdReportDtls>):
    RecyclerView.Adapter<AdapterDtOrdReptDtls.DtOrdReptDtlsViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DtOrdReptDtlsViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.row_ord_dtls_report_dtls,parent,false)
        return DtOrdReptDtlsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: DtOrdReptDtlsViewHolder, position: Int) {
        holder.bindItems()
    }

    inner class DtOrdReptDtlsViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        fun bindItems(){
            itemView.apply {
                try {
                    tv_row_dor_dtls_shop_name.text=mList.get(adapterPosition).shop_name
                    tv_row_dor_dtls_total_qty.text=mList.get(adapterPosition).orderQtyTotal.toBigInteger().toString()
                    tv_row_dor_dtls_total_amt.text=String.format("%.02f",mList.get(adapterPosition).orderValueTotal.toBigDecimal())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

}