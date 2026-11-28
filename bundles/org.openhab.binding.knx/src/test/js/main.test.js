import { test } from 'node:test';
import assert from 'node:assert/strict';

import { createDocument, textNodes } from './fakedom.js';
import { createEventBody, createReadOnlyStorage, createStorage, json, settle } from './fakes.js';

// main.js is the one module that reads the browser's globals at load; here it gets a stand-in window.

function standIn({ session, local, assigned, listeners, fetched, answer, hash = '#/b/knx%3Aip%3Ax/setup' }) {
    const document = createDocument();
    const parts = {
        header: document.createElement('header'),
        alert: document.createElement('div'),
        main: document.createElement('main'),
    };
    document.getElementById = (id) => parts[id] ?? null;
    document.documentElement = document.createElement('html');
    parts.root = document.documentElement;
    const window = {
        location: {
            href: 'http://openhab.invalid/knx/',
            hash,
            assign: (address) => assigned.push(address),
        },
        document,
        sessionStorage: session,
        localStorage: local,
        crypto: { getRandomValues: (bytes) => bytes.fill(7), subtle: undefined },
        history: { replaceState() {} },
        navigator: {},
        fetch(input, init) {
            fetched.push([String(input), this === window]);
            return answer ? answer(new URL(String(input)), init ?? {}) : Promise.reject(new TypeError('Failed to fetch'));
        },
        setTimeout: () => 1,
        clearTimeout() {},
        setInterval: () => 1,
        clearInterval() {},
        requestAnimationFrame: () => 1,
        confirm: () => false,
        addEventListener: (type, listener) => {
            listeners[type] = listener;
        },
    };
    return { window, parts };
}

test('main: with nothing kept the page starts signed out, reads only its own layout and MainUI key of local storage, and signs in', async () => {
    const session = createStorage();
    const local = createReadOnlyStorage({});
    const listeners = {};
    const fetched = [];
    const assigned = [];
    const { window, parts } = standIn({ session, local, assigned, listeners, fetched });
    globalThis.window = window;
    await import('../web/js/main.js');
    await settle();

    assert.deepEqual(Object.keys(listeners).sort(), ['hashchange', 'pagehide', 'pageshow']);
    assert.deepEqual(fetched, []);
    // the layout holds the theme, which is needed before anything is drawn
    assert.deepEqual(local.reads, ['knx.layout', 'openhab.ui:refreshToken']);
    assert.equal(parts.root.hasAttribute('data-theme'), false);
    const button = parts.main.querySelector('button');
    assert.equal(button.textContent, 'Sign in with openHAB');
    button.click();
    await settle();
    assert.equal(assigned.length, 1);
    const login = new URL(assigned[0]);
    assert.equal(login.origin + login.pathname, 'http://openhab.invalid/auth');
    assert.equal(login.searchParams.get('client_id'), 'http://openhab.invalid/knx/');
    assert.equal(session.getItem('knx.loginRoute'), '#/b/knx%3Aip%3Ax/setup');
    assert.ok(session.touched().every((key) => key.startsWith('knx.')));
    listeners.pagehide();
});

test('main: fetch is called on window, as a browser needs it', async () => {
    const session = createStorage({ 'knx.refreshToken': 'own-r' });
    const fetched = [];
    const listeners = {};
    const { window, parts } = standIn({ session, local: createReadOnlyStorage({}), assigned: [], listeners, fetched });
    globalThis.window = window;
    await import('../web/js/main.js?again');
    await settle();
    assert.deepEqual(fetched, [['http://openhab.invalid/rest/auth/token', true]]);
    assert.equal(parts.main.querySelector('button').textContent, 'Try again');
    assert.equal(session.getItem('knx.refreshToken'), 'own-r');
    listeners.pagehide();
});

