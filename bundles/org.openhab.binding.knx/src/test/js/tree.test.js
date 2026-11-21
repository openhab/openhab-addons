import { test } from 'node:test';
import assert from 'node:assert/strict';

import { createTree } from '../web/js/tree.js';
import { createDocument } from './fakedom.js';

const NODES = [
    {
        key: 'a',
        icon: 'range',
        address: '31',
        name: 'Lights <b>',
        children: [
            { key: 'a1', address: '31/0', name: 'Ground', children: [{ key: 'a1x', address: '31/0/1', name: 'Kitchen', children: [] }] },
            { key: 'a2', address: '31/1', name: 'First', mark: 'note', children: [] },
        ],
    },
    { key: 'b', name: 'Weather', children: [] },
];

function open() {
    const document = createDocument();
    const chosen = [];
    const tree = createTree({ document, label: 'Group Addresses', onSelect: (node) => chosen.push(node.key) });
    tree.setRoots(NODES);
    return { document, tree, chosen };
}

function shown(tree) {
    return tree.node.querySelectorAll('li').map((item) => item.querySelector('.name').textContent);
}

function item(tree, name) {
    return tree.node.querySelectorAll('li').find((candidate) => candidate.querySelector('.name').textContent === name);
}

test('tree: the roles, levels and texts; children come into the document when their node opens', () => {
    const { tree } = open();
    assert.equal(tree.node.getAttribute('role'), 'tree');
    assert.equal(tree.node.getAttribute('aria-label'), 'Group Addresses');
    assert.deepEqual(shown(tree), ['Lights <b>', 'Weather']);
    const lights = item(tree, 'Lights <b>');
    assert.equal(lights.getAttribute('role'), 'treeitem');
    assert.equal(lights.getAttribute('aria-level'), '1');
    assert.equal(lights.getAttribute('aria-expanded'), 'false');
    assert.equal(lights.querySelector('.address').textContent, '31');
    assert.ok(lights.querySelector('.icon-range'));
    assert.equal(item(tree, 'Weather').getAttribute('aria-expanded'), null);
    tree.open('a');
    assert.deepEqual(shown(tree), ['Lights <b>', 'Ground', 'First', 'Weather']);
    assert.equal(lights.getAttribute('aria-expanded'), 'true');
    assert.equal(item(tree, 'Ground').getAttribute('aria-level'), '2');
    assert.equal(item(tree, 'Ground').parentNode.getAttribute('role'), 'group');
    assert.equal(item(tree, 'First').querySelector('.kind').textContent, 'note');
    tree.open('a', false);
    assert.deepEqual(shown(tree), ['Lights <b>', 'Weather']);
});

test('tree: a click on a node selects it and tells; a click on its arrow only opens or closes it', () => {
    const { tree, chosen } = open();
    item(tree, 'Lights <b>').querySelector('.twisty').fire('click');
    assert.deepEqual(chosen, []);
    assert.deepEqual(shown(tree), ['Lights <b>', 'Ground', 'First', 'Weather']);
    item(tree, 'First').querySelector('.name').fire('click');
    assert.deepEqual(chosen, ['a2']);
    assert.equal(item(tree, 'First').getAttribute('aria-selected'), 'true');
    assert.ok(item(tree, 'First').classList.contains('selected'));
    item(tree, 'Weather').fire('click');
    assert.equal(item(tree, 'First').getAttribute('aria-selected'), 'false');
    assert.equal(tree.selected().key, 'b');
});

test('tree: a double click on a node opens and closes it', () => {
    const { tree } = open();
    item(tree, 'Lights <b>').querySelector('.name').fire('dblclick');
    assert.deepEqual(shown(tree), ['Lights <b>', 'Ground', 'First', 'Weather']);
    item(tree, 'Lights <b>').querySelector('.name').fire('dblclick');
    assert.deepEqual(shown(tree), ['Lights <b>', 'Weather']);
    item(tree, 'Weather').fire('dblclick');
    assert.deepEqual(shown(tree), ['Lights <b>', 'Weather']);
});

