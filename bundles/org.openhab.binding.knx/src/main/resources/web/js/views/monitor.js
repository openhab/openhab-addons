// The Group Monitor panel: the telegrams of the bridge's stream, the newest at the bottom, with the counters of call
// 3, and in its bar Start, Stop, Clear and the page filter. The list follows its end unless the viewer scrolled up.
// Drawing is bundled per animation frame, and only the newest rows are in the document. Rows are tinted by their
// type. A selected row shows in Properties and is named in the address by its telegram's number. The panel only
// listens; what sends is the bar of views/monitorsend.js alone, and only where the bridge's setting allows it.

import { add, attr, clear, el, place } from '../dom.js';
import { projectPlaces } from '../model.js';
import { clockText, dateText, parseTime, sourceNameOf } from '../rows.js';
import { createTable } from '../table.js';
import { errorText, hasText, text } from '../texts.js';
import { createWorkplace } from '../workplace.js';
import { createSendBar } from './monitorsend.js';

/** The rows drawn at first, and added by each "Show more". */
export const PAGE_ROWS = 300;
/** How often the counters of call 3 are asked again. */
export const COUNTERS_MILLIS = 10000;

const TYPES = ['WRITE', 'READ', 'RESPONSE', 'UNKNOWN'];
const DIRECTIONS = ['IN', 'OUT'];
/** How near the end of the list, in px, still counts as being at the end. */
export const END_SLACK = 8;
// Stops that signing in again cures; every other stop offers "Try again".
const SIGN_IN_CODES = new Set(['signed-out', 'http-403']);

/** {@return the page's text for an enum value of the server, or the value itself} */
function named(group, value) {
    return hasText(`${group}.${value}`) ? text(`${group}.${value}`) : String(value ?? '');
}

function present(value) {
    return value !== undefined && value !== null && value !== '';
}

/** {@return the value with its unit, else a span with the payload in hex, else nothing} */
export function valueOf(document, telegram) {
    if (present(telegram.value)) {
        return present(telegram.unit) ? `${telegram.value} ${telegram.unit}` : String(telegram.value);
    }
    return present(telegram.payload) ? el(document, 'span', { class: 'payload' }, String(telegram.payload)) : null;
}

/** {@return the decoded value with its unit and the raw bytes, each when the telegram has it} */
export function infoOf(document, telegram) {
    const value = present(telegram.value) ? (present(telegram.unit) ? `${telegram.value} ${telegram.unit}` : String(telegram.value)) : null;
    return [value, present(telegram.payload) ? el(document, 'span', { class: 'payload' }, String(telegram.payload)) : null];
}

function securityParts(document, telegram) {
    return [
        named('security', telegram.security),
        present(telegram.reason) ? el(document, 'span', { class: 'reason' }, named('reason', telegram.reason)) : null,
        telegram.plainOnSecuredAddress === true ? el(document, 'span', { class: 'mark warn' }, text('monitor.plainOnSecured')) : null,
    ];
}

function outcomeOf(telegram) {
    return telegram.direction === 'OUT' && present(telegram.outcome) ? named('outcome', telegram.outcome) : '';
}

function timeOf(document, telegram) {
    const date = parseTime(telegram.time);
    return el(document, 'span', { class: 'clock', title: date ? dateText(date) : null }, date ? clockText(date) : String(telegram.time ?? ''));
}

function word(value) {
    return present(value) ? String(value) : '';
}

/** {@return the function the project gives the telegram's destination address, or nothing} */
export function buildingFunctionOf(places, telegram) {
    return places.functionOf.get(telegram.destination)?.name ?? '';
}

/** {@return the space of that function, else the room of the device the telegram comes from} */
export function buildingPartOf(places, telegram) {
    return places.functionOf.get(telegram.destination)?.part || places.roomOf.get(telegram.source) || '';
}

/**
 * The columns in the order of the group monitor ETS users know; Security and Outcome are the page's own. Flags, Prio
 * and Hop Count are left out: a telegram record does not hold them.
 *
 * @param {() => {functionOf: Map, roomOf: Map}} places where the project puts an address or a device
 * @param {() => number} placesVersion changes when the places were read
 */
