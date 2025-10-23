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
 * Visitor model for UniFi Access API (Section 4.1 Schemas).
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Visitor {
    public String id;
    public String firstName;
    public String lastName;
    public String remarks;
    public String mobilePhone;
    public String email;
    public String visitorCompany;
    public Long startTime;
    public Long endTime;
    public VisitReason visitReason;
    public List<NfcCard> nfcCards;
    public PinCode pinCode;
    public String scheduleId;
    public Schedule schedule;
    public List<Resource> resources;
    public List<LicensePlate> licensePlates;

    public Instant startInstant() {
        return UaTime.fromEpochSeconds(startTime);
    }

    public Instant endInstant() {
        return UaTime.fromEpochSeconds(endTime);
    }

    public static class NfcCard {
        public String id;
        public String token;
    }

    public static class PinCode {
        public String token;
    }

    public static class Schedule {
        public String id;
        public Boolean isDefault;
        public String name;
        public String type;
        public WeekSchedule weekSchedule;
    }

    public static class WeekSchedule {
        public List<TimeRange> sunday;
        public List<TimeRange> monday;
        public List<TimeRange> tuesday;
        public List<TimeRange> wednesday;
        public List<TimeRange> thursday;
        public List<TimeRange> friday;
        public List<TimeRange> saturday;
    }

    public static class TimeRange {
        public String startTime;
        public String endTime;
    }

    public static class Resource {
        public String id;
        public String name;
        public String type;
    }

    public static class LicensePlate {
        public String id;
        public String credential;
        public String credentialType;
        public CredentialStatus credentialStatus;
    }

    public enum VisitReason {
        @SerializedName("Interview")
        INTERVIEW,
        @SerializedName("Business")
        BUSINESS,
        @SerializedName("Cooperation")
        COOPERATION,
        @SerializedName("Others")
        OTHERS
    }

    public enum CredentialStatus {
        @SerializedName("active")
        ACTIVE,
        @SerializedName("deactivate")
        DEACTIVATE
    }
}
