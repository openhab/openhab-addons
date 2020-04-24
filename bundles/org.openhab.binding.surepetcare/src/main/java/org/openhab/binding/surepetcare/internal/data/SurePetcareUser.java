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
package org.openhab.binding.surepetcare.internal.data;

/**
 * The {@link SurePetcareUser} is the Java class used to represent a Sure Petcare API user. It's used to deserialize
 * JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareUser extends SurePetcareBaseObject {

    // "user":{
    // "id":23465,
    // "email_address":"rs@gugus.com",
    // "first_name":"Admin",
    // "last_name":"User",
    // "country_id":77,
    // "language_id":37,
    // "marketing_opt_in":false,
    // "terms_accepted":true,
    // "weight_units":0,
    // "time_format":0,
    // "version":"MA==",
    // "created_at":"2019-09-02T08:20:03+00:00",
    // "updated_at":"2019-09-02T08:20:03+00:00",
    // "notifications":{
    // "device_status":true,
    // "animal_movement":true,
    // "intruder_movements":true,
    // "new_device_pet":true,
    // "household_management":true,
    // "photos":true,
    // "low_battery":true,
    // "curfew":true,
    // "feeding_activity":true
    // }
    // }

    private String emailAddress;
    private String firstName;
    private String lastName;
    private Integer countryId;
    private Integer languageId;
    // and various others not yet mapped

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", email_address=" + emailAddress + ", first_name=" + firstName + ", last_name="
                + lastName + "]";
    }

}
