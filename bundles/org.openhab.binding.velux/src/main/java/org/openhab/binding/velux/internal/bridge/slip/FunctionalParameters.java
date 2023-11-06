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
package org.openhab.binding.velux.internal.bridge.slip;

import java.util.Arrays;

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

    private final int[] values;

    /**
     * Private constructor to create a FunctionalParameters instance with all empty values.
     */
    private FunctionalParameters() {
        values = new int[FUNCTIONAL_PARAMETER_COUNT];
        Arrays.fill(values, VeluxProductPosition.VPP_VELUX_UNKNOWN);
    }

    /**
     * Public constructor to create a FunctionalParameters instance from one specific value at one specific index.
     */
    public FunctionalParameters(int index, int newValue) {
        this();
        values[index] = newValue;
    }

    @Override
    public FunctionalParameters clone() {
        FunctionalParameters result = new FunctionalParameters();
        System.arraycopy(values, 0, result.values, 0, FUNCTIONAL_PARAMETER_COUNT);
        return result;
    }

    @Override
    public String toString() {
        return String.format("{0x%04X, 0x%04X, 0x%04X, 0x%04X}", values[0], values[1], values[2], values[3]);
    }

    /**
     * Return the functional parameter value at index.
     *
     * @return the value at the index.
     */
    public int getValue(int index) {
        return values[index];
    }

    /**
     * Create a Functional Parameters instance from the merger of the data in 'foundationFunctionalParameters' and
     * 'substituteFunctionalParameters'. Invalid parameter values are not merged. If either
     * 'foundationFunctionalParameters' or 'substituteFunctionalParameters' is null, the merge includes only the data
     * from the non null parameter set. And if both sets of parameters are null then the result is also null.
     *
     * @param foundationFunctionalParameters the Functional Parameters to be used as the foundation for the merge.
     * @param substituteFunctionalParameters the Functional Parameters to substituted on top (if they can be).
     * @return a new Functional Parameters class instance containing the merged data.
     */
    public static @Nullable FunctionalParameters createMergeSubstitute(
            @Nullable FunctionalParameters foundationFunctionalParameters,
            @Nullable FunctionalParameters substituteFunctionalParameters) {
        if (foundationFunctionalParameters == null && substituteFunctionalParameters == null) {
            return null;
        }
        FunctionalParameters result = new FunctionalParameters();
        if (foundationFunctionalParameters != null) {
            for (int i = 0; i < FUNCTIONAL_PARAMETER_COUNT; i++) {
                if (isNormalPosition(foundationFunctionalParameters.values[i])) {
                    result.values[i] = foundationFunctionalParameters.values[i];
                }
            }
        }
        if (substituteFunctionalParameters != null) {
            for (int i = 0; i < FUNCTIONAL_PARAMETER_COUNT; i++) {
                if (isNormalPosition(substituteFunctionalParameters.values[i])) {
                    result.values[i] = substituteFunctionalParameters.values[i];
                }
            }
        }
        return result;
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
     * Create a FunctionalParameters instance from the given Packet. Where the parameters are packed into an array of
     * two byte integer values.
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
     * Create a FunctionalParameters instance from the given Packet. Where the parameters are packed into an array of
     * three byte records each comprising a one byte index followed by a two byte integer value.
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

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof FunctionalParameters)) {
            return false;
        }
        FunctionalParameters other = (FunctionalParameters) obj;
        for (int i = 0; i < FUNCTIONAL_PARAMETER_COUNT; i++) {
            if (values[i] != other.values[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    };
}
