package com.payment.test.tool

import android.content.Intent
import android.os.Bundle
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate
import com.margelo.nitro.expobraintree.ExpoBraintree

class MainActivity : ReactActivity() {

  override fun getMainComponentName(): String = "ExpoBraintreeExample"

  override fun createReactActivityDelegate(): ReactActivityDelegate =
      DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ExpoBraintree.initGooglePay(this)
    ExpoBraintree.initThreeDSecure(this)
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    this.intent = intent
  }
}
