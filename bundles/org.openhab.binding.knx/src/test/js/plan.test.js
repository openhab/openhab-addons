import { test } from 'node:test';
import assert from 'node:assert/strict';

import { createCore } from '../web/js/core.js';
import { ANSWER_LOST, runPlan, undo } from '../web/js/plan.js';
import { createFetch, empty, json, networkFailure, settle } from './fakes.js';

const ORIGIN = 'http://openhab.invalid';
const REST = '/oh/rest/';
const BASE = new URL(REST, ORIGIN);
const BRIDGE = 'knx:ip:made-up';
const T = 'knx:device:made-up:Hall';
const LIGHT = `${T}:Light`;
const TEMPERATURE = `${T}:Temperature`;

/** One group, one Thing with two channels, two Items in the group, two links: as call 22 gives them. */
function plan() {
    return [
        { kind: 'GROUP', itemName: 'gHall', item: { type: 'Group', name: 'gHall', label: 'Hall', tags: ['Room'], groupNames: [] } },
        {
            kind: 'THING',
            uid: T,
            thing: {
                UID: T,
                thingTypeUID: 'knx:device',
                bridgeUID: BRIDGE,
                label: 'Hall',
                configuration: {},
                channels: [
                    {
                        uid: LIGHT,
                        id: 'Light',
                        channelTypeUID: 'knx:switch',
                        kind: 'STATE',
                        itemType: 'Switch',
                        label: 'Light',
                        configuration: { ga: '1.001:31/7/1+<31/7/2' },
                        properties: {},
                        defaultTags: [],
                    },
                    {
                        uid: TEMPERATURE,
                        id: 'Temperature',
                        channelTypeUID: 'knx:number',
                        kind: 'STATE',
                        itemType: 'Number:Temperature',
                        label: 'Temperature',
                        configuration: { ga: '9.001:<31/7/3' },
                        properties: {},
                        defaultTags: [],
                    },
                ],
            },
        },
        { kind: 'ITEM', itemName: 'Hall_Light', item: { type: 'Switch', name: 'Hall_Light', label: 'Light', tags: [], groupNames: ['gHall'] } },
        {
            kind: 'ITEM',
            itemName: 'Hall_Temperature',
            item: {
                type: 'Number:Temperature',
                name: 'Hall_Temperature',
                label: 'Temperature',
                category: 'temperature',
                tags: ['Measurement', 'Temperature'],
                groupNames: ['gHall'],
            },
        },
        { kind: 'LINK', itemName: 'Hall_Light', channelUID: LIGHT, link: { itemName: 'Hall_Light', channelUID: LIGHT, configuration: {} } },
        {
            kind: 'LINK',
            itemName: 'Hall_Temperature',
            channelUID: TEMPERATURE,
            link: { itemName: 'Hall_Temperature', channelUID: TEMPERATURE, configuration: {} },
        },
    ];
}

const CHECKS = [
    'GET items/gHall',
    `GET things/${T}`,
    'GET items/Hall_Light',
    'GET items/Hall_Temperature',
    `GET links/Hall_Light/${LIGHT}`,
    `GET links/Hall_Temperature/${TEMPERATURE}`,
];
const CREATES = [
    ['GET items/gHall', 'PUT items/gHall'],
    [`GET things/${T}`, 'POST things'],
    ['GET items/Hall_Light', 'PUT items/Hall_Light'],
    ['GET items/Hall_Temperature', 'PUT items/Hall_Temperature'],
    [`GET links/Hall_Light/${LIGHT}`, `PUT links/Hall_Light/${LIGHT}`],
    [`GET links/Hall_Temperature/${TEMPERATURE}`, `PUT links/Hall_Temperature/${TEMPERATURE}`],
];

/**
 * The addresses of fixed point 8 and nothing else, as openHAB answers them. A call to any other address, with a query
 * or of another method, is written into strays and fails like a network failure; every test asserts strays empty.
 */
function routeOf(call) {
    if (call.url.origin !== ORIGIN || call.url.search !== '' || call.url.hash !== '' || !call.url.pathname.startsWith(REST)) {
        return null;
    }
    const path = call.url.pathname.slice(REST.length);
    const parts = path.split('/').map((part) => decodeURIComponent(part));
    const allowed = (methods, route) => (methods.includes(call.method) ? route : null);
    if (path === 'things') {
        return allowed(['GET', 'POST'], { kind: 'things' });
    }
    if (parts.length === 2 && parts[0] === 'things') {
        return allowed(['GET', 'DELETE'], { kind: 'thing', id: parts[1] });
    }
    if (parts.length === 2 && parts[0] === 'items') {
        return allowed(['GET', 'PUT', 'DELETE'], { kind: 'item', id: parts[1] });
    }
    if (parts.length === 3 && parts[0] === 'links') {
        return allowed(['GET', 'PUT', 'DELETE'], { kind: 'link', id: `${parts[1]} ${parts[2]}` });
    }
    return null;
}

