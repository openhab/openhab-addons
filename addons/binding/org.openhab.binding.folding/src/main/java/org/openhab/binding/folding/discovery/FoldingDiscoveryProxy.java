package org.openhab.binding.folding.discovery;

import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * Singleton instance to connect the Client handler and the discovery
 * service.
 */

public class FoldingDiscoveryProxy {

    private static FoldingDiscoveryProxy instance;
    private FoldingSlotDiscoveryService discoveryService = null;

    private FoldingDiscoveryProxy() {
    }

    public static FoldingDiscoveryProxy getInstance() {
        if (instance == null) {
            instance = new FoldingDiscoveryProxy();
        }
        return instance;
    }

    public void setService(FoldingSlotDiscoveryService service) {
        this.discoveryService = service;
    }

    public void newSlot(ThingUID bridgeUID, String host, String id, String description) {
        if (instance != null) {
            discoveryService.newSlot(bridgeUID, host, id, description);
        }
    }

}
