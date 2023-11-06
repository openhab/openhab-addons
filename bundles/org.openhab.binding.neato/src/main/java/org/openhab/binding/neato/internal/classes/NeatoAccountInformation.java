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
package org.openhab.binding.neato.internal.classes;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link NeatoAccountInformation} is the internal class for the neato web service account and information.
 *
 * @author Patrik Wimnell - Initial contribution
 */
public class NeatoAccountInformation {

    private String email;
    private Object firstName;
    private Object lastName;
    private String locale;
    @SerializedName("country_code")
    private String countryCode;
    private Boolean developer;
    private Boolean newsletter;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("verified_at")
    private String verifiedAt;
    private List<Robot> robots = null;
    @SerializedName("recent_firmwares")
    private RecentFirmwares recentFirmwares;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Object getFirstName() {
        return firstName;
    }

    public void setFirstName(Object firstName) {
        this.firstName = firstName;
    }

    public Object getLastName() {
        return lastName;
    }

    public void setLastName(Object lastName) {
        this.lastName = lastName;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Boolean getDeveloper() {
        return developer;
    }

    public void setDeveloper(Boolean developer) {
        this.developer = developer;
    }

    public Boolean getNewsletter() {
        return newsletter;
    }

    public void setNewsletter(Boolean newsletter) {
        this.newsletter = newsletter;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(String verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public List<Robot> getRobots() {
        return robots;
    }

    public void setRobots(List<Robot> robots) {
        this.robots = robots;
    }

    public RecentFirmwares getRecentFirmwares() {
        return recentFirmwares;
    }

    public void setRecentFirmwares(RecentFirmwares recentFirmwares) {
        this.recentFirmwares = recentFirmwares;
    }
}
