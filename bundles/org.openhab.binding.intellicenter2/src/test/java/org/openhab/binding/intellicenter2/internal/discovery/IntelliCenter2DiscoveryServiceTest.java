/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.intellicenter2.internal.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.intellicenter2.internal.IntelliCenter2Configuration;
import org.openhab.binding.intellicenter2.internal.handler.IntelliCenter2BridgeHandler;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.internal.BridgeImpl;

/**
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
@Disabled
public class IntelliCenter2DiscoveryServiceTest {

    @Test
    @Timeout(30)
    public void testDiscovery() throws Exception {
        var bridge = new BridgeImpl(new ThingTypeUID("intellicenter2:bridge"), getClass().getSimpleName());
        var config = new IntelliCenter2Configuration();
        config.hostname = "192.168.1.148";
        var bridgeHandler = new IntelliCenter2BridgeHandler(bridge, config);
        bridgeHandler.initialize();

        var listener = new SimpleDiscoveryListener();
        var discovery = new IntelliCenter2DiscoveryService();
        discovery.setThingHandler(bridgeHandler);
        discovery.addDiscoveryListener(listener);
        // wait for the protocol to be ready
        bridgeHandler.getProtocol();
        discovery.startScan();

        // 2 pools and 4 features, 1 light, 2 pumps, 3 sensors (that are the same)
        assertEquals(12, listener.discoveredResults.size());
    }
}
