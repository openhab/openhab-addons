// The only module that makes elements and sets attributes. Server text only ever becomes a text node or the
// value of an attribute of the fixed list below. The icons are the page's own drawings, kept here as path data.

export const ATTRIBUTES = Object.freeze([
    'id',
    'class',
    'title',
    'href',
    'type',
    'name',
    'value',
    'placeholder',
    'autocomplete',
    'for',
    'role',
    'aria-live',
    'aria-modal',
    'aria-label',
    'colspan',
    'open',
    'disabled',
    'readonly',
    'maxlength',
    'checked',
    'tabindex',
    'scope',
    'aria-selected',
    'aria-expanded',
    'aria-pressed',
    'aria-sort',
    'aria-current',
    'aria-level',
    'aria-orientation',
    'aria-valuenow',
    'aria-valuemin',
    'aria-valuemax',
]);

const ALLOWED = new Set(ATTRIBUTES);
const TAG = /^[a-z][a-z0-9]*$/;
const REFUSED_TAGS = new Set([
    'base',
    'embed',
    'form',
    'frame',
    'frameset',
    'iframe',
    'img',
    'link',
    'math',
    'meta',
    'object',
    'script',
    'style',
    'svg',
    'template',
]);
const NAME = /^[A-Za-z][\w-]*$/;
const CLASSES = /^[A-Za-z][\w-]*( [A-Za-z][\w-]*)*$/;

function checkValue(name, text) {
    if (name === 'href' && !text.startsWith('#/')) {
        throw new TypeError('an href must be a route of the page, made by router.js');
    }
    if (name === 'id' && !NAME.test(text)) {
        throw new TypeError('an id is a fixed name of the page');
    }
    if (name === 'class' && !CLASSES.test(text)) {
        throw new TypeError('a class is a fixed name of the page');
    }
}

/**
 * Sets one attribute of the fixed list. true sets it empty, false, null and undefined remove it.
 *
 * @param {Element} node
 * @param {string} name
 * @param {string|number|boolean|null|undefined} value
 */
export function attr(node, name, value) {
    if (!ALLOWED.has(name)) {
        throw new TypeError(`the attribute ${String(name)} is not one the page sets`);
    }
    if (value === false || value === null || value === undefined) {
        node.removeAttribute(name);
        return;
    }
    const text = value === true ? '' : String(value);
    checkValue(name, text);
    node.setAttribute(name, text);
}

function isNode(child) {
    return typeof child === 'object' && typeof child.nodeType === 'number' && typeof child.appendChild === 'function';
}

function appendAll(document, node, children) {
    for (const child of children) {
        if (child === null || child === undefined || child === false) {
            continue;
        }
        if (Array.isArray(child)) {
            appendAll(document, node, child);
        } else if (typeof child === 'string' || typeof child === 'number') {
            node.appendChild(document.createTextNode(String(child)));
        } else if (isNode(child)) {
            node.appendChild(child);
        } else {
            throw new TypeError('a child is a text, a number or an element');
        }
    }
}

/**
 * @param {Document} document
 * @param {string} tag a plain tag name of the page's code
 * @param {Object<string, *>} [attributes] names of the fixed list only
 * @param {...*} children texts (made into text nodes), numbers, elements, lists of these; null is skipped
 * @returns {Element}
 */
export function el(document, tag, attributes, ...children) {
    if (typeof tag !== 'string' || !TAG.test(tag) || REFUSED_TAGS.has(tag)) {
        throw new TypeError(`the page does not make <${String(tag)}> elements`);
    }
    const node = document.createElement(tag);
    for (const [name, value] of Object.entries(attributes ?? {})) {
        attr(node, name, value);
    }
    appendAll(document, node, children);
    return node;
}

/** Appends children as el does. */
export function add(document, node, ...children) {
    appendAll(document, node, children);
    return node;
}

/** Removes every child of the node. */
export function clear(node) {
    while (node.firstChild) {
        node.removeChild(node.firstChild);
    }
}

/** Puts the nodes into the parent in this order, moving only what is out of place. */
export function place(parent, nodes) {
    // what leaves goes first, or every node that stays would be moved in front of it
    const staying = new Set(nodes);
    for (const child of [...parent.childNodes]) {
        if (!staying.has(child)) {
            parent.removeChild(child);
        }
    }
    for (let at = 0; at < nodes.length; at++) {
        const here = parent.childNodes[at] ?? null;
        if (here !== nodes[at]) {
            parent.insertBefore(nodes[at], here);
        }
    }
    while (parent.childNodes.length > nodes.length) {
        parent.removeChild(parent.lastChild);
    }
}

