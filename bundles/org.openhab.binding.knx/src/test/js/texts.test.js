import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readdirSync, readFileSync, statSync } from 'node:fs';

import { errorText, hasText, text } from '../web/js/texts.js';
import { FLAGS } from '../web/js/views/objects.js';
import { PLAN_CODES } from '../web/js/plan.js';

const WEB = new URL('../web/', import.meta.url);

// The error codes of spec F (KnxRestError) and the stream's own end codes.
const SERVER_CODES = [
    'bad-input',
    'not-a-project',
    'malformed',
    'password-required',
    'wrong-password',
    'unsupported-protection',
    'limit-exceeded',
    'not-a-keyring',
    'unknown-bridge',
    'no-project',
    'unknown-device',
    'unknown-group-address',
    'project-unusable',
    'busy',
    'monitor-off',
    'too-large',
    'too-many-streams',
    'store-failed',
    'read-failed',
    'internal',
];
const END_CODES = ['slow-client', 'bridge-gone', 'shutdown'];
// The page's own codes: api.js and auth.js make them.
const PAGE_CODES = ['network', 'signed-out', 'bad-answer', 'bad-language'];
// The problem codes of call 22 (spec H, "Names later stages rely on").
const PROBLEM_CODES = [
    'bad-id',
    'bad-item-name',
    'bad-label',
    'bad-tag',
    'bad-category',
    'bad-key',
    'duplicate-id',
    'duplicate-item',
    'thing-exists',
    'item-exists',
    'unknown-group',
    'group-cycle',
    'bad-target',
    'unknown-address',
    'bad-address',
    'type-not-allowed',
    'no-data-type',
    'nothing-selected',
    'already-configured',
    'status-only',
    'secured-without-keyring',
    'read-without-flag',
];
const PAIRINGS = ['ROLE', 'OBJECTS', 'FUNCTION', 'NAMES', 'NONE'];
// The reasons checkPlan of js/plan.js gives.
const PLAN_REASONS = ['empty', 'bad-step', 'bad-kind', 'bad-order', 'bad-id', 'bad-body', 'thing-setting', 'unknown-group', 'unknown-link', 'duplicate', 'bad-existing'];

function jsFiles(folder = WEB, prefix = '') {
    const files = [];
    for (const name of readdirSync(folder)) {
        const url = new URL(name, folder);
        if (statSync(url).isDirectory()) {
            files.push(...jsFiles(new URL(`${name}/`, folder), `${prefix}${name}/`));
        } else if (name.endsWith('.js')) {
            files.push(prefix + name);
        }
    }
    return files;
}

test('texts: values fill the numbered places, as text', () => {
    assert.equal(text('login.user', '<b>Ann</b>'), 'Signed in as <b>Ann</b>');
    assert.equal(text('error.http', 404), 'openHAB answered with status 404.');
});

test('texts: an unknown key gives the key, also one of the object prototype', () => {
    assert.equal(text('no.such.key'), 'no.such.key');
    assert.equal(text('constructor'), 'constructor');
    assert.equal(text('__proto__'), '__proto__');
    assert.equal(hasText('constructor'), false);
    assert.equal(hasText('login.signIn'), true);
});

test('texts: every error code of the server, every end code and every code of the page has a text', () => {
    for (const code of [...SERVER_CODES, ...END_CODES, ...PAGE_CODES]) {
        assert.ok(hasText(`error.${code}`), code);
        assert.notEqual(errorText(code, 'server text'), 'server text', code);
    }
});

test('texts: an unknown code shows the server text, an http code its status, and nothing at all a fallback', () => {
    assert.equal(errorText('some-new-code', 'The server says <b>so</b>.'), 'The server says <b>so</b>.');
    assert.equal(errorText('http-502', ''), 'openHAB answered with status 502.');
    assert.equal(errorText('http-502', 'Bad Gateway'), 'openHAB answered with status 502.');
    assert.equal(errorText('some-new-code', ''), 'Something went wrong (some-new-code).');
    assert.equal(errorText('http-403', ''), 'This needs an administrator of openHAB.');
    assert.equal(errorText('constructor', undefined), 'Something went wrong (constructor).');
    assert.equal(errorText(undefined, undefined), 'Something went wrong (unknown).');
});

test('texts: every key the page passes to text() exists', () => {
    const missing = [];
    let used = 0;
    for (const file of jsFiles()) {
        const source = readFileSync(new URL(file, WEB), 'utf8');
        for (const match of source.matchAll(/\btext\(\s*'([^']+)'/g)) {
            used++;
            if (!hasText(match[1])) {
                missing.push(`${file}: ${match[1]}`);
            }
        }
    }
    assert.deepEqual(missing, []);
    assert.ok(used > 20, `${used} keys used`);
});

