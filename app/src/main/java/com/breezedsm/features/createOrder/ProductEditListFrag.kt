package com.breezedsm.features.createOrder

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.breezedsm.R
import com.breezedsm.app.AppDatabase
import com.breezedsm.app.utils.AppUtils
import com.breezedsm.app.utils.ToasterMiddle
import com.breezedsm.base.presentation.BaseFragment
import com.breezedsm.features.dashboard.presentation.DashboardActivity
import com.breezedsm.widgets.AppCustomEditText
import com.fsmevaluationlive.features.viewAllOrder.orderOptimized.ProductCatagoryDialog
import com.pnikosis.materialishprogress.ProgressWheel
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.Locale

class ProductEditListFrag: BaseFragment(), View.OnClickListener {
    private lateinit var mContext: Context
    private lateinit var ivMic: ImageView
    private lateinit var etSearch: AppCustomEditText
    private lateinit var ivSearch: ImageView
    private lateinit var ivFilter: ImageView

    private lateinit var ll_grSel: LinearLayout
    private lateinit var ll_catagorySel: LinearLayout
    private lateinit var ll_measureSel: LinearLayout
    private lateinit var tv_grSel: TextView
    private lateinit var tv_catagorySel: TextView
    private lateinit var tv_measureSel: TextView

    private lateinit var tv_produtCount: TextView
    private lateinit var tv_productAmt: TextView

    private lateinit var rv_productRate: RecyclerView
    private lateinit var productAdapter: AdapterProductList
    private lateinit var ll_cart: LinearLayout

    private lateinit var llFilterRoot: LinearLayout

    //private lateinit var finalOrderEditDataList: ArrayList<FinalProductRateSubmit>

    private lateinit var progressWheel: ProgressWheel