function columnsOf(document, places, placesVersion) {
    // a lost or restart row holds no telegram
    const of = (read) => (entry) => (entry.telegram ? read(entry.telegram) : '');
    return [
        { key: 'count', label: text('column.count'), className: 'number', value: of((t) => t.number) },
        { key: 'time', label: text('column.time'), className: 'time', value: of((t) => word(t.time)), cell: of((t) => timeOf(document, t)) },
        { key: 'service', label: text('column.service'), className: 'service', value: of((t) => named('service', t.direction)) },
        { key: 'sourceAddress', label: text('column.sourceAddress'), className: 'source', value: of((t) => word(t.source)), cell: of((t) => el(document, 'span', { class: 'address' }, word(t.source))) },
        { key: 'sourceName', label: text('column.sourceName'), className: 'source-name', value: of((t) => word(sourceNameOf(t))) },
        {
            key: 'destinationAddress',
            label: text('column.destinationAddress'),
            className: 'destination',
            value: of((t) => word(t.destination)),
            cell: of((t) => el(document, 'span', { class: 'address' }, word(t.destination))),
        },
        { key: 'destinationName', label: text('column.destinationName'), className: 'destination-name', value: of((t) => word(t.destinationName)) },
        { key: 'buildingFunction', label: text('column.buildingFunction'), className: 'building-function', stamp: placesVersion, value: of((t) => buildingFunctionOf(places(), t)) },
        { key: 'buildingPart', label: text('column.buildingPart'), className: 'building-part', stamp: placesVersion, value: of((t) => buildingPartOf(places(), t)) },
        { key: 'type', label: text('column.type'), className: 'type', value: of((t) => named('type', t.type)) },
        { key: 'dpt', label: text('column.dpt'), className: 'dpt', value: of((t) => word(t.dataType)) },
        { key: 'info', label: text('column.info'), className: 'value', value: of((t) => [word(t.value), word(t.unit), word(t.payload)].join(' ')), cell: of((t) => infoOf(document, t)) },
        { key: 'security', label: text('column.security'), className: 'security', value: of((t) => named('security', t.security)), cell: of((t) => securityParts(document, t)) },
        { key: 'outcome', label: text('column.outcome'), className: 'outcome', value: of(outcomeOf) },
    ];
}

function markerRow(document, entry, columns) {
    const words = entry.marker === 'lost' ? (entry.count === 1 ? text('monitor.lostOne') : text('monitor.lost', entry.count)) : text('monitor.restarted');
    return el(document, 'tr', { class: 'marker' }, el(document, 'td', { colspan: columns }, words));
}

const TINTS = Object.freeze({ WRITE: 'type-write', READ: 'type-read', RESPONSE: 'type-response' });

function rowClass(entry) {
    const telegram = entry.telegram;
    return [Object.hasOwn(TINTS, telegram.type) ? TINTS[telegram.type] : null, telegram.direction === 'OUT' ? 'own' : null, telegram.security === 'NOT_DECRYPTED' ? 'not-decrypted' : null].filter(Boolean).join(' ') || null;
}

function count(value) {
    return Number.isFinite(value) ? value : 0;
}

