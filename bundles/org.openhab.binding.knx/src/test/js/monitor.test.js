import { test } from 'node:test';
import assert from 'node:assert/strict';

import { KnxApiError } from '../web/js/api.js';
import { createRows } from '../web/js/rows.js';
import { COUNTERS_MILLIS, END_SLACK, PAGE_ROWS, show } from '../web/js/views/monitor.js';
import { allElements, textNodes } from './fakedom.js';
import { settle } from './fakes.js';
import { fakeStream, viewContext } from './fakeview.js';

const BRIDGE = { uid: 'knx:ip:line7', label: 'Line 7', present: true, status: 'ONLINE', connection: 'SECURE_TUNNEL', bufferSize: 2000 };
const MONITOR = {
    connection: 'SECURE_TUNNEL',
    ownAddress: '7.7.250',
    bufferSize: 2000,
    rows: 3,
    lastNumber: 3,
    monitorId: 'm1',
    received: 12,
    sent: 3,
    notDecrypted: { NO_KEY: 2, SEQUENCE_NUMBER: 1 },
    notDecryptedOtherReason: 1,
    plainOnSecuredAddress: 4,
    notDecryptedSenders: [],
};

function telegram(number, more = {}) {
    return {
        number,
        time: '2026-11-15T07:01:02.123456789Z',
        source: '7.7.3',
        destination: '31/7/1',
        type: 'WRITE',
        direction: 'IN',
        payload: '01',
        value: 'ON',
        security: 'PLAIN',
        plainOnSecuredAddress: false,
        ...more,
    };
}

function fakeApi() {
    const api = {
        monitorCalls: 0,
        clearCalls: 0,
        monitorAnswer: MONITOR,
        async monitor(bridge) {
            assert.equal(bridge, BRIDGE.uid);
            api.monitorCalls++;
            if (api.monitorAnswer instanceof Error) {
                throw api.monitorAnswer;
            }
            return api.monitorAnswer;
        },
        async clearTelegrams(bridge) {
            assert.equal(bridge, BRIDGE.uid);
            api.clearCalls++;
        },
    };
    return api;
}

async function open({ rows = createRows(), stream = fakeStream('live'), api = fakeApi(), query = {} } = {}) {
    const harness = viewContext({ bridge: BRIDGE, rows, stream, api, route: { view: 'monitor', bridge: BRIDGE.uid, query } });
    const listenersBefore = harness.document.listenerCount();
    const view = show(harness.context);
    await settle();
    harness.runFrames();
    return Object.assign(harness, { view, rows, stream, api, listenersBefore });
}

function bodyRows(element) {
    return element.querySelector('tbody').children;
}

function cells(row) {
    return row.children.map((cell) => cell.textContent);
}

function button(element, name) {
    return element.querySelector(`.${name}`);
}

function headings(element) {
    return element.querySelector('thead').querySelectorAll('th').map((cell) => cell.textContent);
}

// the columns, by their place
const [NUMBER, TIME, SERVICE, SOURCE, SOURCE_NAME, DESTINATION, DESTINATION_NAME, BUILDING_FUNCTION, BUILDING_PART, TYPE, DPT, INFO, SECURITY, OUTCOME] = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13];

test('monitor: the title, the columns in the order of a group monitor, and the stream is started', async () => {
    const m = await open();
    assert.equal(m.element.querySelector('h2').textContent, 'Group Monitor');
    assert.equal(m.element.querySelectorAll('h2').length, 1);
    assert.deepEqual(headings(m.element), [
        '#',
        'Time',
        'Service',
        'Source Address',
        'Source Name',
        'Destination Address',
        'Destination Name',
        'Building Function',
        'Building Part',
        'Type',
        'DPT',
        'Info',
        'Security',
        'Outcome',
    ]);
    assert.equal(m.element.querySelector('.tree-pane'), null);
    // the bar holds Start, Stop, Clear and the filters
    const tools = m.element.querySelector('.tools');
    assert.ok(tools.parentNode.classList.contains('panel-bar'));
    assert.deepEqual(tools.children.map((node) => node.getAttribute('name') ?? node.getAttribute('class')), ['start', 'pause', 'fresh', 'clear', 'type', 'dir']);
    // the text filter is the list's find box, at the bar's end
    const slot = m.element.querySelector('.find-slot');
    assert.ok(slot.parentNode === tools.parentNode);
    assert.deepEqual(slot.children.map((node) => node.getAttribute('name')), ['filter']);
    assert.deepEqual(tools.querySelectorAll('button').map((node) => node.textContent), ['Start', 'Stop', 'Clear']);
    assert.equal(m.stream.starts, 1);
});

test('monitor: no row, one row, and an own row without a source name shows "openHAB"', async () => {
    const m = await open();
    assert.equal(bodyRows(m.element).length, 0);
    assert.equal(m.element.querySelector('.empty').textContent, 'No telegrams yet.');
    m.rows.add(telegram(1, { direction: 'OUT', source: '7.7.250', outcome: 'SENT' }));
    m.runFrames();
    const [row] = bodyRows(m.element);
    assert.equal(bodyRows(m.element).length, 1);
    assert.equal(m.element.querySelector('.empty'), null);
    assert.deepEqual(cells(row).slice(NUMBER, DESTINATION), ['1', cells(row)[TIME], 'to bus', '7.7.250', 'openHAB']);
    assert.equal(row.children[OUTCOME].textContent, 'sent');
    assert.ok(row.classList.contains('own'));
});

