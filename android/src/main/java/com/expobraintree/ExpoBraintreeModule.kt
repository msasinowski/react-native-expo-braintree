package com.expobraintree

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.braintreepayments.api.card.Card
import com.braintreepayments.api.card.CardClient
import com.braintreepayments.api.card.CardResult
import com.braintreepayments.api.datacollector.DataCollector
import com.braintreepayments.api.datacollector.DataCollectorResult
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
        private val moduleHandlers: ExpoBraintreeModuleHandlers = ExpoBraintreeModuleHandlers()

        private var threeDSecureClientRefInstance: ThreeDSecureClient? = null
        private var promiseRefInstance: Promise? = null
        private var payPalClientRefInstance: PayPalClient? = null
        private var venmoClientRefInstance: VenmoClient? = null

        fun initPayPal() { payPalLauncher = PayPalLauncher() }
        fun initVenmo() { venmoLauncher = VenmoLauncher() }

        fun initThreeDSecure(activity: FragmentActivity) {
            threeDSecureLauncher = ThreeDSecureLauncher(activity) { paymentAuthResult ->
                val client = threeDSecureClientRefInstance
                val promise = promiseRefInstance

                if (client != null && promise != null) {
                    client.tokenize(paymentAuthResult) { result ->
                        when (result) {
                            is ThreeDSecureResult.Success -> moduleHandlers.onThreeDSecureSuccessHandler(result.nonce, promise)
                            is ThreeDSecureResult.Failure -> moduleHandlers.onThreeDSecureFailure(result.error, promise)
                            is ThreeDSecureResult.Cancel -> moduleHandlers.onCancel(Exception("Flow Cancelled"), promise)
                        }
                        clearStaticReferences()
                    }
                }
            }
        }

        // Init method is still in place but it is by default only initialize Paypal and venmo
        fun init() {
            initPayPal()
            initVenmo()
        }

        fun setupThreeDSecureInstance(client: ThreeDSecureClient, promise: Promise) {
            this.threeDSecureClientRefInstance = client
            this.promiseRefInstance = promise
        }

        fun clearStaticReferences() {
            threeDSecureClientRefInstance = null
            promiseRefInstance = null
            payPalClientRefInstance = null
            venmoClientRefInstance = null
        }
    }

    private fun isBusy(promise: Promise): Boolean {
        if (promiseRefInstance != null) {
            promise.reject(
                EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
                ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.value,
                SharedDataConverter.createError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, "Another payment flow is in progress")
            )
            return true
        }
        promiseRefInstance = promise
        return false
    }

    @ReactMethod
    fun request3DSecurePaymentCheck(data: ReadableMap, localPromise: Promise) {
        val activity = fragmentActivity ?: return rejectNoActivity(localPromise)
        if (isBusy(localPromise)) return

        try {
            val clientToken = data.getString("clientToken") ?: ""
            val client = ThreeDSecureClient(reactContextRef, clientToken)
            setupThreeDSecureInstance(client, localPromise)

            val request = CardDataConverter.create3DSecureRequest(data)
            client.createPaymentAuthRequest(reactContextRef, request) { response ->
                when (response) {
                    is ThreeDSecurePaymentAuthRequest.ReadyToLaunch -> threeDSecureLauncher.launch(response)
                    is ThreeDSecurePaymentAuthRequest.LaunchNotRequired -> {
                        moduleHandlers.onThreeDSecureSuccessHandler(response.nonce, localPromise)
                        clearStaticReferences()
                    }
                    is ThreeDSecurePaymentAuthRequest.Failure -> {
                        moduleHandlers.onThreeDSecureFailure(response.error, localPromise)
                        clearStaticReferences()
                    }
                }
            }
        } catch (ex: Exception) {
            handleKotlinException(localPromise, ex)
        }
    }

    @ReactMethod
    fun requestBillingAgreement(data: ReadableMap, localPromise: Promise) {
        val activity = fragmentActivity ?: return rejectNoActivity(localPromise)
        if (isBusy(localPromise)) return

        try {
            val client = PayPalClient(
                activity,
                data.getString("clientToken") ?: "",
                Uri.parse(data.getString("merchantAppLink") ?: ""),
                data.getString("fallbackUrlScheme")
            )
            payPalClientRefInstance = client
            val vaultRequest = PayPalDataConverter.createVaultRequest(data)
            
            client.createPaymentAuthRequest(reactContextRef, vaultRequest) { authRequest ->
                when (authRequest) {
                    is PayPalPaymentAuthRequest.ReadyToLaunch -> {
                        val pending = payPalLauncher.launch(activity, authRequest)
                        if (pending is PayPalPendingRequest.Started) {
                            PendingRequestStore.instance.putPayPalPendingRequest(reactContextRef, pending)
                        } else if (pending is PayPalPendingRequest.Failure) {
                            moduleHandlers.onFailure(pending.error, localPromise)
                            clearStaticReferences()
                        }
                    }
                    is PayPalPaymentAuthRequest.Failure -> {
                        moduleHandlers.onFailure(authRequest.error, localPromise)
                        clearStaticReferences()
                    }
                }
            }
        } catch (ex: Exception) {
            handleKotlinException(localPromise, ex)
        }
    }

    @ReactMethod
    fun requestOneTimePayment(data: ReadableMap, localPromise: Promise) {
        val activity = fragmentActivity ?: return rejectNoActivity(localPromise)
        if (isBusy(localPromise)) return

        try {
            val client = PayPalClient(
                activity,
                data.getString("clientToken") ?: "",
                Uri.parse(data.getString("merchantAppLink") ?: ""),
                data.getString("fallbackUrlScheme")
            )
            payPalClientRefInstance = client
            val checkoutRequest = PayPalDataConverter.createCheckoutRequest(data)

            client.createPaymentAuthRequest(reactContextRef, checkoutRequest) { authRequest ->
                when (authRequest) {
                    is PayPalPaymentAuthRequest.ReadyToLaunch -> {
                        val pending = payPalLauncher.launch(activity, authRequest)
                        if (pending is PayPalPendingRequest.Started) {
                            PendingRequestStore.instance.putPayPalPendingRequest(reactContextRef, pending)
                        } else if (pending is PayPalPendingRequest.Failure) {
                            moduleHandlers.onFailure(pending.error, localPromise)
                            clearStaticReferences()
                        }
                    }
                    is PayPalPaymentAuthRequest.Failure -> {
                        moduleHandlers.onFailure(authRequest.error, localPromise)
                        clearStaticReferences()
                    }
                }
            }
        } catch (ex: Exception) {
            handleKotlinException(localPromise, ex)
        }
    }

    @ReactMethod
    fun tokenizeCardData(data: ReadableMap, localPromise: Promise) {
        if (isBusy(localPromise)) return
        try {
            val clientToken = data.getString("clientToken") ?: ""
            if (fragmentActivity != null && clientToken.isNotEmpty()) {
                val cardClient = CardClient(reactContextRef, clientToken)
                val cardRequest = CardDataConverter.createTokenizeCardRequest(data)
                cardClient.tokenize(cardRequest) { cardResult ->
                    when (cardResult) {
                        is CardResult.Success -> moduleHandlers.onCardTokenizeSuccessHandler(cardResult.nonce, localPromise)
                        is CardResult.Failure -> moduleHandlers.onCardTokenizeFailure(cardResult.error, localPromise)
                    }
                    clearStaticReferences()
                }
            } else {
                throw Exception("Invalid activity or client token")
            }
        } catch (ex: Exception) {
            handleKotlinException(localPromise, ex)
        }
    }

    @ReactMethod
    fun requestVenmoNonce(data: ReadableMap, localPromise: Promise) {
        val activity = fragmentActivity ?: return rejectNoActivity(localPromise)
        if (isBusy(localPromise)) return

        try {
            val client = VenmoClient(
                activity,
                data.getString("clientToken") ?: "",
                Uri.parse(data.getString("merchantAppLink") ?: ""),
                data.getString("fallbackUrlScheme")
            )
            venmoClientRefInstance = client
            val request = VenmoDataConverter.createRequest(data)

            client.createPaymentAuthRequest(reactContextRef, request) { authRequest ->
                when (authRequest) {
                    is VenmoPaymentAuthRequest.ReadyToLaunch -> {
                        val pending = venmoLauncher.launch(activity, authRequest)
                        if (pending is VenmoPendingRequest.Started) {
                            PendingRequestStore.instance.putVenmoPendingRequest(reactContextRef, pending)
                        } else if (pending is VenmoPendingRequest.Failure) {
                            moduleHandlers.onFailure(pending.error, localPromise)
                            clearStaticReferences()
                        }
                    }
                    is VenmoPaymentAuthRequest.Failure -> {
                        moduleHandlers.onFailure(authRequest.error, localPromise)
                        clearStaticReferences()
                    }
                }
            }
        } catch (ex: Exception) {
            handleKotlinException(localPromise, ex)
        }
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
        } catch (ex: Exception) {
            handleKotlinException(localPromise, ex)
        }
    }


    private fun handleReturnToApp(intent: Intent) {
        val activity = fragmentActivity ?: return
        val promise = promiseRefInstance ?: return

        // PayPal handling
        PendingRequestStore.instance.getPayPalPendingRequest(activity)?.let { pending ->
            val result = payPalLauncher.handleReturnToApp(pending, intent)
            when (result) {
                is PayPalPaymentAuthResult.Success -> {
                    payPalClientRefInstance?.tokenize(result) { tokenResult ->
                        when (tokenResult) {
                            is PayPalResult.Success -> moduleHandlers.onPayPalSuccessHandler(tokenResult.nonce, promise)
                            is PayPalResult.Failure -> moduleHandlers.onFailure(tokenResult.error, promise)
                            is PayPalResult.Cancel -> moduleHandlers.onCancel(Exception("Cancel"), promise)
                        }
                        clearStaticReferences()
                    }
                }
                is PayPalPaymentAuthResult.Failure -> {
                    moduleHandlers.onFailure(result.error, promise)
                    clearStaticReferences()
                }
                PayPalPaymentAuthResult.NoResult -> {
                    moduleHandlers.onCancel(Exception("No result"), promise)
                    clearStaticReferences()
                }
            }
            PendingRequestStore.instance.clearPayPalPendingRequest(activity)
        }

        // Venmo handling
        PendingRequestStore.instance.getVenmoPendingRequest(activity)?.let { pending ->
            val result = venmoLauncher.handleReturnToApp(pending, intent)
            when (result) {
                is VenmoPaymentAuthResult.Success -> {
                    venmoClientRefInstance?.tokenize(result) { tokenResult ->
                        when (tokenResult) {
                            is VenmoResult.Success -> moduleHandlers.onVenmoSuccessHandler(tokenResult.nonce, promise)
                            is VenmoResult.Failure -> moduleHandlers.onFailure(tokenResult.error, promise)
                            is VenmoResult.Cancel -> moduleHandlers.onCancel(Exception("Cancel"), promise)
                        }
                        clearStaticReferences()
                    }
                }
                is VenmoPaymentAuthResult.Failure -> {
                    moduleHandlers.onFailure(result.error, promise)
                    clearStaticReferences()
                }
                VenmoPaymentAuthResult.NoResult -> {
                    moduleHandlers.onCancel(Exception("No result"), promise)
                    clearStaticReferences()
                }
            }
            PendingRequestStore.instance.clearVenmoPendingRequest(activity)
        }
    }

    private fun rejectNoActivity(promise: Promise) {
        promise.reject(
            EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
            ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.value,
            SharedDataConverter.createError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, "Activity not initialized")
        )
    }

    private fun handleKotlinException(promise: Promise, ex: Exception) {
        promise.reject(
            EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
            ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.value,
            SharedDataConverter.createError(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, ex.message)
        )
        clearStaticReferences()
    }

    override fun onHostResume() {
        fragmentActivity?.intent?.let { if (it.data != null) handleReturnToApp(it) }
    }

    override fun onNewIntent(intent: Intent) {
        fragmentActivity?.let {
            it.intent = intent
            handleReturnToApp(intent)
            it.intent = Intent() 
        }
    }

    override fun getName() = NAME
    override fun onHostPause() {}
    override fun onHostDestroy() { clearStaticReferences() }
    override fun onActivityResult(p0: Activity, p1: Int, p2: Int, p3: Intent?) {}
}