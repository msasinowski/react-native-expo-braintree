import { createRunOncePlugin, type ConfigPlugin } from '@expo/config-plugins';
import { withExpoBraintreeAndroid } from './withExpoBraintree.android';
import {
  withExpoBraintreeAppDelegate,
  withExpoBraintreePlist,
  withSwiftPaypalRebornWrapperFile,
} from './withExpoBraintree.ios';

const pkg = require('expo-braintree/package.json');

export type ExpoBraintreePluginProps = {
  /**
   * xCode project name, used for importing the swift paypal reborn config header
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
  config = withSwiftPaypalRebornWrapperFile(config);
  config = withExpoBraintreeAppDelegate(config, props);
  config = withExpoBraintreePlist(config);
  return config;
};

export default createRunOncePlugin(
  withExpoBraintreePlugin,
  pkg.name,
  pkg.version
);
