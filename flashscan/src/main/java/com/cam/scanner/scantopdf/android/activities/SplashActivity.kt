package com.cam.scanner.scantopdf.android.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.cam.scanner.scantopdf.android.AfterIntsall
import com.cam.scanner.scantopdf.android.AppController
import com.cam.scanner.scantopdf.android.AppOpenManager
import com.cam.scanner.scantopdf.android.BuildConfig
import com.cam.scanner.scantopdf.android.R
import com.cam.scanner.scantopdf.android.db.DBHandler
import com.cam.scanner.scantopdf.android.rest.ApiClient
import com.cam.scanner.scantopdf.android.rest.ApiInterface
import com.cam.scanner.scantopdf.android.rest.response.GetPlans
import com.cam.scanner.scantopdf.android.util.Constants
import com.cam.scanner.scantopdf.android.util.FirebaseRemoteConfigOperations
import com.cam.scanner.scantopdf.android.util.FlashScanUtil
import com.cam.scanner.scantopdf.android.util.PrefManager
import com.cam.scanner.scantopdf.android.util.SubscribeToTopic
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.Objects

//import com.google.android.gms.ads.InterstitialAd;
class SplashActivity : BaseActivity(), PurchasesUpdatedListener {
    private var prefManager: PrefManager? = null
    private var interstitialAd: InterstitialAd? = null
    private var util: FlashScanUtil? = null
    private var isAdLoaded = false
    private var isMoveForwarded = false
    private var tv_loading: TextView? = null
    private var progress_lay: RelativeLayout? = null
    private var dbHandler: DBHandler? = null

    private var billingClient: BillingClient? = null
    private var restoreFound = false
    private val restoreFoundQuarterly = false
    private var restoreFoundOcrMonthly = false

    private var apiInterface: ApiInterface? = null
    private var screenId: String? = null
    private var actionId: String? = null
    private var isFromNotif = false
    private var isSplashInBackground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (loadFirebaseDataAndCheckRedirection()) {
            return
        }

        //ReleaseBuild 5.0
        /*val firebaseRemoteConfigOperations = FirebaseRemoteConfigOperations(this, this)
        firebaseRemoteConfigOperations.firebaseRemoteConfig()*/

        findIds()
        initObjects()

        prefManager!!.isSplashDone = false
        Log.i(TAG, "Loading Issue Debug:: prefManager.isSplashDone():" + prefManager!!.isSplashDone)

        androidIdInPrefs()

        if (Constants.IS_REMOTE_CONFIG_FROM_OWN_API) {
            plansFromApi
        }

        if (Constants.IS_ORDER_REAL) {
//            checkPremiumYearlyStatus();
        } else {
            setVirtualOrderFound()
        }

        sessionUpdate()

        /*loadInterstitialAd();*/
        var trackingdone = prefManager!!.isTrackingDone

        //for debug build
        if (BuildConfig.DEBUG) {
            trackingdone = true
        }


        Log.i(TAG, "trackingdone: $trackingdone")

        if (!trackingdone) {
            Log.i(TAG, "going to afterinstall")
            AfterIntsall().getInstallDetails(this@SplashActivity)
        }

       /* val packageName = firebaseRemoteConfigOperations.packageName

        Log.i(TAG, "packageName remote: $packageName")*/

        val PACKAGE_NAME = applicationContext.packageName

        Log.i(TAG, "PACKAGE_NAME: $PACKAGE_NAME")

        if (!packageName.equals(PACKAGE_NAME, ignoreCase = true)) {
            return
        }


//        handleLoadingVisibility()

        val showIntersSplash = dbHandler!!.showIntesSplash()
        var canShow: Boolean

        val intersSplashAfter = dbHandler!!.intersSplashAfter()

        Log.i(TAG, "intersSplashAfter: $intersSplashAfter")

        increaseSplashAttempted()

        if (dbHandler!!.splashAttempted > intersSplashAfter) {
            Log.i(TAG, "Can show inters ad after splash")
            canShow = true
        } else {
            Log.i(TAG, "Can not show inters ad after splash yet.")
            canShow = false
        }

        Log.i(
            TAG,
            "showIntersSplash: $showIntersSplash, canShow: $canShow, isFromNotif: $isFromNotif"
        )

        loadfiresbasedata()

        Log.i(TAG, "Loading Issue Debug:: prefManager.isSplashDone():" + prefManager!!.isSplashDone)

