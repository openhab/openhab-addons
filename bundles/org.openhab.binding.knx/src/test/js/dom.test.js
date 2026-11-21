import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readdirSync, readFileSync, statSync } from 'node:fs';

import { ATTRIBUTES, ICONS, SIZES, THEMES, attr, clear, el, icon, mainUiLink, place, size, theme } from '../web/js/dom.js';
import { allElements, createDocument, textNodes } from './fakedom.js';

const WEB = new URL('../web/', import.meta.url);

function webFiles(folder = WEB, prefix = '') {
    const files = [];
    for (const name of readdirSync(folder)) {
        const url = new URL(name, folder);
        if (statSync(url).isDirectory()) {
            files.push(...webFiles(new URL(`${name}/`, folder), `${prefix}${name}/`));
        } else {
            files.push(prefix + name);
        }
    }
    return files;
}

test('dom: a child text with markup makes one text node and no element', () => {
    const document = createDocument();
    const cell = el(document, 'td', {}, '<img src=x onerror=alert(1)>');
    assert.equal(cell.childNodes.length, 1);
    assert.equal(cell.firstChild.nodeType, 3);
    assert.equal(cell.textContent, '<img src=x onerror=alert(1)>');
    assert.deepEqual(allElements(cell), []);
});

test('dom: the attribute names style, onclick and src throw', () => {
    const document = createDocument();
    for (const name of ['style', 'onclick', 'src', 'onerror', 'srcdoc', 'formaction', 'Style', 'ID']) {
        assert.throws(() => el(document, 'p', { [name]: 'x' }), TypeError, name);
        assert.throws(() => attr(el(document, 'p', {}), name, 'x'), TypeError, name);
    }
});

test('dom: every name of the fixed list is accepted and set as text', () => {
    const document = createDocument();
    assert.deepEqual(
        [...ATTRIBUTES].sort(),
        [
            'aria-current',
            'aria-expanded',
            'aria-label',
            'aria-level',
            'aria-live',
            'aria-modal',
            'aria-orientation',
            'aria-pressed',
            'aria-selected',
            'aria-sort',
            'aria-valuemax',
            'aria-valuemin',
            'aria-valuenow',
            'autocomplete',
            'checked',
            'class',
            'colspan',
            'disabled',
            'for',
            'href',
            'id',
            'maxlength',
            'name',
            'open',
            'placeholder',
            'readonly',
            'role',
            'scope',
            'tabindex',
            'title',
            'type',
            'value',
        ],
    );
    const field = el(document, 'input', { type: 'password', autocomplete: 'off', maxlength: 10, title: '<b>x</b>' });
    assert.equal(field.getAttribute('type'), 'password');
    assert.equal(field.getAttribute('maxlength'), '10');
    assert.equal(field.getAttribute('title'), '<b>x</b>');
});

test('dom: true sets an empty attribute, false and null leave it out or remove it', () => {
    const document = createDocument();
    const button = el(document, 'button', { disabled: true, title: null, name: undefined, open: false });
    assert.equal(button.getAttribute('disabled'), '');
    assert.equal(button.hasAttribute('title'), false);
    assert.equal(button.hasAttribute('name'), false);
    assert.equal(button.hasAttribute('open'), false);
    attr(button, 'disabled', false);
    assert.equal(button.hasAttribute('disabled'), false);
    attr(button, 'disabled', true);
    assert.equal(button.getAttribute('disabled'), '');
});

test('dom: an href must be a route of the page', () => {
    const document = createDocument();
    assert.equal(el(document, 'a', { href: '#/b/knx%3Aip%3Ax/monitor' }).getAttribute('href'), '#/b/knx%3Aip%3Ax/monitor');
    for (const href of ['javascript:alert(1)', 'JavaScript:x', 'http://example.invalid/', '//example.invalid', '#x', '', 'x']) {
        assert.throws(() => el(document, 'a', { href }), TypeError, href);
    }
});

test('dom: an id or a class is a name of the page, never an address', () => {
    const document = createDocument();
    assert.equal(el(document, 'p', { id: 'login-state', class: 'note warn' }).getAttribute('class'), 'note warn');
    for (const value of ['31/7/1', '7.7.1', 'a b', '', '1x', '<b>', 'knx:ip:x']) {
        assert.throws(() => el(document, 'p', { id: value }), TypeError, value);
    }
    for (const value of ['31/7/1', '7.7.1', '', ' x', '1x', 'a<b']) {
        assert.throws(() => el(document, 'p', { class: value }), TypeError, value);
    }
});

test('dom: children are text nodes or elements, nested lists are flattened, empty ones skipped', () => {
    const document = createDocument();
    const item = el(document, 'li', {}, 'a', 7, null, undefined, false, ['b', [el(document, 'b', {}, 'c')]]);
    assert.deepEqual(textNodes(item), ['a', '7', 'b', 'c']);
    assert.equal(item.children.length, 1);
    assert.throws(() => el(document, 'li', {}, { textContent: 'x' }), TypeError);
    assert.throws(() => el(document, 'li', {}, () => 'x'), TypeError);
});

test('dom: the tag is a plain name and no attribute object is needed', () => {
    const document = createDocument();
    assert.equal(el(document, 'h2').tagName, 'H2');
    assert.equal(el(document, 'p', undefined, 'x').textContent, 'x');
    for (const tag of ['script', 'iframe', 'img x', '', '<p>', 'object', 'embed', 'style', 'link', 'base', 'meta']) {
        assert.throws(() => el(document, tag), TypeError, tag);
    }
});

