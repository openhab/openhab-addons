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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CapabilityDefinition}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface CapabilityDefinition {
    MonitoringResultFormat getMonitoringDataFormat();

    void setMonitoringDataFormat(MonitoringResultFormat monitoringDataFormat);

    List<MonitoringBinaryProtocol> getMonitoringBinaryProtocol();

    void setMonitoringBinaryProtocol(List<MonitoringBinaryProtocol> monitoringBinaryProtocol);

    DeviceTypes getDeviceType();

    LGAPIVerion getDeviceVersion();

    void setDeviceType(DeviceTypes deviceType);

    void setDeviceVersion(LGAPIVerion version);
}
