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
package org.openhab.binding.modbus.helioseasycontrols.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * @author Sami Salonen - Initial contribution
 */
public class PreparePayloadTest {

    private Method preparePayloadMethod;

    public PreparePayloadTest() throws NoSuchMethodException, SecurityException {
        preparePayloadMethod = HeliosEasyControlsHandler.class.getDeclaredMethod("preparePayload", String.class);
        preparePayloadMethod.setAccessible(true);
    }

    private ModbusRegisterArray preparePayload(String payload) {
        try {
            return (ModbusRegisterArray) preparePayloadMethod.invoke(null, payload);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            fail("Reflection failure:" + e.getMessage());
            throw new RuntimeException(); // to make compiler happy
        }
    }

    public static Collection<Object[]> data() {
        return Collections.unmodifiableList(Stream
                // Due to nul byte, full register full of nul bytes added
                .of(new Object[] { "v00020=a", new ModbusRegisterArray(0x7630, 0x3030, 0x3230, 0x3d61, 0x0000) },
                        new Object[] { "v00020=aa", new ModbusRegisterArray(0x7630, 0x3030, 0x3230, 0x3d61, 0x6100) })
                .collect(Collectors.toList()));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testPreparePayload(String payload, ModbusRegisterArray expectedRegisters) {
        ModbusRegisterArray actual = preparePayload(payload);

        assertEquals(actual.size(), expectedRegisters.size(), String.format("payload=%s", payload));
        for (int i = 0; i < expectedRegisters.size(); i++) {
            int expectedRegisterDataUnsigned = expectedRegisters.getRegister(i);
            int actualUnsigned = actual.getRegister(i);

            assertEquals(expectedRegisterDataUnsigned, actualUnsigned,
                    String.format("register index i=%d, payload=%s", i, payload));
        }
    }
}
