/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.comfoair.internal.ComfoAirCommandType;
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

    private Logger logger = LoggerFactory.getLogger(DataTypeVolt.class);

    @Override
    public State convertToState(int @Nullable [] data, ComfoAirCommandType commandType) {
        if (data == null) {
            logger.trace("\"DataTypeVolt\" class \"convertToState\" method parameter: null");
            return UnDefType.NULL;
        } else {
            int[] get_reply_data_pos = commandType.getGetReplyDataPos();
            if (get_reply_data_pos != null && get_reply_data_pos[0] < data.length) {
                return new QuantityType<>((double) data[get_reply_data_pos[0]] * 10 / 255, SmartHomeUnits.VOLT);
            } else {
                return UnDefType.NULL;
            }
        }
    }

    @Override
    public int @Nullable [] convertFromState(State value, ComfoAirCommandType commandType) {
        if (value instanceof UnDefType) {
            logger.trace("\"DataTypeVolt\" class \"convertFromState\" undefined state");
            return null;
        } else {
            int[] template = commandType.getChangeDataTemplate();

            template[commandType.getChangeDataPos()] = (int) (((DecimalType) value).doubleValue() * 255 / 10);

            return template;
        }
    }
}
