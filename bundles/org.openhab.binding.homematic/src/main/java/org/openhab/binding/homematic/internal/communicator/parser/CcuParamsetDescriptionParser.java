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
package org.openhab.binding.homematic.internal.communicator.parser;

import java.io.IOException;

import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmInterface;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.binding.homematic.internal.model.TclScriptDataEntry;
import org.openhab.binding.homematic.internal.model.TclScriptDataList;

/**
 * Parses parameter descriptions from a CCU script and extracts datapoint metadata.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class CcuParamsetDescriptionParser extends CommonRpcParser<TclScriptDataList, Void> {
    private HmParamsetType paramsetType;
    private HmChannel channel;
    private boolean isHmIpDevice;

    public CcuParamsetDescriptionParser(HmChannel channel, HmParamsetType paramsetType) {
        this.channel = channel;
        this.paramsetType = paramsetType;
        this.isHmIpDevice = channel.getDevice().getHmInterface() == HmInterface.HMIP;
    }

    @Override
    public Void parse(TclScriptDataList resultList) throws IOException {
        if (resultList.getEntries() != null) {
            for (TclScriptDataEntry entry : resultList.getEntries()) {
                HmDatapoint dp = assembleDatapoint(entry.name, entry.unit, entry.valueType,
                        this.toOptionList(entry.options), convertToType(entry.minValue), convertToType(entry.maxValue),
                        toInteger(entry.operations), convertToType(entry.value), paramsetType, isHmIpDevice);
                channel.addDatapoint(dp);
            }
        }
        return null;
    }

    private String[] toOptionList(String options) {
        String[] result = options == null ? null : options.split(";");
        return result == null || result.length == 0 ? null : result;
    }
}
