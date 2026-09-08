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
package org.openhab.binding.smaenergymeter.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;
import org.openhab.binding.smaenergymeter.internal.SerialNumber;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;

class EnergyMeterTest {

    @Test
    void parsesKnownObisValues() throws IOException {
        byte[] telegram = new byte[96];
        telegram[0] = 'S';
        telegram[1] = 'M';
        telegram[2] = 'A';
        telegram[16] = 0x60;
        telegram[17] = 0x69;
        ByteBuffer.wrap(telegram, 0x14, 4).putInt(0x12345678);

        int offset = 28;
        offset = writeUint32(telegram, offset, ObisId.POSITIVE_ACTIVE_POWER.getCode());
        offset = writeUint32(telegram, offset, 1234);
        offset = writeUint32(telegram, offset, ObisId.POSITIVE_ACTIVE_ENERGY.getCode());
        offset = writeUint64(telegram, offset, 7_200_000L);
        offset = writeUint32(telegram, offset, ObisId.NEGATIVE_ACTIVE_POWER_L1.getCode());
        offset = writeUint32(telegram, offset, 567);
        offset = writeUint32(telegram, offset, 0x99990400);
        writeUint32(telegram, offset, 0);

        EnergyMeter meter = new EnergyMeter();
        meter.parse(telegram);

        assertEquals("305419896", meter.getSerialNumber());
        assertEquals(new DecimalType("123.4"), meter.getPowerIn());
        assertEquals(new DecimalType("2"), meter.getEnergyIn());
        assertEquals(new DecimalType("56.7"), meter.getPowerOutL1());
        assertEquals(DecimalType.ZERO, meter.getPowerOut());
    }

