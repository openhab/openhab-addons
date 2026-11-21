import { test } from 'node:test';
import assert from 'node:assert/strict';

import { createApi } from '../web/js/api.js';
import { errorText } from '../web/js/texts.js';
import { show } from '../web/js/views/project.js';
import { allElements, textNodes } from './fakedom.js';
import { createFetch, empty, json, networkFailure, settle } from './fakes.js';
import { viewContext } from './fakeview.js';

const BASE = new URL('http://openhab.invalid/oh/rest/');
const BRIDGE = { uid: 'knx:ip:line7', label: 'Line 7', present: true, status: 'ONLINE', connection: 'SECURE_TUNNEL', bufferSize: 2000, projectState: 'LOADED' };
const REMOVED = { uid: 'knx:ip:gone', present: false, projectState: 'LOADED' };
const PASSWORD = 'pässword€';

const BRIDGE_ANSWER = { ...BRIDGE, ownAddress: '7.7.250', keyringLoaded: true };
const KEYRING = {
    loaded: true,
    fileName: 'line7.knxkeys',
    signatureValid: true,
    backboneKeyPresent: true,
    securedGroupAddresses: 3,
    interfaces: [
        { address: '7.7.250', type: 'Tunneling', host: '7.7.0', userId: 2, linkedSecuredGroupAddresses: 3, own: true, key: 'SECRET-IFKEY', password: 'SECRET-IFPW' },
        { address: '7.7.251', type: 'Tunneling', host: '7.7.0', linkedSecuredGroupAddresses: 0, own: false },
    ],
    backboneKey: 'SECRET-BACKBONE',
    password: 'SECRET-PASSWORD',
};
const PROJECT = {
    state: 'LOADED',
    name: 'Test <house>',
    installation: 'Main',
    importedAt: '2026-11-15T07:01:02.123456Z',
    sourceName: 'house.knxproj',
    language: 'en-US',
    groupAddresses: 12,
    securedGroupAddresses: 3,
    devices: 4,
    groupObjects: 30,
    ignoredInstallations: 0,
};
const UPLOADED = { ...PROJECT, name: 'Uploaded house', groupAddresses: 20 };
const KEYRING_UPLOAD = {
    stored: true,
    fileName: 'knx_ip_line7.knxkeys',
    signatureValid: true,
    shadowedByConfigFolder: true,
    keyring: { ...KEYRING, fileName: 'knx_ip_line7.knxkeys' },
    key: 'SECRET-ANSWERKEY',
};
const CHECK = {
    differences: [
        { kind: 'DATAPOINT_TYPE_DIFFERS', address: '31/7/1', configuredDataType: '1.001', projectDataType: '5.001', label: 'Kitchen <light>', noteOnly: false },
        { kind: 'ADDRESS_NOT_IN_PROJECT', address: '31/7/99', label: 'Old channel', noteOnly: false },
        { kind: 'DATAPOINT_TYPE_DIFFERS', address: '31/7/2', configuredDataType: '1.001', projectDataType: '1.002', label: 'Hall', noteOnly: true },
    ],
    thingsWithoutHandler: 0,
};

const ROOT = (uid) => `/oh/rest/knx/bridges/${encodeURIComponent(uid)}`;

function server(uid, answers = {}) {
    const defaults = {
        'GET ': () => json(200, BRIDGE_ANSWER),
        'GET /keyring': () => json(200, KEYRING),
        'GET /project': () => json(200, PROJECT),
        'GET /project/check': () => json(200, CHECK),
        'PUT /project': () => json(200, UPLOADED),
        'DELETE /project': () => empty(204),
        'PUT /keyring': () => json(200, KEYRING_UPLOAD),
    };
    return createFetch((call) => {
        const key = `${call.method} ${call.url.pathname.slice(ROOT(uid).length)}`;
        const answer = Object.hasOwn(answers, key) ? answers[key] : defaults[key];
        return answer ? answer(call) : json(404, { code: 'unknown-bridge', text: 'x' });
    });
}

function fakeAuth() {
    return { token: async () => 'tok', renew: async () => 'tok', lost() {} };
}

async function open({ bridge = BRIDGE, answers, clipboard } = {}) {
    const fetch = server(bridge.uid, answers);
    const api = createApi({ fetch, base: BASE, auth: fakeAuth() });
    const rows = { forgotten: 0, forgetProject: () => rows.forgotten++ };
    const harness = viewContext({ bridge, api, rows, route: { view: 'project', bridge: bridge.uid, query: {} } });
    harness.context.clipboard = clipboard;
    const view = show(harness.context);
    await settle();
    return Object.assign(harness, { view, api, fetch, rows });
}

