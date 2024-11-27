package com.expobraintree

import com.braintreepayments.api.PayPalAccountNonce
import com.braintreepayments.api.UserCanceledException
import com.braintreepayments.api.CardNonce

import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.Promise


class PaypalRebornModuleHandlers {

  public fun handleGetDeviceDataFromDataCollectorResult(result: String?, error: Exception?, mPromise: Promise) {
    if (error != null) {
      mPromise.reject(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
        ERROR_TYPES.DATA_COLLECTOR_ERROR.value,
        PaypalDataConverter.createError(
          EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, error.message
        ))
      return
    }
    if (result != null) {
      mPromise.resolve(result)
    }
  }

  public fun onPayPalFailure(error: Exception, mPromise: Promise) {
    if (error is UserCanceledException) {
      mPromise.reject(EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.value,
        ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.value,
        PaypalDataConverter.createError(
          EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.value, error.message
        ))
      return
    }
    error.message?.let {
      mPromise.reject(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
        ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value,
        PaypalDataConverter.createError(
          EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, error.message
        ))
    } ?: {
      mPromise.reject(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
        ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value,
        PaypalDataConverter.createError(
          EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, "PayPal Error"
        ))
    }
  }

  public fun onPayPalSuccessHandler(payPalAccountNonce: PayPalAccountNonce, mPromise: Promise) {
    val result: WritableMap = PaypalDataConverter.convertPaypalDataAccountNonce(payPalAccountNonce)
    result.putMap("billingAddress", PaypalDataConverter.convertAddressData(payPalAccountNonce.billingAddress))
    result.putMap("shippingAddress", PaypalDataConverter.convertAddressData(payPalAccountNonce.shippingAddress))
    mPromise.resolve(result)
  }


  public fun onCardTokenizeFailure(error: Exception, mPromise: Promise) {
    mPromise.reject(EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value,
      ERROR_TYPES.CARD_TOKENIZATION_ERROR.value,
      PaypalDataConverter.createError(
        EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value, error.message
      ))
  }

  public fun onCardTokenizeSuccessHandler(cardNonce: CardNonce, mPromise: Promise) {
    val result: WritableMap = PaypalDataConverter.createTokenizeCardDataNonce(cardNonce)
    mPromise.resolve(result)
  }
}