test('dom: clear removes every child', () => {
    const document = createDocument();
    const list = el(document, 'ul', {}, el(document, 'li', {}, 'a'), 'b');
    clear(list);
    assert.equal(list.childNodes.length, 0);
});

test('dom: dom.js is the only file of the page that calls createElement or setAttribute', () => {
    const found = [];
    for (const file of webFiles()) {
        if (!file.endsWith('.js') || file === 'js/dom.js') {
            continue;
        }
        const source = readFileSync(new URL(file, WEB), 'utf8');
        if (/createElement|setAttribute|createTextNode/.test(source)) {
            found.push(file);
        }
    }
    assert.deepEqual(found, []);
});

test('dom: an icon is the page\'s own drawing, hidden from screen readers, and an unknown name throws', () => {
    const document = createDocument();
    for (const name of Object.keys(ICONS)) {
        const drawing = icon(document, name);
        assert.equal(drawing.tagName, 'SVG');
        assert.equal(drawing.namespaceURI, 'http://www.w3.org/2000/svg');
        assert.equal(drawing.getAttribute('aria-hidden'), 'true');
        assert.ok(drawing.classList.contains(`icon-${name}`));
        assert.equal(drawing.children.length, ICONS[name].length);
        assert.ok(drawing.children.every((path) => path.tagName === 'PATH' && /^[MmLlHhVvAaZz0-9 .-]+$/.test(path.getAttribute('d'))));
    }
    assert.throws(() => icon(document, 'logo'), TypeError);
    assert.throws(() => icon(document, 'constructor'), TypeError);
    assert.throws(() => el(document, 'svg', {}), TypeError);
});

test('dom: size sets one of the page\'s widths in px and takes it away again', () => {
    const document = createDocument();
    const node = el(document, 'div', {});
    assert.deepEqual([...SIZES], ['--tree-width', '--properties-width', '--col-width', '--second-height']);
    size(node, '--tree-width', 240.4);
    assert.equal(node.style.getPropertyValue('--tree-width'), '240px');
    size(node, '--tree-width', NaN);
    assert.equal(node.style.getPropertyValue('--tree-width'), '240px');
    size(node, '--tree-width', null);
    assert.equal(node.style.getPropertyValue('--tree-width'), '');
    assert.throws(() => size(node, 'width', 10), TypeError);
    assert.throws(() => size(node, '--other', 10), TypeError);
});

test('dom: place puts the nodes in order and keeps the ones that stay', () => {
    const document = createDocument();
    const [a, b, c] = ['a', 'b', 'c'].map((name) => el(document, 'li', {}, name));
    const list = el(document, 'ul', {}, a, b);
    place(list, [c, a]);
    assert.deepEqual(list.children, [c, a]);
    assert.equal(b.parentNode, null);
    place(list, []);
    assert.equal(list.childNodes.length, 0);
});

test('dom: place moves one node when the list slides by one', () => {
    const document = createDocument();
    const items = Array.from({ length: 301 }, (_, n) => el(document, 'li', {}, String(n)));
    const list = el(document, 'ul', {}, items.slice(0, 300));
    let moves = 0;
    const insertBefore = list.insertBefore.bind(list);
    list.insertBefore = (node, before) => {
        moves++;
        return insertBefore(node, before);
    };
    place(list, items.slice(1));
    assert.equal(moves, 1);
    assert.deepEqual(list.children, items.slice(1));
    assert.equal(items[0].parentNode, null);
});

test('dom: theme names light or dark on the root and takes any other name away', () => {
    const root = createDocument().createElement('html');
    assert.deepEqual([...THEMES], ['light', 'dark']);
    theme(root, 'dark');
    assert.equal(root.getAttribute('data-theme'), 'dark');
    theme(root, 'light');
    assert.equal(root.getAttribute('data-theme'), 'light');
    for (const other of ['auto', 'pink', '', undefined, 'constructor']) {
        theme(root, 'dark');
        theme(root, other);
        assert.equal(root.hasAttribute('data-theme'), false);
    }
});

test('dom: a link to Main UI is made from the kind and a checked id, below the root of openHAB, and opens a tab of its own', () => {
    const document = createDocument();
    const root = new URL('http://openhab.invalid/oh/');
    const thing = mainUiLink(document, root, 'thing', 'knx:device:made-up:Kitchen', 'Open');
    assert.equal(thing.tagName, 'A');
    assert.equal(thing.getAttribute('href'), '/oh/settings/things/knx:device:made-up:Kitchen');
    assert.equal(thing.getAttribute('target'), '_blank');
    assert.equal(thing.getAttribute('rel'), 'noopener noreferrer');
    assert.equal(thing.textContent, 'Open');
    assert.equal(mainUiLink(document, new URL('http://openhab.invalid/'), 'item', 'Kitchen_Light', 'x').getAttribute('href'), '/settings/items/Kitchen_Light');
    for (const [kind, id] of [
        ['thing', '../../rest/x'],
        ['thing', 'knx:device:a/b:c'],
        ['thing', 'knx'],
        ['item', 'Kitchen Light'],
        ['item', '1x'],
        ['page', 'knx:device:a:b'],
        ['constructor', 'x'],
        ['thing', undefined],
    ]) {
        const plain = mainUiLink(document, root, kind, id, 'words');
        assert.equal(plain.tagName, 'SPAN', `${kind} ${id}`);
        assert.equal(plain.hasAttribute('href'), false);
        assert.equal(plain.textContent, 'words');
    }
    assert.equal(mainUiLink(document, undefined, 'thing', 'knx:device:a:b', 'x').tagName, 'SPAN');
    assert.equal(mainUiLink(document, 'http://openhab.invalid/', 'thing', 'knx:device:a:b', 'x').tagName, 'SPAN');
});
