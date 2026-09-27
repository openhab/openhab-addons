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
package org.openhab.binding.vesync.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.vesync.internal.VeSyncConstants.*;
import static org.openhab.binding.vesync.internal.handlers.VeSyncDeviceAirPurifierHandler.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.vesync.internal.handlers.VeSyncDevicePurifierMetadata;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;

/**
 * Tests for {@link VeSyncStateDescriptionProvider}.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class VeSyncStateDescriptionProviderTest {

    @Test
    void providesModelSpecificFanSpeedRanges() {
        assertFanSpeedRange(CORE300S);
        assertFanSpeedRange(CORE400S);
        assertFanSpeedRange(VITAL200S);
    }

    @Test
    void ignoresOtherChannels() {
        ThingRegistry thingRegistry = Objects.requireNonNull(mock(ThingRegistry.class));
        ThingUID thingUID = new ThingUID(THING_TYPE_AIR_PURIFIER, "test");
        Thing thing = ThingBuilder.create(THING_TYPE_AIR_PURIFIER, thingUID)
                .withProperties(Map.of(DEVICE_PROP_DEVICE_FAMILY, CORE300S.deviceFamilyName)).build();
        when(thingRegistry.get(thingUID)).thenReturn(thing);
        Channel channel = ChannelBuilder.create(new ChannelUID(thingUID, DEVICE_CHANNEL_ENABLED), "Switch").build();

        assertNull(new VeSyncStateDescriptionProvider(thingRegistry).getStateDescription(channel, null, null));
    }

    private void assertFanSpeedRange(VeSyncDevicePurifierMetadata metadata) {
        ThingRegistry thingRegistry = Objects.requireNonNull(mock(ThingRegistry.class));
        ThingUID thingUID = new ThingUID(THING_TYPE_AIR_PURIFIER, metadata.deviceFamilyName.toLowerCase());
        Thing thing = ThingBuilder.create(THING_TYPE_AIR_PURIFIER, thingUID)
                .withProperties(Map.of(DEVICE_PROP_DEVICE_FAMILY, metadata.deviceFamilyName)).build();
        when(thingRegistry.get(thingUID)).thenReturn(thing);
        Channel channel = ChannelBuilder
                .create(new ChannelUID(thingUID, DEVICE_CHANNEL_FAN_SPEED_ENABLED), "Number:Dimensionless").build();
        StateDescription original = StateDescriptionFragmentBuilder.create().withPattern("%.0f").withReadOnly(true)
                .build().toStateDescription();

        StateDescription description = new VeSyncStateDescriptionProvider(thingRegistry).getStateDescription(channel,
                original, null);

        assertNotNull(description);
        assertEquals(BigDecimal.valueOf(metadata.minFanSpeed), description.getMinimum());
        assertEquals(BigDecimal.valueOf(metadata.maxFanSpeed), description.getMaximum());
        assertEquals(BigDecimal.ONE, description.getStep());
        assertEquals("%.0f", description.getPattern());
        assertFalse(description.isReadOnly());
    }
}
