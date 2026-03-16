package com.expobraintree

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.braintreepayments.api.card.CardClient
import com.braintreepayments.api.card.CardResult
import com.braintreepayments.api.datacollector.DataCollector
import com.braintreepayments.api.datacollector.DataCollectorResult
import com.braintreepayments.api.googlepay.*
import com.braintreepayments.api.paypal.*
import com.braintreepayments.api.threedsecure.*
import com.braintreepayments.api.venmo.*
import com.expobraintree.SharedDataConverter.Companion.createDataCollectorRequest
import com.facebook.react.bridge.*

class ExpoBraintreeModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext), ActivityEventListener, LifecycleEventListener {

    private val NAME = "ExpoBraintree"
    private var reactContextRef: Context = reactContext

    private val fragmentActivity: FragmentActivity?
        get() = currentActivity as? FragmentActivity

    init {
        reactContext.addLifecycleEventListener(this)
        reactContext.addActivityEventListener(this)
    }

    companion object {
        lateinit var payPalLauncher: PayPalLauncher
        lateinit var venmoLauncher: VenmoLauncher
        lateinit var threeDSecureLauncher: ThreeDSecureLauncher
        lateinit var googlePayLauncher: GooglePayLauncher
        
        private val moduleHandlers: ExpoBraintreeModuleHandlers = ExpoBraintreeModuleHandlers()

        private var threeDSecureClientRefInstance: ThreeDSecureClient? = null
        private var promiseRefInstance: Promise? = null
        private var payPalClientRefInstance: PayPalClient? = null
        private var venmoClientRefInstance: VenmoClient? = null
        private var googlePayClientRefInstance: GooglePayClient? = null

        fun init() {
            initPayPal()
            initVenmo()
        }

        fun initPayPal() { payPalLauncher = PayPalLauncher() }
        fun initVenmo() { venmoLauncher = VenmoLauncher() }

        fun initThreeDSecure(activity: FragmentActivity) {
            threeDSecureLauncher = ThreeDSecureLauncher(activity) { paymentAuthResult ->
                val client = threeDSecureClientRefInstance
                val promise = promiseRefInstance

                if (client != null && promise != null) {
                    client.tokenize(paymentAuthResult) { result ->
                        when (result) {
                            is ThreeDSecureResult.Success -> {
                                if (result.nonce.threeDSecureInfo.liabilityShifted) {
                                    moduleHandlers.onThreeDSecureSuccessHandler(result.nonce, promise)
                                } else {
                                    promise.reject(
                                        EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value,
                                        THREE_D_SECURE_ERROR_TYPES.D_SECURE_LIABILITY_NOT_SHIFTED.value
                                    )
                                }
                            }
                            is ThreeDSecureResult.Failure -> {
                                promise.reject(EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value, result.error.message ?: "Unknown error")
                            }
                            is ThreeDSecureResult.Cancel -> {
                                promise.reject(EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.value, ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.value)
                            }
                        }
                        clearStaticReferences()
                    }
                }
            }
        }

        fun initGooglePay(activity: FragmentActivity) {
            googlePayLauncher = GooglePayLauncher(activity) { paymentAuthResult ->
                val promise = promiseRefInstance ?: return@GooglePayLauncher
                val client = googlePayClientRefInstance ?: return@GooglePayLauncher

                client.tokenize(paymentAuthResult) { result ->
                    when (result) {
                        is GooglePayResult.Success -> {
                            // Rzutowanie na GooglePayCardNonce, aby pasowało do handlera
                            val cardNonce = result.nonce as? GooglePayCardNonce
                            if (cardNonce != null) {
                                moduleHandlers.onGooglePaySuccessHandler(cardNonce, promise)
                            } else {
                                // Jeśli to inny typ nonca (np. PayPal), wysyłamy ogólnie
                                promise.resolve(result.nonce.string)
                            }
                        }
                        is GooglePayResult.Failure -> {
                            promise.reject(
                                GOOGLE_PAY_ERROR_TYPES.GOOGLE_PAY_FAILED.value,
                                result.error.message ?: "Google Pay Failed"
                            )
                        }
                        is GooglePayResult.Cancel -> {
                            promise.reject(
                                EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.value,
                                "User cancelled Google Pay"
                            )
                        }
                    }
                    clearStaticReferences()
                }
            }
        }

        fun clearStaticReferences() {
            threeDSecureClientRefInstance = null
            promiseRefInstance = null
            payPalClientRefInstance = null
            venmoClientRefInstance = null
            googlePayClientRefInstance = null
        }
    }

