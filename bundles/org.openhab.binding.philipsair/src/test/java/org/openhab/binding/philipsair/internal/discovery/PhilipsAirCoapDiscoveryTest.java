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
package org.openhab.binding.philipsair.internal.discovery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.philipsair.internal.PhilipsAirConfiguration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingUID;

/**
 * Tests the creation of discovery results of {@link PhilipsAirCoapDiscovery}, without sending any CoAP traffic.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class PhilipsAirCoapDiscoveryTest {

    private final List<DiscoveryResult> results = new ArrayList<>();
    private @NonNullByDefault({}) PhilipsAirCoapDiscovery discovery;

    @BeforeEach
    public void setUp() {
        discovery = new PhilipsAirCoapDiscovery(mock(NetworkAddressService.class)) {
            @Override
            protected void thingDiscovered(DiscoveryResult discoveryResult) {
                results.add(discoveryResult);
            }
        };
    }

    @AfterEach
    public void tearDown() {
        if (discovery != null) {
            discovery.deactivate();
        }
    }

    @Test
    public void validResponseCreatesResult() {
        discovery.discovered(
                "{\"name\":\"Living\",\"type\":\"AC3737\",\"modelid\":\"AC3737/10\",\"device_id\":\"abc123\","
                        + "\"product_id\":\"x\"}",
                "192.168.1.60");

        assertEquals(1, results.size());
        DiscoveryResult result = results.get(0);
        assertEquals(new ThingUID(THING_TYPE_COAP, "abc123"), result.getThingUID());
        assertEquals(PhilipsAirConfiguration.CONFIG_DEVICE_UUID, result.getRepresentationProperty());
        assertEquals("192.168.1.60", result.getProperties().get(PhilipsAirConfiguration.CONFIG_HOST));
        assertEquals("abc123", result.getProperties().get(PhilipsAirConfiguration.CONFIG_DEVICE_UUID));
    }

    @Test
    public void incompleteOrInvalidResponseCreatesNoResult() {
        discovery.discovered("{\"name\":\"Living\"}", "192.168.1.60");
        discovery.discovered("not json", "192.168.1.60");

        assertTrue(results.isEmpty());
    }
}
