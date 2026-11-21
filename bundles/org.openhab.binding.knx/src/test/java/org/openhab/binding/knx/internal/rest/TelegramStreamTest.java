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

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.LongStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openhab.binding.knx.internal.factory.KNXHandlerFactory;
import org.openhab.binding.knx.internal.handler.IPBridgeThingHandler;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.monitor.OwnSend;
import org.openhab.binding.knx.internal.monitor.SecurityState;
import org.openhab.binding.knx.internal.monitor.SendOutcome;
import org.openhab.binding.knx.internal.monitor.TelegramFilter;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.monitor.TelegramRecord;
import org.openhab.binding.knx.internal.monitor.TelegramType;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * Checks the live telegram stream on a recording client and a timer the test moves on: what a client gets, in
 * which order, when it is dropped, and that the bus threads only signal.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class TelegramStreamTest {
    private static final String BRIDGE = "knx:ip:home";
    private static final IndividualAddress SENSOR = new IndividualAddress(7, 7, 20);
    private static final IndividualAddress OWN = new IndividualAddress(7, 7, 250);
    private static final GroupAddress LIGHT = new GroupAddress(31, 7, 1);
    private static final GroupAddress TEMPERATURE = new GroupAddress(31, 7, 2);

    private @TempDir Path folder = Path.of("");
    private TelegramMonitor monitor = new TelegramMonitor();
    private KNXHandlerFactory factory = mock(KNXHandlerFactory.class);
    private ManualTimer timer = new ManualTimer();
    private TelegramStream stream = new TelegramStream(service(), timer);

    @BeforeEach
    void setUp() {
        monitor = new TelegramMonitor();
        factory = mock(KNXHandlerFactory.class);
        useHandlers(RestFixtures.handler("home", monitor));
        timer = new ManualTimer();
        stream = new TelegramStream(service(), timer);
    }

    private void useHandlers(KNXBridgeBaseThingHandler... handlers) {
        when(factory.getBridges()).thenReturn(List.of(handlers));
    }

    private KnxRestService service() {
        return new KnxRestService(factory, new KnxProjectStore(folder, Clock.systemUTC()), mock(ThingRegistry.class),
                mock(ItemRegistry.class), mock(ItemChannelLinkRegistry.class));
    }

    private static Map<String, @Nullable String> filter(String... keysAndValues) {
        Map<String, @Nullable String> filter = new HashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            filter.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return filter;
    }

    private RecordingClient open(@Nullable String after, @Nullable String monitorId, String... filter) {
        RecordingClient client = new RecordingClient();
        stream.open(client, BRIDGE, filter(filter), after, monitorId);
        return client;
    }

    private RecordingClient open() {
        return open(null, null);
    }

    private void receive(int rows, GroupAddress address) {
        for (int i = 0; i < rows; i++) {
            monitor.received(SENSOR, address, TelegramType.WRITE, new byte[] { 1 }, SecurityState.PLAIN);
        }
    }

    /**
     * Runs the passes the stream schedules, for at most five seconds, finishing the given clients' sends in between.
     */
    private void runPasses(RecordingClient... finishing) {
        for (int i = 0; i < 25 && timer.waitingOnce() > 0; i++) {
            for (RecordingClient client : finishing) {
                client.finishAll();
            }
            timer.advance(Objects.requireNonNull(timer.nextOnceIn().orElse(0L)));
        }
    }

    private static int listeners(TelegramMonitor monitor) throws ReflectiveOperationException {
        Field field = TelegramMonitor.class.getDeclaredField("listeners");
        field.setAccessible(true);
        return ((List<?>) Objects.requireNonNull(field.get(monitor))).size();
    }

    private static void assertEnd(RecordingClient client, String code) {
        assertEquals("end", client.last().name());
        assertEquals(code, client.last().data().get("code").getAsString());
        assertNotNull(client.last().data().get("text"));
        assertTrue(client.closed);
    }

    @Test
    void openComesFirstAndRowsFollowInNumberOrderOnlyWhenTheTimerRuns() {
        receive(3, LIGHT);
        RecordingClient client = open();

        assertEquals(List.of("open"), client.names());
        assertEquals(3, client.events.get(0).number("lastNumber"));
        assertEquals(monitor.id(), client.events.get(0).data().get("monitorId").getAsString());
        assertNull(client.events.get(0).data().get("reset"));

        receive(4, LIGHT);
        // the bus thread only signalled: nothing was sent and one pass waits in the timer
        assertEquals(List.of("open"), client.names());
        assertEquals(1, timer.waitingOnce());

        timer.runDue();
        assertEquals(List.of(4L, 5L, 6L, 7L), client.telegramNumbers());
        assertEquals("31/7/1", client.named("telegram").get(0).data().get("destination").getAsString());
        assertEquals(0, timer.waitingOnce());
    }

    @Test
    void passesComeNotSoonerThan200MillisecondsApart() {
        RecordingClient client = open();
        receive(1, LIGHT);
        timer.runDue();
        assertEquals(List.of(1L), client.telegramNumbers());

        receive(1, LIGHT);
        timer.advance(199);
        assertEquals(List.of(1L), client.telegramNumbers());
        timer.advance(1);
        assertEquals(List.of(1L, 2L), client.telegramNumbers());
    }

    @Test
    void aFilterPassesOnlyMatchingRows() {
        RecordingClient light = open(null, null, "dst", "31/7/1");
        RecordingClient both = open();
        receive(2, LIGHT);
        receive(2, TEMPERATURE);
        receive(1, LIGHT);

        timer.runDue();

        assertEquals(List.of(1L, 2L, 5L), light.telegramNumbers());
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), both.telegramNumbers());
    }

    @Test
    void afterReplaysTheRowsStillInTheBufferWithoutAHoleOrADouble() {
        receive(10, LIGHT);
        RecordingClient client = open("3", monitor.id());
        receive(2, LIGHT);

        runPasses();

        assertEquals(LongStream.rangeClosed(4, 12).boxed().toList(), client.telegramNumbers());
        assertEquals(List.of(), client.named("lost"));
        assertNull(client.events.get(0).data().get("reset"));
    }

    @Test
    void numbersOfAnotherMonitorOrAboveTheLastOneStartFromTheBeginning() {
        receive(5, LIGHT);

        RecordingClient above = open("99", monitor.id());
        RecordingClient other = open("2", "made-up-other-monitor");
        RecordingClient right = open("2", monitor.id());
        runPasses();

        for (RecordingClient client : List.of(above, other)) {
            assertTrue(client.events.get(0).data().get("reset").getAsBoolean());
            assertEquals(5, client.events.get(0).number("lastNumber"));
            assertEquals(List.of(1L, 2L, 3L, 4L, 5L), client.telegramNumbers());
        }
        assertNull(right.events.get(0).data().get("reset"));
        assertEquals(List.of(3L, 4L, 5L), right.telegramNumbers());
    }

    @Test
    void anOwnSendGivesPendingAndLaterItsOutcome() {
        RecordingClient client = open();
        OwnSend send = monitor.sending(OWN, LIGHT, TelegramType.READ, "1.001", null, SecurityState.PLAIN);
        timer.runDue();
        assertEquals("PENDING", client.named("telegram").get(0).data().get("outcome").getAsString());

        send.ended(SendOutcome.NO_ANSWER);
        assertEquals(List.of("open", "telegram"), client.names());
        runPasses();

        assertEquals(List.of("open", "telegram", "outcome"), client.names());
        assertEquals(1, client.last().number("number"));
        assertEquals("NO_ANSWER", client.last().data().get("outcome").getAsString());
    }

    @Test
    void anOwnSendThatEndedBeforeThePassGivesOneRowWithTheFinalOutcome() {
        RecordingClient client = open();
        OwnSend send = monitor.sending(OWN, LIGHT, TelegramType.READ, "1.001", null, SecurityState.PLAIN);
        send.ended(SendOutcome.SENT);

        runPasses();

        assertEquals(List.of("open", "telegram"), client.names());
        assertEquals("SENT", client.named("telegram").get(0).data().get("outcome").getAsString());
    }

    @Test
    void anOutcomeGoesOnlyToTheClientsThatGotTheRow() {
        RecordingClient light = open(null, null, "dst", "31/7/1");
        RecordingClient temperature = open(null, null, "dst", "31/7/2");
        OwnSend send = monitor.sending(OWN, LIGHT, TelegramType.READ, "1.001", null, SecurityState.PLAIN);
        timer.runDue();
        send.ended(SendOutcome.SENT);
        runPasses();

        assertEquals(List.of("open", "telegram", "outcome"), light.names());
        assertEquals(List.of("open"), temperature.names());
    }

    @Test
    void aBurstLargerThanTheBufferGivesLostWithTheRightCount() {
        monitor.resize(5);
        RecordingClient client = open();
        receive(12, LIGHT);

        runPasses();

        assertEquals(List.of("open", "lost", "telegram", "telegram", "telegram", "telegram", "telegram"),
                client.names());
        assertEquals(7, client.named("lost").get(0).number("count"));
        assertEquals(List.of(8L, 9L, 10L, 11L, 12L), client.telegramNumbers());
    }

    @Test
    void rowsClearedBeforeTheyWereSentAreLost() {
        RecordingClient client = open();
        receive(3, LIGHT);
        monitor.clear();
        runPasses();
        receive(1, LIGHT);
        runPasses();

        assertEquals(List.of("open", "lost", "telegram"), client.names());
        assertEquals(3, client.named("lost").get(0).number("count"));
        assertEquals(List.of(4L), client.telegramNumbers());
    }

    @Test
    void aFastClientGetsAThousandRowReplayInOrderAt256APass() {
        receive(1000, LIGHT);
        RecordingClient client = open("0", monitor.id());
        client.slow = true;

        timer.runDue();
        assertEquals(TelegramStream.MAX_PENDING, client.telegramNumbers().size());
        runPasses(client);
        timer.advance(TelegramStream.ALIVE_MILLIS);
        timer.advance(TelegramStream.ALIVE_MILLIS);

        assertEquals(LongStream.rangeClosed(1, 1000).boxed().toList(), client.telegramNumbers());
        assertFalse(client.closed);
        assertEquals(List.of(), client.named("end"));
    }

    @Test
    void aClientThatFinishesNoSendIsClosedAtTheSecondTickWhileAnotherGetsEveryRow() {
        RecordingClient stuck = open();
        stuck.slow = true;
        RecordingClient fast = open();
        receive(600, LIGHT);

        runPasses();
        assertEquals(TelegramStream.MAX_PENDING, stuck.telegramNumbers().size());
        assertEquals(LongStream.rangeClosed(1, 600).boxed().toList(), fast.telegramNumbers());

        timer.advance(TelegramStream.ALIVE_MILLIS);
        assertFalse(stuck.closed);
        receive(10, LIGHT);
        runPasses();
        timer.advance(TelegramStream.ALIVE_MILLIS);

        assertEnd(stuck, "slow-client");
        assertEquals(LongStream.rangeClosed(1, 610).boxed().toList(), fast.telegramNumbers());
        assertFalse(fast.closed);
    }

    @Test
    void aClientThatCatchesUpBetweenTheTicksIsNotClosed() {
        RecordingClient client = open();
        client.slow = true;
        receive(300, LIGHT);
        runPasses();

        timer.advance(TelegramStream.ALIVE_MILLIS);
        client.finishAll();
        runPasses();
        client.finishAll();
        timer.advance(TelegramStream.ALIVE_MILLIS);

        assertFalse(client.closed);
        assertEquals(300, client.telegramNumbers().size());
    }

    @Test
    void aliveComesEveryTenSecondsWithTheLastNumberSent() {
        RecordingClient client = open();
        receive(2, LIGHT);
        runPasses();

        timer.advance(TelegramStream.ALIVE_MILLIS);
        timer.advance(TelegramStream.ALIVE_MILLIS);

        List<RecordingClient.Event> alive = client.named("alive");
        assertEquals(2, alive.size());
        assertEquals(2, alive.get(1).number("lastNumber"));
    }

    @Test
    void aClientWhoseSendFailsIsGoneAfterThePassAndFreesItsPlace() throws Exception {
        List<RecordingClient> clients = new ArrayList<>();
        for (int i = 0; i < TelegramStream.MAX_STREAMS; i++) {
            clients.add(open());
        }
        clients.get(0).gone = true;
        receive(1, LIGHT);
        runPasses();

        assertTrue(clients.get(0).closed);
        assertEquals(List.of("open"), clients.get(0).names());
        RecordingClient ninth = open();
        assertEquals(List.of("open"), ninth.names());
        assertEquals(1, listeners(monitor));
    }

    @Test
    void aReaderThatIsFinishedBeforeAnyPassFreesItsPlaceForTheNinth() throws Exception {
        List<RecordingClient> clients = new ArrayList<>();
        for (int i = 0; i < TelegramStream.MAX_STREAMS; i++) {
            clients.add(open());
        }
        RecordingClient gone = clients.get(3);
        gone.gone = true;

        RecordingClient ninth = open();

        assertEquals(List.of("open"), ninth.names());
        assertTrue(gone.closed);
        assertEquals(List.of("open"), gone.names());
        assertEquals(1, listeners(monitor));
        for (RecordingClient client : clients) {
            assertEquals(client != gone, !client.closed);
        }
        RecordingClient tenth = open();
        assertEnd(tenth, "too-many-streams");
    }

    @Test
    void theLastReaderOfABridgeFinishedFreesItsPlaceAndTheListenerOfItsMonitor() throws Exception {
        TelegramMonitor otherMonitor = new TelegramMonitor();
        useHandlers(RestFixtures.handler("home", monitor), RestFixtures.handler("garage", otherMonitor));
        RecordingClient garage = new RecordingClient();
        stream.open(garage, "knx:ip:garage", filter(), null, null);
        for (int i = 1; i < TelegramStream.MAX_STREAMS; i++) {
            open();
        }
        garage.gone = true;
        assertEquals(1, listeners(otherMonitor));

        RecordingClient ninth = open();

        assertEquals(List.of("open"), ninth.names());
        assertTrue(garage.closed);
        assertEquals(0, listeners(otherMonitor));
        assertEquals(1, listeners(monitor));
    }

    @Test
    void theNinthStreamIsRefused() {
        for (int i = 0; i < TelegramStream.MAX_STREAMS; i++) {
            open();
        }

        RecordingClient ninth = open();

        assertEquals(List.of("end"), ninth.names());
        assertEnd(ninth, "too-many-streams");
    }

    @Test
    void openingFailsWithOneEndEventInTheOrderOfTheChecks() {
        RecordingClient unknown = new RecordingClient();
        stream.open(unknown, "knx:ip:gone", filter(), null, null);
        assertEnd(unknown, "unknown-bridge");

        for (String key : List.of("from", "to", "context", "limit")) {
            RecordingClient refused = open(null, null, key, "1");
            assertEquals(List.of("end"), refused.names(), key);
            assertEnd(refused, "bad-input");
            assertTrue(refused.last().data().get("text").getAsString().startsWith(key), key);
        }
        assertEnd(open(null, null, "src", "not an address"), "bad-input");
        assertEnd(open("x", null), "bad-input");

        monitor.resize(0);
        RecordingClient off = open(null, null, "src", "not an address");
        assertEnd(off, "monitor-off");
    }

    @Test
    void aReplacedHandlerEndsTheStreamsOfTheOldMonitor() throws Exception {
        RecordingClient client = open();
        TelegramMonitor newMonitor = new TelegramMonitor();
        IPBridgeThingHandler replaced = RestFixtures.handler("home", newMonitor);
        useHandlers(replaced);

        timer.advance(TelegramStream.ALIVE_MILLIS);

        assertEnd(client, "bridge-gone");
        assertEquals(0, listeners(monitor));
        RecordingClient again = open();
        assertEquals(newMonitor.id(), again.events.get(0).data().get("monitorId").getAsString());
        assertEquals(1, listeners(newMonitor));
    }

    @Test
    void aMonitorTurnedOffWhileStreamsAreOpenEndsThemAtTheTick() throws Exception {
        RecordingClient first = open();
        RecordingClient second = open(null, null, "dst", "31/7/1");
        receive(1, LIGHT);
        runPasses();

        monitor.resize(0);
        timer.advance(TelegramStream.ALIVE_MILLIS);

        for (RecordingClient client : List.of(first, second)) {
            assertEnd(client, "monitor-off");
            assertEquals(KnxRestError.MONITOR_OFF.text(), client.last().data().get("text").getAsString());
            assertEquals(List.of(), client.named("alive"));
        }
        assertEquals(0, listeners(monitor));
        receive(1, LIGHT);
        assertEquals(0, timer.waitingOnce());
        timer.advance(TelegramStream.ALIVE_MILLIS);
        assertEquals(List.of("open", "telegram", "end"), first.names());

        RecordingClient another = open();
        assertEnd(another, "monitor-off");
        monitor.resize(10);
        assertEquals(List.of("open"), open().names());
        assertEquals(1, listeners(monitor));
    }

    @Test
    void aRemovedBridgeEndsItsStreams() throws Exception {
        RecordingClient client = open();
        useHandlers();

        timer.advance(TelegramStream.ALIVE_MILLIS);

        assertEnd(client, "bridge-gone");
        assertEquals(0, listeners(monitor));
    }

    @Test
    void theLastClientLeavingRemovesTheListener() throws Exception {
        RecordingClient first = open();
        RecordingClient second = open();
        assertEquals(1, listeners(monitor));

        first.gone = true;
        receive(1, LIGHT);
        runPasses();
        assertEquals(1, listeners(monitor));

        second.gone = true;
        timer.advance(TelegramStream.ALIVE_MILLIS);
        assertEquals(0, listeners(monitor));

        receive(1, LIGHT);
        assertEquals(0, timer.waitingOnce());
    }

    @Test
    void closingEndsEveryStreamAndRefusesNewOnes() throws Exception {
        RecordingClient client = open();

        stream.close();

        assertEnd(client, "shutdown");
        assertEquals(0, listeners(monitor));
        assertEnd(open(), "shutdown");
        timer.advance(3 * TelegramStream.ALIVE_MILLIS);
        assertEquals(List.of("open", "end"), client.names());
    }

    @Test
    void aRowAppendedWhileTheStreamOpensIsSentWithoutWaitingForTheNextOne() {
        TelegramMonitor racing = new TelegramMonitor() {
            private boolean armed = true;

            @Override
            public long lastNumber() {
                long number = super.lastNumber();
                if (armed) {
                    armed = false;
                    received(SENSOR, LIGHT, TelegramType.WRITE, new byte[] { 1 }, SecurityState.PLAIN);
                }
                return number;
            }
        };
        racing.received(SENSOR, LIGHT, TelegramType.WRITE, new byte[] { 1 }, SecurityState.PLAIN);
        useHandlers(RestFixtures.handler("home", racing));

        RecordingClient client = open();
        timer.runDue();

        assertEquals(1, client.events.get(0).number("lastNumber"));
        assertEquals(List.of(2L), client.telegramNumbers());
    }

    /** Runs the next pass the stream scheduled, and any that falls due at the same time. */
    private void nextPass() {
        timer.advance(Objects.requireNonNull(timer.nextOnceIn().orElse(0L)));
    }

    private void passesUntilNoneWaits(int atMost) {
        for (int i = 0; i < atMost && timer.waitingOnce() > 0; i++) {
            nextPass();
        }
        assertEquals(0, timer.waitingOnce());
    }

    @Test
    void aLiveReaderGetsANewRowInTheNextPassWhileAnotherReplaysAFullRing() {
        monitor.resize(20_000);
        receive(19_999, LIGHT);
        RecordingClient replaying = open("0", monitor.id());
        RecordingClient live = open();
        receive(1, LIGHT);

        nextPass();
        assertEquals(List.of(20_000L), live.telegramNumbers());

        passesUntilNoneWaits(200);
        assertEquals(LongStream.rangeClosed(1, 20_000).boxed().toList(), replaying.telegramNumbers());
        assertEquals(List.of(), replaying.named("lost"));
        assertEquals(List.of(20_000L), live.telegramNumbers());
    }

    @Test
    void aSlowReplayingReaderNeverHoldsUpTheNewRowsOfALiveOne() {
        monitor.resize(20_000);
        receive(19_999, LIGHT);
        RecordingClient replaying = open("0", monitor.id());
        replaying.busy = TelegramStream.MAX_PENDING - 50;
        RecordingClient live = open();

        List<Long> expected = new ArrayList<>();
        for (long row = 20_000; row < 20_020; row++) {
            receive(1, LIGHT);
            expected.add(row);
            nextPass();
            assertEquals(expected, live.telegramNumbers());
        }

        passesUntilNoneWaits(1000);
        assertEquals(LongStream.rangeClosed(1, 20_019).boxed().toList(), replaying.telegramNumbers());
        assertEquals(List.of(), replaying.named("lost"));
        assertFalse(replaying.closed);
    }

    @Test
    void aPassAsksTheMonitorForNoMoreRowsThanTheReadersCanTake() {
        List<Integer> handedOut = new ArrayList<>();
        monitor = new TelegramMonitor() {
            @Override
            public List<TelegramRecord> query(TelegramFilter filter) {
                List<TelegramRecord> rows = super.query(filter);
                handedOut.add(rows.size());
                return rows;
            }
        };
        useHandlers(RestFixtures.handler("home", monitor));
        monitor.resize(20_000);
        receive(19_999, LIGHT);
        RecordingClient replaying = open("0", monitor.id());
        replaying.slow = true;
        List<RecordingClient> live = List.of(open(), open(), open());
        receive(1, LIGHT);

        nextPass();
        assertEquals(TelegramStream.MAX_PENDING, replaying.telegramNumbers().size());
        assertTrue(handedOut.stream().mapToInt(Integer::intValue).sum() <= TelegramStream.MAX_PENDING + 1,
                "rows handed out in one pass: " + handedOut);
        for (RecordingClient client : live) {
            assertEquals(List.of(20_000L), client.telegramNumbers());
        }

        handedOut.clear();
        replaying.finishAll();
        receive(2, LIGHT);
        nextPass();
        assertTrue(handedOut.stream().mapToInt(Integer::intValue).sum() <= TelegramStream.MAX_PENDING + 2,
                "rows handed out in one pass: " + handedOut);
        assertEquals(LongStream.rangeClosed(1, 512).boxed().toList(), replaying.telegramNumbers());
    }

    @Test
    void aReaderWhoseMarkLeftTheRingGetsLostAlsoBesideALiveReader() {
        monitor.resize(10);
        receive(10, LIGHT);
        RecordingClient behind = open("2", monitor.id());
        RecordingClient live = open();
        receive(5, LIGHT);

        runPasses();

        assertEquals("lost", behind.events.get(1).name());
        assertEquals(3, behind.named("lost").get(0).number("count"));
        assertEquals(LongStream.rangeClosed(6, 15).boxed().toList(), behind.telegramNumbers());
        assertEquals(LongStream.rangeClosed(11, 15).boxed().toList(), live.telegramNumbers());
        assertEquals(List.of(), live.named("lost"));
    }

    @Test
    void aReplayFollowsAtOnceWhileTheReaderIsBehindAndTheSpacingIsBackAfterwards() {
        receive(1000, LIGHT);
        RecordingClient client = open("0", monitor.id());
        long start = timer.now();

        timer.runDue();

        assertEquals(LongStream.rangeClosed(1, 1000).boxed().toList(), client.telegramNumbers());
        assertEquals(start, timer.now());
        assertEquals(0, timer.waitingOnce());

        receive(1, LIGHT);
        assertEquals(Optional.of(TelegramStream.PASS_SPACING_MILLIS), timer.nextOnceIn());
        timer.advance(TelegramStream.PASS_SPACING_MILLIS - 1);
        assertEquals(1000, client.telegramNumbers().size());
        timer.advance(1);
        assertEquals(1001, client.telegramNumbers().size());
    }

    @Test
    void aFilteredReaderGoesPastRowsItDoesNotWantInOnePassAndAPassThatFilledAReaderKeepsTheSpacing() {
        receive(1000, LIGHT);
        RecordingClient temperature = open("0", monitor.id(), "dst", "31/7/2");

        assertTrue(timer.runNextDue());

        assertEquals(List.of("open"), temperature.names());
        assertEquals(0, timer.waitingOnce());

        RecordingClient full = open("0", monitor.id());
        full.slow = true;
        timer.advance(TelegramStream.PASS_SPACING_MILLIS);
        assertEquals(TelegramStream.MAX_PENDING, full.telegramNumbers().size());
        assertEquals(Optional.of(TelegramStream.PASS_SPACING_MILLIS), timer.nextOnceIn());
    }

    /** A monitor whose query, on the given call after arming, appends a row as a bus thread would meanwhile. */
    private void appendingOnQuery(int call, boolean[] armed) {
        int[] calls = { 0 };
        TelegramMonitor appending = new TelegramMonitor() {
            @Override
            public List<TelegramRecord> query(TelegramFilter filter) {
                List<TelegramRecord> rows = super.query(filter);
                if (armed[0] && ++calls[0] == call) {
                    received(SENSOR, LIGHT, TelegramType.WRITE, new byte[] { 1 }, SecurityState.PLAIN);
                }
                return rows;
            }
        };
        monitor = appending;
        useHandlers(RestFixtures.handler("home", appending));
    }

    @Test
    void aRowArrivingDuringACatchUpPassLeavesTheNextPassImmediate() {
        boolean[] armed = { false };
        appendingOnQuery(1, armed);
        receive(1000, LIGHT);
        RecordingClient client = open("0", monitor.id());
        armed[0] = true;
        long start = timer.now();

        assertTrue(timer.runNextDue());

        assertEquals(TelegramStream.MAX_PENDING, client.telegramNumbers().size());
        assertEquals(Optional.of(0L), timer.nextOnceIn());
        assertEquals(1, timer.waitingOnce());
        timer.runDue();
        assertEquals(LongStream.rangeClosed(1, 1001).boxed().toList(), client.telegramNumbers());
        assertEquals(start, timer.now());
        assertEquals(0, timer.waitingOnce());
    }

    @Test
    void aRowArrivingDuringTheLastPassOfAReplayIsSentByTheNextPass() {
        appendingOnQuery(2, new boolean[] { true });
        receive(300, LIGHT);
        RecordingClient client = open("0", monitor.id());

        assertTrue(timer.runNextDue());
        assertTrue(timer.runNextDue());

        assertEquals(LongStream.rangeClosed(1, 300).boxed().toList(), client.telegramNumbers());
        assertEquals(1, timer.waitingOnce());
        assertEquals(Optional.of(TelegramStream.PASS_SPACING_MILLIS), timer.nextOnceIn());
        timer.advance(TelegramStream.PASS_SPACING_MILLIS);
        assertEquals(LongStream.rangeClosed(1, 301).boxed().toList(), client.telegramNumbers());
        assertEquals(0, timer.waitingOnce());
    }

    @Test
    void aRowArrivingDuringAPassWithNobodyBehindKeepsTheSpacing() {
        boolean[] armed = { false };
        appendingOnQuery(1, armed);
        RecordingClient client = open();
        receive(1, LIGHT);
        armed[0] = true;

        timer.runDue();

        assertEquals(List.of(1L), client.telegramNumbers());
        assertEquals(Optional.of(TelegramStream.PASS_SPACING_MILLIS), timer.nextOnceIn());
        timer.advance(TelegramStream.PASS_SPACING_MILLIS - 1);
        assertEquals(List.of(1L), client.telegramNumbers());
        timer.advance(1);
        assertEquals(List.of(1L, 2L), client.telegramNumbers());
    }

    @Test
    void aRowArrivingJustAfterAPassReadTheNewestNumberIsSentWithoutWaitingForAnother() {
        int[] calls = { -1 };
        TelegramMonitor racing = new TelegramMonitor() {
            @Override
            public long lastNumber() {
                long number = super.lastNumber();
                // the second read of a pass is the one that decides whether anybody is behind
                if (calls[0] >= 0 && ++calls[0] == 2) {
                    received(SENSOR, LIGHT, TelegramType.WRITE, new byte[] { 1 }, SecurityState.PLAIN);
                }
                return number;
            }
        };
        monitor = racing;
        useHandlers(RestFixtures.handler("home", racing));
        RecordingClient client = open();
        receive(1, LIGHT);
        calls[0] = 0;

        timer.runDue();

        assertEquals(List.of(1L), client.telegramNumbers());
        assertEquals(1, timer.waitingOnce());
        timer.advance(TelegramStream.PASS_SPACING_MILLIS);
        assertEquals(List.of(1L, 2L), client.telegramNumbers());
    }

    /** A monitor whose first query after arming runs the given failure instead of answering. */
    private boolean[] failingOnQuery(Runnable failure) {
        boolean[] armed = { false };
        TelegramMonitor failing = new TelegramMonitor() {
            @Override
            public List<TelegramRecord> query(TelegramFilter filter) {
                if (armed[0]) {
                    armed[0] = false;
                    failure.run();
                }
                return super.query(filter);
            }
        };
        monitor = failing;
        useHandlers(RestFixtures.handler("home", failing));
        return armed;
    }

    @Test
    void aPassThatFailedLeavesTheWaitingRowToTheNextTelegramWhichBringsItAlong() {
        boolean[] armed = failingOnQuery(() -> {
            throw new IllegalStateException("made-up");
        });
        RecordingClient client = open();
        receive(1, LIGHT);
        armed[0] = true;

        timer.runDue();

        assertEquals(List.of(), client.telegramNumbers());
        assertEquals(0, timer.waitingOnce());
        receive(1, LIGHT);
        runPasses();
        assertEquals(List.of(1L, 2L), client.telegramNumbers());
    }

    @Test
    void anErrorInAPassIsThrownOnAndTheStreamGoesOnForItsReaderAndANewOne() {
        boolean[] armed = failingOnQuery(() -> {
            throw new StackOverflowError();
        });
        RecordingClient first = open();
        receive(1, LIGHT);
        armed[0] = true;

        assertThrows(StackOverflowError.class, timer::runDue);

        assertEquals(List.of(), first.telegramNumbers());
        receive(1, LIGHT);
        runPasses();
        assertEquals(List.of(1L, 2L), first.telegramNumbers());
        RecordingClient second = open("0", monitor.id());
        receive(1, LIGHT);
        runPasses();
        assertEquals(List.of(1L, 2L, 3L), first.telegramNumbers());
        assertEquals(List.of(1L, 2L, 3L), second.telegramNumbers());
        assertFalse(first.closed);
        assertFalse(second.closed);
    }

    @Test
    void anOutcomeSignalledAfterAPassTookTheSignalsIsSentByTheNextPassWithoutAnotherTelegram() {
        int[] reads = { -1 };
        List<OwnSend> sends = new ArrayList<>();
        TelegramMonitor racing = new TelegramMonitor() {
            @Override
            public long lastNumber() {
                long number = super.lastNumber();
                // the second read of a pass comes after the pass took the signals
                if (reads[0] >= 0 && ++reads[0] == 2) {
                    sends.get(0).ended(SendOutcome.SENT);
                }
                return number;
            }
        };
        monitor = racing;
        useHandlers(RestFixtures.handler("home", racing));
        RecordingClient client = open();
        sends.add(monitor.sending(OWN, LIGHT, TelegramType.READ, "1.001", null, SecurityState.PLAIN));
        timer.runDue();
        receive(1, LIGHT);
        reads[0] = 0;

        timer.advance(TelegramStream.PASS_SPACING_MILLIS);

        assertEquals(List.of("open", "telegram", "telegram"), client.names());
        assertEquals(1, timer.waitingOnce());
        runPasses();
        assertEquals(List.of("open", "telegram", "telegram", "outcome"), client.names());
        assertEquals(1, client.last().number("number"));
        assertEquals("SENT", client.last().data().get("outcome").getAsString());
    }

    /** {@return the windows the monitor answers to the stream, as they come} */
    private List<TelegramMonitor.Window> recordWindows() {
        List<TelegramMonitor.Window> windows = new ArrayList<>();
        TelegramMonitor recording = new TelegramMonitor() {
            @Override
            public Window window(TelegramFilter filter, long after, int limit, int lookAt) {
                Window window = super.window(filter, after, limit, lookAt);
                windows.add(window);
                return window;
            }
        };
        monitor = recording;
        useHandlers(RestFixtures.handler("home", recording));
        return windows;
    }

    @Test
    void aFilteredReaderGoesPastAFullRingOfOtherRowsWithoutWaitingBetweenPasses() {
        List<TelegramMonitor.Window> windows = recordWindows();
        monitor.resize(20_000);
        receive(20_000, LIGHT);
        RecordingClient temperature = open("0", monitor.id(), "dst", "31/7/2");
        long start = timer.now();

        timer.runDue();

        assertEquals(start, timer.now());
        assertEquals(0, timer.waitingOnce());
        assertEquals(20_000 / TelegramStream.MAX_ROWS_PER_PASS, windows.size());
        for (TelegramMonitor.Window window : windows) {
            assertEquals(TelegramStream.MAX_ROWS_PER_PASS, window.last() - window.first() + 1);
        }
        receive(1, TEMPERATURE);
        assertEquals(Optional.of(TelegramStream.PASS_SPACING_MILLIS), timer.nextOnceIn());
        nextPass();
        assertEquals(List.of(20_001L), temperature.telegramNumbers());
        timer.advance(TelegramStream.ALIVE_MILLIS);
        assertEquals(20_001, temperature.named("alive").get(0).number("lastNumber"));
    }

    @Test
    void aFilteredReaderGoingPastOtherRowsNeverHoldsUpALiveOne() {
        monitor.resize(20_000);
        receive(19_999, LIGHT);
        RecordingClient temperature = open("0", monitor.id(), "dst", "31/7/2");
        RecordingClient live = open();

        List<Long> expected = new ArrayList<>();
        for (long row = 20_000; row < 20_004; row++) {
            receive(1, row % 2 == 0 ? LIGHT : TEMPERATURE);
            expected.add(row);
            assertTrue(timer.runNextDue());
            assertEquals(expected, live.telegramNumbers());
        }
        receive(1, TEMPERATURE);
        passesUntilNoneWaits(10);

        assertEquals(List.of(20_001L, 20_003L, 20_004L), temperature.telegramNumbers());
        assertEquals(LongStream.rangeClosed(20_000, 20_004).boxed().toList(), live.telegramNumbers());
    }

    @Test
    void aFilteredReaderWithLessRoomThanMatchesInTheWindowGetsEachOnceInOrder() {
        List<TelegramMonitor.Window> windows = recordWindows();
        for (int row = 1; row <= 1200; row++) {
            receive(1, row % 3 == 1 ? TEMPERATURE : LIGHT);
        }
        RecordingClient temperature = open("0", monitor.id(), "dst", "31/7/2");
        temperature.busy = TelegramStream.MAX_PENDING - 30;
        RecordingClient live = open();
        receive(1, LIGHT);

        passesUntilNoneWaits(100);

        assertEquals(LongStream.iterate(1, n -> n + 3).limit(400).boxed().toList(), temperature.telegramNumbers());
        assertEquals(List.of(1201L), live.telegramNumbers());
        assertTrue(windows.size() >= 400 / 30);
        for (TelegramMonitor.Window window : windows) {
            assertTrue(window.rows().size() <= 30, "rows handed out: " + window.rows().size());
            assertTrue(window.last() - window.first() < TelegramStream.MAX_ROWS_PER_PASS);
        }
    }

    @Test
    void aFilteredReaderWhoseMarkLeftTheRingGetsLost() {
        monitor.resize(10);
        for (int row = 1; row <= 15; row++) {
            receive(1, row % 2 == 1 ? TEMPERATURE : LIGHT);
        }
        RecordingClient temperature = open("2", monitor.id(), "dst", "31/7/2");

        runPasses();

        assertEquals("lost", temperature.events.get(1).name());
        assertEquals(3, temperature.named("lost").get(0).number("count"));
        assertEquals(List.of(7L, 9L, 11L, 13L, 15L), temperature.telegramNumbers());
    }

    @Test
    void aFilteredReaderWhoseLostEventTookAPlaceGoesOnRightBeforeTheRowThatDidNotFit() {
        List<TelegramMonitor.Window> windows = recordWindows();
        monitor.resize(10);
        for (int row = 1; row <= 15; row++) {
            receive(1, row % 2 == 1 ? TEMPERATURE : LIGHT);
        }
        RecordingClient temperature = open("2", monitor.id(), "dst", "31/7/2");
        temperature.slow = true;
        temperature.busy = TelegramStream.MAX_PENDING - 3;

        runPasses(temperature);

        assertEquals(3, temperature.named("lost").get(0).number("count"));
        assertEquals(List.of(7L, 9L, 11L, 13L, 15L), temperature.telegramNumbers());
        assertEquals(List.of(7L, 9L, 11L), windows.get(0).rows().stream().map(TelegramRecord::number).toList());
        assertEquals(11, windows.get(1).first());
    }

    @Test
    void theStreamsOwnEndCodesHaveFixedTextsWithoutAPathOrAnException() {
        List<String> codes = new ArrayList<>();
        for (TelegramStream.End end : TelegramStream.End.values()) {
            codes.add(end.code);
            assertFalse(end.text.toLowerCase(java.util.Locale.ROOT).contains("exception"), end.code);
            assertFalse(end.text.contains("/"), end.code);
            for (KnxRestError error : KnxRestError.values()) {
                assertNotEquals(error.code(), end.code);
            }
        }
        assertEquals(List.of("slow-client", "bridge-gone", "shutdown"), codes);
    }
}
