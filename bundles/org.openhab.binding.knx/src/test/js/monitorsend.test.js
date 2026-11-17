// The Group Monitor's bar to read or write a group address. Sending is the one thing of the page that reaches the
// bus, so every way that must not send is tested by counting the calls of the stand-in fetch.

import { test } from 'node:test';
import assert from 'node:assert/strict';

import { createApi } from '../web/js/api.js';
import { createRows } from '../web/js/rows.js';
import { show } from '../web/js/views/monitor.js';
import { COMMON_TYPES, SUGGESTIONS, editorKind, noAskKey, rawOf, threeLevel } from '../web/js/views/monitorsend.js';
import { createFetch, json, settle } from './fakes.js';
import { fakeStream, viewContext } from './fakeview.js';

const BASE = new URL('http://openhab.invalid/oh/rest/');
const BRIDGE = { uid: 'knx:ip:made-up', label: 'Line 7', present: true, status: 'ONLINE', bufferSize: 2000, projectState: 'LOADED', sendAllowed: true };
const NO_ASK = noAskKey(BRIDGE.uid);
const SEND_PATH = '/oh/rest/knx/bridges/knx%3Aip%3Amade-up/group-telegram';
const ADDRESSES = [
    { address: '31/0/1', name: 'Hall light', dataType: '1.001' },
    { address: '31/0/2', name: 'Hall dimmer', dataType: '5.001' },
    { address: '31/0/3', name: 'Hall temperature', dataType: '9.001' },
    { address: '31/0/4', name: '<b>Hall</b> text', dataType: '16.001' },
    { address: '31/1/1', name: 'No type' },
];
const OFF_LINE =
    'Reading and writing group addresses from here is off for this bridge. To allow it, turn on "Allow sending from the KNX page" in the settings of the bridge Thing (shown with the advanced settings), then load this page again.';

function telegram(number, more = {}) {
    return { number, time: '2026-11-15T07:01:02.123456789Z', source: '31.1.3', destination: '31/0/2', type: 'WRITE', direction: 'IN', payload: '80', value: '50', dataType: '5.001', security: 'PLAIN', plainOnSecuredAddress: false, ...more };
}

/**
 * The monitor on the real API client over a stand-in fetch: every call the page makes is in fetch.calls.
 *
 * @param {Object} [options] bridge, query, keep, other (what a second panel shows), answer (of call 24: a Response
 *     maker), addresses (the project's group addresses)
 */
async function open({ bridge = BRIDGE, query = {}, keep = new Map(), other = null, answer, addresses = ADDRESSES } = {}) {
    const fetch = createFetch((call) => {
        const path = call.url.pathname;
        if (path === SEND_PATH && call.method === 'POST') {
            const body = JSON.parse(call.body);
            return answer ? answer(body) : json(200, { action: body.action, state: body.action === 'read' ? 'queued' : 'sent' });
        }
        if (path.endsWith('/monitor')) {
            return json(200, { received: 0, sent: 0 });
        }
        if (path.endsWith('/project/group-addresses')) {
            return json(200, { items: addresses, total: addresses.length });
        }
        return json(200, []);
    });
    const api = createApi({ fetch, base: BASE, auth: { token: async () => 'made-up-token', renew: async () => 'made-up-token', lost() {} } });
    const rows = createRows({ api, bridge: bridge.uid });
    const h = viewContext({ bridge, rows, stream: fakeStream('live'), api, route: { view: 'monitor', bridge: bridge.uid, query } });
    h.context.keep = keep;
    h.context.split = { half: 'first', open: other !== null, toggle: null, sidebar: null, other: () => other };
    const view = show(h.context);
    await settle();
    h.runFrames();
    const bar = h.element.querySelector('.send-bar');
    const one = (selector) => {
        const found = bar.querySelectorAll(selector);
        assert.equal(found.length, 1, selector);
        return found[0];
    };
    return {
        h,
        view,
        rows,
        fetch,
        keep,
        bar,
        one,
        /** {@return the bodies of the calls that send, in their order} */
        sent: () => fetch.calls.filter((call) => call.method !== 'GET').map((call) => [call.method, call.url.pathname, JSON.parse(call.body)]),
        type(selector, value, event = 'input') {
            const field = one(selector);
            field.value = value;
            field.fire(event);
            return field;
        },
        async press(selector) {
            one(selector).click();
            await settle();
        },
    };
}

