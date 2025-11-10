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
 * Wlan connect state.
 * 
 * @author Dan Cunningham - Initial contribution
 */
public enum WlanConnectState {
    @SerializedName("PROCESS")
    PROCESS,
    @SerializedName("PAIRFAIL")
    PAIRFAIL,
    @SerializedName("FAIL")
    FAIL,
    @SerializedName("OK")
    OK;

    public static WlanConnectState fromString(String raw) {
        for (WlanConnectState s : values()) {
            if (s.name().equalsIgnoreCase(raw.trim())) {
                return s;
            }
        }
        return FAIL; // default fallback
    }
}
