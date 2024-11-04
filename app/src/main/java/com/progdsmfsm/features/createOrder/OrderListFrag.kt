package com.progdsmfsm.features.createOrder

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.text.Html
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.progdsmfsm.R
import com.progdsmfsm.app.AppDatabase
import com.progdsmfsm.app.NetworkConstant
import com.progdsmfsm.app.Pref
import com.progdsmfsm.app.domain.AddShopDBModelEntity
import com.progdsmfsm.app.domain.NewOrderDataEntity
import com.progdsmfsm.app.types.FragType
import com.progdsmfsm.app.utils.AppUtils
import com.progdsmfsm.app.utils.ToasterMiddle
import com.progdsmfsm.base.BaseResponse
import com.progdsmfsm.base.presentation.BaseActivity
import com.progdsmfsm.base.presentation.BaseFragment
import com.progdsmfsm.features.dashboard.presentation.DashboardActivity
import com.progdsmfsm.features.login.api.productlistapi.ProductListRepoProvider
import com.progdsmfsm.features.stockCompetetorStock.CompetetorStockFragment
import com.progdsmfsm.widgets.AppCustomTextView
import com.google.android.gms.vision.text.Line
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pnikosis.materialishprogress.ProgressWheel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_dashboard_new.tv_shop
import kotlinx.android.synthetic.main.inflate_member_pjp.view.tv_location
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.math.BigDecimal
import java.math.BigInteger

class OrderListFrag : BaseFragment(), View.OnClickListener {
    private lateinit var mContext: Context
    lateinit var shopObj: AddShopDBModelEntity

    private lateinit var tv_shopNameIni: TextView
    private lateinit var tv_shopName: TextView
    private lateinit var tv_addr: TextView
    private lateinit var tv_contactNo: TextView
    private lateinit var tv_totalOrdAmt: TextView

    private lateinit var ll_no_data_root: LinearLayout
    private lateinit var tv_noDataHeader: TextView
    private lateinit var tv_noDataBody: TextView

    private lateinit var rv_ordList: RecyclerView
    private lateinit var fab_addProduct: FloatingActionButton

    private lateinit var adapterOrderList: AdapterOrderList

    private lateinit var progress_wheel: ProgressWheel

