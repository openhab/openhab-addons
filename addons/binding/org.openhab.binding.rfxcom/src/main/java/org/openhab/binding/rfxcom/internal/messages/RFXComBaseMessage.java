/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import static org.openhab.binding.rfxcom.internal.messages.ByteEnumUtil.fromByte;

import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

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
        FAN(23),
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
        IO_LINES(128);

        private final int packetType;

        PacketType(int packetType) {
            this.packetType = packetType;
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
        packetType = fromByte(PacketType.class, data[1]);
        subType = data[2];
        seqNbr = data[3];
        id1 = data[4];

        if (data.length > 5) {
            id2 = data[5];
        }
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
        str += ", Seq number = " + (short) (seqNbr & 0xFF);

        return str;
    }

    @Override
    public void setConfig(RFXComDeviceConfiguration deviceConfiguration) throws RFXComException {
        // noop
    }
}
