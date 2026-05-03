package com.cam.scanner.scantopdf.android.util;

import com.cam.scanner.scantopdf.android.rest.model.DummyPurchaseModel;

public class DummyPurchase {

    public DummyPurchaseModel getDummyPurchasePremiumYearly() {


        String orderId = "ITL.5678-2923-6539-12348";
        String packageName = "com.cam.scanner.scantopdf.android";
        String productId_SKU = Constants.PRODUCT_ID_PREMIUM_TEST;
        Long purchaseTime = System.currentTimeMillis();
        int purchaseState = 0;
        String purchaseToken = "ITLprehohilhlomopbjpcpdd.AO-J1Ox7GVMdNAvxF46ifeaq26GVPlo_n6rvjEYLVunc3_OInZgtZnswZB0yqumN0NfxK2Y-uVMKJqSATGQJf54qaGrcOPaKJLi-4TsqKQYYzjYkRsq9TjiSrWpqcM4vnOlQjSk6xUTSiLIYjdpIv0QNcx2wPREITL";
        boolean isAutoRenewing = true;
        boolean isAcknowledged = false;

        DummyPurchaseModel dummyPurchaseModel = new DummyPurchaseModel();
        dummyPurchaseModel.setOrderId(orderId);
        dummyPurchaseModel.setPackageName(packageName);
        dummyPurchaseModel.setProductId(productId_SKU);
        dummyPurchaseModel.setPurchaseTime(purchaseTime);
        dummyPurchaseModel.setPurchaseState(purchaseState);
        dummyPurchaseModel.setPurchaseToken(purchaseToken);
        dummyPurchaseModel.setAutoRenewing(isAutoRenewing);
        dummyPurchaseModel.setAcknowledged(isAcknowledged);

        return dummyPurchaseModel;
    }

    public DummyPurchaseModel getDummyPurchaseOcrMonthly() {


        String orderId = "ITL.1234-2923-6539-12347";
        String packageName = "com.cam.scanner.scantopdf.android";
        String productId_SKU = Constants.PRODUCT_ID_OCR_MONTH_TEST;
        Long purchaseTime = System.currentTimeMillis();
        int purchaseState = 0;
        String purchaseToken = "ITLocrhohilhlomopbjpcpdd.AO-J1Ox7GVMdNAvxF46ifeaq26GVPlo_n6rvjEYLVunc3_OInZgtZnswZB0yqumN0NfxK2Y-uVMKJqSATGQJf54qaGrcOPaKJLi-4TsqKQYYzjYkRsq9TjiSrWpqcM4vnOlQjSk6xUTSiLIYjdpIv0QNcx2wOCRITL";
        boolean isAutoRenewing = true;
        boolean isAcknowledged = false;

        DummyPurchaseModel dummyPurchaseModel = new DummyPurchaseModel();
        dummyPurchaseModel.setOrderId(orderId);
        dummyPurchaseModel.setPackageName(packageName);
        dummyPurchaseModel.setProductId(productId_SKU);
        dummyPurchaseModel.setPurchaseTime(purchaseTime);
        dummyPurchaseModel.setPurchaseState(purchaseState);
        dummyPurchaseModel.setPurchaseToken(purchaseToken);
        dummyPurchaseModel.setAutoRenewing(isAutoRenewing);
        dummyPurchaseModel.setAcknowledged(isAcknowledged);

        return dummyPurchaseModel;
    }

    /*public Purchase getDummyPurchase() {

     *//*{"orderId":"GPA.3377-2923-6539-19050","packageName":"com.cam.scanner.scantopdf.android","productId":"test.flashscan.premium_yearly",
                "purchaseTime":1598001630363,"purchaseState":0,
                "purchaseToken":"aahcpmhohilhlomopbjpcpdd.AO-J1Ox7GVMdNAvxF46ifeaq26GVPlo_n6rvjEYLVunc3_OInZgtZnswZB0yqumN0NfxK2Y-uVMKJqSATGQJf54qaGrcOPaKJLi-4TsqKQYYzjYkRsq9TjiSrWpqcM4vnOlQjSk6xUTSiLIYjdpIv0QNcx2w0BGBDQ",
                "autoRenewing":true,"acknowledged":true}*//*


        String orderId = "ITL.3377-2923-6539-12345";
        String packageName = "com.cam.scanner.scantopdf.android";
        String productId_SKU = "test.flashscan.premium_yearly";
        Long purchaseTime = System.currentTimeMillis();
        int purchaseState = 0;
        String purchaseToken = "ITLcpmhohilhlomopbjpcpdd.AO-J1Ox7GVMdNAvxF46ifeaq26GVPlo_n6rvjEYLVunc3_OInZgtZnswZB0yqumN0NfxK2Y-uVMKJqSATGQJf54qaGrcOPaKJLi-4TsqKQYYzjYkRsq9TjiSrWpqcM4vnOlQjSk6xUTSiLIYjdpIv0QNcx2w0BGITL";
        boolean isAutoRenewing = true;
        boolean isAcknowledged = false;


        String jsonPurchaseInfo = "{\"orderId\":\"ITL.3377-2923-6539-12345\",\"packageName\":\"com.cam.scanner.scantopdf.android\",\"productId\":\"test.flashscan.premium_yearly\",\n" +
                "                \"purchaseTime\":1598001630363,\"purchaseState\":0,\n" +
                "                \"purchaseToken\":\"ITLcpmhohilhlomopbjpcpdd.AO-J1Ox7GVMdNAvxF46ifeaq26GVPlo_n6rvjEYLVunc3_OInZgtZnswZB0yqumN0NfxK2Y-uVMKJqSATGQJf54qaGrcOPaKJLi-4TsqKQYYzjYkRsq9TjiSrWpqcM4vnOlQjSk6xUTSiLIYjdpIv0QNcx2w0BGITL\",\n" +
                "                \"autoRenewing\":true,\"acknowledged\":false}";


        Purchase purchase = null;
        try {
            purchase = new Purchase(null, null);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return purchase;
    }*/
}
