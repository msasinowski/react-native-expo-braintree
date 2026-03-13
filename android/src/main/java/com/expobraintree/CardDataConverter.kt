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

    /**
     * Converts the 3D Secure result nonce into a WritableMap to be sent back to JavaScript.
     * This includes card details and the critical threeDSecureInfo object.
     */
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

      val infoMap: WritableMap = Arguments.createMap()
      val info = cardNonce.threeDSecureInfo
      
      if (info != null) {
          infoMap.putBoolean("liabilityShifted", info.liabilityShifted)
          infoMap.putBoolean("liabilityShiftPossible", info.liabilityShiftPossible)
          infoMap.putString("status", info.status)
          infoMap.putBoolean("wasVerified", info.wasVerified)
      }

      result.putMap("threeDSecureInfo", infoMap)
      
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
      
      // Map personal names - Note: Braintree v6 uses givenName and surname
      if (options.hasKey("givenName")) {
        address.givenName = options.getString("givenName")
      }
      if (options.hasKey("surName")) {
        address.surname = options.getString("surName")
      }
      
      // Map contact details
      if (options.hasKey("phoneNumber")) {
        address.phoneNumber = options.getString("phoneNumber")
      }
      
      // CRITICAL: countryCodeAlpha2 MUST be provided if 'region' is present.
      // This prevents the "The region cannot be provided without a corresponding country code" (422) error.
      if (options.hasKey("countryCodeAlpha2")) {
        address.countryCodeAlpha2 = options.getString("countryCodeAlpha2")
      }
      
      // Map geographic location details
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

      // 3D Secure 2.0 requires additional info for better risk assessment
      val additionalInformation = ThreeDSecureAdditionalInformation()
      additionalInformation.shippingAddress = address
      
      val threeDSecureRequest = ThreeDSecureRequest()
      // Use elvis operator to ensure non-null values for the SDK
      threeDSecureRequest.nonce = options.getString("nonce") ?: ""
      threeDSecureRequest.email = options.getString("email") ?: ""
      threeDSecureRequest.amount = options.getString("amount") ?: "0.00"
      
      // Attach the objects to the main request
      threeDSecureRequest.billingAddress = address
      threeDSecureRequest.additionalInformation = additionalInformation
      
      return threeDSecureRequest
    }
  }
}
