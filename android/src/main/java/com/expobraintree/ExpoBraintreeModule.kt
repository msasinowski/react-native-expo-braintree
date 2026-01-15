package com.expobraintree

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.braintreepayments.api.card.Card
import com.braintreepayments.api.card.CardClient
import com.braintreepayments.api.card.CardNonce
import com.braintreepayments.api.card.CardResult
import com.braintreepayments.api.datacollector.DataCollector
import com.braintreepayments.api.datacollector.DataCollectorResult
import com.braintreepayments.api.paypal.PayPalAccountNonce
import com.braintreepayments.api.paypal.PayPalCheckoutRequest
import com.braintreepayments.api.paypal.PayPalClient
import com.braintreepayments.api.paypal.PayPalLauncher
import com.braintreepayments.api.paypal.PayPalPaymentAuthRequest
import com.braintreepayments.api.paypal.PayPalPaymentAuthResult
import com.braintreepayments.api.paypal.PayPalPendingRequest
import com.braintreepayments.api.paypal.PayPalResult
import com.braintreepayments.api.paypal.PayPalVaultRequest
import com.braintreepayments.api.venmo.VenmoAccountNonce
import com.braintreepayments.api.venmo.VenmoClient
import com.braintreepayments.api.venmo.VenmoLauncher
import com.braintreepayments.api.venmo.VenmoPaymentAuthRequest
import com.braintreepayments.api.venmo.VenmoPaymentAuthResult
import com.braintreepayments.api.venmo.VenmoPendingRequest
import com.braintreepayments.api.venmo.VenmoRequest
import com.braintreepayments.api.venmo.VenmoResult
import com.expobraintree.SharedDataConverter.Companion.createDataCollectorRequest
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap


class ExpoBraintreeModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext), ActivityEventListener, LifecycleEventListener {
  val NAME = "ExpoBraintree"
  private lateinit var promiseRef: Promise
  private lateinit var currentActivityRef: FragmentActivity
  private var reactContextRef: Context
  private lateinit var payPalClientRef: PayPalClient
  private lateinit var venmoClientRef: VenmoClient

  init {
    this.reactContextRef = reactContext
    reactContext.addLifecycleEventListener(this)
    reactContext.addActivityEventListener(this)
  }

  companion object {
    lateinit var payPalLauncher: PayPalLauncher
    lateinit var venmoLauncher: VenmoLauncher
    private val moduleHandlers: ExpoBraintreeModuleHandlers = ExpoBraintreeModuleHandlers()

    fun init() {
      payPalLauncher = PayPalLauncher()
      venmoLauncher = VenmoLauncher()
    }
  }