test('monitor: names and values from the server are text, never elements', async () => {
    const m = await open();
    m.rows.add(telegram(1, { sourceName: '<b>x</b>', destinationName: '<img src=x onerror=alert(1)>', value: '<i>1</i>', unit: '<u>', payload: undefined }));
    m.runFrames();
    const [row] = bodyRows(m.element);
    assert.deepEqual(cells(row).slice(SOURCE, BUILDING_FUNCTION), ['7.7.3', '<b>x</b>', '31/7/1', '<img src=x onerror=alert(1)>']);
    assert.equal(row.children[INFO].textContent, '<i>1</i> <u>');
    const tags = allElements(row).map((node) => node.tagName);
    assert.ok(tags.every((tag) => ['TD', 'SPAN'].includes(tag)), tags.join());
});

test('monitor: a row shows its number, time of day with milliseconds and the full date as title, service, type, DPT, value and raw bytes, security and outcome', async () => {
    const m = await open();
    m.rows.add(telegram(1, { value: '21.5', unit: '°C', dataType: '9.001', payload: '0C1A' }));
    m.rows.add(telegram(2, { type: 'READ', value: undefined, payload: undefined }));
    m.rows.add(telegram(3, { value: undefined, payload: '0C1A' }));
    m.rows.add(telegram(4, { security: 'NOT_DECRYPTED', type: 'UNKNOWN', reason: 'NO_KEY', value: undefined, payload: undefined }));
    m.rows.add(telegram(5, { security: 'PLAIN', plainOnSecuredAddress: true }));
    m.rows.add(telegram(6, { direction: 'IN', outcome: 'SENT', type: 'RESPONSE' }));
    m.rows.add(telegram(7, { time: 'not a time' }));
    m.runFrames();
    const rows = bodyRows(m.element);
    const byNumber = (n) => rows[n - 1];
    assert.deepEqual(rows.map((row) => row.children[NUMBER].textContent), ['1', '2', '3', '4', '5', '6', '7']);
    const clock = byNumber(1).children[TIME].querySelector('.clock');
    assert.match(clock.textContent, /^\d\d:\d\d:\d\d\.123$/);
    assert.match(clock.getAttribute('title'), /^2026-11-1\d \d\d:\d\d:\d\d\.123$/);
    assert.deepEqual(cells(byNumber(1)).slice(TYPE), ['Write', '9.001', '21.5 °C0C1A', 'plain', '']);
    assert.deepEqual(textNodes(byNumber(1).children[INFO]), ['21.5 °C', '0C1A']);
    assert.equal(byNumber(1).children[SERVICE].textContent, 'from bus');
    assert.deepEqual(cells(byNumber(2)).slice(TYPE, SECURITY), ['Read', '', '']);
    assert.equal(byNumber(3).children[INFO].textContent, '0C1A');
    assert.equal(byNumber(3).children[INFO].querySelector('.payload').textContent, '0C1A');
    assert.deepEqual(textNodes(byNumber(4).children[SECURITY]), ['not decrypted', 'no key for the address']);
    assert.equal(byNumber(4).getAttribute('class'), 'not-decrypted');
    assert.equal(byNumber(4).children[TYPE].textContent, '?');
    assert.deepEqual(textNodes(byNumber(5).children[SECURITY]), ['plain', 'plain on a secured address']);
    assert.equal(byNumber(6).children[TYPE].textContent, 'Response');
    assert.equal(byNumber(6).children[OUTCOME].textContent, '');
    assert.equal(byNumber(7).children[TIME].textContent, 'not a time');
    assert.equal(byNumber(7).children[TIME].querySelector('.clock').getAttribute('title'), null);
});

test('monitor: new rows come at the bottom', async () => {
    const m = await open();
    m.rows.add(telegram(1));
    m.rows.add(telegram(2));
    m.runFrames();
    m.rows.add(telegram(3));
    m.runFrames();
    assert.deepEqual(bodyRows(m.element).map((row) => row.children[NUMBER].textContent), ['1', '2', '3']);
});

test('monitor: the list follows its end, unless the viewer scrolled up; back at the end it follows again', async () => {
    const m = await open();
    const list = m.element.querySelector('.list');
    Object.assign(list, { scrollHeight: 1000, clientHeight: 200, scrollTop: 0 });
    m.rows.add(telegram(1));
    m.runFrames();
    assert.equal(list.scrollTop, 1000);
    list.scrollTop = 300;
    list.fire('scroll', { bubbles: false });
    list.scrollHeight = 1100;
    m.rows.add(telegram(2));
    m.runFrames();
    assert.equal(list.scrollTop, 300);
    list.scrollTop = 1100 - 200 - END_SLACK;
    list.fire('scroll', { bubbles: false });
    list.scrollHeight = 1200;
    m.rows.add(telegram(3));
    m.runFrames();
    assert.equal(list.scrollTop, 1200);
    assert.equal(END_SLACK, 8);
});

