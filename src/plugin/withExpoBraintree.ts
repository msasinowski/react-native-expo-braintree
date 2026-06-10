import { type ConfigPlugin, createRunOncePlugin } from '@expo/config-plugins';
import {
  withExpoBraintreeAndroid,
  withExpoBraintreeAndroidGradle,
} from './withExpoBraintree.android';
import {
  withExpoBraintreePlist,
  withVenmoScheme,
  withExpoBraintreeAppDelegate,
  withBraintreeWrapperFile,
} from './withExpoBraintree.ios';

const pkg = require('react-native-expo-braintree/package.json');

export type ExpoBraintreePluginProps = {
  /**
   * Android AppLink host
   */
  host: string;
  /**
   * Android AppLink pathPrefix
   */
  pathPrefix?: string;
  /**
   * Set this flag to true if you need to use requestVenmoNonce which will be using fallbackUrlScheme
   * It will also add an intent-filter if it is not added before
   *  <intent-filter>
   *     <action android:name="android.intent.action.VIEW" />
   *     <category android:name="android.intent.category.DEFAULT" />
   *     <category android:name="android.intent.category.BROWSABLE" />
   *     <data android:scheme="${applicationId}.braintree" />
   *  </intent-filter>
   */
  addFallbackUrlScheme?: 'true' | 'false';
  /**
   * Flag that determines if we should initialize 3ds secure flow
   * It will also add an intent-filter if it is not added before
   *  <intent-filter>
   *     <action android:name="android.intent.action.VIEW" />
   *     <category android:name="android.intent.category.DEFAULT" />
   *     <category android:name="android.intent.category.BROWSABLE" />
   *     <data android:scheme="${applicationId}.braintree" />
   *  </intent-filter>
   */
  initialize3DSecure?: 'true' | 'false';
  /**
   * Flag that determines if we should initialize Google Pay
   */
  initializeGooglePay?: 'true' | 'false';
  /**
   * Flag that determines if we should initialize Venmo
   * Adds the Braintree URL scheme, LSApplicationQueriesSchemes entry,
   * ExpoBraintreeConfig.swift wrapper, and AppDelegate URL handler.
   */
  initializeVenmo?: 'true' | 'false';
};

export const withExpoBraintreePlugin: ConfigPlugin<ExpoBraintreePluginProps> = (
  expoConfig,
  props
) => {
  // Android mods
  let config = withExpoBraintreeAndroid(expoConfig, props);
  if (props?.initialize3DSecure === 'true') {
    config = withExpoBraintreeAndroidGradle(config);
  }
  // IOS mods
  if (props?.initializeVenmo === 'true') {
    config = withExpoBraintreeAppDelegate(config);
    config = withBraintreeWrapperFile(config);
    config = withExpoBraintreePlist(config);
    config = withVenmoScheme(config);
  }

  return config;
};

export default createRunOncePlugin(
  withExpoBraintreePlugin,
  pkg.name,
  pkg.version
);
