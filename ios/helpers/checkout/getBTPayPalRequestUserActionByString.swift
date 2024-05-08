//
//  getBTPayPalRequestUserActionByString.swift
//  expo-braintree
//
//  Created by Maciej Sasinowski on 28/04/2024.
//

import Braintree
import Foundation

func getBTPayPalRequestUserActionByString(intent: String?) -> BTPayPalRequestUserAction {
  switch intent {
  case "none":
    return BTPayPalRequestUserAction.none
  case "payNow":
    return BTPayPalRequestUserAction.payNow
  default:
    return BTPayPalRequestUserAction.none
  }
}
