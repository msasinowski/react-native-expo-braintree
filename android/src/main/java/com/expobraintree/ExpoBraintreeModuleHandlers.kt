package com.expobraintree

import com.braintreepayments.api.PayPalAccountNonce
import com.braintreepayments.api.UserCanceledException
import com.braintreepayments.api.CardNonce
import com.braintreepayments.api.VenmoAccountNonce

import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.Promise


class ExpoBraintreeModuleHandlers {

  public fun handleGetDeviceDataFromDataCollectorResult(result: String?, error: Exception?, mPromise: Promise) {
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

  public fun onFailure(error: Exception, mPromise: Promise) {
    if (error is UserCanceledException) {
      mPromise.reject(EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.value,
        ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.value,
        SharedDataConverter.createError(
          EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.value, error.message
        ))
      return
    }
    mPromise.reject(error.message)
  }

  public fun onPayPalSuccessHandler(payPalAccountNonce: PayPalAccountNonce, mPromise: Promise) {
    val result: WritableMap = PaypalDataConverter.convertPaypalDataAccountNonce(payPalAccountNonce)
    result.putMap("billingAddress", SharedDataConverter.convertAddressData(payPalAccountNonce.billingAddress))
    result.putMap("shippingAddress", SharedDataConverter.convertAddressData(payPalAccountNonce.shippingAddress))
    mPromise.resolve(result)
  }

  public fun onVenmoSuccessHandler(nonce: VenmoAccountNonce, mPromise: Promise) {
    val result: WritableMap = VenmoDataConverter.convertVenmoDataAccountNonce(nonce)
    result.putMap("billingAddress", SharedDataConverter.convertAddressData(nonce.billingAddress))
    result.putMap("shippingAddress", SharedDataConverter.convertAddressData(nonce.shippingAddress))
    mPromise.resolve(result)
  }

  public fun onCardTokenizeFailure(error: Exception, mPromise: Promise) {
    mPromise.reject(EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value,
      ERROR_TYPES.CARD_TOKENIZATION_ERROR.value,
      SharedDataConverter.createError(
        EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value, error.message
      ))
  }

  public fun onCardTokenizeSuccessHandler(cardNonce: CardNonce, mPromise: Promise) {
    val result: WritableMap = PaypalDataConverter.createTokenizeCardDataNonce(cardNonce)
    mPromise.resolve(result)
  }
}
