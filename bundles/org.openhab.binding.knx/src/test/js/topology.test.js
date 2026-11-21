import { test } from 'node:test';
import assert from 'node:assert/strict';

import { KnxApiError } from '../web/js/api.js';
import { createRows } from '../web/js/rows.js';
import { deviceOrder } from '../web/js/model.js';
import { parse } from '../web/js/router.js';
import { USES_ROWS, deviceRoute, projectMissing } from '../web/js/views/explorer.js';
import { USES_AT_ONCE } from '../web/js/views/objects.js';
import { show } from '../web/js/views/topology.js';
import { createDocument, textNodes } from './fakedom.js';
import { settle } from './fakes.js';
import { createLayout } from '../web/js/layout.js';
import { fakeStream, shareSidebar, viewContext } from './fakeview.js';

const BRIDGE = { uid: 'knx:ip:line7', label: 'Line 7', present: true, status: 'ONLINE', bufferSize: 2000, projectState: 'LOADED' };
const OFF = { ...BRIDGE, bufferSize: 0 };
const REMOVED = { uid: 'knx:ip:gone', present: false, projectState: 'LOADED' };

function object(number, name, groupAddresses = [], more = {}) {
    const flags = { read: false, write: false, communication: false, transmit: false, update: false, readOnInit: false };
    const made = { name, groupAddresses, ...flags, ...more };
    if (number !== null) {
        made.number = number;
    }
    return made;
}

const OBJECTS = [
    object(2, 'Switch B', ['31/7/2'], { sendingGroupAddress: '31/7/2', communication: true, write: true, transmit: true, dataType: '1.001', sizeBits: 1 }),
    object(null, 'Nested', ['31/7/5']),
    object(1, 'Alternative', []),
    object(1, 'Switch A', ['31/7/3', '31/7/1'], { sendingGroupAddress: '31/7/1', text: 'Output A', function: 'Switching', sizeBits: 8 }),
    object(3, 'Spare x', []),
    object(3, 'Spare y', []),
    object(4, 'Linked one', ['31/7/4']),
    object(4, 'Linked two', ['31/7/4'], { read: true, update: true, readOnInit: true }),
];

const ACTUATOR = { id: 'P-7_D-1', address: '7.7.1', name: 'Kitchen <b>actuator</b>', product: 'Switch actuator', orderNumber: 'SA-8', room: 'Kitchen', description: 'Below the sink', objects: 8, linkedObjects: 5, filterDevice: false };
const SENSOR = { id: 'P-7_D-2', address: '7.7.12', name: 'Hall sensor', product: 'Presence', orderNumber: 'PS-1', room: 'Hall', objects: 3, linkedObjects: 0, filterDevice: false };
const DUMMY = { id: 'P-7_D-5', address: '7.1.0', name: 'Coupler dummy', objects: 2, linkedObjects: 2, filterDevice: true };
const SPARE = { id: 'P-7_D/9', name: 'Spare part', objects: 0, linkedObjects: 0, filterDevice: false };
const DETAIL = { ...ACTUATOR, groupObjects: OBJECTS };

const TOPOLOGY = {
    areas: [
        {
            address: '7',
            name: 'Main <area>',
            lines: [
                { address: '7.7', name: 'Ground floor', devices: [SENSOR, ACTUATOR] },
                { address: '7.1', name: 'Backbone side', devices: [DUMMY] },
                { address: '7.9', name: 'Empty line', devices: [] },
            ],
        },
    ],
    unplacedDevices: [SPARE],
};

function telegram(number, destination, more = {}) {
    return { number, time: '2026-11-15T07:01:02.250Z', source: '7.7.9', destination, type: 'WRITE', direction: 'IN', payload: '01', value: 'ON', security: 'PLAIN', ...more };
}

const ADDRESSES = [
    { address: '31/7/1', name: 'Kitchen <b>light</b>', last: telegram(3, '31/7/1') },
    { address: '31/7/2', name: 'Hall light' },
    { address: '31/7/3', name: 'Never sent' },
    { address: '31/7/4', name: 'Fourth' },
];
const SUGGESTIONS = [
    { groupAddress: '31/7/1', name: 'Kitchen light', key: 'ga', value: '31/7/1', label: 'Kitchen light', text: 'Type switch : kitchen "Kitchen light" [ ga="31/7/1" ]', notes: [] },
    { groupAddress: '31/7/2', name: 'Hall light', key: 'ga', value: '31/7/2', label: 'Hall light', text: 'Type switch : hall "Hall light" [ ga="31/7/2" ]', notes: [], password: 'never-shown' },
];

function fakeApi({ topology = TOPOLOGY, device = DETAIL, suggestions = SUGGESTIONS, addresses = ADDRESSES, uses = {} } = {}) {
    const api = {
        calls: [],
        async groupAddress(bridge, address) {
            api.calls.push(['groupAddress', bridge, address]);
            const answer = typeof uses === 'function' ? await uses(address) : uses[address];
            if (answer instanceof Error) {
                throw answer;
            }
            return { address, usedBy: answer ?? [] };
        },
        async topology(bridge) {
            api.calls.push(['topology', bridge]);
            if (topology instanceof Error) {
                throw topology;
            }
            return topology;
        },
        async device(bridge, id) {
            api.calls.push(['device', bridge, id]);
            const answer = typeof device === 'function' ? device(id) : device;
            if (answer instanceof Error) {
                throw answer;
            }
            return answer;
        },
        async deviceChannels(bridge, id) {
            api.calls.push(['channels', bridge, id]);
            if (suggestions instanceof Error) {
                throw suggestions;
            }
            return suggestions;
        },
        async allGroupAddresses(bridge) {
            api.calls.push(['groupAddresses', bridge]);
            return addresses;
        },
    };
    return api;
}

async function open({ bridge = BRIDGE, api = fakeApi(), sel, row, clipboard, stream = bridge.present ? fakeStream('live') : null, rows, shared = false, base, layout } = {}) {
    const model = rows === undefined ? createRows({ bridge: bridge.uid, api }) : rows;
    const harness = viewContext({ bridge, api, rows: model, stream, layout, route: { view: 'topology', bridge: bridge.uid, query: { ...(sel ? { sel } : {}), ...(row ? { row } : {}) } } });
    harness.context.clipboard = clipboard;
    harness.context.base = base;
    const side = shared ? shareSidebar(harness) : null;
    const view = show(harness.context);
    await settle();
    harness.runFrames();
    return Object.assign(harness, { view, api, rows: model, stream, side });
}

const openDevice = (more = {}) => open({ sel: 'device:P-7_D-1', ...more });

function words(node) {
    return textNodes(node).filter((part) => part !== '').join(' ');
}

function treeItems(element) {
    return element.querySelector('.tree').querySelectorAll('li').map((item) => words(item.querySelector('.node')));
}

function treeItem(element, name) {
    return element.querySelector('.tree').querySelectorAll('li').find((item) => words(item.querySelector('.node')) === name);
}

function listRows(element) {
    return element.querySelector('.list').querySelector('tbody')?.children ?? [];
}

function headings(element) {
    return (element.querySelector('.list').querySelector('thead')?.querySelectorAll('th') ?? []).map((cell) => cell.textContent);
}

/** The sections of Properties, each led by the name of the tab it stands under. */
function facts(element) {
    const properties = element.querySelector('.properties');
    return ['Settings', 'Comments', 'Information'].flatMap((tab) => (properties.querySelector(`.tab-${tab.toLowerCase()}`)?.querySelectorAll('section') ?? []).map((section) => [tab, ...textNodes(section).filter((part) => part !== '')]));
}

/** The texts of the sidebar's openHAB container. */
function openhab(element) {
    return textNodes(element.querySelector('.openhab')).filter((part) => part !== '');
}

function tabs(element) {
    return element.querySelector('.list-tabs').querySelectorAll('button').map((tab) => [tab.textContent, tab.getAttribute('aria-selected')]);
}

function tab(element, name) {
    return element.querySelector('.list-tabs').querySelectorAll('button').find((button) => button.textContent.startsWith(name));
}

function crumbs(element) {
    return element.querySelector('.crumbs').querySelectorAll('button').map((crumb) => crumb.textContent);
}

const DEVICE_COLUMNS = ['Address', 'Line', 'Room', 'Name', 'Description', 'Product', 'Order Number', 'Objects', 'Linked', 'openHAB'];
const OBJECT_COLUMNS = ['Number', 'Name', 'Object Function', 'Linked with', 'Other Linked', 'Length', 'C', 'R', 'W', 'T', 'U', 'Data Type', 'openHAB'];
const INIT_COLUMNS = [...OBJECT_COLUMNS.slice(0, 11), 'I', 'Data Type', 'openHAB'];

test('topology: the tree starts with the panel\'s name, the areas built from the individual addresses open under it, and ends with the dynamic folders as a closed group', async () => {
    const t = await open();
    assert.equal(t.element.querySelector('h2').textContent, 'Topology');
    // the tree comes from call 14; the objects of every device (call 11) and the addresses (call 9) are read behind it
    assert.deepEqual(t.api.calls[0], ['topology', 'knx:ip:line7']);
    assert.deepEqual(t.api.calls.slice(1).map((call) => call[0]).sort(), ['device', 'device', 'device', 'device', 'groupAddresses']);
    assert.deepEqual(treeItems(t.element), ['Topology', '7 Main <area>', '7.1 Backbone side', '7.7 Ground floor', '7.9 Empty line', 'Dynamic Folders']);
    treeItem(t.element, '7.1 Backbone side').querySelector('.twisty').fire('click');
    assert.deepEqual(treeItems(t.element).slice(2, 4), ['7.1 Backbone side', '7.1.0 Coupler dummy filter device']);
    assert.deepEqual(t.navigations, []);
});

