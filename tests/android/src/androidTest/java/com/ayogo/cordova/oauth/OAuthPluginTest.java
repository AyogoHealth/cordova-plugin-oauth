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

import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.webkit.WebView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OAuthPluginTest {
    static final long TIMEOUT = 5000;
    static final String TEST_PACKAGE = "com.ayogo.cordova.oauth.tests";

    private UiDevice device;

    @Before
    public void waitForAppLaunch() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        device.pressHome();

        final String launcherPackage = getLauncherPackageName();
        assertNotNull(launcherPackage);
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), TIMEOUT);

        Context context = ApplicationProvider.getApplicationContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(TEST_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        device.wait(Until.hasObject(By.pkg(TEST_PACKAGE).depth(0)), TIMEOUT);
    }

    @After
    public void ensureLogout() {
        UiObject2 logoutBtn = device.wait(Until.findObject(By.text("Logout")), TIMEOUT);
        if (logoutBtn != null) {
            logoutBtn.click();

            device.waitForIdle();
        }
    }

    @Test
    public void testOAuthFlow() {
        assertNotNull(device);

        device.wait(Until.findObject(By.clazz(WebView.class)), TIMEOUT);

        UiObject2 loginBtn = device.wait(Until.findObject(By.text("Sign in with OAuth")), TIMEOUT);
        assertNotNull(loginBtn);
        loginBtn.click();

        // Now we have to interact with the Chrome Custom Tab
        UiObject2 oauthBtn = device.wait(Until.findObject(By.text("Click Here to Login")), TIMEOUT);
        assertNotNull(oauthBtn);
        oauthBtn.click();

        // Should be back in the app now
        device.wait(Until.findObject(By.clazz(WebView.class)), TIMEOUT);

        device.wait(Until.findObject(By.text("LOGGED IN")), TIMEOUT);
    }

    @Test
    public void testOAuthCancellation() {
        assertNotNull(device);

        device.wait(Until.findObject(By.clazz(WebView.class)), TIMEOUT);

        UiObject2 loginBtn = device.wait(Until.findObject(By.text("Sign in with OAuth")), TIMEOUT);
        assertNotNull(loginBtn);
        loginBtn.click();

        // Now we have to close the Chrome Custom Tab without logging in
        UiObject2 oauthBtn = device.wait(Until.findObject(By.text("Click Here to Login")), TIMEOUT);
        assertNotNull(oauthBtn);

        device.pressBack();

        // Should be back in the app now
        device.wait(Until.findObject(By.clazz(WebView.class)), TIMEOUT);

        device.wait(Until.findObject(By.text("LOGIN CANCELLED!")), TIMEOUT);
    }

    /**
     * Uses package manager to find the package name of the device launcher. Usually this package
     * is "com.android.launcher" but can be different at times. This is a generic solution which
     * works on all platforms.`
     */
    @SuppressWarnings("deprecation")
    private String getLauncherPackageName() {
        // Create launcher Intent
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        // Use PackageManager to get the launcher package name
        PackageManager pm = ApplicationProvider.getApplicationContext().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.activityInfo.packageName;
    }
}