    /**
     * Checks if there is an active PayPal or Venmo request stored in the PendingStore.
     */
    private fun isWebPaymentPending(): Boolean {
        val activity = fragmentActivity ?: return false
        val hasPayPal = PendingRequestStore.instance.getPayPalPendingRequest(activity) != null
        val hasVenmo = PendingRequestStore.instance.getVenmoPendingRequest(activity) != null
        return hasPayPal || hasVenmo
    }

    // --- Google Pay Implementation ---
    @ReactMethod
    fun requestGooglePayPayment(data: ReadableMap, localPromise: Promise) {
        val activity = fragmentActivity ?: return rejectNoActivity(localPromise)
        clearStaticReferences()
        promiseRefInstance = localPromise

        try {
            val googlePayClient = GooglePayClient(activity, data.getString("clientToken") ?: "")
            googlePayClientRefInstance = googlePayClient

            googlePayClient.isReadyToPay(activity) { readinessResult ->
                if (readinessResult is GooglePayReadinessResult.ReadyToPay) {
                    val request = GooglePayDataConverter.createPaymentRequest(data)
                    googlePayClient.createPaymentAuthRequest(request) { authRequest ->
                        when (authRequest) {
                            is GooglePayPaymentAuthRequest.ReadyToLaunch -> googlePayLauncher.launch(authRequest)
                            is GooglePayPaymentAuthRequest.Failure -> {
                                localPromise.reject(
                                    GOOGLE_PAY_ERROR_TYPES.GOOGLE_PAY_FAILED.value,
                                    authRequest.error.message
                                )
                                clearStaticReferences()
                            }
                        }
                    }
                } else {
                    localPromise.reject(
                        GOOGLE_PAY_ERROR_TYPES.GOOGLE_PAY_NOT_AVAILABLE.value,
                        "Google Pay not available"
                    )
                    clearStaticReferences()
                }
            }
        } catch (ex: Exception) { handleKotlinException(localPromise, ex) }
    }

    @ReactMethod
    fun request3DSecurePaymentCheck(data: ReadableMap, localPromise: Promise) {
        val activity = fragmentActivity ?: return rejectNoActivity(localPromise)
        clearStaticReferences()
        promiseRefInstance = localPromise
        try {
            val client = ThreeDSecureClient(activity, data.getString("clientToken") ?: "")
            threeDSecureClientRefInstance = client
            val request = CardDataConverter.create3DSecureRequest(data)
            client.createPaymentAuthRequest(activity, request) { response ->
                when (response) {
                    is ThreeDSecurePaymentAuthRequest.ReadyToLaunch -> threeDSecureLauncher.launch(response)
                    is ThreeDSecurePaymentAuthRequest.LaunchNotRequired -> {
                        if (response.nonce.threeDSecureInfo.liabilityShifted) {
                            moduleHandlers.onThreeDSecureSuccessHandler(response.nonce, localPromise)
                        } else {
                            localPromise.reject(
                                EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value,
                                THREE_D_SECURE_ERROR_TYPES.D_SECURE_LIABILITY_NOT_SHIFTED.value
                            )
                        }
                        clearStaticReferences()
                    }
                    is ThreeDSecurePaymentAuthRequest.Failure -> {
                        localPromise.reject(EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value, response.error.message ?: "3DS Failed")
                        clearStaticReferences()
                    }
                }
            }
        } catch (ex: Exception) { handleKotlinException(localPromise, ex) }
    }

    @ReactMethod
    fun requestBillingAgreement(data: ReadableMap, localPromise: Promise) {
        val activity = fragmentActivity ?: return rejectNoActivity(localPromise)
        clearStaticReferences()
        promiseRefInstance = localPromise
        try {
            val client = PayPalClient(activity, data.getString("clientToken") ?: "", Uri.parse(data.getString("merchantAppLink") ?: ""), data.getString("fallbackUrlScheme"))
            payPalClientRefInstance = client
            val vaultRequest = PayPalDataConverter.createVaultRequest(data)
            client.createPaymentAuthRequest(reactContextRef, vaultRequest) { authRequest ->
                when (authRequest) {
                    is PayPalPaymentAuthRequest.ReadyToLaunch -> {
                        val pending = payPalLauncher.launch(activity, authRequest)
                        if (pending is PayPalPendingRequest.Failure) {
                            moduleHandlers.onFailure(pending.error, localPromise)
                            clearStaticReferences()
                        } else if (pending is PayPalPendingRequest.Started) {
                            PendingRequestStore.instance.putPayPalPendingRequest(reactContextRef, pending)
                        }
                    }
                    is PayPalPaymentAuthRequest.Failure -> {
                        moduleHandlers.onFailure(authRequest.error, localPromise)
                        clearStaticReferences()
                    }
                }
            }
        } catch (ex: Exception) { handleKotlinException(localPromise, ex) }
    }

