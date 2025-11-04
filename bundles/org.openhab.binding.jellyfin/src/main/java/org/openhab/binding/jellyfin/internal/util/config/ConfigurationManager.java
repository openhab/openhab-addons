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
 * Utility class for managing configuration updates using type-safe extractors.
 * Provides a generic interface for extracting and comparing configuration from
 * various source types.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class ConfigurationManager {
    private final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);

    /**
     * Analyzes configuration changes using a generic extractor pattern.
     * Delegates extraction logic to the provided extractor and logs any detected changes.
     *
     * @param <T> The type of source from which configuration is extracted
     * @param extractor The extractor that knows how to extract configuration from the source type
     * @param source The source object containing configuration information
     * @param current The current configuration to compare against
     * @return ConfigurationUpdate containing the extracted configuration and change status
     */
    public <T> ConfigurationUpdate analyze(ConfigurationExtractor<T> extractor, T source, Configuration current) {
        ConfigurationUpdate update = extractor.extract(source, current);

        if (update.hasChanges()) {
            Configuration updated = update.configuration();
            logger.info(
                    "Configuration changes detected from {}: hostname={} -> {}, port={} -> {}, ssl={} -> {}, path={} -> {}",
                    source.getClass().getSimpleName(), current.hostname, updated.hostname, current.port, updated.port,
                    current.ssl, updated.ssl, current.path, updated.path);
        } else {
            logger.debug("No configuration changes detected from {}", source.getClass().getSimpleName());
        }

        return update;
    }
}
