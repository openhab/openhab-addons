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
package org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal;

import com.google.gson.annotations.SerializedName;

/**
 * @author Johannes Ptaszyk - Initial contribution
 */
public class PortalLoginResponse extends AbstractPortalResponse {

    @SerializedName("userId")
    private final String userId;

    @SerializedName("resource")
    private final String resource;

    @SerializedName("token")
    private final String token;

    @SerializedName("last")
    private final String last;

    public PortalLoginResponse(String result, String userId, String resource, String token, String last) {
        super(result);
        this.userId = userId;
        this.resource = resource;
        this.token = token;
        this.last = last;
    }

    public String getUserId() {
        return userId;
    }

    public String getResource() {
        return resource;
    }

    public String getToken() {
        return token;
    }

    public String getLast() {
        return last;
    }
}
