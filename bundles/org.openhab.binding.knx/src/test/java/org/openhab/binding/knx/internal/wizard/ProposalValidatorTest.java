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
import static org.openhab.binding.knx.internal.project.ChannelFixtures.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.knx.internal.project.KnxGroupAddress;
import org.openhab.binding.knx.internal.project.KnxIndividualAddress;
import org.openhab.binding.knx.internal.project.KnxProject;
import org.openhab.binding.knx.internal.rest.dto.CandidateDTO;
import org.openhab.binding.knx.internal.rest.dto.PreviewRequestDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedChannelDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedItemDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedPartDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedRoomDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedThingDTO;
import org.openhab.binding.knx.internal.wizard.CheckedProposal.Read;
import org.openhab.binding.knx.internal.wizard.ProposalValidator.BadRequest;
import org.openhab.binding.knx.internal.wizard.ProposalValidator.Problem;
import org.openhab.binding.knx.internal.wizard.ProposalValidator.Result;
import org.openhab.binding.knx.internal.wizard.ProposalValidator.Severity;

/**
 * The checks of call 22 on a made-up project: each problem from a request that differs from a good one in one
 * field, and the fields the validator must not read.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class ProposalValidatorTest {
    private static final String BRIDGE = "knx:ip:home";
    private static final String MARKER = "MadeUpMarker";

    private final Set<String> things = new HashSet<>();
    private final Set<String> items = new HashSet<>();
    private final Set<String> groups = new HashSet<>();
    private final Set<Integer> configured = new HashSet<>();
    private boolean keyring;

    private static int ga(int n) {
        return raw(31, 7, n);
    }

    private static KnxGroupAddress a(int n, @Nullable String dpt, String name) {
        return address(n, ga(n), dpt, name);
    }

    /**
     * An actuator with a switch and its status in channel CH-1 (the switch is also linked to the central address
     * 31/7/12), a dimming
     * value and its status, a switch that cannot be read, a secured switch, an address without type and a blind; a
     * sensor with a temperature, a text and a logic value it answers on 31/7/13 and takes from 31/7/12 as well; a
     * filter device whose object sends on 31/7/14.
     */
    static KnxProject project() {
        KnxGroupAddress secured = new KnxGroupAddress("P-1_GA-6", ga(6), "31/7/6", "Secured switch", null,
                List.of(type("1.001")), true, List.of());
        List<KnxGroupAddress> addresses = List.of(a(1, "1.001", "Kitchen light"), a(2, "1.001", "Kitchen light status"),
                a(3, "5.001", "Kitchen dim"), a(4, "9.001", "Kitchen temperature"), a(5, "1.001", "Spare switch"),
                secured, a(7, null, "No type"), a(8, "5.001", "Kitchen dim status"), a(9, "1.008", "Blind"),
                a(10, "16.001", "Text"), a(12, "1.001", "Central light"), a(13, "1.001", "Logic value"),
                a(14, "1.001", "Filter value"));
        Dev actuator = device("Actuator", new KnxIndividualAddress(7, 7, 1),
                object("O-1", 1, "Switch", null, "1.001", 1, 12).flags("C-W--").channel("CH-1"),
                object("O-2", 2, "Status", null, "1.001", 2).flags("CR-T-").channel("CH-1"),
                object("O-3", 3, "Dim", null, "5.001", 3).flags("C-W--"),
                object("O-8", 8, "Dim status", null, "5.001", 8).flags("CR-T-"),
                object("O-5", 5, "Spare", null, "1.001", 5).flags("C-W--"),
                object("O-6", 6, "Secured", null, "1.001", 6).flags("C-W--"),
                object("O-7", 7, "No type", null, null, 7).flags("C-W--"),
                object("O-9", 9, "Blind", null, "1.008", 9).flags("C-W--"));
        Dev sensor = device("Sensor", new KnxIndividualAddress(7, 7, 2),
                object("O-4", 1, "Temperature", null, "9.001", 4).flags("CR-T-"),
                object("O-10", 2, "Text", null, "16.001", 10).flags("CR-T-"),
                object("O-13", 3, "Logic", null, "1.001", 13, 12).flags("CRW--"));
        Dev filter = device("", new KnxIndividualAddress(7, 7, 3),
                object("O-14", 1, "Value", null, "1.001", 14).flags("CRWT-")).of(filterProduct());
        return projectOf(addresses, List.of(actuator, sensor, filter), List.of());
    }

    private Result validate(PreviewRequestDTO request) {
        return new ProposalValidator(project(), BRIDGE, things::contains,
                name -> items.contains(name) || groups.contains(name), groups::contains, configured, keyring)
                .validate(request);
    }

    // the request as the page sends it back: the fields the validator must not read are filled as call 21 does

    static ProposedPartDTO part(String key, String address, @Nullable String status, boolean read, boolean statusRead) {
        ProposedPartDTO part = new ProposedPartDTO();
        part.key = key;
        part.groupAddress = address;
        part.statusAddress = status;
        part.read = read;
        part.statusRead = statusRead;
        part.dataType = "1.001";
        part.groupAddressName = "Name";
        part.statusName = status == null ? null : "Status name";
        part.pairing = status == null ? "NONE" : "OBJECTS";
        part.candidates = List.of();
        part.text = "1.001:" + address;
        return part;
    }

    static ProposedItemDTO item(String name, String type, String label, @Nullable String category, List<String> tags,
            List<String> groups) {
        ProposedItemDTO item = new ProposedItemDTO();
        item.name = name;
        item.type = type;
        item.label = label;
        item.category = category;
        item.tags = new ArrayList<>(tags);
        item.groups = new ArrayList<>(groups);
        item.allowedTypes = List.of(type);
        return item;
    }

    static ProposedChannelDTO channel(String id, String label, String type, ProposedItemDTO item,
            ProposedPartDTO... parts) {
        ProposedChannelDTO channel = new ProposedChannelDTO();
        channel.id = id;
        channel.label = label;
        channel.channelType = type;
        channel.selected = true;
        channel.parts = new ArrayList<>(Arrays.asList(parts));
        channel.item = item;
        channel.allowedChannelTypes = List.of(type);
        channel.combined = parts.length > 1;
        channel.alreadyConfigured = List.of();
        channel.notes = List.of();
        return channel;
    }

    static ProposedThingDTO thing(String id, String label, ProposedChannelDTO... channels) {
        ProposedThingDTO thing = new ProposedThingDTO();
        thing.id = id;
        thing.uid = "knx:device:home:" + id;
        thing.label = label;
        thing.source = "FUNCTION";
        thing.sourceId = "P-1_F-1";
        thing.roomId = "P-1_S-1";
        thing.room = "Kitchen";
        thing.channels = new ArrayList<>(Arrays.asList(channels));
        return thing;
    }

    static ProposedRoomDTO room(String groupName, String label) {
        ProposedRoomDTO room = new ProposedRoomDTO();
        room.id = "P-1_S-1";
        room.name = "Kitchen";
        room.groupName = groupName;
        room.label = label;
        return room;
    }

    /** A switch with its status, a temperature in the room group, and a dimmer whose position has a status. */
    static PreviewRequestDTO good() {
        PreviewRequestDTO request = new PreviewRequestDTO();
        request.things = new ArrayList<>(List.of(thing("Kitchen", "Kitchen",
                channel("Light", "Light", "switch",
                        item("Kitchen_Light", "Switch", "Light", null, List.of(), List.of("gKitchen")),
                        part("ga", "31/7/1", "31/7/2", false, true)),
                channel("Temperature", "Temperature", "number",
                        item("Kitchen_Temperature", "Number:Temperature", "Temperature", "temperature",
                                List.of("Measurement", "Temperature"), List.of("gKitchen")),
                        part("ga", "31/7/4", null, true, false)),
                channel("Dimmer_", "Dimmer", "dimmer",
                        item("Kitchen_Dimmer", "Dimmer", "Dimmer", null, List.of(), List.of()),
                        part("switch", "31/7/5", null, false, false),
                        part("position", "31/7/3", "31/7/8", false, true)))));
        request.roomGroups = new ArrayList<>(List.of(room("gKitchen", "Kitchen")));
        request.readAtStart = true;
        return request;
    }

    private static PreviewRequestDTO good(Consumer<PreviewRequestDTO> change) {
        PreviewRequestDTO request = good();
        change.accept(request);
        return request;
    }

    private static ProposedThingDTO thing0(PreviewRequestDTO request) {
        return Objects.requireNonNull(Objects.requireNonNull(request.things).get(0));
    }

    private static ProposedChannelDTO channel(PreviewRequestDTO request, int c) {
        return Objects.requireNonNull(Objects.requireNonNull(thing0(request).channels).get(c));
    }

    private static ProposedPartDTO part(PreviewRequestDTO request, int c, int p) {
        return Objects.requireNonNull(Objects.requireNonNull(channel(request, c).parts).get(p));
    }

    private static ProposedItemDTO item(PreviewRequestDTO request, int c) {
        return Objects.requireNonNull(channel(request, c).item);
    }

    private static ProposedRoomDTO room0(PreviewRequestDTO request) {
        return Objects.requireNonNull(Objects.requireNonNull(request.roomGroups).get(0));
    }

    /** The problems as {@code SEVERITY code where}. */
    private static List<String> problems(Result result) {
        return result.problems().stream().map(p -> p.severity() + " " + p.code() + " " + p.where()).toList();
    }

    private static List<String> errors(Result result) {
        return result.problems().stream().filter(p -> p.severity() == Severity.ERROR)
                .map(p -> p.code() + " " + p.where()).toList();
    }

    private static CheckedProposal model(Result result) {
        return Objects.requireNonNull(result.proposal(), () -> problems(result).toString());
    }

    private static String text(Result result, int channel, int part) {
        return model(result).things().get(0).channels().get(channel).parts().get(part).text();
    }

    // the good request

    @Test
    void theGoodRequestGivesItsModelTextAndReads() {
        Result result = validate(good());

        assertEquals(List.of("WARNING status-only things/0/channels/1"), problems(result));
        assertEquals("""
                Thing knx:device:home:Kitchen "Kitchen" (knx:ip:home) {
                    Type switch : Light "Light" [ ga="1.001:31/7/1+<31/7/2" ]
                    Type number : Temperature "Temperature" [ ga="9.001:<31/7/4" ]
                    Type dimmer : Dimmer_ "Dimmer" [ switch="1.001:31/7/5", position="5.001:31/7/3+<31/7/8" ]
                }
                """, ProposalText.of(model(result)).thingsText());
        assertEquals(List.of("31/7/2", "31/7/4", "31/7/8"), result.readAddresses());
        assertEquals(3, result.readTelegrams());
        ProposalPlan plan = ProposalPlan.of(model(result));
        assertEquals(plan.readAddresses(), result.readAddresses());
        assertEquals(plan.readTelegrams(), result.readTelegrams());
        assertEquals(List.of("gKitchen"), model(result).locationGroups().stream().map(g -> g.name()).toList());
    }

    @Test
    void readAtStartLeftOutCountsAsTrue() {
        assertEquals(validate(good()), validate(good(r -> r.readAtStart = null)));
    }

    // the fields the validator does not read

    @Test
    void aRequestThatDiffersOnlyInAFieldNotReadGivesTheSameAnswer() {
        Map<String, Consumer<PreviewRequestDTO>> changes = new LinkedHashMap<>();
        changes.put("thing uid of another bridge", r -> thing0(r).uid = "knx:device:other:Kitchen");
        changes.put("thing uid of another binding", r -> thing0(r).uid = "mqtt:topic:x:Kitchen");
        changes.put("thing source", r -> thing0(r).source = "DEVICE");
        changes.put("thing sourceId", r -> thing0(r).sourceId = "P-1_DI-9");
        changes.put("thing roomId", r -> thing0(r).roomId = "P-1_S-9");
        changes.put("thing room", r -> thing0(r).room = MARKER);
        changes.put("channel allowedChannelTypes", r -> channel(r, 0).allowedChannelTypes = List.of("color"));
        changes.put("channel statusOnly", r -> channel(r, 0).statusOnly = true);
        changes.put("channel combined", r -> channel(r, 0).combined = true);
        changes.put("channel split", r -> channel(r, 2).split = List.of(channel(r, 0)));
        changes.put("channel alreadyConfigured", r -> channel(r, 0).alreadyConfigured = List.of("knx:device:x:y:z"));
        changes.put("channel notes", r -> channel(r, 0).notes = List.of(MARKER));
        changes.put("part text", r -> part(r, 0, 0).text = "1.001:<31/7/9+<31/7/10");
        changes.put("part dataType", r -> part(r, 0, 0).dataType = "9.001");
        changes.put("part secured", r -> part(r, 0, 0).secured = true);
        changes.put("part groupAddressName", r -> part(r, 0, 0).groupAddressName = MARKER);
        changes.put("part statusName", r -> part(r, 0, 0).statusName = MARKER);
        changes.put("part pairing", r -> part(r, 0, 0).pairing = "ROLE");
        changes.put("part candidates", r -> {
            CandidateDTO candidate = new CandidateDTO();
            candidate.address = "31/7/9";
            candidate.name = MARKER;
            part(r, 0, 0).candidates = List.of(candidate);
        });
        changes.put("item allowedTypes", r -> item(r, 0).allowedTypes = List.of("Group"));
        changes.put("item allowedItemTypes", r -> item(r, 0).allowedItemTypes = Map.of("switch", List.of("Group")));
        changes.put("room id", r -> room0(r).id = "P-1_S-9");
        changes.put("room name", r -> room0(r).name = MARKER);
        Result expected = validate(good());
        for (Map.Entry<String, Consumer<PreviewRequestDTO>> change : changes.entrySet()) {
            assertEquals(expected, validate(good(change.getValue())), change.getKey());
        }
    }

    @Test
    void aRequestThatClaimsAnotherDataTypeIsCorrected() {
        Result result = validate(good(r -> part(r, 1, 0).dataType = "14.068"));

        assertEquals("9.001", model(result).things().get(0).channels().get(1).parts().get(0).dataType().toString());
        assertEquals("9.001:<31/7/4", text(result, 1, 0));
    }

    @Test
    void theUidsAreBuiltFromThePathsBridgeAndTheIds() {
        CheckedProposal model = model(validate(good(r -> thing0(r).uid = "knx:device:other:Evil")));

        assertEquals("knx:device:home:Kitchen", model.things().get(0).uid());
        assertEquals("knx:device:home:Kitchen:Light", model.things().get(0).channels().get(0).uid());
        assertEquals("knx:device:home:Kitchen:Light", model.things().get(0).channels().get(0).item().channelUID());
    }

    // ids and names

    @Test
    void aThingIdOutsideThePatternIsBadId() {
        assertEquals(List.of("bad-id things/0/id"), errors(validate(good(r -> thing0(r).id = "Kit chen"))));
    }

    @Test
    void aChannelIdOutsideThePatternIsBadId() {
        assertEquals(List.of("bad-id things/0/channels/0/id"),
                errors(validate(good(r -> channel(r, 0).id = "Light-1"))));
    }

    @Test
    void anItemNameOutsideThePatternIsBadItemName() {
        assertEquals(List.of("bad-item-name things/0/channels/0/item/name"),
                errors(validate(good(r -> item(r, 0).name = "1Light"))));
    }

    @Test
    void aGroupNameOutsideThePatternIsBadItemName() {
        Result result = validate(good(r -> room0(r).groupName = "g Kitchen"));

        assertEquals(List.of("bad-item-name roomGroups/0/groupName", "unknown-group things/0/channels/0/item/groups/0",
                "unknown-group things/0/channels/1/item/groups/0"), errors(result));
    }

    @Test
    void aWordOfTheThingGrammarAsThingOrChannelIdIsBadId() {
        for (String keyword : ProposalNamesTest.THING_KEYWORDS) {
            assertEquals(List.of("bad-id things/0/id"), errors(validate(good(r -> thing0(r).id = keyword))), keyword);
            assertEquals(List.of("bad-id things/0/channels/0/id"),
                    errors(validate(good(r -> channel(r, 0).id = keyword))), keyword);
        }
        assertEquals(List.of(), errors(validate(good(r -> channel(r, 0).id = "switch"))));
    }

    @Test
    void aWordOfTheItemsGrammarAsItemOrGroupNameIsBadItemName() {
        for (String keyword : ProposalNamesTest.ITEM_KEYWORDS) {
            assertEquals(List.of("bad-item-name things/0/channels/0/item/name"),
                    errors(validate(good(r -> item(r, 0).name = keyword))), keyword);
            Result group = validate(good(r -> room0(r).groupName = keyword));
            assertEquals("bad-item-name roomGroups/0/groupName", errors(group).get(0), keyword);
        }
        assertEquals(List.of(), errors(validate(good(r -> item(r, 0).name = "Switch"))));
    }

    @Test
    void twoThingsWithOneIdAreDuplicateId() {
        Result result = validate(good(r -> Objects.requireNonNull(r.things)
                .add(thing("Kitchen", "Hall",
                        channel("Blind", "Blind", "switch",
                                item("Hall_Blind", "Switch", "Blind", null, List.of(), List.of()),
                                part("ga", "31/7/12", null, false, false))))));

        assertEquals(List.of("duplicate-id things/1/id"), errors(result));
    }

    @Test
    void twoChannelsWithOneIdInAThingAreDuplicateId() {
        assertEquals(List.of("duplicate-id things/0/channels/1/id"),
                errors(validate(good(r -> channel(r, 1).id = "Light"))));
    }

    @Test
    void twoItemsWithOneNameAreDuplicateItem() {
        assertEquals(List.of("duplicate-item things/0/channels/1/item/name"),
                errors(validate(good(r -> item(r, 1).name = "Kitchen_Light"))));
    }

    @Test
    void anItemNamedLikeARoomGroupIsDuplicateItem() {
        assertEquals(List.of("duplicate-item things/0/channels/0/item/name"),
                errors(validate(good(r -> item(r, 0).name = "gKitchen"))));
    }

    @Test
    void twoRoomGroupsWithOneNameAreDuplicateItem() {
        assertEquals(List.of("duplicate-item roomGroups/1/groupName"),
                errors(validate(good(r -> Objects.requireNonNull(r.roomGroups).add(room("gKitchen", "Other"))))));
    }

    @Test
    void anExistingThingIsThingExists() {
        things.add("knx:device:home:Kitchen");

        Result result = validate(good());

        assertEquals(List.of("thing-exists things/0/id"), errors(result));
        assertNull(result.proposal());
    }

    @Test
    void anExistingItemOrGroupIsItemExists() {
        items.add("Kitchen_Light");
        items.add("gKitchen");

        assertEquals(List.of("item-exists roomGroups/0/groupName", "item-exists things/0/channels/0/item/name"),
                errors(validate(good())));
    }

    @Test
    void aGroupOfTheHouseThatIsNoRoomGroupOfTheRequestIsUnknownGroup() {
        items.add("gHouse");

        assertEquals(List.of("unknown-group things/0/channels/2/item/groups/0"),
                errors(validate(good(r -> Objects.requireNonNull(item(r, 2).groups).add("gHouse")))));
    }

    // labels

    @Test
    void anEmptyLabelIsBadLabel() {
        assertEquals(List.of("bad-label things/0/label"), errors(validate(good(r -> thing0(r).label = ""))));
    }

    @Test
    void aLabelEmptyAfterCleaningIsBadLabel() {
        assertEquals(List.of("bad-label things/0/channels/0/label"),
                errors(validate(good(r -> channel(r, 0).label = "\n\u0007\t"))));
    }

    @Test
    void aLabelOfMoreThan200CharactersIsBadLabel() {
        assertEquals(List.of("bad-label things/0/channels/0/item/label"),
                errors(validate(good(r -> item(r, 0).label = "x".repeat(201)))));
        assertEquals(List.of("bad-label roomGroups/0/label"),
                errors(validate(good(r -> room0(r).label = "y".repeat(201)))));
        assertEquals(List.of(), errors(validate(good(r -> item(r, 0).label = "x".repeat(200)))));
    }

    @Test
    void labelsAreCleanedForTheModel() {
        CheckedProposal model = model(validate(good(r -> {
            thing0(r).label = " Kitchen\n\"north\" ";
            item(r, 0).label = "Light [main]";
        })));

        assertEquals("Kitchen\"north\"", model.things().get(0).label());
        assertEquals("Light (main)", model.things().get(0).channels().get(0).item().label());
    }

    // Items

    @Test
    void aTagWithAQuoteIsBadTag() {
        assertEquals(List.of("bad-tag things/0/channels/1/item/tags"),
                errors(validate(good(r -> Objects.requireNonNull(item(r, 1).tags).add("Temp\"erature")))));
    }

    @Test
    void elevenTagsAreBadTag() {
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            tags.add("Tag" + i);
        }
        assertEquals(List.of("bad-tag things/0/channels/1/item/tags"),
                errors(validate(good(r -> item(r, 1).tags = tags))));
        assertEquals(List.of(), errors(validate(good(r -> item(r, 1).tags = tags.subList(0, 10)))));
    }

    @Test
    void aCategoryWithAnAngleBracketIsBadCategory() {
        assertEquals(List.of("bad-category things/0/channels/1/item/category"),
                errors(validate(good(r -> item(r, 1).category = "temp>"))));
    }

    @ParameterizedTest
    @ValueSource(strings = { "-x", "true", "false", "Light", "a b", "a.b",
            "x12345678901234567890123456789012345678901" })
    void aCategoryTheItemsFileCannotHoldIsBadCategory(String category) {
        assertEquals(List.of("bad-category things/0/channels/1/item/category"),
                errors(validate(good(r -> item(r, 1).category = category))));
    }

    @ParameterizedTest
    @ValueSource(strings = { "a-b", "x_1", "_", "7", "truely", "x123456789012345678901234567890123456789" })
    void aCategoryOfSmallLettersDigitsUnderscoreAndInnerDashIsKept(String category) {
        assertEquals(category, model(validate(good(r -> item(r, 1).category = category))).things().get(0).channels()
                .get(1).item().category());
    }

    @Test
    void anEmptyCategoryIsNone() {
        assertNull(model(validate(good(r -> item(r, 1).category = ""))).things().get(0).channels().get(1).item()
                .category());
    }

    @Test
    void theItemTypeGroupIsNotAllowed() {
        assertEquals(List.of("type-not-allowed things/0/channels/0/item/type"),
                errors(validate(good(r -> item(r, 0).type = "Group"))));
    }

    @Test
    void anItemTypeOfAnotherDimensionIsNotAllowed() {
        assertEquals(List.of("type-not-allowed things/0/channels/1/item/type"),
                errors(validate(good(r -> item(r, 1).type = "Number:Length"))));
        assertEquals(List.of(), errors(validate(good(r -> item(r, 1).type = "Number"))));
    }

    // keys, addresses and data types

    @Test
    void aKeyOfAnotherChannelTypeIsBadKey() {
        assertEquals(List.of("bad-key things/0/channels/0/parts/0/key"),
                errors(validate(good(r -> part(r, 0, 0).key = "hsb"))));
    }

    @Test
    void oneKeyTwiceIsBadKey() {
        assertEquals(List.of("bad-key things/0/channels/2/parts/1/key"),
                errors(validate(good(r -> part(r, 2, 1).key = "switch"))));
    }

    @Test
    void aChannelWithoutPartsIsBadKey() {
        assertEquals(List.of("bad-key things/0/channels/0/parts"),
                errors(validate(good(r -> channel(r, 0).parts = new ArrayList<>()))));
    }

    @Test
    void anAddressNotInTheProjectIsUnknownAddress() {
        assertEquals(List.of("unknown-address things/0/channels/0/parts/0/groupAddress"),
                errors(validate(good(r -> part(r, 0, 0).groupAddress = "31/7/30"))));
        assertEquals(List.of("unknown-address things/0/channels/0/parts/0/statusAddress"),
                errors(validate(good(r -> part(r, 0, 0).statusAddress = "31/7/31"))));
    }

    @Test
    void textThatIsNoGroupAddressIsBadAddress() {
        assertEquals(List.of("bad-address things/0/channels/0/parts/0/groupAddress"),
                errors(validate(good(r -> part(r, 0, 0).groupAddress = "<31/7/1"))));
        assertEquals(List.of("bad-address things/0/channels/0/parts/0/statusAddress"),
                errors(validate(good(r -> part(r, 0, 0).statusAddress = "31/7/2+31/7/9"))));
    }

    @Test
    void aStatusAddressThatIsTheMainAddressIsBadAddress() {
        assertEquals(List.of("bad-address things/0/channels/0/parts/0/statusAddress"),
                errors(validate(good(r -> part(r, 0, 0).statusAddress = "31/7/1"))));
    }

    @Test
    void anAddressWithoutDataTypeIsNoDataType() {
        assertEquals(List.of("no-data-type things/0/channels/0/parts/0/groupAddress"), errors(validate(good(r -> {
            part(r, 0, 0).groupAddress = "31/7/7";
            part(r, 0, 0).statusAddress = null;
        }))));
    }

    @Test
    void aDataTypeThatDoesNotWorkOnTheKeyIsNotAllowed() {
        Result result = validate(good(r -> {
            channel(r, 0).channelType = "number";
            item(r, 0).type = "Number";
        }));

        assertEquals(List.of("type-not-allowed things/0/channels/0/parts/0/key"), errors(result));
    }

    @Test
    void aChannelTypeThatIsNotOfferedIsNotAllowed() {
        assertEquals(List.of("type-not-allowed things/0/channels/0/channelType"),
                errors(validate(good(r -> channel(r, 0).channelType = "switch-control"))));
        assertEquals(List.of("type-not-allowed things/0/channels/0/channelType"), errors(validate(good(r -> {
            channel(r, 0).channelType = null;
            channel(r, 0).item = null;
        }))));
    }

    @Test
    void aStatusAddressOfAnotherDataTypeIsRefused() {
        assertEquals(List.of("type-not-allowed things/0/channels/0/parts/0/statusAddress"),
                errors(validate(good(r -> part(r, 0, 0).statusAddress = "31/7/4"))));
    }

    @Test
    void aStatusAddressNeedNotBeACandidate() {
        Result result = validate(good(r -> part(r, 0, 0).statusAddress = "31/7/12"));

        assertEquals(List.of(), errors(result));
        assertEquals("1.001:31/7/1+<31/7/12", text(result, 0, 0));
        assertTrue(problems(result).contains("WARNING read-without-flag things/0/channels/0/parts/0"));
    }

    @Test
    void noStatusAddressLeavesTheStatusOut() {
        assertEquals("1.001:<31/7/1", text(validate(good(r -> {
            part(r, 0, 0).statusAddress = null;
            part(r, 0, 0).read = true;
        })), 0, 0));
        assertEquals("1.001:31/7/1", text(validate(good(r -> part(r, 0, 0).statusAddress = "")), 0, 0));
    }

    @Test
    void nothingSelectedIsNothingSelected() {
        Result result = validate(
                good(r -> Objects.requireNonNull(thing0(r).channels).forEach(c -> c.selected = false)));

        assertEquals(List.of("nothing-selected things"), errors(result));
        assertNull(result.proposal());
    }

    @Test
    void anUnselectedChannelIsNotReadAtAll() {
        Result expected = validate(good(r -> Objects.requireNonNull(thing0(r).channels).remove(2)));

        assertEquals(expected, validate(good(r -> {
            channel(r, 2).selected = false;
            channel(r, 2).id = "bad id";
            channel(r, 2).parts = null;
            channel(r, 2).item = null;
        })));
    }

    // warnings

    @Test
    void anAddressABridgeChannelUsesIsAlreadyConfigured() {
        configured.add(ga(2));

        Result result = validate(good());

        assertTrue(problems(result).contains("WARNING already-configured things/0/channels/0/parts/0/statusAddress"),
                problems(result).toString());
        assertNotNull(result.proposal());
    }

    @Test
    void aSecuredAddressWithoutKeyringIsAWarning() {
        Consumer<PreviewRequestDTO> secured = r -> part(r, 2, 0).groupAddress = "31/7/6";

        assertTrue(problems(validate(good(secured)))
                .contains("WARNING secured-without-keyring things/0/channels/2/parts/0/groupAddress"));
        keyring = true;
        assertFalse(problems(validate(good(secured))).stream().anyMatch(p -> p.contains("secured-without-keyring")));
    }

    @Test
    void aReadOnAnAddressNoObjectAnswersIsKeptWithAWarning() {
        Result result = validate(good(r -> part(r, 2, 0).read = true));

        assertTrue(problems(result).contains("WARNING read-without-flag things/0/channels/2/parts/0"));
        assertEquals("1.001:<31/7/5", text(result, 2, 0));
        assertEquals(4, result.readTelegrams());
    }

    @Test
    void aReadOnAFurtherLinkOfAReadableObjectIsKeptWithAWarning() {
        Consumer<PreviewRequestDTO> central = r -> {
            part(r, 2, 0).groupAddress = "31/7/12";
            part(r, 2, 0).read = true;
        };
        Result result = validate(good(central));

        assertTrue(problems(result).contains("WARNING read-without-flag things/0/channels/2/parts/0"),
                problems(result).toString());
        assertEquals("1.001:<31/7/12", text(result, 2, 0));

        Result sending = validate(good(central.andThen(r -> part(r, 2, 0).groupAddress = "31/7/13")));

        assertFalse(problems(sending).stream().anyMatch(p -> p.contains("read-without-flag")),
                problems(sending).toString());
        assertEquals("1.001:<31/7/13", text(sending, 2, 0));
    }

    @Test
    void aReadOnAnAddressOnlyAFilterDeviceAnswersIsKeptWithAWarning() {
        Result result = validate(good(r -> {
            part(r, 2, 0).groupAddress = "31/7/14";
            part(r, 2, 0).read = true;
        }));

        assertTrue(problems(result).contains("WARNING read-without-flag things/0/channels/2/parts/0"),
                problems(result).toString());
        assertEquals("1.001:<31/7/14", text(result, 2, 0));
    }

    @Test
    void aStatusAddressAsChannelIsStatusOnly() {
        assertEquals(List.of("WARNING status-only things/0/channels/1"), problems(validate(good())));
    }

    // read marks

    @Test
    void bothReadTicksGiveOneMarkOnTheStatusAddress() {
        Result result = validate(good(r -> part(r, 0, 0).read = true));

        assertEquals("1.001:31/7/1+<31/7/2", text(result, 0, 0));
        assertEquals(3, result.readTelegrams());
    }

    @Test
    void aReadTickOnTheMainAddressAloneMarksIt() {
        Result result = validate(good(r -> {
            part(r, 0, 0).read = true;
            part(r, 0, 0).statusRead = false;
        }));

        assertEquals(Read.MAIN, model(result).things().get(0).channels().get(0).parts().get(0).read());
        assertEquals(List.of("31/7/1", "31/7/4", "31/7/8"), result.readAddresses());
    }

    @Test
    void anAddressIsMarkedInItsFirstChannelOnly() {
        Result result = validate(good(r -> Objects.requireNonNull(thing0(r).channels)
                .add(channel("Light_2", "Light 2", "switch",
                        item("Kitchen_Light_2", "Switch", "Light 2", null, List.of(), List.of()),
                        part("ga", "31/7/12", "31/7/2", true, true)))));

        assertEquals("1.001:31/7/1+<31/7/2", text(result, 0, 0));
        assertEquals("1.001:31/7/12+31/7/2", text(result, 3, 0));
        assertEquals(3, result.readTelegrams());
    }

    @Test
    void withoutReadAtStartNothingIsRead() {
        Result result = validate(good(r -> r.readAtStart = false));

        assertEquals(List.of(), result.readAddresses());
        assertEquals(0, result.readTelegrams());
        assertFalse(ProposalText.of(model(result)).thingsText().contains("<"));
        assertFalse(model(result).readAtStart());
        assertEquals(0, ProposalPlan.of(model(result)).readTelegrams());
    }

    @Test
    void theReadsEqualTheMarksOfTheText() {
        for (boolean read : List.of(true, false)) {
            Result result = validate(good(r -> {
                part(r, 0, 0).read = read;
                part(r, 2, 0).read = read;
                part(r, 2, 1).read = read;
            }));
            String text = ProposalText.of(model(result)).thingsText();
            assertEquals(text.chars().filter(c -> c == '<').count(), result.readTelegrams());
            assertEquals(ProposalPlan.of(model(result)).readAddresses(), result.readAddresses());
        }
    }

    @Test
    void withAnErrorTheReadsOfTheCheckedPartsAreGiven() {
        Result result = validate(good(r -> item(r, 0).type = "Group"));

        assertNull(result.proposal());
        assertEquals(List.of("31/7/2", "31/7/4", "31/7/8"), result.readAddresses());
        assertEquals(3, result.readTelegrams());
    }

    // the request's shape

    @Test
    void aMissingThingListIsABadRequest() {
        BadRequest e = assertThrows(BadRequest.class, () -> validate(good(r -> r.things = null)));
        assertEquals("things", e.where());
    }

    @Test
    void aNullEntryIsABadRequest() {
        BadRequest e = assertThrows(BadRequest.class, () -> validate(
                good(r -> thing0(r).channels = Arrays.asList(new ProposedChannelDTO[] { channel(r, 0), null }))));
        assertEquals("things/0/channels/1", e.where());
    }

    @Test
    void aMissingRequiredFieldIsABadRequest() {
        assertEquals("things/0/channels/0/item/name",
                assertThrows(BadRequest.class, () -> validate(good(r -> item(r, 0).name = null))).where());
        assertEquals("things/0/channels/0/parts/0/groupAddress",
                assertThrows(BadRequest.class, () -> validate(good(r -> part(r, 0, 0).groupAddress = null))).where());
        assertEquals("roomGroups/0/label",
                assertThrows(BadRequest.class, () -> validate(good(r -> room0(r).label = null))).where());
    }

    @Test
    void tooManyKeysAreABadRequest() {
        PreviewRequestDTO request = good();
        List<ProposedChannelDTO> channels = Objects.requireNonNull(thing0(request).channels);
        for (int i = 0; i < 2000; i++) {
            channels.add(channel("C" + i, "C", "switch", item("I" + i, "Switch", "C", null, List.of(), List.of()),
                    part("ga", "31/7/12", null, false, false)));
        }
        assertThrows(BadRequest.class, () -> validate(request));
    }

    // problem texts

    @Test
    void noProblemTextRepeatsRequestText() {
        Result result = validate(good(r -> {
            thing0(r).id = MARKER + " id";
            thing0(r).label = MARKER.repeat(30);
            channel(r, 0).id = MARKER + "-";
            channel(r, 0).label = "";
            channel(r, 0).channelType = MARKER;
            part(r, 1, 0).key = MARKER;
            part(r, 1, 0).groupAddress = MARKER;
            part(r, 2, 1).statusAddress = MARKER;
            item(r, 2).name = "-" + MARKER;
            item(r, 2).type = MARKER;
            item(r, 2).category = MARKER + ">";
            item(r, 2).tags = List.of(MARKER + "\"");
            item(r, 2).groups = List.of(MARKER);
            room0(r).groupName = MARKER + " group";
        }));

        assertTrue(result.problems().size() > 10, problems(result).toString());
        for (Problem problem : result.problems()) {
            assertFalse(problem.text().contains(MARKER), problem.text());
            assertFalse(problem.where().contains(MARKER), problem.where());
        }
    }

    // location groups

    private static ProposedRoomDTO group(String groupName, String label, @Nullable String tag,
            @Nullable String parent) {
        ProposedRoomDTO room = room(groupName, label);
        room.tag = tag;
        room.parent = parent;
        return room;
    }

    /** The good request with its Items in gKitchen, which lies in gUp, which lies in gHouse; sent children first. */
    private static PreviewRequestDTO nested() {
        return good(r -> r.roomGroups = new ArrayList<>(
                List.of(group("gKitchen", "Kitchen", "Room", "gUp"), group("gUp", "Up", "Floor", "gHouse"),
                        group("gHouse", "House", "Building", null), group("gBoard", "Board", "", "gHouse"))));
    }

    private static List<String> groupsOf(Result result) {
        return model(result).locationGroups().stream()
                .map(g -> g.name() + " " + g.tag() + " " + g.parent() + (g.existing() ? " there" : "")).toList();
    }

    @Test
    void locationGroupsAreCheckedAndOrderedParentsFirst() {
        Result result = validate(nested());

        assertEquals(List.of(), errors(result));
        assertEquals(List.of("gHouse Building null", "gBoard null gHouse", "gUp Floor gHouse", "gKitchen Room gUp"),
                groupsOf(result));
        assertEquals("""
                Group gHouse "House" ["Building"]
                Group gBoard "Board" (gHouse)
                Group gUp "Up" (gHouse) ["Floor"]
                Group gKitchen "Kitchen" (gUp) ["Room"]
                """, ProposalText.of(model(result)).itemsText().split("\n\n")[0] + "\n");
    }

    @Test
    void aRequestOfBeforeTheLocationGroupsGivesRoomsWithoutParent() {
        assertEquals(List.of("gKitchen Room null"), groupsOf(validate(good())));
    }

    @Test
    void aGroupThatExistsInOpenhabIsUsedAsItIsAndAnItemThatIsNoGroupIsRefused() {
        groups.add("gHouse");
        groups.add("gKitchen");

        Result result = validate(nested());

        assertEquals(List.of(), errors(result));
        assertEquals(List.of("gHouse Building null there", "gBoard null gHouse", "gUp Floor gHouse",
                "gKitchen Room gUp there"), groupsOf(result));
        assertEquals(List.of("gBoard", "gUp"), ProposalPlan.of(model(result)).steps().stream()
                .filter(step -> step.kind() == ProposalPlan.Kind.GROUP).map(ProposalPlan.Step::itemName).toList());

        groups.clear();
        items.add("gUp");
        assertEquals(List.of("item-exists roomGroups/1/groupName"), errors(validate(nested())));
    }

    private static PreviewRequestDTO nested(Consumer<List<ProposedRoomDTO>> change) {
        PreviewRequestDTO request = nested();
        change.accept(Objects.requireNonNull(request.roomGroups));
        return request;
    }

    @Test
    void aParentMustBeAnotherLocationGroupOfTheRequest() {
        groups.add("gElsewhere");
        assertEquals(List.of("unknown-group roomGroups/1/parent"),
                errors(validate(nested(g -> g.get(1).parent = "gElsewhere"))));
        assertEquals(List.of("unknown-group roomGroups/1/parent"),
                errors(validate(nested(g -> g.get(1).parent = "gUp"))));
        assertEquals(List.of("unknown-group roomGroups/1/parent"),
                errors(validate(nested(g -> g.get(1).parent = MARKER))));
        assertEquals(List.of(), errors(validate(nested(g -> g.get(1).parent = ""))));
    }

    @Test
    void groupsThatLieInEachOtherAreAGroupCycle() {
        Result result = validate(nested(g -> g.get(2).parent = "gKitchen"));

        assertEquals(List.of("group-cycle roomGroups/0/parent", "group-cycle roomGroups/1/parent",
                "group-cycle roomGroups/2/parent", "group-cycle roomGroups/3/parent"), errors(result));
        assertNull(result.proposal());
    }

    @Test
    void aTagIsOneOfTheLocationTagsOrNone() {
        for (String tag : ProposalLocations.TAGS) {
            assertEquals(List.of(), errors(validate(nested(g -> g.get(0).tag = tag))), tag);
        }
        for (String tag : List.of("Kitchen", "Lightbulb", "room", MARKER)) {
            Result result = validate(nested(g -> g.get(0).tag = tag));
            assertEquals(List.of("bad-tag roomGroups/0/tag"), errors(result), tag);
            assertFalse(result.problems().toString().contains(MARKER));
        }
    }

    @Test
    void theIconOfALocationGroupIsCheckedLikeAnItemsIcon() {
        Result good = validate(nested(g -> g.get(0).category = "house"));
        assertEquals("house", model(good).locationGroups().get(3).category());
        assertEquals(List.of("bad-category roomGroups/0/category"),
                errors(validate(nested(g -> g.get(0).category = "Hou se"))));
    }

    // the target of the lines for an existing Thing

    @Test
    void aTargetIsAUidOfADeviceThingOrAnIdBelowTheBridge() {
        assertEquals(Map.of(), validate(good()).targets());
        assertEquals(Map.of(), validate(good(r -> thing0(r).target = "")).targets());
        assertEquals(Map.of("knx:device:home:Kitchen", "knx:device:home:wall"),
                validate(good(r -> thing0(r).target = "wall")).targets());
        assertEquals(Map.of("knx:device:home:Kitchen", "knx:device:other:wall-2"),
                validate(good(r -> thing0(r).target = "knx:device:other:wall-2")).targets());
        assertEquals(Map.of("knx:device:home:Kitchen", "knx:device:wall"),
                validate(good(r -> thing0(r).target = "knx:device:wall")).targets());
    }

    @Test
    void aTargetThatIsNeitherIsAWarningAndStopsNothing() {
        for (String target : List.of("knx:ip:home", "mqtt:topic:x", "wall } Thing", "knx:device:a:b:c",
                MARKER + " x")) {
            Result result = validate(good(r -> thing0(r).target = target));
            assertEquals(List.of("WARNING status-only things/0/channels/1", "WARNING bad-target things/0/target"),
                    problems(result), target);
            assertEquals(Map.of(), result.targets());
            assertNotNull(result.proposal());
            assertFalse(result.problems().toString().contains(MARKER));
        }
    }

    @Test
    void theTargetChangesNeitherTheModelNorTheSteps() {
        Result plain = validate(good());
        Result withTarget = validate(good(r -> thing0(r).target = "wall"));

        assertEquals(plain.proposal(), withTarget.proposal());
        assertEquals(ProposalPlan.of(model(plain)), ProposalPlan.of(model(withTarget)));
    }
}
