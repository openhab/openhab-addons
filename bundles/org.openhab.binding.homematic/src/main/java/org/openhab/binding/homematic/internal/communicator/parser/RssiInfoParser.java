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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.openhab.binding.homematic.internal.model.HmGatewayInfo;
import org.openhab.binding.homematic.internal.model.HmRssiInfo;

/**
 * Parses a result with all rssi values of all datapoints.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class RssiInfoParser extends CommonRpcParser<Object[], List<HmRssiInfo>> {
    private HomematicConfig config;

    public RssiInfoParser(HomematicConfig config) {
        this.config = config;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<HmRssiInfo> parse(Object[] result) throws IOException {
        List<HmRssiInfo> rssiList = new ArrayList<>();
        if (result.length > 0 && result[0] instanceof Map) {
            Map<String, ?> devices = (Map<String, ?>) result[0];
            HmGatewayInfo gatewayInfo = config.getGatewayInfo();
            String gatewayAddress = gatewayInfo != null ? gatewayInfo.getAddress() : null;

            for (String sourceDevice : devices.keySet()) {
                Map<String, Object[]> targetDevices = (Map<String, Object[]>) devices.get(sourceDevice);
                if (targetDevices != null) {
                    for (Map.Entry<String, Object[]> entry : targetDevices.entrySet()) {
                        if (entry.getKey().equals(gatewayAddress)) {
                            Object[] values = entry.getValue();
                            Integer rssiDevice = getAdjustedRssiValue((Integer) values[0]);
                            Integer rssiPeer = getAdjustedRssiValue((Integer) values[1]);
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
