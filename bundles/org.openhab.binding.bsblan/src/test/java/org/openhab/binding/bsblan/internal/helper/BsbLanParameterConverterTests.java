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
package org.openhab.binding.bsblan.internal.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.bsblan.internal.BsbLanBindingConstants.Channels;
import org.openhab.binding.bsblan.internal.api.models.BsbLanApiParameter;

import org.junit.Test;

/**
 * The {@link BsbLanParameterConverterTests} class implements tests 
 * for {@link BsbLanParameterConverter}.
 *
 * @author Peter Schraffl - Initial contribution
 */
public class BsbLanParameterConverterTests {

    @Test
    public void testGetStatesForStringParameter() {
        BsbLanApiParameter parameter = new BsbLanApiParameter();
        parameter.dataType = BsbLanApiParameter.DataType.DT_STRN;
        parameter.description = "Test-Description";
        parameter.name = "Test-Name";
        parameter.unit = "Test-Unit";
        parameter.value = "Test-Value";

        State state = null;

        state = BsbLanParameterConverter.getState(Channels.Parameter.DATATYPE, parameter);
        assertNotNull(state);
        assertEquals(new DecimalType(BsbLanApiParameter.DataType.DT_STRN.getValue()), state.as(DecimalType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.DESCRIPTION, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Description"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.NAME, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Name"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.UNIT, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Unit"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.NUMBER_VALUE, parameter);
        assertNull(state);

        state = BsbLanParameterConverter.getState(Channels.Parameter.STRING_VALUE, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Value"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.SWITCH_VALUE, parameter);
        assertNotNull(state);
        assertEquals(OnOffType.ON, state);
    }

    @Test
    public void testGetStatesForEnumParameterValue1() {
        BsbLanApiParameter parameter = new BsbLanApiParameter();
        parameter.dataType = BsbLanApiParameter.DataType.DT_ENUM;
        parameter.description = "Test-Description";
        parameter.name = "Test-Name";
        parameter.unit = "Test-Unit";
        parameter.value = "1";

        State state = null;

        state = BsbLanParameterConverter.getState(Channels.Parameter.DATATYPE, parameter);
        assertNotNull(state);
        assertEquals(new DecimalType(BsbLanApiParameter.DataType.DT_ENUM.getValue()), state.as(DecimalType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.DESCRIPTION, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Description"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.NAME, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Name"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.UNIT, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Unit"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.NUMBER_VALUE, parameter);
        assertNotNull(state);
        assertEquals(new DecimalType(1), state.as(DecimalType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.STRING_VALUE, parameter);
        assertNotNull(state);
        assertEquals(new StringType("1"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.SWITCH_VALUE, parameter);
        assertNotNull(state);
        assertEquals(OnOffType.ON, state);
    }

    @Test
    public void testGetStatesForEnumParameterValue0() {
        BsbLanApiParameter parameter = new BsbLanApiParameter();
        parameter.dataType = BsbLanApiParameter.DataType.DT_ENUM;
        parameter.description = "Test-Description";
        parameter.name = "Test-Name";
        parameter.unit = "Test-Unit";
        parameter.value = "0";

        State state = null;

        state = BsbLanParameterConverter.getState(Channels.Parameter.DATATYPE, parameter);
        assertNotNull(state);
        assertEquals(new DecimalType(BsbLanApiParameter.DataType.DT_ENUM.getValue()), state.as(DecimalType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.DESCRIPTION, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Description"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.NAME, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Name"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.UNIT, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Unit"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.NUMBER_VALUE, parameter);
        assertNotNull(state);
        assertEquals(new DecimalType(0), state.as(DecimalType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.STRING_VALUE, parameter);
        assertNotNull(state);
        assertEquals(new StringType("0"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.SWITCH_VALUE, parameter);
        assertNotNull(state);
        assertEquals(OnOffType.OFF, state);
    }

    @Test
    public void testGetStatesForValueParameterValue() {
        BsbLanApiParameter parameter = new BsbLanApiParameter();
        parameter.dataType = BsbLanApiParameter.DataType.DT_VALS;
        parameter.description = "Test-Description";
        parameter.name = "Test-Name";
        parameter.unit = "Test-Unit";
        parameter.value = "22.5";

        State state = null;

        state = BsbLanParameterConverter.getState(Channels.Parameter.DATATYPE, parameter);
        assertNotNull(state);
        assertEquals(new DecimalType(BsbLanApiParameter.DataType.DT_VALS.getValue()), state.as(DecimalType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.DESCRIPTION, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Description"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.NAME, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Name"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.UNIT, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Unit"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.NUMBER_VALUE, parameter);
        assertNotNull(state);
        assertEquals(new DecimalType(22.5), state.as(DecimalType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.STRING_VALUE, parameter);
        assertNotNull(state);
        assertEquals(new StringType("22.5"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(Channels.Parameter.SWITCH_VALUE, parameter);
        assertNotNull(state);
        assertEquals(OnOffType.ON, state);
    }

    @Test
    public void testGetStatesEscapesHtml() {
        BsbLanApiParameter parameter = new BsbLanApiParameter();
        parameter.dataType = BsbLanApiParameter.DataType.DT_VALS;
        parameter.description = "Test-Description";
        parameter.name = "Test-Name";
        parameter.unit = "&deg;C";
        parameter.value = "22.5";

        State state = null;

        state = BsbLanParameterConverter.getState(Channels.Parameter.UNIT, parameter);
        assertNotNull(state);
        assertEquals(new StringType("°C"), state.as(StringType.class));
    }

    @Test
    public void testGetValueForReadonlyChannels() {
        assertNull(BsbLanParameterConverter.getValue(Channels.Parameter.DATATYPE, OnOffType.ON));
        assertNull(BsbLanParameterConverter.getValue(Channels.Parameter.DESCRIPTION, OnOffType.ON));
        assertNull(BsbLanParameterConverter.getValue(Channels.Parameter.NAME, OnOffType.ON));
        assertNull(BsbLanParameterConverter.getValue(Channels.Parameter.UNIT, OnOffType.ON));
    }

    @Test
    public void testGetValueForNumberValueChannel() {
        assertNull("1", BsbLanParameterConverter.getValue(Channels.Parameter.NUMBER_VALUE, OnOffType.ON));
        assertNull("0", BsbLanParameterConverter.getValue(Channels.Parameter.NUMBER_VALUE, OnOffType.OFF));
        assertNull(BsbLanParameterConverter.getValue(Channels.Parameter.NUMBER_VALUE, null));
        assertEquals("42", BsbLanParameterConverter.getValue(Channels.Parameter.NUMBER_VALUE, new DecimalType(42)));
        assertEquals("22.5", BsbLanParameterConverter.getValue(Channels.Parameter.NUMBER_VALUE, new DecimalType(22.5)));
        assertNull(BsbLanParameterConverter.getValue(Channels.Parameter.NUMBER_VALUE, new StringType("Not a number value")));
        assertNull(BsbLanParameterConverter.getValue(Channels.Parameter.NUMBER_VALUE, new StringType("")));
    }

    @Test
    public void testGetValueForSwitchValueChannel() {
        assertEquals("1", BsbLanParameterConverter.getValue(Channels.Parameter.SWITCH_VALUE, OnOffType.ON));
        assertEquals("0", BsbLanParameterConverter.getValue(Channels.Parameter.SWITCH_VALUE, OnOffType.OFF));
        assertNull(BsbLanParameterConverter.getValue(Channels.Parameter.SWITCH_VALUE, null));
        assertNull(BsbLanParameterConverter.getValue(Channels.Parameter.SWITCH_VALUE, new DecimalType(1)));
        assertNull(BsbLanParameterConverter.getValue(Channels.Parameter.SWITCH_VALUE, new DecimalType(0)));
        assertNull(BsbLanParameterConverter.getValue(Channels.Parameter.SWITCH_VALUE, new DecimalType(42)));
        assertNull(BsbLanParameterConverter.getValue(Channels.Parameter.SWITCH_VALUE, new DecimalType(22.5)));
        assertNull(BsbLanParameterConverter.getValue(Channels.Parameter.SWITCH_VALUE, new StringType("Not a number value")));
        assertNull(BsbLanParameterConverter.getValue(Channels.Parameter.SWITCH_VALUE, new StringType("")));
    }

    @Test
    public void testGetValueForStringValueChannel() {
        assertEquals("1", BsbLanParameterConverter.getValue(Channels.Parameter.STRING_VALUE, OnOffType.ON));
        assertEquals("0", BsbLanParameterConverter.getValue(Channels.Parameter.STRING_VALUE, OnOffType.OFF));
        assertNull(BsbLanParameterConverter.getValue(Channels.Parameter.STRING_VALUE, null));
        assertEquals("42", BsbLanParameterConverter.getValue(Channels.Parameter.STRING_VALUE, new DecimalType(42)));
        assertEquals("22.5", BsbLanParameterConverter.getValue(Channels.Parameter.STRING_VALUE,  new DecimalType(22.5)));
        assertEquals("A string value", BsbLanParameterConverter.getValue(Channels.Parameter.STRING_VALUE, new StringType("A string value")));
        assertEquals("", BsbLanParameterConverter.getValue(Channels.Parameter.STRING_VALUE, new StringType("")));
    }
}