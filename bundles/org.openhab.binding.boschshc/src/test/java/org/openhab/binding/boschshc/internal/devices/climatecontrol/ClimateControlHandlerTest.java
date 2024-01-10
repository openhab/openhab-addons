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
package org.openhab.binding.boschshc.internal.devices.climatecontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openhab.binding.boschshc.internal.devices.AbstractBoschSHCDeviceHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.roomclimatecontrol.dto.RoomClimateControlServiceState;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link ClimateControlHandler}.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class ClimateControlHandlerTest extends AbstractBoschSHCDeviceHandlerTest<ClimateControlHandler> {

    private @Captor @NonNullByDefault({}) ArgumentCaptor<RoomClimateControlServiceState> roomClimateControlServiceStateCaptor;

    @Override
    protected String getDeviceID() {
        return "hdm:ZigBee:abcd6fc012ad25b1";
    }

    @Override
    protected ClimateControlHandler createFixture() {
        return new ClimateControlHandler(getThing());
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_CLIMATE_CONTROL;
    }

    @Test
    void testHandleCommandRoomClimateControlService()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        QuantityType<Temperature> temperature = new QuantityType<>(21.5, SIUnits.CELSIUS);
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SETPOINT_TEMPERATURE),
                temperature);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("RoomClimateControl"),
                roomClimateControlServiceStateCaptor.capture());
        RoomClimateControlServiceState state = roomClimateControlServiceStateCaptor.getValue();
        assertEquals(temperature, state.getSetpointTemperatureState());
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
    void testUpdateChannelsRoomClimateControlService() {
        JsonElement jsonObject = JsonParser.parseString("""
                {
                   "@type": "climateControlState",
                   "setpointTemperature": 21.5
                 }\
                """);
        getFixture().processUpdate("RoomClimateControl", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SETPOINT_TEMPERATURE),
                new QuantityType<>(21.5, SIUnits.CELSIUS));
    }
}