test('topology: with nothing selected the list holds every device, in the columns ETS users know', async () => {
    const t = await open();
    assert.deepEqual(headings(t.element), DEVICE_COLUMNS);
    assert.deepEqual(
        listRows(t.element).map((row) => row.children.map((cell) => cell.textContent)),
        [
            ['7.1.0', '7.1 Backbone side', '', 'Coupler dummyfilter device', '', '', '', '2', '2', ''],
            ['7.7.1', '7.7 Ground floor', 'Kitchen', 'Kitchen <b>actuator</b>', 'Below the sink', 'Switch actuator', 'SA-8', '8', '5', ''],
            ['7.7.12', '7.7 Ground floor', 'Hall', 'Hall sensor', '', 'Presence', 'PS-1', '3', '0', ''],
            ['–', '', '', 'Spare part', '', '', '', '0', '0', ''],
        ],
    );
    assert.equal(t.element.querySelector('.list').querySelector('a'), null);
    assert.equal(listRows(t.element)[1].children[3].children.length, 0);
    assert.equal(listRows(t.element)[0].querySelector('.filter-device').textContent, 'filter device');
    assert.deepEqual(t.statuses.at(-1), { selection: 'Topology', shown: 4, total: 4 });
});

test('topology: an area lists the devices of all its lines, a line its own, "Not assigned" the ones without an address', async () => {
    const t = await open({ sel: 'area:7' });
    assert.deepEqual(listRows(t.element).map((row) => row.children[0].textContent), ['7.1.0', '7.7.1', '7.7.12']);
    assert.deepEqual(t.statuses.at(-1), { selection: '7 Main <area>', shown: 3, total: 3 });
    assert.deepEqual(facts(t.element), [
        ['Settings', 'Address', '7', 'Name', 'Main <area>'],
        ['Information', 'Devices', '3'],
    ]);
    treeItem(t.element, '7.7 Ground floor').querySelector('.name').fire('click');
    assert.deepEqual(t.navigations, [{ view: 'topology', bridge: 'knx:ip:line7', query: { sel: 'line:7.7' } }]);
    assert.deepEqual(listRows(t.element).map((row) => row.children[0].textContent), ['7.7.1', '7.7.12']);
    t.view.update({ view: 'topology', bridge: BRIDGE.uid, query: { sel: 'line:7.9' } });
    assert.equal(listRows(t.element).length, 0);
    assert.equal(t.element.querySelector('.empty').textContent, 'No device is here.');
    t.view.update({ view: 'topology', bridge: BRIDGE.uid, query: { sel: 'unassigned' } });
    assert.deepEqual(listRows(t.element).map((row) => row.children[3].textContent), ['Spare part']);
    assert.equal(treeItem(t.element, 'Not assigned to a line').getAttribute('aria-selected'), 'true');
});

test('topology: a selected row shows the device in Properties with "Add to openHAB"; Enter opens the device', async () => {
    const t = await open({ sel: 'line:7.7' });
    assert.equal(t.element.querySelector('.add'), null);
    listRows(t.element)[0].children[0].fire('click');
    assert.equal(t.element.querySelector('.properties').querySelector('.subject').textContent, '7.7.1 Kitchen <b>actuator</b>');
    assert.deepEqual(facts(t.element), [
        ['Settings', 'Address', '7.7.1', 'Name', 'Kitchen <b>actuator</b>', 'Description', 'Below the sink'],
        ['Comments', 'Description', 'Below the sink'],
        ['Information', 'Product', 'Switch actuator', 'Order Number', 'SA-8', 'Room', 'Kitchen', 'Objects', '8', 'Linked', '5'],
    ]);
    assert.deepEqual(openhab(t.element), ['Channel', '0 of 5 linked group addresses are in openHAB', 'Add to openHAB…']);
    const adds = t.element.querySelectorAll('.add');
    assert.deepEqual(adds.map((link) => [link.parentNode.className, link.getAttribute('href')]), [
        ['subject-tools', '#/b/knx%3Aip%3Aline7/wizard?device=P-7_D-1'],
        ['openhab', '#/b/knx%3Aip%3Aline7/wizard?device=P-7_D-1'],
    ]);
    listRows(t.element)[0].fire('keydown', { key: 'Enter' });
    assert.deepEqual(t.navigations.at(-1), { view: 'topology', bridge: 'knx:ip:line7', query: { sel: 'device:P-7_D-1' } });
});

test('topology: a click on a row, also on its name, only selects; Properties has the link "Open", a double click opens', async () => {
    const t = await open();
    listRows(t.element)[1].children[3].fire('click');
    assert.deepEqual(t.navigations, []);
    assert.equal(headings(t.element)[0], 'Address', 'still the list of devices');
    assert.equal(listRows(t.element)[1].getAttribute('aria-selected'), 'true');
    const link = t.element.querySelector('.properties').querySelector('.open');
    assert.equal(link.textContent, 'Open');
    assert.equal(link.getAttribute('href'), '#/b/knx%3Aip%3Aline7/topology?sel=device%3AP-7_D-1');
    listRows(t.element)[3].children[3].fire('click');
    assert.equal(t.element.querySelector('.properties').querySelector('.open').getAttribute('href'), '#/b/knx%3Aip%3Aline7/topology?sel=device%3AP-7_D%2F9');
    assert.deepEqual(t.navigations, []);
    listRows(t.element)[3].children[3].fire('dblclick');
    assert.deepEqual(t.navigations, [{ view: 'topology', bridge: 'knx:ip:line7', query: { sel: 'device:P-7_D/9' } }]);
});

test('topology: a selected row is part of the address, without a history entry, and comes back from it', async () => {
    const t = await open({ sel: 'line:7.7' });
    listRows(t.element)[1].children[0].fire('click');
    assert.deepEqual(t.replacements, [{ view: 'topology', bridge: 'knx:ip:line7', query: { sel: 'line:7.7', row: 'device:P-7_D-2' } }]);
    assert.deepEqual(t.navigations, []);

    const again = await open({ sel: 'line:7.7', row: 'device:P-7_D-2' });
    assert.equal(listRows(again.element)[1].getAttribute('aria-selected'), 'true');
    assert.equal(again.element.querySelector('.properties').querySelector('.subject').textContent, '7.7.12 Hall sensor');
    assert.deepEqual(again.replacements, []);
    again.view.update({ view: 'topology', bridge: 'knx:ip:line7', query: { sel: 'line:7.7', row: 'device:P-7_D-1' } });
    assert.equal(listRows(again.element)[0].getAttribute('aria-selected'), 'true');
    assert.equal(listRows(again.element)[1].getAttribute('aria-selected'), 'false');
    again.view.update({ view: 'topology', bridge: 'knx:ip:line7', query: { sel: 'line:7.7' } });
    assert.equal(listRows(again.element)[0].getAttribute('aria-selected'), 'false');
    assert.equal(again.element.querySelector('.properties').querySelector('.subject').textContent, '7.7 Ground floor');

    const device = await openDevice({ row: 'object:2' });
    assert.equal(listRows(device.element)[2].getAttribute('aria-selected'), 'true');
    assert.equal(device.element.querySelector('.properties').querySelector('.subject').textContent, '2 Switch B');
    assert.ok(facts(device.element).some((section) => section[0] === 'Settings' && section[1] === 'Group object' && section.includes('Switch B')));
    listRows(device.element)[0].fire('click');
    assert.deepEqual(device.replacements, [{ view: 'topology', bridge: 'knx:ip:line7', query: { sel: 'device:P-7_D-1', row: 'object:0' } }]);
    listRows(device.element)[0].fire('keydown', { key: 'Enter' });
    assert.deepEqual(device.navigations, []);
});

test('topology: opening a device from the list leaves the row out of the address and puts the focus on the new list', async () => {
    const t = await open({ sel: 'line:7.7' });
    const row = listRows(t.element)[0];
    row.children[0].fire('click');
    row.focus();
    row.fire('keydown', { key: 'Enter' });
    assert.deepEqual(t.navigations, [{ view: 'topology', bridge: 'knx:ip:line7', query: { sel: 'device:P-7_D-1' } }]);
    assert.equal(t.document.activeElement, t.element.querySelector('.list'), 'the list itself while the objects are read');
    await settle();
    assert.equal(headings(t.element)[0], 'Number');
    assert.equal(t.document.activeElement, listRows(t.element)[0]);
    t.view.update({ view: 'topology', bridge: 'knx:ip:line7', query: { sel: 'device:P-7_D-1' } });
    assert.equal(t.document.activeElement, listRows(t.element)[0]);
});

test('topology: a panel opened from another one by key takes the focus into its list, also when the rows come later', async () => {
    const harness = viewContext({ bridge: BRIDGE, api: fakeApi(), rows: null, route: { view: 'topology', bridge: BRIDGE.uid, query: { sel: 'line:7.7' } } });
    const view = show(harness.context);
    view.focus();
    assert.equal(harness.document.activeElement, harness.element.querySelector('.list'));
    await settle();
    assert.equal(harness.document.activeElement, listRows(harness.element)[0]);
    view.close();
});

test('topology: a selection the tree does not have says so and shows no device', async () => {
    for (const sel of ['line:9.9', 'area:99', 'nothing']) {
        const t = await open({ sel });
        assert.equal(t.element.querySelector('.list').textContent, 'The project has nothing of this name.', sel);
        assert.equal(t.element.querySelector('.list').querySelector('table'), null, sel);
        assert.equal(t.element.querySelector('.properties').textContent, 'Nothing is selected.', sel);
        assert.deepEqual(t.statuses.at(-1), { selection: sel, shown: undefined, total: undefined });
        t.view.update({ view: 'topology', bridge: 'knx:ip:line7', query: { sel: 'line:7.7' } });
        assert.equal(listRows(t.element).length, 2, sel);
        assert.equal(t.element.querySelector('.find-slot').querySelector('input').getAttribute('name'), 'find');
    }
});

