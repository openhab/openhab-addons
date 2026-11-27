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
package org.openhab.binding.semsportal.internal.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * POJO containing the response to the request for the status of the devices of a power station. Only the inverters
 * are of interest.
 *
 * @author Julio Vilmar Gesser - Initial contribution
 */
public class InverterStatusResponse extends BaseResponse {
    private static final String INVERTER = "INVERTER";

    private Devices data;

    /**
     * @return the inverters, in the order of the response. Empty if the station has none (or does not exist: the
     *         portal does not complain about that here).
     */
    public List<Inverter> getInverters() {
        List<Inverter> inverters = new ArrayList<>();
        if (data != null && data.deviceDetailList != null) {
            for (DeviceDetail device : data.deviceDetailList) {
                if (INVERTER.equals(device.deviceType) && device.statusDetailList != null) {
                    for (StatusDetail detail : device.statusDetailList) {
                        if (detail.snList != null) {
                            for (String serialNumber : detail.snList) {
                                Model info = detail.detailMap == null ? null : detail.detailMap.get(serialNumber);
                                inverters.add(
                                        new Inverter(serialNumber, info == null ? null : info.model, detail.status));
                            }
                        }
                    }
                }
            }
        }
        return inverters;
    }

    private static class Devices {
        private List<DeviceDetail> deviceDetailList;
    }

    private static class DeviceDetail {
        private String deviceType;
        private List<StatusDetail> statusDetailList;
    }

    private static class StatusDetail {
        private int status;
        private List<String> snList;
        private Map<String, Model> detailMap;
    }

    private static class Model {
        private String model;
    }
}
