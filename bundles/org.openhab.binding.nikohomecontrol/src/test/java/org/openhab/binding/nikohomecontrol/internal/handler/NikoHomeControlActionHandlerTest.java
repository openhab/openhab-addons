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
package org.openhab.binding.nikohomecontrol.internal.handler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;
import static org.openhab.binding.nikohomecontrol.internal.NikoHomeControlBindingConstants.*;

import java.util.Map;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcAction;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.ActionType;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests action-state range validation and percentage conversion.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class NikoHomeControlActionHandlerTest {
    private static final ThingUID BRIDGE_UID = new ThingUID(BRIDGEI_THING_TYPE, "controller");
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private @Nullable NikoHomeControlActionHandler currentHandler;

    @AfterEach
    void disposeHandler() {
        NikoHomeControlActionHandler handler = currentHandler;
        if (handler != null) {
            handler.dispose();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 50, 99, 100 })
    void dimmerPublishesValidPercentages(int state) {
        NikoHomeControlActionHandler handler = createHandler(ActionType.DIMMER, false);

        handler.actionEvent(state);

        verify(callback).stateUpdated(new ChannelUID(handler.getThing().getUID(), CHANNEL_BRIGHTNESS),
                new PercentType(state));
        verify(callback).statusUpdated(handler.getThing(),
                new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        verifyNoMoreInteractions(callback);
    }

    @ParameterizedTest
    @CsvSource({ "0,false,100", "1,false,99", "50,false,50", "99,false,1", "100,false,0", "0,true,0", "1,true,1",
            "50,true,50", "99,true,99", "100,true,100" })
    void blindPublishesValidPositions(int state, boolean invert, int expected) {
        NikoHomeControlActionHandler handler = createHandler(ActionType.ROLLERSHUTTER, invert);

        handler.actionEvent(state);

        verify(callback).stateUpdated(new ChannelUID(handler.getThing().getUID(), CHANNEL_ROLLERSHUTTER),
                new PercentType(expected));
        verify(callback).statusUpdated(handler.getThing(),
                new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        verifyNoMoreInteractions(callback);
    }

    @ParameterizedTest
    @MethodSource("invalidPercentageStates")
    void invalidPercentageStatesAreIgnored(ActionType type, boolean invert, long state) {
        NikoHomeControlActionHandler handler = createHandler(type, invert);

        assertDoesNotThrow(() -> handler.actionEvent(state));

        verifyNoInteractions(callback);
    }

    private static Stream<Arguments> invalidPercentageStates() {
        return LongStream
                .of(-1, 101, Integer.MIN_VALUE, Integer.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE, 4294967296L,
                        4294967346L, -4294967246L)
                .boxed()
                .flatMap(state -> Stream.of(Arguments.of(ActionType.DIMMER, false, state),
                        Arguments.of(ActionType.ROLLERSHUTTER, false, state),
                        Arguments.of(ActionType.ROLLERSHUTTER, true, state)));
    }

    @ParameterizedTest
    @CsvSource({ "DIMMER,false,brightness,37", "ROLLERSHUTTER,false,rollershutter,63",
            "ROLLERSHUTTER,true,rollershutter,37" })
    void validUpdateStillWorksAfterAnInvalidState(ActionType type, boolean invert, String channel, int expected) {
        NikoHomeControlActionHandler handler = createHandler(type, invert);

        handler.actionEvent(4294967346L);
        handler.actionEvent(37);

        verify(callback).stateUpdated(new ChannelUID(handler.getThing().getUID(), channel), new PercentType(expected));
        verify(callback).statusUpdated(handler.getThing(),
                new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        verifyNoMoreInteractions(callback);
    }

    private NikoHomeControlActionHandler createHandler(ActionType type, boolean invert) {
        ThingTypeUID thingType = type == ActionType.DIMMER ? THING_TYPE_DIMMABLE_LIGHT : THING_TYPE_BLIND;
        Thing thing = ThingBuilder.create(thingType, "action").withBridge(BRIDGE_UID)
                .withConfiguration(new Configuration(Map.of("actionId", "1", "invert", invert))).build();
        Bridge bridge = mock(Bridge.class);
        NikoHomeControlBridgeHandler bridgeHandler = mock(NikoHomeControlBridgeHandler.class);
        NikoHomeControlCommunication communication = mock(NikoHomeControlCommunication.class);
        NhcAction action = mock(NhcAction.class);
        when(callback.getBridge(BRIDGE_UID)).thenReturn(bridge);
        when(bridge.getHandler()).thenReturn(bridgeHandler);
        when(bridge.getStatus()).thenReturn(ThingStatus.OFFLINE);
        when(bridgeHandler.getCommunication()).thenReturn(communication);
        when(communication.communicationActive()).thenReturn(true);
        when(communication.getActions()).thenReturn(Map.of("1", action));
        when(action.getType()).thenReturn(type);

        NikoHomeControlActionHandler handler = new NikoHomeControlActionHandler(thing);
        currentHandler = handler;
        handler.setCallback(callback);
        handler.initialize();
        handler.startCommunication();
        clearInvocations(callback);
        return handler;
    }
}