test('monitor: a selected row shows all its fields in Properties, and an outcome that arrives later shows there too', async () => {
    const m = await open();
    const properties = m.element.querySelector('.properties');
    assert.equal(properties.textContent, 'Nothing is selected.');
    m.rows.add(telegram(1, { direction: 'OUT', source: '7.7.250', destinationName: 'Kitchen <b>', dataType: '1.001', outcome: 'PENDING', security: 'NOT_DECRYPTED', reason: 'NO_KEY' }));
    m.runFrames();
    bodyRows(m.element)[0].children[INFO].fire('click');
    assert.equal(bodyRows(m.element)[0].getAttribute('aria-selected'), 'true');
    assert.equal(properties.querySelector('.subject').textContent, '# 1');
    const facts = textNodes(properties.querySelector('dl'));
    assert.deepEqual(facts.slice(0, 2), ['#', '1']);
    assert.match(facts[3], /^2026-11-1\d \d\d:\d\d:\d\d\.123$/);
    assert.deepEqual(facts.slice(4), [
        'Service',
        'to bus',
        'Source Address',
        '7.7.250',
        'Source Name',
        'openHAB',
        'Destination Address',
        '31/7/1',
        'Destination Name',
        'Kitchen <b>',
        'Type',
        'Write',
        'DPT',
        '1.001',
        'Value',
        'ON',
        'Raw bytes',
        '01',
        'Security',
        'not decrypted',
        'no key for the address',
        'Outcome',
        'pending',
    ]);
    m.rows.setOutcome(1, 'SENT');
    m.runFrames();
    assert.equal(textNodes(properties.querySelector('dl')).at(-1), 'sent');
    assert.equal(bodyRows(m.element)[0].getAttribute('aria-selected'), 'true');
});

test('monitor: the status bar hears how many telegrams are shown of how many are held', async () => {
    const m = await open();
    m.rows.add(telegram(1, { destinationName: 'Kitchen' }));
    m.rows.add(telegram(2));
    m.rows.addLost(3);
    m.runFrames();
    assert.deepEqual(m.statuses.at(-1), { selection: 'Group Monitor', shown: 2, total: 2 });
    m.rows.setFilter({ text: 'kitch' });
    m.runFrames();
    assert.deepEqual(m.statuses.at(-1), { selection: 'Group Monitor', shown: 1, total: 2 });
});

test('monitor: a click on a heading sorts the rows that are drawn', async () => {
    const m = await open();
    m.rows.add(telegram(1, { destination: '31/7/9' }));
    m.rows.add(telegram(2, { destination: '31/7/1' }));
    m.runFrames();
    m.element.querySelector('thead').querySelectorAll('button')[DESTINATION].click();
    assert.deepEqual(bodyRows(m.element).map((row) => row.children[NUMBER].textContent), ['2', '1']);
});

test('monitor: lost and restart markers are rows of their own', async () => {
    const m = await open();
    m.rows.add(telegram(1));
    m.rows.addLost(1);
    m.rows.addLost(7);
    m.runFrames();
    assert.deepEqual(bodyRows(m.element).map((row) => row.textContent).slice(1), ['1 telegram was lost', '7 telegrams were lost']);
    assert.equal(bodyRows(m.element)[1].children[0].getAttribute('colspan'), '14');
    m.rows.restart();
    m.runFrames();
    assert.deepEqual(bodyRows(m.element).map((row) => row.textContent), ['The monitor started again']);
});

test('monitor: an outcome that arrives later changes its row', async () => {
    const m = await open();
    m.rows.add(telegram(1, { direction: 'OUT', outcome: 'PENDING' }));
    m.runFrames();
    assert.equal(bodyRows(m.element)[0].children[OUTCOME].textContent, 'pending');
    m.rows.setOutcome(1, 'NO_ANSWER');
    m.runFrames();
    assert.equal(bodyRows(m.element)[0].children[OUTCOME].textContent, 'no answer');
});

test('monitor: the counters come from call 3 and are asked again every 10 s while the view is shown', async () => {
    const m = await open();
    const counters = m.element.querySelector('.counters');
    assert.deepEqual(textNodes(counters), [
        'Received 12',
        'Sent 3',
        'Not decrypted 4 (no key for the address 2, sequence number 1, other reason 1)',
        'Plain on a secured address 4',
        'secure tunnel',
        'Own address 7.7.250',
    ]);
    assert.equal(m.api.monitorCalls, 1);
    assert.equal(COUNTERS_MILLIS, 10000);
    m.api.monitorAnswer = { ...MONITOR, received: 13, notDecrypted: {}, notDecryptedOtherReason: 0, ownAddress: undefined, connection: undefined };
    await m.clock.advance(9999);
    assert.equal(m.api.monitorCalls, 1);
    await m.clock.advance(1);
    assert.equal(m.api.monitorCalls, 2);
    assert.deepEqual(textNodes(counters), ['Received 13', 'Sent 3', 'Not decrypted 0', 'Plain on a secured address 4']);
    m.view.close();
    await m.clock.advance(60_000);
    assert.equal(m.api.monitorCalls, 2);
});

test('monitor: a failed call 3 shows its error once it fails and takes it away when it works again', async () => {
    const api = fakeApi();
    api.monitorAnswer = new KnxApiError(0, 'network');
    const m = await open({ api });
    assert.deepEqual(m.errors.map((error) => error?.code ?? null), ['network']);
    api.monitorAnswer = MONITOR;
    await m.clock.advance(COUNTERS_MILLIS);
    assert.deepEqual(m.errors.map((error) => error?.code ?? null), ['network', null]);
    await m.clock.advance(COUNTERS_MILLIS);
    assert.equal(m.errors.length, 2);
});

