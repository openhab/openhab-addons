import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

import { KnxApiError, createApi } from '../web/js/api.js';
import { createRows } from '../web/js/rows.js';
import { createLayout } from '../web/js/layout.js';
import { CONTEXT_KEYS, THEME_CHOICES, VIEW_MODULES, createShell } from '../web/js/shell.js';
import { createStream } from '../web/js/stream.js';
import { allElements, createDocument, textNodes } from './fakedom.js';
import { createEventBody, createFetch, createTimers, json, settle } from './fakes.js';

const PRESENT = { uid: 'knx:ip:line7', label: 'Line <7>', present: true, status: 'ONLINE', connection: 'SECURE_TUNNEL', projectState: 'LOADED' };
const OTHER = { uid: 'knx:ip:other', label: 'Other', present: true, status: 'OFFLINE', connection: 'TUNNEL', projectState: 'NONE' };
const REMOVED = { uid: 'knx:ip:gone', present: false, projectState: 'LOADED' };

function fakeAuth(outcome = { signedIn: true, user: { name: 'ann' } }) {
    let signedIn = false;
    const auth = {
        calls: [],
        outcomes: [outcome],
        async start() {
            const next = auth.outcomes.length > 1 ? auth.outcomes.shift() : auth.outcomes[0];
            auth.calls.push('start');
            signedIn = next.signedIn;
            return next;
        },
        signedIn: () => signedIn,
        user: () => (signedIn ? { name: '<i>ann</i>' } : null),
        async signOut() {
            auth.calls.push('signOut');
            signedIn = false;
        },
        async signIn(route) {
            auth.calls.push(['signIn', route]);
            return 'http://openhab.invalid/auth?response_type=code';
        },
        token: async () => 't',
        renew: async () => 't',
        lost() {
            signedIn = false;
        },
    };
    return auth;
}

function fakeLocation(hash = '') {
    return {
        current: hash,
        set: [],
        replaced: [],
        assigned: [],
        hash() {
            return this.current;
        },
        setHash(value) {
            this.current = value;
            this.set.push(value);
        },
        replaceHash(value) {
            this.current = value;
            this.replaced.push(value);
        },
        assign(address) {
            this.assigned.push(address);
        },
    };
}

function setup({ bridges = [PRESENT, OTHER, REMOVED], hash = '', auth = fakeAuth(), views, createRows, createStream, layout, root, wide } = {}) {
    const document = createDocument();
    const header = document.createElement('header');
    const alert = document.createElement('div');
    alert.setAttribute('role', 'alert');
    const main = document.createElement('main');
    const location = fakeLocation(hash);
    const api = {
        calls: 0,
        async bridges() {
            api.calls++;
            if (api.fail) {
                throw api.fail;
            }
            return bridges;
        },
    };
    const extras = { base: new URL('http://openhab.invalid/rest/'), fetch: async () => null, timers: { setTimeout() {}, clearTimeout() {}, setInterval() {}, clearInterval() {} }, frame() {}, confirm: () => true, clipboard: { writeText: async () => {} } };
    const options = { document, header, alert, main, api, auth, location, layout, root, ...extras };
    if (views) {
        options.views = views;
    }
    if (createRows) {
        options.createRows = createRows;
    }
    if (createStream) {
        options.createStream = createStream;
    }
    if (wide) {
        options.wide = wide;
    }
    const shell = createShell(options);
    const go = async (value) => {
        location.current = value;
        await shell.showHash(value);
        await settle();
    };
    return { document, header, alert, main, location, api, auth, shell, go, extras };
}

function title(main) {
    return main.querySelector('h2')?.textContent;
}

function navTexts(header) {
    const nav = header.querySelector('nav');
    return nav ? nav.querySelectorAll('a').map((a) => [a.textContent, a.getAttribute('href'), a.classList.contains('current')]) : [];
}

test('shell: signed out shows one button "Sign in with openHAB" that goes to the login with the wanted route', async () => {
    const s = setup({ auth: fakeAuth({ signedIn: false }), hash: '#/b/knx%3Aip%3Aline7/buildings' });
    await s.shell.begin();
    const buttons = s.main.querySelectorAll('button');
    assert.deepEqual(buttons.map((b) => b.textContent), ['Sign in with openHAB']);
    assert.equal(s.api.calls, 0);
    assert.equal(s.header.querySelector('nav'), null);
    buttons[0].click();
    await settle();
    assert.deepEqual(s.auth.calls, ['start', ['signIn', '#/b/knx%3Aip%3Aline7/buildings']]);
    assert.deepEqual(s.location.assigned, ['http://openhab.invalid/auth?response_type=code']);
});

test('shell: the signed-out page shows the text of the login, and "Try again" when openHAB cannot be reached', async () => {
    const foreign = setup({ auth: fakeAuth({ signedIn: false, message: 'login.foreign' }) });
    await foreign.shell.begin();
    assert.ok(textNodes(foreign.main).includes('The login answer did not belong to this page'));

    const auth = fakeAuth();
    auth.outcomes = [{ signedIn: false, message: 'login.unreachable', retry: true }, { signedIn: true, user: { name: 'ann' } }];
    const s = setup({ auth, bridges: [PRESENT] });
    await s.shell.begin();
    assert.ok(textNodes(s.main).includes('openHAB cannot be reached'));
    const buttons = s.main.querySelectorAll('button');
    assert.deepEqual(buttons.map((b) => b.textContent), ['Try again']);
    buttons[0].click();
    await settle();
    assert.deepEqual(auth.calls, ['start', 'start']);
    assert.equal(title(s.main), 'Group Addresses');
});

test('shell: the bridge chooser shows label, status, connection kind and "removed", all as text', async () => {
    const s = setup();
    await s.shell.begin();
    assert.equal(title(s.main), 'KNX bridges');
    const items = s.main.querySelectorAll('li');
    assert.equal(items.length, 3);
    assert.deepEqual(textNodes(items[0]), ['Line <7>', 'ONLINE', 'secure tunnel']);
    assert.deepEqual(textNodes(items[1]), ['Other', 'OFFLINE', 'tunnel']);
    assert.deepEqual(textNodes(items[2]), ['knx:ip:gone', 'removed']);
    assert.deepEqual(
        items.map((item) => item.querySelector('a').getAttribute('href')),
        ['#/b/knx%3Aip%3Aline7/group-addresses', '#/b/knx%3Aip%3Aother/monitor', '#/b/knx%3Aip%3Agone/group-addresses'],
    );
    assert.ok(allElements(s.main).every((element) => !['B', 'I', 'IMG', 'SCRIPT'].includes(element.tagName)));
});

test('shell: no bridge says so, and a failed list shows its error under the header', async () => {
    const none = setup({ bridges: [] });
    await none.shell.begin();
    assert.ok(textNodes(none.main).includes('There is no KNX bridge in openHAB.'));
    const failed = setup();
    failed.api.fail = new KnxApiError(0, 'network');
    await failed.shell.begin();
    assert.equal(failed.alert.textContent, 'openHAB cannot be reached.');
    assert.equal(failed.main.querySelectorAll('li').length, 0);
    assert.ok(!textNodes(failed.main).includes('There is no KNX bridge in openHAB.'));
});

test('shell: with exactly one bridge the chooser goes on to its first panel: Buildings when the project has spaces, else Group Addresses, without a project the Group Monitor', async () => {
    const views = { ...VIEW_MODULES, buildings: { show: () => ({ close() {} }) } };
    const built = setup({ views, bridges: [{ ...PRESENT, projectSpaces: 1 }] });
    await built.shell.begin();
    assert.deepEqual(built.location.replaced, ['#/b/knx%3Aip%3Aline7/buildings']);
    const s = setup({ bridges: [PRESENT] });
    await s.shell.begin();
    assert.equal(title(s.main), 'Group Addresses');
    assert.deepEqual(s.location.replaced, ['#/b/knx%3Aip%3Aline7/group-addresses']);
    const bare = setup({ bridges: [OTHER] });
    await bare.shell.begin();
    assert.equal(title(bare.main), 'Group Monitor');
    assert.deepEqual(bare.location.replaced, ['#/b/knx%3Aip%3Aother/monitor']);
    const removed = setup({ bridges: [REMOVED] });
    await removed.shell.begin();
    assert.equal(title(removed.main), 'Group Addresses');
});

