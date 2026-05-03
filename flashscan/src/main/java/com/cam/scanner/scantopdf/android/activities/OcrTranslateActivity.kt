package com.cam.scanner.scantopdf.android.activities

import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.cam.scanner.scantopdf.android.R
import com.cam.scanner.scantopdf.android.databinding.ActivityOcrTranslateBinding
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class OcrTranslateActivity : AppCompatActivity() {

    private var binding: ActivityOcrTranslateBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOcrTranslateBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setUpWebView()

    }

    private fun setUpWebView() {
        val webSettings: WebSettings? = binding?.webView?.settings
        webSettings?.javaScriptEnabled = true

        // Set a WebViewClient to handle page loading within the WebView
        binding?.webView?.webViewClient  = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                Log.e("webView", "onReceivedError::$error")
                super.onReceivedError(view, request, error)
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                Log.e("webView", "onReceivedHttpError::${errorResponse?.data}")
                super.onReceivedHttpError(view, request, errorResponse)
            }
        }

        // Get the text to translate from the intent
        val textToTranslate = intent.getStringExtra(getString(R.string.transalate_text))
        textToTranslate?.let {
            loadGoogleTranslate(it)
        }
    }

    private fun loadGoogleTranslate(text: String) {
        val encodedText = try {
            URLEncoder.encode(text, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            ""
        }
        val url = "https://translate.google.com/?sl=auto&tl=en&text=$encodedText"
        binding?.webView?.loadUrl(url)
    }
}