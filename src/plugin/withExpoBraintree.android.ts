import {
  AndroidConfig,
  type ConfigPlugin,
  withAndroidManifest,
  withMainActivity,
  WarningAggregator,
} from '@expo/config-plugins';
import type {
  ManifestIntentFilter,
  StringBoolean,
} from '@expo/config-plugins/build/android/Manifest';
import { addImports } from '@expo/config-plugins/build/android/codeMod';
import { mergeContents } from '@expo/config-plugins/build/utils/generateCode';

interface WithExpoBraintreeAndroidProps {
  host: string;
  pathPrefix?: string;
  fallbackUrlScheme?: string;
}

const { getMainActivityOrThrow } = AndroidConfig.Manifest;

export const withExpoBraintreeAndroid: ConfigPlugin<
  WithExpoBraintreeAndroidProps
> = (expoConfig, { host, pathPrefix, fallbackUrlScheme }) => {
  let newConfig = withAndroidManifest(expoConfig, (config) => {
    config.modResults = addBraintreeLinks(
      config.modResults,
      host,
      pathPrefix,
      fallbackUrlScheme
    );
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
//     <data android:scheme="http" />
//     <data android:scheme="https" />
//     <data android:host="myownpersonaldomain.com" />
//     <data android:pathPrefix="/braintree-payments"/>
//   </intent-filter>
// </activity>;

// If you provide a fallbackUrlScheme it will also add a new intent filter for that
// <activity>
//   ...
//   <intent-filter>
//     <action android:name="android.intent.action.VIEW" />
//     <category android:name="android.intent.category.DEFAULT" />
//     <category android:name="android.intent.category.BROWSABLE" />
//        <data android:scheme="{fallbackUrlScheme}" />
//   </intent-filter>
// </activity>;

export const addBraintreeLinks = (
  modResults: AndroidConfig.Manifest.AndroidManifest,
  host: string,
  pathPrefix?: string,
  fallbackUrlScheme?: string
): AndroidConfig.Manifest.AndroidManifest => {
  const mainActivity = getMainActivityOrThrow(modResults);
  const intentFilters = mainActivity['intent-filter'];

  // Host is required props for a plugin
  if (!host) {
    WarningAggregator.addWarningAndroid(
      'withExpoBraintree addBraintreeLinks',
      `No Host provided for withExpoBraintree.android addBraintreeLinks`
    );
  }

  // If there was a fallbackUrlScheme but it is not including .braintree at the end we thrown an error
  if (!!fallbackUrlScheme && !fallbackUrlScheme?.endsWith('.braintree')) {
    WarningAggregator.addWarningAndroid(
      'withExpoBraintree addBraintreeLinks',
      `{fallbackUrlScheme} provided but fallback url does not end with .braintree which is required`
    );
  }

  // Check if the intent filter already exists
  if (hasIntentFilter(intentFilters, host, pathPrefix)) {
    WarningAggregator.addWarningAndroid(
      'withExpoBraintree addBraintreeLinks',
      `withExpoBraintreeAndroid: AndroidManifest not require any changes`
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

  const newFallbackUrlSchemeIntentFilter: ManifestIntentFilter = {
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
          'android:scheme': fallbackUrlScheme,
        },
      },
    ],
  };

  // Add the intent-filter to the main activity
  mainActivity['intent-filter'] = [
    ...(mainActivity['intent-filter'] || []),
    newIntentFilter,
  ];

  // If there is fallbackUrlScheme then we add that
  if (fallbackUrlScheme) {
    // Add the intent-filter to the main activity
    mainActivity['intent-filter'] = [
      ...(mainActivity['intent-filter'] || []),
      newFallbackUrlSchemeIntentFilter,
    ];
  }
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
