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
package org.openhab.binding.feican.internal;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;

/**
 * Creates commands to send to Feican devices.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class Commands {
    private static final byte[] DISCOVER_COMMAND = { 126, 7, 9, -128, -128, -128, -128, -128, -17 };
    private static final byte[] ON_COMMAND = { 126, 4, 4, 1, 0, -128, -128, 0, -17 };
    private static final byte[] OFF_COMMAND = { 126, 4, 4, 0, 0, -128, -128, 0, -17 };
    private static final byte[] RGB_COMMAND = { 126, 7, 5, 3, 0, 0, 0, 0, -17 };
    private static final byte[] COLOR_TEMPERATURE_COMMAND = { 126, 6, 5, 2, 0, 0, -128, 8, -17 };
    private static final byte[] BRIGHTNESS_COMMAND = { 126, 4, 1, 0, -128, -128, -128, 0, -17 };
    private static final byte[] PROGRAM_COMMAND = { 126, 5, 3, 0, 3, -128, -128, 0, -17 };
    private static final byte[] PROGRAM_SPEED_COMMAND = { 126, 4, 2, 0, -128, -128, -128, 0, -17 };

    /**
     * Returns the command to discover devices.
     *
     * @return discover command
     */
    public static byte[] discover() {
        return DISCOVER_COMMAND;
    }

    /**
     * Returns the command to switch a device on or off depending on the given parameter.
     *
     * @param onOff command to be on or off command
     * @return the on/off command
     */
    public byte[] switchOnOff(OnOffType onOff) {
        if (onOff == OnOffType.ON) {
            return ON_COMMAND;
        } else {
            return OFF_COMMAND;
        }
    }

    /**
     * Returns the command to set the color.
     *
     * @param color the color to set
     * @return the color command
     */
    public byte[] color(HSBType color) {
        byte[] command = RGB_COMMAND.clone();
        PercentType[] rgb = color.toRGB();
        command[4] = convertColorPercentToByte(rgb[0]);
        command[5] = convertColorPercentToByte(rgb[1]);
        command[6] = convertColorPercentToByte(rgb[2]);
        return command;
    }

    /**
     * Converts a percentage (0-100) to a color range value (0-255). This is needed because {@link HSBType} returns
     * color values in the 100-range while a 255 range is needed.
     *
     * @param percent value to be converted.
     * @return converted value as a byte value
     */
    private byte convertColorPercentToByte(PercentType percent) {
        return percent.toBigDecimal().multiply(BigDecimal.valueOf(255))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).byteValue();
    }

    /**
     * Returns the command to set the color temperature to a value between 0 and 100.
     *
     * @param percentage the color temperature to set
     * @return the color temperature command
     */
    public byte[] colorTemperature(PercentType percentage) {
        byte[] command = COLOR_TEMPERATURE_COMMAND.clone();
        command[4] = percentage.byteValue();
        command[5] = (byte) (100 - percentage.intValue());
        return command;
    }

    /**
     * Returns the command to set the brightness on a bulb running in color, color temperature or program.
     *
     * @param percentage the brightness to set
     * @return the brightness command
     */
    public byte[] brightness(PercentType percentage) {
        byte[] command = BRIGHTNESS_COMMAND.clone();
        command[3] = percentage.byteValue();
        return command;
    }

    /**
     * Returns the command to set a preset program. Program codes on the device start with 0x80 and up. Binding maps
     * this with values starting with 1.
     *
     * @param program this binding value representing the preset program
     * @return the command to set the program
     */
    public byte[] program(int program) {
        byte[] command = PROGRAM_COMMAND.clone();
        command[3] = (byte) (-129 + program);
        return command;
    }

    /**
     * Returns the command to set program speed to a value between 0 and 100.
     *
     * @param percentage the program speed to set
     * @return the program speed command
     */
    public byte[] programSpeed(PercentType percentage) {
        byte[] command = PROGRAM_SPEED_COMMAND.clone();
        command[3] = percentage.byteValue();
        return command;
    }
}
