/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.ojelectronics.internal.models.userprofile;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Model for signing sin
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class PostSignInQueryModel {

    @SerializedName("APIKEY")
    public String apiKey = "";

    public String userName = "";

    public String password = "";

    public int customerId;

    public int clientSWVersion;

    /**
     * Add API-Key
     *
     * @param apiKey API-Key
     * @return Model
     */
    public PostSignInQueryModel withApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    /**
     * Add User-Name
     *
     * @param userName User-Name for API access
     * @return Model
     */
    public PostSignInQueryModel withUserName(String userName) {
        this.userName = userName;
        return this;
    }

    /**
     * Add Password
     *
     * @param password Password for API access
     * @return Model
     */
    public PostSignInQueryModel withPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Add customer ID
     *
     * @param customerId Customer Id
     * @return Model
     */
    public PostSignInQueryModel withCustomerId(int customerId) {
        this.customerId = customerId;
        return this;
    }

    /**
     * Add Software Version
     *
     * @param clientSWVersion Software Version
     * @return Model
     */
    public PostSignInQueryModel withClientSWVersion(int clientSWVersion) {
        this.clientSWVersion = clientSWVersion;
        return this;
    }
}
