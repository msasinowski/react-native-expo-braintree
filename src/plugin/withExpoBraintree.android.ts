import {
  withAndroidManifest,
  AndroidConfig,
  type ConfigPlugin,
  withProjectBuildGradle,
} from '@expo/config-plugins';
import {
  createGeneratedHeaderComment,
  type MergeResults,
  removeGeneratedContents,
} from '@expo/config-plugins/build/utils/generateCode';

// Because we need the package to be added AFTER the React and Google maven packages, we create a new all projects.
// It's ok to have multiple all projects.repositories, so we create a new one since it's cheaper than tokenizing
// the existing block to find the correct place to insert our camera maven.
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

const { getMainActivityOrThrow } = AndroidConfig.Manifest;

export const withExpoBraintreeAndroid: ConfigPlugin = (expoConfig) => {
  return withAndroidManifest(expoConfig, (config) => {
    config.modResults = addPaypalIntentFilter(config.modResults);
    return config;
  });
};

type ManifestData = {
  $: {
    [key: string]: string | undefined;
    'android:host'?: string;
    'android:pathPrefix'?: string;
    'android:scheme'?: string;
  };
};

// Add new intent filter
// <activity>
//   ...
//   <intent-filter>
//     <action android:name="android.intent.action.VIEW" />
//     <category android:name="android.intent.category.DEFAULT" />
//     <category android:name="android.intent.category.BROWSABLE" />
//     <data android:scheme="${applicationId}.braintree" />
//   </intent-filter>
// </activity>;
const intentActionView = 'android.intent.action.VIEW';
const intentCategoryDefault = 'android.intent.category.DEFAULT';
const intentCategoryBrowsable = 'android.intent.category.BROWSABLE';
const intentDataBraintree = '${applicationId}.braintree';

export const addPaypalIntentFilter = (
  modResults: AndroidConfig.Manifest.AndroidManifest
): AndroidConfig.Manifest.AndroidManifest => {
  const mainActivity = getMainActivityOrThrow(modResults);
  // We want always to add the data to the first intent filter
  const intentFilters = mainActivity['intent-filter'];
  if (!intentFilters?.length) {
    console.warn(
      'withExpoBraintreeAndroid.addPaypalIntentFilter: No .Intent Filters'
    );
    return modResults;
  }
  const {
    isIntentActionExist,
    isIntentCategoryBrowsableExist,
    isIntentCategoryDefaultExist,
    isIntentDataBraintreeExist,
  } = checkAndroidManifestData(intentFilters);

  if (
    isIntentActionExist &&
    isIntentCategoryBrowsableExist &&
    isIntentCategoryDefaultExist &&
    isIntentDataBraintreeExist
  ) {
    console.warn(
      'withExpoBraintreeAndroid: AndroidManifest not require any changes'
    );
    return modResults;
  }
  intentFilters.push({
    action: [
      {
        $: { 'android:name': intentActionView },
      },
    ],
    category: [
      { $: { 'android:name': intentCategoryDefault } },
      { $: { 'android:name': intentCategoryBrowsable } },
    ],
    data: [{ $: { 'android:scheme': '${applicationId}.braintree' } }],
  });
  return modResults;
};

const checkAndroidManifestData = (
  intentFilters: AndroidConfig.Manifest.ManifestIntentFilter[]
) => ({
  isIntentActionExist: isElementInAndroidManifestExist(
    intentFilters,
    intentActionView,
    'action'
  ),
  isIntentCategoryDefaultExist: isElementInAndroidManifestExist(
    intentFilters,
    intentCategoryDefault,
    'category'
  ),
  isIntentCategoryBrowsableExist: isElementInAndroidManifestExist(
    intentFilters,
    intentCategoryBrowsable,
    'category'
  ),
  isIntentDataBraintreeExist: isElementInAndroidManifestExist(
    intentFilters,
    intentDataBraintree,
    'data'
  ),
});

const isElementInAndroidManifestExist = (
  intentFilters: AndroidConfig.Manifest.ManifestIntentFilter[] | undefined,
  value: string,
  type: 'action' | 'data' | 'category'
) =>
  !!intentFilters?.some((intentFilter) =>
    intentFilter[type]?.find((item) => {
      switch (type) {
        case 'action':
        case 'category':
          return item.$['android:name'] === value;
        case 'data':
          const typedItem = item as ManifestData;
          return typedItem.$['android:scheme'] === value;
      }
    })
  );

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
