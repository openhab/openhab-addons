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
package org.openhab.persistence.influxdb.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;

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

    @ParameterizedTest
    @ValueSource(strings = { "1.12", "25" })
    public void convertDecimalToState(String number) {
        BigDecimal val = new BigDecimal(number);
        NumberItem item = new NumberItem("name");
        assertThat(InfluxDBStateConvertUtils.objectToState(val, item), equalTo(new DecimalType(val)));
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
}
