/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.dmx.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.dmx.internal.DmxBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;

/**
 * Tests cases for {@link org.openhab.binding.dmx.internal.handler.Lib485BridgeHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Lib485BridgeHandlerTest extends JavaTest {
    private static final String TEST_ADDRESS = "localhost";
    private static final ThingUID BRIDGE_UID_LIB485 = new ThingUID(THING_TYPE_LIB485_BRIDGE, "lib485bridge");
    private static final ChannelUID CHANNEL_UID_MUTE = new ChannelUID(BRIDGE_UID_LIB485, CHANNEL_MUTE);

    private @NonNullByDefault({}) Map<String, Object> bridgeProperties;
    private @NonNullByDefault({}) Bridge bridge;
    private @NonNullByDefault({}) Lib485BridgeHandler bridgeHandler;

    @BeforeEach
    public void setUp() {
        bridgeProperties = new HashMap<>();
        bridgeProperties.put(CONFIG_ADDRESS, TEST_ADDRESS);
        bridge = BridgeBuilder.create(THING_TYPE_LIB485_BRIDGE, "lib485bridge").withLabel("Lib485 Bridge")
                .withChannel(ChannelBuilder.create(CHANNEL_UID_MUTE, "Switch").withType(MUTE_CHANNEL_TYPEUID).build())
                .withConfiguration(new Configuration(bridgeProperties)).build();

        ThingHandlerCallback mockCallback = mock(ThingHandlerCallback.class);
        doAnswer(answer -> {
            ((Thing) answer.getArgument(0)).setStatusInfo(answer.getArgument(1));
            return null;
        }).when(mockCallback).statusUpdated(any(), any());

        bridgeHandler = new Lib485BridgeHandler(bridge) {
            @Override
            protected void validateConfigurationParameters(Map<String, Object> configurationParameters) {
            }
        };

        bridgeHandler.getThing().setHandler(bridgeHandler);
        bridgeHandler.setCallback(mockCallback);
        bridgeHandler.initialize();
    }

    @AfterEach
    public void tearDown() {
        bridgeHandler.dispose();
    }

    @Test
    public void assertBridgeStatus() {
        waitForAssert(() -> assertEquals(ThingStatus.OFFLINE, bridge.getStatusInfo().getStatus()));
    }
}