    private var selGrIDStr = ""
    private var selCategoryIDStr = ""
    private var selMeasureIDStr = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    companion object {
        var order_id: String = ""
        fun getInstance(objects: Any): ProductEditListFrag {
            val fragment = ProductEditListFrag()
            order_id = objects.toString()
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.frag_product_list, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View?) {
        ivMic = view!!.findViewById(R.id.iv_frag_ord_prod_mic)
        etSearch = view!!.findViewById(R.id.et_frag_ord_prod_search)
        ivSearch = view!!.findViewById(R.id.iv_frag_ord_prod_search)
        ivFilter = view!!.findViewById(R.id.iv_frag_ord_prod_filter)

        ll_grSel = view!!.findViewById(R.id.ll_frag_ord_pro_list_gr_sel_root)
        ll_catagorySel = view.findViewById(R.id.ll_frag_ord_pro_list_catag_sel_root)
        ll_measureSel = view.findViewById(R.id.ll_frag_ord_pro_list_measure_sel_root)
        tv_grSel = view.findViewById(R.id.tv_frag_ord_pro_list_gr_sel)
        tv_catagorySel = view.findViewById(R.id.tv_frag_ord_pro_list_catag_sel)
        tv_measureSel = view.findViewById(R.id.tv_frag_ord_pro_list_measure_sel)

        tv_produtCount = view.findViewById(R.id.tv_ord_prod_list_frag_count)
        tv_productAmt = view.findViewById(R.id.tv_ord_prod_list_frag_amt)
        ll_cart = view.findViewById(R.id.ll_ord_prod_list_frag_cart)

        rv_productRate = view.findViewById(R.id.rv_ord_pro_list_frag_product_list)
        progressWheel = view.findViewById(R.id.pw_frag_ord_pro_list)
        llFilterRoot = view.findViewById(R.id.ll_ord_pro_list_frag_filter_root)

        llFilterRoot.visibility= View.GONE

        ll_cart.setOnClickListener(this)
        ivSearch.setOnClickListener(this)
        ivMic.setOnClickListener(this)
        ivFilter.setOnClickListener(this)
        ll_grSel.setOnClickListener(this)
        ll_catagorySel.setOnClickListener(this)
        ll_measureSel.setOnClickListener(this)

        progressWheel.stopSpinning()


        /*finalOrderEditDataList = ArrayList()
        finalOrderEditDataList = AppDatabase.getDBInstance()!!.newOrderProductDao().getCustomOrdProductL(order_id) as ArrayList<FinalProductRateSubmit>
        */tv_productAmt.text = "₹" +String.format("%.2f",CartEditListFrag.ordProductDtlsL.sumByDouble { it.submitedQty.toInt() * it.submitedRate.toDouble() })
        tv_produtCount.text = CartEditListFrag.ordProductDtlsL.size.toString()

        var dataL : ArrayList<ProductRateList> = ArrayList()
        doAsync {
            progressWheel.spin()
            dataL = AppDatabase.getDBInstance()!!.newProductListDao().getProductRateL() as ArrayList<ProductRateList>
            uiThread {
                progressWheel.stopSpinning()
                showDataList(dataL)
            }
        }

        ivSearch.setOnClickListener {
            if(etSearch.text.toString().length > 0){
                filterAutoSearchData(etSearch.text.toString())
            }else{
                showDataList(dataL)
            }
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0.toString().length == 0){
                    showDataList(dataL)
                }
            }
        })

    }

    fun showDataList(dataL:ArrayList<ProductRateList>){
        var viewList:ArrayList<ProductRateList> = ArrayList()
        viewList = dataL.clone() as ArrayList<ProductRateList>
        productAdapter = AdapterProductList(mContext,viewList, CartEditListFrag.ordDtls.shop_id, CartEditListFrag.ordProductDtlsL,object:AdapterProductList.OnProductOptiOnClick{
            override fun onProductAddClick(productCount: Int, sumAmt: Double) {
                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                tv_produtCount.text = "${productCount.toString()}"
                tv_productAmt.text = "₹ ${String.format("%.2f",sumAmt.toDouble())}"
            }
        })
        rv_productRate.adapter = productAdapter
    }

    fun filterAutoSearchData(searchParam:String){
        progressWheel.spin()
        doAsync {
            var viewList = AppDatabase.getDBInstance()!!.newProductListDao().getProductRateFilteredL(searchParam) as ArrayList<ProductRateList>
            uiThread {
                progressWheel.stopSpinning()
                showDataList(viewList)
            }
        }
    }

    override fun onClick(p0: View?) {
        when(p0!!.id){
            ll_cart.id->{
                (mContext as DashboardActivity).onBackPressed()
            }
            R.id.iv_frag_ord_prod_mic -> {
                progressWheel.spin()
                val intent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                Handler().postDelayed(Runnable {
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"hi")
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH)
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?")
                }, 1000)
                try {
                    startActivityForResult(intent, 7009)
                    Handler().postDelayed(Runnable {
                        progressWheel.stopSpinning()
                    }, 3000)

                } catch (a: ActivityNotFoundException) {
                    a.printStackTrace()
                }
            }
            ivFilter.id->{
                if(llFilterRoot.visibility == View.VISIBLE){
                    llFilterRoot.visibility = View.GONE
                    var dataL : ArrayList<ProductRateList> = ArrayList()
                    doAsync {
                        progressWheel.spin()
                        dataL = AppDatabase.getDBInstance()!!.newProductListDao().getProductRateL() as ArrayList<ProductRateList>
                        uiThread {
                            progressWheel.stopSpinning()
                            showDataList(dataL)
                        }
                    }
                }else{
                    llFilterRoot.visibility = View.VISIBLE
                }
            }
            ll_grSel.id ->{
                ll_grSel.isEnabled = false

                var brandList = AppDatabase.getDBInstance()?.newProductListDao()?.getDistinctBrandList() as ArrayList<CommonProductCatagory>
                if (brandList.size > 0) {
                    ProductCatagoryDialog.newInstance(brandList, "Select Group", {
                        var str =
                            "<font color=#425066>Group : </font> <font color=#013446>${it.name_sel}</font>"
                        tv_grSel.text = Html.fromHtml(str)
                        selGrIDStr = it.id_sel

                        selCategoryIDStr = ""
                        tv_catagorySel.text = "Search by Category"

                        selMeasureIDStr = ""
                        tv_measureSel.text = "Search by Measurement"

                        showDataList(AppDatabase.getDBInstance()?.newProductListDao()?.getProductRateFilteredLByBrand(selGrIDStr) as ArrayList<ProductRateList>)

                    }).show((mContext as DashboardActivity).supportFragmentManager, "")
                } else {
                    ll_grSel.isEnabled = true
                    ToasterMiddle.msgShort(mContext, "No Group Found")
                }

                Handler().postDelayed(Runnable {
                    ll_grSel.isEnabled = true
                }, 1000)
            }
            ll_catagorySel.id->{
                ll_catagorySel.isEnabled = false

                if (!selGrIDStr.equals("")) {
                    var categoryList = AppDatabase.getDBInstance()?.newProductListDao()?.getDistinctCategoryList(selGrIDStr) as ArrayList<CommonProductCatagory>
                    if (categoryList.size > 0) {
                        ProductCatagoryDialog.newInstance(categoryList, "Select Category", {
                            var str = "<font color=#425066>Category : </font> <font color=#013446>${it.name_sel}</font>"
                            tv_catagorySel.text = Html.fromHtml(str)
                            selCategoryIDStr = it.id_sel

                            selMeasureIDStr = ""
                            tv_measureSel.text = "Search by Measurement"

                            showDataList(AppDatabase.getDBInstance()?.newProductListDao()?.getProductRateFilteredLByBrandCategory(selGrIDStr,selCategoryIDStr) as ArrayList<ProductRateList>)

                        }).show((mContext as DashboardActivity).supportFragmentManager, "")
                    } else {
                        ToasterMiddle.msgShort(mContext, "No Category Found")
                    }
                }else{
                    ll_catagorySel.isEnabled = true
                    ToasterMiddle.msgShort(mContext, "Please select Group first")
                }

                Handler().postDelayed(Runnable {
                    ll_catagorySel.isEnabled = true
                }, 1000)
            }
            ll_measureSel.id->{
                ll_measureSel.isEnabled = false

                if (!selGrIDStr.equals("")) {
                    if(!selCategoryIDStr.equals("")){
                        var measureList: ArrayList<CommonProductCatagory> = ArrayList()
                        measureList = AppDatabase.getDBInstance()?.newProductListDao()?.getDistinctMeasureList(selGrIDStr,selCategoryIDStr) as ArrayList<CommonProductCatagory>
                        if (measureList.size > 0) {
                            ProductCatagoryDialog.newInstance(measureList, "Select Measurement", {
                                var str = "<font color=#425066>Measurement : </font> <font color=#013446>${it.name_sel}</font>"
                                tv_measureSel.text = Html.fromHtml(str)
                                selMeasureIDStr = it.id_sel

                                showDataList(AppDatabase.getDBInstance()?.newProductListDao()?.getProductRateFilteredLByBrandCategoryWatt(selGrIDStr,selCategoryIDStr,selMeasureIDStr) as ArrayList<ProductRateList>)

                            }).show((mContext as DashboardActivity).supportFragmentManager, "")
                        } else {
                            ToasterMiddle.msgShort(mContext, "No Measurement Found")
                        }
                    }else{
                        ToasterMiddle.msgShort(mContext, "Please select Category first")
                    }
                } else {
                    ll_measureSel.isEnabled = true
                    ToasterMiddle.msgShort(mContext, "Please select Group first")
                }

                Handler().postDelayed(Runnable {
                    ll_measureSel.isEnabled = true
                }, 1000)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 7009){
            try {
                val result = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                etSearch.setText(result!![0].toString())
            }catch (ex:Exception){
                ex.printStackTrace()
            }
        }
    }


}