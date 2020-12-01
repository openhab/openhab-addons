/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class represents the current device mode and is used for deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class DeviceMode {

    /** Whether the device is on or not */
    private boolean isOn;

    /**
     * Constructor used for deserialization only
     */
    public DeviceMode() {
    }

    /**
     * Checks if the device is on or not
     *
     * @return true, if on - false otherwise
     */
    public boolean isOn() {
        return isOn;
    }

    @Override
    public String toString() {
        return "DeviceMode [isOn=" + isOn + "]";
    }
}
