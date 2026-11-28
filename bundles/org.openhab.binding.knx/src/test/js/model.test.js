import { test } from 'node:test';
import assert from 'node:assert/strict';

import { addressBook, addressInOpenhab, addressesUnder, associations, buildingTree, deviceInOpenhab, deviceOrder, devicesUnder, folderGroup, folderNode, functionsUnder, groupTree, linkedAddresses, objectInOpenhab, projectPlaces, rangeByNames, rangeKey, topologyTree, unplacedNodes } from '../web/js/model.js';

function outline(nodes, depth = 0) {
    return nodes.flatMap((node) => [`${'  '.repeat(depth)}${[node.address, node.name].filter((part) => part !== '').join(' ')}`, ...outline(node.children, depth + 1)]);
}

test('model: a three-level project gives main groups, middle groups and addresses, each range with its number', () => {
    const roots = groupTree([
        { address: '31/0/1', name: 'Kitchen', range: ['Lights', 'Ground floor'] },
        { address: '31/0/2', name: 'Hall', range: ['Lights', 'Ground floor'] },
        { address: '31/1/1', name: 'Bedroom', range: ['Lights', 'First floor'] },
    ]);
    assert.deepEqual(outline(roots), ['31 Lights', '  31/0 Ground floor', '    31/0/1 Kitchen', '    31/0/2 Hall', '  31/1 First floor', '    31/1/1 Bedroom']);
    assert.deepEqual(roots[0].children.map((node) => node.key), [rangeKey(['Lights', 'Ground floor'], '31/0'), rangeKey(['Lights', 'First floor'], '31/1')]);
    assert.equal(roots[0].children[0].children[0].key, 'ga:31/0/1');
    assert.deepEqual(addressesUnder(roots[0]).map((item) => item.address), ['31/0/1', '31/0/2', '31/1/1']);
});

test('model: a two-level project has one level of ranges', () => {
    const roots = groupTree([
        { address: '31/1', name: 'One', range: ['Main'] },
        { address: '31/2', name: 'Two', range: ['Main'] },
    ]);
    assert.deepEqual(outline(roots), ['31 Main', '  31/1 One', '  31/2 Two']);
});

test('model: a free-style project nests as deep as its ranges, and a range shows no number', () => {
    const roots = groupTree([
        { address: '63489', name: 'Deep', range: ['A', 'B', 'C', 'D'] },
        { address: '63490', name: 'Shallow', range: ['A'] },
        { address: '63491', name: 'Other', range: ['E'] },
    ]);
    assert.deepEqual(outline(roots), ['A', '  B', '    C', '      D', '        63489 Deep', '  63490 Shallow', 'E', '  63491 Other']);
});

test('model: a range name with a slash keeps its own key, and an address without a range goes under one node', () => {
    const roots = groupTree([
        { address: '31/0/1', name: 'x', range: ['a/b'] },
        { address: '31/0/2', name: 'y', range: ['a', 'b'] },
        { address: '31/0/3', name: 'z', range: [] },
        { address: '31/0/4', name: 'no list' },
    ]);
    assert.deepEqual(roots.map((node) => node.key), ['range:a%2Fb@31', 'range:a@31', 'range:']);
    assert.equal(roots[1].children[0].key, 'range:a/b@31/0');
    assert.deepEqual(outline([roots[2]]), ['Without a group', '  31/0/3 z', '  31/0/4 no list']);
    assert.deepEqual(groupTree(undefined), []);
});

const TOPOLOGY = {
    areas: [
        { address: '7', name: 'North', lines: [{ address: '7.1', name: 'Hall line', devices: [{ id: 'd2', address: '7.1.20', name: 'Dimmer' }, { id: 'd1', address: '7.1.3', name: 'Switch' }] }, { address: '7.4', name: 'Empty line', devices: [] }] },
        { address: '31', name: '', lines: [] },
    ],
    unplacedDevices: [{ id: 'd9', name: 'Spare' }, { id: 'd5', address: '7.2.1', name: 'Sensor' }],
};

