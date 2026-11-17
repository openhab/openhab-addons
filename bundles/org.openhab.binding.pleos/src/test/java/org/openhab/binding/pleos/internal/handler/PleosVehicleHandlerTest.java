/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.pleos.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehicleStatusResponse.ValueWithUnit;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

/**
 * Unit tests for {@link PleosVehicleHandler} data conversion methods.
 *
 * @author Stephan Sandmann - Initial contribution
 */
@NonNullByDefault
public class PleosVehicleHandlerTest {

    @Test
    public void testFormatTireValueUndefOnNullOrSentinel() {
        Unit<?> bar = Units.BAR;
        assertEquals(UnDefType.UNDEF, PleosVehicleHandler.formatTireValue(null, "bar", bar));
        assertEquals(UnDefType.UNDEF, PleosVehicleHandler.formatTireValue(255.0, "bar", bar));
    }

    @Test
    public void testFormatTireValueBar() {
        State state = PleosVehicleHandler.formatTireValue(24.0, "bar", Units.BAR);
        assertNotNull(state);
        assertEquals("2.4 bar", state.toString());
    }

    @Test
    public void testFormatTireValueKpa() {
        State state = PleosVehicleHandler.formatTireValue(50.0, "kpa", MetricPrefix.KILO(SIUnits.PASCAL));
        assertNotNull(state);
        assertEquals("250 kPa", state.toString());
    }

    @Test
    public void testFormatTireValuePsi() {
        State state = PleosVehicleHandler.formatTireValue(35.0, "psi", ImperialUnits.POUND_FORCE_SQUARE_INCH);
        assertNotNull(state);
        assertEquals("35 psi", state.toString());
    }

    @Test
    public void testResolveSeatLevelNumeric() {
        assertEquals("OFF", PleosVehicleHandler.resolveSeatLevel(new JsonPrimitive(0)));
        assertEquals("LOW", PleosVehicleHandler.resolveSeatLevel(new JsonPrimitive(1)));
        assertEquals("MIDDLE", PleosVehicleHandler.resolveSeatLevel(new JsonPrimitive(2)));
        assertEquals("HIGH", PleosVehicleHandler.resolveSeatLevel(new JsonPrimitive(3)));
        assertEquals("OFF", PleosVehicleHandler.resolveSeatLevel(new JsonPrimitive(99)));
    }

    @Test
    public void testResolveSeatLevelString() {
        assertEquals("OFF", PleosVehicleHandler.resolveSeatLevel(new JsonPrimitive("OFF")));
        assertEquals("LOW", PleosVehicleHandler.resolveSeatLevel(new JsonPrimitive("LOW")));
        assertEquals("LOW", PleosVehicleHandler.resolveSeatLevel(new JsonPrimitive("1")));
        assertEquals("MIDDLE", PleosVehicleHandler.resolveSeatLevel(new JsonPrimitive("MIDDLE")));
        assertEquals("MIDDLE", PleosVehicleHandler.resolveSeatLevel(new JsonPrimitive("MID")));
        assertEquals("MIDDLE", PleosVehicleHandler.resolveSeatLevel(new JsonPrimitive("2")));
        assertEquals("HIGH", PleosVehicleHandler.resolveSeatLevel(new JsonPrimitive("HIGH")));
        assertEquals("HIGH", PleosVehicleHandler.resolveSeatLevel(new JsonPrimitive("3")));
        assertEquals("OFF", PleosVehicleHandler.resolveSeatLevel(new JsonPrimitive("INVALID")));
    }

    @Test
    public void testResolveSeatLevelNullAndJsonNull() {
        assertEquals("OFF", PleosVehicleHandler.resolveSeatLevel(null));
        assertEquals("OFF", PleosVehicleHandler.resolveSeatLevel(JsonNull.INSTANCE));
    }

    @Test
    public void testToTemperatureOrUndef() {
        assertEquals(UnDefType.UNDEF, PleosVehicleHandler.toTemperatureOrUndef(null));
        assertEquals(UnDefType.UNDEF, PleosVehicleHandler.toTemperatureOrUndef(new ValueWithUnit(null, "C")));

        State celsius = PleosVehicleHandler.toTemperatureOrUndef(new ValueWithUnit(21.5, "C"));
        assertEquals("21.5 °C", celsius.toString());

        State fahrenheit = PleosVehicleHandler.toTemperatureOrUndef(new ValueWithUnit(72.0, "F"));
        assertEquals("72 °F", fahrenheit.toString());
    }
}
