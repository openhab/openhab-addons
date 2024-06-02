/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.persistence.influxdb.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.ImageItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.PlayerItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.unit.SIUnits;

/**
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault
public class InfluxDBStateConvertUtilsTest {

    @Test
    public void convertDecimalState() {
        DecimalType decimalType = new DecimalType(new BigDecimal("1.12"));
        assertThat(InfluxDBStateConvertUtils.stateToObject(decimalType), is(new BigDecimal("1.12")));
    }

    @Test
    public void convertIntegerDecimalState() {
        DecimalType decimalType = new DecimalType(12L);
        assertThat(InfluxDBStateConvertUtils.stateToObject(decimalType), is(new BigDecimal("12")));
    }

    @Test
    public void convertOnOffState() {
        assertThat(InfluxDBStateConvertUtils.stateToObject(OpenClosedType.OPEN), equalTo(1));
        assertThat(InfluxDBStateConvertUtils.stateToObject(OnOffType.ON), equalTo(1));
    }

    @Test
    public void convertDateTimeState() {
        ZonedDateTime now = ZonedDateTime.now();
        long nowInMillis = now.toInstant().toEpochMilli();
        DateTimeType type = new DateTimeType(now);
        assertThat(InfluxDBStateConvertUtils.stateToObject(type), equalTo(nowInMillis));
    }

    @Test
    public void convertImageState() {
        RawType type = new RawType(new byte[] { 0x64, 0x66, 0x55, 0x00, 0x34 }, RawType.DEFAULT_MIME_TYPE);
        assertThat(InfluxDBStateConvertUtils.stateToObject(type), is("data:application/octet-stream;base64,ZGZVADQ="));
    }

    @Test
    public void convertPlayPauseState() {
        assertThat(InfluxDBStateConvertUtils.stateToObject(PlayPauseType.PAUSE), is("PAUSE"));
        assertThat(InfluxDBStateConvertUtils.stateToObject(PlayPauseType.PLAY), is("PLAY"));
    }

    @Test
    public void convertRewindFastForwardState() {
        assertThat(InfluxDBStateConvertUtils.stateToObject(RewindFastforwardType.REWIND), is("REWIND"));
        assertThat(InfluxDBStateConvertUtils.stateToObject(RewindFastforwardType.FASTFORWARD), is("FASTFORWARD"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.12", "25" })
    public void convertDecimalToState(String number) {
        UnitProvider unitProviderMock = mock(UnitProvider.class);
        when(unitProviderMock.getUnit(any())).thenReturn((Unit) SIUnits.CELSIUS);
        BigDecimal val = new BigDecimal(number);
        NumberItem plainItem = new NumberItem("plain");
        NumberItem dimensionItem = new NumberItem("Number:Temperature", "dimension", unitProviderMock);
        assertThat(InfluxDBStateConvertUtils.objectToState(val, plainItem), equalTo(new DecimalType(val)));
        assertThat(InfluxDBStateConvertUtils.objectToState(val, dimensionItem),
                equalTo(new QuantityType<>(new BigDecimal(number), SIUnits.CELSIUS)));
    }

    @Test
    public void convertOnOffToState() {
        boolean val1 = true;
        int val2 = 1;
        double val3 = 1.0;
        SwitchItem onOffItem = new SwitchItem("name");
        ContactItem contactItem = new ContactItem("name");
        assertThat(InfluxDBStateConvertUtils.objectToState(val1, onOffItem), equalTo(OnOffType.ON));
        assertThat(InfluxDBStateConvertUtils.objectToState(val2, onOffItem), equalTo(OnOffType.ON));
        assertThat(InfluxDBStateConvertUtils.objectToState(val3, onOffItem), equalTo(OnOffType.ON));
        assertThat(InfluxDBStateConvertUtils.objectToState(val1, contactItem), equalTo(OpenClosedType.OPEN));
        assertThat(InfluxDBStateConvertUtils.objectToState(val2, contactItem), equalTo(OpenClosedType.OPEN));
        assertThat(InfluxDBStateConvertUtils.objectToState(val3, contactItem), equalTo(OpenClosedType.OPEN));
    }

    @Test
    public void convertDateTimeToState() {
        long val = System.currentTimeMillis();
        DateTimeItem item = new DateTimeItem("name");

        DateTimeType expected = new DateTimeType(
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(val), ZoneId.systemDefault()));
        assertThat(InfluxDBStateConvertUtils.objectToState(val, item), equalTo(expected));
    }

    @Test
    public void convertImageToState() {
        ImageItem item = new ImageItem("name");
        RawType type = new RawType(new byte[] { 0x64, 0x66, 0x55, 0x00, 0x34 }, RawType.DEFAULT_MIME_TYPE);
        assertThat(InfluxDBStateConvertUtils.objectToState("data:application/octet-stream;base64,ZGZVADQ=", item),
                is(type));
    }

    @Test
    public void convertPlayerToState() {
        PlayerItem item = new PlayerItem("name");
        assertThat(InfluxDBStateConvertUtils.objectToState("PLAY", item), is(PlayPauseType.PLAY));
        assertThat(InfluxDBStateConvertUtils.objectToState("PAUSE", item), is(PlayPauseType.PAUSE));
        assertThat(InfluxDBStateConvertUtils.objectToState("REWIND", item), is(RewindFastforwardType.REWIND));
        assertThat(InfluxDBStateConvertUtils.objectToState("FASTFORWARD", item), is(RewindFastforwardType.FASTFORWARD));
    }
}
