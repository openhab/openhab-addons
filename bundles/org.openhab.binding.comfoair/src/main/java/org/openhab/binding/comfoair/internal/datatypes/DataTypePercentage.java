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
package org.openhab.binding.comfoair.internal.datatypes;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.comfoair.internal.ComfoAirCommandType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle relative values
 *
 * @author Hans Böhm - Initial Contribution
 */
@NonNullByDefault
public class DataTypePercentage implements ComfoAirDataType {
    private static final DataTypePercentage SINGLETON_INSTANCE = new DataTypePercentage();

    private DataTypePercentage() {
    }

    private final Logger logger = LoggerFactory.getLogger(DataTypePercentage.class);

    public static DataTypePercentage getInstance() {
        return SINGLETON_INSTANCE;
    }

    @Override
    public State convertToState(int @Nullable [] data, ComfoAirCommandType commandType) {
        if (data == null) {
            logger.trace("\"DataTypePercentage\" class \"convertToState\" method parameter: null");
            return UnDefType.NULL;
        } else {
            int value = calculateNumberValue(data, commandType);

            if (value < 0) {
                return UnDefType.NULL;
            }

            int[] possibleValues = commandType.getPossibleValues();
            if (possibleValues != null) {
                for (int possibleValue : possibleValues) {
                    if ((value & possibleValue) == possibleValue) {
                        return new QuantityType<>(value, Units.PERCENT);
                    }
                }
                return UnDefType.NULL;
            }
            return new QuantityType<>(value, Units.PERCENT);
        }
    }

    @Override
    public int @Nullable [] convertFromState(State value, ComfoAirCommandType commandType) {
        int[] template = commandType.getChangeDataTemplate();
        int[] possibleValues = commandType.getPossibleValues();
        int position = commandType.getChangeDataPos();

        QuantityType<?> percents = ((QuantityType<?>) value).toUnit(Units.PERCENT);

        if (percents != null) {
            if (possibleValues == null) {
                template[position] = percents.intValue();
            } else {
                for (int i = 0; i < possibleValues.length; i++) {
                    if (possibleValues[i] == percents.intValue()) {
                        template[position] = percents.intValue();
                        break;
                    }
                }
            }
            return template;
        } else {
            logger.trace("\"DataTypePercentage\" class \"convertFromState\" undefined state");
            return null;
        }
    }
}
