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
package org.openhab.binding.toon.internal.api;

import java.util.List;

/**
 * The {@link DeviceConfigInfo} class defines the json object as received by the api.
 *
 * @author Jorg de Jong - Initial contribution
 */
public class DeviceConfigInfo {
    private List<DeviceConfig> device;

    public List<DeviceConfig> getDevice() {
        return device;
    }

    public void setDevice(List<DeviceConfig> device) {
        this.device = device;
    }
}
