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
package org.openhab.binding.knx.internal.wizard;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.knx.internal.wizard.WizardFixtures.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.channel.GroupAddressConfiguration;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Channel;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.LocationGroup;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Read;
import org.openhab.binding.knx.internal.wizard.ProposalPlan.ChannelBody;
import org.openhab.binding.knx.internal.wizard.ProposalPlan.Counts;
import org.openhab.binding.knx.internal.wizard.ProposalPlan.ItemBody;
import org.openhab.binding.knx.internal.wizard.ProposalPlan.Kind;
import org.openhab.binding.knx.internal.wizard.ProposalPlan.LinkBody;
import org.openhab.binding.knx.internal.wizard.ProposalPlan.Step;
import org.openhab.binding.knx.internal.wizard.ProposalPlan.ThingBody;
import org.opentest4j.AssertionFailedError;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Tests for the wizard's create steps: their order, the bodies as core's REST API takes them, the read count, and
 * the text against the steps. All data is made up.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class ProposalPlanTest {
    private static final Gson GSON = new GsonBuilder().create();

    private static final Set<String> THING_FIELDS = Set.of("UID", "thingTypeUID", "bridgeUID", "label", "configuration",
            "channels");
    private static final Set<String> CHANNEL_FIELDS = Set.of("uid", "id", "channelTypeUID", "kind", "itemType", "label",
            "configuration", "properties", "defaultTags");
    private static final Set<String> ITEM_FIELDS = Set.of("type", "name", "label", "category", "tags", "groupNames");
    private static final Set<String> LINK_FIELDS = Set.of("itemName", "channelUID", "configuration");

    private static final Pattern THING_LINE = Pattern
            .compile("Thing (\\S+) \"((?:[^\"\\\\]|\\\\.)*)\" \\((\\S+)\\) \\{");
    private static final Pattern CHANNEL_LINE = Pattern
            .compile(" {4}Type (\\S+) : (\\S+) \"((?:[^\"\\\\]|\\\\.)*)\" \\[ (.*) \\]");
    private static final Pattern KEY_VALUE = Pattern.compile("([A-Za-z]+)=\"((?:[^\"\\\\]|\\\\.)*)\"(, )?");

    static JsonObject json(Object body) {
        return GSON.toJsonTree(body).getAsJsonObject();
    }

    static void assertFields(Set<String> expected, JsonObject body, String what) {
        assertEquals(new TreeSet<>(expected), new TreeSet<>(body.keySet()), what);
    }

    static void assertEmptyObject(JsonObject body, String field) {
        JsonElement value = body.get(field);
        assertNotNull(value, field + " missing");
        assertTrue(value.isJsonObject() && value.getAsJsonObject().isEmpty(), field + " is " + value);
    }

    /** The shape core's REST API takes; a body that misses a field fails here. */
    static void assertShape(Step step) {
        switch (step.kind()) {
            case THING -> assertThingShape(json(Objects.requireNonNull(step.thing())));
            case GROUP, ITEM -> {
                JsonObject item = json(Objects.requireNonNull(step.item()));
                Set<String> expected = new TreeSet<>(ITEM_FIELDS);
                if (!item.has("category")) {
                    // core takes a missing category as none
                    expected.remove("category");
                }
                assertFields(expected, item, "item");
                assertTrue(item.get("tags").isJsonArray());
                assertTrue(item.get("groupNames").isJsonArray());
            }
            case LINK -> {
                JsonObject link = json(Objects.requireNonNull(step.link()));
                assertFields(LINK_FIELDS, link, "link");
                assertEmptyObject(link, "configuration");
            }
        }
    }

    static void assertThingShape(JsonObject thing) {
        assertFields(THING_FIELDS, thing, "thing");
        assertEmptyObject(thing, "configuration");
        for (JsonElement element : thing.getAsJsonArray("channels")) {
            assertChannelShape(element.getAsJsonObject());
        }
    }

    static void assertChannelShape(JsonObject channel) {
        assertFields(CHANNEL_FIELDS, channel, "channel");
        assertEmptyObject(channel, "properties");
        assertTrue(channel.get("defaultTags").isJsonArray() && channel.getAsJsonArray("defaultTags").isEmpty(),
                "defaultTags");
        assertTrue(channel.get("configuration").isJsonObject());
    }

    /** No device address in any Thing or channel configuration. */
    static void assertNoAddress(ProposalPlan plan) {
        for (Step step : plan.steps()) {
            ThingBody thing = step.thing();
            if (thing == null) {
                continue;
            }
            assertFalse(thing.configuration().containsKey("address"), "address in " + thing.UID());
            for (ChannelBody channel : thing.channels()) {
                assertFalse(channel.configuration().containsKey("address"), "address in " + channel.uid());
            }
        }
    }

    static List<Step> steps(ProposalPlan plan, Kind kind) {
        return plan.steps().stream().filter(step -> step.kind() == kind).toList();
    }

    static String unescape(String text) {
        return text.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    @Test
    void stepsComeGroupsThingsItemsLinks() {
        ProposalPlan plan = ProposalPlan.of(sample(true));
        assertEquals(List.of(Kind.GROUP, Kind.THING, Kind.THING, Kind.ITEM, Kind.ITEM, Kind.ITEM, Kind.ITEM, Kind.LINK,
                Kind.LINK, Kind.LINK, Kind.LINK), plan.steps().stream().map(Step::kind).toList());
        assertEquals(List.of("knx:device:example:Kitchen", "knx:device:example:Hall"),
                steps(plan, Kind.THING).stream().map(Step::uid).toList());
        assertEquals(List.of("Kitchen_Ceiling_light", "Kitchen_Spots", "Kitchen_Temperature", "Blind"),
                steps(plan, Kind.ITEM).stream().map(Step::itemName).toList());
        assertEquals(new Counts(2, 4, 4, 4, 1), plan.counts());
    }

    @Test
    void everyBodyHasTheFieldsCoreNeeds() {
        for (boolean readAtStart : List.of(true, false)) {
            ProposalPlan plan = ProposalPlan.of(sample(readAtStart));
            plan.steps().forEach(ProposalPlanTest::assertShape);
        }
        JsonObject temperature = json(
                Objects.requireNonNull(steps(ProposalPlan.of(sample(true)), Kind.ITEM).get(2).item()));
        assertFields(ITEM_FIELDS, temperature, "item with a category");
    }

    @Test
    void aChannelWithoutPropertiesOrDefaultTagsFailsTheShape() {
        JsonObject thing = json(
                Objects.requireNonNull(steps(ProposalPlan.of(sample(true)), Kind.THING).get(0).thing()));
        assertThingShape(thing);
        for (String field : List.of("properties", "defaultTags", "configuration")) {
            JsonObject broken = thing.deepCopy();
            broken.getAsJsonArray("channels").get(0).getAsJsonObject().remove(field);
            assertThrows(AssertionFailedError.class, () -> assertThingShape(broken), field);
        }
        JsonObject filled = thing.deepCopy();
        filled.getAsJsonArray("channels").get(0).getAsJsonObject().getAsJsonObject("properties").addProperty("a", "b");
        assertThrows(AssertionFailedError.class, () -> assertThingShape(filled));
    }

    @Test
    void theBodiesSayWhatTheModelSays() {
        ProposalPlan plan = ProposalPlan.of(sample(true));
        Step group = plan.steps().get(0);
        assertEquals("gKitchen", group.itemName());
        assertEquals(new ItemBody("Group", "gKitchen", "Kitchen", null, List.of("Room"), List.of()), group.item());

        ThingBody kitchen = Objects.requireNonNull(plan.steps().get(1).thing());
        assertEquals("knx:device:example:Kitchen", kitchen.UID());
        assertEquals("knx:device", kitchen.thingTypeUID());
        assertEquals("knx:ip:example", kitchen.bridgeUID());
        assertEquals("Kitchen", kitchen.label());
        assertEquals(Map.of(), kitchen.configuration());
        ChannelBody spots = kitchen.channels().get(1);
        assertEquals("knx:device:example:Kitchen:Spots", spots.uid());
        assertEquals("Spots", spots.id());
        assertEquals("knx:dimmer", spots.channelTypeUID());
        assertEquals("STATE", spots.kind());
        assertEquals("Dimmer", spots.itemType());
        assertEquals("Spots", spots.label());
        assertEquals(List.of("switch", "position", "increaseDecrease"), List.copyOf(spots.configuration().keySet()));
        assertEquals("5.001:31/7/5+<31/7/6", spots.configuration().get("position"));
        assertEquals(Map.of(), spots.properties());
        assertEquals(List.of(), spots.defaultTags());
        assertEquals("Number:Temperature", kitchen.channels().get(2).itemType());

        Step temperature = steps(plan, Kind.ITEM).get(2);
        assertEquals(new ItemBody("Number:Temperature", "Kitchen_Temperature", "Temperature", "temperature",
                List.of("Measurement", "Temperature"), List.of("gKitchen")), temperature.item());
        Step link = steps(plan, Kind.LINK).get(2);
        assertEquals(new LinkBody("Kitchen_Temperature", "knx:device:example:Kitchen:Temperature", Map.of()),
                link.link());
    }

    @Test
    void theChannelKeepsTheBindingsItemTypeWhenTheItemIsPlainNumber() {
        Channel channel = channel("Kitchen", "T", "T", "number", "T", "Number",
                part("ga", "9.001", "31/7/8", null, Read.NONE));
        ProposalPlan plan = ProposalPlan.of(proposal(true, List.of(), thing("Kitchen", "Kitchen", channel)));
        assertEquals("Number:Temperature",
                Objects.requireNonNull(steps(plan, Kind.THING).get(0).thing()).channels().get(0).itemType());
        assertEquals("Number", Objects.requireNonNull(steps(plan, Kind.ITEM).get(0).item()).type());
    }

    @Test
    void noThingOrChannelConfigurationHoldsAnAddress() {
        for (boolean readAtStart : List.of(true, false)) {
            assertNoAddress(ProposalPlan.of(sample(readAtStart)));
        }
        ProposalPlan plan = ProposalPlan.of(sample(true));
        ThingBody thing = Objects.requireNonNull(steps(plan, Kind.THING).get(0).thing());
        ThingBody withAddress = new ThingBody(thing.UID(), thing.thingTypeUID(), thing.bridgeUID(), thing.label(),
                Map.of("address", "7.7.1"), thing.channels());
        assertThrows(AssertionFailedError.class,
                () -> assertNoAddress(new ProposalPlan(
                        List.of(new Step(Kind.THING, withAddress.UID(), null, null, withAddress, null, null)),
                        List.of(), 0, plan.counts())));
        ChannelBody channel = thing.channels().get(0);
        Map<String, String> configuration = new LinkedHashMap<>(channel.configuration());
        configuration.put("address", "7.7.1");
        ChannelBody channelWithAddress = new ChannelBody(channel.uid(), channel.id(), channel.channelTypeUID(),
                channel.kind(), channel.itemType(), channel.label(), configuration, Map.of(), List.of());
        ThingBody withChannel = new ThingBody(thing.UID(), thing.thingTypeUID(), thing.bridgeUID(), thing.label(),
                Map.of(), List.of(channelWithAddress));
        assertThrows(AssertionFailedError.class,
                () -> assertNoAddress(new ProposalPlan(
                        List.of(new Step(Kind.THING, withChannel.UID(), null, null, withChannel, null, null)),
                        List.of(), 0, plan.counts())));
    }

    @Test
    void theLinkBodyNamesEqualItsPath() {
        ProposalPlan plan = ProposalPlan.of(sample(true));
        Set<String> items = new TreeSet<>();
        steps(plan, Kind.ITEM).forEach(step -> items.add(Objects.requireNonNull(step.itemName())));
        Set<String> channels = new TreeSet<>();
        for (Step step : steps(plan, Kind.THING)) {
            Objects.requireNonNull(step.thing()).channels().forEach(channel -> channels.add(channel.uid()));
        }
        for (Step step : steps(plan, Kind.LINK)) {
            LinkBody link = Objects.requireNonNull(step.link());
            assertEquals(step.itemName(), link.itemName());
            assertEquals(step.channelUID(), link.channelUID());
            assertTrue(items.contains(link.itemName()), link.itemName());
            assertTrue(channels.contains(link.channelUID()), link.channelUID());
        }
        for (Step step : plan.steps()) {
            ThingBody thing = step.thing();
            if (thing != null) {
                assertEquals(step.uid(), thing.UID());
            }
            ItemBody item = step.item();
            if (item != null) {
                assertEquals(step.itemName(), item.name());
            }
        }
    }

    @Test
    void aStepHasExactlyOneBodyOfItsKind() {
        ItemBody item = new ItemBody("Switch", "A", "A", null, List.of(), List.of());
        LinkBody link = new LinkBody("A", "knx:device:example:K:A", Map.of());
        assertThrows(IllegalArgumentException.class, () -> new Step(Kind.ITEM, null, "A", null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Step(Kind.ITEM, null, "A", null, null, item, link));
        assertThrows(IllegalArgumentException.class, () -> new Step(Kind.LINK, null, "A", null, null, item, null));
        assertThrows(IllegalArgumentException.class, () -> new Step(Kind.THING, null, "A", null, null, item, null));
        new Step(Kind.ITEM, null, "A", null, null, item, null);
    }

    @Test
    void readTelegramsIsTheSumOfTheReadAddresses() {
        ProposalPlan plan = ProposalPlan.of(sample(true));
        assertEquals(List.of("31/7/2", "31/7/4", "31/7/6", "31/7/8", "31/7/13"), plan.readAddresses());
        int sum = 0;
        for (Step step : steps(plan, Kind.THING)) {
            for (ChannelBody channel : Objects.requireNonNull(step.thing()).channels()) {
                for (String text : channel.configuration().values()) {
                    sum += Objects.requireNonNull(GroupAddressConfiguration.parse(text)).getReadGAs().size();
                }
            }
        }
        assertEquals(sum, plan.readTelegrams());
        assertEquals(5, plan.readTelegrams());

        ProposalPlan off = ProposalPlan.of(sample(false));
        assertEquals(0, off.readTelegrams());
        assertEquals(List.of(), off.readAddresses());
    }

    @Test
    void anAddressReadInTwoChannelsCountsTwoTelegramsAndIsListedOnce() {
        Channel local = channel("Kitchen", "Local", "Local", "switch", "Local", "Switch",
                part("ga", "1.001", "31/7/1", "31/7/3", Read.STATUS));
        Channel central = channel("Kitchen", "Central", "Central", "switch", "Central", "Switch",
                part("ga", "1.001", "31/7/2", "31/7/3", Read.STATUS));
        ProposalPlan plan = ProposalPlan.of(proposal(true, List.of(), thing("Kitchen", "Kitchen", local, central)));
        assertEquals(2, plan.readTelegrams());
        assertEquals(List.of("31/7/3"), plan.readAddresses());
    }

    @Test
    void theThingsTextSaysWhatTheThingStepsSay() {
        for (boolean readAtStart : List.of(true, false)) {
            CheckedProposal proposal = sample(readAtStart);
            Map<String, ThingBody> bodies = new LinkedHashMap<>();
            for (Step step : steps(ProposalPlan.of(proposal), Kind.THING)) {
                ThingBody thing = Objects.requireNonNull(step.thing());
                bodies.put(thing.UID(), thing);
            }
            List<String> seen = new ArrayList<>();
            @Nullable
            ThingBody current = null;
            int channel = 0;
            for (String line : ProposalText.of(proposal).thingsText().split("\n")) {
                Matcher thingLine = THING_LINE.matcher(line);
                Matcher channelLine = CHANNEL_LINE.matcher(line);
                if (thingLine.matches()) {
                    current = Objects.requireNonNull(bodies.get(thingLine.group(1)), line);
                    seen.add(current.UID());
                    assertEquals(current.label(), unescape(thingLine.group(2)));
                    assertEquals(current.bridgeUID(), thingLine.group(3));
                    channel = 0;
                } else if (channelLine.matches()) {
                    ThingBody thing = Objects.requireNonNull(current, line);
                    ChannelBody body = thing.channels().get(channel++);
                    assertEquals(body.channelTypeUID(), "knx:" + channelLine.group(1));
                    assertEquals(body.id(), channelLine.group(2));
                    assertEquals(body.label(), unescape(channelLine.group(3)));
                    Map<String, String> configuration = new LinkedHashMap<>();
                    Matcher keyValue = KEY_VALUE.matcher(channelLine.group(4));
                    int end = 0;
                    while (keyValue.find() && keyValue.start() == end) {
                        configuration.put(keyValue.group(1), unescape(keyValue.group(2)));
                        end = keyValue.end();
                    }
                    assertEquals(channelLine.group(4).length(), end, line);
                    assertEquals(body.configuration(), configuration, line);
                } else if ("}".equals(line)) {
                    assertEquals(Objects.requireNonNull(current).channels().size(), channel, line);
                    current = null;
                } else {
                    assertEquals("", line);
                }
            }
            assertEquals(List.copyOf(bodies.keySet()), seen);
        }
    }

    @Test
    void locationGroupsAreCreatedParentsFirstAndAnExistingOneIsNotCreated() {
        Channel light = channel("Bath", "Light", "Light", "switch", "Bath_Light", "Switch",
                part("ga", "1.001", "31/7/1", null, Read.NONE));
        light = withItem(light, item("Bath_Light", "Switch", "Light", light.uid(), null, List.of(), List.of("gBath")));
        ProposalPlan plan = ProposalPlan.of(proposal(true,
                List.of(new LocationGroup("gHouse", "House", "Building", "house", null, false),
                        new LocationGroup("gUp", "Up", "Floor", null, "gHouse", true),
                        new LocationGroup("gBath", "Bath", "Room", "group", "gUp", false),
                        new LocationGroup("gBoard", "Board", null, null, "gHouse", false)),
                thing("Bath", "Bath", light)));

        assertEquals(List.of("GROUP gHouse", "GROUP gBath", "GROUP gBoard", "THING null", "ITEM Bath_Light",
                "LINK Bath_Light"), plan.steps().stream().map(s -> s.kind() + " " + s.itemName()).toList());
        assertEquals(new ItemBody("Group", "gHouse", "House", "house", List.of("Building"), List.of()),
                plan.steps().get(0).item());
        assertEquals(new ItemBody("Group", "gBath", "Bath", "group", List.of("Room"), List.of("gUp")),
                plan.steps().get(1).item());
        assertEquals(new ItemBody("Group", "gBoard", "Board", null, List.of(), List.of("gHouse")),
                plan.steps().get(2).item());
        assertEquals(3, plan.counts().groups());
        assertEquals(List.of("gBath"), Objects.requireNonNull(plan.steps().get(4).item()).groupNames());
    }
}