    private var onDeleteProcessing = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    companion object {
        var shop_id: String = ""
        fun getInstance(objects: Any): OrderListFrag {
            val orderListFrag = OrderListFrag()
            if (!TextUtils.isEmpty(objects.toString())) {
                shop_id = objects.toString()
            }
            return orderListFrag
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.frag_order_list, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        shopObj = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(shop_id)

        tv_shopNameIni = view.findViewById(R.id.tv_frag_ord_list_shop_name_initial)
        tv_shopName = view.findViewById(R.id.tv_frag_ord_list_shop_name)
        tv_addr = view.findViewById(R.id.tv_frag_ord_list_addr)
        tv_contactNo = view.findViewById(R.id.tv_frag_ord_list_contact_no)
        tv_totalOrdAmt = view.findViewById(R.id.tv_frag_ord_list_total_ord_amt)

        ll_no_data_root = view.findViewById(R.id.ll_no_data_root)
        tv_noDataHeader = view.findViewById(R.id.tv_empty_page_msg_head)
        tv_noDataBody = view.findViewById(R.id.tv_empty_page_msg)
        rv_ordList = view.findViewById(R.id.rv_frag_ord_list_dtls)
        fab_addProduct = view.findViewById(R.id.fab_frag_ord_add_product)
        progress_wheel = view.findViewById(R.id.pw_frag_ord_list)

        progress_wheel.stopSpinning()

        tv_shopNameIni.text = shopObj.shopName!!.trim().get(0).toString()
        tv_shopName.text = shopObj.shopName
        tv_addr.text = shopObj.address
        val text =
            "<font color=" + mContext.resources.getColor(R.color.dark_gray) + ">Contact Number: </font> <font color=" +
                    mContext.resources.getColor(R.color.black) + ">" + shopObj.ownerContactNumber + "</font>"
        tv_contactNo.text = Html.fromHtml(text)



        fab_addProduct.setOnClickListener({
            if (Pref.isAddAttendence == false)
                (mContext as DashboardActivity).checkToShowAddAttendanceAlert()
            else if (Pref.IsShowDayStart) {
                if (Pref.DayStartMarked) {
                    (mContext as DashboardActivity).loadFragment(
                        FragType.ProductListFrag,
                        true,
                        shopObj.shop_id
                    )
                } else {
                    ToasterMiddle.msgShort(mContext, "Please start your day.")
                }
            } else {
                (mContext as DashboardActivity).loadFragment(
                    FragType.ProductListFrag,
                    true,
                    shopObj.shop_id
                )
            }
        })

        setData()
    }


    fun setData() {

        var ordAmt = AppDatabase.getDBInstance()!!.newOrderDataDao().getOrderAmtByShop(shop_id).toBigDecimal().toString()
        val text1 =
            "<font color=" + mContext.resources.getColor(R.color.dark_gray) + ">Total Order Amt: </font> <font color=" +
                    mContext.resources.getColor(R.color.black) + ">" + "₹ ${
                String.format(
                    "%.02f",
                    ordAmt.toBigDecimal()
                )
            }" + "</font>"
        tv_totalOrdAmt.text = Html.fromHtml(text1)

        var ordL = AppDatabase.getDBInstance()!!.newOrderDataDao().getOrderByShop(shop_id) as ArrayList<NewOrderDataEntity>
        if (ordL.size > 0) {
            ll_no_data_root.visibility = View.GONE
            rv_ordList.visibility = View.VISIBLE
            adapterOrderList =
                AdapterOrderList(mContext, ordL, object : AdapterOrderList.OnActionClick {
                    override fun onViewClick(obj: NewOrderDataEntity) {

                        progress_wheel.spin()
                        (mContext as DashboardActivity).loadFragment(FragType.ViewOrdDtls, true, obj.order_id)
                        Handler().postDelayed(Runnable {
                            progress_wheel.stopSpinning()
                        }, 500)

                    }

                    override fun onSyncClick(obj: NewOrderDataEntity) {
                        if (AppUtils.isOnline(mContext)) {
                            syncOrd(obj.order_id)
                        } else {
                            ToasterMiddle.msgShort(
                                mContext,
                                mContext.getString(R.string.login_net_disconnected1)
                            )
                        }

                    }

                    override fun onEditClick(obj: NewOrderDataEntity) {
                       /* if (Pref.isAddAttendence == false) {
                            (mContext as DashboardActivity).checkToShowAddAttendanceAlert()
                        } else {
                            (mContext as DashboardActivity).loadFragment(
                                FragType.CartEditListFrag,
                                true,
                                obj.order_id
                            )
                        }*/

                        if (Pref.isAddAttendence == false)
                            (mContext as DashboardActivity).checkToShowAddAttendanceAlert()
                        else if (Pref.IsShowDayStart) {
                            if (Pref.DayStartMarked) {
                                (mContext as DashboardActivity).loadFragment(
                                    FragType.CartEditListFrag,
                                    true,
                                    obj.order_id
                                )
                            } else {
                                ToasterMiddle.msgShort(mContext, "Please start your day.")
                            }
                        } else {
                            (mContext as DashboardActivity).loadFragment(
                                FragType.CartEditListFrag,
                                true,
                                obj.order_id
                            )
                        }
                    }

                    override fun onDelClick(obj: NewOrderDataEntity) {
                        if(onDeleteProcessing == false){
                            onDeleteProcessing = true

                            if (Pref.isAddAttendence == false) {
                                onDeleteProcessing = false
                                (mContext as DashboardActivity).checkToShowAddAttendanceAlert()
                            }else if (Pref.IsShowDayStart) {
                                if (Pref.DayStartMarked) {
                                    deleteOrdDialog(obj)
                                } else {
                                    onDeleteProcessing = false
                                    ToasterMiddle.msgShort(mContext, "Please start your day.")
                                }
                            } else {
                                deleteOrdDialog(obj)
                            }
                        }
                    }
                })
            rv_ordList.adapter = adapterOrderList
        } else {
            ll_no_data_root.visibility = View.VISIBLE
            rv_ordList.visibility = View.GONE
            tv_noDataHeader.text = "No order found"
            tv_noDataBody.text = "Click + to add order"
        }
    }

