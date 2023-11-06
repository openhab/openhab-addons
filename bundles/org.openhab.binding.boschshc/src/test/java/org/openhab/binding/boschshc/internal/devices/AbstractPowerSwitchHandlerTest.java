/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.powermeter.dto.PowerMeterServiceState;
import org.openhab.binding.boschshc.internal.services.powerswitch.PowerSwitchState;
import org.openhab.binding.boschshc.internal.services.powerswitch.dto.PowerSwitchServiceState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.RefreshType;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Abstract unit test implementation for devices with power switches and energy monitoring.
 *
 * @author David Pace - Initial contribution
 *
 * @param <T> type of the handler to be tested
 */
@NonNullByDefault
public abstract class AbstractPowerSwitchHandlerTest<T extends AbstractPowerSwitchHandler>
        extends AbstractBoschSHCDeviceHandlerTest<T> {

    private @Captor @NonNullByDefault({}) ArgumentCaptor<PowerSwitchServiceState> serviceStateCaptor;

    private @Captor @NonNullByDefault({}) ArgumentCaptor<QuantityType<Power>> powerCaptor;

    private @Captor @NonNullByDefault({}) ArgumentCaptor<QuantityType<Energy>> energyCaptor;

    @BeforeEach
    @Override
    public void beforeEach() throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        super.beforeEach();

        PowerSwitchServiceState powerSwitchServiceState = new PowerSwitchServiceState();
        powerSwitchServiceState.switchState = PowerSwitchState.ON;
        lenient().when(bridgeHandler.getState(anyString(), eq("PowerSwitch"), same(PowerSwitchServiceState.class)))
                .thenReturn(powerSwitchServiceState);

        PowerMeterServiceState powerMeterServiceState = new PowerMeterServiceState();
        powerMeterServiceState.powerConsumption = 12.34d;
        powerMeterServiceState.energyConsumption = 56.78d;
        lenient().when(bridgeHandler.getState(anyString(), eq("PowerMeter"), same(PowerMeterServiceState.class)))
                .thenReturn(powerMeterServiceState);
    }

    @Test
    public void testHandleCommandPowerSwitchChannel()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH), OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("PowerSwitch"), serviceStateCaptor.capture());
        PowerSwitchServiceState state = serviceStateCaptor.getValue();
        assertSame(PowerSwitchState.ON, state.switchState);

        getFixture().handleCommand(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH), OnOffType.OFF);
        verify(getBridgeHandler(), times(2)).putState(eq(getDeviceID()), eq("PowerSwitch"),
                serviceStateCaptor.capture());
        state = serviceStateCaptor.getValue();
        assertSame(PowerSwitchState.OFF, state.switchState);
    }

    @Test
    public void testUpdateChannelPowerSwitchState() {
        JsonElement jsonObject = JsonParser
                .parseString("{\n" + "  \"@type\": \"powerSwitchState\",\n" + "  \"switchState\": \"ON\"\n" + "}");
        getFixture().processUpdate("PowerSwitch", jsonObject);
        verify(getCallback()).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH), OnOffType.ON);

        jsonObject = JsonParser
                .parseString("{\n" + "  \"@type\": \"powerSwitchState\",\n" + "  \"switchState\": \"OFF\"\n" + "}");
        getFixture().processUpdate("PowerSwitch", jsonObject);
        verify(getCallback()).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH), OnOffType.OFF);
    }

    @Test
    public void testUpdateChannelPowerMeterServiceState() {
        JsonElement jsonObject = JsonParser.parseString("""
                {
                  "@type": "powerMeterState",
                  "powerConsumption": "23",
                  "energyConsumption": 42
                }\
                """);
        getFixture().processUpdate("PowerMeter", jsonObject);

        verify(getCallback()).stateUpdated(eq(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_CONSUMPTION)),
                powerCaptor.capture());
        QuantityType<Power> powerValue = powerCaptor.getValue();
        assertEquals(23, powerValue.intValue());

        verify(getCallback()).stateUpdated(eq(getChannelUID(BoschSHCBindingConstants.CHANNEL_ENERGY_CONSUMPTION)),
                energyCaptor.capture());
        QuantityType<Energy> energyValue = energyCaptor.getValue();
        assertEquals(42, energyValue.intValue());
    }

    @Test
    public void testHandleCommandRefreshPowerSwitchChannel() {
        getFixture().handleCommand(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH), RefreshType.REFRESH);
        verify(getCallback()).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH), OnOffType.ON);
    }

    @Test
    public void testHandleCommandRefreshPowerConsumptionChannel() {
        getFixture().handleCommand(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_CONSUMPTION),
                RefreshType.REFRESH);
        verify(getCallback()).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_CONSUMPTION),
                new QuantityType<Power>(12.34d, Units.WATT));
    }

    @Test
    public void testHandleCommandRefreshEnergyConsumptionChannel() {
        getFixture().handleCommand(getChannelUID(BoschSHCBindingConstants.CHANNEL_ENERGY_CONSUMPTION),
                RefreshType.REFRESH);
        verify(getCallback()).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_ENERGY_CONSUMPTION),
                new QuantityType<Energy>(56.78d, Units.WATT_HOUR));
    }
}
