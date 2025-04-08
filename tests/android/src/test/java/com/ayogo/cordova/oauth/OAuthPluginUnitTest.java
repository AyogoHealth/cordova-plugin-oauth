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
import static org.mockito.Mockito.verify;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
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
}
