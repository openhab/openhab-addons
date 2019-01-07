/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.internal.jsons;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;

/**
 * The {@link JsonActivity} encapsulate the GSON data of the push command for push activity
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonActivities {

    public @Nullable Activity @Nullable [] activities;

    public class Activity {
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
        public @Nullable SourceDeviceId @Nullable [] sourceDeviceIds;
        public @Nullable String utteranceId;
        public @Nullable Long version;

        public class SourceDeviceId {
            public @Nullable String deviceAccountId;
            public @Nullable String deviceType;
            public @Nullable String serialNumber;

        }

        public class Description {

            public @Nullable String summary;
            public @Nullable String firstUtteranceId;
            public @Nullable String firstStreamId;

        }

        public Description ParseDescription() {
            String description = this.description;
            if (StringUtils.isEmpty(description)) {
                return new Description();
            }
            Gson gson = new Gson();
            return gson.fromJson(description, Description.class);
        }
    }
}
