package com.itl.commonres.firebaseUtils

class FirebaseConstants {
    companion object {
        const val AD_FULL_SCREEN_CAPPING = "afsc"
        const val Ad_SHOW = "showAd"
        const val Ad_SHOW_HEX = "showAdHex"


        //scanHub
        const val SCAN_HUB_FIRST_LAUNCH = "shfl"
        const val SCAN_HUB_SECOND_LAUNCH = "shsl"

        //boostX
        const val BOOSTX_FIRST_LAUNCH = "bxfl"
        const val BOOSTX_SECOND_LAUNCH = "bxsl"

        var HEX_SHOW_AD = ""
        var INTERSTITIAL_SHOW_AD = 0L


        var scanHubFirstLaunch = false
        var scanHubSecondLaunch = false

        var boostXFirstLaunch = false
        var boostXSecondLaunch = false

        const val TUTORIAL_SCREEN_SHOW = "tss"
        var isTutorialScreenShow = false

    }
}