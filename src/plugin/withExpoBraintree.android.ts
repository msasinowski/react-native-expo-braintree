import {
  AndroidConfig,
  type ConfigPlugin,
  withAndroidManifest,
  withMainActivity,
  withProjectBuildGradle,
} from '@expo/config-plugins';
import type {
  ManifestIntentFilter,
  StringBoolean,
} from '@expo/config-plugins/build/android/Manifest';
import { addImports } from '@expo/config-plugins/build/android/codeMod';
import {
  mergeContents,
  createGeneratedHeaderComment,
  type MergeResults,
  removeGeneratedContents,
} from '@expo/config-plugins/build/utils/generateCode';

interface IntentFilterProps {
  host: string;
  pathPrefix?: string;
  initializePayPal?: string;
  initializeVenmo?: string;
  initialize3DSecure?: string;
}

const { getMainActivityOrThrow } = AndroidConfig.Manifest;

export const withExpoBraintreeAndroid: ConfigPlugin<IntentFilterProps> = (
  expoConfig,
  { host, pathPrefix, initialize3DSecure, initializePayPal, initializeVenmo }
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
    let newSrc = [];
    if (initializePayPal === 'true') {
      newSrc.push(
        `   ExpoBraintreeModule.init()${language === 'java' ? ';' : ''}`
      );
    }
    if (initializeVenmo === 'true') {
      newSrc.push(
        `   ExpoBraintreeModule.initVenmo()${language === 'java' ? ';' : ''}`
      );
    }
    if (initialize3DSecure === 'true') {
      newSrc.push(
        `   ExpoBraintreeModule.initThreeDSecure(this)${language === 'java' ? ';' : ''}`
      );
    }
    const withInit = mergeContents({
      src: withImports,
      comment: '    // add BraintreeModule import',
      tag: 'braintree-module-init',
      offset: 1,
      anchor: /(?<=^.*super\.onCreate.*$)/m,
      newSrc: newSrc.join('\n'),
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
// Because we need the package to be added AFTER the React and Google maven packages, we create a new all projects.
// It's ok to have multiple all projects.repositories, so we create a new one since it's cheaper than tokenizing
// the existing block to find the correct place to insert our content.
const gradle3DSecureBraintreeRepo = [
  `allprojects {`,
  ` repositories {`,
  `   maven {`,
  `     url "https://cardinalcommerceprod.jfrog.io/artifactory/android"`,
  `     credentials {`,
  `       username 'braintree_team_sdk'`,
  `       password 'AKCp8jQcoDy2hxSWhDAUQKXLDPDx6NYRkqrgFLRc3qDrayg6rrCbJpsKKyMwaykVL8FWusJpp'`,
  `     }`,
  `   }`,
  `  }`,
  `}`,
].join('\n');

export const withExpoBraintreeAndroidGradle: ConfigPlugin = (expoConfig) => {
  return withProjectBuildGradle(expoConfig, (config) => {
    if (config.modResults.language === 'groovy') {
      config.modResults.contents = appendContents({
        tag: 'expo-braintree-import',
        src: config.modResults.contents,
        newSrc: gradle3DSecureBraintreeRepo,
        comment: '//',
      }).contents;
    } else {
      throw new Error(
        'Cannot add expo-braintree-import maven gradle because the build.gradle is not groovy'
      );
    }
    return config;
  });
};

export const appendContents = ({
  src,
  newSrc,
  tag,
  comment,
}: {
  src: string;
  newSrc: string;
  tag: string;
  comment: string;
}): MergeResults => {
  const header = createGeneratedHeaderComment(newSrc, tag, comment);
  if (!src.includes(header)) {
    // Ensure the old generated contents are removed.
    const sanitizedTarget = removeGeneratedContents(src, tag);
    const contentsToAdd = [
      // @something
      header,
      // contents
      newSrc,
      // @end
      `${comment} @generated end ${tag}`,
    ].join('\n');

    return {
      contents: sanitizedTarget ?? src + contentsToAdd,
      didMerge: true,
      didClear: !!sanitizedTarget,
    };
  }
  return { contents: src, didClear: false, didMerge: false };
};
