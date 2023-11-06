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
package org.openhab.binding.solarwatt.internal.domain.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarwatt.internal.domain.dto.DeviceDTO;

/**
 * Deprecated class.
 *
 * Was superseded by {@link ScheduleApp}.
 *
 * This fields have been identified to exist:
 * com.solarwatt.devices.sem.SmartEnergyManagement=[
 * FractionFeedInLimit,
 * FractionFeedInTestLimit,
 * ModeManagement,
 * ModeActive,
 * IdConsumerSettingsMap,
 * IdConsumerSelectionList,
 * ModeTestActive,
 * PowerInSwitchedOnDevices,
 * MigratedToScheduleApp,
 * IdDevicesMap,
 * IdConsumerManagementIntervalsMap,
 * IdManageableDeviceInfo
 * ]
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class SmartEnergyManagement extends Device {
    public static final String SOLAR_WATT_CLASSNAME = "com.solarwatt.devices.sem.SmartEnergyManagement";

    public SmartEnergyManagement(DeviceDTO deviceDTO) {
        super(deviceDTO);
    }

    @Override
    protected String getSolarWattLabel() {
        return "SmartEnergyManagement";
    }
}
