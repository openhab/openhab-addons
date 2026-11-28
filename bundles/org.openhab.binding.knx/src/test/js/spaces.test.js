// Slice Q: the kinds of space are told apart by a drawing and a word, and a space that holds spaces lists them.

import { test } from 'node:test';
import assert from 'node:assert/strict';

import { KnxApiError } from '../web/js/api.js';
import { ICONS } from '../web/js/dom.js';
import { findIndex } from '../web/js/finder.js';
import { SPACE_LOOKS, buildingTree, spaceKind, spaceLook } from '../web/js/model.js';
import { createRows } from '../web/js/rows.js';
import { text } from '../web/js/texts.js';
import { show } from '../web/js/views/buildings.js';
import { textNodes } from './fakedom.js';
import { settle } from './fakes.js';
import { viewContext } from './fakeview.js';

const BRIDGE = { uid: 'knx:ip:line31', label: 'Line 31', present: true, status: 'ONLINE', bufferSize: 2000, projectState: 'LOADED' };
const REMOVED = { uid: 'knx:ip:gone', present: false, projectState: 'LOADED' };

const DEVICES = [
    { id: 'D-1', address: '31.1.1', name: 'Kitchen actuator', room: 'Kitchen', objects: 1, linkedObjects: 1, groupObjects: [{ number: 0, name: 'A', groupAddresses: ['31/0/1'] }] },
    { id: 'D-2', address: '31.1.2', name: 'Board supply', room: 'Board A', objects: 1, linkedObjects: 1, groupObjects: null },
    { id: 'D-3', address: '31.1.3', name: 'Bedroom sensor', room: 'Bedroom', objects: 1, linkedObjects: 1, groupObjects: [{ number: 0, name: 'B', groupAddresses: ['31/0/2'] }] },
];
const TOPOLOGY = { areas: [{ address: '31', name: 'Main', lines: [{ address: '31.1', name: 'House line', devices: DEVICES.map(({ groupObjects, ...summary }) => summary) }] }], unplacedDevices: [] };

function space(id, name, type, more = {}) {
    return { id, name, type, spaces: [], devices: [], functions: [], ...more };
}

const LOCATIONS = [
    space('B-1', 'House', 'Building', {
        spaces: [
            space('B-2', 'West wing', 'BuildingPart', {
                spaces: [
                    space('B-3', 'Ground floor', 'Floor', {
                        number: '0',
                        spaces: [
                            space('B-4', 'Kitchen', 'Room', { number: '0.1', devices: ['D-1'], functions: [{ id: 'F-1', name: 'Ceiling light', type: 'SwitchableLight', groupAddresses: [] }] }),
                            space('B-5', 'Utility room', 'Room', { spaces: [space('B-6', 'Board A', 'DistributionBoard', { devices: ['D-2'] })] }),
                            space('B-11', 'Hall', 'Corridor'),
                        ],
                    }),
                    space('B-7', 'First floor', 'Floor', { number: '1', spaces: [space('B-8', 'Bedroom', 'Room', { devices: ['D-3'] })] }),
                    space('B-9', 'Stairs', 'Stairway'),
                ],
            }),
            space('B-10', 'Garden', 'Area'),
            space('B-12', 'Vault', 'Strongroom'),
        ],
    }),
];

function fakeApi() {
    return {
        topology: async () => TOPOLOGY,
        locations: async () => LOCATIONS,
        allGroupAddresses: async () => [
            { address: '31/0/1', name: 'Kitchen light', configured: true },
            { address: '31/0/2', name: 'Bedroom presence', configured: false },
        ],
        device: async (bridge, id) => {
            const found = DEVICES.find((known) => known.id === id);
            if (found.groupObjects === null) {
                throw new KnxApiError(500, 'internal', 'x');
            }
            return found;
        },
        deviceChannels: async () => [],
        groupAddress: async (bridge, address) => ({ address, usedBy: [] }),
    };
}

