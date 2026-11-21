// The wizard's step runner: it creates what the steps of a preview say, through core.js, one step after another, and
// undoes what one run created. It never overwrites: every step is checked before the first create and once more just
// before its own create, the first failure stops the run, nothing is retried by itself, and the undo list holds only
// what this run created.

import { CHANNEL_UID, ITEM_NAME, KINDS, THING_UID } from './core.js';

/** The mark of an entry of the undo list whose create lost its answer and that was found afterwards. */
export const ANSWER_LOST = 'answer-lost';
/** The codes a run or an undo can end with, besides http-<status>. */
export const PLAN_CODES = ['bad-plan', 'busy', 'exists', 'exists-now', 'group-gone', 'item-overwritten', ANSWER_LOST, 'network', 'signed-out', 'failed'];

const BODY = { GROUP: 'item', THING: 'thing', ITEM: 'item', LINK: 'link' };
const UNDO_RANK = { LINK: 0, ITEM: 1, THING: 2, GROUP: 3 };

let busy = false;
const runLists = new WeakSet();

function undoList(entries) {
    const list = Object.freeze(entries.map((entry) => Object.freeze(entry)));
    runLists.add(list);
    return list;
}

function isObject(value) {
    return value !== null && typeof value === 'object' && !Array.isArray(value);
}

function matches(pattern, value) {
    return typeof value === 'string' && pattern.test(value);
}

/**
 * {@return null when the steps may run, else the first step that may not, as {index, reason}}
 *
 * Beyond the kinds and the id patterns, a plan must be one call 22 can give: groups, Things, Items, links in this order;
 * every body names the ids of its step; no Thing setting; a group in at most one group, made before it or one openHAB
 * has already; an Item only in such groups; a link only between an Item and a channel of the plan; nothing twice.
 *
 * @param {Array<string>} [existing] the groups call 22 says openHAB has already: used as they are, never made
 */
export function checkPlan(steps, existing = []) {
    if (!Array.isArray(steps) || steps.length === 0) {
        return { index: -1, reason: 'empty' };
    }
    if (!Array.isArray(existing) || existing.some((name) => !matches(ITEM_NAME, name))) {
        return { index: -1, reason: 'bad-existing' };
    }
    const things = new Map();
    const groups = new Set();
    const known = new Set(existing);
    const items = new Set();
    const links = new Set();
    let rank = 0;
    for (const [index, step] of steps.entries()) {
        const reason = stepProblem(step, rank, { things, groups, items, links, known });
        if (reason !== null) {
            return { index, reason };
        }
        rank = KINDS.indexOf(step.kind);
    }
    return null;
}

function stepProblem(step, rank, seen) {
    if (!isObject(step)) {
        return 'bad-step';
    }
    const at = KINDS.indexOf(step.kind);
    if (at < 0) {
        return 'bad-kind';
    }
    if (at < rank) {
        return 'bad-order';
    }
    const bodies = ['thing', 'item', 'link'].filter((name) => step[name] !== undefined && step[name] !== null);
    const body = step[BODY[step.kind]];
    if (bodies.length !== 1 || bodies[0] !== BODY[step.kind] || !isObject(body)) {
        return 'bad-body';
    }
    switch (step.kind) {
        case 'THING':
            return thingProblem(step, body, seen);
        case 'LINK':
            return linkProblem(step, body, seen);
        default:
            return itemProblem(step, body, seen);
    }
}

function thingProblem(step, body, seen) {
    if (!matches(THING_UID, step.uid)) {
        return 'bad-id';
    }
    if (body.UID !== step.uid || !Array.isArray(body.channels)) {
        return 'bad-body';
    }
    if (!isObject(body.configuration) || Object.keys(body.configuration).length > 0) {
        return 'thing-setting';
    }
    const channels = new Set();
    for (const channel of body.channels) {
        if (!isObject(channel) || !matches(CHANNEL_UID, channel.uid) || !channel.uid.startsWith(`${step.uid}:`)) {
            return 'bad-body';
        }
        if (!isObject(channel.configuration) || Object.hasOwn(channel.configuration, 'address')) {
            return 'thing-setting';
        }
        channels.add(channel.uid);
    }
    if (seen.things.has(step.uid)) {
        return 'duplicate';
    }
    seen.things.set(step.uid, channels);
    return null;
}

function itemProblem(step, body, seen) {
    if (!matches(ITEM_NAME, step.itemName)) {
        return 'bad-id';
    }
    const group = step.kind === 'GROUP';
    if (body.name !== step.itemName || (body.type === 'Group') !== group || !Array.isArray(body.groupNames)) {
        return 'bad-body';
    }
    if ((group && body.groupNames.length > 1) || body.groupNames.some((name) => !seen.groups.has(name) && !seen.known.has(name))) {
        return 'unknown-group';
    }
    if (seen.groups.has(step.itemName) || seen.items.has(step.itemName) || seen.known.has(step.itemName)) {
        return 'duplicate';
    }
    (group ? seen.groups : seen.items).add(step.itemName);
    return null;
}