test('main: the Monitor of a bridge gets the row model and the stream, which reads call 4 and opens call 20', async () => {
    const session = createStorage({ 'knx.refreshToken': 'own-r' });
    const fetched = [];
    const listeners = {};
    const bodies = [];
    const bridge = { uid: 'knx:ip:x', label: 'X', present: true, status: 'ONLINE', bufferSize: 100 };
    const answer = async (url, init) => {
        switch (url.pathname) {
            case '/rest/auth/token':
                return json(200, { access_token: 'a1', refresh_token: 'own-r', expires_in: 3600, user: { name: 'ann', roles: ['administrator'] } });
            case '/rest/knx/bridges':
                return json(200, [bridge]);
            case '/rest/knx/bridges/knx%3Aip%3Ax/monitor':
                return json(200, { received: 1, sent: 0, notDecrypted: {}, notDecryptedOtherReason: 0, plainOnSecuredAddress: 0 });
            case '/rest/knx/bridges/knx%3Aip%3Ax/telegrams':
                return json(200, { telegrams: [], lastNumber: 4, monitorId: 'm1', more: false, reset: false });
            case '/rest/knx/bridges/knx%3Aip%3Ax/telegrams/stream': {
                const body = createEventBody(init.signal);
                bodies.push(body);
                body.send('open', { lastNumber: 4, monitorId: 'm1' });
                body.send('telegram', { number: 5, time: '2026-11-15T07:01:02Z', source: '7.7.1', destination: '31/7/1', type: 'WRITE', direction: 'IN', value: 'ON', security: 'PLAIN' });
                return body.response;
            }
            default:
                return json(404, { code: 'unknown', text: 'x' });
        }
    };
    const { window, parts } = standIn({ session, local: createReadOnlyStorage({}), assigned: [], listeners, fetched, answer, hash: '#/b/knx%3Aip%3Ax/monitor' });
    globalThis.window = window;
    await import('../web/js/main.js?stream');
    await settle();
    const paths = fetched.map(([address]) => new URL(address).pathname);
    assert.ok(paths.includes('/rest/knx/bridges/knx%3Aip%3Ax/telegrams/stream'), paths.join());
    assert.equal(parts.header.querySelector('.stream').textContent, 'Live');
    assert.equal(parts.main.querySelector('h2').textContent, 'Group Monitor');
    listeners.pagehide();
    assert.equal(bodies[0].aborted(), true);
});

function monitorAnswer(bodies) {
    const bridge = { uid: 'knx:ip:x', label: 'X', present: true, status: 'ONLINE', bufferSize: 100 };
    return async (url, init) => {
        switch (url.pathname) {
            case '/rest/auth/token':
                return json(200, { access_token: 'a1', refresh_token: 'own-r', expires_in: 3600, user: { name: 'ann', roles: ['administrator'] } });
            case '/rest/knx/bridges':
                return json(200, [bridge]);
            case '/rest/knx/bridges/knx%3Aip%3Ax/monitor':
                return json(200, { received: 1, sent: 0, notDecrypted: {}, notDecryptedOtherReason: 0, plainOnSecuredAddress: 0 });
            case '/rest/knx/bridges/knx%3Aip%3Ax/telegrams':
                return json(200, { telegrams: [], lastNumber: 4, monitorId: 'm1', more: false, reset: false });
            case '/rest/knx/bridges/knx%3Aip%3Ax/telegrams/stream': {
                const body = createEventBody(init.signal);
                bodies.push(body);
                body.send('open', { lastNumber: 4, monitorId: 'm1' });
                return body.response;
            }
            default:
                return json(404, { code: 'unknown', text: 'x' });
        }
    };
}

