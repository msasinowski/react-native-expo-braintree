package com.expobraintree

import com.braintreepayments.api.core.PostalAddress
import com.braintreepayments.api.datacollector.DataCollectorRequest
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.ReadableMap

class SharedDataConverter {
    companion object {
        fun createError(domain: String, details: String?): WritableMap {
            val result: WritableMap = Arguments.createMap()
            result.putString("domain", domain)
            result.putString("details", details ?: "Unknown error") // Obsługa null dla detali
            return result
        }

        fun convertAddressData(address: PostalAddress?): WritableMap {
            val result: WritableMap = Arguments.createMap()
            result.putString("recipientName", address?.recipientName)
            result.putString("streetAddress", address?.streetAddress)
            result.putString("extendedAddress", address?.extendedAddress)
            result.putString("locality", address?.locality)
            result.putString("countryCodeAlpha2", address?.countryCodeAlpha2)
            result.putString("postalCode", address?.postalCode)
            result.putString("region", address?.region)
            return result
        }

        fun createDataCollectorRequest(options: ReadableMap): DataCollectorRequest {
            val hasConsentStr = if (options.hasKey("hasUserLocationConsent")) {
                options.getString("hasUserLocationConsent")
            } else "false"
            
            val hasConsent = hasConsentStr == "true"
            
            val riskCorrelationId = if (options.hasKey("riskCorrelationId")) {
                options.getString("riskCorrelationId")
            } else null

            return DataCollectorRequest(hasConsent, riskCorrelationId)
        }
    }
}