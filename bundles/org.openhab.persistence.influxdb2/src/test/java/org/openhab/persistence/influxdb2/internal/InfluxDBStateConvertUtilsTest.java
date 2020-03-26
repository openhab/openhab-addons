package org.openhab.persistence.influxdb2.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;

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
        var nowInMillis = now.toInstant().toEpochMilli();
        var type = new DateTimeType(now);
        assertThat(InfluxDBStateConvertUtils.stateToString(type), equalTo(String.valueOf(nowInMillis)));
        assertThat(InfluxDBStateConvertUtils.stateToObject(type), equalTo(nowInMillis));
    }

    @Test
    public void convertDecimalToState() {
        var val = new BigDecimal("1.12");
        var item = new NumberItem("name");
        assertThat(InfluxDBStateConvertUtils.objectToState(val, item), equalTo(new DecimalType(val)));
    }

    @Test
    public void convertOnOffToState() {
        var val1 = true;
        var val2 = 1;
        var onOffItem = new SwitchItem("name");
        var contactItem = new ContactItem("name");
        assertThat(InfluxDBStateConvertUtils.objectToState(val1, onOffItem), equalTo(OnOffType.ON));
        assertThat(InfluxDBStateConvertUtils.objectToState(val2, onOffItem), equalTo(OnOffType.ON));
        assertThat(InfluxDBStateConvertUtils.objectToState(val1, contactItem), equalTo(OpenClosedType.OPEN));
        assertThat(InfluxDBStateConvertUtils.objectToState(val2, contactItem), equalTo(OpenClosedType.OPEN));
    }

    @Test
    public void convertDateTimeToState() {
        var val = System.currentTimeMillis();
        var item = new DateTimeItem("name");

        DateTimeType expected = new DateTimeType(
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(val), ZoneId.systemDefault()));
        assertThat(InfluxDBStateConvertUtils.objectToState(val, item), equalTo(expected));
    }
}