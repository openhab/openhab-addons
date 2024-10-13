/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.vesync.internal.handlers;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VeSyncDevicePurifierMetadata} class contains the definition for the control of humidifer device types.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class VeSyncDevicePurifierMetadata extends VeSyncDeviceMetadata {

    public VeSyncDevicePurifierMetadata(final int v2version, final String deviceFamilyName,
            final List<String> deviceGenerations, final List<String> nonStandardIds, final List<String> fanModes,
            final int minFanSpeed, final int maxFanSpeed, final List<String> nightLightModes) {
        super(deviceFamilyName, deviceGenerations, nonStandardIds);
        this.fanModes = fanModes;
        this.minFanSpeed = minFanSpeed;
        this.maxFanSpeed = maxFanSpeed;
        this.nightLightModes = nightLightModes;
        this.protocolV2Version = v2version;
    }

    public final int protocolV2Version;

    /**
     * The fan modes supported by this generation of device
     */
    public final List<String> fanModes;

    /**
     * The minimum fan speed supported
     */
    public final int minFanSpeed;

    /**
     * The maximum fan speed supported
     */
    public final int maxFanSpeed;

    /**
     * The night light supported by this generation of device
     */
    public final List<String> nightLightModes;

    public final boolean isFanModeSupported(final String fanMode) {
        return fanModes.contains(fanMode);
    }

    public final boolean isFanSpeedSupported(final int speed) {
        return speed >= minFanSpeed && speed <= maxFanSpeed;
    }

    public final boolean isNightLightModeSupported(final String nightLightMode) {
        return nightLightModes.contains(nightLightMode);
    }
}
