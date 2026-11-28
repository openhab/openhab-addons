// The wizard's last part: the sheet that asks before anything is made (the counts, the location groups, every address
// that will be read at start, the warnings), then the run of js/plan.js with each step's state in a list grouped by Thing, and at the
// end what was made, the undo of this run, and the way to each new Thing in openHAB's Main UI.

import { add, attr, clear, el, mainUiLink } from '../dom.js';
import { ANSWER_LOST, runPlan, undo } from '../plan.js';
import { hasText, text } from '../texts.js';
import { problemWords } from './wizardplan.js';

const HTTP_CODE = /^http-(\d{3})$/;
/** Under this name the tab's store holds the mark of the visit a run view belongs to. */
const SESSION_KEY = 'wizard.session';
const STATES = ['waiting', 'checked', 'exists', 'created', 'failed', 'removed'];

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

/** {@return the words for a code a run or an undo ended with} */
export function planText(code) {
    if (hasText(`plan.${code}`)) {
        return text(`plan.${code}`);
    }
    const http = HTTP_CODE.exec(String(code));
    return http ? text('error.http', http[1]) : text('error.unknown', code ?? 'unknown');
}

function stepName(step) {
    if (step.kind === 'LINK') {
        return text('wizard.linkName', textOf(step.itemName), textOf(step.channelUID));
    }
    return step.kind === 'THING' ? textOf(step.uid) : textOf(step.itemName);
}

function thingOf(channelUID) {
    const uid = textOf(channelUID);
    return uid.slice(0, Math.max(0, uid.lastIndexOf(':')));
}

/**
 * {@return the steps in groups, in the order they first come: the location groups, then per Thing its own step, its
 * Items and its links; each entry keeps the step's place in the run}
 */
export function stepGroups(steps) {
    const thingOfItem = new Map();
    for (const step of steps) {
        if (step.kind === 'LINK') {
            thingOfItem.set(step.itemName, thingOf(step.channelUID));
        }
    }
    const groups = new Map();
    steps.forEach((step, index) => {
        const key = step.kind === 'GROUP' ? '' : step.kind === 'THING' ? textOf(step.uid) : step.kind === 'LINK' ? thingOf(step.channelUID) : (thingOfItem.get(step.itemName) ?? '');
        if (!groups.has(key)) {
            groups.set(key, { key, label: '', steps: [] });
        }
        const group = groups.get(key);
        if (step.kind === 'THING') {
            group.label = textOf(step.thing?.label);
        }
        group.steps.push({ step, index });
    });
    return [...groups.values()];
}

/**
 * @param {Object} options
 * @param options.context the view's context
 * @param options.core the client of js/core.js
 * @param {Object} options.on state() when a run or an undo starts or ends, cancel() and confirmed(answer) from the sheet
 *     (the answer the sheet was drawn from),
 *     plan() and panel() from the end's buttons
 * @returns {{node: Element, sheet: Function, start: Function, working: Function, left: Function, close: Function}}
 */
