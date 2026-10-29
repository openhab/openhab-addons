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
package org.openhab.binding.bluetooth.airthings.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;

/**
 * Tests for {@link AbstractAirthingsHandler}.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class AbstractAirthingsHandlerTest {

    private static final ThingUID THING_UID = new ThingUID(AirthingsBindingConstants.THING_TYPE_AIRTHINGS_WAVE_PLUS,
            "test");
    private static final ThingUID BRIDGE_UID = new ThingUID("bluetooth", "bridge", "hub");
    private static final UUID DATA_UUID = UUID.randomUUID();

    private final Thing thing = mock(Thing.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final Bridge bridge = mock(Bridge.class);
    private final BluetoothAdapter adapter = mock(BluetoothAdapter.class,
            withSettings().extraInterfaces(BridgeHandler.class));
    private final BluetoothDevice device = mock(BluetoothDevice.class);
    private final BluetoothCharacteristic characteristic = new BluetoothCharacteristic(DATA_UUID, 1);

    private final AtomicReference<CompletableFuture<byte[]>> pendingRead = new AtomicReference<>();
    private final AtomicReference<ThingStatus> bridgeStatus = new AtomicReference<>(ThingStatus.ONLINE);

    private @Nullable TestAirThingsHandler handler;

    @BeforeEach
    public void setUp() {
        Map<String, Object> configuration = new HashMap<>();
        configuration.put("address", "00:11:22:33:44:55");
        configuration.put("refreshInterval", 60000);

        when(thing.getUID()).thenReturn(THING_UID);
        when(thing.getThingTypeUID()).thenReturn(AirthingsBindingConstants.THING_TYPE_AIRTHINGS_WAVE_PLUS);
        when(thing.getBridgeUID()).thenReturn(BRIDGE_UID);
        when(thing.getConfiguration()).thenReturn(new Configuration(configuration));
        when(thing.getChannels()).thenReturn(List.of());

        when(callback.getBridge(any())).thenReturn(bridge);
        when(bridge.getHandler()).thenAnswer(i -> adapter);
        when(bridge.getStatusInfo())
                .thenAnswer(i -> ThingStatusInfoBuilder.create(bridgeStatus.get(), ThingStatusDetail.NONE).build());
        when(adapter.getDevice(any())).thenReturn(device);

        when(device.getConnectionState()).thenReturn(ConnectionState.CONNECTED);
        when(device.getServices()).thenReturn(List.of());
        when(device.getCharacteristic(any())).thenReturn(characteristic);
        when(device.readCharacteristic(any())).thenAnswer(i -> {
            CompletableFuture<byte[]> future = new CompletableFuture<>();
            pendingRead.set(future);
            return future;
        });
        when(device.disconnect()).thenReturn(true);
    }

    @AfterEach
    public void tearDown() {
        TestAirThingsHandler handler = this.handler;
        if (handler != null) {
            handler.dispose();
        }
    }

    private TestAirThingsHandler createHandler() {
        TestAirThingsHandler handler = new TestAirThingsHandler(thing);
        handler.setCallback(callback);
        handler.initialize();
        this.handler = handler;
        return handler;
    }

    private void failPendingRead() {
        pendingRead.get().completeExceptionally(new RuntimeException("simulated read failure"));
    }

    private void completePendingRead(byte[] data) {
        pendingRead.get().complete(data);
    }

    @Test
    public void repeatedAsyncReadFailuresEventuallySetOffline() {
        TestAirThingsHandler handler = createHandler();

        for (int i = 0; i < 6; i++) {
            handler.onServicesDiscovered();
            failPendingRead();
        }

        verify(callback, timeout(5000)).statusUpdated(any(),
                argThat((ThingStatusInfo status) -> status.getStatus() == ThingStatus.OFFLINE
                        && status.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR));
    }

    @Test
    public void successfulReadResetsErrorCounter() {
        TestAirThingsHandler handler = createHandler();

        handler.onServicesDiscovered();
        for (int i = 0; i < 4; i++) {
            failPendingRead();
            handler.onServicesDiscovered();
        }
        completePendingRead(new byte[] { 1, 2, 3 });

        verify(callback, timeout(5000)).statusUpdated(any(),
                argThat((ThingStatusInfo status) -> status.getStatus() == ThingStatus.ONLINE));
        assertEquals(1, handler.channelsUpdateCount.get());

        for (int i = 0; i < 5; i++) {
            failPendingRead();
            handler.onServicesDiscovered();
        }
        verify(callback, never()).statusUpdated(any(),
                argThat((ThingStatusInfo status) -> status.getStatus() == ThingStatus.OFFLINE));
    }

    @Test
    public void pendingReadDoesNotPublishWhenBridgeWentOffline() {
        TestAirThingsHandler handler = createHandler();

        handler.onServicesDiscovered();
        bridgeStatus.set(ThingStatus.OFFLINE);
        clearInvocations(callback);

        completePendingRead(new byte[] { 1, 2, 3 });

        verify(callback, timeout(5000)).statusUpdated(any(),
                argThat((ThingStatusInfo status) -> status.getStatus() == ThingStatus.OFFLINE
                        && status.getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE));
        verify(callback, never()).statusUpdated(any(),
                argThat((ThingStatusInfo status) -> status.getStatus() == ThingStatus.ONLINE));
        verify(callback, never()).stateUpdated(any(), any());
        assertEquals(0, handler.channelsUpdateCount.get());
        verify(device, atLeastOnce()).disconnect();
    }

    private static final class TestAirThingsHandler extends AbstractAirthingsHandler {

        private final AtomicInteger channelsUpdateCount = new AtomicInteger();

        private TestAirThingsHandler(Thing thing) {
            super(thing);
        }

        @Override
        protected UUID getDataUUID() {
            return DATA_UUID;
        }

        @Override
        protected void updateChannels(int[] data) {
            channelsUpdateCount.incrementAndGet();
        }
    }
}
