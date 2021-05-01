/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.hdmicec.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Mapping of CEC button names to their opcodes
 *
 * @author Sam Spencer - Initial contribution
 */

@NonNullByDefault
public enum RemoteButtonCode {
    Select(0x00),
    Up(0x01),
    Down(0x02),
    Left(0x03),
    Right(0x04),
    RightUp(0x05),
    RightDown(0x06),
    LeftUp(0x07),
    LeftDown(0x08),
    RootMenu(0x09),
    SetupMenu(0x0A),
    ContentsMenu(0x0B),
    FavoriteMenu(0x0C),
    Exit(0x0D),
    Zero(0x20),
    One(0x21),
    Two(0x22),
    Three(0x23),
    Four(0x24),
    Five(0x25),
    Six(0x26),
    Seven(0x27),
    Eight(0x28),
    Nine(0x29),
    Dot(0x2A),
    Enter(0x2B),
    Clear(0x2C),
    NextFavorite(0x2F),
    ChannelUp(0x30),
    ChannelDown(0x31),
    PreviousChannel(0x32),
    SoundSelect(0x33),
    InputSelect(0x34),
    DisplayInformation(0x35),
    Help(0x36),
    PageUp(0x37),
    PageDown(0x38),
    Power(0x40),
    VolumeUp(0x41),
    VolumeDown(0x42),
    Mute(0x43),
    Play(0x44),
    Stop(0x45),
    Pause(0x46),
    Record(0x47),
    Rewind(0x48),
    Fastforward(0x49),
    Eject(0x4A),
    Forward(0x4B),
    Backward(0x4C),
    StopRecord(0x4D),
    PauseRecord(0x4E),
    Reserved(0x4F),
    Angle(0x50),
    Subpicture(0x51),
    VOD(0x52),
    Guide(0x53),
    Timer(0x54),
    InitialConfiguration(0x55),
    PlayFunction(0x60),
    PausePlayFunction(0x61),
    RecordFunction(0x62),
    PauseRecordFunction(0x63),
    StopFunction(0x64),
    MuteFunction(0x65),
    RestoreVolumeFunction(0x66),
    TuneFunction(0x67),
    SelectMediaFunction(0x68),
    SelectAVInputFunction(0x69),
    SelectAudioInputFunction(0x6A),
    PowerToggleFunction(0x6B),
    PowerOffFunction(0x6C),
    PowerOnFunction(0x6D),
    Blue(0x71),
    Red(0x72),
    Green(0x73),
    Yellow(0x74),
    Data(0x76),
    F1(0x71),
    F2(0x72),
    F3(0x73),
    F4(0x74),
    F5(0x75),
    INVALID(0xFF);

    private final byte shortCode;

    RemoteButtonCode(int code) {
        this.shortCode = (byte) code;
    }

    public byte value() {
        return this.shortCode;
    }

    public String opcode() {
        return String.format("%02x", shortCode);
    }

    private static RemoteButtonCode numbers[] = { RemoteButtonCode.Zero, RemoteButtonCode.One, RemoteButtonCode.Two,
            RemoteButtonCode.Three, RemoteButtonCode.Four, RemoteButtonCode.Five, RemoteButtonCode.Six,
            RemoteButtonCode.Seven, RemoteButtonCode.Eight, RemoteButtonCode.Nine };

    public static RemoteButtonCode fromString(String s) {
        for (RemoteButtonCode c : RemoteButtonCode.values()) {
            if (s.equalsIgnoreCase(c.name())) {
                return c;
            }
        }
        try {
            int i = Integer.parseInt(s);
            if (i >= 0 && i <= 9) {
                return numbers[i];
            }
        } catch (NumberFormatException e) {
        }
        return RemoteButtonCode.INVALID;
    }

    public static String opcodeFromString(String s) {
        return RemoteButtonCode.fromString(s).opcode();
    }
}
