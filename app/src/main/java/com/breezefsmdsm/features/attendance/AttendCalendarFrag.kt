package com.breezefsmdsm.features.attendance

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.breezefsmdsm.R
import com.breezefsmdsm.app.NetworkConstant
import com.breezefsmdsm.app.Pref
import com.breezefsmdsm.app.types.FragType
import com.breezefsmdsm.app.utils.AppUtils
import com.breezefsmdsm.app.utils.Toaster
import com.breezefsmdsm.base.presentation.BaseActivity
import com.breezefsmdsm.base.presentation.BaseFragment
import com.breezefsmdsm.features.attendance.api.AttendanceRepositoryProvider
import com.breezefsmdsm.features.attendance.model.AttendanceRequest
import com.breezefsmdsm.features.attendance.model.AttendanceResponse
import com.breezefsmdsm.features.attendance.model.DayStartEndListResponse
import com.breezefsmdsm.features.dashboard.presentation.DashboardActivity
import com.example.xcall.EventDayDecorator
import com.example.xcall.EventDayDecorator1
import com.pnikosis.materialishprogress.ProgressWheel
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.time.LocalDate
import java.util.Calendar

// Rev 1.0 Created by Suman on 24-05-2023 mantis id 26211

class AttendCalendarFrag: BaseFragment(),OnClickListener {

