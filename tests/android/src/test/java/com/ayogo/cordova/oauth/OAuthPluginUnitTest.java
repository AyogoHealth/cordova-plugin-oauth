/**
 * Copyright 2022 Ayogo Health Inc.
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Intent;
import android.net.Uri;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OAuthPluginUnitTest {
    @Test
    public void executeWithUnknownAction() throws JSONException {
        OAuthPlugin plugin = new OAuthPlugin();

        CallbackContext context = mock(CallbackContext.class);
        boolean retVal = plugin.execute("unknownAction", "[]", context);
        assertEquals(false, retVal);

        ArgumentCaptor<PluginResult> result = ArgumentCaptor.forClass(PluginResult.class);
        verify(context).sendPluginResult(result.capture());
        assertEquals(PluginResult.Status.INVALID_ACTION.ordinal(), result.getValue().getStatus());
    }

    @Test
    public void executeOAuthWithMissingArgument() throws JSONException {
        OAuthPlugin plugin = new OAuthPlugin();

        CallbackContext context = mock(CallbackContext.class);
        boolean retVal = plugin.execute("startOAuth", "[]", context);
        assertEquals(false, retVal);

        ArgumentCaptor<PluginResult> result = ArgumentCaptor.forClass(PluginResult.class);
        verify(context).sendPluginResult(result.capture());
        assertEquals(PluginResult.Status.ERROR.ordinal(), result.getValue().getStatus());
    }

    private OAuthPlugin initializedPlugin() {
        OAuthPlugin plugin = new OAuthPlugin();
        plugin.privateInitialize("OAuth", null, null, new CordovaPreferences());
        return plugin;
    }

    @Test
    public void onNewIntentWithoutAction() {
        OAuthPlugin plugin = initializedPlugin();

        // e.g. the activity started by component name only (`adb shell am start -n`)
        Intent intent = mock(Intent.class);
        plugin.onNewIntent(intent);

        verify(intent, never()).getData();
    }

    @Test
    public void onNewIntentViewWithoutData() {
        OAuthPlugin plugin = initializedPlugin();

        Intent intent = mock(Intent.class);
        when(intent.getAction()).thenReturn(Intent.ACTION_VIEW);
        plugin.onNewIntent(intent);
    }

    @Test
    public void onNewIntentViewWithoutHost() {
        OAuthPlugin plugin = initializedPlugin();

        // e.g. an opaque URI such as `myscheme:path`
        Uri uri = mock(Uri.class);
        Intent intent = mock(Intent.class);
        when(intent.getAction()).thenReturn(Intent.ACTION_VIEW);
        when(intent.getData()).thenReturn(uri);
        plugin.onNewIntent(intent);

        verify(uri, never()).getQueryParameterNames();
    }
}
