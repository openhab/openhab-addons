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
package org.openhab.binding.surepetcare.internal.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SurePetcareHousehold} is the Java class used as a DTO to represent a Sure Petcare Household.
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareHousehold extends SurePetcareBaseObject {

    public String name;

    public String shareCode;

    public Integer timezoneId;

    @SerializedName("users")
    public List<HouseholdUsers> users = null;

    @Override
    public String toString() {
        return "SurePetcareHousehold [id=" + id + ", name=" + name + "]";
    }

    public static class HouseholdUsers {
        public class User {
            @SerializedName("id")
            public Long userId;
            @SerializedName("name")
            public String userName;
        }

        public User user;
    }
}
