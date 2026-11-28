import { test } from 'node:test';
import assert from 'node:assert/strict';

import { KnxApiError } from '../web/js/api.js';
import { createRows } from '../web/js/rows.js';
import { createStream } from '../web/js/stream.js';
import { FIND_ROWS, SUGGESTION_MILLIS, show } from '../web/js/views/groupaddresses.js';
import { dptBits } from '../web/js/views/objects.js';
import { textNodes } from './fakedom.js';
import { createEventBody, createFetch, settle } from './fakes.js';
import { createLayout } from '../web/js/layout.js';
import { fakeStream, shareSidebar, viewContext } from './fakeview.js';

const PRESENT = { uid: 'knx:ip:line7', label: 'Line 7', present: true, status: 'ONLINE', bufferSize: 2000, projectState: 'LOADED' };
const OFF = { ...PRESENT, bufferSize: 0 };
const REMOVED = { uid: 'knx:ip:gone', present: false, projectState: 'LOADED' };

function telegram(number, destination, more = {}) {
    return {
        number,
        time: '2026-11-15T07:01:02.250Z',
        source: '7.7.3',
        destination,
        type: 'WRITE',
        direction: 'IN',
        payload: '01',
        value: 'ON',
        security: 'PLAIN',
        plainOnSecuredAddress: false,
        ...more,
    };
}

const ADDRESSES = [
    { address: '31/0/1', name: 'Kitchen <b>light</b>', description: 'Ceiling', dataType: '1.001', secured: false, range: ['House', 'Lights'], configured: true, last: telegram(3, '31/0/1') },
    { address: '31/0/2', name: 'Hall light', dataType: '1.001', secured: true, range: ['House', 'Lights'], configured: false },
    { address: '31/1/1', name: 'Bedroom blind', dataType: '1.008', secured: false, range: ['House', 'Blinds'] },
    { address: '31/7/9', name: 'Outside temperature', dataType: '9', secured: false, range: ['House', 'Weather'], last: telegram(4, '31/7/9', { value: undefined, payload: '0C1A' }) },
    { address: '31/7/10', name: 'No range', secured: false, range: [] },
];
const SEEN = [telegram(7, '31/7/20', { destinationName: 'Seen <i>one</i>', dataType: '1.001' }), telegram(8, '31/7/3', { value: '21.5', unit: '°C' })];
const DEVICES = [
    {
        id: 'P-7_DI-1',
        address: '7.7.1',
        name: 'Switch actuator',
        product: 'Relay 8x',
        groupObjects: [
            { number: 3, name: 'Status A', function: 'Status', dataType: '1.001', sizeBits: 1, communication: true, read: true, transmit: true, groupAddresses: ['31/0/2', '31/0/1'], sendingGroupAddress: '31/0/2' },
            { number: 0, name: 'Channel A', function: 'Switch', dataType: '1.001', sizeBits: 1, communication: true, write: true, update: true, groupAddresses: ['31/0/1'] },
        ],
    },
    {
        id: 'P-7_DI-2',
        address: '7.7.2',
        name: 'Push <button>',
        groupObjects: [{ number: 1, name: 'Rocker 1', text: 'Kitchen', function: 'Switch', dataType: '1.001', sizeBits: 1, communication: true, transmit: true, readOnInit: true, groupAddresses: ['31/0/1'], sendingGroupAddress: '31/0/1' }],
    },
];

function fakeApi({ project = { state: 'LOADED' }, addresses = ADDRESSES, seen = SEEN, devices = DEVICES, uses = {} } = {}) {
    const api = {
        calls: [],
        async groupAddress(bridge, address) {
            api.calls.push(['groupAddress', address]);
            const answer = typeof uses === 'function' ? uses(address) : uses[address];
            if (answer instanceof Error) {
                throw answer;
            }
            return { address, usedBy: answer ?? [] };
        },
        async project(bridge) {
            api.calls.push(['project', bridge]);
            if (project instanceof Error) {
                throw project;
            }
            return project;
        },
        async allGroupAddresses(bridge) {
            api.calls.push(['groupAddresses', bridge]);
            return addresses;
        },
        async lastValues(bridge) {
            api.calls.push(['lastValues', bridge]);
            if (seen instanceof Error) {
                throw seen;
            }
            return seen;
        },
        async allDevices(bridge) {
            api.calls.push(['devices', bridge]);
            if (devices instanceof Error) {
                throw devices;
            }
            return devices.map(({ groupObjects, ...summary }) => summary);
        },
        async device(bridge, id) {
            api.calls.push(['device', id]);
            return devices.find((device) => device.id === id);
        },
        async channels(bridge, list) {
            api.calls.push(['channels', list.join()]);
            return { suggestions: list.map((address) => ({ groupAddress: address, text: `Type switch : a "A" [ ga="1.001:${address}" ]` })), unknown: [] };
        },
    };
    return api;
}

async function open({ bridge = PRESENT, api = fakeApi(), stream = bridge.present ? fakeStream('live') : null, rows, sel, row, shared = false, base, layout } = {}) {
    const model = rows ?? createRows({ bridge: bridge.uid, api });
    const harness = viewContext({ bridge, api, rows: model, stream, layout, route: { view: 'group-addresses', bridge: bridge.uid, query: { ...(sel ? { sel } : {}), ...(row ? { row } : {}) } } });
    harness.context.base = base;
    const side = shared ? shareSidebar(harness) : null;
    const view = show(harness.context);
    await settle();
    harness.runFrames();
    return Object.assign(harness, { view, api, rows: model, stream, side });
}

function tableRows(element) {
    return element.querySelector('.list').querySelector('tbody')?.children ?? [];
}

function words(node) {
    return textNodes(node).filter((part) => part !== '').join(' ');
}

function addresses(element) {
    return tableRows(element).map((row) => row.querySelector('.address').textContent);
}

function rowOf(element, address) {
    return tableRows(element).find((row) => row.querySelector('.address')?.textContent === address);
}

function notes(element) {
    return element.querySelector('.notes').querySelectorAll('.note').map((note) => note.textContent);
}

function headings(element) {
    return (element.querySelector('.list').querySelector('thead')?.querySelectorAll('th') ?? []).map((cell) => cell.textContent);
}

function treeItems(element) {
    return element.querySelector('.tree').querySelectorAll('li').map((item) => textNodes(item.querySelector('.node')).join(' '));
}

