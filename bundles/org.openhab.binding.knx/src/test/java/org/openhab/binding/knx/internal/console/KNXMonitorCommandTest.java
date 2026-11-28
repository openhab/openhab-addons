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
package org.openhab.binding.knx.internal.console;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.knx.internal.factory.KNXHandlerFactory;
import org.openhab.binding.knx.internal.handler.IPBridgeThingHandler;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.monitor.ConnectionKind;
import org.openhab.binding.knx.internal.monitor.DataType;
import org.openhab.binding.knx.internal.monitor.NotDecryptedReason;
import org.openhab.binding.knx.internal.monitor.ProjectLookup;
import org.openhab.binding.knx.internal.monitor.SecurityState;
import org.openhab.binding.knx.internal.monitor.SendOutcome;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.monitor.TelegramType;
import org.openhab.binding.knx.internal.monitor.ValueText;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.io.console.Console;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * Checks the {@code monitor} console command.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KNXMonitorCommandTest {
    private static final IndividualAddress SENSOR = new IndividualAddress(1, 1, 20);
    private static final IndividualAddress SWITCH = new IndividualAddress(1, 1, 21);
    private static final IndividualAddress OPENHAB = new IndividualAddress(1, 1, 250);
    private static final GroupAddress TEMPERATURE = new GroupAddress(3, 1, 10);
    private static final GroupAddress LIGHT = new GroupAddress(3, 1, 11);
    private static final GroupAddress UNUSED = new GroupAddress(3, 1, 99);
    private static final Instant START = Instant.parse("2026-03-01T10:00:00Z");
    private static final String HEADER = "KNX bridge \"Home KNX\" (knx:ip:home): tunnel, own address 1.1.250, "
            + "buffer 2000, rows 5, received 3, sent 2, not decrypted 0";
    private static final List<String> ROWS = List.of(
            "       1 2026-03-01 10:00:00.001 in  write    1.1.20   -> 3/1/10   plain   9.001   21.5 °C",
            "       2 2026-03-01 10:00:00.002 in  write    1.1.21   -> 3/1/11   secured 1.001   ON",
            "       3 2026-03-01 10:00:00.003 out write    1.1.250  -> 3/1/11   secured 1.001   OFF  sent",
            "       4 2026-03-01 10:00:00.004 in  write    1.1.20   -> 3/1/99   plain   -       raw 0C 33",
            "       5 2026-03-01 10:00:00.005 out read     1.1.250  -> 3/1/10   plain   9.001   -  no answer");

    private final KNXHandlerFactory factory = mock(KNXHandlerFactory.class);
    private final Console console = mock(Console.class);
    private final KNXCommandExtension extension = new KNXCommandExtension(factory,
            new KnxProjectStore(Path.of("target", "no-such-knx-folder"), Clock.systemUTC()), mock(LocaleProvider.class),
            Clock.fixed(START.plusMillis(1003), ZoneOffset.UTC), ZoneOffset.UTC);

    private static TelegramMonitor monitorWithRows() {
        TelegramMonitor monitor = new TelegramMonitor(new StepClock());
        monitor.connected(ConnectionKind.TUNNEL, OPENHAB);
        monitor.useChannelTypes(address -> TEMPERATURE.equals(address) ? new DataType("9.001", DecimalType.class)
                : LIGHT.equals(address) ? new DataType("1.001", OnOffType.class) : null);
        monitor.received(SENSOR, TEMPERATURE, TelegramType.WRITE, new byte[] { 0x0c, 0x33 }, SecurityState.PLAIN);
        monitor.received(SWITCH, LIGHT, TelegramType.WRITE, new byte[] { 1 }, SecurityState.SECURED);
        monitor.sent(OPENHAB, LIGHT, TelegramType.WRITE, new byte[0], "1.001", new ValueText("OFF", null),
                SecurityState.SECURED, SendOutcome.SENT);
        monitor.received(SENSOR, UNUSED, TelegramType.WRITE, new byte[] { 0x0c, 0x33 }, SecurityState.PLAIN);
        monitor.sending(OPENHAB, TEMPERATURE, TelegramType.READ, "9.001", null, SecurityState.PLAIN)
                .ended(SendOutcome.NO_ANSWER);
        return monitor;
    }

    private KNXBridgeBaseThingHandler bridge(String id, String label, TelegramMonitor monitor) {
        IPBridgeThingHandler handler = mock(IPBridgeThingHandler.class);
        Bridge thing = mock(Bridge.class);
        when(thing.getUID()).thenReturn(new ThingUID("knx", "ip", id));
        when(thing.getLabel()).thenReturn(label);
        when(handler.getThing()).thenReturn(thing);
        when(handler.getTelegramMonitor()).thenReturn(monitor);
        return handler;
    }

    private void bridges(KNXBridgeBaseThingHandler... handlers) {
        when(factory.getBridges()).thenReturn(Set.of(handlers));
    }

    private List<String> run(String... args) {
        clearInvocations(console);
        extension.execute(args, console);
        ArgumentCaptor<String> lines = ArgumentCaptor.forClass(String.class);
        verify(console, atLeast(0)).println(lines.capture());
        return new ArrayList<>(lines.getAllValues());
    }

    /** The header and the rows with the given numbers. */
    private static List<String> expected(int... numbers) {
        List<String> lines = new ArrayList<>(List.of(HEADER));
        for (int number : numbers) {
            lines.add(ROWS.get(number - 1));
        }
        return lines;
    }

    @Test
    void theHeaderAndEveryRowOldestFirst() {
        bridges(bridge("home", "Home KNX", monitorWithRows()));

        assertEquals(expected(1, 2, 3, 4, 5), run("monitor"));
        assertEquals(expected(1, 2, 3, 4, 5), run("monitor", "home"));
    }

    @Test
    void eachFilter() {
        bridges(bridge("home", "Home KNX", monitorWithRows()));

        assertEquals(expected(1, 4), run("monitor", "home", "src=1.1.20"));
        assertEquals(expected(1, 2, 3, 5), run("monitor", "dst=3/1/10,3/1/11"));
        assertEquals(expected(5), run("monitor", "type=read"));
        assertEquals(expected(3, 5), run("monitor", "dir=OUT"));
        assertEquals(expected(2, 3), run("monitor", "dpt=1"));
        assertEquals(expected(1, 5), run("monitor", "dpt=9.001"));
        assertEquals(expected(4, 5), run("monitor", "from=10:00:00.004"));
        assertEquals(expected(1, 2), run("monitor", "to=2026-03-01T10:00:00.002"));
        assertEquals(expected(3, 4, 5), run("monitor", "from=2026-03-01T10:00:00.003Z"));
        assertEquals(expected(3, 4, 5), run("monitor", "from=-1s"));
        assertEquals(expected(4, 5), run("monitor", "type=read", "context=1"));
        assertEquals(expected(4, 5), run("monitor", "limit=2"));
    }

    @Test
    void filtersCombine() {
        bridges(bridge("home", "Home KNX", monitorWithRows()));

        assertEquals(expected(5), run("monitor", "src=1.1.250", "dpt=9"));
        assertEquals(expected(1, 3, 4), run("monitor", "src=1.1.20,1.1.250", "type=write"));
    }

    @Test
    void theNewestFiftyByDefault() {
        TelegramMonitor monitor = new TelegramMonitor(new StepClock());
        for (int i = 0; i < 60; i++) {
            monitor.received(SENSOR, UNUSED, TelegramType.WRITE, new byte[] { 1 }, SecurityState.PLAIN);
        }
        bridges(bridge("home", "Home KNX", monitor));

        List<String> out = run("monitor");

        assertEquals(51, out.size());
        assertTrue(out.get(1).startsWith("      11 "), out.get(1));
        assertTrue(out.get(50).startsWith("      60 "), out.get(50));
    }

    @Test
    void clearForgetsTheRows() {
        TelegramMonitor monitor = monitorWithRows();
        bridges(bridge("home", "Home KNX", monitor));

        assertEquals(List.of("KNX bridge \"Home KNX\" (knx:ip:home): telegrams cleared."),
                run("monitor", "home", "clear"));

        assertEquals(List.of(), monitor.records());
        assertEquals(List.of("KNX bridge \"Home KNX\" (knx:ip:home): tunnel, own address 1.1.250, buffer 2000, "
                + "rows 0, received 3, sent 2, not decrypted 0", "  no telegrams"), run("monitor"));
    }

    @Test
    void everyBridgeWhenNoneIsNamed() {
        bridges(bridge("home", "Home KNX", monitorWithRows()), bridge("annex", "Annex", new TelegramMonitor()));

        List<String> out = run("monitor", "limit=1");

        assertEquals(
                List.of("KNX bridge \"Annex\" (knx:ip:annex): not connected, buffer 2000, rows 0, received 0, sent 0, "
                        + "not decrypted 0", "  no telegrams", HEADER, ROWS.get(4)),
                out);
    }

    @Test
    void anUnknownBridgeIsSaid() {
        bridges(bridge("home", "Home KNX", monitorWithRows()));

        assertEquals(List.of("No KNX bridge \"nope\". Known bridges: knx:ip:home"), run("monitor", "nope"));
    }

    @Test
    void whatCannotBeReadIsSaid() {
        bridges(bridge("home", "Home KNX", monitorWithRows()));

        assertEquals(List.of("Cannot read \"src=nope\": not an individual address"), run("monitor", "src=nope"));
        assertEquals(List.of("Cannot read \"dst=1/2/3,x\": not a group address"), run("monitor", "dst=1/2/3,x"));
        assertEquals(List.of("Cannot read \"type=maybe\": not one of write, read, response"),
                run("monitor", "type=maybe"));
        assertEquals(List.of("Cannot read \"dpt=nine\": not a data type such as 9 or 9.001"),
                run("monitor", "dpt=nine"));
        assertEquals(List.of("Cannot read \"limit=-1\": not a number from 0 on"), run("monitor", "limit=-1"));
        assertEquals(List.of("Cannot read \"speed=3\": unknown filter"), run("monitor", "speed=3"));
        assertTrue(run("monitor", "from=yesterday").get(0).startsWith("Cannot read \"from=yesterday\": "));
        assertEquals(List.of("Cannot read \"other\": only one bridge can be given."), run("monitor", "home", "other"));
    }

    @Test
    void namesShowWhenKnown() {
        TelegramMonitor monitor = new TelegramMonitor(new StepClock());
        monitor.useProject(new ProjectLookup() {
            @Override
            public @Nullable String groupAddressName(GroupAddress address) {
                return "Made-up group";
            }

            @Override
            public @Nullable String dataType(GroupAddress address) {
                return null;
            }

            @Override
            public @Nullable String deviceName(IndividualAddress address) {
                return "Made-up device";
            }
        });
        monitor.received(SENSOR, UNUSED, TelegramType.WRITE, new byte[] { 1 }, SecurityState.PLAIN);
        bridges(bridge("home", "Home KNX", monitor));

        assertEquals(
                "       1 2026-03-01 10:00:00.001 in  write    1.1.20 \"Made-up device\" -> 3/1/99 \"Made-up group\" "
                        + "plain   -       raw 01",
                run("monitor").get(1));
    }

    @Test
    void aPlainTelegramOnASecuredAddressIsMarkedAndCounted() {
        TelegramMonitor monitor = new TelegramMonitor(new StepClock());
        monitor.received(SENSOR, UNUSED, TelegramType.WRITE, new byte[] { 1 }, SecurityState.PLAIN, true);
        bridges(bridge("home", "Home KNX", monitor));

        assertEquals(List.of(
                "KNX bridge \"Home KNX\" (knx:ip:home): not connected, buffer 2000, rows 1, received 1, sent 0, "
                        + "not decrypted 0, plain on secured addresses 1",
                "       1 2026-03-01 10:00:00.001 in  write    1.1.20   -> 3/1/99   plain on secured address -       "
                        + "raw 01"),
                run("monitor"));
    }

    @Test
    void notDecryptedTelegramsShowWithTheirReasonAndAreCountedPerReason() {
        TelegramMonitor monitor = new TelegramMonitor(new StepClock());
        monitor.notDecrypted(SENSOR, UNUSED, NotDecryptedReason.SEQUENCE_NUMBER, a -> false);
        monitor.notDecrypted(SENSOR, UNUSED, NotDecryptedReason.SEQUENCE_NUMBER, a -> false);
        monitor.notDecrypted(SWITCH, LIGHT, NotDecryptedReason.NO_KEY, a -> false);
        monitor.notDecrypted(SWITCH, LIGHT, null, a -> false);
        bridges(bridge("home", "Home KNX", monitor));

        assertEquals(List.of(
                "KNX bridge \"Home KNX\" (knx:ip:home): not connected, buffer 2000, rows 4, received 4, sent 0, "
                        + "not decrypted 4 (NO_KEY 1, SEQUENCE_NUMBER 2, other 1)",
                "       1 2026-03-01 10:00:00.001 in  ?        1.1.20   -> 3/1/99   not decrypted (SEQUENCE_NUMBER) -       -",
                "       2 2026-03-01 10:00:00.002 in  ?        1.1.20   -> 3/1/99   not decrypted (SEQUENCE_NUMBER) -       -",
                "       3 2026-03-01 10:00:00.003 in  ?        1.1.21   -> 3/1/11   not decrypted (NO_KEY) -       -",
                "       4 2026-03-01 10:00:00.004 in  ?        1.1.21   -> 3/1/11   not decrypted (unknown reason) -       -"),
                run("monitor"));
    }

    @Test
    void usageAndCompleterKnowTheCommand() {
        bridges(bridge("home", "Home KNX", monitorWithRows()));

        assertTrue(extension.getUsages().stream().anyMatch(u -> u.contains("monitor [<bridge>] [src=")),
                extension.getUsages().toString());
        assertTrue(extension.getUsages().stream().anyMatch(u -> u.contains("newest 50")));
        assertTrue(extension.getUsages().stream().anyMatch(u -> u.contains("monitor [<bridge>] clear")));

        List<String> candidates = new ArrayList<>();
        assertTrue(extension.complete(new String[] { "mo" }, 0, 2, candidates));
        assertTrue(candidates.contains("monitor "), candidates.toString());

        candidates.clear();
        assertTrue(extension.complete(new String[] { "monitor", "" }, 1, 0, candidates));
        assertEquals(List.of("knx:ip:home "), candidates);
    }

    /** Each reading is one millisecond after the one before. */
    private static class StepClock extends Clock {
        private Instant now = START;

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(@Nullable ZoneId zone) {
            return this;
        }

        @Override
        public synchronized Instant instant() {
            now = now.plus(Duration.ofMillis(1));
            return now;
        }
    }
}
