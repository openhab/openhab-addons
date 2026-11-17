// The wizard, "Add to openHAB": tick rooms, functions, devices or group addresses of the ETS project (calls 15, 10
// and 9), get a proposal of Things, channels and Items (call 21) and work on it as a plan (views/wizardplan.js): every
// change is checked again by call 22 after a short wait, and the page creates the plan in openHAB with js/plan.js
// after a sheet that asks (views/wizardrun.js). The view shows what the calls answer and sends what is on the page;
// pairing, names, reads, counts and what is valid are the server's. The plan under work and what the last run made
// are kept while the viewer is in another panel.

import { createCore } from '../core.js';
import { add, attr, clear, el, place } from '../dom.js';
import { deviceOrder } from '../model.js';
import { createPlan } from '../proposal.js';
import { errorText, text } from '../texts.js';
import { projectMissing } from './explorer.js';
import { createPlanView } from './wizardplan.js';
import { createRunView } from './wizardrun.js';

export { planText } from './wizardrun.js';

/** The group addresses drawn at most in the list of the first screen; the filter narrows it. */
export const ADDRESSES_SHOWN = 500;
/** How long the page waits after a change before it asks call 22, so quick changes make one call; in ms. */
export const CHECK_WAIT = 400;

const PRESELECTIONS = ['space', 'function', 'device'];

function textOf(value) {
    return typeof value === 'string' ? value : '';
}

function listOf(value) {
    return Array.isArray(value) ? value.filter((item) => typeof item === 'object' && item !== null) : [];
}

