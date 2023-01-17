/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialCommand;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponseListener;
import org.openhab.core.thing.ChannelUID;

/**
 * This class is used to convert a channel id to it's corresponding class command.
 *
 * @author Richard Lavoie - Initial contribution
 *
 */
public class CommandFactory {

    public static LGSerialCommand createCommandFor(ChannelUID channel, LGSerialResponseListener listener) {
        int setId = listener.getSetID();
        String name = channel.getId();
        switch (name) {
            case "3d":
                return new LG3DCommand(setId);
            case "3d-extended":
                return new LG3DExtendedCommand(setId);
            case "aspect-ratio":
                return new AspectRatioCommand(setId);
            case "auto-sleep":
                return new AutoSleepCommand(setId);
            case "auto-volume":
                return new AutoVolumeCommand(setId);
            case "backlight":
                return new BacklightCommand(setId);
            case "balance":
                return new BalanceCommand(setId);
            case "bass":
                return new BassCommand(setId);
            case "brightness":
                return new BrightnessCommand(setId);
            case "color":
                return new ColorCommand(setId);
            case "color-temperature":
                return new ColorTemperatureCommand(setId);
            case "color-temperature2":
                return new ColorTemperature2Command(setId);
            case "contrast":
                return new ContrastCommand(setId);
            case "dpm":
                return new DPMCommand(setId);
            case "elapsed-time":
                return new ElapsedTimeCommand(setId);
            case "energy-saving":
                return new EnergySavingCommand(setId);
            case "fan-fault-check":
                return new FanFaultCheckCommand(setId);
            case "h-position":
                return new HPositionCommand(setId);
            case "h-size":
                return new HSizeCommand(setId);
            case "input":
                return new InputSelectCommand(setId);
            case "input2":
                return new InputSelect2Command(setId);
            case "ir-key-code":
                return new IRKeyCodeCommand(setId);
            case "ism-method":
                return new ISMMethodCommand(setId);
            case "lamp-fault-check":
                return new LampFaultCheckCommand(setId);
            case "natural-mode":
                return new NaturalModeCommand(setId);
            case "osd-language":
                return new OSDLanguageCommand(setId);
            case "osd-select":
                return new OSDSelectCommand(setId);
            case "picture-mode":
                return new PictureModeCommand(setId);
            case "power":
                return new PowerCommand(setId);
            case "power-indicator":
                return new PowerIndicatorCommand(setId);
            case "power-saving":
                return new PowerSavingCommand(setId);
            case "raw":
                return new RawCommand();
            case "screen-mute":
                return new ScreenMuteCommand(setId);
            case "serial-number":
                return new SerialNoCommand(setId);
            case "sharpness":
                return new SharpnessCommand(setId);
            case "sleep-time":
                return new SleepTimeCommand(setId);
            case "software-version":
                return new SoftwareVersionCommand(setId);
            case "sound-mode":
                return new SoundModeCommand(setId);
            case "speaker":
                return new SpeakerCommand(setId);
            case "temperature-value":
                return new TemperatureValueCommand(setId);
            case "tile":
                return new TileCommand(setId);
            case "tile-h-position":
                return new TileHPositionCommand(setId);
            case "tile-h-size":
                return new TileHSizeCommand(setId);
            case "tile-id-set":
                return new TileIdSetCommand(setId);
            case "tile-v-position":
                return new TileVPositionCommand(setId);
            case "tile-v-size":
                return new TileVSizeCommand(setId);
            case "time":
                return new TimeCommand(setId);
            case "tint":
                return new TintCommand(setId);
            case "treble":
                return new TrebleCommand(setId);
            case "volume":
                return new VolumeCommand(setId);
            case "volume-mute":
                return new VolumeMuteCommand(setId);
            case "v-position":
                return new VPositionCommand(setId);
            case "v-size":
                return new VSizeCommand(setId);
        }
        return null;
    }
}
