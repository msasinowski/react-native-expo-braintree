import Braintree
import Foundation
import NitroModules

extension NSDictionary {
    func toSwiftDict() -> Dictionary<String, String> {
        var result: [String: String] = [:]
        for (key, value) in self {
            guard let key = key as? String else { continue }
            if let stringValue = value as? String {
                result[key] = stringValue
            } else if let boolValue = value as? Bool {
                result[key] = boolValue ? "true" : "false"
            } else if let data = try? JSONSerialization.data(withJSONObject: value),
                      let json = String(data: data, encoding: .utf8) {
                result[key] = json
            }
        }
        return result
    }
}

class ExpoBraintree: HybridExpoBraintreeSpec {
    var threeDSecureClient: BTThreeDSecureClient? = nil

    func requestBillingAgreement(options: Dictionary<String, String>) throws -> Promise<Dictionary<String, String>> {
        let promise = Promise<Dictionary<String, String>>()
        let clientToken = options["clientToken"] ?? ""
        let payPalClient = BTPayPalClient(authorization: clientToken)
        let vaultRequest = prepareBTPayPalVaultRequest(options: options)
        payPalClient.tokenize(vaultRequest) { accountNonce, error in
            if let accountNonce = accountNonce {
                promise.resolve(withResult: prepareBTPayPalAccountNonceResult(accountNonce: accountNonce).toSwiftDict())
            } else {
                promise.resolve(withResult: ["error": "true", "code": EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue, "message": ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue, "domain": ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue, "nativeError": error?.localizedDescription ?? "Failed to tokenize PayPal billing agreement"])
            }
        }
        return promise
    }

    func requestOneTimePayment(options: Dictionary<String, String>) throws -> Promise<Dictionary<String, String>> {
        let promise = Promise<Dictionary<String, String>>()
        let clientToken = options["clientToken"] ?? ""
        let payPalClient = BTPayPalClient(authorization: clientToken)
        let checkoutRequest = prepareBTPayPalCheckoutRequest(options: options)
        payPalClient.tokenize(checkoutRequest) { accountNonce, error in
            if let accountNonce = accountNonce {
                promise.resolve(withResult: prepareBTPayPalAccountNonceResult(accountNonce: accountNonce).toSwiftDict())
            } else {
                promise.resolve(withResult: ["error": "true", "code": EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue, "message": ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue, "domain": ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue, "nativeError": error?.localizedDescription ?? "Failed to tokenize PayPal one-time payment"])
            }
        }
        return promise
    }

    func tokenizeCardData(options: Dictionary<String, String>) throws -> Promise<Dictionary<String, String>> {
        let promise = Promise<Dictionary<String, String>>()
        let clientToken = options["clientToken"] ?? ""
        let cardClient = BTCardClient(authorization: clientToken)
        let card = prepareCardData(options: options)
        cardClient.tokenize(card) { cardNonce, error in
            if let cardNonce = cardNonce {
                promise.resolve(withResult: prepareBTCardNonceResult(cardNonce: cardNonce).toSwiftDict())
            } else {
                promise.resolve(withResult: ["error": "true", "code": EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue, "message": ERROR_TYPES.CARD_TOKENIZATION_ERROR.rawValue, "domain": ERROR_TYPES.CARD_TOKENIZATION_ERROR.rawValue, "nativeError": error?.localizedDescription ?? "Failed to tokenize card"])
            }
        }
        return promise
    }

    func getDeviceDataFromDataCollector(options: Dictionary<String, String>) throws -> Promise<Dictionary<String, String>> {
        let promise = Promise<Dictionary<String, String>>()
        let clientToken = options["clientToken"] ?? ""
        let dataCollector = BTDataCollector(authorization: clientToken)
        dataCollector.collectDeviceData { deviceData, error in
            if let error = error {
                promise.resolve(withResult: ["error": "true", "code": EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue, "message": ERROR_TYPES.DATA_COLLECTOR_ERROR.rawValue, "domain": ERROR_TYPES.DATA_COLLECTOR_ERROR.rawValue, "nativeError": error.localizedDescription])
            } else if let deviceData = deviceData {
                promise.resolve(withResult: ["data": deviceData])
            } else {
                promise.resolve(withResult: ["error": "true", "code": EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue, "message": ERROR_TYPES.DATA_COLLECTOR_ERROR.rawValue, "domain": ERROR_TYPES.DATA_COLLECTOR_ERROR.rawValue, "nativeError": "Failed to collect device data"])
            }
        }
        return promise
    }

