import { test } from 'node:test';
import assert from 'node:assert/strict';

import { createLayout } from '../web/js/layout.js';
import { COLUMN_WIDTH, PAGE_STEP, ROWS_CAP, createTable } from '../web/js/table.js';
import { createDocument } from './fakedom.js';

const ROWS = [
    { id: 'a', address: '31/0/10', name: 'Kitchen <b>', size: 8 },
    { id: 'b', address: '31/0/2', name: 'hall', size: 1 },
    { id: 'c', address: '31/1/1', name: 'Bedroom', size: 16 },
];

function open(more = {}) {
    const document = createDocument();
    const selected = [];
    const opened = [];
    let live = 'x';
    const table = createTable({
        document,
        label: 'Addresses',
        columns: [
            { key: 'address', label: 'Address', value: (row) => row.address },
            { key: 'name', label: 'Name', value: (row) => row.name },
            { key: 'size', label: 'Length', title: 'Size in bits', value: (row) => row.size, find: false, className: 'num' },
            { key: 'live', label: 'Last Value', value: () => live, stamp: () => live, find: false },
        ],
        keyOf: (row) => row.id,
        onSelect: (row) => selected.push(row?.id ?? null),
        onOpen: (row) => opened.push(row.id),
        ...more,
    });
    table.setRows(ROWS, { none: 'Nothing here.', noMatch: 'Nothing found.' });
    return { document, table, selected, opened, setLive: (value) => (live = value) };
}

function ids(table) {
    return table.node.querySelector('tbody').children.map((row) => row.children[0].textContent);
}

function heading(table, label) {
    return table.node.querySelectorAll('th').find((cell) => cell.textContent === label);
}

function row(table, address) {
    return table.node.querySelector('tbody').children.find((candidate) => candidate.children[0].textContent === address);
}

test('table: a real table with a grid role, column headings as buttons, and server text as text', () => {
    const { table } = open();
    const grid = table.node.querySelector('table');
    assert.equal(grid.getAttribute('role'), 'grid');
    assert.equal(grid.getAttribute('aria-label'), 'Addresses');
    assert.deepEqual(table.node.querySelectorAll('th').map((cell) => [cell.textContent, cell.getAttribute('scope'), cell.children[0].tagName]), [
        ['Address', 'col', 'BUTTON'],
        ['Name', 'col', 'BUTTON'],
        ['Length', 'col', 'BUTTON'],
        ['Last Value', 'col', 'BUTTON'],
    ]);
    assert.equal(heading(table, 'Length').querySelector('button').getAttribute('title'), 'Size in bits');
    assert.deepEqual(ids(table), ['31/0/10', '31/0/2', '31/1/1']);
    assert.equal(row(table, '31/0/10').children[1].textContent, 'Kitchen <b>');
    assert.equal(row(table, '31/0/10').children[1].children.length, 0);
    assert.equal(row(table, '31/0/10').children[2].textContent, '8');
    assert.ok(row(table, '31/0/10').children[2].classList.contains('num'));
    assert.deepEqual(table.counts(), { shown: 3, total: 3 });
});

test('table: a click on a heading sorts, a second click turns the order, and the heading says so', () => {
    const { table } = open();
    heading(table, 'Address').querySelector('button').click();
    assert.deepEqual(ids(table), ['31/0/2', '31/0/10', '31/1/1']);
    assert.equal(heading(table, 'Address').getAttribute('aria-sort'), 'ascending');
    heading(table, 'Address').querySelector('button').click();
    assert.deepEqual(ids(table), ['31/1/1', '31/0/10', '31/0/2']);
    assert.equal(heading(table, 'Address').getAttribute('aria-sort'), 'descending');
    heading(table, 'Length').querySelector('button').click();
    assert.deepEqual(ids(table), ['31/0/2', '31/0/10', '31/1/1']);
    assert.equal(heading(table, 'Address').getAttribute('aria-sort'), null);
    assert.equal(heading(table, 'Length').getAttribute('aria-sort'), 'ascending');
    assert.deepEqual(table.sorted(), { key: 'size', descending: false });
    heading(table, 'Name').querySelector('button').click();
    assert.deepEqual(ids(table), ['31/1/1', '31/0/2', '31/0/10']);
});

