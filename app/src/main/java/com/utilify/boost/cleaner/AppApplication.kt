package com.utilify.boost.cleaner

import android.R
import android.app.Activity
import android.app.Application
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.LifecycleObserver
import com.advanced.phone.junk.cache.cleaner.booster.antimalware.AdvancedPhoneCleaner
import dagger.hilt.android.HiltAndroidApp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import com.cam.scanner.scantopdf.android.AppController


@HiltAndroidApp
class AppApplication : Application(), Application.ActivityLifecycleCallbacks,
    LifecycleObserver {

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(this)

        //Flash Scan AppController
        AppController.getINSTANCE().onCreate(applicationContext)
        AdvancedPhoneCleaner.getInstance().onCreate(applicationContext)

        //MobileAds.initialize(this)

        //FirebaseApp.initializeApp(applicationContext)
        //FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true
        //CommonMethods.mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        //CommonMethods.generateFCMTokenIfEmpty(applicationContext)

        //FirebaseDbConfig.initialize()
        com.itl.commonres.firebaseUtils.FirebaseDbConfig.initialize()
        //CommonMethods.loadAppOpenAd(applicationContext)
        //CommonMethods.loadInterstitialAd(applicationContext)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        setStatusBarColor(activity, Color.WHITE)
    }

    fun setStatusBarColor(activity: Activity, color: Int) {
        val window = activity.window
        window.statusBarColor = color
        
        val decorView = window.decorView
        val isLightColor = Color.luminance(color) > 0.5
        ViewCompat.getWindowInsetsController(decorView)?.isAppearanceLightStatusBars = isLightColor

        // Global fix for Android 15+ forced edge-to-edge:
        // Apply padding to the root content view so it stays within system bars.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val rootView = decorView.findViewById<View>(R.id.content)
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

}