/** Fills the bar for a write of On to 31/0/1. */
function fillWrite(m) {
    m.type('.send-address', '31/0/1', 'change');
    m.type('.send-value', 'ON', 'change');
}

test('send: without the bridge\'s setting the bar is one line that says how to allow it: no field, no button, no call', async () => {
    for (const bridge of [{ ...BRIDGE, sendAllowed: false }, { ...BRIDGE, sendAllowed: undefined }, { ...BRIDGE, sendAllowed: 'true' }]) {
        const m = await open({ bridge });
        assert.equal(m.bar.textContent, OFF_LINE);
        assert.equal(m.bar.querySelectorAll('input').length + m.bar.querySelectorAll('select').length + m.bar.querySelectorAll('button').length, 0);
        assert.deepEqual(m.sent(), []);
        assert.equal(
            m.fetch.calls.some((call) => call.url.pathname.includes('group-addresses')),
            false,
            'not even the names are read for a bar that cannot send',
        );
    }
});

test('send: the bar has the address, "Use selected", the type with its short list, the value, Read and Write, above the list', async () => {
    const m = await open();
    assert.deepEqual(
        m.bar.querySelectorAll('input').map((field) => [field.getAttribute('name'), field.getAttribute('aria-label')]),
        [
            ['send-address', 'Group address'],
            ['send-dpt', 'Type, as 1.001'],
            ['send-value', 'Value'],
        ],
    );
    assert.deepEqual(
        m.bar.querySelectorAll('button').map((node) => node.textContent),
        ['Use selected', 'Read', 'Write'],
    );
    assert.deepEqual(
        m.one('.send-dpt-common')
            .querySelectorAll('option')
            .map((option) => option.getAttribute('value')),
        ['', ...COMMON_TYPES],
    );
    assert.ok(m.h.element.querySelector('.notes').contains(m.bar), 'the bar stands in the notes above the list');
    assert.equal(m.bar.querySelector('.send-confirm'), null);
    assert.deepEqual(m.sent(), []);
});

test('send: nothing is sent by opening the panel, typing, choosing, a suggestion, "Use selected", Enter in any field, or a press on Read or Write alone', async () => {
    const m = await open();
    m.rows.add(telegram(1));
    m.h.runFrames();
    const fields = ['.send-address', '.send-dpt', '.send-value'];
    m.type('.send-address', '31/0');
    assert.ok(m.bar.querySelectorAll('.suggestion').length > 0);
    m.bar.querySelectorAll('.suggestion')[0].click();
    m.type('.send-address', '31/0/1', 'change');
    m.type('.send-dpt', '9.001');
    m.type('.send-dpt-common', '1.001', 'change');
    m.type('.send-value', 'OFF', 'change');
    for (const selector of fields) {
        for (const type of ['keydown', 'keypress', 'keyup']) {
            m.one(selector).fire(type, { key: 'Enter' });
        }
        m.one(selector).fire('change');
        m.one(selector).fire('focusout');
    }
    m.h.element.querySelector('tbody').children[0].fire('click');
    await m.press('.send-use');
    for (const selector of fields) {
        m.one(selector).fire('keydown', { key: 'Enter' });
    }
    await settle();
    assert.equal(m.bar.querySelector('.send-confirm'), null, 'Enter opens no question either');
    assert.deepEqual(m.sent(), []);
    // one press: the question, still nothing on the bus
    m.type('.send-value', '10');
    await m.press('.send-write');
    assert.ok(m.bar.querySelector('.send-confirm'));
    await m.press('.send-read');
    await m.press('.send-write');
    m.one('.send-confirm').fire('keydown', { key: 'Enter' });
    m.one('.send-words').click();
    await settle();
    assert.deepEqual(m.sent(), [], 'Read and Write only ask');
    assert.deepEqual(
        m.fetch.calls.map((call) => call.method).filter((method) => method !== 'GET'),
        [],
    );
});

