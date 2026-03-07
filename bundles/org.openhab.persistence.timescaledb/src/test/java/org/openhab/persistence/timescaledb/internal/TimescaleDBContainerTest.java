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
package org.openhab.persistence.timescaledb.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.CallItem;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.ImageItem;
import org.openhab.core.library.items.LocationItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.PlayerItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.types.State;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Integration tests that run against a real TimescaleDB instance via Testcontainers.
 *
 * <p>
 * These tests verify end-to-end behavior: schema creation, store/query round-trips for
 * all state types, downsampling job execution, and the {@code remove()} operation.
 *
 * <p>
 * Tag: {@code integration} — can be excluded from fast unit-test runs with
 * {@code mvn test -Dgroups='!integration'}.
 */
@Tag("integration")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TimescaleDBContainerTest {

    @Container
    static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("timescale/timescaledb:latest-pg16")
            .withDatabaseName("openhab_test").withUsername("openhab").withPassword("openhab");

    private static HikariDataSource dataSource;

    @BeforeAll
    static void setUpDataSource() {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(db.getJdbcUrl());
        cfg.setUsername(db.getUsername());
        cfg.setPassword(db.getPassword());
        cfg.setMaximumPoolSize(3);
        dataSource = new HikariDataSource(cfg);
    }

    @BeforeEach
    void initSchema() throws SQLException {
        // Drop and recreate for test isolation
        try (Connection conn = dataSource.getConnection(); var stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS items CASCADE");
            stmt.execute("DROP TABLE IF EXISTS item_meta CASCADE");
        }
        try (Connection conn = dataSource.getConnection()) {
            TimescaleDBSchema.initialize(conn, "7 days", 0, 0);
        }
    }

    // ------------------------------------------------------------------
    // Schema
    // ------------------------------------------------------------------

    @Test
    @Order(1)
    void schema_hypertableExists() throws SQLException {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn
                        .prepareStatement("SELECT hypertable_name FROM timescaledb_information.hypertables "
                                + "WHERE hypertable_name = 'items'");
                ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next(), "Hypertable 'items' should exist");
        }
    }

    @Test
    @Order(2)
    void schema_itemMetaTableExists() throws SQLException {
        try (Connection conn = dataSource.getConnection();
                var stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM item_meta")) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1), "item_meta should be empty after init");
        }
    }

    // ------------------------------------------------------------------
    // Store / query round-trips — all state types
    // ------------------------------------------------------------------

    @Test
    @Order(10)
    void roundTrip_DecimalType() throws SQLException {
        storeAndVerify("DecimalSensor", new NumberItem("DecimalSensor"), new DecimalType(42.5),
                s -> assertInstanceOf(DecimalType.class, s),
                s -> assertEquals(42.5, ((DecimalType) s).doubleValue(), 1e-6));
    }

    @Test
    @Order(11)
    void roundTrip_QuantityType_temperature() throws SQLException {
        storeAndVerify("TempSensor", new NumberItem("TempSensor"), new QuantityType<>("23.4 °C"),
                s -> assertInstanceOf(QuantityType.class, s), s -> assertEquals("23.4 °C", s.toString()));
    }

    @Test
    @Order(12)
    void roundTrip_OnOffType() throws SQLException {
        storeAndVerify("Switch1", new SwitchItem("Switch1"), OnOffType.ON, s -> assertEquals(OnOffType.ON, s));
        storeAndVerify("Switch2", new SwitchItem("Switch2"), OnOffType.OFF, s -> assertEquals(OnOffType.OFF, s));
    }

    @Test
    @Order(13)
    void roundTrip_OpenClosedType() throws SQLException {
        storeAndVerify("Contact1", new ContactItem("Contact1"), OpenClosedType.OPEN,
                s -> assertEquals(OpenClosedType.OPEN, s));
    }

    @Test
    @Order(14)
    void roundTrip_PercentType() throws SQLException {
        storeAndVerify("Dimmer1", new DimmerItem("Dimmer1"), new PercentType(75),
                s -> assertInstanceOf(PercentType.class, s), s -> assertEquals(75, ((PercentType) s).intValue()));
    }

    @Test
    @Order(15)
    void roundTrip_HSBType() throws SQLException {
        storeAndVerify("Color1", new ColorItem("Color1"), new HSBType("120,50,80"),
                s -> assertInstanceOf(HSBType.class, s), s -> assertEquals("120,50,80", s.toString()));
    }

    @Test
    @Order(16)
    void roundTrip_DateTimeType() throws SQLException {
        ZonedDateTime dt = ZonedDateTime.parse("2024-06-01T10:00:00+02:00");
        storeAndVerify("DateTime1", new DateTimeItem("DateTime1"), new DateTimeType(dt),
                s -> assertInstanceOf(DateTimeType.class, s));
    }

    @Test
    @Order(17)
    void roundTrip_StringType() throws SQLException {
        storeAndVerify("String1", new StringItem("String1"), new StringType("hello world"),
                s -> assertEquals("hello world", s.toString()));
    }

    @Test
    @Order(18)
    void roundTrip_PointType_locationItem() throws SQLException {
        storeAndVerify("Location1", new LocationItem("Location1"), new PointType("52.5200,13.4050,34.0000"),
                s -> assertInstanceOf(PointType.class, s), s -> {
                    PointType pt = (PointType) s;
                    assertEquals(52.52, pt.getLatitude().doubleValue(), 1e-3);
                    assertEquals(13.405, pt.getLongitude().doubleValue(), 1e-3);
                });
    }

    @Test
    @Order(19)
    void roundTrip_PlayPauseType_playerItem() throws SQLException {
        storeAndVerify("Player1", new PlayerItem("Player1"), PlayPauseType.PLAY,
                s -> assertEquals(PlayPauseType.PLAY, s));
        storeAndVerify("Player2", new PlayerItem("Player2"), PlayPauseType.PAUSE,
                s -> assertEquals(PlayPauseType.PAUSE, s));
    }

    @Test
    @Order(20)
    void roundTrip_StringListType_callItem() throws SQLException {
        storeAndVerify("Call1", new CallItem("Call1"), new StringListType("Alice", "Bob"),
                s -> assertInstanceOf(StringListType.class, s),
                s -> assertEquals(new StringListType("Alice", "Bob").toString(), s.toString()));
    }

    @Test
    @Order(21)
    void roundTrip_RawType_imageItem() throws SQLException {
        byte[] bytes = { (byte) 0x89, 0x50, 0x4E, 0x47 }; // PNG magic bytes
        storeAndVerify("Image1", new ImageItem("Image1"), new RawType(bytes, "image/png"),
                s -> assertInstanceOf(RawType.class, s), s -> {
                    RawType raw = (RawType) s;
                    assertEquals("image/png", raw.getMimeType());
                    assertArrayEquals(bytes, raw.getBytes());
                });
    }

    @Test
    @Order(22)
    void roundTrip_UpDownType_rollershutterItem() throws SQLException {
        storeAndVerify("Roller2", new RollershutterItem("Roller2"), new PercentType(30),
                s -> assertInstanceOf(PercentType.class, s), s -> assertEquals(30, ((PercentType) s).intValue()));
    }

    @Test
    @Order(23)
    void roundTrip_GroupItem_withNumberBase() throws SQLException {
        var baseItem = new NumberItem("GBase");
        var groupItem = new GroupItem("Group1", baseItem);
        storeAndVerify("Group1", groupItem, new DecimalType(55.5), s -> assertInstanceOf(DecimalType.class, s),
                s -> assertEquals(55.5, ((DecimalType) s).doubleValue(), 1e-6));
    }

    @Test
    @Order(24)
    void roundTrip_GroupItem_withSwitchBase() throws SQLException {
        var baseItem = new SwitchItem("GBase");
        var groupItem = new GroupItem("GroupSwitch1", baseItem);
        storeAndVerify("GroupSwitch1", groupItem, OnOffType.ON, s -> assertEquals(OnOffType.ON, s));
    }

    @Test
    @Order(25)
    void roundTrip_GroupItem_withColorBase() throws SQLException {
        var baseItem = new ColorItem("GBase");
        var groupItem = new GroupItem("GroupColor1", baseItem);
        storeAndVerify("GroupColor1", groupItem, new HSBType("240,100,50"), s -> assertInstanceOf(HSBType.class, s),
                s -> assertEquals("240,100,50", s.toString()));
    }

    // ------------------------------------------------------------------
    // Query — filtering and ordering
    // ------------------------------------------------------------------

    @Test
    @Order(31)
    void query_dateRange_returnsOnlyRowsInRange() throws SQLException {
        ZonedDateTime t1 = ZonedDateTime.now().minusDays(10);
        ZonedDateTime t2 = ZonedDateTime.now().minusDays(5);
        ZonedDateTime t3 = ZonedDateTime.now().minusDays(1);
        NumberItem item = new NumberItem("RangeSensor");

        try (Connection conn = dataSource.getConnection()) {
            int id = TimescaleDBQuery.getOrCreateItemId(conn, "RangeSensor", null);
            TimescaleDBQuery.insert(conn, id, t1, new TimescaleDBMapper.Row(1.0, null, null));
            TimescaleDBQuery.insert(conn, id, t2, new TimescaleDBMapper.Row(2.0, null, null));
            TimescaleDBQuery.insert(conn, id, t3, new TimescaleDBMapper.Row(3.0, null, null));

            FilterCriteria filter = new FilterCriteria();
            filter.setItemName("RangeSensor");
            filter.setBeginDate(ZonedDateTime.now().minusDays(7));
            filter.setEndDate(ZonedDateTime.now().minusDays(2));

            List<HistoricItem> results = TimescaleDBQuery.query(conn, item, id, filter);
            assertEquals(1, results.size(), "Only t2 should be in range");
            assertEquals(2.0, ((DecimalType) results.get(0).getState()).doubleValue(), 1e-6);
        }
    }

    @Test
    @Order(32)
    void query_ordering_ascending() throws SQLException {
        NumberItem item = new NumberItem("OrderSensor");
        try (Connection conn = dataSource.getConnection()) {
            int id = TimescaleDBQuery.getOrCreateItemId(conn, "OrderSensor", null);
            ZonedDateTime base = ZonedDateTime.now().minusHours(3);
            TimescaleDBQuery.insert(conn, id, base.plusHours(2), new TimescaleDBMapper.Row(3.0, null, null));
            TimescaleDBQuery.insert(conn, id, base, new TimescaleDBMapper.Row(1.0, null, null));
            TimescaleDBQuery.insert(conn, id, base.plusHours(1), new TimescaleDBMapper.Row(2.0, null, null));

            FilterCriteria filter = new FilterCriteria();
            filter.setItemName("OrderSensor");
            filter.setOrdering(Ordering.ASCENDING);

            List<HistoricItem> results = TimescaleDBQuery.query(conn, item, id, filter);
            assertEquals(3, results.size());
            assertEquals(1.0, ((DecimalType) results.get(0).getState()).doubleValue(), 1e-6);
            assertEquals(3.0, ((DecimalType) results.get(2).getState()).doubleValue(), 1e-6);
        }
    }

    @Test
    @Order(33)
    void query_pagination_limitsResults() throws SQLException {
        NumberItem item = new NumberItem("PageSensor");
        try (Connection conn = dataSource.getConnection()) {
            int id = TimescaleDBQuery.getOrCreateItemId(conn, "PageSensor", null);
            ZonedDateTime base = ZonedDateTime.now().minusHours(10);
            for (int i = 0; i < 10; i++) {
                TimescaleDBQuery.insert(conn, id, base.plusHours(i), new TimescaleDBMapper.Row((double) i, null, null));
            }

            FilterCriteria filter = new FilterCriteria();
            filter.setItemName("PageSensor");
            filter.setOrdering(Ordering.ASCENDING);
            filter.setPageSize(3);
            filter.setPageNumber(0);

            List<HistoricItem> page0 = TimescaleDBQuery.query(conn, item, id, filter);
            assertEquals(3, page0.size());

            filter.setPageNumber(1);
            List<HistoricItem> page1 = TimescaleDBQuery.query(conn, item, id, filter);
            assertEquals(3, page1.size());

            // Pages should not overlap
            assertNotEquals(page0.get(0).getTimestamp(), page1.get(0).getTimestamp());
        }
    }

    // ------------------------------------------------------------------
    // remove
    // ------------------------------------------------------------------

    @Test
    @Order(30)
    void remove_deletesByDateRange() throws SQLException {
        NumberItem item = new NumberItem("DeleteSensor");
        try (Connection conn = dataSource.getConnection()) {
            int id = TimescaleDBQuery.getOrCreateItemId(conn, "DeleteSensor", null);
            ZonedDateTime base = ZonedDateTime.now().minusDays(5);
            TimescaleDBQuery.insert(conn, id, base, new TimescaleDBMapper.Row(1.0, null, null));
            TimescaleDBQuery.insert(conn, id, base.plusDays(1), new TimescaleDBMapper.Row(2.0, null, null));
            TimescaleDBQuery.insert(conn, id, base.plusDays(4), new TimescaleDBMapper.Row(3.0, null, null));

            FilterCriteria filter = new FilterCriteria();
            filter.setItemName("DeleteSensor");
            filter.setBeginDate(base.minusMinutes(1));
            filter.setEndDate(base.plusDays(2));

            int deleted = TimescaleDBQuery.remove(conn, id, filter);
            assertEquals(2, deleted, "Should delete the first two rows");

            FilterCriteria allFilter = new FilterCriteria();
            allFilter.setItemName("DeleteSensor");
            List<HistoricItem> remaining = TimescaleDBQuery.query(conn, item, id, allFilter);
            assertEquals(1, remaining.size(), "One row should remain");
            assertEquals(3.0, ((DecimalType) remaining.get(0).getState()).doubleValue(), 1e-6);
        }
    }

    // ------------------------------------------------------------------
    // item_id caching
    // ------------------------------------------------------------------

    @Test
    @Order(40)
    void itemIdCache_sameNameReturnsSameId() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            int id1 = TimescaleDBQuery.getOrCreateItemId(conn, "CacheSensor", "label");
            int id2 = TimescaleDBQuery.getOrCreateItemId(conn, "CacheSensor", "label");
            assertEquals(id1, id2, "Same name should always return the same item_id");
        }
    }

    // ------------------------------------------------------------------
    // Downsampling job (integration)
    // ------------------------------------------------------------------

    @Test
    @Order(50)
    void downsampleJob_aggregatesRawRowsAndDeletesThem() throws SQLException {
        NumberItem item = new NumberItem("DownsampleSensor");

        // Seed 6 raw rows: 3 buckets of 2h each, all older than retainRawDays=0 (use 0 for test)
        // We use retainRawDays=0 so the job processes all rows immediately (NOW() - 0 days)
        ZonedDateTime base = ZonedDateTime.now().minusDays(1);
        try (Connection conn = dataSource.getConnection()) {
            int id = TimescaleDBQuery.getOrCreateItemId(conn, "DownsampleSensor", null);

            // 3 pairs, each pair falls in a different 2h bucket
            for (int bucket = 0; bucket < 3; bucket++) {
                for (int offset = 0; offset < 2; offset++) {
                    TimescaleDBQuery.insert(conn, id, base.plusHours(bucket * 2L + offset),
                            new TimescaleDBMapper.Row((double) (bucket * 10 + offset), null, null));
                }
            }
        }

        // Configure metadata for downsampling with retainRawDays=0
        MetadataRegistry metadataRegistry = mock(MetadataRegistry.class);
        Metadata meta = new Metadata(new MetadataKey("timescaledb", "DownsampleSensor"), "AVG",
                Map.of("downsampleInterval", "2h", "retainRawDays", "0"));
        when(metadataRegistry.get(new MetadataKey("timescaledb", "DownsampleSensor"))).thenReturn(meta);
        when(metadataRegistry.getAll()).thenReturn((java.util.Collection) List.of(meta));

        TimescaleDBMetadataService metaService = new TimescaleDBMetadataService(metadataRegistry);

        // Resolve item_id for the job
        int[] storedId = new int[1];
        try (Connection conn = dataSource.getConnection()) {
            storedId[0] = TimescaleDBQuery.getOrCreateItemId(conn, "DownsampleSensor", null);
        }

        TimescaleDBDownsampleJob job = new TimescaleDBDownsampleJob(dataSource, metaService,
                name -> "DownsampleSensor".equals(name) ? Optional.of(storedId[0]) : Optional.empty());
        job.run();

        // Verify: raw rows gone, aggregated rows present
        try (Connection conn = dataSource.getConnection()) {
            int id = storedId[0];

            // No raw rows should remain (retainRawDays=0 means all raw rows are eligible)
            try (PreparedStatement ps = conn
                    .prepareStatement("SELECT COUNT(*) FROM items WHERE item_id = ? AND downsampled = FALSE")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(0, rs.getInt(1), "No raw rows should remain after downsampling");
                }
            }

            // 3 aggregated rows should exist (one per 2h bucket)
            try (PreparedStatement ps = conn
                    .prepareStatement("SELECT COUNT(*) FROM items WHERE item_id = ? AND downsampled = TRUE")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(3, rs.getInt(1), "3 aggregated rows should exist");
                }
            }
        }
    }

    @Test
    @Order(51)
    void downsampleJob_retentionDays_dropsOldData() throws SQLException {
        ZonedDateTime old = ZonedDateTime.now().minusDays(60);
        ZonedDateTime recent = ZonedDateTime.now().minusHours(1);

        try (Connection conn = dataSource.getConnection()) {
            int id = TimescaleDBQuery.getOrCreateItemId(conn, "RetentionSensor", null);
            TimescaleDBQuery.insert(conn, id, old, new TimescaleDBMapper.Row(1.0, null, null));
            TimescaleDBQuery.insert(conn, id, recent, new TimescaleDBMapper.Row(2.0, null, null));
        }

        MetadataRegistry mr = mock(MetadataRegistry.class);
        // retentionDays=30 → the 60-day-old row should be deleted
        Metadata meta = new Metadata(new MetadataKey("timescaledb", "RetentionSensor"), "AVG",
                Map.of("downsampleInterval", "1h", "retainRawDays", "0", "retentionDays", "30"));
        when(mr.get(new MetadataKey("timescaledb", "RetentionSensor"))).thenReturn(meta);
        when(mr.getAll()).thenReturn((java.util.Collection) List.of(meta));

        TimescaleDBMetadataService ms = new TimescaleDBMetadataService(mr);

        int[] retentionId = new int[1];
        try (Connection conn = dataSource.getConnection()) {
            retentionId[0] = TimescaleDBQuery.getOrCreateItemId(conn, "RetentionSensor", null);
        }

        new TimescaleDBDownsampleJob(dataSource, ms,
                name -> "RetentionSensor".equals(name) ? Optional.of(retentionId[0]) : Optional.empty()).run();

        try (Connection conn = dataSource.getConnection()) {
            FilterCriteria all = new FilterCriteria();
            all.setItemName("RetentionSensor");
            List<HistoricItem> remaining = TimescaleDBQuery.query(conn, new NumberItem("RetentionSensor"),
                    retentionId[0], all);
            // Only the recent row should survive (as a downsampled row)
            assertEquals(1, remaining.size(), "Old data should have been dropped by retention policy");
        }
    }

    // ------------------------------------------------------------------
    // Compression / Retention policies (schema level)
    // ------------------------------------------------------------------

    @Test
    @Order(60)
    void schema_compressionPolicy_isRegistered() throws SQLException {
        // Re-init schema with compression enabled
        try (Connection conn = dataSource.getConnection(); var stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS items CASCADE");
            stmt.execute("DROP TABLE IF EXISTS item_meta CASCADE");
        }
        try (Connection conn = dataSource.getConnection()) {
            TimescaleDBSchema.initialize(conn, "7 days", 30, 0);
        }

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM timescaledb_information.jobs "
                        + "WHERE application_name LIKE '%Compression%' AND hypertable_name = 'items'");
                ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertTrue(rs.getInt(1) > 0, "A compression policy job should be registered");
        }
    }

    @Test
    @Order(61)
    void schema_retentionPolicy_isRegistered() throws SQLException {
        try (Connection conn = dataSource.getConnection(); var stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS items CASCADE");
            stmt.execute("DROP TABLE IF EXISTS item_meta CASCADE");
        }
        try (Connection conn = dataSource.getConnection()) {
            TimescaleDBSchema.initialize(conn, "7 days", 0, 365);
        }

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM timescaledb_information.jobs "
                        + "WHERE application_name LIKE '%Retention%' AND hypertable_name = 'items'");
                ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertTrue(rs.getInt(1) > 0, "A retention policy job should be registered");
        }
    }

    // ------------------------------------------------------------------
    // TimescaleDBPersistenceService — full lifecycle via real DB
    // ------------------------------------------------------------------

    @Test
    @Order(70)
    void service_activate_initializesSchemaAndSchedulesJob() throws Exception {
        MetadataRegistry mr = mock(MetadataRegistry.class);
        when(mr.getAll()).thenReturn(Collections.emptyList());
        TimescaleDBPersistenceService service = new TimescaleDBPersistenceService(mock(ItemRegistry.class),
                new TimescaleDBMetadataService(mr));

        service.activate(Map.of("url", db.getJdbcUrl(), "user", db.getUsername(), "password", db.getPassword()));

        var dsField = TimescaleDBPersistenceService.class.getDeclaredField("dataSource");
        dsField.setAccessible(true);
        assertNotNull(dsField.get(service), "DataSource must be initialized after activate()");

        var jobField = TimescaleDBPersistenceService.class.getDeclaredField("downsampleJob");
        jobField.setAccessible(true);
        assertNotNull(jobField.get(service), "Downsampling job must be scheduled after activate()");

        service.deactivate();
        assertNull(dsField.get(service), "DataSource must be null after deactivate()");
        assertNull(jobField.get(service), "Downsampling job must be null after deactivate()");
    }

    @Test
    @Order(71)
    void service_storeAndQuery_viaServiceInterface() throws Exception {
        MetadataRegistry mr = mock(MetadataRegistry.class);
        when(mr.getAll()).thenReturn(Collections.emptyList());
        ItemRegistry ir = mock(ItemRegistry.class);
        NumberItem item = new NumberItem("ServiceSensor");
        when(ir.getItem("ServiceSensor")).thenReturn(item);

        TimescaleDBPersistenceService service = new TimescaleDBPersistenceService(ir,
                new TimescaleDBMetadataService(mr));
        service.activate(Map.of("url", db.getJdbcUrl(), "user", db.getUsername(), "password", db.getPassword()));

        try {
            service.store(item, ZonedDateTime.now(), new DecimalType(77.7), null);

            FilterCriteria filter = new FilterCriteria();
            filter.setItemName("ServiceSensor");
            List<org.openhab.core.persistence.HistoricItem> results = StreamSupport
                    .stream(service.query(filter).spliterator(), false).toList();

            assertFalse(results.isEmpty(), "Service.query() must return the stored value");
            assertEquals(77.7, ((DecimalType) results.get(0).getState()).doubleValue(), 1e-6);

            assertTrue(service.remove(filter), "Service.remove() must return true for known item");
        } finally {
            service.deactivate();
        }
    }

    @Test
    @Order(72)
    @SuppressWarnings("unchecked")
    void service_deactivate_cancelsScheduledFuture() throws Exception {
        MetadataRegistry mr = mock(MetadataRegistry.class);
        when(mr.getAll()).thenReturn(Collections.emptyList());
        TimescaleDBPersistenceService service = new TimescaleDBPersistenceService(mock(ItemRegistry.class),
                new TimescaleDBMetadataService(mr));
        service.activate(Map.of("url", db.getJdbcUrl(), "user", db.getUsername(), "password", db.getPassword()));

        var jobField = TimescaleDBPersistenceService.class.getDeclaredField("downsampleJob");
        jobField.setAccessible(true);
        ScheduledFuture<Object> job = (ScheduledFuture<Object>) jobField.get(service);
        assertNotNull(job);
        assertFalse(job.isCancelled(), "Job must not be cancelled before deactivate()");

        service.deactivate();
        assertTrue(job.isCancelled(), "Job must be cancelled after deactivate()");
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    @SafeVarargs
    private void storeAndVerify(String itemName, org.openhab.core.items.Item item, State state,
            java.util.function.Consumer<State>... assertions) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            int id = TimescaleDBQuery.getOrCreateItemId(conn, itemName, null);
            TimescaleDBMapper.Row row = TimescaleDBMapper.toRow(state);
            assertNotNull(row, "Mapper must produce a row for state type: " + state.getClass().getSimpleName());
            TimescaleDBQuery.insert(conn, id, ZonedDateTime.now(), row);

            FilterCriteria filter = new FilterCriteria();
            filter.setItemName(itemName);
            List<HistoricItem> results = TimescaleDBQuery.query(conn, item, id, filter);

            assertFalse(results.isEmpty(), "Query should return at least one result for item " + itemName);
            State loadedState = results.get(0).getState();
            for (var assertion : assertions) {
                assertion.accept(loadedState);
            }
        }
    }
}
