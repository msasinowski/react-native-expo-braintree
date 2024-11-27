package com.expobraintree

import com.braintreepayments.api.card.CardNonce
import com.braintreepayments.api.core.UserCanceledException
import com.braintreepayments.api.paypal.PayPalAccountNonce
import com.braintreepayments.api.venmo.VenmoAccountNonce

import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.Promise


class ExpoBraintreeModuleHandlers {

  fun handleGetDeviceDataFromDataCollectorResult(result: String?, error: Exception?, mPromise: Promise) {
    if (error != null) {
      mPromise.reject(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
        ERROR_TYPES.DATA_COLLECTOR_ERROR.value,
        SharedDataConverter.createError(
          EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, error.message
        ))
      return
    }
    if (result != null) {
      mPromise.resolve(result)
    }
  }

  fun onFailure(error: Exception, mPromise: Promise) {
    if (error is UserCanceledException) {
      mPromise.reject(EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.value,
        ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.value,
        SharedDataConverter.createError(
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

  fun onCancel(error: Exception, mPromise: Promise) {
      mPromise.reject(EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.value,
        ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.value,
        SharedDataConverter.createError(
          EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.value, error.message
        ))
  }

  fun onPayPalSuccessHandler(payPalAccountNonce: PayPalAccountNonce, mPromise: Promise) {
    val result: WritableMap = PaypalDataConverter.convertPaypalDataAccountNonce(payPalAccountNonce)
    result.putMap("billingAddress", SharedDataConverter.convertAddressData(payPalAccountNonce.billingAddress))
    result.putMap("shippingAddress", SharedDataConverter.convertAddressData(payPalAccountNonce.shippingAddress))
    mPromise.resolve(result)
  }

  fun onVenmoSuccessHandler(nonce: VenmoAccountNonce, mPromise: Promise) {
    val result: WritableMap = VenmoDataConverter.convertVenmoDataAccountNonce(nonce)
    result.putMap("billingAddress", SharedDataConverter.convertAddressData(nonce.billingAddress))
    result.putMap("shippingAddress", SharedDataConverter.convertAddressData(nonce.shippingAddress))
    mPromise.resolve(result)
  }

  fun onCardTokenizeFailure(error: Exception, mPromise: Promise) {
    mPromise.reject(EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value,
      ERROR_TYPES.CARD_TOKENIZATION_ERROR.value,
      SharedDataConverter.createError(
        EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value, error.message
      ))
  }

  fun onCardTokenizeSuccessHandler(cardNonce: CardNonce, mPromise: Promise) {
    val result: WritableMap = PaypalDataConverter.createTokenizeCardDataNonce(cardNonce)
    mPromise.resolve(result)
  }
}
