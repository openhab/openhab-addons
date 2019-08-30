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
package org.openhab.binding.gpstracker.internal.message.life360;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link MembersItem} is a Life360 message POJO
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class MembersItem {

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("loginEmail")
    private String loginEmail;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("loginPhone")
    private String loginPhone;

    @SerializedName("location")
    private Location location;

    @SerializedName("id")
    private String id;

    private List<PlacesItem> places = new ArrayList<>();

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLoginEmail(String loginEmail) {
        this.loginEmail = loginEmail;
    }

    public String getLoginEmail() {
        return loginEmail;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setLoginPhone(String loginPhone) {
        this.loginPhone = loginPhone;
    }

    public String getLoginPhone() {
        return loginPhone;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return
                "MembersItem{" +
                        "lastName = '" + lastName + '\'' +
                        ",loginEmail = '" + loginEmail + '\'' +
                        ",firstName = '" + firstName + '\'' +
                        ",loginPhone = '" + loginPhone + '\'' +
                        ",location = '" + location + '\'' +
                        ",id = '" + id + '\'' +
                        "}";
    }

    public void addPlace(PlacesItem p) {
        places.add(p);
    }

    public List<PlacesItem> getPlaces() {
        return places;
    }
}
