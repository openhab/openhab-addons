import { test } from 'node:test';
import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';

import { sha256 } from '../web/js/sha256.js';

const hex = (bytes) => Array.from(bytes, (b) => b.toString(16).padStart(2, '0')).join('');
const ascii = (text) => Uint8Array.from(text, (c) => c.charCodeAt(0));

test('sha256 of the empty input', () => {
    assert.equal(hex(sha256(new Uint8Array(0))), 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855');
});

test('sha256 of abc', () => {
    assert.equal(hex(sha256(ascii('abc'))), 'ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad');
});

test('sha256 of the 56-byte NIST message', () => {
    const message = 'abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq';
    assert.equal(message.length, 56);
    assert.equal(hex(sha256(ascii(message))), '248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1');
});

test('sha256 of one million a', () => {
    const million = new Uint8Array(1000000).fill(0x61);
    assert.equal(hex(sha256(million)), 'cdc76e5c9914fb9281a1c7e284d73e67f1809a48a497200e046d39ccc7112cd0');
});

test('sha256 equals node:crypto for 200 inputs of lengths 0 to 199', () => {
    let seed = 0x7a5c3e11;
    const next = () => {
        seed = (Math.imul(seed, 1103515245) + 12345) >>> 0;
        return seed >>> 24;
    };
    for (let length = 0; length < 200; length++) {
        const input = Uint8Array.from({ length }, next);
        const expected = createHash('sha256').update(input).digest('hex');
        assert.equal(hex(sha256(input)), expected, `length ${length}`);
    }
});

test('sha256 gives 32 bytes as a Uint8Array and leaves its input alone', () => {
    const input = ascii('abc');
    const digest = sha256(input);
    assert.ok(digest instanceof Uint8Array);
    assert.equal(digest.length, 32);
    assert.deepEqual(Array.from(input), [0x61, 0x62, 0x63]);
});

test('sha256 refuses anything but a Uint8Array', () => {
    assert.throws(() => sha256('abc'), TypeError);
    assert.throws(() => sha256([1, 2, 3]), TypeError);
    assert.throws(() => sha256(undefined), TypeError);
});
