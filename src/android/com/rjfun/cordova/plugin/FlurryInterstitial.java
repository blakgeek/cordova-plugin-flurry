package com.rjfun.cordova.plugin;

import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.flurry.android.*;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * This class represents the native implementation for the FlurryBanner Cordova plugin.
 * This plugin can be used to request FlurryBanner ads natively via the Google FlurryBanner SDK.
 * The Google FlurryBanner SDK is a dependency for this plugin.
 */
public class FlurryInterstitial extends CordovaPlugin implements FlurryAdListener {
    private static final String NAME = "INTERSTITIAL";
    private boolean initialized = false;

    private FrameLayout adView = null;
    private String publisherId;
    private boolean showAdOnReceive = true;
    private static final String LOGTAG = "FlurryInterstitial";

    /**
     * Cordova Actions.
     */
    private static final String ACTION_SET_PUBLISHER_ID = "setPublisherId";
    private static final String ACTION_CREATE_AD_VIEW = "createAdView";
    private static final String ACTION_DESTROY_AD_VIEW = "destroyAdView";
    private static final String ACTION_REQUEST_AD = "requestAd";
    private static final String ACTION_DEBUG = "enableDebug";

    private static final int PUBLISHER_ID_ARG_INDEX = 0;
    private static final int POSITION_AT_TOP_ARG_INDEX = 0;
    private static final int IS_TESTING_ARG_INDEX = 0;
    private static final int SHOW_AD_ON_RECEIVE_ARG_INDEX = 1;
    private static final int SHOW_AD_ARG_INDEX = 0;

