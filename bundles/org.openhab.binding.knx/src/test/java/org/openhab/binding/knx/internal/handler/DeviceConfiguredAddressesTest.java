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
package org.openhab.binding.knx.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.knx.internal.KNXBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.client.KNXClient;
import org.openhab.binding.knx.internal.project.KnxConfiguredAddress;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Checks the group addresses a device Thing lists for a comparison with the ETS project.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class DeviceConfiguredAddressesTest {
    private static final ThingUID THING = new ThingUID("knx", "device", "made-up");
    private static final ThingUID BRIDGE = new ThingUID("knx", "ip", "made-up");

    private static Channel channel(String id, String ga) {
        return ChannelBuilder.create(new ChannelUID(THING, id)).withType(new ChannelTypeUID(BINDING_ID, CHANNEL_SWITCH))
                .withConfiguration(new Configuration(Map.of("ga", ga))).build();
    }

    @Test
    void allChannelsInTheOrderOfTheirUid() {
        Thing thing = ThingBuilder.create(THING_TYPE_DEVICE, THING).withBridge(BRIDGE)
                .withChannels(List.of(channel("zeta", "31/7/2"), channel("alpha", "31/7/1"), channel("mid", "31/7/3")))
                .build();
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        Bridge bridge = mock(Bridge.class);
        IPBridgeThingHandler bridgeHandler = mock(IPBridgeThingHandler.class);
        KNXClient client = mock(KNXClient.class);
        when(bridge.getHandler()).thenReturn(bridgeHandler);
        when(bridgeHandler.getClient()).thenReturn(client);
        when(bridgeHandler.getScheduler()).thenReturn(mock(ScheduledExecutorService.class));
        when(callback.getBridge(BRIDGE)).thenReturn(bridge);
        when(callback.isChannelLinked(any())).thenReturn(false);
        DeviceThingHandler handler = new DeviceThingHandler(thing);
        handler.setCallback(callback);
        handler.initialize();

        List<String> labels = handler.configuredAddresses().stream().map(KnxConfiguredAddress::label).toList();

        assertEquals(List.of("knx:device:made-up:alpha ga", "knx:device:made-up:mid ga", "knx:device:made-up:zeta ga"),
                labels);

        handler.dispose();
        assertEquals(List.of(), handler.configuredAddresses());
    }
}