test('shell: the tabs of a present bridge are the five panels in their order, without a Devices tab; of a removed bridge four without the Group Monitor', async () => {
    const s = setup();
    await s.shell.begin();
    await s.go('#/b/knx%3Aip%3Aline7/topology');
    assert.deepEqual(navTexts(s.header), [
        ['Group Addresses', '#/b/knx%3Aip%3Aline7/group-addresses', false],
        ['Topology', '#/b/knx%3Aip%3Aline7/topology', true],
        ['Buildings', '#/b/knx%3Aip%3Aline7/buildings', false],
        ['Group Monitor', '#/b/knx%3Aip%3Aline7/monitor', false],
        ['Project', '#/b/knx%3Aip%3Aline7/project', false],
    ]);
    assert.deepEqual(s.header.children.map((part) => part.className || part.tagName), ['brand', 'NAV', 'bridge', 'stream', 'user', 'sign-out', 'theme']);
    await s.go('#/b/knx%3Aip%3Aline7/wizard');
    assert.ok(navTexts(s.header).every(([, , current]) => !current));
    await s.go('#/b/knx%3Aip%3Agone/monitor');
    assert.equal(title(s.main), 'Group Addresses');
    assert.deepEqual(s.location.replaced.at(-1), '#/b/knx%3Aip%3Agone/group-addresses');
    assert.deepEqual(
        navTexts(s.header).map(([name]) => name),
        ['Group Addresses', 'Topology', 'Buildings', 'Project'],
    );
    assert.ok(textNodes(s.header).includes('removed'));
});

test('shell: the addresses of the page before the panels open the matching panel and are replaced by the new ones', async () => {
    const s = setup();
    await s.shell.begin();
    for (const [old, now, name] of [
        ['addresses', 'group-addresses', 'Group Addresses'],
        ['building', 'buildings', 'Buildings'],
        ['setup', 'project', 'Project'],
        ['devices/7.7.1', 'topology?sel=device%3A7.7.1', 'Topology'],
        ['devices', 'topology', 'Topology'],
        ['devices?row=device%3AP-7_D-1', 'topology?row=device%3AP-7_D-1', 'Topology'],
        ['monitor', null, 'Group Monitor'],
    ]) {
        const before = s.location.replaced.length;
        await s.go(`#/b/knx%3Aip%3Aline7/${old}`);
        assert.equal(title(s.main), name, old);
        assert.deepEqual(s.location.replaced.slice(before), now === null ? [] : [`#/b/knx%3Aip%3Aline7/${now}`], old);
        assert.equal(navTexts(s.header).find(([, , current]) => current)[0], name);
    }
});

test('shell: a panel that is shown hears of a new selection and is not made again; another panel or the same address makes it again', async () => {
    const made = [];
    const updates = [];
    const closed = [];
    const panel = (name) => ({
        show(context) {
            made.push([name, context.route.query]);
            return { close: () => closed.push(name), update: (route) => updates.push([name, route.query]) };
        },
    });
    const views = { ...VIEW_MODULES, topology: panel('topology'), 'group-addresses': panel('group-addresses'), monitor: { show: () => (made.push(['monitor', {}]), { close() {} }) } };
    const live = () => ({ state: () => ({ phase: 'live' }), subscribe: () => () => {}, close() {} });
    const s = setup({ views, createStream: live });
    await s.shell.begin();
    await s.go('#/b/knx%3Aip%3Aline7/topology');
    await s.go('#/b/knx%3Aip%3Aline7/topology?sel=line%3A7.7');
    await s.go('#/b/knx%3Aip%3Aline7/topology');
    assert.deepEqual(made, [['topology', {}]]);
    assert.deepEqual(updates, [['topology', { sel: 'line:7.7' }], ['topology', {}]]);
    assert.deepEqual(closed, []);
    await s.go('#/b/knx%3Aip%3Aother/topology');
    assert.deepEqual(closed, ['topology']);
    assert.equal(made.length, 2);
    await s.go('#/b/knx%3Aip%3Aother/group-addresses?sel=ga%3A31%2F7%2F1');
    assert.deepEqual(made.at(-1), ['group-addresses', { sel: 'ga:31/7/1' }]);
    s.location.current = '#/b/knx%3Aip%3Aother/group-addresses?sel=ga%3A31%2F7%2F1';
    s.shell.navigate({ view: 'group-addresses', bridge: OTHER.uid, query: { sel: 'ga:31/7/1' } });
    await settle();
    assert.equal(made.length, 4);
    assert.equal(updates.length, 2);
    await s.go('#/b/knx%3Aip%3Aother/monitor');
    await s.go('#/b/knx%3Aip%3Aother/monitor?x=1');
    assert.deepEqual(made.slice(4), [['monitor', {}], ['monitor', {}]]);
});

function withStatus(options) {
    const s = setup(options);
    return s;
}

test('shell: the status bar shows what a panel says is selected, its rows "12 of 350" and the bridge\'s state', async () => {
    let context = null;
    const views = { ...VIEW_MODULES, buildings: { show: (c) => ((context = c), { close() {} }) } };
    const document = createDocument();
    const status = document.createElement('footer');
    const header = document.createElement('header');
    const alert = document.createElement('div');
    const main = document.createElement('main');
    const location = fakeLocation('#/b/knx%3Aip%3Aline7/buildings');
    const auth = fakeAuth();
    const stream = { current: { phase: 'live' }, state: () => stream.current, listeners: [], subscribe: (listener) => (stream.listeners.push(listener), () => {}), close() {} };
    const shell = createShell({ document, header, alert, main, status, api: { bridges: async () => [PRESENT, OTHER] }, auth, location, views, createStream: () => stream });
    await shell.begin();
    assert.deepEqual(status.children.map((part) => part.className), ['selection', 'count', 'bridge-state']);
    assert.equal(status.querySelector('.bridge-state').textContent, 'Line <7> ONLINE · Live');
    assert.equal(status.querySelector('.count').textContent, '');
    context.status({ selection: '7.7 Hall <line>', shown: 12, total: 350 });
    assert.deepEqual(textNodes(status).slice(0, 2), ['7.7 Hall <line>', '12 of 350']);
    context.status({ selection: 'Devices' });
    assert.deepEqual(textNodes(status).slice(0, 2), ['Devices', 'Line <7> ONLINE · Live']);
    stream.current = { phase: 'connecting' };
    stream.listeners.forEach((listener) => listener());
    assert.equal(status.querySelector('.bridge-state').textContent, 'Line <7> ONLINE · Connecting to the monitor');
    location.current = '#/b/knx%3Aip%3Aline7/project';
    await shell.showHash(location.current);
    assert.equal(status.querySelector('.selection').textContent, '');
    location.current = '#/';
    await shell.showHash(location.current);
    assert.equal(status.children.length, 0);
    await shell.showHash('#/b/knx%3Aip%3Aline7/buildings');
    shell.signedOut({});
    assert.equal(status.children.length, 0);
    assert.equal(withStatus().header.querySelector('footer'), null);
});

test('shell: an unknown route or bridge shows the chooser, which reads the list again', async () => {
    const s = setup();
    await s.shell.begin();
    const before = s.api.calls;
    await s.go('#/b/knx%3Aip%3Aline7/monitor');
    await s.go('#/b/knx%3Aip%3Anowhere/monitor');
    assert.equal(title(s.main), 'KNX bridges');
    assert.equal(s.location.replaced.at(-1), '#/');
    await s.go('#/%E0%A4%A');
    assert.equal(title(s.main), 'KNX bridges');
    assert.ok(s.api.calls > before);
});

test('shell: every panel shows its title, also when its calls fail', async () => {
    const s = setup();
    await s.shell.begin();
    for (const [route, name] of [
        ['group-addresses', 'Group Addresses'],
        ['topology', 'Topology'],
        ['topology?sel=device%3A7.7.1', 'Topology'],
        ['buildings', 'Buildings'],
        ['monitor', 'Group Monitor'],
        ['project', 'Project'],
        ['wizard?device=7.7.1', 'Add to openHAB'],
    ]) {
        await s.go(`#/b/knx%3Aip%3Aline7/${route}`);
        assert.equal(title(s.main), name, route);
        assert.equal(s.main.querySelectorAll('h2').length, 1);
    }
    assert.deepEqual(Object.keys(VIEW_MODULES), ['group-addresses', 'topology', 'buildings', 'monitor', 'project', 'wizard']);
});

