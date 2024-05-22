package com.breezedsm.features.createOrder

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.breezedsm.R
import com.breezedsm.app.AppDatabase
import com.breezedsm.app.domain.NewOrderDataEntity
import com.breezedsm.app.utils.AppUtils
import com.breezedsm.app.utils.FTStorageUtils
import com.breezedsm.app.utils.ToasterMiddle
import com.breezedsm.base.presentation.BaseFragment
import com.breezedsm.features.dashboard.presentation.DashboardActivity
import com.github.jhonnyx2012.horizontalpicker.DatePickerListener
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.joda.time.DateTime
import timber.log.Timber
import java.util.Calendar
import java.util.Locale

class DateWiseOrdReportFrag : BaseFragment(), View.OnClickListener {
    private lateinit var mContext: Context

    private lateinit var cvFromDate: CardView
    private lateinit var cvToDate: CardView
    private lateinit var cvDateSubmit: CardView
    private lateinit var tvFromDate: TextView
    private lateinit var tvToDate: TextView
    private lateinit var tvTotalQty: TextView
    private lateinit var tvTotalValue: TextView

    private lateinit var adapterDtOrdRept: AdapterDtOrdRept
    private lateinit var rvDtls: RecyclerView

    private lateinit var ll_no_data_root: LinearLayout
    private lateinit var tv_empty_page_msg_head:TextView
    private lateinit var tv_empty_page_msg:TextView
    private lateinit var img_direction: ImageView

