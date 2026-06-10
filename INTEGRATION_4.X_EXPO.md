## Integration

## Package Version 4.0.0 (Nitro Modules)

> **Required dependency:** `react-native-nitro-modules` must be installed alongside this library and configured in your project. It provides the Nitro Modules runtime that bridges native code.

```sh
npx expo install react-native-expo-braintree react-native-nitro-modules
```

### Expo Based Project (Expo SDK 53+)

Expo based project needs minimum integration from the app perspective.
In your `app.config.ts` or `app.config.json` or `app.config.js` add the expo-braintree plugin:

```javascript
plugins: [
  [
    "react-native-expo-braintree",
    {
      host: "braintree-example-app.web.app",
      pathPrefix: "/braintree-payments", // Optional
      initialize3DSecure: "true",         // Optional, if you use 3D Secure
      initializeGooglePay: "true",        // Optional, if you use Google Pay
      initializeVenmo: "true",            // Optional, if you use Venmo
      addFallbackUrlScheme: "true",       // Optional, needed for Android Venmo/3DSecure fallback
    },
  ],
];
```

### Plugin Options

| Option | Description |
| :----- | :---------- |
| `host` | Domain that serves `.well-known/assetlinks.json` for Android App Links |
| `pathPrefix` | Path prefix for context switch handling (Optional) |
| `initialize3DSecure` | Whether to initialize 3D Secure launcher (`"true"` / `"false"`) |
| `initializeGooglePay` | Whether to initialize Google Pay launcher (`"true"` / `"false"`) |
| `initializeVenmo` | Whether to initialize Venmo context switching on iOS (`"true"` / `"false"`) |
| `addFallbackUrlScheme` | Whether to add a fallback URL scheme for Android Venmo (`"true"` / `"false"`) |

### Android Specific

The plugin modifies `AndroidManifest.xml` to add intent filters for App Links and fallback URL scheme.
It also injects `initGooglePay()` and `initThreeDSecure()` calls into `MainActivity.onCreate()` if enabled.

### iOS Specific

If `initializeVenmo` is set to `"true"`, the plugin automatically modifies `AppDelegate.swift` to handle URL callbacks for Venmo, configures `Info.plist` schemes, and registers `ExpoBraintreeConfig.swift`. No manual code modifications or Xcode configurations are required.

[withExpoBraintree.android.ts](src/plugin/withExpoBraintree.android.ts)
