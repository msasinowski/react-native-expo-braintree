package com.expobraintree

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.braintreepayments.api.card.Card
import com.braintreepayments.api.card.CardClient
import com.braintreepayments.api.card.CardResult
import com.braintreepayments.api.paypal.PayPalLauncher
import com.braintreepayments.api.paypal.PayPalPaymentAuthResult
import com.braintreepayments.api.paypal.PayPalPendingRequest
import com.braintreepayments.api.paypal.PayPalClient
import com.braintreepayments.api.paypal.PayPalVaultRequest
import com.braintreepayments.api.paypal.PayPalPaymentAuthRequest
import com.braintreepayments.api.threedsecure.ThreeDSecureClient
import com.braintreepayments.api.threedsecure.ThreeDSecureLauncher
import com.braintreepayments.api.threedsecure.ThreeDSecurePaymentAuthRequest
import com.braintreepayments.api.threedsecure.ThreeDSecureRequest
import com.braintreepayments.api.threedsecure.ThreeDSecureResult

import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap


class ExpoBraintreeModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext), LifecycleEventListener, ActivityEventListener {

  val NAME = "ExpoBraintree"
  private lateinit var currentActivityRef: FragmentActivity
  private lateinit var payPalClient: PayPalClient
  private var reactContextRef: Context

  init {
    this.reactContextRef = reactContext
    reactContext.addLifecycleEventListener(this)
    reactContext.addActivityEventListener(this)
  }

  companion object {
    private lateinit var threeDSecureLauncher: ThreeDSecureLauncher
    private lateinit var payPalLauncher: PayPalLauncher
    private lateinit var threeDSecureClient: ThreeDSecureClient
    private lateinit var promiseRef: Promise
    private val paypalRebornModuleHandlers: PaypalRebornModuleHandlers =
      PaypalRebornModuleHandlers()

    fun initializeThreeDSecureLauncher(activity: FragmentActivity) {
      threeDSecureLauncher =
        ThreeDSecureLauncher(activity) { paymentAuthResult ->
          threeDSecureClient.tokenize(paymentAuthResult) { threeDSecureNonce ->
            when (threeDSecureNonce) {
              is ThreeDSecureResult.Success -> { /* send result.nonce to server */
                paypalRebornModuleHandlers.onThreeDSecureSuccessHandler(
                  threeDSecureNonce.nonce,
                  promiseRef
                )
              }

              is ThreeDSecureResult.Failure -> { /* handle result.error */
                paypalRebornModuleHandlers.onThreeDSecureFailure(
                  threeDSecureNonce.error,
                  promiseRef
                )
              }

              is ThreeDSecureResult.Cancel -> {
                promiseRef.reject(
                  EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
                  ERROR_TYPES.PAYMENT_3D_SECURE_CANCELLED.value,
                  PaypalDataConverter.createError(
                    EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value,
                    ERROR_TYPES.PAYMENT_3D_SECURE_CANCELLED.value
                  )
                )
              }
            }
          }
        }
    }

    fun initializePaypalLauncher() {
      payPalLauncher = PayPalLauncher()
    }
  }

