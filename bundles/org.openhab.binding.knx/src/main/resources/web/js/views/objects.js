// What the panels share about group objects: their order, their flags as letter columns, their size, the Group
// Objects list of one device or of several (the sending address under "Linked with", the others under "Other
// Linked", each with its name from call 9 and its live last value), and "Copy".

import { add, clear, el, mainUiLink } from '../dom.js';
import { objectInOpenhab } from '../model.js';
import { addressOrder } from '../rows.js';
import { createTable } from '../table.js';
import { errorText, hasText, text } from '../texts.js';
import { valueOf } from './monitor.js';

/** The flags in the order of their letters: text key of the letter, field of GroupObjectDTO, text key of its name. */
export const FLAGS = Object.freeze([
    ['flag.letter.communication', 'communication', 'flag.communication'],
    ['flag.letter.read', 'read', 'flag.read'],
    ['flag.letter.write', 'write', 'flag.write'],
    ['flag.letter.transmit', 'transmit', 'flag.transmit'],
    ['flag.letter.update', 'update', 'flag.update'],
    ['flag.letter.readOnInit', 'readOnInit', 'flag.readOnInit'],
]);

/** The flags that always have a column of their own: C R W T U. Read on init gets one when an object has it set. */
export const LIST_FLAGS = FLAGS.slice(0, 5);

/** Above this many objects the list of several devices starts with every device folded. */
export const FOLD_ABOVE = 500;

function addressesOf(object) {
    return Array.isArray(object.groupAddresses) ? object.groupAddresses.filter((address) => typeof address === 'string') : [];
}

function numberOf(object) {
    return Number.isInteger(object.number) ? object.number : null;
}

function textOf(value) {
    return typeof value === 'string' ? value : '';
}

/**
 * The objects in the order of the page: by number, an object without a number last; of objects with the same number
 * the linked ones first, and the unlinked ones marked unused when a linked one shares the number. Otherwise the
 * reader's order is kept.
 *
 * @returns {Array<{object: Object, unused: boolean}>}
 */
export function orderObjects(objects) {
    const list = (Array.isArray(objects) ? objects : [])
        .filter((object) => typeof object === 'object' && object !== null)
        .map((object, at) => ({ object, at, number: numberOf(object), linked: addressesOf(object).length > 0 }));
    const linkedNumbers = new Set(list.filter((entry) => entry.number !== null && entry.linked).map((entry) => entry.number));
    list.sort((a, b) => {
        if (a.number !== b.number) {
            if (a.number === null || b.number === null) {
                return a.number === null ? 1 : -1;
            }
            return a.number - b.number;
        }
        if (a.number !== null && a.linked !== b.linked) {
            return a.linked ? -1 : 1;
        }
        return a.at - b.at;
    });
    return list.map((entry) => ({ object: entry.object, unused: entry.number !== null && !entry.linked && linkedNumbers.has(entry.number) }));
}

/** {@return the object's group addresses, the sending one first} */
export function linkOrder(object) {
    const addresses = addressesOf(object);
    const sending = typeof object.sendingGroupAddress === 'string' ? object.sendingGroupAddress : null;
    if (sending === null) {
        return addresses;
    }
    return [sending, ...addresses.filter((address) => address !== sending)];
}

/** {@return the size of an object as the page shows it} */
export function sizeText(bits) {
    if (!Number.isInteger(bits) || bits <= 0) {
        return '';
    }
    if (bits % 8 === 0) {
        return bits === 8 ? text('device.byte') : text('device.bytes', bits / 8);
    }
    return bits === 1 ? text('device.bit') : text('device.bits', bits);
}

// The size of the main types whose every sub type has the same one.
const DPT_BITS = Object.freeze({ 1: 1, 2: 2, 3: 4, 4: 8, 5: 8, 6: 8, 7: 16, 8: 16, 9: 16, 10: 24, 11: 24, 12: 32, 13: 32, 14: 32, 15: 32, 16: 112, 17: 8, 18: 8, 19: 64, 20: 8, 29: 64, 232: 24 });

