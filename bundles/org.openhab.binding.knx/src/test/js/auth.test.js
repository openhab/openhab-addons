import { test } from 'node:test';
import assert from 'node:assert/strict';

import { createApi } from '../web/js/api.js';
import { USE_MAINUI_LOGIN, createAuth } from '../web/js/auth.js';
import { challenge } from '../web/js/pkce.js';
import { createFetch, createReadOnlyStorage, createStorage, createTimers, empty, json, networkFailure } from './fakes.js';

const PAGE = 'http://openhab.invalid/oh/knx/';
const ORIGIN = 'http://openhab.invalid';
const BASE = new URL('../rest/', PAGE);
const TOKEN_PATH = '/oh/rest/auth/token';
const LOGOUT_PATH = '/oh/rest/auth/logout';
const MAINUI_KEY = 'openhab.ui:refreshToken';
const MAINUI_TOKEN = 'mainui-refresh-in-local-storage';

const LOGIN_KEPT = { 'knx.loginState': 's-1', 'knx.loginVerifier': 'v'.repeat(43), 'knx.loginRoute': '#/b/knx%3Aip%3Ax/devices' };

function answer({ access = 'a1', refresh = 'r1', expires = 3600, roles = ['administrator'], name = 'ann' } = {}) {
    return json(200, {
        access_token: access,
        refresh_token: refresh,
        expires_in: expires,
        scope: 'admin',
        token_type: 'bearer',
        user: { name, roles },
    });
}

function randomBytes() {
    let next = 1;
    const asked = [];
    const give = (length) => {
        asked.push(length);
        return Uint8Array.from({ length }, () => next++ & 255);
    };
    give.asked = asked;
    return give;
}

function setup({ address = PAGE, session = {}, local = {}, handler = () => empty(599), useMainUi } = {}) {
    const store = createStorage(session);
    const readOnly = createReadOnlyStorage(local);
    const clock = createTimers();
    const fetch = createFetch(handler);
    const addresses = [];
    const signedOut = [];
    const random = randomBytes();
    const deps = {
        fetch,
        base: BASE,
        address,
        session: store,
        local: readOnly,
        timers: clock.timers,
        now: clock.now,
        randomBytes: random,
        replaceAddress: (url) => addresses.push({ url, callsBefore: fetch.calls.length }),
        onSignedOut: (key) => signedOut.push(key),
    };
    if (useMainUi !== undefined) {
        deps.useMainUi = useMainUi;
    }
    return { auth: createAuth(deps), fetch, session: store, local: readOnly, clock, addresses, signedOut, random, deps };
}

function form(call) {
    return Object.fromEntries(new URLSearchParams(call.body));
}

/** The rules every test ends with. */
function checkRules({ fetch, session }) {
    for (const key of session.touched()) {
        assert.ok(key.startsWith('knx.'), `storage key ${key}`);
    }
    for (const call of fetch.calls) {
        assert.equal('credentials' in call.init, false);
        assert.equal(call.url.search, '', 'nothing in the address of a call');
        if (call.url.pathname === TOKEN_PATH) {
            assert.equal(call.headers.Authorization, undefined);
            assert.equal(call.headers['X-OPENHAB-TOKEN'], undefined);
            assert.equal(call.headers['Content-Type'], 'application/x-www-form-urlencoded');
        }
    }
}

test('auth: with nothing kept the page is signed out and calls nothing', async () => {
    const s = setup();
    assert.deepEqual(await s.auth.start(), { signedIn: false });
    assert.equal(s.fetch.calls.length, 0);
    assert.equal(s.auth.signedIn(), false);
    assert.deepEqual(s.addresses, []);
    await assert.rejects(s.auth.token(), (error) => error.code === 'signed-out');
    checkRules(s);
});

test('auth: the ways in order: a login answer before the own refresh token before MainUI', async () => {
    const all = { ...LOGIN_KEPT, 'knx.refreshToken': 'own-r' };
    const first = setup({ address: `${PAGE}?code=c-1&state=s-1`, session: all, local: { [MAINUI_KEY]: MAINUI_TOKEN }, handler: () => answer() });
    assert.equal((await first.auth.start()).signedIn, true);
    assert.deepEqual(first.fetch.calls.map((call) => form(call).grant_type), ['authorization_code']);
    checkRules(first);

    const second = setup({ session: { 'knx.refreshToken': 'own-r' }, local: { [MAINUI_KEY]: MAINUI_TOKEN }, handler: () => answer() });
    assert.equal((await second.auth.start()).signedIn, true);
    assert.deepEqual(second.fetch.calls.map((call) => form(call).refresh_token), ['own-r']);
    assert.deepEqual(second.local.reads, []);
    checkRules(second);

    const third = setup({ local: { [MAINUI_KEY]: MAINUI_TOKEN }, handler: () => answer() });
    assert.equal((await third.auth.start()).signedIn, true);
    assert.deepEqual(third.fetch.calls.map((call) => form(call).refresh_token), [MAINUI_TOKEN]);
    checkRules(third);
});