test('table: a column can bring its own order', () => {
    const { table } = open({
        columns: [{ key: 'address', label: 'Address', value: (row) => row.address, compare: (a, b) => b.size - a.size }],
    });
    table.node.querySelector('button').click();
    assert.deepEqual(ids(table), ['31/1/1', '31/0/10', '31/0/2']);
});

test('table: the find box filters as you type, over the columns that take part, and says when nothing is found', () => {
    const { table } = open();
    const find = table.findField;
    assert.equal(find.getAttribute('type'), 'search');
    assert.equal(find.getAttribute('aria-label'), 'Find');
    find.value = ' HALL ';
    find.fire('input');
    assert.deepEqual(ids(table), ['31/0/2']);
    assert.deepEqual(table.counts(), { shown: 1, total: 3 });
    assert.equal(table.find(), 'hall');
    find.value = '16';
    find.fire('input');
    assert.deepEqual(ids(table), []);
    assert.equal(table.node.querySelector('.empty').textContent, 'Nothing found.');
    find.value = '31/1';
    find.fire('input');
    assert.deepEqual(ids(table), ['31/1/1']);
    assert.equal(table.node.querySelector('.empty'), null);
    table.setRows([]);
    assert.equal(table.node.querySelector('.empty').textContent, 'Nothing here.');
});

test('table: a click selects a row; arrow keys move the selection, Enter opens', () => {
    const { document, table, selected, opened } = open();
    assert.equal(row(table, '31/0/10').getAttribute('tabindex'), '0');
    assert.equal(row(table, '31/0/2').getAttribute('tabindex'), '-1');
    row(table, '31/0/2').children[1].fire('click');
    assert.deepEqual(selected, ['b']);
    assert.equal(row(table, '31/0/2').getAttribute('aria-selected'), 'true');
    assert.ok(row(table, '31/0/2').classList.contains('selected'));
    assert.equal(row(table, '31/0/2').getAttribute('tabindex'), '0');
    assert.equal(row(table, '31/0/10').getAttribute('tabindex'), '-1');
    assert.equal(row(table, '31/0/2').fire('keydown', { key: 'ArrowDown' }).defaultPrevented, true);
    assert.deepEqual(selected, ['b', 'c']);
    assert.equal(document.activeElement, row(table, '31/1/1'));
    assert.equal(row(table, '31/0/2').getAttribute('aria-selected'), 'false');
    row(table, '31/1/1').fire('keydown', { key: 'ArrowDown' });
    assert.deepEqual(selected, ['b', 'c', 'c']);
    row(table, '31/1/1').fire('keydown', { key: 'Home' });
    row(table, '31/0/10').fire('keydown', { key: 'End' });
    row(table, '31/1/1').fire('keydown', { key: 'ArrowUp' });
    assert.equal(table.selected().id, 'b');
    assert.deepEqual(opened, []);
    row(table, '31/0/2').fire('keydown', { key: 'Enter' });
    assert.deepEqual(opened, ['b']);
    row(table, '31/0/10').fire('dblclick');
    assert.deepEqual(opened, ['b', 'a']);
    assert.equal(row(table, '31/0/2').fire('keydown', { key: 'a' }).defaultPrevented, false);
});

test('table: the selection stays through sorting and finding, and select() tells nobody', () => {
    const { table, selected } = open();
    assert.equal(table.select('c').id, 'c');
    assert.deepEqual(selected, []);
    heading(table, 'Name').querySelector('button').click();
    assert.equal(row(table, '31/1/1').getAttribute('aria-selected'), 'true');
    table.findField.value = 'kitchen';
    table.findField.fire('input');
    table.findField.value = '';
    table.findField.fire('input');
    assert.equal(row(table, '31/1/1').getAttribute('aria-selected'), 'true');
    assert.equal(table.selected().id, 'c');
    assert.equal(table.select('nothing'), null);
    assert.equal(table.selected(), null);
    assert.equal(row(table, '31/1/1').getAttribute('aria-selected'), 'false');
});

test('table: a row that stays keeps its node; refresh draws again only the cells whose stamp changed', () => {
    const { table, setLive } = open();
    const before = row(table, '31/0/2');
    const name = before.children[1].firstChild;
    table.setRows([ROWS[1], ROWS[0]]);
    assert.equal(row(table, '31/0/2'), before);
    assert.deepEqual(ids(table), ['31/0/2', '31/0/10']);
    assert.equal(before.children[3].textContent, 'x');
    setLive('y');
    table.refresh();
    assert.equal(before.children[3].textContent, 'y');
    assert.equal(before.children[1].firstChild, name);
    table.setRows([{ ...ROWS[1], name: 'Hall 2' }]);
    assert.notEqual(row(table, '31/0/2'), before);
});

