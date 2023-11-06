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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openhab.binding.boschshc.internal.devices.smokedetector.SmokeDetectorHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.smokedetectorcheck.SmokeDetectorCheckState;
import org.openhab.binding.boschshc.internal.services.smokedetectorcheck.dto.SmokeDetectorCheckServiceState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit Tests for {@link SmokeDetectorHandler}.
 *
 * @author Gerd Zanker - Initial contribution
 *
 */
@NonNullByDefault
public abstract class AbstractSmokeDetectorHandlerTest<T extends AbstractSmokeDetectorHandler>
        extends AbstractBatteryPoweredDeviceHandlerTest<T> {

    @Captor
    private @NonNullByDefault({}) ArgumentCaptor<SmokeDetectorCheckServiceState> smokeDetectorCheckStateCaptor;

    @Test
    public void testHandleCommand()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        // valid commands with valid thing & channel
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SMOKE_CHECK),
                new StringType(SmokeDetectorCheckState.SMOKE_TEST_REQUESTED.toString()));
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("SmokeDetectorCheck"),
                smokeDetectorCheckStateCaptor.capture());
        SmokeDetectorCheckServiceState state = smokeDetectorCheckStateCaptor.getValue();
        assertSame(SmokeDetectorCheckState.SMOKE_TEST_REQUESTED, state.value);

        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SMOKE_CHECK),
                new StringType(SmokeDetectorCheckState.NONE.toString()));
        verify(getBridgeHandler(), times(2)).putState(eq(getDeviceID()), eq("SmokeDetectorCheck"),
                smokeDetectorCheckStateCaptor.capture());
        state = smokeDetectorCheckStateCaptor.getValue();
        assertSame(SmokeDetectorCheckState.NONE, state.value);

        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SMOKE_CHECK),
                new StringType(SmokeDetectorCheckState.SMOKE_TEST_OK.toString()));
        verify(getBridgeHandler(), times(3)).putState(eq(getDeviceID()), eq("SmokeDetectorCheck"),
                smokeDetectorCheckStateCaptor.capture());
        state = smokeDetectorCheckStateCaptor.getValue();
        assertSame(SmokeDetectorCheckState.SMOKE_TEST_OK, state.value);

        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SMOKE_CHECK),
                new StringType(SmokeDetectorCheckState.SMOKE_TEST_FAILED.toString()));
        verify(getBridgeHandler(), times(4)).putState(eq(getDeviceID()), eq("SmokeDetectorCheck"),
                smokeDetectorCheckStateCaptor.capture());
        state = smokeDetectorCheckStateCaptor.getValue();
        assertSame(SmokeDetectorCheckState.SMOKE_TEST_FAILED, state.value);
    }

    @Test
    public void testHandleCommandPlayPauseType()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SMOKE_CHECK),
                PlayPauseType.PLAY);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("SmokeDetectorCheck"),
                smokeDetectorCheckStateCaptor.capture());
        SmokeDetectorCheckServiceState state = smokeDetectorCheckStateCaptor.getValue();
        assertSame(SmokeDetectorCheckState.SMOKE_TEST_REQUESTED, state.value);
    }

    @Test
    public void testHandleCommandUnknownCommand()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SMOKE_CHECK),
                OnOffType.ON);
        ThingStatusInfo expectedThingStatusInfo = ThingStatusInfoBuilder
                .create(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR)
                .withDescription(
                        "Error when service SmokeDetectorCheck should handle command org.openhab.core.library.types.OnOffType: SmokeDetectorCheck: Can not handle command org.openhab.core.library.types.OnOffType")
                .build();
        verify(getCallback()).statusUpdated(getThing(), expectedThingStatusInfo);
    }

    @Test
    public void testUpdateChannelSmokeDetectorCheckServiceStateNone() {
        JsonElement jsonObject = JsonParser.parseString("{\"@type\":\"smokeDetectorCheckState\",\"value\":NONE}");
        getFixture().processUpdate("SmokeDetectorCheck", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SMOKE_CHECK),
                new StringType("NONE"));
    }

    @Test
    public void testUpdateChannelSmokeDetectorCheckServiceStateRequests() {
        JsonElement jsonObject = JsonParser
                .parseString("{\"@type\":\"smokeDetectorCheckState\",\"value\":SMOKE_TEST_REQUESTED}");
        getFixture().processUpdate("SmokeDetectorCheck", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SMOKE_CHECK),
                new StringType("SMOKE_TEST_REQUESTED"));
    }
}
