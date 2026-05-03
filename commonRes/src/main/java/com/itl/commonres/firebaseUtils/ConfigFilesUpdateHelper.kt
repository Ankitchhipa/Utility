package com.itl.commonres.firebaseUtils

import android.content.Context
import android.util.Log
import androidx.annotation.NonNull
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.itl.commonres.utils.CommonMethods
import com.itl.commonres.utils.Constants
import com.itl.commonres.utils.SharedPrefUtil
import javax.inject.Inject


class ConfigFilesUpdateHelper @Inject constructor(
    context: Context,
    sharedPrefUtil: SharedPrefUtil
) {

    private val TAG = ConfigFilesUpdateHelper::class.java.simpleName
    var firebaseDbConfig = FirebaseDbConfig


    fun adShowHexEventListener() {
        if (!firebaseDbConfig.isInitialized) return
        val adShowHexEventListener = object : ValueEventListener {
            override fun onDataChange(@NonNull snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.getValue(String::class.java)?.let { value ->
                        Log.e(TAG, "adShowHexEventListener::$value")
                        FirebaseConstants.HEX_SHOW_AD = value
                        CommonMethods.hexToBinary()
                    }
                }
            }

            override fun onCancelled(@NonNull error: DatabaseError) {
                Log.e(TAG, "adShowHexEventListener::onCancelled:${error.message}")
            }
        }
        firebaseDbConfig.configRef.child(FirebaseConstants.Ad_SHOW_HEX)
            .addListenerForSingleValueEvent(adShowHexEventListener)
    }

    fun adShowEventListener() {
        if (!firebaseDbConfig.isInitialized) return
        val adShowEventListener = object : ValueEventListener {
            override fun onDataChange(@NonNull snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.getValue(Boolean::class.java)?.let { value ->
                        Log.e(TAG, "adShowEventListener::$value")
                        Constants.isAdShow = value
                    }
                }
            }

            override fun onCancelled(@NonNull error: DatabaseError) {
                Log.e(TAG, "adShowEventListener::onCancelled:${error.message}")
            }
        }
        firebaseDbConfig.configRef.child(FirebaseConstants.Ad_SHOW)
            .addListenerForSingleValueEvent(adShowEventListener)
    }

    fun adShowInterstitialLongEventListener() {
        if (!firebaseDbConfig.isInitialized) return
        val adShowInterstitialHexEventListener = object : ValueEventListener {
            override fun onDataChange(@NonNull snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.getValue(Long::class.java)?.let { value ->
                        Log.e(TAG, "adShowInterstitialHexEventListener::$value")
                        FirebaseConstants.INTERSTITIAL_SHOW_AD = value
                        CommonMethods.createInterstitialAdArrayList()
                    }
                }
            }

            override fun onCancelled(@NonNull error: DatabaseError) {
                Log.e(TAG, "adShowInterstitialHexEventListener::onCancelled:${error.message}")
            }
        }
        firebaseDbConfig.configRef.child(FirebaseConstants.AD_FULL_SCREEN_CAPPING)
            .addListenerForSingleValueEvent(adShowInterstitialHexEventListener)
    }

    fun adScanHubFirstLaunchEventListener() {
        if (!firebaseDbConfig.isInitialized) return
        val adScanHubFirstLaunchEventListener = object : ValueEventListener {
            override fun onDataChange(@NonNull snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.getValue(Boolean::class.java)?.let { value ->
                        Log.e(TAG, "adScanHubFirstLaunchEventListener::$value")
                        FirebaseConstants.scanHubFirstLaunch = value
                    }
                }
            }

            override fun onCancelled(@NonNull error: DatabaseError) {
                Log.e(TAG, "adScanHubFirstLaunchEventListener::onCancelled:${error.message}")
            }
        }
        firebaseDbConfig.configRef.child(FirebaseConstants.SCAN_HUB_FIRST_LAUNCH)
            .addListenerForSingleValueEvent(adScanHubFirstLaunchEventListener)
    }

    fun adScanHubSecondLaunchEventListener() {
        if (!firebaseDbConfig.isInitialized) return
        val adScanHubSecondLaunchEventListener = object : ValueEventListener {
            override fun onDataChange(@NonNull snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.getValue(Boolean::class.java)?.let { value ->
                        Log.e(TAG, "adScanHubSecondLaunchEventListener::$value")
                        FirebaseConstants.scanHubSecondLaunch = value
                    }
                }
            }

            override fun onCancelled(@NonNull error: DatabaseError) {
                Log.e(TAG, "adScanHubSecondLaunchEventListener::onCancelled:${error.message}")
            }
        }
        firebaseDbConfig.configRef.child(FirebaseConstants.SCAN_HUB_SECOND_LAUNCH)
            .addListenerForSingleValueEvent(adScanHubSecondLaunchEventListener)
    }

    fun adBoostXFirstLaunchEventListener() {
        if (!firebaseDbConfig.isInitialized) return
        val adBoostXFirstLaunchEventListener = object : ValueEventListener {
            override fun onDataChange(@NonNull snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.getValue(Boolean::class.java)?.let { value ->
                        Log.e(TAG, "adBoostXFirstLaunchEventListener::$value")
                        FirebaseConstants.boostXFirstLaunch = value
                    }
                }
            }

            override fun onCancelled(@NonNull error: DatabaseError) {
                Log.e(TAG, "adBoostXFirstLaunchEventListener::onCancelled:${error.message}")
            }
        }
        firebaseDbConfig.configRef.child(FirebaseConstants.BOOSTX_FIRST_LAUNCH)
            .addListenerForSingleValueEvent(adBoostXFirstLaunchEventListener)
    }

    fun adBoostXSecondLaunchEventListener() {
        if (!firebaseDbConfig.isInitialized) return
        val adBoostXSecondLaunchEventListener = object : ValueEventListener {
            override fun onDataChange(@NonNull snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.getValue(Boolean::class.java)?.let { value ->
                        Log.e(TAG, "adBoostXSecondLaunchEventListener::$value")
                        FirebaseConstants.boostXSecondLaunch = value
                    }
                }
            }

            override fun onCancelled(@NonNull error: DatabaseError) {
                Log.e(TAG, "adBoostXSecondLaunchEventListener::onCancelled:${error.message}")
            }
        }
        firebaseDbConfig.configRef.child(FirebaseConstants.BOOSTX_SECOND_LAUNCH)
            .addListenerForSingleValueEvent(adBoostXSecondLaunchEventListener)
    }

    fun tutorialScreenShowLaunchEventListener() {
        if (!firebaseDbConfig.isInitialized) return
        val tutorialScreenShowLaunchEventListener = object : ValueEventListener {
            override fun onDataChange(@NonNull snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.getValue(Boolean::class.java)?.let { value ->
                        Log.e(TAG, "tutorialScreenShowLaunchEventListener::$value")
                        FirebaseConstants.isTutorialScreenShow = value
                    }
                }
            }

            override fun onCancelled(@NonNull error: DatabaseError) {
                Log.e(TAG, "adBoostXSecondLaunchEventListener::onCancelled:${error.message}")
            }
        }
        firebaseDbConfig.configRef.child(FirebaseConstants.TUTORIAL_SCREEN_SHOW)
            .addListenerForSingleValueEvent(tutorialScreenShowLaunchEventListener)
    }

}