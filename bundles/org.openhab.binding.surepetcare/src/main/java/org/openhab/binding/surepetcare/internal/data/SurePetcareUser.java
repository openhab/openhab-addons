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
    // "first_name":"Rene",
    // "last_name":"Scherer",
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

    private String email_address;
    private String first_name;
    private String last_name;
    private Integer country_id;
    private Integer language_id;
    // and various others not yet mapped

    public String getEmail_address() {
        return email_address;
    }

    public void setEmail_address(String email_address) {
        this.email_address = email_address;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public Integer getCountry_id() {
        return country_id;
    }

    public void setCountry_id(Integer country_id) {
        this.country_id = country_id;
    }

    public Integer getLanguage_id() {
        return language_id;
    }

    public void setLanguage_id(Integer language_id) {
        this.language_id = language_id;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", email_address=" + email_address + ", first_name=" + first_name + ", last_name="
                + last_name + "]";
    }

}
