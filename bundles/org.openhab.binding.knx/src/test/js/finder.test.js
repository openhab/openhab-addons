import { test } from 'node:test';
import assert from 'node:assert/strict';

import { FIND_RESULTS, createFinder, findIndex } from '../web/js/finder.js';
import { createRows } from '../web/js/rows.js';
import { createWorkplace } from '../web/js/workplace.js';
import { createDocument } from './fakedom.js';
import { settle } from './fakes.js';

const ADDRESSES = [
    { address: '31/0/1', name: 'Kitchen <b>light</b>' },
    { address: '31/0/2', name: 'Hall light' },
    { address: '31/7/9', name: 'Outside temperature' },
];
const DETAILS = [
    {
        id: 'P-31_D-1',
        address: '31.1.5',
        name: 'Switch actuator',
        groupObjects: [
            { number: 7, name: 'Status', function: 'Feedback', groupAddresses: ['31/0/2'] },
            { number: 2, name: 'Channel A', function: 'Switch', groupAddresses: ['31/0/1'] },
            {},
        ],
    },
    { id: 'P-31_D-2', name: 'Kitchen sensor', groupObjects: [] },
];
const SPACES = [{ id: 'P-31_B-1', name: 'House', spaces: [{ id: 'P-31_B-2', name: 'Kitchen', spaces: [] }, { name: 'No id' }] }];

function open({ load = async () => ({ addresses: ADDRESSES, details: DETAILS, spaces: SPACES }) } = {}) {
    const document = createDocument();
    const picked = [];
    let loads = 0;
    const finder = createFinder({
        document,
        load: () => {
            loads++;
            return load();
        },
        onPick: (found) => picked.push(found),
    });
    const type = async (words) => {
        finder.field.value = words;
        finder.field.fire('input');
        await settle();
    };
    const results = () => finder.node.querySelectorAll('.found').map((button) => [button.querySelector('.kind').textContent, button.querySelector('.name').textContent]);
    return { document, finder, picked, type, results, loads: () => loads };
}

test('finder: the index holds every group address, device, group object and room with the place it opens', () => {
    const index = findIndex(ADDRESSES, DETAILS, SPACES);
    assert.deepEqual(index.map((entry) => [entry.kind, entry.label, entry.view, entry.sel, entry.row]), [
        ['address', '31/0/1 Kitchen <b>light</b>', 'group-addresses', 'ga:31/0/1', undefined],
        ['address', '31/0/2 Hall light', 'group-addresses', 'ga:31/0/2', undefined],
        ['address', '31/7/9 Outside temperature', 'group-addresses', 'ga:31/7/9', undefined],
        ['device', '31.1.5 Switch actuator', 'topology', 'device:P-31_D-1', undefined],
        // the row of an object is its place in the device's list as the page orders it: by number
        ['object', '31.1.5 Switch actuator · 2 Channel A Switch', 'topology', 'device:P-31_D-1', 'object:0'],
        ['object', '31.1.5 Switch actuator · 7 Status Feedback', 'topology', 'device:P-31_D-1', 'object:1'],
        ['device', 'Kitchen sensor', 'topology', 'device:P-31_D-2', undefined],
        ['room', 'House', 'buildings', 'space:P-31_B-1', undefined],
        ['room', 'Kitchen', 'buildings', 'space:P-31_B-2', undefined],
    ]);
    assert.deepEqual(findIndex(null, undefined, 'x'), []);
});

test('finder: one box finds by number or text, in any case; the lists are read once, when the box is first used', async () => {
    const f = open();
    assert.equal(f.finder.field.getAttribute('type'), 'search');
    assert.equal(f.loads(), 0);
    await f.type('KITCHEN');
    assert.equal(f.loads(), 1);
    assert.deepEqual(f.results(), [
        ['Group address', '31/0/1 Kitchen <b>light</b>'],
        ['Device', 'Kitchen sensor'],
        ['Room', 'Kitchen'],
    ]);
    assert.ok(f.finder.node.querySelectorAll('.name').every((name) => name.children.length === 0), 'names are text');
    await f.type('31.1.5');
    assert.deepEqual(f.results().map(([kind]) => kind), ['Device', 'Group object', 'Group object']);
    await f.type('31/7');
    assert.deepEqual(f.results(), [['Group address', '31/7/9 Outside temperature']]);
    await f.type(' feedback ');
    assert.deepEqual(f.results(), [['Group object', '31.1.5 Switch actuator · 7 Status Feedback']]);
    await f.type('nothing like this');
    assert.equal(f.finder.node.querySelector('.find-results').textContent, 'Nothing in the project matches.');
    await f.type('');
    assert.equal(f.finder.node.querySelector('.find-results').textContent, '');
    assert.equal(f.loads(), 1);
});

test('finder: a click on a result tells which thing it is and where it opens', async () => {
    const f = open();
    await f.type('status');
    f.finder.node.querySelector('.found').querySelector('.name').fire('click');
    assert.deepEqual(f.picked.map((found) => [found.view, found.sel, found.row]), [['topology', 'device:P-31_D-1', 'object:1']]);
    await f.type('house');
    f.finder.node.querySelector('.found').click();
    assert.deepEqual(f.picked.at(-1).sel, 'space:P-31_B-1');
});

