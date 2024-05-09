import { createRunOncePlugin, type ConfigPlugin } from '@expo/config-plugins';
import { withExpoBraintreeAndroid } from './withExpoBraintree.android';
import {
  withExpoBraintreeAppDelegate,
  withExpoBraintreePlist,
  withSwiftBraintreeWrapperFile,
} from './withExpoBraintree.ios';

const pkg = require('react-native-expo-braintree/package.json');

export type ExpoBraintreePluginProps = {
  /**
   * xCode project name, used for importing the swift expo braintree config header
   */
  xCodeProjectAppName: string;
};

export const withExpoBraintreePlugin: ConfigPlugin<ExpoBraintreePluginProps> = (
  config,
  props
) => {
  // Android mods
  config = withExpoBraintreeAndroid(config);
  // IOS mods
  config = withSwiftBraintreeWrapperFile(config);
  config = withExpoBraintreeAppDelegate(config, props);
  config = withExpoBraintreePlist(config);
  return config;
};

export default createRunOncePlugin(
  withExpoBraintreePlugin,
  pkg.name,
  pkg.version
);
