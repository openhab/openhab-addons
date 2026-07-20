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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.mkChannelId;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;

/**
 * Tests {@link ShellyChannelMigration#migrateChannels} end-to-end: which channels get created for a
 * given starting schema version, existing channel set and device profile.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyChannelMigrationRulesTest {

    private static final ThingUID THING_UID = new ThingUID("shelly", "shellyplus1pm", "test");

    static {
        // CHANNEL_DEFINITIONS is populated once at (OSGi) construction time
        ShellyTranslationProvider messages = mock(ShellyTranslationProvider.class);
        when(messages.get(anyString(), any(Object[].class))).thenAnswer(i -> i.getArgument(0));
        new ShellyChannelDefinitions(messages);
    }

    @Test
    void schema6CreatesExactlyTheMinuteEnergySiblingsAndMeterResetForNonEMDevice() {
        ShellyThingInterface handler = handlerAtSchema(5, false,
                channel(CHANNEL_GROUP_METER, CHANNEL_METER_CURRENTPOWER));

        ShellyChannelMigration.migrateChannels(handler);

        Set<String> created = capturedNewChannelIds(handler);
        assertEquals(Set.of(mkChannelId(CHANNEL_GROUP_METER, CHANNEL_METER_ENERGYHISTMIN1),
                mkChannelId(CHANNEL_GROUP_METER, CHANNEL_METER_ENERGYHISTMIN2),
                mkChannelId(CHANNEL_GROUP_METER, CHANNEL_METER_ENERGYHISTMIN3),
                mkChannelId(CHANNEL_GROUP_METER, CHANNEL_METER_ENERGYAVGLAST3MIN),
                mkChannelId(CHANNEL_GROUP_METER, CHANNEL_EMETER_RESETTOTAL)), created);
    }

    @Test
    void schema6ResetsAtDeviceLevelFor3EmInsteadOfPerMeter() {
        ShellyThingInterface handler = handlerAtSchema(5, true,
                channel(CHANNEL_GROUP_METER, CHANNEL_METER_CURRENTPOWER),
                channel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ACCUMULATEDPOWER));

        ShellyChannelMigration.migrateChannels(handler);

        Set<String> created = capturedNewChannelIds(handler);
        assertTrue(created.contains(mkChannelId(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_RESETTOTAL)));
        assertFalse(created.contains(mkChannelId(CHANNEL_GROUP_METER, CHANNEL_EMETER_RESETTOTAL)));
    }

    @Test
    void schema6Gen1PowerMeterGetsMinuteEnergyButNoReset() {
        // Gen1 /meter devices (1PM, Plug, 2.5, dimmers) report counters[] but have no reset API
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLY1PM);
        ShellyThingInterface handler = handlerAtSchema(5, profile,
                channel(CHANNEL_GROUP_METER, CHANNEL_METER_CURRENTPOWER));

        ShellyChannelMigration.migrateChannels(handler);

        Set<String> created = capturedNewChannelIds(handler);
        assertEquals(Set.of(mkChannelId(CHANNEL_GROUP_METER, CHANNEL_METER_ENERGYHISTMIN1),
                mkChannelId(CHANNEL_GROUP_METER, CHANNEL_METER_ENERGYHISTMIN2),
                mkChannelId(CHANNEL_GROUP_METER, CHANNEL_METER_ENERGYHISTMIN3),
                mkChannelId(CHANNEL_GROUP_METER, CHANNEL_METER_ENERGYAVGLAST3MIN)), created);
    }

    @Test
    void schema6Gen1EmGetsResetButNoMinuteEnergy() {
        // Gen1 /emeter devices (EM) support reset_totals but report no per-minute counters
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYEM);
        profile.isEMeter = true;
        ShellyThingInterface handler = handlerAtSchema(5, profile,
                channel(CHANNEL_GROUP_METER, CHANNEL_METER_CURRENTPOWER));

        ShellyChannelMigration.migrateChannels(handler);

        assertEquals(Set.of(mkChannelId(CHANNEL_GROUP_METER, CHANNEL_EMETER_RESETTOTAL)),
                capturedNewChannelIds(handler));
    }

    @Test
    void schema6Gen2ClampMeterGetsResetButNoMinuteEnergy() {
        // Gen2 em1 components (Plus EM, EM Mini, Pro EM-50) support reset but have no aenergy.by_minute
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSEM);
        ShellyThingInterface handler = handlerAtSchema(5, profile,
                channel(CHANNEL_GROUP_METER, CHANNEL_METER_CURRENTPOWER));

        ShellyChannelMigration.migrateChannels(handler);

        assertEquals(Set.of(mkChannelId(CHANNEL_GROUP_METER, CHANNEL_EMETER_RESETTOTAL)),
                capturedNewChannelIds(handler));
    }

    @Test
    void schema6CreatesNoMinuteEnergySiblingsWithoutCurrentPowerAnchor() {
        ShellyThingInterface handler = handlerAtSchema(5, false,
                channel(CHANNEL_GROUP_METER, CHANNEL_METER_TOTALENERGY));

        ShellyChannelMigration.migrateChannels(handler);

        assertTrue(capturedNewChannelIds(handler).isEmpty());
    }

    @Test
    void alreadyOnCurrentSchemaSkipsMigrationEntirely() {
        ShellyThingInterface handler = handlerAtSchema(6, false,
                channel(CHANNEL_GROUP_METER, CHANNEL_METER_CURRENTPOWER));

        ShellyChannelMigration.migrateChannels(handler);

        verify(handler, never()).updateThingChannels(anyMap(), anyMap());
    }

    private static ShellyThingInterface handlerAtSchema(int schemaVersion, boolean is3EM, Channel... existingChannels) {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
        profile.is3EM = is3EM;
        return handlerAtSchema(schemaVersion, profile, existingChannels);
    }

    private static ShellyThingInterface handlerAtSchema(int schemaVersion, ShellyDeviceProfile profile,
            Channel... existingChannels) {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(THING_UID);
        when(thing.getChannels()).thenReturn(List.of(existingChannels));

        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getThing()).thenReturn(thing);
        when(handler.getThingName()).thenReturn("test");
        when(handler.getProfile()).thenReturn(profile);
        when(handler.getProperty(PROPERTY_CHANNEL_SCHEMA_VERSION)).thenReturn(String.valueOf(schemaVersion));
        when(handler.updateThingChannels(anyMap(), anyMap())).thenReturn(true);
        return handler;
    }

    @SuppressWarnings("unchecked")
    private static Set<String> capturedNewChannelIds(ShellyThingInterface handler) {
        ArgumentCaptor<Map<String, Channel>> captor = ArgumentCaptor.forClass(Map.class);
        verify(handler, atLeastOnce()).updateThingChannels(anyMap(), captor.capture());
        Set<String> ids = new HashSet<>();
        for (Map<String, Channel> newChannels : captor.getAllValues()) {
            ids.addAll(newChannels.keySet());
        }
        return ids;
    }

    private static Channel channel(String group, String name) {
        return ChannelBuilder.create(new ChannelUID(THING_UID, mkChannelId(group, name))).build();
    }
}
