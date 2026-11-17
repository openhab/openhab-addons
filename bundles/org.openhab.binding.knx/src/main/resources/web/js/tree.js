// The tree of a panel: nodes that open and close (by their arrow, a double click or the arrow keys), one selected
// node, moved through with the arrow keys. A node's children are put into the document when it is first opened. No
// id or selector is made from a node's key.

import { add, attr, clear, el, icon } from './dom.js';

/**
 * @param {Object} options
 * @param {Document} options.document
 * @param {string} options.label what the tree holds, for screen readers
 * @param {(node: Object) => void} [options.onSelect] a node was chosen by click or Enter; a node is {key, name,
 *     children?, icon?, iconTitle? (what the drawing stands for), address?, mark?, className? (a fixed class of the
 *     page for its row), badge? ({icon, label, className}: a small state drawing after the name)}
 * @param {(node: Object) => boolean} [options.startsOpen] whether a node is open when nothing else is known of it
 * @param {{read: () => ({open: Array<string>, closed: Array<string>}|null), write: Function}|null} [options.memory]
 *     keeps what the viewer opened and closed, see treeMemory
 * @returns {{node: Element, setRoots: Function, select: Function, selected: Function, open: Function, close: Function}}
 */
/** The rows a tree shows at most after "Expand all"; the viewer opens further nodes one by one. */
export const EXPAND_ROWS = 2000;

