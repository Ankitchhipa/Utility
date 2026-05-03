package com.cam.scanner.scantopdf.android.ads;

public interface AdClosed {
    void onAdClosed();
    void onAdLoadedOrFailed(boolean isLoaded);
}
