# react-native-paypal-reborn

## Getting started
React Native Paypal Reborn package is a pure native implementation of Braintree SDK
https://developer.paypal.com/braintree/docs/start/overview

| React Native Paypal reborn Version | Braintree Android SDK | Braintree IOS SDK | Minimum SDK Android | Minimum SDK IOS |
| :--------------------------------: | :-------------------: | :---------------: | :-----------------: | :-------------: |
|               0.0.1                |         v3.x          |       v5.x        |         21         |      13.0       |
|               0.1.0                |         v3.x          |       v5.x        |         21          |      13.0       |
|               1.0.0                |        v4.2.x         |       v5.x        |         21          |      13.0       |
|               1.1.0                |        v4.41.x        |       v5.x        |         21          |      13.0       |
|               2.0.0                |        v4.41.x        |      v6.17.0      |         21          |      14.0       |



## Android Specific

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

## iOS Specific
```bash
cd ios
pod install
```
###### Configure a new URL scheme
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
###### Update your code
From version 2.0.0 of the react-native-paypal-reborn, for the IOS part of the setup there is need to make few more additional steps to integrate the library into your project. The reason is that Braintree SDk from version v6, reimplement all the IOS resources to use swift. Because of that we not longer can use Braintree Header files into AppDelegate.m file. And we need to create our own swift wrapper that can be accessible in AppDelegate.m file.

- Open your React Native ios Project in xCode
- Create PaypalRebornConfig.swift in your project, while creating the .swift file xCode will ask if you want to automatically create your {AppName}-Bridging-Header.h - Allow that
- Put following content into PaypalRebornConfig.swift

```swift
import Braintree
import Foundation

@objc public class PaypalRebornConfig: NSObject {

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
    [PaypalRebornConfig configure];
}

- (BOOL)application:(UIApplication *)application
            openURL:(NSURL *)url
            options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options {

    if ([url.scheme localizedCaseInsensitiveCompare:[PaypalRebornConfig getPaymentUrlScheme]] == NSOrderedSame) {
        return [PaypalRebornConfig handleUrl:url];
    }
    
    return [RCTLinkingManager application:application openURL:url options:options];
}

```
The same steps are already implemented into example app, if you have any issues please check it.

## Usage

##### Request One Time Payment

```javascript
import {
  requestOneTimePayment,
} from "react-native-paypal-reborn";

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
} from "react-native-paypal-reborn";

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
} from "react-native-paypal-reborn";

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
} from "react-native-paypal-reborn";

const result: string = await getDeviceDataFromDataCollector("Token")

```

## TODO

- [ ] Add Missing Methods from Braintree SDK ApplePay, Google Pay, 3D