export function createTree({ document, label, onSelect, startsOpen = () => false, memory = null }) {
    const root = el(document, 'ul', { class: 'tree', role: 'tree', 'aria-label': label });
    const entries = new Map();
    const byItem = new Map();
    let roots = [];
    let selected = null;
    let active = null;

    function index(nodes, parent, depth) {
        return nodes.map((data) => {
            const entry = { data, parent, depth, open: false, item: null, group: null, children: [] };
            entries.set(data.key, entry);
            entry.children = index(Array.isArray(data.children) ? data.children : [], entry, depth + 1);
            return entry;
        });
    }

    function itemOf(entry) {
        if (entry.item) {
            return entry.item;
        }
        const data = entry.data;
        const parent = entry.children.length > 0;
        entry.item = el(
            document,
            'li',
            { class: data.className ?? null, role: 'treeitem', 'aria-level': entry.depth, 'aria-expanded': parent ? 'false' : null, 'aria-selected': 'false', tabindex: -1 },
            el(
                document,
                'div',
                { class: 'node' },
                el(document, 'span', { class: parent ? 'twisty' : 'twisty none' }, parent ? icon(document, 'chevron') : null),
                data.icon ? (data.iconTitle ? el(document, 'span', { class: 'kind-icon', role: 'img', title: data.iconTitle, 'aria-label': data.iconTitle }, icon(document, data.icon)) : icon(document, data.icon)) : null,
                data.address ? el(document, 'span', { class: 'address' }, String(data.address)) : null,
                el(document, 'span', { class: 'name' }, String(data.name ?? '')),
                data.mark ? el(document, 'span', { class: 'kind' }, String(data.mark)) : null,
                data.badge ? el(document, 'span', { class: `badge ${data.badge.className}`, role: 'img', title: data.badge.label, 'aria-label': data.badge.label }, icon(document, data.badge.icon)) : null,
            ),
        );
        byItem.set(entry.item, entry);
        return entry.item;
    }

    function setOpen(entry, open) {
        if (entry.children.length === 0 || entry.open === open) {
            return;
        }
        entry.open = open;
        const item = itemOf(entry);
        attr(item, 'aria-expanded', open ? 'true' : 'false');
        if (open && entry.group === null) {
            entry.group = el(document, 'ul', { role: 'group' }, entry.children.map(itemOf));
        }
        if (open) {
            add(document, item, entry.group);
        } else {
            entry.group.remove();
        }
    }

    /** Keeps the nodes the viewer has other than they start. */
    function remember() {
        if (!memory) {
            return;
        }
        const open = [];
        const closed = [];
        for (const entry of entries.values()) {
            if (entry.children.length > 0 && entry.open !== (startsOpen(entry.data) === true)) {
                (entry.open ? open : closed).push(entry.data.key);
            }
        }
        memory.write({ open, closed });
    }

    function toggle(entry, open) {
        setOpen(entry, open);
        remember();
    }

    /**
     * Opens the tree level by level, the outer nodes first, as long as it then shows at most EXPAND_ROWS rows: a
     * node whose children would go beyond stays closed. {@return whether every node is open}
     */
    function openWithin() {
        let rows = roots.length;
        let complete = true;
        for (let level = roots; level.length > 0; ) {
            const next = [];
            for (const entry of level) {
                if (entry.children.length === 0) {
                    continue;
                }
                if (!entry.open && rows + entry.children.length > EXPAND_ROWS) {
                    complete = false;
                    continue;
                }
                setOpen(entry, true);
                rows += entry.children.length;
                next.push(...entry.children);
            }
            level = next;
        }
        return complete;
    }

    function setAll(open) {
        let complete = true;
        if (open) {
            complete = openWithin();
        } else {
            for (const entry of entries.values()) {
                setOpen(entry, false);
            }
        }
        if (!open && roots.length > 0) {
            // the node that held the keyboard may be out of sight now
            let top = active ?? roots[0];
            while (top.parent) {
                top = top.parent;
            }
            setActive(top, root.contains(document.activeElement));
        }
        remember();
        return complete;
    }

    function reveal(entry) {
        for (let at = entry.parent; at; at = at.parent) {
            setOpen(at, true);
        }
    }

    function visible() {
        const list = [];
        const visit = (entry) => {
            list.push(entry);
            if (entry.open) {
                entry.children.forEach(visit);
            }
        };
        roots.forEach(visit);
        return list;
    }

    function setActive(entry, focus) {
        if (active && active.item) {
            attr(active.item, 'tabindex', -1);
        }
        active = entry;
        if (entry) {
            reveal(entry);
            attr(itemOf(entry), 'tabindex', 0);
            if (focus) {
                entry.item.focus();
            }
        }
    }

    function mark(entry) {
        if (selected && selected.item) {
            attr(selected.item, 'aria-selected', 'false');
            selected.item.classList.remove('selected');
        }
        selected = entry;
        if (entry) {
            reveal(entry);
            attr(itemOf(entry), 'aria-selected', 'true');
            entry.item.classList.add('selected');
        }
    }

    function choose(entry, focus) {
        mark(entry);
        setActive(entry, focus);
        onSelect?.(entry.data);
    }

    function entryOf(target) {
        for (let at = target; at && at !== root; at = at.parentNode) {
            if (byItem.has(at)) {
                return byItem.get(at);
            }
        }
        return null;
    }

    function inTwisty(target, entry) {
        for (let at = target; at && at !== entry.item; at = at.parentNode) {
            if (at.classList?.contains('twisty')) {
                return true;
            }
        }
        return false;
    }

    const onClick = (event) => {
        const entry = entryOf(event.target);
        if (!entry) {
            return;
        }
        if (inTwisty(event.target, entry)) {
            toggle(entry, !entry.open);
            setActive(entry, false);
        } else {
            choose(entry, false);
        }
    };

    const onDoubleClick = (event) => {
        const entry = entryOf(event.target);
        if (entry && !inTwisty(event.target, entry)) {
            toggle(entry, !entry.open);
        }
    };

    function step(entry, key) {
        const shown = visible();
        const at = shown.indexOf(entry);
        switch (key) {
            case 'ArrowDown':
                return shown[Math.min(at + 1, shown.length - 1)];
            case 'ArrowUp':
                return shown[Math.max(at - 1, 0)];
            case 'Home':
                return shown[0];
            case 'End':
                return shown[shown.length - 1];
            case 'ArrowRight':
                if (entry.children.length > 0 && !entry.open) {
                    toggle(entry, true);
                    return entry;
                }
                return entry.children[0] ?? entry;
            case 'ArrowLeft':
                if (entry.open) {
                    toggle(entry, false);
                    return entry;
                }
                return entry.parent ?? entry;
            default:
                return null;
        }
    }

    const onKey = (event) => {
        const entry = entryOf(event.target) ?? active;
        if (!entry) {
            return;
        }
        if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            choose(entry, true);
            return;
        }
        const next = step(entry, event.key);
        if (next) {
            event.preventDefault();
            setActive(next, true);
        }
    };

    root.addEventListener('click', onClick);
    root.addEventListener('dblclick', onDoubleClick);
    root.addEventListener('keydown', onKey);

    return {
        node: root,
        /**
         * Shows these nodes; a node with the key selected before stays selected. A node that was here before stays
         * open or closed; a new one is as the viewer left it on an earlier visit, else as it starts.
         */
        setRoots(nodes) {
            const was = new Map([...entries.values()].filter((entry) => entry.children.length > 0).map((entry) => [entry.data.key, entry.open]));
            const stored = was.size === 0 ? memory?.read() : null;
            const keptOpen = new Set(stored?.open ?? []);
            const keptClosed = new Set(stored?.closed ?? []);
            const kept = selected?.data.key;
            entries.clear();
            byItem.clear();
            selected = null;
            active = null;
            clear(root);
            roots = index(Array.isArray(nodes) ? nodes : [], null, 1);
            add(document, root, roots.map(itemOf));
            for (const entry of entries.values()) {
                const key = entry.data.key;
                if (was.has(key) ? was.get(key) : keptOpen.has(key) || (!keptClosed.has(key) && startsOpen(entry.data) === true)) {
                    setOpen(entry, true);
                }
            }
            if (kept !== undefined && entries.has(kept)) {
                mark(entries.get(kept));
            }
            setActive(selected ?? roots[0] ?? null, false);
        },
        /**
         * Marks the node of this key as selected and opens the way to it, without telling onSelect.
         *
         * @returns {Object|null} the node, null when the tree has none of this key
         */
        select(key) {
            const entry = entries.get(key) ?? null;
            mark(entry);
            if (entry) {
                setActive(entry, false);
            }
            return entry ? entry.data : null;
        },
        selected() {
            return selected ? selected.data : null;
        },
        /** Opens or closes the node of this key. */
        open(key, open = true) {
            const entry = entries.get(key);
            if (entry) {
                if (open) {
                    reveal(entry);
                }
                setOpen(entry, open);
            }
        },
        /** Opens every node, as far as the tree then stays within EXPAND_ROWS rows. {@return whether all are open} */
        expandAll() {
            return setAll(true);
        },
        collapseAll() {
            setAll(false);
        },
        /** Puts the keyboard focus on the tree's current node. {@return false when the tree is empty} */
        focus() {
            const entry = active ?? selected ?? roots[0] ?? null;
            if (entry) {
                itemOf(entry).focus();
            }
            return entry !== null;
        },
        /** {@return the node of this key, or null} */
        find(key) {
            return entries.get(key)?.data ?? null;
        },
        /** {@return the nodes from the outermost one down to the node of this key; none for an unknown key} */
        path(key) {
            const found = [];
            for (let entry = entries.get(key); entry; entry = entry.parent) {
                found.unshift(entry.data);
            }
            return found;
        },
        close() {
            root.removeEventListener('click', onClick);
            root.removeEventListener('dblclick', onDoubleClick);
            root.removeEventListener('keydown', onKey);
        },
    };
}

