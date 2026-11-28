// What the wizard's test files share: a made-up proposal with its checks, fakes of the calls and of openHAB's own
// addresses, and the ways to reach a row, a cell or a node of the plan.

import assert from 'node:assert/strict';

import { createLayout } from '../web/js/layout.js';
import { CHECK_WAIT, show } from '../web/js/views/wizard.js';
import { createFetch, empty, settle } from './fakes.js';
import { viewContext } from './fakeview.js';

export const BRIDGE = { uid: 'knx:ip:made-up', label: 'Line 7', present: true, status: 'ONLINE', bufferSize: 2000, projectState: 'LOADED' };
export const BASE = new URL('http://openhab.invalid/oh/rest/');
export const THING = 'knx:device:made-up:Kitchen';
export const LIGHT = `${THING}:Light`;
export const CORE_PATH = /^\/oh\/rest\/(things|items|links)(\/|$)/;

export const LOCATIONS = [
    {
        id: 'P-7_B-1',
        name: 'House',
        type: 'Building',
        spaces: [
            {
                id: 'P-7_B-2',
                name: 'Kitchen',
                type: 'Room',
                spaces: [],
                devices: [],
                functions: [{ id: 'P-7_F-1', name: 'Ceiling light', type: 'SwitchableLight', groupAddresses: [] }],
            },
        ],
        devices: [],
        functions: [],
    },
];
export const DEVICES = [
    { id: 'P-7_D-2', address: '7.7.2', name: 'Hall sensor', objects: 1, linkedObjects: 1 },
    { id: 'P-7_D-1', address: '7.7.1', name: 'Kitchen actuator', objects: 2, linkedObjects: 2 },
];
export const ADDRESSES = [
    { address: '31/7/1', name: 'Kitchen light', dataType: '1.001' },
    { address: '31/7/2', name: 'Kitchen light status', dataType: '1.001' },
    { address: '31/7/3', name: '<i>x</i>', dataType: '9.001' },
];

export const LOCATION_TAGS = ['Building', 'Floor', 'Room', 'Corridor', 'Outdoor'];
/** The two location groups of the made-up proposal as call 22 gets them while both are ticked. */
export const HOUSE_GROUPS = [
    { groupName: 'gHouse', label: 'House', tag: 'Building', category: 'house' },
    { groupName: 'gKitchen', label: 'Kitchen', tag: 'Room', category: 'group', parent: 'gHouse' },
];

/** The Things as call 22 gets them while the location groups are on: the Items of the Thing in the Kitchen are in its group. */
export function inKitchen(things, group = 'gKitchen') {
    for (const channel of things[0].channels) {
        if (channel.item) {
            channel.item.groups = [group];
        }
    }
    return things;
}

export function part(key, dataType, address, more = {}) {
    return {
        key,
        dataType,
        groupAddress: address,
        groupAddressName: `Name of ${address}`,
        secured: false,
        read: false,
        statusRead: false,
        pairing: 'NONE',
        candidates: [],
        text: `${dataType}:${address}`,
        ...more,
    };
}

export function item(name, type, more = {}) {
    return { name, type, allowedTypes: [type], label: name, tags: [], groups: [], ...more };
}

