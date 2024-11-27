package com.expobraintree

import com.braintreepayments.api.card.Card
import com.braintreepayments.api.card.CardNonce
import com.braintreepayments.api.threedsecure.ThreeDSecureAdditionalInformation
import com.braintreepayments.api.threedsecure.ThreeDSecureNonce
import com.braintreepayments.api.threedsecure.ThreeDSecurePostalAddress
import com.braintreepayments.api.threedsecure.ThreeDSecureRequest
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap


class CardDataConverter {

  companion object {

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

    fun createThreeDSecureDataNonce(cardNonce: ThreeDSecureNonce): WritableMap {
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
