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
package org.openhab.binding.caldav.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openhab.core.config.core.ConfigDescriptionRegistry;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingFactory;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeBuilder;

/**
 * Calendar channel creation preserves existing Thing structure and metadata.
 *
 * @author Andreas Vilippus - Initial contribution
 */
@NonNullByDefault
class CalDavHandlerFactoryTest {
    private interface CalendarStorage extends Storage<String> {
    }

    private static final class Factory extends CalDavHandlerFactory {
        private final ThingType type;

        Factory(ThingType type) {
            super(mock(HttpClientFactory.class), () -> ZoneOffset.UTC, storageService());
            this.type = type;
        }

        private static StorageService storageService() {
            StorageService service = mock(StorageService.class);
            when(service.<String> getStorage("caldav-calendar-cache")).thenReturn(mock(CalendarStorage.class));
            return service;
        }

        @Override
        protected @Nullable ThingType getThingTypeByUID(ThingTypeUID uid) {
            return type.getUID().equals(uid) ? type : null;
        }

        @Override
        protected @Nullable ConfigDescriptionRegistry getConfigDescriptionRegistry() {
            return null;
        }
    }

    @Test
    void addsSingleStringColorChannelAndPreservesGroupedChannelsAndMetadata() {
        ThingType type = ThingTypeBuilder.instance("caldav", "calendar", "Calendar").build();
        ThingUID bridge = new ThingUID("caldav:account:example");
        ThingUID uid = new ThingUID("caldav:calendar:example:family");
        Configuration configuration = new Configuration(Map.of("path", "/calendar/"));
        Channel events = ChannelBuilder.create(new ChannelUID(uid, "events#json"), "String")
                .withType(new ChannelTypeUID("caldav", "events-json")).build();
        Thing original = ThingBuilder.create(type.getUID(), uid).withBridge(bridge).withLabel("Family")
                .withConfiguration(configuration).withChannel(events)
                .withProperties(
                        Map.of("calendarColor", "#CEE7FFFF", "calendarPrivileges", "read", "thingTypeVersion", "1"))
                .build();
        try (MockedStatic<ThingFactory> core = mockStatic(ThingFactory.class)) {
            core.when(() -> ThingFactory.createThing(type, uid, configuration, bridge, null)).thenReturn(original);
            Thing created = Objects
                    .requireNonNull(new Factory(type).createThing(type.getUID(), configuration, uid, bridge));
            assertEquals(uid, created.getUID());
            assertEquals(bridge, created.getBridgeUID());
            assertEquals(original.getLabel(), created.getLabel());
            assertEquals(configuration, created.getConfiguration());
            assertEquals(original.getProperties(), created.getProperties());
            assertSame(events, created.getChannel("events#json"));
            assertEquals(2, created.getChannels().size());
            Channel color = Objects.requireNonNull(created.getChannel("calendar-color"));
            assertEquals("String", color.getAcceptedItemType());
            assertEquals(new ChannelTypeUID("caldav", "calendar-color"), color.getChannelTypeUID());
            assertEquals(new ChannelUID(uid, "calendar-color"), color.getUID());
            assertNull(original.getChannel("calendar-color"));
        }
    }

    @Test
    void alreadyPresentCalendarColorIsNotDuplicated() {
        ThingType type = ThingTypeBuilder.instance("caldav", "calendar", "Calendar").build();
        ThingUID uid = new ThingUID("caldav:calendar:example:family");
        Configuration configuration = new Configuration();
        Thing original = ThingBuilder.create(type.getUID(), uid)
                .withChannel(ChannelBuilder.create(new ChannelUID(uid, "calendar-color"), "String")
                        .withType(new ChannelTypeUID("caldav", "calendar-color")).build())
                .build();
        try (MockedStatic<ThingFactory> core = mockStatic(ThingFactory.class)) {
            core.when(() -> ThingFactory.createThing(type, uid, configuration, null, null)).thenReturn(original);
            assertSame(original, new Factory(type).createThing(type.getUID(), configuration, uid, null));
            assertEquals(1, original.getChannels().size());
        }
    }

    @Test
    void accountCreationDoesNotAddCalendarColor() {
        ThingType type = ThingTypeBuilder.instance("caldav", "account", "Account").buildBridge();
        ThingUID uid = new ThingUID("caldav:account:example");
        Configuration configuration = new Configuration();
        Thing original = BridgeBuilder.create(type.getUID(), uid).build();
        try (MockedStatic<ThingFactory> core = mockStatic(ThingFactory.class)) {
            core.when(() -> ThingFactory.createThing(type, uid, configuration, null, null)).thenReturn(original);
            assertSame(original, new Factory(type).createThing(type.getUID(), configuration, uid, null));
            assertNull(original.getChannel("calendar-color"));
        }
    }

    @Test
    void calendarTypeFromAnotherBindingIsNotModified() {
        ThingType type = ThingTypeBuilder.instance("other", "calendar", "Other Calendar").build();
        ThingUID uid = new ThingUID("other:calendar:example");
        Configuration configuration = new Configuration();
        Thing original = ThingBuilder.create(type.getUID(), uid).build();
        try (MockedStatic<ThingFactory> core = mockStatic(ThingFactory.class)) {
            core.when(() -> ThingFactory.createThing(type, uid, configuration, null, null)).thenReturn(original);
            assertSame(original, new Factory(type).createThing(type.getUID(), configuration, uid, null));
            assertNull(original.getChannel("calendar-color"));
        }
    }

    @Test
    void unavailableThingTypeStillReturnsNull() {
        ThingType type = ThingTypeBuilder.instance("caldav", "account", "Account").buildBridge();
        assertNull(new Factory(type).createThing(new ThingTypeUID("caldav", "calendar"), new Configuration(),
                new ThingUID("caldav:calendar:example:family"), null));
    }
}
