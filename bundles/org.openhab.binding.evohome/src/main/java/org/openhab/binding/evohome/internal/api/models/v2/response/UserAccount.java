/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.evohome.internal.api.models.v2.response;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for the user account
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */
public class UserAccount {

    @SerializedName("userId")
    private String userId;

    @SerializedName("username")
    private String userName;

    @SerializedName("firstname")
    private String firstName;

    @SerializedName("lastname")
    private String lastName;

    @SerializedName("streetAddress")
    private String streetAddress;

    @SerializedName("city")
    private String city;

    @SerializedName("postcode")
    private String postCode;

    @SerializedName("country")
    private String country;

    @SerializedName("language")
    private String language;

    public String getUserId() {
        return userId;
    }
}
