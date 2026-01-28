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
package org.openhab.binding.jellyfin.internal.util.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.Configuration;

/**
 * Generic interface for extracting configuration properties from various source types.
 * Implementations define how to extract configuration data from specific sources
 * (e.g., URI, SystemInfo, etc.) and compare it against current configuration.
 *
 * @param <T> The type of source from which configuration is extracted
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
@FunctionalInterface
public interface ConfigurationExtractor<T> {
    /**
     * Extracts configuration properties from the given source and compares them
     * against the current configuration to determine what has changed.
     *
     * @param source The source object containing configuration information
     * @param current The current configuration to compare against
     * @return ConfigurationUpdate containing the extracted configuration and change status
     */
    ConfigurationUpdate extract(T source, Configuration current);
}
