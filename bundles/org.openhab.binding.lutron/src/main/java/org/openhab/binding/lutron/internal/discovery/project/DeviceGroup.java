/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.discovery.project;

import java.util.Collections;
import java.util.List;

/**
 * A group of input devices in the Lutron system.
 *
 * @author Allan Tong - Initial contribution
 */
public class DeviceGroup implements DeviceNode {
    private String name;
    private List<Device> devices;

    public String getName() {
        return name;
    }

    public List<Device> getDevices() {
        return devices != null ? devices : Collections.<Device> emptyList();
    }
}