/** {@return the size in bits a data type such as "9.001" or "9" gives, 0 when it gives none} */
export function dptBits(dataType) {
    const match = /^(\d+)(\.\d+)?$/.exec(typeof dataType === 'string' ? dataType : '');
    return match && Object.hasOwn(DPT_BITS, match[1]) ? DPT_BITS[match[1]] : 0;
}

/**
 * Copies the text of a read-only field: with the clipboard when there is one, otherwise (or when it fails) by
 * selecting the field and asking the document to copy. When both fail the text stays selected.
 *
 * @param {Document} document
 * @param {{writeText: Function}|undefined} clipboard
 * @param {HTMLTextAreaElement|HTMLInputElement} field
 * @returns {Promise<boolean>} true when the text was copied
 */
export async function copyText(document, clipboard, field) {
    if (clipboard && typeof clipboard.writeText === 'function') {
        try {
            await clipboard.writeText(field.value);
            return true;
        } catch {
            // the fallback below
        }
    }
    field.focus();
    field.select();
    try {
        return typeof document.execCommand === 'function' && document.execCommand('copy') === true;
    } catch {
        return false;
    }
}

/** {@return the flag's letter when it is set, a dash when it is not; both say which in their title} */
export function flagMark(document, set, nameKey, letterKey) {
    const title = text(set ? 'device.flagSet' : 'device.flagNotSet', text(nameKey));
    return el(document, 'span', { class: set ? 'flag on' : 'flag off', title, 'aria-label': title }, set ? text(letterKey) : text('flag.none'));
}

/**
 * @param {(row: Object) => Object} objectOf the group object of a row
 * @param {boolean} [withInit] also the column I, after U
 * @returns {Array<Object>} the columns C R W T U of a list of group objects
 */
export function flagColumns(document, objectOf, withInit = false) {
    return (withInit ? FLAGS : LIST_FLAGS).map(([letter, field, key]) => ({
        key: field,
        label: text(letter),
        title: text(key),
        className: 'flag-cell',
        find: false,
        value: (row) => (objectOf(row)[field] === true ? 1 : 0),
        cell: (row) => flagMark(document, objectOf(row)[field] === true, key, letter),
    }));
}

/**
 * {@return what Properties shows for a group object: its fields and its flags as read-only tick boxes under
 * Settings, its description under Comments}
 */
export function objectSections(object) {
    const [linked, ...others] = linkOrder(object);
    return [
        {
            heading: text('device.object'),
            rows: [
                [text('column.number'), numberOf(object) === null ? null : String(numberOf(object))],
                [text('column.name'), textOf(object.name)],
                [text('column.function'), textOf(object.function)],
                [text('column.dataType'), textOf(object.dataType)],
                [text('column.length'), sizeText(object.sizeBits)],
                [text('column.linkedWith'), linked ?? null],
                [text('column.otherLinked'), others.join(', ')],
            ],
        },
        {
            heading: text('column.flags'),
            // Read on init is no flag of the list's five: it shows only when it is set
            ticks: (object.readOnInit === true ? FLAGS : LIST_FLAGS).map(([, field, key]) => [text(key), object[field] === true]),
        },
        { tab: 'comments', heading: text('device.object'), rows: [[text('column.description'), object.text !== object.name ? textOf(object.text) : '']] },
    ];
}

/**
 * The Group Objects list: of one device, or of several with a heading row per device.
 *
 * @param {Object} options
 * @param {Document} options.document
 * @param [options.rows] the row model, for the last values
 * @param {boolean} [options.grouped] the objects of several devices, grouped under a heading row per device
 * @param {boolean} [options.withOpenhab] the column that says whether a channel uses one of the object's addresses
 * @param {(row: {object: Object, unused: boolean}|null) => void} [options.onSelect]
 * @param {(row: Object) => void} [options.onOpen]
 * @param {(address: string) => void} [options.onAddress] a double click on an address under "Linked with" or "Other
 *     Linked"
 * @param {() => void} [options.onDraw]
 * @param {{get: Function, set: Function}} [options.layout]
 * @returns {{table: Object, setObjects: Function, setDevices: Function, setNames: Function, refresh: Function}}
 */
