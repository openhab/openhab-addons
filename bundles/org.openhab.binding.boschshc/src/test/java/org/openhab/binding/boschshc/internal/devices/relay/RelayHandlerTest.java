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
package org.openhab.binding.boschshc.internal.devices.relay;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openhab.binding.boschshc.internal.devices.AbstractPowerSwitchHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.childprotection.dto.ChildProtectionServiceState;
import org.openhab.binding.boschshc.internal.services.impulseswitch.ImpulseSwitchService;
import org.openhab.binding.boschshc.internal.services.impulseswitch.dto.ImpulseSwitchServiceState;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link RelayHandler}.
 * 
 * @author David Pace - Initial contributions
 *
 */
@NonNullByDefault
class RelayHandlerTest extends AbstractPowerSwitchHandlerTest<RelayHandler> {

    private @Captor @NonNullByDefault({}) ArgumentCaptor<ChildProtectionServiceState> childProtectionServiceStateCaptor;

    private @Captor @NonNullByDefault({}) ArgumentCaptor<ImpulseSwitchServiceState> impulseSwitchServiceStateCaptor;

    @Override
    protected RelayHandler createFixture() {
        return new RelayHandler(getThing());
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_RELAY;
    }

    @Override
    protected String getDeviceID() {
        return "hdm:ZigBee:30XXXXXXXXXXXXXX";
    }

    @Test
    void testUpdateChannelsCommunicationQualityService() {
        String json = """
                {
                    "@type": "communicationQualityState",
                    "quality": "UNKNOWN"
                }
                """;
        JsonElement jsonObject = JsonParser.parseString(json);

        getFixture().processUpdate("CommunicationQuality", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SIGNAL_STRENGTH),
                new DecimalType(0));

        json = """
                {
                    "@type": "communicationQualityState",
                    "quality": "GOOD"
                }
                """;
        jsonObject = JsonParser.parseString(json);

        getFixture().processUpdate("CommunicationQuality", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SIGNAL_STRENGTH),
                new DecimalType(4));
    }

    @Test
    void testUpdateChannelsChildProtectionService() {
        String json = """
                {
                    "@type": "ChildProtectionState",
                    "childLockActive": true
                }
                """;
        JsonElement jsonObject = JsonParser.parseString(json);

        getFixture().processUpdate("ChildProtection", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION), OnOffType.ON);
    }

    @Test
    void testUpdateChannelsImpulseSwitchService()
            throws BoschSHCException, InterruptedException, TimeoutException, ExecutionException {
        configureImpulseSwitchMode();
        String json = """
                {
                  "@type": "ImpulseSwitchState",
                  "impulseState": true,
                  "impulseLength": 100,
                  "instantOfLastImpulse": "2024-04-14T15:52:31.677366Z"
                }
                """;
        JsonElement jsonObject = JsonParser.parseString(json);

        getFixture().processUpdate("ImpulseSwitch", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_IMPULSE_SWITCH), OnOffType.ON);
        verify(getCallback(), times(2)).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_IMPULSE_LENGTH),
                new DecimalType(100));
        verify(getCallback(), times(2)).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_INSTANT_OF_LAST_IMPULSE),
                new DateTimeType("2024-04-14T15:52:31.677366Z"));
    }

    @Test
    void testUpdateChannelsImpulseSwitchServiceNoInstantOfLastImpulse()
            throws BoschSHCException, InterruptedException, TimeoutException, ExecutionException {
        configureImpulseSwitchMode();
        String json = """
                {
                  "@type": "ImpulseSwitchState",
                  "impulseState": true,
                  "impulseLength": 100
                }
                """;
        JsonElement jsonObject = JsonParser.parseString(json);

        getFixture().processUpdate("ImpulseSwitch", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_IMPULSE_SWITCH), OnOffType.ON);
        verify(getCallback(), times(2)).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_IMPULSE_LENGTH),
                new DecimalType(100));
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_INSTANT_OF_LAST_IMPULSE),
                UnDefType.NULL);
    }

    private void configureImpulseSwitchMode()
            throws BoschSHCException, InterruptedException, TimeoutException, ExecutionException {
        getDevice().deviceServiceIds = List.of(ImpulseSwitchService.IMPULSE_SWITCH_SERVICE_NAME);
        getFixture().initialize();

        assertThat(getFixture().getThing().getChannel(BoschSHCBindingConstants.CHANNEL_IMPULSE_SWITCH),
                is(notNullValue()));
        assertThat(getFixture().getThing().getChannel(BoschSHCBindingConstants.CHANNEL_IMPULSE_LENGTH),
                is(notNullValue()));
        assertThat(getFixture().getThing().getChannel(BoschSHCBindingConstants.CHANNEL_INSTANT_OF_LAST_IMPULSE),
                is(notNullValue()));

        @Nullable
        JsonElement impulseSwitchServiceState = JsonParser.parseString("""
                {
                  "@type": "ImpulseSwitchState",
                  "impulseState": false,
                  "impulseLength": 100,
                  "instantOfLastImpulse": "2024-04-14T15:52:31.677366Z"
                }
                """);
        getFixture().processUpdate(ImpulseSwitchService.IMPULSE_SWITCH_SERVICE_NAME, impulseSwitchServiceState);
    }

    @Test
    void testHandleCommandChildProtection()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION), OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("ChildProtection"),
                childProtectionServiceStateCaptor.capture());
        ChildProtectionServiceState state = childProtectionServiceStateCaptor.getValue();
        assertThat(state.childLockActive, is(true));
    }

    @Test
    void testHandleCommandChildProtectionInvalidCommand()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION),
                DecimalType.ZERO);
        verify(getBridgeHandler(), times(0)).putState(eq(getDeviceID()), eq("ChildProtection"), any());
    }

    @Test
    void testHandleCommandImpulseStateOn()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        configureImpulseSwitchMode();

        Instant testDate = Instant.now();
        getFixture().setCurrentDateTimeProvider(() -> testDate);

        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_IMPULSE_SWITCH),
                OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq(ImpulseSwitchService.IMPULSE_SWITCH_SERVICE_NAME),
                impulseSwitchServiceStateCaptor.capture());
        ImpulseSwitchServiceState state = impulseSwitchServiceStateCaptor.getValue();
        assertThat(state.impulseState, is(true));
        assertThat(state.impulseLength, is(100));
        assertThat(state.instantOfLastImpulse, is(testDate.toString()));
    }

    @Test
    void testHandleCommandImpulseLength()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        configureImpulseSwitchMode();

        Instant testDate = Instant.now();
        getFixture().setCurrentDateTimeProvider(() -> testDate);

        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_IMPULSE_LENGTH),
                new DecimalType(15));
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq(ImpulseSwitchService.IMPULSE_SWITCH_SERVICE_NAME),
                impulseSwitchServiceStateCaptor.capture());
        ImpulseSwitchServiceState state = impulseSwitchServiceStateCaptor.getValue();
        assertThat(state.impulseState, is(false));
        assertThat(state.impulseLength, is(15));
        assertThat(state.instantOfLastImpulse, is("2024-04-14T15:52:31.677366Z"));
    }

    @Test
    void testHandleCommandImpulseStateOff()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_IMPULSE_SWITCH),
                OnOffType.OFF);
        verify(getBridgeHandler(), times(0)).postState(eq(getDeviceID()),
                eq(ImpulseSwitchService.IMPULSE_SWITCH_SERVICE_NAME), any());
    }
}
