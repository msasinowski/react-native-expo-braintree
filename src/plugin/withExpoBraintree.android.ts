import {
  AndroidConfig,
  type ConfigPlugin,
  withAndroidManifest,
  withMainActivity,
} from '@expo/config-plugins';
import type {
  ManifestIntentFilter,
  StringBoolean,
} from '@expo/config-plugins/build/android/Manifest';
import { addImports } from '@expo/config-plugins/build/android/codeMod';
import { mergeContents } from '@expo/config-plugins/build/utils/generateCode';

interface IntentFilterProps {
  host: string;
  pathPrefix?: string;
}

const { getMainActivityOrThrow } = AndroidConfig.Manifest;

export const withExpoBraintreeAndroid: ConfigPlugin<IntentFilterProps> = (
  expoConfig,
  { host, pathPrefix }
) => {
  let newConfig = withAndroidManifest(expoConfig, (config) => {
    config.modResults = addPaypalAppLinks(config.modResults, host, pathPrefix);
    return config;
  });

  newConfig = withMainActivity(expoConfig, (config) => {
    const { modResults } = config;
    const { language } = modResults;

    const withImports = addImports(
      modResults.contents,
      ['com.expobraintree.ExpoBraintreeModule'],
      language === 'java'
    );

    const withInit = mergeContents({
      src: withImports,
      comment: '    // add BraintreeModule import',
      tag: 'braintree-module-init',
      offset: 1,
      anchor: /(?<=^.*super\.onCreate.*$)/m,
      newSrc: `     ExpoBraintreeModule.init()${language === 'java' ? ';' : ''}`,
    });

    return {
      ...config,
      modResults: {
        ...modResults,
        contents: withInit.contents,
      },
    };
  });

  return newConfig;
};

// Add new intent filter for App Links
// <activity>
//   ...
//   <intent-filter android:autoVerify="true">
//     <action android:name="android.intent.action.VIEW" />
//     <category android:name="android.intent.category.DEFAULT" />
//     <category android:name="android.intent.category.BROWSABLE" />
//        <data android:scheme="http" />
//        <data android:scheme="https" />
//        <data android:host="myownpersonaldomain.com" />
//        <data android:pathPrefix="/braintree-payments"/>
//   </intent-filter>
// </activity>;

export const addPaypalAppLinks = (
  modResults: AndroidConfig.Manifest.AndroidManifest,
  host: string,
  pathPrefix?: string
): AndroidConfig.Manifest.AndroidManifest => {
  const mainActivity = getMainActivityOrThrow(modResults);
  const intentFilters = mainActivity['intent-filter'];

  if (!host) {
    throw Error(
      'No Host provided for withExpoBraintree.android addPaypalAppLinks'
    );
  }

  // Check if the intent filter already exists
  if (hasIntentFilter(intentFilters, host, pathPrefix)) {
    console.warn(
      'withExpoBraintreeAndroid: AndroidManifest not require any changes'
    );
    return modResults;
  }

  const newIntentFilter: ManifestIntentFilter = {
    action: [
      {
        $: {
          'android:name': 'android.intent.action.VIEW',
        },
      },
    ],
    category: [
      {
        $: {
          'android:name': 'android.intent.category.DEFAULT',
        },
      },
      {
        $: {
          'android:name': 'android.intent.category.BROWSABLE',
        },
      },
    ],
    data: [
      {
        $: {
          'android:scheme': 'http',
        },
      },
      {
        $: {
          'android:scheme': 'https',
        },
      },
      {
        $: {
          'android:host': host,
        },
      },
    ],
    $: {
      'android:autoVerify': 'true' as StringBoolean,
    },
  };
  // If there is pathPrefix then we add that
  if (pathPrefix) {
    newIntentFilter.data?.push({
      $: {
        'android:pathPrefix': pathPrefix,
      },
    });
  }

  // Add the intent-filter to the main activity
  mainActivity['intent-filter'] = [
    ...(mainActivity['intent-filter'] || []),
    newIntentFilter,
  ];
  return modResults;
};

/**
 * Check if an intent-filter with the same data already exists
 * @param {object} intentFilters - The AndroidManifest intent filters
 * @param {string} host - The host to check
 * @param {string} pathPrefix - The pathPrefix to check
 * @returns {boolean} - Returns true if a matching intent filter is found
 */
function hasIntentFilter(
  intentFilters: AndroidConfig.Manifest.ManifestIntentFilter[] | undefined,
  host: string,
  pathPrefix?: string
) {
  return intentFilters?.some((filter) => {
    const hasAutoVerify = filter.$ && filter.$['android:autoVerify'] === 'true';
    const hasViewAction = filter.action?.some(
      (action) => action.$['android:name'] === 'android.intent.action.VIEW'
    );
    const hasDefaultCategory = filter.category?.some(
      (category) =>
        category.$['android:name'] === 'android.intent.category.DEFAULT'
    );
    const hasBrowsableCategory = filter.category?.some(
      (category) =>
        category.$['android:name'] === 'android.intent.category.BROWSABLE'
    );

    const hasHttpScheme = filter.data?.some(
      (data) => data.$['android:scheme'] === 'http'
    );
    const hasHttpsScheme = filter.data?.some(
      (data) => data.$['android:scheme'] === 'https'
    );
    const hasMatchingHost = filter.data?.some(
      (data) => data.$['android:host'] === host
    );
    const hasMatchingPathPrefix = filter.data?.some(
      (data) => data.$['android:pathPrefix'] === pathPrefix
    );

    // Check all conditions to ensure it matches the intent filter we want to add
    return (
      hasAutoVerify &&
      hasViewAction &&
      hasDefaultCategory &&
      hasBrowsableCategory &&
      hasHttpScheme &&
      hasHttpsScheme &&
      hasMatchingHost &&
      (hasMatchingPathPrefix || !pathPrefix)
    );
  });
}
