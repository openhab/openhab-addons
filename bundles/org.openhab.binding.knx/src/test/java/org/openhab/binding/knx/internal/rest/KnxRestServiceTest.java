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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.LongStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;
import org.openhab.binding.knx.internal.KNXBindingConstants;
import org.openhab.binding.knx.internal.handler.DeviceThingHandler;
import org.openhab.binding.knx.internal.handler.IPBridgeThingHandler;
import org.openhab.binding.knx.internal.monitor.ConnectionKind;
import org.openhab.binding.knx.internal.monitor.NotDecryptedReason;
import org.openhab.binding.knx.internal.monitor.SecurityState;
import org.openhab.binding.knx.internal.monitor.SendOutcome;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.monitor.TelegramType;
import org.openhab.binding.knx.internal.project.KnxConfiguredAddress;
import org.openhab.binding.knx.internal.project.ProjectTestFiles;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore;
import org.openhab.binding.knx.internal.rest.dto.AreaDTO;
import org.openhab.binding.knx.internal.rest.dto.BridgeDTO;
import org.openhab.binding.knx.internal.rest.dto.ChannelRequestDTO;
import org.openhab.binding.knx.internal.rest.dto.ChannelSuggestionDTO;
import org.openhab.binding.knx.internal.rest.dto.ChannelSuggestionsDTO;
import org.openhab.binding.knx.internal.rest.dto.ChannelUseDTO;
import org.openhab.binding.knx.internal.rest.dto.DeviceDTO;
import org.openhab.binding.knx.internal.rest.dto.DeviceDetailDTO;
import org.openhab.binding.knx.internal.rest.dto.DevicePageDTO;
import org.openhab.binding.knx.internal.rest.dto.DifferenceDTO;
import org.openhab.binding.knx.internal.rest.dto.FunctionDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupAddressDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupAddressDetailDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupAddressPageDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupObjectDTO;
import org.openhab.binding.knx.internal.rest.dto.KeyringDTO;
import org.openhab.binding.knx.internal.rest.dto.KeyringInterfaceDTO;
import org.openhab.binding.knx.internal.rest.dto.LineDTO;
import org.openhab.binding.knx.internal.rest.dto.MonitorDTO;
import org.openhab.binding.knx.internal.rest.dto.ProjectCheckDTO;
import org.openhab.binding.knx.internal.rest.dto.ProjectDTO;
import org.openhab.binding.knx.internal.rest.dto.SpaceDTO;
import org.openhab.binding.knx.internal.rest.dto.TelegramDTO;
import org.openhab.binding.knx.internal.rest.dto.TelegramPageDTO;
import org.openhab.binding.knx.internal.rest.dto.TopologyDTO;
import org.openhab.binding.knx.internal.security.KeyringFacts;
import org.openhab.binding.knx.internal.security.KeyringTestData;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;
import io.calimero.secure.Keyring;

