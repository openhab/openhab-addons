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
package org.openhab.binding.veluxactive.internal.discovery;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.veluxactive.internal.VeluxActiveBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.veluxactive.internal.api.ModuleInfo;
import org.openhab.binding.veluxactive.internal.api.ModuleType;
import org.openhab.binding.veluxactive.internal.handler.VeluxActiveGatewayHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * Verifies sensor discovery uses module identity independently of room assignment.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
class VeluxActiveGatewayDiscoveryServiceTest {
    @Test
    void discoversPhysicalSensorWithStableIdentityAcrossRoomMoves() {
        var account = mock(VeluxActiveGatewayHandler.class);
        var bridge = BridgeBuilder.create(THING_TYPE_GATEWAY, "test").build();
        when(account.getThing()).thenReturn(bridge);
        var results = new ArrayList<DiscoveryResult>();
        var service = new VeluxActiveGatewayDiscoveryService() {
            @Override
            protected void thingDiscovered(DiscoveryResult result) {
                results.add(result);
            }
        };
        service.setThingHandler(account);
        var replies = new ArrayList<Consumer<List<ModuleInfo>>>();
        doAnswer(invocation -> {
            replies.add(invocation.getArgument(0));
            return null;
        }).when(account).discover(any());
        try {
            service.startScan();
            replies.getFirst()
                    .accept(List.of(
                            new ModuleInfo("home", "sensor", "Sensor", ModuleType.INDOOR_SENSOR, "hall", "Hall", null),
                            new ModuleInfo("home", "gateway", "Gateway", ModuleType.GATEWAY, null, null, null),
                            new ModuleInfo("home", "switch", "Switch", ModuleType.DEPARTURE_SWITCH, null, null, null),
                            new ModuleInfo("home", "opening", "Window", ModuleType.OPENING, "hall", "Hall", null)));
            assertThat(results.size(), is(3));
            assertThat(results.get(2).getThingTypeUID(), is(THING_TYPE_OPENING));
            assertThat(results.get(2).getProperties().get("moduleId"), is("opening"));
            assertThat(results.get(2).getBridgeUID(), is(bridge.getUID()));
            assertThat(results.get(1).getThingTypeUID(), is(THING_TYPE_DEPARTURE_SWITCH));
            assertThat(results.get(1).getProperties().get("moduleId"), is("switch"));
            assertThat(results.getFirst().getThingTypeUID(), is(THING_TYPE_INDOOR_SENSOR));
            assertThat(results.getFirst().getProperties().get("moduleId"), is("sensor"));
            assertThat(results.getFirst().getBridgeUID(), is(bridge.getUID()));
            assertThat(results.getFirst().getProperties().containsKey("homeId"), is(false));
            assertThat(results.get(2).getLabel(), is("Window (Hall)"));
            assertThat(results.getFirst().getLabel(), is("Sensor"));
            var openingUID = results.get(2).getThingUID();
            var sensorUID = results.getFirst().getThingUID();
            service.startScan();
            replies.get(1).accept(List.of(
                    new ModuleInfo("home", "sensor", "Sensor", ModuleType.INDOOR_SENSOR, "bedroom", "Bedroom", null)));
            assertThat(results.getLast().getThingUID(), is(sensorUID));
            service.startScan();
            replies.getLast().accept(List
                    .of(new ModuleInfo("home", "opening", "Window", ModuleType.OPENING, "bedroom", "Bedroom", null)));
            assertThat(results.getLast().getThingUID(), is(openingUID));
            assertThat(results.getLast().getLabel(), is("Window (Bedroom)"));
            for (String roomName : new String[] { null, "", "   " }) {
                service.startScan();
                replies.getLast().accept(
                        List.of(new ModuleInfo("home", "opening", "Window", ModuleType.OPENING, null, roomName, null)));
                assertThat(results.getLast().getThingUID(), is(openingUID));
                assertThat(results.getLast().getLabel(), is("Window"));
            }
            service.dispose();
            replies.getLast().accept(
                    List.of(new ModuleInfo("home", "late", "Late", ModuleType.INDOOR_SENSOR, null, null, null)));
            assertThat(results.size(), is(8));
        } finally {
            service.dispose();
        }
    }
}
