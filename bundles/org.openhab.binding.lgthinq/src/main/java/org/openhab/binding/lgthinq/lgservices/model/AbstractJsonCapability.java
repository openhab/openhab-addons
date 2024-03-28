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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AbstractJsonCapability}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractJsonCapability implements CapabilityDefinition {
    // default result format
    protected DeviceTypes deviceType = DeviceTypes.UNKNOWN;
    protected LGAPIVerion version = LGAPIVerion.UNDEF;
    private MonitoringResultFormat monitoringDataFormat = MonitoringResultFormat.JSON_FORMAT;

    private List<MonitoringBinaryProtocol> monitoringBinaryProtocol = new ArrayList<>();

    @Override
    public MonitoringResultFormat getMonitoringDataFormat() {
        return monitoringDataFormat;
    }

    @Override
    public void setMonitoringDataFormat(MonitoringResultFormat monitoringDataFormat) {
        this.monitoringDataFormat = monitoringDataFormat;
    }

    @Override
    public List<MonitoringBinaryProtocol> getMonitoringBinaryProtocol() {
        return monitoringBinaryProtocol;
    }

    @Override
    public void setMonitoringBinaryProtocol(List<MonitoringBinaryProtocol> monitoringBinaryProtocol) {
        this.monitoringBinaryProtocol = monitoringBinaryProtocol;
    }

    @Override
    public void setDeviceType(DeviceTypes deviceType) {
        this.deviceType = deviceType;
    }

    @Override
    public void setDeviceVersion(LGAPIVerion version) {
        this.version = version;
    }

    @Override
    public DeviceTypes getDeviceType() {
        return deviceType;
    }

    @Override
    public LGAPIVerion getDeviceVersion() {
        return version;
    }
}
