package com.progdsmfsm.features.nearbyshops.diaog

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.progdsmfsm.R
import com.progdsmfsm.app.AppDatabase
import com.progdsmfsm.app.NetworkConstant
import com.progdsmfsm.app.Pref
import com.progdsmfsm.app.domain.AddShopDBModelEntity
import com.progdsmfsm.app.utils.AppUtils
import com.progdsmfsm.app.utils.Toaster
import com.progdsmfsm.base.presentation.BaseActivity
import com.progdsmfsm.features.addshop.model.AddShopRequestData
import com.progdsmfsm.features.addshop.model.AddShopResponse
import com.progdsmfsm.features.dashboard.presentation.DashboardActivity
import com.progdsmfsm.features.nearbyshops.presentation.UpdateShopStatusDialog
import com.progdsmfsm.features.shopdetail.presentation.api.EditShopRepoProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.row_inactive_shop_list.view.tv_row_inactive_shop_name
import kotlinx.android.synthetic.main.row_inactive_shop_list.view.tv_row_inactive_shop_update_status
import timber.log.Timber

class AdapterInactiveShop(var mContext: Context, var mList:ArrayList<AddShopDBModelEntity>,var listner:OnClick):
RecyclerView.Adapter<AdapterInactiveShop.AdapterInactiveViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterInactiveViewHolder {
        var v = LayoutInflater.from(mContext).inflate(R.layout.row_inactive_shop_list,parent,false)
        return AdapterInactiveViewHolder(v)
    }

    override fun onBindViewHolder(holder: AdapterInactiveViewHolder, position: Int) {
        holder.bindItems()
    }

    override fun getItemCount(): Int {
       return mList.size
    }

    inner class AdapterInactiveViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        fun bindItems(){

            itemView.tv_row_inactive_shop_name.text = mList.get(adapterPosition).shopName

            itemView.tv_row_inactive_shop_update_status.setOnClickListener {
                    UpdateShopStatusDialog.getInstance(mList.get(adapterPosition).shopName!!, "Cancel", "Confirm", true,"",mList.get(adapterPosition).user_id.toString()!!,
                        object : UpdateShopStatusDialog.OnDSButtonClickListener {
                            override fun onLeftClick() {

                            }
                            override fun onRightClick(status: String) {
                                if(AppUtils.isOnline(mContext)){
                                    if(!status.equals("")){
                                        if(status.equals("Inactive")){
                                            AppDatabase.getDBInstance()!!.addShopEntryDao().updateShopStatus(mList.get(adapterPosition).shop_id,"0")
                                            if(AppUtils.isOnline(mContext))
                                                convertToReqAndApiCallForShopStatus(AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(mList.get(adapterPosition).shop_id!!))
                                        }
                                        if(status.equals("Active")){
                                            AppDatabase.getDBInstance()!!.addShopEntryDao().updateShopStatus(mList.get(adapterPosition).shop_id,"1")
                                            if(AppUtils.isOnline(mContext))
                                                convertToReqAndApiCallForShopStatus(AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(mList.get(adapterPosition).shop_id!!))
                                        }
                                    }
                                }else{
                                    Toaster.msgShort(mContext,"No network found.")
                                }
                            }
                        }).show((mContext as DashboardActivity).supportFragmentManager, "")
                }
        }
    }

    interface OnClick{
        fun onItemClick(shop_id:String)
    }

    private fun convertToReqAndApiCallForShopStatus(addShopData: AddShopDBModelEntity) {
        if (Pref.user_id == null || Pref.user_id == "" || Pref.user_id == " ") {
            (mContext as DashboardActivity).showSnackMessage("Please login again")
            BaseActivity.isApiInitiated = false
            return
        }
        val addShopReqData = AddShopRequestData()
        addShopReqData.session_token = Pref.session_token
        addShopReqData.address = addShopData.address
        addShopReqData.actual_address = addShopData.address
        addShopReqData.owner_contact_no = addShopData.ownerContactNumber
        addShopReqData.owner_email = addShopData.ownerEmailId
        addShopReqData.owner_name = addShopData.ownerName
        addShopReqData.pin_code = addShopData.pinCode
        addShopReqData.shop_lat = addShopData.shopLat.toString()
        addShopReqData.shop_long = addShopData.shopLong.toString()
        addShopReqData.shop_name = addShopData.shopName.toString()
        addShopReqData.shop_id = addShopData.shop_id
        addShopReqData.added_date = ""
        addShopReqData.user_id = Pref.user_id
        addShopReqData.type = addShopData.type
        addShopReqData.assigned_to_pp_id = addShopData.assigned_to_pp_id
        addShopReqData.assigned_to_dd_id = addShopData.assigned_to_dd_id
        addShopReqData.amount = addShopData.amount
        addShopReqData.area_id = addShopData.area_id

        addShopReqData.model_id = addShopData.model_id
        addShopReqData.primary_app_id = addShopData.primary_app_id
        addShopReqData.secondary_app_id = addShopData.secondary_app_id
        addShopReqData.lead_id = addShopData.lead_id
        addShopReqData.stage_id = addShopData.stage_id
        addShopReqData.funnel_stage_id = addShopData.funnel_stage_id
        addShopReqData.booking_amount = addShopData.booking_amount
        addShopReqData.type_id = addShopData.type_id

        if (!TextUtils.isEmpty(addShopData.dateOfBirth))
            addShopReqData.dob =
                AppUtils.changeAttendanceDateFormatToCurrent(addShopData.dateOfBirth)

        if (!TextUtils.isEmpty(addShopData.dateOfAniversary))
            addShopReqData.date_aniversary =
                AppUtils.changeAttendanceDateFormatToCurrent(addShopData.dateOfAniversary)

        addShopReqData.director_name = addShopData.director_name
        addShopReqData.key_person_name = addShopData.person_name
        addShopReqData.phone_no = addShopData.person_no

        if (!TextUtils.isEmpty(addShopData.family_member_dob))
            addShopReqData.family_member_dob =
                AppUtils.changeAttendanceDateFormatToCurrent(addShopData.family_member_dob)

        if (!TextUtils.isEmpty(addShopData.add_dob))
            addShopReqData.addtional_dob =
                AppUtils.changeAttendanceDateFormatToCurrent(addShopData.add_dob)

        if (!TextUtils.isEmpty(addShopData.add_doa))
            addShopReqData.addtional_doa =
                AppUtils.changeAttendanceDateFormatToCurrent(addShopData.add_doa)

        addShopReqData.specialization = addShopData.specialization
        addShopReqData.category = addShopData.category
        addShopReqData.doc_address = addShopData.doc_address
        addShopReqData.doc_pincode = addShopData.doc_pincode
        addShopReqData.is_chamber_same_headquarter = addShopData.chamber_status.toString()
        addShopReqData.is_chamber_same_headquarter_remarks = addShopData.remarks
        addShopReqData.chemist_name = addShopData.chemist_name
        addShopReqData.chemist_address = addShopData.chemist_address
        addShopReqData.chemist_pincode = addShopData.chemist_pincode
        addShopReqData.assistant_contact_no = addShopData.assistant_no
        addShopReqData.average_patient_per_day = addShopData.patient_count
        addShopReqData.assistant_name = addShopData.assistant_name

        if (!TextUtils.isEmpty(addShopData.doc_family_dob))
            addShopReqData.doc_family_member_dob =
                AppUtils.changeAttendanceDateFormatToCurrent(addShopData.doc_family_dob)

        if (!TextUtils.isEmpty(addShopData.assistant_dob))
            addShopReqData.assistant_dob =
                AppUtils.changeAttendanceDateFormatToCurrent(addShopData.assistant_dob)

        if (!TextUtils.isEmpty(addShopData.assistant_doa))
            addShopReqData.assistant_doa =
                AppUtils.changeAttendanceDateFormatToCurrent(addShopData.assistant_doa)

        if (!TextUtils.isEmpty(addShopData.assistant_family_dob))
            addShopReqData.assistant_family_dob =
                AppUtils.changeAttendanceDateFormatToCurrent(addShopData.assistant_family_dob)

        addShopReqData.entity_id = addShopData.entity_id
        addShopReqData.party_status_id = addShopData.party_status_id
        addShopReqData.retailer_id = addShopData.retailer_id
        addShopReqData.dealer_id = addShopData.dealer_id
        addShopReqData.beat_id = addShopData.beat_id
        addShopReqData.assigned_to_shop_id = addShopData.assigned_to_shop_id

        if(addShopData.shopStatusUpdate.equals("0"))
            addShopReqData.shopStatusUpdate = addShopData.shopStatusUpdate
        else
            addShopReqData.shopStatusUpdate = "1"

        callEditShopApiForShopStatus(addShopReqData)
    }

    private fun callEditShopApiForShopStatus(addShopReqData: AddShopRequestData) {
        if (BaseActivity.isApiInitiated)
            return
        BaseActivity.isApiInitiated = true
        val index = addShopReqData.shop_id!!.indexOf("_")
        if (true) {
            val repository = EditShopRepoProvider.provideEditShopWithoutImageRepository()
            BaseActivity.compositeDisposable.add(
                repository.editShop(addShopReqData)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val addShopResult = result as AddShopResponse
                        Timber.d("Edit Shop : " + ", SHOP: " + addShopReqData.shop_name + ", RESPONSE:" + result.message)
                        if (addShopResult.status == NetworkConstant.SUCCESS) {
                            AppDatabase.getDBInstance()!!.addShopEntryDao().updateIsEditUploaded(1, addShopReqData.shop_id)
                            (mContext as DashboardActivity).showSnackMessage("Status updated successfully")
                        }
                        BaseActivity.isApiInitiated = false
                        listner.onItemClick(addShopReqData.shop_id!!)
                    }, { error ->
                        error.printStackTrace()
                        BaseActivity.isApiInitiated = false
                        listner.onItemClick(addShopReqData.shop_id!!)
                    })
            )
        }
    }

}