  @ReactMethod
  fun requestBillingAgreement(data: ReadableMap, localPromise: Promise) {
    try {
      promiseRef = localPromise
      currentActivityRef = getCurrentActivity() as FragmentActivity

      if (this::currentActivityRef.isInitialized) {
        payPalClientRef = PayPalClient(
          currentActivityRef,
          data.getString("clientToken") ?: "",
          Uri.parse(data.getString("merchantAppLink") ?: ""),
          data.getString("fallbackUrlScheme") ?: null
        )
        val vaultRequest: PayPalVaultRequest = PaypalDataConverter.createVaultRequest(data)
        payPalClientRef.createPaymentAuthRequest(
          reactContextRef,
          vaultRequest
        ) { paymentAuthRequest ->
          when (paymentAuthRequest) {
            is PayPalPaymentAuthRequest.ReadyToLaunch -> {
              val pendingRequest = payPalLauncher.launch(currentActivityRef, paymentAuthRequest)
              when (pendingRequest) {
                is PayPalPendingRequest.Started -> { /* store pending request */
                  PendingRequestStore.getInstance().putPayPalPendingRequest(
                    reactContextRef,
                    pendingRequest
                  )
                }

                is PayPalPendingRequest.Failure -> { /* handle error */
                  moduleHandlers.onFailure(pendingRequest.error, promiseRef)
                }
              }
            }

            is PayPalPaymentAuthRequest.Failure -> { /* handle paymentAuthRequest.error */
              moduleHandlers.onFailure(paymentAuthRequest.error, promiseRef)
            }
          }
        }
      } else {
        throw Exception()
      }
    } catch (ex: Exception) {
      localPromise.reject(
        EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
        ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.value,
        SharedDataConverter.createError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ex.message)
      )
    }
  }

  @ReactMethod
  fun getDeviceDataFromDataCollector(data: ReadableMap, localPromise: Promise) {
    try {
      promiseRef = localPromise
      currentActivityRef = getCurrentActivity() as FragmentActivity

      if (this::currentActivityRef.isInitialized) {
        val dataCollectorClient =
          DataCollector(currentActivityRef, data.getString("clientToken") ?: "")
        val dataCollectorRequest = createDataCollectorRequest(data)
        dataCollectorClient.collectDeviceData(reactContextRef, dataCollectorRequest) { result ->
          when (result) {
            is DataCollectorResult.Failure ->
              moduleHandlers.handleGetDeviceDataFromDataCollectorResult(
                null,
                result.error,
                promiseRef
              )

            is DataCollectorResult.Success ->
              moduleHandlers.handleGetDeviceDataFromDataCollectorResult(
                result.deviceData,
                null,
                promiseRef
              )
          }
        }
      } else {
        throw Exception("Not Initialized")
      }
    } catch (ex: Exception) {
      promiseRef.reject(
        EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
        ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.value,
        SharedDataConverter.createError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ex.message)
      )
    }
  }

  @ReactMethod
  fun requestOneTimePayment(data: ReadableMap, localPromise: Promise) {
    try {
      promiseRef = localPromise
      currentActivityRef = getCurrentActivity() as FragmentActivity

      if (this::currentActivityRef.isInitialized) {
          payPalClientRef = PayPalClient(
            currentActivityRef,
            data.getString("clientToken") ?: "",
            Uri.parse(data.getString("merchantAppLink") ?: ""),
            data.getString("fallbackUrlScheme") ?: null
          )
          val checkoutRequest: PayPalCheckoutRequest =
            PaypalDataConverter.createCheckoutRequest(data)
          payPalClientRef.createPaymentAuthRequest(
            reactContextRef,
            checkoutRequest
          ) { paymentAuthRequest ->
            when (paymentAuthRequest) {
              is PayPalPaymentAuthRequest.ReadyToLaunch -> {
                val pendingRequest = payPalLauncher.launch(currentActivityRef, paymentAuthRequest)
                when (pendingRequest) {
                  is PayPalPendingRequest.Started -> { /* store pending request */
                    PendingRequestStore.getInstance().putPayPalPendingRequest(
                      reactContextRef,
                      pendingRequest
                    )
                  }

                  is PayPalPendingRequest.Failure -> { /* handle error */
                    moduleHandlers.onFailure(pendingRequest.error, promiseRef)
                  }
                }
              }

              is PayPalPaymentAuthRequest.Failure -> { /* handle paymentAuthRequest.error */
                moduleHandlers.onFailure(paymentAuthRequest.error, promiseRef)
              }
            }
          }
        } else {
        throw Exception()
      }
    } catch (ex: Exception) {
      localPromise.reject(
        EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
        ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.value,
        SharedDataConverter.createError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ex.message)
      )
    }
  }

  @ReactMethod
  fun tokenizeCardData(data: ReadableMap, localPromise: Promise) {
    try {
      promiseRef = localPromise
      currentActivityRef = getCurrentActivity() as FragmentActivity

      val clientToken = data.getString("clientToken") ?: ""
      if (this::currentActivityRef.isInitialized && clientToken.isNotEmpty()) {
        val cardClient = CardClient(reactContextRef, data.getString("clientToken") ?: "")
        val cardRequest: Card = PaypalDataConverter.createTokenizeCardRequest(data)
        cardClient.tokenize(cardRequest) { cardResult ->
          when (cardResult) {
            is CardResult.Success -> {
              moduleHandlers.onCardTokenizeSuccessHandler(
                cardResult.nonce,
                promiseRef
              )
            }

            is CardResult.Failure -> {
              moduleHandlers.onCardTokenizeFailure(cardResult.error, promiseRef)
            }
          }
        }
      } else {
        throw Exception()
      }
    } catch (ex: Exception) {
      localPromise.reject(
        EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
        ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.value,
        SharedDataConverter.createError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ex.message)
      )
    }
  }

  @ReactMethod
  fun requestVenmoNonce(data: ReadableMap, localPromise: Promise) {
    try {
      promiseRef = localPromise
      currentActivityRef = getCurrentActivity() as FragmentActivity

      if (this::currentActivityRef.isInitialized) {
        venmoClientRef = VenmoClient(
          currentActivityRef,
          data.getString("clientToken") ?: "",
          Uri.parse(data.getString("merchantAppLink") ?: ""),
          data.getString("fallbackUrlScheme") ?: null
        )
        val request: VenmoRequest = VenmoDataConverter.createRequest(data)
        venmoClientRef.createPaymentAuthRequest(
          reactContextRef,
          request
        ) { paymentAuthRequest ->
          when (paymentAuthRequest) {
            is VenmoPaymentAuthRequest.ReadyToLaunch -> {
              val pendingRequest = venmoLauncher.launch(currentActivityRef, paymentAuthRequest)
              when (pendingRequest) {
                is VenmoPendingRequest.Started -> { /* store pending request */
                  PendingRequestStore.getInstance().putVenmoPendingRequest(
                    reactContextRef,
                    pendingRequest
                  )
                }

                is VenmoPendingRequest.Failure -> { /* handle error */
                  moduleHandlers.onFailure(pendingRequest.error, promiseRef)
                }
              }
            }

            is VenmoPaymentAuthRequest.Failure ->
              moduleHandlers.onFailure(paymentAuthRequest.error, promiseRef)
          }
        }
      } else {
        throw Exception()
      }
    } catch (ex: Exception) {
      localPromise.reject(
        EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
        ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.value,
        SharedDataConverter.createError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ex.message)
      )
    }
  }

  private fun handlePayPalAccountNonceResult(
    payPalAccountNonce: PayPalAccountNonce?,
    error: Exception?,
  ) {
    if (error != null) {
      moduleHandlers.onFailure(error, promiseRef)
      return
    }
    if (payPalAccountNonce != null) {
      moduleHandlers.onPayPalSuccessHandler(payPalAccountNonce, promiseRef)
    }
  }

  private fun handleVenmoAccountNonceResult(
    nonce: VenmoAccountNonce?,
    error: Exception?,
  ) {
    if (error != null) {
      moduleHandlers.onFailure(error, promiseRef)
      return
    }
    if (nonce != null) {
      moduleHandlers.onVenmoSuccessHandler(nonce, promiseRef)
    }
  }

  private fun getPayPalPendingRequest(): PayPalPendingRequest.Started? {
    currentActivityRef = getCurrentActivity() as FragmentActivity
    return PendingRequestStore.getInstance().getPayPalPendingRequest(currentActivityRef)
  }

  private fun clearPayPalPendingRequest() {
    currentActivityRef = getCurrentActivity() as FragmentActivity
    PendingRequestStore.getInstance().clearPayPalPendingRequest(currentActivityRef)
  }

  private fun getVenmoPendingRequest(): VenmoPendingRequest.Started? {
    currentActivityRef = getCurrentActivity() as FragmentActivity
    return PendingRequestStore.getInstance().getVenmoPendingRequest(currentActivityRef)
  }

  private fun clearVenmoPendingRequest() {
    currentActivityRef = getCurrentActivity() as FragmentActivity
    PendingRequestStore.getInstance().clearVenmoPendingRequest(currentActivityRef)
  }

  private fun handleReturnToApp(intent: Intent) {
    // fetch stored pending requests
    val payPalPendingRequest: PayPalPendingRequest.Started? = getPayPalPendingRequest()
    val venmoPendingRequest: VenmoPendingRequest.Started? = getVenmoPendingRequest()

    // PayPal
    if (payPalPendingRequest != null) {
      val paymentAuthResult = payPalLauncher.handleReturnToApp(payPalPendingRequest, intent)

      when (paymentAuthResult) {
        is PayPalPaymentAuthResult.Failure ->
          handlePayPalAccountNonceResult(null, paymentAuthResult.error)

        PayPalPaymentAuthResult.NoResult ->
          moduleHandlers.onCancel(Exception("No result"), promiseRef)

        is PayPalPaymentAuthResult.Success ->
          completePayPalFlow(paymentAuthResult);
      }
      clearPayPalPendingRequest()
    }

    // Venmo
    if (venmoPendingRequest != null) {
      when (val paymentAuthResult = venmoLauncher.handleReturnToApp(venmoPendingRequest, intent)) {
        is VenmoPaymentAuthResult.Failure ->
          handleVenmoAccountNonceResult(null, paymentAuthResult.error)

        VenmoPaymentAuthResult.NoResult ->
          moduleHandlers.onCancel(Exception("No result"), promiseRef)

        is VenmoPaymentAuthResult.Success ->
          completeVenmoFlow(paymentAuthResult);
      }
      clearVenmoPendingRequest()
    }
  }

  private fun completePayPalFlow(paymentAuthResult: PayPalPaymentAuthResult.Success) {
    payPalClientRef.tokenize(paymentAuthResult) { result ->
      when (result) {
        is PayPalResult.Success -> { /* handle result.nonce */
          handlePayPalAccountNonceResult(result.nonce, null)
        }

        is PayPalResult.Failure -> { /* handle result.error */
          handlePayPalAccountNonceResult(null, result.error)
        }

        is PayPalResult.Cancel -> { /* handle user canceled */
          moduleHandlers.onCancel(Exception("Cancel"), promiseRef)
        }
      }
    }
  }

  private fun completeVenmoFlow(paymentAuthResult: VenmoPaymentAuthResult.Success) {
    venmoClientRef.tokenize(paymentAuthResult) { result ->
      when (result) {
        is VenmoResult.Success -> { /* handle result.nonce */
          handleVenmoAccountNonceResult(result.nonce, null)
        }

        is VenmoResult.Failure -> { /* handle result.error */
          handleVenmoAccountNonceResult(null, result.error)
        }

        is VenmoResult.Cancel -> { /* handle user canceled */
          moduleHandlers.onCancel(Exception("Cancel"), promiseRef)
        }
      }
    }
  }

  override fun getName(): String {
    return NAME
  }

  // empty required Implementations from interfaces
  override fun onHostPause() {}
  override fun onHostDestroy() {}
  override fun onActivityResult(p0: Activity, p1: Int, p2: Int, p3: Intent?) {}

  override fun onHostResume() {
    if (this::currentActivityRef.isInitialized) {
      currentActivityRef = getCurrentActivity() as FragmentActivity
      handleReturnToApp(currentActivityRef.getIntent())
    }
  }

  override fun onNewIntent(intent: Intent) {
    if (this::currentActivityRef.isInitialized) {
      currentActivityRef = getCurrentActivity() as FragmentActivity
      currentActivityRef.setIntent(intent)
      handleReturnToApp(intent)
      currentActivityRef.intent = Intent()
    }
  }
}
