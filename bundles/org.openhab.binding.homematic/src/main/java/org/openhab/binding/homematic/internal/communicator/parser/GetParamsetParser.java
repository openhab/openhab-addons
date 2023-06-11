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
import java.util.Map;

import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.openhab.binding.homematic.internal.model.HmInterface;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses a paramset message and extracts datapoint values.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class GetParamsetParser extends CommonRpcParser<Object[], Void> {
    private final Logger logger = LoggerFactory.getLogger(GetParamsetParser.class);

    private HmChannel channel;
    private HmParamsetType paramsetType;

    public GetParamsetParser(HmChannel channel, HmParamsetType paramsetType) {
        this.channel = channel;
        this.paramsetType = paramsetType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Void parse(Object[] message) throws IOException {
        if (message == null || message.length == 0 || !(message[0] instanceof Map)) {
            return null;
        }

        Map<String, ?> mapMessage = (Map<String, ?>) message[0];
        for (String dpName : mapMessage.keySet()) {
            HmDatapointInfo dpInfo = new HmDatapointInfo(paramsetType, channel, dpName);
            HmDatapoint dp = channel.getDatapoint(dpInfo);
            if (dp != null) {
                dp.setValue(convertToType(dp, mapMessage.get(dpName)));
                adjustRssiValue(dp);
            } else {
                // should never happen, but in case ...

                // suppress warning for this datapoint due wrong CCU metadata
                String deviceType = channel.getDevice().getType();
                boolean isHmSenMdirNextTrans = dpInfo.getName().equals("NEXT_TRANSMISSION")
                        && (deviceType.startsWith("HM-Sen-MDIR-O") || deviceType.startsWith("HM-Sen-MDIR-WM55")
                                || deviceType.startsWith("HM-Sec-MDIR-2"));
                if (!isHmSenMdirNextTrans) {
                    if (dpInfo.getParamsetType() == HmParamsetType.MASTER
                            && channel.getDevice().getHmInterface() == HmInterface.HMIP) {
                        // These data points can't currently be recognized and therefore can't be created
                        logger.debug("Can't set value for channel configuration datapoint '{}'", dpInfo);
                    } else {
                        logger.warn("Can't set value for datapoint '{}'", dpInfo);
                    }
                }
            }
        }
        return null;
    }
}