function field(element, name) {
    return element.querySelectorAll('input').find((input) => input.getAttribute('name') === name) ?? null;
}

function part(element, name) {
    return element.querySelector(`.${name}`);
}

function calls(fetch) {
    return fetch.calls.map((call) => `${call.method} ${call.url.pathname.replace(/^.*\/bridges\/[^/]+/, '')}`);
}

function everything(element) {
    const words = [...textNodes(element)];
    for (const node of allElements(element)) {
        for (const value of node.attributes.values()) {
            words.push(value);
        }
    }
    return words.join('\n');
}

function base64Utf8(words) {
    return Buffer.from(words, 'utf8').toString('base64');
}

test('project: a bridge shows its four parts from calls 2, 7, 8 and 16', async () => {
    const s = await open();
    assert.deepEqual(calls(s.fetch).sort(), ['GET ', 'GET /keyring', 'GET /project', 'GET /project/check']);
    assert.deepEqual(
        s.element.querySelectorAll('h3').map((node) => node.textContent),
        ['Bridge', 'Keyring', 'ETS project', 'Check'],
    );
    assert.deepEqual(textNodes(part(s.element, 'bridge').querySelector('dl')), [
        'Thing UID',
        'knx:ip:line7',
        'Label',
        'Line 7',
        'Status',
        'ONLINE',
        'Connection',
        'secure tunnel',
        'Own address',
        '7.7.250',
        'Buffer size',
        '2000',
    ]);
    const project = textNodes(part(s.element, 'project').querySelector('dl'));
    assert.deepEqual(project.slice(0, 6), ['Name', 'Test <house>', 'Installation', 'Main', 'File', 'house.knxproj']);
    assert.ok(project.includes('12 (3 secured)'));
    assert.ok(!project.includes('Other installations left out'));
});

test('project: the keyring part shows what the keyring says and never a key or password field of an answer', async () => {
    const s = await open();
    const keyring = part(s.element, 'keyring');
    assert.deepEqual(textNodes(keyring.querySelector('dl')), [
        'File',
        'line7.knxkeys',
        'Signature',
        'valid',
        'Backbone key present',
        'yes',
        'Secured group addresses',
        '3',
    ]);
    const rows = keyring.querySelector('tbody').children;
    assert.deepEqual(textNodes(rows[0]), ['7.7.250', 'Tunneling', '7.7.0', '2', '3', 'own']);
    assert.ok(rows[0].classList.contains('own'));
    assert.deepEqual(textNodes(rows[1]), ['7.7.251', 'Tunneling', '7.7.0', '', '0', '']);
    assert.ok(!/SECRET/.test(everything(s.element)));
});

test('project: no keyring, a monitor switched off and a project of each state say so', async () => {
    const s = await open({
        answers: {
            'GET ': () => json(200, { ...BRIDGE_ANSWER, bufferSize: 0 }),
            'GET /keyring': () => json(200, { loaded: false }),
            'GET /project': () => json(200, { state: 'NONE' }),
            'GET /project/check': () => json(404, { code: 'no-project', text: 'x' }),
        },
    });
    assert.ok(textNodes(part(s.element, 'bridge')).includes('0 (the monitor is switched off)'));
    assert.ok(textNodes(part(s.element, 'keyring')).includes('No keyring is loaded for this bridge.'));
    assert.ok(textNodes(part(s.element, 'project')).includes('No ETS project is loaded for this bridge.'));
    assert.equal(part(s.element, 'project').querySelector('.remove'), null);
    assert.deepEqual(textNodes(part(s.element, 'check')), ['Check', 'The check needs a loaded ETS project.']);
    for (const [state, words] of [
        ['UNREADABLE', 'The stored ETS project cannot be read.'],
        ['NEWER_VERSION', 'The stored ETS project was written by a newer version of the add-on.'],
    ]) {
        const t = await open({ answers: { 'GET /project': () => json(200, { state }) } });
        assert.ok(textNodes(part(t.element, 'project')).includes(words), state);
        assert.ok(part(t.element, 'project').querySelector('.remove'), state);
    }
});

