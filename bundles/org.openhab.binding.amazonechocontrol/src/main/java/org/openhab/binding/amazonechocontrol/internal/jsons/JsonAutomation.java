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
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link JsonAutomation} encapsulate the GSON data of automation query
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonAutomation {
    public @Nullable String automationId;
    public @Nullable String name;
    public @Nullable List<Trigger> triggers;
    public @Nullable TreeMap<String, Object> sequence;
    public @Nullable String status;
    public long creationTimeEpochMillis;
    public long lastUpdatedTimeEpochMillis;

    public static class Trigger {
        public @Nullable Payload payload;
        public @Nullable String id;
        public @Nullable String type;
    }

    public static class Payload {
        public @Nullable String customerId;
        public @Nullable String utterance;
        public @Nullable String locale;
        public @Nullable String marketplaceId;
    }
}
