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
 * Represents a Gardena session.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class Session {

    private String token;
    private long created;

    @SerializedName(value = "user_id")
    private String userId;

    public Session() {
        this.created = System.currentTimeMillis();
    }

    /**
     * Returns the token of the session.
     */
    public String getToken() {
        return token;
    }

    /**
     * Returns the userId of the session.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the creation timestamp of the session.
     */
    public long getCreated() {
        return created;
    }

}