test('monitor: Clear asks first, calls 5 only on yes, then empties the rows', async () => {
    const m = await open();
    m.rows.add(telegram(1));
    m.runFrames();
    m.answer = false;
    button(m.element, 'clear').click();
    await settle();
    assert.deepEqual(m.questions, ['Clear the monitor of this bridge for everyone?']);
    assert.equal(m.api.clearCalls, 0);
    assert.equal(m.rows.held(), 1);
    m.answer = true;
    button(m.element, 'clear').click();
    await settle();
    m.runFrames();
    assert.equal(m.api.clearCalls, 1);
    assert.equal(m.rows.held(), 0);
    assert.equal(m.rows.lastValues().size, 0);
    assert.equal(bodyRows(m.element).length, 0);
});

test('monitor: a failed Clear shows its error and keeps the rows', async () => {
    const api = fakeApi();
    api.clearTelegrams = async () => {
        throw new KnxApiError(409, 'monitor-off', 'x');
    };
    const m = await open({ api });
    m.rows.add(telegram(1));
    button(m.element, 'clear').click();
    await settle();
    assert.equal(m.errors.at(-1).code, 'monitor-off');
    assert.equal(m.rows.held(), 1);
});

test('monitor: a burst of 1000 rows is drawn in one frame as the newest 300; "Show more" adds 300', async () => {
    const m = await open();
    for (let n = 1; n <= 1000; n++) {
        m.rows.add(telegram(n));
    }
    assert.equal(m.pendingFrames(), 1);
    m.runFrames();
    assert.equal(PAGE_ROWS, 300);
    assert.equal(bodyRows(m.element).length, 300);
    assert.equal(bodyRows(m.element)[0].children[NUMBER].textContent, '701');
    assert.equal(bodyRows(m.element)[299].children[NUMBER].textContent, '1000');
    assert.equal(bodyRows(m.element)[0].children[TIME].textContent.length, 12);
    const more = button(m.element, 'more');
    assert.equal(more.parentNode.children[0], more);
    assert.equal(more.textContent, 'Show more');
    more.click();
    m.runFrames();
    assert.equal(bodyRows(m.element).length, 600);
    assert.equal(bodyRows(m.element)[0].children[NUMBER].textContent, '401');
    for (let turn = 0; turn < 2; turn++) {
        button(m.element, 'more').click();
        m.runFrames();
    }
    assert.equal(bodyRows(m.element).length, 1000);
    assert.equal(button(m.element, 'more'), null);
});

test('monitor: 5000 rows hold 2000 in the model and draw 300', async () => {
    const m = await open();
    for (let n = 1; n <= 5000; n++) {
        m.rows.add(telegram(n));
    }
    m.runFrames();
    assert.equal(m.rows.held(), 2000);
    assert.equal(bodyRows(m.element).length, 300);
    m.rows.add(telegram(5001));
    m.runFrames();
    assert.equal(bodyRows(m.element).length, 300);
    assert.equal(bodyRows(m.element)[299].children[NUMBER].textContent, '5001');
    assert.equal(bodyRows(m.element)[0].querySelector('.address').textContent, '7.7.3');
});

test('monitor: rows that stay are not made again', async () => {
    const m = await open();
    m.rows.add(telegram(1));
    m.runFrames();
    const first = bodyRows(m.element)[0];
    m.rows.add(telegram(2));
    m.runFrames();
    assert.equal(bodyRows(m.element)[0], first);
});

test('monitor: Stop pauses the drawing and counts the new rows while the stream goes on; Start draws them', async () => {
    const m = await open();
    m.rows.add(telegram(1));
    m.runFrames();
    const pause = button(m.element, 'pause');
    const start = button(m.element, 'start');
    assert.deepEqual([start.textContent, start.disabled, pause.textContent, pause.disabled], ['Start', true, 'Stop', false]);
    pause.focus();
    pause.click();
    m.runFrames();
    assert.deepEqual([start.disabled, pause.disabled], [false, true]);
    assert.ok(m.document.activeElement === start, 'the focus goes from the button that gave way to the other one');
    assert.equal(m.stream.starts, 1);
    assert.equal(m.stream.subscribers(), 1);
    m.rows.add(telegram(2));
    m.rows.add(telegram(3));
    m.runFrames();
    assert.equal(bodyRows(m.element).length, 1);
    assert.equal(m.element.querySelector('.fresh').textContent, '2 new');
    pause.click();
    assert.equal(m.rows.paused(), true, 'a click on the button that is off does nothing');
    start.click();
    m.runFrames();
    assert.deepEqual([start.disabled, pause.disabled], [true, false]);
    assert.equal(m.element.querySelector('.fresh').textContent, '');
    assert.equal(bodyRows(m.element).length, 3);
});

test('monitor: the filter fields set the page filter', async () => {
    const m = await open();
    m.rows.add(telegram(1, { destinationName: 'Kitchen' }));
    m.rows.add(telegram(2, { type: 'READ', direction: 'OUT' }));
    m.rows.add(telegram(3, { destination: '31/7/3' }));
    m.runFrames();
    const field = m.element.querySelector('.find-slot').querySelector('input');
    assert.equal(field.getAttribute('name'), 'filter');
    field.value = 'kitch';
    field.fire('input');
    m.runFrames();
    assert.deepEqual(bodyRows(m.element).map((row) => row.querySelector('.address').textContent), ['7.7.3']);
    field.value = '';
    field.fire('input');
    const [type, dir] = m.element.querySelector('.tools').querySelectorAll('select');
    assert.deepEqual(type.querySelectorAll('option').map((option) => option.getAttribute('value')), ['', 'WRITE', 'READ', 'RESPONSE', 'UNKNOWN']);
    assert.deepEqual(dir.querySelectorAll('option').map((option) => option.textContent), ['Both directions', 'from bus', 'to bus']);
    type.value = 'READ';
    type.fire('change');
    m.runFrames();
    assert.equal(bodyRows(m.element).length, 1);
    type.value = '';
    type.fire('change');
    dir.value = 'IN';
    dir.fire('change');
    m.runFrames();
    assert.equal(bodyRows(m.element).length, 2);
    assert.deepEqual(m.rows.filter(), { text: '', type: '', dir: 'IN' });
});

