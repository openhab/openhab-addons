// The Buildings panel: the building structure of call 15 with the devices (call 14) and functions of each space; a
// space lists its devices and functions, a device its group objects, a function its group addresses.

import { buildingTree, devicesUnder, folderNode, topologyTree, unplacedNodes } from '../model.js';
import { text } from '../texts.js';
import { showExplorer } from './explorer.js';

/** @returns {{close: () => void, update: (route: Object) => void}} */
export function show(context) {
    const { api, bridge } = context;
    return showExplorer(context, {
        title: text('view.buildings'),
        icon: 'building',
        startsOpen: (node) => node.kind === 'space' && node.children.some((child) => child.kind === 'space'),
        emptyNote: 'building.noSpaces',
        load: async () => {
            const [topology, locations] = await Promise.allSettled([
                Promise.resolve().then(() => api.topology(bridge.uid)),
                Promise.resolve().then(() => api.locations(bridge.uid)),
            ]);
            const known = topology.status === 'fulfilled' ? topologyTree(topology.value).devices : [];
            const roots = locations.status === 'fulfilled' ? buildingTree(locations.value, known) : [];
            const inBuilding = roots.flatMap(devicesUnder);
            // trades are left out: call 15 gives the building structure only
            return {
                roots,
                // the first node lists every device, the ones without a room too
                devices: [...new Set([...inBuilding, ...known])],
                folders: [folderNode('folder:no-room', text('folder.noRoom'), { children: unplacedNodes(roots, known) }), folderNode('folder:filter', text('folder.filter'), { rule: 'filter' })],
                problems: [topology, locations].filter((result) => result.status === 'rejected').map((result) => result.reason),
            };
        },
    });
}
