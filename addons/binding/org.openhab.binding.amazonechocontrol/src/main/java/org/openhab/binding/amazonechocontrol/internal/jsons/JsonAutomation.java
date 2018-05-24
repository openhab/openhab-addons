/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.internal.jsons;

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
    public @Nullable Trigger @Nullable [] triggers;
    public @Nullable TreeMap<String, @Nullable Object> sequence;
    public @Nullable String status;
    public long creationTimeEpochMillis;
    public long lastUpdatedTimeEpochMillis;

    public class Trigger {
        public @Nullable Payload payload;
        public @Nullable String id;
        public @Nullable String type;
    }

    public class Payload {
        public @Nullable String customerId;
        public @Nullable String utterance;
        public @Nullable String locale;
        public @Nullable String marketplaceId;
    }
}
