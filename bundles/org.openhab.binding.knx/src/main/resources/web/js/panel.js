// One panel of the workplace: its bar (back, forward, the panel chooser, the path of what is selected, the Panels
// menu, the actions and the find box), its tree, and its list with the row of tabs above it. A panel holds everything it shows itself,
// so a workplace can hold more than one. On a narrow screen the style sheet makes the tree slide in, opened by a
// button of the bar.

import { add, attr, clear, el, icon } from './dom.js';
import { createSplitter } from './splitter.js';
import { text } from './texts.js';

export const TREE_WIDTH = { initial: 280, min: 140, max: 640 };

/**
 * @param {Object} options
 * @param {Document} options.document
 * @param {{get: Function, set: Function}} [options.layout]
 * @param {string} options.title the panel's name
 * @param {boolean} [options.tree] false for a panel without a tree
 * @param {Array<{view: string, label: string}>} [options.panels] what the chooser offers
 * @param {string} [options.current] the view of this panel, one of panels
 * @param {(view: string) => void} [options.onPanel] another panel was chosen
 * @param {{back: Function, forward: Function}} [options.history] the browser's history
 * @param {{open: boolean, toggle: Function}} [options.second] the second panel of the workplace, for the Panels menu;
 *     without it the bar has no such menu
 * @returns {Object} node, tools (where the panel's content puts its actions), find (where it puts its find box),
 *     treePane, list, and the calls below
 */
