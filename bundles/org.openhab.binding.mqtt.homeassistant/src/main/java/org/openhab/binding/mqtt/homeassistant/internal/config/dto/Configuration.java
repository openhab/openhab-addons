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
package org.openhab.binding.mqtt.homeassistant.internal.config.dto;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Value;

/**
 * Base class for home assistant configurations.
 *
 * @author Cody Cutrer - Initial contribution
 */
@SuppressWarnings("unchecked")
@NonNullByDefault
public interface Configuration {
    Map<String, @Nullable Object> getConfig();

    /**
     * Helper methods to easily retrieve values from the configuration map
     */
    default String getString(String key) {
        return (String) Objects.requireNonNull(getConfig().get(key));
    }

    default @Nullable String getOptionalString(String key) {
        return (String) getConfig().get(key);
    }

    default Value getValue(String key) {
        return (Value) Objects.requireNonNull(getConfig().get(key));
    }

    default @Nullable Value getOptionalValue(String key) {
        return (Value) getConfig().get(key);
    }

    default int getInt(String key) {
        return ((Integer) Objects.requireNonNull(getConfig().get(key))).intValue();
    }

    default @Nullable Integer getOptionalInt(String key) {
        return (Integer) getConfig().get(key);
    }

    default double getDouble(String key) {
        return ((Double) Objects.requireNonNull(getConfig().get(key))).doubleValue();
    }

    default @Nullable Double getOptionalDouble(String key) {
        return (Double) getConfig().get(key);
    }

    default boolean getBoolean(String key) {
        return ((Boolean) Objects.requireNonNull(getConfig().get(key))).booleanValue();
    }

    default @Nullable Boolean getOptionalBoolean(String key) {
        return (Boolean) getConfig().get(key);
    }

    default List<String> getStringList(String key) {
        return (List<String>) Objects.requireNonNull(getConfig().get(key));
    }

    default @Nullable List<String> getOptionalStringList(String key) {
        return (List<String>) getConfig().get(key);
    }

    default @Nullable Set<String> getOptionalStringSet(String key) {
        return (Set<String>) getConfig().get(key);
    }
}
