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
 * The {@link VeSyncDeviceMetadata} class contains the definition for the identification of multiple device types,
 * to a single family of devices.
 *
 * New Device Type Ids are formatted as [DeviceType][-][Device Generation][-][Device Region]
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class VeSyncDeviceMetadata {

    public VeSyncDeviceMetadata(final String deviceFamilyName, final List<String> deviceGenerations,
            final List<String> nonStandardIds) {
        this.deviceFamilyName = deviceFamilyName;
        this.deviceGenerations = deviceGenerations;
        this.nonStandardIds = nonStandardIds;
    }

    /**
     * The name of the family the set of ID's represents.
     *
     */
    public final String deviceFamilyName;

    /**
     * The version id, that represents the specific model of the device
     */
    public final List<String> deviceGenerations;

    /**
     * Device Types not following the standard 3 segment convention
     */
    public final List<String> nonStandardIds;

    public boolean deviceTypeIdMatches(final String[] deviceTypeSegments) {
        return (deviceTypeSegments.length == 3 && deviceGenerations.contains(deviceTypeSegments[1]));
    }

    public String getDeviceFamilyName() {
        return deviceFamilyName;
    }
}
