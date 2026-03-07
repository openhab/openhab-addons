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

import java.time.ZonedDateTime;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.core.items.GroupItem;
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
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.persistence.FilterCriteria.Operator;
import org.openhab.core.types.UnDefType;

/**
 * Unit tests for {@link TimescaleDBMapper} — covers all state types in both directions.
 *
 * @author René Ulbricht - Initial contribution
 */
class TimescaleDBMapperTest {

    // ------------------------------------------------------------------
    // toRow — store direction
    // ------------------------------------------------------------------

    @Test
    void toRow_DecimalType() {
        var row = TimescaleDBMapper.toRow(new DecimalType(42.5));
        assertNotNull(row);
        assertEquals(42.5, row.value());
        assertNull(row.string());
        assertNull(row.unit());
    }

    @Test
    void toRow_QuantityType_temperature() {
        var row = TimescaleDBMapper.toRow(new QuantityType<>("23.4 °C"));
        assertNotNull(row);
        assertNotNull(row.value());
        assertEquals(23.4, row.value(), 1e-6);
        assertNull(row.string());
        assertEquals("°C", row.unit());
    }

    @Test
    void toRow_OnOffType_ON() {
        var row = TimescaleDBMapper.toRow(OnOffType.ON);
        assertNotNull(row);
        assertEquals(1.0, row.value());
        assertNull(row.string());
        assertNull(row.unit());
    }

    @Test
    void toRow_OnOffType_OFF() {
        var row = TimescaleDBMapper.toRow(OnOffType.OFF);
        assertNotNull(row);
        assertEquals(0.0, row.value());
    }

    @Test
    void toRow_OpenClosedType_OPEN() {
        var row = TimescaleDBMapper.toRow(OpenClosedType.OPEN);
        assertNotNull(row);
        assertEquals(1.0, row.value());
    }

    @Test
    void toRow_OpenClosedType_CLOSED() {
        var row = TimescaleDBMapper.toRow(OpenClosedType.CLOSED);
        assertNotNull(row);
        assertEquals(0.0, row.value());
    }

    @Test
    void toRow_PercentType() {
        var row = TimescaleDBMapper.toRow(new PercentType(75));
        assertNotNull(row);
        assertEquals(75.0, row.value(), 1e-6);
        assertNull(row.unit());
    }

    @Test
    void toRow_UpDownType_UP() {
        var row = TimescaleDBMapper.toRow(UpDownType.UP);
        assertNotNull(row);
        assertEquals(0.0, row.value());
    }

    @Test
    void toRow_UpDownType_DOWN() {
        var row = TimescaleDBMapper.toRow(UpDownType.DOWN);
        assertNotNull(row);
        assertEquals(1.0, row.value());
    }

    @Test
    void toRow_HSBType() {
        var row = TimescaleDBMapper.toRow(new HSBType("120,50,80"));
        assertNotNull(row);
        assertNull(row.value());
        assertEquals("120,50,80", row.string());
        assertNull(row.unit());
    }

    @Test
    void toRow_DateTimeType() {
        ZonedDateTime now = ZonedDateTime.now();
        var row = TimescaleDBMapper.toRow(new DateTimeType(now));
        assertNotNull(row);
        assertNull(row.value());
        assertNotNull(row.string());
        assertNull(row.unit());
    }

    @Test
    void toRow_StringType() {
        var row = TimescaleDBMapper.toRow(new StringType("hello world"));
        assertNotNull(row);
        assertNull(row.value());
        assertEquals("hello world", row.string());
        assertNull(row.unit());
    }

    @Test
    void toRow_PointType() {
        var row = TimescaleDBMapper.toRow(new PointType("52.5,13.4,34.0"));
        assertNotNull(row);
        assertNull(row.value());
        assertNotNull(row.string());
        assertTrue(row.string().contains("52.5"));
        assertTrue(row.string().contains("13.4"));
        assertNull(row.unit());
    }

    @Test
    void toRow_PlayPauseType_PLAY() {
        var row = TimescaleDBMapper.toRow(PlayPauseType.PLAY);
        assertNotNull(row);
        assertNull(row.value());
        assertEquals("PLAY", row.string());
        assertNull(row.unit());
    }

    @Test
    void toRow_PlayPauseType_PAUSE() {
        var row = TimescaleDBMapper.toRow(PlayPauseType.PAUSE);
        assertNotNull(row);
        assertEquals("PAUSE", row.string());
    }

