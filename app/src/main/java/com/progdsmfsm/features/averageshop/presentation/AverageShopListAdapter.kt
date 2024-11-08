package com.progdsmfsm.features.averageshop.presentation

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.progdsmfsm.R
import com.progdsmfsm.app.AppDatabase
import com.progdsmfsm.app.Pref
import com.progdsmfsm.app.domain.ShopActivityEntity
import com.progdsmfsm.app.types.FragType
import com.progdsmfsm.app.uiaction.IntentActionable
import com.progdsmfsm.app.utils.AppUtils
import com.progdsmfsm.features.dashboard.presentation.DashboardActivity
import kotlinx.android.synthetic.main.inflate_avg_shop_item.view.*
import kotlinx.android.synthetic.main.inflate_registered_shops.view.*
import kotlinx.android.synthetic.main.inflate_registered_shops.view.add_order_ll
import kotlinx.android.synthetic.main.inflate_registered_shops.view.add_quot_ll
import kotlinx.android.synthetic.main.inflate_registered_shops.view.call_ll
import kotlinx.android.synthetic.main.inflate_registered_shops.view.call_view
import kotlinx.android.synthetic.main.inflate_registered_shops.view.direction_ll
import kotlinx.android.synthetic.main.inflate_registered_shops.view.direction_view
import kotlinx.android.synthetic.main.inflate_registered_shops.view.last_visited_RL
import kotlinx.android.synthetic.main.inflate_registered_shops.view.last_visited_date_TV
import kotlinx.android.synthetic.main.inflate_registered_shops.view.ll_activity
import kotlinx.android.synthetic.main.inflate_registered_shops.view.ll_shop_code
import kotlinx.android.synthetic.main.inflate_registered_shops.view.ll_stage
import kotlinx.android.synthetic.main.inflate_registered_shops.view.ll_stock
import kotlinx.android.synthetic.main.inflate_registered_shops.view.menu_IV
import kotlinx.android.synthetic.main.inflate_registered_shops.view.myshop_address_TV
import kotlinx.android.synthetic.main.inflate_registered_shops.view.myshop_name_TV
import kotlinx.android.synthetic.main.inflate_registered_shops.view.order_RL
import kotlinx.android.synthetic.main.inflate_registered_shops.view.order_view
import kotlinx.android.synthetic.main.inflate_registered_shops.view.shop_IV
import kotlinx.android.synthetic.main.inflate_registered_shops.view.stock_view
import kotlinx.android.synthetic.main.inflate_registered_shops.view.sync_icon
import kotlinx.android.synthetic.main.inflate_registered_shops.view.total_visited_RL
import kotlinx.android.synthetic.main.inflate_registered_shops.view.tv_shop_code
import kotlinx.android.synthetic.main.inflate_registered_shops.view.tv_shop_contact_no
import kotlinx.android.synthetic.main.inflate_registered_shops.view.update_address_TV
import kotlinx.android.synthetic.main.inflate_registered_shops.view.update_stage_TV
import java.util.*

/**
 * Created by Pratishruti on 15-11-2017.
 */
class AverageShopListAdapter(context: Context, userLocationDataEntity: List<ShopActivityEntity>, val listener: AverageShopListClickListener) : RecyclerView.Adapter<AverageShopListAdapter.MyViewHolder>() {
    private val layoutInflater: LayoutInflater
    private var context: Context
    var userLocationDataEntity: List<ShopActivityEntity> = userLocationDataEntity