test('table: a row of the caller\'s own is shown as it is and never selected', () => {
    const document = createDocument();
    const selected = [];
    const table = createTable({
        document,
        label: 'Rows',
        columns: [{ key: 'address', label: 'Address', value: (row) => row.address ?? '' }],
        keyOf: (row) => row.id,
        versionOf: (row) => row.version,
        rowNode: (row) => (row.marker ? document.createElement('tr') : null),
        rowClass: (row) => (row.own ? 'own' : null),
        onSelect: (row) => selected.push(row?.id ?? null),
    });
    table.setRows([{ id: 1, version: 0, marker: true }, { id: 2, version: 0, address: '31/0/1', own: true }]);
    const [marker, record] = table.node.querySelector('tbody').children;
    assert.equal(marker.children.length, 0);
    assert.ok(record.classList.contains('own'));
    assert.equal(record.getAttribute('tabindex'), '0');
    marker.fire('click');
    assert.deepEqual(selected, []);
    record.fire('keydown', { key: 'ArrowUp' });
    assert.deepEqual(selected, [2]);
    table.setRows([{ id: 1, version: 0, marker: true }, { id: 2, version: 1, address: '31/0/1' }]);
    assert.equal(table.node.querySelector('tbody').children[0], marker);
    assert.notEqual(table.node.querySelector('tbody').children[1], record);
    assert.equal(table.node.querySelector('tbody').children[1].getAttribute('aria-selected'), 'true');
});

test('table: close() takes the listeners away', () => {
    const { document, table, selected } = open();
    table.close();
    row(table, '31/0/2').fire('click');
    assert.deepEqual(selected, []);
    assert.equal(document.listenerCount(), 0);
});

test('table: a sort leaves a row of the caller\'s own out and never asks a column for its value', () => {
    const document = createDocument();
    const table = createTable({
        document,
        label: 'Rows',
        columns: [{ key: 'address', label: 'Address', value: (row) => row.record.address }],
        keyOf: (row) => row.id,
        rowNode: (row) => (row.marker ? document.createElement('tr') : null),
    });
    const rows = [{ id: 1, record: { address: '31/0/9' } }, { id: 2, marker: true, record: { address: '' } }, { id: 3, record: { address: '31/0/1' } }];
    table.setRows(rows);
    assert.equal(table.node.querySelector('tbody').children.length, 3);
    rows[1].record = undefined;
    table.node.querySelector('thead').querySelector('button').click();
    assert.deepEqual(table.node.querySelector('tbody').children.map((row) => row.textContent), ['31/0/1', '31/0/9']);
    assert.deepEqual(table.shown().map((row) => row.id), [3, 1]);
});

test('table: a third click on a heading goes back to the order the rows came in', () => {
    const { table } = open();
    const button = heading(table, 'Address').querySelector('button');
    button.click();
    button.click();
    button.click();
    assert.deepEqual(ids(table), ['31/0/10', '31/0/2', '31/1/1']);
    assert.equal(heading(table, 'Address').getAttribute('aria-sort'), null);
    assert.equal(table.sorted(), null);
    button.click();
    assert.deepEqual(table.sorted(), { key: 'address', descending: false });
});

function withMarkers() {
    const document = createDocument();
    const made = [];
    const table = createTable({
        document,
        label: 'Rows',
        columns: [{ key: 'address', label: 'Address', value: (row) => row.address }],
        keyOf: (row) => row.id,
        versionOf: (row) => row.version,
        rowNode: (row) => {
            if (!row.marker) {
                return null;
            }
            made.push(row.id);
            const node = document.createElement('tr');
            node.setAttribute('class', 'marker');
            return node;
        },
    });
    return { document, table, made };
}