/** @returns {{close: () => void}} */
export function show(context) {
    const { document, element, api, bridge, route, showError } = context;
    const core = createCore({ fetch: context.fetch, base: context.base, auth: context.auth });
    const timers = context.timers ?? { setTimeout: (callback) => (callback(), 0), clearTimeout() {} };
    let query = route?.query ?? {};
    let chosen = JSON.stringify(PRESELECTIONS.map((name) => textOf(query[name])));
    const keepKey = `wizard:${textOf(bridge?.uid)}`;
    const kept = context.keep?.get(keepKey);
    let restored = kept && kept.chosen === chosen ? kept : null;
    // opened for something else while a plan with changes is kept: the viewer says which of the two goes on
    if (kept && restored === null && kept.plan?.changed() === true && context.confirm(text('wizard.keepPlan'))) {
        restored = kept;
        chosen = kept.chosen;
        // the page's address names what the kept plan was opened for, so the way back goes there
        query = kept.query ?? {};
        context.replace?.({ view: route.view, bridge: route.bridge, query });
    }

    let closed = false;
    let screen = 'choose';

    // The first screen: what is ticked, by kind; the set of each tick box.
    const ticked = { spaces: new Set(), functions: new Set(), devices: new Set(), groupAddresses: new Set() };
    if (restored) {
        for (const [name, set] of Object.entries(ticked)) {
            for (const id of restored.ticked[name]) {
                set.add(id);
            }
        }
    } else {
        for (const [name, set] of [
            ['space', ticked.spaces],
            ['function', ticked.functions],
            ['device', ticked.devices],
        ]) {
            if (textOf(query[name]) !== '') {
                set.add(query[name]);
            }
        }
    }
    const boxes = new Map();
    let addressBoxes = [];
    let addresses = [];
    let proposeTurn = 0;

    // The plan, and what call 22 last said about it.
    let plan = restored?.plan ?? null;
    let planView = null;
    let check = { answer: null, pending: false, error: null };
    let checkTimer = null;
    let checkTurn = 0;

    const heading = el(document, 'h2', {}, text('view.wizard'));
    const tickedLine = el(document, 'p', { class: 'ticked', 'aria-live': 'polite' });
    const proposeButton = el(document, 'button', { type: 'button', class: 'propose' }, text('wizard.propose'));
    const toPlanButton = el(document, 'button', { type: 'button', class: 'to-plan' }, text('wizard.backToPlan'));
    const lastRunButton = el(document, 'button', { type: 'button', class: 'last-run' });
    const proposeState = el(document, 'p', { class: 'note propose-state', role: 'status' });
    const filterField = el(document, 'input', {
        type: 'search',
        name: 'filter',
        placeholder: text('wizard.filter'),
        'aria-label': text('wizard.filter'),
        autocomplete: 'off',
    });
    const roomsPart = el(document, 'div', { class: 'pick rooms' });
    const devicesPart = el(document, 'div', { class: 'pick devices' });
    const addressList = el(document, 'ul', { class: 'pick addresses' });
    const addressMore = el(document, 'p', { class: 'note more' });
    const chooseBar = el(document, 'div', { class: 'tools choose-bar' }, tickedLine, toPlanButton, proposeButton);
    const choose = el(
        document,
        'section',
        { class: 'part wizard-choose' },
        el(document, 'h3', {}, text('wizard.choose')),
        el(document, 'div', { class: 'choose-lists' }, el(document, 'div', { class: 'choose-list' }, el(document, 'h4', {}, text('wizard.rooms')), roomsPart), el(document, 'div', { class: 'choose-list' }, el(document, 'h4', {}, text('wizard.devices')), devicesPart), el(document, 'div', { class: 'choose-list' }, el(document, 'h4', {}, text('wizard.addresses')), el(document, 'div', { class: 'tools' }, filterField), addressList, addressMore)),
        proposeState,
        chooseBar,
    );

    const run = createRunView({
        context,
        core,
        on: {
            state: () => {
                updateState();
                planView?.redraw();
            },
            cancel: () => {
                showScreen('plan');
                planView?.redraw();
                planView?.focusCreate();
            },
            confirmed: (shown) => {
                // only the answer the sheet showed is run: a change behind the sheet has another one, or none yet
                if (!canCreate() || check.answer.answer !== shown) {
                    showScreen('plan');
                    planView?.redraw();
                    planView?.say(text('wizard.sheetStale'));
                    planView?.focusCreate();
                    return;
                }
                check.answer.used = true;
                showScreen('run');
                run.focusRun();
                run.start(shown);
            },
            plan: () => {
                showScreen('plan');
                planView?.redraw();
                updateState();
            },
            panel: () => context.navigate(origin()),
        },
    });

    /** {@return the panel the wizard was opened from, by what was chosen there} */
    function origin() {
        if (textOf(query.device) !== '') {
            return { view: 'topology', bridge: bridge.uid, query: { sel: `device:${query.device}` } };
        }
        return { view: textOf(query.space) !== '' || textOf(query.function) !== '' ? 'buildings' : 'group-addresses', bridge: bridge.uid, query: {} };
    }

    function showScreen(name, sheet) {
        screen = name === 'plan' && planView === null ? 'choose' : name;
        element.classList.toggle('work', screen === 'plan');
        element.classList.toggle('sections', screen !== 'plan');
        place(element, [heading, run.gone(), screen === 'plan' ? planView.node : screen === 'run' ? run.node : choose, sheet].filter(Boolean));
    }

    function anyTicked() {
        return Object.values(ticked).some((set) => set.size > 0);
    }

    function fresh() {
        return check.answer !== null && !check.pending && check.answer.version === plan.version() && !check.answer.used;
    }

    function canCreate() {
        if (run.working() || plan === null || !fresh()) {
            return false;
        }
        const answer = check.answer.answer;
        return !listOf(answer.problems).some((problem) => problem.severity === 'ERROR') && listOf(answer.steps).length > 0;
    }

    function updateState() {
        clear(tickedLine);
        add(document, tickedLine, text('wizard.ticked', ticked.spaces.size, ticked.functions.size, ticked.devices.size, ticked.groupAddresses.size));
        attr(proposeButton, 'disabled', run.working() || !anyTicked());
        clear(lastRunButton);
        add(document, lastRunButton, text('wizard.lastRun', run.left()));
        attr(lastRunButton, 'disabled', run.working());
        place(chooseBar, [tickedLine, run.left() > 0 ? lastRunButton : null, plan === null ? null : toPlanButton, proposeButton].filter(Boolean));
    }

    // The first screen — choose.

    function tick(set, id, kind) {
        const box = el(document, 'input', { type: 'checkbox', name: 'pick', class: `pick-${kind}`, checked: set.has(id) });
        boxes.set(box, { set, id });
        return box;
    }

    function spaceItem(space) {
        const functions = listOf(space.functions);
        const children = listOf(space.spaces);
        const kind = [textOf(space.type), textOf(space.number)].filter((part) => part !== '').join(' ');
        return el(
            document,
            'li',
            {},
            el(
                document,
                'label',
                {},
                tick(ticked.spaces, textOf(space.id), 'space'),
                ' ',
                el(document, 'span', { class: 'name' }, textOf(space.name)),
                kind === '' ? null : el(document, 'span', { class: 'kind' }, kind),
            ),
            functions.length === 0
                ? null
                : el(
                      document,
                      'ul',
                      { class: 'functions' },
                      functions.map((fn) =>
                          el(
                              document,
                              'li',
                              {},
                              el(
                                  document,
                                  'label',
                                  {},
                                  tick(ticked.functions, textOf(fn.id), 'function'),
                                  ' ',
                                  el(document, 'span', { class: 'name' }, textOf(fn.name)),
                                  textOf(fn.type) === '' ? null : el(document, 'span', { class: 'kind' }, fn.type),
                              ),
                          ),
                      ),
                  ),
            children.length === 0 ? null : el(document, 'ul', { class: 'spaces' }, children.map(spaceItem)),
        );
    }

    function drawRooms(spaces) {
        const top = listOf(spaces);
        add(document, roomsPart, top.length === 0 ? el(document, 'p', { class: 'note' }, text('building.noSpaces')) : el(document, 'ul', { class: 'spaces' }, top.map(spaceItem)));
    }

    function drawDevices(devices) {
        const sorted = listOf(devices).sort(deviceOrder);
        for (const device of sorted) {
            // a preselection by individual address ticks the device by its id
            if (textOf(device.address) !== '' && ticked.devices.has(device.address) && textOf(device.id) !== '') {
                ticked.devices.delete(device.address);
                ticked.devices.add(device.id);
            }
        }
        add(
            document,
            devicesPart,
            sorted.some((device) => device.filterDevice === true) ? el(document, 'p', { class: 'note filter-note' }, text('device.filterNote')) : null,
            sorted.length === 0
                ? el(document, 'p', { class: 'note' }, text('devices.empty'))
                : el(
                      document,
                      'ul',
                      { class: 'devices' },
                      sorted.map((device) =>
                          el(
                              document,
                              'li',
                              {},
                              el(
                                  document,
                                  'label',
                                  {},
                                  tick(ticked.devices, textOf(device.id), 'device'),
                                  ' ',
                                  textOf(device.address) === '' ? null : el(document, 'span', { class: 'address' }, device.address),
                                  el(document, 'span', { class: 'name' }, textOf(device.name)),
                                  device.filterDevice === true ? el(document, 'span', { class: 'filter-device' }, text('device.filter')) : null,
                              ),
                          ),
                      ),
                  ),
        );
    }

    function drawAddresses() {
        for (const box of addressBoxes) {
            boxes.delete(box);
        }
        addressBoxes = [];
        clear(addressList);
        clear(addressMore);
        const filter = filterField.value.trim().toLowerCase();
        const matches = addresses.filter(
            (item) => filter === '' || textOf(item.address).toLowerCase().includes(filter) || textOf(item.name).toLowerCase().includes(filter),
        );
        for (const item of matches.slice(0, ADDRESSES_SHOWN)) {
            const box = tick(ticked.groupAddresses, textOf(item.address), 'address');
            addressBoxes.push(box);
            add(
                document,
                addressList,
                el(
                    document,
                    'li',
                    {},
                    el(
                        document,
                        'label',
                        {},
                        box,
                        ' ',
                        el(document, 'span', { class: 'address' }, textOf(item.address)),
                        el(document, 'span', { class: 'name' }, textOf(item.name)),
                        textOf(item.dataType) === '' ? null : el(document, 'span', { class: 'kind' }, item.dataType),
                    ),
                ),
            );
        }
        if (matches.length > ADDRESSES_SHOWN) {
            add(document, addressMore, text('wizard.moreAddresses', matches.length - ADDRESSES_SHOWN));
        } else if (addresses.length > 0 && matches.length === 0) {
            add(document, addressMore, text('addresses.noMatch'));
        }
    }

    function failed(part, reason) {
        add(document, part, el(document, 'p', { class: 'note' }, errorText(reason?.code, reason?.text)));
    }

    async function load() {
        const [locations, devices, groupAddresses] = await Promise.allSettled([
            Promise.resolve().then(() => api.locations(bridge.uid)),
            Promise.resolve().then(() => api.allDevices(bridge.uid)),
            Promise.resolve().then(() => api.allGroupAddresses(bridge.uid)),
        ]);
        if (closed) {
            return;
        }
        for (const outcome of [locations, devices, groupAddresses]) {
            const missing = outcome.status === 'rejected' ? projectMissing(document, bridge, outcome.reason?.code) : null;
            if (missing) {
                clear(choose);
                add(document, choose, el(document, 'h3', {}, text('wizard.choose')), missing);
                return;
            }
        }
        if (locations.status === 'fulfilled') {
            drawRooms(locations.value);
        } else {
            failed(roomsPart, locations.reason);
        }
        if (devices.status === 'fulfilled') {
            drawDevices(devices.value);
        } else {
            failed(devicesPart, devices.reason);
        }
        if (groupAddresses.status === 'fulfilled') {
            addresses = listOf(groupAddresses.value);
            drawAddresses();
        } else {
            failed(addressList, groupAddresses.reason);
        }
        updateState();
    }


    async function propose() {
        if (run.working() || !anyTicked()) {
            return;
        }
        if (plan !== null && plan.changed() && !context.confirm(text('wizard.dropChanges'))) {
            return;
        }
        const turn = ++proposeTurn;
        const request = {
            groupAddresses: [...ticked.groupAddresses],
            functions: [...ticked.functions],
            spaces: [...ticked.spaces],
            devices: [...ticked.devices],
            readAtStart: plan === null ? true : plan.state().readAtStart,
        };
        clear(proposeState);
        add(document, proposeState, text('app.loading'));
        let proposal;
        try {
            proposal = await api.proposal(bridge.uid, request);
        } catch (error) {
            if (!closed && turn === proposeTurn) {
                clear(proposeState);
                add(document, proposeState, errorText(error?.code, error?.text));
            }
            return;
        }
        if (closed || turn !== proposeTurn) {
            return;
        }
        clear(proposeState);
        openPlan(createPlan(proposal));
    }

    // The plan and its checking.

    function openPlan(next, view) {
        planView?.close();
        plan = next;
        check = { answer: null, pending: false, error: null };
        planView = createPlanView({
            context,
            plan,
            check: () => check,
            working: () => run.working(),
            lastRun: () => run.left(),
            canCreate,
            view,
            on: {
                changed: () => scheduleCheck(CHECK_WAIT),
                choose: () => {
                    showScreen('choose');
                    updateState();
                },
                recheck: () => scheduleCheck(0),
                lastRun: () => {
                    if (run.left() > 0) {
                        showScreen('run');
                    }
                },
                create: () => {
                    if (canCreate() && (run.left() === 0 || context.confirm(text('wizard.runLeft', run.left())))) {
                        showScreen('plan', run.sheet(check.answer.answer, planView.readNames()));
                        run.focusSheet();
                    }
                },
            },
        });
        showScreen('plan');
        updateState();
        scheduleCheck(0);
    }

    /** Asks call 22 about the plan as it stands, now or after the wait; an answer still under way no longer counts. */
    function scheduleCheck(wait) {
        if (plan === null || run.working()) {
            return;
        }
        if (checkTimer !== null) {
            timers.clearTimeout(checkTimer);
            checkTimer = null;
        }
        checkTurn++;
        check.pending = true;
        check.error = null;
        planView.redraw();
        if (wait > 0) {
            checkTimer = timers.setTimeout(askCheck, wait);
        } else {
            askCheck();
        }
    }

    async function askCheck() {
        checkTimer = null;
        const turn = checkTurn;
        const version = plan.version();
        const { request, made } = plan.request();
        let answer;
        let failure = null;
        try {
            answer = await api.preview(bridge.uid, request);
        } catch (error) {
            failure = error ?? {};
        }
        if (closed || turn !== checkTurn) {
            return;
        }
        check = failure === null ? { answer: { answer: typeof answer === 'object' && answer !== null ? answer : {}, version, made, used: false }, pending: false, error: null } : { answer: null, pending: false, error: failure };
        planView.redraw();
    }

    const onClick = (event) => {
        const target = event.target;
        if (target === proposeButton) {
            propose();
        } else if (target === toPlanButton && plan !== null) {
            showScreen('plan');
            planView.redraw();
        } else if (target === lastRunButton && run.left() > 0) {
            showScreen('run');
            run.focusRun();
        }
    };
    const onEdit = (event) => {
        const target = event.target;
        if (target === filterField) {
            drawAddresses();
        } else if (boxes.has(target)) {
            const { set, id } = boxes.get(target);
            if (target.checked) {
                set.add(id);
            } else {
                set.delete(id);
            }
            updateState();
        }
    };
    choose.addEventListener('click', onClick);
    choose.addEventListener('input', onEdit);
    choose.addEventListener('change', onEdit);

    showScreen('choose');
    updateState();
    if (restored?.plan) {
        openPlan(restored.plan, restored.view);
        if (restored.screen !== 'plan') {
            showScreen('choose');
        }
    }
    load().catch((error) => {
        if (!closed) {
            showError(error);
        }
    });

    return {
        close() {
            closed = true;
            if (checkTimer !== null) {
                timers.clearTimeout(checkTimer);
            }
            context.keep?.set(keepKey, {
                chosen,
                query: Object.fromEntries(PRESELECTIONS.filter((name) => textOf(query[name]) !== '').map((name) => [name, query[name]])),
                ticked: Object.fromEntries(Object.entries(ticked).map(([name, set]) => [name, [...set]])),
                plan,
                screen: screen === 'choose' ? 'choose' : 'plan',
                view: planView?.view(),
            });
            choose.removeEventListener('click', onClick);
            choose.removeEventListener('input', onEdit);
            choose.removeEventListener('change', onEdit);
            planView?.close();
            run.close();
        },
    };
}