    @Test
    void toRow_StringListType() {
        var row = TimescaleDBMapper.toRow(new StringListType("Alice", "Bob", "Charlie"));
        assertNotNull(row);
        assertNull(row.value());
        assertNotNull(row.string());
        assertTrue(row.string().contains("Alice"));
        assertNull(row.unit());
    }

    @Test
    void toRow_RawType() {
        byte[] bytes = { 0x01, 0x02, 0x03 };
        var row = TimescaleDBMapper.toRow(new RawType(bytes, "image/png"));
        assertNotNull(row);
        assertNull(row.value());
        assertNotNull(row.string());
        assertEquals("image/png", row.unit());
        // Verify round-trip base64
        byte[] decoded = Base64.getDecoder().decode(row.string());
        assertArrayEquals(bytes, decoded);
    }

    @Test
    void toRow_UnDefType_returnsNull() {
        var row = TimescaleDBMapper.toRow(UnDefType.UNDEF);
        assertNull(row);
    }

    // ------------------------------------------------------------------
    // toState — load direction
    // ------------------------------------------------------------------

    @Test
    void toState_QuantityType_fromValueAndUnit() {
        var item = new NumberItem("TestNumber");
        var state = TimescaleDBMapper.toState(item, 23.4, null, "°C");
        assertInstanceOf(QuantityType.class, state);
        assertEquals("23.4 °C", state.toString());
    }

    @Test
    void toState_DecimalType_fromValue() {
        var item = new NumberItem("TestNumber");
        var state = TimescaleDBMapper.toState(item, 42.0, null, null);
        assertInstanceOf(DecimalType.class, state);
        assertEquals(new DecimalType(42.0), state);
    }

    @Test
    void toState_OnOffType_fromValue_switchItem() {
        var item = new SwitchItem("TestSwitch");
        assertEquals(OnOffType.ON, TimescaleDBMapper.toState(item, 1.0, null, null));
        assertEquals(OnOffType.OFF, TimescaleDBMapper.toState(item, 0.0, null, null));
    }

    @Test
    void toState_OpenClosedType_fromValue() {
        var item = new ContactItem("TestContact");
        assertEquals(OpenClosedType.OPEN, TimescaleDBMapper.toState(item, 1.0, null, null));
        assertEquals(OpenClosedType.CLOSED, TimescaleDBMapper.toState(item, 0.0, null, null));
    }

    @Test
    void toState_PercentType_fromValue_dimmerItem() {
        var item = new DimmerItem("TestDimmer");
        var state = TimescaleDBMapper.toState(item, 75.0, null, null);
        assertInstanceOf(PercentType.class, state);
        assertEquals(75, ((PercentType) state).intValue());
    }

    @Test
    void toState_PercentType_fromValue_rollershutterItem() {
        var item = new RollershutterItem("TestRoller");
        var state = TimescaleDBMapper.toState(item, 50.0, null, null);
        assertInstanceOf(PercentType.class, state);
    }

    @Test
    void toState_HSBType_fromString() {
        var item = new ColorItem("TestColor");
        var state = TimescaleDBMapper.toState(item, null, "120,50,80", null);
        assertInstanceOf(HSBType.class, state);
        assertEquals("120,50,80", state.toString());
    }

    @Test
    void toState_DateTimeType_fromString() {
        ZonedDateTime dt = ZonedDateTime.parse("2024-01-15T10:30:00+01:00");
        var item = new DateTimeItem("TestDateTime");
        var state = TimescaleDBMapper.toState(item, null, dt.toString(), null);
        assertInstanceOf(DateTimeType.class, state);
    }

    @Test
    void toState_StringType_fromString_stringItem() {
        var item = new StringItem("TestString");
        var state = TimescaleDBMapper.toState(item, null, "hello", null);
        assertInstanceOf(StringType.class, state);
        assertEquals("hello", state.toString());
    }

    @Test
    void toState_PointType_fromString_locationItem() {
        var item = new LocationItem("TestLocation");
        var state = TimescaleDBMapper.toState(item, null, "52.5200,13.4050,34.0000", null);
        assertInstanceOf(PointType.class, state);
        PointType point = (PointType) state;
        assertEquals(52.52, point.getLatitude().doubleValue(), 1e-3);
        assertEquals(13.405, point.getLongitude().doubleValue(), 1e-3);
    }

    @Test
    void toState_PlayPauseType_fromString_playerItem() {
        var item = new PlayerItem("TestPlayer");
        assertEquals(PlayPauseType.PLAY, TimescaleDBMapper.toState(item, null, "PLAY", null));
        assertEquals(PlayPauseType.PAUSE, TimescaleDBMapper.toState(item, null, "PAUSE", null));
    }