test('topology: the find box goes over address, line, room, name, description, product and order number; headings sort', async () => {
    const t = await open();
    const field = t.element.querySelector('.find-slot').querySelector('input');
    const shown = () => listRows(t.element).map((row) => row.children[0].textContent);
    for (const [words, found] of [
        ['7.7.1', ['7.7.1', '7.7.12']],
        ['ACTUATOR', ['7.7.1']],
        ['presence', ['7.7.12']],
        ['sa-8', ['7.7.1']],
        ['  hall ', ['7.7.12']],
        ['sink', ['7.7.1']],
        ['backbone', ['7.1.0']],
        ['', ['7.1.0', '7.7.1', '7.7.12', '–']],
    ]) {
        field.value = words;
        field.fire('input');
        assert.deepEqual(shown(), found, words);
    }
    field.value = 'nothing like this';
    field.fire('input');
    assert.equal(t.element.querySelector('.empty').textContent, 'No device matches the filter.');
    assert.deepEqual(t.statuses.at(-1), { selection: 'Topology', shown: 0, total: 4 });
    field.value = '';
    field.fire('input');
    t.element.querySelector('thead').querySelectorAll('button')[7].click();
    assert.deepEqual(shown(), ['–', '7.1.0', '7.7.12', '7.7.1']);
    t.element.querySelector('thead').querySelectorAll('button')[0].click();
    assert.deepEqual(shown(), ['7.1.0', '7.7.1', '7.7.12', '–']);
});

test('topology: a device lists its Group Objects in ETS order of columns, in the order of the page', async () => {
    const t = await openDevice();
    assert.equal(treeItem(t.element, '7.7.1 Kitchen <b>actuator</b>').getAttribute('aria-selected'), 'true');
    assert.deepEqual(tabs(t.element), [['Group Objects (8)', 'true'], ['Channels (0)', 'false']]);
    assert.deepEqual(headings(t.element), INIT_COLUMNS);
    assert.deepEqual(
        listRows(t.element).map((row) => [words(row.children[0]), row.children[1].textContent]),
        [
            ['1', 'Switch A'],
            ['1 not in use', 'Alternative'],
            ['2', 'Switch B'],
            ['3', 'Spare x'],
            ['3', 'Spare y'],
            ['4', 'Linked one'],
            ['4', 'Linked two'],
            ['–', 'Nested'],
        ],
    );
    const [first, unused, second] = listRows(t.element);
    assert.deepEqual([2, 5].map((at) => first.children[at].textContent), ['Switching', '1 byte']);
    assert.deepEqual([5, 12].map((at) => second.children[at].textContent), ['1 bit', '1.001']);
    assert.ok(unused.classList.contains('unused'));
    assert.equal(t.element.querySelectorAll('.unused').length, 1);
    // the device that is opened, then every device once for the lists that need all of them
    assert.deepEqual(t.api.calls.map((call) => call[0]).sort(), ['channels', 'device', 'device', 'device', 'device', 'device', 'groupAddresses', 'topology']);
    assert.deepEqual(t.api.calls[1], ['device', 'knx:ip:line7', 'P-7_D-1']);
    assert.equal(t.stream.starts, 1);
    assert.deepEqual(t.statuses.at(-1), { selection: '7.7.1 Kitchen <b>actuator</b>', shown: 8, total: 8 });
});

test('topology: the five flags as columns C R W T U, the letter where set and a dash where not, each with a title; tick boxes in Properties', async () => {
    const t = await openDevice();
    const flags = (row) => [6, 7, 8, 9, 10].map((at) => row.children[at].textContent).join('');
    assert.deepEqual(listRows(t.element).map(flags), ['-----', '-----', 'C-WT-', '-----', '-----', '-----', '-R--U', '-----']);
    assert.equal(listRows(t.element)[2].children[6].querySelector('.flag').className, 'flag on');
    assert.equal(listRows(t.element)[2].children[7].querySelector('.flag').className, 'flag off');
    const linkedTwo = listRows(t.element)[6];
    assert.equal(linkedTwo.children[7].querySelector('.flag').getAttribute('title'), 'Read: set');
    assert.equal(linkedTwo.children[6].querySelector('.flag').getAttribute('title'), 'Communication: not set');
    assert.equal(linkedTwo.children[7].querySelector('.flag').getAttribute('aria-label'), 'Read: set');
    linkedTwo.children[1].fire('click');
    assert.deepEqual(facts(t.element).slice(0, 2), [
        ['Settings', 'Group object', 'Number', '4', 'Name', 'Linked two', 'Linked with', '31/7/4'],
        ['Settings', 'Flags', 'Communication', 'Read', 'Write', 'Transmit', 'Update', 'Read on init'],
    ]);
    const ticks = t.element.querySelector('.properties').querySelector('.ticks').querySelectorAll('input');
    assert.deepEqual(ticks.map((box) => [box.getAttribute('type'), box.disabled, box.checked]), [
        ['checkbox', true, false],
        ['checkbox', true, true],
        ['checkbox', true, false],
        ['checkbox', true, false],
        ['checkbox', true, true],
        ['checkbox', true, true],
    ]);
    listRows(t.element)[2].children[1].fire('click');
    assert.deepEqual(facts(t.element)[1], ['Settings', 'Flags', 'Communication', 'Read', 'Write', 'Transmit', 'Update'], 'Read on init only when it is set');
});

test('topology: "Linked with" holds the sending address with its name, "Other Linked" the further ones; names come from call 9', async () => {
    const t = await openDevice();
    const [linked, others] = [3, 4].map((at) => listRows(t.element)[0].children[at]);
    assert.deepEqual(textNodes(linked).filter((part) => part !== ''), ['31/7/1', 'Kitchen <b>light</b>', 'ON']);
    assert.equal(linked.querySelector('.name').children.length, 0);
    assert.deepEqual(others.querySelectorAll('li').map((item) => textNodes(item).filter((part) => part !== '')), [['31/7/3', 'Never sent']]);
    // an object without a sending address: the first one it is linked to
    const nested = listRows(t.element)[7];
    assert.deepEqual(textNodes(nested.children[3]).filter((part) => part !== ''), ['31/7/5']);
    assert.equal(nested.children[4].textContent, '');
    assert.equal(listRows(t.element)[1].children[3].textContent, '', 'an object linked to nothing');
});

test('topology: last values come from the row model and stay live; the list of call 9 is read once for the bridge', async () => {
    const t = await openDevice();
    const value = () => listRows(t.element)[2].children[3].querySelector('.value')?.textContent ?? '';
    assert.equal(value(), '');
    t.rows.add(telegram(20, '31/7/2', { value: 'OFF' }));
    assert.equal(value(), '');
    t.runFrames();
    assert.equal(value(), 'OFF');
    t.rows.clear();
    t.runFrames();
    assert.equal(value(), '');
    t.view.update({ view: 'topology', bridge: BRIDGE.uid, query: { sel: 'line:7.7' } });
    t.view.update({ view: 'topology', bridge: BRIDGE.uid, query: { sel: 'device:P-7_D-1' } });
    await settle();
    assert.equal(t.api.calls.filter(([name]) => name === 'groupAddresses').length, 1);
    // twice for the device that is opened, once for each of the four devices of the project
    assert.equal(t.api.calls.filter(([name]) => name === 'device').length, 6);
});

test('topology: a bridge with the monitor off or removed opens no stream', async () => {
    const off = await openDevice({ bridge: OFF });
    assert.equal(off.stream.starts, 0);
    const removed = await openDevice({ bridge: REMOVED });
    assert.equal(removed.stream, null);
    assert.equal(listRows(removed.element).length, 8);
    assert.equal(removed.element.querySelector('.add'), null);
});

test('topology: Properties of a device show its head, a filter device its label and note', async () => {
    const t = await openDevice();
    assert.equal(t.element.querySelector('.properties').querySelector('.subject').textContent, '7.7.1 Kitchen <b>actuator</b>');
    assert.deepEqual(facts(t.element), [
        ['Settings', 'Address', '7.7.1', 'Name', 'Kitchen <b>actuator</b>', 'Description', 'Below the sink'],
        ['Comments', 'Description', 'Below the sink'],
        ['Information', 'Product', 'Switch actuator', 'Order Number', 'SA-8', 'Room', 'Kitchen', 'Objects', '8', 'Linked', '5'],
    ]);
    assert.equal(t.element.querySelector('.properties').querySelector('.filter-device'), null);
    assert.equal(t.element.querySelector('.filter-note'), null);
    const dummy = await open({ sel: 'device:P-7_D-5', api: fakeApi({ device: { ...DUMMY, groupObjects: [] } }) });
    assert.equal(dummy.element.querySelector('.properties').querySelector('.filter-device').textContent, 'filter device');
    assert.equal(
        dummy.element.querySelector('.filter-note').textContent,
        "A filter device only fills a line coupler's filter table. It is no hardware and never answers, so its objects give no read mark.",
    );
    assert.equal(dummy.element.querySelector('.list').querySelector('.empty').textContent, 'The device has no group objects.');
});

test('topology: the channel suggestions of call 12 as one text block in the openHAB container, and no other field of the answer', async () => {
    const t = await openDevice();
    const field = t.element.querySelector('textarea');
    assert.equal(field.value, 'Type switch : kitchen "Kitchen light" [ ga="31/7/1" ]\nType switch : hall "Hall light" [ ga="31/7/2" ]');
    assert.equal(field.hasAttribute('readonly'), true);
    assert.ok(!textNodes(t.element).some((words) => words.includes('never-shown')));
    assert.ok(!field.value.includes('never-shown'));
    assert.deepEqual(openhab(t.element), ['Channel', '0 of 5 linked group addresses are in openHAB', 'Add to openHAB…', 'Channel suggestions', 'Copy']);
    assert.ok(t.element.querySelector('.openhab').contains(field));
    const none = await openDevice({ api: fakeApi({ suggestions: [] }) });
    assert.ok(textNodes(none.element).includes('The project suggests no channel for this device.'));
    assert.equal(none.element.querySelector('textarea'), null);
    const failed = await openDevice({ api: fakeApi({ suggestions: new KnxApiError(500, 'internal', 'x') }) });
    assert.ok(textNodes(failed.element).includes('Something went wrong inside the KNX add-on.'));
    assert.deepEqual(failed.errors, []);
    assert.equal(listRows(failed.element).length, 8);
});

