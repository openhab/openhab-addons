/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmInterface;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.binding.homematic.internal.model.HmValueType;

/**
 * Parses a parameter description message and extracts datapoint metadata.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class GetParamsetDescriptionParser extends CommonRpcParser<Object[], Void> {
    private HmParamsetType paramsetType;
    private HmChannel channel;

    public GetParamsetDescriptionParser(HmChannel channel, HmParamsetType paramsetType) {
        this.channel = channel;
        this.paramsetType = paramsetType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Void parse(Object[] message) throws IOException {
        Collection<HmDatapoint> datapoints = new ArrayList<HmDatapoint>();
        Map<String, Map<String, ?>> dpNames = (Map<String, Map<String, ?>>) message[0];
        boolean isHmIpDevice = channel.getDevice().getHmInterface() == HmInterface.HMIP;

        for (String datapointName : dpNames.keySet()) {
            Map<String, ?> dpMeta = dpNames.get(datapointName);
            HmDatapoint dp = new HmDatapoint();
            dp.setName(datapointName);
            dp.setDescription(datapointName);
            dp.setUnit(StringUtils.replace(toString(dpMeta.get("UNIT")), "�", "°"));
            if (dp.getUnit() == null && StringUtils.startsWith(dp.getName(), "RSSI_")) {
                dp.setUnit("dBm");
            }

            HmValueType type = HmValueType.parse(toString(dpMeta.get("TYPE")));
            if (type == null || type == HmValueType.UNKNOWN) {
                throw new IOException("Unknown datapoint type: " + toString(dpMeta.get("TYPE")));
            }

            dp.setOptions(toOptionList(dpMeta.get("VALUE_LIST")));
            dp.setType(type);
            if (dp.isNumberType() || dp.isEnumType()) {
                if (isHmIpDevice && dp.isEnumType()) {
                    dp.setMinValue(dp.getOptionIndex(toString(dpMeta.get("MIN"))));
                    dp.setMaxValue(dp.getOptionIndex(toString(dpMeta.get("MAX"))));
                } else {
                    dp.setMinValue(toNumber(dpMeta.get("MIN")));
                    dp.setMaxValue(toNumber(dpMeta.get("MAX")));
                }
            }
            Integer operations = toInteger(dpMeta.get("OPERATIONS"));
            dp.setReadOnly((operations & 2) != 2);
            dp.setReadable((operations & 1) == 1);
            dp.setParamsetType(paramsetType);
            if (isHmIpDevice && dp.isEnumType()) {
                dp.setDefaultValue(dp.getOptionIndex(toString(dpMeta.get("DEFAULT"))));
            } else {
                dp.setDefaultValue(convertToType(dp, dpMeta.get("DEFAULT")));
            }
            dp.setValue(dp.getDefaultValue());
            channel.addDatapoint(dp);
            datapoints.add(dp);
        }

        return null;
    }

}
