package com.progdsmfsm.features.chatbot.presentation

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.progdsmfsm.R
import com.progdsmfsm.app.AppDatabase
import com.progdsmfsm.app.Pref
import com.progdsmfsm.app.SearchListener
import com.progdsmfsm.app.domain.AddShopDBModelEntity
import com.progdsmfsm.app.utils.AppUtils
import com.progdsmfsm.app.utils.FTStorageUtils
import com.progdsmfsm.base.presentation.BaseFragment
import com.progdsmfsm.features.chat.presentation.ShowPeopleFragment
import com.progdsmfsm.features.dashboard.presentation.DashboardActivity
import com.progdsmfsm.widgets.AppCustomTextView
import java.io.File

class ChatBotShopListFragment : BaseFragment() {

    private lateinit var mContext: Context

    private lateinit var shopList: RecyclerView
    private lateinit var noShopAvailable: AppCompatTextView
    private lateinit var tv_shop_count: AppCustomTextView
    private lateinit var progress_wheel: com.pnikosis.materialishprogress.ProgressWheel

    private var isVisit = false

    private val adapter: ChatBotShopAdapter by lazy {
        ChatBotShopAdapter(mContext, isVisit) {
            val heading = "${Pref.shopText.toUpperCase()} DETAILS"
            var pdfBody = "\n\n\n\n${Pref.shopText} Name: " + it.shopName + "\n\nAddress: " + it.address +
                    "\n\nOwner Name: " + it.ownerName + "\n\nContact No.: " + it.ownerContactNumber

            if (!TextUtils.isEmpty(it.ownerEmailId))
                pdfBody += "\n\nEmail ID: " + it.ownerEmailId

            val shopType = AppDatabase.getDBInstance()?.shopTypeDao()?.getSingleType(it.type)
            shopType?.let {
                pdfBody += "\n\n${Pref.shopText} Type: " + it.shoptype_name
            }

            pdfBody += "\n\nLat: " + it.shopLat + ",  Long: " + it.shopLong + "\n\nCreated User: " +
                    Pref.user_name

            if (!TextUtils.isEmpty(it.added_date))
                pdfBody += "\n\n${Pref.shopText} Created Date: " + AppUtils.convertDateTimeToCommonFormat(it.added_date)


            if (!TextUtils.isEmpty(it.shopImageLocalPath)) {
                var image: Bitmap? = null//BitmapFactory.decodeResource(mContext.resources, R.mipmap.ic_launcher)
                Glide.with(mContext)
                        .asBitmap()
                        .load(it.shopImageLocalPath)
                        .into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                image = resource

                                val path = FTStorageUtils.stringToPdf(pdfBody, mContext, "FTS_" + it.shop_id + ".pdf",
                                        image, heading, 2.7f)

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
                                } else
                                    (mContext as DashboardActivity).showSnackMessage("Pdf can not be sent.")
                            }
                        })
            }
            else {
                val path = FTStorageUtils.stringToPdf(pdfBody, mContext, "FTS_" + it.shop_id + ".pdf",
                        null, heading, 2.7f)

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
                } else
                    (mContext as DashboardActivity).showSnackMessage("Pdf can not be sent.")
            }
        }
    }

    companion object {
        fun getInstance(isVisit: Any): ChatBotShopListFragment {
            val fragment = ChatBotShopListFragment()

            val bundle = Bundle()
            bundle.putBoolean("isVisit", isVisit as Boolean)
            fragment.arguments = bundle

            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context

        isVisit = arguments?.getBoolean("isVisit")!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_chatbot_shop_list, container, false)

        initView(view)

        (mContext as DashboardActivity).setSearchListener(object : SearchListener {
            override fun onSearchQueryListener(query: String) {
                if (query.isBlank()) {
                    val allShopList = AppDatabase.getDBInstance()!!.addShopEntryDao().all

                    if (allShopList != null && allShopList.isNotEmpty()) {
                        noShopAvailable.visibility = View.GONE
                        adapter.updateAdapter(allShopList)
                        tv_shop_count.text = "Total ${Pref.shopText}(s): ${allShopList.size}"
                    }
                    else {
                        adapter.updateAdapter(ArrayList<AddShopDBModelEntity>())
                        tv_shop_count.text = "Total ${Pref.shopText}(s): 0"
                        noShopAvailable.visibility = View.VISIBLE
                    }
                } else {
                    val searchedList = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopBySearchData(query)
                    if (searchedList != null && searchedList.isNotEmpty()) {
                        noShopAvailable.visibility = View.GONE
                        adapter.updateAdapter(searchedList)
                        tv_shop_count.text = "Total ${Pref.shopText}(s): ${searchedList.size}"
                    }
                    else {
                        adapter.updateAdapter(ArrayList<AddShopDBModelEntity>())
                        tv_shop_count.text = "Total ${Pref.shopText}(s): 0"
                        noShopAvailable.visibility = View.VISIBLE
                    }
                }
            }
        })

        return view
    }

    private fun initView(view: View) {
        view.apply {
            shopList = findViewById(R.id.rv_shop_list)
            noShopAvailable = findViewById(R.id.no_shop_tv)
            tv_shop_count = findViewById(R.id.tv_shop_count)
            progress_wheel = findViewById(R.id.progress_wheel)
        }

        progress_wheel.stopSpinning()
        shopList.layoutManager = LinearLayoutManager(mContext)
        shopList.adapter = adapter

        val list = AppDatabase.getDBInstance()!!.addShopEntryDao().all
        if (list != null && list.isNotEmpty()) {
            tv_shop_count.text = "Total ${Pref.shopText}(s): ${list.size}"
            noShopAvailable.visibility = View.GONE
            adapter.updateAdapter(list)
        }
        else {
            tv_shop_count.text = "Total ${Pref.shopText}(s): 0"
            noShopAvailable.visibility = View.VISIBLE
        }
    }
}