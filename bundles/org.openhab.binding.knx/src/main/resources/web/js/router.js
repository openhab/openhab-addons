// The page's routes, after the "#": "/" (bridge chooser) and "/b/<bridge UID>/<panel>" (the wizard among them), each
// part URI-encoded, optionally followed by "?name=value&…"; what is selected in a panel is its "sel" value, the
// selected row of its list its "row" value, the tab of the list its "tab" value. The
// addresses of the page before it had panels still open the matching place. A second panel under the first is the
// "p2" value (its panel) and its own values with "p2-" before their names. Every href of the page is made here.

export const CHOOSER = 'chooser';
export const WIZARD = 'wizard';

/** The panels of a bridge, in the order of the tabs; the wizard is reached from "Add to openHAB" and is no tab. */
export const VIEWS = ['group-addresses', 'topology', 'buildings', 'monitor', 'project', WIZARD];
export const NAV_VIEWS = VIEWS.filter((view) => view !== WIZARD);
/** What a bridge without a handler (a stored project of a removed bridge) still offers. */
export const REMOVED_VIEWS = ['group-addresses', 'topology', 'buildings', 'project'];
/** The panels that can stand in one half of a workplace with two panels. */
export const SPLIT_VIEWS = ['group-addresses', 'topology', 'buildings', 'monitor'];
/** The names the views had before the panels, and the panel Devices, whose list is the one of Topology's first node. */
export const OLD_VIEWS = Object.freeze({ addresses: 'group-addresses', building: 'buildings', setup: 'project', devices: 'topology' });

const QUERY_NAME = /^[A-Za-z][\w-]*$/;

/** {@return the route of the bridge chooser} */
export function chooser(query = {}) {
    return { view: CHOOSER, query };
}

function parseQuery(text) {
    const query = {};
    for (const part of text.split('&')) {
        if (part === '') {
            continue;
        }
        const at = part.indexOf('=');
        const name = decodeURIComponent(at < 0 ? part : part.slice(0, at));
        const value = at < 0 ? '' : decodeURIComponent(part.slice(at + 1));
        if (QUERY_NAME.test(name) && !Object.hasOwn(query, name)) {
            query[name] = value;
        }
    }
    return query;
}

/** {@return the query with a second panel that is no more put right: Devices under the first panel is Topology} */
function second(query) {
    return Object.hasOwn(OLD_VIEWS, query.p2) ? { ...query, p2: OLD_VIEWS[query.p2] } : query;
}

function parseParts(path, query) {
    if (path === '/') {
        return chooser(query);
    }
    const parts = path.slice(1).split('/').map(decodeURIComponent);
    if (parts[0] !== 'b' || parts.length < 3 || parts[1] === '') {
        return chooser();
    }
    const [, bridge, name, device] = parts;
    const view = Object.hasOwn(OLD_VIEWS, name) ? OLD_VIEWS[name] : name;
    if (parts.length === 3 && VIEWS.includes(view)) {
        return { view, bridge, query: second(query) };
    }
    // the device detail of the page before the panels: the device, selected in Topology
    if (parts.length === 4 && name === 'devices' && device !== '') {
        return { view: 'topology', bridge, query: { ...query, sel: `device:${device}` } };
    }
    return chooser();
}

/**
 * @param {string} hash the address's hash, with its "#"
 * @returns {{view: string, bridge?: string, query: Object<string, string>}} the chooser for
 *     anything that is not a route of the page or does not decode
 */
export function parse(hash) {
    const text = typeof hash === 'string' && hash.startsWith('#') ? hash.slice(1) : '';
    if (!text.startsWith('/')) {
        return chooser();
    }
    const at = text.indexOf('?');
    try {
        const query = at < 0 ? {} : parseQuery(text.slice(at + 1));
        return parseParts(at < 0 ? text : text.slice(0, at), query);
    } catch (e) {
        if (e instanceof URIError) {
            return chooser();
        }
        throw e;
    }
}

function queryPart(query) {
    const parts = [];
    for (const [name, value] of Object.entries(query ?? {})) {
        if (!QUERY_NAME.test(name)) {
            throw new TypeError('a query name is a plain name');
        }
        if (value !== undefined && value !== null) {
            parts.push(`${encodeURIComponent(name)}=${encodeURIComponent(String(value))}`);
        }
    }
    return parts.length === 0 ? '' : `?${parts.join('&')}`;
}