const SVG = 'http://www.w3.org/2000/svg';

/** The page's icons: each a few strokes on a 16 by 16 grid, drawn for this page. */
export const ICONS = Object.freeze({
    chevron: ['M6 3.5l4.5 4.5L6 12.5'],
    check: ['M3.5 8.5l3 3l6-7'],
    key: ['M4.5 8a2 2 0 1 0 4 0a2 2 0 1 0-4 0', 'M8.5 8h5.5M12 8v2.2M14 8v1.6'],
    send: ['M3 8h9M9 5l3 3-3 3'],
    range: ['M2.5 4.5h4l1.2 1.5h5.8v6.5h-11z'],
    address: ['M8 2.5l5.5 5.5L8 13.5L2.5 8z'],
    area: ['M2.5 3.5h11v9h-11z', 'M2.5 6.5h11'],
    line: ['M2.5 8h11M5 5.5v5M8 5.5v5M11 5.5v5'],
    device: ['M3.5 3.5h9v9h-9z', 'M6 6.5h4M6 9.5h4'],
    building: ['M3.5 13.5v-10h6v10M9.5 6.5h3v7M2 13.5h12', 'M5.5 6h2M5.5 9h2'],
    part: ['M2.5 13.5v-7h5v7M7.5 13.5v-10h6v10M2 13.5h12', 'M9.5 6h2M9.5 9h2'],
    floor: ['M2.5 4.5h11M2.5 8h11M2.5 11.5h11', 'M2.5 2.5v11M13.5 2.5v11'],
    stairway: ['M2.5 13.5h3v-3h3v-3h3v-3h2.5'],
    corridor: ['M5.5 2.5v11M10.5 2.5v11', 'M8 5v1.5M8 9.5v1.5'],
    room: ['M3 13.5v-10h10v10z', 'M9.5 13.5v-4h-3v4'],
    cabinet: ['M3.5 2.5h9v11h-9z', 'M8 2.5v11', 'M6 7.5v1.5M10 7.5v1.5'],
    ground: ['M2 12.5h12', 'M8 12.5v-4', 'M8 8.5a3 3 0 1 1 .01 0'],
    space: ['M3 3h10v10h-10z'],
    function: ['M8 2.5v2M8 11.5v2M2.5 8h2M11.5 8h2', 'M5.5 8a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0-5 0'],
    panel: ['M2.5 3.5h11v9h-11z', 'M10 3.5v9'],
    menu: ['M3 4.5h10M3 8h10M3 11.5h10'],
    close: ['M4 4l8 8M12 4l-8 8'],
    back: ['M10 3.5L5.5 8l4.5 4.5'],
    forward: ['M6 3.5L10.5 8L6 12.5'],
    search: ['M3.5 7a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0-7 0', 'M9.6 9.6l3.4 3.4'],
    split: ['M2.5 3.5h11v9h-11z', 'M2.5 8h11'],
    pulse: ['M2 8h3l1.5-4l3 8l1.5-4h3'],
    link: ['M6.5 9.5l3-3', 'M7 5l1-1a2.5 2.5 0 0 1 3.5 3.5l-1 1', 'M9 11l-1 1a2.5 2.5 0 0 1-3.5-3.5l1-1'],
    folder: ['M2.5 4.5h4l1.2 1.5h5.8v6.5h-11z', 'M6.8 9.2a1.4 1.4 0 1 0 2.8 0a1.4 1.4 0 1 0-2.8 0'],
    pencil: ['M3 13l.6-2.8L10.8 3L13 5.2L5.8 12.4z', 'M9.4 4.4l2.2 2.2'],
    plus: ['M8 3.5v9M3.5 8h9'],
    minus: ['M3.5 8h9'],
    same: ['M4 6h8M4 10h8'],
    warn: ['M8 2.5l6 10.5h-12z', 'M8 6.5v3M8 11v.6'],
    error: ['M8 2.5a5.5 5.5 0 1 0 0 11a5.5 5.5 0 1 0 0-11', 'M5.8 5.8l4.4 4.4M10.2 5.8l-4.4 4.4'],
    bridge: ['M2 11.5h12M3.5 11.5v-3a4.5 4.5 0 0 1 9 0v3'],
    thing: ['M3 4.5h10v7h-10z', 'M5.5 11.5v2M10.5 11.5v2M6 8h4'],
    channel: ['M2.5 8h4M9.5 8h4', 'M6.5 5.5v5M9.5 5.5v5'],
    item: ['M8 2.5l5 3v5l-5 3l-5-3v-5z'],
    group: ['M2.5 5.5h7v7h-7z', 'M5.5 5.5v-2h8v7h-4'],
    undo: ['M5.5 4L3 6.5L5.5 9', 'M3 6.5h6a3.5 3.5 0 0 1 0 7h-2'],
    expand: ['M4 4l4 3.5l4-3.5', 'M4 8.5l4 3.5l4-3.5'],
    collapse: ['M4 7.5l4-3.5l4 3.5', 'M4 12l4-3.5l4 3.5'],
    redo: ['M10.5 4L13 6.5L10.5 9', 'M13 6.5h-6a3.5 3.5 0 0 0 0 7h2'],
});