test('table: a row of the caller\'s own is kept while a sort leaves it out, and comes back as the same node', () => {
    const { table, made } = withMarkers();
    const rows = [{ id: 1, version: 0, address: '31/0/9' }, { id: 2, version: 0, marker: true }, { id: 3, version: 0, address: '31/0/1' }];
    table.setRows(rows);
    const marker = table.node.querySelector('.marker');
    const button = table.node.querySelector('thead').querySelector('button');
    button.click();
    table.setRows([...rows]);
    table.setRows([...rows]);
    assert.equal(table.node.querySelector('.marker'), null);
    assert.deepEqual(made, [2]);
    button.click();
    button.click();
    assert.equal(table.node.querySelector('.marker'), marker);
    assert.deepEqual(made, [2]);
});

test('table: sorted with nothing but rows of the caller\'s own, these are shown and nothing says "not found"', () => {
    const { table } = withMarkers();
    table.setRows([{ id: 1, version: 0, address: '31/0/9' }], { none: 'Nothing here.', noMatch: 'Nothing found.' });
    table.node.querySelector('thead').querySelector('button').click();
    table.setRows([{ id: 2, version: 0, marker: true }, { id: 3, version: 0, marker: true }]);
    assert.equal(table.node.querySelector('tbody').children.length, 2);
    assert.equal(table.node.querySelector('.empty'), null);
    assert.deepEqual(table.counts(), { shown: 2, total: 2 });
});

function many(count) {
    return Array.from({ length: count }, (_, at) => ({ id: `r${at}`, address: `31/0/${at}`, name: `Row ${at}`, size: 1 }));
}

test('table: Page Down and Page Up move a page of rows, Home and End to the ends', () => {
    const { document, table, selected } = open();
    table.setRows(many(40));
    const rows = () => table.node.querySelector('tbody').children;
    assert.equal(rows()[0].fire('keydown', { key: 'PageDown' }).defaultPrevented, true);
    assert.equal(table.selected().id, `r${PAGE_STEP}`);
    assert.equal(document.activeElement, rows()[PAGE_STEP]);
    rows()[PAGE_STEP].fire('keydown', { key: 'PageDown' });
    rows()[2 * PAGE_STEP].fire('keydown', { key: 'PageDown' });
    rows()[3 * PAGE_STEP].fire('keydown', { key: 'PageDown' });
    assert.equal(table.selected().id, 'r39');
    rows()[39].fire('keydown', { key: 'PageUp' });
    assert.equal(table.selected().id, `r${39 - PAGE_STEP}`);
    rows()[3].fire('keydown', { key: 'PageUp' });
    assert.equal(table.selected().id, 'r0');
    rows()[0].fire('keydown', { key: 'End' });
    assert.equal(table.selected().id, 'r39');
    rows()[39].fire('keydown', { key: 'Home' });
    assert.equal(table.selected().id, 'r0');
    assert.equal(selected.length, 8);
});

test('table: a page is what fits the list when the browser tells the heights', () => {
    const { document, table } = open();
    const list = document.createElement('div');
    list.appendChild(table.node);
    list.clientHeight = 240;
    table.setRows(many(40));
    for (const row of table.node.querySelector('tbody').children) {
        row.offsetHeight = 24;
    }
    table.node.querySelector('tbody').children[0].fire('keydown', { key: 'PageDown' });
    assert.equal(table.selected().id, 'r9');
});

test('table: a redraw that takes the focused row away puts the focus on the first row, one that keeps it leaves it', () => {
    const { document, table } = open();
    row(table, '31/0/2').focus();
    table.setRows([ROWS[2], ROWS[1]]);
    assert.equal(document.activeElement, row(table, '31/0/2'), 'moved, and found again');
    table.setRows([ROWS[2], ROWS[0]]);
    assert.equal(document.activeElement, row(table, '31/1/1'), 'gone: the first row');
    table.select('a');
    row(table, '31/1/1').focus();
    table.setRows([ROWS[0], ROWS[1]]);
    assert.equal(document.activeElement, row(table, '31/0/10'), 'gone: the selected row');
    const outside = document.createElement('button');
    outside.focus();
    table.setRows([ROWS[1]]);
    assert.equal(document.activeElement, outside, 'the focus was elsewhere and stays there');
});

test('table: a sort or a find keeps the focus on the row that had it', () => {
    const { document, table } = open();
    row(table, '31/1/1').focus();
    heading(table, 'Address').querySelector('button').click();
    heading(table, 'Address').querySelector('button').click();
    assert.equal(document.activeElement, row(table, '31/1/1'));
    table.findField.value = '31/';
    table.findField.fire('input');
    assert.equal(document.activeElement, row(table, '31/1/1'));
    table.findField.value = 'hall';
    table.findField.fire('input');
    assert.equal(document.activeElement, row(table, '31/0/2'));
});