        //        if (!prefManager.isAppAdFree() && Constants.SHOW_INTERSTITIAL_ADS.FOR_SPLASH) {
        if (!prefManager!!.isAppAdFree && showIntersSplash && canShow && !isFromNotif && !appOpenAdCheck()) {
            Log.i(TAG, "APP OPEN Not load")
            if (util!!.isConnectingToInternet) {
                loadAndShowInterstitialAd()
                Handler().postDelayed({ //                        Log.i(TAG, "isAdLoaded:" + isAdLoaded + ", isMoveForwarded:" + isMoveForwarded);
                    Log.i(
                        TAG,
                        "Loading Issue Debug:: isAdLoaded:" + isAdLoaded + ", prefManager.isSplashDone():" + prefManager!!.isSplashDone
                    )
                    //                        if (!isAdLoaded && !isMoveForwarded) {
                    if (!isAdLoaded && !prefManager!!.isSplashDone && !Constants.isSplashAdLoad && !prefManager!!.showAppOpenAd()) {
                        Log.i(TAG, "Loading Issue Debug:: in onCreate::: call moveForward()")
                        moveForward()
                    } else {
                        Log.i(
                            TAG,
                            "Loading Issue Debug:: in onCreate::: else condition - moveForward()"
                        )
                    }
                }, Constants.SPLASH_TIME_OUT)
            } else {
                Log.i(
                    TAG,
                    "Loading Issue Debug:: in onCreate::: isConnectingToInternet else condition"
                )
                noAdMoveToNextScreen()
            }
        } else {
            Log.i(
                TAG,
                "Loading Issue Debug:: in onCreate::: !prefManager.isAppAdFree() && showIntersSplash && canShow && !isFromNotif else condition"
            )
            if (!isFromNotif) {
                Log.i(TAG, "Loading Issue Debug:: in onCreate::: isFromNotif: $isFromNotif")
                //                noAdMoveToNextScreen();
            } else {
                Log.i(TAG, "Loading Issue Debug:: in onCreate::: isFromNotif: $isFromNotif")
            }
            noAdMoveToNextScreen()
        }
    }

    private fun loadFirebaseDataAndCheckRedirection(): Boolean {
        var needRedirection = false
        Log.i(TAG, "inside loadFirebaseDataAndCheckRedirection()")
        val data = intent.extras
        if (data != null) {
            val appPackageName = data.getString("app_package_name")

            Log.i(TAG, "appPackageName: $appPackageName")

            if (appPackageName != null && !appPackageName.isEmpty()) {
                needRedirection = true
                var intent = try {
                    Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
                } catch (anfe: ActivityNotFoundException) {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                    )
                }

                startActivity(intent)

                finishAffinity()
            }
        }
        return needRedirection
    }

    private fun androidIdInPrefs() {
        val selfAndroidId = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )

        Log.i(TAG, "ocr reset premium, self android_id: $selfAndroidId")

        prefManager!!.setSecureAndroidId(selfAndroidId)
    }

    private fun setVirtualOrderFound() {
        prefManager!!.isPremiumYearly =
            Constants.IN_DUMMY_AT_SPLASH_PREMIUM_YEARLY_FOUND
        if (Constants.IN_DUMMY_AT_SPLASH_PREMIUM_YEARLY_FOUND) {
            prefManager!!.orderIdPremiumYearly =
                Constants.LAST_TESTED_ORDER_ID_PREMIUM_YEARLY
            prefManager!!.planIdForApi = Constants.PLAN_PEMIUM_YEARLY
        }

        prefManager!!.isOcrMonthly =
            Constants.IN_DUMMY_AT_SPLASH_OCR_MONTHLY_FOUND
        if (Constants.IN_DUMMY_AT_SPLASH_OCR_MONTHLY_FOUND) {
            prefManager!!.orderIdOcrMonthly =
                Constants.LAST_TESTED_ORDER_ID_OCR_MONTHLY
            if (Constants.IN_DUMMY_AT_SPLASH_PREMIUM_YEARLY_FOUND) {
                prefManager!!.planIdForApi = Constants.PLAN_PEMIUM_YEARLY
            } else {
                prefManager!!.planIdForApi = Constants.PLAN_FREE
            }
        }
    }

    private val plansFromApi: Unit
        get() {
            if (util!!.isConnectingToInternet) {
                val call = apiInterface!!.plans
                call.enqueue(object : Callback<GetPlans?> {
                    override fun onResponse(call: Call<GetPlans?>, response: Response<GetPlans?>) {
                        Log.i(TAG, "GetPlans response: $response")

                        val getPlans = response.body()
                        Log.i(TAG, "getPlans online: $getPlans")

                        putInDbOCRRules(getPlans)
                    }

                    override fun onFailure(call: Call<GetPlans?>, t: Throwable) {
                        Log.i(TAG, "getPlans call onFailure")
                        call.cancel()
                    }
                })
            } else {
                try {
                    val obj = JSONObject(loadJSONFromAsset())
                    Log.i(TAG, "obj: $obj")

                    // Creating a Gson Object
                    val gson = Gson()

                    // Converting json to object
                    // first parameter should be prpreocessed json
                    // and second should be mapping class
                    val getPlans = gson.fromJson(loadJSONFromAsset(), GetPlans::class.java)
                    Log.i(TAG, "getPlans offline: $getPlans")

                    putInDbOCRRules(getPlans)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }

    fun loadJSONFromAsset(): String? {
        var json: String? = null
        try {
            val `is` = assets.open(Constants.PLANS_FILE)
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            json = String(buffer, charset("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    private fun putInDbOCRRules(getPlans: GetPlans?) {
        try {
            val listPlans = getPlans!!.oPlanList

            var freeOcr = 0
            var premiumYearly = 0
            var ocrMonthly = 0

            var testFreeOcr = 0
            var testPremiumYearly = 0
            var testOcrMonthly = 0

            for (oPlanList in listPlans) {
                val planId = oPlanList.planId
                val credits = oPlanList.credits

                when (planId) {
                    Constants.PLAN_FREE -> freeOcr = credits
                    Constants.PLAN_PEMIUM_YEARLY -> premiumYearly = credits
                    Constants.PLAN_OCR_MONTHLY -> ocrMonthly = credits
                    Constants.PLAN_FREE_TEST -> testFreeOcr = credits
                    Constants.PLAN_PEMIUM_YEARLY_TEST -> testPremiumYearly = credits
                    Constants.PLAN_OCR_MONTHLY_TEST -> testOcrMonthly = credits
                }
            }


            Log.d(TAG, "freeOcr: $freeOcr")
            Log.d(TAG, "premiumYearlyOcr: $premiumYearly")
            Log.d(TAG, "ocrMonthly: $ocrMonthly")

            Log.d(TAG, "testFreeOcr: $testFreeOcr")
            Log.d(TAG, "testPremiumYearlyOcr: $testPremiumYearly")
            Log.d(TAG, "testOcrMonthly: $testOcrMonthly")

            if (dbHandler!!.existOcrRules()) {
                dbHandler!!.updateOcrRules(
                    freeOcr,
                    premiumYearly,
                    ocrMonthly,
                    testFreeOcr,
                    testPremiumYearly,
                    testOcrMonthly
                )
            } else {
                dbHandler!!.insertOcrRules(
                    freeOcr,
                    premiumYearly,
                    ocrMonthly,
                    testFreeOcr,
                    testPremiumYearly,
                    testOcrMonthly
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "exception" + e.message)
        }
    }

    private fun checkPremiumYearlyStatus() {
        if (util!!.isConnectingToInternet) {
            connectBillingService()
        }
    }

    private fun connectBillingService() {
        /*billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener(this).build()

        clearGooglePlayStoreBillingCacheIfPossible()

        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                *//*if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.

                    *//**//*billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS,
                            new PurchaseHistoryResponseListener() {
                                @Override
                                public void onPurchaseHistoryResponse(BillingResult billingResult, List<PurchaseHistoryRecord> list) {
                                    Log.i(TAG, "list: " + list);
                                }

                            });*//**//*

                    val purchasesResult = billingClient!!.queryPurchases(BillingClient.SkuType.SUBS)
                    if (purchasesResult.purchasesList != null) {
                        val purchases = purchasesResult.purchasesList
                        if (purchases!!.size > 0) {
                            Log.i(TAG, "purchase found in restore")

                            var isPremiumFound = false
                            val isPremiumFoundQuarterly = false
                            var isOcrMonthlyFound = false

                            for (purchase in purchases) {
                                if (Constants.PRODUCT_ID_PREMIUM == purchase.sku) {
                                    Log.i(TAG, "premium found")

                                    isPremiumFound = true
                                    prefManager!!.orderIdPremiumYearly = purchase.orderId
                                    prefManager!!.planIdForApi = Constants.PLAN_PEMIUM_YEARLY

                                    prefManager!!.purchasedPlanName = Constants.BUY_NOW_YEARLY

                                    //                                    prefManager.setPremiumYearly(true);
                                    restoreFound = true

                                    //                                    ocrCountResetViaOrderId(purchase.getOrderId());
                                    ocrCountResetViaPurchaseTimeForPremiumYearly(
                                        purchase.purchaseTime,
                                        Constants.PLAN_PEMIUM_YEARLY
                                    )
                                    ocrCountResetIfOrderIdDifferForPremiumYearly(
                                        purchase.orderId,
                                        Constants.PLAN_PEMIUM_YEARLY
                                    )
                                }

                                *//**//*if (Constants.PRODUCT_ID_PREMIUM_QUARTELY.equals(purchase.getSku())) {
                                    Log.i(TAG, "quarterly premium found");

                                    isPremiumFoundQuarterly = true;
                                    prefManager.setOrderIdPremiumQuarterly(purchase.getOrderId());
                                    prefManager.setPlanIdForApi(Constants.PLAN_PEMIUM_QUARTERLY);

                                    prefManager.setPurchasedPlanName(Constants.BUY_NOW_QUARTERLY);
//                                    prefManager.setPremiumYearly(true);

                                    restoreFoundQuarterly = true;

//                                    ocrCountResetViaOrderId(purchase.getOrderId());

                                    ocrCountResetViaPurchaseTimeForPremiumYearly(purchase.getPurchaseTime(), Constants.PLAN_PEMIUM_QUARTERLY);
                                    ocrCountResetIfOrderIdDifferForPremiumYearly(purchase.getOrderId(), Constants.PLAN_PEMIUM_QUARTERLY );
                                }*//**//*
                                if (Constants.PRODUCT_ID_OCR_MONTH == purchase.sku) {
                                    Log.i(TAG, "ocr monthly found")

                                    isOcrMonthlyFound = true
                                    prefManager!!.orderIdOcrMonthly = purchase.orderId

                                    //                                    prefManager.setOcrMonthly(true);
                                    restoreFoundOcrMonthly = true

                                    ocrCountResetViaPurchaseTimeOcrMonthly(
                                        purchase.purchaseTime,
                                        Constants.PLAN_OCR_MONTHLY
                                    )
                                    ocrCountResetIfOrderIdDifferForOcrMonthly(
                                        purchase.orderId,
                                        Constants.PLAN_OCR_MONTHLY
                                    )
                                }
                            }

                            //After loop end
                            prefManager!!.isPremiumYearly = isPremiumFound
                            // prefManager.setPremiumQuarterly(isPremiumFoundQuarterly);
                            prefManager!!.isOcrMonthly = isOcrMonthlyFound

                            topicSubscription(isPremiumFound, isOcrMonthlyFound)
                        } else {
                            prefManager!!.isPremiumYearly = false
                            // prefManager.setPremiumQuarterly(false);
                            prefManager!!.isOcrMonthly = false

                            Log.i(TAG, "both plans not found")
                        }
                    }

                    //                    getInAppProduct();

                    //Check if earlier watermark free purchased
                    if (!prefManager!!.isAppWatermarkFree) {
                        val purchasesResultInApp =
                            billingClient!!.queryPurchases(BillingClient.SkuType.INAPP)
                        if (purchasesResultInApp.purchasesList != null) {
                            val purchasesInApp = purchasesResultInApp.purchasesList
                            if (purchasesInApp!!.size > 0) {
                                Log.i(TAG, "inapp purchase found in restore")
                                for (purchaseInApp in purchasesInApp) {
                                    if (Constants.PRODUCT_ID_WATERMARK_FREE == purchaseInApp.sku) {
                                        Log.i(TAG, "watermark free in restore")
                                        prefManager!!.isAppWatermarkFree = true
                                        //                                        restoreFoundWatermark = true;
                                    }

                                    if (Constants.PRODUCT_ID_AD_FREE == purchaseInApp.sku) {
                                        Log.i(TAG, "ad free in restore")
                                        prefManager!!.isAppAdFree = true
                                    }
                                }
                            }
                        }
                    }
                }*//*

                if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                    Log.i(TAG, "item already owned")
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })*/
    }

    private fun topicSubscription(isPremiumFound: Boolean, isOcrMonthlyFound: Boolean) {
        val subscribeToTopic = SubscribeToTopic(this)

        if (isPremiumFound && isOcrMonthlyFound) {
            subscribeToTopic.doSubscribeToTopic(Constants.SubscribeToTopic.BOTH_PLANS)
            subscribeToTopic.doSubscribeToTestTopic(Constants.SubscribeToTopic.BOTH_PLANS_TEST)
            Log.i(TAG, "Subscribed to Both_plans and both_plans_test")

            subscribeToTopic.doUnsubscribeFromTopic(Constants.SubscribeToTopic.OCR_MONTHLY)
            subscribeToTopic.doUnsubscribeFromTestTopic(Constants.SubscribeToTopic.OCR_MONTHLY_TEST)
            Log.i(TAG, "Unsubscribed to ocr_monthly and ocr_monthly_test")

            subscribeToTopic.doUnsubscribeFromTopic(Constants.SubscribeToTopic.FREE)
            subscribeToTopic.doUnsubscribeFromTestTopic(Constants.SubscribeToTopic.FREE_TEST)
            Log.i(TAG, "Unsubscribed from free  and free_test")

            subscribeToTopic.doSubscribeToTopic(Constants.SubscribeToTopic.PREMIUM_YEARLY)
            subscribeToTopic.doSubscribeToTestTopic(Constants.SubscribeToTopic.PREMIUM_YEARLY_TEST)
            Log.i(TAG, "Subscribed to Premium_yearly and premium_yearly_test")

            prefManager!!.setUnsubscribeFromFree(true)
        } else if (isPremiumFound && !isOcrMonthlyFound) {
            subscribeToTopic.doSubscribeToTopic(Constants.SubscribeToTopic.PREMIUM_YEARLY)
            subscribeToTopic.doSubscribeToTestTopic(Constants.SubscribeToTopic.PREMIUM_YEARLY_TEST)
            Log.i(TAG, "Subscribed to Both_plans and both_plans_test")

            subscribeToTopic.doUnsubscribeFromTopic(Constants.SubscribeToTopic.OCR_MONTHLY)
            subscribeToTopic.doUnsubscribeFromTestTopic(Constants.SubscribeToTopic.OCR_MONTHLY_TEST)
            Log.i(TAG, "Unsubscribed to ocr_monthly and ocr_monthly_test")

            subscribeToTopic.doUnsubscribeFromTopic(Constants.SubscribeToTopic.FREE)
            subscribeToTopic.doUnsubscribeFromTestTopic(Constants.SubscribeToTopic.FREE_TEST)
            Log.i(TAG, "Unsubscribed from free  and free_test")

            subscribeToTopic.doSubscribeToTopic(Constants.SubscribeToTopic.BOTH_PLANS)
            subscribeToTopic.doSubscribeToTestTopic(Constants.SubscribeToTopic.BOTH_PLANS_TEST)

            prefManager!!.setUnsubscribeFromFree(true)
        } else if (isOcrMonthlyFound && !isPremiumFound) {
            subscribeToTopic.doSubscribeToTopic(Constants.SubscribeToTopic.OCR_MONTHLY)
            subscribeToTopic.doSubscribeToTestTopic(Constants.SubscribeToTopic.OCR_MONTHLY_TEST)
            Log.i(TAG, "Subscribed to Both_plans and both_plans_test")

            subscribeToTopic.doUnsubscribeFromTopic(Constants.SubscribeToTopic.PREMIUM_YEARLY)
            subscribeToTopic.doUnsubscribeFromTestTopic(Constants.SubscribeToTopic.PREMIUM_YEARLY_TEST)
            Log.i(TAG, "Unsubscribed to ocr_monthly and ocr_monthly_test")

            subscribeToTopic.doUnsubscribeFromTopic(Constants.SubscribeToTopic.FREE)
            subscribeToTopic.doUnsubscribeFromTestTopic(Constants.SubscribeToTopic.FREE_TEST)
            Log.i(TAG, "Unsubscribed from free  and free_test")

            subscribeToTopic.doSubscribeToTopic(Constants.SubscribeToTopic.BOTH_PLANS)
            subscribeToTopic.doSubscribeToTestTopic(Constants.SubscribeToTopic.BOTH_PLANS_TEST)

            prefManager!!.setUnsubscribeFromFree(true)
        } else {
            prefManager!!.setUnsubscribeFromFree(false)
        }
    }

    private fun ocrCountResetIfOrderIdDifferForOcrMonthly(
        orderIdOcrMonthly: String,
        whichPlan: Int
    ) {
        val existingOrderOcrMonthly = prefManager!!.orderIdOcrMonthly

        if (!orderIdOcrMonthly.equals(existingOrderOcrMonthly, ignoreCase = true)) {
            resetOcrAttempted(whichPlan)
        }
    }

    private fun ocrCountResetIfOrderIdDifferForPremiumYearly(
        orderIdPremium: String,
        whichPlan: Int
    ) {
        var existingOrderPremium: String? = null

        /* if (whichPlan == Constants.PLAN_PEMIUM_QUARTERLY) {
            existingOrderPremium = prefManager.getOrderIdPremiumQuarterly();
        } else*/
        if (whichPlan == Constants.PLAN_PEMIUM_YEARLY) {
            existingOrderPremium = prefManager!!.orderIdPremiumYearly
        }

        if (!orderIdPremium.equals(existingOrderPremium, ignoreCase = true)) {
            resetOcrAttempted(whichPlan)
        }
    }

    private fun ocrCountResetViaPurchaseTimeForPremiumYearly(purchaseTime: Long, whichPlan: Int) {
        var addTime: Long = 0

        val selfAndroidId = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )

        Log.i(TAG, "ocr reset premium, self android_id: $selfAndroidId")

        var isTestDevice = false

        if (dbHandler!!.existDevicesAllowed(selfAndroidId)) {
            isTestDevice = true
        }

        if (isTestDevice) {
            val MILLIS_IN_SECOND: Long = 1000
            val SECONDS_IN_MINUTE: Long = 60

            var planDuration = 0
            /* if (whichPlan == Constants.PLAN_PEMIUM_QUARTERLY) {
                planDuration = 10;
            } else*/
            if (whichPlan == Constants.PLAN_PEMIUM_YEARLY) {
                planDuration = 30
            }

            val MILLISECONDS_FOR_TEST = MILLIS_IN_SECOND * SECONDS_IN_MINUTE * planDuration
            addTime = MILLISECONDS_FOR_TEST
        } else {
            val MILLIS_IN_SECOND: Long = 1000
            val SECONDS_IN_MINUTE: Long = 60
            val MINUTES_IN_HOUR: Long = 60
            val HOURS_IN_DAY: Long = 24
            val DAYS_IN_YEAR = 365 //I know this value is more like 365.24...
            val DAYS_IN_QUARTER = 90

            var planDuration = 0
            /* if (whichPlan == Constants.PLAN_PEMIUM_QUARTERLY) {
                planDuration = DAYS_IN_QUARTER;
            } else*/
            if (whichPlan == Constants.PLAN_PEMIUM_YEARLY) {
                planDuration = DAYS_IN_YEAR
            }

            val MILLISECONDS_FOR_LIVE =
                MILLIS_IN_SECOND * SECONDS_IN_MINUTE * MINUTES_IN_HOUR * HOURS_IN_DAY * planDuration
            addTime = MILLISECONDS_FOR_LIVE
        }

        val existingPremiumCount = prefManager!!.premiumCount

        val currentYear = existingPremiumCount + 1

        val expiryTime = purchaseTime + (addTime * currentYear)

        //        expiryTime = expiryTime * currentYear;
        val currentMillis = System.currentTimeMillis()
        Log.i(TAG, "premium::: expirtyTime: $expiryTime, currentMillis: $currentMillis")

        if (expiryTime < currentMillis) {
            prefManager!!.premiumCount = currentYear
            resetOcrAttempted(whichPlan)
            Log.i(TAG, "ocr premium yearly reset done")
        }
    }

    private fun ocrCountResetViaPurchaseTimeOcrMonthly(purchaseTime: Long, whichPlan: Int) {
        var addTime: Long = 0

        val selfAndroidId = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )

        Log.i(TAG, "ocr monthly, self android_id: $selfAndroidId")

        var isTestDevice = false

        if (dbHandler!!.existDevicesAllowed(selfAndroidId)) {
            isTestDevice = true
        }

        if (isTestDevice) {
            val MILLIS_IN_SECOND: Long = 1000
            val SECONDS_IN_MINUTE: Long = 60
            val MILLISECONDS_IN_30MINUTES = MILLIS_IN_SECOND * SECONDS_IN_MINUTE * 5
            addTime = MILLISECONDS_IN_30MINUTES
        } else {
            val MILLIS_IN_SECOND: Long = 1000
            val SECONDS_IN_MINUTE: Long = 60
            val MINUTES_IN_HOUR: Long = 60
            val HOURS_IN_DAY: Long = 24
            val DAYS_IN_MONTH: Long = 30 //I know this value is more like 365.24...
            val MILLISECONDS_IN_MONTH =
                MILLIS_IN_SECOND * SECONDS_IN_MINUTE * MINUTES_IN_HOUR * HOURS_IN_DAY * DAYS_IN_MONTH
            addTime = MILLISECONDS_IN_MONTH
        }

        //        long expiryTime = purchaseTime + addTime;
        Log.i(TAG, "ocr monthly::: purchaseTime: $purchaseTime, addTime: $addTime")

        val existingOcrMonthlyCount = prefManager!!.ocrMonthlyCount

        Log.i(TAG, "ocr monthly::: existingOcrMonthlyCount: $existingOcrMonthlyCount")

        val currentMonth = existingOcrMonthlyCount + 1

        Log.i(TAG, "ocr monthly::: currentMonth: $currentMonth")

        val expiryTime = purchaseTime + (addTime * currentMonth)
        val currentMillis = System.currentTimeMillis()
        Log.i(
            TAG,
            "ocr monthly::: expirtyTime with month multiply: $expiryTime, currentMillis: $currentMillis"
        )

        if (expiryTime < currentMillis) {
            prefManager!!.ocrMonthlyCount = currentMonth
            resetOcrAttempted(whichPlan)
            Log.i(TAG, "ocr monthly reset done")
        }
    }

    private fun ocrCountResetViaOrderId(orderId: String, whichPlan: Int) {
        val existingPremiumCount = prefManager!!.premiumCount

        if (orderId.contains("..")) {
            val splitted =
                orderId.split("..".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val orderAttempt = splitted[1]
            val orderAttemptInt = orderAttempt.toInt()
            val newOrderAttempt = orderAttemptInt + 1
            prefManager!!.premiumCount = newOrderAttempt
        } else {
            prefManager!!.premiumCount = 1
        }

        val afterUpdate = prefManager!!.premiumCount

        if (afterUpdate > existingPremiumCount) {
            resetOcrAttempted(whichPlan)
        }
    }

    private fun resetOcrAttempted(whichPlan: Int) {
        when (whichPlan) {
            Constants.PLAN_FREE -> resetFreeOcrAttempted()
            Constants.PLAN_OCR_MONTHLY -> resetOcrMonthlyAttempted()
            Constants.PLAN_PEMIUM_YEARLY ->                 //case Constants.PLAN_PEMIUM_QUARTERLY:
                resetOcrPremiumYearlyAttempted()
        }
    }

    private fun resetOcrPremiumYearlyAttempted() {
        if (dbHandler!!.existOcrAttempted()) {
            dbHandler!!.updateOcrAttempt(0, Constants.PLAN_PEMIUM_YEARLY)
            Log.i(TAG, "reset ocr attempted for premium yearly.")
        }
    }

    private fun resetOcrMonthlyAttempted() {
        if (dbHandler!!.existOcrAttempted()) {
            dbHandler!!.updateOcrAttempt(0, Constants.PLAN_OCR_MONTHLY)
            Log.i(TAG, "reset ocr attempted for ocr monthly.")
        }
    }

    private fun resetFreeOcrAttempted() {
        val existingOcrFreeAttempted = dbHandler!!.ocrFreeAttempted

        Log.i(TAG, "existingOcrFreeAttempted: $existingOcrFreeAttempted")

        val allowedFreeOcr = dbHandler!!.allowedFreeOcr

        Log.i(TAG, "allowedFreeOcr: $allowedFreeOcr")

        if (existingOcrFreeAttempted > allowedFreeOcr) {
            if (dbHandler!!.existOcrAttempted()) {
                dbHandler!!.updateOcrAttempt(allowedFreeOcr, Constants.PLAN_FREE)
                Log.i(TAG, "reset ocr attempted for free.")
            }
        }
    }

    private fun clearGooglePlayStoreBillingCacheIfPossible() {
        /*billingClient!!.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP) { billingResult, list ->
            Log.i(
                TAG, "onPurchaseHistoryResponse"
            )
        }*/

        /* billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, (responseCode, purchasesList) -> {
        });*/

        /*billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS, (responseCode, purchasesList) -> {
        });*/
    }

    private fun noAdMoveToNextScreen() {
        Handler().postDelayed({ //                if (!isMoveForwarded) {
            Log.i(
                TAG,
                "Loading Issue Debug:: in noAdMoveToNextScreen::: call moveForward(), prefManager.isSplashDone(): " + prefManager!!.isSplashDone
            )
            if (!prefManager!!.isSplashDone && !AppOpenManager.isShowingAd) {
                Log.i(TAG, "Loading Issue Debug:: in noAdMoveToNextScreen::: call moveForward()")
                moveForward()
            } else {
                Log.i(TAG, "Loading Issue Debug:: in noAdMoveToNextScreen::: else moveForward()")
            }
        }, Constants.NO_AD_SPLASH_TIME_OUT)
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume called")
        if (!isSplashInBackground) {
            isSplashInBackground = false
            if (interstitialAd != null && !prefManager!!.isAppAdFree && util!!.isConnectingToInternet && !prefManager!!.isSplashDone) {
                show()
            }
        } else {
            isSplashInBackground = false
            if (!prefManager!!.isSplashDone) {
                moveForward()
            }
        }

        //  check to show either splash interstitial or splash app open ad
        /*if(!appOpenAdCheck()) {
            if (interstitialAd != null && interstitialAd.isLoaded()) {
                interstitialAd.show();
            }
        }*/
    }

    private fun appOpenAdCheck(): Boolean {
        val isApOpen = prefManager!!.showAppOpenAd()
        Log.e(TAG, "isSplashAdShowStatus$isApOpen")

        val isSplashInterstitialAd = prefManager!!.showSplashInterstitialAd()
        Log.e(TAG, "isSplashInterstitialAd$isSplashInterstitialAd")

        return if (!isApOpen && isSplashInterstitialAd) false // show splash interstitial ad
        else true // show app open ad
    }

    private fun handleLoadingVisibility() {
        if (prefManager!!.isAppAdFree) {
            tv_loading!!.visibility = View.GONE
        } else {
            tv_loading!!.visibility = View.VISIBLE
        }
    }

    private fun findIds() {
        tv_loading = findViewById(R.id.tv_loading)
        progress_lay = findViewById(R.id.progress_lay)
    }

    private fun moveForward() {
        if (!isSplashInBackground) {   // check to prevent app launch automatically from background to foreground.
            Log.i(TAG, "Loading Issue Debug:: inside moveForward()")
            isMoveForwarded = true
            val intent: Intent
            if (!prefManager!!.isFirstTimeLaunched) {
                intent = Intent(this, ProductTourActivity::class.java)
                intent.putExtra(Constants.FROM_NAV, Constants.FROM_SPLASH)
                Log.i(TAG, "Loading Issue Debug:: inside moveForward()::: ProductTourActivity")
            } else {
                intent = Intent(this, HomeActivity::class.java)
                Log.i(TAG, "Loading Issue Debug:: inside moveForward()::: HomeActivity")
            }
            Log.i(TAG, "Loading Issue Debug:: startActivity in moveForward()")
            startActivity(intent)
            finish()
        }
    }

    private fun loadAndShowInterstitialAd() {
        if (interstitialAd == null) {
            progress_lay!!.visibility = View.VISIBLE
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                this,
                BuildConfig.INTERSTITIAL_SPLASH,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(mInterstitialAd: InterstitialAd) {
                        Log.i(TAG, "onAdLoaded")
                        interstitialAd = mInterstitialAd
                        //progress_lay.setVisibility(View.GONE);
                        if (!prefManager!!.isSplashDone) {
                            show()
                        }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.i(
                            TAG,
                            "onAdFailedToLoad  " + loadAdError.message + " errorcode " + loadAdError.code
                        )

                        interstitialAd = null
                        progress_lay!!.visibility = View.GONE
                        noAdMoveToNextScreen()
                    }
                })
        } else {
            progress_lay!!.visibility = View.GONE
            if (!prefManager!!.isSplashDone) {
                show()
            }
        }

        /*interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(BuildConfig.INTERSTITIAL_SPLASH);
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.i(TAG, "Loading Issue Debug:: onAdLoaded called");
                isAdLoaded = true;
                if (interstitialAd.isLoaded()) {
//                    if (!isMoveForwarded) {
                    Log.i(TAG, "Loading Issue Debug:: prefManager.isSplashDone()" + prefManager.isSplashDone());
                    if (!prefManager.isSplashDone() && !Constants.isAppInBackground) {
                        interstitialAd.show();
                    }
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.i(TAG, "Loading Issue Debug:: noAdMoveToNextScreen() from onAdFailedToLoad");
                noAdMoveToNextScreen();
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
                Log.i(TAG, "Loading Issue Debug:: onAdOpened");
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                Log.i(TAG, "Loading Issue Debug:: onAdClicked");
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.i(TAG, "Loading Issue Debug:: onAdLeftApplication");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the interstitial ad is closed.
                Log.i(TAG, "Loading Issue Debug:: onAdClosed, prefManager.isSplashDone(): " + prefManager.isSplashDone());
//                if (!isMoveForwarded) {
                if (!prefManager.isSplashDone() && !Constants.isSplashAdLoad && !prefManager.showAppOpenAd()) {
                    Log.i(TAG, "Loading Issue Debug:: call moveForward() from onAdClosed");
                    moveForward();
                } else {
                    Log.i(TAG, "Loading Issue Debug:: else condition in onAdClosed- moveForward()");
                }

            }
        });*/
    }

    fun show() {
        try {
            if (interstitialAd != null && !isSplashInBackground) {
                isAdLoaded = true
                Log.d(TAG, "show_called")
                interstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        // Called when fullscreen content is dismissed.
                        Log.d(TAG, "The ad was dismissed.")
                        moveForward()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        // Called when fullscreen content failed to show.
                        Log.d(TAG, "The ad failed to show.")
                    }

                    override fun onAdShowedFullScreenContent() {
                        // Called when fullscreen content is shown.
                        // Make sure to set your reference to null so you don't
                        // show it a second time.
                        interstitialAd = null
                        Log.d(TAG, "The ad was shown.")
                    }
                }

                interstitialAd!!.show(this)
            } else {
                Log.d(TAG, "else show.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initObjects() {
        prefManager = PrefManager(this)
        util = FlashScanUtil(this)
        dbHandler = AppController.getINSTANCE().dbHandler
        apiInterface = ApiClient.getClient().create(ApiInterface::class.java)
    }

    override fun onBackPressed() {
    }

    private fun increaseSplashAttempted() {
        val existingSplashAttempted = dbHandler!!.splashAttempted

        Log.i(TAG, "existingSplashAttempted: $existingSplashAttempted")

        val currentAttempt = existingSplashAttempted + 1

        Log.i(TAG, "currentAttempt: $currentAttempt")

        if (dbHandler!!.existSplashAttempted()) {
            dbHandler!!.updateSplashAttempt(currentAttempt)
            Log.i(TAG, "update")
        } else {
            dbHandler!!.insertSplashAttempt(currentAttempt)
            Log.i(TAG, "insert")
        }
    }

    private fun sessionUpdate() {
        if (dbHandler!!.existIntersCreateFreq()) {
            dbHandler!!.updateIntersCreateFreq(0)
            Log.i(TAG, "updated session")
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, list: List<Purchase>?) {
    }

    //    In Case of app close
    private fun loadfiresbasedata() {
        Log.i(TAG, "inside loadfiresbasedata()")
        val data = intent.extras
        Log.i(TAG, "firebase device token:" + prefManager!!.firebaseDeviceToken)
        Log.i(TAG, "notification loadfiresbasedata > ")
        //        getFireToken();
        if (data != null) {
            isFromNotif = true

            val bodyStr1 = data.getString("body")
            val titleStr = data.getString("title")
            Log.i(TAG, "notification bodyStr1 > $bodyStr1")
            Log.i(TAG, "notification title > $titleStr")
            val sid = data.getString("sid")
            val picUrl = data.getString("picture_url")
            val action = data.getString("action")
            val offerUrl = data.getString("offer_url")

            /* String appPackageName = data.getString("app_package_name");

            Log.i(TAG, "appPackageName: " + appPackageName);

            Intent intent;
            try {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
            } catch (android.content.ActivityNotFoundException anfe) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
            }

            startActivity(intent);

            finishAffinity();*/
            prefManager!!.offerUrlServer = offerUrl

            val planId = data.getString("plan_id")
            Log.i(TAG, "notification SID > $sid")
            Log.i(TAG, "notification picUrl > $picUrl")
            Log.i(TAG, "notification offerUrl > $offerUrl")
            screenId = sid
            actionId = action
            if (!TextUtils.isEmpty(offerUrl) && offerUrl!!.length > 10) {
                Log.i(TAG, "startActivity in loadfiresbasedata offerurl present")
                goToScreensIntentWithOffer(planId)
            } else {
                if (screenId != null) {
                    // Other notifications click case
                    Log.i(TAG, "startActivity in loadfiresbasedata screenId not null")
                    startActivity(goToScreensIntent(planId))
                    finish()
                    Log.i(TAG, "notification data found and screenid = $screenId")
                } else {
                    // Normal flow
                    Log.i(TAG, "notification data found but screenid not found")
                }
            }
        } else {
            // Normal flow
            Log.i(TAG, "notification data not found")
            val url = ""
            //            url = "https://astroproducts.s3.amazonaws.com/hor/appads/andr/html/TarotLife_Offer_Ask_Tarot_1_en.html";
            prefManager!!.offerUrlServer = url
        }
        Log.i(TAG, "completed loadfiresbasedata()")
    }

    private fun goToScreensIntent(planIdStr: String?): Intent {
        Log.i(TAG, "Loading Issue Debug:: =======     goToScreensIntentWithOffer")
        /*int planId = 0;
        if (planIdStr != null) {
            try {
                planId = Integer.parseInt(planIdStr);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if (planId == Constants.PLAN_PEMIUM_YEARLY) {
            defaultIntent = new Intent(this, PremiumActivity.class);
        } else if (planId == Constants.PLAN_OCR_MONTHLY) {
            defaultIntent = new Intent(this, OcrPlanDialog.class);
        } else {
            defaultIntent = new Intent(this, HomeActivity.class);
        }*/
        val defaultIntent = Intent(this, HomeActivity::class.java)
        defaultIntent.putExtra(Constants.EXTRA_PLAN_ID_IN_NOTIF, planIdStr)
        defaultIntent.putExtra(Constants.EXTRA_BACKSTACKOFFER, true)
        defaultIntent.putExtra(Constants.EXTRA_BACKSTACK, true)
        defaultIntent.putExtra(Constants.FROM_NAV, Constants.FROM_NOTIF)
        defaultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        defaultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return defaultIntent
    }

    private fun goToScreensIntentWithOffer(planIdStr: String?) {
        finish()
        Log.i(TAG, "Loading Issue Debug:: =======     goToScreensIntentWithOffer")
        val defaultIntent = Intent(this, HomeActivity::class.java)
        defaultIntent.putExtra(Constants.EXTRA_PLAN_ID_IN_NOTIF, planIdStr)
        defaultIntent.putExtra(Constants.EXTRA_BACKSTACKOFFER, true)
        defaultIntent.putExtra(Constants.EXTRA_BACKSTACK, true)
        defaultIntent.putExtra(Constants.FROM_NAV, Constants.FROM_NOTIF)
        defaultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        defaultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(defaultIntent)
    }

    private val fireToken: Unit
        get() {
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.i(TAG, "getInstanceId failed", task.exception)
                        return@OnCompleteListener
                    }
                    // Get new Instance ID token
                    val token = Objects.requireNonNull(task.result).token
                    // Log and toast
                    Constants.NOTIFY_TOKEN = token
                    Log.i(TAG, "firebase device token getting explicitly:$token")
                })
        }

    override fun onStop() {
        super.onStop()
        isSplashInBackground = true
    }

    companion object {
        private val TAG: String = SplashActivity::class.java.simpleName
    }
}
