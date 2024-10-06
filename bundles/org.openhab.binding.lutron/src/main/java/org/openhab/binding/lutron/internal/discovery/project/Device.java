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
package org.openhab.binding.lutron.internal.discovery.project;

import java.util.Collections;
import java.util.List;

/**
 * An input device in a Lutron system such as a keypad or occupancy sensor.
 *
 * @author Allan Tong - Initial contribution
 */
public class Device implements DeviceNode {
    private String name;
    private Integer integrationId;
    private String type;
    private List<Component> components;

    public String getName() {
        return name;
    }

    public Integer getIntegrationId() {
        return integrationId;
    }

    public String getType() {
        return type;
    }

    public DeviceType getDeviceType() {
        try {
            return DeviceType.valueOf(this.type);
        } catch (Exception e) {
            return null;
        }
    }

    public List<Component> getComponents() {
        return components != null ? components : Collections.emptyList();
    }
}