function answer(call, route, state) {
    const store = { thing: state.things, item: state.items, link: state.links }[route.kind];
    switch (route.kind) {
        case 'things': {
            if (call.method === 'GET') {
                return json(200, []);
            }
            const uid = JSON.parse(call.body).UID;
            if (state.things.has(uid)) {
                return empty(409);
            }
            state.things.add(uid);
            return json(201, {});
        }
        default:
            break;
    }
    switch (call.method) {
        case 'GET':
            return store.has(route.id) ? json(200, {}) : empty(404);
        case 'PUT': {
            const existed = store.has(route.id);
            store.add(route.id);
            return route.kind === 'link' || existed ? json(200, {}) : json(201, {});
        }
        case 'DELETE':
            if (!store.has(route.id)) {
                return empty(404);
            }
            store.delete(route.id);
            return empty(route.kind === 'thing' ? 202 : 200);
        default:
            throw new Error('unreachable');
    }
}

function fakeAuth() {
    const auth = {
        renewals: 0,
        lostCount: 0,
        current: 't1',
        async token() {
            return auth.current;
        },
        async renew() {
            auth.renewals++;
            auth.current = `t${auth.renewals + 1}`;
            return auth.current;
        },
        lost() {
            auth.lostCount++;
        },
    };
    return auth;
}

/**
 * @param {{things?: string[], items?: string[], links?: string[], fault?: Function}} [options] fault(call, route,
 *     state) may give a Response for a call, or throw for a network failure; undefined lets openHAB answer
 */
function openhab({ things = [], items = [], links = [], fault } = {}) {
    const state = { things: new Set(things), items: new Set(items), links: new Set(links) };
    const strays = [];
    const fetch = createFetch(async (call) => {
        const route = routeOf(call);
        if (route === null) {
            strays.push(`${call.method} ${call.url.href}`);
            throw new Error('a call outside fixed point 8');
        }
        const forced = await fault?.(call, route, state);
        return forced ?? answer(call, route, state);
    });
    const auth = fakeAuth();
    const core = createCore({ fetch, base: BASE, auth });
    const events = [];
    return {
        fetch,
        auth,
        core,
        state,
        strays,
        events,
        progress: (event) => events.push(event),
        /** {@return the calls as METHOD and the decoded path below the REST root} */
        log: () => fetch.calls.map((call) => `${call.method} ${call.url.pathname.slice(REST.length).split('/').map(decodeURIComponent).join('/')}`),
    };
}

function assertOnlyFixedPointEight(oh) {
    assert.deepEqual(oh.strays, []);
    for (const call of oh.fetch.calls) {
        assert.match(call.headers['X-OPENHAB-TOKEN'], /^t\d$/);
        assert.equal(call.headers.Authorization, undefined);
        assert.equal(call.headers.Accept, 'application/json');
        assert.equal(call.headers['Content-Type'], 'application/json');
    }
}

function ids(list) {
    return list.map((entry) => [entry.kind, entry.uid ?? entry.itemName, entry.channelUID, entry.mark].filter((part) => part !== undefined).join(' '));
}

const ALL = ['GROUP gHall', `THING ${T}`, 'ITEM Hall_Light', 'ITEM Hall_Temperature', `LINK Hall_Light ${LIGHT}`, `LINK Hall_Temperature ${TEMPERATURE}`];

test('plan: the order of calls, all checks before the first create', async () => {
    const oh = openhab();
    const result = await runPlan(plan(), oh.core, oh.progress);
    assert.equal(result.status, 'done');
    assert.deepEqual(oh.log(), [...CHECKS, ...CREATES.flat()]);
    assert.deepEqual(ids(result.created), ALL);
    assert.deepEqual([...oh.state.things], [T]);
    assert.deepEqual([...oh.state.items], ['gHall', 'Hall_Light', 'Hall_Temperature']);
    assert.deepEqual([...oh.state.links], [`Hall_Light ${LIGHT}`, `Hall_Temperature ${TEMPERATURE}`]);
    assert.deepEqual(
        oh.events.map((event) => `${event.index} ${event.state}`),
        [0, 1, 2, 3, 4, 5].map((i) => `${i} checked`).concat([0, 1, 2, 3, 4, 5].map((i) => `${i} created`)),
    );
    assertOnlyFixedPointEight(oh);
});

test('plan: an existing Thing, Item, group or link stops before anything is created, with the list of what exists', async () => {
    const cases = [
        [{ things: [T] }, [1]],
        [{ items: ['Hall_Light'] }, [2]],
        [{ items: ['gHall'] }, [0]],
        [{ links: [`Hall_Temperature ${TEMPERATURE}`] }, [5]],
        [{ items: ['gHall', 'Hall_Temperature'], links: [`Hall_Light ${LIGHT}`] }, [0, 3, 4]],
    ];
    for (const [there, existing] of cases) {
        const oh = openhab(there);
        const result = await runPlan(plan(), oh.core, oh.progress);
        assert.equal(result.status, 'exists', JSON.stringify(there));
        assert.deepEqual(result.existing, existing);
        assert.deepEqual(oh.log(), CHECKS, 'every check, then nothing');
        assert.deepEqual(result.created, []);
        assert.deepEqual(
            oh.events.filter((event) => event.state === 'exists').map((event) => event.index),
            existing,
        );
        assertOnlyFixedPointEight(oh);
    }
});