test('model: the topology is built from the individual addresses, with the names of call 14', () => {
    const { roots, devices, unassigned } = topologyTree(TOPOLOGY);
    assert.deepEqual(outline(roots), [
        '7 North',
        '  7.1 Hall line',
        '    7.1.3 Switch',
        '    7.1.20 Dimmer',
        '  7.2',
        '    7.2.1 Sensor',
        '  7.4 Empty line',
        '31',
    ]);
    assert.deepEqual(roots.map((node) => node.key), ['area:7', 'area:31']);
    // the devices without an address stand in a dynamic folder, under the key the node had before
    assert.deepEqual(outline([unassigned]), ['Not assigned to a line', '  Spare']);
    assert.deepEqual([unassigned.key, unassigned.kind, unassigned.icon], ['unassigned', 'folder', 'folder']);
    assert.equal(roots[0].children[0].key, 'line:7.1');
    assert.equal(roots[0].children[0].children[0].key, 'device:d1');
    assert.deepEqual(devices.map((device) => device.id), ['d1', 'd2', 'd5', 'd9']);
    assert.deepEqual(devicesUnder(roots[0]).map((device) => device.id), ['d1', 'd2', 'd5']);
});

test('model: without devices that lack an address the folder "Not assigned to a line" is empty, and a filter device carries its label', () => {
    const { roots, unassigned } = topologyTree({ areas: [{ address: '7', name: 'A', lines: [{ address: '7.0', name: 'L', devices: [{ id: 'f', address: '7.0.0', name: 'Dummy', filterDevice: true }] }] }] });
    assert.deepEqual(unassigned.children, []);
    assert.deepEqual(roots.map((node) => node.key), ['area:7']);
    assert.equal(roots[0].children[0].children[0].mark, 'filter device');
    assert.deepEqual(topologyTree(undefined).roots, []);
    assert.deepEqual(topologyTree(undefined).devices, []);
});

test('model: the building tree holds spaces, then the devices and the functions of each space', () => {
    const roots = buildingTree(
        [
            {
                id: 's1',
                name: 'House',
                type: 'Building',
                spaces: [{ id: 's2', name: 'Kitchen', type: 'Room', number: '0.1', devices: ['d2', 'd1', 'gone'], functions: [{ id: 'f1', name: 'Ceiling', type: 'SwitchableLight' }], spaces: [] }],
                devices: ['d2'],
                functions: [],
            },
        ],
        [{ id: 'd1', address: '7.1.3', name: 'Switch' }, { id: 'd2', address: '7.1.20', name: 'Dimmer' }],
    );
    assert.deepEqual(outline(roots), ['House', '  Kitchen', '    7.1.3 Switch', '    gone', '    Ceiling', '  7.1.20 Dimmer']);
    assert.equal(roots[0].icon, 'building');
    assert.equal(roots[0].children[0].icon, 'room');
    assert.equal(roots[0].children[0].mark, '0.1', 'the number; the drawing says the kind');
    assert.deepEqual([roots[0].iconTitle, roots[0].children[0].iconTitle], ['Building', 'Room']);
    assert.deepEqual(roots[0].children[0].children.map((node) => node.key), ['device:d1', 'device:gone', 'function:f1']);
    assert.deepEqual(devicesUnder(roots[0]).map((device) => device.id), ['d1', 'gone', 'd2']);
    assert.deepEqual(functionsUnder(roots[0]).map((fn) => fn.id), ['f1']);
    assert.deepEqual(buildingTree(null, null), []);
});