test('send: Write then Send sends exactly what the question says, once; the answer is shown in the bar', async () => {
    const m = await open();
    fillWrite(m);
    await m.press('.send-write');
    assert.equal(m.one('.send-words').textContent, 'Write 31/0/1 «Hall light» · 1.001 · On — one telegram on the bus of «Line 7»');
    assert.equal(m.h.document.activeElement, m.one('.send-cancel'), 'the focus stands on the button that sends nothing');
    assert.deepEqual(m.sent(), []);
    await m.press('.send-go');
    assert.deepEqual(m.sent(), [['POST', SEND_PATH, { address: '31/0/1', action: 'write', dpt: '1.001', value: 'ON' }]]);
    assert.equal(m.one('.send-answer').textContent, 'Sent: one write to 31/0/1. It shows in the list below.');
    assert.equal(m.bar.querySelector('.send-confirm'), null, 'the question is gone: a second Send has nothing to press');
    assert.equal(m.fetch.calls.at(-1).headers['X-OPENHAB-TOKEN'], 'made-up-token');
});

test('send: Read then Send sends one read with the address alone', async () => {
    const m = await open();
    m.type('.send-address', '31/0/3', 'change');
    await m.press('.send-read');
    assert.equal(m.one('.send-words').textContent, 'Read 31/0/3 «Hall temperature» — one telegram on the bus of «Line 7»');
    await m.press('.send-go');
    assert.deepEqual(m.sent(), [['POST', SEND_PATH, { address: '31/0/3', action: 'read' }]]);
    assert.equal(m.one('.send-answer').textContent, 'Sent: one read of 31/0/3. It and its answer show in the list below.');
});

test('send: "Do not send" and a change of any field behind the question take the question back; nothing is sent', async () => {
    const m = await open();
    fillWrite(m);
    await m.press('.send-write');
    await m.press('.send-cancel');
    assert.equal(m.bar.querySelector('.send-confirm'), null);
    for (const [selector, value, event] of [
        ['.send-value', 'OFF', 'change'],
        ['.send-address', '31/0/4', 'input'],
        ['.send-dpt', '1.008', 'input'],
        ['.send-dpt-common', '5.001', 'change'],
    ]) {
        await m.press('.send-write');
        if (m.bar.querySelector('.send-confirm') === null) {
            // the number editor of 5.001 is empty: Write asks for a value first
            m.type('.send-value', '10');
            await m.press('.send-write');
        }
        assert.ok(m.bar.querySelector('.send-confirm'), selector);
        m.type(selector, value, event);
        assert.equal(m.bar.querySelector('.send-confirm'), null, `${selector}: what the question said is no longer what the fields hold`);
    }
    assert.deepEqual(m.sent(), []);
});

test('send: a press on Send while a telegram is under way sends no second one', async () => {
    let release;
    const m = await open({ answer: () => new Promise((resolve) => (release = () => resolve(json(200, { action: 'write', state: 'sent' })))) });
    fillWrite(m);
    await m.press('.send-write');
    const go = m.one('.send-go');
    go.click();
    go.click();
    await settle();
    assert.equal(m.one('.send-answer').textContent, 'Sending…');
    assert.deepEqual([m.one('.send-read').disabled, m.one('.send-write').disabled], [true, true]);
    await m.press('.send-write');
    await m.press('.send-read');
    assert.equal(m.sent().length, 1);
    release();
    await settle();
    assert.equal(m.one('.send-write').disabled, false);
    assert.equal(m.sent().length, 1);
});

