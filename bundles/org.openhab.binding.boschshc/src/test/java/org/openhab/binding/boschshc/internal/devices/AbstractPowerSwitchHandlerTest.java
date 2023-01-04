/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.powerswitch.PowerSwitchState;
import org.openhab.binding.boschshc.internal.services.powerswitch.dto.PowerSwitchServiceState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Abstract unit test implementation for devices with power switches and energy monitoring.
 *
 * @author David Pace - Initial contribution
 *
 * @param <T> type of the handler to be tested
 */
public abstract class AbstractPowerSwitchHandlerTest<T extends AbstractPowerSwitchHandler>
        extends AbstractBoschSHCDeviceHandlerTest<T> {

    @Captor
    private ArgumentCaptor<PowerSwitchServiceState> serviceStateCaptor;

    @Captor
    private ArgumentCaptor<QuantityType<Power>> powerCaptor;

    @Captor
    private ArgumentCaptor<QuantityType<Energy>> energyCaptor;

    @Test
    public void testHandleCommand()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {

        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_POWER_SWITCH),
                OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("PowerSwitch"), serviceStateCaptor.capture());
        PowerSwitchServiceState state = serviceStateCaptor.getValue();
        assertSame(PowerSwitchState.ON, state.switchState);

        getFixture().handleCommand(new ChannelUID(new ThingUID(getThingTypeUID(), "abcdef"),
                BoschSHCBindingConstants.CHANNEL_POWER_SWITCH), OnOffType.OFF);
        verify(getBridgeHandler(), times(2)).putState(eq(getDeviceID()), eq("PowerSwitch"),
                serviceStateCaptor.capture());
        state = serviceStateCaptor.getValue();
        assertSame(PowerSwitchState.OFF, state.switchState);
    }

    @Test
    public void testUpdateChannel_PowerSwitchState() {
        JsonElement jsonObject = JsonParser
                .parseString("{\n" + "  \"@type\": \"powerSwitchState\",\n" + "  \"switchState\": \"ON\"\n" + "}");
        getFixture().processUpdate("PowerSwitch", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_POWER_SWITCH), OnOffType.ON);

        jsonObject = JsonParser
                .parseString("{\n" + "  \"@type\": \"powerSwitchState\",\n" + "  \"switchState\": \"OFF\"\n" + "}");
        getFixture().processUpdate("PowerSwitch", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_POWER_SWITCH), OnOffType.OFF);
    }

    @Test
    public void testUpdateChannel_PowerMeterServiceState() {
        JsonElement jsonObject = JsonParser.parseString("{\n" + "  \"@type\": \"powerMeterState\",\n"
                + "  \"powerConsumption\": \"23\",\n" + "  \"energyConsumption\": 42\n" + "}");
        getFixture().processUpdate("PowerMeter", jsonObject);

        verify(getCallback()).stateUpdated(
                eq(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_POWER_CONSUMPTION)),
                powerCaptor.capture());
        QuantityType<Power> powerValue = powerCaptor.getValue();
        assertEquals(23, powerValue.intValue());

        verify(getCallback()).stateUpdated(
                eq(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_ENERGY_CONSUMPTION)),
                energyCaptor.capture());
        QuantityType<Energy> energyValue = energyCaptor.getValue();
        assertEquals(42, energyValue.intValue());
    }
}
