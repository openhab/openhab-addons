// The Group Addresses panel. With a loaded project: the tree of ranges and addresses of call 9 with its dynamic
// folders, the list of what is under the selected range or in the selected folder with each address's last value kept
// live from the stream, and for one selected address its Associations, the group objects linked to it (from call 11
// of every device, read once per bridge). Without a project: the addresses seen on the bus (call 6).

import { add, clear, el, icon, place } from '../dom.js';
import { addressBook, addressInOpenhab, addressKey, addressesUnder, associations, deviceInOpenhab, folderGroup, folderNode, groupTree, rangeByNames } from '../model.js';
import { href, selection } from '../router.js';
import { addressOrder, clockText, dateText, parseTime } from '../rows.js';
import { ROWS_CAP, createTable } from '../table.js';
import { errorText, text } from '../texts.js';
import { createTree, treeMemory } from '../tree.js';
import { createWorkplace } from '../workplace.js';
import { deviceRoute } from './explorer.js';
import { valueOf } from './monitor.js';
import { createCopyBox, dptBits, flagColumns, mainUiRoot, objectSections, readUses, sizeText, usesBlock } from './objects.js';

const STATE_TEXTS = Object.freeze({
    NONE: 'addresses.noProject',
    UNREADABLE: 'addresses.unreadable',
    NEWER_VERSION: 'addresses.newer',
});

/** The rows drawn at first, and added by each "Show more". */
export const FIND_ROWS = ROWS_CAP;
/** How long a row of the list stays selected before its channel text is asked for (call 13). */
export const SUGGESTION_MILLIS = 250;

function textOf(value) {
    return typeof value === 'string' ? value : '';
}

