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

/** Set of authentication elements we need to re-evaluate on login. */
const authElements = new Set();


/**
 * Click handler for the login button to start the OAuth flow.
 */
document.querySelectorAll('.login').forEach((btn) => {
  btn.addEventListener('click', () => {
    // Our fake OAuth redirecting page
    const url = 'https://ayogohealth.github.io/cordova-plugin-oauth/example/oauth_access_token.html';

    // Open a window with the "oauth:" prefix to trigger the plugin
    const hwnd = window.open(url, 'oauth:testpage');

    hwnd.addEventListener('close', (evt) => {
      if (!localStorage.getItem('access_token')) {
        localStorage.setItem('login_cancelled', 'true');

        // Re-evaluate the authentication elements
        authElements.forEach((el) => el.evaluateState());
      }
    });
  })
});


/**
 * Receiver for OAuth login postMessage data from the plugin.
 */
window.addEventListener('message', (evt) => {
  // Ensure this message is from the plugin, then parse the data
  if (evt.data.match(/^oauth::/)) {
    const data = JSON.parse(evt.data.substring(7));

    // Store the access token from the OAuth message data
    localStorage.setItem('access_token', data.access_token);

    // Re-evaluate the authentication elements
    authElements.forEach((el) => el.evaluateState());
  }
});


/**
 * Click handler for the logout button.
 */
document.getElementById('logout-button').addEventListener('click', () => {
  // Erase the access token in localStorage
  localStorage.clear();

  // Re-evaluate the authentication elements
  authElements.forEach((el) => el.evaluateState());
});


/**
 * Custom Element to conditionally show a slot based on authentication status.
 */
class AuthRouterElement extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: 'open' });
  }

  connectedCallback() {
    authElements.add(this);

    this.evaluateState();
  }

  disconnectedCallback() {
    authElements.delete(this);
  }

  evaluateState() {
    const access_token = localStorage.getItem('access_token');
    const login_cancelled = localStorage.getItem('login_cancelled');

    if (access_token && access_token != '') {
      this.shadowRoot.innerHTML = '<slot name="authenticated"></slot>';
    } else if (login_cancelled && login_cancelled != '') {
      this.shadowRoot.innerHTML = '<slot name="login_cancelled"></slot>';
    } else {
      this.shadowRoot.innerHTML = '<slot name="unauthenticated"></slot>';
    }
  }
}
window.customElements.define('auth-router', AuthRouterElement);