/** How many trees, and how many nodes of each, are kept. */
export const KEPT_TREES = 12;
export const KEPT_NODES = 400;

function strings(value) {
    return Array.isArray(value) ? value.filter((key) => typeof key === 'string') : [];
}

/**
 * What a viewer opened and closed in one tree, kept with the page's layout for the next visit.
 *
 * @param {{get: Function, set: Function}} [layout]
 * @param {string} name names the tree: its panel and bridge
 * @returns {{read: Function, write: Function}|null} null without a layout
 */
export function treeMemory(layout, name) {
    if (!layout) {
        return null;
    }
    const all = () => {
        const kept = layout.get('trees');
        return typeof kept === 'object' && kept !== null && !Array.isArray(kept) ? kept : {};
    };
    return {
        read() {
            const kept = all();
            const mine = Object.hasOwn(kept, name) ? kept[name] : null;
            return typeof mine === 'object' && mine !== null ? { open: strings(mine.open), closed: strings(mine.closed) } : null;
        },
        write(state) {
            // the tree written last stands last; the oldest ones go
            const others = Object.entries(all()).filter(([key]) => key !== name);
            others.push([name, { open: strings(state.open).slice(0, KEPT_NODES), closed: strings(state.closed).slice(0, KEPT_NODES) }]);
            layout.set('trees', Object.fromEntries(others.slice(-KEPT_TREES)));
        },
    };
}
