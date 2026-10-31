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
package org.openhab.persistence.victoriametrics.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.RollershutterItem;
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

/**
 * @author Joan Pujol Espinar - Initial contribution
 * @author Michael Weger - Adapted to VictoriaMetrics
 */
@NonNullByDefault
@SuppressWarnings({ "unchecked", "rawtypes" })
public class VictoriaMetricsStateConvertUtilsTest {

    @Test
    public void convertDecimalState() {
        DecimalType decimalType = new DecimalType(new BigDecimal("1.12"));
        assertThat(VictoriaMetricsStateConvertUtils.stateToObject(decimalType), is(new BigDecimal("1.12")));
    }

    @Test
    public void convertIntegerDecimalState() {
        DecimalType decimalType = new DecimalType(12L);
        assertThat(VictoriaMetricsStateConvertUtils.stateToObject(decimalType), is(new BigDecimal("12")));
    }

    @Test
    public void convertQuantityState() {
        QuantityType<?> type = new QuantityType<>(new BigDecimal("21.5"), SIUnits.CELSIUS);
        assertThat(VictoriaMetricsStateConvertUtils.stateToObject(type), is(new BigDecimal("21.5")));
    }

    @Test
    public void convertPercentState() {
        assertThat(VictoriaMetricsStateConvertUtils.stateToObject(new PercentType(42)), is(new BigDecimal("42")));
    }

    @Test
    public void convertOnOffState() {
        assertThat(VictoriaMetricsStateConvertUtils.stateToObject(OpenClosedType.OPEN), equalTo(1));
        assertThat(VictoriaMetricsStateConvertUtils.stateToObject(OpenClosedType.CLOSED), equalTo(0));
        assertThat(VictoriaMetricsStateConvertUtils.stateToObject(OnOffType.ON), equalTo(1));
        assertThat(VictoriaMetricsStateConvertUtils.stateToObject(OnOffType.OFF), equalTo(0));
    }

    @Test
    public void convertDateTimeState() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        long nowInMillis = now.toInstant().toEpochMilli();
        DateTimeType type = new DateTimeType(now);
        assertThat(VictoriaMetricsStateConvertUtils.stateToObject(type), equalTo(nowInMillis));
    }

    @Test
    public void convertNonNumericStatesIsNotSupported() {
        assertThat(VictoriaMetricsStateConvertUtils.stateToObject(new StringType("text")), nullValue());
        assertThat(VictoriaMetricsStateConvertUtils.stateToObject(new HSBType("120,100,50")), nullValue());
        assertThat(VictoriaMetricsStateConvertUtils.stateToObject(new PointType("52.5,13.4")), nullValue());
        assertThat(
                VictoriaMetricsStateConvertUtils.stateToObject(
                        new RawType(new byte[] { 0x64, 0x66, 0x55, 0x00, 0x34 }, RawType.DEFAULT_MIME_TYPE)),
                nullValue());
        assertThat(VictoriaMetricsStateConvertUtils.stateToObject(PlayPauseType.PLAY), nullValue());
        assertThat(VictoriaMetricsStateConvertUtils.stateToObject(RewindFastforwardType.REWIND), nullValue());
        assertThat(VictoriaMetricsStateConvertUtils.stateToObject(new StringListType("a", "b")), nullValue());
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.12", "25" })
    public void convertDecimalToState(String number) {
        UnitProvider unitProviderMock = mock(UnitProvider.class);
        when(unitProviderMock.getUnit(any())).thenReturn((Unit) SIUnits.CELSIUS);
        BigDecimal val = new BigDecimal(number);
        NumberItem plainItem = new NumberItem("plain");
        NumberItem dimensionItem = new NumberItem("Number:Temperature", "dimension", unitProviderMock);
        assertThat(VictoriaMetricsStateConvertUtils.objectToState(val, plainItem), equalTo(new DecimalType(val)));
        assertThat(VictoriaMetricsStateConvertUtils.objectToState(val, dimensionItem),
                equalTo(new QuantityType<>(new BigDecimal(number), SIUnits.CELSIUS)));
    }

    @Test
    public void convertPercentToState() {
        BigDecimal val = new BigDecimal("42");
        assertThat(VictoriaMetricsStateConvertUtils.objectToState(val, new DimmerItem("name")),
                equalTo(new PercentType(42)));
        assertThat(VictoriaMetricsStateConvertUtils.objectToState(val, new RollershutterItem("name")),
                equalTo(new PercentType(42)));
    }

    @Test
    public void convertOnOffToState() {
        boolean val1 = true;
        int val2 = 1;
        double val3 = 1.0;
        SwitchItem onOffItem = new SwitchItem("name");
        ContactItem contactItem = new ContactItem("name");
        assertThat(VictoriaMetricsStateConvertUtils.objectToState(val1, onOffItem), equalTo(OnOffType.ON));
        assertThat(VictoriaMetricsStateConvertUtils.objectToState(val2, onOffItem), equalTo(OnOffType.ON));
        assertThat(VictoriaMetricsStateConvertUtils.objectToState(val3, onOffItem), equalTo(OnOffType.ON));
        assertThat(VictoriaMetricsStateConvertUtils.objectToState(val1, contactItem), equalTo(OpenClosedType.OPEN));
        assertThat(VictoriaMetricsStateConvertUtils.objectToState(val2, contactItem), equalTo(OpenClosedType.OPEN));
        assertThat(VictoriaMetricsStateConvertUtils.objectToState(val3, contactItem), equalTo(OpenClosedType.OPEN));
        assertThat(VictoriaMetricsStateConvertUtils.objectToState(BigDecimal.ZERO, contactItem),
                equalTo(OpenClosedType.CLOSED));
    }

    @Test
    public void convertDateTimeToState() {
        long val = System.currentTimeMillis();
        DateTimeItem item = new DateTimeItem("name");

        DateTimeType expected = new DateTimeType(Instant.ofEpochMilli(val));
        assertThat(VictoriaMetricsStateConvertUtils.objectToState(val, item), equalTo(expected));
        // values are returned as float64, which can use the exponent notation
        assertThat(VictoriaMetricsStateConvertUtils.objectToState(new BigDecimal("1.7e12"), item),
                equalTo(new DateTimeType(Instant.ofEpochMilli(1_700_000_000_000L))));
    }
}
