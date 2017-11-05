/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neato.internal.classes;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link NeatoAccountInformation} is the internal class for the neato web service account and information.
 *
 * @author Patrik Wimnell - Initial contribution
 */
public class NeatoAccountInformation {

    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("first_name")
    @Expose
    private Object firstName;
    @SerializedName("last_name")
    @Expose
    private Object lastName;
    @SerializedName("locale")
    @Expose
    private String locale;
    @SerializedName("country_code")
    @Expose
    private String countryCode;
    @SerializedName("developer")
    @Expose
    private Boolean developer;
    @SerializedName("newsletter")
    @Expose
    private Boolean newsletter;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("verified_at")
    @Expose
    private String verifiedAt;
    @SerializedName("robots")
    @Expose
    private List<Robot> robots = null;
    @SerializedName("recent_firmwares")
    @Expose
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