async function copyWith(clipboard, commandResult) {
    const t = await openDevice({ clipboard });
    t.document.commandResult = commandResult;
    t.element.querySelector('.copy').click();
    await settle();
    return t;
}

test('topology: Copy also works in the sidebar that two panels share, which stands outside the panel', async () => {
    const written = [];
    const t = await openDevice({ clipboard: { writeText: async (words) => written.push(words) }, shared: true });
    assert.equal(t.element.querySelector('.sidebar'), null);
    t.side.querySelector('.copy').click();
    await settle();
    assert.deepEqual(written, [t.side.querySelector('textarea').value]);
    assert.equal(t.side.querySelector('.copied').textContent, 'Copied');
});

test('topology: Copy uses the clipboard when there is one', async () => {
    const written = [];
    const t = await copyWith({ writeText: async (words) => written.push(words) }, true);
    assert.deepEqual(written, [t.element.querySelector('textarea').value]);
    assert.deepEqual(t.document.commands, []);
    assert.equal(t.element.querySelector('.copied').textContent, 'Copied');
});

test('topology: without a clipboard Copy selects the field and asks the document to copy', async () => {
    const t = await copyWith(undefined, true);
    const field = t.element.querySelector('textarea');
    assert.deepEqual(t.document.commands, [{ command: 'copy', selection: field }]);
    assert.equal(t.element.querySelector('.copied').textContent, 'Copied');
});

test('topology: when clipboard and copy command both fail the text stays selected and the page says Press Ctrl+C', async () => {
    const refused = {
        writeText: async () => {
            throw new Error('not allowed');
        },
    };
    for (const [clipboard, result] of [
        [refused, false],
        [refused, new Error('not supported')],
        [undefined, false],
    ]) {
        const t = await copyWith(clipboard, result);
        const field = t.element.querySelector('textarea');
        assert.equal(t.document.selection, field);
        assert.equal(t.document.activeElement, field);
        assert.equal(t.document.commands.length, 1);
        assert.equal(t.element.querySelector('.copied').textContent, 'Press Ctrl+C');
    }
});

test('topology: an address of the page before the panels names a device by its individual address', async () => {
    const t = await open({ sel: 'device:7.7.1' });
    assert.equal(treeItem(t.element, '7.7.1 Kitchen <b>actuator</b>').getAttribute('aria-selected'), 'true');
    assert.deepEqual(t.api.calls[1], ['device', 'knx:ip:line7', '7.7.1']);
    assert.equal(listRows(t.element).length, 8);
    assert.equal(t.element.querySelector('.subject-tools').querySelector('a').getAttribute('href'), '#/b/knx%3Aip%3Aline7/wizard?device=7.7.1');
});

test('topology: an unknown device and a missing project each say so, other failures go to the error line', async () => {
    // only the read of the opened device fails; the reads of every device behind it go well
    const only = (wanted, error) => (id) => (id === wanted ? error : DETAIL);
    const unknown = await open({ api: fakeApi({ device: only('7.7.99', new KnxApiError(404, 'unknown-device', 'x')) }), sel: 'device:7.7.99' });
    assert.ok(textNodes(unknown.element.querySelector('.list')).includes('The project has no such device.'));
    assert.deepEqual(unknown.statuses.at(-1).selection, '7.7.99');
    assert.deepEqual(unknown.errors, []);
    let reads = 0;
    const first = (error) => () => (reads++ === 0 ? error : DETAIL);
    const missing = await open({ api: fakeApi({ device: first(new KnxApiError(404, 'no-project', 'x')) }), sel: 'device:P-7_D-1' });
    assert.equal(missing.element.querySelector('.list').querySelector('.missing').querySelector('a').getAttribute('href'), '#/b/knx%3Aip%3Aline7/project');
    reads = 0;
    const network = await open({ api: fakeApi({ device: first(new KnxApiError(0, 'network')) }), sel: 'device:P-7_D-1' });
    assert.deepEqual(network.errors.map((error) => error.code), ['network']);
    for (const t of [unknown, missing, network]) {
        assert.deepEqual(t.api.calls.slice(0, 2).map((call) => call[0]), ['topology', 'device']);
        assert.equal(t.api.calls.some((call) => call[0] === 'channels'), false, 'no suggestions for a device that could not be read');
    }

    // a device that cannot be read behind the list shows its error, the others are listed
    const broken = await open({ api: fakeApi({ device: only('P-7_D-2', new KnxApiError(500, 'internal', 'x')) }), sel: 'line:7.7' });
    assert.deepEqual(broken.errors.map((error) => error.code), ['internal']);
    assert.equal(listRows(broken.element).length, 2);
});

test('topology: without a project the text and the link to Project, and no error line; another failure goes to the error line', async () => {
    for (const [code, words] of [
        ['no-project', 'No ETS project is loaded for this bridge.'],
        ['project-unusable', 'The stored ETS project cannot be used. Import it again.'],
    ]) {
        const t = await open({ api: fakeApi({ topology: new KnxApiError(404, code, 'server words') }) });
        const note = t.element.querySelector('.missing');
        assert.deepEqual(textNodes(note), [words, ' ', 'Go to Project'], code);
        assert.equal(note.querySelector('a').getAttribute('href'), '#/b/knx%3Aip%3Aline7/project');
        assert.equal(t.element.querySelector('.list').querySelector('table'), null);
        assert.deepEqual(t.errors, []);
    }
    const failed = await open({ api: fakeApi({ topology: new KnxApiError(0, 'network') }) });
    assert.deepEqual(failed.errors.map((error) => error.code), ['network']);
    assert.equal(failed.element.querySelector('.missing'), null);
});

test('topology: a project without areas or devices says so', async () => {
    const t = await open({ api: fakeApi({ topology: { areas: [], unplacedDevices: [] } }) });
    assert.deepEqual(treeItems(t.element), ['Topology', 'Dynamic Folders']);
    assert.equal(t.element.querySelector('.empty').textContent, 'The project has no devices.');
});

test('topology: the tree moves with the arrow keys and selects with Enter', async () => {
    const t = await open();
    const key = (name) => t.document.activeElement.fire('keydown', { key: name });
    treeItem(t.element, 'Topology').focus();
    key('ArrowDown');
    key('ArrowDown');
    key('ArrowRight');
    key('ArrowDown');
    key('ArrowDown');
    assert.ok(t.document.activeElement === treeItem(t.element, '7.7 Ground floor'));
    assert.deepEqual(t.navigations, []);
    key('Enter');
    assert.deepEqual(t.navigations.map((route) => route.query), [{ sel: 'line:7.7' }]);
    assert.deepEqual(listRows(t.element).map((row) => row.children[0].textContent), ['7.7.1', '7.7.12']);
});

test('topology: close() leaves no listener and no drawing, also while a call is on its way', async () => {
    const t = await openDevice();
    assert.ok(t.document.listenerCount() > 0);
    t.view.close();
    assert.equal(t.document.listenerCount(), 0);
    assert.equal(t.rows.subscribers(), 0);
    t.rows.add(telegram(30, '31/7/2'));
    assert.equal(t.pendingFrames(), 0);
    assert.equal(t.clock.count(), 0);

    for (const slow of ['topology', 'device']) {
        let answer = null;
        const api = fakeApi();
        api[slow] = () => new Promise((resolve) => (answer = resolve));
        const harness = viewContext({ bridge: BRIDGE, api, rows: createRows({ bridge: BRIDGE.uid, api }), stream: fakeStream(), route: { view: 'topology', bridge: BRIDGE.uid, query: { sel: 'device:P-7_D-1' } } });
        const view = show(harness.context);
        await settle();
        view.close();
        answer(slow === 'topology' ? TOPOLOGY : DETAIL);
        await settle();
        assert.equal(harness.element.querySelector('.list').querySelector('table'), null, slow);
        assert.deepEqual(harness.errors, []);
        assert.equal(harness.document.listenerCount(), 0);
    }
});

test('topology: the column I comes after U only when an object of the device has read on init set', async () => {
    const plain = await openDevice({ api: fakeApi({ device: { ...ACTUATOR, groupObjects: OBJECTS.map((item) => ({ ...item, readOnInit: false })) } }) });
    assert.deepEqual(headings(plain.element), OBJECT_COLUMNS);
    const t = await openDevice();
    assert.deepEqual(headings(t.element), INIT_COLUMNS);
    const marks = listRows(t.element).map((row) => row.children[11].querySelector('.flag').classList.contains('on'));
    assert.deepEqual(marks, [false, false, false, false, false, false, true, false]);
    assert.equal(t.element.querySelector('.list').querySelectorAll('th')[11].querySelector('button').getAttribute('title'), 'Read on init');
});

test('topology: the tree that slid in closes when a node is chosen, and the focus goes to the list', async () => {
    const t = await open();
    const box = t.element.querySelector('.panel');
    t.element.querySelector('.tree-toggle').click();
    assert.ok(box.classList.contains('tree-open'));
    assert.ok(t.document.activeElement === treeItem(t.element, 'Topology'));
    treeItem(t.element, '7 Main <area>').fire('keydown', { key: 'Enter' });
    assert.equal(box.classList.contains('tree-open'), false);
    assert.ok(t.document.activeElement === listRows(t.element)[0]);
    // with the tree beside the list nothing slid in: the focus stays in the tree
    treeItem(t.element, 'Topology').fire('keydown', { key: 'Enter' });
    assert.ok(t.document.activeElement === treeItem(t.element, 'Topology'));
});

