/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.jsons;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link JsonActivities} encapsulate the GSON data of the push command for push activity
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonActivities {

    public @Nullable List<Activity> activities;

    public static class Activity {
        public @Nullable String activityStatus;
        public @Nullable Long creationTimestamp;
        public @Nullable String description;
        public @Nullable Object domainAttributes;
        public @Nullable Object domainType;
        public @Nullable Object feedbackAttributes;
        public @Nullable String id;
        public @Nullable String intentType;
        public @Nullable String providerInfoDescription;
        public @Nullable String registeredCustomerId;
        public @Nullable Object sourceActiveUsers;
        public @Nullable List<SourceDeviceId> sourceDeviceIds;
        public @Nullable String utteranceId;
        public @Nullable Long version;

        public List<SourceDeviceId> getSourceDeviceIds() {
            return Objects.requireNonNullElse(sourceDeviceIds, List.of());
        }

        public static class SourceDeviceId {
            public @Nullable String deviceAccountId;
            public @Nullable String deviceType;
            public @Nullable String serialNumber;
        }

        public static class Description {
            public @Nullable String summary;
            public @Nullable String firstUtteranceId;
            public @Nullable String firstStreamId;
        }

        public Description parseDescription() {
            String description = this.description;
            if (description == null || description.isEmpty() || !description.startsWith("{")
                    || !description.endsWith("}")) {
                return new Description();
            }
            Gson gson = new Gson();
            try {
                Description description1 = gson.fromJson(description, Description.class);
                return description1 != null ? description1 : new Description();
            } catch (JsonSyntaxException e) {
                return new Description();
            }
        }
    }
}
