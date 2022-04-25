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
package org.openhab.binding.velux.internal.bridge.slip;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.things.VeluxProductPosition;

/**
 * Implementation of API Functional Parameters.
 * Supports an array of of four Functional Parameter values { FP1 .. FP4 }
 *
 * @author Andrew Fiddian-Green - Initial contribution.
 */

@NonNullByDefault
public class FunctionalParameters {
    private static final int FUNCTIONAL_PARAMETER_COUNT = 4;

    private final int[] values = new int[FUNCTIONAL_PARAMETER_COUNT];

    /**
     * Constructor.
     * Initialises the array of values.
     */
    public FunctionalParameters() {
        for (int i = 0; i < FUNCTIONAL_PARAMETER_COUNT; i++) {
            values[i] = VeluxProductPosition.VPP_VELUX_UNKNOWN;
        }
    }

    @Override
    public FunctionalParameters clone() {
        FunctionalParameters result = new FunctionalParameters();
        result.setValues(this);
        return result;
    }

    @Override
    public String toString() {
        return String.format("{0x%04X, 0x%04X, 0x%04X, 0x%04X}", values[0], values[1], values[2], values[3]);
    }

    /**
     * Return the functional parameter values as an array of int.
     *
     * @return
     */
    public int[] getValues() {
        return values;
    }

    /**
     * Set a new set of Functional Parameter values.
     *
     * @param newFunctionalParameters the new set of Functional Parameter values.
     */
    public void setValues(FunctionalParameters newFunctionalParameters) {
        System.arraycopy(newFunctionalParameters.getValues(), 0, values, 0, FUNCTIONAL_PARAMETER_COUNT);
    }

    /**
     * Set one specific Functional Parameter value.
     * Note: Java runtime does the range check, and eventually throws index out of bound exception.
     *
     * @param index the index of the value to be set.
     * @param newValue the new value.
     */
    public void setValue(int index, int newValue) {
        values[index] = newValue;
    }

    /**
     * Copy the Functional Parameters from newFunctionalParameters. Substitute VPP_VELUX_UNKNOWN for any values that
     * are not supported by the VeluxProduct class.
     *
     * @param newFunctionalParameters the Functional Parameters to copy from.
     * @return isModified if any of the values have been modified.
     */
    public boolean setProductAllowedFPValues(FunctionalParameters newFunctionalParameters) {
        boolean isModified = false;
        int[] newVals = newFunctionalParameters.getValues();
        for (int i = 0; i < FUNCTIONAL_PARAMETER_COUNT; i++) {
            if ((values[i] == newVals[i]) || isIgnore(newVals[i])) {
                continue;
            } else if (isNormalPosition(newVals[i])) {
                values[i] = newVals[i];
                isModified = true;
            } else {
                values[i] = VeluxProductPosition.VPP_VELUX_UNKNOWN;
                isModified = true;
            }
        }
        return isModified;
    }

    /**
     * Check if a given parameter value is a normal actuator position value (i.e. 0x0000 .. 0xC800).
     *
     * @param paramValue the value to be checked.
     * @return true if it is a normal actuator position value.
     */
    public static boolean isNormalPosition(int paramValue) {
        return (paramValue >= VeluxProductPosition.VPP_VELUX_MIN) && (paramValue <= VeluxProductPosition.VPP_VELUX_MAX);
    }

    /**
     * Check if a given parameter value is the 'undefined' actuator position value (i.e. 0xF7FF).
     *
     * @param paramValue the value to be checked.
     * @return true if it is the 'undefined' value.
     */
    public static boolean isUndefined(int paramValue) {
        return paramValue == VeluxProductPosition.VPP_VELUX_UNKNOWN;
    }

    /**
     * Check if a given parameter value is the 'ignore' actuator position value (i.e. 0xD400).
     *
     * @param paramValue the value to be checked.
     * @return true if it is the 'ignore' value.
     */
    public static boolean isIgnore(int paramValue) {
        return paramValue == VeluxProductPosition.VPP_VELUX_IGNORE;
    }

    /**
     * Read the Functional Parameters from the given Packet. Where the parameters are packed into an array of two byte
     * integer values.
     *
     * @param responseData the Packet to read from.
     * @param startPosition the read starting position.
     * @return this object.
     */
    public static @Nullable FunctionalParameters readArray(Packet responseData, int startPosition) {
        int pos = startPosition;
        boolean isValid = false;
        FunctionalParameters result = new FunctionalParameters();
        for (int i = 0; i < FUNCTIONAL_PARAMETER_COUNT; i++) {
            int value = responseData.getTwoByteValue(pos);
            if (isNormalPosition(value)) {
                result.values[i] = value;
                isValid = true;
            }
            pos = pos + 2;
        }
        return isValid ? result : null;
    }

    /**
     * Read the Functional Parameters from the given Packet. Where the parameters are packed into an array of three byte
     * records each comprising a one byte index followed by a two byte integer value.
     *
     * @param responseData the Packet to read from.
     * @param startPosition the read starting position.
     * @return this object.
     */
    public static @Nullable FunctionalParameters readArrayIndexed(Packet responseData, int startPosition) {
        int pos = startPosition;
        boolean isValid = false;
        FunctionalParameters result = new FunctionalParameters();
        for (int i = 0; i < FUNCTIONAL_PARAMETER_COUNT; i++) {
            int index = responseData.getOneByteValue(pos) - 1;
            pos++;
            if ((index >= 0) && (index < FUNCTIONAL_PARAMETER_COUNT)) {
                int value = responseData.getTwoByteValue(pos);
                if (isNormalPosition(value)) {
                    result.values[index] = value;
                    isValid = true;
                }
            }
            pos = pos + 2;
        }
        return isValid ? result : null;
    }

    /**
     * Write the Functional Parameters to the given packet. Only writes normal valid position values.
     *
     * @param requestData the Packet to write to.
     * @param startPosition the write starting position.
     * @return fpIndex a bit map that indicates which of the written Functional Parameters contains a normal valid
     *         position value.
     */
    public int writeArray(Packet requestData, int startPosition) {
        int bitMask = 0b10000000;
        int pos = startPosition;
        int fpIndex = 0;
        for (int i = 0; i < FUNCTIONAL_PARAMETER_COUNT; i++) {
            if (isNormalPosition(values[i])) {
                fpIndex |= bitMask;
                requestData.setTwoByteValue(pos, values[i]);
            }
            pos = pos + 2;
            bitMask = bitMask >>> 1;
        }
        return fpIndex;
    }
}
