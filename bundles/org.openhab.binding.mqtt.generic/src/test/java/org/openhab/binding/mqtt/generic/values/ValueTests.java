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
package org.openhab.binding.mqtt.generic.values;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mqtt.generic.mapping.ColorMode;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.TypeParser;

/**
 * Test cases for the value classes. They should throw exceptions if the wrong command type is used
 * for an update. The percent value class should raise an exception if the value is out of range.
 *
 * The on/off value class should accept a multitude of values including the custom defined ones.
 *
 * The string value class states are tested.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ValueTests {
    private Command p(Value v, String str) {
        return Objects.requireNonNull(TypeParser.parseCommand(v.getSupportedCommandTypes(), str));
    }

    @Test
    public void illegalTextStateUpdate() {
        TextValue v = new TextValue("one,two".split(","));
        assertThrows(IllegalArgumentException.class, () -> v.update(p(v, "three")));
    }

    public void textStateUpdate() {
        TextValue v = new TextValue("one,two".split(","));
        v.update(p(v, "one"));
    }

    public void colorUpdate() {
        ColorValue v = new ColorValue(ColorMode.RGB, "fancyON", "fancyOFF", 77);
        v.update(p(v, "255, 255, 255"));

        v.update(p(v, "OFF"));
        assertThat(((HSBType) v.getChannelState()).getBrightness().intValue(), is(0));
        v.update(p(v, "ON"));
        assertThat(((HSBType) v.getChannelState()).getBrightness().intValue(), is(77));

        v.update(p(v, "0"));
        assertThat(((HSBType) v.getChannelState()).getBrightness().intValue(), is(0));
        v.update(p(v, "1"));
        assertThat(((HSBType) v.getChannelState()).getBrightness().intValue(), is(1));
    }

    @Test
    public void illegalColorUpdate() {
        ColorValue v = new ColorValue(ColorMode.RGB, null, null, 10);
        assertThrows(IllegalArgumentException.class, () -> v.update(p(v, "255,255,abc")));
    }

    @Test
    public void illegalNumberCommand() {
        NumberValue v = new NumberValue(null, null, null, null);
        assertThrows(IllegalArgumentException.class, () -> v.update(OnOffType.OFF));
    }

    @Test
    public void illegalPercentCommand() {
        PercentageValue v = new PercentageValue(null, null, null, null, null);
        assertThrows(IllegalStateException.class, () -> v.update(new StringType("demo")));
    }

    @Test
    public void illegalOnOffCommand() {
        OnOffValue v = new OnOffValue(null, null);
        assertThrows(IllegalArgumentException.class, () -> v.update(new DecimalType(101.0)));
    }

    @Test
    public void illegalPercentUpdate() {
        PercentageValue v = new PercentageValue(null, null, null, null, null);
        assertThrows(IllegalArgumentException.class, () -> v.update(new DecimalType(101.0)));
    }

    @Test
    public void onoffUpdate() {
        OnOffValue v = new OnOffValue("fancyON", "fancyOff");
        // Test with command
        v.update(OnOffType.OFF);
        assertThat(v.getMQTTpublishValue(null), is("fancyOff"));
        assertThat(v.getChannelState(), is(OnOffType.OFF));
        v.update(OnOffType.ON);
        assertThat(v.getMQTTpublishValue(null), is("fancyON"));
        assertThat(v.getChannelState(), is(OnOffType.ON));

        // Test with string, representing the command
        v.update(new StringType("OFF"));
        assertThat(v.getMQTTpublishValue(null), is("fancyOff"));
        assertThat(v.getChannelState(), is(OnOffType.OFF));
        v.update(new StringType("ON"));
        assertThat(v.getMQTTpublishValue(null), is("fancyON"));
        assertThat(v.getChannelState(), is(OnOffType.ON));

        // Test with custom string, setup in the constructor
        v.update(new StringType("fancyOff"));
        assertThat(v.getMQTTpublishValue(null), is("fancyOff"));
        assertThat(v.getMQTTpublishValue("=%s"), is("=fancyOff"));
        assertThat(v.getChannelState(), is(OnOffType.OFF));
        v.update(new StringType("fancyON"));
        assertThat(v.getMQTTpublishValue(null), is("fancyON"));
        assertThat(v.getMQTTpublishValue("=%s"), is("=fancyON"));
        assertThat(v.getChannelState(), is(OnOffType.ON));
    }

    @Test
    public void openCloseUpdate() {
        OpenCloseValue v = new OpenCloseValue("fancyON", "fancyOff");
        // Test with command
        v.update(OpenClosedType.CLOSED);
        assertThat(v.getMQTTpublishValue(null), is("fancyOff"));
        assertThat(v.getChannelState(), is(OpenClosedType.CLOSED));
        v.update(OpenClosedType.OPEN);
        assertThat(v.getMQTTpublishValue(null), is("fancyON"));
        assertThat(v.getChannelState(), is(OpenClosedType.OPEN));

        // Test with string, representing the command
        v.update(new StringType("CLOSED"));
        assertThat(v.getMQTTpublishValue(null), is("fancyOff"));
        assertThat(v.getChannelState(), is(OpenClosedType.CLOSED));
        v.update(new StringType("OPEN"));
        assertThat(v.getMQTTpublishValue(null), is("fancyON"));
        assertThat(v.getChannelState(), is(OpenClosedType.OPEN));

        // Test with custom string, setup in the constructor
        v.update(new StringType("fancyOff"));
        assertThat(v.getMQTTpublishValue(null), is("fancyOff"));
        assertThat(v.getChannelState(), is(OpenClosedType.CLOSED));
        v.update(new StringType("fancyON"));
        assertThat(v.getMQTTpublishValue(null), is("fancyON"));
        assertThat(v.getChannelState(), is(OpenClosedType.OPEN));
    }

    @Test
    public void numberUpdate() {
        NumberValue v = new NumberValue(null, null, new BigDecimal(10), Units.WATT);

        // Test with command with units
        v.update(new QuantityType<>(20, Units.WATT));
        assertThat(v.getMQTTpublishValue(null), is("20"));
        assertThat(v.getChannelState(), is(new QuantityType<>(20, Units.WATT)));
        v.update(new QuantityType<>(20, MetricPrefix.KILO(Units.WATT)));
        assertThat(v.getMQTTpublishValue(null), is("20000"));
        assertThat(v.getChannelState(), is(new QuantityType<>(20, MetricPrefix.KILO(Units.WATT))));

        // Test with command without units
        v.update(new QuantityType<>("20"));
        assertThat(v.getMQTTpublishValue(null), is("20"));
        assertThat(v.getChannelState(), is(new QuantityType<>(20, Units.WATT)));
    }

    @Test
    public void numberUpdateMireds() {
        NumberValue v = new NumberValue(null, null, new BigDecimal(10), Units.MIRED);

        v.update(new QuantityType<>(2700, Units.KELVIN));
        assertThat(v.getMQTTpublishValue("%.0f"), is("370"));
    }

    @Test
    public void numberPercentageUpdate() {
        NumberValue v = new NumberValue(null, null, new BigDecimal(10), Units.PERCENT);

        // Test with command with units
        v.update(new QuantityType<>(20, Units.PERCENT));
        assertThat(v.getMQTTpublishValue(null), is("20"));
        assertThat(v.getChannelState(), is(new QuantityType<>(20, Units.PERCENT)));

        // Test with command without units
        v.update(new QuantityType<>("20"));
        assertThat(v.getMQTTpublishValue(null), is("20"));
        assertThat(v.getChannelState(), is(new QuantityType<>(20, Units.PERCENT)));
    }

    @Test
    public void rollershutterUpdateWithStrings() {
        RollershutterValue v = new RollershutterValue("fancyON", "fancyOff", "fancyStop");
        // Test with command
        v.update(UpDownType.UP);
        assertThat(v.getMQTTpublishValue(null), is("fancyON"));
        assertThat(v.getChannelState(), is(PercentType.ZERO));
        v.update(UpDownType.DOWN);
        assertThat(v.getMQTTpublishValue(null), is("fancyOff"));
        assertThat(v.getChannelState(), is(PercentType.HUNDRED));

        // Test with custom string
        v.update(new StringType("fancyON"));
        assertThat(v.getMQTTpublishValue(null), is("fancyON"));
        assertThat(v.getChannelState(), is(PercentType.ZERO));
        v.update(new StringType("fancyOff"));
        assertThat(v.getMQTTpublishValue(null), is("fancyOff"));
        assertThat(v.getChannelState(), is(PercentType.HUNDRED));
        v.update(new PercentType(27));
        assertThat(v.getMQTTpublishValue(null), is("27"));
        assertThat(v.getChannelState(), is(new PercentType(27)));
    }

    @Test
    public void rollershutterUpdateWithOutStrings() {
        RollershutterValue v = new RollershutterValue(null, null, "fancyStop");
        // Test with command
        v.update(UpDownType.UP);
        assertThat(v.getMQTTpublishValue(null), is("0"));
        assertThat(v.getChannelState(), is(PercentType.ZERO));
        v.update(UpDownType.DOWN);
        assertThat(v.getMQTTpublishValue(null), is("100"));
        assertThat(v.getChannelState(), is(PercentType.HUNDRED));

        // Test with custom string
        v.update(PercentType.ZERO);
        assertThat(v.getMQTTpublishValue(null), is("0"));
        assertThat(v.getChannelState(), is(PercentType.ZERO));
        v.update(PercentType.HUNDRED);
        assertThat(v.getMQTTpublishValue(null), is("100"));
        assertThat(v.getChannelState(), is(PercentType.HUNDRED));
        v.update(new PercentType(27));
        assertThat(v.getMQTTpublishValue(null), is("27"));
        assertThat(v.getChannelState(), is(new PercentType(27)));
    }

    @Test
    public void percentCalc() {
        PercentageValue v = new PercentageValue(new BigDecimal(10.0), new BigDecimal(110.0), new BigDecimal(1.0), null,
                null);
        v.update(new DecimalType("110.0"));
        assertThat((PercentType) v.getChannelState(), is(new PercentType(100)));
        assertThat(v.getMQTTpublishValue(null), is("110"));
        v.update(new DecimalType(10.0));
        assertThat((PercentType) v.getChannelState(), is(new PercentType(0)));
        assertThat(v.getMQTTpublishValue(null), is("10"));

        v.update(OnOffType.ON);
        assertThat((PercentType) v.getChannelState(), is(new PercentType(100)));
        v.update(OnOffType.OFF);
        assertThat((PercentType) v.getChannelState(), is(new PercentType(0)));
    }

    @Test
    public void percentMQTTValue() {
        PercentageValue v = new PercentageValue(null, null, null, null, null);
        v.update(new DecimalType("10.10000"));
        assertThat(v.getMQTTpublishValue(null), is("10.1"));
        for (int i = 0; i <= 100; i++) {
            v.update(new DecimalType(i));
            assertThat(v.getMQTTpublishValue(null), is("" + i));
        }
    }

    @Test
    public void percentCustomOnOff() {
        PercentageValue v = new PercentageValue(new BigDecimal("0.0"), new BigDecimal("100.0"), new BigDecimal("1.0"),
                "on", "off");
        v.update(new StringType("on"));
        assertThat((PercentType) v.getChannelState(), is(new PercentType(100)));
        v.update(new StringType("off"));
        assertThat((PercentType) v.getChannelState(), is(new PercentType(0)));
    }

    @Test
    public void decimalCalc() {
        PercentageValue v = new PercentageValue(new BigDecimal("0.1"), new BigDecimal("1.0"), new BigDecimal("0.1"),
                null, null);
        v.update(new DecimalType(1.0));
        assertThat((PercentType) v.getChannelState(), is(new PercentType(100)));
        v.update(new DecimalType(0.1));
        assertThat((PercentType) v.getChannelState(), is(new PercentType(0)));
        v.update(new DecimalType(0.2));
        assertEquals(((PercentType) v.getChannelState()).floatValue(), 11.11f, 0.01f);
    }

    @Test
    public void increaseDecreaseCalc() {
        PercentageValue v = new PercentageValue(new BigDecimal("1.0"), new BigDecimal("11.0"), new BigDecimal("0.5"),
                null, null);

        // Normal operation.
        v.update(new DecimalType("6.0"));
        assertEquals(((PercentType) v.getChannelState()).floatValue(), 50.0f, 0.01f);
        v.update(IncreaseDecreaseType.INCREASE);
        assertEquals(((PercentType) v.getChannelState()).floatValue(), 55.0f, 0.01f);
        v.update(IncreaseDecreaseType.DECREASE);
        v.update(IncreaseDecreaseType.DECREASE);
        assertEquals(((PercentType) v.getChannelState()).floatValue(), 45.0f, 0.01f);

        // Lower limit.
        v.update(new DecimalType("1.1"));
        assertEquals(((PercentType) v.getChannelState()).floatValue(), 1.0f, 0.01f);
        v.update(IncreaseDecreaseType.DECREASE);
        assertEquals(((PercentType) v.getChannelState()).floatValue(), 0.0f, 0.01f);

        // Upper limit.
        v.update(new DecimalType("10.8"));
        assertEquals(((PercentType) v.getChannelState()).floatValue(), 98.0f, 0.01f);
        v.update(IncreaseDecreaseType.INCREASE);
        assertEquals(((PercentType) v.getChannelState()).floatValue(), 100.0f, 0.01f);
    }

    @Test
    public void upDownCalc() {
        PercentageValue v = new PercentageValue(new BigDecimal("1.0"), new BigDecimal("11.0"), new BigDecimal("0.5"),
                null, null);

        // Normal operation.
        v.update(new DecimalType("6.0"));
        assertEquals(((PercentType) v.getChannelState()).floatValue(), 50.0f, 0.01f);
        v.update(UpDownType.UP);
        assertEquals(((PercentType) v.getChannelState()).floatValue(), 55.0f, 0.01f);
        v.update(UpDownType.DOWN);
        v.update(UpDownType.DOWN);
        assertEquals(((PercentType) v.getChannelState()).floatValue(), 45.0f, 0.01f);

        // Lower limit.
        v.update(new DecimalType("1.1"));
        assertEquals(((PercentType) v.getChannelState()).floatValue(), 1.0f, 0.01f);
        v.update(UpDownType.DOWN);
        assertEquals(((PercentType) v.getChannelState()).floatValue(), 0.0f, 0.01f);

        // Upper limit.
        v.update(new DecimalType("10.8"));
        assertEquals(((PercentType) v.getChannelState()).floatValue(), 98.0f, 0.01f);
        v.update(UpDownType.UP);
        assertEquals(((PercentType) v.getChannelState()).floatValue(), 100.0f, 0.01f);
    }

    @Test
    public void percentCalcInvalid() {
        PercentageValue v = new PercentageValue(new BigDecimal(10.0), new BigDecimal(110.0), new BigDecimal(1.0), null,
                null);
        assertThrows(IllegalArgumentException.class, () -> v.update(new DecimalType(9.0)));
    }
}
