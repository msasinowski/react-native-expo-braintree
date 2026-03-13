package com.expobraintree

import android.content.Context
import android.content.SharedPreferences
import com.braintreepayments.api.paypal.PayPalPendingRequest
import com.braintreepayments.api.venmo.VenmoPendingRequest

class PendingRequestStore private constructor() {

    companion object {
        private const val PREFERENCES_KEY = "PENDING_REQUEST_SHARED_PREFERENCES"
        private const val PAYPAL_PENDING_REQUEST_KEY = "PAYPAL_PENDING_REQUEST"
        private const val VENMO_PENDING_REQUEST_KEY = "VENMO_PENDING_REQUEST"
        
        private const val LOCAL_PAYMENT_PENDING_REQUEST_KEY = "LOCAL_PAYMENT_PENDING_REQUEST"
        private const val SEPA_DIRECT_DEBIT_PENDING_REQUEST_KEY = "SEPA_DIRECT_DEBIT_PENDING_REQUEST"

        @JvmStatic
        val instance: PendingRequestStore by lazy { PendingRequestStore() }
    }

    // --- Venmo ---

    fun putVenmoPendingRequest(context: Context, pendingRequest: VenmoPendingRequest.Started) {
        put(VENMO_PENDING_REQUEST_KEY, pendingRequest.pendingRequestString, context)
    }

    fun getVenmoPendingRequest(context: Context): VenmoPendingRequest.Started? {
        return get(VENMO_PENDING_REQUEST_KEY, context)?.let {
            VenmoPendingRequest.Started(it)
        }
    }

    fun clearVenmoPendingRequest(context: Context) {
        remove(VENMO_PENDING_REQUEST_KEY, context)
    }

    // --- PayPal ---

    fun putPayPalPendingRequest(context: Context, pendingRequest: PayPalPendingRequest.Started) {
        put(PAYPAL_PENDING_REQUEST_KEY, pendingRequest.pendingRequestString, context)
    }

    fun getPayPalPendingRequest(context: Context): PayPalPendingRequest.Started? {
        return get(PAYPAL_PENDING_REQUEST_KEY, context)?.let {
            PayPalPendingRequest.Started(it)
        }
    }

    fun clearPayPalPendingRequest(context: Context) {
        remove(PAYPAL_PENDING_REQUEST_KEY, context)
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
    }

    private fun put(key: String, value: String, context: Context) {
        getPrefs(context).edit().putString(key, value).apply()
    }

    private fun get(key: String, context: Context): String? {
        return getPrefs(context).getString(key, null)
    }

    private fun remove(key: String, context: Context) {
        getPrefs(context).edit().remove(key).apply()
    }
}