test('monitor: a stream stopped for good says why, with "Try again" that shows the view again; none where signing in is the cure', async () => {
    const m = await open();
    const note = m.element.querySelector('.stopped');
    assert.equal(note.textContent, '');
    for (const [code, words] of [
        ['monitor-off', 'The monitor of this bridge is switched off (buffer size 0)'],
        ['unknown-bridge', 'There is no such KNX bridge.'],
        ['bad-input', null],
        ['http-404', null],
    ]) {
        m.stream.set({ phase: 'stopped', code });
        if (words) {
            assert.equal(note.textContent.startsWith(words), true, code);
        }
        const button = note.querySelector('button');
        assert.equal(button?.textContent, 'Try again', code);
        const before = m.navigations.length;
        button.click();
        assert.deepEqual(m.navigations.slice(before), [m.context.route], code);
        m.stream.set({ phase: 'connecting' });
        assert.equal(note.textContent, '');
    }
    assert.equal(m.stream.retries, 0);
    m.stream.set({ phase: 'stopped', code: 'http-403' });
    assert.notEqual(note.textContent, '');
    assert.equal(note.querySelector('button'), null);
    m.stream.set({ phase: 'stopped', code: 'signed-out' });
    assert.equal(note.textContent, '');
    assert.equal(note.querySelector('button'), null);
});

test('monitor: close() leaves no listener, no timer and no drawing behind', async () => {
    const m = await open();
    m.rows.add(telegram(1));
    m.runFrames();
    assert.ok(m.document.listenerCount() > m.listenersBefore);
    m.rows.add(telegram(2));
    assert.equal(m.pendingFrames(), 1);
    m.view.close();
    assert.equal(m.document.listenerCount(), m.listenersBefore);
    assert.equal(m.clock.count(), 0);
    assert.equal(m.rows.subscribers(), 0);
    assert.equal(m.stream.subscribers(), 0);
    m.runFrames();
    assert.equal(bodyRows(m.element).length, 1);
    m.rows.add(telegram(3));
    assert.equal(m.pendingFrames(), 0);
});

test('monitor: close() while call 3 is on its way draws nothing afterwards', async () => {
    const api = fakeApi();
    let answer = null;
    api.monitor = () => new Promise((resolve) => (answer = resolve));
    const harness = viewContext({ bridge: BRIDGE, rows: createRows(), stream: fakeStream(), api });
    const view = show(harness.context);
    view.close();
    answer(MONITOR);
    await settle();
    assert.equal(harness.element.querySelector('.counters').textContent, '');
    assert.equal(harness.errors.length, 0);
});

test('monitor: "Try again" is off from the click until the stream leaves its stop, so repeated clicks send one navigation', async () => {
    const m = await open();
    const note = m.element.querySelector('.stopped');
    m.stream.set({ phase: 'stopped', code: 'unknown-bridge' });
    const button = note.querySelector('button');
    assert.equal(button.disabled, false);
    button.click();
    button.click();
    button.click();
    assert.equal(m.navigations.length, 1);
    assert.equal(note.querySelector('button').disabled, true);
    m.stream.set({ phase: 'connecting' });
    m.stream.set({ phase: 'stopped', code: 'unknown-bridge' });
    const again = note.querySelector('button');
    assert.equal(again.disabled, false);
    again.click();
    assert.equal(m.navigations.length, 2);
});

test('monitor: with the list full, a new telegram moves one row and leaves the others where they are', async () => {
    const m = await open();
    for (let n = 1; n <= PAGE_ROWS; n++) {
        m.rows.add(telegram(n));
    }
    m.runFrames();
    const body = m.element.querySelector('tbody');
    let moves = 0;
    const insertBefore = body.insertBefore.bind(body);
    body.insertBefore = (node, before) => {
        moves++;
        return insertBefore(node, before);
    };
    m.rows.add(telegram(PAGE_ROWS + 1));
    m.runFrames();
    assert.equal(moves, 1);
    assert.equal(bodyRows(m.element).length, PAGE_ROWS);
    assert.equal(bodyRows(m.element)[0].children[NUMBER].textContent, '2');
    assert.equal(bodyRows(m.element).at(-1).children[NUMBER].textContent, String(PAGE_ROWS + 1));
});

test('monitor: a heading sorts the telegrams also when lost and restart rows are held, which are left out meanwhile', async () => {
    const m = await open();
    m.rows.restart();
    m.rows.add(telegram(1, { destination: '31/7/9' }));
    m.rows.addLost(3);
    m.rows.add(telegram(2, { destination: '31/7/1' }));
    m.runFrames();
    assert.equal(bodyRows(m.element).length, 4);
    for (const at of [NUMBER, TIME, SERVICE, SOURCE, SOURCE_NAME, DESTINATION_NAME, TYPE, DPT, INFO, SECURITY, OUTCOME, DESTINATION]) {
        m.element.querySelector('thead').querySelectorAll('button')[at].click();
    }
    assert.deepEqual(bodyRows(m.element).map((row) => row.children[NUMBER].textContent), ['2', '1']);
    m.rows.add(telegram(3, { destination: '31/7/5' }));
    m.runFrames();
    assert.deepEqual(bodyRows(m.element).map((row) => row.children[NUMBER].textContent), ['2', '3', '1']);
});