test('table: focus() takes the selected row, else the first, and says when there is none', () => {
    const { document, table } = open();
    assert.equal(table.focus(), true);
    assert.equal(document.activeElement, row(table, '31/0/10'));
    table.select('c');
    table.focus();
    assert.equal(document.activeElement, row(table, '31/1/1'));
    table.setRows([]);
    document.activeElement = null;
    assert.equal(table.focus(), false);
    assert.equal(document.activeElement, null);
});

test('table: a limit draws only so many rows and makes no node for the others; matched() counts them all', () => {
    let cells = 0;
    const { table } = open({
        columns: [{ key: 'address', label: 'Address', value: (row) => row.address, cell: (row) => (cells++, row.address) }],
    });
    cells = 0;
    table.setLimit(5);
    table.setRows(many(40));
    assert.equal(table.node.querySelector('tbody').children.length, 5);
    assert.equal(cells, 5);
    assert.deepEqual(table.counts(), { shown: 5, total: 40 });
    assert.equal(table.matched(), 40);
    table.findField.value = '31/0/1';
    table.findField.fire('input');
    assert.equal(table.matched(), 11);
    assert.deepEqual(table.counts(), { shown: 11, total: 40 }, 'another find text starts from the list\'s own cap');
    table.setLimit(10);
    assert.equal(table.node.querySelector('tbody').children.length, 10);
    table.setLimit(Infinity);
    assert.equal(table.node.querySelector('tbody').children.length, 11);
});

test('table: a list draws 500 rows at first with Show more under it; each press adds 500, and the last one hands the focus to a row', () => {
    const { document, table } = open();
    const drawn = () => table.node.querySelector('tbody').children.length;
    assert.equal(ROWS_CAP, 500);
    assert.equal(table.node.querySelector('.more'), null);
    table.setRows(many(1200));
    assert.equal(drawn(), 500);
    assert.deepEqual(table.counts(), { shown: 500, total: 1200 });
    const more = table.node.querySelector('.more');
    assert.equal(more.textContent, 'Show more');
    assert.ok(table.node.children.at(-1) === more, 'under the list');
    more.click();
    assert.equal(drawn(), 1000);
    more.focus();
    more.click();
    assert.equal(drawn(), 1200);
    assert.equal(table.node.querySelector('.more'), null);
    assert.ok(document.activeElement === table.node.querySelector('tbody').children[0]);
    table.findField.value = 'row';
    table.findField.fire('input');
    assert.equal(drawn(), 500, 'another find text starts with the first rows again');
    table.close();
    assert.equal(document.listenerCount(), 0);
});

test('table: a row selected by its key below the drawn rows is drawn; a list that bounds its rows itself has no cap', () => {
    const { table } = open();
    table.setRows(many(1200));
    assert.equal(table.select('r700').id, 'r700');
    const rows = table.node.querySelector('tbody').children;
    assert.equal(rows.length, 1000);
    assert.equal(rows[700].getAttribute('aria-selected'), 'true');
    const free = open({ cap: Infinity });
    free.table.setRows(many(1200));
    assert.equal(free.table.node.querySelector('tbody').children.length, 1200);
    assert.equal(free.table.node.querySelector('.more'), null);
});

test('table: other columns can be set; the rows are drawn again and a sort by a column that left is dropped', () => {
    const { table } = open();
    table.select('b');
    heading(table, 'Length').querySelector('button').click();
    table.setColumns([
        { key: 'address', label: 'Address', value: (row) => row.address },
        { key: 'extra', label: 'I', value: (row) => row.size },
    ]);
    assert.deepEqual(table.node.querySelectorAll('th').map((cell) => cell.textContent), ['Address', 'I']);
    assert.equal(table.sorted(), null);
    assert.deepEqual(ids(table), ['31/0/10', '31/0/2', '31/1/1']);
    assert.equal(row(table, '31/0/2').children.length, 2);
    assert.equal(row(table, '31/0/2').getAttribute('aria-selected'), 'true');
    heading(table, 'I').querySelector('button').click();
    assert.deepEqual(ids(table), ['31/0/2', '31/0/10', '31/1/1']);
    table.setColumns([
        { key: 'extra', label: 'I', value: (row) => row.size },
        { key: 'address', label: 'Address', value: (row) => row.address },
    ]);
    assert.equal(heading(table, 'I').getAttribute('aria-sort'), 'ascending');
    assert.deepEqual(table.node.querySelector('tbody').children.map((line) => line.children[1].textContent), ['31/0/2', '31/0/10', '31/1/1']);
});

