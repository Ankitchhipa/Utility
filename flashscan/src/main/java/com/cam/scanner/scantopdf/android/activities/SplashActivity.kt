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
import com.android.billingclient.api.QueryPurchasesParams
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

        findIds()
        initObjects()

        prefManager!!.isSplashDone = false
        androidIdInPrefs()

        if (Constants.IS_REMOTE_CONFIG_FROM_OWN_API) {
            plansFromApi
        }

        if (Constants.IS_ORDER_REAL) {
            checkPremiumStatus()
        } else {
            setVirtualOrderFound()
        }

        sessionUpdate()

        var trackingdone = prefManager!!.isTrackingDone
        if (BuildConfig.DEBUG) {
            trackingdone = true
        }

        if (!trackingdone) {
            AfterIntsall().getInstallDetails(this@SplashActivity)
        }

        val showIntersSplash = dbHandler!!.showIntesSplash()
        var canShow: Boolean
        val intersSplashAfter = dbHandler!!.intersSplashAfter()

        increaseSplashAttempted()

        if (dbHandler!!.splashAttempted > intersSplashAfter) {
            canShow = true
        } else {
            canShow = false
        }

        loadfiresbasedata()

        if (!prefManager!!.isAppAdFree && showIntersSplash && canShow && !isFromNotif && !appOpenAdCheck()) {
            if (util!!.isConnectingToInternet) {
                loadAndShowInterstitialAd()
                Handler().postDelayed({
                    if (!isAdLoaded && !prefManager!!.isSplashDone && !Constants.isSplashAdLoad && !prefManager!!.showAppOpenAd()) {
                        moveForward()
                    }
                }, Constants.SPLASH_TIME_OUT)
            } else {
                noAdMoveToNextScreen()
            }
        } else {
            noAdMoveToNextScreen()
        }
    }

    private fun loadFirebaseDataAndCheckRedirection(): Boolean {
        var needRedirection = false
        val data = intent.extras
        if (data != null) {
            val appPackageName = data.getString("app_package_name")
            if (appPackageName != null && !appPackageName.isEmpty()) {
                needRedirection = true
                val intent = try {
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
        prefManager!!.setSecureAndroidId(selfAndroidId)
    }

    private fun setVirtualOrderFound() {
        prefManager!!.isPremiumYearly = Constants.IN_DUMMY_AT_SPLASH_PREMIUM_YEARLY_FOUND
        if (Constants.IN_DUMMY_AT_SPLASH_PREMIUM_YEARLY_FOUND) {
            prefManager!!.orderIdPremiumYearly = Constants.LAST_TESTED_ORDER_ID_PREMIUM_YEARLY
            prefManager!!.planIdForApi = Constants.PLAN_PEMIUM_YEARLY
        }

        prefManager!!.isOcrMonthly = Constants.IN_DUMMY_AT_SPLASH_OCR_MONTHLY_FOUND
        if (Constants.IN_DUMMY_AT_SPLASH_OCR_MONTHLY_FOUND) {
            prefManager!!.orderIdOcrMonthly = Constants.LAST_TESTED_ORDER_ID_OCR_MONTHLY
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
                        val getPlans = response.body()
                        putInDbOCRRules(getPlans)
                    }

                    override fun onFailure(call: Call<GetPlans?>, t: Throwable) {
                        call.cancel()
                    }
                })
            } else {
                try {
                    val gson = Gson()
                    val getPlans = gson.fromJson(loadJSONFromAsset(), GetPlans::class.java)
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

    private fun checkPremiumStatus() {
        if (util!!.isConnectingToInternet) {
            connectBillingService()
        }
    }

    private fun connectBillingService() {
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases(com.android.billingclient.api.PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .setListener(this).build()

        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val params = QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()

                    billingClient!!.queryPurchasesAsync(params) { result, purchases ->
                        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                            var isPremiumFound = false
                            var isMonthlyFound = false

                            for (purchase in purchases) {
                                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                                    val products = purchase.products
                                    if (products.contains(Constants.PRODUCT_ID_PREMIUM)) {
                                        isPremiumFound = true
                                    }
                                    if (products.contains(Constants.PRODUCT_ID_MONTHLY)) {
                                        isMonthlyFound = true
                                    }
                                }
                            }
                            prefManager!!.isPremiumYearly = isPremiumFound
                            prefManager!!.isPremiumMonthly = isMonthlyFound
                            topicSubscription(isPremiumFound || isMonthlyFound)
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
            }
        })
    }

    private fun topicSubscription(isPremiumFound: Boolean) {
        val subscribeToTopic = SubscribeToTopic(this)
        if (isPremiumFound) {
            subscribeToTopic.doUnsubscribeFromTopic(Constants.SubscribeToTopic.FREE)
            subscribeToTopic.doSubscribeToTopic(Constants.SubscribeToTopic.BOTH_PREMIUM)
            prefManager!!.setUnsubscribeFromFree(true)
        } else {
            prefManager!!.setUnsubscribeFromFree(false)
        }
    }

    private fun noAdMoveToNextScreen() {
        Handler().postDelayed({
            if (!prefManager!!.isSplashDone && !AppOpenManager.isShowingAd) {
                moveForward()
            }
        }, Constants.NO_AD_SPLASH_TIME_OUT)
    }

    override fun onResume() {
        super.onResume()
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
    }

    private fun appOpenAdCheck(): Boolean {
        val isApOpen = prefManager!!.showAppOpenAd()
        val isSplashInterstitialAd = prefManager!!.showSplashInterstitialAd()
        return if (!isApOpen && isSplashInterstitialAd) false
        else true
    }

    private fun findIds() {
        tv_loading = findViewById(R.id.tv_loading)
        progress_lay = findViewById(R.id.progress_lay)
    }

    private fun moveForward() {
        if (!isSplashInBackground) {
            isMoveForwarded = true
            val intent: Intent
            if (!prefManager!!.isFirstTimeLaunched) {
                intent = Intent(this, ProductTourActivity::class.java)
                intent.putExtra(Constants.FROM_NAV, Constants.FROM_SPLASH)
            } else {
                intent = Intent(this, HomeActivity::class.java)
            }
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
                        interstitialAd = mInterstitialAd
                        if (!prefManager!!.isSplashDone) {
                            show()
                        }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
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
    }

    fun show() {
        try {
            if (interstitialAd != null && !isSplashInBackground) {
                isAdLoaded = true
                interstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        moveForward()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    }

                    override fun onAdShowedFullScreenContent() {
                        interstitialAd = null
                    }
                }
                interstitialAd!!.show(this)
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
        val currentAttempt = existingSplashAttempted + 1
        if (dbHandler!!.existSplashAttempted()) {
            dbHandler!!.updateSplashAttempt(currentAttempt)
        } else {
            dbHandler!!.insertSplashAttempt(currentAttempt)
        }
    }

    private fun sessionUpdate() {
        if (dbHandler!!.existIntersCreateFreq()) {
            dbHandler!!.updateIntersCreateFreq(0)
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, list: List<Purchase>?) {
    }

    private fun loadfiresbasedata() {
        val data = intent.extras
        if (data != null) {
            isFromNotif = true
            val offerUrl = data.getString("offer_url")
            prefManager!!.offerUrlServer = offerUrl
            val sid = data.getString("sid")
            val action = data.getString("action")
            val planId = data.getString("plan_id")
            screenId = sid
            actionId = action
            if (!TextUtils.isEmpty(offerUrl) && offerUrl!!.length > 10) {
                goToScreensIntentWithOffer(planId)
            } else {
                if (screenId != null) {
                    startActivity(goToScreensIntent(planId))
                    finish()
                }
            }
        } else {
            prefManager!!.offerUrlServer = ""
        }
    }

    private fun goToScreensIntent(planIdStr: String?): Intent {
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
        val defaultIntent = Intent(this, HomeActivity::class.java)
        defaultIntent.putExtra(Constants.EXTRA_PLAN_ID_IN_NOTIF, planIdStr)
        defaultIntent.putExtra(Constants.EXTRA_BACKSTACKOFFER, true)
        defaultIntent.putExtra(Constants.EXTRA_BACKSTACK, true)
        defaultIntent.putExtra(Constants.FROM_NAV, Constants.FROM_NOTIF)
        defaultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        defaultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(defaultIntent)
    }

    override fun onStop() {
        super.onStop()
        isSplashInBackground = true
    }

    companion object {
        private val TAG: String = SplashActivity::class.java.simpleName
    }
}
