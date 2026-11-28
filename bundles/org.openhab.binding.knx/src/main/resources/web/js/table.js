// The list of a panel: a table whose headings sort (ascending, descending, then the order the rows came in), with a
// find box, one selected row moved with the arrow and page keys, and Enter or a double click to open it. Row nodes are
// kept by key, so a row that stays is not made again. A redraw that takes the focused row away hands the focus on.
// Rows can stand in groups under a heading row that folds them away. The right edge of a column heading can be
// dragged to set the column's width, which is kept per list.

import { add, attr, clear, el, icon, place, size } from './dom.js';
import { text } from './texts.js';

function lower(value) {
    return typeof value === 'string' ? value.toLowerCase() : typeof value === 'number' ? String(value) : '';
}

/** How many rows Page Up and Page Down move when the list's height is not known. */
export const PAGE_STEP = 10;
/** The limits of a column's width set by hand, and the width a drag starts from when the browser tells none. */
export const COLUMN_WIDTH = Object.freeze({ min: 32, max: 900, initial: 120 });

function compareValues(a, b) {
    if (typeof a === 'number' && typeof b === 'number') {
        return a - b;
    }
    return String(a ?? '').localeCompare(String(b ?? ''), undefined, { numeric: true, sensitivity: 'base' });
}

/** The rows a list draws at first, and adds with each "Show more". */
export const ROWS_CAP = 500;

/**
 * @param {Object} options
 * @param {Document} options.document
 * @param {string} options.label what the list holds, for screen readers
 * @param {Array<Object>} options.columns each {key, label, value(row), cell?(row), compare?(a, b), stamp?(row),
 *     find?, title?, className?}: value is what the column sorts and finds by; cell gives what the cell shows when
 *     that is more than the value as text; stamp names the state a cell was drawn for, so refresh() draws it again
 *     when it changed; find: false keeps the column out of the find box; title explains a short heading
 * @param {(row: Object) => *} options.keyOf what names a row; rows of one key share a node
 * @param {(row: Object) => *} [options.versionOf] a row whose version changed is made again
 * @param {(row: Object) => Element|null} [options.rowNode] a row node of the caller's own, for a row that is no
 *     record; null for the table's own
 * @param {(row: Object) => string|null} [options.rowClass]
 * @param {(row: Object|null) => void} [options.onSelect] the selected row changed by click or key
 * @param {(row: Object, event?: Event) => void} [options.onOpen] Enter or a double click on a row; the double click
 *     comes with its event
 * @param {(text: string) => void} [options.onFind] the find box changed, before the rows are filtered
 * @param {() => void} [options.onDraw] the rows were drawn
 * @param {string} [options.findLabel]
 * @param {(row: Object) => {key: string, label: string}|null} [options.groupOf] the group of a row: rows of one
 *     group stand together under a heading row, in the order the groups first come in the rows; a click on the
 *     heading folds its rows away
 * @param {{get: Function, set: Function}} [options.layout] keeps the column widths set by hand
 * @param {string} [options.name] the list's name in the layout; without it the widths last for this visit
 * @param {boolean} [options.multi] several rows can be selected: Ctrl and a click adds or takes one, Shift and a click
 *     or an arrow key takes all from the row the selection started at; picked() gives them
 * @param {number} [options.cap] how many rows a list without groups draws at first and adds with "Show more", which
 *     stands under it while more rows match; Infinity for a list that bounds its rows itself
 */
