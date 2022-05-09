<!--
  Copyright 2021-2022 Ayogo Health Inc.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      https://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

cordova-plugin-oauth
====================

This plugin provides a mechanism for showing an OAuth flow in a secured
system-provided browser view and receiving the result via a Message event.

On iOS, this uses ASWebAuthenticationSession (iOS >=12),
SFAuthenticationSession (iOS 11), SFSafariViewController (iOS >=8, <=10),
falling back to Safari.

On Android, this uses Custom Tabs in the user's preferred web application,
falling back to their browser.

**Note:** This plugin might conflict with the Cordova In-App Browser plugin!

> ℹ️ **This plugin uses AndroidX!**
>
> Use version 3.x if you are building without AndroidX enabled.


Installation
------------

```
cordova plugin add cordova-plugin-oauth [--variable URL_SCHEME=mycoolapp]
```

By default, the plugin registers the app ID as a scheme to be used as the
OAuth callback URL, and expects a host of `oauth_callback` (i.e., if your
app's ID is `com.example.foo`, your OAuth redirect URL should be
`com.example.foo://oauth_callback`).

The scheme for the OAuth callback URL can be changed by providing a
`URL_SCHEME` variable when installing. If your `URL_SCHEME` is `mycoolapp`,
then your OAuth redirect URL should be `mycoolapp://oauth_callback`.


Supported Platforms
-------------------

* **iOS** (cordova-ios >= 6.1.0)
* **Android** (cordova-android >= 9.0.0)


Usage
-----

1.  Trigger the plugin by opening the OAuth login page in a new window, with a
    window name that includes `"oauth:"`:

    ```javascript
    var endpoint = 'https://accounts.google.com/o/oauth2/v2/auth?....';
    window.open(endpoint, 'oauth:google', '');
    ```

2.  The plugin will open the OAuth login page in a new browser window.

3.  When the OAuth process is complete and it redirects to your app scheme, the
    plugin will send a message to the Cordova app.

    The message begins with `oauth::` and is followed by a JSON object
    containing all of the key/value pairs from the OAuth redirect query string.

    ```javascript
    // Called from a callback URL like
    // com.example.foo://oauth_callback?code=b10a8db164e0754105b7a99be72e3fe5
    // it would be received in JavaScript like this:

    window.addEventListener('message', function(event) {
      if (event.data.match(/^oauth::/)) {
        var data = JSON.parse(event.data.substring(7));

        // Use data.code
      }
    }
    ```

    The advantage of this implementation is that it can be made to work with
    Universal Links and App Links so that the same flow can be used in both
    mobile apps and web.

    If the login was cancelled by the user without interacting with the 
    endpoint by closing the overlay without logging in, the received message 
    in JavaScript will be `oauth::{"error": "cancelled"}`.


Contributing
------------

Contributions of bug reports, feature requests, and pull requests are greatly
appreciated!

Please note that this project is released with a [Contributor Code of
Conduct][coc]. By participating in this project you agree to abide by its
terms.


Licence
-------

Released under the Apache 2.0 Licence.  
Copyright © 2020-2022 Ayogo Health Inc.

[coc]: https://github.com/AyogoHealth/cordova-plugin-oauth/blob/main/CODE_OF_CONDUCT.md
