# react-native-expo-braintree

This package is a continuation of https://www.npmjs.com/package/react-native-paypal-reborn , the name change happened because of adding expo support, and all of future updates will be done under react-native-expo-braintree

## Read This Before Usage

Please note that starting from version 2.x.x, the entire integration was rewritten from scratch using Kotlin (previously Java) and Swift (previously Objective-C) to prepare the codebase for a future migration to an Expo package. If you encounter any problems or issues, please report them via the Issues panel.

As of now, only the 3.x.x version of the package is actively supported. Support for the 2.x.x version has been discontinued. The primary reason is that the 3.x.x release introduces breaking changes in the Android integration, and all ongoing development and fixes will target the 3.x.x line exclusively.

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
|                3.0.1                |        v5.2.x         |      v6.23.3      |         23          |      14.0       |
|                3.0.2                |        v5.2.x         |      v6.23.3      |         23          |      14.0       |
|                3.0.3                |        v5.2.x         |      v6.23.3      |         23          |      14.0       |
|                3.0.4                |        v5.2.x         |      v6.23.3      |         23          |      14.0       |
|                3.0.5                |        v5.2.x         |      v6.23.3      |         23          |      14.0       |
|                3.1.0                |        v5.9.x         |      v6.31.0      |         23          |      14.0       |
|                3.1.0                |        v5.19.0        |      v6.41.0      |         23          |      15.1       |

| React Native Expo Braintree Version |    Expo SDK    |
| :---------------------------------: | :------------: |
|                2.2.0                |    50 or 51    |
|                2.2.1                |    50 or 51    |
|                2.2.2                |    50 or 51    |
|                2.4.0                |    50 or 51    |
|                2.5.0                | 50 or 51 or 52 |
|                3.0.1                |    50 or 51    |
|                3.0.2                |    50 or 51    |
|                3.0.3                | 50 or 51 or 52 |
|                3.0.4                | 50 or 51 or 52 |
|                3.0.5                | 50 or 51 or 52 |
|                3.1.0                | 50 or 51 or 52 |
|                3.2.0                |       53       |

## !!! Important Information Only For Package Version Above 3.x.x^ !!!

Package Version 3.x.x, introduce breaking change for the whole Android Integration, long story short [Braintree SDK For Android](https://github.com/braintree/braintree_android), from version 5.X.X , change a way of handling deep link and context switch from the PayPal (Browser) to the App, instead using custom schema Deep Link, SDK Version 5.x.x introduces usage of [AppLinks for Android](https://developer.android.com/training/app-links), with https schema.

Please check official Migration Guide from v4 to v5, most likely you do not need to do anything with that, the package itself already handles that. [MIgration Guide v4 to v5](https://github.com/braintree/braintree_android/blob/main/v5_MIGRATION_GUIDE.md)

## Required Setup For 3.x.x^

This step is required, to finalize if you want to upgrade this package to the newest version 3.x.x^.

Please follow, the official [Set Up App Links](https://github.com/braintree/braintree_android/blob/main/APP_LINK_SETUP.md) Guide, and make sure that all the steps was completed.

### Troubleshooting for 3.x.x^

- Make sure that the domain/url that you added to the AndroidManifest, is verified successfully as auto-verified AppLink, you can check that by using command from adb

Command:

`adb -d shell pm get-app-links com.expobraintreeexample`

Output:

```com.expobraintreeexample:
    ID: b3a3a2ff-0148-4bc0-b0d0-dfcaaf047a4b
    Signatures: [FA:C6:17:45:DC:09:03:78:6F:B9:ED:E6:2A:96:2B:39:9F:73:48:F0:BB:6F:89:9B:83:32:66:75:91:03:3B:9C]
    Domain verification state:
      braintree-example-app.web.app: verified
```

- Make Sure that, your .well-known/assetlinks.json on your web page, is using right fingerprint, and the right data to handle the AppLink, an example used for [Example App and for link](https://braintree-example-app.web.app/.well-known/assetlinks.json) is located [here](https://github.com/msasinowski/react-native-expo-braintree-app-link/tree/main)
- You can have, various other issues related to that upgrade , but most likely they will be related to the AppLink's setup, until Q3 2025 braintree-android v4.x.x will be supported, but after that , everyone will need to switch to the v5.x.x SDK. If you have any other issues please create an Issue, on board

## Integration

Please follow the correct integration guide before you will start a new issue, and be sure that you follow the right steps.

### EXPO Based Project (EXPO SDK 53)

#### Package Version 3.x.x

[Integration Expo Based Project Package Version 3.x.x](INTEGRATION_3.X_EXPO.md)

### React Native Bare Project (react-native-cli)

#### Package Version 3.x.x

[React Native CLI Based Project Package Version 3.x.x](INTEGRATION_3.X_REACT_NATIVE_CLI.md)

## Usage and Examples

You can find it in [Example App](example/src/App.tsx) or in dedicated, usage pages:

- 3.x.x -> [Usage](USAGE_3.X.md)

## Special Thanks

- To iacop0 https://github.com/iacop0 - For introducing Venmo Integration And Android Version Bump

## TODO

- [ ] Add ApplePay,
- [ ] Google Pay,
- [ ] 3D-Secure (In Progress)
- [x] Venmo
