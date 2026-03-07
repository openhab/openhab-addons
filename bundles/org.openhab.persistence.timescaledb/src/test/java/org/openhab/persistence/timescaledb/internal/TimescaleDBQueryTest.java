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
 */
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
    void insert_setsAllFiveParameters() throws Exception {
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
    void insert_numericNullUsesSetNull() throws Exception {
        var row = new TimescaleDBMapper.Row(null, "hello", null);
        ZonedDateTime now = ZonedDateTime.now();

        TimescaleDBQuery.insert(connection, 3, now, row);

        verify(preparedStatement).setNull(eq(3), anyInt());
    }

    // ------------------------------------------------------------------
    // getOrCreateItemId — existing item (SELECT path)
    // ------------------------------------------------------------------

    @Test
    void getOrCreateItemId_existingItem_returnsFromSelect() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(42);

        int id = TimescaleDBQuery.getOrCreateItemId(connection, "MySensor", "My Sensor Label");

        assertEquals(42, id);
        // Only one PreparedStatement needed (the SELECT)
        verify(connection, times(1)).prepareStatement(contains("SELECT id FROM item_meta"));
    }

    @Test
    void getOrCreateItemId_newItem_insertsAndReturns() throws Exception {
        // First call (SELECT) returns no rows; second (INSERT) returns the new id
        ResultSet selectRs = mock(ResultSet.class);
        ResultSet insertRs = mock(ResultSet.class);
        PreparedStatement selectPs = mock(PreparedStatement.class);
        PreparedStatement insertPs = mock(PreparedStatement.class);

        when(selectRs.next()).thenReturn(false);
        when(insertRs.next()).thenReturn(true);
        when(insertRs.getInt(1)).thenReturn(99);
        when(selectPs.executeQuery()).thenReturn(selectRs);
        when(insertPs.executeQuery()).thenReturn(insertRs);

        when(connection.prepareStatement(contains("SELECT id FROM item_meta"))).thenReturn(selectPs);
        when(connection.prepareStatement(contains("INSERT INTO item_meta"))).thenReturn(insertPs);

        int id = TimescaleDBQuery.getOrCreateItemId(connection, "NewSensor", null);

        assertEquals(99, id);
    }

    // ------------------------------------------------------------------
    // query — result mapping
    // ------------------------------------------------------------------

    @Test
    void query_emptyResult_returnsEmptyList() throws Exception {
        when(resultSet.next()).thenReturn(false);
        var filter = new FilterCriteria();
        filter.setItemName("Sensor");

        List<HistoricItem> result = TimescaleDBQuery.query(connection, new NumberItem("Sensor"), 1, filter);

        assertTrue(result.isEmpty());
    }

    @Test
    void query_singleRow_returnsOneItem() throws Exception {
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
    void query_withUnit_returnsQuantityType() throws Exception {
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
    void query_withSwitchItem_returnsOnOffType() throws Exception {
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
    void query_withDateRange_addsBothDateParams() throws Exception {
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
    void query_ascendingOrder_appendsAsc() throws Exception {
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
    void query_descendingOrder_appendsDesc() throws Exception {
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
    void query_withPagination_addsLimitAndOffset() throws Exception {
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
    // remove
    // ------------------------------------------------------------------

    @Test
    void remove_noDateFilter_onlyFiltersOnItemId() throws Exception {
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
    void remove_withDateRange_addsBothDateParams() throws Exception {
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