// A project for the lists that need every device: three devices with objects of their own, two addresses in openHAB.
const IN_USE = [
    { address: '31/7/1', name: 'Kitchen light', dataType: '1.001', configured: true },
    { address: '31/7/2', name: 'Hall light', dataType: '1.001', configured: false },
    { address: '31/7/3', name: 'Status', description: 'Feedback of the kitchen', dataType: '1.011', configured: true },
    { address: '31/7/4', name: 'Fourth' },
];
const DETAILS = {
    'P-7_D-1': { ...ACTUATOR, groupObjects: [object(2, 'Switch B', ['31/7/2']), object(1, 'Switch A', ['31/7/3', '31/7/1'], { sendingGroupAddress: '31/7/1', communication: true, write: true }), object(1, 'Status A', ['31/7/3'])] },
    'P-7_D-2': { ...SENSOR, groupObjects: [object(0, 'Presence', ['31/7/2', '31/7/4'], { transmit: true, readOnInit: true })] },
    'P-7_D-5': { ...DUMMY, groupObjects: [object(0, 'Dummy', ['31/7/1'])] },
    'P-7_D/9': { ...SPARE, groupObjects: [] },
};
const projectApi = (more = {}) => fakeApi({ addresses: IN_USE, device: (id) => DETAILS[id] ?? new KnxApiError(404, 'unknown-device', 'x'), ...more });

function outline(element) {
    return listRows(element).map((row) => (row.classList.contains('group-row') ? `# ${row.querySelector('.label').textContent} (${row.querySelector('.count').textContent})` : `${row.children[0].textContent} ${row.children[1].textContent}`));
}

test('folders: Topology has "Not assigned to a line", "In openHAB" and "Not in openHAB" below its areas; a folder lists its members', async () => {
    const t = await open({ api: projectApi() });
    treeItem(t.element, 'Dynamic Folders').querySelector('.twisty').fire('click');
    assert.deepEqual(treeItems(t.element).slice(-4), ['Dynamic Folders', 'Not assigned to a line', 'In openHAB', 'Not in openHAB']);
    assert.deepEqual(treeItems(t.element).slice(0, 2), ['Topology', '7 Main <area>']);
    assert.ok(treeItem(t.element, 'In openHAB').querySelector('.icon-folder'));
    const members = () => listRows(t.element).map((row) => row.children[0].textContent);
    treeItem(t.element, 'In openHAB').querySelector('.name').fire('click');
    assert.deepEqual(t.navigations.at(-1), { view: 'topology', bridge: 'knx:ip:line7', query: { sel: 'folder:in-openhab' } });
    assert.deepEqual(members(), ['7.1.0', '7.7.1'], 'a device with at least one linked address that a channel uses');
    assert.deepEqual(tabs(t.element), [['Devices (2)', 'true'], ['Group Objects (4)', 'false']]);
    assert.deepEqual(crumbs(t.element), ['Dynamic Folders', 'In openHAB']);
    assert.deepEqual(t.statuses.at(-1), { selection: 'In openHAB', shown: 2, total: 2 });
    treeItem(t.element, 'Not in openHAB').querySelector('.name').fire('click');
    assert.deepEqual(members(), ['7.7.12', '–']);
    treeItem(t.element, 'Not assigned to a line').querySelector('.name').fire('click');
    assert.deepEqual(listRows(t.element).map((row) => row.children[3].textContent), ['Spare part']);
    // the group itself lists nothing and says what to do
    treeItem(t.element, 'Dynamic Folders').querySelector('.name').fire('click');
    assert.equal(t.element.querySelector('.list').textContent, 'Choose one of the folders.');
    assert.equal(t.element.querySelector('.properties').textContent, 'Nothing is selected.');
    // the openHAB column of the device list follows the same rule
    t.view.update({ view: 'topology', bridge: BRIDGE.uid, query: {} });
    assert.deepEqual(listRows(t.element).map((row) => row.children[9].textContent), ['in use', 'in use', '', '']);
});

test('folders: a device whose objects could not be read is in neither openHAB folder: it stands under a line of its own and is read again the next time', async () => {
    let broken = true;
    const device = (id) => (id === 'P-7_D-2' && broken ? new KnxApiError(0, 'network') : (DETAILS[id] ?? new KnxApiError(404, 'unknown-device', 'x')));
    const api = projectApi({ device });
    const rows = createRows({ bridge: BRIDGE.uid, api });
    const t = await open({ api, rows, sel: 'folder:not-in-openhab' });
    const members = () => listRows(t.element).map((row) => row.children[0].textContent);
    assert.deepEqual(members(), ['–'], 'not counted as "Not in openHAB"');
    const unread = t.element.querySelector('.list').querySelector('.unread');
    assert.equal(unread.querySelector('p').textContent, 'Could not be read: 1. These devices are in neither openHAB folder and are read again when a panel is next opened.');
    assert.deepEqual(unread.querySelectorAll('a').map((link) => [link.textContent, link.getAttribute('href')]), [['7.7.12 Hall sensor', '#/b/knx%3Aip%3Aline7/topology?sel=device%3AP-7_D-2']]);
    treeItem(t.element, 'In openHAB').querySelector('.name').fire('click');
    assert.deepEqual(members(), ['7.1.0', '7.7.1']);
    assert.ok(t.element.querySelector('.list').querySelector('.unread'), 'said in both folders');
    // the list of all devices says it in the openHAB column, and has no such line
    t.view.update({ view: 'topology', bridge: BRIDGE.uid, query: {} });
    assert.deepEqual(listRows(t.element).map((row) => row.children[9].textContent), ['in use', 'in use', 'could not be read', '']);
    assert.equal(t.element.querySelector('.list').querySelector('.unread'), null);
    t.view.close();
    broken = false;
    const again = await open({ api, rows, sel: 'folder:not-in-openhab' });
    assert.deepEqual(listRows(again.element).map((row) => row.children[0].textContent), ['7.7.12', '–']);
    assert.equal(again.element.querySelector('.list').querySelector('.unread'), null);
    assert.equal(api.calls.filter(([name, , id]) => name === 'device' && id === 'P-7_D-2').length, 2, 'only the one that failed is read again');
});

test('folders: the openHAB folders say "Loading" until every device was read, and a removed bridge has none of them', async () => {
    let release = null;
    const api = projectApi();
    const read = api.device;
    api.device = async (bridge, id) => {
        await new Promise((resolve) => (release = resolve));
        return read(bridge, id);
    };
    const harness = viewContext({ bridge: BRIDGE, api, rows: createRows({ bridge: BRIDGE.uid, api }), stream: fakeStream('live'), route: { view: 'topology', bridge: BRIDGE.uid, query: { sel: 'folder:in-openhab' } } });
    const view = show(harness.context);
    await settle();
    assert.equal(harness.element.querySelector('.empty').textContent, 'Loading…');
    assert.ok(release !== null);
    view.close();

    const removed = await open({ bridge: REMOVED, api: projectApi() });
    treeItem(removed.element, 'Dynamic Folders').querySelector('.twisty').fire('click');
    assert.deepEqual(treeItems(removed.element).slice(-2), ['Dynamic Folders', 'Not assigned to a line']);
    assert.deepEqual(headings(removed.element), DEVICE_COLUMNS.slice(0, 9), 'and no openHAB column');
    removed.view.update({ view: 'topology', bridge: REMOVED.uid, query: { sel: 'folder:in-openhab' } });
    assert.equal(removed.element.querySelector('.list').textContent, 'The project has nothing of this name.');
});

test('crumbs: the path of what is selected stands in the bar, and a crumb selects its node', async () => {
    const t = await open({ api: projectApi(), sel: 'device:P-7_D-1' });
    assert.deepEqual(crumbs(t.element), ['Topology', '7 Main <area>', '7.7 Ground floor', '7.7.1 Kitchen <b>actuator</b>']);
    const buttons = t.element.querySelector('.crumbs').querySelectorAll('button');
    assert.equal(buttons[3].getAttribute('aria-current'), 'true');
    assert.equal(buttons[1].children.length, 0, 'a name is text');
    buttons[2].click();
    assert.deepEqual(t.navigations, [{ view: 'topology', bridge: 'knx:ip:line7', query: { sel: 'line:7.7' } }]);
    assert.deepEqual(crumbs(t.element), ['Topology', '7 Main <area>', '7.7 Ground floor']);
    assert.equal(treeItem(t.element, '7.7 Ground floor').getAttribute('aria-selected'), 'true');
    assert.deepEqual(listRows(t.element).map((row) => row.children[0].textContent), ['7.7.1', '7.7.12']);
    t.element.querySelector('.crumbs').querySelectorAll('button')[0].click();
    assert.deepEqual(t.navigations.at(-1), { view: 'topology', bridge: 'knx:ip:line7', query: {} });
    assert.deepEqual(crumbs(t.element), ['Topology']);
    // the last crumb is where the panel is: choosing it changes nothing
    t.element.querySelector('.crumbs').querySelectorAll('button')[0].click();
    assert.equal(t.navigations.length, 2);
    t.view.update({ view: 'topology', bridge: BRIDGE.uid, query: { sel: 'line:9.9' } });
    assert.deepEqual(crumbs(t.element), ['Topology', 'line:9.9'], 'a selection the tree does not have');
});