test('main: a page the browser kept and shows again draws its view again with one stream; a normal load changes nothing', async () => {
    const session = createStorage({ 'knx.refreshToken': 'own-r' });
    const fetched = [];
    const listeners = {};
    const bodies = [];
    const { window, parts } = standIn({
        session,
        local: createReadOnlyStorage({}),
        assigned: [],
        listeners,
        fetched,
        answer: monitorAnswer(bodies),
        hash: '#/b/knx%3Aip%3Ax/monitor',
    });
    globalThis.window = window;
    await import('../web/js/main.js?kept');
    await settle();
    assert.deepEqual(Object.keys(listeners).sort(), ['hashchange', 'pagehide', 'pageshow']);
    const heading = parts.main.querySelector('h2');
    const count = window.document.listenerCount();
    const calls = fetched.length;

    listeners.pageshow({ persisted: false });
    await settle();
    assert.equal(fetched.length, calls);
    assert.equal(bodies.length, 1);
    assert.equal(parts.main.querySelector('h2'), heading);
    assert.equal(window.document.listenerCount(), count);

    listeners.pagehide({ persisted: true });
    assert.equal(bodies[0].aborted(), true);
    assert.equal(parts.main.querySelector('h2'), null);
    listeners.pageshow({ persisted: true });
    await settle();
    assert.equal(parts.main.querySelector('h2').textContent, 'Group Monitor');
    assert.equal(parts.header.querySelector('.stream').textContent, 'Live');
    assert.equal(bodies.length, 2);
    assert.equal(bodies.filter((body) => !body.aborted()).length, 1);
    assert.equal(window.document.listenerCount(), count);
    listeners.pagehide({ persisted: false });
    assert.ok(bodies.every((body) => body.aborted()));
});

test('main: a signed-out page that the browser kept and shows again still has its Sign in button', async () => {
    const session = createStorage();
    const fetched = [];
    const assigned = [];
    const listeners = {};
    const { window, parts } = standIn({ session, local: createReadOnlyStorage({}), assigned, listeners, fetched });
    globalThis.window = window;
    await import('../web/js/main.js?keptSignedOut');
    await settle();
    assert.equal(parts.main.querySelector('button').textContent, 'Sign in with openHAB');

    listeners.pagehide({ persisted: true });
    listeners.pageshow({ persisted: true });
    await settle();
    assert.equal(parts.main.querySelector('h2').textContent, 'You are signed out.');
    const button = parts.main.querySelector('button');
    assert.equal(button.textContent, 'Sign in with openHAB');
    button.click();
    await settle();
    assert.equal(assigned.length, 1);
    assert.deepEqual(fetched, []);
    listeners.pagehide({ persisted: false });
});

function blockStorage(window, names) {
    for (const name of names) {
        Object.defineProperty(window, name, {
            get() {
                throw new DOMException('The operation is insecure.', 'SecurityError');
            },
        });
    }
}

test('main: a browser that blocks storage gets the signed-out page with its one text, no sign-in and no exception', async () => {
    const fetched = [];
    const assigned = [];
    const listeners = {};
    const { window, parts } = standIn({ session: null, local: null, assigned, listeners, fetched });
    blockStorage(window, ['sessionStorage', 'localStorage']);
    globalThis.window = window;
    await import('../web/js/main.js?blocked');
    await settle();
    assert.equal(parts.main.querySelector('h2').textContent, 'You are signed out.');
    assert.ok(textNodes(parts.main).includes('This browser blocks storage for this page, and signing in needs it.'));
    assert.deepEqual(parts.main.querySelectorAll('button'), []);
    assert.equal(parts.header.querySelector('.sign-out'), null);
    assert.deepEqual(fetched, []);
    assert.deepEqual(assigned, []);
    assert.deepEqual(Object.keys(listeners), []);
});

for (const [name, value] of [['null', null], ['undefined', undefined]]) {
    test(`main: a browser whose session storage is ${name} (no exception) is treated as blocked`, async () => {
        const fetched = [];
        const assigned = [];
        const listeners = {};
        const { window, parts } = standIn({ session: value, local: createReadOnlyStorage({}), assigned, listeners, fetched });
        globalThis.window = window;
        await import(`../web/js/main.js?${name}Session`);
        await settle();
        assert.equal(parts.main.querySelector('h2').textContent, 'You are signed out.');
        assert.ok(textNodes(parts.main).includes('This browser blocks storage for this page, and signing in needs it.'));
        assert.deepEqual(parts.main.querySelectorAll('button'), []);
        assert.equal(parts.header.querySelector('.sign-out'), null);
        assert.deepEqual(fetched, []);
        assert.deepEqual(assigned, []);
        assert.deepEqual(Object.keys(listeners), []);
    });
}

