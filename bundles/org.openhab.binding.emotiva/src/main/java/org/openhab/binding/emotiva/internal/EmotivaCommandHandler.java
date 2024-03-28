/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.emotiva.internal;

import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.DEFAULT_VOLUME_MAX_DECIBEL;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.DEFAULT_VOLUME_MIN_DECIBEL;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlRequest;
import org.openhab.binding.emotiva.internal.protocol.EmotivaProtocolVersion;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags;
import org.openhab.binding.emotiva.internal.protocol.OHChannelToEmotivaCommand;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.Command;

/**
 * A command handler translates an openHAB command into an Emotiva commands.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public class EmotivaCommandHandler {

    private final EmotivaConfiguration config;

    public EmotivaCommandHandler(EmotivaConfiguration config) {
        this.config = config;
    }

    public int emotivaVolumeValue(Command command) throws UnsupportedCommandTypeException {
        return emotivaVolumeValue(command, DEFAULT_VOLUME_MIN_DECIBEL, DEFAULT_VOLUME_MAX_DECIBEL);
    }

    public int emotivaVolumeValue(Command command, int minValue, int maxValue) throws UnsupportedCommandTypeException {

        if (command == IncreaseDecreaseType.INCREASE) {
            return +1;
        } else if (command == IncreaseDecreaseType.DECREASE) {
            return -1;
        } else if (command instanceof PercentType percentCommand) {
            return (percentCommand.intValue() * (config.getMainVolumeMax().intValue() - minValue) / 100) + minValue;
        } else if (command instanceof DecimalType decimalCommand) {
            if (decimalCommand.intValue() > maxValue) {
                return maxValue;
            } else if (decimalCommand.intValue() < minValue) {
                return minValue;
            }
            return decimalCommand.intValue();
        } else if (command instanceof QuantityType quantityType) {
            return quantityType.intValue();
        } else {
            throw new UnsupportedCommandTypeException();
        }
    }

    public static PercentType volumeDecibelToPercentage(String volumeInDecibel) {
        String volumeString = volumeInDecibel.replace("dB", "").trim();
        int clampedValue = clamp(volumeString, DEFAULT_VOLUME_MIN_DECIBEL, DEFAULT_VOLUME_MAX_DECIBEL);
        return new PercentType(Math.round((100 - ((float) Math.abs(clampedValue - DEFAULT_VOLUME_MAX_DECIBEL)
                / Math.abs(DEFAULT_VOLUME_MIN_DECIBEL - DEFAULT_VOLUME_MAX_DECIBEL)) * 100)));
    }

    public static int volumePercentageToDecibel(int volumeInPercentage) {
        int clampedValue = clamp(volumeInPercentage, 0, 100);
        return (clampedValue * (DEFAULT_VOLUME_MAX_DECIBEL - DEFAULT_VOLUME_MIN_DECIBEL) / 100)
                + DEFAULT_VOLUME_MIN_DECIBEL;
    }

    public static int volumePercentageToDecibel(String volumeInPercentage) {
        String volumeInPercentage1 = volumeInPercentage.replace("%", "").trim();
        int clampedValue = clamp(volumeInPercentage1, 0, 100);
        return (clampedValue * (DEFAULT_VOLUME_MAX_DECIBEL - DEFAULT_VOLUME_MIN_DECIBEL) / 100)
                + DEFAULT_VOLUME_MIN_DECIBEL;
    }

    public static double clamp(Number value, double min, double max) {
        return Math.min(Math.max(value.intValue(), min), max);
    }

    private static int clamp(String volumeInPercentage, int min, int max) {
        return Math.min(Math.max(Double.valueOf(volumeInPercentage.trim()).intValue(), min), max);
    }

    private static int clamp(int volumeInPercentage, int min, int max) {
        return Math.min(Math.max(Double.valueOf(volumeInPercentage).intValue(), min), max);
    }

    public static EmotivaControlRequest channelToControlRequest(String id,
            Map<String, Map<EmotivaControlCommands, String>> commandMaps, EmotivaProtocolVersion protocolVersion) {
        EmotivaSubscriptionTags channelSubscription = EmotivaSubscriptionTags.fromChannelUID(id);
        EmotivaControlCommands channelFromCommand = OHChannelToEmotivaCommand.fromChannelUID(id);
        return new EmotivaControlRequest(id, channelSubscription, channelFromCommand, commandMaps, protocolVersion);
    }
}
