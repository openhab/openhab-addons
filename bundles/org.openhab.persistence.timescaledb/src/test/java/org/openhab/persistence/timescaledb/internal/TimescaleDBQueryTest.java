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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.persistence.HistoricItem;

/**
 * Unit tests for {@link TimescaleDBQuery} using mocked JDBC connections.
 *
 * @author René Ulbricht - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })
@SuppressWarnings("null")
class TimescaleDBQueryTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws Exception {
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // empty result by default
    }

    // ------------------------------------------------------------------
    // insert
    // ------------------------------------------------------------------

    @Test
    void insertSetsallfiveparameters() throws Exception {
        var row = new TimescaleDBMapper.Row(23.4, null, "°C");
        ZonedDateTime now = ZonedDateTime.now();

        TimescaleDBQuery.insert(connection, 5, now, row);

        verify(preparedStatement).setTimestamp(eq(1), any(Timestamp.class));
        verify(preparedStatement).setInt(2, 5);
        verify(preparedStatement).setDouble(3, 23.4);
        verify(preparedStatement).setString(4, null); // string
        verify(preparedStatement).setString(5, "°C"); // unit
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void insertSqlcontainsonconflictdoNothing() throws Exception {
        var capturedSql = new java.util.ArrayList<String>();
        when(connection.prepareStatement(anyString())).thenAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return preparedStatement;
        });

        TimescaleDBQuery.insert(connection, 1, ZonedDateTime.now(), new TimescaleDBMapper.Row(1.0, null, null));

        String sql = capturedSql.get(0);
        assertTrue(sql.contains("ON CONFLICT DO NOTHING"),
                "INSERT must silently ignore duplicate writes via ON CONFLICT DO NOTHING");
        assertFalse(sql.contains("ON CONFLICT ("),
                "ON CONFLICT must NOT specify columns — TimescaleDB hypertables do not support "
                        + "column-inference conflict targets and throw instead of silently dropping");
    }

    @Test
    void insertNumericnullusessetnull() throws Exception {
        var row = new TimescaleDBMapper.Row(null, "hello", null);
        ZonedDateTime now = ZonedDateTime.now();

        TimescaleDBQuery.insert(connection, 3, now, row);

        verify(preparedStatement).setNull(eq(3), anyInt());
    }

    // ------------------------------------------------------------------
    // getOrCreateItemId — UPSERT behaviour
    // ------------------------------------------------------------------

    @Test
    void getOrCreateItemIdUsesUpsertAndReturnsId() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(42);

        int id = TimescaleDBQuery.getOrCreateItemId(connection, "MySensor", "My Sensor Label");

        assertEquals(42, id);
        verify(connection, times(1)).prepareStatement(contains("INSERT INTO item_meta"));
        verify(connection, times(1)).prepareStatement(contains("ON CONFLICT"));
        verify(connection, times(1)).prepareStatement(contains("RETURNING id"));
    }

    @Test
    void getOrCreateItemIdSetsValueParameter() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(7);

        TimescaleDBQuery.getOrCreateItemId(connection, "Sensor", "label", "sensor.temperature", null);

        // Parameter 3 = value (TEXT)
        verify(preparedStatement).setString(3, "sensor.temperature");
    }

    @Test
    void getOrCreateItemIdSetsMetadataJsonParameter() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(7);

        String json = "{\"aggregation\":\"AVG\"}";
        TimescaleDBQuery.getOrCreateItemId(connection, "Sensor", "label", "sensor.temperature", json);

        // Parameter 4 = metadata JSONB — must use setObject with Types.OTHER
        verify(preparedStatement).setObject(eq(4), eq(json), eq(java.sql.Types.OTHER));
    }

    @Test
    void getOrCreateItemIdNullValueAndMetadataPassedAsNull() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(5);

        TimescaleDBQuery.getOrCreateItemId(connection, "Sensor", null);

        verify(preparedStatement).setString(3, null);
        verify(preparedStatement).setObject(eq(4), isNull(), eq(java.sql.Types.OTHER));
    }

    // ------------------------------------------------------------------
    // query — result mapping
    // ------------------------------------------------------------------

    @Test
    void queryEmptyresultReturnsemptylist() throws Exception {
        when(resultSet.next()).thenReturn(false);
        var filter = new FilterCriteria();
        filter.setItemName("Sensor");

        List<HistoricItem> result = TimescaleDBQuery.query(connection, new NumberItem("Sensor"), 1, filter);

        assertTrue(result.isEmpty());
    }

    @Test
    void querySinglerowReturnsoneitem() throws Exception {
        Instant ts = Instant.parse("2024-06-01T12:00:00Z");
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getTimestamp(1)).thenReturn(Timestamp.from(ts));
        when(resultSet.getObject(2)).thenReturn(42.0); // value
        when(resultSet.getString(3)).thenReturn(null); // string
        when(resultSet.getString(4)).thenReturn(null); // unit

        var filter = new FilterCriteria();
        filter.setItemName("Sensor");

        List<HistoricItem> result = TimescaleDBQuery.query(connection, new NumberItem("Sensor"), 1, filter);

        assertEquals(1, result.size());
        assertEquals(new DecimalType(42.0), result.get(0).getState());
        assertEquals(ts, result.get(0).getTimestamp().toInstant());
    }

    @Test
    void queryWithunitReturnsquantitytype() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getTimestamp(1)).thenReturn(Timestamp.from(Instant.now()));
        when(resultSet.getObject(2)).thenReturn(22.5);
        when(resultSet.getString(3)).thenReturn(null);
        when(resultSet.getString(4)).thenReturn("°C");

        var filter = new FilterCriteria();
        filter.setItemName("TempSensor");

        List<HistoricItem> result = TimescaleDBQuery.query(connection, new NumberItem("TempSensor"), 1, filter);

        assertEquals(1, result.size());
        assertEquals("22.5 °C", result.get(0).getState().toString());
    }

    @Test
    void queryWithswitchitemReturnsonofftype() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getTimestamp(1)).thenReturn(Timestamp.from(Instant.now()));
        when(resultSet.getObject(2)).thenReturn(1.0);
        when(resultSet.getString(3)).thenReturn(null);
        when(resultSet.getString(4)).thenReturn(null);

        var filter = new FilterCriteria();
        filter.setItemName("Switch1");

        List<HistoricItem> result = TimescaleDBQuery.query(connection, new SwitchItem("Switch1"), 2, filter);

        assertEquals(OnOffType.ON, result.get(0).getState());
    }

    // ------------------------------------------------------------------
    // query — SQL construction
    // ------------------------------------------------------------------

    @Test
    void queryWithdaterangeAddsbothdateparams() throws Exception {
        var capturedSql = new java.util.ArrayList<String>();
        when(connection.prepareStatement(anyString())).thenAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return preparedStatement;
        });

        var filter = new FilterCriteria();
        filter.setItemName("Sensor");
        filter.setBeginDate(ZonedDateTime.now().minusDays(7));
        filter.setEndDate(ZonedDateTime.now());

        TimescaleDBQuery.query(connection, new NumberItem("Sensor"), 1, filter);

        String sql = capturedSql.get(0);
        assertTrue(sql.contains("time >= ?"), "Should have begin date filter");
        assertTrue(sql.contains("time <= ?"), "Should have end date filter");
    }

    @Test
    void queryAscendingorderAppendsasc() throws Exception {
        var capturedSql = new java.util.ArrayList<String>();
        when(connection.prepareStatement(anyString())).thenAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return preparedStatement;
        });

        var filter = new FilterCriteria();
        filter.setItemName("Sensor");
        filter.setOrdering(Ordering.ASCENDING);

        TimescaleDBQuery.query(connection, new NumberItem("Sensor"), 1, filter);

        assertTrue(capturedSql.get(0).contains("ORDER BY time ASC"));
    }

    @Test
    void queryDescendingorderAppendsdesc() throws Exception {
        var capturedSql = new java.util.ArrayList<String>();
        when(connection.prepareStatement(anyString())).thenAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return preparedStatement;
        });

        var filter = new FilterCriteria();
        filter.setItemName("Sensor");
        filter.setOrdering(Ordering.DESCENDING);

        TimescaleDBQuery.query(connection, new NumberItem("Sensor"), 1, filter);

        assertTrue(capturedSql.get(0).contains("ORDER BY time DESC"));
    }

    @Test
    void queryWithpaginationAddslimitandoffset() throws Exception {
        var capturedSql = new java.util.ArrayList<String>();
        when(connection.prepareStatement(anyString())).thenAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return preparedStatement;
        });

        var filter = new FilterCriteria();
        filter.setItemName("Sensor");
        filter.setPageSize(50);
        filter.setPageNumber(2);

        TimescaleDBQuery.query(connection, new NumberItem("Sensor"), 1, filter);

        String sql = capturedSql.get(0);
        assertTrue(sql.contains("LIMIT ?"), "Should have LIMIT clause");
        assertTrue(sql.contains("OFFSET ?"), "Should have OFFSET clause");
    }

    // ------------------------------------------------------------------
    // findItemId — SELECT-only cache-miss fallback
    // ------------------------------------------------------------------

    @Test
    void findItemIdKnownitemReturnsid() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(77);

        var result = TimescaleDBQuery.findItemId(connection, "KnownSensor");

        assertTrue(result.isPresent());
        assertEquals(77, result.get());
        // Must use the SELECT query — no INSERT
        verify(connection).prepareStatement(contains("SELECT id FROM item_meta"));
        verify(connection, never()).prepareStatement(contains("INSERT"));
    }

    @Test
    void findItemIdUnknownitemReturnsempty() throws Exception {
        when(resultSet.next()).thenReturn(false);

        var result = TimescaleDBQuery.findItemId(connection, "UnknownSensor");

        assertFalse(result.isPresent());
        verify(connection).prepareStatement(contains("SELECT id FROM item_meta"));
        verify(connection, never()).prepareStatement(contains("INSERT"));
    }

    @Test
    void findItemIdSetsNameParameter() throws Exception {
        when(resultSet.next()).thenReturn(false);

        TimescaleDBQuery.findItemId(connection, "MySensor");

        verify(preparedStatement).setString(1, "MySensor");
    }

    // ------------------------------------------------------------------
    // remove
    // ------------------------------------------------------------------

    @Test
    void removeNodatefilterOnlyfiltersonitemid() throws Exception {
        var capturedSql = new java.util.ArrayList<String>();
        when(connection.prepareStatement(anyString())).thenAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return preparedStatement;
        });
        when(preparedStatement.executeUpdate()).thenReturn(5);

        var filter = new FilterCriteria();
        filter.setItemName("Sensor");

        int deleted = TimescaleDBQuery.remove(connection, 3, filter);

        assertEquals(5, deleted);
        assertFalse(capturedSql.get(0).contains("time >="), "Should not contain date filter");
    }

    @Test
    void removeWithdaterangeAddsbothdateparams() throws Exception {
        var capturedSql = new java.util.ArrayList<String>();
        when(connection.prepareStatement(anyString())).thenAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return preparedStatement;
        });
        when(preparedStatement.executeUpdate()).thenReturn(3);

        var filter = new FilterCriteria();
        filter.setItemName("Sensor");
        filter.setBeginDate(ZonedDateTime.now().minusDays(30));
        filter.setEndDate(ZonedDateTime.now());

        TimescaleDBQuery.remove(connection, 1, filter);

        String sql = capturedSql.get(0);
        assertTrue(sql.contains("time >= ?"));
        assertTrue(sql.contains("time <= ?"));
    }
}