    fun deleteOrdDialog(obj: NewOrderDataEntity){
        val simpleDialog = Dialog(mContext)
        simpleDialog.setCancelable(false)
        simpleDialog.getWindow()!!
            .setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        simpleDialog.setContentView(R.layout.dialog_yes_no)
        val tv_header =
            simpleDialog.findViewById(R.id.dialog_yes_no_headerTV) as AppCustomTextView
        val tv_body =
            simpleDialog.findViewById(R.id.dialog_cancel_order_header_TV) as AppCustomTextView
        val dialogYes =
            simpleDialog.findViewById(R.id.tv_dialog_yes_no_yes) as AppCustomTextView
        val dialogNo =
            simpleDialog.findViewById(R.id.tv_dialog_yes_no_no) as AppCustomTextView

        tv_header.text = AppUtils.hiFirstNameText()
        tv_body.text = "Want to delete order ${obj.order_id} ?"
        dialogNo.setOnClickListener {
            simpleDialog.dismiss()
            onDeleteProcessing = false
        }
        dialogYes.setOnClickListener {
            simpleDialog.dismiss()
            deleteOrd(obj)
        }

        simpleDialog.show()
    }

    override fun onClick(p0: View?) {
        TODO("Not yet implemented")
    }

    private fun syncOrd(ordId: String) {
        progress_wheel.spin()
        var ordDtls = AppDatabase.getDBInstance()!!.newOrderDataDao().getOrderByID(ordId)
        var ordProductDtls =
            AppDatabase.getDBInstance()!!.newOrderProductDao().getProductsOrder(ordId)
        var syncOrd = SyncOrd()
        var syncOrdProductL: ArrayList<SyncOrdProductL> = ArrayList()

        doAsync {
            syncOrd.user_id = Pref.user_id!!
            syncOrd.order_id = ordId
            syncOrd.order_date = ordDtls.order_date
            syncOrd.order_time = ordDtls.order_time
            syncOrd.order_date_time = ordDtls.order_date_time
            syncOrd.shop_id = ordDtls.shop_id
            syncOrd.shop_name = ordDtls.shop_name
            syncOrd.shop_type = ordDtls.shop_type
            syncOrd.isInrange = ordDtls.isInrange
            syncOrd.order_lat = ordDtls.order_lat
            syncOrd.order_long = ordDtls.order_long
            syncOrd.shop_addr = ordDtls.shop_addr
            syncOrd.shop_pincode = ordDtls.shop_pincode
            syncOrd.order_total_amt = ordDtls.order_total_amt.toDouble()
            syncOrd.order_remarks = ordDtls.order_remarks

            for (i in 0..ordProductDtls.size - 1) {
                var obj = SyncOrdProductL()
                obj.order_id = ordProductDtls.get(i).order_id
                obj.product_id = ordProductDtls.get(i).product_id
                obj.product_name = ordProductDtls.get(i).product_name
                obj.submitedQty = ordProductDtls.get(i).submitedQty.toDouble()
                obj.submitedSpecialRate = ordProductDtls.get(i).submitedSpecialRate.toDouble()

                obj.total_amt=ordProductDtls.get(i).total_amt.toString().toDouble()
                obj.mrp=ordProductDtls.get(i).mrp.toString().toDouble()
                obj.itemPrice=ordProductDtls.get(i).itemPrice.toString().toDouble()

                syncOrdProductL.add(obj)
            }
            syncOrd.product_list = syncOrdProductL

            uiThread {
                val repository = ProductListRepoProvider.productListProvider()
                BaseActivity.compositeDisposable.add(
                    repository.syncProductListITC(syncOrd)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as BaseResponse
                            if (response.status == NetworkConstant.SUCCESS) {
                                doAsync {
                                    AppDatabase.getDBInstance()!!.newOrderDataDao()
                                        .updateIsUploaded(syncOrd.order_id, true)
                                    uiThread {
                                        progress_wheel.stopSpinning()
                                        ToasterMiddle.msgShort(
                                            mContext,
                                            mContext.getString(R.string.sync_done)
                                        )
                                        setData()
                                    }
                                }
                            } else {
                                progress_wheel.stopSpinning()
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                            }
                        }, { error ->
                            progress_wheel.stopSpinning()
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                        })
                )
            }
        }

    }

    fun deleteOrd(ordObj: NewOrderDataEntity) {
        Handler().postDelayed(Runnable {
            onDeleteProcessing = false
        }, 1200)
        if (ordObj.isUploaded) {
            AppDatabase.getDBInstance()!!.newOrderDataDao().updateOrdDelete(ordObj.order_id, true)
            if(AppUtils.isOnline(mContext)){
                deleteOrdApiCall()
            }else{
                msgShow("Order successfully deleted.")
            }
        } else {
            AppDatabase.getDBInstance()!!.newOrderDataDao().deleteOrderHeader(ordObj.order_id)
            AppDatabase.getDBInstance()!!.newOrderProductDao().deleteProductByOrdID(ordObj.order_id)
            msgShow("Order successfully deleted.")
        }
    }

    fun deleteOrdApiCall() {
        progress_wheel.spin()
        var deleteL = AppDatabase.getDBInstance()!!.newOrderDataDao().getDeleteL(true) as ArrayList<NewOrderDataEntity>
        if (deleteL.size > 0) {
            var deleteOrd: DeleteOrd = DeleteOrd()
            deleteOrd.user_id = Pref.user_id.toString()
            deleteOrd.session_token = Pref.session_token.toString()
            for (i in 0..deleteL.size - 1) {
                deleteOrd.order_delete_list.add(OrdID(deleteL.get(i).order_id))
            }

            val repository = ProductListRepoProvider.productListProvider()
            BaseActivity.compositeDisposable.add(
                repository.deleteOrderITC(deleteOrd)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val response = result as BaseResponse
                        if (response.status == NetworkConstant.SUCCESS) {
                            doAsync {
                                var ordIDL = deleteOrd.order_delete_list.map { it.order_id }
                                for(i in 0..ordIDL.size-1){
                                    AppDatabase.getDBInstance()!!.newOrderDataDao().deleteOrderHeader(ordIDL.get(i))
                                    AppDatabase.getDBInstance()!!.newOrderProductDao().deleteProductByOrdID(ordIDL.get(i))
                                }
                                uiThread {
                                    progress_wheel.stopSpinning()
                                    msgShow("Order successfully deleted.")
                                }
                            }
                        } else {
                            progress_wheel.stopSpinning()
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                        }
                    }, { error ->
                        progress_wheel.stopSpinning()
                        (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                    })
            )
        }else{
            progress_wheel.stopSpinning()
        }
    }

    fun msgShow(msg:String){
        val simpleDialog = Dialog(mContext)
        simpleDialog.setCancelable(false)
        simpleDialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        simpleDialog.setContentView(R.layout.dialog_message)
        val headerTv = simpleDialog.findViewById(R.id.dialog_message_headerTV) as AppCustomTextView
        val bodyTv = simpleDialog.findViewById(R.id.dialog_message_header_TV) as AppCustomTextView
        val okTV = simpleDialog.findViewById(R.id.tv_message_ok) as AppCustomTextView
        headerTv.text = "Congrats!"
        bodyTv.text = msg
        okTV.setOnClickListener({ view ->
            simpleDialog.cancel()
            setData()
        })
        simpleDialog.show()
        voiceOrderMsg(msg)
    }

    private fun voiceOrderMsg(msg:String) {
        if (Pref.isVoiceEnabledForOrderSaved) {
            val speechStatus = (mContext as DashboardActivity).textToSpeech.speak(
                msg,
                TextToSpeech.QUEUE_FLUSH,
                null
            )
            if (speechStatus == TextToSpeech.ERROR)
                Log.e("Add Order", "TTS error in converting Text to Speech!")
        }
    }

    fun updateData(){
        setData()
    }

}