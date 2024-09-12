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
package org.openhab.binding.boschshc.internal.devices.thermostat;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openhab.binding.boschshc.internal.devices.AbstractBatteryPoweredDeviceHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.childlock.dto.ChildLockServiceState;
import org.openhab.binding.boschshc.internal.services.childlock.dto.ChildLockState;
import org.openhab.binding.boschshc.internal.services.silentmode.SilentModeState;
import org.openhab.binding.boschshc.internal.services.silentmode.dto.SilentModeServiceState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit Tests for {@link ThermostatHandler}.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class ThermostatHandlerTest extends AbstractBatteryPoweredDeviceHandlerTest<ThermostatHandler> {

    private @Captor @NonNullByDefault({}) ArgumentCaptor<ChildLockServiceState> childLockServiceStateCaptor;

    private @Captor @NonNullByDefault({}) ArgumentCaptor<SilentModeServiceState> silentModeServiceStateCaptor;

    @Override
    protected ThermostatHandler createFixture() {
        return new ThermostatHandler(getThing());
    }

    @Override
    protected String getDeviceID() {
        return "hdm:ZigBee:000d6f0017f1ace2";
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_THERMOSTAT;
    }

    @Test
    void testHandleCommandChildLockService()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CHILD_LOCK),
                OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("Thermostat"), childLockServiceStateCaptor.capture());
        ChildLockServiceState state = childLockServiceStateCaptor.getValue();
        assertSame(ChildLockState.ON, state.childLock);
    }

    @Test
    void testHandleCommandSilentModeService()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SILENT_MODE),
                OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("SilentMode"),
                silentModeServiceStateCaptor.capture());
        SilentModeServiceState state = silentModeServiceStateCaptor.getValue();
        assertSame(SilentModeState.MODE_SILENT, state.mode);
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
    void testHandleCommandUnknownCommandSilentModeService() {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SILENT_MODE),
                new DecimalType(42));
        ThingStatusInfo expectedThingStatusInfo = ThingStatusInfoBuilder
                .create(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR)
                .withDescription(
                        "Error when service SilentMode should handle command org.openhab.core.library.types.DecimalType: SilentMode: Can not handle command org.openhab.core.library.types.DecimalType")
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
        JsonElement jsonObject = JsonParser
                .parseString("{\n" + "   \"@type\": \"valveTappetState\",\n" + "   \"position\": 42\n" + " }");
        getFixture().processUpdate("ValveTappet", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_VALVE_TAPPET_POSITION),
                new DecimalType(42));
    }

    @Test
    void testUpdateChannelsChildLockService() {
        JsonElement jsonObject = JsonParser
                .parseString("{\n" + "   \"@type\": \"childLockState\",\n" + "   \"childLock\": \"ON\"\n" + " }");
        getFixture().processUpdate("Thermostat", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CHILD_LOCK), OnOffType.ON);
    }

    @Test
    void testUpdateChannelsSilentModeService() {
        JsonElement jsonObject = JsonParser.parseString("{\"@type\": \"silentModeState\", \"mode\": \"MODE_SILENT\"}");
        getFixture().processUpdate("SilentMode", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SILENT_MODE), OnOffType.ON);
    }

    @Test
    void testUpdateChannelsSilentModeServiceNormal() {
        JsonElement jsonObject = JsonParser.parseString("{\"@type\": \"silentModeState\", \"mode\": \"MODE_NORMAL\"}");
        getFixture().processUpdate("SilentMode", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SILENT_MODE), OnOffType.OFF);
    }
}
