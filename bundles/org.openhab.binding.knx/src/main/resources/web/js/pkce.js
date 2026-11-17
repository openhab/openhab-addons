import { sha256 } from './sha256.js';

const ALPHABET = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';
const VERIFIER = /^[A-Za-z0-9\-._~]{43,128}$/;
const VERIFIER_BYTES = 32;

function checkBytes(bytes) {
    if (!(bytes instanceof Uint8Array)) {
        throw new TypeError('bytes must be a Uint8Array');
    }
}

/**
 * @param {Uint8Array} bytes
 * @returns {string} Base64 with padding, as the X-KNX-Password header takes it
 */
export function base64(bytes) {
    checkBytes(bytes);
    let text = '';
    for (let i = 0; i < bytes.length; i += 3) {
        const left = bytes.length - i;
        const n = (bytes[i] << 16) | ((left > 1 ? bytes[i + 1] : 0) << 8) | (left > 2 ? bytes[i + 2] : 0);
        text += ALPHABET[(n >>> 18) & 63] + ALPHABET[(n >>> 12) & 63];
        text += left > 1 ? ALPHABET[(n >>> 6) & 63] : '=';
        text += left > 2 ? ALPHABET[n & 63] : '=';
    }
    return text;
}

/**
 * @param {Uint8Array} bytes
 * @returns {string} Base64 with the URL alphabet and without padding (RFC 4648, section 5)
 */
export function base64url(bytes) {
    let text = base64(bytes).replaceAll('+', '-').replaceAll('/', '_');
    while (text.endsWith('=')) {
        text = text.slice(0, -1);
    }
    return text;
}

/**
 * @param {(length: number) => Uint8Array} randomBytes gives that many random bytes, e.g. from
 *     crypto.getRandomValues
 * @returns {string} a code verifier of 43 characters
 */
export function newVerifier(randomBytes) {
    const bytes = randomBytes(VERIFIER_BYTES);
    if (!(bytes instanceof Uint8Array) || bytes.length !== VERIFIER_BYTES) {
        throw new TypeError(`randomBytes must give a Uint8Array of ${VERIFIER_BYTES} bytes`);
    }
    return base64url(bytes);
}

/**
 * The S256 code challenge of RFC 7636: base64url(SHA-256(verifier)).
 *
 * @param {string} verifier
 * @param {SubtleCrypto} [subtle] the browser's crypto.subtle; without it the page's own SHA-256 is used
 * @returns {Promise<string>}
 */
export async function challenge(verifier, subtle) {
    if (typeof verifier !== 'string' || !VERIFIER.test(verifier)) {
        throw new TypeError('not a code verifier of RFC 7636');
    }
    const bytes = Uint8Array.from(verifier, (c) => c.charCodeAt(0));
    const digest = subtle ? new Uint8Array(await subtle.digest('SHA-256', bytes)) : sha256(bytes);
    return base64url(digest);
}
