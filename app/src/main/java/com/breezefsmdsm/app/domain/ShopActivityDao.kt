package com.breezefsmdsm.app.domain

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.breezefsmdsm.app.AppConstant

/**
 * Created by Pratishruti on 07-12-2017.
 */
@Dao
interface ShopActivityDao {

    @Query("SELECT * FROM " + AppConstant.SHOP_ACTIVITY)
    fun getAll(): List<ShopActivityEntity>

    @Insert
    fun insertAll(vararg shopActivity: ShopActivityEntity)

    @Query("Select * from shop_activity where date=:date")
    fun getTotalShopVisitedForADay(date: String): List<ShopActivityEntity>

    @Query("Select * from shop_activity where date=:date and isUploaded=:isUploaded")
    fun getShopsNotUploaded(date: String, isUploaded: Boolean): List<ShopActivityEntity>

    @Query("update shop_activity set isUploaded=:isUploaded where shopid=:shopId and date=:date")
    fun updateIsUploaded(isUploaded: Boolean, shopId: String, date: String): Long

    @Query("Select duration_spent from shop_activity where shopid=:shopId and date=:date")
    fun getTimeDurationForDayOfShop(shopId: String, date: String): String

    @Query("Select endTimeStamp from shop_activity where shopid=:shopId and date=:date")
    fun getEndTimeStampOfShopForDay(shopId: String, date: String): String

    @Query("update shop_activity set duration_spent=:duration where shopid=:shopId and date=:date")
    fun updateTimeDurationForDayOfShop(shopId: String, duration: String, date: String)

    @Query("update shop_activity set totalMinute=:duration where shopid=:shopId and date=:date")
    fun updateTotalMinuteForDayOfShop(shopId: String, duration: String, date: String)

    @Query("update shop_activity set isVisited=:isVisited where shopid=:shopId and date=:date")
    fun updateIsVisitedOfShop(isVisited: Boolean, shopId: String, date: String)

    @Query("update shop_activity set endTimeStamp=:endTime where shopid=:shopId and date=:date")
    fun updateEndTimeOfShop(endTime: String, shopId: String, date: String): Int

    @Query("Select isVisited from shop_activity where shopid=:shopId and date=:date")
    fun getIsVisitedOfShop(shopId: String, date: String): Boolean

    @Query("Select date from shop_activity where shopid=:shopId")
    fun getVisitDateOfShop(shopId: String): String

    @Query("Select * from shop_activity where shopid=:shopId and date=:date")
    fun getShopForDay(shopId: String, date: String): List<ShopActivityEntity>

    @Query("Select * from shop_activity where shopid=:shopId and date=:date and isDurationCalculated=:isDurationCalculated")
    fun getShopForDayDurationWise(shopId: String, date: String, isDurationCalculated: Boolean): List<ShopActivityEntity>

    @Query("Select * from shop_activity where shopid=:shopId and date=:date and isVisited=:isVisited")
    fun getVisitedShopForDay(shopId: String, date: String, isVisited: Boolean): ShopActivityEntity

    @Query("Select * from shop_activity where shopid=:shopId")
    fun getShopActivityForId(shopId: String): ShopActivityEntity

    @Query("Select * from shop_activity where shopid=:shopId order by visited_date DESC")
    fun getShopActivityForIdDescVisitDate(shopId: String): List<ShopActivityEntity>

    @Query("Select duration_spent from shop_activity where shopid=:shopId")
    fun updateShopActivity(shopId: String): String

    @Query("Select startTimeStamp from shop_activity where shopid=:shopId and date=:date")
    fun getStartTimestampOfShop(shopId: String, date: String): String

    @Query("Select isDurationCalculated from shop_activity where shopid=:shopId and date=:date")
    fun isDurationAvailable(shopId: String, date: String): Boolean

    @Query("Select isUploaded from shop_activity where shopid=:shopId and date=:date")
    fun isShopUploaded(shopId: String, date: String): Boolean

    @Query("update shop_activity set isDurationCalculated=:isDurationCalculated where shopid=:shopId and date=:date")
    fun updateDurationAvailable(isDurationCalculated: Boolean, shopId: String, date: String)

    @Query("Select * from shop_activity where isUploaded=:isUploaded")
    fun getSyncedShopActivity(isUploaded: Boolean): List<ShopActivityEntity>

    @Query("Select * from shop_activity where isDurationCalculated=:isDurationCalculated")
    fun durationAvailable(isDurationCalculated: Boolean): List<ShopActivityEntity>

    @Query("Select * from shop_activity where isDurationCalculated=:isDurationCalculated and isUploaded=:isUploaded")
    fun durationAvailable(isDurationCalculated: Boolean, isUploaded: Boolean): List<ShopActivityEntity>

    @Query("Select * from shop_activity where shopid=:shopId and isDurationCalculated=:isDurationCalculated and isUploaded=:isUploaded")
    fun durationAvailableForShop(shopId: String, isDurationCalculated: Boolean, isUploaded: Boolean): ShopActivityEntity?

    @Query("Select * from shop_activity where shopid=:shopId and isDurationCalculated=:isDurationCalculated and isUploaded=:isUploaded and date=:date")
    fun durationAvailableForTodayShop(shopId: String, isDurationCalculated: Boolean, isUploaded: Boolean, date: String): ShopActivityEntity?

    @Query("DELETE FROM shop_activity where shopid=:shopId and date=:date")
    fun deleteShopByIdAndDate(shopId: String, date: String): Int


