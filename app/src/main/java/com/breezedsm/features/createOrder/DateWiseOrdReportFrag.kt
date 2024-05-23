package com.breezedsm.features.createOrder

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.breezedsm.R
import com.breezedsm.app.AppDatabase
import com.breezedsm.app.utils.AppUtils
import com.breezedsm.app.utils.ToasterMiddle
import com.breezedsm.app.widgets.MovableFloatingActionButton
import com.breezedsm.base.presentation.BaseFragment
import com.breezedsm.features.dashboard.presentation.DashboardActivity
import com.itextpdf.text.BadElementException
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Chunk
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Image
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.html.WebColors
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigDecimal
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
    private lateinit var llFooterRoot: LinearLayout

    private lateinit var adapterDtOrdRept: AdapterDtOrdRept
    private lateinit var rvDtls: RecyclerView

    private lateinit var ll_no_data_root: LinearLayout
    private lateinit var tv_empty_page_msg_head:TextView
    private lateinit var tv_empty_page_msg:TextView
    private lateinit var img_direction: ImageView
    private lateinit var fab_frag_ord_report_share: MovableFloatingActionButton

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
        fab_frag_ord_report_share = view.findViewById(R.id.fab_frag_ord_report_share)
        llFooterRoot = view.findViewById(R.id.ll_frag_ord_footer_root)

        llFooterRoot.visibility = View.GONE

        cvFromDate.setOnClickListener(this)
        cvToDate.setOnClickListener(this)
        cvDateSubmit.setOnClickListener(this)
        fab_frag_ord_report_share.setOnClickListener(this)

        tv_empty_page_msg.visibility = View.GONE
        img_direction.visibility = View.GONE

        fab_frag_ord_report_share.setCustomClickListener {
            dataProcessing()
        }

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



                datePickerDialog.datePicker.maxDate = Calendar.getInstance(Locale.ENGLISH).timeInMillis

                val mCalendar = Calendar.getInstance()
                val minDay = mDay-15
                val minMonth = mMonth
                val minYear = mYear
                mCalendar.set(minYear, minMonth, minDay)
                datePickerDialog.datePicker.minDate = mCalendar.timeInMillis

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

                val mCalendar = Calendar.getInstance()
                val minDay = mDay-15
                val minMonth = mMonth
                val minYear = mYear
                mCalendar.set(minYear, minMonth, minDay)
                datePickerDialog.datePicker.minDate = mCalendar.timeInMillis

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
                            llFooterRoot.visibility = View.VISIBLE
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
                            llFooterRoot.visibility = View.GONE
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

    private fun dataProcessing() {

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
                        /*adapterDtOrdRept = AdapterDtOrdRept(mContext, finalL)
                        rvDtls.adapter = adapterDtOrdRept*/
                        if (finalL.size>0) {
                            sharePDF(finalL)
                        }
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

    private fun sharePDF(finalL: ArrayList<OrdReportDateRoot>) {
        var document: Document = Document()
        var fileName = "FTS"+ "_" + AppUtils.getCurrentDateTime().replace(" ","").replace(":","_")
        fileName = fileName.replace("/", "_")
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() +"/ORDERDETALIS/"

        var pathNew = ""

        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdirs()
        }


        try {
            try {
                PdfWriter.getInstance(document, FileOutputStream(path + fileName + ".pdf"))
            } catch (ex: Exception) {
                ex.printStackTrace()
                pathNew = mContext.filesDir.toString() + "/" + fileName + ".pdf"
                PdfWriter.getInstance(document, FileOutputStream(pathNew))
            }
            document.open()

            var font: Font = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)
            var fontD: Font = Font(Font.FontFamily.HELVETICA, 11f, Font.BOLD)
            var fontTH: Font = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)
            var font1: Font = Font(Font.FontFamily.HELVETICA, 8f, Font.NORMAL)
            var fontBoldU: Font = Font(Font.FontFamily.HELVETICA, 11f, Font.UNDERLINE or Font.BOLD)
            var fontBoldUColor: Font = Font(Font.FontFamily.HELVETICA, 12f, Font.UNDERLINE or Font.BOLD)
            var fontBoldUColorNew: Font = Font(Font.FontFamily.HELVETICA, 11f,  Font.BOLD)
            val myColorpan = WebColors.getRGBColor("#196f84")
            fontBoldUColor.setColor(myColorpan)
            fontBoldUColorNew.setColor(myColorpan)

            val space10f = Paragraph("", font)
            space10f.spacingAfter = 10f
            val space20f = Paragraph("", font)
            space20f.spacingAfter = 20f
            document.add(space20f)

            //image add begin
            val bm: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.breezelogo)
            val bitmap = Bitmap.createScaledBitmap(bm, 80, 80, true);
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            var img: Image? = null
            val byteArray: ByteArray = stream.toByteArray()

            try {
                img = Image.getInstance(byteArray)
                img.scaleToFit(110f, 110f)
                img.scalePercent(70f)
                img.alignment = Image.ALIGN_LEFT
            } catch (e: BadElementException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            //image add end
            val heading = Paragraph("Date Wise Order Register", fontBoldUColor)
            heading.alignment = Element.ALIGN_CENTER
            val pHead = Paragraph()
            pHead.add(Chunk(img, 0f, -30f))
            document.add(pHead)
            document.add(heading)

            val dateRange = Paragraph("Date Range : " +AppUtils.getFormatedDateNew(str_selectedFromDate,"yyyy-mm-dd","dd-mm-yyyy") +" To "+AppUtils.getFormatedDateNew(str_selectedToDate,"yyyy-mm-dd","dd-mm-yyyy") , font)
            dateRange.alignment = Element.ALIGN_CENTER
            dateRange.spacingAfter = 2f
            dateRange.spacingBefore = 10f
            document.add(dateRange)

            document.add(space10f)

            val space30f = Paragraph("", font)
            space30f.spacingAfter = 10f
            document.add(space30f)

            for (i in 0..finalL.size - 1) {
                var orderObj = finalL.get(i)

                val space30f = Paragraph("", font)
                space30f.spacingAfter = 5f
                document.add(space30f)

                val ordDate = Paragraph("Date : " + AppUtils.getFormatedDateNew(orderObj?.ordDate,"yyyy-mm-dd","dd-mm-yyyy"), fontBoldUColorNew)
                ordDate.alignment = Element.ALIGN_LEFT
                ordDate.spacingAfter = 2f
                ordDate.spacingBefore = 10f
                document.add(ordDate)

                val space20f = Paragraph("", font)
                space20f.spacingAfter = 5f
                document.add(space20f)

                //product table
                var widths = floatArrayOf(0.06f, 0.30f, 0.10f, 0.20f)

                var tableHeader: PdfPTable = PdfPTable(widths)
                tableHeader.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT)
                tableHeader.setWidthPercentage(100f)

                val slNo = PdfPCell(Phrase("SL.", fontTH))
                slNo.setHorizontalAlignment(Element.ALIGN_LEFT)
                slNo.borderColor = BaseColor.LIGHT_GRAY
                tableHeader.addCell(slNo)

                val itemDesc = PdfPCell(Phrase("Outlet name", fontTH))
                itemDesc.setHorizontalAlignment(Element.ALIGN_LEFT)
                itemDesc.borderColor = BaseColor.LIGHT_GRAY
                tableHeader.addCell(itemDesc);

                val itemUOM = PdfPCell(Phrase("Order Qty", fontTH))
                itemUOM.setHorizontalAlignment(Element.ALIGN_RIGHT);
                itemUOM.borderColor = BaseColor.LIGHT_GRAY
                tableHeader.addCell(itemUOM)

                val itemRate = PdfPCell(Phrase("Order Value", fontTH))
                itemRate.setHorizontalAlignment(Element.ALIGN_RIGHT);
                itemRate.borderColor = BaseColor.LIGHT_GRAY
                tableHeader.addCell(itemRate)

                document.add(tableHeader)

                var totalQty = 0
                var totalAmnt : BigDecimal = BigDecimal.ZERO

                for (j in 0..orderObj.ordDtlsL.size - 1) {

                    try {
                        totalQty = totalQty+orderObj.ordDtlsL.get(j).orderQtyTotal.toInt()
                        totalAmnt = totalAmnt + orderObj.ordDtlsL.get(j).orderValueTotal.toBigDecimal()
                        println("tag_amount $totalAmnt $totalQty")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        println("tag_amount ${e.message}")
                    }
                    var sLNo: String = ""
                    var item: String = ""
                    var qty: String = ""
                    var rate: String = ""
                    var total: String = ""

                    sLNo = (j + 1).toString() + " "
                    item = orderObj.ordDtlsL!!.get(j).shop_name + "       "
                    qty = orderObj.ordDtlsL!!.get(j).orderQtyTotal + " "
                    rate = orderObj.ordDtlsL!!.get(j).orderValueTotal.toString() + " "
                    // total =  String.format("%.02f",(orderObj.ordDtlsL!!.get(i)..toDouble() * ordProductL!!.get(i).submitedSpecialRate.toDouble()))

                    val tableRows = PdfPTable(widths)
                    tableRows.defaultCell.horizontalAlignment = Element.ALIGN_CENTER
                    tableRows.setWidthPercentage(100f)

                    var cell1 = PdfPCell(Phrase(sLNo, font1))
                    cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell1.borderColor = BaseColor.LIGHT_GRAY
                    tableRows.addCell(cell1)

                    var cell2 = PdfPCell(Phrase(item, font1))
                    cell2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell2.borderColor = BaseColor.LIGHT_GRAY
                    tableRows.addCell(cell2)

                    var cell3 = PdfPCell(Phrase(qty, font1))
                    cell3.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cell3.borderColor = BaseColor.LIGHT_GRAY
                    tableRows.addCell(cell3)

                    var cell4 = PdfPCell(Phrase(rate, font1))
                    cell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cell4.borderColor = BaseColor.LIGHT_GRAY
                    tableRows.addCell(cell4)

                    /* var cell5 = PdfPCell(Phrase(total, font1))
                    cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell5.borderColor = BaseColor.GRAY
                    tableRows.addCell(cell5)*/

                    document.add(tableRows)

                    document.add(Paragraph())

                    /*val space25f = Paragraph("", font)
                    space25f.spacingAfter = 5f
                    document.add(space25f)*/
                }
                //test code begin

               /* val space10f = Paragraph("", font)
                space10f.spacingAfter = 10f
                document.add(space10f)*/


                val tableRows = PdfPTable(widths)
                tableRows.defaultCell.horizontalAlignment = Element.ALIGN_CENTER
                tableRows.setWidthPercentage(100f)

                var cell1 = PdfPCell(Phrase("", font))
                cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell1.borderColor = BaseColor.WHITE
                tableRows.addCell(cell1)

                var cell2 = PdfPCell(Phrase("Day wise total  : ", font))
                cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell2.borderColor = BaseColor.WHITE
                tableRows.addCell(cell2)

                var cell3 = PdfPCell(Phrase(totalQty.toString(), font))
                cell3.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell3.borderColor = BaseColor.WHITE
                tableRows.addCell(cell3)


                var cell4 = PdfPCell(Phrase(String.format("%.02f",totalAmnt), font))
                cell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell4.borderColor = BaseColor.WHITE
                tableRows.addCell(cell4)

                document.add(tableRows)

                document.add(Paragraph())

                //test code end


                /*val totalOrdAmt = Paragraph("Day wise total               :                                       " + totalQty.toString()+"                                     "+ String.format("%.02f",totalAmnt), font)
                totalOrdAmt.alignment = Element.ALIGN_RIGHT
                document.add(totalOrdAmt)*/

                document.add(space20f)

            }

            document.close()

            var sendingPath = path + fileName + ".pdf"
            if(!pathNew.equals("")){
                sendingPath = pathNew
            }
            try{
                val shareIntent = Intent(Intent.ACTION_SEND)
                val fileUrl = Uri.parse(sendingPath)
                val file = File(fileUrl.path)
                val uri: Uri = FileProvider.getUriForFile(mContext, mContext.applicationContext.packageName.toString() + ".provider", file)
                shareIntent.type = "image/png"
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                startActivity(Intent.createChooser(shareIntent, "Share pdf using"))
            }catch (ex:Exception){
                ex.printStackTrace()
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
            }

        }
        catch (ex:Exception){
            ex.printStackTrace()
            println("printshare"+ex.message)
        }
    }
}