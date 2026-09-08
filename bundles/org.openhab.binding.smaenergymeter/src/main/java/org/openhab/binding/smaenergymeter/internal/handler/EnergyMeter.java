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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smaenergymeter.internal.SerialNumber;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;

/**
 * The {@link EnergyMeter} class is responsible for communication with the SMA device
 * and extracting the data fields out of the received telegrams.
 *
 * @author Osman Basha - Initial contribution
 * @author Łukasz Dywicki - Extracted multicast group handling to
 *         {@link org.openhab.binding.smaenergymeter.internal.packet.PacketListener}.
 */
@NonNullByDefault
public class EnergyMeter {

    private static final byte[] E_METER_PROTOCOL_ID = new byte[] { 0x60, 0x69 };
    private static final int HEADER_LENGTH = 28;
    private static final int VERSION_LENGTH = 4;

    private String serialNumber = "";
    private final EnumMap<ObisId, BigDecimal> values = new EnumMap<>(ObisId.class);
    private StringType version = StringType.EMPTY;

    public void parse(byte[] bytes) throws IOException {
        try {
            String sma = new String(Arrays.copyOfRange(bytes, 0, 3), StandardCharsets.US_ASCII);
            if (!"SMA".equals(sma)) {
                throw new IOException("Not a SMA telegram." + sma);
            }
            byte[] protocolId = Arrays.copyOfRange(bytes, 16, 18);
            if (!Arrays.equals(protocolId, E_METER_PROTOCOL_ID)) {
                throw new IllegalArgumentException(
                        "Received frame with wrong protocol ID " + Arrays.toString(protocolId));
            }

            ByteBuffer buffer = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 0x14, 0x18));
            serialNumber = SerialNumber.fromRaw(buffer.getInt());
            values.clear();
            version = StringType.EMPTY;

