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

import static org.openhab.binding.rfxcom.internal.messages.ByteEnumUtil.fromByte;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RFXCOM data class for interface message.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Ivan Martinez - Older firmware support (OH1)
 */
public class RFXComInterfaceMessage extends RFXComBaseMessage {

    private final Logger logger = LoggerFactory.getLogger(RFXComInterfaceMessage.class);

    public enum SubType implements ByteEnumWrapper {
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

        @Override
        public byte toByte() {
            return (byte) subType;
        }
    }

    public enum Commands implements ByteEnumWrapper {
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

        @Override
        public byte toByte() {
            return (byte) command;
        }
    }

    public enum TransceiverType implements ByteEnumWrapper {
        _310MHZ(0x50, "RFXtrx315 operating at 310MHz"),
        _315MHZ(0x51, "RFXtrx315 operating at 315MHz"),
        _433_92MHZ_RECEIVER_ONLY(0x52, "RFXrec433 operating at 433.92MHz (receiver only)"),
        _433_92MHZ_TRANSCEIVER(0x53, "RFXtrx433 operating at 433.92MHz"),
        _433_42MHZ(0x54, "RFXtrx433 operating at 433.42MHz"),
        _868_00MHZ(0x55, "RFXtrx868X operating at 868MHz"),
        _868_00MHZ_FSK(0x56, "RFXtrx868X operating at 868.00MHz FSK"),
        _868_30MHZ(0x57, "RFXtrx868X operating at 868.30MHz"),
        _868_30MHZ_FSK(0x58, "RFXtrx868X operating at 868.30MHz FSK"),
        _868_35MHZ(0x59, "RFXtrx868X operating at 868.35MHz"),
        _868_35MHZ_FSK(0x5A, "RFXtrx868X operating at 868.35MHz FSK"),
        _868_95MHZ_FSK(0x5B, "RFXtrx868X operating at 868.95MHz"),
        _433_92MHZ_IOT(0x5C, "RFXtrxIOT operating at 433.92MHz"),
        _868_00MHZ_IOT(0x5D, "RFXtrxIOT operating at 868MHz"),
        _434_50MHZ(0x5F, "RFXtrx433 operating at 434.50MHz"),
        _UNKNOWN(0xFF, "Unknown");

        private final int type;
        private final String name;