export function createObjectsTable({ document, rows, grouped = false, withOpenhab = false, onSelect, onOpen, onAddress, onDraw, layout }) {
    const book = new Map();
    const linkNodes = new WeakMap();
    let namesVersion = 0;
    let shownDevices = '';
    let listed = [];

    function lastStamp(address) {
        const last = rows?.last(address);
        return last ? `${last.number}:${last.outcome ?? ''}` : '';
    }

    function linkParts(address) {
        const last = rows?.last(address) ?? null;
        const name = textOf(book.get(address)?.name);
        const parts = [
            el(document, 'span', { class: 'address' }, address),
            name !== '' ? el(document, 'span', { class: 'name' }, name) : null,
            last ? el(document, 'span', { class: 'value' }, valueOf(document, last)) : null,
        ].filter(Boolean);
        for (const part of parts) {
            linkNodes.set(part, address);
        }
        return parts;
    }

    /** {@return the address a double click hit: the one under the pointer, in "Linked with" also the cell's own} */
    function addressAt(row, target) {
        for (let at = target; at && at.tagName !== 'TR'; at = at.parentNode) {
            if (linkNodes.has(at)) {
                return linkNodes.get(at);
            }
            if (at.tagName === 'TD') {
                return at.classList.contains('linked') ? linkedOf(row) : '';
            }
        }
        return '';
    }

    const linkedOf = (row) => linkOrder(row.object)[0] ?? '';
    const othersOf = (row) => linkOrder(row.object).slice(1);
    const inOpenhab = (row) => objectInOpenhab(row.object, book);

    const columnsFor = (withInit) =>
        [
            {
                key: 'number',
                label: text('column.number'),
                className: 'number',
                value: (row) => numberOf(row.object) ?? Infinity,
                cell: (row) => [
                    numberOf(row.object) === null ? text('app.none') : String(numberOf(row.object)),
                    row.unused ? el(document, 'span', { class: 'mark' }, text('device.unused')) : null,
                ],
            },
            { key: 'name', label: text('column.name'), value: (row) => textOf(row.object.name) },
            { key: 'function', label: text('column.function'), value: (row) => textOf(row.object.function) },
            {
                key: 'linkedWith',
                label: text('column.linkedWith'),
                className: 'linked',
                value: (row) => [linkedOf(row), textOf(book.get(linkedOf(row))?.name)].join(' ').trim(),
                compare: (a, b) => addressOrder(linkedOf(a), linkedOf(b)),
                stamp: (row) => `${namesVersion}|${lastStamp(linkedOf(row))}`,
                cell: (row) => (linkedOf(row) === '' ? null : el(document, 'span', { class: 'link' }, linkParts(linkedOf(row)))),
            },
            {
                key: 'otherLinked',
                label: text('column.otherLinked'),
                className: 'addresses',
                value: (row) => othersOf(row).join(' '),
                stamp: (row) => `${namesVersion}|${othersOf(row).map(lastStamp).join(',')}`,
                cell: (row) => (othersOf(row).length === 0 ? null : el(document, 'ul', { class: 'links' }, othersOf(row).map((address) => el(document, 'li', {}, linkParts(address))))),
            },
            { key: 'length', label: text('column.length'), value: (row) => (Number.isInteger(row.object.sizeBits) ? row.object.sizeBits : 0), cell: (row) => sizeText(row.object.sizeBits) },
            ...flagColumns(document, (row) => row.object, withInit),
            { key: 'dataType', label: text('column.dataType'), className: 'type', value: (row) => textOf(row.object.dataType) },
            withOpenhab
                ? {
                      key: 'openhab',
                      label: text('column.openhab'),
                      className: 'configured',
                      find: false,
                      value: (row) => (inOpenhab(row) ? 1 : 0),
                      stamp: () => namesVersion,
                      cell: (row) => (inOpenhab(row) ? text('addresses.configured') : null),
                  }
                : null,
        ].filter(Boolean);
    let withInit = false;
    const table = createTable({
        document,
        label: text('device.objects'),
        keyOf: (row) => row.key,
        rowClass: (row) => (row.unused ? 'unused' : null),
        groupOf: grouped ? (row) => row.group : undefined,
        onSelect,
        onOpen: (row, event) => {
            const address = onAddress && event ? addressAt(row, event.target) : '';
            if (address !== '') {
                onAddress(address);
            } else {
                onOpen?.(row);
            }
        },
        onDraw,
        layout,
        name: grouped ? 'groupedObjects' : 'objects',
        columns: columnsFor(false),
    });

    function show(list, init, none) {
        if (init !== withInit) {
            withInit = init;
            table.setColumns(columnsFor(init));
        }
        table.setRows(list, { none: text(none), noMatch: text('devices.noMatch') });
    }

    return {
        table,
        /** Shows the objects of one device; a row's key is its place in the list. */
        setObjects(objects) {
            show(
                orderObjects(objects).map((entry, at) => ({ ...entry, at, key: at })),
                Array.isArray(objects) && objects.some((object) => object?.readOnInit === true),
                'device.noObjects',
            );
        },
        /**
         * Shows the objects of these devices (call 11), each device's under a heading row with its address and name.
         *
         * @param {boolean} [loading] the devices are still being read
         * @param {string} [wanted] the key of the row the address names
         */
        setDevices(details, loading = false, wanted = '') {
            const list = [];
            let init = false;
            for (const device of Array.isArray(details) ? details : []) {
                const id = String(device.id ?? device.address);
                const label = [textOf(device.address), textOf(device.name) || String(device.id ?? '')].filter((part) => part !== '').join(' ');
                const group = { key: id, label };
                const objects = Array.isArray(device.groupObjects) ? device.groupObjects : [];
                init = init || objects.some((object) => object?.readOnInit === true);
                orderObjects(objects).forEach((entry, at) => list.push({ ...entry, at, key: `object:${id}|${at}`, device, group }));
            }
            // a long list starts folded, once for each set of devices: what the viewer unfolds then stays open
            const ids = [...new Set(list.map((row) => row.group.key))];
            if (ids.length > 0 && ids.join('\n') !== shownDevices) {
                shownDevices = ids.join('\n');
                // not the device of the row the address names: that row is to be seen
                const open = list.find((row) => row.key === wanted)?.group.key;
                table.setFolded(list.length > FOLD_ABOVE ? ids.filter((id) => id !== open) : [], false);
            }
            listed = list;
            show(list, init, loading ? 'app.loading' : 'device.noObjectsHere');
        },
        /** Unfolds the device of the row with this key, so that the row can be seen. */
        reveal(key) {
            const group = listed.find((row) => row.key === key)?.group.key;
            if (group !== undefined && table.folded().includes(group)) {
                table.setFolded(table.folded().filter((folded) => folded !== group));
            }
        },
        /** Takes the names, and whether a channel uses them, from the list of call 9. */
        setNames(list) {
            book.clear();
            for (const item of Array.isArray(list) ? list : []) {
                if (typeof item?.address === 'string') {
                    book.set(item.address, item);
                }
            }
            namesVersion++;
            table.refresh();
        },
        refresh() {
            table.refresh();
        },
    };
}

