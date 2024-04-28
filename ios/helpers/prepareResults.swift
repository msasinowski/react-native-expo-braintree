//
//  prepareResults.swift
//  react-native-paypal-reborn
//
//  Created by Maciej Sasinowski on 28/04/2024.
//

import Braintree
import Foundation

func prepareAddressResult(address: BTPostalAddress?) -> NSMutableDictionary {
  let addressResult = NSMutableDictionary()
  addressResult["recipientName"] = address?.recipientName
  addressResult["streetAddress"] = address?.streetAddress
  addressResult["extendedAddress"] = address?.extendedAddress
  addressResult["locality"] = address?.locality
  addressResult["countryCodeAlpha2"] = address?.countryCodeAlpha2
  addressResult["postalCode"] = address?.postalCode
  addressResult["region"] = address?.region
  return addressResult
}

func prepareBTPayPalAccountNonceResult(accountNonce: BTPayPalAccountNonce)
  -> NSDictionary
{
  let result = NSMutableDictionary()
  result["email"] = accountNonce.email ?? ""
  result["payerID"] = accountNonce.payerID ?? ""
  result["nonce"] = accountNonce.nonce
  result["firstName"] = accountNonce.firstName ?? ""
  result["lastName"] = accountNonce.lastName ?? ""
  result["billingAddress"] = prepareAddressResult(address: accountNonce.billingAddress)
  result["shippingAddress"] = prepareAddressResult(address: accountNonce.shippingAddress)
  return result
}
