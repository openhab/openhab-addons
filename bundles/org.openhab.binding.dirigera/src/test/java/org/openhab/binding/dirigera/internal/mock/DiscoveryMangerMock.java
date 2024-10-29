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
package org.openhab.binding.dirigera.internal.mock;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dirigera.internal.discovery.DirigeraDiscoveryManager;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DiscoveryMangerMock} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DiscoveryMangerMock extends DirigeraDiscoveryManager {
    private final Logger logger = LoggerFactory.getLogger(DiscoveryMangerMock.class);
    public Map<String, DiscoveryResult> discoveries = new HashMap<>();
    public Map<String, DiscoveryResult> deletes = new HashMap<>();

    @Override
    public void thingDiscovered(final DiscoveryResult discoveryResult) {
        synchronized (this) {
            // logger.warn("Discovery thingDiscovered {}", discoveryResult);
            String id = discoveryResult.getThingUID().getId();
            discoveries.put(id, discoveryResult);
            this.notifyAll();
        }
    }

    @Override
    public void thingRemoved(final DiscoveryResult discoveryResult) {
        // logger.warn("Discovery thingRemoved {}", discoveryResult);
        String id = discoveryResult.getThingUID().getId();
        DiscoveryResult remover = discoveries.remove(id);
        // assertNotNull(remover);
        if (remover != null) {
            deletes.put(id, remover);
        }
    }

    public void waitForDetection() {
        synchronized (this) {
            if (discoveries.isEmpty()) {
                try {
                    wait(5000);
                } catch (InterruptedException e) {
                    fail(e.getMessage());
                }
            }
        }
    }
}
