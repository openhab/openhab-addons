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
package org.openhab.binding.nibeheatpump.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.nibeheatpump.internal.models.PumpModel;
import org.openhab.binding.nibeheatpump.internal.models.VariableInformation;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;

/**
 * Tests cases for {@link NibeHeatPumpHandler}.
 *
 * @author Jevgeni Kiski - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
public class NibeHeatPumpHandlerCommand2NibeTest {
    private NibeHeatPumpHandler product; // the class under test
    private Method m;
    private static String METHOD_NAME = "convertCommandToNibeValue";
    private Class<?>[] parameterTypes;
    private Object[] parameters;

    private @Mock SerialPortManager serialPortManager;

    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { 47028, new DecimalType("-1"), (byte) 0xFF },
                { 48132, new DecimalType("0"), 0 }, { 48132, new StringType("0"), 0 },
                { 43009, new DecimalType("28.7"), 0x011F }, { 40004, new DecimalType("-0.1"), (short) 0xFFFF },
                { 47418, new DecimalType("75"), 0x004B }, { 43514, new DecimalType("7"), 0x0007 },
                { 47291, new DecimalType("65535"), 0xFFFF }, { 42437, new DecimalType("429496729.5"), 0xFFFFFFFF },
                { 42504, new DecimalType("4294967295"), 0xFFFFFFFF }, { 47041, new StringType("1"), 0x1 },
                { 47371, OnOffType.from(true), 0x1 }, { 47371, OnOffType.from(false), 0x0 }, });
    }

    @BeforeEach
    public void setUp() throws Exception {
        product = new NibeHeatPumpHandler(null, PumpModel.F1X55, serialPortManager);
        parameterTypes = new Class[2];
        parameterTypes[0] = VariableInformation.class;
        parameterTypes[1] = Command.class;
        m = product.getClass().getDeclaredMethod(METHOD_NAME, parameterTypes);
        m.setAccessible(true);
        parameters = new Object[2];
    }

    @ParameterizedTest
    @MethodSource("data")
    public void convertNibeValueToStateTest(final int coilAddress, final Command command, final int expected)
            throws InvocationTargetException, IllegalAccessException {
        VariableInformation varInfo = VariableInformation.getVariableInfo(PumpModel.F1X55, coilAddress);
        parameters[0] = varInfo;
        parameters[1] = command;
        int value = (int) m.invoke(product, parameters);

        assertEquals(expected, value);
    }
}
