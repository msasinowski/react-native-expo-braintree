# Usage
# Package Version 3.x.x

> [!WARNING]
> **Deprecated Version**
> This guide is for the legacy **3.x.x** version of the library, which is no longer actively supported.
> It is highly recommended to migrate your project to **4.x.x** (which features a full native rewrite powered by JSI Nitro Modules). Refer to the corresponding 4.x configuration and usage guides:
> - Expo integration: [INTEGRATION_4.X_EXPO.md](INTEGRATION_4.X_EXPO.md)
> - Bare React Native integration: [INTEGRATION_4.X_REACT_NATIVE_CLI.md](INTEGRATION_4.X_REACT_NATIVE_CLI.md)
> - Usage reference: [USAGE_4.X.md](USAGE_4.X.md)
## Request One Time Payment

```javascript
import {
  requestOneTimePayment,
} from "expo-braintree";

const result: BTPayPalAccountNonceResult | BTPayPalError  = await requestOneTimePayment({
        clientToken: 'Token",
        merchantAppLink: "https://braintree-example-app.web.app",
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
```
