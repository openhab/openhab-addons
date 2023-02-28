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
 * Class to handle volt values
 *
 * @author Grzegorz Miasko - Initial Contribution
 * @author Hans Böhm - QuantityTypes
 */
@NonNullByDefault
public class DataTypeVolt implements ComfoAirDataType {
    private static final DataTypeVolt SINGLETON_INSTANCE = new DataTypeVolt();

    private DataTypeVolt() {
    }

    private final Logger logger = LoggerFactory.getLogger(DataTypeVolt.class);

    public static DataTypeVolt getInstance() {
        return SINGLETON_INSTANCE;
    }

    @Override
    public State convertToState(int @Nullable [] data, ComfoAirCommandType commandType) {
        if (data == null) {
            logger.trace("\"DataTypeVolt\" class \"convertToState\" method parameter: null");
            return UnDefType.NULL;
        } else {
            int[] readReplyDataPos = commandType.getReadReplyDataPos();
            if (readReplyDataPos != null && readReplyDataPos[0] < data.length) {
                return new QuantityType<>((double) data[readReplyDataPos[0]] * 10 / 255, Units.VOLT);
            } else {
                return UnDefType.NULL;
            }
        }
    }

    @Override
    public int @Nullable [] convertFromState(State value, ComfoAirCommandType commandType) {
        int[] template = commandType.getChangeDataTemplate();
        QuantityType<?> volts = ((QuantityType<?>) value).toUnit(Units.VOLT);

        if (volts != null) {
            template[commandType.getChangeDataPos()] = (int) (volts.doubleValue() * 255 / 10);
            return template;
        } else {
            logger.trace("\"DataTypeVolt\" class \"convertFromState\" undefined state");
            return null;
        }
    }
}
