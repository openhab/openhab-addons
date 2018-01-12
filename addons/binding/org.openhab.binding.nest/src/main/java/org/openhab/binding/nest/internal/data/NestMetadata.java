/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.data;

import com.google.gson.annotations.SerializedName;

/**
 * The meta data in the data downloads from Nest.
 *
 * @author David Bennett - Initial Contribution
 */
public class NestMetadata {
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("client_version")
    private String clientVersion;

    public String getAccessToken() {
        return accessToken;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NestMetadata [accessToken=").append(accessToken).append(", clientVersion=")
                .append(clientVersion).append("]");
        return builder.toString();
    }
}