test('send: without "Do not ask again" every send asks; with it Read and Write send at once in this tab until "Ask again"', async () => {
    const m = await open();
    fillWrite(m);
    for (let n = 1; n <= 2; n++) {
        await m.press('.send-write');
        assert.equal(m.sent().length, n - 1, 'the question comes each time');
        assert.equal(m.one('.send-no-ask').checked, false, 'the tick is never set by itself');
        await m.press('.send-go');
        assert.equal(m.sent().length, n);
    }
    await m.press('.send-read');
    m.one('.send-no-ask').click();
    assert.equal(m.sent().length, 2, 'the tick itself sends nothing');
    assert.equal(m.keep.get(NO_ASK), undefined, 'and counts only with Send');
    await m.press('.send-go');
    assert.equal(m.sent().length, 3);
    assert.equal(m.keep.get(NO_ASK), true);
    assert.equal(m.one('.send-no-ask-on').textContent, 'Read and Write send at once in this tab, without asking. Ask again');
    await m.press('.send-write');
    assert.equal(m.sent().length, 4);
    assert.equal(m.bar.querySelector('.send-confirm'), null);
    // the panel opened again in the same tab keeps it; it is in the tab's memory only
    const again = await open({ keep: m.keep });
    fillWrite(again);
    await again.press('.send-write');
    assert.equal(again.sent().length, 1);
    await again.press('.send-ask-again');
    assert.equal(again.keep.has(NO_ASK), false);
    await again.press('.send-write');
    assert.equal(again.sent().length, 1);
    assert.ok(again.bar.querySelector('.send-confirm'));
    // a new tab, or the same one after a reload or a sign-out, starts with an empty memory: it asks
    const fresh = await open();
    fillWrite(fresh);
    await fresh.press('.send-write');
    assert.deepEqual(fresh.sent(), []);
});

test('send: "do not ask again" holds for its bridge alone, and a bar that has not said so asks once more', async () => {
    const keep = new Map();
    const first = await open({ keep });
    const second = await open({ keep });
    fillWrite(first);
    fillWrite(second);
    await first.press('.send-write');
    first.one('.send-no-ask').click();
    await first.press('.send-go');
    await first.press('.send-write');
    assert.equal(first.sent().length, 2);
    // the second panel was drawn before the tick: it does not say that it sends at once, so it asks
    assert.equal(second.bar.querySelector('.send-no-ask-on'), null);
    await second.press('.send-write');
    assert.deepEqual(second.sent(), []);
    assert.ok(second.bar.querySelector('.send-confirm'));
    assert.ok(second.bar.querySelector('.send-no-ask-on'), 'now it says so');
    await second.press('.send-go');
    await second.press('.send-write');
    assert.equal(second.sent().length, 2);
    // another bridge chosen in the same tab asks
    const other = await open({ keep, bridge: { ...BRIDGE, uid: 'knx:ip:made-up-2', label: 'Line 8' } });
    fillWrite(other);
    await other.press('.send-write');
    assert.equal(other.fetch.calls.filter((call) => call.method !== 'GET').length, 0);
    assert.ok(other.bar.querySelector('.send-confirm'));
    assert.equal(other.bar.querySelector('.send-no-ask-on'), null);
});

test('send: the second click of a double click on Read, Write or Send sends no second telegram', async () => {
    const m = await open();
    fillWrite(m);
    await m.press('.send-write');
    m.one('.send-go').fire('click', { detail: 2 });
    await settle();
    assert.deepEqual(m.sent(), [], 'a double click that ends on Send does not send');
    m.one('.send-no-ask').click();
    await m.press('.send-go');
    assert.equal(m.sent().length, 1);
    for (const selector of ['.send-write', '.send-read']) {
        const before = m.sent().length;
        m.one(selector).fire('click', { detail: 1 });
        await settle();
        m.one(selector).fire('click', { detail: 2 });
        await settle();
        assert.equal(m.sent().length, before + 1, selector);
    }
    // a press by keyboard has no click count
    m.one('.send-write').fire('click', { detail: 0 });
    await settle();
    assert.equal(m.sent().length, 4);
});

