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

import static org.openhab.binding.solarwatt.internal.SolarwattBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarwatt.internal.domain.dto.DeviceDTO;

/**
 * The energy manager itself which aggregates all other devices.
 *
 * This fields have been identified to exist:
 * com.kiwigrid.devices.em.EnergyManager=[
 * IdInterfacesMap,
 * DateCloudLastSeen,
 * IdBootLoaderVersion,
 * URLProxy,
 * IdTimezone,
 * FractionCPULoadAverageLastFifteenMinutes,
 * FractionCPULoadAverageLastFiveMinutes,
 * IdSystemImageVersion,
 * VersionPackagesMap,
 * IdNotInstalledDevicesMap,
 * StatusMonitoringMap,
 * FractionCPULoadUser,
 * VersionExtensionsMap,
 * InstallConfiguration,
 * URLCloud,
 * SettingsProxyMap,
 * IdDevicesMap,
 * SettingsMap,
 * ReasonReboots,
 * IdDriverList,
 * TimeSinceStart,
 * ExchangeDevice,
 * SettingsPrivacyMap,
 * FractionTopFiveProcessesMap,
 * StateAction,
 * FractionCPULoadAverageLastMinute,
 * SettingsNetworkMap,
 * SettingsDatetimeMap,
 * FractionCPULoadKernel,
 * FractionCPULoadTotal,
 * IdRemoteAppSet,
 * IdOwner,
 * VersionLocalApplicationsMap,
 * AddressLocation,
 * Command,
 * DriverMap,
 * DateTagCollectorWatchdogEvents,
 * LocationGeographical
 * ]
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class EnergyManager extends Device {
    public static final String SOLAR_WATT_CLASSNAME = "com.kiwigrid.devices.em.EnergyManager";

    public EnergyManager(DeviceDTO deviceDTO) {
        super(deviceDTO);
    }

    @Override
    public void update(DeviceDTO deviceDTO) {
        super.update(deviceDTO);

        this.addSecondsQuantity(CHANNEL_TIMESTAMP.getChannelName(), "SettingsDatetimeMap", ".timestamp", deviceDTO);
        this.addStringState(CHANNEL_IDTIMEZONE, deviceDTO);
        this.addPercentQuantity(CHANNEL_FRACTION_CPU_LOAD_TOTAL, deviceDTO);
        this.addPercentQuantity(CHANNEL_FRACTION_CPU_LOAD_USER, deviceDTO);
        this.addPercentQuantity(CHANNEL_FRACTION_CPU_LOAD_KERNEL, deviceDTO);
        this.addPercentQuantity(CHANNEL_FRACTION_CPU_LOAD_AVERAGE_LAST_MINUTE, deviceDTO);
        this.addPercentQuantity(CHANNEL_FRACTION_CPU_LOAD_AVERAGE_LAST_FIVE_MINUTES, deviceDTO);
        this.addPercentQuantity(CHANNEL_FRACTION_CPU_LOAD_AVERAGE_LAST_FIFTEEN_MINUTES, deviceDTO);
    }
}
