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
import org.openhab.binding.homematic.internal.model.HmInterface;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.binding.homematic.internal.model.TclScriptDataEntry;
import org.openhab.binding.homematic.internal.model.TclScriptDataList;

/**
 * Parses parameter descriptions from a CCU script and extracts datapoint metadata.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class CcuParamsetDescriptionParser extends CommonRpcParser<TclScriptDataList, @Nullable Void> {
    private HmParamsetType paramsetType;
    private HmChannel channel;
    private boolean isHmIpDevice;

    public CcuParamsetDescriptionParser(HmChannel channel, HmParamsetType paramsetType) {
        this.channel = channel;
        this.paramsetType = paramsetType;
        this.isHmIpDevice = channel.getDevice().getHmInterface() == HmInterface.HMIP;
    }

    @Override
    public @Nullable Void parse(TclScriptDataList resultList) throws IOException {
        List<TclScriptDataEntry> entries = resultList.getEntries();
        if (entries != null) {
            for (TclScriptDataEntry entry : entries) {
                HmDatapoint dp = assembleDatapoint(entry.name, entry.unit, entry.valueType,
                        this.toOptionList(entry.options), convertToType(entry.minValue), convertToType(entry.maxValue),
                        toInteger(entry.operations), convertToType(entry.value), null, paramsetType, isHmIpDevice);
                channel.addDatapoint(dp);
            }
        }
        return null;
    }

    private String @Nullable [] toOptionList(@Nullable String options) {
        String[] result = options == null ? null : options.split(";");
        return result == null || result.length == 0 ? null : result;
    }
}
