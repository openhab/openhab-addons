// What the panels' trees and lists are made of, built from the answers of calls 9, 10, 11, 14 and 15. Nothing here
// touches the document. A tree node is {key, kind, icon, address, name, mark?, children} plus the answer's own
// object; its key is also the "sel" value of the panel's address.

import { text } from './texts.js';

const INDIVIDUAL = /^(\d{1,2})\.(\d{1,2})\.(\d{1,3})$/;

function textOf(value) {
    return typeof value === 'string' ? value : '';
}

function listOf(value) {
    return Array.isArray(value) ? value.filter((item) => typeof item === 'object' && item !== null) : [];
}

function individualNumber(address) {
    const match = INDIVIDUAL.exec(textOf(address));
    return match ? Number(match[1]) * 65536 + Number(match[2]) * 256 + Number(match[3]) : Infinity;
}

/** Sorts devices by individual address; devices without one come last, by name. */
export function deviceOrder(a, b) {
    const difference = individualNumber(a.address) - individualNumber(b.address);
    if (difference < 0 || difference > 0) {
        return difference;
    }
    const left = textOf(a.name).toLowerCase();
    const right = textOf(b.name).toLowerCase();
    return left < right ? -1 : left > right ? 1 : 0;
}

/**
 * {@return the key of the range with these names, outermost first}
 *
 * @param {Array<string>} path
 * @param {string} [prefix] the range's number, which tells two ranges of one name apart; none in a free-style project
 */
export function rangeKey(path, prefix = '') {
    const names = `range:${path.map((part) => encodeURIComponent(part)).join('/')}`;
    return prefix === '' ? names : `${names}@${prefix}`;
}

/** {@return the first range a key without a number names, or null} */
export function rangeByNames(nodes, key) {
    for (const node of nodes) {
        if (node.kind !== 'range') {
            continue;
        }
        const found = node.key.startsWith(`${key}@`) ? node : rangeByNames(node.children, key);
        if (found) {
            return found;
        }
    }
    return null;
}

export function addressKey(address) {
    return `ga:${address}`;
}

export function deviceKey(device) {
    return `device:${String(device.id ?? device.address)}`;
}

/** {@return every group address below the node, in the order of the tree} */
export function addressesUnder(node) {
    if (node.kind === 'ga') {
        return [node.item];
    }
    return node.children.flatMap(addressesUnder);
}

/**
 * The group addresses as the project nests them: each address under the ranges its "range" names, however many
 * there are. An address without a range goes under one node of its own.
 *
 * @param {Array<Object>} addresses the list of call 9
 * @returns {Array<Object>} the outermost ranges
 */
export function groupTree(addresses) {
    const roots = [];
    const ranges = new Map();
    let loose = null;
    for (const item of listOf(addresses)) {
        const path = Array.isArray(item.range) ? item.range.filter((part) => typeof part === 'string') : [];
        let siblings = roots;
        if (path.length === 0) {
            if (loose === null) {
                loose = { key: 'range:', kind: 'range', icon: 'range', address: '', name: text('addresses.noRange'), children: [], depth: 0 };
            }
            siblings = loose.children;
        }
        const parts = String(item.address).split('/');
        for (let depth = 1; depth <= path.length; depth++) {
            const prefix = parts.length > depth ? parts.slice(0, depth).join('/') : '';
            const key = rangeKey(path.slice(0, depth), prefix);
            let range = ranges.get(key);
            if (!range) {
                range = { key, kind: 'range', icon: 'range', address: prefix, name: path[depth - 1], children: [], depth };
                ranges.set(key, range);
                // ranges stay ahead of the addresses beside them
                const at = siblings.findIndex((node) => node.kind === 'ga');
                siblings.splice(at < 0 ? siblings.length : at, 0, range);
            }
            siblings = range.children;
        }
        siblings.push({ key: addressKey(item.address), kind: 'ga', icon: 'address', address: String(item.address), name: textOf(item.name), children: [], item });
    }
    return loose === null ? roots : [...roots, loose];
}

function deviceNode(device) {
    return {
        key: deviceKey(device),
        kind: 'device',
        icon: 'device',
        address: textOf(device.address),
        name: textOf(device.name) || String(device.id ?? ''),
        mark: device.filterDevice === true ? text('device.filter') : undefined,
        children: [],
        device,
    };
}

/** {@return every device below the node, each once, in the order of the tree} */
export function devicesUnder(node) {
    const found = new Set();
    const visit = (at) => {
        if (at.kind === 'device') {
            found.add(at.device);
        }
        at.children.forEach(visit);
    };
    visit(node);
    return [...found];
}

/** {@return every function below the node, in the order of the tree} */
export function functionsUnder(node) {
    return node.kind === 'function' ? [node.fn] : node.children.flatMap(functionsUnder);
}