async function open({ sel, row, tab, bridge = BRIDGE, withRows = true } = {}) {
    const api = fakeApi();
    const query = { ...(sel ? { sel } : {}), ...(tab ? { tab } : {}), ...(row ? { row } : {}) };
    const harness = viewContext({ bridge, api, rows: withRows ? createRows({ bridge: bridge.uid, api }) : null, route: { view: 'buildings', bridge: bridge.uid, query } });
    const view = show(harness.context);
    await settle();
    harness.runFrames();
    return Object.assign(harness, { view });
}

function item(element, name) {
    return element.querySelector('.tree').querySelectorAll('li').find((candidate) => candidate.querySelector('.name').textContent === name);
}

function tabs(element) {
    return element.querySelector('.list-tabs').querySelectorAll('button').map((tab) => [tab.textContent, tab.getAttribute('aria-selected')]);
}

function headings(element) {
    return element.querySelector('.list').querySelector('thead').querySelectorAll('th').map((cell) => cell.textContent);
}

function rowsOf(element) {
    return element.querySelector('.list').querySelector('tbody').children.map((row) => row.children.map((cell) => cell.textContent));
}

function settings(element) {
    return textNodes(element.querySelector('.properties').querySelector('.tab-settings')).filter((part) => part !== '');
}

test('spaces: each kind of space the project writes has a kind of the page; anything else is "other"', () => {
    const kinds = [
        ['Building', 'building'],
        ['BuildingPart', 'part'],
        ['Floor', 'floor'],
        ['Stairway', 'stairway'],
        ['Corridor', 'corridor'],
        ['Room', 'room'],
        ['DistributionBoard', 'cabinet'],
        ['Area', 'ground'],
        ['Ground', 'ground'],
        ['Segment', 'ground'],
        ['Strongroom', 'other'],
        ['', 'other'],
        [undefined, 'other'],
        ['constructor', 'other'],
    ];
    for (const [type, kind] of kinds) {
        assert.equal(spaceKind(type), kind, String(type));
    }
    assert.equal(spaceKind('ROOM'), 'room');
});

test('spaces: every kind has a drawing of its own and its word in texts.js', () => {
    const looks = Object.values(SPACE_LOOKS);
    assert.equal(looks.length, 9);
    assert.equal(new Set(looks.map((look) => look.icon)).size, 9);
    assert.equal(new Set(looks.map((look) => ICONS[look.icon].join(' '))).size, 9, 'no two kinds share a drawing');
    for (const other of ['device', 'function', 'folder', 'range', 'area', 'line']) {
        assert.ok(!looks.some((look) => ICONS[look.icon].join(' ') === ICONS[other].join(' ')), other);
    }
    assert.deepEqual(looks.map((look) => text(look.word)), ['Building', 'Building part', 'Floor', 'Stairway', 'Corridor', 'Room', 'Distribution board', 'Area', 'Other']);
    assert.deepEqual(spaceLook({ type: 'DistributionBoard' }), { icon: 'cabinet', title: 'Distribution board' });
    assert.deepEqual(spaceLook(undefined), { icon: 'space', title: 'Other' });
});

test('spaces: a node of the building tree carries its kind\'s drawing and word; of the grey text only the number is left', () => {
    const [house] = buildingTree(LOCATIONS, []);
    const flat = [];
    const visit = (node) => {
        if (node.kind === 'space') {
            flat.push([node.name, node.icon, node.iconTitle, node.mark]);
            node.children.forEach(visit);
        }
    };
    visit(house);
    assert.deepEqual(flat, [
        ['House', 'building', 'Building', undefined],
        ['West wing', 'part', 'Building part', undefined],
        ['Ground floor', 'floor', 'Floor', '0'],
        ['Kitchen', 'room', 'Room', '0.1'],
        ['Utility room', 'room', 'Room', undefined],
        ['Board A', 'cabinet', 'Distribution board', undefined],
        ['Hall', 'corridor', 'Corridor', undefined],
        ['First floor', 'floor', 'Floor', '1'],
        ['Bedroom', 'room', 'Room', undefined],
        ['Stairs', 'stairway', 'Stairway', undefined],
        ['Garden', 'ground', 'Area', undefined],
        ['Vault', 'space', 'Other', undefined],
    ]);
});

