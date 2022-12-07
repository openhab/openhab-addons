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
package org.openhab.binding.gardena.internal.model;

import com.google.gson.annotations.SerializedName;

/**
 * Session attributes for valid Gardena JSON serialization.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class SessionAttributes {
    @SerializedName("user_id")
    private String userId;

    @SerializedName("provider")
    private String provider;

    /**
     * Returns the user id.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user id.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the provider.
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Sets the provider.
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }
}
