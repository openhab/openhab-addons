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
package org.openhab.binding.smartthings.internal.dto;

/**
 * Data object for smartthings state data
 *
 * @author Bob Raker - Initial contribution
 */
public class SmartthingsStateData {
    public String deviceDisplayName;
    public String capabilityAttribute;
    public String value;

    public SmartthingsStateData() {
        // These values will always be overridden when the object is initialized by GSon
        deviceDisplayName = "";
        capabilityAttribute = "";
        value = "";
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(", deviceDisplayName :").append(deviceDisplayName);
        sb.append(", capabilityAttribute :").append(capabilityAttribute);
        sb.append(", value :").append(value);
        return sb.toString();
    }
}
