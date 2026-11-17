// The wizard's main screen: what will be made in openHAB, as a workplace. The tree at the left is the openHAB side
// (the bridge, its Things, their channels and Items, the location groups nested as the project's spaces), the list in
// the middle has the tabs Channels, Items, Things, Locations, Read at start and Text files, the sidebar holds Edit, Checks and Source, and a bar at the bottom
// sums the plan up. A cell that may be changed turns into its editor on a double click or Enter; the sidebar's form
// changes the same fields, for all selected rows at once. Every change goes into the plan of js/proposal.js; what is
// valid is said by call 22 alone, whose answer the owner of this screen hands in.

import { add, attr, clear, el, icon, place } from '../dom.js';
import { SPACE_LOOKS, addressKey, spaceKind } from '../model.js';
import { createPanel } from '../panel.js';
import { tagsOf, whereOf } from '../proposal.js';
import { NAV_VIEWS, WIZARD, href, viewsOf } from '../router.js';
import { createSidebar } from '../sidebar.js';
import { createSplitter } from '../splitter.js';
import { createTable } from '../table.js';
import { errorText, hasText, text } from '../texts.js';
import { createTree } from '../tree.js';
import { PROPERTIES_WIDTH } from '../workplace.js';
import { createCopyBox } from './objects.js';

/** The tabs of the list, in their order. */
export const PLAN_TABS = Object.freeze(['channels', 'items', 'things', 'locations', 'reads', 'text']);
/** The containers of the wizard's sidebar: name, drawing, text key of the heading, open at first. */
export const PLAN_CONTAINERS = Object.freeze([
    ['edit', 'pencil', 'wizard.edit', true],
    ['checks', 'check', 'wizard.checks', true],
    ['source', 'link', 'wizard.source', false],
]);
/** The states a node of the tree or a row shows: drawing and text key. */
export const PLAN_STATES = Object.freeze({
    new: ['plus', 'wizard.state.new'],
    there: ['same', 'wizard.state.there'],
    out: ['minus', 'wizard.state.out'],
    warning: ['warn', 'wizard.state.warning'],
    error: ['error', 'wizard.state.error'],
});

/** Each field of the plan as the screen shows it: kind of editor, class of its control, text key of its label. */
const FIELD = Object.freeze({
    readAtStart: ['tick', 'read-at-start', 'wizard.readAtStart'],
    locations: ['tick', 'location-groups', 'wizard.locationGroups'],
    make: ['tick', 'location-group', 'wizard.locationTick'],
    groupName: ['text', 'location-name', 'wizard.groupName'],
    roomLabel: ['text', 'location-label', 'wizard.groupLabel'],
    roomTag: ['choice', 'location-tag', 'wizard.locationTag'],
    thingLabel: ['text', 'thing-label', 'wizard.thingLabel'],
    thingId: ['text', 'thing-id', 'wizard.thingId'],
    selected: ['tick', 'channel-pick', 'wizard.selected'],
    channelLabel: ['text', 'channel-label', 'wizard.channelLabel'],
    channelType: ['choice', 'channel-type', 'wizard.channelType'],
    read: ['tick', 'part-read', 'wizard.read'],
    statusAddress: ['choice', 'part-status', 'wizard.status'],
    statusRead: ['tick', 'part-status-read', 'wizard.statusRead'],
    itemName: ['text', 'item-name', 'wizard.itemName'],
    itemLabel: ['text', 'item-label', 'wizard.itemLabel'],
    itemType: ['choice', 'item-type', 'wizard.itemType'],
    itemCategory: ['text', 'item-category', 'wizard.category'],
    itemTags: ['text', 'item-tags', 'wizard.tags'],
});
/** What a change in the sidebar sets on every selected row. */
const SHARED_FIELDS = Object.freeze({
    channels: ['selected', 'read', 'statusRead', 'itemType', 'itemCategory', 'itemTags'],
    items: ['selected', 'itemType', 'itemCategory', 'itemTags'],
    locations: ['make', 'roomTag'],
});
const CHANNEL_FIELDS = ['selected', 'channelLabel', 'channelType'];
const PART_FIELDS = ['read', 'statusAddress', 'statusRead'];
const ITEM_FIELDS = ['itemName', 'itemLabel', 'itemType', 'itemCategory', 'itemTags'];
const ROOM_FIELDS = ['make', 'groupName', 'roomLabel', 'roomTag'];
/** The place of the row's mark on the Split button, which is no field of the plan. */
const SPLIT = 'split';

function textOf(value) {
    return typeof value === 'string' ? value : '';
}

function listOf(value) {
    return Array.isArray(value) ? value.filter((entry) => typeof entry === 'object' && entry !== null) : [];
}

function stringsOf(value) {
    return Array.isArray(value) ? value.filter((entry) => typeof entry === 'string') : [];
}

function count(value) {
    return Number.isInteger(value) ? value : 0;
}

function named(group, value) {
    return hasText(`${group}.${value}`) ? text(`${group}.${value}`) : textOf(value);
}

function spaced(...parts) {
    return parts.filter((part) => textOf(part) !== '').join(' ');
}

/** {@return the words of a problem of call 22: the page's text for its code, else the server's} */
export function problemWords(problem) {
    return hasText(`problem.${problem.code}`) ? text(`problem.${problem.code}`) : textOf(problem.text);
}

/**
 * @param {Object} options
 * @param options.context the view's context
 * @param options.plan the plan of js/proposal.js
 * @param {() => Object} options.check the state of the checking: answer (the last answer of call 22, with the plan's
 *     version it was asked for, the rooms that went out and whether a run used it), pending, error
 * @param {() => boolean} options.working a run or an undo is going
 * @param {() => boolean} options.canCreate
 * @param {Object} options.on changed() after every change of the plan, choose(), recheck(), create()
 * @param {{scope?: string, tab?: string, textMode?: string}} [options.view] what was selected when the screen was left
 * @returns {{node: Element, redraw: Function, view: Function, say: Function, focusCreate: Function, close: Function}}
 */
