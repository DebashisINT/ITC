package com.breezefsmdsm.features.localshops

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.breezefsmdsm.R
import com.breezefsmdsm.app.AppDatabase
import com.breezefsmdsm.app.Pref
import com.breezefsmdsm.app.domain.AddShopDBModelEntity
import com.breezefsmdsm.app.domain.OrderDetailsListEntity
import com.breezefsmdsm.app.utils.AppUtils
import com.breezefsmdsm.features.location.LocationWizard
import com.breezefsmdsm.widgets.AppCustomTextView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.inflate_nearby_shops.view.*
import kotlinx.android.synthetic.main.inflate_nearby_shops.view.add_order_ll
import kotlinx.android.synthetic.main.inflate_nearby_shops.view.add_quot_ll
import kotlinx.android.synthetic.main.inflate_nearby_shops.view.call_ll
import kotlinx.android.synthetic.main.inflate_nearby_shops.view.direction_ll
import kotlinx.android.synthetic.main.inflate_nearby_shops.view.direction_view
import kotlinx.android.synthetic.main.inflate_nearby_shops.view.last_visited_date_TV
import kotlinx.android.synthetic.main.inflate_nearby_shops.view.ll_shop_code
import kotlinx.android.synthetic.main.inflate_nearby_shops.view.myshop_address_TV
import kotlinx.android.synthetic.main.inflate_nearby_shops.view.myshop_name_TV
import kotlinx.android.synthetic.main.inflate_nearby_shops.view.order_amt_p_TV
import kotlinx.android.synthetic.main.inflate_nearby_shops.view.order_view
import kotlinx.android.synthetic.main.inflate_nearby_shops.view.shop_IV
import kotlinx.android.synthetic.main.inflate_nearby_shops.view.shop_image_IV
import kotlinx.android.synthetic.main.inflate_nearby_shops.view.shop_list_LL
import kotlinx.android.synthetic.main.inflate_nearby_shops.view.total_v_TV
import kotlinx.android.synthetic.main.inflate_nearby_shops.view.total_visited_value_TV
import kotlinx.android.synthetic.main.inflate_nearby_shops.view.tv_shop_code
import kotlinx.android.synthetic.main.inflate_nearby_shops.view.tv_shop_contact_no
import kotlinx.android.synthetic.main.inflate_registered_shops.view.*

/**
 * Created by riddhi on 2/1/18.
 */