    @Test
    void parsesProvidedSampleTelegram() throws IOException {
        byte[] packet = hexToBytes("""
                01 00 5e 0c ff fe 2c 4d 54 69 72 fc 08 00 45 00
                02 7c 5c a0 40 00 01 11 69 13 c0 a8 02 0a ef 0c
                ff fe cc c3 25 32 02 68 b8 ee 53 4d 41 00 00 04
                02 a0 00 00 00 01 02 4c 00 10 60 69 01 0e 71 57
                9d 2b 7f dc 48 ba 00 01 04 00 00 00 00 00 00 15
                04 00 00 00 0d 02 00 29 04 00 00 00 00 00 00 3d
                04 00 00 00 00 00 00 01 08 00 00 00 00 06 00 df
                a9 e8 00 15 08 00 00 00 00 04 9c 1b 5a 08 00 29
                08 00 00 00 00 01 89 04 34 88 00 3d 08 00 00 00
                00 01 86 8b f6 c8 00 02 04 00 00 00 02 ab 00 16
                04 00 00 00 00 00 00 2a 04 00 00 00 07 ad 00 3e
                04 00 00 00 08 00 00 02 08 00 00 00 00 01 6f 02
                8b 98 00 16 08 00 00 00 00 00 79 9a 64 78 00 2a
                08 00 00 00 00 01 6f 69 2b 28 00 3e 08 00 00 00
                00 01 30 ca d8 d0 00 03 04 00 00 00 00 00 00 17
                04 00 00 00 00 00 00 2b 04 00 00 00 00 00 00 3f
                04 00 00 00 00 00 00 03 08 00 00 00 00 00 00 0a
                6a 40 00 17 08 00 00 00 00 00 00 3a 83 60 00 2b
                08 00 00 00 00 00 00 f9 19 38 00 3f 08 00 00 00
                00 00 00 5e f3 08 00 04 04 00 00 00 25 23 00 18
                04 00 00 00 10 a7 00 2c 04 00 00 00 06 a1 00 40
                04 00 00 00 0d da 00 04 08 00 00 00 00 07 86 19
                cd b8 00 18 08 00 00 00 00 02 f2 29 52 08 00 2c
                08 00 00 00 00 01 fb f4 ea 4f 00 40 08 00 00 00
                00 02 99 83 b6 c0 00 09 04 00 00 00 00 00 00 1d
                04 00 00 00 15 22 00 31 04 00 00 00 00 00 00 45
                04 00 00 00 00 00 00 09 08 00 00 00 00 08 62 9d
                dd 38 00 1d 08 00 00 00 00 05 96 83 1e e0 00 31
                08 00 00 00 00 02 4b dd a2 50 00 45 08 00 00 00
                00 02 a8 8b de 18 00 0a 04 00 00 00 25 3c 00 1e
                04 00 00 00 00 00 00 32 04 00 00 00 0a 25 00 46
                04 00 00 00 0f ff 00 0a 08 00 00 00 00 01 c4 66
                ac 8f 00 1e 08 00 00 00 00 00 92 b0 34 10 00 32
                08 00 00 00 00 01 ba 7e 34 a0 00 46 08 00 00 00
                00 01 9f 87 00 50 00 0d 04 00 ff ff ff b8 00 21
                04 00 00 00 02 68 00 35 04 00 ff ff fd 0b 00 49
                04 00 ff ff fe 0c 00 1f 04 00 00 00 0b 9c 00 33
                04 00 00 00 05 27 00 47 04 00 00 00 07 76 00 20
                04 00 00 03 a4 d1 00 34 04 00 00 03 aa b1 00 48
                04 00 00 03 a8 a0 00 0e 04 00 00 00 c3 71 90 00
                00 00 01 02 04 52 00 00 00 00
                """);
        byte[] telegram = new byte[608];
        System.arraycopy(packet, findSmaPayloadStart(packet), telegram, 0, telegram.length);

        EnergyMeter meter = new EnergyMeter();
        meter.parse(telegram);

        assertEquals("1901567275", meter.getSerialNumber());
        assertEquals(new DecimalType("0"), meter.getPowerIn());
        assertEquals(new DecimalType("68.3"), meter.getPowerOut());
        assertEquals(new DecimalType("7162.3505"), meter.getEnergyIn());
        assertEquals(new DecimalType("1710.3903"), meter.getEnergyOut());
        assertEquals(new DecimalType("333"), meter.getPowerInL1());
        assertEquals(new DecimalType("0"), meter.getPowerOutL1());
        assertEquals(new DecimalType("5499.6965"), meter.getEnergyInL1());
        assertEquals(new DecimalType("566.7115"), meter.getEnergyOutL1());
        assertEquals(new DecimalType("0"), meter.getPowerInL2());
        assertEquals(new DecimalType("196.5"), meter.getPowerOutL2());
        assertEquals(new DecimalType("1831.5893"), meter.getEnergyInL2());
        assertEquals(new DecimalType("1712.2585"), meter.getEnergyOutL2());
        assertEquals(new DecimalType("0"), meter.getPowerInL3());
        assertEquals(new DecimalType("204.8"), meter.getPowerOutL3());
        assertEquals(new DecimalType("1820.0797"), meter.getEnergyInL3());
        assertEquals(new DecimalType("1420.4354"), meter.getEnergyOutL3());
        assertEquals(new DecimalType("0"), meter.getReactivePowerIn());
        assertEquals(new DecimalType("950.7"), meter.getReactivePowerOut());
        assertEquals(new DecimalType("0.1896"), meter.getReactiveEnergyIn());
        assertEquals(new DecimalType("8976.2803"), meter.getReactiveEnergyOut());
        assertEquals(new DecimalType("0"), meter.getReactivePowerInL1());
        assertEquals(new DecimalType("426.3"), meter.getReactivePowerOutL1());
        assertEquals(new DecimalType("1.0652"), meter.getReactiveEnergyInL1());
        assertEquals(new DecimalType("3514.6469"), meter.getReactiveEnergyOutL1());
        assertEquals(new DecimalType("0"), meter.getReactivePowerInL2());
        assertEquals(new DecimalType("169.7"), meter.getReactivePowerOutL2());
        assertEquals(new DecimalType("4.5347"), meter.getReactiveEnergyInL2());
        assertEquals(new DecimalType("2367.2497997222"), meter.getReactiveEnergyOutL2());
        assertEquals(new DecimalType("0"), meter.getReactivePowerInL3());
        assertEquals(new DecimalType("354.6"), meter.getReactivePowerOutL3());
        assertEquals(new DecimalType("1.7285"), meter.getReactiveEnergyInL3());
        assertEquals(new DecimalType("3101.5224"), meter.getReactiveEnergyOutL3());
        assertEquals(new DecimalType("0"), meter.getApparentPowerIn());
        assertEquals(new DecimalType("953.2"), meter.getApparentPowerOut());
        assertEquals(new DecimalType("10003.9587"), meter.getApparentEnergyIn());
        assertEquals(new DecimalType("2108.3417997222"), meter.getApparentEnergyOut());
        assertEquals(new DecimalType("541"), meter.getApparentPowerInL1());
        assertEquals(new DecimalType("0"), meter.getApparentPowerOutL1());
        assertEquals(new DecimalType("6666.67"), meter.getApparentEnergyInL1());
        assertEquals(new DecimalType("683.617"), meter.getApparentEnergyOutL1());
        assertEquals(new DecimalType("0"), meter.getApparentPowerInL2());
        assertEquals(new DecimalType("259.7"), meter.getApparentPowerOutL2());
        assertEquals(new DecimalType("2739.653"), meter.getApparentEnergyInL2());
        assertEquals(new DecimalType("2062.1668"), meter.getApparentEnergyOutL2());
        assertEquals(new DecimalType("0"), meter.getApparentPowerInL3());
        assertEquals(new DecimalType("409.5"), meter.getApparentPowerOutL3());
        assertEquals(new DecimalType("3171.5759"), meter.getApparentEnergyInL3());
        assertEquals(new DecimalType("1936.4978"), meter.getApparentEnergyOutL3());
        assertEquals(new DecimalType("-7.2"), meter.getPowerFactor());
        assertEquals(new DecimalType("61.6"), meter.getPowerFactorL1());
        assertEquals(new DecimalType("-75.7"), meter.getPowerFactorL2());
        assertEquals(new DecimalType("-50"), meter.getPowerFactorL3());
        assertEquals(new DecimalType("2.972"), meter.getCurrentL1());
        assertEquals(new DecimalType("1.319"), meter.getCurrentL2());
        assertEquals(new DecimalType("1.91"), meter.getCurrentL3());
        assertEquals(new DecimalType("238.801"), meter.getVoltageL1());
        assertEquals(new DecimalType("240.305"), meter.getVoltageL2());
        assertEquals(new DecimalType("239.776"), meter.getVoltageL3());
        assertEquals(new DecimalType("50.033"), meter.getFrequency());
        assertEquals(new StringType("1.2.4.R"), meter.getVersion());
    }

