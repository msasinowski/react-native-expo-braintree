package com.margelo.nitro.expobraintree

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentActivity
import com.braintreepayments.api.card.CardClient
import com.braintreepayments.api.card.CardNonce
import com.braintreepayments.api.card.CardResult
import com.braintreepayments.api.core.UserCanceledException
import com.braintreepayments.api.datacollector.DataCollector
import com.braintreepayments.api.datacollector.DataCollectorResult
import com.braintreepayments.api.googlepay.*
import com.braintreepayments.api.paypal.*
import com.braintreepayments.api.threedsecure.*
import com.braintreepayments.api.venmo.*
import com.margelo.nitro.expobraintree.converters.CardDataConverter
import com.margelo.nitro.expobraintree.converters.GooglePayDataConverter
import com.margelo.nitro.expobraintree.converters.PayPalDataConverter
import com.margelo.nitro.expobraintree.converters.SharedDataConverter
import com.margelo.nitro.expobraintree.converters.SharedDataConverter.Companion.createDataCollectorRequest
import com.margelo.nitro.expobraintree.converters.VenmoDataConverter
import com.margelo.nitro.expobraintree.enums.EXCEPTION_TYPES
import com.margelo.nitro.expobraintree.enums.ERROR_TYPES
import com.margelo.nitro.expobraintree.enums.GOOGLE_PAY_ERROR_TYPES
import com.margelo.nitro.expobraintree.enums.THREE_D_SECURE_ERROR_TYPES
import com.margelo.nitro.expobraintree.handlers.ExpoBraintreeModuleHandlers
import com.margelo.nitro.expobraintree.store.PendingRequestStore
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableNativeMap
import com.facebook.react.bridge.ReadableType
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeMap
import org.json.JSONObject

import com.margelo.nitro.core.Promise
import com.margelo.nitro.NitroModules
import com.facebook.proguard.annotations.DoNotStrip
import java.lang.ref.WeakReference

@DoNotStrip
class ExpoBraintree : HybridExpoBraintreeSpec() {

  private val moduleHandlers = ExpoBraintreeModuleHandlers()
  private var threeDSecureClientRefInstance: ThreeDSecureClient? = null
  private var promiseRefInstance: Promise<*>? = null
  private var payPalClientRefInstance: PayPalClient? = null
  private var venmoClientRefInstance: VenmoClient? = null
  private var googlePayClientRefInstance: GooglePayClient? = null
  private val payPalLauncher by lazy { PayPalLauncher() }
  private val venmoLauncher by lazy { VenmoLauncher() }

