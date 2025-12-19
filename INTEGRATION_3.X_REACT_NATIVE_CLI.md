# React Native Bare Project (react-native-cli)

## Package Version 3.x.x

Please follow and Finish [Set Up App Links](https://github.com/braintree/braintree_android/blob/main/APP_LINK_SETUP.md), before the next steps.

### Android Specific

In Your `AndroidManifest.xml`, add this intent-filter to your main activity in `AndroidManifest.xml`
`braintree-example-app.web.app` should be replaced with your own domain that you defined in [Set Up App Links](https://github.com/braintree/braintree_android/blob/main/APP_LINK_SETUP.md)

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

Initialize, the Braintree, module. in your android/app/src/main/java/com/{app_name}/MainApplication.kt

```
import com.expobraintree.ExpoBraintreeModule

  override fun onCreate() {
    ...
    ExpoBraintreeModule.init()
    ...
  }

```

### iOS Specific

```bash
cd ios
pod install
```

#### Configure a new URL scheme

Add a bundle url scheme {BUNDLE_IDENTIFIER}.braintree in your app Info via XCode or manually in the Info.plist. In your Info.plist, you should have something like:

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

#### Update your code

Starting from version 3.2.0 of react-native-expo-braintree, additional setup steps are required for the iOS integration.

This change is necessary because, beginning with Braintree SDK v6, all iOS resources have been reimplemented in Swift. As a result, a custom Swift wrapper must be created to expose the required functionality for use within the AppDelegate.swift file.

It is assumed that after upgrading to React Native 0.79.x, the project no longer uses an AppDelegate.mm file and instead relies on a Swift-based AppDelegate. if you still use Rn below 0.77.x which is not really possible you can check 3.1.0 integration steps and applay them here.

- Open your React Native ios Project in xCode
- Create ExpoBraintreeConfig.swift in your project
- Put following content into ExpoBraintreeConfig.swift

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

- Update Content of you AppDelegate.swift

```swift
  func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
  ) -> Bool {
    let delegate = ReactNativeDelegate()
    let factory = RCTReactNativeFactory(delegate: delegate)
    delegate.dependencyProvider = RCTAppDependencyProvider()

    reactNativeDelegate = delegate
    reactNativeFactory = factory

    window = UIWindow(frame: UIScreen.main.bounds)

    factory.startReactNative(
      withModuleName: "ExpoBraintreeExample",
      in: window,
      launchOptions: launchOptions
    )

    return true
  }

    //  ADD THIS ONE TO INTEGRATE
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
  //  ADD THIS ONE TO INTEGRATE

```

The same steps are already implemented into example app, if you have any issues please check it.