/** How many group addresses are asked at a time for the channels that hold them (call 23). */
export const USES_AT_ONCE = 4;

/**
 * Reads the channels that hold each of these group addresses, a few at a time.
 *
 * @param {(address: string) => Promise<Object>} read call 23 for one address
 * @param {Array<string>} addresses
 * @param {(address: string, answer: {uses: Array<Object>}|{error: *}) => void} done told for each address as it comes
 * @returns {Promise<void>} settled when every address was told
 */
export async function readUses(read, addresses, done) {
    let next = 0;
    const worker = async () => {
        while (next < addresses.length) {
            const address = addresses[next++];
            try {
                const detail = await read(address);
                done(address, { uses: Array.isArray(detail?.usedBy) ? detail.usedBy.filter((use) => typeof use === 'object' && use !== null) : [] });
            } catch (error) {
                done(address, { error });
            }
        }
    };
    await Promise.all(Array.from({ length: Math.min(USES_AT_ONCE, addresses.length) }, worker));
}

/** {@return the root of openHAB, where Main UI stands, from the REST root; undefined when that is not known} */
export function mainUiRoot(base) {
    return base instanceof URL ? new URL('../', base) : undefined;
}

/** {@return the role of an address in a channel in words: main, status or listening address} */
export function roleText(role) {
    return hasText(`use.role.${role}`) ? text(`use.role.${role}`) : textOf(role);
}