    /**
     * This is the main method for the FlurryBanner plugin.  All API calls go through here.
     * This method determines the action, and executes the appropriate call.
     *
     * @param action          The action that the plugin should execute.
     * @param args            The input parameters for the action.
     * @param callbackContext The callback context.
     * @return A PluginResult representing the result of the provided action.  A
     * status of INVALID_ACTION is returned if the action is not recognized.
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        try {
            PluginResult result = null;
            if (ACTION_CREATE_AD_VIEW.equals(action)) {
                createAdView(args, callbackContext);

            } else if (ACTION_DESTROY_AD_VIEW.equals(action)) {
                result = destroyAdView(callbackContext);

            } else if (ACTION_REQUEST_AD.equals(action)) {
                result = requestAd(args, callbackContext);

            } else if (ACTION_SET_PUBLISHER_ID.equals(action)) {
                setPublisherId(args, callbackContext);

            } else if (ACTION_DEBUG.equals(action)) {
                enableDebug(args);

            } else {
                Log.d(LOGTAG, String.format("Invalid action passed: %s", action));
                result = new PluginResult(Status.INVALID_ACTION);
            }

            if (result != null) {
                callbackContext.sendPluginResult(result);
            }

        } catch (JSONException exception) {
            Log.w(LOGTAG, String.format("Got JSON Exception: %s", exception.getMessage()));
            callbackContext.sendPluginResult(new PluginResult(Status.JSON_EXCEPTION));
        }

        return true;
    }

    private void setPublisherId(JSONArray args, CallbackContext callbackContext) throws JSONException {

        publisherId = args.getString(PUBLISHER_ID_ARG_INDEX);
        Log.w(LOGTAG, String.format("set publisher id: %s", publisherId));
    }

    private void enableDebug(JSONArray args) throws JSONException {
        if (args.getBoolean(0)) {
            FlurryAgent.setLogEnabled(true);
            FlurryAgent.setLogEvents(true);
            FlurryAgent.setLogLevel(Log.VERBOSE);
        } else {
            FlurryAgent.setLogEnabled(false);
            FlurryAgent.setLogEvents(false);
            FlurryAgent.setLogLevel(Log.ERROR);
        }
    }

    /**
     * Parses the create banner view input parameters and runs the create banner
     * view action on the UI thread.  If this request is successful, the developer
     * should make the requestAd call to request an ad for the banner.
     *
     * @param args The JSONArray representing input parameters.  This function
     *             expects the first object in the array to be a JSONObject with the
     *             input parameters.
     * @return A PluginResult representing whether or not the banner was created
     * successfully.
     */
    private void createAdView(JSONArray args, CallbackContext callbackContext) throws JSONException {

        Log.w(LOGTAG, "createAdView");


        boolean firstTimeInit = false;
        if (!initialized) {
            firstTimeInit = true;
            FlurryAds.setAdListener(this);
            Log.w(LOGTAG, "onStartSession");
            FlurryAgent.onStartSession(cordova.getActivity(), publisherId);
            initialized = true;
        }

        if (adView == null) {
            Log.w(LOGTAG, "creating banner ad FrameLayout");
            adView = new FrameLayout(cordova.getActivity());
            LayoutParams params = new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            adView.setLayoutParams(params);
        }

        // Create the AdView on the UI thread.
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (adView.getParent() != null) {
                    ((ViewGroup) adView.getParent()).removeView(adView);
                }
                ViewGroup parentView = (ViewGroup) webView.getParent();
                parentView.addView(adView);

            }
        });

        if (firstTimeInit) { // when init we will delay 3 seconds before callback
            final CallbackContext delayCallback = callbackContext;
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    delayCallback.success();
                }
            });
        } else {
            callbackContext.success();
        }
    }

    private PluginResult destroyAdView(CallbackContext callbackContext) {
        Log.w(LOGTAG, "destroyAdView");


        final CallbackContext delayCallback = callbackContext;
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (adView != null) {
                    FlurryAds.removeAd(cordova.getActivity(), NAME, adView);

                    ViewGroup parentView = (ViewGroup) adView.getParent();
                    if (parentView != null) {
                        parentView.removeView(adView);
                    }
                    adView = null;
                }
                delayCallback.success();
            }
        });

        return null;
    }

    /**
     * Parses the request interstitial ad input parameters and runs the request ad action on
     * the UI thread.
     *
     * @param inputs The JSONArray representing input parameters.  This function
     *               expects the first object in the array to be a JSONObject with the
     *               input parameters.
     * @return A PluginResult representing whether or not an ad was requested
     * succcessfully.  Listen for onReceiveAd() and onFailedToReceiveAd()
     * callbacks to see if an ad was successfully retrieved.
     */
    private PluginResult requestAd(JSONArray inputs, CallbackContext callbackContext) throws JSONException {
        Log.w(LOGTAG, "requestInterstitialAd");

        if (adView == null) {
            return new PluginResult(Status.ERROR, "adView is null, call createAdView/createInterstitialView first.");
        }

        final boolean isTesting = inputs.optBoolean(IS_TESTING_ARG_INDEX, false);
        showAdOnReceive = inputs.optBoolean(SHOW_AD_ON_RECEIVE_ARG_INDEX, true);


        final CallbackContext delayCallback = callbackContext;
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                FlurryAds.enableTestAds(isTesting);

                FlurryAds.fetchAd(cordova.getActivity(), NAME, adView, FlurryAdSize.FULLSCREEN);


                delayCallback.success();
            }
        });

        return null;
    }

    /**
     * This class implements the FlurryBanner ad listener events.  It forwards the events
     * to the JavaScript layer.  To listen for these events, use:
     * <p/>
     * document.addEventListener('onReceiveAd', function());
     * document.addEventListener('onFailedToReceiveAd', function(data));
     * document.addEventListener('onPresentAd', function());
     * document.addEventListener('onDismissAd', function());
     * document.addEventListener('onLeaveToAd', function());
     */
    //public class BasicListener implements FlurryAdListener {
    @Override
    public void onAdClicked(String arg0) {
        Log.w(LOGTAG, "onAdClicked");
        // TODO Auto-generated method stub

    }

    @Override
    public void onAdClosed(String arg0) {
        Log.w(LOGTAG, "onAdClosed");
        // TODO Auto-generated method stub
        //
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:cordova.fireDocumentEvent('onDismissAd');");
            }
        });
    }

    @Override
    public void onAdOpened(String arg0) {
        Log.w(LOGTAG, "onAdOpened");
        // TODO Auto-generated method stub
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:cordova.fireDocumentEvent('onPresentAd');");
            }
        });
        //
    }

    @Override
    public void onApplicationExit(String arg0) {
        // TODO Auto-generated method stub
        Log.w(LOGTAG, "onApplicationExit");

    }

    @Override
    public void onRenderFailed(String arg0) {
        // TODO Auto-generated method stub
        Log.w(LOGTAG, "onRenderFailed");

    }

    @Override
    public void onRendered(String arg0) {
        // TODO Auto-generated method stub
        Log.w(LOGTAG, "onRendered");
        //
    }

    @Override
    public void onVideoCompleted(String arg0) {
        Log.w(LOGTAG, "onVideoCompleted");
        // TODO Auto-generated method stub

    }

    @Override
    public boolean shouldDisplayAd(String arg0, FlurryAdType arg1) {
        Log.w(LOGTAG, "shouldDisplayAd");

        return true;
    }

    @Override
    public void spaceDidFailToReceiveAd(String errorCode) {
        Log.w(LOGTAG, String.format("spaceDidFailToReceiveAd: %s", errorCode));

        //FlurryAds.fetchAd(cordova.getActivity(), adSpace, adView, adSize);

        final String fErrorCode = errorCode;
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl(String.format("javascript:cordova.fireDocumentEvent('onFailedToReceiveAd', { 'error': '%s' });", fErrorCode));

            }
        });
    }

    @Override
    public void spaceDidReceiveAd(String target) {
        Log.w(LOGTAG, String.format("spaceDidReceiveAd, for %s, now show it", target));

        if (showAdOnReceive) {
            FlurryAds.displayAd(cordova.getActivity(), target, adView);
        }

        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:cordova.fireDocumentEvent('onReceiveAd');");
            }
        });
    }

    //}

    @Override
    public void onDestroy() {
        if (adView != null) {
            FlurryAds.removeAd(cordova.getActivity(), NAME, adView);

            ViewGroup parentView = (ViewGroup) adView.getParent();
            if (parentView != null) {
                parentView.removeView(adView);
            }
            adView = null;
        }
        super.onDestroy();
    }

}

