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

import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlRequest;
import org.openhab.binding.emotiva.internal.protocol.EmotivaProtocolVersion;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags;
import org.openhab.binding.emotiva.internal.protocol.OHChannelToEmotivaCommand;
import org.openhab.core.library.types.PercentType;

/**
 * Helper class for Emotiva commands.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public class EmotivaCommandHelper {

    public static PercentType volumeDecibelToPercentage(String volumeInDecibel) {
        String volumeTrimmed = volumeInDecibel.replace("dB", "").trim();
        int clampedValue = clamp(volumeTrimmed, DEFAULT_VOLUME_MIN_DECIBEL, DEFAULT_VOLUME_MAX_DECIBEL);
        return new PercentType(Math.round((100 - ((float) Math.abs(clampedValue - DEFAULT_VOLUME_MAX_DECIBEL)
                / Math.abs(DEFAULT_VOLUME_MIN_DECIBEL - DEFAULT_VOLUME_MAX_DECIBEL)) * 100)));
    }

    public static double integerToPercentage(int integer) {
        int clampedValue = clamp(integer, 0, 100);
        return Math.round((100 - ((float) Math.abs(clampedValue - 100) / Math.abs(-100)) * 100));
    }

    public static int volumePercentageToDecibel(int volumeInPercentage) {
        int clampedValue = clamp(volumeInPercentage, 0, 100);
        return (clampedValue * (DEFAULT_VOLUME_MAX_DECIBEL - DEFAULT_VOLUME_MIN_DECIBEL) / 100)
                + DEFAULT_VOLUME_MIN_DECIBEL;
    }

    public static int volumePercentageToDecibel(String volumeInPercentage) {
        String volumeInPercentageTrimmed = volumeInPercentage.replace("%", "").trim();
        int clampedValue = clamp(volumeInPercentageTrimmed, 0, 100);
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

    public static EmotivaControlRequest channelToControlRequest(String id, EmotivaProcessorState state,
            EmotivaProtocolVersion protocolVersion) {
        EmotivaSubscriptionTags channelSubscription = EmotivaSubscriptionTags.fromChannelUID(id);
        EmotivaControlCommands channelFromCommand = OHChannelToEmotivaCommand.fromChannelUID(id);
        return new EmotivaControlRequest(id, channelSubscription, channelFromCommand, state, protocolVersion);
    }

    public static String getMenuPanelRowLabel(int row) {
        return switch (row) {
            case 4 -> "top";
            case 5 -> "middle";
            case 6 -> "bottom";
            default -> "";
        };
    }

    public static String getMenuPanelColumnLabel(int column) {
        return switch (column) {
            case 0 -> "start";
            case 1 -> "center";
            case 2 -> "end";
            default -> "";
        };
    }

    public static String updateProgress(double progressPercentage) {
        final int width = 30;
        var sb = new StringBuilder();

        sb.append("[");
        int i = 0;
        for (; i <= (int) (progressPercentage * width); i++) {
            sb.append(".");
        }
        for (; i < width; i++) {
            sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }
}
