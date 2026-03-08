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
package org.openhab.binding.dirigera.internal.mock;

import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dirigera.internal.discovery.DirigeraDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;

/**
 * The {@link DicoveryServiceMock} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DicoveryServiceMock extends DirigeraDiscoveryService {
    public Map<String, DiscoveryResult> discoveries = new HashMap<>();
    public Map<String, DiscoveryResult> deletes = new HashMap<>();

    @Override
    public void deviceDiscovered(final DiscoveryResult discoveryResult) {
        synchronized (this) {
            // logger.warn("Discovery thingDiscovered {}", discoveryResult);
            String id = discoveryResult.getThingUID().getId();
            discoveries.put(id, discoveryResult);
            this.notifyAll();
        }
    }

    @Override
    public void deviceRemoved(final DiscoveryResult discoveryResult) {
        String id = discoveryResult.getThingUID().getId();
        synchronized (this) {
            DiscoveryResult remover = discoveries.remove(id);
            if (remover != null) {
                deletes.put(id, remover);
            }
        }
    }

    public void waitForDetection(int size) {
        synchronized (this) {
            Instant start = Instant.now();
            Instant check = Instant.now();
            while (discoveries.size() != size && Duration.between(start, check).getSeconds() < 5) {
                try {
                    wait(100);
                } catch (InterruptedException e) {
                    fail(e.getMessage());
                }
                check = Instant.now();
            }
        }
        if (discoveries.size() != size) {
            fail("Discovery size expected " + size + " but was " + discoveries.size());
        }
    }
}
