/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.folding.internal.discovery;

import org.openhab.core.thing.ThingUID;

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
