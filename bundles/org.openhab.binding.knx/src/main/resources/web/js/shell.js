// The frame around the panels: the top bar with the panel tabs, the bridge and the login, the error line under it,
// the status bar at the bottom, the signed-out page, the bridge chooser, and the switch from route to panel. A panel
// that is already shown is told of a new selection instead of being made again. A second panel can stand under the
// first: both are in the address, each is a view of its own, and they share one sidebar, which shows the half that
// was used last. Everything of the browser comes in as an argument.

import { add, attr, clear, el, place, theme } from './dom.js';
import { CHOOSER, NAV_VIEWS, SPLIT_VIEWS, chooser, defaultRoute, firstOf, href, parse, resolve, secondOf, viewsOf, withSecond } from './router.js';
import { createSplitter } from './splitter.js';
import { stateText } from './stream.js';
import { errorText, hasText, text } from './texts.js';
import * as buildings from './views/buildings.js';
import * as groupAddresses from './views/groupaddresses.js';
import * as monitor from './views/monitor.js';
import * as project from './views/project.js';
import * as topology from './views/topology.js';
import * as wizard from './views/wizard.js';
import { createSide } from './workplace.js';

/** The module of each route; each exports show(context) → { close(), update?(route) }. */
export const VIEW_MODULES = Object.freeze({ 'group-addresses': groupAddresses, topology, buildings, monitor, project, wizard });

/** What the theme switch offers, in its order; "auto" follows the system. */
export const THEME_CHOICES = Object.freeze(['auto', 'light', 'dark']);

/** What a view's context holds; views use no global but these. */
export const CONTEXT_KEYS = Object.freeze([
    'document',
    'element',
    'api',
    'stream',
    'rows',
    'bridge',
    'route',
    'navigate',
    'replace',
    'confirm',
    'frame',
    'timers',
    'clipboard',
    'auth',
    'fetch',
    'base',
    'showError',
    'status',
    'layout',
    'tab',
    'history',
    'jump',
    'split',
    'keep',
]);

/** The height of the second panel, in px. */
export const SECOND_HEIGHT = { initial: 320, min: 120, max: 1600 };

/**
 * @param {Object} options
 * @param {Document} options.document
 * @param {Element} options.header
 * @param {Element} options.alert the error line under the header
 * @param {Element} options.main
 * @param {Element} [options.status] the status bar at the bottom
 * @param {{get: Function, set: Function}} [options.layout] what the viewer set, for the panes' widths
 * @param {Element} [options.root] the document's root element, which carries the chosen theme
 * @param {{back: Function, forward: Function}} [options.history] the browser's history, for the panels' bar
 * @param {() => boolean} [options.wide] whether the page is wide enough for two panels; without it, it is
 * @param options.api the client of js/api.js
 * @param options.auth the login of js/auth.js
 * @param {{hash: Function, setHash: Function, replaceHash: Function, assign: Function}} options.location
 * @param options.fetch handed to the views, as are timers, frame, confirm and clipboard
 * @param {URL} [options.base] the REST root, for the wizard's calls of openHAB's own addresses
 * @param {Object} [options.views] the view module of each route, VIEW_MODULES unless a test gives others
 * @param {(bridge: Object) => Object} [options.createRows] the row model of a bridge (stage G2)
 * @param {(bridge: Object, rows: Object) => Object} [options.createStream] the telegram stream of a present bridge
 */
