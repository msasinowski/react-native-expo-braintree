import Braintree
import Foundation

/**
 * Maps a BTPostalAddress to an NSMutableDictionary for React Native.
 */
func prepareAddressResult(address: BTPostalAddress?) -> NSMutableDictionary {
    let addressResult = NSMutableDictionary()
    
    // In v7, properties are often optional; provide empty strings to avoid nil in the bridge
    addressResult["recipientName"] = address?.recipientName ?? ""
    addressResult["streetAddress"] = address?.streetAddress ?? ""
    addressResult["extendedAddress"] = address?.extendedAddress ?? ""
    addressResult["locality"] = address?.locality ?? ""
    addressResult["countryCodeAlpha2"] = address?.countryCodeAlpha2 ?? ""
    addressResult["postalCode"] = address?.postalCode ?? ""
    addressResult["region"] = address?.region ?? ""
    
    return addressResult
}

/**
 * Maps BTPayPalAccountNonce to an NSDictionary for the JS layer.
 * Updated for Braintree v7 modularity.
 */
func prepareBTPayPalAccountNonceResult(accountNonce: BTPayPalAccountNonce) -> NSDictionary {
    let result = NSMutableDictionary()
    
    // Core payment information
    result["nonce"] = accountNonce.nonce
    result["payerID"] = accountNonce.payerID ?? ""
    
    // Customer profile information
    result["email"] = accountNonce.email ?? ""
    result["phone"] = accountNonce.phone ?? ""
    result["firstName"] = accountNonce.firstName ?? ""
    result["lastName"] = accountNonce.lastName ?? ""
    
    // Address information
    result["billingAddress"] = prepareAddressResult(address: accountNonce.billingAddress)
    result["shippingAddress"] = prepareAddressResult(address: accountNonce.shippingAddress)
    
    return result
}

/**
 * Maps BTVenmoAccountNonce to an NSDictionary for the JS layer.
 * Updated for Braintree v7 modularity.
 */
func prepareBTVenmoAccountNonceResult(accountNonce: BTVenmoAccountNonce) -> NSDictionary {
    let result = NSMutableDictionary()
    
    // Core payment information
    result["nonce"] = accountNonce.nonce
    
    // Venmo specific profile data
    result["username"] = accountNonce.username ?? ""
    result["externalID"] = accountNonce.externalID ?? ""
    
    // Standard profile data
    result["email"] = accountNonce.email ?? ""
    result["phoneNumber"] = accountNonce.phoneNumber ?? ""
    result["firstName"] = accountNonce.firstName ?? ""
    result["lastName"] = accountNonce.lastName ?? ""
    
    // Address information (Available if requested in BTVenmoRequest)
    result["billingAddress"] = prepareAddressResult(address: accountNonce.billingAddress)
    result["shippingAddress"] = prepareAddressResult(address: accountNonce.shippingAddress)
    
    return result
}