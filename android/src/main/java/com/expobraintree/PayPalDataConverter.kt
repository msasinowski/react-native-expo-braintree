package com.expobraintree

import com.braintreepayments.api.paypal.PayPalAccountNonce
import com.braintreepayments.api.paypal.PayPalCheckoutRequest
import com.braintreepayments.api.paypal.PayPalPaymentIntent
import com.braintreepayments.api.paypal.PayPalPaymentUserAction
import com.braintreepayments.api.paypal.PayPalVaultRequest
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap


class PayPalDataConverter {

  companion object {
    fun convertPaypalDataAccountNonce(payPalAccountNonce: PayPalAccountNonce): WritableMap {
      val result: WritableMap = Arguments.createMap()
      result.putString("nonce", payPalAccountNonce.string)
      result.putString("payerId", payPalAccountNonce.payerId)
      result.putString("email", payPalAccountNonce.email)
      result.putString("firstName", payPalAccountNonce.firstName)
      result.putString("lastName", payPalAccountNonce.lastName)
      result.putString("phone", payPalAccountNonce.phone)

      payPalAccountNonce.shippingAddress?.let { addr ->
        val shipMap = Arguments.createMap()
        shipMap.putString("recipientName", addr.recipientName)
        shipMap.putString("streetAddress", addr.streetAddress)
        shipMap.putString("extendedAddress", addr.extendedAddress)
        shipMap.putString("locality", addr.locality) // City
        shipMap.putString("region", addr.region)     // State/Province
        shipMap.putString("postalCode", addr.postalCode)
        shipMap.putString("countryCodeAlpha2", addr.countryCodeAlpha2)
        result.putMap("shippingAddress", shipMap)
      }

      payPalAccountNonce.billingAddress?.let { addr ->
        val billMap = Arguments.createMap()
        billMap.putString("recipientName", addr.recipientName)
        billMap.putString("streetAddress", addr.streetAddress)
        billMap.putString("extendedAddress", addr.extendedAddress)
        billMap.putString("locality", addr.locality)
        billMap.putString("region", addr.region)
        billMap.putString("postalCode", addr.postalCode)
        billMap.putString("countryCodeAlpha2", addr.countryCodeAlpha2)
        result.putMap("billingAddress", billMap)
      }

      return result
    }

    // Helper to safely get Boolean even if it's passed as a String from JS
    private fun getSafeBoolean(options: ReadableMap, key: String): Boolean {
        if (!options.hasKey(key)) return false
        return try {
            options.getBoolean(key)
        } catch (e: Exception) {
            options.getString(key)?.lowercase() == "true"
        }
    }

    fun createVaultRequest(options: ReadableMap): PayPalVaultRequest {
      val userConsent = getSafeBoolean(options, "hasUserLocationConsent")
      val request = PayPalVaultRequest(userConsent)

      if (options.hasKey("billingAgreementDescription")) {
        request.billingAgreementDescription = options.getString("billingAgreementDescription")
      }
      
      // Standardize Boolean handling for Shipping
      request.isShippingAddressRequired = getSafeBoolean(options, "isShippingAddressRequired")
      request.isShippingAddressEditable = getSafeBoolean(options, "isShippingAddressEditable")
      
      if (options.hasKey("localeCode")) request.localeCode = options.getString("localeCode")
      if (options.hasKey("displayName")) request.displayName = options.getString("displayName")
      
      return request
    }

    fun createCheckoutRequest(options: ReadableMap): PayPalCheckoutRequest {
      val userConsent = getSafeBoolean(options, "hasUserLocationConsent")
      val amount = options.getString("amount") ?: ""
      val request = PayPalCheckoutRequest(amount, userConsent)
      
      // Fixed Boolean checks
      request.isShippingAddressRequired = getSafeBoolean(options, "isShippingAddressRequired")
      request.isShippingAddressEditable = getSafeBoolean(options, "isShippingAddressEditable")

      if (options.hasKey("intent")) {
        when (options.getString("intent")) {
          "sale" -> request.intent = PayPalPaymentIntent.SALE
          "order" -> request.intent = PayPalPaymentIntent.ORDER
          else -> request.intent = PayPalPaymentIntent.AUTHORIZE
        }
      }
      
      return request
    }
  }
}