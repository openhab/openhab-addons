/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