test('project: the project upload sends the file as body and the password as Base64 of UTF-8', async () => {
    const s = await open();
    const file = new File([Uint8Array.of(1, 2, 3)], 'house.knxproj');
    field(s.element, 'projectFile').files = [file];
    field(s.element, 'projectPassword').value = PASSWORD;
    field(s.element, 'language').value = 'de-CH';
    s.element.querySelector('.project').querySelector('.upload').click();
    await settle();
    const put = s.fetch.calls.find((call) => call.method === 'PUT');
    assert.equal(put.url.pathname, `${ROOT(BRIDGE.uid)}/project`);
    assert.equal(put.body, file);
    assert.equal(put.headers['X-KNX-Password'], base64Utf8(PASSWORD));
    assert.equal(put.headers['Content-Type'], 'application/octet-stream');
    assert.deepEqual(Object.fromEntries(put.url.searchParams), { name: 'house.knxproj', language: 'de-CH' });
    assert.equal(field(s.element, 'projectPassword').value, '');
    assert.ok(textNodes(part(s.element, 'project')).includes('The project is loaded.'));
    assert.ok(textNodes(part(s.element, 'project')).includes('Uploaded house'));
    assert.equal(s.rows.forgotten, 1);
    assert.ok(!everything(s.element).includes(PASSWORD));
    assert.ok(!everything(s.element).includes(base64Utf8(PASSWORD)));
    assert.equal(calls(s.fetch).filter((call) => call === 'GET /project/check').length, 2);
});

test('project: no password header when the password field is empty, for both uploads', async () => {
    const s = await open();
    field(s.element, 'projectFile').files = [new File([Uint8Array.of(1)], 'house.knxproj')];
    s.element.querySelector('.project').querySelector('.upload').click();
    await settle();
    field(s.element, 'keyringFile').files = [new File([Uint8Array.of(1)], 'line.knxkeys')];
    s.element.querySelector('.keyring').querySelector('.upload').click();
    await settle();
    const puts = s.fetch.calls.filter((call) => call.method === 'PUT');
    assert.equal(puts.length, 2);
    assert.ok(puts.every((call) => !('X-KNX-Password' in call.headers)));
    assert.ok(!('language' in Object.fromEntries(puts[0].url.searchParams)));
});

const UPLOAD_ERRORS = [
    [400, 'bad-input'],
    [400, 'not-a-project'],
    [400, 'malformed'],
    [400, 'password-required'],
    [400, 'wrong-password'],
    [400, 'unsupported-protection'],
    [413, 'limit-exceeded'],
    [413, 'too-large'],
    [409, 'busy'],
    [404, 'unknown-bridge'],
    [500, 'store-failed'],
    [500, 'read-failed'],
    [500, 'internal'],
];

test('project: each upload error code has its own text, and the password field is empty after each failure', async () => {
    const seen = new Map();
    for (const [status, code] of UPLOAD_ERRORS) {
        const s = await open({ answers: { 'PUT /project': () => json(status, { code, text: `server text ${code}` }) } });
        const password = field(s.element, 'projectPassword');
        field(s.element, 'projectFile').files = [new File([Uint8Array.of(1)], 'house.knxproj')];
        password.value = PASSWORD;
        s.element.querySelector('.project').querySelector('.upload').click();
        await settle();
        const words = part(s.element, 'project').querySelector('.result').textContent;
        assert.equal(words, errorText(code), code);
        assert.ok(!words.includes('server text'), code);
        assert.equal(password.value, '', code);
        assert.equal(s.document.activeElement === password, code === 'password-required', code);
        assert.equal(s.rows.forgotten, 0, code);
        assert.ok(!everything(s.element).includes(PASSWORD), code);
        seen.set(words, code);
    }
    assert.equal(seen.size, UPLOAD_ERRORS.length);
});

test('project: a network failure and a language the server would refuse also empty the password field', async () => {
    const s = await open({
        answers: {
            'PUT /project': () => {
                throw networkFailure();
            },
        },
    });
    const password = field(s.element, 'projectPassword');
    field(s.element, 'projectFile').files = [new File([Uint8Array.of(1)], 'house.knxproj')];
    password.value = PASSWORD;
    s.element.querySelector('.project').querySelector('.upload').click();
    await settle();
    assert.equal(part(s.element, 'project').querySelector('.result').textContent, 'openHAB cannot be reached.');
    assert.equal(password.value, '');

    password.value = PASSWORD;
    field(s.element, 'language').value = 'no language';
    s.element.querySelector('.project').querySelector('.upload').click();
    await settle();
    assert.equal(part(s.element, 'project').querySelector('.result').textContent, 'The language must look like "en" or "de-CH".');
    assert.equal(password.value, '');
    assert.equal(s.fetch.calls.filter((call) => call.method === 'PUT').length, 1);
});