/** @returns {{close: () => void, update: (route: Object) => void, focus: () => void}} */
export function show(context) {
    const { document, element, api, rows, stream, bridge, navigate, frame, timers, clipboard, showError } = context;
    const workplace = createWorkplace({ document, element, layout: context.layout, title: text('view.group-addresses'), context });
    workplace.setCrumbs([{ key: '', label: text('view.group-addresses') }]);
    if (!rows) {
        return {
            update() {},
            focus() {},
            close() {
                workplace.close();
            },
        };
    }

    const monitorOn = bridge.present === true && bridge.bufferSize !== 0;
    let closed = false;
    let pending = false;
    let mode = null;
    let roots = [];
    let items = [];
    let book = new Map();
    let withValues = false;
    let withChannel = false;
    let seenCount = -1;
    let selected = textOf(context.route?.query?.sel);
    let rowKey = textOf(context.route?.query?.row);
    let listMode = null;
    let suggestionTimer = null;
    let node = null;
    let linked = null;
    let linkedVersion = 0;
    let turn = 0;
    let addressTable = null;
    let activeTable = null;
    let statusLabel = '';
    let suggestionParts = [];
    let suggestionFor = null;
    let shown = null;
    const uses = new Map();
    const root = mainUiRoot(context.base);

    const copyBox = createCopyBox(document, 'suggestion', text('addresses.suggestion'), clipboard);
    const foldersNote = el(document, 'p', { class: 'note' }, text('folder.choose'));
    const unknownNote = el(document, 'p', { class: 'note' }, text('addresses.unknown'));

    function openLink(route) {
        return el(document, 'a', { class: 'open', href: href(route) }, text('frame.open'));
    }

    /** The selected row of the list becomes part of the address, without a new entry in the history. */
    function setRow(key) {
        rowKey = key;
        context.replace?.(selection(context.route, selected, key));
    }

    /** Focus on the list's selected or first row; on the list itself while it has none. */
    function focusList() {
        if (!activeTable?.focus()) {
            workplace.focusList();
        }
    }

    /** The rows a waiting focus was meant for have come. */
    function settleFocus() {
        if (document.activeElement === workplace.list) {
            activeTable?.focus();
        }
    }

    function note(key) {
        add(document, workplace.notes, el(document, 'p', { class: 'note' }, text(key)));
    }

    function noteMonitorOff() {
        add(document, workplace.notes, el(document, 'p', { class: 'note' }, errorText('monitor-off')));
    }

    const redrawStatus = () => {
        const counts = activeTable ? activeTable.counts() : null;
        context.status?.({ selection: statusLabel, shown: counts?.shown, total: counts?.total });
    };

    function lastOf(row) {
        return row.item ? (rows.last(row.item.address) ?? null) : null;
    }

    /** {@return the size of the first linked object, else the one the data type gives, else 0} */
    function bitsOf(item) {
        const bits = linked ? linked.get(item.address)?.[0]?.object.sizeBits : null;
        return Number.isInteger(bits) && bits > 0 ? bits : dptBits(item.dataType);
    }

    function lengthOf(row) {
        return row.item ? bitsOf(row.item) : 0;
    }

    function lastCell(last) {
        if (!last) {
            return null;
        }
        const date = parseTime(last.time);
        return [
            el(document, 'span', { class: 'value' }, valueOf(document, last)),
            el(document, 'span', { class: 'time', title: date ? dateText(date) : null }, date ? clockText(date) : String(last.time ?? '')),
        ];
    }

    function securityMark(secured) {
        return secured === true ? el(document, 'span', { class: 'secured', role: 'img', title: text('addresses.secured'), 'aria-label': text('addresses.secured') }, icon(document, 'key')) : null;
    }

    /** A row is a range {key, kind: 'range', address, name} or an address {key, kind: 'ga', item}. */
    function addressColumns() {
        const seen = mode === 'seen';
        const columns = [
            {
                key: 'address',
                label: text('column.address'),
                className: 'address-cell',
                value: (row) => row.address,
                compare: (a, b) => addressOrder(a.address, b.address),
                cell: (row) => [icon(document, row.kind === 'ga' ? 'address' : 'range'), el(document, 'span', { class: 'address' }, row.address)],
            },
            { key: 'name', label: text('column.name'), className: 'name', value: (row) => row.name },
            seen ? null : { key: 'description', label: text('column.description'), value: (row) => textOf(row.item?.description) },
            { key: 'dataType', label: text('column.dataType'), className: 'type', value: (row) => textOf(row.item?.dataType) },
            seen ? null : { key: 'length', label: text('column.length'), find: false, value: lengthOf, stamp: () => linkedVersion, cell: (row) => sizeText(lengthOf(row)) },
            seen
                ? null
                : { key: 'security', label: text('column.security'), className: 'security', find: false, value: (row) => (row.item?.secured === true ? 1 : 0), cell: (row) => securityMark(row.item?.secured) },
            seen
                ? null
                : {
                      key: 'associations',
                      label: text('column.associations'),
                      className: 'num',
                      find: false,
                      value: (row) => (row.item && linked ? (linked.get(row.item.address)?.length ?? 0) : -1),
                      stamp: () => linkedVersion,
                      cell: (row) => (row.item && linked ? String(linked.get(row.item.address)?.length ?? 0) : null),
                  },
            withValues
                ? {
                      key: 'lastValue',
                      label: text('column.lastValue'),
                      className: 'last',
                      find: false,
                      value: (row) => textOf(lastOf(row)?.time),
                      stamp: lastOf,
                      cell: (row) => lastCell(lastOf(row)),
                  }
                : null,
            withChannel
                ? {
                      key: 'openhab',
                      label: text('column.openhab'),
                      className: 'configured',
                      find: false,
                      value: (row) => (addressInOpenhab(row.item) ? 1 : 0),
                      cell: (row) => (addressInOpenhab(row.item) ? text('addresses.configured') : null),
                  }
                : null,
        ];
        return columns.filter(Boolean);
    }

    function rowOf(child) {
        return child.kind === 'ga' ? { key: child.key, kind: 'ga', address: child.address, name: child.name, item: child.item } : child;
    }

    const rowCache = new Map();
    function keptRow(child) {
        if (!rowCache.has(child.key)) {
            rowCache.set(child.key, rowOf(child));
        }
        return rowCache.get(child.key);
    }

    /** {@return the object as its column names it: number, name and function} */
    function objectWords(row) {
        const number = Number.isInteger(row.object.number) ? `${row.object.number}:` : '';
        return [number, [textOf(row.object.name), textOf(row.object.function)].filter((part) => part !== '').join(' - ')].filter((part) => part !== '').join(' ');
    }

    const associationTable = createTable({
        document,
        label: text('addresses.associations'),
        keyOf: (row) => row.key,
        onSelect: (row) => {
            setRow(row ? row.key : '');
            showAddress(node, row);
        },
        // the device in the other panel of the workplace, in its own tree when that is Topology or Buildings
        onOpen: (row) => (context.jump ?? navigate)(deviceRoute(bridge, row.device), ['topology', 'buildings']),
        onDraw: () => {
            if (listMode === 'associations') {
                drawTabs();
            }
            redrawStatus();
        },
        layout: context.layout,
        name: 'associations',
        columns: [
            {
                key: 'object',
                label: text('column.object'),
                className: 'object',
                value: objectWords,
                compare: (a, b) => (Number.isInteger(a.object.number) ? a.object.number : Infinity) - (Number.isInteger(b.object.number) ? b.object.number : Infinity),
            },
            {
                key: 'device',
                label: text('column.device'),
                value: (row) => [textOf(row.device.address), textOf(row.device.name)].join(' '),
                cell: (row) => [
                    textOf(row.device.address) === '' ? null : el(document, 'span', { class: 'address' }, row.device.address),
                    el(document, 'span', { class: 'name' }, textOf(row.device.name) || String(row.device.id ?? '')),
                ],
            },
            {
                key: 'sending',
                label: text('column.sending'),
                className: 'flag-cell',
                find: false,
                value: (row) => (row.sending ? 1 : 0),
                cell: (row) => el(document, 'span', { class: row.sending ? 'flag on sending' : 'flag off', title: row.sending ? text('device.sendingTitle') : null }, row.sending ? text('flag.letter.sending') : text('flag.none')),
            },
            { key: 'dataType', label: text('column.dataType'), className: 'type', value: (row) => textOf(row.object.dataType) },
            ...flagColumns(document, (row) => row.object),
            { key: 'product', label: text('column.product'), value: (row) => textOf(row.device.product) },
            bridge.present === true
                ? {
                      key: 'openhab',
                      label: text('column.openhab'),
                      className: 'configured',
                      find: false,
                      value: (row) => (deviceInOpenhab(row.device, book) ? 1 : 0),
                      cell: (row) => (deviceInOpenhab(row.device, book) ? text('addresses.configured') : null),
                  }
                : null,
        ].filter(Boolean),
    });

    const tree = createTree({
        document,
        label: text('view.group-addresses'),
        startsOpen: (at) => at.key === '',
        memory: treeMemory(context.layout, `group-addresses|${bridge.uid}`),
        // the slide-in tree of a narrow screen closes, and the focus that was in it goes to the list
        onSelect: (chosen) => go(chosen.key, workplace.closeTree()),
    });
    workplace.setTree(tree);

    function setTools(table) {
        activeTable = table;
        place(workplace.tools, [wizardLink()].filter(Boolean));
        place(workplace.find, [table.findField]);
    }

    function drawTabs() {
        if (listMode === 'associations') {
            workplace.setTabs([{ key: 'associations', label: text('addresses.associations'), count: linked ? associationTable.counts().total : undefined }], 'associations');
        } else if (listMode === 'addresses') {
            workplace.setTabs([{ key: 'addresses', label: text('view.group-addresses'), count: addressTable.counts().total }], 'addresses');
        } else {
            workplace.setTabs([], '');
        }
    }

    function drawCrumbs() {
        const path = mode === 'project' ? tree.path(node ? node.key : '') : [];
        const crumbs = path.map((at) => ({ key: at.key, label: [textOf(at.address), textOf(at.name)].filter((part) => part !== '').join(' ') }));
        if (crumbs.length === 0) {
            crumbs.push({ key: '', label: text('view.group-addresses') });
        }
        workplace.setCrumbs(crumbs, (key) => {
            if (key !== selected) {
                go(key, element.contains(document.activeElement));
            }
        });
    }

    function wizardLink() {
        return bridge.present === true && mode === 'project' ? el(document, 'a', { class: 'add', href: href({ view: 'wizard', bridge: bridge.uid, query: {} }) }, text('wizard.link')) : null;
    }

    /** The openHAB and Live containers for a group address; for anything else both are empty. */
    function showSide(item) {
        if (!item) {
            workplace.openhab.empty();
            workplace.live.watch(null);
            return;
        }
        workplace.openhab.show(
            [[text('addresses.channel'), withChannel ? text(addressInOpenhab(item) ? 'addresses.configured' : 'addresses.notConfigured') : null]],
            [...(withChannel && addressInOpenhab(item) && uses.has(item.address) ? usesBlock(document, root, uses.get(item.address).answer) : []), ...(suggestionFor === item.address ? suggestionParts : []), wizardLink()],
        );
        workplace.live.watch({ addresses: [String(item.address)] });
    }

    /** {@return the addresses a dynamic folder holds} */
    function membersOf(folder) {
        switch (folder.rule) {
            case 'in':
                return items.filter(addressInOpenhab);
            case 'out':
                return items.filter((item) => !addressInOpenhab(item));
            case 'secured':
                return items.filter((item) => item.secured === true);
            case 'unlinked':
                return linked ? items.filter((item) => (linked.get(item.address)?.length ?? 0) === 0) : [];
            default:
                return [];
        }
    }

    function addressSections(item, last) {
        const links = linked ? (linked.get(item.address) ?? []) : null;
        const bits = bitsOf(item);
        return [
            {
                rows: [
                    [text('column.address'), String(item.address)],
                    [text('column.name'), textOf(item.name)],
                    [text('column.description'), textOf(item.description)],
                    [text('column.dataType'), textOf(item.dataType)],
                    [text('column.length'), sizeText(bits)],
                    [text('column.security'), item.secured === true ? text('addresses.secured') : null],
                ],
            },
            { tab: 'comments', rows: [[text('column.description'), textOf(item.description)]] },
            {
                tab: 'information',
                rows: [
                    [text('column.associations'), links ? String(links.length) : null],
                    [text('column.lastValue'), last ? lastValue(last) : null, 'last'],
                    [text('column.time'), last ? lastTime(last) : null, 'time'],
                ],
            },
        ];
    }

    function lastValue(last) {
        return el(document, 'span', { class: 'value' }, valueOf(document, last));
    }

    function lastTime(last) {
        const date = parseTime(last.time);
        return date ? dateText(date) : String(last.time ?? '');
    }

    function dropSuggestionTimer() {
        if (suggestionTimer !== null) {
            timers.clearTimeout(suggestionTimer);
            suggestionTimer = null;
        }
    }

    /** Asks for the channel text of an address: at once when it is opened, else when its row stays selected. */
    function askSuggestion(item, now) {
        dropSuggestionTimer();
        if (mode !== 'project' || suggestionFor === item.address) {
            return;
        }
        if (now) {
            loadSuggestion(item);
            return;
        }
        suggestionTimer = timers.setTimeout(() => {
            suggestionTimer = null;
            if (!closed) {
                loadSuggestion(item);
            }
        }, SUGGESTION_MILLIS);
    }

    /**
     * Asks which channels hold the address and which Items are linked to them (call 23), once for each address; a
     * read that failed is made again when the address is next shown.
     */
    function loadUses(item) {
        const held = uses.get(item.address);
        if (!withChannel || !addressInOpenhab(item) || (held && held.answer?.error === undefined)) {
            return;
        }
        const state = {};
        uses.set(item.address, state);
        readUses(
            (address) => api.groupAddress(bridge.uid, address),
            [item.address],
            (address, answer) => {
                state.answer = answer;
                if (!closed && shown?.item.address === address) {
                    showSide(shown.item);
                }
            },
        );
    }

    async function loadSuggestion(item) {
        loadUses(item);
        const mine = ++turn;
        suggestionFor = item.address;
        suggestionParts = [];
        let answer;
        try {
            answer = await api.channels(bridge.uid, [item.address]);
        } catch (error) {
            if (closed || mine !== turn) {
                return;
            }
            suggestionParts = [el(document, 'h5', {}, text('addresses.suggestion')), el(document, 'p', { class: 'note' }, errorText(error?.code, error?.text))];
            showSide(shown?.item);
            return;
        }
        if (closed || mine !== turn) {
            return;
        }
        const lines = (Array.isArray(answer?.suggestions) ? answer.suggestions : []).map((suggestion) => suggestion?.text).filter((line) => typeof line === 'string');
        if (lines.length > 0) {
            copyBox.field.value = lines.join('\n');
            clear(copyBox.state);
            suggestionParts = [el(document, 'h5', {}, text('addresses.suggestion')), copyBox.field, copyBox.tools];
            showSide(shown?.item);
        }
    }

    /** Properties for a group address, with the selected linked object when there is one. */
    function showAddress(addressNode, row) {
        const item = addressNode.item;
        const opened = node !== null && node.kind === 'ga';
        shown = { item, last: rows.last(item.address) ?? null };
        let open = null;
        if (opened) {
            open = row ? openLink(deviceRoute(bridge, row.device)) : null;
        } else if (mode === 'project') {
            open = openLink(selection(context.route, addressNode.key));
        }
        workplace.properties.show(
            [item.address, textOf(item.name)].filter((part) => part !== '').join(' '),
            [
                ...addressSections(item, shown.last),
                ...(row ? [...objectSections(row.object), { tab: 'information', heading: text('device.object'), rows: [[text('column.device'), [textOf(row.device.address), textOf(row.device.name)].filter((part) => part !== '').join(' ')], [text('column.product'), textOf(row.device.product)]] }] : []),
            ],
            [open],
        );
        showSide(item);
        askSuggestion(item, opened);
    }

    /** @param {boolean} row the range is a row of the list and can be opened */
    function showRange(range, row) {
        shown = null;
        dropSuggestionTimer();
        showSide(null);
        if (!range || range.key === '' || range.kind === 'folders') {
            workplace.properties.empty();
            return;
        }
        workplace.properties.show(
            [range.address, range.name].filter((part) => part !== '').join(' '),
            [
                {
                    rows: [
                        [text('column.address'), range.address],
                        [text('column.name'), range.name],
                    ],
                },
                { tab: 'information', rows: [[text('addresses.under'), String(range.kind === 'folder' ? membersOf(range).length : addressesUnder(range).length)]] },
            ],
            [row ? openLink(selection(context.route, range.key)) : null],
        );
    }

    /** Draws Properties for what is selected now: a row of the list, else the node of the tree. */
    function showSelection() {
        if (node && node.kind === 'ga') {
            showAddress(node, associationTable.selected());
            return;
        }
        const row = addressTable?.selected() ?? null;
        if (row && row.kind === 'ga') {
            showAddress(row, null);
        } else {
            showRange(row ?? node, Boolean(row));
        }
    }

    function placeList() {
        place(workplace.list, node?.kind === 'folders' ? [foldersNote] : [addressTable.node]);
    }

    function listRows() {
        if (mode === 'seen') {
            return [...rows.lastValues().keys()].sort(addressOrder).map((address) => {
                const last = rows.last(address);
                return keptRow({ key: addressKey(address), kind: 'ga', address, name: textOf(last?.destinationName), item: { address, name: last?.destinationName, dataType: last?.dataType } });
            });
        }
        const asRow = (item) => keptRow({ key: addressKey(item.address), kind: 'ga', address: String(item.address), name: textOf(item.name), item });
        if (node?.kind === 'folder') {
            return membersOf(node).map(asRow);
        }
        if (node?.kind === 'folders') {
            return [];
        }
        const under = node && node.key !== '' ? node : { kind: 'range', children: roots };
        // the find box looks through every address below, not only the next level
        return addressTable.find() === '' ? under.children.map(keptRow) : under.children.flatMap((child) => (child.kind === 'ga' ? [child] : addressesUnder(child).map((item) => ({ key: addressKey(item.address), kind: 'ga', address: String(item.address), name: textOf(item.name), item })))).map(keptRow);
    }

    function drawList() {
        const all = node === null || node.key === '';
        const waiting = node?.kind === 'folder' && node.rule === 'unlinked' && !linked;
        const none = mode === 'seen' ? 'addresses.noneSeen' : waiting ? 'app.loading' : all ? 'addresses.empty' : node.kind === 'folder' ? 'addresses.emptyFolder' : 'addresses.emptyRange';
        addressTable.setRows(listRows(), { none: text(none), noMatch: text('addresses.noMatch') });
    }

    function drawAssociations() {
        const item = node.item;
        const links = linked ? (linked.get(item.address) ?? []) : [];
        associationTable.setRows(
            links.map((link) => {
                // two objects of one device can share a number, or have none: the place in the device tells them apart
                const key = `link:${link.device.id ?? link.device.address}|${link.at}`;
                const cacheKey = `${item.address}|${key}`;
                if (!rowCache.has(cacheKey) || rowCache.get(cacheKey).object !== link.object) {
                    rowCache.set(cacheKey, { ...link, key, sending: link.object.sendingGroupAddress === item.address });
                }
                return rowCache.get(cacheKey);
            }),
            { none: linked ? text('addresses.noAssociations') : text('app.loading'), noMatch: text('addresses.noMatch') },
        );
    }

    /** Shows what the key names: a range with what is under it, or one address with its Associations. */
    function apply(key, row = '') {
        selected = key;
        rowKey = row;
        if (mode === null) {
            return;
        }
        node = mode === 'project' ? tree.select(key) : null;
        if (node === null && mode === 'project' && key.startsWith('range:') && !key.includes('@')) {
            // an address made before ranges were told apart by number
            const range = rangeByNames(roots, key);
            node = range ? tree.select(range.key) : null;
        }
        if (key !== '' && node === null && mode === 'project') {
            statusLabel = key;
            listMode = 'unknown';
            shown = null;
            dropSuggestionTimer();
            setTools(addressTable);
            place(workplace.list, [unknownNote]);
            workplace.properties.empty();
            showSide(null);
            drawTabs();
            drawCrumbs();
            redrawStatus();
            return;
        }
        if (node && node.kind === 'ga') {
            statusLabel = [node.address, node.name].filter((part) => part !== '').join(' ');
            listMode = 'associations';
            setTools(associationTable);
            place(workplace.list, [associationTable.node]);
            drawAssociations();
            associationTable.select(rowKey === '' ? undefined : rowKey);
        } else {
            statusLabel = node && node.key !== '' ? [node.address, node.name].filter((part) => part !== '').join(' ') : text('view.group-addresses');
            listMode = 'addresses';
            setTools(addressTable);
            addressTable.setLimit(FIND_ROWS, false);
            drawList();
            addressTable.select(rowKey === '' ? undefined : rowKey);
        }
        drawTabs();
        drawCrumbs();
        showSelection();
    }

    /** Selects the row the address names in the list that is shown; no name selects none. */
    function restoreRow() {
        if (listMode === 'associations' || listMode === 'addresses') {
            activeTable.select(rowKey === '' ? undefined : rowKey);
            showSelection();
        }
    }

    /** @param {boolean} [focus] the focus was on what is left behind: it goes to the new list */
    function go(key, focus = false) {
        // going to the address already shown would make the shell build the panel again
        const moved = key !== selected;
        const hadRow = rowKey !== '';
        apply(key);
        if (moved) {
            navigate(selection(context.route, key));
        } else if (hadRow) {
            context.replace?.(selection(context.route, key));
        }
        if (focus) {
            focusList();
        }
    }

    function draw() {
        pending = false;
        if (closed || mode === null) {
            return;
        }
        if (mode === 'seen' && rows.lastValues().size !== seenCount) {
            seenCount = rows.lastValues().size;
            drawList();
        }
        addressTable.refresh();
        if (shown === null) {
            return;
        }
        const last = rows.last(shown.item.address) ?? null;
        if (last !== shown.last) {
            shown.last = last;
            // only the two fields change: a focused button or a selected text in the pane stays as it is
            const set = last !== null && workplace.properties.set('last', lastValue(last)) && workplace.properties.set('time', lastTime(last));
            if (!set) {
                showSelection();
            }
        }
    }

    function request() {
        if (!pending && !closed) {
            pending = true;
            frame(draw);
        }
    }

    function begin(kind, found) {
        mode = kind;
        addressTable = createTable({
            document,
            label: text('view.group-addresses'),
            keyOf: (row) => row.key,
            columns: addressColumns(),
            onSelect: (row) => {
                setRow(row ? row.key : '');
                showSelection();
            },
            onOpen: (row) => {
                if (mode === 'project') {
                    go(row.key, true);
                }
            },
            onFind: () => {
                // the rows the box looks through change with its text
                if (mode === 'project' && activeTable === addressTable) {
                    drawList();
                }
            },
            onDraw: () => {
                if (listMode === 'addresses') {
                    placeList();
                    drawTabs();
                }
                redrawStatus();
            },
            layout: context.layout,
            name: kind === 'project' ? 'groupAddresses' : 'seenAddresses',
        });
        roots = kind === 'project' ? groupTree(found) : [];
        items = kind === 'project' ? found.filter((item) => typeof item === 'object' && item !== null) : [];
        book = addressBook(items);
        const folders = [
            ...(withChannel ? [folderNode('folder:not-in-openhab', text('folder.notInOpenhab'), { rule: 'out' }), folderNode('folder:in-openhab', text('folder.inOpenhab'), { rule: 'in' })] : []),
            folderNode('folder:no-associations', text('folder.noAssociations'), { rule: 'unlinked' }),
            folderNode('folder:secured', text('folder.secured'), { rule: 'secured' }),
        ];
        const root = { key: '', kind: 'range', icon: 'range', address: '', name: text('view.group-addresses'), children: roots };
        tree.setRoots(kind === 'project' ? [root, folderGroup(folders)] : [root]);
        apply(selected, rowKey);
        settleFocus();
        request();
    }

    async function loadAssociations() {
        let details;
        try {
            details = await rows.deviceDetails();
        } catch (error) {
            if (!closed) {
                showError(error);
            }
            return;
        }
        if (closed) {
            return;
        }
        linked = associations(details.devices);
        linkedVersion++;
        addressTable.refresh();
        if (node && node.kind === 'ga') {
            drawAssociations();
            associationTable.select(rowKey === '' ? undefined : rowKey);
            settleFocus();
        } else if (node?.kind === 'folder' && node.rule === 'unlinked') {
            drawList();
            addressTable.select(rowKey === '' ? undefined : rowKey);
            settleFocus();
        }
        drawTabs();
        showSelection();
        // the devices that were read are shown; the others are read again with the next visit
        if (details.problems.length > 0) {
            showError(details.problems[0]);
        }
    }

    async function load() {
        let project;
        try {
            project = await api.project(bridge.uid);
        } catch (error) {
            if (!closed) {
                showError(error);
            }
            return;
        }
        if (closed) {
            return;
        }
        if (project?.state === 'LOADED') {
            let addresses;
            try {
                addresses = await rows.groupAddresses();
            } catch (error) {
                if (!closed) {
                    showError(error);
                }
                return;
            }
            if (closed) {
                return;
            }
            withValues = monitorOn;
            withChannel = bridge.present === true;
            if (bridge.present === true && !monitorOn) {
                noteMonitorOff();
            }
            begin('project', addresses);
            if (monitorOn) {
                stream?.start();
            }
            loadAssociations();
            return;
        }
        if (Object.hasOwn(STATE_TEXTS, project?.state)) {
            note(STATE_TEXTS[project.state]);
        }
        if (bridge.present !== true) {
            return;
        }
        if (!monitorOn) {
            noteMonitorOff();
            return;
        }
        let seen;
        try {
            seen = await api.lastValues(bridge.uid);
        } catch (error) {
            if (closed) {
                return;
            }
            if (error?.code === 'monitor-off') {
                noteMonitorOff();
            } else {
                showError(error);
            }
            return;
        }
        if (closed) {
            return;
        }
        rows.seedLast(seen);
        withValues = true;
        begin('seen', []);
        stream?.start();
    }

    const leaveRows = rows.subscribe(() => request());

    load();

    return {
        update(route) {
            context.route = route;
            const key = textOf(route?.query?.sel);
            const row = textOf(route?.query?.row);
            // Back, or "Open" in Properties, takes away what held the focus
            const focusWas = workplace.holds(document.activeElement);
            if (key !== selected || mode === null) {
                apply(key, row);
            } else if (row !== rowKey) {
                rowKey = row;
                restoreRow();
            }
            if (focusWas && !workplace.holds(document.activeElement)) {
                focusList();
            }
        },
        /** The panel was opened from another one by key or click: the keyboard goes on in its list. */
        focus() {
            focusList();
        },
        close() {
            closed = true;
            dropSuggestionTimer();
            leaveRows();
            copyBox.close();
            workplace.close();
            tree.close();
            addressTable?.close();
            associationTable.close();
        },
    };
}
