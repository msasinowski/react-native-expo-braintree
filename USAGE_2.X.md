# Usage
# Package Version 2.x.x
## Request One Time Payment

```javascript
import {
  requestOneTimePayment,
} from "expo-braintree";

const result: BTPayPalAccountNonceResult | BTPayPalError  = await requestOneTimePayment({
    clientToken: 'Token',
    amount: '5.0',
    currencyCode: 'USD'
    })

```

## Card tokenization
```javascript
import {
  tokenizeCard,
} from "expo-braintree";

const result: BTCardTokenizationNonceResult | BTPayPalError = await tokenizeCard({
    clientToken: 'Token",
    number: '1111222233334444',
    expirationMonth: '11',
    expirationYear: '24',
    cvv: '123',
    postalCode: '',
    })

```

## Request PayPal billing agreement
```javascript
import {
  requestBillingAgreement,
} from "expo-braintree";

const result: BTPayPalAccountNonceResult | BTPayPalError  = await requestBillingAgreement({
    clientToken: 'Token',
    billingAgreementDescription: 'Description,
    localeCode: 'en-US'
    })
    .then(result => console.log(result))
    .catch((error) => console.log(error));
```
## Call Data Collector and get correlation id
```javascript
import {
  getDeviceDataFromDataCollector,
} from "expo-braintree";

const result: string = await getDeviceDataFromDataCollector(clientToken})

```