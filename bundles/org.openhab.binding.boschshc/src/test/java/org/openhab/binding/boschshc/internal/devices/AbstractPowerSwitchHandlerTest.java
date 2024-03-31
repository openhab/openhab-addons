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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.powerswitch.PowerSwitchState;
import org.openhab.binding.boschshc.internal.services.powerswitch.dto.PowerSwitchServiceState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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

    @BeforeEach
    @Override
    public void beforeEach() throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        PowerSwitchServiceState powerSwitchServiceState = new PowerSwitchServiceState();
        powerSwitchServiceState.switchState = PowerSwitchState.ON;
        when(getBridgeHandler().getState(anyString(), eq("PowerSwitch"), same(PowerSwitchServiceState.class)))
                .thenReturn(powerSwitchServiceState);

        super.beforeEach();
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

    @ParameterizedTest
    @MethodSource("org.openhab.binding.boschshc.internal.tests.common.CommonTestUtils#getExecutionAndTimeoutAndInterruptedExceptionArguments()")
    public void testHandleCommandPowerSwitchChannelHandleExceptions(Exception e)
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(getBridgeHandler().putState(any(), any(), any())).thenThrow(e);

        getFixture().handleCommand(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH), OnOffType.ON);

        verify(getCallback()).statusUpdated(same(getThing()),
                argThat(status -> status.getStatus().equals(ThingStatus.OFFLINE)
                        && status.getStatusDetail().equals(ThingStatusDetail.COMMUNICATION_ERROR)));
    }

    @Test
    public void testUpdateChannelPowerSwitchState() {
        JsonElement jsonObject = JsonParser
                .parseString("{\n" + "  \"@type\": \"powerSwitchState\",\n" + "  \"switchState\": \"ON\"\n" + "}");

        getFixture().processUpdate("PowerSwitch", jsonObject);

        // state is updated twice: via short poll in initialize() and via long poll result in this test
        verify(getCallback(), times(2)).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH),
                OnOffType.ON);

        jsonObject = JsonParser
                .parseString("{\n" + "  \"@type\": \"powerSwitchState\",\n" + "  \"switchState\": \"OFF\"\n" + "}");

        getFixture().processUpdate("PowerSwitch", jsonObject);

        verify(getCallback()).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH), OnOffType.OFF);
    }

    @Test
    public void testHandleCommandRefreshPowerSwitchChannel() {
        getFixture().handleCommand(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH), RefreshType.REFRESH);

        // state is updated twice: via short poll in initialize() and via long poll result in this test
        verify(getCallback(), times(2)).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH),
                OnOffType.ON);
    }

    @ParameterizedTest
    @MethodSource("org.openhab.binding.boschshc.internal.tests.common.CommonTestUtils#getBoschShcAndExecutionAndTimeoutAndInterruptedExceptionArguments()")
    public void testHandleCommandRefreshPowerSwitchChannelHandleExceptions(Exception e)
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(getBridgeHandler().getState(anyString(), eq("PowerSwitch"), same(PowerSwitchServiceState.class)))
                .thenThrow(e);

        getFixture().handleCommand(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH), RefreshType.REFRESH);

        verify(getCallback()).statusUpdated(same(getThing()),
                argThat(status -> status.getStatus().equals(ThingStatus.OFFLINE)
                        && status.getStatusDetail().equals(ThingStatusDetail.COMMUNICATION_ERROR)));
    }
}
