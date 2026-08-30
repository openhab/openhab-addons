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
package org.openhab.binding.modbus.sungrow.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;

/**
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
class SungrowInverterHandlerTest {

    private static ModbusRegisterArray createVersionRegisters() {
        // Register layout starting at 4950:
        // 4950-4951 (index 0-1): Protocol number (swapped register order)
        // 4952-4953 (index 2-3): Protocol version (swapped register order)
        // 4954-4968 (index 4-17): ARM cert version string (28 bytes = 14 registers)
        // 4969-4983 (index 19-32): DSP cert version string (28 bytes = 14 registers)
        return new ModbusRegisterArray(0x3000, 0x4142, 0x1100, 0x0100, 0x4152, 0x4D5F, 0x5341, 0x5050, 0x4849, 0x5245,
                0x2D48, 0x5F56, 0x3131, 0x5F56, 0x3031, 0x5f42, 0x0000, 0x0000, 0x0000, 0x4D44, 0x5350, 0x5f53, 0x4150,
                0x5048, 0x4952, 0x452D, 0x485F, 0x5631, 0x315F, 0x5630, 0x315F, 0x4200, 0x0000);
    }

    @Test
    public void testUpdateVersionPropertiesSetsAllProperties() {
        Thing thing = mock(Thing.class);
        SungrowInverterHandler handler = new SungrowInverterHandler(thing);
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);

        ModbusRegisterArray registers = createVersionRegisters();
        ModbusReadRequestBlueprint request = new ModbusReadRequestBlueprint(1,
                ModbusReadFunctionCode.READ_INPUT_REGISTERS, 4949, 33, 1);
        AsyncModbusReadResult result = new AsyncModbusReadResult(request, registers);

        handler.updateVersionProperties(result);

        verify(thing).setProperty(ModbusSungrowBindingConstants.PROP_KEY_PROTOCOL_NUMBER, "AB0");
        verify(thing).setProperty(ModbusSungrowBindingConstants.PROP_KEY_PROTOCOL_VERSION, "V1.0.17");
        verify(thing).setProperty(ModbusSungrowBindingConstants.PROP_KEY_ARM_CERT_VERSION_NUMBER,
                "ARM_SAPPHIRE-H_V11_V01_B");
        verify(thing).setProperty(ModbusSungrowBindingConstants.PROP_KEY_DSP_CERT_VERSION_NUMBER,
                "MDSP_SAPPHIRE-H_V11_V01_B");
    }
}