test('send: a tick that was not sent with is forgotten by the next question', async () => {
    const m = await open();
    fillWrite(m);
    await m.press('.send-write');
    m.one('.send-no-ask').click();
    assert.equal(m.one('.send-no-ask').checked, true);
    await m.press('.send-cancel');
    await m.press('.send-write');
    assert.equal(m.one('.send-no-ask').checked, false);
    await m.press('.send-go');
    assert.equal(m.sent().length, 1);
    assert.deepEqual([...m.keep.keys()], []);
    await m.press('.send-write');
    assert.ok(m.bar.querySelector('.send-confirm'), 'it still asks');
});

test('send: with "do not ask again" a press while a telegram is under way sends no second one', async () => {
    let release;
    const m = await open({ keep: new Map([[NO_ASK, true]]), answer: () => new Promise((resolve) => (release = () => resolve(json(200, { action: 'write', state: 'sent' })))) });
    fillWrite(m);
    await m.press('.send-write');
    assert.equal(m.sent().length, 1);
    assert.deepEqual([m.one('.send-read').disabled, m.one('.send-write').disabled], [true, true]);
    // past the disabled buttons too
    m.one('.send-write').fire('click');
    m.one('.send-read').fire('click');
    await settle();
    assert.equal(m.sent().length, 1);
    release();
    await settle();
    await m.press('.send-read');
    assert.equal(m.sent().length, 2);
});

test('send: Enter in a field of a bar that is filled completely opens no question and sends nothing', async () => {
    const m = await open();
    fillWrite(m);
    for (const selector of ['.send-address', '.send-dpt', '.send-value']) {
        assert.notEqual(m.one(selector).value, '', selector);
        for (const type of ['keydown', 'keypress', 'keyup']) {
            m.one(selector).fire(type, { key: 'Enter' });
        }
    }
    await settle();
    assert.equal(m.bar.querySelector('.send-confirm'), null);
    assert.deepEqual(m.sent(), []);
});

test('send: "do not ask again" is kept in no storage of the browser and in no address', async () => {
    const m = await open();
    fillWrite(m);
    await m.press('.send-write');
    m.one('.send-no-ask').click();
    await m.press('.send-go');
    assert.deepEqual([...m.keep.keys()], [NO_ASK]);
    assert.deepEqual(m.h.replacements, []);
    assert.deepEqual(m.h.navigations, []);
});

test('send: an address of the page that names an address, an action and a value fills nothing and sends nothing', async () => {
    const query = { address: '31/0/1', action: 'write', value: 'ON', dpt: '1.001', send: '1', 'send-address': '31/0/1', dst: '31/0/1' };
    const m = await open({ query });
    assert.deepEqual([m.one('.send-address').value, m.one('.send-dpt').value], ['', '']);
    assert.equal(m.bar.querySelector('.send-confirm'), null);
    assert.deepEqual(m.sent(), []);
});

test('send: the page\'s address never carries what the bar holds or did', async () => {
    const m = await open();
    m.rows.add(telegram(4));
    m.h.runFrames();
    fillWrite(m);
    await m.press('.send-write');
    await m.press('.send-go');
    m.h.element.querySelector('tbody').children[0].fire('click');
    await m.press('.send-use');
    await m.press('.send-read');
    await m.press('.send-go');
    assert.equal(m.sent().length, 2);
    assert.deepEqual(m.h.navigations, []);
    for (const route of m.h.replacements) {
        assert.deepEqual(Object.keys(route.query), ['row'], 'only the selected row of the list is named');
    }
});

test('send: what is missing is said in the bar and asks nothing: no address, a write without a type or a value', async () => {
    const m = await open();
    await m.press('.send-read');
    assert.equal(m.one('.send-answer').textContent, 'Give a group address first.');
    m.type('.send-address', '31/1/1', 'change');
    await m.press('.send-write');
    assert.equal(m.one('.send-answer').textContent, 'Give the data point type as main.sub, such as 1.001.');
    for (const bad of ['9', '9.', 'x.1', '1.001 ; 2']) {
        m.type('.send-dpt', bad);
        await m.press('.send-write');
        assert.equal(m.one('.send-answer').textContent, 'Give the data point type as main.sub, such as 1.001.', bad);
    }
    m.type('.send-dpt', '9.001');
    await m.press('.send-write');
    assert.equal(m.one('.send-answer').textContent, 'Give a value first.');
    assert.equal(m.bar.querySelector('.send-confirm'), null);
    assert.deepEqual(m.sent(), []);
    // a read needs neither
    await m.press('.send-read');
    assert.equal(m.one('.send-words').textContent, 'Read 31/1/1 «No type» — one telegram on the bus of «Line 7»');
});