test('model: the associations of an address are the objects linked to it, by device and object number', () => {
    const byAddress = associations([
        { id: 'd2', address: '7.1.20', groupObjects: [{ number: 4, groupAddresses: ['31/0/1', '31/0/1'] }, { number: 1, groupAddresses: ['31/0/2', '31/0/1'] }] },
        { id: 'd1', address: '7.1.3', groupObjects: [{ number: 0, groupAddresses: ['31/0/1'] }, { groupAddresses: 'none' }] },
    ]);
    assert.deepEqual(byAddress.get('31/0/1').map(({ device, object }) => [device.id, object.number]), [['d1', 0], ['d2', 1], ['d2', 4]]);
    assert.deepEqual(byAddress.get('31/0/2').map(({ device, object }) => [device.id, object.number]), [['d2', 1]]);
    assert.equal(byAddress.has('31/0/3'), false);
});

test('model: the order puts devices without an address last, by name', () => {
    const sorted = [{ name: 'b' }, { address: '7.1.10', name: 'x' }, { name: 'A' }, { address: '7.1.9', name: 'y' }].sort(deviceOrder);
    assert.deepEqual(sorted.map((device) => device.name), ['y', 'x', 'A', 'b']);
});

test('model: two middle groups of one name stay two nodes, each with its number', () => {
    const roots = groupTree([
        { address: '31/1/1', name: 'One', range: ['Lights', 'New middle group'] },
        { address: '31/2/1', name: 'Two', range: ['Lights', 'New middle group'] },
        { address: '31/1/2', name: 'Three', range: ['Lights', 'New middle group'] },
    ]);
    assert.deepEqual(outline(roots), ['31 Lights', '  31/1 New middle group', '    31/1/1 One', '    31/1/2 Three', '  31/2 New middle group', '    31/2/1 Two']);
    assert.deepEqual(roots[0].children.map((node) => node.key), ['range:Lights/New%20middle%20group@31/1', 'range:Lights/New%20middle%20group@31/2']);
});

// two main groups of one name cannot be written here: the fixtures rule allows main group 31 only
test('model: the key of a main group carries its number, also in a two-level project', () => {
    const three = groupTree([{ address: '31/0/1', name: 'One', range: ['Lights', 'Hall'] }]);
    assert.deepEqual(three.map((node) => [node.key, node.address]), [['range:Lights@31', '31']]);
    const two = groupTree([
        { address: '31/1', name: 'One', range: ['Main'] },
        { address: '31/2', name: 'Two', range: ['Main'] },
    ]);
    assert.deepEqual(two.map((node) => [node.key, node.address]), [['range:Main@31', '31']]);
});

test('model: a free-style range is told by its names, and a key without a number names the first range of those names', () => {
    const free = groupTree([
        { address: '63489', name: 'One', range: ['A', 'B'] },
        { address: '63999', name: 'Two', range: ['A', 'B'] },
    ]);
    assert.deepEqual(outline(free), ['A', '  B', '    63489 One', '    63999 Two']);
    assert.equal(free[0].children[0].key, 'range:A/B');
    const roots = groupTree([
        { address: '31/1/1', name: 'One', range: ['Lights', 'Hall'] },
        { address: '31/2/1', name: 'Two', range: ['Lights', 'Hall'] },
        { address: '31/3/1', name: 'Three', range: ['Lights', 'Hall@x'] },
    ]);
    assert.equal(rangeByNames(roots, 'range:Lights').key, 'range:Lights@31');
    assert.equal(rangeByNames(roots, 'range:Lights/Hall').key, 'range:Lights/Hall@31/1');
    assert.equal(rangeByNames(roots, 'range:Lights/Hall%40x').key, 'range:Lights/Hall%40x@31/3');
    assert.equal(rangeByNames(roots, 'range:Lights/Ha'), null);
    assert.equal(rangeByNames(roots, 'range:Gone'), null);
});

