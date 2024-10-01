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
package org.openhab.binding.boschshc.internal.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.powermeter.dto.PowerMeterServiceState;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.RefreshType;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Abstract unit test implementation for power switch handler with power meter support.
 * 
 * @author David Pace - Initial contribution
 *
 * @param <T> type of the handler to be tested
 */
@NonNullByDefault
public abstract class AbstractPowerSwitchHandlerWithPowerMeterTest<T extends AbstractPowerSwitchHandlerWithPowerMeter>
        extends AbstractPowerSwitchHandlerTest<T> {

    private @Captor @NonNullByDefault({}) ArgumentCaptor<QuantityType<Power>> powerCaptor;

    private @Captor @NonNullByDefault({}) ArgumentCaptor<QuantityType<Energy>> energyCaptor;

    @BeforeEach
    public void beforeEach(TestInfo testInfo)
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        PowerMeterServiceState powerMeterServiceState = new PowerMeterServiceState();
        powerMeterServiceState.powerConsumption = 12.34d;
        powerMeterServiceState.energyConsumption = 56.78d;
        lenient().when(getBridgeHandler().getState(anyString(), eq("PowerMeter"), same(PowerMeterServiceState.class)))
                .thenReturn(powerMeterServiceState);

        super.beforeEach(testInfo);
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

        // state is updated twice: via short poll in initialize() and via long poll result in this test
        verify(getCallback(), times(2)).stateUpdated(
                eq(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_CONSUMPTION)), powerCaptor.capture());
        QuantityType<Power> powerValue = powerCaptor.getValue();
        assertEquals(23, powerValue.intValue());

        // state is updated twice: via short poll in initialize() and via long poll result in this test
        verify(getCallback(), times(2)).stateUpdated(
                eq(getChannelUID(BoschSHCBindingConstants.CHANNEL_ENERGY_CONSUMPTION)), energyCaptor.capture());
        QuantityType<Energy> energyValue = energyCaptor.getValue();
        assertEquals(42, energyValue.intValue());
    }

    @Test
    public void testHandleCommandRefreshPowerConsumptionChannel() {
        getFixture().handleCommand(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_CONSUMPTION),
                RefreshType.REFRESH);

        // state is updated twice: via short poll in initialize() and via refresh command in this test
        verify(getCallback(), times(2)).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_CONSUMPTION),
                new QuantityType<>(12.34d, Units.WATT));
    }

    @Test
    public void testHandleCommandRefreshEnergyConsumptionChannel() {
        getFixture().handleCommand(getChannelUID(BoschSHCBindingConstants.CHANNEL_ENERGY_CONSUMPTION),
                RefreshType.REFRESH);

        // state is updated twice: via short poll in initialize() and via refresh command in this test
        verify(getCallback(), times(2)).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_ENERGY_CONSUMPTION),
                new QuantityType<>(56.78d, Units.WATT_HOUR));
    }
}
