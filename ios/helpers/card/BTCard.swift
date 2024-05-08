//  BTCard.swift
//  expo-braintree
//
//  Created by Maciej Sasinowski on 28/04/2024.
//
import Braintree
import Foundation

func prepareCardData(options: [String: String]) -> BTCard {
  let card = BTCard()
  card.number = options["number"]
  card.expirationMonth = options["expirationMonth"]
  card.expirationYear = options["expirationYear"]
  card.cvv = options["cvv"]
  card.postalCode = options["postalCode"]
  // Validation on the client side is not yet supported on BT side
  card.shouldValidate = false
  return card
}

func prepareBTCardNonceResult(cardNonce: BTCardNonce)
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