test('send: a refusal of the server is said in the page\'s words, never in the server\'s text', async () => {
    const cases = {
        'send-not-allowed': [403, 'Sending from this page is off for this bridge. Nothing was sent.'],
        'bad-group-address': [400, 'This is no group address of three parts (main/middle/sub). Nothing was sent.'],
        'unknown-data-type': [400, 'openHAB does not know this data point type. Nothing was sent.'],
        'bad-value': [400, 'The data point type cannot take this value. Nothing was sent.'],
        'bridge-offline': [409, 'The bridge is not connected. Nothing was sent.'],
        'too-many-telegrams': [429, 'At most 5 telegrams in 10 seconds go out from here. Nothing was sent; wait a moment.'],
        'send-failed': [502, 'The telegram did not go out.'],
    };
    for (const [code, [status, words]] of Object.entries(cases)) {
        const m = await open({ answer: () => json(status, { code, text: '<i>made-up server text</i>' }) });
        fillWrite(m);
        await m.press('.send-write');
        await m.press('.send-go');
        assert.equal(m.one('.send-answer').textContent, words, code);
        assert.ok(m.one('.send-answer').classList.contains('warn'), code);
        assert.equal(m.sent().length, 1, 'a refused send is not tried again by itself');
    }
});

test('send: the names of the project are offered while typing, as text; a pick fills the address and its type and sends nothing', async () => {
    const m = await open();
    m.type('.send-address', 'hall');
    assert.deepEqual(
        m.bar.querySelectorAll('.suggestion').map((node) => node.textContent),
        ['31/0/1 Hall light', '31/0/2 Hall dimmer', '31/0/3 Hall temperature', '31/0/4 <b>Hall</b> text'],
    );
    assert.equal(m.bar.querySelectorAll('b').length, 0, 'a name of the project is never markup');
    assert.ok(SUGGESTIONS >= 4);
    m.bar.querySelectorAll('.suggestion')[2].click();
    assert.deepEqual([m.one('.send-address').value, m.one('.send-dpt').value, m.one('.send-dpt-common').value], ['31/0/3', '9.001', '9.001']);
    assert.equal(m.bar.querySelectorAll('.suggestion').length, 0);
    m.type('.send-address', '31/0/');
    m.one('.send-address').fire('keydown', { key: 'Escape' });
    assert.equal(m.bar.querySelectorAll('.suggestion').length, 0, 'Escape closes the list');
    assert.deepEqual(m.sent(), []);
});

test('send: the value\'s editor fits the type: On/Off for 1.x, a number box with the unit for numbers, text else', async () => {
    assert.deepEqual(
        ['1.001', '1.008', '5.001', '9.001', '14.056', '16.001', '10.001', '232.600', '', '9', 'made-up'].map(editorKind),
        ['switch', 'switch', 'number', 'number', 'number', 'text', 'text', 'text', 'text', 'text', 'text'],
    );
    const m = await open();
    m.type('.send-address', '31/0/1', 'change');
    assert.deepEqual(
        m.one('.send-value')
            .querySelectorAll('option')
            .map((option) => [option.getAttribute('value'), option.textContent]),
        [
            ['ON', 'On'],
            ['OFF', 'Off'],
        ],
    );
    m.type('.send-address', '31/0/3', 'change');
    assert.equal(m.one('.send-value').getAttribute('type'), 'number');
    assert.equal(m.one('.unit').textContent, '°C');
    m.type('.send-value', '21.5');
    await m.press('.send-write');
    assert.equal(m.one('.send-words').textContent, 'Write 31/0/3 «Hall temperature» · 9.001 · 21.5 °C — one telegram on the bus of «Line 7»');
    await m.press('.send-go');
    assert.deepEqual(m.sent()[0][2], { address: '31/0/3', action: 'write', dpt: '9.001', value: '21.5' }, 'the number goes out without the unit');
    // a type changed by hand to another number type keeps what was typed
    m.type('.send-dpt', '9.004');
    assert.deepEqual([m.one('.send-value').value, m.one('.unit').textContent], ['21.5', 'lx']);
    m.type('.send-address', '31/0/4', 'change');
    assert.equal(m.one('.send-value').getAttribute('type'), 'text');
    m.type('.send-value', '  made-up text  ');
    await m.press('.send-write');
    await m.press('.send-go');
    assert.deepEqual(m.sent()[1][2], { address: '31/0/4', action: 'write', dpt: '16.001', value: 'made-up text' });
});

