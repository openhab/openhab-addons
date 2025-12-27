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
 * Source input mode enum.
 * 
 * @author Dan Cunningham - Initial contribution
 */
public enum SourceInputMode {
    @SerializedName("line-in")
    LINE_IN("line-in"),
    @SerializedName("bluetooth")
    BLUETOOTH("bluetooth"),
    @SerializedName("optical")
    OPTICAL("optical"),
    @SerializedName("udisk")
    UDISK("udisk"),
    @SerializedName("PCUSB")
    PCUSB("PCUSB"),
    @SerializedName("wifi")
    WIFI("wifi"),
    @SerializedName("HDMI")
    HDMI("HDMI");

    private final String value;

    SourceInputMode(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