    @Query("update shop_activity set isUploaded=:isUploaded where shopid=:shopId and date=:date")
    fun updateisUploaded(isUploaded: Boolean, shopId: String, date: String)

    @Query("Select shopid from shop_activity where shopid=:shopId and date=:date")
    fun isShopActivityAvailable(shopId: String, date: String): Boolean

    @Query("update shop_activity set isDurationCalculated=:isDurationCalculated where date=:date")
    fun updateDurationCalculatedStatus(isDurationCalculated: Boolean, date: String): Int

    @Query("Select * from shop_activity where date=:date and isDurationCalculated=:isDurationCalculated")
    fun getDurationCalculatedVisitedShopForADay(date: String, isDurationCalculated: Boolean): List<ShopActivityEntity>

    @Query("Select * from shop_activity where date=:date and isDurationCalculated=:isDurationCalculated and isVisited=:isVisited")
    fun getDurationCalculatedShopForADay(date: String, isDurationCalculated: Boolean, isVisited:Boolean): List<ShopActivityEntity>

    @Query("DELETE FROM shop_activity")
    fun deleteAll()

    @Query("Select * from shop_activity where next_visit_date=:next_visit_date order by visited_date DESC")
    fun getShopActivityNextVisitDateWise(next_visit_date: String): List<ShopActivityEntity>

    @Query("update shop_activity set early_revisit_reason=:early_revisit_reason where shopid=:shopid  and date=:date")
    fun updateEarlyRevisitReason(early_revisit_reason: String, shopid: String, date: String)

    @Query("update shop_activity set device_model=:device_model, android_version=:android_version, battery=:battery, net_status=:net_status, net_type=:net_type where shopid=:shopid  and date=:date")
    fun updateDeviceStatusReason(device_model: String, android_version: String, battery: String, net_status: String,
                                 net_type: String, shopid: String, date: String)



    @Query("update shop_activity set isDurationCalculated=:isDurationCalculated where shopid=:shopId and date=:date and startTimeStamp=:startTimeStamp")
    fun updateDurationAvailable(isDurationCalculated: Boolean, shopId: String, date: String, startTimeStamp: String)

    @Query("update shop_activity set duration_spent=:duration where shopid=:shopId and date=:date and startTimeStamp=:startTimeStamp")
    fun updateTimeDurationForDayOfShop(shopId: String, duration: String, date: String, startTimeStamp: String)

    @Query("update shop_activity set totalMinute=:duration where shopid=:shopId and date=:date and startTimeStamp=:startTimeStamp")
    fun updateTotalMinuteForDayOfShop(shopId: String, duration: String, date: String, startTimeStamp: String)

    @Query("update shop_activity set isVisited=:isVisited where shopid=:shopId and date=:date and startTimeStamp=:startTimeStamp")
    fun updateIsVisitedOfShop(isVisited: Boolean, shopId: String, date: String, startTimeStamp: String)

    @Query("update shop_activity set endTimeStamp=:endTime where shopid=:shopId and date=:date and startTimeStamp=:startTimeStamp")
    fun updateEndTimeOfShop(endTime: String, shopId: String, date: String, startTimeStamp: String)

    @Query("update shop_activity set early_revisit_reason=:early_revisit_reason where shopid=:shopid  and date=:date and startTimeStamp=:startTimeStamp")
    fun updateEarlyRevisitReason(early_revisit_reason: String, shopid: String, date: String, startTimeStamp: String)

    @Query("update shop_activity set device_model=:device_model, android_version=:android_version, battery=:battery, net_status=:net_status, net_type=:net_type where shopid=:shopid  and date=:date and startTimeStamp=:startTimeStamp")
    fun updateDeviceStatusReason(device_model: String, android_version: String, battery: String, net_status: String,
                                 net_type: String, shopid: String, date: String, startTimeStamp: String)

    @Query("update shop_activity set isUploaded=:isUploaded where shopid=:shopId and date=:date and startTimeStamp=:startTimeStamp")
    fun updateIsUploaded(isUploaded: Boolean, shopId: String, date: String, startTimeStamp: String)

    @Query("update shop_activity set isUploaded=:isUploaded where shopid=:shopId and date=:date and startTimeStamp=:startTimeStamp")
    fun updateisUploaded(isUploaded: Boolean, shopId: String, date: String, startTimeStamp: String)

    @Query("update shop_activity set out_time=:out_time where shopid=:shopId and date=:date and startTimeStamp=:startTimeStamp")
    fun updateOutTime(out_time: String, shopId: String, date: String, startTimeStamp: String)

    @Query("update shop_activity set out_loc=:out_loc where shopid=:shopId and date=:date and startTimeStamp=:startTimeStamp")
    fun updateOutLocation(out_loc: String, shopId: String, date: String, startTimeStamp: String)

    @Query("Select * from shop_activity where shopid=:shopId and isDurationCalculated=:isDurationCalculated and isUploaded=:isUploaded")
    fun durationAvailableForShopList(shopId: String, isDurationCalculated: Boolean, isUploaded: Boolean): List<ShopActivityEntity>

    @Query("Select * from shop_activity")
    fun getShopActivityAll(): List<ShopActivityEntity>


    @Query("Select * from shop_activity where shopId=:shopId and isUploaded=:isUploaded")
    fun getNewShopActivityKey(shopId: String,isUploaded: Boolean): ShopActivityEntity


}