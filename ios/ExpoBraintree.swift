//
//  BTPayPalVaultRequest.swift
//  expo-braintree
//
//  Created by Maciej Sasinowski on 28/04/2024.
//

import Braintree
import Foundation
import React

@objc(ExpoBraintree)
class ExpoBraintree: NSObject, BTThreeDSecureRequestDelegate {
  var threeDSecureClient: BTThreeDSecureClient? = nil
  
  @objc(requestBillingAgreement:withResolver:withRejecter:)
  func requestBillingAgreement(
    options: [String: String], resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    let clientToken = options["clientToken"] ?? ""
    // Step 1: Initialize Braintree API Client
    let apiClientOptional = BTAPIClient(authorization: clientToken)
    guard let apiClient = apiClientOptional else {
      return reject(
        EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
        ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue,
        NSError(domain: ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue, code: -1))
    }
    // Step 2: Initialize BPayPal API Client
    let payPalClient = BTPayPalClient(apiClient: apiClient)
    let vaultRequest = prepareBTPayPalVaultRequest(options: options)
    payPalClient.tokenize(vaultRequest) {
      (accountNonce, error) -> Void in
      if let accountNonce = accountNonce {
        // Step 3: Handle Success: Paypal Nonce Created resolved
        return resolve(
          prepareBTPayPalAccountNonceResult(
            accountNonce: accountNonce
          ))
      } else if let error = error as? BTPayPalError {
        // Step 3: Handle Error: Tokenize error
        switch error.errorCode {
        case BTPayPalError.disabled.errorCode:
          return reject(
            EXCEPTION_TYPES.PAYPAL_DISABLED_IN_CONFIGURATION.rawValue,
            ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.rawValue,
            NSError(
              domain: ERROR_TYPES.PAYPAL_DISABLED_IN_CONFIGURATION_ERROR.rawValue,
              code: BTPayPalError.disabled.errorCode)
          )
        case BTPayPalError.canceled.errorCode:
          return reject(
            EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.rawValue,
            ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.rawValue,
            NSError(
              domain: ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.rawValue,
              code: BTPayPalError.canceled.errorCode)
          )
        default:
          return reject(
            EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
            ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue,
            NSError(
              domain: error.localizedDescription,
              code: -1
            )
          )
        }
      }
    }
  }
  
  @objc(requestOneTimePayment:withResolver:withRejecter:)
  func requestOneTimePayment(
    options: [String: String], resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    let clientToken = options["clientToken"] ?? ""
    // Step 1: Initialize Braintree API Client
    let apiClientOptional = BTAPIClient(authorization: clientToken)
    guard let apiClient = apiClientOptional else {
      return reject(
        EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
        ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue,
        NSError(domain: ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue, code: -1))
    }
    // Step 2: Initialize BPayPal API Client
    let payPalClient = BTPayPalClient(apiClient: apiClient)
    let checkoutRequest = prepareBTPayPalCheckoutRequest(options: options)
    payPalClient.tokenize(checkoutRequest) {
      (accountNonce, error) -> Void in
      if let accountNonce = accountNonce {
        // Step 3: Handle Success: Paypal Nonce Created resolved
        return resolve(
          prepareBTPayPalAccountNonceResult(
            accountNonce: accountNonce
          ))
      } else if let error = error as? BTPayPalError {
        // Step 3: Handle Error: Tokenize error
        switch error.errorCode {
        case BTPayPalError.disabled.errorCode:
          return reject(
            EXCEPTION_TYPES.PAYPAL_DISABLED_IN_CONFIGURATION.rawValue,
            ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.rawValue,
            NSError(
              domain: ERROR_TYPES.PAYPAL_DISABLED_IN_CONFIGURATION_ERROR.rawValue,
              code: BTPayPalError.disabled.errorCode)
          )
        case BTPayPalError.canceled.errorCode:
          return reject(
            EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.rawValue,
            ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.rawValue,
            NSError(
              domain: ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.rawValue,
              code: BTPayPalError.canceled.errorCode)
          )
        default:
          return reject(
            EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
            ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue,
            NSError(
              domain: error.localizedDescription,
              code: -1
            )
          )
        }
      }
    }
  }
  
