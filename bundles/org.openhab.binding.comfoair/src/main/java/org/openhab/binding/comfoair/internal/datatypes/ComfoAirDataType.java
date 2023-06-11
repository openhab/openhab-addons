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
package org.openhab.binding.comfoair.internal.datatypes;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.comfoair.internal.ComfoAirCommandType;
import org.openhab.core.types.State;

/**
 * Abstract class to convert binary hex values into openHAB states and vice
 * versa
 *
 * @author Holger Hees - Initial Contribution
 * @author Hans Böhm - Refactoring
 */
@NonNullByDefault
public interface ComfoAirDataType {
    /**
     * Generate a openHAB State object based on response data.
     *
     * @param response
     * @param commandType
     * @return converted State object
     */
    State convertToState(int[] response, ComfoAirCommandType commandType);

    /**
     * Generate byte array based on a openHAB State.
     *
     * @param value
     * @param commandType
     * @return converted byte array
     */
    int @Nullable [] convertFromState(State value, ComfoAirCommandType commandType);

    default int calculateNumberValue(int[] data, ComfoAirCommandType commandType) {
        int[] readReplyDataPos = commandType.getReadReplyDataPos();
        int readReplyDataBits = commandType.getReadReplyDataBits();

        int value = 0;
        if (readReplyDataPos != null) {
            if (readReplyDataBits == 0) {
                int base = 0;

                for (int i = readReplyDataPos.length - 1; i >= 0; i--) {
                    if (readReplyDataPos[i] < data.length) {
                        value += data[readReplyDataPos[i]] << base;
                        base += 8;
                    } else {
                        return -1;
                    }
                }
            } else {
                value = (data[readReplyDataPos[0]] & readReplyDataBits) == readReplyDataBits ? 1 : 0;
            }
        } else {
            value = -1;
        }
        return value;
    }

    default String calculateStringValue(int[] data, ComfoAirCommandType commandType) {
        int[] readReplyDataPos = commandType.getReadReplyDataPos();
        StringBuilder value = new StringBuilder();
        if (readReplyDataPos != null) {
            for (int pos : readReplyDataPos) {
                if (pos < data.length) {
                    value.append((char) data[pos]);
                }
            }
        }
        return value.toString();
    }
}
