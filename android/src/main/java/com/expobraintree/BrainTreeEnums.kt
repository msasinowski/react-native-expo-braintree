package com.expobraintree

enum class EXCEPTION_TYPES(val value: String) {
  KOTLIN_EXCEPTION("ExpoBraintree:`KotlinException"),
  USER_CANCEL_EXCEPTION("ExpoBraintree:`UserCancelException"),
  TOKENIZE_EXCEPTION("ExpoBraintree:`TokenizeException"),
}

enum class PAYPAL_EXCEPTION_TYPES(val value: String) {
  PAYPAL_DISABLED_IN_CONFIGURATION("ExpoBraintree:`Paypal disabled in configuration")
}

enum class VENMO_EXCEPTION_TYPES(val value: String) {
  VENMO_DISABLED_IN_CONFIGURATION("ExpoBraintree:`VENMO disabled in configuration")
}

enum class ERROR_TYPES(val value: String) {
  API_CLIENT_INITIALIZATION_ERROR("API_CLIENT_INITIALIZATION_ERROR"),
  TOKENIZE_VAULT_PAYMENT_ERROR("TOKENIZE_VAULT_PAYMENT_ERROR"),
  USER_CANCEL_TRANSACTION_ERROR("USER_CANCEL_TRANSACTION_ERROR"),
  DATA_COLLECTOR_ERROR("DATA_COLLECTOR_ERROR"),
  CARD_TOKENIZATION_ERROR("CARD_TOKENIZATION_ERROR")
}

enum class PAYPAL_ERROR_TYPES(val value: String) {
  PAYPAL_DISABLED_IN_CONFIGURATION_ERROR("PAYPAL_DISABLED_IN_CONFIGURATION_ERROR")
}

enum class VENMO_ERROR_TYPES(val value: String) {
  VENMO_DISABLED_IN_CONFIGURATION("VENMO_DISABLED_IN_CONFIGURATION_ERROR")
}