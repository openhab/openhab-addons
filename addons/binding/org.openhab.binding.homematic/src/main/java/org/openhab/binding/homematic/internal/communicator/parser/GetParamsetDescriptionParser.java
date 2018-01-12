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
import org.openhab.binding.homematic.internal.model.HmInterface;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses a parameter description message and extracts datapoint metadata.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class GetParamsetDescriptionParser extends CommonRpcParser<Object[], Void> {
    private final Logger logger = LoggerFactory.getLogger(GetParamsetDescriptionParser.class);
    private HmParamsetType paramsetType;
    private HmChannel channel;
    private boolean isHmIpDevice;

    public GetParamsetDescriptionParser(HmChannel channel, HmParamsetType paramsetType) {
        this.channel = channel;
        this.paramsetType = paramsetType;
        this.isHmIpDevice = channel.getDevice().getHmInterface() == HmInterface.HMIP;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Void parse(Object[] message) throws IOException {
        if (!(message[0] instanceof Map)) {
            logger.debug("Unexpected datatype '{}',  ignoring message", message[0].getClass());
            return null;
        }
        Map<String, Map<String, Object>> dpNames = (Map<String, Map<String, Object>>) message[0];

        for (String datapointName : dpNames.keySet()) {
            Map<String, Object> dpMeta = dpNames.get(datapointName);

            HmDatapoint dp = assembleDatapoint(datapointName, toString(dpMeta.get("UNIT")),
                    toString(dpMeta.get("TYPE")), toOptionList(dpMeta.get("VALUE_LIST")), dpMeta.get("MIN"),
                    dpMeta.get("MAX"), toInteger(dpMeta.get("OPERATIONS")), dpMeta.get("DEFAULT"), paramsetType,
                    isHmIpDevice);
            channel.addDatapoint(dp);
        }

        return null;
    }
}