/**
 * @param {Document} document
 * @param {string} name a name of ICONS
 * @returns {Element} the drawing, hidden from screen readers: the text beside it or the label of its parent says it
 */
export function icon(document, name) {
    if (!Object.hasOwn(ICONS, name)) {
        throw new TypeError(`the page has no icon ${String(name)}`);
    }
    const svg = document.createElementNS(SVG, 'svg');
    svg.setAttribute('class', `icon icon-${name}`);
    svg.setAttribute('viewBox', '0 0 16 16');
    svg.setAttribute('aria-hidden', 'true');
    svg.setAttribute('focusable', 'false');
    for (const d of ICONS[name]) {
        const path = document.createElementNS(SVG, 'path');
        path.setAttribute('d', d);
        svg.appendChild(path);
    }
    return svg;
}

/** The widths the page sets, as custom properties the style sheet reads. */
export const SIZES = Object.freeze(['--tree-width', '--properties-width', '--col-width', '--second-height']);

/** Sets one size of SIZES in px; null takes it away. */
export function size(node, name, px) {
    if (!SIZES.includes(name)) {
        throw new TypeError(`the page sets no size ${String(name)}`);
    }
    if (px === null || px === undefined) {
        node.style.removeProperty(name);
    } else if (Number.isFinite(px)) {
        node.style.setProperty(name, `${Math.round(px)}px`);
    }
}

/** The themes a viewer can choose; anything else follows the system. */
export const THEMES = Object.freeze(['light', 'dark']);

/** Names the chosen theme on the document's root for the style sheet; another name takes the choice away. */
export function theme(root, name) {
    if (THEMES.includes(name)) {
        root.setAttribute('data-theme', name);
    } else {
        root.removeAttribute('data-theme');
    }
}

/** The pages of openHAB's Main UI the page links to, each below the root of openHAB, and what their id looks like. */
const MAIN_UI = Object.freeze({
    thing: ['settings/things/', /^[A-Za-z0-9_-]+(:[A-Za-z0-9_-]+){2,3}$/],
    item: ['settings/items/', /^[A-Za-z_][A-Za-z0-9_]*$/],
});

/**
 * A link to the page of a Thing or an Item in openHAB's Main UI, opened in a tab of its own. The address is made
 * here from the kind and the id; an id that is none, or a root that is not known, gives the words without a link.
 *
 * @param {Document} document
 * @param {URL|undefined} root the root of openHAB, where Main UI stands
 * @param {string} kind thing or item
 * @param {string} id the Thing's UID or the Item's name
 * @param {...*} children as for el
 * @returns {Element}
 */
export function mainUiLink(document, root, kind, id, ...children) {
    const page = Object.hasOwn(MAIN_UI, kind) ? MAIN_UI[kind] : null;
    if (!page || !(root instanceof URL) || typeof id !== 'string' || !page[1].test(id)) {
        return el(document, 'span', { class: 'main-ui' }, ...children);
    }
    const link = el(document, 'a', { class: 'main-ui' }, ...children);
    link.setAttribute('href', new URL(page[0] + id, root).pathname);
    link.setAttribute('target', '_blank');
    link.setAttribute('rel', 'noopener noreferrer');
    return link;
}
