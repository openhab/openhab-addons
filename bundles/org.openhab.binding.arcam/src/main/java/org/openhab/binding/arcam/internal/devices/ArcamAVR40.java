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
package org.openhab.binding.arcam.internal.devices;

import static org.openhab.binding.arcam.internal.connection.ArcamCommandCode.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.arcam.internal.ArcamNowPlaying;
import org.openhab.binding.arcam.internal.ArcamZone;
import org.openhab.binding.arcam.internal.connection.ArcamCommand;
import org.openhab.binding.arcam.internal.connection.ArcamCommandCode;
import org.openhab.binding.arcam.internal.connection.ArcamCommandData;
import org.openhab.binding.arcam.internal.connection.ArcamCommandDataFinder;
import org.openhab.binding.arcam.internal.connection.ArcamCommandFinder;
import org.openhab.binding.arcam.internal.exceptions.NotSupportedException;

/**
 * This class contains the device specific implementation for the AVR40
 *
 * @author Joep Admiraal - Initial contribution
 */
@NonNullByDefault
public class ArcamAVR40 implements ArcamDevice {

    public static final List<ArcamCommandData> INPUT_COMMANDS = new ArrayList<>(List.of( //
            new ArcamCommandData("FZONE1", "Follow Zone 1", (byte) 0x00), //
            new ArcamCommandData("CD", (byte) 0x01), //
            new ArcamCommandData("BD", (byte) 0x02), //
            new ArcamCommandData("AV", (byte) 0x03), //
            new ArcamCommandData("SAT", (byte) 0x04), //
            new ArcamCommandData("PVR", (byte) 0x05), //
            new ArcamCommandData("UHD", (byte) 0x06), //
            new ArcamCommandData("AUX", (byte) 0x08), //
            new ArcamCommandData("DISPLAY", (byte) 0x09), //
            new ArcamCommandData("TUNERFM", "TUNER (FM)", (byte) 0x0B), //
            new ArcamCommandData("TUNERDAB", "TUNER (DAB)", (byte) 0x0C), //
            new ArcamCommandData("NET", (byte) 0x0E), //
            new ArcamCommandData("STB", (byte) 0x10), //
            new ArcamCommandData("GAME", (byte) 0x11), //
            new ArcamCommandData("BT", (byte) 0x12) //
    ));

    public static final List<ArcamCommandData> DISPLAY_BRIGHTNESS_COMMANDS = new ArrayList<>(List.of( //
            new ArcamCommandData("OFF", "Front panel is off", (byte) 0x00), //
            new ArcamCommandData("L1", "Front panel L1", (byte) 0x01), //
            new ArcamCommandData("L2", "Front panel L2", (byte) 0x02) //
    ));

    /**
     * List of commands, with the databytes set to the request state bytes
     * Used to request one of the states of an Arcam device.
     * Used to generate the commands to send to the Arcam device.
     */
    public static final List<ArcamCommand> COMMANDS = new ArrayList<>(List.of( //
            // Non channel related
            new ArcamCommand(HEARTBEAT, new byte[] { 0x21, 0x01, 0x25, 0x01, (byte) 0xF0, 0x0D }), //
            // Generic
            new ArcamCommand(DISPLAY_BRIGHTNESS, new byte[] { 0x21, 0x01, 0x01, 0x01, (byte) 0xF0, 0x0D }), //
            new ArcamCommand(HEADPHONES, new byte[] { 0x21, 0x01, 0x02, 0x01, (byte) 0xF0, 0x0D }), //
            new ArcamCommand(INCOMING_SAMPLE_RATE, new byte[] { 0x21, 0x01, 0x44, 0x01, (byte) 0xF0, 0x0D }), //
            new ArcamCommand(SOFTWARE_VERSION, new byte[] { 0x21, 0x01, 0x04, 0x01, (byte) 0xF0, 0x0D }), //
            // Master zone
            new ArcamCommand(MASTER_BALANCE, new byte[] { 0x21, 0x01, 0x3B, 0x01, (byte) 0xF0, 0x0D }), //
            new ArcamCommand(MASTER_MUTE, new byte[] { 0x21, 0x01, 0x0E, 0x01, (byte) 0xF0, 0x0D }), //
            new ArcamCommand(MASTER_INPUT, new byte[] { 0x21, 0x01, 0x1D, 0x01, (byte) 0xF0, 0x0D }), //
            new ArcamCommand(MASTER_DIRECT_MODE, new byte[] { 0x21, 0x01, 0x0F, 0x01, (byte) 0xF0, 0x0D }), //
            new ArcamCommand(MASTER_POWER, new byte[] { 0x21, 0x01, 0x00, 0x01, (byte) 0xF0, 0x0D }), //
            new ArcamCommand(MASTER_ROOM_EQUALISATION, new byte[] { 0x21, 0x01, 0x37, 0x01, (byte) 0xF0, 0x0D }), //
            new ArcamCommand(MASTER_VOLUME, new byte[] { 0x21, 0x01, 0x0D, 0x01, (byte) 0xF0, 0x0D }), //
            // Zone 2
            new ArcamCommand(ZONE2_BALANCE, new byte[] { 0x21, 0x02, 0x3B, 0x01, (byte) 0xF0, 0x0D }), //
            new ArcamCommand(ZONE2_DIRECT_MODE, new byte[] { 0x21, 0x02, 0x0F, 0x01, (byte) 0xF0, 0x0D }), //
            new ArcamCommand(ZONE2_INPUT, new byte[] { 0x21, 0x02, 0x1D, 0x01, (byte) 0xF0, 0x0D }), //
            new ArcamCommand(ZONE2_MUTE, new byte[] { 0x21, 0x02, 0x0E, 0x01, (byte) 0xF0, 0x0D }), //
            new ArcamCommand(ZONE2_POWER, new byte[] { 0x21, 0x02, 0x00, 0x01, (byte) 0xF0, 0x0D }), //
            new ArcamCommand(ZONE2_ROOM_EQUALISATION, new byte[] { 0x21, 0x02, 0x37, 0x01, (byte) 0xF0, 0x0D }), //
            new ArcamCommand(ZONE2_VOLUME, new byte[] { 0x21, 0x02, 0x0D, 0x01, (byte) 0xF0, 0x0D }) //
    ));

