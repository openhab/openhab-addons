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
package org.openhab.binding.linktap.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link LinkTapDeviceMetadata} class contains the definition of a devices metadata as given by a Gateway device.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class LinkTapDeviceMetadata {

    /**
     * The ID of the device as stored in the relevant Gateway Device.
     */
    public final String deviceId;

    /**
     * The human-readable name of the device as stored in the relevant Gateway Device.
     */
    public final String deviceName;

    public LinkTapDeviceMetadata(final String deviceId, final String deviceName) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
    }
}