test('monitor: a sorted list does not jump to its end', async () => {
    const m = await open();
    const list = m.element.querySelector('.list');
    Object.assign(list, { scrollHeight: 1000, clientHeight: 200, scrollTop: 0 });
    m.rows.add(telegram(1));
    m.runFrames();
    assert.equal(list.scrollTop, 1000);
    m.element.querySelector('thead').querySelectorAll('button')[DESTINATION].click();
    list.scrollHeight = 1100;
    m.rows.add(telegram(2));
    m.runFrames();
    assert.equal(list.scrollTop, 1000);
});

test('monitor: a third click on a heading shows the natural order again, with the lost and restart rows as the nodes they were', async () => {
    const m = await open();
    m.rows.add(telegram(1, { destination: '31/7/9' }));
    m.rows.addLost(3);
    m.rows.add(telegram(2, { destination: '31/7/1' }));
    m.runFrames();
    const marker = bodyRows(m.element)[1];
    assert.equal(marker.textContent, '3 telegrams were lost');
    const heading = m.element.querySelector('thead').querySelectorAll('button')[DESTINATION];
    heading.click();
    assert.deepEqual(bodyRows(m.element).map((row) => row.children[NUMBER].textContent), ['2', '1']);
    m.rows.add(telegram(3, { destination: '31/7/5' }));
    m.runFrames();
    heading.click();
    assert.deepEqual(bodyRows(m.element).map((row) => row.children[NUMBER].textContent), ['1', '3', '2']);
    heading.click();
    assert.deepEqual(bodyRows(m.element).map((row) => row.textContent.slice(0, 1)), ['1', '3', '2', '3']);
    assert.equal(bodyRows(m.element)[1], marker);
    assert.equal(heading.parentNode.getAttribute('aria-sort'), null);
});

test('monitor: sorted, a list that holds only lost or restart rows shows them and does not say that no telegram matches', async () => {
    const m = await open();
    m.rows.add(telegram(1));
    m.runFrames();
    m.element.querySelector('thead').querySelectorAll('button')[DESTINATION].click();
    m.rows.restart();
    m.rows.addLost(2);
    m.runFrames();
    assert.deepEqual(bodyRows(m.element).map((row) => row.textContent), ['The monitor started again', '2 telegrams were lost']);
    assert.equal(m.element.querySelector('.empty'), null);
    m.rows.add(telegram(1));
    m.runFrames();
    assert.deepEqual(bodyRows(m.element).map((row) => row.children[NUMBER].textContent), ['1']);
});

test('monitor: Stop says in its title that only the display pauses', async () => {
    const m = await open();
    const pause = button(m.element, 'pause');
    assert.equal(pause.getAttribute('title'), 'Pauses the display only. Telegrams are still recorded and show when it is started again.');
    assert.equal(pause.textContent, 'Stop');
    assert.equal(button(m.element, 'start').getAttribute('title'), null);
});

test('monitor: a selected row is named in the address by its telegram number, and an address with a number selects that row', async () => {
    const m = await open({ query: { dst: '31/7/1' } });
    m.rows.add(telegram(11));
    m.rows.add(telegram(12));
    m.runFrames();
    bodyRows(m.element)[1].fire('click');
    assert.deepEqual(m.replacements, [{ view: 'monitor', bridge: BRIDGE.uid, query: { dst: '31/7/1', row: '12' } }]);
    assert.deepEqual(m.navigations, []);

    const again = await open({ query: { row: '12' } });
    again.rows.add(telegram(11));
    again.runFrames();
    assert.equal(again.element.querySelector('.properties').textContent, 'Nothing is selected.');
    again.rows.add(telegram(12, { value: 'OFF' }));
    again.rows.add(telegram(13));
    again.runFrames();
    assert.deepEqual(bodyRows(again.element).map((row) => row.getAttribute('aria-selected')), ['false', 'true', 'false']);
    assert.equal(again.element.querySelector('.properties').querySelector('.subject').textContent, '# 12');
    assert.deepEqual(again.replacements, []);
    // the viewer's own choice wins over the address from then on
    bodyRows(again.element)[0].fire('click');
    again.rows.clear();
    again.rows.add(telegram(12));
    again.runFrames();
    assert.deepEqual(bodyRows(again.element).map((row) => row.getAttribute('aria-selected')), ['false']);
});

test('monitor: the focused row keeps the focus while telegrams come, and hands it on when it leaves the list', async () => {
    const m = await open();
    for (let n = 1; n <= PAGE_ROWS; n++) {
        m.rows.add(telegram(n));
    }
    m.runFrames();
    const second = bodyRows(m.element)[1];
    second.focus();
    m.rows.add(telegram(PAGE_ROWS + 1));
    m.runFrames();
    assert.equal(m.document.activeElement, second);
    assert.equal(bodyRows(m.element)[0], second);
    m.rows.add(telegram(PAGE_ROWS + 2));
    m.runFrames();
    assert.equal(m.document.activeElement, bodyRows(m.element)[0]);
    assert.equal(bodyRows(m.element)[0].children[NUMBER].textContent, '3');
    button(m.element, 'clear').focus();
    m.rows.add(telegram(PAGE_ROWS + 3));
    m.runFrames();
    assert.equal(m.document.activeElement, button(m.element, 'clear'));
});

