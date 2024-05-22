package com.breezedsm.features.createOrder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.breezedsm.R
import com.breezedsm.app.utils.AppUtils
import kotlinx.android.synthetic.main.row_ord_dtls_report.view.rv_ord_dtls_list_dtls
import kotlinx.android.synthetic.main.row_ord_dtls_report.view.tv_ord_dtls_date
import kotlinx.android.synthetic.main.row_ord_dtls_report.view.tv_ord_dtls_total_amt
import kotlinx.android.synthetic.main.row_ord_dtls_report.view.tv_ord_dtls_total_qty

class AdapterDtOrdRept(var mContext: Context, var mList:ArrayList<DateWiseOrdReportFrag.OrdReportDateRoot>):
    RecyclerView.Adapter<AdapterDtOrdRept.DateWiseOrdReportViewHolder>(){

        private lateinit var adapterDtOrdReptDtls:AdapterDtOrdReptDtls

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateWiseOrdReportViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.row_ord_dtls_report,parent,false)
        return DateWiseOrdReportViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: DateWiseOrdReportViewHolder, position: Int) {
        holder.bindItems()
    }

    inner class DateWiseOrdReportViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        fun bindItems(){
            itemView.apply {
                tv_ord_dtls_date.text = "Date : "+ AppUtils.getFormatedDateNew(mList.get(adapterPosition).ordDate,"yyyy-mm-dd","dd-mm-yyyy")

                adapterDtOrdReptDtls=AdapterDtOrdReptDtls(mContext,mList.get(adapterPosition).ordDtlsL)

                var totalQty =""
                var totalAmt =""
                try {
                    totalQty = mList.get(adapterPosition).ordDtlsL.map { it.orderQtyTotal }.sumOf { it.toInt() }.toString()
                    totalAmt = mList.get(adapterPosition).ordDtlsL.map { it.orderValueTotal }.sumOf { it.toBigDecimal() }.toString()
                }catch (ex:Exception){
                    ex.printStackTrace()
                }

                rv_ord_dtls_list_dtls.adapter = adapterDtOrdReptDtls
                tv_ord_dtls_total_qty.text = totalQty
                tv_ord_dtls_total_amt.text = totalAmt
            }
        }
    }

}