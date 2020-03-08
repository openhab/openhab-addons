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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Model for signing sin
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class PostSignInQueryModel {

    @SerializedName("APIKEY")
    @Expose
    public String aPIKEY = "";

    @SerializedName("UserName")
    @Expose
    public String userName = "";

    @SerializedName("Password")
    @Expose
    public String password = "";

    @SerializedName("CustomerId")
    @Expose
    public Integer customerId = 0;

    @SerializedName("ClientSWVersion")
    @Expose
    public Integer clientSWVersion = 0;

    /**
     * Add API-Key
     *
     * @param aPIKEY API-Key
     * @return Model
     */
    public PostSignInQueryModel withAPIKEY(String aPIKEY) {
        this.aPIKEY = aPIKEY;
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
    public PostSignInQueryModel withCustomerId(Integer customerId) {
        this.customerId = customerId;
        return this;
    }

    /**
     * Add Software Version
     *
     * @param clientSWVersion Software Version
     * @return Model
     */
    public PostSignInQueryModel withClientSWVersion(Integer clientSWVersion) {
        this.clientSWVersion = clientSWVersion;
        return this;
    }
}