/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.dmx.internal.DmxBridgeHandler;
import org.openhab.binding.dmx.internal.multiverse.BaseDmxChannel;
import org.openhab.binding.dmx.internal.multiverse.Universe;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests cases for {@link DmxBridgeHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class DmxBridgeHandlerTest extends JavaTest {

    /**
     * simple DmxBridgeHandlerImplementation
     *
     * @author Jan N. Klug
     *
     */
    public class DmxBridgeHandlerImpl extends DmxBridgeHandler {
        public DmxBridgeHandlerImpl(Bridge dmxBridge) {
            super(dmxBridge);
        }

        @Override
        public void openConnection() {
        }

        @Override
        protected void sendDmxData() {
        }

        @Override
        protected void closeConnection() {
        }

        @Override
        public void initialize() {
            universe = new Universe(TEST_UNIVERSE);
            super.updateConfiguration();
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private static final int TEST_UNIVERSE = 1;
    private static final int TEST_CHANNEL = 100;

    private final ThingTypeUID THING_TYPE_TEST_BRIDGE = new ThingTypeUID(BINDING_ID, "testbridge");
    private final ThingUID BRIDGE_UID_TEST = new ThingUID(THING_TYPE_TEST_BRIDGE, "testbridge");
    private final ChannelUID CHANNEL_UID_MUTE = new ChannelUID(BRIDGE_UID_TEST, CHANNEL_MUTE);

    Map<String, Object> bridgeProperties;

    private Bridge bridge;
    private DmxBridgeHandlerImpl bridgeHandler;

    @BeforeEach
    public void setUp() {
        bridgeProperties = new HashMap<>();
        bridge = BridgeBuilder.create(THING_TYPE_TEST_BRIDGE, "testbridge").withLabel("Test Bridge")
                .withChannel(ChannelBuilder.create(CHANNEL_UID_MUTE, "Switch").withType(MUTE_CHANNEL_TYPEUID).build())
                .withConfiguration(new Configuration(bridgeProperties)).build();

        ThingHandlerCallback mockCallback = mock(ThingHandlerCallback.class);
        doAnswer(answer -> {
            ((Thing) answer.getArgument(0)).setStatusInfo(answer.getArgument(1));
            return null;
        }).when(mockCallback).statusUpdated(any(), any());

        bridgeHandler = Mockito.spy(new DmxBridgeHandlerImpl(bridge));
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

    @Disabled("https://github.com/eclipse/smarthome/issues/6015#issuecomment-411313627")
    @Test
    public void assertSendDmxDataIsCalled() {
        Mockito.verify(bridgeHandler, after(500).atLeast(9)).sendDmxData();
    }

    @Disabled("https://github.com/eclipse/smarthome/issues/6015")
    @Test
    public void assertMuteChannelMutesOutput() {
        bridgeHandler.handleCommand(CHANNEL_UID_MUTE, OnOffType.ON);
        Mockito.verify(bridgeHandler, after(500).atMost(1)).sendDmxData();

        bridgeHandler.handleCommand(CHANNEL_UID_MUTE, OnOffType.OFF);
        Mockito.verify(bridgeHandler, after(500).atLeast(9)).sendDmxData();
    }

    @Test
    public void assertRetrievingOfChannels() {
        BaseDmxChannel channel = new BaseDmxChannel(TEST_UNIVERSE, TEST_CHANNEL);
        BaseDmxChannel returnedChannel = bridgeHandler.getDmxChannel(channel,
                ThingBuilder.create(THING_TYPE_DIMMER, "testthing").build());

        Integer channelId = returnedChannel.getChannelId();
        assertThat(channelId, is(TEST_CHANNEL));
    }
}