test('send: "Use selected" takes the selected telegram\'s address and type, else the group address selected in a second panel; it sends nothing', async () => {
    const m = await open({ other: { view: 'group-addresses', query: { sel: 'ga:31/0/3' } } });
    await m.press('.send-use');
    assert.deepEqual([m.one('.send-address').value, m.one('.send-dpt').value], ['31/0/3', '9.001']);
    m.rows.add(telegram(1));
    m.h.runFrames();
    m.h.element.querySelector('tbody').children[0].fire('click');
    await m.press('.send-use');
    assert.deepEqual([m.one('.send-address').value, m.one('.send-dpt').value], ['31/0/2', '5.001'], 'the selected telegram comes first');
    const row = await open({ other: { view: 'group-addresses', query: { sel: 'range:31/0', row: 'ga:31/0/1' } } });
    await row.press('.send-use');
    assert.equal(row.one('.send-address').value, '31/0/1');
    for (const other of [null, { view: 'topology', query: { sel: 'device:x' } }, { view: 'group-addresses', query: { sel: 'ga:../../x' } }]) {
        const none = await open({ other });
        await none.press('.send-use');
        assert.equal(none.one('.send-address').value, '');
        assert.equal(none.one('.send-answer').textContent, 'Nothing is selected: select a telegram here or a group address in a second panel.');
    }
    assert.deepEqual([...m.sent(), ...row.sent()], []);
});

test('send: a bridge without a project has the bar without names, and a typed address still asks before it sends', async () => {
    const m = await open({ bridge: { ...BRIDGE, projectState: 'NONE' } });
    assert.equal(
        m.fetch.calls.some((call) => call.url.pathname.includes('group-addresses')),
        false,
    );
    m.type('.send-address', '31/0/1', 'change');
    await m.press('.send-read');
    assert.equal(m.one('.send-words').textContent, 'Read 31/0/1 — one telegram on the bus of «Line 7»');
    assert.deepEqual(m.sent(), []);
});

test('send: the answer of a send that ends after the panel was closed changes nothing, and the closed bar listens to nothing', async () => {
    let release;
    const m = await open({ answer: () => new Promise((resolve) => (release = () => resolve(json(200, { action: 'write', state: 'sent' })))) });
    fillWrite(m);
    await m.press('.send-write');
    await m.press('.send-go');
    const before = m.one('.send-answer').textContent;
    m.view.close();
    release();
    await settle();
    assert.equal(m.one('.send-answer').textContent, before);
    m.one('.send-write').click();
    m.one('.send-read').click();
    await settle();
    assert.equal(m.sent().length, 1);
});

