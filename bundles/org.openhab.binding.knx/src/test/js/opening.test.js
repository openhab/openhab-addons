// Slice Q: how a tree stands when a panel is first opened, what is kept of the viewer's opening and closing, and the
// tools of the tree's heading. Made-up project: a building with a part, floors, rooms and a board in a room.

import { test } from 'node:test';
import assert from 'node:assert/strict';

import { createLayout } from '../web/js/layout.js';
import { createPanel } from '../web/js/panel.js';
import { EXPAND_ROWS, KEPT_NODES, KEPT_TREES, createTree, treeMemory } from '../web/js/tree.js';
import { show } from '../web/js/views/buildings.js';
import { createDocument, textNodes } from './fakedom.js';
import { settle } from './fakes.js';
import { viewContext } from './fakeview.js';

const BRIDGE = { uid: 'knx:ip:line31', label: 'Line 31', present: true, status: 'ONLINE', bufferSize: 2000, projectState: 'LOADED' };

function device(id, address, name, room) {
    return { id, address, name, room, objects: 1, linkedObjects: 1 };
}

const TOPOLOGY = {
    areas: [
        {
            address: '31',
            name: 'Main',
            lines: [
                {
                    address: '31.1',
                    name: 'House line',
                    devices: [device('D-1', '31.1.1', 'Kitchen actuator', 'Kitchen'), device('D-2', '31.1.2', 'Board supply', 'Board A'), device('D-3', '31.1.3', 'Bedroom sensor', 'Bedroom'), device('D-4', '31.1.4', 'Spare dimmer')],
                },
            ],
        },
    ],
    unplacedDevices: [],
};

function space(id, name, type, more = {}) {
    return { id, name, type, spaces: [], devices: [], functions: [], ...more };
}

export const LOCATIONS = [
    space('B-1', 'House', 'Building', {
        spaces: [
            space('B-2', 'West wing', 'BuildingPart', {
                spaces: [
                    space('B-3', 'Ground floor', 'Floor', {
                        number: '0',
                        spaces: [
                            space('B-4', 'Kitchen', 'Room', { number: '0.1', devices: ['D-1'], functions: [{ id: 'F-1', name: 'Ceiling light', type: 'SwitchableLight', groupAddresses: [{ groupAddress: '31/0/1', name: 'Kitchen light' }] }] }),
                            space('B-5', 'Utility room', 'Room', { spaces: [space('B-6', 'Board A', 'DistributionBoard', { devices: ['D-2'] })] }),
                        ],
                    }),
                    space('B-7', 'First floor', 'Floor', { number: '1', spaces: [space('B-8', 'Bedroom', 'Room', { devices: ['D-3'] })] }),
                    space('B-11', 'Second floor', 'Floor', { number: '2', spaces: [space('B-12', 'Study', 'Room')] }),
                    space('B-9', 'Stairs', 'Stairway'),
                ],
            }),
            space('B-10', 'Garden', 'Area'),
        ],
    }),
];

function fakeApi() {
    return {
        topology: async () => TOPOLOGY,
        locations: async () => LOCATIONS,
        device: async (bridge, id) => ({ ...TOPOLOGY.areas[0].lines[0].devices.find((known) => known.id === id), groupObjects: [] }),
        deviceChannels: async () => [],
    };
}

function memoryLayout() {
    const kept = new Map();
    return { kept, layout: createLayout({ read: () => kept.get('layout') ?? null, write: (value) => kept.set('layout', value) }) };
}

async function open({ sel, layout } = {}) {
    const harness = viewContext({ bridge: BRIDGE, api: fakeApi(), layout, route: { view: 'buildings', bridge: BRIDGE.uid, query: sel ? { sel } : {} } });
    const view = show(harness.context);
    await settle();
    return Object.assign(harness, { view });
}

function names(element) {
    return element.querySelector('.tree').querySelectorAll('li').map((item) => item.querySelector('.name').textContent);
}

function item(element, name) {
    return element.querySelector('.tree').querySelectorAll('li').find((candidate) => candidate.querySelector('.name').textContent === name);
}

const FIRST_VIEW = ['Buildings', 'House', 'West wing', 'Ground floor', 'Kitchen', 'Utility room', 'Board A', 'First floor', 'Bedroom', 'Second floor', 'Study', 'Stairs', 'Garden', 'Dynamic Folders'];

