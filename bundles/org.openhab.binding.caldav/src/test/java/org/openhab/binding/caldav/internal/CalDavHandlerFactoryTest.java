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
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openhab.binding.caldav.internal.discovery.CalDavDiscoveryService;
import org.openhab.binding.caldav.internal.handler.AccountHandler;
import org.openhab.core.config.core.ConfigDescriptionRegistry;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
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
 * @author Andreas Vilippus - Registry dependency wiring for calendar metadata
 */
@NonNullByDefault
class CalDavHandlerFactoryTest {
    /** Typed storage contract used by calendar test fixtures and mocks. */
    private interface CalendarStorage extends Storage<String> {
    }

    /** Handler factory fixture with a fixed Thing type and supplied Core service dependencies. */
    private static final class Factory extends CalDavHandlerFactory {
        private final ThingType type;

        /**
         * Creates a factory fixture with a predefined Thing type and supplied Core services.
         *
         * @param type Thing type returned by the factory fixture
         */
        Factory(ThingType type) {
            this(type, Objects.requireNonNull(mock(ThingRegistry.class)),
                    Objects.requireNonNull(mock(HttpClientFactory.class)));
        }

        /**
         * Creates a factory fixture with a predefined Thing type and supplied Core services.
         *
         * @param type Thing type returned by the factory fixture
         * @param registry registry used for calendar representation updates
         * @param httpFactory Core factory for account HTTP clients
         */
        Factory(ThingType type, ThingRegistry registry, HttpClientFactory httpFactory) {
            super(httpFactory, () -> ZoneOffset.UTC, storageService(), registry);
            this.type = type;
        }

        /**
         * Exposes handler creation for dependency wiring assertions.
         *
         * @param thing calendar or account Thing to handle
         * @return the handler created for the supplied Thing
         */
        @Nullable
        ThingHandler handler(Thing thing) {
            return createHandler(thing);
        }

        private static StorageService storageService() {
            StorageService service = mock(StorageService.class);
            when(service.<String> getStorage("caldav-calendar-cache")).thenReturn(mock(CalendarStorage.class));
            return service;
        }

        /**
         * Supplies the fixture Thing type without a running type registry.
         *
         * @param uid requested Thing type UID
         * @return the fixture Thing type when its UID matches
         */
        @Override
        protected @Nullable ThingType getThingTypeByUID(ThingTypeUID uid) {
            return type.getUID().equals(uid) ? type : null;
        }

        /**
         * Disables configuration-description lookup in the factory fixture.
         *
         * @return null because this fixture does not resolve configuration descriptions
         */
        @Override
        protected @Nullable ConfigDescriptionRegistry getConfigDescriptionRegistry() {
            return null;
        }
    }

    /** Verifies that configured calendars are read from the injected registry before discovery starts. */
    @Test
    void accountHandlerUsesInjectedRegistryBeforeDiscovery() {
        ThingType type = ThingTypeBuilder.instance("caldav", "account", "Account").buildBridge();
        ThingRegistry registry = Objects.requireNonNull(mock(ThingRegistry.class));
        HttpClientFactory httpFactory = Objects.requireNonNull(mock(HttpClientFactory.class));
        when(httpFactory.createHttpClient(anyString(), any(SslContextFactory.Client.class)))
                .thenReturn(Objects.requireNonNull(mock(HttpClient.class)));
        Thing bridge = BridgeBuilder.create(type.getUID(), "wired")
                .withConfiguration(new Configuration(Map.of("url", "https://example.org/"))).build();
        AccountHandler account = assertInstanceOf(AccountHandler.class,
                new Factory(type, registry, httpFactory).handler(bridge));
        account.setCallback(Objects.requireNonNull(mock(ThingHandlerCallback.class)));
        CalDavDiscoveryService discovery = Objects.requireNonNull(mock(CalDavDiscoveryService.class));
        account.registerDiscoveryService(discovery);
        try {
            account.initialize();
            var order = inOrder(registry, discovery);
            order.verify(registry).getAll();
            order.verify(discovery).startScan();
        } finally {
            account.dispose();
        }
    }

    /** Checks that calendar creation adds one ungrouped color channel while preserving the existing Thing structure. */
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

    /** Checks that creation preserves an existing color channel without duplication. */
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

    /** Checks that account bridges do not receive a calendar color channel. */
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

    /** Checks that calendar types from other bindings remain untouched. */
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

    /** Checks that missing Thing metadata prevents creation rather than producing an incomplete Thing. */
    @Test
    void unavailableThingTypeStillReturnsNull() {
        ThingType type = ThingTypeBuilder.instance("caldav", "account", "Account").buildBridge();
        assertNull(new Factory(type).createThing(new ThingTypeUID("caldav", "calendar"), new Configuration(),
                new ThingUID("caldav:calendar:example:family"), null));
    }
}