test('texts: the keys the views build from server names and from their own lists exist', () => {
    const keys = [
        ...FLAGS.flatMap(([letter, , key]) => [letter, key]),
        ...['ADDRESS_NOT_IN_PROJECT', 'ADDRESS_WITHOUT_OBJECT', 'DATAPOINT_TYPE_DIFFERS', 'READ_WITHOUT_READ_FLAG'].map((kind) => `check.${kind}`),
        ...['TUNNEL', 'SECURE_TUNNEL', 'ROUTER', 'SECURE_ROUTER', 'SERIAL'].map((kind) => `connection.${kind}`),
        ...['group-addresses', 'topology', 'buildings', 'monitor', 'project', 'wizard'].map((view) => `view.${view}`),
        ...['IN', 'OUT'].map((direction) => `service.${direction}`),
        ...['WRITE', 'READ', 'RESPONSE', 'UNKNOWN'].map((type) => `type.${type}`),
        ...['address', 'type', 'host', 'user', 'secured', 'own'].map((name) => `setup.interface.${name}`),
        ...['address', 'kind', 'label', 'configured', 'project'].map((name) => `setup.difference.${name}`),
    ];
    assert.deepEqual(
        keys.filter((key) => !hasText(key)),
        [],
    );
    assert.deepEqual(
        FLAGS.map(([letter]) => text(letter)),
        ['C', 'R', 'W', 'T', 'U', 'I'],
    );
    assert.equal(text('app.none'), '–');
});

test('texts: every problem code of call 22, every pairing value and every code and reason of the step runner has a text', () => {
    const keys = [
        ...PROBLEM_CODES.map((code) => `problem.${code}`),
        ...PAIRINGS.map((pairing) => `pairing.${pairing}`),
        ...PLAN_CODES.map((code) => `plan.${code}`),
        ...PLAN_REASONS.map((reason) => `planReason.${reason}`),
        ...['GROUP', 'THING', 'ITEM', 'LINK'].map((kind) => `kind.${kind}`),
        ...['waiting', 'checked', 'exists', 'created', 'failed', 'removed'].map((state) => `stepState.${state}`),
        ...['ERROR', 'WARNING'].map((severity) => `severity.${severity}`),
        ...['FUNCTION', 'DEVICE', 'ADDRESSES'].map((source) => `source.${source}`),
    ];
    assert.deepEqual(
        keys.filter((key) => !hasText(key)),
        [],
    );
    assert.equal(new Set(PROBLEM_CODES.map((code) => text(`problem.${code}`))).size, PROBLEM_CODES.length);
    assert.equal(text('pairing.ROLE'), 'from the ETS function');
    assert.equal(text('pairing.OBJECTS'), "from the device's objects");
    assert.equal(text('pairing.FUNCTION'), 'from the function');
    assert.equal(text('pairing.NAMES'), 'from the names');
    assert.equal(text('pairing.NONE'), 'not paired');
});

test('texts: the left-out line is one text with both counts, and the three old keys are gone', () => {
    assert.equal(text('wizard.leftOut', 3, 24), 'Got no Thing, no address of their own left: functions 3, devices 24');
    assert.equal(text('wizard.leftOut', 0, 1), 'Got no Thing, no address of their own left: functions 0, devices 1');
    for (const key of ['wizard.leftOutFunctions', 'wizard.leftOutDevices', 'wizard.leftOutBoth']) {
        assert.equal(text(key), key);
    }
});

test('texts: the category problem names the whole rule of the check', () => {
    assert.equal(
        text('problem.bad-category'),
        'The category may hold only small letters, digits, _ and -, may not start with -, has at most 40 characters and may not be true or false.',
    );
});

test('texts: the read difference of the check says that nothing answers, not which object lacks a flag', () => {
    assert.equal(text('check.READ_WITHOUT_READ_FLAG'), 'read is configured, but nothing answers a read on this address');
});

test('texts: the label and the note of a filter device', () => {
    assert.equal(text('device.filter'), 'filter device');
    assert.equal(
        text('device.filterNote'),
        "A filter device only fills a line coupler's filter table. It is no hardware and never answers, so its objects give no read mark.",
    );
});

