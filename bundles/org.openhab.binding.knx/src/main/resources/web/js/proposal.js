// The plan under work in the wizard: the proposal of call 21 as the viewer changed it. Its rooms are the spaces of the
// project on the way to a Thing, each a location group of openHAB when it is ticked. Every change goes through
// apply() by the name of a field, is kept so it can be taken back and made again, and can be reset to the proposal.
// request() gives what call 22 gets. Nothing here decides whether a value is valid: that is call 22's answer. The
// module knows no document.

/** The fields a viewer may change, by what they belong to. */
export const FIELDS = Object.freeze({
    plan: ['readAtStart', 'locations'],
    room: ['make', 'groupName', 'roomLabel', 'roomTag'],
    thing: ['thingLabel', 'thingId', 'thingTarget'],
    channel: ['selected', 'channelLabel', 'channelType', 'split'],
    part: ['read', 'statusAddress', 'statusRead'],
    item: ['itemName', 'itemLabel', 'itemType', 'itemCategory', 'itemTags'],
});

const TEXT_FIELDS = Object.freeze({
    groupName: 'groupName',
    roomLabel: 'label',
    thingLabel: 'label',
    thingId: 'id',
    thingTarget: 'target',
    channelLabel: 'label',
    itemName: 'name',
    itemLabel: 'label',
});
const TICK_FIELDS = Object.freeze({ readAtStart: 'readAtStart', locations: 'locations', make: 'make', selected: 'selected', read: 'read', statusRead: 'statusRead' });

const WHERE = /^things\/(\d+)(?:\/channels\/(\d+))?/;
const ROOM_WHERE = /^roomGroups\/(\d+)/;

/** How many changes can be taken back. */
export const HISTORY_MAX = 200;

function textOf(value) {
    return typeof value === 'string' ? value : '';
}

function listOf(value) {
    return Array.isArray(value) ? value.filter((entry) => typeof entry === 'object' && entry !== null) : [];
}

function stringsOf(value) {
    return Array.isArray(value) ? value.filter((entry) => typeof entry === 'string') : [];
}

function copy(value) {
    return value === undefined ? undefined : JSON.parse(JSON.stringify(value));
}

function same(a, b) {
    return JSON.stringify(a ?? null) === JSON.stringify(b ?? null);
}

/** {@return the tags of one text field: split at commas, trimmed, the empty ones left out} */
export function tagsOf(words) {
    return textOf(words)
        .split(',')
        .map((tag) => tag.trim())
        .filter((tag) => tag !== '');
}

/**
 * {@return what a problem of call 22 stands at: {thing, channel?} or {room}, null for the plan as a whole}
 *
 * @param {string} where the problem's place, as call 22 names it
 * @param {Array<number>} made the rooms that went out as location groups, by their place in the plan
 */
export function whereOf(where, made = []) {
    const room = ROOM_WHERE.exec(textOf(where));
    if (room) {
        const at = made[Number(room[1])];
        return at === undefined ? null : { room: at };
    }
    const at = WHERE.exec(textOf(where));
    if (!at) {
        return null;
    }
    return at[2] === undefined ? { thing: Number(at[1]) } : { thing: Number(at[1]), channel: Number(at[2]) };
}

function itemOf(channel) {
    return typeof channel?.item === 'object' && channel.item !== null ? channel.item : null;
}

/** {@return the Item types the channel type allows, as the proposal says; none when it does not say} */
function itemTypesOf(item, channelType) {
    const byType = item?.allowedItemTypes;
    if (typeof byType !== 'object' || byType === null || Array.isArray(byType) || !Object.hasOwn(byType, channelType)) {
        return [];
    }
    return stringsOf(byType[channelType]);
}

/**
 * @param proposal the answer of call 21
 * @returns {Object} the plan: state(), get, can, options, apply, undo, redo, reset, request, counts and the rest below
 */
