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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.volvocars.internal.api.VolvoCarsApi;
import org.openhab.binding.volvocars.internal.api.VolvoCarsException;
import org.openhab.binding.volvocars.internal.api.dto.CommandResult;
import org.openhab.binding.volvocars.internal.api.dto.ValueField;
import org.openhab.binding.volvocars.internal.api.dto.Vehicle;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonPrimitive;

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
    void stateReadBeforeReconfigurationIsNotPublished() throws Exception {
        Vehicle vehicle = new Vehicle();
        vehicle.fuelType = "PETROL";
        when(api.getVehicle(anyString())).thenReturn(vehicle);
        ValueField locked = new ValueField();
        locked.value = new JsonPrimitive("LOCKED");
        CountDownLatch responseReceived = new CountDownLatch(1);
        CountDownLatch releaseResponse = new CountDownLatch(1);
        when(api.getValues(VIN, "doors")).thenAnswer(invocation -> {
            responseReceived.countDown();
            // the response has already arrived, so canceling the poll job does not stop the poll
            boolean interrupted = false;
            while (true) {
                try {
                    releaseResponse.await(TIMEOUT_MS, TimeUnit.MILLISECONDS);
                    break;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
            return Map.of("centralLock", locked);
        });
        ChannelUID lockChannel = new ChannelUID(thing.getUID(), GROUP_DOORS, CHANNEL_LOCK);

        handler.initialize();
        assertTrue(responseReceived.await(TIMEOUT_MS, TimeUnit.MILLISECONDS));
        thing.getConfiguration().put("vin", OTHER_VIN);
        handler.thingUpdated(thing);
        releaseResponse.countDown();

        // the poll of the new vehicle publishes its state, the response for the previous vehicle is discarded
        verify(api, timeout(TIMEOUT_MS)).getValues(OTHER_VIN, "doors");
        verify(callback, timeout(TIMEOUT_MS)).stateUpdated(lockChannel, UnDefType.UNDEF);
        verify(callback, after(1000).never()).stateUpdated(lockChannel, OnOffType.ON);
    }

    @Test
    void channelsAreAddedBackWhenTheVinChanges() throws Exception {
        // a subset of the defined channels, as core creates them from the channel group types
        Map<String, List<String>> definedChannels = Map.of(GROUP_FUEL, List.of(CHANNEL_FUEL_AMOUNT), GROUP_BATTERY,
                List.of(CHANNEL_BATTERY_LEVEL, CHANNEL_CHARGING_STATUS));
        when(callback.createChannelBuilders(any(), any())).thenAnswer(invocation -> {
            ChannelGroupUID groupUID = invocation.getArgument(0);
            return definedChannels.getOrDefault(groupUID.getId(), List.of()).stream()
                    .map(id -> ChannelBuilder.create(new ChannelUID(groupUID, id), "Number")).toList();
        });
        Thing localThing = ThingBuilder.create(THING_TYPE_VEHICLE, thing.getUID()).withBridge(bridgeUID)
                .withConfiguration(new Configuration(Map.of("vin", VIN)))
                .withChannels(
                        definedChannels.entrySet().stream()
                                .flatMap(group -> group.getValue().stream().map(id -> ChannelBuilder
                                        .create(new ChannelUID(thing.getUID(), group.getKey(), id), "Number").build()))
                                .toList())
                .build();
        VehicleHandler localHandler = new VehicleHandler(localThing);
        localHandler.setCallback(callback);
        Vehicle petrol = new Vehicle();
        petrol.fuelType = "PETROL";
        Vehicle electric = new Vehicle();
        electric.fuelType = "ELECTRIC";
        when(api.getVehicle(VIN)).thenReturn(petrol);
        when(api.getVehicle(OTHER_VIN)).thenReturn(electric);
        when(api.getSupportedEnergyCapabilities(OTHER_VIN)).thenReturn(Set.of("batteryChargeLevel", "chargingStatus"));
        try {
            localHandler.initialize();
            verify(callback, timeout(TIMEOUT_MS).times(1)).statusUpdated(any(),
                    argThat(info -> info.getStatus() == ThingStatus.ONLINE));
            assertEquals(Set.of(CHANNEL_FUEL_AMOUNT), channelIds(localHandler));

            // the Thing is reconfigured to an electric vehicle
            Thing petrolThing = localHandler.getThing();
            petrolThing.getConfiguration().put("vin", OTHER_VIN);
            localHandler.thingUpdated(petrolThing);
            verify(callback, timeout(TIMEOUT_MS).times(2)).statusUpdated(any(),
                    argThat(info -> info.getStatus() == ThingStatus.ONLINE));
            assertEquals(Set.of(CHANNEL_BATTERY_LEVEL, CHANNEL_CHARGING_STATUS), channelIds(localHandler));

            // the Thing is only updated if its channels or properties change
            clearInvocations(callback);
            localHandler.thingUpdated(localHandler.getThing());
            verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(),
                    argThat(info -> info.getStatus() == ThingStatus.ONLINE));
            verify(callback, never()).thingUpdated(any());
        } finally {
            localHandler.dispose();
        }
    }

    private static Set<String> channelIds(VehicleHandler localHandler) {
        return localHandler.getThing().getChannels().stream().map(channel -> channel.getUID().getIdWithoutGroup())
                .collect(Collectors.toSet());
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
