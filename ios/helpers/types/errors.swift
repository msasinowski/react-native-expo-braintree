//
//  BTPayPalVaultRequest.swift
//  expo-braintree
//
//  Created by Maciej Sasinowski on 28/04/2024.
//

import Foundation

enum EXCEPTION_TYPES: String {
  case SWIFT_EXCEPTION = "ReactNativeExpoBraintree:`SwiftException"
  case USER_CANCEL_EXCEPTION = "ReactNativeExpoBraintree:`UserCancelException"
  case TOKENIZE_EXCEPTION = "ReactNativeExpoBraintree:`TokenizeException"
  case PAYPAL_DISABLED_IN_CONFIGURATION =
        "ReactNativeExpoBraintree:`Paypal disabled in configuration"
  case VENMO_DISABLED_IN_CONFIGURATION =
        "ReactNativeExpoBraintree:`Venmo disabled in configuration"
}

enum ERROR_TYPES: String {
  case API_CLIENT_INITIALIZATION_ERROR = "API_CLIENT_INITIALIZATION_ERROR"
  case TOKENIZE_VAULT_PAYMENT_ERROR = "TOKENIZE_VAULT_PAYMENT_ERROR"
  case USER_CANCEL_TRANSACTION_ERROR = "USER_CANCEL_TRANSACTION_ERROR"
  case PAYPAL_DISABLED_IN_CONFIGURATION_ERROR = "PAYPAL_DISABLED_IN_CONFIGURATION_ERROR"
  case VENMO_DISABLED_IN_CONFIGURATION_ERROR = "VENMO_DISABLED_IN_CONFIGURATION_ERROR"
  case DATA_COLLECTOR_ERROR = "DATA_COLLECTOR_ERROR"
  case CARD_TOKENIZATION_ERROR = "CARD_TOKENIZATION_ERROR"
  case D_SECURE_CARD_TOKENIZATION_ERROR = "D_SECURE_CARD_TOKENIZATION_ERROR"
  case D_SECURE_CARD_TOKENIZATION_VALIDATION_ERROR = "D_SECURE_CARD_TOKENIZATION_VALIDATION_ERROR"
  case D_SECURE_NOT_ABLE_TO_SHIFT_LIABILITY = "D_SECURE_NOT_ABLE_TO_SHIFT_LIABILITY"
  case D_SECURE_LIABILITY_NOT_SHIFTED = "D_SECURE_LIABILITY_NOT_SHIFTED"
  case PAYMENT_3D_SECURE_FAILED = "PAYMENT_3D_SECURE_FAILED"
}
