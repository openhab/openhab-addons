/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.binding.homematic.internal.model.HmValueType;
import org.openhab.binding.homematic.internal.model.dto.TclScriptDataEntry;
import org.openhab.binding.homematic.internal.model.dto.TclScriptDataList;

/**
 * Parses a TclRega script result containing variables and scripts.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class CcuVariablesAndScriptsParser extends CommonRpcParser<TclScriptDataList, @Nullable Void> {
    private HmChannel channel;

    public CcuVariablesAndScriptsParser(HmChannel channel) {
        this.channel = channel;
    }

    @Override
    public @Nullable Void parse(TclScriptDataList resultList) throws IOException {
        List<TclScriptDataEntry> entries = resultList.getEntries();
        if (entries != null) {
            for (TclScriptDataEntry entry : entries) {
                HmDatapoint dp = channel.getDatapoint(HmParamsetType.VALUES, entry.name);
                if (dp != null) {
                    dp.setValue(convertToType(entry.value));
                } else {
                    dp = new HmDatapoint(entry.name, entry.description, HmValueType.parse(entry.valueType),
                            convertToType(entry.value), entry.readOnly, HmParamsetType.VALUES);
                    dp.setInfo(entry.name);

                    if (dp.isIntegerType()) {
                        dp.setMinValue(toInteger(entry.minValue));
                        dp.setMaxValue(toInteger(entry.maxValue));
                    } else if (dp.isFloatType()) {
                        dp.setMinValue(toDouble(entry.minValue));
                        dp.setMaxValue(toDouble(entry.maxValue));
                    }
                    dp.setUnit(entry.unit);
                    String[] result = entry.options == null || entry.options.isEmpty() ? null
                            : entry.options.split(";");
                    dp.setOptions(result);

                    if (result != null) {
                        dp.setMinValue(0);
                        dp.setMaxValue(result.length - 1);
                    }

                    dp.setParamsetType(HmParamsetType.VALUES);
                    channel.addDatapoint(dp);
                }
            }
        }
        return null;
    }
}
