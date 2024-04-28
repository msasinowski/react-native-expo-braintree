#import "AppDelegate.h"

#import <React/RCTBundleURLProvider.h>
#import "PaypalRebornExample-Swift.h"
#import <React/RCTLinkingManager.h>



@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  self.moduleName = @"PaypalRebornExample";
  // You can add your custom initial props in the dictionary below.
  // They will be passed down to the ViewController used by React Native.
  self.initialProps = @{};
  [PaypalRebornConfig configure];
  return [super application:application didFinishLaunchingWithOptions:launchOptions];
}


- (BOOL)application:(UIApplication *)application
            openURL:(NSURL *)url
            options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options {
    
    
    if ([url.scheme localizedCaseInsensitiveCompare:[PaypalRebornConfig getPaymentUrlScheme]] == NSOrderedSame) {
        return [PaypalRebornConfig handleUrl:url];
    }
    
    return [RCTLinkingManager application:application openURL:url options:options];
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

@end
