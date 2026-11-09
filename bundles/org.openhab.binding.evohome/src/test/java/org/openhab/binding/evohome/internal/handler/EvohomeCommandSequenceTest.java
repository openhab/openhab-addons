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
package org.openhab.binding.evohome.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.evohome.internal.EvohomeBindingConstants.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpFields;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.ZoneStatus;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.internal.BridgeImpl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests command ordering and partial controller resets through the API request bodies.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
class EvohomeCommandSequenceTest {
    private static final String TOKEN = "{\"access_token\":\"test-token\",\"expires_in\":3600}";
    private static final String ACCOUNT = "{\"userId\":\"user\"}";
    private static final String LOCATIONS = """
            [{"locationInfo":{"locationId":"location","timeZone":{"timeZoneId":"Europe/Budapest"}},"gateways":[
              {"gatewayInfo":{"gatewayId":"gateway"},"temperatureControlSystems":[
                {"systemId":"controller","zones":[{"zoneId":"zone"},{"zoneId":"second-zone"},
                  {"zoneId":"third-zone"}],"dhw":{"dhwId":"water"},
                  "allowedSystemModes":[{"systemMode":"Auto","canBePermanent":true}]},
                {"systemId":"other-controller","zones":[{"zoneId":"other-zone"}]}]}]}]
            """;
    private static final String STATUS = "{\"locationId\":\"location\",\"gateways\":[]}";