/**
 * Areas, lines and devices, placed by the devices' individual addresses (1.1.5 is on line 1.1 of area 1); call 14
 * gives the names and the areas and lines that hold no device. A device without an address goes into the folder
 * "Not assigned to a line".
 *
 * @param topology the answer of call 14
 * @returns {{roots: Array<Object>, devices: Array<Object>, unassigned: Object}} the areas, every device by address,
 *     and the folder of the devices without one
 */
export function topologyTree(topology) {
    const areas = new Map();
    const lines = new Map();
    const areaOf = (number) => {
        if (!areas.has(number)) {
            areas.set(number, { key: `area:${number}`, kind: 'area', icon: 'area', address: String(number), name: '', children: [], number });
        }
        return areas.get(number);
    };
    const lineOf = (area, line) => {
        const address = `${area}.${line}`;
        if (!lines.has(address)) {
            const node = { key: `line:${address}`, kind: 'line', icon: 'line', address, name: '', children: [], number: line };
            lines.set(address, node);
            areaOf(area).children.push(node);
        }
        return lines.get(address);
    };
    const byId = new Map();
    const take = (device) => {
        const id = String(device.id ?? device.address);
        if (!byId.has(id)) {
            byId.set(id, device);
        }
    };
    for (const area of listOf(topology?.areas)) {
        const number = Number(area.address);
        if (Number.isInteger(number)) {
            areaOf(number).name = textOf(area.name);
        }
        for (const line of listOf(area.lines)) {
            const parts = textOf(line.address).split('.').map(Number);
            if (parts.length === 2 && parts.every(Number.isInteger)) {
                lineOf(parts[0], parts[1]).name = textOf(line.name);
            }
            listOf(line.devices).forEach(take);
        }
    }
    listOf(topology?.unplacedDevices).forEach(take);
    const devices = [...byId.values()].sort(deviceOrder);
    const loose = [];
    for (const device of devices) {
        const match = INDIVIDUAL.exec(textOf(device.address));
        if (match) {
            lineOf(Number(match[1]), Number(match[2])).children.push(deviceNode(device));
        } else {
            loose.push(deviceNode(device));
        }
    }
    const roots = [...areas.values()].sort((a, b) => a.number - b.number);
    for (const area of roots) {
        area.children.sort((a, b) => a.number - b.number);
    }
    return { roots, devices, unassigned: folderNode('unassigned', text('topology.notAssigned'), { children: loose }) };
}

/**
 * {@return a dynamic folder: a ready-made list among the tree's first nodes}
 *
 * @param {Object} [more] children, the nodes that stand only here; or members, a call that gives the folder's
 *     devices or addresses when it is selected; loading, true while members cannot be told yet
 */
export function folderNode(key, name, more = {}) {
    return { key, kind: 'folder', icon: 'folder', address: '', name, children: [], ...more };
}

/** {@return the group "Dynamic Folders" with these folders} */
export function folderGroup(folders) {
    return { key: 'folders', kind: 'folders', icon: 'folder', address: '', name: text('frame.dynamicFolders'), children: folders };
}

/** {@return a tree node for each of the devices that stand nowhere below the roots} */
export function unplacedNodes(roots, devices) {
    const placed = new Set(roots.flatMap(devicesUnder).map((device) => String(device.id ?? device.address)));
    return listOf(devices)
        .filter((device) => !placed.has(String(device.id ?? device.address)))
        .sort(deviceOrder)
        .map(deviceNode);
}

const SPACE_KINDS = Object.freeze({
    building: 'building',
    buildingpart: 'part',
    floor: 'floor',
    stairway: 'stairway',
    corridor: 'corridor',
    room: 'room',
    distributionboard: 'cabinet',
    area: 'ground',
    ground: 'ground',
    segment: 'ground',
});

/** The drawing and the word of each kind of space. */
export const SPACE_LOOKS = Object.freeze({
    building: { icon: 'building', word: 'space.building' },
    part: { icon: 'part', word: 'space.part' },
    floor: { icon: 'floor', word: 'space.floor' },
    stairway: { icon: 'stairway', word: 'space.stairway' },
    corridor: { icon: 'corridor', word: 'space.corridor' },
    room: { icon: 'room', word: 'space.room' },
    cabinet: { icon: 'cabinet', word: 'space.cabinet' },
    ground: { icon: 'ground', word: 'space.ground' },
    other: { icon: 'space', word: 'space.other' },
});

/** {@return the kind of a space, a name of SPACE_LOOKS, from the type the project wrote} */
export function spaceKind(type) {
    const written = textOf(type).toLowerCase();
    return Object.hasOwn(SPACE_KINDS, written) ? SPACE_KINDS[written] : 'other';
}

/** {@return {icon, title} of a space: its kind's drawing and word} */
export function spaceLook(space) {
    const look = SPACE_LOOKS[spaceKind(space?.type)];
    return { icon: look.icon, title: text(look.word) };
}

/**
 * The building structure of call 15 with the devices and functions of each space under it.
 *
 * @param spaces the answer of call 15
 * @param {Array<Object>} devices the devices of call 14; a device the list lacks shows its id
 * @returns {Array<Object>} the outermost spaces
 */
