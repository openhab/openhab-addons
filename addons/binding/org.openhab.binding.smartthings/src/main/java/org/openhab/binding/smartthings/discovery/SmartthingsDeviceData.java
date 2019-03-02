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
package org.openhab.binding.smartthings.discovery;

/**
 * Mapping object for data returned from smartthings hub
 *
 * @author Bob Raker - Initial contribution
 *
 */

public class SmartthingsDeviceData {
    private String capability;
    private String attribute;
    private String name;
    private String id;

    SmartthingsDeviceData() {
    }

    public String getCapability() {
        return capability;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

}
