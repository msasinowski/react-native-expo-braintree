package com.expobraintreeexample

import android.os.Bundle
import com.expobraintree.ExpoBraintreeModule
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate

class MainActivity : ReactActivity() {

  // @generated end [Streem:Android] mod-main-activity-add-content
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(null)
    ExpoBraintreeModule.initPayPal()
    ExpoBraintreeModule.initVenmo()
    ExpoBraintreeModule.initThreeDSecure(this)
  }

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  override fun getMainComponentName(): String = "ExpoBraintreeExample"

  /**
   * Returns the instance of the [ReactActivityDelegate]. We use [DefaultReactActivityDelegate]
   * which allows you to enable New Architecture with a single boolean flags [fabricEnabled]
   */
  override fun createReactActivityDelegate(): ReactActivityDelegate =
          DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)
}
