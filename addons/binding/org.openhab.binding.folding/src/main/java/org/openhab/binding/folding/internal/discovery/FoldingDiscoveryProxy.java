/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.folding.internal.discovery;

import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * Singleton instance to connect the Client handler and the discovery
 * service.
 *
 * @author Marius Bjoernstad - Initial contribution
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
