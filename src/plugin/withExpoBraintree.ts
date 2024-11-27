import { type ConfigPlugin, createRunOncePlugin } from '@expo/config-plugins';
import {
  withExpoBraintreeAndroid,
  withExpoBraintreeAndroidGradle,
} from './withExpoBraintree.android';
import {
  withExpoBraintreeAppDelegate,
  withExpoBraintreePlist,
  withSwiftBraintreeWrapperFile,
  withVenmoScheme,
} from './withExpoBraintree.ios';

const pkg = require('react-native-expo-braintree/package.json');

export type ExpoBraintreePluginProps = {
  /**
   * xCode project name, used for importing the swift expo braintree config header
   */
  xCodeProjectAppName: string;

  /**
   * Android AppLink host
   */
  host: string;

  /**
   * Android AppLink pathPrefix
   */
  pathPrefix?: string;

  /**
   * Boolean that determines if PayPal is used/needed (Values "true" | "false")
   */
  initializePayPal?: string;
  /**
   * Boolean that determines if Venmo is used/needed (Values "true" | "false")
   */
  initializeVenmo?: string;
  /**
   * Boolean that determines if 3D Secure is used/needed (Values "true" | "false")
   */
  initialize3DSecure?: string;
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
  config = withSwiftBraintreeWrapperFile(config);
  config = withExpoBraintreeAppDelegate(config, props);
  config = withExpoBraintreePlist(config);
  config = withVenmoScheme(config);

  return config;
};

export default createRunOncePlugin(
  withExpoBraintreePlugin,
  pkg.name,
  pkg.version
);
