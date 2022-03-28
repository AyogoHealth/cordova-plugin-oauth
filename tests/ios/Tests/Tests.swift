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
@testable import Cordova
@testable import OAuthPluginTest

// MARK: Mock classes
class MockCommandDelegate : NSObject, CDVCommandDelegate {
    var settings: [AnyHashable : Any]! {
        get {
            return ["oauthscheme": "oauthtest"]
        }
    }

    var urlTransformer: UrlTransformerBlock!
    var lastResult: CDVPluginResult!

    func path(forResource resourcepath: String!) -> String! {
        return "";
    }

    func getCommandInstance(_ pluginName: String!) -> Any! {
        return nil;
    }

    func send(_ result: CDVPluginResult!, callbackId: String!) {
        self.lastResult = result
    }

    func evalJs(_ js: String!) { }
    func evalJs(_ js: String!, scheduledOnRunLoop: Bool) { }
    func run(inBackground block: (() -> Void)!) { }
}

class MockWebViewEngine : NSObject, CDVWebViewEngineProtocol {
    var engineWebView: UIView

    func evaluateJavaScript(_ javaScriptString: String, completionHandler: ((Any, Error) -> Void)? = nil) {
        // TODO
    }

    func url() -> URL {
        return URL(string: "https://example.com")!
    }

    func canLoad(_ request: URLRequest) -> Bool {
        return false
    }

    required init?(frame: CGRect) {
        self.engineWebView = WKWebView()
    }
    required init?(frame: CGRect, configuration: WKWebViewConfiguration?) {
        self.engineWebView = WKWebView()
    }
    func update(withInfo info: [AnyHashable : Any]) { }
    func load(_ request: URLRequest) -> Any { return 0 }
    func loadHTMLString(_ string: String, baseURL: URL?) -> Any { return 0 }
}

// MARK: Test Cases
class OAuthPluginTests: XCTestCase {
    var plugin: OAuthPlugin!
    var cmdDlg: MockCommandDelegate!
    var webEngine: MockWebViewEngine!
    var vc: UIViewController!

    override func setUpWithError() throws {
        // Always reset this to the default
        OAuthPlugin.forcedVersion = UInt32.max

        vc = UIViewController()
        cmdDlg = MockCommandDelegate()
        webEngine = MockWebViewEngine(frame: CGRect(x: 0, y: 0, width: 0, height: 0))

        plugin = OAuthPlugin()
        plugin.commandDelegate = cmdDlg
        plugin.viewController = vc
        plugin.setValue(webEngine, forKey:"webViewEngine")
    }

    override func tearDownWithError() throws {
        cmdDlg = nil
        plugin = nil
        webEngine = nil
        vc = nil
    }

    func testCallbackScheme() throws {
        plugin.pluginInitialize()

        XCTAssertEqual(plugin.callbackScheme, "oauthtest://oauth_callback", "OAuth Callback Scheme did not match")
    }

    func testOAuthCommandArgument() throws {
        plugin.pluginInitialize()

        let emptycmd = CDVInvokedUrlCommand(arguments:[], callbackId:"", className:"CDVOAuthPlugin", methodName:"startOAuth")
        plugin.startOAuth(emptycmd!)
        XCTAssertEqual(cmdDlg.lastResult.status as! UInt, CDVCommandStatus.error.rawValue)
    }

    func testOAuthCommandURL() throws {
        plugin.pluginInitialize()

        let nonURLcmd = CDVInvokedUrlCommand(arguments:["Hello world!"], callbackId:"", className:"CDVOAuthPlugin", methodName:"startOAuth")
        plugin.startOAuth(nonURLcmd!)
        XCTAssertEqual(cmdDlg.lastResult.status as! UInt, CDVCommandStatus.error.rawValue)
    }

    func testSafariProvider() throws {
        OAuthPlugin.forcedVersion = 7
        plugin.pluginInitialize()

        let cmd = CDVInvokedUrlCommand(arguments:["https://example.com"], callbackId:"", className:"CDVOAuthPlugin", methodName:"startOAuth")
        plugin.startOAuth(cmd!)

        XCTAssertEqual(cmdDlg.lastResult.status as! UInt, CDVCommandStatus.ok.rawValue)
        XCTAssertTrue(plugin.authSystem is SafariAppOAuthSessionProvider)
    }

    func testSafariViewControllerProvider() throws {
        guard #available(iOS 9.0, *) else {
            throw XCTSkip("Only for iOS 9+")
        }

        OAuthPlugin.forcedVersion = 9
        plugin.pluginInitialize()

        let cmd = CDVInvokedUrlCommand(arguments:["https://example.com"], callbackId:"", className:"CDVOAuthPlugin", methodName:"startOAuth")
        plugin.startOAuth(cmd!)

        XCTAssertEqual(cmdDlg.lastResult.status as! UInt, CDVCommandStatus.ok.rawValue)
        XCTAssertTrue(plugin.authSystem is SFSafariViewControllerOAuthSessionProvider)
    }

    func testSFAuthenticationSessionProvider() throws {
        guard #available(iOS 11.0, *) else {
            throw XCTSkip("Only for iOS 11+")
        }

        OAuthPlugin.forcedVersion = 11
        plugin.pluginInitialize()

        let cmd = CDVInvokedUrlCommand(arguments:["https://example.com"], callbackId:"", className:"CDVOAuthPlugin", methodName:"startOAuth")
        plugin.startOAuth(cmd!)

        XCTAssertEqual(cmdDlg.lastResult.status as! UInt, CDVCommandStatus.ok.rawValue)
        XCTAssertTrue(plugin.authSystem is SFAuthenticationSessionOAuthSessionProvider)
    }

    func testASWebAuthenticationSessionProvider() throws {
        guard #available(iOS 12.0, *) else {
            throw XCTSkip("Only for iOS 12+")
        }

        OAuthPlugin.forcedVersion = 12
        plugin.pluginInitialize()

        let cmd = CDVInvokedUrlCommand(arguments:["https://example.com"], callbackId:"", className:"CDVOAuthPlugin", methodName:"startOAuth")
        plugin.startOAuth(cmd!)

        XCTAssertEqual(cmdDlg.lastResult.status as! UInt, CDVCommandStatus.ok.rawValue)
        XCTAssertTrue(plugin.authSystem is ASWebAuthenticationSessionOAuthSessionProvider)
    }
}
