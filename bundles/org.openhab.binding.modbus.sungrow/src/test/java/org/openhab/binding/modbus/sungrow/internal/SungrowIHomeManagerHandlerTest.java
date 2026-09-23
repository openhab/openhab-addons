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
class SungrowIHomeManagerHandlerTest {

    private static ModbusRegisterArray createDeviceInfoRegisters() {
        return new ModbusRegisterArray(0x72A, // 8000: Device-Type Code
                0x3000, // 8001-8002: Protocol-No: AW0
                0x4157, //
                0x0200, // 8003 - 8004: Protocol- version: V1.0.2
                0x0100);
    }

    @Test
    void testUpdateDeviceInfoPropertiesSetsAllProperties() throws Exception {
        Thing thing = mock(Thing.class);
        SungrowIHomeManagerHandler handler = new SungrowIHomeManagerHandler(thing);
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);

        ModbusRegisterArray registers = createDeviceInfoRegisters();
        ModbusReadRequestBlueprint request = new ModbusReadRequestBlueprint(1,
                ModbusReadFunctionCode.READ_INPUT_REGISTERS, 7999, 8, 1);
        AsyncModbusReadResult result = new AsyncModbusReadResult(request, registers);

        handler.updateDeviceInfoProperties(result);

        verify(thing).setProperty(ModbusSungrowBindingConstants.PROP_KEY_IHM_DEVICE_TYPE_CODE, "iHomeManager");
        verify(thing).setProperty(ModbusSungrowBindingConstants.PROP_KEY_IHM_PROTOCOL_NUMBER, "AW0");
        verify(thing).setProperty(ModbusSungrowBindingConstants.PROP_KEY_IHM_PROTOCOL_VERSION, "V1.0.2");
    }
}
