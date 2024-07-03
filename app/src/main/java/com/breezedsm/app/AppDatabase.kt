package com.breezedsm.app

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import android.content.Context
import android.os.FileObserver.CREATE

import com.breezedsm.app.AppConstant.DBNAME
import com.breezedsm.app.domain.*
import com.breezedsm.features.location.UserLocationDataDao
import com.breezedsm.features.location.UserLocationDataEntity
import com.breezedsm.features.login.UserAttendanceDataDao
import com.breezedsm.features.login.UserLoginDataEntity
import com.breezedsm.features.stockCompetetorStock.model.CompetetorStockData



/*
 * Copyright (C) 2017 Naresh Gowd Idiga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@Database(entities = arrayOf(AddShopDBModelEntity::class, UserLocationDataEntity::class, UserLoginDataEntity::class, ShopActivityEntity::class,
        StateListEntity::class, CityListEntity::class, MarketingDetailEntity::class, MarketingDetailImageEntity::class, MarketingCategoryMasterEntity::class,
        TaListDBModelEntity::class, AssignToPPEntity::class, AssignToDDEntity::class, WorkTypeEntity::class, OrderListEntity::class,
        OrderDetailsListEntity::class, ShopVisitImageModelEntity::class, UpdateStockEntity::class, PerformanceEntity::class,
        GpsStatusEntity::class, CollectionDetailsEntity::class, InaccurateLocationDataEntity::class, LeaveTypeEntity::class, RouteEntity::class,
        ProductListEntity::class, OrderProductListEntity::class, StockListEntity::class, RouteShopListEntity::class, SelectedWorkTypeEntity::class,
        SelectedRouteEntity::class, SelectedRouteShopListEntity::class, OutstandingListEntity::class/*, LocationEntity::class*/,
        IdleLocEntity::class, BillingEntity::class, StockDetailsListEntity::class, StockProductListEntity::class, BillingProductListEntity::class,
        MeetingEntity::class, MeetingTypeEntity::class, ProductRateEntity::class, AreaListEntity::class, PjpListEntity::class,
        ShopTypeEntity::class, ModelEntity::class, PrimaryAppEntity::class, SecondaryAppEntity::class, LeadTypeEntity::class,
        StageEntity::class, FunnelStageEntity::class, BSListEntity::class, QuotationEntity::class, TypeListEntity::class,
        MemberEntity::class, MemberShopEntity::class, TeamAreaEntity::class, TimesheetListEntity::class, ClientListEntity::class,
        ProjectListEntity::class, ActivityListEntity::class, TimesheetProductListEntity::class, ShopVisitAudioEntity::class,
        TaskEntity::class, BatteryNetStatusEntity::class, ActivityDropDownEntity::class, TypeEntity::class,
        PriorityListEntity::class, ActivityEntity::class, AddDoctorProductListEntity::class, AddDoctorEntity::class,
        AddChemistProductListEntity::class, AddChemistEntity::class, DocumentypeEntity::class, DocumentListEntity::class, PaymentModeEntity::class,
        EntityTypeEntity::class, PartyStatusEntity::class, RetailerEntity::class, DealerEntity::class, BeatEntity::class, AssignToShopEntity::class,
        VisitRemarksEntity::class,ShopVisitCompetetorModelEntity::class,
        OrderStatusRemarksModelEntity::class,CurrentStockEntryModelEntity::class,CurrentStockEntryProductModelEntity::class,
           CcompetetorStockEntryModelEntity::class,CompetetorStockEntryProductModelEntity::class,
        ShopTypeStockViewStatus::class,ProspectEntity::class,ShopDeactivateEntity::class,NewGpsStatusEntity::class,NewProductListEntity::class,NewRateListEntity::class,
    NewOrderDataEntity::class,NewOrderProductEntity::class),
        version = 10, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun addShopEntryDao(): AddShopDao
    abstract fun userLocationDataDao(): UserLocationDataDao
    abstract fun userAttendanceDataDao(): UserAttendanceDataDao
    abstract fun shopActivityDao(): ShopActivityDao
    abstract fun stateDao(): StateListDao
    abstract fun cityDao(): CityListDao
    abstract fun marketingDetailDao(): MarketingDetailDao
    abstract fun marketingDetailImageDao(): MarketingDetailImageDao
    abstract fun marketingCategoryMasterDao(): MarketingCategoryMasterDao

    //New implementation
    abstract fun taListDao(): TaListDao

    abstract fun ppListDao(): AssignToPPDao
    abstract fun ddListDao(): AssignToDDDao
    abstract fun workTypeDao(): WorkTypeDao
    abstract fun orderListDao(): OrderListDao
    abstract fun orderDetailsListDao(): OrderDetailsListDao
    abstract fun shopVisitImageDao(): ShopVisitImageDao
    abstract fun shopVisitCompetetorImageDao(): ShopVisitCompetetorDao
    abstract fun shopVisitOrderStatusRemarksDao(): OrderStatusRemarksDao
    abstract fun shopCurrentStockEntryDao(): CurrentStockEntryDao
    abstract fun shopCurrentStockProductsEntryDao(): CurrentStockEntryProductDao
    abstract fun competetorStockEntryDao(): CompetetorStockEntryDao
    abstract fun competetorStockEntryProductDao(): CompetetorStockEntryProductDao
    abstract fun shopTypeStockViewStatusDao(): ShopTypeStockViewStatusDao
    abstract fun updateStockDao(): UpdateStockDao
    abstract fun performanceDao(): PerformanceDao
    abstract fun gpsStatusDao(): GpsStatusDao
    abstract fun collectionDetailsDao(): CollectionDetailsDao
    abstract fun inaccurateLocDao(): InAccurateLocDataDao
    abstract fun leaveTypeDao(): LeaveTypeDao
    abstract fun routeDao(): RouteDao
    abstract fun productListDao(): ProductListDao
    abstract fun orderProductListDao(): OrderProductListDao
    abstract fun stockListDao(): StockListDao
    abstract fun routeShopListDao(): RouteShopListDao
    abstract fun selectedWorkTypeDao(): SelectedWorkTypeDao
    abstract fun selectedRouteListDao(): SelectedRouteDao
    abstract fun selectedRouteShopListDao(): SelectedRouteShopListDao
    abstract fun updateOutstandingDao(): OutstandingListDao
    //abstract fun locationDao(): LocationDao
    abstract fun idleLocDao(): IdleLocDao

    abstract fun billingDao(): BillingDao
    abstract fun stockDetailsListDao(): StockDetailsListDao
    abstract fun stockProductDao(): StockProductListDao
    abstract fun billProductDao(): BillingProductListDao
    abstract fun addMeetingDao(): MeetingDao
    abstract fun addMeetingTypeDao(): MeetingTypeDao
    abstract fun productRateDao(): ProductRateDao
    abstract fun areaListDao(): AreaListDao
    abstract fun shopTypeDao(): ShopTypeDao
    abstract fun pjpListDao(): PjpListDao
    abstract fun modelListDao(): ModelDao
    abstract fun primaryAppListDao(): PrimaryAppDao
    abstract fun secondaryAppListDao(): SecondaryAppDao
    abstract fun leadTypeDao(): LeadTypeDao
    abstract fun stageDao(): StageDao
    abstract fun funnelStageDao(): FunnelStageDao
    abstract fun bsListDao(): BSListDao
    abstract fun quotDao(): QuotationDao
    abstract fun typeListDao(): TypeListDao
    abstract fun memberDao(): MemberDao
    abstract fun memberShopDao(): MemberShopDao
    abstract fun memberAreaDao(): TeamAreaDao
    abstract fun timesheetDao(): TimesheetListDao
    abstract fun clientDao(): ClientListDao
    abstract fun projectDao(): ProjectListDao
    abstract fun activityDao(): ActivityListDao
    abstract fun productDao(): TimesheetProductListDao
    abstract fun shopVisitAudioDao(): ShopVisitAudioDao
    abstract fun taskDao(): TaskDao
    abstract fun batteryNetDao(): BatteryNetStatusDao

    abstract fun activityDropdownDao(): ActivityDropDownDao
    abstract fun typeDao(): TypeDao
    abstract fun priorityDao(): PriorityDao
    abstract fun activDao(): ActivityDao

    abstract fun addDocProductDao(): AddDoctorProductListDao
    abstract fun addDocDao(): AddDoctorDao
    abstract fun addChemistProductDao(): AddChemistProductListDao
    abstract fun addChemistDao(): AddChemistDao

    abstract fun documentTypeDao(): DocumentypeDao
    abstract fun documentListDao(): DocumentListDao

    abstract fun paymenttDao(): PaymentModeDao

    abstract fun entityDao(): EntityTypeDao
    abstract fun partyStatusDao(): PartyStatusDao
    abstract fun retailerDao(): RetailerDao
    abstract fun dealerDao(): DealerDao
    abstract fun beatDao(): BeatDao
    abstract fun assignToShopDao(): AssignToShopDao

    abstract fun visitRemarksDao(): VisitRemarksDao

    abstract fun prosDao(): ProspectDao
    abstract fun shopDeactivateDao(): ShopDeactivateDao

    abstract fun newGpsStatusDao(): NewGpsStatusDao

    abstract fun newProductListDao(): NewProductListDao
    abstract fun newRateListDao(): NewRateListDao

    abstract fun newOrderDataDao(): NewOrderDataDao
    abstract fun newOrderProductDao(): NewOrderProductDao

    companion object {
        var INSTANCE: AppDatabase? = null

        fun initAppDatabase(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DBNAME)
                        // allow queries on the main thread.
                        // Don't do this on a real app! See PersistenceBasicSample for an example.
                        .allowMainThreadQueries()
                        .addMigrations(MIGRATION_1_2,MIGRATION_2_3,MIGRATION_3_4,MIGRATION_4_5,MIGRATION_5_6,
                            MIGRATION_6_7,MIGRATION_7_8,MIGRATION_8_9,MIGRATION_9_10)
//                        .fallbackToDestructiveMigration()
                        .build()
            }
        }

        fun getDBInstance(): AppDatabase? {

            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE prospect_list_master (id INTEGER NOT NULL PRIMARY KEY , pros_id  TEXT , pros_name TEXT ) ")
                database.execSQL("ALTER TABLE battery_net_status_list ADD COLUMN Available_Storage TEXT")
                database.execSQL("ALTER TABLE battery_net_status_list ADD COLUMN Total_Storage TEXT")
            }
        }
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE shop_detail ADD COLUMN shopStatusUpdate TEXT DEFAULT '1' ")
            }
        }

        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("create TABLE shop_deactivate_record  (id INTEGER NOT NULL PRIMARY KEY , shop_id  TEXT , noti_id TEXT ) ")
            }
        }
        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE shop_detail ADD COLUMN isShopDuplicate INTEGER NOT NULL DEFAULT 0 ")
            }
        }
        val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE battery_net_status_list ADD COLUMN Power_Saver_Status TEXT NOT NULL DEFAULT 'Off' ")
                database.execSQL("create TABLE new_gps_status  (id INTEGER NOT NULL PRIMARY KEY , date_time  TEXT , gps_service_status TEXT, network_status  TEXT , isUploaded INTEGER NOT NULL DEFAULT 0) ")
            }
        }
        val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE INDEX ACTIVITYID ON shop_activity (shopActivityId,shopid,visited_date)")
                database.execSQL("CREATE INDEX ACTIVITY_ID_DATE ON shop_activity (shopid,visited_date)")
            }
        }
        val MIGRATION_7_8: Migration = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //database.execSQL( "DROP INDEX IF EXISTS 'ACTIVITYID' ")
                //database.execSQL( "DROP INDEX IF EXISTS 'ACTIVITY_ID_DATE' ")
                database.execSQL("ALTER TABLE shop_activity ADD COLUMN isNewShop INTEGER NOT NULL DEFAULT 0 ")
            }
        }
        val MIGRATION_8_9: Migration = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("create TABLE new_product_list (product_id TEXT NOT NULL PRIMARY KEY,product_name TEXT NOT NULL,brand_id TEXT NOT NULL," +
                        "brand_name TEXT NOT NULL,category_id TEXT NOT NULL,category_name TEXT NOT NULL,watt_id TEXT NOT NULL,watt_name TEXT NOT NULL,UOM TEXT NOT NULL )")
                database.execSQL("create table new_rate_list (product_id TEXT NOT NULL PRIMARY KEY,mrp TEXT NOT NULL,item_price TEXT NOT NULL,specialRate TEXT NOT NULL)")
                database.execSQL("create table new_order_data (sl_no INTEGER NOT NULL PRIMARY KEY,order_id TEXT NOT NULL,order_date TEXT NOT NULL,order_time TEXT NOT NULL," +
                        "order_date_time TEXT NOT NULL,shop_id TEXT NOT NULL,shop_name TEXT NOT NULL,shop_type TEXT NOT NULL,isInrange INTEGER NOT NULL DEFAULT 0,order_lat TEXT NOT NULL,order_long TEXT NOT NULL,shop_addr TEXT NOT NULL," +
                        "shop_pincode TEXT NOT NULL,order_total_amt TEXT NOT NULL,order_remarks TEXT NOT NULL,isUploaded INTEGER NOT NULL DEFAULT 0)")

                database.execSQL("create table new_order_product (sl_no INTEGER NOT NULL PRIMARY KEY,order_id TEXT NOT NULL," +
                        "product_id TEXT NOT NULL,product_name TEXT NOT NULL,submitedQty TEXT NOT NULL,submitedSpecialRate TEXT NOT NULL)")
            }
        }

        val MIGRATION_9_10: Migration = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE new_order_product ADD COLUMN shop_id TEXT NOT NULL DEFAULT '' ")
                database.execSQL("ALTER TABLE new_order_product ADD COLUMN total_amt TEXT NOT NULL DEFAULT '' ")
                database.execSQL("ALTER TABLE new_order_product ADD COLUMN mrp TEXT NOT NULL DEFAULT '' ")
                database.execSQL("ALTER TABLE new_order_product ADD COLUMN itemPrice TEXT NOT NULL DEFAULT '' ")

                database.execSQL("ALTER TABLE new_order_data ADD COLUMN order_edit_date_time TEXT NOT NULL DEFAULT '' ")
                database.execSQL("ALTER TABLE new_order_data ADD COLUMN order_edit_remarks TEXT NOT NULL DEFAULT '' ")
                database.execSQL("ALTER TABLE new_order_data ADD COLUMN isEdited INTEGER NOT NULL DEFAULT 0 ")
                database.execSQL("ALTER TABLE new_order_data ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0  ")
                 }
        }
    }
}
