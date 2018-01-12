/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.parser;

import java.io.IOException;
import java.util.Map;

import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.binding.homematic.internal.model.HmValueType;

/**
 * Parses a Homegear message with variables and generates the datapoints.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class GetAllSystemVariablesParser extends CommonRpcParser<Object[], Void> {
    private HmChannel channel;

    public GetAllSystemVariablesParser(HmChannel channel) {
        this.channel = channel;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Void parse(Object[] message) throws IOException {
        Map<String, ?> mapMessage = (Map<String, ?>) message[0];
        for (String variableName : mapMessage.keySet()) {
            Object value = mapMessage.get(variableName);
            HmDatapoint dp = channel.getDatapoint(HmParamsetType.VALUES, variableName);
            if (dp != null) {
                dp.setValue(value);
            } else {
                HmDatapoint dpVariable = new HmDatapoint(variableName, variableName, guessType(value), value, false,
                        HmParamsetType.VALUES);
                dpVariable.setInfo(variableName);
                channel.addDatapoint(dpVariable);
            }
        }
        return null;
    }

    /**
     * Guesses the value type.
     */
    private HmValueType guessType(Object value) {
        if (value == null) {
            return HmValueType.UNKNOWN;
        } else if (value instanceof Boolean) {
            return HmValueType.BOOL;
        } else if (value instanceof Integer || value instanceof Long) {
            return HmValueType.INTEGER;
        } else if (value instanceof Number) {
            return HmValueType.FLOAT;
        } else {
            return HmValueType.STRING;
        }
    }

}