test('monitor: opened from another panel by key, the focus goes to its list', async () => {
    const m = await open();
    m.view.focus();
    assert.equal(m.document.activeElement, m.element.querySelector('.list'));
    m.rows.add(telegram(1));
    m.runFrames();
    m.view.focus();
    assert.equal(m.document.activeElement, bodyRows(m.element)[0]);
});

const LOADED = { ...BRIDGE, projectState: 'LOADED' };
const SPACES = [
    {
        id: 's1',
        name: 'House',
        spaces: [{ id: 's2', name: 'Kitchen <east>', spaces: [], functions: [{ id: 'f1', name: 'Ceiling <light>', groupAddresses: [{ groupAddress: '31/7/1' }] }] }],
        functions: [],
    },
];
const DEVICES = [{ id: 'd1', address: '7.7.3', name: 'Push button', room: 'Hall' }];

function projectApi({ locations = SPACES, devices = DEVICES } = {}) {
    const api = fakeApi();
    api.reads = [];
    api.locations = async (bridge) => {
        api.reads.push(['locations', bridge]);
        if (locations instanceof Error) {
            throw locations;
        }
        return locations;
    };
    api.allDevices = async (bridge) => {
        api.reads.push(['devices', bridge]);
        if (devices instanceof Error) {
            throw devices;
        }
        return devices;
    };
    return api;
}

async function openLoaded(api = projectApi(), rows = createRows({ bridge: LOADED.uid, api })) {
    const harness = viewContext({ bridge: LOADED, rows, stream: fakeStream('live'), api, route: { view: 'monitor', bridge: LOADED.uid, query: {} } });
    const view = show(harness.context);
    await settle();
    harness.runFrames();
    return Object.assign(harness, { view, rows, api });
}

test('monitor: Building Function and Building Part come from the project: the function and space of the destination, else the room of the source device', async () => {
    const m = await openLoaded();
    assert.deepEqual(m.api.reads, [['locations', LOADED.uid], ['devices', LOADED.uid]]);
    m.rows.add(telegram(1));
    m.rows.add(telegram(2, { destination: '31/7/2' }));
    m.rows.add(telegram(3, { destination: '31/7/2', source: '7.7.9' }));
    m.runFrames();
    assert.deepEqual(headings(m.element).slice(BUILDING_FUNCTION, TYPE), ['Building Function', 'Building Part']);
    assert.deepEqual(bodyRows(m.element).map((row) => cells(row).slice(BUILDING_FUNCTION, TYPE)), [
        ['Ceiling <light>', 'Kitchen <east>'],
        ['', 'Hall'],
        ['', ''],
    ]);
    assert.equal(bodyRows(m.element)[0].children[BUILDING_FUNCTION].children.length, 0, 'names of the project are text');
    // the find box of the monitor's columns is the page filter; the two columns sort like the others
    headings(m.element);
    m.element.querySelector('thead').querySelectorAll('button')[BUILDING_PART].click();
    assert.deepEqual(bodyRows(m.element).map((row) => row.children[BUILDING_PART].textContent), ['', 'Hall', 'Kitchen <east>']);
    bodyRows(m.element)[2].fire('click');
    const props = textNodes(m.element.querySelector('.properties').querySelector('.tab-information'));
    assert.deepEqual(props.slice(props.indexOf('Building Function'), props.indexOf('Building Function') + 4), ['Building Function', 'Ceiling <light>', 'Building Part', 'Kitchen <east>']);
});

test('monitor: telegrams that were drawn before the project was read get their two columns when it comes', async () => {
    let release = null;
    const api = projectApi();
    const read = api.locations;
    api.locations = (bridge) => new Promise((resolve) => (release = () => resolve(read(bridge))));
    const rows = createRows({ bridge: LOADED.uid, api });
    rows.add(telegram(1));
    const m = await openLoaded(api, rows);
    const row = bodyRows(m.element)[0];
    assert.deepEqual(cells(row).slice(BUILDING_FUNCTION, TYPE), ['', '']);
    release();
    await settle();
    assert.ok(bodyRows(m.element)[0] === row, 'the row is not made again');
    assert.deepEqual(cells(row).slice(BUILDING_FUNCTION, TYPE), ['Ceiling <light>', 'Kitchen <east>']);
});

test('monitor: without a loaded project the project is not asked; one failing list leaves the other column, and no error line', async () => {
    const api = projectApi();
    const plain = viewContext({ bridge: BRIDGE, rows: createRows({ bridge: BRIDGE.uid, api }), stream: fakeStream('live'), api, route: { view: 'monitor', bridge: BRIDGE.uid, query: {} } });
    show(plain.context);
    await settle();
    assert.deepEqual(api.reads, []);
    const m = await openLoaded(projectApi({ locations: new KnxApiError(500, 'internal', 'x') }));
    m.rows.add(telegram(1));
    m.runFrames();
    assert.deepEqual(cells(bodyRows(m.element)[0]).slice(BUILDING_FUNCTION, TYPE), ['', 'Hall']);
    assert.deepEqual(m.errors.filter(Boolean), []);
    const none = await openLoaded(projectApi({ locations: new KnxApiError(0, 'network'), devices: new KnxApiError(0, 'network') }));
    none.rows.add(telegram(1));
    none.runFrames();
    assert.deepEqual(cells(bodyRows(none.element)[0]).slice(BUILDING_FUNCTION, TYPE), ['', '']);
    none.view.close();
    await settle();
});

