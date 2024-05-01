package com.paypalreborn

import com.braintreepayments.api.PayPalAccountNonce
import com.braintreepayments.api.UserCanceledException

import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.Promise


class PaypalRebornModuleHandlers {

  public fun handleGetDeviceDataFromDataCollectorResult(result: String?, error: Exception?, mPromise: Promise) {
    if (error != null) {
      mPromise.reject("-1", ERROR_TYPES.DATA_COLLECTOR_ERROR.value,
        PaypalDataConverter.createError(
          EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,error.message
        ))
      return
    }
    if (result != null) {
      mPromise.resolve(result)
    }
  }

  public fun onPayPalFailure(error: Exception, mPromise: Promise) {
    if (error is UserCanceledException) {
      mPromise.reject("-1", ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.value,
        PaypalDataConverter.createError(
          EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.value,error.message
        ))
      return
    }
    mPromise.reject(error.message)
  }

  public fun onPayPalSuccessHandler(payPalAccountNonce: PayPalAccountNonce, mPromise: Promise) {
    val result: WritableMap = PaypalDataConverter.convertPaypalDataAccountNonce(payPalAccountNonce)
    result.putMap("billingAddress", PaypalDataConverter.convertAddressData(payPalAccountNonce.billingAddress))
    result.putMap("shippingAddress", PaypalDataConverter.convertAddressData(payPalAccountNonce.shippingAddress))
    mPromise.resolve(result)
  }
}