export function proposal() {
    return {
        things: [
            {
                id: 'Kitchen',
                uid: THING,
                label: '<i>x</i> kitchen',
                source: 'FUNCTION',
                sourceId: 'P-7_F-1',
                roomId: 'P-7_B-2',
                room: 'Kitchen',
                channels: [
                    {
                        id: 'Light',
                        channelType: 'switch',
                        allowedChannelTypes: ['switch'],
                        label: 'Light',
                        selected: true,
                        statusOnly: false,
                        combined: false,
                        parts: [
                            part('ga', '1.001', '31/7/1', {
                                statusAddress: '31/7/2',
                                statusName: 'Kitchen light status',
                                statusRead: true,
                                pairing: 'OBJECTS',
                                candidates: [
                                    { address: '31/7/2', name: 'Kitchen light status' },
                                    { address: '31/7/4', name: 'Other status' },
                                ],
                                text: '1.001:31/7/1+<31/7/2',
                            }),
                        ],
                        item: item('Kitchen_Light', 'Switch'),
                        alreadyConfigured: [],
                        notes: [],
                    },
                    {
                        id: 'Dimmer',
                        channelType: 'dimmer',
                        allowedChannelTypes: ['dimmer'],
                        label: 'Dimmer',
                        selected: true,
                        statusOnly: false,
                        combined: true,
                        parts: [part('switch', '1.001', '31/7/5'), part('position', '5.001', '31/7/6')],
                        item: item('Kitchen_Dimmer', 'Dimmer'),
                        split: [
                            {
                                id: 'Dimmer_switch',
                                channelType: 'switch',
                                allowedChannelTypes: ['switch'],
                                label: 'Dimmer switch',
                                selected: true,
                                combined: false,
                                parts: [part('ga', '1.001', '31/7/5')],
                                item: item('Kitchen_Dimmer_switch', 'Switch'),
                                alreadyConfigured: [],
                                notes: [],
                            },
                            {
                                id: 'Dimmer_position',
                                channelType: 'number',
                                allowedChannelTypes: ['number'],
                                label: 'Dimmer position',
                                selected: true,
                                combined: false,
                                parts: [part('ga', '5.001', '31/7/6')],
                                item: item('Kitchen_Dimmer_position', 'Number:Dimensionless', { allowedTypes: ['Number:Dimensionless', 'Number'] }),
                                alreadyConfigured: [],
                                notes: [],
                            },
                        ],
                        alreadyConfigured: ['knx:device:made-up:Old:dim'],
                        notes: ['A made-up note of the mapper'],
                    },
                ],
            },
        ],
        rooms: [
            { id: 'P-7_B-1', name: 'House', groupName: 'gHouse', label: 'House', type: 'Building', tag: 'Building', allowedTags: LOCATION_TAGS, category: 'house' },
            { id: 'P-7_B-2', name: 'Kitchen', groupName: 'gKitchen', label: 'Kitchen', type: 'Room', parentId: 'P-7_B-1', parent: 'gHouse', tag: 'Room', allowedTags: LOCATION_TAGS, category: 'group' },
        ],
        unknown: [],
        leftOut: [{ source: 'DEVICE', sourceId: 'P-7_DI-9' }],
        readAtStart: true,
        thingsWithoutHandler: 1,
        deviceThings: [
            { uid: 'knx:device:made-up:Old', label: 'Old <b>actuator</b>', channelIds: ['dim', 'Light'] },
            { uid: 'knx:device:made-up:Wall', channelIds: [] },
        ],
    };
}

/** The lines call 22 adds when the Thing of the made-up proposal has the Thing Old as its target. */
export function linesPreview() {
    return {
        ...goodPreview(),
        lines: {
            thingsText: '// Channels of "<i>x</i> kitchen" for the Thing knx:device:made-up:Old: paste them before its closing brace\n    // the Thing has a channel Light already: rename this one\n    Type switch : Light "Light" [ ga="1.001:31/7/1+<31/7/2" ]\n',
            itemsText: 'Switch Kitchen_Light "Light" { channel="knx:device:made-up:Old:Light" }\n',
            existingChannels: [{ target: 'knx:device:made-up:Old', channelIds: ['Light', 'Dimmer'] }],
            withoutTarget: 0,
        },
    };
}

