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
package org.openhab.binding.solarman.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.solarman.internal.sunsynk.SunsynkProfile;
import org.openhab.binding.solarman.internal.util.SolarmanWorkQueue;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Exercises dynamic channel upgrades from the archived pre-metadata Thing structure without recreating the Thing.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
class SolarmanExistingThingTest {
    @ParameterizedTest
    @ValueSource(strings = { "initialize", "same-handler", "replacement-handler" })
    void oldSinglePhaseThingAcquiresMetadataWithoutLosingIdentityOrConfiguration(String lifecycle) throws IOException {
        Thing original = historicalThing();
        assertEquals(147, original.getChannels().size());
        Set<String> metadata = Set.of("communication-board-version", "control-board-version", "inverter-id");
        assertTrue(metadata.stream().allMatch(id -> original.getChannel(id) == null));
        var currentProfile = new SunsynkProfile("single_phase");
        var historicalProfile = mock(SunsynkProfile.class);
        Set<String> oldIds = original.getChannels().stream().map(channel -> channel.getUID().getId())
                .collect(Collectors.toSet());
        when(historicalProfile.sensors()).thenReturn(
                currentProfile.sensors().stream().filter(sensor -> oldIds.contains(sensor.channelId())).toList());
        when(historicalProfile.readPlan()).thenReturn(currentProfile.readPlan());
        TestHandler handler = new TestHandler(original);
        try {
            if (!"initialize".equals(lifecycle)) {
                // The previous handler publishes only the channel IDs frozen in the archived fixture.
                handler.profileOverride = historicalProfile;
                handler.initialize();
                assertEquals(oldIds, handler.getThing().getChannels().stream().map(channel -> channel.getUID().getId())
                        .collect(Collectors.toSet()));
                handler.profileOverride = null;
            }
            if ("replacement-handler".equals(lifecycle)) {
                Thing existing = handler.getThing();
                TestHandler previous = handler;
                previous.dispose();
                verify(previous.futures.getFirst()).cancel(true);
                verify(previous.testScheduler, never()).shutdown();
                handler = new TestHandler(existing);
                handler.initialize();
            } else if ("same-handler".equals(lifecycle)) {
                // Exercise the real BaseThingHandler lifecycle entry point with the existing Thing structure.
                handler.thingUpdated(handler.getThing());
                verify(handler.futures.getFirst()).cancel(true);
            } else {
                handler.initialize();
            }
            assertUpgrade(original, handler.getThing(), metadata);
            var firstUpgrade = handler.getThing();
            int lastFuture = handler.futures.size() - 1;
            handler.initialize();
            verify(handler.futures.get(lastFuture)).cancel(true);
            assertUpgrade(original, handler.getThing(), metadata);
            assertEquals(new HashSet<>(firstUpgrade.getChannels()), new HashSet<>(handler.getThing().getChannels()),
                    "A further initialization must not duplicate or change upgraded channels");
        } finally {
            handler.dispose();
        }
        verify(handler.futures.getLast()).cancel(true);
        verify(handler.testScheduler, never()).shutdown();
    }

    private static Thing historicalThing() throws IOException {
        JsonNode fixture;
        try (var stream = Objects.requireNonNull(
                SolarmanExistingThingTest.class.getResourceAsStream("/sunsynk/single-phase-0.5.6-channels.json"))) {
            fixture = new ObjectMapper().readTree(stream);
        }
        assertEquals("5.2.4.202611012143", fixture.path("bundle_version").asText());
        assertEquals("fc00425487332951d444d6815aa993190d98002a4a1ec92989c3dd2b73f3e1ed",
                fixture.path("artifact_sha256").asText());
        var type = SolarmanBindingConstants.THING_TYPE_SOLARMAN_LOGGER;
        var uid = new ThingUID(type, "example");
        var builder = ThingBuilder.create(type, uid).withLabel("Existing inverter").withLocation("Utility room")
                .withProperties(Map.of("serialNumber", "EXAMPLE-SERIAL", "test-property", "retained"))
                .withConfiguration(
                        new Configuration(Map.of("hostname", "127.0.0.1", "port", 8899, "serialNumber", "1234567890",
                                "inverterType", "sunsynk_single_phase", "timeZone", "UTC", "refreshInterval", 45)));
        for (var entry : fixture.path("channels")) {
            builder.withChannel(ChannelBuilder.create(new ChannelUID(uid, entry.path("id").asText()))
                    .withType(new ChannelTypeUID(entry.path("type").asText()))
                    .withAcceptedItemType(entry.path("itemType").asText()).withLabel(entry.path("label").asText())
                    .withProperties(Map.of(SolarmanBindingConstants.DYNAMIC_CHANNEL, "true")).build());
        }
        return builder.build();
    }

    private static void assertUpgrade(Thing original, Thing updated, Set<String> metadata) {
        assertEquals(original.getUID(), updated.getUID());
        assertEquals(original.getThingTypeUID(), updated.getThingTypeUID());
        assertEquals(original.getLabel(), updated.getLabel());
        assertEquals(original.getLocation(), updated.getLocation());
        assertEquals(original.getProperties(), updated.getProperties());
        assertEquals(original.getConfiguration().getProperties(), updated.getConfiguration().getProperties());
        assertEquals(150, updated.getChannels().size());
        Set<ChannelUID> added = updated.getChannels().stream().map(Channel::getUID).collect(Collectors.toSet());
        for (var old : original.getChannels()) {
            Channel retained = Objects.requireNonNull(updated.getChannel(old.getUID().getId()));
            assertEquals(old.getUID(), retained.getUID(), "Existing Item link targets must remain addressable");
            assertEquals(old.getChannelTypeUID(), retained.getChannelTypeUID());
            assertEquals(old.getAcceptedItemType(), retained.getAcceptedItemType());
            assertEquals(old.getLabel(), retained.getLabel());
            assertEquals(old.getKind(), retained.getKind());
            assertEquals(old.getConfiguration().getProperties(), retained.getConfiguration().getProperties());
            assertEquals(old.getProperties(), retained.getProperties());
            added.remove(retained.getUID());
        }
        assertEquals(metadata.stream().map(id -> new ChannelUID(updated.getUID(), id)).collect(Collectors.toSet()),
                added);
        for (String id : metadata) {
            Channel channel = Objects.requireNonNull(updated.getChannel(id));
            assertEquals("String", channel.getAcceptedItemType());
            assertEquals("true", channel.getProperties().get(SolarmanBindingConstants.DYNAMIC_CHANNEL));
        }
    }

    private static class TestHandler extends SolarmanLoggerHandler {
        final ScheduledExecutorService testScheduler = mock(ScheduledExecutorService.class);
        final List<ScheduledFuture<?>> futures = new ArrayList<>();
        @Nullable
        SunsynkProfile profileOverride;

        TestHandler(Thing thing) {
            super(thing);
            ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
            doAnswer(invocation -> {
                setThing(invocation.getArgument(0));
                return null;
            }).when(callback).thingUpdated(any());
            doAnswer(invocation -> {
                ScheduledFuture<?> future = mock(ScheduledFuture.class);
                futures.add(future);
                return future;
            }).when(testScheduler).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                    any(TimeUnit.class));
            setCallback(callback);
        }

        @Override
        protected SunsynkProfile loadProfile(String name) throws IOException {
            @Nullable
            SunsynkProfile override = profileOverride;
            return override == null ? super.loadProfile(name) : override;
        }

        @Override
        protected SolarmanWorkQueue createWorkQueue(Runnable poll, int interval, Consumer<Exception> errors) {
            return new SolarmanWorkQueue(testScheduler, poll, interval, errors);
        }
    }
}
