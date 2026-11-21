import { test } from 'node:test';
import assert from 'node:assert/strict';

import { FLAGS, LIST_FLAGS, copyText, flagMark, linkOrder, objectSections, orderObjects, sizeText } from '../web/js/views/objects.js';
import { createDocument } from './fakedom.js';

function object(number, name, groupAddresses = [], more = {}) {
    const made = { name, groupAddresses, ...more };
    if (number !== null) {
        made.number = number;
    }
    return made;
}

test('objects: an object without a number comes last, also before reading order; an unlinked one beside a linked one is unused', () => {
    const ordered = orderObjects([object(null, 'n1', ['31/7/1']), object(9, 'nine'), object(null, 'n2'), object(0, 'zero')]);
    assert.deepEqual(
        ordered.map((entry) => entry.object.name),
        ['zero', 'nine', 'n1', 'n2'],
    );
    assert.deepEqual(
        ordered.map((entry) => entry.unused),
        [false, false, false, false],
    );
    const same = orderObjects([object(5, 'b', []), object(5, 'a', ['31/7/1']), object(5, 'c', ['31/7/2'])]);
    assert.deepEqual(
        same.map((entry) => [entry.object.name, entry.unused]),
        [
            ['a', false],
            ['c', false],
            ['b', true],
        ],
    );
    assert.deepEqual(orderObjects(undefined), []);
});

test('objects: the sending address comes first', () => {
    assert.deepEqual(linkOrder(object(1, 'x', ['31/7/3', '31/7/1'], { sendingGroupAddress: '31/7/1' })), ['31/7/1', '31/7/3']);
    assert.deepEqual(linkOrder(object(1, 'x', ['31/7/3', '31/7/1'])), ['31/7/3', '31/7/1']);
});

test('objects: sizes in bits and bytes', () => {
    assert.deepEqual([1, 4, 8, 16, 14 * 8, 0, undefined].map(sizeText), ['1 bit', '4 bits', '1 byte', '2 bytes', '14 bytes', '', '']);
});

test('objects: five flags have a column; a set flag shows its letter, one that is not set a dash', () => {
    assert.deepEqual(LIST_FLAGS.map(([, field]) => field), ['communication', 'read', 'write', 'transmit', 'update']);
    assert.equal(FLAGS.length, 6);
    const document = createDocument();
    const on = flagMark(document, true, 'flag.transmit', 'flag.letter.transmit');
    assert.equal(on.textContent, 'T');
    assert.equal(on.className, 'flag on');
    assert.equal(on.getAttribute('title'), 'Transmit: set');
    const off = flagMark(document, false, 'flag.read', 'flag.letter.read');
    assert.equal(off.textContent, '-');
    assert.equal(off.className, 'flag off');
    assert.equal(off.getAttribute('aria-label'), 'Read: not set');
});

test('objects: Properties of a group object: its fields, the flags as five tick boxes and Read on init when set, the description under Comments', () => {
    const made = object(4, 'Switch A', ['31/7/3', '31/7/1'], { sendingGroupAddress: '31/7/1', function: 'Switching', text: 'Output A', dataType: '1.001', sizeBits: 1, communication: true, transmit: true });
    const [fields, flags, comments] = objectSections(made);
    assert.equal(fields.tab, undefined, 'under Settings');
    assert.deepEqual(
        fields.rows.filter(([, value]) => value),
        [['Number', '4'], ['Name', 'Switch A'], ['Object Function', 'Switching'], ['Data Type', '1.001'], ['Length', '1 bit'], ['Linked with', '31/7/1'], ['Other Linked', '31/7/3']],
    );
    assert.deepEqual(flags.ticks, [['Communication', true], ['Read', false], ['Write', false], ['Transmit', true], ['Update', false]]);
    assert.equal(comments.tab, 'comments');
    assert.deepEqual(comments.rows, [['Description', 'Output A']]);
    assert.deepEqual(objectSections({ ...made, readOnInit: true })[1].ticks.at(-1), ['Read on init', true]);
    assert.deepEqual(objectSections({ name: 'Same', text: 'Same' })[2].rows, [['Description', '']], 'a description that only repeats the name is no comment');
});

test('objects: copyText works on a document without execCommand', async () => {
    const document = createDocument();
    document.execCommand = undefined;
    const field = document.createElement('textarea');
    field.value = 'x';
    assert.equal(await copyText(document, undefined, field), false);
    assert.equal(document.selection, field);
});
