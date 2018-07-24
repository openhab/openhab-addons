/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        return components != null ? components : Collections.<Component> emptyList();
    }
}