            int offset = HEADER_LENGTH;
            while (offset + Integer.BYTES <= bytes.length) {
                int obis = readInt32(bytes, offset);
                offset += Integer.BYTES;

                int valueLength = switch (obis & 0x0000FF00) {
                    case 0x00000400 -> Integer.BYTES;
                    case 0x00000800 -> Long.BYTES;
                    default -> 0;
                };
                if (obis == ObisId.VERSION.getCode()) {
                    valueLength = VERSION_LENGTH;
                }
                if (valueLength == 0) {
                    break;
                }
                if (offset + valueLength > bytes.length) {
                    break;
                }

                ObisId obisId = ObisId.fromCode(obis);
                if (obisId == ObisId.VERSION) {
                    version = decodeVersion(bytes, offset);
                } else if (obisId != null) {
                    long rawValue = valueLength == Integer.BYTES ? readInt32(bytes, offset) : readUint64(bytes, offset);
                    values.put(obisId, scaleValue(rawValue, obisId));
                }
                offset += valueLength;
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public DecimalType getPowerIn() {
        return getDecimalType(ObisId.POSITIVE_ACTIVE_POWER);
    }

    public DecimalType getPowerOut() {
        return getDecimalType(ObisId.NEGATIVE_ACTIVE_POWER);
    }

    public DecimalType getEnergyIn() {
        return getDecimalType(ObisId.POSITIVE_ACTIVE_ENERGY);
    }

    public DecimalType getEnergyOut() {
        return getDecimalType(ObisId.NEGATIVE_ACTIVE_ENERGY);
    }

    public DecimalType getPowerInL1() {
        return getDecimalType(ObisId.POSITIVE_ACTIVE_POWER_L1);
    }

    public DecimalType getPowerOutL1() {
        return getDecimalType(ObisId.NEGATIVE_ACTIVE_POWER_L1);
    }

    public DecimalType getEnergyInL1() {
        return getDecimalType(ObisId.POSITIVE_ACTIVE_ENERGY_L1);
    }

    public DecimalType getEnergyOutL1() {
        return getDecimalType(ObisId.NEGATIVE_ACTIVE_ENERGY_L1);
    }

    public DecimalType getPowerInL2() {
        return getDecimalType(ObisId.POSITIVE_ACTIVE_POWER_L2);
    }

    public DecimalType getPowerOutL2() {
        return getDecimalType(ObisId.NEGATIVE_ACTIVE_POWER_L2);
    }

    public DecimalType getEnergyInL2() {
        return getDecimalType(ObisId.POSITIVE_ACTIVE_ENERGY_L2);
    }

    public DecimalType getEnergyOutL2() {
        return getDecimalType(ObisId.NEGATIVE_ACTIVE_ENERGY_L2);
    }

    public DecimalType getPowerInL3() {
        return getDecimalType(ObisId.POSITIVE_ACTIVE_POWER_L3);
    }

    public DecimalType getPowerOutL3() {
        return getDecimalType(ObisId.NEGATIVE_ACTIVE_POWER_L3);
    }

    public DecimalType getEnergyInL3() {
        return getDecimalType(ObisId.POSITIVE_ACTIVE_ENERGY_L3);
    }

    public DecimalType getEnergyOutL3() {
        return getDecimalType(ObisId.NEGATIVE_ACTIVE_ENERGY_L3);
    }

    public DecimalType getReactivePowerIn() {
        return getDecimalType(ObisId.POSITIVE_REACTIVE_POWER);
    }

    public DecimalType getReactivePowerOut() {
        return getDecimalType(ObisId.NEGATIVE_REACTIVE_POWER);
    }

    public DecimalType getReactiveEnergyIn() {
        return getDecimalType(ObisId.POSITIVE_REACTIVE_ENERGY);
    }

    public DecimalType getReactiveEnergyOut() {
        return getDecimalType(ObisId.NEGATIVE_REACTIVE_ENERGY);
    }

    public DecimalType getReactivePowerInL1() {
        return getDecimalType(ObisId.POSITIVE_REACTIVE_POWER_L1);
    }

    public DecimalType getReactivePowerOutL1() {
        return getDecimalType(ObisId.NEGATIVE_REACTIVE_POWER_L1);
    }

    public DecimalType getReactiveEnergyInL1() {
        return getDecimalType(ObisId.POSITIVE_REACTIVE_ENERGY_L1);
    }

    public DecimalType getReactiveEnergyOutL1() {
        return getDecimalType(ObisId.NEGATIVE_REACTIVE_ENERGY_L1);
    }

    public DecimalType getReactivePowerInL2() {
        return getDecimalType(ObisId.POSITIVE_REACTIVE_POWER_L2);
    }

    public DecimalType getReactivePowerOutL2() {
        return getDecimalType(ObisId.NEGATIVE_REACTIVE_POWER_L2);
    }

    public DecimalType getReactiveEnergyInL2() {
        return getDecimalType(ObisId.POSITIVE_REACTIVE_ENERGY_L2);
    }

    public DecimalType getReactiveEnergyOutL2() {
        return getDecimalType(ObisId.NEGATIVE_REACTIVE_ENERGY_L2);
    }

    public DecimalType getReactivePowerInL3() {
        return getDecimalType(ObisId.POSITIVE_REACTIVE_POWER_L3);
    }

    public DecimalType getReactivePowerOutL3() {
        return getDecimalType(ObisId.NEGATIVE_REACTIVE_POWER_L3);
    }

    public DecimalType getReactiveEnergyInL3() {
        return getDecimalType(ObisId.POSITIVE_REACTIVE_ENERGY_L3);
    }

    public DecimalType getReactiveEnergyOutL3() {
        return getDecimalType(ObisId.NEGATIVE_REACTIVE_ENERGY_L3);
    }

    public DecimalType getApparentPowerIn() {
        return getDecimalType(ObisId.POSITIVE_APPARENT_POWER);
    }

    public DecimalType getApparentPowerOut() {
        return getDecimalType(ObisId.NEGATIVE_APPARENT_POWER);
    }

    public DecimalType getApparentEnergyIn() {
        return getDecimalType(ObisId.POSITIVE_APPARENT_ENERGY);
    }

    public DecimalType getApparentEnergyOut() {
        return getDecimalType(ObisId.NEGATIVE_APPARENT_ENERGY);
    }

    public DecimalType getApparentPowerInL1() {
        return getDecimalType(ObisId.POSITIVE_APPARENT_POWER_L1);
    }

    public DecimalType getApparentPowerOutL1() {
        return getDecimalType(ObisId.NEGATIVE_APPARENT_POWER_L1);
    }

    public DecimalType getApparentEnergyInL1() {
        return getDecimalType(ObisId.POSITIVE_APPARENT_ENERGY_L1);
    }

    public DecimalType getApparentEnergyOutL1() {
        return getDecimalType(ObisId.NEGATIVE_APPARENT_ENERGY_L1);
    }

    public DecimalType getApparentPowerInL2() {
        return getDecimalType(ObisId.POSITIVE_APPARENT_POWER_L2);
    }

    public DecimalType getApparentPowerOutL2() {
        return getDecimalType(ObisId.NEGATIVE_APPARENT_POWER_L2);
    }

    public DecimalType getApparentEnergyInL2() {
        return getDecimalType(ObisId.POSITIVE_APPARENT_ENERGY_L2);
    }

    public DecimalType getApparentEnergyOutL2() {
        return getDecimalType(ObisId.NEGATIVE_APPARENT_ENERGY_L2);
    }

    public DecimalType getApparentPowerInL3() {
        return getDecimalType(ObisId.POSITIVE_APPARENT_POWER_L3);
    }

    public DecimalType getApparentPowerOutL3() {
        return getDecimalType(ObisId.NEGATIVE_APPARENT_POWER_L3);
    }

    public DecimalType getApparentEnergyInL3() {
        return getDecimalType(ObisId.POSITIVE_APPARENT_ENERGY_L3);
    }

    public DecimalType getApparentEnergyOutL3() {
        return getDecimalType(ObisId.NEGATIVE_APPARENT_ENERGY_L3);
    }

    public DecimalType getPowerFactor() {
        return getDecimalType(ObisId.POWER_FACTOR);
    }

    public DecimalType getPowerFactorL1() {
        return getDecimalType(ObisId.POWER_FACTOR_L1);
    }

    public DecimalType getPowerFactorL2() {
        return getDecimalType(ObisId.POWER_FACTOR_L2);
    }

    public DecimalType getPowerFactorL3() {
        return getDecimalType(ObisId.POWER_FACTOR_L3);
    }

    public DecimalType getCurrentL1() {
        return getDecimalType(ObisId.CURRENT_L1);
    }

    public DecimalType getCurrentL2() {
        return getDecimalType(ObisId.CURRENT_L2);
    }

    public DecimalType getCurrentL3() {
        return getDecimalType(ObisId.CURRENT_L3);
    }

    public DecimalType getVoltageL1() {
        return getDecimalType(ObisId.VOLTAGE_L1);
    }

    public DecimalType getVoltageL2() {
        return getDecimalType(ObisId.VOLTAGE_L2);
    }

    public DecimalType getVoltageL3() {
        return getDecimalType(ObisId.VOLTAGE_L3);
    }

    public DecimalType getFrequency() {
        return getDecimalType(ObisId.FREQUENCY);
    }

    public StringType getVersion() {
        return version;
    }

    private DecimalType getDecimalType(ObisId obisId) {
        return new DecimalType(values.getOrDefault(obisId, BigDecimal.ZERO));
    }

    private BigDecimal scaleValue(long rawValue, ObisId obisId) {
        return BigDecimal.valueOf(rawValue).divide(BigDecimal.valueOf(obisId.getDivider()), 10, RoundingMode.HALF_UP)
                .stripTrailingZeros();
    }

    private StringType decodeVersion(byte[] bytes, int offset) {
        int major = bytes[offset] & 0xFF;
        int minor = bytes[offset + 1] & 0xFF;
        int build = bytes[offset + 2] & 0xFF;
        char revision = (char) (bytes[offset + 3] & 0xFF);
        return new StringType("%d.%d.%d.%s".formatted(major, minor, build, revision == 0 ? "N" : revision));
    }

    private int readInt32(byte[] bytes, int offset) {
        return ByteBuffer.wrap(bytes, offset, Integer.BYTES).getInt();
    }

    private long readUint64(byte[] bytes, int offset) {
        return ByteBuffer.wrap(bytes, offset, Long.BYTES).getLong();
    }
}
