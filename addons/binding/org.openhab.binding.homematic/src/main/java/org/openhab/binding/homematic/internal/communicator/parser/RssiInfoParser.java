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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.openhab.binding.homematic.internal.model.HmRssiInfo;

/**
 * Parses a result with all rssi values of all datapoints.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class RssiInfoParser extends CommonRpcParser<Object[], List<HmRssiInfo>> {
    private HomematicConfig config;

    public RssiInfoParser(HomematicConfig config) {
        this.config = config;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<HmRssiInfo> parse(Object[] result) throws IOException {
        List<HmRssiInfo> rssiList = new ArrayList<HmRssiInfo>();
        if (result != null && result.length > 0 && result[0] instanceof Map) {
            Map<String, ?> devices = (Map<String, ?>) result[0];

            for (String sourceDevice : devices.keySet()) {
                Map<String, Object[]> targetDevices = (Map<String, Object[]>) devices.get(sourceDevice);
                if (targetDevices != null) {
                    for (String targetDevice : targetDevices.keySet()) {
                        if (targetDevice.equals(config.getGatewayInfo().getAddress())) {
                            Integer rssiDevice = getAdjustedRssiValue((Integer) targetDevices.get(targetDevice)[0]);
                            Integer rssiPeer = getAdjustedRssiValue((Integer) targetDevices.get(targetDevice)[1]);
                            HmRssiInfo rssiInfo = new HmRssiInfo(sourceDevice, rssiDevice, rssiPeer);
                            rssiList.add(rssiInfo);
                        }
                    }
                }
            }
        }
        return rssiList;
    }
}