test('shell: a view gets exactly the context of the spec, and close() when it is left', async () => {
    const seen = [];
    const closed = [];
    const recorder = (name) => ({
        show(context) {
            seen.push([name, context]);
            return { close: () => closed.push(name) };
        },
    });
    const views = Object.fromEntries(Object.keys(VIEW_MODULES).map((name) => [name, recorder(name)]));
    const s = setup({ views });
    await s.shell.begin();
    await s.go('#/b/knx%3Aip%3Aline7/monitor?dst=31%2F7%2F1');
    const [name, context] = seen[0];
    assert.equal(name, 'monitor');
    assert.deepEqual(Object.keys(context).sort(), [...CONTEXT_KEYS].sort());
    assert.deepEqual(
        CONTEXT_KEYS,
        ['document', 'element', 'api', 'stream', 'rows', 'bridge', 'route', 'navigate', 'replace', 'confirm', 'frame', 'timers', 'clipboard', 'auth', 'fetch', 'base', 'showError', 'status', 'layout', 'tab', 'history', 'jump', 'split', 'keep'],
    );
    assert.equal(context.document, s.document);
    assert.equal(context.bridge, PRESENT);
    assert.deepEqual(context.route, { view: 'monitor', bridge: PRESENT.uid, query: { dst: '31/7/1' } });
    assert.equal(context.api, s.api);
    assert.equal(context.auth, s.auth);
    assert.equal(context.fetch, s.extras.fetch);
    assert.equal(context.base, s.extras.base);
    assert.equal(context.timers, s.extras.timers);
    assert.equal(context.frame, s.extras.frame);
    assert.equal(context.confirm, s.extras.confirm);
    assert.equal(context.clipboard, s.extras.clipboard);
    assert.ok(s.main.contains(context.element));
    assert.equal(typeof context.status, 'function');
    context.navigate({ view: 'project', bridge: PRESENT.uid, query: {} });
    assert.deepEqual(s.location.set, ['#/b/knx%3Aip%3Aline7/project']);
    await s.go('#/b/knx%3Aip%3Aline7/setup');
    assert.deepEqual(closed, ['monitor']);
    assert.equal(s.main.contains(context.element), false);
    s.shell.stop();
    assert.deepEqual(closed, ['monitor', 'project']);
});

test('shell: an error shows under the header by its code, the server text for an unknown code, and goes on the next route', async () => {
    let context = null;
    const views = { ...VIEW_MODULES, monitor: { show: (c) => ((context = c), { close() {} }) } };
    const s = setup({ views });
    await s.shell.begin();
    await s.go('#/b/knx%3Aip%3Aline7/monitor');
    assert.equal(s.alert.getAttribute('role'), 'alert');
    context.showError(new KnxApiError(404, 'unknown-bridge', 'server words'));
    assert.equal(s.alert.textContent, 'There is no such KNX bridge.');
    context.showError(new KnxApiError(400, 'new-code', 'The <b>server</b> words'));
    assert.deepEqual(textNodes(s.alert), ['The <b>server</b> words']);
    context.showError(new KnxApiError(401, 'signed-out'));
    assert.deepEqual(textNodes(s.alert), ['The <b>server</b> words']);
    context.showError(null);
    assert.equal(s.alert.textContent, '');
    context.showError(new KnxApiError(502, 'http-502'));
    await s.go('#/b/knx%3Aip%3Aline7/setup');
    assert.equal(s.alert.textContent, '');
});

test('shell: a view that throws while it is shown leaves its error and the page goes on', async () => {
    const views = { ...VIEW_MODULES, buildings: { show: () => { throw new KnxApiError(0, 'network'); } } };
    const s = setup({ views });
    await s.shell.begin();
    await s.go('#/b/knx%3Aip%3Aline7/building');
    assert.equal(s.alert.textContent, 'openHAB cannot be reached.');
    await s.go('#/b/knx%3Aip%3Aline7/setup');
    assert.equal(title(s.main), 'Project');
});

test('shell: the header shows the user name as text and Sign out, which signs out and closes the view', async () => {
    const closed = [];
    const views = { ...VIEW_MODULES, 'group-addresses': { show: () => ({ close: () => closed.push('monitor') }) } };
    const s = setup({ views, bridges: [PRESENT] });
    await s.shell.begin();
    assert.ok(textNodes(s.header).includes('Signed in as <i>ann</i>'));
    assert.ok(textNodes(s.header).includes('Line <7>'));
    const signOut = s.header.querySelector('.sign-out');
    assert.equal(signOut.textContent, 'Sign out');
    signOut.click();
    await settle();
    assert.deepEqual(s.auth.calls, ['start', 'signOut']);
    assert.deepEqual(closed, ['monitor']);
    assert.deepEqual(s.main.querySelectorAll('button').map((b) => b.textContent), ['Sign in with openHAB']);
    assert.equal(s.header.querySelector('nav'), null);
    assert.ok(!textNodes(s.header).includes('Signed in as <i>ann</i>'));
});

test('shell: a login that ends on its own shows the signed-out page with its text', async () => {
    const s = setup({ bridges: [PRESENT] });
    await s.shell.begin();
    s.auth.lost();
    s.shell.signedOut({ message: 'login.ended' });
    assert.ok(textNodes(s.main).includes('Your login has ended. Sign in again.'));
    await s.go('#/b/knx%3Aip%3Aline7/setup');
    assert.notEqual(title(s.main), 'Project');
});

test('shell: the route kept before the login page is shown after it', async () => {
    const s = setup({ auth: fakeAuth({ signedIn: true, user: { name: 'ann' }, route: '#/b/knx%3Aip%3Aother/building' }) });
    await s.shell.begin();
    assert.equal(s.location.replaced[0], '#/b/knx%3Aip%3Aother/buildings');
    assert.equal(title(s.main), 'Buildings');
    const odd = setup({ auth: fakeAuth({ signedIn: true, user: { name: 'ann' }, route: 'javascript:alert(1)' }) });
    await odd.shell.begin();
    assert.equal(odd.location.replaced[0], '#/');
});

test('shell: rows and stream per bridge, kept until the bridge changes, and no stream for a removed bridge', async () => {
    const made = [];
    const createRows = (bridge) => {
        const rows = { bridge: bridge.uid, close: () => made.push(`rows closed ${bridge.uid}`) };
        made.push(`rows ${bridge.uid}`);
        return rows;
    };
    const createStream = (bridge, rows) => {
        made.push(`stream ${bridge.uid} ${rows.bridge}`);
        return { close: () => made.push(`stream closed ${bridge.uid}`) };
    };
    let context = null;
    const record = { show: (c) => ((context = c), { close() {} }) };
    const views = { ...VIEW_MODULES, monitor: record, 'group-addresses': record, project: record };
    const s = setup({ views, createRows, createStream });
    await s.shell.begin();
    await s.go('#/b/knx%3Aip%3Aline7/monitor');
    const first = context;
    await s.go('#/b/knx%3Aip%3Aline7/setup');
    assert.equal(context.stream, first.stream);
    assert.equal(context.rows, first.rows);
    await s.go('#/');
    await s.go('#/b/knx%3Aip%3Agone/addresses');
    assert.equal(context.stream, null);
    assert.equal(context.rows.bridge, 'knx:ip:gone');
    s.shell.stop();
    assert.deepEqual(made, [
        'rows knx:ip:line7',
        'stream knx:ip:line7 knx:ip:line7',
        'stream closed knx:ip:line7',
        'rows closed knx:ip:line7',
        'rows knx:ip:gone',
        'rows closed knx:ip:gone',
    ]);
    const plain = setup({ views });
    await plain.shell.begin();
    await plain.go('#/b/knx%3Aip%3Aline7/monitor');
    assert.equal(context.stream, null);
    assert.equal(context.rows, null);
});

