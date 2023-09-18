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
package org.openhab.persistence.mongodb.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openhab.persistence.mongodb.internal.MongoDBConvertUtils.FIELD_VALUE;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.measure.Unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.ImageItem;
import org.openhab.core.library.items.NumberItem;
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
import org.openhab.core.types.State;

import com.mongodb.BasicDBObject;

/**
 * Conversion logic between openHAB {@link State} types and MongoDB store types
 *
 * @author Konrad Zawadka - Initial contribution, based on previous work from Joan Pujol Espinar, Theo Weiss and Dominik
 *         Vorreiter
 */
class MongoDBConvertUtilsTest {

    @Test
    public void convertDecimalState() {
        var decimalType = new DecimalType(new BigDecimal("1.12"));
        assertThat(MongoDBConvertUtils.stateToObject(decimalType), is(new BigDecimal("1.12")));
    }

    @Test
    public void convertQuantityTypeState() {
        var temperatureState = QuantityType.valueOf("7.3 °C");
        assertThat(MongoDBConvertUtils.stateToObject(temperatureState), equalTo(new BigDecimal("7.3")));
    }

    @Test
    public void convertIntegerDecimalState() {
        var decimalType = new DecimalType(12L);
        assertThat(MongoDBConvertUtils.stateToObject(decimalType), is(new BigDecimal("12")));
    }

    @Test
    public void convertOnOffState() {
        assertThat(MongoDBConvertUtils.stateToObject(OpenClosedType.OPEN), equalTo(1));
        assertThat(MongoDBConvertUtils.stateToObject(OnOffType.ON), equalTo(1));
    }

    @Test
    public void convertDateTimeState() {
        var now = ZonedDateTime.now();
        long nowInMillis = now.toInstant().toEpochMilli();
        DateTimeType type = new DateTimeType(now);
        assertThat(MongoDBConvertUtils.stateToObject(type), equalTo(nowInMillis));
    }

    @Test
    public void convertImageState() {
        var type = new RawType(new byte[] { 0x64, 0x66, 0x55, 0x00, 0x34 }, RawType.DEFAULT_MIME_TYPE);
        assertThat(MongoDBConvertUtils.stateToObject(type), is("data:application/octet-stream;base64,ZGZVADQ="));
    }

    @Test
    public void convertPlayPauseState() {
        assertThat(MongoDBConvertUtils.stateToObject(PlayPauseType.PAUSE), is("PAUSE"));
        assertThat(MongoDBConvertUtils.stateToObject(PlayPauseType.PLAY), is("PLAY"));
    }

    @Test
    public void convertRewindFastForwardState() {
        assertThat(MongoDBConvertUtils.stateToObject(RewindFastforwardType.REWIND), is("REWIND"));
        assertThat(MongoDBConvertUtils.stateToObject(RewindFastforwardType.FASTFORWARD), is("FASTFORWARD"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.12", "25" })
    public void convertDecimalToState(String number) {
        UnitProvider unitProviderMock = mock(UnitProvider.class);
        when(unitProviderMock.getUnit(any())).thenReturn((Unit) SIUnits.CELSIUS);
        BigDecimal val = new BigDecimal(number);
        NumberItem plainItem = new NumberItem("plain");
        NumberItem dimensionItem = new NumberItem("Number:Temperature", "dimension", unitProviderMock);
        assertThat(MongoDBConvertUtils.objectToState(basicDbObjectOf(val), plainItem), equalTo(new DecimalType(val)));
        assertThat(MongoDBConvertUtils.objectToState(basicDbObjectOf(val), dimensionItem),
                equalTo(new QuantityType<>(new BigDecimal(number), SIUnits.CELSIUS)));
    }

    @Test
    public void convertOnOffToState() {
        boolean val1 = true;
        int val2 = 1;
        double val3 = 1.0;
        SwitchItem onOffItem = new SwitchItem("name");
        ContactItem contactItem = new ContactItem("name");
        assertThat(MongoDBConvertUtils.objectToState(basicDbObjectOf(val1), onOffItem), equalTo(OnOffType.ON));
        assertThat(MongoDBConvertUtils.objectToState(basicDbObjectOf(val3), onOffItem), equalTo(OnOffType.ON));
        assertThat(MongoDBConvertUtils.objectToState(basicDbObjectOf(val2), onOffItem), equalTo(OnOffType.ON));
        assertThat(MongoDBConvertUtils.objectToState(basicDbObjectOf(val1), contactItem), equalTo(OpenClosedType.OPEN));
        assertThat(MongoDBConvertUtils.objectToState(basicDbObjectOf(val2), contactItem), equalTo(OpenClosedType.OPEN));
        assertThat(MongoDBConvertUtils.objectToState(basicDbObjectOf(val3), contactItem), equalTo(OpenClosedType.OPEN));
    }

    @Test
    public void convertDateTimeToState() {
        long val = System.currentTimeMillis();
        DateTimeItem item = new DateTimeItem("name");

        DateTimeType expected = new DateTimeType(
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(val), ZoneId.systemDefault()));
        assertThat(MongoDBConvertUtils.objectToState(basicDbObjectOf(val), item), equalTo(expected));
        assertThat(MongoDBConvertUtils.objectToState(basicDbObjectOf(Date.from(Instant.ofEpochMilli(val))), item),
                equalTo(expected));
    }

    @Test
    public void convertImageToState() {
        ImageItem item = new ImageItem("name");
        RawType type = new RawType(new byte[] { 0x64, 0x66, 0x55, 0x00, 0x34 }, RawType.DEFAULT_MIME_TYPE);
        BasicDBObject obj = basicDbObjectOf("data:application/octet-stream;base64,ZGZVADQ=");
        assertThat(MongoDBConvertUtils.objectToState(obj, item), is(type));
    }

    private BasicDBObject basicDbObjectOf(Object value) {
        return new BasicDBObject(FIELD_VALUE, value);
    }
}
