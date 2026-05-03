package com.itl.commonres.utils

interface RewardedAdInterface {
    fun proceed()
}

interface AdStatusInterface{
    fun onAdFailed()
    fun onAdLoaded()
}