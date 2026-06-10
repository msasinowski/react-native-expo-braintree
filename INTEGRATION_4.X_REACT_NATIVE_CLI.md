# React Native Bare Project (react-native-cli)

## Integration Guide -- `react-native-expo-braintree` (v4.0.0, Nitro Modules)

This guide explains how to configure **Braintree** in a **React Native Bare (react-native-cli) project** using **react-native-expo-braintree v4.0.0**.

> **Required dependency:** `react-native-nitro-modules` must be installed alongside this library. It provides the Nitro Modules runtime that bridges native code.

```sh
npm install react-native-expo-braintree react-native-nitro-modules
```

> **Important:** Before starting the steps below, you must complete the **App Links configuration** described here:
> https://github.com/braintree/braintree_android/blob/main/APP_LINK_SETUP.md

---

# Android Configuration

## 1. Update `AndroidManifest.xml`

Depending on the Braintree methods you use, add the appropriate **intent filters** to your **MainActivity**.

### A. If you use:

- `requestBillingAgreement`
- `requestOneTimePayment`
- `tokenizeCardData`

Replace `braintree-example-app.web.app` with your App Links domain:

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

### B. If you use:

- `requestVenmoNonce`
- `request3DSecurePaymentCheck`

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

### C. If you use **all methods**, add both intent filters.

---

# 2. Update `MainActivity.kt`

Nitro Modules handles core module registration automatically.
You **only** need to add initialization for **Google Pay** and **3D Secure** (if you use those features):

```kotlin
import com.margelo.nitro.expobraintree.ExpoBraintree

override fun onCreate() {
    super.onCreate()

    // For 3D Secure (if you use it):
    ExpoBraintree.initThreeDSecure(this)

    // For Google Pay (if you use it):
    ExpoBraintree.initGooglePay(this)
}
```

Please refer to the source class [ExpoBraintree](android/src/main/java/com/margelo/nitro/expobraintree/ExpoBraintree.kt) for detail implementation.

> **Note:** Unlike the previous version, no `ExpoBraintreeModule.init()` call is needed for core functionality. Nitro autolinking handles it.

---

# 3. Update `build.gradle`

If you use **3D Secure**, add the CardinalCommerce repository to your project-level `android/build.gradle`:

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

```bash
cd ios
pod install
```

---

# 2. Configure URL Scheme

Add a **Bundle URL Scheme** to your app.

### Using Xcode

1. Open your project in **Xcode**
2. Go to **Info**
3. Add a **URL Type**

### Required URL Scheme

```
{BUNDLE_IDENTIFIER}.braintree
```

### Example `Info.plist`

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

# 3. Create `ExpoBraintreeConfig.swift`

> **Note:** This step is only required if you use `requestVenmoNonce`.
> PayPal flows use `ASWebAuthenticationSession` which handles the return URL automatically.

Create a new Swift file in your Xcode project to handle Braintree URL callbacks:

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
This is auto-generated for Swift integration inside [withExpoBraintree.ios.ts](src/plugin/withExpoBraintree.ios.ts).

---

# 4. Update `AppDelegate.swift`

> **Note:** This step is only required if you use `requestVenmoNonce`.
> PayPal flows use `ASWebAuthenticationSession` which handles the return URL automatically.

Add the `application(_:open:options:)` method to handle Braintree context switch returns (required for Venmo):

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

All required Android and iOS configuration examples can be found in the example project directories:

- [example/android](example/android)
- [example/ios](example/ios)

---

# Summary

To integrate **react-native-expo-braintree v4.0.0** in a React Native Bare project:

1. Configure **Braintree App Links**
2. Add required **Android intent filters**
3. Initialize **3D Secure / Google Pay** in `MainActivity` (optional)
4. Add **3D Secure repository** if required
5. Configure **iOS URL Scheme**
6. Create **ExpoBraintreeConfig.swift**
7. Update **AppDelegate.swift** with `application(_:open:options:)`
8. Run `pod install`
