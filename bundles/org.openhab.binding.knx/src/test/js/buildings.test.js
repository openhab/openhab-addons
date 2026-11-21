import { test } from 'node:test';
import assert from 'node:assert/strict';

import { KnxApiError } from '../web/js/api.js';
import { parse } from '../web/js/router.js';
import { show } from '../web/js/views/buildings.js';
import { textNodes } from './fakedom.js';
import { settle } from './fakes.js';
import { viewContext } from './fakeview.js';

const BRIDGE = { uid: 'knx:ip:line7', label: 'Line 7', present: true, status: 'ONLINE', bufferSize: 2000, projectState: 'LOADED' };
const REMOVED = { uid: 'knx:ip:gone', present: false, projectState: 'LOADED' };

function device(id, address, name, more = {}) {
    const made = { id, name, objects: 1, linkedObjects: 1, ...more };
    if (address) {
        made.address = address;
    }
    return made;
}

const TOPOLOGY = {
    areas: [
        {
            address: '7',
            name: 'Main <area>',
            lines: [{ address: '7.7', name: 'Ground floor line', devices: [device('P-7_D-2', '7.7.12', 'Hall switch', { room: 'Store' }), device('P-7_D-1', '7.7.2', 'Kitchen <b>actuator</b>', { room: 'Kitchen <i>east</i>' })] }],
        },
    ],
    unplacedDevices: [device('P-7_D-9', null, 'Loose sensor')],
};

const LOCATIONS = [
    {
        id: 'P-7_B-1',
        name: 'House',
        type: 'Building',
        spaces: [
            {
                id: 'P-7_B-2',
                name: 'Kitchen <i>east</i>',
                type: 'Room',
                number: '0.1',
                usage: 'Cooking',
                spaces: [],
                devices: ['P-7_D-1', 'P-7_D-404'],
                functions: [
                    {
                        id: 'P-7_F-1',
                        name: 'Ceiling light',
                        type: 'SwitchableLight',
                        groupAddresses: [
                            { groupAddress: '31/7/1', name: 'Kitchen light', role: 'SwitchOnOff' },
                            { name: 'Address not in the project' },
                        ],
                    },
                ],
            },
            { id: 'P-7_B-3', name: 'Store', type: 'Room', spaces: [], devices: ['P-7_D-2'], functions: [] },
            { id: 'P-7_B-4', name: 'Attic', type: 'Room', spaces: [], devices: [], functions: [] },
        ],
        devices: [],
        functions: [],
    },
];

function fakeApi({ topology = TOPOLOGY, locations = LOCATIONS } = {}) {
    const api = {
        calls: [],
        async topology(bridge) {
            api.calls.push(['topology', bridge]);
            if (topology instanceof Error) {
                throw topology;
            }
            return topology;
        },
        async locations(bridge) {
            api.calls.push(['locations', bridge]);
            if (locations instanceof Error) {
                throw locations;
            }
            return locations;
        },
        async device(bridge, id) {
            api.calls.push(['device', bridge, id]);
            return { ...device(id, '7.7.2', 'Kitchen <b>actuator</b>'), groupObjects: [{ number: 0, name: 'Channel A', groupAddresses: [] }] };
        },
        async deviceChannels() {
            return [];
        },
    };
    return api;
}

async function open({ bridge = BRIDGE, api = fakeApi(), sel, row, layout } = {}) {
    const harness = viewContext({ bridge, api, layout, route: { view: 'buildings', bridge: bridge.uid, query: { ...(sel ? { sel } : {}), ...(row ? { row } : {}) } } });
    const view = show(harness.context);
    await settle();
    return Object.assign(harness, { view, api });
}

function words(node) {
    return textNodes(node).filter((part) => part !== '').join(' ');
}

function treeItems(element) {
    return element.querySelector('.tree').querySelectorAll('li').map((item) => words(item.querySelector('.node')));
}

function treeItem(element, name) {
    return element.querySelector('.tree').querySelectorAll('li').find((item) => words(item.querySelector('.node')) === name);
}

function tables(element) {
    return element.querySelector('.list').querySelectorAll('table');
}

function rowsOf(table) {
    return table.querySelector('tbody').children.map((row) => row.children.map((cell) => cell.textContent));
}