test('texts: no file of the page joins a list with a word of its own', () => {
    const found = [];
    let joins = 0;
    for (const file of jsFiles()) {
        const source = readFileSync(new URL(file, WEB), 'utf8');
        for (const match of source.matchAll(/\.join\(\s*(['"`])((?:\\.|(?!\1).)*)\1/g)) {
            joins++;
            // '\n' is a line break, not the letter n
            if (/\p{L}/u.test(match[2].replace(/\\./g, ''))) {
                found.push(`${file}: ${match[0]}`);
            }
        }
    }
    assert.deepEqual(found, []);
    assert.ok(joins > 5, `${joins} joins seen`);
});

test('texts: the panels carry the names ETS users know', () => {
    assert.deepEqual(
        ['group-addresses', 'topology', 'buildings', 'monitor', 'project'].map((view) => text(`view.${view}`)),
        ['Group Addresses', 'Topology', 'Buildings', 'Group Monitor', 'Project'],
    );
});

test('texts: a text that says which file to bring or which tool made something names ETS', () => {
    assert.equal(text('setup.projectUpload'), 'Upload an ETS project');
    assert.equal(text('setup.projectFile'), 'ETS project file (.knxproj)');
    assert.equal(text('setup.project'), 'ETS project');
    assert.equal(text('setup.toolVersion'), 'ETS version');
    assert.equal(text('setup.lastModified'), 'Last changed in ETS');
    assert.equal(text('project.none'), 'No ETS project is loaded for this bridge.');
    assert.equal(text('error.not-a-project'), 'The file is not an ETS project.');
    assert.equal(text('source.FUNCTION'), 'ETS function');
});

test('texts: the page does not call itself ETS, in its name, its tabs, its frame or its files', () => {
    const source = readFileSync(new URL('js/texts.js', WEB), 'utf8');
    const entries = [...source.matchAll(/^\s+'([^']+)':\s*\n?\s*(['"])((?:\\.|(?!\2).)*)\2,$/gm)].map((match) => [match[1], match[3]]);
    assert.ok(entries.length > 300, `${entries.length} texts read`);
    const own = entries.filter(([key]) => /^(app|view|frame|column|login|bridges)\./.test(key));
    assert.ok(own.length > 60, `${own.length} texts of the frame read`);
    assert.deepEqual(
        own.filter(([, value]) => /\bETS\b/i.test(value)),
        [],
    );
    assert.deepEqual(
        entries.filter(([, value]) => /ETS[- ]like|like ETS/i.test(value)),
        [],
    );
    for (const file of ['index.html', 'app.css']) {
        assert.doesNotMatch(readFileSync(new URL(file, WEB), 'utf8').replace(/\/\*[^]*?\*\//g, ''), /\bETS\b/i, file);
    }
});

// The texts that say which file to bring or which tool made something: every one of them names ETS.
const ETS_KEYS = [
    'addresses.noProject',
    'addresses.unreadable',
    'addresses.newer',
    'project.none',
    'project.unreadable',
    'project.newer',
    'setup.removedBridge',
    'setup.project',
    'setup.toolVersion',
    'setup.lastModified',
    'setup.projectUpload',
    'setup.projectFile',
    'setup.removeConfirm',
    'setup.checkNeedsProject',
    'wizard.unknown',
    'problem.unknown-address',
    'problem.no-data-type',
    'pairing.ROLE',
    'source.FUNCTION',
    'error.not-a-project',
    'error.malformed',
    'error.password-required',
    'error.wrong-password',
    'error.unsupported-protection',
    'error.limit-exceeded',
    'error.no-project',
    'error.project-unusable',
];

test('texts: each of the 27 texts about the project file or its tool names ETS, and no other text does', () => {
    assert.equal(ETS_KEYS.length, 27);
    assert.equal(new Set(ETS_KEYS).size, 27);
    for (const key of ETS_KEYS) {
        assert.ok(hasText(key), key);
        assert.match(text(key), /\bETS\b/, key);
    }
    const source = readFileSync(new URL('js/texts.js', WEB), 'utf8');
    const naming = [...source.matchAll(/^\s+'([^']+)':\s*\n?\s*(['"])((?:\\.|(?!\2).)*)\2,$/gm)].filter((match) => /\bETS\b/.test(match[3])).map((match) => match[1]);
    assert.deepEqual(naming.sort(), [...ETS_KEYS].sort());
});

test('texts: the words of the new frame: the theme, the tabs of Properties, the containers, the dynamic folders', () => {
    assert.deepEqual(['theme.auto', 'theme.light', 'theme.dark'].map((key) => text(key)), ['Auto', 'Light', 'Dark']);
    assert.deepEqual(['frame.settings', 'frame.comments', 'frame.information'].map((key) => text(key)), ['Settings', 'Comments', 'Information']);
    assert.deepEqual(['frame.properties', 'frame.openhab', 'frame.live', 'frame.findProject'].map((key) => text(key)), ['Properties', 'openHAB', 'Live', 'Find']);
    assert.equal(text('frame.dynamicFolders'), 'Dynamic Folders');
    assert.deepEqual(
        ['folder.noRoom', 'folder.filter', 'folder.inOpenhab', 'folder.notInOpenhab', 'topology.notAssigned', 'folder.noAssociations', 'folder.secured'].map((key) => text(key)),
        ['Not assigned to a room', 'Filter devices', 'In openHAB', 'Not in openHAB', 'Not assigned to a line', 'No associations', 'Secured'],
    );
    assert.deepEqual(['column.linkedWith', 'column.otherLinked', 'column.buildingFunction', 'column.buildingPart'].map((key) => text(key)), ['Linked with', 'Other Linked', 'Building Function', 'Building Part']);
    assert.equal(text('frame.tabCount', 'Group Objects', 52), 'Group Objects (52)');
    for (const kind of ['address', 'device', 'object', 'room']) {
        assert.ok(hasText(`find.${kind}`), kind);
    }
});