test('project: without a chosen file nothing is sent and the password field is emptied', async () => {
    const s = await open();
    for (const [name, passwordName] of [
        ['project', 'projectPassword'],
        ['keyring', 'keyringPassword'],
    ]) {
        field(s.element, passwordName).value = PASSWORD;
        s.element.querySelector(`.${name}`).querySelector('.upload').click();
        await settle();
        assert.equal(part(s.element, name).querySelector('.result').textContent, 'Choose a file first.', name);
        assert.equal(field(s.element, passwordName).value, '', name);
    }
    assert.equal(s.fetch.calls.filter((call) => call.method === 'PUT').length, 0);
});

test('project: while the project upload runs the button is disabled and the page says so', async () => {
    let release = null;
    const s = await open({ answers: { 'PUT /project': () => new Promise((resolve) => (release = () => resolve(json(200, UPLOADED)))) } });
    const button = s.element.querySelector('.project').querySelector('.upload');
    field(s.element, 'projectFile').files = [new File([Uint8Array.of(1)], 'house.knxproj')];
    field(s.element, 'projectPassword').value = PASSWORD;
    button.click();
    await settle();
    assert.ok(button.disabled);
    assert.equal(part(s.element, 'project').querySelector('.result').textContent, 'Reading the project. This can take a few seconds.');
    assert.equal(button.click(), null);
    release();
    await settle();
    assert.ok(!button.disabled);
    assert.equal(field(s.element, 'projectPassword').value, '');
    assert.equal(s.fetch.calls.filter((call) => call.method === 'PUT').length, 1);
});

test('project: the keyring upload shows the stored name, the signature, the warning and the keyringFile line with Copy', async () => {
    const written = [];
    const s = await open({ clipboard: { writeText: async (words) => written.push(words) } });
    const file = new File([Uint8Array.of(9)], 'line.knxkeys');
    field(s.element, 'keyringFile').files = [file];
    field(s.element, 'keyringPassword').value = PASSWORD;
    s.element.querySelector('.keyring').querySelector('.upload').click();
    await settle();
    const put = s.fetch.calls.find((call) => call.method === 'PUT');
    assert.equal(put.url.pathname, `${ROOT(BRIDGE.uid)}/keyring`);
    assert.equal(put.body, file);
    assert.equal(put.headers['X-KNX-Password'], base64Utf8(PASSWORD));
    assert.equal(field(s.element, 'keyringPassword').value, '');
    const result = part(s.element, 'keyring').querySelector('.result');
    assert.deepEqual(result.querySelectorAll('p').map((node) => node.textContent), [
        'Stored as knx_ip_line7.knxkeys.',
        'The password matches the signature of the keyring.',
        'A file of this name in the configuration folder (conf/misc) is used instead of the uploaded one.',
        'The file holds 3 secured group addresses and 2 interfaces.',
        "Set the bridge's keyringFile to knx_ip_line7.knxkeys",
    ]);
    assert.ok(result.querySelector('.warn'));
    assert.equal(field(s.element, 'keyringFileName').value, 'knx_ip_line7.knxkeys');
    result.querySelector('.copy').click();
    await settle();
    assert.deepEqual(written, ['knx_ip_line7.knxkeys']);
    assert.equal(result.querySelector('.copied').textContent, 'Copied');
    assert.ok(!/SECRET/.test(everything(s.element)));
    assert.ok(!everything(s.element).includes(PASSWORD));
    assert.equal(calls(s.fetch).filter((call) => call === 'GET /keyring').length, 2);
    assert.ok(!calls(s.fetch).some((call) => call.startsWith('PUT ') && !call.endsWith('/keyring')));
});

