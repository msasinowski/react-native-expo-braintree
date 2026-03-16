export enum EXCEPTION_TYPES {
  KOTLIN_EXCEPTION = 'ExpoBraintree:`KotlinException',
  USER_CANCEL_EXCEPTION = 'ExpoBraintree:`UserCancelException',
  TOKENIZE_EXCEPTION = 'ExpoBraintree:`TokenizeException',
}

export enum PAYPAL_EXCEPTION_TYPES {
  PAYPAL_DISABLED_IN_CONFIGURATION = 'ExpoBraintree:`Paypal disabled in configuration',
}

export enum VENMO_EXCEPTION_TYPES {
  VENMO_DISABLED_IN_CONFIGURATION = 'ExpoBraintree:`VENMO disabled in configuration',
}

export enum GOOGLE_PAY_ERROR_TYPES {
  GOOGLE_PAY_NOT_AVAILABLE = 'GOOGLE_PAY_NOT_AVAILABLE',
  GOOGLE_PAY_FAILED = 'GOOGLE_PAY_FAILED',
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

export enum THREE_D_SECURE_ERROR_TYPES {
  D_SECURE_NOT_ABLE_TO_SHIFT_LIABILITY = 'D_SECURE_NOT_ABLE_TO_SHIFT_LIABILITY',
  D_SECURE_LIABILITY_NOT_SHIFTED = 'D_SECURE_LIABILITY_NOT_SHIFTED',
  PAYMENT_3D_SECURE_FAILED = 'PAYMENT_3D_SECURE_FAILED',
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
  hasUserLocationConsent?: boolean;
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

export type BTThreeDError = {
  code?: EXCEPTION_TYPES | VENMO_EXCEPTION_TYPES;
  message?: ERROR_TYPES | THREE_D_SECURE_ERROR_TYPES | string;
  domain?: ERROR_TYPES | THREE_D_SECURE_ERROR_TYPES;
};

export type ThreeDSecureInfo = {
  /** True if the bank accepts responsibility for the fraud risk */
  liabilityShifted: boolean;
  /** True if the card is eligible for 3D Secure */
  liabilityShiftPossible: boolean;
  /** The status of the 3D Secure verification (e.g., 'authenticated', 'lookup_error') */
  status: string;
  /** Indicates if the verification process was actually performed */
  wasVerified: boolean;
};

export type BTCardTokenization3DSNonceResult = {
  nonce: string;
  cardNetwork?: string;
  lastTwo?: string;
  lastFour?: string;
  expirationMonth?: string;
  expirationYear?: string;
  /** Detailed 3D Secure verification results from the bank */
  threeDSecureInfo?: ThreeDSecureInfo;
};

type D = '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9';

export type ThreeDSecureCheckOptions = {
  clientToken: string;
  /**
   * Transaction amount in "X.YY" format.
   * @example "10.00"
   */
  amount: `${number}.${D}${D}`;
  nonce: string;
  email?: string;
  givenName?: string;
  surName?: string;
  phoneNumber?: string;
  streetAddress?: string;
  extendedAddress?: string;
  city?: string;
  postalCode?: string;
  region?: string;
  countryCodeAlpha2?: string;
};

export enum GOOGLE_PAY_TOTAL_PRICE_STATUS {
  /** The total price is an estimated price and might still change (maps to 1 in Kotlin) */
  ESTIMATED = 1,
  /** The total price is the final price and will not change (maps to 3/else in Kotlin) */
  FINAL = 3,
}

export type RequestGooglePayOptions = {
  clientToken: string;
  totalPrice: string;
  currencyCode: string;
  totalPriceStatus?: GOOGLE_PAY_TOTAL_PRICE_STATUS;
  googleMerchantName?: string;
  billingAddressRequired?: boolean;
  emailRequired?: boolean;
  phoneNumberRequired?: boolean;
  shippingAddressRequired?: boolean;
  allowPrepaidCards?: boolean;
};

export type BTGooglePayNonceResult = {
  nonce: string;
  type: 'GooglePayCard';
  description: string;
  details: {
    cardType: string;
    lastFour: string;
    lastTwo: string;
  };
  billingAddress?: {
    recipientName?: string;
    streetAddress?: string;
    locality?: string;
    countryCodeAlpha2?: string;
  };
};

export type BTGooglePayError = {
  code?: EXCEPTION_TYPES | GOOGLE_PAY_ERROR_TYPES;
  message?: string;
};
