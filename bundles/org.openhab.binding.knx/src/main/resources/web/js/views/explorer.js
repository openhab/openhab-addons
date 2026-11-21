// What Topology and Buildings share: a tree of places with its dynamic folders, the lists of the selected place under
// their tabs (Rooms or Parts, Devices, Group Objects grouped per device, Functions), the lists of a selected device
// (Group Objects, Channels), and the panel's face of the sidebar. A panel gives its tree; everything else is the same
// in both.

import { add, clear, el, icon, place } from '../dom.js';
import { addressBook, addressInOpenhab, addressKey, deviceInOpenhab, deviceKey, deviceOrder, devicesUnder, folderGroup, folderNode, functionsUnder, linkedAddresses, spaceKind, spaceLook } from '../model.js';
import { href, selection } from '../router.js';
import { createTable } from '../table.js';
import { errorText, text } from '../texts.js';
import { createTree, treeMemory } from '../tree.js';
import { createWorkplace } from '../workplace.js';
import { createCopyBox, createObjectsTable, itemLinks, mainUiRoot, objectSections, readUses, roleText, thingLink, usesBlock } from './objects.js';

const MISSING = Object.freeze({
    NONE: 'project.none',
    'no-project': 'project.none',
    UNREADABLE: 'project.unreadable',
    NEWER_VERSION: 'project.newer',
});

/**
 * @param {string} reason a project state of call 8 or an error code of calls 9 to 16
 * @returns {Element|null} why the project cannot be shown, with a link to Project; null when the reason is none of
 *     these
 */
export function projectMissing(document, bridge, reason) {
    let words = null;
    if (Object.hasOwn(MISSING, reason)) {
        words = text(MISSING[reason]);
    } else if (reason === 'project-unusable') {
        words = errorText(reason);
    }
    if (words === null) {
        return null;
    }
    return el(
        document,
        'p',
        { class: 'note missing' },
        words,
        ' ',
        el(document, 'a', { href: href({ view: 'project', bridge: bridge.uid, query: {} }) }, text('project.toSetup')),
    );
}

/** {@return the route of a device: selected in Topology; the project id names a device without doubt} */
export function deviceRoute(bridge, device) {
    return { view: 'topology', bridge: bridge.uid, query: { sel: deviceKey(device) } };
}

function textOf(value) {
    return typeof value === 'string' ? value : '';
}

function count(value) {
    return Number.isFinite(value) ? value : 0;
}

function listOf(value) {
    return Array.isArray(value) ? value.filter((item) => typeof item === 'object' && item !== null) : [];
}

/** {@return what names a device in the page: its project id, else its address} */
function idOf(device) {
    return String(device.id ?? device.address);
}

function nameOf(device) {
    return [textOf(device.address), textOf(device.name) || String(device.id ?? '')].filter((part) => part !== '').join(' ');
}

/**
 * {@return the columns of a list of devices}
 *
 * @param {(device: Object) => boolean} [inOpenhab] with it, the column that says whether a channel uses the device
 * @param {() => *} [stamp] names the state the openHAB column was drawn for
 * @param {(device: Object) => boolean} [unread] whether the device's objects could not be read
 * @param {(device: Object) => string} [line] with it, the column that names the device's line
 */
export function deviceColumns(document, inOpenhab, stamp, unread = () => false, line = null) {
    return [
        { key: 'address', label: text('column.address'), className: 'address-cell', value: (device) => textOf(device.address), compare: deviceOrder, cell: (device) => el(document, 'span', { class: 'address' }, textOf(device.address) || text('app.none')) },
        line ? { key: 'line', label: text('column.line'), value: (device) => line(device) } : null,
        { key: 'room', label: text('column.room'), value: (device) => textOf(device.room) },
        {
            key: 'name',
            label: text('column.name'),
            className: 'name',
            value: (device) => textOf(device.name) || String(device.id ?? ''),
            cell: (device) => [
                textOf(device.name) || String(device.id ?? ''),
                device.filterDevice === true ? el(document, 'span', { class: 'filter-device' }, text('device.filter')) : null,
            ],
        },
        { key: 'description', label: text('column.description'), value: (device) => textOf(device.description) },
        { key: 'product', label: text('column.product'), value: (device) => textOf(device.product) },
        { key: 'orderNumber', label: text('column.orderNumber'), value: (device) => textOf(device.orderNumber) },
        { key: 'objects', label: text('column.objects'), className: 'num', find: false, value: (device) => count(device.objects) },
        { key: 'linked', label: text('column.linked'), className: 'num', find: false, value: (device) => count(device.linkedObjects) },
        inOpenhab
            ? { key: 'openhab', label: text('column.openhab'), className: 'configured', find: false, value: (device) => (inOpenhab(device) ? 1 : 0), stamp, cell: (device) => (unread(device) ? el(document, 'span', { class: 'uses-state' }, text('device.unread')) : inOpenhab(device) ? text('addresses.configured') : null) }
            : null,
    ].filter(Boolean);
}

/** {@return what Properties shows for a device, under its three tabs} */
function deviceSections(document, device) {
    return [
        {
            rows: [
                [text('column.address'), textOf(device.address)],
                [
                    text('column.name'),
                    device.filterDevice === true
                        ? el(document, 'span', {}, textOf(device.name), el(document, 'span', { class: 'filter-device' }, text('device.filter')))
                        : textOf(device.name),
                ],
                [text('column.description'), textOf(device.description)],
            ],
        },
        { tab: 'comments', rows: [[text('column.description'), textOf(device.description)]] },
        {
            tab: 'information',
            rows: [
                [text('column.product'), textOf(device.product)],
                [text('column.orderNumber'), textOf(device.orderNumber)],
                [text('column.room'), textOf(device.room)],
                [text('column.objects'), Number.isFinite(device.objects) ? String(device.objects) : null],
                [text('column.linked'), Number.isFinite(device.linkedObjects) ? String(device.linkedObjects) : null],
            ],
            more: [device.filterDevice === true ? el(document, 'p', { class: 'note filter-note' }, text('device.filterNote')) : null],
        },
    ];
}

