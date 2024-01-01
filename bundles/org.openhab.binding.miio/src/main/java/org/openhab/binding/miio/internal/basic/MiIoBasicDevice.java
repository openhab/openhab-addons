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
package org.openhab.binding.miio.internal.basic;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Mapping devices from json
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiIoBasicDevice {

    @SerializedName("deviceMapping")
    @Expose
    private DeviceMapping deviceMapping = new DeviceMapping();

    public DeviceMapping getDevice() {
        return deviceMapping;
    }

    public void setDevice(DeviceMapping deviceMapping) {
        this.deviceMapping = deviceMapping;
    }
}
