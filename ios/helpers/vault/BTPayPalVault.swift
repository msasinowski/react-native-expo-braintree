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
    
    if let billingDescription = options["billingAgreementDescription"], !billingDescription.isEmpty {
        vaultRequest.billingAgreementDescription = billingDescription
    }
    
    if let displayName = options["displayName"], !displayName.isEmpty {
        vaultRequest.displayName = displayName
    }
    
    if let localeCode = options["localeCode"], !localeCode.isEmpty {
        vaultRequest.localeCode = getLocaleCodeValueByString(localeCode: localeCode)
    }
    
    if let userEmail = options["userAuthenticationEmail"], !userEmail.isEmpty {
        vaultRequest.userAuthenticationEmail = userEmail
    }
    
    vaultRequest.offerCredit = getBoolValueByString(
        value: options["offerCredit"], 
        defaultValue: false
    )
    
    vaultRequest.isShippingAddressRequired = getBoolValueByString(
        value: options["isShippingAddressRequired"], 
        defaultValue: false
    )
    
    vaultRequest.isShippingAddressEditable = getBoolValueByString(
        value: options["isShippingAddressEditable"], 
        defaultValue: false
    )
    
    vaultRequest.isAccessibilityElement = getBoolValueByString(
        value: options["isAccessibilityElement"], 
        defaultValue: false
    )
    
    return vaultRequest
}