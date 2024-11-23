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

import static org.openhab.binding.vesync.internal.dto.requests.VeSyncProtocolConstants.MODE_AUTO;
import static org.openhab.binding.vesync.internal.dto.requests.VeSyncProtocolConstants.MODE_AUTO_HUMIDITY;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VeSyncDeviceHumidifierMetadata} class contains the definition for the control of humidifer device types.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class VeSyncDeviceHumidifierMetadata extends VeSyncDeviceMetadata {

    public VeSyncDeviceHumidifierMetadata(final int v2version, final String deviceFamilyName,
            final List<String> deviceGenerations, final List<String> nonStandardIds, final List<String> fanModes,
            final int targetMinMistLevel, final int targetMaxMistLevel, final int targetMinWarmMistLevel,
            final int targetMaxWarmMistLevel, final boolean remapsAutoToHumidity, List<String> nightLightModes) {
        super(deviceFamilyName, deviceGenerations, nonStandardIds);
        this.fanModes = fanModes;
        this.targetMinMistLevel = targetMinMistLevel;
        this.targetMaxMistLevel = targetMaxMistLevel;
        this.targetMinWarmMistLevel = targetMinWarmMistLevel;
        this.targetMaxWarmMistLevel = targetMaxWarmMistLevel;
        this.remapsAutoToHumidity = remapsAutoToHumidity;
        this.nightLightModes = nightLightModes;
        this.protocolV2Version = v2version;
    }

    public final int protocolV2Version;

    /**
     * The fan modes supported by this generation of device
     */
    public final List<String> fanModes;

    /**
     * The minimum target mist level supported
     */
    public final int targetMinMistLevel;

    /**
     * The maximum target mist level supported
     */
    public final int targetMaxMistLevel;

    public final boolean isTargetMistLevelSupported(final int target) {
        return target >= targetMinMistLevel && target <= targetMaxMistLevel;
    }

    /**
     * The minimum target mist level supported
     */
    public final int targetMinWarmMistLevel;

    /**
     * The maximum target mist level supported
     */
    public final int targetMaxWarmMistLevel;

    public final boolean isTargetWramMistLevelSupported(final int target) {
        return target >= targetMinWarmMistLevel && target <= targetMaxWarmMistLevel;
    }

    /**
     * Stores whether auto in openhab is humidity mode in the protocol
     */
    public final boolean remapsAutoToHumidity;

    public String getProtocolMode(final String mode) {
        if (!remapsAutoToHumidity) {
            return mode;
        } else {
            if (MODE_AUTO.equals(mode)) {
                return MODE_AUTO_HUMIDITY;
            }
            return mode;
        }
    }

    public List<String> nightLightModes;
}
