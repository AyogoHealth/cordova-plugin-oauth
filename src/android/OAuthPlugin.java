/**
 * Copyright 2019 Ayogo Health Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ayogo.cordova.oauth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import androidx.browser.customtabs.CustomTabsIntent;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class OAuthPlugin extends CordovaPlugin {
    private final String TAG = "OAuthPlugin";

    // Taken from Google's CustomTabsHelper
    // https://github.com/GoogleChrome/custom-tabs-client/blob/da65829d7d4b80c00809c6c4aa7f61f88fc7ca26/shared/src/main/java/org/chromium/customtabsclient/shared/CustomTabsHelper.java
    static final String STABLE_PACKAGE = "com.android.chrome";
    static final String BETA_PACKAGE = "com.chrome.beta";
    static final String DEV_PACKAGE = "com.chrome.dev";
    static final String LOCAL_PACKAGE = "com.google.android.apps.chrome";
    private static final String EXTRA_CUSTOM_TABS_KEEP_ALIVE = "android.support.customtabs.extra.KEEP_ALIVE";
    private static final String ACTION_CUSTOM_TABS_CONNECTION = "android.support.customtabs.action.CustomTabsService";


    /**
     * The name of the package to use for the custom tab service.
     */
    private String tabProvider = null;


    /**
     * Executes the request.
     *
     * This method is called from the WebView thread. To do a non-trivial amount of work, use:
     *     cordova.getThreadPool().execute(runnable);
     *
     * To run on the UI thread, use:
     *     cordova.getActivity().runOnUiThread(runnable);
     *
     * @param action          The action to execute.
     * @param rawArgs         The exec() arguments in JSON form.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return                Whether the action was valid.
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        if ("startOAuth".equals(action)) {
            try {
                String authEndpoint = args.getString(0);

                this.startOAuth(authEndpoint);

                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
                return true;
            } catch (JSONException e) {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
                return false;
            }
        }

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
        return false;
    }



    /**
     * Called when the activity receives a new intent.
     *
     * We use this method to intercept the OAuth callback URI and dispatch a
     * message to the WebView.
     */
    @Override
    public void onNewIntent(Intent intent) {
        if (intent == null || !intent.getAction().equals(Intent.ACTION_VIEW)) {
            return;
        }

        final Uri uri = intent.getData();

        if (uri.getHost().equals("oauth_callback")) {
            LOG.i(TAG, "OAuth called back with parameters.");

            try {
                JSONObject jsobj = new JSONObject();

                for (String queryKey : uri.getQueryParameterNames()) {
                    jsobj.put(queryKey, uri.getQueryParameter(queryKey));
                }

                final String msg = jsobj.toString();
                this.webView.getEngine().evaluateJavascript("window.dispatchEvent(new MessageEvent('message', { data: 'oauth::" + msg + "' }));", null);
            } catch (JSONException e) {
                LOG.e(TAG, "JSON Serialization failed");
                e.printStackTrace();
            }
        }
    }


    /**
     * Launches the custom tab with the OAuth endpoint URL.
     *
     * @param url The URL of the OAuth endpoint.
     */
    private void startOAuth(String url) {
        String customTabsBrowser = findCustomTabProvider();

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();

        String packageName = this.findCustomTabProvider();
        if (packageName != null) {
           customTabsIntent.intent.setPackage(packageName);
        }

        customTabsIntent.launchUrl(this.cordova.getActivity(), Uri.parse(url));
    }


    /**
     * Goes through all apps that handle VIEW intents and have a warmup service.
     *
     * Picks the one chosen by the user if there is one, otherwise makes a best
     * effort to return a valid package name.
     *
     * This is <strong>not</strong> threadsafe.
     *
     * @return The package name recommended to use for connecting to custom
     * tabs related components.
     */
    private String findCustomTabProvider() {
        if (this.tabProvider != null) {
            return this.tabProvider;
        }

        PackageManager pm = this.cordova.getActivity().getPackageManager();

        // Get default VIEW intent handler.
        Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));
        ResolveInfo defaultViewHandlerInfo = pm.resolveActivity(activityIntent, 0);
        String defaultViewHandlerPackageName = null;

        if (defaultViewHandlerInfo != null) {
            defaultViewHandlerPackageName = defaultViewHandlerInfo.activityInfo.packageName;
        }


        // Get all apps that can handle VIEW intents.
        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, PackageManager.MATCH_ALL);
        List<String> packagesSupportingCustomTabs = new ArrayList<>();

        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(info.activityInfo.packageName);

            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName);
            }
        }

        // Now packagesSupportingCustomTabs contains all apps that can handle both VIEW intents
        // and service calls.
        if (packagesSupportingCustomTabs.isEmpty()) {
            this.tabProvider = null;
        } else if (packagesSupportingCustomTabs.size() == 1) {
            this.tabProvider = packagesSupportingCustomTabs.get(0);
        } else if (!TextUtils.isEmpty(defaultViewHandlerPackageName) && !this.hasSpecializedHandlerIntents(activityIntent) && packagesSupportingCustomTabs.contains(defaultViewHandlerPackageName)) {
            this.tabProvider = defaultViewHandlerPackageName;
        } else if (packagesSupportingCustomTabs.contains(STABLE_PACKAGE)) {
            this.tabProvider = STABLE_PACKAGE;
        } else if (packagesSupportingCustomTabs.contains(BETA_PACKAGE)) {
            this.tabProvider = BETA_PACKAGE;
        } else if (packagesSupportingCustomTabs.contains(DEV_PACKAGE)) {
            this.tabProvider = DEV_PACKAGE;
        } else if (packagesSupportingCustomTabs.contains(LOCAL_PACKAGE)) {
            this.tabProvider = LOCAL_PACKAGE;
        }

        return this.tabProvider;
    }


    /**
     * Used to check whether there is a specialized handler for a given intent.
     *
     * @param intent The intent to check with.
     * @return Whether there is a specialized handler for the given intent.
     */
    private boolean hasSpecializedHandlerIntents(Intent intent) {
        try {
            PackageManager pm = this.cordova.getActivity().getPackageManager();
            List<ResolveInfo> handlers = pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);

            if (handlers == null || handlers.size() == 0) {
                return false;
            }

            for (ResolveInfo resolveInfo : handlers) {
                IntentFilter filter = resolveInfo.filter;

                if (filter == null || filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0 || resolveInfo.activityInfo == null) {
                    continue;
                }

                return true;
            }
        } catch (RuntimeException e) {
            LOG.e(TAG, "Runtime exception while getting specialized handlers");
        }

        return false;
    }
}