test('auth: way 1 takes the code out of the address before the trade, deletes the three keys and keeps the refresh token', async () => {
    const s = setup({ address: `${PAGE}?code=c-123&state=s-1&keep=1`, session: LOGIN_KEPT, handler: () => answer() });
    const outcome = await s.auth.start();
    assert.deepEqual(outcome, { signedIn: true, user: { name: 'ann' }, route: '#/b/knx%3Aip%3Ax/devices' });
    assert.deepEqual(s.addresses, [{ url: `${PAGE}?keep=1`, callsBefore: 0 }]);
    assert.equal(s.fetch.calls.length, 1);
    const [call] = s.fetch.calls;
    assert.equal(call.method, 'POST');
    assert.equal(call.url.href, `${ORIGIN}${TOKEN_PATH}`);
    assert.deepEqual(form(call), {
        grant_type: 'authorization_code',
        code: 'c-123',
        client_id: PAGE,
        redirect_uri: PAGE,
        code_verifier: 'v'.repeat(43),
    });
    assert.deepEqual([...s.session.values.keys()], ['knx.refreshToken']);
    assert.equal(s.session.getItem('knx.refreshToken'), 'r1');
    assert.deepEqual(s.session.removed.sort(), ['knx.loginRoute', 'knx.loginState', 'knx.loginVerifier']);
    assert.equal(await s.auth.token(), 'a1');
    assert.equal(s.auth.signedIn(), true);
    assert.deepEqual(s.auth.user(), { name: 'ann' });
    checkRules(s);
});

test('auth: the address without the answer is the page address when nothing else was in it', async () => {
    const s = setup({ address: `${PAGE}?code=c-123&state=s-1`, session: LOGIN_KEPT, handler: () => answer() });
    await s.auth.start();
    assert.deepEqual(s.addresses.map((a) => a.url), [PAGE]);
    checkRules(s);
});

test('auth: a state that does not match trades nothing, deletes the keys and says so', async () => {
    for (const [address, session] of [
        [`${PAGE}?code=c-1&state=other`, LOGIN_KEPT],
        [`${PAGE}?code=c-1&state=s-1`, {}],
        [`${PAGE}?code=c-1&state=s-1`, { 'knx.loginState': 's-1' }],
        [`${PAGE}?code=c-1`, LOGIN_KEPT],
        [`${PAGE}?state=s-1`, LOGIN_KEPT],
        [`${PAGE}?code=&state=s-1`, LOGIN_KEPT],
        [`${PAGE}?code=c-1&state=`, { ...LOGIN_KEPT, 'knx.loginState': '' }],
    ]) {
        const s = setup({ address, session: { ...session, 'knx.refreshToken': 'own-r' }, local: { [MAINUI_KEY]: MAINUI_TOKEN }, handler: () => answer() });
        assert.deepEqual(await s.auth.start(), { signedIn: false, message: 'login.foreign' }, address);
        assert.equal(s.fetch.calls.length, 0, address);
        assert.deepEqual(s.addresses.map((a) => a.url), [PAGE], address);
        assert.deepEqual([...s.session.values.keys()], ['knx.refreshToken'], address);
        checkRules(s);
    }
});

test('auth: an error answer of the login is taken out of the address and the page is signed out', async () => {
    const s = setup({
        address: `${PAGE}?error=access_denied&error_description=x&state=s-1`,
        session: { ...LOGIN_KEPT, 'knx.refreshToken': 'own-r' },
        handler: () => answer(),
    });
    assert.deepEqual(await s.auth.start(), { signedIn: false });
    assert.equal(s.fetch.calls.length, 0);
    assert.deepEqual(s.addresses.map((a) => a.url), [PAGE]);
    assert.equal(s.session.getItem('knx.loginState'), null);
    checkRules(s);
});

