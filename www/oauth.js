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

// https://github.com/ungap/event-target
/**
 * Copyright (c) 2018, Andrea Giammarchi, @WebReflection
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
 * LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE
 * OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THIS SOFTWARE.
 */
var EventTargetPolyfill = (function(Object, wm) {
  var create = Object.create;
  var defineProperty = Object.defineProperty;
  var proto = EventTarget.prototype;
  define(proto, 'addEventListener', function (type, listener, options) {
    for (var
      secret = wm.get(this),
      listeners = secret[type] || (secret[type] = []),
      i = 0, length = listeners.length; i < length; i++
    ) {
      if (listeners[i].listener === listener)
        return;
    }
    listeners.push({target: this, listener: listener, options: options});
  });
  define(proto, 'dispatchEvent', function (event) {
    var secret = wm.get(this);
    var listeners = secret[event.type];
    if (listeners) {
      define(event, 'target', this);
      define(event, 'currentTarget', this);
      listeners.slice(0).some(dispatch, event);
      delete event.currentTarget;
      delete event.target;
    }
    return true;
  });
  define(proto, 'removeEventListener', function (type, listener) {
    for (var
      secret = wm.get(this),
      /* istanbul ignore next */
      listeners = secret[type] || (secret[type] = []),
      i = 0, length = listeners.length; i < length; i++
    ) {
      if (listeners[i].listener === listener) {
        listeners.splice(i, 1);
        return;
      }
    }
  });
  return EventTarget;

  function EventTarget() {'use strict';
    wm.set(this, create(null));
  }
  function define(target, name, value) {
    defineProperty(
      target,
      name,
      {
        configurable: true,
        writable: true,
        value: value
      }
    );
  }
  function dispatch(info) {
    var options = info.options;
    if (options && options.once)
      info.target.removeEventListener(this.type, info.listener);
    if (typeof info.listener === 'function')
      info.listener.call(info.target, this);
    else
      info.listener.handleEvent(this);
    return this._stopImmediatePropagationFlag;
  }
})(Object, new WeakMap());


module.exports = function(url, name, features) {
  if (name && name.match && name.match(/^oauth:/)) {
    var wnd = null;
    if (window.EventTarget) {
      wnd = new EventTarget();
    } else {
      wnd = new EventTargetPolyfill();
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
