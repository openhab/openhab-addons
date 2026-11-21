import { test } from 'node:test';
import assert from 'node:assert/strict';

import { KnxApiError, PAGE_MAX, createApi } from '../web/js/api.js';
import { createFetch, empty, json, networkFailure } from './fakes.js';

const BASE = new URL('http://openhab.invalid/oh/rest/');
const B = 'knx:ip:example';
const EB = 'knx%3Aip%3Aexample';
const ROOT = `/oh/rest/knx/bridges/${EB}`;

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

function setup(handler = () => json(200, {})) {
    const fetch = createFetch(handler);
    const auth = fakeAuth();
    return { fetch, auth, api: createApi({ fetch, base: BASE, auth }) };
}

function query(call) {
    return Object.fromEntries(call.url.searchParams);
}

test('api: header and address of every method', async () => {
    const file = new File([Uint8Array.of(1, 2, 3)], 'house.knxproj');
    const keyring = new File([Uint8Array.of(4)], 'house.knxkeys');
    const cases = [
        [(api) => api.bridges(), 'GET', '/oh/rest/knx/bridges', {}],
        [(api) => api.bridge(B), 'GET', ROOT, {}],
        [(api) => api.monitor(B), 'GET', `${ROOT}/monitor`, {}],
        [
            (api) =>
                api.telegrams(B, {
                    src: '7.7.1',
                    dst: '31/7/1',
                    type: 'WRITE',
                    dir: 'IN',
                    dpt: '1.001',
                    from: '2026-01-01T00:00:00Z',
                    to: '2026-01-02T00:00:00Z',
                    context: '2',
                    limit: 2000,
                    after: 17,
                    monitor: 'm-1',
                    other: 'dropped',
                    empty: '',
                }),
            'GET',
            `${ROOT}/telegrams`,
            {
                src: '7.7.1',
                dst: '31/7/1',
                type: 'WRITE',
                dir: 'IN',
                dpt: '1.001',
                from: '2026-01-01T00:00:00Z',
                to: '2026-01-02T00:00:00Z',
                context: '2',
                limit: '2000',
                after: '17',
                monitor: 'm-1',
            },
        ],
        [(api) => api.telegrams(B), 'GET', `${ROOT}/telegrams`, {}],
        [(api) => api.clearTelegrams(B), 'DELETE', `${ROOT}/telegrams`, {}],
        [(api) => api.lastValues(B), 'GET', `${ROOT}/last-values`, {}],
        [(api) => api.keyring(B), 'GET', `${ROOT}/keyring`, {}],
        [(api) => api.project(B), 'GET', `${ROOT}/project`, {}],
        [
            (api) => api.groupAddresses(B, { offset: 10, limit: 5 }),
            'GET',
            `${ROOT}/project/group-addresses`,
            { offset: '10', limit: '5' },
        ],
        [(api) => api.groupAddress(B, '31/7/1'), 'GET', `${ROOT}/project/group-address`, { address: '31/7/1' }],
        [(api) => api.devices(B, { offset: 0, limit: 5 }), 'GET', `${ROOT}/project/devices`, { offset: '0', limit: '5' }],
        [(api) => api.device(B, '7.7.1'), 'GET', `${ROOT}/project/devices/7.7.1`, {}],
        [(api) => api.device(B, 'a/b?c'), 'GET', `${ROOT}/project/devices/a%2Fb%3Fc`, {}],
        [(api) => api.deviceChannels(B, '7.7.1'), 'GET', `${ROOT}/project/devices/7.7.1/channels`, {}],
        [(api) => api.channels(B, ['31/7/1', '31/7/2']), 'POST', `${ROOT}/project/channels`, {}],
        [(api) => api.topology(B), 'GET', `${ROOT}/project/topology`, {}],
        [(api) => api.locations(B), 'GET', `${ROOT}/project/locations`, {}],
        [(api) => api.check(B), 'GET', `${ROOT}/project/check`, {}],
        [(api) => api.uploadProject(B, file), 'PUT', `${ROOT}/project`, { name: 'house.knxproj' }],
        [(api) => api.removeProject(B), 'DELETE', `${ROOT}/project`, {}],
        [(api) => api.uploadKeyring(B, keyring), 'PUT', `${ROOT}/keyring`, {}],
        [(api) => api.proposal(B, { groupAddresses: ['31/7/1'] }), 'POST', `${ROOT}/project/proposal`, {}],
        [(api) => api.preview(B, { things: [] }), 'POST', `${ROOT}/project/proposal/preview`, {}],
        [(api) => api.groupTelegram(B, { address: '31/7/1', action: 'read' }), 'POST', `${ROOT}/group-telegram`, {}],
    ];
    for (const [run, method, path, expected] of cases) {
        const { fetch, api } = setup((call) => (call.method === 'DELETE' ? empty(204) : json(200, { ok: true })));
        await run(api);
        assert.equal(fetch.calls.length, 1, path);
        const [call] = fetch.calls;
        assert.equal(call.method, method, path);
        assert.equal(call.url.origin, 'http://openhab.invalid', path);
        assert.equal(call.url.pathname, path);
        assert.deepEqual(query(call), expected, path);
        assert.equal(call.headers['X-OPENHAB-TOKEN'], 't1', path);
        assert.equal(call.headers.Accept, 'application/json', path);
        assert.equal(call.headers.Authorization, undefined, path);
        assert.deepEqual(Object.keys(call.init).sort(), call.body === undefined ? ['headers', 'method'] : ['body', 'headers', 'method'], path);
    }
});

