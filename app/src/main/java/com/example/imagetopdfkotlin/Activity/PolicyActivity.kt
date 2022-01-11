package com.example.imagetopdfkotlin.Activity

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.databinding.ActivityPolicyBinding

class PolicyActivity : AppCompatActivity() {
    var policyBinding: ActivityPolicyBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        policyBinding =
            DataBindingUtil.setContentView(this@PolicyActivity, R.layout.activity_policy)

        policyBinding?.mWebView?.webViewClient = MyWebViewClient()
        openURL()
        policyBinding?.mBack?.setOnClickListener(View.OnClickListener { onBackPressed() })
    }

    private fun openURL() {
        policyBinding?.mWebView?.loadUrl("https://sites.google.com/view/imagepdfprivacypolicy/home")
        policyBinding?.mWebView?.requestFocus()
    }

    private class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }
    }
}