# Usage

## Import

```typescript
import {
  requestOneTimePayment,
  requestBillingAgreement,
  tokenizeCardData,
  getDeviceDataFromDataCollector,
  requestVenmoNonce,
  request3DSecurePaymentCheck,
  requestGooglePayPayment,
} from 'react-native-expo-braintree';
```

---

## Request One Time Payment (PayPal)

```typescript
const result = await requestOneTimePayment({
  clientToken: 'YOUR_CLIENT_TOKEN',
  merchantAppLink: 'https://braintree-example-app.web.app',
  amount: '5.0',
  currencyCode: 'USD',
  intent: BTPayPalCheckoutIntent.sale, // authorize | order | sale
});
```

---

## Request Billing Agreement (PayPal Vault)

```typescript
const result = await requestBillingAgreement({
  clientToken: 'YOUR_CLIENT_TOKEN',
  merchantAppLink: 'https://braintree-example-app.web.app',
  billingAgreementDescription: 'Recurring Payment',
  displayName: 'My App',
});
```

---

## Card Tokenization

```typescript
const result = await tokenizeCardData({
  clientToken: 'YOUR_CLIENT_TOKEN',
  number: '4111111111111111',
  expirationMonth: '12',
  expirationYear: '2030',
  cvv: '123',
  postalCode: '12345', // optional
});
```

---

## Data Collector

```typescript
const deviceData = await getDeviceDataFromDataCollector(clientToken);
```

Returns a correlation ID string for fraud detection.

---

## Venmo Nonce

```typescript
const nonce = await requestVenmoNonce({
  clientToken: 'YOUR_CLIENT_TOKEN',
  merchantAppLink: 'https://braintree-example-app.web.app/braintree-payments/',
  vault: BoolValue.false,
  paymentMethodUsage: BTVenmoPaymntMethodUsage.singleUse,
});
```

---

## 3D Secure

```typescript
// First, tokenize a card:
const tokenized = await tokenizeCardData({
  clientToken: '3DS_CLIENT_TOKEN',
  number: '4000000000002503', // challenge required
  expirationMonth: '12',
  expirationYear: '2028',
  cvv: '123',
});

// Then verify with 3D Secure:
const verified = await request3DSecurePaymentCheck({
  clientToken: '3DS_CLIENT_TOKEN',
  amount: '10.00',
  nonce: tokenized.nonce,
  email: 'jill.doe@example.com',
  givenName: 'Jill',
  surName: 'Doe',
  streetAddress: '555 Smith St',
  city: 'Chicago',
  region: 'IL',
  postalCode: '60622',
  countryCodeAlpha2: 'US',
  phoneNumber: '5551234567',
});
```

### 3DS Test Cards

| Scenario | Card Number |
| :------- | :---------- |
| Success (No Challenge) | `4000000000002701` |
| Challenge Required | `4000000000002503` |
| Failed (Frictionless) | `4000000000002925` |

---

## Google Pay (Android only)

```typescript
const result = await requestGooglePayPayment({
  clientToken: 'YOUR_CLIENT_TOKEN',
  totalPrice: '199.00',
  currencyCode: 'USD',
  totalPriceStatus: GOOGLE_PAY_TOTAL_PRICE_STATUS.FINAL,
  billingAddressRequired: true,
  shippingAddressRequired: true,
  emailRequired: true,
  allowPrepaidCards: false,
});
```

---

## Error Handling

All methods return a result on success or throw an error object
with the following structure:

```typescript
{
  code?: string;        // e.g. "ExpoBraintree:`KotlinException"
  message?: string;     // e.g. "TOKENIZE_VAULT_PAYMENT_ERROR"
  domain?: string;      // e.g. "TOKENIZE_VAULT_PAYMENT_ERROR"
  nativeError?: string; // Original native error message
}
```

Errors can also include `error: 'true'` flag for methods that
resolve successfully with an error payload.

---

## TypeScript Types

Available via:

```typescript
import type {
  BTPayPalAccountNonceResult,
  BTPayPalError,
  BTCardTokenizationNonceResult,
  BTCardTokenization3DSNonceResult,
  BTThreeDError,
  BTVenmoNonceResult,
  BTVenmoError,
  BTGooglePayNonceResult,
  BTGooglePayError,
  BTDataCollectorError,
  ThreeDSecureCheckOptions,
  RequestOneTimePaymentOptions,
  RequestBillingAgreementOptions,
  TokenizeCardOptions,
  RequestVenmoNonceOptions,
  RequestGooglePayOptions,
} from 'react-native-expo-braintree';
```

### Enums

```typescript
import {
  BTPayPalCheckoutIntent,
  BTPayPalRequestUserAction,
  BoolValue,
  BTVenmoPaymntMethodUsage,
  GOOGLE_PAY_TOTAL_PRICE_STATUS,
} from 'react-native-expo-braintree';
```
