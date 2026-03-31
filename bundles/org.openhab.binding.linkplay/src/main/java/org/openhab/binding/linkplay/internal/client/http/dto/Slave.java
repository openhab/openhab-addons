/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linkplay.internal.client.http.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Slave device
 * 
 * @author Dan Cunningham - Initial contribution
 */
public class Slave {
    public String name;
    public String uuid;
    public String ip;
    public String version;
    @SerializedName("type")
    public String deviceType;
    public int channel;
    public int volume;
    public int mute;
    public int batteryPercent;
    public int batteryCharging;

    @Override
    public String toString() {
        return "Slave [name=" + name + ", uuid=" + uuid + ", ip=" + ip + ", version=" + version + ", deviceType="
                + deviceType + ", channel=" + channel + ", volume=" + volume + ", mute=" + mute + ", batteryPercent="
                + batteryPercent + ", batteryCharging=" + batteryCharging + "]";
    }
}