    init {
        layoutInflater = LayoutInflater.from(context)
        this.context = context
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindItems(context, userLocationDataEntity, listener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = layoutInflater.inflate(R.layout.inflate_avg_shop_item, parent, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        return userLocationDataEntity.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(context: Context, userLocationDataEntity: List<ShopActivityEntity>, listener: AverageShopListClickListener) {

            try {

                //itemView.iconWrapper_rl.visibility = View.GONE
                itemView.call_ll.visibility = View.GONE
                itemView.call_view.visibility = View.GONE
                itemView.direction_ll.visibility = View.GONE
                itemView.direction_view.visibility = View.GONE
                itemView.order_RL.visibility = View.GONE
                itemView.total_visited_RL.visibility = View.GONE
                itemView.last_visited_RL.visibility = View.GONE
                itemView.ll_stage.visibility = View.GONE
                itemView.update_stage_TV.visibility = View.GONE

                val shop = AppDatabase.getDBInstance()?.addShopEntryDao()?.getShopByIdN(userLocationDataEntity[adapterPosition].shopid)
                if (Pref.willStockShow) {
                    if (Pref.isStockAvailableForAll) {
                        itemView.ll_stock.visibility = View.VISIBLE
                        itemView.stock_view.visibility = View.VISIBLE

                        if (Pref.isOrderShow) {
                            itemView.add_order_ll.visibility = View.VISIBLE
                            itemView.stock_view.visibility = View.VISIBLE

                            if (Pref.isQuotationShow)
                                itemView.order_view.visibility = View.VISIBLE
                            else
                                itemView.order_view.visibility = View.GONE

                        } else {
                            itemView.add_order_ll.visibility = View.GONE
                            itemView.stock_view.visibility = View.GONE
                            itemView.order_view.visibility = View.GONE
                        }

                    } else {
                        if (!TextUtils.isEmpty(shop?.type) && shop?.type == "4") {
                            itemView.ll_stock.visibility = View.VISIBLE
                            itemView.stock_view.visibility = View.VISIBLE

                            if (Pref.isOrderShow) {
                                itemView.add_order_ll.visibility = View.VISIBLE
                                itemView.stock_view.visibility = View.VISIBLE

                                if (Pref.isQuotationShow)
                                    itemView.order_view.visibility = View.VISIBLE
                                else
                                    itemView.order_view.visibility = View.GONE

                            } else {
                                itemView.add_order_ll.visibility = View.GONE
                                itemView.stock_view.visibility = View.GONE
                                itemView.order_view.visibility = View.GONE
                            }

                        } else {
                            itemView.ll_stock.visibility = View.GONE
                            itemView.stock_view.visibility = View.GONE

                            if (Pref.isOrderShow) {
                                itemView.add_order_ll.visibility = View.VISIBLE

                                if (Pref.isQuotationShow)
                                    itemView.order_view.visibility = View.VISIBLE
                                else
                                    itemView.order_view.visibility = View.GONE
                            } else {
                                itemView.add_order_ll.visibility = View.GONE
                                itemView.order_view.visibility = View.GONE
                            }
                        }
                    }
                }
                else {
                    itemView.ll_stock.visibility = View.GONE
                    itemView.stock_view.visibility = View.GONE

                    if (Pref.isOrderShow) {
                        itemView.add_order_ll.visibility = View.VISIBLE

                        if (Pref.isQuotationShow)
                            itemView.order_view.visibility = View.VISIBLE
                        else
                            itemView.order_view.visibility = View.GONE
                    } else {
                        itemView.add_order_ll.visibility = View.GONE
                        itemView.order_view.visibility = View.GONE
                    }

                }

//            Picasso.with(context).load(userLocationDataEntity[adapterPosition].shopImageLocalPath).into(itemView.shop_image_IV);
                itemView.myshop_name_TV.text = userLocationDataEntity[adapterPosition].shop_name
                itemView.myshop_address_TV.text = userLocationDataEntity[adapterPosition].shop_address

                if (shop != null && shop.isUploaded) {

                    if (userLocationDataEntity[adapterPosition].isUploaded && userLocationDataEntity[adapterPosition].isDurationCalculated) {
                        itemView.sync_icon.visibility = View.VISIBLE

                        val list = AppDatabase.getDBInstance()!!.shopVisitImageDao().getTodaysUnSyncedListAccordingToShopId(false, userLocationDataEntity[adapterPosition].shopid!!,
                                userLocationDataEntity[adapterPosition].visited_date!!)

                        if (list != null && list.isNotEmpty()) {
                            itemView.sync_icon.setImageResource(R.drawable.ic_registered_shop_not_sync)
                            itemView.sync_icon.setOnClickListener(View.OnClickListener {
                                listener.onSyncClick(adapterPosition)
                            })
                        } else {
                            val list_ = AppDatabase.getDBInstance()!!.shopVisitAudioDao().getTodaysUnSyncedListAccordingToShopId(false, userLocationDataEntity[adapterPosition].shopid!!,
                                    userLocationDataEntity[adapterPosition].visited_date!!)

                            if (list_ != null && list_.isNotEmpty()) {
                                itemView.sync_icon.setImageResource(R.drawable.ic_registered_shop_not_sync)
                                itemView.sync_icon.setOnClickListener(View.OnClickListener {
                                    listener.onSyncClick(adapterPosition)
                                })
                            }
                            else
                                itemView.sync_icon.setImageResource(R.drawable.ic_dashboard_green_tick_new)
                        }

                    } else {
                        if (userLocationDataEntity[adapterPosition].isDurationCalculated && !userLocationDataEntity[adapterPosition].isUploaded) {
                            itemView.sync_icon.visibility = View.VISIBLE
                            itemView.sync_icon.setImageResource(R.drawable.ic_registered_shop_not_sync)
                            itemView.sync_icon.setOnClickListener(View.OnClickListener {
                                listener.onSyncClick(adapterPosition)
                            })
                        } else
                            itemView.sync_icon.visibility = View.GONE
                    }
                }
                else {
                    if (userLocationDataEntity[adapterPosition].isDurationCalculated && !userLocationDataEntity[adapterPosition].isUploaded) {
                        itemView.sync_icon.visibility = View.VISIBLE
                        itemView.sync_icon.setImageResource(R.drawable.ic_registered_shop_not_sync)
                        itemView.sync_icon.setOnClickListener(View.OnClickListener {
                            listener.onSyncClick(adapterPosition)
                        })
                    } else
                        itemView.sync_icon.visibility = View.GONE
                }

                itemView.update_address_TV.visibility = View.GONE

//            if (userLocationDataEntity[adapterPosition].lastVisitedDate == "") {
//                var listnew = AppDatabase.getDBInstance()!!.addShopEntryDao().getVisitedShopListByName(userLocationDataEntity[adapterPosition].shop_name, true)
//                userLocationDataEntity[adapterPosition].totalVisitCount = listnew.size.toString()
//                userLocationDataEntity[adapterPosition].lastVisitedDate = listnew[listnew.size - 1].visitDate
//            }

//            itemView.total_visited_value_TV.setText(userLocationDataEntity[adapterPosition])
                itemView.last_visited_date_TV.text = userLocationDataEntity[adapterPosition].visited_date

                val drawable = TextDrawable.builder()
                        .buildRoundRect(userLocationDataEntity[adapterPosition].shop_name!!.toUpperCase().take(1), ColorGenerator.MATERIAL.randomColor, 120)

                itemView.shop_IV.setImageDrawable(drawable)

                itemView.menu_IV.findViewById<ImageView>(R.id.menu_IV).setOnClickListener(View.OnClickListener {
                    listener.OnMenuClick(adapterPosition, itemView.menu_IV)
                })
//
                itemView.setOnClickListener {
                    listener.OnItemClick(adapterPosition)
                }

                itemView.tv_shop_contact_no.text = shop?.ownerName + " (${shop?.ownerContactNumber})"

                itemView.tv_shop_contact_no.setOnClickListener {
                    IntentActionable.initiatePhoneCall(context, shop?.ownerContactNumber)
                }

                itemView.ll_stock.setOnClickListener {
                    (context as DashboardActivity).loadFragment(FragType.StockListFragment, true, shop!!)
                }

                itemView.add_order_ll.setOnClickListener {
                    (context as DashboardActivity).loadFragment(FragType.ViewAllOrderListFragment, true, shop!!)
                }

                //itemView.ll_activity.visibility=View.GONE
                //itemView.activity_vieww.visibility=View.GONE

                itemView.ll_activity.setOnClickListener {
                    when (shop?.type) {
                        "7" -> {
                            (context as DashboardActivity).isFromShop = true
                            (context as DashboardActivity).loadFragment(FragType.ChemistActivityListFragment, true, shop)
                        }
                        "8" -> {
                            (context as DashboardActivity).isFromShop = true
                            (context as DashboardActivity).loadFragment(FragType.DoctorActivityListFragment, true, shop)
                        }
                        else -> {
                            (context as DashboardActivity).isFromMenu = false
                            (context as DashboardActivity).loadFragment(FragType.AddActivityFragment, true, shop!!)
                        }
                    }
                }

                if (Pref.isEntityCodeVisible) {
                    if (!TextUtils.isEmpty(shop?.entity_code)) {
                        itemView.ll_shop_code.visibility = View.VISIBLE
                        itemView.tv_shop_code.text = shop?.entity_code
                    } else
                        itemView.ll_shop_code.visibility = View.GONE
                } else
                    itemView.ll_shop_code.visibility = View.GONE

                if (Pref.isQuotationShow) {
                    itemView.add_quot_ll.visibility = View.VISIBLE
                    //itemView.order_view.visibility = View.VISIBLE
                } else {
                    itemView.add_quot_ll.visibility = View.GONE
                    //itemView.order_view.visibility = View.GONE
                }

                itemView.add_quot_ll.setOnClickListener {
                    (context as DashboardActivity).isBack = true
                    (context as DashboardActivity).loadFragment(FragType.QuotationListFragment, true, shop?.shop_id!!)
                }

                if (!TextUtils.isEmpty(userLocationDataEntity[adapterPosition].device_model))
                    itemView.tv_device_model.text = userLocationDataEntity[adapterPosition].device_model

                if (!TextUtils.isEmpty(userLocationDataEntity[adapterPosition].android_version))
                    itemView.tv_android_version.text = userLocationDataEntity[adapterPosition].android_version

                if (!TextUtils.isEmpty(userLocationDataEntity[adapterPosition].battery))
                    itemView.tv_battery.text = userLocationDataEntity[adapterPosition].battery + "%"

                if (!TextUtils.isEmpty(userLocationDataEntity[adapterPosition].net_status))
                    itemView.tv_net_status.text = userLocationDataEntity[adapterPosition].net_status

                if (!TextUtils.isEmpty(userLocationDataEntity[adapterPosition].net_type))
                    itemView.tv_net_type.text = userLocationDataEntity[adapterPosition].net_type



                //////////////////

                var currentViewSt=AppDatabase.getDBInstance()?.shopTypeStockViewStatusDao()?.getShopCurrentStockViewStatus(shop?.type!!)
                var competitorViewSt=AppDatabase.getDBInstance()?.shopTypeStockViewStatusDao()?.getShopCompetitorStockViewStatus(shop?.type!!)


                if(AppUtils.getSharedPreferencesCurrentStock(context)){
                    if(AppUtils.getSharedPreferencesCurrentStockApplicableForAll(context)){
                        itemView.ll_current_stock.visibility=View.VISIBLE
                        itemView.current_stock_view.visibility=View.VISIBLE
                    }else{
                        //if(shop?.type?.toInt() == 1 || shop?.type?.toInt() == 3){
                        if(currentViewSt==1){
                            itemView.ll_current_stock.visibility=View.VISIBLE
                            itemView.current_stock_view.visibility=View.VISIBLE
                        }
                    }
                }
                if(AppUtils.getSharedPreferencesIscompetitorStockRequired(context)){
                    if(!AppUtils.getSharedPreferencesIsCompetitorStockforParty(context)){
                        itemView.ll_competetor_stock.visibility=View.VISIBLE
                        itemView.competetor_stock_view.visibility=View.VISIBLE
                    }else{
                        //if(shop?.type?.toInt() == 1 || shop?.type?.toInt() == 3){
                        if(competitorViewSt==1){
                            itemView.ll_competetor_stock.visibility=View.VISIBLE
                            itemView.competetor_stock_view.visibility=View.VISIBLE
                        }
                    }
                }



                itemView.ll_current_stock.setOnClickListener{
                    (context as DashboardActivity).loadFragment(FragType.UpdateShopStockFragment, true, userLocationDataEntity[adapterPosition].shopid!!)
                }
                itemView.ll_competetor_stock.setOnClickListener{
                    (context as DashboardActivity).loadFragment(FragType.CompetetorStockFragment, true, userLocationDataEntity[adapterPosition].shopid!!)
                }

                if (Pref.willActivityShow) {
                    itemView.ll_activity.visibility = View.VISIBLE
                    itemView.activity_vieww.visibility = View.VISIBLE
                }else{
                    itemView.ll_activity.visibility = View.GONE
                    itemView.activity_vieww.visibility = View.GONE
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    open fun updateList(locationDataEntity: List<ShopActivityEntity>) {
        Collections.reverse(locationDataEntity)
        userLocationDataEntity = locationDataEntity
        notifyDataSetChanged()
    }
}