/**
 * Checks each endpoint of {@link KnxRestService} on made-up bridges and the made-up project: the happy case and
 * every error code.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxRestServiceTest {
    private static final Instant IMPORTED = Instant.parse("2026-11-14T12:34:56Z");

    private @TempDir Path folder = Path.of("");
    private KnxProjectStore store = new KnxProjectStore(Path.of("target", "unused"), Clock.systemUTC());
    private TelegramMonitor homeMonitor = new TelegramMonitor();
    private TelegramMonitor otherMonitor = new TelegramMonitor();
    private IPBridgeThingHandler home = RestFixtures.handler("home", homeMonitor);
    private IPBridgeThingHandler other = RestFixtures.handler("other", otherMonitor);
    private ItemChannelLinkRegistry links = mock(ItemChannelLinkRegistry.class);

    @BeforeEach
    void setUp() throws Exception {
        store = new KnxProjectStore(folder, Clock.fixed(IMPORTED, ZoneOffset.UTC));
        homeMonitor = new TelegramMonitor();
        otherMonitor = new TelegramMonitor();
        home = RestFixtures.handler("home", homeMonitor);
        other = RestFixtures.handler("other", otherMonitor);
        store.importProject(RestFixtures.uid("home"), ProjectTestFiles.plain(), null, "de-DE", "house.knxproj");
    }

    private KnxRestService service() {
        return new KnxRestService(RestFixtures.factory(other, home), store, mock(ThingRegistry.class),
                mock(ItemRegistry.class), links);
    }

    private static void assertFails(KnxRestError error, Executable call) {
        KnxRestException e = assertThrows(KnxRestException.class, call);
        assertEquals(error.code(), e.code());
        assertEquals(error.status(), e.status());
    }

    private void writeStoredFile(String id, String content) throws IOException {
        Files.writeString(folder.resolve("project-knx_ip_" + id + ".json"), content, StandardCharsets.UTF_8);
    }

    // number 1 and 2

    @Test
    void bridgesListsTheHandlersInUidOrderThenTheStoredProjectsWithoutHandler() throws Exception {
        store.importProject(RestFixtures.uid("gone"), ProjectTestFiles.plain("Made-up Gone"), null, null,
                "gone.knxproj");
        homeMonitor.connected(ConnectionKind.SECURE_TUNNEL, new IndividualAddress(7, 7, 250));

        List<BridgeDTO> bridges = service().bridges();

        assertEquals(List.of("knx:ip:home", "knx:ip:other", "knx:ip:gone"),
                bridges.stream().map(bridge -> bridge.uid).toList());
        BridgeDTO first = bridges.get(0);
        assertTrue(first.present);
        assertEquals("Made-up bridge home", first.label);
        assertEquals("UNINITIALIZED", first.status);
        assertEquals("SECURE_TUNNEL", first.connection);
        assertEquals("7.7.250", first.ownAddress);
        assertEquals(TelegramMonitor.DEFAULT_BUFFER_SIZE, first.bufferSize);
        assertEquals("LOADED", first.projectState);
        assertEquals(ProjectTestFiles.PROJECT_NAME, first.projectName);
        int spaces = service().locations("knx:ip:home").size();
        assertTrue(spaces > 0);
        assertEquals(spaces, first.projectSpaces);
        assertEquals(Boolean.FALSE, first.keyringLoaded);

        BridgeDTO second = bridges.get(1);
        assertNull(second.connection);
        assertNull(second.ownAddress);
        assertEquals("NONE", second.projectState);
        assertNull(second.projectName);
        assertNull(second.projectSpaces);

        BridgeDTO gone = bridges.get(2);
        assertFalse(gone.present);
        assertEquals("LOADED", gone.projectState);
        assertEquals("Made-up Gone", gone.projectName);
        assertEquals(
                "{\"uid\":\"knx:ip:gone\",\"present\":false,\"projectState\":\"LOADED\",\"projectName\":\"Made-up Gone\","
                        + "\"projectSpaces\":" + service().locations("knx:ip:gone").size() + ",\"sendAllowed\":false}",
                RestFixtures.GSON.toJson(gone));
    }

    @Test
    void aBridgeIsFoundByItsExactUidWithAHandlerOrAStoredProject() throws Exception {
        assertEquals("knx:ip:home", service().bridge("knx:ip:home").uid);
        assertTrue(service().bridge("knx:ip:other").present);
        store.importProject(RestFixtures.uid("gone"), ProjectTestFiles.plain(), null, null, "gone.knxproj");
        BridgeDTO gone = service().bridge("knx:ip:gone");
        assertFalse(gone.present);
        assertNull(gone.label);
        assertNull(gone.keyringLoaded);
    }

    @Test
    void aTextThatIsNoBridgeUidOfThisAddOnIsAnUnknownBridge() {
        KnxRestService service = service();
        for (String text : new String[] { "", "home", "Made-up bridge home", "KNX:IP:HOME", "knx:ip:Home",
                "hue:bridge:home", "knx:device:home", "knx:ip:home:x", "knx:ip:", "knx:ip:home ", " knx:ip:home",
                "knx:ip:ho/me", "knx:ip:" + "a".repeat(300), "knx:ip:nobody", "knx:serial:home" }) {
            assertFails(KnxRestError.UNKNOWN_BRIDGE, () -> service.bridge(text));
        }
        assertFails(KnxRestError.UNKNOWN_BRIDGE, () -> service.bridge(null));
        assertFails(KnxRestError.UNKNOWN_BRIDGE, () -> service.project("hue:bridge:home"));
        assertFails(KnxRestError.UNKNOWN_BRIDGE, () -> service.project(null));
    }

    @Test
    void aSerialBridgeUidIsAccepted() {
        assertEquals("NONE", service().project("knx:serial:ttyUSB0").state);
    }

    // number 8

    @Test
    void theProjectOverviewOfALoadedProject() {
        ProjectDTO project = service().project("knx:ip:home");
        assertEquals("LOADED", project.state);
        assertEquals(ProjectTestFiles.PROJECT_NAME, project.name);
        assertEquals("Made-up installation", project.installation);
        assertEquals("2026-11-14T12:34:56Z", project.importedAt);
        assertEquals("house.knxproj", project.sourceName);
        assertEquals("de-DE", project.language);
        assertEquals("THREE_LEVEL", project.groupAddressStyle);
        assertEquals("6.4.7777.0", project.toolVersion);
        assertEquals("2026-03-04T05:06:07.000Z", project.lastModified);
        assertEquals(6, project.groupAddresses);
        assertEquals(1, project.securedGroupAddresses);
        assertEquals(3, project.devices);
        assertEquals(4, project.groupObjects);
        assertEquals(1, project.ignoredInstallations);
    }

    @Test
    void theProjectOverviewAlwaysAnswersWithTheState() throws Exception {
        assertEquals("{\"state\":\"NONE\"}", RestFixtures.GSON.toJson(service().project("knx:ip:other")));
        assertEquals("{\"state\":\"NONE\"}", RestFixtures.GSON.toJson(service().project("knx:ip:nobody")));
        writeStoredFile("broken", "{not json");
        writeStoredFile("newer", "{\"version\":2}");
        assertEquals("{\"state\":\"UNREADABLE\"}", RestFixtures.GSON.toJson(service().project("knx:ip:broken")));
        assertEquals("{\"state\":\"NEWER_VERSION\"}", RestFixtures.GSON.toJson(service().project("knx:ip:newer")));
    }

    @Test
    void theProjectOfABridgeIsLookedUpForTheEndpointsThatReadIt() throws Exception {
        KnxRestService service = service();
        assertEquals(ProjectTestFiles.PROJECT_NAME,
                service.storedProject(RestFixtures.uid("home")).project().info().name());
        assertFails(KnxRestError.NO_PROJECT, () -> service.storedProject(RestFixtures.uid("other")));
        writeStoredFile("broken", "{not json");
        writeStoredFile("newer", "{\"version\":2}");
        assertFails(KnxRestError.PROJECT_UNUSABLE, () -> service.storedProject(RestFixtures.uid("broken")));
        assertFails(KnxRestError.PROJECT_UNUSABLE, () -> service.storedProject(RestFixtures.uid("newer")));
        assertSame(home, service.handler("knx:ip:home"));
        assertFails(KnxRestError.UNKNOWN_BRIDGE, () -> service.handler("knx:ip:gone"));
    }

    // numbers 3 to 7

    private static final IndividualAddress SENSOR = new IndividualAddress(7, 7, 20);
    private static final IndividualAddress OWN = new IndividualAddress(7, 7, 250);
    private static final GroupAddress LIGHT = new GroupAddress(31, 7, 1);
    private static final GroupAddress TEMPERATURE = new GroupAddress(31, 7, 2);
    private static final GroupAddress UNKNOWN_GROUP = new GroupAddress(31, 7, 99);

    private void writes(TelegramMonitor monitor, int count) {
        for (int i = 0; i < count; i++) {
            monitor.received(SENSOR, i % 2 == 0 ? LIGHT : TEMPERATURE, TelegramType.WRITE,
                    new byte[] { (byte) (i % 2) }, SecurityState.PLAIN);
        }
    }

    private static List<Long> numbers(TelegramPageDTO page) {
        return page.telegrams.stream().map(telegram -> telegram.number).toList();
    }

    private static List<Long> range(long first, long last) {
        return LongStream.rangeClosed(first, last).boxed().toList();
    }

    private static Map<String, @Nullable String> filter(String... keysAndValues) {
        Map<String, @Nullable String> filter = new LinkedHashMap<>();
        for (String key : KnxRestService.FILTER_KEYS) {
            filter.put(key, null);
        }
        for (int i = 0; i < keysAndValues.length; i += 2) {
            filter.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return filter;
    }

    private TelegramPageDTO page(@Nullable String limit, @Nullable String after, @Nullable String monitor,
            String... filter) {
        return service().telegrams("knx:ip:home", filter(filter), limit, after, monitor);
    }

    @Test
    void theMonitorHeaderTellsCountsNumberAndId() {
        homeMonitor.connected(ConnectionKind.TUNNEL, OWN);
        writes(homeMonitor, 3);
        homeMonitor.received(SENSOR, LIGHT, TelegramType.WRITE, new byte[] { 1 }, SecurityState.PLAIN, true);
        homeMonitor.notDecrypted(SENSOR, UNKNOWN_GROUP, NotDecryptedReason.NO_KEY, address -> false);
        homeMonitor.notDecrypted(SENSOR, UNKNOWN_GROUP, null, address -> false);

        MonitorDTO monitor = service().monitor("knx:ip:home");

        assertEquals("TUNNEL", monitor.connection);
        assertEquals("7.7.250", monitor.ownAddress);
        assertEquals(TelegramMonitor.DEFAULT_BUFFER_SIZE, monitor.bufferSize);
        assertEquals(6, monitor.rows);
        assertEquals(6, monitor.lastNumber);
        assertEquals(homeMonitor.id(), monitor.monitorId);
        assertEquals(6, monitor.received);
        assertEquals(0, monitor.sent);
        assertEquals(Map.of("NO_KEY", 1L), monitor.notDecrypted);
        assertEquals(1, monitor.notDecryptedOtherReason);
        assertEquals(1, monitor.plainOnSecuredAddress);
        assertEquals(1, monitor.notDecryptedSenders.size());
        assertEquals("31/7/99", monitor.notDecryptedSenders.get(0).groupAddress);
        assertEquals(List.of("7.7.20"), monitor.notDecryptedSenders.get(0).senders);
    }

    @Test
    void theMonitorEndpointsNeedABridgeWithAHandler() throws Exception {
        store.importProject(RestFixtures.uid("gone"), ProjectTestFiles.plain(), null, null, "gone.knxproj");
        KnxRestService service = service();
        for (String bridge : new String[] { "knx:ip:gone", "knx:ip:nobody", "hue:bridge:home" }) {
            assertFails(KnxRestError.UNKNOWN_BRIDGE, () -> service.monitor(bridge));
            assertFails(KnxRestError.UNKNOWN_BRIDGE, () -> service.telegrams(bridge, filter(), null, null, null));
            assertFails(KnxRestError.UNKNOWN_BRIDGE, () -> service.clearTelegrams(bridge));
            assertFails(KnxRestError.UNKNOWN_BRIDGE, () -> service.lastValues(bridge));
            assertFails(KnxRestError.UNKNOWN_BRIDGE, () -> service.keyring(bridge));
            assertFails(KnxRestError.UNKNOWN_BRIDGE, () -> service.check(bridge));
        }
    }

    @Test
    void aPageHasTheNewestRowsOldestFirstWithMoreAndTheLastNumber() {
        writes(homeMonitor, 250);

        TelegramPageDTO page = page(null, null, null);

        assertEquals(range(51, 250), numbers(page));
        assertTrue(page.more);
        assertEquals(250, page.lastNumber);
        assertEquals(homeMonitor.id(), page.monitorId);
        assertFalse(page.reset);

        TelegramPageDTO all = page("250", null, null);
        assertEquals(range(1, 250), numbers(all));
        assertFalse(all.more);
        assertEquals(List.of(), numbers(page("0", null, null)));
        assertTrue(page("0", null, null).more);
        assertEquals(range(1, 250), numbers(page("5000", null, null)));
    }

    @Test
    void aPageAfterANumberHasTheOldestRowsAboveIt() {
        writes(homeMonitor, 250);
        TelegramPageDTO next = page("10", "100", null);
        assertEquals(range(101, 110), numbers(next));
        assertTrue(next.more);
        TelegramPageDTO end = page("10", "245", homeMonitor.id());
        assertEquals(range(246, 250), numbers(end));
        assertFalse(end.more);
        assertFalse(end.reset);
        TelegramPageDTO none = page(null, "250", null);
        assertEquals(List.of(), numbers(none));
        assertFalse(none.more);
        assertFalse(none.reset);
    }

    @Test
    void readingOnWithAfterGivesEachRowOnceWhileRowsArrive() {
        List<Long> seen = new ArrayList<>();
        long last = 0;
        for (int round = 0; round < 20; round++) {
            writes(homeMonitor, round % 3 * 5 + 1);
            TelegramPageDTO page = page("7", Long.toString(last), homeMonitor.id());
            assertFalse(page.reset);
            for (TelegramDTO telegram : page.telegrams) {
                seen.add(telegram.number);
                last = telegram.number;
            }
        }
        while (true) {
            TelegramPageDTO page = page("7", Long.toString(last), null);
            if (page.telegrams.isEmpty()) {
                break;
            }
            for (TelegramDTO telegram : page.telegrams) {
                seen.add(telegram.number);
                last = telegram.number;
            }
        }
        assertEquals(range(1, homeMonitor.lastNumber()), seen);
    }

    @Test
    void numbersOfAnotherMonitorReadFromTheStart() {
        writes(homeMonitor, 30);

        TelegramPageDTO above = page("10", "999", null);
        assertTrue(above.reset);
        assertEquals(range(1, 10), numbers(above));
        assertTrue(above.more);

        TelegramPageDTO other = page("10", "20", "made-up-other-monitor");
        assertTrue(other.reset);
        assertEquals(range(1, 10), numbers(other));

        TelegramPageDTO otherWithoutAfter = page("10", null, "made-up-other-monitor");
        assertTrue(otherWithoutAfter.reset);
        assertEquals(range(1, 10), numbers(otherWithoutAfter));

        TelegramPageDTO same = page("10", "20", homeMonitor.id());
        assertFalse(same.reset);
        assertEquals(range(21, 30), numbers(same));
        assertEquals(numbers(page("10", "20", null)), numbers(same));
        assertFalse(page("10", null, homeMonitor.id()).reset);
    }

    @Test
    void contextRowsAtOrBelowAfterAreLeftOut() {
        writes(homeMonitor, 6);
        homeMonitor.received(SENSOR, UNKNOWN_GROUP, TelegramType.READ, new byte[0], SecurityState.PLAIN);
        writes(homeMonitor, 2);

        assertEquals(range(1, 9), numbers(page(null, null, null, "type", "read", "context", "60000")));
        assertEquals(range(6, 9), numbers(page(null, "5", null, "type", "read", "context", "60000")));
    }

    @Test
    void theFilterParametersAreReadAsTheConsoleReadsThem() {
        writes(homeMonitor, 6);
        homeMonitor.sent(OWN, LIGHT, TelegramType.WRITE, new byte[] { 1 }, "1.001", null, SecurityState.PLAIN,
                SendOutcome.SENT);

        assertEquals(List.of(1L, 3L, 5L, 7L), numbers(page(null, null, null, "dst", "31/7/1")));
        assertEquals(List.of(7L), numbers(page(null, null, null, "dir", "out", "src", "7.7.250")));
        assertEquals(List.of(7L), numbers(page(null, null, null, "dpt", "1", "type", "write", "dir", "out")));
        assertEquals(range(1, 7), numbers(page(null, null, null, "from", "-1h")));
        assertEquals(List.of(), numbers(page(null, null, null, "to", "-1h")));
    }

    @Test
    void aParameterThatCannotBeReadIsBadInputNamingIt() {
        writes(homeMonitor, 3);
        assertBadInput("limit", () -> page("abc", null, null));
        assertBadInput("limit", () -> page("5001", null, null));
        assertBadInput("limit", () -> page("-1", null, null));
        assertBadInput("limit", () -> page("", null, null));
        assertBadInput("after", () -> page(null, "x", null));
        assertBadInput("after", () -> page(null, "-1", null));
        assertBadInput("after", () -> page(null, "1".repeat(30), null));
        assertBadInput("src", () -> page(null, null, null, "src", "nope"));
        assertBadInput("dst", () -> page(null, null, null, "dst", "31/7/1,x"));
        assertBadInput("type", () -> page(null, null, null, "type", "maybe"));
        assertBadInput("dir", () -> page(null, null, null, "dir", "up"));
        assertBadInput("dpt", () -> page(null, null, null, "dpt", "nine"));
        assertBadInput("context", () -> page(null, null, null, "context", "-1"));
        KnxRestException time = assertBadInput("from", () -> page(null, null, null, "from", "nonsense"));
        assertFalse(time.text().contains("nonsense"), time.text());
        assertBadInput("to", () -> page(null, null, null, "to", "2026-13-01T00:00"));
        assertEquals("src: not an individual address",
                assertBadInput("src", () -> page(null, null, null, "src", "nope")).text());
    }

    private static KnxRestException assertBadInput(String name, Executable call) {
        KnxRestException e = assertThrows(KnxRestException.class, call);
        assertEquals("bad-input", e.code());
        assertEquals(400, e.status());
        assertTrue(e.text().startsWith(name + ": "), e.text());
        return e;
    }

    @Test
    void aMonitorThatIsOffAnswersMonitorOff() {
        homeMonitor.resize(0);
        KnxRestService service = service();
        assertFails(KnxRestError.MONITOR_OFF, () -> service.telegrams("knx:ip:home", filter(), null, null, null));
        assertFails(KnxRestError.MONITOR_OFF, () -> service.lastValues("knx:ip:home"));
        assertEquals(0, service.monitor("knx:ip:home").bufferSize);
    }

    @Test
    void clearingForgetsTheRowsAndKeepsTheNumbers() {
        writes(homeMonitor, 5);
        service().clearTelegrams("knx:ip:home");
        assertEquals(0, homeMonitor.records().size());
        assertEquals(5, service().monitor("knx:ip:home").lastNumber);
        assertEquals(List.of(), service().lastValues("knx:ip:home"));
    }

    @Test
    void theLastValuesAreSortedByAddressWithNamesFromTheProject() {
        homeMonitor.useProject(store.lookup(RestFixtures.uid("home")));
        homeMonitor.received(SENSOR, TEMPERATURE, TelegramType.WRITE, new byte[] { 0x0c, 0x33 }, SecurityState.PLAIN);
        homeMonitor.received(SENSOR, LIGHT, TelegramType.WRITE, new byte[] { 1 }, SecurityState.SECURED);
        homeMonitor.received(SENSOR, LIGHT, TelegramType.READ, new byte[0], SecurityState.PLAIN);
        homeMonitor.sending(OWN, TEMPERATURE, TelegramType.READ, "9.001", null, SecurityState.PLAIN);

        List<TelegramDTO> values = service().lastValues("knx:ip:home");

        assertEquals(List.of("31/7/1", "31/7/2"), values.stream().map(value -> value.destination).toList());
        TelegramDTO light = values.get(0);
        assertEquals(2, light.number);
        assertEquals("7.7.20", light.source);
        assertEquals("Made-up light", light.destinationName);
        assertEquals("WRITE", light.type);
        assertEquals("IN", light.direction);
        assertEquals("01", light.payload);
        assertEquals("1.001", light.dataType);
        assertEquals("SECURED", light.security);
        assertNull(light.reason);
        assertNull(light.outcome);
        assertEquals("0C33", values.get(1).payload);
        assertEquals("9.001", values.get(1).dataType);
        assertNotNull(values.get(1).value);
        assertTrue(Instant.parse(light.time).isAfter(Instant.EPOCH));
    }

    @Test
    void anOwnReadStillPendingIsARowWithItsOutcome() {
        homeMonitor.sending(OWN, TEMPERATURE, TelegramType.READ, "9.001", null, SecurityState.PLAIN);
        homeMonitor.notDecrypted(SENSOR, LIGHT, NotDecryptedReason.NO_KEY, address -> false);
        List<TelegramDTO> rows = page(null, null, null).telegrams;
        assertEquals("PENDING", rows.get(0).outcome);
        assertEquals("OUT", rows.get(0).direction);
        assertNull(rows.get(0).payload);
        assertEquals("NOT_DECRYPTED", rows.get(1).security);
        assertEquals("NO_KEY", rows.get(1).reason);
        assertEquals("UNKNOWN", rows.get(1).type);
    }

    @Test
    void withoutKeyringOnlyLoadedFalse() {
        assertEquals("{\"loaded\":false}", RestFixtures.GSON.toJson(service().keyring("knx:ip:home")));
    }

    @Test
    void theKeyringTellsFileSignatureAndInterfaces() {
        Keyring keyring = KeyringTestData.load("openhab6.knxkeys");
        KeyringFacts facts = new KeyringFacts(keyring);
        KeyringFacts.InterfaceFacts first = facts.interfaces().get(0);
        when(home.getKeyringFacts()).thenReturn(facts);
        when(home.getKeyringFileName()).thenReturn("openhab6.knxkeys");
        when(home.isKeyringSignatureValid()).thenReturn(true);
        when(home.getOwnAddress()).thenReturn(Optional.of(first.address()));

        KeyringDTO dto = service().keyring("knx:ip:home");

        assertTrue(dto.loaded);
        assertEquals("openhab6.knxkeys", dto.fileName);
        assertEquals(Boolean.TRUE, dto.signatureValid);
        assertEquals(facts.hasBackboneKey(), dto.backboneKeyPresent);
        assertEquals(facts.securedGroupAddresses().size(), dto.securedGroupAddresses);
        List<KeyringInterfaceDTO> interfaces = dto.interfaces;
        assertNotNull(interfaces);
        assertEquals(facts.interfaces().size(), interfaces.size());
        assertEquals(first.address().toString(), interfaces.get(0).address);
        assertEquals(first.type().name(), interfaces.get(0).type);
        assertEquals(first.linkedSecuredGroups().size(), interfaces.get(0).linkedSecuredGroupAddresses);
        assertTrue(interfaces.get(0).own);
        assertEquals(1, interfaces.stream().filter(iface -> iface.own).count());
        for (int i = 0; i < interfaces.size(); i++) {
            int userId = facts.interfaces().get(i).userId();
            assertEquals(userId == 0 ? null : userId, interfaces.get(i).userId);
        }
    }

    // numbers 9 to 16

    private Bridge bridgeWithChannels(String id, List<KnxConfiguredAddress> configured, int withoutHandler) {
        List<Thing> things = new ArrayList<>();
        DeviceThingHandler device = mock(DeviceThingHandler.class);
        when(device.configuredAddresses()).thenReturn(configured);
        Thing withHandler = mock(Thing.class);
        when(withHandler.getUID()).thenReturn(new ThingUID("knx", "device", id, "made-up-actuator"));
        when(withHandler.getHandler()).thenReturn(device);
        things.add(withHandler);
        for (int i = 0; i < withoutHandler; i++) {
            Thing thing = mock(Thing.class);
            when(thing.getUID()).thenReturn(new ThingUID("knx", "device", id, "made-up-spare-" + i));
            when(thing.getThingTypeUID()).thenReturn(KNXBindingConstants.THING_TYPE_DEVICE);
            things.add(thing);
        }
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(RestFixtures.uid(id));
        when(bridge.getLabel()).thenReturn("Made-up bridge " + id);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(bridge.getThings()).thenReturn(things);
        return bridge;
    }

    private static KnxConfiguredAddress configured(GroupAddress address, String dataType, String label) {
        return new KnxConfiguredAddress(address.getRawAddress(), dataType, false, true, label);
    }

    @Test
    void theGroupAddressesComeInPagesWithConfiguredAndLastValue() {
        home = RestFixtures.handler(
                bridgeWithChannels("home", List.of(configured(TEMPERATURE, "9.001", "made-up temperature ga")), 0),
                homeMonitor);
        homeMonitor.received(SENSOR, LIGHT, TelegramType.WRITE, new byte[] { 1 }, SecurityState.PLAIN);

        GroupAddressPageDTO page = service().groupAddresses("knx:ip:home", null, null);

        assertEquals(6, page.total);
        assertEquals(0, page.offset);
        assertEquals(List.of("31/7/1", "31/7/2", "31/7/3", "31/7/4", "31/7/5", "31/7/6"),
                page.items.stream().map(address -> address.address).toList());
        GroupAddressDTO light = page.items.get(0);
        assertEquals(new GroupAddress(31, 7, 1).getRawAddress(), light.raw);
        assertEquals("Made-up light", light.name);
        assertEquals("1.001", light.dataType);
        assertEquals(List.of("1.001"), light.dataTypes);
        assertTrue(light.secured);
        assertEquals(List.of("Made-up main", "Made-up middle"), light.range);
        assertEquals(Boolean.FALSE, light.configured);
        TelegramDTO last = light.last;
        assertNotNull(last);
        assertEquals("01", last.payload);
        GroupAddressDTO temperature = page.items.get(1);
        assertEquals(Boolean.TRUE, temperature.configured);
        assertNull(temperature.last);
        assertEquals("made-up description", temperature.description);
        assertEquals("Made-up second line [31m", page.items.get(3).name);

        GroupAddressPageDTO second = service().groupAddresses("knx:ip:home", "2", "3");
        assertEquals(List.of("31/7/3", "31/7/4", "31/7/5"), second.items.stream().map(a -> a.address).toList());
        assertEquals(2, second.offset);
        assertEquals(6, second.total);
        assertEquals(List.of(), service().groupAddresses("knx:ip:home", "999999999", "10000").items);
    }

    private static Thing deviceThing(String bridge, String id, String label, List<KnxConfiguredAddress> configured,
            Channel... channels) {
        DeviceThingHandler device = mock(DeviceThingHandler.class);
        when(device.configuredAddresses()).thenReturn(configured);
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID("knx", "device", bridge, id));
        when(thing.getLabel()).thenReturn(label);
        when(thing.getHandler()).thenReturn(device);
        for (Channel channel : channels) {
            when(thing.getChannel(channel.getUID())).thenReturn(channel);
        }
        return thing;
    }

    private static Channel channel(String uid, String type, String label) {
        Channel channel = mock(Channel.class);
        when(channel.getUID()).thenReturn(new ChannelUID(uid));
        when(channel.getChannelTypeUID()).thenReturn(new ChannelTypeUID("knx", type));
        when(channel.getLabel()).thenReturn(label);
        return channel;
    }

    private static Bridge bridgeOf(String id, Thing... things) {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(RestFixtures.uid(id));
        when(bridge.getThings()).thenReturn(List.of(things));
        return bridge;
    }

    @Test
    void aGroupAddressTellsTheChannelsOfThisBridgeThatHoldItWithRoleAndItems() {
        int light = LIGHT.getRawAddress();
        int temperature = TEMPERATURE.getRawAddress();
        String dimmer = "knx:device:home:made-up-dimmer:light";
        String lamp = "knx:device:home:made-up-actuator:lamp";
        Thing dimmerThing = deviceThing("home", "made-up-dimmer", "Made-up dimmer",
                List.of(new KnxConfiguredAddress(temperature, "1.001", false, true, false, dimmer + " switch"),
                        new KnxConfiguredAddress(light, "1.001", true, false, true, dimmer + " switch"),
                        new KnxConfiguredAddress(temperature, "5.001", false, true, false, dimmer + " position"),
                        new KnxConfiguredAddress(light, "5.001", false, false, false, dimmer + " position"),
                        new KnxConfiguredAddress(light, "3.007", false, false, false, dimmer + " increaseDecrease")),
                channel(dimmer, "dimmer", "Made-up ceiling"));
        Thing lampThing = deviceThing("home", "made-up-actuator", "Made-up actuator",
                List.of(new KnxConfiguredAddress(light, "1.001", false, true, false, lamp + " ga")));
        String foreign = "knx:device:other:made-up-foreign:lamp";
        Thing foreignThing = deviceThing("other", "made-up-foreign", "Made-up foreign",
                List.of(new KnxConfiguredAddress(light, "1.001", false, true, false, foreign + " ga")),
                channel(foreign, "switch", "Made-up foreign lamp"));
        home = RestFixtures.handler(bridgeOf("home", dimmerThing, lampThing), homeMonitor);
        other = RestFixtures.handler(bridgeOf("other", foreignThing), otherMonitor);
        when(links.getLinkedItemNames(new ChannelUID(dimmer))).thenReturn(Set.of("MadeUp_Ceiling_B", "MadeUp_Ceiling"));
        when(links.getLinkedItemNames(new ChannelUID(foreign))).thenReturn(Set.of("MadeUp_Foreign"));
        homeMonitor.received(SENSOR, LIGHT, TelegramType.WRITE, new byte[] { 1 }, SecurityState.PLAIN);

        GroupAddressDetailDTO detail = service().groupAddress("knx:ip:home", "31/7/1");

        assertEquals("31/7/1", detail.address);
        assertEquals("Made-up light", detail.name);
        assertEquals(Boolean.TRUE, detail.configured);
        assertNotNull(detail.last);
        List<ChannelUseDTO> usedBy = Objects.requireNonNull(detail.usedBy);
        assertEquals(
                List.of("made-up-actuator:lamp main", "made-up-dimmer:light status", "made-up-dimmer:light listening"),
                usedBy.stream().map(use -> use.thingUID.substring("knx:device:home:".length()) + ":" + use.channelId
                        + " " + use.role).toList());
        ChannelUseDTO status = usedBy.get(1);
        assertEquals("knx:device:home:made-up-dimmer", status.thingUID);
        assertEquals("Made-up dimmer", status.thingLabel);
        assertEquals("Made-up ceiling", status.channelLabel);
        assertEquals("dimmer", status.channelType);
        assertEquals(List.of("MadeUp_Ceiling", "MadeUp_Ceiling_B"), status.items);
        ChannelUseDTO withoutChannel = usedBy.get(0);
        assertNull(withoutChannel.channelLabel);
        assertNull(withoutChannel.channelType);
        assertEquals(List.of(), withoutChannel.items);

        GroupAddressDetailDTO unused = service().groupAddress("knx:ip:home", "31/7/3");
        assertEquals(Boolean.FALSE, unused.configured);
        assertEquals(List.of(), unused.usedBy);
    }

    @Test
    void theDetailOfAGroupAddressSaysConfiguredAsTheListDoesAndNamesAChannelWhereItCan() {
        String lamp = "knx:device:home:made-up-actuator:lamp";
        Thing lampThing = deviceThing("home", "made-up-actuator", "Made-up actuator",
                List.of(new KnxConfiguredAddress(LIGHT.getRawAddress(), "1.001", false, true, false, lamp + " ga"),
                        new KnxConfiguredAddress(TEMPERATURE.getRawAddress(), "9.001", false, true, false,
                                "made-up label without a channel"),
                        new KnxConfiguredAddress(TEMPERATURE.getRawAddress(), "9.001", false, false, false,
                                "knx:device:home:made-up-gone:value ga")));
        home = RestFixtures.handler(bridgeOf("home", lampThing), homeMonitor);

        GroupAddressPageDTO page = service().groupAddresses("knx:ip:home", null, null);

        assertTrue(page.items.size() > 2);
        int configured = 0;
        for (GroupAddressDTO listed : page.items) {
            GroupAddressDetailDTO detail = service().groupAddress("knx:ip:home", listed.address);
            assertEquals(listed.configured, detail.configured, listed.address);
            List<ChannelUseDTO> usedBy = Objects.requireNonNull(detail.usedBy);
            if (Boolean.TRUE.equals(listed.configured)) {
                configured++;
            } else {
                assertEquals(List.of(), usedBy, listed.address);
            }
        }
        assertEquals(2, configured);
        assertEquals(List.of(lamp), Objects.requireNonNull(service().groupAddress("knx:ip:home", "31/7/1").usedBy)
                .stream().map(use -> use.thingUID + ":" + use.channelId).toList());
        GroupAddressDetailDTO unnamed = service().groupAddress("knx:ip:home", TEMPERATURE.toString());
        assertEquals(Boolean.TRUE, unnamed.configured);
        assertEquals(List.of(), unnamed.usedBy);
    }

    @Test
    void aGroupAddressWithoutBridgeHandlerHasNoUsedByAndAnUnknownOneIsAnError() throws Exception {
        store.importProject(RestFixtures.uid("stored"), ProjectTestFiles.plain(), null, null, "house.knxproj");

        GroupAddressDetailDTO detail = service().groupAddress("knx:ip:stored", "31/7/1");

        assertNull(detail.configured);
        assertNull(detail.usedBy);
        assertNull(detail.last);
        assertFails(KnxRestError.UNKNOWN_GROUP_ADDRESS, () -> service().groupAddress("knx:ip:home", "31/0/99"));
        assertBadInput("address", () -> service().groupAddress("knx:ip:home", "made-up"));
        assertBadInput("address", () -> service().groupAddress("knx:ip:home", null));
        assertFails(KnxRestError.NO_PROJECT, () -> service().groupAddress("knx:ip:other", "31/7/1"));
        assertFails(KnxRestError.UNKNOWN_BRIDGE, () -> service().groupAddress("made-up", "31/7/1"));
    }

    @Test
    void theStoredProjectOfABridgeWithoutHandlerIsReadable() throws Exception {
        store.importProject(RestFixtures.uid("gone"), ProjectTestFiles.plain(), null, null, "gone.knxproj");
        KnxRestService service = service();

        GroupAddressPageDTO addresses = service.groupAddresses("knx:ip:gone", null, null);
        assertEquals(6, addresses.items.size());
        assertNull(addresses.items.get(0).configured);
        assertNull(addresses.items.get(0).last);
        assertFalse(RestFixtures.GSON.toJson(addresses).contains("configured"));
        assertEquals(3, service.devices("knx:ip:gone", null, null).total);
        assertEquals("7.7.1", service.device("knx:ip:gone", "7.7.1").address);
        assertEquals(3, service.deviceChannels("knx:ip:gone", "7.7.1").size());
        assertEquals(1, service.channels("knx:ip:gone", request("31/7/1")).suggestions.size());
        assertEquals(1, service.topology("knx:ip:gone").areas.size());
        assertEquals(1, service.locations("knx:ip:gone").size());
        assertEquals("LOADED", service.project("knx:ip:gone").state);
    }

    @Test
    void theProjectEndpointsAnswerNoProjectOrUnusable() throws Exception {
        KnxRestService service = service();
        writeStoredFile("broken", "{not json");
        for (String bridge : new String[] { "knx:ip:other", "knx:ip:nobody", "knx:ip:broken" }) {
            KnxRestError error = bridge.endsWith("broken") ? KnxRestError.PROJECT_UNUSABLE : KnxRestError.NO_PROJECT;
            assertFails(error, () -> service.groupAddresses(bridge, null, null));
            assertFails(error, () -> service.devices(bridge, null, null));
            assertFails(error, () -> service.device(bridge, "7.7.1"));
            assertFails(error, () -> service.deviceChannels(bridge, "7.7.1"));
            assertFails(error, () -> service.channels(bridge, request("31/7/1")));
            assertFails(error, () -> service.topology(bridge));
            assertFails(error, () -> service.locations(bridge));
        }
        assertFails(KnxRestError.NO_PROJECT, () -> service.check("knx:ip:other"));
        assertFails(KnxRestError.UNKNOWN_BRIDGE, () -> service.groupAddresses("hue:bridge:home", null, null));
    }

    @Test
    void pageParametersThatCannotBeReadAreBadInput() {
        KnxRestService service = service();
        assertBadInput("offset", () -> service.groupAddresses("knx:ip:home", "-1", null));
        assertBadInput("offset", () -> service.devices("knx:ip:home", "x", null));
        assertBadInput("offset", () -> service.devices("knx:ip:home", "99999999999", null));
        assertBadInput("limit", () -> service.groupAddresses("knx:ip:home", null, "10001"));
        assertBadInput("limit", () -> service.devices("knx:ip:home", null, "many"));
        assertEquals(1, service.devices("knx:ip:home", "2", "10000").items.size());
    }

    @Test
    void theDevicesComeInPagesAsTheReaderGivesThem() {
        DevicePageDTO page = service().devices("knx:ip:home", null, null);
        assertEquals(3, page.total);
        DeviceDTO first = page.items.get(0);
        assertEquals("P-0777-0_DI-1", first.id);
        assertEquals("7.7.1", first.address);
        assertEquals(ProjectTestFiles.DEVICE_NAME, first.name);
        assertEquals("Made-up switch actuator product", first.product);
        assertEquals("MU 7.1", first.orderNumber);
        assertEquals("Made-up kitchen", first.room);
        assertEquals(3, first.objects);
        assertEquals(3, first.linkedObjects);
        assertFalse(first.filterDevice);
        DeviceDTO second = page.items.get(1);
        assertEquals(ProjectTestFiles.PRODUCT_TEXT, second.name);
        assertNull(second.room);
        DeviceDTO spare = page.items.get(2);
        assertNull(spare.address);
        assertEquals(0, spare.objects);
        assertEquals(List.of("P-0777-0_DI-2"),
                service().devices("knx:ip:home", "1", "1").items.stream().map(d -> d.id).toList());
    }

    @Test
    void everyCallThatListsDevicesSaysWhichAreFilterDevices() throws Exception {
        store.importProject(RestFixtures.uid("other"), ProjectTestFiles.withFilterDevices(), null, "de-DE",
                "filter.knxproj");
        KnxRestService service = service();

        assertEquals(List.of(false, true, true),
                service.devices("knx:ip:other", null, null).items.stream().map(d -> d.filterDevice).toList());
        assertTrue(service.device("knx:ip:other", "7.7.2").filterDevice);
        assertFalse(service.device("knx:ip:other", "7.7.1").filterDevice);
        TopologyDTO topology = service.topology("knx:ip:other");
        assertEquals(List.of(false, true),
                topology.areas.get(0).lines.get(0).devices.stream().map(d -> d.filterDevice).toList());
        assertEquals(List.of(true), topology.unplacedDevices.stream().map(d -> d.filterDevice).toList());

        assertEquals(List.of(false, false, false),
                service.devices("knx:ip:home", null, null).items.stream().map(d -> d.filterDevice).toList());
        assertFalse(service.device("knx:ip:home", "7.7.2").filterDevice);
    }

    @Test
    void aDeviceIsFoundByAddressOrIdWithItsGroupObjects() {
        DeviceDetailDTO device = service().device("knx:ip:home", "7.7.1");
        assertEquals("P-0777-0_DI-1", device.id);
        assertEquals(3, device.groupObjects.size());
        GroupObjectDTO switching = device.groupObjects.stream()
                .filter(object -> object.groupAddresses.contains("31/7/1")).findFirst().orElseThrow();
        assertEquals(List.of("31/7/1", "31/7/4"), switching.groupAddresses);
        assertEquals("1.001", switching.dataType);
        assertEquals("P-0777-0_DI-2", service().device("knx:ip:home", "P-0777-0_DI-2").id);
        assertEquals("7.7.2", service().device("knx:ip:home", "P-0777-0_DI-2").address);

        KnxRestService service = service();
        for (String unknown : new String[] { "7.7.9", "16.0.1", "7.7", "P-0777-0_DI-99", "", "x".repeat(500) }) {
            assertFails(KnxRestError.UNKNOWN_DEVICE, () -> service.device("knx:ip:home", unknown));
            assertFails(KnxRestError.UNKNOWN_DEVICE, () -> service.deviceChannels("knx:ip:home", unknown));
        }
        assertFails(KnxRestError.UNKNOWN_DEVICE, () -> service.device("knx:ip:home", null));
    }

    @Test
    void theChannelLinesOfADeviceAreThoseOfTheConsole() {
        List<ChannelSuggestionDTO> lines = service().deviceChannels("knx:ip:home", "7.7.1");
        assertEquals(List.of("ga=\"1.001:31/7/1\"", "ga=\"9.001:<31/7/2\"", "upDown=\"1.008:31/7/5\""),
                lines.stream().map(line -> line.text).toList());
        assertEquals(List.of("switch", "number", "rollershutter"),
                lines.stream().map(line -> line.channelType).toList());
        assertEquals("31/7/1", lines.get(0).groupAddress);
        assertEquals("Made-up light", lines.get(0).name);
        assertEquals("ga", lines.get(0).key);
        assertEquals("1.001:31/7/1", lines.get(0).value);
    }

    private static ChannelRequestDTO request(@Nullable String... addresses) {
        ChannelRequestDTO request = new ChannelRequestDTO();
        request.groupAddresses = Arrays.asList(addresses);
        return request;
    }

    @Test
    void channelLinesForAListOfAddressesWithTheUnknownOnes() {
        ChannelSuggestionsDTO answer = service().channels("knx:ip:home", request("31/7/2", "31/7/99", "31/7/1"));
        assertEquals(List.of("31/7/2", "31/7/1"), answer.suggestions.stream().map(s -> s.groupAddress).toList());
        assertEquals(List.of("31/7/99"), answer.unknown);
    }

    @Test
    void aChannelRequestThatCannotBeReadIsBadInput() {
        KnxRestService service = service();
        assertBadInput("groupAddresses", () -> service.channels("knx:ip:home", null));
        assertBadInput("groupAddresses", () -> service.channels("knx:ip:home", new ChannelRequestDTO()));
        assertBadInput("groupAddresses", () -> service.channels("knx:ip:home", request()));
        assertBadInput("groupAddresses", () -> service.channels("knx:ip:home", request("31/7/1", "x")));
        assertBadInput("groupAddresses", () -> service.channels("knx:ip:home", request("31/7/1", null)));
        String[] many = new String[2001];
        Arrays.fill(many, "31/7/1");
        assertBadInput("groupAddresses", () -> service.channels("knx:ip:home", request(many)));
        String[] most = new String[2000];
        Arrays.fill(most, "31/7/99");
        assertEquals(2000, service.channels("knx:ip:home", request(most)).unknown.size());
        assertFalse(assertBadInput("groupAddresses", () -> service.channels("knx:ip:home", request("made-up-text")))
                .text().contains("made-up-text"));
    }

    @Test
    void theTopologyHasAreasLinesAndUnplacedDevices() {
        TopologyDTO topology = service().topology("knx:ip:home");
        AreaDTO area = topology.areas.get(0);
        assertEquals("7", area.address);
        assertEquals("Made-up area", area.name);
        LineDTO line = area.lines.get(0);
        assertEquals("7.7", line.address);
        assertEquals("Made-up line", line.name);
        assertEquals(List.of("7.7.1", "7.7.2"), line.devices.stream().map(d -> d.address).toList());
        assertEquals(List.of("P-0777-0_DI-3"), topology.unplacedDevices.stream().map(d -> d.id).toList());
    }

    @Test
    void theLocationsAreATreeWithFunctions() {
        List<SpaceDTO> locations = service().locations("knx:ip:home");
        SpaceDTO building = locations.get(0);
        assertEquals("Made-up building", building.name);
        assertEquals("Building", building.type);
        SpaceDTO kitchen = building.spaces.get(0);
        assertEquals("Made-up kitchen", kitchen.name);
        assertEquals(List.of("P-0777-0_DI-1"), kitchen.devices);
        FunctionDTO light = kitchen.functions.get(0);
        assertEquals("Made-up kitchen light", light.name);
        assertEquals("31/7/1", light.groupAddresses.get(0).groupAddress);
        assertEquals("Switch", light.groupAddresses.get(0).name);
    }

    @Test
    void theCheckGivesTheDifferencesTheConsolePrints() {
        home = RestFixtures.handler(bridgeWithChannels("home",
                List.of(configured(TEMPERATURE, "5.001", "made-up temperature ga"),
                        configured(LIGHT, "1.002", "made-up light ga"),
                        configured(UNKNOWN_GROUP, "1.001", "made-up missing ga")),
                2), homeMonitor);

        ProjectCheckDTO check = service().check("knx:ip:home");

        assertEquals(2, check.thingsWithoutHandler);
        assertEquals(
                List.of("DATAPOINT_TYPE_DIFFERS 31/7/2 false", "DATAPOINT_TYPE_DIFFERS 31/7/1 true",
                        "ADDRESS_NOT_IN_PROJECT 31/7/99 false"),
                check.differences.stream().map(d -> d.kind + " " + d.address + " " + d.noteOnly).toList());
        DifferenceDTO temperature = check.differences.get(0);
        assertEquals("5.001", temperature.configuredDataType);
        assertEquals("9.001", temperature.projectDataType);
        assertEquals("made-up temperature ga", temperature.label);
    }

    @Test
    void noAnswerHoldsADeviceParameter() {
        homeMonitor.received(SENSOR, LIGHT, TelegramType.WRITE, new byte[] { 1 }, SecurityState.PLAIN);
        KnxRestService service = service();
        StringBuilder json = new StringBuilder();
        for (Object answer : List.of(service.bridges(), service.project("knx:ip:home"),
                service.groupAddresses("knx:ip:home", null, null), service.devices("knx:ip:home", null, null),
                service.device("knx:ip:home", "7.7.1"), service.deviceChannels("knx:ip:home", "7.7.1"),
                service.channels("knx:ip:home", request("31/7/1", "31/7/2", "31/7/5")), service.topology("knx:ip:home"),
                service.locations("knx:ip:home"))) {
            json.append(RestFixtures.GSON.toJson(answer));
        }
        assertTrue(json.toString().contains(ProjectTestFiles.DEVICE_NAME));
        assertFalse(json.toString().contains(ProjectTestFiles.PARAMETER_VALUE));
        assertFalse(json.toString().contains("MADEUP-"));
    }

    @Test
    void noErrorTextHoldsAServerPathOrAnException() throws Exception {
        writes(homeMonitor, 3);
        List<KnxRestException> failures = new ArrayList<>();
        KnxRestService service = service();
        writeStoredFile("broken", "{not json");
        List<Executable> calls = List.of(() -> service.bridge("knx:ip:nobody"),
                () -> service.groupAddresses("knx:ip:other", null, null),
                () -> service.groupAddresses("knx:ip:broken", null, null), () -> service.device("knx:ip:home", "7.7.9"),
                () -> page("abc", null, null), () -> page(null, "x", null),
                () -> page(null, null, null, "from", "nonsense"),
                () -> page(null, null, null, "to", "2026-13-01T00:00"), () -> page(null, null, null, "src", "nope"),
                () -> page(null, null, null, "dst", "x"), () -> service.channels("knx:ip:home", request("x")),
                () -> service.channels("knx:ip:home", null));
        for (Executable call : calls) {
            failures.add(assertThrows(KnxRestException.class, call));
        }
        for (KnxRestError error : KnxRestError.values()) {
            failures.add(new KnxRestException(error));
        }
        String path = folder.toString();
        for (KnxRestException failure : failures) {
            String text = failure.text();
            assertFalse(text.toLowerCase(Locale.ROOT).contains("exception"), text);
            assertFalse(text.contains(path) || text.contains("/knx/") || text.contains("userdata"), text);
            assertFalse(text.contains("java."), text);
        }
    }

    // number 18

    @Test
    void removeDeletesTheStoredProjectAlsoOfABridgeWithoutHandler() throws Exception {
        store.importProject(RestFixtures.uid("gone"), ProjectTestFiles.plain("Made-up Gone"), null, null,
                "gone.knxproj");

        service().removeProject("knx:ip:home");
        assertEquals(KnxProjectStore.FileState.NONE, store.fileState(RestFixtures.uid("home")));
        service().removeProject("knx:ip:gone");
        assertEquals(List.of(), store.stored());

        assertFails(KnxRestError.NO_PROJECT, () -> service().removeProject("knx:ip:home"));
        assertFails(KnxRestError.NO_PROJECT, () -> service().removeProject("knx:ip:never"));
        assertFails(KnxRestError.UNKNOWN_BRIDGE, () -> service().removeProject("hue:bridge:1"));
        assertFails(KnxRestError.UNKNOWN_BRIDGE, () -> service().removeProject(null));
    }

    @Test
    void aRemoveThatCannotDeleteIsAStoreFailure() throws Exception {
        KnxProjectStore failing = mock(KnxProjectStore.class);
        when(failing.remove(any())).thenThrow(new IOException("made-up failure in " + folder));

        assertFails(KnxRestError.STORE_FAILED,
                () -> new KnxRestService(RestFixtures.factory(home), failing, mock(ThingRegistry.class),
                        mock(ItemRegistry.class), mock(ItemChannelLinkRegistry.class)).removeProject("knx:ip:home"));
    }
}