/** @returns {{close: () => void, focus: () => void}} */
export function show(context) {
    const { document, element, api, rows, stream, bridge, route, navigate, frame, timers, confirm, showError } = context;
    const workplace = createWorkplace({ document, element, layout: context.layout, title: text('view.monitor'), tree: false, context });
    if (!rows) {
        return {
            focus() {},
            close() {
                workplace.close();
            },
        };
    }

    let closed = false;
    let pending = false;
    let forced = true;
    let limit = PAGE_ROWS;
    let loading = false;
    let pollFailed = false;
    let following = true;
    let shownVersion = null;
    let wantedRow = typeof route?.query?.row === 'string' ? route.query.row : '';
    let places = projectPlaces([], []);
    let placesVersion = 0;
    // the address of the page as the panel last set it: "Try again" shows this one again
    let shownRoute = route;

    const counters = el(document, 'p', { class: 'counters' });
    const stopped = el(document, 'p', { class: 'stopped', role: 'status' });
    const retryButton = el(document, 'button', { type: 'button', class: 'retry' }, text('monitor.tryAgain'));
    const filterField = el(document, 'input', {
        type: 'search',
        name: 'filter',
        class: 'find',
        placeholder: text('monitor.filter'),
        'aria-label': text('monitor.filter'),
        autocomplete: 'off',
    });
    const option = (value, words) => el(document, 'option', { value }, words);
    const typeField = el(
        document,
        'select',
        { name: 'type', 'aria-label': text('monitor.typeFilter') },
        option('', text('monitor.allTypes')),
        TYPES.map((type) => option(type, named('type', type))),
    );
    const dirField = el(
        document,
        'select',
        { name: 'dir', 'aria-label': text('monitor.dirFilter') },
        option('', text('monitor.allDirections')),
        DIRECTIONS.map((dir) => option(dir, named('service', dir))),
    );
    const startButton = el(document, 'button', { type: 'button', class: 'start' }, text('monitor.goOn'));
    const pauseButton = el(document, 'button', { type: 'button', class: 'pause', title: text('monitor.pauseTitle') }, text('monitor.pause'));
    const fresh = el(document, 'span', { class: 'fresh', 'aria-live': 'polite' });
    const clearButton = el(document, 'button', { type: 'button', class: 'clear' }, text('monitor.clear'));
    const more = el(document, 'button', { type: 'button', class: 'more' }, text('monitor.more'));
    const columns = columnsOf(document, () => places, () => placesVersion);
    const table = createTable({
        document,
        label: text('view.monitor'),
        columns,
        cap: Infinity,
        keyOf: (entry) => entry.key,
        versionOf: (entry) => entry.version,
        rowNode: (entry) => (entry.marker ? markerRow(document, entry, columns.length) : null),
        rowClass,
        onSelect: (entry) => {
            wantedRow = '';
            showTelegram(entry);
            const query = { ...route?.query };
            delete query.row;
            if (entry?.telegram) {
                query.row = String(entry.telegram.number);
            }
            shownRoute = { view: route.view, bridge: route.bridge, query };
            context.replace?.(shownRoute);
        },
        layout: context.layout,
        name: 'monitor',
    });
    workplace.setCrumbs([{ key: '', label: text('view.monitor') }]);
    const list = workplace.list;
    list.classList.add('telegrams');
    const sendBar = createSendBar({ context, selected: () => table.selected()?.telegram ?? null });
    add(document, workplace.notes, counters, stopped, sendBar.node);
    add(document, workplace.tools, startButton, pauseButton, fresh, clearButton, typeField, dirField);
    add(document, workplace.find, filterField);
    add(document, list, table.node);

    const kept = rows.filter();
    filterField.value = kept.text;
    typeField.value = kept.type;
    dirField.value = kept.dir;

    function showTelegram(entry) {
        const telegram = entry?.telegram;
        shownVersion = entry ? entry.version : null;
        if (!telegram) {
            workplace.properties.empty();
            workplace.live.watch(null);
            return;
        }
        workplace.live.watch({ addresses: word(telegram.destination) === '' ? [] : [telegram.destination] });
        const date = parseTime(telegram.time);
        workplace.properties.show(`${text('column.count')} ${telegram.number}`, [
            {
                tab: 'information',
                rows: [
                    [text('column.count'), String(telegram.number)],
                    [text('column.time'), date ? dateText(date) : word(telegram.time)],
                    [text('column.service'), named('service', telegram.direction)],
                    [text('column.sourceAddress'), word(telegram.source)],
                    [text('column.sourceName'), word(sourceNameOf(telegram))],
                    [text('column.destinationAddress'), word(telegram.destination)],
                    [text('column.destinationName'), word(telegram.destinationName)],
                    [text('column.buildingFunction'), buildingFunctionOf(places, telegram)],
                    [text('column.buildingPart'), buildingPartOf(places, telegram)],
                    [text('column.type'), named('type', telegram.type)],
                    [text('column.dpt'), word(telegram.dataType)],
                    [text('column.value'), present(telegram.value) ? (present(telegram.unit) ? `${telegram.value} ${telegram.unit}` : String(telegram.value)) : null],
                    [text('column.payload'), present(telegram.payload) ? el(document, 'span', { class: 'payload' }, String(telegram.payload)) : null],
                    [text('column.security'), el(document, 'span', {}, securityParts(document, telegram))],
                    [text('column.outcome'), outcomeOf(telegram)],
                ],
            },
        ]);
    }

    function drawPause() {
        const paused = rows.paused();
        // the button that would do nothing gives way; the focus on it goes to the other one
        const focused = document.activeElement;
        attr(startButton, 'disabled', !paused);
        attr(pauseButton, 'disabled', paused);
        if (focused === startButton && !paused) {
            pauseButton.focus();
        } else if (focused === pauseButton && paused) {
            startButton.focus();
        }
        clear(fresh);
        if (paused && rows.newCount() > 0) {
            add(document, fresh, text('monitor.new', rows.newCount()));
        }
    }

    function atEnd() {
        return (Number(list.scrollHeight) || 0) - (Number(list.scrollTop) || 0) - (Number(list.clientHeight) || 0) <= END_SLACK;
    }

    function drawTable() {
        const visible = rows.visible();
        // the newest rows, the oldest of them on top
        const shown = (visible.length > limit ? visible.slice(0, limit) : [...visible]).reverse();
        table.setRows(shown, { none: rows.held() === 0 ? text('monitor.empty') : text('monitor.noMatch') });
        const hadFocus = document.activeElement === more;
        place(list, visible.length > limit ? [more, table.node] : [table.node]);
        // the last batch takes the button away from under the focus
        if (hadFocus && document.activeElement !== more && !table.focus()) {
            workplace.focusList();
        }
        // sorted, the newest row is not at the end
        if (following && table.sorted() === null) {
            list.scrollTop = Number(list.scrollHeight) || 0;
        }
        if (wantedRow !== '') {
            // the row the address names, once its telegram is among the rows
            const wanted = shown.find((entry) => entry.telegram && String(entry.telegram.number) === wantedRow);
            if (wanted) {
                wantedRow = '';
                table.select(wanted.key);
            }
        }
        const selected = table.selected();
        if (selected && selected.version !== shownVersion) {
            showTelegram(selected);
        }
        context.status?.({ selection: text('view.monitor'), shown: shown.filter((entry) => entry.telegram).length, total: rows.entries().filter((entry) => entry.telegram).length });
    }

    function draw() {
        pending = false;
        if (closed) {
            return;
        }
        const force = forced;
        forced = false;
        drawPause();
        if (force || !rows.paused()) {
            drawTable();
        }
    }

    function request(force) {
        forced = forced || force;
        if (!pending && !closed) {
            pending = true;
            frame(draw);
        }
    }

    function drawStopped() {
        clear(stopped);
        const state = stream?.state?.();
        if (state?.phase !== 'stopped') {
            attr(retryButton, 'disabled', false);
        }
        if (state?.phase === 'stopped' && state.code !== 'signed-out') {
            add(document, stopped, errorText(state.code));
            if (!SIGN_IN_CODES.has(state.code)) {
                add(document, stopped, ' ', retryButton);
            }
        }
    }

    function drawCounters(monitor) {
        clear(counters);
        const reasons = Object.entries(monitor.notDecrypted ?? {}).map(([reason, n]) => `${named('reason', reason)} ${count(n)}`);
        if (count(monitor.notDecryptedOtherReason) > 0) {
            reasons.push(text('monitor.otherReason', count(monitor.notDecryptedOtherReason)));
        }
        const notDecrypted =
            Object.values(monitor.notDecrypted ?? {}).reduce((sum, n) => sum + count(n), 0) + count(monitor.notDecryptedOtherReason);
        const parts = [
            text('monitor.received', count(monitor.received)),
            text('monitor.sent', count(monitor.sent)),
            reasons.length > 0 ? text('monitor.notDecryptedWhy', notDecrypted, reasons.join(', ')) : text('monitor.notDecrypted', notDecrypted),
            text('monitor.plainOnSecuredCount', count(monitor.plainOnSecuredAddress)),
            present(monitor.connection) ? named('connection', monitor.connection) : null,
            present(monitor.ownAddress) ? text('monitor.ownAddress', monitor.ownAddress) : null,
        ];
        for (const part of parts.filter((value) => value !== null)) {
            add(document, counters, el(document, 'span', { class: 'counter' }, part));
        }
    }

    async function loadCounters() {
        if (loading || closed) {
            return;
        }
        loading = true;
        try {
            const monitor = await api.monitor(bridge.uid);
            if (closed) {
                return;
            }
            drawCounters(monitor ?? {});
            if (pollFailed) {
                pollFailed = false;
                showError(null);
            }
        } catch (error) {
            if (!closed) {
                pollFailed = true;
                showError(error);
            }
        } finally {
            loading = false;
        }
    }

    async function clearAll() {
        if (!confirm(text('monitor.clearConfirm'))) {
            return;
        }
        try {
            await api.clearTelegrams(bridge.uid);
            rows.clear();
        } catch (error) {
            if (!closed) {
                showError(error);
            }
        }
    }

    const onScroll = () => {
        following = atEnd();
    };
    const onClick = (event) => {
        const target = event.target;
        if (target === pauseButton) {
            rows.pause();
        } else if (target === startButton) {
            rows.resume();
        } else if (target === clearButton) {
            clearAll();
        } else if (target === more) {
            limit += PAGE_ROWS;
            request(true);
        } else if (target === retryButton) {
            attr(retryButton, 'disabled', true);
            navigate(shownRoute);
        }
    };
    const onInput = (event) => {
        if (event.target === filterField) {
            rows.setFilter({ text: filterField.value });
        }
    };
    const onChange = (event) => {
        if (event.target === typeField) {
            rows.setFilter({ type: typeField.value });
        } else if (event.target === dirField) {
            rows.setFilter({ dir: dirField.value });
        } else if (event.target === filterField) {
            rows.setFilter({ text: filterField.value });
        }
    };
    element.addEventListener('click', onClick);
    element.addEventListener('input', onInput);
    element.addEventListener('change', onChange);
    list.addEventListener('scroll', onScroll);

    const leaveRows = rows.subscribe((kind) => {
        if (kind === 'filter') {
            limit = PAGE_ROWS;
        }
        if (kind !== 'last') {
            request(kind !== 'rows');
        }
    });
    const leaveStream = typeof stream?.subscribe === 'function' ? stream.subscribe(drawStopped) : null;
    const poll = timers.setInterval(loadCounters, COUNTERS_MILLIS);

    /** Where the project puts addresses and devices, from the lists the row model keeps; without a project the two building columns stay empty. */
    async function loadPlaces() {
        if (bridge.projectState !== 'LOADED') {
            return;
        }
        const [spaces, devices] = await Promise.allSettled([rows.locations(), rows.devices()]);
        if (closed || (spaces.status === 'rejected' && devices.status === 'rejected')) {
            return;
        }
        places = projectPlaces(spaces.status === 'fulfilled' ? spaces.value : [], devices.status === 'fulfilled' ? devices.value : []);
        placesVersion++;
        table.refresh();
    }

    drawStopped();
    request(true);
    loadCounters();
    loadPlaces();
    stream?.start();

    return {
        /** The panel was opened from another one by key or click: the keyboard goes on in its list. */
        focus() {
            if (!table.focus()) {
                workplace.focusList();
            }
        },
        close() {
            closed = true;
            timers.clearInterval(poll);
            leaveRows();
            leaveStream?.();
            element.removeEventListener('click', onClick);
            element.removeEventListener('input', onInput);
            element.removeEventListener('change', onChange);
            list.removeEventListener('scroll', onScroll);
            sendBar.close();
            table.close();
            workplace.close();
        },
    };
}
