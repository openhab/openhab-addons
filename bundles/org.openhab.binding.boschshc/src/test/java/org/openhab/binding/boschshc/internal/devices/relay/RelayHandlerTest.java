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
package org.openhab.binding.boschshc.internal.devices.relay;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.boschshc.internal.devices.AbstractPowerSwitchHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.childprotection.dto.ChildProtectionServiceState;
import org.openhab.binding.boschshc.internal.services.impulseswitch.ImpulseSwitchService;
import org.openhab.binding.boschshc.internal.services.impulseswitch.dto.ImpulseSwitchServiceState;
import org.openhab.binding.boschshc.internal.services.powerswitch.PowerSwitchService;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
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
@ExtendWith(MockitoExtension.class)
class RelayHandlerTest extends AbstractPowerSwitchHandlerTest<RelayHandler> {

    private @Captor @NonNullByDefault({}) ArgumentCaptor<ChildProtectionServiceState> childProtectionServiceStateCaptor;

    private @Captor @NonNullByDefault({}) ArgumentCaptor<ImpulseSwitchServiceState> impulseSwitchServiceStateCaptor;

    @Override
    protected void beforeHandlerInitialization(TestInfo testInfo) {
        super.beforeHandlerInitialization(testInfo);

        Channel signalStrengthChannel = ChannelBuilder
                .create(new ChannelUID(getThingUID(), BoschSHCBindingConstants.CHANNEL_SIGNAL_STRENGTH)).build();
        Channel childProtectionChannel = ChannelBuilder
                .create(new ChannelUID(getThingUID(), BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION)).build();
        Channel powerSwitchChannel = ChannelBuilder
                .create(new ChannelUID(getThingUID(), BoschSHCBindingConstants.CHANNEL_POWER_SWITCH)).build();
        Channel impulseSwitchChannel = ChannelBuilder
                .create(new ChannelUID(getThingUID(), BoschSHCBindingConstants.CHANNEL_IMPULSE_SWITCH)).build();
        Channel impulseLengthChannel = ChannelBuilder
                .create(new ChannelUID(getThingUID(), BoschSHCBindingConstants.CHANNEL_IMPULSE_LENGTH)).build();
        Channel instantOfLastImpulseChannel = ChannelBuilder
                .create(new ChannelUID(getThingUID(), BoschSHCBindingConstants.CHANNEL_INSTANT_OF_LAST_IMPULSE))
                .build();

        when(getThing().getChannels()).thenReturn(List.of(signalStrengthChannel, childProtectionChannel,
                powerSwitchChannel, impulseSwitchChannel, impulseLengthChannel, instantOfLastImpulseChannel));

        lenient().when(getThing().getChannel(BoschSHCBindingConstants.CHANNEL_SIGNAL_STRENGTH))
                .thenReturn(signalStrengthChannel);
        lenient().when(getThing().getChannel(BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION))
                .thenReturn(childProtectionChannel);
        lenient().when(getThing().getChannel(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH))
                .thenReturn(powerSwitchChannel);
        lenient().when(getThing().getChannel(BoschSHCBindingConstants.CHANNEL_IMPULSE_SWITCH))
                .thenReturn(impulseSwitchChannel);
        lenient().when(getThing().getChannel(BoschSHCBindingConstants.CHANNEL_IMPULSE_LENGTH))
                .thenReturn(impulseLengthChannel);
        lenient().when(getThing().getChannel(BoschSHCBindingConstants.CHANNEL_INSTANT_OF_LAST_IMPULSE))
                .thenReturn(instantOfLastImpulseChannel);

        if (testInfo.getTags().contains(ImpulseSwitchService.IMPULSE_SWITCH_SERVICE_NAME)) {
            getDevice().deviceServiceIds = List.of(ImpulseSwitchService.IMPULSE_SWITCH_SERVICE_NAME);
        }
    }