    private lateinit var mContext: Context
    private lateinit var calendarView : MaterialCalendarView
    private lateinit var progress_wheel:ProgressWheel
    var mEventDaysPresent: ArrayList<CalendarDay> = ArrayList()
    var mEventDaysPresentQualified: ArrayList<CalendarDay> = ArrayList()
    var mEventDaysAbsent: ArrayList<CalendarDay> = ArrayList()
    var mEventDaysToday: ArrayList<CalendarDay> = ArrayList()
    var dateL:ArrayList<Pair<String?, String?>> = ArrayList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.frag_attend_calendar, container, false)
        initView(view)

        return view
    }

    private fun initView(view: View) {
        calendarView = view.findViewById(R.id.calendarView_frag_attend_calendar)
        progress_wheel = view.findViewById(R.id.progress_wheel_frag_attend)
        //calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_MULTIPLE)
        calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_NONE)

        var firstDayOfMonth = AppUtils.getFirstDateOfThisMonth_DD_MM_YY()
        var y = firstDayOfMonth.split("-").get(2).toInt()
        var m = firstDayOfMonth.split("-").get(1).toInt()
        var d = firstDayOfMonth.split("-").get(0).toInt()
        calendarView.state().edit()
            .setMinimumDate(CalendarDay.from(y,m,d))
            .setMaximumDate(CalendarDay.today())
            .commit()

        setCalender()
    }

    private fun setCalender(){
        var currentYear = AppUtils.getCurrentDateForShopActi().split("-").get(0)
        var currentMonth = AppUtils.getCurrentDateForShopActi().split("-").get(1)
        var currentDay = AppUtils.getCurrentDateForShopActi().split("-").get(2)

        setDataForCurrentMonth(currentYear,currentMonth,currentDay)

        calendarView.setOnMonthChangedListener { widget, date -> // Do something here
            val selectedDate = date as CalendarDay
            var noOfDays = AppUtils.numberOfDaysInMonth(selectedDate.month,selectedDate.year)
            var startD = "${selectedDate.year}-${String.format("%02d", selectedDate.month)}-01"
            var endDate = "${selectedDate.year}-${String.format("%02d", selectedDate.month)}-${String.format("%02d", noOfDays)}"


            var currentY = AppUtils.getCurrentDateForShopActi().split("-").get(0)
            var currentM = AppUtils.getCurrentDateForShopActi().split("-").get(1)
            if(selectedDate.year.toString().equals(currentY) && String.format("%02d", selectedDate.month).equals(currentM)){
                setDataForCurrentMonth(currentYear,currentMonth,currentDay)
            }else{
                //getAttendListApi(startD,endDate)
                getDaystartEndListApi(startD,endDate)
            }
        }
    }

    private fun setDataForCurrentMonth(currentYear:String,currentMonth:String,currentDay:String){
        var startDate = "$currentYear-$currentMonth-01"
        var endDate = "$currentYear-$currentMonth-$currentDay"
        //getAttendListApi(startDate,endDate)
        getDaystartEndListApi(startDate,endDate)
    }

    /*private fun getAttendListApi(startD:String,endD:String){

        val attendanceReq = AttendanceRequest()
        attendanceReq.user_id = Pref.user_id!!
        attendanceReq.session_token = Pref.session_token
        attendanceReq.start_date = startD
        attendanceReq.end_date = endD

        val repository = AttendanceRepositoryProvider.provideAttendanceRepository()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
            repository.getAttendanceList(attendanceReq)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    val attendanceList = result as AttendanceResponse
                    progress_wheel.stopSpinning()
                    if (attendanceList.status == "200") {
                        doAsync {
                            dateL = ArrayList()
                            for(i in 0..attendanceList.shop_list!!.size-1){
                                var obj = attendanceList.shop_list!!.get(i).login_date!!.split("T").get(0).toString()
                                dateL.add(obj)
                            }
                            uiThread {
                                setFinalCal(startD,endD)
                            }
                        }
                    }else{
                        //(mContext as DashboardActivity).showSnackMessage("No Record Found.")
                    }
                }, { error ->
                        progress_wheel.stopSpinning()
                        error.printStackTrace()
                        (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                    })
        )
    }*/

    private fun getDaystartEndListApi(startD:String,endD:String){

        val attendanceReq = AttendanceRequest()
        attendanceReq.user_id = Pref.user_id!!
        attendanceReq.session_token = Pref.session_token
        attendanceReq.start_date = startD
        attendanceReq.end_date = endD

        val repository = AttendanceRepositoryProvider.provideAttendanceRepository()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
            repository.getDayStartEndList(attendanceReq)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    val attendanceList = result as DayStartEndListResponse
                    progress_wheel.stopSpinning()
                    if (attendanceList.status == "200") {
                        doAsync {
                            //dateL = ArrayList()
                            dateL = ArrayList()
                            for(i in 0..attendanceList.day_start_end_list!!.size-1){
                                var obj = attendanceList.day_start_end_list!!.get(i).dayStart_date_time!!.split("T").get(0).toString()
                                dateL.add(Pair(obj,attendanceList.day_start_end_list!!.get(i).isQualifiedAttendance.toString()))
                            }
                            uiThread {
                                setFinalCal(startD,endD)
                            }
                        }
                    }else{
                        //(mContext as DashboardActivity).showSnackMessage("No Record Found.")
                    }
                }, { error ->
                    progress_wheel.stopSpinning()
                    error.printStackTrace()
                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                })
        )
    }


    private fun setFinalCal(startD:String,endD:String){
        if(dateL.size>0){

            dateL = dateL.distinctBy { it.first } as ArrayList<Pair<String?, String?>>

            mEventDaysPresent.clear()
            mEventDaysPresentQualified.clear()
            mEventDaysAbsent.clear()
            mEventDaysToday.clear()
            calendarView.removeDecorators()

            var noOfDays = AppUtils.getDayDuration(startD,endD)
            var totalDaysL:ArrayList<String> = ArrayList()
            var dayCalculation = startD
            for(i in 0.. noOfDays-2){
                totalDaysL.add(dayCalculation)
                dayCalculation = LocalDate.parse(dayCalculation).plusDays(1).toString()
            }
            outerL@ for(i in 0..totalDaysL.size-1){
                var isPresent = false
                var isPresentQualified = false
               innerL@ for(j in 0..dateL.size-1){
                    if(totalDaysL.get(i).equals(dateL.get(j).first)){
                        isPresent = true
                        if(dateL.get(j).second.equals("1")){
                            isPresentQualified = true
                        }
                         break@innerL
                    }
                }
                var y = totalDaysL.get(i).split("-").get(0).toInt()
                var m = totalDaysL.get(i).split("-").get(1).toInt()
                var d = totalDaysL.get(i).split("-").get(2).toInt()
                if(isPresent){
                    if(isPresentQualified){
                        mEventDaysPresentQualified.add(CalendarDay.from(y,m,d))
                    }else{
                        mEventDaysPresent.add(CalendarDay.from(y,m,d))
                    }
                }else{
                    mEventDaysAbsent.add(CalendarDay.from(y,m,d))
                }
            }

            calendarView.addDecorator(EventDayDecorator(mContext, mEventDaysPresent))
            calendarView.addDecorator(EventDayDecorator1(mContext, mEventDaysAbsent))
            calendarView.addDecorator(EventDayDecorator4(mContext, mEventDaysPresentQualified))


            var todayDate = AppUtils.getCurrentDateForShopActi()
            var todayY = todayDate.split("-").get(0).toInt()
            var todayM = todayDate.split("-").get(1).toInt()
            var todayD = todayDate.split("-").get(2).toInt()
            if(mEventDaysPresent.contains(CalendarDay.today())){
                mEventDaysToday.add(CalendarDay.from(todayY,todayM,todayD))
                calendarView.addDecorator(EventDayDecorator2(mContext, mEventDaysToday))
            }
            if(mEventDaysAbsent.contains(CalendarDay.today())){
                mEventDaysToday.add(CalendarDay.from(todayY,todayM,todayD))
                calendarView.addDecorator(EventDayDecorator3(mContext, mEventDaysToday))
            }


        }
    }

    override fun onClick(p0: View?) {

    }
}