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
package org.openhab.binding.miio.internal.cloud;

import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO class wraps the device list info json structure
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class CloudDeviceListDTO {

    @SerializedName("list")
    @Expose
    private List<CloudDeviceDTO> cloudDevices = null;

    public List<CloudDeviceDTO> getCloudDevices() {
        if (cloudDevices == null) {
            return Collections.emptyList();
        }
        return cloudDevices;
    }
}
