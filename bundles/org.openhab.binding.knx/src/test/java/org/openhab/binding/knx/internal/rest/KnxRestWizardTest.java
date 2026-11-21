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
package org.openhab.binding.knx.internal.rest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.knx.internal.project.ChannelFixtures.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;
import org.openhab.binding.knx.internal.KNXBindingConstants;
import org.openhab.binding.knx.internal.handler.DeviceThingHandler;
import org.openhab.binding.knx.internal.handler.IPBridgeThingHandler;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.project.KnxConfiguredAddress;
import org.openhab.binding.knx.internal.project.KnxIndividualAddress;
import org.openhab.binding.knx.internal.project.KnxProject;
import org.openhab.binding.knx.internal.project.KnxProposal;
import org.openhab.binding.knx.internal.project.KnxProposalBuilder;
import org.openhab.binding.knx.internal.project.ProjectTestFiles;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore;
import org.openhab.binding.knx.internal.rest.dto.ChannelBodyDTO;
import org.openhab.binding.knx.internal.rest.dto.CreateStepDTO;
import org.openhab.binding.knx.internal.rest.dto.PreviewRequestDTO;
import org.openhab.binding.knx.internal.rest.dto.ProblemDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposalDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposalLinesDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposalPreviewDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposalRequestDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedChannelDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedItemDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedPartDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedThingDTO;
import org.openhab.binding.knx.internal.rest.dto.ThingBodyDTO;
import org.openhab.binding.knx.internal.wizard.ProposalNames;
import org.openhab.binding.knx.internal.wizard.ProposalNames.Names;
import org.openhab.binding.knx.internal.wizard.ProposalValidator;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.calimero.GroupAddress;