export function buildingTree(spaces, devices) {
    const byId = new Map();
    for (const device of listOf(devices)) {
        byId.set(device.id, device);
    }
    const used = new Set();
    let unnamed = 0;
    const functionNode = (fn) => {
        const id = textOf(fn.id) === '' ? `#${++unnamed}` : fn.id;
        return { key: `function:${id}`, kind: 'function', icon: 'function', address: '', name: textOf(fn.name), mark: textOf(fn.type) || undefined, children: [], fn };
    };
    const spaceNode = (space) => {
        const id = textOf(space.id) === '' ? `#${++unnamed}` : space.id;
        const ids = Array.isArray(space.devices) ? space.devices.filter((entry) => typeof entry === 'string') : [];
        const own = [];
        for (const deviceId of ids) {
            // a device has one place in the tree, as it has one key
            if (!used.has(deviceId)) {
                used.add(deviceId);
                own.push(byId.get(deviceId) ?? { id: deviceId });
            }
        }
        const look = spaceLook(space);
        return {
            key: `space:${id}`,
            kind: 'space',
            icon: look.icon,
            iconTitle: look.title,
            address: '',
            name: textOf(space.name),
            mark: textOf(space.number) || undefined,
            children: [...listOf(space.spaces).map(spaceNode), ...own.sort(deviceOrder).map(deviceNode), ...listOf(space.functions).map(functionNode)],
            space,
        };
    };
    return listOf(spaces).map(spaceNode);
}

/**
 * @param {Array<Object>} details every device with its group objects (call 11)
 * @returns {Map<string, Array<{device: Object, object: Object, at: number}>>} per group address the objects linked
 *     to it, by device address and object number; at is the object's place in its device's list
 */
export function associations(details) {
    const byAddress = new Map();
    for (const device of [...listOf(details)].sort(deviceOrder)) {
        const objects = listOf(device.groupObjects)
            .map((object, at) => ({ object, at }))
            .sort((a, b) => (a.object.number ?? Infinity) - (b.object.number ?? Infinity) || a.at - b.at);
        for (const { object, at } of objects) {
            const addresses = Array.isArray(object.groupAddresses) ? object.groupAddresses.filter((address) => typeof address === 'string') : [];
            for (const address of new Set(addresses)) {
                if (!byAddress.has(address)) {
                    byAddress.set(address, []);
                }
                byAddress.get(address).push({ device, object, at });
            }
        }
    }
    return byAddress;
}

/**
 * What "in openHAB" means, for everything the page shows. A group address: a channel of the bridge uses it (the
 * "configured" of call 9). A group object or a device: at least one of its linked addresses is such an address.
 */
export function addressInOpenhab(item) {
    return item?.configured === true;
}

/** @param {Map<string, Object>} book the group addresses of call 9 by address */
export function objectInOpenhab(object, book) {
    const addresses = Array.isArray(object?.groupAddresses) ? object.groupAddresses : [];
    return addresses.some((address) => addressInOpenhab(book.get(address)));
}

/** {@return the linked group addresses of a device (call 11), each once, in the order of its objects} */
export function linkedAddresses(detail) {
    const found = new Set();
    for (const object of listOf(detail?.groupObjects)) {
        for (const address of Array.isArray(object.groupAddresses) ? object.groupAddresses : []) {
            if (typeof address === 'string') {
                found.add(address);
            }
        }
    }
    return [...found];
}

/** @param detail the device with its group objects (call 11) */
export function deviceInOpenhab(detail, book) {
    return linkedAddresses(detail).some((address) => addressInOpenhab(book.get(address)));
}

/** {@return the group addresses of call 9 by address} */
export function addressBook(addresses) {
    const book = new Map();
    for (const item of listOf(addresses)) {
        if (typeof item.address === 'string') {
            book.set(item.address, item);
        }
    }
    return book;
}

/**
 * Where the project puts a telegram: the function a group address belongs to with the space that function stands in,
 * and the room of each device by its individual address.
 *
 * @param spaces the answer of call 15
 * @param {Array<Object>} devices the devices of call 10
 * @returns {{functionOf: Map<string, {name: string, part: string}>, roomOf: Map<string, string>}}
 */
export function projectPlaces(spaces, devices) {
    const functionOf = new Map();
    const visit = (space) => {
        for (const fn of listOf(space.functions)) {
            for (const entry of listOf(fn.groupAddresses)) {
                // the first function that names an address stands for it
                if (typeof entry.groupAddress === 'string' && !functionOf.has(entry.groupAddress)) {
                    functionOf.set(entry.groupAddress, { name: textOf(fn.name), part: textOf(space.name) });
                }
            }
        }
        listOf(space.spaces).forEach(visit);
    };
    listOf(spaces).forEach(visit);
    const roomOf = new Map();
    for (const device of listOf(devices)) {
        if (textOf(device.address) !== '' && textOf(device.room) !== '') {
            roomOf.set(device.address, device.room);
        }
    }
    return { functionOf, roomOf };
}
