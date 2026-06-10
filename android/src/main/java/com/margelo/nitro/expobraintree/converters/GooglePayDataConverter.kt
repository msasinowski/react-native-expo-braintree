package com.margelo.nitro.expobraintree.converters

import com.braintreepayments.api.googlepay.GooglePayRequest
import com.braintreepayments.api.googlepay.GooglePayTotalPriceStatus
import com.facebook.react.bridge.ReadableMap
class GooglePayDataConverter {
    companion object {
        private fun getBool(data: ReadableMap, key: String, default: Boolean = false): Boolean {
            if (!data.hasKey(key)) return default
            return try {
                data.getBoolean(key)
            } catch (e: Exception) {
                data.getString(key)?.lowercase() == "true"
            }
        }

        private fun getInt(data: ReadableMap, key: String, default: Int = 0): Int {
            if (!data.hasKey(key)) return default
            return try {
                data.getInt(key)
            } catch (e: Exception) {
                data.getString(key)?.toIntOrNull() ?: default
            }
        }

        fun createPaymentRequest(data: ReadableMap): GooglePayRequest {
            val totalPrice = data.getString("totalPrice") ?: "0.00"
            val currencyCode = data.getString("currencyCode") ?: "USD"
            val statusInt = getInt(data, "totalPriceStatus", 3)
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
            request.isBillingAddressRequired = getBool(data, "billingAddressRequired")
            request.isEmailRequired = getBool(data, "emailRequired")
            request.isPhoneNumberRequired = getBool(data, "phoneNumberRequired")
            request.isShippingAddressRequired = getBool(data, "shippingAddressRequired")
            request.allowPrepaidCards = getBool(data, "allowPrepaidCards", true)
            return request
        }
    }
}