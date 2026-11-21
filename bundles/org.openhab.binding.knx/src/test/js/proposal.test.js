import { test } from 'node:test';
import assert from 'node:assert/strict';

import { FIELDS, HISTORY_MAX, createPlan, tagsOf, whereOf } from '../web/js/proposal.js';

function part(address, more = {}) {
    return { key: 'ga', dataType: '1.001', groupAddress: address, groupAddressName: `Name of ${address}`, secured: false, read: false, statusRead: false, pairing: 'NONE', candidates: [], ...more };
}

function channel(id, more = {}) {
    return {
        id,
        channelType: 'switch',
        allowedChannelTypes: ['switch'],
        label: id,
        selected: true,
        combined: false,
        parts: [part('31/5/1')],
        item: { name: `Hall_${id}`, type: 'Switch', allowedTypes: ['Switch'], label: id, tags: [], groups: [] },
        alreadyConfigured: [],
        notes: [],
        ...more,
    };
}

function proposal() {
    return {
        things: [
            {
                id: 'Hall',
                uid: 'knx:device:made-up:Hall',
                label: 'Hall',
                source: 'DEVICE',
                sourceId: 'P-9_D-1',
                roomId: 'P-9_B-1',
                room: 'Hall',
                channels: [
                    channel('Light', {
                        parts: [part('31/5/1', { statusAddress: '31/5/2', statusName: 'Light state', statusRead: true, pairing: 'OBJECTS', candidates: [{ address: '31/5/2', name: 'Light state' }, { address: '31/5/3', name: 'Other' }] })],
                    }),
                    channel('Mode', {
                        channelType: 'number',
                        allowedChannelTypes: ['number', 'string'],
                        parts: [part('31/5/4', { dataType: '20.102' })],
                        item: { name: 'Hall_Mode', type: 'Number', allowedTypes: ['Number'], allowedItemTypes: { number: ['Number'], string: ['String'] }, label: 'Mode', tags: [], groups: [] },
                    }),
                    channel('Dim', {
                        channelType: 'dimmer',
                        allowedChannelTypes: ['dimmer'],
                        combined: true,
                        selected: false,
                        alreadyConfigured: ['knx:device:made-up:Old:dim'],
                        parts: [part('31/5/5', { key: 'switch' }), part('31/5/6', { key: 'position', dataType: '5.001' })],
                        split: [channel('Dim_switch', { parts: [part('31/5/5', { statusAddress: '31/5/7', pairing: 'NAMES' })] }), channel('Dim_position', { parts: [part('31/5/6', { dataType: '5.001' })] })],
                    }),
                ],
            },
            { id: 'Loose', uid: 'knx:device:made-up:Loose', label: 'Loose', source: 'ADDRESSES', channels: [channel('One')] },
        ],
        rooms: [{ id: 'P-9_B-1', name: 'Hall', groupName: 'gHall', label: 'Hall', type: 'Room', tag: 'Room', allowedTags: TAGS, category: 'group' }],
        unknown: ['31/7/9'],
        leftOut: [{ source: 'DEVICE', sourceId: 'x' }, { source: 'FUNCTION', sourceId: 'y' }, { source: 'OTHER' }, null],
        readAtStart: true,
        thingsWithoutHandler: 2,
    };
}

const TAGS = ['Building', 'Floor', 'Room', 'Corridor', 'Outdoor'];
const HALL = [{ groupName: 'gHall', label: 'Hall', tag: 'Room', category: 'group' }];

/** The Things as call 22 gets them while the Hall's group is made: the Items of the Thing in the Hall are in it. */
function inHall(things) {
    for (const entry of things[0].channels) {
        entry.item.groups = ['gHall'];
    }
    return things;
}

const LIGHT = { thing: 0, channel: 0 };
const LIGHT_PART = { thing: 0, channel: 0, part: 0 };
const MODE = { thing: 0, channel: 1 };
const DIM = { thing: 0, channel: 2 };

