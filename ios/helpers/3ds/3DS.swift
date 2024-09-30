//  BTCard.swift
//  expo-braintree
//
//  Created by Maciej Sasinowski on 28/04/2024.
//
import Braintree
import Foundation

func prepare3DSecureData(options: [String: String]) -> BTThreeDSecureRequest {
  let threeDSecureRequest = BTThreeDSecureRequest()
  let formatter = NumberFormatter()
  let amount = options["amount"] ?? ""
  formatter.generatesDecimalNumbers = true
  threeDSecureRequest.amount = formatter.number(from: amount) as? NSDecimalNumber ?? 0
  threeDSecureRequest.nonce = options["nonce"]

  // Not required optional params 
  threeDSecureRequest.email = options["email"]
  let threeDsRequestPostalAddress = BTThreeDSecurePostalAddress()
  threeDsRequestPostalAddress.givenName = options["givenName"]
  threeDsRequestPostalAddress.surname = options["surName"]
  threeDsRequestPostalAddress.phoneNumber = options["phoneNumber"]
  threeDsRequestPostalAddress.streetAddress = options["streetAddress"]
  threeDsRequestPostalAddress.extendedAddress = options["extendedAddress"]
  threeDsRequestPostalAddress.locality = options["city"]
  threeDsRequestPostalAddress.postalCode = options["postalCode"]
  threeDsRequestPostalAddress.region = options["region"]
  threeDsRequestPostalAddress.countryCodeAlpha2 = options["countryCodeAlpha2"]

  let threeDsRequestAdditionalInformation = BTThreeDSecureAdditionalInformation()
  threeDsRequestAdditionalInformation.shippingAddress = threeDsRequestPostalAddress

  threeDSecureRequest.additionalInformation = threeDsRequestAdditionalInformation
  threeDSecureRequest.billingAddress = threeDsRequestPostalAddress;
  
  
  return threeDSecureRequest
}

func prepare3DSecureNonceResult(cardNonce: BTCardNonce)
  -> NSDictionary
{
  let result = NSMutableDictionary()
  result["nonce"] = cardNonce.nonce
  result["cardNetwork"] = cardNonce.cardNetwork
  result["lastFour"] = cardNonce.lastFour
  result["lastTwo"] = cardNonce.lastTwo
  result["expirationMonth"] = cardNonce.expirationMonth
  result["expirationYear"] = cardNonce.expirationYear
  return result
}
