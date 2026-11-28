// The Live container's content: the newest telegrams of what is selected, a group address or a device, taken from
// the rows the page already holds from the monitor's stream. It only reads them; nothing is asked of the bus.

import { add, clear, el } from './dom.js';
import { clockText, dateText, parseTime, sourceNameOf } from './rows.js';
import { hasText, text } from './texts.js';

/** How many telegrams the container shows. */
export const LIVE_ROWS = 5;

function word(value) {
    return value !== undefined && value !== null && value !== '' ? String(value) : '';
}

function valueText(telegram) {
    if (word(telegram.value) !== '') {
        return word(telegram.unit) !== '' ? `${telegram.value} ${telegram.unit}` : String(telegram.value);
    }
    return '';
}

/**
 * {@return the newest telegrams that match, newest first}
 *
 * @param {Array<Object>} entries the rows of rows.entries(), oldest first
 * @param {{addresses?: Array<string>, source?: string}} watched group addresses a telegram goes to, and the individual
 *     address of a device it comes from; either is enough
 */
export function liveTelegrams(entries, watched, count = LIVE_ROWS) {
    const addresses = new Set(Array.isArray(watched?.addresses) ? watched.addresses : []);
    const source = word(watched?.source);
    const found = [];
    for (let at = entries.length - 1; at >= 0 && found.length < count; at--) {
        const telegram = entries[at].telegram;
        if (telegram && (addresses.has(telegram.destination) || (source !== '' && telegram.source === source))) {
            found.push(telegram);
        }
    }
    return found;
}

/**
 * @param {Object} options
 * @param {Document} options.document
 * @param [options.rows] the row model of the bridge
 * @param {(callback: Function) => *} [options.frame] bundles the drawing; without it every change draws at once
 * @returns {{node: Element, watch: Function, close: Function}}
 */
export function createLive({ document, rows, frame }) {
    const node = el(document, 'div', { class: 'live' });
    let watched = null;
    let pending = false;
    let closed = false;
    let drawn = null;

    function line(telegram) {
        const date = parseTime(telegram.time);
        const type = hasText(`type.${telegram.type}`) ? text(`type.${telegram.type}`) : word(telegram.type);
        const value = valueText(telegram);
        return el(
            document,
            'li',
            {},
            el(document, 'span', { class: 'clock', title: date ? dateText(date) : null }, date ? clockText(date) : word(telegram.time)),
            el(document, 'span', { class: 'address source', title: word(sourceNameOf(telegram)) || null }, word(telegram.source)),
            el(document, 'span', { class: 'address destination', title: word(telegram.destinationName) || null }, word(telegram.destination)),
            el(document, 'span', { class: 'type' }, type),
            value !== '' ? el(document, 'span', { class: 'value' }, value) : word(telegram.payload) !== '' ? el(document, 'span', { class: 'payload' }, word(telegram.payload)) : null,
        );
    }

    function draw() {
        pending = false;
        if (closed) {
            return;
        }
        if (watched === null) {
            drawn = null;
            clear(node);
            add(document, node, el(document, 'p', { class: 'note' }, text('live.none')));
            return;
        }
        const found = rows ? liveTelegrams(rows.entries(), watched) : [];
        const stamp = found.map((telegram) => `${telegram.number}:${telegram.outcome ?? ''}`).join(',');
        if (stamp === drawn) {
            return;
        }
        drawn = stamp;
        clear(node);
        add(document, node, found.length === 0 ? el(document, 'p', { class: 'note' }, text('live.empty')) : el(document, 'ul', { class: 'live-list' }, found.map(line)));
    }

    function request() {
        if (pending || closed) {
            return;
        }
        if (typeof frame !== 'function') {
            draw();
            return;
        }
        pending = true;
        frame(draw);
    }

    // nothing is drawn, and no frame asked for, while nothing is watched
    const leave = rows && typeof rows.subscribe === 'function' ? rows.subscribe((kind) => watched !== null && (kind === 'rows' || kind === 'clear') && request()) : null;
    draw();

    return {
        node,
        /** Shows the telegrams of these group addresses or of this source from now on; null shows none. */
        watch(next) {
            watched = next && (word(next.source) !== '' || (Array.isArray(next.addresses) && next.addresses.length > 0)) ? next : null;
            drawn = null;
            draw();
        },
        close() {
            closed = true;
            leave?.();
        },
    };
}