    @Test
    void supportsLegacyHexadecimalSerialNumbers() {
        assertEquals("1901567275", SerialNumber.normalize("1901567275"));
        assertEquals("1901567275", SerialNumber.normalize("71579D2B"));
        assertEquals("1901567275", SerialNumber.normalize("0x71579d2b"));
        assertTrue(SerialNumber.matches("71579D2B", "1901567275"));
        assertTrue(SerialNumber.matches("0x71579d2b", "1901567275"));
    }

    private static int writeUint32(byte[] bytes, int offset, int value) {
        ByteBuffer.wrap(bytes, offset, 4).putInt(value);
        return offset + 4;
    }

    private static int writeUint64(byte[] bytes, int offset, long value) {
        ByteBuffer.wrap(bytes, offset, 8).putLong(value);
        return offset + 8;
    }

    private static byte[] hexToBytes(String hex) {
        String sanitized = hex.replaceAll("\\s+", "");
        byte[] result = new byte[sanitized.length() / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) Integer.parseInt(sanitized.substring(i * 2, i * 2 + 2), 16);
        }
        return result;
    }

    private static int findSmaPayloadStart(byte[] packet) {
        for (int i = 0; i <= packet.length - 3; i++) {
            if (packet[i] == 'S' && packet[i + 1] == 'M' && packet[i + 2] == 'A') {
                return i;
            }
        }
        throw new IllegalArgumentException("SMA payload not found");
    }
}
