/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.presence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openhab.binding.boschshc.internal.devices.AbstractBoschSHCHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.services.presence.PresenceSimulationConfigurationService;
import org.openhab.binding.boschshc.internal.services.presence.dto.PresenceSimulationConfigurationServiceState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link PresenceSimulationHandler}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class PresenceSimulationHandlerTest extends AbstractBoschSHCHandlerTest<PresenceSimulationHandler> {
    @Captor
    @NonNullByDefault({})
    private ArgumentCaptor<PresenceSimulationConfigurationServiceState> presenceSimulationConfigurationServiceStateCaptor;

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_PRESENCE_SIMULATION;
    }

    @Override
    protected PresenceSimulationHandler createFixture() {
        return new PresenceSimulationHandler(getThing());
    }

    @Test
    void testUpdateChannelsPresenceSimulationConfiguration() {
        JsonElement jsonObject = JsonParser.parseString("""
                {
                  "@type": "presenceSimulationConfigurationState",
                  "enabled": true
                }""");

        getFixture().processUpdate(
                PresenceSimulationConfigurationService.PRESENCE_SIMULATION_CONFIGURATION_SERVICE_NAME, jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_PRESENCE_SIMULATION_ENABLED),
                OnOffType.ON);
    }

    @Test
    void testHandleCommandPresenceSimulationOn() throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_PRESENCE_SIMULATION_ENABLED),
                OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(BoschSHCBindingConstants.SERVICE_PRESENCE_SIMULATION),
                eq(PresenceSimulationConfigurationService.PRESENCE_SIMULATION_CONFIGURATION_SERVICE_NAME),
                presenceSimulationConfigurationServiceStateCaptor.capture());
        PresenceSimulationConfigurationServiceState state = presenceSimulationConfigurationServiceStateCaptor
                .getValue();
        assertTrue(state.enabled);
    }

    @Test
    void testHandleCommandPresenceSimulationOff() throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_PRESENCE_SIMULATION_ENABLED),
                OnOffType.OFF);
        verify(getBridgeHandler()).putState(eq(BoschSHCBindingConstants.SERVICE_PRESENCE_SIMULATION),
                eq(PresenceSimulationConfigurationService.PRESENCE_SIMULATION_CONFIGURATION_SERVICE_NAME),
                presenceSimulationConfigurationServiceStateCaptor.capture());
        PresenceSimulationConfigurationServiceState state = presenceSimulationConfigurationServiceStateCaptor
                .getValue();
        assertFalse(state.enabled);
    }
}