test('project: a keyring upload without password shows no signature line; a refused one shows its text', async () => {
    const { signatureValid, ...withoutSignature } = KEYRING_UPLOAD;
    assert.equal(signatureValid, true);
    const s = await open({ answers: { 'PUT /keyring': () => json(200, { ...withoutSignature, shadowedByConfigFolder: false }) } });
    field(s.element, 'keyringFile').files = [new File([Uint8Array.of(9)], 'line.knxkeys')];
    s.element.querySelector('.keyring').querySelector('.upload').click();
    await settle();
    const result = part(s.element, 'keyring').querySelector('.result');
    assert.deepEqual(result.querySelectorAll('p').map((node) => node.textContent), [
        'Stored as knx_ip_line7.knxkeys.',
        'The file holds 3 secured group addresses and 2 interfaces.',
        "Set the bridge's keyringFile to knx_ip_line7.knxkeys",
    ]);
    const t = await open({ answers: { 'PUT /keyring': () => json(400, { code: 'not-a-keyring', text: 'x' }) } });
    field(t.element, 'keyringFile').files = [new File([Uint8Array.of(9)], 'line.knxkeys')];
    field(t.element, 'keyringPassword').value = PASSWORD;
    t.element.querySelector('.keyring').querySelector('.upload').click();
    await settle();
    assert.equal(part(t.element, 'keyring').querySelector('.result').textContent, 'The file is not a keyring that can be read.');
    assert.equal(field(t.element, 'keyringPassword').value, '');
});

test('project: Remove asks first and calls 18 only on yes, then reads the project again', async () => {
    const s = await open();
    s.answer = false;
    part(s.element, 'project').querySelector('.remove').click();
    await settle();
    assert.deepEqual(s.questions, ['Remove the stored ETS project of this bridge? Channels and Things stay as they are.']);
    assert.ok(!calls(s.fetch).includes('DELETE /project'));
    s.answer = true;
    part(s.element, 'project').querySelector('.remove').click();
    await settle();
    assert.equal(calls(s.fetch).filter((call) => call === 'DELETE /project').length, 1);
    assert.equal(calls(s.fetch).filter((call) => call === 'GET /project').length, 2);
    assert.equal(s.rows.forgotten, 1);
    assert.ok(textNodes(part(s.element, 'project')).includes('The project is removed.'));
});

test('project: a removed bridge offers the project part only: state and Remove, no upload', async () => {
    const s = await open({ bridge: REMOVED });
    assert.deepEqual(calls(s.fetch), ['GET /project']);
    assert.deepEqual(
        s.element.querySelectorAll('h3').map((node) => node.textContent),
        ['ETS project'],
    );
    assert.ok(textNodes(s.element).some((words) => words.startsWith('This bridge is no longer in openHAB.')));
    assert.equal(s.element.querySelectorAll('input').length, 0);
    s.element.querySelector('.remove').click();
    await settle();
    assert.deepEqual(calls(s.fetch), ['GET /project', 'DELETE /project', 'GET /project']);
});

test('project: the check shows the differences as a table, or that there are none', async () => {
    const s = await open();
    const rows = part(s.element, 'check').querySelector('tbody').children;
    assert.deepEqual(textNodes(rows[0]), ['31/7/1', 'the data types differ', 'Kitchen <light>', '1.001', '5.001']);
    assert.deepEqual(textNodes(rows[1]), ['31/7/99', 'not in the project', 'Old channel', '', '']);
    assert.deepEqual(textNodes(rows[2]), ['31/7/2', 'the data types differ', 'note only', 'Hall', '1.001', '1.002']);
    const t = await open({ answers: { 'GET /project/check': () => json(200, { differences: [], thingsWithoutHandler: 2 }) } });
    assert.deepEqual(textNodes(part(t.element, 'check')), [
        'Check',
        'No differences between the channels and the project.',
        '2 KNX Things of this bridge have no handler and were not checked.',
    ]);
});

test('project: close() leaves no listener and empties the password fields', async () => {
    const s = await open();
    assert.ok(s.document.listenerCount() > 0);
    field(s.element, 'projectPassword').value = PASSWORD;
    field(s.element, 'keyringPassword').value = PASSWORD;
    s.view.close();
    assert.equal(s.document.listenerCount(), 0);
    assert.equal(field(s.element, 'projectPassword').value, '');
    assert.equal(field(s.element, 'keyringPassword').value, '');
    assert.equal(s.clock.count(), 0);
});

test('project: the password fields are password fields without autocomplete', async () => {
    const s = await open();
    for (const name of ['projectPassword', 'keyringPassword']) {
        assert.equal(field(s.element, name).getAttribute('type'), 'password');
        assert.equal(field(s.element, name).getAttribute('autocomplete'), 'off');
    }
    assert.equal(field(s.element, 'language').getAttribute('maxlength'), '10');
});
