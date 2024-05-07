import { createRunOncePlugin, type ConfigPlugin } from '@expo/config-plugins';
import { withReactNativePaypalRebornAndroid } from './withReactNativePaypalReborn.android';
import {
  withReactNativePaypalRebornAppDelegate,
  withReactNativePaypalRebornPlist,
  withSwiftPaypalRebornWrapperFile,
} from './withReactNativePaypalReborn.ios';

const pkg = require('react-native-paypal-reborn/package.json');

export type ReactNativePaypalRebornPluginProps = {
  /**
   * xCode project name, used for importing the swift paypal reborn config header
   */
  xCodeProjectAppName: string;
};

export const withReactNativePaypalRebornPlugin: ConfigPlugin<
  ReactNativePaypalRebornPluginProps
> = (config, props) => {
  // Android mods
  config = withReactNativePaypalRebornAndroid(config);
  // IOS mods
  config = withSwiftPaypalRebornWrapperFile(config);
  config = withReactNativePaypalRebornAppDelegate(config, props);
  config = withReactNativePaypalRebornPlist(config);
  return config;
};

export default createRunOncePlugin(
  withReactNativePaypalRebornPlugin,
  pkg.name,
  pkg.version
);