test('main: a browser that blocks only local storage starts signed out without MainUI\'s login, and signing in works', async () => {
    const session = createStorage();
    const fetched = [];
    const assigned = [];
    const listeners = {};
    const { window, parts } = standIn({ session, local: null, assigned, listeners, fetched });
    blockStorage(window, ['localStorage']);
    globalThis.window = window;
    await import('../web/js/main.js?blockedLocal');
    await settle();
    assert.deepEqual(fetched, []);
    const button = parts.main.querySelector('button');
    assert.equal(button.textContent, 'Sign in with openHAB');
    button.click();
    await settle();
    assert.equal(assigned.length, 1);
    listeners.pagehide();
});

test('main: the wizard reaches openHAB\'s own addresses below the REST root of the page', async () => {
    const session = createStorage({ 'knx.refreshToken': 'own-r' });
    const fetched = [];
    const methods = [];
    const bridge = { uid: 'knx:ip:x', label: 'X', present: true, status: 'ONLINE', bufferSize: 0 };
    const group = { kind: 'GROUP', itemName: 'gMadeUp', item: { type: 'Group', name: 'gMadeUp', label: 'Made up', tags: ['Room'], groupNames: [] } };
    const answer = async (url, init) => {
        methods.push(`${init.method ?? 'GET'} ${url.pathname}`);
        switch (url.pathname) {
            case '/rest/auth/token':
                return json(200, { access_token: 'a1', refresh_token: 'own-r', expires_in: 3600, user: { name: 'ann', roles: ['administrator'] } });
            case '/rest/knx/bridges':
                return json(200, [bridge]);
            case '/rest/knx/bridges/knx%3Aip%3Ax/project/locations':
                return json(200, []);
            case '/rest/knx/bridges/knx%3Aip%3Ax/project/devices':
                return json(200, { items: [], total: 0, offset: 0 });
            case '/rest/knx/bridges/knx%3Aip%3Ax/project/group-addresses':
                return json(200, { items: [{ address: '31/7/3', name: 'Made-up value' }], total: 1, offset: 0 });
            case '/rest/knx/bridges/knx%3Aip%3Ax/project/proposal':
                return json(200, { things: [], rooms: [], unknown: [], readAtStart: true, thingsWithoutHandler: 0 });
            case '/rest/knx/bridges/knx%3Aip%3Ax/project/proposal/preview':
                return json(200, { problems: [], readAddresses: [], readTelegrams: 0, counts: { things: 0, channels: 0, items: 0, links: 0, groups: 1 }, steps: [group] });
            case '/rest/items/gMadeUp':
                return init.method === 'PUT' ? new Response(null, { status: 201 }) : json(404, {});
            default:
                return json(404, { code: 'unknown', text: 'x' });
        }
    };
    const { window, parts } = standIn({ session, local: createReadOnlyStorage({}), assigned: [], listeners: {}, fetched, answer, hash: '#/b/knx%3Aip%3Ax/wizard' });
    window.confirm = () => true;
    globalThis.window = window;
    await import('../web/js/main.js?wizard');
    await settle();
    assert.equal(parts.main.querySelector('h2').textContent, 'Add to openHAB');
    parts.main.querySelector('.pick-address').click();
    parts.main.querySelector('.propose').click();
    await settle();
    parts.main.querySelector('.create').click();
    parts.main.querySelector('.confirm-create').click();
    await settle();
    assert.deepEqual(
        methods.filter((line) => !line.includes('/rest/knx/') && !line.includes('/rest/auth/')),
        ['GET /rest/items/gMadeUp', 'GET /rest/items/gMadeUp', 'PUT /rest/items/gMadeUp'],
    );
    // what the run left to undo is counted in the tab's own store, under the page's key; local storage gets nothing
    assert.deepEqual(JSON.parse(session.getItem('knx.tab')), { 'wizard.run:knx:ip:x': 1 });
    // the page loaded again in the same tab: the undo list is gone, and the wizard says so
    const again = standIn({ session, local: createReadOnlyStorage({}), assigned: [], listeners: {}, fetched: [], answer, hash: '#/b/knx%3Aip%3Ax/wizard' });
    globalThis.window = again.window;
    await import('../web/js/main.js?wizard-again');
    await settle();
    assert.match(again.parts.main.querySelector('.run-gone').textContent, /^The last run made 1 Things, Items, links and groups that were not undone\./);
    assert.equal(again.parts.main.querySelector('.last-run'), null);
});