test('plan: a failed check stops at once, nothing is created', async () => {
    for (const fault of [() => empty(500), () => empty(403), () => Promise.reject(networkFailure())]) {
        const oh = openhab({ fault: (call) => (call.url.pathname.endsWith('/Hall_Light') ? fault() : undefined) });
        const result = await runPlan(plan(), oh.core, oh.progress);
        assert.equal(result.status, 'stopped');
        assert.equal(result.index, 2);
        assert.ok(['http-500', 'http-403', 'network'].includes(result.code), result.code);
        assert.deepEqual(oh.log(), CHECKS.slice(0, 3));
        assert.deepEqual(result.created, []);
        assert.deepEqual(oh.events.at(-1), { index: 2, state: 'failed', code: result.code });
        assertOnlyFixedPointEight(oh);
    }
});

test('plan: a failure in the middle stops and leaves the right undo list', async () => {
    const refused = openhab({ fault: (call) => (call.method === 'PUT' && call.url.pathname.endsWith('/Hall_Temperature') ? json(400, {}) : undefined) });
    const result = await runPlan(plan(), refused.core);
    assert.equal(result.status, 'stopped');
    assert.equal(result.index, 3);
    assert.equal(result.code, 'http-400');
    assert.deepEqual(ids(result.created), ALL.slice(0, 3));
    assert.deepEqual(refused.log(), [...CHECKS, ...CREATES.slice(0, 4).flat()]);
    assertOnlyFixedPointEight(refused);

    let gets = 0;
    const failing = openhab({
        fault: (call) => (call.method === 'GET' && call.url.pathname.includes('/links/Hall_Light/') && ++gets === 2 ? empty(503) : undefined),
    });
    const second = await runPlan(plan(), failing.core);
    assert.equal(second.status, 'stopped');
    assert.equal(second.index, 4);
    assert.equal(second.code, 'http-503');
    assert.deepEqual(ids(second.created), ALL.slice(0, 4));
    assert.deepEqual(failing.log(), [...CHECKS, ...CREATES.slice(0, 4).flat(), CREATES[4][0]], 'no create after a failed check');
    assertOnlyFixedPointEight(failing);
});

test('plan: an object made by someone else between the check and the create stops the run and is not undone', async () => {
    const between = openhab({
        fault: (call, route, state) => {
            if (call.method === 'PUT' && route.id === 'gHall') {
                state.things.add(T);
            }
            return undefined;
        },
    });
    const result = await runPlan(plan(), between.core);
    assert.equal(result.status, 'stopped');
    assert.equal(result.index, 1);
    assert.equal(result.code, 'exists-now');
    assert.deepEqual(ids(result.created), ['GROUP gHall']);
    assert.deepEqual(between.log(), [...CHECKS, ...CREATES[0], CREATES[1][0]]);

    const conflict = openhab({ fault: (call) => (call.method === 'POST' ? empty(409) : undefined) });
    const second = await runPlan(plan(), conflict.core);
    assert.equal(second.code, 'exists-now');
    assert.deepEqual(ids(second.created), ['GROUP gHall']);
    assert.deepEqual(conflict.log(), [...CHECKS, ...CREATES.slice(0, 2).flat()]);
    assertOnlyFixedPointEight(between);
    assertOnlyFixedPointEight(conflict);
});

test('plan: an Item PUT answering 200 stops, and that Item is not on the undo list', async () => {
    for (const [name, index] of [
        ['Hall_Light', 2],
        ['gHall', 0],
    ]) {
        const oh = openhab({ fault: (call, route, state) => (call.method === 'PUT' && route.id === name ? (state.items.add(name), json(200, {})) : undefined) });
        const result = await runPlan(plan(), oh.core);
        assert.equal(result.status, 'stopped');
        assert.equal(result.index, index);
        assert.equal(result.code, 'item-overwritten');
        assert.deepEqual(ids(result.created), ALL.slice(0, index));
        assert.deepEqual(oh.log(), [...CHECKS, ...CREATES.slice(0, index + 1).flat()]);
        const undone = await undo(result.created, oh.core);
        assert.equal(undone.status, 'done');
        assert.ok(!oh.log().includes(`DELETE items/${name}`), 'the Item someone else made is never deleted');
        assert.ok(oh.state.items.has(name));
        assertOnlyFixedPointEight(oh);
    }
});

