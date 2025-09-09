/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle revolutions per minute values
 *
 * @author Grzegorz Miasko - Initial Contribution
 * @author Hans Böhm - Refactoring
 */
@NonNullByDefault
public class DataTypeRPM implements ComfoAirDataType {
    private static final DataTypeRPM SINGLETON_INSTANCE = new DataTypeRPM();

    private DataTypeRPM() {
    }

    private final Logger logger = LoggerFactory.getLogger(DataTypeRPM.class);

    public static DataTypeRPM getInstance() {
        return SINGLETON_INSTANCE;
    }

    @Override
    public State convertToState(int @Nullable [] data, ComfoAirCommandType commandType) {
        if (data == null) {
            logger.trace("\"DataTypeRPM\" class \"convertToState\" method parameter: null");
            return UnDefType.NULL;
        } else {
            int value = calculateNumberValue(data, commandType);

            if (value < 0) {
                return UnDefType.NULL;
            }
            // transferred value is (1875000 / rpm) per protocol
            return new QuantityType<>((int) (1875000.0 / value), Units.RPM);
        }
    }

    @Override
    public int @Nullable [] convertFromState(State value, ComfoAirCommandType commandType) {
        int[] template = commandType.getChangeDataTemplate();
        float rpm;

        if (value instanceof QuantityType<?> qt) {
            QuantityType<?> qtRpm = qt.toUnit(Units.RPM);

            if (qtRpm != null) {
                rpm = qtRpm.floatValue();
            } else {
                return null;
            }
        } else if (value instanceof DecimalType dt) {
            rpm = dt.floatValue();
        } else {
            logger.trace("\"DataTypeRPM\" class \"convertFromState\" undefined state");
            return null;
        }

        // transferred value is (1875000 / rpm) per protocol
        template[commandType.getChangeDataPos()] = (int) (1875000 / rpm);
        return template;
    }
}
