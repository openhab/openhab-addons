/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingUID;

/**
 * Singleton instance to connect the Client handler and the discovery
 * service.
 *
 * @author Marius Bjoernstad - Initial contribution
 */
@NonNullByDefault
public class FoldingDiscoveryProxy {

    private static final FoldingDiscoveryProxy INSTANCE = new FoldingDiscoveryProxy();
    private volatile @Nullable FoldingSlotDiscoveryService discoveryService;

    private FoldingDiscoveryProxy() {
    }

    public static FoldingDiscoveryProxy getInstance() {
        return INSTANCE;
    }

    public void setService(FoldingSlotDiscoveryService service) {
        this.discoveryService = service;
    }

    public void newSlot(ThingUID bridgeUID, String host, @Nullable String id, @Nullable String description) {
        FoldingSlotDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null) {
            discoveryService.newSlot(bridgeUID, host, id, description);
        }
    }
}