test('proposal: an untouched plan sends the proposal as it came, its location groups on, and the read switch', () => {
    const plan = createPlan(proposal());
    assert.deepEqual(plan.request(), { request: { things: inHall(proposal().things), roomGroups: HALL, readAtStart: true }, made: [0] });
    assert.equal(createPlan({ ...proposal(), rooms: [] }).state().locations, false, 'a project without spaces has the switch off');
    assert.equal(createPlan({ ...proposal(), readAtStart: false }).request().request.readAtStart, false);
    assert.deepEqual(plan.info, { unknown: ['31/7/9'], leftOut: { functions: 1, devices: 1 }, thingsWithoutHandler: 2, deviceThings: [] });
    assert.equal(plan.changed(), false);
    assert.deepEqual(createPlan(null).request().request, { things: [], roomGroups: [], readAtStart: true });
    assert.deepEqual(createPlan({ things: 'x', rooms: [null, 3] }).state(), { things: [], rooms: [], readAtStart: true, locations: false });
});

test('proposal: every field a viewer may change goes into the request under its name; nothing else changes', () => {
    const plan = createPlan(proposal());
    const changes = [
        [{ thing: 0 }, 'thingLabel', 'Hall two'],
        [{ thing: 0 }, 'thingId', 'Hall_2'],
        [LIGHT, 'selected', false],
        [LIGHT, 'channelLabel', 'Ceiling'],
        [LIGHT_PART, 'read', true],
        [LIGHT_PART, 'statusRead', false],
        [LIGHT_PART, 'statusAddress', '31/5/3'],
        [LIGHT, 'itemName', 'Hall_Ceiling'],
        [LIGHT, 'itemLabel', 'Ceiling light'],
        [LIGHT, 'itemCategory', 'light'],
        [LIGHT, 'itemTags', ['Light', 'Switch']],
        [{}, 'readAtStart', false],
    ];
    for (const change of changes) {
        assert.equal(plan.apply([change]), true, change[1]);
    }
    const expected = proposal().things;
    Object.assign(expected[0], { label: 'Hall two', id: 'Hall_2' });
    Object.assign(expected[0].channels[0], { selected: false, label: 'Ceiling' });
    Object.assign(expected[0].channels[0].parts[0], { read: true, statusRead: false, statusAddress: '31/5/3' });
    Object.assign(expected[0].channels[0].item, { name: 'Hall_Ceiling', label: 'Ceiling light', category: 'light', tags: ['Light', 'Switch'] });
    assert.deepEqual(plan.request().request, { things: inHall(expected), roomGroups: HALL, readAtStart: false });
    assert.equal(plan.version(), changes.length);
    assert.deepEqual(Object.values(FIELDS).flat().length, 21);
});

test('proposal: no status address takes the status read mark away, and only an address of the choice is taken', () => {
    const plan = createPlan(proposal());
    assert.deepEqual(
        plan.options(LIGHT_PART, 'statusAddress').map((option) => option.value),
        ['31/5/2', '31/5/3', ''],
    );
    assert.equal(plan.pairing(LIGHT_PART), 'OBJECTS');
    assert.equal(plan.apply([[LIGHT_PART, 'statusAddress', '31/5/9']]), false);
    plan.apply([[LIGHT_PART, 'statusAddress', '31/5/3']]);
    assert.equal(plan.pairing(LIGHT_PART), 'chosen');
    plan.apply([[LIGHT_PART, 'statusAddress', '']]);
    const sent = plan.request().request.things[0].channels[0].parts[0];
    assert.deepEqual([sent.statusAddress, sent.statusRead], [null, false]);
    assert.equal(plan.pairing(LIGHT_PART), 'NONE');
    assert.equal(plan.can(LIGHT_PART, 'statusRead'), false);
    assert.equal(plan.apply([[LIGHT_PART, 'statusRead', true]]), false);
    // the proposal's own address is still offered
    assert.deepEqual(
        plan.options(LIGHT_PART, 'statusAddress').map((option) => option.value),
        ['31/5/2', '31/5/3', ''],
    );
    plan.apply([[LIGHT_PART, 'statusAddress', '31/5/2']]);
    assert.equal(plan.pairing(LIGHT_PART), 'OBJECTS');
    // a part with nothing to choose from cannot be changed
    assert.equal(plan.can({ thing: 0, channel: 1, part: 0 }, 'statusAddress'), false);
});

