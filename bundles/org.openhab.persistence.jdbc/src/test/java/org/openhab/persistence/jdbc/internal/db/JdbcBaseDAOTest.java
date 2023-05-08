/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.persistence.jdbc.internal.db;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.types.State;

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
    public void testObjectAsStateReturnsValidState() {
        State decimalType = jdbcBaseDAO.objectAsState(new NumberItem("testNumberItem"), null, 7.3);
        assertInstanceOf(DecimalType.class, decimalType);
        assertEquals(DecimalType.valueOf("7.3"), decimalType);
        State quantityType = jdbcBaseDAO.objectAsState(new NumberItem("testNumberItem"), SIUnits.CELSIUS, 7.3);
        assertInstanceOf(QuantityType.class, quantityType);
        assertEquals(QuantityType.valueOf("7.3 °C"), quantityType);

        State hsbType = jdbcBaseDAO.objectAsState(new ColorItem("testColorItem"), null, "184,100,52");
        assertInstanceOf(HSBType.class, hsbType);
        assertEquals(HSBType.valueOf("184,100,52"), hsbType);

        State percentType = jdbcBaseDAO.objectAsState(new DimmerItem("testDimmerItem"), null, 52);
        assertInstanceOf(PercentType.class, percentType);
        assertEquals(PercentType.valueOf("52"), percentType);

        percentType = jdbcBaseDAO.objectAsState(new RollershutterItem("testRollershutterItem"), null, 39);
        assertInstanceOf(PercentType.class, percentType);
        assertEquals(PercentType.valueOf("39"), percentType);

        State openClosedType = jdbcBaseDAO.objectAsState(new ContactItem("testContactItem"), null, "OPEN");
        assertInstanceOf(OpenClosedType.class, openClosedType);
        assertThat(openClosedType, is(OpenClosedType.OPEN));

        State playPauseType = jdbcBaseDAO.objectAsState(new PlayerItem("testPlayerItem"), null, "PLAY");
        assertInstanceOf(PlayPauseType.class, playPauseType);
        assertThat(playPauseType, is(PlayPauseType.PLAY));
        State rewindFastforwardType = jdbcBaseDAO.objectAsState(new PlayerItem("testPlayerItem"), null, "REWIND");
        assertInstanceOf(RewindFastforwardType.class, rewindFastforwardType);
        assertThat(rewindFastforwardType, is(RewindFastforwardType.REWIND));

        State onOffType = jdbcBaseDAO.objectAsState(new SwitchItem("testSwitchItem"), null, "ON");
        assertInstanceOf(OnOffType.class, onOffType);
        assertThat(onOffType, is(OnOffType.ON));

        State stringListType = jdbcBaseDAO.objectAsState(new CallItem("testCallItem"), null, "0699222222,0179999998");
        assertInstanceOf(StringListType.class, stringListType);
        assertEquals(StringListType.valueOf("0699222222,0179999998"), stringListType);

        State expectedRawType = new RawType(new byte[0], "application/octet-stream");
        State rawType = jdbcBaseDAO.objectAsState(new ImageItem("testImageItem"), null, expectedRawType.toFullString());
        assertInstanceOf(RawType.class, rawType);
        assertThat(rawType, is(expectedRawType));

        State pointType = jdbcBaseDAO.objectAsState(new LocationItem("testLocationItem"), null, "1,2,3");
        assertInstanceOf(PointType.class, pointType);
        assertEquals(PointType.valueOf("1,2,3"), pointType);

        State stringType = jdbcBaseDAO.objectAsState(new StringItem("testStringItem"), null, "String");
        assertInstanceOf(StringType.class, stringType);
        assertEquals(StringType.valueOf("String"), stringType);
    }

    @Test
    public void objectAsStateReturnsValiDateTimeTypeForTimestamp() {
        State dateTimeType = jdbcBaseDAO.objectAsState(new DateTimeItem("testDateTimeItem"), null,
                java.sql.Timestamp.valueOf("2021-02-01 23:30:02.049"));
        assertInstanceOf(DateTimeType.class, dateTimeType);
        assertEquals(DateTimeType.valueOf("2021-02-01T23:30:02.049"), dateTimeType);
    }

    @Test
    public void objectAsStateReturnsValidDateTimeTypeForLocalDateTime() {
        State dateTimeType = jdbcBaseDAO.objectAsState(new DateTimeItem("testDateTimeItem"), null,
                LocalDateTime.parse("2021-02-01T23:30:02.049"));
        assertInstanceOf(DateTimeType.class, dateTimeType);
        assertEquals(DateTimeType.valueOf("2021-02-01T23:30:02.049"), dateTimeType);
    }

    @Test
    public void objectAsStateReturnsValidDateTimeTypeForLong() {
        State dateTimeType = jdbcBaseDAO.objectAsState(new DateTimeItem("testDateTimeItem"), null,
                Long.valueOf("1612222202049"));
        assertInstanceOf(DateTimeType.class, dateTimeType);
        assertEquals(
                new DateTimeType(
                        ZonedDateTime.ofInstant(Instant.parse("2021-02-01T23:30:02.049Z"), ZoneId.systemDefault())),
                dateTimeType);
    }

    @Test
    public void objectAsStateReturnsValidDateTimeTypeForSqlDate() {
        State dateTimeType = jdbcBaseDAO.objectAsState(new DateTimeItem("testDateTimeItem"), null,
                java.sql.Date.valueOf("2021-02-01"));
        assertInstanceOf(DateTimeType.class, dateTimeType);
        assertEquals(DateTimeType.valueOf("2021-02-01T00:00:00.000"), dateTimeType);
    }

    @Test
    public void objectAsStateReturnsValidDateTimeTypeForInstant() {
        State dateTimeType = jdbcBaseDAO.objectAsState(new DateTimeItem("testDateTimeItem"), null,
                Instant.parse("2021-02-01T23:30:02.049Z"));
        assertInstanceOf(DateTimeType.class, dateTimeType);
        assertEquals(
                new DateTimeType(
                        ZonedDateTime.ofInstant(Instant.parse("2021-02-01T23:30:02.049Z"), ZoneId.systemDefault())),
                dateTimeType);
    }

    @Test
    public void objectAsStateReturnsValidDateTimeTypeForString() {
        State dateTimeType = jdbcBaseDAO.objectAsState(new DateTimeItem("testDateTimeItem"), null,
                "2021-02-01 23:30:02.049");
        assertInstanceOf(DateTimeType.class, dateTimeType);
        assertEquals(DateTimeType.valueOf("2021-02-01T23:30:02.049"), dateTimeType);
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
        assertThat(sql, is("SELECT time, value FROM " + DB_TABLE_NAME + " WHERE TIME>='" //
                + JdbcBaseDAO.JDBC_DATE_FORMAT.format(Objects.requireNonNull(filter.getBeginDate())) + "'" //
                + " AND TIME<='" + JdbcBaseDAO.JDBC_DATE_FORMAT.format(Objects.requireNonNull(filter.getEndDate()))
                + "' ORDER BY time DESC"));
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
        assertThat(sql, is("DELETE FROM " + DB_TABLE_NAME + " WHERE TIME>='" //
                + JdbcBaseDAO.JDBC_DATE_FORMAT.format(Objects.requireNonNull(filter.getBeginDate())) + "'" //
                + " AND TIME<='" + JdbcBaseDAO.JDBC_DATE_FORMAT.format(Objects.requireNonNull(filter.getEndDate()))
                + "'"));
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
        assertThat(sql, is(" WHERE TIME>='"
                + JdbcBaseDAO.JDBC_DATE_FORMAT.format(Objects.requireNonNull(filter.getBeginDate())) + "'"));
    }

    @Test
    public void testResolveTimeFilterWithEndDateOnlyReturnsWhereClause() {
        filter.setEndDate(parseDateTimeString("2022-01-15T15:01:44"));

        String sql = jdbcBaseDAO.resolveTimeFilter(filter, UTC_ZONE_ID);
        assertThat(sql, is(" WHERE TIME<='"
                + JdbcBaseDAO.JDBC_DATE_FORMAT.format(Objects.requireNonNull(filter.getEndDate())) + "'"));
    }

    @Test
    public void testResolveTimeFilterWithStartAndEndDateReturnsWhereClauseWithTwoConditions() {
        filter.setBeginDate(parseDateTimeString("2022-01-10T15:01:44"));
        filter.setEndDate(parseDateTimeString("2022-01-15T15:01:44"));

        String sql = jdbcBaseDAO.resolveTimeFilter(filter, UTC_ZONE_ID);
        assertThat(sql,
                is(" WHERE TIME>='" + JdbcBaseDAO.JDBC_DATE_FORMAT.format(Objects.requireNonNull(filter.getBeginDate()))
                        + "'" //
                        + " AND TIME<='"
                        + JdbcBaseDAO.JDBC_DATE_FORMAT.format(Objects.requireNonNull(filter.getEndDate())) + "'"));
    }

    private ZonedDateTime parseDateTimeString(String dts) {
        return ZonedDateTime.of(LocalDateTime.parse(dts, DATE_PARSER), UTC_ZONE_ID);
    }
}
