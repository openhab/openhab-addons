/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.persistence.influxdb2.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Test;
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
public class InfluxDBStateConvertUtilsTest {

    @Test
    public void convertDecimalState() {
        DecimalType decimalType = new DecimalType(new BigDecimal("1.12"));
        assertThat(InfluxDBStateConvertUtils.stateToString(decimalType), equalTo("1.12"));
        assertThat((Double) InfluxDBStateConvertUtils.stateToObject(decimalType), closeTo(1.12, 0.01));
    }

    @Test
    public void convertOnOffState() {
        assertThat(InfluxDBStateConvertUtils.stateToString(OpenClosedType.OPEN),
                equalTo(InfluxDBStateConvertUtils.DIGITAL_VALUE_ON));
        assertThat(InfluxDBStateConvertUtils.stateToString(OnOffType.ON),
                equalTo(InfluxDBStateConvertUtils.DIGITAL_VALUE_ON));
        assertThat(InfluxDBStateConvertUtils.stateToObject(OpenClosedType.OPEN), equalTo(Boolean.TRUE));
        assertThat(InfluxDBStateConvertUtils.stateToObject(OnOffType.ON), equalTo(Boolean.TRUE));
    }

    @Test
    public void convertDateTimeState() {
        ZonedDateTime now = ZonedDateTime.now();
        long nowInMillis = now.toInstant().toEpochMilli();
        DateTimeType type = new DateTimeType(now);
        assertThat(InfluxDBStateConvertUtils.stateToString(type), equalTo(String.valueOf(nowInMillis)));
        assertThat(InfluxDBStateConvertUtils.stateToObject(type), equalTo(nowInMillis));
    }

    @Test
    public void convertDecimalToState() {
        BigDecimal val = new BigDecimal("1.12");
        NumberItem item = new NumberItem("name");
        assertThat(InfluxDBStateConvertUtils.objectToState(val, item), equalTo(new DecimalType(val)));
    }

    @Test
    public void convertOnOffToState() {
        boolean val1 = true;
        int val2 = 1;
        SwitchItem onOffItem = new SwitchItem("name");
        ContactItem contactItem = new ContactItem("name");
        assertThat(InfluxDBStateConvertUtils.objectToState(val1, onOffItem), equalTo(OnOffType.ON));
        assertThat(InfluxDBStateConvertUtils.objectToState(val2, onOffItem), equalTo(OnOffType.ON));
        assertThat(InfluxDBStateConvertUtils.objectToState(val1, contactItem), equalTo(OpenClosedType.OPEN));
        assertThat(InfluxDBStateConvertUtils.objectToState(val2, contactItem), equalTo(OpenClosedType.OPEN));
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