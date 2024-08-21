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
package org.openhab.binding.max.internal.device;

/**
 * Unsupported devices.
 *
 * @author Andreas Heil - Initial contribution
 * @author Marcel Verpaalen - OH2 update
 */

public class UnsupportedDevice extends Device {

    public UnsupportedDevice(DeviceConfiguration c) {
        super(c);
    }

    @Override
    public DeviceType getType() {
        return DeviceType.Invalid;
    }

    @Override
    public String getName() {
        return "Unsupported device";
    }
}
