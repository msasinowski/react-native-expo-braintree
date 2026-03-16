package com.expobraintree

import com.braintreepayments.api.card.CardNonce
import com.braintreepayments.api.core.UserCanceledException
import com.braintreepayments.api.paypal.PayPalAccountNonce
import com.braintreepayments.api.threedsecure.ThreeDSecureNonce
import com.braintreepayments.api.venmo.VenmoAccountNonce
import com.braintreepayments.api.googlepay.GooglePayCardNonce

import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.Arguments


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
        SharedDataConverter.createError(
          EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, error.message
        ))
      return
    }

    mPromise.reject(EXCEPTION_TYPES.KOTLIN_EXCEPTION.value,
      ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.value,
      SharedDataConverter.createError(
        EXCEPTION_TYPES.KOTLIN_EXCEPTION.value, "PayPal Error"
      ))
  }

  fun onCancel(error: Exception, mPromise: Promise) {
      mPromise.reject(EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.value,
        ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.value,
        SharedDataConverter.createError(
          EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.value, error.message
        ))
  }

  fun onPayPalSuccessHandler(payPalAccountNonce: PayPalAccountNonce, mPromise: Promise) {
    val result: WritableMap = PayPalDataConverter.convertPaypalDataAccountNonce(payPalAccountNonce)
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
    val result: WritableMap = CardDataConverter.createTokenizeCardDataNonce(cardNonce)
    mPromise.resolve(result)
  }

  fun onThreeDSecureFailure(error: Exception, mPromise: Promise) {
      // Get the most descriptive message possible
      val errorMessage = error.message ?: error.localizedMessage ?: "Unknown 3D Secure error"

      // We use the actual error message as the second argument (message) 
      // so it's visible in the 'details' field in JS.
      mPromise.reject(
        EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value,
        errorMessage, // This replaces the generic CARD_TOKENIZATION_ERROR
        SharedDataConverter.createError(
          EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value, 
          errorMessage
        )
      )
    }
    
  fun onThreeDSecureSuccessHandler(threeDSecureNonce: ThreeDSecureNonce, mPromise: Promise) {
     val info = threeDSecureNonce.threeDSecureInfo

     // Validation: Reject only if verification was attempted but failed
     // Note: we removed 'is' prefix to fix the 'Unresolved reference' error
     if (info.wasVerified && !info.liabilityShifted) {
       mPromise.reject(
         EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value,
         THREE_D_SECURE_ERROR_TYPES.PAYMENT_3D_SECURE_FAILED.value,
         SharedDataConverter.createError(
           EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value, 
           "Liability shift failed"
         )
       )
       return
     }

     // Basic check for empty nonce
     if (threeDSecureNonce.string.isEmpty()){
       mPromise.reject(
         EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value,
         "Empty nonce received",
         SharedDataConverter.createError(
           EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value, 
           "Empty nonce"
         )
       )
       return
     }

     // If everything is fine, resolve with full data
     try {
       val result: WritableMap = CardDataConverter.createThreeDSecureDataNonce(threeDSecureNonce)
       mPromise.resolve(result)
     } catch (e: Exception) {
       mPromise.reject(EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value, e.message, e)
     }
   }

  fun onGooglePaySuccessHandler(nonce: GooglePayCardNonce, promise: Promise) {
      val result: WritableMap = Arguments.createMap()
      
      // Base nonce information
      result.putString("nonce", nonce.string)
      result.putString("type", "GooglePayCard")
      
      // Card details mapping
      val details: WritableMap = Arguments.createMap()
      details.putString("cardType", nonce.cardType)
      details.putString("lastFour", nonce.lastFour)
      details.putString("lastTwo", nonce.lastTwo)
      result.putMap("details", details)
      
      // Billing Address mapping (optional)
      // Only populates if billingAddressRequired was true and user provided it
      nonce.billingAddress?.let { address ->
          val billingMap: WritableMap = Arguments.createMap()
          billingMap.putString("recipientName", address.recipientName)
          billingMap.putString("streetAddress", address.streetAddress)
          billingMap.putString("locality", address.locality) // City
          billingMap.putString("countryCodeAlpha2", address.countryCodeAlpha2)
          result.putMap("billingAddress", billingMap)
      }
      
      promise.resolve(result)
  }
}
