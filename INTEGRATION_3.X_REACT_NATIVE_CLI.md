# React Native Bare Project (react-native-cli)

## Integration Guide -- `react-native-expo-braintree` (v3.x.x)

> [!WARNING]
> **Deprecated Version**
> This guide is for the legacy **3.x.x** version of the library, which is no longer actively supported.
> It is highly recommended to migrate your project to **4.x.x** (which features a full native rewrite powered by JSI Nitro Modules). Refer to the corresponding 4.x configuration and usage guides:
> - Expo integration: [INTEGRATION_4.X_EXPO.md](INTEGRATION_4.X_EXPO.md)
> - Bare React Native integration: [INTEGRATION_4.X_REACT_NATIVE_CLI.md](INTEGRATION_4.X_REACT_NATIVE_CLI.md)
> - Usage reference: [USAGE_4.X.md](USAGE_4.X.md)

This guide explains how to configure **Braintree** in a **React Native Bare (react-native-cli) project** using **react-native-expo-braintree v3.x.x**.

> **Important:** Before starting the steps below, you must complete the **App Links configuration** described here:
> https://github.com/braintree/braintree_android/blob/main/APP_LINK_SETUP.md

---

# Android Configuration

## 1. Update `AndroidManifest.xml`

Depending on the Braintree methods you use, you must add the appropriate **intent filters** to your **MainActivity**.

### A. If you use:

- `requestBillingAgreement`
- `requestOneTimePayment`
- `tokenizeCardData`

Add the following intent filter inside your `MainActivity`.

Replace `braintree-example-app.web.app` with the domain configured during **App Links setup**.

```xml
<activity>
    ...
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="https" />
        <data android:host="braintree-example-app.web.app" />
    </intent-filter>
</activity>
```

---

### B. If you use:

- `requestVenmoNonce`
- `request3DSecurePaymentCheck`

Add the following intent filter:

```xml
<activity>
    ...
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="${applicationId}.braintree" />
    </intent-filter>
</activity>
```

---

### C. If you use **all methods**

You must add **both intent filters**.

---

# 2. Update `MainActivity.kt`

File location:

    android/app/src/main/java/com/{app_name}/MainActivity.kt

---

### A. For:

- `requestBillingAgreement`
- `requestOneTimePayment`
- `tokenizeCardData`

Add the following inside the `onCreate` method:

```kotlin
import com.expobraintree.ExpoBraintreeModule

override fun onCreate() {
    ...
    ExpoBraintreeModule.init()
    ...
}
```