export function createShell(options) {
    const { document, header, alert, main, api, auth, location } = options;
    const views = options.views ?? VIEW_MODULES;
    const createRows = options.createRows ?? (() => null);
    const createStream = options.createStream ?? (() => null);
    const wide = options.wide ?? (() => true);

    let bridges = [];
    let bridgesLoaded = false;
    let current = null;
    let services = null;
    let leaveStream = null;
    let signedOutShown = false;
    let shown = 0;
    // what a view keeps for its next visit in this tab of the browser: the wizard's plan under work and what its
    // last run made
    const keep = new Map();

    const brand = el(document, 'a', { class: 'brand', href: href(chooser()) }, text('app.name'));
    const bridgeLine = el(document, 'span', { class: 'bridge' });
    const nav = el(document, 'nav', { 'aria-label': text('app.views') });
    const streamLine = el(document, 'span', { class: 'stream', 'aria-live': 'polite' });
    const statusBar = options.status ?? null;
    const statusSelection = el(document, 'span', { class: 'selection' });
    const statusCount = el(document, 'span', { class: 'count' });
    const statusBridge = el(document, 'span', { class: 'bridge-state' });
    let statusBridgeWords = '';
    const userLine = el(document, 'span', { class: 'user' });
    const signOutButton = el(document, 'button', { type: 'button', class: 'sign-out' }, text('login.signOut'));
    const themeButtons = THEME_CHOICES.map((name) => el(document, 'button', { type: 'button', class: `theme-${name}`, 'aria-pressed': 'false' }, text(`theme.${name}`)));
    const themeBox = el(document, 'span', { class: 'theme', role: 'group', 'aria-label': text('theme.label') }, themeButtons);
    add(document, header, brand, themeBox);

    function chosenTheme() {
        const kept = options.layout?.get('theme');
        return THEME_CHOICES.includes(kept) ? kept : 'auto';
    }

    function showTheme(name) {
        if (options.root) {
            theme(options.root, name);
        }
        themeButtons.forEach((button, at) => attr(button, 'aria-pressed', THEME_CHOICES[at] === name ? 'true' : 'false'));
    }

    showTheme(chosenTheme());
    themeBox.addEventListener('click', (event) => {
        const at = themeButtons.indexOf(event.target);
        if (at >= 0) {
            options.layout?.set('theme', THEME_CHOICES[at]);
            showTheme(THEME_CHOICES[at]);
        }
    });

    signOutButton.addEventListener('click', () => {
        signOut();
    });
    main.addEventListener('click', (event) => {
        const target = event.target;
        if (!signedOutShown || !target?.classList) {
            return;
        }
        if (target.classList.contains('sign-in')) {
            signIn();
        } else if (target.classList.contains('try-again')) {
            begin();
        }
    });

    function showError(error) {
        if (error?.code === 'signed-out') {
            return;
        }
        clear(alert);
        if (error) {
            add(document, alert, errorText(error.code, error.text));
        }
    }

    /** What a panel says about itself: what is selected and how many rows its list shows of how many. */
    function setStatus({ selection, shown: rowsShown, total } = {}) {
        clear(statusSelection);
        clear(statusCount);
        if (typeof selection === 'string' && selection !== '') {
            add(document, statusSelection, selection);
        }
        if (Number.isFinite(rowsShown) && Number.isFinite(total)) {
            add(document, statusCount, text('frame.count', rowsShown, total));
        }
    }

    function drawStatusBridge() {
        clear(statusBridge);
        const stream = services?.stream;
        const words = typeof stream?.state === 'function' ? stateText(stream.state()) : '';
        add(document, statusBridge, [statusBridgeWords, words].filter((part) => part !== '').join(' · '));
    }

    function renderStatus(bridge) {
        if (statusBar === null) {
            return;
        }
        clear(statusBar);
        statusBridgeWords = bridge ? [bridge.label ?? bridge.uid, bridge.status].filter((part) => typeof part === 'string' && part !== '').join(' ') : '';
        if (bridge && auth.signedIn()) {
            drawStatusBridge();
            add(document, statusBar, statusSelection, statusCount, statusBridge);
        }
    }

    function closeHalf(half) {
        if (typeof half.view?.close === 'function') {
            try {
                half.view.close();
            } catch (error) {
                showError(error);
            }
        }
        half.view = null;
    }

    function closeView() {
        const was = current;
        current = null;
        setStatus();
        for (const half of was?.halves ?? []) {
            closeHalf(half);
        }
        was?.close?.();
        clear(main);
    }

    function showStreamState() {
        clear(streamLine);
        const stream = services?.stream;
        const words = typeof stream?.state === 'function' ? stateText(stream.state()) : '';
        if (words !== '') {
            add(document, streamLine, words);
        }
        drawStatusBridge();
    }

    function closeServices() {
        const old = services;
        services = null;
        leaveStream?.();
        leaveStream = null;
        clear(streamLine);
        for (const service of [old?.stream, old?.rows]) {
            if (service && typeof service.close === 'function') {
                service.close();
            }
        }
    }

    /** {@return whether the kept services of the bridge give it no live stream: none, or one stopped for good} */
    function deadServices(uid) {
        if (services === null || services.uid !== uid) {
            return false;
        }
        if (!services.stream) {
            return true;
        }
        const state = typeof services.stream.state === 'function' ? services.stream.state() : null;
        return state?.phase === 'stopped' && state.code !== 'signed-out';
    }

    function presentIn(list, uid) {
        return list.some((bridge) => bridge.uid === uid && bridge.present === true);
    }

    function servicesFor(bridge) {
        if (services?.uid !== bridge.uid) {
            closeServices();
            const rows = createRows(bridge) ?? null;
            const stream = bridge.present ? (createStream(bridge, rows) ?? null) : null;
            services = { uid: bridge.uid, rows, stream };
            if (typeof stream?.subscribe === 'function') {
                leaveStream = stream.subscribe(showStreamState);
            }
            showStreamState();
        }
        return services;
    }

    function connectionText(kind) {
        return hasText(`connection.${kind}`) ? text(`connection.${kind}`) : String(kind);
    }

    function bridgeParts(bridge) {
        return [
            el(document, 'span', { class: 'label' }, bridge.label ?? bridge.uid),
            bridge.status ? el(document, 'span', { class: 'status' }, bridge.status) : null,
            bridge.connection ? el(document, 'span', { class: 'kind' }, connectionText(bridge.connection)) : null,
            bridge.present ? null : el(document, 'span', { class: 'removed' }, text('bridges.removed')),
        ];
    }

    function renderHeader(route, bridge) {
        for (const part of [bridgeLine, nav, streamLine, userLine, signOutButton]) {
            part.remove();
        }
        clear(bridgeLine);
        clear(nav);
        clear(userLine);
        if (!auth.signedIn()) {
            return;
        }
        themeBox.remove();
        if (bridge) {
            add(document, bridgeLine, bridgeParts(bridge));
            if (bridges.length > 1) {
                add(document, bridgeLine, el(document, 'a', { class: 'all', href: href(chooser()) }, text('bridges.all')));
            }
            const offered = viewsOf(bridge);
            for (const view of NAV_VIEWS.filter((name) => offered.includes(name))) {
                const link = withSecond({ view, bridge: bridge.uid, query: {} }, secondOf(route));
                add(document, nav, el(document, 'a', { href: href(link), class: view === route.view ? 'current' : null, 'aria-current': view === route.view ? 'page' : null }, text(`view.${view}`)));
            }
            add(document, header, nav, bridgeLine, bridge.present ? streamLine : null);
        }
        add(document, userLine, text('login.user', auth.user()?.name ?? ''));
        add(document, header, userLine, signOutButton, themeBox);
    }

    function renderChooser(element) {
        add(document, element, el(document, 'h2', {}, text('bridges.title')));
        if (!bridgesLoaded) {
            return;
        }
        if (bridges.length === 0) {
            add(document, element, el(document, 'p', { class: 'note' }, text('bridges.none')));
            return;
        }
        const list = el(document, 'ul', { class: 'bridges' });
        for (const bridge of bridges) {
            const [label, ...rest] = bridgeParts(bridge);
            add(document, list, el(document, 'li', {}, el(document, 'a', { href: href(defaultRoute(bridge)) }, label), rest));
        }
        add(document, element, list);
    }

    /** The half is the one used last: the shared sidebar and the status bar show it. */
    function activate(half) {
        const state = current;
        if (!state || !state.halves.includes(half) || state.active === half) {
            return;
        }
        state.active = half;
        for (const other of state.halves) {
            other.element.classList.toggle('active', state.side !== null && other === half);
        }
        if (state.side && half.face) {
            state.side.sidebar.show(half.face);
        }
        setStatus(half.status);
    }

    /** {@return the address's route when this half shows the route} */
    function wholeWith(half, route) {
        return half.name === 'second' ? withSecond(current.route, route) : withSecond(route, secondOf(current.route));
    }

    function navigateFrom(half, route) {
        if (!current?.halves.includes(half) || route.bridge !== current.route.bridge || !SPLIT_VIEWS.includes(route.view)) {
            navigate(route);
            return;
        }
        navigate(wholeWith(half, route));
    }

    /**
     * Gives a panel that is shown another address without a new entry in the history: what is selected in its list.
     * The panel is not told; it asked for it.
     */
    function replaceFrom(half, route) {
        if (!current?.halves.includes(half) || half.route.view !== route.view || half.route.bridge !== route.bridge) {
            return;
        }
        const whole = wholeWith(half, route);
        const wanted = href(whole);
        if (wanted !== location.hash()) {
            location.replaceHash(wanted);
        }
        half.route = route;
        current.route = whole;
    }

    /**
     * Shows the route in the other half; with one panel only, in this one.
     *
     * @param {Array<string>} [sameIn] panels of the other half that can show the route's selection themselves
     */
    function jumpFrom(half, route, sameIn = []) {
        const other = current?.halves.includes(half) ? current.halves.find((candidate) => candidate !== half) : null;
        if (!other) {
            navigateFrom(half, route);
            return;
        }
        const wanted = sameIn.includes(other.route.view) ? { ...route, view: other.route.view } : route;
        if (wanted.bridge === other.route.bridge && href(wanted) === href(other.route)) {
            activate(other);
        } else {
            navigateFrom(other, wanted);
        }
    }

    function toggleSecond() {
        if (current === null) {
            return;
        }
        const { route } = current;
        const open = secondOf(route) === null;
        navigate(open ? withSecond(route, { view: route.view === 'group-addresses' ? 'buildings' : 'group-addresses', bridge: route.bridge, query: {} }) : firstOf(route));
    }

    function contextFor(half, bridge, state) {
        const { rows, stream } = servicesFor(bridge);
        return {
            document,
            element: half.element,
            api,
            stream,
            rows,
            bridge,
            route: half.route,
            navigate: (route) => navigateFrom(half, route),
            replace: (route) => replaceFrom(half, route),
            confirm: options.confirm,
            frame: options.frame,
            timers: options.timers,
            clipboard: options.clipboard,
            auth,
            fetch: options.fetch,
            base: options.base,
            showError,
            status: (words) => {
                half.status = words;
                if (current === state && (state.active ?? state.halves[0]) === half) {
                    setStatus(words);
                }
            },
            layout: options.layout,
            tab: options.tab ?? null,
            keep,
            history: options.history,
            jump: (route, sameIn) => jumpFrom(half, route, sameIn),
            split: {
                half: half.name,
                open: state.side !== null,
                toggle: SPLIT_VIEWS.includes(half.route.view) && wide() ? toggleSecond : null,
                /** {@return what the other panel shows, as {view, query}; null with one panel} */
                other: () => {
                    const shown = current === state ? state.halves.find((candidate) => candidate !== half) : null;
                    return shown ? { view: shown.route.view, query: { ...shown.route.query } } : null;
                },
                sidebar: state.side
                    ? {
                          face: () => (half.face = state.side.sidebar.face()),
                          release: (face) => {
                              state.side.sidebar.release(face);
                              if (half.face === face) {
                                  half.face = null;
                              }
                          },
                      }
                    : null,
            },
        };
    }

    function showHalf(half, bridge, state) {
        try {
            half.view = views[half.route.view].show(contextFor(half, bridge, state));
        } catch (error) {
            showError(error);
        }
    }

    function newHalf(name, route) {
        return { name, route, element: el(document, 'section', { class: name === 'second' ? 'view second' : 'view' }), view: null, face: null, status: undefined };
    }

    /** {@return whether the panels that are shown could take the route by being told of it} */
    function updated(route, bridge, second) {
        const state = current;
        const [top, bottom] = state?.halves ?? [];
        const first = firstOf(route);
        if (!top || state.route.bridge !== route.bridge || top.route.view !== first.view || (bottom === undefined) !== (second === null)) {
            return false;
        }
        const topMoved = href(first) !== href(top.route);
        const bottomMoved = bottom !== undefined && href(second) !== href(bottom.route);
        // an upper panel that could not be made is made again with every move
        if (top.view === null) {
            return false;
        }
        // an upper panel that cannot be told (the monitor) is left alone while only the lower half moves
        if (typeof top.view.update !== 'function' && (topMoved || !bottomMoved)) {
            return false;
        }
        showError(null);
        renderHeader(route, bridge);
        state.route = route;
        const tell = (half, to) => {
            half.route = to;
            try {
                half.view.update(to);
            } catch (error) {
                showError(error);
            }
        };
        if (topMoved || !bottomMoved) {
            tell(top, first);
        }
        if (bottom !== undefined && (bottomMoved || !topMoved)) {
            if (bottom.route.view === second.view && typeof bottom.view?.update === 'function') {
                tell(bottom, second);
            } else {
                // another panel in the lower half: the upper one stays as it is
                const focusWas = bottom.element.contains(document.activeElement);
                closeHalf(bottom);
                const next = newHalf('second', second);
                state.halves[1] = next;
                place(state.box, [top.element, state.border, next.element]);
                showHalf(next, bridge, state);
                if (state.active === bottom) {
                    state.active = null;
                }
                if (focusWas && typeof next.view?.focus === 'function') {
                    next.view.focus();
                }
            }
        }
        activate(bottomMoved && !topMoved ? state.halves[1] : topMoved || state.active === null ? top : state.active);
        return true;
    }

    /** @param {boolean} again make the panel again, also when it is the one shown */
    function showRoute(route, again) {
        const bridge = route.view === CHOOSER ? null : bridges.find((candidate) => candidate.uid === route.bridge);
        const second = bridge && wide() ? secondOf(route) : null;
        if (!again && bridge && updated(route, bridge, second)) {
            return;
        }
        // a panel left by a key or a click inside it hands the focus to the panel that comes
        const focusWas = main.contains(document.activeElement);
        closeView();
        showError(null);
        renderHeader(route, bridge);
        renderStatus(bridge);
        const top = newHalf('first', firstOf(route));
        const state = { route, halves: [], active: null, side: null, box: null, border: null, close: null };
        current = state;
        if (!bridge) {
            add(document, main, top.element);
            renderChooser(top.element);
            return;
        }
        state.halves.push(top);
        if (second === null) {
            add(document, main, top.element);
            showHalf(top, bridge, state);
        } else {
            const bottom = newHalf('second', second);
            state.halves.push(bottom);
            const outer = el(document, 'section', { class: 'view split' });
            state.box = el(document, 'div', { class: 'halves' });
            // the window may give the lower half less than the kept height
            const border = createSplitter({ document, target: state.box, property: '--second-height', name: 'second', layout: options.layout, ...SECOND_HEIGHT, reverse: true, rows: true, label: text('frame.resizeSecond'), className: 'row-splitter', actual: () => bottom.element.clientHeight });
            state.border = border.node;
            // Find goes to what it found in the half that was used last
            const context = { ...contextFor(top, bridge, state), navigate: (to) => navigateFrom(state.active ?? top, to) };
            state.side = createSide({ document, layout: options.layout, target: outer, context });
            const used = (event) => {
                const half = state.halves.find((candidate) => candidate.element.contains(event.target));
                if (half) {
                    activate(half);
                }
            };
            // a link of a panel names that panel alone: followed plainly it would close the other half
            const followed = (event) => {
                let link = event.target;
                while (link && link !== outer && link.tagName !== 'A') {
                    link = link.parentNode;
                }
                const to = link && link !== outer ? link.getAttribute('href') : null;
                if (typeof to !== 'string' || !to.startsWith('#/') || event.defaultPrevented || event.button > 0 || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) {
                    return;
                }
                const route = parse(to);
                if (route.view !== CHOOSER) {
                    event.preventDefault();
                    // a link in the sidebar belongs to the half the sidebar shows
                    navigateFrom(state.halves.find((candidate) => candidate.element.contains(link)) ?? state.active ?? top, route);
                }
            };
            outer.addEventListener('pointerdown', used);
            outer.addEventListener('focusin', used);
            outer.addEventListener('click', followed);
            state.close = () => {
                outer.removeEventListener('pointerdown', used);
                outer.removeEventListener('focusin', used);
                outer.removeEventListener('click', followed);
                border.close();
                state.side.close();
            };
            add(document, state.box, top.element, border.node, bottom.element);
            add(document, outer, state.box, state.side.splitter.node, state.side.sidebar.node);
            add(document, main, outer);
            showHalf(top, bridge, state);
            showHalf(bottom, bridge, state);
        }
        activate(top);
        if (focusWas && typeof top.view?.focus === 'function') {
            top.view.focus();
        }
    }

    /** {@return the error of the call, or null} */
    async function loadBridges() {
        try {
            bridges = await api.bridges();
            bridgesLoaded = true;
            return null;
        } catch (error) {
            bridges = [];
            bridgesLoaded = false;
            return error;
        }
    }

    /**
     * Shows the route of the address's hash.
     *
     * @param {boolean} [again] make the panel again, also when it is the one shown
     */
    async function showHash(hash, again = false) {
        if (!auth.signedIn()) {
            return;
        }
        const turn = ++shown;
        const parsed = parse(hash);
        const recheck = parsed.view !== CHOOSER && bridgesLoaded && deadServices(parsed.bridge);
        const before = bridges;
        const read = parsed.view === CHOOSER || !bridgesLoaded || recheck;
        const failure = read ? await loadBridges() : null;
        if (turn !== shown || !auth.signedIn()) {
            return;
        }
        const absent = recheck && !presentIn(bridges, parsed.bridge);
        if (absent) {
            // the same view comes back with the old services and their stopped text
            bridges = before;
            bridgesLoaded = true;
        }
        const route = resolve(parsed, bridges);
        const wanted = href(route);
        if (wanted !== hash) {
            location.replaceHash(wanted);
        }
        if (read && !absent && deadServices(route.bridge) && presentIn(bridges, route.bridge)) {
            closeView();
            closeServices();
        }
        showRoute(route, again);
        if (failure) {
            showError(failure);
        }
    }

    /** Goes to the route; the route already shown is shown again, as setting the same hash fires no hashchange. */
    function navigate(route) {
        const wanted = href(route);
        if (wanted === location.hash()) {
            showHash(wanted, true);
        } else {
            location.setHash(wanted);
        }
    }

    function showLoading() {
        closeView();
        add(document, main, el(document, 'p', { class: 'note' }, text('app.loading')));
    }

    /**
     * Shows the signed-out page.
     *
     * @param {{message?: string, retry?: boolean, final?: boolean}} [outcome] message is a key of texts.js; retry
     *     offers "Try again" in place of "Sign in"; final offers no button, as nothing on the page can help
     */
    function signedOut({ message, retry, final } = {}) {
        closeView();
        closeServices();
        keep.clear();
        bridges = [];
        bridgesLoaded = false;
        renderHeader(null, null);
        renderStatus(null);
        showError(null);
        const button = final
            ? null
            : retry
            ? el(document, 'button', { type: 'button', class: 'try-again' }, text('login.tryAgain'))
            : el(document, 'button', { type: 'button', class: 'sign-in' }, text('login.signIn'));
        add(
            document,
            main,
            el(
                document,
                'section',
                { class: 'signed-out' },
                el(document, 'h2', {}, text('login.signedOut')),
                message ? el(document, 'p', { class: 'note' }, text(message)) : null,
                button,
            ),
        );
        signedOutShown = true;
    }

    /** Signs in by the first way that works and shows the route of the address. */
    async function begin() {
        signedOutShown = false;
        renderHeader(null, null);
        showLoading();
        const outcome = await auth.start();
        if (!outcome.signedIn) {
            signedOut(outcome);
            return;
        }
        if (outcome.route) {
            location.replaceHash(href(parse(outcome.route)));
        }
        await showHash(location.hash());
    }

    async function signIn() {
        try {
            location.assign(await auth.signIn(location.hash()));
        } catch (error) {
            showError(error);
        }
    }

    async function signOut() {
        closeView();
        closeServices();
        await auth.signOut();
        signedOut({});
    }

    /** The page is left: the view and the bridge's stream end. */
    function stop() {
        if (signedOutShown) {
            // nothing runs behind the signed-out page; a page the browser shows again must keep its button
            return;
        }
        closeView();
        closeServices();
    }

    return { begin, showHash, signedOut, stop, navigate };
}
