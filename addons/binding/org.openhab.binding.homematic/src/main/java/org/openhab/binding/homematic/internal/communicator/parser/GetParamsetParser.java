/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses a paramset message and extracts datapoint values.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class GetParamsetParser extends CommonRpcParser<Object[], Void> {
    private static final Logger logger = LoggerFactory.getLogger(GetParamsetParser.class);

    private HmChannel channel;
    private HmParamsetType paramsetType;

    public GetParamsetParser(HmChannel channel, HmParamsetType paramsetType) {
        this.channel = channel;
        this.paramsetType = paramsetType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Void parse(Object[] message) throws IOException {
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
                boolean isHmSenMdirNextTrans = channel.getDevice().getType().startsWith("HM-Sen-MDIR-O")
                        && dpInfo.getName().equals("NEXT_TRANSMISSION");
                if (!isHmSenMdirNextTrans) {
                    logger.warn("Can't set value for datapoint '{}'", dpInfo);
                }
            }
        }
        return null;
    }
}
