# React Native Bare Project (react-native-cli)

## Integration Guide -- `react-native-expo-braintree` (v3.x.x)

This guide explains how to configure **Braintree** in a **React Native
Bare (react-native-cli) project** using **react-native-expo-braintree
v3.x.x**.

> **Important:** Before starting the steps below, you must complete the
> **App Links configuration** described here:\
> https://github.com/braintree/braintree_android/blob/main/APP_LINK_SETUP.md

---

# Android Configuration

## 1. Update `AndroidManifest.xml`

Depending on the Braintree methods you use, you must add the appropriate
**intent filters** to your **MainActivity**.

### A. If you use:

- `requestBillingAgreement`
- `requestOneTimePayment`
- `tokenizeCardData`

Add the following intent filter inside your `MainActivity`.

Replace `braintree-example-app.web.app` with the domain configured
during **App Links setup**.

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

---

### B. For:

- `request3DSecurePaymentCheck`

Add the following instead:

```kotlin
import com.expobraintree.ExpoBraintreeModule

override fun onCreate() {
    ...
    ExpoBraintreeModule.initThreeDSecure(this)
    ...
}
```

---

### C. For:

- `requestGooglePayPayment`

Add the following instead:

```kotlin
import com.expobraintree.ExpoBraintreeModule

override fun onCreate() {
    ...
    ExpoBraintreeModule.initGooglePay(this)
    ...
}
```

---

### D. If you use **all methods**

You must add **all initialization methods**.

---

# 3. Update `build.gradle`

If you use **3D Secure**, add the following repository to:

    android/build.gradle

Add it **at the end of the file**.

```gradle
allprojects {
    repositories {
        maven {
            url "https://cardinalcommerceprod.jfrog.io/artifactory/android"
            credentials {
                username 'braintree_team_sdk'
                password 'AKCp8jQcoDy2hxSWhDAUQKXLDPDx6NYRkqrgFLRc3qDrayg6rrCbJpsKKyMwaykVL8FWusJpp'
            }
        }
    }
}
```

---

# iOS Configuration

## 1. Install CocoaPods

Run the following command:

```bash
cd ios
pod install
```

---

# 2. Configure URL Scheme

Add a **Bundle URL Scheme** to your app.

### Using Xcode

1.  Open your project in **Xcode**
2.  Go to **Info**
3.  Add a **URL Type**

### Required URL Scheme

    {BUNDLE_IDENTIFIER}.braintree

---

### Example `Info.plist` Configuration

```xml
<key>CFBundleURLTypes</key>
<array>
    <dict>
        <key>CFBundleTypeRole</key>
        <string>Editor</string>
        <key>CFBundleURLName</key>
        <string>com.myapp</string>
        <key>CFBundleURLSchemes</key>
        <array>
            <string>com.myapp.braintree</string>
        </array>
    </dict>
</array>
```

---

# 3. iOS Swift Configuration (Required for v3.x.x)

Starting with **Braintree SDK v6**, the iOS SDK was rewritten in
**Swift**.\
Because of this, a small Swift wrapper is required to expose the
functionality needed by `AppDelegate.swift`.

This guide assumes:

- You are using **React Native 0.79+**
- Your project uses **AppDelegate.swift** instead of `AppDelegate.mm`

If you are using **older React Native (\<0.77)**, refer to the **3.1.0
integration guide**.

---

# 4. Create `ExpoBraintreeConfig.swift`

1.  Open your **iOS project in Xcode**
2.  Create a new file:

```{=html}
<!-- -->
```

    ExpoBraintreeConfig.swift

Add the following code:

```swift
import Braintree
import Foundation

public final class ExpoBraintreeConfig {

    private init() {}

    public static var paymentURLScheme: String {
        let bundleIdentifier = Bundle.main.bundleIdentifier ?? ""
        return bundleIdentifier + ".braintree"
    }

    public static func handleUrl(url: URL) -> Bool {
        return BTAppContextSwitcher.sharedInstance.handleOpen(url)
    }
}
```

---

# 5. Update `AppDelegate.swift`

Add or update the following method:

```swift
func application(
  _ application: UIApplication,
  open url: URL,
  options: [UIApplication.OpenURLOptionsKey : Any] = [:]
) -> Bool {

  if url.scheme?.localizedCaseInsensitiveCompare(
    ExpoBraintreeConfig.paymentURLScheme
  ) == .orderedSame {
    return ExpoBraintreeConfig.handleUrl(url: url)
  }

  return RCTLinkingManager.application(
    application,
    open: url,
    options: options
  )
}
```

---

# Example Projects

All required Android and iOS configuration examples can be found in the
example project directories:

    example/android
    example/ios

---

# Summary

To integrate **react-native-expo-braintree v3.x.x** in a React Native
Bare project:

1.  Configure **Braintree App Links**
2.  Add required **Android intent filters**
3.  Initialize the module in **MainActivity**
4.  Add **3DSecure repository** if required
5.  Configure **iOS URL Scheme**
6.  Create **ExpoBraintreeConfig.swift**
7.  Update **AppDelegate.swift**
