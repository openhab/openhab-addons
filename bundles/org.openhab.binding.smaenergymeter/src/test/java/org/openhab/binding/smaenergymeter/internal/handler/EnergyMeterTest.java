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
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

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
        assertQuantityTypeValue("123.4", Units.WATT, meter.getState(ObisId.POSITIVE_ACTIVE_POWER));
        assertQuantityTypeValue("2", Units.KILOWATT_HOUR, meter.getState(ObisId.POSITIVE_ACTIVE_ENERGY));
        assertQuantityTypeValue("56.7", Units.WATT, meter.getState(ObisId.NEGATIVE_ACTIVE_POWER_L1));
        assertQuantityTypeZero(Units.WATT, meter.getState(ObisId.NEGATIVE_ACTIVE_POWER));
    }

    @Test
    void parsesProvidedSampleTelegram() throws IOException {
        byte[] telegram = hexToBytes("""
                53 4d 41 00 00 04 02 a0 00 00 00 01 02 4c 00 10 60 69 01 0e 71 57 9d 2b 7f dc 48 ba
                00 01 04 00 00 00 00 00 00 15 04 00 00 00 0d 02 00 29 04 00 00 00 00 00 00 3d 04 00
                00 00 00 00 00 01 08 00 00 00 00 06 00 df a9 e8 00 15 08 00 00 00 00 04 9c 1b 5a 08
                00 29 08 00 00 00 00 01 89 04 34 88 00 3d 08 00 00 00 00 01 86 8b f6 c8 00 02 04 00
                00 00 02 ab 00 16 04 00 00 00 00 00 00 2a 04 00 00 00 07 ad 00 3e 04 00 00 00 08 00
                00 02 08 00 00 00 00 01 6f 02 8b 98 00 16 08 00 00 00 00 00 79 9a 64 78 00 2a 08 00
                00 00 00 01 6f 69 2b 28 00 3e 08 00 00 00 00 01 30 ca d8 d0 00 03 04 00 00 00 00 00
                00 17 04 00 00 00 00 00 00 2b 04 00 00 00 00 00 00 3f 04 00 00 00 00 00 00 03 08 00
                00 00 00 00 00 0a 6a 40 00 17 08 00 00 00 00 00 00 3a 83 60 00 2b 08 00 00 00 00 00
                00 f9 19 38 00 3f 08 00 00 00 00 00 00 5e f3 08 00 04 04 00 00 00 25 23 00 18 04 00
                00 00 10 a7 00 2c 04 00 00 00 06 a1 00 40 04 00 00 00 0d da 00 04 08 00 00 00 00 07
                86 19 cd b8 00 18 08 00 00 00 00 02 f2 29 52 08 00 2c 08 00 00 00 00 01 fb f4 ea 4f
                00 40 08 00 00 00 00 02 99 83 b6 c0 00 09 04 00 00 00 00 00 00 1d 04 00 00 00 15 22
                00 31 04 00 00 00 00 00 00 45 04 00 00 00 00 00 00 09 08 00 00 00 00 08 62 9d dd 38
                00 1d 08 00 00 00 00 05 96 83 1e e0 00 31 08 00 00 00 00 02 4b dd a2 50 00 45 08 00
                00 00 00 02 a8 8b de 18 00 0a 04 00 00 00 25 3c 00 1e 04 00 00 00 00 00 00 32 04 00
                00 00 0a 25 00 46 04 00 00 00 0f ff 00 0a 08 00 00 00 00 01 c4 66 ac 8f 00 1e 08 00
                00 00 00 00 92 b0 34 10 00 32 08 00 00 00 00 01 ba 7e 34 a0 00 46 08 00 00 00 00 01
                9f 87 00 50 00 0d 04 00 ff ff ff b8 00 21 04 00 00 00 02 68 00 35 04 00 ff ff fd 0b
                00 49 04 00 ff ff fe 0c 00 1f 04 00 00 00 0b 9c 00 33 04 00 00 00 05 27 00 47 04 00
                00 00 07 76 00 20 04 00 00 03 a4 d1 00 34 04 00 00 03 aa b1 00 48 04 00 00 03 a8 a0
                00 0e 04 00 00 00 c3 71 90 00 00 00 01 02 04 52 00 00 00 00
                """);

        EnergyMeter meter = new EnergyMeter();
        meter.parse(telegram);

        assertEquals("1901567275", meter.getSerialNumber());
        assertQuantityTypeValue("0", Units.WATT, meter.getState(ObisId.POSITIVE_ACTIVE_POWER));
        assertQuantityTypeValue("68.3", Units.WATT, meter.getState(ObisId.NEGATIVE_ACTIVE_POWER));
        assertQuantityTypeValue("7162.3505", Units.KILOWATT_HOUR, meter.getState(ObisId.POSITIVE_ACTIVE_ENERGY));
        assertQuantityTypeValue("1710.3903", Units.KILOWATT_HOUR, meter.getState(ObisId.NEGATIVE_ACTIVE_ENERGY));
        assertQuantityTypeValue("333", Units.WATT, meter.getState(ObisId.POSITIVE_ACTIVE_POWER_L1));
        assertQuantityTypeValue("0", Units.WATT, meter.getState(ObisId.NEGATIVE_ACTIVE_POWER_L1));
        assertQuantityTypeValue("5499.6965", Units.KILOWATT_HOUR, meter.getState(ObisId.POSITIVE_ACTIVE_ENERGY_L1));
        assertQuantityTypeValue("566.7115", Units.KILOWATT_HOUR, meter.getState(ObisId.NEGATIVE_ACTIVE_ENERGY_L1));
        assertQuantityTypeValue("0", Units.WATT, meter.getState(ObisId.POSITIVE_ACTIVE_POWER_L2));
        assertQuantityTypeValue("196.5", Units.WATT, meter.getState(ObisId.NEGATIVE_ACTIVE_POWER_L2));
        assertQuantityTypeValue("1831.5893", Units.KILOWATT_HOUR, meter.getState(ObisId.POSITIVE_ACTIVE_ENERGY_L2));
        assertQuantityTypeValue("1712.2585", Units.KILOWATT_HOUR, meter.getState(ObisId.NEGATIVE_ACTIVE_ENERGY_L2));
        assertQuantityTypeValue("0", Units.WATT, meter.getState(ObisId.POSITIVE_ACTIVE_POWER_L3));
        assertQuantityTypeValue("204.8", Units.WATT, meter.getState(ObisId.NEGATIVE_ACTIVE_POWER_L3));
        assertQuantityTypeValue("1820.0797", Units.KILOWATT_HOUR, meter.getState(ObisId.POSITIVE_ACTIVE_ENERGY_L3));
        assertQuantityTypeValue("1420.4354", Units.KILOWATT_HOUR, meter.getState(ObisId.NEGATIVE_ACTIVE_ENERGY_L3));
        assertQuantityTypeValue("0", Units.VAR, meter.getState(ObisId.POSITIVE_REACTIVE_POWER));
        assertQuantityTypeValue("950.7", Units.VAR, meter.getState(ObisId.NEGATIVE_REACTIVE_POWER));
        assertQuantityTypeValue("0.1896", Units.KILOVAR_HOUR, meter.getState(ObisId.POSITIVE_REACTIVE_ENERGY));
        assertQuantityTypeValue("8976.2803", Units.KILOVAR_HOUR, meter.getState(ObisId.NEGATIVE_REACTIVE_ENERGY));
        assertQuantityTypeValue("0", Units.VAR, meter.getState(ObisId.POSITIVE_REACTIVE_POWER_L1));
        assertQuantityTypeValue("426.3", Units.VAR, meter.getState(ObisId.NEGATIVE_REACTIVE_POWER_L1));
        assertQuantityTypeValue("1.0652", Units.KILOVAR_HOUR, meter.getState(ObisId.POSITIVE_REACTIVE_ENERGY_L1));
        assertQuantityTypeValue("3514.6469", Units.KILOVAR_HOUR, meter.getState(ObisId.NEGATIVE_REACTIVE_ENERGY_L1));
        assertQuantityTypeValue("0", Units.VAR, meter.getState(ObisId.POSITIVE_REACTIVE_POWER_L2));
        assertQuantityTypeValue("169.7", Units.VAR, meter.getState(ObisId.NEGATIVE_REACTIVE_POWER_L2));
        assertQuantityTypeValue("4.5347", Units.KILOVAR_HOUR, meter.getState(ObisId.POSITIVE_REACTIVE_ENERGY_L2));
        assertQuantityTypeValue("2367.2497997222", Units.KILOVAR_HOUR,
                meter.getState(ObisId.NEGATIVE_REACTIVE_ENERGY_L2));
        assertQuantityTypeValue("0", Units.VAR, meter.getState(ObisId.POSITIVE_REACTIVE_POWER_L3));
        assertQuantityTypeValue("354.6", Units.VAR, meter.getState(ObisId.NEGATIVE_REACTIVE_POWER_L3));
        assertQuantityTypeValue("1.7285", Units.KILOVAR_HOUR, meter.getState(ObisId.POSITIVE_REACTIVE_ENERGY_L3));
        assertQuantityTypeValue("3101.5224", Units.KILOVAR_HOUR, meter.getState(ObisId.NEGATIVE_REACTIVE_ENERGY_L3));
        assertQuantityTypeValue("0", Units.VOLT_AMPERE, meter.getState(ObisId.POSITIVE_APPARENT_POWER));
        assertQuantityTypeValue("953.2", Units.VOLT_AMPERE, meter.getState(ObisId.NEGATIVE_APPARENT_POWER));
        assertQuantityTypeValue("10003.9587", Units.VOLT_AMPERE_HOUR, meter.getState(ObisId.POSITIVE_APPARENT_ENERGY));
        assertQuantityTypeValue("2108.3417997222", Units.VOLT_AMPERE_HOUR,
                meter.getState(ObisId.NEGATIVE_APPARENT_ENERGY));
        assertQuantityTypeValue("541", Units.VOLT_AMPERE, meter.getState(ObisId.POSITIVE_APPARENT_POWER_L1));
        assertQuantityTypeValue("0", Units.VOLT_AMPERE, meter.getState(ObisId.NEGATIVE_APPARENT_POWER_L1));
        assertQuantityTypeValue("6666.67", Units.VOLT_AMPERE_HOUR, meter.getState(ObisId.POSITIVE_APPARENT_ENERGY_L1));
        assertQuantityTypeValue("683.617", Units.VOLT_AMPERE_HOUR, meter.getState(ObisId.NEGATIVE_APPARENT_ENERGY_L1));
        assertQuantityTypeValue("0", Units.VOLT_AMPERE, meter.getState(ObisId.POSITIVE_APPARENT_POWER_L2));
        assertQuantityTypeValue("259.7", Units.VOLT_AMPERE, meter.getState(ObisId.NEGATIVE_APPARENT_POWER_L2));
        assertQuantityTypeValue("2739.653", Units.VOLT_AMPERE_HOUR, meter.getState(ObisId.POSITIVE_APPARENT_ENERGY_L2));
        assertQuantityTypeValue("2062.1668", Units.VOLT_AMPERE_HOUR,
                meter.getState(ObisId.NEGATIVE_APPARENT_ENERGY_L2));
        assertQuantityTypeValue("0", Units.VOLT_AMPERE, meter.getState(ObisId.POSITIVE_APPARENT_POWER_L3));
        assertQuantityTypeValue("409.5", Units.VOLT_AMPERE, meter.getState(ObisId.NEGATIVE_APPARENT_POWER_L3));
        assertQuantityTypeValue("3171.5759", Units.VOLT_AMPERE_HOUR,
                meter.getState(ObisId.POSITIVE_APPARENT_ENERGY_L3));
        assertQuantityTypeValue("1936.4978", Units.VOLT_AMPERE_HOUR,
                meter.getState(ObisId.NEGATIVE_APPARENT_ENERGY_L3));
        assertQuantityTypeValue("-0.072", Units.ONE, meter.getState(ObisId.POWER_FACTOR));
        assertQuantityTypeValue("0.616", Units.ONE, meter.getState(ObisId.POWER_FACTOR_L1));
        assertQuantityTypeValue("-0.757", Units.ONE, meter.getState(ObisId.POWER_FACTOR_L2));
        assertQuantityTypeValue("-0.5", Units.ONE, meter.getState(ObisId.POWER_FACTOR_L3));
        assertQuantityTypeValue("2.972", Units.AMPERE, meter.getState(ObisId.CURRENT_L1));
        assertQuantityTypeValue("1.319", Units.AMPERE, meter.getState(ObisId.CURRENT_L2));
        assertQuantityTypeValue("1.91", Units.AMPERE, meter.getState(ObisId.CURRENT_L3));
        assertQuantityTypeValue("238.801", Units.VOLT, meter.getState(ObisId.VOLTAGE_L1));
        assertQuantityTypeValue("240.305", Units.VOLT, meter.getState(ObisId.VOLTAGE_L2));
        assertQuantityTypeValue("239.776", Units.VOLT, meter.getState(ObisId.VOLTAGE_L3));
        assertQuantityTypeValue("50.033", Units.HERTZ, meter.getState(ObisId.FREQUENCY));
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

    @Test
    void parsesTruncatedTelegramUntilLastCompleteObisRecord() throws IOException {
        byte[] payload = hexToBytes("""
                53 4d 41 00 00 04 02 a0 00 00 00 01 02 44 00 10 60 69 01 0e 71 42 cf 5f f5 64 5a ce
                00 01 04 00 00 00 54 e0 00 01 08 00 00 00 00 0d 20 d5 23 70 00 02 04 00 00 00 00 00
                00 02 08 00 00 00 00 13 65 8d 6c 60 00 03 04 00 00 00 07 57 00 03 08 00 00 00 00 04
                21 e8 2a b8 00 04 04 00 00 00 00 00 00 04 08 00 00 00 00 04 0c 47 bb b0 00 09 04 00
                00 00 55 31 00 09 08 00 00 00 00 0e 37 ca c1 10 00 0a 04 00 00 00 00 00 00 0a 08 00
                00 00 00 14 60 68 97 b8 00 0d 04 00 00 00 03 e4 00 15 04 00 00 00 06 05 00 15 08 00
                00 00 00 05 88 b0 a6 48 00 16 04 00 00 00 00 00 00 16 08 00 00 00 00 04 ab fb 45 78
                00 17 04 00
                """);
        byte[] telegram = new byte[payload.length];
        System.arraycopy(payload, 0, telegram, 0, payload.length);
        telegram[16] = 0x60;
        telegram[17] = 0x69;

        EnergyMeter meter = new EnergyMeter();
        meter.parse(telegram);

        assertEquals("1900203871", meter.getSerialNumber());
        assertQuantityTypeZero(Units.WATT, meter.getState(ObisId.NEGATIVE_ACTIVE_POWER));
        assertQuantityTypeZero(Units.VAR, meter.getState(ObisId.NEGATIVE_REACTIVE_POWER));
        assertQuantityTypeZero(Units.VOLT_AMPERE, meter.getState(ObisId.NEGATIVE_APPARENT_POWER));
        assertQuantityTypeZero(Units.WATT, meter.getState(ObisId.NEGATIVE_ACTIVE_POWER_L1));
        assertQuantityTypeZero(Units.VAR, meter.getState(ObisId.POSITIVE_REACTIVE_POWER_L1));
        assertQuantityTypeZero(Units.KILOVAR_HOUR, meter.getState(ObisId.POSITIVE_REACTIVE_ENERGY_L1));
        assertQuantityTypeZero(Units.AMPERE, meter.getState(ObisId.CURRENT_L1));
        assertEquals(StringType.EMPTY, meter.getVersion());
    }

    @Test
    void parsesTelegramLongerThanLegacyPacketSize() throws IOException {
        byte[] telegram = hexToBytes("""
                53 4d 41 00 00 0f 02 a0 00 00 00 01 02 44 00 10 60 69 01 0e 71 42 ca 5a 00 2d 89 74
                00 01 04 00 00 00 13 f2 00 01 08 00 00 00 00 00 05 19 70 80 00 02 04 00 00 00 00 00
                00 02 08 00 00 00 00 00 00 c6 75 00 00 03 04 00 00 00 00 00 00 03 08 00 00 00 00 00
                00 01 25 e8 00 04 04 00 00 00 07 16 00 04 08 00 00 00 00 00 00 67 95 a8 00 09 04 00
                00 00 15 2a 00 09 08 00 00 00 00 00 00 7e 68 a0 00 0a 04 00 00 00 00 00 00 0a 08 00
                00 00 00 00 00 1a ab 58 00 0d 04 00 00 00 03 ae 00 15 04 00 00 00 05 72 00 15 08 00
                00 00 00 00 00 16 96 80 00 16 04 00 00 00 00 00 00 16 08 00 00 00 00 00 00 07 35 00
                00 17 04 00 00 00 00 00 00 17 08 00 00 00 00 00 00 04 56 f0 00 18 04 00 00 00 01 f7
                00 18 08 00 00 00 00 00 00 3c db d8 00 1d 04 00 00 00 05 ca 00 1d 08 00 00 00 00 00
                00 30 36 a8 00 1e 04 00 00 00 00 00 00 1e 08 00 00 00 00 00 00 1e be c8 00 1f 04 00
                00 00 02 9f 00 20 04 00 03 00 81 3c 00 21 04 00 00 00 03 ad 00 29 04 00 00 00 05 84
                00 29 08 00 00 00 00 00 00 00 02 df 00 2a 04 00 00 00 00 00 00 2a 08 00 00 00 00 00
                00 22 57 e0 00 2b 04 00 00 00 00 00 00 2b 08 00 00 00 00 00 00 00 89 d0 00 2c 04 00
                00 00 03 73 00 2c 08 00 00 00 00 00 00 2d af c8 00 31 04 00 00 00 06 82 00 31 08 00
                00 00 00 00 00 1e ba 90 00 32 04 00 00 00 00 00 00 32 08 00 00 00 00 00 00 2c 6c 58
                00 33 04 00 00 00 40 0c 00 34 04 00 00 03 82 33 00 35 04 00 00 00 03 50 00 3d 04 00
                00 00 08 bf 00 3d 08 00 00 00 00 00 00 4a f5 b0 00 3f 04 00 00 00 00 00 00 3f 08 00
                00 00 00 00 00 02 f1 c0 00 40 04 00 00 00 01 ab 00 40 08 00 00 00 00 00 00 00 00 00
                00 45 04 00 00 00 09 23 00 45 08 00 00 00 00 00 00 4b 64 c8 00 46 04 00 00 00 00 00
                00 46 08 00 00 00 00 00 00 00 00 00 00 47 04 00 00 00 04 6c 00 48 04 00 00 03 7f c7
                00 49 04 00 00 00 03 d7 90 00 00 00 01 01 00 52 00 00 00 00 00
                """);

        EnergyMeter meter = new EnergyMeter();
        meter.parse(telegram);

        assertEquals("1900202586", meter.getSerialNumber());
        assertQuantityTypeValue("510.6", Units.WATT, meter.getState(ObisId.POSITIVE_ACTIVE_POWER));
        assertEquals(new StringType("1.1.0.R"), meter.getVersion());
    }

    @Test
    void parsesSampleTelegram() throws IOException {
        byte[] telegram = hexToBytes("""
                53 4d 41 00 00 0f 02 a0 00 00 00 01 02 44 00 10 60 69 01 0e 71 42 ca 5a 00 2d 89 74
                00 01 04 00 00 00 13 f2 00 01 08 00 00 00 00 00 05 19 70 80 00 02 04 00 00 00 00 00
                00 02 08 00 00 00 00 00 00 c6 75 00 00 03 04 00 00 00 00 00 00 03 08 00 00 00 00 00
                00 01 25 e8 00 04 04 00 00 00 07 16 00 04 08 00 00 00 00 00 00 67 95 a8 00 09 04 00
                00 00 15 2a 00 09 08 00 00 00 00 00 00 7e 68 a0 00 0a 04 00 00 00 00 00 00 0a 08 00
                00 00 00 00 00 1a ab 58 00 0d 04 00 00 00 03 ae 00 15 04 00 00 00 05 72 00 15 08 00
                00 00 00 00 00 16 96 80 00 16 04 00 00 00 00 00 00 16 08 00 00 00 00 00 00 07 35 00
                00 17 04 00 00 00 00 00 00 17 08 00 00 00 00 00 00 04 56 f0 00 18 04 00 00 00 01 f7
                00 18 08 00 00 00 00 00 00 3c db d8 00 1d 04 00 00 00 05 ca 00 1d 08 00 00 00 00 00
                00 30 36 a8 00 1e 04 00 00 00 00 00 00 1e 08 00 00 00 00 00 00 1e be c8 00 1f 04 00
                00 00 02 9f 00 20 04 00 03 00 81 3c 00 21 04 00 00 00 03 ad 00 29 04 00 00 00 05 84
                00 29 08 00 00 00 00 00 00 00 02 df 00 2a 04 00 00 00 00 00 00 2a 08 00 00 00 00 00
                00 22 57 e0 00 2b 04 00 00 00 00 00 00 2b 08 00 00 00 00 00 00 00 89 d0 00 2c 04 00
                00 00 03 73 00 2c 08 00 00 00 00 00 00 2d af c8 00 31 04 00 00 00 06 82 00 31 08 00
                00 00 00 00 00 1e ba 90 00 32 04 00 00 00 00 00 00 32 08 00 00 00 00 00 00 2c 6c 58
                00 33 04 00 00 00 40 0c 00 34 04 00 00 03 82 33 00 35 04 00 00 00 03 50 00 3d 04 00
                00 00 08 bf 00 3d 08 00 00 00 00 00 00 4a f5 b0 00 3f 04 00 00 00 00 00 00 3f 08 00
                00 00 00 00 00 02 f1 c0 00 40 04 00 00 00 01 ab 00 40 08 00 00 00 00 00 00 00 00 00
                00 45 04 00 00 00 09 23 00 45 08 00 00 00 00 00 00 4b 64 c8 00 46 04 00 00 00 00 00
                00 46 08 00 00 00 00 00 00 00 00 00 00 47 04 00 00 00 04 6c 00 48 04 00 00 03 7f c7
                00 49 04 00 00 00 03 d7 90 00 00 00 01 01 00 52 00 00 00 00 00
                """);

        EnergyMeter meter = new EnergyMeter();
        meter.parse(telegram);

        assertEquals("1900202586", meter.getSerialNumber());
        assertQuantityTypeValue("510.6", Units.WATT, meter.getState(ObisId.POSITIVE_ACTIVE_POWER));
        assertQuantityTypeValue("23.7648", Units.KILOWATT_HOUR, meter.getState(ObisId.POSITIVE_ACTIVE_ENERGY));
        assertQuantityTypeZero(Units.WATT, meter.getState(ObisId.NEGATIVE_ACTIVE_POWER));
        assertQuantityTypeValue("3.6128", Units.KILOWATT_HOUR, meter.getState(ObisId.NEGATIVE_ACTIVE_ENERGY));
        assertQuantityTypeValue("139.4", Units.WATT, meter.getState(ObisId.POSITIVE_ACTIVE_POWER_L1));
        assertQuantityTypeValue("0.4112", Units.KILOWATT_HOUR, meter.getState(ObisId.POSITIVE_ACTIVE_ENERGY_L1));
        assertQuantityTypeZero(Units.WATT, meter.getState(ObisId.NEGATIVE_ACTIVE_POWER_L1));
        assertQuantityTypeValue("0.1312", Units.KILOWATT_HOUR, meter.getState(ObisId.NEGATIVE_ACTIVE_ENERGY_L1));
        assertQuantityTypeValue("141.2", Units.WATT, meter.getState(ObisId.POSITIVE_ACTIVE_POWER_L2));
        assertQuantityTypeValue("0.0002041667", Units.KILOWATT_HOUR, meter.getState(ObisId.POSITIVE_ACTIVE_ENERGY_L2));
        assertQuantityTypeZero(Units.WATT, meter.getState(ObisId.NEGATIVE_ACTIVE_POWER_L2));
        assertQuantityTypeValue("0.6252", Units.KILOWATT_HOUR, meter.getState(ObisId.NEGATIVE_ACTIVE_ENERGY_L2));
        assertQuantityTypeValue("223.9", Units.WATT, meter.getState(ObisId.POSITIVE_ACTIVE_POWER_L3));
        assertQuantityTypeValue("1.3646", Units.KILOWATT_HOUR, meter.getState(ObisId.POSITIVE_ACTIVE_ENERGY_L3));
        assertQuantityTypeZero(Units.WATT, meter.getState(ObisId.NEGATIVE_ACTIVE_POWER_L3));
        assertQuantityTypeZero(Units.KILOWATT_HOUR, meter.getState(ObisId.NEGATIVE_ACTIVE_ENERGY_L3));
        assertQuantityTypeValue("0", Units.VAR, meter.getState(ObisId.POSITIVE_REACTIVE_POWER));
        assertQuantityTypeValue("181.4", Units.VAR, meter.getState(ObisId.NEGATIVE_REACTIVE_POWER));
        assertQuantityTypeValue("541.8", Units.VOLT_AMPERE, meter.getState(ObisId.POSITIVE_APPARENT_POWER));
        assertQuantityTypeValue("0", Units.VOLT_AMPERE, meter.getState(ObisId.NEGATIVE_APPARENT_POWER));
        assertQuantityTypeValue("0.942", Units.ONE, meter.getState(ObisId.POWER_FACTOR));
        assertQuantityTypeValue("0.941", Units.ONE, meter.getState(ObisId.POWER_FACTOR_L1));
        assertQuantityTypeValue("0.848", Units.ONE, meter.getState(ObisId.POWER_FACTOR_L2));
        assertQuantityTypeValue("0.983", Units.ONE, meter.getState(ObisId.POWER_FACTOR_L3));
        assertQuantityTypeValue("0.671", Units.AMPERE, meter.getState(ObisId.CURRENT_L1));
        assertQuantityTypeValue("16.396", Units.AMPERE, meter.getState(ObisId.CURRENT_L2));
        assertQuantityTypeValue("1.132", Units.AMPERE, meter.getState(ObisId.CURRENT_L3));
        assertEquals(new StringType("1.1.0.R"), meter.getVersion());
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

    private static void assertQuantityTypeValue(String expectedValue, javax.measure.Unit<?> expectedUnit, State state) {
        QuantityType<?> qt = (QuantityType<?>) state;
        assertEquals(expectedValue, qt.toBigDecimal().stripTrailingZeros().toPlainString());
        assertEquals(expectedUnit, qt.getUnit());
    }

    private static void assertQuantityTypeZero(javax.measure.Unit<?> expectedUnit, State state) {
        assertQuantityTypeValue("0", expectedUnit, state);
    }
}
