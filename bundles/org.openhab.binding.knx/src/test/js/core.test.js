import { test } from 'node:test';
import assert from 'node:assert/strict';

import { CoreError, createCore } from '../web/js/core.js';
import { createFetch, empty, json, networkFailure } from './fakes.js';

const BASE = new URL('http://openhab.invalid/oh/rest/');
const THING = 'knx:device:my-bridge:Made_up';
const CHANNEL = `${THING}:Light_1`;

function fakeAuth(tokens = ['t1', 't2', 't3']) {
    const auth = {
        renewals: 0,
        lostCount: 0,
        current: tokens[0],
        async token() {
            return auth.current;
        },
        async renew() {
            auth.renewals++;
            auth.current = tokens[auth.renewals];
            return auth.current;
        },
        lost() {
            auth.lostCount++;
        },
    };
    return auth;
}

function setup(handler = () => empty(404)) {
    const fetch = createFetch(handler);
    const auth = fakeAuth();
    return { fetch, auth, core: createCore({ fetch, base: BASE, auth }) };
}

test('core: method and encoded address of every call, by kind', async () => {
    const thing = { kind: 'THING', uid: THING };
    const item = { kind: 'ITEM', itemName: 'Made_up_light' };
    const group = { kind: 'GROUP', itemName: 'gHall' };
    const link = { kind: 'LINK', itemName: 'Made_up_light', channelUID: CHANNEL };
    const encodedThing = 'knx%3Adevice%3Amy-bridge%3AMade_up';
    const cases = [
        [(core) => core.get('THING', thing), 'GET', `/oh/rest/things/${encodedThing}`],
        [(core) => core.create('THING', thing, { UID: THING }), 'POST', '/oh/rest/things'],
        [(core) => core.remove('THING', thing), 'DELETE', `/oh/rest/things/${encodedThing}`],
        [(core) => core.get('ITEM', item), 'GET', '/oh/rest/items/Made_up_light'],
        [(core) => core.create('ITEM', item, { name: 'Made_up_light' }), 'PUT', '/oh/rest/items/Made_up_light'],
        [(core) => core.remove('ITEM', item), 'DELETE', '/oh/rest/items/Made_up_light'],
        [(core) => core.get('GROUP', group), 'GET', '/oh/rest/items/gHall'],
        [(core) => core.create('GROUP', group, { name: 'gHall' }), 'PUT', '/oh/rest/items/gHall'],
        [(core) => core.remove('GROUP', group), 'DELETE', '/oh/rest/items/gHall'],
        [(core) => core.get('LINK', link), 'GET', `/oh/rest/links/Made_up_light/${encodedThing}%3ALight_1`],
        [(core) => core.create('LINK', link, {}), 'PUT', `/oh/rest/links/Made_up_light/${encodedThing}%3ALight_1`],
        [(core) => core.remove('LINK', link), 'DELETE', `/oh/rest/links/Made_up_light/${encodedThing}%3ALight_1`],
    ];
    for (const [run, method, path] of cases) {
        const { fetch, core } = setup(() => empty(200));
        assert.equal(await run(core), 200, path);
        assert.equal(fetch.calls.length, 1, path);
        const [call] = fetch.calls;
        assert.equal(call.method, method, path);
        assert.equal(call.url.origin, 'http://openhab.invalid', path);
        assert.equal(call.url.pathname, path);
        assert.equal(call.url.search, '', path);
        assert.equal(call.headers['X-OPENHAB-TOKEN'], 't1', path);
        assert.equal(call.headers.Accept, 'application/json', path);
        assert.equal(call.headers['Content-Type'], 'application/json', path);
        assert.equal(call.headers.Authorization, undefined, path);
        assert.equal(call.body === undefined, method === 'GET' || method === 'DELETE', path);
    }
});

test('core: the body goes out as the JSON of the object it was given', async () => {
    const { fetch, core } = setup(() => empty(201));
    const body = { UID: THING, label: '<i>x</i> "q" \\', configuration: {}, channels: [{ id: 'Light_1', defaultTags: [] }] };
    assert.equal(await core.create('THING', { uid: THING }, body), 201);
    assert.deepEqual(JSON.parse(fetch.calls[0].body), body);
    for (const bad of [undefined, null, 'text', 7, ['x']]) {
        await assert.rejects(core.create('THING', { uid: THING }, bad), (error) => error instanceof CoreError && error.code === 'bad-body');
    }
    assert.equal(fetch.calls.length, 1);
});