    func requestVenmoNonce(options: Dictionary<String, String>) throws -> Promise<Dictionary<String, String>> {
        let promise = Promise<Dictionary<String, String>>()
        let clientToken = options["clientToken"] ?? ""
        let appLinkString = options["merchantAppLink"] ?? ""
        guard let universalLinkURL = URL(string: appLinkString) else {
            promise.resolve(withResult: ["error": "true", "code": EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue, "message": ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue, "domain": ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue, "nativeError": "Invalid or missing merchantAppLink"])
            return promise
        }
        let venmoClient = BTVenmoClient(authorization: clientToken, universalLink: universalLinkURL)
        let venmoRequest = prepareBTVenmoRequest(options: options)
        venmoClient.tokenize(venmoRequest) { accountNonce, error in
            if let error = error {
                promise.resolve(withResult: ["error": "true", "code": EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue, "message": ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue, "domain": ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue, "nativeError": error.localizedDescription])
                return
            }
            if let accountNonce = accountNonce {
                promise.resolve(withResult: prepareBTVenmoAccountNonceResult(accountNonce: accountNonce).toSwiftDict())
            } else {
                promise.resolve(withResult: ["error": "true", "code": EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue, "message": ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue, "domain": ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue, "nativeError": "Venmo tokenization returned nil without error"])
            }
        }
        return promise
    }

    func request3DSecurePaymentCheck(options: Dictionary<String, String>) throws -> Promise<Dictionary<String, String>> {
        let promise = Promise<Dictionary<String, String>>()
        let clientToken = options["clientToken"] ?? ""
        let nonce = options["nonce"] ?? ""
        let amount = options["amount"] ?? ""
        if amount.isEmpty || nonce.isEmpty {
            promise.resolve(withResult: ["error": "true", "code": EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue, "message": ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue, "domain": ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue, "nativeError": "Invalid 3DS parameters: amount and nonce are required"])
            return promise
        }
        self.threeDSecureClient = BTThreeDSecureClient(authorization: clientToken)
        let threeDSRequest = prepare3DSecureData(options: options)
        threeDSRequest.threeDSecureRequestDelegate = self
        DispatchQueue.main.async { [self] in
            self.threeDSecureClient?.start(threeDSRequest) { result, error in
                if let tokenizedCard = result?.tokenizedCard {
                    if tokenizedCard.threeDSecureInfo.liabilityShifted {
                        promise.resolve(withResult: prepare3DSecureNonceResult(tokenizedCard: tokenizedCard).toSwiftDict())
                    } else {
                        promise.resolve(withResult: ["error": "true", "code": EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue, "message": ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue, "domain": ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue, "nativeError": "Liability not shifted"])
                    }
                } else {
                    promise.resolve(withResult: ["error": "true", "code": EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue, "message": ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue, "domain": ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue, "nativeError": error?.localizedDescription ?? "3DS verification failed"])
                }
            }
        }
        return promise
    }

    func requestGooglePayPayment(options: Dictionary<String, String>) throws -> Promise<Dictionary<String, String>> {
        return Promise.resolved(withResult: ["error": "true", "code": EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue, "message": ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue, "domain": ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue, "nativeError": "Google Pay is not supported on iOS"])
    }

}

extension ExpoBraintree: BTThreeDSecureRequestDelegate {
    func onLookupComplete(_ request: BTThreeDSecureRequest, lookupResult: BTThreeDSecureResult, next: @escaping () -> Void) {
        next()
    }
}