/** For how many addresses of a device the Channels tab reads the channels at first, and with each "Show more". */
export const USES_ROWS = 20;

const PLACE_TABS = ['spaces', 'devices', 'objects', 'functions'];
const DEVICE_TABS = ['objects', 'channels'];

/**
 * @param context the view's context
 * @param {Object} panel
 * @param {string} panel.title the panel's name
 * @param {string} [panel.icon] the icon of the tree's first node
 * @param {() => Promise<{roots?: Array, devices: Array, folders?: Array, problems?: Array}>} panel.load
 *     the tree's nodes below its first node and the devices listed for that node, which are every device the panel
 *     knows; folders, the panel's own dynamic folders; problems are the errors of calls that failed while the rest
 *     could be shown
 * @param {(node: Object) => boolean} [panel.startsOpen] the nodes below the first one that are open at first
 * @param {(device: Object) => string} [panel.lineOf] names the line of a device, for a column of the device lists
 * @param {string} [panel.emptyNote] the text key said when the tree has no node
 * @param {string} [panel.emptyDevices] the text key said when the panel has no device at all
 * @returns {{close: () => void, update: (route: Object) => void}}
 */
export function showExplorer(context, panel) {
    const { document, element, api, rows, stream, bridge, navigate, frame, clipboard, showError } = context;
    const workplace = createWorkplace({ document, element, layout: context.layout, title: panel.title, context });
    const live = bridge.present === true && bridge.bufferSize !== 0;
    const withOpenhab = bridge.present === true;
    const routeOf = (device) => selection(context.route, deviceKey(device));

    let closed = false;
    let pending = false;
    let loaded = false;
    let devices = [];
    let allDevices = [];
    let book = new Map();
    let bookLoaded = false;
    let details = new Map();
    let detailsLoaded = false;
    let stateVersion = 0;
    let selected = textOf(context.route?.query?.sel);
    let rowKey = textOf(context.route?.query?.row);
    let tab = textOf(context.route?.query?.tab);
    let mode = null;
    let turn = 0;
    let detail = null;
    let leaveRows = null;
    let subject = null;
    let suggestionParts = [];
    let placeNode = null;
    let placeDevices = [];
    let placeFunctions = [];
    let placeSpaces = [];
    let channelRows = [];
    let channelAddresses = [];
    let uses = new Map();
    let usesWanted = USES_ROWS;
    let unread = [];
    let lateSide = null;
    const root = mainUiRoot(context.base);

    const subjectTools = el(document, 'span', { class: 'subject-tools' });
    const copyBox = createCopyBox(document, 'suggestions', text('device.suggestions'), clipboard);
    const deviceNote = el(document, 'div', { class: 'device-note' });
    const unknownNote = el(document, 'p', { class: 'note' }, text('frame.unknown'));
    const foldersNote = el(document, 'p', { class: 'note' }, text('folder.choose'));
    const usesState = el(document, 'span', {});
    const usesMore = el(document, 'button', { type: 'button', class: 'more' }, text('frame.more'));
    const channelsNote = el(document, 'p', { class: 'note channels-note' }, usesState);
    const unreadBox = el(document, 'div', { class: 'unread' });

    let openedFunction = null;
    let entryKeys = new Map();

    const functionKey = (fn) => (textOf(fn.id) === '' ? fn : `function:${fn.id}`);
    const ready = () => detailsLoaded && bookLoaded;
    const inOpenhab = (device) => {
        const known = details.get(idOf(device));
        return known !== undefined && deviceInOpenhab(known, book);
    };
    const isUnread = (device) => unread.some((failed) => idOf(failed) === idOf(device));
    const useState = (row) => (row.use ? null : text(row.answer === undefined ? (row.asked ? 'app.loading' : 'use.later') : row.answer.error !== undefined ? 'use.failed' : 'use.none'));

    function openLink(route) {
        return el(document, 'a', { class: 'open', href: href(route) }, text('frame.open'));
    }

    /** {@return the first tab of a place: its rooms or parts when it holds any, else its devices} */
    function firstPlaceTab() {
        return placeSpaces.length > 0 ? 'spaces' : 'devices';
    }

    /** {@return the tab for the address: none for the first tab, which an address without one shows} */
    function tabName() {
        const first = mode === 'device' ? DEVICE_TABS[0] : firstPlaceTab();
        return tab === first || mode === 'function' ? '' : tab;
    }

    /** The selected row of the list becomes part of the address, without a new entry in the history. */
    function setRow(key) {
        rowKey = typeof key === 'string' ? key : '';
        context.replace?.(selection(context.route, selected, rowKey, tabName()));
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

    function wizardLink(query) {
        return bridge.present === true ? el(document, 'a', { class: 'add', href: href({ view: 'wizard', bridge: bridge.uid, query }) }, text('wizard.link')) : null;
    }

    /** Names what "Add to openHAB" would add: a device, a space or a function; null for anything else. */
    function setSubject(query) {
        subject = query;
        clear(subjectTools);
        add(document, subjectTools, query ? wizardLink(query) : null);
    }

    function status(label, table) {
        const counts = table ? table.counts() : null;
        context.status?.({ selection: label, shown: counts?.shown, total: counts?.total });
    }

    let statusLabel = '';
    let activeTable = null;
    const redrawStatus = () => status(statusLabel, activeTable);

    const deviceTable = createTable({
        document,
        label: text('devices.title'),
        keyOf: (device) => deviceKey(device),
        columns: deviceColumns(document, withOpenhab ? inOpenhab : null, () => stateVersion, isUnread, panel.lineOf),
        layout: context.layout,
        name: 'devices',
        onSelect: (device) => {
            showRowDevice(device);
            setRow(device ? deviceKey(device) : '');
        },
        onOpen: (device) => go(deviceKey(device), true),
        onDraw: redrawStatus,
    });
    // a row is the tree's node of a space that stands directly in the selected one
    const spaceTable = createTable({
        document,
        label: text('buildings.rooms'),
        keyOf: (node) => node.key,
        layout: context.layout,
        name: 'spaces',
        columns: [
            {
                key: 'kind',
                label: text('column.type'),
                className: 'space-kind',
                value: (node) => spaceLook(node.space).title,
                cell: (node) => [el(document, 'span', { class: 'kind-icon' }, icon(document, spaceLook(node.space).icon)), spaceLook(node.space).title],
            },
            { key: 'name', label: text('column.name'), className: 'name', value: (node) => node.name },
            { key: 'number', label: text('column.number'), value: (node) => textOf(node.space.number) },
            { key: 'devices', label: text('devices.title'), className: 'num', find: false, value: (node) => devicesUnder(node).length },
            { key: 'functions', label: text('buildings.functions'), className: 'num', find: false, value: (node) => functionsUnder(node).length },
            withOpenhab
                ? {
                      key: 'openhab',
                      label: text('buildings.inOpenhab'),
                      className: 'num',
                      find: false,
                      stamp: () => stateVersion,
                      value: (node) => (ready() ? devicesUnder(node).filter((device) => !isUnread(device) && inOpenhab(device)).length : -1),
                      cell: (node) => (ready() ? String(devicesUnder(node).filter((device) => !isUnread(device) && inOpenhab(device)).length) : null),
                  }
                : null,
        ].filter(Boolean),
        onSelect: (node) => {
            showNode(node ?? placeNode, node !== null && node !== undefined);
            setRow(node ? node.key : '');
        },
        onOpen: (node) => go(node.key, true),
        onDraw: redrawStatus,
    });
    const functionTable = createTable({
        document,
        label: text('buildings.functions'),
        keyOf: functionKey,
        layout: context.layout,
        name: 'functions',
        columns: [
            { key: 'name', label: text('column.name'), value: (fn) => textOf(fn.name) },
            { key: 'type', label: text('column.type'), value: (fn) => textOf(fn.type) },
            { key: 'addresses', label: text('column.groupAddresses'), className: 'num', find: false, value: (fn) => listOf(fn.groupAddresses).length },
        ],
        onSelect: (fn) => {
            if (fn) {
                showFunction(fn, true);
            } else {
                showNode(placeNode);
            }
            setRow(fn ? functionKey(fn) : '');
        },
        onOpen: (fn) => {
            if (textOf(fn.id) !== '') {
                go(`function:${fn.id}`, true);
            }
        },
        onDraw: redrawStatus,
    });
    const functionAddressTable = createTable({
        document,
        label: text('column.groupAddresses'),
        keyOf: (entry) => entryKeys.get(entry) ?? entry,
        layout: context.layout,
        name: 'functionAddresses',
        columns: [
            { key: 'address', label: text('column.address'), value: (entry) => textOf(entry.groupAddress), cell: (entry) => el(document, 'span', { class: 'address' }, textOf(entry.groupAddress) || text('app.none')) },
            { key: 'name', label: text('column.name'), value: (entry) => textOf(entry.name) },
            { key: 'role', label: text('column.role'), value: (entry) => textOf(entry.role) },
        ],
        onSelect: (entry) => {
            showFunctionAddress(entry);
            setRow(entry ? entryKeys.get(entry) : '');
        },
        onOpen: (entry) => {
            const route = addressRoute(textOf(entry.groupAddress));
            if (route) {
                navigate(route);
            }
        },
        onDraw: redrawStatus,
    });
    const objects = createObjectsTable({
        document,
        rows,
        withOpenhab,
        layout: context.layout,
        onSelect: (row) => {
            showDetail(row);
            setRow(row ? `object:${row.at}` : '');
        },
        onAddress: showAddressBeside,
        onDraw: redrawStatus,
    });
    const placeObjects = createObjectsTable({
        document,
        rows,
        grouped: true,
        withOpenhab,
        layout: context.layout,
        onSelect: (row) => {
            showPlaceObject(row);
            setRow(row ? row.key : '');
        },
        onAddress: showAddressBeside,
        onOpen: (row) => go(deviceKey(row.device), true),
        onDraw: redrawStatus,
    });
    // a row is a group address of the open device that a channel uses, once for each channel that holds it
    const channelTable = createTable({
        document,
        label: text('device.channels'),
        keyOf: (row) => row.key,
        layout: context.layout,
        name: 'channels',
        columns: [
            { key: 'address', label: text('column.address'), className: 'address-cell', value: (row) => row.address, cell: (row) => el(document, 'span', { class: 'address' }, row.address) },
            { key: 'name', label: text('column.name'), className: 'name', value: (row) => textOf(row.item.name) },
            { key: 'dataType', label: text('column.dataType'), className: 'type', value: (row) => textOf(row.item.dataType) },
            { key: 'objects', label: text('column.objects'), value: (row) => row.objects.join(', ') },
            { key: 'thing', label: text('column.thing'), className: 'thing', value: (row) => (row.use ? textOf(row.use.thingLabel) || textOf(row.use.thingUID) : useState(row)), cell: (row) => (row.use ? thingLink(document, root, row.use) : el(document, 'span', { class: 'uses-state' }, useState(row))) },
            { key: 'channel', label: text('column.channel'), value: (row) => (row.use ? textOf(row.use.channelLabel) || textOf(row.use.channelId) : '') },
            { key: 'channelType', label: text('column.type'), className: 'type', value: (row) => textOf(row.use?.channelType) },
            { key: 'role', label: text('column.role'), value: (row) => (row.use ? roleText(row.use.role) : '') },
            { key: 'items', label: text('column.items'), value: (row) => (Array.isArray(row.use?.items) ? row.use.items.join(', ') : ''), cell: (row) => (row.use ? itemLinks(document, root, row.use) : null) },
        ],
        versionOf: (row) => row.version,
        onSelect: (row) => {
            showChannel(row);
            setRow(row ? row.key : '');
        },
        onOpen: (row) => navigate(addressRoute(row.address)),
        onDraw: redrawStatus,
    });

    const tree = createTree({
        document,
        label: panel.title,
        startsOpen: (node) => node.kind === 'root' || panel.startsOpen?.(node) === true,
        memory: treeMemory(context.layout, `${context.route?.view}|${bridge.uid}`),
        // the slide-in tree of a narrow screen closes, and the focus that was in it goes to the list
        onSelect: (node) => go(node.key, workplace.closeTree()),
    });
    workplace.setTree(tree);

    function setTools(table) {
        activeTable = table;
        place(workplace.tools, table ? [subjectTools] : []);
        place(workplace.find, table ? [table.findField] : []);
    }

    /** {@return the devices a node of the tree lists: those below it, or the ones a dynamic folder's rule picks} */
    function devicesOf(node) {
        if (node.kind === 'folders') {
            return [];
        }
        if (node.kind === 'folder' && node.rule) {
            if (node.rule === 'filter') {
                return allDevices.filter((device) => device.filterDevice === true);
            }
            // a device that could not be read is in neither folder: nothing is known of it
            return ready() ? allDevices.filter((device) => !isUnread(device) && inOpenhab(device) === (node.rule === 'in')) : [];
        }
        return devicesUnder(node).sort(deviceOrder);
    }

    function waiting(node) {
        return node?.kind === 'folder' && (node.rule === 'in' || node.rule === 'out') && !ready();
    }

    function tabsOf() {
        if (mode === 'devices') {
            const known = placeDevices.every((device) => details.has(idOf(device)));
            const objectCount = known ? placeDevices.reduce((sum, device) => sum + listOf(details.get(idOf(device)).groupObjects).length, 0) : placeDevices.reduce((sum, device) => sum + count(device.objects), 0);
            return [
                placeSpaces.length > 0 ? { key: 'spaces', label: text(spaceKind(placeNode.space?.type) === 'floor' ? 'buildings.rooms' : 'buildings.parts'), count: placeSpaces.length } : null,
                { key: 'devices', label: text('devices.title'), count: placeDevices.length },
                { key: 'objects', label: text('device.objects'), count: objectCount },
                placeFunctions.length > 0 ? { key: 'functions', label: text('buildings.functions'), count: placeFunctions.length } : null,
            ].filter(Boolean);
        }
        if (mode === 'device') {
            return [
                { key: 'objects', label: text('device.objects'), count: detail ? listOf(detail.groupObjects).length : undefined },
                { key: 'channels', label: text('device.channels'), count: detail && bookLoaded ? channelAddresses.length : undefined },
            ];
        }
        if (mode === 'function') {
            return [{ key: 'addresses', label: text('column.groupAddresses'), count: openedFunction ? listOf(openedFunction.groupAddresses).length : 0 }];
        }
        return [];
    }

    function drawTabs() {
        workplace.setTabs(tabsOf(), mode === 'function' ? 'addresses' : tab, pickTab);
    }

    function pickTab(key) {
        if (key === tab || mode === 'function') {
            return;
        }
        tab = key;
        rowKey = '';
        for (const table of [spaceTable, deviceTable, functionTable, placeObjects.table, objects.table, channelTable]) {
            table.select(undefined);
        }
        showList();
        if (mode === 'devices') {
            showNode(placeNode);
        } else {
            showDetail(null);
        }
        context.replace?.(selection(context.route, selected, '', tabName()));
    }

    /** Takes the tab the address names; the row it names decides when the two disagree, and no tab is the first. */
    function chooseTab(wanted) {
        if (mode === 'device') {
            tab = rowKey.startsWith('ga:') ? 'channels' : rowKey.startsWith('object:') || !DEVICE_TABS.includes(wanted) ? 'objects' : wanted;
        } else if (mode === 'devices') {
            tab = rowKey.startsWith('function:') ? 'functions' : rowKey.startsWith('object:') ? 'objects' : rowKey.startsWith('device:') ? 'devices' : rowKey.startsWith('space:') || !PLACE_TABS.includes(wanted) ? firstPlaceTab() : wanted;
            if (tab === 'spaces' && placeSpaces.length === 0) {
                tab = 'devices';
            }
        }
    }

    /** Puts the list of the current tab into the panel. */
    function showList() {
        if (mode === 'devices') {
            const table = tab === 'spaces' ? spaceTable : tab === 'objects' ? placeObjects.table : tab === 'functions' ? functionTable : deviceTable;
            // the objects of every device below are many rows: they are made when their tab is shown
            placeObjects.setDevices(tab === 'objects' ? placeDevices.map((device) => details.get(idOf(device))).filter((known) => known !== undefined) : [], !detailsLoaded, rowKey);
            setTools(table);
            drawUnread();
            place(workplace.list, placeNode?.kind === 'folders' ? [foldersNote] : unreadBox.firstChild ? [table.node, unreadBox] : [table.node]);
        } else if (mode === 'device') {
            const table = tab === 'channels' ? channelTable : objects.table;
            setTools(table);
            place(workplace.list, detail === null ? [deviceNote] : tab === 'channels' ? [channelTable.node, channelsNote] : [objects.table.node]);
            if (detail !== null && tab === 'channels') {
                askUses();
            }
        } else if (mode === 'function') {
            setTools(functionAddressTable);
            place(workplace.list, [functionAddressTable.node]);
        }
        drawTabs();
        redrawStatus();
    }

    function drawCrumbs() {
        const path = tree.path(selected);
        const crumbs = path.map((node) => ({ key: node.key, label: [textOf(node.address), textOf(node.name)].filter((part) => part !== '').join(' '), icon: node.icon, iconTitle: node.iconTitle }));
        if (crumbs.length === 0) {
            crumbs.push({ key: '', label: panel.title });
            // a device the tree does not hold, or a selection it does not know
            if (mode === 'device' || mode === 'unknown') {
                crumbs.push({ key: selected, label: statusLabel || selected });
            }
        }
        workplace.setCrumbs(crumbs, (key) => {
            if (key !== selected) {
                go(key, element.contains(document.activeElement));
            }
        });
    }

    function functionSections(fn) {
        return [
            {
                rows: [
                    [text('column.name'), textOf(fn.name)],
                    [text('column.type'), textOf(fn.type)],
                ],
            },
            { tab: 'information', rows: [[text('column.groupAddresses'), String(listOf(fn.groupAddresses).length)]] },
        ];
    }

    function addressRoute(address) {
        return address === '' ? null : { view: 'group-addresses', bridge: bridge.uid, query: { sel: addressKey(address) } };
    }

    /** The address in the other panel of the workplace; in this one, as Group Addresses, when there is no other. */
    function showAddressBeside(address) {
        const route = addressRoute(address);
        if (route) {
            (context.jump ?? navigate)(route);
        }
    }

    /** What the openHAB and Live containers show; drawn again when the lists it needs have come. */
    function setSide(draw) {
        lateSide = draw;
        draw();
    }

    function addressFact(address) {
        const item = book.get(address);
        return withOpenhab && bookLoaded && item ? [[text('addresses.channel'), text(addressInOpenhab(item) ? 'addresses.configured' : 'addresses.notConfigured')]] : [];
    }

    /** @param {string} [wizardId] what names the device for the wizard, when that is not its id */
    function deviceSide(device, more = () => [], wizardId = idOf(device)) {
        const id = idOf(device);
        setSide(() => {
            const known = (detail !== null && idOf(detail) === id ? detail : null) ?? details.get(id);
            const linked = known ? linkedAddresses(known) : [];
            const used = linked.filter((address) => addressInOpenhab(book.get(address))).length;
            workplace.openhab.show(withOpenhab && known && bookLoaded ? [[text('addresses.channel'), text('device.inOpenhab', used, linked.length)]] : [], [wizardLink({ device: wizardId }), ...more()]);
            workplace.live.watch({ source: textOf(device.address), addresses: linked });
        });
    }

    /** Properties of a group address of the opened function; of the function itself when no row is selected. */
    function showFunctionAddress(entry) {
        if (!entry) {
            if (openedFunction) {
                showFunction(openedFunction);
            }
            return;
        }
        const address = textOf(entry.groupAddress);
        const route = addressRoute(address);
        workplace.properties.show(
            [address, textOf(entry.name)].filter((part) => part !== '').join(' '),
            [
                {
                    rows: [
                        [text('column.address'), address || text('app.none')],
                        [text('column.name'), textOf(entry.name)],
                        [text('column.role'), textOf(entry.role)],
                    ],
                },
            ],
            [route ? openLink(route) : null],
        );
        setSide(() => {
            workplace.openhab.show(addressFact(address), [openedFunction && textOf(openedFunction.id) !== '' ? wizardLink({ function: openedFunction.id }) : null]);
            workplace.live.watch({ addresses: address === '' ? [] : [address] });
        });
    }

    /** @param {boolean} [row] the function is a row of the list and can be opened */
    function showFunction(fn, row = false) {
        setSubject(textOf(fn.id) === '' ? null : { function: fn.id });
        workplace.properties.show(textOf(fn.name), functionSections(fn), [row && textOf(fn.id) !== '' ? openLink(selection(context.route, `function:${fn.id}`)) : null]);
        setSide(() => {
            workplace.openhab.show([], [textOf(fn.id) === '' ? null : wizardLink({ function: fn.id })]);
            workplace.live.watch({ addresses: listOf(fn.groupAddresses).map((entry) => textOf(entry.groupAddress)).filter((address) => address !== '') });
        });
    }

    function showRowDevice(device) {
        if (!device) {
            showNode(placeNode);
            return;
        }
        setSubject({ device: idOf(device) });
        workplace.properties.show(nameOf(device), deviceSections(document, device), [openLink(routeOf(device))]);
        deviceSide(device);
    }

    /**
     * Properties for a node of the tree that is no device.
     *
     * @param {boolean} [row] the node is a row of the list and can be opened
     */
    function showNode(node, row = false) {
        if (!node || node.key === '' || node.kind === 'folders') {
            setSubject(null);
            workplace.properties.empty();
            setSide(() => {
                workplace.openhab.empty();
                workplace.live.watch(null);
            });
            return;
        }
        if (node.kind === 'function') {
            showFunction(node.fn);
            return;
        }
        const space = node.space;
        const id = space ? textOf(space.id) : '';
        setSubject(id === '' ? null : { space: id });
        workplace.properties.show([node.address, node.name].filter((part) => part !== '').join(' '), [
            {
                rows: [
                    [text('column.address'), node.address],
                    [text('column.name'), node.name],
                    [text('buildings.type'), space ? spaceLook(space).title : null],
                    [text('buildings.number'), space ? textOf(space.number) : null],
                    [text('column.usage'), space ? textOf(space.usage) : null],
                ],
            },
            { tab: 'information', rows: [[text('devices.title'), waiting(node) ? null : String(devicesOf(node).length)]] },
        ], [row ? openLink(selection(context.route, node.key)) : null]);
        setSide(() => {
            // of a device whose objects could not be read nothing is known: it counts on neither side
            const under = devicesOf(node).filter((device) => !isUnread(device));
            const linked = [...new Set(under.flatMap((device) => linkedAddresses(details.get(idOf(device)))))];
            const facts = [
                [text('devices.title'), text('device.devicesInOpenhab', under.filter(inOpenhab).length, under.length)],
                [text('view.group-addresses'), text('device.inOpenhab', linked.filter((address) => addressInOpenhab(book.get(address))).length, linked.length)],
            ];
            workplace.openhab.show(withOpenhab && ready() ? facts : [], [id === '' ? null : wizardLink({ space: id })]);
            workplace.live.watch(null);
        });
    }

    function objectTitle(object) {
        return [Number.isInteger(object.number) ? String(object.number) : '', textOf(object.name)].filter((part) => part !== '').join(' ');
    }

    /** Properties for the open device, or for the selected group object of it. */
    function showDetail(row) {
        if (detail === null) {
            return;
        }
        if (row) {
            workplace.properties.show(objectTitle(row.object) || nameOf(detail), [...objectSections(row.object), { tab: 'information', rows: [[text('column.device'), nameOf(detail)], [text('column.product'), textOf(detail.product)]] }]);
        } else {
            workplace.properties.show(nameOf(detail), deviceSections(document, detail));
        }
        deviceSide(detail, () => suggestionParts, detail.wanted);
    }

    /** Properties for a group object in the list of a place, where it stands under its device. */
    function showPlaceObject(row) {
        if (!row) {
            showNode(placeNode);
            return;
        }
        setSubject({ device: idOf(row.device) });
        workplace.properties.show(
            objectTitle(row.object) || nameOf(row.device),
            [...objectSections(row.object), { tab: 'information', rows: [[text('column.device'), nameOf(row.device)], [text('column.product'), textOf(row.device.product)]] }],
            [openLink(routeOf(row.device))],
        );
        deviceSide(row.device);
    }

    /** Properties for a group address of the open device that a channel uses. */
    function showChannel(row) {
        if (!row) {
            showDetail(null);
            return;
        }
        workplace.properties.show(
            [row.address, textOf(row.item.name)].filter((part) => part !== '').join(' '),
            [
                {
                    rows: [
                        [text('column.address'), row.address],
                        [text('column.name'), textOf(row.item.name)],
                        [text('column.description'), textOf(row.item.description)],
                        [text('column.dataType'), textOf(row.item.dataType)],
                    ],
                },
                { tab: 'comments', rows: [[text('column.description'), textOf(row.item.description)]] },
                { tab: 'information', rows: [[text('column.device'), nameOf(detail)], [text('column.objects'), row.objects.join(', ')]] },
            ],
            [openLink(addressRoute(row.address))],
        );
        if (withOpenhab && !uses.has(row.address)) {
            askUses([row.address]);
        }
        setSide(() => {
            workplace.openhab.show(addressFact(row.address), [...(withOpenhab ? usesBlock(document, root, uses.get(row.address)?.answer) : []), wizardLink({ device: detail.wanted })]);
            workplace.live.watch({ addresses: [row.address] });
        });
    }

    function drawValues() {
        pending = false;
        if (closed) {
            return;
        }
        if (mode === 'device') {
            objects.refresh();
        } else if (mode === 'devices' && tab === 'objects') {
            placeObjects.refresh();
        }
    }

    function request() {
        if (!pending && !closed) {
            pending = true;
            frame(drawValues);
        }
    }

    /** The addresses of the open device that a channel uses, each with the numbers of the objects linked to it. */
    function fillChannels() {
        const byAddress = new Map();
        for (const object of detail ? listOf(detail.groupObjects) : []) {
            for (const address of Array.isArray(object.groupAddresses) ? object.groupAddresses : []) {
                const item = book.get(address);
                if (!addressInOpenhab(item)) {
                    continue;
                }
                if (!byAddress.has(address)) {
                    byAddress.set(address, { key: addressKey(address), address, item, objects: [] });
                }
                if (Number.isInteger(object.number) && !byAddress.get(address).objects.includes(object.number)) {
                    byAddress.get(address).objects.push(object.number);
                }
            }
        }
        channelAddresses = [...byAddress.values()];
        channelRows = channelAddresses.flatMap((entry) => {
            const state = uses.get(entry.address);
            const list = state?.answer?.uses ?? [];
            if (list.length === 0) {
                return [{ ...entry, use: null, asked: state !== undefined, answer: state?.answer, version: state?.answer ?? state ?? entry.item }];
            }
            // the first row of an address carries the key the page address names
            return list.map((use, at) => ({ ...entry, key: at === 0 ? entry.key : `${entry.key}|${at}`, use, version: use }));
        });
        channelTable.setRows(channelRows, { none: text(bookLoaded ? 'device.noChannels' : 'app.loading'), noMatch: text('addresses.noMatch') });
        drawUses();
    }

    /** Under the Channels list: for how many of the addresses the channels were read, with "Show more" for the others. */
    function drawUses() {
        const read = channelAddresses.filter((entry) => uses.get(entry.address)?.answer !== undefined).length;
        const hadFocus = document.activeElement === usesMore;
        clear(usesState);
        add(document, usesState, text('use.read', read, channelAddresses.length));
        const left = channelAddresses.length > usesWanted;
        // empty, the style sheet takes the line away
        place(channelsNote, channelAddresses.length === 0 ? [] : left ? [usesState, usesMore] : [usesState]);
        if (hadFocus && !left) {
            focusList();
        }
    }

    /**
     * Asks which channels hold the addresses of the open device (call 23): the first ones of the Channels list, or
     * the ones named. Each answer is drawn as it comes.
     */
    function askUses(named) {
        if (!withOpenhab || detail === null) {
            return;
        }
        const mine = turn;
        const held = uses;
        const wanted = (named ?? channelAddresses.slice(0, usesWanted).map((entry) => entry.address)).filter((address) => !held.has(address));
        if (wanted.length === 0) {
            return;
        }
        for (const address of wanted) {
            held.set(address, {});
        }
        fillChannels();
        readUses(
            (address) => api.groupAddress(bridge.uid, address),
            wanted,
            (address, answer) => {
                held.get(address).answer = answer;
                if (closed || mine !== turn || held !== uses) {
                    return;
                }
                const selectedRow = channelTable.selected();
                fillChannels();
                if (selectedRow?.address === address) {
                    lateSide?.();
                }
            },
        );
    }

    /** Under the list of an openHAB folder: the devices that are in neither, because their objects could not be read. */
    function drawUnread() {
        clear(unreadBox);
        const shown = mode === 'devices' && placeNode?.kind === 'folder' && (placeNode.rule === 'in' || placeNode.rule === 'out') && unread.length > 0;
        if (shown) {
            add(
                document,
                unreadBox,
                el(document, 'p', { class: 'note' }, text('folder.unread', unread.length)),
                el(
                    document,
                    'ul',
                    {},
                    unread.map((device) => el(document, 'li', {}, el(document, 'a', { href: href(routeOf(device)) }, nameOf(device)))),
                ),
            );
        }
    }

    /** Fills the lists of the selected place from what is known now. */
    function fillPlace() {
        const node = placeNode;
        const all = node === null || node.key === '';
        placeDevices = all ? devices : devicesOf(node);
        placeFunctions = all ? [] : functionsUnder(node);
        if (tab === 'functions' && placeFunctions.length === 0) {
            tab = firstPlaceTab();
        }
        spaceTable.setRows(placeSpaces, { none: text('buildings.noSpacesHere'), noMatch: text('buildings.noSpaceMatch') });
        functionTable.setRows(placeFunctions);
        deviceTable.setRows(placeDevices, { none: text(waiting(node) ? 'app.loading' : all && panel.emptyDevices ? panel.emptyDevices : 'devices.none'), noMatch: text('devices.noMatch') });
    }

    /** The lists of every device (call 11) and of the group addresses (call 9) came: what hangs on them is drawn again. */
    function onLists() {
        stateVersion++;
        objects.setNames([...book.values()]);
        placeObjects.setNames([...book.values()]);
        deviceTable.refresh();
        spaceTable.refresh();
        if (mode === 'devices') {
            fillPlace();
            showList();
            if (rowKey !== '' && activeTable?.selected() === null) {
                restoreRow();
            }
            settleFocus();
        } else if (mode === 'device') {
            fillChannels();
            drawTabs();
            if (tab === 'channels') {
                askUses();
            }
            if (rowKey !== '' && tab === 'channels' && channelTable.selected() === null) {
                restoreRow();
            }
        }
        lateSide?.();
    }

    async function loadLists() {
        if (!rows) {
            return;
        }
        const [names, read] = await Promise.allSettled([rows.groupAddresses(), rows.deviceDetails(allDevices)]);
        if (closed) {
            return;
        }
        if (names.status === 'fulfilled') {
            book = addressBook(names.value);
            bookLoaded = true;
        } else {
            showError(names.reason);
        }
        if (read.status === 'fulfilled') {
            details = new Map(read.value.devices.map((device) => [idOf(device), device]));
            unread = [...read.value.failed].sort(deviceOrder);
            detailsLoaded = true;
            if (read.value.problems.length > 0) {
                showError(read.value.problems[0]);
            }
        } else {
            showError(read.reason);
        }
        onLists();
    }

    async function loadSuggestions(mine, wanted) {
        suggestionParts = [];
        let found;
        try {
            found = await api.deviceChannels(bridge.uid, wanted);
        } catch (error) {
            if (closed || mine !== turn) {
                return;
            }
            suggestionParts = [el(document, 'h5', {}, text('device.suggestions')), el(document, 'p', { class: 'note' }, errorText(error?.code, error?.text))];
            lateSide?.();
            return;
        }
        if (closed || mine !== turn) {
            return;
        }
        const lines = (Array.isArray(found) ? found : []).map((suggestion) => suggestion?.text).filter((line) => typeof line === 'string');
        if (lines.length === 0) {
            suggestionParts = [el(document, 'h5', {}, text('device.suggestions')), el(document, 'p', { class: 'note' }, text('device.noSuggestions'))];
        } else {
            copyBox.field.value = lines.join('\n');
            clear(copyBox.state);
            suggestionParts = [el(document, 'h5', {}, text('device.suggestions')), copyBox.field, copyBox.tools];
        }
        lateSide?.();
    }

    async function openDevice(wanted, known) {
        const mine = ++turn;
        mode = 'device';
        detail = null;
        suggestionParts = [];
        channelRows = [];
        channelAddresses = [];
        uses = new Map();
        usesWanted = USES_ROWS;
        chooseTab(tab);
        clear(deviceNote);
        objects.table.select(undefined);
        channelTable.select(undefined);
        objects.setObjects([]);
        statusLabel = known ? nameOf(known) : wanted;
        setSubject({ device: wanted });
        showList();
        if (known) {
            workplace.properties.show(statusLabel, deviceSections(document, known));
            deviceSide(known);
        } else {
            workplace.properties.empty();
            setSide(() => {
                workplace.openhab.empty();
                workplace.live.watch(null);
            });
        }
        drawCrumbs();
        let device;
        try {
            device = await api.device(bridge.uid, wanted);
        } catch (error) {
            if (closed || mine !== turn) {
                return;
            }
            const missing = projectMissing(document, bridge, error?.code);
            if (missing) {
                add(document, deviceNote, missing);
            } else if (error?.code === 'unknown-device') {
                add(document, deviceNote, el(document, 'p', { class: 'note' }, errorText(error.code)));
            } else {
                showError(error);
            }
            return;
        }
        if (closed || mine !== turn || typeof device !== 'object' || device === null) {
            return;
        }
        detail = { ...device, wanted };
        statusLabel = nameOf(device) || wanted;
        objects.setObjects(device.groupObjects);
        fillChannels();
        showList();
        showDetail(null);
        drawCrumbs();
        restoreRow();
        settleFocus();
        loadSuggestions(mine, wanted);
    }

    function openFunction(node) {
        turn++;
        mode = 'function';
        statusLabel = node.name;
        openedFunction = node.fn;
        const entries = listOf(node.fn.groupAddresses);
        // an entry without an address has no name for the page address; the same address twice gets a number
        entryKeys = new Map();
        const taken = new Set();
        for (const entry of entries) {
            const address = textOf(entry.groupAddress);
            let key = `fga:${address}`;
            for (let n = 2; taken.has(key); n++) {
                key = `fga:${address}:${n}`;
            }
            if (address !== '') {
                taken.add(key);
                entryKeys.set(entry, key);
            }
        }
        functionAddressTable.select(undefined);
        functionAddressTable.setRows(entries, { none: text('buildings.noFunctionAddresses'), noMatch: text('addresses.noMatch') });
        showList();
        showFunction(node.fn);
        drawCrumbs();
        restoreRow();
    }

    function openPlace(node) {
        turn++;
        mode = 'devices';
        placeNode = node;
        const all = node === null || node.key === '';
        placeSpaces = node?.kind === 'space' ? node.children.filter((child) => child.kind === 'space') : [];
        chooseTab(tab);
        statusLabel = all ? panel.title : [node.address, node.name].filter((part) => part !== '').join(' ');
        for (const table of [spaceTable, deviceTable, functionTable, placeObjects.table]) {
            table.select(undefined);
        }
        fillPlace();
        showList();
        showNode(node);
        drawCrumbs();
        restoreRow();
    }

    function openUnknown(key) {
        turn++;
        mode = 'unknown';
        statusLabel = key;
        setSubject(null);
        setTools(null);
        place(workplace.list, [unknownNote]);
        workplace.setTabs([], '');
        workplace.properties.empty();
        setSide(() => {
            workplace.openhab.empty();
            workplace.live.watch(null);
        });
        drawCrumbs();
        redrawStatus();
    }

    /** Selects the row the address names in the list that is shown; no name selects none. */
    function restoreRow(always = false) {
        if (rowKey === '' && !always) {
            return;
        }
        if (mode === 'devices') {
            if (tab === 'spaces') {
                const space = spaceTable.select(rowKey);
                showNode(space ?? placeNode, space !== null && space !== undefined);
            } else if (tab === 'functions') {
                const fn = functionTable.select(rowKey);
                if (fn) {
                    showFunction(fn, true);
                } else {
                    showNode(placeNode);
                }
            } else if (tab === 'objects') {
                placeObjects.reveal(rowKey);
                showPlaceObject(placeObjects.table.select(rowKey));
            } else {
                showRowDevice(deviceTable.select(rowKey));
            }
        } else if (mode === 'function') {
            showFunctionAddress(functionAddressTable.select(rowKey));
        } else if (mode === 'device' && detail !== null) {
            if (tab === 'channels') {
                showChannel(channelTable.select(rowKey));
            } else {
                showDetail(objects.table.select(rowKey.startsWith('object:') ? Number(rowKey.slice('object:'.length)) : undefined));
            }
        }
    }

    /** Shows what the key names: a place with its devices, a function, or a device with its group objects. */
    function apply(key, row = '', wantedTab = '') {
        selected = key;
        rowKey = row;
        tab = wantedTab;
        if (!loaded) {
            return;
        }
        let node = tree.select(key);
        if (key.startsWith('device:')) {
            const wanted = key.slice('device:'.length);
            // an address of the page before the panels names a device by its individual address
            const known = allDevices.find((device) => String(device.id) === wanted) ?? allDevices.find((device) => device.address === wanted) ?? null;
            if (node === null && known) {
                node = tree.select(deviceKey(known));
            }
            openDevice(wanted, known);
            return;
        }
        if (node && node.kind === 'function') {
            openFunction(node);
        } else if (node === null && key !== '') {
            openUnknown(key);
        } else {
            openPlace(node);
        }
    }

    /** @param {boolean} [focus] the focus was on what is left behind: it goes to the new list */
    function go(key, focus = false) {
        // going to the address already shown would make the shell build the panel again
        const moved = key !== selected;
        const hadRow = rowKey !== '' || tabName() !== '';
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

    async function load() {
        let found;
        try {
            found = await panel.load();
        } catch (error) {
            if (closed) {
                return;
            }
            const missing = projectMissing(document, bridge, error?.code);
            if (missing) {
                add(document, workplace.notes, missing);
            } else {
                showError(error);
            }
            return;
        }
        if (closed) {
            return;
        }
        const problems = Array.isArray(found.problems) ? found.problems : [];
        for (const problem of problems) {
            const missing = projectMissing(document, bridge, problem?.code);
            if (missing) {
                add(document, workplace.notes, missing);
                return;
            }
        }
        for (const problem of problems) {
            add(document, workplace.notes, el(document, 'p', { class: 'note' }, errorText(problem?.code, problem?.text)));
        }
        devices = listOf(found.devices).sort(deviceOrder);
        allDevices = devices;
        const roots = Array.isArray(found.roots) ? found.roots : [];
        const folders = [
            ...(Array.isArray(found.folders) ? found.folders : []),
            ...(withOpenhab ? [folderNode('folder:in-openhab', text('folder.inOpenhab'), { rule: 'in' }), folderNode('folder:not-in-openhab', text('folder.notInOpenhab'), { rule: 'out' })] : []),
        ];
        // the dynamic folders are no part of the project: they stand as a group of their own below it
        tree.setRoots([{ key: '', kind: 'root', icon: panel.icon, address: '', name: panel.title, children: roots }, folderGroup(folders)]);
        if (roots.length === 0 && panel.emptyNote && problems.length === 0) {
            add(document, workplace.notes, el(document, 'p', { class: 'note' }, text(panel.emptyNote)));
        }
        loaded = true;
        apply(selected, rowKey, tab);
        settleFocus();
        // the last values of the lists and the Live container come from the stream
        if (rows) {
            leaveRows = rows.subscribe(() => request());
        }
        if (live) {
            stream?.start();
        }
        loadLists();
    }

    workplace.setCrumbs([{ key: '', label: panel.title }]);

    const onMore = () => {
        usesWanted += USES_ROWS;
        drawUses();
        askUses();
    };
    usesMore.addEventListener('click', onMore);

    load().catch((error) => {
        if (!closed) {
            showError(error);
        }
    });

    return {
        update(route) {
            context.route = route;
            const key = textOf(route?.query?.sel);
            const row = textOf(route?.query?.row);
            const wantedTab = textOf(route?.query?.tab);
            // Back, or "Open" in Properties, takes away what held the focus
            const focusWas = workplace.holds(document.activeElement);
            if (key !== selected || !loaded) {
                apply(key, row, wantedTab);
            } else if (row !== rowKey || wantedTab !== tabName()) {
                // the same selection with another row or tab: nothing is read again
                rowKey = row;
                const before = tab;
                chooseTab(wantedTab);
                if (mode === 'devices' && tab === 'functions' && placeFunctions.length === 0) {
                    tab = firstPlaceTab();
                }
                if (tab !== before) {
                    showList();
                }
                restoreRow(true);
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
            leaveRows?.();
            usesMore.removeEventListener('click', onMore);
            copyBox.close();
            workplace.close();
            tree.close();
            for (const table of [spaceTable, deviceTable, functionTable, functionAddressTable, objects.table, placeObjects.table, channelTable]) {
                table.close();
            }
        },
    };
}
