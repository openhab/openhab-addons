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
package org.openhab.binding.emerald.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class for holding the set of parameters around the HWS.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class Login {
    public int code;
    public String message = "";

    @SerializedName("info")
    public @NonNullByDefault({}) Info info;

    public String token = "";

    public class Info {
        public String id = "";

        @SerializedName("first_name")
        public String firstName = "";

        @SerializedName("last_name")
        public String lastName = "";

        public String email = "";

        @SerializedName("phone_no")
        public String phoneNo = "";

        @SerializedName("agent_id")
        public String agentId = "";

        @SerializedName("social_type")
        public String socialType = "";

        @SerializedName("is_tc_accepted")
        public int isTcAccepted;

        public String status = "";

        @SerializedName("deleted_by")
        public String deletedBy = "";

        @SerializedName("zendesk_chat")
        public int zendeskChat;

        @SerializedName("feedback_status")
        public boolean feedbackStatus;

        @SerializedName("weekly_report")
        public boolean weeklyReport;

        public boolean subscribe;

        @SerializedName("auto_sync")
        public boolean autoSync;

        public boolean serverless;

        @SerializedName("email_notification")
        public boolean emailNotification;

        @SerializedName("push_notification")
        public boolean pushNotification;

        @SerializedName("contact_form")
        public boolean contactForm;

        @SerializedName("safelink_policy")
        public boolean safelinkPolicy;

        @SerializedName("dd_news_offer")
        public int ddNewsOffer;

        @SerializedName("created_at")
        public String createdAt = "";

        @SerializedName("installed_devices")
        public String installedDevices = "";

        @SerializedName("full_name")
        public String fullName = "";

        @SerializedName("background_sync_count")
        public int backgroundSyncCount;

        @SerializedName("pending_rating")
        public String pendingRating = "";

        @SerializedName("feedback_form")
        public boolean feedbackForm;

        @SerializedName("location_permission_type")
        public String locationPermissionType = "";
    }

    private Login() {
    }
}