test('opening: Buildings shows the whole structure down to the rooms and a board in a room at once; what a room holds is closed, the dynamic folders stand last and closed', async () => {
    const b = await open();
    assert.deepEqual(names(b.element), FIRST_VIEW);
    assert.equal(item(b.element, 'Kitchen').getAttribute('aria-expanded'), 'false', 'a room with devices and functions is closed');
    assert.equal(item(b.element, 'Utility room').getAttribute('aria-expanded'), 'true', 'a room that holds a board is open');
    assert.equal(item(b.element, 'Board A').getAttribute('aria-expanded'), 'false');
    assert.equal(item(b.element, 'Dynamic Folders').getAttribute('aria-expanded'), 'false');
    assert.equal(item(b.element, 'Stairs').getAttribute('aria-expanded'), null);
});

test('opening: with nothing selected Buildings selects its first node, and the list is the Devices tab with every device and its room', async () => {
    const b = await open();
    assert.equal(item(b.element, 'Buildings').getAttribute('aria-selected'), 'true');
    const tabs = b.element.querySelector('.list-tabs').querySelectorAll('button').map((tab) => [tab.textContent, tab.getAttribute('aria-selected')]);
    assert.deepEqual(tabs[0], ['Devices (4)', 'true']);
    const rows = b.element.querySelector('.list').querySelector('tbody').children.map((row) => row.children.slice(0, 3).map((cell) => cell.textContent));
    assert.deepEqual(rows, [
        ['31.1.1', 'Kitchen', 'Kitchen actuator'],
        ['31.1.2', 'Board A', 'Board supply'],
        ['31.1.3', 'Bedroom', 'Bedroom sensor'],
        ['31.1.4', '', 'Spare dimmer'],
    ]);
});

test('opening: a room\'s content stands as sub-spaces, then devices by address, then functions', async () => {
    const b = await open();
    item(b.element, 'Kitchen').querySelector('.twisty').fire('click');
    const at = names(b.element).indexOf('Kitchen');
    assert.deepEqual(names(b.element).slice(at, at + 3), ['Kitchen', 'Kitchen actuator', 'Ceiling light']);
});

test('opening: what the viewer opens and closes in Buildings is kept and comes back; a selection of the address still has its path opened', async () => {
    const { kept, layout } = memoryLayout();
    const b = await open({ layout });
    assert.equal(kept.size, 0);
    item(b.element, 'Ground floor').querySelector('.twisty').fire('click');
    item(b.element, 'Bedroom').querySelector('.twisty').fire('click');
    assert.deepEqual(JSON.parse(kept.get('layout')).trees, { 'buildings|knx:ip:line31': { open: ['space:B-8'], closed: ['space:B-3'] } });
    b.view.close();
    const again = await open({ layout });
    assert.deepEqual(names(again.element), ['Buildings', 'House', 'West wing', 'Ground floor', 'First floor', 'Bedroom', 'Bedroom sensor', 'Second floor', 'Study', 'Stairs', 'Garden', 'Dynamic Folders']);
    again.view.close();
    const named = await open({ layout, sel: 'space:B-6' });
    assert.equal(item(named.element, 'Board A').getAttribute('aria-selected'), 'true');
    assert.equal(item(named.element, 'Ground floor').getAttribute('aria-expanded'), 'true');
});

test('opening: the tree\'s heading has Expand all and Collapse all, each a drawing with a title', async () => {
    const b = await open();
    const head = b.element.querySelector('.tree-head');
    const [expand, collapse] = head.querySelectorAll('button');
    assert.deepEqual([expand.getAttribute('title'), collapse.getAttribute('title')], ['Expand all', 'Collapse all']);
    assert.deepEqual([expand.getAttribute('aria-label'), collapse.getAttribute('aria-label')], ['Expand all', 'Collapse all']);
    assert.ok(expand.querySelector('.icon-expand'));
    assert.ok(collapse.querySelector('.icon-collapse'));
    collapse.click();
    assert.deepEqual(names(b.element), ['Buildings', 'Dynamic Folders']);
    assert.equal(item(b.element, 'Buildings').getAttribute('tabindex'), '0', 'the keyboard stays on a node that is in view');
    expand.click();
    assert.deepEqual(names(b.element).slice(0, 8), ['Buildings', 'House', 'West wing', 'Ground floor', 'Kitchen', 'Kitchen actuator', 'Ceiling light', 'Utility room']);
    assert.ok(names(b.element).includes('Not assigned to a room'));
});

