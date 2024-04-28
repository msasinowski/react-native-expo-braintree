export enum EXCEPTION_TYPES {
  SWIFT_EXCEPTION = 'ReactNativePaypalReborn:`SwiftException',
  USER_CANCEL_EXCEPTION = 'ReactNativePaypalReborn:`UserCancelException',
  PAYPAL_DISABLED_IN_CONFIGURATION = 'ReactNativePaypalReborn:`Paypal disabled in configuration',
  TOKENIZE_EXCEPTION = 'ReactNativePaypalReborn:`TokenizeException',
}

export enum ERROR_TYPES {
  API_CLIENT_INITIALIZATION_ERROR = 'API_CLIENT_INITIALIZATION_ERROR',
  TOKENIZE_VAULT_PAYMENT_ERROR = 'TOKENIZE_VAULT_PAYMENT_ERROR',
  USER_CANCEL_TRANSACTION_ERROR = 'USER_CANCEL_TRANSACTION_ERROR',
  PAYPAL_DISABLED_IN_CONFIGURATION_ERROR = 'PAYPAL_DISABLED_IN_CONFIGURATION_ERROR',
  DATA_COLLECTOR_ERROR = 'DATA_COLLECTOR_ERROR',
  CARD_TOKENIZATION_ERROR = 'CARD_TOKENIZATION_ERROR',
}

export enum BTPayPalCheckoutIntent {
  authorize = 'authorize',
  order = 'order',
  sale = 'sale',
}
export enum BTPayPalRequestUserAction {
  none = 'none',
  payNow = 'payNow',
}

export enum BoolValue {
  true = 'true',
  false = 'false',
}

export type RequestBillingAgreementOptions = {
  clientToken: string;
  billingAgreementDescription?: string;
  displayName?: string;
  localeCode?: string;
  userAuthenticationEmail?: string;
  offerCredit?: BoolValue;
  isShippingAddressRequired?: BoolValue;
  isShippingAddressEditable?: BoolValue;
  isAccessibilityElement?: BoolValue;
};
export type RequestOneTimePaymentOptions = {
  amount: string;
  intent?: BTPayPalCheckoutIntent;
  userAction?: BTPayPalRequestUserAction;
  offerPayLater?: BoolValue;
  currencyCode?: string;
  requestBillingAgreement?: BoolValue;
  clientToken: string;
};

export type TokenizeCardOptions = {
  number: string;
  expirationMonth: string;
  expirationYear: string;
  cvv: string;
  postalCode?: string;
  clientToken: string;
};

export type BTPayPalAccountNonceAddressResult = {
  recipientName?: string;
  streetAddress?: string;
  extendedAddress?: string;
  locality?: string;
  countryCodeAlpha2?: string;
  postalCode?: string;
  region?: string;
};

export type BTPayPalAccountNonceResult = {
  email?: string;
  payerID?: string;
  nonce: string;
  firstName?: string;
  lastName?: string;
  billingAddress?: BTPayPalAccountNonceAddressResult;
  shippingAddress?: BTPayPalAccountNonceAddressResult;
};

export type BTCardTokenizationNonceResult = {
  nonce: string;
  cardNetwork?: string;
  lastTwo?: string;
  lastFour?: string;
  expirationMonth?: string;
  expirationYear?: string;
};

export type BTPayPalGetDeviceDataResult = string;

export type BTPayPalError = {
  code?: EXCEPTION_TYPES;
  message?: ERROR_TYPES | string;
  domain?: ERROR_TYPES;
};
