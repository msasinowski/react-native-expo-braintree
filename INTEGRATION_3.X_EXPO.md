## Integration
## Package Version 3.x.x
### Expo Based Project (EXPO SDK 50 and EXPO SDK 51) 
From version 2.1.1 of the package, react-native-expo-braintree added a possibility to use the package into expo based project, without need to eject from the expo. Special expo plugin was added into the source of the package which can be used. in any expo project.

Expo based project needs minimum integration from the app perspective.
In Your `app.config.ts` or `app.config.json` or `app.config.js` please add expo-braintree plugin into plugins section.
```javascript
...
  plugins: [
    [
      "react-native-expo-braintree",
      {
        xCodeProjectAppName: "xCodeProjectAppName",
        host: "braintree-example-app.web.app",
      },
    ],
...
```
`xCodeProjectAppName` - Name of your xCode project in case of this repository, for example app  it will be `ExpoBraintreeExample`

`host` - Domain that provide a .well-known/applinks.json, it need to be the same as it is defined in [Set Up App Links](https://github.com/braintree/braintree_android/blob/main/APP_LINK_SETUP.md)

`pathPrefix` - Path prefix, in case of you want to separate path only to handle the context switch (Optional)

#### Android Specific
Currently expo-plugin written for making changes into Android settings files, using one danger modifiers from expo-config-plugins called `withMainActivity`

[Plugin Code ](src/plugin/withExpoBraintree.android.ts)


#### iOS Specific
Currently expo-plugin written for making changes into IOS settings files, using one danger modifier from expo-config-plugins called `withAppDelegate`

[Plugin Code ](src/plugin/withExpoBraintree.ios.ts)