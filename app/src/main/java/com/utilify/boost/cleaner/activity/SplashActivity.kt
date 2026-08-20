package com.utilify.boost.cleaner.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.cam.scanner.scantopdf.android.util.PremiumStatusHelper
import com.cam.scanner.scantopdf.android.util.PrefManager
import com.itl.commonres.firebaseUtils.ConfigFilesUpdateHelper
import com.itl.commonres.firebaseUtils.FirebaseConstants
import com.itl.commonres.firebaseUtils.FirebaseDbConfig
import com.itl.commonres.utils.Constants
import com.itl.commonres.utils.SharedPrefUtil
import com.utilify.boost.cleaner.databinding.ActivitySplashBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity(), PurchasesUpdatedListener {

    private lateinit var binding: ActivitySplashBinding
    private var countDownTimer: CountDownTimer? = null
    private var handler: Handler? = null
    protected var runnable: Runnable = Runnable { this.startDashboard() }
    private val tagName = SplashActivity::class.java.simpleName
    private var sharedPrefUtil: SharedPrefUtil? = null
    private var prefManager: PrefManager? = null
    private var billingClient: BillingClient? = null
    private var isBillingChecked = false
    private var isSplashTimerFinished = false
    private var isDashboardStarted = false
    private var floatingAnimator: AnimatorSet? = null

    @Inject
    lateinit var configFilesUpdateHelper: ConfigFilesUpdateHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handler = Handler(Looper.getMainLooper())

        setAllAnimation()
        runCountDownTimer()
        initObjects()
        
        checkPremiumStatus()
        
        if (FirebaseDbConfig.isInitialized) {
            getConfigValuesFromFirebaseSingleValueEventListener()
        }
    }

    private fun initObjects() {
        sharedPrefUtil = SharedPrefUtil(this)
        prefManager = PrefManager(this)
        Constants.isFirstLaunch = sharedPrefUtil?.isFirstLaunch() ?: false
        
        if (prefManager?.secureAndroidId == null) {
            val selfAndroidId = android.provider.Settings.Secure.getString(
                contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )
            prefManager?.secureAndroidId = selfAndroidId
        }
    }

    private fun checkPremiumStatus() {
        /*if (!com.cam.scanner.scantopdf.android.util.Constants.IS_ORDER_REAL) {
            isBillingChecked = true
            return
        }*/

        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .setListener(this).build()

        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val params = QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()

                    billingClient!!.queryPurchasesAsync(params) { result: BillingResult, purchaseList: List<Purchase> ->
                        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                            PremiumStatusHelper.applySubscriptionPurchases(prefManager, purchaseList)
                        }
                        markBillingChecked()
                    }
                } else {
                    markBillingChecked()
                }
            }

            override fun onBillingServiceDisconnected() {
                markBillingChecked()
            }
        })
    }

    private fun markBillingChecked() {
        isBillingChecked = true
        openDashboardIfReady()
    }

    private fun setAllAnimation() {
        val smoothInterpolator = AccelerateDecelerateInterpolator()
        val bounceInterpolator = OvershootInterpolator(1.15f)

        binding.topBadge.apply {
            alpha = 0f
            translationY = -36f
            scaleX = 0.92f
            scaleY = 0.92f
        }

        binding.headerView.apply {
            alpha = 0f
            translationY = 58f
        }

        binding.logoCard.apply {
            alpha = 0f
            scaleX = 0.82f
            scaleY = 0.82f
            rotation = -6f
        }

        binding.appIcon.apply {
            alpha = 0f
            scaleX = 0.82f
            scaleY = 0.82f
        }

        binding.appTitle.apply {
            alpha = 0f
            translationY = 30f
        }

        binding.tagCard.apply {
            alpha = 0f
            translationY = 38f
            scaleX = 0.96f
            scaleY = 0.96f
        }

        binding.footerTag.apply {
            alpha = 0f
            translationY = 20f
        }

        binding.topBadge.animate()
            .alpha(1f)
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(520L)
            .setInterpolator(smoothInterpolator)
            .start()

        binding.headerView.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(100L)
            .setDuration(760L)
            .setInterpolator(smoothInterpolator)
            .start()

        binding.logoCard.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .rotation(0f)
            .setStartDelay(120L)
            .setDuration(900L)
            .setInterpolator(bounceInterpolator)
            .withEndAction { startFloatingAnimation() }
            .start()

        binding.appIcon.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(220L)
            .setDuration(700L)
            .setInterpolator(bounceInterpolator)
            .start()

        binding.appTitle.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(340L)
            .setDuration(560L)
            .setInterpolator(smoothInterpolator)
            .start()

        binding.tagCard.animate()
            .alpha(1f)
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(500L)
            .setDuration(620L)
            .setInterpolator(bounceInterpolator)
            .start()

        binding.footerTag.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(700L)
            .setDuration(480L)
            .setInterpolator(smoothInterpolator)
            .start()
    }

    private fun startFloatingAnimation() {
        floatingAnimator?.cancel()

        val cardFloat = ObjectAnimator.ofFloat(binding.logoCard, View.TRANSLATION_Y, 0f, -10f, 0f).apply {
            duration = 2800L
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        val iconPulseX = ObjectAnimator.ofFloat(binding.appIcon, View.SCALE_X, 1f, 1.05f, 1f).apply {
            duration = 2200L
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        val iconPulseY = ObjectAnimator.ofFloat(binding.appIcon, View.SCALE_Y, 1f, 1.05f, 1f).apply {
            duration = 2200L
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        floatingAnimator = AnimatorSet().apply {
            playTogether(cardFloat, iconPulseX, iconPulseY)
            start()
        }
    }

    private fun runCountDownTimer() {
        countDownTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                isSplashTimerFinished = true
                openDashboardIfReady()
                handler?.postDelayed({
                    if (!isBillingChecked) {
                        markBillingChecked()
                    }
                }, 1500)
            }
        }
        countDownTimer?.start()
    }

    private fun openDashboardIfReady() {
        if (!isSplashTimerFinished || !isBillingChecked || isDashboardStarted) {
            return
        }

        isDashboardStarted = true
        handler?.postDelayed(runnable, 120)
    }

    private fun startDashboard() {
        val data: Bundle? = intent.extras
        val sid = data?.getString("sid", "")
        Log.e(tagName, "Notification sid =====: $sid")

        openDashboard()
    }

    private fun openDashboard() {
        if (FirebaseConstants.isTutorialScreenShow && Constants.isFirstLaunch) {
            startActivity(Intent(this, TutorialActivity::class.java))
        } else {
            startActivity(Intent(this, UniScanDashboardActivity::class.java))
        }
        finishAffinity()
    }

    private fun getConfigValuesFromFirebaseSingleValueEventListener() {
        configFilesUpdateHelper.adShowEventListener()
        configFilesUpdateHelper.adShowHexEventListener()
        configFilesUpdateHelper.adShowInterstitialLongEventListener()
        configFilesUpdateHelper.adScanHubFirstLaunchEventListener()
        configFilesUpdateHelper.adScanHubSecondLaunchEventListener()
        configFilesUpdateHelper.adBoostXFirstLaunchEventListener()
        configFilesUpdateHelper.adBoostXSecondLaunchEventListener()
        configFilesUpdateHelper.tutorialScreenShowLaunchEventListener()
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
    }

    override fun onDestroy() {
        billingClient?.endConnection()
        floatingAnimator?.cancel()
        countDownTimer?.cancel()
        handler?.removeCallbacks(runnable)
        super.onDestroy()
    }
}
