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
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.solarman.internal.defmodel.Request;
import org.openhab.binding.solarman.internal.updater.SolarmanChannelUpdater;
import org.openhab.binding.solarman.internal.updater.SolarmanProcessResult;
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
import org.w3c.dom.Element;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Compares real handler publication/polling with independently executed, pinned upstream Java output.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
class SolarmanLegacyBaselineTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonNode BASELINE = resource("main-baseline.json");
    private static final JsonNode DIFFERENCES = resource("main-reviewed-differences.json");

    private static JsonNode resource(String name) {
        try (var stream = Objects
                .requireNonNull(SolarmanLegacyBaselineTest.class.getResourceAsStream("/legacy/" + name))) {
            return MAPPER.readTree(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Stream<String> profiles() {
        return BASELINE.path("profiles").properties().stream().map(Map.Entry::getKey).sorted();
    }

    @Test
    void frozenBaselineCoversEveryPublishedLegacyProfileAndOnlyReviewedDifferences() throws Exception {
        assertEquals("821bb618754b6a0973ae4a11a28ac93133be278c", BASELINE.path("revision").asText());
        assertEquals(BASELINE.path("revision"), DIFFERENCES.path("baseline_revision"));
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Set<String> published = new TreeSet<>();
        try (var stream = Objects.requireNonNull(getClass().getResourceAsStream("/OH-INF/thing/thing-types.xml"))) {
            var parameters = factory.newDocumentBuilder().parse(stream).getElementsByTagName("parameter");
            for (int index = 0; index < parameters.getLength(); index++) {
                Element parameter = (Element) parameters.item(index);
                if ("inverterType".equals(parameter.getAttribute("name"))) {
                    var options = parameter.getElementsByTagName("option");
                    for (int option = 0; option < options.getLength(); option++) {
                        String value = ((Element) options.item(option)).getAttribute("value");
                        if (!SolarmanBindingConstants.SUNSYNK_INVERTER_TYPES.containsKey(value)) {
                            published.add(value);
                        }
                    }
                }
            }
        }
        assertEquals(new TreeSet<>(profiles().toList()), published);
        assertEquals(18, published.size());
        assertEquals(966, profiles().mapToInt(profile -> baseline(profile).path("channels").size()).sum());
        assertEquals(8, DIFFERENCES.path("item_type_changes").size());
        assertEquals(5, DIFFERENCES.path("request_changes").size());
        for (var difference : DIFFERENCES.path("item_type_changes").properties()) {
            String[] identity = difference.getKey().split("/", 2);
            var channel = baseline(identity[0]).path("channels").valueStream()
                    .filter(node -> identity[1].equals(node.path("id").asText())).findFirst().orElseThrow();
            assertEquals(channel.path("itemType"), difference.getValue().path("before"));
            assertEquals("Number:Time", difference.getValue().path("after").asText());
            assertFalse(difference.getValue().path("reason").asText().isBlank());
        }
    }

    @Test
    void mainBaselineRetainsHistoricalProfilesExceptExplicitUpstreamRemovals() {
        var historical = resource("upstream-baseline.json");
        assertEquals("908b3ddfa388885edb9f72ca37b73802de121aab", historical.path("revision").asText());
        assertEquals(17, historical.path("profiles").size());
        assertEquals(905,
                historical.path("profiles").valueStream().mapToInt(profile -> profile.path("channels").size()).sum());
        Set<String> removed = Set.of("inverter-gen-connected-status", "inverter-gen-power");
        historical.path("profiles").properties().forEach(entry -> {
            String profile = entry.getKey();
            var previous = entry.getValue();
            var current = baseline(profile);
            assertEquals(previous.path("requests"), current.path("requests"), profile);
            var expectedChannels = previous.path("channels").valueStream()
                    .filter(channel -> !"deye_hybrid".equals(profile) || !removed.contains(channel.path("id").asText()))
                    .toList();
            assertEquals(expectedChannels, current.path("channels").valueStream().toList(), profile);
        });
        assertEquals(18, BASELINE.path("profiles").size());
        var sunwoda = baseline("sunwoda_hybrid");
        assertFalse(sunwoda.isMissingNode());
        assertEquals(5, sunwoda.path("requests").size());
        assertTrue(sunwoda.path("requests").valueStream().allMatch(request -> request.path("function").asInt() == 4));
    }

    @ParameterizedTest
    @MethodSource("profiles")
    void legacyPublicationAndPollingMatchPinnedUpstreamWithExplicitDifferences(String profile) throws Exception {
        compare(profile, false);
    }

    @ParameterizedTest
    @MethodSource("profiles")
    void customChannelsAndAdditionalReadsPreserveBaselineWithoutCrossFunctionMerging(String profile) throws Exception {
        compare(profile, true);
    }

    private void compare(String profile, boolean custom) throws Exception {
        Thing thing = thing(profile);
        List<List<Integer>> requested = new ArrayList<>();
        if (custom) {
            var first = baseline(profile).path("requests").get(0);
            thing.getConfiguration().put("additionalRequests",
                    first.path("function").asInt() + ":" + first.path("start").asInt() + "-" + first.path("end").asInt()
                            + ",0x03:0xFFFA-0xFFFF,0x04:0xFFFA-0xFFFF");
            thing = ThingBuilder.create(thing).withChannel(customChannel(thing)).build();
        }
        var handler = new TestHandler(thing);
        try (var updater = mockConstruction(SolarmanChannelUpdater.class, (instance, context) -> {
            when(instance.fetchDataFromLogger(anyList(), any(), any(), anyMap())).thenAnswer(invocation -> {
                List<Request> plan = invocation.getArgument(0);
                plan.forEach(request -> requested
                        .add(List.of(request.getMbFunctioncode(), request.getStart(), request.getEnd())));
                return new SolarmanProcessResult();
            });
        })) {
            try {
                handler.initialize();
                assertEquals(1, updater.constructed().size());
                assertEquals(baseline(profile).path("channels").size() + (custom ? 1 : 0),
                        handler.getThing().getChannels().size());
                for (var expected : baseline(profile).path("channels")) {
                    assertChannel(profile, expected, handler.getThing());
                }
                if (custom) {
                    assertEquals(customChannel(thing), handler.getThing().getChannel("custom-power"));
                }
                handler.poll.run();
                verify(updater.constructed().getFirst()).fetchDataFromLogger(anyList(), any(), any(), anyMap());
                List<List<Integer>> original = requests(baseline(profile).path("requests"));
                Set<String> expectedCoverage = coverage(original);
                if (custom) {
                    expectedCoverage.addAll(coverage(List.of(List.of(3, 65530, 65535), List.of(4, 65530, 65535))));
                } else {
                    var reviewed = DIFFERENCES.path("request_changes").path(profile);
                    assertEquals(reviewed.isMissingNode() ? original : requests(reviewed.path("expected")), requested,
                            "Only explicitly reviewed batching/order changes are allowed: " + profile);
                }
                assertEquals(expectedCoverage, coverage(requested), profile);
                assertEquals(expectedCoverage.size(),
                        requested.stream().mapToInt(row -> row.get(2) - row.get(1) + 1).sum(),
                        "No register is read twice for the same function");
                assertTrue(requested.stream().allMatch(row -> row.get(2) - row.get(1) < 125));
            } finally {
                handler.dispose();
            }
        }
    }

    private void assertChannel(String profile, JsonNode expected, Thing thing) throws IOException {
        String id = expected.path("id").asText();
        Channel actual = Objects.requireNonNull(thing.getChannel(id), id);
        var difference = DIFFERENCES.path("item_type_changes").path(profile + "/" + id);
        String itemType = difference.isMissingNode() ? expected.path("itemType").asText()
                : difference.path("after").asText();
        assertEquals(itemType, actual.getAcceptedItemType(), profile + "/" + id);
        assertEquals(expected.path("type").asText(), Objects.requireNonNull(actual.getChannelTypeUID()).getAsString());
        assertEquals(expected.path("label").asText(), actual.getLabel());
        assertEquals(expected.path("kind").asText(), actual.getKind().name());
        assertEquals(thing.getUID(), actual.getUID().getThingUID());
        assertEquals("true", actual.getProperties().get(SolarmanBindingConstants.DYNAMIC_CHANNEL));
        var configuration = MAPPER.readTree(MAPPER.writeValueAsBytes(actual.getConfiguration().getProperties()));
        var expectedConfiguration = expected.path("configuration");
        assertEquals(
                expectedConfiguration.properties().stream().map(Map.Entry::getKey)
                        .collect(java.util.stream.Collectors.toSet()),
                configuration.properties().stream().map(Map.Entry::getKey)
                        .collect(java.util.stream.Collectors.toSet()));
        for (var entry : expectedConfiguration.properties()) {
            JsonNode actualValue = configuration.path(entry.getKey());
            if (entry.getValue().isNumber()) {
                assertTrue(actualValue.isNumber());
                assertEquals(entry.getValue().decimalValue().stripTrailingZeros(),
                        actualValue.decimalValue().stripTrailingZeros(), profile + "/" + id + "/" + entry.getKey());
            } else {
                assertEquals(entry.getValue(), actualValue, profile + "/" + id + "/" + entry.getKey());
            }
        }
    }

    private static JsonNode baseline(String profile) {
        return BASELINE.path("profiles").path(profile);
    }

    private static List<List<Integer>> requests(JsonNode nodes) {
        return nodes.valueStream().map(
                node -> List.of(node.path("function").asInt(), node.path("start").asInt(), node.path("end").asInt()))
                .toList();
    }

    private static Set<String> coverage(List<List<Integer>> requests) {
        Set<String> coverage = new HashSet<>();
        for (var request : requests) {
            IntStream.rangeClosed(request.get(1), request.get(2))
                    .forEach(register -> coverage.add(request.getFirst() + ":" + register));
        }
        return coverage;
    }

    private static Thing thing(String profile) {
        var type = SolarmanBindingConstants.THING_TYPE_SOLARMAN_LOGGER;
        return ThingBuilder.create(type, new ThingUID(type, "example"))
                .withConfiguration(new Configuration(
                        Map.of("hostname", "127.0.0.1", "serialNumber", "1234567890", "inverterType", profile)))
                .build();
    }

    private static Channel customChannel(Thing thing) {
        return ChannelBuilder.create(new ChannelUID(thing.getUID(), "custom-power"), "Number:Power")
                .withType(new ChannelTypeUID("solarman", "custom-power")).withLabel("Custom power")
                .withConfiguration(new Configuration(Map.of("registers", "[0xFFFA]", "uom", "W"))).build();
    }

    private static class TestHandler extends SolarmanLoggerHandler {
        final ScheduledExecutorService testScheduler = mock(ScheduledExecutorService.class);
        Runnable poll = () -> fail("The handler did not prepare its poll");

        TestHandler(Thing thing) {
            super(thing);
            ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
            doAnswer(invocation -> {
                setThing(invocation.getArgument(0));
                return null;
            }).when(callback).thingUpdated(any());
            doAnswer(invocation -> mock(ScheduledFuture.class)).when(testScheduler)
                    .scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
            setCallback(callback);
        }

        @Override
        protected SolarmanWorkQueue createWorkQueue(Runnable poll, int interval, Consumer<Exception> errors) {
            this.poll = poll;
            return new SolarmanWorkQueue(testScheduler, poll, interval, errors);
        }
    }
}
