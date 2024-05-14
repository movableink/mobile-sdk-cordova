#import <Foundation/Foundation.h>
#import <objc/runtime.h>
#import <MovableInk/MovableInk-Swift.h>
#import "MISDK+AppDelegate.h"

@implementation AppDelegate (MISDK)

#ifndef MISDK_DISABLE_APP_DELEGATE

#pragma mark - Original method exist flags for swizzling
static BOOL isOriginalContinueUserActivityExist;
static BOOL isOriginalOpenURLOptionsExist;

#if MISDK_SHOULD_SWIZZLE
+ (void)load {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{

        SEL originalSelector = @selector(application:continueUserActivity:restorationHandler:);
        SEL swizzledSelector = @selector(mi_application:continueUserActivity:restorationHandler:);
        [self addSwizzledMethodWithOriginalSelector:originalSelector swizzledSelector:swizzledSelector methodExistFlag:&isOriginalContinueUserActivityExist];

        SEL originalSelector3 = @selector(application:openURL:options:);
        SEL swizzledSelector3 = @selector(mi_application:openURL:options:);
        [self addSwizzledMethodWithOriginalSelector:originalSelector3 swizzledSelector:swizzledSelector3 methodExistFlag:&isOriginalOpenURLOptionsExist];
    });
}

#else

#pragma mark - AppDelegate Deep Link implementation
- (BOOL)application:(UIApplication *)app openURL:(NSURL *)url options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options {
    return [MIClient handleUniversalLinkWithUrl: url];
}

- (BOOL)application:(UIApplication *)application continueUserActivity:(NSUserActivity *)userActivity restorationHandler:(void (^)(NSArray<id<UIUserActivityRestoring>> * _Nullable))restorationHandler {
   return [MIClient handleUniversalLinkFrom: userActivity];
}

#endif

#pragma mark - Method Swizzling - Deep Link implementation
- (BOOL)mi_application:(UIApplication *)application continueUserActivity:(NSUserActivity *)userActivity restorationHandler:(void (^)(NSArray * _Nullable))restorationHandler {
    [MIClient handleUniversalLinkFrom: userActivity];

    if (isOriginalContinueUserActivityExist) {
      return [self mi_application:application continueUserActivity:userActivity restorationHandler:restorationHandler];
    }

    return YES;
}

- (BOOL)mi_application:(UIApplication *)app openURL:(NSURL *)url options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options {
    [MIClient handleUniversalLinkWithUrl: url];

    if (isOriginalOpenURLOptionsExist) {
      return [self mi_application:app openURL:url options:options];
    }

    return YES;
}

#pragma mark - Add swizzled methods
+ (void)addSwizzledMethodWithOriginalSelector:(SEL)originalSelector
                             swizzledSelector:(SEL)swizzledSelector
                              methodExistFlag:(BOOL *)methodExistFlag {
    Class class = [self class];
    Method originalMethod = class_getInstanceMethod(class, originalSelector);
    Method swizzledMethod = class_getInstanceMethod(class, swizzledSelector);
    *methodExistFlag = [class instancesRespondToSelector:originalSelector];

    BOOL didAddMethod =
    class_addMethod(
      class,
      originalSelector,
      method_getImplementation(swizzledMethod),
      method_getTypeEncoding(swizzledMethod)
    );

    if (didAddMethod) {
        class_replaceMethod(
          class,
          swizzledSelector,
          method_getImplementation(originalMethod),
          method_getTypeEncoding(originalMethod)
        );
    } else {
        method_exchangeImplementations(originalMethod, swizzledMethod);
    }
}

#endif
@end
