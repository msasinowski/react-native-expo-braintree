package com.expobraintree

import com.braintreepayments.api.card.Card
import com.braintreepayments.api.card.CardNonce
import com.braintreepayments.api.paypal.PayPalAccountNonce
import com.braintreepayments.api.paypal.PayPalCheckoutRequest
import com.braintreepayments.api.paypal.PayPalPaymentIntent
import com.braintreepayments.api.paypal.PayPalPaymentUserAction
import com.braintreepayments.api.paypal.PayPalVaultRequest
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap


class PaypalDataConverter {

  companion object {
    fun convertPaypalDataAccountNonce(payPalAccountNonce: PayPalAccountNonce): WritableMap {
      val result: WritableMap = Arguments.createMap()
      result.putString("nonce", payPalAccountNonce.string)
      result.putString("payerId", payPalAccountNonce.payerId)
      result.putString("email", payPalAccountNonce.email)
      result.putString("firstName", payPalAccountNonce.firstName)
      result.putString("lastName", payPalAccountNonce.lastName)
      result.putString("phone", payPalAccountNonce.phone)
      return result
    }

    fun createTokenizeCardDataNonce(cardNonce: CardNonce): WritableMap {
      val result: WritableMap = Arguments.createMap()
      result.putString("nonce", cardNonce.string)
      if (cardNonce.cardType == "Unknown") {
        result.putString("cardNetwork", "")
      } else {
        result.putString("cardNetwork", cardNonce.cardType)
      }
      result.putString("lastFour", cardNonce.lastFour)
      result.putString("lastTwo", cardNonce.lastTwo)
      result.putString("expirationMonth", cardNonce.expirationMonth)
      result.putString("expirationYear", cardNonce.expirationYear)
      return result
    }

    fun createVaultRequest(options: ReadableMap): PayPalVaultRequest {
      val hasUserLocationConsent: String = options.getString("hasUserLocationConsent") ?: "false"
      val request = when (hasUserLocationConsent) {
        "false" -> PayPalVaultRequest(false)
        "true" -> PayPalVaultRequest(true)
        else -> throw IllegalArgumentException("Invalid hasUserLocationConsent parameter")
      }

      if (options.hasKey("billingAgreementDescription")) {
        request.billingAgreementDescription =  options.getString("billingAgreementDescription")
      }
      if (options.hasKey("localeCode")) {
        request.localeCode = options.getString("localeCode")
      }
      if (options.hasKey("displayName")) {
        request.displayName = options.getString("displayName")
      }
      if (options.hasKey("offerCredit")) {
        val offerCredit: String = options.getString("offerCredit") ?: ""
        when (offerCredit) {
          "true" -> request.shouldOfferCredit = true
        }
      }
      if (options.hasKey("isShippingAddressRequired")) {
        val isShippingAddressRequired: String = options.getString("isShippingAddressRequired") ?: ""
        when (isShippingAddressRequired) {
          "true" -> request.isShippingAddressRequired = true
        }
      }
      if (options.hasKey("isShippingAddressEditable")) {
        val isShippingAddressEditable: String = options.getString("isShippingAddressEditable") ?: ""
        when (isShippingAddressEditable) {
          "true" -> request.isShippingAddressEditable = true
        }
      }
      return request
    }

    fun createCheckoutRequest(options: ReadableMap): PayPalCheckoutRequest {
      val hasUserLocationConsent: String = options.getString("hasUserLocationConsent") ?: "false"
      val request = when (hasUserLocationConsent) {
        "false" -> PayPalCheckoutRequest(options.getString("amount") ?: "",false)
        "true" -> PayPalCheckoutRequest(options.getString("amount") ?: "", true)
        else -> throw IllegalArgumentException("Invalid hasUserLocationConsent parameter")
      }
      if (options.hasKey("billingAgreementDescription")) request.billingAgreementDescription = options.getString("billingAgreementDescription")
      if (options.hasKey("localeCode")) request.localeCode = options.getString("localeCode")
        ?: "en-US"
      if (options.hasKey("displayName")) request.displayName = options.getString("displayName")
      if (options.hasKey("userAction")) {
        val userAction: String = options.getString("userAction") ?: ""
        when (userAction) {
          "payNow" -> request.userAction = PayPalPaymentUserAction.USER_ACTION_COMMIT
        }
      }
      if (options.hasKey("isShippingAddressRequired")) {
        val isShippingAddressRequired: String = options.getString("isShippingAddressRequired") ?: ""
        when (isShippingAddressRequired) {
          "false" -> request.isShippingAddressRequired = false
          "true" -> request.isShippingAddressRequired = true
        }
      }
      if (options.hasKey("intent")) {
        val intent: String = options.getString("intent") ?: ""
        when (intent) {
          "sale" -> request.intent = PayPalPaymentIntent.SALE
          "order" -> request.intent = PayPalPaymentIntent.ORDER
        }
      } else {
        request.intent = PayPalPaymentIntent.AUTHORIZE
      }
      return request
    }

    fun createTokenizeCardRequest(options: ReadableMap): Card {
      val card: Card = Card()
      if (options.hasKey("number")) {
        card.number = options.getString("number")
      }
      if (options.hasKey("expirationMonth")) {
        card.expirationMonth = options.getString("expirationMonth")
      }
      if (options.hasKey("expirationYear")) {
        card.expirationYear = options.getString("expirationYear")
      }
      if (options.hasKey("cvv")) {
        card.cvv = (options.getString("cvv"))
      }
      if (options.hasKey("postalCode")) {
        card.postalCode = options.getString("postalCode")
      }
      return card
    }
  }
}
