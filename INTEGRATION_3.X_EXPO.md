## Integration

## Package Version 3.x.x

### Expo Based Project (EXPO SDK 53+)

Expo based project needs minimum integration from the app perspective.
In Your `app.config.ts` or `app.config.json` or `app.config.js` please add expo-braintree plugin into plugins section.

```javascript
...
  plugins: [
    [
      "react-native-expo-braintree",
      {
        xCodeProjectAppName: "xCodeProjectAppName", // Optional if you are still using AppDelegate.mm / AppDelegate.m
        host: "braintree-example-app.web.app",
        pathPrefix: "/braintree-payments" // Optional,
        // Depending on which payment do you really need in the project initialize only required one
        initialize3DSecure: "true",
        initializeGooglePay: "true",
        addFallbackUrlScheme: "true",
        appDelegateLanguage?: "swift"; // Optional if you are still using AppDelegate.mm / AppDelegate.m
      },
    ],
...
```

`xCodeProjectAppName` - Name of your xCode project in case of this repository, for example app it will be `ExpoBraintreeExample` (Optional)

`host` - Domain that provide a .well-known/applinks.json, it need to be the same as it is defined in [Set Up App Links](https://github.com/braintree/braintree_android/blob/main/APP_LINK_SETUP.md)

`pathPrefix` - Path prefix, in case of you want to separate path only to handle the context switch (Optional)
`initialize3DSecure` - Boolean that determines if 3D Secure is used/needed (Values "true" | "false")
`initializeGooglePay` - Boolean that determines if Google Pay is used/needed (Values "true" | "false")
`addFallbackUrlScheme` - Boolean that determines if we should add a scheme for a fallback url used in venmo
`appDelegateLanguage` - Indicator that tell's the plugin logic if you are still using Objective C file for AppDelegate (Optional)

#### Android Specific

Currently expo-plugin written for making changes into Android settings files, using one danger modifiers from expo-config-plugins called `withMainActivity`

[Plugin Code ](src/plugin/withExpoBraintree.android.ts)

#### iOS Specific

Currently expo-plugin written for making changes into IOS settings files, using one danger modifier from expo-config-plugins called `withAppDelegate`

[Plugin Code ](src/plugin/withExpoBraintree.ios.ts)
