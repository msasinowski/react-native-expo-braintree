//
//  BTPayPalVaultRequest.swift (Error Definitions)
//  expo-braintree
//
//  Created by Maciej Sasinowski on 28/04/2024.
//

import Foundation

/**
 * Exception types used to categorize errors sent to the React Native bridge.
 * These match the Kotlin constants used in the Android implementation.
 */
enum EXCEPTION_TYPES: String {
    // Prefix used for consistent error handling in the Expo module
    case SWIFT_EXCEPTION = "ExpoBraintree:`SwiftException"
    case USER_CANCEL_EXCEPTION = "ExpoBraintree:`UserCancelException"
    case TOKENIZE_EXCEPTION = "ExpoBraintree:`TokenizeException"
    
    // Configuration-specific exceptions
    case PAYPAL_DISABLED_IN_CONFIGURATION = "ExpoBraintree:`PAYPAL_DISABLED_IN_CONFIGURATION"
    case VENMO_DISABLED_IN_CONFIGURATION = "ExpoBraintree:`VENMO_DISABLED_IN_CONFIGURATION"
}

/**
 * Detailed error codes to identify specific failure points during the payment flow.
 */
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
