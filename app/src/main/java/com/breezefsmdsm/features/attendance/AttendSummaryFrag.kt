package com.breezefsmdsm.features.attendance

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.breezefsmdsm.R
import com.breezefsmdsm.app.Pref
import com.breezefsmdsm.app.utils.AppUtils
import com.breezefsmdsm.app.utils.Toaster
import com.breezefsmdsm.base.presentation.BaseActivity
import com.breezefsmdsm.base.presentation.BaseFragment
import com.breezefsmdsm.features.attendance.api.AttendanceRepositoryProvider
import com.breezefsmdsm.features.attendance.model.AttendSummResponse
import com.breezefsmdsm.features.attendance.model.DayStartEndListResponse
import com.breezefsmdsm.features.dashboard.presentation.DashboardActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class AttendSummaryFrag : BaseFragment(), View.OnClickListener {
    private lateinit var mContext: Context
    private lateinit var workDaysTV:TextView
    private lateinit var presentDaysTV:TextView
    private lateinit var absentDaysTV:TextView
    private lateinit var qualifiedDaysTV:TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.frag_attend_summary, container, false)
        initView(view)

        return view
    }

    private fun initView(view: View) {
        workDaysTV = view.findViewById(R.id.tv_frag_attend_summ_work_days)
        presentDaysTV = view.findViewById(R.id.tv_frag_attend_summ_present_days)
        absentDaysTV = view.findViewById(R.id.tv_frag_attend_summ_absent_days)
        qualifiedDaysTV = view.findViewById(R.id.tv_frag_attend_summ_qualified_days)
        getData()
    }

    override fun onClick(p0: View?) {

    }

    fun getData(){
        if(AppUtils.isOnline(mContext)){
            val repository = AttendanceRepositoryProvider.provideAttendanceRepository()
            BaseActivity.compositeDisposable.add(
                repository.getAttendanceSumm(Pref.user_id!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val response = result as AttendSummResponse
                        //progress_wheel.stopSpinning()
                        if (response.status == "200") {
                            workDaysTV.text = response.total_work_day.toString()
                            presentDaysTV.text = response.total_present_day.toString()
                            absentDaysTV.text = response.total_absent_day.toString()
                            qualifiedDaysTV.text = response.total_qualified_day.toString()
                        }else{
                            (mContext as DashboardActivity).showSnackMessage("No Record Found.")
                        }
                    }, { error ->
                        //progress_wheel.stopSpinning()
                        error.printStackTrace()
                        (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                    })
            )
        }else{
            Toaster.msgShort(mContext,getString(R.string.no_internet))
        }
    }

}