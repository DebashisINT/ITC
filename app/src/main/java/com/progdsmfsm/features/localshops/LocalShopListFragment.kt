package com.progdsmfsm.features.localshops

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import timber.log.Timber
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.progdsmfsm.R
import com.progdsmfsm.app.AppDatabase
import com.progdsmfsm.app.Pref
import com.progdsmfsm.app.SearchListener
import com.progdsmfsm.app.domain.AddShopDBModelEntity
import com.progdsmfsm.app.types.FragType
import com.progdsmfsm.app.uiaction.IntentActionable
import com.progdsmfsm.app.utils.AppUtils
import com.progdsmfsm.app.utils.FTStorageUtils
import com.progdsmfsm.base.presentation.BaseFragment
import com.progdsmfsm.features.dashboard.presentation.DashboardActivity
import com.progdsmfsm.features.location.LocationWizard.Companion.NEARBY_RADIUS
import com.progdsmfsm.features.location.SingleShotLocationProvider
import java.util.*

/**
 * Created by riddhi on 2/1/18.
 */
class LocalShopListFragment : BaseFragment(), View.OnClickListener {


    private lateinit var localShopsListAdapter: LocalShopsListAdapter
    private lateinit var nearByShopsList: RecyclerView
    private lateinit var mContext: Context
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var noShopAvailable: AppCompatTextView
    private var list: MutableList<AddShopDBModelEntity> = ArrayList()
    private lateinit var floating_fab: FloatingActionMenu
    private lateinit var programFab1: FloatingActionButton
    private lateinit var programFab2: FloatingActionButton
    private lateinit var programFab3: FloatingActionButton
    private lateinit var shop_list_parent_rl: RelativeLayout
    private lateinit var progress_wheel: com.pnikosis.materialishprogress.ProgressWheel

