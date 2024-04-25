#import "AppDelegate.h"

#import <React/RCTBundleURLProvider.h>
#import <PaypalRebornExample-Swift.h>


@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  self.moduleName = @"PaypalRebornExample";
  // You can add your custom initial props in the dictionary below.
  // They will be passed down to the ViewController used by React Native.
  self.initialProps = @{};

  [BTAppContextSwitcher setReturnURLScheme:self.paymentsURLScheme];
  return [super application:application didFinishLaunchingWithOptions:launchOptions];
}

- (NSURL *)sourceURLForBridge:(RCTBridge *)bridge
{
  return [self bundleURL];
}

- (NSURL *)bundleURL
{
#if DEBUG
  return [[RCTBundleURLProvider sharedSettings] jsBundleURLForBundleRoot:@"index"];
#else
  return [[NSBundle mainBundle] URLForResource:@"main" withExtension:@"jsbundle"];
#endif
}

- (BOOL)application:(UIApplication *)application
            openURL:(NSURL *)url
            options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options {

    if ([url.scheme localizedCaseInsensitiveCompare:self.paymentsURLScheme] == NSOrderedSame) {
        return [BTAppContextSwitcher handleOpenURL:url];
    }
    
    return [RCTLinkingManager application:application openURL:url options:options];
}

- (NSString *)paymentsURLScheme {
    NSString *bundleIdentifier = [[NSBundle mainBundle] bundleIdentifier];
    return [NSString stringWithFormat:@"%@.%@", bundleIdentifier, @"payments"];
}

@end