export function createPlan(proposal) {
    const first = {
        things: copy(listOf(proposal?.things)).map((thing) => ({ ...thing, channels: listOf(thing.channels) })),
        rooms: copy(listOf(proposal?.rooms)).map((room) => ({ ...room, make: true })),
        readAtStart: proposal?.readAtStart !== false,
    };
    first.locations = first.rooms.length > 0;
    const info = Object.freeze({
        unknown: Object.freeze(stringsOf(proposal?.unknown)),
        leftOut: Object.freeze({
            functions: listOf(proposal?.leftOut).filter((entry) => entry.source === 'FUNCTION').length,
            devices: listOf(proposal?.leftOut).filter((entry) => entry.source === 'DEVICE').length,
        }),
        thingsWithoutHandler: Number.isInteger(proposal?.thingsWithoutHandler) ? proposal.thingsWithoutHandler : 0,
        // the device Things the bridge has in openHAB: what channel lines can be written for
        deviceThings: Object.freeze(
            listOf(proposal?.deviceThings)
                .filter((thing) => textOf(thing.uid) !== '')
                .map((thing) => Object.freeze({ uid: thing.uid, label: textOf(thing.label), channelIds: Object.freeze(stringsOf(thing.channelIds)) })),
        ),
    });
    let state = copy(first);
    let version = 0;
    let done = [];
    let undone = [];

    function channelOf(ref) {
        return state.things[ref.thing]?.channels[ref.channel] ?? null;
    }

    function partOf(ref) {
        return listOf(channelOf(ref)?.parts)[ref.part] ?? null;
    }

    /** {@return the status address the proposal paired the part with, '' for none} */
    function originStatus(ref) {
        const id = channelOf(ref)?.id;
        for (const channel of listOf(first.things[ref.thing]?.channels)) {
            for (const candidate of [channel, ...listOf(channel.split)]) {
                if (candidate.id === id) {
                    return textOf(listOf(candidate.parts)[ref.part]?.statusAddress);
                }
            }
        }
        return '';
    }

    function statusOptions(ref) {
        const part = partOf(ref);
        if (!part) {
            return [];
        }
        const origin = originStatus(ref);
        const options = origin === '' ? [] : [{ value: origin, name: textOf(part.statusName) }];
        for (const candidate of listOf(part.candidates)) {
            const address = textOf(candidate.address);
            if (address !== '' && address !== origin && !options.some((option) => option.value === address)) {
                options.push({ value: address, name: textOf(candidate.name) });
            }
        }
        const current = textOf(part.statusAddress);
        if (current !== '' && !options.some((option) => option.value === current)) {
            options.unshift({ value: current, name: '' });
        }
        options.push({ value: '', name: '' });
        return options;
    }

    function typeOptions(list, current) {
        const values = [...list];
        if (textOf(current) !== '' && !values.includes(current)) {
            values.unshift(current);
        }
        return values.map((value) => ({ value, name: '' }));
    }

    /** {@return what the field can be set to, for a field that is a choice; else an empty list} */
    function options(ref, field) {
        const channel = channelOf(ref);
        if (field === 'statusAddress') {
            return statusOptions(ref);
        }
        if (field === 'channelType' && channel) {
            return typeOptions(channel.combined === true ? [] : stringsOf(channel.allowedChannelTypes), channel.channelType);
        }
        if (field === 'itemType' && itemOf(channel)) {
            return typeOptions(stringsOf(itemOf(channel).allowedTypes), itemOf(channel).type);
        }
        if (field === 'roomTag' && state.rooms[ref.room]) {
            const room = state.rooms[ref.room];
            return [...typeOptions(stringsOf(room.allowedTags), room.tag), { value: '', name: '' }];
        }
        return [];
    }

    function roomAt(id) {
        return textOf(id) === '' ? -1 : state.rooms.findIndex((room) => room.id === id);
    }

    function made(r) {
        return state.locations === true && state.rooms[r]?.make === true;
    }

    /** {@return the place of the nearest ticked group on the way up from this room, the room itself when asked; -1 for none} */
    function groupFrom(r, own) {
        let at = own ? r : roomAt(state.rooms[r]?.parentId);
        // a project's spaces make no ring; the count ends the walk if an answer did
        for (let turns = 0; at >= 0 && turns <= state.rooms.length; turns++) {
            if (made(at)) {
                return at;
            }
            at = roomAt(state.rooms[at].parentId);
        }
        return -1;
    }

    function roomOf(thing) {
        return roomAt(state.things[thing]?.roomId);
    }

    /** {@return the object the field is a field of, and its path in the state; null when the plan has none} */
    function holder(ref, field) {
        if (FIELDS.plan.includes(field)) {
            return { object: state, path: [] };
        }
        if (FIELDS.room.includes(field)) {
            const room = state.rooms[ref.room];
            return room ? { object: room, path: ['rooms', ref.room] } : null;
        }
        const thing = state.things[ref.thing];
        if (!thing) {
            return null;
        }
        if (FIELDS.thing.includes(field)) {
            return { object: thing, path: ['things', ref.thing] };
        }
        const channel = thing.channels[ref.channel];
        if (!channel) {
            return null;
        }
        const path = ['things', ref.thing, 'channels', ref.channel];
        if (FIELDS.channel.includes(field)) {
            return { object: channel, path };
        }
        if (FIELDS.part.includes(field)) {
            const part = partOf(ref);
            return part ? { object: part, path: [...path, 'parts', ref.part] } : null;
        }
        if (FIELDS.item.includes(field)) {
            return itemOf(channel) ? { object: itemOf(channel), path: [...path, 'item'] } : null;
        }
        return null;
    }

    /** {@return the field's value as the plan holds it: a text, a tick, or the list of tags} */
    function get(ref, field) {
        const held = holder(ref, field);
        if (!held) {
            return undefined;
        }
        if (Object.hasOwn(TEXT_FIELDS, field)) {
            return textOf(held.object[TEXT_FIELDS[field]]);
        }
        if (Object.hasOwn(TICK_FIELDS, field)) {
            return held.object[TICK_FIELDS[field]] === true;
        }
        switch (field) {
            case 'channelType':
                return textOf(held.object.channelType);
            case 'statusAddress':
                return textOf(held.object.statusAddress);
            case 'itemType':
                return textOf(held.object.type);
            case 'roomTag':
                return textOf(held.object.tag);
            case 'itemCategory':
                return textOf(held.object.category);
            case 'itemTags':
                return stringsOf(held.object.tags);
            default:
                return undefined;
        }
    }

    /** {@return whether the field can be changed now} */
    function can(ref, field) {
        const held = holder(ref, field);
        if (!held) {
            return false;
        }
        switch (field) {
            case 'read':
                return state.readAtStart;
            case 'statusRead':
                return state.readAtStart && textOf(held.object.statusAddress) !== '';
            case 'make':
                return state.locations === true;
            case 'statusAddress':
            case 'channelType':
            case 'itemType':
            case 'roomTag':
                return options(ref, field).length > 1;
            case 'split':
                return held.object.combined === true && listOf(held.object.split).length > 0;
            default:
                return true;
        }
    }

    /** {@return the paths and values one change sets; none when it cannot be made} */
    function setsOf(ref, field, value) {
        if (!can(ref, field)) {
            return [];
        }
        const { object, path } = holder(ref, field);
        if (Object.hasOwn(TEXT_FIELDS, field)) {
            return typeof value === 'string' ? [[[...path, TEXT_FIELDS[field]], value]] : [];
        }
        if (Object.hasOwn(TICK_FIELDS, field)) {
            return typeof value === 'boolean' ? [[[...path, TICK_FIELDS[field]], value]] : [];
        }
        switch (field) {
            case 'statusAddress': {
                if (textOf(value) === '') {
                    return [
                        [[...path, 'statusAddress'], null],
                        [[...path, 'statusRead'], false],
                    ];
                }
                return options(ref, field).some((option) => option.value === value) ? [[[...path, 'statusAddress'], value]] : [];
            }
            case 'channelType': {
                if (!options(ref, field).some((option) => option.value === value)) {
                    return [];
                }
                const sets = [[[...path, 'channelType'], value]];
                const allowed = itemTypesOf(itemOf(object), value);
                if (allowed.length > 0) {
                    sets.push([[...path, 'item', 'allowedTypes'], allowed], [[...path, 'item', 'type'], allowed[0]]);
                }
                return sets;
            }
            case 'itemType':
                return options(ref, field).some((option) => option.value === value) ? [[[...path, 'type'], value]] : [];
            case 'roomTag':
                return options(ref, field).some((option) => option.value === value) ? [[[...path, 'tag'], value]] : [];
            case 'itemCategory':
                return typeof value === 'string' ? [[[...path, 'category'], value === '' ? null : value]] : [];
            case 'itemTags':
                return Array.isArray(value) ? [[[...path, 'tags'], stringsOf(value)]] : [];
            case 'split': {
                const channels = [...state.things[ref.thing].channels];
                channels.splice(ref.channel, 1, ...copy(listOf(object.split)));
                return [[['things', ref.thing, 'channels'], channels]];
            }
            default:
                return [];
        }
    }

    function read(path) {
        let at = state;
        for (const name of path) {
            at = at?.[name];
        }
        return at;
    }

    function write(path, value) {
        let at = state;
        for (const name of path.slice(0, -1)) {
            at = at[name];
        }
        at[path[path.length - 1]] = copy(value);
    }

    function keep(steps) {
        if (steps.length === 0) {
            return false;
        }
        done.push(steps);
        if (done.length > HISTORY_MAX) {
            done.shift();
        }
        undone = [];
        version++;
        return true;
    }

    return {
        info,
        /** {@return the plan as it stands: things, rooms (each with make), locations and readAtStart; not to be changed} */
        state: () => state,
        /** {@return a number that grows with every change} */
        version: () => version,
        get,
        can,
        options,
        /** {@return how the part's status address came about: the proposal's pairing, NONE, or chosen (by the viewer)} */
        pairing(ref) {
            const part = partOf(ref);
            const chosen = textOf(part?.statusAddress);
            if (chosen === originStatus(ref)) {
                return textOf(part?.pairing);
            }
            return chosen === '' ? 'NONE' : 'chosen';
        },
        /**
         * Makes the changes as one step that undo() takes back as one. A change that cannot be made, or changes
         * nothing, is left out.
         *
         * @param {Array<[Object, string, *]>} changes each [what it is a field of, the field, the new value]
         * @returns {boolean} whether anything changed
         */
        apply(changes) {
            const steps = [];
            for (const [ref, field, value] of changes) {
                for (const [path, next] of setsOf(ref ?? {}, field, value)) {
                    const before = read(path);
                    if (!same(before, next)) {
                        steps.push({ path, before: copy(before), after: copy(next) });
                        write(path, next);
                    }
                }
            }
            return keep(steps);
        },
        canUndo: () => done.length > 0,
        canRedo: () => undone.length > 0,
        undo() {
            const steps = done.pop();
            if (!steps) {
                return false;
            }
            for (const step of [...steps].reverse()) {
                write(step.path, step.before);
            }
            undone.push(steps);
            version++;
            return true;
        },
        redo() {
            const steps = undone.pop();
            if (!steps) {
                return false;
            }
            for (const step of steps) {
                write(step.path, step.after);
            }
            done.push(steps);
            version++;
            return true;
        },
        /** {@return whether the plan differs from the proposal} */
        changed: () => !same(state, first),
        /** Puts the proposal back, as one step that can be taken back. */
        reset() {
            const steps = [];
            for (const name of ['things', 'rooms', 'readAtStart', 'locations']) {
                if (!same(state[name], first[name])) {
                    steps.push({ path: [name], before: copy(state[name]), after: copy(first[name]) });
                    write([name], first[name]);
                }
            }
            return keep(steps);
        },
        /**
         * {@return what call 22 gets, and which rooms went out as location groups (by their place in the plan): the
         * Things as they stand (a Thing's target only when one is chosen), the ticked groups each under the next ticked group above it, each Item in the group
         * of its Thing's space or the next ticked one above, the read switch}
         */
        request() {
            const out = state.rooms.map((room, at) => (made(at) ? at : -1)).filter((at) => at >= 0);
            const things = copy(state.things);
            things.forEach((thing, t) => {
                if (textOf(thing.target).trim() === '') {
                    delete thing.target;
                } else {
                    thing.target = thing.target.trim();
                }
                const group = groupFrom(roomOf(t), true);
                for (const channel of listOf(thing.channels)) {
                    if (itemOf(channel)) {
                        channel.item.groups = group < 0 ? [] : [textOf(state.rooms[group].groupName)];
                    }
                }
            });
            return {
                request: {
                    things,
                    roomGroups: out.map((at) => {
                        const room = state.rooms[at];
                        const above = groupFrom(at, false);
                        const group = { groupName: textOf(room.groupName), label: textOf(room.label), tag: textOf(room.tag) };
                        if (textOf(room.category) !== '') {
                            group.category = room.category;
                        }
                        if (above >= 0) {
                            group.parent = textOf(state.rooms[above].groupName);
                        }
                        return group;
                    }),
                    readAtStart: state.readAtStart,
                },
                made: out,
            };
        },
        /** {@return the place in the plan of the Thing's space, -1 when it has none} */
        roomOf,
        /** {@return whether the room's group is made: the switch is on and the room is ticked} */
        made,
        /** {@return the place of the group the room's group lies in: the next ticked one above; -1 for none} */
        parentOf: (room) => groupFrom(room, false),
        /** {@return the place of the group the Thing's Items go into, -1 for none} */
        groupOf: (thing) => groupFrom(roomOf(thing), true),
        /** {@return whether the room is the given one or lies below it in the project} */
        within(room, top) {
            let at = room;
            for (let turns = 0; at >= 0 && turns <= state.rooms.length; turns++) {
                if (at === top) {
                    return true;
                }
                at = roomAt(state.rooms[at].parentId);
            }
            return false;
        },
        /**
         * {@return what is ticked, counted from the plan: Things with a ticked channel, ticked channels, their Items
         * and links, the location groups to make, and the unticked channels whose address a channel of openHAB uses already}
         */
        counts() {
            const found = { things: 0, channels: 0, items: 0, links: 0, groups: state.rooms.filter((room, at) => made(at) && room.exists !== true).length, leftAlone: 0 };
            for (const thing of state.things) {
                const ticked = thing.channels.filter((channel) => channel.selected === true);
                found.things += ticked.length > 0 ? 1 : 0;
                found.channels += ticked.length;
                found.items += ticked.filter((channel) => itemOf(channel)).length;
                found.leftAlone += thing.channels.filter((channel) => channel.selected !== true && stringsOf(channel.alreadyConfigured).length > 0).length;
            }
            found.links = found.items;
            return found;
        },
    };
}
