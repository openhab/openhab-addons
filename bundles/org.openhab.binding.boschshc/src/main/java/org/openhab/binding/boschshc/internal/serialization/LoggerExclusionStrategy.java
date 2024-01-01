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

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * A GSON exclusion strategy that prevents loggers from being serialized and deserialized.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class LoggerExclusionStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(@NonNullByDefault({}) FieldAttributes f) {
        return "logger".equalsIgnoreCase(f.getName());
    }

    @Override
    public boolean shouldSkipClass(@NonNullByDefault({}) Class<?> clazz) {
        return false;
    }
}