test('api: the bodies of the call for channels and of the uploads', async () => {
    const { fetch, api } = setup();
    await api.channels(B, ['31/7/1']);
    assert.equal(fetch.calls[0].headers['Content-Type'], 'application/json');
    assert.deepEqual(JSON.parse(fetch.calls[0].body), { groupAddresses: ['31/7/1'] });
    const file = new File([Uint8Array.of(1)], 'x.knxproj');
    await api.uploadProject(B, file);
    assert.equal(fetch.calls[1].body, file);
    assert.equal(fetch.calls[1].headers['Content-Type'], 'application/octet-stream');
});

test('api: the wizard calls send their request as JSON, unchanged', async () => {
    const answer = { things: [{ id: 'KNX', label: '<i>x</i>' }], rooms: [], unknown: [], readAtStart: true };
    const { fetch, api } = setup(() => json(200, answer));
    const selection = { groupAddresses: ['31/7/1'], functions: ['F-1'], spaces: [], devices: ['7.7.1'], readAtStart: false };
    assert.deepEqual(await api.proposal(B, selection), answer);
    const edited = { things: [{ id: 'KNX', label: 'Made-up', channels: [] }], roomGroups: [{ groupName: 'gHall', label: 'Hall' }], readAtStart: true };
    await api.preview(B, edited);
    for (const [call, body] of [
        [fetch.calls[0], selection],
        [fetch.calls[1], edited],
    ]) {
        assert.equal(call.method, 'POST');
        assert.equal(call.headers['Content-Type'], 'application/json');
        assert.equal(call.headers.Accept, 'application/json');
        assert.deepEqual(JSON.parse(call.body), body);
    }
});

test('api: a refused preview gives the add-on error code', async () => {
    const { api } = setup(() => json(400, { code: 'bad-input', text: 'The request is not complete.' }));
    await assert.rejects(api.preview(B, {}), (error) => error.code === 'bad-input' && error.status === 400);
});

test('api: answers are handed on unchanged, 204 gives undefined', async () => {
    const answer = { uid: B, present: true, label: '<b>x</b>' };
    const { api } = setup((call) => (call.method === 'DELETE' ? empty(204) : json(200, answer)));
    assert.deepEqual(await api.bridge(B), answer);
    assert.equal(await api.clearTelegrams(B), undefined);
});

test('api: the error mapping', async () => {
    const cases = [
        [json(404, { code: 'unknown-bridge', text: 'There is no such KNX bridge.' }), 404, 'unknown-bridge', 'There is no such KNX bridge.'],
        [json(409, { code: 'monitor-off', text: 'off' }), 409, 'monitor-off', 'off'],
        [json(403, { message: 'Forbidden' }), 403, 'http-403', ''],
        [json(400, { code: 7, text: 'x' }), 400, 'http-400', ''],
        [new Response('<html>proxy</html>', { status: 502 }), 502, 'http-502', ''],
        [empty(500), 500, 'http-500', ''],
        [new Response('not json', { status: 200 }), 200, 'bad-answer', ''],
    ];
    for (const [response, status, code, text] of cases) {
        const { api } = setup(() => response);
        await assert.rejects(api.bridges(), (error) => {
            assert.ok(error instanceof KnxApiError);
            assert.equal(error.status, status);
            assert.equal(error.code, code);
            assert.equal(error.text, text);
            return true;
        });
    }
});