test('proposal: another channel type takes the first Item type of its list; a type outside the list is not taken', () => {
    const plan = createPlan(proposal());
    assert.equal(plan.can(LIGHT, 'channelType'), false);
    assert.equal(plan.can(LIGHT, 'itemType'), false);
    assert.equal(plan.apply([[MODE, 'channelType', 'rollershutter']]), false);
    assert.equal(plan.apply([[MODE, 'channelType', 'string']]), true);
    const mode = plan.request().request.things[0].channels[1];
    assert.deepEqual([mode.channelType, mode.item.type, mode.item.allowedTypes], ['string', 'String', ['String']]);
    assert.equal(plan.apply([[MODE, 'itemType', 'Number']]), false);
    assert.equal(plan.can(DIM, 'channelType'), false);
});

test('proposal: split puts the split channels in the place of the combined one, each with its own pairing', () => {
    const plan = createPlan(proposal());
    assert.equal(plan.can(LIGHT, 'split'), false);
    assert.equal(plan.apply([[LIGHT, 'split', true]]), false);
    assert.equal(plan.apply([[DIM, 'split', true]]), true);
    const sent = plan.request().request.things[0].channels;
    assert.deepEqual(
        sent.map((entry) => entry.id),
        ['Light', 'Mode', 'Dim_switch', 'Dim_position'],
    );
    assert.deepEqual(
        sent.slice(2),
        proposal().things[0].channels[2].split.map((entry) => ({ ...entry, item: { ...entry.item, groups: ['gHall'] } })),
    );
    assert.equal(plan.pairing({ thing: 0, channel: 2, part: 0 }), 'NAMES');
    assert.deepEqual(
        plan.options({ thing: 0, channel: 2, part: 0 }, 'statusAddress').map((option) => option.value),
        ['31/5/7', ''],
    );
});

test('proposal: the read marks cannot be changed while "Read values at start" is off, and stay as they were', () => {
    const plan = createPlan(proposal());
    plan.apply([[{}, 'readAtStart', false]]);
    assert.equal(plan.can(LIGHT_PART, 'read'), false);
    assert.equal(plan.can(LIGHT_PART, 'statusRead'), false);
    assert.equal(plan.apply([[LIGHT_PART, 'read', true]]), false);
    assert.equal(plan.request().request.things[0].channels[0].parts[0].statusRead, true);
});

test('proposal: a location group goes out with its name, label, tag and icon, and the Items of its space get it', () => {
    const plan = createPlan(proposal());
    assert.equal(plan.roomOf(0), 0);
    assert.equal(plan.roomOf(1), -1);
    plan.apply([[{ room: 0 }, 'groupName', 'gEntrance']]);
    plan.apply([[{ room: 0 }, 'roomLabel', 'Entrance']]);
    assert.deepEqual(
        plan.options({ room: 0 }, 'roomTag').map((option) => option.value),
        [...TAGS, ''],
    );
    assert.equal(plan.apply([[{ room: 0 }, 'roomTag', 'Kitchen']]), false, 'a tag the proposal does not allow');
    plan.apply([[{ room: 0 }, 'roomTag', 'Corridor']]);
    const { request, made } = plan.request();
    assert.deepEqual(request.roomGroups, [{ groupName: 'gEntrance', label: 'Entrance', tag: 'Corridor', category: 'group' }]);
    assert.deepEqual(made, [0]);
    assert.deepEqual(
        request.things.map((thing) => thing.channels.map((entry) => entry.item.groups)),
        [[['gEntrance'], ['gEntrance'], ['gEntrance']], [[]]],
    );
    plan.apply([[{ room: 0 }, 'roomTag', '']]);
    assert.equal(plan.request().request.roomGroups[0].tag, '', 'no tag goes out as the empty text, never left out');
    assert.equal(plan.apply([[{ room: 4 }, 'make', true]]), false);
    plan.apply([[{ room: 0 }, 'make', false]]);
    assert.deepEqual(plan.request().request.roomGroups, []);
    assert.deepEqual(plan.request().request.things[0].channels[0].item.groups, []);
});