test('shell: the header shows the state of the bridge\'s stream as polite text and lets go of it with the bridge', async () => {
    const streams = new Map();
    const createStream = (bridge) => {
        const listeners = new Set();
        const stream = {
            current: { phase: 'idle' },
            listeners,
            state: () => stream.current,
            subscribe(listener) {
                listeners.add(listener);
                return () => listeners.delete(listener);
            },
            set(next) {
                stream.current = next;
                for (const listener of listeners) {
                    listener(next);
                }
            },
            close() {},
        };
        streams.set(bridge.uid, stream);
        return stream;
    };
    const record = { show: () => ({ close() {} }) };
    const views = { ...VIEW_MODULES, monitor: record, 'group-addresses': record };
    const s = setup({ views, createRows: () => ({}), createStream });
    await s.shell.begin();
    await s.go('#/b/knx%3Aip%3Aline7/monitor');
    const line = () => s.header.querySelector('.stream');
    assert.equal(line().getAttribute('aria-live'), 'polite');
    assert.equal(line().textContent, '');
    const first = streams.get('knx:ip:line7');
    first.set({ phase: 'live' });
    assert.equal(line().textContent, 'Live');
    first.set({ phase: 'waiting', code: 'too-many-streams', wait: 10000 });
    assert.equal(line().textContent, 'Too many pages are open; waiting for a free place');
    first.set({ phase: 'stopped', code: 'monitor-off' });
    assert.deepEqual(textNodes(line()), ['Stopped: The monitor of this bridge is switched off (buffer size 0)']);
    await s.go('#/b/knx%3Aip%3Aother/monitor');
    assert.equal(first.listeners.size, 0);
    assert.equal(line().textContent, '');
    first.set({ phase: 'live' });
    assert.equal(line().textContent, '');
    streams.get('knx:ip:other').set({ phase: 'connecting' });
    assert.equal(line().textContent, 'Connecting to the monitor');
    await s.go('#/b/knx%3Aip%3Agone/addresses');
    assert.equal(line(), null);
    assert.equal(streams.get('knx:ip:other').listeners.size, 0);
    await s.go('#/b/knx%3Aip%3Aline7/monitor');
    assert.equal(streams.get('knx:ip:line7').listeners.size, 1);
    s.shell.stop();
    assert.equal(streams.get('knx:ip:line7').listeners.size, 0);
});

test('shell: going from view to view leaves no listener behind', async () => {
    const s = setup();
    await s.shell.begin();
    await s.go('#/b/knx%3Aip%3Aline7/monitor');
    const count = s.document.listenerCount();
    for (const route of ['group-addresses', 'topology', 'topology?sel=device%3A7.7.1', 'buildings', 'project', 'monitor']) {
        await s.go(`#/b/knx%3Aip%3Aline7/${route}`);
    }
    await s.go('#/');
    await s.go('#/b/knx%3Aip%3Aline7/monitor');
    assert.equal(s.document.listenerCount(), count);
});

test('shell: index.html is the shell with one main, one module script, one stylesheet and the noscript line', () => {
    const html = readFileSync(new URL('../web/index.html', import.meta.url), 'utf8');
    assert.equal(html.match(/<main\b/g)?.length, 1);
    assert.equal(html.match(/<script\b/g)?.length, 1);
    assert.match(html, /<script type="module" src="js\/main.js"><\/script>/);
    assert.equal(html.match(/<link\b/g)?.length, 1);
    assert.match(html, /<link rel="stylesheet" href="app.css">/);
    assert.match(html, /<noscript>[^<]*This page needs JavaScript\.[^<]*<\/noscript>|<noscript><p>This page needs JavaScript\.<\/p><\/noscript>/);
    assert.match(html, /<header id="header"><\/header>/);
    assert.match(html, /<main id="main"><\/main>\s*<footer id="status"><\/footer>/);
    assert.match(html, /<div id="alert" role="alert"[^>]*><\/div>/);
    assert.match(html, /<title>KNX<\/title>/);
});

// The two sequences of a bridge that goes away and comes back, with the real api, row model, stream and views.
const LIVE = { uid: 'knx:ip:x', label: 'X', present: true, status: 'ONLINE', connection: 'TUNNEL', projectState: 'NONE', bufferSize: 100 };
const LIVE_HASH = '#/b/knx%3Aip%3Ax/monitor';

function liveSetup({ bridges, hash = LIVE_HASH }) {
    const clock = createTimers();
    const document = createDocument();
    const header = document.createElement('header');
    const alert = document.createElement('div');
    const main = document.createElement('main');
    const location = fakeLocation(hash);
    const auth = fakeAuth();
    const s = { bridges, clock, document, header, main, location, auth, answers: [], bodies: [], streams: [], rowModels: 0, mostOpen: 0 };
    const fetch = createFetch((call) => {
        const path = call.url.pathname;
        if (path === '/rest/knx/bridges') {
            return json(200, s.bridges);
        }
        if (path.endsWith('/telegrams/stream')) {
            const open = s.bodies.filter((body) => !body.aborted()).length;
            s.mostOpen = Math.max(s.mostOpen, open + 1);
            const body = createEventBody(call.init.signal);
            s.bodies.push(body);
            s.answers.shift()?.(body);
            return body.response;
        }
        if (path.endsWith('/telegrams')) {
            return json(200, { telegrams: [], lastNumber: 4, monitorId: 'm1', more: false, reset: false });
        }
        if (path.endsWith('/monitor')) {
            return json(200, { received: 0, sent: 0, notDecrypted: {}, notDecryptedOtherReason: 0, plainOnSecuredAddress: 0 });
        }
        return json(404, { code: 'unknown-bridge', text: 'x' });
    });
    const base = new URL('http://openhab.invalid/rest/');
    const api = createApi({ fetch, base, auth });
    const frames = [];
    s.shell = createShell({
        document,
        header,
        alert,
        main,
        api,
        auth,
        location,
        fetch,
        timers: clock.timers,
        frame: (callback) => frames.push(callback),
        confirm: () => false,
        clipboard: undefined,
        createRows: (bridge) => {
            s.rowModels++;
            return createRows({ bridge: bridge.uid, api });
        },
        createStream: (bridge, rows) => {
            const stream = createStream({ bridge: bridge.uid, rows, api, auth, fetch, base, timers: clock.timers, now: clock.now });
            s.streams.push(stream);
            return stream;
        },
    });
    s.calls1 = () => fetch.calls.filter((call) => call.url.pathname === '/rest/knx/bridges').length;
    s.draw = () => {
        while (frames.length > 0) {
            frames.shift()();
        }
    };
    s.go = async (value) => {
        location.current = value;
        await s.shell.showHash(value);
        await settle();
        s.draw();
    };
    s.stopped = () => main.querySelector('.stopped');
    s.destinations = () => (main.querySelector('tbody')?.children ?? []).map((row) => row.querySelector('.destination')?.querySelector('.address')?.textContent);
    return s;
}

const openAndSend = (number, destination) => (body) => {
    body.send('open', { lastNumber: 4, monitorId: 'm1' });
    body.send('telegram', { number, time: '2026-11-15T07:01:02Z', source: '7.7.1', destination, type: 'WRITE', direction: 'IN', value: 'ON', security: 'PLAIN' });
};

test('shell: a bridge that went away comes back by "Try again": call 1 again, new services, one stream at a time', async () => {
    const s = liveSetup({ bridges: [LIVE] });
    s.answers.push(openAndSend(5, '31/7/1'));
    await s.shell.begin();
    await settle();
    s.draw();
    assert.equal(s.header.querySelector('.stream').textContent, 'Live');
    assert.deepEqual(s.destinations(), ['31/7/1']);

    s.bridges = [];
    s.answers.push((body) => body.send('end', { code: 'unknown-bridge', text: 'x' }));
    s.bodies[0].send('end', { code: 'bridge-gone', text: 'x' });
    await settle();
    await s.clock.advance(1000);
    s.draw();
    assert.equal(s.bodies.length, 2);
    assert.deepEqual(s.streams[0].state(), { phase: 'stopped', code: 'unknown-bridge' });
    assert.ok(textNodes(s.stopped()).includes('There is no such KNX bridge.'));
    const button = s.stopped().querySelector('button');
    assert.equal(button?.textContent, 'Try again');

    const before = s.calls1();
    button.click();
    await settle();
    s.draw();
    assert.equal(s.calls1(), before + 1);
    assert.equal(s.streams.length, 1);
    assert.equal(s.rowModels, 1);
    assert.equal(s.bodies.length, 2);
    assert.equal(title(s.main), 'Group Monitor');
    assert.ok(textNodes(s.stopped()).includes('There is no such KNX bridge.'));

    s.bridges = [LIVE];
    s.answers.push(openAndSend(6, '31/7/2'));
    s.stopped().querySelector('button').click();
    await settle();
    s.draw();
    assert.equal(s.calls1(), before + 2);
    assert.equal(s.streams.length, 2);
    assert.equal(s.rowModels, 2);
    assert.deepEqual(s.streams[0].state(), { phase: 'closed' });
    assert.equal(s.streams[0].subscribers(), 0);
    assert.equal(s.bodies.length, 3);
    assert.equal(s.header.querySelector('.stream').textContent, 'Live');
    assert.deepEqual(s.destinations(), ['31/7/2']);
    assert.equal(s.stopped().textContent, '');
    assert.equal(s.mostOpen, 1);
    s.shell.stop();
    assert.ok(s.bodies.every((body) => body.aborted()));
});

