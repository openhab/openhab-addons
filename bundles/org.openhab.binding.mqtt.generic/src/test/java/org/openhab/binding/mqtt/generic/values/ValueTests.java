/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.TypeParser;
import org.junit.Test;
import org.openhab.binding.mqtt.generic.values.ColorValue;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.OpenCloseValue;
import org.openhab.binding.mqtt.generic.values.PercentageValue;
import org.openhab.binding.mqtt.generic.values.RollershutterValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.generic.values.Value;

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
public class ValueTests {
    Command p(Value v, String str) {
        return TypeParser.parseCommand(v.getSupportedCommandTypes(), str);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalTextStateUpdate() {
        TextValue v = new TextValue("one,two".split(","));
        v.update(p(v, "three"));
    }

    public void textStateUpdate() {
        TextValue v = new TextValue("one,two".split(","));
        v.update(p(v, "one"));
    }

    public void colorUpdate() {
        ColorValue v = new ColorValue(true, "fancyON", "fancyOFF", 77);
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

    @Test(expected = IllegalArgumentException.class)
    public void illegalColorUpdate() {
        ColorValue v = new ColorValue(true, null, null, 10);
        v.update(p(v, "255,255,abc"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalNumberCommand() {
        NumberValue v = new NumberValue(null, null, null);
        v.update(OnOffType.OFF);
    }

    @Test(expected = IllegalStateException.class)
    public void illegalPercentCommand() {
        PercentageValue v = new PercentageValue(null, null, null, null, null);
        v.update(new StringType("demo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalOnOffCommand() {
        OnOffValue v = new OnOffValue(null, null);
        v.update(new DecimalType(101.0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalPercentUpdate() {
        PercentageValue v = new PercentageValue(null, null, null, null, null);
        v.update(new DecimalType(101.0));
    }

    @Test
    public void onoffUpdate() {
        OnOffValue v = new OnOffValue("fancyON", "fancyOff");
        // Test with command
        v.update(OnOffType.OFF);
        assertThat(v.getMQTTpublishValue(), is("fancyOff"));
        assertThat(v.getChannelState(), is(OnOffType.OFF));
        v.update(OnOffType.ON);
        assertThat(v.getMQTTpublishValue(), is("fancyON"));
        assertThat(v.getChannelState(), is(OnOffType.ON));

        // Test with string, representing the command
        v.update(new StringType("OFF"));
        assertThat(v.getMQTTpublishValue(), is("fancyOff"));
        assertThat(v.getChannelState(), is(OnOffType.OFF));
        v.update(new StringType("ON"));
        assertThat(v.getMQTTpublishValue(), is("fancyON"));
        assertThat(v.getChannelState(), is(OnOffType.ON));

        // Test with custom string, setup in the constructor
        v.update(new StringType("fancyOff"));
        assertThat(v.getMQTTpublishValue(), is("fancyOff"));
        assertThat(v.getChannelState(), is(OnOffType.OFF));
        v.update(new StringType("fancyON"));
        assertThat(v.getMQTTpublishValue(), is("fancyON"));
        assertThat(v.getChannelState(), is(OnOffType.ON));
    }

    @Test
    public void openCloseUpdate() {
        OpenCloseValue v = new OpenCloseValue("fancyON", "fancyOff");
        // Test with command
        v.update(OpenClosedType.CLOSED);
        assertThat(v.getMQTTpublishValue(), is("fancyOff"));
        assertThat(v.getChannelState(), is(OpenClosedType.CLOSED));
        v.update(OpenClosedType.OPEN);
        assertThat(v.getMQTTpublishValue(), is("fancyON"));
        assertThat(v.getChannelState(), is(OpenClosedType.OPEN));

        // Test with string, representing the command
        v.update(new StringType("CLOSED"));
        assertThat(v.getMQTTpublishValue(), is("fancyOff"));
        assertThat(v.getChannelState(), is(OpenClosedType.CLOSED));
        v.update(new StringType("OPEN"));
        assertThat(v.getMQTTpublishValue(), is("fancyON"));
        assertThat(v.getChannelState(), is(OpenClosedType.OPEN));

        // Test with custom string, setup in the constructor
        v.update(new StringType("fancyOff"));
        assertThat(v.getMQTTpublishValue(), is("fancyOff"));
        assertThat(v.getChannelState(), is(OpenClosedType.CLOSED));
        v.update(new StringType("fancyON"));
        assertThat(v.getMQTTpublishValue(), is("fancyON"));
        assertThat(v.getChannelState(), is(OpenClosedType.OPEN));
    }

    @Test
    public void rollershutterUpdate() {
        RollershutterValue v = new RollershutterValue("fancyON", "fancyOff", "fancyStop");
        // Test with command
        v.update(UpDownType.UP);
        assertThat(v.getMQTTpublishValue(), is("0"));
        assertThat(v.getChannelState(), is(PercentType.ZERO));
        v.update(UpDownType.DOWN);
        assertThat(v.getMQTTpublishValue(), is("100"));
        assertThat(v.getChannelState(), is(PercentType.HUNDRED));

        // Test with custom string
        v.update(new StringType("fancyON"));
        assertThat(v.getMQTTpublishValue(), is("0"));
        assertThat(v.getChannelState(), is(PercentType.ZERO));
        v.update(new StringType("fancyOff"));
        assertThat(v.getMQTTpublishValue(), is("100"));
        assertThat(v.getChannelState(), is(PercentType.HUNDRED));
        v.update(new PercentType(27));
        assertThat(v.getMQTTpublishValue(), is("27"));
        assertThat(v.getChannelState(), is(new PercentType(27)));
    }

    @Test
    public void percentCalc() {
        PercentageValue v = new PercentageValue(new BigDecimal(10.0), new BigDecimal(110.0), new BigDecimal(1.0), null,
                null);
        v.update(new DecimalType(110.0));
        assertThat((PercentType) v.getChannelState(), is(new PercentType(100)));
        assertThat(v.getMQTTpublishValue(), is("110.0"));
        v.update(new DecimalType(10.0));
        assertThat((PercentType) v.getChannelState(), is(new PercentType(0)));
        assertThat(v.getMQTTpublishValue(), is("10.0"));

        v.update(OnOffType.ON);
        assertThat((PercentType) v.getChannelState(), is(new PercentType(100)));
        v.update(OnOffType.OFF);
        assertThat((PercentType) v.getChannelState(), is(new PercentType(0)));
    }

    @Test
    public void percentCustomOnOff() {
        PercentageValue v = new PercentageValue(new BigDecimal(0.0), new BigDecimal(100.0), new BigDecimal(1.0), "on",
                "off");
        v.update(new StringType("on"));
        assertThat((PercentType) v.getChannelState(), is(new PercentType(100)));
        v.update(new StringType("off"));
        assertThat((PercentType) v.getChannelState(), is(new PercentType(0)));
    }

    @Test
    public void decimalCalc() {
        PercentageValue v = new PercentageValue(new BigDecimal(0.1), new BigDecimal(1.0), new BigDecimal(0.1), null,
                null);
        v.update(new DecimalType(1.0));
        assertThat((PercentType) v.getChannelState(), is(new PercentType(100)));
        v.update(new DecimalType(0.1));
        assertThat((PercentType) v.getChannelState(), is(new PercentType(0)));
        v.update(new DecimalType(0.2));
        assertEquals(((PercentType) v.getChannelState()).floatValue(), 11.11f, 0.01f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void percentCalcInvalid() {
        PercentageValue v = new PercentageValue(new BigDecimal(10.0), new BigDecimal(110.0), new BigDecimal(1.0), null,
                null);
        v.update(new DecimalType(9.0));
    }
}
