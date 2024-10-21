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
package org.openhab.binding.dirigera.mock;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dirigera.internal.discovery.DirigeraDiscoveryManager;
import org.openhab.core.config.discovery.DiscoveryResult;

/**
 * The {@link DiscoveryMangerMock} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DiscoveryMangerMock extends DirigeraDiscoveryManager {
    public Map<String, DiscoveryResult> discoveries = new HashMap<>();
    public Map<String, DiscoveryResult> deletes = new HashMap<>();

    @Override
    public void thingDiscovered(final DiscoveryResult discoveryResult) {
        String id = discoveryResult.getThingUID().getId();
        System.out.println("Added " + discoveryResult.getThingTypeUID());
        discoveries.put(id, discoveryResult);
    }

    @Override
    public void thingRemoved(final DiscoveryResult discoveryResult) {
        System.out.println("Remove " + discoveryResult.getThingTypeUID());
        String id = discoveryResult.getThingUID().getId();
        DiscoveryResult remover = discoveries.remove(id);
        assertNotNull(remover);
        deletes.put(id, remover);
    }

}