const NODES = [
    { key: 'a', name: 'A', children: [{ key: 'a1', name: 'A1', children: [{ key: 'a1x', name: 'A1x', children: [] }] }] },
    { key: 'b', name: 'B', children: [{ key: 'b1', name: 'B1', children: [] }] },
];

function plainTree(more = {}) {
    const document = createDocument();
    const tree = createTree({ document, label: 'Test', ...more });
    return { document, tree, shown: () => tree.node.querySelectorAll('li').map((entry) => entry.querySelector('.name').textContent) };
}

test('opening: a tree opens the nodes its rule names; a kept state goes before the rule, and a later fill keeps what is open now', () => {
    const { tree, shown } = plainTree({ startsOpen: (node) => node.key === 'a' });
    tree.setRoots(NODES);
    assert.deepEqual(shown(), ['A', 'A1', 'B']);
    const writes = [];
    const kept = plainTree({ startsOpen: (node) => node.key === 'a', memory: { read: () => ({ open: ['b', 'a1'], closed: ['a'] }), write: (state) => writes.push(state) } });
    kept.tree.setRoots(NODES);
    assert.deepEqual(kept.shown(), ['A', 'B', 'B1']);
    assert.deepEqual(writes, [], 'reading writes nothing');
    kept.tree.open('a');
    assert.deepEqual(kept.shown(), ['A', 'A1', 'A1x', 'B', 'B1'], 'a node opened under a closed one is open when that one opens');
    assert.deepEqual(writes, [], 'the page opening a node is not the viewer\'s doing');
    kept.tree.setRoots([NODES[0], { key: 'b', name: 'B', children: [{ key: 'b2', name: 'B2', children: [] }] }]);
    assert.deepEqual(kept.shown(), ['A', 'A1', 'A1x', 'B', 'B2']);
});

test('opening: arrow keys, the arrow of a node, a double click and the two tools are kept; only what differs from the rule is written', () => {
    const writes = [];
    const { tree, shown } = plainTree({ startsOpen: (node) => node.key === 'a', memory: { read: () => null, write: (state) => writes.push(state) } });
    tree.setRoots(NODES);
    const node = (name) => tree.node.querySelectorAll('li').find((entry) => entry.querySelector('.name').textContent === name);
    node('B').fire('keydown', { key: 'ArrowRight' });
    assert.deepEqual(writes.at(-1), { open: ['b'], closed: [] });
    node('B').fire('keydown', { key: 'ArrowLeft' });
    assert.deepEqual(writes.at(-1), { open: [], closed: [] });
    node('A').querySelector('.twisty').fire('click');
    assert.deepEqual(writes.at(-1), { open: [], closed: ['a'] });
    node('A').querySelector('.name').fire('dblclick');
    assert.deepEqual(writes.at(-1), { open: [], closed: [] });
    tree.expandAll();
    assert.deepEqual(shown(), ['A', 'A1', 'A1x', 'B', 'B1']);
    assert.deepEqual(writes.at(-1), { open: ['a1', 'b'], closed: [] });
    tree.collapseAll();
    assert.deepEqual(shown(), ['A', 'B']);
    assert.deepEqual(writes.at(-1), { open: [], closed: ['a'] });
    tree.select('a1x');
    assert.deepEqual(shown(), ['A', 'A1', 'A1x', 'B'], 'a selection opens its path');
});

