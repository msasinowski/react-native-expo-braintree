import Braintree
import Foundation
import React

enum EXCEPTION_TYPES: String {
  case SWIFT_EXCEPTION = "ReactNativePaypalReborn:`SwiftException"
}

enum ERROR_TYPES: String {
  case API_CLIENT_INITIALIZATION_ERROR = "API_CLIENT_INITIALIZATION_ERROR"
  case TOKENIZE_VAULT_PAYMENT_ERROR = "TOKENIZE_VAULT_PAYMENT_ERROR"
}

@objc(PaypalReborn)
class PaypalReborn: NSObject {

  @objc(multiply:withB:withResolver:withRejecter:)
  func multiply(a: Float, b: Float, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock)
  {
    resolve(a * b)
  }

  @objc(requestBillingAgreement:withResolver:withRejecter:)
  func requestBillingAgreement(
    options: [String: String], resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    // sandbox_9dbg82cq_dcpspy2brwdjr3qn
    let clientToken = options["clientToken"] ?? ""
    let billingAgreementDescription = options["billingAgreementDescription"] ?? ""

    let apiClientOptional = BTAPIClient(authorization: clientToken)
    guard let apiClient = apiClientOptional else {
      return reject(
        EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
        ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue,
        NSError(domain: ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue, code: -1))
    }
    let payPalClient = BTPayPalClient(apiClient: apiClient)

    let vaultRequest = BTPayPalVaultRequest()
    vaultRequest.billingAgreementDescription = billingAgreementDescription

    payPalClient.tokenize(
      vaultRequest
    ) { nonce, error in
      guard let nonce else {
        return reject(
          EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
          ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue,
          NSError(
            domain: error?.localizedDescription
              ?? ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue, code: -1)
        )

      }
      resolve(
        nonce
      )
    }
  }
}
