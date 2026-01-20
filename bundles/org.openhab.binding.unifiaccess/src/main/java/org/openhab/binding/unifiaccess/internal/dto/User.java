/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.unifiaccess.internal.dto;

import java.time.Instant;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * UniFi Access User DTO.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class User {
    public String id;
    public String firstName;
    public String lastName;
    public String fullName;
    public String alias;
    public String userEmail;
    public String emailStatus;
    public String phone;
    public String employeeNumber;
    public Long onboardTime;
    public List<NfcCard> nfcCards;
    public List<LicensePlate> licensePlates;
    public PinCode pinCode;
    public List<String> accessPolicyIds;
    public List<AccessPolicy> accessPolicies;
    public Status status;
    public TouchPass touchPass;

    public Instant onboardInstant() {
        return UaTime.fromEpochSeconds(onboardTime);
    }

    public enum Status {
        @SerializedName("ACTIVE")
        ACTIVE,
        @SerializedName("PENDING")
        PENDING,
        @SerializedName("DEACTIVATED")
        DEACTIVATED
    }

    public static class NfcCard {
        public String id;
        public String token;
        public String type;
    }

    public static class LicensePlate {
        public String id;
        public String credential;
        public String credentialType;
        public String credentialStatus;

        public enum LicenseStatus {
            @SerializedName("active")
            ACTIVE,
            @SerializedName("deactivate")
            DEACTIVATE
        }
    }

    public static class PinCode {
        public String token;
    }

    public static class AccessPolicy {
        public String id;
        public String name;
        public List<Resource> resources;
        public String scheduleId;

        public static class Resource {
            public String id;
            public String type;
        }
    }
}