test('model: "in openHAB" is one rule: an address a channel uses, an object or a device with at least one such address', () => {
    const book = addressBook([{ address: '31/0/1', configured: true }, { address: '31/0/2', configured: false }, { address: '31/0/3' }, null, { name: 'no address' }]);
    assert.deepEqual([...book.keys()], ['31/0/1', '31/0/2', '31/0/3']);
    assert.equal(addressInOpenhab(book.get('31/0/1')), true);
    assert.equal(addressInOpenhab(book.get('31/0/2')), false);
    assert.equal(addressInOpenhab(book.get('31/0/3')), false);
    assert.equal(addressInOpenhab(undefined), false);
    assert.equal(addressInOpenhab({ configured: 'yes' }), false);
    assert.equal(objectInOpenhab({ groupAddresses: ['31/0/2', '31/0/1'] }, book), true);
    assert.equal(objectInOpenhab({ groupAddresses: ['31/0/2', '31/0/9'] }, book), false);
    assert.equal(objectInOpenhab({}, book), false);
    const used = { id: 'd1', groupObjects: [{ groupAddresses: ['31/0/2'] }, { groupAddresses: ['31/0/3', '31/0/1', '31/0/2'] }] };
    const unused = { id: 'd2', groupObjects: [{ groupAddresses: ['31/0/2'] }, { groupAddresses: [] }, {}] };
    assert.deepEqual(linkedAddresses(used), ['31/0/2', '31/0/3', '31/0/1']);
    assert.equal(deviceInOpenhab(used, book), true);
    assert.equal(deviceInOpenhab(unused, book), false);
    assert.equal(deviceInOpenhab({ id: 'd3' }, book), false);
    assert.equal(deviceInOpenhab(used, new Map()), false);
});

test('model: the dynamic folders are one group among the tree\'s nodes; a device that stands nowhere gets a node of its own', () => {
    const folder = folderNode('folder:x', 'Filter devices', { rule: 'filter' });
    assert.deepEqual(folder, { key: 'folder:x', kind: 'folder', icon: 'folder', address: '', name: 'Filter devices', children: [], rule: 'filter' });
    const group = folderGroup([folder]);
    assert.deepEqual([group.key, group.kind, group.name], ['folders', 'folders', 'Dynamic Folders']);
    assert.deepEqual(group.children, [folder]);
    const devices = [{ id: 'd1', address: '31.1.3', name: 'Switch' }, { id: 'd2', address: '31.1.2', name: 'Dimmer' }, { id: 'd3', name: 'Spare' }];
    const roots = buildingTree([{ id: 's1', name: 'House', devices: ['d1'] }], devices);
    assert.deepEqual(unplacedNodes(roots, devices).map((node) => [node.key, node.kind]), [['device:d2', 'device'], ['device:d3', 'device']]);
    assert.deepEqual(unplacedNodes(roots, null), []);
});

test('model: the project places a telegram: the function and space of its destination, the room of its source', () => {
    const places = projectPlaces(
        [
            {
                id: 's1',
                name: 'House',
                functions: [{ id: 'f0', name: 'All off', groupAddresses: [{ groupAddress: '31/0/9' }] }],
                spaces: [{ id: 's2', name: 'Kitchen', functions: [{ id: 'f1', name: 'Ceiling', groupAddresses: [{ groupAddress: '31/0/1' }, { groupAddress: '31/0/2' }, {}] }, { id: 'f2', name: 'Second', groupAddresses: [{ groupAddress: '31/0/1' }] }] }],
            },
        ],
        [{ id: 'd1', address: '31.1.5', room: 'Hall' }, { id: 'd2', address: '31.1.6' }, { id: 'd3', room: 'Nowhere' }],
    );
    assert.deepEqual(places.functionOf.get('31/0/1'), { name: 'Ceiling', part: 'Kitchen' });
    assert.deepEqual(places.functionOf.get('31/0/9'), { name: 'All off', part: 'House' });
    assert.equal(places.functionOf.has('31/0/5'), false);
    assert.deepEqual([...places.roomOf], [['31.1.5', 'Hall']]);
    assert.deepEqual(projectPlaces(null, undefined), { functionOf: new Map(), roomOf: new Map() });
});
