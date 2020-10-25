package org.openhab.binding.modbus.helioseasycontrols.internal;

/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openhab.io.transport.modbus.ModbusRegisterArray;

/**
 * @author Sami Salonen - Initial contribution
 */
@RunWith(Parameterized.class)
public class PreparePayloadTest {

    private final String payload;
    private final ModbusRegisterArray expectedRegisters;
    private Method preparePayloadMethod;

    public PreparePayloadTest(String payload, ModbusRegisterArray expectedRegisters)
            throws NoSuchMethodException, SecurityException {
        this.payload = payload;
        this.expectedRegisters = expectedRegisters;
        preparePayloadMethod = HeliosEasyControlsHandler.class.getDeclaredMethod("preparePayload", String.class);
        preparePayloadMethod.setAccessible(true);
    }

    private ModbusRegisterArray preparePayload(String payload) {
        try {
            return (ModbusRegisterArray) preparePayloadMethod.invoke(null, this.payload);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            fail("Reflection failure:" + e.getMessage());
            throw new RuntimeException(); // to make compiler happy
        }
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Collections.unmodifiableList(Stream
                // Due to nul byte, full register full of nul bytes added
                .of(new Object[] { "v00020=a", new ModbusRegisterArray(0x7630, 0x3030, 0x3230, 0x3d61, 0x0000) },
                        new Object[] { "v00020=aa", new ModbusRegisterArray(0x7630, 0x3030, 0x3230, 0x3d61, 0x6100) })
                .collect(Collectors.toList()));

    }

    @Test
    public void testPreparePayload()
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        ModbusRegisterArray actual = preparePayload(this.payload);

        assertThat(String.format("payload=%s", payload), actual.size(), is(equalTo(expectedRegisters.size())));
        for (int i = 0; i < expectedRegisters.size(); i++) {
            int expectedRegisterDataUnsigned = expectedRegisters.getRegister(i);
            int actualUnsigned = actual.getRegister(i);

            assertThat(String.format("register index i=%d, payload=%s", i, payload), actualUnsigned,
                    is(equalTo(expectedRegisterDataUnsigned)));
        }
    }
}
