/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.UnDefType;

/**
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyDiagnosticsChannelsTest {

    private static final ThingUID THING_UID = new ThingUID("shelly", "shellyplus1", "test");

    @BeforeAll
    static void initChannelDefinitions() {
        ShellyTranslationProvider messages = mock(ShellyTranslationProvider.class);
        when(messages.get(anyString(), any(Object[].class))).thenReturn("mocked");
        new ShellyChannelDefinitions(messages);
    }

    private static Thing thing() {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(THING_UID);
        return thing;
    }

    private static ShellyThingInterface handler(ThingTypeUID thingType) {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getProfile()).thenReturn(new ShellyDeviceProfile(thingType));
        Thing thing = thing();
        when(handler.getThing()).thenReturn(thing);
        when(handler.areChannelsCreated()).thenReturn(true);
        return handler;
    }

    private static String diag(String channel) {
        return CHANNEL_GROUP_DIAG + "#" + channel;
    }

    private static void verifyQuantity(ShellyThingInterface handler, String channel, long expected) {
        verify(handler).updateChannel(eq(CHANNEL_GROUP_DIAG), eq(channel),
                argThat(s -> s instanceof QuantityType<?> q && q.longValue() == expected));
    }

    @ParameterizedTest
    @ValueSource(strings = { "shelly1", "shellyblubutton" })
    void diagnosticsAreSkippedForGen1AndBlu(String thingTypeId) {
        ShellyThingInterface handler = handler(new ThingTypeUID(BINDING_ID, thingTypeId));
        ShellySettingsStatus status = new ShellySettingsStatus();
        status.ramTotal = 50000L;

        assertThat(ShellyChannelDefinitions.createDiagnosticsChannels(handler.getThing(), handler.getProfile(), status)
                .isEmpty(), is(true));
        ShellyComponents.updateDiagnosticsStats(handler, new ShellyDeviceStats());

        verify(handler, never()).updateChannel(eq(CHANNEL_GROUP_DIAG), anyString(), any());
    }

    @Test
    void createDiagnosticsChannelsAddsOnlyReportedFields() {
        ShellySettingsStatus status = new ShellySettingsStatus();
        status.ramTotal = 50000L;
        status.fsFree = 400000L;

        Map<String, Channel> channels = ShellyChannelDefinitions.createDiagnosticsChannels(thing(),
                new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1), status);

        assertThat(channels.keySet(),
                containsInAnyOrder(diag(CHANNEL_DIAG_TOTALMEM), diag(CHANNEL_DIAG_FREEFS), diag(CHANNEL_DIAG_RESTARTS),
                        diag(CHANNEL_DIAG_TIMEOUTERRORS), diag(CHANNEL_DIAG_ALARMS), diag(CHANNEL_DIAG_LASTALARM),
                        diag(CHANNEL_DIAG_PROTOCOLERRORS)));
    }

    @Test
    void updateDeviceStatusPublishesDiagnosticValues() {
        ShellyThingInterface handler = handler(THING_TYPE_SHELLYPLUS1);
        ShellySettingsStatus status = new ShellySettingsStatus();
        status.ramTotal = 50000L;
        status.ramFree = 20000L;
        status.fsSize = 1000000L;
        status.fsFree = 400000L;
        status.restartRequired = true;

        ShellyComponents.updateDeviceStatus(handler, status);

        verifyQuantity(handler, CHANNEL_DIAG_TOTALMEM, 50000);
        verifyQuantity(handler, CHANNEL_DIAG_FREEMEM, 20000);
        verifyQuantity(handler, CHANNEL_DIAG_TOTALFS, 1000000);
        verifyQuantity(handler, CHANNEL_DIAG_FREEFS, 400000);
        verify(handler).updateChannel(CHANNEL_GROUP_DIAG, CHANNEL_DIAG_RESTARTREQ, OnOffType.ON);
    }

    @Test
    void updateDeviceStatusSkipsDiagnosticsForGen1() {
        ShellyThingInterface handler = handler(THING_TYPE_SHELLY1);
        ShellySettingsStatus status = new ShellySettingsStatus();
        status.ramTotal = 50000L;

        ShellyComponents.updateDeviceStatus(handler, status);

        verify(handler, never()).updateChannel(eq(CHANNEL_GROUP_DIAG), anyString(), any());
    }

    @Test
    void updateDiagnosticsStatsPublishesCountersAndLastAlarm() {
        ShellyThingInterface handler = handler(THING_TYPE_SHELLYPLUS1);
        ShellyDeviceStats stats = new ShellyDeviceStats();
        stats.restarts.set(2);
        stats.timeoutErrors.set(3);
        stats.alarms.set(1);
        stats.protocolErrors.set(4);
        stats.maxInternalTemp.set(55);
        stats.lastAlarm.set(new ShellyDeviceStats.ShellyDeviceAlarm("OVERTEMP", 0));

        ShellyComponents.updateDiagnosticsStats(handler, stats);

        verify(handler).updateChannel(CHANNEL_GROUP_DIAG, CHANNEL_DIAG_RESTARTS, new DecimalType(2));
        verify(handler).updateChannel(CHANNEL_GROUP_DIAG, CHANNEL_DIAG_TIMEOUTERRORS, new DecimalType(3));
        verify(handler).updateChannel(CHANNEL_GROUP_DIAG, CHANNEL_DIAG_ALARMS, new DecimalType(1));
        verify(handler).updateChannel(CHANNEL_GROUP_DIAG, CHANNEL_DIAG_PROTOCOLERRORS, new DecimalType(4));
        verifyQuantity(handler, CHANNEL_DIAG_MAXITEMP, 55);
        verify(handler).updateChannel(eq(CHANNEL_GROUP_DIAG), eq(CHANNEL_DIAG_LASTALARM),
                argThat(s -> s instanceof StringType && s.toString().startsWith("OVERTEMP")));
    }

    @Test
    void updateDiagnosticsStatsClearsAlarmAndMaxTempAfterStatsReset() {
        ShellyThingInterface handler = handler(THING_TYPE_SHELLYPLUS1);
        ShellyDeviceStats stats = new ShellyDeviceStats();
        stats.maxInternalTemp.set(55);
        stats.lastAlarm.set(new ShellyDeviceStats.ShellyDeviceAlarm("OVERTEMP", 0));
        ShellyComponents.updateDiagnosticsStats(handler, stats);

        ShellyComponents.updateDiagnosticsStats(handler, new ShellyDeviceStats());

        verify(handler).updateChannel(CHANNEL_GROUP_DIAG, CHANNEL_DIAG_LASTALARM, UnDefType.UNDEF);
        verify(handler).updateChannel(CHANNEL_GROUP_DIAG, CHANNEL_DIAG_MAXITEMP, UnDefType.UNDEF);
    }
}
