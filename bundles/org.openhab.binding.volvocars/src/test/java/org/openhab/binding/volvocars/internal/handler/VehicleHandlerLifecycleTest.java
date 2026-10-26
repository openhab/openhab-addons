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
package org.openhab.binding.volvocars.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.volvocars.internal.VolvoCarsBindingConstants.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.volvocars.internal.api.VolvoCarsApi;
import org.openhab.binding.volvocars.internal.api.VolvoCarsException;
import org.openhab.binding.volvocars.internal.api.dto.CommandResult;
import org.openhab.binding.volvocars.internal.api.dto.Vehicle;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.RefreshType;

/**
 * Tests the lifecycle of the {@link VehicleHandler}, i.e. commands and polls across reconfiguration and
 * reauthorization.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class VehicleHandlerLifecycleTest {
    private static final String VIN = "YV1TESTVIN0000001";
    private static final String OTHER_VIN = "YV1TESTVIN0000002";
    private static final long TIMEOUT_MS = 10000;

    private final ThingUID bridgeUID = new ThingUID(THING_TYPE_ACCOUNT, "account");
    private final Thing thing = ThingBuilder.create(THING_TYPE_VEHICLE, new ThingUID(THING_TYPE_VEHICLE, "car"))
            .withBridge(bridgeUID).withConfiguration(new Configuration(Map.of("vin", VIN))).build();
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final AccountHandler accountHandler = mock(AccountHandler.class);
    private final VolvoCarsApi api = mock(VolvoCarsApi.class);
    private final VehicleHandler handler = new VehicleHandler(thing);

    @BeforeEach
    void setUp() throws VolvoCarsException {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getHandler()).thenReturn(accountHandler);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(callback.getBridge(bridgeUID)).thenReturn(bridge);
        when(accountHandler.getApi()).thenReturn(api);
        when(api.executeCommand(anyString(), anyString(), any())).thenReturn(new CommandResult());
        handler.setCallback(callback);
    }

    @AfterEach
    void tearDown() {
        handler.dispose();
    }

    @Test
    void commandQueuedBeforeReconfigurationIsNotSent() throws Exception {
        when(api.getVehicle(anyString())).thenThrow(new VolvoCarsException("Not Found", 404, false));
        CountDownLatch commandWaiting = new CountDownLatch(1);
        CountDownLatch releaseCommand = new CountDownLatch(1);
        AtomicBoolean blockNextApiAccess = new AtomicBoolean();
        when(accountHandler.getApi()).thenAnswer(invocation -> {
            if (blockNextApiAccess.getAndSet(false)) {
                commandWaiting.countDown();
                releaseCommand.await(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            }
            return api;
        });
        handler.initialize();
        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(eq(thing),
                argThat(info -> info.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR));

        // hold the command task before it sends the command, then switch the Thing to another vehicle
        blockNextApiAccess.set(true);
        handler.handleCommand(new ChannelUID(thing.getUID(), GROUP_DOORS, CHANNEL_LOCK), OnOffType.OFF);
        assertTrue(commandWaiting.await(TIMEOUT_MS, TimeUnit.MILLISECONDS));
        thing.getConfiguration().put("vin", OTHER_VIN);
        handler.thingUpdated(thing);
        releaseCommand.countDown();

        verify(api, after(1000).never()).executeCommand(anyString(), anyString(), any());

        // commands issued after the reconfiguration are sent to the new vehicle
        handler.handleCommand(new ChannelUID(thing.getUID(), GROUP_DOORS, CHANNEL_LOCK), OnOffType.OFF);
        verify(api, timeout(TIMEOUT_MS)).executeCommand(eq(OTHER_VIN), eq("unlock"), isNull());
    }

    @Test
    void energyCapabilitiesAreRetriedIfUnknown() throws Exception {
        Vehicle vehicle = new Vehicle();
        vehicle.fuelType = "ELECTRIC";
        when(api.getVehicle(VIN)).thenReturn(vehicle);
        when(api.getSupportedEnergyCapabilities(VIN)).thenThrow(new VolvoCarsException("Forbidden", 403, false))
                .thenReturn(Set.of("batteryChargeLevel"));

        handler.initialize();
        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(eq(thing),
                argThat(info -> info.getStatus() == ThingStatus.ONLINE));
        verify(api, never()).getEnergyState(anyString());

        // e.g. after the account was authorized again with the energy scope
        handler.handleCommand(new ChannelUID(thing.getUID(), GROUP_BATTERY, CHANNEL_BATTERY_LEVEL),
                RefreshType.REFRESH);

        verify(api, timeout(TIMEOUT_MS)).getEnergyState(VIN);
    }
}