export function createTable({ document, label, columns: firstColumns, keyOf, versionOf, rowNode, rowClass, onSelect, onOpen, onFind, onDraw, findLabel, groupOf, layout, name, multi = false, cap = ROWS_CAP }) {
    const findField = el(document, 'input', {
        type: 'search',
        name: 'find',
        class: 'find',
        placeholder: findLabel ?? text('frame.find'),
        'aria-label': findLabel ?? text('frame.find'),
        autocomplete: 'off',
    });
    let columns = firstColumns;
    const headings = new Map();
    const buttons = new Map();
    const handles = new Map();
    const head = el(document, 'tr');
    const widthsName = name ? `columns.${name}` : null;
    const keptWidths = widthsName ? layout?.get(widthsName) : null;
    const widths = new Map(keptWidths !== null && typeof keptWidths === 'object' ? Object.entries(keptWidths).filter(([, px]) => Number.isFinite(px)) : []);
    let drag = null;

    function sizeCell(cell, key) {
        const px = widths.get(key);
        cell.classList.toggle('sized', px !== undefined);
        size(cell, '--col-width', px ?? null);
    }

    function drawHead() {
        clear(head);
        headings.clear();
        buttons.clear();
        handles.clear();
        for (const column of columns) {
            const button = el(document, 'button', { type: 'button', class: 'sort', title: column.title ?? null }, column.label);
            const handle = el(document, 'span', { class: 'col-resize', role: 'separator', 'aria-orientation': 'vertical', 'aria-label': text('frame.columnWidth', column.label) });
            const heading = el(document, 'th', { scope: 'col', class: column.className ?? null }, button, handle);
            headings.set(column.key, heading);
            buttons.set(button, column);
            handles.set(handle, column);
            sizeCell(heading, column.key);
            add(document, head, heading);
        }
    }

    /** Gives the column this width, or its own again with null, in its heading and in every row that is made. */
    function setWidth(key, px) {
        if (px === null) {
            widths.delete(key);
        } else {
            widths.set(key, Math.min(COLUMN_WIDTH.max, Math.max(COLUMN_WIDTH.min, Math.round(px))));
        }
        const at = columns.findIndex((column) => column.key === key);
        if (at < 0) {
            return;
        }
        sizeCell(headings.get(key), key);
        for (const line of lines.values()) {
            if (line.cells) {
                sizeCell(line.cells[at], key);
            }
        }
    }

    function keepWidths() {
        if (widthsName) {
            layout?.set(widthsName, Object.fromEntries(widths));
        }
    }

    function markSort() {
        for (const [key, heading] of headings) {
            attr(heading, 'aria-sort', sort !== null && key === sort.key ? (sort.descending ? 'descending' : 'ascending') : null);
        }
    }

    const body = el(document, 'tbody');
    const table = el(document, 'table', { class: 'grid', role: 'grid', 'aria-label': label }, el(document, 'thead', {}, head), body);
    const empty = el(document, 'p', { class: 'empty note' });
    const more = el(document, 'button', { type: 'button', class: 'more' }, text('frame.more'));
    const node = el(document, 'div', { class: 'grid-box' }, table);

    let rows = [];
    let shown = [];
    let find = '';
    let sort = null;
    let limit = cap;
    let matched = 0;
    let order = [];
    let selectedKey;
    let hasSelection = false;
    let picked = new Set();
    let anchorKey;
    let lines = new Map();
    let byNode = new Map();
    let texts = { none: '', noMatch: '' };

    function cellOf(column, row) {
        const cell = el(document, 'td', { class: column.className ?? null });
        if (widths.has(column.key)) {
            sizeCell(cell, column.key);
        }
        fill(cell, column, row);
        return cell;
    }

    function fill(cell, column, row) {
        const content = column.cell ? column.cell(row) : column.value(row);
        add(document, cell, typeof content === 'number' ? String(content) : (content ?? null));
    }

    /** {@return the line of a row with a node of the caller's own, null for a record} */
    function ownLine(row) {
        const key = keyOf(row);
        const version = versionOf ? versionOf(row) : row;
        const line = lines.get(key);
        if (line && line.version === version) {
            line.row = row;
            return line.cells ? null : line;
        }
        const own = rowNode(row);
        return own ? { key, row, version, node: own, cells: null, stamps: null } : null;
    }

    function lineOf(row) {
        const key = keyOf(row);
        const version = versionOf ? versionOf(row) : row;
        let line = lines.get(key);
        if (line && line.version === version) {
            line.row = row;
            return line;
        }
        const cells = columns.map((column) => cellOf(column, row));
        const selected = multi ? picked.has(key) : hasSelection && key === selectedKey;
        line = {
            key,
            row,
            version,
            cells,
            stamps: columns.map((column) => (column.stamp ? column.stamp(row) : undefined)),
            node: el(document, 'tr', { class: rowClass ? rowClass(row) : null, tabindex: -1, 'aria-selected': selected ? 'true' : 'false' }, cells),
        };
        if (selected) {
            line.node.classList.add('selected');
        }
        return line;
    }

    const folded = new Set();
    const groupLines = new Map();
    const foldButtons = new Map();

    /** {@return the heading row of a group, made once and brought up to date} */
    function headingOf(group, count, open) {
        let line = groupLines.get(group.key);
        if (!line) {
            const labelNode = el(document, 'span', { class: 'label' });
            const countNode = el(document, 'span', { class: 'count' });
            const button = el(document, 'button', { type: 'button', class: 'fold' }, icon(document, 'chevron'), labelNode, countNode);
            const cell = el(document, 'td', {}, button);
            line = { button, labelNode, countNode, cell, node: el(document, 'tr', { class: 'group-row' }, cell) };
            groupLines.set(group.key, line);
            foldButtons.set(button, group.key);
        }
        attr(line.cell, 'colspan', columns.length);
        attr(line.button, 'aria-expanded', open ? 'true' : 'false');
        attr(line.button, 'title', text('frame.fold', group.label));
        clear(line.labelNode);
        add(document, line.labelNode, group.label);
        clear(line.countNode);
        add(document, line.countNode, String(count));
        return line.node;
    }

    function matches(row) {
        if (find === '') {
            return true;
        }
        return columns.some((column) => column.find !== false && lower(column.value(row)).includes(find));
    }

    // Only records are sorted; a row with a node of the caller's own has no column values and is left out meanwhile,
    // unless such rows are all there is.
    function ordered(list, own) {
        if (sort === null) {
            return list;
        }
        const records = list.filter((row) => !own.has(row));
        if (records.length === 0) {
            return list;
        }
        const column = columns.find((candidate) => candidate.key === sort.key);
        const compare = column.compare ?? ((a, b) => compareValues(column.value(a), column.value(b)));
        records.sort(compare);
        return sort.descending ? records.reverse() : records;
    }

    function focusLine() {
        const selected = hasSelection ? lines.get(selectedKey) : null;
        if (selected?.cells) {
            return selected;
        }
        for (const line of byNode.values()) {
            if (line.cells) {
                return line;
            }
        }
        return null;
    }

    function draw() {
        const focused = lineOfTarget(document.activeElement);
        const kept = rows.filter(matches);
        const next = new Map();
        const own = new Map();
        if (rowNode) {
            for (const row of kept) {
                const line = ownLine(row);
                if (line) {
                    own.set(row, line);
                    // kept also while a sort leaves it out, so it is not made again each time
                    next.set(line.key, line);
                }
            }
        }
        const all = ordered(kept, own);
        matched = all.length;
        byNode = new Map();
        const lineNode = (row) => {
            const line = own.get(row) ?? lineOf(row);
            next.set(line.key, line);
            byNode.set(line.node, line);
            return line.node;
        };
        let nodes;
        if (groupOf) {
            // the groups in the order the rows came in, each holding its rows in the order of the sort
            const groups = new Map();
            for (const row of kept) {
                const group = groupOf(row);
                if (group && !groups.has(group.key)) {
                    groups.set(group.key, { group, rows: [] });
                }
            }
            const loose = [];
            for (const row of all) {
                const group = groupOf(row);
                (group ? groups.get(group.key).rows : loose).push(row);
            }
            shown = [...loose];
            nodes = loose.map(lineNode);
            for (const { group, rows: members } of groups.values()) {
                const open = !folded.has(group.key);
                nodes.push(headingOf(group, members.length, open));
                if (open) {
                    shown.push(...members);
                    nodes.push(...members.map(lineNode));
                }
            }
            for (const key of [...groupLines.keys()]) {
                if (!groups.has(key)) {
                    groupLines.delete(key);
                }
            }
        } else {
            shown = all.length > limit ? all.slice(0, limit) : all;
            nodes = shown.map(lineNode);
        }
        order = all;
        lines = next;
        place(body, nodes);
        clear(empty);
        const none = all.length === 0;
        if (none) {
            add(document, empty, rows.length === 0 ? texts.none : texts.noMatch);
        }
        const moreHadFocus = document.activeElement === more;
        const capped = !groupOf && all.length > shown.length;
        place(node, [table, none && (texts.none !== '' || rows.length > 0) ? empty : null, capped ? more : null].filter(Boolean));
        const focusable = focusLine();
        // the last batch takes the button away from under the focus
        if (moreHadFocus && !capped) {
            focusable?.node.focus();
        }
        for (const line of byNode.values()) {
            if (line.cells) {
                attr(line.node, 'tabindex', line === focusable ? 0 : -1);
            }
        }
        // a focused row that was moved or taken away leaves the focus on nothing
        if (focused && !body.contains(document.activeElement)) {
            const line = byNode.get(lines.get(focused.key)?.node);
            (line?.cells ? line : focusable)?.node.focus();
        }
        onDraw?.();
    }

    function setSelected(key, on) {
        const line = lines.get(key);
        if (line?.cells) {
            attr(line.node, 'aria-selected', on ? 'true' : 'false');
            line.node.classList.toggle('selected', on);
            attr(line.node, 'tabindex', on ? 0 : -1);
        }
    }

    /** The selection of a list that takes several rows: the lead row carries the focus, picked holds them all. */
    function pickRow(row, { notify, focus, mode }) {
        const before = picked;
        const key = row === null || row === undefined ? undefined : keyOf(row);
        const keys = shown.filter((candidate) => lines.get(keyOf(candidate))?.cells).map(keyOf);
        const from = keys.indexOf(anchorKey);
        const to = keys.indexOf(key);
        if (key === undefined) {
            picked = new Set();
            anchorKey = undefined;
        } else if (mode === 'toggle' && picked.has(key)) {
            picked = new Set(before);
            picked.delete(key);
            anchorKey = undefined;
        } else if (mode === 'toggle') {
            picked = new Set(before).add(key);
            anchorKey = key;
        } else if (mode === 'range' && from >= 0 && to >= 0) {
            picked = new Set(keys.slice(Math.min(from, to), Math.max(from, to) + 1));
        } else {
            picked = new Set([key]);
            anchorKey = key;
        }
        const lead = key !== undefined && picked.has(key) ? key : [...picked].pop();
        anchorKey ??= lead;
        hasSelection = lead !== undefined;
        selectedKey = lead;
        for (const line of lines.values()) {
            if (line.cells) {
                const on = picked.has(line.key);
                attr(line.node, 'aria-selected', on ? 'true' : 'false');
                line.node.classList.toggle('selected', on);
                attr(line.node, 'tabindex', line.key === lead ? 0 : -1);
            }
        }
        if (focus && hasSelection) {
            lines.get(selectedKey)?.node.focus();
        }
        if (notify) {
            onSelect?.(hasSelection ? selectedRow() : null);
        }
    }

    function selectRow(row, { notify, focus, mode }) {
        if (multi) {
            pickRow(row, { notify, focus, mode });
            return;
        }
        if (hasSelection) {
            setSelected(selectedKey, false);
        }
        hasSelection = row !== null && row !== undefined;
        selectedKey = hasSelection ? keyOf(row) : undefined;
        if (hasSelection) {
            for (const line of lines.values()) {
                if (line.cells && line.key !== selectedKey) {
                    attr(line.node, 'tabindex', -1);
                }
            }
            setSelected(selectedKey, true);
            if (focus) {
                lines.get(selectedKey)?.node.focus();
            }
        }
        if (notify) {
            onSelect?.(hasSelection ? row : null);
        }
    }

    function lineOfTarget(target) {
        for (let at = target; at && at !== body; at = at.parentNode) {
            const line = byNode.get(at);
            if (line) {
                return line.cells ? line : null;
            }
        }
        return null;
    }

    function selectedRow() {
        return hasSelection ? (lines.get(selectedKey)?.row ?? rows.find((row) => keyOf(row) === selectedKey) ?? null) : null;
    }

    /** {@return how many rows fit the list beside its heading, when the browser tells the heights} */
    function pageStep() {
        const height = Number(focusLine()?.node.offsetHeight);
        const room = Number(node.parentNode?.clientHeight);
        return height > 0 && room >= height * 3 ? Math.floor(room / height) - 1 : PAGE_STEP;
    }

    function foldOf(target) {
        for (let at = target; at && at !== body; at = at.parentNode) {
            if (foldButtons.has(at)) {
                return at;
            }
        }
        return null;
    }

    const onClick = (event) => {
        const fold = foldOf(event.target);
        if (fold) {
            const key = foldButtons.get(fold);
            if (!folded.delete(key)) {
                folded.add(key);
            }
            draw();
            return;
        }
        const column = buttons.get(event.target);
        if (column) {
            if (sort === null || sort.key !== column.key) {
                sort = { key: column.key, descending: false };
            } else {
                sort = sort.descending ? null : { key: column.key, descending: true };
            }
            markSort();
            draw();
            return;
        }
        const line = lineOfTarget(event.target);
        if (line) {
            selectRow(line.row, { notify: true, focus: false, mode: event.ctrlKey || event.metaKey ? 'toggle' : event.shiftKey ? 'range' : undefined });
        }
    };
    const onDoubleClick = (event) => {
        const line = lineOfTarget(event.target);
        if (line) {
            onOpen?.(line.row, event);
        }
    };
    const onKey = (event) => {
        const records = shown.filter((row) => lines.get(keyOf(row))?.cells);
        if (records.length === 0) {
            return;
        }
        const line = lineOfTarget(event.target);
        const from = line ? records.indexOf(line.row) : hasSelection ? records.findIndex((row) => keyOf(row) === selectedKey) : -1;
        let to = null;
        switch (event.key) {
            case 'ArrowDown':
                to = Math.min(from + 1, records.length - 1);
                break;
            case 'ArrowUp':
                to = Math.max(from - 1, 0);
                break;
            case 'PageDown':
                to = Math.min(Math.max(from, 0) + pageStep(), records.length - 1);
                break;
            case 'PageUp':
                to = Math.max(from - pageStep(), 0);
                break;
            case 'Home':
                to = 0;
                break;
            case 'End':
                to = records.length - 1;
                break;
            case 'Enter':
                // on a group heading Enter is the button's own: it folds
                if (from >= 0 && foldOf(event.target) === null) {
                    event.preventDefault();
                    selectRow(records[from], { notify: !hasSelection || keyOf(records[from]) !== selectedKey, focus: false });
                    onOpen?.(records[from]);
                }
                return;
            default:
                return;
        }
        event.preventDefault();
        selectRow(records[to], { notify: true, focus: true, mode: event.shiftKey ? 'range' : undefined });
    };
    const onInput = () => {
        find = findField.value.toLowerCase().trim();
        limit = cap;
        onFind?.(find);
        draw();
    };
    const onMore = () => {
        limit += cap;
        draw();
    };
    const onDown = (event) => {
        const column = handles.get(event.target);
        if (!column) {
            return;
        }
        const heading = headings.get(column.key);
        const from = widths.get(column.key) ?? (Number(heading.offsetWidth) > 0 ? Number(heading.offsetWidth) : COLUMN_WIDTH.initial);
        drag = { key: column.key, x: Number(event.clientX) || 0, from, handle: event.target };
        if (typeof event.target.setPointerCapture === 'function' && event.pointerId !== undefined) {
            event.target.setPointerCapture(event.pointerId);
        }
        event.preventDefault?.();
    };
    const onMove = (event) => {
        if (drag) {
            setWidth(drag.key, drag.from + (Number(event.clientX) || 0) - drag.x);
        }
    };
    const onUp = () => {
        if (drag) {
            drag = null;
            keepWidths();
        }
    };
    // a double click on the edge gives the column its own width again
    const onEdgeDouble = (event) => {
        const column = handles.get(event.target);
        if (column) {
            setWidth(column.key, null);
            keepWidths();
        }
    };

    drawHead();
    table.addEventListener('click', onClick);
    body.addEventListener('dblclick', onDoubleClick);
    body.addEventListener('keydown', onKey);
    findField.addEventListener('input', onInput);
    more.addEventListener('click', onMore);
    const headListeners = [
        ['pointerdown', onDown],
        ['pointermove', onMove],
        ['pointerup', onUp],
        ['pointercancel', onUp],
        ['dblclick', onEdgeDouble],
    ];
    for (const [type, listener] of headListeners) {
        head.addEventListener(type, listener);
    }

    return {
        node,
        findField,
        /**
         * Shows these rows, in this order until a heading sorts them.
         *
         * @param {{none?: string, noMatch?: string}} [words] what to say for no rows at all and for none found
         */
        setRows(list, words) {
            rows = Array.isArray(list) ? list : [];
            if (words) {
                texts = { none: words.none ?? '', noMatch: words.noMatch ?? '' };
            }
            draw();
        },
        /** Shows other columns; the rows are made again, and a sort by a column that left is dropped. */
        setColumns(list) {
            columns = list;
            if (sort !== null && !columns.some((column) => column.key === sort.key)) {
                sort = null;
            }
            drawHead();
            markSort();
            lines = new Map();
            draw();
        },
        /**
         * Draws at most this many rows until the find box changes or "Show more" adds to them; matched() tells how
         * many there would be.
         *
         * @param {boolean} [now] false when the caller sets the rows next anyway
         */
        setLimit(count, now = true) {
            limit = count;
            if (now) {
                draw();
            }
        },
        /** {@return how many rows pass the find box, drawn or not} */
        matched() {
            return matched;
        },
        /** Puts the keyboard focus on the selected row, else on the first. {@return false when the list has no row} */
        focus() {
            const line = focusLine();
            line?.node.focus();
            return Boolean(line);
        },
        /** Draws again the cells whose column has a stamp that changed. */
        refresh() {
            for (const line of lines.values()) {
                if (!line.cells) {
                    continue;
                }
                columns.forEach((column, at) => {
                    if (!column.stamp) {
                        return;
                    }
                    const stamp = column.stamp(line.row);
                    if (stamp !== line.stamps[at]) {
                        line.stamps[at] = stamp;
                        clear(line.cells[at]);
                        fill(line.cells[at], column, line.row);
                    }
                });
            }
        },
        /** Selects the row of this key without telling onSelect; undefined or an unknown key selects none. */
        select(key) {
            const row = rows.find((candidate) => keyOf(candidate) === key) ?? null;
            // a row below the rows that are drawn is to be seen
            const at = row === null || groupOf ? -1 : order.indexOf(row);
            if (at >= limit) {
                limit = Math.ceil((at + 1) / cap) * cap;
                draw();
            }
            selectRow(row, { notify: false, focus: false });
            return row;
        },
        selected: selectedRow,
        /** {@return every selected row of a list that takes several, in the order of the rows} */
        picked() {
            return multi ? rows.filter((row) => picked.has(keyOf(row))) : [selectedRow()].filter((row) => row !== null);
        },
        /** {@return the text of the find box, trimmed and in small letters} */
        find() {
            return find;
        },
        /** {@return the sorted column and direction, or null} */
        sorted() {
            return sort === null ? null : { ...sort };
        },
        /** {@return how many rows are shown and how many there are} */
        counts() {
            return { shown: shown.length, total: rows.length };
        },
        /** {@return the rows as they are shown} */
        shown() {
            return shown;
        },
        close() {
            table.removeEventListener('click', onClick);
            body.removeEventListener('dblclick', onDoubleClick);
            body.removeEventListener('keydown', onKey);
            findField.removeEventListener('input', onInput);
            more.removeEventListener('click', onMore);
            for (const [type, listener] of headListeners) {
                head.removeEventListener(type, listener);
            }
        },
        /** {@return the keys of the groups that are folded away} */
        folded() {
            return [...folded];
        },
        /**
         * Folds exactly the groups of these keys away.
         *
         * @param {boolean} [now] false when the caller sets the rows next anyway
         */
        setFolded(keys, now = true) {
            folded.clear();
            for (const key of keys) {
                folded.add(key);
            }
            if (now) {
                draw();
            }
        },
        /** {@return the widths set by hand, by column key} */
        widths() {
            return Object.fromEntries(widths);
        },
    };
}