test('spaces: the tree draws the kind with its word as title; a device and a function keep their own drawings', async () => {
    const b = await open({ sel: 'space:B-4' });
    for (const [name, drawing, word] of [['House', 'building', 'Building'], ['West wing', 'part', 'Building part'], ['Ground floor', 'floor', 'Floor'], ['Board A', 'cabinet', 'Distribution board'], ['Hall', 'corridor', 'Corridor'], ['Stairs', 'stairway', 'Stairway'], ['Garden', 'ground', 'Area'], ['Vault', 'space', 'Other']]) {
        const mark = item(b.element, name).querySelector('.node').querySelector('.kind-icon');
        assert.equal(mark.getAttribute('title'), word, name);
        assert.equal(mark.getAttribute('aria-label'), word, name);
        assert.equal(mark.getAttribute('role'), 'img');
        assert.ok(mark.querySelector(`.icon-${drawing}`), name);
    }
    assert.deepEqual(textNodes(item(b.element, 'Ground floor').querySelector('.node')).filter((part) => part !== ''), ['Ground floor', '0']);
    assert.deepEqual(textNodes(item(b.element, 'West wing').querySelector('.node')).filter((part) => part !== ''), ['West wing']);
    item(b.element, 'Kitchen').querySelector('.twisty').fire('click');
    assert.ok(item(b.element, 'Kitchen actuator').querySelector('.node').querySelector('.icon-device'));
    assert.equal(item(b.element, 'Kitchen actuator').querySelector('.kind-icon'), null);
    assert.ok(item(b.element, 'Ceiling light').querySelector('.node').querySelector('.icon-function'));
});

test('spaces: the path shows each space with its drawing, and Properties name the kind by its word', async () => {
    const b = await open({ sel: 'space:B-6' });
    const crumbs = b.element.querySelector('.crumbs').querySelectorAll('button');
    assert.deepEqual(crumbs.map((crumb) => crumb.textContent), ['Buildings', 'House', 'West wing', 'Ground floor', 'Utility room', 'Board A']);
    assert.deepEqual(crumbs.map((crumb) => crumb.querySelector('.kind-icon')?.getAttribute('title') ?? null), [null, 'Building', 'Building part', 'Floor', 'Room', 'Distribution board']);
    assert.ok(crumbs[5].querySelector('.icon-cabinet'));
    assert.deepEqual(settings(b.element), ['Name', 'Board A', 'Type', 'Distribution board']);
    b.view.update({ view: 'buildings', bridge: BRIDGE.uid, query: { sel: 'space:B-12' } });
    assert.deepEqual(settings(b.element), ['Name', 'Vault', 'Type', 'Other']);
});

test('spaces: a floor lists its rooms first: kind, name, number, the devices and functions of all levels below, and how many of those devices are in openHAB', async () => {
    const b = await open({ sel: 'space:B-3' });
    assert.deepEqual(tabs(b.element), [['Rooms (3)', 'true'], ['Devices (2)', 'false'], ['Group Objects (2)', 'false'], ['Functions (1)', 'false']]);
    assert.deepEqual(headings(b.element), ['Type', 'Name', 'Number', 'Devices', 'Functions', 'In openHAB']);
    assert.deepEqual(rowsOf(b.element), [
        ['Room', 'Kitchen', '0.1', '1', '1', '1'],
        ['Room', 'Utility room', '', '1', '0', '0'],
        ['Corridor', 'Hall', '', '0', '0', '0'],
    ]);
    const first = b.element.querySelector('.list').querySelector('tbody').children[0];
    assert.ok(first.children[0].querySelector('.icon-room'));
    assert.deepEqual(b.statuses.at(-1), { selection: 'Ground floor', shown: 3, total: 3 });
});

