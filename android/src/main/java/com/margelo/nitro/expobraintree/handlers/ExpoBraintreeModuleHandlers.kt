package com.margelo.nitro.expobraintree.handlers

import com.braintreepayments.api.card.CardNonce
import com.braintreepayments.api.paypal.PayPalAccountNonce
import com.braintreepayments.api.threedsecure.ThreeDSecureNonce
import com.braintreepayments.api.venmo.VenmoAccountNonce
import com.braintreepayments.api.googlepay.GooglePayCardNonce
import com.margelo.nitro.expobraintree.converters.CardDataConverter
import com.margelo.nitro.expobraintree.converters.PayPalDataConverter
import com.margelo.nitro.expobraintree.converters.SharedDataConverter
import com.margelo.nitro.expobraintree.converters.VenmoDataConverter
import com.margelo.nitro.expobraintree.enums.EXCEPTION_TYPES
import com.margelo.nitro.expobraintree.enums.ERROR_TYPES
import com.margelo.nitro.expobraintree.enums.THREE_D_SECURE_ERROR_TYPES
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.Arguments
import com.margelo.nitro.core.Promise

class ExpoBraintreeModuleHandlers {

  fun onCancel(promise: Promise<Map<String, String>>, nativeError: String? = null) {
    promise.resolve(errorMap(EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.value, ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.value, nativeError ?: EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.value))
  }

  fun onPayPalSuccessHandler(payPalAccountNonce: PayPalAccountNonce, promise: Promise<Map<String, String>>) {
    val result: WritableMap = PayPalDataConverter.convertPaypalDataAccountNonce(payPalAccountNonce)
    result.putMap("billingAddress", SharedDataConverter.convertAddressData(payPalAccountNonce.billingAddress))
    result.putMap("shippingAddress", SharedDataConverter.convertAddressData(payPalAccountNonce.shippingAddress))
    promise.resolve(writableMapToStringMap(result))
  }

  fun onVenmoSuccessHandler(nonce: VenmoAccountNonce, promise: Promise<Map<String, String>>) {
    val result: WritableMap = VenmoDataConverter.convertVenmoDataAccountNonce(nonce)
    result.putMap("billingAddress", SharedDataConverter.convertAddressData(nonce.billingAddress))
    result.putMap("shippingAddress", SharedDataConverter.convertAddressData(nonce.shippingAddress))
    promise.resolve(writableMapToStringMap(result))
  }

  fun onCardTokenizeFailure(error: Exception, promise: Promise<Map<String, String>>) {
    promise.resolve(errorMap(EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value, ERROR_TYPES.CARD_TOKENIZATION_ERROR.value, error.message))
  }

  fun onCardTokenizeSuccessHandler(cardNonce: CardNonce, promise: Promise<Map<String, String>>) {
    val result: WritableMap = CardDataConverter.createTokenizeCardDataNonce(cardNonce)
    promise.resolve(writableMapToStringMap(result))
  }

  fun onThreeDSecureFailure(error: Exception, promise: Promise<Map<String, String>>) {
    promise.resolve(errorMap(EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value, "3DS tokenize failed", error.message))
  }

  fun onThreeDSecureSuccessHandler(threeDSecureNonce: ThreeDSecureNonce, promise: Promise<Map<String, String>>) {
    val info = threeDSecureNonce.threeDSecureInfo
    if (info.wasVerified && !info.liabilityShifted) {
      promise.resolve(errorMap(EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value, THREE_D_SECURE_ERROR_TYPES.PAYMENT_3D_SECURE_FAILED.value))
      return
    }
    if (threeDSecureNonce.string.isEmpty()) {
      promise.resolve(errorMap(EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value, "Empty nonce received"))
      return
    }
    try {
      val result: WritableMap = CardDataConverter.createThreeDSecureDataNonce(threeDSecureNonce)
      promise.resolve(writableMapToStringMap(result))
    } catch (e: Exception) {
      promise.resolve(errorMap(EXCEPTION_TYPES.TOKENIZE_EXCEPTION.value, "3DS result mapping failed", e.message))
    }
  }

  fun onGooglePaySuccessHandler(nonce: GooglePayCardNonce, promise: Promise<Map<String, String>>) {
    val result: WritableMap = Arguments.createMap()
    result.putString("nonce", nonce.string)
    result.putString("type", "GooglePayCard")
    val details: WritableMap = Arguments.createMap()
    details.putString("cardType", nonce.cardType)
    details.putString("lastFour", nonce.lastFour)
    details.putString("lastTwo", nonce.lastTwo)
    result.putMap("details", details)
    nonce.billingAddress?.let { address ->
      val billingMap: WritableMap = Arguments.createMap()
      billingMap.putString("recipientName", address.recipientName)
      billingMap.putString("streetAddress", address.streetAddress)
      billingMap.putString("locality", address.locality)
      billingMap.putString("countryCodeAlpha2", address.countryCodeAlpha2)
      result.putMap("billingAddress", billingMap)
    }
    promise.resolve(writableMapToStringMap(result))
  }

  fun errorMap(code: String, message: String?, nativeError: String? = null): Map<String, String> {
    val map = mutableMapOf(
      "error" to "true",
      "code" to code,
      "message" to (message ?: code),
      "domain" to (message ?: code),
    )
    if (nativeError != null) {
      map["nativeError"] = nativeError
    }
    return map
  }

  fun resolveError(code: String, message: String?, nativeError: String? = null, promise: Promise<Map<String, String>>) {
    promise.resolve(errorMap(code, message, nativeError))
  }

  private fun writableMapToStringMap(writable: WritableMap): Map<String, String> {
    val result = mutableMapOf<String, String>()
    val iterator = (writable as com.facebook.react.bridge.ReadableNativeMap).keySetIterator()
    while (iterator.hasNextKey()) {
      val key = iterator.nextKey()
      when (writable.getType(key)) {
        com.facebook.react.bridge.ReadableType.String -> result[key] = writable.getString(key) ?: ""
        com.facebook.react.bridge.ReadableType.Boolean -> result[key] = writable.getBoolean(key).toString()
        com.facebook.react.bridge.ReadableType.Number -> result[key] = writable.getDouble(key).toString()
        com.facebook.react.bridge.ReadableType.Map -> {
          writable.getMap(key)?.let { result[key] = readableMapToJsonString(it) }
        }
        com.facebook.react.bridge.ReadableType.Array -> result[key] = writable.getArray(key).toString()
        else -> {}
      }
    }
    return result
  }

  private fun readableMapToJsonString(map: com.facebook.react.bridge.ReadableMap): String {
    val json = org.json.JSONObject()
    val iter = (map as com.facebook.react.bridge.ReadableNativeMap).keySetIterator()
    while (iter.hasNextKey()) {
      val key = iter.nextKey()
      when (map.getType(key)) {
        com.facebook.react.bridge.ReadableType.String -> json.put(key, map.getString(key) ?: "")
        com.facebook.react.bridge.ReadableType.Boolean -> json.put(key, map.getBoolean(key))
        com.facebook.react.bridge.ReadableType.Number -> json.put(key, map.getDouble(key))
        com.facebook.react.bridge.ReadableType.Map -> {
          map.getMap(key)?.let { json.put(key, readableMapToJsonString(it)) }
        }
        com.facebook.react.bridge.ReadableType.Array -> json.put(key, map.getArray(key).toString())
        else -> {}
      }
    }
    return json.toString()
  }
}
