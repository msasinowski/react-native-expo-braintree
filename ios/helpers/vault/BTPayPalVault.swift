//
//  BTPayPalVaultRequest.swift
//  expo-braintree
//
//  Created by Maciej Sasinowski on 28/04/2024.
//

import Braintree
import Foundation

func prepareBTPayPalVaultRequest(options: [String: String]) -> BTPayPalVaultRequest {
  let vaultRequest = BTPayPalVaultRequest()
  let billingAgreementDescription = options["billingAgreementDescription"]
  if !(billingAgreementDescription ?? "").isEmpty {
    vaultRequest.billingAgreementDescription = billingAgreementDescription
  }
  let displayName = options["displayName"]
  if !(displayName ?? "").isEmpty {
    vaultRequest.displayName = displayName
  }
  let localeCode = options["localeCode"]
  if !(localeCode ?? "").isEmpty {
    vaultRequest.localeCode = getLocaleCodeValueByString(localeCode: localeCode)
  }
  let userAuthenticationEmail = options["userAuthenticationEmail"]
  if !(userAuthenticationEmail ?? "").isEmpty {
    vaultRequest.userAuthenticationEmail = userAuthenticationEmail
  }
  vaultRequest.offerCredit = getBoolValueByString(
    value: options["offerCredit"], defaultValue: false)
  vaultRequest.isShippingAddressRequired = getBoolValueByString(
    value: options["isShippingAddressRequired"], defaultValue: false)
  vaultRequest.isShippingAddressEditable = getBoolValueByString(
    value: options["isShippingAddressEditable"], defaultValue: false)
  vaultRequest.isAccessibilityElement = getBoolValueByString(
    value: options["isAccessibilityElement"], defaultValue: false)
  return vaultRequest
}
