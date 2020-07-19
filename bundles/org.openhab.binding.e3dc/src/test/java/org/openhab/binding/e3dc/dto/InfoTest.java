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
package org.openhab.binding.e3dc.dto;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.e3dc.internal.dto.InfoBlock;
import org.openhab.binding.e3dc.internal.modbus.Data;
import org.openhab.binding.e3dc.internal.modbus.Data.DataType;
import org.openhab.binding.e3dc.internal.modbus.ModbusCallback;
import org.openhab.binding.e3dc.mock.DataListenerMock;

/**
 * The {@link InfoTest} Test Data Transfer Objects of Information Block
 *
 * @author Bernd Weymann - Initial contribution
 */
public class InfoTest {
    private ModbusCallback mc;

    @Before
    public void setup() {
        byte[] infoBlock = new byte[] { -29, -36, 1, 2, 0, -120, 69, 51, 47, 68, 67, 32, 71, 109, 98, 72, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 83, 49, 48, 32, 69, 32, 65, 73, 79, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 85, 78, 73, 78, 73, 84, 73, 65, 76, 73, 90, 69, 68,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 83, 49, 48, 95, 50, 48, 50, 48, 95, 48, 52, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        mc = new ModbusCallback(DataType.INFO);
        mc.setArray(infoBlock);
    }

    @Test
    public void testvalidInformationBlock() {
        InfoBlock b = (InfoBlock) mc.getData(DataType.INFO);
        assertNotNull(b);
        assertEquals("MagicByte", "E3DC", b.modbusId.toString());
        assertEquals("Model", "S10 E AIO", b.modelName.toString());
        assertEquals("Firmware", "S10_2020_04", b.firmware.toString());
        assertEquals("Manufacturer", "E3/DC GmbH", b.manufacturer.toString());
    }

    @Test
    public void testWrongQuery() {
        InfoBlock b = (InfoBlock) mc.getData(DataType.POWER);
        assertNull(b);
    }

    @Test
    public void testNotifications() {
        DataListenerMock listener = new DataListenerMock(DataType.INFO);
        mc.addDataListener(listener);
        assertEquals("Callback 1", 1, listener.getCallCounter());

        Data d = listener.getCurrentData();
        assertNotNull(d);
        assertTrue("Info Data", d instanceof InfoBlock);
        InfoBlock b = (InfoBlock) d;
        assertEquals("MagicByte", "E3DC", b.modbusId.toString());
        assertEquals("Model", "S10 E AIO", b.modelName.toString());
        assertEquals("Firmware", "S10_2020_04", b.firmware.toString());
        assertEquals("Manufacturer", "E3/DC GmbH", b.manufacturer.toString());
        mc.setArray(new byte[] {});
        assertEquals("Callback 2", 2, listener.getCallCounter());
    }

    @Test
    public void testInvalidBlockSize() {
        byte[] infoBlock = new byte[] { -29, -36, 1, 2, 0, -120, 69, 51, 47, 68, 67, 32, 71, 109, 98, 72, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 83, 49, 48, 32, 69, 32, 65, 73, 79, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 85, 78, 73, 78, 73, 84, 73, 65, 76, 73, 90, 69, 68,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 83, 49, 48, 95, 50, 48, 50, 48, 95, 48 };
        ModbusCallback mc = new ModbusCallback(DataType.INFO);
        mc.setArray(infoBlock);
        InfoBlock b = (InfoBlock) mc.getData(DataType.INFO);
        // block still valid but data maybe corrupted => logged in warnings
        assertNotNull(b);
    }
}
