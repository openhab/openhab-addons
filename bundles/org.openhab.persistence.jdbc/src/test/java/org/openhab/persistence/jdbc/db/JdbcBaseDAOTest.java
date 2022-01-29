/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.persistence.jdbc.db;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Ordering;

/**
 * Tests the {@link JdbcBaseDAO}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class JdbcBaseDAOTest {

    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    private static final DateTimeFormatter DATE_PARSER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");
    private static final String DB_TABLE_NAME = "testitem";

    private final JdbcBaseDAO jdbcBaseDAO = new JdbcBaseDAO();
    private @NonNullByDefault({}) FilterCriteria filter;

    @BeforeEach
    public void setup() {
        filter = new FilterCriteria();
    }

    @Test
    public void testHistItemFilterQueryProviderReturnsSelectQueryWithoutWhereClauseDescendingOrder() {
        String sql = jdbcBaseDAO.histItemFilterQueryProvider(filter, 0, DB_TABLE_NAME, "TEST", UTC_ZONE_ID);
        assertThat(sql, is("SELECT time, value FROM " + DB_TABLE_NAME + " ORDER BY time DESC"));
    }

    @Test
    public void testHistItemFilterQueryProviderReturnsSelectQueryWithoutWhereClauseAscendingOrder() {
        filter.setOrdering(Ordering.ASCENDING);

        String sql = jdbcBaseDAO.histItemFilterQueryProvider(filter, 0, DB_TABLE_NAME, "TEST", UTC_ZONE_ID);
        assertThat(sql, is("SELECT time, value FROM " + DB_TABLE_NAME + " ORDER BY time ASC"));
    }

    @Test
    public void testHistItemFilterQueryProviderWithStartAndEndDateReturnsDeleteQueryWithWhereClauseDescendingOrder() {
        filter.setBeginDate(parseDateTimeString("2022-01-10T15:01:44"));
        filter.setEndDate(parseDateTimeString("2022-01-15T15:01:44"));

        String sql = jdbcBaseDAO.histItemFilterQueryProvider(filter, 0, DB_TABLE_NAME, "TEST", UTC_ZONE_ID);
        assertThat(sql, is("SELECT time, value FROM " + DB_TABLE_NAME + " WHERE TIME>'" //
                + JdbcBaseDAO.JDBC_DATE_FORMAT.format(filter.getBeginDate()) + "'" //
                + " AND TIME<'" + JdbcBaseDAO.JDBC_DATE_FORMAT.format(filter.getEndDate()) + "' ORDER BY time DESC"));
    }

    @Test
    public void testHistItemFilterQueryProviderReturnsSelectQueryWithoutWhereClauseDescendingOrderAndLimit() {
        filter.setPageSize(1);

        String sql = jdbcBaseDAO.histItemFilterQueryProvider(filter, 0, DB_TABLE_NAME, "TEST", UTC_ZONE_ID);
        assertThat(sql, is("SELECT time, value FROM " + DB_TABLE_NAME + " ORDER BY time DESC LIMIT 0,1"));
    }

    @Test
    public void testHistItemFilterDeleteProviderReturnsDeleteQueryWithoutWhereClause() {
        String sql = jdbcBaseDAO.histItemFilterDeleteProvider(filter, DB_TABLE_NAME, UTC_ZONE_ID);
        assertThat(sql, is("TRUNCATE TABLE " + DB_TABLE_NAME));
    }

    @Test
    public void testHistItemFilterDeleteProviderWithStartAndEndDateReturnsDeleteQueryWithWhereClause() {
        filter.setBeginDate(parseDateTimeString("2022-01-10T15:01:44"));
        filter.setEndDate(parseDateTimeString("2022-01-15T15:01:44"));

        String sql = jdbcBaseDAO.histItemFilterDeleteProvider(filter, DB_TABLE_NAME, UTC_ZONE_ID);
        assertThat(sql, is("DELETE FROM " + DB_TABLE_NAME + " WHERE TIME>'" //
                + JdbcBaseDAO.JDBC_DATE_FORMAT.format(filter.getBeginDate()) + "'" //
                + " AND TIME<'" + JdbcBaseDAO.JDBC_DATE_FORMAT.format(filter.getEndDate()) + "'"));
    }

    @Test
    public void testResolveTimeFilterWithNoDatesReturnsEmptyString() {
        String sql = jdbcBaseDAO.resolveTimeFilter(filter, UTC_ZONE_ID);
        assertThat(sql, is(""));
    }

    @Test
    public void testResolveTimeFilterWithStartDateOnlyReturnsWhereClause() {
        filter.setBeginDate(parseDateTimeString("2022-01-10T15:01:44"));

        String sql = jdbcBaseDAO.resolveTimeFilter(filter, UTC_ZONE_ID);
        assertThat(sql, is(" WHERE TIME>'" + JdbcBaseDAO.JDBC_DATE_FORMAT.format(filter.getBeginDate()) + "'"));
    }

    @Test
    public void testResolveTimeFilterWithEndDateOnlyReturnsWhereClause() {
        filter.setEndDate(parseDateTimeString("2022-01-15T15:01:44"));

        String sql = jdbcBaseDAO.resolveTimeFilter(filter, UTC_ZONE_ID);
        assertThat(sql, is(" WHERE TIME<'" + JdbcBaseDAO.JDBC_DATE_FORMAT.format(filter.getEndDate()) + "'"));
    }

    @Test
    public void testResolveTimeFilterWithStartAndEndDateReturnsWhereClauseWithTwoConditions() {
        filter.setBeginDate(parseDateTimeString("2022-01-10T15:01:44"));
        filter.setEndDate(parseDateTimeString("2022-01-15T15:01:44"));

        String sql = jdbcBaseDAO.resolveTimeFilter(filter, UTC_ZONE_ID);
        assertThat(sql, is(" WHERE TIME>'" + JdbcBaseDAO.JDBC_DATE_FORMAT.format(filter.getBeginDate()) + "'" //
                + " AND TIME<'" + JdbcBaseDAO.JDBC_DATE_FORMAT.format(filter.getEndDate()) + "'"));
    }

    private ZonedDateTime parseDateTimeString(String dts) {
        return ZonedDateTime.of(LocalDateTime.parse(dts, DATE_PARSER), UTC_ZONE_ID);
    }
}
