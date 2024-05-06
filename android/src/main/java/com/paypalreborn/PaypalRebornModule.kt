package com.paypalreborn

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity

import com.braintreepayments.api.BraintreeClient
import com.braintreepayments.api.BraintreeRequestCodes
import com.braintreepayments.api.BrowserSwitchResult
import com.braintreepayments.api.DataCollector
import com.braintreepayments.api.PayPalAccountNonce
import com.braintreepayments.api.CardNonce
import com.braintreepayments.api.PayPalCheckoutRequest
import com.braintreepayments.api.PayPalClient
import com.braintreepayments.api.PayPalVaultRequest
import com.braintreepayments.api.CardClient
import com.braintreepayments.api.Card

import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap


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
        throw Exception()
      }
    } catch (ex: Exception) {
      localPromise.reject(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
        ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.value,
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
      promiseRef.reject(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
        ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.value,
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
        throw Exception()
      }
    } catch (ex: Exception) {
      localPromise.reject(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
        ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.value,
        PaypalDataConverter.createError(
          EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ex.message
        ))
    }
  }

  @ReactMethod
  fun tokenizeCardData(data: ReadableMap, localPromise: Promise) {
    try {
      promiseRef = localPromise
      currentActivityRef = getCurrentActivity() as FragmentActivity
      braintreeClientRef = BraintreeClient(currentActivityRef, data.getString("clientToken") ?: "")

      if (this::currentActivityRef.isInitialized && this::braintreeClientRef.isInitialized) {
        val cardClient = CardClient(braintreeClientRef)
        val cardRequest: Card = PaypalDataConverter.createTokenizeCardRequest(data)
        cardClient.tokenize(cardRequest) { cardNonce, error ->
          handleCardTokenizeResult(cardNonce, error)
        }
      } else {
        throw Exception()
      }
    } catch (ex: Exception) {
      localPromise.reject(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
        ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.value,
        PaypalDataConverter.createError(
          EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ex.message
        ))
    }
  }

  public fun handleCardTokenizeResult(
    cardNonce: CardNonce?,
    error: Exception?,
  ) {
    if (error != null) {
      paypalRebornModuleHandlers.onCardTokenizeFailure(error, promiseRef)
      return
    }
    if (cardNonce != null) {
      paypalRebornModuleHandlers.onCardTokenizeSuccessHandler(cardNonce, promiseRef)
    }
  }

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
