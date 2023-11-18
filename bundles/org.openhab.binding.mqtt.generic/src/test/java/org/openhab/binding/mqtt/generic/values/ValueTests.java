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
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
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
        assertThrows(IllegalArgumentException.class, () -> v.parseCommand(p(v, "three")));
    }

    @Test
    public void textStateUpdate() {
        TextValue v = new TextValue("one,two".split(","));
        v.parseCommand(p(v, "one"));
    }

    @Test
    public void colorUpdate() {
        ColorValue v = new ColorValue(ColorMode.RGB, "fancyON", "fancyOFF", 77);
        v.update((State) v.parseCommand(p(v, "255,255,255")));

        HSBType hsb = (HSBType) v.parseCommand(p(v, "OFF"));
        assertThat(hsb.getBrightness().intValue(), is(0));
        v.update(hsb);
        hsb = (HSBType) v.parseCommand(p(v, "ON"));
        assertThat(hsb.getBrightness().intValue(), is(77));

        hsb = (HSBType) v.parseCommand(p(v, "0"));
        assertThat(hsb.getBrightness().intValue(), is(0));
        hsb = (HSBType) v.parseCommand(p(v, "1"));
        assertThat(hsb.getBrightness().intValue(), is(1));
    }

    @Test
    public void illegalColorUpdate() {
        ColorValue v = new ColorValue(ColorMode.RGB, null, null, 10);
        assertThrows(IllegalArgumentException.class, () -> v.parseCommand(p(v, "255,255,abc")));
    }

    @Test
    public void illegalNumberCommand() {
        NumberValue v = new NumberValue(null, null, null, null);
        assertThrows(IllegalArgumentException.class, () -> v.parseCommand(OnOffType.OFF));
    }

    @Test
    public void illegalPercentCommand() {
        PercentageValue v = new PercentageValue(null, null, null, null, null);
        assertThrows(IllegalStateException.class, () -> v.parseCommand(new StringType("demo")));
    }

    @Test
    public void illegalOnOffCommand() {
        OnOffValue v = new OnOffValue(null, null);
        assertThrows(IllegalArgumentException.class, () -> v.parseCommand(new DecimalType(101.0)));
    }

    @Test
    public void illegalPercentUpdate() {
        PercentageValue v = new PercentageValue(null, null, null, null, null);
        assertThrows(IllegalArgumentException.class, () -> v.parseCommand(new DecimalType(101.0)));
    }

    @Test
    public void onoffUpdate() {
        OnOffValue v = new OnOffValue("fancyON", "fancyOff");

        // Test with command
        assertThat(v.parseCommand(OnOffType.OFF), is(OnOffType.OFF));
        assertThat(v.parseCommand(OnOffType.ON), is(OnOffType.ON));

        // Test with string, representing the command
        assertThat(v.parseCommand(new StringType("OFF")), is(OnOffType.OFF));
        assertThat(v.parseCommand(new StringType("ON")), is(OnOffType.ON));

        // Test with custom string, setup in the constructor
        assertThat(v.parseCommand(new StringType("fancyOff")), is(OnOffType.OFF));
        assertThat(v.parseCommand(new StringType("fancyON")), is(OnOffType.ON));

        // Test basic formatting
        assertThat(v.getMQTTpublishValue(OnOffType.ON, null), is("fancyON"));
        assertThat(v.getMQTTpublishValue(OnOffType.OFF, null), is("fancyOff"));

        // Test custom formatting
        assertThat(v.getMQTTpublishValue(OnOffType.OFF, "=%s"), is("=fancyOff"));
        assertThat(v.getMQTTpublishValue(OnOffType.ON, "=%s"), is("=fancyON"));
    }

    @Test
    public void openCloseUpdate() {
        OpenCloseValue v = new OpenCloseValue("fancyON", "fancyOff");

        // Test with command
        assertThat(v.parseCommand(OpenClosedType.CLOSED), is(OpenClosedType.CLOSED));
        assertThat(v.parseCommand(OpenClosedType.OPEN), is(OpenClosedType.OPEN));

        // Test with string, representing the command
        assertThat(v.parseCommand(new StringType("CLOSED")), is(OpenClosedType.CLOSED));
        assertThat(v.parseCommand(new StringType("OPEN")), is(OpenClosedType.OPEN));

        // Test with custom string, setup in the constructor
        assertThat(v.parseCommand(new StringType("fancyOff")), is(OpenClosedType.CLOSED));
        assertThat(v.parseCommand(new StringType("fancyON")), is(OpenClosedType.OPEN));

        // Test basic formatting
        assertThat(v.getMQTTpublishValue(OpenClosedType.CLOSED, null), is("fancyOff"));
        assertThat(v.getMQTTpublishValue(OpenClosedType.OPEN, null), is("fancyON"));
    }

    @Test
    public void numberUpdate() {
        NumberValue v = new NumberValue(null, null, new BigDecimal(10), Units.WATT);

        // Test with command with units
        Command command = v.parseCommand(new QuantityType<>(20, Units.WATT));
        assertThat(command, is(new QuantityType<>(20, Units.WATT)));
        assertThat(v.getMQTTpublishValue(command, null), is("20"));
        command = v.parseCommand(new QuantityType<>(20, MetricPrefix.KILO(Units.WATT)));
        assertThat(command, is(new QuantityType<>(20, MetricPrefix.KILO(Units.WATT))));
        assertThat(v.getMQTTpublishValue(command, null), is("20000"));

        // Test with command without units
        command = v.parseCommand(new QuantityType<>("20"));
        assertThat(command, is(new QuantityType<>(20, Units.WATT)));
        assertThat(v.getMQTTpublishValue(command, null), is("20"));
    }

    @Test
    public void numberUpdateMireds() {
        NumberValue v = new NumberValue(null, null, new BigDecimal(10), Units.MIRED);

        Command command = v.parseCommand(new QuantityType<>(2700, Units.KELVIN));
        assertThat(v.getMQTTpublishValue(command, "%.0f"), is("370"));
    }

    @Test
    public void numberPercentageUpdate() {
        NumberValue v = new NumberValue(null, null, new BigDecimal(10), Units.PERCENT);

        // Test with command with units
        Command command = v.parseCommand(new QuantityType<>(20, Units.PERCENT));
        assertThat(command, is(new QuantityType<>(20, Units.PERCENT)));
        assertThat(v.getMQTTpublishValue(command, null), is("20"));

        // Test with command without units
        command = v.parseCommand(new QuantityType<>("20"));
        assertThat(command, is(new QuantityType<>(20, Units.PERCENT)));
        assertThat(v.getMQTTpublishValue(command, null), is("20"));
    }

    @Test
    public void rollershutterUpdateWithStrings() {
        RollershutterValue v = new RollershutterValue("fancyON", "fancyOff", "fancyStop");
        // Test with UP/DOWN/STOP command
        assertThat(v.parseCommand(UpDownType.UP), is(UpDownType.UP));
        assertThat(v.getMQTTpublishValue(UpDownType.UP, null), is("fancyON"));
        assertThat(v.parseCommand(UpDownType.DOWN), is(UpDownType.DOWN));
        assertThat(v.getMQTTpublishValue(UpDownType.DOWN, null), is("fancyOff"));
        assertThat(v.parseCommand(StopMoveType.STOP), is(StopMoveType.STOP));
        assertThat(v.getMQTTpublishValue(StopMoveType.STOP, null), is("fancyStop"));

        // Test with custom string
        assertThat(v.parseCommand(new StringType("fancyON")), is(UpDownType.UP));
        assertThat(v.parseCommand(new StringType("fancyOff")), is(UpDownType.DOWN));

        // Test with exact percent
        Command command = new PercentType(27);
        assertThat(v.parseCommand((Command) command), is(command));
        assertThat(v.getMQTTpublishValue(command, null), is("27"));

        // Test formatting 0/100
        assertThat(v.getMQTTpublishValue(PercentType.ZERO, null), is("fancyON"));
        assertThat(v.getMQTTpublishValue(PercentType.HUNDRED, null), is("fancyOff"));
    }

    @Test
    public void rollershutterUpdateWithOutStrings() {
        RollershutterValue v = new RollershutterValue(null, null, "fancyStop");
        // Test with command
        assertThat(v.parseCommand(UpDownType.UP), is(PercentType.ZERO));
        assertThat(v.parseCommand(UpDownType.DOWN), is(PercentType.HUNDRED));

        // Test with custom string
        // Test formatting 0/100
        assertThat(v.getMQTTpublishValue(PercentType.ZERO, null), is("0"));
        assertThat(v.getMQTTpublishValue(PercentType.HUNDRED, null), is("100"));
    }

    @Test
    public void percentCalc() {
        PercentageValue v = new PercentageValue(new BigDecimal(10.0), new BigDecimal(110.0), new BigDecimal(1.0), null,
                null);
        assertThat(v.parseCommand(new DecimalType("110.0")), is(PercentType.HUNDRED));
        assertThat(v.getMQTTpublishValue(PercentType.HUNDRED, null), is("110"));
        assertThat(v.parseCommand(new DecimalType(10.0)), is(PercentType.ZERO));
        assertThat(v.getMQTTpublishValue(PercentType.ZERO, null), is("10"));

        assertThat(v.parseCommand(OnOffType.ON), is(PercentType.HUNDRED));
        assertThat(v.parseCommand(OnOffType.OFF), is(PercentType.ZERO));
    }

    @Test
    public void percentMQTTValue() {
        PercentageValue v = new PercentageValue(null, null, null, null, null);
        assertThat(v.parseCommand(new DecimalType("10.10000")), is(new PercentType("10.1")));
        assertThat(v.getMQTTpublishValue(new PercentType("10.1"), null), is("10.1"));
        Command command;
        for (int i = 0; i <= 100; i++) {
            command = v.parseCommand(new DecimalType(i));
            assertThat(v.getMQTTpublishValue(command, null), is("" + i));
        }
    }

    @Test
    public void percentCustomOnOff() {
        PercentageValue v = new PercentageValue(new BigDecimal("0.0"), new BigDecimal("100.0"), new BigDecimal("1.0"),
                "on", "off");
        assertThat(v.parseCommand(new StringType("on")), is(PercentType.HUNDRED));
        assertThat(v.parseCommand(new StringType("off")), is(PercentType.ZERO));
    }

    @Test
    public void decimalCalc() {
        PercentageValue v = new PercentageValue(new BigDecimal("0.1"), new BigDecimal("1.0"), new BigDecimal("0.1"),
                null, null);
        assertThat(v.parseCommand(new DecimalType(1.0)), is(PercentType.HUNDRED));
        assertThat(v.parseCommand(new DecimalType(0.1)), is(PercentType.ZERO));
        PercentType command = (PercentType) v.parseCommand(new DecimalType(0.2));
        assertEquals(command.floatValue(), 11.11f, 0.01f);
    }

    @Test
    public void increaseDecreaseCalc() {
        PercentageValue v = new PercentageValue(new BigDecimal("1.0"), new BigDecimal("11.0"), new BigDecimal("0.5"),
                null, null);

        // Normal operation.
        PercentType command = (PercentType) v.parseCommand(new DecimalType("6.0"));
        assertEquals(command.floatValue(), 50.0f, 0.01f);
        v.update(command);
        command = (PercentType) v.parseCommand(IncreaseDecreaseType.INCREASE);
        assertEquals(command.floatValue(), 55.0f, 0.01f);
        command = (PercentType) v.parseCommand(IncreaseDecreaseType.DECREASE);
        assertEquals(command.floatValue(), 45.0f, 0.01f);

        // Lower limit.
        command = (PercentType) v.parseCommand(new DecimalType("1.1"));
        assertEquals(command.floatValue(), 1.0f, 0.01f);
        v.update(command);
        command = (PercentType) v.parseCommand(IncreaseDecreaseType.DECREASE);
        assertEquals(command.floatValue(), 0.0f, 0.01f);

        // Upper limit.
        command = (PercentType) v.parseCommand(new DecimalType("10.8"));
        assertEquals(command.floatValue(), 98.0f, 0.01f);
        v.update(command);
        command = (PercentType) v.parseCommand(IncreaseDecreaseType.INCREASE);
        assertEquals(command.floatValue(), 100.0f, 0.01f);
    }

    @Test
    public void upDownCalc() {
        PercentageValue v = new PercentageValue(new BigDecimal("1.0"), new BigDecimal("11.0"), new BigDecimal("0.5"),
                null, null);

        // Normal operation.
        PercentType command = (PercentType) v.parseCommand(new DecimalType("6.0"));
        assertEquals(command.floatValue(), 50.0f, 0.01f);
        v.update(command);
        command = (PercentType) v.parseCommand(UpDownType.UP);
        assertEquals(command.floatValue(), 55.0f, 0.01f);
        command = (PercentType) v.parseCommand(UpDownType.DOWN);
        assertEquals(command.floatValue(), 45.0f, 0.01f);

        // Lower limit.
        command = (PercentType) v.parseCommand(new DecimalType("1.1"));
        assertEquals(command.floatValue(), 1.0f, 0.01f);
        v.update(command);
        command = (PercentType) v.parseCommand(UpDownType.DOWN);
        assertEquals(command.floatValue(), 0.0f, 0.01f);

        // Upper limit.
        command = (PercentType) v.parseCommand(new DecimalType("10.8"));
        assertEquals(command.floatValue(), 98.0f, 0.01f);
        v.update(command);
        command = (PercentType) v.parseCommand(UpDownType.UP);
        assertEquals(command.floatValue(), 100.0f, 0.01f);
    }

    @Test
    public void percentCalcInvalid() {
        PercentageValue v = new PercentageValue(new BigDecimal(10.0), new BigDecimal(110.0), new BigDecimal(1.0), null,
                null);
        assertThrows(IllegalArgumentException.class, () -> v.parseCommand(new DecimalType(9.0)));
    }
}
