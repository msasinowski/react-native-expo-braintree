package com.expobraintree

import com.braintreepayments.api.VenmoRequest 
import com.braintreepayments.api.VenmoPaymentMethodUsage
import com.braintreepayments.api.VenmoAccountNonce

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap


class VenmoDataConverter {

  companion object {
    fun convertVenmoDataAccountNonce(nonce: VenmoAccountNonce): WritableMap {
      val result: WritableMap = Arguments.createMap()
      result.putString("nonce", nonce.string)
      result.putString("email", nonce.email)
      result.putString("externalID", nonce.externalId)
      result.putString("firstName", nonce.firstName)
      result.putString("lastName", nonce.lastName)
      result.putString("phoneNumber", nonce.phoneNumber)
      result.putString("username", nonce.username)
      return result
    }

    fun createRequest(options: ReadableMap): VenmoRequest {
      val request: VenmoRequest;
      val paymentMethodUsage: String = options.getString("paymentMethodUsage") ?: ""
      when (paymentMethodUsage) {
        "multiUse" -> VenmoRequest(VenmoPaymentMethodUsage.MULTI_USE)
        "singleUse" -> VenmoRequest(VenmoPaymentMethodUsage.SINGLE_USE)
      }
      if (options.hasKey("profileId")) {
        request.setProfileId(options.getString("profileId"))
      }
      if (options.hasKey("displayName")) {
        request.setDisplayName(options.getString("displayName"))
      }
      if (options.hasKey("subTotalAmount")) {
        request.setSubTotalAmount(options.getString("subTotalAmount"))
      }
      if (options.hasKey("discountAmount")) {
        request.setDiscountAmount(options.getString("discountAmount"))
      }
      if (options.hasKey("taxAmount")) {
        request.setTaxAmount(options.getString("taxAmount"))
      }
      if (options.hasKey("shippingAmount")) {
        request.setShippingAmount(options.getString("shippingAmount"))
      }
      if (options.hasKey("totalAmount")) {
        request.setTotalAmount(options.getString("totalAmount"))
      }
      if (options.hasKey("shouldVault")) {
        val shouldVault: String = options.getString("shouldVault") ?: ""
        when (shouldVault) {
          "true" -> request.shouldVault = true
        }
      }
      if (options.hasKey("collectCustomerBillingAddress")) {
        val collectCustomerBillingAddress: String = options.getString("collectCustomerBillingAddress") ?: ""
        when (collectCustomerBillingAddress) {
          "true" -> request.collectCustomerBillingAddress(true)
        }
      }
      if (options.hasKey("collectCustomerShippingAddress")) {
        val collectCustomerShippingAddress: String = options.getString("collectCustomerShippingAddress") ?: ""
        when (collectCustomerShippingAddress) {
          "true" -> request.collectCustomerShippingAddress(true)
        }
      }
      if (options.hasKey("isFinalAmount")) {
        val isFinalAmount: String = options.getString("isFinalAmount") ?: ""
        when (isFinalAmount) {
          "true" -> request.isFinalAmount(true)
        }
      }
      return request
    }
  }
}
