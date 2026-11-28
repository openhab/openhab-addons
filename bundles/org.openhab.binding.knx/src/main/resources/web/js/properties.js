// The Properties container's content: the fields of what is selected under three small tabs, Settings, Comments and
// Information. Everything is read-only: the page changes nothing in the project. All three tabs are in the document,
// the style sheet shows one: the tab the viewer chose, or the first that holds something when that one is empty.

import { add, attr, clear, el } from './dom.js';
import { text } from './texts.js';

/** The tabs in their order; a section without a tab goes to the first. */
export const PROPERTY_TABS = Object.freeze(['settings', 'comments', 'information']);

function present(value) {
    return value !== undefined && value !== null && value !== '' && value !== false;
}

/**
 * {@return label and value rows as a list, null when no row has a value}
 *
 * @param {Array<[string, *, string?]>} rows a row is [label, value, name]; a row without a value is left out
 * @param {Map<string, Element>} [fields] takes the value's element of each row with a name
 */
export function factList(document, rows, fields) {
    const shown = (rows ?? []).filter(([, value]) => present(value));
    if (shown.length === 0) {
        return null;
    }
    return el(
        document,
        'dl',
        { class: 'props' },
        shown.map(([label, value, name]) => {
            const field = el(document, 'dd', {}, typeof value === 'number' ? String(value) : value);
            if (name !== undefined && fields) {
                fields.set(name, field);
            }
            return [el(document, 'dt', {}, label), field];
        }),
    );
}

/** {@return read-only tick boxes, one per [label, ticked]} */
function tickList(document, ticks) {
    return el(
        document,
        'ul',
        { class: 'ticks' },
        ticks.map(([label, ticked]) => el(document, 'li', {}, el(document, 'label', { class: 'tick' }, el(document, 'input', { type: 'checkbox', disabled: true, checked: ticked === true }), label))),
    );
}

/**
 * @param {Document} document
 * @param {{get: Function, set: Function}} [layout] keeps the tab the viewer chose
 * @returns {{node: Element, show: Function, empty: Function, set: Function, tab: Function, close: Function}}
 */
export function createProperties(document, layout) {
    const node = el(document, 'div', { class: 'properties' });
    const fields = new Map();
    const tabs = PROPERTY_TABS.map((name) => el(document, 'button', { type: 'button', role: 'tab', class: `prop-tab prop-tab-${name}`, 'aria-selected': 'false' }, text(`frame.${name}`)));
    const tabRow = el(document, 'div', { class: 'prop-tabs', role: 'tablist', 'aria-label': text('frame.properties') }, tabs);
    const panels = PROPERTY_TABS.map((name) => el(document, 'div', { class: `tab-panel tab-${name}`, role: 'tabpanel', 'aria-label': text(`frame.${name}`) }));
    let current = PROPERTY_TABS.includes(layout?.get('propertiesTab')) ? layout.get('propertiesTab') : PROPERTY_TABS[0];

    /** Shows the tab; the tab the viewer chose stays the chosen one. */
    function showTab(name) {
        PROPERTY_TABS.forEach((tab, at) => {
            const on = tab === name;
            attr(tabs[at], 'aria-selected', on ? 'true' : 'false');
            attr(tabs[at], 'tabindex', on ? 0 : -1);
            tabs[at].classList.toggle('current', on);
            panels[at].classList.toggle('off', !on);
        });
    }

    function empty() {
        fields.clear();
        clear(node);
        add(document, node, el(document, 'p', { class: 'note' }, text('frame.noSelection')));
    }

    const onClick = (event) => {
        const at = tabs.indexOf(event.target);
        if (at >= 0) {
            current = PROPERTY_TABS[at];
            showTab(current);
            layout?.set('propertiesTab', current);
        }
    };
    const onKey = (event) => {
        const at = tabs.indexOf(event.target);
        const step = event.key === 'ArrowRight' ? 1 : event.key === 'ArrowLeft' ? -1 : 0;
        if (at >= 0 && step !== 0) {
            event.preventDefault();
            const next = (at + step + tabs.length) % tabs.length;
            current = PROPERTY_TABS[next];
            showTab(current);
            layout?.set('propertiesTab', current);
            tabs[next].focus();
        }
    };
    node.addEventListener('click', onClick);
    node.addEventListener('keydown', onKey);

    showTab(current);
    empty();

    return {
        node,
        empty,
        /** {@return the name of the tab the viewer chose} */
        tab() {
            return current;
        },
        /**
         * Puts a new value into the row of this name and leaves everything else of the pane as it is.
         *
         * @returns {boolean} false when no such row is shown or the value is none: the caller shows the pane again
         */
        set(name, value) {
            const field = fields.get(name);
            if (!field || !present(value)) {
                return false;
            }
            clear(field);
            add(document, field, typeof value === 'number' ? String(value) : value);
            return true;
        },
        /**
         * @param {string} title what is selected
         * @param {Array<{tab?: string, heading?: string, rows?: Array<[string, *]>, ticks?: Array<[string, boolean]>,
         *     more?: Array<Element|null>}>} sections tab is a name of PROPERTY_TABS, the first without one; a row is
         *     [label, value]; the value is a text, a number or an element, and a row without a value is left out; ticks
         *     are read-only tick boxes; more are elements shown under the rows. A section with nothing in it is left
         *     out. A row may carry a third part, a name by which set() finds its value again.
         * @param {Array<Element|null>} [actions] what can be done with the selected thing, shown under its name
         */
        show(title, sections, actions = []) {
            fields.clear();
            clear(node);
            panels.forEach(clear);
            add(document, node, el(document, 'p', { class: 'subject' }, String(title ?? '')));
            if (actions.some(Boolean)) {
                add(document, node, el(document, 'p', { class: 'actions' }, actions));
            }
            for (const section of sections) {
                const list = factList(document, section.rows, fields);
                const ticks = Array.isArray(section.ticks) && section.ticks.length > 0 ? tickList(document, section.ticks) : null;
                const more = (section.more ?? []).filter(Boolean);
                if (list === null && ticks === null && more.length === 0) {
                    continue;
                }
                const panel = panels[Math.max(0, PROPERTY_TABS.indexOf(section.tab))];
                add(document, panel, el(document, 'section', { class: 'group' }, present(section.heading) ? el(document, 'h4', {}, section.heading) : null, list, ticks, more));
            }
            const filled = panels.map((panel) => panel.firstChild !== null);
            for (const panel of panels) {
                if (panel.firstChild === null) {
                    add(document, panel, el(document, 'p', { class: 'note' }, text('frame.nothingHere')));
                }
            }
            // a selection with nothing under the chosen tab shows its first tab that holds something
            const at = PROPERTY_TABS.indexOf(current);
            showTab(filled[at] || !filled.includes(true) ? current : PROPERTY_TABS[filled.indexOf(true)]);
            add(document, node, tabRow, panels);
        },
        close() {
            node.removeEventListener('click', onClick);
            node.removeEventListener('keydown', onKey);
        },
    };
}