test('tree: arrow keys move, right opens, left closes, Enter selects', () => {
    const { document, tree, chosen } = open();
    const key = (name) => document.activeElement.fire('keydown', { key: name });
    item(tree, 'Lights <b>').focus();
    assert.equal(key('ArrowRight').defaultPrevented, true);
    assert.deepEqual(shown(tree), ['Lights <b>', 'Ground', 'First', 'Weather']);
    key('ArrowRight');
    assert.equal(document.activeElement, item(tree, 'Ground'));
    key('ArrowDown');
    assert.equal(document.activeElement, item(tree, 'First'));
    assert.equal(item(tree, 'First').getAttribute('tabindex'), '0');
    assert.equal(item(tree, 'Ground').getAttribute('tabindex'), '-1');
    key('ArrowDown');
    key('ArrowDown');
    assert.equal(document.activeElement, item(tree, 'Weather'));
    key('ArrowUp');
    key('ArrowLeft');
    assert.equal(document.activeElement, item(tree, 'Lights <b>'));
    assert.deepEqual(chosen, []);
    key('Enter');
    assert.deepEqual(chosen, ['a']);
    key('ArrowLeft');
    assert.deepEqual(shown(tree), ['Lights <b>', 'Weather']);
    key('End');
    assert.equal(document.activeElement, item(tree, 'Weather'));
    key('Home');
    assert.equal(document.activeElement, item(tree, 'Lights <b>'));
    assert.equal(key('x').defaultPrevented, false);
});

test('tree: select opens the way to a node without telling, and an unknown key selects nothing', () => {
    const { tree, chosen } = open();
    assert.equal(tree.select('a1x').name, 'Kitchen');
    assert.deepEqual(shown(tree), ['Lights <b>', 'Ground', 'Kitchen', 'First', 'Weather']);
    assert.equal(item(tree, 'Kitchen').getAttribute('aria-selected'), 'true');
    assert.equal(item(tree, 'Kitchen').getAttribute('aria-level'), '3');
    assert.deepEqual(chosen, []);
    assert.equal(tree.select('nothing'), null);
    assert.equal(tree.selected(), null);
    assert.equal(tree.find('b').name, 'Weather');
});

test('tree: new nodes keep what was open and selected, and close() takes the listeners away', () => {
    const { document, tree, chosen } = open();
    tree.select('a1x');
    tree.setRoots(JSON.parse(JSON.stringify(NODES)));
    assert.deepEqual(shown(tree), ['Lights <b>', 'Ground', 'Kitchen', 'First', 'Weather']);
    assert.equal(tree.selected().key, 'a1x');
    tree.close();
    item(tree, 'Weather').fire('click');
    assert.deepEqual(chosen, []);
    assert.equal(document.listenerCount(), 0);
});

test('tree: focus() puts the keyboard on the current node', () => {
    const document = createDocument();
    const tree = createTree({ document, label: 'Places' });
    assert.equal(tree.focus(), false);
    tree.setRoots([{ key: 'a', name: 'Area 31', children: [{ key: 'a1', name: 'Line 31.1' }] }, { key: 'b', name: 'Area 30' }]);
    assert.equal(tree.focus(), true);
    assert.equal(document.activeElement, tree.node.children[0]);
    tree.select('a1');
    tree.focus();
    assert.equal(document.activeElement.textContent, 'Line 31.1');
});

test('tree: path gives the nodes from the outermost one down to a node, also one that was never opened', () => {
    const { tree } = open();
    assert.deepEqual(tree.path('a1x').map((node) => node.key), ['a', 'a1', 'a1x']);
    assert.deepEqual(tree.path('b').map((node) => node.name), ['Weather']);
    assert.deepEqual(tree.path('nothing'), []);
    assert.deepEqual(tree.path('constructor'), []);
});

test('tree: a node can carry a state drawing with its words and a class of the page for its row', () => {
    const document = createDocument();
    const tree = createTree({ document, label: 'Plan' });
    tree.setRoots([
        { key: 'a', name: 'Made-up Thing', badge: { icon: 'plus', label: 'new', className: 'state-new' }, children: [] },
        { key: 'b', name: 'Left out', className: 'left-out', badge: { icon: 'minus', label: 'left out', className: 'state-out' }, children: [] },
        { key: 'c', name: 'Plain', children: [] },
    ]);
    const [first, second, third] = tree.node.querySelectorAll('li');
    const badge = first.querySelector('.badge');
    assert.ok(badge.classList.contains('state-new'));
    assert.deepEqual([badge.getAttribute('role'), badge.getAttribute('title'), badge.getAttribute('aria-label')], ['img', 'new', 'new']);
    assert.ok(badge.querySelector('.icon-plus'));
    assert.equal(first.classList.contains('left-out'), false);
    assert.ok(second.classList.contains('left-out'));
    assert.ok(second.querySelector('.icon-minus'));
    assert.equal(third.querySelector('.badge'), null);
});
