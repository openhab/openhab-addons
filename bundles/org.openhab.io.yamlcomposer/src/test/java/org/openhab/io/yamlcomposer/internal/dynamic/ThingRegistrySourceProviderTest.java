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
package org.openhab.io.yamlcomposer.internal.dynamic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.events.ThingStatusInfoChangedEvent;
import org.openhab.core.thing.type.ChannelKind;

/**
 * Unit tests for {@link ThingRegistrySourceProvider}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
class ThingRegistrySourceProviderTest {

    private @Nullable ThingRegistry thingRegistry;
    private @Nullable ThingRegistrySourceProvider provider;
    private final List<EntityChange> emittedChanges = new ArrayList<>();

    @BeforeEach
    void setUp() {
        thingRegistry = mock(ThingRegistry.class);
        provider = new ThingRegistrySourceProvider(Objects.requireNonNull(thingRegistry));
        provider.setOnChangeListener(emittedChanges::add);
        emittedChanges.clear();
    }

    @Test
    void adaptToMapRenamesConfigAndProvidesAliases() {
        ThingUID thingUID = new ThingUID("mqtt:topic:porch");
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(thingUID);
        when(thing.getThingTypeUID()).thenReturn(new ThingTypeUID("mqtt:topic"));
        when(thing.isEnabled()).thenReturn(true);
        when(thing.getLabel()).thenReturn("Porch Light");

        Configuration thingConfig = new Configuration(Map.of("host", "192.168.1.50"));
        when(thing.getConfiguration()).thenReturn(thingConfig);

        ChannelUID channelUID = new ChannelUID(thingUID, "switch");
        Channel channel = mock(Channel.class);
        when(channel.getUID()).thenReturn(channelUID);
        when(channel.getConfiguration()).thenReturn(new Configuration(Map.of("stateTopic", "porch/state")));
        when(channel.getKind()).thenReturn(ChannelKind.STATE);
        when(thing.getChannels()).thenReturn(List.of(channel));

        Map<String, @Nullable Object> map = provider.adaptToMap(thing);

        // Verify the custom id, uid (lower case), and enabled state are present
        assertEquals("porch", map.get("id"));
        assertEquals("mqtt:topic:porch", map.get("uid"));
        assertEquals(true, map.get("enabled"));

        // Verify thing configuration was renamed to 'config'
        @SuppressWarnings("unchecked")
        Map<String, Object> adaptedThingConfig = (Map<String, Object>) map.get("config");
        assertNotNull(adaptedThingConfig);
        assertEquals("192.168.1.50", adaptedThingConfig.get("host"));
        assertNull(map.get("configuration"));

        // Verify that channels contain a map, keyed by the channel id
        @SuppressWarnings("unchecked")
        Map<String, Map<String, @Nullable Object>> channelsMap = (Map<String, Map<String, @Nullable Object>>) map
                .get("channels");
        assertNotNull(channelsMap);
        Map<String, @Nullable Object> adaptedChannel = channelsMap.get("switch");
        assertNotNull(adaptedChannel);

        // Verify channel-level id, uid, and capitalized UID alias
        assertEquals("switch", adaptedChannel.get("id"));
        assertEquals("mqtt:topic:porch:switch", adaptedChannel.get("uid"));
        assertEquals("mqtt:topic:porch:switch", adaptedChannel.get("UID"));

        // Verify channel-level 'configuration' was renamed to 'config' and holds configuration properties
        @SuppressWarnings("unchecked")
        Map<String, Object> channelConfig = (Map<String, Object>) adaptedChannel.get("config");
        assertNotNull(channelConfig);
        assertEquals("porch/state", channelConfig.get("stateTopic"));
        assertNull(adaptedChannel.get("configuration"));
    }

    @Test
    void handlesEnabledStateTransitionsOnStatusInfoChangedEvent() {
        ThingUID thingUID = new ThingUID("mqtt:topic:porch");
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(thingUID);
        when(thing.getThingTypeUID()).thenReturn(new ThingTypeUID("mqtt:topic"));
        when(thing.isEnabled()).thenReturn(true);
        when(thing.getConfiguration()).thenReturn(new Configuration(Map.of()));

        when(thingRegistry.get(thingUID)).thenReturn(thing);

        ThingStatusInfo oldStatusInfo = mock(ThingStatusInfo.class);
        when(oldStatusInfo.getStatusDetail()).thenReturn(ThingStatusDetail.DISABLED);

        ThingStatusInfo newStatusInfo = mock(ThingStatusInfo.class);
        when(newStatusInfo.getStatusDetail()).thenReturn(ThingStatusDetail.NONE);

        ThingStatusInfoChangedEvent event = mock(ThingStatusInfoChangedEvent.class);
        when(event.getThingUID()).thenReturn(thingUID);
        when(event.getOldStatusInfo()).thenReturn(oldStatusInfo);
        when(event.getStatusInfo()).thenReturn(newStatusInfo);

        provider.receive(event);

        assertEquals(1, emittedChanges.size());
        EntityChange change = emittedChanges.get(0);
        assertEquals("THINGS", change.source());

        assertNotNull(change.oldEntity());
        assertNotNull(change.newEntity());

        assertEquals(false, change.oldEntity().get("enabled"));
        assertEquals(true, change.newEntity().get("enabled"));
    }

    @Test
    void ignoresStatusInfoChangedEventWhenDisabledStateHasNotChanged() {
        ThingUID thingUID = new ThingUID("mqtt:topic:porch");

        ThingStatusInfo oldStatusInfo = mock(ThingStatusInfo.class);
        when(oldStatusInfo.getStatusDetail()).thenReturn(ThingStatusDetail.COMMUNICATION_ERROR);

        ThingStatusInfo newStatusInfo = mock(ThingStatusInfo.class);
        when(newStatusInfo.getStatusDetail()).thenReturn(ThingStatusDetail.NONE);

        ThingStatusInfoChangedEvent event = mock(ThingStatusInfoChangedEvent.class);
        when(event.getThingUID()).thenReturn(thingUID);
        when(event.getOldStatusInfo()).thenReturn(oldStatusInfo);
        when(event.getStatusInfo()).thenReturn(newStatusInfo);

        provider.receive(event);

        assertTrue(emittedChanges.isEmpty(), "No change should be emitted when disabled status is unaffected");
    }

    @Test
    void unregistersListenerOnDeactivate() {
        provider.deactivate();
        verify(thingRegistry).removeRegistryChangeListener(Objects.requireNonNull(provider));
    }
}
