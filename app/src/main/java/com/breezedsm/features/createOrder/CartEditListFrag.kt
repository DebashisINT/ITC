package com.breezedsm.features.createOrder

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.breezedsm.R
import com.breezedsm.app.AppDatabase
import com.breezedsm.app.NetworkConstant
import com.breezedsm.app.Pref
import com.breezedsm.app.domain.AddShopDBModelEntity
import com.breezedsm.app.domain.NewOrderDataEntity
import com.breezedsm.app.domain.NewOrderProductEntity
import com.breezedsm.app.domain.NewProductListEntity
import com.breezedsm.app.types.FragType
import com.breezedsm.app.utils.AppUtils
import com.breezedsm.app.utils.FTStorageUtils
import com.breezedsm.app.utils.ToasterMiddle
import com.breezedsm.base.BaseResponse
import com.breezedsm.base.presentation.BaseActivity
import com.breezedsm.base.presentation.BaseFragment
import com.breezedsm.features.dashboard.presentation.DashboardActivity
import com.breezedsm.features.location.LocationWizard
import com.breezedsm.features.login.api.productlistapi.ProductListRepoProvider
import com.breezedsm.widgets.AppCustomTextView
import com.google.android.material.textfield.TextInputEditText
import com.pnikosis.materialishprogress.ProgressWheel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import timber.log.Timber

class CartEditListFrag: BaseFragment(), View.OnClickListener {
    private lateinit var mContext: Context
    private lateinit var rv_cartProductL: RecyclerView
    private lateinit var progress_wheel: ProgressWheel
    private lateinit var tv_totalQty: TextView
    private lateinit var tv_totalValue: TextView
    private lateinit var ll_placeOrder: LinearLayout
    private lateinit var tv_addNewProduct: TextView
    private lateinit var ordID: TextView

    private lateinit var adapterCartEditList: AdapterCartEditList


