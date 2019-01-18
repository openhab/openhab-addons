/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.homematic.internal.communicator.parser;

import java.io.IOException;

import org.eclipse.smarthome.binding.homematic.internal.model.HmChannel;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapoint;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapointInfo;
import org.eclipse.smarthome.binding.homematic.internal.model.TclScriptDataEntry;
import org.eclipse.smarthome.binding.homematic.internal.model.TclScriptDataList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses a TclRega script result containing datapoint values for a channel.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class CcuValueParser extends CommonRpcParser<TclScriptDataList, Void> {
    private final Logger logger = LoggerFactory.getLogger(CcuValueParser.class);

    private HmChannel channel;

    public CcuValueParser(HmChannel channel) {
        this.channel = channel;
    }

    @Override
    public Void parse(TclScriptDataList resultList) throws IOException {
        if (resultList.getEntries() != null) {
            for (TclScriptDataEntry entry : resultList.getEntries()) {
                HmDatapointInfo dpInfo = HmDatapointInfo.createValuesInfo(channel, entry.name);
                HmDatapoint dp = channel.getDatapoint(dpInfo);
                if (dp != null) {
                    dp.setValue(convertToType(dp, entry.value));
                    adjustRssiValue(dp);
                } else {
                    // should never happen, but in case ...
                    logger.warn("Can't set value for datapoint '{}'", dpInfo);
                }
            }
        }
        return null;
    }
}
