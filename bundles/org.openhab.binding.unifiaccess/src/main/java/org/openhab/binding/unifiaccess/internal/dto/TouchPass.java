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
 * Touch Pass model (Section 6.11 "Touch Pass Schemas").
 *
 *
 * @author Dan Cunningham - Initial contribution
 */
public class TouchPass {
    public Object activatedAt;
    public String cardId;
    public String cardName;
    public Object expiredAt;
    public String id;
    public String lastActivity;
    public Status status;
    public String userAvatar;
    public String userEmail;
    public String userId;
    public String userName;
    public UserStatus userStatus;
    public List<Bundle> bundles;

    public enum Status {
        @SerializedName("ACTIVE")
        ACTIVE,
        @SerializedName("PENDING")
        PENDING,
        @SerializedName("SUSPENDED")
        SUSPENDED,
        @SerializedName("INACTIVE")
        INACTIVE,
        @SerializedName("EXPIRED")
        EXPIRED
    }

    public enum UserStatus {
        @SerializedName("ACTIVE")
        ACTIVE,
        @SerializedName("PENDING")
        PENDING,
        @SerializedName("UNLINK")
        UNLINK
    }

    public static class Bundle {
        public String bundleId;
        public BundleStatus bundleStatus;
        public String deviceId;
        public String deviceName;
        public Integer deviceType;
        public Source source;
    }

    public enum BundleStatus {
        @SerializedName("ACTIVE")
        ACTIVE,
        @SerializedName("SUSPENDED")
        SUSPENDED
    }

    public enum Source {
        @SerializedName("google")
        GOOGLE,
        @SerializedName("apple")
        APPLE
    }

    public Instant lastActivityInstant() {
        return UaTime.parseInstant(lastActivity);
    }

    public boolean isLinked() {
        return userStatus != null && userStatus != UserStatus.UNLINK;
    }
}
