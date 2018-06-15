/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
