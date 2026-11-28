// The Topology panel: areas, lines and devices by individual address (call 14), the devices without one in a dynamic
// folder; a place lists its devices, a device its group objects.

import { topologyTree } from '../model.js';
import { text } from '../texts.js';
import { showExplorer } from './explorer.js';

/** @returns {{close: () => void, update: (route: Object) => void}} */
export function show(context) {
    const { api, bridge } = context;
    const lines = new Map();
    return showExplorer(context, {
        title: text('view.topology'),
        icon: 'area',
        startsOpen: (node) => node.kind === 'area',
        lineOf: (device) => {
            const address = typeof device.address === 'string' ? device.address : '';
            const line = address.slice(0, address.lastIndexOf('.'));
            return lines.get(line) ?? line;
        },
        emptyDevices: 'devices.empty',
        load: async () => {
            const { roots, devices, unassigned } = topologyTree(await api.topology(bridge.uid));
            for (const line of roots.flatMap((area) => area.children)) {
                lines.set(line.address, [line.address, line.name].filter((part) => part !== '').join(' '));
            }
            // "Secure devices" is left out: the calls do not say which device is secured
            return { roots, devices, folders: [unassigned] };
        },
    });
}