function linkProblem(step, body, seen) {
    if (!matches(ITEM_NAME, step.itemName) || !matches(CHANNEL_UID, step.channelUID)) {
        return 'bad-id';
    }
    if (body.itemName !== step.itemName || body.channelUID !== step.channelUID) {
        return 'bad-body';
    }
    const thing = step.channelUID.slice(0, step.channelUID.lastIndexOf(':'));
    if (!seen.items.has(step.itemName) || !seen.things.get(thing)?.has(step.channelUID)) {
        return 'unknown-link';
    }
    const key = `${step.itemName} ${step.channelUID}`;
    if (seen.links.has(key)) {
        return 'duplicate';
    }
    seen.links.add(key);
    return null;
}

function entryOf(step, index, mark) {
    const entry = { kind: step.kind, step: index };
    if (step.kind === 'THING') {
        entry.uid = step.uid;
    } else {
        entry.itemName = step.itemName;
    }
    if (step.kind === 'LINK') {
        entry.channelUID = step.channelUID;
    }
    if (mark !== undefined) {
        entry.mark = mark;
    }
    return entry;
}

function codeOf(error) {
    return error?.code === 'network' || error?.code === 'signed-out' ? error.code : 'failed';
}

/** Runs once({renew: true}); after a 401 (the token is renewed by then) once more with no renewal left. */
async function renewing(once) {
    try {
        return await once({ renew: true });
    } catch (error) {
        if (error?.code === 'renewed') {
            return once({ renew: false });
        }
        throw error;
    }
}

function reporter(onProgress) {
    return (index, state, code) => {
        try {
            onProgress(code === undefined ? { index, state } : { index, state, code });
        } catch {
            // The view's trouble ends no run: the list of what was created must come back.
        }
    };
}

/** {@return 200, 404, another status, or null when the GET found no answer} */
async function lookAgain(core, step, options) {
    try {
        try {
            return await core.get(step.kind, step, options);
        } catch (error) {
            if (error?.code === 'renewed') {
                return await core.get(step.kind, step, { renew: false });
            }
            throw error;
        }
    } catch {
        return null;
    }
}

async function createStep(core, step, options) {
    const before = await core.get(step.kind, step, options);
    if (before === 200) {
        return { stop: 'exists-now' };
    }
    if (before !== 404) {
        return { stop: `http-${before}` };
    }
    let status = 0;
    try {
        status = await core.create(step.kind, step, step[BODY[step.kind]], options);
    } catch (error) {
        if (error?.code !== 'network') {
            throw error;
        }
    }
    if (status !== 0) {
        if (status === (step.kind === 'LINK' ? 200 : 201)) {
            return { created: true };
        }
        if (step.kind !== 'THING' && step.kind !== 'LINK' && status === 200) {
            // Someone else made the Item between the GET and the PUT: it is overwritten, and it is not ours to undo.
            return { stop: 'item-overwritten' };
        }
        if (step.kind === 'THING' && status === 409) {
            return { stop: 'exists-now' };
        }
        if (status >= 400 && status < 500) {
            return { stop: `http-${status}` };
        }
    }
    const found = await lookAgain(core, step, options);
    return {
        created: found === 200,
        mark: ANSWER_LOST,
        stop: ANSWER_LOST,
        found: found === 200 ? true : found === 404 ? false : null,
    };
}

/** {@return the groups of openHAB the steps put something into} */
function usedGroups(steps, known) {
    const used = new Set();
    for (const step of steps) {
        for (const name of step.item?.groupNames ?? []) {
            if (known.includes(name)) {
                used.add(name);
            }
        }
    }
    return [...used];
}

