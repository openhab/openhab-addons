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
package org.openhab.binding.rotel.internal.communication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rotel.internal.RotelException;

/**
 * Class managing the mapping of message flags with indicators
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RotelFlagsMapping {

    public static final RotelFlagsMapping MAPPING1 = new RotelFlagsMapping(
            List.of(new RotelFlagInfo(RotelFlagInfoType.MULTI_INPUT, 3, 1), //
                    new RotelFlagInfo(RotelFlagInfoType.ZONE2, 5, 0), //
                    new RotelFlagInfo(RotelFlagInfoType.CENTER, 8, 6), //
                    new RotelFlagInfo(RotelFlagInfoType.SURROUND_LEFT, 8, 4), //
                    new RotelFlagInfo(RotelFlagInfoType.SURROUND_RIGHT, 8, 3)));
    public static final RotelFlagsMapping MAPPING2 = new RotelFlagsMapping(
            List.of(new RotelFlagInfo(RotelFlagInfoType.ZONE2, 4, 7), //
                    new RotelFlagInfo(RotelFlagInfoType.CENTER, 5, 6), //
                    new RotelFlagInfo(RotelFlagInfoType.SURROUND_LEFT, 5, 4), //
                    new RotelFlagInfo(RotelFlagInfoType.SURROUND_RIGHT, 5, 3)));
    public static final RotelFlagsMapping MAPPING3 = new RotelFlagsMapping(
            List.of(new RotelFlagInfo(RotelFlagInfoType.ZONE2, 3, 2), //
                    new RotelFlagInfo(RotelFlagInfoType.ZONE3, 4, 2), //
                    new RotelFlagInfo(RotelFlagInfoType.ZONE4, 4, 1), //
                    new RotelFlagInfo(RotelFlagInfoType.CENTER, 5, 6), //
                    new RotelFlagInfo(RotelFlagInfoType.SURROUND_LEFT, 5, 4), //
                    new RotelFlagInfo(RotelFlagInfoType.SURROUND_RIGHT, 5, 3)));

    private Map<RotelFlagInfoType, RotelFlagInfo> infoMap;

    public RotelFlagsMapping() {
        this.infoMap = new HashMap<>();
    }

    public RotelFlagsMapping(List<RotelFlagInfo> infos) {
        this.infoMap = new HashMap<>();
        for (RotelFlagInfo info : infos) {
            this.infoMap.put(info.infoType(), info);
        }
    }

    /**
     * Get the availability of the information
     *
     * @return true if the information is available
     */
    public boolean isInfoPresent(RotelFlagInfoType infoType) {
        return infoMap.get(infoType) != null;
    }

    /**
     * Get the information
     *
     * @param infoType the type of information
     * @param flags the table of flags
     *
     * @return true if the information is ON in the flags or false if OFF
     *
     * @throws RotelException in case the information is undefined
     */
    public boolean isInfoOn(RotelFlagInfoType infoType, byte[] flags) throws RotelException {
        RotelFlagInfo info = infoMap.get(infoType);
        if (info == null || info.flagNumber() > flags.length) {
            throw new RotelException("Info " + infoType.name() + " not available in flags");
        }
        return RotelFlagsMapping.isBitFlagOn(flags, info.flagNumber(), info.bitNumber());
    }

    /**
     * Set the information
     *
     * @param infoType the type of information
     * @param flags the table of flags
     * @param on true to set the information to ON or false to set it to OFF
     *
     * @return true if the information was updated, false if not
     */
    public boolean setInfo(RotelFlagInfoType infoType, byte[] flags, boolean on) {
        RotelFlagInfo info = infoMap.get(infoType);
        return info == null ? false : RotelFlagsMapping.setBitFlag(flags, info.flagNumber(), info.bitNumber(), on);
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
        RotelFlagInfo center = infoMap.get(RotelFlagInfoType.CENTER);
        RotelFlagInfo surroundLeft = infoMap.get(RotelFlagInfoType.SURROUND_LEFT);
        RotelFlagInfo surroundRight = infoMap.get(RotelFlagInfoType.SURROUND_RIGHT);
        return (center != null && center.flagNumber() <= flags.length
                && RotelFlagsMapping.isBitFlagOn(flags, center.flagNumber(), center.bitNumber()))
                || (surroundLeft != null && surroundLeft.flagNumber() <= flags.length
                        && RotelFlagsMapping.isBitFlagOn(flags, surroundLeft.flagNumber(), surroundLeft.bitNumber()))
                || (surroundRight != null && surroundRight.flagNumber() <= flags.length
                        && RotelFlagsMapping.isBitFlagOn(flags, surroundRight.flagNumber(), surroundRight.bitNumber()));
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
     * @return true if the flag was updated, false if not
     */
    private static boolean setBitFlag(byte[] flags, int flagNumber, int bitNumber, boolean on) {
        if (flagNumber < 1 || flagNumber > flags.length || bitNumber < 0 || bitNumber > 7) {
            return false;
        }
        if (on) {
            flags[flagNumber - 1] |= (1 << bitNumber);
        } else {
            flags[flagNumber - 1] &= ~(1 << bitNumber);
        }
        return true;
    }
}
