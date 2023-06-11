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
package org.openhab.binding.zway.internal.config;

import static org.openhab.binding.zway.internal.ZWayBindingConstants.DEVICE_CONFIG_VIRTUAL_DEVICE_ID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ZWayZAutomationDeviceConfiguration} class defines the model for a Z-Way device configuration.
 *
 * @author Patrick Hecker - Initial contribution
 */
@NonNullByDefault
public class ZWayZAutomationDeviceConfiguration {
    private @Nullable String deviceId;

    public @Nullable String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(@Nullable String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{ " + DEVICE_CONFIG_VIRTUAL_DEVICE_ID + "=" + getDeviceId() + "}";
    }
}