test('plan: a create whose answer is lost is looked for once, and goes on the undo list marked when it is there', async () => {
    const lostAfterCreating = (method, id, how) => (call, route, state) => {
        if (call.method === method && (route.id === id || (route.kind === 'things' && id === T))) {
            answer(call, route, state);
            return how();
        }
        return undefined;
    };
    for (const how of [() => Promise.reject(networkFailure()), () => empty(503), () => empty(500)]) {
        const oh = openhab({ fault: lostAfterCreating('POST', T, how) });
        const result = await runPlan(plan(), oh.core, oh.progress);
        assert.equal(result.status, 'stopped');
        assert.equal(result.index, 1);
        assert.equal(result.code, 'answer-lost');
        assert.equal(result.found, true);
        assert.deepEqual(ids(result.created), ['GROUP gHall', `THING ${T} ${ANSWER_LOST}`]);
        assert.equal(ANSWER_LOST, 'answer-lost');
        assert.deepEqual(oh.log(), [...CHECKS, ...CREATES.slice(0, 2).flat(), `GET things/${T}`], 'one GET, then the run stops');
        assert.deepEqual(oh.events.at(-1), { index: 1, state: 'failed', code: 'answer-lost' });
        assertOnlyFixedPointEight(oh);
    }

    const item = openhab({ fault: lostAfterCreating('PUT', 'Hall_Light', () => Promise.reject(networkFailure())) });
    const itemResult = await runPlan(plan(), item.core);
    assert.deepEqual(ids(itemResult.created), [...ALL.slice(0, 2), `ITEM Hall_Light ${ANSWER_LOST}`]);

    const notMade = openhab({ fault: (call) => (call.method === 'POST' ? Promise.reject(networkFailure()) : undefined) });
    const notMadeResult = await runPlan(plan(), notMade.core);
    assert.equal(notMadeResult.code, 'answer-lost');
    assert.equal(notMadeResult.found, false);
    assert.deepEqual(ids(notMadeResult.created), ['GROUP gHall']);
    assert.deepEqual(notMade.log(), [...CHECKS, ...CREATES.slice(0, 2).flat(), `GET things/${T}`]);

    let posted = false;
    const unknown = openhab({
        fault: (call, route, state) => {
            if (call.method === 'POST') {
                posted = true;
                answer(call, route, state);
                return Promise.reject(networkFailure());
            }
            return posted ? Promise.reject(networkFailure()) : undefined;
        },
    });
    const unknownResult = await runPlan(plan(), unknown.core);
    assert.equal(unknownResult.code, 'answer-lost');
    assert.equal(unknownResult.found, null, 'not known whether it was made');
    assert.deepEqual(ids(unknownResult.created), ['GROUP gHall']);
    assert.equal(unknown.fetch.calls.length, CHECKS.length + 4 + 1);
    for (const oh of [item, notMade, unknown]) {
        assertOnlyFixedPointEight(oh);
    }
});

test('plan: undo goes links, Items, Things, groups, backwards; 404 and a Thing answering 202 are done', async () => {
    const oh = openhab();
    const result = await runPlan(plan(), oh.core);
    oh.state.links.delete(`Hall_Light ${LIGHT}`);
    const before = oh.fetch.calls.length;
    const undone = await undo(result.created, oh.core, oh.progress);
    assert.equal(undone.status, 'done');
    assert.deepEqual(oh.log().slice(before), [
        `DELETE links/Hall_Temperature/${TEMPERATURE}`,
        `DELETE links/Hall_Light/${LIGHT}`,
        'DELETE items/Hall_Temperature',
        'DELETE items/Hall_Light',
        `DELETE things/${T}`,
        'DELETE items/gHall',
    ]);
    assert.deepEqual(ids(undone.removed), [...ALL].reverse());
    assert.deepEqual(undone.failed, []);
    assert.deepEqual(undone.left, []);
    assert.deepEqual(
        oh.events.slice(-6).map((event) => `${event.index} ${event.state}`),
        ['5 removed', '4 removed', '3 removed', '2 removed', '1 removed', '0 removed'],
    );
    assert.equal(oh.state.things.size + oh.state.items.size + oh.state.links.size, 0);
    assertOnlyFixedPointEight(oh);
});

test('plan: undo keeps that order whatever the order of the list', async () => {
    const oh = openhab();
    const result = await runPlan(plan(), oh.core);
    assert.ok(Object.isFrozen(result.created));
    assert.throws(() => result.created.reverse());
    const before = oh.fetch.calls.length;
    await undo(result.created, oh.core);
    assert.deepEqual(
        oh.log()
            .slice(before)
            .map((call) => call.split(' ')[1].split('/')[0]),
        ['links', 'links', 'items', 'items', 'things', 'items'],
    );
});

test('plan: a failing delete does not stop the rest, and what is left can be undone again', async () => {
    let refuse = true;
    const oh = openhab({ fault: (call, route) => (call.method === 'DELETE' && route.id === 'Hall_Light' && refuse ? empty(500) : undefined) });
    const result = await runPlan(plan(), oh.core);
    const before = oh.fetch.calls.length;
    const undone = await undo(result.created, oh.core, oh.progress);
    assert.equal(undone.status, 'partial');
    assert.deepEqual(
        undone.failed.map((failure) => [failure.entry.itemName, failure.code]),
        [['Hall_Light', 'http-500']],
    );
    assert.equal(oh.fetch.calls.length - before, 6, 'every delete was tried');
    assert.deepEqual(ids(undone.left), ['ITEM Hall_Light']);
    assert.ok(oh.events.some((event) => event.index === 2 && event.state === 'failed' && event.code === 'http-500'));
    refuse = false;
    const again = await undo(undone.left, oh.core);
    assert.equal(again.status, 'done');
    assert.equal(oh.log().at(-1), 'DELETE items/Hall_Light');
    assert.equal(oh.state.items.size, 0);

    const offline = openhab({ fault: (call) => (call.method === 'DELETE' && call.url.pathname.includes('/links/') ? Promise.reject(networkFailure()) : undefined) });
    const offlineRun = await runPlan(plan(), offline.core);
    const offlineUndo = await undo(offlineRun.created, offline.core);
    assert.deepEqual(
        offlineUndo.failed.map((failure) => failure.code),
        ['network', 'network'],
    );
    assert.deepEqual(ids(offlineUndo.removed), [...ALL.slice(0, 4)].reverse());
    assertOnlyFixedPointEight(oh);
    assertOnlyFixedPointEight(offline);
});