/** Steps as call 22 gives them: one group, one Thing with one channel, one Item, one link. */
export function steps() {
    return [
        { kind: 'GROUP', itemName: 'gKitchen', item: { type: 'Group', name: 'gKitchen', label: 'Kitchen', category: 'group', tags: ['Room'], groupNames: [] } },
        {
            kind: 'THING',
            uid: THING,
            thing: {
                UID: THING,
                thingTypeUID: 'knx:device',
                bridgeUID: BRIDGE.uid,
                label: 'Kitchen',
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
                ],
            },
        },
        { kind: 'ITEM', itemName: 'Kitchen_Light', item: { type: 'Switch', name: 'Kitchen_Light', label: 'Light', tags: [], groupNames: ['gKitchen'] } },
        { kind: 'LINK', itemName: 'Kitchen_Light', channelUID: LIGHT, link: { itemName: 'Kitchen_Light', channelUID: LIGHT, configuration: {} } },
    ];
}

export function goodPreview() {
    return {
        thingsText: 'Thing knx:device:made-up:Kitchen "<i>x</i> kitchen" (knx:ip:made-up) {\n}\n',
        itemsText: 'Switch Kitchen_Light "Light" { channel="knx:device:made-up:Kitchen:Light" }\n',
        problems: [{ severity: 'WARNING', code: 'already-configured', where: 'things/0/channels/1', text: '31/7/5 is used by another channel of this bridge.' }],
        readAddresses: ['31/7/2', '31/7/6'],
        readTelegrams: 3,
        counts: { things: 1, channels: 2, items: 2, links: 2, groups: 1 },
        steps: steps(),
    };
}

export function badPreview() {
    return {
        problems: [
            { severity: 'WARNING', code: 'status-only', where: 'things/0/channels/0', text: 'A made-up warning.' },
            { severity: 'ERROR', code: 'bad-item-name', where: 'things/0/channels/0/item/name', text: 'A made-up error.' },
            { severity: 'ERROR', code: 'item-exists', where: 'roomGroups/1/groupName', text: 'gKitchen exists.' },
        ],
        readAddresses: ['31/7/2'],
        readTelegrams: 1,
    };
}

export function fakeApi({ previews = [goodPreview()], proposed = proposal(), devices = DEVICES } = {}) {
    const api = {
        calls: [],
        async locations(bridge) {
            api.calls.push(['locations', bridge]);
            return LOCATIONS;
        },
        async allDevices(bridge) {
            api.calls.push(['allDevices', bridge]);
            return devices;
        },
        async allGroupAddresses(bridge) {
            api.calls.push(['allGroupAddresses', bridge]);
            return ADDRESSES;
        },
        async proposal(bridge, request) {
            api.calls.push(['proposal', bridge, JSON.parse(JSON.stringify(request))]);
            if (proposed instanceof Error) {
                throw proposed;
            }
            return proposed;
        },
        async preview(bridge, request) {
            api.calls.push(['preview', bridge, JSON.parse(JSON.stringify(request))]);
            const next = previews.length > 1 ? previews.shift() : previews[0];
            return JSON.parse(JSON.stringify(next));
        },
        requests: (name) => api.calls.filter((call) => call[0] === name).map((call) => call[2]),
    };
    return api;
}

/** openHAB's own addresses as a fake: nothing exists, every create works; any other address fails the test. */
export function fakeCore(answer) {
    const strays = [];
    const fetch = createFetch((call) => {
        if (!CORE_PATH.test(call.url.pathname)) {
            strays.push(call.url.pathname);
            return empty(500);
        }
        if (answer) {
            return answer(call);
        }
        if (call.method === 'GET') {
            return empty(404);
        }
        if (call.method === 'DELETE') {
            return empty(call.url.pathname.startsWith('/oh/rest/things/') ? 202 : 200);
        }
        return empty(call.url.pathname.startsWith('/oh/rest/links/') ? 200 : 201);
    });
    fetch.strays = strays;
    return fetch;
}

export function fakeAuth() {
    return { token: async () => 'made-up-token', renew: async () => 'made-up-token', lost() {} };
}

/** @param {boolean} [options.answer] what the question boxes answer, from the first one on */
export async function open({ api = fakeApi(), fetch = fakeCore(), query = {}, keep, tab, rows, bridge = BRIDGE, answer = true } = {}) {
    const h = viewContext({ bridge, api, rows, route: { view: 'wizard', bridge: bridge.uid, query } });
    h.answer = answer;
    h.context.fetch = fetch;
    h.context.base = BASE;
    h.context.auth = fakeAuth();
    h.context.keep = keep;
    h.context.tab = tab ?? null;
    const view = show(h.context);
    await settle();
    return { h, api, fetch, view, root: h.element };
}