    private lateinit var addShopData : AddShopDBModelEntity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    companion object {
         var isCartChanges = false
         var ordDtls : NewOrderDataEntity = NewOrderDataEntity()
         var ordProductDtlsL : ArrayList<FinalProductRateSubmit> = ArrayList()

        var order_id: String = ""
        var iseditCommit:Boolean = true
        fun getInstance(objects: Any): CartEditListFrag {
            val fragment = CartEditListFrag()
            order_id = objects as String
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.frag_cart_edit_l, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View?) {
        rv_cartProductL = view!!.findViewById(R.id.rv_ord_pro_list_frag_cart_edit_list)
        progress_wheel = view!!.findViewById(R.id.pw_frag_ord_cart_edit_list)
        tv_totalQty = view!!.findViewById(R.id.tv_ord_prod_cart_edit_frag_total_item)
        tv_totalValue = view!!.findViewById(R.id.tv_ord_prod_cart_edit_frag_total_value)
        ll_placeOrder = view!!.findViewById(R.id.ll_ord_prod_cart_edit_frag_place_order)
        tv_addNewProduct = view!!.findViewById(R.id.tv_frag_cart_edit_add_new_product)
        ordID = view!!.findViewById(R.id.tv_frag_cart_edit_ord_id)

        ll_placeOrder.setOnClickListener(this)
        tv_addNewProduct.setOnClickListener(this)

        ordDtls = AppDatabase.getDBInstance()!!.newOrderDataDao().getOrderByID(order_id)
        addShopData = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopDetail(ordDtls.shop_id)
        ordProductDtlsL = ArrayList()
        ordProductDtlsL = AppDatabase.getDBInstance()!!.newOrderProductDao().getCustomOrdProductL(order_id) as ArrayList<FinalProductRateSubmit>
        ordID.text = "Order ID: \n$order_id"

        setData()
    }

    fun setData(){
        try {

            tv_totalQty.text= ordProductDtlsL.sumOf { it.submitedQty.toInt() }.toString()
            tv_totalValue.text= String.format("%.2f",ordProductDtlsL!!.sumByDouble { it.submitedQty.toInt() * it.submitedRate.toDouble() }).toString()


            adapterCartEditList = AdapterCartEditList(mContext,ordProductDtlsL,object :AdapterCartEditList.OnCartOptiOnClick{
                override fun onDelChangeClick(cartSize: Int) {
                    isCartChanges=true
                    tv_totalQty.text= ordProductDtlsL.sumOf { it.submitedQty.toInt() }.toString()
                    tv_totalValue.text= String.format("%.2f",ordProductDtlsL!!.sumByDouble { it.submitedQty.toInt() * it.submitedRate.toDouble() }).toString()
                }

                override fun onRateQtyChange() {
                    isCartChanges=true
                    tv_totalQty.text= ordProductDtlsL.sumOf { it.submitedQty.toInt() }.toString()
                    tv_totalValue.text= String.format("%.2f",ordProductDtlsL!!.sumByDouble { it.submitedQty.toInt() * it.submitedRate.toDouble() }).toString()
                }

            })

            rv_cartProductL.adapter = adapterCartEditList

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onClick(p0: View?) {
        when(p0!!.id){
            ll_placeOrder.id->{
                println("tag_click ll_placeOrder")
                ll_placeOrder.isEnabled = false

                if(iseditCommit){
                    if(ordProductDtlsL.size==0){
                        ll_placeOrder.isEnabled = true
                        ToasterMiddle.msgLong(mContext,"Please select product.")
                    }else{
                        //check product avaliable in product master
                        var productIdL = ordProductDtlsL.map { it.product_id } as ArrayList<String>
                        var proExistanceL = AppDatabase.getDBInstance()!!.newOrderProductDao().getProductExistance(productIdL) as ArrayList<NewProductListEntity>

                        if(productIdL.size != proExistanceL.size){
                            ll_placeOrder.isEnabled = true

                            val simpleDialog = Dialog(mContext)
                            simpleDialog.setCancelable(false)
                            simpleDialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                            simpleDialog.setContentView(R.layout.dialog_message)
                            val dialogHeader = simpleDialog.findViewById(R.id.dialog_message_headerTV) as AppCustomTextView
                            val body = simpleDialog.findViewById(R.id.dialog_message_header_TV) as AppCustomTextView
                            dialogHeader.text = AppUtils.hiFirstNameText()
                            body.text = "Product(s) are not available. Talk to Manager."
                            val dialogYes = simpleDialog.findViewById(R.id.tv_message_ok) as AppCustomTextView
                            dialogYes.setOnClickListener({ view ->
                                simpleDialog.cancel()
                            })
                            simpleDialog.show()
                        }else{
                            showCheckAlert("Order Confirmation", "Would you like to confirm the order edit?")
                        }
                    }
                }else{
                    ll_placeOrder.isEnabled = true
                    openDialog("Please click on tick to save this edit.")
                }
            }
            tv_addNewProduct.id ->{
                (mContext as DashboardActivity).loadFragment(FragType.ProductEditListFrag, true, ordDtls.order_id)
            }
        }
    }

    private fun showCheckAlert(header: String, title: String) {
        val simpleDialog = Dialog(mContext)
        simpleDialog.setCancelable(false)
        simpleDialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        simpleDialog.setContentView(R.layout.dialog_yes_no)
        val tvHeader = simpleDialog.findViewById(R.id.dialog_yes_no_headerTV) as AppCustomTextView
        val tvBody = simpleDialog.findViewById(R.id.dialog_cancel_order_header_TV) as AppCustomTextView
        val dialogYes = simpleDialog.findViewById(R.id.tv_dialog_yes_no_yes) as AppCustomTextView
        val dialogNo = simpleDialog.findViewById(R.id.tv_dialog_yes_no_no) as AppCustomTextView

        tvHeader.text = "Order Confirmation"
        tvBody.text="Would you like to confirm the order edit?"
        dialogYes.setOnClickListener {
            simpleDialog.cancel()
            if (!Pref.isShowOrderRemarks)
                editSaveOrder()
            else
                showRemarksAlert()
        }
        dialogNo.setOnClickListener {
            ll_placeOrder.isEnabled = true
            simpleDialog.cancel()
        }
        simpleDialog.show()
    }

    private fun editSaveOrder(remarks:String=""){
        progress_wheel.spin()
        Handler().postDelayed(Runnable {
            try {
                val orderListDetails = NewOrderDataEntity()

                orderListDetails.order_id = ordDtls.order_id
                orderListDetails.order_edit_date_time = AppUtils.getCurrentDateForShopActi()+" "+AppUtils.getCurrentTime()
                orderListDetails.order_total_amt = String.format("%.2f",ordProductDtlsL!!.sumByDouble { it.submitedQty.toInt() * it.submitedRate.toDouble() }.toBigDecimal()).toString()
                orderListDetails.order_edit_remarks = remarks
                orderListDetails.isEdited = true

                AppDatabase.getDBInstance()!!.newOrderDataDao().updateOrdEdit(orderListDetails.order_id,orderListDetails.order_edit_date_time,
                    orderListDetails.order_total_amt, orderListDetails.order_edit_remarks,orderListDetails.isEdited)
                doAsync {
                    var orderProductDtls : ArrayList<NewOrderProductEntity> = ArrayList()
                    for(i in 0..ordProductDtlsL.size-1){
                        var obj : NewOrderProductEntity = NewOrderProductEntity()
                        obj.order_id = orderListDetails.order_id
                        obj.product_id = ordProductDtlsL.get(i).product_id
                        obj.product_name = ordProductDtlsL.get(i).product_name
                        obj.submitedQty = ordProductDtlsL.get(i).submitedQty
                        obj.submitedSpecialRate = ordProductDtlsL.get(i).submitedRate
                        obj.shop_id = ordDtls.shop_id

                        obj.total_amt = ordProductDtlsL.get(i).total_amt
                        obj.mrp = ordProductDtlsL.get(i).mrp
                        obj.itemPrice = ordProductDtlsL.get(i).item_price

                        orderProductDtls.add(obj)
                    }
                    AppDatabase.getDBInstance()!!.newOrderProductDao().deleteProductByOrdID(orderListDetails.order_id)
                    AppDatabase.getDBInstance()!!.newOrderProductDao().insertAll(orderProductDtls)
                    uiThread {
                        if(AppUtils.isOnline(mContext)){
                            editOrderApi(orderListDetails.order_id,addShopData)
                        }else{
                            progress_wheel.stopSpinning()
                            msgShow("${AppUtils.hiFirstNameText()}. Your order for ${addShopData.shopName} has been saved successfully.Order No. is ${orderListDetails.order_id}")
                        }

                    }
                }
            }
            catch (e: Exception) {
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                Timber.d("error ${e.printStackTrace()}")
                ll_placeOrder.isEnabled = true
            }
        }, 1000)
    }

    fun editOrderApi(ordId:String, addShopData: AddShopDBModelEntity){
        progress_wheel.spin()
        var ordDtls = AppDatabase.getDBInstance()!!.newOrderDataDao().getOrderByID(ordId)
        var ordProductDtls = AppDatabase.getDBInstance()!!.newOrderProductDao().getProductsOrder(ordId)
        var editOrd = EditOrd()
        var editOrdProductL:ArrayList<SyncOrdProductL> = ArrayList()

        doAsync {
            editOrd.user_id = Pref.user_id!!
            editOrd.order_id = ordId
            editOrd.order_date = ordDtls.order_date
            editOrd.order_time = ordDtls.order_time
            editOrd.order_date_time = ordDtls.order_date_time
            editOrd.shop_id = ordDtls.shop_id
            editOrd.shop_name = ordDtls.shop_name
            editOrd.shop_type = ordDtls.shop_type
            editOrd.isInrange = ordDtls.isInrange
            editOrd.order_lat = ordDtls.order_lat
            editOrd.order_long = ordDtls.order_long
            editOrd.shop_addr = ordDtls.shop_addr
            editOrd.shop_pincode = ordDtls.shop_pincode
            editOrd.order_total_amt = ordDtls.order_total_amt.toDouble()
            editOrd.order_remarks = ordDtls.order_remarks

            editOrd.order_edit_date_time = ordDtls.order_edit_date_time
            editOrd.order_edit_remarks = ordDtls.order_edit_remarks

            for(i in 0..ordProductDtls.size-1){
                var obj = SyncOrdProductL()
                obj.order_id=ordProductDtls.get(i).order_id
                obj.product_id=ordProductDtls.get(i).product_id
                obj.product_name=ordProductDtls.get(i).product_name
                obj.submitedQty=ordProductDtls.get(i).submitedQty.toDouble()
                obj.submitedSpecialRate=ordProductDtls.get(i).submitedSpecialRate.toDouble()

                obj.total_amt=ordProductDtls.get(i).total_amt.toString().toDouble()
                obj.mrp=ordProductDtls.get(i).mrp.toString().toDouble()
                obj.itemPrice=ordProductDtls.get(i).itemPrice.toString().toDouble()

                editOrdProductL.add(obj)
            }
            editOrd.product_list = editOrdProductL

            uiThread {
                val repository = ProductListRepoProvider.productListProvider()
                BaseActivity.compositeDisposable.add(
                    repository.editProductListITC(editOrd)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as BaseResponse
                            if (response.status == NetworkConstant.SUCCESS) {
                                doAsync {
                                    AppDatabase.getDBInstance()!!.newOrderDataDao().updateIsEdited(editOrd.order_id,false)
                                    uiThread {
                                        progress_wheel.stopSpinning()
                                        msgShow("${AppUtils.hiFirstNameText()}. Your order for ${addShopData.shopName} has been saved successfully.Order No. is ${editOrd.order_id}")
                                    }
                                }
                            } else {
                                ll_placeOrder.isEnabled = true
                                progress_wheel.stopSpinning()
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                            }
                        }, { error ->
                            ll_placeOrder.isEnabled = true
                            progress_wheel.stopSpinning()
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                        })
                )
            }
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
            ordProductDtlsL = ArrayList()
            isCartChanges=false
            (mContext as DashboardActivity).onBackPressed()
        })
        simpleDialog.show()
        voiceOrderMsg()
    }