async function execute(steps, core, created, report, known) {
    const existing = [];
    // a group of openHAB the plan builds on must still be there: nothing is made into a gap
    for (const itemName of usedGroups(steps, known)) {
        let status;
        try {
            status = await renewing((options) => core.get('GROUP', { itemName }, options));
        } catch (error) {
            return { status: 'stopped', index: -1, code: codeOf(error), existing };
        }
        if (status !== 200) {
            return { status: 'stopped', index: -1, code: status === 404 ? 'group-gone' : `http-${status}`, existing };
        }
    }
    for (const [index, step] of steps.entries()) {
        let status;
        try {
            status = await renewing((options) => core.get(step.kind, step, options));
        } catch (error) {
            report(index, 'failed', codeOf(error));
            return { status: 'stopped', index, code: codeOf(error), existing };
        }
        if (status === 200) {
            existing.push(index);
            report(index, 'exists');
        } else if (status === 404) {
            report(index, 'checked');
        } else {
            report(index, 'failed', `http-${status}`);
            return { status: 'stopped', index, code: `http-${status}`, existing };
        }
    }
    if (existing.length > 0) {
        return { status: 'exists', existing };
    }
    for (const [index, step] of steps.entries()) {
        let outcome;
        try {
            outcome = await renewing((options) => createStep(core, step, options));
        } catch (error) {
            report(index, 'failed', codeOf(error));
            return { status: 'stopped', index, code: codeOf(error), existing };
        }
        if (outcome.created) {
            created.push(entryOf(step, index, outcome.stop === ANSWER_LOST ? ANSWER_LOST : undefined));
        }
        if (outcome.stop !== undefined) {
            report(index, 'failed', outcome.stop);
            const stopped = { status: 'stopped', index, code: outcome.stop, existing };
            if (outcome.stop === ANSWER_LOST) {
                stopped.found = outcome.found;
            }
            return stopped;
        }
        report(index, 'created');
    }
    return { status: 'done', existing };
}

/**
 * Creates what the steps of a preview say. The steps are copied first: what the run checks is what it sends.
 *
 * @param {Object[]} steps the steps of call 22, as the answer gave them
 * @param {Object} core the client of js/core.js
 * @param {(event: {index: number, state: string, code?: string}) => void} [onProgress] state is checked, exists,
 *     created or failed
 * @param {{existingGroups?: Array<string>}} [options] the groups the answer says openHAB has already
 * @returns {Promise<Object>} status done, exists (existing: the indexes of what exists), stopped (index, code, and
 *     found for answer-lost: true, false, or null when not known), refused (code bad-plan, index, reason) or busy;
 *     created: what this run created, the list undo takes
 */
export async function runPlan(steps, core, onProgress = () => {}, { existingGroups = [] } = {}) {
    if (busy) {
        return { status: 'busy', code: 'busy', existing: [], created: undoList([]) };
    }
    let copy = null;
    try {
        copy = JSON.parse(JSON.stringify(steps));
    } catch {
        copy = null;
    }
    const known = Array.isArray(existingGroups) ? [...existingGroups] : null;
    const problem = checkPlan(copy, known);
    if (problem !== null) {
        return { status: 'refused', code: 'bad-plan', ...problem, existing: [], created: undoList([]) };
    }
    busy = true;
    const created = [];
    let outcome;
    try {
        outcome = await execute(copy, core, created, reporter(onProgress), known);
    } catch {
        outcome = { status: 'stopped', index: -1, code: 'failed', existing: [] };
    } finally {
        busy = false;
    }
    return { ...outcome, created: undoList(created) };
}

/**
 * Deletes what one run created: links, Items, Things, groups, each kind backwards. A 404 counts as done, and a Thing's
 * 202 (core removes it once its handler is done); any other failure is reported and the rest is still tried.
 *
 * @param {Object[]} created the created list of a run, or the left list of an undo; each list is taken once
 * @param {(event: {index: number, state: string, code?: string}) => void} [onProgress] index into created; state is
 *     removed or failed
 * @returns {Promise<Object>} status done, partial, refused (a list no run gave, or one taken before) or busy;
 *     removed, failed ({entry, code}), and left: what is still there, for another undo
 */
export async function undo(created, core, onProgress = () => {}) {
    if (busy) {
        return { status: 'busy', removed: [], failed: [], left: created };
    }
    if (!runLists.has(created)) {
        return { status: 'refused', removed: [], failed: [], left: undoList([]) };
    }
    runLists.delete(created);
    busy = true;
    const report = reporter(onProgress);
    const removed = [];
    const failed = [];
    try {
        const order = created
            .map((entry, index) => ({ entry, index }))
            .sort((a, b) => UNDO_RANK[a.entry.kind] - UNDO_RANK[b.entry.kind] || b.index - a.index);
        for (const { entry, index } of order) {
            let code;
            try {
                const status = await renewing((options) => core.remove(entry.kind, entry, options));
                if (status !== 200 && status !== 404 && !(entry.kind === 'THING' && status === 202)) {
                    code = `http-${status}`;
                }
            } catch (error) {
                code = codeOf(error);
            }
            if (code === undefined) {
                removed.push(entry);
                report(index, 'removed');
            } else {
                failed.push({ entry, code });
                report(index, 'failed', code);
            }
        }
    } finally {
        busy = false;
    }
    const left = created.filter((entry) => failed.some((failure) => failure.entry === entry));
    return { status: failed.length > 0 ? 'partial' : 'done', removed, failed, left: undoList(left) };
}
