# react-native-expo-braintree
This package is a continuation of  https://www.npmjs.com/package/react-native-paypal-reborn with changed more generic name and all the updates will be under the new react-native-expo-braintree package.
## Important Information
Please note that, 2.0.0 version of the library is rewritten from the scratch using Kotlin (previously Java) and Swift (previously Objective-C) to prepare the whole codebase to migrate it into expo package at some point. If you find any problem or issue in the package do not hesitate and report it via Issue panel.

1.1.0 is stable and well checked version, if you do not need to use v6 IOS Braintree SDK, or have some lower version of minimum (Android or IOS) SDK please use versions 1.x.x.


## Getting started
React Native Expo Braintree package is a pure native implementation of Braintree SDK
https://developer.paypal.com/braintree/docs/start/overview

| React Native Paypal reborn Version | Braintree Android SDK | Braintree IOS SDK | Minimum SDK Android | Minimum SDK IOS |
| :--------------------------------: | :-------------------: | :---------------: | :-----------------: | :-------------: |
|               0.0.1                |         v3.x          |       v5.x        |         21          |      13.0       |
|               0.1.0                |         v3.x          |       v5.x        |         21          |      13.0       |
|               1.0.0                |        v4.2.x         |       v5.x        |         21          |      13.0       |
|               1.1.0                |        v4.41.x        |       v5.x        |         21          |      13.0       |
|               2.0.1                |        v4.41.x        |      v6.17.0      |         21          |      14.0       |
|               2.1.1                |        v4.41.x        |      v6.17.0      |         21          |      14.0       |

| React Native Expo Braintree Version | Braintree Android SDK | Braintree IOS SDK | Minimum SDK Android | Minimum SDK IOS |
| :--------------------: | :-------------------: | :---------------: | :-----------------: | :-------------: |
|         2.2.0          |        v4.41.x        |      v6.17.0      |         21          |      14.0       |
|         2.2.1          |        v4.41.x        |      v6.17.0      |         21          |      14.0       |
|         2.2.2          |        v4.41.x        |      v6.17.0      |         21          |      14.0       |

## Integration
### Expo Based Project (expo SDK 50) (Alpha)
From version 2.1.1 of the package, expo-braintree added a possibility to use the package into expo based project, without need to eject from the expo. Special expo plugin was added into the source of the package which can be used. in any expo project.

Expo based project needs minimum integration from the app perspective.
In Your `app.config.ts` or `app.config.json` or `app.config.js` please add expo-braintree plugin into plugins section.
```javascript
...
  plugins: [
    [
      "react-native-expo-braintree",
      {
        xCodeProjectAppName: "xCodeProjectAppName",
      },
    ],
...
```
`xCodeProjectAppName` - Name of your xCode project in case of example app in this repository it will be `ExpoBraintreeExample`

#### Android Specific
Currently expo-plugin written for making changes into Android settings files, using non danger modifiers from expo-config-plugins

#### iOS Specific
Currently expo-plugin written for making changes into IOS settings files, using one danger modifier from expo-config-plugins called `withAppDelegate`
### React Native Bare Project (react-native-cli)

#### Android Specific

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

#### iOS Specific
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

## TODO

- [ ] Add Missing Methods from Braintree SDK ApplePay, Google Pay, 3D
- [x] Based on swift and kotlin implementation create expo working version library 
- [x] rename the package to react-native-expo-braintree 
