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

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BooleanSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openhab.binding.knx.internal.client.FeedLink;
import org.openhab.binding.knx.internal.client.MonitorTestClient;
import org.openhab.binding.knx.internal.handler.IPBridgeThingHandler;
import org.openhab.binding.knx.internal.monitor.SecurityState;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.monitor.TelegramRecord;
import org.openhab.binding.knx.internal.monitor.TelegramType;
import org.openhab.binding.knx.internal.project.ProjectTestFiles;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore;
import org.openhab.binding.knx.internal.projectstore.LogCapture;
import org.openhab.binding.knx.internal.rest.dto.BridgeDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupTelegramDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupTelegramRequestDTO;
import org.openhab.core.auth.Role;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.util.HexUtils;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;
import io.calimero.KNXTimeoutException;
import io.calimero.datapoint.StateDP;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;

/**
 * Call 24, the Group Monitor's read and write, on a real client over a stand-in link that records what goes out.
 * Every refusal is checked to send nothing. All data is made up.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxRestGroupTelegramTest {
    private static final String HOME = "knx:ip:home";
    private static final String MARKER = "MadeUpMarker";

    /** A clock the test moves by hand. */
    private static final class HandClock extends Clock {
        long millis = 1_000_000;

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(@Nullable ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(millis);
        }
    }

    private @TempDir Path folder = Path.of("");
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final HandClock clock = new HandClock();
    private final TelegramMonitor monitor = new TelegramMonitor();
    private final FeedLink link = new FeedLink(new IndividualAddress(1, 1, 250));
    private final MonitorTestClient client = new MonitorTestClient(scheduler, monitor, () -> link, 3);
    private KnxProjectStore store = new KnxProjectStore(Path.of("target", "unused"), Clock.systemUTC());

    @BeforeEach
    void setUp() {
        store = new KnxProjectStore(folder, Clock.systemUTC());
        client.initialize();
        assertTrue(client.isConnected());
    }

    @AfterEach
    void tearDown() {
        client.dispose();
        scheduler.shutdownNow();
    }

    private IPBridgeThingHandler handler(String id, Map<String, Object> configuration, ThingStatus status) {
        Bridge bridge = RestFixtures.bridgeThing(id, configuration);
        bridge.setStatusInfo(new ThingStatusInfo(status, ThingStatusDetail.NONE, null));
        IPBridgeThingHandler handler = RestFixtures.handler(bridge, monitor);
        when(handler.client()).thenReturn(client);
        return handler;
    }

    private KnxRestService service(Map<String, Object> configuration, ThingStatus status) {
        return new KnxRestService(
                RestFixtures.factory(handler("home", configuration, status),
                        handler("other", Map.of("groupMonitorSend", true), ThingStatus.ONLINE)),
                store, mock(ThingRegistry.class), mock(ItemRegistry.class), mock(ItemChannelLinkRegistry.class), clock,
                ZoneOffset.UTC);
    }

    private KnxRestService allowed() {
        return service(Map.of("groupMonitorSend", true), ThingStatus.ONLINE);
    }

    private static GroupTelegramRequestDTO request(@Nullable String address, @Nullable String action,
            @Nullable String dpt, @Nullable String value) {
        GroupTelegramRequestDTO request = new GroupTelegramRequestDTO();
        request.address = address;
        request.action = action;
        request.dpt = dpt;
        request.value = value;
        return request;
    }

    private static GroupTelegramRequestDTO write(String address, String dpt, String value) {
        return request(address, "write", dpt, value);
    }

    private static GroupTelegramRequestDTO read(String address) {
        return request(address, "read", null, null);
    }

    private List<String> sent() {
        return link.sent().stream().map(frame -> HexUtils.bytesToHex(frame, " ")).toList();
    }

    private static void waitFor(BooleanSupplier condition) throws InterruptedException {
        long end = System.nanoTime() + 10_000_000_000L;
        while (!condition.getAsBoolean()) {
            assertTrue(System.nanoTime() < end, "condition not met within 10 seconds");
            Thread.sleep(5);
        }
    }

    /** The call is refused with this error, its answer holds only the error's fixed text, and nothing went out. */
    private void assertRefused(KnxRestError error, KnxRestService service, @Nullable GroupTelegramRequestDTO request) {
        KnxRestException e = assertThrows(KnxRestException.class, () -> service.groupTelegram(HOME, request));
        assertEquals(error.code(), e.code());
        assertEquals(error.status(), e.status());
        assertFalse(e.text().contains(MARKER), e.text());
        assertEquals(List.of(), sent());
        assertEquals(List.of(), monitor.records());
    }

    // what goes out

    @Test
    void aWriteOfASwitchGoesOutAsOneGroupWrite() {
        GroupTelegramDTO answer = allowed().groupTelegram(HOME, write("31/0/1", "1.001", "ON"));

        assertEquals("write", answer.action);
        assertEquals("sent", answer.state);
        assertEquals(List.of("00 81"), sent());
        TelegramRecord row = monitor.records().get(0);
        assertEquals(TelegramType.WRITE, row.type());
        assertEquals("31/0/1", row.destination().toString());
        assertEquals("1.001", row.dataType());

        allowed().groupTelegram(HOME, write("31/0/1", "1.001", "OFF"));
        assertEquals(List.of("00 81", "00 80"), sent());
    }

    @Test
    void aWriteOfAPercentValueAndOfATemperatureCarryTheRightBytes() {
        KnxRestService service = allowed();

        service.groupTelegram(HOME, write("31/0/2", "5.001", "50"));
        service.groupTelegram(HOME, write("31/0/3", "9.001", "21.5"));
        service.groupTelegram(HOME, write("31/0/3", "9.001", "21.5 °C"));
        service.groupTelegram(HOME, write("31/0/2", "5.001", "100 %"));

        assertEquals(List.of("00 80 80", "00 80 0C 33", "00 80 0C 33", "00 80 FF"), sent());
    }

    @Test
    void aValueIsReadAsAChannelReadsACommandOfThatType() {
        KnxRestService service = allowed();

        service.groupTelegram(HOME, write("31/0/4", "1.008", "DOWN"));
        service.groupTelegram(HOME, write("31/0/4", "1.008", "UP"));
        service.groupTelegram(HOME, write("31/0/4", "1.008", "ON"));
        service.groupTelegram(HOME, write("31/0/5", "5.010", "7"));
        service.groupTelegram(HOME, write("31/0/6", "16.001", "Hi"));

        assertEquals(List.of("00 81", "00 80", "00 81", "00 80 07"), sent().subList(0, 4));
        assertTrue(sent().get(4).startsWith("00 80 48 69 00"), sent().get(4));
    }

    @Test
    void timesStepsAndColoursAreReadToo() {
        KnxRestService service = allowed();

        service.groupTelegram(HOME, write("31/0/7", "10.001", "12:34:56"));
        service.groupTelegram(HOME, write("31/0/8", "3.007", "INCREASE"));
        service.groupTelegram(HOME, write("31/0/9", "232.600", "0,100,100"));
        service.groupTelegram(HOME, write("31/0/10", "17.001", "5"));

        assertEquals(List.of("00 80 8C 22 38", "00 8D", "00 80 FF 00 00", "00 80 05"), sent());
    }

    @Test
    void aReadGoesOutAsOneGroupReadAndIsNotSentAgainWithoutAnAnswer() throws Exception {
        GroupTelegramDTO answer = allowed().groupTelegram(HOME, read("31/0/1"));

        assertEquals("read", answer.action);
        assertEquals("queued", answer.state);
        waitFor(() -> monitor.records().size() == 1 && monitor.records().get(0).outcome() != null
                && !"PENDING".equals(String.valueOf(monitor.records().get(0).outcome())));
        // the bridge sends a channel's read three times without an answer; this one must go out once
        client.readOnce(new StateDP(new GroupAddress(31, 0, 9), "", 0, "1.001"));
        waitFor(() -> link.sent().size() == 2);
        assertEquals(List.of("00 00", "00 00"), sent());
        assertEquals(TelegramType.READ, monitor.records().get(0).type());
        assertEquals("31/0/1", monitor.records().get(0).destination().toString());
        assertEquals(1,
                monitor.records().stream().filter(row -> "31/0/1".equals(row.destination().toString())).count());
    }

    @Test
    void aChannelsReadKeepsItsTriesWhileThePagesReadOfTheAddressWaits() throws Exception {
        KnxRestService service = allowed();
        try (LogCapture log = new LogCapture()) {
            // keeps the reader busy with an answer that does not come, so that the next read waits
            service.groupTelegram(HOME, read("31/0/8"));
            waitFor(() -> link.sent().size() == 1);
            service.groupTelegram(HOME, read("31/0/9"));
            client.readDatapoint(new StateDP(new GroupAddress(31, 0, 9), "", 0, "1.001"));

            waitFor(() -> log.warnings().stream()
                    .anyMatch(line -> line.startsWith("Giving up reading datapoint 31/0/9")));
        }
        assertEquals(3,
                monitor.records().stream().filter(row -> "31/0/9".equals(row.destination().toString())).count());
        assertEquals(4, link.sent().size());
    }

    @Test
    void aReadWithoutTypeTakesTheTypeOfTheProject() throws Exception {
        store.importProject(RestFixtures.uid("home"), ProjectTestFiles.plain(), null, "de-DE", "house.knxproj");

        allowed().groupTelegram(HOME, read("31/7/2"));

        waitFor(() -> monitor.records().size() == 1);
        assertEquals("9.001", monitor.records().get(0).dataType());
        waitFor(() -> link.sent().size() == 1);
        assertEquals(List.of("00 00"), sent());
    }

    @Test
    void aSecuredAddressIsSentSecured() {
        allowed().groupTelegram(HOME, write("8/0/0", "1.001", "ON"));

        assertEquals(1, link.sent().size());
        assertTrue(sent().get(0).startsWith("03 F1"), sent().get(0));
        assertEquals(SecurityState.SECURED, monitor.records().get(0).security());
    }

    // refusals: nothing goes out

    @Test
    void theSettingIsOffByDefaultAndTheCallThenAnswers403AndSendsNothing() {
        assertRefused(KnxRestError.SEND_NOT_ALLOWED, service(Map.of(), ThingStatus.ONLINE),
                write("31/0/1", "1.001", "ON"));
        assertRefused(KnxRestError.SEND_NOT_ALLOWED, service(Map.of("groupMonitorSend", false), ThingStatus.ONLINE),
                read("31/0/1"));
        assertEquals(403, KnxRestError.SEND_NOT_ALLOWED.status());
        // also before the request is looked at
        assertRefused(KnxRestError.SEND_NOT_ALLOWED, service(Map.of(), ThingStatus.ONLINE), null);
        assertRefused(KnxRestError.SEND_NOT_ALLOWED, service(Map.of(), ThingStatus.ONLINE),
                write(MARKER, MARKER, MARKER));
    }

    @Test
    void aBadAddressSendsNothing() {
        for (String address : List.of("", "0/0/0", "32/0/1", "31/8/1", "31/0/256", "1.1.1", "31/0/1 ", "a/b/c", "31/0",
                "63489", "1", "31/0/1/1", "31//1", MARKER)) {
            assertRefused(KnxRestError.BAD_GROUP_ADDRESS, allowed(), write(address, "1.001", "ON"));
            assertRefused(KnxRestError.BAD_GROUP_ADDRESS, allowed(), read(address));
        }
        assertRefused(KnxRestError.BAD_GROUP_ADDRESS, allowed(), request(null, "read", null, null));
    }

    @Test
    void anUnknownTypeSendsNothing() {
        for (String dpt : List.of("1", "1.1", "99.001", "1.999", "0.000", "x", MARKER)) {
            assertRefused(KnxRestError.UNKNOWN_DATA_TYPE, allowed(), write("31/0/1", dpt, "ON"));
            assertRefused(KnxRestError.UNKNOWN_DATA_TYPE, allowed(), request("31/0/1", "read", dpt, null));
        }
        assertRefused(KnxRestError.UNKNOWN_DATA_TYPE, allowed(), request("31/0/1", "write", null, "ON"));
        assertRefused(KnxRestError.UNKNOWN_DATA_TYPE, allowed(), request("31/0/1", "write", "", "ON"));
    }

    @Test
    void aValueTheTypeCannotTakeSendsNothing() {
        assertRefused(KnxRestError.BAD_VALUE, allowed(), write("31/0/1", "1.001", "maybe"));
        assertRefused(KnxRestError.BAD_VALUE, allowed(), write("31/0/1", "1.001", "UP"));
        assertRefused(KnxRestError.BAD_VALUE, allowed(), write("31/0/1", "1.001", ""));
        assertRefused(KnxRestError.BAD_VALUE, allowed(), request("31/0/1", "write", "1.001", null));
        assertRefused(KnxRestError.BAD_VALUE, allowed(), write("31/0/2", "5.001", "ON"));
        assertRefused(KnxRestError.BAD_VALUE, allowed(), write("31/0/3", "9.001", "warm"));
        assertRefused(KnxRestError.BAD_VALUE, allowed(), write("31/0/3", "9.001", "21 " + MARKER));
        assertRefused(KnxRestError.BAD_VALUE, allowed(), write("31/0/3", "9.001", "9".repeat(201)));
    }

    @Test
    void aValueOutOfTheRangeOfTheTypeIsRefusedBeforeAnythingIsSentCountedOrLogged() throws Exception {
        String tooLong = MARKER + "IsTooLong";
        List<List<String>> cases = List.of(List.of("5.010", "300"), List.of("5.001", "150"), List.of("5.001", "-1"),
                List.of("7.001", "70000"), List.of("9.001", "700000"), List.of("14.056", "1e40"), List.of("6.020", "x"),
                List.of("16.001", tooLong), List.of("17.001", "99"), List.of("18.001", "200"));
        try (LogCapture log = new LogCapture()) {
            KnxRestService service = allowed();
            for (int round = 0; round < 2; round++) {
                for (List<String> each : cases) {
                    assertRefused(KnxRestError.BAD_VALUE, service, write("31/0/2", each.get(0), each.get(1)));
                }
            }
            assertEquals(400, KnxRestError.BAD_VALUE.status());
            assertEquals(List.of(), log.warnings());
            log.all().forEach(line -> assertFalse(line.contains(MARKER) || line.startsWith("Value '"), line));

            // twenty refusals later the bridge's five telegrams are all still there
            for (int i = 0; i < GroupTelegramSender.LIMIT; i++) {
                service.groupTelegram(HOME, write("31/0/2", "5.001", "100"));
            }
            assertEquals(GroupTelegramSender.LIMIT, link.sent().size());
        }
    }

    @Test
    void aValueWithAUnitOfAnotherKindIsRefusedAndNotSentWithoutItsUnit() throws Exception {
        try (LogCapture log = new LogCapture()) {
            assertRefused(KnxRestError.BAD_VALUE, allowed(), write("31/0/2", "5.001", "50 °C"));
            assertRefused(KnxRestError.BAD_VALUE, allowed(), write("31/0/3", "9.001", "21.5 W"));
            assertRefused(KnxRestError.BAD_VALUE, allowed(), write("31/0/3", "9.001", "21.5 %"));
            assertEquals(List.of(), log.warnings());
        }

        // a unit of the same kind is converted, a temperature difference keeps the unit it was given in
        KnxRestService service = allowed();
        service.groupTelegram(HOME, write("31/0/3", "9.001", "294.65 K"));
        service.groupTelegram(HOME, write("31/0/4", "9.002", "2 °C"));
        service.groupTelegram(HOME, write("31/0/4", "9.002", "2 K"));
        assertEquals(List.of("00 80 0C 33", "00 80 00 C8", "00 80 00 C8"), sent());
    }

    @Test
    void aUnitThatOnlyHoldsFahrenheitIsRefusedForATemperatureDifference() throws Exception {
        try (LogCapture log = new LogCapture()) {
            assertRefused(KnxRestError.BAD_VALUE, allowed(), write("31/0/4", "9.002", "5 W/°F"));
            assertRefused(KnxRestError.BAD_VALUE, allowed(), write("31/0/4", "9.002", "5 °F/h"));
            assertRefused(KnxRestError.BAD_VALUE, allowed(), write("31/0/5", "9.023", "5 W/°F"));
            assertRefused(KnxRestError.BAD_VALUE, allowed(), write("31/0/5", "9.023", "5 °F"));
            assertRefused(KnxRestError.BAD_VALUE, allowed(), write("31/0/6", "9.003", "5 W/°F"));
            assertRefused(KnxRestError.BAD_VALUE, allowed(), write("31/0/6", "9.003", "5 °F"));
            // the encoder cannot bring a gradient in °F to K/h and would send the number as it is
            assertRefused(KnxRestError.BAD_VALUE, allowed(), write("31/0/6", "9.003", "5 °F/h"));
            assertEquals(List.of(), log.warnings());
        }

        KnxRestService service = allowed();
        service.groupTelegram(HOME, write("31/0/4", "9.002", "5 °F"));
        service.groupTelegram(HOME, write("31/0/5", "9.023", "5 °F/%"));
        service.groupTelegram(HOME, write("31/0/6", "9.003", "5 K/h"));
        assertEquals(List.of("00 80 01 16", "00 80 01 16", "00 80 01 F4"), sent());
    }

    /** Each case is refused as a bad value, twice, without a WARN, and the bridge's five telegrams are still there. */
    private void assertAllRefusedAsBadValue(List<List<String>> cases) throws Exception {
        try (LogCapture log = new LogCapture()) {
            KnxRestService service = allowed();
            for (int round = 0; round < 2; round++) {
                for (List<String> each : cases) {
                    KnxRestException e = assertThrows(KnxRestException.class,
                            () -> service.groupTelegram(HOME, write("31/0/2", each.get(0), each.get(1))),
                            () -> each + " went out as " + sent());
                    assertEquals("bad-value", e.code(), each.toString());
                    assertEquals(400, e.status(), each.toString());
                    assertEquals(List.of(), sent(), each.toString());
                    assertEquals(List.of(), monitor.records(), each.toString());
                }
            }
            assertEquals(List.of(), log.warnings());

            for (int i = 0; i < GroupTelegramSender.LIMIT; i++) {
                service.groupTelegram(HOME, write("31/0/2", "5.001", "100"));
            }
            assertEquals(GroupTelegramSender.LIMIT, link.sent().size());
        }
    }

    @Test
    void aNumberThatIsNoValueOfTheTypeIsRefusedAndNotSentAsAnotherValue() throws Exception {
        assertAllRefusedAsBadValue(List.of(List.of("2.001", "99"), List.of("2.001", "4"), List.of("2.001", "-1"),
                List.of("2.002", "250"), List.of("2.001", "1.9"), List.of("2.001", "4294967297"),
                List.of("2.001", "1e2000000"), List.of("1.022", "7"), List.of("1.022", "-3"), List.of("1.022", "0.5"),
                List.of("18.001", "4294967296"), List.of("18.001", "1e200000"), List.of("18.001", "4294967301"),
                List.of("18.001", "64"), List.of("18.001", "127"), List.of("18.001", "192"), List.of("18.001", "-1"),
                List.of("18.001", "5.5")));
    }

    @Test
    void theNumbersOfAControlledBitASceneChoiceAndASceneControlStillGoOut() {
        KnxRestService service = allowed();
        for (String value : List.of("0", "1", "2", "3")) {
            service.groupTelegram(HOME, write("31/0/2", "2.001", value));
        }
        assertEquals(List.of("00 80", "00 81", "00 82", "00 83"), sent());

        clock.millis += GroupTelegramSender.WINDOW_MILLIS;
        for (String value : List.of("0", "63", "128", "191")) {
            service.groupTelegram(HOME, write("31/0/2", "18.001", value));
        }
        assertEquals(List.of("00 80 00", "00 80 3F", "00 80 80", "00 80 BF"), sent().subList(4, 8));

        clock.millis += GroupTelegramSender.WINDOW_MILLIS;
        service.groupTelegram(HOME, write("31/0/2", "1.022", "0"));
        service.groupTelegram(HOME, write("31/0/2", "1.022", "1"));
        assertEquals(List.of("00 80", "00 81"), sent().subList(8, 10));
    }

    @Test
    void aUnitOnATypeThatHasNoUnitIsRefusedAndTheNumberIsNotSentWithoutIt() throws Exception {
        assertAllRefusedAsBadValue(List.of(List.of("12.001", "5 kW"), List.of("12.001", "5 kWh"),
                List.of("12.001", "5 %"), List.of("5.010", "5 W"), List.of("5.010", "5 %"), List.of("5.005", "5 W"),
                List.of("7.001", "5 km"), List.of("8.001", "5 W"), List.of("13.001", "5 °C"),
                List.of("6.010", "-5 W")));
    }

    @Test
    void aPlainNumberOnATypeThatHasNoUnitStillGoesOut() {
        KnxRestService service = allowed();

        service.groupTelegram(HOME, write("31/0/2", "12.001", "5"));
        service.groupTelegram(HOME, write("31/0/2", "5.010", "5"));

        assertEquals(List.of("00 80 00 00 00 05", "00 80 05"), sent());
    }

    @Test
    void aUnitTheUnitLibraryCannotWorkWithIsABadValueAndNoInternalError() throws Exception {
        assertAllRefusedAsBadValue(
                List.of(List.of("9.001", "5 °C/%"), List.of("9.023", "5 m°C"), List.of("9.023", "5 °C"),
                        List.of("9.002", "5 °C/%"), List.of("14.068", "5 °C/%"), List.of("14.069", "5 °C/%"),
                        List.of("7.600", "5 °C/%"), List.of("9.001", "5 °F/%"), List.of("14.070", "5 °F/%")));
    }

    @Test
    void aNumberTooLongOrWithAHugePowerOfTenIsRefusedBeforeItIsWrittenOut() throws Exception {
        String longFive = "5." + "0".repeat(39);
        assertEquals(41, longFive.length());
        List<List<String>> cases = List.of(List.of("14.056", "1e999999999"), List.of("14.056", "1e-999999999"),
                List.of("5.010", "1e999999999"), List.of("14.056", "1e999999999 W"), List.of("14.056", "1E999999999"),
                List.of("5.010", "0e401"), List.of("5.010", "0e-401"), List.of("5.010", longFive),
                List.of("14.056", longFive + " W"), List.of("5.010", "0e99999999999999999999"));
        assertTimeoutPreemptively(Duration.ofSeconds(20), () -> assertAllRefusedAsBadValue(cases));

        // the longest and the widest that are taken
        KnxRestService service = allowed();
        clock.millis += GroupTelegramSender.WINDOW_MILLIS;
        service.groupTelegram(HOME, write("31/0/2", "5.010", "5." + "0".repeat(38)));
        service.groupTelegram(HOME, write("31/0/2", "5.010", "0e400"));
        service.groupTelegram(HOME, write("31/0/2", "14.056", "1e3 W"));
        assertEquals(List.of("00 80 05", "00 80 00", "00 80 44 7A 00 00"), sent().subList(5, 8));
    }

    @Test
    void aCommaInANumberIsRefusedAndNotReadAsAThousandsMark() {
        assertRefused(KnxRestError.BAD_VALUE, allowed(), write("31/0/3", "9.001", "21,5"));
        assertRefused(KnxRestError.BAD_VALUE, allowed(), write("31/0/3", "9.001", "21,5 °C"));
        assertRefused(KnxRestError.BAD_VALUE, allowed(), write("31/0/2", "5.010", "1,000"));

        allowed().groupTelegram(HOME, write("31/0/9", "232.600", "0,100,100"));
        assertEquals(List.of("00 80 FF 00 00"), sent());
    }

    @Test
    void anActionThatIsNeitherReadNorWriteSendsNothing() {
        for (String action : List.of("", "WRITE", "respond", MARKER)) {
            assertRefused(KnxRestError.BAD_INPUT, allowed(), request("31/0/1", action, "1.001", "ON"));
        }
        assertRefused(KnxRestError.BAD_INPUT, allowed(), request("31/0/1", null, "1.001", "ON"));
        assertRefused(KnxRestError.BAD_INPUT, allowed(), null);
    }

    @Test
    void aBridgeThatIsNotOnlineSendsNothing() {
        assertRefused(KnxRestError.BRIDGE_OFFLINE, service(Map.of("groupMonitorSend", true), ThingStatus.OFFLINE),
                write("31/0/1", "1.001", "ON"));
        assertRefused(KnxRestError.BRIDGE_OFFLINE, service(Map.of("groupMonitorSend", true), ThingStatus.UNINITIALIZED),
                read("31/0/1"));
        link.open = false;
        assertRefused(KnxRestError.BRIDGE_OFFLINE, allowed(), write("31/0/1", "1.001", "ON"));
    }

    @Test
    void aClientThatLostItsLinkBetweenTheCheckAndTheWriteAnswersOfflineAndLogsNoSentLine() throws Exception {
        // never connected, so without communicator and link, but says it is: the moment after the check
        MonitorTestClient gone = new MonitorTestClient(scheduler, monitor, () -> link, 3) {
            @Override
            public boolean isConnected() {
                return true;
            }
        };
        IPBridgeThingHandler home = handler("home", Map.of("groupMonitorSend", true), ThingStatus.ONLINE);
        when(home.client()).thenReturn(gone);
        KnxRestService service = new KnxRestService(RestFixtures.factory(home), store, mock(ThingRegistry.class),
                mock(ItemRegistry.class), mock(ItemChannelLinkRegistry.class), clock, ZoneOffset.UTC);

        try (LogCapture log = new LogCapture()) {
            assertRefused(KnxRestError.BRIDGE_OFFLINE, service, write("31/0/1", "1.001", "ON"));
            assertEquals(List.of(), log.all().stream().filter(line -> line.startsWith("KNX page:")).toList());
        }
    }

    @Test
    void anUnknownBridgeSendsNothing() {
        KnxRestException e = assertThrows(KnxRestException.class,
                () -> allowed().groupTelegram("knx:ip:nowhere", write("31/0/1", "1.001", "ON")));
        assertEquals("unknown-bridge", e.code());
        assertEquals(List.of(), sent());
    }

    @Test
    void aWriteTheInterfaceDoesNotConfirmIsAnsweredAsFailed() {
        for (int i = 0; i < 3; i++) {
            link.failNextSend(new KNXTimeoutException("made-up missing confirmation"));
        }

        KnxRestException e = assertThrows(KnxRestException.class,
                () -> allowed().groupTelegram(HOME, write("31/0/1", "1.001", "ON")));

        assertEquals("send-failed", e.code());
        assertEquals(List.of(), sent());
    }

    // the limit

    @Test
    void atMostFiveTelegramsGoOutInAnyTenSecondsPerBridge() {
        KnxRestService service = allowed();
        for (int i = 0; i < 5; i++) {
            service.groupTelegram(HOME, write("31/0/1", "1.001", "ON"));
            clock.millis += 1000;
        }
        assertEquals(5, link.sent().size());

        for (int i = 0; i < 3; i++) {
            KnxRestException e = assertThrows(KnxRestException.class,
                    () -> service.groupTelegram(HOME, write("31/0/1", "1.001", "ON")));
            assertEquals("too-many-telegrams", e.code());
            assertEquals(429, e.status());
        }
        assertThrows(KnxRestException.class, () -> service.groupTelegram(HOME, read("31/0/1")));
        assertEquals(5, link.sent().size());

        // the other bridge has its own count
        service.groupTelegram("knx:ip:other", write("31/0/1", "1.001", "ON"));
        assertEquals(6, link.sent().size());

        // ten seconds after the first one, one more may go; a refused call did not count
        clock.millis += 4999;
        assertThrows(KnxRestException.class, () -> service.groupTelegram(HOME, write("31/0/1", "1.001", "ON")));
        clock.millis += 1;
        service.groupTelegram(HOME, write("31/0/1", "1.001", "ON"));
        assertThrows(KnxRestException.class, () -> service.groupTelegram(HOME, write("31/0/1", "1.001", "ON")));
        assertEquals(7, link.sent().size());
    }

    @Test
    void aRefusedRequestDoesNotCountForTheLimit() {
        KnxRestService service = allowed();
        for (int i = 0; i < 20; i++) {
            assertThrows(KnxRestException.class, () -> service.groupTelegram(HOME, write("31/0/1", "1.001", "x")));
        }
        for (int i = 0; i < 5; i++) {
            service.groupTelegram(HOME, write("31/0/1", "1.001", "ON"));
        }
        assertEquals(5, link.sent().size());
    }

    // the log, the login rule, the bridge answer

    @Test
    void oneInfoLinePerTelegramNamesBridgeActionAndAddressAndNeverTheValue() throws Exception {
        try (LogCapture log = new LogCapture()) {
            KnxRestService service = allowed();
            service.groupTelegram(HOME, write("31/0/6", "16.001", MARKER));
            service.groupTelegram(HOME, write("31/0/3", "9.001", "23.75"));
            service.groupTelegram(HOME, read("31/0/1"));
            assertThrows(KnxRestException.class, () -> service.groupTelegram(HOME, write("31/0/1", "1.001", MARKER)));

            List<String> lines = log.all().stream().filter(line -> line.startsWith("KNX page:")).toList();
            assertEquals(List.of("KNX page: group write to 31/0/6 sent through knx:ip:home",
                    "KNX page: group write to 31/0/3 sent through knx:ip:home",
                    "KNX page: group read of 31/0/1 queued for knx:ip:home"), lines);
            lines.forEach(line -> assertFalse(line.contains(MARKER) || line.contains("23.75"), line));
            log.warnings().forEach(line -> assertFalse(line.contains(MARKER) || line.contains("23.75"), line));
        }
    }

    @Test
    void theCallIsAPostWithAJsonBodyForAdminsOnly() throws Exception {
        Method method = KnxResource.class.getMethod("groupTelegram", String.class, GroupTelegramRequestDTO.class);

        assertNotNull(method.getAnnotation(POST.class));
        jakarta.ws.rs.Path path = method.getAnnotation(jakarta.ws.rs.Path.class);
        assertNotNull(path);
        assertEquals("bridges/{b}/group-telegram", path.value());
        RolesAllowed roles = method.getAnnotation(RolesAllowed.class);
        assertNotNull(roles);
        assertArrayEquals(new String[] { Role.ADMIN }, roles.value());
        Consumes consumes = method.getAnnotation(Consumes.class);
        assertNotNull(consumes);
        assertArrayEquals(new String[] { "application/json" }, consumes.value());
    }

    @Test
    void everyBridgeSaysWhetherSendingIsAllowed() {
        KnxRestService service = service(Map.of(), ThingStatus.ONLINE);

        List<BridgeDTO> bridges = service.bridges();

        assertEquals(List.of("knx:ip:home false", "knx:ip:other true"),
                bridges.stream().map(bridge -> bridge.uid + " " + bridge.sendAllowed).toList());
        assertFalse(service.bridge(HOME).sendAllowed);
        assertTrue(allowed().bridge(HOME).sendAllowed);
        assertTrue(RestFixtures.GSON.toJson(service.bridge(HOME)).contains("\"sendAllowed\":false"));
    }
}
