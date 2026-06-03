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
      addFallbackUrlScheme: "true",       // Optional, needed for Venmo
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
| `addFallbackUrlScheme` | Whether to add a fallback URL scheme for Venmo (`"true"` / `"false"`) |

### Android Specific

The plugin modifies `AndroidManifest.xml` to add intent filters for App Links and fallback URL scheme.
It also injects `initGooglePay()` and `initThreeDSecure()` calls into `MainActivity.onCreate()` if enabled.

### iOS Specific

No AppDelegate modifications needed — Nitro Modules handles native module registration automatically.
You still need to configure your URL scheme in Xcode (see CLI integration guide for details).

[Plugin Source Code](src/plugin/withExpoBraintree.android.ts)
