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
package org.openhab.binding.boschshc.internal.devices.userdefinedstate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.boschshc.internal.devices.AbstractBoschSHCHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.userstate.dto.UserStateServiceState;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Unit tests for UserStateHandlerTest
 *
 * @author Patrick Gell - Initial contribution
 */
@NonNullByDefault
class UserStateHandlerTest extends AbstractBoschSHCHandlerTest<UserStateHandler> {

    private final Configuration config = new Configuration(Map.of("id", UUID.randomUUID().toString()));

    @Override
    protected UserStateHandler createFixture() {
        return new UserStateHandler(getThing());
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_USER_DEFINED_STATE;
    }

    @Override
    protected Configuration getConfiguration() {
        return config;
    }

    @Test
    void testHandleCommandSetState()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        var channel = new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_USER_DEFINED_STATE);
        getFixture().handleCommand(channel, OnOffType.ON);

        ArgumentCaptor<String> deviceId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UserStateServiceState> stateClass = ArgumentCaptor.forClass(UserStateServiceState.class);

        verify(getBridgeHandler()).getUserStateInfo(config.get("id").toString());
        verify(getBridgeHandler()).getState(anyString(), anyString(), any());
        verify(getBridgeHandler()).putState(deviceId.capture(), anyString(), stateClass.capture());

        assertNotNull(deviceId.getValue());
        assertEquals(channel.getThingUID().getId(), deviceId.getValue());

        assertNotNull(stateClass.getValue());
        assertTrue(stateClass.getValue().isState());
    }

    @ParameterizedTest()
    @MethodSource("org.openhab.binding.boschshc.internal.tests.common.CommonTestUtils#getExecutionExceptionAndInterruptedExceptionArguments()")
    void testHandleCommandSetStateUpdatesThingStatusOnException(Exception exception)
            throws InterruptedException, TimeoutException, ExecutionException {
        when(getBridgeHandler().putState(anyString(), anyString(), any(UserStateServiceState.class)))
                .thenThrow(exception);
        var channel = new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_USER_DEFINED_STATE);

        getFixture().handleCommand(channel, OnOffType.ON);

        verify(getCallback()).statusUpdated(same(getThing()),
                argThat(status -> status.getStatus().equals(ThingStatus.OFFLINE)
                        && status.getStatusDetail().equals(ThingStatusDetail.COMMUNICATION_ERROR)));
    }

    @Test
    void initializeWithoutId() {
        when(getThing().getConfiguration()).thenReturn(new Configuration());

        getFixture().initialize();

        verify(getCallback()).statusUpdated(same(getThing()),
                argThat(status -> status.getStatus().equals(ThingStatus.OFFLINE)
                        && status.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));
    }

    @ParameterizedTest
    @MethodSource("org.openhab.binding.boschshc.internal.tests.common.CommonTestUtils#getBoschShcAndExecutionAndTimeoutAndInterruptedExceptionArguments()")
    void initializeHandleExceptions(Exception e)
            throws BoschSHCException, InterruptedException, TimeoutException, ExecutionException {
        when(getBridgeHandler().getUserStateInfo(anyString())).thenThrow(e);

        getFixture().initialize();

        verify(getCallback()).statusUpdated(same(getThing()),
                argThat(status -> status.getStatus().equals(ThingStatus.OFFLINE)
                        && status.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));
    }
}
