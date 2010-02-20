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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
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
 * Tests cases for {@link org.openhab.binding.dmx.internal.handler.SacnBridgeHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class SacnBridgeHandlerTest extends JavaTest {
    private static final String TEST_ADDRESS = "localhost";
    private static final int TEST_UNIVERSE = 1;

    private final ThingUID BRIDGE_UID_SACN = new ThingUID(THING_TYPE_SACN_BRIDGE, "sacnbridge");
    private final ChannelUID CHANNEL_UID_MUTE = new ChannelUID(BRIDGE_UID_SACN, CHANNEL_MUTE);

    Map<String, Object> bridgeProperties;

    private Bridge bridge;
    private SacnBridgeHandler bridgeHandler;

    @Before
    public void setUp() {
        bridgeProperties = new HashMap<>();
        bridgeProperties.put(CONFIG_ADDRESS, TEST_ADDRESS);
        bridgeProperties.put(CONFIG_UNIVERSE, TEST_UNIVERSE);
        bridgeProperties.put(CONFIG_SACN_MODE, "unicast");
        bridge = BridgeBuilder.create(THING_TYPE_SACN_BRIDGE, "sacnbridge").withLabel("sACN Bridge")
                .withChannel(ChannelBuilder.create(CHANNEL_UID_MUTE, "Switch").withType(MUTE_CHANNEL_TYPEUID).build())
                .withConfiguration(new Configuration(bridgeProperties)).build();

        ThingHandlerCallback mockCallback = mock(ThingHandlerCallback.class);
        doAnswer(answer -> {
            ((Thing) answer.getArgument(0)).setStatusInfo(answer.getArgument(1));
            return null;
        }).when(mockCallback).statusUpdated(any(), any());

        bridgeHandler = new SacnBridgeHandler(bridge) {
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
        waitForAssert(() -> assertEquals(ThingStatus.ONLINE, bridge.getStatusInfo().getStatus()));
    }

    @Test
    public void renamingOfUniverses() {
        waitForAssert(() -> assertThat(bridgeHandler.getUniverseId(), is(TEST_UNIVERSE)));

        bridgeProperties.replace(CONFIG_UNIVERSE, 2);
        bridgeHandler.handleConfigurationUpdate(bridgeProperties);
        waitForAssert(() -> assertThat(bridgeHandler.getUniverseId(), is(2)));

        bridgeProperties.replace(CONFIG_UNIVERSE, TEST_UNIVERSE);
        bridgeHandler.handleConfigurationUpdate(bridgeProperties);
        waitForAssert(() -> assertThat(bridgeHandler.getUniverseId(), is(TEST_UNIVERSE)));
    }
}
