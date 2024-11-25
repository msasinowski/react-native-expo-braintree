# react-native-expo-braintree
This package is a continuation of  https://www.npmjs.com/package/react-native-paypal-reborn , the name change happened because of adding expo support, and all of future updates will be done under react-native-expo-braintree

## Read This Before Usage
- Please note that, from 2.x.x version of the package and whole integration is rewritten from the scratch using Kotlin (previously Java) and Swift (previously Objective-C) to prepare the whole codebase to migrate it into expo package at some point. If you find any problem or issue in the package do not hesitate and report it via Issue panel.
- Currently we, actively support 2.x.x and 3.x.x versions of the package, the main reason why is, that 3.x.x version of package provides a breaking change for the Android integration

## Getting started
React Native Expo Braintree package is a pure native implementation of Braintree SDK
https://developer.paypal.com/braintree/docs/start/overview

| React Native Expo Braintree Version | Braintree Android SDK | Braintree IOS SDK | Minimum SDK Android | Minimum SDK IOS |
| :---------------------------------: | :-------------------: | :---------------: | :-----------------: | :-------------: |
|                2.2.0                |        v4.41.x        |      v6.17.0      |         21          |      14.0       |
|                2.2.1                |        v4.41.x        |      v6.17.0      |         21          |      14.0       |
|                2.2.2                |        v4.41.x        |      v6.17.0      |         21          |      14.0       |
|                2.3.0                |        v4.41.x        |      v6.23.3      |         21          |      14.0       |
|                2.4.0                |        v4.41.x        |      v6.23.3      |         21          |      14.0       |
|                3.0.0                |        v5.2.x         |      v6.23.3      |         23          |      14.0       |

## Integration
Since package, currently is supporting two versions tracks 2.x.x and 3.x.x, which had a bit different integration steps, the documentation about that is separated based on version and based on if your project is using expo or react-native-cli. Please follow the correct integration guide before you will start a new issue.

### EXPO Based Project (EXPO SDK 50 and EXPO SDK 51)

#### Package Version 2.x.x
[Integration Expo Based Project Package Version 2.x.x](INTEGRATION_2.X_EXPO.md)

#### Package Version 3.x.x
[Integration Expo Based Project Package Version 3.x.x](INTEGRATION_3.X_EXPO.md)

### React Native Bare Project (react-native-cli)

#### Package Version 2.x.x
[React Native CLI Based Project Package Version 2.x.x](INTEGRATION_2.X_REACT_NATIVE_CLI.md)

#### Package Version 3.x.x
[React Native CLI Based Project Package Version 3.x.x](INTEGRATION_3.X_REACT_NATIVE_CLI.md)


## Usage

##### Request One Time Payment

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

##### Card tokenization
```javascript
import {
  tokenizeCard,
} from "expo-braintree";

const result: BTCardTokenizationNonceResult | BTPayPalError = await tokenizeCard({
    clientToken: 'Token,
    number: '1111222233334444',
    expirationMonth: '11',
    expirationYear: '24',
    cvv: '123',
    postalCode: '',
    })

```

##### Request PayPal billing agreement
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
##### Call Data Collector and get correlation id
```javascript
import {
  getDeviceDataFromDataCollector,
} from "expo-braintree";

const result: string = await getDeviceDataFromDataCollector("Token")

```

## Special Thanks
- To iacop0 https://github.com/iacop0 - For introducing Venmo Integration And Android Version Bump 

## TODO
- [ ] Add ApplePay,
- [ ] Google Pay, 
- [ ] 3D-Secure (In Progress)
- [x] Venmo

