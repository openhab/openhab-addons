/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.neato.internal.classes;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link BeehiveAuthenticcation} is the internal class for handling authentication.
 *
 * @author Patrik Wimnell - Initial contribution
 */
public class BeehiveAuthentication {

    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("current_time")
    private String currentTime;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }
}
