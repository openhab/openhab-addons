/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.dmx.internal.DmxBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.test.java.JavaTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests cases for {@link org.openhab.binding.dmx.internal.handler.Lib485BridgeHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class Lib485BridgeHandlerTest extends JavaTest {

    private static final String TEST_ADDRESS = "localhost";

    private final ThingUID BRIDGE_UID_LIB485 = new ThingUID(THING_TYPE_LIB485_BRIDGE, "lib485bridge");
    private final ChannelUID CHANNEL_UID_MUTE = new ChannelUID(BRIDGE_UID_LIB485, CHANNEL_MUTE);

    Map<String, Object> bridgeProperties;

    private Bridge bridge;
    private Lib485BridgeHandler bridgeHandler;

    @Before
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

    @After
    public void tearDown() {
        bridgeHandler.dispose();
    }

    @Test
    public void assertBridgeStatus() {
        waitForAssert(() -> assertEquals(ThingStatus.OFFLINE, bridge.getStatusInfo().getStatus()));
    }
}
