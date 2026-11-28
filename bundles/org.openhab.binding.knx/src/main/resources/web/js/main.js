// The start of the page and the only module that touches the browser's globals: it hands each part what it needs.

import { createApi } from './api.js';
import { createAuth } from './auth.js';
import { createLayout } from './layout.js';
import { createRows } from './rows.js';
import { createShell } from './shell.js';
import { createStream } from './stream.js';

const page = window.location.href;
const base = new URL('../rest/', page);
const doc = window.document;
const fetcher = (input, init) => window.fetch(input, init);
const timers = {
    setTimeout: (callback, ms) => window.setTimeout(callback, ms),
    clearTimeout: (id) => window.clearTimeout(id),
    setInterval: (callback, ms) => window.setInterval(callback, ms),
    clearInterval: (id) => window.clearInterval(id),
};

let shell = null;

// the width from which the page has room for two panels: where the style sheet's narrow layout ends
const wide = typeof window.matchMedia === 'function' ? window.matchMedia('(min-width: 801px)') : null;

function sessionStorageOrNull() {
    try {
        return window.sessionStorage ?? null;
    } catch {
        return null;
    }
}

const session = sessionStorageOrNull();

const auth = createAuth({
    fetch: fetcher,
    base,
    address: page,
    session,
    local: { getItem: (key) => window.localStorage.getItem(key) },
    timers,
    now: () => Date.now(),
    randomBytes: (length) => window.crypto.getRandomValues(new Uint8Array(length)),
    subtle: window.crypto.subtle,
    replaceAddress: (address) => window.history.replaceState(null, '', address),
    onSignedOut: (message) => shell?.signedOut({ message }),
});

const api = createApi({ fetch: fetcher, base, auth });

// what the viewer set (pane and column widths, the theme, the open containers): the one thing the page writes to
// this store, under its own key
const layout = createLayout({
    read: () => window.localStorage.getItem('knx.layout'),
    write: (value) => window.localStorage.setItem('knx.layout', value),
});

// what must outlive a reload of the page in this tab and no longer: the count of what the wizard's last run left to
// undo, so the page can say that its undo list is gone. A count only, under the page's own key.
const tab = createLayout(
    session === null
        ? null
        : {
              read: () => session.getItem('knx.tab'),
              write: (value) => session.setItem('knx.tab', value),
          },
);

shell = createShell({
    document: doc,
    header: doc.getElementById('header'),
    alert: doc.getElementById('alert'),
    main: doc.getElementById('main'),
    status: doc.getElementById('status'),
    root: doc.documentElement,
    history: { back: () => window.history.back(), forward: () => window.history.forward() },
    wide: () => wide === null || wide.matches === true,
    layout,
    tab,
    api,
    auth,
    createRows: (bridge) => createRows({ bridge: bridge.uid, api }),
    createStream: (bridge, rows) => createStream({ bridge: bridge.uid, rows, api, auth, fetch: fetcher, base, timers, now: () => Date.now() }),
    fetch: fetcher,
    base,
    timers,
    frame: (callback) => window.requestAnimationFrame(callback),
    confirm: (message) => window.confirm(message),
    clipboard: window.navigator.clipboard,
    location: {
        hash: () => window.location.hash,
        setHash: (hash) => {
            window.location.hash = hash;
        },
        replaceHash: (hash) => window.history.replaceState(null, '', hash),
        assign: (address) => window.location.assign(address),
    },
});

if (session === null) {
    shell.signedOut({ message: 'login.storageBlocked', final: true });
} else {
    window.addEventListener('hashchange', () => shell.showHash(window.location.hash));
    window.addEventListener('pagehide', () => shell.stop());
    wide?.addEventListener?.('change', () => shell.showHash(window.location.hash, true));
    window.addEventListener('pageshow', (event) => {
        if (event.persisted) {
            shell.showHash(window.location.hash);
        }
    });
    shell.begin();
}