test('auth: a code trade that fails ends signed out with a text and keeps nothing', async () => {
    for (const [handler, message] of [
        [() => json(400, { error: 'invalid_grant' }), 'login.failed'],
        [() => empty(401), 'login.failed'],
        [() => json(200, { nothing: true }), 'login.failed'],
        [
            () => {
                throw networkFailure();
            },
            'login.unreachable',
        ],
    ]) {
        const s = setup({ address: `${PAGE}?code=c-1&state=s-1`, session: LOGIN_KEPT, handler });
        assert.deepEqual(await s.auth.start(), { signedIn: false, message });
        assert.deepEqual([...s.session.values.keys()], []);
        assert.equal(s.clock.count(), 0);
        checkRules(s);
    }
});

test('auth: a refused own refresh token (400 or 401) is deleted and MainUI is tried next', async () => {
    for (const status of [400, 401]) {
        const s = setup({
            session: { 'knx.refreshToken': 'own-r' },
            local: { [MAINUI_KEY]: MAINUI_TOKEN },
            handler: (call) => (form(call).refresh_token === 'own-r' ? json(status, { error: 'invalid_grant' }) : answer({ refresh: MAINUI_TOKEN })),
        });
        assert.deepEqual(await s.auth.start(), { signedIn: true, user: { name: 'ann' } });
        assert.deepEqual(s.session.removed, ['knx.refreshToken']);
        assert.deepEqual(s.fetch.calls.map((call) => form(call).refresh_token), ['own-r', MAINUI_TOKEN]);
        checkRules(s);
    }
    const alone = setup({ session: { 'knx.refreshToken': 'own-r' }, handler: () => empty(400) });
    assert.deepEqual(await alone.auth.start(), { signedIn: false });
    checkRules(alone);
});

test('auth: a network failure on the own refresh token deletes nothing and offers to try again', async () => {
    for (const handler of [
        () => {
            throw networkFailure();
        },
        () => empty(503),
        () => new Response('<html>proxy</html>', { status: 200 }),
    ]) {
        const s = setup({ session: { 'knx.refreshToken': 'own-r' }, local: { [MAINUI_KEY]: MAINUI_TOKEN }, handler });
        assert.deepEqual(await s.auth.start(), { signedIn: false, message: 'login.unreachable', retry: true });
        assert.deepEqual(s.session.touched(), []);
        assert.equal(s.fetch.calls.length, 1);
        assert.deepEqual(s.local.reads, []);
        checkRules(s);
    }
});

test("auth: MainUI's key is read and never written, and its trade uses the origin as MainUI does", async () => {
    const s = setup({ local: { [MAINUI_KEY]: MAINUI_TOKEN }, handler: () => answer({ refresh: MAINUI_TOKEN }) });
    assert.equal((await s.auth.start()).signedIn, true);
    assert.deepEqual(form(s.fetch.calls[0]), {
        grant_type: 'refresh_token',
        refresh_token: MAINUI_TOKEN,
        client_id: ORIGIN,
        redirect_uri: ORIGIN,
    });
    assert.ok(s.local.reads.every((key) => key === MAINUI_KEY));
    assert.deepEqual(s.session.touched(), []);
    await s.clock.advance(3540 * 1000);
    assert.deepEqual(form(s.fetch.calls[1]).refresh_token, MAINUI_TOKEN);
    assert.deepEqual(s.session.touched(), []);
    checkRules(s);
});

test("auth: MainUI's refresh token from a way 3 answer is in nothing the module gives out and in no storage", async () => {
    const fromAnswer = 'mainui-refresh-from-the-answer';
    const s = setup({ local: { [MAINUI_KEY]: MAINUI_TOKEN }, handler: () => answer({ refresh: fromAnswer }) });
    const outcome = await s.auth.start();
    await s.auth.renew();
    const seen = JSON.stringify([outcome, s.auth.user(), Object.entries(s.auth), await s.auth.token(), s.signedOut]);
    for (const token of [fromAnswer, MAINUI_TOKEN]) {
        assert.doesNotMatch(seen, new RegExp(token));
        assert.ok(![...s.session.values.values()].includes(token));
    }
    assert.ok(Object.values(s.auth).every((value) => typeof value === 'function'));
    checkRules(s);
});

