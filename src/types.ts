export enum EXCEPTION_TYPES {
  SWIFT_EXCEPTION = 'ExpoBraintree:`SwiftException',
  USER_CANCEL_EXCEPTION = 'ExpoBraintree:`UserCancelException',
  TOKENIZE_EXCEPTION = 'ExpoBraintree:`TokenizeException',
}

export enum PAYPAL_EXCEPTION_TYPES {
  PAYPAL_DISABLED_IN_CONFIGURATION = 'ExpoBraintree:`Paypal disabled in configuration',
}

export enum VENMO_EXCEPTION_TYPES {
  VENMO_DISABLED_IN_CONFIGURATION = 'ExpoBraintree:`VENMO disabled in configuration',
}

export enum ERROR_TYPES {
  API_CLIENT_INITIALIZATION_ERROR = 'API_CLIENT_INITIALIZATION_ERROR',
  TOKENIZE_VAULT_PAYMENT_ERROR = 'TOKENIZE_VAULT_PAYMENT_ERROR',
  USER_CANCEL_TRANSACTION_ERROR = 'USER_CANCEL_TRANSACTION_ERROR',
  DATA_COLLECTOR_ERROR = 'DATA_COLLECTOR_ERROR',
  CARD_TOKENIZATION_ERROR = 'CARD_TOKENIZATION_ERROR',
}

export enum PAYPAL_ERROR_TYPES {
  PAYPAL_DISABLED_IN_CONFIGURATION_ERROR = 'PAYPAL_DISABLED_IN_CONFIGURATION_ERROR',
}

export enum VENMO_ERROR_TYPES {
  VENMO_DISABLED_IN_CONFIGURATION = 'VENMO_DISABLED_IN_CONFIGURATION_ERROR',
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

export enum BTVenmoPaymntMethodUsage {
  multiUse = 'multiUse',
  singleUse = 'singleUse',
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
  hasUserLocationConsent?: BoolValue;
  merchantAppLink: string;
  fallbackUrlScheme?: string;
};
export type RequestOneTimePaymentOptions = {
  amount: string;
  intent?: BTPayPalCheckoutIntent;
  userAction?: BTPayPalRequestUserAction;
  offerPayLater?: BoolValue;
  currencyCode?: string;
  requestBillingAgreement?: BoolValue;
  hasUserLocationConsent?: BoolValue;
  clientToken: string;
  merchantAppLink: string;
  fallbackUrlScheme?: string;
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

export type RequestVenmoNonceOptions = {
  clientToken: string;
  profileID?: string;
  vault?: BoolValue;
  paymentMethodUsage?: BTVenmoPaymntMethodUsage;
  displayName?: string;
  collectCustomerBillingAddress?: BoolValue;
  collectCustomerShippingAddress?: BoolValue;
  isFinalAmount?: BoolValue;
  subTotalAmount?: string;
  discountAmount?: string;
  taxAmount?: string;
  shippingAmount?: string;
  totalAmount?: string;
  fallbackToWeb?: BoolValue;
  fallbackUrlScheme?: string;
};

export type BTVenmoNonceResult = {
  nonce: string;
  email?: string;
  externalID?: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  username?: string;
  billingAddress?: BTPayPalAccountNonceAddressResult;
  shippingAddress?: BTPayPalAccountNonceAddressResult;
};

export type BTPayPalGetDeviceDataResult = string;

export type BTPayPalError = {
  code?: EXCEPTION_TYPES | PAYPAL_EXCEPTION_TYPES;
  message?: ERROR_TYPES | PAYPAL_ERROR_TYPES | string;
  domain?: ERROR_TYPES | PAYPAL_ERROR_TYPES;
};

export type BTVenmoError = {
  code?: EXCEPTION_TYPES | VENMO_EXCEPTION_TYPES;
  message?: ERROR_TYPES | VENMO_ERROR_TYPES | string;
  domain?: ERROR_TYPES | VENMO_ERROR_TYPES;
};