    private fun showRemarksAlert(){
        val simpleDialog = Dialog(mContext)
        simpleDialog.setCancelable(false)
        simpleDialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        simpleDialog.setContentView(R.layout.dialog_remarks)
        val dialogHeader = simpleDialog.findViewById(R.id.dialog_remarks_header_TV) as AppCustomTextView
        val tvOk = simpleDialog.findViewById(R.id.tv_remarks_ok) as AppCustomTextView
        val ivCancel = simpleDialog.findViewById(R.id.iv_remarks_cancel) as ImageView
        val etRemarks = simpleDialog.findViewById(R.id.et_remarks) as TextInputEditText
        dialogHeader.text = "Order Remarks"
        tvOk.setOnClickListener {
            var remarks = etRemarks.text.toString().trim()
            editSaveOrder(remarks)
            simpleDialog.cancel()
        }
        ivCancel.setOnClickListener({ view ->
            ll_placeOrder.isEnabled = true
            simpleDialog.cancel()
        })
        simpleDialog.show()
    }

    fun openDialog(text:String){
        val simpleDialog = Dialog(mContext)
        simpleDialog.setCancelable(false)
        simpleDialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        simpleDialog.setContentView(R.layout.dialog_ok)
        val dialogHeader = simpleDialog.findViewById(R.id.dialog_yes_header_TV) as AppCustomTextView
        dialogHeader.text = text
        val dialogYes = simpleDialog.findViewById(R.id.tv_dialog_yes) as AppCustomTextView
        dialogYes.setOnClickListener({ view ->
            simpleDialog.cancel()
        })
        simpleDialog.show()
    }

    private fun voiceOrderMsg() {
        if (Pref.isVoiceEnabledForOrderSaved) {
            val msg = "Hi, Order saved successfully."
            val speechStatus = (mContext as DashboardActivity).textToSpeech.speak(
                msg,
                TextToSpeech.QUEUE_FLUSH,
                null
            )
            if (speechStatus == TextToSpeech.ERROR)
                Log.e("Add Order", "TTS error in converting Text to Speech!")
        }
    }

    fun updateCart(){
        setData()
    }
}