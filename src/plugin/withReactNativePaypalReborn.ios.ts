/* eslint-disable no-bitwise */
import {
  withAppDelegate,
  withInfoPlist,
  type ConfigPlugin,
  IOSConfig,
} from '@expo/config-plugins';
import eol from 'eol';
import type { ReactNativePaypalRebornPluginProps } from './withReactNativePaypalReborn';

export const withReactNativePaypalRebornAppDelegate: ConfigPlugin<
  ReactNativePaypalRebornPluginProps
> = (expoConfig, { xCodeProjectAppName }) => {
  return withAppDelegate(expoConfig, (config) => {
    const appDelegate = config.modResults;
    let contents = eol.split(appDelegate.contents);
    // Step 1 Edit Import part
    // Editing import part for -swift.h file to be able to use Braintree
    const importSwiftHeaderFileContent = `#import "${xCodeProjectAppName}-Swift.h"`;
    const importSwiftHeaderFileIndex = contents.findIndex((content) =>
      content.includes(importSwiftHeaderFileContent)
    );
    // If importSwiftHeaderFileContent do not exist in AppDelegate.mm
    if (!~importSwiftHeaderFileIndex) {
      contents = [importSwiftHeaderFileContent, ...contents];
    }
    const importExpoModulesSwiftHeader = `#import "ExpoModulesCore-Swift.h"`;
    const importExpoModulesSwiftHeaderFileIndex = contents.findIndex(
      (content) => content.includes(importExpoModulesSwiftHeader)
    );
    // If importExpoModulesSwiftHeader do not exist in AppDelegate.mm
    if (!~importExpoModulesSwiftHeaderFileIndex) {
      contents = [importExpoModulesSwiftHeader, ...contents];
    }
    // Step 2 Add configure method in didFinishLaunchingWithOptions
    const didFinishLaunchingWithOptions = 'didFinishLaunchingWithOptions';
    const payPalRebornConfigureLine = '  [PaypalRebornConfig configure];';
    let didFinishLaunchingWithOptionsElementIndex = contents.findIndex(
      (content) => content.includes(didFinishLaunchingWithOptions)
    );
    const payPalRebornConfigureLineIndex = contents.findIndex((content) =>
      content.includes(payPalRebornConfigureLine)
    );
    // If didFinishLaunchingWithOptions exist in AppDelegate.mm and payPalRebornConfigureLine do not exist
    if (
      !~payPalRebornConfigureLineIndex &&
      !!~didFinishLaunchingWithOptionsElementIndex
    ) {
      contents.splice(
        // We are adding +2 to the index to insert content after '{' block
        didFinishLaunchingWithOptionsElementIndex + 2,
        0,
        payPalRebornConfigureLine
      );
    }
    // Step 3 Add method to properly handle openUrl method in AppDelegate.m
    const openUrlMethod =
      '- (BOOL)application:(UIApplication *)application openURL';
    const payPalRebornOpenUrlLines = [
      '  if ([url.scheme localizedCaseInsensitiveCompare:[PaypalRebornConfig getPaymentUrlScheme]] == NSOrderedSame) {',
      '    return [PaypalRebornConfig handleUrl:url];',
      '  }',
    ];
    const openUrlMethodElementIndex = contents.findIndex((content) =>
      content.includes(openUrlMethod)
    );
    const payPalRebornOpenUrlLineIndex = contents.findIndex((content) =>
      content.includes(payPalRebornOpenUrlLines?.[0] ?? '')
    );
    // If openUrlMethodElementIndex exist in AppDelegate.mm and payPalRebornOpenUrlLineIndex do not exist
    if (!~payPalRebornOpenUrlLineIndex && !!~openUrlMethodElementIndex) {
      contents.splice(
        // We are adding +1 to the index to insert content after '{' block
        openUrlMethodElementIndex + 1,
        0,
        ...payPalRebornOpenUrlLines
      );
    }
    config.modResults.contents = contents.join('\n');
    return config;
  });
};

/**
 * Add a new wrapper Swift file to the Xcode project for Swift compatibility.
 */
export const withSwiftPaypalRebornWrapperFile: ConfigPlugin = (config) => {
  return IOSConfig.XcodeProjectFile.withBuildSourceFile(config, {
    filePath: 'PaypalRebornConfig.swift',
    contents: [
      'import Braintree',
      'import Foundation',
      '',
      '@objc public class PaypalRebornConfig: NSObject {',
      '',
      '@objc(configure)',
      'public static func configure() {',
      '  BTAppContextSwitcher.sharedInstance.returnURLScheme = self.getPaymentUrlScheme()',
      '}',
      '',
      '@objc(getPaymentUrlScheme)',
      'public static func getPaymentUrlScheme() -> String {',
      '  let bundleIdentifier = Bundle.main.bundleIdentifier ?? ""',
      '  return bundleIdentifier + ".braintree"',
      '}',
      '',
      '@objc(handleUrl:)',
      'public static func handleUrl(url: URL) -> Bool {',
      '  return BTAppContextSwitcher.sharedInstance.handleOpen(url)',
      '}',
      '}',
    ].join('\n'),
  });
};

export const withReactNativePaypalRebornPlist: ConfigPlugin = (expoConfig) => {
  return withInfoPlist(expoConfig, (config) => {
    const bundleIdentifier = config.ios?.bundleIdentifier ?? '';
    const bundleIdentifierWithBraintreeSchema = `${bundleIdentifier}.braintree`;
    const bundleUrlTypes = config.modResults.CFBundleURLTypes;
    const isBraintreeSchemaNotExist = !bundleUrlTypes?.find((urlTypes) => {
      urlTypes.CFBundleURLSchemes.includes(bundleIdentifierWithBraintreeSchema);
    });
    // If Braintree url schema for specific bundle id not exist then add this entry
    if (isBraintreeSchemaNotExist) {
      config.modResults.CFBundleURLTypes = bundleUrlTypes?.map(
        (bundleUrlType) => {
          const isUrlSchemaContainBundleIdentifier =
            bundleUrlType.CFBundleURLSchemes.includes(bundleIdentifier);
          if (isUrlSchemaContainBundleIdentifier) {
            bundleUrlType.CFBundleURLSchemes.push(
              bundleIdentifierWithBraintreeSchema
            );
          }
          return bundleUrlType;
        }
      );
    }
    return config;
  });
};
