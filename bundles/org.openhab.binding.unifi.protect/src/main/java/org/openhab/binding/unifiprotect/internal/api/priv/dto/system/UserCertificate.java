/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.unifiprotect.internal.api.priv.dto.system;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * User Certificate DTO
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UserCertificate {

    public static class Subject {
        @SerializedName("CN")
        public @Nullable String cn;

        @SerializedName("C")
        public @Nullable String c;

        @SerializedName("O")
        public @Nullable String o;
    }

    public static class Issuer {
        @SerializedName("C")
        public @Nullable String c;

        @SerializedName("O")
        public @Nullable String o;

        @SerializedName("CN")
        public @Nullable String cn;
    }

    public static class SubjectAltName {
        @SerializedName("DNS")
        public @Nullable List<String> dns;
    }

    public @Nullable String id;
    public @Nullable String name;
    public @Nullable Integer version;

    @SerializedName("serial_number")
    public @Nullable String serialNumber;

    public @Nullable String fingerprint;
    public @Nullable Subject subject;
    public @Nullable Issuer issuer;

    @SerializedName("subject_alt_name")
    public @Nullable SubjectAltName subjectAltName;

    @SerializedName("valid_from")
    public @Nullable String validFrom;

    @SerializedName("valid_to")
    public @Nullable String validTo;

    @SerializedName("created_at")
    public @Nullable String createdAt;

    @SerializedName("updated_at")
    public @Nullable String updatedAt;

    public @Nullable Boolean active;
}
