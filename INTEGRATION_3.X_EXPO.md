## Integration

## Package Version 3.x.x

> [!WARNING]
> **Deprecated Version**
> This guide is for the legacy **3.x.x** version of the library, which is no longer actively supported.
> It is highly recommended to migrate your project to **4.x.x** (which features a full native rewrite powered by JSI Nitro Modules). Refer to the corresponding 4.x configuration and usage guides:
> - Expo integration: [INTEGRATION_4.X_EXPO.md](INTEGRATION_4.X_EXPO.md)
> - Bare React Native integration: [INTEGRATION_4.X_REACT_NATIVE_CLI.md](INTEGRATION_4.X_REACT_NATIVE_CLI.md)
> - Usage reference: [USAGE_4.X.md](USAGE_4.X.md)

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

`xCodeProjectAppName` - Name of your xCode project in case of this repos