    public static final Map<Byte, String> SAMPLE_RATES = Map.ofEntries( //
            Map.entry((byte) 0x00, "32 kHz"), //
            Map.entry((byte) 0x01, "44.1 kHz"), //
            Map.entry((byte) 0x02, "48 kHz"), //
            Map.entry((byte) 0x03, "88.2 kHz"), //
            Map.entry((byte) 0x04, "96 kHz"), //
            Map.entry((byte) 0x05, "176.4 kHz"), //
            Map.entry((byte) 0x06, "192 kHz"), //
            Map.entry((byte) 0x07, "Unknown"), //
            Map.entry((byte) 0x08, "Undetected") //
    );//

    public static String AVR40 = "AVR40";

    private ArcamCommandDataFinder commandDataFinder;
    private ArcamCommandFinder commandFinder;

    public ArcamAVR40() {
        this.commandDataFinder = new ArcamCommandDataFinder();
        this.commandFinder = new ArcamCommandFinder();
    }
    // Generate command bytes to send

    @Override
    public byte[] getBalanceCommand(int balance, ArcamZone zone) {
        // 0x00 – Set the balance to the centre
        // 0x01 – 0x06 – Set the balance to the right 1, 2, ..., 5, 6
        // 0x81 – 0x86 – Set the balance to the left 1, 2,..., 5, 6
        byte[] data;
        if (zone == ArcamZone.MASTER) {
            data = commandFinder.getCommandFromCode(MASTER_BALANCE, COMMANDS);
        } else {
            data = commandFinder.getCommandFromCode(ZONE2_BALANCE, COMMANDS);
        }
        byte balanceByte = (byte) balance;

        if (balance < 0) {
            balanceByte = (byte) ((balance * -1) + 0x80);
        }
        data[4] = balanceByte;

        return data;
    }

    @Override
    public byte[] getDacFilterCommand(String dacFilter) {
        throw new NotSupportedException();
    }

    @Override
    public byte[] getDisplayBrightnessCommand(String displayBrightness) {
        byte[] data = commandFinder.getCommandFromCode(DISPLAY_BRIGHTNESS, COMMANDS);
        data[4] = commandDataFinder.getByteFromCommandDataCode(displayBrightness, DISPLAY_BRIGHTNESS_COMMANDS);

        return data;
    }

    @Override
    public byte[] getHeartbeatCommand() {
        return commandFinder.getCommandFromCode(HEARTBEAT, COMMANDS);
    }

    @Override
    public byte[] getInputCommand(String inputName, ArcamZone zone) {
        byte[] data = commandFinder.getCommandFromCode(MASTER_INPUT, COMMANDS);
        data[4] = commandDataFinder.getByteFromCommandDataCode(inputName, INPUT_COMMANDS);

        return data;
    }

