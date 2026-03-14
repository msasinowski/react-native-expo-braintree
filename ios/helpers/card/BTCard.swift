//
//  BTCard.swift
//  expo-braintree
//
//  Created by Maciej Sasinowski on 28/04/2024.
//

import Braintree
import Foundation

/**
 * Prepares a BTCard object from the options dictionary received from JavaScript.
 */
func prepareCardData(options: [String: String]) -> BTCard {
    let card = BTCard()
    
    // Standard credit card fields mapping
    card.number = options["number"]
    card.expirationMonth = options["expirationMonth"]
    card.expirationYear = options["expirationYear"]
    card.cvv = options["cvv"]
    card.postalCode = options["postalCode"]
    
    // Braintree recommends performing validation on the server side.
    // Setting shouldValidate to false prevents the SDK from blocking tokenization locally.
    card.shouldValidate = false
    
    return card
}

/**
 * Maps the BTCardNonce result into an NSDictionary to be sent back to React Native.
 */
func prepareBTCardNonceResult(cardNonce: BTCardNonce) -> NSDictionary {
    let result = NSMutableDictionary()
    
    // The payment method nonce string used for transactions
    result["nonce"] = cardNonce.nonce
    
    // Transform the cardNetwork Enum into a readable String (e.g., "Visa")
    result["cardNetwork"] = transformCardNetworkToString(cardNonce.cardNetwork)
    
    // Metadata about the card
    result["lastFour"] = cardNonce.lastFour
    result["lastTwo"] = cardNonce.lastTwo
    result["expirationMonth"] = cardNonce.expirationMonth
    result["expirationYear"] = cardNonce.expirationYear
    
    return result
}

/**
 * Helper function to map BTCardNetwork enums to human-readable strings.
 * This prevents receiving raw integers or unknown objects in JavaScript.
 */
private func transformCardNetworkToString(_ network: BTCardNetwork) -> String {
    switch network {
    case .visa: 
        return "Visa"
    case .masterCard: 
        return "MasterCard"
    case .AMEX:
        return "Amex"
    case .dinersClub: 
        return "DinersClub"
    case .JCB:
        return "JCB"
    case .maestro: 
        return "Maestro"
    case .discover: 
        return "Discover"
    case .unionPay: 
        return "UnionPay"
    case .hiper: 
        return "Hiper"
    case .hipercard: 
        return "Hipercard"
    case .unknown: 
        return "Unknown"
    @unknown default:
        return "Unknown"
    }
}