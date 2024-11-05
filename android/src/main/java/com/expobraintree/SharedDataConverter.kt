package com.expobraintree

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap

import com.braintreepayments.api.PostalAddress


class SharedDataConverter {
  companion object {
    fun createError(domain: String, details: String?): WritableMap {
      val result: WritableMap = Arguments.createMap();
      result.putString("domain", domain)
      result.putString("details", details)
      return result
    }

    fun convertAddressData(address: PostalAddress): WritableMap {
      val result: WritableMap = Arguments.createMap();
      result.putString("recipientName", address.recipientName)
      result.putString("streetAddress", address.streetAddress)
      result.putString("extendedAddress", address.extendedAddress)
      result.putString("locality", address.locality)
      result.putString("countryCodeAlpha2", address.countryCodeAlpha2)
      result.putString("postalCode", address.postalCode)
      result.putString("region", address.region)
      return result
    }
  }
}