    @ReactMethod
    fun requestOneTimePayment(data: ReadableMap, localPromise: Promise) {
        val activity = fragmentActivity ?: return rejectNoActivity(localPromise)
        clearStaticReferences()
        promiseRefInstance = localPromise
        try {
            val client = PayPalClient(activity, data.getString("clientToken") ?: "", Uri.parse(data.getString("merchantAppLink") ?: ""), data.getString("fallbackUrlScheme"))
            payPalClientRefInstance = client
            val checkoutRequest = PayPalDataConverter.createCheckoutRequest(data)
            client.createPaymentAuthRequest(reactContextRef, checkoutRequest) { authRequest ->
                when (authRequest) {
                    is PayPalPaymentAuthRequest.ReadyToLaunch -> {
                        val pending = payPalLauncher.launch(activity, authRequest)
                        if (pending is PayPalPendingRequest.Failure) {
                            moduleHandlers.onFailure(pending.error, localPromise)
                            clearStaticReferences()
                        } else if (pending is PayPalPendingRequest.Started) {
                            PendingRequestStore.instance.putPayPalPendingRequest(reactContextRef, pending)
                        }
                    }
                    is PayPalPaymentAuthRequest.Failure -> {
                        moduleHandlers.onFailure(authRequest.error, localPromise)
                        clearStaticReferences()
                    }
                }
            }
        } catch (ex: Exception) { handleKotlinException(localPromise, ex) }
    }

    @ReactMethod
    fun tokenizeCardData(data: ReadableMap, localPromise: Promise) {
        clearStaticReferences()
        promiseRefInstance = localPromise
        try {
            val cardClient = CardClient(reactContextRef, data.getString("clientToken") ?: "")
            val cardRequest = CardDataConverter.createTokenizeCardRequest(data)
            cardClient.tokenize(cardRequest) { result ->
                when (result) {
                    is CardResult.Success -> moduleHandlers.onCardTokenizeSuccessHandler(result.nonce, localPromise)
                    is CardResult.Failure -> moduleHandlers.onCardTokenizeFailure(result.error, localPromise)
                }
                clearStaticReferences()
            }
        } catch (ex: Exception) { handleKotlinException(localPromise, ex) }
    }

    @ReactMethod
    fun requestVenmoNonce(data: ReadableMap, localPromise: Promise) {
        val activity = fragmentActivity ?: return rejectNoActivity(localPromise)
        clearStaticReferences()
        promiseRefInstance = localPromise
        try {
            val client = VenmoClient(activity, data.getString("clientToken") ?: "", Uri.parse(data.getString("merchantAppLink") ?: ""), data.getString("fallbackUrlScheme"))
            venmoClientRefInstance = client
            val request = VenmoDataConverter.createRequest(data)
            client.createPaymentAuthRequest(reactContextRef, request) { authRequest ->
                when (authRequest) {
                    is VenmoPaymentAuthRequest.ReadyToLaunch -> {
                        val pending = venmoLauncher.launch(activity, authRequest)
                        if (pending is VenmoPendingRequest.Failure) {
                            moduleHandlers.onFailure(pending.error, localPromise)
                            clearStaticReferences()
                        } else if (pending is VenmoPendingRequest.Started) {
                            PendingRequestStore.instance.putVenmoPendingRequest(reactContextRef, pending)
                        }
                    }
                    is VenmoPaymentAuthRequest.Failure -> {
                        moduleHandlers.onFailure(authRequest.error, localPromise)
                        clearStaticReferences()
                    }
                }
            }
        } catch (ex: Exception) { handleKotlinException(localPromise, ex) }
    }

    @ReactMethod
    fun getDeviceDataFromDataCollector(data: ReadableMap, localPromise: Promise) {
        val activity = fragmentActivity ?: return rejectNoActivity(localPromise)
        try {
            val dataCollector = DataCollector(activity, data.getString("clientToken") ?: "")
            val request = createDataCollectorRequest(data)
            dataCollector.collectDeviceData(reactContextRef, request) { result ->
                when (result) {
                    is DataCollectorResult.Success -> moduleHandlers.handleGetDeviceDataFromDataCollectorResult(result.deviceData, null, localPromise)
                    is DataCollectorResult.Failure -> moduleHandlers.handleGetDeviceDataFromDataCollectorResult(null, result.error, localPromise)
                }
            }
        } catch (ex: Exception) { handleKotlinException(localPromise, ex) }
    }