test('auth: sign out after MainUI calls no logout, sets knx.noMainUi, and the same tab then stays signed out', async () => {
    const s = setup({ local: { [MAINUI_KEY]: MAINUI_TOKEN }, handler: () => answer() });
    await s.auth.start();
    await s.auth.signOut();
    assert.equal(s.fetch.calls.length, 1);
    assert.equal(s.fetch.calls.filter((call) => call.url.pathname === LOGOUT_PATH).length, 0);
    assert.equal(s.session.getItem('knx.noMainUi'), '1');
    assert.equal(s.auth.signedIn(), false);
    assert.equal(s.clock.count(), 0);
    await assert.rejects(s.auth.token(), (error) => error.code === 'signed-out');

    const reload = createAuth({ ...s.deps });
    assert.deepEqual(await reload.start(), { signedIn: false });
    assert.equal(s.fetch.calls.length, 1);
    checkRules(s);
});

test('auth: USE_MAINUI_LOGIN is on, and off it skips way 3', async () => {
    assert.equal(USE_MAINUI_LOGIN, true);
    const s = setup({ local: { [MAINUI_KEY]: MAINUI_TOKEN }, handler: () => answer(), useMainUi: false });
    assert.deepEqual(await s.auth.start(), { signedIn: false });
    assert.equal(s.fetch.calls.length, 0);
    assert.deepEqual(s.local.reads, []);
    checkRules(s);
});

test("auth: MainUI's login refused, unreachable or unreadable goes on silently to signed out", async () => {
    for (const handler of [
        () => json(400, { error: 'invalid_grant' }),
        () => empty(500),
        () => {
            throw networkFailure();
        },
    ]) {
        const s = setup({ local: { [MAINUI_KEY]: MAINUI_TOKEN }, handler });
        assert.deepEqual(await s.auth.start(), { signedIn: false });
        assert.deepEqual(s.session.touched(), []);
        checkRules(s);
    }
    const blocked = setup({ handler: () => answer() });
    blocked.deps.local = {
        getItem() {
            throw new Error('SecurityError');
        },
    };
    assert.deepEqual(await createAuth(blocked.deps).start(), { signedIn: false });
    assert.equal(blocked.fetch.calls.length, 0);
});

test('auth: a token answer without the administrator role', async () => {
    const user = { roles: ['user'] };
    const own = setup({
        address: `${PAGE}?code=c-1&state=s-1`,
        session: LOGIN_KEPT,
        handler: (call) => (call.url.pathname === LOGOUT_PATH ? empty(200) : answer(user)),
    });
    assert.deepEqual(await own.auth.start(), { signedIn: false, message: 'login.needsAdmin' });
    const logout = own.fetch.calls.find((call) => call.url.pathname === LOGOUT_PATH);
    assert.equal(logout.headers['X-OPENHAB-TOKEN'], 'a1');
    assert.deepEqual(form(logout), { refresh_token: 'r1' });
    assert.equal(own.session.getItem('knx.refreshToken'), null);
    await assert.rejects(own.auth.token());
    assert.equal(own.clock.count(), 0);
    checkRules(own);

    const kept = setup({ session: { 'knx.refreshToken': 'own-r' }, handler: (call) => (call.url.pathname === LOGOUT_PATH ? empty(200) : answer(user)) });
    assert.deepEqual(await kept.auth.start(), { signedIn: false, message: 'login.needsAdmin' });
    assert.deepEqual(form(kept.fetch.calls[1]), { refresh_token: 'own-r' });
    assert.equal(kept.session.getItem('knx.refreshToken'), null);
    checkRules(kept);

    const mainUi = setup({ local: { [MAINUI_KEY]: MAINUI_TOKEN }, handler: () => answer({ roles: [] }) });
    assert.deepEqual(await mainUi.auth.start(), { signedIn: false, message: 'login.needsAdmin' });
    assert.equal(mainUi.fetch.calls.length, 1);
    checkRules(mainUi);
});

test('auth: the renewal timer runs 60 seconds before the token runs out', async () => {
    let n = 0;
    const s = setup({ session: { 'knx.refreshToken': 'own-r' }, handler: () => answer({ access: `a${++n}` }) });
    await s.auth.start();
    assert.deepEqual(s.clock.delays(), [3540 * 1000]);
    await s.clock.advance(3540 * 1000 - 1);
    assert.equal(s.fetch.calls.length, 1);
    await s.clock.advance(1);
    assert.equal(s.fetch.calls.length, 2);
    assert.deepEqual(form(s.fetch.calls[1]), { grant_type: 'refresh_token', refresh_token: 'own-r', client_id: PAGE, redirect_uri: PAGE });
    assert.equal(await s.auth.token(), 'a2');
    assert.deepEqual(s.clock.delays(), [3540 * 1000]);
    assert.deepEqual(s.signedOut, []);
    checkRules(s);
});