test('shell: a bridge absent when the page opened gets its stream when it is back and its Monitor is opened', async () => {
    const s = liveSetup({ bridges: [{ ...LIVE, present: false }] });
    await s.shell.begin();
    await settle();
    s.draw();
    assert.equal(title(s.main), 'Group Addresses');
    assert.equal(s.rowModels, 1);
    assert.equal(s.streams.length, 0);

    const before = s.calls1();
    await s.go(LIVE_HASH);
    assert.equal(s.calls1(), before + 1);
    assert.equal(title(s.main), 'Group Addresses');
    assert.ok(textNodes(s.header).includes('removed'));
    assert.equal(s.rowModels, 1);
    assert.equal(s.streams.length, 0);
    assert.equal(s.bodies.length, 0);

    s.bridges = [LIVE];
    s.answers.push(openAndSend(5, '31/7/3'));
    await s.go(LIVE_HASH);
    assert.equal(s.calls1(), before + 2);
    assert.equal(title(s.main), 'Group Monitor');
    assert.equal(s.rowModels, 2);
    assert.equal(s.streams.length, 1);
    assert.equal(s.bodies.length, 1);
    assert.equal(s.header.querySelector('.stream').textContent, 'Live');
    assert.deepEqual(s.destinations(), ['31/7/3']);
    assert.equal(s.mostOpen, 1);
    s.shell.stop();
});

test('shell: a stream stopped by a lost login does not make the shell read call 1 again; one stopped for another reason does', async () => {
    const streams = [];
    const createStream = () => {
        const stream = { current: { phase: 'live' }, state: () => stream.current, subscribe: () => () => {}, close() {} };
        streams.push(stream);
        return stream;
    };
    const record = { show: () => ({ close() {} }) };
    const views = { ...VIEW_MODULES, monitor: record, 'group-addresses': record, project: record };
    const s = setup({ views, createRows: () => ({ close() {} }), createStream });
    await s.shell.begin();
    await s.go('#/b/knx%3Aip%3Aline7/monitor');
    const before = s.api.calls;
    streams[0].current = { phase: 'stopped', code: 'signed-out' };
    assert.equal(s.auth.signedIn(), true);
    await s.go('#/b/knx%3Aip%3Aline7/setup');
    assert.equal(s.api.calls, before);
    assert.equal(streams.length, 1);

    streams[0].current = { phase: 'stopped', code: 'monitor-off' };
    await s.go('#/b/knx%3Aip%3Aline7/addresses');
    assert.equal(s.api.calls, before + 1);
    assert.equal(streams.length, 2);
});

test('shell: a bridge that is still absent keeps its old rows when another route reads call 1', async () => {
    const made = [];
    const createRows = (bridge) => {
        made.push(`rows ${bridge.uid}`);
        return { close: () => made.push(`rows closed ${bridge.uid}`) };
    };
    const record = { show: () => ({ close() {} }) };
    const views = { ...VIEW_MODULES, 'group-addresses': record };
    const s = setup({ bridges: [REMOVED], views, createRows });
    await s.shell.begin();
    assert.deepEqual(made, ['rows knx:ip:gone']);
    const first = [];
    const grab = { show: (context) => (first.push(context.rows), { close() {} }) };
    views['group-addresses'] = grab;

    await s.go('#/');
    assert.deepEqual(made, ['rows knx:ip:gone']);
    await s.go('#/b/knx%3Aip%3Agone/addresses');
    assert.deepEqual(made, ['rows knx:ip:gone']);
    assert.equal(first.length, 2);
    assert.equal(first[0], first[1]);
    s.shell.stop();
    assert.deepEqual(made, ['rows knx:ip:gone', 'rows closed knx:ip:gone']);
});

test('shell: the tab of the panel that is shown carries aria-current="page", no other does', async () => {
    const s = setup({ hash: '#/b/knx%3Aip%3Aline7/topology' });
    await s.shell.begin();
    const current = () => s.header.querySelector('nav').querySelectorAll('a').filter((a) => a.getAttribute('aria-current') !== null).map((a) => [a.textContent, a.getAttribute('aria-current')]);
    assert.deepEqual(current(), [['Topology', 'page']]);
    await s.go('#/b/knx%3Aip%3Aline7/buildings');
    assert.deepEqual(current(), [['Buildings', 'page']]);
});

const live = () => ({ state: () => ({ phase: 'live' }), subscribe: () => () => {}, close() {} });

function recorder(seen, more = {}) {
    return (name) => ({
        show(context) {
            const view = { name, context, updates: [], focused: 0, close() {}, update: (route) => view.updates.push(route), ...more };
            if (more.focus) {
                view.focus = () => view.focused++;
            }
            seen.push(view);
            return view;
        },
    });
}

test('shell: replace gives the shown panel another address without a history entry and without telling it', async () => {
    const seen = [];
    const views = Object.fromEntries(Object.keys(VIEW_MODULES).map((name) => [name, recorder(seen)(name)]));
    const s = setup({ views, createStream: live, hash: '#/b/knx%3Aip%3Aline7/topology?sel=line%3A31.1' });
    await s.shell.begin();
    const [view] = seen;
    const route = { view: 'topology', bridge: PRESENT.uid, query: { sel: 'line:31.1', row: 'device:d-1' } };
    view.context.replace(route);
    assert.equal(s.location.current, '#/b/knx%3Aip%3Aline7/topology?sel=line%3A31.1&row=device%3Ad-1');
    assert.deepEqual(s.location.replaced.at(-1), s.location.current);
    assert.deepEqual(s.location.set, []);
    assert.deepEqual(view.updates, []);
    assert.equal(seen.length, 1);
    // a reload, or the same address from a link, reaches the panel with the row
    await s.go(s.location.current);
    assert.equal(seen.length, 1);
    assert.deepEqual(view.updates.at(-1).query, { sel: 'line:31.1', row: 'device:d-1' });
    const replaced = s.location.replaced.length;
    view.context.replace(route);
    assert.equal(s.location.replaced.length, replaced, 'the address already says so');
    view.context.replace({ view: 'buildings', bridge: PRESENT.uid, query: { row: 'x' } });
    view.context.replace({ view: 'topology', bridge: OTHER.uid, query: { row: 'x' } });
    assert.equal(s.location.replaced.length, replaced, 'only the panel that is shown has an address to change');
});

test('shell: a panel left with the focus in it hands the focus to the panel that comes; a focus elsewhere is left alone', async () => {
    const seen = [];
    const views = Object.fromEntries(Object.keys(VIEW_MODULES).map((name) => [name, recorder(seen, { focus: true })(name)]));
    const s = setup({ views, createStream: live, hash: '#/b/knx%3Aip%3Aline7/buildings' });
    await s.shell.begin();
    assert.equal(seen[0].focused, 0);
    const row = s.document.createElement('button');
    seen[0].context.element.appendChild(row);
    row.focus();
    await s.go('#/b/knx%3Aip%3Aline7/topology?sel=device%3Ad-1');
    assert.equal(seen[1].name, 'topology');
    assert.equal(seen[1].focused, 1);
    s.header.querySelector('nav').querySelectorAll('a')[0].focus();
    await s.go('#/b/knx%3Aip%3Aline7/group-addresses');
    assert.equal(seen[2].focused, 0);
});

function themeButtons(header) {
    return header.querySelector('.theme').querySelectorAll('button');
}

function pressed(header) {
    return themeButtons(header).map((button) => [button.textContent, button.getAttribute('aria-pressed')]);
}

test('theme: the top bar offers Auto, Light and Dark; Auto leaves the root to the system', async () => {
    const root = createDocument().createElement('html');
    const s = setup({ root, layout: createLayout(null) });
    assert.deepEqual([...THEME_CHOICES], ['auto', 'light', 'dark']);
    assert.equal(s.header.querySelector('.theme').getAttribute('role'), 'group');
    assert.equal(s.header.querySelector('.theme').getAttribute('aria-label'), 'Theme');
    assert.deepEqual(pressed(s.header), [['Auto', 'true'], ['Light', 'false'], ['Dark', 'false']]);
    assert.equal(root.hasAttribute('data-theme'), false);
    await s.shell.begin();
    await s.go('#/b/knx%3Aip%3Aline7/project');
    assert.deepEqual(pressed(s.header), [['Auto', 'true'], ['Light', 'false'], ['Dark', 'false']], 'also in the bar of a bridge');
    assert.equal(s.header.lastChild, s.header.querySelector('.theme'), 'at the right end');
});

