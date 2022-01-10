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
package org.openhab.binding.bluetooth.bluegiga.internal.eir;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link EirRecord}.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class EirRecordTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testProcess16BitUUIDs() {
        int[] data = { 0x03, 0x0F, 0x18, 0x00, 0x18 };
        String batteryService = "0000180F-0000-1000-8000-00805F9B34FB";
        String genericAccess = "00001800-0000-1000-8000-00805F9B34FB";

        EirRecord eirRecord = new EirRecord(data);
        assertEquals(EirDataType.EIR_SVC_UUID16_COMPLETE, eirRecord.getType());
        List<UUID> uuids = (List<UUID>) eirRecord.getRecord();
        assertEquals(2, uuids.size());
        assertTrue(uuids.contains(UUID.fromString(batteryService)));
        assertTrue(uuids.contains(UUID.fromString(genericAccess)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcess32BitUUIDs() {
        int[] data = { 0x05, /* service1 */ 0x10, 0x8e, 0xe7, 0x74, /* service2 */ 0x11, 0x8e, 0xe7, 0x64 };
        String service1 = "74E78E10-0000-1000-8000-00805F9B34FB";
        String service2 = "64E78E11-0000-1000-8000-00805F9B34FB";

        EirRecord eirRecord = new EirRecord(data);
        assertEquals(EirDataType.EIR_SVC_UUID32_COMPLETE, eirRecord.getType());
        List<UUID> uuids = (List<UUID>) eirRecord.getRecord();
        assertEquals(2, uuids.size());
        assertTrue(uuids.contains(UUID.fromString(service1)));
        assertTrue(uuids.contains(UUID.fromString(service2)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcess128BitUUIDs() {
        int[] data = { 0x07, /* service1 */ 0x6d, 0x66, 0x70, 0x44, 0x73, 0x66, 0x62, 0x75, 0x66, 0x45, 0x76, 0x64,
                0x55, 0xaa, 0x6c, 0x22, /* service2 */ 0x6e, 0x66, 0x70, 0x44, 0x73, 0x66, 0x62, 0x75, 0x66, 0x45, 0x76,
                0x64, 0x55, 0xaa, 0x6c, 0x12, };
        String service1 = "226caa55-6476-4566-7562-66734470666d";
        String service2 = "126caa55-6476-4566-7562-66734470666e";

        EirRecord eirRecord = new EirRecord(data);
        assertEquals(EirDataType.EIR_SVC_UUID128_COMPLETE, eirRecord.getType());
        List<UUID> uuids = (List<UUID>) eirRecord.getRecord();
        assertEquals(2, uuids.size());
        assertTrue(uuids.contains(UUID.fromString(service1)));
        assertTrue(uuids.contains(UUID.fromString(service2)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessManufacturerData() {
        int[] data = { /* length 0x05, */ 0xFF, 0xFF, 0x02, 0x00, 0xFF };
        short siliconLabsID = (short) 0x02FF;

        EirRecord eirRecord = new EirRecord(data);
        assertEquals(EirDataType.EIR_MANUFACTURER_SPECIFIC, eirRecord.getType());
        Map<Short, int[]> manufacturerData = (Map<Short, int[]>) eirRecord.getRecord();
        assertTrue(manufacturerData.containsKey(siliconLabsID));
        assertArrayEquals(new int[] { 0x00, 0xFF }, manufacturerData.get(siliconLabsID));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUUID16ServiceData() {
        int[] data = { /* length 0x05, */ /* service data 16 bit UUID */ 0x16, 0x0F, 0x18, 0x45 };
        UUID batteryServiceUUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB");

        EirRecord eirRecord = new EirRecord(data);
        assertEquals(EirDataType.EIR_SVC_DATA_UUID16, eirRecord.getType());
        Map<UUID, int[]> serviceData = (Map<UUID, int[]>) eirRecord.getRecord();
        assertTrue(serviceData.containsKey(batteryServiceUUID));
        assertArrayEquals(new int[] { 0x45 }, serviceData.get(batteryServiceUUID));
    }

    @SuppressWarnings({ "unchecked" })
    @Test
    public void testUUID32ServiceData() {
        int[] data = { /* service data 32 bit UUID */ 0x20, /* UUID */ 0x10, 0x8e, 0xe7, 0x74, /* data */ 0x74, 0x01,
                0x0d, 0x01, (byte) 0xec };
        UUID dataServiceUUID = UUID.fromString("74E78E10-0000-1000-8000-00805F9B34FB");

        EirRecord eirRecord = new EirRecord(data);
        assertEquals(EirDataType.EIR_SVC_DATA_UUID32, eirRecord.getType());
        Map<UUID, int[]> serviceData = (Map<UUID, int[]>) eirRecord.getRecord();
        assertTrue(serviceData.containsKey(dataServiceUUID));
        assertArrayEquals(new int[] { 0x74, 0x01, 0x0d, 0x01, (byte) 0xec }, serviceData.get(dataServiceUUID));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUUID128ServiceData() {
        int[] data = { /* service data 32 bit UUID */ 0x21, /* UUID */ 0x6d, 0x66, 0x70, 0x44, 0x73, 0x66, 0x62, 0x75,
                0x66, 0x45, 0x76, 0x64, 0x55, 0xaa, 0x6c, 0x22, /* data */ 0x74, 0x01, 0x0d, 0x01, (byte) 0xec };
        UUID dataServiceUUID = UUID.fromString("226caa55-6476-4566-7562-66734470666d");

        EirRecord eirRecord = new EirRecord(data);
        assertEquals(EirDataType.EIR_SVC_DATA_UUID128, eirRecord.getType());
        Map<UUID, int[]> serviceData = (Map<UUID, int[]>) eirRecord.getRecord();
        assertTrue(serviceData.containsKey(dataServiceUUID));
        assertArrayEquals(new int[] { 0x74, 0x01, 0x0d, 0x01, (byte) 0xec }, serviceData.get(dataServiceUUID));
    }
}
