//
//  BTPayPalCheckout.swift
//  react-native-paypal-reborn
//
//  Created by Maciej Sasinowski on 28/04/2024.
//

import Braintree
import Foundation

func prepareBTPayPalCheckoutRequest(options: [String?: String]) -> BTPayPalCheckoutRequest {
  let amount = options["amount"] ?? ""
  let intent = getBTPayPalCheckoutIntentByString(intent: options["intent"])
  let userAction = getBTPayPalRequestUserActionByString(intent: options["userAction"])
  let offerPayLater = getBoolValueByString(value: options["offerPayLater"], defaultValue: false)
  let currencyCode = options["currencyCode"]
  let requestBillingAgreement = getBoolValueByString(
    value: options["requestBillingAgreement"], defaultValue: false)

  let checkoutRequest = BTPayPalCheckoutRequest(
    amount: amount, intent: intent, userAction: userAction, offerPayLater: offerPayLater,
    currencyCode: currencyCode, requestBillingAgreement: requestBillingAgreement)

  return checkoutRequest
}