test('core: the status is handed back as it is, the body of the answer is never read', async () => {
    for (const status of [200, 201, 202, 204, 400, 404, 405, 409, 500, 503]) {
        let read = false;
        const { core } = setup(() => {
            const response = status === 204 ? empty(204) : json(status, { path: '../../x', method: 'DELETE' });
            const original = response.json.bind(response);
            response.json = () => {
                read = true;
                return original();
            };
            return response;
        });
        assert.equal(await core.get('ITEM', { itemName: 'Made_up' }), status);
        assert.equal(read, false, String(status));
    }
});

test('core: an id outside its pattern or a kind outside the four makes no call', async () => {
    const { fetch, core } = setup();
    const bad = [
        ['THING', { uid: 'knx:device:b' }],
        ['THING', { uid: 'knx:ip:b:x' }],
        ['THING', { uid: 'knx:device:b:x:y' }],
        ['THING', { uid: 'knx:device:b:x/y' }],
        ['THING', { uid: 'knx:device:b:..' }],
        ['THING', { uid: 'knx:device:b:x y' }],
        ['THING', { uid: 'knx:device:b:é' }],
        ['THING', { uid: 'knx:device::x' }],
        ['THING', { uid: ' knx:device:b:x' }],
        ['THING', { uid: 'knx:device:b:x\n' }],
        ['THING', {}],
        ['THING', { uid: 7 }],
        ['ITEM', { itemName: '1x' }],
        ['ITEM', { itemName: 'x-y' }],
        ['ITEM', { itemName: 'x.y' }],
        ['ITEM', { itemName: 'x/y' }],
        ['ITEM', { itemName: '..' }],
        ['ITEM', { itemName: '' }],
        ['ITEM', { itemName: 'a?b' }],
        ['GROUP', { itemName: 'g#x' }],
        ['GROUP', {}],
        ['LINK', { itemName: 'Made_up', channelUID: THING }],
        ['LINK', { itemName: 'Made_up', channelUID: `${THING}:a:b` }],
        ['LINK', { itemName: 'Made_up', channelUID: `${THING}:a/b` }],
        ['LINK', { itemName: 'x-y', channelUID: CHANNEL }],
        ['LINK', { itemName: 'Made_up' }],
        ['BRIDGE', { uid: THING }],
        ['thing', { uid: THING }],
        [undefined, { uid: THING }],
    ];
    for (const [kind, ids] of bad) {
        for (const run of [() => core.get(kind, ids), () => core.create(kind, ids, {}), () => core.remove(kind, ids)]) {
            await assert.rejects(run(), (error) => error instanceof CoreError && error.code === 'bad-id' && error.status === 0, `${kind} ${JSON.stringify(ids)}`);
        }
    }
    assert.equal(fetch.calls.length, 0);
});

test('core: a 401 renews the token once and says so, without sending the call again', async () => {
    const { fetch, auth, core } = setup((call) => (call.headers['X-OPENHAB-TOKEN'] === 't1' ? empty(401) : empty(404)));
    await assert.rejects(core.get('ITEM', { itemName: 'Made_up' }), (error) => error.code === 'renewed' && error.status === 401);
    assert.equal(auth.renewals, 1);
    assert.equal(fetch.calls.length, 1);
    assert.equal(await core.get('ITEM', { itemName: 'Made_up' }), 404);
    assert.equal(fetch.calls[1].headers['X-OPENHAB-TOKEN'], 't2');
    assert.equal(auth.lostCount, 0);
});

test('core: a 401 where no renewal is allowed ends the login', async () => {
    const { fetch, auth, core } = setup(() => empty(401));
    await assert.rejects(core.remove('ITEM', { itemName: 'Made_up' }, { renew: false }), (error) => error.code === 'signed-out' && error.status === 401);
    assert.equal(auth.renewals, 0);
    assert.equal(auth.lostCount, 1);
    assert.equal(fetch.calls.length, 1);
});

test('core: a refused renewal or no token gives signed-out, a network failure network', async () => {
    const refused = setup(() => empty(401));
    refused.auth.renew = async () => {
        const error = new Error('signed-out');
        error.code = 'signed-out';
        throw error;
    };
    await assert.rejects(refused.core.get('THING', { uid: THING }), (error) => error.code === 'signed-out');
    const none = setup();
    none.auth.token = async () => {
        throw new Error('no login');
    };
    await assert.rejects(none.core.get('THING', { uid: THING }), (error) => error.code === 'signed-out');
    assert.equal(none.fetch.calls.length, 0);
    const offline = setup(() => {
        throw networkFailure();
    });
    await assert.rejects(offline.core.create('THING', { uid: THING }, {}), (error) => error.code === 'network' && error.status === 0);
});
