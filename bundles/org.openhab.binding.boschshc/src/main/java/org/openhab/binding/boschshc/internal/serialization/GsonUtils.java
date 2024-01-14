/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.serialization;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Utilities for JSON serialization and deserialization using Google Gson.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public final class GsonUtils {
    private GsonUtils() {
        // Utility Class
    }

    /**
     * The default Gson instance to be used for serialization and deserialization.
     * <p>
     * This instance does not serialize or deserialize fields named <code>logger</code>.
     */
    public static final Gson DEFAULT_GSON_INSTANCE = new GsonBuilder()
            .registerTypeAdapter(BoschSHCServiceState.class, new BoschServiceDataDeserializer())
            .addSerializationExclusionStrategy(new LoggerExclusionStrategy())
            .addDeserializationExclusionStrategy(new LoggerExclusionStrategy()).create();
}
