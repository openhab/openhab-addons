/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * The {@link ZWayZAutomationDeviceConfiguration} class defines the model for a Z-Way device configuration.
 *
 * @author Patrick Hecker - Initial contribution
 */
public class ZWayZAutomationDeviceConfiguration {
    private String deviceId;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(DEVICE_CONFIG_VIRTUAL_DEVICE_ID, this.getDeviceId()).toString();
    }
}
