# react-native-expo-braintree

A high-performance, native implementation of the [Braintree SDK](https://developer.paypal.com/braintree/docs/start/overview) for React Native and Expo.

---

**Note:** This package is the successor to `react-native-paypal-reborn`. The name change reflects the addition of first-class Expo support. All future updates and features will be released under `react-native-expo-braintree`.

## ⚠️ Read Before Usage

- **Native Rewrite (v2.x.x+):** Starting from version 2.0.0, the core was rewritten in **Kotlin** (Android) and **Swift** (iOS) to ensure modern compatibility and Expo support.
- **Support Status:** Only version **3.x.x** is actively supported. Support for 2.x.x has been discontinued due to breaking changes in the Braintree Android integration. All ongoing fixes target the 3.x.x line exclusively.
- **Issues:** If you encounter problems, please report them via the [Issues panel](https://github.com/msasinowski/react-native-expo-braintree/issues).

---

## Compatibility Matrix

### Native SDK Versions

| Package Version   | Braintree Android | Braintree iOS | Min Android SDK | Min iOS |
| :---------------- | :---------------: | :-----------: | :-------------: | :-----: |
| **3.4.0**         |      v5.19.0      |    v6.41.0    |       23        |  15.1   |
| **3.3.0**         |      v5.19.0      |    v6.41.0    |       23        |  15.1   |
| **3.2.0 - 3.2.2** |      v5.19.0      |    v6.41.0    |       23        |  15.1   |
| **3.1.0**         |      v5.9.x       |    v6.31.0    |       23        |  14.0   |
| **3.0.1 - 3.0.5** |      v5.2.x       |    v6.23.3    |       23        |  14.0   |
| **2.2.0 - 2.4.0** |      v4.41.x      |    v6.17.0    |       21        |  14.0   |

### Expo SDK Support

| Package Version   | Supported Expo SDK |
| :---------------- | :----------------- |
| **3.2.1+**        | 53                 |
| **3.0.3 - 3.1.0** | 50, 51, 52         |
| **3.0.1 - 3.0.2** | 50, 51             |
| **2.5.0**         | 50, 51, 52         |
| **2.2.0 - 2.4.0** | 50, 51             |

---

### Feature List

| Package Version | Supported Expo SDK       |
| :-------------- | :----------------------- |
| **3.4.0**       | Google Pay Feature Added |
| **3.3.0**       | 3D Secure Feature Added  |

---

## 🛠️ Demos

![iOS](assets/ios_demo_with_3d_secure.gif)
![Android](assets/android_demo_with_3ds_secure.gif)

## 🛠️ Troubleshooting Guide (v3.x.x+)

**Important:** Version 3.x.x uses **Braintree Android SDK v5**, which introduces a breaking change: shifting from **Custom Scheme Deep Links** to **Android App Links (HTTPS)**.

### 1. Required Setup: Android App Links

To ensure PayPal and browser-based flows work, you must transition to App Links.

- **Official Guide:** [Braintree Android App Link Setup](https://github.com/braintree/braintree_android/blob/main/APP_LINK_SETUP.md)
- **Migration Context:** Braintree Android v4.x.x is supported until **Q3 2025**. After that, SDK v5 is mandatory.

### 2. Common Issues & Solutions

#### A. Missing Fallback Scheme (Android)

**Symptom:** You receive a `TOKENIZE_VAULT_PAYMENT_ERROR` on Android during `requestBillingAgreement`.

**The Cause:** Even with App Links, the SDK requires a Fallback URL Scheme for specific browser-switch flows.

**The Fix (Expo):** Pass the `addFallbackUrlScheme` property into your Expo config plugin

**Important:** The fallbackUrlScheme must match the one used in your runtime calls and must end with .braintree (e.g., com.your.app.braintree). This suffix is a requirement of the Braintree Android SDK to correctly identify the return intent.

```json
"plugins": [
  ["react-native-expo-braintree", {
    "host": "your-domain.com",
    "pathPrefix": "/payments",
    "addFallbackUrlScheme": "true"
  }]
]
```

#### B. Verification via ADB

Android must verify your domain. Run:
`adb -d shell pm get-app-links <YOUR_PACKAGE_NAME>`

**Required Output:**
`your-braintree-domain.com: verified`

**CAUTION:** If the state is `legacy_failure` or `ask`, the OS has rejected your `assetlinks.json` configuration. Check your SHA-256 fingerprints and HTTPS accessibility.

#### C. Server-Side: assetlinks.json

Your web domain must host a valid association file at:
`https://your-domain.com/.well-known/assetlinks.json`

- **Fingerprints:** Ensure the `sha256_cert_fingerprints` matches your app's signing certificate.
- **Reference:** [Braintree Example assetlinks.json](https://braintree-example-app.web.app/.well-known/assetlinks.json)

#### D. 3DSecure Window Layout Issues (EdgeToEdge Fix) (Android)

**Symptom:** You receive a a crash during initializing 3DSecure or some layout issues

**The Cause:** If you are using Android 14 (API 34) or higher, you might encounter layout issues where the 3DSecure verification screen (Cardinal SDK) is rendered behind system bars or has unclickable buttons. This is caused by the new "Edge-to-Edge" enforcement in newer Android versions. Or you got just a crash when you start a 3DSecure window.

**The Fix (rn-cli-bare):** You must ensure your project uses at least version 1.8.0 of the androidx.activity library to properly handle window insets for 3DSecure activities. Add the following to your android/app/build.gradle (in the dependencies block):

```gradle
dependencies {
    // ... other dependencies
    // 3DSecure EdgeToEdge Fix
    api "androidx.activity:activity:1.8.0"
}
```

---

## Support

If you continue to experience issues after verifying the App Link state and the Fallback Scheme, please create an Issue on the GitHub repository: [GitHub Issues](https://github.com/msasinowski/react-native-expo-braintree/issues)

## Integration

Please follow the correct integration guide for your project type:

### 🚀 Expo Based Project (SDK 53)

- [Integration Guide for v3.x.x (Expo)](INTEGRATION_3.X_EXPO.md)

### 🏗️ React Native Bare Project (CLI)

- [Integration Guide for v3.x.x (CLI)](INTEGRATION_3.X_REACT_NATIVE_CLI.md)

---

## Usage and Examples

You can find implementation details in the [Example App](example/src/App.tsx) or in the dedicated usage page:

- [3.x.x Usage Reference](USAGE_3.X.md)

## Special Thanks

- [iacop0](https://github.com/iacop0) — For introducing Venmo Integration and Android Version Bump.

## Roadmap

- [x] Venmo Integration
- [x] 3D-Secure (Implemented in 3.3.0)
- [x] Google Pay (Implemented in 3.4.0)
- [ ] Apple Pay (TBD)