test('finder: at most 50 results are shown, with the number of the others; a failed read says so and is tried again', async () => {
    const many = Array.from({ length: 70 }, (_, n) => ({ address: `31/1/${n}`, name: `Lamp ${n}` }));
    const big = open({ load: async () => ({ addresses: many, details: [], spaces: [] }) });
    await big.type('lamp');
    assert.equal(FIND_RESULTS, 50);
    assert.equal(big.results().length, 50);
    assert.equal(big.finder.node.querySelector('.find-results').querySelector('p').textContent, '20 more match; type more to narrow it.');

    let fail = true;
    const broken = open({
        load: async () => {
            if (fail) {
                throw new Error('no project');
            }
            return { addresses: ADDRESSES, details: [], spaces: [] };
        },
    });
    await broken.type('hall');
    assert.equal(broken.finder.node.querySelector('.find-results').textContent, 'The project could not be read.');
    fail = false;
    await broken.type('hall ');
    assert.deepEqual(broken.results(), [['Group address', '31/0/2 Hall light']]);
    assert.equal(broken.loads(), 2);
});

test('finder: an index with a part missing says so above the results, and the lists are read again when the box is next entered', async () => {
    let incomplete = true;
    const f = open({ load: async () => ({ addresses: ADDRESSES, details: incomplete ? [] : DETAILS, spaces: SPACES, incomplete }) });
    await f.type('hall');
    const box = f.finder.node.querySelector('.find-results');
    assert.equal(box.querySelector('.incomplete').textContent, 'A part of the project could not be read, so results may be missing. It is read again when you next use this box.');
    assert.deepEqual(f.results(), [['Group address', '31/0/2 Hall light']]);
    await f.type('hall ');
    assert.equal(f.loads(), 1, 'not with every letter');
    incomplete = false;
    f.finder.field.fire('focus');
    await f.type('channel a');
    assert.equal(f.loads(), 2);
    assert.equal(box.querySelector('.incomplete'), null);
    assert.equal(f.results().length, 1);
    f.finder.field.fire('focus');
    await f.type('channel');
    assert.equal(f.loads(), 2, 'a whole index is kept');
});

test('finder: close() leaves no listener, and an answer that comes after it draws nothing', async () => {
    let answer = null;
    const f = open({ load: () => new Promise((resolve) => (answer = resolve)) });
    f.finder.field.value = 'hall';
    f.finder.field.fire('input');
    await settle();
    assert.equal(f.finder.node.querySelector('.find-results').textContent, 'Loading…');
    f.finder.close();
    answer({ addresses: ADDRESSES, details: [], spaces: [] });
    await settle();
    assert.deepEqual(f.results(), []);
    assert.equal(f.document.listenerCount(), 0);
});

test('finder: in the workplace Find reads the project through the row model and call 15, and a result goes to its panel', async () => {
    const document = createDocument();
    const element = document.createElement('section');
    const api = {
        calls: [],
        async allGroupAddresses() {
            api.calls.push('groupAddresses');
            return ADDRESSES;
        },
        async allDevices() {
            api.calls.push('devices');
            return DETAILS.map(({ groupObjects, ...device }) => device);
        },
        async device(bridge, id) {
            api.calls.push(`device ${id}`);
            return DETAILS.find((device) => device.id === id);
        },
        async locations() {
            api.calls.push('locations');
            return SPACES;
        },
    };
    const bridge = { uid: 'knx:ip:line31', present: true };
    const navigations = [];
    const context = { api, bridge, rows: createRows({ bridge: bridge.uid, api }), route: { view: 'monitor', bridge: bridge.uid, query: {} }, navigate: (route) => navigations.push(route) };
    const made = createWorkplace({ document, element, title: 'Group Monitor', tree: false, context });
    const box = element.querySelector('.container-find');
    assert.equal(box.querySelector('h3').textContent, 'Find');
    const field = box.querySelector('input');
    assert.deepEqual(api.calls, [], 'nothing is read before the box is used');
    field.value = 'channel a';
    field.fire('input');
    await settle();
    assert.deepEqual([...api.calls].sort(), ['device P-31_D-1', 'device P-31_D-2', 'devices', 'groupAddresses', 'locations']);
    box.querySelector('.found').click();
    assert.deepEqual(navigations, [{ view: 'topology', bridge: 'knx:ip:line31', query: { sel: 'device:P-31_D-1', row: 'object:0' } }]);
    field.value = 'kitchen';
    field.fire('input');
    await settle();
    box.querySelectorAll('.found')[2].click();
    assert.deepEqual(navigations.at(-1), { view: 'buildings', bridge: 'knx:ip:line31', query: { sel: 'space:P-31_B-2' } });
    box.querySelectorAll('.found')[0].click();
    assert.deepEqual(navigations.at(-1), { view: 'group-addresses', bridge: 'knx:ip:line31', query: { sel: 'ga:31/0/1' } });
    made.close();
    assert.equal(document.listenerCount(), 0);

    // without a bridge's row model there is nothing to look through
    const bare = document.createElement('section');
    createWorkplace({ document, element: bare, title: 'Topology' });
    assert.equal(bare.querySelector('.container-find').querySelector('input'), null);
    assert.equal(bare.querySelector('.container-find').querySelector('.note').textContent, 'Find needs a bridge with a project.');
});
