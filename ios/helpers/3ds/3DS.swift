//  3DS.swift
//  expo-braintree
//
//  Created by Maciej Sasinowski on 28/04/2024.
//
import Braintree
import Foundation

func prepare3DSecureData(options: [String: String]) -> BTThreeDSecureRequest {
    let threeDSecureRequest = BTThreeDSecureRequest()
    
    // Używamy specyficznego Locale, aby kropka zawsze działała jako separator
    let formatter = NumberFormatter()
    formatter.locale = Locale(identifier: "en_US_POSIX") 
    formatter.numberStyle = .decimal
    formatter.generatesDecimalNumbers = true
    
    let amountString = options["amount"] ?? "0"
    
    // Bezpieczne parsowanie kwoty
    if let amountNumber = formatter.number(from: amountString) as? NSDecimalNumber {
        threeDSecureRequest.amount = amountNumber
    } else {
        // Fallback: jeśli formatter zawiedzie, próbujemy bezpośredniej inicjalizacji
        threeDSecureRequest.amount = NSDecimalNumber(string: amountString, locale: Locale(identifier: "en_US_POSIX"))
    }
    
    threeDSecureRequest.nonce = options["nonce"]
    threeDSecureRequest.email = options["email"]
    
    let postalAddress = BTThreeDSecurePostalAddress()
    postalAddress.givenName = options["givenName"]
    postalAddress.surname = options["surName"]
    postalAddress.phoneNumber = options["phoneNumber"]
    postalAddress.streetAddress = options["streetAddress"]
    postalAddress.extendedAddress = options["extendedAddress"]
    postalAddress.locality = options["city"]
    postalAddress.postalCode = options["postalCode"]
    postalAddress.region = options["region"]
    postalAddress.countryCodeAlpha2 = options["countryCodeAlpha2"]
    
    let additionalInfo = BTThreeDSecureAdditionalInformation()
    additionalInfo.shippingAddress = postalAddress
    
    threeDSecureRequest.additionalInformation = additionalInfo
    threeDSecureRequest.billingAddress = postalAddress
    
    return threeDSecureRequest
}

func prepare3DSecureNonceResult(tokenizedCard: BTCardNonce) -> NSDictionary {
    let result = NSMutableDictionary()
    
    result["nonce"] = tokenizedCard.nonce
    result["cardNetwork"] = tokenizedCard.cardNetwork == .unknown ? "Unknown" : String(describing: tokenizedCard.cardNetwork)
    result["lastFour"] = tokenizedCard.lastFour
    result["lastTwo"] = tokenizedCard.lastTwo
    result["expirationMonth"] = tokenizedCard.expirationMonth
    result["expirationYear"] = tokenizedCard.expirationYear
    
    // CRITICAL: Added threeDSecureInfo so JS knows the status of authentication
    let info = NSMutableDictionary()
    info["liabilityShifted"] = tokenizedCard.threeDSecureInfo.liabilityShifted
    info["liabilityShiftPossible"] = tokenizedCard.threeDSecureInfo.liabilityShiftPossible
    info["wasVerified"] = tokenizedCard.threeDSecureInfo.wasVerified
    info["status"] = tokenizedCard.threeDSecureInfo.status
    
    result["threeDSecureInfo"] = info
    
    return result
}