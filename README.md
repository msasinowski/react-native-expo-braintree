# react-native-expo-braintree

A high-performance, native implementation of the [Braintree SDK](https://developer.paypal.com/braintree/docs/start/overview) for React Native and Expo, powered by [Nitro Modules](https://nitro.margelo.com/).

---

## Compatibility Matrix

### Native SDK Versions

| Package Version | Braintree Android | Braintree iOS | Min Android SDK | Min iOS |
| :-------------- | :---------------: | :-----------: | :-------------: | :-----: |
| **4.0.0**       |      v5.19.0      |    v7.5.0     |       23        |  15.1   |

### Expo SDK Support

| Package Version | Supported Expo SDK |
| :-------------- | :----------------- |
| **4.0.0**       | 53+                |

---

## Features

All 7 Braintree payment methods supported:

| Method | Android | iOS |
| :----- | :-----: | :-: |
| PayPal One-Time Payment | ✅ | ✅ |
| PayPal Billing Agreement | ✅ | ✅ |
| Card Tokenization | ✅ | ✅ |
| 3D Secure | ✅ | ✅ |
| Venmo | ✅ | ✅ |
| Google Pay | ✅ | N/A |
| Data Collector | ✅ | ✅ |

---

## Installation

```sh
npm install react-native-expo-braintree react-native-nitro-modules
```

> `react-native-nitro-modules` is required as this library relies on [Nitro Modules](https://nitro.margelo.com/).

---

## Troubleshooting Guide

### 1. Required Setup: Android App Links

To ensure PayPal and browser-based flows work, you must configure Android App Links.

- **Official Guide:** [Braintree Android App Link Setup](https://github.com/braintree/braintree_android/blob/main/APP_LINK_SETUP.md)
- **Context:** Braintree Android v5 requires App Links instead of custom scheme deep links.

### 2. Common Issues

#### A. Missing Fallback Scheme (Android)

**Symptom:** `TOKENIZE_VAULT_PAYMENT_ERROR` on Android during `requestBillingAgreement`.

**The Fix (Expo):** Pass `addFallbackUrlScheme` into your Expo config plugin:

```json
"plugins": [
  ["react-native-expo-braintree", {
    "host": "your-domain.com",
    "pathPrefix": "/payments",
    "addFallbackUrlScheme": "true"
  }]
]
```

**Important:** The fallbackUrlScheme must end with `.braintree` (e.g., `com.your.app.braintree`).

#### B. Verification via ADB

```sh
adb -d shell pm get-app-links <YOUR_PACKAGE_NAME>
```

**Required Output:** `your-braintree-domain.com: verified`

#### C. Server-Side: assetlinks.json

Your web domain must host a valid association file at:
`https://your-domain.com/.well-known/assetlinks.json`

**Reference:** [Braintree Example assetlinks.json](https://braintree-example-app.web.app/.well-known/assetlinks.json)

#### D. 3DSecure Window Layout Issues (Android)

**Symptom:** Crash or layout issues during 3DSecure on Android 14+.

**Fix:** Add to your `android/app/build.gradle`:

```gradle
dependencies {
    api "androidx.activity:activity:1.9.3"
}
```

---

## Integration

Please follow the correct integration guide for your project type:

### Expo Based Project (SDK 53+)

- [Integration Guide (Expo)](INTEGRATION_3.X_EXPO.md)

### React Native Bare Project (CLI)

- [Integration Guide (CLI)](INTEGRATION_3.X_REACT_NATIVE_CLI.md)

---

## Usage

Full API reference and examples:

- [Usage Reference](USAGE_3.X.md)

## License

MIT
