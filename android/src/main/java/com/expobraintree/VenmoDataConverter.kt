package com.expobraintree

import com.braintreepayments.api.venmo.VenmoAccountNonce
import com.braintreepayments.api.venmo.VenmoPaymentMethodUsage
import com.braintreepayments.api.venmo.VenmoRequest
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
      val paymentMethodUsage: String = options.getString("paymentMethodUsage") ?: ""
      val request = when (paymentMethodUsage) {
        "multiUse" -> VenmoRequest(VenmoPaymentMethodUsage.MULTI_USE)
        "singleUse" -> VenmoRequest(VenmoPaymentMethodUsage.SINGLE_USE)
        else -> throw IllegalArgumentException("Invalid payment method usage")
      }
      if (options.hasKey("profileId")) {
        request.profileId = options.getString("profileId")
      }
      if (options.hasKey("displayName")) {
        request.displayName = options.getString("displayName")
      }
      if (options.hasKey("subTotalAmount")) {
        request.subTotalAmount = options.getString("subTotalAmount")
      }
      if (options.hasKey("discountAmount")) {
        request.discountAmount = options.getString("discountAmount")
      }
      if (options.hasKey("taxAmount")) {
        request.taxAmount =  options.getString("taxAmount")
      }
      if (options.hasKey("shippingAmount")) {
        request.shippingAmount = options.getString("shippingAmount")
      }
      if (options.hasKey("totalAmount")) {
        request.totalAmount = options.getString("totalAmount")
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
          "true" -> request.collectCustomerBillingAddress = true
        }
      }
      if (options.hasKey("collectCustomerShippingAddress")) {
        val collectCustomerShippingAddress: String = options.getString("collectCustomerShippingAddress") ?: ""
        when (collectCustomerShippingAddress) {
          "true" -> request.collectCustomerShippingAddress = true
        }
      }
      return request
    }
  }
}
