import Braintree
import Foundation

func prepareBTVenmoRequest(options: [String: String]) -> BTVenmoRequest {
    let usageStr = options["paymentMethodUsage"]
    let usage: BTVenmoPaymentMethodUsage = (usageStr == "singleUse") ? .singleUse : .multiUse
    
    // All options from JS passed directly into the v7 initializer
    let venmoRequest = BTVenmoRequest(
        paymentMethodUsage: usage,
        profileID: options["profileID"],
        vault: getBoolValueByString(value: options["vault"], defaultValue: false),
        displayName: options["displayName"],
        collectCustomerBillingAddress: getBoolValueByString(value: options["collectCustomerBillingAddress"], defaultValue: false),
        collectCustomerShippingAddress: getBoolValueByString(value: options["collectCustomerShippingAddress"], defaultValue: false),
        isFinalAmount: getBoolValueByString(value: options["isFinalAmount"], defaultValue: false),
        subTotalAmount: options["subTotalAmount"],
        discountAmount: options["discountAmount"],
        taxAmount: options["taxAmount"],
        shippingAmount: options["shippingAmount"],
        totalAmount: options["totalAmount"]
    )

    return venmoRequest
}