test('theme: a choice names the theme on the root and is kept for the next visit; Auto takes the name away', () => {
    const kept = { text: null };
    const store = { read: () => kept.text, write: (value) => (kept.text = value) };
    const root = createDocument().createElement('html');
    const s = setup({ root, layout: createLayout(store) });
    themeButtons(s.header)[2].click();
    assert.equal(root.getAttribute('data-theme'), 'dark');
    assert.deepEqual(pressed(s.header).map(([, on]) => on), ['false', 'false', 'true']);
    assert.deepEqual(JSON.parse(kept.text), { theme: 'dark' });

    const again = createDocument().createElement('html');
    const next = setup({ root: again, layout: createLayout(store) });
    assert.equal(again.getAttribute('data-theme'), 'dark');
    assert.deepEqual(pressed(next.header).map(([, on]) => on), ['false', 'false', 'true']);

    themeButtons(next.header)[1].click();
    assert.equal(again.getAttribute('data-theme'), 'light');
    themeButtons(next.header)[0].click();
    assert.equal(again.hasAttribute('data-theme'), false);
    assert.deepEqual(JSON.parse(kept.text), { theme: 'auto' });
});

test('theme: without a store, with one that refuses, or with a kept value that is no theme, the page follows the system', () => {
    const refusing = {
        read() {
            throw new Error('blocked');
        },
        write() {
            throw new Error('blocked');
        },
    };
    for (const layout of [undefined, createLayout(null), createLayout(refusing), createLayout({ read: () => '{"theme":"pink"}', write() {} })]) {
        const root = createDocument().createElement('html');
        const s = setup({ root, layout });
        assert.equal(root.hasAttribute('data-theme'), false);
        assert.deepEqual(pressed(s.header).map(([, on]) => on), ['true', 'false', 'false']);
        themeButtons(s.header)[1].click();
        assert.equal(root.getAttribute('data-theme'), 'light', 'for this visit');
    }
});

const B = '#/b/knx%3Aip%3Aline7';

/** Views that write down what happens to them and show their name in their face of the shared sidebar. */
function splitViews(fixed = []) {
    const log = [];
    const contexts = {};
    const make = (name) => ({
        show(context) {
            const key = `${context.split.half}:${name}`;
            contexts[context.split.half] = context;
            log.push(['show', key, context.route.query]);
            const face = context.split.sidebar?.face() ?? null;
            const draw = (query) => face?.properties.show([name, query.sel].filter(Boolean).join(' '), [{ rows: [['Name', name]] }]);
            draw(context.route.query);
            return {
                ...(fixed.includes(name)
                    ? {}
                    : {
                          update(route) {
                              log.push(['update', key, route.query]);
                              draw(route.query);
                          },
                      }),
                focus() {
                    log.push(['focus', key]);
                },
                close() {
                    log.push(['close', key]);
                    if (face) {
                        context.split.sidebar.release(face);
                    }
                },
            };
        },
    });
    return { log, contexts, views: Object.fromEntries(Object.keys(VIEW_MODULES).map((name) => [name, make(name)])) };
}

// without a live stream the shell asks for the bridges again on every route and makes the panels anew
const liveStream = () => ({ state: () => ({ phase: 'live' }), subscribe: () => () => {}, close() {} });

async function openSplit({ fixed, ...more } = {}) {
    const recorded = splitViews(fixed);
    const s = setup({ views: recorded.views, bridges: [PRESENT], hash: `${B}/buildings?sel=space%3AS-1&p2=group-addresses&p2-sel=ga%3A31%2F0%2F1`, createStream: liveStream, ...more });
    await s.shell.begin();
    await settle();
    /** Follows what a view asked the address to become, as the browser's hashchange does. */
    const follow = () => s.go(s.location.current);
    return { ...s, ...recorded, follow };
}

test('shell: an address with a second panel shows two panels, one above the other, with a border between and one sidebar beside both', async () => {
    const s = await openSplit();
    assert.deepEqual(s.log, [
        ['show', 'first:buildings', { sel: 'space:S-1' }],
        ['show', 'second:group-addresses', { sel: 'ga:31/0/1' }],
    ]);
    const outer = s.main.children[0];
    assert.equal(s.main.children.length, 1);
    assert.deepEqual(outer.children.map((child) => child.className), ['halves', 'splitter properties-splitter', 'pane sidebar']);
    const halves = outer.children[0];
    assert.deepEqual(halves.children.map((child) => child.className), ['view active', 'splitter row-splitter', 'view second']);
    assert.equal(halves.children[1].getAttribute('aria-label'), 'Height of the second panel');
    assert.equal(halves.style.getPropertyValue('--second-height'), '320px');
    assert.equal(outer.style.getPropertyValue('--properties-width'), '300px');
    assert.equal(s.contexts.first.element, halves.children[0]);
    assert.equal(s.contexts.second.element, halves.children[2]);
    assert.deepEqual(s.contexts.first.route, { view: 'buildings', bridge: PRESENT.uid, query: { sel: 'space:S-1' } });
    assert.deepEqual(s.contexts.second.route, { view: 'group-addresses', bridge: PRESENT.uid, query: { sel: 'ga:31/0/1' } });
    assert.deepEqual([s.contexts.first.split.half, s.contexts.first.split.open, s.contexts.second.split.half, s.contexts.second.split.open], ['first', true, 'second', true]);
    assert.deepEqual(Object.keys(s.contexts.second).sort(), [...CONTEXT_KEYS].sort());
    // each panel can ask what the other one shows, and gets a copy
    assert.deepEqual(s.contexts.first.split.other(), { view: 'group-addresses', query: { sel: 'ga:31/0/1' } });
    assert.deepEqual(s.contexts.second.split.other(), { view: 'buildings', query: { sel: 'space:S-1' } });
    s.contexts.first.split.other().query.sel = 'changed';
    assert.equal(s.contexts.second.route.query.sel, 'ga:31/0/1');
    // the tabs of the top strip go to another first panel and keep the second
    assert.deepEqual(navTexts(s.header).slice(0, 2).map((link) => link[1]), [`${B}/group-addresses?p2=group-addresses&p2-sel=ga%3A31%2F0%2F1`, `${B}/topology?p2=group-addresses&p2-sel=ga%3A31%2F0%2F1`]);
    assert.equal(navTexts(s.header).at(-1)[1], `${B}/project`);
    // the border can be dragged, and its place is kept
    halves.children[1].fire('pointerdown', { clientY: 400, pointerId: 1 });
    halves.children[1].fire('pointermove', { clientY: 300 });
    assert.equal(halves.style.getPropertyValue('--second-height'), '420px');
    s.shell.stop();
    assert.deepEqual(s.log.slice(2), [['close', 'first:buildings'], ['close', 'second:group-addresses']]);
    assert.equal(s.document.listenerCount(), s.header.ownListenerCount() + s.main.ownListenerCount() + s.header.querySelector('.sign-out').ownListenerCount() + s.header.querySelector('.theme').ownListenerCount(), 'only the frame\'s own listeners are left');
});