test('chooser: the bar names the panel that is shown and offers the five panels, none of them Devices; another one is opened', async () => {
    const t = await open({ api: projectApi(), sel: 'line:7.7' });
    const chooser = t.element.querySelector('.panel-chooser');
    assert.deepEqual(chooser.querySelectorAll('option').map((option) => option.textContent), ['Group Addresses', 'Topology', 'Buildings', 'Group Monitor', 'Project']);
    assert.equal(chooser.value, 'topology');
    chooser.value = 'group-addresses';
    chooser.fire('change');
    assert.deepEqual(t.navigations, [{ view: 'group-addresses', bridge: 'knx:ip:line7', query: {} }]);
    const removed = await open({ bridge: REMOVED, api: projectApi() });
    assert.deepEqual(removed.element.querySelector('.panel-chooser').querySelectorAll('option').map((option) => option.getAttribute('value')), ['group-addresses', 'topology', 'buildings', 'project']);
    // back and forward are the browser's history
    const moves = [];
    const harness = viewContext({ bridge: BRIDGE, api: projectApi(), rows: null, route: { view: 'topology', bridge: BRIDGE.uid, query: {} } });
    harness.context.history = { back: () => moves.push('back'), forward: () => moves.push('forward') };
    show(harness.context);
    harness.element.querySelector('.nav-back').click();
    harness.element.querySelector('.nav-forward').click();
    assert.deepEqual(moves, ['back', 'forward']);
});

test('tabs: a line shows Devices and Group Objects with their counts; Group Objects lists the objects of all its devices under a heading row per device', async () => {
    const t = await open({ api: projectApi(), sel: 'line:7.7' });
    assert.deepEqual(tabs(t.element), [['Devices (2)', 'true'], ['Group Objects (4)', 'false']]);
    tab(t.element, 'Group Objects').click();
    assert.deepEqual(tabs(t.element).map(([, on]) => on), ['false', 'true']);
    assert.deepEqual(headings(t.element), INIT_COLUMNS);
    assert.deepEqual(outline(t.element), ['# 7.7.1 Kitchen <b>actuator</b> (3)', '1 Switch A', '1 Status A', '2 Switch B', '# 7.7.12 Hall sensor (1)', '0 Presence']);
    assert.deepEqual(t.replacements.at(-1), { view: 'topology', bridge: 'knx:ip:line7', query: { sel: 'line:7.7', tab: 'objects' } });
    assert.deepEqual(t.statuses.at(-1), { selection: '7.7 Ground floor', shown: 4, total: 4 });
    const switchA = listRows(t.element)[1];
    assert.deepEqual([3, 4, 13].map((at) => textNodes(switchA.children[at]).filter((part) => part !== '').join(' ')), ['31/7/1 Kitchen light', '31/7/3 Status', 'in use']);
    assert.equal(listRows(t.element)[3].children[13].textContent, '', 'an object whose addresses no channel uses');

    // a heading row folds its device
    const fold = listRows(t.element)[0].querySelector('.fold');
    fold.click();
    assert.deepEqual(outline(t.element), ['# 7.7.1 Kitchen <b>actuator</b> (3)', '# 7.7.12 Hall sensor (1)', '0 Presence']);
    assert.equal(fold.getAttribute('aria-expanded'), 'false');
    assert.deepEqual(t.statuses.at(-1), { selection: '7.7 Ground floor', shown: 1, total: 4 });
    fold.click();
    assert.equal(outline(t.element).length, 6);
});

test('tabs: a group object of a place only selects on a click: Properties show it with its device, the address names tab and row; Enter opens the device', async () => {
    const t = await open({ api: projectApi(), sel: 'line:7.7' });
    tab(t.element, 'Group Objects').click();
    listRows(t.element)[1].children[1].fire('click');
    assert.deepEqual(t.navigations, []);
    assert.equal(t.element.querySelector('.properties').querySelector('.subject').textContent, '1 Switch A');
    assert.deepEqual(facts(t.element).at(-1), ['Information', 'Device', '7.7.1 Kitchen <b>actuator</b>', 'Product', 'Switch actuator']);
    assert.equal(t.element.querySelector('.properties').querySelector('.open').getAttribute('href'), '#/b/knx%3Aip%3Aline7/topology?sel=device%3AP-7_D-1');
    assert.deepEqual(t.replacements.at(-1), { view: 'topology', bridge: 'knx:ip:line7', query: { sel: 'line:7.7', tab: 'objects', row: 'object:P-7_D-1|0' } });
    listRows(t.element)[1].fire('keydown', { key: 'Enter' });
    assert.deepEqual(t.navigations, [{ view: 'topology', bridge: 'knx:ip:line7', query: { sel: 'device:P-7_D-1' } }]);

    // the address brings tab and row back, also before the devices were read
    const again = await open({ api: projectApi(), sel: 'line:7.7', row: 'object:P-7_D-2|0' });
    assert.deepEqual(tabs(again.element).map(([, on]) => on), ['false', 'true']);
    assert.equal(listRows(again.element)[5].getAttribute('aria-selected'), 'true');
    assert.equal(again.element.querySelector('.properties').querySelector('.subject').textContent, '0 Presence');
    again.view.update({ view: 'topology', bridge: BRIDGE.uid, query: { sel: 'line:7.7' } });
    assert.deepEqual(tabs(again.element).map(([, on]) => on), ['true', 'false'], 'an address without a tab shows the first one');
    again.view.update({ view: 'topology', bridge: BRIDGE.uid, query: { sel: 'line:7.7', tab: 'objects' } });
    assert.equal(outline(again.element).length, 6);
});

const USES = {
    '31/7/3': [
        { thingUID: 'knx:device:line7:kitchen', thingLabel: 'Kitchen <i>actuator</i>', channelId: 'light', channelLabel: 'Ceiling', channelType: 'switch', role: 'status', items: ['Kitchen_Light', 'Kitchen_Light_B'] },
        { thingUID: 'knx:device:line7:hall', channelId: 'dimmer', channelType: 'dimmer', role: 'listening', items: [] },
    ],
    '31/7/1': [{ thingUID: 'knx:device:line7:kitchen', thingLabel: 'Kitchen <i>actuator</i>', channelId: 'light', channelLabel: 'Ceiling', channelType: 'switch', role: 'main', items: ['Kitchen_Light'] }],
};
const REST = new URL('http://openhab.invalid/oh/rest/');


function links(node) {
    return node.querySelectorAll('a').map((link) => [link.textContent, link.getAttribute('href'), link.getAttribute('target')]);
}

test('tabs: a device shows Group Objects and Channels; Channels lists, per address a channel uses, the Thing, channel, type, role and Items', async () => {
    const t = await open({ api: projectApi({ uses: USES }), sel: 'device:P-7_D-1', base: REST });
    assert.deepEqual(tabs(t.element), [['Group Objects (3)', 'true'], ['Channels (2)', 'false']]);
    assert.equal(t.api.calls.some(([name]) => name === 'groupAddress'), false, 'nothing is asked before the tab is shown');
    tab(t.element, 'Channels').click();
    assert.deepEqual(headings(t.element), ['Address', 'Name', 'Data Type', 'Objects', 'Thing', 'Channel', 'Type', 'Role', 'Items']);
    assert.deepEqual(listRows(t.element).map((row) => row.children.map((cell) => cell.textContent)), [
        ['31/7/3', 'Status', '1.011', '1', 'Loading…', '', '', '', ''],
        ['31/7/1', 'Kitchen light', '1.001', '1', 'Loading…', '', '', '', ''],
    ]);
    await settle();
    assert.deepEqual(t.api.calls.filter(([name]) => name === 'groupAddress'), [['groupAddress', 'knx:ip:line7', '31/7/3'], ['groupAddress', 'knx:ip:line7', '31/7/1']]);
    assert.deepEqual(listRows(t.element).map((row) => row.children.map((cell) => cell.textContent)), [
        ['31/7/3', 'Status', '1.011', '1', 'Kitchen <i>actuator</i>', 'Ceiling', 'switch', 'status address', 'Kitchen_Light, Kitchen_Light_B'],
        ['31/7/3', 'Status', '1.011', '1', 'knx:device:line7:hall', 'dimmer', 'dimmer', 'listening address', ''],
        ['31/7/1', 'Kitchen light', '1.001', '1', 'Kitchen <i>actuator</i>', 'Ceiling', 'switch', 'main address', 'Kitchen_Light'],
    ]);
    assert.deepEqual(tabs(t.element)[1], ['Channels (2)', 'true'], 'the count is the addresses');
    assert.equal(listRows(t.element)[0].children[4].children[0].children.length, 0, 'a label of openHAB is text');
    assert.deepEqual(links(listRows(t.element)[0]), [
        ['Kitchen <i>actuator</i>', '/oh/settings/things/knx:device:line7:kitchen', '_blank'],
        ['Kitchen_Light', '/oh/settings/items/Kitchen_Light', '_blank'],
        ['Kitchen_Light_B', '/oh/settings/items/Kitchen_Light_B', '_blank'],
    ]);
    assert.equal(t.element.querySelector('.channels-note').textContent, 'Thing, channel and Items were read for 2 of 2 addresses.');
    assert.equal(t.element.querySelector('.channels-note').querySelector('.more'), null);
    assert.deepEqual(t.replacements.at(-1), { view: 'topology', bridge: 'knx:ip:line7', query: { sel: 'device:P-7_D-1', tab: 'channels' } });
    listRows(t.element)[0].children[1].fire('click');
    assert.deepEqual(t.navigations, []);
    assert.deepEqual(facts(t.element), [
        ['Settings', 'Address', '31/7/3', 'Name', 'Status', 'Description', 'Feedback of the kitchen', 'Data Type', '1.011'],
        ['Comments', 'Description', 'Feedback of the kitchen'],
        ['Information', 'Device', '7.7.1 Kitchen <b>actuator</b>', 'Objects', '1'],
    ]);
    assert.deepEqual(openhab(t.element), [
        'Channel',
        'in use',
        'Used by',
        'Kitchen <i>actuator</i>',
        'Ceiling · switch · status address',
        'Items:',
        ' ',
        'Kitchen_Light',
        ', ',
        'Kitchen_Light_B',
        'knx:device:line7:hall',
        'dimmer · dimmer · listening address',
        'No Item is linked.',
        'Add to openHAB…',
    ]);
    assert.deepEqual(t.replacements.at(-1).query, { sel: 'device:P-7_D-1', tab: 'channels', row: 'ga:31/7/3' });
    listRows(t.element)[0].fire('dblclick');
    assert.deepEqual(t.navigations, [{ view: 'group-addresses', bridge: 'knx:ip:line7', query: { sel: 'ga:31/7/3' } }]);
    assert.equal(t.api.calls.filter(([name]) => name === 'groupAddress').length, 2, 'each address is asked once');

    const again = await openChannelsAt('ga:31/7/1');
    assert.deepEqual(tabs(again.element).map(([, on]) => on), ['false', 'true']);
    assert.equal(listRows(again.element)[2].getAttribute('aria-selected'), 'true');
    const none = await open({ api: projectApi(), sel: 'device:P-7_D-2' });
    assert.deepEqual(tabs(none.element), [['Group Objects (1)', 'true'], ['Channels (0)', 'false']]);
    tab(none.element, 'Channels').click();
    assert.equal(none.element.querySelector('.empty').textContent, 'No channel of this bridge uses an address of this device.');
    assert.equal(none.element.querySelector('.channels-note').textContent, '');
    assert.equal(none.api.calls.some(([name]) => name === 'groupAddress'), false);
});

