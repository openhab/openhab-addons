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
package org.openhab.binding.sleepiq.internal.api.dto;

import org.openhab.binding.sleepiq.internal.api.enums.Side;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Sleeper} holds the information about the sleeper that's
 * associated with a bed side.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class Sleeper {
    private String firstName;
    private Boolean active;
    private Boolean emailValidated;
    @SerializedName("isChild")
    private Boolean child;
    private String bedId;
    private String birthYear;
    private String zipCode;
    private String timezone;
    @SerializedName("isMale")
    private Boolean male;
    private Integer weight; // lbs
    private String duration;
    private String sleeperId;
    private Integer height; // inches
    private Long licenseVersion;
    private String username;
    private Integer birthMonth; // 0-based; 12 means not entered?
    private Integer sleepGoal;
    @SerializedName("isAccountOwner")
    private Boolean accountOwner;
    private String accountId;
    private String email;
    private String avatar;
    private String lastLogin; // should be ZonedDateTime but provider passes string "null" when missing
    private Side side; // 0=left; 1=right

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Sleeper withFirstName(String firstName) {
        setFirstName(firstName);
        return this;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Sleeper withActive(Boolean active) {
        setActive(active);
        return this;
    }

    public Boolean isEmailValidated() {
        return emailValidated;
    }

    public void setEmailValidated(Boolean emailValidated) {
        this.emailValidated = emailValidated;
    }

    public Sleeper withEmailValidated(Boolean emailValidated) {
        setEmailValidated(emailValidated);
        return this;
    }

    public Boolean isChild() {
        return child;
    }

    public void setChild(Boolean child) {
        this.child = child;
    }

    public Sleeper withChild(Boolean child) {
        setChild(child);
        return this;
    }

    public String getBedId() {
        return bedId;
    }

    public void setBedId(String bedId) {
        this.bedId = bedId;
    }

    public Sleeper withBedId(String bedId) {
        setBedId(bedId);
        return this;
    }

    public String getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(String birthYear) {
        this.birthYear = birthYear;
    }

    public Sleeper withBirthYear(String birthYear) {
        setBirthYear(birthYear);
        return this;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Sleeper withZipCode(String zipCode) {
        setZipCode(zipCode);
        return this;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Sleeper withTimezone(String timezone) {
        setTimezone(timezone);
        return this;
    }

    public Boolean isMale() {
        return male;
    }

    public void setMale(Boolean male) {
        this.male = male;
    }

    public Sleeper withMale(Boolean male) {
        setMale(male);
        return this;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Sleeper withWeight(Integer weight) {
        setWeight(weight);
        return this;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Sleeper withDuration(String duration) {
        setDuration(duration);
        return this;
    }

    public String getSleeperId() {
        return sleeperId;
    }

    public void setSleeperId(String sleeperId) {
        this.sleeperId = sleeperId;
    }

    public Sleeper withSleeperId(String sleeperId) {
        setSleeperId(sleeperId);
        return this;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Sleeper withHeight(Integer height) {
        setHeight(height);
        return this;
    }

    public Long getLicenseVersion() {
        return licenseVersion;
    }

    public void setLicenseVersion(Long licenseVersion) {
        this.licenseVersion = licenseVersion;
    }

    public Sleeper withLicenseVersion(Long licenseVersion) {
        setLicenseVersion(licenseVersion);
        return this;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Sleeper withUsername(String username) {
        setUsername(username);
        return this;
    }

    public Integer getBirthMonth() {
        return birthMonth;
    }

    public void setBirthMonth(Integer birthMonth) {
        this.birthMonth = birthMonth;
    }

    public Sleeper withBirthMonth(Integer birthMonth) {
        setBirthMonth(birthMonth);
        return this;
    }

    public Integer getSleepGoal() {
        return sleepGoal;
    }

    public void setSleepGoal(Integer sleepGoal) {
        this.sleepGoal = sleepGoal;
    }

    public Sleeper withSleepGoal(Integer sleepGoal) {
        setSleepGoal(sleepGoal);
        return this;
    }

    public Boolean isAccountOwner() {
        return accountOwner;
    }

    public void setAccountOwner(Boolean accountOwner) {
        this.accountOwner = accountOwner;
    }

    public Sleeper withAccountOwner(Boolean accountOwner) {
        setAccountOwner(accountOwner);
        return this;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Sleeper withAccountId(String accountId) {
        setAccountId(accountId);
        return this;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Sleeper withEmail(String email) {
        setEmail(email);
        return this;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Sleeper withAvatar(String avatar) {
        setAvatar(avatar);
        return this;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Sleeper withLastLogin(String lastLogin) {
        setLastLogin(lastLogin);
        return this;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public Sleeper withSide(Side side) {
        setSide(side);
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sleeperId == null) ? 0 : sleeperId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Sleeper)) {
            return false;
        }
        Sleeper other = (Sleeper) obj;
        if (sleeperId == null) {
            if (other.sleeperId != null) {
                return false;
            }
        } else if (!sleeperId.equals(other.sleeperId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Sleeper [firstName=");
        builder.append(firstName);
        builder.append(", active=");
        builder.append(active);
        builder.append(", emailValidated=");
        builder.append(emailValidated);
        builder.append(", child=");
        builder.append(child);
        builder.append(", bedId=");
        builder.append(bedId);
        builder.append(", birthYear=");
        builder.append(birthYear);
        builder.append(", zipCode=");
        builder.append(zipCode);
        builder.append(", timezone=");
        builder.append(timezone);
        builder.append(", male=");
        builder.append(male);
        builder.append(", weight=");
        builder.append(weight);
        builder.append(", duration=");
        builder.append(duration);
        builder.append(", sleeperId=");
        builder.append(sleeperId);
        builder.append(", height=");
        builder.append(height);
        builder.append(", licenseVersion=");
        builder.append(licenseVersion);
        builder.append(", username=");
        builder.append(username);
        builder.append(", birthMonth=");
        builder.append(birthMonth);
        builder.append(", sleepGoal=");
        builder.append(sleepGoal);
        builder.append(", accountOwner=");
        builder.append(accountOwner);
        builder.append(", accountId=");
        builder.append(accountId);
        builder.append(", email=");
        builder.append(email);
        builder.append(", avatar=");
        builder.append(avatar);
        builder.append(", lastLogin=");
        builder.append(lastLogin);
        builder.append(", side=");
        builder.append(side);
        builder.append("]");
        return builder.toString();
    }
}
