import { test } from 'node:test';
import assert from 'node:assert/strict';

import { createDocument } from './fakedom.js';

// The stand-in must refuse what a browser refuses, or a view test would pass where the page breaks.

test('fakedom: no innerHTML, outerHTML, insertAdjacentHTML or document.write', () => {
    const document = createDocument();
    const p = document.createElement('p');
    assert.throws(() => {
        p.innerHTML = '<b>x</b>';
    });
    assert.throws(() => p.innerHTML);
    assert.throws(() => {
        p.outerHTML = '<b>x</b>';
    });
    assert.throws(() => p.insertAdjacentHTML('beforeend', '<b>x</b>'));
    assert.throws(() => document.write('<b>x</b>'));
});

test('fakedom: querySelector throws on an address as a selector, as a browser does', () => {
    const document = createDocument();
    const root = document.createElement('div');
    const cell = document.createElement('td');
    cell.setAttribute('id', 'last-value');
    cell.setAttribute('class', 'value live');
    root.appendChild(cell);
    assert.equal(root.querySelector('#last-value'), cell);
    assert.equal(root.querySelector('.live'), cell);
    assert.equal(root.querySelector('td'), cell);
    assert.equal(root.querySelector('#other'), null);
    for (const selector of ['#31/7/1', '#7.7.1', '.31/7/1', 'td.value', 'div td', '#1x', '[id=x]', '']) {
        assert.throws(() => root.querySelector(selector), SyntaxError, selector);
    }
});

test('fakedom: textContent, events that bubble, a disabled button and the listener count', () => {
    const document = createDocument();
    const list = document.createElement('ul');
    const button = document.createElement('button');
    list.appendChild(button);
    button.textContent = '<i>x</i>';
    assert.equal(button.childNodes.length, 1);
    assert.equal(list.textContent, '<i>x</i>');
    const seen = [];
    const onList = (event) => seen.push(`list ${event.target.tagName}`);
    list.addEventListener('click', onList);
    button.addEventListener('click', () => seen.push('button'));
    button.click();
    assert.deepEqual(seen, ['button', 'list BUTTON']);
    assert.equal(document.listenerCount(), 2);
    button.setAttribute('disabled', '');
    button.click();
    assert.equal(seen.length, 2);
    list.removeEventListener('click', onList);
    assert.equal(document.listenerCount(), 1);
});

test('fakedom: a field keeps what was typed apart from its value attribute', () => {
    const document = createDocument();
    const field = document.createElement('input');
    assert.equal(field.value, '');
    field.setAttribute('value', 'a');
    assert.equal(field.value, 'a');
    field.value = 'typed';
    assert.equal(field.value, 'typed');
    assert.equal(field.getAttribute('value'), 'a');
});

test('fakedom: a tick box keeps its own checked state; a click flips it and fires click, input and change', () => {
    const document = createDocument();
    const box = document.createElement('input');
    box.setAttribute('type', 'checkbox');
    assert.equal(box.checked, false);
    box.setAttribute('checked', '');
    assert.equal(box.checked, true);
    const seen = [];
    for (const type of ['click', 'input', 'change']) {
        box.addEventListener(type, () => seen.push(`${type} ${box.checked}`));
    }
    box.click();
    assert.deepEqual(seen, ['click false', 'input false', 'change false']);
    assert.equal(box.getAttribute('checked'), '');
    box.checked = true;
    assert.equal(box.checked, true);
    box.setAttribute('disabled', '');
    box.click();
    assert.equal(box.checked, true);
    assert.equal(seen.length, 3);
    const button = document.createElement('button');
    assert.equal(button.checked, undefined);
    const select = document.createElement('select');
    select.value = 'x';
    assert.equal(select.value, 'x');
});
