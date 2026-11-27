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
package org.openhab.binding.e3dc.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.e3dc.internal.E3DCBindingConstants;
import org.openhab.binding.e3dc.internal.rscp.RscpException;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests how the things behind the bridge follow it: the status they report, the data that brings them online and
 * the command that must not block the thread of the framework.
 *
 * <p>
 * {@code @SuppressWarnings("null")}: Mockito is not designed with null type annotations in mind, so combining it
 * with this {@code @NonNullByDefault} test class produces "unsafe interpretation" compiler advisories with no
 * null-safety benefit.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class E3DCChildHandlerTest {

    private static final long WAIT_SECONDS = 10;

    /** A thing type together with the way its handler is built. */
    private record Child(ThingTypeUID type, Function<Thing, E3DCThingHandler> factory) {
        @Override
        public String toString() {
            return type.getId();
        }
    }

    static Stream<Child> children() {
        return Stream.of(new Child(E3DCBindingConstants.THING_TYPE_WALLBOX, E3DCWallboxHandler::new),
                new Child(E3DCBindingConstants.THING_TYPE_POWER_METER, E3DCPowerMeterHandler::new),
                new Child(E3DCBindingConstants.THING_TYPE_BATTERY, E3DCBatteryHandler::new),
                new Child(E3DCBindingConstants.THING_TYPE_BATTERY_MODULE, E3DCBatteryModuleHandler::new));
    }

    /** Builds a handler whose bridge reports the given handler, or no handler at all. */
    private static E3DCThingHandler createHandler(Child child, @Nullable E3DCBridgeHandler bridgeHandler,
            ThingHandlerCallback callback) {
        ThingUID bridgeUID = new ThingUID(E3DCBindingConstants.THING_TYPE_BRIDGE, "device");
        Bridge bridge = BridgeBuilder.create(E3DCBindingConstants.THING_TYPE_BRIDGE, bridgeUID).build();
        if (bridgeHandler != null) {
            bridge.setHandler(bridgeHandler);
        }
        Thing thing = ThingBuilder.create(child.type(), new ThingUID(child.type(), bridgeUID, "child"))
                .withBridge(bridgeUID).build();
        when(callback.getBridge(bridgeUID)).thenReturn(bridge);
        E3DCThingHandler handler = child.factory().apply(thing);
        handler.setCallback(callback);
        return handler;
    }

    private static ThingStatusInfo lastStatus(ThingHandlerCallback callback) {
        ArgumentCaptor<ThingStatusInfo> captor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeastOnce()).statusUpdated(any(), captor.capture());
        return captor.getValue();
    }

    @ParameterizedTest
    @MethodSource("children")
    void whenTheBridgeHasNoHandlerThenTheThingReportsAnUninitializedBridge(Child child) {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        E3DCThingHandler handler = createHandler(child, null, callback);

        handler.initialize();

        ThingStatusInfo status = lastStatus(callback);
        assertEquals(ThingStatus.UNINITIALIZED, status.getStatus());
        assertEquals(ThingStatusDetail.BRIDGE_UNINITIALIZED, status.getStatusDetail());
    }

    @ParameterizedTest
    @MethodSource("children")
    void whenTheBridgeIsNotConnectedThenTheThingReportsAnOfflineBridge(Child child) {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        E3DCBridgeHandler bridgeHandler = mock(E3DCBridgeHandler.class);
        when(bridgeHandler.isConnected()).thenReturn(false);
        E3DCThingHandler handler = createHandler(child, bridgeHandler, callback);

        handler.initialize();

        ThingStatusInfo status = lastStatus(callback);
        assertEquals(ThingStatus.OFFLINE, status.getStatus());
        assertEquals(ThingStatusDetail.BRIDGE_OFFLINE, status.getStatusDetail());
        verify(bridgeHandler).registerDataListener(handler);
    }

    @ParameterizedTest
    @MethodSource("children")
    void whenTheBridgeIsConnectedThenTheThingWaitsForDataAndAsksForARefresh(Child child) {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        E3DCBridgeHandler bridgeHandler = mock(E3DCBridgeHandler.class);
        when(bridgeHandler.isConnected()).thenReturn(true);
        E3DCThingHandler handler = createHandler(child, bridgeHandler, callback);

        handler.initialize();

        assertEquals(ThingStatus.UNKNOWN, lastStatus(callback).getStatus());
        verify(bridgeHandler).requestRefresh();
    }

    @ParameterizedTest
    @MethodSource("children")
    void whenDataCarriesNoValueThenTheThingReportsAConfigurationError(Child child) {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        E3DCBridgeHandler bridgeHandler = mock(E3DCBridgeHandler.class);
        when(bridgeHandler.isConnected()).thenReturn(true);
        E3DCThingHandler handler = createHandler(child, bridgeHandler, callback);
        handler.initialize();

        // the battery module handler only accepts the data container of its own battery
        handler.onData(RscpValue.container(0,
                List.of(RscpValue.container(RscpTag.BAT_DATA, List.of(RscpValue.uchar8(RscpTag.BAT_INDEX, 0))))));

        ThingStatusInfo status = lastStatus(callback);
        assertEquals(ThingStatus.OFFLINE, status.getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, status.getStatusDetail());
    }

    @Test
    void whenDataCarriesAValueThenTheThingGoesOnline() {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        E3DCBridgeHandler bridgeHandler = mock(E3DCBridgeHandler.class);
        when(bridgeHandler.isConnected()).thenReturn(true);
        Child module = children().filter(child -> E3DCBindingConstants.THING_TYPE_BATTERY_MODULE.equals(child.type()))
                .findFirst().orElseThrow();
        E3DCThingHandler handler = createHandler(module, bridgeHandler, callback);
        handler.initialize();

        handler.onData(
                RscpValue.container(0,
                        List.of(RscpValue.container(RscpTag.BAT_DATA,
                                List.of(RscpValue.uchar8(RscpTag.BAT_INDEX, 0),
                                        RscpValue.container(RscpTag.BAT_DCB_INFO, List.of(
                                                RscpValue.uchar8(RscpTag.BAT_DCB_INDEX, 0),
                                                RscpValue.int32(RscpTag.BAT_DCB_REMAINING_CAPACITY, 7400))))))));

        assertEquals(ThingStatus.ONLINE, lastStatus(callback).getStatus());
    }

    @ParameterizedTest
    @MethodSource("children")
    void whenDisposedThenTheThingLeavesTheBridge(Child child) {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        E3DCBridgeHandler bridgeHandler = mock(E3DCBridgeHandler.class);
        when(bridgeHandler.isConnected()).thenReturn(true);
        E3DCThingHandler handler = createHandler(child, bridgeHandler, callback);
        handler.initialize();

        handler.dispose();

        verify(bridgeHandler).unregisterDataListener(handler);
    }

    @Test
    void whenTheDeviceIsSlowThenACommandDoesNotBlockTheCaller() throws Exception {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        E3DCBridgeHandler bridgeHandler = mock(E3DCBridgeHandler.class);
        when(bridgeHandler.isConnected()).thenReturn(true);
        CountDownLatch deviceAnswers = new CountDownLatch(1);
        CountDownLatch requestSent = new CountDownLatch(1);
        when(bridgeHandler.sendRequest(any())).thenAnswer(invocation -> {
            requestSent.countDown();
            if (!deviceAnswers.await(WAIT_SECONDS, TimeUnit.SECONDS)) {
                throw new RscpException("the test did not release the device");
            }
            return RscpValue.container(0, List.of(RscpValue.none(RscpTag.EMS_SET_POWER_SETTINGS)));
        });
        Child wallbox = new Child(E3DCBindingConstants.THING_TYPE_WALLBOX, E3DCWallboxHandler::new);
        E3DCThingHandler handler = createHandler(wallbox, bridgeHandler, callback);
        handler.initialize();
        ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(),
                E3DCBindingConstants.GROUP_WALLBOX_STATUS_CONTROL, "sun-mode");

        handler.handleCommand(channelUID, OnOffType.ON);

        // the call returned while the device has not answered yet
        assertEquals(1, deviceAnswers.getCount());
        assertTrue(requestSent.await(WAIT_SECONDS, TimeUnit.SECONDS), "the write must reach the bridge");
        verify(callback, never()).stateUpdated(eq(channelUID), any());

        deviceAnswers.countDown();

        verify(callback, timeout(WAIT_SECONDS * 1000)).stateUpdated(eq(channelUID), eq(OnOffType.ON));
        handler.dispose();
    }
}