    @Override
    protected void afterHandlerInitialization(TestInfo testInfo) {
        super.afterHandlerInitialization(testInfo);

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

    @Tag(ImpulseSwitchService.IMPULSE_SWITCH_SERVICE_NAME)
    @Test
    void testUpdateChannelsImpulseSwitchService()
            throws BoschSHCException, InterruptedException, TimeoutException, ExecutionException {
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

    @Tag(ImpulseSwitchService.IMPULSE_SWITCH_SERVICE_NAME)
    @Test
    void testUpdateChannelsImpulseSwitchServiceNoInstantOfLastImpulse()
            throws BoschSHCException, InterruptedException, TimeoutException, ExecutionException {
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

    @Test
    void testDeviceModeChanged() throws BoschSHCException, InterruptedException, TimeoutException, ExecutionException {
        getDevice().deviceServiceIds = List.of(ImpulseSwitchService.IMPULSE_SWITCH_SERVICE_NAME);

        // initialize again to check whether mode change is detected
        getFixture().initialize();

        verify(getCallback()).statusUpdated(any(Thing.class),
                argThat(status -> status.getStatus().equals(ThingStatus.OFFLINE)
                        && status.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));

        verify(getCallback(), times(1)).statusUpdated(any(Thing.class),
                argThat(status -> status.getStatus().equals(ThingStatus.ONLINE)));

        verify(getCallback(), times(0)).thingUpdated(
                argThat(t -> ImpulseSwitchService.IMPULSE_SWITCH_SERVICE_NAME.equals(t.getProperties().get("mode"))));
    }

    @Test
    void testDeviceModeUnchanged()
            throws BoschSHCException, InterruptedException, TimeoutException, ExecutionException {
        // initialize again without mode change
        getFixture().initialize();

        verify(getCallback(), times(0)).statusUpdated(any(Thing.class),
                argThat(status -> status.getStatus().equals(ThingStatus.OFFLINE)
                        && status.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));
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

    @Tag(ImpulseSwitchService.IMPULSE_SWITCH_SERVICE_NAME)
    @Test
    void testHandleCommandImpulseStateOn()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
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

    @Tag(ImpulseSwitchService.IMPULSE_SWITCH_SERVICE_NAME)
    @Test
    void testHandleCommandImpulseLengthDecimalType()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
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

    @Tag(ImpulseSwitchService.IMPULSE_SWITCH_SERVICE_NAME)
    @Test
    void testHandleCommandImpulseLengthQuantityType()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        Instant testDate = Instant.now();
        getFixture().setCurrentDateTimeProvider(() -> testDate);

        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_IMPULSE_LENGTH),
                new QuantityType<Time>(1.5, Units.SECOND));
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq(ImpulseSwitchService.IMPULSE_SWITCH_SERVICE_NAME),
                impulseSwitchServiceStateCaptor.capture());
        ImpulseSwitchServiceState state = impulseSwitchServiceStateCaptor.getValue();
        assertThat(state.impulseState, is(false));
        assertThat(state.impulseLength, is(15));
        assertThat(state.instantOfLastImpulse, is("2024-04-14T15:52:31.677366Z"));
    }

    @Tag(ImpulseSwitchService.IMPULSE_SWITCH_SERVICE_NAME)
    @Test
    void testHandleCommandImpulseLengthQuantityTypeTooManyFractionDigits()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        Instant testDate = Instant.now();
        getFixture().setCurrentDateTimeProvider(() -> testDate);

        // 0.08 s of 1.58 s will be discarded because API precision is limited to deciseconds
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_IMPULSE_LENGTH),
                new QuantityType<Time>(1.58, Units.SECOND));
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

    @Test
    void testUpdateModePropertyIfApplicablePowerSwitchMode() {
        verify(getCallback(), times(2)).thingUpdated(argThat(t -> PowerSwitchService.POWER_SWITCH_SERVICE_NAME
                .equals(t.getProperties().get(RelayHandler.PROPERTY_MODE))));
    }

    @Tag(ImpulseSwitchService.IMPULSE_SWITCH_SERVICE_NAME)
    @Test
    void testUpdateModePropertyIfApplicableImpulseSwitchMode() {
        verify(getCallback(), times(2)).thingUpdated(argThat(t -> ImpulseSwitchService.IMPULSE_SWITCH_SERVICE_NAME
                .equals(t.getProperties().get(RelayHandler.PROPERTY_MODE))));
    }

    /**
     * This has to be tested differently for the RelayHandler because the thing mock
     * will be replaced by a real thing during the first initialization, which
     * modifies the channels.
     */
    @Test
    @Tag(TAG_LEGACY_LOCATION_PROPERTY)
    @Override
    protected void deleteLegacyLocationProperty() {
        ArgumentCaptor<Thing> thingCaptor = ArgumentCaptor.forClass(Thing.class);
        verify(getCallback(), times(3)).thingUpdated(thingCaptor.capture());
        List<Thing> allValues = thingCaptor.getAllValues();
        assertThat(allValues, hasSize(3));
        assertThat(allValues.get(2).getProperties(), not(hasKey(BoschSHCBindingConstants.PROPERTY_LOCATION_LEGACY)));
    }

    /**
     * This has to be tested differently for the RelayHandler because the thing mock
     * will be replaced by a real thing during the first initialization, which
     * modifies the channels.
     */
    @Test
    @Tag(TAG_LOCATION_PROPERTY)
    @Override
    protected void locationPropertyDidNotChange() {
        // re-initialize
        getFixture().initialize();

        verify(getCallback(), times(3)).thingUpdated(
                argThat(t -> t.getProperties().get(BoschSHCBindingConstants.PROPERTY_LOCATION).equals("Kitchen")));
    }

    /**
     * This has to be tested differently for the RelayHandler because the thing mock
     * will be replaced by a real thing during the first initialization, which
     * modifies the channels.
     */
    @Test
    @Tag(TAG_LOCATION_PROPERTY)
    @Override
    protected void locationPropertyDidChange() {
        getDevice().roomId = "hz_2";
        when(getBridgeHandler().resolveRoomId("hz_2")).thenReturn("Dining Room");

        // re-initialize
        getFixture().initialize();

        verify(getCallback(), times(4)).thingUpdated(
                argThat(t -> t.getProperties().get(BoschSHCBindingConstants.PROPERTY_LOCATION).equals("Dining Room")));
    }
}