  @ReactMethod
  fun requestBillingAgreement(data: ReadableMap, localPromise: Promise) {
    try {
      promiseRef = localPromise
      currentActivityRef = getCurrentActivity() as FragmentActivity

      if (this::currentActivityRef.isInitialized) {
        val clientToken = data.getString("clientToken") ?: ""
        payPalClient = PayPalClient(
          reactContextRef,
          clientToken,
          Uri.parse("https://braintree-example-app.web.app/braintree-payments") // Merchant App Link
        )
        val vaultRequest: PayPalVaultRequest = PaypalDataConverter.createVaultRequest(data)
        payPalClient.createPaymentAuthRequest(reactContextRef, vaultRequest) { paymentAuthRequest ->
          when (paymentAuthRequest) {
            is PayPalPaymentAuthRequest.ReadyToLaunch -> {
              val pendingRequest = payPalLauncher.launch(currentActivityRef, paymentAuthRequest)
              when (pendingRequest) {
                is PayPalPendingRequest.Started -> { /* store pending request */
                  ExpoBraintreePendingRequestStore.instance.putPayPalPendingRequest(
                    reactContextRef,
                    pendingRequest
                  )
                }

                is PayPalPendingRequest.Failure -> { /* handle error */
                  localPromise.reject("PayPalPendingRequest.Failure")
                }
              }
            }

            is PayPalPaymentAuthRequest.Failure -> { /* handle paymentAuthRequest.error */
              localPromise.reject(paymentAuthRequest.error.localizedMessage)
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
        PaypalDataConverter.createError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ex.message)
      )
    }
  }
//
//  @ReactMethod
//  fun getDeviceDataFromDataCollector(clientToken: String?, localPromise: Promise) {
//    try {
//      promiseRef = localPromise
//      braintreeClientRef = BraintreeClient(reactContextRef, clientToken ?: "")
//      if (this::braintreeClientRef.isInitialized) {
//        val dataCollectorClient = DataCollector(braintreeClientRef)
//        dataCollectorClient.collectDeviceData(reactContextRef) { result: String?, e: Exception? ->
//          paypalRebornModuleHandlers.handleGetDeviceDataFromDataCollectorResult(
//            result,
//            e,
//            promiseRef
//          )
//        }
//      } else {
//        throw Exception("Not Initialized")
//      }
//    } catch (ex: Exception) {
//      promiseRef.reject(
//        EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
//        ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.value,
//        PaypalDataConverter.createError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ex.message)
//      )
//    }
//  }
//
//  @ReactMethod
//  fun requestOneTimePayment(data: ReadableMap, localPromise: Promise) {
//    try {
//      promiseRef = localPromise
//      currentActivityRef = getCurrentActivity() as FragmentActivity
//      braintreeClientRef = BraintreeClient(currentActivityRef, data.getString("clientToken") ?: "")
//
//      if (this::currentActivityRef.isInitialized && this::braintreeClientRef.isInitialized) {
//        payPalClientRef = PayPalClient(braintreeClientRef)
//        val checkoutRequest: PayPalCheckoutRequest = PaypalDataConverter.createCheckoutRequest(data)
//        payPalClientRef.tokenizePayPalAccount(currentActivityRef, checkoutRequest) { e: Exception?
//          ->
//          handlePayPalAccountNonceResult(null, e)
//        }
//      } else {
//        throw Exception()
//      }
//    } catch (ex: Exception) {
//      localPromise.reject(
//        EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
//        ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.value,
//        PaypalDataConverter.createError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ex.message)
//      )
//    }
//  }

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
              paypalRebornModuleHandlers.onCardTokenizeSuccessHandler(
                cardResult.nonce,
                promiseRef
              )
            }
            is CardResult.Failure -> {
              paypalRebornModuleHandlers.onCardTokenizeFailure(cardResult.error, promiseRef)
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
        PaypalDataConverter.createError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ex.message)
      )
    }
  }

  @ReactMethod
  fun request3DSecurePaymentCheck(data: ReadableMap, localPromise: Promise) {
    try {
      promiseRef = localPromise
      currentActivityRef = getCurrentActivity() as FragmentActivity
      if (this::currentActivityRef.isInitialized) {
        threeDSecureClient =
          ThreeDSecureClient(currentActivityRef, data.getString("clientToken") ?: "")
        val threeDSecureRequest: ThreeDSecureRequest =
          PaypalDataConverter.create3DSecureRequest(data)
        threeDSecureClient.createPaymentAuthRequest(
          currentActivityRef,
          threeDSecureRequest
        ) { threeDSecureResponse ->
          when (threeDSecureResponse) {
            is ThreeDSecurePaymentAuthRequest.ReadyToLaunch -> {
              threeDSecureLauncher.launch(threeDSecureResponse)
            }

            is ThreeDSecurePaymentAuthRequest.LaunchNotRequired -> {
              paypalRebornModuleHandlers.onThreeDSecureSuccessHandler(
                threeDSecureResponse.nonce,
                localPromise
              )
            }

            is ThreeDSecurePaymentAuthRequest.Failure -> { /* handle error */
              paypalRebornModuleHandlers.onThreeDSecureFailure(
                threeDSecureResponse.error,
                localPromise
              )
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
        PaypalDataConverter.createError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ex.message)
      )
    }
  }

  //
//  public fun handleCardTokenizeResult(
//    cardNonce: CardNonce?,
//    error: Exception?,
//  ) {
//    if (error != null) {
//      paypalRebornModuleHandlers.onCardTokenizeFailure(error, promiseRef)
//      return
//    }
//    if (cardNonce != null) {
//      paypalRebornModuleHandlers.onCardTokenizeSuccessHandler(cardNonce, promiseRef)
//    }
//  }
//
//  public fun handleThreeDSecureResponse(
//    threeDSecureResult: ThreeDSecureResult?,
//    error: Exception?,
//  ) {
//    if (error != null) {
//      paypalRebornModuleHandlers.onThreeDSecureFailure(error, promiseRef)
//      return
//    }
//    if (threeDSecureResult != null) {
//      paypalRebornModuleHandlers.onThreeDSecureSuccessHandler(threeDSecureResult, promiseRef)
//    }
//  }
//
//  public fun handlePayPalAccountNonceResult(
//    payPalAccountNonce: PayPalAccountNonce?,
//    error: Exception?,
//  ) {
//    if (error != null) {
//      paypalRebornModuleHandlers.onPayPalFailure(error, promiseRef)
//      return
//    }
//    if (payPalAccountNonce != null) {
//      paypalRebornModuleHandlers.onPayPalSuccessHandler(payPalAccountNonce, promiseRef)
//    }
//  }
  fun handleReturnToAppPaypal(intent: Intent) {
    val pendingRequest: PayPalPendingRequest.Started? =
      ExpoBraintreePendingRequestStore.instance.getPayPalPendingRequest(reactContextRef)
    promiseRef.resolve(pendingRequest.toString())
    if (pendingRequest != null) {
      val paymentAuthResult =
        payPalLauncher.handleReturnToApp(pendingRequest, intent)
      promiseRef.resolve(paymentAuthResult.toString())
      if (paymentAuthResult is PayPalPaymentAuthResult.Success) {
        promiseRef.resolve("(paymentAuthResult is PayPalPaymentAuthResult.Success) {")
      } else {
        promiseRef.reject("User did not complete payment flow\"")
      }
      ExpoBraintreePendingRequestStore.instance.clearPayPalPendingRequest(reactContextRef)
    }


    // ExpoBraintreePendingRequestStore.instance.getPayPalPendingRequest(reactContextRef)?.let {
    //   when (val paymentAuthResult =
    //     payPalLauncher.handleReturnToApp(PayPalPendingRequest.Started(it), intent)) {
    //     is PayPalPaymentAuthResult.Success -> {
    //       completePayPalFlow(paymentAuthResult)
    //       // clear stored PayPalPendingRequest.Success
    //     }

    //     is PayPalPaymentAuthResult.NoResult -> {
    //       // user returned to app without completing PayPal flow, handle accordingly
    //     }

    //     is PayPalPaymentAuthResult.Failure -> {
    //       // handle error case
    //     }
    //   }
    // }
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
      handleReturnToAppPaypal(currentActivityRef.getIntent())
    }
  }

  override fun onNewIntent(p0: Intent) {
    if (this::currentActivityRef.isInitialized) {
      currentActivityRef.setIntent(p0)
      handleReturnToAppPaypal(p0)
    }
  }

}
