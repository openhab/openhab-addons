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
package org.openhab.binding.homematic.internal.communicator.parser;

import java.io.IOException;

import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.binding.homematic.internal.model.HmValueType;
import org.openhab.binding.homematic.internal.model.TclScriptDataEntry;
import org.openhab.binding.homematic.internal.model.TclScriptDataList;

/**
 * Parses a TclRega script result containing variables and scripts.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class CcuVariablesAndScriptsParser extends CommonRpcParser<TclScriptDataList, Void> {
    private HmChannel channel;

    public CcuVariablesAndScriptsParser(HmChannel channel) {
        this.channel = channel;
    }

    @Override
    public Void parse(TclScriptDataList resultList) throws IOException {
        if (resultList.getEntries() != null) {
            for (TclScriptDataEntry entry : resultList.getEntries()) {
                HmDatapoint dp = channel.getDatapoint(HmParamsetType.VALUES, entry.name);
                if (dp != null) {
                    dp.setValue(convertToType(entry.value));
                } else {
                    dp = new HmDatapoint();
                    dp.setName(entry.name);
                    dp.setInfo(entry.name);
                    dp.setDescription(entry.description);
                    dp.setType(HmValueType.parse(entry.valueType));
                    dp.setValue(convertToType(entry.value));
                    if (dp.isIntegerType()) {
                        dp.setMinValue(toInteger(entry.minValue));
                        dp.setMaxValue(toInteger(entry.maxValue));
                    } else if (dp.isFloatType()) {
                        dp.setMinValue(toDouble(entry.minValue));
                        dp.setMaxValue(toDouble(entry.maxValue));
                    }
                    dp.setReadOnly(entry.readOnly);
                    dp.setUnit(entry.unit);
                    String[] result = entry.options == null ? null : entry.options.split(";");
                    dp.setOptions(result == null || result.length == 0 ? null : result);

                    if (dp.getOptions() != null) {
                        dp.setMinValue(0);
                        dp.setMaxValue(dp.getOptions().length - 1);
                    }

                    dp.setParamsetType(HmParamsetType.VALUES);
                    channel.addDatapoint(dp);
                }
            }
        }
        return null;
    }
}