test('plan: only 200 counts for an Item or link delete; 202 only for a Thing', async () => {
    const oh = openhab({ fault: (call, route) => (call.method === 'DELETE' && route.kind === 'item' ? empty(202) : undefined) });
    const result = await runPlan(plan(), oh.core);
    const undone = await undo(result.created, oh.core);
    assert.deepEqual(
        undone.failed.map((failure) => [failure.entry.kind, failure.code]),
        [
            ['ITEM', 'http-202'],
            ['ITEM', 'http-202'],
            ['GROUP', 'http-202'],
        ],
    );
    assert.deepEqual(ids(undone.removed), [`LINK Hall_Temperature ${TEMPERATURE}`, `LINK Hall_Light ${LIGHT}`, `THING ${T}`]);
});

test('plan: undo takes only a list a run gave, and each list once', async () => {
    const oh = openhab();
    const handMade = [{ kind: 'THING', uid: 'knx:device:made-up:Other' }];
    const refused = await undo(handMade, oh.core);
    assert.equal(refused.status, 'refused');
    const result = await runPlan(plan(), oh.core);
    assert.equal((await undo([...result.created], oh.core)).status, 'refused', 'a copy is no list of a run');
    const before = oh.fetch.calls.length;
    assert.equal(before, CHECKS.length + 12);
    assert.equal((await undo(result.created, oh.core)).status, 'done');
    assert.equal((await undo(result.created, oh.core)).status, 'refused');
    assert.equal(oh.fetch.calls.length, before + 6);
    assertOnlyFixedPointEight(oh);
});

test('plan: a second runPlan or an undo during a run starts nothing', async () => {
    let release;
    const gate = new Promise((resolve) => {
        release = resolve;
    });
    const oh = openhab({ fault: async (call) => (oh.fetch.calls.length === 1 ? (await gate, undefined) : undefined) });
    const first = runPlan(plan(), oh.core);
    await settle();
    assert.equal(oh.fetch.calls.length, 1);
    const second = await runPlan(plan(), oh.core, oh.progress);
    assert.equal(second.status, 'busy');
    assert.deepEqual(second.created, []);
    const other = openhab();
    const earlier = await runPlan(plan(), other.core);
    assert.equal(earlier.status, 'busy');
    assert.equal((await undo(earlier.created, other.core)).status, 'busy');
    assert.equal(oh.fetch.calls.length, 1);
    assert.equal(other.fetch.calls.length, 0);
    assert.deepEqual(oh.events, []);
    release();
    const result = await first;
    assert.equal(result.status, 'done');

    let releaseUndo;
    const undoGate = new Promise((resolve) => {
        releaseUndo = resolve;
    });
    const slow = openhab({ fault: async (call) => (call.method === 'DELETE' ? (await undoGate, undefined) : undefined) });
    const run = await runPlan(plan(), slow.core);
    const undoing = undo(run.created, slow.core);
    await settle();
    const during = await runPlan(plan(), other.core);
    assert.equal(during.status, 'busy');
    assert.equal(other.fetch.calls.length, 0);
    releaseUndo();
    assert.equal((await undoing).status, 'done');
    assert.equal((await runPlan(plan(), other.core)).status, 'done', 'after the run, a run starts again');
});

/** {@return the plan with one change made by edit} */
function changed(edit) {
    const steps = plan();
    edit(steps);
    return steps;
}

