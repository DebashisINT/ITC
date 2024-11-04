package com.progdsmfsm.features.dymanicSection.presentation

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.progdsmfsm.R
import com.progdsmfsm.app.NetworkConstant
import com.progdsmfsm.app.types.FragType
import com.progdsmfsm.app.utils.AppUtils
import com.progdsmfsm.base.presentation.BaseActivity
import com.progdsmfsm.base.presentation.BaseFragment
import com.progdsmfsm.features.dashboard.presentation.DashboardActivity
import com.progdsmfsm.features.dymanicSection.api.DynamicRepoProvider
import com.progdsmfsm.features.dymanicSection.model.AllDynamicDataModel
import com.progdsmfsm.features.dymanicSection.model.AllDynamicListResponseModel
import com.progdsmfsm.widgets.AppCustomTextView
import timber.log.Timber
import com.pnikosis.materialishprogress.ProgressWheel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AllDynamicListFragment : BaseFragment() {

    private lateinit var mContext: Context

    private lateinit var rv_all_dynamic_list: RecyclerView
    private lateinit var tv_no_data: AppCustomTextView
    private lateinit var progress_wheel: ProgressWheel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_all_dynamic_list, container, false)

        initView(view)
        getDynamicList()

        return view
    }

    private fun initView(view: View) {
        view.apply {
            rv_all_dynamic_list = findViewById(R.id.rv_all_dynamic_list)
            tv_no_data = findViewById(R.id.tv_no_data)
            progress_wheel = findViewById(R.id.progress_wheel)
        }
        progress_wheel.stopSpinning()
        rv_all_dynamic_list.layoutManager = LinearLayoutManager(mContext)
    }

    private fun getDynamicList() {
        if (!AppUtils.isOnline(mContext)) {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
            tv_no_data.visibility = View.VISIBLE
            return
        }

        val repository = DynamicRepoProvider.dynamicRepoProvider()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
                repository.getAllDynamicList()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->

                            progress_wheel.stopSpinning()

                            val response = result as AllDynamicListResponseModel

                            Timber.d("DYNAMIC ALL LIST RESPONSE=======> " + response.status)

                            if (response.status == NetworkConstant.SUCCESS) {
                                if (response.form_list != null && response.form_list!!.size > 0) {
                                    tv_no_data.visibility = View.GONE
                                    initAdapter(response.form_list)

                                } else {
                                    tv_no_data.visibility = View.VISIBLE
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                }
                            } else {
                                tv_no_data.visibility = View.VISIBLE
                                (mContext as DashboardActivity).showSnackMessage(response.message!!)
                            }

                        }, { error ->
                            BaseActivity.isApiInitiated = false
                            error.printStackTrace()
                            progress_wheel.stopSpinning()
                            tv_no_data.visibility = View.VISIBLE
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                            Timber.d("DYNAMIC ALL LIST ERROR=======> " + error.localizedMessage)
                        })
        )
    }

    private fun initAdapter(formList: ArrayList<AllDynamicDataModel>?) {
        rv_all_dynamic_list.adapter = AllDynamicListAdapter(mContext, formList) {
            (mContext as DashboardActivity).dynamicScreen = it.name
            (mContext as DashboardActivity).loadFragment(FragType.DynamicListFragment, true, it.id)
        }
    }
}