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

import XCTest

class OAuthPluginUITests: XCTestCase {
    let app = XCUIApplication()

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    override func tearDownWithError() throws {
        let logoutBtn = app.webViews.buttons["Logout"]
        if logoutBtn.exists {
            logoutBtn.tap()
            sleep(1)
        }

        app.terminate()
    }

    func testOAuthFlow() throws {
        app.launch()
        app.tap()

        // Wait for the landing page to load
        let landingPage = app.staticTexts["WELCOME!"]
        _ = landingPage.waitForExistence(timeout: 5)

        // Tap the button, Kronk!
        let button = app.webViews.buttons["Sign in with OAuth"]
        XCTAssert(button.exists)
        XCTAssert(button.isHittable)
        button.tap()

        if #available(iOS 12.0, *) {
            runAuthenticationSessionFlow()
        } else if #available(iOS 11.0, *) {
            runAuthenticationSessionFlow()
        } else if #available(iOS 9.0, *) {
            runSafariViewControllerFlow()
        } else {
            runMobileSafariFlow()
        }

        // Verify the app received the OAuth token and considers us logged in
        let loggedIn = app.staticTexts["LOGGED IN"]
        _ = loggedIn.waitForExistence(timeout: 5)
        XCTAssert(loggedIn.exists)
    }

    private func runAuthenticationSessionFlow() {
        let springboard = XCUIApplication(bundleIdentifier: "com.apple.springboard")
        let continueBtn = springboard.buttons["Continue"]
        if continueBtn.waitForExistence(timeout: 10) {
            continueBtn.tap()
        }

        let oauthButton = app.webViews.buttons["Click Here to Login"]
        _ = oauthButton.waitForExistence(timeout: 5)
        XCTAssert(oauthButton.exists)
        XCTAssert(oauthButton.isHittable)
        oauthButton.tap()
    }

    private func runSafariViewControllerFlow() {
        let oauthButton = app.webViews.buttons["Click Here to Login"]
        _ = oauthButton.waitForExistence(timeout: 5)
        XCTAssert(oauthButton.exists)
        XCTAssert(oauthButton.isHittable)
        oauthButton.tap()
    }

    private func runMobileSafariFlow() {
        let safari = XCUIApplication(bundleIdentifier: "com.apple.mobilesafari")
        _ = safari.wait(for: .runningForeground, timeout: 5)

        let oauthButton = safari.webViews.buttons["Click Here to Login"]
        _ = oauthButton.waitForExistence(timeout: 5)
        XCTAssert(oauthButton.exists)
        XCTAssert(oauthButton.isHittable)
        oauthButton.tap()

        // Tell Safari to open the app
        let openBtn = safari.buttons["Open"]
        if openBtn.waitForExistence(timeout: 5) {
            openBtn.tap()
        }
    }
}
