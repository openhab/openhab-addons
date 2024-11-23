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
package org.openhab.binding.dmx.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
 * Tests cases for {@link org.openhab.binding.dmx.internal.handler.SacnBridgeHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SacnBridgeHandlerTest extends JavaTest {
    private static final String TEST_ADDRESS = "localhost";
    private static final int TEST_UNIVERSE = 1;
    private static final ThingUID BRIDGE_UID_SACN = new ThingUID(THING_TYPE_SACN_BRIDGE, "sacnbridge");
    private static final ChannelUID CHANNEL_UID_MUTE = new ChannelUID(BRIDGE_UID_SACN, CHANNEL_MUTE);

    private @NonNullByDefault({}) Map<String, Object> bridgeProperties;
    private @NonNullByDefault({}) Bridge bridge;
    private @NonNullByDefault({}) SacnBridgeHandler bridgeHandler;

    @BeforeEach
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

    @AfterEach
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
