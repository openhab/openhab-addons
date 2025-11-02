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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Type-safe configuration update result containing the updated configuration values
 * and whether any changes were detected. Provides methods to apply changes to a
 * Configuration object.
 *
 * @param hostname The hostname value (never null, use empty string if not set)
 * @param port The port number
 * @param ssl Whether SSL/HTTPS should be used
 * @param path The path component (never null, use empty string if not set)
 * @param hasChanges Whether any changes were detected compared to the original configuration
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public record ConfigurationUpdate(String hostname, int port, boolean ssl, String path, boolean hasChanges) {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationUpdate.class);

    /**
     * Applies this configuration update to the given configuration object.
     * Only modifies fields if changes were detected.
     *
     * @param target The configuration object to update
     */
    public void applyTo(Configuration target) {
        if (!hasChanges) {
            LOGGER.debug("No configuration changes to apply");
            return;
        }

        target.hostname = hostname;
        target.port = port;
        target.ssl = ssl;
        target.path = path;

        LOGGER.info("Applied configuration changes: hostname={}, port={}, ssl={}, path={}", hostname, port, ssl, path);
    }
}