const GROUPED = [
    { id: 'a1', address: '31/0/3', name: 'Switch', size: 1, device: { key: 'd2', label: '31.1.5 Switch actuator' } },
    { id: 'a2', address: '31/0/1', name: 'Status', size: 1, device: { key: 'd2', label: '31.1.5 Switch actuator' } },
    { id: 'b1', address: '31/0/9', name: 'Dim', size: 4, device: { key: 'd1', label: '31.1.2 Dimmer <b>' } },
    { id: 'a3', address: '31/0/2', name: 'Lock', size: 1, device: { key: 'd2', label: '31.1.5 Switch actuator' } },
];

function grouped(more = {}) {
    const made = open({ groupOf: (row) => row.device ?? null, ...more });
    made.table.setRows(GROUPED, { none: 'Nothing here.', noMatch: 'Nothing found.' });
    return made;
}

function outline(table) {
    return table.node.querySelector('tbody').children.map((line) => (line.classList.contains('group-row') ? `# ${line.querySelector('.label').textContent} (${line.querySelector('.count').textContent})` : line.children[0].textContent));
}

function fold(table, label) {
    return table.node.querySelectorAll('.fold').find((button) => button.querySelector('.label').textContent === label);
}

test('table: grouped rows stand under a heading row per group, the groups in the order they first come', () => {
    const { table } = grouped();
    assert.deepEqual(outline(table), ['# 31.1.5 Switch actuator (3)', '31/0/3', '31/0/1', '31/0/2', '# 31.1.2 Dimmer <b> (1)', '31/0/9']);
    const [heading] = table.node.querySelector('tbody').children;
    assert.equal(heading.children.length, 1);
    assert.equal(heading.children[0].getAttribute('colspan'), '4');
    const button = heading.querySelector('.fold');
    assert.equal(button.tagName, 'BUTTON');
    assert.equal(button.getAttribute('aria-expanded'), 'true');
    assert.equal(button.querySelector('.label').children.length, 0, 'the name of a group is text');
    assert.deepEqual(table.counts(), { shown: 4, total: 4 });
    // rows without a group stand first, without a heading
    table.setRows([{ id: 'x', address: '31/7/7', name: 'Loose', size: 1 }, ...GROUPED]);
    assert.deepEqual(outline(table).slice(0, 2), ['31/7/7', '# 31.1.5 Switch actuator (3)']);
});

test('table: a click on a group heading folds its rows away and back; the heading stays the same node', () => {
    const { table, selected } = grouped();
    const button = fold(table, '31.1.5 Switch actuator');
    button.click();
    assert.deepEqual(outline(table), ['# 31.1.5 Switch actuator (3)', '# 31.1.2 Dimmer <b> (1)', '31/0/9']);
    assert.equal(button.getAttribute('aria-expanded'), 'false');
    assert.deepEqual(table.folded(), ['d2']);
    assert.deepEqual(table.counts(), { shown: 1, total: 4 });
    assert.deepEqual(selected, [], 'folding selects nothing');
    assert.ok(fold(table, '31.1.5 Switch actuator') === button);
    // a click that lands on the drawing or the name inside the button
    button.querySelector('.label').fire('click');
    assert.deepEqual(outline(table).length, 6);
    assert.equal(button.getAttribute('aria-expanded'), 'true');
    fold(table, '31.1.2 Dimmer <b>').click();
    button.click();
    assert.deepEqual(outline(table), ['# 31.1.5 Switch actuator (3)', '# 31.1.2 Dimmer <b> (1)']);
    assert.equal(table.node.querySelector('.empty'), null, 'folded rows are not "nothing found"');
});

test('table: Enter on a group heading folds as the button does and does not open the selected row', () => {
    const { table, opened } = grouped();
    table.select('a1');
    const event = fold(table, '31.1.2 Dimmer <b>').fire('keydown', { key: 'Enter' });
    assert.deepEqual(opened, []);
    assert.equal(event.defaultPrevented, false, 'the button\'s click follows and folds');
});