function filled(value) {
    return typeof value === 'string' && value !== '';
}

/**
 * @param {{view: string, bridge?: string, query?: Object<string, string>}} route
 * @returns {string} "#/" followed by the URI-encoded parts
 */
export function href(route) {
    const query = queryPart(route.query);
    if (route.view === CHOOSER) {
        return `#/${query}`;
    }
    if (!VIEWS.includes(route.view) || !filled(route.bridge)) {
        throw new TypeError('not a route of the page');
    }
    return `#/b/${encodeURIComponent(route.bridge)}/${route.view}${query}`;
}

/**
 * {@return the route of a panel with this node selected and, in its list, this row, under this tab; without a key,
 * the panel with nothing selected}
 */
export function selection(route, key, row, tab) {
    const query = filled(key) ? { sel: key } : {};
    if (filled(tab)) {
        query.tab = tab;
    }
    if (filled(row)) {
        query.row = row;
    }
    return { view: route.view, bridge: route.bridge, query };
}

const SECOND = 'p2';
const SECOND_PART = 'p2-';

function ofSecond(name) {
    return name === SECOND || name.startsWith(SECOND_PART);
}

/** {@return the route of the first panel alone: without what the address says about a second one} */
export function firstOf(route) {
    const query = Object.fromEntries(Object.entries(route.query ?? {}).filter(([name]) => !ofSecond(name)));
    return route.view === CHOOSER ? { view: CHOOSER, query } : { view: route.view, bridge: route.bridge, query };
}

/** {@return the route of the second panel the address names, or null: only a panel of SPLIT_VIEWS has one, and is one} */
export function secondOf(route) {
    const view = route?.query?.[SECOND];
    if (!SPLIT_VIEWS.includes(route?.view) || !SPLIT_VIEWS.includes(view)) {
        return null;
    }
    const query = {};
    for (const [name, value] of Object.entries(route.query)) {
        if (name.startsWith(SECOND_PART) && name.length > SECOND_PART.length) {
            query[name.slice(SECOND_PART.length)] = value;
        }
    }
    return { view, bridge: route.bridge, query };
}

/** {@return the route with this second panel under its first; without one, or where there can be none, the first alone} */
export function withSecond(route, second) {
    const first = firstOf(route);
    if (!second || !SPLIT_VIEWS.includes(first.view) || !SPLIT_VIEWS.includes(second.view)) {
        return first;
    }
    const query = { ...first.query, [SECOND]: second.view };
    for (const [name, value] of Object.entries(firstOf(second).query)) {
        query[`${SECOND_PART}${name}`] = value;
    }
    return { ...first, query };
}

/** {@return the views the bridge offers} */
export function viewsOf(bridge) {
    return bridge.present ? [...VIEWS] : [...REMOVED_VIEWS];
}

/**
 * {@return the route a bridge opens with: Buildings when its project has a building structure, else Group Addresses;
 * without a project the Group Monitor, or Group Addresses for a removed bridge}
 */
export function defaultRoute(bridge) {
    const view = bridge.projectState === 'LOADED' ? (bridge.projectSpaces > 0 ? 'buildings' : 'group-addresses') : bridge.present ? 'monitor' : 'group-addresses';
    return { view, bridge: bridge.uid, query: {} };
}

/**
 * @param route a parsed route
 * @param {Array<{uid: string, present: boolean}>} bridges the bridges of call 1
 * @returns the route to show: the chooser goes on to the only bridge, an unknown bridge gives the chooser, a view
 *     the bridge does not offer gives the bridge's default route, a second panel that cannot be is left out
 */
export function resolve(route, bridges) {
    if (route.view === CHOOSER) {
        return bridges.length === 1 ? defaultRoute(bridges[0]) : route;
    }
    const bridge = bridges.find((candidate) => candidate.uid === route.bridge);
    if (!bridge) {
        return resolve(chooser(), bridges);
    }
    if (!viewsOf(bridge).includes(route.view)) {
        return defaultRoute(bridge);
    }
    const second = secondOf(route);
    const named = Object.keys(route.query ?? {}).some(ofSecond);
    return named && !(second && viewsOf(bridge).includes(second.view)) ? firstOf(route) : route;
}
