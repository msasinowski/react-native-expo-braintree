//
//  getBTPayPalCheckoutintentByString.swift
//  expo-braintree
//
//  Created by Maciej Sasinowski on 28/04/2024.
//

import Braintree
import Foundation

func getBTPayPalCheckoutIntentByString(intent: String?) -> BTPayPalRequestIntent {
  switch intent {
  case "authorize":
    return BTPayPalRequestIntent.authorize
  case "order":
    return BTPayPalRequestIntent.order
  case "sale":
    return BTPayPalRequestIntent.sale
  default:
    return BTPayPalRequestIntent.authorize
  }
}
