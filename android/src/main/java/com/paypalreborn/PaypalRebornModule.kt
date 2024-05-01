package com.paypalreborn

import android.app.Activity
import android.content.Context;
import android.content.Intent
import androidx.fragment.app.FragmentActivity

import com.braintreepayments.api.BraintreeClient
import com.braintreepayments.api.BraintreeRequestCodes
import com.braintreepayments.api.BrowserSwitchResult
import com.braintreepayments.api.DataCollector
import com.braintreepayments.api.PayPalAccountNonce
import com.braintreepayments.api.PayPalClient
import com.braintreepayments.api.PayPalVaultRequest
import com.braintreepayments.api.PayPalCheckoutRequest

import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap


class PaypalRebornModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext), ActivityEventListener, LifecycleEventListener {
  val NAME = "PaypalReborn"
  private lateinit var promiseRef: Promise
  private lateinit var currentActivityRef: FragmentActivity
  private var reactContextRef: Context
  private lateinit var braintreeClientRef: BraintreeClient
  private lateinit var payPalClientRef: PayPalClient
  private val paypalRebornModuleHandlers: PaypalRebornModuleHandlers = PaypalRebornModuleHandlers()

  init {
    this.reactContextRef = reactContext
    reactContext.addLifecycleEventListener(this)
    reactContext.addActivityEventListener(this)
  }

  @ReactMethod
  fun requestBillingAgreement(data: ReadableMap, localPromise: Promise) {
    try {
      promiseRef = localPromise
      currentActivityRef = getCurrentActivity() as FragmentActivity
      braintreeClientRef = BraintreeClient(currentActivityRef, data.getString("clientToken") ?: "")

      if (this::currentActivityRef.isInitialized && this::braintreeClientRef.isInitialized) {
        payPalClientRef = PayPalClient(braintreeClientRef)
        val vaultRequest: PayPalVaultRequest = PaypalDataConverter.createVaultRequest(data)
        payPalClientRef.tokenizePayPalAccount(
          currentActivityRef,
          vaultRequest
        ) { e: Exception? ->
          handlePayPalAccountNonceResult(null, e)
        }
      } else {
        throw Exception("Not Initialized")
      }
    } catch (ex: Exception) {
      localPromise.reject("-1", ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.value,
        PaypalDataConverter.createError(
          EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ex.message
        ))
    }
  }

  @ReactMethod
  fun getDeviceDataFromDataCollector(clientToken: String?, localPromise: Promise) {
    try {
      promiseRef = localPromise
      braintreeClientRef = BraintreeClient(reactContextRef, clientToken ?: "")
      if (this::braintreeClientRef.isInitialized) {
        val dataCollectorClient = DataCollector(braintreeClientRef)
        dataCollectorClient.collectDeviceData(
          reactContextRef
        ) { result: String?, e: Exception? ->
          paypalRebornModuleHandlers.handleGetDeviceDataFromDataCollectorResult(result, e, promiseRef)
        }
      } else {
        throw Exception("Not Initialized")
      }
    } catch (ex: Exception) {
      promiseRef.reject("-1", ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.value,
        PaypalDataConverter.createError(
          EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
          ex.message
        ))

    }
  }

  @ReactMethod
  fun requestOneTimePayment(data: ReadableMap, localPromise: Promise) {
    try {
      promiseRef = localPromise
      currentActivityRef = getCurrentActivity() as FragmentActivity
      braintreeClientRef = BraintreeClient(currentActivityRef, data.getString("clientToken") ?: "")

      if (this::currentActivityRef.isInitialized && this::braintreeClientRef.isInitialized) {
        payPalClientRef = PayPalClient(braintreeClientRef)
        val checkoutRequest: PayPalCheckoutRequest = PaypalDataConverter.createCheckoutRequest(data)
        payPalClientRef.tokenizePayPalAccount(
          currentActivityRef,
          checkoutRequest
        ) { e: Exception? ->
          handlePayPalAccountNonceResult(null, e)
        }
      } else {
        throw Exception("Not Initialized")
      }
    } catch (ex: Exception) {
      localPromise.reject("-1", ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.value,
        PaypalDataConverter.createError(
          EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ex.message
        ))
    }
  }


//  @ReactMethod
//  fun requestOneTimePayment(data: ReadableMap, localPromise: Promise) {
//    try {
//      this.promise = localPromise
//      val activity = getCurrentActivity() as FragmentActivity
//      val braintreeClient = BraintreeClient(activity, data.getString("clientToken") ?: "")
//      if (localPromise == null || braintreeClient == null) {
//        localPromise.reject("-1", ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.value,
//          PaypalDataConverter.createError(
//            EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
//          ))
//        return
//      }
//        val payPalClient = PayPalClient(activity as FragmentActivity, braintreeClient)
//        payPalClient.setListener(this@PaypalRebornModule)
//        val checkoutRequest: PayPalCheckoutRequest = PaypalDataConverter.createCheckoutRequest(data)
//        payPalClient.tokenizePayPalAccount(activity, checkoutRequest)
//    } catch (ex: Exception) {
//      localPromise.reject("-1", ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.value,
//        PaypalDataConverter.createError(
//          EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
//        ))
//    }
//  }

//  @ReactMethod
//  fun tokenizeCardData(data: ReadableMap, localPromise: Promise) {
//    this.promise = promise
//  }
//

  public fun handlePayPalAccountNonceResult(
    payPalAccountNonce: PayPalAccountNonce?,
    error: Exception?,
  ) {
    if (error != null) {
      paypalRebornModuleHandlers.onPayPalFailure(error, promiseRef)
      return
    }
    if (payPalAccountNonce != null) {
      paypalRebornModuleHandlers.onPayPalSuccessHandler(payPalAccountNonce, promiseRef)
    }
  }

  override fun onHostResume() {
    if (this::braintreeClientRef.isInitialized && this::currentActivityRef.isInitialized) {
      val browserSwitchResult: BrowserSwitchResult? =
        braintreeClientRef.deliverBrowserSwitchResult(currentActivityRef)
      if (browserSwitchResult != null) {
        when (browserSwitchResult.requestCode) {
          BraintreeRequestCodes.PAYPAL -> if (this::payPalClientRef.isInitialized) {
            payPalClientRef.onBrowserSwitchResult(
              browserSwitchResult,
              this::handlePayPalAccountNonceResult
            )
          }
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent?) {
    if (this::currentActivityRef.isInitialized) {
      currentActivityRef.setIntent(intent)
    }
  }

  override fun getName(): String {
    return NAME
  }

  // empty required Implementations from interfaces
  override fun onHostPause() {}
  override fun onHostDestroy() {}
  override fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, intent: Intent?) {}
}