test('shell: each half goes its own way: a new selection in one tells only that panel, and both selections stay in the address', async () => {
    const s = await openSplit();
    s.log.length = 0;
    s.contexts.second.navigate({ view: 'group-addresses', bridge: PRESENT.uid, query: { sel: 'ga:31/0/2' } });
    assert.equal(s.location.set.at(-1), `${B}/buildings?sel=space%3AS-1&p2=group-addresses&p2-sel=ga%3A31%2F0%2F2`);
    await s.follow();
    assert.deepEqual(s.log, [['update', 'second:group-addresses', { sel: 'ga:31/0/2' }]]);
    s.log.length = 0;
    s.contexts.first.navigate({ view: 'buildings', bridge: PRESENT.uid, query: { sel: 'space:S-2' } });
    assert.equal(s.location.set.at(-1), `${B}/buildings?sel=space%3AS-2&p2=group-addresses&p2-sel=ga%3A31%2F0%2F2`);
    await s.follow();
    assert.deepEqual(s.log, [['update', 'first:buildings', { sel: 'space:S-2' }]]);
    // a selected row of either list is written into the address without a history entry
    const set = s.location.set.length;
    s.contexts.second.replace({ view: 'group-addresses', bridge: PRESENT.uid, query: { sel: 'ga:31/0/2', row: 'r1' } });
    assert.equal(s.location.replaced.at(-1), `${B}/buildings?sel=space%3AS-2&p2=group-addresses&p2-sel=ga%3A31%2F0%2F2&p2-row=r1`);
    s.contexts.first.replace({ view: 'buildings', bridge: PRESENT.uid, query: { sel: 'space:S-2', row: 'r0' } });
    assert.equal(s.location.replaced.at(-1), `${B}/buildings?sel=space%3AS-2&row=r0&p2=group-addresses&p2-sel=ga%3A31%2F0%2F2&p2-row=r1`);
    s.contexts.second.replace({ view: 'topology', bridge: PRESENT.uid, query: {} });
    assert.equal(s.location.replaced.at(-1).includes('p2-row=r1'), true, 'a panel that is not shown changes nothing');
    assert.equal(s.location.set.length, set);
    // another panel in the lower half: only that half is made again
    s.log.length = 0;
    s.contexts.second.navigate({ view: 'topology', bridge: PRESENT.uid, query: {} });
    await s.follow();
    assert.deepEqual(s.log, [['close', 'second:group-addresses'], ['show', 'second:topology', {}]]);
    assert.deepEqual(s.main.children[0].children[0].children.map((child) => child.className), ['view', 'splitter row-splitter', 'view second active']);
    // another panel in the upper half keeps the lower one in the address
    s.contexts.first.navigate({ view: 'buildings', bridge: PRESENT.uid, query: {} });
    assert.equal(s.location.set.at(-1), `${B}/buildings?p2=topology`);
    // the wizard and Project take the whole page
    s.contexts.second.navigate({ view: 'wizard', bridge: PRESENT.uid, query: { device: 'D-1' } });
    assert.equal(s.location.set.at(-1), `${B}/wizard?device=D-1`);
});

test('shell: an upper panel that cannot be told of a route stays as it is when only the lower half moves', async () => {
    const s = await openSplit({ fixed: ['monitor'], hash: `${B}/monitor?p2=group-addresses&p2-sel=ga%3A31%2F0%2F1` });
    const halves = s.main.children[0].children[0];
    const subject = () => s.main.children[0].querySelector('.sidebar').querySelector('.subject').textContent;
    s.log.length = 0;
    s.contexts.second.navigate({ view: 'group-addresses', bridge: PRESENT.uid, query: { sel: 'ga:31/0/2' } });
    await s.follow();
    assert.deepEqual(s.log, [['update', 'second:group-addresses', { sel: 'ga:31/0/2' }]]);
    assert.deepEqual(halves.children.map((child) => child.className), ['view', 'splitter row-splitter', 'view second active']);
    assert.equal(subject(), 'group-addresses ga:31/0/2');
    // its own route moved: it has to be made again, and the other with it
    s.log.length = 0;
    s.go(`${B}/monitor?filter=x&p2=group-addresses&p2-sel=ga%3A31%2F0%2F2`);
    await settle();
    assert.deepEqual(s.log.map((entry) => entry.slice(0, 2)), [['close', 'first:monitor'], ['close', 'second:group-addresses'], ['show', 'first:monitor'], ['show', 'second:group-addresses']]);
});

test('shell: an upper panel that could not be made is made again when the lower half moves', async () => {
    const recorded = splitViews();
    let fail = true;
    const made = recorded.views.buildings.show;
    recorded.views.buildings.show = (context) => {
        if (fail) {
            throw new Error('made-up failure');
        }
        return made(context);
    };
    const s = setup({ views: recorded.views, bridges: [PRESENT], hash: `${B}/buildings?sel=space%3AS-1&p2=group-addresses&p2-sel=ga%3A31%2F0%2F1`, createStream: liveStream });
    await s.shell.begin();
    await settle();
    assert.deepEqual(recorded.log.map((entry) => entry.slice(0, 2)), [['show', 'second:group-addresses']]);
    fail = false;
    recorded.log.length = 0;
    recorded.contexts.second.navigate({ view: 'group-addresses', bridge: PRESENT.uid, query: { sel: 'ga:31/0/2' } });
    s.go(s.location.current);
    await settle();
    assert.deepEqual(recorded.log.map((entry) => entry.slice(0, 2)), [['close', 'second:group-addresses'], ['show', 'first:buildings'], ['show', 'second:group-addresses']]);
    assert.deepEqual(recorded.contexts.first.route.query, { sel: 'space:S-1' });
});

test('shell: the border of the second panel is told the height the lower half really has', async () => {
    const s = await openSplit({ layout: createLayout({ read: () => JSON.stringify({ second: 1500 }), write() {} }) });
    const halves = s.main.children[0].children[0];
    const border = halves.children[1];
    assert.equal(halves.style.getPropertyValue('--second-height'), '1500px');
    halves.children[2].clientHeight = 400;
    border.fire('keydown', { key: 'ArrowDown' });
    assert.equal(halves.style.getPropertyValue('--second-height'), '384px');
});

test('shell: jump shows a route in the other half; with one panel it shows it in this one', async () => {
    const s = await openSplit();
    s.log.length = 0;
    // from Buildings above: the address below, where Group Addresses already is
    s.contexts.first.jump({ view: 'group-addresses', bridge: PRESENT.uid, query: { sel: 'ga:31/0/9' } });
    assert.equal(s.location.set.at(-1), `${B}/buildings?sel=space%3AS-1&p2=group-addresses&p2-sel=ga%3A31%2F0%2F9`);
    await s.follow();
    assert.deepEqual(s.log, [['update', 'second:group-addresses', { sel: 'ga:31/0/9' }]]);
    // from Group Addresses below: the device above, in the tree of Buildings, which can show it itself
    s.contexts.second.jump({ view: 'topology', bridge: PRESENT.uid, query: { sel: 'device:D-1' } }, ['topology', 'buildings']);
    assert.equal(s.location.set.at(-1), `${B}/buildings?sel=device%3AD-1&p2=group-addresses&p2-sel=ga%3A31%2F0%2F9`);
    await s.follow();
    // without that, the other half becomes the panel of the route
    s.contexts.second.jump({ view: 'topology', bridge: PRESENT.uid, query: { sel: 'device:D-1' } });
    assert.equal(s.location.set.at(-1), `${B}/topology?sel=device%3AD-1&p2=group-addresses&p2-sel=ga%3A31%2F0%2F9`);
    // what the other half already shows is not made again
    const set = s.location.set.length;
    s.log.length = 0;
    s.contexts.second.jump({ view: 'buildings', bridge: PRESENT.uid, query: { sel: 'device:D-1' } });
    assert.equal(s.location.set.length, set);
    assert.deepEqual(s.log, []);
    assert.equal(s.contexts.first.element.classList.contains('active'), true);

    const single = setup({ views: s.views, bridges: [PRESENT] });
    await single.shell.begin();
    await single.go(`${B}/buildings?sel=space%3AS-1`);
    assert.equal(s.contexts.first.split.open, false);
    s.contexts.first.jump({ view: 'group-addresses', bridge: PRESENT.uid, query: { sel: 'ga:31/0/9' } }, ['topology']);
    assert.equal(single.location.set.at(-1), `${B}/group-addresses?sel=ga%3A31%2F0%2F9`);
});

test('shell: the sidebar shows the half that was used last: a click or the keyboard in it, or a selection made in it from the other', async () => {
    const s = await openSplit();
    const subject = () => s.main.querySelector('.sidebar').querySelector('.subject').textContent;
    const [top, , bottom] = s.main.children[0].children[0].children;
    assert.equal(subject(), 'buildings space:S-1');
    assert.equal(s.main.querySelectorAll('.sidebar').length, 1);
    assert.equal(s.main.querySelectorAll('.properties').length, 1);
    bottom.fire('pointerdown');
    assert.equal(subject(), 'group-addresses ga:31/0/1');
    assert.deepEqual([top.classList.contains('active'), bottom.classList.contains('active')], [false, true]);
    top.fire('focusin');
    assert.equal(subject(), 'buildings space:S-1');
    // a jump from above selects below: the sidebar shows what was selected last
    s.contexts.first.jump({ view: 'group-addresses', bridge: PRESENT.uid, query: { sel: 'ga:31/0/9' } });
    await s.follow();
    assert.equal(subject(), 'group-addresses ga:31/0/9');
    s.contexts.first.navigate({ view: 'buildings', bridge: PRESENT.uid, query: { sel: 'space:S-3' } });
    await s.follow();
    assert.equal(subject(), 'buildings space:S-3');
});

