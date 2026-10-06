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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

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

    private static ThingTypeUID thingType(String id) {
        return new ThingTypeUID(BINDING_ID, id);
    }

    private static String diag(String channel) {
        return CHANNEL_GROUP_DIAG + "#" + channel;
    }

    @ParameterizedTest
    @ValueSource(strings = { "shelly1", "shellyblubutton" })
    void createDiagnosticsChannelsSkipsGen1AndBlu(String thingTypeId) {
        ShellySettingsStatus status = new ShellySettingsStatus();
        status.ramTotal = 50000L;

        Map<String, Channel> channels = ShellyChannelDefinitions.createDiagnosticsChannels(thing(),
                new ShellyDeviceProfile(thingType(thingTypeId)), status);

        assertThat(channels.isEmpty(), is(true));
    }

    @Test
    void createDiagnosticsChannelsAddsOnlyReportedFields() {
        ShellySettingsStatus status = new ShellySettingsStatus();
        status.ramTotal = 50000L;
        status.fsFree = 400000L;

        Map<String, Channel> channels = ShellyChannelDefinitions.createDiagnosticsChannels(thing(),
                new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1), status);

        assertThat(channels.keySet(), hasItems(diag(CHANNEL_DIAG_TOTALMEM), diag(CHANNEL_DIAG_FREEFS)));
        assertThat(channels.keySet(), not(hasItem(diag(CHANNEL_DIAG_FREEMEM))));
        assertThat(channels.keySet(), not(hasItem(diag(CHANNEL_DIAG_TOTALFS))));
        assertThat(channels.keySet(), not(hasItem(diag(CHANNEL_DIAG_RESTARTREQ))));
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

        verify(handler).updateChannel(eq(CHANNEL_GROUP_DIAG), eq(CHANNEL_DIAG_TOTALMEM),
                argThat(s -> s instanceof QuantityType<?> q && q.longValue() == 50000));
        verify(handler).updateChannel(eq(CHANNEL_GROUP_DIAG), eq(CHANNEL_DIAG_FREEMEM),
                argThat(s -> s instanceof QuantityType<?> q && q.longValue() == 20000));
        verify(handler).updateChannel(eq(CHANNEL_GROUP_DIAG), eq(CHANNEL_DIAG_TOTALFS),
                argThat(s -> s instanceof QuantityType<?> q && q.longValue() == 1000000));
        verify(handler).updateChannel(eq(CHANNEL_GROUP_DIAG), eq(CHANNEL_DIAG_FREEFS),
                argThat(s -> s instanceof QuantityType<?> q && q.longValue() == 400000));
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
}
