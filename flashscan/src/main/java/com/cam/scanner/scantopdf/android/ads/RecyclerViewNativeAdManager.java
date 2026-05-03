package com.cam.scanner.scantopdf.android.ads;

import com.google.android.gms.ads.nativead.NativeAd;

public class RecyclerViewNativeAdManager {

    private static volatile RecyclerViewNativeAdManager instance = null;

    public NativeAd getUnifiedNativeAd() {
        return unifiedNativeAd;
    }

    public void setUnifiedNativeAd(NativeAd unifiedNativeAd) {
        this.unifiedNativeAd = unifiedNativeAd;
    }

    private NativeAd unifiedNativeAd;

    public static RecyclerViewNativeAdManager getInstance() {
        if (instance == null) {
            synchronized (RecyclerViewNativeAdManager.class) {
                if (instance == null) {
                    instance = new RecyclerViewNativeAdManager();
                }
            }
        }
        return instance;
    }

    private RecyclerViewNativeAdManager() {

    }


}