export function only(root, selector) {
    const found = root.querySelectorAll(selector);
    assert.equal(found.length, 1, selector);
    return found[0];
}

/** {@return the tick box of a list of the first screen whose label holds the words} */
export function pick(root, kind, words) {
    const found = root.querySelectorAll(`.pick-${kind}`).filter((box) => box.parentNode.textContent.includes(words));
    assert.equal(found.length, 1, `${kind} ${words}`);
    return found[0];
}

export function type(field, value) {
    field.value = value;
    field.fire('change');
}

export async function click(node) {
    node.click();
    await settle();
}

/** The store of the browser tab as main.js makes it: it outlives a reload of the page, which tabStore(text) stands for. */
export function tabStore(text = { value: null }) {
    const store = createLayout({ read: () => text.value, write: (value) => (text.value = value) });
    store.text = text;
    return store;
}

/** The plan screen with the first check answered. */
export async function proposed(options) {
    const opened = await open(options);
    await click(pick(opened.root, 'address', '31/7/3'));
    await click(only(opened.root, '.propose'));
    return opened;
}

/** Lets the wait after a change pass, so call 22 is asked and answered. */
export async function checked(opened) {
    await opened.h.clock.advance(CHECK_WAIT);
    await settle();
}

export function createButton(root) {
    return only(root, '.create');
}

export async function confirmed(opened) {
    await click(createButton(opened.root));
    await click(only(opened.root, '.confirm-create'));
}

export function bodyRows(root) {
    return root.querySelector('tbody').querySelectorAll('tr');
}

export function row(root, words) {
    const found = bodyRows(root).filter((line) => !line.classList.contains('group-row') && line.textContent.includes(words));
    assert.equal(found.length, 1, words);
    return found[0];
}

export function cellOf(root, words, column) {
    return only(row(root, words), `.col-${column}`);
}

export function tab(root, words) {
    const found = root.querySelectorAll('.list-tab').filter((node) => node.textContent.startsWith(words));
    assert.equal(found.length, 1, words);
    return found[0];
}

/** Opens the editor of a cell by a double click and gives it; fails when the cell has none. */
export function editorOf(root, words, column) {
    only(cellOf(root, words, column), '.can-edit').fire('dblclick');
    return only(root, '.cell-editor');
}

export function editCell(root, words, column, value) {
    const editor = editorOf(root, words, column);
    editor.value = value;
    editor.fire('keydown', { key: 'Enter' });
}

export function treeItems(root) {
    for (let turns = 0; turns < 10; turns++) {
        const closedItems = root.querySelectorAll('li').filter((node) => node.getAttribute('role') === 'treeitem' && node.getAttribute('aria-expanded') === 'false');
        if (closedItems.length === 0) {
            break;
        }
        closedItems[0].querySelector('.twisty').click();
    }
    return root.querySelectorAll('li').filter((node) => node.getAttribute('role') === 'treeitem');
}

export function treeLine(item) {
    const line = item.querySelector('.node');
    return [Number(item.getAttribute('aria-level')), line.querySelector('.name').textContent, line.querySelector('.kind')?.textContent ?? '', line.querySelector('.badge').getAttribute('title'), item.classList.contains('left-out')];
}

export function treeNode(root, words) {
    const found = treeItems(root).filter((item) => item.querySelector('.node').textContent.includes(words));
    assert.equal(found.length, 1, words);
    return found[0].querySelector('.name');
}

export function lastPreview(api) {
    return api.requests('preview').at(-1);
}

export function morePreview() {
    return { ...goodPreview(), readAddresses: ['31/7/2', '31/7/4', '31/7/6'], readTelegrams: 9 };
}