  private val activityLifecycleCallbacks: Application.ActivityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
    override fun onActivityResumed(activity: Activity) {
      if (activity !is FragmentActivity) return
      val activePromise = promiseRefInstance ?: return
      val intent = activity.intent
      if (intent?.data != null) {
        handlePendingBrowserReturn(activity, intent, activePromise)
        activity.intent = Intent(Intent.ACTION_MAIN)
      } else if (isWebPaymentPending() && threeDSecureClientRefInstance == null) {
        handlePendingBrowserCancel(activity, activePromise)
      }
    }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
  }

  init {
    currentInstance = WeakReference(this)
    try {
      val app = NitroModules.applicationContext?.applicationContext as? Application
      if (app != null) {
        app.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
      }
    } catch (_: Exception) {}
  }

  private fun getReactContext(): ReactApplicationContext {
    return NitroModules.applicationContext
      ?: throw IllegalStateException("No ReactApplicationContext available")
  }

  private fun requireActivity(): FragmentActivity {
    return getReactContext().currentActivity as? FragmentActivity
      ?: throw IllegalStateException("No FragmentActivity available")
  }

  private fun Map<String, String>.toReadableMap(): ReadableMap {
    val map = WritableNativeMap()
    forEach { (key, value) -> map.putString(key, value) }
    return map
  }

  private fun isWebPaymentPending(): Boolean {
    val context = getReactContext()
    return PendingRequestStore.instance.getPayPalPendingRequest(context) != null
      || PendingRequestStore.instance.getVenmoPendingRequest(context) != null
  }

  @Suppress("UNCHECKED_CAST")
  private fun handlePendingBrowserReturn(activity: FragmentActivity, intent: Intent, activePromise: Promise<*>) {
    val promise = activePromise as? Promise<Map<String, String>> ?: return
    PendingRequestStore.instance.getPayPalPendingRequest(activity)?.let { pending ->
      val result = payPalLauncher.handleReturnToApp(pending, intent)
      when (result) {
        is PayPalPaymentAuthResult.Success -> {
          payPalClientRefInstance?.tokenize(result) { tokenResult ->
            handlePayPalTokenResult(tokenResult, promise)
            PendingRequestStore.instance.clearPayPalPendingRequest(activity)
            clearInstanceReferences()
          }
        }
        is PayPalPaymentAuthResult.Failure -> {
          moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, result.error?.message, promise)
          PendingRequestStore.instance.clearPayPalPendingRequest(activity)
          clearInstanceReferences()
        }
        is PayPalPaymentAuthResult.NoResult -> {}
      }
    }
    PendingRequestStore.instance.getVenmoPendingRequest(activity)?.let { pending ->
      val result = venmoLauncher.handleReturnToApp(pending, intent)
      when (result) {
        is VenmoPaymentAuthResult.Success -> {
          venmoClientRefInstance?.tokenize(result) { tokenResult ->
            handleVenmoTokenResult(tokenResult, promise)
            PendingRequestStore.instance.clearVenmoPendingRequest(activity)
            clearInstanceReferences()
          }
        }
        is VenmoPaymentAuthResult.Failure -> {
          moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, result.error?.message, promise)
          PendingRequestStore.instance.clearVenmoPendingRequest(activity)
          clearInstanceReferences()
        }
        is VenmoPaymentAuthResult.NoResult -> {}
      }
    }
  }

  private fun handlePendingBrowserCancel(activity: FragmentActivity, activePromise: Promise<*>) {
    @Suppress("UNCHECKED_CAST")
    val promise = activePromise as? Promise<Map<String, String>>
    if (promise != null) moduleHandlers.onCancel(promise)
    PendingRequestStore.instance.clearPayPalPendingRequest(activity)
    PendingRequestStore.instance.clearVenmoPendingRequest(activity)
    clearInstanceReferences()
  }

  private fun clearInstanceReferences() {
    threeDSecureClientRefInstance = null
    promiseRefInstance = null
    payPalClientRefInstance = null
    venmoClientRefInstance = null
    googlePayClientRefInstance = null
  }

  private fun WritableMap.toMapString(): Map<String, String> {
    val nativeMap = this as? ReadableNativeMap ?: return emptyMap()
    val result = mutableMapOf<String, String>()
    val iterator = nativeMap.keySetIterator()
    while (iterator.hasNextKey()) {
      val key = iterator.nextKey()
      when (nativeMap.getType(key)) {
        ReadableType.String -> result[key] = nativeMap.getString(key) ?: ""
        ReadableType.Boolean -> result[key] = nativeMap.getBoolean(key).toString()
        ReadableType.Number -> result[key] = nativeMap.getDouble(key).toString()
        ReadableType.Map -> {
          nativeMap.getMap(key)?.let { subMap ->
            result[key] = readableMapToJsonString(subMap)
          }
        }
        ReadableType.Array -> {
          result[key] = nativeMap.getArray(key).toString()
        }
        ReadableType.Null -> {}
      }
    }
    return result
  }

  private fun readableMapToJsonString(map: ReadableMap): String {
    if (map !is ReadableNativeMap) return "{}"
    val json = JSONObject()
    val iterator = map.keySetIterator()
    while (iterator.hasNextKey()) {
      val key = iterator.nextKey()
      when (map.getType(key)) {
        ReadableType.String -> json.put(key, map.getString(key) ?: "")
        ReadableType.Boolean -> json.put(key, map.getBoolean(key))
        ReadableType.Number -> json.put(key, map.getDouble(key))
        ReadableType.Map -> {
          map.getMap(key)?.let { subMap -> json.put(key, readableMapToJsonString(subMap)) }
        }
        ReadableType.Array -> {
          json.put(key, map.getArray(key).toString())
        }
        ReadableType.Null -> json.put(key, JSONObject.NULL)
      }
    }
    return json.toString()
  }

  private fun handlePayPalTokenResult(tokenResult: PayPalResult, promise: Promise<Map<String, String>>) {
    when (tokenResult) {
      is PayPalResult.Success -> moduleHandlers.onPayPalSuccessHandler(tokenResult.nonce, promise)
      is PayPalResult.Failure -> {
        val error = tokenResult.error
        if (error is UserCanceledException) {
          moduleHandlers.resolveError(EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.value, ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.value, error.message, promise)
        } else {
          moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, error?.message, promise)
        }
      }
      is PayPalResult.Cancel -> moduleHandlers.onCancel(promise)
    }
  }

  private fun handleVenmoTokenResult(tokenResult: VenmoResult, promise: Promise<Map<String, String>>) {
    when (tokenResult) {
      is VenmoResult.Success -> moduleHandlers.onVenmoSuccessHandler(tokenResult.nonce, promise)
      is VenmoResult.Failure -> {
        val error = tokenResult.error
        if (error is UserCanceledException) {
          moduleHandlers.resolveError(EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.value, ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.value, error.message, promise)
        } else {
          moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, error?.message, promise)
        }
      }
      is VenmoResult.Cancel -> moduleHandlers.onCancel(promise)
    }
  }

  private fun getDefaultFallbackScheme(): String {
    val context = getReactContext()
    return "${context.packageName}.braintree"
  }

  override fun requestBillingAgreement(options: Map<String, String>): Promise<Map<String, String>> {
    val promise = Promise<Map<String, String>>()
    try {
      val activity = requireActivity()
      clearInstanceReferences()
      promiseRefInstance = promise
      val fallbackUrl = options["fallbackUrlScheme"] ?: getDefaultFallbackScheme()
      val client = PayPalClient(
        activity,
        options["clientToken"] ?: "",
        Uri.parse(options["merchantAppLink"] ?: ""),
        fallbackUrl
      )
      payPalClientRefInstance = client
      val vaultRequest = PayPalDataConverter.createVaultRequest(options.toReadableMap())
      client.createPaymentAuthRequest(getReactContext(), vaultRequest) { authRequest ->
        when (authRequest) {
          is PayPalPaymentAuthRequest.ReadyToLaunch -> {
            val pending = payPalLauncher.launch(activity, authRequest)
            if (pending is PayPalPendingRequest.Failure) {
              moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, pending.error?.message, promise)
              clearInstanceReferences()
            } else if (pending is PayPalPendingRequest.Started) {
              PendingRequestStore.instance.putPayPalPendingRequest(getReactContext(), pending)
            }
          }
          is PayPalPaymentAuthRequest.Failure -> {
            moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, authRequest.error?.message, promise)
            clearInstanceReferences()
          }
        }
      }
    } catch (ex: Exception) {
      moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, ex?.message, promise)
            clearInstanceReferences()
    }
    return promise
  }

  override fun requestOneTimePayment(options: Map<String, String>): Promise<Map<String, String>> {
    val promise = Promise<Map<String, String>>()
    val activity = requireActivity()
    clearInstanceReferences()
    promiseRefInstance = promise
    try {
      val fallbackUrl = options["fallbackUrlScheme"] ?: getDefaultFallbackScheme()
      val client = PayPalClient(
        activity,
        options["clientToken"] ?: "",
        Uri.parse(options["merchantAppLink"] ?: ""),
        fallbackUrl
      )
      payPalClientRefInstance = client
      val checkoutRequest = PayPalDataConverter.createCheckoutRequest(options.toReadableMap())
      client.createPaymentAuthRequest(getReactContext(), checkoutRequest) { authRequest ->
        when (authRequest) {
          is PayPalPaymentAuthRequest.ReadyToLaunch -> {
            val pending = payPalLauncher.launch(activity, authRequest)
            if (pending is PayPalPendingRequest.Failure) {
              moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, pending.error?.message, promise)
              clearInstanceReferences()
            } else if (pending is PayPalPendingRequest.Started) {
              PendingRequestStore.instance.putPayPalPendingRequest(getReactContext(), pending)
            }
          }
          is PayPalPaymentAuthRequest.Failure -> {
            moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, authRequest.error?.message, promise)
            clearInstanceReferences()
          }
        }
      }
    } catch (ex: Exception) {
      moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, ex.message, promise)
      clearInstanceReferences()
    }
    return promise
  }
  override fun tokenizeCardData(options: Map<String, String>): Promise<Map<String, String>> {
    val promise = Promise<Map<String, String>>()
    clearInstanceReferences()
    promiseRefInstance = promise
    try {
      val cardClient = CardClient(getReactContext(), options["clientToken"] ?: "")
      val cardRequest = CardDataConverter.createTokenizeCardRequest(options.toReadableMap())
      cardClient.tokenize(cardRequest) { result ->
        when (result) {
          is CardResult.Success -> moduleHandlers.onCardTokenizeSuccessHandler(result.nonce, promise)
          is CardResult.Failure -> moduleHandlers.onCardTokenizeFailure(result.error, promise)
        }
        clearInstanceReferences()
      }
    } catch (ex: Exception) {
      moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, ex?.message, promise)
            clearInstanceReferences()
    }
    return promise
  }

  override fun getDeviceDataFromDataCollector(options: Map<String, String>): Promise<Map<String, String>> {
    val promise = Promise<Map<String, String>>()
    try {
      val activity = requireActivity()
      val dataCollector = DataCollector(activity, options["clientToken"] ?: "")
      val request = createDataCollectorRequest(options.toReadableMap())
      dataCollector.collectDeviceData(getReactContext(), request) { result ->
        when (result) {
          is DataCollectorResult.Success -> promise.resolve(mapOf("data" to result.deviceData))
          is DataCollectorResult.Failure -> {
            promise.resolve(moduleHandlers.errorMap(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.DATA_COLLECTOR_ERROR.value, result.error?.message))
          }
        }
      }
    } catch (ex: Exception) {
      promise.resolve(moduleHandlers.errorMap(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.DATA_COLLECTOR_ERROR.value, ex.message))
    }
    return promise
  }

  override fun requestVenmoNonce(options: Map<String, String>): Promise<Map<String, String>> {
    val promise = Promise<Map<String, String>>()
    val activity = requireActivity()
    clearInstanceReferences()
    promiseRefInstance = promise
    try {
      val fallbackUrl = options["fallbackUrlScheme"] ?: getDefaultFallbackScheme()
      val client = VenmoClient(
        activity,
        options["clientToken"] ?: "",
        Uri.parse(options["merchantAppLink"] ?: ""),
        fallbackUrl
      )
      venmoClientRefInstance = client
      val request = VenmoDataConverter.createRequest(options.toReadableMap())
      client.createPaymentAuthRequest(getReactContext(), request) { authRequest ->
        when (authRequest) {
          is VenmoPaymentAuthRequest.ReadyToLaunch -> {
            val pending = venmoLauncher.launch(activity, authRequest)
            if (pending is VenmoPendingRequest.Failure) {
              moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, pending.error?.message, promise)
              clearInstanceReferences()
            } else if (pending is VenmoPendingRequest.Started) {
              PendingRequestStore.instance.putVenmoPendingRequest(getReactContext(), pending)
            }
          }
          is VenmoPaymentAuthRequest.Failure -> {
            moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, authRequest.error?.message, promise)
            clearInstanceReferences()
          }
        }
      }
    } catch (ex: Exception) {
      moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, ex?.message, promise)
            clearInstanceReferences()
    }
    return promise
  }

  override fun request3DSecurePaymentCheck(options: Map<String, String>): Promise<Map<String, String>> {
    val promise = Promise<Map<String, String>>()
    val activity = requireActivity()
    clearInstanceReferences()
    promiseRefInstance = promise
    try {
      val launcher = getStoredThreeDSecureLauncher()
      if (launcher == null) {
        moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, "ThreeDSecureLauncher not initialized - call initThreeDSecure() in MainActivity.onCreate()", promise)
        return promise
      }
      val client = ThreeDSecureClient(activity, options["clientToken"] ?: "")
      threeDSecureClientRefInstance = client
      val request = CardDataConverter.create3DSecureRequest(options.toReadableMap())
      client.createPaymentAuthRequest(activity, request) { response ->
        when (response) {
          is ThreeDSecurePaymentAuthRequest.ReadyToLaunch -> {
            activity.runOnUiThread { launcher.launch(response) }
          }
          is ThreeDSecurePaymentAuthRequest.LaunchNotRequired -> {
            handleThreeDSecureNonLaunch(response, promise)
          }
          is ThreeDSecurePaymentAuthRequest.Failure -> {
            moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, response.error?.message, promise)
            clearInstanceReferences()
          }
        }
      }
    } catch (ex: Exception) {
      moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, ex?.message, promise)
            clearInstanceReferences()
    }
    return promise
  }

  override fun requestGooglePayPayment(options: Map<String, String>): Promise<Map<String, String>> {
    val promise = Promise<Map<String, String>>()
    val activity = requireActivity()
    clearInstanceReferences()
    promiseRefInstance = promise
    try {
      val launcher = getStoredGooglePayLauncher()
      if (launcher == null) {
        moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, "GooglePayLauncher not initialized", promise)
        return promise
      }
      val googlePayClient = GooglePayClient(activity, options["clientToken"] ?: "")
      googlePayClientRefInstance = googlePayClient
      val request = GooglePayDataConverter.createPaymentRequest(options.toReadableMap())
      googlePayClient.createPaymentAuthRequest(request) { authRequest ->
        try {
          when (authRequest) {
            is GooglePayPaymentAuthRequest.ReadyToLaunch -> {
              activity.runOnUiThread { launcher.launch(authRequest) }
            }
            is GooglePayPaymentAuthRequest.Failure -> {
              moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, authRequest.error?.message, promise)
              clearInstanceReferences()
            }
          }
        } catch (ex: Exception) {
          moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, ex.message, promise)
          clearInstanceReferences()
        }
      }
    } catch (ex: Exception) {
      moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, ex.message, promise)
      clearInstanceReferences()
    }
    return promise
  }

  private fun handleThreeDSecureTokenizeResult(result: ThreeDSecureResult, promise: Promise<Map<String, String>>) {
    when (result) {
      is ThreeDSecureResult.Success -> moduleHandlers.onThreeDSecureSuccessHandler(result.nonce, promise)
      is ThreeDSecureResult.Failure -> moduleHandlers.onThreeDSecureFailure(result.error, promise)
      is ThreeDSecureResult.Cancel -> moduleHandlers.onCancel(promise)
    }
    clearInstanceReferences()
  }

  private fun handleThreeDSecureNonLaunch(response: ThreeDSecurePaymentAuthRequest.LaunchNotRequired, promise: Promise<Map<String, String>>) {
    moduleHandlers.onThreeDSecureSuccessHandler(response.nonce, promise)
    clearInstanceReferences()
  }

  private fun handleGooglePayTokenizeResult(result: GooglePayResult, promise: Promise<Map<String, String>>) {
    when (result) {
      is GooglePayResult.Success -> {
        val cardNonce = result.nonce as? GooglePayCardNonce
        if (cardNonce != null) {
          moduleHandlers.onGooglePaySuccessHandler(cardNonce, promise)
        } else {
          promise.resolve(mapOf("nonce" to result.nonce.string))
        }
      }
      is GooglePayResult.Failure -> {
        moduleHandlers.resolveError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value, result.error?.message, promise)
      }
      is GooglePayResult.Cancel -> {
        moduleHandlers.onCancel(promise)
      }
    }
    clearInstanceReferences()
  }

  companion object {
    @JvmStatic
    private var staticGooglePayLauncher: GooglePayLauncher? = null
    @JvmStatic
    private var staticThreeDSecureLauncher: ThreeDSecureLauncher? = null
    @JvmStatic
    private var currentInstance: WeakReference<ExpoBraintree>? = null

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun initGooglePay(activity: FragmentActivity) {
      staticGooglePayLauncher = GooglePayLauncher(activity) { paymentAuthResult ->
        currentInstance?.get()?.let { instance ->
          val promise = instance.promiseRefInstance as? Promise<Map<String, String>>
          val client = instance.googlePayClientRefInstance
          if (client != null && promise != null) {
            client.tokenize(paymentAuthResult) { result ->
              Handler(Looper.getMainLooper()).post {
                try {
                  instance.handleGooglePayTokenizeResult(result, promise)
                } catch (_: Exception) {
                }
              }
            }
          }
        }
      }
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun initThreeDSecure(activity: FragmentActivity) {
      staticThreeDSecureLauncher = ThreeDSecureLauncher(activity) { paymentAuthResult ->
        currentInstance?.get()?.let { instance ->
          val promise = instance.promiseRefInstance as? Promise<Map<String, String>>
          val client = instance.threeDSecureClientRefInstance
          if (client != null && promise != null) {
            client.tokenize(paymentAuthResult) { result ->
              Handler(Looper.getMainLooper()).post {
                try {
                  instance.handleThreeDSecureTokenizeResult(result, promise)
                } catch (_: Exception) {
                }
              }
            }
          }
        }
      }
    }

    @JvmStatic
    fun getStoredGooglePayLauncher(): GooglePayLauncher? = staticGooglePayLauncher

    @JvmStatic
    fun getStoredThreeDSecureLauncher(): ThreeDSecureLauncher? = staticThreeDSecureLauncher
  }
}