class LocalShopsListAdapter(context: Context, list: List<AddShopDBModelEntity>, val listener: LocalShopListClickListener,private val getSize: (Int) -> Unit) :
        RecyclerView.Adapter<LocalShopsListAdapter.MyViewHolder>(), Filterable {
    private val layoutInflater: LayoutInflater
    private var context: Context
    private var mList: ArrayList<AddShopDBModelEntity>

    private var tempList: ArrayList<AddShopDBModelEntity>? = null
    private var filterList: ArrayList<AddShopDBModelEntity>? = null

    init {
        layoutInflater = LayoutInflater.from(context)
        this.context = context
//        mList = list
        tempList = ArrayList()
        filterList = ArrayList()
        mList = ArrayList()

        tempList?.addAll(list)
        mList?.addAll(list as ArrayList<AddShopDBModelEntity>)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindItems(context, mList, listener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = layoutInflater.inflate(R.layout.inflate_nearby_shops, parent, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(context: Context, list: List<AddShopDBModelEntity>, listener: LocalShopListClickListener) {
            //Picasso.with(context).load(list[adapterPosition].shopImageLocalPath).into(itemView.shop_image_IV)

            if(list[adapterPosition].shopStatusUpdate.equals("0")){
                itemView.shop_list_LL.visibility=View.GONE
            }else{
                itemView.shop_list_LL.visibility=View.VISIBLE
            }

            itemView.total_v_TV.text = context.getString(R.string.total_visits)

            if (!TextUtils.isEmpty(list[adapterPosition].shopImageLocalPath)) {
                Picasso.get()
                        .load(list[adapterPosition].shopImageLocalPath)
                        .resize(100, 100)
                        .into(itemView.shop_image_IV)
            }
            itemView.myshop_name_TV.text = list[adapterPosition].shopName
            var address: String = list[adapterPosition].address + ", " + list[adapterPosition].pinCode
            itemView.myshop_address_TV.text = address

            val drawable = TextDrawable.builder()
                    .buildRoundRect(list[adapterPosition].shopName.toUpperCase().take(1), ColorGenerator.MATERIAL.randomColor, 120)

            itemView.shop_IV.setImageDrawable(drawable)

            itemView.shop_image_IV.findViewById<ImageView>(R.id.shop_image_IV).setOnClickListener(View.OnClickListener {
                //                listener.OnNearByShopsListClick(adapterPosition)
            })

            itemView.call_ll.findViewById<LinearLayout>(R.id.call_ll).setOnClickListener(View.OnClickListener {
                //                listener.callClick(adapterPosition)
            })

            itemView.direction_ll.findViewById<LinearLayout>(R.id.direction_ll).setOnClickListener(View.OnClickListener {
                //                listener.mapClick(adapterPosition)
            })

            if(Pref.isOrderShow==false){
                itemView.add_order_ll.visibility=View.GONE
                itemView.direction_view.visibility=View.GONE
            }

            itemView.add_order_ll.findViewById<LinearLayout>(R.id.add_order_ll).setOnClickListener {
                listener.onOrderClick(list[adapterPosition])
            }

            itemView.order_amt_p_TV.text = " " + context.getString(R.string.zero_order_in_value)
            itemView.total_visited_value_TV.text = " " + list[adapterPosition].totalVisitCount
            itemView.last_visited_date_TV.text = " " + list[adapterPosition].lastVisitedDate

            if (list[adapterPosition].visited) {
                itemView.visit_icon.visibility = View.VISIBLE
                itemView.visit_TV.text = "Revisited Today"
            } else {
                itemView.visit_icon.visibility = View.GONE

                /*if (Pref.isReplaceShopText)
                    itemView.visit_TV.text = "VISIT THIS CUSTOMER"
                else
                    itemView.visit_TV.text = "VISIT THIS SHOP"*/

                itemView.visit_TV.text = "Revisit Now"
            }

            itemView.shop_list_LL.setOnClickListener(View.OnClickListener {
                //                listener.OnNearByShopsListClick(adapterPosition)
            })

            itemView.visit_rl.setOnClickListener(View.OnClickListener {


                    if (Pref.IsShowDayStart && !Pref.DayStartMarked) {
                        val simpleDialog = Dialog(context)
                        simpleDialog.setCancelable(false)
                        simpleDialog.getWindow()!!
                            .setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        simpleDialog.setContentView(R.layout.dialog_message)
                        val dialogHeader =
                            simpleDialog.findViewById(R.id.dialog_message_header_TV) as AppCustomTextView
                        val dialog_yes_no_headerTV =
                            simpleDialog.findViewById(R.id.dialog_message_headerTV) as AppCustomTextView
                        dialog_yes_no_headerTV.text = AppUtils.hiFirstNameText()
                        dialogHeader.text = "Please start your day..."
                        val dialogYes =
                            simpleDialog.findViewById(R.id.tv_message_ok) as AppCustomTextView
                        dialogYes.setOnClickListener({ view ->
                            simpleDialog.cancel()
                        })
                        simpleDialog.show()
                    }else {
                        if (Pref.isMultipleVisitEnable) {
                            val list_ = AppDatabase.getDBInstance()!!.shopActivityDao()
                                .getShopForDay(
                                    list[adapterPosition].shop_id,
                                    AppUtils.getCurrentDateForShopActi()
                                )
                            /*if (list_ == null || list_.isEmpty())
                                listener.visitShop(list[adapterPosition])
                            else {
                                var isDurationCalculated = false
                                for (i in list_.indices) {
                                    isDurationCalculated = list_[i].isDurationCalculated
                                    if (!list_[i].isDurationCalculated)
                                        break

                                }

                                if (isDurationCalculated)
                                    listener.visitShop(list[adapterPosition])
                            }*/
                            listener.visitShop(list[adapterPosition])
                        } else {
                            if (!list[adapterPosition].visited)
                                listener.visitShop(list[adapterPosition])
                        }
                    }

            })

            itemView.tv_shop_contact_no.text = list[adapterPosition].ownerContactNumber

            //01-09-2021 ITC DD Pref Hide
        /*    if (!TextUtils.isEmpty(list[adapterPosition].assigned_to_dd_id)) {
                itemView.rl_dd.visibility = View.VISIBLE
                itemView.tv_pp_dd_header.text = context.getString(R.string.dist)

                val dd = AppDatabase.getDBInstance()?.ddListDao()?.getSingleValue(list[adapterPosition].assigned_to_dd_id)

                if (!TextUtils.isEmpty(dd?.dd_name) && !TextUtils.isEmpty(dd?.dd_phn_no))
                    itemView.tv_pp_dd_value.text = dd?.dd_name + " (" + dd?.dd_phn_no + ")"
                else if (!TextUtils.isEmpty(dd?.dd_name))
                    itemView.tv_pp_dd_value.text = dd?.dd_name

            } else if (!TextUtils.isEmpty(list[adapterPosition].assigned_to_pp_id)) {
                itemView.rl_dd.visibility = View.VISIBLE
                itemView.tv_pp_dd_header.text = context.getString(R.string.pp_super)

                val pp = AppDatabase.getDBInstance()?.ppListDao()?.getSingleValue(list[adapterPosition].assigned_to_pp_id)

                if (!TextUtils.isEmpty(pp?.pp_name) && !TextUtils.isEmpty(pp?.pp_phn_no))
                    itemView.tv_pp_dd_value.text = pp?.pp_name + " (" + pp?.pp_phn_no + ")"
                else if (!TextUtils.isEmpty(pp?.pp_name))
                    itemView.tv_pp_dd_value.text = pp?.pp_name

            } else {
                itemView.rl_dd.visibility = View.GONE
            }*/

          /*  try {
                val orderList = AppDatabase.getDBInstance()!!.orderDetailsListDao().getListAccordingToShopId(list[adapterPosition].shop_id) as ArrayList<OrderDetailsListEntity>

                if (orderList != null && orderList.isNotEmpty()) {
                    itemView.order_RL.visibility = View.VISIBLE

                    var amount = 0.0
                    for (i in orderList.indices) {
                        if (!TextUtils.isEmpty(orderList[i].amount))
                            amount += orderList[i].amount?.toDouble()!!
                    }
                    val finalAmount = String.format("%.2f", amount.toFloat())
                    itemView.order_amt_p_TV.text = "\u20B9 $finalAmount"
                } else
                    itemView.order_RL.visibility = View.GONE
            } catch (e: Exception) {
                e.printStackTrace()
                itemView.order_RL.visibility = View.GONE
            }*/

            itemView.call_ll.setOnClickListener {
                listener.onCallClick(list[adapterPosition])
            }

            itemView.direction_ll.setOnClickListener {
                listener.onLocationClick(list[adapterPosition])
            }

            itemView.add_quot_ll.setOnClickListener {
                listener.onQuationClick(list[adapterPosition])
            }

            if (Pref.isEntityCodeVisible) {
                if (!TextUtils.isEmpty(list[adapterPosition].entity_code)) {
                    itemView.ll_shop_code.visibility = View.VISIBLE
                    itemView.tv_shop_code.text = list[adapterPosition].entity_code
                } else
                    itemView.ll_shop_code.visibility = View.GONE
            } else
                itemView.ll_shop_code.visibility = View.GONE


            if (Pref.isQuotationShow) {
                itemView.add_quot_ll.visibility = View.VISIBLE
                itemView.order_view.visibility = View.VISIBLE
            } else {
                itemView.add_quot_ll.visibility = View.GONE
                itemView.order_view.visibility = View.GONE
            }

            val distance = LocationWizard.getDistance(list[adapterPosition].shopLat, list[adapterPosition].shopLong,
                    Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble())
            itemView.approx_distance.text = (distance * 1000).toString() + " mtr"

            if (Pref.willShowPartyStatus)
                itemView.rl_party.visibility = View.VISIBLE
            else
                itemView.rl_party.visibility = View.GONE

            if (Pref.willShowEntityTypeforShop && list[adapterPosition].type == "1")
                itemView.rl_entity.visibility = View.VISIBLE
            else
                itemView.rl_entity.visibility = View.GONE

            if (!TextUtils.isEmpty(list[adapterPosition].entity_id)) {
                val entity = AppDatabase.getDBInstance()?.entityDao()?.getSingleItem(list[adapterPosition].entity_id)
                itemView.tv_entity_value.text = entity?.name
            }
            else
                itemView.tv_entity_value.text = ""

            if (!TextUtils.isEmpty(list[adapterPosition].party_status_id)) {
                val partyStatus = AppDatabase.getDBInstance()?.partyStatusDao()?.getSingleItem(list[adapterPosition].party_status_id)
                itemView.tv_party_value.text = partyStatus?.name
            }
            else
                itemView.tv_party_value.text = ""


        }

    }

    fun updateAdapter(mlist: ArrayList<AddShopDBModelEntity>) {
        this.mList = mlist
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return SearchFilter()
    }

    inner class SearchFilter : Filter() {
        override fun performFiltering(p0: CharSequence?): FilterResults {

            //var land=AppDatabase.getDBInstance()!!.addShopEntryDao().getLandNumber(p0?.toString())

            val results = FilterResults()
            filterList?.clear()
            tempList?.indices!!
                    .filter { tempList?.get(it)?.shopName?.toLowerCase()?.contains(p0?.toString()?.toLowerCase()!!)!!
                    }
                    .forEach { filterList?.add(tempList?.get(it)!!) }


            results.values = filterList
            results.count = filterList?.size!!

            return results
        }

        override fun publishResults(p0: CharSequence?, results: FilterResults?) {

            try {
                filterList = results?.values as ArrayList<AddShopDBModelEntity>?
                mList?.clear()
                val hashSet = HashSet<String>()
                if (filterList != null) {

                    filterList?.indices!!
                            .filter { hashSet.add(filterList?.get(it)?.shopName!!) }
                            .forEach { mList?.add(filterList?.get(it)!!) }

                    getSize(mList?.size!!)

                    notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun refreshList(list: ArrayList<AddShopDBModelEntity>) {

        mList?.clear()
        mList?.addAll(list)

        tempList?.clear()
        tempList?.addAll(list)

        if (filterList == null)
            filterList = ArrayList()
        filterList?.clear()

        notifyDataSetChanged()
    }


}