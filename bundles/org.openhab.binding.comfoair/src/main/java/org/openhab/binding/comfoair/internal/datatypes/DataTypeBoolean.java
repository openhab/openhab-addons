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

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.comfoair.internal.ComfoAirCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle boolean values which are handled as decimal 0/1 states
 *
 * @author Holger Hees - Initial Contribution
 * @author Hans Böhm - Refactoring
 */
public class DataTypeBoolean implements ComfoAirDataType {
    private Logger logger = LoggerFactory.getLogger(DataTypeBoolean.class);

    @Override
    public State convertToState(int[] data, ComfoAirCommandType commandType) {
        if (data == null || commandType == null) {
            logger.trace("\"DataTypeBoolean\" class \"convertToState\" method parameter: null");
            return UnDefType.NULL;
        } else {
            int[] get_reply_data_pos = commandType.getGetReplyDataPos();
            int get_reply_data_bits = commandType.getGetReplyDataBits();

            if (get_reply_data_pos[0] < data.length) {
                boolean result = (data[get_reply_data_pos[0]] & get_reply_data_bits) == get_reply_data_bits;
                return (result) ? OnOffType.ON : OnOffType.OFF;
            } else {
                return UnDefType.NULL;
            }
        }
    }

    @Override
    public int[] convertFromState(State value, ComfoAirCommandType commandType) {
        if (value == null || commandType == null) {
            logger.trace("\"DataTypeBoolean\" class \"convertFromState\" method parameter: null");
            return null;
        } else {
            DecimalType decimalValue = value.as(DecimalType.class);

            if (decimalValue != null) {
                int[] template = commandType.getChangeDataTemplate();

                template[commandType.getChangeDataPos()] = decimalValue.intValue() == 1
                        ? commandType.getPossibleValues()[0]
                        : 0x00;

                return template;
            } else {
                logger.trace(
                        "\"DataTypeBoolean\" class \"convertFromState\" method: State value conversion returned null");
                return null;
            }
        }
    }
}