test('plan: a bad kind, id, body or order starts nothing', async () => {
    const bad = {
        'not a list': ['empty', () => 'steps'],
        'an empty list': ['empty', () => []],
        'a step that is no object': ['bad-step', () => changed((s) => (s[2] = null))],
        'a kind outside the four': ['bad-kind', () => changed((s) => (s[0].kind = 'BRIDGE'))],
        'a kind in lower case': ['bad-kind', () => changed((s) => (s[1].kind = 'thing'))],
        'a Thing UID of another type': ['bad-id', () => changed((s) => (s[1].uid = s[1].thing.UID = 'knx:ip:made-up'))],
        'a Thing UID with a slash': ['bad-id', () => changed((s) => (s[1].uid = s[1].thing.UID = 'knx:device:made-up:a/b'))],
        'a Thing UID with a dot part': ['bad-id', () => changed((s) => (s[1].uid = s[1].thing.UID = 'knx:device:..:x'))],
        'an Item name with a leading digit': ['bad-id', () => changed((s) => (s[2].itemName = s[2].item.name = '1Hall'))],
        'an Item name with a slash': ['bad-id', () => changed((s) => (s[3].itemName = s[3].item.name = 'Hall/x'))],
        'a group name with a space': ['bad-id', () => changed((s) => (s[0].itemName = s[0].item.name = 'g Hall'))],
        'a channel UID of no Thing': ['bad-id', () => changed((s) => (s[4].channelUID = s[4].link.channelUID = 'Light'))],
        'a link Item name with a dash': ['bad-id', () => changed((s) => (s[5].itemName = s[5].link.itemName = 'Hall-Temperature'))],
        'a Thing body with another UID': ['bad-body', () => changed((s) => (s[1].thing.UID = 'knx:device:made-up:Other'))],
        'an Item body with another name': ['bad-body', () => changed((s) => (s[2].item.name = 'Other'))],
        'a link body with another Item': ['bad-body', () => changed((s) => (s[4].link.itemName = 'Hall_Temperature'))],
        'a link body with another channel': ['bad-body', () => changed((s) => (s[4].link.channelUID = TEMPERATURE))],
        'a Thing step without its body': ['bad-body', () => changed((s) => delete s[1].thing)],
        'an Item step with a link body too': ['bad-body', () => changed((s) => (s[2].link = s[4].link))],
        'a link step with an Item body': ['bad-body', () => changed((s) => (s[4] = { ...s[4], link: undefined, item: s[2].item }))],
        'a Thing before a group': ['bad-order', () => changed((s) => s.splice(0, 2, s[1], s[0]))],
        'a link before its Item': ['bad-order', () => changed((s) => s.splice(3, 2, s[4], s[3]))],
        'one Thing twice': ['duplicate', () => changed((s) => s.splice(2, 0, structuredClone(s[1])))],
        'one Item twice': ['duplicate', () => changed((s) => s.splice(3, 0, structuredClone(s[2])))],
        'an Item named like a group': ['duplicate', () => changed((s) => (s[2].itemName = s[2].item.name = 'gHall'))],
        'one link twice': ['duplicate', () => changed((s) => s.push(structuredClone(s[5])))],
        'a Thing setting': ['thing-setting', () => changed((s) => (s[1].thing.configuration = { address: '7.7.1' }))],
        'any Thing setting': ['thing-setting', () => changed((s) => (s[1].thing.configuration = { pingInterval: '600' }))],
        'an address in a channel': ['thing-setting', () => changed((s) => (s[1].thing.channels[0].configuration.address = '7.7.1'))],
        'a channel of another Thing': ['bad-body', () =>
            changed((s) => {
                s[1].thing.channels[1].uid = 'knx:device:made-up:Other:Temperature';
                s.pop();
            })],
        'an Item in a group the plan does not make': ['unknown-group', () => changed((s) => (s[3].item.groupNames = ['gHall', 'gExisting']))],
        'a group in a group': ['unknown-group', () => changed((s) => (s[0].item.groupNames = ['gOther']))],
        'a group that is no Group': ['bad-body', () => changed((s) => (s[0].item.type = 'Switch'))],
        'an Item that is a Group': ['bad-body', () => changed((s) => (s[2].item.type = 'Group'))],
        'a link to an Item the plan does not make': ['unknown-link', () => changed((s) => (s[4].itemName = s[4].link.itemName = 'Existing_item'))],
        'a link to a group': ['unknown-link', () => changed((s) => (s[4].itemName = s[4].link.itemName = 'gHall'))],
        'a link to a channel the Thing does not have': ['unknown-link', () => changed((s) => (s[4].channelUID = s[4].link.channelUID = `${T}:Other`))],
        'a link to a Thing the plan does not make': ['unknown-link', () => changed((s) => (s[4].channelUID = s[4].link.channelUID = 'knx:device:made-up:Other:Light'))],
    };
    const good = openhab();
    for (const [name, [reason, make]] of Object.entries(bad)) {
        const oh = openhab();
        const result = await runPlan(make(), oh.core, oh.progress);
        assert.equal(result.status, 'refused', name);
        assert.equal(result.code, 'bad-plan', name);
        assert.equal(result.reason, reason, name);
        assert.equal(oh.fetch.calls.length, 0, name);
        assert.deepEqual(oh.events, [], name);
        assert.deepEqual(result.created, [], name);
    }
    assert.equal((await runPlan(plan(), good.core)).status, 'done', 'the plan the bad ones are made from runs');
});

/** The plan with its group under a floor group that is made first. */
function nested() {
    const steps = plan();
    steps[0].item.groupNames = ['gFloor'];
    steps.unshift({ kind: 'GROUP', itemName: 'gFloor', item: { type: 'Group', name: 'gFloor', label: 'Floor', category: 'groundfloor', tags: ['Floor'], groupNames: [] } });
    return steps;
}

