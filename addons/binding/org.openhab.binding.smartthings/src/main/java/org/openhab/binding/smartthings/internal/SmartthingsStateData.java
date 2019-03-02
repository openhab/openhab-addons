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
package org.openhab.binding.smartthings.internal;

/**
 * Data object for smartthings state data
 *
 * @author Bob Raker - Initial contribution
 *
 */
public class SmartthingsStateData {
    private String deviceDisplayName;
    private String capabilityAttribute;
    private String value;
    private long hubTime;
    private long openHabStartTime;
    private long hubEndTime;

    SmartthingsStateData() {
    }

    public String getDeviceDisplayName() {
        return deviceDisplayName;
    }

    public String getCapabilityAttribute() {
        return capabilityAttribute;
    }

    public String getValue() {
        return value;
    }

    public long getHubTime() {
        return hubTime;
    }

    public long getOpenHabStartTime() {
        return openHabStartTime;
    }

    public long getHubEndTime() {
        return hubEndTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("State message: display Name:\"").append(deviceDisplayName);
        sb.append("\", attribute: \"").append(capabilityAttribute);
        sb.append("\", value: \"").append(value).append("\"");
        sb.append("\", hubTime: \"").append(hubTime).append("\"");
        sb.append("\", hpenHabStartTime: \"").append(openHabStartTime).append("\"");
        sb.append("\", hubEndTime: \"").append(hubEndTime).append("\"");
        return sb.toString();
    }
}
