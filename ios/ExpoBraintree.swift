import Braintree
import Foundation
import React
import PassKit

@objc(ExpoBraintree)
class ExpoBraintree: NSObject, BTThreeDSecureRequestDelegate, PKPaymentAuthorizationViewControllerDelegate {
    
    var threeDSecureClient: BTThreeDSecureClient? = nil
    // Properties to store Apple Pay state
    private var applePayResolve: RCTPromiseResolveBlock? = nil
    private var applePayReject: RCTPromiseRejectBlock? = nil
    private var applePayApiClient: BTAPIClient? = nil
    
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
        let clientToken = options["clientToken"] ?? ""
        let appLinkString = options["merchantAppLink"] ?? ""

        guard let universalLinkURL = URL(string: appLinkString) else {
            reject(
                EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
                "Venmo v7: Invalid or missing merchantAppLink",
                nil
            )
            return
        }

        let venmoClient = BTVenmoClient(authorization: clientToken, universalLink: universalLinkURL)
        let venmoRequest = prepareBTVenmoRequest(options: options)

        venmoClient.tokenize(venmoRequest) { (accountNonce, error) in
            if let error = error {
                self.handleVenmoError(error as NSError, reject: reject)
                return
            }
            
            if let accountNonce = accountNonce {
                resolve([
                    "nonce": accountNonce.nonce,
                    "type": "Venmo",
                    "username": accountNonce.username ?? "",
                    "email": accountNonce.email ?? ""
                ])
            } else {
                reject(
                    EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
                    ERROR_TYPES.VENMO_DISABLED_IN_CONFIGURATION_ERROR.rawValue,
                    nil
                )
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
    
  // MARK: - Apple Pay
      @objc(requestApplePay:withResolver:withRejecter:)
      func requestApplePay(
          options: [String: Any], resolve: @escaping RCTPromiseResolveBlock,
          reject: @escaping RCTPromiseRejectBlock
      ) {
          let clientToken = options["clientToken"] as? String ?? ""
          
          guard let apiClient = BTAPIClient(authorization: clientToken) else {
              return reject(
                  EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
                  ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue,
                  nil)
          }
          
          self.applePayApiClient = apiClient
          self.applePayResolve = resolve
          self.applePayReject = reject
          
          let applePayClient = BTApplePayClient(apiClient: apiClient)
          
          // FIXED: Method name in v6 is 'paymentRequest', not 'makePaymentRequest'
          applePayClient.makePaymentRequest() { (paymentRequest, error) in
              guard let paymentRequest = paymentRequest else {
                  self.cleanupApplePay()
                  reject(EXCEPTION_TYPES.APPLE_PAY_EXCEPTION.rawValue, error?.localizedDescription ?? "Cannot create Payment Request", error)
                  return
              }
              
              paymentRequest.merchantIdentifier = options["merchantId"] as? String ?? ""
              paymentRequest.countryCode = options["countryCode"] as? String ?? "PL"
              paymentRequest.currencyCode = options["currencyCode"] as? String ?? "PLN"
              paymentRequest.merchantCapabilities = .capability3DS
              
              let amount = options["amount"] as? String ?? "0.00"
              let companyName = options["companyName"] as? String ?? "Total"
              paymentRequest.paymentSummaryItems = [
                  PKPaymentSummaryItem(label: companyName, amount: NSDecimalNumber(string: amount))
              ]
              
              DispatchQueue.main.async {
                  guard let vc = PKPaymentAuthorizationViewController(paymentRequest: paymentRequest) else {
                      self.cleanupApplePay()
                      reject(EXCEPTION_TYPES.APPLE_PAY_EXCEPTION.rawValue, ERROR_TYPES.APPLE_PAY_NOT_AVAILABLE.rawValue, nil)
                      return
                  }
                  vc.delegate = self
                  
                  // Modern way to find the root view controller (avoids keyWindow deprecation)
                  let connectedScenes = UIApplication.shared.connectedScenes
                      .filter { $0.activationState == .foregroundActive }
                      .compactMap { $0 as? UIWindowScene }
                  
                  let rootVC = connectedScenes.first?.windows.first(where: { $0.isKeyWindow })?.rootViewController

                  if let rootVC = rootVC {
                      rootVC.present(vc, animated: true)
                  } else {
                      self.cleanupApplePay()
                      reject(EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue, "Could not find Root ViewController", nil)
                  }
              }
          }
      }

      // MARK: - PKPaymentAuthorizationViewControllerDelegate
      func paymentAuthorizationViewController(_ controller: PKPaymentAuthorizationViewController, didAuthorizePayment payment: PKPayment, handler completion: @escaping (PKPaymentAuthorizationResult) -> Void) {
          
          guard let apiClient = self.applePayApiClient else {
              completion(PKPaymentAuthorizationResult(status: .failure, errors: nil))
              return
          }
          
          let applePayClient = BTApplePayClient(apiClient: apiClient)
          
          // FIXED: Method name in v6 is 'tokenizeApplePayPayment', not 'tokenize'
          applePayClient.tokenize(payment) { (nonce, error) in
              if let error = error {
                  self.applePayReject?(
                      EXCEPTION_TYPES.APPLE_PAY_EXCEPTION.rawValue,
                      ERROR_TYPES.APPLE_PAY_TOKENIZATION_ERROR.rawValue,
                      error
                  )
                  completion(PKPaymentAuthorizationResult(status: .failure, errors: [error]))
              } else if let nonce = nonce {
                  self.applePayResolve?(self.prepareBTApplePayNonceResult(nonce: nonce))
                  completion(PKPaymentAuthorizationResult(status: .success, errors: nil))
                  
                  self.applePayResolve = nil
                  self.applePayReject = nil
              }
          }
      }

    func paymentAuthorizationViewControllerDidFinish(_ controller: PKPaymentAuthorizationViewController) {
        controller.dismiss(animated: true) {
            // If resolve still exists, it means user cancelled the sheet manually
            if self.applePayResolve != nil {
                self.applePayReject?(
                    EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.rawValue,
                    ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.rawValue,
                    nil
                )
            }
            self.cleanupApplePay()
        }
    }

    private func prepareBTApplePayNonceResult(nonce: BTApplePayCardNonce) -> [String: Any] {
        return [
            "nonce": nonce.nonce,
            "type": "ApplePay",
            "isDefault": nonce.isDefault
        ]
    }
    
    private func cleanupApplePay() {
        self.applePayResolve = nil
        self.applePayReject = nil
    }

}
