import { test } from 'node:test';
import assert from 'node:assert/strict';

import { LIVE_ROWS, createLive, liveTelegrams } from '../web/js/live.js';
import { createRows } from '../web/js/rows.js';
import { createDocument, textNodes } from './fakedom.js';

function telegram(number, destination, more = {}) {
    return { number, time: '2026-11-15T07:01:02.250Z', source: '31.1.9', destination, type: 'WRITE', direction: 'IN', payload: '01', value: 'ON', security: 'PLAIN', ...more };
}

function filled() {
    const rows = createRows();
    rows.add(telegram(1, '31/0/1'));
    rows.add(telegram(2, '31/0/2', { value: 'OFF' }));
    rows.add(telegram(3, '31/0/1', { source: '31.1.5', value: undefined, payload: '0C1A', type: 'RESPONSE' }));
    rows.addLost(4);
    rows.add(telegram(5, '31/0/7', { source: '31.1.5', value: '21.5', unit: '°C', destinationName: 'Hall <b>' }));
    return rows;
}

function lines(node) {
    return node.querySelectorAll('li').map((line) => textNodes(line).filter((part) => part !== ''));
}

test('live: the newest telegrams of the watched group addresses or of the watched source, newest first, at most five', () => {
    const entries = filled().entries();
    assert.deepEqual(liveTelegrams(entries, { addresses: ['31/0/1'] }).map((t) => t.number), [3, 1]);
    assert.deepEqual(liveTelegrams(entries, { source: '31.1.5' }).map((t) => t.number), [5, 3]);
    assert.deepEqual(liveTelegrams(entries, { source: '31.1.5', addresses: ['31/0/2'] }).map((t) => t.number), [5, 3, 2]);
    assert.deepEqual(liveTelegrams(entries, { addresses: ['31/0/9'] }), []);
    assert.deepEqual(liveTelegrams(entries, {}), []);
    assert.deepEqual(liveTelegrams(entries, null), []);
    const many = createRows();
    for (let n = 1; n <= 40; n++) {
        many.add(telegram(n, '31/0/1'));
    }
    assert.equal(LIVE_ROWS, 5);
    assert.deepEqual(liveTelegrams(many.entries(), { addresses: ['31/0/1'] }).map((t) => t.number), [40, 39, 38, 37, 36]);
});

test('live: nothing watched says so; a watched address lists its telegrams with time, addresses, type and value, all as text', () => {
    const document = createDocument();
    const rows = filled();
    const live = createLive({ document, rows });
    assert.equal(live.node.textContent, 'Select a group address or a device to see its telegrams.');
    live.watch({ addresses: ['31/0/7', '31/0/1'] });
    assert.deepEqual(lines(live.node).map((line) => line.slice(1)), [
        ['31.1.5', '31/0/7', 'Write', '21.5 °C'],
        ['31.1.5', '31/0/1', 'Response', '0C1A'],
        ['31.1.9', '31/0/1', 'Write', 'ON'],
    ]);
    assert.match(lines(live.node)[0][0], /^\d\d:\d\d:\d\d\.250$/);
    const first = live.node.querySelector('li');
    assert.equal(first.querySelector('.destination').getAttribute('title'), 'Hall <b>');
    assert.ok(first.querySelectorAll('span').every((part) => part.children.length === 0));
    live.watch({ addresses: ['31/0/99'] });
    assert.equal(live.node.textContent, 'No telegram of it has been seen yet.');
    live.watch(null);
    assert.equal(live.node.textContent, 'Select a group address or a device to see its telegrams.');
    live.watch({ addresses: [], source: '' });
    assert.equal(live.node.textContent, 'Select a group address or a device to see its telegrams.');
});

test('live: a new telegram of the stream shows with the next frame; one of another address changes nothing', () => {
    const document = createDocument();
    const rows = filled();
    const frames = [];
    const live = createLive({ document, rows, frame: (callback) => frames.push(callback) });
    live.watch({ source: '31.1.5' });
    assert.equal(lines(live.node).length, 2);
    const [first] = live.node.querySelectorAll('li');
    rows.add(telegram(6, '31/0/2'));
    assert.equal(frames.length, 1);
    frames.shift()();
    assert.ok(live.node.querySelectorAll('li')[0] === first, 'what is shown is not made again');
    rows.add(telegram(7, '31/0/8', { source: '31.1.5', value: '7' }));
    rows.add(telegram(8, '31/0/8', { source: '31.1.5', value: '8' }));
    assert.equal(frames.length, 1, 'one drawing for a burst');
    frames.shift()();
    assert.deepEqual(lines(live.node).map((line) => line.at(-1)), ['8', '7', '21.5 °C', '0C1A']);
    rows.clear();
    frames.shift()();
    assert.equal(live.node.textContent, 'No telegram of it has been seen yet.');
});

test('live: while nothing is watched no frame is asked for; close() leaves the rows and draws no more', () => {
    const document = createDocument();
    const rows = filled();
    const frames = [];
    const live = createLive({ document, rows, frame: (callback) => frames.push(callback) });
    rows.add(telegram(9, '31/0/1'));
    assert.equal(frames.length, 0);
    assert.equal(rows.subscribers(), 1);
    live.watch({ addresses: ['31/0/1'] });
    rows.add(telegram(10, '31/0/1'));
    assert.equal(frames.length, 1);
    live.close();
    assert.equal(rows.subscribers(), 0);
    frames.shift()();
    assert.equal(lines(live.node).length, 3, 'as it was');
    assert.equal(document.listenerCount(), 0);
    // a bridge without a row model
    const none = createLive({ document });
    none.watch({ addresses: ['31/0/1'] });
    assert.equal(none.node.textContent, 'No telegram of it has been seen yet.');
});

test('live: the row model is only read: nothing in it changes, and the model has no call that sends', () => {
    const rows = filled();
    const before = JSON.stringify(rows.entries());
    const used = [];
    const watched = new Proxy(rows, {
        get(target, name) {
            used.push(name);
            return target[name];
        },
    });
    const live = createLive({ document: createDocument(), rows: watched });
    live.watch({ addresses: ['31/0/1'], source: '31.1.5' });
    rows.add(telegram(11, '31/0/1'));
    assert.deepEqual([...new Set(used)].sort(), ['entries', 'subscribe']);
    assert.equal(JSON.stringify(rows.entries().slice(0, 5)), before);
});
