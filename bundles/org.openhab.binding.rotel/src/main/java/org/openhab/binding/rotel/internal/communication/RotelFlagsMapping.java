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
package org.openhab.binding.rotel.internal.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rotel.internal.RotelException;

/**
 * Class managing the mapping of message flags with indicators
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RotelFlagsMapping {

    public static final RotelFlagsMapping MAPPING1 = new RotelFlagsMapping(3, 1, 5, 0, -1, -1, -1, -1, 8, 6, 8, 4, 8,
            3);
    public static final RotelFlagsMapping MAPPING2 = new RotelFlagsMapping(-1, -1, 4, 7, -1, -1, -1, -1, 5, 6, 5, 4, 5,
            3);
    public static final RotelFlagsMapping MAPPING3 = new RotelFlagsMapping(4, 7, 1, 7, 2, 7, 6, 2, 5, 6, 5, 4, 5, 3);
    public static final RotelFlagsMapping MAPPING4 = new RotelFlagsMapping(3, 1, 1, 7, 2, 7, 6, 2, 8, 6, 8, 4, 8, 3);
    public static final RotelFlagsMapping MAPPING5 = new RotelFlagsMapping(-1, -1, 3, 2, 4, 2, 4, 1, 5, 6, 5, 4, 5, 3);
    public static final RotelFlagsMapping NO_MAPPING = new RotelFlagsMapping(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1);

    private int multiInputFlagNumber;
    private int multiInputBitNumber;
    private int zone2FlagNumber;
    private int zone2BitNumber;
    private int zone3FlagNumber;
    private int zone3BitNumber;
    private int zone4FlagNumber;
    private int zone4BitNumber;
    private int centerFlagNumber;
    private int centerBitNumber;
    private int surroundLeftFlagNumber;
    private int surroundLeftBitNumber;
    private int surroundRightFlagNumber;
    private int surroundRightBitNumber;

    /**
     * Constructor
     *
     * For each flag number, value 1 means the first flag; a negative value is used for an undefined indicator.
     * For each bit number, value is from 0 to 7; a negative value is used for an undefined indicator.
     *
     * @param multiInputFlagNumber the flag number in the standard feedback message in which to find the multi input
     *            indicator
     * @param multiInputBitNumber the bit number in the flag in which to find the multi input indicator
     * @param zone2FlagNumber the flag number in the standard feedback message in which to find the zone 2 indicator
     * @param zone2BitNumber the bit number in the flag in which to find the zone 2 indicator
     * @param zone3FlagNumber the flag number in the standard feedback message in which to find the zone 3 indicator
     * @param zone3BitNumber the bit number in the flag in which to find the zone 3 indicator
     * @param zone4FlagNumber the flag number in the standard feedback message in which to find the zone 4 indicator
     * @param zone4BitNumber the bit number in the flag in which to find the zone 4 indicator
     * @param centerFlagNumber the flag number in the standard feedback message in which to find the center channel
     *            indicator
     * @param centerBitNumber the bit number in the flag in which to find the center channel indicator
     * @param surroundLeftFlagNumber the flag number in the standard feedback message in which to find the surround left
     *            channel indicator
     * @param surroundLeftBitNumber the bit number in the flag in which to find the surround left channel indicator
     * @param surroundRightFlagNumber the flag number in the standard feedback message in which to find the surround
     *            right channel indicator
     * @param surroundRightBitNumber the bit number in the flag in which to find the surround right channel indicator
     */
    private RotelFlagsMapping(int multiInputFlagNumber, int multiInputBitNumber, int zone2FlagNumber,
            int zone2BitNumber, int zone3FlagNumber, int zone3BitNumber, int zone4FlagNumber, int zone4BitNumber,
            int centerFlagNumber, int centerBitNumber, int surroundLeftFlagNumber, int surroundLeftBitNumber,
            int surroundRightFlagNumber, int surroundRightBitNumber) {
        this.multiInputFlagNumber = multiInputFlagNumber;
        this.multiInputBitNumber = multiInputBitNumber;
        this.zone2FlagNumber = zone2FlagNumber;
        this.zone2BitNumber = zone2BitNumber;
        this.zone3FlagNumber = zone3FlagNumber;
        this.zone3BitNumber = zone3BitNumber;
        this.zone4FlagNumber = zone4FlagNumber;
        this.zone4BitNumber = zone4BitNumber;
        this.centerFlagNumber = centerFlagNumber;
        this.centerBitNumber = centerBitNumber;
        this.surroundLeftFlagNumber = surroundLeftFlagNumber;
        this.surroundLeftBitNumber = surroundLeftBitNumber;
        this.surroundRightFlagNumber = surroundRightFlagNumber;
        this.surroundRightBitNumber = surroundRightBitNumber;
    }

    /**
     * Get the multi input indicator
     *
     * @param flags the table of flags
     *
     * @return true if the indicator is ON in the flags or false if OFF
     *
     * @throws RotelException in case the multi input indicator is undefined
     */
    public boolean isMultiInputOn(byte[] flags) throws RotelException {
        return RotelFlagsMapping.isBitFlagOn(flags, multiInputFlagNumber, multiInputBitNumber);
    }

    /**
     * Set the multi input indicator
     *
     * @param flags the table of flags
     * @param on true to set the indicator to ON or false to set it to OFF
     *
     * @throws RotelException in case the multi input indicator is undefined
     */
    public void setMultiInput(byte[] flags, boolean on) throws RotelException {
        RotelFlagsMapping.setBitFlag(flags, multiInputFlagNumber, multiInputBitNumber, on);
    }

    /**
     * Get the zone 2 indicator
     *
     * @param flags the table of flags
     *
     * @return true if the indicator is ON in the flags or false if OFF
     *
     * @throws RotelException in case the zone 2 indicator is undefined
     */
    public boolean isZone2On(byte[] flags) throws RotelException {
        return RotelFlagsMapping.isBitFlagOn(flags, zone2FlagNumber, zone2BitNumber);
    }

    /**
     * Set the zone 2 indicator
     *
     * @param flags the table of flags
     * @param on true to set the indicator to ON or false to set it to OFF
     *
     * @throws RotelException in case the zone 2 indicator is undefined
     */
    public void setZone2(byte[] flags, boolean on) throws RotelException {
        RotelFlagsMapping.setBitFlag(flags, zone2FlagNumber, zone2BitNumber, on);
    }

    /**
     * Get the zone 3 indicator
     *
     * @param flags the table of flags
     *
     * @return true if the indicator is ON in the flags or false if OFF
     *
     * @throws RotelException in case the zone 3 indicator is undefined
     */
    public boolean isZone3On(byte[] flags) throws RotelException {
        return RotelFlagsMapping.isBitFlagOn(flags, zone3FlagNumber, zone3BitNumber);
    }

    /**
     * Set the zone 3 indicator
     *
     * @param flags the table of flags
     * @param on true to set the indicator to ON or false to set it to OFF
     *
     * @throws RotelException in case the zone 3 indicator is undefined
     */
    public void setZone3(byte[] flags, boolean on) throws RotelException {
        RotelFlagsMapping.setBitFlag(flags, zone3FlagNumber, zone3BitNumber, on);
    }

    /**
     * Get the zone 4 indicator
     *
     * @param flags the table of flags
     *
     * @return true if the indicator is ON in the flags or false if OFF
     *
     * @throws RotelException in case the zone 4 indicator is undefined
     */
    public boolean isZone4On(byte[] flags) throws RotelException {
        return RotelFlagsMapping.isBitFlagOn(flags, zone4FlagNumber, zone4BitNumber);
    }

    /**
     * Set the zone 4 indicator
     *
     * @param flags the table of flags
     * @param on true to set the indicator to ON or false to set it to OFF
     *
     * @throws RotelException in case the zone 4 indicator is undefined
     */
    public void setZone4(byte[] flags, boolean on) throws RotelException {
        RotelFlagsMapping.setBitFlag(flags, zone4FlagNumber, zone4BitNumber, on);
    }

    /**
     * Check whether more than front channels are ON
     *
     * @param flags the table of flags
     *
     * @return true if the indicators show that center or surround channels are ON in the flags
     *
     * @throws RotelException in case the center or surround channel indicators are undefined
     */
    public boolean isMoreThan2Channels(byte[] flags) throws RotelException {
        return (centerFlagNumber >= 1 && centerFlagNumber <= flags.length
                && RotelFlagsMapping.isBitFlagOn(flags, centerFlagNumber, centerBitNumber))
                || (surroundLeftFlagNumber >= 1 && surroundLeftFlagNumber <= flags.length
                        && RotelFlagsMapping.isBitFlagOn(flags, surroundLeftFlagNumber, surroundLeftBitNumber))
                || (surroundRightFlagNumber >= 1 && surroundRightFlagNumber <= flags.length
                        && RotelFlagsMapping.isBitFlagOn(flags, surroundRightFlagNumber, surroundRightBitNumber));
    }

    /**
     * Get a bit value inside the provided table of flags
     *
     * @param flags the table of flags
     * @param flagNumber the flag number to consider
     * @param bitNumber the bit number in the flag to consider
     *
     * @return true if the bit value in the flag is 1 or false if 0
     *
     * @throws RotelException in case of out of bounds value for the flag number or the bit number
     */
    public static boolean isBitFlagOn(byte[] flags, int flagNumber, int bitNumber) throws RotelException {
        if (flagNumber < 1 || flagNumber > flags.length) {
            throw new RotelException("Flag number out of bounds");
        }
        if (bitNumber < 0 || bitNumber > 7) {
            throw new RotelException("Bit number out of bounds");
        }
        int val = flags[flagNumber - 1] & 0x000000FF;
        return (val & (1 << bitNumber)) != 0;
    }

    /**
     * Set a bit value to 1 or 0 in the provided table of flags
     *
     * @param flags the table of flags
     * @param flagNumber the flag number to consider
     * @param bitNumber the bit number in the flag to consider
     * @param on true to set the bit value to 1 or false to set it to 0
     *
     * @throws RotelException in case of out of bounds value for the flag number or the bit number
     */
    private static void setBitFlag(byte[] flags, int flagNumber, int bitNumber, boolean on) throws RotelException {
        if (flagNumber < 1 || flagNumber > flags.length) {
            throw new RotelException("Flag number out of bounds");
        }
        if (bitNumber < 0 || bitNumber > 7) {
            throw new RotelException("Bit number out of bounds");
        }
        if (on) {
            flags[flagNumber - 1] |= (1 << bitNumber);
        } else {
            flags[flagNumber - 1] &= ~(1 << bitNumber);
        }
    }
}
