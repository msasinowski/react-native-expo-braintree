import Braintree
import Foundation
import React

@objc(ExpoBraintree)
class ExpoBraintree: NSObject, BTThreeDSecureRequestDelegate {
    
    var threeDSecureClient: BTThreeDSecureClient? = nil
    
    // MARK: - PayPal Billing Agreement (Vault)
    @objc(requestBillingAgreement:withResolver:withRejecter:)
    func requestBillingAgreement(
        options: [String: String], resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock
    ) {
        let clientToken = options["clientToken"] ?? ""
        let payPalClient = BTPayPalClient(authorization: clientToken)
        let vaultRequest = prepareBTPayPalVaultRequest(options: options)
        
        payPalClient.tokenize(vaultRequest) { (accountNonce, error) in
            if let accountNonce = accountNonce {
                resolve(prepareBTPayPalAccountNonceResult(accountNonce: accountNonce))
            } else {
                self.handlePayPalError(error as NSError?, reject: reject)
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
        let payPalClient = BTPayPalClient(authorization: clientToken)
        let checkoutRequest = prepareBTPayPalCheckoutRequest(options: options)
        
        payPalClient.tokenize(checkoutRequest) { (accountNonce, error) in
            if let accountNonce = accountNonce {
                resolve(prepareBTPayPalAccountNonceResult(accountNonce: accountNonce))
            } else {
                self.handlePayPalError(error as NSError?, reject: reject)
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
        let dataCollector = BTDataCollector(authorization: clientToken)
        
        dataCollector.collectDeviceData { (deviceData, error) in
            if let error = error {
                reject(EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue, ERROR_TYPES.DATA_COLLECTOR_ERROR.rawValue, error)
            } else if let deviceData = deviceData {
                resolve(deviceData)
            } else {
                reject(EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue, ERROR_TYPES.DATA_COLLECTOR_ERROR.rawValue, nil)
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
        let cardClient = BTCardClient(authorization: clientToken)
        let card = prepareCardData(options: options)
        
        cardClient.tokenize(card) { (cardNonce, error) in
            if let cardNonce = cardNonce {
                resolve(prepareBTCardNonceResult(cardNonce: cardNonce))
            } else {
                reject(EXCEPTION_TYPES.TOKENIZE_EXCEPTION.rawValue, ERROR_TYPES.CARD_TOKENIZATION_ERROR.rawValue, error)
            }
        }
    }
    
   @objc(requestVenmoNonce:withResolver:withRejecter:)
func requestVenmoNonce(
    options: [String: String], 
    resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
) {
    NSLog("[BraintreeVenmo] >>> START: requestVenmoNonce")
    
    let clientToken = options["clientToken"] ?? ""
    let appLinkString = options["merchantAppLink"] ?? ""
    
    NSLog("[BraintreeVenmo] ClientToken length: %ld", clientToken.count)
    NSLog("[BraintreeVenmo] MerchantAppLink: %@", appLinkString)

    // 1. Walidacja Universal Link (Wymagany w v7)
    guard let universalLinkURL = URL(string: appLinkString) else {
        NSLog("[BraintreeVenmo] !!! BŁĄD: Niepoprawny URL: %@", appLinkString)
        reject("ERR", "Venmo v7: Invalid or missing merchantAppLink", nil)
        return
    }

    // 2. Inicjalizacja BTVenmoClient
    // W v7 używamy: authorization (String) oraz universalLink (URL)
    let venmoClient = BTVenmoClient(authorization: clientToken, universalLink: universalLinkURL)
    NSLog("[BraintreeVenmo] BTVenmoClient zainicjalizowany")

    // 3. Przygotowanie BTVenmoRequest
    // Metoda canLaunchVenmoApp() NIE ISTNIEJE w v7 - usuwamy ją.
    let venmoRequest = prepareBTVenmoRequest(options: options)

    // 4. Proces Tokenizacji
    NSLog("[BraintreeVenmo] Wywołuję venmoClient.tokenize...")
    
    venmoClient.tokenize(venmoRequest) { (accountNonce, error) in
        if let error = error {
            let nsError = error as NSError
            
            NSLog("[BraintreeVenmo] !!! BŁĄD TOKENIZACJI")
            NSLog("[BraintreeVenmo] Description: %@", nsError.localizedDescription)
            NSLog("[BraintreeVenmo] Domain: %@", nsError.domain)
            NSLog("[BraintreeVenmo] Code: %ld", Int(nsError.code))
            
            // Logowanie błędów systemowych (np. problem z Universal Link)
            if let underlyingError = nsError.userInfo[NSUnderlyingErrorKey] as? NSError {
                NSLog("[BraintreeVenmo] Underlying Error: %@", underlyingError.localizedDescription)
                NSLog("[BraintreeVenmo] Underlying Domain: %@", underlyingError.domain)
            }
            
            self.handleVenmoError(nsError, reject: reject)
            return
        }
        
        if let accountNonce = accountNonce {
            NSLog("[BraintreeVenmo] SUCCESS: Nonce otrzymany: %@", accountNonce.nonce)
            resolve([
                "nonce": accountNonce.nonce,
                "type": "Venmo",
                "username": accountNonce.username ?? "",
                "email": accountNonce.email ?? ""
            ])
        } else {
            NSLog("[BraintreeVenmo] !!! BŁĄD: Brak danych (Unexpected)")
            reject("ERR", "Unknown Venmo error - no nonce received", nil)
        }
    }
}
    
    // MARK: - 3D Secure
    @objc(request3DSecurePaymentCheck:withResolver:withRejecter:)
    func request3DSecurePaymentCheck(
        options: [String: String], resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock
    ) {
        let clientToken = options["clientToken"] ?? ""
        let nonce = options["nonce"] ?? ""
        let amount = options["amount"] ?? ""
        
        if amount.isEmpty || nonce.isEmpty {
            return reject(EXCEPTION_TYPES.TOKENIZE_EXCEPTION.rawValue, ERROR_TYPES.D_SECURE_CARD_TOKENIZATION_VALIDATION_ERROR.rawValue, nil)
        }
        
        self.threeDSecureClient = BTThreeDSecureClient(authorization: clientToken)
        let threeDSRequest = prepare3DSecureData(options: options)
        threeDSRequest.threeDSecureRequestDelegate = self
        
        DispatchQueue.main.async {
            // FIX for v7: method is renamed from 'startPaymentFlow' to 'start'
            self.threeDSecureClient?.start(threeDSRequest) { (result, error) in
                if let tokenizedCard = result?.tokenizedCard {
                    if tokenizedCard.threeDSecureInfo.liabilityShifted {
                        resolve(prepare3DSecureNonceResult(tokenizedCard: tokenizedCard))
                    } else {
                        reject(EXCEPTION_TYPES.TOKENIZE_EXCEPTION.rawValue, ERROR_TYPES.D_SECURE_LIABILITY_NOT_SHIFTED.rawValue, nil)
                    }
                } else {
                    reject(EXCEPTION_TYPES.TOKENIZE_EXCEPTION.rawValue, ERROR_TYPES.PAYMENT_3D_SECURE_FAILED.rawValue, error)
                }
            }
        }
    }

    // MARK: - BTThreeDSecureRequestDelegate
    func onLookupComplete(_ request: BTThreeDSecureRequest, lookupResult: BTThreeDSecureResult, next: @escaping () -> Void) {
        next()
    }
    
    // MARK: - Error Handling
    private func handlePayPalError(_ error: NSError?, reject: @escaping RCTPromiseRejectBlock) {
        guard let error = error else {
            reject(EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue, "Unknown PayPal Error", nil)
            return
        }
        
        // FIX for v7: Use .errorCode comparison or check against static errorDomain
        if error.domain == BTPayPalError.errorDomain && error.code == BTPayPalError.canceled.errorCode {
            return reject(EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.rawValue, ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.rawValue, error)
        }
        
        reject(EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue, error.localizedDescription, error)
    }

    private func handleVenmoError(_ error: NSError?, reject: @escaping RCTPromiseRejectBlock) {
        guard let error = error else {
            reject(EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue, "Unknown Venmo Error", nil)
            return
        }
        
        // FIX for v7: Use .errorCode comparison
        if error.domain == BTVenmoError.errorDomain && error.code == BTVenmoError.canceled.errorCode {
            return reject(EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.rawValue, ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.rawValue, error)
        }
        
        reject(EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue, error.localizedDescription, error)
    }
}