  @objc(getDeviceDataFromDataCollector:withResolver:withRejecter:)
  func getDeviceDataFromDataCollector(
    options: [String: String], resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    // Step 1: Initialize Braintree API Client
    let clientToken = options["clientToken"] ?? ""
    let apiClientOptional = BTAPIClient(authorization: clientToken)
    guard let apiClient = apiClientOptional else {
      return reject(
        EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
        ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue,
        NSError(domain: ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue, code: -1))
    }
    // Step 2: Initialize DataCollerctor
    let dataCollector = BTDataCollector(apiClient: apiClient)
    // Step 3: Try To Collect Device Data and make a corelation Id if that is possible
    dataCollector.collectDeviceData { corelationId, dataCollectorError in
      if let corelationId = corelationId {
        // Step 4: Return corelation id
        return resolve(corelationId)
      } else if let dataCollectorError = dataCollectorError {
        // Step 4: Handle Error: DataCollector error
        return reject(
          EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
          ERROR_TYPES.DATA_COLLECTOR_ERROR.rawValue,
          NSError(
            domain: ERROR_TYPES.DATA_COLLECTOR_ERROR.rawValue,
            code: -1)
        )
      }
    }
  }
  
  @objc(tokenizeCardData:withResolver:withRejecter:)
  func tokenizeCardData(
    options: [String: String], resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    let clientToken = options["clientToken"] ?? ""
    // Step 1: Initialize Braintree API Client
    let apiClientOptional = BTAPIClient(authorization: clientToken)
    guard let apiClient = apiClientOptional else {
      return reject(
        EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
        ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue,
        NSError(domain: ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue, code: -1))
    }
    // Step 2: Initialize DataCollerctor
    let cardClient = BTCardClient(apiClient: apiClient)
    let card = prepareCardData(options: options)
    // Step 3: Try To Collect Device Data and make a corelation Id if that is possible
    cardClient.tokenize(card) {
      (cardNonce, error) -> Void in
      if let cardNonce = cardNonce {
        // Step 4: Return corelation id
        return resolve(prepareBTCardNonceResult(cardNonce: cardNonce))
      } else if let error = error {
        // Step 4: Handle Error: DataCollector error
        return reject(
          EXCEPTION_TYPES.TOKENIZE_EXCEPTION.rawValue,
          ERROR_TYPES.CARD_TOKENIZATION_ERROR.rawValue,
          NSError(
            domain: ERROR_TYPES.CARD_TOKENIZATION_ERROR.rawValue,
            code: -1)
        )
      }
    }
  }
  
  @objc(requestVenmoNonce:withResolver:withRejecter:)
  func requestVenmoNonce(
    options: [String: String], resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    let clientToken = options["clientToken"] ?? ""
    
    // Step 1: Initialize Braintree API Client
    let apiClientOptional = BTAPIClient(authorization: clientToken)
    guard let apiClient = apiClientOptional else {
      return reject(
        EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
        ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue,
        NSError(domain: ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue, code: -1))
    }
    
    // Step 2: Initialize BTVenmoClient API Client
    let venmoClient = BTVenmoClient(apiClient: apiClient)
    let vaultRequest = prepareBTVenmoRequest(options: options)
    
    venmoClient.tokenize(vaultRequest) {
      (accountNonce, error) -> Void in
      if let accountNonce = accountNonce {
        
        // Step 3: Handle Success: Venmo Nonce Created resolved
        return resolve(
          prepareBTVenmoAccountNonceResult(
            accountNonce: accountNonce
          ))
      } else if let error = error as? BTVenmoError {
        // Step 3: Handle Error: Tokenize error
        switch error.errorCode {
        case BTVenmoError.disabled.errorCode:
          return reject(
            EXCEPTION_TYPES.VENMO_DISABLED_IN_CONFIGURATION.rawValue,
            ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.rawValue,
            NSError(
              domain: ERROR_TYPES.VENMO_DISABLED_IN_CONFIGURATION_ERROR.rawValue,
              code: BTPayPalError.disabled.errorCode)
          )
        case BTVenmoError.canceled.errorCode:
          return reject(
            EXCEPTION_TYPES.USER_CANCEL_EXCEPTION.rawValue,
            ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.rawValue,
            NSError(
              domain: ERROR_TYPES.USER_CANCEL_TRANSACTION_ERROR.rawValue,
              code: BTPayPalError.canceled.errorCode)
          )
        default:
          return reject(
            EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
            ERROR_TYPES.TOKENIZE_VAULT_PAYMENT_ERROR.rawValue,
            NSError(
              domain: error.localizedDescription,
              code: -1
            )
          )
        }
      }
    }
  }
  
