// The border between two panes: dragged with the pointer or moved with the arrow keys, it sets the width of the
// pane beside it, or the height of the pane under it when the panes stand one above the other. The width is kept in the layout and found again on the next visit.

import { attr, el, size } from './dom.js';

/** How far one press of an arrow key moves the border, in px. */
export const KEY_STEP = 16;

/**
 * @param {Object} options
 * @param {Document} options.document
 * @param {Element} options.target the element that carries the width
 * @param {string} options.property the width's name, one of SIZES of dom.js
 * @param {string} options.name the width's name in the layout
 * @param {{get: Function, set: Function}} [options.layout]
 * @param {number} options.initial the width without a kept one
 * @param {number} options.min
 * @param {number} options.max
 * @param {boolean} [options.reverse] the pane is on the right of the border: moving left makes it wider
 * @param {boolean} [options.rows] the panes stand one above the other: the border moves up and down and sets a height
 * @param {string} options.label
 * @param {string} [options.className] a class of its own beside "splitter"
 * @param {() => number} [options.actual] the size the pane really has, when the layout may give it less than was
 *     set: a move starts from there, so the border follows at once
 * @returns {{node: Element, width: () => number, close: () => void}}
 */
export function createSplitter({ document, target, property, name, layout, initial, min, max, reverse = false, rows = false, label, className, actual }) {
    const node = el(document, 'div', {
        class: className ? `splitter ${className}` : 'splitter',
        role: 'separator',
        tabindex: 0,
        'aria-orientation': rows ? 'horizontal' : 'vertical',
        'aria-label': label,
        'aria-valuemin': min,
        'aria-valuemax': max,
    });
    let width = initial;
    let drag = null;
    const at = (event) => Number(rows ? event.clientY : event.clientX) || 0;
    const [backKey, forthKey] = rows ? ['ArrowUp', 'ArrowDown'] : ['ArrowLeft', 'ArrowRight'];

    function clamp(value) {
        return Math.min(max, Math.max(min, Math.round(value)));
    }

    function apply(value) {
        width = clamp(value);
        size(target, property, width);
        attr(node, 'aria-valuenow', width);
    }

    function keep() {
        layout?.set(name, width);
    }

    /** {@return the size a move starts from: the set one, or the smaller one the pane really has} */
    function start() {
        const real = Number(actual?.());
        return real > 0 && real < width ? clamp(real) : width;
    }

    const kept = layout?.get(name);
    apply(Number.isFinite(kept) ? kept : initial);

    const onDown = (event) => {
        drag = { x: at(event), width: start() };
        node.classList.add('dragging');
        if (typeof node.setPointerCapture === 'function' && event.pointerId !== undefined) {
            node.setPointerCapture(event.pointerId);
        }
        event.preventDefault?.();
    };
    const onMove = (event) => {
        if (drag) {
            const moved = at(event) - drag.x;
            apply(drag.width + (reverse ? -moved : moved));
        }
    };
    const onUp = () => {
        if (drag) {
            drag = null;
            node.classList.remove('dragging');
            keep();
        }
    };
    const onKey = (event) => {
        let next = null;
        if (event.key === backKey) {
            next = start() + (reverse ? KEY_STEP : -KEY_STEP);
        } else if (event.key === forthKey) {
            next = start() + (reverse ? -KEY_STEP : KEY_STEP);
        } else if (event.key === 'Home') {
            next = min;
        } else if (event.key === 'End') {
            next = max;
        }
        if (next !== null) {
            event.preventDefault();
            apply(next);
            keep();
        }
    };
    const listeners = [
        ['pointerdown', onDown],
        ['pointermove', onMove],
        ['pointerup', onUp],
        ['pointercancel', onUp],
        ['keydown', onKey],
    ];
    for (const [type, listener] of listeners) {
        node.addEventListener(type, listener);
    }

    return {
        node,
        width: () => width,
        close() {
            for (const [type, listener] of listeners) {
                node.removeEventListener(type, listener);
            }
        },
    };
}
