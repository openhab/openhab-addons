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
package org.openhab.binding.vesync.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link VeSyncDeviceConfiguration} class contains fields mapping the configuration parameters for a VeSync
 * device's configuration.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class VeSyncDeviceConfiguration {

    /**
     * The clear text device name as reported by the API.
     */
    @Nullable
    public String deviceName;

    /**
     * The mac address of the device as reported by the API.
     */
    @Nullable
    public String macId;
}
