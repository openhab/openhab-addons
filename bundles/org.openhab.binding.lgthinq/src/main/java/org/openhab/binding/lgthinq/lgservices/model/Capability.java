/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link Capability}
 *
 * @author Nemer Daud - Initial contribution
 */
public abstract class Capability {
    // default result format
    private MonitoringResultFormat monitoringDataFormat = MonitoringResultFormat.JSON_FORMAT;

    private List<MonitoringBinaryProtocol> monitoringBinaryProtocol = new ArrayList<>();

    public MonitoringResultFormat getMonitoringDataFormat() {
        return monitoringDataFormat;
    }

    public void setMonitoringDataFormat(MonitoringResultFormat monitoringDataFormat) {
        this.monitoringDataFormat = monitoringDataFormat;
    }

    public List<MonitoringBinaryProtocol> getMonitoringBinaryProtocol() {
        return monitoringBinaryProtocol;
    }

    public void setMonitoringBinaryProtocol(List<MonitoringBinaryProtocol> monitoringBinaryProtocol) {
        this.monitoringBinaryProtocol = monitoringBinaryProtocol;
    }
}
