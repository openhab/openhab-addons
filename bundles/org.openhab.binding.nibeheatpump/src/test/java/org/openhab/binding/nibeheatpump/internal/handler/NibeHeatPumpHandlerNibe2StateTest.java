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
package org.openhab.binding.nibeheatpump.internal.handler;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.smarthome.core.types.State;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openhab.binding.nibeheatpump.internal.models.PumpModel;
import org.openhab.binding.nibeheatpump.internal.models.VariableInformation;

/**
 * Tests cases for {@link NibeHeatPumpHandler}.
 *
 * @author Jevgeni Kiski - Initial contribution
 */
@RunWith(Parameterized.class)
public class NibeHeatPumpHandlerNibe2StateTest {

    // we need to get the decimal separator of the default locale for our tests
    private static final char SEP = new DecimalFormatSymbols().getDecimalSeparator();
    private static final String METHOD_NAME = "convertNibeValueToState";

    private NibeHeatPumpHandler product; // the class under test
    private Method m;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
    private int fCoilAddress;
    private int fValue;
    private String fFormat;
    private String fType;
    private String fExpected;

    @Parameterized.Parameters(name = "{index}: f({0}, {1}, {3})={4}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { //
                { 47028, 0xFF, "%d", "Number", "-1" }, //
                { 47028, 0x7F, "%d", "Number", "127" }, //
                { 47028, 0x80, "%d", "Number", "-128" }, //
                { 48132, 1966080, "%s", "String", "0" }, //
                { 47028, 0xFF, "%d", "Number", "-1" }, //
                { 43009, 0x011F, "%.1f", "Number", "28" + SEP + "7" }, //
                { 43009, 0x7FFF, "%.1f", "Number", "3276" + SEP + "7" }, //
                { 43009, 0x8000, "%.1f", "Number", "-3276" + SEP + "8" }, //
                { 40004, 0xFFFF, "%.1f", "Number", "-0" + SEP + "1" }, //
                { 40004, 0xFFFF, "%.1f", "Number", "-0" + SEP + "1" }, //
                { 43416, 0xFFFFFFFF, "%d", "Number", "4294967295" }, //
                { 47418, 0x004B, "%d", "Number", "75" }, //
                { 43514, 0x0007, "%d", "Number", "7" }, //
                { 47291, 0xFFFF, "%d", "Number", "65535" }, //
                { 42437, 0xFFFFFFFF, "%.1f", "Number", "429496729" + SEP + "5" }, //
                { 42504, 0xFFFFFFFF, "%d", "Number", "4294967295" }, //
                { 43081, 196, "%.1f", "Number", "19" + SEP + "6" }, //
                { 43424, 1685, "%d", "Number", "1685" }, //
                { 43416, 4857, "%d", "Number", "4857" }, //
                { 43420, 9487, "%d", "Number", "9487" }, //
                { 40940, (byte) 0xFF, "%.1f", "Number", "-0" + SEP + "1" }, //
                { 40940, 0x80000000, "%.1f", "Number", "-214748364" + SEP + "8" }, //
                { 40940, 0x7FFFFFFF, "%.1f", "Number", "214748364" + SEP + "7" }, //
                { 40940, (short) 0xFFFF, "%.1f", "Number", "-0" + SEP + "1" }, //
                { 40940, (byte) 0xFF, "%.1f", "Number", "-0" + SEP + "1" }, //
                { 40940, 0xFFFF, "%.1f", "Number", "6553" + SEP + "5" } //
        });
    }

    public NibeHeatPumpHandlerNibe2StateTest(final int coilAddress, final int value, final String format,
            final String type, final String expected) {
        this.fCoilAddress = coilAddress;
        this.fValue = value;
        this.fFormat = format;
        this.fType = type;
        this.fExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        product = new NibeHeatPumpHandler(null, PumpModel.F1X55);
        parameterTypes = new Class[3];
        parameterTypes[0] = VariableInformation.class;
        parameterTypes[1] = int.class;
        parameterTypes[2] = String.class;
        m = product.getClass().getDeclaredMethod(METHOD_NAME, parameterTypes);
        m.setAccessible(true);
        parameters = new Object[3];
    }

    @Test
    public void convertNibeValueToStateTest() throws InvocationTargetException, IllegalAccessException {
        VariableInformation varInfo = VariableInformation.getVariableInfo(PumpModel.F1X55, fCoilAddress);
        parameters[0] = varInfo;
        parameters[1] = fValue;
        parameters[2] = fType;
        State state = (State) m.invoke(product, parameters);

        assertEquals(fExpected, state.format(fFormat));
    }
}
