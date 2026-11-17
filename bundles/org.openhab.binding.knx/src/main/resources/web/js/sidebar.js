// The sidebar at the right of the workplace: containers stacked under each other, each with a heading that opens and
// closes it. Several can be open; which are open, and whether the sidebar itself is open, is kept for the viewer.
// Closed, the sidebar is a narrow strip of the headings' drawings. A panel fills its own face of the sidebar; the
// sidebar shows one face at a time.

import { add, attr, clear, el, icon, place } from './dom.js';
import { createLive } from './live.js';
import { createProperties, factList } from './properties.js';
import { text } from './texts.js';

/** The containers in their order: name, drawing, text key of the heading, open at first. */
export const CONTAINERS = Object.freeze([
    ['properties', 'panel', 'frame.properties', true],
    ['openhab', 'link', 'frame.openhab', true],
    ['live', 'pulse', 'frame.live', false],
    ['find', 'search', 'frame.findProject', false],
]);

/**
 * @param {Object} options
 * @param {Document} options.document
 * @param {{get: Function, set: Function}} [options.layout]
 * @param [options.rows] the row model, for Live
 * @param {Function} [options.frame] bundles Live's drawing
 * @param {(open: boolean) => void} [options.onOpen] the sidebar was opened or closed
 * @param {Array<Array>} [options.containers] other containers than CONTAINERS, in the same shape; such a sidebar has
 *     no faces: its owner fills body(name)
 * @returns {{node: Element, face: Function, show: Function, release: Function, body: Function, isOpen: Function, close: Function}}
 */
export function createSidebar({ document, layout, rows, frame, onOpen, containers: wanted = CONTAINERS }) {
    const toggle = el(document, 'button', { type: 'button', class: 'tool sidebar-toggle' }, icon(document, 'chevron'));
    const node = el(document, 'aside', { class: 'pane sidebar', 'aria-label': text('frame.sidebar') }, el(document, 'div', { class: 'sidebar-head' }, toggle));
    const containers = new Map();
    const faces = [];
    let open = true;

    for (const [name, drawing, key, first] of wanted) {
        const head = el(document, 'button', { type: 'button', class: 'container-head', title: text(key) }, icon(document, drawing), el(document, 'span', { class: 'label' }, text(key)));
        const body = el(document, 'div', { class: 'container-body' });
        const box = el(document, 'section', { class: `container container-${name}` }, el(document, 'h3', {}, head), body);
        const kept = layout?.get(`sidebar.${name}`);
        containers.set(name, { head, body, box, open: typeof kept === 'boolean' ? kept : first });
        add(document, node, box);
    }

    function drawContainer(name) {
        const container = containers.get(name);
        container.box.classList.toggle('closed', !container.open);
        attr(container.head, 'aria-expanded', container.open ? 'true' : 'false');
    }

    function setOpen(next) {
        open = next;
        node.classList.toggle('closed', !open);
        attr(toggle, 'aria-expanded', open ? 'true' : 'false');
        attr(toggle, 'title', text(open ? 'frame.hideSidebar' : 'frame.showSidebar'));
        attr(toggle, 'aria-label', text(open ? 'frame.hideSidebar' : 'frame.showSidebar'));
        onOpen?.(open);
    }

    const onClick = (event) => {
        let target = event.target;
        while (target && target !== node && target.tagName !== 'BUTTON') {
            target = target.parentNode;
        }
        if (target === toggle) {
            setOpen(!open);
            layout?.set('propertiesOpen', open);
            return;
        }
        for (const [name, container] of containers) {
            if (target === container.head) {
                // on the closed strip a drawing opens the sidebar with its container
                container.open = open ? !container.open : true;
                drawContainer(name);
                layout?.set(`sidebar.${name}`, container.open);
                if (!open) {
                    setOpen(true);
                    layout?.set('propertiesOpen', true);
                }
            }
        }
    };
    node.addEventListener('click', onClick);

    for (const [name] of wanted) {
        drawContainer(name);
    }
    setOpen(layout?.get('propertiesOpen') !== false);

    function show(face) {
        place(containers.get('properties').body, [face.properties.node]);
        place(containers.get('openhab').body, [face.openhab.node]);
        place(containers.get('live').body, [face.live.node]);
    }

    return {
        node,
        /** {@return a face of the sidebar for one panel: its own Properties, openHAB and Live} */
        face() {
            const properties = createProperties(document, layout);
            const live = createLive({ document, rows, frame });
            const openhabNode = el(document, 'div', { class: 'openhab' });
            const openhab = {
                node: openhabNode,
                empty() {
                    clear(openhabNode);
                    add(document, openhabNode, el(document, 'p', { class: 'note' }, text('frame.noSelection')));
                },
                /**
                 * @param {Array<[string, *]>} facts label and value rows
                 * @param {Array<Element|null>} [more] what stands under them
                 */
                show(facts, more = []) {
                    clear(openhabNode);
                    add(document, openhabNode, factList(document, facts), more.filter(Boolean));
                    if (openhabNode.firstChild === null) {
                        openhab.empty();
                    }
                },
            };
            openhab.empty();
            const face = { properties, openhab, live };
            faces.push(face);
            if (faces.length === 1) {
                show(face);
            }
            return face;
        },
        show,
        /** A panel that is gone gives its face back. */
        release(face) {
            const at = faces.indexOf(face);
            if (at >= 0) {
                faces.splice(at, 1);
                face.properties.close();
                face.live.close();
            }
        },
        /** {@return the place of a container's content} */
        body(name) {
            return containers.get(name).body;
        },
        isOpen: () => open,
        close() {
            node.removeEventListener('click', onClick);
            for (const face of faces) {
                face.properties.close();
                face.live.close();
            }
        },
    };
}