test('plan: a group lies in a group made before it; the undo removes the inner group before the outer one', async () => {
    const oh = openhab();
    const result = await runPlan(nested(), oh.core, oh.progress);
    assert.equal(result.status, 'done');
    assert.deepEqual([...oh.state.items].slice(0, 2), ['gFloor', 'gHall'], 'the outer group is made first');
    const put = oh.fetch.calls.find((call) => call.method === 'PUT' && call.url.pathname.endsWith('/items/gHall'));
    assert.deepEqual(JSON.parse(put.body).groupNames, ['gFloor']);
    const before = oh.fetch.calls.length;
    const undone = await undo(result.created, oh.core);
    assert.equal(undone.status, 'done');
    assert.deepEqual(
        oh
            .log()
            .slice(before)
            .filter((line) => line.startsWith('DELETE items/g')),
        ['DELETE items/gHall', 'DELETE items/gFloor'],
    );
    assert.equal(oh.state.items.size, 0);
});

test('plan: a group in a group that comes after it, in two groups, or in one nobody named starts nothing', async () => {
    const cases = {
        'the outer group after the inner one': (s) => s.splice(0, 2, s[1], s[0]),
        'a group in two groups': (s) => (s[1].item.groupNames = ['gFloor', 'gFloor']),
        'a group in itself': (s) => (s[0].item.groupNames = ['gFloor']),
    };
    for (const [name, change] of Object.entries(cases)) {
        const steps = nested();
        change(steps);
        const oh = openhab();
        const result = await runPlan(steps, oh.core, oh.progress);
        assert.deepEqual([result.status, result.reason], ['refused', 'unknown-group'], name);
        assert.equal(oh.fetch.calls.length, 0, name);
    }
});

/** The plan whose group is not made: openHAB has it, the Items go into it. */
function intoExisting() {
    return plan().slice(1);
}

test('plan: a group openHAB has already is used as it is: looked at first, never made, never undone', async () => {
    const oh = openhab({ items: ['gHall'] });
    const result = await runPlan(intoExisting(), oh.core, oh.progress, { existingGroups: ['gHall', 'gUnused'] });
    assert.equal(result.status, 'done');
    assert.equal(oh.log()[0], 'GET items/gHall', 'the first call looks whether the group is still there');
    assert.deepEqual(
        oh.log().filter((line) => line.endsWith('items/gHall') && !line.startsWith('GET')),
        [],
        'the group itself is never written',
    );
    assert.equal(oh.log().some((line) => line.includes('gUnused')), false, 'a group nothing goes into is not looked at');
    assert.deepEqual(ids(result.created), ALL.slice(1));
    await undo(result.created, oh.core);
    assert.deepEqual([...oh.state.items], ['gHall'], 'the undo leaves the group of openHAB');
    assertOnlyFixedPointEight(oh);
});

test('plan: a new group can lie in a group of openHAB', async () => {
    const steps = plan();
    steps[0].item.groupNames = ['gFloor'];
    const oh = openhab({ items: ['gFloor'] });
    const result = await runPlan(steps, oh.core, oh.progress, { existingGroups: ['gFloor'] });
    assert.equal(result.status, 'done');
    assert.deepEqual(ids(result.created), ALL);
});

test('plan: a group the answer called existing that is gone stops the run before anything is made', async () => {
    const oh = openhab();
    const result = await runPlan(intoExisting(), oh.core, oh.progress, { existingGroups: ['gHall'] });
    assert.deepEqual([result.status, result.index, result.code], ['stopped', -1, 'group-gone']);
    assert.deepEqual(oh.log(), ['GET items/gHall']);
    assert.deepEqual(result.created, []);
});

test('plan: without the answer naming it, an Item in a group of openHAB starts nothing; so does a made group named as existing or a name that is none', async () => {
    const cases = [
        [intoExisting(), undefined, 'unknown-group'],
        [intoExisting(), { existingGroups: ['gOther'] }, 'unknown-group'],
        [plan(), { existingGroups: ['gHall'] }, 'duplicate'],
        [intoExisting(), { existingGroups: ['gHall', '../x'] }, 'bad-existing'],
        [intoExisting(), { existingGroups: 'gHall' }, 'bad-existing'],
    ];
    for (const [steps, options, reason] of cases) {
        const oh = openhab({ items: ['gHall'] });
        const result = await runPlan(steps, oh.core, oh.progress, options);
        assert.deepEqual([result.status, result.reason], ['refused', reason]);
        assert.equal(oh.fetch.calls.length, 0);
    }
});

test('plan: ids are encoded in the path', async () => {
    const oh = openhab();
    await runPlan(plan(), oh.core);
    const paths = oh.fetch.calls.map((call) => call.url.pathname);
    assert.ok(paths.includes('/oh/rest/things/knx%3Adevice%3Amade-up%3AHall'), paths.join('\n'));
    assert.ok(paths.includes('/oh/rest/links/Hall_Light/knx%3Adevice%3Amade-up%3AHall%3ALight'));
    assert.ok(paths.every((path) => !path.slice(REST.length).includes(':')), 'no colon left unencoded');
    assertOnlyFixedPointEight(oh);
});

