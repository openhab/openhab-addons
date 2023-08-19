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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.stream.Stream;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.core.items.Item;
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
    void setup() {
        filter = new FilterCriteria();
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForObjectAsStateValid")
    void objectAsStateReturnsValidStateForCompatibleType(Item item, Object value,
            @Nullable Unit<? extends Quantity<?>> unit, Object expected) {
        State actual = jdbcBaseDAO.objectAsState(item, unit, value);
        assertInstanceOf(expected.getClass(), actual, item.getName());
        assertEquals(expected, actual, item.getName());
    }

    private static Stream<Arguments> provideTestCasesForObjectAsStateValid() {
        return Stream.of( //
                Arguments.of(new ImageItem("String_ImageItem"),
                        new RawType(new byte[0], "application/octet-stream").toFullString(), null,
                        new RawType(new byte[0], "application/octet-stream")),
                Arguments.of(new NumberItem("Float_NumberItem"), 7.3, null, DecimalType.valueOf("7.3")),
                Arguments.of(new NumberItem("Float_NumberItem_Unit"), 7.3, SIUnits.CELSIUS,
                        QuantityType.valueOf("7.3 °C")),
                Arguments.of(new ContactItem("String_ContactItem"), "OPEN", null, OpenClosedType.OPEN),
                Arguments.of(new PlayerItem("String_PlayerItem_Play"), "PLAY", null, PlayPauseType.PLAY),
                Arguments.of(new PlayerItem("String_PlayerItem_Rewind"), "REWIND", null, RewindFastforwardType.REWIND),
                Arguments.of(new CallItem("String_CallItem"), "0699222222,0179999998", null,
                        StringListType.valueOf("0699222222,0179999998")),
                Arguments.of(new StringItem("String_StringItem"), "String", null, StringType.valueOf("String")),
                Arguments.of(new SwitchItem("String_SwitchItem"), "ON", null, OnOffType.ON),
                Arguments.of(new DimmerItem("Integer_DimmerItem"), 52, null, PercentType.valueOf("52")),
                Arguments.of(new RollershutterItem("Integer_RollershutterItem"), 39, null, PercentType.valueOf("39")),
                Arguments.of(new ColorItem("CharArray_ColorItem"),
                        new byte[] { (byte) '1', (byte) '8', (byte) '4', (byte) ',', (byte) '1', (byte) '0', (byte) '0',
                                (byte) ',', (byte) '5', (byte) '2' },
                        null, HSBType.valueOf("184,100,52")),
                Arguments.of(new ColorItem("String_ColorItem"), "184,100,52", null, HSBType.valueOf("184,100,52")),
                Arguments.of(new LocationItem("String_LocationItem"), "1,2,3", null, PointType.valueOf("1,2,3")),
                Arguments.of(new DateTimeItem("Timestamp_DateTimeItem"),
                        java.sql.Timestamp.valueOf("2021-02-01 23:30:02.049"), null,
                        DateTimeType.valueOf("2021-02-01T23:30:02.049")),
                Arguments.of(new DateTimeItem("LocalDateTime_DateTimeItem"),
                        LocalDateTime.parse("2021-02-01T23:30:02.049"), null,
                        DateTimeType.valueOf("2021-02-01T23:30:02.049")),
                Arguments.of(new DateTimeItem("Long_DateTimeItem"), Long.valueOf("1612222202049"), null,
                        new DateTimeType(ZonedDateTime.ofInstant(Instant.parse("2021-02-01T23:30:02.049Z"),
                                ZoneId.systemDefault()))),
                Arguments.of(new DateTimeItem("Date_DateTimeItem"), java.sql.Date.valueOf("2021-02-01"), null,
                        DateTimeType.valueOf("2021-02-01T00:00:00.000")),
                Arguments.of(new DateTimeItem("Instant_DateTimeItem"), Instant.parse("2021-02-01T23:30:02.049Z"), null,
                        new DateTimeType(ZonedDateTime.ofInstant(Instant.parse("2021-02-01T23:30:02.049Z"),
                                ZoneId.systemDefault()))),
                Arguments.of(new DateTimeItem("String_DateTimeItem"), "2021-02-01 23:30:02.049", null,
                        DateTimeType.valueOf("2021-02-01T23:30:02.049")));
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForObjectAsStateInvalid")
    void objectAsStateThrowsUnsupportedOperationExceptionForIncompatibleType(Item item, Object value,
            @Nullable Unit<? extends Quantity<?>> unit) {
        assertThrows(UnsupportedOperationException.class, () -> {
            jdbcBaseDAO.objectAsState(item, unit, value);
        }, item.getName());
    }

    private static Stream<Arguments> provideTestCasesForObjectAsStateInvalid() {
        return Stream.of( //
                Arguments.of(new SwitchItem("Integer_SwitchItem"), 1, null),
                Arguments.of(new RollershutterItem("String_RollershutterItem"), "39", null),
                Arguments.of(new ColorItem("Integer_ColorItem"), 5, null), //
                Arguments.of(new NumberItem("Timestamp_NumberItem"), java.sql.Timestamp.valueOf("2023-08-15 21:02:06"),
                        null),
                Arguments.of(new NumberItem("Timestamp_NumberItem_Unit"),
                        java.sql.Timestamp.valueOf("2023-08-15 21:02:06"), SIUnits.CELSIUS),
                Arguments.of(new LocationItem("Timestamp_LocationItem"),
                        java.sql.Timestamp.valueOf("2023-08-15 21:02:06"), null));
    }

    @Test
    void testHistItemFilterQueryProviderReturnsSelectQueryWithoutWhereClauseDescendingOrder() {
        String sql = jdbcBaseDAO.histItemFilterQueryProvider(filter, 0, DB_TABLE_NAME, "TEST", UTC_ZONE_ID);
        assertThat(sql, is("SELECT time, value FROM " + DB_TABLE_NAME + " ORDER BY time DESC"));
    }

    @Test
    void testHistItemFilterQueryProviderReturnsSelectQueryWithoutWhereClauseAscendingOrder() {
        filter.setOrdering(Ordering.ASCENDING);

        String sql = jdbcBaseDAO.histItemFilterQueryProvider(filter, 0, DB_TABLE_NAME, "TEST", UTC_ZONE_ID);
        assertThat(sql, is("SELECT time, value FROM " + DB_TABLE_NAME + " ORDER BY time ASC"));
    }

    @Test
    void testHistItemFilterQueryProviderWithStartAndEndDateReturnsDeleteQueryWithWhereClauseDescendingOrder() {
        filter.setBeginDate(parseDateTimeString("2022-01-10T15:01:44"));
        filter.setEndDate(parseDateTimeString("2022-01-15T15:01:44"));

        String sql = jdbcBaseDAO.histItemFilterQueryProvider(filter, 0, DB_TABLE_NAME, "TEST", UTC_ZONE_ID);
        assertThat(sql, is("SELECT time, value FROM " + DB_TABLE_NAME + " WHERE TIME>='" //
                + JdbcBaseDAO.JDBC_DATE_FORMAT.format(Objects.requireNonNull(filter.getBeginDate())) + "'" //
                + " AND TIME<='" + JdbcBaseDAO.JDBC_DATE_FORMAT.format(Objects.requireNonNull(filter.getEndDate()))
                + "' ORDER BY time DESC"));
    }

    @Test
    void testHistItemFilterQueryProviderReturnsSelectQueryWithoutWhereClauseDescendingOrderAndLimit() {
        filter.setPageSize(1);

        String sql = jdbcBaseDAO.histItemFilterQueryProvider(filter, 0, DB_TABLE_NAME, "TEST", UTC_ZONE_ID);
        assertThat(sql, is("SELECT time, value FROM " + DB_TABLE_NAME + " ORDER BY time DESC LIMIT 0,1"));
    }

    @Test
    void testHistItemFilterDeleteProviderReturnsDeleteQueryWithoutWhereClause() {
        String sql = jdbcBaseDAO.histItemFilterDeleteProvider(filter, DB_TABLE_NAME, UTC_ZONE_ID);
        assertThat(sql, is("TRUNCATE TABLE " + DB_TABLE_NAME));
    }

    @Test
    void testHistItemFilterDeleteProviderWithStartAndEndDateReturnsDeleteQueryWithWhereClause() {
        filter.setBeginDate(parseDateTimeString("2022-01-10T15:01:44"));
        filter.setEndDate(parseDateTimeString("2022-01-15T15:01:44"));

        String sql = jdbcBaseDAO.histItemFilterDeleteProvider(filter, DB_TABLE_NAME, UTC_ZONE_ID);
        assertThat(sql, is("DELETE FROM " + DB_TABLE_NAME + " WHERE TIME>='" //
                + JdbcBaseDAO.JDBC_DATE_FORMAT.format(Objects.requireNonNull(filter.getBeginDate())) + "'" //
                + " AND TIME<='" + JdbcBaseDAO.JDBC_DATE_FORMAT.format(Objects.requireNonNull(filter.getEndDate()))
                + "'"));
    }

    @Test
    void testResolveTimeFilterWithNoDatesReturnsEmptyString() {
        String sql = jdbcBaseDAO.resolveTimeFilter(filter, UTC_ZONE_ID);
        assertThat(sql, is(""));
    }

    @Test
    void testResolveTimeFilterWithStartDateOnlyReturnsWhereClause() {
        filter.setBeginDate(parseDateTimeString("2022-01-10T15:01:44"));

        String sql = jdbcBaseDAO.resolveTimeFilter(filter, UTC_ZONE_ID);
        assertThat(sql, is(" WHERE TIME>='"
                + JdbcBaseDAO.JDBC_DATE_FORMAT.format(Objects.requireNonNull(filter.getBeginDate())) + "'"));
    }

    @Test
    void testResolveTimeFilterWithEndDateOnlyReturnsWhereClause() {
        filter.setEndDate(parseDateTimeString("2022-01-15T15:01:44"));

        String sql = jdbcBaseDAO.resolveTimeFilter(filter, UTC_ZONE_ID);
        assertThat(sql, is(" WHERE TIME<='"
                + JdbcBaseDAO.JDBC_DATE_FORMAT.format(Objects.requireNonNull(filter.getEndDate())) + "'"));
    }

    @Test
    void testResolveTimeFilterWithStartAndEndDateReturnsWhereClauseWithTwoConditions() {
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
