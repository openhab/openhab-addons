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
package org.openhab.binding.comfoair.internal.datatypes;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.comfoair.internal.ComfoAirCommandType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle numeric values
 *
 * @author Holger Hees - Initial Contribution
 * @author Hans Böhm - Refactoring
 */
@NonNullByDefault
public class DataTypeNumber implements ComfoAirDataType {
    private static final DataTypeNumber SINGLETON_INSTANCE = new DataTypeNumber();

    private DataTypeNumber() {
    }

    private final Logger logger = LoggerFactory.getLogger(DataTypeNumber.class);

    public static DataTypeNumber getInstance() {
        return SINGLETON_INSTANCE;
    }

    @Override
    public State convertToState(int @Nullable [] data, ComfoAirCommandType commandType) {
        if (data == null) {
            logger.trace("\"DataTypeNumber\" class \"convertToState\" method parameter: null");
            return UnDefType.NULL;
        } else {
            int value = calculateNumberValue(data, commandType);

            if (value < 0) {
                return UnDefType.NULL;
            }

            int[] possibleValues = commandType.getPossibleValues();
            if (possibleValues != null) {
                // fix for unexpected value for "level" value. got a 0x33. valid was
                // the 0x03. 0x30 was to much.
                // send DATA: 07 f0 00 cd 00 7a 07 0f
                // receive CMD: ce DATA: 0f 20 32 00 0f 21 33 2d 33 03 01 5a 5b 00
                for (int possibleValue : possibleValues) {
                    if ((value & possibleValue) == possibleValue) {
                        return new DecimalType(value);
                    }
                }
                return UnDefType.NULL;
            }
            return new DecimalType(value);
        }
    }

    @Override
    public int @Nullable [] convertFromState(State value, ComfoAirCommandType commandType) {
        if (value instanceof UnDefType) {
            logger.trace("\"DataTypeNumber\" class \"convertFromState\" undefined state");
            return null;
        } else {
            int[] template = commandType.getChangeDataTemplate();
            int[] possibleValues = commandType.getPossibleValues();
            int position = commandType.getChangeDataPos();

            int intValue = ((DecimalType) value).intValue();

            if (possibleValues == null) {
                template[position] = intValue;
            } else {
                for (int i = 0; i < possibleValues.length; i++) {
                    if (possibleValues[i] == intValue) {
                        template[position] = intValue;
                        break;
                    }
                }
            }
            return template;
        }
    }
}