test('send: an address in any of the three styles is turned into main/middle/sub by one function; what is no address stays', () => {
    // put together from numbers where the address is outside the made-up main group
    const ga = (...parts) => parts.join('/');
    assert.deepEqual(
        ['31/0/1', '31/1', '63489', '31/7/255', '31/2047', '65535', '31/1/2', '31/258', '63746', ga(0, 1), '1', '2048'].map(threeLevel),
        ['31/0/1', '31/0/1', '31/0/1', '31/7/255', '31/7/255', '31/7/255', '31/1/2', '31/1/2', '31/1/2', ga(0, 0, 1), ga(0, 0, 1), ga(1, 0, 0)],
    );
    assert.deepEqual(['31/0/1', '31/1', '63489', ga(0, 1)].map(rawOf), [63489, 63489, 63489, 1]);
    const none = ['', '0', ga(0, 0), ga(0, 0, 0), '65536', ga(32, 1), ga(31, 2048), ga(31, 8, 1), ga(31, 0, 256), '31/0/1/1', '31//1', '31/', ' 31/1', '31.1.1', 'made-up', null, 63489];
    for (const each of none) {
        assert.equal(rawOf(each), null, String(each));
        assert.equal(threeLevel(each), typeof each === 'string' ? each : '', String(each));
    }
});

test('send: in a project of two-level style a suggested address is shown as the project writes it and sent as main/middle/sub', async () => {
    const m = await open({
        addresses: [
            { address: '31/1', raw: 63489, name: 'Hall light', dataType: '1.001' },
            { address: '31/258', raw: 63746, name: 'Hall temperature', dataType: '9.001' },
        ],
    });
    m.type('.send-address', 'hall');
    assert.deepEqual(
        m.bar.querySelectorAll('.suggestion').map((node) => node.textContent),
        ['31/1 Hall light', '31/258 Hall temperature'],
    );
    m.bar.querySelectorAll('.suggestion')[0].click();
    assert.deepEqual([m.one('.send-address').value, m.one('.send-dpt').value], ['31/1', '1.001']);
    m.type('.send-value', 'ON', 'change');
    await m.press('.send-write');
    assert.equal(m.one('.send-words').textContent, 'Write 31/1 (= 31/0/1) «Hall light» · 1.001 · On — one telegram on the bus of «Line 7»');
    await m.press('.send-go');
    assert.equal(m.one('.send-answer').textContent, 'Sent: one write to 31/1. It shows in the list below.');
    m.type('.send-address', '31/258', 'change');
    await m.press('.send-read');
    await m.press('.send-go');
    assert.deepEqual(
        m.sent().map((call) => call[2]),
        [
            { address: '31/0/1', action: 'write', dpt: '1.001', value: 'ON' },
            { address: '31/1/2', action: 'read' },
        ],
    );
});

test('send: in a project of free style "Use selected" hands over main/middle/sub, and a telegram\'s address finds the project\'s name and type', async () => {
    const addresses = [
        { address: '63489', raw: 63489, name: 'Hall light', dataType: '1.001' },
        { address: '63490', name: 'Hall dimmer', dataType: '5.001' },
    ];
    const m = await open({ addresses, other: { view: 'group-addresses', query: { sel: 'ga:63489' } } });
    await m.press('.send-use');
    assert.deepEqual([m.one('.send-address').value, m.one('.send-dpt').value], ['63489', '1.001']);
    await m.press('.send-read');
    assert.equal(m.one('.send-words').textContent, 'Read 63489 (= 31/0/1) «Hall light» — one telegram on the bus of «Line 7»');
    await m.press('.send-go');
    assert.deepEqual(m.sent()[0][2], { address: '31/0/1', action: 'read' });

    // the list shows a telegram's address as main/middle/sub whatever the project's style
    m.rows.add(telegram(1, { dataType: undefined }));
    m.h.runFrames();
    m.h.element.querySelector('tbody').children[0].fire('click');
    await m.press('.send-use');
    assert.deepEqual([m.one('.send-address').value, m.one('.send-dpt').value], ['31/0/2', '5.001']);
    m.type('.send-value', '50');
    await m.press('.send-write');
    assert.equal(m.one('.send-words').textContent, 'Write 31/0/2 «Hall dimmer» · 5.001 · 50 % — one telegram on the bus of «Line 7»');
    await m.press('.send-go');
    assert.deepEqual(m.sent()[1][2], { address: '31/0/2', action: 'write', dpt: '5.001', value: '50' });
});