        TransceiverType(int type, String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public byte toByte() {
            return (byte) type;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum FirmwareType implements ByteEnumWrapper {
        TYPE1_RX_ONLY(0x00, "Type1 RFXrec receive only firmware"),
        TYPE1(0x01, "Type1"),
        TYPE2(0x02, "Type2"),
        EXT(0x03, "Ext"),
        EXT2(0x04, "Ext2"),
        PRO1(0x05, "Pro1"),
        PRO2(0x06, "Pro2"),
        PROXL1(0x10, "ProXL1"),
        PROXL2(0x12, "ProXL2"), // Discovered in the wild (not from RFXtrx SDK)
        UNKNOWN(0xFF, "Unknown");

        private final int type;
        private final String name;

        FirmwareType(int type, String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public byte toByte() {
            return (byte) type;
        }

        @Override
        public String toString() {
            return name;
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

    public boolean enableHomeConfortPackets; // 0x02 - HomeConfort (433.92)
    public boolean enableKEELOQPackets; // 0x01 - KEELOQ (433.92)

    public byte hardwareVersion1;
    public byte hardwareVersion2;

    public int outputPower; // -18dBm to +13dBm. N.B. maximum allowed is +10dBm

    public FirmwareType firmwareType;

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
            str += ", Hardware version = " + hardwareVersion1 + "." + hardwareVersion2;
            str += ", Firmware type = " + (firmwareType != null ? firmwareType : "unknown");
            str += ", Firmware version = " + firmwareVersion;
            str += ", Output power = " + outputPower + "dBm";
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
            str += ", FS20/Legrand CAD (868.35/433.92) packets = " + enableFS20Packets;
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

            str += ", HomeConfort (433.92) packets = " + enableHomeConfortPackets;
            str += ", KEELOQ (433.92/868.95) packets = " + enableKEELOQPackets;
        } else if (subType == SubType.START_RECEIVER) {
            str += ", Text = " + text;
        }

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);

        if (subType == SubType.RESPONSE) {
            encodeResponseMessage(data);
        } else if (subType == SubType.START_RECEIVER) {
            encodeStartReceiverMessage(data);
        } else {
            // We don't handle the other subTypes but to avoid null pointer
            // exceptions we set command to something. It doesn't really
            // matter what but it may be printed in log messages so...
            command = Commands.UNSUPPORTED_COMMAND;
        }
    }

    private void encodeResponseMessage(byte[] data) throws RFXComException {
        command = fromByte(Commands.class, data[4]);
        try {
            transceiverType = fromByte(TransceiverType.class, data[5]);
        } catch (RFXComUnsupportedValueException e) {
            transceiverType = TransceiverType._UNKNOWN;
            logger.warn(
                    "The transceiver type reported ({}) isn't known to the RFXCom binding. Please raise an issue at https://github.com/openhab/openhab-addons/ to have it included.",
                    Byte.toUnsignedInt(data[5]));
        }

        hardwareVersion1 = data[11];
        hardwareVersion2 = data[12];

        outputPower = data[13] - 18;

        /*
         * Firmware versions before 1000 did not include a firmware
         * type in their response. Instead, versions 0-99 were for
         * Type 1, versions 100-199 were for Type 2, and versions
         * above 200 were for Ext.
         *
         * From version 1000, the response includes a longer message
         * which adds a byte for firmware type. The version is calculated
         * from the single byte, to which 1000 is added.
         *
         * Discovered through hints in the release notes and experimentation
         * with RFXmngr. See RESPONSES.md for data.
         */
        if (data.length > 14) {
            firmwareVersion = Byte.toUnsignedInt(data[6]) + 1000;
            try {
                firmwareType = fromByte(FirmwareType.class, data[14]);
            } catch (RFXComUnsupportedValueException e) {
                firmwareType = FirmwareType.UNKNOWN;
                logger.warn(
                        "The firmware type reported ({}) isn't known to the RFXCom binding. Please raise an issue at https://github.com/openhab/openhab-addons/ to have it included.",
                        data[14]);
            }
        } else {
            firmwareVersion = Byte.toUnsignedInt(data[6]);

            if (firmwareVersion < 100) {
                firmwareType = FirmwareType.TYPE1;
            } else if (firmwareVersion < 200) {
                firmwareType = FirmwareType.TYPE2;
            } else {
                firmwareType = FirmwareType.EXT;
            }
        }

        /*
         * These are actually dependent on the type of device and
         * firmware, this list is mainly for RFXtrx443 at 433.92MHz,
         * which most of our users use.
         *
         * TODO: At some point, we should reconcile this with the SDK
         * and accommodate for other devices and protocols. This is
         * probably not worth doing until someone needs it and has
         * suitable devices to test with!
         */
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

        enableHomeConfortPackets = (data[10] & 0x02) != 0;
        enableKEELOQPackets = (data[10] & 0x01) != 0;

        text = "";
    }

    private void encodeStartReceiverMessage(byte[] data) throws RFXComException {
        command = fromByte(Commands.class, data[4]);

        ByteBuffer text_bytes = ByteBuffer.wrap(data, 5, data.length - 5);
        text = StandardCharsets.US_ASCII.decode(text_bytes).toString();
    }

    @Override
    public byte[] decodeMessage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void convertFromState(String channelId, Type type) {
        throw new UnsupportedOperationException();
    }

    /**
     * Command to reset RFXCOM controller.
     *
     */
    public static final byte[] CMD_RESET = new byte[] { 0x0D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00 };

    /**
     * Command to get RFXCOM controller status.
     *
     */
    public static final byte[] CMD_GET_STATUS = new byte[] { 0x0D, 0x00, 0x00, 0x01, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00 };

    /**
     * Command to save RFXCOM controller configuration.
     *
     */
    public static final byte[] CMD_SAVE = new byte[] { 0x0D, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00 };

    /**
     * Command to start RFXCOM receiver.
     *
     */
    public static final byte[] CMD_START_RECEIVER = new byte[] { 0x0D, 0x00, 0x00, 0x03, 0x07, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00 };
}
