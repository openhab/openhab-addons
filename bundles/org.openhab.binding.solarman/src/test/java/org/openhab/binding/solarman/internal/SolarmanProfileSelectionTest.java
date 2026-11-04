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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.solarman.internal.sunsynk.SunsynkProfile;
import org.openhab.binding.solarman.internal.util.SolarmanWorkQueue;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.w3c.dom.Element;

/**
 * Verifies opt-in profile selection, unchanged legacy channels and one lifecycle owner across reconfiguration.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
class SolarmanProfileSelectionTest {
    private static Stream<String> advancedSelections() {
        return SolarmanBindingConstants.SUNSYNK_INVERTER_TYPES.keySet().stream().sorted();
    }

    private static List<String> selectorValues() throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        try (var stream = Objects.requireNonNull(
                SolarmanProfileSelectionTest.class.getResourceAsStream("/OH-INF/thing/thing-types.xml"))) {
            var parameters = factory.newDocumentBuilder().parse(stream).getElementsByTagName("parameter");
            List<String> names = new ArrayList<>();
            for (int index = 0; index < parameters.getLength(); index++) {
                Element parameter = (Element) parameters.item(index);
                if ("inverterType".equals(parameter.getAttribute("name"))) {
                    var options = parameter.getElementsByTagName("option");
                    for (int option = 0; option < options.getLength(); option++) {
                        names.add(((Element) options.item(option)).getAttribute("value"));
                    }
                }
            }
            return names;
        }
    }

    private static Stream<String> legacySelections() throws Exception {
        var names = selectorValues().stream()
                .filter(value -> !SolarmanBindingConstants.SUNSYNK_INVERTER_TYPES.containsKey(value)).toList();
        assertEquals(18, names.size());
        return names.stream();
    }

    @Test
    void publishedSelectorsAndFactorySupportUnifiedAndCompatibilityThings() throws Exception {
        var names = selectorValues();
        assertEquals(23, names.size());
        assertTrue(names.containsAll(SolarmanBindingConstants.SUNSYNK_INVERTER_TYPES.keySet()));
        assertEquals(new java.util.HashSet<>(SolarmanBindingConstants.SUNSYNK_PROFILES.values()),
                new java.util.HashSet<>(SolarmanBindingConstants.SUNSYNK_INVERTER_TYPES.values()));
        SolarmanHandlerFactory factory = new SolarmanHandlerFactory();
        for (String name : names) {
            assertInstanceOf(SolarmanLoggerHandler.class, factory.createHandler(thing(name)));
        }
        for (var type : SolarmanBindingConstants.SUNSYNK_PROFILES.keySet()) {
            assertTrue(factory.supportsThingType(type));
            assertInstanceOf(org.openhab.binding.solarman.internal.sunsynk.SunsynkHandler.class,
                    factory.createHandler(ThingBuilder.create(type, new ThingUID(type, "compatibility")).build()));
        }
    }

    @ParameterizedTest
    @MethodSource("advancedSelections")
    void advancedSelectionBuildsEveryTypedChannelOnTheLogger(String selection) throws IOException {
        TestHandler handler = new TestHandler(thing(selection));
        try {
            handler.initialize();
            var profile = new SunsynkProfile(
                    Objects.requireNonNull(SolarmanBindingConstants.SUNSYNK_INVERTER_TYPES.get(selection)));
            assertEquals(profile.sensors().size(), handler.getThing().getChannels().size());
            for (var sensor : profile.sensors()) {
                var channel = Objects.requireNonNull(handler.getThing().getChannel(sensor.channelId()));
                assertEquals(sensor.itemType(), channel.getAcceptedItemType());
                assertEquals(
                        SolarmanBindingConstants.SUNSYNK_CHANNEL_TYPE_IDS
                                .get(profileName(selection) + ":" + sensor.id()),
                        Objects.requireNonNull(channel.getChannelTypeUID()).getId());
                assertTrue(channel.getProperties().containsKey(SolarmanBindingConstants.DYNAMIC_CHANNEL));
                assertEquals(handler.getThing().getUID(), channel.getUID().getThingUID());
            }
            assertEquals(1, handler.futures.size());
            handler.handleCommand(new ChannelUID(handler.getThing().getUID(), "unknown"),
                    org.openhab.core.types.RefreshType.REFRESH);
        } finally {
            handler.dispose();
        }
        verify(handler.futures.getFirst()).cancel(true);
        verify(handler.testScheduler, never()).shutdown();
    }

    @ParameterizedTest
    @MethodSource("legacySelections")
    void everyExistingSelectionKeepsItsOriginalGeneratedChannels(String selection) {
        TestHandler handler = new TestHandler(thing(selection));
        try {
            handler.initialize();
            var definition = Objects.requireNonNull(new DefinitionParser().parseDefinition(selection));
            var expected = new org.openhab.binding.solarman.internal.channel.SolarmanChannelManager()
                    .generateItemChannelMap(thing(selection), definition).values().stream()
                    .map(channel -> channel.getUID().getId()).sorted().toList();
            assertEquals(expected, handler.getThing().getChannels().stream().map(channel -> channel.getUID().getId())
                    .sorted().toList());
            assertEquals(1, handler.futures.size());
        } finally {
            handler.dispose();
        }
    }

    @Test
    void switchingProfilesCancelsEachOldWorkerAndRemovesObsoleteDynamicChannels() {
        TestHandler handler = new TestHandler(thing("deye_hybrid"));
        try {
            handler.initialize();
            List<String> original = handler.getThing().getChannels().stream().map(channel -> channel.getUID().getId())
                    .sorted().toList();
            handler.select("sunsynk_single_phase");
            assertNotNull(handler.getThing().getChannel("prog6-capacity"));
            assertNull(handler.getThing().getChannel("time-of-use-time-of-use-soc-6"));
            verify(handler.futures.get(0)).cancel(true);
            handler.select("sunsynk_micro_hybrid_ess");
            assertEquals(86, handler.getThing().getChannels().size());
            verify(handler.futures.get(1)).cancel(true);
            handler.select("deye_hybrid");
            assertEquals(original, handler.getThing().getChannels().stream().map(channel -> channel.getUID().getId())
                    .sorted().toList());
            verify(handler.futures.get(2)).cancel(true);
        } finally {
            handler.dispose();
        }
        verify(handler.futures.get(3)).cancel(true);
        assertEquals(4, handler.futures.size());
    }

    @Test
    void disposalDuringUnifiedProfileLoadingPreventsChannelPublicationAndScheduling() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        TestHandler handler = new TestHandler(thing("sunsynk_single_phase")) {
            @Override
            protected SunsynkProfile loadProfile(String name) throws IOException {
                entered.countDown();
                try {
                    if (!release.await(5, TimeUnit.SECONDS)) {
                        throw new IOException("Profile loading test timed out");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Profile loading interrupted", e);
                }
                return super.loadProfile(name);
            }
        };
        FutureTask<@Nullable Void> task = new FutureTask<>(() -> {
            handler.initialize();
            return null;
        });
        Thread thread = Thread.ofVirtual().name("solarman-selector-test").start(task);
        try {
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            handler.dispose();
            clearInvocations(handler.callback);
            release.countDown();
            task.get(5, TimeUnit.SECONDS);
            assertTrue(handler.futures.isEmpty());
            verifyNoInteractions(handler.callback);
        } finally {
            release.countDown();
            task.cancel(true);
            thread.join(5000);
            handler.dispose();
            assertFalse(thread.isAlive());
        }
    }

    @Test
    void disposalFromChannelCallbackPreventsAdoptingTheNewWorker() {
        TestHandler handler = new TestHandler(thing("sunsynk_single_phase"));
        doAnswer(invocation -> {
            handler.dispose();
            return null;
        }).when(handler.callback).thingUpdated(any());
        try {
            handler.initialize();
            assertTrue(handler.futures.isEmpty());
        } finally {
            handler.dispose();
        }
    }

    @Test
    void advancedSelectionRejectsLegacyAdditionalRequestsWithoutScheduling() {
        Thing initial = thing("sunsynk_single_phase");
        initial.getConfiguration().put("additionalRequests", "0x03:0x0000-0x0001");
        TestHandler handler = new TestHandler(initial);
        try {
            handler.initialize();
            assertTrue(handler.futures.isEmpty());
            var status = org.mockito.ArgumentCaptor.forClass(ThingStatusInfo.class);
            verify(handler.callback).statusUpdated(any(), status.capture());
            assertEquals(ThingStatus.OFFLINE, status.getValue().getStatus());
            assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, status.getValue().getStatusDetail());
        } finally {
            handler.dispose();
        }
    }

    @Test
    void legacyOneSecondRefreshRemainsPublishedAndAccepted() throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        try (var stream = Objects.requireNonNull(getClass().getResourceAsStream("/OH-INF/thing/thing-types.xml"))) {
            var parameters = factory.newDocumentBuilder().parse(stream).getElementsByTagName("parameter");
            boolean found = false;
            for (int index = 0; index < parameters.getLength(); index++) {
                Element parameter = (Element) parameters.item(index);
                if ("refreshInterval".equals(parameter.getAttribute("name"))) {
                    assertEquals("1", parameter.getAttribute("min"));
                    assertEquals("s", parameter.getAttribute("unit"));
                    found = true;
                }
            }
            assertTrue(found);
        }
        Thing initial = thing("sunwoda_hybrid");
        initial.getConfiguration().put("refreshInterval", 1);
        var handler = new TestHandler(initial);
        try {
            handler.initialize();
            assertEquals(1, handler.futures.size());
            assertFalse(handler.getThing().getChannels().isEmpty());
        } finally {
            handler.dispose();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 4, 5 })
    void advancedRefreshRetainsItsFiveSecondMinimum(int interval) {
        Thing initial = thing("sunsynk_single_phase");
        initial.getConfiguration().put("refreshInterval", interval);
        var handler = new TestHandler(initial);
        try {
            handler.initialize();
            if (interval < 5) {
                assertTrue(handler.futures.isEmpty());
                var status = org.mockito.ArgumentCaptor.forClass(ThingStatusInfo.class);
                verify(handler.callback).statusUpdated(any(), status.capture());
                assertEquals(ThingStatus.OFFLINE, status.getValue().getStatus());
                assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, status.getValue().getStatusDetail());
            } else {
                assertEquals(1, handler.futures.size());
                assertNotNull(handler.getThing().getChannel("prog6-capacity"));
            }
        } finally {
            handler.dispose();
        }
    }

    private static String profileName(String selection) {
        return Objects.requireNonNull(SolarmanBindingConstants.SUNSYNK_INVERTER_TYPES.get(selection));
    }

    private static Thing thing(String selection) {
        var type = SolarmanBindingConstants.THING_TYPE_SOLARMAN_LOGGER;
        return ThingBuilder
                .create(type, new ThingUID(type, "example")).withConfiguration(new Configuration(Map.of("hostname",
                        "127.0.0.1", "serialNumber", "1234567890", "inverterType", selection, "timeZone", "UTC")))
                .build();
    }

    private static class TestHandler extends SolarmanLoggerHandler {
        final ScheduledExecutorService testScheduler = mock(ScheduledExecutorService.class);
        final List<ScheduledFuture<?>> futures = new ArrayList<>();
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

        TestHandler(Thing thing) {
            super(thing);
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
        protected SolarmanWorkQueue createWorkQueue(Runnable poll, int interval, Consumer<Exception> errors) {
            return new SolarmanWorkQueue(testScheduler, poll, interval, errors);
        }

        void select(String selection) {
            var config = new Configuration(getThing().getConfiguration().getProperties());
            config.put("inverterType", selection);
            setThing(ThingBuilder.create(getThing()).withConfiguration(config).build());
            initialize();
        }
    }
}