    var str_selectedFromDate = ""
    var str_selectedToDate = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.frag_ord_reg_report, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View?) {
        cvFromDate = view!!.findViewById(R.id.cv_frag_ord_report_from_date)
        cvToDate = view!!.findViewById(R.id.cv_frag_ord_report_to_date)
        cvDateSubmit = view!!.findViewById(R.id.cv_frag_ord_report_date_submit)
        tvFromDate = view!!.findViewById(R.id.tv_frag_ord_report_from_date)
        tvToDate = view!!.findViewById(R.id.tv_frag_ord_report_to_date)
        rvDtls = view!!.findViewById(R.id.rv_frag_ord_report_dtls)
        tvTotalQty = view!!.findViewById(R.id.tv_frag_ord_reg_report_total_qty)
        tvTotalValue = view!!.findViewById(R.id.tv_frag_ord_reg_report_total_value)

        ll_no_data_root = view.findViewById(R.id.ll_no_data_root)
        tv_empty_page_msg_head = view.findViewById(R.id.tv_empty_page_msg_head)
        tv_empty_page_msg = view.findViewById(R.id.tv_empty_page_msg)
        img_direction = view.findViewById(R.id.img_direction)

        cvFromDate.setOnClickListener(this)
        cvToDate.setOnClickListener(this)
        cvDateSubmit.setOnClickListener(this)

        tv_empty_page_msg.visibility = View.GONE
        img_direction.visibility = View.GONE

    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            cvFromDate.id -> {
                val cFromDate = Calendar.getInstance(Locale.ENGLISH)
                var mYear: Int = cFromDate.get(Calendar.YEAR)
                var mMonth: Int = cFromDate.get(Calendar.MONTH)
                var mDay: Int = cFromDate.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(mContext,
                    object : DatePickerDialog.OnDateSetListener {
                        override fun onDateSet(
                            p0: DatePicker?,
                            year: Int,
                            monthOfYear: Int,
                            dayOfMonth: Int
                        ) {

                            var sel_day = String.format("%02d", dayOfMonth)
                            var sel_Month = String.format("%02d", monthOfYear + 1)
                            var sel_Year = year
                            str_selectedFromDate = "$sel_Year-$sel_Month-$sel_day"
                            tvFromDate.text = "$sel_day-$sel_Month-$sel_Year"
                        }
                    }, mYear, mMonth, mDay
                )
                datePickerDialog.datePicker.maxDate =
                    Calendar.getInstance(Locale.ENGLISH).timeInMillis
                datePickerDialog.show()
            }

            cvToDate.id -> {
                if (str_selectedFromDate.equals("")) {
                    ToasterMiddle.msgLong(mContext, "Please select From Date.")
                    return
                }
                val cFromDate = Calendar.getInstance(Locale.ENGLISH)
                var mYear: Int = cFromDate.get(Calendar.YEAR)
                var mMonth: Int = cFromDate.get(Calendar.MONTH)
                var mDay: Int = cFromDate.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(mContext,
                    object : DatePickerDialog.OnDateSetListener {
                        override fun onDateSet(
                            p0: DatePicker?,
                            year: Int,
                            monthOfYear: Int,
                            dayOfMonth: Int
                        ) {

                            var sel_day = String.format("%02d", dayOfMonth)
                            var sel_Month = String.format("%02d", monthOfYear + 1)
                            var sel_Year = year
                            str_selectedToDate = "$sel_Year-$sel_Month-$sel_day"


                            if (AppUtils.getIsEndDayAfterStartDay(
                                    str_selectedFromDate,
                                    str_selectedToDate
                                )
                            ) {
                                tvToDate.text = "$sel_day-$sel_Month-$sel_Year"
                            } else {
                                ToasterMiddle.msgLong(mContext, "Your From Date is before To Date.")
                                str_selectedToDate = ""
                                tvToDate.text = "To Date"
                            }
                        }
                    }, mYear, mMonth, mDay
                )
                datePickerDialog.datePicker.maxDate =
                    Calendar.getInstance(Locale.ENGLISH).timeInMillis
                datePickerDialog.show()
            }

            cvDateSubmit.id -> {
                /*try {
                    if(!str_selectedFromDate.equals("") && !str_selectedToDate.equals("")){
                        var ordDateL = AppDatabase.getDBInstance()!!.newOrderDataDao().getDistinctOrdDates(str_selectedFromDate,str_selectedToDate) as ArrayList<String>
                        if(ordDateL.size>0){
                            rvDtls.visibility = View.VISIBLE

                            doAsync {
                                var ordReportDateRootL:ArrayList<OrdReportDateRoot> = ArrayList()
                                for(i in ordDateL){
                                    var ordReportDateRoot :OrdReportDateRoot = OrdReportDateRoot()
                                    ordReportDateRoot.ordDate = i

                                    var ordDtlsByDate = AppDatabase.getDBInstance()!!.newOrderDataDao().getOrderDtlsDateWise(i,i) as ArrayList<NewOrderDataEntity>
                                    for(j in 0..ordDtlsByDate.size-1){
                                        var objDtls :OrdReportDtls = OrdReportDtls()
                                        objDtls.shop_id = ordDtlsByDate.get(j).shop_id
                                        objDtls.shop_name = ordDtlsByDate.get(j).shop_name
                                        objDtls.orderQtyTotal = AppDatabase.getDBInstance()!!.newOrderDataDao().getQtySumByOrdID(ordDtlsByDate.get(j).order_id)
                                        objDtls.orderValueTotal = ordDtlsByDate.get(j).order_total_amt
                                        ordReportDateRoot.ordDtlsL.add(objDtls)
                                    }
                                    ordReportDateRootL.add(ordReportDateRoot)
                                }

                                var finalL:ArrayList<OrdReportDateRoot> = ArrayList()
                                for(i in 0..ordReportDateRootL.size-1){
                                    var rootObj = OrdReportDateRoot()
                                    var dtlsObjL :ArrayList<OrdReportDtls> = ArrayList()
                                    rootObj.ordDate = ordReportDateRootL.get(i).ordDate

                                    var dtlsL = ordReportDateRootL.get(i).ordDtlsL.groupBy { it.shop_id }
                                    for(j in dtlsL){
                                        var ordObj = OrdReportDtls()
                                        ordObj.shop_id = j.value.get(0).shop_id
                                        ordObj.shop_name = j.value.get(0).shop_name
                                        var qty = j.value.sumOf { it.orderQtyTotal.toInt() }
                                        var amt = String.format("%.02f",j.value.sumOf { it.orderValueTotal.toBigDecimal() })
                                        ordObj.orderQtyTotal = qty.toString()
                                        ordObj.orderValueTotal = amt.toString()
                                        dtlsObjL.add(ordObj)
                                    }
                                    rootObj.ordDtlsL = dtlsObjL
                                    finalL.add(rootObj)
                                }
                                uiThread {
                                    if(finalL.size>0){
                                        adapterDtOrdRept = AdapterDtOrdRept(mContext,finalL)
                                        rvDtls.adapter=adapterDtOrdRept
                                    }
                                }
                            }
                        }else{
                            rvDtls.visibility = View.GONE
                            ToasterMiddle.msgLong(mContext,"No data found.")
                        }
                    }else{
                        ToasterMiddle.msgLong(mContext,"Please select dates.")
                    }
                } catch (e: Exception) {
                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                    Timber.d("order view error ${e.printStackTrace()}")
                }*/

                try {
                    if (!str_selectedFromDate.equals("") && !str_selectedToDate.equals("")) {
                        var finalL: ArrayList<OrdReportDateRoot> = ArrayList()
                        var ordDtlsQueryL = AppDatabase.getDBInstance()!!.newOrderDataDao().getOrdReportByDt(str_selectedFromDate, str_selectedToDate) as ArrayList<OrdReportDtlsQuery>
                        if (ordDtlsQueryL.size > 0) {
                            ll_no_data_root.visibility = View.GONE
                            rvDtls.visibility = View.VISIBLE
                            tv_empty_page_msg_head.text = "No data found"
                            try {
                                var totalQty =
                                    ordDtlsQueryL.map { it.orderQtyTotal }.sumOf { it.toInt() }
                                        .toString()
                                var totalValue = ordDtlsQueryL.map { it.orderValueTotal }
                                    .sumOf { it.toBigDecimal() }.toString()
                                tvTotalQty.text = totalQty.toString()
                                tvTotalValue.text = totalValue.toString()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            var dateQueryL = ordDtlsQueryL.map { it.order_date }.distinct()
                            if (dateQueryL.size > 0) {
                                for (i in dateQueryL) {
                                    var ordDtlsRoot: OrdReportDateRoot = OrdReportDateRoot()
                                    ordDtlsRoot.ordDate = i
                                    ordDtlsRoot.ordDtlsL =
                                        AppDatabase.getDBInstance()!!.newOrderDataDao()
                                            .getOrdReportBySingleDt(ordDtlsRoot.ordDate) as ArrayList<OrdReportDtls>
                                    finalL.add(ordDtlsRoot)
                                }
                            }
                            if (finalL.size > 0) {
                                adapterDtOrdRept = AdapterDtOrdRept(mContext, finalL)
                                rvDtls.adapter = adapterDtOrdRept
                            }
                        }else{
                            ll_no_data_root.visibility = View.VISIBLE
                            rvDtls.visibility = View.GONE
                            tv_empty_page_msg_head.text = "No data found"
                        }
                    } else {
                        ToasterMiddle.msgLong(mContext, "Please select dates.")
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }


    data class OrdReportDateRoot(
        var ordDate: String = "",
        var ordDtlsL: ArrayList<OrdReportDtls> = ArrayList()
    )

    data class OrdReportDtls(
        var shop_id: String = "",
        var shop_name: String = "",
        var orderQtyTotal: String = "",
        var orderValueTotal: String = ""
    )

    data class OrdReportDtlsQuery(
        var order_date: String = "",
        var shop_id: String = "",
        var shop_name: String = "",
        var orderQtyTotal: String = "",
        var orderValueTotal: String = ""
    )


}