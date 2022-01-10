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
package org.openhab.binding.rfxcom.internal.messages;

import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;
import org.openhab.core.util.HexUtils;

/**
 * Base class for RFXCOM data classes. All other data classes should extend this class.
 *
 * @author Pauli Anttila - Initial contribution
 */
public abstract class RFXComBaseMessage implements RFXComMessage {

    public static final String ID_DELIMITER = ".";

    public enum PacketType implements ByteEnumWrapper {
        INTERFACE_CONTROL(0),
        INTERFACE_MESSAGE(1),
        TRANSMITTER_MESSAGE(2),
        UNDECODED_RF_MESSAGE(3),
        LIGHTING1(16),
        LIGHTING2(17),
        LIGHTING3(18),
        LIGHTING4(19),
        LIGHTING5(20),
        LIGHTING6(21),
        CHIME(22),
        FAN(23, RFXComFanMessage.SubType.LUCCI_AIR_FAN, RFXComFanMessage.SubType.WESTINGHOUSE_7226640,
                RFXComFanMessage.SubType.CASAFAN),
        FAN_SF01(23, RFXComFanMessage.SubType.SF01),
        FAN_ITHO(23, RFXComFanMessage.SubType.CVE_RFT),
        FAN_LUCCI_DC(23, RFXComFanMessage.SubType.LUCCI_AIR_DC),
        FAN_LUCCI_DC_II(23, RFXComFanMessage.SubType.LUCCI_AIR_DC_II),
        FAN_SEAV(23, RFXComFanMessage.SubType.SEAV_TXS4),
        FAN_FT1211R(23, RFXComFanMessage.SubType.FT1211R),
        FAN_FALMEC(23, RFXComFanMessage.SubType.FALMEC),
        FAN_ITHO_CVE_ECO_RFT(23, RFXComFanMessage.SubType.ITHO_CVE_ECO_RFT),
        FAN_NOVY(23, RFXComFanMessage.SubType.NOVY),
        CURTAIN1(24),
        BLINDS1(25),
        RFY(26),
        HOME_CONFORT(27),
        EDISIO(28),
        SECURITY1(32),
        SECURITY2(33),
        CAMERA1(40),
        REMOTE_CONTROL(48),
        THERMOSTAT1(64),
        THERMOSTAT2(65),
        THERMOSTAT3(66),
        THERMOSTAT4(67),
        RADIATOR1(72),
        BBQ(78),
        TEMPERATURE_RAIN(79),
        TEMPERATURE(80),
        HUMIDITY(81),
        TEMPERATURE_HUMIDITY(82),
        BAROMETRIC(83),
        TEMPERATURE_HUMIDITY_BAROMETRIC(84),
        RAIN(85),
        WIND(86),
        UV(87),
        DATE_TIME(88),
        CURRENT(89),
        ENERGY(90),
        CURRENT_ENERGY(91),
        POWER(92),
        WEIGHT(93),
        GAS(94),
        WATER(95),
        CARTELECTRONIC(96),
        RFXSENSOR(112),
        RFXMETER(113),
        FS20(114),
        RAW(127),
        IO_LINES(128);

        private final int packetType;
        private final ByteEnumWrapper[] subTypes;

        PacketType(int packetType, ByteEnumWrapper... subTypes) {
            this.packetType = packetType;
            this.subTypes = subTypes;
        }

        @Override
        public byte toByte() {
            return (byte) packetType;
        }
    }

    public byte[] rawMessage;
    private PacketType packetType;
    public byte packetId;
    public byte subType;
    public byte seqNbr;
    public byte id1;
    public byte id2;

    public RFXComBaseMessage() {
    }

    public RFXComBaseMessage(PacketType packetType) {
        this.packetType = packetType;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        rawMessage = data;

        packetId = data[1];
        packetType = fromByte(data[1], data[2]);
        subType = data[2];
        seqNbr = data[3];
        id1 = data[4];

        if (data.length > 5) {
            id2 = data[5];
        }
    }

    private PacketType fromByte(byte packetId, byte subType) throws RFXComUnsupportedValueException {
        for (PacketType enumValue : PacketType.values()) {
            if (enumValue.toByte() == packetId) {
                // if there are no subtypes?
                if (enumValue.subTypes.length == 0) {
                    return enumValue;
                }
                // otherwise check for the matching subType
                for (ByteEnumWrapper e : enumValue.subTypes) {
                    if (e.toByte() == subType) {
                        return enumValue;
                    }
                }
            }
        }

        throw new RFXComUnsupportedValueException(PacketType.class, packetId);
    }

    public PacketType getPacketType() {
        return packetType;
    }

    @Override
    public String toString() {
        String str;

        if (rawMessage == null) {
            str = "Raw data = unknown";
        } else {
            str = "Raw data = " + HexUtils.bytesToHex(rawMessage);
        }

        str += ", Packet type = " + packetType;
        str += ", Seq number = " + Byte.toUnsignedInt(seqNbr);

        return str;
    }

    @Override
    public void setConfig(RFXComDeviceConfiguration deviceConfiguration) throws RFXComException {
        // noop
    }
}
