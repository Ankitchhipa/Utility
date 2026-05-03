package com.itl.commonres.purchase

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import com.itl.commonres.databinding.ActivitySubscriptionScreenBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubscriptionScreen : AppCompatActivity(), PurchasesUpdatedListener {

    private lateinit var binding: ActivitySubscriptionScreenBinding
    private lateinit var billingClient: BillingClient

    private var productDetailsList = mutableListOf<ProductDetails>()

    private var TAG = SubscriptionScreen::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySubscriptionScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {

            }

            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    processForPurchase()
                }
            }

        })

    }

    private fun init() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .build()
    }

    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {

    }

    private fun processForPurchase() {
        CoroutineScope(Dispatchers.IO).launch {
            processPurchases()
        }
    }

    private suspend fun processPurchases() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("product_id_example")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        // leverage queryProductDetails Kotlin extension function
        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params.build())
        }

        // Process the result.
        runOnUiThread {
            if (productDetailsResult.billingResult.responseCode ==
                BillingClient.BillingResponseCode.OK
            ) {
                productDetailsList.clear()
                productDetailsList.addAll(productDetailsResult.productDetailsList ?: arrayListOf())
                Log.e(TAG, "processPurchases: $productDetailsList")
            }
        }
    }
}