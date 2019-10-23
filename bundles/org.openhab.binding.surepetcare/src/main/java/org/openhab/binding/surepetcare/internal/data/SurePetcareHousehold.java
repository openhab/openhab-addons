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

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SurePetcareHousehold} is the Java class used as a DTO to represent a Sure Petcare Household.
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareHousehold extends SurePetcareBaseObject {

    public class HouseholdUsers {
        public class User {
            @SerializedName("id")
            private Integer userId;
            @SerializedName("name")
            private String userName;

            public Integer getUserId() {
                return userId;
            }

            public void setUserId(Integer userId) {
                this.userId = userId;
            }

            public String getUserName() {
                return userName;
            }

            public void setUserName(String userName) {
                this.userName = userName;
            }
        }

        @SerializedName("user")
        private User user;

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

    private String name;
    private String shareCode;
    private Integer timezoneId;
    @SerializedName("users")
    private List<HouseholdUsers> householdUsers = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShareCode() {
        return shareCode;
    }

    public void setShareCode(String shareCode) {
        this.shareCode = shareCode;
    }

    public Integer getTimezoneId() {
        return timezoneId;
    }

    public void setTimezoneId(Integer timezoneId) {
        this.timezoneId = timezoneId;
    }

    public List<HouseholdUsers> getHouseholdUsers() {
        return householdUsers;
    }

    public void setHouseholdUsers(List<HouseholdUsers> householdUsers) {
        this.householdUsers = householdUsers;
    }

    @Override
    public String toString() {
        return "SurePetcareHousehold [id=" + id + ", name=" + name + "]";
    }

}