    /**
     * Handles the return from PayPal/Venmo flows via Deep Linking.
     */
    private fun handleReturnToApp(intent: Intent) {
        val activity = fragmentActivity ?: return
        val promise = promiseRefInstance ?: return

        PendingRequestStore.instance.getPayPalPendingRequest(activity)?.let { pending ->
            val result = payPalLauncher.handleReturnToApp(pending, intent)
            when (result) {
                is PayPalPaymentAuthResult.Success -> {
                    payPalClientRefInstance?.tokenize(result) { tokenResult ->
                        when (tokenResult) {
                            is PayPalResult.Success -> moduleHandlers.onPayPalSuccessHandler(tokenResult.nonce, promise)
                            is PayPalResult.Failure -> moduleHandlers.onFailure(tokenResult.error, promise)
                            is PayPalResult.Cancel -> moduleHandlers.onCancel(Exception(EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.value), promise)
                        }
                        PendingRequestStore.instance.clearPayPalPendingRequest(activity)
                        clearStaticReferences()
                    }
                }
                is PayPalPaymentAuthResult.Failure -> {
                    moduleHandlers.onFailure(result.error, promise)
                    PendingRequestStore.instance.clearPayPalPendingRequest(activity)
                    clearStaticReferences()
                }
                is PayPalPaymentAuthResult.NoResult -> {}
            }
        }

        PendingRequestStore.instance.getVenmoPendingRequest(activity)?.let { pending ->
            val result = venmoLauncher.handleReturnToApp(pending, intent)
            when (result) {
                is VenmoPaymentAuthResult.Success -> {
                    venmoClientRefInstance?.tokenize(result) { tokenResult ->
                        when (tokenResult) {
                            is VenmoResult.Success -> moduleHandlers.onVenmoSuccessHandler(tokenResult.nonce, promise)
                            is VenmoResult.Failure -> moduleHandlers.onFailure(tokenResult.error, promise)
                            is VenmoResult.Cancel -> moduleHandlers.onCancel(Exception(EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.value), promise)
                        }
                        PendingRequestStore.instance.clearVenmoPendingRequest(activity)
                        clearStaticReferences()
                    }
                }
                is VenmoPaymentAuthResult.Failure -> {
                    moduleHandlers.onFailure(result.error, promise)
                    PendingRequestStore.instance.clearVenmoPendingRequest(activity)
                    clearStaticReferences()
                }
                is VenmoPaymentAuthResult.NoResult -> {}
            }
        }
    }

    override fun onHostResume() {
        val activity = fragmentActivity ?: return
        val intent = activity.intent
        
        // Check if returning via a valid PayPal/Venmo deep link
        if (intent?.data != null) {
            handleReturnToApp(intent)
            activity.intent = Intent(Intent.ACTION_MAIN) // Clear data to avoid re-processing
        } else {
            // Scenario: App resumed but no intent data found.
            // Check if we were waiting for a web-based flow (PayPal/Venmo).
            val isThreeDSActive = threeDSecureClientRefInstance != null
            
            // Only cancel if a web flow was pending AND we are NOT in a 3DS flow.
            // 3DS v6 manages its own result callback and often resumes with null intent.
            if (isWebPaymentPending() && !isThreeDSActive) {
                promiseRefInstance?.let { promise ->
                    moduleHandlers.onCancel(Exception(EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.value), promise)
                    PendingRequestStore.instance.clearPayPalPendingRequest(activity)
                    PendingRequestStore.instance.clearVenmoPendingRequest(activity)
                    clearStaticReferences()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        fragmentActivity?.let { activity ->
            activity.intent = intent // Update intent for onHostResume
            if (intent.data != null) {
                handleReturnToApp(intent)
            }
        }
    }

    private fun handleKotlinException(promise: Promise, ex: Exception) {
        val message = ex.message ?: "Kotlin Exception"
        promise.reject(
            EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value, 
            message,                                
            SharedDataConverter.createError(EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value, message)
        )
        clearStaticReferences()
    }

    private fun rejectNoActivity(promise: Promise) {
        promise.reject(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.value, SharedDataConverter.createError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, "Activity not initialized"))
        clearStaticReferences()
    }

    override fun getName() = NAME
    override fun onHostPause() {}
    override fun onHostDestroy() { clearStaticReferences() }
    override fun onActivityResult(p0: Activity, p1: Int, p2: Int, p3: Intent?) {}
}