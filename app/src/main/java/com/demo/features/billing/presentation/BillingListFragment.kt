package com.demo.features.billing.presentation

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elvishew.xlog.XLog
import com.pnikosis.materialishprogress.ProgressWheel
import com.demo.R
import com.demo.app.AppDatabase
import com.demo.app.NetworkConstant
import com.demo.app.Pref
import com.demo.app.domain.*
import com.demo.app.types.FragType
import com.demo.app.utils.AppUtils
import com.demo.app.utils.FTStorageUtils
import com.demo.base.BaseResponse
import com.demo.base.presentation.BaseActivity
import com.demo.base.presentation.BaseFragment
import com.demo.features.addshop.api.AddShopRepositoryProvider
import com.demo.features.addshop.api.assignToPPList.AssignToPPListRepoProvider
import com.demo.features.addshop.api.assignedToDDList.AssignToDDListRepoProvider
import com.demo.features.addshop.api.typeList.TypeListRepoProvider
import com.demo.features.addshop.model.AddShopRequestData
import com.demo.features.addshop.model.AddShopResponse
import com.demo.features.addshop.model.AssignedToShopListResponseModel
import com.demo.features.addshop.model.assigntoddlist.AssignToDDListResponseModel
import com.demo.features.addshop.model.assigntopplist.AssignToPPListResponseModel
import com.demo.features.billing.api.AddBillingRepoProvider
import com.demo.features.billing.model.AddBillingInputParamsModel
import com.demo.features.dashboard.presentation.DashboardActivity
import com.demo.features.location.LocationWizard
import com.demo.features.location.model.ShopDurationRequest
import com.demo.features.location.model.ShopDurationRequestData
import com.demo.features.location.shopdurationapi.ShopDurationRepositoryProvider
import com.demo.features.nearbyshops.presentation.QrCodeDialog
import com.demo.features.viewAllOrder.api.addorder.AddOrderRepoProvider
import com.demo.features.viewAllOrder.model.AddOrderInputParamsModel
import com.demo.features.viewAllOrder.model.AddOrderInputProductList
import com.demo.widgets.AppCustomTextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import kotlin.collections.ArrayList

/**
 * Created by Saikat on 19-02-2019.
 */
class BillingListFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mContext: Context
    private lateinit var rv_billing_list: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var progress_wheel: ProgressWheel
    private lateinit var tv_no_data_available: AppCustomTextView

    companion object {

        private var order: OrderDetailsListEntity? = null

        fun newInstance(objects: Any): BillingListFragment {
            val fragment = BillingListFragment()

            if (objects is OrderDetailsListEntity)
                order = objects
            return fragment
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_billing_list, container, false)
        initView(view)

        val list = AppDatabase.getDBInstance()!!.billingDao().getDataOrderIdWise(order?.order_id!!) as ArrayList
        if (list != null && list.isNotEmpty())
            initAdapter(list)
        else {
            tv_no_data_available.visibility = View.VISIBLE
        }

        return view
    }

    private fun initView(view: View) {
        fab = view.findViewById(R.id.fab)
        progress_wheel = view.findViewById(R.id.progress_wheel)
        progress_wheel.stopSpinning()
        rv_billing_list = view.findViewById(R.id.rv_billing_list)
        rv_billing_list.layoutManager = LinearLayoutManager(mContext)
        tv_no_data_available = view.findViewById(R.id.tv_no_data_available)

        if (Pref.canAddBillingFromBillingList)
            fab.visibility = View.VISIBLE
        else
            fab.visibility = View.GONE

        fab.setOnClickListener(this)
    }

    private fun initAdapter(list: ArrayList<BillingEntity>) {
        tv_no_data_available.visibility = View.GONE
        rv_billing_list.adapter = BillingListAdapter(mContext, list, object : BillingListAdapter.OnClickListener {
            override fun onSyncClick(adapterPosition: Int) {
                if (AppUtils.isOnline(mContext)) {
                    if (!(mContext as DashboardActivity).shop?.isUploaded!!)
                        syncShop(list[adapterPosition], (mContext as DashboardActivity).shop!!)
                    else
                        checkToCallOrderApi(list[adapterPosition])
                } else
                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
            }

            override fun onDownloadClick(bill: BillingEntity) {
                val heading = "SALES INVOICE"
                var pdfBody = "\n\n\n\nInvoice No.: " + bill.invoice_no + "\n\nInvoice Date: " +
                        AppUtils.convertToCommonFormat(bill.invoice_date) + "\n\nInvoice Amount: " + getString(R.string.rupee_symbol_with_space) + bill.invoice_amount +
                        "\n\nOrder No.: " + bill.order_id + "\n\nOrder Date: "

                val order = AppDatabase.getDBInstance()!!.orderDetailsListDao().getSingleOrder(bill.order_id)
                val shop = AppDatabase.getDBInstance()?.addShopEntryDao()?.getShopByIdN(order?.shop_id)

                pdfBody = pdfBody + order?.only_date!! + "\n\nParty Name: " + shop?.shopName + "\n\nAddress: " + shop?.address +
                        "\n\nContact No.: " + shop?.ownerContactNumber + "\n\nSales Person: " + Pref.user_name + "\n\n\n"

                val productList = AppDatabase.getDBInstance()!!.orderProductListDao().getDataAccordingToOrderId(order.order_id!!)
                productList?.forEach {it1 ->
                    pdfBody = pdfBody + "Item: " + it1.product_name + "\nQty: " + it1.qty + "  Rate: " +
                            getString(R.string.rupee_symbol_with_space) + it1.rate + "  Amount: " + getString(R.string.rupee_symbol_with_space) +
                            it1.total_price + "\n\n"
                }

                pdfBody = pdfBody + "Total Amount: " + getString(R.string.rupee_symbol_with_space) + order.amount

                val image = BitmapFactory.decodeResource(mContext.resources, R.mipmap.ic_launcher)

                val path = FTStorageUtils.stringToPdf(pdfBody, mContext, "FTS_" + bill.bill_id + ".pdf", image,
                        heading, 2.7f)

                if (!TextUtils.isEmpty(path)) {
                    try {
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        val fileUrl = Uri.parse(path)

                        val file = File(fileUrl.path)
                        val uri = Uri.fromFile(file)
                        shareIntent.type = "image/png"
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                        startActivity(Intent.createChooser(shareIntent, "Share pdf using"));
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                else
                    (mContext as DashboardActivity).showSnackMessage("Pdf can not be sent.")
            }

            override fun onCreateQrClick(bill: BillingEntity) {
                var pdfBody = "Invoice No.: " + bill.invoice_no + "\n\nInvoice Date: " +
                        AppUtils.convertToCommonFormat(bill.invoice_date) + "\n\nInvoice Amount: INR." + bill.invoice_amount +
                        "\n\nOrder No.: " + bill.order_id + "\n\nOrder Date: "

                val order = AppDatabase.getDBInstance()!!.orderDetailsListDao().getSingleOrder(bill.order_id)
                val shop = AppDatabase.getDBInstance()?.addShopEntryDao()?.getShopByIdN(order?.shop_id)

                pdfBody = pdfBody + order?.only_date!! + "\n\nParty Name: " + shop?.shopName + "\n\nAddress: " + shop?.address +
                        "\n\nContact No.: " + shop?.ownerContactNumber + "\n\nSales Person: " + Pref.user_name + "\n\n\n"

                val productList = AppDatabase.getDBInstance()!!.orderProductListDao().getDataAccordingToOrderId(order.order_id!!)
                productList?.forEach {it1 ->
                    pdfBody = pdfBody + "Item: " + it1.product_name + "\nQty: " + it1.qty + "  Rate: INR." + it1.rate + "  Amount: INR." +
                            it1.total_price + "\n\n"
                }

                pdfBody = pdfBody + "Total Amount: INR." + order.amount

                val bitmap = AppUtils.createQrCode(pdfBody)

                if (bitmap != null)
                    QrCodeDialog.newInstance(bitmap, shop?.shop_id!!, shop.shopName!!, bill.bill_id!!, "Create QR of Invoice").show((mContext as DashboardActivity).supportFragmentManager, "")
            }
        })
    }

    private fun syncShop(billing: BillingEntity, shop: AddShopDBModelEntity) {
        val addShopData = AddShopRequestData()
        //if (!shop.isUploaded) {
        addShopData.session_token = Pref.session_token
        addShopData.address = shop.address
        addShopData.owner_contact_no = shop.ownerContactNumber
        addShopData.owner_email = shop.ownerEmailId
        addShopData.owner_name = shop.ownerName
        addShopData.pin_code = shop.pinCode
        addShopData.shop_lat = shop.shopLat.toString()
        addShopData.shop_long = shop.shopLong.toString()
        addShopData.shop_name = shop.shopName.toString()
        addShopData.type = shop.type.toString()
        addShopData.shop_id = shop.shop_id
        addShopData.user_id = Pref.user_id

        if (!TextUtils.isEmpty(shop.dateOfBirth))
            addShopData.dob = AppUtils.changeAttendanceDateFormatToCurrent(shop.dateOfBirth)

        if (!TextUtils.isEmpty(shop.dateOfAniversary))
            addShopData.date_aniversary = AppUtils.changeAttendanceDateFormatToCurrent(shop.dateOfAniversary)

        addShopData.assigned_to_dd_id = shop.assigned_to_dd_id
        addShopData.assigned_to_pp_id = shop.assigned_to_pp_id
        addShopData.added_date = shop.added_date
        addShopData.amount = shop.amount
        addShopData.area_id = shop.area_id
        addShopData.model_id = shop.model_id
        addShopData.primary_app_id = shop.primary_app_id
        addShopData.secondary_app_id = shop.secondary_app_id
        addShopData.lead_id = shop.lead_id
        addShopData.stage_id = shop.stage_id
        addShopData.funnel_stage_id = shop.funnel_stage_id
        addShopData.booking_amount = shop.booking_amount
        addShopData.type_id = shop.type_id

        addShopData.director_name = shop.director_name
        addShopData.key_person_name = shop.person_name
        addShopData.phone_no = shop.person_no

        if (!TextUtils.isEmpty(shop.family_member_dob))
            addShopData.family_member_dob = AppUtils.changeAttendanceDateFormatToCurrent(shop.family_member_dob)

        if (!TextUtils.isEmpty(shop.add_dob))
            addShopData.addtional_dob = AppUtils.changeAttendanceDateFormatToCurrent(shop.add_dob)

        if (!TextUtils.isEmpty(shop.add_doa))
            addShopData.addtional_doa = AppUtils.changeAttendanceDateFormatToCurrent(shop.add_doa)

        addShopData.specialization = shop.specialization
        addShopData.category = shop.category
        addShopData.doc_address = shop.doc_address
        addShopData.doc_pincode = shop.doc_pincode
        addShopData.is_chamber_same_headquarter = shop.chamber_status.toString()
        addShopData.is_chamber_same_headquarter_remarks = shop.remarks
        addShopData.chemist_name = shop.chemist_name
        addShopData.chemist_address = shop.chemist_address
        addShopData.chemist_pincode = shop.chemist_pincode
        addShopData.assistant_contact_no = shop.assistant_no
        addShopData.average_patient_per_day = shop.patient_count
        addShopData.assistant_name = shop.assistant_name

        if (!TextUtils.isEmpty(shop.doc_family_dob))
            addShopData.doc_family_member_dob = AppUtils.changeAttendanceDateFormatToCurrent(shop.doc_family_dob)

        if (!TextUtils.isEmpty(shop.assistant_dob))
            addShopData.assistant_dob = AppUtils.changeAttendanceDateFormatToCurrent(shop.assistant_dob)

        if (!TextUtils.isEmpty(shop.assistant_doa))
            addShopData.assistant_doa = AppUtils.changeAttendanceDateFormatToCurrent(shop.assistant_doa)

        if (!TextUtils.isEmpty(shop.assistant_family_dob))
            addShopData.assistant_family_dob = AppUtils.changeAttendanceDateFormatToCurrent(shop.assistant_family_dob)

        addShopData.entity_id = shop.entity_id
        addShopData.party_status_id = shop.party_status_id
        addShopData.retailer_id = shop.retailer_id
        addShopData.dealer_id = shop.dealer_id
        addShopData.beat_id = shop.beat_id
        addShopData.assigned_to_shop_id = shop.assigned_to_shop_id
        addShopData.actual_address = shop.actual_address

        var uniqKeyObj=AppDatabase.getDBInstance()!!.shopActivityDao().getNewShopActivityKey(shop.shop_id,false)
        addShopData.shop_revisit_uniqKey=uniqKeyObj?.shop_revisit_uniqKey!!

        callAddShopApi(addShopData, shop.shopImageLocalPath, shop.doc_degree, billing)
        //}
    }

    private fun callAddShopApi(addShop: AddShopRequestData, shop_imgPath: String?, degree_imgPath: String?, billing: BillingEntity) {

        if (BaseActivity.isApiInitiated)
            return

        BaseActivity.isApiInitiated = true


        progress_wheel.spin()


        XLog.d("=======SyncShop Input Params (Add Billing)=============")
        XLog.d("shop id=======> " + addShop.shop_id)
        val index = addShop.shop_id!!.indexOf("_")
        XLog.d("decoded shop id=======> " + addShop.user_id + "_" + AppUtils.getDate(addShop.shop_id!!.substring(index + 1, addShop.shop_id!!.length).toLong()))
        XLog.d("shop added date=======> " + addShop.added_date)
        XLog.d("shop address=======> " + addShop.address)
        XLog.d("assigned to dd id=======> " + addShop.assigned_to_dd_id)
        XLog.d("assigned to pp id=======> " + addShop.assigned_to_pp_id)
        XLog.d("date aniversery=======> " + addShop.date_aniversary)
        XLog.d("dob=======> " + addShop.dob)
        XLog.d("shop owner phn no=======> " + addShop.owner_contact_no)
        XLog.d("shop owner email=======> " + addShop.owner_email)
        XLog.d("shop owner name=======> " + addShop.owner_name)
        XLog.d("shop pincode=======> " + addShop.pin_code)
        XLog.d("session token=======> " + addShop.session_token)
        XLog.d("shop lat=======> " + addShop.shop_lat)
        XLog.d("shop long=======> " + addShop.shop_long)
        XLog.d("shop name=======> " + addShop.shop_name)
        XLog.d("shop type=======> " + addShop.type)
        XLog.d("user id=======> " + addShop.user_id)
        XLog.d("amount=======> " + addShop.amount)
        XLog.d("area id=======> " + addShop.area_id)
        XLog.d("model id=======> " + addShop.model_id)
        XLog.d("primary app id=======> " + addShop.primary_app_id)
        XLog.d("secondary app id=======> " + addShop.secondary_app_id)
        XLog.d("lead id=======> " + addShop.lead_id)
        XLog.d("stage id=======> " + addShop.stage_id)
        XLog.d("funnel stage id=======> " + addShop.funnel_stage_id)
        XLog.d("booking amount=======> " + addShop.booking_amount)
        XLog.d("type id=======> " + addShop.type_id)

        if (shop_imgPath != null)
            XLog.d("shop image path=======> $shop_imgPath")

        XLog.d("director name=======> " + addShop.director_name)
        XLog.d("family member dob=======> " + addShop.family_member_dob)
        XLog.d("key person's name=======> " + addShop.key_person_name)
        XLog.d("phone no=======> " + addShop.phone_no)
        XLog.d("additional dob=======> " + addShop.addtional_dob)
        XLog.d("additional doa=======> " + addShop.addtional_doa)
        XLog.d("doctor family member dob=======> " + addShop.doc_family_member_dob)
        XLog.d("specialization=======> " + addShop.specialization)
        XLog.d("average patient count per day=======> " + addShop.average_patient_per_day)
        XLog.d("category=======> " + addShop.category)
        XLog.d("doctor address=======> " + addShop.doc_address)
        XLog.d("doctor pincode=======> " + addShop.doc_pincode)
        XLog.d("chambers or hospital under same headquarter=======> " + addShop.is_chamber_same_headquarter)
        XLog.d("chamber related remarks=======> " + addShop.is_chamber_same_headquarter_remarks)
        XLog.d("chemist name=======> " + addShop.chemist_name)
        XLog.d("chemist name=======> " + addShop.chemist_address)
        XLog.d("chemist pincode=======> " + addShop.chemist_pincode)
        XLog.d("assistant name=======> " + addShop.assistant_name)
        XLog.d("assistant contact no=======> " + addShop.assistant_contact_no)
        XLog.d("assistant dob=======> " + addShop.assistant_dob)
        XLog.d("assistant date of anniversary=======> " + addShop.assistant_doa)
        XLog.d("assistant family dob=======> " + addShop.assistant_family_dob)
        XLog.d("entity id=======> " + addShop.entity_id)
        XLog.d("party status id=======> " + addShop.party_status_id)
        XLog.d("retailer id=======> " + addShop.retailer_id)
        XLog.d("dealer id=======> " + addShop.dealer_id)
        XLog.d("beat id=======> " + addShop.beat_id)
        XLog.d("assigned to shop id=======> " + addShop.assigned_to_shop_id)
        XLog.d("actual address=======> " + addShop.actual_address)

        if (degree_imgPath != null)
            XLog.d("doctor degree image path=======> $degree_imgPath")
        XLog.d("======================================================")

        if (TextUtils.isEmpty(shop_imgPath) && TextUtils.isEmpty(degree_imgPath)) {
            val repository = AddShopRepositoryProvider.provideAddShopWithoutImageRepository()
            BaseActivity.compositeDisposable.add(
                    repository.addShop(addShop)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ result ->
                                val addShopResult = result as AddShopResponse
                                XLog.d("syncShopFromShopList : " + ", SHOP: " + addShop.shop_name + ", RESPONSE:" + result.message)
                                if (addShopResult.status == NetworkConstant.SUCCESS) {
                                    AppDatabase.getDBInstance()!!.addShopEntryDao().updateIsUploaded(true, addShop.shop_id)

                                    doAsync {
                                        val resultAs = runLongTask(addShop.shop_id)
                                        uiThread {
                                            if (resultAs == true) {
                                                BaseActivity.isApiInitiated = false
                                                getAssignedPPListApi(addShop.shop_id, billing)
                                            }
                                        }
                                    }
                                    progress_wheel.stopSpinning()

                                } else if (addShopResult.status == NetworkConstant.DUPLICATE_SHOP_ID) {
                                    XLog.d("DuplicateShop : " + ", SHOP: " + addShop.shop_name)
                                    AppDatabase.getDBInstance()!!.addShopEntryDao().updateIsUploaded(true, addShop.shop_id)
                                    progress_wheel.stopSpinning()
                                    //(mContext as DashboardActivity).showSnackMessage(addShopResult.message!!)
                                    if (AppDatabase.getDBInstance()!!.addShopEntryDao().getDuplicateShopData(addShop.owner_contact_no).size > 0) {
                                        AppDatabase.getDBInstance()!!.addShopEntryDao().deleteShopById(addShop.shop_id)
                                        AppDatabase.getDBInstance()!!.shopActivityDao().deleteShopByIdAndDate(addShop.shop_id!!, AppUtils.getCurrentDateForShopActi())
                                    }
                                    doAsync {
                                        val resultAs = runLongTask(addShop.shop_id)
                                        uiThread {
                                            if (resultAs == true) {
                                                BaseActivity.isApiInitiated = false
                                                getAssignedPPListApi(addShop.shop_id, billing)
                                            }
                                        }
                                    }
                                } else {
                                    BaseActivity.isApiInitiated = false
                                    progress_wheel.stopSpinning()
                                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.unable_to_sync))
                                }

                            }, { error ->
                                error.printStackTrace()
                                BaseActivity.isApiInitiated = false
                                progress_wheel.stopSpinning()
                                if (error != null)
                                    XLog.d("syncShopFromShopList : " + ", SHOP: " + addShop.shop_name + error.localizedMessage)
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.unable_to_sync))
                            })
            )
        }
        else {
            val repository = AddShopRepositoryProvider.provideAddShopRepository()
            BaseActivity.compositeDisposable.add(
                    repository.addShopWithImage(addShop, shop_imgPath, degree_imgPath, mContext)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ result ->
                                val addShopResult = result as AddShopResponse
                                XLog.d("syncShopFromShopList : " + ", SHOP: " + addShop.shop_name + ", RESPONSE:" + result.message)
                                if (addShopResult.status == NetworkConstant.SUCCESS) {
                                    AppDatabase.getDBInstance()!!.addShopEntryDao().updateIsUploaded(true, addShop.shop_id)

                                    doAsync {
                                        val resultAs = runLongTask(addShop.shop_id)
                                        uiThread {
                                            if (resultAs == true) {
                                                BaseActivity.isApiInitiated = false
                                                getAssignedPPListApi(addShop.shop_id, billing)
                                            }
                                        }
                                    }
                                    progress_wheel.stopSpinning()

                                } else if (addShopResult.status == NetworkConstant.DUPLICATE_SHOP_ID) {
                                    XLog.d("DuplicateShop : " + ", SHOP: " + addShop.shop_name)
                                    AppDatabase.getDBInstance()!!.addShopEntryDao().updateIsUploaded(true, addShop.shop_id)
                                    progress_wheel.stopSpinning()
                                    //(mContext as DashboardActivity).showSnackMessage(addShopResult.message!!)
                                    if (AppDatabase.getDBInstance()!!.addShopEntryDao().getDuplicateShopData(addShop.owner_contact_no).size > 0) {
                                        AppDatabase.getDBInstance()!!.addShopEntryDao().deleteShopById(addShop.shop_id)
                                        AppDatabase.getDBInstance()!!.shopActivityDao().deleteShopByIdAndDate(addShop.shop_id!!, AppUtils.getCurrentDateForShopActi())
                                    }
                                    doAsync {
                                        val resultAs = runLongTask(addShop.shop_id)
                                        uiThread {
                                            if (resultAs == true) {
                                                BaseActivity.isApiInitiated = false
                                                getAssignedPPListApi(addShop.shop_id, billing)
                                            }
                                        }
                                    }
                                } else {
                                    BaseActivity.isApiInitiated = false
                                    progress_wheel.stopSpinning()
                                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.unable_to_sync))
                                }

                            }, { error ->
                                error.printStackTrace()
                                BaseActivity.isApiInitiated = false
                                progress_wheel.stopSpinning()
                                if (error != null)
                                    XLog.d("syncShopFromShopList : " + ", SHOP: " + addShop.shop_name + error.localizedMessage)
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.unable_to_sync))
                            })
            )
        }
    }

    private fun runLongTask(shop_id: String?): Any {
        val shopActivity = AppDatabase.getDBInstance()!!.shopActivityDao().durationAvailableForShop(shop_id!!, true, false)
        if (shopActivity != null)
            callShopActivitySubmit(shop_id)
        return true
    }

    private var shop_duration = ""
    private var startTimeStamp = ""
    private fun callShopActivitySubmit(shopId: String) {
        var list = AppDatabase.getDBInstance()!!.shopActivityDao().getShopForDay(shopId, AppUtils.getCurrentDateForShopActi())
        if (list.isEmpty())
            return
        var shopDataList: MutableList<ShopDurationRequestData> = java.util.ArrayList()
        var shopDurationApiReq = ShopDurationRequest()
        shopDurationApiReq.user_id = Pref.user_id
        shopDurationApiReq.session_token = Pref.session_token

        if (!Pref.isMultipleVisitEnable) {
            var shopActivity = list[0]

            var shopDurationData = ShopDurationRequestData()
            shopDurationData.shop_id = shopActivity.shopid
            if (shopActivity.startTimeStamp != "0" && !shopActivity.isDurationCalculated) {
                val totalMinute = AppUtils.getMinuteFromTimeStamp(shopActivity.startTimeStamp, System.currentTimeMillis().toString())
                val duration = AppUtils.getTimeFromTimeSpan(shopActivity.startTimeStamp, System.currentTimeMillis().toString())

                AppDatabase.getDBInstance()!!.shopActivityDao().updateTotalMinuteForDayOfShop(shopActivity.shopid!!, totalMinute, AppUtils.getCurrentDateForShopActi())
                AppDatabase.getDBInstance()!!.shopActivityDao().updateTimeDurationForDayOfShop(shopActivity.shopid!!, duration, AppUtils.getCurrentDateForShopActi())

                shopDurationData.spent_duration = duration
            } else {
                shopDurationData.spent_duration = shopActivity.duration_spent
            }
            shopDurationData.visited_date = shopActivity.visited_date
            shopDurationData.visited_time = shopActivity.visited_date
            if (TextUtils.isEmpty(shopActivity.distance_travelled))
                shopActivity.distance_travelled = "0.0"
            shopDurationData.distance_travelled = shopActivity.distance_travelled
            var sList = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdList(shopDurationData.shop_id)
            if (sList != null && sList.isNotEmpty())
                shopDurationData.total_visit_count = sList[0].totalVisitCount

            if (!TextUtils.isEmpty(shopActivity.feedback))
                shopDurationData.feedback = shopActivity.feedback
            else
                shopDurationData.feedback = ""

            shopDurationData.isFirstShopVisited = shopActivity.isFirstShopVisited
            shopDurationData.distanceFromHomeLoc = shopActivity.distance_from_home_loc
            shopDurationData.next_visit_date = shopActivity.next_visit_date

            if (!TextUtils.isEmpty(shopActivity.early_revisit_reason))
                shopDurationData.early_revisit_reason = shopActivity.early_revisit_reason
            else
                shopDurationData.early_revisit_reason = ""

            shopDurationData.device_model = shopActivity.device_model
            shopDurationData.android_version = shopActivity.android_version
            shopDurationData.battery = shopActivity.battery
            shopDurationData.net_status = shopActivity.net_status
            shopDurationData.net_type = shopActivity.net_type
            shopDurationData.in_time = shopActivity.in_time
            shopDurationData.out_time = shopActivity.out_time
            shopDurationData.start_timestamp = shopActivity.startTimeStamp
            shopDurationData.in_location = shopActivity.in_loc
            shopDurationData.out_location = shopActivity.out_loc

            shopDurationData.shop_revisit_uniqKey = shopActivity.shop_revisit_uniqKey!!

            shopDataList.add(shopDurationData)
        }
        else {
            for (i in list.indices) {
                var shopActivity = list[i]

                var shopDurationData = ShopDurationRequestData()
                shopDurationData.shop_id = shopActivity.shopid
                if (shopActivity.startTimeStamp != "0" && !shopActivity.isDurationCalculated) {
                    val totalMinute = AppUtils.getMinuteFromTimeStamp(shopActivity.startTimeStamp, System.currentTimeMillis().toString())
                    val duration = AppUtils.getTimeFromTimeSpan(shopActivity.startTimeStamp, System.currentTimeMillis().toString())

                    AppDatabase.getDBInstance()!!.shopActivityDao().updateTotalMinuteForDayOfShop(shopActivity.shopid!!, totalMinute, AppUtils.getCurrentDateForShopActi(), shopActivity.startTimeStamp)
                    AppDatabase.getDBInstance()!!.shopActivityDao().updateTimeDurationForDayOfShop(shopActivity.shopid!!, duration, AppUtils.getCurrentDateForShopActi(), shopActivity.startTimeStamp)

                    shopDurationData.spent_duration = duration
                } else {
                    shopDurationData.spent_duration = shopActivity.duration_spent
                }
                shopDurationData.visited_date = shopActivity.visited_date
                shopDurationData.visited_time = shopActivity.visited_date

                if (TextUtils.isEmpty(shopActivity.distance_travelled))
                    shopActivity.distance_travelled = "0.0"

                shopDurationData.distance_travelled = shopActivity.distance_travelled

                var sList = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdList(shopDurationData.shop_id)
                if (sList != null && sList.isNotEmpty())
                    shopDurationData.total_visit_count = sList[0].totalVisitCount

                if (!TextUtils.isEmpty(shopActivity.feedback))
                    shopDurationData.feedback = shopActivity.feedback
                else
                    shopDurationData.feedback = ""

                shopDurationData.isFirstShopVisited = shopActivity.isFirstShopVisited
                shopDurationData.distanceFromHomeLoc = shopActivity.distance_from_home_loc
                shopDurationData.next_visit_date = shopActivity.next_visit_date

                if (!TextUtils.isEmpty(shopActivity.early_revisit_reason))
                    shopDurationData.early_revisit_reason = shopActivity.early_revisit_reason
                else
                    shopDurationData.early_revisit_reason = ""

                shopDurationData.device_model = shopActivity.device_model
                shopDurationData.android_version = shopActivity.android_version
                shopDurationData.battery = shopActivity.battery
                shopDurationData.net_status = shopActivity.net_status
                shopDurationData.net_type = shopActivity.net_type
                shopDurationData.in_time = shopActivity.in_time
                shopDurationData.out_time = shopActivity.out_time
                shopDurationData.start_timestamp = shopActivity.startTimeStamp
                shopDurationData.in_location = shopActivity.in_loc
                shopDurationData.out_location = shopActivity.out_loc

                shopDurationData.shop_revisit_uniqKey = shopActivity.shop_revisit_uniqKey!!

                shopDataList.add(shopDurationData)
            }
        }

        if (shopDataList.isEmpty()) {
            return
        }

        shopDurationApiReq.shop_list = shopDataList
        val repository = ShopDurationRepositoryProvider.provideShopDurationRepository()

        BaseActivity.compositeDisposable.add(
                repository.shopDuration(shopDurationApiReq)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            XLog.d("syncShopActivityFromShopList : " + ", SHOP: " + list[0].shop_name + ", RESPONSE:" + result.message)
                            if (result.status == NetworkConstant.SUCCESS) {

                            }

                        }, { error ->
                            error.printStackTrace()
                            if (error != null)
                                XLog.d("syncShopActivityFromShopList : " + ", SHOP: " + list[0].shop_name + error.localizedMessage)
//                                (mContext as DashboardActivity).showSnackMessage("ERROR")
                        })
        )

    }

    private fun getAssignedPPListApi(shop_id: String?, billing: BillingEntity) {

        val shopActivityList = AppDatabase.getDBInstance()!!.shopActivityDao().getShopForDay(shop_id!!, AppUtils.getCurrentDateForShopActi())

        /*if (shopActivityList[0].isVisited && shopActivityList[0].isDurationCalculated) {
            if (!Pref.isMultipleVisitEnable)
                AppDatabase.getDBInstance()!!.shopActivityDao().updateisUploaded(true, shop_id, AppUtils.getCurrentDateForShopActi())
            else
                AppDatabase.getDBInstance()!!.shopActivityDao().updateisUploaded(true, shop_id, AppUtils.getCurrentDateForShopActi(), startTimeStamp)
            XLog.d("============sync locally shop visited (Add Billing)==========")
        }*/

        shopActivityList?.forEach {
            if (it.isVisited && it.isDurationCalculated) {
                if (!Pref.isMultipleVisitEnable)
                    AppDatabase.getDBInstance()!!.shopActivityDao().updateisUploaded(true, shop_id, AppUtils.getCurrentDateForShopActi())
                else
                    AppDatabase.getDBInstance()!!.shopActivityDao().updateisUploaded(true, shop_id, AppUtils.getCurrentDateForShopActi(), startTimeStamp)
                XLog.d("============sync locally shop visited (Billing List)==========")
            }
        }

        if (BaseActivity.isApiInitiated)
            return

        BaseActivity.isApiInitiated = true

        val repository = AssignToPPListRepoProvider.provideAssignPPListRepository()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
                repository.assignToPPList(Pref.profile_state)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as AssignToPPListResponseModel
                            if (response.status == NetworkConstant.SUCCESS) {
                                val list = response.assigned_to_pp_list

                                if (list != null && list.isNotEmpty()) {

                                    doAsync {

                                        val assignPPList = AppDatabase.getDBInstance()?.ppListDao()?.getAll()
                                        if (assignPPList != null)
                                            AppDatabase.getDBInstance()?.ppListDao()?.delete()

                                        for (i in list.indices) {
                                            val assignToPP = AssignToPPEntity()
                                            assignToPP.pp_id = list[i].assigned_to_pp_id
                                            assignToPP.pp_name = list[i].assigned_to_pp_authorizer_name
                                            assignToPP.pp_phn_no = list[i].phn_no
                                            AppDatabase.getDBInstance()?.ppListDao()?.insert(assignToPP)
                                        }

                                        uiThread {
                                            BaseActivity.isApiInitiated = false
                                            progress_wheel.stopSpinning()
                                            getAssignedDDListApi(shop_id, billing)
                                        }
                                    }
                                } else {
                                    BaseActivity.isApiInitiated = false
                                    progress_wheel.stopSpinning()
                                    getAssignedDDListApi(shop_id, billing)
                                }
                            } else {
                                BaseActivity.isApiInitiated = false
                                progress_wheel.stopSpinning()
                                getAssignedDDListApi(shop_id, billing)
                            }

                        }, { error ->
                            error.printStackTrace()
                            BaseActivity.isApiInitiated = false
                            progress_wheel.stopSpinning()
                            getAssignedDDListApi(shop_id, billing)
                        })
        )
    }

    private fun getAssignedDDListApi(shop_id: String?, billing: BillingEntity) {

        if (BaseActivity.isApiInitiated)
            return

        BaseActivity.isApiInitiated = true

        val repository = AssignToDDListRepoProvider.provideAssignDDListRepository()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
                repository.assignToDDList(Pref.profile_state)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as AssignToDDListResponseModel
                            if (response.status == NetworkConstant.SUCCESS) {
                                val list = response.assigned_to_dd_list

                                if (list != null && list.isNotEmpty()) {

                                    doAsync {

                                        val assignDDList = AppDatabase.getDBInstance()?.ddListDao()?.getAll()
                                        if (assignDDList != null)
                                            AppDatabase.getDBInstance()?.ddListDao()?.delete()

                                        for (i in list.indices) {
                                            val assignToDD = AssignToDDEntity()
                                            assignToDD.dd_id = list[i].assigned_to_dd_id
                                            assignToDD.dd_name = list[i].assigned_to_dd_authorizer_name
                                            assignToDD.dd_phn_no = list[i].phn_no
                                            assignToDD.pp_id = list[i].assigned_to_pp_id
                                            assignToDD.type_id = list[i].type_id
                                            assignToDD.dd_latitude = list[i].dd_latitude
                                            assignToDD.dd_longitude = list[i].dd_longitude
                                            AppDatabase.getDBInstance()?.ddListDao()?.insert(assignToDD)
                                        }

                                        uiThread {
                                            BaseActivity.isApiInitiated = false
                                            progress_wheel.stopSpinning()
                                            getAssignedToShopApi(shop_id, billing)
                                        }
                                    }
                                } else {
                                    BaseActivity.isApiInitiated = false
                                    progress_wheel.stopSpinning()
                                    getAssignedToShopApi(shop_id, billing)
                                }
                            } else {
                                BaseActivity.isApiInitiated = false
                                progress_wheel.stopSpinning()
                                getAssignedToShopApi(shop_id, billing)
                            }

                        }, { error ->
                            error.printStackTrace()
                            BaseActivity.isApiInitiated = false
                            progress_wheel.stopSpinning()
                            getAssignedToShopApi(shop_id, billing)
                        })
        )
    }

    private fun getAssignedToShopApi(shop_id: String?, billing: BillingEntity) {
        if (BaseActivity.isApiInitiated)
            return

        BaseActivity.isApiInitiated = true

        val repository = TypeListRepoProvider.provideTypeListRepository()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
                repository.assignToShopList(Pref.profile_state)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as AssignedToShopListResponseModel
                            if (response.status == NetworkConstant.SUCCESS) {
                                val list = response.shop_list

                                AppDatabase.getDBInstance()?.assignToShopDao()?.delete()

                                doAsync {
                                    list?.forEach {
                                        val shop = AssignToShopEntity()
                                        AppDatabase.getDBInstance()?.assignToShopDao()?.insert(shop.apply {
                                            assigned_to_shop_id = it.assigned_to_shop_id
                                            name = it.name
                                            phn_no = it.phn_no
                                            type_id = it.type_id
                                        })
                                    }

                                    uiThread {
                                        progress_wheel.stopSpinning()
                                        BaseActivity.isApiInitiated = false
                                        checkToCallOrderApi(billing)
                                    }
                                }
                            }
                            else {
                                progress_wheel.stopSpinning()
                                BaseActivity.isApiInitiated = false
                                checkToCallOrderApi(billing)
                            }

                        }, { error ->
                            progress_wheel.stopSpinning()
                            error.printStackTrace()
                            BaseActivity.isApiInitiated = false
                            checkToCallOrderApi(billing)
                        })
        )
    }


    private fun checkToCallOrderApi(billing: BillingEntity) {
        if (!order?.isUploaded!!) {
            syncAddOrderApi(order?.shop_id, order?.order_id, order?.amount!!, order?.date!!, order?.remarks, order?.signature, order?.order_lat,
                    order?.order_long, billing, order)
        } else {
            callAddBillApi(billing)
        }
    }


    private fun syncAddOrderApi(shop_id: String?, order_id: String?, amount: String, date: String, remarks: String?, signature: String?,
                                orderLat: String?, orderLong: String?, billing: BillingEntity, orderListDetails: OrderDetailsListEntity?) {

        if (BaseActivity.isApiInitiated)
            return

        BaseActivity.isApiInitiated = true

        val addOrder = AddOrderInputParamsModel()
        addOrder.collection = ""
        addOrder.description = ""
        addOrder.order_amount = amount
        addOrder.order_date = date //AppUtils.getCurrentDateFormatInTa(date)
        addOrder.order_id = order_id
        addOrder.shop_id = shop_id
        addOrder.session_token = Pref.session_token
        addOrder.user_id = Pref.user_id
        addOrder.latitude = orderLat
        addOrder.longitude = orderLong

        if (remarks != null)
            addOrder.remarks = remarks
        else
            addOrder.remarks = ""

        if (orderListDetails?.patient_name != null)
            addOrder.patient_name = orderListDetails.patient_name
        else
            addOrder.patient_name = ""

        if (orderListDetails?.patient_address != null)
            addOrder.patient_address = orderListDetails.patient_address
        else
            addOrder.patient_address = ""

        if (orderListDetails?.patient_no != null)
            addOrder.patient_no = orderListDetails.patient_no
        else
            addOrder.patient_no = ""

        val shopActivity = AppDatabase.getDBInstance()!!.shopActivityDao().getShopActivityForId(shop_id!!)
        if (shopActivity != null) {
            if (shopActivity.isVisited && !shopActivity.isDurationCalculated && shopActivity.date == AppUtils.getCurrentDateForShopActi()) {
                val shopDetail = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(shop_id)

                if (!TextUtils.isEmpty(shopDetail.address))
                    addOrder.address = shopDetail.address
                else
                    addOrder.address = ""
            } else {
                if (!TextUtils.isEmpty(orderLat) && !TextUtils.isEmpty(orderLong))
                    addOrder.address = LocationWizard.getLocationName(mContext, orderLat!!.toDouble(), orderLong!!.toDouble())
                else
                    addOrder.address = ""
            }
        } else {
            if (!TextUtils.isEmpty(orderLat) && !TextUtils.isEmpty(orderLong))
                addOrder.address = LocationWizard.getLocationName(mContext, orderLat!!.toDouble(), orderLong!!.toDouble())
            else
                addOrder.address = ""
        }

        val list = AppDatabase.getDBInstance()!!.orderProductListDao().getDataAccordingToShopAndOrderId(order_id!!, shop_id!!)
        val productList = java.util.ArrayList<AddOrderInputProductList>()

        for (i in list.indices) {
            val product = AddOrderInputProductList()
            product.id = list[i].product_id
            product.qty = list[i].qty
            product.rate = list[i].rate
            product.total_price = list[i].total_price
            product.product_name = list[i].product_name
            productList.add(product)
        }

        addOrder.product_list = productList

        progress_wheel.spin()

        if (TextUtils.isEmpty(signature)) {
            val repository = AddOrderRepoProvider.provideAddOrderRepository()
            BaseActivity.compositeDisposable.add(
                    repository.addNewOrder(addOrder)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ result ->
                                val orderList = result as BaseResponse
                                progress_wheel.stopSpinning()
                                BaseActivity.isApiInitiated = false
                                if (orderList.status == NetworkConstant.SUCCESS) {
                                    AppDatabase.getDBInstance()!!.orderDetailsListDao().updateIsUploaded(true, order_id)
                                    callAddBillApi(billing)
                                } else
                                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.unable_to_sync))

                            }, { error ->
                                error.printStackTrace()
                                BaseActivity.isApiInitiated = false
                                progress_wheel.stopSpinning()
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.unable_to_sync))
                            })
            )
        }
        else {
            val repository = AddOrderRepoProvider.provideAddOrderImageRepository()
            BaseActivity.compositeDisposable.add(
                    repository.addNewOrder(addOrder, signature!!, mContext)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ result ->
                                val orderList = result as BaseResponse
                                progress_wheel.stopSpinning()
                                BaseActivity.isApiInitiated = false
                                if (orderList.status == NetworkConstant.SUCCESS) {
                                    AppDatabase.getDBInstance()!!.orderDetailsListDao().updateIsUploaded(true, order_id)
                                    callAddBillApi(billing)
                                } else
                                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.unable_to_sync))

                            }, { error ->
                                error.printStackTrace()
                                BaseActivity.isApiInitiated = false
                                progress_wheel.stopSpinning()
                                (mContext as DashboardActivity).showSnackMessage(getString(R.string.unable_to_sync))
                            })
            )
        }
    }


    private fun callAddBillApi(billing: BillingEntity) {

        if (BaseActivity.isApiInitiated)
            return

        BaseActivity.isApiInitiated = true

        val addBill = AddBillingInputParamsModel()
        addBill.bill_id = billing.bill_id
        addBill.invoice_amount = billing.invoice_amount
        addBill.invoice_date = billing.invoice_date
        addBill.invoice_no = billing.invoice_no
        addBill.remarks = billing.remarks
        addBill.order_id = billing.order_id
        addBill.session_token = Pref.session_token!!
        addBill.user_id = Pref.user_id!!
        addBill.patient_no = billing.patient_no
        addBill.patient_name = billing.patient_name
        addBill.patient_address = billing.patient_address

        val list = AppDatabase.getDBInstance()!!.billProductDao().getDataAccordingToBillId(addBill.bill_id)
        val productList = ArrayList<AddOrderInputProductList>()

        for (i in list.indices) {
            val product = AddOrderInputProductList()
            product.id = list[i].product_id
            product.qty = list[i].qty
            product.rate = list[i].rate
            product.total_price = list[i].total_price
            product.product_name = list[i].product_name
            productList.add(product)
        }

        addBill.product_list = productList

        XLog.d("======SYNC BILLING DETAILS INPUT PARAMS (BILLING LIST)======")
        XLog.d("USER ID===> " + addBill.user_id)
        XLog.d("SESSION ID====> " + addBill.session_token)
        XLog.d("BILL ID====> " + addBill.bill_id)
        XLog.d("INVOICE NO.====> " + addBill.invoice_no)
        XLog.d("INVOICE DATE====> " + addBill.invoice_date)
        XLog.d("INVOICE AMOUNT====> " + addBill.invoice_amount)
        XLog.d("REMARKS====> " + addBill.remarks)
        XLog.d("ORDER ID====> " + addBill.order_id)

        try {
            XLog.d("PATIENT NO====> " + addBill.patient_no)
            XLog.d("PATIENT NAME====> " + addBill.patient_name)
            XLog.d("PATIENT ADDRESS====> " + addBill.patient_address)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }

        if (!TextUtils.isEmpty(billing.attachment))
            XLog.d("ATTACHMENT=======> " + billing.attachment)

        XLog.d("PRODUCT LIST SIZE====> " + addBill.product_list?.size)
        XLog.d("==============================================================")

        if (!TextUtils.isEmpty(billing.attachment)) {
            val repository = AddBillingRepoProvider.addBillImageRepository()
            progress_wheel.spin()
            BaseActivity.compositeDisposable.add(
                    repository.addBillingDetailsMultipart(addBill, billing.attachment, mContext)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ result ->
                                val baseResponse = result as BaseResponse
                                XLog.d("SYNC BILLING DETAILS : " + "RESPONSE : " + baseResponse.status + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ", MESSAGE : " + baseResponse.message)
                                BaseActivity.isApiInitiated = false

                                if (baseResponse.status == NetworkConstant.SUCCESS) {
                                    AppDatabase.getDBInstance()!!.billingDao().updateIsUploadedBillingIdWise(true, addBill.bill_id)
                                    updateItem()
                                    (mContext as DashboardActivity).showSnackMessage(baseResponse.message!!)
                                } else
                                    (mContext as DashboardActivity).showSnackMessage("Unable to sync billing")

                                progress_wheel.stopSpinning()

                            }, { error ->
                                XLog.d("SYNC BILLING DETAILS : " + "ERROR : " + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ", MESSAGE : " + error.localizedMessage)
                                error.printStackTrace()
                                BaseActivity.isApiInitiated = false
                                progress_wheel.stopSpinning()
                                (mContext as DashboardActivity).showSnackMessage("Unable to sync billing")
                            })
            )
        } else {
            val repository = AddBillingRepoProvider.addBillRepository()
            progress_wheel.spin()
            BaseActivity.compositeDisposable.add(
                    repository.addBillingDetails(addBill)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ result ->
                                val baseResponse = result as BaseResponse
                                XLog.d("SYNC BILLING DETAILS : " + "RESPONSE : " + baseResponse.status + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ", MESSAGE : " + baseResponse.message)
                                BaseActivity.isApiInitiated = false

                                if (baseResponse.status == NetworkConstant.SUCCESS) {
                                    AppDatabase.getDBInstance()!!.billingDao().updateIsUploadedBillingIdWise(true, addBill.bill_id)
                                    updateItem()
                                    (mContext as DashboardActivity).showSnackMessage(baseResponse.message!!)
                                } else
                                    (mContext as DashboardActivity).showSnackMessage("Unable to sync billing")

                                progress_wheel.stopSpinning()

                            }, { error ->
                                XLog.d("SYNC BILLING DETAILS : " + "ERROR : " + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ", MESSAGE : " + error.localizedMessage)
                                error.printStackTrace()
                                BaseActivity.isApiInitiated = false
                                progress_wheel.stopSpinning()
                                (mContext as DashboardActivity).showSnackMessage("Unable to sync billing")
                            })
            )
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

            R.id.fab -> {
                if (!Pref.isAddAttendence)
                    (context as DashboardActivity).checkToShowAddAttendanceAlert()
                else
                    (mContext as DashboardActivity).loadFragment(FragType.AddBillingFragment, true, order!!)
            }
        }
    }

    fun updateItem() {
        val list = AppDatabase.getDBInstance()!!.billingDao().getDataOrderIdWise(order?.order_id!!) as ArrayList
        if (list != null && list.isNotEmpty())
            initAdapter(list)
        else {
            tv_no_data_available.visibility = View.VISIBLE
        }
    }
}