test('auth: a short token is renewed at half its time, not in a loop', async () => {
    const s = setup({ session: { 'knx.refreshToken': 'own-r' }, handler: () => answer({ expires: 30 }) });
    await s.auth.start();
    assert.deepEqual(s.clock.delays(), [15 * 1000]);
    checkRules(s);
});

test('auth: a refused renewal ends in the signed-out state', async () => {
    let n = 0;
    const s = setup({ session: { 'knx.refreshToken': 'own-r' }, handler: () => (++n === 1 ? answer() : json(400, { error: 'invalid_grant' })) });
    await s.auth.start();
    await s.clock.advance(3540 * 1000);
    assert.deepEqual(s.signedOut, ['login.ended']);
    assert.equal(s.session.getItem('knx.refreshToken'), null);
    assert.equal(s.auth.signedIn(), false);
    assert.equal(s.clock.count(), 0);
    await assert.rejects(s.auth.token(), (error) => error.code === 'signed-out');
    checkRules(s);
});

test('auth: an unreachable renewal keeps the token and tries again after 30 seconds', async () => {
    let n = 0;
    const s = setup({
        session: { 'knx.refreshToken': 'own-r' },
        handler: () => {
            n++;
            if (n === 2) {
                throw networkFailure();
            }
            return answer({ access: `a${n}` });
        },
    });
    await s.auth.start();
    await s.clock.advance(3540 * 1000);
    assert.deepEqual(s.clock.delays(), [30 * 1000]);
    assert.deepEqual(s.signedOut, []);
    assert.equal(s.session.getItem('knx.refreshToken'), 'own-r');
    await s.clock.advance(30 * 1000);
    assert.equal(n, 3);
    assert.equal(await s.auth.token(), 'a3');
    checkRules(s);
});

test('auth: token() renews first when the time has passed without the timer', async () => {
    let n = 0;
    const s = setup({ session: { 'knx.refreshToken': 'own-r' }, handler: () => answer({ access: `a${++n}` }) });
    await s.auth.start();
    s.clock.jump(3600 * 1000);
    assert.equal(await s.auth.token(), 'a2');
    assert.equal(s.fetch.calls.length, 2);
    checkRules(s);
});

test('auth: two renewals at the same time make one call', async () => {
    let n = 0;
    const s = setup({ session: { 'knx.refreshToken': 'own-r' }, handler: () => answer({ access: `a${++n}` }) });
    await s.auth.start();
    const [one, two] = await Promise.all([s.auth.renew(), s.auth.renew()]);
    assert.equal(one, 'a2');
    assert.equal(two, 'a2');
    assert.equal(s.fetch.calls.length, 2);
    checkRules(s);
});

test('auth: 401 gives one renewal and the call again; a second 401 ends in the signed-out state', async () => {
    let n = 0;
    const handler = (call) => {
        if (call.url.pathname === TOKEN_PATH) {
            return answer({ access: `a${++n}` });
        }
        return call.headers['X-OPENHAB-TOKEN'] === 'a1' ? empty(401) : json(200, [{ uid: 'knx:ip:x' }]);
    };
    const s = setup({ session: { 'knx.refreshToken': 'own-r' }, handler });
    await s.auth.start();
    const api = createApi({ fetch: s.fetch, base: BASE, auth: s.auth });
    assert.deepEqual(await api.bridges(), [{ uid: 'knx:ip:x' }]);
    assert.deepEqual(
        s.fetch.calls.map((call) => [call.url.pathname, call.headers['X-OPENHAB-TOKEN'] ?? '-']),
        [
            [TOKEN_PATH, '-'],
            ['/oh/rest/knx/bridges', 'a1'],
            [TOKEN_PATH, '-'],
            ['/oh/rest/knx/bridges', 'a2'],
        ],
    );
    assert.deepEqual(s.signedOut, []);
    checkRules(s);

    const always = setup({
        session: { 'knx.refreshToken': 'own-r' },
        handler: (call) => (call.url.pathname === TOKEN_PATH ? answer() : empty(401)),
    });
    await always.auth.start();
    const api2 = createApi({ fetch: always.fetch, base: BASE, auth: always.auth });
    await assert.rejects(api2.bridges(), (error) => error.code === 'signed-out');
    assert.equal(always.fetch.calls.filter((call) => call.url.pathname === TOKEN_PATH).length, 2);
    assert.deepEqual(always.signedOut, ['login.ended']);
    assert.equal(always.auth.signedIn(), false);
    assert.equal(always.clock.count(), 0);
    checkRules(always);
});