test('spaces: a building and a building part list their parts; a room that holds a board too, a room without one starts with its devices', async () => {
    const b = await open({ sel: 'space:B-1' });
    assert.deepEqual(tabs(b.element)[0], ['Parts (3)', 'true']);
    assert.deepEqual(rowsOf(b.element), [
        ['Building part', 'West wing', '', '3', '1', '1'],
        ['Area', 'Garden', '', '0', '0', '0'],
        ['Other', 'Vault', '', '0', '0', '0'],
    ]);
    b.view.update({ view: 'buildings', bridge: BRIDGE.uid, query: { sel: 'space:B-2' } });
    assert.deepEqual(tabs(b.element)[0], ['Parts (3)', 'true']);
    assert.deepEqual(rowsOf(b.element).map((row) => row.slice(0, 4)), [
        ['Floor', 'Ground floor', '0', '2'],
        ['Floor', 'First floor', '1', '1'],
        ['Stairway', 'Stairs', '', '0'],
    ]);
    b.view.update({ view: 'buildings', bridge: BRIDGE.uid, query: { sel: 'space:B-5' } });
    assert.deepEqual(tabs(b.element).slice(0, 2), [['Parts (1)', 'true'], ['Devices (1)', 'false']]);
    b.view.update({ view: 'buildings', bridge: BRIDGE.uid, query: { sel: 'space:B-4' } });
    assert.deepEqual(tabs(b.element), [['Devices (1)', 'true'], ['Group Objects (1)', 'false'], ['Functions (1)', 'false']]);
    assert.equal(headings(b.element)[0], 'Address');
    b.view.update({ view: 'buildings', bridge: BRIDGE.uid, query: {} });
    assert.deepEqual(tabs(b.element)[0], ['Devices (3)', 'true'], 'the first node lists devices');
});

test('spaces: a row of the list only selects, Properties has "Open" and the address names the row; a double click opens the space; the other tabs stay in reach', async () => {
    const b = await open({ sel: 'space:B-3' });
    const body = () => b.element.querySelector('.list').querySelector('tbody');
    body().children[1].children[1].fire('click');
    assert.deepEqual(b.navigations, []);
    assert.deepEqual(b.replacements.at(-1), { view: 'buildings', bridge: BRIDGE.uid, query: { sel: 'space:B-3', row: 'space:B-5' } });
    assert.deepEqual(settings(b.element), ['Name', 'Utility room', 'Type', 'Room']);
    assert.equal(b.element.querySelector('.properties').querySelector('.open').getAttribute('href'), '#/b/knx%3Aip%3Aline31/buildings?sel=space%3AB-5');
    body().children[1].children[1].fire('dblclick');
    assert.deepEqual(b.navigations.at(-1), { view: 'buildings', bridge: BRIDGE.uid, query: { sel: 'space:B-5' } });
    b.view.update({ view: 'buildings', bridge: BRIDGE.uid, query: { sel: 'space:B-3', tab: 'devices' } });
    assert.deepEqual(tabs(b.element).map(([, on]) => on), ['false', 'true', 'false', 'false']);
    assert.deepEqual(rowsOf(b.element).map((row) => row[0]), ['31.1.1', '31.1.2']);
    b.element.querySelector('.list-tabs').querySelectorAll('button')[0].click();
    assert.deepEqual(b.replacements.at(-1), { view: 'buildings', bridge: BRIDGE.uid, query: { sel: 'space:B-3' } }, 'the first tab needs no name in the address');
    const named = await open({ sel: 'space:B-3', row: 'space:B-4' });
    assert.equal(named.element.querySelector('.list').querySelector('tbody').children[0].getAttribute('aria-selected'), 'true');
    assert.deepEqual(settings(named.element).slice(0, 2), ['Name', 'Kitchen']);
});

test('spaces: a removed bridge has no openHAB column, and without the devices\' objects the column is empty, not 0', async () => {
    const removed = await open({ sel: 'space:B-3', bridge: REMOVED });
    assert.deepEqual(headings(removed.element), ['Type', 'Name', 'Number', 'Devices', 'Functions']);
    const waiting = await open({ sel: 'space:B-3', withRows: false });
    assert.deepEqual(rowsOf(waiting.element).map((row) => row[5]), ['', '', '']);
});

test('spaces: Find shows a room with the drawing of its kind', () => {
    const found = findIndex([], [], LOCATIONS);
    assert.deepEqual(found.filter((entry) => entry.kind === 'room').map((entry) => [entry.label, entry.icon]).slice(0, 4), [['House', 'building'], ['West wing', 'part'], ['Ground floor', 'floor'], ['Kitchen', 'room']]);
});
