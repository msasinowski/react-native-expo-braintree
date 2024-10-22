package com.expobraintree

//import com.braintreepayments.api.PayPalAccountNonce
//import com.braintreepayments.api.UserCanceledException
//import com.braintreepayments.api.CardNonce
import com.braintreepayments.api.threedsecure.ThreeDSecureResult
import com.braintreepayments.api.card.CardNonce;
import com.braintreepayments.api.threedsecure.ThreeDSecureNonce

import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.Promise


class PaypalRebornModuleHandlers {
//
//  public fun handleGetDeviceDataFromDataCollectorResult(result: String?, error: Exception?, mPromise: Promise) {
//    if (error != null) {
//      mPromise.reject(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
//        ERROR_TYPES.DATA_COLLECTOR_ERROR.value,
//        PaypalDataConverter.createError(
//          EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, error.message
//        ))
//      return
//    }
//    if (result != null) {
//      mPromise.resolve(result)
//    }
//  }
//
//  public fun onPayPalFailure(error: Exception, mPromise: Promise) {
//    if (error is UserCanceledException) {
//      mPromise.reject(EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.value,
//        ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.value,
//        PaypalDataConverter.createError(
//          EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.value, error.message
//        ))
//      return
//    }
//    mPromise.reject(error.message)
//  }
//
//  public fun onPayPalSuccessHandler(payPalAccountNonce: PayPalAccountNonce, mPromise: Promise) {
//    val result: WritableMap = PaypalDataConverter.convertPaypalDataAccountNonce(payPalAccountNonce)
//    result.putMap("billingAddress", PaypalDataConverter.convertAddressData(payPalAccountNonce.billingAddress))
//    result.putMap("shippingAddress", PaypalDataConverter.convertAddressData(payPalAccountNonce.shippingAddress))
//    mPromise.resolve(result)
//  }
//
//
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
//
//
  public fun onThreeDSecureFailure(error: Exception, mPromise: Promise) {
    mPromise.reject(EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value,
      ERROR_TYPES.CARD_TOKENIZATION_ERROR.value,
      PaypalDataConverter.createError(
        EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value, error.localizedMessage
      ))
  }
//
  public fun onThreeDSecureSuccessHandler(threeDSecureNonce: ThreeDSecureNonce, mPromise: Promise) {
    if (threeDSecureNonce.threeDSecureInfo.liabilityShiftPossible == true &&
      threeDSecureNonce.threeDSecureInfo.wasVerified == true) {
      mPromise.reject(EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value,
        ERROR_TYPES.D_SECURE_NOT_ABLE_TO_SHIFT_LIABILITY.value,
        PaypalDataConverter.createError(
          EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value, EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value
        ))
      return
    }

    if (threeDSecureNonce.threeDSecureInfo.liabilityShifted == true &&
      threeDSecureNonce.threeDSecureInfo.wasVerified == true) {
      mPromise.reject(EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value,
        ERROR_TYPES.D_SECURE_LIABILITY_NOT_SHIFTED.value,
        PaypalDataConverter.createError(
          EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value, EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value
        ))
      return
    }

    if (threeDSecureNonce.string == "") {
      mPromise.reject(EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value,
        ERROR_TYPES.PAYMENT_3D_SECURE_FAILED.value,
        PaypalDataConverter.createError(
          EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value, EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value
        ))
      return
    }

    val result: WritableMap = PaypalDataConverter.createThreeDSecureCardDataNonce(threeDSecureNonce)
    mPromise.resolve(result)
    return
  }
}