test('auth: sign out of the own session calls logout with the token header and the form, and deletes the key', async () => {
    for (const logoutAnswer of [
        () => empty(200),
        () => empty(500),
        () => {
            throw networkFailure();
        },
    ]) {
        const s = setup({
            session: { 'knx.refreshToken': 'own-r' },
            handler: (call) => (call.url.pathname === LOGOUT_PATH ? logoutAnswer() : answer()),
        });
        await s.auth.start();
        await s.auth.signOut();
        const logout = s.fetch.calls[1];
        assert.equal(logout.url.href, `${ORIGIN}${LOGOUT_PATH}`);
        assert.equal(logout.method, 'POST');
        assert.equal(logout.headers['X-OPENHAB-TOKEN'], 'a1');
        assert.equal(logout.headers['Content-Type'], 'application/x-www-form-urlencoded');
        assert.deepEqual(form(logout), { refresh_token: 'own-r' });
        assert.equal(s.session.getItem('knx.refreshToken'), null);
        assert.equal(s.session.getItem('knx.noMainUi'), '1');
        assert.equal(s.auth.signedIn(), false);
        assert.equal(s.clock.count(), 0);
        assert.deepEqual(s.signedOut, []);
        checkRules(s);
    }
});

test('auth: sign out with a token that has run out renews it first', async () => {
    let n = 0;
    const s = setup({
        session: { 'knx.refreshToken': 'own-r' },
        handler: (call) => (call.url.pathname === LOGOUT_PATH ? empty(200) : answer({ access: `a${++n}` })),
    });
    await s.auth.start();
    s.clock.jump(3600 * 1000);
    await s.auth.signOut();
    assert.deepEqual(
        s.fetch.calls.map((call) => call.url.pathname),
        [TOKEN_PATH, TOKEN_PATH, LOGOUT_PATH],
    );
    assert.equal(s.fetch.calls[2].headers['X-OPENHAB-TOKEN'], 'a2');
    assert.deepEqual(s.signedOut, []);
    checkRules(s);
});

test("auth: sign in keeps a verifier, a state and the route, and goes to openHAB's login with a challenge", async () => {
    const s = setup({ address: `${PAGE}#/b/knx%3Aip%3Ax/monitor` });
    const login = new URL(await s.auth.signIn('#/b/knx%3Aip%3Ax/monitor'));
    assert.deepEqual(s.random.asked.sort(), [16, 32]);
    const verifier = s.session.getItem('knx.loginVerifier');
    const state = s.session.getItem('knx.loginState');
    assert.match(verifier, /^[A-Za-z0-9_-]{43}$/);
    assert.match(state, /^[A-Za-z0-9_-]{22}$/);
    assert.equal(s.session.getItem('knx.loginRoute'), '#/b/knx%3Aip%3Ax/monitor');
    assert.equal(login.origin + login.pathname, `${ORIGIN}/oh/auth`);
    assert.equal(login.hash, '');
    assert.deepEqual(Object.fromEntries(login.searchParams), {
        response_type: 'code',
        client_id: PAGE,
        redirect_uri: PAGE,
        scope: 'admin',
        state,
        code_challenge: await challenge(verifier),
        code_challenge_method: 'S256',
    });
    assert.doesNotMatch(login.href, new RegExp(verifier));
    assert.equal(s.fetch.calls.length, 0);
    checkRules(s);

    const back = setup({ address: `${PAGE}?code=c-9&state=${encodeURIComponent(state)}`, session: Object.fromEntries(s.session.values), handler: () => answer() });
    const outcome = await back.auth.start();
    assert.equal(outcome.route, '#/b/knx%3Aip%3Ax/monitor');
    assert.equal(form(back.fetch.calls[0]).code_verifier, verifier);
    checkRules(back);
});

test('auth: sign in uses crypto.subtle when it is given', async () => {
    const used = [];
    const s = setup();
    s.deps.subtle = {
        async digest(algorithm, data) {
            used.push(algorithm);
            const { webcrypto } = await import('node:crypto');
            return webcrypto.subtle.digest(algorithm, data);
        },
    };
    const login = new URL(await createAuth(s.deps).signIn('#/'));
    assert.deepEqual(used, ['SHA-256']);
    assert.equal(login.searchParams.get('code_challenge'), await challenge(s.session.getItem('knx.loginVerifier')));
});
