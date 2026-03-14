import Braintree
import Foundation
import React

@objc(ExpoBraintree)
class ExpoBraintree: NSObject, BTThreeDSecureRequestDelegate {
    
    // 3DS Client must be a class property to prevent deallocation during the process
    var threeDSecureClient: BTThreeDSecureClient? = nil
    
    // MARK: - PayPal Billing Agreement (Vault)
    @objc(requestBillingAgreement:withResolver:withRejecter:)
    func requestBillingAgreement(
        options: [String: String], resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock
    ) {
        let clientToken = options["clientToken"] ?? ""
        let apiClientOptional = BTAPIClient(authorization: clientToken)
        
        guard let apiClient = apiClientOptional else {
            return reject(
                EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
                ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue,
                NSError(domain: ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue, code: -1))
        }
        
        let payPalClient = BTPayPalClient(apiClient: apiClient)
        let vaultRequest = prepareBTPayPalVaultRequest(options: options)
        
        payPalClient.tokenize(vaultRequest) { (accountNonce, error) in
            if let accountNonce = accountNonce {
                return resolve(prepareBTPayPalAccountNonceResult(accountNonce: accountNonce))
            } else if let error = error as NSError? {
                self.handlePayPalError(error, reject: reject)
            }
        }
    }
    
    // MARK: - PayPal One Time Payment (Checkout)
    @objc(requestOneTimePayment:withResolver:withRejecter:)
    func requestOneTimePayment(
        options: [String: String], resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock
    ) {
        let clientToken = options["clientToken"] ?? ""
        let apiClientOptional = BTAPIClient(authorization: clientToken)
        
        guard let apiClient = apiClientOptional else {
            return reject(
                EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
                ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue,
                NSError(domain: ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue, code: -1))
        }
        
        let payPalClient = BTPayPalClient(apiClient: apiClient)
        let checkoutRequest = prepareBTPayPalCheckoutRequest(options: options)
        
        payPalClient.tokenize(checkoutRequest) { (accountNonce, error) in
            if let accountNonce = accountNonce {
                return resolve(prepareBTPayPalAccountNonceResult(accountNonce: accountNonce))
            } else if let error = error as NSError? {
                self.handlePayPalError(error, reject: reject)
            }
        }
    }
    
    // MARK: - Data Collector
    @objc(getDeviceDataFromDataCollector:withResolver:withRejecter:)
    func getDeviceDataFromDataCollector(
        options: [String: String], resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock
    ) {
        let clientToken = options["clientToken"] ?? ""
        let apiClientOptional = BTAPIClient(authorization: clientToken)
        guard let apiClient = apiClientOptional else {
            return reject(
                EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
                ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue,
                NSError(domain: ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue, code: -1))
        }
        
        let dataCollector = BTDataCollector(apiClient: apiClient)
        // Fixed: Added second parameter (error) to completion handler
        dataCollector.collectDeviceData { (deviceData, error) in
            if let error = error {
                return reject(
                    EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
                    error.localizedDescription,
                    error)
            }
            
            if let deviceData = deviceData {
                return resolve(deviceData)
            } else {
                return reject(
                    EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
                    ERROR_TYPES.DATA_COLLECTOR_ERROR.rawValue,
                    NSError(domain: ERROR_TYPES.DATA_COLLECTOR_ERROR.rawValue, code: -1))
            }
        }
    }
    
    // MARK: - Card Tokenization
    @objc(tokenizeCardData:withResolver:withRejecter:)
    func tokenizeCardData(
        options: [String: String], resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock
    ) {
        let clientToken = options["clientToken"] ?? ""
        let apiClientOptional = BTAPIClient(authorization: clientToken)
        
        guard let apiClient = apiClientOptional else {
            return reject(
                EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
                ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue,
                NSError(domain: ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue, code: -1))
        }
        
        let cardClient = BTCardClient(apiClient: apiClient)
        let card = prepareCardData(options: options)
        
        cardClient.tokenize(card) { (cardNonce, error) in
            if let cardNonce = cardNonce {
                return resolve(prepareBTCardNonceResult(cardNonce: cardNonce))
            } else {
                return reject(
                    EXCEPTION_TYPES.TOKENIZE_EXCEPTION.rawValue,
                    ERROR_TYPES.CARD_TOKENIZATION_ERROR.rawValue,
                    error)
            }
        }
    }
    
