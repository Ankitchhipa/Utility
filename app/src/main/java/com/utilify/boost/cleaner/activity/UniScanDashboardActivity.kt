package com.utilify.boost.cleaner.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.advanced.phone.junk.cache.cleaner.booster.antimalware.AdvancedPhoneCleaner
import com.advanced.phone.junk.cache.cleaner.booster.antimalware.HomeActivity
import com.advanced.phone.junk.cache.cleaner.booster.antimalware.tracking.CountryCode
import com.advanced.phone.junk.cache.cleaner.booster.antimalware.utility.SharedPrefUtil
import com.cam.scanner.scantopdf.android.util.PrefManager
import com.itl.commonres.appinterface.OnAdDismissInterface
import com.itl.commonres.utils.AdsPlacementsEnum
import com.itl.commonres.utils.CommonMethods
import com.itl.commonres.utils.CommonMethods.multipleClicked
import com.itl.commonres.utils.Constants
import com.itl.commonres.utils.InterstitialAdCappingEnum
import com.itl.commonres.utils.OnClickEnum
import com.itl.commonres.utils.PermissionInterface
import com.utilify.boost.cleaner.BannerEnum
import com.utilify.boost.cleaner.BuildConfig
import com.utilify.boost.cleaner.R
import com.utilify.boost.cleaner.adapter.RecommendeAppAdapter
import com.utilify.boost.cleaner.databinding.ActivityUniScanDashboardBinding


class UniScanDashboardActivity : AppCompatActivity(), PermissionInterface, OnAdDismissInterface {

    private lateinit var binding: ActivityUniScanDashboardBinding