test('plan: bodies go out unchanged, also when the steps change during the run', async () => {
    const steps = plan();
    const original = plan();
    const oh = openhab();
    const result = await runPlan(steps, oh.core, (event) => {
        if (event.index === 0 && event.state === 'checked') {
            steps[1].thing.configuration.address = '7.7.1';
            steps[2].item.groupNames.push('gExisting');
            steps[4].itemName = 'Other';
        }
    });
    assert.equal(result.status, 'done');
    const bodies = oh.fetch.calls.filter((call) => call.method === 'POST' || call.method === 'PUT').map((call) => JSON.parse(call.body));
    assert.deepEqual(
        bodies,
        original.map((step) => step.thing ?? step.item ?? step.link),
    );
    assert.ok(oh.log().includes(`PUT links/Hall_Light/${LIGHT}`));
    assertOnlyFixedPointEight(oh);
});

test('plan: the token header is X-OPENHAB-TOKEN on every call of a run and an undo', async () => {
    const oh = openhab();
    const result = await runPlan(plan(), oh.core);
    await undo(result.created, oh.core);
    assert.equal(oh.fetch.calls.length, CHECKS.length + 12 + 6);
    for (const call of oh.fetch.calls) {
        assert.equal(call.headers['X-OPENHAB-TOKEN'], 't1');
        assert.deepEqual(Object.keys(call.headers).sort(), ['Accept', 'Content-Type', 'X-OPENHAB-TOKEN']);
    }
});

test('plan: a 401 renews the token once and runs the step again from its GET', async () => {
    const once = openhab({ fault: (call, route) => (call.method === 'PUT' && route.id === 'Hall_Light' && call.headers['X-OPENHAB-TOKEN'] === 't1' ? empty(401) : undefined) });
    const result = await runPlan(plan(), once.core);
    assert.equal(result.status, 'done');
    assert.equal(once.auth.renewals, 1);
    assert.deepEqual(once.log().slice(CHECKS.length + 4, CHECKS.length + 8), [
        'GET items/Hall_Light',
        'PUT items/Hall_Light',
        'GET items/Hall_Light',
        'PUT items/Hall_Light',
    ]);
    assert.deepEqual(ids(result.created), ALL);

    const check = openhab({ fault: (call) => (call.headers['X-OPENHAB-TOKEN'] === 't1' && call.url.pathname.endsWith('/gHall') ? empty(401) : undefined) });
    const checked = await runPlan(plan(), check.core);
    assert.equal(checked.status, 'done');
    assert.deepEqual(check.log().slice(0, 3), ['GET items/gHall', 'GET items/gHall', `GET things/${T}`]);

    const twice = openhab({ fault: (call, route) => (call.method === 'PUT' && route.id === 'Hall_Light' ? empty(401) : undefined) });
    const stopped = await runPlan(plan(), twice.core);
    assert.equal(stopped.status, 'stopped');
    assert.equal(stopped.code, 'signed-out');
    assert.equal(stopped.index, 2);
    assert.equal(twice.auth.renewals, 1);
    assert.equal(twice.auth.lostCount, 1);
    assert.deepEqual(ids(stopped.created), ALL.slice(0, 2));
    assert.equal(twice.fetch.calls.length, CHECKS.length + 4 + 4);

    const deleting = openhab({ fault: (call) => (call.method === 'DELETE' && call.headers['X-OPENHAB-TOKEN'] === 't1' ? empty(401) : undefined) });
    const made = await runPlan(plan(), deleting.core);
    const before = deleting.fetch.calls.length;
    const undone = await undo(made.created, deleting.core);
    assert.equal(undone.status, 'done');
    assert.equal(deleting.auth.renewals, 1);
    assert.deepEqual(deleting.log().slice(before, before + 2), [`DELETE links/Hall_Temperature/${TEMPERATURE}`, `DELETE links/Hall_Temperature/${TEMPERATURE}`]);
    assert.equal(deleting.fetch.calls.length - before, 7);
    for (const oh of [once, check, twice, deleting]) {
        assertOnlyFixedPointEight(oh);
    }
});

test('plan: a view whose progress callback throws ends no run', async () => {
    const oh = openhab();
    const result = await runPlan(plan(), oh.core, () => {
        throw new Error('view gone');
    });
    assert.equal(result.status, 'done');
    assert.deepEqual(ids(result.created), ALL);
});

test('plan: the stand-in openHAB fails a call to any address outside fixed point 8', async () => {
    const oh = openhab();
    for (const [method, path] of [
        ['GET', 'knx/bridges'],
        ['PUT', `things/${encodeURIComponent(T)}`],
        ['POST', 'items/x'],
        ['DELETE', 'things'],
        ['GET', 'items/x?recursive=true'],
        ['GET', 'thing-types'],
        ['PUT', 'links/a/b/c'],
    ]) {
        await assert.rejects(oh.fetch(new URL(path, BASE).href, { method }));
    }
    await assert.rejects(oh.fetch('http://other.invalid/oh/rest/items/x', { method: 'GET' }));
    assert.equal(oh.strays.length, 8);
});