  @objc(request3DSecurePaymentCheck:withResolver:withRejecter:)
  func request3DSecurePaymentCheck(
    options: [String: String], resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    let clientToken = options["clientToken"] ?? ""
    let nonce = options["nonce"] ?? ""
    let amount = options["amount"] ?? ""
    
    // Step 1: Initialize Braintree API Client
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
    guard let secureClient = self.threeDSecureClient else {
      return reject(
        EXCEPTION_TYPES.SWIFT_EXCEPTION.rawValue,
        ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue,
        NSError(domain: ERROR_TYPES.API_CLIENT_INITIALIZATION_ERROR.rawValue, code: -1))
    }
    
    let threeDSSecureRequest = prepare3DSecureData(options: options)
    threeDSSecureRequest.threeDSecureRequestDelegate = self
    
    secureClient.startPaymentFlow(threeDSSecureRequest) {
      (threeDSecureNonceOptional, error) -> Void in
      if let tokenizedCard = threeDSecureNonceOptional?.tokenizedCard {
        
        if tokenizedCard.threeDSecureInfo.liabilityShiftPossible && tokenizedCard.threeDSecureInfo.wasVerified {
          return reject(
            EXCEPTION_TYPES.TOKENIZE_EXCEPTION.rawValue,
            ERROR_TYPES.D_SECURE_NOT_ABLE_TO_SHIFT_LIABILITY.rawValue,
            NSError(
              domain: ERROR_TYPES.D_SECURE_NOT_ABLE_TO_SHIFT_LIABILITY.rawValue,
              code: -1)
          )
        }
        
        if tokenizedCard.threeDSecureInfo.liabilityShifted && tokenizedCard.threeDSecureInfo.wasVerified{
          return reject(
            EXCEPTION_TYPES.TOKENIZE_EXCEPTION.rawValue,
            ERROR_TYPES.D_SECURE_LIABILITY_NOT_SHIFTED.rawValue,
            NSError(
              domain: ERROR_TYPES.D_SECURE_LIABILITY_NOT_SHIFTED.rawValue,
              code: -1)
          )
        }
        
        if (tokenizedCard.nonce ?? "").isEmpty {
          return reject(
            EXCEPTION_TYPES.TOKENIZE_EXCEPTION.rawValue,
            ERROR_TYPES.PAYMENT_3D_SECURE_FAILED.rawValue,
            NSError(
              domain: ERROR_TYPES.PAYMENT_3D_SECURE_FAILED.rawValue,
              code: -1)
          )
        }
        
        return resolve(prepare3DSecureNonceResult(tokenizedCard:tokenizedCard))
      } else if let error = error {
        // Step 4: Handle Global Error
        return reject(
          EXCEPTION_TYPES.TOKENIZE_EXCEPTION.rawValue,
          error.localizedDescription,
          NSError(
            domain: ERROR_TYPES.D_SECURE_CARD_TOKENIZATION_ERROR.rawValue,
            code: -1)
        )
      }
    }
  }
  
  //  Function needed for BTThreeDSecureRequestDelegate
  func onLookupComplete(
    _ request: BTThreeDSecureRequest,
    lookupResult: BTThreeDSecureResult,
    next: @escaping () -> Void
  ) {
    next()
  }
}
