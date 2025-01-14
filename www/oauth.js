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

var exec = require('cordova/exec');
var modulemapper = require('cordova/modulemapper');
var noop = function() { };


module.exports = function(url, name, features) {
  if (name && name.match && name.match(/^oauth:/)) {
    var wnd = null;
    if (window.EventTarget) {
      wnd = new EventTarget();
    }

    function success() {
      if (wnd) {
        if (wnd.onclose) {
          wnd.onclose();
        }
        wnd.dispatchEvent(new Event('close'));
      }
    }

    cordova.exec(success, noop, 'OAuth', 'startOAuth', [url]);

    return wnd;
  } else {
    var originalWindowOpen = modulemapper.getOriginalSymbol(window, 'open');
    return originalWindowOpen.apply(window, arguments);
  }
};
