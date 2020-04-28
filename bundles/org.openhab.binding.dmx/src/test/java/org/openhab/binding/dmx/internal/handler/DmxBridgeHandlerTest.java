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
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.test.java.JavaTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.openhab.binding.dmx.internal.DmxBridgeHandler;
import org.openhab.binding.dmx.internal.multiverse.BaseDmxChannel;
import org.openhab.binding.dmx.internal.multiverse.Universe;

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

    @Before
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

    @After
    public void tearDown() {
        bridgeHandler.dispose();
    }

    @Test
    public void assertBridgeStatus() {
        waitForAssert(() -> assertEquals(ThingStatus.ONLINE, bridge.getStatusInfo().getStatus()));
    }

    @Ignore("https://github.com/eclipse/smarthome/issues/6015#issuecomment-411313627")
    @Test
    public void assertSendDmxDataIsCalled() {
        Mockito.verify(bridgeHandler, after(500).atLeast(9)).sendDmxData();
    }

    @Ignore("https://github.com/eclipse/smarthome/issues/6015")
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
