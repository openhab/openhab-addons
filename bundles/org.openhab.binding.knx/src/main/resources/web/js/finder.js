// The Find container's content: one box that looks through the whole project, group addresses, devices, group objects
// and rooms, by number or text. The lists are read when the box is first used. A result is a button; the caller
// goes to its place.

import { add, clear, el, icon } from './dom.js';
import { addressKey, deviceKey, spaceLook } from './model.js';
import { text } from './texts.js';
import { orderObjects } from './views/objects.js';

/** How many results are shown. */
export const FIND_RESULTS = 50;

function textOf(value) {
    return typeof value === 'string' ? value : '';
}

function listOf(value) {
    return Array.isArray(value) ? value.filter((item) => typeof item === 'object' && item !== null) : [];
}

/**
 * {@return everything of the project that can be found, each {kind, icon, label, words, view, sel, row?}}
 *
 * @param {Array<Object>} addresses the list of call 9
 * @param {Array<Object>} details every device with its group objects (call 11)
 * @param {Array<Object>} spaces the answer of call 15
 */
export function findIndex(addresses, details, spaces) {
    const index = [];
    const entry = (kind, drawing, label, view, sel, row) => index.push({ kind, icon: drawing, label, words: label.toLowerCase(), view, sel, row });
    for (const item of listOf(addresses)) {
        entry('address', 'address', [String(item.address), textOf(item.name)].filter((part) => part !== '').join(' '), 'group-addresses', addressKey(item.address));
    }
    for (const device of listOf(details)) {
        const name = [textOf(device.address), textOf(device.name) || String(device.id ?? '')].filter((part) => part !== '').join(' ');
        entry('device', 'device', name, 'topology', deviceKey(device));
        orderObjects(device.groupObjects).forEach(({ object }, at) => {
            const words = [Number.isInteger(object.number) ? String(object.number) : '', textOf(object.name), textOf(object.function)].filter((part) => part !== '').join(' ');
            if (words !== '') {
                entry('object', 'function', [name, words].join(' · '), 'topology', deviceKey(device), `object:${at}`);
            }
        });
    }
    const visit = (space) => {
        if (textOf(space.id) !== '' && textOf(space.name) !== '') {
            entry('room', spaceLook(space).icon, space.name, 'buildings', `space:${space.id}`);
        }
        listOf(space.spaces).forEach(visit);
    };
    listOf(spaces).forEach(visit);
    return index;
}

/**
 * @param {Object} options
 * @param {Document} options.document
 * @param {() => Promise<{addresses: Array, details: Array, spaces: Array, incomplete?: boolean}>} options.load reads
 *     the project's lists; incomplete when a part could not be read: the results say so, and the lists are read again
 *     when the box is next entered
 * @param {(result: Object) => void} options.onPick a result was chosen
 * @returns {{node: Element, field: Element, close: Function}}
 */
export function createFinder({ document, load, onPick }) {
    const field = el(document, 'input', { type: 'search', name: 'project-find', class: 'find', placeholder: text('find.placeholder'), 'aria-label': text('find.placeholder'), autocomplete: 'off' });
    const results = el(document, 'div', { class: 'find-results', 'aria-live': 'polite' });
    const node = el(document, 'div', { class: 'finder' }, field, results);
    const byButton = new Map();
    let index = null;
    let loading = null;
    let turn = 0;
    let closed = false;
    let incomplete = false;

    function draw(words) {
        clear(results);
        byButton.clear();
        if (words === '') {
            return;
        }
        add(document, results, incomplete ? el(document, 'p', { class: 'note incomplete' }, text('find.incomplete')) : null);
        const found = index.filter((entry) => entry.words.includes(words));
        if (found.length === 0) {
            add(document, results, el(document, 'p', { class: 'note' }, text('find.none')));
            return;
        }
        const list = el(document, 'ul', {});
        for (const entry of found.slice(0, FIND_RESULTS)) {
            const button = el(document, 'button', { type: 'button', class: 'found' }, icon(document, entry.icon), el(document, 'span', { class: 'kind' }, text(`find.${entry.kind}`)), el(document, 'span', { class: 'name' }, entry.label));
            byButton.set(button, entry);
            add(document, list, el(document, 'li', {}, button));
        }
        add(document, results, list, found.length > FIND_RESULTS ? el(document, 'p', { class: 'note' }, text('find.more', found.length - FIND_RESULTS)) : null);
    }

    async function find() {
        const mine = ++turn;
        const words = field.value.toLowerCase().trim();
        if (index === null && words !== '') {
            clear(results);
            add(document, results, el(document, 'p', { class: 'note' }, text('app.loading')));
            loading ??= Promise.resolve().then(load);
            let lists;
            try {
                lists = await loading;
            } catch {
                lists = null;
            }
            if (closed) {
                return;
            }
            if (lists === null) {
                // asked again with the next letter
                loading = null;
                if (mine === turn) {
                    clear(results);
                    add(document, results, el(document, 'p', { class: 'note' }, text('find.failed')));
                }
                return;
            }
            index = findIndex(lists.addresses, lists.details, lists.spaces);
            incomplete = lists.incomplete === true;
        }
        if (mine === turn && index !== null) {
            draw(words);
        }
    }

    const onInput = () => {
        find();
    };
    const onClick = (event) => {
        let target = event.target;
        while (target && target !== results && !byButton.has(target)) {
            target = target.parentNode;
        }
        const entry = byButton.get(target);
        if (entry) {
            onPick(entry);
        }
    };
    const onFocus = () => {
        if (incomplete) {
            index = null;
            loading = null;
            incomplete = false;
        }
    };
    field.addEventListener('input', onInput);
    field.addEventListener('focus', onFocus);
    results.addEventListener('click', onClick);

    return {
        node,
        field,
        close() {
            closed = true;
            field.removeEventListener('input', onInput);
            field.removeEventListener('focus', onFocus);
            results.removeEventListener('click', onClick);
        },
    };
}