test('opening: the kept trees are capped in number and in nodes, the tree written last stays; a store that gives rubbish or refuses is no error', () => {
    const { kept, layout } = memoryLayout();
    for (let n = 0; n < KEPT_TREES + 3; n++) {
        treeMemory(layout, `buildings|knx:ip:b${n}`).write({ open: [`space:${n}`], closed: [] });
    }
    const trees = JSON.parse(kept.get('layout')).trees;
    assert.equal(Object.keys(trees).length, KEPT_TREES);
    assert.equal(Object.keys(trees)[0], 'buildings|knx:ip:b3');
    assert.deepEqual(treeMemory(layout, `buildings|knx:ip:b${KEPT_TREES + 2}`).read(), { open: [`space:${KEPT_TREES + 2}`], closed: [] });
    assert.equal(treeMemory(layout, 'buildings|knx:ip:b0').read(), null);
    treeMemory(layout, 'buildings|knx:ip:b3').write({ open: [], closed: [] });
    assert.equal(Object.keys(JSON.parse(kept.get('layout')).trees).at(-1), 'buildings|knx:ip:b3');
    const many = Array.from({ length: KEPT_NODES + 50 }, (unused, n) => `space:${n}`);
    treeMemory(layout, 'big').write({ open: many, closed: many });
    const big = treeMemory(layout, 'big').read();
    assert.deepEqual([big.open.length, big.closed.length], [KEPT_NODES, KEPT_NODES]);

    assert.equal(treeMemory(undefined, 'x'), null);
    const rubbish = createLayout({ read: () => '{"trees":{"x":{"open":"all","closed":[1,"k"]},"y":7}}', write: () => {} });
    assert.deepEqual(treeMemory(rubbish, 'x').read(), { open: [], closed: ['k'] });
    assert.equal(treeMemory(rubbish, 'y').read(), null);
    assert.equal(treeMemory(rubbish, '__proto__').read(), null);
    const refusing = createLayout({
        read: () => {
            throw new Error('blocked');
        },
        write: () => {
            throw new Error('blocked');
        },
    });
    assert.equal(treeMemory(refusing, 'x').read(), null);
    treeMemory(refusing, 'x').write({ open: ['a'], closed: [] });
    assert.deepEqual(treeMemory(refusing, 'x').read(), { open: ['a'], closed: [] }, 'kept for this visit');
});

test('opening: a panel whose tree has no such tools gets no heading', () => {
    const document = createDocument();
    const panel = createPanel({ document, title: 'Plain' });
    panel.setTree({ node: document.createElement('ul'), focus: () => true });
    assert.equal(panel.treePane.querySelector('.tree-head'), null);
    assert.deepEqual(textNodes(panel.treePane), []);
});

/** A made-up project of main groups, each with middle groups of 250 addresses. */
function hugeNodes(mains, middles) {
    return Array.from({ length: mains }, (_, m) => ({
        key: `m${m}`,
        name: `Main ${m}`,
        children: Array.from({ length: middles }, (_, i) => ({
            key: `m${m}/${i}`,
            name: `Middle ${m}/${i}`,
            children: Array.from({ length: 250 }, (_, a) => ({ key: `m${m}/${i}/${a}`, name: `31/${i}/${a}` })),
        })),
    }));
}

test('opening: Expand all on a huge tree opens the outer levels first and stops before more rows than the cap are drawn', () => {
    const { tree } = plainTree();
    // 4 main groups, 40 middle groups, 10,000 addresses
    tree.setRoots(hugeNodes(4, 10));
    assert.equal(tree.expandAll(), false, 'the tree says that not everything is open');
    const drawn = tree.node.querySelectorAll('li');
    assert.ok(drawn.length <= EXPAND_ROWS, `${drawn.length} rows`);
    assert.equal(drawn.filter((entry) => entry.getAttribute('aria-level') === '2').length, 40, 'every middle group is in view');
    assert.ok(drawn.length > 1500, 'and as many addresses as fit');
    // a closed group still opens by itself, and Collapse all closes everything
    const closed = drawn.find((entry) => entry.getAttribute('aria-expanded') === 'false');
    closed.querySelector('.twisty').click();
    assert.equal(closed.getAttribute('aria-expanded'), 'true');
    tree.collapseAll();
    assert.equal(tree.node.querySelectorAll('li').length, 4);
});

test('opening: Expand all on a tree within the cap opens every node and says so', () => {
    const { tree } = plainTree();
    tree.setRoots(hugeNodes(2, 3));
    assert.equal(tree.expandAll(), true);
    assert.equal(tree.node.querySelectorAll('li').length, 2 + 6 + 1500);
    assert.equal(EXPAND_ROWS, 2000);
});

test('opening: the tree pane says when Expand all stopped at the cap; Collapse all and a full Expand all take the line away', () => {
    const document = createDocument();
    const panel = createPanel({ document, title: 'Plain' });
    const tree = createTree({ document, label: 'Plain' });
    tree.setRoots(hugeNodes(4, 10));
    panel.setTree(tree);
    const note = panel.node.querySelector('.tree-note');
    assert.equal(note.textContent, '');
    panel.node.querySelector('.expand-all').click();
    assert.equal(note.textContent, 'Opened as far as the page draws at once. Open the closed groups one by one.');
    panel.node.querySelector('.collapse-all').click();
    assert.equal(note.textContent, '');
    tree.setRoots(hugeNodes(1, 2));
    panel.node.querySelector('.expand-all').click();
    assert.equal(note.textContent, '');
});