test('table: a sort orders the rows inside their groups, the find box counts what is left in each, and the keys pass over headings', () => {
    const { table, selected } = grouped();
    heading(table, 'Address').querySelector('button').click();
    assert.deepEqual(outline(table), ['# 31.1.5 Switch actuator (3)', '31/0/1', '31/0/2', '31/0/3', '# 31.1.2 Dimmer <b> (1)', '31/0/9']);
    table.findField.value = '31/0/3';
    table.findField.fire('input');
    assert.deepEqual(outline(table), ['# 31.1.5 Switch actuator (1)', '31/0/3']);
    table.findField.value = '';
    table.findField.fire('input');
    const last = row(table, '31/0/3');
    last.fire('click');
    last.fire('keydown', { key: 'ArrowDown' });
    assert.deepEqual(selected, ['a1', 'b1'], 'from the last row of a group to the first of the next');
    row(table, '31/0/9').fire('keydown', { key: 'ArrowUp' });
    assert.deepEqual(selected.at(-1), 'a1');
    row(table, '31/0/3').fire('keydown', { key: 'Home' });
    assert.deepEqual(selected.at(-1), 'a2');
});

function edge(table, label) {
    return heading(table, label).querySelector('.col-resize');
}

test('table: the edge of a heading is dragged to set the column\'s width, in the heading and in every row, also rows made later', () => {
    const { table } = open();
    const handle = edge(table, 'Name');
    assert.equal(handle.getAttribute('role'), 'separator');
    assert.equal(handle.getAttribute('aria-label'), 'Width of the column Name');
    const name = heading(table, 'Name');
    assert.equal(name.classList.contains('sized'), false);
    handle.fire('pointerdown', { clientX: 300 });
    handle.fire('pointermove', { clientX: 340 });
    assert.equal(name.style.getPropertyValue('--col-width'), `${COLUMN_WIDTH.initial + 40}px`);
    assert.ok(name.classList.contains('sized'));
    const cells = () => table.node.querySelector('tbody').children.map((line) => line.children[1]);
    assert.deepEqual(cells().map((cell) => [cell.classList.contains('sized'), cell.style.getPropertyValue('--col-width')]), [[true, '160px'], [true, '160px'], [true, '160px']]);
    assert.equal(table.node.querySelector('tbody').children[0].children[0].classList.contains('sized'), false, 'the other columns keep their own width');
    handle.fire('pointermove', { clientX: -5000 });
    assert.equal(name.style.getPropertyValue('--col-width'), `${COLUMN_WIDTH.min}px`);
    handle.fire('pointermove', { clientX: 9000 });
    assert.equal(name.style.getPropertyValue('--col-width'), `${COLUMN_WIDTH.max}px`);
    handle.fire('pointermove', { clientX: 380 });
    handle.fire('pointerup', {});
    handle.fire('pointermove', { clientX: 900 });
    assert.deepEqual(table.widths(), { name: 200 }, 'the pointer that let go moves nothing');
    table.setRows([...ROWS, { id: 'd', address: '31/2/2', name: 'New', size: 1 }]);
    assert.equal(cells()[3].style.getPropertyValue('--col-width'), '200px');
    assert.equal(table.sorted(), null, 'dragging the edge does not sort');
    // a double click on the edge gives the column its own width again
    handle.fire('dblclick');
    assert.deepEqual(table.widths(), {});
    assert.equal(name.classList.contains('sized'), false);
    assert.equal(cells()[0].style.getPropertyValue('--col-width'), '');
});