/** House > West wing > Ground floor > (Bath, Hall) and First floor > Bath; a Thing in each Bath, one on the ground floor, one nowhere. */
function house() {
    const room = (id, name, type, parentId, groupName, tag) => ({ id, name, groupName, label: name, type, tag, allowedTags: TAGS, category: 'group', ...(parentId ? { parentId } : {}) });
    const thing = (id, roomId) => ({ id, uid: `knx:device:made-up:${id}`, label: id, source: 'FUNCTION', ...(roomId ? { roomId } : {}), channels: [channel(`${id}_c`)] });
    return {
        things: [thing('Lower_bath', 'B-4'), thing('Upper_bath', 'B-6'), thing('Floor_light', 'B-3'), thing('Loose')],
        rooms: [
            room('B-1', 'House', 'Building', '', 'gHouse', 'Building'),
            room('B-2', 'West wing', 'BuildingPart', 'B-1', 'gWest_wing', 'Building'),
            room('B-3', 'Ground floor', 'Floor', 'B-2', 'gGround_floor', 'Floor'),
            room('B-4', 'Bath', 'Room', 'B-3', 'gBath', 'Room'),
            room('B-5', 'First floor', 'Floor', 'B-2', 'gFirst_floor', 'Floor'),
            room('B-6', 'Bath', 'Room', 'B-5', 'gBath_2', 'Room'),
        ],
        readAtStart: true,
    };
}

function groupsOut(plan) {
    const { request } = plan.request();
    return {
        groups: request.roomGroups.map((group) => `${group.groupName}<${group.parent ?? ''}`),
        items: request.things.map((thing) => thing.channels[0].item.groups.join()),
    };
}

test('proposal: every space on the way to a Thing is a group under the one above it; an Item is in the deepest one only', () => {
    const plan = createPlan(house());
    assert.deepEqual(groupsOut(plan), {
        groups: ['gHouse<', 'gWest_wing<gHouse', 'gGround_floor<gWest_wing', 'gBath<gGround_floor', 'gFirst_floor<gWest_wing', 'gBath_2<gFirst_floor'],
        items: ['gBath', 'gBath_2', 'gGround_floor', ''],
    });
    assert.deepEqual(plan.request().made, [0, 1, 2, 3, 4, 5]);
    assert.equal(plan.counts().groups, 6);
});

test('proposal: an unticked group hands what is in it to the next ticked group above, or to none', () => {
    const plan = createPlan(house());
    plan.apply([[{ room: 2 }, 'make', false]]);
    assert.deepEqual(groupsOut(plan), {
        groups: ['gHouse<', 'gWest_wing<gHouse', 'gBath<gWest_wing', 'gFirst_floor<gWest_wing', 'gBath_2<gFirst_floor'],
        items: ['gBath', 'gBath_2', 'gWest_wing', ''],
    });
    assert.deepEqual(plan.request().made, [0, 1, 3, 4, 5], 'a problem of call 22 at roomGroups/2 is the Bath of the plan');
    assert.equal(plan.parentOf(3), 1);
    assert.equal(plan.groupOf(2), 1);
    plan.apply([
        [{ room: 0 }, 'make', false],
        [{ room: 1 }, 'make', false],
    ]);
    assert.deepEqual(groupsOut(plan), { groups: ['gBath<', 'gFirst_floor<', 'gBath_2<gFirst_floor'], items: ['gBath', 'gBath_2', '', ''] });
    assert.equal(plan.parentOf(3), -1);
    assert.equal(plan.groupOf(2), -1);
    assert.equal(plan.within(5, 4), true);
    assert.equal(plan.within(3, 4), false);
});

