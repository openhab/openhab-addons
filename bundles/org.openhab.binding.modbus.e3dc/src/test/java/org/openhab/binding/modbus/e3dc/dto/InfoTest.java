/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.modbus.e3dc.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.modbus.e3dc.internal.dto.InfoBlock;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data.DataType;
import org.openhab.binding.modbus.e3dc.internal.modbus.Parser;

/**
 * The {@link InfoTest} Test Data Transfer Objects of Information Block
 *
 * @author Bernd Weymann - Initial contribution
 */
public class InfoTest {
    private Parser mc;

    @BeforeEach
    public void setup() {
        byte[] infoBlock = new byte[] { -29, -36, 1, 2, 0, -120, 69, 51, 47, 68, 67, 32, 71, 109, 98, 72, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 83, 49, 48, 32, 69, 32, 65, 73, 79, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 85, 78, 73, 78, 73, 84, 73, 65, 76, 73, 90, 69, 68,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 83, 49, 48, 95, 50, 48, 50, 48, 95, 48, 52, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        mc = new Parser(DataType.INFO);
        mc.setArray(infoBlock);
    }

    @Test
    public void testvalidInformationBlock() {
        Optional<Data> infoOpt = mc.parse(DataType.INFO);
        assertTrue(infoOpt.isPresent());
        InfoBlock b = (InfoBlock) infoOpt.get();
        assertNotNull(b);
        assertEquals("E3DC", b.modbusId.toString(), "MagicByte");
        assertEquals("S10 E AIO", b.modelName.toString(), "Model");
        assertEquals("S10_2020_04", b.firmware.toString(), "Firmware");
        assertEquals("E3/DC GmbH", b.manufacturer.toString(), "Manufacturer");
    }

    @Test
    public void testWrongQuery() {
        Optional<Data> dataOpt = mc.parse(DataType.POWER);
        assertFalse(dataOpt.isPresent());
    }

    @Test
    public void testInvalidBlockSize() {
        byte[] infoBlock = new byte[] { -29, -36, 1, 2, 0, -120, 69, 51, 47, 68, 67, 32, 71, 109, 98, 72, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 83, 49, 48, 32, 69, 32, 65, 73, 79, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 85, 78, 73, 78, 73, 84, 73, 65, 76, 73, 90, 69, 68,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 83, 49, 48, 95, 50, 48, 50, 48, 95, 48 };
        Parser mc = new Parser(DataType.INFO);
        mc.setArray(infoBlock);
        Optional<Data> infoOpt = mc.parse(DataType.INFO);
        InfoBlock b = (InfoBlock) infoOpt.get();
        // block still valid but data maybe corrupted => logged in warnings
        assertTrue(infoOpt.isPresent());
        assertNotNull(b);
    }
}