function headings(table) {
    return table.querySelector('thead').querySelectorAll('th').map((cell) => cell.textContent);
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

function addLinks(node) {
    return node.querySelectorAll('.add').map((link) => [link.textContent, link.getAttribute('href')]);
}

test('buildings: the tree as the project has it, with the devices and functions of a room under it', async () => {
    const b = await open({ sel: 'function:P-7_F-1' });
    assert.equal(b.element.querySelector('h2').textContent, 'Buildings');
    assert.deepEqual(b.api.calls, [
        ['topology', 'knx:ip:line7'],
        ['locations', 'knx:ip:line7'],
    ]);
    assert.deepEqual(treeItems(b.element), [
        'Buildings',
        'House',
        'Kitchen <i>east</i> 0.1',
        '7.7.2 Kitchen <b>actuator</b>',
        'P-7_D-404',
        'Ceiling light SwitchableLight',
        'Store',
        'Attic',
        'Dynamic Folders',
    ]);
    assert.ok(treeItem(b.element, 'House').querySelector('.icon-building'));
    assert.equal(treeItem(b.element, 'House').querySelector('.kind-icon').getAttribute('title'), 'Building');
    assert.ok(treeItem(b.element, 'Store').querySelector('.icon-room'));
    assert.equal(treeItem(b.element, 'Store').querySelector('.kind-icon').getAttribute('aria-label'), 'Room');
    assert.ok(treeItem(b.element, 'Ceiling light SwitchableLight').querySelector('.icon-function'));
    assert.equal(treeItem(b.element, 'Kitchen <i>east</i> 0.1').querySelector('.name').children.length, 0);
});

test('buildings: with nothing selected the first node is selected and the list holds every device with its room', async () => {
    const b = await open();
    assert.deepEqual(treeItems(b.element), ['Buildings', 'House', 'Kitchen <i>east</i> 0.1', 'Store', 'Attic', 'Dynamic Folders']);
    assert.equal(treeItem(b.element, 'Buildings').getAttribute('aria-selected'), 'true');
    assert.deepEqual(tabs(b.element)[0], ['Devices (4)', 'true']);
    const [devices] = tables(b.element);
    assert.equal(tables(b.element).length, 1);
    assert.deepEqual(headings(devices), ['Address', 'Room', 'Name', 'Description', 'Product', 'Order Number', 'Objects', 'Linked', 'openHAB']);
    assert.deepEqual(rowsOf(devices).map((row) => row.slice(0, 3)), [
        ['7.7.2', 'Kitchen <i>east</i>', 'Kitchen <b>actuator</b>'],
        ['7.7.12', 'Store', 'Hall switch'],
        ['–', '', 'P-7_D-404'],
        ['–', '', 'Loose sensor'],
    ]);
    assert.deepEqual(b.statuses.at(-1), { selection: 'Buildings', shown: 4, total: 4 });
});

test('buildings: a space lists the devices and the functions below it, and Properties offer "Add to openHAB" for it', async () => {
    const b = await open({ sel: 'space:P-7_B-1' });
    assert.deepEqual(tabs(b.element), [['Parts (3)', 'true'], ['Devices (3)', 'false'], ['Group Objects (2)', 'false'], ['Functions (1)', 'false']]);
    tab(b.element, 'Devices').click();
    assert.equal(tables(b.element).length, 1, 'one list at a time');
    assert.equal(rowsOf(tables(b.element)[0]).length, 3);
    assert.equal(headings(tables(b.element)[0])[0], 'Address');
    tab(b.element, 'Functions').click();
    assert.deepEqual(tabs(b.element).map(([, on]) => on), ['false', 'false', 'false', 'true']);
    const [functions] = tables(b.element);
    assert.deepEqual(headings(functions), ['Name', 'Type', 'Group Addresses']);
    assert.deepEqual(rowsOf(functions), [['Ceiling light', 'SwitchableLight', '2']]);
    assert.deepEqual(b.replacements.at(-1), { view: 'buildings', bridge: 'knx:ip:line7', query: { sel: 'space:P-7_B-1', tab: 'functions' } });
    assert.deepEqual(facts(b.element), [
        ['Settings', 'Name', 'House', 'Type', 'Building'],
        ['Information', 'Devices', '3'],
    ]);
    assert.deepEqual(openhab(b.element), ['Add to openHAB…']);
    b.view.update({ view: 'buildings', bridge: BRIDGE.uid, query: { sel: 'space:P-7_B-2' } });
    assert.deepEqual(rowsOf(tables(b.element)[0]).map((row) => row[2]), ['Kitchen <b>actuator</b>', 'P-7_D-404']);
    assert.equal(tables(b.element)[0].querySelector('a'), null);
    assert.deepEqual(facts(b.element)[0], ['Settings', 'Name', 'Kitchen <i>east</i>', 'Type', 'Room', 'Number', '0.1', 'Usage', 'Cooking']);
    assert.deepEqual(addLinks(b.element), [
        ['Add to openHAB…', '#/b/knx%3Aip%3Aline7/wizard?space=P-7_B-2'],
        ['Add to openHAB…', '#/b/knx%3Aip%3Aline7/wizard?space=P-7_B-2'],
    ]);
    b.view.update({ view: 'buildings', bridge: BRIDGE.uid, query: { sel: 'space:P-7_B-4' } });
    assert.deepEqual(tabs(b.element), [['Devices (0)', 'true'], ['Group Objects (0)', 'false']], 'no Functions tab without functions');
    assert.equal(b.element.querySelector('.empty').textContent, 'No device is here.');
});

test('buildings: a function row shows in Properties; opened, the function lists its group addresses with their roles', async () => {
    const b = await open({ sel: 'space:P-7_B-2' });
    tab(b.element, 'Functions').click();
    const row = tables(b.element)[0].querySelector('tbody').children[0];
    row.fire('click');
    assert.deepEqual(facts(b.element), [
        ['Settings', 'Name', 'Ceiling light', 'Type', 'SwitchableLight'],
        ['Information', 'Group Addresses', '2'],
    ]);
    assert.deepEqual(openhab(b.element), ['Add to openHAB…']);
    assert.deepEqual(addLinks(b.element).map(([, link]) => link), ['#/b/knx%3Aip%3Aline7/wizard?function=P-7_F-1', '#/b/knx%3Aip%3Aline7/wizard?function=P-7_F-1']);
    row.fire('dblclick');
    assert.deepEqual(b.navigations.at(-1), { view: 'buildings', bridge: 'knx:ip:line7', query: { sel: 'function:P-7_F-1' } });
    assert.equal(treeItem(b.element, 'Ceiling light SwitchableLight').getAttribute('aria-selected'), 'true');
    const [addresses] = tables(b.element);
    assert.deepEqual(headings(addresses), ['Address', 'Name', 'Role']);
    assert.deepEqual(rowsOf(addresses), [
        ['31/7/1', 'Kitchen light', 'SwitchOnOff'],
        ['–', 'Address not in the project', ''],
    ]);
    assert.deepEqual(b.statuses.at(-1), { selection: 'Ceiling light', shown: 2, total: 2 });
});

test('buildings: a device of a room opens its Group Objects here', async () => {
    const b = await open({ sel: 'space:P-7_B-2' });
    treeItem(b.element, 'Kitchen <i>east</i> 0.1').querySelector('.twisty').fire('click');
    treeItem(b.element, '7.7.2 Kitchen <b>actuator</b>').querySelector('.name').fire('click');
    await settle();
    assert.deepEqual(b.navigations.at(-1), { view: 'buildings', bridge: 'knx:ip:line7', query: { sel: 'device:P-7_D-1' } });
    assert.deepEqual(b.api.calls.at(-1), ['device', 'knx:ip:line7', 'P-7_D-1']);
    assert.equal(headings(tables(b.element)[0])[0], 'Number');
    assert.deepEqual(rowsOf(tables(b.element)[0]).map((row) => row.slice(0, 2)), [['0', 'Channel A']]);
    assert.deepEqual(addLinks(b.element)[0], ['Add to openHAB…', '#/b/knx%3Aip%3Aline7/wizard?device=P-7_D-1']);
});

test('buildings: every room, function and device of a present bridge leads to the wizard with its id; a removed bridge has no such link', async () => {
    const b = await open();
    const found = [];
    for (const sel of ['space:P-7_B-1', 'space:P-7_B-2', 'function:P-7_F-1', 'space:P-7_B-3']) {
        b.view.update({ view: 'buildings', bridge: BRIDGE.uid, query: { sel } });
        found.push(addLinks(b.element.querySelector('.tools'))[0]);
    }
    assert.deepEqual(found, [
        ['Add to openHAB…', '#/b/knx%3Aip%3Aline7/wizard?space=P-7_B-1'],
        ['Add to openHAB…', '#/b/knx%3Aip%3Aline7/wizard?space=P-7_B-2'],
        ['Add to openHAB…', '#/b/knx%3Aip%3Aline7/wizard?function=P-7_F-1'],
        ['Add to openHAB…', '#/b/knx%3Aip%3Aline7/wizard?space=P-7_B-3'],
    ]);
    for (const [, link] of found) {
        assert.equal(parse(link).view, 'wizard');
    }
    b.view.update({ view: 'buildings', bridge: BRIDGE.uid, query: {} });
    assert.deepEqual(addLinks(b.element), []);
    const removed = await open({ bridge: REMOVED, sel: 'space:P-7_B-2' });
    assert.deepEqual(addLinks(removed.element), []);
    assert.deepEqual(removed.api.calls.map((call) => call[1]), ['knx:ip:gone', 'knx:ip:gone']);
    tables(removed.element)[0].querySelector('tbody').children[0].fire('click');
    assert.ok(removed.element.querySelector('.properties').querySelector('.open').getAttribute('href').startsWith('#/b/knx%3Aip%3Agone/buildings?sel=device'));
});

test('buildings: a project without a building structure says so', async () => {
    const b = await open({ api: fakeApi({ locations: [] }) });
    assert.deepEqual(b.element.querySelector('.notes').querySelectorAll('.note').map((note) => note.textContent), ['The project has no building structure.']);
    assert.deepEqual(treeItems(b.element), ['Buildings', 'Dynamic Folders']);
});

test('buildings: without a project one text and the link to Project', async () => {
    for (const code of ['no-project', 'project-unusable']) {
        const error = new KnxApiError(code === 'no-project' ? 404 : 409, code, 'x');
        const b = await open({ api: fakeApi({ topology: error, locations: error }) });
        assert.equal(b.element.querySelectorAll('.missing').length, 1, code);
        assert.equal(b.element.querySelector('.missing').querySelector('a').getAttribute('href'), '#/b/knx%3Aip%3Aline7/project');
        assert.equal(b.element.querySelector('.list').querySelector('table'), null);
        assert.deepEqual(b.errors, []);
    }
});

test('buildings: one failing call shows its error, the other part is shown; room devices then show their ids', async () => {
    const b = await open({ api: fakeApi({ topology: new KnxApiError(0, 'network') }), sel: 'space:P-7_B-2' });
    assert.deepEqual(b.element.querySelector('.notes').querySelectorAll('.note').map((note) => note.textContent), ['openHAB cannot be reached.']);
    assert.deepEqual(rowsOf(tables(b.element)[0]).map((row) => row[2]), ['P-7_D-1', 'P-7_D-404']);
    const rooms = await open({ api: fakeApi({ locations: new KnxApiError(500, 'internal', 'x') }) });
    assert.deepEqual(rooms.element.querySelector('.notes').querySelectorAll('.note').map((note) => note.textContent), ['Something went wrong inside the KNX add-on.']);
    assert.deepEqual(treeItems(rooms.element), ['Buildings', 'Dynamic Folders']);
});

test('buildings: close() before the answers draws nothing and leaves no listener', async () => {
    const answers = [];
    const api = {
        topology: () => new Promise((resolve) => answers.push(() => resolve(TOPOLOGY))),
        locations: () => new Promise((resolve) => answers.push(() => resolve(LOCATIONS))),
    };
    const harness = viewContext({ bridge: BRIDGE, api });
    const view = show(harness.context);
    await settle();
    view.close();
    for (const answer of answers) {
        answer();
    }
    await settle();
    assert.equal(harness.element.querySelector('.list').querySelector('table'), null);
    assert.equal(harness.element.querySelector('.tree').children.length, 0);
    assert.equal(harness.document.listenerCount(), 0);
    assert.equal(harness.clock.count(), 0);
});

test('buildings: a function row only selects on a click; Properties has "Open" and the address names the row', async () => {
    const b = await open({ sel: 'space:P-7_B-2' });
    tab(b.element, 'Functions').click();
    tables(b.element)[0].querySelector('tbody').children[0].children[0].fire('click');
    assert.deepEqual(b.navigations, []);
    assert.equal(b.element.querySelector('.properties').querySelector('.open').getAttribute('href'), '#/b/knx%3Aip%3Aline7/buildings?sel=function%3AP-7_F-1');
    assert.deepEqual(b.replacements, [
        { view: 'buildings', bridge: BRIDGE.uid, query: { sel: 'space:P-7_B-2', tab: 'functions' } },
        { view: 'buildings', bridge: BRIDGE.uid, query: { sel: 'space:P-7_B-2', tab: 'functions', row: 'function:P-7_F-1' } },
    ]);
    // an address made before the tabs names the row only: the tab that holds it is shown
    const again = await open({ sel: 'space:P-7_B-2', row: 'function:P-7_F-1' });
    assert.deepEqual(tabs(again.element).map(([, on]) => on), ['false', 'false', 'true']);
    assert.equal(tables(again.element)[0].querySelector('tbody').children[0].getAttribute('aria-selected'), 'true');
    assert.equal(again.element.querySelector('.properties').querySelector('.subject').textContent, 'Ceiling light');
});

test('buildings: a group address of an opened function only selects on a click; Properties has "Open", the address names the row, Enter opens it', async () => {
    const b = await open({ sel: 'function:P-7_F-1' });
    const body = () => tables(b.element)[0].querySelector('tbody');
    body().children[0].children[1].fire('click');
    assert.deepEqual(b.navigations, []);
    assert.equal(body().children[0].getAttribute('aria-selected'), 'true');
    assert.deepEqual(facts(b.element), [['Settings', 'Address', '31/7/1', 'Name', 'Kitchen light', 'Role', 'SwitchOnOff']]);
    const target = { view: 'group-addresses', bridge: BRIDGE.uid, query: { sel: 'ga:31/7/1' } };
    const link = b.element.querySelector('.properties').querySelector('.open').getAttribute('href');
    assert.deepEqual(parse(link), target);
    assert.deepEqual(b.replacements, [{ view: 'buildings', bridge: BRIDGE.uid, query: { sel: 'function:P-7_F-1', row: 'fga:31/7/1' } }]);

    body().children[0].fire('keydown', { key: 'Enter' });
    assert.deepEqual(b.navigations, [target]);
    body().children[0].children[0].fire('dblclick');
    assert.deepEqual(b.navigations, [target, target]);

    body().children[1].fire('click');
    assert.equal(b.element.querySelector('.properties').querySelector('.open'), null);
    assert.deepEqual(b.replacements.at(-1), { view: 'buildings', bridge: BRIDGE.uid, query: { sel: 'function:P-7_F-1' } });
    body().children[1].fire('keydown', { key: 'Enter' });
    assert.equal(b.navigations.length, 2);

    const again = await open({ sel: 'function:P-7_F-1', row: 'fga:31/7/1' });
    assert.equal(tables(again.element)[0].querySelector('tbody').children[0].getAttribute('aria-selected'), 'true');
    assert.equal(again.element.querySelector('.properties').querySelector('.subject').textContent, '31/7/1 Kitchen light');
    assert.deepEqual(again.replacements, []);

    const gone = await open({ sel: 'function:P-7_F-1', row: 'fga:31/7/9' });
    assert.equal(gone.element.querySelector('.properties').querySelector('.subject').textContent, 'Ceiling light');
});

test('buildings: the same group address twice in a function gives two rows that are selected apart', async () => {
    const twice = structuredClone(LOCATIONS);
    twice[0].spaces[0].functions[0].groupAddresses = [
        { groupAddress: '31/7/1', name: 'Kitchen light', role: 'SwitchOnOff' },
        { groupAddress: '31/7/1', name: 'Kitchen light', role: 'InfoOnOff' },
    ];
    const b = await open({ api: fakeApi({ locations: twice }), sel: 'function:P-7_F-1', row: 'fga:31/7/1:2' });
    const lines = tables(b.element)[0].querySelector('tbody').children;
    assert.deepEqual(lines.map((line) => line.getAttribute('aria-selected')), ['false', 'true']);
    assert.deepEqual(facts(b.element)[0].at(-1), 'InfoOnOff');
});

test('buildings: a selection the tree does not have says so', async () => {
    const b = await open({ sel: 'space:gone' });
    assert.equal(b.element.querySelector('.list').textContent, 'The project has nothing of this name.');
    assert.equal(tables(b.element).length, 0);
});

test('folders: Buildings has "Not assigned to a room", "Filter devices" and the openHAB folders in a closed group below the building', async () => {
    const topology = {
        areas: [
            {
                address: '7',
                name: 'Main',
                lines: [
                    {
                        address: '7.7',
                        name: 'Line',
                        devices: [...TOPOLOGY.areas[0].lines[0].devices, device('P-7_D-7', '7.7.0', 'Coupler dummy', { filterDevice: true }), device('P-7_D-8', '7.7.30', 'Garden pump')],
                    },
                ],
            },
        ],
        unplacedDevices: TOPOLOGY.unplacedDevices,
    };
    const b = await open({ api: fakeApi({ topology }) });
    assert.equal(treeItems(b.element).at(-1), 'Dynamic Folders');
    assert.equal(treeItem(b.element, 'Dynamic Folders').getAttribute('aria-expanded'), 'false');
    treeItem(b.element, 'Dynamic Folders').querySelector('.twisty').fire('click');
    assert.deepEqual(treeItems(b.element), ['Buildings', 'House', 'Kitchen <i>east</i> 0.1', 'Store', 'Attic', 'Dynamic Folders', 'Not assigned to a room', 'Filter devices', 'In openHAB', 'Not in openHAB']);
    const names = () => rowsOf(tables(b.element)[0]).map((row) => row[2]);
    b.view.update({ view: 'buildings', bridge: BRIDGE.uid, query: { sel: 'folder:no-room' } });
    assert.deepEqual(names(), ['Coupler dummyfilter device', 'Garden pump', 'Loose sensor'], 'every device of the project that stands in no space');
    assert.deepEqual(tabs(b.element)[0], ['Devices (3)', 'true']);
    assert.deepEqual(crumbs(b.element), ['Dynamic Folders', 'Not assigned to a room']);
    // such a device is reached from the folder, in the tree too
    treeItem(b.element, 'Not assigned to a room').querySelector('.twisty').fire('click');
    assert.ok(treeItem(b.element, '7.7.30 Garden pump'));
    b.view.update({ view: 'buildings', bridge: BRIDGE.uid, query: { sel: 'folder:filter' } });
    assert.deepEqual(names(), ['Coupler dummyfilter device']);
    b.view.update({ view: 'buildings', bridge: BRIDGE.uid, query: {} });
    assert.equal(names().length, 6, 'with nothing selected, every device');
    const removed = await open({ bridge: REMOVED, api: fakeApi({ topology }) });
    treeItem(removed.element, 'Dynamic Folders').querySelector('.twisty').fire('click');
    assert.deepEqual(treeItems(removed.element).slice(-3), ['Dynamic Folders', 'Not assigned to a room', 'Filter devices']);
});

test('crumbs: Buildings shows the way through building and room down to a device or a function', async () => {
    const b = await open({ sel: 'device:P-7_D-1' });
    await settle();
    assert.deepEqual(crumbs(b.element), ['Buildings', 'House', 'Kitchen <i>east</i>', '7.7.2 Kitchen <b>actuator</b>']);
    b.element.querySelector('.crumbs').querySelectorAll('button')[2].click();
    assert.deepEqual(b.navigations.at(-1), { view: 'buildings', bridge: 'knx:ip:line7', query: { sel: 'space:P-7_B-2' } });
    assert.deepEqual(tabs(b.element), [['Devices (2)', 'true'], ['Group Objects (1)', 'false'], ['Functions (1)', 'false']]);
    b.view.update({ view: 'buildings', bridge: BRIDGE.uid, query: { sel: 'function:P-7_F-1' } });
    assert.deepEqual(crumbs(b.element), ['Buildings', 'House', 'Kitchen <i>east</i>', 'Ceiling light']);
    assert.deepEqual(tabs(b.element), [['Group Addresses (2)', 'true']]);
});