test('monitor: devices and places come from the lists the row model keeps: a second monitor on the same bridge reads nothing again', async () => {
    const api = projectApi();
    const rows = createRows({ bridge: LOADED.uid, api });
    const first = await openLoaded(api, rows);
    first.view.close();
    const second = await openLoaded(api, rows);
    assert.deepEqual(api.reads, [['locations', LOADED.uid], ['devices', LOADED.uid]]);
    second.rows.add(telegram(1));
    second.runFrames();
    assert.deepEqual(cells(bodyRows(second.element)[0]).slice(BUILDING_FUNCTION, TYPE), ['Ceiling <light>', 'Kitchen <east>']);
    rows.forgetProject();
    await openLoaded(api, rows);
    assert.equal(api.reads.length, 4, 'another project is read again');
});

test('monitor: rows are tinted by their type, write, read and response; another type has no tint', async () => {
    const m = await open();
    m.rows.add(telegram(1));
    m.rows.add(telegram(2, { type: 'READ', value: undefined, payload: undefined }));
    m.rows.add(telegram(3, { type: 'RESPONSE', direction: 'OUT', outcome: 'SENT' }));
    m.rows.add(telegram(4, { type: 'UNKNOWN', security: 'NOT_DECRYPTED' }));
    m.runFrames();
    assert.deepEqual(bodyRows(m.element).map((row) => row.className), ['type-write', 'type-read', 'type-response own', 'not-decrypted']);
});

test('monitor: a selected telegram shows all its fields under Information, which is the tab shown, and Live follows its destination', async () => {
    const m = await open();
    m.rows.add(telegram(1, { value: 'ON' }));
    m.rows.add(telegram(2, { destination: '31/7/2' }));
    m.rows.add(telegram(3, { value: 'OFF' }));
    m.runFrames();
    bodyRows(m.element)[0].fire('click');
    const properties = m.element.querySelector('.properties');
    assert.equal(properties.querySelector('.tab-settings').textContent, 'Nothing here for this selection.');
    assert.deepEqual(['settings', 'comments', 'information'].map((name) => properties.querySelector(`.tab-${name}`).classList.contains('off')), [true, true, false]);
    const fields = textNodes(properties.querySelector('.tab-information'));
    assert.deepEqual(fields.filter((part, at) => at % 2 === 0).slice(0, 8), ['#', 'Time', 'Service', 'Source Address', 'Destination Address', 'Type', 'Value', 'Raw bytes']);
    const live = m.element.querySelector('.live');
    assert.deepEqual(live.querySelectorAll('li').map((line) => line.querySelector('.value').textContent), ['OFF', 'ON']);
    bodyRows(m.element)[1].fire('click');
    assert.equal(live.querySelectorAll('li').length, 1);
});

test('monitor: on a bridge that does not allow sending the panel has nothing that sends: no field for a value, no button but Start, Stop, Clear and what opens or closes a part of the page', async () => {
    const m = await open();
    m.rows.add(telegram(1));
    m.runFrames();
    bodyRows(m.element)[0].fire('click');
    const names = m.element.querySelector('.panel').querySelectorAll('button').map((node) => node.className).filter((name) => name !== 'sort');
    assert.deepEqual(names, ['tool nav-back', 'tool nav-forward', 'crumb current', 'start', 'pause', 'clear']);
    assert.deepEqual(m.element.querySelector('.panel').querySelectorAll('input').map((field) => field.getAttribute('name')), ['filter']);
    assert.deepEqual(Object.keys(m.api).filter((name) => typeof m.api[name] === 'function').sort(), ['clearTelegrams', 'monitor'], 'the stand-in has no call that could send, and the panel asked for none');
    assert.equal(m.element.querySelectorAll('.send-bar').length, 1);
    assert.equal(m.element.querySelector('.send-bar').children.length, 1, 'the bar is its one line');
    assert.ok(m.element.querySelector('.send-off'));
});

test('monitor: "Try again" after a row was selected shows the address of the page again and makes no new one', async () => {
    const stream = fakeStream('live');
    const m = await open({ stream });
    m.rows.add(telegram(7));
    m.runFrames();
    bodyRows(m.element)[0].fire('click');
    assert.deepEqual(m.replacements.at(-1).query, { row: '7' });
    stream.set({ phase: 'stopped', code: 'bridge-gone' });
    button(m.element, 'retry').click();
    assert.deepEqual(m.navigations, [{ view: 'monitor', bridge: BRIDGE.uid, query: { row: '7' } }], 'the address as the panel last set it, which the shell shows again');
});

test('monitor: "Show more" that takes itself away with the last batch hands the focus to the list', async () => {
    const m = await open();
    for (let n = 1; n <= PAGE_ROWS + 5; n++) {
        m.rows.add(telegram(n));
    }
    m.runFrames();
    const more = m.element.querySelector('.more');
    more.focus();
    more.click();
    m.runFrames();
    assert.equal(m.element.querySelector('.more'), null);
    assert.ok(m.document.activeElement === bodyRows(m.element)[0]);
});