async function openChannelsAt(row) {
    const t = await open({ api: projectApi({ uses: USES }), sel: 'device:P-7_D-1', row, base: REST });
    await settle();
    return t;
}

test('openhab: a device whose objects could not be read is left out of "n of m devices are in openHAB" of its place', async () => {
    const device = (id) => (id === 'P-7_D-2' ? new KnxApiError(0, 'network') : (DETAILS[id] ?? new KnxApiError(404, 'unknown-device', 'x')));
    const api = projectApi({ device });
    const t = await open({ api, rows: createRows({ bridge: BRIDGE.uid, api }), sel: 'line:7.7' });
    assert.equal(listRows(t.element).length, 2, 'the line still lists both devices');
    assert.deepEqual(openhab(t.element).slice(0, 2), ['Devices', '1 of 1 devices are in openHAB']);
});

test('tabs: Channels says so when an address could not be read or no channel holds it, and without the root of openHAB names are plain text', async () => {
    const t = await open({ api: projectApi({ uses: { '31/7/3': new KnxApiError(0, 'network'), '31/7/1': [] } }), sel: 'device:P-7_D-1' });
    tab(t.element, 'Channels').click();
    await settle();
    assert.deepEqual(listRows(t.element).map((row) => row.children[4].textContent), ['could not be read', 'No channel of this bridge holds the address.']);
    listRows(t.element)[0].fire('click');
    assert.deepEqual(openhab(t.element).slice(2, 4), ['Used by', 'openHAB cannot be reached.']);
    assert.deepEqual(t.errors.filter(Boolean), []);
    const plain = await open({ api: projectApi({ uses: USES }), sel: 'device:P-7_D-1' });
    tab(plain.element, 'Channels').click();
    await settle();
    assert.deepEqual(links(plain.element.querySelector('.list')), []);
    assert.equal(listRows(plain.element)[0].children[4].textContent, 'Kitchen <i>actuator</i>');
});

test('tabs: Channels reads the addresses a few at a time, the first 20 at first and 20 more with each Show more; a device left behind draws nothing', async () => {
    const count = 45;
    const addresses = Array.from({ length: count }, (_, n) => ({ address: `31/6/${n}`, name: `Made-up ${n}`, configured: true }));
    const waiting = [];
    let running = 0;
    let most = 0;
    const uses = (address) =>
        new Promise((resolve) => {
            running++;
            most = Math.max(most, running);
            waiting.push(() => {
                running--;
                resolve([{ thingUID: 'knx:device:line7:made-up', channelId: `c${address.split('/')[2]}`, channelType: 'switch', role: 'main', items: [] }]);
            });
        });
    const device = { ...ACTUATOR, groupObjects: [object(1, 'Many', addresses.map((item) => item.address))] };
    const t = await open({ api: fakeApi({ addresses, device, uses }), sel: 'device:P-7_D-1' });
    tab(t.element, 'Channels').click();
    const release = async () => {
        while (waiting.length > 0) {
            waiting.shift()();
            await settle();
        }
    };
    await settle();
    assert.equal(USES_AT_ONCE, 4);
    assert.equal(running, 4);
    await release();
    assert.equal(most, 4);
    const asked = () => t.api.calls.filter(([name]) => name === 'groupAddress').length;
    assert.equal(USES_ROWS, 20);
    assert.equal(asked(), 20);
    assert.equal(listRows(t.element).length, count);
    assert.equal(listRows(t.element)[19].children[5].textContent, 'c19');
    assert.equal(listRows(t.element)[20].children[4].textContent, 'not read yet');
    const note = t.element.querySelector('.channels-note');
    assert.equal(note.querySelector('span').textContent, 'Thing, channel and Items were read for 20 of 45 addresses.');
    note.querySelector('.more').click();
    await settle();
    await release();
    assert.equal(asked(), 40);
    note.querySelector('.more').focus();
    note.querySelector('.more').click();
    await settle();
    assert.equal(note.querySelector('.more'), null);
    assert.ok(t.document.activeElement === listRows(t.element)[0], 'the button left: the focus goes to the list');
    // the answers of a device that was left do not come into the list of the next one
    t.view.update({ view: 'topology', bridge: BRIDGE.uid, query: { sel: 'line:7.7' } });
    await release();
    assert.equal(asked(), 45);
    assert.equal(headings(t.element)[0], 'Address');
    assert.equal(t.element.querySelector('.channels-note'), null);
    assert.deepEqual(t.errors.filter(Boolean), []);
    t.view.close();
    assert.equal(t.document.listenerCount(), 0);
});

test('openhab: the container says how many addresses of a device and how many devices of a place are in openHAB, with "Add to openHAB"', async () => {
    const t = await open({ api: projectApi(), sel: 'line:7.7' });
    // an address two devices share counts once
    assert.deepEqual(openhab(t.element), ['Devices', '1 of 2 devices are in openHAB', 'Group Addresses', '2 of 4 linked group addresses are in openHAB']);
    listRows(t.element)[0].fire('click');
    assert.deepEqual(openhab(t.element), ['Channel', '2 of 3 linked group addresses are in openHAB', 'Add to openHAB…']);
    assert.equal(t.element.querySelector('.openhab').querySelector('.add').getAttribute('href'), '#/b/knx%3Aip%3Aline7/wizard?device=P-7_D-1');
    listRows(t.element)[1].fire('click');
    assert.deepEqual(openhab(t.element), ['Channel', '0 of 2 linked group addresses are in openHAB', 'Add to openHAB…']);
    t.view.update({ view: 'topology', bridge: BRIDGE.uid, query: {} });
    assert.equal(t.element.querySelector('.openhab').textContent, 'Nothing is selected.');
    const removed = await open({ bridge: REMOVED, api: projectApi(), sel: 'line:7.7' });
    listRows(removed.element)[0].fire('click');
    assert.equal(removed.element.querySelector('.openhab').textContent, 'Nothing is selected.', 'a removed bridge has no channels and no wizard');
});

test('live: the container shows the newest telegrams of the selected device, by its address or its linked addresses, and only reads', async () => {
    const t = await open({ api: projectApi(), sel: 'line:7.7' });
    const live = () => t.element.querySelector('.live');
    assert.equal(live().textContent, 'Select a group address or a device to see its telegrams.');
    t.rows.add(telegram(21, '31/7/3', { source: '7.7.99', value: 'ON' }));
    t.rows.add(telegram(22, '31/7/9', { source: '7.7.1', value: 'OFF' }));
    t.rows.add(telegram(23, '31/7/4', { source: '7.7.98', value: '7' }));
    const before = t.api.calls.length;
    listRows(t.element)[0].fire('click');
    const shown = () => live().querySelectorAll('li').map((line) => [line.querySelector('.source').textContent, line.querySelector('.destination').textContent, line.querySelector('.value').textContent]);
    assert.deepEqual(shown(), [['7.7.1', '31/7/9', 'OFF'], ['7.7.99', '31/7/3', 'ON']]);
    t.rows.add(telegram(24, '31/7/1', { source: '7.7.97', value: 'ON' }));
    t.runFrames();
    assert.equal(shown().length, 3);
    listRows(t.element)[1].fire('click');
    assert.deepEqual(shown(), [['7.7.98', '31/7/4', '7']]);
    assert.equal(t.api.calls.length, before, 'no call is made for it');
    assert.ok(t.api.calls.every(([name]) => ['topology', 'groupAddresses', 'device', 'channels'].includes(name)), 'and the panel made only calls that read');
    assert.equal(t.stream.starts, 1, 'the stream the monitor uses');
});

test('focus: Back, or "Open" in Properties, shows another selection, and a focus that lost its place goes to the list', async () => {
    const t = await open({ api: projectApi(), sel: 'line:7.7' });
    listRows(t.element)[0].fire('click');
    const link = t.element.querySelector('.properties').querySelector('.open');
    link.focus();
    t.view.update({ view: 'topology', bridge: BRIDGE.uid, query: { sel: 'device:P-7_D-1' } });
    assert.ok(t.document.activeElement === t.element.querySelector('.list'), 'the list itself while the objects are read');
    await settle();
    assert.ok(t.document.activeElement === listRows(t.element)[0]);
    listRows(t.element)[2].focus();
    t.view.update({ view: 'topology', bridge: BRIDGE.uid, query: { sel: 'line:7.7' } });
    assert.ok(t.document.activeElement === listRows(t.element)[0], 'Back from the device: the row that held the focus is gone');
    // a focus outside the panel, or one that keeps its place, is left alone
    t.document.activeElement = null;
    t.view.update({ view: 'topology', bridge: BRIDGE.uid, query: { sel: 'area:7' } });
    assert.equal(t.document.activeElement, null);
    const find = t.element.querySelector('.find-slot').querySelector('input');
    find.focus();
    t.view.update({ view: 'topology', bridge: BRIDGE.uid, query: { sel: 'line:7.7' } });
    assert.ok(t.document.activeElement === find);
});

