#import "CDVFlurryInterstitial.h"
#import "Flurry.h"
#import "FlurryAds.h"
#import "FlurryAdDelegate.h"
#import "MainViewController.h"

@implementation CDVFlurryInterstitial

#pragma mark Cordova JS bridge

- (CDVPlugin *)initWithWebView:(UIWebView *)theWebView {
    NSLog( @"initWithWebView" );

	self = (CDVFlurryInterstitial *)[super initWithWebView:theWebView];
	return self;
}

- (void)setPublisherId:(CDVInvokedUrlCommand *)command {
    
    publisherId = [command argumentAtIndex:0];
    NSLog(@"set publisherId: %@", publisherId);
}

- (void)enableDebug:(CDVInvokedUrlCommand *)command {
    
    [Flurry setDebugLogEnabled: [[command argumentAtIndex:0 withDefault:[NSNumber numberWithBool:YES]] boolValue]];
}

- (void)createAdView:(CDVInvokedUrlCommand *)command {
    NSLog( @"creating interstitial view");
    
    CDVPluginResult *pluginResult;
	NSString *callbackId = command.callbackId;
    
	// Register yourself as a delegate for ad callbacks
	[FlurryAds setAdDelegate:self];
    
    // Fetch fullscreen ads early when a later display is likely. For
    // example, at the beginning of a level.
	[FlurryAds fetchAdForSpace: @"INTERSTITIAL_MAIN_VIEW" frame:self.webView.superview.frame size:FULLSCREEN];
    
	// Call the success callback that was passed in through the javascript.
	pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
	[self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}

- (void)destroyAdView:(CDVInvokedUrlCommand *)command {
    NSLog( @"destroying interstitial view" );
    
	CDVPluginResult *pluginResult;
	NSString *callbackId = command.callbackId;
    
	// Call the success callback that was passed in through the javascript.
	pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
	[self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}

- (void)showAd:(CDVInvokedUrlCommand *)command {
    NSLog( @"show interstial ad (noop)");
}

- (void)requestAd:(CDVInvokedUrlCommand *)command {
    NSLog( @"requesting interstitial add" );
    
    // TODO
    CDVPluginResult *pluginResult;
	NSString *callbackId = command.callbackId;
    
    [FlurryAds fetchAdForSpace: @"INTERSTITIAL_MAIN_VIEW" frame:self.webView.superview.frame size:FULLSCREEN];
    
    BOOL useTestAds = [[command argumentAtIndex:0 withDefault:NO] boolValue];
    
    [FlurryAds enableTestAds:useTestAds];
    [FlurryAds fetchAndDisplayAdForSpace:@"INTERSTITIAL_MAIN_VIEW" view:self.webView.superview size:FULLSCREEN];
    
	pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
	[self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}

#pragma mark FlurryAdDelegate implementation

- (void) spaceDidReceiveAd:(NSString*)adSpace {
    // Received Ad
    [FlurryAds displayAdForSpace:adSpace onView:self.webView.superview];
    
	NSLog(@"%s: Received ad successfully.", __PRETTY_FUNCTION__);
	[self.commandDelegate evalJs:@"cordova.fireDocumentEvent('onReceiveAd');"];
}

- (void) spaceDidFailToReceiveAd:(NSString*) adSpace error:(NSError *)error {
	NSLog(@"%s: Failed to receive ad with error: %@",
			__PRETTY_FUNCTION__, [error localizedFailureReason]);
	// Since we're passing error back through Cordova, we need to set this up.
	NSString *jsString =
		@"cordova.fireDocumentEvent('onFailedToReceiveAd',"
		@"{ 'error': '%@' });";
	[self.commandDelegate evalJs:[NSString stringWithFormat:jsString, [error localizedFailureReason]]];
}

- (void) spaceDidFailToRender:(NSString *)space error:(NSError *)error {
	NSLog(@"%s: Failed to receive ad with error: %@",
          __PRETTY_FUNCTION__, [error localizedFailureReason]);
	// Since we're passing error back through Cordova, we need to set this up.
	NSString *jsString =
    @"cordova.fireDocumentEvent('onFailedToRenderAd',"
    @"{ 'error': '%@' });";
	[self.commandDelegate evalJs:[NSString stringWithFormat:jsString, [error localizedFailureReason]]];
}

- (void) spaceWillLeaveApplication:(NSString *)adSpace {
	//[self.commandDelegate evalJs:@"cordova.fireDocumentEvent('onLeaveToAd');"];
    NSLog( @"adViewWillLeaveApplication" );
}

- (BOOL) spaceShouldDisplay:(NSString*)adSpace interstitial:(BOOL)interstitial {
	[self.commandDelegate evalJs:@"cordova.fireDocumentEvent('onPresentAd');"];
    NSLog( @"adViewWillPresentScreen" );
    
    return TRUE;
}

- (void) spaceWillDismiss:(NSString *)adSpace {
	[self.commandDelegate evalJs:@"cordova.fireDocumentEvent('onDismissAd');"];
    NSLog( @"adViewDismissScreen" );
}

- (void)spaceDidDismiss:(NSString *)adSpace interstitial:(BOOL)interstitial {
	[self.commandDelegate evalJs:@"cordova.fireDocumentEvent('onDidDismissAd');"];
    NSLog( @"adViewDidDismissScreen" );
}

#pragma mark Cleanup

- (void)dealloc {
	[[UIDevice currentDevice] endGeneratingDeviceOrientationNotifications];
	[[NSNotificationCenter defaultCenter]
		removeObserver:self
		name:UIDeviceOrientationDidChangeNotification
		object:nil];

    [FlurryAds setAdDelegate:nil];
}

@end