export function createRunView({ context, core, on }) {
    const { document, confirm } = context;
    const root = context.base instanceof URL ? new URL('../', context.base) : undefined;
    const keepKey = `wizard.run:${textOf(context.bridge?.uid)}`;
    // the store of the tab is emptied at sign-out: a run that began before it finds another mark there, or none
    let session = context.keep?.get(SESSION_KEY);
    if (context.keep && session === undefined) {
        session = {};
        context.keep.set(SESSION_KEY, session);
    }

    let closed = false;
    let working = false;
    let undoList = null;
    let createdPart = null;
    let createdStates = [];
    let sheetNode = null;
    let sheetAnswer = null;

    const progress = el(document, 'p', { class: 'progress', role: 'status' });
    const runPart = el(document, 'div', { class: 'run' });
    const undoButton = el(document, 'button', { type: 'button', class: 'undo' }, text('wizard.undo'));
    const planButton = el(document, 'button', { type: 'button', class: 'back-plan' }, text('wizard.backToPlan'));
    const panelButton = el(document, 'button', { type: 'button', class: 'back-panel' }, text('wizard.backToPanel'));
    const links = el(document, 'ul', { class: 'open-things' });
    const runHeading = el(document, 'h3', { tabindex: '-1' }, text('wizard.createTitle'));
    const node = el(
        document,
        'section',
        { class: 'part wizard-run' },
        runHeading,
        el(document, 'p', { class: 'note help' }, text('wizard.limits')),
        progress,
        runPart,
        links,
        el(document, 'div', { class: 'tools end' }, planButton, panelButton),
    );
    const createButton = el(document, 'button', { type: 'button', class: 'confirm-create' }, text('wizard.create'));
    const cancelButton = el(document, 'button', { type: 'button', class: 'confirm-cancel' }, text('wizard.cancel'));

    function updateState() {
        attr(undoButton, 'disabled', working);
        attr(planButton, 'disabled', working);
        attr(panelButton, 'disabled', working);
        on.state();
    }

    function stateSpan() {
        return el(document, 'span', { class: 'state waiting' }, text('stepState.waiting'));
    }

    function showState(span, event) {
        clear(span);
        attr(span, 'class', STATES.includes(event.state) ? `state ${event.state}` : 'state');
        add(document, span, named('stepState', event.state), event.code === undefined ? null : `: ${planText(event.code)}`);
    }

    function showLinks() {
        clear(links);
        add(
            document,
            links,
            (undoList ?? []).filter((entry) => entry.kind === 'THING').map((entry) => el(document, 'li', {}, mainUiLink(document, root, 'thing', textOf(entry.uid), text('wizard.openThing', textOf(entry.uid))))),
        );
    }

    function showCreated(list, summary) {
        createdPart?.remove();
        createdStates = [];
        createdPart = el(document, 'div', { class: 'created' });
        add(document, createdPart, summary);
        if (list.length > 0) {
            add(
                document,
                createdPart,
                el(document, 'h4', {}, text('wizard.createdTitle')),
                el(
                    document,
                    'ul',
                    { class: 'steps' },
                    list.map((entry) => {
                        const state = el(document, 'span', { class: 'state' });
                        createdStates.push(state);
                        return el(document, 'li', {}, el(document, 'span', { class: 'kind' }, named('kind', entry.kind)), ' ', stepName(entry), entry.mark === ANSWER_LOST ? el(document, 'span', { class: 'mark' }, text('wizard.answerLost')) : null, ' ', state);
                    }),
                ),
                el(document, 'div', { class: 'tools' }, undoButton),
            );
        }
        add(document, runPart, createdPart);
        showLinks();
    }

    function runSummary(outcome) {
        const lines = [];
        switch (outcome.status) {
            case 'done':
                lines.push(text('wizard.done', outcome.created.length));
                break;
            case 'exists':
                lines.push(text('wizard.exists', outcome.existing.length));
                break;
            case 'stopped':
                lines.push(outcome.index >= 0 ? text('wizard.stoppedAt', outcome.index + 1, planText(outcome.code)) : text('wizard.stopped', planText(outcome.code)));
                if (outcome.code === ANSWER_LOST) {
                    lines.push(text(outcome.found === true ? 'wizard.lostFound' : outcome.found === false ? 'wizard.lostNotFound' : 'wizard.lostUnknown'));
                }
                break;
            case 'refused':
                lines.push(text('wizard.refused', named('planReason', outcome.reason)));
                break;
            default:
                lines.push(planText(outcome.code ?? 'failed'));
        }
        return lines.map((line) => el(document, 'p', { class: outcome.status === 'done' ? 'note' : 'note warn' }, line));
    }

    /**
     * Keeps what can still be undone for the next time the wizard is opened in this tab, also when the run or the
     * undo ended after the wizard was left; nothing left to undo keeps nothing.
     */
    function keepUndo(list) {
        const left = Array.isArray(list) ? list.length : 0;
        // the count outlives a reload of the page, the list does not: the next visit says so
        context.tab?.set(keepKey, left > 0 ? left : null);
        // a sign-out emptied the store behind a run that was still going: nothing of it goes to the next visit
        if (context.keep?.get(SESSION_KEY) !== session) {
            return;
        }
        context.keep?.set(keepKey, left > 0 ? list : null);
    }

    /** Runs the steps of the answer; nothing is asked here, the sheet did. */
    async function start(answer) {
        if (working) {
            return;
        }
        const steps = listOf(answer?.steps);
        working = true;
        undoList = null;
        keepUndo(null);
        goneNode?.remove();
        goneNode = null;
        createdPart = null;
        clear(runPart);
        clear(links);
        updateState();
        const states = steps.map(() => stateSpan());
        const made = new Set();
        const showProgress = () => {
            clear(progress);
            add(document, progress, text('wizard.progress', made.size, steps.length));
        };
        showProgress();
        add(
            document,
            runPart,
            stepGroups(steps).map((group) =>
                el(
                    document,
                    'div',
                    { class: 'step-group' },
                    el(document, 'h4', {}, group.key === '' ? text('wizard.locationGroups') : [group.label, group.label === '' ? null : ' ', el(document, 'span', { class: 'kind' }, group.key)]),
                    el(
                        document,
                        'ul',
                        { class: 'steps' },
                        group.steps.map(({ step, index }) => el(document, 'li', {}, el(document, 'span', { class: 'kind' }, named('kind', step.kind)), ' ', stepName(step), ' ', states[index])),
                    ),
                ),
            ),
        );
        let outcome;
        try {
            outcome = await runPlan(
                steps,
                core,
                (event) => {
                    if (!closed && states[event.index]) {
                        showState(states[event.index], event);
                        if (event.state === 'created') {
                            made.add(event.index);
                            showProgress();
                        }
                    }
                },
                { existingGroups: stringsOf(answer?.existingGroups) },
            );
        } finally {
            working = false;
        }
        keepUndo(outcome.created);
        if (closed) {
            return;
        }
        undoList = outcome.created;
        showCreated(undoList, runSummary(outcome));
        updateState();
    }

    async function undoRun() {
        if (working || undoList === null || undoList.length === 0) {
            return;
        }
        if (!confirm(text('wizard.undoConfirm', undoList.length))) {
            return;
        }
        working = true;
        updateState();
        let outcome;
        try {
            outcome = await undo(undoList, core, (event) => {
                if (!closed && createdStates[event.index]) {
                    showState(createdStates[event.index], event);
                }
            });
        } finally {
            working = false;
        }
        keepUndo(outcome.left);
        if (closed) {
            return;
        }
        undoList = outcome.left;
        const summary =
            outcome.status === 'done'
                ? text('wizard.undone', outcome.removed.length)
                : outcome.status === 'partial'
                ? text('wizard.undoPartial', outcome.failed.length)
                : outcome.status === 'busy'
                ? planText('busy')
                : text('wizard.undoRefused');
        const failures = outcome.failed.map((failure) => el(document, 'p', { class: 'note warn' }, `${named('kind', failure.entry.kind)} ${stepName(failure.entry)}: ${planText(failure.code)}`));
        showCreated(undoList, [el(document, 'p', { class: outcome.status === 'done' ? 'note' : 'note warn' }, summary), failures]);
        updateState();
    }

    /**
     * {@return the sheet that asks before the run: what will be made, every address read at start with its name
     * where the plan has one, the warnings}
     */
    function sheet(answer, names) {
        const counts = typeof answer?.counts === 'object' && answer.counts !== null ? answer.counts : {};
        const reads = stringsOf(answer?.readAddresses);
        const warnings = listOf(answer?.problems).filter((problem) => problem.severity !== 'ERROR');
        const groups = listOf(answer?.steps).filter((step) => step.kind === 'GROUP');
        const there = stringsOf(answer?.existingGroups);
        sheetAnswer = answer;
        sheetNode = el(
            document,
            'div',
            { class: 'confirm-sheet', role: 'dialog', 'aria-modal': 'true', 'aria-label': text('wizard.createTitle'), tabindex: '-1' },
            el(
                document,
                'div',
                { class: 'sheet' },
                el(document, 'h3', {}, text('wizard.createTitle')),
                el(document, 'p', { class: 'counts' }, text('wizard.summary.will', count(counts.things), count(counts.channels), count(counts.items), count(counts.groups), count(counts.links))),
                el(document, 'p', { class: 'question' }, text('wizard.confirm', count(counts.things), count(counts.items), count(counts.links), count(counts.groups), count(answer?.readTelegrams))),
                groups.length + there.length === 0
                    ? null
                    : [
                          el(document, 'h4', {}, text('wizard.confirmGroups', groups.length)),
                          el(
                              document,
                              'ul',
                              { class: 'group-list' },
                              groups.map((step) => {
                                  const above = stringsOf(step.item?.groupNames)[0];
                                  const tag = stringsOf(step.item?.tags)[0];
                                  return el(document, 'li', {}, el(document, 'span', { class: 'name' }, textOf(step.itemName)), ' ', textOf(step.item?.label), tag === undefined ? null : [' ', el(document, 'span', { class: 'kind' }, tag)], above === undefined ? null : [' ', el(document, 'span', { class: 'in' }, text('wizard.locationIn', above))]);
                              }),
                              there.map((name) => el(document, 'li', { class: 'there' }, el(document, 'span', { class: 'name' }, name), ' ', text('wizard.groupThere'))),
                          ),
                      ],
                el(document, 'h4', {}, text('wizard.confirmReads', reads.length)),
                reads.length === 0
                    ? el(document, 'p', { class: 'note' }, text('wizard.readsEmpty'))
                    : el(
                          document,
                          'ul',
                          { class: 'read-list' },
                          reads.map((address) => el(document, 'li', {}, el(document, 'span', { class: 'address' }, address), textOf(names?.get(address)) === '' ? null : [' ', el(document, 'span', { class: 'name' }, names.get(address))])),
                      ),
                warnings.length === 0
                    ? null
                    : [
                          el(document, 'h4', {}, text('wizard.confirmWarnings', warnings.length)),
                          el(
                              document,
                              'ul',
                              { class: 'problems' },
                              warnings.map((problem) => {
                                  const words = problemWords(problem);
                                  return el(document, 'li', { class: 'warning' }, words, textOf(problem.text) === '' || words === problem.text ? null : el(document, 'span', { class: 'detail' }, problem.text));
                              }),
                          ),
                      ],
                el(document, 'p', { class: 'note limits' }, text('wizard.limits')),
                el(document, 'div', { class: 'tools' }, createButton, cancelButton),
            ),
        );
        sheetNode.addEventListener('click', onSheetClick);
        sheetNode.addEventListener('keydown', onSheetKey);
        return sheetNode;
    }

    function closeSheet() {
        if (sheetNode) {
            sheetNode.removeEventListener('click', onSheetClick);
            sheetNode.removeEventListener('keydown', onSheetKey);
            sheetNode.remove();
            sheetNode = null;
            sheetAnswer = null;
        }
    }

    function onSheetClick(event) {
        if (event.target === createButton) {
            const shown = sheetAnswer;
            closeSheet();
            on.confirmed(shown);
        } else if (event.target === cancelButton) {
            closeSheet();
            on.cancel();
        }
    }

    function onSheetKey(event) {
        if (event.key === 'Escape') {
            event.preventDefault();
            closeSheet();
            on.cancel();
        } else if (event.key === 'Tab' && (event.shiftKey ? event.target !== cancelButton : event.target === cancelButton)) {
            // Tab stays in the sheet, also from the sheet itself, where a click on its text leaves the focus: the
            // plan behind it is not reached while it is open
            event.preventDefault();
            (event.shiftKey ? cancelButton : createButton).focus();
        }
    }

    const onClick = (event) => {
        if (event.target === undoButton) {
            undoRun();
        } else if (event.target === planButton && !working) {
            on.plan();
        } else if (event.target === panelButton && !working) {
            on.panel();
        }
    };
    node.addEventListener('click', onClick);

    const keptUndo = context.keep?.get(keepKey);
    const goneButton = el(document, 'button', { type: 'button', class: 'run-gone-ok' }, text('wizard.runGoneOk'));
    let goneNode = null;
    if (Array.isArray(keptUndo) && keptUndo.length > 0) {
        undoList = keptUndo;
        showCreated(undoList, el(document, 'p', { class: 'note' }, text('wizard.runKept', undoList.length)));
    } else {
        // the page was loaded again, or the viewer signed out, since a run that could still be undone
        const lost = context.tab?.get(keepKey);
        if (Number.isInteger(lost) && lost > 0) {
            goneNode = el(document, 'p', { class: 'note warn run-gone', role: 'status' }, text('wizard.runGone', lost), ' ', goneButton);
        }
    }
    const onGone = () => {
        context.tab?.set(keepKey, null);
        goneNode?.remove();
        goneNode = null;
    };
    goneButton.addEventListener('click', onGone);

    return {
        node,
        sheet,
        /** Puts the keyboard focus into the open sheet, on the button that makes nothing. */
        focusSheet: () => cancelButton.focus(),
        /** Puts the keyboard focus on the run's heading, as the sheet's button it was on is gone. */
        focusRun: () => runHeading.focus(),
        /** {@return how many objects of the last run can still be undone} */
        left: () => undoList?.length ?? 0,
        /** {@return the line that says the last run's undo list is gone, or null} */
        gone: () => goneNode,
        start,
        working: () => working,
        close() {
            closed = true;
            closeSheet();
            node.removeEventListener('click', onClick);
            goneButton.removeEventListener('click', onGone);
        },
    };
}