test('proposal: the one switch takes every location group out and puts them back as they were ticked', () => {
    const plan = createPlan(house());
    plan.apply([[{ room: 3 }, 'make', false]]);
    assert.equal(plan.apply([[{}, 'locations', false]]), true);
    assert.deepEqual(groupsOut(plan), { groups: [], items: ['', '', '', ''] });
    assert.equal(plan.counts().groups, 0);
    assert.equal(plan.can({ room: 0 }, 'make'), false);
    assert.equal(plan.apply([[{ room: 0 }, 'make', false]]), false);
    plan.apply([[{}, 'locations', true]]);
    assert.deepEqual(groupsOut(plan).items, ['gGround_floor', 'gBath_2', 'gGround_floor', '']);
    plan.reset();
    assert.equal(plan.counts().groups, 6);
});

test('proposal: a group openHAB has already still goes out, and is not counted as one to make', () => {
    const proposed = house();
    proposed.rooms[0].exists = true;
    const plan = createPlan(proposed);
    assert.equal(plan.request().request.roomGroups[0].groupName, 'gHouse');
    assert.equal(plan.counts().groups, 5);
});

test('proposal: several changes at once are one step; undo, redo and reset walk the steps', () => {
    const plan = createPlan(proposal());
    assert.deepEqual([plan.canUndo(), plan.canRedo(), plan.undo(), plan.redo(), plan.reset()], [false, false, false, false, false]);
    plan.apply([[LIGHT, 'itemName', 'Hall_A']]);
    plan.apply([
        [LIGHT, 'itemTags', ['Light']],
        [MODE, 'itemTags', ['Light']],
        [{ thing: 1, channel: 0 }, 'itemTags', ['Light']],
        [{ thing: 7, channel: 0 }, 'itemTags', ['Light']],
    ]);
    plan.apply([[DIM, 'split', true]]);
    const names = () => plan.state().things[0].channels.map((entry) => `${entry.item.name}:${entry.item.tags.join('+')}`);
    assert.deepEqual(names(), ['Hall_A:Light', 'Hall_Mode:Light', 'Hall_Dim_switch:', 'Hall_Dim_position:']);
    assert.equal(plan.undo(), true);
    assert.equal(plan.undo(), true);
    assert.deepEqual(names(), ['Hall_A:', 'Hall_Mode:', 'Hall_Dim:']);
    assert.equal(plan.state().things[1].channels[0].item.tags.length, 0);
    assert.equal(plan.redo(), true);
    assert.deepEqual(names(), ['Hall_A:Light', 'Hall_Mode:Light', 'Hall_Dim:']);
    // a change that changes nothing is no step
    assert.equal(plan.apply([[LIGHT, 'itemName', 'Hall_A']]), false);
    assert.equal(plan.canRedo(), true);
    plan.apply([[LIGHT, 'itemName', 'Hall_B']]);
    assert.equal(plan.canRedo(), false);
    assert.equal(plan.changed(), true);
    assert.equal(plan.reset(), true);
    assert.equal(plan.changed(), false);
    assert.deepEqual(plan.request().request.things, inHall(proposal().things));
    assert.equal(plan.undo(), true);
    assert.deepEqual(names(), ['Hall_B:Light', 'Hall_Mode:Light', 'Hall_Dim:']);
    for (let n = 0; n < HISTORY_MAX + 5; n++) {
        plan.apply([[LIGHT, 'itemName', `Hall_${n}`]]);
    }
    let steps = 0;
    while (plan.undo()) {
        steps++;
    }
    assert.equal(steps, HISTORY_MAX);
});

