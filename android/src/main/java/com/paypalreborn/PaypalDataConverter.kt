package com.paypalreborn

import com.braintreepayments.api.PayPalAccountNonce
import com.braintreepayments.api.PayPalCheckoutRequest
import com.braintreepayments.api.PayPalPaymentIntent
import com.braintreepayments.api.PayPalVaultRequest
import com.braintreepayments.api.PostalAddress
import com.braintreepayments.api.Card;
import com.braintreepayments.api.CardNonce;

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap


class PaypalDataConverter {

  companion object {
    fun convertAddressData(address: PostalAddress): WritableMap {
      val result: WritableMap = Arguments.createMap();
      result.putString("recipientName", address.recipientName)
      result.putString("streetAddress", address.streetAddress)
      result.putString("extendedAddress", address.extendedAddress)
      result.putString("locality", address.locality)
      result.putString("countryCodeAlpha2", address.countryCodeAlpha2)
      result.putString("postalCode", address.postalCode)
      result.putString("region", address.region)
      return result
    }

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
      result.putString("lastFour", cardNonce.getLastFour())
      result.putString("lastTwo", cardNonce.getLastTwo())
      result.putString("expirationMonth", cardNonce.getExpirationMonth())
      result.putString("expirationYear", cardNonce.getExpirationYear())
      return result
    }

    fun createError(domain: String, details: String?): WritableMap {
      val result: WritableMap = Arguments.createMap();
      result.putString("domain", domain)
      result.putString("details", details)
      return result
    }

    fun createVaultRequest(options: ReadableMap): PayPalVaultRequest {
      val request: PayPalVaultRequest = PayPalVaultRequest()
      if (options.hasKey("billingAgreementDescription")) {
        request.setBillingAgreementDescription(options.getString("billingAgreementDescription"))
      }
      if (options.hasKey("localeCode")) {
        request.setLocaleCode(options.getString("localeCode"))
      }
      if (options.hasKey("displayName")) {
        request.setDisplayName(options.getString("displayName"))
      }
      if (options.hasKey("offerCredit")) {
        val offerCredit: String = options.getString("offerCredit") ?: ""
        when (offerCredit) {
          "true" -> request.setShouldOfferCredit(true)
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
          "true" -> request.setShippingAddressEditable(true)
        }
      }
      return request
    }

    fun createCheckoutRequest(options: ReadableMap): PayPalCheckoutRequest {
      val request = PayPalCheckoutRequest(options.getString("amount") ?: "")
      if (options.hasKey("billingAgreementDescription")) request.billingAgreementDescription = options.getString("billingAgreementDescription")
      if (options.hasKey("localeCode")) request.localeCode = options.getString("localeCode")
        ?: "en-US"
      if (options.hasKey("displayName")) request.displayName = options.getString("displayName")
      if (options.hasKey("userAction")) request.userAction = options.getString("userAction")
        ?: "none"
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
        card.setNumber(options.getString("number"))
      }
      if (options.hasKey("expirationMonth")) {
        card.setExpirationMonth(options.getString("expirationMonth"))
      }
      if (options.hasKey("expirationYear")) {
        card.setExpirationYear(options.getString("expirationYear"))
      }
      if (options.hasKey("cvv")) {
        card.setCvv(options.getString("cvv"))
      }
      if (options.hasKey("postalCode")) {
        card.setCvv(options.getString("postalCode"))
      }
      return card
    }


  }
}
