// The workplace of a view: one panel (bar, tree, tabs and list) and the sidebar at its right (Properties, openHAB,
// Live and Find), with a border between them that can be moved. Two panels, one above the other, share one sidebar,
// which the shell makes. The panel and the sidebar are pieces of their own:
// panel.js and sidebar.js.

import { add, el } from './dom.js';
import { createFinder } from './finder.js';
import { createPanel } from './panel.js';
import { NAV_VIEWS, SPLIT_VIEWS, viewsOf } from './router.js';
import { createSidebar } from './sidebar.js';
import { createSplitter } from './splitter.js';
import { text } from './texts.js';

export { TREE_WIDTH } from './panel.js';
export const PROPERTIES_WIDTH = { initial: 300, min: 180, max: 640 };

/**
 * The sidebar with the border at its left and Find in its last container.
 *
 * @param {Object} options
 * @param {Document} options.document
 * @param {{get: Function, set: Function}} [options.layout]
 * @param {Element} options.target the element that holds the sidebar: it carries its width and whether it is closed
 * @param [options.context] its rows feed Live, its calls feed Find, its navigate goes to what Find found
 * @returns {{sidebar: Object, splitter: Object, close: () => void}}
 */
export function createSide({ document, layout, target, context }) {
    const bridge = context?.bridge;
    const sidebar = createSidebar({ document, layout, rows: context?.rows, frame: context?.frame, onOpen: (open) => target.classList.toggle('sidebar-closed', !open) });
    const splitter = createSplitter({ document, target, property: '--properties-width', name: 'properties', layout, ...PROPERTIES_WIDTH, reverse: true, label: text('frame.resizeSidebar'), className: 'properties-splitter' });
    const finder =
        context?.rows && bridge
            ? createFinder({
                  document,
                  load: async () => {
                      const [addresses, details, spaces] = await Promise.allSettled([
                          context.rows.groupAddresses(),
                          context.rows.deviceDetails(),
                          context.rows.locations(),
                      ]);
                      if (addresses.status === 'rejected' && details.status === 'rejected') {
                          throw addresses.reason;
                      }
                      return {
                          addresses: addresses.status === 'fulfilled' ? addresses.value : [],
                          details: details.status === 'fulfilled' ? details.value.devices : [],
                          spaces: spaces.status === 'fulfilled' ? spaces.value : [],
                          incomplete: [addresses, details, spaces].some((read) => read.status === 'rejected') || (details.status === 'fulfilled' && details.value.problems.length > 0),
                      };
                  },
                  onPick: (found) => context.navigate({ view: found.view, bridge: bridge.uid, query: found.row ? { sel: found.sel, row: found.row } : { sel: found.sel } }),
              })
            : null;
    add(document, sidebar.body('find'), finder ? finder.node : el(document, 'p', { class: 'note' }, text('find.noProject')));
    return {
        sidebar,
        splitter,
        close() {
            splitter.close();
            sidebar.close();
            finder?.close();
        },
    };
}

/**
 * @param {Object} options
 * @param {Document} options.document
 * @param {Element} options.element the view's element
 * @param {{get: Function, set: Function}} [options.layout]
 * @param {string} options.title the panel's name
 * @param {boolean} [options.tree] false for a panel without a tree
 * @param [options.context] the view's context: its bridge and route name the panels of the chooser, its rows feed
 *     Live, its calls feed Find; its split says whether the workplace has two panels, which then share a sidebar
 *     that is not this panel's own
 * @returns {Object} what the panel gives (tools, find, treePane, list, setTree, setCrumbs, setTabs, closeTree, focusList),
 *     notes, and the panel's face of the sidebar: properties, openhab, live
 */
export function createWorkplace({ document, element, layout, title, tree = true, context }) {
    const notes = el(document, 'div', { class: 'notes' });
    const bridge = context?.bridge;
    const offered = bridge ? viewsOf(bridge) : [];
    const split = context?.split ?? null;
    // the lower panel offers what can stand there
    const choices = NAV_VIEWS.filter((view) => offered.includes(view) && (split?.half !== 'second' || SPLIT_VIEWS.includes(view)));
    const panel = createPanel({
        document,
        layout,
        title,
        tree,
        panels: choices.map((view) => ({ view, label: text(`view.${view}`) })),
        current: context?.route?.view,
        onPanel: (view) => context.navigate({ view, bridge: bridge.uid, query: {} }),
        history: context?.history,
        second: typeof split?.toggle === 'function' ? { open: split.open === true, toggle: split.toggle } : null,
    });
    const box = el(document, 'div', { class: 'workplace' });
    const shared = split?.sidebar ?? null;
    const side = shared ? null : createSide({ document, layout, target: box, context });
    const face = shared ? shared.face() : side.sidebar.face();
    add(document, box, panel.node, side ? [side.splitter.node, side.sidebar.node] : null);
    element.classList.add('work');
    add(document, element, el(document, 'h2', {}, title), notes, box);

    return {
        notes,
        panel,
        sidebar: side?.sidebar ?? null,
        tools: panel.tools,
        find: panel.find,
        treePane: panel.treePane,
        list: panel.list,
        properties: face.properties,
        openhab: face.openhab,
        live: face.live,
        setTree: panel.setTree,
        setCrumbs: panel.setCrumbs,
        setTabs: panel.setTabs,
        closeTree: panel.closeTree,
        focusList: panel.focusList,
        /** {@return whether the node is in the panel or in its face of the sidebar, which may stand outside the view} */
        holds(node) {
            return element.contains(node) || [face.properties.node, face.openhab.node, face.live.node].some((part) => part.contains(node));
        },
        close() {
            panel.close();
            if (side) {
                side.close();
            } else {
                shared.release(face);
            }
        },
    };
}
