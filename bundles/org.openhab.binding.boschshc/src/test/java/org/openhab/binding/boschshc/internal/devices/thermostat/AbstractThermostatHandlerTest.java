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
package org.openhab.binding.boschshc.internal.devices.thermostat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openhab.binding.boschshc.internal.devices.AbstractBatteryPoweredDeviceHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.services.childlock.dto.ChildLockServiceState;
import org.openhab.binding.boschshc.internal.services.childlock.dto.ChildLockState;
import org.openhab.binding.boschshc.internal.services.temperatureoffset.TemperatureOffsetService;
import org.openhab.binding.boschshc.internal.services.temperatureoffset.dto.TemperatureOffsetServiceState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link AbstractThermostatHandler}.
 * 
 * @author David Pace - Initial contribution
 * 
 */
@NonNullByDefault
public abstract class AbstractThermostatHandlerTest<T extends AbstractThermostatHandler>
        extends AbstractBatteryPoweredDeviceHandlerTest<T> {

    private @Captor @NonNullByDefault({}) ArgumentCaptor<ChildLockServiceState> childLockServiceStateCaptor;
    private @Captor @NonNullByDefault({}) ArgumentCaptor<TemperatureOffsetServiceState> temperatureOffsetCaptor;

    @Test
    void testHandleCommandChildLockService() throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CHILD_LOCK),
                OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("Thermostat"), childLockServiceStateCaptor.capture());
        ChildLockServiceState state = childLockServiceStateCaptor.getValue();
        assertSame(ChildLockState.ON, state.childLock);
    }

    @Test
    void testHandleCommandUnknownCommandChildLockService() {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CHILD_LOCK),
                new DecimalType(42));
        ThingStatusInfo expectedThingStatusInfo = ThingStatusInfoBuilder
                .create(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR)
                .withDescription(
                        "Error when service Thermostat should handle command org.openhab.core.library.types.DecimalType: Thermostat: Can not handle command org.openhab.core.library.types.DecimalType")
                .build();
        verify(getCallback()).statusUpdated(getThing(), expectedThingStatusInfo);
    }

    @Test
    void testUpdateChannelsTemperatureLevelService() {
        JsonElement jsonObject = JsonParser.parseString("""
                {
                   "@type": "temperatureLevelState",
                   "temperature": 21.5
                }\
                """);
        getFixture().processUpdate("TemperatureLevel", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_TEMPERATURE),
                new QuantityType<>(21.5, SIUnits.CELSIUS));
    }

    @Test
    void testUpdateChannelsValveTappetService() {
        JsonElement jsonObject = JsonParser.parseString("""
                {
                    "@type": "valveTappetState",
                    "position": 42
                }\
                """);
        getFixture().processUpdate("ValveTappet", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_VALVE_TAPPET_POSITION),
                new DecimalType(42));
    }

    @Test
    void testUpdateChannelsChildLockService() {
        JsonElement jsonObject = JsonParser.parseString("""
                {
                    "@type": "childLockState",
                    "childLock": "ON"
                }\
                """);
        getFixture().processUpdate("Thermostat", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CHILD_LOCK), OnOffType.ON);
    }

    @Test
    void testUpdateChannelsTemperatureOffsetService() {
        JsonElement jsonObject = JsonParser.parseString("""
                {
                    "@type": "temperatureOffsetState",
                    "offset": 1.3,
                    "stepSize": 0.1,
                    "minOffset": -5.0,
                    "maxOffset": 5.0
                }\
                """);
        getFixture().processUpdate(TemperatureOffsetService.TEMPERATURE_OFFSET_SERVICE_NAME, jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_TEMPERATURE_OFFSET),
                new QuantityType<>(1.3d, SIUnits.CELSIUS));
    }

    @Test
    void testHandleCommandTemperatureOffsetServiceQuantityTypeCelsius()
            throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_TEMPERATURE_OFFSET),
                new QuantityType<>(-1.3d, SIUnits.CELSIUS));
        verify(getBridgeHandler()).putState(eq(getDeviceID()),
                eq(TemperatureOffsetService.TEMPERATURE_OFFSET_SERVICE_NAME), temperatureOffsetCaptor.capture());
        TemperatureOffsetServiceState state = temperatureOffsetCaptor.getValue();
        assertNull(state.minOffset);
        assertNull(state.maxOffset);
        assertNull(state.stepSize);
        assertEquals(-1.3d, state.offset, 0.001d);
    }

    @Test
    void testHandleCommandTemperatureOffsetServiceQuantityTypeCelsiusExceedMaxOffset()
            throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_TEMPERATURE_OFFSET),
                new QuantityType<>(6d, SIUnits.CELSIUS));
        verify(getBridgeHandler()).putState(eq(getDeviceID()),
                eq(TemperatureOffsetService.TEMPERATURE_OFFSET_SERVICE_NAME), temperatureOffsetCaptor.capture());
        TemperatureOffsetServiceState state = temperatureOffsetCaptor.getValue();
        assertNull(state.minOffset);
        assertNull(state.maxOffset);
        assertNull(state.stepSize);
        assertEquals(5d, state.offset, 0.001d);
    }

    @Test
    void testHandleCommandTemperatureOffsetServiceQuantityTypeFahrenheit()
            throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_TEMPERATURE_OFFSET),
                new QuantityType<>(-1.3d, ImperialUnits.FAHRENHEIT));
        verify(getBridgeHandler()).putState(eq(getDeviceID()),
                eq(TemperatureOffsetService.TEMPERATURE_OFFSET_SERVICE_NAME), temperatureOffsetCaptor.capture());
        TemperatureOffsetServiceState state = temperatureOffsetCaptor.getValue();
        assertNull(state.minOffset);
        assertNull(state.maxOffset);
        assertNull(state.stepSize);
        assertEquals(-0.7d, state.offset, 0.001d);
    }

    @Test
    void testHandleCommandTemperatureOffsetServiceQuantityTypeFahrenheitExceedMaxOffset()
            throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_TEMPERATURE_OFFSET),
                new QuantityType<>(10d, ImperialUnits.FAHRENHEIT));
        verify(getBridgeHandler()).putState(eq(getDeviceID()),
                eq(TemperatureOffsetService.TEMPERATURE_OFFSET_SERVICE_NAME), temperatureOffsetCaptor.capture());
        TemperatureOffsetServiceState state = temperatureOffsetCaptor.getValue();
        assertNull(state.minOffset);
        assertNull(state.maxOffset);
        assertNull(state.stepSize);
        assertEquals(5d, state.offset, 0.001d);
    }

    @Test
    void testHandleCommandTemperatureOffsetServiceQuantityTypeKelvin()
            throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_TEMPERATURE_OFFSET),
                new QuantityType<>(-1.3d, Units.KELVIN));
        verify(getBridgeHandler()).putState(eq(getDeviceID()),
                eq(TemperatureOffsetService.TEMPERATURE_OFFSET_SERVICE_NAME), temperatureOffsetCaptor.capture());
        TemperatureOffsetServiceState state = temperatureOffsetCaptor.getValue();
        assertNull(state.minOffset);
        assertNull(state.maxOffset);
        assertNull(state.stepSize);
        assertEquals(-1.3d, state.offset, 0.001d);
    }

    @Test
    void testHandleCommandTemperatureOffsetServiceQuantityTypeDimensionless()
            throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_TEMPERATURE_OFFSET),
                new QuantityType<>(-1.3d, Units.ONE));
        verify(getBridgeHandler(), times(0)).putState(eq(getDeviceID()),
                eq(TemperatureOffsetService.TEMPERATURE_OFFSET_SERVICE_NAME), any());
    }

    @Test
    void testHandleCommandTemperatureOffsetServiceDecimalType()
            throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_TEMPERATURE_OFFSET),
                new DecimalType(-1.3d));
        verify(getBridgeHandler()).putState(eq(getDeviceID()),
                eq(TemperatureOffsetService.TEMPERATURE_OFFSET_SERVICE_NAME), temperatureOffsetCaptor.capture());
        TemperatureOffsetServiceState state = temperatureOffsetCaptor.getValue();
        assertNull(state.minOffset);
        assertNull(state.maxOffset);
        assertNull(state.stepSize);
        assertEquals(-1.3d, state.offset, 0.001d);
    }

    @Test
    void testHandleCommandTemperatureOffsetServiceInvalidCommand()
            throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_TEMPERATURE_OFFSET), OnOffType.ON);
        verify(getBridgeHandler(), times(0)).putState(eq(getDeviceID()),
                eq(TemperatureOffsetService.TEMPERATURE_OFFSET_SERVICE_NAME), any());
    }
}