test('table: the widths are kept per list and come back; without a store or a name they last for the visit', () => {
    const kept = { text: null };
    const layout = () => createLayout({ read: () => kept.text, write: (value) => (kept.text = value) });
    const first = open({ layout: layout(), name: 'devices' });
    const handle = edge(first.table, 'Address');
    handle.fire('pointerdown', { clientX: 100 });
    handle.fire('pointermove', { clientX: 90 });
    assert.equal(kept.text, null, 'kept when the pointer lets go');
    handle.fire('pointerup', {});
    assert.deepEqual(JSON.parse(kept.text), { 'columns.devices': { address: 110 } });

    const again = open({ layout: layout(), name: 'devices' });
    assert.equal(heading(again.table, 'Address').style.getPropertyValue('--col-width'), '110px');
    assert.equal(again.table.node.querySelector('tbody').children[0].children[0].style.getPropertyValue('--col-width'), '110px');
    const other = open({ layout: layout(), name: 'objects' });
    assert.deepEqual(other.table.widths(), {}, 'each list has its own widths');
    edge(again.table, 'Address').fire('dblclick');
    assert.deepEqual(JSON.parse(kept.text), { 'columns.devices': {} });

    for (const more of [{}, { name: 'devices' }, { layout: createLayout(null), name: 'devices' }, { layout: createLayout({ read: () => '{"columns.devices":"wide"}', write() {} }), name: 'devices' }]) {
        const { table } = open(more);
        edge(table, 'Name').fire('pointerdown', { clientX: 0 });
        edge(table, 'Name').fire('pointermove', { clientX: 10 });
        edge(table, 'Name').fire('pointerup', {});
        assert.deepEqual(table.widths(), { name: 130 });
    }
});

test('table: the sorted column shows its direction in the heading, and close() leaves no listener', () => {
    const { table, document } = open();
    heading(table, 'Name').querySelector('button').click();
    assert.equal(heading(table, 'Name').getAttribute('aria-sort'), 'ascending');
    heading(table, 'Name').querySelector('button').click();
    assert.equal(heading(table, 'Name').getAttribute('aria-sort'), 'descending');
    assert.equal(heading(table, 'Address').hasAttribute('aria-sort'), false);
    table.close();
    assert.equal(document.listenerCount(), 0);
});

test('table: a list that takes several rows: Ctrl and a click adds or takes one, Shift and a click or an arrow key takes the rows between', () => {
    const { table, selected } = open({ multi: true });
    const marked = () => table.node.querySelector('tbody').children.filter((line) => line.getAttribute('aria-selected') === 'true').map((line) => line.children[0].textContent);
    const picked = () => table.picked().map((entry) => entry.id);
    row(table, '31/0/10').click();
    assert.deepEqual([marked(), picked()], [['31/0/10'], ['a']]);
    row(table, '31/1/1').fire('click', { ctrlKey: true });
    assert.deepEqual([marked(), picked()], [['31/0/10', '31/1/1'], ['a', 'c']]);
    assert.equal(table.selected().id, 'c');
    assert.equal(row(table, '31/1/1').getAttribute('tabindex'), '0');
    assert.equal(row(table, '31/0/10').getAttribute('tabindex'), '-1');
    row(table, '31/1/1').fire('click', { ctrlKey: true });
    assert.deepEqual([picked(), table.selected().id], [['a'], 'a']);
    // the row that is left is where a range starts from
    row(table, '31/1/1').fire('click', { shiftKey: true });
    assert.deepEqual([marked(), table.selected().id], [['31/0/10', '31/0/2', '31/1/1'], 'c']);
    row(table, '31/1/1').fire('keydown', { key: 'ArrowUp', shiftKey: true });
    assert.deepEqual(picked(), ['a', 'b']);
    row(table, '31/0/2').fire('keydown', { key: 'ArrowDown' });
    assert.deepEqual(picked(), ['c']);
    assert.deepEqual(selected, ['a', 'c', 'a', 'c', 'b', 'c']);
    // rows that are made again stay selected
    table.setRows(ROWS.map((entry) => ({ ...entry })));
    row(table, '31/0/2').fire('click', { metaKey: true });
    table.setRows(ROWS.map((entry) => ({ ...entry })));
    assert.deepEqual(marked(), ['31/0/2', '31/1/1']);
    table.select(undefined);
    assert.deepEqual([marked(), picked(), table.selected()], [[], [], null]);
});

test('table: a list of one selected row takes no second one with Ctrl or Shift, and picked() is that row', () => {
    const { table } = open();
    row(table, '31/0/10').click();
    row(table, '31/1/1').fire('click', { ctrlKey: true });
    row(table, '31/0/2').fire('click', { shiftKey: true });
    assert.deepEqual(
        table.node.querySelector('tbody').children.filter((line) => line.classList.contains('selected')).map((line) => line.children[0].textContent),
        ['31/0/2'],
    );
    assert.deepEqual(table.picked().map((entry) => entry.id), ['b']);
});