test('api: a network failure is the code network', async () => {
    const { api } = setup(() => {
        throw networkFailure();
    });
    await assert.rejects(api.bridges(), (error) => error instanceof KnxApiError && error.code === 'network' && error.status === 0);
});

test('api: a 401 gives one renewal and the call again with the new token', async () => {
    const { fetch, auth, api } = setup((call) => (call.headers['X-OPENHAB-TOKEN'] === 't1' ? empty(401) : json(200, [])));
    assert.deepEqual(await api.bridges(), []);
    assert.equal(auth.renewals, 1);
    assert.deepEqual(
        fetch.calls.map((call) => call.headers['X-OPENHAB-TOKEN']),
        ['t1', 't2'],
    );
    assert.equal(auth.lostCount, 0);
});

test('api: a second 401 on the same call ends the login and the call fails', async () => {
    const { fetch, auth, api } = setup(() => empty(401));
    await assert.rejects(api.monitor(B), (error) => error.code === 'signed-out' && error.status === 401);
    assert.equal(fetch.calls.length, 2);
    assert.equal(auth.renewals, 1);
    assert.equal(auth.lostCount, 1);
});

test('api: a refused renewal fails the call without asking again', async () => {
    const { fetch, auth, api } = setup(() => empty(401));
    auth.renew = async () => {
        const error = new Error('signed-out');
        error.code = 'signed-out';
        throw error;
    };
    await assert.rejects(api.monitor(B), (error) => error.code === 'signed-out');
    assert.equal(fetch.calls.length, 1);
});

test('api: no token, no call', async () => {
    const { fetch, auth, api } = setup();
    auth.token = async () => {
        const error = new Error('signed-out');
        error.code = 'signed-out';
        throw error;
    };
    await assert.rejects(api.bridges(), (error) => error.code === 'signed-out');
    assert.equal(fetch.calls.length, 0);
});

test('api: reading all pages with the largest limit until total is reached', async () => {
    const total = 2 * PAGE_MAX + 3;
    const { fetch, api } = setup((call) => {
        const offset = Number(call.url.searchParams.get('offset'));
        const limit = Number(call.url.searchParams.get('limit'));
        const items = Array.from({ length: Math.max(0, Math.min(limit, total - offset)) }, (_, i) => ({ address: `31/7/${offset + i}` }));
        return json(200, { items, total, offset });
    });
    assert.equal(PAGE_MAX, 10000);
    const all = await api.allGroupAddresses(B);
    assert.equal(all.length, total);
    assert.equal(all[total - 1].address, `31/7/${total - 1}`);
    assert.deepEqual(
        fetch.calls.map((call) => [call.url.pathname, query(call)]),
        [0, PAGE_MAX, 2 * PAGE_MAX].map((offset) => [`${ROOT}/project/group-addresses`, { offset: String(offset), limit: String(PAGE_MAX) }]),
    );
    const devices = await api.allDevices(B);
    assert.equal(devices.length, total);
    assert.equal(fetch.calls.at(-1).url.pathname, `${ROOT}/project/devices`);
});

test('api: reading all pages stops on an empty page and on a small total', async () => {
    const { fetch, api } = setup(() => json(200, { items: [], total: 50, offset: 0 }));
    assert.deepEqual(await api.allDevices(B), []);
    assert.equal(fetch.calls.length, 1);
    const one = setup(() => json(200, { items: [{ id: 'd1' }], total: 1, offset: 0 }));
    assert.deepEqual(await one.api.allDevices(B), [{ id: 'd1' }]);
    assert.equal(one.fetch.calls.length, 1);
});

