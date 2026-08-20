package com.cam.scanner.scantopdf.android.util;

import android.content.Context;

import com.android.billingclient.api.Purchase;

import java.util.List;

public final class PremiumStatusHelper {

    private PremiumStatusHelper() {
    }

    public static PremiumState applySubscriptionPurchases(PrefManager prefManager, List<Purchase> purchases) {
        PremiumState state = getPremiumState(purchases);
        prefManager.setPremiumYearly(state.isYearlyPremium());
        prefManager.setPremiumMonthly(state.isMonthlyPremium());
        return state;
    }

    public static PremiumState getPremiumState(List<Purchase> purchases) {
        boolean yearlyPremium = false;
        boolean monthlyPremium = false;

        if (purchases != null) {
            for (Purchase purchase : purchases) {
                if (purchase.getPurchaseState() != Purchase.PurchaseState.PURCHASED) {
                    continue;
                }

                List<String> products = purchase.getProducts();
                if (products.contains(Constants.PRODUCT_ID_PREMIUM)) {
                    yearlyPremium = true;
                }
                if (products.contains(Constants.PRODUCT_ID_MONTHLY)) {
                    monthlyPremium = true;
                }
            }
        }

        return new PremiumState(yearlyPremium, monthlyPremium);
    }

    public static void syncPremiumTopic(Context context, PrefManager prefManager) {
        SubscribeToTopic subscribeToTopic = new SubscribeToTopic(context);
        if (prefManager.isPremium()) {
            subscribeToTopic.doUnsubscribeFromTopic(Constants.SubscribeToTopic.FREE);
            subscribeToTopic.doSubscribeToTopic(Constants.SubscribeToTopic.BOTH_PREMIUM);
            prefManager.setUnsubscribeFromFree(true);
        } else {
            prefManager.setUnsubscribeFromFree(false);
        }
    }

    public static final class PremiumState {
        private final boolean yearlyPremium;
        private final boolean monthlyPremium;

        private PremiumState(boolean yearlyPremium, boolean monthlyPremium) {
            this.yearlyPremium = yearlyPremium;
            this.monthlyPremium = monthlyPremium;
        }

        public boolean isYearlyPremium() {
            return yearlyPremium;
        }

        public boolean isMonthlyPremium() {
            return monthlyPremium;
        }

        public boolean isPremium() {
            return yearlyPremium || monthlyPremium;
        }
    }
}
