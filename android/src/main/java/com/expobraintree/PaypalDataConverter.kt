package com.expobraintree

//import com.braintreepayments.api.Card
import com.braintreepayments.api.card.CardNonce
//import com.braintreepayments.api.PayPalAccountNonce
//import com.braintreepayments.api.PayPalCheckoutRequest
//import com.braintreepayments.api.PayPalPaymentIntent
import com.braintreepayments.api.paypal.PayPalVaultRequest
//import com.braintreepayments.api.PostalAddress
import com.braintreepayments.api.threedsecure.ThreeDSecureAdditionalInformation
import com.braintreepayments.api.threedsecure.ThreeDSecurePostalAddress
import com.braintreepayments.api.threedsecure.ThreeDSecureRequest
import com.braintreepayments.api.threedsecure.ThreeDSecureNonce
import com.braintreepayments.api.card.Card
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap


class PaypalDataConverter {

  companion object {
    //    fun convertAddressData(address: PostalAddress): WritableMap {
//      val result: WritableMap = Arguments.createMap();
//      result.putString("recipientName", address.recipientName)
//      result.putString("streetAddress", address.streetAddress)
//      result.putString("extendedAddress", address.extendedAddress)
//      result.putString("locality", address.locality)
//      result.putString("countryCodeAlpha2", address.countryCodeAlpha2)
//      result.putString("postalCode", address.postalCode)
//      result.putString("region", address.region)
//      return result
//    }
//
//    fun convertPaypalDataAccountNonce(payPalAccountNonce: PayPalAccountNonce): WritableMap {
//      val result: WritableMap = Arguments.createMap()
//      result.putString("nonce", payPalAccountNonce.string)
//      result.putString("payerId", payPalAccountNonce.payerId)
//      result.putString("email", payPalAccountNonce.email)
//      result.putString("firstName", payPalAccountNonce.firstName)
//      result.putString("lastName", payPalAccountNonce.lastName)
//      result.putString("phone", payPalAccountNonce.phone)
//      return result
//    }
//
    fun createTokenizeCardDataNonce(
      cardNonce: CardNonce
    ): WritableMap {
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

    fun createThreeDSecureCardDataNonce(threeDSecureNonce: ThreeDSecureNonce): WritableMap {
      val result: WritableMap = Arguments.createMap()
      result.putString("nonce", threeDSecureNonce.string)
      if (threeDSecureNonce.cardType == "Unknown") {
        result.putString("cardNetwork", "")
      } else {
        result.putString("cardNetwork", threeDSecureNonce.cardType)
      }
      result.putString("lastFour", threeDSecureNonce.lastFour)
      result.putString("lastTwo", threeDSecureNonce.lastTwo)
      result.putString("expirationMonth", threeDSecureNonce.expirationMonth)
      result.putString("expirationYear", threeDSecureNonce.expirationYear)
      return result
    }

    fun createError(domain: String, details: String?): WritableMap {
      val result: WritableMap = Arguments.createMap();
      result.putString("domain", domain)
      result.putString("details", details)
      return result
    }

    fun createVaultRequest(options: ReadableMap): PayPalVaultRequest {
      val request: PayPalVaultRequest = PayPalVaultRequest(hasUserLocationConsent = true)
      if (options.hasKey("billingAgreementDescription")) {
        request.billingAgreementDescription = options.getString("billingAgreementDescription")
      }
      if (options.hasKey("localeCode")) {
        request.localeCode= options.getString("localeCode")
      }
      if (options.hasKey("displayName")) {
        request.displayName=options.getString("displayName")
      }
      if (options.hasKey("offerCredit")) {
        val offerCredit: String = options.getString("offerCredit") ?: ""
        when (offerCredit) {
          "true" -> request.shouldOfferCredit= true
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
          "true" -> request.isShippingAddressEditable= true
        }
      }
      return request
    }
//
//    fun createCheckoutRequest(options: ReadableMap): PayPalCheckoutRequest {
//      val request = PayPalCheckoutRequest(options.getString("amount") ?: "")
//      if (options.hasKey("billingAgreementDescription")) request.billingAgreementDescription = options.getString("billingAgreementDescription")
//      if (options.hasKey("localeCode")) request.localeCode = options.getString("localeCode")
//        ?: "en-US"
//      if (options.hasKey("displayName")) request.displayName = options.getString("displayName")
//      if (options.hasKey("userAction")) request.userAction = options.getString("userAction")
//        ?: "none"
//      if (options.hasKey("isShippingAddressRequired")) {
//        val isShippingAddressRequired: String = options.getString("isShippingAddressRequired") ?: ""
//        when (isShippingAddressRequired) {
//          "false" -> request.isShippingAddressRequired = false
//          "true" -> request.isShippingAddressRequired = true
//        }
//      }
//      if (options.hasKey("intent")) {
//        val intent: String = options.getString("intent") ?: ""
//        when (intent) {
//          "sale" -> request.intent = PayPalPaymentIntent.SALE
//          "order" -> request.intent = PayPalPaymentIntent.ORDER
//        }
//      } else {
//        request.intent = PayPalPaymentIntent.AUTHORIZE
//      }
//      return request
//    }

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
        card.cvv = options.getString("cvv")
      }
      if (options.hasKey("postalCode")) {
        card.postalCode = options.getString("postalCode")
      }
      return card
    }

    fun create3DSecureRequest(options: ReadableMap): ThreeDSecureRequest {
      val address = ThreeDSecurePostalAddress()

      if (options.hasKey("firstname")) {
        address.givenName = options.getString("firstname")
      }

      if (options.hasKey("lastname")) {
        address.surname = options.getString("lastname")
      }

      if (options.hasKey("phoneNumber")) {
        address.phoneNumber = options.getString("phoneNumber")
      }

      if (options.hasKey("countryCode")) {
        address.countryCodeAlpha2 = options.getString("countryCode")
      }

      if (options.hasKey("city")) {
        address.locality = options.getString("city")
      }

      if (options.hasKey("postalCode")) {
        address.postalCode = options.getString("postalCode")
      }

      if (options.hasKey("region")) {
        address.region = options.getString("region")
      }

      if (options.hasKey("streetAddress")) {
        address.streetAddress = options.getString("streetAddress")
      }

      if (options.hasKey("streetAddress2")) {
        address.extendedAddress = options.getString("streetAddress2")
      }

      val additionalInformation = ThreeDSecureAdditionalInformation()
      additionalInformation.shippingAddress = address

      val threeDSecureRequest = ThreeDSecureRequest()
      threeDSecureRequest.nonce = options.getString("nonce")
      threeDSecureRequest.email = options.getString("email")
      threeDSecureRequest.billingAddress = address
      threeDSecureRequest.additionalInformation = additionalInformation
      threeDSecureRequest.amount = options.getString("amount")
      return threeDSecureRequest
    }

  }
}
