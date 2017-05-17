/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for interface message.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComInterfaceMessage extends RFXComBaseMessage {

    public enum SubType {
        UNKNOWN_COMMAND(-1),
        RESPONSE(0),
        UNKNOWN_RTS_REMOTE(1),
        NO_EXTENDED_HW_PRESENT(2),
        LIST_RFY_REMOTES(3),
        LIST_ASA_REMOTES(4),
        START_RECEIVER(7);

        private final int subType;

        SubType(int subType) {
            this.subType = subType;
        }

        SubType(byte subType) {
            this.subType = subType;
        }

        public byte toByte() {
            return (byte) subType;
        }

        public static SubType fromByte(int input) throws RFXComUnsupportedValueException {
            for (SubType subType : SubType.values()) {
                if (subType.subType == input) {
                    return subType;
                }
            }

            throw new RFXComUnsupportedValueException(SubType.class, input);
        }
    }

    public enum Commands {
        RESET(0), // Reset the receiver/transceiver. No answer is transmitted!
        GET_STATUS(2), // Get Status, return firmware versions and configuration of the interface
        SET_MODE(3), // Set mode msg1-msg5, return firmware versions and configuration of the interface
        ENABLE_ALL(4), // Enable all receiving modes of the receiver/transceiver
        ENABLE_UNDECODED_PACKETS(5), // Enable reporting of undecoded packets
        SAVE_RECEIVING_MODES(6), // Save receiving modes of the receiver/transceiver in non-volatile memory
        START_RECEIVER(7), // Start RFXtrx receiver
        T1(8), // For internal use by RFXCOM
        T2(9), // For internal use by RFXCOM

        UNSUPPORTED_COMMAND(-1); // wrong command received from the application

        private final int command;

        Commands(int command) {
            this.command = command;
        }

        public byte toByte() {
            return (byte) command;
        }

        public static Commands fromByte(int input) throws RFXComUnsupportedValueException {
            for (Commands command : Commands.values()) {
                if (command.command == input) {
                    return command;
                }
            }

            throw new RFXComUnsupportedValueException(Commands.class, input);
        }
    }

    public enum TransceiverType {
        _310MHZ(80),
        _315MHZ(81),
        _433_92MHZ_RECEIVER_ONLY(82),
        _433_92MHZ_TRANSCEIVER(83),
        _868_00MHZ(85),
        _868_00MHZ_FSK(86),
        _868_30MHZ(87),
        _868_30MHZ_FSK(88),
        _868_35MHZ(89),
        _868_35MHZ_FSK(90),
        _868_95MHZ_FSK(91);

        private final int type;

        TransceiverType(int type) {
            this.type = type;
        }

        TransceiverType(byte type) {
            this.type = type;
        }

        public byte toByte() {
            return (byte) type;
        }

        public static TransceiverType fromByte(int input) throws RFXComUnsupportedValueException {
            for (TransceiverType type : TransceiverType.values()) {
                if (type.type == input) {
                    return type;
                }
            }

            throw new RFXComUnsupportedValueException(TransceiverType.class, input);
        }
    }

    public SubType subType;
    public Commands command;
    public String text = "";

    public TransceiverType transceiverType;
    public int firmwareVersion;

    public boolean enableUndecodedPackets; // 0x80 - Undecoded packets
    public boolean enableImagintronixOpusPackets; // 0x40 - Imagintronix/Opus (433.92)
    public boolean enableByronSXPackets; // 0x20 - Byron SX (433.92)
    public boolean enableRSLPackets; // 0x10 - RSL (433.92)
    public boolean enableLighting4Packets; // 0x08 - Lighting4 (433.92)
    public boolean enableFineOffsetPackets; // 0x04 - FineOffset / Viking (433.92)
    public boolean enableRubicsonPackets; // 0x02 - Rubicson (433.92)
    public boolean enableAEPackets; // 0x01 - AE (433.92)

    public boolean enableBlindsT1T2T3T4Packets; // 0x80 - BlindsT1/T2/T3/T4 (433.92)
    public boolean enableBlindsT0Packets; // 0x40 - BlindsT0 (433.92)
    public boolean enableProGuardPackets; // 0x20 - ProGuard (868.35 FSK)
    public boolean enableFS20Packets; // 0x10 - FS20 (868.35)
    public boolean enableLaCrossePackets; // 0x08 - La Crosse (433.92/868.30)
    public boolean enableHidekiUPMPackets; // 0x04 - Hideki/UPM (433.92)
    public boolean enableADPackets; // 0x02 - AD LightwaveRF (433.92)
    public boolean enableMertikPackets; // 0x01 - Mertik (433.92)

    public boolean enableVisonicPackets; // 0x80 - Visonic (315/868.95)
    public boolean enableATIPackets; // 0x40 - ATI (433.92)
    public boolean enableOregonPackets; // 0x20 - Oregon Scientific (433.92)
    public boolean enableMeiantechPackets; // 0x10 - Meiantech (433.92)
    public boolean enableHomeEasyPackets; // 0x08 - HomeEasy EU (433.92)
    public boolean enableACPackets; // 0x04 - AC (433.92)
    public boolean enableARCPackets; // 0x02 - ARC (433.92)
    public boolean enableX10Packets; // 0x01 - X10 (310/433.92)

    public byte hardwareVersion1;
    public byte hardwareVersion2;

    public RFXComInterfaceMessage() {

    }

    public RFXComInterfaceMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Command = " + command;

        if (subType == SubType.RESPONSE) {

            str += ", Transceiver type = " + transceiverType;
            str += ", Firmware version = " + firmwareVersion;
            str += ", Hardware version = " + hardwareVersion1 + "." + hardwareVersion2;
            str += ", Undecoded packets = " + enableUndecodedPackets;
            str += ", RFU6 packets = " + enableImagintronixOpusPackets;
            str += ", Byron SX packets packets (433.92) = " + enableByronSXPackets;
            str += ", RSL packets packets (433.92) = " + enableRSLPackets;
            str += ", Lighting4 packets (433.92) = " + enableLighting4Packets;
            str += ", FineOffset / Viking (433.92) packets = " + enableFineOffsetPackets;
            str += ", Rubicson (433.92) packets = " + enableRubicsonPackets;
            str += ", AE (433.92) packets = " + enableAEPackets;

            str += ", BlindsT1/T2/T3 (433.92) packets = " + enableBlindsT1T2T3T4Packets;
            str += ", BlindsT0 (433.92) packets = " + enableBlindsT0Packets;
            str += ", ProGuard (868.35 FSK) packets = " + enableProGuardPackets;
            str += ", FS20 (868.35) packets = " + enableFS20Packets;
            str += ", La Crosse (433.92/868.30) packets = " + enableLaCrossePackets;
            str += ", Hideki/UPM (433.92) packets = " + enableHidekiUPMPackets;
            str += ", AD LightwaveRF (433.92) packets = " + enableADPackets;
            str += ", Mertik (433.92) packets = " + enableMertikPackets;

            str += ", Visonic (315/868.95) packets = " + enableVisonicPackets;
            str += ", ATI (433.92) packets = " + enableATIPackets;
            str += ", Oregon Scientific (433.92) packets = " + enableOregonPackets;
            str += ", Meiantech (433.92) packets = " + enableMeiantechPackets;
            str += ", HomeEasy EU (433.92) packets = " + enableHomeEasyPackets;
            str += ", AC (433.92) packets = " + enableACPackets;
            str += ", ARC (433.92) packets = " + enableARCPackets;
            str += ", X10 (310/433.92) packets = " + enableX10Packets;
        } else if (subType == SubType.START_RECEIVER) {
            str += ", Text = " + text;
        }

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {

        super.encodeMessage(data);

        subType = SubType.fromByte(super.subType);
        command = Commands.fromByte(data[4]);

        if (subType == SubType.RESPONSE) {
            transceiverType = TransceiverType.fromByte(data[5]);

            firmwareVersion = data[6] & 0xFF;

            enableUndecodedPackets = (data[7] & 0x80) != 0;
            enableImagintronixOpusPackets = (data[7] & 0x40) != 0;
            enableByronSXPackets = (data[7] & 0x20) != 0;
            enableRSLPackets = (data[7] & 0x10) != 0;
            enableLighting4Packets = (data[7] & 0x08) != 0;
            enableFineOffsetPackets = (data[7] & 0x04) != 0;
            enableRubicsonPackets = (data[7] & 0x02) != 0;
            enableAEPackets = (data[7] & 0x01) != 0;

            enableBlindsT1T2T3T4Packets = (data[8] & 0x80) != 0;
            enableBlindsT0Packets = (data[8] & 0x40) != 0;
            enableProGuardPackets = (data[8] & 0x20) != 0;
            enableFS20Packets = (data[8] & 0x10) != 0;
            enableLaCrossePackets = (data[8] & 0x08) != 0;
            enableHidekiUPMPackets = (data[8] & 0x04) != 0;
            enableADPackets = (data[8] & 0x02) != 0;
            enableMertikPackets = (data[8] & 0x01) != 0;

            enableVisonicPackets = (data[9] & 0x80) != 0;
            enableATIPackets = (data[9] & 0x40) != 0;
            enableOregonPackets = (data[9] & 0x20) != 0;
            enableMeiantechPackets = (data[9] & 0x10) != 0;
            enableHomeEasyPackets = (data[9] & 0x08) != 0;
            enableACPackets = (data[9] & 0x04) != 0;
            enableARCPackets = (data[9] & 0x02) != 0;
            enableX10Packets = (data[9] & 0x01) != 0;

            hardwareVersion1 = data[10];
            hardwareVersion2 = data[11];

            text = "";

        } else if (subType == SubType.START_RECEIVER) {
            final int len = 16;
            final int dataOffset = 5;

            byte[] byteArray = new byte[len];

            for (int i = dataOffset; i < (dataOffset + len); i++) {
                byteArray[i - dataOffset] += data[i];
            }

            try {
                text = new String(byteArray, "ASCII");
            } catch (UnsupportedEncodingException e) {
                // ignore
            }
        }
    }

    @Override
    public byte[] decodeMessage() {

        byte[] data = new byte[14];

        data[0] = 0x0D;
        data[1] = RFXComBaseMessage.PacketType.INTERFACE_MESSAGE.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = command.toByte();
        data[5] = transceiverType.toByte();
        data[6] = 0;

        data[7] |= enableUndecodedPackets ? 0x80 : 0x00;
        data[7] |= enableImagintronixOpusPackets ? 0x40 : 0x00;
        data[7] |= enableByronSXPackets ? 0x20 : 0x00;
        data[7] |= enableRSLPackets ? 0x10 : 0x00;
        data[7] |= enableLighting4Packets ? 0x08 : 0x00;
        data[7] |= enableFineOffsetPackets ? 0x04 : 0x00;
        data[7] |= enableRubicsonPackets ? 0x02 : 0x00;
        data[7] |= enableAEPackets ? 0x01 : 0x00;

        data[8] |= enableBlindsT1T2T3T4Packets ? 0x80 : 0x00;
        data[8] |= enableBlindsT0Packets ? 0x40 : 0x00;
        data[8] |= enableProGuardPackets ? 0x20 : 0x00;
        data[8] |= enableFS20Packets ? 0x10 : 0x00;
        data[8] |= enableLaCrossePackets ? 0x08 : 0x00;
        data[8] |= enableHidekiUPMPackets ? 0x04 : 0x00;
        data[8] |= enableADPackets ? 0x02 : 0x00;
        data[8] |= enableMertikPackets ? 0x01 : 0x00;

        data[9] |= enableVisonicPackets ? 0x80 : 0x00;
        data[9] |= enableATIPackets ? 0x40 : 0x00;
        data[9] |= enableOregonPackets ? 0x20 : 0x00;
        data[9] |= enableMeiantechPackets ? 0x10 : 0x00;
        data[9] |= enableHomeEasyPackets ? 0x08 : 0x00;
        data[9] |= enableACPackets ? 0x04 : 0x00;
        data[9] |= enableARCPackets ? 0x02 : 0x00;
        data[9] |= enableX10Packets ? 0x01 : 0x00;

        data[10] = hardwareVersion1;
        data[11] = hardwareVersion2;
        data[12] = 0;
        data[13] = 0;

        return data;
    }

    @Override
    public State convertToState(RFXComValueSelector valueSelector) throws RFXComException {

        throw new RFXComException("Not supported");
    }

    @Override
    public void setSubType(Object subType) throws RFXComException {
        throw new RFXComException("Not supported");
    }

    @Override
    public void setDeviceId(String deviceId) throws RFXComException {
        throw new RFXComException("Not supported");
    }

    @Override
    public void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException {

        throw new RFXComException("Not supported");
    }

    @Override
    public Object convertSubType(String subType) throws RFXComException {

        for (SubType s : SubType.values()) {
            if (s.toString().equals(subType)) {
                return s;
            }
        }

        try {
            return SubType.fromByte(Integer.parseInt(subType));
        } catch (NumberFormatException e) {
            throw new RFXComUnsupportedValueException(SubType.class, subType);
        }
    }

    @Override
    public List<RFXComValueSelector> getSupportedInputValueSelectors() throws RFXComException {
        return null;
    }

    @Override
    public List<RFXComValueSelector> getSupportedOutputValueSelectors() throws RFXComException {
        return null;
    }

}
