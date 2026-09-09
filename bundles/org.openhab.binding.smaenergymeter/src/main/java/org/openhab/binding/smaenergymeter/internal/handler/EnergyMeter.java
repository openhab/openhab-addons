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

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smaenergymeter.internal.SerialNumber;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

/**
 * The {@link EnergyMeter} class is responsible for communication with the SMA device
 * and extracting the data fields out of the received telegrams.
 *
 * @author Osman Basha - Initial contribution
 * @author Łukasz Dywicki - Extracted multicast group handling to
 *         {@link org.openhab.binding.smaenergymeter.internal.packet.PacketListener}.
 * @author Marcel Goerentz - Refactor OBIS parsing, add power factor, reactive/apparent power, voltage, current, and
 *         frequency
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

    public State getState(ObisId obisId) {
        return getQuantityType(obisId);
    }

    public StringType getVersion() {
        return version;
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

    private QuantityType<?> getQuantityType(ObisId obisId) {
        BigDecimal value = values.getOrDefault(obisId, BigDecimal.ZERO);
        Unit<?> unit = switch (obisId) {
            case POSITIVE_REACTIVE_POWER, POSITIVE_REACTIVE_POWER_L1, POSITIVE_REACTIVE_POWER_L2,
                    POSITIVE_REACTIVE_POWER_L3, NEGATIVE_REACTIVE_POWER, NEGATIVE_REACTIVE_POWER_L1,
                    NEGATIVE_REACTIVE_POWER_L2, NEGATIVE_REACTIVE_POWER_L3 ->
                (Unit<?>) Units.VAR;
            case POSITIVE_REACTIVE_ENERGY, POSITIVE_REACTIVE_ENERGY_L1, POSITIVE_REACTIVE_ENERGY_L2,
                    POSITIVE_REACTIVE_ENERGY_L3, NEGATIVE_REACTIVE_ENERGY, NEGATIVE_REACTIVE_ENERGY_L1,
                    NEGATIVE_REACTIVE_ENERGY_L2, NEGATIVE_REACTIVE_ENERGY_L3 ->
                (Unit<?>) Units.KILOVAR_HOUR;
            case POSITIVE_APPARENT_POWER, POSITIVE_APPARENT_POWER_L1, POSITIVE_APPARENT_POWER_L2,
                    POSITIVE_APPARENT_POWER_L3, NEGATIVE_APPARENT_POWER, NEGATIVE_APPARENT_POWER_L1,
                    NEGATIVE_APPARENT_POWER_L2, NEGATIVE_APPARENT_POWER_L3 ->
                (Unit<?>) Units.VOLT_AMPERE;
            case POSITIVE_APPARENT_ENERGY, POSITIVE_APPARENT_ENERGY_L1, POSITIVE_APPARENT_ENERGY_L2,
                    POSITIVE_APPARENT_ENERGY_L3, NEGATIVE_APPARENT_ENERGY, NEGATIVE_APPARENT_ENERGY_L1,
                    NEGATIVE_APPARENT_ENERGY_L2, NEGATIVE_APPARENT_ENERGY_L3 ->
                (Unit<?>) Units.VOLT_AMPERE_HOUR;
            case POSITIVE_ACTIVE_POWER, POSITIVE_ACTIVE_POWER_L1, POSITIVE_ACTIVE_POWER_L2, POSITIVE_ACTIVE_POWER_L3,
                    NEGATIVE_ACTIVE_POWER, NEGATIVE_ACTIVE_POWER_L1, NEGATIVE_ACTIVE_POWER_L2,
                    NEGATIVE_ACTIVE_POWER_L3 ->
                (Unit<?>) Units.WATT;
            case POSITIVE_ACTIVE_ENERGY, POSITIVE_ACTIVE_ENERGY_L1, POSITIVE_ACTIVE_ENERGY_L2,
                    POSITIVE_ACTIVE_ENERGY_L3, NEGATIVE_ACTIVE_ENERGY, NEGATIVE_ACTIVE_ENERGY_L1,
                    NEGATIVE_ACTIVE_ENERGY_L2, NEGATIVE_ACTIVE_ENERGY_L3 ->
                (Unit<?>) Units.KILOWATT_HOUR;
            case POWER_FACTOR, POWER_FACTOR_L1, POWER_FACTOR_L2, POWER_FACTOR_L3 -> (Unit<?>) Units.ONE;
            case CURRENT_L1, CURRENT_L2, CURRENT_L3 -> (Unit<?>) Units.AMPERE;
            case VOLTAGE_L1, VOLTAGE_L2, VOLTAGE_L3 -> (Unit<?>) Units.VOLT;
            case FREQUENCY -> (Unit<?>) Units.HERTZ;
            default -> throw new IllegalArgumentException("Unsupported OBIS id for quantity state: " + obisId);
        };
        return new QuantityType<>(value, unit);
    }
}
