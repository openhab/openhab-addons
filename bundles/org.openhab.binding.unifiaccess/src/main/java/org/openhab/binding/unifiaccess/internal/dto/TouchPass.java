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
 * <p>
 * Represents a mobile credential with optional device bundles and user linkage.
 * </p>
 *
 * @author Dan Cunningham - Initial contribution
 */
public class TouchPass {
    @SerializedName("activated_at")
    public Object activatedAt;
    @SerializedName("card_id")
    public String cardId;
    @SerializedName("card_name")
    public String cardName;
    @SerializedName("expired_at")
    public Object expiredAt;
    public String id;
    @SerializedName("last_activity")
    public String lastActivity;
    public Status status;
    @SerializedName("user_avatar")
    public String userAvatar;
    @SerializedName("user_email")
    public String userEmail;
    @SerializedName("user_id")
    public String userId;
    @SerializedName("user_name")
    public String userName;
    @SerializedName("user_status")
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
        @SerializedName("bundle_id")
        public String bundleId;
        @SerializedName("bundle_status")
        public BundleStatus bundleStatus;
        @SerializedName("device_id")
        public String deviceId;
        @SerializedName("device_name")
        public String deviceName;
        @SerializedName("device_type")
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