export function createPlanView({ context, plan, check, working, lastRun, canCreate, on, view: kept }) {
    const { document, bridge, layout } = context;
    const title = text('view.wizard');

    let scope = textOf(kept?.scope) === '' ? 'bridge' : kept.scope;
    let tab = PLAN_TABS.includes(kept?.tab) ? kept.tab : 'channels';
    let editing = null;
    let cursor = null;
    let marked = null;
    let answering = null;
    let answered = false;
    let closed = false;
    let notice = '';
    let textMode = kept?.textMode === 'lines' ? 'lines' : 'new';
    // the Things whose existing Thing is typed, not picked from the list
    const typedTargets = new Set();
    const targetFields = new Map();

    const cellFields = new WeakMap();
    const formFields = new Map();
    // a row that did not change keeps its buttons, so what a button does is kept as long as the button
    const actions = new WeakMap();

    // The frame.

    const offered = bridge ? viewsOf(bridge) : [];
    const panel = createPanel({
        document,
        layout,
        title,
        panels: [{ view: WIZARD, label: title }, ...NAV_VIEWS.filter((name) => offered.includes(name)).map((name) => ({ view: name, label: text(`view.${name}`) }))],
        current: WIZARD,
        onPanel: (name) => context.navigate({ view: name, bridge: bridge.uid, query: {} }),
        history: context.history,
    });
    const box = el(document, 'div', { class: 'workplace plan-workplace' });
    const sidebar = createSidebar({ document, layout, containers: PLAN_CONTAINERS, onOpen: (open) => box.classList.toggle('sidebar-closed', !open) });
    const splitter = createSplitter({ document, target: box, property: '--properties-width', name: 'properties', layout, ...PROPERTIES_WIDTH, reverse: true, label: text('frame.resizeSidebar'), className: 'properties-splitter' });
    add(document, box, panel.node, splitter.node, sidebar.node);
    const editBody = el(document, 'div', { class: 'plan-edit' });
    const checksBody = el(document, 'div', { class: 'plan-checks' });
    const sourceBody = el(document, 'div', { class: 'plan-source' });
    add(document, sidebar.body('edit'), editBody);
    add(document, sidebar.body('checks'), checksBody);
    add(document, sidebar.body('source'), sourceBody);

    const notes = el(document, 'div', { class: 'plan-notes' });
    const willLine = el(document, 'span', { class: 'will' });
    const problemLine = el(document, 'span', { class: 'problem-counts' });
    const stateLine = el(document, 'span', { class: 'check-state', role: 'status' });
    const button = (className, key, drawing) => el(document, 'button', { type: 'button', class: className }, drawing ? icon(document, drawing) : null, text(key));
    const chooseButton = button('back-choose', 'wizard.backToChoose', 'back');
    const undoButton = button('undo-edit', 'wizard.undoChange', 'undo');
    const redoButton = button('redo-edit', 'wizard.redoChange', 'redo');
    const resetButton = button('reset-plan', 'wizard.reset');
    const recheckButton = button('recheck', 'wizard.recheck');
    const createButton = button('create', 'wizard.createOpen');
    const lastRunButton = el(document, 'button', { type: 'button', class: 'last-run' });
    const summaryTools = el(document, 'div', { class: 'tools' });
    const summary = el(
        document,
        'div',
        { class: 'plan-summary' },
        el(document, 'p', { class: 'summary-text', 'aria-live': 'polite' }, willLine, ' ', problemLine, ' ', stateLine),
        summaryTools,
    );
    const node = el(document, 'div', { class: 'wizard-plan' }, notes, box, summary);

    const readSwitch = el(document, 'input', { type: 'checkbox', name: 'read-at-start', class: 'read-at-start' });
    const locationSwitch = el(document, 'input', { type: 'checkbox', name: 'location-groups', class: 'location-groups' });
    add(document, panel.tools, el(document, 'label', { class: 'tick' }, readSwitch, ' ', text('wizard.readAtStart')), el(document, 'label', { class: 'tick', title: text('wizard.help.locations') }, locationSwitch, ' ', text('wizard.locationGroups')));

    // What the plan and the last answer say.

    function answer() {
        return check().answer?.answer ?? null;
    }

    function problems() {
        const all = listOf(answer()?.problems);
        return [...all.filter((problem) => problem.severity === 'ERROR'), ...all.filter((problem) => problem.severity !== 'ERROR')];
    }

    /** {@return the problems by where they stand: `t/<thing>`, `c/<thing>/<channel>`, `g/<room>`} */
    function problemsByPlace() {
        const places = new Map();
        for (const problem of problems()) {
            const at = whereOf(problem.where, check().answer?.made);
            const key = at === null ? 'plan' : at.room !== undefined ? `g/${at.room}` : at.channel === undefined ? `t/${at.thing}` : `c/${at.thing}/${at.channel}`;
            places.set(key, [...(places.get(key) ?? []), problem]);
        }
        return places;
    }

    function worst(list) {
        if (!list || list.length === 0) {
            return null;
        }
        return list.some((problem) => problem.severity === 'ERROR') ? 'error' : 'warning';
    }

    function channelState(places, t, c, channel) {
        const bad = worst(places.get(`c/${t}/${c}`));
        if (bad) {
            return bad;
        }
        if (channel.selected === true) {
            return 'new';
        }
        return stringsOf(channel.alreadyConfigured).length > 0 ? 'there' : 'out';
    }

    function thingState(places, t, thing) {
        const states = thing.channels.map((channel, c) => channelState(places, t, c, channel));
        const own = worst(places.get(`t/${t}`));
        for (const bad of ['error', 'warning']) {
            if (own === bad || states.includes(bad)) {
                return bad;
            }
        }
        if (states.includes('new')) {
            return 'new';
        }
        return states.length > 0 && states.every((state) => state === 'there') ? 'there' : 'out';
    }

    /** {@return whether openHAB has the room's group already: by the last answer, before it by the proposal} */
    function groupExists(room) {
        const got = answer();
        return Array.isArray(got?.existingGroups) ? got.existingGroups.includes(room.groupName) : room.exists === true;
    }

    function roomState(places, r, room) {
        return worst(places.get(`g/${r}`)) ?? (plan.made(r) ? (groupExists(room) ? 'there' : 'new') : 'out');
    }

    function groupNameOf(r) {
        return r < 0 ? '' : textOf(plan.state().rooms[r]?.groupName);
    }

    /** {@return the ticked channels with an Item whose Items go into the room's group, as [thing, channel] places} */
    function membersOf(r) {
        const found = [];
        if (!plan.made(r)) {
            return found;
        }
        plan.state().things.forEach((thing, t) => {
            if (plan.groupOf(t) !== r) {
                return;
            }
            thing.channels.forEach((channel, c) => {
                if (channel.selected === true && typeof channel.item === 'object' && channel.item !== null) {
                    found.push([t, c]);
                }
            });
        });
        return found;
    }

    function badge(state) {
        const [drawing, key] = PLAN_STATES[state];
        return { icon: drawing, label: text(key), className: `state-${state}` };
    }

    /** @param {Array<Object>} [list] the problems the state stands for: their words are said with it */
    function stateMark(state, list = []) {
        const [drawing, key] = PLAN_STATES[state];
        const words = list.length === 0 ? text(key) : `${text(key)}: ${list.map(problemWords).join(' · ')}`;
        return el(document, 'span', { class: `badge state-${state}`, role: 'img', title: words, 'aria-label': words }, icon(document, drawing));
    }

    function greyed(state) {
        return state === 'out' || state === 'there' ? 'left-out' : null;
    }

    // The tree: the openHAB side.

    function treeRoots(places) {
        const state = plan.state();
        const things = state.things.map((thing, t) => {
            const of = thingState(places, t, thing);
            return {
                key: `t/${t}`,
                name: textOf(thing.label),
                mark: textOf(thing.id),
                icon: 'thing',
                badge: badge(of),
                className: greyed(of),
                children: thing.channels.map((channel, c) => {
                    const at = channelState(places, t, c, channel);
                    const item = typeof channel.item === 'object' && channel.item !== null ? channel.item : null;
                    return {
                        key: `c/${t}/${c}`,
                        name: textOf(channel.label),
                        mark: textOf(channel.channelType),
                        icon: 'channel',
                        badge: badge(at),
                        className: greyed(at),
                        children: item === null ? [] : [{ key: `i/${t}/${c}`, name: textOf(item.name), mark: textOf(item.type), icon: 'item', badge: badge(at), className: greyed(at) }],
                    };
                }),
            };
        });
        const roots = [{ key: 'bridge', name: textOf(bridge.label) === '' ? textOf(bridge.uid) : bridge.label, mark: textOf(bridge.uid), icon: 'bridge', badge: badge('there'), children: things }];
        if (state.rooms.length > 0) {
            // the project's spaces as they lie in each other; a space whose space above is not in the plan stands on top
            const ids = new Set(state.rooms.map((room) => room.id));
            const nodeOf = (room, r) => {
                const of = roomState(places, r, room);
                const look = SPACE_LOOKS[spaceKind(room.type)];
                const members = membersOf(r).map(([t, c]) => {
                    const channel = state.things[t].channels[c];
                    return { key: `m/${r}/${t}/${c}`, name: textOf(channel.item.name), mark: textOf(channel.item.type), icon: 'item', badge: badge(channelState(places, t, c, channel)) };
                });
                const below = state.rooms.map((other, at) => (at !== r && textOf(other.parentId) !== '' && other.parentId === room.id ? nodeOf(other, at) : null)).filter(Boolean);
                return { key: `g/${r}`, name: textOf(room.label), mark: textOf(room.groupName), icon: look.icon, iconTitle: text(look.word), badge: badge(of), className: greyed(of), children: [...below, ...members] };
            };
            const top = state.rooms.map((room, r) => (textOf(room.parentId) === '' || !ids.has(room.parentId) ? nodeOf(room, r) : null)).filter(Boolean);
            const any = state.rooms.some((room, r) => plan.made(r));
            roots.push({ key: 'groups', name: text('wizard.locations'), icon: 'group', badge: badge(any ? 'new' : 'out'), className: any ? null : 'left-out', children: top });
        }
        return roots;
    }

    /** {@return what a key of the tree names: its kind and the places in the plan} */
    function parts(key) {
        const [kind, ...numbers] = textOf(key).split('/');
        const [a, b, c] = numbers.map(Number);
        switch (kind) {
            case 't':
                return { kind, thing: a };
            case 'c':
            case 'i':
                return { kind, thing: a, channel: b };
            case 'g':
                return { kind, room: a };
            case 'm':
                return { kind, room: a, thing: b, channel: c };
            default:
                return { kind: kind === 'groups' ? 'groups' : 'bridge' };
        }
    }

    /** {@return whether the list shows the Thing for what is selected in the tree} */
    function inScope(t) {
        const at = parts(scope);
        const state = plan.state();
        if (at.kind === 'groups') {
            return plan.roomOf(t) >= 0;
        }
        if (at.kind === 'g') {
            return plan.within(plan.roomOf(t), at.room);
        }
        return at.thing === undefined || at.thing === t || !state.things[at.thing];
    }

    const tree = createTree({
        document,
        label: title,
        onSelect: (picked) => {
            scope = picked.key;
            cursor = null;
            const at = parts(scope);
            for (const table of Object.values(tables)) {
                table.select(undefined);
            }
            if (at.channel !== undefined && (tab === 'channels' || tab === 'items')) {
                tables[tab].select(tab === 'channels' ? `c/${at.thing}/${at.channel}/0` : `i/${at.thing}/${at.channel}`);
            } else if (at.kind === 'g' && tab === 'locations') {
                tables.locations.select(`g/${at.room}`);
            }
            drawCrumbs();
            drawList();
            drawSide();
            if (panel.closeTree()) {
                focusList();
            }
        },
    });
    panel.setTree(tree);

    // The rows of the lists.

    function channelRows(places) {
        const rows = [];
        plan.state().things.forEach((thing, t) => {
            if (!inScope(t)) {
                return;
            }
            thing.channels.forEach((channel, c) => {
                const list = listOf(channel.parts);
                const state = channelState(places, t, c, channel);
                (list.length === 0 ? [null] : list).forEach((part, p) => {
                    rows.push({ key: `c/${t}/${c}/${p}`, tab: 'channels', t, c, p, ref: { thing: t, channel: c, part: p }, thing, channel, part, first: p === 0, several: list.length > 1, state, problems: p === 0 ? (places.get(`c/${t}/${c}`) ?? []) : [] });
                });
            });
        });
        return rows;
    }

    function itemRows(places) {
        const rows = [];
        plan.state().things.forEach((thing, t) => {
            if (!inScope(t)) {
                return;
            }
            thing.channels.forEach((channel, c) => {
                if (typeof channel.item === 'object' && channel.item !== null) {
                    rows.push({ key: `i/${t}/${c}`, tab: 'items', t, c, ref: { thing: t, channel: c }, thing, channel, item: channel.item, first: true, state: channelState(places, t, c, channel), problems: places.get(`c/${t}/${c}`) ?? [] });
                }
            });
        });
        return rows;
    }

    function thingRows(places) {
        const rows = [];
        plan.state().things.forEach((thing, t) => {
            if (inScope(t)) {
                // a Thing's state is the worst of its channels
                const problems = [...(places.get(`t/${t}`) ?? []), ...thing.channels.flatMap((channel, c) => places.get(`c/${t}/${c}`) ?? [])];
                rows.push({ key: `t/${t}`, tab: 'things', t, ref: { thing: t }, thing, group: groupNameOf(plan.groupOf(t)), first: true, state: thingState(places, t, thing), problems });
            }
        });
        return rows;
    }

    /** {@return the spaces of the plan, those of the selected one and below it when one is selected} */
    function locationRows(places) {
        const at = parts(scope);
        const rows = [];
        plan.state().rooms.forEach((room, r) => {
            if (at.kind === 'g' && !plan.within(r, at.room)) {
                return;
            }
            rows.push({ key: `g/${r}`, tab: 'locations', r, ref: { room: r }, room, parent: plan.made(r) ? groupNameOf(plan.parentOf(r)) : '', members: membersOf(r).length, first: true, state: roomState(places, r, room), problems: places.get(`g/${r}`) ?? [] });
        });
        return rows;
    }

    /** {@return the addresses call 22 says will be read, each with its name and the channels that mark it} */
    function readRows() {
        const marked = new Map();
        plan.state().things.forEach((thing) => {
            for (const channel of thing.channels) {
                for (const part of listOf(channel.parts)) {
                    for (const [address, name] of [
                        [part.groupAddress, part.groupAddressName],
                        [part.statusAddress, part.statusName],
                    ]) {
                        if (textOf(address) !== '') {
                            const entry = marked.get(address) ?? { name: textOf(name), channels: [] };
                            if (channel.selected === true) {
                                entry.channels.push(text('wizard.channelOf', textOf(thing.label), textOf(channel.label)));
                            }
                            marked.set(address, entry);
                        }
                    }
                }
            }
        });
        return stringsOf(answer()?.readAddresses).map((address) => ({
            key: `r/${address}`,
            tab: 'reads',
            address,
            name: marked.get(address)?.name ?? '',
            channels: [...new Set(marked.get(address)?.channels ?? [])],
            devices: answered ? (answering.get(address) ?? []) : null,
        }));
    }

    /** Reads once which devices answer a read, from the project's objects; without the row model the column stays empty. */
    function loadAnswering() {
        if (answering !== null || typeof context.rows?.deviceDetails !== 'function') {
            return;
        }
        answering = new Map();
        Promise.resolve()
            .then(() => context.rows.deviceDetails())
            .then((details) => {
                for (const device of listOf(details?.devices)) {
                    for (const object of listOf(device.groupObjects)) {
                        const address = textOf(object.sendingGroupAddress);
                        if (address !== '' && object.read === true && object.communication === true) {
                            answering.set(address, [...(answering.get(address) ?? []), spaced(device.address, device.name)]);
                        }
                    }
                }
                answered = true;
                if (!closed && tab === 'reads') {
                    drawList();
                }
            })
            .catch(() => {
                // the column stays empty
            });
    }

    // Cells and editors.

    function applies(row, field) {
        if (PART_FIELDS.includes(field)) {
            return row.part !== null && row.part !== undefined;
        }
        if (ROOM_FIELDS.includes(field)) {
            return row.room !== null && row.room !== undefined;
        }
        return row.first === true && plan.get(row.ref, field) !== undefined;
    }

    function shown(ref, field) {
        const value = plan.get(ref, field);
        if (field === 'roomTag') {
            return value === '' ? text('wizard.noTag') : value;
        }
        if (field === 'statusAddress') {
            return value === '' ? text('wizard.noStatus') : spaced(value, plan.options(ref, field).find((option) => option.value === value)?.name);
        }
        return Array.isArray(value) ? value.join(', ') : textOf(value);
    }

    function optionWords(field, option) {
        if (field === 'roomTag') {
            return option.value === '' ? text('wizard.noTag') : option.value;
        }
        return field === 'statusAddress' ? (option.value === '' ? text('wizard.noStatus') : spaced(option.value, option.name)) : option.value;
    }

    function control(field, value, options, label) {
        const [kind, className] = FIELD[field];
        if (kind === 'tick') {
            return el(document, 'input', { type: 'checkbox', name: className, class: className, checked: value === true, 'aria-label': label });
        }
        if (kind === 'choice') {
            const select = el(
                document,
                'select',
                { name: className, class: className, 'aria-label': label },
                options.map((option) => el(document, 'option', { value: option.value }, option.words)),
            );
            select.value = textOf(value);
            return select;
        }
        return el(document, 'input', { type: 'text', name: className, class: className, value: textOf(value), autocomplete: 'off', 'aria-label': label });
    }

    function valueOf(field, input) {
        const kind = FIELD[field][0];
        if (kind === 'tick') {
            return input.checked === true;
        }
        return field === 'itemTags' ? tagsOf(input.value) : input.value;
    }

    function isCursor(row, field) {
        return cursor !== null && cursor.key === row.key && cursor.field === field;
    }

    function cell(row, field) {
        if (!applies(row, field)) {
            return null;
        }
        if (editing !== null && editing.key === row.key && editing.field === field) {
            return editing.node;
        }
        const enabled = plan.can(row.ref, field) && !working();
        if (FIELD[field][0] === 'tick') {
            const tick = control(field, plan.get(row.ref, field), [], text(FIELD[field][2]));
            attr(tick, 'disabled', !enabled);
            attr(tick, 'tabindex', '-1');
            attr(tick, 'title', enabled ? text('wizard.tickHint') : null);
            if (isCursor(row, field)) {
                tick.classList.add('current');
                marked = tick;
            }
            cellFields.set(tick, { row, field });
            return tick;
        }
        const words = shown(row.ref, field);
        if (!enabled) {
            return words;
        }
        const wrap = el(document, 'span', { class: isCursor(row, field) ? 'can-edit current' : 'can-edit', title: text('wizard.editHint') }, el(document, 'span', { class: 'value' }, words), el(document, 'span', { class: 'pencil' }, icon(document, 'pencil')));
        cellFields.set(wrap, { row, field });
        if (isCursor(row, field)) {
            marked = wrap;
        }
        return wrap;
    }

    function fieldColumn(field, key = FIELD[field][2]) {
        return { key: field, label: text(key), className: `col-${FIELD[field][1]}`, value: (row) => (applies(row, field) ? shown(row.ref, field) : ''), cell: (row) => cell(row, field) };
    }

    const stateColumn = { key: 'state', label: text('wizard.column.state'), className: 'col-state', value: (row) => text(PLAN_STATES[row.state][1]), cell: (row) => (row.first ? stateMark(row.state, row.problems) : null) };
    // stands after the state while a row of the list has a problem
    const problemColumn = {
        key: 'problems',
        label: text('wizard.column.problems'),
        className: 'col-problems',
        value: (row) => row.problems.map((problem) => spaced(problemWords(problem), problem.text)).join(' '),
        cell: (row) =>
            row.problems.map((problem) => {
                const words = problemWords(problem);
                return el(document, 'span', { class: problem.severity === 'ERROR' ? 'problem error' : 'problem warning' }, words, textOf(problem.text) === '' || words === problem.text ? null : el(document, 'span', { class: 'detail' }, problem.text));
            }),
    };
    const withProblems = { channels: false, items: false, things: false, locations: false };

    function notesOf(row) {
        if (!row.first) {
            return [];
        }
        const used = stringsOf(row.channel.alreadyConfigured);
        return [row.channel.statusOnly === true ? text('wizard.statusOnly') : null, ...stringsOf(row.channel.notes), used.length === 0 ? null : text('wizard.alreadyConfigured', used.join(', '))].filter((words) => words !== null);
    }

    const columns = {
        channels: [
            stateColumn,
            fieldColumn('selected'),
            fieldColumn('channelLabel', 'wizard.column.channel'),
            fieldColumn('channelType', 'wizard.column.type'),
            {
                key: 'address',
                label: text('wizard.column.address'),
                className: 'col-address',
                value: (row) => (row.part ? spaced(row.several || textOf(row.part.key) !== 'ga' ? row.part.key : '', row.part.groupAddress, row.part.groupAddressName) : ''),
                cell: (row) =>
                    row.part
                        ? [
                              row.several || textOf(row.part.key) !== 'ga' ? el(document, 'span', { class: 'key' }, textOf(row.part.key)) : null,
                              el(document, 'span', { class: 'address' }, textOf(row.part.groupAddress)),
                              el(document, 'span', { class: 'name' }, textOf(row.part.groupAddressName)),
                              textOf(row.part.dataType) === '' ? null : el(document, 'span', { class: 'kind' }, row.part.dataType),
                              row.part.secured === true ? el(document, 'span', { class: 'mark' }, text('addresses.secured')) : null,
                          ]
                        : null,
            },
            fieldColumn('read', 'wizard.column.read'),
            fieldColumn('statusAddress'),
            fieldColumn('statusRead', 'wizard.column.statusRead'),
            fieldColumn('itemName'),
            fieldColumn('itemType'),
            { key: 'from', label: text('wizard.column.from'), className: 'col-from', value: (row) => (row.part ? named('pairing', plan.pairing(row.ref)) : ''), cell: (row) => (row.part ? el(document, 'span', { class: 'pairing' }, named('pairing', plan.pairing(row.ref))) : null) },
            {
                key: 'notes',
                label: text('wizard.column.notes'),
                className: 'col-notes',
                value: (row) => notesOf(row).join(' '),
                cell: (row) => {
                    const split = row.first && plan.can(row.ref, 'split') ? el(document, 'button', { type: 'button', class: isCursor(row, SPLIT) ? 'split current' : 'split', tabindex: '-1', disabled: working(), title: text('wizard.splitHint') }, text('wizard.split')) : null;
                    if (split) {
                        actions.set(split, () => change([[row.ref, 'split', true]]));
                        if (isCursor(row, SPLIT)) {
                            marked = split;
                        }
                    }
                    return [split, notesOf(row).map((words) => el(document, 'span', { class: 'note' }, words))];
                },
            },
        ],
        items: [
            stateColumn,
            fieldColumn('selected'),
            fieldColumn('itemName', 'wizard.column.name'),
            fieldColumn('itemLabel', 'wizard.column.label'),
            fieldColumn('itemType', 'wizard.column.type'),
            fieldColumn('itemCategory'),
            fieldColumn('itemTags', 'wizard.column.tags'),
            {
                key: 'groups',
                label: text('wizard.column.groups'),
                className: 'col-groups',
                value: (row) => groupNameOf(plan.groupOf(row.t)),
            },
            { key: 'channel', label: text('wizard.column.linked'), className: 'col-linked', value: (row) => text('wizard.channelOf', textOf(row.thing.label), textOf(row.channel.label)) },
        ],
        things: [
            stateColumn,
            fieldColumn('thingLabel', 'wizard.column.label'),
            fieldColumn('thingId', 'wizard.column.id'),
            { key: 'channels', label: text('wizard.column.channels'), className: 'col-count', value: (row) => text('frame.count', row.thing.channels.filter((channel) => channel.selected === true).length, row.thing.channels.length) },
            { key: 'room', label: text('wizard.column.room'), className: 'col-room', value: (row) => textOf(row.thing.room) },
            { key: 'group', label: text('wizard.column.locationGroup'), className: 'col-groups', value: (row) => row.group },
        ],
        locations: [
            stateColumn,
            fieldColumn('make', 'wizard.selected'),
            {
                key: 'space',
                label: text('wizard.column.space'),
                className: 'col-space',
                value: (row) => spaced(text(SPACE_LOOKS[spaceKind(row.room.type)].word), row.room.name),
                cell: (row) => {
                    const look = SPACE_LOOKS[spaceKind(row.room.type)];
                    return [el(document, 'span', { class: 'kind-icon', role: 'img', title: text(look.word), 'aria-label': text(look.word) }, icon(document, look.icon)), ' ', el(document, 'span', { class: 'name' }, textOf(row.room.name))];
                },
            },
            fieldColumn('groupName'),
            fieldColumn('roomLabel', 'wizard.column.label'),
            fieldColumn('roomTag'),
            { key: 'parent', label: text('wizard.column.parent'), className: 'col-parent', value: (row) => row.parent },
            { key: 'members', label: text('wizard.column.members'), className: 'col-count', value: (row) => String(row.members) },
        ],
        reads: [
            { key: 'address', label: text('wizard.column.address'), className: 'col-address', value: (row) => row.address },
            { key: 'name', label: text('wizard.column.name'), className: 'col-name', value: (row) => row.name },
            { key: 'channels', label: text('wizard.column.markedBy'), className: 'col-linked', value: (row) => row.channels.join(' · ') },
            { key: 'devices', label: text('wizard.column.answers'), className: 'col-devices', value: (row) => (row.devices === null ? text('app.none') : row.devices.join(' · ')) },
        ],
    };

    /** What a row was drawn for: a row whose stamp changed is made again. */
    function stampOf(row) {
        const at = cursor !== null && cursor.key === row.key ? cursor.field : '';
        const edited = editing !== null && editing.key === row.key ? editing.field : '';
        const state = plan.state();
        const held = row.tab === 'things' ? [row.thing, row.group] : row.tab === 'locations' ? [row.parent, row.members] : [row.channel ?? null, row.thing?.label, row.thing?.id];
        return JSON.stringify([row.state, row.problems ?? null, held, row.room ?? null, row.address ?? null, row.channels ?? null, row.devices ?? null, state.readAtStart, state.locations, row.tab === 'items' ? state.rooms : null, at, edited, working()]);
    }

    function thingGroup(row) {
        if (parts(scope).kind !== 'bridge' || row.tab === 'things' || row.tab === 'reads' || row.tab === 'locations') {
            return null;
        }
        return { key: `t/${row.t}`, label: spaced(row.thing.label, row.thing.id) };
    }

    const tables = {};
    for (const name of ['channels', 'items', 'things', 'locations', 'reads']) {
        tables[name] = createTable({
            document,
            label: text(`wizard.tab.${name}`),
            columns: columns[name],
            keyOf: (row) => row.key,
            versionOf: (row) => row.stamp,
            rowClass: (row) => [greyed(row.state), row.first === false ? 'part-row' : null].filter(Boolean).join(' ') || null,
            groupOf: thingGroup,
            multi: name !== 'reads',
            layout,
            name: `wizard.${name}`,
            onSelect: (row) => {
                // the list is not drawn again inside its own click: only the mark is taken off
                if (cursor !== null && cursor.key !== row?.key) {
                    cursor = null;
                    marked?.classList.remove('current');
                }
                drawSide();
            },
            onOpen: (row, event) => open(row, event),
        });
    }
    const things = createCopyBox(document, 'things', text('wizard.thingsText'), context.clipboard);
    const items = createCopyBox(document, 'items', text('wizard.itemsText'), context.clipboard);
    const readsLine = el(document, 'p', { class: 'reads note' });
    const textPart = el(
        document,
        'div',
        { class: 'plan-text' },
        el(document, 'div', { class: 'text' }, el(document, 'h4', {}, text('wizard.thingsText')), things.field, things.tools),
        el(document, 'div', { class: 'text' }, el(document, 'h4', {}, text('wizard.itemsText')), items.field, items.tools),
    );
    const textNote = el(document, 'p', { class: 'note' }, text('wizard.textWaits'));
    const modeButtons = ['new', 'lines'].map((mode) => {
        const choice = el(document, 'button', { type: 'button', class: `text-mode-${mode}`, 'aria-pressed': 'false' }, text(`wizard.text.${mode}`));
        actions.set(choice, () => {
            textMode = mode;
            drawList();
        });
        return choice;
    });
    const modeBar = el(document, 'div', { class: 'text-mode', role: 'group', 'aria-label': text('wizard.text.choice') }, modeButtons);

    /** The value of the choice list's line that lets the viewer type an id: no Thing UID looks like it. */
    const TYPED = '*';

    /** {@return per Thing of the plan the choice of the existing Thing its lines are for, the note and the warnings} */
    function linesPart(got) {
        targetFields.clear();
        const offered = plan.info.deviceThings;
        const rows = plan.state().things.map((thing, t) => {
            const target = textOf(thing.target);
            const listed = offered.some((device) => device.uid === target);
            const typed = typedTargets.has(t) || (target !== '' && !listed);
            const label = text('wizard.target', textOf(thing.label));
            const select = el(
                document,
                'select',
                { name: 'target-thing', class: 'target-thing', 'aria-label': label, disabled: working() },
                el(document, 'option', { value: '' }, text('wizard.targetNone')),
                offered.map((device) => el(document, 'option', { value: device.uid }, spaced(device.label, device.uid))),
                el(document, 'option', { value: TYPED }, text('wizard.targetOther')),
            );
            select.value = typed ? TYPED : target;
            targetFields.set(select, { t, typed: false });
            let field = null;
            if (typed) {
                field = el(document, 'input', { type: 'text', name: 'target-id', class: 'target-id', value: target, autocomplete: 'off', placeholder: text('wizard.targetId'), 'aria-label': text('wizard.targetId'), disabled: working() });
                targetFields.set(field, { t, typed: true });
            }
            return el(document, 'li', {}, el(document, 'span', { class: 'name' }, textOf(thing.label)), ' ', el(document, 'span', { class: 'kind' }, textOf(thing.id)), ' ', select, field ? [' ', field] : null);
        });
        const lines = typeof got?.lines === 'object' && got.lines !== null ? got.lines : null;
        return el(
            document,
            'div',
            { class: 'lines-targets' },
            el(document, 'p', { class: 'note lines-note' }, text('wizard.linesNote')),
            el(document, 'ul', { class: 'targets' }, rows),
            listOf(lines?.existingChannels).map((entry) => el(document, 'p', { class: 'note warn lines-existing' }, text('wizard.linesExisting', stringsOf(entry.channelIds).length, textOf(entry.target)))),
            lines === null ? el(document, 'p', { class: 'note lines-none' }, text('wizard.linesNone')) : count(lines.withoutTarget) > 0 ? el(document, 'p', { class: 'note lines-without' }, text('wizard.linesWithout', count(lines.withoutTarget))) : null,
        );
    }

    /** {@return the cells of the row the mark can stand on: the fields that can be changed, and last the Split button} */
    function editableFields(row) {
        const fields = (columns[row.tab] ?? []).map((column) => column.key).filter((field) => Object.hasOwn(FIELD, field) && applies(row, field) && plan.can(row.ref, field));
        return row.tab === 'channels' && row.first === true && plan.can(row.ref, 'split') ? [...fields, SPLIT] : fields;
    }

    function split(row) {
        cursor = null;
        change([[row.ref, 'split', true]]);
        focusList();
    }

    function finishEdit() {
        const was = editing;
        editing = null;
        if (was) {
            for (const [type, listener] of was.listeners) {
                was.node.removeEventListener(type, listener);
            }
        }
        return was;
    }

    function startEdit(row, field) {
        if (working() || !applies(row, field) || !plan.can(row.ref, field)) {
            return;
        }
        finishEdit();
        cursor = { key: row.key, field };
        const kind = FIELD[field][0];
        const options = plan.options(row.ref, field).map((option) => ({ value: option.value, words: optionWords(field, option) }));
        const input = control(field, kind === 'text' ? shown(row.ref, field) : plan.get(row.ref, field), options, text(FIELD[field][2]));
        attr(input, 'class', 'cell-editor');
        const commit = () => {
            if (editing?.node !== input) {
                return;
            }
            const value = valueOf(field, input);
            finishEdit();
            if (!change([[row.ref, field, value]])) {
                drawList();
            }
            focusList();
        };
        const listeners = [
            [
                'keydown',
                (event) => {
                    // the keys are the editor's: the list would move its selection with them
                    event.stopPropagation();
                    if (event.key === 'Enter') {
                        event.preventDefault();
                        commit();
                    } else if (event.key === 'Escape') {
                        event.preventDefault();
                        finishEdit();
                        drawList();
                        focusList();
                    }
                },
            ],
            [
                'change',
                (event) => {
                    event.stopPropagation();
                    // a closed choice list says "change" with each arrow key: it is kept on Enter or on leaving
                    if (kind !== 'choice') {
                        commit();
                    }
                },
            ],
            ['focusout', commit],
            ['dblclick', (event) => event.stopPropagation()],
        ];
        for (const [type, listener] of listeners) {
            input.addEventListener(type, listener);
        }
        editing = { key: row.key, field, node: input, listeners };
        drawList();
        input.focus();
        if (kind === 'text') {
            input.select();
        }
    }

    /** {@return the control or the wrap of the cell the target is in, when that cell can be changed} */
    function cellAt(target) {
        for (let at = target; at && at !== panel.list; at = at.parentNode) {
            if (cellFields.has(at)) {
                return at;
            }
        }
        return null;
    }

    function fieldAt(target) {
        return cellFields.get(cellAt(target)) ?? null;
    }

    /**
     * A double click opens the editor of its cell; Enter or F2 the one the row's mark stands on, without a mark the
     * row's first text; on the Split button they split. A tick box has no editor: neither of them flips it.
     */
    function open(row, event) {
        if (row.tab === 'reads') {
            return;
        }
        const at = event ? fieldAt(event.target) : null;
        if (event && at === null) {
            return;
        }
        const fields = editableFields(row).filter((field) => field !== SPLIT);
        const first = fields.find((field) => FIELD[field][0] === 'text') ?? fields.find((field) => FIELD[field][0] !== 'tick');
        const field = at?.field ?? (cursor?.key === row.key ? cursor.field : first);
        if (field === SPLIT) {
            split(row);
        } else if (field !== undefined && FIELD[field][0] !== 'tick') {
            startEdit(row, field);
        }
    }

    function focusList() {
        if (tab === 'text' || !tables[tab].focus()) {
            panel.focusList();
        }
    }

    // Drawing.

    function drawList() {
        const places = problemsByPlace();
        const rows = { channels: channelRows(places), items: itemRows(places), things: thingRows(places), locations: locationRows(places), reads: readRows() };
        const state = plan.state();
        const inside = state.things.filter((thing, t) => inScope(t));
        panel.setTabs(
            [
                { key: 'channels', label: text('wizard.tab.channels'), count: inside.reduce((sum, thing) => sum + thing.channels.length, 0) },
                { key: 'items', label: text('wizard.tab.items'), count: rows.items.length },
                { key: 'things', label: text('wizard.tab.things'), count: rows.things.length },
                { key: 'locations', label: text('wizard.tab.locations'), count: rows.locations.length },
                { key: 'reads', label: text('wizard.tab.reads'), count: rows.reads.length },
                { key: 'text', label: text('wizard.tab.text') },
            ],
            tab,
            (key) => {
                finishEdit();
                tab = key;
                cursor = null;
                drawList();
                drawSide();
            },
        );
        if (tab === 'text') {
            const got = answer();
            const ready = typeof got?.thingsText === 'string' || typeof got?.itemsText === 'string';
            const lines = textMode === 'lines';
            // both texts are the answer's: the page writes no line itself
            const shownText = lines ? got?.lines : got;
            things.field.value = textOf(shownText?.thingsText);
            items.field.value = textOf(shownText?.itemsText);
            modeButtons.forEach((choice, at) => attr(choice, 'aria-pressed', (at === 1) === lines ? 'true' : 'false'));
            const focused = [...targetFields.keys()].indexOf(document.activeElement);
            place(panel.find, []);
            place(panel.list, [modeBar, lines ? linesPart(got) : null, !ready ? textNote : lines && typeof got?.lines !== 'object' ? null : textPart].filter(Boolean));
            if (focused >= 0) {
                [...targetFields.keys()][focused]?.focus();
            }
            return;
        }
        if (tab === 'reads') {
            loadAnswering();
            const got = answer();
            const telegrams = count(got?.readTelegrams);
            clear(readsLine);
            add(document, readsLine, got === null ? text('wizard.readsUnknown') : text('wizard.reads', telegrams, telegrams === 0 ? text('wizard.readsNone') : stringsOf(got.readAddresses).join(', ')));
        }
        const table = tables[tab];
        for (const row of rows[tab]) {
            row.stamp = stampOf(row);
        }
        const bad = tab !== 'reads' && rows[tab].some((row) => row.problems.length > 0);
        if (tab !== 'reads' && bad !== withProblems[tab]) {
            withProblems[tab] = bad;
            table.setColumns(bad ? [stateColumn, problemColumn, ...columns[tab].slice(1)] : columns[tab]);
        }
        place(panel.find, [table.findField]);
        place(panel.list, tab === 'reads' ? [readsLine, table.node] : [table.node]);
        table.setRows(rows[tab], { none: text(tab === 'reads' ? 'wizard.readsEmpty' : tab === 'locations' ? 'wizard.noLocations' : 'wizard.nothingProposed'), noMatch: text('wizard.noMatch') });
    }

    function drawTree() {
        tree.setRoots(treeRoots(problemsByPlace()));
        if (tree.select(scope) === null) {
            scope = 'bridge';
            tree.select(scope);
        }
        tree.open('bridge');
        drawCrumbs();
    }

    function drawCrumbs() {
        panel.setCrumbs(
            tree.path(scope).map((entry) => ({ key: entry.key, label: entry.name })),
            (key) => {
                scope = key;
                tree.select(scope);
                redraw();
            },
        );
    }

    function drawNotes() {
        clear(notes);
        const info = plan.info;
        add(
            document,
            notes,
            el(document, 'p', { class: 'note no-central' }, text('wizard.noCentral')),
            info.unknown.length > 0 ? el(document, 'p', { class: 'note warn' }, text('wizard.unknown', info.unknown.join(', '))) : null,
            info.leftOut.functions > 0 || info.leftOut.devices > 0 ? el(document, 'p', { class: 'note left-out' }, text('wizard.leftOut', info.leftOut.functions, info.leftOut.devices)) : null,
            info.thingsWithoutHandler > 0 ? el(document, 'p', { class: 'note' }, text('wizard.thingsWithoutHandler', info.thingsWithoutHandler)) : null,
            plan.state().things.length === 0 ? el(document, 'p', { class: 'note nothing' }, text('wizard.nothingProposed')) : null,
        );
    }

    function problemItem(problem) {
        const words = problemWords(problem);
        const line = el(
            document,
            'button',
            { type: 'button', class: 'check' },
            el(document, 'span', { class: 'severity' }, named('severity', problem.severity)),
            ' ',
            words,
            textOf(problem.text) === '' || words === problem.text ? null : el(document, 'span', { class: 'detail' }, problem.text),
        );
        actions.set(line, () => goTo(whereOf(problem.where, check().answer?.made)));
        return el(document, 'li', { class: problem.severity === 'ERROR' ? 'error' : 'warning' }, line);
    }

    /** Selects what a problem stands at: its channel's row, its Thing, or its location group. */
    function goTo(at) {
        if (at === null) {
            return;
        }
        finishEdit();
        cursor = null;
        if (at.room !== undefined) {
            scope = `g/${at.room}`;
            tab = 'locations';
        } else {
            scope = `t/${at.thing}`;
            tab = at.channel === undefined ? 'things' : 'channels';
        }
        redraw();
        tables[tab].select(at.room !== undefined ? `g/${at.room}` : at.channel === undefined ? `t/${at.thing}` : `c/${at.thing}/${at.channel}/0`);
        drawSide();
        focusList();
    }

    function formField(refs, field, words) {
        const [kind, , key] = FIELD[field];
        const values = refs.map((ref) => plan.get(ref, field));
        const alike = values.every((value) => JSON.stringify(value) === JSON.stringify(values[0]));
        let options = [];
        if (kind === 'choice') {
            const lists = refs.map((ref) => plan.options(ref, field));
            options = lists[0].filter((option) => lists.every((list) => list.some((other) => other.value === option.value))).map((option) => ({ value: option.value, words: optionWords(field, option) }));
            if (!alike) {
                options.unshift({ value: '', words: text('wizard.mixed') });
            }
        }
        const label = words ?? text(key);
        const value = alike ? (kind === 'text' ? shown(refs[0], field) : values[0]) : kind === 'tick' ? false : '';
        const input = control(field, value, options, label);
        attr(input, 'disabled', working() || !refs.some((ref) => plan.can(ref, field)) || (kind === 'choice' && options.length < 2));
        formFields.set(input, { refs, field, mixed: !alike });
        if (kind === 'tick') {
            input.indeterminate = !alike;
        }
        const help = hasText(`wizard.help.${field}`) ? el(document, 'span', { class: 'help' }, text(`wizard.help.${field}`)) : null;
        if (kind === 'tick') {
            return el(document, 'div', { class: 'form-field' }, el(document, 'label', { class: 'tick' }, input, ' ', label), help);
        }
        return el(document, 'div', { class: 'form-field' }, el(document, 'label', { class: 'field' }, el(document, 'span', { class: 'caption' }, label), input), help);
    }

    function channelForm(t, c) {
        const state = plan.state();
        const thing = state.things[t];
        const channel = thing?.channels[c];
        if (!channel) {
            return [];
        }
        const ref = { thing: t, channel: c };
        const split = plan.can(ref, 'split') ? el(document, 'button', { type: 'button', class: 'split', disabled: working() }, text('wizard.split')) : null;
        if (split) {
            actions.set(split, () => change([[ref, 'split', true]]));
        }
        const several = listOf(channel.parts).length > 1;
        return [
            el(document, 'h4', {}, text('wizard.channelOf', textOf(thing.label), textOf(channel.label))),
            CHANNEL_FIELDS.map((field) => formField([ref], field)),
            split,
            listOf(channel.parts).map((part, p) => {
                const at = { ...ref, part: p };
                return el(
                    document,
                    'fieldset',
                    { class: 'part' },
                    el(document, 'legend', {}, spaced(several || textOf(part.key) !== 'ga' ? part.key : '', part.groupAddress, part.groupAddressName)),
                    PART_FIELDS.map((field) => formField([at], field)),
                    el(document, 'p', { class: 'note' }, el(document, 'span', { class: 'pairing' }, named('pairing', plan.pairing(at)))),
                );
            }),
            typeof channel.item === 'object' && channel.item !== null ? [el(document, 'h4', {}, text('kind.ITEM')), ITEM_FIELDS.map((field) => formField([ref], field))] : null,
        ];
    }

    function roomForm(r) {
        const room = plan.state().rooms[r];
        if (!room) {
            return [];
        }
        const ref = { room: r };
        const above = groupNameOf(plan.parentOf(r));
        return [
            el(document, 'h4', {}, text('wizard.locationOf', text(SPACE_LOOKS[spaceKind(room.type)].word), textOf(room.name))),
            formField([ref], 'make', text('wizard.locationGroup', textOf(room.groupName))),
            formField([ref], 'groupName'),
            formField([ref], 'roomLabel'),
            formField([ref], 'roomTag'),
            el(document, 'p', { class: 'note location-parent' }, above === '' ? text('wizard.locationTop') : text('wizard.locationIn', above)),
            groupExists(room) && plan.made(r) ? el(document, 'p', { class: 'note location-exists' }, text('wizard.locationExists')) : null,
        ];
    }

    function thingForm(t) {
        if (!plan.state().things[t]) {
            return [];
        }
        const ref = { thing: t };
        const group = groupNameOf(plan.groupOf(t));
        return [el(document, 'h4', {}, text('kind.THING')), formField([ref], 'thingLabel'), formField([ref], 'thingId'), group === '' ? null : el(document, 'p', { class: 'note thing-group' }, text('wizard.thingGroup', group))];
    }

    function sharedForm(rows) {
        const fields = SHARED_FIELDS[tab] ?? [];
        return [
            el(document, 'h4', {}, text('wizard.rowsSelected', rows.length)),
            fields.map((field) => {
                const seen = new Set();
                const refs = rows
                    .map((row) => (PART_FIELDS.includes(field) || ROOM_FIELDS.includes(field) ? row.ref : { thing: row.t, channel: row.c }))
                    .filter((ref) => {
                        const key = JSON.stringify(ref);
                        return plan.get(ref, field) === undefined || seen.has(key) ? false : seen.add(key);
                    });
                return refs.length === 0 ? null : formField(refs, field);
            }),
        ];
    }

    /** {@return the one channel the sidebar is about, or null} */
    function oneChannel() {
        const rows = tab === 'text' ? [] : tables[tab].picked();
        if (rows.length === 1 && rows[0].c !== undefined) {
            return { thing: rows[0].t, channel: rows[0].c };
        }
        const at = parts(scope);
        return rows.length === 0 && at.channel !== undefined ? { thing: at.thing, channel: at.channel } : null;
    }

    function editForm() {
        const rows = tab === 'text' || tab === 'reads' ? [] : tables[tab].picked();
        if (rows.length > 1) {
            return sharedForm(rows);
        }
        const one = oneChannel();
        if (one) {
            return channelForm(one.thing, one.channel);
        }
        if (rows.length === 1 && rows[0].tab === 'things') {
            return thingForm(rows[0].t);
        }
        if (rows.length === 1 && rows[0].tab === 'locations') {
            return roomForm(rows[0].r);
        }
        const at = parts(scope);
        switch (at.kind) {
            case 't':
                return thingForm(at.thing);
            case 'g':
                return roomForm(at.room);
            case 'groups':
                return [formField([{}], 'locations'), plan.state().rooms.map((room, r) => formField([{ room: r }], 'make', text('wizard.locationGroup', textOf(room.groupName))))];
            default:
                return [el(document, 'p', { class: 'note' }, text('wizard.editNone'))];
        }
    }

    function link(route, words) {
        return el(document, 'a', { class: 'open', href: href({ ...route, bridge: bridge.uid }) }, words);
    }

    function sourceFacts() {
        const one = oneChannel();
        const state = plan.state();
        const thing = state.things[one?.thing ?? parts(scope).thing];
        if (!thing) {
            return [el(document, 'p', { class: 'note' }, text('wizard.sourceNone'))];
        }
        const facts = [
            [text('wizard.thingFrom', named('source', thing.source)), thing.source === 'DEVICE' && textOf(thing.sourceId) !== '' ? link({ view: 'topology', query: { sel: `device:${thing.sourceId}` } }, text('frame.open')) : null],
            textOf(thing.room) === '' ? null : [text('wizard.thingRoom', thing.room), null],
        ];
        const channel = one ? thing.channels[one.channel] : null;
        for (const [p, part] of listOf(channel?.parts).entries()) {
            const at = { ...one, part: p };
            facts.push([spaced(part.key, part.groupAddress, part.groupAddressName, part.dataType), textOf(part.groupAddress) === '' ? null : link({ view: 'group-addresses', query: { sel: addressKey(part.groupAddress) } }, text('frame.open'))]);
            if (textOf(part.statusAddress) !== '') {
                facts.push([spaced(text('wizard.status'), shown(at, 'statusAddress'), named('pairing', plan.pairing(at))), link({ view: 'group-addresses', query: { sel: addressKey(part.statusAddress) } }, text('frame.open'))]);
            }
        }
        const used = stringsOf(channel?.alreadyConfigured);
        return [
            el(
                document,
                'ul',
                { class: 'facts' },
                facts.filter(Boolean).map(([words, more]) => el(document, 'li', {}, words, more ? [' ', more] : null)),
            ),
            stringsOf(channel?.notes).map((words) => el(document, 'p', { class: 'note' }, words)),
            used.length === 0 ? null : el(document, 'p', { class: 'note' }, text('wizard.alreadyConfigured', used.join(', '))),
        ];
    }

    function drawSide() {
        const focused = [...formFields.keys()].indexOf(document.activeElement);
        formFields.clear();
        clear(editBody);
        add(document, editBody, editForm());
        if (focused >= 0) {
            [...formFields.keys()][focused]?.focus();
        }
        clear(checksBody);
        const list = problems();
        const state = check();
        add(
            document,
            checksBody,
            state.pending ? el(document, 'p', { class: 'note checking' }, text('wizard.checking')) : null,
            state.error ? el(document, 'p', { class: 'note warn' }, errorText(state.error.code, state.error.text)) : null,
            list.length === 0 ? (state.answer ? el(document, 'p', { class: 'note' }, text('wizard.noProblems')) : null) : el(document, 'ul', { class: 'problems' }, list.map(problemItem)),
        );
        clear(sourceBody);
        add(document, sourceBody, sourceFacts());
    }

    function drawSummary() {
        const state = check();
        const got = state.answer;
        const fresh = got !== null && got.version === plan.version();
        const own = plan.counts();
        const counts = fresh && typeof got.answer?.counts === 'object' && got.answer.counts !== null ? got.answer.counts : own;
        clear(willLine);
        add(
            document,
            willLine,
            text('wizard.summary.will', count(counts.things), count(counts.channels), count(counts.items), count(counts.groups), count(counts.links)),
            ' ',
            text('wizard.summary.leaves', own.leftAlone),
            ' ',
            got === null ? text('wizard.summary.readsUnknown') : text('wizard.summary.reads', stringsOf(got.answer?.readAddresses).length, count(got.answer?.readTelegrams)),
        );
        const list = problems();
        const errors = list.filter((problem) => problem.severity === 'ERROR').length;
        clear(problemLine);
        add(document, problemLine, got === null ? null : text('wizard.summary.problems', errors, list.length - errors));
        problemLine.classList.toggle('has-errors', errors > 0);
        clear(stateLine);
        let words = '';
        if (notice !== '') {
            words = notice;
        } else if (state.pending) {
            words = text('wizard.checking');
        } else if (state.error) {
            words = errorText(state.error.code, state.error.text);
        } else if (got?.used) {
            words = text('wizard.used');
        } else if (errors > 0) {
            words = text('wizard.fixErrors');
        }
        add(document, stateLine, words);
        const busy = working();
        attr(chooseButton, 'disabled', busy);
        attr(undoButton, 'disabled', busy || !plan.canUndo());
        attr(redoButton, 'disabled', busy || !plan.canRedo());
        attr(resetButton, 'disabled', busy || !plan.changed());
        attr(recheckButton, 'disabled', busy);
        attr(createButton, 'disabled', !canCreate());
        const left = lastRun();
        clear(lastRunButton);
        add(document, lastRunButton, text('wizard.lastRun', left));
        attr(lastRunButton, 'disabled', busy);
        place(summaryTools, [chooseButton, undoButton, redoButton, resetButton, recheckButton, left > 0 ? lastRunButton : null, createButton].filter(Boolean));
        readSwitch.checked = plan.state().readAtStart;
        attr(readSwitch, 'disabled', busy);
        locationSwitch.checked = plan.state().locations === true;
        attr(locationSwitch, 'disabled', busy || plan.state().rooms.length === 0);
    }

    function redraw() {
        if (closed) {
            return;
        }
        drawNotes();
        drawTree();
        drawList();
        drawSide();
        drawSummary();
    }

    /** Makes the changes in the plan and tells the owner. {@return whether anything changed} */
    function change(changes) {
        if (working() || !plan.apply(changes)) {
            return false;
        }
        notice = '';
        on.changed();
        return true;
    }

    function buttonOf(target) {
        let at = target;
        while (at && at !== node && at.tagName !== 'BUTTON') {
            at = at.parentNode;
        }
        return at;
    }

    const onClick = (event) => {
        const target = buttonOf(event.target);
        // a click on a cell that can be changed puts the row's mark there, for Enter
        const at = editing === null ? fieldAt(event.target) : null;
        if (at && FIELD[at.field][0] !== 'tick' && tables[tab]?.selected()?.key === at.row.key) {
            marked?.classList.remove('current');
            cursor = { key: at.row.key, field: at.field };
            marked = cellAt(event.target);
            marked.classList.add('current');
        }
        if (actions.has(target)) {
            actions.get(target)();
        } else if (target === chooseButton) {
            on.choose();
        } else if (target === recheckButton) {
            on.recheck();
        } else if (target === lastRunButton) {
            on.lastRun();
        } else if (target === createButton) {
            notice = '';
            drawSummary();
            on.create();
        } else if (!working() && ((target === undoButton && plan.undo()) || (target === redoButton && plan.redo()) || (target === resetButton && plan.reset()))) {
            finishEdit();
            notice = '';
            on.changed();
        }
    };
    const onChange = (event) => {
        const target = event.target;
        if (target === readSwitch) {
            change([[{}, 'readAtStart', readSwitch.checked]]);
        } else if (target === locationSwitch) {
            change([[{}, 'locations', locationSwitch.checked]]);
        } else if (targetFields.has(target)) {
            const { t, typed } = targetFields.get(target);
            let value = target.value.trim();
            if (!typed && target.value === TYPED) {
                typedTargets.add(t);
                value = '';
            } else if (!typed) {
                typedTargets.delete(t);
            }
            if (!change([[{ thing: t }, 'thingTarget', value]])) {
                drawList();
            }
        } else if (formFields.has(target)) {
            const { refs, field, mixed } = formFields.get(target);
            // the empty first line of a choice over rows that differ is no value
            if (mixed && FIELD[field][0] === 'choice' && target.value === '') {
                return;
            }
            const value = valueOf(field, target);
            if (!change(refs.map((ref) => [ref, field, value]))) {
                drawSide();
            }
        } else {
            const at = cellFields.get(target);
            if (at && FIELD[at.field][0] === 'tick' && !change([[at.row.ref, at.field, target.checked]])) {
                drawList();
            }
        }
    };
    const onKey = (event) => {
        const step = event.key === 'ArrowRight' ? 1 : event.key === 'ArrowLeft' ? -1 : 0;
        const row = tab === 'text' ? null : tables[tab].selected();
        if (row === null || editing !== null || event.target?.tagName !== 'TR') {
            return;
        }
        if (event.key === 'F2') {
            event.preventDefault();
            open(row);
            return;
        }
        if (event.key === ' ' && cursor?.key === row.key && cursor.field === SPLIT && editableFields(row).includes(SPLIT)) {
            event.preventDefault();
            split(row);
            return;
        }
        if (event.key === ' ' && cursor?.key === row.key && cursor.field !== SPLIT && FIELD[cursor.field][0] === 'tick' && editableFields(row).includes(cursor.field)) {
            event.preventDefault();
            change([[row.ref, cursor.field, !plan.get(row.ref, cursor.field)]]);
            return;
        }
        if (step === 0) {
            return;
        }
        const fields = editableFields(row);
        if (fields.length === 0) {
            return;
        }
        event.preventDefault();
        const at = cursor?.key === row.key ? fields.indexOf(cursor.field) : -1;
        cursor = { key: row.key, field: fields[Math.min(fields.length - 1, Math.max(0, at < 0 ? (step > 0 ? 0 : fields.length - 1) : at + step))] };
        drawList();
    };
    node.addEventListener('click', onClick);
    node.addEventListener('change', onChange);
    panel.list.addEventListener('keydown', onKey);
    redraw();

    return {
        node,
        redraw,
        /** {@return what is selected, for the next time the screen is shown} */
        view: () => ({ scope, tab, textMode }),
        /** {@return the name of each address read at start, by address} */
        readNames: () => new Map(readRows().map((row) => [row.address, row.name])),
        /** Says a line of the owner in the summary bar until the plan is changed or Create is pressed again. */
        say(words) {
            notice = words;
            drawSummary();
        },
        /** Puts the keyboard focus back on the button that opened the sheet. */
        focusCreate: () => createButton.focus(),
        close() {
            closed = true;
            finishEdit();
            node.removeEventListener('click', onClick);
            node.removeEventListener('change', onChange);
            panel.list.removeEventListener('keydown', onKey);
            for (const table of Object.values(tables)) {
                table.close();
            }
            things.close();
            items.close();
            tree.close();
            splitter.close();
            sidebar.close();
            panel.close();
        },
    };
}
