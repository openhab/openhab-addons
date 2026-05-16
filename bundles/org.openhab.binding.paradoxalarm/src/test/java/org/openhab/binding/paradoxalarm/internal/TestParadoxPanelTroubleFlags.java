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
package org.openhab.binding.paradoxalarm.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.paradoxalarm.internal.model.ParadoxPanel;

/**
 * Verifies that {@link ParadoxPanel#parseTroubleFlags} extracts the correct bits from RAM block 1.
 *
 * Byte/bit offsets are derived from the PAI EVO RAMDataParserMap[1] BitStruct
 * (paradox/hardware/evo/parsers.py) and validated against live EVO192 RAM captures.
 *
 * Trouble flags are in RAMDataParserMap[1] (RAM block number 2 in PAI), but stored at
 * memoryMap.getElement(0) (RAM block number 1 = first 64-byte page, 0-indexed).
 * The troubles BitStruct starts at byte offset 13 within that page.
 *
 * RAM block 1 layout (relevant bytes, construct BitStruct MSB-first ordering):
 * byte 13: module_supervision_trouble = bit 3 from LSB (mask 0x08)
 * byte 14: battery_failure_trouble = bit 1 from LSB (mask 0x02)
 * ac_trouble = bit 0 from LSB (mask 0x01)
 * byte 15: com_pc_trouble = bit 5 from LSB (mask 0x20)
 */
@NonNullByDefault
public class TestParadoxPanelTroubleFlags {

    private ParadoxPanel panel = new ParadoxPanel();

    private byte[] emptyRamBlock() {
        return new byte[64];
    }

    @Test
    public void testNoTroublesWhenAllBytesZero() {
        panel.parseTroubleFlags(emptyRamBlock());
        assertFalse(panel.isAcTrouble());
        assertFalse(panel.isBatteryTrouble());
        assertFalse(panel.isModuleSupervisionTrouble());
        assertFalse(panel.isCommunicationTrouble());
    }

    @Test
    public void testAcTrouble() {
        byte[] ram = emptyRamBlock();
        ram[14] |= 0x01; // ac_trouble = bit 0 of byte 14
        panel.parseTroubleFlags(ram);
        assertTrue(panel.isAcTrouble());
        assertFalse(panel.isBatteryTrouble());
        assertFalse(panel.isModuleSupervisionTrouble());
        assertFalse(panel.isCommunicationTrouble());
    }

    @Test
    public void testBatteryTrouble() {
        byte[] ram = emptyRamBlock();
        ram[14] |= 0x02; // battery_failure_trouble = bit 1 of byte 14
        panel.parseTroubleFlags(ram);
        assertFalse(panel.isAcTrouble());
        assertTrue(panel.isBatteryTrouble());
        assertFalse(panel.isModuleSupervisionTrouble());
        assertFalse(panel.isCommunicationTrouble());
    }

    @Test
    public void testModuleSupervisionTrouble() {
        byte[] ram = emptyRamBlock();
        ram[13] |= 0x08; // module_supervision_trouble = bit 3 of byte 13
        panel.parseTroubleFlags(ram);
        assertFalse(panel.isAcTrouble());
        assertFalse(panel.isBatteryTrouble());
        assertTrue(panel.isModuleSupervisionTrouble());
        assertFalse(panel.isCommunicationTrouble());
    }

    @Test
    public void testCommunicationTrouble() {
        byte[] ram = emptyRamBlock();
        ram[15] |= 0x20; // com_pc_trouble = bit 5 of byte 15
        panel.parseTroubleFlags(ram);
        assertFalse(panel.isAcTrouble());
        assertFalse(panel.isBatteryTrouble());
        assertFalse(panel.isModuleSupervisionTrouble());
        assertTrue(panel.isCommunicationTrouble());
    }

    @Test
    public void testAdjacentBitsDoNotTriggerFalsePositives() {
        byte[] ram = emptyRamBlock();
        // Set every bit around the target bits to ensure no mask overlap
        ram[13] = (byte) 0xF7; // all bits set except bit 3 (module_supervision)
        ram[14] = (byte) 0xFC; // all bits set except bits 0 and 1 (ac + battery)
        ram[15] = (byte) 0xDF; // all bits set except bit 5 (com_pc)
        panel.parseTroubleFlags(ram);
        assertFalse(panel.isAcTrouble());
        assertFalse(panel.isBatteryTrouble());
        assertFalse(panel.isModuleSupervisionTrouble());
        assertFalse(panel.isCommunicationTrouble());
    }

    @Test
    public void testAllTroublesSimultaneously() {
        byte[] ram = emptyRamBlock();
        ram[13] |= 0x08;
        ram[14] |= 0x03; // both ac (0x01) and battery (0x02)
        ram[15] |= 0x20;
        panel.parseTroubleFlags(ram);
        assertTrue(panel.isAcTrouble());
        assertTrue(panel.isBatteryTrouble());
        assertTrue(panel.isModuleSupervisionTrouble());
        assertTrue(panel.isCommunicationTrouble());
    }
}
