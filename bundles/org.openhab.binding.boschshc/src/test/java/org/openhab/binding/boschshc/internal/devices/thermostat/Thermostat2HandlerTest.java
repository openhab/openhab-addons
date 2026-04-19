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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.services.displaydirection.dto.DisplayDirectionServiceState;
import org.openhab.binding.boschshc.internal.services.displaydirection.dto.DisplayDirectionState;
import org.openhab.binding.boschshc.internal.services.displayedtemperatureconfiguration.dto.DisplayedTemperatureConfigurationServiceState;
import org.openhab.binding.boschshc.internal.services.displayedtemperatureconfiguration.dto.DisplayedTemperatureState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link Thermostat2Handler}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class Thermostat2HandlerTest extends AbstractThermostatHandlerTest<Thermostat2Handler> {

    private @Captor @NonNullByDefault({}) ArgumentCaptor<DisplayDirectionServiceState> displayDirectionCaptor;
    private @Captor @NonNullByDefault({}) ArgumentCaptor<DisplayedTemperatureConfigurationServiceState> temperatureConfigurationCaptor;

    @Override
    protected Thermostat2Handler createFixture() {
        return new Thermostat2Handler(getThing());
    }

    @Override
    protected String getDeviceID() {
        return "hdm:ZigBee:000d6f0017f1ace2";
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_THERMOSTAT_2;
    }

    @Test
    void testUpdateChannelsDisplayDirectionService() {
        JsonElement jsonObject = JsonParser.parseString("""
                {
                    "@type": "displayDirectionState",
                    "direction": "REVERSED"
                }\
                """);
        getFixture().processUpdate("DisplayDirection", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_DISPLAY_DIRECTION), OnOffType.ON);
    }

    @Test
    void testHandleCommandDisplayDirectionService() throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_DISPLAY_DIRECTION), OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("DisplayDirection"),
                displayDirectionCaptor.capture());
        DisplayDirectionServiceState state = displayDirectionCaptor.getValue();
        assertSame(DisplayDirectionState.REVERSED, state.direction);
    }

    @Test
    void testHandleCommandDisplayDirectionServiceInvalidCommand()
            throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_DISPLAY_DIRECTION),
                DecimalType.ZERO);
        verify(getBridgeHandler(), times(0)).putState(eq(getDeviceID()), eq("DisplayDirection"), any());
    }

    @Test
    void testUpdateChannelsDisplayedTemperatureConfigurationService() {
        JsonElement jsonObject = JsonParser.parseString("""
                {
                    "@type": "displayedTemperatureConfigurationState",
                    "displayedTemperature": "SETPOINT"
                }\
                """);
        getFixture().processUpdate("DisplayedTemperatureConfiguration", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_DISPLAYED_TEMPERATURE),
                OnOffType.ON);
    }

    @Test
    void testHandleCommandDisplayedTemperatureConfigurationService()
            throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_DISPLAYED_TEMPERATURE),
                OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("DisplayedTemperatureConfiguration"),
                temperatureConfigurationCaptor.capture());
        DisplayedTemperatureConfigurationServiceState state = temperatureConfigurationCaptor.getValue();
        assertSame(DisplayedTemperatureState.SETPOINT, state.displayedTemperature);
    }

    @Test
    void testHandleCommandDisplayedTemperatureConfigurationServiceInvalidCommand()
            throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_DISPLAYED_TEMPERATURE),
                DecimalType.ZERO);
        verify(getBridgeHandler(), times(0)).putState(eq(getDeviceID()), eq("DisplayedTemperatureConfiguration"),
                any());
    }
}