test('shell: the status bar shows what the half that was used last says about itself', async () => {
    const recorded = splitViews();
    const document = createDocument();
    const header = document.createElement('header');
    const main = document.createElement('main');
    const statusBar = document.createElement('footer');
    const location = fakeLocation();
    const auth = fakeAuth();
    const shell = createShell({ document, header, alert: document.createElement('div'), main, status: statusBar, api: { bridges: async () => [PRESENT] }, auth, location, views: recorded.views });
    await shell.begin();
    location.current = `${B}/buildings?p2=group-addresses`;
    await shell.showHash(location.current);
    await settle();
    recorded.contexts.first.status({ selection: 'Kitchen', shown: 2, total: 5 });
    recorded.contexts.second.status({ selection: '31/0/1 Light', shown: 1, total: 1 });
    assert.deepEqual(textNodes(statusBar).slice(0, 2), ['Kitchen', '2 of 5']);
    main.querySelector('.second').fire('pointerdown');
    assert.deepEqual(textNodes(statusBar).slice(0, 2), ['31/0/1 Light', '1 of 1']);
    recorded.contexts.first.status({ selection: 'Hall', shown: 1, total: 5 });
    assert.deepEqual(textNodes(statusBar).slice(0, 2), ['31/0/1 Light', '1 of 1']);
    main.querySelector('.view').querySelector('.view').fire('pointerdown');
    assert.deepEqual(textNodes(statusBar).slice(0, 2), ['Hall', '1 of 5']);
});

test('shell: the Panels menu opens the second panel with Group Addresses (Buildings under Group Addresses) and closes it again', async () => {
    const recorded = splitViews();
    const s = setup({ views: recorded.views, bridges: [PRESENT], hash: `${B}/buildings?sel=space%3AS-1` });
    await s.shell.begin();
    await settle();
    assert.equal(s.main.children[0].className, 'view');
    assert.deepEqual([recorded.contexts.first.split.open, recorded.contexts.first.split.sidebar], [false, null]);
    recorded.contexts.first.split.toggle();
    assert.equal(s.location.set.at(-1), `${B}/buildings?sel=space%3AS-1&p2=group-addresses`);
    await s.go(s.location.current);
    assert.equal(s.main.children[0].className, 'view split');
    assert.deepEqual(recorded.log.map((entry) => entry.slice(0, 2)), [
        ['show', 'first:buildings'],
        ['close', 'first:buildings'],
        ['show', 'first:buildings'],
        ['show', 'second:group-addresses'],
    ]);
    recorded.contexts.second.split.toggle();
    assert.equal(s.location.set.at(-1), `${B}/buildings?sel=space%3AS-1`);
    await s.go(s.location.current);
    assert.equal(s.main.children[0].className, 'view');
    await s.go(`${B}/group-addresses`);
    recorded.contexts.first.split.toggle();
    assert.equal(s.location.set.at(-1), `${B}/group-addresses?p2=buildings`);
    // Project is no panel of a half: nothing to open
    await s.go(`${B}/project`);
    assert.equal(recorded.contexts.first.split.toggle, null);
});

test('shell: a narrow page has no second panel: the address keeps it, the first panel is shown alone and offers none', async () => {
    let wide = false;
    const s = await openSplit({ wide: () => wide });
    assert.deepEqual(s.log, [['show', 'first:buildings', { sel: 'space:S-1' }]]);
    assert.equal(s.main.children[0].className, 'view');
    assert.equal(s.main.querySelector('.halves'), null);
    assert.equal(s.contexts.first.split.toggle, null);
    assert.equal(s.contexts.first.split.open, false);
    assert.equal(s.location.current, `${B}/buildings?sel=space%3AS-1&p2=group-addresses&p2-sel=ga%3A31%2F0%2F1`);
    s.contexts.first.navigate({ view: 'buildings', bridge: PRESENT.uid, query: { sel: 'space:S-2' } });
    assert.equal(s.location.set.at(-1), `${B}/buildings?sel=space%3AS-2&p2=group-addresses&p2-sel=ga%3A31%2F0%2F1`);
    // the page grew: shown again, it has both
    wide = true;
    await s.shell.showHash(s.location.current, true);
    await settle();
    assert.equal(s.main.children[0].className, 'view split');
});

test('shell: the real panels stand in both halves: two titles, two bars, one sidebar; "Close second panel" leaves the upper one', async () => {
    const s = setup({ bridges: [PRESENT] });
    await s.shell.begin();
    await s.go(`${B}/buildings?p2=group-addresses`);
    assert.deepEqual(s.main.querySelectorAll('h2').map((heading) => heading.textContent), ['Buildings', 'Group Addresses']);
    assert.equal(s.main.querySelectorAll('.panel-bar').length, 2);
    assert.equal(s.main.querySelectorAll('.sidebar').length, 1);
    assert.equal(s.main.querySelectorAll('.tree-pane').length, 2);
    assert.deepEqual(s.main.querySelectorAll('.panel-chooser').map((chooser) => chooser.value), ['buildings', 'group-addresses']);
    const items = s.main.querySelectorAll('.second-panel');
    assert.deepEqual(items.map((item) => item.textContent), ['Close second panel', 'Close second panel']);
    items[1].click();
    assert.equal(s.location.set.at(-1), `${B}/buildings`);
    await s.go(s.location.current);
    assert.deepEqual(s.main.querySelectorAll('h2').map((heading) => heading.textContent), ['Buildings']);
    assert.equal(s.main.querySelector('.second-panel').textContent, 'Open second panel');
});

test('shell: a link inside two panels goes on in its own half and keeps the other; a link of the sidebar belongs to the half it shows', async () => {
    const s = await openSplit();
    const link = (to) => {
        const a = s.document.createElement('a');
        a.setAttribute('href', to);
        return a;
    };
    const [top, , bottom] = s.main.children[0].children[0].children;
    const inBottom = bottom.appendChild(link(`${B}/group-addresses?sel=ga%3A31%2F0%2F4`));
    const event = inBottom.fire('click');
    assert.equal(event.defaultPrevented, true);
    assert.equal(s.location.set.at(-1), `${B}/buildings?sel=space%3AS-1&p2=group-addresses&p2-sel=ga%3A31%2F0%2F4`);
    await s.follow();
    const inSide = s.main.querySelector('.sidebar').appendChild(link(`${B}/topology?sel=device%3AD-1`));
    inSide.fire('click');
    assert.equal(s.location.set.at(-1), `${B}/buildings?sel=space%3AS-1&p2=topology&p2-sel=device%3AD-1`, 'the lower half was used last');
    top.fire('pointerdown');
    inSide.fire('click');
    assert.equal(s.location.set.at(-1), `${B}/topology?sel=device%3AD-1&p2=group-addresses&p2-sel=ga%3A31%2F0%2F4`);
    // the wizard takes the whole page; a click meant for a new tab is left to the browser
    top.appendChild(link(`${B}/wizard?device=D-1`)).fire('click');
    assert.equal(s.location.set.at(-1), `${B}/wizard?device=D-1`);
    const set = s.location.set.length;
    assert.equal(inBottom.fire('click', { ctrlKey: true }).defaultPrevented, false);
    assert.equal(inBottom.fire('click', { button: 1 }).defaultPrevented, false);
    assert.equal(s.location.set.length, set);
});

test('shell: every view of a visit gets the same store to keep things in, so the wizard finds its plan again', async () => {
    const seen = [];
    const recorder = (name) => ({
        show(context) {
            seen.push([name, context.keep]);
            return { close() {} };
        },
    });
    const views = Object.fromEntries(Object.keys(VIEW_MODULES).map((name) => [name, recorder(name)]));
    const s = setup({ views });
    await s.shell.begin();
    await s.go('#/b/knx%3Aip%3Aline7/wizard');
    seen.at(-1)[1].set('wizard:made-up', 'a plan');
    await s.go('#/b/knx%3Aip%3Aline7/monitor');
    await s.go('#/b/knx%3Aip%3Aline7/wizard');
    assert.deepEqual(seen.slice(-3).map(([name]) => name), ['wizard', 'monitor', 'wizard']);
    assert.ok(seen.at(-1)[1] instanceof Map);
    assert.equal(seen.at(-1)[1].get('wizard:made-up'), 'a plan');
    // signed out: what was kept is not there for whoever signs in next on this page
    s.shell.signedOut({});
    assert.equal(seen.at(-1)[1].size, 0);
});
