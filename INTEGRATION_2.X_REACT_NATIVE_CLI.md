# React Native Bare Project (react-native-cli)
## Package Version 2.x.x

### Android Specific

In Your `AndroidManifest.xml`, `android:allowBackup="false"` can be replaced `android:allowBackup="true"`, it is responsible for app backup.

Also, add this intent-filter to your main activity in `AndroidManifest.xml`

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
From version 2.0.0 of the expo-braintree, for the IOS part of the setup there is need to make few more additional steps to integrate the library into your project. The reason is that Braintree SDk from version v6, reimplement all the IOS resources to use swift. Because of that we not longer can use Braintree Header files into AppDelegate.m file. And we need to create our own swift wrapper that can be accessible in AppDelegate.m file.

- Open your React Native ios Project in xCode
- Create ExpoBraintreeConfig.swift in your project, while creating the .swift file xCode will ask if you want to automatically create your {AppName}-Bridging-Header.h - Allow that
- Put following content into ExpoBraintreeConfig.swift

```swift
import Braintree
import Foundation

@objc public class ExpoBraintreeConfig: NSObject {

  @objc(configure)
  public static func configure() {
    BTAppContextSwitcher.sharedInstance.returnURLScheme = self.getPaymentUrlScheme()
  }

  @objc(getPaymentUrlScheme)
  public static func getPaymentUrlScheme() -> String {
    let bundleIdentifier = Bundle.main.bundleIdentifier ?? ""
    return bundleIdentifier + ".braintree"
  }

  @objc(handleUrl:)
  public static func handleUrl(url: URL) -> Bool {
    return BTAppContextSwitcher.sharedInstance.handleOpen(url)
  }
}
```
- Update Content of you AppDelegate.m


```objective-c
#import "{AppName}}-Swift.h"
#import <React/RCTLinkingManager.h>

...
- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    ...
    [ExpoBraintreeConfig configure];
}

- (BOOL)application:(UIApplication *)application
            openURL:(NSURL *)url
            options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options {

    if ([url.scheme localizedCaseInsensitiveCompare:[ExpoBraintreeConfig getPaymentUrlScheme]] == NSOrderedSame) {
        return [ExpoBraintreeConfig handleUrl:url];
    }
    
    return [RCTLinkingManager application:application openURL:url options:options];
}

```
The same steps are already implemented into example app, if you have any issues please check it.