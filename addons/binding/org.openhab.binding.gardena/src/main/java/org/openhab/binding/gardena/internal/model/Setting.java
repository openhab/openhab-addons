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
package org.openhab.binding.gardena.internal.model;

/**
 * Represents a Gardena setting.
 *
 * @author Gerhard Riegler - Initial contribution
 */

public class Setting {
    private String name;
    private String id;
    private Object value;
    private transient Device device;

    /**
     * Returns the name of the setting.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the id of the setting.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the value of the setting.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the name of the setting.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Returns the device of the setting.
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Sets the name of the setting.
     */
    public void setDevice(Device device) {
        this.device = device;
    }
}
