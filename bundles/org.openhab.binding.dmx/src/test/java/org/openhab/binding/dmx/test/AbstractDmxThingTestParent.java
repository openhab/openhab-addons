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
package org.openhab.binding.dmx.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.dmx.test.TestBridgeHandler.THING_TYPE_TEST_BRIDGE;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.test.java.JavaTest;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

/**
 * Common utilities for DMX thing handler tests.
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
public class AbstractDmxThingTestParent extends JavaTest {

    private Map<String, Object> bridgeProperties;

    protected Bridge bridge;
    protected TestBridgeHandler dmxBridgeHandler;
    protected ThingHandlerCallback mockCallback;

    protected void setup() {
        initializeBridge();

        mockCallback = mock(ThingHandlerCallback.class);
        doAnswer(answer -> {
            ((Thing) answer.getArgument(0)).setStatusInfo(answer.getArgument(1));
            return null;
        }).when(mockCallback).statusUpdated(any(), any());
    }

    private void initializeBridge() {
        bridgeProperties = new HashMap<>();
        bridge = BridgeBuilder.create(THING_TYPE_TEST_BRIDGE, "testbridge").withLabel("Test Bridge")
                .withConfiguration(new Configuration(bridgeProperties)).build();
        dmxBridgeHandler = new TestBridgeHandler(bridge);
        bridge.setHandler(dmxBridgeHandler);
        ThingHandlerCallback bridgeHandler = mock(ThingHandlerCallback.class);
        doAnswer(answer -> {
            ((Thing) answer.getArgument(0)).setStatusInfo(answer.getArgument(1));
            return null;
        }).when(bridgeHandler).statusUpdated(any(), any());
        dmxBridgeHandler.setCallback(bridgeHandler);
        dmxBridgeHandler.initialize();
    }

    protected void initializeHandler(ThingHandler handler) {
        handler.getThing().setHandler(handler);
        handler.setCallback(mockCallback);
        handler.initialize();
    }

    protected void assertThingStatus(Thing thing) {
        // check that thing turns online if properly configured
        waitForAssert(() -> assertEquals(ThingStatus.ONLINE, thing.getStatusInfo().getStatus()));

        // check that thing properly follows bridge status
        ThingHandler handler = thing.getHandler();
        assertNotNull(handler);
        handler.bridgeStatusChanged(ThingStatusInfoBuilder.create(ThingStatus.OFFLINE).build());
        waitForAssert(() -> assertEquals(ThingStatus.OFFLINE, thing.getStatusInfo().getStatus()));
        handler.bridgeStatusChanged(ThingStatusInfoBuilder.create(ThingStatus.ONLINE).build());
        waitForAssert(() -> assertEquals(ThingStatus.ONLINE, thing.getStatusInfo().getStatus()));
    }

    protected void assertThingStatusWithoutBridge(ThingHandler handler) {
        handler.setCallback(mockCallback);
        handler.initialize();
        waitForAssert(() -> {
            assertEquals(ThingStatus.OFFLINE, handler.getThing().getStatus());
            assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, handler.getThing().getStatusInfo().getStatusDetail());
        });
    }

    public void assertPercentTypeCommands(ThingHandler handler, ChannelUID channelUID, int fadeTime) {
        long currentTime = System.currentTimeMillis();

        // set 30%
        handler.handleCommand(channelUID, new PercentType(30));
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, fadeTime);

        waitForAssert(() -> {
            assertChannelStateUpdate(channelUID,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(30.0, 1.0))));
        });

        // set 0%
        handler.handleCommand(channelUID, PercentType.ZERO);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, fadeTime);

        waitForAssert(() -> {
            assertChannelStateUpdate(channelUID, state -> assertEquals(PercentType.ZERO, state));
        });

        // set 100%
        handler.handleCommand(channelUID, PercentType.HUNDRED);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, fadeTime);

        waitForAssert(() -> {
            assertChannelStateUpdate(channelUID,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(100.0, 0.5))));
        });
    }

    protected void assertChannelStateUpdate(ChannelUID channelUID, Consumer<State> stateAssertion) {
        ArgumentCaptor<State> captor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(ArgumentMatchers.eq(channelUID), captor.capture());
        State value = captor.getValue();
        stateAssertion.accept(value);
    }
}