test('proposal: a value of the wrong kind, a field of nothing and an unknown field change nothing', () => {
    const plan = createPlan(proposal());
    for (const change of [
        [LIGHT, 'itemName', 7],
        [LIGHT, 'selected', 'yes'],
        [LIGHT, 'itemTags', 'Light'],
        [LIGHT, 'address', '31.1.9'],
        [{ thing: 0 }, 'address', '31.1.9'],
        [{ thing: 9 }, 'thingLabel', 'x'],
        [{ thing: 0, channel: 9 }, 'channelLabel', 'x'],
        [{ thing: 0, channel: 0, part: 9 }, 'read', true],
        [undefined, 'thingLabel', 'x'],
    ]) {
        assert.equal(plan.apply([change]), false, JSON.stringify(change));
    }
    assert.equal(plan.get(LIGHT, 'address'), undefined);
    assert.equal(plan.version(), 0);
    assert.deepEqual(plan.request().request.things, inHall(proposal().things));
});

test('proposal: the counts are of what is ticked; an unticked channel whose address is in use counts as left alone', () => {
    const plan = createPlan(proposal());
    assert.deepEqual(plan.counts(), { things: 2, channels: 3, items: 3, links: 3, groups: 1, leftAlone: 1 });
    plan.apply([
        [{ thing: 1, channel: 0 }, 'selected', false],
        [{ room: 0 }, 'make', false],
        [DIM, 'selected', true],
    ]);
    assert.deepEqual(plan.counts(), { things: 1, channels: 3, items: 3, links: 3, groups: 0, leftAlone: 0 });
});

test('proposal: tags are split at commas, and a problem\'s place is found from its text', () => {
    assert.deepEqual(tagsOf(' Light, Switch ,, '), ['Light', 'Switch']);
    assert.deepEqual(tagsOf(undefined), []);
    assert.deepEqual(whereOf('things/3'), { thing: 3 });
    assert.deepEqual(whereOf('things/3/label'), { thing: 3 });
    assert.deepEqual(whereOf('things/0/channels/12/item/name'), { thing: 0, channel: 12 });
    assert.deepEqual(whereOf('roomGroups/1/groupName', [0, 4]), { room: 4 });
    assert.equal(whereOf('roomGroups/2', [0, 4]), null);
    assert.equal(whereOf('selection'), null);
    assert.equal(whereOf(undefined), null);
});

test('proposal: a change after a reset or an undo never alters the proposal or a kept step', () => {
    const plan = createPlan(proposal());
    plan.apply([[LIGHT, 'itemTags', ['Light']]]);
    plan.reset();
    plan.apply([[LIGHT, 'itemName', 'Hall_Z']]);
    plan.reset();
    assert.deepEqual(plan.request().request.things, inHall(proposal().things));
    plan.undo();
    plan.undo();
    plan.undo();
    assert.deepEqual(plan.state().things[0].channels[0].item.tags, ['Light']);
    plan.apply([[LIGHT, 'itemTags', ['Light', 'Hall']]]);
    plan.undo();
    assert.deepEqual(plan.state().things[0].channels[0].item.tags, ['Light']);
    plan.redo();
    assert.deepEqual(plan.state().things[0].channels[0].item.tags, ['Light', 'Hall']);
});

test('proposal: the existing Thing chosen for a Thing goes out as its target, trimmed, and not at all when none is chosen', () => {
    const offered = { ...proposal(), deviceThings: [{ uid: 'knx:device:made-up:wall', label: 'Wall', channelIds: ['a'] }, { label: 'no uid' }, null] };
    const plan = createPlan(offered);
    assert.deepEqual(plan.info.deviceThings, [{ uid: 'knx:device:made-up:wall', label: 'Wall', channelIds: ['a'] }]);
    assert.equal(
        plan.request().request.things.some((thing) => Object.hasOwn(thing, 'target')),
        false,
    );
    plan.apply([[{ thing: 0 }, 'thingTarget', ' knx:device:made-up:wall ']]);
    assert.deepEqual(
        plan.request().request.things.map((thing) => thing.target),
        ['knx:device:made-up:wall', undefined],
    );
    plan.apply([[{ thing: 0 }, 'thingTarget', '  ']]);
    assert.equal(Object.hasOwn(plan.request().request.things[0], 'target'), false);
});
