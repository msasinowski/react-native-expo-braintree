package com.expobraintree

import android.content.Context
import com.braintreepayments.api.paypal.PayPalPendingRequest

class ExpoBraintreePendingRequestStore {
  companion object {
    const val PREFERENCES_KEY = "PENDING_REQUEST_SHARED_PREFERENCES"
    const val PAYPAL_PENDING_REQUEST_KEY = "PAYPAL_PENDING_REQUEST"
    const val LOCAL_PAYMENT_PENDING_REQUEST_KEY = "LOCAL_PAYMENT_PENDING_REQUEST"
    const val SEPA_DIRECT_DEBIT_PENDING_REQUEST_KEY = "SEPA_DIRECT_DEBIT_PENDING_REQUEST"
    const val VENMO_PENDING_REQUEST_KEY = "VENMO_PENDING_REQUEST"
    val instance = ExpoBraintreePendingRequestStore()

    fun put(key: String?, value: String?, context: Context) {
      val applicationContext = context.applicationContext
      val sharedPreferences =
        applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
      sharedPreferences.edit().putString(key, value).apply()
    }

    operator fun get(key: String?, context: Context): String? {
      val applicationContext = context.applicationContext
      val sharedPreferences =
        applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
      return sharedPreferences.getString(key, null)
    }

    fun remove(key: String?, context: Context) {
      val applicationContext = context.applicationContext
      val sharedPreferences =
        applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
      sharedPreferences.edit().remove(key).apply()
    }
  }

  fun putPayPalPendingRequest(
    context: Context,
    pendingRequest: PayPalPendingRequest.Started
  ) {
    put(PAYPAL_PENDING_REQUEST_KEY, pendingRequest.pendingRequestString, context)
  }

  fun getPayPalPendingRequest(context: Context): PayPalPendingRequest.Started? {
    val requestString = Companion[PAYPAL_PENDING_REQUEST_KEY, context]
    return requestString?.let { PayPalPendingRequest.Started(it) }
  }

  fun clearPayPalPendingRequest(context: Context) {
    remove(PAYPAL_PENDING_REQUEST_KEY, context)
  }

//  fun putLocalPaymentPendingRequest(
//    context: Context,
//    pendingRequest: LocalPaymentPendingRequest.Started
//  ) {
//    put(LOCAL_PAYMENT_PENDING_REQUEST_KEY, pendingRequest.getPendingRequestString(), context)
//  }
//
//  fun getLocalPaymentPendingRequest(context: Context): LocalPaymentPendingRequest.Started? {
//    val requestString = Companion[LOCAL_PAYMENT_PENDING_REQUEST_KEY, context]
//    return requestString?.let { Started(it) }
//  }
//
//  fun clearLocalPaymentPendingRequest(context: Context) {
//    remove(LOCAL_PAYMENT_PENDING_REQUEST_KEY, context)
//  }
//
//  fun putSEPADirectDebitPendingRequest(
//    context: Context,
//    pendingRequest: SEPADirectDebitPendingRequest.Started
//  ) {
//    put(SEPA_DIRECT_DEBIT_PENDING_REQUEST_KEY, pendingRequest.getPendingRequestString(), context)
//  }
//
//  fun getSEPADirectDebitPendingRequest(context: Context): SEPADirectDebitPendingRequest.Started? {
//    val requestString = Companion[SEPA_DIRECT_DEBIT_PENDING_REQUEST_KEY, context]
//    return requestString?.let { Started(it) }
//  }
//
//  fun clearSEPADirectDebitPendingRequest(context: Context) {
//    remove(SEPA_DIRECT_DEBIT_PENDING_REQUEST_KEY, context)
//  }
//
//  fun putVenmoPendingRequest(
//    context: Context,
//    pendingRequest: VenmoPendingRequest.Started
//  ) {
//    put(VENMO_PENDING_REQUEST_KEY, pendingRequest.getPendingRequestString(), context)
//  }
//
//  fun getVenmoPendingRequest(context: Context): VenmoPendingRequest.Started? {
//    val requestString = Companion[VENMO_PENDING_REQUEST_KEY, context]
//    return requestString?.let { Started(it) }
//  }
//
//  fun clearVenmoPendingRequest(context: Context) {
//    remove(VENMO_PENDING_REQUEST_KEY, context)
//  }


}