/** {@return the Thing of a use as a link to its page in Main UI} */
export function thingLink(document, root, use) {
    return mainUiLink(document, root, 'thing', textOf(use.thingUID), textOf(use.thingLabel) || textOf(use.thingUID));
}

/** {@return the Items of a use, each a link to its page in Main UI; nothing when it has none} */
export function itemLinks(document, root, use) {
    const names = Array.isArray(use.items) ? use.items.filter((name) => typeof name === 'string') : [];
    return names.flatMap((name, at) => (at > 0 ? [', ', mainUiLink(document, root, 'item', name, name)] : [mainUiLink(document, root, 'item', name, name)]));
}

/**
 * What the openHAB container tells about the channels that hold a group address: per channel its Thing, the channel
 * with its type, the role of the address in it and the Items linked to it.
 *
 * @param {URL|undefined} root the root of openHAB
 * @param {{uses: Array<Object>}|{error: *}|undefined} answer of readUses; undefined while it is read
 * @returns {Array<Element>}
 */
export function usesBlock(document, root, answer) {
    const heading = el(document, 'h5', {}, text('use.heading'));
    if (answer === undefined) {
        return [heading, el(document, 'p', { class: 'note uses-state' }, text('app.loading'))];
    }
    if (answer.error !== undefined) {
        return [heading, el(document, 'p', { class: 'note uses-state' }, errorText(answer.error?.code, answer.error?.text))];
    }
    if (answer.uses.length === 0) {
        return [heading, el(document, 'p', { class: 'note uses-state' }, text('use.none'))];
    }
    return [
        heading,
        el(
            document,
            'ul',
            { class: 'uses' },
            answer.uses.map((use) => {
                const items = itemLinks(document, root, use);
                return el(
                    document,
                    'li',
                    {},
                    el(document, 'span', { class: 'use-thing' }, thingLink(document, root, use)),
                    el(document, 'span', { class: 'use-channel' }, [textOf(use.channelLabel) || textOf(use.channelId), textOf(use.channelType), roleText(use.role)].filter((part) => part !== '').join(' · ')),
                    el(document, 'span', { class: 'use-items' }, items.length > 0 ? [text('use.items'), ' ', ...items] : text('use.noItems')),
                );
            }),
        ),
    ];
}

/**
 * A read-only text with "Copy" and what came of it. The button copies wherever the box stands: the sidebar that two
 * panels share is outside the panel that fills it.
 *
 * @param {{writeText: Function}|undefined} clipboard
 */
export function createCopyBox(document, name, label, clipboard) {
    const field = el(document, 'textarea', { name, readonly: true, 'aria-label': label });
    const button = el(document, 'button', { type: 'button', class: 'copy' }, text('device.copy'));
    const state = el(document, 'span', { class: 'copied', 'aria-live': 'polite' });
    let closed = false;
    const onClick = async () => {
        clear(state);
        const copied = await copyText(document, clipboard, field);
        if (!closed) {
            add(document, state, text(copied ? 'device.copied' : 'device.pressCopy'));
        }
    };
    button.addEventListener('click', onClick);
    return {
        field,
        state,
        tools: el(document, 'div', { class: 'tools' }, button, state),
        close() {
            closed = true;
            button.removeEventListener('click', onClick);
        },
    };
}