    @ParameterizedTest
    @CsvSource({ "FollowSchedule, temporary, 201, TemporaryOverride",
            "TemporaryOverride, permanent, 201, PermanentOverride", "TemporaryOverride, cancel, 201, PermanentOverride",
            "FollowSchedule, temporary, 422, PermanentOverride", "TemporaryOverride, permanent, 422, TemporaryOverride",
            "TemporaryOverride, cancel, 422, TemporaryOverride" })
    void queuedSetPointUsesResultOfEarlierCommand(String initialMode, String firstOperation, int firstStatus,
            String expectedMode) throws Exception {
        try (Fixture fixture = new Fixture(false)) {
            fixture.start();
            EvohomeHeatingZoneHandler zone = fixture.zone("zone");
            Instant expiry = Instant.now().plusSeconds(7200).truncatedTo(ChronoUnit.SECONDS);
            readback(zone, initialMode, 20, expiry);

            boolean accepted = overlappingCommands(fixture, () -> firstCommand(zone, firstOperation, expiry),
                    () -> zone.handleCommand(channel(zone, "SetPoint"), new DecimalType(22)), firstStatus, () -> {
                    });

            assertEquals(firstStatus == 201, accepted);
            assertEquals(2, fixture.commandBodies().size());
            assertSetPoint(fixture.commandBodies().getLast(), expectedMode, 22, expiry);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "PermanentOverride", "TemporaryOverride" })
    void queuedOverrideModeUsesLatestAcceptedTemperature(String mode) throws Exception {
        try (Fixture fixture = new Fixture(false)) {
            fixture.start();
            EvohomeHeatingZoneHandler zone = fixture.zone("zone");
            Instant expiry = Instant.now().plusSeconds(7200).truncatedTo(ChronoUnit.SECONDS);
            readback(zone, "FollowSchedule", 20, expiry);

            assertTrue(overlappingCommands(fixture, () -> zone.setTemporarySetPoint(21, expiry),
                    () -> zone.handleCommand(channel(zone, "SetPointStatus"), new StringType(mode)), 201, () -> {
                    }));

            JsonObject body = fixture.commandBodies().getLast();
            assertEquals(mode, body.get("setpointMode").getAsString());
            assertEquals(21, body.get("heatSetpointValue").getAsDouble());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void queuedCommandIsDiscardedAfterBridgeReplacementOrZoneDisposal(boolean replaceBridgeClient) throws Exception {
        try (Fixture fixture = new Fixture(false)) {
            fixture.start();
            EvohomeHeatingZoneHandler zone = fixture.zone("zone");
            Instant expiry = Instant.now().plusSeconds(7200).truncatedTo(ChronoUnit.SECONDS);
            readback(zone, "FollowSchedule", 20, expiry);

            boolean accepted = overlappingCommands(fixture, () -> zone.setTemporarySetPoint(21, expiry),
                    () -> zone.handleCommand(channel(zone, "SetPoint"), new DecimalType(22)), 201,
                    replaceBridgeClient ? fixture.handler::initialize : zone::dispose);

            assertEquals(!replaceBridgeClient, accepted);
            assertEquals(1, fixture.commandBodies().size());
        }
    }

    @ParameterizedTest
    @CsvSource({ "0, false", "1, false", "2, false", "3, false", "4, false", "2, true", "4, true" })
    void partialResetUpdatesOnlySuccessfullyCancelledZones(int failedRequest, boolean timeout) throws Exception {
        try (Fixture fixture = new Fixture(false)) {
            fixture.start();
            var zones = List.of(fixture.zone("zone"), fixture.zone("second-zone"), fixture.zone("third-zone"));
            EvohomeHeatingZoneHandler otherZone = fixture.zone("other-zone");
            Instant expiry = Instant.now().plusSeconds(7200).truncatedTo(ChronoUnit.SECONDS);
            doReturn(response(201, "{}")).when(fixture.request).send();
            for (EvohomeHeatingZoneHandler zone : zones) {
                assertTrue(zone.setTemporarySetPoint(21, expiry));
            }
            assertTrue(otherZone.setTemporarySetPoint(21, expiry));
            AtomicInteger resetRequests = new AtomicInteger();
            doAnswer(invocation -> {
                if (resetRequests.getAndIncrement() == failedRequest) {
                    if (timeout) {
                        throw new TimeoutException();
                    }
                    return response(400, "{}");
                }
                return response(201, "{}");
            }).when(fixture.request).send();

            assertFalse(fixture.handler.resetSystem("controller"));
            assertEquals(failedRequest + 1, resetRequests.get());

            doReturn(response(201, "{}")).when(fixture.request).send();
            for (int index = 0; index < zones.size(); index++) {
                EvohomeHeatingZoneHandler zone = zones.get(index);
                boolean cancelled = index + 1 < failedRequest;
                readback(zone, cancelled ? "FollowSchedule" : "TemporaryOverride", 20, expiry);
                zone.handleCommand(channel(zone, "SetPoint"), new DecimalType(22));
                assertSetPoint(fixture.commandBodies().getLast(), cancelled ? "PermanentOverride" : "TemporaryOverride",
                        22, expiry);
            }
            otherZone.handleCommand(channel(otherZone, "SetPoint"), new DecimalType(23));
            assertSetPoint(fixture.commandBodies().getLast(), "TemporaryOverride", 23, expiry);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void completeResetClearsOnlySelectedControllerContexts(boolean nativeReset) throws Exception {
        try (Fixture fixture = new Fixture(nativeReset)) {
            fixture.start();
            var zones = List.of(fixture.zone("zone"), fixture.zone("second-zone"), fixture.zone("third-zone"));
            EvohomeHeatingZoneHandler otherZone = fixture.zone("other-zone");
            Instant expiry = Instant.now().plusSeconds(7200).truncatedTo(ChronoUnit.SECONDS);
            doReturn(response(201, "{}")).when(fixture.request).send();
            for (EvohomeHeatingZoneHandler zone : zones) {
                assertTrue(zone.setTemporarySetPoint(21, expiry));
            }
            assertTrue(otherZone.setTemporarySetPoint(21, expiry));
            int commandsBeforeReset = fixture.commandBodies().size();

            assertTrue(fixture.handler.resetSystem("controller"));
            assertEquals(commandsBeforeReset + (nativeReset ? 1 : 5), fixture.commandBodies().size());

            for (EvohomeHeatingZoneHandler zone : zones) {
                zone.handleCommand(channel(zone, "SetPoint"), new DecimalType(22));
                assertSetPoint(fixture.commandBodies().getLast(), "PermanentOverride", 22, expiry);
            }
            otherZone.handleCommand(channel(otherZone, "SetPoint"), new DecimalType(23));
            assertSetPoint(fixture.commandBodies().getLast(), "TemporaryOverride", 23, expiry);
        }
    }

    private static boolean firstCommand(EvohomeHeatingZoneHandler zone, String operation, Instant expiry) {
        return switch (operation) {
            case "temporary" -> zone.setTemporarySetPoint(21, expiry);
            case "permanent" -> zone.setPermanentSetPoint(21);
            case "cancel" -> zone.cancelSetPointOverride();
            case "next" -> zone.setTemporarySetPointUntilNextSwitchpoint(21);
            case "default" -> zone.setTemporarySetPoint(21);
            default -> throw new IllegalArgumentException(operation);
        };
    }

    @ParameterizedTest
    @CsvSource({ "SetPoint,true", "SetPoint,false", "temporary,true", "permanent,true", "cancel,true", "next,true",
            "default,true" })
    void queuedZoneWriteCannotSurviveZoneReinitialization(String operation, boolean changeZone) throws Exception {
        try (Fixture fixture = new Fixture(false)) {
            fixture.start();
            EvohomeHeatingZoneHandler zone = fixture.zone("zone");
            Instant expiry = Instant.now().plusSeconds(7200).truncatedTo(ChronoUnit.SECONDS);
            readback(zone, "FollowSchedule", 20, expiry);

            assertTrue(overlappingCommands(fixture, () -> zone.setTemporarySetPoint(21, expiry), () -> {
                if ("SetPoint".equals(operation)) {
                    zone.handleCommand(channel(zone, "SetPoint"), new DecimalType(22));
                } else {
                    firstCommand(zone, operation, expiry);
                }
            }, 201, () -> {
                if (changeZone) {
                    zone.getThing().getConfiguration().put("id", "second-zone");
                }
                zone.thingUpdated(zone.getThing());
            }));

            assertEquals(changeZone ? "second-zone" : "zone", zone.getId());
            ArgumentCaptor<String> urls = ArgumentCaptor.forClass(String.class);
            verify(fixture.http, atLeastOnce()).newRequest(urls.capture());
            List<String> writeZones = urls.getAllValues().stream().filter(url -> url.endsWith("/heatSetpoint"))
                    .map(url -> url.contains("/temperatureZone/second-zone/") ? "second-zone" : "zone").toList();
            assertEquals(List.of("zone"), writeZones, "The queued old-lifecycle write must be discarded");
            zone.handleCommand(channel(zone, "SetPoint"), new DecimalType(22));
            assertEquals(2, fixture.commandBodies().size());
            assertSetPoint(fixture.commandBodies().getLast(), "PermanentOverride", 22, expiry);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "temporary", "permanent", "cancel", "next", "default" })
    void disposedZoneRejectsDirectWrite(String operation) throws Exception {
        try (Fixture fixture = new Fixture(false)) {
            fixture.start();
            EvohomeHeatingZoneHandler zone = fixture.zone("zone");
            Instant expiry = Instant.now().plusSeconds(7200).truncatedTo(ChronoUnit.SECONDS);
            readback(zone, "FollowSchedule", 20, expiry);
            doReturn(response(201, "{}")).when(fixture.request).send();
            zone.dispose();
            clearInvocations(fixture.http, fixture.request);

            assertFalse(firstCommand(zone, operation, expiry), "Disposed zone must reject " + operation);
            verifyNoInteractions(fixture.http, fixture.request);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void lateAcceptanceCannotSeedReinitializedZoneContext(boolean reinitializeBeforeAcceptance) throws Exception {
        try (Fixture fixture = new Fixture(false)) {
            fixture.start();
            EvohomeHeatingZoneHandler zone = fixture.zone("zone");
            Instant expiry = Instant.now().plusSeconds(7200).truncatedTo(ChronoUnit.SECONDS);
            readback(zone, "FollowSchedule", 20, expiry);

            assertTrue(overlappingCommands(fixture, () -> zone.setTemporarySetPoint(21, expiry),
                    () -> fixture.handler.executeZoneCommand(() -> {
                    }), 201, () -> {
                        zone.dispose();
                        if (reinitializeBeforeAcceptance) {
                            zone.initialize();
                        }
                    }));
            if (!reinitializeBeforeAcceptance) {
                zone.initialize();
            }
            readback(zone, "FollowSchedule", 20, expiry);
            zone.handleCommand(channel(zone, "SetPoint"), new DecimalType(22));

            assertSetPoint(fixture.commandBodies().getLast(), "PermanentOverride", 22, expiry);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void confirmedRoundedTargetDoesNotMaskExternalCancellation(boolean untilNextSwitchpoint) throws Exception {
        try (Fixture fixture = new Fixture(false)) {
            fixture.start();
            EvohomeHeatingZoneHandler zone = fixture.zone("zone");
            Instant expiry = Instant.now().plusSeconds(7200).truncatedTo(ChronoUnit.SECONDS);
            readback(zone, "FollowSchedule", 20, expiry);
            doReturn(response(201, "{}")).when(fixture.request).send();

            if (untilNextSwitchpoint) {
                String schedule = "{\"dailySchedules\":[" + String.join(",",
                        List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday").stream()
                                .map(day -> "{\"dayOfWeek\":\"%s\",\"switchpoints\":[{\"timeOfDay\":\"12:00:00\"}]}"
                                        .formatted(day))
                                .toList())
                        + "]}";
                doReturn(response(200, schedule), response(201, "{}")).when(fixture.request).send();
                assertTrue(zone.setTemporarySetPointUntilNextSwitchpoint(21.2));
                expiry = Instant.parse(fixture.commandBodies().getLast().get("timeUntil").getAsString());
            } else {
                assertTrue(zone.setTemporarySetPoint(21.2, expiry));
            }
            assertSetPoint(fixture.commandBodies().getLast(), "TemporaryOverride", 21, expiry);
            readback(zone, "TemporaryOverride", 21, expiry);
            readback(zone, "FollowSchedule", 20, expiry);
            zone.handleCommand(channel(zone, "SetPoint"), new DecimalType(22));

            assertSetPoint(fixture.commandBodies().getLast(), "PermanentOverride", 22, expiry);
        }
    }

    @Test
    void lateResetAcceptanceCannotReplaceReinitializedZoneContext() throws Exception {
        try (Fixture fixture = new Fixture(false)) {
            fixture.start();
            EvohomeHeatingZoneHandler zone = fixture.zone("zone");
            Instant expiry = Instant.now().plusSeconds(7200).truncatedTo(ChronoUnit.SECONDS);
            assertTrue(overlappingCommands(fixture, () -> fixture.handler.resetSystem("controller"),
                    () -> fixture.handler.executeZoneCommand(() -> {
                    }), 201, () -> {
                        zone.dispose();
                        zone.initialize();
                        readback(zone, "TemporaryOverride", 21, expiry);
                    }));
            zone.handleCommand(channel(zone, "SetPoint"), new DecimalType(22));
            assertSetPoint(fixture.commandBodies().getLast(), "TemporaryOverride", 22, expiry);
        }
    }

    @Test
    void acceptedPartialResetRemainsEffectiveWhenReadbackIsDelayed() throws Exception {
        try (Fixture fixture = new Fixture(false)) {
            fixture.start();
            EvohomeHeatingZoneHandler zone = fixture.zone("zone");
            Instant expiry = Instant.now().plusSeconds(7200).truncatedTo(ChronoUnit.SECONDS);
            readback(zone, "TemporaryOverride", 21, expiry);
            doReturn(response(201, "{}"), response(201, "{}"), response(400, "{}")).when(fixture.request).send();
            assertFalse(fixture.handler.resetSystem("controller"));

            doReturn(response(201, "{}")).when(fixture.request).send();
            Instant afterPendingWindow = Instant.now().plusSeconds(31);
            try (MockedStatic<Instant> clock = mockStatic(Instant.class, CALLS_REAL_METHODS)) {
                clock.when(Instant::now).thenReturn(afterPendingWindow);
                zone.handleCommand(channel(zone, "SetPoint"), new DecimalType(22));
            }

            assertSetPoint(fixture.commandBodies().getLast(), "PermanentOverride", 22, expiry);
        }
    }

    @Test
    void freshReadbackAfterGraceReplacesAcceptedCancellationContext() throws Exception {
        try (Fixture fixture = new Fixture(false)) {
            fixture.start();
            EvohomeHeatingZoneHandler zone = fixture.zone("zone");
            Instant expiry = Instant.now().plusSeconds(7200).truncatedTo(ChronoUnit.SECONDS);
            readback(zone, "TemporaryOverride", 21, expiry);
            doReturn(response(201, "{}")).when(fixture.request).send();
            assertTrue(zone.cancelSetPointOverride());
            Instant afterGrace = Instant.now().plusSeconds(31);
            try (MockedStatic<Instant> clock = mockStatic(Instant.class, CALLS_REAL_METHODS)) {
                clock.when(Instant::now).thenReturn(afterGrace);
                readback(zone, "TemporaryOverride", 23, expiry);
                zone.handleCommand(channel(zone, "SetPoint"), new DecimalType(22));
            }
            assertSetPoint(fixture.commandBodies().getLast(), "TemporaryOverride", 22, expiry);
        }
    }

    private static ChannelUID channel(EvohomeHeatingZoneHandler zone, String id) {
        return new ChannelUID(zone.getThing().getUID(), id);
    }

    private static void readback(EvohomeHeatingZoneHandler zone, String mode, double temperature, Instant expiry) {
        JsonObject setpoint = new JsonObject();
        setpoint.addProperty("setpointMode", mode);
        setpoint.addProperty("targetHeatTemperature", temperature);
        if ("TemporaryOverride".equals(mode)) {
            setpoint.addProperty("until", expiry.toString());
        }
        JsonObject status = new JsonObject();
        status.addProperty("zoneId", zone.getId());
        status.add("activeFaults", new JsonArray());
        status.add("setpointStatus", setpoint);
        zone.update(null, Objects.requireNonNull(new Gson().fromJson(status, ZoneStatus.class)));
    }

    private static void assertSetPoint(JsonObject body, String mode, double temperature, Instant expiry) {
        assertEquals(mode, body.get("setpointMode").getAsString(), body.toString());
        assertEquals(temperature, body.get("heatSetpointValue").getAsDouble());
        if ("TemporaryOverride".equals(mode)) {
            assertEquals(expiry.toString(), body.get("timeUntil").getAsString());
        } else {
            assertFalse(body.has("timeUntil"));
        }
    }

    private static boolean overlappingCommands(Fixture fixture, Callable<Boolean> firstCommand, Runnable queuedCommand,
            int firstStatus, Runnable whileBlocked) throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger requests = new AtomicInteger();
        doAnswer(invocation -> {
            if (requests.getAndIncrement() == 0) {
                entered.countDown();
                assertTrue(release.await(10, TimeUnit.SECONDS));
                return response(firstStatus, "{}");
            }
            return response(201, "{}");
        }).when(fixture.request).send();
        AtomicReference<@Nullable Thread> queuedWorker = new AtomicReference<>();
        var executor = Executors.newFixedThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable, "evohome-command-sequence-test");
            thread.setDaemon(true);
            return thread;
        });
        try {
            var first = executor.submit(firstCommand);
            assertTrue(entered.await(10, TimeUnit.SECONDS));
            var queued = executor.submit(() -> {
                queuedWorker.set(Thread.currentThread());
                queuedCommand.run();
            });
            assertTrue(awaitBlocked(queuedWorker));
            whileBlocked.run();
            release.countDown();
            boolean accepted = first.get(10, TimeUnit.SECONDS);
            queued.get(10, TimeUnit.SECONDS);
            return accepted;
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private static boolean awaitBlocked(AtomicReference<@Nullable Thread> worker) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            Thread thread = worker.get();
            if (thread != null && thread.getState() == Thread.State.BLOCKED) {
                return true;
            }
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        }
        return false;
    }

    private static ContentResponse response(int code, String json) {
        return mock(ContentResponse.class, invocation -> switch (invocation.getMethod().getName()) {
            case "getStatus" -> code;
            case "getContentAsString" -> json;
            case "getHeaders" -> new HttpFields();
            default -> RETURNS_DEFAULTS.answer(invocation);
        });
    }

    private static class Fixture implements AutoCloseable {
        private final HttpClient http = mock(HttpClient.class);
        private final Request request = mock(Request.class, RETURNS_SELF);
        private final ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        private final List<Runnable> tasks = new CopyOnWriteArrayList<>();
        private final List<EvohomeHeatingZoneHandler> zones = new CopyOnWriteArrayList<>();
        private final Bridge bridge = BridgeBuilder.create(THING_TYPE_EVOHOME_ACCOUNT, "sequence")
                .withConfiguration(
                        new Configuration(Map.of("username", "user", "password", "password", "refreshInterval", 15)))
                .build();
        private final EvohomeAccountBridgeHandler handler = new EvohomeAccountBridgeHandler(bridge, http, scheduler);

        private Fixture(boolean nativeReset) throws Exception {
            when(http.newRequest(anyString())).thenReturn(request);
            when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT),
                    response(200, nativeReset ? LOCATIONS.replace("\"Auto\"", "\"AutoWithReset\"") : LOCATIONS),
                    response(200, STATUS));
            when(scheduler.schedule(any(Runnable.class), anyLong(), eq(TimeUnit.SECONDS))).thenAnswer(invocation -> {
                tasks.add(Objects.requireNonNull(invocation.getArgument(0, Runnable.class)));
                return mock(ScheduledFuture.class);
            });
            ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
            handler.setCallback(callback);
            doAnswer(invocation -> {
                bridge.setStatusInfo(Objects.requireNonNull(invocation.getArgument(1, ThingStatusInfo.class)));
                return null;
            }).when(callback).statusUpdated(eq(bridge), any(ThingStatusInfo.class));
        }

        private void start() {
            handler.initialize();
            tasks.getFirst().run();
        }

        private EvohomeHeatingZoneHandler zone(String zoneId) {
            Thing thing = ThingBuilder.create(THING_TYPE_EVOHOME_HEATING_ZONE, zoneId).withBridge(bridge.getUID())
                    .withConfiguration(new Configuration(Map.of("id", zoneId))).build();
            EvohomeHeatingZoneHandler zone = new EvohomeHeatingZoneHandler(thing) {
                @Override
                protected @Nullable EvohomeAccountBridgeHandler getEvohomeBridge() {
                    return handler;
                }
            };
            zone.setCallback(mock(ThingHandlerCallback.class));
            zone.initialize();
            thing.setHandler(zone);
            ((BridgeImpl) bridge).addThing(thing);
            zones.add(zone);
            return zone;
        }

        private List<JsonObject> commandBodies() {
            ArgumentCaptor<ContentProvider> content = ArgumentCaptor.forClass(ContentProvider.class);
            verify(request, atLeastOnce()).content(content.capture(), eq("application/json"));
            return content.getAllValues().stream().map(provider -> {
                StringBuilder body = new StringBuilder();
                for (ByteBuffer buffer : provider) {
                    body.append(StandardCharsets.UTF_8.decode(buffer.duplicate()));
                }
                return JsonParser.parseString(body.toString()).getAsJsonObject();
            }).toList();
        }

        @Override
        public void close() {
            zones.forEach(EvohomeHeatingZoneHandler::dispose);
            handler.dispose();
        }
    }
}