test('main: the width of a pane is kept in local storage under the page\'s own key and comes back; a store that refuses changes nothing else', async () => {
    const bodies = [];
    const open = async (local, tag) => {
        const { window, parts } = standIn({ session: createStorage({ 'knx.refreshToken': 'own-r' }), local, assigned: [], listeners: {}, fetched: [], answer: monitorAnswer(bodies), hash: '#/b/knx%3Aip%3Ax/monitor' });
        globalThis.window = window;
        await import(`../web/js/main.js?layout-${tag}`);
        await settle();
        return parts;
    };
    const local = createStorage({});
    const first = await open(local, 'first');
    const splitter = first.main.querySelector('.splitter');
    assert.equal(splitter.getAttribute('aria-valuenow'), '300');
    splitter.fire('keydown', { key: 'ArrowLeft' });
    assert.deepEqual(local.touched(), ['knx.layout']);
    assert.deepEqual(JSON.parse(local.getItem('knx.layout')), { properties: 316 });
    const second = await open(local, 'second');
    assert.equal(second.main.querySelector('.splitter').getAttribute('aria-valuenow'), '316');
    assert.equal(second.main.querySelector('.workplace').style.getPropertyValue('--properties-width'), '316px');

    first.header.querySelector('.theme-dark').click();
    assert.equal(first.root.getAttribute('data-theme'), 'dark');
    assert.equal(JSON.parse(local.getItem('knx.layout')).theme, 'dark');
    const dark = await open(local, 'dark');
    assert.equal(dark.root.getAttribute('data-theme'), 'dark', 'the kept theme is on the root when the page starts');

    const refusing = createReadOnlyStorage({});
    const third = await open(refusing, 'third');
    third.main.querySelector('.splitter').fire('keydown', { key: 'ArrowLeft' });
    assert.equal(third.main.querySelector('.splitter').getAttribute('aria-valuenow'), '316');
    assert.deepEqual(refusing.reads.filter((key) => key !== 'openhab.ui:refreshToken'), ['knx.layout']);

    const fourth = await open(null, 'fourth');
    fourth.main.querySelector('.splitter').fire('keydown', { key: 'ArrowLeft' });
    assert.equal(fourth.main.querySelector('.splitter').getAttribute('aria-valuenow'), '316');
    assert.equal(fourth.main.querySelector('h2').textContent, 'Group Monitor');
});

test('main: two panels need a page wider than 800px; when the page grows or shrinks it is shown again', async () => {
    const session = createStorage({ 'knx.refreshToken': 'own-r' });
    const listeners = {};
    const { window, parts } = standIn({ session, local: createReadOnlyStorage({}), assigned: [], listeners, fetched: [], answer: monitorAnswer([]), hash: '#/b/knx%3Aip%3Ax/monitor?p2=monitor' });
    const asked = [];
    const media = { matches: false, addEventListener: (type, listener) => (media[type] = listener) };
    window.matchMedia = (query) => (asked.push(query), media);
    globalThis.window = window;
    await import('../web/js/main.js?wide');
    await settle();
    assert.deepEqual(asked, ['(min-width: 801px)']);
    assert.equal(parts.main.querySelectorAll('h2').length, 1);
    assert.equal(parts.main.querySelector('.panels-menu'), null);
    media.matches = true;
    media.change();
    await settle();
    assert.equal(parts.main.querySelectorAll('h2').length, 2);
    assert.equal(parts.main.querySelectorAll('.sidebar').length, 1);
    assert.equal(parts.main.querySelector('.second-panel').textContent, 'Close second panel');
    media.matches = false;
    media.change();
    await settle();
    assert.equal(parts.main.querySelectorAll('h2').length, 1);
    listeners.pagehide();
});