    // MARK: - Venmo
    @objc(requestVenmoNonce:withResolver:withRejecter:)
    func requestVenmoNonce(
        options: [String: String], resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock
    ) {
        let clientToken = options["clientToken"] ?? ""
        let apiClientOptional = BTAPIClient(authorization: clientToken)
        
        guard let apiClient = apiClientOptional else {
            return reject(
                EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
                ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue,
                NSError(domain: ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue, code: -1))
        }
        
        let venmoClient = BTVenmoClient(apiClient: apiClient)
        let venmoRequest = prepareBTVenmoRequest(options: options)
        
        venmoClient.tokenize(venmoRequest) { (accountNonce, error) in
            if let accountNonce = accountNonce {
                return resolve(prepareBTVenmoAccountNonceResult(accountNonce: accountNonce))
            } else if let error = error as NSError? {
                self.handleVenmoError(error, reject: reject)
            }
        }
    }
    
    // MARK: - 3D Secure Verification
    @objc(request3DSecurePaymentCheck:withResolver:withRejecter:)
    func request3DSecurePaymentCheck(
        options: [String: String], resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock
    ) {
        let clientToken = options["clientToken"] ?? ""
        let nonce = options["nonce"] ?? ""
        let amount = options["amount"] ?? ""
        
        let apiClientOptional = BTAPIClient(authorization: clientToken)
        guard let apiClient = apiClientOptional else {
            return reject(
                EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
                ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue,
                NSError(domain: ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue, code: -1))
        }
        
        if amount.isEmpty || nonce.isEmpty {
            return reject(
                EXCEPTION_TYPES.TOKENIZE_EXCEPTION.rawValue,
                ERROR_TYPES.D_SECURE_CARD_TOKENIZATION_VALIDATION_ERROR.rawValue,
                NSError(domain: ERROR_TYPES.D_SECURE_CARD_TOKENIZATION_VALIDATION_ERROR.rawValue, code: -1))
        }
        
        self.threeDSecureClient = BTThreeDSecureClient(apiClient: apiClient)
        
        let threeDSSecureRequest = prepare3DSecureData(options: options)
        threeDSSecureRequest.threeDSecureRequestDelegate = self
        
        // Running on Main Thread prevents White Screen issues
        DispatchQueue.main.async {
            self.threeDSecureClient?.startPaymentFlow(threeDSSecureRequest) { (threeDSecureResult, error) in
                if let tokenizedCard = threeDSecureResult?.tokenizedCard {
                    
                    // Strict Security Logic: Only allow if liability shift occurred
                    if tokenizedCard.threeDSecureInfo.liabilityShifted {
                        return resolve(prepare3DSecureNonceResult(tokenizedCard: tokenizedCard))
                    } else {
                        // Reject all other cases (3DS failed, not supported, or tech error)
                        return reject(
                            EXCEPTION_TYPES.TOKENIZE_EXCEPTION.rawValue,
                            ERROR_TYPES.D_SECURE_LIABILITY_NOT_SHIFTED.rawValue,
                            nil
                        )
                    }
                    
                } else if let error = error {
                    return reject(
                        EXCEPTION_TYPES.TOKENIZE_EXCEPTION.rawValue,
                        error.localizedDescription,
                        error
                    )
                }
            }
        }
    }
    
    // MARK: - BTThreeDSecureRequestDelegate
    func onLookupComplete(_ request: BTThreeDSecureRequest, lookupResult: BTThreeDSecureResult, next: @escaping () -> Void) {
        next()
    }
    
    // MARK: - Error Helpers
    private func handlePayPalError(_ error: NSError, reject: @escaping RCTPromiseRejectBlock) {
        if error.domain == BTPayPalError.errorDomain {
            if let payPalError = error as? BTPayPalError {
                switch payPalError {
                case .disabled:
                    return reject(EXCEPTION_TYPES.PAYPAL_DISABLED_IN_CONFIGURATION.rawValue, ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.rawValue, error)
                case .canceled:
                    return reject(EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.rawValue, ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.rawValue, error)
                default: break
                }
            }
        }
        reject(EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue, error.localizedDescription, error)
    }

    private func handleVenmoError(_ error: NSError, reject: @escaping RCTPromiseRejectBlock) {
        if error.domain == BTVenmoError.errorDomain {
            if let venmoError = error as? BTVenmoError {
                switch venmoError {
                case .disabled:
                    return reject(EXCEPTION_TYPES.VENMO_DISABLED_IN_CONFIGURATION.rawValue, ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.rawValue, error)
                case .canceled:
                    return reject(EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.rawValue, ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.rawValue, error)
                default: break
                }
            }
        }
        reject(EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue, error.localizedDescription, error)
    }
}