    private var screenName = "UniScanDashboardActivity"
    private var sharedPrefUtil: SharedPrefUtil? = null
    private var prefManager: PrefManager? = null
    private var commonMethods: CommonMethods? = null
    private var isPauseForPermissions = false
    private var onClickItem = -1
    private var list: ArrayList<Int> =
        arrayListOf(BannerEnum.AnytimeAstro.value,BannerEnum.TarotLife.value)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUniScanDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start Animation for cl_container
        val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down_anim)
        slideDown.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                binding.dashboard.nestedScroll.visibility = View.VISIBLE
                val slideUp = AnimationUtils.loadAnimation(this@UniScanDashboardActivity, R.anim.slide_up_anim)
                binding.dashboard.nestedScroll.startAnimation(slideUp)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        binding.dashboard.clContainer.startAnimation(slideDown)


        initObjects()
        setScrollingListener()
        initAPC()
        clickListener()
        setRecommendedAppsAdapter()

        requestNotificationPermission()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog()
            }
        })
    }

    override fun onStart() {
        super.onStart()
    }

    private fun setRecommendedAppsAdapter() {
        binding.dashboard.rvRecommendedApp.adapter = RecommendeAppAdapter(list,this)
    }

    /*private fun loadAds() {
        if(AppUtils.isNetworkConnected(this@UniScanDashboardActivity)){
            CommonMethods.loadInterstitialAd(this, false)
        }
    }*/

    /*override fun onBackPressed() {
        if (binding.main.isDrawerOpen(GravityCompat.START)) {
            binding.main.closeDrawers()
            return
        }
        showExitDialog()
    }*/

    /*private fun loadView() {
        if (AppUtils.isNetworkConnected(this@UniScanDashboardActivity) && Constants.isAdShow) {
            AdUtils.loadBottomNativeAd(
                this,
                BuildConfig.AD_UNIT_ID_NATIVE_AD_HOME,
                binding.dashboard.adPlaceholder.root
            )
            binding.dashboard.adPlaceholder.root.visibility = View.VISIBLE
        } else {
            binding.dashboard.adPlaceholder.root.visibility = View.GONE
            Log.e("PhoneMate : ", "Ad Not Showing")
        }
    }*/

    private fun initObjects() {
        sharedPrefUtil = SharedPrefUtil(this)
        prefManager = PrefManager(this)
        commonMethods = CommonMethods(this)

        if(!Constants.isAdShow){
            binding.dashboard.tvRecommendedApps.visibility = View.GONE
            binding.dashboard.rvRecommendedApp.visibility = View.GONE
        }
        //binding.dashboard.toolbar.ads.visibility = View.VISIBLE
    }

    private fun setScrollingListener() {
        binding.dashboard.nestedScroll.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > binding.dashboard.cleanShareScanContent.y) { // Scrolling down
                binding.dashboard.toolbar.title.text = getString(R.string.app_name)
            } else {
                binding.dashboard.toolbar.title.text = ""
            }
        })
    }

    private fun initAPC() {
        if (sharedPrefUtil!!.getString(SharedPrefUtil.COUNTRYNAME) == null) {
            startService(Intent(this, CountryCode::class.java))
        }
    }

    private fun clickListener() {
        binding.dashboard.apply {
            menu.setOnClickListener {
                val popup = PopupMenu(this@UniScanDashboardActivity, it)
                popup.menuInflater.inflate(R.menu.dashboard_menu, popup.menu)

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_refer -> {
                            // Handle Refer and Earn
                            val shareIntent = Intent(Intent.ACTION_SEND)
                            shareIntent.type = "text/plain"
                            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this awesome app: https://play.google.com/store/apps/details?id=$packageName")
                            startActivity(Intent.createChooser(shareIntent, "Refer via"))
                            true
                        }
                        R.id.menu_privacy -> {
                            // Handle Privacy Policy (usually a URL)
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://your-privacy-policy-link.com"))
                            startActivity(browserIntent)
                            true
                        }
                        R.id.menu_setting -> {
                            startActivity(Intent(this@UniScanDashboardActivity, SettingsMainActivity::class.java))
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }

            boost.setOnClickListener {
                if (multipleClicked()) return@setOnClickListener
                onClickItem = OnClickEnum.BoostX.value
                if (isStoragePermissionGranted()) {

                    //log event
                    CommonMethods.logCustomFireBaseEvents(
                        screenName,
                        Constants.OPEN_BOOSTX
                    )

                    if (CommonMethods.isNetworkConnected(this@UniScanDashboardActivity) && Constants.isAdShow && CommonMethods.isAdActive(
                            AdsPlacementsEnum.DASHBOARD_BOOSTX.value
                        ) && CommonMethods.isShowInterstitialAdDashboard(
                            OnClickEnum.BoostX.value,
                            InterstitialAdCappingEnum.DASHBOARD_BOOSTX.value
                        )
                    ) {
                        Constants.AdInterstitialCappingArrayList[InterstitialAdCappingEnum.DASHBOARD_BOOSTX.value] =
                            Constants.AdInterstitialCappingArrayList[InterstitialAdCappingEnum.DASHBOARD_BOOSTX.value] + 1
                        showAd()
                    } else {
                        Log.e("Mobibuz : ", "Ad Not Showing")
                        proceedForModuleClick()
                    }
                }
            }

            docScan.setOnClickListener {
                if (multipleClicked()) return@setOnClickListener
                onClickItem = OnClickEnum.ScanHub.value
                if (isStoragePermissionGranted()) {
                    //log event
                    CommonMethods.logCustomFireBaseEvents(
                        screenName,
                        Constants.OPEN_SCAN_HUB
                    )
                    if (CommonMethods.isNetworkConnected(this@UniScanDashboardActivity) && Constants.isAdShow && CommonMethods.isAdActive(
                            AdsPlacementsEnum.DASHBOARD_SCANHUB.value
                        ) && CommonMethods.isShowInterstitialAdDashboard(
                            OnClickEnum.ScanHub.value,
                            InterstitialAdCappingEnum.DASHBOARD_SCANHUB.value
                        )
                    ) {
                        Constants.AdInterstitialCappingArrayList[InterstitialAdCappingEnum.DASHBOARD_SCANHUB.value] =
                            Constants.AdInterstitialCappingArrayList[InterstitialAdCappingEnum.DASHBOARD_SCANHUB.value]+1
                        showAd()
                    } else {
                        Log.e("Mobibuz : ", "Ad Not Showing")
                        proceedForModuleClick()
                    }
                }
            }
        }
        binding.dashboard.toolbar.apply {
            ivMenu.setOnClickListener {
                if (multipleClicked()) return@setOnClickListener
                startActivity(Intent(this@UniScanDashboardActivity, SettingsMainActivity::class.java))
            }
            /*ads.setOnClickListener {
                if (multipleClicked()) return@setOnClickListener
                startActivity(Intent(this@UniScanDashboardActivity, PurchaseScreen::class.java))
            }*/
        }
    }

    fun bytesToGB(bytes: Long): Double {
        return bytes.toDouble() / (1024 * 1024 * 1024)
    }

    private fun showExitDialog() {
        CommonMethods(this).showExitDialog {
            finishAffinity()
        }
    }

    override fun onResume() {
        super.onResume()
        //setStorageSpace()
    }

    /*private fun setStorageSpace() {
        object : RamCalculation(this) {
            override fun onPostExecute(strings: Array<String>) {
                super.onPostExecute(strings)
                try {
                    setProgressView(strings)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.execute()
    }*/

    /* private fun setProgressView(strings: Array<String>) {
         val memPerExternal = strings[1].toInt()
         val totRam = strings[4]
         val availableRAMSize = strings[5]

         val ramSize = availableRAMSize.replace(" GB", "").toFloat().roundToInt()
         val totalRamSize = totRam.replace(" GB", "").toFloat().roundToInt()

         binding.dashboard.availableSpacePercentage.text = "$memPerExternal%"
         binding.dashboard.availableSpace.text =
             "$ramSize GB/$totalRamSize GB"

         binding.dashboard.progress.progress = memPerExternal

         var progressSpace = 0
         val handler = Handler()

         handler.post(object : Runnable {
             override fun run() {
                 progressSpace += 1
                 if (progressSpace <= memPerExternal) {
                     binding.dashboard.progress.progress = progressSpace
                     handler.postDelayed(this, 30)
                     binding.dashboard.availableSpacePercentage.text = "$progressSpace%"
                 }
             }
         })
     }
 */
    private fun isStoragePermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                isPauseForPermissions = false
                //onClickItem = -1
                return true
            } else {
                CommonMethods(this).showPermissionDialog(
                    "All_Files_Access",
                    this,
                    onClickItem != OnClickEnum.ScanHub.value
                )
                return false
            }
        } else {
            isPauseForPermissions = false
            //onClickItem = -1
            return true
        }
    }

    override fun onPermissionClickOkay(isAllFilesAccess: Boolean, context: Context) {
        isPauseForPermissions = true
        CommonMethods(this).processPermission(isAllFilesAccess, context)
    }

    override fun onPermissionClickNotNow(context: Context) {
        proceedForModuleClick()
    }

    private fun showAd() {
        CommonMethods.onAdDismissInterface = this
        if (Constants.interstitialAd != null) {
            Constants.interstitialAd.show(this)
        } else {
            CommonMethods.loadInterstitialAd(
                this,
                true,
                BuildConfig.AD_UNIT_ID_MODULE_INTERSTITIAL_AD
            )
        }
    }

    override fun onAdDismiss() {
        Log.e("onAdDismiss", "onAdDismiss: call $onClickItem")
        proceedForModuleClick()
    }

    private fun proceedForModuleClick() {
        when (onClickItem) {
            OnClickEnum.ScanHub.value -> {
                startActivity(
                    Intent(
                        this,
                        com.cam.scanner.scantopdf.android.activities.HomeActivity::class.java
                    )
                )
            }

            OnClickEnum.BoostX.value -> {
                startActivity(Intent(this@UniScanDashboardActivity, HomeActivity::class.java))
            }
        }
    }

    private val requestNotificationPermissions = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        AdvancedPhoneCleaner.IS_NOTIFICATION_ALLOWED = isGranted
    }

    private fun hasNotificationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestNotificationPermission() {
        if (hasNotificationPermission()) {
            AdvancedPhoneCleaner.IS_NOTIFICATION_ALLOWED = true
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
        ) {
            requestNotificationPermissions.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            requestNotificationPermissions.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

}