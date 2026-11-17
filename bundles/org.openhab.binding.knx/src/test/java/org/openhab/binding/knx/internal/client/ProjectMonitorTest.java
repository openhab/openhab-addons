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
package org.openhab.binding.knx.internal.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openhab.binding.knx.internal.handler.GroupAddressListener;
import org.openhab.binding.knx.internal.monitor.DataType;
import org.openhab.binding.knx.internal.monitor.ProjectLookup;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.monitor.TelegramRecord;
import org.openhab.binding.knx.internal.project.ProjectTestFiles;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore;
import org.openhab.binding.knx.internal.projectstore.LogCapture;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ThingUID;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * Checks that the group monitor shows names and values from the stored project of its bridge.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class ProjectMonitorTest {
    private static final ThingUID BRIDGE = new ThingUID("knx:ip:made-up");
    private static final IndividualAddress OPENHAB = new IndividualAddress(7, 7, 250);
    private static final IndividualAddress ACTUATOR = new IndividualAddress(7, 7, 1);
    private static final GroupAddress TEMPERATURE = new GroupAddress(31, 7, 2);
    private static final GroupAddress ACCESS = new GroupAddress(31, 7, 3);
    private static final byte[] WRITE_21_5 = { 0x00, (byte) 0x80, 0x0c, 0x33 };

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private @Nullable MonitorTestClient client;

    @TempDir
    @Nullable
    Path tempFolder;

    @AfterEach
    void tearDown() {
        MonitorTestClient tmp = client;
        if (tmp != null) {
            tmp.dispose();
        }
        scheduler.shutdownNow();
    }

    private KnxProjectStore store() {
        return new KnxProjectStore(Objects.requireNonNull(tempFolder), Clock.systemUTC());
    }

    private FeedLink connect(TelegramMonitor monitor) {
        FeedLink link = new FeedLink(OPENHAB);
        MonitorTestClient c = new MonitorTestClient(scheduler, monitor, () -> link);
        client = c;
        c.initialize();
        assertTrue(c.isConnected());
        return link;
    }

    private static TelegramRecord only(TelegramMonitor monitor) {
        List<TelegramRecord> records = monitor.records();
        assertEquals(1, records.size());
        return records.get(0);
    }

    @Test
    void namesAndValueComeWithAnImportAndGoWithARemove() throws Exception {
        KnxProjectStore store = store();
        TelegramMonitor monitor = new TelegramMonitor();
        monitor.useProject(store.lookup(BRIDGE));
        FeedLink link = connect(monitor);
        link.receive(ACTUATOR, TEMPERATURE, WRITE_21_5);

        TelegramRecord raw = only(monitor);
        assertNull(raw.dataType());
        assertNull(raw.value());
        assertNull(raw.destinationName());
        assertNull(raw.sourceName());

        store.importProject(BRIDGE, ProjectTestFiles.plain(), null, null);
        TelegramRecord named = only(monitor);
        assertEquals("9.001", named.dataType());
        assertEquals("21.5", named.value());
        assertEquals("°C", named.unit());
        assertEquals("Made-up temperature", named.destinationName());
        assertEquals(ProjectTestFiles.DEVICE_NAME, named.sourceName());
        assertEquals("Made-up temperature",
                Objects.requireNonNull(monitor.lastTelegrams().get(TEMPERATURE)).destinationName());

        store.remove(BRIDGE);
        TelegramRecord rawAgain = only(monitor);
        assertNull(rawAgain.dataType());
        assertNull(rawAgain.value());
        assertNull(rawAgain.destinationName());
        assertNull(rawAgain.sourceName());
    }

    @Test
    void aChannelWithAnotherTypeComesBeforeTheProject() throws Exception {
        KnxProjectStore store = store();
        store.importProject(BRIDGE, ProjectTestFiles.plain(), null, null);
        TelegramMonitor monitor = new TelegramMonitor();
        monitor.useProject(store.lookup(BRIDGE));
        FeedLink link = connect(monitor);
        GroupAddressListener channel = mock(GroupAddressListener.class);
        when(channel.listensTo(TEMPERATURE)).thenReturn(true);
        when(channel.monitorDataType(TEMPERATURE)).thenReturn(new DataType("7.001", DecimalType.class));
        Objects.requireNonNull(client).registerGroupAddressListener(channel);

        link.receive(ACTUATOR, TEMPERATURE, WRITE_21_5);

        TelegramRecord record = only(monitor);
        assertEquals("7.001", record.dataType());
        assertEquals("3123.0", record.value());
        assertEquals("Made-up temperature", record.destinationName());
    }

    @Test
    void aProjectTypeTheMonitorCannotDecodeGivesRawBytesAndTheName() throws Exception {
        KnxProjectStore store = store();
        store.importProject(BRIDGE, ProjectTestFiles.plain(), null, null);
        TelegramMonitor monitor = new TelegramMonitor();
        monitor.useProject(store.lookup(BRIDGE));
        FeedLink link = connect(monitor);

        try (LogCapture log = new LogCapture()) {
            link.receive(ACTUATOR, ACCESS, new byte[] { 0x00, (byte) 0x80, 0x01, 0x02, 0x03, 0x04 });
            TelegramRecord record = only(monitor);

            assertNull(record.dataType());
            assertNull(record.value());
            assertArrayEquals(new byte[] { 0x01, 0x02, 0x03, 0x04 }, record.payload());
            assertEquals("Made-up access", record.destinationName());
            assertEquals(List.of(), log.warnings());
        }
    }

    @Test
    void aThrowingLookupDoesNotBreakTheQuery() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        monitor.useProject(new ProjectLookup() {
            @Override
            public @Nullable String groupAddressName(GroupAddress address) {
                throw new IllegalStateException("made-up failure");
            }

            @Override
            public @Nullable String dataType(GroupAddress address) {
                throw new IllegalStateException("made-up failure");
            }

            @Override
            public @Nullable String deviceName(IndividualAddress address) {
                throw new IllegalStateException("made-up failure");
            }
        });
        FeedLink link = connect(monitor);

        link.receive(ACTUATOR, TEMPERATURE, WRITE_21_5);

        TelegramRecord record = only(monitor);
        assertNull(record.value());
        assertNull(record.destinationName());
        assertNull(record.sourceName());
    }
}
