import { test } from 'node:test';
import assert from 'node:assert/strict';

import { createApi } from '../web/js/api.js';
import { createRows } from '../web/js/rows.js';
import { BREAK_WAITS, BUSY_WAITS, SILENCE_MILLIS, START_ROWS, createStream, stateText } from '../web/js/stream.js';
import { createEventBody, createFetch, createTimers, empty, json, networkFailure, settle } from './fakes.js';

const BRIDGE = 'knx:ip:line7';
const BASE = new URL('http://openhab.invalid/rest/');
const PAGE_PATH = '/rest/knx/bridges/knx%3Aip%3Aline7/telegrams';
const STREAM_PATH = `${PAGE_PATH}/stream`;

function telegram(number, more = {}) {
    return {
        number,
        time: '2026-11-15T07:01:02.123Z',
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

const PAGE = { telegrams: [telegram(99), telegram(100), telegram(101)], lastNumber: 100, monitorId: 'm1', more: false, reset: false };

function harness({ pages = [PAGE], streams = [] } = {}) {
    const clock = createTimers();
    const rows = createRows();
    const bodies = [];
    const answers = [...streams];
    const streamTimes = [];
    const auth = {
        current: 't1',
        renewed: 0,
        lostCalls: 0,
        token: async () => auth.current,
        renew: async () => {
            auth.renewed++;
            auth.current = `t${auth.renewed + 1}`;
            return auth.current;
        },
        lost() {
            auth.lostCalls++;
        },
    };
    const fetch = createFetch((call) => {
        if (call.url.pathname === STREAM_PATH) {
            streamTimes.push(clock.now());
            const body = createEventBody(call.init.signal);
            bodies.push(body);
            const answer = answers.shift();
            return (answer && answer(body, call)) || body.response;
        }
        if (call.url.pathname === PAGE_PATH) {
            const page = pages.length > 1 ? pages.shift() : pages[0];
            return typeof page === 'function' ? page(call) : json(200, page);
        }
        throw new Error(`unexpected address ${call.url}`);
    });
    const api = createApi({ fetch, base: BASE, auth });
    const states = [];
    const stream = createStream({ bridge: BRIDGE, rows, api, auth, fetch, base: BASE, timers: clock.timers, now: clock.now });
    stream.subscribe((state) => states.push(state));
    const calls = (path) => fetch.calls.filter((call) => call.url.pathname === path);
    return {
        clock,
        rows,
        bodies,
        fetch,
        auth,
        stream,
        states,
        streamTimes,
        pageCalls: () => calls(PAGE_PATH),
        streamCalls: () => calls(STREAM_PATH),
        body: () => bodies.at(-1),
    };
}

function query(call) {
    return Object.fromEntries(call.url.searchParams.entries());
}

function gaps(times) {
    return times.slice(1).map((time, at) => time - times[at]);
}

function numbers(rows) {
    return rows.entries().map((entry) => entry.telegram?.number ?? entry.marker);
}

const open = (more = {}) => (body) => {
    body.send('open', { lastNumber: 100, monitorId: 'm1', ...more });
};
const openThenClose = (body) => {
    open()(body);
    body.close();
};
const endThenClose = (code) => (body) => {
    body.send('end', { code, text: 'server words' });
    body.close();
};
const closeAtOnce = (body) => {
    body.close();
};

test('stream: the start reads call 4 with limit 2000, then opens call 20 after its last number with its monitor id', async () => {
    const h = harness({ streams: [open()] });
    h.stream.start();
    h.stream.start();
    await settle();
    assert.deepEqual(h.fetch.calls.map((call) => call.url.pathname), [PAGE_PATH, STREAM_PATH]);
    assert.equal(START_ROWS, 2000);
    assert.deepEqual(query(h.pageCalls()[0]), { limit: '2000' });
    assert.deepEqual(query(h.streamCalls()[0]), { after: '100', monitor: 'm1' });
    assert.deepEqual(numbers(h.rows), [99, 100, 101]);
    assert.deepEqual(h.states.map((state) => state.phase), ['connecting', 'live']);
    assert.equal(h.stream.state().phase, 'live');
});

test('stream: call 20 sends the token header and Accept: text/event-stream, nothing else', async () => {
    const h = harness({ streams: [open()] });
    h.stream.start();
    await settle();
    const call = h.streamCalls()[0];
    assert.deepEqual(call.headers, { 'X-OPENHAB-TOKEN': 't1', Accept: 'text/event-stream' });
    assert.deepEqual(Object.keys(call.init).sort(), ['headers', 'method', 'signal']);
    assert.equal(call.method, 'GET');
    assert.equal(call.body, undefined);
});

test('stream: telegrams go into the rows, and a row already held from call 4 is dropped', async () => {
    const h = harness({ streams: [open()] });
    h.stream.start();
    await settle();
    h.body().send('telegram', telegram(101, { value: 'OFF' }));
    h.body().send('telegram', telegram(102, { value: 'OFF' }));
    await settle();
    assert.deepEqual(numbers(h.rows), [99, 100, 101, 102]);
    assert.equal(h.rows.entries()[2].telegram.value, 'ON');
    assert.equal(h.rows.last('31/7/1').number, 102);
});

test('stream: a body that ends without end is a break: reopen after 1 s with the last number received and the monitor id', async () => {
    const h = harness({ streams: [open()] });
    h.stream.start();
    await settle();
    h.body().send('telegram', telegram(102));
    h.body().send('telegram', telegram(103));
    h.body().close();
    await settle();
    assert.equal(h.stream.state().phase, 'waiting');
    assert.equal(h.stream.state().wait, 1000);
    await h.clock.advance(999);
    assert.equal(h.streamCalls().length, 1);
    await h.clock.advance(1);
    assert.equal(h.streamCalls().length, 2);
    assert.deepEqual(query(h.streamCalls()[1]), { after: '103', monitor: 'm1' });
    assert.equal(h.pageCalls().length, 1);
});

test('stream: the waits between breaks are 1, 2, 5, 10, 30 s, then every 30 s, and back to 1 s after a stream that delivered open', async () => {
    const streams = [closeAtOnce, closeAtOnce, closeAtOnce, closeAtOnce, closeAtOnce, closeAtOnce, openThenClose, closeAtOnce, open()];
    const h = harness({ streams });
    h.stream.start();
    await settle();
    await h.clock.advance(81_000);
    assert.deepEqual(BREAK_WAITS, [1000, 2000, 5000, 10000, 30000]);
    assert.deepEqual(gaps(h.streamTimes), [1000, 2000, 5000, 10000, 30000, 30000, 1000, 2000]);
    assert.equal(h.stream.state().phase, 'live');
});

test('stream: end monitor-off stops for good, with the body closing right after; "Try again" starts again from call 4', async () => {
    const h = harness({ streams: [endThenClose('monitor-off'), open()] });
    h.stream.start();
    await settle();
    assert.deepEqual(h.stream.state(), { phase: 'stopped', code: 'monitor-off' });
    await h.clock.advance(600_000);
    assert.equal(h.streamCalls().length, 1);
    assert.equal(h.clock.count(), 0);
    assert.equal(stateText(h.stream.state()), 'Stopped: The monitor of this bridge is switched off (buffer size 0)');
    h.stream.retry();
    await settle();
    assert.equal(h.pageCalls().length, 2);
    assert.equal(h.streamCalls().length, 2);
    assert.equal(h.stream.state().phase, 'live');
});

test('stream: end too-many-streams tries again only after 10 s, then 20 s, then every 30 s', async () => {
    const busy = endThenClose('too-many-streams');
    const h = harness({ streams: [busy, busy, busy, busy, open()] });
    h.stream.start();
    await settle();
    assert.deepEqual(h.stream.state(), { phase: 'waiting', code: 'too-many-streams', wait: 10000 });
    assert.equal(stateText(h.stream.state()), 'Too many pages are open; waiting for a free place');
    await h.clock.advance(9999);
    assert.equal(h.streamCalls().length, 1);
    await h.clock.advance(100_000);
    assert.deepEqual(BUSY_WAITS, [10000, 20000, 30000]);
    assert.deepEqual(gaps(h.streamTimes), [10000, 20000, 30000, 30000]);
    assert.equal(h.stream.state().phase, 'live');
    assert.equal(h.pageCalls().length, 1);
});

test('stream: end bridge-gone shows the restart and reopens; the open with reset empties the rows and the new id is kept', async () => {
    const first = (body) => {
        open()(body);
        body.send('telegram', telegram(102));
        body.send('end', { code: 'bridge-gone', text: 'x' });
        body.close();
    };
    const second = (body) => {
        body.send('open', { lastNumber: 0, monitorId: 'm2', reset: true });
        body.send('telegram', telegram(1, { destination: '31/7/9' }));
    };
    const h = harness({ streams: [first, second] });
    h.stream.start();
    await settle();
    assert.equal(h.stream.state().code, 'bridge-gone');
    assert.equal(stateText(h.stream.state()), 'The bridge restarted; reconnecting');
    await h.clock.advance(1000);
    assert.deepEqual(query(h.streamCalls()[1]), { after: '102', monitor: 'm1' });
    assert.deepEqual(numbers(h.rows), ['restarted', 1]);
    assert.equal(h.stream.state().phase, 'live');
    h.body().close();
    await settle();
    await h.clock.advance(1000);
    assert.deepEqual(query(h.streamCalls()[2]), { after: '1', monitor: 'm2' });
});

test('stream: after bridge-gone a reopen that answers unknown-bridge stops', async () => {
    const h = harness({ streams: [endThenClose('bridge-gone'), endThenClose('unknown-bridge')] });
    h.stream.start();
    await settle();
    await h.clock.advance(600_000);
    assert.equal(h.streamCalls().length, 2);
    assert.deepEqual(h.stream.state(), { phase: 'stopped', code: 'unknown-bridge' });
});

test('stream: end bad-input and unknown-bridge stop', async () => {
    for (const code of ['bad-input', 'unknown-bridge']) {
        const h = harness({ streams: [endThenClose(code)] });
        h.stream.start();
        await settle();
        await h.clock.advance(600_000);
        assert.equal(h.streamCalls().length, 1, code);
        assert.deepEqual(h.stream.state(), { phase: 'stopped', code }, code);
    }
});

test('stream: end slow-client, shutdown, internal and an unknown code reopen', async () => {
    for (const code of ['slow-client', 'shutdown', 'internal', 'some-new-code']) {
        const h = harness({ streams: [endThenClose(code), open()] });
        h.stream.start();
        await settle();
        assert.equal(h.stream.state().phase, 'waiting', code);
        await h.clock.advance(1000);
        assert.equal(h.streamCalls().length, 2, code);
        assert.equal(h.stream.state().phase, 'live', code);
    }
});

test('stream: the end event decides even when the body stays open, and the request is aborted', async () => {
    const h = harness({ streams: [(body) => body.send('end', { code: 'monitor-off', text: 'x' })] });
    h.stream.start();
    await settle();
    assert.deepEqual(h.stream.state(), { phase: 'stopped', code: 'monitor-off' });
    assert.equal(h.bodies[0].aborted(), true);
});

test('stream: an open with reset empties the rows and the numbers count from there', async () => {
    const h = harness({ streams: [(body) => body.send('open', { lastNumber: 3, monitorId: 'm9', reset: true })] });
    h.stream.start();
    await settle();
    assert.deepEqual(numbers(h.rows), ['restarted']);
    h.body().send('telegram', telegram(1));
    h.body().send('telegram', telegram(2));
    h.body().close();
    await settle();
    await h.clock.advance(1000);
    assert.deepEqual(query(h.streamCalls()[1]), { after: '2', monitor: 'm9' });
});

test('stream: lost adds a marker row, outcome sets the outcome of its row, alive keeps the row count', async () => {
    const h = harness({ streams: [open()] });
    h.stream.start();
    await settle();
    h.body().send('telegram', telegram(102, { direction: 'OUT', outcome: 'PENDING' }));
    h.body().send('lost', { count: 4 });
    h.body().send('outcome', { number: 102, outcome: 'SENT' });
    h.body().send('alive', { lastNumber: 200 });
    await settle();
    assert.deepEqual(numbers(h.rows), [99, 100, 101, 102, 'lost']);
    assert.equal(h.rows.entries()[4].count, 4);
    assert.equal(h.rows.entries()[3].telegram.outcome, 'SENT');
    h.body().close();
    await settle();
    await h.clock.advance(1000);
    assert.equal(query(h.streamCalls()[1]).after, '102');
});

test('stream: an event whose data is not JSON is left out, the stream goes on', async () => {
    const h = harness({ streams: [open()] });
    h.stream.start();
    await settle();
    h.body().raw('event: telegram\ndata: {broken\n\nevent: telegram\ndata: null\n\n');
    h.body().send('telegram', telegram(102));
    await settle();
    assert.deepEqual(numbers(h.rows), [99, 100, 101, 102]);
    assert.equal(h.stream.state().phase, 'live');
});

test('stream: 25 s without any event is a break; alive keeps the stream open', async () => {
    const h = harness({ streams: [open(), open()] });
    h.stream.start();
    await settle();
    await h.clock.advance(20_000);
    h.body().send('alive', { lastNumber: 100 });
    await settle();
    await h.clock.advance(SILENCE_MILLIS - 1);
    assert.equal(h.streamCalls().length, 1);
    assert.equal(h.bodies[0].aborted(), false);
    await h.clock.advance(1);
    assert.equal(h.bodies[0].aborted(), true);
    assert.equal(h.stream.state().phase, 'waiting');
    await h.clock.advance(1000);
    assert.equal(h.streamCalls().length, 2);
    assert.equal(SILENCE_MILLIS, 25_000);
});

test('stream: a 401 on opening renews the token once and opens again with the new one', async () => {
    const h = harness({ streams: [() => empty(401), open()] });
    h.stream.start();
    await settle();
    assert.equal(h.auth.renewed, 1);
    assert.deepEqual(h.streamCalls().map((call) => call.headers['X-OPENHAB-TOKEN']), ['t1', 't2']);
    assert.equal(h.stream.state().phase, 'live');
});

test('stream: a second 401 ends the login and the stream', async () => {
    const h = harness({ streams: [() => empty(401), () => empty(401)] });
    h.stream.start();
    await settle();
    assert.equal(h.auth.lostCalls, 1);
    assert.deepEqual(h.stream.state(), { phase: 'stopped', code: 'signed-out' });
    await h.clock.advance(600_000);
    assert.equal(h.streamCalls().length, 2);
});

test('stream: call 4 answering monitor-off stops before call 20; no answer tries call 4 again', async () => {
    const off = harness({ pages: [() => json(409, { code: 'monitor-off', text: 'x' })] });
    off.stream.start();
    await settle();
    await off.clock.advance(600_000);
    assert.equal(off.streamCalls().length, 0);
    assert.equal(off.pageCalls().length, 1);
    assert.deepEqual(off.stream.state(), { phase: 'stopped', code: 'monitor-off' });

    const unreachable = harness({
        pages: [
            () => {
                throw networkFailure();
            },
            PAGE,
        ],
        streams: [open()],
    });
    unreachable.stream.start();
    await settle();
    assert.deepEqual(unreachable.stream.state(), { phase: 'waiting', code: 'network', wait: 1000 });
    await unreachable.clock.advance(1000);
    assert.equal(unreachable.pageCalls().length, 2);
    assert.equal(unreachable.stream.state().phase, 'live');
});

test('stream: a stream that cannot be reached is a break', async () => {
    const h = harness({
        streams: [
            () => {
                throw networkFailure();
            },
            () => empty(502),
            open(),
        ],
    });
    h.stream.start();
    await settle();
    assert.equal(h.stream.state().phase, 'waiting');
    await h.clock.advance(3000);
    assert.deepEqual(gaps(h.streamTimes), [1000, 2000]);
    assert.equal(h.stream.state().phase, 'live');
});

test('stream: 5000 telegrams in one burst leave 2000 rows and the last number for the reopen', async () => {
    const h = harness({ streams: [open()] });
    h.stream.start();
    await settle();
    let text = '';
    for (let n = 101; n <= 5100; n++) {
        text += `event: telegram\ndata: ${JSON.stringify(telegram(n))}\n\n`;
    }
    h.body().raw(text);
    await settle();
    assert.equal(h.rows.held(), 2000);
    assert.equal(h.rows.entries().at(-1).telegram.number, 5100);
    h.body().close();
    await settle();
    await h.clock.advance(1000);
    assert.equal(query(h.streamCalls()[1]).after, '5100');
});

test('stream: close aborts the request, stops every timer and calls nothing more', async () => {
    const h = harness({ streams: [open()] });
    h.stream.start();
    await settle();
    assert.ok(h.clock.count() > 0);
    const seen = h.states.length;
    h.stream.close();
    await settle();
    assert.equal(h.bodies[0].aborted(), true);
    assert.equal(h.clock.count(), 0);
    await h.clock.advance(600_000);
    assert.equal(h.fetch.calls.length, 2);
    assert.equal(h.states.length, seen);
    h.stream.start();
    h.stream.retry();
    await settle();
    assert.equal(h.fetch.calls.length, 2);
});

test('stream: close while waiting, or while call 4 is on its way, calls nothing more', async () => {
    const waiting = harness({ streams: [closeAtOnce] });
    waiting.stream.start();
    await settle();
    assert.equal(waiting.stream.state().phase, 'waiting');
    waiting.stream.close();
    assert.equal(waiting.clock.count(), 0);
    await waiting.clock.advance(600_000);
    assert.equal(waiting.streamCalls().length, 1);

    let answer = null;
    const slow = harness({ pages: [() => new Promise((resolve) => (answer = resolve))] });
    slow.stream.start();
    await settle();
    slow.stream.close();
    answer(json(200, PAGE));
    await settle();
    assert.equal(slow.streamCalls().length, 0);
    assert.deepEqual(slow.rows.entries(), []);
});

test('stream: a listener can leave', async () => {
    const h = harness({ streams: [open()] });
    const seen = [];
    const leave = h.stream.subscribe((state) => seen.push(state.phase));
    leave();
    h.stream.start();
    await settle();
    assert.deepEqual(seen, []);
    assert.equal(h.stream.subscribers(), 1);
});

test('stream: the state as text', () => {
    assert.equal(stateText({ phase: 'idle' }), '');
    assert.equal(stateText({ phase: 'connecting' }), 'Connecting to the monitor');
    assert.equal(stateText({ phase: 'live' }), 'Live');
    assert.equal(stateText({ phase: 'waiting', code: 'network', wait: 5000 }), 'The connection to the monitor broke; trying again in 5 s');
    assert.equal(stateText({ phase: 'waiting', code: 'slow-client', wait: 1000 }), 'The page fell too far behind; reconnecting');
    assert.equal(stateText({ phase: 'stopped', code: 'unknown-bridge' }), 'Stopped: There is no such KNX bridge.');
    assert.equal(stateText({ phase: 'stopped', code: 'new-code' }), 'Stopped: Something went wrong (new-code).');
});

test('stream: call 20 that gets no answer headers within 25 s is aborted and is a break like any other', async () => {
    const never = () => new Promise(() => {});
    const h = harness({ streams: [never, never, open()] });
    h.stream.start();
    await settle();
    assert.equal(h.streamCalls().length, 1);
    await h.clock.advance(SILENCE_MILLIS - 1);
    assert.deepEqual(h.stream.state(), { phase: 'connecting' });
    await h.clock.advance(1);
    assert.equal(h.streamCalls()[0].init.signal.aborted, true);
    assert.deepEqual(h.stream.state(), { phase: 'waiting', wait: BREAK_WAITS[0] });
    assert.equal(stateText(h.stream.state()), 'The connection to the monitor broke; trying again in 1 s');
    await h.clock.advance(BREAK_WAITS[0]);
    assert.equal(h.streamCalls().length, 2);
    await h.clock.advance(SILENCE_MILLIS);
    assert.equal(h.streamCalls()[1].init.signal.aborted, true);
    assert.deepEqual(h.stream.state(), { phase: 'waiting', wait: BREAK_WAITS[1] });
    await h.clock.advance(BREAK_WAITS[1]);
    assert.equal(h.streamCalls().length, 3);
    assert.deepEqual(query(h.streamCalls()[2]), { after: '100', monitor: 'm1' });
    assert.deepEqual(h.stream.state(), { phase: 'live' });
    assert.deepEqual(h.clock.delays(), [SILENCE_MILLIS]);
    h.stream.close();
    assert.equal(h.clock.count(), 0);
});
