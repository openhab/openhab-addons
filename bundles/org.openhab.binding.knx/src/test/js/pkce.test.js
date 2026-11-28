import { test } from 'node:test';
import assert from 'node:assert/strict';
import { webcrypto } from 'node:crypto';

import { base64, base64url, challenge, newVerifier } from '../web/js/pkce.js';

// RFC 7636, appendix B
const RFC_OCTETS = [
    116, 24, 223, 180, 151, 153, 224, 37, 79, 250, 96, 125, 216, 173, 187, 186, 22, 212, 37, 77, 105, 214, 191, 240,
    91, 88, 5, 88, 83, 132, 141, 121,
];
const RFC_VERIFIER = 'dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk';
const RFC_CHALLENGE = 'E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM';

const inputs = () => {
    let seed = 0x2b7e1516;
    const next = () => {
        seed = (Math.imul(seed, 1664525) + 1013904223) >>> 0;
        return seed >>> 24;
    };
    const all = [Uint8Array.of(0xfb, 0xff, 0xbf), Uint8Array.of(0xff, 0xff), Uint8Array.of(0xfe)];
    for (let length = 0; length <= 100; length++) {
        all.push(Uint8Array.from({ length }, next));
    }
    return all;
};

test('pkce: the RFC 7636 appendix B verifier from its octets', () => {
    const asked = [];
    const verifier = newVerifier((length) => {
        asked.push(length);
        return Uint8Array.from(RFC_OCTETS);
    });
    assert.equal(verifier, RFC_VERIFIER);
    assert.deepEqual(asked, [32]);
});

test('pkce: the RFC 7636 appendix B challenge with the own SHA-256', async () => {
    assert.equal(await challenge(RFC_VERIFIER), RFC_CHALLENGE);
    assert.equal(await challenge(RFC_VERIFIER, undefined), RFC_CHALLENGE);
    assert.equal(await challenge(RFC_VERIFIER, null), RFC_CHALLENGE);
});

test('pkce: the RFC 7636 appendix B challenge with crypto.subtle', async () => {
    const used = [];
    const subtle = {
        digest: (algorithm, data) => {
            used.push(algorithm);
            return webcrypto.subtle.digest(algorithm, data);
        },
    };
    assert.equal(await challenge(RFC_VERIFIER, subtle), RFC_CHALLENGE);
    assert.deepEqual(used, ['SHA-256']);
});

test('pkce: base64url has no +, / or = and equals node for many inputs', () => {
    for (const bytes of inputs()) {
        const text = base64url(bytes);
        assert.doesNotMatch(text, /[+/=]/);
        assert.equal(text, Buffer.from(bytes).toString('base64url'), `length ${bytes.length}`);
    }
    assert.equal(base64url(Uint8Array.of(0xfb, 0xff, 0xbf)), '-_-_');
});

test('pkce: base64 equals node for many inputs', () => {
    for (const bytes of inputs()) {
        assert.equal(base64(bytes), Buffer.from(bytes).toString('base64'), `length ${bytes.length}`);
    }
    assert.equal(base64(Uint8Array.of(0xfb, 0xff, 0xbf)), '+/+/');
    assert.equal(base64(new Uint8Array(0)), '');
});

test('pkce: base64 of the UTF-8 bytes of a text with é and €', () => {
    const bytes = new TextEncoder().encode('café €');
    assert.equal(bytes.length, 9);
    assert.equal(base64(bytes), 'Y2Fmw6kg4oKs');
    const password = new TextEncoder().encode('pässword€');
    assert.equal(base64(password), Buffer.from('pässword€', 'utf8').toString('base64'));
});

test('pkce: a verifier has 43 characters of the unreserved set', () => {
    let calls = 0;
    const verifier = newVerifier((length) => {
        calls++;
        return Uint8Array.from({ length }, (_, i) => (i * 37 + 251) & 0xff);
    });
    assert.equal(calls, 1);
    assert.equal(verifier.length, 43);
    assert.match(verifier, /^[A-Za-z0-9\-._~]{43}$/);
});

test('pkce: newVerifier refuses random bytes of the wrong kind or length', () => {
    assert.throws(() => newVerifier(() => new Uint8Array(31)), TypeError);
    assert.throws(() => newVerifier(() => new Array(32).fill(1)), TypeError);
    assert.throws(() => newVerifier(() => undefined), TypeError);
});

test('pkce: challenge refuses a verifier outside RFC 7636', async () => {
    await assert.rejects(challenge('short'), TypeError);
    await assert.rejects(challenge('a'.repeat(129)), TypeError);
    await assert.rejects(challenge('a'.repeat(42) + 'é'), TypeError);
    await assert.rejects(challenge('a'.repeat(42) + '+'), TypeError);
    await assert.rejects(challenge(undefined), TypeError);
    assert.match(await challenge('a'.repeat(128)), /^[A-Za-z0-9_-]{43}$/);
});

test('pkce: base64 and base64url refuse anything but a Uint8Array', () => {
    assert.throws(() => base64('abc'), TypeError);
    assert.throws(() => base64url([1, 2]), TypeError);
});