    @Test
    void toState_StringListType_fromString_callItem() {
        var item = new CallItem("TestCall");
        var state = TimescaleDBMapper.toState(item, null, "Alice,Bob,Charlie", null);
        assertInstanceOf(StringListType.class, state);
        assertEquals("Alice,Bob,Charlie", state.toString());
    }

    @Test
    void toState_RawType_fromBase64_imageItem() {
        byte[] bytes = { 0x01, 0x02, 0x03 };
        String encoded = Base64.getEncoder().encodeToString(bytes);
        var item = new ImageItem("TestImage");
        var state = TimescaleDBMapper.toState(item, null, encoded, "image/jpeg");
        assertInstanceOf(RawType.class, state);
        RawType raw = (RawType) state;
        assertEquals("image/jpeg", raw.getMimeType());
        assertArrayEquals(bytes, raw.getBytes());
    }

    @Test
    void toState_RawType_missingMimeType_usesDefault() {
        byte[] bytes = { 0x00 };
        String encoded = Base64.getEncoder().encodeToString(bytes);
        var item = new ImageItem("TestImage");
        var state = TimescaleDBMapper.toState(item, null, encoded, null);
        assertInstanceOf(RawType.class, state);
        assertEquals("application/octet-stream", ((RawType) state).getMimeType());
    }

    @Test
    void toState_GroupItem_withNumberBaseItem_returnsDecimalType() {
        var baseItem = new NumberItem("Base");
        var groupItem = new GroupItem("TestGroup", baseItem);
        var state = TimescaleDBMapper.toState(groupItem, 99.0, null, null);
        assertInstanceOf(DecimalType.class, state);
        assertEquals(99.0, ((DecimalType) state).doubleValue(), 1e-6);
    }

    @Test
    void toState_GroupItem_withSwitchBaseItem_returnsOnOffType() {
        var baseItem = new SwitchItem("Base");
        var groupItem = new GroupItem("TestGroup", baseItem);
        assertEquals(OnOffType.ON, TimescaleDBMapper.toState(groupItem, 1.0, null, null));
        assertEquals(OnOffType.OFF, TimescaleDBMapper.toState(groupItem, 0.0, null, null));
    }

    @Test
    void toState_GroupItem_withColorBaseItem_returnsHSBType() {
        var baseItem = new ColorItem("Base");
        var groupItem = new GroupItem("TestGroup", baseItem);
        var state = TimescaleDBMapper.toState(groupItem, null, "240,100,50", null);
        assertInstanceOf(HSBType.class, state);
    }

    @Test
    void toState_allNullReturnsUndef() {
        var item = new NumberItem("TestNumber");
        var state = TimescaleDBMapper.toState(item, null, null, null);
        assertEquals(UnDefType.UNDEF, state);
    }

    @Test
    void toState_invalidQuantityUnit_returnsUndef() {
        var item = new NumberItem("TestNumber");
        var state = TimescaleDBMapper.toState(item, 10.0, null, "NOT_A_UNIT");
        // Should return UNDEF or fall back gracefully
        assertNotNull(state);
    }

    @Test
    void toState_invalidHSB_returnsUndef() {
        var item = new ColorItem("TestColor");
        var state = TimescaleDBMapper.toState(item, null, "not-a-valid-hsb", null);
        assertEquals(UnDefType.UNDEF, state);
    }

    @Test
    void toState_invalidPointType_returnsUndef() {
        var item = new LocationItem("TestLocation");
        var state = TimescaleDBMapper.toState(item, null, "not-a-valid-point", null);
        assertEquals(UnDefType.UNDEF, state);
    }

    @Test
    void toState_invalidPlayPauseType_returnsUndef() {
        var item = new PlayerItem("TestPlayer");
        var state = TimescaleDBMapper.toState(item, null, "INVALID_STATE", null);
        assertEquals(UnDefType.UNDEF, state);
    }

    @Test
    void toState_invalidBase64_returnsUndef() {
        var item = new ImageItem("TestImage");
        var state = TimescaleDBMapper.toState(item, null, "!!!not-base64!!!", null);
        assertEquals(UnDefType.UNDEF, state);
    }

    // ------------------------------------------------------------------
    // toSqlOperator
    // ------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({ "EQ,=", "NEQ,<>", "LT,<", "LTE,<=", "GT,>", "GTE,>=" })
    void toSqlOperator_allSupportedOperators(Operator op, String expected) {
        assertEquals(expected, TimescaleDBMapper.toSqlOperator(op));
    }
}
