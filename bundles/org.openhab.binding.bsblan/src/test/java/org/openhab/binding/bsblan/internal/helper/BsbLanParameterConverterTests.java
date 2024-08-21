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
package org.openhab.binding.bsblan.internal.helper;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.bsblan.internal.BsbLanBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bsblan.internal.api.dto.BsbLanApiParameterDTO;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.State;

/**
 * The {@link BsbLanParameterConverterTests} class implements tests
 * for {@link BsbLanParameterConverter}.
 *
 * @author Peter Schraffl - Initial contribution
 */
@NonNullByDefault
public class BsbLanParameterConverterTests {

    @Test
    public void testGetStatesForStringParameter() {
        BsbLanApiParameterDTO parameter = new BsbLanApiParameterDTO();
        parameter.dataType = BsbLanApiParameterDTO.DataType.DT_STRN;
        parameter.description = "Test-Description";
        parameter.name = "Test-Name";
        parameter.unit = "Test-Unit";
        parameter.value = "Test-Value";

        State state = null;

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_DATATYPE, parameter);
        assertNotNull(state);
        assertEquals(new DecimalType(BsbLanApiParameterDTO.DataType.DT_STRN.getValue()), state.as(DecimalType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_DESCRIPTION, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Description"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_NAME, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Name"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_UNIT, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Unit"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_NUMBER_VALUE, parameter);
        assertNull(state);

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_STRING_VALUE, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Value"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_SWITCH_VALUE, parameter);
        assertNotNull(state);
        assertEquals(OnOffType.ON, state);
    }

    @Test
    public void testGetStatesForEnumParameterValue1() {
        BsbLanApiParameterDTO parameter = new BsbLanApiParameterDTO();
        parameter.dataType = BsbLanApiParameterDTO.DataType.DT_ENUM;
        parameter.description = "Test-Description";
        parameter.name = "Test-Name";
        parameter.unit = "Test-Unit";
        parameter.value = "1";

        State state = null;

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_DATATYPE, parameter);
        assertNotNull(state);
        assertEquals(new DecimalType(BsbLanApiParameterDTO.DataType.DT_ENUM.getValue()), state.as(DecimalType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_DESCRIPTION, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Description"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_NAME, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Name"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_UNIT, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Unit"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_NUMBER_VALUE, parameter);
        assertNotNull(state);
        assertEquals(new DecimalType(1), state.as(DecimalType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_STRING_VALUE, parameter);
        assertNotNull(state);
        assertEquals(new StringType("1"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_SWITCH_VALUE, parameter);
        assertNotNull(state);
        assertEquals(OnOffType.ON, state);
    }

    @Test
    public void testGetStatesForEnumParameterValue0() {
        BsbLanApiParameterDTO parameter = new BsbLanApiParameterDTO();
        parameter.dataType = BsbLanApiParameterDTO.DataType.DT_ENUM;
        parameter.description = "Test-Description";
        parameter.name = "Test-Name";
        parameter.unit = "Test-Unit";
        parameter.value = "0";

        State state = null;

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_DATATYPE, parameter);
        assertNotNull(state);
        assertEquals(new DecimalType(BsbLanApiParameterDTO.DataType.DT_ENUM.getValue()), state.as(DecimalType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_DESCRIPTION, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Description"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_NAME, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Name"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_UNIT, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Unit"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_NUMBER_VALUE, parameter);
        assertNotNull(state);
        assertEquals(new DecimalType(0), state.as(DecimalType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_STRING_VALUE, parameter);
        assertNotNull(state);
        assertEquals(new StringType("0"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_SWITCH_VALUE, parameter);
        assertNotNull(state);
        assertEquals(OnOffType.OFF, state);
    }

    @Test
    public void testGetStatesForValueParameterValue() {
        BsbLanApiParameterDTO parameter = new BsbLanApiParameterDTO();
        parameter.dataType = BsbLanApiParameterDTO.DataType.DT_VALS;
        parameter.description = "Test-Description";
        parameter.name = "Test-Name";
        parameter.unit = "Test-Unit";
        parameter.value = "22.5";

        State state = null;

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_DATATYPE, parameter);
        assertNotNull(state);
        assertEquals(new DecimalType(BsbLanApiParameterDTO.DataType.DT_VALS.getValue()), state.as(DecimalType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_DESCRIPTION, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Description"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_NAME, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Name"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_UNIT, parameter);
        assertNotNull(state);
        assertEquals(new StringType("Test-Unit"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_NUMBER_VALUE, parameter);
        assertNotNull(state);
        assertEquals(new DecimalType(22.5), state.as(DecimalType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_STRING_VALUE, parameter);
        assertNotNull(state);
        assertEquals(new StringType("22.5"), state.as(StringType.class));

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_SWITCH_VALUE, parameter);
        assertNotNull(state);
        assertEquals(OnOffType.ON, state);
    }

    @Test
    public void testGetStatesEscapesHtml() {
        BsbLanApiParameterDTO parameter = new BsbLanApiParameterDTO();
        parameter.dataType = BsbLanApiParameterDTO.DataType.DT_VALS;
        parameter.description = "Test-Description";
        parameter.name = "Test-Name";
        parameter.unit = "&deg;C";
        parameter.value = "22.5";

        State state = null;

        state = BsbLanParameterConverter.getState(PARAMETER_CHANNEL_UNIT, parameter);
        assertNotNull(state);
        assertEquals(new StringType("°C"), state.as(StringType.class));
    }

    @Test
    public void testGetValueForReadonlyChannels() {
        assertNull(BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_DATATYPE, OnOffType.ON));
        assertNull(BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_DESCRIPTION, OnOffType.ON));
        assertNull(BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_NAME, OnOffType.ON));
        assertNull(BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_UNIT, OnOffType.ON));
    }

    @Test
    public void testGetValueForNumberValueChannel() {
        assertNull(BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_NUMBER_VALUE, OnOffType.ON), "1");
        assertNull(BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_NUMBER_VALUE, OnOffType.OFF), "0");
        assertEquals("42", BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_NUMBER_VALUE, new DecimalType(42)));
        assertEquals("22.5", BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_NUMBER_VALUE, new DecimalType(22.5)));
        assertNull(BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_NUMBER_VALUE,
                new StringType("Not a number value")));
        assertNull(BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_NUMBER_VALUE, new StringType("")));
        assertEquals("75", BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_NUMBER_VALUE, new PercentType(75)));
        assertEquals("22.5", BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_NUMBER_VALUE,
                new QuantityType<>(22.5, SIUnits.CELSIUS)));
    }

    @Test
    public void testGetValueForSwitchValueChannel() {
        assertEquals("1", BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_SWITCH_VALUE, OnOffType.ON));
        assertEquals("0", BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_SWITCH_VALUE, OnOffType.OFF));
        assertNull(BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_SWITCH_VALUE, new DecimalType(1)));
        assertNull(BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_SWITCH_VALUE, new DecimalType(0)));
        assertNull(BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_SWITCH_VALUE, new DecimalType(42)));
        assertNull(BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_SWITCH_VALUE, new DecimalType(22.5)));
        assertNull(BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_SWITCH_VALUE,
                new StringType("Not a number value")));
        assertNull(BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_SWITCH_VALUE, new StringType("")));
    }

    @Test
    public void testGetValueForStringValueChannel() {
        assertEquals("1", BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_STRING_VALUE, OnOffType.ON));
        assertEquals("0", BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_STRING_VALUE, OnOffType.OFF));
        assertEquals("42", BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_STRING_VALUE, new DecimalType(42)));
        assertEquals("22.5", BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_STRING_VALUE, new DecimalType(22.5)));
        assertEquals("A string value",
                BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_STRING_VALUE, new StringType("A string value")));
        assertEquals("", BsbLanParameterConverter.getValue(PARAMETER_CHANNEL_STRING_VALUE, new StringType("")));
    }
}
