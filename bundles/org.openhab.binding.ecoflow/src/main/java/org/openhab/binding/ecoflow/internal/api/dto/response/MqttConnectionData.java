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

package org.openhab.binding.ecoflow.internal.api.dto.response;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class MqttConnectionData {
    @SerializedName("certificateAccount")
    public final String userName;
    @SerializedName("certificatePassword")
    public final String password;
    @SerializedName("url")
    public final String host;
    public final int port;
    @SerializedName("protocol")
    public final String scheme;

    MqttConnectionData(String userName, String password, String host, int port, String scheme) {
        this.userName = userName;
        this.password = password;
        this.host = host;
        this.port = port;
        this.scheme = scheme;
    }
}