test('api: the upload headers with and without a password', async () => {
    const file = new File([Uint8Array.of(1)], 'house.knxproj');
    const { fetch, api } = setup();
    await api.uploadProject(B, file, { password: 'pässword€' });
    await api.uploadProject(B, file, { password: '' });
    await api.uploadProject(B, file, {});
    await api.uploadKeyring(B, file, { password: 'pässword€' });
    await api.uploadKeyring(B, file);
    const passwords = fetch.calls.map((call) => call.headers['X-KNX-Password']);
    const base64 = Buffer.from('pässword€', 'utf8').toString('base64');
    assert.deepEqual(passwords, [base64, undefined, undefined, base64, undefined]);
    for (const call of fetch.calls) {
        assert.equal(call.headers['Content-Type'], 'application/octet-stream');
        assert.equal(call.method, 'PUT');
        assert.equal(call.body, file);
        assert.doesNotMatch(call.url.href, /p%C3%A4ssword|pässword/);
    }
    assert.equal(fetch.calls.filter((call) => 'X-KNX-Password' in call.headers).length, 2);
});

test('api: the language only when chosen and only when the server would take it', async () => {
    const file = new File([Uint8Array.of(1)], 'my house.knxproj');
    const { fetch, api } = setup();
    for (const language of ['de', 'de-CH', 'nl-BE', 'fil', 'zh-Hant-TW', ' en ']) {
        await api.uploadProject(B, file, { language });
    }
    await api.uploadProject(B, file, { language: '' });
    assert.deepEqual(
        fetch.calls.map((call) => query(call)),
        [
            { name: 'my house.knxproj', language: 'de' },
            { name: 'my house.knxproj', language: 'de-CH' },
            { name: 'my house.knxproj', language: 'nl-BE' },
            { name: 'my house.knxproj', language: 'fil' },
            { name: 'my house.knxproj', language: 'zh-Hant-TW' },
            { name: 'my house.knxproj', language: 'en' },
            { name: 'my house.knxproj' },
        ],
    );
    for (const language of ['d', 'deutsch', 'de_CH', 'de-', 'de-C', 'de-ABCDEFGHI', '../x', 'de&x=1']) {
        await assert.rejects(api.uploadProject(B, file, { language }), (error) => error.code === 'bad-language', language);
    }
    assert.equal(fetch.calls.length, 7);
});

test('api: a path part that URL would resolve away is refused before any call', async () => {
    const { fetch, api } = setup();
    for (const device of ['..', '.', '']) {
        await assert.rejects(api.device(B, device), (error) => error.code === 'bad-input' && error.status === 0, device);
        await assert.rejects(api.deviceChannels(B, device), (error) => error.code === 'bad-input', device);
    }
    await assert.rejects(api.monitor('..'), (error) => error.code === 'bad-input');
    await assert.rejects(api.monitor(undefined), (error) => error.code === 'bad-input');
    assert.equal(fetch.calls.length, 0);
    await api.device(B, '..x');
    await api.device(B, '%2e%2e');
    assert.deepEqual(
        fetch.calls.map((call) => call.url.pathname),
        [`${ROOT}/project/devices/..x`, `${ROOT}/project/devices/%252e%252e`],
    );
});

test('api: call 24 sends the four fields of a telegram request as JSON and nothing else of what it is handed', async () => {
    const { fetch, api } = setup(() => json(200, { action: 'write', state: 'sent' }));
    const answer = await api.groupTelegram(B, { address: '31/7/1', action: 'write', dpt: '1.001', value: 'ON', repeat: 9, delay: 5, addresses: ['31/7/2'] });
    assert.deepEqual(answer, { action: 'write', state: 'sent' });
    assert.equal(fetch.calls.length, 1);
    assert.equal(fetch.calls[0].headers['Content-Type'], 'application/json');
    assert.deepEqual(JSON.parse(fetch.calls[0].body), { address: '31/7/1', action: 'write', dpt: '1.001', value: 'ON' });
    assert.deepEqual(query(fetch.calls[0]), {}, 'nothing of a telegram goes into the address of the call');
});

test('api: a refused telegram gives the add-on error code and is not sent a second time', async () => {
    const { fetch, api } = setup(() => json(429, { code: 'too-many-telegrams', text: 'made-up' }));
    await assert.rejects(api.groupTelegram(B, { address: '31/7/1', action: 'read' }), (error) => error.code === 'too-many-telegrams' && error.status === 429);
    assert.equal(fetch.calls.length, 1);
});
