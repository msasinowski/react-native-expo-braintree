package com.expobraintree

import com.braintreepayments.api.googlepay.GooglePayRequest
import com.braintreepayments.api.googlepay.GooglePayTotalPriceStatus
import com.facebook.react.bridge.ReadableMap

class GooglePayDataConverter {
    companion object {
        /**
         * Maps all possible GooglePayRequest options from React Native to the Braintree SDK.
         * Based on: https://braintree.github.io/braintree_android/GooglePay/com.braintreepayments.api/-google-pay-request/index.html
         */
        fun createPaymentRequest(data: ReadableMap): GooglePayRequest {
            val totalPrice = data.getString("totalPrice") ?: "0.00"
            val currencyCode = data.getString("currencyCode") ?: "USD"
            
            // Map price status: 1 -> ESTIMATED, else -> FINAL
            val statusInt = if (data.hasKey("totalPriceStatus")) data.getInt("totalPriceStatus") else 3
            val totalPriceStatus = when (statusInt) {
                1 -> GooglePayTotalPriceStatus.TOTAL_PRICE_STATUS_ESTIMATED
                else -> GooglePayTotalPriceStatus.TOTAL_PRICE_STATUS_FINAL
            }

            val request = GooglePayRequest(
                currencyCode,
                totalPrice,
                totalPriceStatus
            )

            if (data.hasKey("googleMerchantName")) {
                request.googleMerchantName = data.getString("googleMerchantName")
            }
            // Requirements
            if (data.hasKey("billingAddressRequired")) {
                request.isBillingAddressRequired = data.getBoolean("billingAddressRequired")
            }
            if (data.hasKey("emailRequired")) {
                request.isEmailRequired = data.getBoolean("emailRequired")
            }
            if (data.hasKey("phoneNumberRequired")) {
                request.isPhoneNumberRequired = data.getBoolean("phoneNumberRequired")
            }
            if (data.hasKey("shippingAddressRequired")) {
                request.isShippingAddressRequired = data.getBoolean("shippingAddressRequired")
            }

            // Card Restrictions
            if (data.hasKey("allowPrepaidCards")) {
                request.allowPrepaidCards = data.getBoolean("allowPrepaidCards")
            }
            if (data.hasKey("allowCreditCards")) {
                request.allowCreditCards = data.getBoolean("allowCreditCards")
            }
            return request
        }
    }
}