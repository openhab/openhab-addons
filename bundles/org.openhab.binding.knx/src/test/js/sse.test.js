import { test } from 'node:test';
import assert from 'node:assert/strict';

import { MAX_EVENT_CHARS, createSseParser } from '../web/js/sse.js';

const encoder = new TextEncoder();

function parse(chunks) {
    const events = [];
    const parser = createSseParser((event) => events.push(event));
    for (const chunk of chunks) {
        parser.push(typeof chunk === 'string' ? encoder.encode(chunk) : chunk);
    }
    return { events, parser };
}

test('sse: one event', () => {
    const { events } = parse(['event: telegram\ndata: {"number":1}\n\n']);
    assert.deepEqual(events, [{ event: 'telegram', data: '{"number":1}' }]);
});

test('sse: two events in one chunk, and an event without a name is "message"', () => {
    const { events } = parse(['event: open\ndata: a\n\ndata: b\n\n']);
    assert.deepEqual(events, [
        { event: 'open', data: 'a' },
        { event: 'message', data: 'b' },
    ]);
});

test('sse: an event cut into two chunks at every byte gives the same event, with LF, CRLF, CR and a 3-byte character', () => {
    for (const ending of ['\n', '\r\n', '\r']) {
        const whole = encoder.encode(`event: telegram${ending}data: {"name":"Küche €"}${ending}${ending}`);
        assert.ok(whole.length > 20);
        for (let at = 0; at <= whole.length; at++) {
            const { events } = parse([whole.slice(0, at), whole.slice(at)]);
            assert.deepEqual(events, [{ event: 'telegram', data: '{"name":"Küche €"}' }], `${JSON.stringify(ending)} at ${at}`);
        }
    }
});

test('sse: every byte in a chunk of its own gives the same events', () => {
    const whole = encoder.encode('event: a\r\ndata: €1\r\n\r\nevent: b\rdata: 2\r\r');
    const { events } = parse([...whole].map((byte) => Uint8Array.of(byte)));
    assert.deepEqual(events, [
        { event: 'a', data: '€1' },
        { event: 'b', data: '2' },
    ]);
});

test('sse: a CR at the end of a chunk ends its line at once, and the LF of the next chunk is not a second line', () => {
    const events = [];
    const parser = createSseParser((event) => events.push(event));
    parser.push(encoder.encode('event: end\r\ndata: x\r\n\r'));
    assert.deepEqual(events, [{ event: 'end', data: 'x' }]);
    parser.push(encoder.encode('\ndata: y\n\n'));
    assert.deepEqual(events, [
        { event: 'end', data: 'x' },
        { event: 'message', data: 'y' },
    ]);
});

test('sse: comment lines are left out', () => {
    const { events } = parse([': hello\nevent: alive\n:another\ndata: 1\n\n:\n\n']);
    assert.deepEqual(events, [{ event: 'alive', data: '1' }]);
});

test('sse: the data lines of one event are joined with LF, and only one leading space is dropped', () => {
    const { events } = parse(['data: one\ndata:two\ndata:  three\ndata\n\n']);
    assert.deepEqual(events, [{ event: 'message', data: 'one\ntwo\n three\n' }]);
});

test('sse: an event without data is not sent, and its name does not pass on to the next one', () => {
    const { events } = parse(['event: lost\n\ndata: x\n\nevent: open\nid: 3\nretry: 10\n\n']);
    assert.deepEqual(events, [{ event: 'message', data: 'x' }]);
});

test('sse: an empty data line is an event with empty data', () => {
    const { events } = parse(['event: alive\ndata:\n\n']);
    assert.deepEqual(events, [{ event: 'alive', data: '' }]);
});

test('sse: a field name is taken as a whole, so "data2" or "Data" is not data', () => {
    const { events } = parse(['data2: x\nData: y\nevent: e\ndata: z\n\n']);
    assert.deepEqual(events, [{ event: 'e', data: 'z' }]);
});

test('sse: end() drops an unfinished event, and the parser starts clean after it', () => {
    const { events, parser } = parse(['event: telegram\ndata: {"number":1}\n']);
    parser.end();
    assert.deepEqual(events, []);
    parser.push(encoder.encode('data: 2\n\n'));
    assert.deepEqual(events, [{ event: 'message', data: '2' }]);
});

test('sse: end() drops a cut character and a cut line too', () => {
    const euro = encoder.encode('data: €');
    const { events, parser } = parse([euro.slice(0, euro.length - 1)]);
    parser.end();
    parser.push(encoder.encode('data: ok\n\n'));
    assert.deepEqual(events, [{ event: 'message', data: 'ok' }]);
});

test('sse: a byte-order mark at the start is not part of the first field', () => {
    const { events } = parse([Uint8Array.of(0xef, 0xbb, 0xbf), 'event: open\ndata: 1\n\n']);
    assert.deepEqual(events, [{ event: 'open', data: '1' }]);
});

test('sse: an event larger than the bound is dropped whole, and the next one comes through', () => {
    const big = 'x'.repeat(MAX_EVENT_CHARS);
    const { events } = parse(['event: telegram\ndata: ', big, big, '\ndata: tail\n\n', 'event: alive\ndata: 1\n\n']);
    assert.deepEqual(events, [{ event: 'alive', data: '1' }]);
});

test('sse: a line that never ends does not grow the parser beyond the bound', () => {
    const events = [];
    const parser = createSseParser((event) => events.push(event));
    const chunk = encoder.encode('y'.repeat(64 * 1024));
    for (let i = 0; i < 64; i++) {
        parser.push(chunk);
    }
    assert.ok(parser.held() <= MAX_EVENT_CHARS, String(parser.held()));
    parser.push(encoder.encode('\n\ndata: after\n\n'));
    assert.deepEqual(events, [{ event: 'message', data: 'after' }]);
});