    private lateinit var getFloatingVal: ArrayList<String>
    private val preid: Int = 100
    private var isGetLocation = -1
    private lateinit var geofenceTv: AppCompatTextView
    private lateinit var shopCountTvShow:AppCompatTextView


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_nearby_shops, container, false)
        initView(view)

        (mContext as DashboardActivity).setSearchListener(object : SearchListener {
            override fun onSearchQueryListener(query: String) {
                if (query.isBlank()) {
                    (list as ArrayList<AddShopDBModelEntity>)?.let {
                        localShopsListAdapter?.refreshList(it)
                        //tv_cust_no.text = "Total customer(s): " + it.size
                    }
                } else {
                    localShopsListAdapter?.filter?.filter(query)
                }
            }
        })
        return view
    }

    override fun updateUI(any: Any) {
        super.updateUI(any)

        nearByShopsList.visibility = View.GONE
        isGetLocation = -1

        fetchNearbyShops()
    }


    private fun initView(view: View) {
        getFloatingVal = ArrayList<String>()
        progress_wheel = view.findViewById(R.id.progress_wheel)
        progress_wheel.stopSpinning()
        nearByShopsList = view.findViewById(R.id.near_by_shops_RCV)
        noShopAvailable = view.findViewById(R.id.no_shop_tv)
        shop_list_parent_rl = view.findViewById(R.id.shop_list_parent_rl)
        geofenceTv = view.findViewById(R.id.tv_geofence_relax)
        shopCountTvShow = view.findViewById(R.id.tv_shop_count_fragment_nearby_shop)



        if(Pref.IsRestrictNearbyGeofence){
            //geofenceTv.visibility = View.VISIBLE
            //geofenceTv.text ="Geofence Relaxed :  " + Pref.GeofencingRelaxationinMeter + " mtr"
            geofenceTv.visibility = View.GONE
        }
        else{
            geofenceTv.visibility = View.GONE
        }

        shop_list_parent_rl.setOnClickListener { view ->
            floating_fab.close(true)
        }
        floating_fab = view.findViewById(R.id.floating_fab)
        floating_fab.menuIconView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_dashboard_filter_icon))
        floating_fab.menuButtonColorNormal = mContext.resources.getColor(R.color.colorAccent)
        floating_fab.menuButtonColorPressed = mContext.resources.getColor(R.color.colorPrimaryDark)
        floating_fab.menuButtonColorRipple = mContext.resources.getColor(R.color.colorPrimary)

        floating_fab.isIconAnimated = false
        floating_fab.setClosedOnTouchOutside(true)

        getFloatingVal.add("Alphabetically")
        getFloatingVal.add("Visit Date")
        getFloatingVal.add("Most Visited")
        floating_fab.visibility = View.GONE

        for (i in getFloatingVal.indices) {
            if (i == 0) {
                programFab1 = FloatingActionButton(activity)
                programFab1.buttonSize = FloatingActionButton.SIZE_MINI
                programFab1.id = preid + i
                programFab1.colorNormal = mContext.resources.getColor(R.color.colorPrimaryDark)
                programFab1.colorPressed = mContext.resources.getColor(R.color.delivery_status_green)
                programFab1.colorRipple = mContext.resources.getColor(R.color.delivery_status_green)
                programFab1.labelText = getFloatingVal[0]
                floating_fab.addMenuButton(programFab1)
                programFab1.setOnClickListener(this)

            }
            if (i == 1) {
                programFab2 = FloatingActionButton(activity)
                programFab2.buttonSize = FloatingActionButton.SIZE_MINI
                programFab2.id = preid + i
                programFab2.colorNormal = mContext.resources.getColor(R.color.colorAccent)
                programFab2.colorPressed = mContext.resources.getColor(R.color.delivery_status_green)
                programFab2.colorRipple = mContext.resources.getColor(R.color.delivery_status_green)
                programFab2.labelText = getFloatingVal[1]
                floating_fab.addMenuButton(programFab2)
                programFab2.setOnClickListener(this)

            }

            if (i == 2) {
                programFab3 = FloatingActionButton(activity)
                programFab3.buttonSize = FloatingActionButton.SIZE_MINI
                programFab3.id = preid + i
                programFab3.colorNormal = mContext.resources.getColor(R.color.colorAccent)
                programFab3.colorPressed = mContext.resources.getColor(R.color.delivery_status_green)
                programFab3.colorRipple = mContext.resources.getColor(R.color.delivery_status_green)
                programFab3.labelText = getFloatingVal[2]
                floating_fab.addMenuButton(programFab3)
                programFab3.setOnClickListener(this)


            }
            //programFab1.setImageResource(R.drawable.ic_filter);
            if (i == 0) {
                programFab1.setImageResource(R.drawable.ic_tick_float_icon)
                programFab1.colorNormal = mContext.resources.getColor(R.color.delivery_status_green)
            } else if (i == 1)
                programFab2.setImageResource(R.drawable.ic_tick_float_icon_gray)
            else
                programFab3.setImageResource(R.drawable.ic_tick_float_icon_gray)

        }

        noShopAvailable.text = "No Registered " + Pref.shopText + " Available"

        fetchNearbyShops()
    }

    fun refreshList(){
        fetchNearbyShops()
    }

    override fun onClick(v: View?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    @SuppressLint("WrongConstant")
    private fun initAdapter() {

        if (list != null && list.size > 0) {

            Timber.d("Local Shop List:== selected list size=====> " + list.size)

            val newList = ArrayList<AddShopDBModelEntity>()

            for (i in list.indices) {
                val userId = list[i].shop_id.substring(0, list[i].shop_id.indexOf("_"))
                if (userId == Pref.user_id)
                    newList.add(list[i])
            }

            Timber.d("Local Shop List:== new selected list size=====> " + newList.size)

            noShopAvailable.visibility = View.GONE
            nearByShopsList.visibility = View.VISIBLE

            try {

                //20-04-2022
                shopCountTvShow.text = "Near By Shop Count : "+list.size

                localShopsListAdapter = LocalShopsListAdapter(mContext, list, object : LocalShopListClickListener {
                    override fun onQuationClick(shop: Any) {
                        (mContext as DashboardActivity).isBack = true
                        val nearbyShop: AddShopDBModelEntity = shop as AddShopDBModelEntity
                        (mContext as DashboardActivity).loadFragment(FragType.QuotationListFragment, true, nearbyShop.shop_id)
                    }

                    override fun onCallClick(shop: Any) {
                        val nearbyShop: AddShopDBModelEntity = shop as AddShopDBModelEntity
                        IntentActionable.initiatePhoneCall(mContext, nearbyShop.ownerContactNumber)
                    }

                    override fun onOrderClick(shop: Any) {
                        val nearbyShop: AddShopDBModelEntity = shop as AddShopDBModelEntity
                        (mContext as DashboardActivity).loadFragment(FragType.ViewAllOrderListFragment, true, nearbyShop)
                    }

                    override fun onLocationClick(shop: Any) {
                        val nearbyShop: AddShopDBModelEntity = shop as AddShopDBModelEntity
                        (mContext as DashboardActivity).openLocationMap(nearbyShop, false)
                    }

                    override fun visitShop(shop: Any) {
                        if (!Pref.isAddAttendence)
                            (mContext as DashboardActivity).checkToShowAddAttendanceAlert()
                        else {
                            val nearbyShop: AddShopDBModelEntity = shop as AddShopDBModelEntity
                            (mContext as DashboardActivity).callShopVisitConfirmationDialog(nearbyShop.shopName, nearbyShop.shop_id)
                        }
                    }

                },
                        {
                            it
                        })

                (mContext as DashboardActivity).nearbyShopList = list

                layoutManager = LinearLayoutManager(mContext, LinearLayout.VERTICAL, false)
                nearByShopsList.layoutManager = layoutManager
                nearByShopsList.adapter = localShopsListAdapter
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            //20-04-2022
            shopCountTvShow.text = "Near By Shop Count : "+list.size
            Timber.d("=====empty selected list (Local Shop List)=======")

            noShopAvailable.visibility = View.VISIBLE
            nearByShopsList.visibility = View.GONE
        }

        progress_wheel.stopSpinning()

    }


    private fun fetchNearbyShops() {

        /*if (!TextUtils.isEmpty(Pref.latitude) && !TextUtils.isEmpty(Pref.longitude)) {
            val location = Location("")
            location.longitude = Pref.latitude?.toDouble()!!
            location.latitude = Pref.longitude?.toDouble()!!
            getNearyShopList(location)
        }
        else {
            Timber.d("====================null location (Local Shop List)===================")

            progress_wheel.spin()
            SingleShotLocationProvider.requestSingleUpdate(mContext,
                    object : SingleShotLocationProvider.LocationCallback {
                        override fun onStatusChanged(status: String) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onProviderEnabled(status: String) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onProviderDisabled(status: String) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onNewLocationAvailable(location: Location) {
                            if (location.accuracy > 50) {
                                (mContext as DashboardActivity).showSnackMessage("Unable to fetch accurate GPS data. Please try again.")
                                progress_wheel.stopSpinning()
                            } else
                                getNearyShopList(location)
                        }

                    })
        }*/

        if (Pref.isOnLeave.equals("true", ignoreCase = true)) {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_you_are_in_leave))
            return
        }


        if (AppUtils.mLocation != null) {
            if (AppUtils.mLocation!!.accuracy <= Pref.gpsAccuracy.toInt())
                getNearyShopList(AppUtils.mLocation!!)
            else {
                Timber.d("=====Inaccurate current location (Local Shop List)=====")
                singleLocation()
            }
        } else {
            Timber.d("=====null location (Local Shop List)======")
            singleLocation()
        }
    }

    private fun singleLocation() {
        progress_wheel.spin()
        SingleShotLocationProvider.requestSingleUpdate(mContext,
                object : SingleShotLocationProvider.LocationCallback {
                    override fun onStatusChanged(status: String) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onProviderEnabled(status: String) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onProviderDisabled(status: String) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onNewLocationAvailable(location: Location) {
                        if (isGetLocation == -1) {
                            isGetLocation = 0
                            if (location.accuracy > Pref.gpsAccuracy.toInt()) {
                                (mContext as DashboardActivity).showSnackMessage("Unable to fetch accurate GPS data. Please try again.")
                                progress_wheel.stopSpinning()
                            } else
                                getNearyShopList(location)
                        }
                    }

                })

        val t = Timer()
        t.schedule(object : TimerTask() {
            override fun run() {
                try {
                    if (isGetLocation == -1) {
                        isGetLocation = 1
                        progress_wheel.stopSpinning()
                        (mContext as DashboardActivity).showSnackMessage("GPS data to show nearby party is inaccurate. Please stop " +
                                "internet, stop GPS/Location service, and then restart internet and GPS services to get nearby party list.")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, 15000)
    }


    fun getNearyShopList(location: Location) {

        list.clear()
        val allShopList = AppDatabase.getDBInstance()!!.addShopEntryDao().all

        val newList = java.util.ArrayList<AddShopDBModelEntity>()

        for (i in allShopList.indices) {
            /*val userId = allShopList[i].shop_id.substring(0, allShopList[i].shop_id.indexOf("_"))
            if (userId == Pref.user_id)*/
                newList.add(allShopList[i])
        }



        if (newList != null && newList.size > 0) {
            Timber.d("Local Shop List: all shop list size======> " + newList.size)
            Timber.d("======Local Shop List======")
            for (i in 0 until newList.size) {
                val shopLat: Double = newList[i].shopLat
                val shopLong: Double = newList[i].shopLong

                if (shopLat != null && shopLong != null) {
                    val shopLocation = Location("")
                    shopLocation.latitude = shopLat
                    shopLocation.longitude = shopLong

                    /*Timber.d("shop_id====> " + allShopList[i].shop_id)
                    Timber.d("shopName====> " + allShopList[i].shopName)
                    Timber.d("shopLat====> $shopLat")
                    Timber.d("shopLong====> $shopLong")
                    Timber.d("lat=====> " + location.latitude)
                    Timber.d("long=====> " + location.longitude)
                    Timber.d("NEARBY_RADIUS====> $NEARBY_RADIUS")*/

                    var mRadious:Int = NEARBY_RADIUS
                    if(Pref.IsRestrictNearbyGeofence){
                        mRadious = Pref.GeofencingRelaxationinMeter
//                        mRadious=9999000
                    }

                    //val isShopNearby = FTStorageUtils.checkShopPositionWithinRadious(location, shopLocation, NEARBY_RADIUS)
                    val isShopNearby = FTStorageUtils.checkShopPositionWithinRadious(location, shopLocation, mRadious)
                    if (isShopNearby) {
                        //Timber.d("shop_id====> " + newList[i].shop_id)
                        //Timber.d("shopName====> " + newList[i].shopName)
                        //Timber.d("shopLat====> $shopLat")
                        //Timber.d("shopLong====> $shopLong")
                        //Timber.d("lat=====> " + location.latitude)
                        //Timber.d("long=====> " + location.longitude)
                        //Timber.d("NEARBY_RADIUS====> $NEARBY_RADIUS")
                        //Timber.d("=====" + newList[i].shopName + " is nearby=====")
                        newList[i].visited = !shoulIBotherToUpdate(newList[i].shop_id)
                        list.add(newList[i])
                    } else {
                        // Timber.d("=============" + allShopList[i].shopName + " is NOT nearby===============")
                    }

                } else {
                    Timber.d("shop_id====> " + newList[i].shop_id)
                    Timber.d("shopName===> " + newList[i].shopName)

                    if (shopLat != null)
                        Timber.d("shopLat===> $shopLat")
                    else
                        Timber.d("shopLat===> null")

                    if (shopLong != null)
                        Timber.d("shopLong====> $shopLong")
                    else
                        Timber.d("shopLong====> null")
                }
            }
            Timber.d("=============================================")

        } else {
            Timber.d("====empty shop list (Local Shop List)======")
        }

        initAdapter()
    }

    fun shoulIBotherToUpdate(shopId: String): Boolean {
        return !AppDatabase.getDBInstance()!!.shopActivityDao().isShopActivityAvailable(shopId, AppUtils.getCurrentDateForShopActi())
    }
}