function treeItem(element, words) {
    return element.querySelector('.tree').querySelectorAll('li').find((item) => textNodes(item.querySelector('.node')).join(' ') === words);
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

function crumbs(element) {
    return element.querySelector('.crumbs').querySelectorAll('button').map((crumb) => crumb.textContent);
}

function find(a, words) {
    const field = a.element.querySelector('.find-slot').querySelector('input');
    field.value = words;
    field.fire('input');
    a.runFrames();
}

const COLUMNS = ['Address', 'Name', 'Description', 'Data Type', 'Length', 'Security', 'Associations', 'Last Value', 'openHAB'];

test('group addresses: the tree of the project\'s ranges and, with nothing selected, the main groups in the list', async () => {
    const a = await open();
    assert.equal(a.element.querySelector('h2').textContent, 'Group Addresses');
    assert.deepEqual(a.api.calls.slice(0, 3), [
        ['project', 'knx:ip:line7'],
        ['groupAddresses', 'knx:ip:line7'],
        ['devices', 'knx:ip:line7'],
    ]);
    assert.deepEqual(treeItems(a.element), ['Group Addresses', '31 House', 'Without a group', 'Dynamic Folders']);
    assert.equal(treeItem(a.element, 'Group Addresses').getAttribute('aria-expanded'), 'true');
    assert.deepEqual(headings(a.element), COLUMNS);
    assert.deepEqual(tableRows(a.element).map(words), ['31 House', 'Without a group']);
    assert.equal(a.element.querySelector('.properties').textContent, 'Nothing is selected.');
    assert.deepEqual(a.statuses.at(-1), { selection: 'Group Addresses', shown: 2, total: 2 });
    assert.equal(a.stream.starts, 1);
    assert.deepEqual(notes(a.element), []);
    assert.deepEqual(a.navigations, []);
});

test('group addresses: a main group lists its middle groups, a middle group its addresses in the columns ETS users know', async () => {
    const a = await open();
    treeItem(a.element, '31 House').querySelector('.name').fire('click');
    assert.deepEqual(a.navigations, [{ view: 'group-addresses', bridge: 'knx:ip:line7', query: { sel: 'range:House@31' } }]);
    assert.deepEqual(tableRows(a.element).map(words), ['31/0 Lights', '31/1 Blinds', '31/7 Weather']);
    assert.deepEqual(a.statuses.at(-1), { selection: '31 House', shown: 3, total: 3 });
    assert.deepEqual(facts(a.element), [
        ['Settings', 'Address', '31', 'Name', 'House'],
        ['Information', 'Group addresses below', '4'],
    ]);
    tableRows(a.element)[0].fire('keydown', { key: 'Enter' });
    assert.deepEqual(a.navigations.at(-1).query, { sel: 'range:House/Lights@31/0' });
    assert.deepEqual(treeItems(a.element), ['Group Addresses', '31 House', '31/0 Lights', '31/1 Blinds', '31/7 Weather', 'Without a group', 'Dynamic Folders']);
    assert.equal(treeItem(a.element, '31/0 Lights').getAttribute('aria-selected'), 'true');
    assert.deepEqual(headings(a.element), COLUMNS);
    assert.deepEqual(addresses(a.element), ['31/0/1', '31/0/2']);
    const kitchen = rowOf(a.element, '31/0/1');
    assert.deepEqual(kitchen.children.map((cell) => cell.textContent), ['31/0/1', 'Kitchen <b>light</b>', 'Ceiling', '1.001', '1 bit', '', '3', kitchen.children[7].textContent, 'in use']);
    assert.equal(kitchen.children[1].children.length, 0);
    assert.equal(kitchen.querySelector('.value').textContent, 'ON');
    assert.match(kitchen.querySelector('.time').textContent, /^\d\d:\d\d:\d\d\.250$/);
    assert.match(kitchen.querySelector('.time').getAttribute('title'), /^2026-11-1\d /);
    const hall = rowOf(a.element, '31/0/2');
    assert.deepEqual(hall.children.map((cell) => cell.textContent), ['31/0/2', 'Hall light', '', '1.001', '1 bit', '', '1', '', '']);
    const key = hall.children[5].querySelector('.secured');
    assert.equal(key.getAttribute('title'), 'secured');
    assert.equal(key.getAttribute('aria-label'), 'secured');
    assert.ok(key.querySelector('.icon-key'));
    assert.equal(kitchen.children[5].querySelector('.secured'), null);
});

test('group addresses: an address whose last telegram has no value shows the payload as hex', async () => {
    const a = await open({ sel: 'range:House/Weather@31/7' });
    const outside = rowOf(a.element, '31/7/9');
    assert.equal(outside.querySelector('.value').textContent, '0C1A');
    assert.equal(outside.querySelector('.payload').textContent, '0C1A');
    assert.equal(outside.children[6].textContent, '0');
});

test('group addresses: without a linked object the Length is the one the data type gives, when it gives one', async () => {
    const addresses = [
        ...ADDRESSES,
        { address: '31/7/11', name: 'Scene', dataType: '17.001', secured: false, range: ['House', 'Weather'] },
        { address: '31/7/12', name: 'Odd type', dataType: '999.001', secured: false, range: ['House', 'Weather'] },
        { address: '31/7/13', name: 'No type', secured: false, range: ['House', 'Weather'] },
    ];
    const a = await open({ api: fakeApi({ addresses }), sel: 'range:House/Weather@31/7' });
    assert.deepEqual(
        ['31/7/9', '31/7/11', '31/7/12', '31/7/13'].map((address) => rowOf(a.element, address).children[4].textContent),
        ['2 bytes', '1 byte', '', ''],
    );
    rowOf(a.element, '31/7/9').fire('click');
    assert.deepEqual(facts(a.element)[0].slice(-2), ['Length', '2 bytes']);
    // a linked object's own size wins over the data type's
    const sized = DEVICES.map((device) => ({ ...device, groupObjects: device.groupObjects.map((item) => ({ ...item, sizeBits: 8 })) }));
    const b = await open({ api: fakeApi({ devices: sized }), sel: 'range:House/Lights@31/0' });
    assert.equal(rowOf(b.element, '31/0/2').children[4].textContent, '1 byte');
    assert.deepEqual([dptBits('1.001'), dptBits('9'), dptBits('14.056'), dptBits('16.000'), dptBits('232.600'), dptBits('1.x'), dptBits(''), dptBits(undefined), dptBits('constructor')], [1, 16, 32, 112, 24, 0, 0, 0, 0]);
});

test('group addresses: Copy also works in the sidebar that two panels share, which stands outside the panel', async () => {
    const a = await open({ sel: 'range:House/Lights@31/0', shared: true });
    assert.equal(a.element.querySelector('.sidebar'), null);
    rowOf(a.element, '31/0/2').children[1].fire('click');
    await a.clock.advance(SUGGESTION_MILLIS);
    a.document.commandResult = true;
    a.side.querySelector('.copy').click();
    await settle();
    assert.deepEqual(a.document.commands.map((entry) => entry.command), ['copy']);
    assert.ok(a.document.commands[0].selection === a.side.querySelector('textarea'));
    assert.equal(a.side.querySelector('.copied').textContent, 'Copied');
    a.view.close();
    a.side.querySelector('.copy')?.click();
    await settle();
    assert.equal(a.document.commands.length, 1);
});

test('group addresses: the bar has "Add to openHAB" for a bridge that is there with a project', async () => {
    const a = await open({ sel: 'range:House/Lights@31/0' });
    const link = a.element.querySelector('.panel-bar').querySelector('.tools').querySelector('.add');
    assert.deepEqual([link.textContent, link.getAttribute('href')], ['Add to openHAB…', '#/b/knx%3Aip%3Aline7/wizard']);
    const gone = await open({ bridge: REMOVED });
    assert.equal(gone.element.querySelector('.panel-bar').querySelector('.add'), null);
});

test('group addresses: a selected row of the list shows its fields in Properties, with the proposed channel and Copy', async () => {
    const a = await open({ sel: 'range:House/Lights@31/0' });
    rowOf(a.element, '31/0/2').children[1].fire('click');
    await a.clock.advance(SUGGESTION_MILLIS);
    assert.equal(a.element.querySelector('.properties').querySelector('.subject').textContent, '31/0/2 Hall light');
    assert.deepEqual(facts(a.element), [
        ['Settings', 'Address', '31/0/2', 'Name', 'Hall light', 'Data Type', '1.001', 'Length', '1 bit', 'Security', 'secured'],
        ['Information', 'Associations', '1'],
    ]);
    assert.deepEqual(openhab(a.element), ['Channel', 'not in use', 'Proposed channel', 'Copy', 'Add to openHAB…']);
    assert.equal(a.element.querySelector('.openhab').querySelector('.add').getAttribute('href'), '#/b/knx%3Aip%3Aline7/wizard');
    const field = a.element.querySelector('.openhab').querySelector('textarea');
    assert.equal(field.value, 'Type switch : a "A" [ ga="1.001:31/0/2" ]');
    assert.equal(field.hasAttribute('readonly'), true);
    assert.deepEqual(a.api.calls.filter(([name]) => name === 'channels'), [['channels', '31/0/2']]);
    a.context.clipboard = undefined;
    a.document.commandResult = true;
    a.element.querySelector('.copy').click();
    await settle();
    assert.equal(a.element.querySelector('.copied').textContent, 'Copied');
    rowOf(a.element, '31/0/1').fire('click');
    await a.clock.advance(SUGGESTION_MILLIS);
    assert.deepEqual(facts(a.element)[0].slice(0, 7), ['Settings', 'Address', '31/0/1', 'Name', 'Kitchen <b>light</b>', 'Description', 'Ceiling']);
    assert.deepEqual(facts(a.element)[1], ['Comments', 'Description', 'Ceiling']);
    assert.deepEqual(facts(a.element)[2].slice(0, 5), ['Information', 'Associations', '3', 'Last Value', 'ON']);
    assert.deepEqual(openhab(a.element).slice(0, 2), ['Channel', 'in use']);
    assert.equal(a.element.querySelector('.openhab').querySelector('textarea').value, 'Type switch : a "A" [ ga="1.001:31/0/1" ]');
});

test('group addresses: one selected address lists its Associations: object, device, sending, data type, the five flags, product, openHAB', async () => {
    const a = await open({ sel: 'ga:31/0/1' });
    assert.equal(treeItem(a.element, '31/0/1 Kitchen <b>light</b>').getAttribute('aria-selected'), 'true');
    assert.deepEqual(tabs(a.element), [['Associations (3)', 'true']]);
    assert.deepEqual(headings(a.element), ['Object', 'Device', 'Sending', 'Data Type', 'C', 'R', 'W', 'T', 'U', 'Product', 'openHAB']);
    assert.deepEqual(
        tableRows(a.element).map((row) => [0, 1, 2, 3, 9, 10].map((at) => textNodes(row.children[at]).join(' '))),
        [
            ['0: Channel A - Switch', '7.7.1 Switch actuator', '-', '1.001', 'Relay 8x', 'in use'],
            ['3: Status A - Status', '7.7.1 Switch actuator', '-', '1.001', 'Relay 8x', 'in use'],
            ['1: Rocker 1 - Switch', '7.7.2 Push <button>', 'S', '1.001', '', 'in use'],
        ],
    );
    const flags = (row) => [4, 5, 6, 7, 8].map((at) => row.children[at].textContent).join('');
    assert.deepEqual(tableRows(a.element).map(flags), ['C-W-U', 'CR-T-', 'C--T-']);
    const [channel, status, rocker] = tableRows(a.element);
    assert.equal(channel.children[4].querySelector('.flag').getAttribute('title'), 'Communication: set');
    assert.equal(channel.children[5].querySelector('.flag').getAttribute('title'), 'Read: not set');
    assert.equal(a.element.querySelector('thead').querySelectorAll('button')[6].getAttribute('title'), 'Write');
    assert.equal(channel.children[2].querySelector('.sending'), null);
    assert.equal(status.children[2].querySelector('.sending'), null);
    assert.equal(rocker.children[2].querySelector('.sending').getAttribute('title'), 'The object sends to this address');
    assert.equal(a.element.querySelector('.list').querySelector('a'), null);
    assert.equal(a.element.querySelector('.properties').querySelector('.open'), null);
    assert.deepEqual(a.statuses.at(-1), { selection: '31/0/1 Kitchen <b>light</b>', shown: 3, total: 3 });
    assert.deepEqual(facts(a.element).map((section) => section[0]), ['Settings', 'Comments', 'Information']);
    assert.deepEqual(a.api.calls.filter(([name]) => name === 'device'), [['device', 'P-7_DI-1'], ['device', 'P-7_DI-2']]);

    rocker.children[1].querySelector('.name').fire('click');
    assert.deepEqual(a.navigations, []);
    assert.equal(a.element.querySelector('.properties').querySelector('.open').getAttribute('href'), '#/b/knx%3Aip%3Aline7/topology?sel=device%3AP-7_DI-2');
    assert.deepEqual(a.replacements, [{ view: 'group-addresses', bridge: 'knx:ip:line7', query: { sel: 'ga:31/0/1', row: 'link:P-7_DI-2|0' } }]);
    assert.deepEqual(facts(a.element).slice(1), [
        ['Settings', 'Group object', 'Number', '1', 'Name', 'Rocker 1', 'Object Function', 'Switch', 'Data Type', '1.001', 'Length', '1 bit', 'Linked with', '31/0/1'],
        ['Settings', 'Flags', 'Communication', 'Read', 'Write', 'Transmit', 'Update', 'Read on init'],
        ['Comments', 'Description', 'Ceiling'],
        ['Comments', 'Group object', 'Description', 'Kitchen'],
        ['Information', 'Associations', '3', 'Last Value', 'ON', 'Time', facts(a.element).at(-2).at(-1)],
        ['Information', 'Group object', 'Device', '7.7.2 Push <button>'],
    ]);
    rocker.fire('keydown', { key: 'Enter' });
    assert.deepEqual(a.navigations.at(-1), { view: 'topology', bridge: 'knx:ip:line7', query: { sel: 'device:P-7_DI-2' } });

    const none = await open({ sel: 'ga:31/1/1' });
    assert.equal(none.element.querySelector('.empty').textContent, 'No group object is linked to this address.');
    assert.deepEqual(tabs(none.element), [['Associations (0)', 'true']]);
});

test('group addresses: the address of a selection opens it, a new address moves it, and an unknown one says so', async () => {
    const a = await open({ sel: 'range:House/Blinds@31/1' });
    assert.deepEqual(addresses(a.element), ['31/1/1']);
    a.view.update({ view: 'group-addresses', bridge: PRESENT.uid, query: { sel: 'range:' } });
    assert.deepEqual(addresses(a.element), ['31/7/10']);
    assert.equal(treeItem(a.element, 'Without a group').getAttribute('aria-selected'), 'true');
    a.view.update({ view: 'group-addresses', bridge: PRESENT.uid, query: {} });
    assert.deepEqual(tableRows(a.element).length, 2);
    a.view.update({ view: 'group-addresses', bridge: PRESENT.uid, query: { sel: 'ga:31/7/77' } });
    assert.equal(a.element.querySelector('.list').textContent, 'The project has no such group address.');
    assert.deepEqual(a.navigations, []);
    treeItem(a.element, 'Group Addresses').querySelector('.name').fire('click');
    assert.deepEqual(a.navigations, [{ view: 'group-addresses', bridge: PRESENT.uid, query: {} }]);
    treeItem(a.element, 'Group Addresses').querySelector('.name').fire('click');
    assert.equal(a.navigations.length, 1);
});

test('group addresses: a free-style project nests as deep as it has ranges', async () => {
    const free = [
        { address: '63489', name: 'Deep', range: ['A', 'B', 'C'] },
        { address: '63490', name: 'Shallow', range: ['A'] },
    ];
    const a = await open({ api: fakeApi({ addresses: free }), sel: 'ga:63489' });
    assert.deepEqual(treeItems(a.element), ['Group Addresses', 'A', 'B', 'C', '63489 Deep', '63490 Shallow', 'Dynamic Folders']);
    assert.equal(treeItem(a.element, '63489 Deep').getAttribute('aria-level'), '5');
});

test('group addresses: the find box looks through every address below the selected group, by address and name', async () => {
    const a = await open();
    find(a, 'HALL');
    assert.deepEqual(addresses(a.element), ['31/0/2']);
    assert.deepEqual(a.statuses.at(-1), { selection: 'Group Addresses', shown: 1, total: 5 });
    find(a, '31/7/1');
    assert.deepEqual(addresses(a.element), ['31/7/10']);
    find(a, 'ceiling');
    assert.deepEqual(addresses(a.element), ['31/0/1']);
    find(a, 'nothing like this');
    assert.equal(tableRows(a.element).length, 0);
    assert.equal(a.element.querySelector('.empty').textContent, 'No address matches.');
    find(a, '');
    assert.deepEqual(tableRows(a.element).map((row) => row.children[1].textContent), ['House', 'Without a group']);
    treeItem(a.element, '31 House').querySelector('.name').fire('click');
    find(a, 'light');
    assert.deepEqual(addresses(a.element), ['31/0/1', '31/0/2']);
});

test('group addresses: a click on a heading sorts the list', async () => {
    const a = await open({ sel: 'range:House/Lights@31/0' });
    const name = a.element.querySelector('thead').querySelectorAll('button')[1];
    name.click();
    assert.deepEqual(addresses(a.element), ['31/0/2', '31/0/1']);
    name.click();
    assert.deepEqual(addresses(a.element), ['31/0/1', '31/0/2']);
});

test('group addresses: a telegram from the stream changes the address\'s value cell', async () => {
    const a = await open({ sel: 'range:House/Lights@31/0' });
    const row = rowOf(a.element, '31/0/1');
    a.rows.add(telegram(20, '31/0/1', { value: 'OFF' }));
    a.rows.add(telegram(21, '31/0/1', { type: 'READ', value: undefined }));
    assert.equal(row.querySelector('.value').textContent, 'ON');
    a.runFrames();
    assert.equal(rowOf(a.element, '31/0/1'), row);
    assert.equal(row.querySelector('.value').textContent, 'OFF');
});

test('group addresses: the value cell follows the real stream of the bridge', async () => {
    const clock = { setTimeout: () => 1, clearTimeout() {} };
    let body = null;
    const fetch = createFetch((call) => {
        body = createEventBody(call.init.signal);
        body.send('open', { lastNumber: 30, monitorId: 'm1' });
        return body.response;
    });
    const api = fakeApi();
    api.telegrams = async () => ({ telegrams: [], lastNumber: 30, monitorId: 'm1', more: false, reset: false });
    const rows = createRows({ bridge: PRESENT.uid, api });
    const auth = { token: async () => 't', renew: async () => 't', lost() {} };
    const stream = createStream({ bridge: PRESENT.uid, rows, api, auth, fetch, base: new URL('http://openhab.invalid/rest/'), timers: clock });
    const a = await open({ api, rows, stream, sel: 'range:House/Lights@31/0' });
    await settle();
    assert.equal(stream.state().phase, 'live');
    body.send('telegram', telegram(31, '31/0/2', { value: 'ON', destinationName: 'Hall light' }));
    await settle();
    a.runFrames();
    assert.equal(rowOf(a.element, '31/0/2').querySelector('.value').textContent, 'ON');
    stream.close();
});

test('group addresses: the last value in Properties follows the stream too', async () => {
    const a = await open({ sel: 'ga:31/0/1' });
    assert.deepEqual(facts(a.element)[2].slice(3, 5), ['Last Value', 'ON']);
    a.rows.add(telegram(40, '31/0/1', { value: 'OFF' }));
    a.runFrames();
    assert.deepEqual(facts(a.element)[2].slice(3, 5), ['Last Value', 'OFF']);
});

test('group addresses: without a project, the addresses seen on the bus from call 6 and the line about Project', async () => {
    const a = await open({ api: fakeApi({ project: { state: 'NONE' } }) });
    assert.deepEqual(notes(a.element), ['No ETS project is loaded for this bridge — see Project']);
    assert.deepEqual(a.api.calls.map(([name]) => name), ['project', 'lastValues']);
    assert.deepEqual(headings(a.element), ['Address', 'Name', 'Data Type', 'Last Value']);
    assert.deepEqual(addresses(a.element), ['31/7/3', '31/7/20']);
    assert.deepEqual(textNodes(rowOf(a.element, '31/7/20')).slice(0, 3), ['31/7/20', 'Seen <i>one</i>', '1.001']);
    assert.equal(rowOf(a.element, '31/7/3').querySelector('.value').textContent, '21.5 °C');
    assert.deepEqual(treeItems(a.element), ['Group Addresses']);
    a.rows.add(telegram(9, '31/7/4', { value: 'OFF' }));
    a.runFrames();
    assert.deepEqual(addresses(a.element), ['31/7/3', '31/7/4', '31/7/20']);
    assert.equal(a.stream.starts, 1);
    find(a, '31/7/2');
    assert.deepEqual(addresses(a.element), ['31/7/20']);
});

test('group addresses: a stored project that cannot be used says why and shows call 6', async () => {
    for (const [state, words] of [
        ['UNREADABLE', 'The stored ETS project cannot be read. Import it again in Project.'],
        ['NEWER_VERSION', 'The stored ETS project was written by a newer version of the add-on. Import it again in Project.'],
    ]) {
        const a = await open({ api: fakeApi({ project: { state } }) });
        assert.deepEqual(notes(a.element), [words], state);
        assert.equal(tableRows(a.element).length, 2, state);
    }
});

test('group addresses: with the monitor switched off, the project addresses without last values and the text of monitor-off', async () => {
    const a = await open({ bridge: OFF, sel: 'range:House/Lights@31/0' });
    assert.deepEqual(notes(a.element), ['The monitor of this bridge is switched off (buffer size 0)']);
    assert.deepEqual(headings(a.element), COLUMNS.filter((name) => name !== 'Last Value'));
    assert.equal(rowOf(a.element, '31/0/1').querySelector('.value'), null);
    assert.equal(a.stream.starts, 0);
    assert.deepEqual(a.api.calls.slice(0, 2).map(([name]) => name), ['project', 'groupAddresses']);
});

test('group addresses: no project and the monitor switched off: only the two texts', async () => {
    const a = await open({ bridge: OFF, api: fakeApi({ project: { state: 'NONE' } }) });
    assert.deepEqual(notes(a.element), ['No ETS project is loaded for this bridge — see Project', 'The monitor of this bridge is switched off (buffer size 0)']);
    assert.equal(a.element.querySelector('table'), null);
    assert.ok(a.element.querySelector('.panel').querySelector('input') === null);
    assert.deepEqual(a.api.calls.map(([name]) => name), ['project']);
    const late = await open({ api: fakeApi({ project: { state: 'NONE' }, seen: new KnxApiError(409, 'monitor-off', 'x') }) });
    assert.deepEqual(notes(late.element), ['No ETS project is loaded for this bridge — see Project', 'The monitor of this bridge is switched off (buffer size 0)']);
    assert.equal(late.element.querySelector('table'), null);
    assert.deepEqual(late.errors, []);
});

test('group addresses: a removed bridge shows its project without the openHAB column, without last values and without a stream', async () => {
    const a = await open({ bridge: REMOVED, sel: 'range:House/Lights@31/0' });
    assert.deepEqual(headings(a.element), COLUMNS.filter((name) => name !== 'Last Value' && name !== 'openHAB'));
    assert.equal(rowOf(a.element, '31/0/1').querySelector('.configured'), null);
    assert.equal(rowOf(a.element, '31/0/1').querySelector('.value'), null);
    const broken = await open({ bridge: REMOVED, api: fakeApi({ project: { state: 'UNREADABLE' } }) });
    assert.deepEqual(notes(broken.element), ['The stored ETS project cannot be read. Import it again in Project.']);
    assert.deepEqual(broken.api.calls.map(([name]) => name), ['project']);
    assert.equal(broken.element.querySelector('table'), null);
});

test('group addresses: none, one and 2048 addresses', async () => {
    const none = await open({ api: fakeApi({ addresses: [] }) });
    assert.equal(tableRows(none.element).length, 0);
    assert.equal(none.element.querySelector('.empty').textContent, 'The project has no group addresses.');
    const one = await open({ api: fakeApi({ addresses: [ADDRESSES[1]] }) });
    assert.deepEqual(tableRows(one.element).map((row) => row.children[1].textContent), ['House']);
    const many = [];
    for (let n = 0; n < 2048; n++) {
        const address = n < 1792 ? `31/${n}` : `31/7/${n - 1792}`;
        many.push({ address, name: `Address ${n}`, secured: false, range: [`Range ${n >> 8}`] });
    }
    const big = await open({ api: fakeApi({ addresses: many }) });
    assert.equal(tableRows(big.element).length, 8);
    find(big, 'address');
    const list = big.element.querySelector('.list');
    assert.equal(tableRows(big.element).length, FIND_ROWS);
    assert.equal(FIND_ROWS, 500);
    assert.equal(list.querySelector('.more').textContent, 'Show more');
    assert.deepEqual(big.statuses.at(-1), { selection: 'Group Addresses', shown: 500, total: 2048 });
    for (const shown of [1000, 1500, 2000, 2048]) {
        list.querySelector('.more').click();
        assert.equal(tableRows(big.element).length, shown);
    }
    assert.equal(list.querySelector('.more'), null);
    find(big, 'address 20');
    assert.equal(tableRows(big.element).length, 59);
    assert.equal(list.querySelector('.more'), null);
    find(big, 'address');
    assert.equal(tableRows(big.element).length, FIND_ROWS, 'a new find text starts with the first rows again');
    list.querySelector('.more').click();
    list.querySelector('.more').click();
    list.querySelector('.more').click();
    list.querySelector('.more').click();
    big.rows.add(telegram(1, '31/7/200', { value: 'OFF' }));
    big.rows.add(telegram(2, '31/1000', { value: 'ON' }));
    big.runFrames();
    assert.equal(rowOf(big.element, '31/7/200').querySelector('.value').textContent, 'OFF');
    assert.equal(rowOf(big.element, '31/1000').querySelector('.value').textContent, 'ON');
    find(big, '');
    tableRows(big.element)[7].fire('dblclick');
    assert.equal(tableRows(big.element).length, 256);
    assert.deepEqual(big.navigations.at(-1).query, { sel: 'range:Range%207@31' });
});

test('group addresses: Clear in the Group Monitor empties the last values here too', async () => {
    const a = await open({ sel: 'range:House/Lights@31/0' });
    a.rows.clear();
    a.runFrames();
    assert.equal(rowOf(a.element, '31/0/1').children[7].textContent, '');
});

test('group addresses: a failed call 8 or 9 shows its error; a failed read of the devices leaves the list without Associations', async () => {
    const a = await open({ api: fakeApi({ project: new KnxApiError(0, 'network') }) });
    assert.deepEqual(a.errors.map((error) => error.code), ['network']);
    const api = fakeApi();
    api.allGroupAddresses = async () => {
        throw new KnxApiError(409, 'project-unusable', 'x');
    };
    const b = await open({ api });
    assert.deepEqual(b.errors.map((error) => error.code), ['project-unusable']);
    const c = await open({ api: fakeApi({ devices: new KnxApiError(500, 'internal', 'x') }), sel: 'range:House/Lights@31/0' });
    assert.deepEqual(c.errors.map((error) => error.code), ['internal']);
    assert.deepEqual(rowOf(c.element, '31/0/1').children.slice(4, 7).map((cell) => cell.textContent), ['1 bit', '', '']);
});

test('group addresses: close() leaves no listener and no drawing behind, also while a call is on its way', async () => {
    const a = await open({ sel: 'range:House/Lights@31/0' });
    assert.ok(a.document.listenerCount() > 0);
    a.rows.add(telegram(30, '31/0/1', { value: 'OFF' }));
    a.view.close();
    assert.equal(a.document.listenerCount(), 0);
    assert.equal(a.rows.subscribers(), 0);
    a.runFrames();
    assert.equal(rowOf(a.element, '31/0/1').querySelector('.value').textContent, 'ON');
    a.rows.add(telegram(31, '31/0/1'));
    assert.equal(a.pendingFrames(), 0);
    assert.equal(a.clock.count(), 0);

    let answer = null;
    const api = fakeApi();
    api.project = () => new Promise((resolve) => (answer = resolve));
    const harness = viewContext({ bridge: PRESENT, api, rows: createRows({ bridge: PRESENT.uid, api }), stream: fakeStream() });
    const view = show(harness.context);
    view.close();
    answer({ state: 'LOADED' });
    await settle();
    assert.equal(harness.element.querySelector('table'), null);
    assert.deepEqual(api.calls, []);
});

test('group addresses: two middle groups of one name are two nodes, and each lists its own addresses', async () => {
    const twins = [
        { address: '31/1/1', name: 'One', secured: false, range: ['House', 'New middle group'] },
        { address: '31/2/1', name: 'Two', secured: false, range: ['House', 'New middle group'] },
    ];
    const a = await open({ api: fakeApi({ addresses: twins }), sel: 'range:House/New%20middle%20group@31/2' });
    assert.deepEqual(treeItems(a.element), ['Group Addresses', '31 House', '31/1 New middle group', '31/2 New middle group', 'Dynamic Folders']);
    assert.equal(treeItem(a.element, '31/2 New middle group').getAttribute('aria-selected'), 'true');
    assert.deepEqual(tableRows(a.element).map((row) => row.children[0].textContent), ['31/2/1']);
});

test('group addresses: an address that names a range without its number still opens it', async () => {
    const a = await open({ sel: 'range:House/Lights' });
    assert.equal(treeItem(a.element, '31/0 Lights').getAttribute('aria-selected'), 'true');
    assert.deepEqual(tableRows(a.element).map((row) => row.children[0].textContent), ['31/0/1', '31/0/2']);
    const gone = await open({ sel: 'range:House/Gone' });
    assert.equal(tableRows(gone.element).length, 0);
    assert.ok(words(gone.element.querySelector('.list')).length > 0);
});

function channelCalls(a) {
    return a.api.calls.filter(([name]) => name === 'channels').map(([, list]) => list);
}

test('group addresses: a click on a row only selects it; Properties has "Open", and the address names the row', async () => {
    const a = await open({ sel: 'range:House@31' });
    rowOf(a.element, '31/0').children[1].fire('click');
    assert.deepEqual(a.navigations, []);
    assert.deepEqual(addresses(a.element), ['31/0', '31/1', '31/7'], 'still the list of the main group');
    assert.equal(a.element.querySelector('.properties').querySelector('.open').getAttribute('href'), '#/b/knx%3Aip%3Aline7/group-addresses?sel=range%3AHouse%2FLights%4031%2F0');
    assert.deepEqual(a.replacements, [{ view: 'group-addresses', bridge: 'knx:ip:line7', query: { sel: 'range:House@31', row: 'range:House/Lights@31/0' } }]);

    const b = await open({ sel: 'range:House/Lights@31/0' });
    rowOf(b.element, '31/0/2').children[1].fire('click');
    assert.equal(b.element.querySelector('.properties').querySelector('.open').textContent, 'Open');
    assert.equal(b.element.querySelector('.properties').querySelector('.open').getAttribute('href'), '#/b/knx%3Aip%3Aline7/group-addresses?sel=ga%3A31%2F0%2F2');
    assert.deepEqual(b.replacements.at(-1).query, { sel: 'range:House/Lights@31/0', row: 'ga:31/0/2' });
    assert.deepEqual(b.navigations, []);
    b.view.update({ view: 'group-addresses', bridge: 'knx:ip:line7', query: { sel: 'ga:31/0/2' } });
    assert.equal(b.element.querySelector('.properties').querySelector('.open'), null, 'the open address has nothing more to open');
});

test('group addresses: an address with a row selects that row again, in the list and in the Associations', async () => {
    const a = await open({ sel: 'range:House/Lights@31/0', row: 'ga:31/0/2' });
    assert.equal(rowOf(a.element, '31/0/2').getAttribute('aria-selected'), 'true');
    assert.equal(a.element.querySelector('.properties').querySelector('.subject').textContent, '31/0/2 Hall light');
    assert.deepEqual(a.replacements, []);
    a.view.update({ view: 'group-addresses', bridge: 'knx:ip:line7', query: { sel: 'range:House/Lights@31/0', row: 'ga:31/0/1' } });
    assert.equal(rowOf(a.element, '31/0/1').getAttribute('aria-selected'), 'true');
    assert.equal(rowOf(a.element, '31/0/2').getAttribute('aria-selected'), 'false');
    a.view.update({ view: 'group-addresses', bridge: 'knx:ip:line7', query: { sel: 'range:House/Lights@31/0' } });
    assert.equal(rowOf(a.element, '31/0/1').getAttribute('aria-selected'), 'false');
    assert.equal(a.element.querySelector('.properties').querySelector('.subject').textContent, '31/0 Lights');

    const b = await open({ sel: 'ga:31/0/1', row: 'link:P-7_DI-2|0' });
    assert.deepEqual(tableRows(b.element).map((row) => row.getAttribute('aria-selected')), ['false', 'false', 'true']);
    assert.deepEqual(facts(b.element)[1].slice(0, 2), ['Settings', 'Group object']);
    assert.equal(b.element.querySelector('.properties').querySelector('.open').getAttribute('href'), '#/b/knx%3Aip%3Aline7/topology?sel=device%3AP-7_DI-2');
});

test('group addresses: Enter on a row opens it, drops the row from the address and puts the focus on the first row of the new list', async () => {
    const a = await open({ sel: 'range:House@31' });
    const lights = rowOf(a.element, '31/0');
    lights.fire('click');
    lights.focus();
    lights.fire('keydown', { key: 'Enter' });
    assert.deepEqual(a.navigations, [{ view: 'group-addresses', bridge: 'knx:ip:line7', query: { sel: 'range:House/Lights@31/0' } }]);
    assert.deepEqual(addresses(a.element), ['31/0/1', '31/0/2']);
    assert.equal(a.document.activeElement, rowOf(a.element, '31/0/1'));
    rowOf(a.element, '31/0/2').fire('keydown', { key: 'Enter' });
    assert.equal(headings(a.element)[0], 'Object');
    assert.equal(a.document.activeElement, tableRows(a.element)[0]);
    assert.deepEqual(a.navigations.at(-1).query, { sel: 'ga:31/0/2' });
});

test('group addresses: the focus waits on the list while the Associations are read, then goes to their first row', async () => {
    const waiting = [];
    const api = fakeApi();
    const device = api.device;
    api.device = async (bridge, id) => {
        await new Promise((resolve) => waiting.push(resolve));
        return device(bridge, id);
    };
    const a = await open({ api, sel: 'range:House/Lights@31/0' });
    rowOf(a.element, '31/0/1').focus();
    rowOf(a.element, '31/0/1').fire('keydown', { key: 'Enter' });
    assert.equal(tableRows(a.element).length, 0);
    assert.equal(a.document.activeElement, a.element.querySelector('.list'));
    assert.equal(waiting.length, 2);
    waiting.forEach((resolve) => resolve());
    await settle();
    assert.equal(tableRows(a.element).length, 3);
    assert.equal(a.document.activeElement, tableRows(a.element)[0]);
});

test('group addresses: the tree that slid in closes when a node is chosen, and the focus goes to the list', async () => {
    const a = await open();
    a.element.querySelector('.tree-toggle').click();
    assert.equal(a.document.activeElement, treeItem(a.element, 'Group Addresses'));
    treeItem(a.element, '31 House').fire('keydown', { key: 'Enter' });
    assert.equal(a.element.querySelector('.workplace').classList.contains('tree-open'), false);
    assert.equal(a.document.activeElement, rowOf(a.element, '31/0'));
});

const USES = {
    '31/0/1': [
        { thingUID: 'knx:device:line7:kitchen', thingLabel: 'Kitchen <i>actuator</i>', channelId: 'light', channelLabel: 'Ceiling', channelType: 'switch', role: 'main', items: ['Kitchen_Light'] },
        { thingUID: 'knx:device:line7:hall', channelId: 'dimmer', channelType: 'dimmer', role: 'status', items: [] },
    ],
};

test('group addresses: the openHAB container of an address names the Things, channels and Items that use it, as links to Main UI', async () => {
    const a = await open({ api: fakeApi({ uses: USES }), sel: 'ga:31/0/1', base: new URL('http://openhab.invalid/oh/rest/') });
    assert.deepEqual(a.api.calls.filter(([name]) => name === 'groupAddress'), [['groupAddress', '31/0/1']]);
    assert.deepEqual(openhab(a.element).slice(0, 10), ['Channel', 'in use', 'Used by', 'Kitchen <i>actuator</i>', 'Ceiling · switch · main address', 'Items:', ' ', 'Kitchen_Light', 'knx:device:line7:hall', 'dimmer · dimmer · status address']);
    const block = a.element.querySelector('.openhab').querySelector('.uses');
    assert.equal(block.querySelector('.use-thing').children[0].children.length, 0, 'a label of openHAB is text');
    assert.deepEqual(block.querySelectorAll('a').map((link) => [link.textContent, link.getAttribute('href'), link.getAttribute('target'), link.getAttribute('rel')]), [
        ['Kitchen <i>actuator</i>', '/oh/settings/things/knx:device:line7:kitchen', '_blank', 'noopener noreferrer'],
        ['Kitchen_Light', '/oh/settings/items/Kitchen_Light', '_blank', 'noopener noreferrer'],
        ['knx:device:line7:hall', '/oh/settings/things/knx:device:line7:hall', '_blank', 'noopener noreferrer'],
    ]);
    assert.equal(block.querySelectorAll('li')[1].querySelector('.use-items').textContent, 'No Item is linked.');
    // the proposal and the wizard link stay under it
    assert.ok(a.element.querySelector('.openhab').querySelector('textarea'));
    assert.equal(openhab(a.element).at(-1), 'Add to openHAB…');
    // an address no channel uses is not asked for; one shown again is not asked twice
    a.view.update({ view: 'group-addresses', bridge: 'knx:ip:line7', query: { sel: 'ga:31/0/2' } });
    await settle();
    assert.equal(a.element.querySelector('.openhab').querySelector('.uses'), null);
    assert.equal(openhab(a.element).includes('Used by'), false);
    a.view.update({ view: 'group-addresses', bridge: 'knx:ip:line7', query: { sel: 'ga:31/0/1' } });
    await settle();
    assert.ok(a.element.querySelector('.openhab').querySelector('.uses'));
    assert.equal(a.api.calls.filter(([name]) => name === 'groupAddress').length, 1);
});

test('group addresses: a row of the list is asked for its channels when it stays selected; a failed read says so and is asked again', async () => {
    let fail = true;
    const a = await open({ api: fakeApi({ uses: (address) => (fail ? new KnxApiError(0, 'network') : USES[address]) }), sel: 'range:House/Lights@31/0' });
    const asked = () => a.api.calls.filter(([name]) => name === 'groupAddress').length;
    rowOf(a.element, '31/0/1').fire('click');
    assert.equal(asked(), 0);
    await a.clock.advance(SUGGESTION_MILLIS);
    assert.equal(asked(), 1);
    assert.deepEqual(openhab(a.element).slice(2, 4), ['Used by', 'openHAB cannot be reached.']);
    assert.deepEqual(a.errors.filter(Boolean), []);
    fail = false;
    rowOf(a.element, '31/0/2').fire('click');
    await a.clock.advance(SUGGESTION_MILLIS);
    rowOf(a.element, '31/0/1').fire('click');
    await a.clock.advance(SUGGESTION_MILLIS);
    assert.equal(asked(), 2);
    assert.deepEqual(openhab(a.element).slice(2, 5), ['Used by', 'Kitchen <i>actuator</i>', 'Ceiling · switch · main address']);
    const removed = await open({ bridge: REMOVED, api: fakeApi({ uses: USES }), sel: 'ga:31/0/1' });
    assert.equal(removed.api.calls.some(([name]) => name === 'groupAddress'), false, 'a bridge without a handler has no channels to ask for');
});

test('group addresses: the channel text is asked for the row that stays selected, not for every row the keys pass', async () => {
    const a = await open({ sel: 'range:House/Lights@31/0' });
    rowOf(a.element, '31/0/1').fire('click');
    await a.clock.advance(SUGGESTION_MILLIS - 1);
    rowOf(a.element, '31/0/1').fire('keydown', { key: 'ArrowDown' });
    await a.clock.advance(SUGGESTION_MILLIS - 1);
    assert.deepEqual(channelCalls(a), []);
    assert.equal(SUGGESTION_MILLIS, 250);
    await a.clock.advance(1);
    assert.deepEqual(channelCalls(a), ['31/0/2']);
    assert.equal(a.element.querySelector('.openhab').querySelector('textarea').value, 'Type switch : a "A" [ ga="1.001:31/0/2" ]');
    // a row that leaves the selection before the time is up is not asked for
    rowOf(a.element, '31/0/1').fire('click');
    a.view.update({ view: 'group-addresses', bridge: 'knx:ip:line7', query: { sel: 'range:House@31' } });
    await a.clock.advance(1000);
    assert.deepEqual(channelCalls(a), ['31/0/2']);
    // opened, an address is asked for at once
    a.view.update({ view: 'group-addresses', bridge: 'knx:ip:line7', query: { sel: 'ga:31/1/1' } });
    await settle();
    assert.deepEqual(channelCalls(a), ['31/0/2', '31/1/1']);
    a.view.close();
    assert.equal(a.clock.count(), 0);
});

test('group addresses: a new last value changes its two fields in Properties and leaves the rest of the pane alone', async () => {
    const a = await open({ sel: 'ga:31/0/1' });
    await settle();
    const pane = a.element.querySelector('.properties');
    const side = a.element.querySelector('.openhab');
    const copy = side.querySelector('.copy');
    const field = side.querySelector('textarea');
    const subject = pane.querySelector('.subject');
    const tabButton = pane.querySelector('.prop-tab-information');
    tabButton.click();
    copy.focus();
    field.select();
    a.rows.add(telegram(40, '31/0/1', { value: 'OFF', time: '2026-11-15T08:00:00.000Z' }));
    a.runFrames();
    assert.deepEqual(facts(a.element)[2].slice(3, 5), ['Last Value', 'OFF']);
    assert.notEqual(facts(a.element)[2][6], undefined);
    // compared by hand: a failed comparison of two elements would print the whole document
    assert.ok(a.document.activeElement === copy, 'the focus stays on Copy');
    assert.ok(a.document.selection === field, 'the selected text stays selected');
    assert.ok(pane.querySelector('.subject') === subject, 'the pane was not made again');
    assert.ok(side.contains(copy), 'nor the openHAB container');
    assert.ok(pane.querySelector('.prop-tab-information') === tabButton && tabButton.classList.contains('current'), 'the chosen tab stays');
    const time = facts(a.element)[2][6];
    a.rows.add(telegram(41, '31/0/1', { value: 'ON', time: '2026-11-15T09:30:00.000Z' }));
    a.runFrames();
    assert.deepEqual(facts(a.element)[2].slice(3, 5), ['Last Value', 'ON']);
    assert.notEqual(facts(a.element)[2][6], time);
    assert.ok(a.document.activeElement === copy, 'the focus stays on Copy');
});

test('group addresses: an address that gets its first value shows it in Properties', async () => {
    const a = await open({ sel: 'ga:31/0/2' });
    await settle();
    assert.deepEqual(facts(a.element)[1], ['Information', 'Associations', '1']);
    a.rows.add(telegram(42, '31/0/2', { value: 'ON' }));
    a.runFrames();
    assert.deepEqual(facts(a.element)[1].slice(0, 5), ['Information', 'Associations', '1', 'Last Value', 'ON']);
});

test('group addresses: two objects of one device with the same number are two rows that stay the same rows', async () => {
    const twin = {
        id: 'P-7_DI-9',
        address: '31.1.9',
        name: 'Twin',
        groupObjects: [
            { number: 5, name: 'First', groupAddresses: ['31/1/1'] },
            { number: 5, name: 'Second', groupAddresses: ['31/1/1'] },
            { name: 'No number', groupAddresses: ['31/1/1'] },
        ],
    };
    const a = await open({ api: fakeApi({ devices: [twin] }), sel: 'ga:31/1/1' });
    assert.deepEqual(tableRows(a.element).map((row) => row.children[0].textContent), ['5: First', '5: Second', 'No number']);
    const before = [...tableRows(a.element)];
    tableRows(a.element)[1].fire('click');
    assert.deepEqual(a.replacements.at(-1).query, { sel: 'ga:31/1/1', row: 'link:P-7_DI-9|1' });
    a.rows.forgetProject();
    treeItem(a.element, '31/1/1 Bedroom blind').fire('click');
    assert.deepEqual([...tableRows(a.element)], before);
    assert.deepEqual(tableRows(a.element).map((row) => row.getAttribute('aria-selected')), ['false', 'false', 'false']);
    a.view.update({ view: 'group-addresses', bridge: 'knx:ip:line7', query: { sel: 'ga:31/1/1', row: 'link:P-7_DI-9|2' } });
    assert.deepEqual(tableRows(a.element).map((row) => row.getAttribute('aria-selected')), ['false', 'false', 'true']);
});

test('group addresses: one device that cannot be read leaves the Associations of the others, and its error on the error line', async () => {
    const api = fakeApi();
    const device = api.device;
    api.device = async (bridge, id) => {
        if (id === 'P-7_DI-2') {
            throw new KnxApiError(500, 'internal', 'x');
        }
        return device(bridge, id);
    };
    const a = await open({ api, sel: 'range:House/Lights@31/0' });
    assert.deepEqual(a.errors.map((error) => error.code), ['internal']);
    assert.deepEqual(rowOf(a.element, '31/0/1').children.slice(4, 7).map((cell) => cell.textContent), ['1 bit', '', '2']);
    assert.deepEqual(rowOf(a.element, '31/0/2').children.slice(4, 7).map((cell) => cell.textContent), ['1 bit', '', '1']);
    a.view.close();
    // the next visit reads only the device that failed
    api.device = device;
    api.calls.length = 0;
    const again = await open({ api, rows: a.rows, sel: 'range:House/Lights@31/0' });
    assert.deepEqual(api.calls.filter(([name]) => name === 'device'), [['device', 'P-7_DI-2']]);
    assert.equal(rowOf(again.element, '31/0/1').children[6].textContent, '3');
    assert.deepEqual(again.errors, []);
});

test('folders: a dynamic folder of many addresses draws 500 rows with Show more, as every list without a find text', async () => {
    const many = Array.from({ length: 1200 }, (_, n) => ({ address: `31/${n}`, name: `Address ${n}`, secured: false, configured: false, range: ['Range'] }));
    const a = await open({ api: fakeApi({ addresses: many }), sel: 'folder:not-in-openhab' });
    assert.equal(tableRows(a.element).length, 500);
    assert.deepEqual(a.statuses.at(-1), { selection: 'Not in openHAB', shown: 500, total: 1200 });
    a.element.querySelector('.list').querySelector('.more').click();
    assert.equal(tableRows(a.element).length, 1000);
    treeItem(a.element, 'Secured').click();
    treeItem(a.element, 'Not in openHAB').click();
    assert.equal(tableRows(a.element).length, 500, 'another folder starts with the first rows again');
});

function listed(element) {
    return tableRows(element).map((row) => row.querySelector('.address').textContent);
}

test('folders: Group Addresses has "Not in openHAB", "In openHAB", "No associations" and "Secured"; a folder lists its addresses', async () => {
    const a = await open();
    treeItem(a.element, 'Dynamic Folders').querySelector('.twisty').fire('click');
    assert.deepEqual(treeItems(a.element).slice(0, 2), ['Group Addresses', '31 House']);
    assert.deepEqual(treeItems(a.element).slice(-5), ['Dynamic Folders', 'Not in openHAB', 'In openHAB', 'No associations', 'Secured']);
    treeItem(a.element, 'In openHAB').querySelector('.name').fire('click');
    assert.deepEqual(a.navigations.at(-1), { view: 'group-addresses', bridge: 'knx:ip:line7', query: { sel: 'folder:in-openhab' } });
    assert.deepEqual(listed(a.element), ['31/0/1']);
    assert.deepEqual(headings(a.element), COLUMNS);
    assert.deepEqual(tabs(a.element), [['Group Addresses (1)', 'true']]);
    assert.deepEqual(crumbs(a.element), ['Dynamic Folders', 'In openHAB']);
    assert.deepEqual(a.statuses.at(-1), { selection: 'In openHAB', shown: 1, total: 1 });
    assert.deepEqual(facts(a.element), [
        ['Settings', 'Name', 'In openHAB'],
        ['Information', 'Group addresses below', '1'],
    ]);
    for (const [sel, members] of [
        ['folder:not-in-openhab', ['31/0/2', '31/1/1', '31/7/9', '31/7/10']],
        ['folder:secured', ['31/0/2']],
        ['folder:no-associations', ['31/1/1', '31/7/9', '31/7/10']],
    ]) {
        a.view.update({ view: 'group-addresses', bridge: PRESENT.uid, query: { sel } });
        assert.deepEqual(listed(a.element), members, sel);
    }
    // a row of a folder is selected and opened as in any list
    tableRows(a.element)[0].fire('click');
    assert.equal(a.element.querySelector('.properties').querySelector('.subject').textContent, '31/1/1 Bedroom blind');
    tableRows(a.element)[0].fire('dblclick');
    assert.deepEqual(a.navigations.at(-1).query, { sel: 'ga:31/1/1' });
    a.view.update({ view: 'group-addresses', bridge: PRESENT.uid, query: { sel: 'folders' } });
    assert.equal(a.element.querySelector('.list').textContent, 'Choose one of the folders.');
    a.view.update({ view: 'group-addresses', bridge: PRESENT.uid, query: {} });
    assert.deepEqual(tableRows(a.element).map(words), ['31 House', 'Without a group'], 'with nothing selected the list holds the project\'s groups, not the folders');
});

test('folders: "No associations" waits for the devices; a removed bridge has no openHAB folders, and the addresses seen on the bus none at all', async () => {
    let release = null;
    const api = fakeApi();
    api.allDevices = () => new Promise((resolve) => (release = () => resolve(DEVICES.map(({ groupObjects, ...device }) => device))));
    const a = await open({ api, sel: 'folder:no-associations' });
    assert.equal(a.element.querySelector('.empty').textContent, 'Loading…');
    release();
    await settle();
    assert.deepEqual(listed(a.element), ['31/1/1', '31/7/9', '31/7/10']);
    assert.deepEqual(tabs(a.element), [['Group Addresses (3)', 'true']]);

    const removed = await open({ bridge: REMOVED });
    treeItem(removed.element, 'Dynamic Folders').querySelector('.twisty').fire('click');
    assert.deepEqual(treeItems(removed.element).slice(-3), ['Dynamic Folders', 'No associations', 'Secured']);
    const seen = await open({ api: fakeApi({ project: { state: 'NONE' } }) });
    assert.deepEqual(treeItems(seen.element), ['Group Addresses']);
});

test('crumbs: the path from main group to address stands in the bar; a crumb selects its group; the tab of a group counts its rows', async () => {
    const a = await open({ sel: 'ga:31/0/2' });
    assert.deepEqual(crumbs(a.element), ['Group Addresses', '31 House', '31/0 Lights', '31/0/2 Hall light']);
    assert.deepEqual(tabs(a.element), [['Associations (1)', 'true']]);
    a.element.querySelector('.crumbs').querySelectorAll('button')[2].click();
    assert.deepEqual(a.navigations, [{ view: 'group-addresses', bridge: 'knx:ip:line7', query: { sel: 'range:House/Lights@31/0' } }]);
    assert.deepEqual(crumbs(a.element), ['Group Addresses', '31 House', '31/0 Lights']);
    assert.deepEqual(tabs(a.element), [['Group Addresses (2)', 'true']]);
    a.element.querySelector('.crumbs').querySelectorAll('button')[1].click();
    assert.deepEqual(tabs(a.element), [['Group Addresses (3)', 'true']]);
    a.view.update({ view: 'group-addresses', bridge: PRESENT.uid, query: { sel: 'ga:31/7/99' } });
    assert.deepEqual(crumbs(a.element), ['Group Addresses']);
    assert.deepEqual(tabs(a.element), []);
    const chooser = a.element.querySelector('.panel-chooser');
    assert.equal(chooser.value, 'group-addresses');
    assert.equal(chooser.querySelectorAll('option').length, 5);
});

test('live: the container shows the newest telegrams of the selected group address and follows the stream; nothing is sent', async () => {
    const a = await open({ sel: 'range:House/Lights@31/0' });
    const live = () => a.element.querySelector('.live');
    assert.equal(live().textContent, 'Select a group address or a device to see its telegrams.');
    a.rows.add(telegram(50, '31/0/2', { value: 'OFF' }));
    a.rows.add(telegram(51, '31/0/1', { value: 'ON' }));
    a.runFrames();
    rowOf(a.element, '31/0/2').fire('click');
    const values = () => live().querySelectorAll('li').map((line) => [line.querySelector('.destination').textContent, line.querySelector('.value').textContent]);
    assert.deepEqual(values(), [['31/0/2', 'OFF']]);
    a.rows.add(telegram(52, '31/0/2', { value: 'ON' }));
    a.runFrames();
    assert.deepEqual(values(), [['31/0/2', 'ON'], ['31/0/2', 'OFF']]);
    a.view.update({ view: 'group-addresses', bridge: PRESENT.uid, query: { sel: 'ga:31/0/1' } });
    assert.deepEqual(values(), [['31/0/1', 'ON']]);
    a.view.update({ view: 'group-addresses', bridge: PRESENT.uid, query: { sel: 'range:House@31' } });
    assert.equal(live().textContent, 'Select a group address or a device to see its telegrams.');
    await a.clock.advance(1000);
    assert.deepEqual([...new Set(a.api.calls.map(([name]) => name))].sort(), ['channels', 'device', 'devices', 'groupAddress', 'groupAddresses', 'project'], 'only calls that read');
});

test('focus: "Show more" that takes itself away with the last batch hands the focus to the list', async () => {
    const many = Array.from({ length: 700 }, (_, n) => ({ address: `31/${n}`, name: `Address ${n}`, secured: false, range: ['Range'] }));
    const a = await open({ api: fakeApi({ addresses: many, devices: [] }) });
    find(a, 'address');
    const more = a.element.querySelector('.list').querySelector('.more');
    more.focus();
    more.click();
    assert.equal(a.element.querySelector('.list').querySelector('.more'), null);
    assert.equal(tableRows(a.element).length, 700);
    assert.ok(a.document.activeElement === tableRows(a.element)[0]);
});

test('focus: Back to another selection puts a focus that lost its place on the list', async () => {
    const a = await open({ sel: 'ga:31/0/1' });
    tableRows(a.element)[2].focus();
    a.view.update({ view: 'group-addresses', bridge: PRESENT.uid, query: { sel: 'range:House/Lights@31/0' } });
    assert.ok(a.document.activeElement === rowOf(a.element, '31/0/1'));
    a.document.activeElement = null;
    a.view.update({ view: 'group-addresses', bridge: PRESENT.uid, query: { sel: 'ga:31/0/1' } });
    assert.equal(a.document.activeElement, null, 'a focus outside the panel is left alone');
});

test('focus: a focus in the sidebar that two panels share counts as the panel\'s own when it loses its place', async () => {
    const a = await open({ sel: 'ga:31/0/1', shared: true });
    await settle();
    const copy = a.side.querySelector('.copy');
    copy.focus();
    a.view.update({ view: 'group-addresses', bridge: PRESENT.uid, query: { sel: 'range:House/Lights@31/0' } });
    assert.equal(a.side.contains(copy), false);
    assert.ok(a.document.activeElement === rowOf(a.element, '31/0/1'));
});

test('group addresses: a double click on an association shows its device in the other panel, in that panel\'s own tree when it is Topology or Buildings', async () => {
    const a = await open({ sel: 'ga:31/0/1' });
    const jumps = [];
    a.context.jump = (...parts) => jumps.push(parts);
    tableRows(a.element)[2].fire('dblclick');
    assert.equal(jumps.length, 1);
    assert.equal(jumps[0][0].view, 'topology');
    assert.equal(jumps[0][0].bridge, PRESENT.uid);
    assert.match(jumps[0][0].query.sel, /^device:/);
    assert.deepEqual(jumps[0][1], ['topology', 'buildings']);
    assert.deepEqual(a.navigations, []);
    tableRows(a.element)[2].fire('keydown', { key: 'Enter' });
    assert.equal(jumps.length, 2);
});

test('opening: Group Addresses shows its name open, so every main group is in view; a main group is closed until it is opened or a selection lies in it', async () => {
    const kept = new Map();
    const layout = createLayout({ read: () => kept.get('layout') ?? null, write: (value) => kept.set('layout', value) });
    const a = await open({ layout });
    assert.deepEqual(treeItems(a.element), ['Group Addresses', '31 House', 'Without a group', 'Dynamic Folders']);
    assert.equal(treeItem(a.element, '31 House').getAttribute('aria-expanded'), 'false');
    assert.equal(treeItem(a.element, 'Dynamic Folders').getAttribute('aria-expanded'), 'false');
    assert.equal(treeItem(a.element, 'Group Addresses').getAttribute('aria-selected'), 'true');
    a.element.querySelector('.tree-head').querySelector('.expand-all').click();
    assert.ok(treeItems(a.element).includes('31/0/2 Hall light'), 'Expand all opens down to the addresses');
    a.element.querySelector('.tree-head').querySelector('.collapse-all').click();
    assert.deepEqual(treeItems(a.element), ['Group Addresses', 'Dynamic Folders']);
    a.view.close();
    const again = await open({ layout });
    assert.deepEqual(treeItems(again.element), ['Group Addresses', 'Dynamic Folders'], 'the closed first node comes back closed');
    const named = await open({ layout, sel: 'ga:31/0/2' });
    assert.equal(treeItem(named.element, '31/0/2 Hall light').getAttribute('aria-selected'), 'true', 'a selection of the address has its path opened');
});