/**
 * Calls 21 (the proposal) and 22 (the preview) of the wizard on the plain test project, with registries as mocks.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxRestWizardTest {
    private static final String HOME = "knx:ip:home";

    private @TempDir Path folder = Path.of("");
    private KnxProjectStore store = new KnxProjectStore(Path.of("target", "unused"), Clock.systemUTC());
    private IPBridgeThingHandler home = RestFixtures.handler("home", new TelegramMonitor());
    private ThingRegistry thingRegistry = mock(ThingRegistry.class);
    private ItemRegistry itemRegistry = mock(ItemRegistry.class);

    @BeforeEach
    void setUp() throws Exception {
        store = new KnxProjectStore(folder, Clock.systemUTC());
        home = RestFixtures.handler("home", new TelegramMonitor());
        thingRegistry = mock(ThingRegistry.class);
        itemRegistry = mock(ItemRegistry.class);
        store.importProject(RestFixtures.uid("home"), ProjectTestFiles.plain(), null, "de-DE", "house.knxproj");
    }

    private KnxRestService service(IPBridgeThingHandler... handlers) {
        return new KnxRestService(RestFixtures.factory(handlers), store, thingRegistry, itemRegistry,
                mock(ItemChannelLinkRegistry.class));
    }

    private KnxRestService service() {
        return service(home, RestFixtures.handler("other", new TelegramMonitor()));
    }

    private static ProposalRequestDTO selection(@Nullable List<@Nullable String> addresses,
            @Nullable List<@Nullable String> devices) {
        ProposalRequestDTO request = new ProposalRequestDTO();
        request.groupAddresses = addresses == null ? null : new ArrayList<>(addresses);
        request.devices = devices == null ? null : new ArrayList<>(devices);
        return request;
    }

    /** Every address and every device of the project. */
    private ProposalRequestDTO everything(KnxRestService service) {
        List<@Nullable String> addresses = new ArrayList<>(
                service.groupAddresses(HOME, null, null).items.stream().map(address -> address.address).toList());
        List<@Nullable String> devices = new ArrayList<>(
                service.devices(HOME, null, null).items.stream().map(device -> device.id).toList());
        return selection(addresses, devices);
    }

    /** The proposal sent back as it came, its rooms as room groups. */
    static PreviewRequestDTO back(ProposalDTO proposal) {
        PreviewRequestDTO request = new PreviewRequestDTO();
        request.things = proposal.things;
        request.roomGroups = proposal.rooms;
        request.readAtStart = proposal.readAtStart;
        return request;
    }

    private static void assertFails(KnxRestError error, Executable call) {
        KnxRestException e = assertThrows(KnxRestException.class, call);
        assertEquals(error.code(), e.code());
    }

    private static KnxRestException assertBadInput(String name, Executable call) {
        KnxRestException e = assertThrows(KnxRestException.class, call);
        assertEquals("bad-input", e.code());
        assertTrue(e.text().startsWith(name + ": "), e.text());
        return e;
    }

    private static List<ProposedChannelDTO> channels(ProposalDTO proposal) {
        List<ProposedChannelDTO> channels = new ArrayList<>();
        for (ProposedThingDTO thing : proposal.things) {
            channels.addAll(Objects.requireNonNull(thing.channels));
        }
        return channels;
    }

    // call 21

    @Test
    void theProposalForAnAddressHasItsThingChannelAndItem() {
        ProposalDTO proposal = service().proposal(HOME, selection(List.of("31/7/2", "31/7/99"), null));

        assertEquals(List.of("31/7/99"), proposal.unknown);
        assertTrue(proposal.readAtStart);
        assertEquals(1, proposal.things.size());
        ProposedThingDTO thing = proposal.things.get(0);
        assertEquals("KNX", thing.id);
        assertEquals("knx:device:home:KNX", thing.uid);
        assertEquals("ADDRESSES", thing.source);
        ProposedChannelDTO channel = Objects.requireNonNull(thing.channels).get(0);
        List<ProposedPartDTO> parts = Objects.requireNonNull(channel.parts);
        assertEquals(1, parts.size());
        assertEquals("31/7/2", parts.get(0).groupAddress);
        assertNotNull(channel.id);
        ProposedItemDTO item = Objects.requireNonNull(channel.item);
        assertNotNull(item.name);
        assertEquals(List.of(), item.groups);
        assertNull(channel.split);
    }

    @Test
    void chosenDevicesThatGotNoThingAreListedAsLeftOut() {
        KnxRestService service = service();
        ProposalRequestDTO request = everything(service);
        Objects.requireNonNull(request.devices).add("7.7.99");

        ProposalDTO proposal = service.proposal(HOME, request);

        assertEquals(List.of("P-0777-0_DI-1"), proposal.things.stream().filter(thing -> "DEVICE".equals(thing.source))
                .map(thing -> thing.sourceId).toList());
        assertEquals(
                "[{\"source\":\"DEVICE\",\"sourceId\":\"P-0777-0_DI-2\"},"
                        + "{\"source\":\"DEVICE\",\"sourceId\":\"P-0777-0_DI-3\"}]",
                RestFixtures.GSON.toJson(proposal.leftOut));
        assertEquals(List.of("7.7.99"), proposal.unknown);
    }

    @Test
    void leftOutIsAnEmptyListWhenEveryChosenEntryGotAThing() {
        ProposalDTO proposal = service().proposal(HOME, selection(List.of("31/7/2", "31/7/99"), null));

        assertEquals(List.of(), proposal.leftOut);
        assertTrue(RestFixtures.GSON.toJson(proposal).contains("\"leftOut\":[]"));
    }

    @Test
    void readAtStartFalseMarksNothing() {
        ProposalRequestDTO request = everything(service());
        request.readAtStart = false;

        ProposalDTO proposal = service().proposal(HOME, request);

        assertFalse(proposal.readAtStart);
        for (ProposedChannelDTO channel : channels(proposal)) {
            Objects.requireNonNull(channel.parts).forEach(part -> assertFalse(part.text.contains("<"), part.text));
        }
    }

    @Test
    void theIdsAndNamesAvoidWhatTheRegistriesHold() {
        when(thingRegistry.get(new ThingUID("knx:device:home:KNX"))).thenReturn(mock(Thing.class));
        String first = Objects.requireNonNull(Objects
                .requireNonNull(service().proposal(HOME, selection(List.of("31/7/2"), null)).things.get(0).channels)
                .get(0).item).name;
        when(itemRegistry.get(Objects.requireNonNull(first))).thenReturn(mock(Item.class));

        ProposalDTO proposal = service().proposal(HOME, selection(List.of("31/7/2"), null));

        assertEquals("knx:device:home:KNX_2", proposal.things.get(0).uid);
        assertEquals(first + "_2",
                Objects.requireNonNull(Objects.requireNonNull(proposal.things.get(0).channels).get(0).item).name);
    }

    @Test
    void alreadyConfiguredNamesTheChannelsOfTheBridge() {
        home = RestFixtures
                .handler(
                        bridgeWithChannels(List.of(new KnxConfiguredAddress(new GroupAddress(31, 7, 2).getRawAddress(),
                                "9.001", false, true, "knx:device:home:made-up-actuator:temperature ga")), 2),
                        new TelegramMonitor());

        ProposalDTO proposal = service().proposal(HOME, selection(List.of("31/7/2"), null));

        ProposedChannelDTO channel = channels(proposal).get(0);
        assertEquals(List.of("knx:device:home:made-up-actuator:temperature"), channel.alreadyConfigured);
        assertFalse(channel.selected);
        assertEquals(2, proposal.thingsWithoutHandler);
    }

    @Test
    void theProposalListsTheDeviceThingsOfTheBridgeWithTheirChannelIds() {
        Bridge bridge = bridgeWithChannels(List.of(), 2);
        Thing spare = bridge.getThings().get(2);
        when(spare.getLabel()).thenReturn("Made-up\u202Espare");
        ThingUID spareUid = spare.getUID();
        Channel light = mock(Channel.class);
        when(light.getUID()).thenReturn(new ChannelUID(spareUid, "light"));
        Channel dim = mock(Channel.class);
        when(dim.getUID()).thenReturn(new ChannelUID(spareUid, "dim"));
        when(spare.getChannels()).thenReturn(List.of(light, dim));
        home = RestFixtures.handler(bridge, new TelegramMonitor());

        ProposalDTO proposal = service().proposal(HOME, selection(List.of("31/7/2"), null));

        // a Thing of another type is not offered; the list is in the order of the UIDs
        assertEquals(List.of("knx:device:home:made-up-spare-0", "knx:device:home:made-up-spare-1"),
                proposal.deviceThings.stream().map(thing -> thing.uid).toList());
        assertNull(proposal.deviceThings.get(0).label);
        assertEquals(List.of(), proposal.deviceThings.get(0).channelIds);
        assertEquals("Made-up spare", proposal.deviceThings.get(1).label);
        assertEquals(List.of("dim", "light"), proposal.deviceThings.get(1).channelIds);
        assertEquals(List.of(), service(RestFixtures.handler("home", new TelegramMonitor())).proposal(HOME,
                selection(List.of("31/7/2"), null)).deviceThings);
    }

    private static Bridge bridgeWithChannels(List<KnxConfiguredAddress> configured, int withoutHandler) {
        List<Thing> things = new ArrayList<>();
        DeviceThingHandler device = mock(DeviceThingHandler.class);
        when(device.configuredAddresses()).thenReturn(configured);
        Thing withHandler = mock(Thing.class);
        when(withHandler.getUID()).thenReturn(new ThingUID("knx", "device", "home", "made-up-actuator"));
        when(withHandler.getHandler()).thenReturn(device);
        things.add(withHandler);
        for (int i = 0; i < withoutHandler; i++) {
            Thing thing = mock(Thing.class);
            when(thing.getUID()).thenReturn(new ThingUID("knx", "device", "home", "made-up-spare-" + i));
            when(thing.getThingTypeUID()).thenReturn(KNXBindingConstants.THING_TYPE_DEVICE);
            things.add(thing);
        }
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(RestFixtures.uid("home"));
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(bridge.getThings()).thenReturn(things);
        return bridge;
    }

    @Test
    void aProposalRequestThatCannotBeReadIsBadInput() {
        KnxRestService service = service();
        assertBadInput("selection", () -> service.proposal(HOME, null));
        assertBadInput("selection", () -> service.proposal(HOME, new ProposalRequestDTO()));
        assertBadInput("selection", () -> service.proposal(HOME, selection(List.of(), List.of())));
        assertBadInput("groupAddresses", () -> service.proposal(HOME, selection(Arrays.asList("31/7/1", null), null)));
        assertBadInput("devices", () -> service.proposal(HOME, selection(null, Arrays.asList("7.7.1", null))));
        KnxRestException e = assertBadInput("groupAddresses",
                () -> service.proposal(HOME, selection(List.of("made-up-text"), null)));
        assertFalse(e.text().contains("made-up-text"));
        List<@Nullable String> many = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            many.add("31/7/1");
        }
        assertEquals(1, service.proposal(HOME, selection(many, many)).things.size());
        many.add("31/7/1");
        List<@Nullable String> more = many;
        assertBadInput("selection", () -> service.proposal(HOME, selection(more, more)));
    }

    @Test
    void theWizardCallsAnswerUnknownBridgeNoProjectOrUnusable() throws Exception {
        Files.writeString(folder.resolve("project-knx_ip_broken.json"), "{not json", StandardCharsets.UTF_8);
        KnxRestService service = service(home, RestFixtures.handler("other", new TelegramMonitor()),
                RestFixtures.handler("broken", new TelegramMonitor()));
        PreviewRequestDTO preview = back(service.proposal(HOME, everything(service)));
        for (String bridge : new String[] { "knx:ip:nobody", "hue:bridge:home", "knx:ip:other", "knx:ip:broken" }) {
            KnxRestError error = bridge.endsWith("broken") ? KnxRestError.PROJECT_UNUSABLE
                    : bridge.endsWith("other") ? KnxRestError.NO_PROJECT : KnxRestError.UNKNOWN_BRIDGE;
            assertFails(error, () -> service.proposal(bridge, selection(List.of("31/7/1"), null)));
            assertFails(error, () -> service.preview(bridge, preview));
        }
    }

    // call 22

    @Test
    void theProposalSentBackGivesTextCountsAndSteps() {
        KnxRestService service = service();
        ProposalDTO proposal = service.proposal(HOME, everything(service));

        ProposalPreviewDTO preview = service.preview(HOME, back(proposal));

        assertFalse(preview.problems.stream().anyMatch(problem -> "ERROR".equals(problem.severity)),
                preview.problems.stream().map(problem -> problem.code + " " + problem.where).toList().toString());
        String thingsText = Objects.requireNonNull(preview.thingsText);
        assertTrue(thingsText.startsWith("Thing knx:device:home:"), thingsText);
        assertNotNull(preview.itemsText);
        List<CreateStepDTO> steps = Objects.requireNonNull(preview.steps);
        long selected = channels(proposal).stream().filter(channel -> channel.selected).count();
        assertTrue(selected > 0);
        assertEquals(selected, Objects.requireNonNull(preview.counts).channels);
        assertEquals(selected, steps.stream().filter(step -> "LINK".equals(step.kind)).count());
        assertEquals(thingsText.chars().filter(c -> c == '<').count(), preview.readTelegrams);
        CreateStepDTO thing = steps.stream().filter(step -> "THING".equals(step.kind)).findFirst().orElseThrow();
        ThingBodyDTO body = Objects.requireNonNull(thing.thing);
        assertEquals(thing.uid, body.UID());
        assertEquals(HOME, body.bridgeUID());
    }

    @Test
    void theAnswersOfThePreviewHoldNoAddressSetting() {
        KnxRestService service = service();
        for (boolean readAtStart : List.of(true, false)) {
            ProposalRequestDTO request = everything(service);
            request.readAtStart = readAtStart;
            ProposalPreviewDTO preview = service.preview(HOME, back(service.proposal(HOME, request)));

            String thingsText = Objects.requireNonNull(preview.thingsText);
            assertFalse(thingsText.contains("address="));
            assertFalse(thingsText.contains(") ["), "a configuration block after a Thing's label");
            JsonObject json = JsonParser.parseString(RestFixtures.GSON.toJson(preview)).getAsJsonObject();
            int bodies = 0;
            for (JsonElement step : json.getAsJsonArray("steps")) {
                JsonObject thing = step.getAsJsonObject().getAsJsonObject("thing");
                if (thing == null) {
                    continue;
                }
                bodies++;
                assertEquals(new JsonObject(), thing.getAsJsonObject("configuration"));
                JsonArray channels = thing.getAsJsonArray("channels");
                for (JsonElement channel : channels) {
                    JsonObject configuration = channel.getAsJsonObject().getAsJsonObject("configuration");
                    assertFalse(configuration.has("address"), configuration.toString());
                    assertNotNull(channel.getAsJsonObject().getAsJsonObject("properties"));
                    assertNotNull(channel.getAsJsonObject().getAsJsonArray("defaultTags"));
                }
            }
            assertTrue(bodies > 0);
        }
    }

    @Test
    void theStepBodiesAreWrittenAsCoreReadsThem() {
        KnxRestService service = service();
        ProposalPreviewDTO preview = service.preview(HOME, back(service.proposal(HOME, everything(service))));

        JsonObject json = JsonParser.parseString(RestFixtures.GSON.toJson(preview)).getAsJsonObject();
        JsonObject thing = null;
        for (JsonElement step : json.getAsJsonArray("steps")) {
            if (step.getAsJsonObject().has("thing")) {
                thing = step.getAsJsonObject().getAsJsonObject("thing");
                break;
            }
        }
        assertNotNull(thing);
        assertEquals(List.of("UID", "thingTypeUID", "bridgeUID", "label", "configuration", "channels"),
                List.copyOf(thing.keySet()));
        assertEquals("knx:device", thing.get("thingTypeUID").getAsString());
        JsonObject channel = thing.getAsJsonArray("channels").get(0).getAsJsonObject();
        assertEquals(new JsonObject(), channel.getAsJsonObject("properties"));
        assertEquals(new JsonArray(), channel.getAsJsonArray("defaultTags"));
        assertEquals("STATE", channel.get("kind").getAsString());
    }

    @Test
    void aPreviewWithAnErrorHasNoTextAndNoSteps() {
        KnxRestService service = service();
        ProposalDTO proposal = service.proposal(HOME, everything(service));
        ProposedChannelDTO first = channels(proposal).stream().filter(channel -> channel.selected).findFirst()
                .orElseThrow();
        Objects.requireNonNull(first.item).type = "Group";

        ProposalPreviewDTO preview = service.preview(HOME, back(proposal));

        assertTrue(preview.problems.stream()
                .anyMatch(problem -> "ERROR".equals(problem.severity) && "type-not-allowed".equals(problem.code)));
        assertNull(preview.thingsText);
        assertNull(preview.itemsText);
        assertNull(preview.steps);
        assertNull(preview.counts);
        assertNotNull(preview.readAddresses);
        String json = RestFixtures.GSON.toJson(preview);
        assertFalse(json.contains("\"steps\""), json);
    }

    @Test
    void anExistingThingIsThingExists() {
        KnxRestService service = service();
        ProposalDTO proposal = service.proposal(HOME, everything(service));
        String uid = proposal.things.get(0).uid;
        when(thingRegistry.get(new ThingUID(uid))).thenReturn(mock(Thing.class));

        ProposalPreviewDTO preview = service.preview(HOME, back(proposal));

        assertEquals(List.of("thing-exists"), preview.problems.stream().filter(p -> "ERROR".equals(p.severity))
                .map((ProblemDTO p) -> p.code).toList());
    }

    @Test
    void aSecuredAddressWithoutKeyringIsAWarning() {
        KnxRestService service = service();
        ProposalPreviewDTO preview = service.preview(HOME,
                back(service.proposal(HOME, selection(List.of("31/7/1"), null))));

        assertTrue(
                preview.problems.stream().anyMatch(p -> "WARNING".equals(p.severity)
                        && "secured-without-keyring".equals(p.code) && p.text.contains("31/7/1")),
                preview.problems.toString());
    }

    @Test
    void aPreviewRequestThatCannotBeReadIsBadInput() {
        KnxRestService service = service();
        assertBadInput("things", () -> service.preview(HOME, null));
        assertBadInput("things", () -> service.preview(HOME, new PreviewRequestDTO()));
        PreviewRequestDTO request = back(service.proposal(HOME, everything(service)));
        Objects.requireNonNull(request.things).get(0).id = null;
        assertBadInput("things/0/id", () -> service.preview(HOME, request));
    }

    @Test
    void theStepsAreMadeFromTheModelNotFromTheRequest() {
        KnxRestService service = service();
        ProposalDTO proposal = service.proposal(HOME, everything(service));
        ProposalPreviewDTO expected = service.preview(HOME, back(proposal));
        for (ProposedThingDTO thing : proposal.things) {
            thing.uid = "knx:device:other:Evil";
            for (ProposedChannelDTO channel : Objects.requireNonNull(thing.channels)) {
                Objects.requireNonNull(channel.parts).forEach(part -> {
                    part.text = "1.001:<31/7/9";
                    part.dataType = "9.001";
                });
            }
        }

        assertEquals(RestFixtures.GSON.toJson(expected),
                RestFixtures.GSON.toJson(service.preview(HOME, back(proposal))));
        for (CreateStepDTO step : Objects.requireNonNull(expected.steps)) {
            ThingBodyDTO body = step.thing;
            if (body != null) {
                for (ChannelBodyDTO channel : body.channels()) {
                    assertFalse(channel.configuration.values().stream().anyMatch(v -> v.contains("31/7/9")));
                }
            }
        }
    }

    @Test
    void aRequestThatStillCarriesListeningIsReadAsOneWithout() {
        KnxRestService service = service();
        PreviewRequestDTO request = back(service.proposal(HOME, everything(service)));
        JsonObject json = RestFixtures.GSON.toJsonTree(request).getAsJsonObject();
        JsonArray listening = new JsonArray();
        listening.add("31/7/9");
        int parts = 0;
        for (JsonElement thing : json.getAsJsonArray("things")) {
            for (JsonElement channel : thing.getAsJsonObject().getAsJsonArray("channels")) {
                for (JsonElement part : channel.getAsJsonObject().getAsJsonArray("parts")) {
                    part.getAsJsonObject().add("listening", listening);
                    parts++;
                }
            }
        }
        assertTrue(parts > 0);

        PreviewRequestDTO old = Objects.requireNonNull(RestFixtures.GSON.fromJson(json, PreviewRequestDTO.class));

        assertEquals(RestFixtures.GSON.toJson(service.preview(HOME, request)),
                RestFixtures.GSON.toJson(service.preview(HOME, old)));
    }

    /** An HVAC mode, a counter and a switch on one actuator; the first two may be a number or a string. */
    private static KnxProject projectWithChoices() {
        return projectOf(
                List.of(address(1, raw(31, 7, 1), "20.102", "Heating mode"),
                        address(2, raw(31, 7, 2), "6.010", "Counter"), address(3, raw(31, 7, 3), "1.001", "Light")),
                List.of(device("Actuator", new KnxIndividualAddress(7, 7, 1),
                        object("O-1", 1, "Mode", null, "20.102", 1).flags("C-W--"),
                        object("O-2", 2, "Counter", null, "6.010", 2).flags("C-W--"),
                        object("O-3", 3, "Light", null, "1.001", 3).flags("C-W--"))),
                List.of());
    }

    /** Call 21 on {@link #projectWithChoices()} for its three addresses. */
    private static ProposalDTO proposalWithChoices() {
        KnxProposal proposal = new KnxProposalBuilder(projectWithChoices()).propose(new KnxProposalBuilder.Selection(
                List.of(raw(31, 7, 1), raw(31, 7, 2), raw(31, 7, 3)), List.of(), List.of(), List.of(), true), Set.of());
        Names names = new ProposalNames(uid -> false, name -> false).names(HOME, proposal);
        return KnxProposalMapper.proposal(proposal, names, Map.of(), 0);
    }

    @Test
    void everyChannelTypeOfferedWithTheFirstOfItsItemTypesPreviewsWithoutError() {
        ProposalValidator validator = new ProposalValidator(projectWithChoices(), HOME, uid -> false, name -> false,
                Set.of(), false);
        List<ProposedChannelDTO> channels = channels(proposalWithChoices());
        int tried = 0;
        for (int c = 0; c < channels.size(); c++) {
            for (String type : channels.get(c).allowedChannelTypes) {
                ProposalDTO changed = proposalWithChoices();
                ProposedChannelDTO channel = channels(changed).get(c);
                ProposedItemDTO item = Objects.requireNonNull(channel.item);
                channel.channelType = type;
                assertEquals(item.allowedTypes, item.allowedItemTypes.get(channels.get(c).channelType));
                item.type = Objects.requireNonNull(item.allowedItemTypes.get(type)).get(0);
                List<String> errors = validator.validate(back(changed)).problems().stream()
                        .filter(problem -> problem.severity() == ProposalValidator.Severity.ERROR)
                        .map(problem -> problem.code() + " " + problem.where()).toList();
                assertEquals(List.of(), errors, type);
                tried++;
            }
        }
        assertEquals(5, tried);
    }

    // location groups

    private ProposalDTO kitchenProposal() {
        ProposalRequestDTO request = new ProposalRequestDTO();
        request.devices = new ArrayList<>(List.of("7.7.1"));
        ProposalDTO proposal = service().proposal(HOME, request);
        assertFalse(proposal.things.isEmpty());
        return proposal;
    }

    private static List<String> rooms(ProposalDTO proposal) {
        return proposal.rooms.stream().map(room -> room.groupName + " " + room.type + " tag=" + room.tag + " in="
                + room.parent + " icon=" + room.category + " exists=" + room.exists).toList();
    }

    @Test
    void theProposalGivesALocationGroupPerSpaceOnThePath() {
        ProposalDTO proposal = kitchenProposal();

        assertEquals(
                List.of("gMade_up_building Building tag=Building in=null icon=house exists=false",
                        "gMade_up_kitchen Room tag=Room in=gMade_up_building icon=group exists=false"),
                rooms(proposal));
        assertEquals(proposal.rooms.get(0).id, proposal.rooms.get(1).parentId);
        assertNull(proposal.rooms.get(0).parentId);
        assertEquals(proposal.rooms.get(1).id, proposal.things.get(0).roomId);
        proposal.rooms.forEach(
                room -> assertEquals(List.of("Building", "Floor", "Room", "Corridor", "Outdoor"), room.allowedTags));
    }

    @Test
    void aGroupThatExistsInOpenhabIsUsedAndNotCreated() {
        when(itemRegistry.get("gMade_up_building")).thenReturn(new GroupItem("gMade_up_building"));
        when(itemRegistry.get("gMade_up_kitchen")).thenReturn(mock(Item.class));

        ProposalDTO proposal = kitchenProposal();

        assertEquals(
                List.of("gMade_up_building Building tag=Building in=null icon=house exists=true",
                        "gMade_up_kitchen_2 Room tag=Room in=gMade_up_building icon=group exists=false"),
                rooms(proposal));

        Objects.requireNonNull(proposal.things.get(0).channels)
                .forEach(channel -> Objects.requireNonNull(channel.item).groups = new ArrayList<>(
                        List.of("gMade_up_kitchen_2")));
        ProposalPreviewDTO preview = service().preview(HOME, back(proposal));

        assertEquals(List.of(), preview.problems.stream().filter(p -> "ERROR".equals(p.severity)).toList());
        assertEquals(List.of("gMade_up_building"), preview.existingGroups);
        List<CreateStepDTO> groups = Objects.requireNonNull(preview.steps).stream()
                .filter(step -> "GROUP".equals(step.kind)).toList();
        assertEquals(List.of("gMade_up_kitchen_2"), groups.stream().map(step -> step.itemName).toList());
        assertEquals(List.of("gMade_up_building"), Objects.requireNonNull(groups.get(0).item).groupNames);
        assertEquals(1, Objects.requireNonNull(preview.counts).groups);
        String itemsText = Objects.requireNonNull(preview.itemsText);
        assertTrue(
                itemsText.startsWith(
                        "Group gMade_up_kitchen_2 \"Made-up kitchen\" <group> (gMade_up_building) [\"Room\"]\n\n"),
                itemsText);
        assertFalse(itemsText.contains("Group gMade_up_building"));
    }

    @Test
    void theLocationGroupsSentBackAreCreatedParentsFirst() {
        ProposalDTO proposal = kitchenProposal();
        Collections.reverse(proposal.rooms);

        ProposalPreviewDTO preview = service().preview(HOME, back(proposal));

        assertEquals(List.of("gMade_up_building", "gMade_up_kitchen"), Objects.requireNonNull(preview.steps).stream()
                .filter(step -> "GROUP".equals(step.kind)).map(step -> step.itemName).toList());
        assertEquals(List.of(), preview.existingGroups);
        assertEquals(2, Objects.requireNonNull(preview.counts).groups);
    }

    // lines for a Thing that exists

    @Test
    void thePreviewHasNoLinesWithoutATarget() {
        assertNull(service().preview(HOME, back(kitchenProposal())).lines);
    }

    @Test
    void thePreviewGivesTheLinesForAnExistingThingAndMarksTheChannelIdsItHas() {
        ProposalDTO proposal = kitchenProposal();
        ProposedThingDTO first = proposal.things.get(0);
        String channelId = Objects.requireNonNull(Objects.requireNonNull(first.channels).get(0).id);
        Thing wall = mock(Thing.class);
        Channel held = mock(Channel.class);
        when(held.getUID()).thenReturn(new ChannelUID("knx:device:home:wall:" + channelId));
        when(wall.getChannels()).thenReturn(List.of(held));
        when(thingRegistry.get(new ThingUID("knx:device:home:wall"))).thenReturn(wall);
        ProposalPreviewDTO plain = service().preview(HOME, back(proposal));
        first.target = "knx:device:home:wall";

        ProposalPreviewDTO preview = service().preview(HOME, back(proposal));

        ProposalLinesDTO lines = Objects.requireNonNull(preview.lines);
        assertEquals(1, lines.existingChannels.size());
        assertEquals("knx:device:home:wall", lines.existingChannels.get(0).target);
        assertEquals(List.of(channelId), lines.existingChannels.get(0).channelIds);
        assertEquals(proposal.things.size() - 1, lines.withoutTarget);
        assertTrue(lines.thingsText.startsWith("// Channels of "), lines.thingsText);
        assertTrue(lines.thingsText.contains("    // the Thing has a channel " + channelId + " already"));
        assertTrue(lines.itemsText.contains("{ channel=\"knx:device:home:wall:" + channelId + "\" }"));
        lines.thingsText.lines().forEach(line -> assertTrue(
                line.isEmpty() || line.startsWith("//") || line.startsWith("    //") || line.startsWith("    Type "),
                line));
        assertFalse(lines.thingsText.contains("address"));
        // the text for new Things and the create steps do not look at the target
        assertEquals(plain.thingsText, preview.thingsText);
        assertEquals(plain.itemsText, preview.itemsText);
        assertEquals(RestFixtures.GSON.toJson(plain.steps), RestFixtures.GSON.toJson(preview.steps));
    }
}