test('tabs: the objects of a place are made when their tab is shown, and a list of more than 500 objects starts with every device folded', async () => {
    const objects = Array.from({ length: 251 }, (_, n) => object(n, `Object ${n}`, []));
    const api = fakeApi({ addresses: IN_USE, device: (id) => ({ ...(id === 'P-7_D-1' ? ACTUATOR : SENSOR), id, groupObjects: objects }) });
    const t = await open({ api, sel: 'line:7.7' });
    assert.deepEqual(tabs(t.element), [['Devices (2)', 'true'], ['Group Objects (502)', 'false']]);
    const made = () => [...t.document.nodes].filter((node) => node.tagName === 'TR' && node.classList.contains('group-row')).length;
    assert.equal(made(), 0, 'nothing of the other tab is made yet');
    tab(t.element, 'Group Objects').click();
    assert.deepEqual(outline(t.element), ['# 7.7.1 Kitchen <b>actuator</b> (251)', '# 7.7.12 Hall sensor (251)']);
    assert.deepEqual(t.statuses.at(-1), { selection: '7.7 Ground floor', shown: 0, total: 502 });
    listRows(t.element)[1].querySelector('.fold').click();
    assert.equal(listRows(t.element).length, 253);
    // what the viewer unfolded stays open when the list is drawn again
    tab(t.element, 'Devices').click();
    tab(t.element, 'Group Objects').click();
    assert.equal(listRows(t.element).length, 253);
    // the device of the row the address names starts open: the row is to be seen
    const named = await open({ api, sel: 'line:7.7' });
    named.view.update({ view: 'topology', bridge: BRIDGE.uid, query: { sel: 'line:7.7', tab: 'objects', row: 'object:P-7_D-2|3' } });
    assert.deepEqual(tabs(named.element)[1], ['Group Objects (502)', 'true']);
    assert.equal(listRows(named.element).length, 253);
    // also a row named later, inside the list that is shown already (Back to a row under a folded heading)
    named.view.update({ view: 'topology', bridge: BRIDGE.uid, query: { sel: 'line:7.7', tab: 'objects', row: 'object:P-7_D-1|5' } });
    assert.equal(listRows(named.element).length, 504);
    assert.equal(listRows(named.element)[6].getAttribute('aria-selected'), 'true');
    assert.equal(named.element.querySelector('.properties').querySelector('.subject').textContent, '5 Object 5');
    const small = await open({ api: projectApi(), sel: 'line:7.7' });
    tab(small.element, 'Group Objects').click();
    assert.equal(outline(small.element).length, 6, 'a short list starts open');
});

test('tabs: the same device with another row or tab in its address is shown without being read again', async () => {
    const t = await open({ api: projectApi(), sel: 'device:P-7_D-1' });
    const reads = () => t.api.calls.filter(([name, , id]) => (name === 'device' || name === 'channels') && id === 'P-7_D-1').length;
    const before = reads();
    t.view.update({ view: 'topology', bridge: BRIDGE.uid, query: { sel: 'device:P-7_D-1', row: 'object:2' } });
    assert.equal(listRows(t.element)[2].getAttribute('aria-selected'), 'true');
    t.view.update({ view: 'topology', bridge: BRIDGE.uid, query: { sel: 'device:P-7_D-1', tab: 'channels' } });
    assert.deepEqual(tabs(t.element).map(([, on]) => on), ['false', 'true']);
    assert.equal(t.element.querySelector('.properties').querySelector('.subject').textContent, '7.7.1 Kitchen <b>actuator</b>');
    t.view.update({ view: 'topology', bridge: BRIDGE.uid, query: { sel: 'device:P-7_D-1', row: 'ga:31/7/1' } });
    assert.equal(listRows(t.element)[1].getAttribute('aria-selected'), 'true');
    t.view.update({ view: 'topology', bridge: BRIDGE.uid, query: { sel: 'device:P-7_D-1' } });
    assert.deepEqual(tabs(t.element).map(([, on]) => on), ['true', 'false']);
    assert.equal(listRows(t.element).every((row) => row.getAttribute('aria-selected') === 'false'), true);
    await settle();
    assert.equal(reads(), before);
    // a device opened by its address keeps that name for the wizard, in the bar and in the openHAB container
    const byAddress = await open({ api: fakeApi({ addresses: IN_USE, device: () => DETAILS['P-7_D-1'] }), sel: 'device:7.7.1' });
    assert.deepEqual(byAddress.element.querySelectorAll('.add').map((link) => link.getAttribute('href')), ['#/b/knx%3Aip%3Aline7/wizard?device=7.7.1', '#/b/knx%3Aip%3Aline7/wizard?device=7.7.1']);
});

test('topology: a double click on an address under "Linked with" or "Other Linked" shows it in the other panel; elsewhere in the row nothing new happens', async () => {
    const t = await openDevice();
    const jumps = [];
    t.context.jump = (...parts) => jumps.push(parts);
    const row = listRows(t.element).find((line) => line.children[1].textContent === 'Switch A');
    row.children[3].querySelector('.address').fire('dblclick');
    assert.deepEqual(jumps, [[{ view: 'group-addresses', bridge: 'knx:ip:line7', query: { sel: 'ga:31/7/1' } }]]);
    // the name beside the address belongs to it; the cell itself to the sending address
    row.children[3].querySelector('.name').fire('dblclick');
    row.children[3].fire('dblclick');
    assert.deepEqual(jumps.slice(1).map(([route]) => route.query.sel), ['ga:31/7/1', 'ga:31/7/1']);
    row.children[4].querySelector('.address').fire('dblclick');
    assert.deepEqual(jumps.at(-1)[0].query, { sel: 'ga:31/7/3' });
    const before = jumps.length;
    row.children[1].fire('dblclick');
    row.children[4].fire('dblclick');
    listRows(t.element).find((line) => line.children[1].textContent === 'Alternative').children[3].fire('dblclick');
    assert.equal(jumps.length, before);
    assert.deepEqual(t.navigations, []);
    // without a shell that knows of two panels, the address opens in this one as Group Addresses
    delete t.context.jump;
    row.children[3].querySelector('.address').fire('dblclick');
    assert.deepEqual(t.navigations, [{ view: 'group-addresses', bridge: 'knx:ip:line7', query: { sel: 'ga:31/7/1' } }]);
});

test('opening: Topology shows its name and the areas open, so every line is in view; a line is closed, and what was opened comes back on the next visit', async () => {
    const kept = new Map();
    const layout = createLayout({ read: () => kept.get('layout') ?? null, write: (value) => kept.set('layout', value) });
    const t = await open({ layout });
    assert.deepEqual(treeItems(t.element), ['Topology', '7 Main <area>', '7.1 Backbone side', '7.7 Ground floor', '7.9 Empty line', 'Dynamic Folders']);
    assert.equal(treeItem(t.element, 'Topology').getAttribute('aria-selected'), 'true');
    assert.equal(treeItem(t.element, '7.7 Ground floor').getAttribute('aria-expanded'), 'false');
    assert.equal(kept.size, 0, 'nothing is kept before the viewer opens or closes a node');
    treeItem(t.element, '7.7 Ground floor').querySelector('.twisty').fire('click');
    assert.deepEqual(JSON.parse(kept.get('layout')).trees, { 'topology|knx:ip:line7': { open: ['line:7.7'], closed: [] } });
    t.view.close();
    const again = await open({ layout });
    assert.equal(treeItem(again.element, '7.7 Ground floor').getAttribute('aria-expanded'), 'true');
    assert.equal(treeItem(again.element, '7.1 Backbone side').getAttribute('aria-expanded'), 'false');
    const other = await open({ layout, bridge: { ...BRIDGE, uid: 'knx:ip:other' } });
    assert.equal(treeItem(other.element, '7.7 Ground floor').getAttribute('aria-expanded'), 'false', 'kept per bridge');
});

test('topology: the order of devices puts those without an address last, by name; a device\'s route names it in Topology', () => {
    const sorted = [{ name: 'b' }, { address: '31.1.10', name: 'x' }, { name: 'A' }, { address: '31.1.9', name: 'y' }].sort(deviceOrder);
    assert.deepEqual(sorted.map((device) => device.name), ['y', 'x', 'A', 'b']);
    assert.deepEqual(deviceRoute(BRIDGE, { address: '31.1.4' }), { view: 'topology', bridge: 'knx:ip:line7', query: { sel: 'device:31.1.4' } });
});

test('topology: projectMissing knows the states of call 8 and the codes of the lists, nothing else', () => {
    const document = createDocument();
    for (const reason of ['NONE', 'no-project', 'UNREADABLE', 'NEWER_VERSION', 'project-unusable']) {
        assert.equal(projectMissing(document, BRIDGE, reason)?.querySelector('a').getAttribute('href'), '#/b/knx%3Aip%3Aline7/project', reason);
    }
    for (const reason of ['LOADED', 'network', 'unknown-bridge', undefined, 'constructor']) {
        assert.equal(projectMissing(document, BRIDGE, reason), null, String(reason));
    }
});

test('topology: the address of the old Devices panel with a row opens Topology\'s list of every device with that row selected', async () => {
    const route = parse('#/b/knx%3Aip%3Aline7/devices?row=device%3AP-7_D-2');
    assert.equal(route.view, 'topology');
    const t = await open({ row: route.query.row });
    assert.equal(treeItem(t.element, 'Topology').getAttribute('aria-selected'), 'true');
    const chosen = listRows(t.element).filter((row) => row.getAttribute('aria-selected') === 'true');
    assert.deepEqual(chosen.map((row) => row.children[0].textContent), ['7.7.12']);
    assert.equal(listRows(t.element).length, 4, 'every device');
});