export function createPanel({ document, layout, title, tree = true, panels = [], current, onPanel, history, second }) {
    const backButton = el(document, 'button', { type: 'button', class: 'tool nav-back', 'aria-label': text('frame.back'), title: text('frame.back') }, icon(document, 'back'));
    const forwardButton = el(document, 'button', { type: 'button', class: 'tool nav-forward', 'aria-label': text('frame.forward'), title: text('frame.forward') }, icon(document, 'forward'));
    const chooser = el(
        document,
        'select',
        { class: 'panel-chooser', name: 'panel', 'aria-label': text('frame.panel') },
        (panels.length > 0 ? panels : [{ view: current ?? '', label: title }]).map((panel) => el(document, 'option', { value: panel.view }, panel.label)),
    );
    chooser.value = current ?? '';
    const treeToggle = tree
        ? el(document, 'button', { type: 'button', class: 'tool tree-toggle', 'aria-label': text('frame.showTree'), title: text('frame.showTree'), 'aria-expanded': 'false' }, icon(document, 'menu'))
        : null;
    const crumbs = el(document, 'nav', { class: 'crumbs', 'aria-label': text('frame.path') });
    const tools = el(document, 'div', { class: 'tools' });
    const find = el(document, 'div', { class: 'find-slot' });
    const menuButton = second
        ? el(document, 'button', { type: 'button', class: 'tool panels-menu', 'aria-expanded': 'false' }, icon(document, 'split'), el(document, 'span', { class: 'label' }, text('frame.panels')))
        : null;
    const secondItem = second ? el(document, 'button', { type: 'button', role: 'menuitem', class: 'second-panel' }, text(second.open ? 'frame.closeSecond' : 'frame.openSecond')) : null;
    const menu = second ? el(document, 'div', { class: 'menu', role: 'menu', 'aria-label': text('frame.panels') }, secondItem) : null;
    const menuBox = second ? el(document, 'span', { class: 'menu-box' }, menuButton, menu) : null;
    const bar = el(document, 'div', { class: 'tool-row panel-bar' }, backButton, forwardButton, chooser, treeToggle, crumbs, tools, menuBox, find);
    const tabRow = el(document, 'div', { class: 'list-tabs', role: 'tablist', 'aria-label': text('frame.listTabs') });
    // the list can take the focus itself while its rows are still being read
    const list = el(document, 'div', { class: 'list', tabindex: -1 });
    const treePane = tree ? el(document, 'aside', { class: 'pane tree-pane', 'aria-label': title }) : null;
    const expandButton = el(document, 'button', { type: 'button', class: 'tool expand-all', 'aria-label': text('frame.expandAll'), title: text('frame.expandAll') }, icon(document, 'expand'));
    const collapseButton = el(document, 'button', { type: 'button', class: 'tool collapse-all', 'aria-label': text('frame.collapseAll'), title: text('frame.collapseAll') }, icon(document, 'collapse'));
    const treeHead = el(document, 'div', { class: 'tree-head' }, el(document, 'span', { class: 'title' }, title), expandButton, collapseButton);
    const treeNote = el(document, 'p', { class: 'note tree-note', role: 'status' });
    const node = el(document, 'div', { class: tree ? 'panel' : 'panel no-tree' });
    const splitter = tree ? createSplitter({ document, target: node, property: '--tree-width', name: 'tree', layout, ...TREE_WIDTH, label: text('frame.resizeTree') }) : null;
    add(document, node, bar, el(document, 'div', { class: 'panel-body' }, treePane, splitter?.node ?? null, el(document, 'div', { class: 'pane list-pane' }, tabRow, list)));

    let treeControl = null;
    let crumbKeys = new Map();
    let onCrumb = null;
    let tabKeys = new Map();
    let onTab = null;

    /** @returns {boolean} whether the slide-in tree was open before */
    function showTree(open) {
        const was = node.classList.contains('tree-open');
        node.classList.toggle('tree-open', open);
        if (treeToggle) {
            attr(treeToggle, 'aria-expanded', open ? 'true' : 'false');
            attr(treeToggle, 'title', text(open ? 'frame.hideTree' : 'frame.showTree'));
            attr(treeToggle, 'aria-label', text(open ? 'frame.hideTree' : 'frame.showTree'));
        }
        return was;
    }

    // a click outside the panel closes the menu too
    const onOutside = (event) => {
        if (!node.contains(event.target)) {
            showMenu(false);
        }
    };

    function showMenu(open) {
        if (menu) {
            menuBox.classList.toggle('open', open);
            attr(menuButton, 'aria-expanded', open ? 'true' : 'false');
            document.removeEventListener('click', onOutside);
            if (open) {
                document.addEventListener('click', onOutside);
            }
        }
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
        if (menu && target !== menuButton) {
            showMenu(false);
        }
        if (target === menuButton) {
            const open = !menuBox.classList.contains('open');
            showMenu(open);
            if (open) {
                secondItem.focus();
            }
        } else if (target === secondItem) {
            second.toggle();
        } else if (target === backButton) {
            history?.back();
        } else if (target === forwardButton) {
            history?.forward();
        } else if (target === treeToggle) {
            const open = !node.classList.contains('tree-open');
            showTree(open);
            if (open) {
                treeControl?.focus();
            } else if (treePane.contains(document.activeElement)) {
                // not every browser puts the focus on a clicked button
                treeToggle.focus();
            }
        } else if (target === expandButton) {
            // a huge tree is opened only as far as the page can draw at once
            const complete = treeControl?.expandAll?.();
            clear(treeNote);
            add(document, treeNote, complete === false ? text('frame.expandCapped') : null);
        } else if (target === collapseButton) {
            treeControl?.collapseAll?.();
            clear(treeNote);
        } else if (crumbKeys.has(target)) {
            onCrumb?.(crumbKeys.get(target));
        } else if (tabKeys.has(target)) {
            onTab?.(tabKeys.get(target));
        }
    };
    const onKey = (event) => {
        if (event.key === 'Escape' && menu && menuBox.classList.contains('open')) {
            event.preventDefault();
            showMenu(false);
            menuButton.focus();
            return;
        }
        if (event.key === 'Escape' && node.classList.contains('tree-open') && treePane?.contains(event.target)) {
            event.preventDefault();
            showTree(false);
            treeToggle.focus();
            return;
        }
        const tabs = [...tabKeys.keys()];
        const at = tabs.indexOf(event.target);
        const step = event.key === 'ArrowRight' ? 1 : event.key === 'ArrowLeft' ? -1 : 0;
        if (at >= 0 && step !== 0 && tabs.length > 1) {
            event.preventDefault();
            const key = tabKeys.get(tabs[(at + step + tabs.length) % tabs.length]);
            onTab?.(key);
            // the row of tabs was drawn again
            [...tabKeys.entries()].find(([, name]) => name === key)?.[0].focus();
        }
    };
    const onChange = (event) => {
        if (event.target === chooser && chooser.value !== current) {
            const wanted = chooser.value;
            // the chooser names the panel that is shown until another one is
            chooser.value = current ?? '';
            onPanel?.(wanted);
        }
    };
    node.addEventListener('click', onClick);
    node.addEventListener('keydown', onKey);
    chooser.addEventListener('change', onChange);

    return {
        node,
        bar,
        tools,
        find,
        treePane,
        list,
        /** Puts the panel's tree into its pane; the slide-in tree of a narrow screen takes the focus when it opens. */
        setTree(control) {
            treeControl = control;
            add(document, treePane, typeof control.expandAll === 'function' ? [treeHead, treeNote] : null, control.node);
        },
        /**
         * The path of what is selected, outermost first. Each crumb but the last is a button.
         *
         * @param {Array<{key: string, label: string, icon?: string, iconTitle?: string}>} path
         * @param {(key: string) => void} [onPick] a crumb was chosen
         */
        setCrumbs(path, onPick) {
            clear(crumbs);
            crumbKeys = new Map();
            onCrumb = onPick ?? null;
            path.forEach((crumb, at) => {
                const last = at === path.length - 1;
                const drawing = crumb.icon && crumb.iconTitle ? el(document, 'span', { class: 'kind-icon', role: 'img', title: crumb.iconTitle, 'aria-label': crumb.iconTitle }, icon(document, crumb.icon)) : null;
                const button = el(document, 'button', { type: 'button', class: last ? 'crumb current' : 'crumb', 'aria-current': last ? 'true' : null }, drawing, crumb.label);
                crumbKeys.set(button, crumb.key);
                add(document, crumbs, button);
            });
        },
        /**
         * The tabs above the list, each with the number of its rows.
         *
         * @param {Array<{key: string, label: string, count?: number}>} tabs
         * @param {string} shown the key of the tab whose list is shown
         * @param {(key: string) => void} [onPick] another tab was chosen
         */
        setTabs(tabs, shown, onPick) {
            const focused = tabKeys.get(document.activeElement);
            clear(tabRow);
            tabKeys = new Map();
            onTab = onPick ?? null;
            for (const tab of tabs) {
                const on = tab.key === shown;
                const label = Number.isFinite(tab.count) ? text('frame.tabCount', tab.label, tab.count) : tab.label;
                const button = el(document, 'button', { type: 'button', role: 'tab', class: on ? 'list-tab current' : 'list-tab', 'aria-selected': on ? 'true' : 'false', tabindex: on ? 0 : -1 }, label);
                tabKeys.set(button, tab.key);
                add(document, tabRow, button);
                if (focused === tab.key) {
                    button.focus();
                }
            }
        },
        /**
         * Slides the tree away again on a narrow screen, after a node was chosen.
         *
         * @returns {boolean} true when it was open: the focus that was in it must go to the list
         */
        closeTree() {
            return showTree(false);
        },
        /** Puts the keyboard focus on the list itself, for the time it has no row to take it. */
        focusList() {
            list.focus();
        },
        close() {
            showMenu(false);
            node.removeEventListener('click', onClick);
            node.removeEventListener('keydown', onKey);
            chooser.removeEventListener('change', onChange);
            splitter?.close();
        },
    };
}
