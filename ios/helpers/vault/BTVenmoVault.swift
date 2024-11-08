
import Braintree
import Foundation

let keyPathForVenmoRequest: [String: WritableKeyPath<BTVenmoRequest, String?>] = [
    "profileID": \.profileID,
    "displayName": \.displayName,
    "subTotalAmount": \.subTotalAmount,
    "discountAmount": \.discountAmount,
    "taxAmount": \.taxAmount,
    "shippingAmount": \.shippingAmount,
    "totalAmount": \.totalAmount,
]


func setStringOptionIfNotEmpty(vaultRequest: inout BTVenmoRequest, options: [String: String], optionsName: String) {
  let optionValue = options[optionsName]
  if !(optionValue ?? "").isEmpty {
    if let keyPath = keyPathForVenmoRequest[optionsName] {
        vaultRequest[keyPath: keyPath] = optionValue
    }
  }
}

func prepareBTVenmoRequest(options: [String: String]) -> BTVenmoRequest {
  let paymentMethodUsage = options["paymentMethodUsage"]
  var vaultRequest = BTVenmoRequest(paymentMethodUsage: getPaymentMethodUsageByString(paymentMethodUsage: paymentMethodUsage))

  setStringOptionIfNotEmpty(vaultRequest: &vaultRequest, options: options, optionsName: "profileID")
  setStringOptionIfNotEmpty(vaultRequest: &vaultRequest, options: options, optionsName: "displayName")
  setStringOptionIfNotEmpty(vaultRequest: &vaultRequest, options: options, optionsName: "subTotalAmount")
  setStringOptionIfNotEmpty(vaultRequest: &vaultRequest, options: options, optionsName: "discountAmount")
  setStringOptionIfNotEmpty(vaultRequest: &vaultRequest, options: options, optionsName: "taxAmount")
  setStringOptionIfNotEmpty(vaultRequest: &vaultRequest, options: options, optionsName: "shippingAmount")
  setStringOptionIfNotEmpty(vaultRequest: &vaultRequest, options: options, optionsName: "totalAmount")

  vaultRequest.vault = getBoolValueByString(
    value: options["vault"], defaultValue: false)
  vaultRequest.collectCustomerBillingAddress = getBoolValueByString(
    value: options["collectCustomerBillingAddress"], defaultValue: false)
  vaultRequest.collectCustomerShippingAddress = getBoolValueByString(
    value: options["collectCustomerShippingAddress"], defaultValue: false)
  vaultRequest.isFinalAmount = getBoolValueByString(
    value: options["isFinalAmount"], defaultValue: false)
  vaultRequest.fallbackToWeb = getBoolValueByString(
    value: options["fallbackToWeb"], defaultValue: false)

  return vaultRequest
}
