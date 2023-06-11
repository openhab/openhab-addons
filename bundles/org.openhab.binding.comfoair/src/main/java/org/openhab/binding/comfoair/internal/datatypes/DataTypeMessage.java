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
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle error messages
 *
 * @author Holger Hees - Initial Contribution
 * @author Hans Böhm - Refactoring
 */
@NonNullByDefault
public class DataTypeMessage implements ComfoAirDataType {
    private static final DataTypeMessage SINGLETON_INSTANCE = new DataTypeMessage();

    private DataTypeMessage() {
    }

    private final Logger logger = LoggerFactory.getLogger(DataTypeMessage.class);

    public static DataTypeMessage getInstance() {
        return SINGLETON_INSTANCE;
    }

    @Override
    public State convertToState(int @Nullable [] data, ComfoAirCommandType commandType) {
        if (data == null) {
            logger.trace("\"DataTypeMessage\" class \"convertToState\" method parameter: null");
            return UnDefType.NULL;
        } else {
            int[] readReplyDataPos = commandType.getReadReplyDataPos();
            if (readReplyDataPos != null) {
                int errorAlo = data[readReplyDataPos[0]];
                int errorE = data[readReplyDataPos[1]];
                int errorEA = (data.length > 9) ? data[readReplyDataPos[2]] : -1;
                int errorAhi = (data.length > 9) ? data[readReplyDataPos[3]] : -1;

                StringBuilder errorCode = new StringBuilder();

                if (errorAlo > 0) {
                    errorCode.append("A");
                    errorCode.append(convertToCode(errorAlo));
                } else if (errorAhi > 0) {
                    if (errorAhi == 0x80) {
                        errorCode.append("A0");
                    } else {
                        errorCode.append("A");
                        errorCode.append(convertToCode(errorAhi) + 8);
                    }
                }

                if (errorE > 0) {
                    if (errorCode.length() > 0) {
                        errorCode.append(" ");
                    }
                    errorCode.append("E");
                    errorCode.append(convertToCode(errorE));
                } else if (errorEA > 0) {
                    if (errorCode.length() > 0) {
                        errorCode.append(" ");
                    }
                    errorCode.append("EA");
                    errorCode.append(convertToCode(errorEA));
                }
                return new StringType(errorCode.length() > 0 ? errorCode.toString() : "No Errors");
            } else {
                return UnDefType.UNDEF;
            }
        }
    }

    @Override
    public int @Nullable [] convertFromState(State value, ComfoAirCommandType commandType) {
        return null;
    }

    private int convertToCode(int code) {
        switch (code) {
            case 0x01:
                return 1;
            case 0x02:
                return 2;
            case 0x04:
                return 3;
            case 0x08:
                return 4;
            case 0x10:
                return 5;
            case 0x20:
                return 6;
            case 0x40:
                return 7;
            case 0x80:
                return 8;
            default:
                return -1;
        }
    }
}