    @Override
    public byte[] getMuteCommand(boolean mute, ArcamZone zone) {
        // Using RC5 simulation
        return new byte[] { 0x21, ArcamDeviceUtil.zoneToByte(zone), 0x08, 0x02, 0x10, 0x0D, 0x0D };
    }

    @Override
    public byte[] getPowerCommand(boolean on, ArcamZone zone) {
        // Using RC5 simulation
        if (on) {
            return new byte[] { 0x21, ArcamDeviceUtil.zoneToByte(zone), 0x08, 0x02, 0x10, 0x7B, 0x0D };
        }

        return new byte[] { 0x21, ArcamDeviceUtil.zoneToByte(zone), 0x08, 0x02, 0x10, 0x7C, 0x0D };
    }

    @Override
    public byte[] getRebootCommand() {
        // Hard coded here instead of in the commands list as there is no way to retrieve the status reboot
        return new byte[] { 0x21, 0x01, 0x26, 0x06, 0x52, 0x45, 0x42, 0x4F, 0x4F, 0x54, 0x0D };
    }

    @Override
    public byte[] getRoomEqualisationCommand(String eq, ArcamZone zone) {
        byte[] data;
        if (zone == ArcamZone.MASTER) {
            data = commandFinder.getCommandFromCode(MASTER_ROOM_EQUALISATION, COMMANDS);
        } else {
            data = commandFinder.getCommandFromCode(ZONE2_ROOM_EQUALISATION, COMMANDS);
        }
        data[4] = commandDataFinder.getByteFromCommandDataCode(eq, ArcamDeviceConstants.ROOM_EQ);
        return data;
    }

    @Override
    public byte[] getVolumeCommand(int volume, ArcamZone zone) {
        byte[] data;
        if (zone == ArcamZone.MASTER) {
            data = commandFinder.getCommandFromCode(MASTER_VOLUME, COMMANDS);
        } else {
            data = commandFinder.getCommandFromCode(ZONE2_VOLUME, COMMANDS);
        }
        data[4] = (byte) volume;

        return data;
    }

    @Override
    public byte[] getStateCommandByte(ArcamCommandCode commandCode) {
        return commandFinder.getCommandFromCode(commandCode, COMMANDS);
    }

    // Interpret incoming bytes

    @Override
    public int getBalance(byte dataByte) {
        // 0x00 – Balance is Centered
        // 0x00 – 0x0C – Balance is Right 1, 2,...,11, 12
        // 0x81 – 0x8C – Balance is Left 1, 2,...,11, 12
        if (dataByte < 0) {
            int balance = (dataByte + 0x80) * -1;
            return balance;
        }

        return dataByte;
    }

    @Override
    public boolean getBoolean(byte dataByte) {
        return dataByte == 0x01;
    }

    @Override
    @Nullable
    public String getDacFilter(Byte dataByte) {
        throw new NotSupportedException();
    }

    @Override
    @Nullable
    public String getDisplayBrightness(byte dataByte) {
        return commandDataFinder.getCommandCodeFromByte(dataByte, DISPLAY_BRIGHTNESS_COMMANDS);
    }

    @Override
    public String getIncomingSampleRate(byte dataByte) {
        String sampleRate = SAMPLE_RATES.get(dataByte);
        if (sampleRate == null) {
            return "Unknown";
        }

        return sampleRate;
    }

    @Override
    @Nullable
    public String getInputName(byte dataByte) {
        return commandDataFinder.getCommandCodeFromByte(dataByte, INPUT_COMMANDS);
    }

    @Override
    public boolean getMute(byte dataByte) {
        return dataByte == 0x01;
    }

    @Override
    @Nullable
    public ArcamNowPlaying setNowPlaying(List<Byte> dataBytes) {
        throw new NotSupportedException();
    }

    @Override
    public String getNowPlayingSampleRate(byte dataByte) {
        throw new NotSupportedException();
    }

    @Override
    public String getNowPlayingEncoder(byte dataByte) {
        throw new NotSupportedException();
    }

    @Override
    @Nullable
    public String getRoomEqualisation(byte dataByte) {
        return commandDataFinder.getCommandCodeFromByte(dataByte, ArcamDeviceConstants.ROOM_EQ);
    }

    @Override
    public String getSoftwareVersion(List<Byte> dataBytes) {
        int major = dataBytes.get(0);
        int minor = dataBytes.get(1);
        return major + "." + minor;
    }

    @Override
    public int getTemperature(List<Byte> dataBytes, int tempNr) {
        return dataBytes.get(tempNr);
    }

    @Override
    public int getTimeoutCounter(List<Byte> dataBytes) {
        throw new NotSupportedException();
    }
}
